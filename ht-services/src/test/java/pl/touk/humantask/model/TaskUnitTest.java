/*
 * Copyright (c) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.spec.TaskDefinition;

/**
 * {@link Task} class unit tests.
 *
 * @author Witek Wołejszo
 * @author Mateusz Lipczyński
 */
public class TaskUnitTest {

    @Test
    public void testInstatiationOnePotentialOwner() throws HumanTaskException {

        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);
        final Map<String, Message> mockMap = new HashMap<String, Message>();
        mockMap.put(Message.DEFAULT_PART_NAME_KEY, new Message(null));
        final List<Assignee> assignees = new ArrayList<Assignee>();
        assignees.add(new Person("mateusz"));
        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue("taskLookupKey"));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS, mockMap);          will(returnValue(assignees));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, mockMap);   will(returnValue(Collections.EMPTY_LIST));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.EXCLUDED_OWNERS, mockMap);           will(returnValue(Collections.EMPTY_LIST));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS, mockMap);   will(returnValue(Collections.EMPTY_LIST));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.TASK_STAKEHOLDERS, mockMap);         will(returnValue(Collections.EMPTY_LIST));
        }});

        Task task = new Task(taskDefinition, null, null);
        assertEquals(Task.Status.RESERVED, task.getStatus());

        mockery.assertIsSatisfied();
    }

    @Test
    public void testInstatiationNoPotentialOwners() throws HumanTaskException {

        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);
        final Map<String, Message> mockMap = new HashMap<String, Message>();
        mockMap.put(Message.DEFAULT_PART_NAME_KEY, new Message(null));
        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue("taskLookupKey"));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS, mockMap);          will(returnValue(Collections.EMPTY_LIST));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, mockMap);   will(returnValue(Collections.EMPTY_LIST));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.EXCLUDED_OWNERS, mockMap);           will(returnValue(Collections.EMPTY_LIST));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS, mockMap);   will(returnValue(Collections.EMPTY_LIST));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.TASK_STAKEHOLDERS, mockMap);         will(returnValue(Collections.EMPTY_LIST));
        }});

        Task task = new Task(taskDefinition, null, null);
        assertEquals(Task.Status.CREATED, task.getStatus());

        mockery.assertIsSatisfied();
    }

    @Test
    public void testInstatiationManyPotentialOwners() throws HumanTaskException {

        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);
        final Map<String, Message> mockMap = new HashMap<String, Message>();
        mockMap.put(Message.DEFAULT_PART_NAME_KEY, new Message(null));
        final List<Assignee> assignees = new ArrayList<Assignee>();
        assignees.add(new Person("mateusz"));
        assignees.add(new Person("witek"));
        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue("taskLookupKey"));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS, mockMap);          will(returnValue(assignees));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, mockMap);   will(returnValue(Collections.EMPTY_LIST));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.EXCLUDED_OWNERS, mockMap);           will(returnValue(Collections.EMPTY_LIST));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS, mockMap);   will(returnValue(Collections.EMPTY_LIST));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.TASK_STAKEHOLDERS, mockMap);         will(returnValue(Collections.EMPTY_LIST));
        }});

        Task task = new Task(taskDefinition, null, null);
        assertEquals(Task.Status.READY, task.getStatus());

        mockery.assertIsSatisfied();
    }

    /**
     * Test of nominateActualOwner method, of class Task.
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
}
