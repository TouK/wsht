/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.model.TaskTypes;

/**
 * 
 *
 * @author Witek Wołejszo
 */
@Repository
public interface TaskDao extends BasicDao<Task, Long> {

    /**
     * Returns tasks currently owned by specified person.
     * @param owner
     * @return
     */
    List<Task> getTasks(Assignee owner);
    
    List<Task> getTasks(Assignee owner, TaskTypes taskType, GenericHumanRole genericHumanRole, String workQueue, List<Task.Status> status, String whereClause, String createdOnClause, Integer maxTasks);
    
}
