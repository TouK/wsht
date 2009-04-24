/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */
package pl.touk.humantask;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jws.WebService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import pl.touk.humantask.dao.AssigneeDao;
import pl.touk.humantask.dao.TaskDao;
import pl.touk.humantask.exceptions.HTIllegalAccessException;
import pl.touk.humantask.exceptions.HTIllegalArgumentException;
import pl.touk.humantask.exceptions.HTIllegalOperationException;
import pl.touk.humantask.exceptions.HTIllegalStateException;
import pl.touk.humantask.exceptions.HTException;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Attachment;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Person;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.model.Task.Status;
import pl.touk.humantask.model.Task.TaskTypes;
import pl.touk.humantask.spec.HumanInteractionsManagerInterface;
import pl.touk.humantask.spec.TaskDefinition;

/**
 * Human task engine services.
 *
 * @author Kamil Eisenbart
 * @author Witek Wołejszo
 * @author Mateusz Lipczyński
 */
@WebService(endpointInterface = "ToukHumanTaskService", serviceName = "TaskService", portName = "TaskPort", targetNamespace = "http://touk.pl/HumanTask")
public class Services implements HumanTaskServicesInterface {

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
     * Definitions of tasks available in WSHT.
     */
//    private List<TaskDefinition> taskDefinitions;

    /**
     * Fully implemented methods - visible in interface.
     */

    private HumanInteractionsManagerInterface taskManager;

    public void setTaskManager(HumanInteractionsManagerInterface taskManager) {
        this.taskManager = taskManager;
    }

    /**
     * Work in progress - visible in interface.
     */

    /**
     * Creates {@link Task} instance based on a definition. See detailed contract in {@link HumanTaskServicesInterface#createTask(String, String, String)}
     *
     * @param taskName
     *            name of the task template from the definition file
     * @param createdBy
     *            user creating task
     * @param requestXml
     *            xml request used to invoke business method; can contain task-specific attributes, like last name, amount, etc.
     *
     * @return created Task
     * @throws HTException Thrown in case of problems at task creation 
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Task createTask(String taskName, String createdBy, String requestXml) throws HTException {

        log.info("Creating task: " + taskName + " , createdBy: " + createdBy);

        TaskDefinition taskDefinition = this.taskManager.getTaskDefinition(taskName);

        Person createdByPerson = assigneeDao.getPerson(createdBy);
        if (createdByPerson == null) {
            createdByPerson = new Person(createdBy);
            //assigneeDao.create(createdByPerson);
        }

        Task newTask = new Task(taskDefinition, createdByPerson, requestXml);
        taskDao.create(newTask);
        return newTask;

    }

    /**
     *
     * @see pl.touk.humantask.HumanTaskServicesInterface#getMyTasks(java.lang.String, pl.touk.humantask.model.Task.TaskTypes, pl.touk.humantask.model.GenericHumanRole, java.lang.String, java.util.List, java.lang.String, java.lang.String, java.lang.Integer) 
     */
    public List<Task> getMyTasks(String personName, TaskTypes taskType, GenericHumanRole genericHumanRole, String workQueue, List<Task.Status> status,
            String whereClause, String createdOnClause, Integer maxTasks) throws HTException {

        if (null == personName && null == workQueue) {
            throw new pl.touk.humantask.exceptions.HTIllegalArgumentException("parameter not specified","workQueue");
        }

        Person person = null;

        if (null == workQueue) {
            person = assigneeDao.getPerson(personName);
        }

        if (null == person && null == workQueue) {
            throw new HTIllegalAccessException("Not a valid name, no such Assignee found: " + personName);
        }

        if (null == taskType) {
            throw new pl.touk.humantask.exceptions.HTIllegalArgumentException("parameter not specified","task type");
        }

        try {
            return taskDao.getTasks(person, taskType, genericHumanRole, workQueue, status, whereClause, createdOnClause, maxTasks);

        } catch (Exception x) {
            throw new HTIllegalOperationException(x.getMessage(),"getMyTasks",x);
        }
    }
   
    /**
     * Later.
     */

    /**
     * Returns task owned by specified person.
     *
     * @param personName Name of tasks owner
     * @return List of specified person tasks
     */
    public List<Task> getMyTasks(String personName) {
        Person person = assigneeDao.getPerson(personName);
        return taskDao.getTasks(person);
    }

