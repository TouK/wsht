/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.spec.TaskDefinition;

/**
 * Holds task instance information.
 * 
 * @author Kamil Eisenbart
 * @author Witek Wołejszo
 * @author Mateusz Lipczyński
 */
@Entity
@Table(name = "TASK")
//@SecondaryTable(name = "TASK_IO", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
public class Task extends Base {

    @Transient
    private final Log LOG = LogFactory.getLog(Task.class);

    @Transient
    private TaskDefinition taskDefinition;

    @Basic
    @Column(nullable = false)
    protected String taskDefinitionKey; // task definition

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

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

    /**
     * Task input message map. Maps message part to message. If
     * document style Web Services are used to start Task, part name
     * should be set to {@link Message.DEFAULT_PART_NAME_KEY}.
     */
    @OneToMany
    @MapKey(name="partName")
    @JoinTable(name="TASK_MSG_INPUT")
    private Map<String, Message> input = new HashMap<String, Message>();
    
    /**
     * Task output message map. Maps message part to message.
     */
    @OneToMany
    @MapKey(name="partName")
    @JoinTable(name="TASK_MSG_OUTPUT")
    private Map<String, Message> output = new HashMap<String, Message>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Status statusBeforeSuspend;

    /**
     * People assigned to different generic human roles.
     */
    @ManyToOne
    private Assignee actualOwner;

    /**
     * This element is used to specify the priority of the task. It is an optional element which value is an integer expression. If not present, the priority of
     * the task is unspecified. 0 is the highest priority, larger numbers identify lower priorities.
     */
    private Integer priority;

    /**
     * Task initiator. Depending on how the task has been instantiated the task initiator may or may not be defined.
     */
    private String createdBy;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date createdOn;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date activationTime;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date expirationTime;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date suspensionTime;

    private boolean skippable;

    private boolean escalated;

    private String presName; // The task’s presentation name.

    private String presSubject; // The task’s presentation subject.

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

    /***************************************************************
     * Constructors                                                *
     ***************************************************************/

    /**
     * Package scope constructor.
     */
    Task() {
        super();
    }

    /**
     * Task constructor.
     * TODO mlp: czym sie roznia konstruktory, usunac ten
     *
     * TODO actualOwner or evaluatedPeopleGroups or people group definitions?
     * TODO ws xml request in constructor?
     *
     * @param taskDefinition
     * @param actualOwner
     * @throws HumanTaskException
     */
    public Task(TaskDefinition taskDefinition, Person actualOwner) throws HumanTaskException {
        this.init(taskDefinition);
        List<Assignee> actualOwnerList = new ArrayList<Assignee>();
        if (actualOwner != null) {
            actualOwnerList.add(actualOwner);
        }
        this.setPotentialOwners(actualOwnerList);
        this.setProperInitialStatus();
    }

    /**
     * Task constructor.
     *
     * TODO actualOwner or evaluatedPeopleGroups or people group definitions?
     * TODO ws xml request in constructor?
     *
     * @param taskDefinition  task definition as an object
     * @param createdBy       person who created the task
     * @param requestXml      input data as XML string
     * @throws HumanTaskException
     */
    public Task(TaskDefinition taskDefinition, Person createdBy, String requestXml) throws HumanTaskException {

        this.init(taskDefinition);
        this.input.put(Message.DEFAULT_PART_NAME_KEY, new Message(requestXml));
        
        //TODO mlp - przepisac wyciaganie Assignees na uzycie interfejsu z TaskDefinition 
        potentialOwners = Collections.EMPTY_LIST;
        
//        // evaluate logical people groups
//        List<TaskDefinition.LogicalPeopleGroup> logicalPeopleGroups = taskDefinition.getLogicalpeopleGroups();
//
//        Map<String, List<Assignee>> lpgPotentialMembers = new HashMap<String, List<Assignee>>();
//        for (TaskDefinition.LogicalPeopleGroup lpg : logicalPeopleGroups) {
//            lpgPotentialMembers.put(lpg.getName(), taskDefinition.evaluate(lpg, this));
//        }
//
//        // TODO assigning potential members to roles
//
//        // TODO potential owners
//        this.potentialOwners = new ArrayList<Assignee>();
//
//        for (String name : taskDefinition.getPotentialOwners()) {
//
//            // WARNING!! t.getTaskDefinition().getPotentialOwners() returns
//            // potential owners group name
//            // not exactly name from this group
//
//            List<Assignee> newMembers = lpgPotentialMembers.get(name);
//
//            for (Assignee assignee : newMembers) {
//                if (!potentialOwners.contains(assignee)) {
//                    potentialOwners.add(assignee);
//                }
//            }
//
//        }

        this.setCreatedBy(createdBy.getName());
        this.setActivationTime(new Date());
        this.setProperInitialStatus();
    }

    /**
     * 
     */
    @PostLoad
    public void postLoad() {
        LOG.info("Post load.");
    }

