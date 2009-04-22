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
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import pl.touk.humantask.exceptions.HTIllegalStateException;
import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.spec.HumanInteractionsManagerInterface;
import pl.touk.humantask.spec.TaskDefinition;

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
//@SecondaryTable(name = "TASK_IO", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
public class Task extends Base {

    @Transient
    private final Log log = LogFactory.getLog(Task.class);

    @Transient
    private TaskDefinition taskDefinition;

    /**
     * Key {@link Task} definition is looked up in {@link HumanInteractionsManagerInterface} by.
     */
    @Column(nullable = false)
    protected String taskDefinitionKey;

    public enum TaskTypes {
        ALL, TASKS, NOTIFICATIONS;
    }

    public enum TaskType {
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
     * document style Web Services are used to start Task, part name
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

    private Boolean skippable;

    private Boolean escalated;

    // Human roles assigned to Task instance during creation
    // TODO initiator???

    @ManyToMany
    @JoinTable(name = "TASK_POTENTIAL_OWNERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private List<Assignee> potentialOwners;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "TASK_EXCLUDED_OWNERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private List<Assignee> excludedOwners;

    //THIS ONE
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "TASK_STAKEHOLDERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private List<Assignee> taskStakeholders;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "TASK_BUSINESS_AMINISTRATORS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private List<Assignee> businessAdministrators;

    @ManyToMany(cascade = CascadeType.PERSIST)
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
     *
     * TODO actualOwner or evaluatedPeopleGroups or people group definitions?
     * TODO ws xml request in constructor?
     *
     * @param taskDefinition  task definition as an object
     * @param createdBy       person who created the task, can be null
     * @param requestXml      input data as XML string
     * @throws HumanTaskException
     */
    public Task(TaskDefinition taskDefinition, Person createdBy, String requestXml) throws HumanTaskException {

        if (taskDefinition == null) {
            throw new pl.touk.humantask.exceptions.HTIllegalArgumentException("Task definition must not be null.");
        }

        this.taskDefinition = taskDefinition;
        this.taskDefinitionKey = taskDefinition.getTaskName();
        this.input.put(Message.DEFAULT_PART_NAME_KEY, new Message(requestXml));
        
        this.potentialOwners        = taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS,          this.input);
        this.businessAdministrators = taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS,   this.input);
        this.excludedOwners         = taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.EXCLUDED_OWNERS,           this.input);
        this.notificationRecipients = taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS,   this.input);
        this.taskStakeholders       = taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.TASK_STAKEHOLDERS,         this.input);

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

    /**
     * If there is only one person in the given list, it
     * returns this person. Otherwise, it returns null.
     *
     * @param assignees    list of assignees that can contain persons and groups
     * @return             the only person in the list, otherwise null
     */
    protected final Person nominateActualOwner(List<Assignee> assignees) {
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
        return this.taskDefinition.getSubject(lang, this.input);
    }

    /**
     * Returns a formatted task description in a required language and form
     *
     * @param lang          description language according ISO, e.g. en-US, pl, de-DE
     * @param contentType   text/plain for plain text or text/html for HTML-formatted text
     * @return description
     */
    public String getDescription(String lang, String contentType) {
        return this.taskDefinition.getDescription(lang, contentType, this.input);
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
        return this.taskDefinition;
    }

    //
    // public void setTaskDefinitionKey(String taskDefinitionKey) {
    // this.taskDefinitionKey = taskDefinitionKey;
    // }

    public String getTaskDefinitionKey() {
        return this.taskDefinitionKey;
    }

    public void setPotentialOwners(List<Assignee> potentialOwners) {
        this.potentialOwners = potentialOwners;
    }

    public List<Assignee> getPotentialOwners() {
        return this.potentialOwners;
    }

    public void setExcludedOwners(List<Assignee> excludedOwners) {
        this.excludedOwners = excludedOwners;
    }

    public List<Assignee> getExcludedOwners() {
        return this.excludedOwners;
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
        return this.businessAdministrators;
    }

    public void setNotificationRecipients(List<Assignee> notificationRecipients) {
        this.notificationRecipients = notificationRecipients;
    }

    public List<Assignee> getNotificationRecipients() {
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

    /**
     * Reserves the task.
     */
    public void reserve() throws HTIllegalStateException {
        this.setStatus(Status.RESERVED);
    }

    /**
     * Evaluates XPath expression in context of the Task. Expression can contain 
     * XPath Exension functions as defined by WS-HumanTask v1. Following
     * XPath functions are implemented:
     * <ul>
     * <li> {@link GetInputXPathFunction} </li>
     * </ul>
     * @param xpathString the XPath 1.0 expression.
     * @return The result of evaluating the <code>XPath</code> function as an <code>Object</code>.
     */
    public Object evaluateXPath(String xpathString) {
        
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

            XPathExpression expr = xpath.compile(xpathString);            
            o = expr.evaluate(emptyDocument, XPathConstants.NODE);
               
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

        /**
         * <p>Evaluate the function with the specified arguments.</p>
         * @see XPathFunction#evaluate(List)
         * @param args The arguments, <code>null</code> is a valid value.
         * @return The result of evaluating the <code>XPath</code> function as an <code>Object</code>.
         * @throws XPathFunctionException If <code>args</code> cannot be evaluated with this <code>XPath</code> function.
         */
        public Object evaluate(List args) throws XPathFunctionException {
            
            Message message = input.get(args.get(0));
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
            
            return document == null ? null : document.getChildNodes();
        }

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
