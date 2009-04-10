/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */
package pl.touk.humantask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.example.ws_ht.api.wsdl.IllegalArgumentFault;
import org.example.ws_ht.api.wsdl.IllegalOperationFault;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import pl.touk.humantask.dao.AssigneeDao;
import pl.touk.humantask.dao.TaskDao;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Attachment;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Group;
import pl.touk.humantask.model.Person;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.model.TaskTypes;
import pl.touk.humantask.model.Task.Status;
import pl.touk.humantask.spec.TaskDefinition;

/**
 * Human task engine services.
 * 
 * @author Kamil Eisenbart
 * @author Witek Wołejszo
 */
public class Services {

    private final Log log = LogFactory.getLog(Services.class);
    
    /**
     * DAO for accessing {@link Task}s.
     */
    private TaskDao taskDao;
    
    /**
     * DAO for accessing {@link Assignee}s.
     */
    private AssigneeDao assigneeDao;
    
    /**
     * {@link PeopleQuery} implementation for user evaluation.
     */
    private PeopleQuery peopleQuery;
    
    /**
     * Definitions of tasks available in WSHT.
     */
    private List<TaskDefinition> taskDefinitions;

    /**
     * Creates {@link Task} instance basing on a definition.
     * The definitions are provided by the services. They can come from a file,
     * e.g. htd1.xml, a database or any other source.
     * We assume that the task is activated upon creation provided that it has any potential owners.
     * Upon creation the following sets of persons are evaluated: <ul>
     * <li>task initiators - @see #taskInitiators</li>
     * <li>task stakeholders - @see #taskStakeholders</li>
     * <li>potential owners - @see #potentialOwners</li>
     * <li>excluded owners - @see #excludedOwners</li>
     * <li>business administrators - @see #businessAdministrators</li>
     * <li>notification recipients - @see #notificationRecipients</li>
     * </ul>
     * Those groups have roles in aspect of the task.
     * The source of a group is a part of the definition - it can
     * be a logical group, a set of people or a set of groups, which can be
     * evaluated basing on the requestXml contents.
     * The status after the operation depends on the count of potential owners:<br/>
     * 0 - CREATED, it is now due to the administrator to add potential owners<br/>
     * 1 - RESERVED, since there's only one possibility;<br/>
     * 2 or more - READY - the potential owners are welcome to take the task.<br/>
     * Request data depends on the task definition, e.g. approving a claim requires a money amount,
     * which may not make sense in case of another task. Request data might be empty
     * in some cases.</br>
     * If the task initiators are not empty and createdBy is not empty, it is checked whether task initiators contain createdBy.
     * If not, it is not allowed to create the task.
     * Depending on the situation, createdBy may be empty.
     * At the end, the new task is stored.
     *
     * @param taskName name of the task template from the definition file
     * @param createdBy user creating task
     * @param requestXml xml request used to invoke business method; can contain task-specific attributes, like last name, amount, etc.
     * @return created Task
     * @throws HumanTaskException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Task createTask(String taskName, String createdBy, String requestXml) throws HumanTaskException {

        log.info("Creating task: " + taskName + " , createdBy: " + createdBy);

        // setOutput(pName, output)
        TaskDefinition taskDefinition = null;
        for (TaskDefinition taskDefinitionConfigured : taskDefinitions) {
            if (taskName.equals(taskDefinitionConfigured.getName()) && taskDefinitionConfigured.getInstantiable()) {
                //newTask.setTaskDefinition(taskDefinition);
                taskDefinition = taskDefinitionConfigured;
                break;
            }
        }

        Person actualOwner = assigneeDao.getPerson(createdBy);
        if (actualOwner == null) {
            actualOwner = new Person(createdBy);
            assigneeDao.create(actualOwner);
        }
        
        Task newTask = new Task(actualOwner, taskDefinition);
        newTask.setRequestXml(requestXml);
        
        if (newTask.getTaskDefinition() == null) {
            throw new HumanTaskException("No definition found for task: " + taskName);
        }

        // evaluate logical people groups
        List<TaskDefinition.LogicalPeopleGroup> logicalPeopleGroups = newTask.getTaskDefinition().getLogicalpeopleGroups();

        Map<String, List<Assignee>> lpgPotentialMembers = new HashMap<String, List<Assignee>>();
        for (TaskDefinition.LogicalPeopleGroup lpg : logicalPeopleGroups) {
            lpgPotentialMembers.put(lpg.getName(), getPeopleQuery().evaluate(lpg, newTask));
        }

        // TODO assigning potential members to roles

        // TODO potential owners
        List<Assignee> potentialOwners = new ArrayList<Assignee>();

        for (String name : newTask.getTaskDefinition().getPotentialOwners()) {

            // WARNING!! t.getTaskDefinition().getPotentialOwners() returns
            // potential owners group name
            // not exactly name from this group

            List<Assignee> newMembers = lpgPotentialMembers.get(name);

            for (Assignee assignee : newMembers) {
                if (!potentialOwners.contains(assignee)) {
                    potentialOwners.add(assignee);
                }
            }

        }

        switch (potentialOwners.size()) {
            case 0:
                // pozostajemy w stanie Created i czekamy na dodanie ownersów przez
                // admina
                break;
            case 1:
                newTask.setActualOwner(potentialOwners.get(0));
                newTask.setPotentialOwners(potentialOwners);
                newTask.setStatus(Status.RESERVED);
                break;
            default:
                newTask.setPotentialOwners(potentialOwners);
                newTask.setStatus(Status.READY);
                break;
        }

        newTask.setCreatedBy(createdBy);
        newTask.setActivationTime(new Date());

        taskDao.create(newTask);

        return newTask;

    }
    
    /**
     * Returns task owned by specified person.
     * 
     * @param owner
     * @return
     */
    public List<Task> getMyTasks(String personName) {
        Person person = assigneeDao.getPerson(personName);
        return taskDao.getTasks(person);
    }
    
