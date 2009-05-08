/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import pl.touk.humantask.HumanTaskServices;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Person;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.model.Task.TaskTypes;

/**
 * DAO operations specific to {@link Task}.
 * 
 * @author Witek Wołejszo
 * @author Warren Crossing
 */
@Repository
public interface TaskDao extends BasicDao<Task, Long> {

    /**
     * Returns all {@link Task}s currenty owned by specifed {@link Person}.
     *
     * @param   owner the owner's name
     * @return  list of {@link Task}s
     */
    List<Task> getTasks(Person owner);

    /**
     * Returns tasks. See {@link HumanTaskServices#getMyTasks(String, TaskTypes, GenericHumanRole, String, List, String, String, Integer)}
     * for method contract.
     *
     * @param owner
     * @param taskType
     * @param genericHumanRole
     * @param workQueue
     * @param status
     * @param whereClause
     * @param createdOnClause
     * @param maxTasks
     * @return
     */
    List<Task> getTasks(Assignee owner, TaskTypes taskType, GenericHumanRole genericHumanRole, String workQueue, List<Task.Status> status, String whereClause,
            String createdOnClause, Integer maxTasks);

    /**
     * Checks if given entity exists.
     * @param primaryKey Primary key of the entity
     * @return true if entity exists false otherwise
     */
    boolean exists(Long primaryKey);
}
