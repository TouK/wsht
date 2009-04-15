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
import java.util.ArrayList;

/**
 * {@link HumanInteractionsManagerImpl} class unit tests.
 * 
 * @author Jakub Kurlenda
 * @author <a href="mailto:jkr@touk.pl">Jakub Kurlenda</a>
 */
public class TaskDefinitionManagerImplUnitTest {

    private HumanInteractionsManagerInterface taskManager;


    @Before
    public void setUp() throws Exception {
        List<Resource> resources = new ArrayList<Resource>();
        Resource resource = new ClassPathResource("htd1.xml");
        resources.add(resource);
        resource = new ClassPathResource("testHtd1.xml");
        resources.add(resource);
        this.taskManager = new HumanInteractionsManagerImpl(resources);
    }

    @Test
    public void testGetTaskDefinitionByName() throws HumanTaskException {
        TaskDefinition taskDefinition = taskManager.getTaskDefinition("ApproveClaim");

        assertNotNull(taskDefinition);
    }

    @Test(expected = HumanTaskException.class)
    public void testTaskDefinitionNotFound() throws HumanTaskException {
        taskManager.getTaskDefinition("JKR");
    }

//    @Test
//    public void testGetTaskDefinitions() {
//        List<TaskDefinition> taskDefinitions = taskManager.getTaskDefinitions();
//        assertNotNull(taskDefinitions);
//        assertEquals(3, taskDefinitions.size());
//    }

//    @Test
//    public void testGetLogicalPeopleGroups() {
////        List<Group> groups = taskManager.getLogicalPeopleGroups();
////        assertNotNull(groups);
////        assertEquals(6, groups.size());
//    }

    //TODO jkr: move
//    @Test
//    public void testGetLogicalPeopleGroupByName() throws HumanTaskException {
//        Group group = taskManager.getLogicalPeopleGroupByName("clerksManager");
//        assertNotNull(group);
//    }
//
//    @Test(expected = HumanTaskException.class)
//    public void testLogicalPeopleGroupNotFound() throws HumanTaskException {
//        taskManager.getLogicalPeopleGroupByName("JKR!@#");
//    }
}