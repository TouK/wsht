/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.dao;

import java.util.Set;

import org.springframework.stereotype.Repository;

import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Group;
import pl.touk.humantask.model.Person;

/**
 * DAO operations specific to {@link Assignee}.
 * 
 * @author Witek Wołejszo
 */
@Repository
public interface AssigneeDao extends BasicDao<Assignee, Long> {

    /**
     * Returns {@link Person} by name.
     * 
     * @param name the name of a person.
     * @return the {@link Person} with specified name or null if no {@link Person} can be found
     */
    Person getPerson(String name);
    
    /**
     * Returns {@link Group} by name.
     * 
     * @param name the name of a group.
     * @return the {@link Group} with specified name or null if no {@link Group} can be found
     */
    Group getGroup(String name);
    
    /**
     * Persists assignees.
     * @param assignees The set of transient or not transient {@link Assignee}s.
     * @return The set of persisted assignees.
     */
    Set<Assignee> saveNotExistingAssignees(Set<Assignee> assignees); 

}