    /**
     * Later.
     */

    /**
     * Claims task. Sets status to Reserved. Only potential owners can claim the
     * task. Excluded owners may not become an actual or potential owner and
     * thus they may not reserve or start the task.
     * 
     * @param task
     * @param assigneeName
     * @return
     * @throws HumanTaskException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Task claimTask(Task task, String assigneeName) throws HumanTaskException {

        Person person = assigneeDao.getPerson(assigneeName);
        log.info("Claiming: " + task + " Person: " + person);

        if (task.getId() == null) {
            log.error("Task has to be persisted before performing any operation.");
            throw new RuntimeException("Task has to be persisted before performing any operation.");
        }

        if (task.getActualOwner() != null) {
            log.error("Task with actual owner cannot be claimed.");
            throw new HumanTaskException("Task with actual owner cannot be claimed.");
        }

        // check if the task can be claimed by person
        // TODO check person in group membership
        if (task.getPotentialOwners().contains(person) && !(task.getExcludedOwners() != null && task.getExcludedOwners().contains(person))) {

            log.info("Setting actual owner of: " + task + " to: " + person);
            task.setActualOwner(person);

        } else {

            log.error("Person: " + person + " is not a potential owner.");
            throw new HumanTaskException("Person: " + person + " is not a potential owner.");

        }

        //task.setStatus(Status.RESERVED);
        task.reserve();

        taskDao.update(task);

        return task;

    }

    /**
     * Starts task. Sets status to inProgess. Actual Owner Potential Owners
     * (state Ready)
     * 
     * @param task
     * @param personName
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Task startTask(Task task, String personName) throws HumanTaskException {

        if (task.getId() == null) {
            log.error("Task has to be persisted before performing any operation.");
            throw new RuntimeException("Task has to be persisted before performing any operation.");
        }

        Person person = assigneeDao.getPerson(personName);

        // TODO exception if not found

        // TODO w stanie ready musi byc podany person
        if (task.getStatus() == Status.READY) {
            if (!task.getPotentialOwners().contains(person)) {
                log.error("This person is not permited to start the task");
                throw new HumanTaskException("This person is not permited to start the task");
            }
            task.setActualOwner(person);
        } else if (!(person == null) && !task.getPotentialOwners().contains(person)) {
            log.error("This person is not permited to start the task");
            throw new HumanTaskException("This person is not permited to start the task");
        }

        task.setStatus(Status.IN_PROGRESS);
        //TODO was update
        taskDao.create(task);

        return task;
    }

    /**
     * Loads single task from persistent store.
     * TODO implement
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Task loadTask(Long taskId) {

        Task task = taskDao.fetch(taskId);
        //task.setTaskDefinition(findTaskDefinitionByKey(task.getTaskDefinitionKey()));

        // TODO throw an exception if no definition found

        return task;
    }

    /**
     * Delegates task to other person.
     * 
     * @param task
     * @param assigneeName
     * @throws HumanTaskException
     */
    public Task delegateTask(Task task, String assigneeName) throws HumanTaskException {

        Person person = assigneeDao.getPerson(assigneeName);

        task.setStatus(Status.RESERVED);
        this.addPotentialOwner(task, person);
        task.setActualOwner(person);

        taskDao.update(task);

        return task;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    // TODO How to distinguish groups and people? now works on people only
    public Task forwardTask(Task task, String assigneeName) throws HumanTaskException {

        Person owner = (Person) task.getActualOwner();

        Assignee assignee = assigneeDao.getPerson(assigneeName);

        // List<Person> list = this.;
        // log.info(g.getPeople().size());

        if (assignee == null) {
            log.error("Task without assignee cannot be forwarded.");
            throw new HumanTaskException("Task without assignee cannot be forwarded");
        }

        // TODO check who is forwarding the task
        /*
         * if ((t.getStatus()==Status.IN_PROGRESS ||
         * t.getStatus()==Status.RESERVED)&& owner==null){
         * log.error("this person or a group cannot forward this task"); throw
         * new
         * HumanTaskException("this person or a group cannot forward this task"
         * ); }
         */

        if (task.getStatus() == Status.IN_PROGRESS || task.getStatus() == Status.RESERVED) {
            this.releaseTask(task, owner.getName());
        }

        if (!(task.getStatus() == Status.READY)) {

            log.error("Task with other stus then Ready cannot be forwarded");
            throw new HumanTaskException("Task with other stus then Ready cannot be forwarded");
        }

        // erasing actual owner from potential owners or all potential owners
        // group
        if (owner != null) {

            this.removeAssigneeFromPotentialOwners(task, owner);
        } else {

            task.setPotentialOwners(null);
        }

        if (assignee instanceof Group) {

            Group g = (Group) assignee;
            List<Person> people = g.getPeople();
            // TODO adding to potentialownersgroup new owners form the group
            this.addPotentialOwner(task, people);

        } else {

            this.addPotentialOwner(task, assignee);

        }

        // adding new potential owners
        // t = t.addPotentialOwner(assignee);

        taskDao.update(task);
        return task;
    }

    /**
     * removes person form potential owners
     * 
     * @param p
     */
    public void removeAssigneeFromPotentialOwners(Task task, Assignee p) {
        int i = 0;
        boolean removed = false;
        List<Assignee> list = task.getPotentialOwners();
        Assignee assignee;
        while (!removed) {
            assignee = list.get(i);

            if (((Person) assignee).equals(p)) {
                list.remove(i);
                removed = true;
            }
            i++;
        }
    }

    /**
     * Retrieve the task details. This operation is used to obtain the data required
    to display a task list, as well as the details for the individual tasks.
     * @param personName If specified and no work queue has been specified then only personal tasks are
    returned, classified by genericHumanRole.
     * @param taskType one of ALL, NOTIFICATIONS, TASKS.
     * @param genericHumanRole A classifier of names contained in the task. 
     * @param workQueue  If the work queue is specified then only tasks having a 
     * work queue and generic human role are returned.
     * @param statusList selects the tasks whose status is one of those specified in List.
     * @param whereClause - an [Hibernate] SQL Expression added to the criteria 
     * These additional fields may be used 
     * (ID,TaskType,Name,Status,Priority,CreatedOn,ActivationTime,ExpirationTime
     * ,StartByExists,CompleteByExists,RenderMethExists,Escalated,PrimarySearchBy);
     * @param createdOnClause - an [Hibernate] SQL Expression performed on an xsd:date. 
     * @param maxTasks - the maximum number of results returned in the List after ordering by activationTime.
     * @return List of Tasks which meet the criteria.
     */
    public List<Task> getMyTasks(String personName, TaskTypes taskType, GenericHumanRole genericHumanRole, String workQueue, List<Task.Status> status, String whereClause, String createdOnClause, Integer maxTasks) throws HumanTaskException {
        Person person = assigneeDao.getPerson(personName);
        return taskDao.getTasks(person, taskType, genericHumanRole, workQueue, status, whereClause, createdOnClause, maxTasks);
    }

    /**
     * releases task from Inprogress and Reserved state
     * 
     * @param task
     * @param personName
     * @throws Exception
     */
    public Task releaseTask(Task task, final String personName) throws HumanTaskException {

        Person person = (Person) assigneeDao.getPerson(personName);

        if (task.getActualOwner() == null || !task.getActualOwner().equals((Person) person)) {
            log.error("Task without actual owner cannot be released	");
            throw new HumanTaskException("Task without actual owner cannot be released");
        }

        task.releaseActualOwner();
        task.setStatus(Status.READY);

        taskDao.update(task);

        return task;
    }

    /**
     * Stops task in progres.
     * 
     * @param task
     * @return
     */
    public Task stopTaskInProgress(Task task) throws HumanTaskException {

        if (!(task.getStatus() == Status.IN_PROGRESS)) {
            log.error("Task has to be In_Progress");
            throw new HumanTaskException("Cannot stop task that is not in progress");
        }

        task.setStatus(Status.RESERVED);
        taskDao.update(task);

        return task;

    }

    /**
     * Returns task definition object matching given key.
     * 
     * @param key
     * @return task definition
     */
    private TaskDefinition findTaskDefinitionByKey(String key) {

        for (TaskDefinition td : taskDefinitions) {
            if (key.equals(td.getKey())) {
                return td;
            }
        }

        return null;
    }

    /**
     * Changes task priority.
     * 
     * @param task
     * @param priority
     */
    public void changeTaskPrioity(Task task, int priority) {

        task.setPriority(priority);
        taskDao.update(task);
    }

    /**
     * TODO name? finishing task
     * 
     * @param task
     * @param persons
     * @throws HumanTaskException
     */
    public void finishTask(Task task, Assignee persons) throws HumanTaskException {

        task.setStatus(Status.COMPLETED);
        taskDao.update(task);
    }

    /**
     * Suspends task.
     * 
     * @param task
     * @param person
     * @throws HumanTaskException
     */
    public void suspendTask(Task task, String personName) throws HumanTaskException {

        Person person = (Person) assigneeDao.getPerson(personName);

        if (!((task.getPotentialOwners().contains(person) && task.getStatus() == Status.READY) || task.getActualOwner().equals(person) || task.getBusinessAdministrators().equals(person))) {
            log.error("you don't have a permission to suspend the task");
            throw new HumanTaskException("you don't have a permission to suspend the task");
        }
        task.setStatus(Status.SUSPENDED);

        taskDao.update(task);
    }

    /**
     * Resumes task after suspension.
     * 
     * @param task
     * @throws HumanTaskException
     */
    public void resumeTask(Task task, String personName) throws HumanTaskException {

        Person person = (Person) assigneeDao.getPerson(personName);

        if (!((task.getPotentialOwners().contains(person) && task.getStatus() == Status.READY) || task.getActualOwner().equals(person) || task.getBusinessAdministrators().equals(person))) {
            log.error("you don't have a permission to resume the task");
            throw new HumanTaskException("you don't have a permission to resume the task");
        }

        task.resume();
        taskDao.update(task);
    }

    // tymczasowo dodany parametr typu Person
    public void completeTask(Task task, String data, String personName) throws HumanTaskException {

        Person person = (Person) assigneeDao.getPerson(personName);

        if (!task.getActualOwner().equals(person)) {

            log.error("this person doesn't have a permission to complete the task");
            throw new HumanTaskException("this person doesn't have a permission to complete the task");
        }

        if (data == null) {
            log.error("data cannot be empty");
            throw new IllegalArgumentException("data cannot be empty");
        }

        task.setStatus(Status.COMPLETED);

        taskDao.update(task);

    }

    /**
     * overloaded method setTaskOutput - gets only 3 parameters
     * 
     * @param task
     * @param dataSetXml
     * @param personName
     * @throws HumanTaskException
     */
    public void setTaskOutput(Task task, String dataSetXml, String personName) throws HumanTaskException {

        this.setTaskOutput(task, dataSetXml, null, personName);
    }

    /**
     * method that sets task output
     * 
     * @param task
     * @param dataSetXml
     * @param pName
     * @param personName
     * @throws HumanTaskException
     */
    public void setTaskOutput(Task task, String dataSetXml, String pName, String personName) throws HumanTaskException {

        Person person = assigneeDao.getPerson(personName);

        if (!person.equals(task.getActualOwner())) {
            log.error(person + " doesn't have the permission to set task output");
            throw new HumanTaskException(person + " doesn't have the permission to set task output");
        }

        task.setOutput(pName, dataSetXml);
        taskDao.update(task);
    }

    /**
     * overloaded getting task output
     * 
     * @param task
     * @param personName
     * @return
     * @throws HumanTaskException
     */
    public String getOutput(Task task, String personName) throws HumanTaskException {

        return this.getOutput(task, null, personName);
    }

    /**
     * getting task output
     * 
     * @param task
     * @param partName
     * @param personName
     * @return
     * @throws HumanTaskException
     */
    public String getOutput(Task task, String partName, String personName) throws HumanTaskException {

        Person person = assigneeDao.getPerson(personName);

        if (!person.equals(task.getActualOwner()) && !person.equals(task.getBusinessAdministrators())) {
            log.error(person + " doesn't have the permission to get task output");
            throw new HumanTaskException(person + " doesn't have the permission to get task output");
        }

        return task.getOutput(partName);
    }

    /**
     * Deletes output.
     * 
     * @param task
     */
    public void deleteOutput(Task task) {
        task.deleteOutput();
        taskDao.update(task);
    }

    /**
     * complete task with fault response
     * 
     * @param task
     * @param personName
     * @throws HumanTaskException
     * @throws IllegalArgumentFault
     */
    public void failTask(Task task, String faultName, String faultData, String personName) throws HumanTaskException {

        Person person = assigneeDao.getPerson(personName);

        if (!person.equals(task.getActualOwner())) {
            log.error(person + " cannot complete the task with fault response");
            throw new HumanTaskException(person + " cannot complete the task with fault response");
        }

        if (faultName == null || faultData == null) {
            log.error("none of faultName and faultXml cannot be null");
            throw new IllegalArgumentException("none of faultName and faultXml cannot be null");
        }

        if (!task.findFoult(faultName)) {
            log.error("Illegal operation fault - there is no such fault name");
            throw new RuntimeException("there is no such fault name");
        }

        // TODO ask fo
        // this.setFault(faultName, faultData);

        task.setStatus(Status.FAILED);
        taskDao.update(task);

    }

    /**
     * set fault name and fault Data
     * 
     * @param task
     * @param faultName
     * @param faultData
     * @param personName
     * @throws HumanTaskException
     * @throws IllegalOperationFault
     */
    public void setFault(Task task, String faultName, String faultData, String personName) throws HumanTaskException {

        Person person = assigneeDao.getPerson(personName);

        if (!person.equals(task.getActualOwner())) {
            log.error(person + " cannot set the fault");
            throw new HumanTaskException(person + " cannot set the fault");
        }

        if (!task.findFoult(faultName)) {
            log.error("Illegal operation fault - there is no such fault name");
            throw new RuntimeException("there is no such fault name");
        }

        task.setFault(faultName, faultData);
        taskDao.update(task);
    }

    /**
     * delete fault name and fault data
     * 
     * @param task
     * @param personName
     * @throws HumanTaskException
     */
    public void deleteFault(Task task, String personName) throws HumanTaskException {
        Person person = assigneeDao.getPerson(personName);

        if (!person.equals(task.getActualOwner())) {
            log.error(person + " cannot remove foult form task " + task);
            throw new HumanTaskException(person + " cannot remove foult form task " + task);
        }

        task.deleteFault();
        taskDao.update(task);

    }

    public String getFault(Task task, String personName) throws HumanTaskException {

        Person person = assigneeDao.getPerson(personName);
        if (person.equals(task.getActualOwner()) && person.equals(task.getBusinessAdministrators())) {
            log.error(person + " cannot get task''s foult");
            throw new HumanTaskException(person + " cannot get task''s foult");
        }

        // TODO it should return fault name && fault data
        String fault = task.getFault();

        // alternatywnie

        String faultName = task.getFaultName();
        String faultData = task.getFaultData();

        return faultData;

    }

    public void suspendUntilPeriod(Task task, long timePeriod) throws HumanTaskException {

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis() + timePeriod);
        suspendUntil(task, cal.getTime());
    }

