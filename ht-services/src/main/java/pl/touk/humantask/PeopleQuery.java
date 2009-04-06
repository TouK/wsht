/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask;

import java.util.List;

import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.spec.TaskDefinition;

/**
 * People queries are evaluated during the creation of a human task or a
 * notification. If a people query fails then the human task or notification is
 * created anyway.
 * 
 * People queries return either one person, a set of people, or the name of one or many groups of people.
 * 
 * @author Witek Wołejszo
 */
public interface PeopleQuery {

    /**
     * Returns list of potential {@link Assignee}s that are members of logical people group.
     * @param logicalPeopleGroup
     * @param task
     * @return
     */
    List<Assignee> evaluate(TaskDefinition.LogicalPeopleGroup logicalPeopleGroup, Task task);

}
