package pl.touk.humantask.model;

import static org.junit.Assert.*;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.spec.TaskDefinition;

public class TaskUnitTest {

    @Test
    public void testInstatiation() throws HumanTaskException {
        
        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);  
        }};
        
        final TaskDefinition taskDefinition = mockery.mock(TaskDefinition.class);
        
        mockery.checking(new Expectations() {{
            one(taskDefinition).getKey(); will(returnValue("taskLookupKey"));
        }});
        
        Task task = new Task(new Person("witek"), taskDefinition);
        assertEquals(Task.Status.RESERVED, task.getStatus());
        
        mockery.assertIsSatisfied();
    }
    
}
