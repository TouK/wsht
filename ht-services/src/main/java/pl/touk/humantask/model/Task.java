/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import pl.touk.humantask.HumanInteractionsManager;
import pl.touk.humantask.HumanTaskServices;
import pl.touk.humantask.dao.AssigneeDao;
import pl.touk.humantask.exceptions.HTConfigurationException;
import pl.touk.humantask.exceptions.HTException;
import pl.touk.humantask.exceptions.HTIllegalAccessException;
import pl.touk.humantask.exceptions.HTIllegalStateException;
import pl.touk.humantask.exceptions.HTRecipientNotAllowedException;
import pl.touk.humantask.model.spec.TaskDefinition;

/**
 * Holds task instance information. Provides task business operations.
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
     * Human interactions manager injected by IoC container.
     */
    @Transient
    @Resource
    public AssigneeDao assigneeDao;
    
    /**
     * Key {@link Task} definition is looked up in {@link HumanInteractionsManager} by.
     */
    @Column(nullable = false)
    protected String taskDefinitionKey;

    public static enum TaskTypes {
        ALL, TASKS, NOTIFICATIONS;
    }

    public static enum Status {
    //TODO: ww sprawdzic ERROR, EXITED, OBSOLETE w specyfikacji
        CREATED, READY, RESERVED, IN_PROGRESS, SUSPENDED, COMPLETED, FAILED, ERROR, EXITED, OBSOLETE;

        public String value() {
            return name();
        }

        public static Status fromValue(String v) {
            return valueOf(v);
        }

    }
    
    /**
     * Task operations. Enumeration used to trigger comments.
     */
    private static enum Operations {
        CREATE, STATUS, NOMINATE, CLAIM, START, DELEGATE, RELEASE; 
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Task input message map. Maps message part to message. If
     * document style Web HumanTaskServicesImpl are used to start Task, part name
     * should be set to {@link Message.DEFAULT_PART_NAME_KEY}.
     */
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @MapKey(name = "partName")
    @JoinTable(name = "TASK_MSG_INPUT")
    private Map<String, Message> input = new HashMap<String, Message>();

    /**
     * Task output message map. Maps message part to message.
     */
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
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
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private Person actualOwner;

    /**
     * This element is used to specify the priority of the task. It is an optional element which value is an integer expression. If not present, the priority of
     * the task is unspecified. 0 is the highest priority, larger numbers identify lower priorities.
     */
    private Integer priority;

    /**
     * Task initiator. Depending on how the task has been instantiated the task initiator may or may not be defined.
     */
    private String createdBy;

    @Temporal(javax.persistence.TemporalType.TIME)
    private Date createdOn;

    @Temporal(javax.persistence.TemporalType.TIME)
    private Date activationTime;

    @Temporal(javax.persistence.TemporalType.TIME)
    private Date expirationTime;

    @Temporal(javax.persistence.TemporalType.TIME)
    private Date suspensionTime;

    private Boolean skippable;

    private Boolean escalated;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "TASK_POTENTIAL_OWNERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private Set<Assignee> potentialOwners;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "TASK_EXCLUDED_OWNERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private Set<Assignee> excludedOwners;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "TASK_STAKEHOLDERS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private Set<Assignee> taskStakeholders;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "TASK_BUSINESS_AMINISTRATORS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private Set<Assignee> businessAdministrators;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "TASK_NOTIFICATION_RECIPIENTS", joinColumns = @JoinColumn(name = "TASK"), inverseJoinColumns = @JoinColumn(name = "ASSIGNEE"))
    private Set<Assignee> notificationRecipients;

    @OneToMany(mappedBy = "task", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<Comment> comments = new ArrayList<Comment>();

    @OneToMany(mappedBy = "task", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<Attachment> attachments = new ArrayList<Attachment>();
    
    /**
     * Task presentation parameters recalculated on input message change. 
     * Maps presentation parameter name to its value. Can be used as a where clause parameter
     * in task query operations.
     */
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "task")
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
     * @param taskDefinition  task definition as an object
     * @param createdBy       person who created the task, can be null
     * @param requestXml      input data as XML string
     * @throws HTException
     */
    public Task(TaskDefinition taskDefinition, String createdBy, String requestXml) throws HTException {

        if (taskDefinition == null) {
            throw new pl.touk.humantask.exceptions.HTIllegalArgumentException("Task definition must not be null.");
        }

        this.taskDefinitionKey = taskDefinition.getTaskName();
        
        Message m = new Message(requestXml);
        this.getInput().put(m.getRootNodeName(), m);

        this.recalculatePresentationParameters();
        
        //retrieveExistingAssignees check if group or people with the same name exist
        //and retrieves existing entities
        this.potentialOwners        = this.assigneeDao.saveNotExistingAssignees(taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS,          this));
        this.businessAdministrators = this.assigneeDao.saveNotExistingAssignees(taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS,   this));
        this.excludedOwners         = this.assigneeDao.saveNotExistingAssignees(taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.EXCLUDED_OWNERS,           this));
        this.notificationRecipients = this.assigneeDao.saveNotExistingAssignees(taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS,   this));
        //TODO Compliant implementations MUST ensure that at least one person isassociated with this role at runtime
        this.taskStakeholders       = this.assigneeDao.saveNotExistingAssignees(taskDefinition.evaluateHumanRoleAssignees(GenericHumanRole.TASK_STAKEHOLDERS,         this));

        this.createdBy = createdBy;
        this.createdOn = new Date();
        this.activationTime = new Date();
        this.escalated = false;
        this.status = Status.CREATED;
       
        Person nominatedPerson = this.nominateActualOwner(this.potentialOwners);
        if (nominatedPerson != null) {
            this.actualOwner = nominatedPerson;
            this.setStatus(Status.RESERVED);
        } else if (!this.potentialOwners.isEmpty()) {
            this.setStatus(Status.READY);
        }
        
        recalculatePresentationParameters();
        
        this.addOperationComment(Operations.CREATE);
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
        //recalculatePresentationParameters();
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
        
        this.addOperationComment(Operations.NOMINATE, result);
        
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

    /**
     * Recalculates presentation parameter values. To be called after object creation or
     * input message update.
     */
    private void recalculatePresentationParameters() {

        log.info("Recalculating presentation parameters.");

        Map<String, Object> pp = this.getTaskDefinition().getTaskPresentationParameters(this);

        //replace all calculated
        for (Entry<String, Object> entry : pp.entrySet()) {
            
            PresentationParameter p = this.presentationParameters.get(entry.getKey());
            
            if (p != null) {
                
                p.setValue(entry.getValue() == null ? null : entry.getValue());

            } else {
                
                p = new PresentationParameter();
                p.setTask(this);
                p.setName(entry.getKey());
                //TODO test
                p.setValue(entry.getValue() == null ? null : entry.getValue());

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

    /**
     * Adds an attachment to the task.
     *
     * @param attachment    a new attachment to add
     */
    public void addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
        //TODO addComment
    }

    /***************************************************************
     * Task operations                                             *
     ***************************************************************/
    
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
                    this.statusBeforeSuspend = this.status;
                }

                this.addOperationComment(Operations.STATUS, status);
                this.status = status;

            } else {

                log.error("Changing Task status: " + this + " status from: " + this.status + " to: " + status + " is not allowed.");
                throw new pl.touk.humantask.exceptions.HTIllegalStateException("Changing Task's: " + this + " status from: " + this.status + " to: " + status
                        + " is not allowed, or task is SUSPENDED", status);

            }

        } else {

            log.info("Changing Task status: " + this + " status from: NULL to: " + status);
            this.addOperationComment(Operations.STATUS, status);
            this.status = status;
        }
    }
    
    /**
     * Claims task. Task in READY status can be claimed by
     * people from potential owners group not listed in excluded owners.
     * 
     * @param person                    The Person that claims the task.
     * @throws HTIllegalStateException  Thrown when task is in illegal state for claim i.e. not READY. 
     * @throws HTIllegalAccessException Thrown when task is in illegal state for claim i.e. not READY or person cannot
     *                                  become actual owner i.e. not potential owner or excluded.
     */
    public void claim(Person person) throws HTIllegalStateException, HTIllegalAccessException {

        //actual owner set
        if (this.getActualOwner() != null) {
            throw new HTIllegalStateException("Task not claimable. Actual owner set: " + this.getActualOwner(), this.getStatus());
        }
        
        //TODO test
        //not ready
        if (!this.getStatus().equals(Task.Status.READY)) {
            throw new HTIllegalStateException("Task not claimable. Not READY.", this.getStatus());
        }
        
        //TODO test
        // check if the task can be claimed by person
        if (!this.getPotentialOwners().contains(person)) {
            throw new HTIllegalAccessException("Not a potential owner.", person.getName());
        }
        
        //TODO test
        // check if the person is excluded from potential owners
        if ((this.getExcludedOwners() != null && this.getExcludedOwners().contains(person))) {
            throw new HTIllegalAccessException("Person is excluded from potential owners.", person.getName());
        }

        this.actualOwner = person;
        this.addOperationComment(Operations.CLAIM, person);
        this.setStatus(Task.Status.RESERVED);
    }
    
    /**
     * Releases the Task.
     * @param person                        The person that is releasing the Task.
     * @throws HTIllegalAccessException     The person is not authorised to perform release operation.
     * @throws HTIllegalStateException      Task cannot be released.
     * @see HumanTaskServices.releaseTask
     */
    public void release(Person person) throws HTIllegalAccessException, HTIllegalStateException {

        //TODO test
        if (this.actualOwner == null) {
            throw new HTIllegalAccessException("Task without actual owner cannot be released.");
        }

        //TODO test
        if (!this.actualOwner.equals(person) && !this.getBusinessAdministrators().contains(person)) {
            throw new HTIllegalAccessException("Calling person is neither the task's actual owner not business administrator");
        }

        this.actualOwner = null;
        this.addOperationComment(Operations.RELEASE, person);
        this.setStatus(Status.READY);
    }
    
    /**
     * Starts task.
     * @see HumanTaskServices#startTask(Long, String)
     * @param person
     * @throws HTIllegalStateException 
     * @throws HTIllegalAccessException 
     */
    public void start(Person person) throws HTIllegalStateException, HTIllegalAccessException {
        
        //only potential owner can start the task
        if (!this.getPotentialOwners().contains(person)) {
            throw new HTIllegalAccessException("This person is not permited to start the task.", person.toString());
        }

        //ready
        if (this.getStatus().equals(Status.READY)) {
            
            //TODO can ready contain actual owner??? validate
            this.claim(person);
            this.addOperationComment(Operations.START, person);
            this.setStatus(Status.IN_PROGRESS);
            
        } else if (this.getStatus().equals(Status.RESERVED)) {
            
            org.apache.commons.lang.Validate.notNull(this.getActualOwner());
            if (this.getActualOwner().equals(person)) {
                
                this.addOperationComment(Operations.START, person);
                this.setStatus(Status.IN_PROGRESS);
                
            } else {
                
                throw new HTIllegalAccessException("This person is not permited to start the task. Task is RESERVED.", person.toString());
                
            }
        
        } else {
            
            throw new HTIllegalStateException("Only READY or RESERVED tasks can be started.", this.getStatus());
            
        }
    }
    
    /**
     * Delegates the task.
     * @see HumanTaskServices#delegateTask(Long, String, String)
     * @param person
     * @param delegatee
     * @throws HTIllegalAccessException 
     * @throws HTIllegalStateException 
     * @throws HTRecipientNotAllowedException 
     */
    public void delegate(Person person, Person delegatee) throws HTIllegalAccessException, HTIllegalStateException, HTRecipientNotAllowedException {
        
        if (!(this.potentialOwners.contains(person) || this.businessAdministrators.contains(person) || this.actualOwner.equals(person))) {
            throw new HTIllegalAccessException("Person delegating the task is not a: potential owner, bussiness administrator, actual owner.");
        }
        
        if (!this.getPotentialOwners().contains(delegatee)) {
            throw new HTRecipientNotAllowedException("Task can be delegated only to potential owners.");
        }
        
        if (!Arrays.asList(Status.READY, Status.RESERVED, Status.IN_PROGRESS).contains(this.status)) {
            throw new HTIllegalStateException("Only READY, RESERVED, IN_PROGRESS tasks can ne delegated.", this.status);
        }
        
        this.addOperationComment(Operations.DELEGATE, person);
        this.actualOwner = person;
        
        if (!this.status.equals(Status.RESERVED)) {
            this.setStatus(Status.RESERVED);
        }
    }
    
