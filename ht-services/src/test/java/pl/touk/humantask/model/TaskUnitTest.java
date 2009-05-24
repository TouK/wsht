/*
 * Copyright (c) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPathConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;

import pl.touk.humantask.exceptions.HTException;
import pl.touk.humantask.model.spec.TaskDefinition;

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
    public void testInstatiationOnePotentialOwner() throws HTException {

        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);
        
        final Set<Assignee> assignees = new HashSet<Assignee>();
        assignees.add(new Person("mateusz"));
        
        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue("Task1"));
            //one(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Task.class))); will(returnValue(assignees));
            atLeast(1).of(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Task.class))); will(returnValue(assignees));
        }});

        Task task = new Task(taskDefinition, null, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ClaimApprovalRequest><cust><firstname>witek</firstname></cust></ClaimApprovalRequest>");
        assertEquals(Task.Status.RESERVED, task.getStatus());

        mockery.assertIsSatisfied();
    }

    /**
     * Tests Task constructor.
     * Scenario: no potential owners. Expected status: CREATED.
     * TODO ready!!! created in case of many ponetial owners!!!
     */
    @Test
    public void testInstatiationNoPotentialOwners() throws HTException {

        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);
        final Set<Assignee> assignees = new HashSet<Assignee>();
        assignees.add(new Person("mateusz"));
        //final Map<String, Message> mockMap = new HashMap<String, Message>();
        //mockMap.put(Message.DEFAULT_PART_NAME_KEY, new Message(null));
        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue("Task1"));
            //potential owners
            one(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Task.class))); will(returnValue(Collections.EMPTY_SET));
            //other roles
            atLeast(1).of(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Task.class))); will(returnValue(assignees));
        }});

        Task task = new Task(taskDefinition, null, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ClaimApprovalRequest><cust><firstname>witek</firstname></cust></ClaimApprovalRequest>");
        assertEquals(Task.Status.CREATED, task.getStatus());

        mockery.assertIsSatisfied();
    }

    /**
     * Tests Task constructor.
     * Scenario: 2 potential owners. Expected status: READY.
     */
    @Test
    public void testInstatiationManyPotentialOwners() throws HTException {

        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);
        
//        final Map<String, Message> mockMap = new HashMap<String, Message>();
//        mockMap.put(Message.DEFAULT_PART_NAME_KEY, new Message(null));
        
        final Set<Assignee> assignees = new HashSet<Assignee>();
        assignees.add(new Person("mateusz"));
        assignees.add(new Person("witek"));
        
        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue("Task1"));
            //one(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Task.class))); will(returnValue(assignees));
            atLeast(1).of(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Task.class))); will(returnValue(assignees));
        }});

        Task task = new Task(taskDefinition, null, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ClaimApprovalRequest><cust><firstname>witek</firstname></cust></ClaimApprovalRequest>");
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
        
        Set<Assignee> assignees = new HashSet<Assignee>();
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
    public void testEvaluateXPathGetInput_String() throws HTException {
        
        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);
        final Set<Assignee> assignees = new HashSet<Assignee>();
        assignees.add(new Person("mateusz"));

        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue("Task1"));
            atLeast(1).of(taskDefinition).evaluateHumanRoleAssignees(with(any(GenericHumanRole.class)), with(any(Task.class))); will(returnValue(assignees));
        }});
        
        Task t = new Task(taskDefinition, null, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ClaimApprovalRequest><cust><firstname>witek</firstname></cust></ClaimApprovalRequest>");
        Object o = t.evaluateXPath("htd:getInput('ClaimApprovalRequest')/cust/firstname", XPathConstants.STRING);

        assertNotNull(o);
        assertTrue(o instanceof String);
        assertEquals("witek", o.toString());
    }

}