    /**
     * @see pl.touk.humantask.HumanTaskServicesInterface#claimTask(pl.touk.humantask.model.Task, java.lang.String) 
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void claimTask(Long taskId, String assigneeName) throws HTIllegalAccessException, HTIllegalArgumentException, HTIllegalStateException {

        Person person = assigneeDao.getPerson(assigneeName);
        
        if (null == person) {
        	throw new HTIllegalAccessException("Not found", assigneeName);
        }

        Task task = locateTask(taskId);
        
        if (task.getId() != null && taskDao.fetch(task.getId()) == null) {
            //TODO: dead code, is this code ever even called
            throw new pl.touk.humantask.exceptions.HTIllegalArgumentException("Task not found");
        }

        if (task.getActualOwner() != null || !Arrays.asList(Task.Status.READY).contains(task.getStatus())) {
            throw new HTIllegalStateException("Task not claimable.", task.getStatus());
        }

        // check if the task can be claimed by person
        if (!task.getPotentialOwners().contains(person) || (task.getExcludedOwners() != null && task.getExcludedOwners().contains(person))) {
            throw new HTIllegalAccessException("Not a potential owner.", person.getName());
        }

        task.setActualOwner(person);
        task.reserve();

        taskDao.update(task);
    }

    /**
     *
     * @see pl.touk.humantask.HumanTaskServicesInterface#startTask(pl.touk.humantask.model.Task, java.lang.String)
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void startTask(Long taskId, String personName) throws HTIllegalAccessException, HTIllegalArgumentException, HTIllegalStateException {

        Person person = assigneeDao.getPerson(personName);

        if (null == person) 
            throw new HTIllegalAccessException("Person not found: ", personName);

        Task task = locateTask(taskId);
        
        if (!task.getPotentialOwners().contains(person)) {
            log.error("This person is not permited to start the task");
            throw new HTIllegalAccessException("This person is not permited to start the task",personName);
        }

        if (task.getActualOwner() != null && (!task.getActualOwner().equals(person))) {
            throw new HTIllegalAccessException("This task is already claimed by:",task.getActualOwner().toString());
        }


        if (task.getStatus() == Status.READY) {
            task.setActualOwner(person);
        }

        task.setStatus(Status.IN_PROGRESS);


        taskDao.update(task);
    }

//    /**
//     * Loads single task from persistent store. TODO implement
//     *
//     * @return
//     */
//    @Transactional(propagation = Propagation.REQUIRED)
//    public Task loadTask(Long taskId) {
//
//        Task task = taskDao.fetch(taskId);
//        // task.setTaskDefinition(findTaskDefinitionByKey(task.getTaskDefinitionKey()));
//
//        // TODO throw an exception if no definition found
//
//        return task;
//    }
//
//    /**
//     * Delegates task to other person.
//     *
//     * @param task
//     * @param assigneeName
//     * @throws HumanTaskException
//     */
//    public Task delegateTask(Long taskId, String assigneeName) throws HumanTaskException {
//
//        Person person = assigneeDao.getPerson(assigneeName);
//
//        task.setStatus(Status.RESERVED);
//        this.addPotentialOwner(task, person);
//        task.setActualOwner(person);
//
//        taskDao.update(task);
//
//        return task;
//    }
//
//    @Transactional(propagation = Propagation.REQUIRED)
//    // TODO How to distinguish groups and people? now works on people only
//    public Task forwardTask(Long taskId, String assigneeName) throws HumanTaskException {
//
//        Person owner = (Person) task.getActualOwner();
//
//        Assignee assignee = assigneeDao.getPerson(assigneeName);
//
//        // List<Person> list = this.;
//        // log.info(g.getPeople().size());
//
//        if (assignee == null) {
//            log.error("Task without assignee cannot be forwarded.");
//            throw new HumanTaskException("Task without assignee cannot be forwarded");
//        }
//
//        // TODO check who is forwarding the task
//        /*
//         * if ((t.getStatus()==Status.IN_PROGRESS || t.getStatus()==Status.RESERVED)&& owner==null){
//         * log.error("this person or a group cannot forward this task"); throw new HumanTaskException("this person or a group cannot forward this task" ); }
//         */
//
//        if (task.getStatus() == Status.IN_PROGRESS || task.getStatus() == Status.RESERVED) {
//            this.releaseTask(task, owner.getName());
//        }
//
//        if (!(task.getStatus() == Status.READY)) {
//
//            log.error("Task with other stus then Ready cannot be forwarded");
//            throw new HumanTaskException("Task with other stus then Ready cannot be forwarded");
//        }
//
//        // erasing actual owner from potential owners or all potential owners
//        // group
//        if (owner != null) {
//
//            this.removeAssigneeFromPotentialOwners(task, owner);
//        } else {
//
//            task.setPotentialOwners(null);
//        }
//
//        //TODO always Person
//        if (assignee instanceof Group) {
//
//            Group g = (Group) assignee;
//            List<Person> people = g.getPeople();
//            // TODO adding to potentialownersgroup new owners form the group
//            this.addPotentialOwner(task, people);
//
//        } else {
//
//            this.addPotentialOwner(task, assignee);
//
//        }
//
//        // adding new potential owners
//        // t = t.addPotentialOwner(assignee);
//
//        taskDao.update(task);
//        return task;
//    }

