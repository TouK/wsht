/*
 * Copyright (c) (2005 - )2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.model.Group;

import java.util.List;

/**
 * {@link TaskDefinitionManagerImpl} class unit tests.
 * 
 * @author Jakub Kurlenda
 * @author <a href="mailto:jkr@touk.pl">Jakub Kurlenda</a>
 */
public class TaskDefinitionManagerImplTest {

    private TaskDefinitionManagerInterface taskManager;


    @Before
    public void setUp() throws Exception {
        Resource resource = new ClassPathResource("htd1.xml");
        this.taskManager = new TaskDefinitionManagerImpl(resource);
    }

    @Test
    public void testGetTaskDefinitionByName() throws HumanTaskException {
        TaskDefinition taskDefinition = taskManager.getTaskDefinitionByName("ApproveClaim");

        assertNotNull(taskDefinition);
    }

    @Test(expected = HumanTaskException.class)
    public void testTaskDefinitionNotFound() throws HumanTaskException {
        taskManager.getTaskDefinitionByName("JKR");
    }

    @Test
    public void testGetTaskDefinitions() {
        List<TaskDefinition> taskDefinitions = taskManager.getTaskDefinitions();
        assertNotNull(taskDefinitions);
        assertEquals(2, taskDefinitions.size());
    }

    @Test
    public void testGetLogicalPeopleGroups() {
        List<Group> groups = taskManager.getLogicalPeopleGroups();
        assertNotNull(groups);
        assertEquals(6, groups.size());
    }

    @Test
    public void testGetLogicalPeopleGroupByName() throws HumanTaskException {
        Group group = taskManager.getLogicalPeopleGroupByName("clerksManager");
        assertNotNull(group);
    }

    @Test(expected = HumanTaskException.class)
    public void testLogicalPeopleGroupNotFound() throws HumanTaskException {
        taskManager.getLogicalPeopleGroupByName("JKR!@#");
    }
}