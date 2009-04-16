/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask;

import java.util.List;

import pl.touk.humantask.exceptions.HTIllegalArgumentException;
import pl.touk.humantask.exceptions.HTIllegalStateException;
import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.exceptions.RecipientNotAllowedException;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.model.Task.TaskTypes;

/**
 * Human task engine services interface.
 * 
 * @author Kamil Eisenbart
 * @author Witek Wołejszo
 */
public interface HumanTaskServicesInterface {
    
    /**
     * Creates {@link Task} instance basing on a definition. The definitions are provided by the services. They can come from a file, e.g. htd1.xml, a database
     * or any other source. We assume that the task is activated upon creation provided that it has any potential owners. Upon creation the following sets of
     * persons are evaluated:
     * <ul>
     * <li>task initiators - @see #taskInitiators</li>
     * <li>task stakeholders - @see #taskStakeholders</li>
     * <li>potential owners - @see #potentialOwners</li>
     * <li>excluded owners - @see #excludedOwners</li>
     * <li>business administrators - @see #businessAdministrators</li>
     * <li>notification recipients - @see #notificationRecipients</li>
     * </ul>
     * Those groups have roles in aspect of the task. The source of a group is a part of the definition - it can be a logical group, a set of people or a set of
     * groups, which can be evaluated basing on the requestXml contents. The status after the operation depends on the count of potential owners:<br/>
     * 0 - CREATED, it is now due to the administrator to add potential owners<br/>
     * 1 - RESERVED, since there's only one possibility;<br/>
     * 2 or more - READY - the potential owners are welcome to take the task.<br/>
     * Request data depends on the task definition, e.g. approving a claim requires a money amount, which may not make sense in case of another task. Request
     * data might be empty in some cases.</br> If the task initiators are not empty and createdBy is not empty, it is checked whether task initiators contain
     * createdBy. If not, it is not allowed to create the task. Depending on the situation, createdBy may be empty. At the end, the new task is stored.
     * 
     * @param taskName
     *            name of the task template from the definition file
     * @param createdBy
     *            user creating task
     * @param requestXml
     *            xml request used to invoke business method; can contain task-specific attributes, like last name, amount, etc.
     * @return created Task
     * @throws HumanTaskException
     */
    public Task createTask(String taskName, String createdBy, String requestXml) throws HumanTaskException;
    
    /**
     * Retrieve the task details. This operation is used to obtain the data required to display a task list, as well as the details for the individual tasks.
     * 
     * @param personName
     *            If specified and no work queue has been specified then only personal tasks are returned, classified by genericHumanRole.
     * @param taskType
     *            one of ALL, NOTIFICATIONS, TASKS.
     * @param genericHumanRole
     *            A classifier of names contained in the task.
     * @param workQueue
     *            If the work queue is specified then only tasks having a work queue and generic human role are returned.
     * @param status
     *            selects the tasks whose status is one of those specified in List, if not specified or is empty list, a status wildcard is assumed.
     * @param whereClause
     *            - an [Hibernate] SQL Expression added to the criteria These additional fields may be used
     *            (ID,TaskType,Name,Status,Priority,CreatedOn,ActivationTime,ExpirationTime
     *            ,StartByExists,CompleteByExists,RenderMethExists,Escalated,PrimarySearchBy);
     * @param createdOnClause
     *            - an [Hibernate] SQL Expression performed on an xsd:date.
     * @param maxTasks
     *            - the maximum number of results returned in the List after ordering by activationTime.
     * @return List of Tasks which meet the criteria.
     */
    public List<Task> getMyTasks(String personName, TaskTypes taskType, GenericHumanRole genericHumanRole, String workQueue, List<Task.Status> status,
            String whereClause, String createdOnClause, Integer maxTasks) throws HumanTaskException;
    
    /**
     * Claim responsibility for a task, i.e. set the task to status Reserved
     *
     * @param  task
     *          The task to claim
     * @param  personName
     *          The person will become the new actual owner.
     *
     * @throws HTIllegalStateException when the state is not possible for the task
     * @throws RecipientNotAllowedException when the personName is not in the list
     *         of potential owners
     */
    public Task claimTask(Task task,String personName) throws RecipientNotAllowedException, HTIllegalArgumentException, HTIllegalStateException;
}
