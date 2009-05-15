/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.Resource;
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
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Configurable;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import pl.touk.humantask.HumanInteractionsManager;
import pl.touk.humantask.exceptions.HTConfigurationException;
import pl.touk.humantask.exceptions.HTException;
import pl.touk.humantask.exceptions.HTIllegalStateException;
import pl.touk.humantask.model.spec.TaskDefinition;

/**
 * Holds task instance information.
 *
 * @author Kamil Eisenbart
 * @author Witek Wołejszo
 * @author Mateusz Lipczyński
 * @author Warren Crossing
 */
@Entity
@Table(name = "TASK")
@Configurable(preConstruction=true)
public class Task extends Base {

    @Transient
    private final Log log = LogFactory.getLog(Task.class);
    
    /**
     * Human interactions manager injected by IoC container.
     */
    @Transient
    @Resource
    public HumanInteractionsManager humanInteractionsManager;
    
    /**
     * Key {@link Task} definition is looked up in {@link HumanInteractionsManager} by.
     */
    @Column(nullable = false)
    protected String taskDefinitionKey;

    //TODO static?
    public static enum TaskTypes {
        ALL, TASKS, NOTIFICATIONS;
    }

    //TODO static?
    public static enum TaskType {
        TASK, NOTIFICATION;
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
     * document style Web HumanTaskServicesImpl are used to start Task, part name
     * should be set to {@link Message.DEFAULT_PART_NAME_KEY}.
     */
    @OneToMany(cascade = CascadeType.PERSIST)
    @MapKey(name = "partName")
    @JoinTable(name = "TASK_MSG_INPUT")
    private Map<String, Message> input = new HashMap<String, Message>();

    /**
     * Task output message map. Maps message part to message.
     */
    @OneToMany(cascade = CascadeType.PERSIST)
    @MapKey(name = "partName")
    @JoinTable(name = "TASK_MSG_OUTPUT")
    private Map<String, Message> output = new HashMap<String, Message>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Status statusBeforeSuspend;

    /**
     * People assigned to different generic human roles.
     */
    @ManyToOne(cascade = CascadeType.PERSIST)
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

    private Boolean skippable;

    private Boolean escalated;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "TASK_POTENTIAL_OWNERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private Set<Assignee> potentialOwners;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "TASK_EXCLUDED_OWNERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private Set<Assignee> excludedOwners;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "TASK_STAKEHOLDERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private Set<Assignee> taskStakeholders;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "TASK_BUSINESS_AMINISTRATORS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private Set<Assignee> businessAdministrators;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "TASK_NOTIFICATION_RECIPIENTS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private Set<Assignee> notificationRecipients;

    @OneToMany
    private List<Comment> comments;

    @OneToMany(mappedBy = "task", cascade = CascadeType.MERGE)
    private List<Attachment> attachments = new ArrayList<Attachment>();
    
