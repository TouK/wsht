/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.spec;

import java.util.List;
import java.util.Map;

import pl.touk.humantask.model.Assignee;

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
     * @param parameters the map of parameters
     */
    List<Assignee> evaluate(String logicalPeopleGroupName, Map<String, Object> parameters);

}
