/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask;

import java.util.List;
import java.util.Map;

import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Message;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.spec.TaskDefinition;

/**
 * A People Query evaluation interface. People queries are evaluated during the creation of a human task or a notification. If a people query fails then the
 * human task or notification is created anyway.
 * 
 * TODO clarify below statement People queries return either one person, a set of people, or the name of one or many groups of people.
 * 
 * @author Kamil Eisenbart
 * @author Witek Wołejszo
 */
public interface PeopleQuery {
    /**
     * Evaluates assignees in logical people group.
     * @param logicalPeopleGroupName the logical people group name
     * @param input the input message that created the task
     */
    List<Assignee> evaluate(String logicalPeopleGroupName, Map<String, Message> input);

}