    /**
     * Removes person from potential owners of specified task.
     *
     * @param task Task to process
     * @param person Person to remove from task owners
     */
    public void removeAssigneeFromPotentialOwners(Long taskId, Assignee person) throws HTIllegalArgumentException {
        int i = 0;
        boolean removed = false;
        Task task = locateTask(taskId);
        List<Assignee> list = task.getPotentialOwners();
        Assignee assignee;
        while (!removed) {
            assignee = list.get(i);

            if (((Person) assignee).equals(person)) {
                list.remove(i);
                removed = true;
            }
            i++;
        }
    }

    /**
     * @see pl.touk.humantask.HumanTaskServicesInterface#releaseTask(pl.touk.humantask.model.Task, java.lang.String)
     */
    public void releaseTask(Long taskId, final String personName) throws HTIllegalArgumentException,HTIllegalAccessException, HTIllegalStateException {

        final Person person = assigneeDao.getPerson(personName);

        if (person == null) {
            throw new HTIllegalAccessException("Person not found: ", personName);
        }
        final Task task = locateTask(taskId);

        if (task.getActualOwner() == null) {
            throw new HTIllegalAccessException("Task without actual owner cannot be released");
        }

        if (!task.getActualOwner().equals(person) && !task.getBusinessAdministrators().contains(person)) {
            throw new HTIllegalAccessException("Calling person is neither the task's actual owner not business administrator");
        }

        task.releaseActualOwner();
        task.setStatus(Status.READY);

        taskDao.update(task);
    }

    /**
     * Stops task in progres.
     *
     * @param task Task to process
     * @return Updated task
     * @throws HTException Thrown if task is not in progress or in case of problems while updating task
     */
    public Task stopTaskInProgress(Long taskId) throws HTException {

        Task task = locateTask(taskId);
        
        if (!(task.getStatus() == Status.IN_PROGRESS)) {
            log.error("Task has to be In_Progress");
            throw new HTException("Cannot stop task that is not in progress");
        }

        task.setStatus(Status.RESERVED);
        taskDao.update(task);

        return task;

    }

//    /**
//     * Returns task definition object matching given key.
//     *
//     * @param key
//     * @return task definition
//     */
//    private TaskDefinition findTaskDefinitionByKey(String key) throws HumanTaskException {
//
//        return taskManager.getTaskDefinitionByKey(key);
////
////        for (TaskDefinition td : taskDefinitions) {
////            if (key.equals(td.getKey())) {
////                return td;
////            }
////        }
////
////        return null;
//    }

    /**
     * Changes task priority.
     *
     * @param task Task to process
     * @param priority Priority to set
     */
    public void changeTaskPrioity(Long taskId, int priority) throws HTIllegalArgumentException{

        Task task = locateTask(taskId);
        
        task.setPriority(priority);
        taskDao.update(task);
    }

    /**
     * Sets task status to COMPLETED.
     * TODO implement assignee check ?
     * 
     * @param task Task to process
     * @param person Person processing task
     * @throws HTException Thrown in case of problems while updating task
     */
    public void finishTask(Long taskId, Assignee person) throws HTException {

        final Task task = locateTask(taskId);
        task.setStatus(Status.COMPLETED);
        taskDao.update(task);
    }

