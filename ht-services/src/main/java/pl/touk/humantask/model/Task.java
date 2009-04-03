/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.collections.BeanMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Index;

import pl.touk.humantask.HumanTaskException;
import pl.touk.humantask.spec.TaskDefinition;

/**
 * Holds task instance information.
 * 
 * @author Witek Wołejszo
 */
@Entity
@Table(name = "TASK")
@SecondaryTable(name = "TASK_IO", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
public class Task {

    @Transient
    private final Log LOG = LogFactory.getLog(Task.class);

    // TODO Tuplizer ???
    @Transient
    private TaskDefinition taskDefinition;

    @Basic
    @Index(name = "TAKS_DEFKEY_IDX")
    @Column(name = "DEFKEY", nullable = false)
    private String taskDefinitionKey;

    public static enum Status {

        CREATED, READY, RESERVED, IN_PROGRESS, SUSPENDED, COMPLETED, FAILED, ERROR, EXITED, OBSOLETE;

        public String value() {
            return name();
        }

        public static Status fromValue(String v) {
            return valueOf(v);
        }

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, table = "TASK_IO")
    @Lob
    private String requestXml;

    @Basic(fetch = FetchType.LAZY)
    @Column(table = "TASK_IO")
    @Lob
    private String responseXml;

    @Column(table = "TASK_IO")
    private String outputXml;

    @Column(table = "TASK_IO")
    private String faultXml;

    @Column
    private String partName;

    @Column
    private String faultName;

    @Enumerated(EnumType.STRING)
    @Index(name = "TASK_STATUS_IDX")
    @Column(nullable = false)
    private Status status;

    private Status statBeforeSuspend;

    /**
     * People assigned to different generic human roles.
     */
    @Index(name = "TASK_ACTUAL_OWNER_IDX")
    @ManyToOne
    private Assignee actualOwner;

    /**
     * This element is used to specify the priority of the task. It is an
     * optional element which value is an integer expression. If not present,
     * the priority of the task is unspecified. 0 is the highest priority,
     * larger numbers identify lower priorities.
     */
    private Integer priority;

    /**
     * Task initiator. Depending on how the task has been instantiated the task
     * initiator may or may not be defined.
     */
    private String createdBy;

    private Date activationTime;

    private Date expirationTime;

    @Column
    private Date suspentionTime;

    private boolean isSkipable;

    private boolean escalated;

    // Human roles assigned to Task instance during creation

    // initiator???

    @ManyToMany
    @JoinTable(name = "TASK_POTENTIAL_OWNERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private List<Assignee> potentialOwners;

    @ManyToMany
    @JoinTable(name = "TASK_EXCLUDED_OWNERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private List<Assignee> excludedOwners;

    @ManyToMany
    @JoinTable(name = "TASK_STAKEHOLDERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private List<Assignee> taskStakeholders;

    @ManyToMany
    @JoinTable(name = "TASK_BUSINESS_AMINISTRATORS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private List<Assignee> businessAdministrators;

    @ManyToMany
    @JoinTable(name = "TASK_NOTIFICATION_RECIPIENTS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private List<Assignee> notificationRecipients;

    @OneToMany
    private List<Comment> comments;

    @OneToMany(mappedBy = "task", cascade = CascadeType.PERSIST)
    private List<Attachment> attachments = new ArrayList<Attachment>();

    // TODO deadlines

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) throws HumanTaskException {

        boolean isOk = false;

        // check if change is valid for current state
        if (this.status != null) {

            switch (this.status) {

            case CREATED:

                if (status == Status.READY || status == Status.RESERVED) {
                    isOk = true;
                }

                break;

            case READY:

                if (status == Status.RESERVED || status == Status.IN_PROGRESS
                        || status == Status.SUSPENDED) {
                    isOk = true;
                }

                break;

            case RESERVED:

                if (status == Status.IN_PROGRESS || status == Status.READY
                        || status == Status.SUSPENDED
                        || status == Status.RESERVED) {
                    isOk = true;
                }

                break;

            case IN_PROGRESS:

                if (status == Status.COMPLETED || status == Status.FAILED
                        || status == Status.RESERVED || status == Status.READY
                        || status == Status.SUSPENDED) {
                    isOk = true;
                }

                break;

            default:
                break;

            }

            if (isOk) {

                LOG.info("Changing Task status : " + this + " status from: " + getStatus() + " to: " + status);

                if (status.equals(Status.SUSPENDED)) {
                    statBeforeSuspend = this.status;
                }

                this.status = status;

            } else {

                LOG.error("Changing Task status: " + this + " status from: " + getStatus() + " to: " + status + " is not allowed.");
                throw new HumanTaskException("Changing Task's: " + this + " status from: " + getStatus() + " to: " + status + " is not allowed, or task is SUSPENDED");

            }

        } else {

            LOG.info("Changing Task status: " + this + " status from: NULL to: " + status);
            this.status = status;

        }

    }

    /**
     * Removing suspended
     */
    // resume while suspended
    //TODO test it
    public void resume() {
        if ((statBeforeSuspend == Status.READY
                || statBeforeSuspend == Status.IN_PROGRESS || statBeforeSuspend == Status.RESERVED)
                && this.status == Status.SUSPENDED)
            this.status = statBeforeSuspend;

        // exceptions ??
    };

    public void deleteOutput() {
        outputXml = null;
        partName = null;
    }

    public void setOutput(String partName, String outputXml) throws HumanTaskException {

        if (partName != null) {
            this.partName = partName;
        }

        this.outputXml = outputXml;
    }

    /**
     * set fault name and foult data
     * 
     * @param fName
     * @param fXml
     */
    public void setFault(String fName, String fXml) {
        faultName = fName;
        faultXml = fXml;
    }

    /**
     * delete fault name and fault data
     */
    public void deleteFault() {
        faultName = null;
        faultXml = null;
    }

    public String getFault() {
        return faultXml;
    }

    public String getFaultName() {
        return faultName;
    }

    public String getFaultData() {
        return faultXml;
    }

    // TODO fult definitions
    /**
     * check if given foultname exists
     * 
     * @param foultName
     * @return
     */
    public boolean findFoult(String foultName) {

        return true;
        // return false;

    }

    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setSuspentionTime(Date date) {
        suspentionTime = date;
    }

    public Date getSuspentionTime() {
        return suspentionTime;
    }

    public String getOutput(String pName) {

        return outputXml;
    }

    public Assignee getActualOwner() {
        return actualOwner;
    }

    public void releaseActualOwner() {
        actualOwner = null;
    }

    public void setActualOwner(Assignee actualOwner) {
        this.actualOwner = actualOwner;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getActivationTime() {
        return activationTime;
    }

    public void setActivationTime(Date activationTime) {
        this.activationTime = activationTime;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    public boolean isSkipable() {
        return isSkipable;
    }

    public void setSkipable(boolean isSkipable) {
        this.isSkipable = isSkipable;
    }

    public boolean isEscalated() {
        return escalated;
    }

    public void setEscalated(boolean escalated) {
        this.escalated = escalated;
    }

    public void setTaskDefinition(TaskDefinition taskDefinition) {
        this.taskDefinition = taskDefinition;
        this.setTaskDefinitionKey(taskDefinition.getKey());
    }

    public TaskDefinition getTaskDefinition() {
        return taskDefinition;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setRequestXml(String requestXml) {
        this.requestXml = requestXml;
    }

    public String getRequestXml() {
        return requestXml;
    }

    public void setPotentialOwners(List<Assignee> potentialOwners) {
        this.potentialOwners = potentialOwners;
    }

    public List<Assignee> getPotentialOwners() {
        return potentialOwners;
    }

    public void setExcludedOwners(List<Assignee> excludedOwners) {
        this.excludedOwners = excludedOwners;
    }

    public List<Assignee> getExcludedOwners() {
        return excludedOwners;
    }

    public void setTaskStakeholders(List<Assignee> taskStakeholders) {
        this.taskStakeholders = taskStakeholders;
    }

    public List<Assignee> getTaskStakeholders() {
        return taskStakeholders;
    }

    public void setBusinessAdministrators(List<Assignee> businessAdministrators) {
        this.businessAdministrators = businessAdministrators;
    }

    public List<Assignee> getBusinessAdministrators() {
        return businessAdministrators;
    }

    public void setNotificationRecipients(List<Assignee> notificationRecipients) {
        this.notificationRecipients = notificationRecipients;
    }

    public List<Assignee> getNotificationRecipients() {
        return notificationRecipients;
    }

    @Override
    public String toString() {
        BeanMap bm = new BeanMap(this);
        return bm.toString();
    }

}
