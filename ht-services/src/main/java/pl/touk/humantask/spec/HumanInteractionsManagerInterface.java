/*
 * Copyright (c) (2005 - )2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.spec;

import java.util.List;

import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.model.Group;

/**
 * This interface introduces methods for task retrieving. We assume that task name is unique
 * in whole configuration.
 *
 * @author <a href="mailto:jkr@touk.pl">Jakub Kurlenda</a>
 */
public interface HumanInteractionsManagerInterface {
    
    /*
     * Retrieves task definition by name.
     *
     * @param taskName
     * @return TaskDefinition instance
     * @throws HumanTaskException in case when no such task definition was found
     */
    TaskDefinition getTaskDefinition(String taskName) throws HumanTaskException;
    
    //TaskDefinition getTaskDefinitionByName(String taskName) throws HumanTaskException;

    /*
    * Method for retrieving all task definitions defined inside application context
    *
    * @return java.util.List implementation, containing TaskDefinition instances
    */
    //List<TaskDefinition> getTaskDefinitions();

    /*
    * Method for retrieving people group, according to its name
    *
    * @param taskName
    * @return TaskDefinition instance
    *
    * @throws HumanTaskException in case when no such people group was found
    */
    //Group getLogicalPeopleGroupByName(String groupName) throws HumanTaskException;

    /*
    * Method for retrieving all people groups defined inside application context
    *
    * @return java.util.List implementation, containing Group instances
    */
//    public List<Group> getLogicalPeopleGroups();
    //List<HumanInteractions> getHumanInteractions();
    
}