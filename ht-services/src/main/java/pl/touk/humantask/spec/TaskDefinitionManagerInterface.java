/*
 * Copyright (c) (2005 - )2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.spec;

import pl.touk.humantask.model.Group;
import pl.touk.humantask.HumanTaskException;

import java.util.List;

/*
 *
 * This interface introduces methods for task retrieving.
 *
 * @author Jakub Kurlenda
 * @author <a href="mailto:jkr@touk.pl">Jakub Kurlenda</a>
 */

public interface TaskDefinitionManagerInterface {
    /*
     * Method for retrieving task definition, according to its name
     *
     * @param taskName
     * @return TaskDefinition instance
     *
     * @throws HumanTaskException in case when no such task definition was found
     */
    public TaskDefinition getTaskDefinitionByName(String taskName) throws HumanTaskException;

    /*
    * Method for retrieving all task definitions defined inside application context
    *
    * @return java.util.List implementation, containing TaskDefinition instances
    */

    public List<TaskDefinition> getTaskDefinitions();

    /*
    * Method for retrieving people group, according to its name
    *
    * @param taskName
    * @return TaskDefinition instance
    *
    * @throws HumanTaskException in case when no such people group was found
    */

    public Group getLogicalPeopleGroupByName(String groupName) throws HumanTaskException;

    /*
    * Method for retrieving all people groups defined inside application context
    *
    * @return java.util.List implementation, containing Group instances
    */
    
    public List<Group> getLogicalPeopleGroups();
}