    /**
     * Suspends task.
     * 
     * @param task Task to process
     * @param personName Name of person processing task
     * @throws HTException Thrown if specified person doesn't have permission to process task or in case of problems while updating task
     */
    public void suspendTask(Long taskId, String personName) throws HTException {

        final Person person = (Person) assigneeDao.getPerson(personName);
        final Task task = locateTask(taskId);
        if (!((task.getPotentialOwners().contains(person) && task.getStatus() == Status.READY)
                || task.getActualOwner().equals(person) || task.getBusinessAdministrators().contains(person))) {
            log.error("you don't have a permission to suspend the task");
            throw new HTException("you don't have a permission to suspend the task");
        }
        task.setStatus(Status.SUSPENDED);

        taskDao.update(task);
    }

    /**
     * Resumes task after suspension.
     * 
     * @param task Task to process
     * @param personName Name of person processing task
     * @throws HTException Thrown if specified person doesn't have permission to process task or in case of problems while updating task
     */
    public void resumeTask(Long taskId, String personName) throws HTException {

        final Person person = (Person) assigneeDao.getPerson(personName);
        final Task task = locateTask(taskId);
        
        if (!((task.getPotentialOwners().contains(person) && task.getStatus() == Status.READY)
                || task.getActualOwner().equals(person) || task.getBusinessAdministrators().contains(person))) {
            log.error("you don't have a permission to resume the task");
            throw new HTException("you don't have a permission to resume the task");
        }

        task.resume();
        taskDao.update(task);
    }

