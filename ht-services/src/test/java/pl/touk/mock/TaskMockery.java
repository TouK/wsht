package pl.touk.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import pl.touk.humantask.dao.AssigneeDao;
import pl.touk.humantask.dao.TaskDao;
import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Person;
import pl.touk.humantask.model.Message;
import pl.touk.humantask.model.Task.Status;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.spec.TaskDefinition;

public class TaskMockery extends Mockery {
    
    TaskDao taskDao;
    AssigneeDao assigneeDao;
    
    Task task = null;
    
    Person jacek = new Person("Jacek");
    Person witek = new Person("Witek");
    Person admin = new Person("admin");


    public TaskMockery(TaskDao taskDao,AssigneeDao assigneeDao) {
        this.assigneeDao = assigneeDao;
        this.taskDao = taskDao;
    }
    
    public Task getGoodTaskMock() throws HumanTaskException {
    
        setImposteriser(ClassImposteriser.INSTANCE);  
       
        final TaskDefinition taskDefinition = mock(TaskDefinition.class);

        final Map<String, Message> mockMap = new HashMap<String, Message>();
        mockMap.put(Message.DEFAULT_PART_NAME_KEY, new Message("x"));
        final List<Assignee> assignees = new ArrayList<Assignee>();

        assignees.add(jacek);
        assignees.add(witek);

        assigneeDao.create(jacek);
        assigneeDao.create(witek);
        assigneeDao.create(admin);

        checking(new Expectations() {{
            one(taskDefinition).getTaskName();
            will(returnValue("taskLookupKey"));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS, mockMap);
            will(returnValue(assignees));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, mockMap);
            will(returnValue(Collections.EMPTY_LIST));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.EXCLUDED_OWNERS, mockMap);
            will(returnValue(Collections.EMPTY_LIST));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS, mockMap);
            will(returnValue(Collections.EMPTY_LIST));
            one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.TASK_STAKEHOLDERS, mockMap);
            will(returnValue(Collections.EMPTY_LIST));
        }});
        
        try {
            task = new Task(taskDefinition, jacek, "<?xml version='1.0'?><root/>");
        } catch (HumanTaskException ex) {
           
        }
       
        task.setTaskStakeholders(Arrays.asList((Assignee)jacek));
        
        taskDao.create(task);
      
        return task;
    }

    public void assignOwner() throws HumanTaskException{
        task.setActualOwner(jacek);

        task.setStatus(Status.IN_PROGRESS);
        
    }

    public Person getImpossibleOwner() {
       return admin;
    }
    
    public Person getPossibleOwner() {
        return jacek;
    }
    
}