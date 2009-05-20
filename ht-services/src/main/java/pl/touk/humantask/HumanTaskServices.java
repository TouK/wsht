/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask;

import java.util.List;

import pl.touk.humantask.exceptions.HTException;
import pl.touk.humantask.exceptions.HTIllegalAccessException;
import pl.touk.humantask.exceptions.HTIllegalArgumentException;
import pl.touk.humantask.exceptions.HTIllegalStateException;
import pl.touk.humantask.exceptions.HTRecipientNotAllowedException;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.model.Task.Status;
import pl.touk.humantask.model.Task.TaskTypes;

/**
 * Human task engine services interface.
 *
 * @author Kamil Eisenbart
 * @author Witek Wołejszo
 * @since 1.0.1
 */
public interface HumanTaskServices {

    /**
     * Creates {@link Task} instance based on a definition. The definitions are provided by {@link HumanInteractionsManager}. 
     * We assume that the task is activated upon creation provided that it has any potential owners. Upon creation the following sets of
     * assignees (people or unresolved group of people) are evaluated:
     * <ul>
     * <li>task initiators - {@link pl.touk.humantask.model.GenericHumanRole#TASK_INITIATOR}</li>
     * <li>task stakeholders - {@link pl.touk.humantask.model.GenericHumanRole#TASK_STAKEHOLDERS}</li>
     * <li>potential owners - {@link pl.touk.humantask.model.GenericHumanRole#POTENTIAL_OWNERS}</li>
     * <li>excluded owners - {@link pl.touk.humantask.model.GenericHumanRole#EXCLUDED_OWNERS}</li>
     * <li>business administrators - {@link pl.touk.humantask.model.GenericHumanRole#BUSINESS_ADMINISTRATORS}</li>
     * <li>notification recipients - {@link pl.touk.humantask.model.GenericHumanRole#NOTIFICATION_RECIPIENTS}</li>
     * </ul>
     * Those groups have roles in aspect of the task. The source of a group is a part of the definition - it can be a logical group, a set of people or a set of
     * groups, which can be evaluated basing on the requestXml contents. The status after the operation depends on the count of potential owners:<br/>
     * 0 - CREATED, it is now due to the administrator to add potential owners<br/>
     * 1 - RESERVED, since there's only one possibility;<br/>
     * 2 or more - READY - the potential owners are welcome to take the task.<br/>
     * Request data depends on the task definition, e.g. approving a claim requires a money amount, which may not make sense in case of another task. Request
     * data might be empty in some cases.<br/> If the task initiators are not empty and createdBy is not empty, it is checked whether task initiators contain
     * createdBy. If not, it is not allowed to create the task. Depending on the situation, createdBy may be empty. At the end, the new task is stored.
     *
     * @param taskName   The name of the task template from the definition file.
     * @param createdBy  The user creating task.
     * @param requestXml The xml request used to invoke business method. Content of the request can be accessed by Task.
     * @return created Task
     * @throws HTException In case of problems while creating task
     */
    Task createTask(String taskName, String createdBy, String requestXml) throws HTException;

    /**
     * Retrieves task's details. This operation is used to obtain the data required to display a tasks list, as well as the details for the individual tasks.
     *
     * @param personName        The person assignee name. If specified and no work queue has been specified then only personal tasks are returned, classified by genericHumanRole.
     * @param taskType          The Task type - one of {@link pl.touk.humantask.model.Task.TaskTypes#ALL}, {@link pl.touk.humantask.model.Task.TaskTypes#NOTIFICATIONS}, {@link pl.touk.humantask.model.Task.TaskTypes#TASKS}.
     * @param genericHumanRole  TODO wcr ?The classifier of names contained in the task?
     * @param workQueue         If the work queue is specified then only tasks having a work queue and generic human role are returned.
     * @param statuses          Task statuses. Tasks which status is one of those specified in List, if not specified or is empty list, a status wildcard is assumed.
     * @param whereClause       The JPA where clause added to the query. These fields may be used:
     * <ul>
     * <li>Task.*</li>
     * <li>PresenationParameter.*</li>
     * </ul>
     * @param orderByClause     The JPA order by clause added to the query. These fields may be used:
     * <ul>
     * <li>Task.*</li>
     * <li>PresenationParameter.*</li>
     * </ul>
     * @param createdOnClause   TODO WCR ?an JPA where clause performed on an xsd:date?
     * @param maxTasks          The maximum number of results returned in the List after ordering by activationTime.
     * @param offset            The number from which found tasks are returned. 
     * @return                  List of Tasks which meet the criteria.
     * @throws HTException In case of problems while getting tasks.
     * @since 1.0.1
     */
    List<Task> getMyTasks(String personName, TaskTypes taskType, GenericHumanRole genericHumanRole, String workQueue, List<Task.Status> statuses,
            String whereClause, String orderByClause, String createdOnClause, Integer maxTasks, Integer offset) throws HTException;