    /**
     * TODO mlp: describe
     * @param taskDefinition
     * @throws HumanTaskException
     */
    private void init(TaskDefinition taskDefinition) throws HumanTaskException {
        if (taskDefinition == null) {
            throw new pl.touk.humantask.exceptions.IllegalArgumentException("Task definition must not be null.");
        }
        this.taskDefinition = taskDefinition;
        this.taskDefinitionKey = taskDefinition.getTaskName();
    }

    /**
     * Set proper initial status depending on the potential owners.
     *
     * TODO actualOwner should be replaced with potential owner evaluation
     *
     * @throws HumanTaskException
     */
    private void setProperInitialStatus() throws HumanTaskException {
        this.status = Status.CREATED;

        switch (potentialOwners.size()) {
        case 0:
            // pozostajemy w stanie Created i czekamy na dodanie ownersów przez
            // admina
            break;
        case 1:
            this.setActualOwner(potentialOwners.get(0));
            this.setPotentialOwners(potentialOwners);
            this.setStatus(Status.RESERVED);
            break;
        default:
            this.setPotentialOwners(potentialOwners);
            this.setStatus(Status.READY);
            break;
        }
    }

    /***************************************************************
     * Getters & Setters *
     ***************************************************************/

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    /**
     * Sets Task status. Not to be called directly, see: @. TODO must not be used from services.
     * TODO change to private
     * 
     * @param status
     * @throws HumanTaskException
     */
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

                if (status == Status.RESERVED || status == Status.IN_PROGRESS || status == Status.READY || status == Status.SUSPENDED) {
                    isOk = true;
                }

                break;

            case RESERVED:

                if (status == Status.IN_PROGRESS || status == Status.READY || status == Status.SUSPENDED || status == Status.RESERVED) {
                    isOk = true;
                }

                break;

            case IN_PROGRESS:

                if (status == Status.COMPLETED || status == Status.FAILED || status == Status.RESERVED || status == Status.READY || status == Status.SUSPENDED) {
                    isOk = true;
                }

                break;

            default:
                break;

            }

            if (isOk) {

                LOG.info("Changing Task status : " + this + " status from: " + getStatus() + " to: " + status);

                if (status.equals(Status.SUSPENDED)) {
                    statusBeforeSuspend = this.status;
                }

                this.status = status;

            } else {

                LOG.error("Changing Task status: " + this + " status from: " + getStatus() + " to: " + status + " is not allowed.");
                throw new pl.touk.humantask.exceptions.IllegalStateException("Changing Task's: " + this + " status from: " + getStatus() + " to: " + status
                        + " is not allowed, or task is SUSPENDED", status);

            }

        } else {

            LOG.info("Changing Task status: " + this + " status from: NULL to: " + status);
            this.status = status;

        }

    }

    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setSuspentionTime(Date date) {
        this.suspensionTime = (date == null) ? null : (Date) date.clone();
    }

    public Date getSuspentionTime() {
        return (this.suspensionTime == null) ? null : (Date) this.suspensionTime.clone();
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
        return (expirationTime == null) ? null : (Date) expirationTime.clone();
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = (expirationTime == null) ? null : (Date) expirationTime.clone();
    }

    public boolean isSkippable() {
        return skippable;
    }

    public void setSkippable(boolean skippable) {
        this.skippable = skippable;
    }

    public boolean isEscalated() {
        return escalated;
    }

    public void setEscalated(boolean escalated) {
        this.escalated = escalated;
    }

    // /**
    // * Sets task definition and TaskDefinitionKey used to retrieve task definition when {@link Task} is instantiated from persistent store.
    // *
    // * @param taskDefinition
    // */
    // public void setTaskDefinition(TaskDefinition taskDefinition) {
    // this.taskDefinition = taskDefinition;
    // this.setTaskDefinitionKey(taskDefinition.getKey());
    // }
    //
    public TaskDefinition getTaskDefinition() {
        return taskDefinition;
    }

    //
    // public void setTaskDefinitionKey(String taskDefinitionKey) {
    // this.taskDefinitionKey = taskDefinitionKey;
    // }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
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

    /***************************************************************
     * Business methods.                                           *
     ***************************************************************/

    /**
     * Resumes suspended task.
     */
    public void resume() {

        if ((statusBeforeSuspend == Status.READY || statusBeforeSuspend == Status.IN_PROGRESS || statusBeforeSuspend == Status.RESERVED)
                && this.status == Status.SUSPENDED) {
            this.status = statusBeforeSuspend;
        }
        // TODO exception
        /* else throw new HumanTaskException("status before suspend is invalid: "+statusBeforeSuspend.toString()); */
    }

    public void reserve() throws HumanTaskException {
        this.setStatus(Status.RESERVED);
    }

    /***************************************************************
     * Infrastructure methods. *
     ***************************************************************/

    @Override
    public int hashCode() {
        int result = ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Task == false) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Task rhs = (Task) obj;
        return new EqualsBuilder().append(id, rhs.id).isEquals();
    }

}