    // TODO can be suspeneded?
    public void suspendUntil(Task task, Date pointOfTime) throws HumanTaskException {

        task.setSuspentionTime(pointOfTime);
        task.setStatus(Status.SUSPENDED);
        taskDao.update(task);
    }

    /**
     * Adds {@link Attachment} to the task.
     * @param task
     * @param attName
     * @param accessType
     * @param contentType
     * @param attachment
     * @param person
     * @throws HumanTaskException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void addAttachment(Task task, String attName, String accessType, String contentType, String attachment, Person person) throws HumanTaskException {

        if (!(person.equals(task.getActualOwner()) || person.equals(task.getBusinessAdministrators()))) {
            log.error(person + "cannot add attachemnt");
            throw new HumanTaskException(person + "cannot add attachemnt");
        }

        Attachment att = new Attachment();
        att.setName(attName);
        att.setAccessType(accessType);
        att.setContentType(contentType);

        Calendar cal = Calendar.getInstance();
        att.setAttachedAt(cal.getTime());
        att.setUserId(person.getId());
        att.setAttachment(attachment);
        task.addAttachment(att);

        taskDao.update(task);
    }

//    public List<Attachment> getAttachments(Task task, String attachmentName, Assignee assignee) {
//
//        List<Attachment> list = new ArrayList<Attachment>();
//
//        for (Attachment att : task.getAttachments()) {
//            if (att.getName().equals(attachmentName)) {
//                list.add(att);
//            }
//        }
//        return list;
//    }
//
    /**
     * adding potential owner
     * 
     * @param t
     * @param p
     * @return
     * @throws Exception
     */
    public void addPotentialOwner(Task task, List<Person> list) {
        for (Person person : list) {
            this.addPotentialOwner(task, person);
        }
    }

