package pl.touk.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

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
    
    public Task getGoodTaskMock() {
        return getGoodTaskMock(false);
    }

    public Task getGoodTaskMock(boolean onlyOnePotentialOwner) {

        setImposteriser(ClassImposteriser.INSTANCE);  
       
        final TaskDefinition taskDefinition = mock(TaskDefinition.class);

        final Map<String, Message> mockMap = new HashMap<String, Message>();
        mockMap.put(Message.DEFAULT_PART_NAME_KEY, new Message("x"));
        final List<Assignee> assignees = new ArrayList<Assignee>();

        assignees.add(jacek);

        if (!onlyOnePotentialOwner) {
            assignees.add(witek);
        }

        assigneeDao.create(jacek);
        assigneeDao.create(witek);
        assigneeDao.create(admin);

        checking(new Expectations() {{
            try{
                one(taskDefinition).getTaskName();
                will(returnValue("taskLookupKey"));
                one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS, mockMap);
                will(returnValue(assignees));
                one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, mockMap);
                will(returnValue(new ArrayList()));
                one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.EXCLUDED_OWNERS, mockMap);
                will(returnValue(new ArrayList()));
                one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS, mockMap);
                will(returnValue(new ArrayList()));
                one(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.TASK_STAKEHOLDERS, mockMap);
                will(returnValue(new ArrayList()));
            } catch (Exception x){
            }
        }});

        
        try {
            task = new Task(taskDefinition, jacek, "<?xml version='1.0'?><root/>");
        } catch (HumanTaskException ex) {
           
        }

        List<Assignee> stakeholders = new ArrayList<Assignee>();
        stakeholders.add((Assignee)jacek);

        task.setTaskStakeholders(stakeholders);
        
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