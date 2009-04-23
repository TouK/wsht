/*
 * Copyright (c) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.w3c.dom.Element;

import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.spec.TaskDefinition;

/**
 * {@link Task} class unit tests.
 *
 * @author Witek Wołejszo
 * @author Mateusz Lipczyński
 */
public class TaskUnitTest {

    private final Log log = LogFactory.getLog(TaskUnitTest.class);
    
    /**
     * Tests Task constructor.
     * Scenario: 1 potential owner. Expected status: RESERVED.
     */
    @Test
    public void testInstatiationOnePotentialOwner() throws HumanTaskException {

        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);
        
        final List<Assignee> assignees = new ArrayList<Assignee>();
        assignees.add(new Person("mateusz"));
        
        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue("taskLookupKey"));
            one(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Map.class))); will(returnValue(assignees));
            atLeast(1).of(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Map.class))); will(returnValue(Collections.EMPTY_LIST));
        }});

        Task task = new Task(taskDefinition, null, "<?xml version=\"1.0\" encoding=\"UTF-8\"?></x>");
        assertEquals(Task.Status.RESERVED, task.getStatus());

        mockery.assertIsSatisfied();
    }

    /**
     * Tests Task constructor.
     * Scenario: no potential owners. Expected status: CREATED.
     */
    @Test
    public void testInstatiationNoPotentialOwners() throws HumanTaskException {

        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);
        //final Map<String, Message> mockMap = new HashMap<String, Message>();
        //mockMap.put(Message.DEFAULT_PART_NAME_KEY, new Message(null));
        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue("taskLookupKey"));
            atLeast(1).of(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Map.class))); will(returnValue(Collections.EMPTY_LIST));
        }});

        Task task = new Task(taskDefinition, null, "</x>");
        assertEquals(Task.Status.CREATED, task.getStatus());

        mockery.assertIsSatisfied();
    }

    /**
     * Tests Task constructor.
     * Scenario: 2 potential owners. Expected status: READY.
     */
    @Test
    public void testInstatiationManyPotentialOwners() throws HumanTaskException {

        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);
        
//        final Map<String, Message> mockMap = new HashMap<String, Message>();
//        mockMap.put(Message.DEFAULT_PART_NAME_KEY, new Message(null));
        
        final List<Assignee> assignees = new ArrayList<Assignee>();
        assignees.add(new Person("mateusz"));
        assignees.add(new Person("witek"));
        
        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue("taskLookupKey"));
            one(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Map.class))); will(returnValue(assignees));
            atLeast(1).of(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Map.class))); will(returnValue(Collections.EMPTY_LIST));
        }});

        Task task = new Task(taskDefinition, null, "</x>");
        assertEquals(Task.Status.READY, task.getStatus());

        mockery.assertIsSatisfied();
    }

    /**
     * Tests Task.nominateActualOwner method.
     * 1. Input: Empty assignees. Output: null.
     * 2. Input: One person and a group assigned. Output: that person.
     * 3. Input: Two persons and a group. Output: null.
     */
    @Test
    public void testNominateActualOwner() {
        List<Assignee> assignees = new ArrayList<Assignee>();
        Task instance = new Task();
        Person result = instance.nominateActualOwner(assignees);
        assertEquals(null, result);

        Person person1 = new Person("mateusz");
        Group group = new Group ("pracownicy DON");
        assignees.add(group);
        assignees.add(person1);
        result = instance.nominateActualOwner(assignees);
        assertEquals(person1, result);

        Person person2 = new Person("ww");
        assignees.add(person2);
        result = instance.nominateActualOwner(assignees);
        assertEquals(null, result);
    }
    
    @Test
    public void testEvaluateXPathGetInput() throws HumanTaskException {
        
        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);

        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue("taskLookupKey"));
            atLeast(1).of(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Map.class))); will(returnValue(Collections.EMPTY_LIST));
        }});
        
        Task t = new Task(taskDefinition, null, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><a><b>test</b></a>");
        Element e = (Element) t.evaluateXPath("htd:getInput('a')/a/b");
        
        //log.info(e.getTextContent());
        
        assertNotNull(e);
        assertEquals("test", e.getTextContent());
        
    }

}