    // TODO move to Task
    private void addPotentialOwner(Task task, Assignee assignee) {

        List<Assignee> owners = new ArrayList<Assignee>();
        owners = task.getPotentialOwners();
        if (!owners.contains(assignee)) {
            owners.add(assignee);
        }
        task.setPotentialOwners(owners);

        taskDao.update(task);
    }

//    // TODO przenies do DAO i raczej powinno byc to zrobione w spring-sie
//    @Transactional(propagation = Propagation.REQUIRED)
//    public void refresh() {
//
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(System.currentTimeMillis());
//
//        List<Task> list = taskDao.getTasksToResume(Status.SUSPENDED, cal.getTime());
//
//        for (Task task : list) {
//            if (cal.getTime().compareTo(task.getSuspentionTime()) > 0) {
//                task.resume();
//                log.info("refreshing tasks status");
//                taskDao.update(task);
//            }
//        }
//    }

    public TaskDao getTaskDao() {
        return taskDao;
    }

    public void setTaskDao(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    public List<TaskDefinition> getTaskDefinitions() {
        return taskDefinitions;
    }

    public void setTaskDefinitions(List<TaskDefinition> taskDefinitions) {
        this.taskDefinitions = taskDefinitions;
    }

    public void setAssigneeDao(AssigneeDao assigneeDao) {
        this.assigneeDao = assigneeDao;
    }

    public AssigneeDao getAssigneeDao() {
        return assigneeDao;
    }

    public void setPeopleQuery(PeopleQuery peopleQuery) {
        this.peopleQuery = peopleQuery;
    }

    public PeopleQuery getPeopleQuery() {
        return peopleQuery;
    }
}
