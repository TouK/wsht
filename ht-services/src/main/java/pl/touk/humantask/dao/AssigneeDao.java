/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Person;
import pl.touk.humantask.model.Task;

/**
 * 
 *
 * @author Witek Wołejszo
 */
@Repository
public interface AssigneeDao extends BasicDao<Assignee, Long> {
    
    Person getPerson(String name);
    
}