//  /**
//  * Resumes suspended task.
//  */
// public void resume() {
//
//     if ((statusBeforeSuspend == Status.READY || statusBeforeSuspend == Status.IN_PROGRESS || statusBeforeSuspend == Status.RESERVED)
//             && this.status == Status.SUSPENDED) {
//         this.status = statusBeforeSuspend;
//     }
//     // TODO exception
//     /* else throw new HumanTaskException("status before suspend is invalid: "+statusBeforeSuspend.toString()); */
// }
//
// /**
//  * Reserves the task.
//  */
// public void reserve() throws HTIllegalStateException {
//     this.setStatus(Status.RESERVED);
// }    
    
    /**
     * Returns task definition of this task.
     * @return
     */
    public TaskDefinition getTaskDefinition() {
        
        if (humanInteractionsManager == null) {
            throw new HTConfigurationException("Human interactions manager not available.", null);
        }
        
        return this.humanInteractionsManager.getTaskDefinition(this.getTaskDefinitionKey());
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

    public List<Attachment> getAttachments() {
        return this.attachments;
    }

//    public void setSuspentionTime(Date date) {
//        this.suspensionTime = (date == null) ? null : (Date) date.clone();
//    }

    public Date getSuspentionTime() {
        return (this.suspensionTime == null) ? null : (Date) this.suspensionTime.clone();
    }

    public Assignee getActualOwner() {
        return this.actualOwner;
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

    public Date getActivationTime() {
        return this.activationTime;
    }

    public Date getExpirationTime() {
        return (this.expirationTime == null) ? null : (Date) this.expirationTime.clone();
    }

    public boolean isSkippable() {
        return this.skippable;
    }

    public boolean isEscalated() {
        return this.escalated;
    }

    public String getTaskDefinitionKey() {
        return this.taskDefinitionKey;
    }

    public Set<Assignee> getPotentialOwners() {
        return this.potentialOwners;
    }

    public Set<Assignee> getExcludedOwners() {
        return this.excludedOwners;
    }

    public Set<Assignee> getTaskStakeholders() {
        return this.taskStakeholders;
    }

    public Set<Assignee> getBusinessAdministrators() {
        return this.businessAdministrators;
    }

    public Set<Assignee> getNotificationRecipients() {
        return this.notificationRecipients;
    }

    public Date getCreatedOn() {
        return this.createdOn == null ? null : (Date)this.createdOn.clone();
    }

    public Map<String, Message> getInput() {
        return this.input;
    }
    
    public Map<String, Message> getOutput() {
        return this.output;
    }
    
    /***************************************************************
     * Infrastructure methods.                                     *
     ***************************************************************/

    /**
     * Adds a comment related to operation taking place.
     * @param operation     performed operation
     * @param people        people involved, starting with person invoking the operation
     */
    public void addOperationComment(Operations operation, Person ... people) {
       
        String content = null;
        
        switch (operation) {
        case CREATE:
            content = "Created.";
            break;
        case START:
            content = "Started by " + people[0];
            break;
        case CLAIM:            
            content = "Claimed by " + people[0];
            break;
        case DELEGATE:            
            content = "Delegated by " + people[0] + " to " + people[1];
            break;
        case NOMINATE:            
            content = "Nominated to " + people[0];
            break;
        case RELEASE:            
            content = "Released by " + people[0];
            break;
        default:
            break;
        }
        
        if (content != null) {
            this.comments.add(new Comment(content));
        }
    }
    
    /**
     * Adds a comment related to operation taking place.
     * @param operation     performed operation
     * @param people        people involved, starting with person invoking the operation
     */
    public void addOperationComment(Operations operation, Status status) {
       
        String content = null;
        
        switch (operation) {
        case STATUS:
            content = "Status changed to " + status;
        default:
            break;
        }
        
        if (content != null) {
            this.comments.add(new Comment(content));
        }
    }
    
    /**
     * Returns presentation parameter values.
     * @return the presentation parameter values
     */
    public Map<String, Object> getPresentationParameterValues() {
        Map<String, Object> result = new HashMap<String, Object>();
        for (Map.Entry<String, PresentationParameter> pp : this.presentationParameters.entrySet()) {
            result.put(pp.getKey(), pp.getValue().getValue());
        }
        return result;
    }


    /**
     * Evaluates XPath expression in context of the Task. Expression can contain 
     * XPath Extension functions as defined by WS-HumanTask v1. Following
     * XPath functions are implemented:
     * <ul>
     * <li> {@link GetInputXPathFunction} </li>
     * <li> {@link GetOutputXPathFunction} </li>
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
                }
                
                if (functionName.equals(new QName("http://www.example.org/WS-HT", "getOutput", "htd"))) {

                    return new GetOutputXPathFunction();
                } 
                    
                return null;
            }

        });

        try {

            //TODO create empty document only once
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document emptyDocument = builder.newDocument();

            XPathExpression expr = xpath.compile(xPathString);            
            o = expr.evaluate(emptyDocument, returnType);
               
        } catch (XPathExpressionException e) {
            
            log.error("Error evaluating XPath: " + xPathString, e);
        } catch (ParserConfigurationException e) {
            
            log.error("Error evaluating XPath:  " + xPathString, e);
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
    
    /**
     * Implements getOutput {@link XPathFunction} - get the data for the part of the task's output message.
     * @author Witek Wołejszo
     */
    private class GetOutputXPathFunction implements XPathFunction {
        
        private final Log log = LogFactory.getLog(GetOutputXPathFunction.class);

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
            
            Message message = getOutput().get(partName);
            Document document = null;
            
            if (message == null) {
                throw new XPathFunctionException("Task's output does not contain partName: " + args.get(0));
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
    
    /***************************************************************
     * Infrastructure methods.                                     *
     ***************************************************************/

    /**
     * Returns the task hashcode.
     * @return task hash code
     */
    @Override
    public int hashCode() {
        return (this.id == null ? 0 : this.id.hashCode());
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
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", this.id).append("name", this.getTaskDefinition().getTaskName()).append("actualOwner", this.getActualOwner()).toString();
    }

}