    /**
     * Completes task.
     *
     * @param task Task to process
     * @param data ?
     * @param personName Name of person processing task
     * @throws HTException Thrown if specified person doesn't have permission to process task or in case of problems while updating task
     */
    public void completeTask(Long taskId, String data, String personName) throws HTException {

        final Person person = (Person) assigneeDao.getPerson(personName);
        final Task task = locateTask(taskId);
        if (!task.getActualOwner().equals(person)) {

            log.error("this person doesn't have a permission to complete the task");
            throw new HTException("this person doesn't have a permission to complete the task");
        }

        if (data == null) {
            log.error("data cannot be empty");
            throw new IllegalArgumentException("data cannot be empty");
        }

        task.setStatus(Status.COMPLETED);

        taskDao.update(task);

    }

//    /**
//     * overloaded method setTaskOutput - gets only 3 parameters
//     *
//     * @param task
//     * @param dataSetXml
//     * @param personName
//     * @throws HumanTaskException
//     */
//    public void setTaskOutput(Long taskId, String dataSetXml, String personName) throws HumanTaskException {
//
//        this.setTaskOutput(task, dataSetXml, null, personName);
//    }

//    /**
//     * method that sets task output
//     *
//     * @param task
//     * @param dataSetXml
//     * @param pName
//     * @param personName
//     * @throws HumanTaskException
//     */
//    public void setTaskOutput(Long taskId, String dataSetXml, String pName, String personName) throws HumanTaskException {
//
//        Person person = assigneeDao.getPerson(personName);
//
//        if (!person.equals(task.getActualOwner())) {
//            log.error(person + " doesn't have the permission to set task output");
//            throw new HumanTaskException(person + " doesn't have the permission to set task output");
//        }
//
//        task.setOutput(pName, dataSetXml);
//        taskDao.update(task);
//    }

//    /**
//     * overloaded getting task output
//     *
//     * @param task
//     * @param personName
//     * @return
//     * @throws HumanTaskException
//     */
//    public String getOutput(Long taskId, String personName) throws HumanTaskException {
//
//        return this.getOutput(task, null, personName);
//    }

//    /**
//     * getting task output
//     *
//     * @param task
//     * @param partName
//     * @param personName
//     * @return
//     * @throws HumanTaskException
//     */
//    public String getOutput(Long taskId, String partName, String personName) throws HumanTaskException {
//
//        Person person = assigneeDao.getPerson(personName);
//
//        if (!person.equals(task.getActualOwner()) && !person.equals(task.getBusinessAdministrators())) {
//            log.error(person + " doesn't have the permission to get task output");
//            throw new HumanTaskException(person + " doesn't have the permission to get task output");
//        }
//
//        return task.getOutput(partName);
//    }

//    /**
//     * Deletes output.
//     *
//     * @param task
//     */
//    public void deleteOutput(Long taskId) {
//        task.deleteOutput();
//        taskDao.update(task);
//    }

//    /**
//     * complete task with fault response
//     *
//     * @param task
//     * @param personName
//     * @throws HumanTaskException
//     * @throws IllegalArgumentFault
//     */
//    public void failTask(Long taskId, String faultName, String faultData, String personName) throws HumanTaskException {
//
//        Person person = assigneeDao.getPerson(personName);
//
//        if (!person.equals(task.getActualOwner())) {
//            log.error(person + " cannot complete the task with fault response");
//            throw new HumanTaskException(person + " cannot complete the task with fault response");
//        }
//
//        if (faultName == null || faultData == null) {
//            log.error("none of faultName and faultXml cannot be null");
//            throw new IllegalArgumentException("none of faultName and faultXml cannot be null");
//        }
//
//        if (!task.findFault(faultName)) {
//            log.error("Illegal operation fault - there is no such fault name");
//            throw new RuntimeException("there is no such fault name");
//        }
//
//        // TODO ask fo
//        // this.setFault(faultName, faultData);
//
//        task.setStatus(Status.FAILED);
//        taskDao.update(task);
//
//    }

//    /**
//     * set fault name and fault Data
//     *
//     * @param task
//     * @param faultName
//     * @param faultData
//     * @param personName
//     * @throws HumanTaskException
//     * @throws IllegalOperationFault
//     */
//    public void setFault(Long taskId, String faultName, String faultData, String personName) throws HumanTaskException {
//
//        Person person = assigneeDao.getPerson(personName);
//
//        if (!person.equals(task.getActualOwner())) {
//            log.error(person + " cannot set the fault");
//            throw new HumanTaskException(person + " cannot set the fault");
//        }
//
//        if (!task.findFault(faultName)) {
//            log.error("Illegal operation fault - there is no such fault name");
//            throw new RuntimeException("there is no such fault name");
//        }
//
//        task.setFault(faultName, faultData);
//        taskDao.update(task);
//    }

//    /**
//     * delete fault name and fault data
//     *
//     * @param task
//     * @param personName
//     * @throws HumanTaskException
//     */
//    public void deleteFault(Long taskId, String personName) throws HumanTaskException {
//        Person person = assigneeDao.getPerson(personName);
//
//        if (!person.equals(task.getActualOwner())) {
//            log.error(person + " cannot remove foult form task " + task);
//            throw new HumanTaskException(person + " cannot remove foult form task " + task);
//        }
//
//        task.deleteFault();
//        taskDao.update(task);
//
//    }

//    public String getFault(Long taskId, String personName) throws HumanTaskException {
//
//        Person person = assigneeDao.getPerson(personName);
//        if (person.equals(task.getActualOwner()) && person.equals(task.getBusinessAdministrators())) {
//            log.error(person + " cannot get task''s foult");
//            throw new HumanTaskException(person + " cannot get task''s foult");
//        }
//
//        // TODO it should return fault name && fault data
//        String fault = task.getFault();
//
//        // alternatywnie
//
//        String faultName = task.getFaultName();
//        String faultData = task.getFaultData();
//
//        return faultData;
//
//    }

    /**
     * Suspends task for given period of time.
     * 
     * @param task Task to process
     * @param timePeriod Time of suspension in milliseconds
     * @throws HTException Thrown in case of problems while processing task 
     */
    public void suspendUntilPeriod(Long taskId, long timePeriod) throws HTException {

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis() + timePeriod);
        
