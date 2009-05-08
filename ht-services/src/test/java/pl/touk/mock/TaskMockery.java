package pl.touk.mock;

import java.util.HashSet;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import pl.touk.humantask.dao.AssigneeDao;
import pl.touk.humantask.dao.TaskDao;
import pl.touk.humantask.exceptions.HTException;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Person;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.model.Task.Status;
import pl.touk.humantask.model.spec.TaskDefinition;

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

        final Set<Assignee> assignees = new HashSet<Assignee>();
        assignees.add(jacek);

        if (!onlyOnePotentialOwner) {
            assignees.add(witek);
        }

        assigneeDao.create(jacek);
        assigneeDao.create(witek);
        assigneeDao.create(admin);

        checking(new Expectations() {{
            try{
                allowing(taskDefinition).getTaskName();
                will(returnValue("taskLookupKey"));
                allowing(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS, with(any(Task.class)));
                will(returnValue(assignees));
                allowing(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, with(any(Task.class)));
                will(returnValue(new HashSet()));
                allowing(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.EXCLUDED_OWNERS, with(any(Task.class)));
                will(returnValue(new HashSet()));
                allowing(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS, with(any(Task.class)));
                will(returnValue(new HashSet()));
                allowing(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.TASK_STAKEHOLDERS, with(any(Task.class)));
                will(returnValue(new HashSet()));
            } catch (Exception x){
            }
        }});

        
        try {
            task = new Task(taskDefinition, jacek, "<?xml version='1.0'?><root/>");
        } catch (HTException ex) {
           
        }

        Set<Assignee> stakeholders = new HashSet<Assignee>();
        stakeholders.add(jacek);

        task.setTaskStakeholders(stakeholders);
        
        taskDao.create(task);
      
        return task;
    }

    public void assignOwner() throws HTException{
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