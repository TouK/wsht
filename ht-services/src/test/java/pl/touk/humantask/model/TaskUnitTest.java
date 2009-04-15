/*
 * Copyright (c) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

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
 */
public class TaskUnitTest {

    @Test
    public void testInstatiationOnePotentialOwner() throws HumanTaskException {
        
        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);  
        }};
        
        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);
        
        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue("taskLookupKey"));
        }});
        
        Task task = new Task(taskDefinition, new Person("witek"));
        assertEquals(Task.Status.RESERVED, task.getStatus());
        
        mockery.assertIsSatisfied();
    }

    @Test
    public void testInstatiationNoPotentialOwners() throws HumanTaskException {

        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);

        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue("taskLookupKey"));
        }});

        Task task = new Task(taskDefinition, null);
        assertEquals(Task.Status.CREATED, task.getStatus());

        mockery.assertIsSatisfied();
    }
    /**
     * Tests instantiation when no actual owner is passed. This is correct call.
     * @throws HumanTaskException
     */
    @Test
    public void testInstatiationNoActualOwner() throws HumanTaskException {
        
        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);  
        }};
        
        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);
        
        mockery.checking(new Expectations() {{
            one(taskDefinition).getTaskName(); will(returnValue("taskLookupKey"));
        }});
        
        Task task = new Task(taskDefinition, null);
        assertEquals(Task.Status.CREATED, task.getStatus());
        
        mockery.assertIsSatisfied();
    }
    
}