    /**
     * Claim responsibility for a task, i.e. set the unassigned task to status Reserved.
     *
     * @param  taskId       The task to claim.
     * @param  personName   The person that will become the new actual owner.
     *
     * @throws HTIllegalAccessException In case that given person is not authorized to perform operation
     * @throws HTIllegalArgumentException In case that given argument is incorrect
     * @throws HTIllegalStateException In case that current task state doesn't allow for the operation to perform
     * @since 1.0.7
     */
    void claimTask(Long taskId, String personName) throws HTIllegalAccessException, HTIllegalArgumentException, HTIllegalStateException;


    /**
     * Start the execution of the task, i.e. set the task to status InProgress.
     *
     * @param taskId        The task to start.
     * @param personName    The person who starts the task.
     *
     * </p>
     * Authorization: must be either {@link pl.touk.humantask.model.GenericHumanRole#ACTUAL_OWNER} or {@link pl.touk.humantask.model.GenericHumanRole#POTENTIAL_OWNERS} and {@link Status#READY}
     * </p>
     * @throws HTIllegalAccessException     In case that given person is not authorised to perform operation
     * @throws HTIllegalArgumentException   In case that given argument is incorrect
     * @throws HTIllegalStateException      In case that current task state doesn't allow for the operation to perform
     * @since 1.0.7
     */
    void startTask(Long taskId, String personName) throws HTIllegalAccessException, HTIllegalArgumentException, HTIllegalStateException;

    /**
     * Releases the task, i.e. set the task back to status Ready.
     * <p>
     * 4.7.2 Releasing a Human Task
     * The current actual owner of a human task may release a task to again make it 
     * available for all potential owners. A task can be released from active states that have
     * an actual owner (Reserved, InProgress), transitioning it into the Ready state.
     * </p>
     * Authorization: must be either {@link pl.touk.humantask.model.GenericHumanRole#ACTUAL_OWNER} or {@link pl.touk.humantask.model.GenericHumanRole#BUSINESS_ADMINISTRATORS}
     * </p>
     * @param task          The task to release.
     * @param personName    The person who is releasing the task.
     *
     * @throws HTIllegalAccessException In case that given person is not authorized to perform operation
     * @throws HTIllegalArgumentException In case that given argument is incorrect
     * @throws HTIllegalStateException In case that current task state doesn't allow for the operation to perform
     * @since 1.0.8
     */
    void releaseTask(Long taskId, final String personName) throws HTIllegalAccessException, HTIllegalArgumentException, HTIllegalStateException;

    /**
     * Gets {@link Task}.
     *
     * @param taskId    The task identifier.
     * @return          If the identifier has a correspoding task, the function returns this task object.
     * @throws HTIllegalArgumentException In case where the task of the specified taskId doesn't exist.
     * @since 1.0.1
     */
    Task getTaskInfo(Long taskId) throws HTIllegalArgumentException;

    /**
     * Delegates {@link Task}. 
     * 4.7.4
     * Task’s potential owners, actual owner or business administrator can delegate a task 
     * to another user, making that user the actual owner of the task, and also adding her 
     * to the list of potential owners in case she is not, yet.
     * </p>
     * {@link Status#READY} tasks can be delegated than they are RESERVED. (Ambiguous in spec. IN_PROGRESS on diagram.) </br>  
     * {@link Status#RESERVED} tasks can be delegated and they remain RESERVED. </br>
     * {@link Status#IN_PROGRESS} tasks can be delegated than they are RESERVED. </br>
     * </p>
     * Current implementation supports
     * delegating to {@link GenericHumanRole#POTENTIAL_OWNERS} and ignores task definition.
     * </p>
     * @param taskId        The task to delegate.
     * @param personName    The person delegating the task.
     * @param delegateeName The delegatee - current owner to be set.
     * @throws HTIllegalAccessException
     * @throws HTIllegalStateException
     * @throws HTIllegalArgumentException
     * @throws HTRecipientNotAllowedException
     * @since 1.0.8
     */
    void delegateTask(Long taskId, String personName, String delegateeName) throws HTIllegalArgumentException, HTIllegalAccessException, HTIllegalStateException, HTRecipientNotAllowedException;
}