        suspendUntil(taskId, cal.getTime());
    }

    /**
     * Suspends task until given point of time.
     *
     * @param task Task to be processed
     * @param pointOfTime Point of time until which task is to be suspended
     * @throws HTException Thrown in case of problems while processing task 
     */
    // TODO can be suspeneded?
    public void suspendUntil(Long taskId, Date pointOfTime) throws HTException {

        final Task task = locateTask(taskId);
        task.setSuspentionTime(pointOfTime);
        task.setStatus(Status.SUSPENDED);
        taskDao.update(task);
    }

    /**
     * Adds {@link Attachment} to the task.
     *
     * @param task Task to process
     * @param attName Attachment name
     * @param accessType Access type of attachment
     * @param contentType Content type of attachment
     * @param attachment Content of attachment
     * @param person Person processing task
     * @throws HTException  Thrown if specified person doesn't have permission to process task or in case of problems while updating task
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void addAttachment(Long taskId, String attName, String accessType, String contentType, String attachment, Person person) throws HTException {

        final Task task = locateTask(taskId);
        if (!(person.equals(task.getActualOwner()) || task.getBusinessAdministrators().contains(person))) {
            log.error(person + "cannot add attachemnt");
            throw new HTException(person + "cannot add attachemnt");
        }

        final Attachment att = new Attachment();
        att.setName(attName);
        att.setAccessType(accessType);
        att.setContentType(contentType);

        final Calendar cal = Calendar.getInstance();
        att.setAttachedAt(cal.getTime());
        att.setUserId(person.getId());
        att.setAttachment(attachment);
        task.addAttachment(att);

        taskDao.update(task);
    }

    // public List<Attachment> getAttachments(Long taskId, String attachmentName, Assignee assignee) {
    //
    // List<Attachment> list = new ArrayList<Attachment>();
    //
    // for (Attachment att : task.getAttachments()) {
    // if (att.getName().equals(attachmentName)) {
    // list.add(att);
    // }
    // }
    // return list;
    // }
    //
    
    /**
     * Adds potential owners to task.
     *
     * @param task Task to add owners to
     * @param list List of persons to add to potential task owners
     */
    public void addPotentialOwner(Long taskId, List<Person> list) throws HTIllegalArgumentException{
        //TODO: not very efficient perhaps overload method to take a task
        for (Person person : list) {
            this.addPotentialOwner(taskId, person);
        }
    }

    // TODO move to Task
    /**
     * Adds potential owner to task.
     * 
     * @param task Task to add owner to
     * @param assignee Person to add as potential owner
     */
    private void addPotentialOwner(Long taskId, Assignee assignee) throws HTIllegalArgumentException{

        final Task task = locateTask(taskId);
        List<Assignee> owners = task.getPotentialOwners();
        if (!owners.contains(assignee)) {
            owners.add(assignee);
        }
        task.setPotentialOwners(owners);

        taskDao.update(task);
    }

    // // TODO przenies do DAO i raczej powinno byc to zrobione w spring-sie
    // @Transactional(propagation = Propagation.REQUIRED)
    // public void refresh() {
    //
    // Calendar cal = Calendar.getInstance();
    // cal.setTimeInMillis(System.currentTimeMillis());
    //
    // List<Task> list = taskDao.getTasksToResume(Status.SUSPENDED, cal.getTime());
    //
    // for (Long taskId : list) {
    // if (cal.getTime().compareTo(task.getSuspentionTime()) > 0) {
    // task.resume();
    // log.info("refreshing tasks status");
    // taskDao.update(task);
    // }
    // }
    // }

    /**
     * @return TaskDao object
     */
    protected TaskDao getTaskDao() {
        return taskDao;
    }

    /**
     * Sets taskDao to given object.
     * @param taskDao TaskDao to set
     */
    public void setTaskDao(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

//    public List<TaskDefinition> getTaskDefinitions() {
//        return taskDefinitions;
//    }
//
//    public void setTaskDefinitions(List<TaskDefinition> taskDefinitions) {
//        this.taskDefinitions = taskDefinitions;
//    }

    /**
     * Sets assigneeDao to given object.
     * @param assigneeDao AssigneeDao to set
     */
    public void setAssigneeDao(AssigneeDao assigneeDao) {
        this.assigneeDao = assigneeDao;
    }

    /**
     * @return AssigneeDao object
     */
    public AssigneeDao getAssigneeDao() {
        return assigneeDao;
    }

    /**
    * @see pl.touk.humantask.HumanTaskServicesInterface#getTaskInfo(java.lang.Long) 
     */
    public Task getTaskInfo(Long taskId) throws HTIllegalArgumentException {
        if (taskDao.exists(taskId)) {
            return taskDao.fetch(taskId);
        } else {
            throw new HTIllegalArgumentException("Task not found");
        }
    }

    //TODO: move to task
    private Task locateTask(Long identifier) throws HTIllegalArgumentException {

        Task task = taskDao.fetch(identifier);

        if (null == task)
            throw new HTIllegalArgumentException("Cannot find Task",Long.toString(identifier));

        return task;
    }
}