    /**
     * Task presentation parameters recalculated on input message change. 
     * Maps presentation parameter name to its value. Can be used as a where clause parameter
     * in task query operations.
     */
    @OneToMany(cascade = CascadeType.PERSIST)
    @MapKey(name = "name")
    private Map<String, PresentationParameter> presentationParameters = new HashMap<String, PresentationParameter>();

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
     *
     * TODO actualOwner or evaluatedPeopleGroups or people group definitions?
     * TODO ws xml request in constructor?
     *
     * @param taskDefinition  task definition as an object
     * @param createdBy       person who created the task, can be null
     * @param requestXml      input data as XML string
     * @throws HTException
     */
    public Task(TaskDefinition taskDefinition, Person createdBy, String requestXml) throws HTException {

        if (taskDefinition == null) {
            throw new pl.touk.humantask.exceptions.HTIllegalArgumentException("Task definition must not be null.");
        }

        this.taskDefinitionKey = taskDefinition.getTaskName();
        
        Message m = new Message(requestXml);
        this.getInput().put(m.getRootNodeName(), m);

        this.calculatePresentationParameters();
        
        this.potentialOwners        = taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS,          this);
        this.businessAdministrators = taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS,   this);
        this.excludedOwners         = taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.EXCLUDED_OWNERS,           this);
        this.notificationRecipients = taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS,   this);
        this.taskStakeholders       = taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.TASK_STAKEHOLDERS,         this);

        this.setCreatedBy(createdBy == null ? null : createdBy.getName());
        this.setActivationTime(new Date());
        this.status = Status.CREATED;
       
        Person nominatedPerson = this.nominateActualOwner(this.potentialOwners);
        if (nominatedPerson != null) {
            this.setActualOwner(nominatedPerson);
            this.setStatus(Status.RESERVED);
        } else if (!potentialOwners.isEmpty()) {
            this.setStatus(Status.READY);
        }
    }

    /**
     * 
     */
    @PostLoad
    public void postLoad() {
        log.info("Post load.");
    }
    
    @PrePersist
    public void prePersist() {
        log.info("Pre persist.");
        calculatePresentationParameters();
    }

    /**
     * If there is only one person in the given list, it
     * returns this person. Otherwise, it returns null.
     *
     * @param assignees    list of assignees that can contain persons and groups
     * @return             the only person in the list, otherwise null
     */
    protected final Person nominateActualOwner(Set<Assignee> assignees) {
        Person result = null;
        int count = 0;
        for (Assignee assignee : assignees) {
            if (assignee instanceof Person) {
                if (count++ > 0) {
                    break;
                }
                result = (Person)assignee;

            }
        }
        return (count == 1) ? result : null;
    }

    /**
     * Returns a formatted task subject in a required language.
     *
     * @param lang subject language according ISO, e.g. en-US, pl, de-DE
     * @return subject
     */
    public String getSubject(String lang) {
        return this.getTaskDefinition().getSubject(lang, this);
        //return this.taskDefinition.getSubject(lang, this.input);
    }

    /**
     * Returns a formatted task description in a required language and form.
     *
     * @param lang The description language according ISO, e.g. en-US, pl, de-DE
     * @param contentType The content type, text/plain for plain text or text/html for HTML-formatted text.
     * @return description
     */
    public String getDescription(String lang, String contentType) {
        return this.getTaskDefinition().getDescription(lang, contentType, this);
    }
    
