/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.dao;

import org.springframework.stereotype.Repository;

import pl.touk.humantask.model.Assignee;
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

}