//    /**
//     * Returns presenation parameters values.
//     * @return
//     */
//    public Map<String, Object> getPresentationParameters() {
//        return this.getTaskDefinition().getTaskPresentationParameters(this);
//    }
    
    /**
     * Recalculates presentation parameter values. To be called after object creation or
     * input message update.
     * TODO rename to recalcuate
     */
    private void calculatePresentationParameters() {

        log.info("Calculating presentation parameters");

        Map<String, Object> pp = this.getTaskDefinition().getTaskPresentationParameters(this);

        //replace all calculated
        for (Entry<String, Object> entry : pp.entrySet()) {
            
            PresentationParameter p = this.presentationParameters.get(entry.getKey());
            
            if (p != null) {
                
                p.setValue(entry.getValue() == null ? null : entry.getValue().toString());
            } else {
                
                p = new PresentationParameter();
                p.setName(entry.getKey());
                //TODO test
                p.setValue(entry.getValue() == null ? null : entry.getValue().toString());
                
                this.presentationParameters.put(p.getName(), p);
            }
        }
        
        //remove obsolete from presentationParameters
        Set<String> allKeys = this.presentationParameters.keySet();
        for (String key : allKeys) {
            if (!pp.containsKey(key)) {
                allKeys.remove(key);
            }
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
     * @throws HTException
     */
    public void setStatus(Status status) throws HTIllegalStateException {

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

                if (status == Status.RESERVED || status == Status.IN_PROGRESS ||
                        status == Status.READY || status == Status.SUSPENDED) {
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

                log.info("Changing Task status : " + this + " status from: " + getStatus() + " to: " + status);

                if (status.equals(Status.SUSPENDED)) {
                    statusBeforeSuspend = this.status;
                }

                this.status = status;

            } else {

                log.error("Changing Task status: " + this + " status from: " + getStatus() + " to: " + status + " is not allowed.");
                throw new pl.touk.humantask.exceptions.HTIllegalStateException("Changing Task's: " + this + " status from: " + getStatus() + " to: " + status
                        + " is not allowed, or task is SUSPENDED", status);

            }

        } else {

            log.info("Changing Task status: " + this + " status from: NULL to: " + status);
            this.status = status;

        }

    }

    /**
     * Adds an attachment to the task.
     *
     * @param attachment    a new attachment to add
     */
    public void addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
    }

    public List<Attachment> getAttachments() {
        return this.attachments;
    }

    public void setSuspentionTime(Date date) {
        this.suspensionTime = (date == null) ? null : (Date) date.clone();
    }

    public Date getSuspentionTime() {
        return (this.suspensionTime == null) ? null : (Date) this.suspensionTime.clone();
    }

    public Assignee getActualOwner() {
        return this.actualOwner;
    }

    public void releaseActualOwner() {
        this.actualOwner = null;
    }

    public void setActualOwner(Assignee actualOwner) {
        this.actualOwner = actualOwner;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getActivationTime() {
        return this.activationTime;
    }

    public void setActivationTime(Date activationTime) {
        this.activationTime = activationTime;
    }

    public Date getExpirationTime() {
        return (this.expirationTime == null) ? null : (Date) this.expirationTime.clone();
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
        
        if (humanInteractionsManager == null) {
            throw new HTConfigurationException("Human interactions manager not available.", null);
        }
        
        return this.humanInteractionsManager.getTaskDefinition(this.getTaskDefinitionKey());
    }

    //
    // public void setTaskDefinitionKey(String taskDefinitionKey) {
    // this.taskDefinitionKey = taskDefinitionKey;
    // }

    public String getTaskDefinitionKey() {
        return this.taskDefinitionKey;
    }

    public void setPotentialOwners(Set<Assignee> potentialOwners) {
        this.potentialOwners = potentialOwners;
    }

    public Set<Assignee> getPotentialOwners() {
        return this.potentialOwners;
    }

    public void setExcludedOwners(Set<Assignee> excludedOwners) {
        this.excludedOwners = excludedOwners;
    }

    public Set<Assignee> getExcludedOwners() {
        return this.excludedOwners;
    }

    public void setTaskStakeholders(Set<Assignee> taskStakeholders) {
        this.taskStakeholders = taskStakeholders;
    }

    public Set<Assignee> getTaskStakeholders() {
        return taskStakeholders;
    }

    public void setBusinessAdministrators(Set<Assignee> businessAdministrators) {
        this.businessAdministrators = businessAdministrators;
    }

    public Set<Assignee> getBusinessAdministrators() {
        return this.businessAdministrators;
    }

    public void setNotificationRecipients(Set<Assignee> notificationRecipients) {
        this.notificationRecipients = notificationRecipients;
    }

    public Set<Assignee> getNotificationRecipients() {
        return notificationRecipients;
    }

    public Date getCreatedOn() {
        return this.createdOn == null ? null : (Date)this.createdOn.clone();
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn == null ? null : (Date)createdOn.clone();
    }

    /***************************************************************
     * Business methods.                                           *
     ***************************************************************/

//    /**
//     * Resumes suspended task.
//     */
//    public void resume() {
//
//        if ((statusBeforeSuspend == Status.READY || statusBeforeSuspend == Status.IN_PROGRESS || statusBeforeSuspend == Status.RESERVED)
//                && this.status == Status.SUSPENDED) {
//            this.status = statusBeforeSuspend;
//        }
//        // TODO exception
//        /* else throw new HumanTaskException("status before suspend is invalid: "+statusBeforeSuspend.toString()); */
//    }
//
//    /**
//     * Reserves the task.
//     */
//    public void reserve() throws HTIllegalStateException {
//        this.setStatus(Status.RESERVED);
//    }

    /**
     * Evaluates XPath expression in context of the Task. Expression can contain 
     * XPath Exension functions as defined by WS-HumanTask v1. Following
     * XPath functions are implemented:
     * <ul>
     * <li> {@link GetInputXPathFunction} </li>
     * </ul>
     * @param xPathString The XPath 1.0 expression.
     * @param returnType The desired return type. See {@link XPathConstants}.
     * @return The result of evaluating the <code>XPath</code> function as an <code>Object</code>.
     */
    public Object evaluateXPath(String xPathString, QName returnType) {
        
        log.debug("Evaluating: " + xPathString);
        
        Object o = null;
        
        XPathFactory xPathFactory = XPathFactory.newInstance();        
        XPath xpath = xPathFactory.newXPath();
        xpath.setNamespaceContext(new TaskDefinition.HtdNamespaceContext());
        
        xpath.setXPathFunctionResolver(new XPathFunctionResolver() {

            public XPathFunction resolveFunction(QName functionName, int arity) {

                if (functionName == null) {
                    throw new NullPointerException("The function name cannot be null.");
                }

                if (functionName.equals(new QName("http://www.example.org/WS-HT", "getInput", "htd"))) {

                    return new GetInputXPathFunction();
                    
                } else {
                    
                    return null;
                }

            }

        });

        try {

            //TODO create empty document only once
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document emptyDocument = builder.newDocument();

            XPathExpression expr = xpath.compile(xPathString);            
            o = expr.evaluate(emptyDocument, returnType);
               
        } catch (XPathExpressionException e) {
            
            log.error("Error evaluating XPath.", e);
        } catch (ParserConfigurationException e) {
            
            log.error("Error evaluating XPath.", e);
        }
        
        return o;    
    }
    
    /**
     * Implements getInput {@link XPathFunction} - get the data for the part of the task's input message.
     * @author Witek Wołejszo
     */
    private class GetInputXPathFunction implements XPathFunction {
        
        private final Log log = LogFactory.getLog(GetInputXPathFunction.class);

        /**
         * <p>Evaluate the function with the specified arguments.</p>
         * @see XPathFunction#evaluate(List)
         * @param args The arguments, <code>null</code> is a valid value.
         * @return The result of evaluating the <code>XPath</code> function as an <code>Object</code>.
         * @throws XPathFunctionException If <code>args</code> cannot be evaluated with this <code>XPath</code> function.
         */
        public Object evaluate(List args) throws XPathFunctionException {
            
            log.debug("Evaluating: " + args);
            
            String partName = (String) args.get(0);
            
            Message message = getInput().get(partName);
            Document document = null;
            
            if (message == null) {
                throw new XPathFunctionException("Task's input does not contain partName: " + args.get(0));
            }

            try {
                
                document = message.getDomDocument();
                
            } catch (ParserConfigurationException e) {

                throw new XPathFunctionException(e);
            } catch (SAXException e) {
                
                throw new XPathFunctionException(e);
            } catch (IOException e) {
                
                throw new XPathFunctionException(e);
            }
            
            return document == null ? null : document.getElementsByTagName(partName);
        }

    }

    public Map<String, Message> getInput() {
        return input;
    }
    
    /***************************************************************
     * Infrastructure methods.                                     *
     ***************************************************************/

    /**
     * Returns the task hashcode.
     * @return task hash code
     */
    @Override
    public int hashCode() {
        return (id == null ? 0 : id.hashCode());
    }

    /**
     * Checks whether the task is equal to another object.
     * @param obj object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Task == false) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Task rhs = (Task) obj;
        return new EqualsBuilder().append(this.id, rhs.id).isEquals();
    }

}
