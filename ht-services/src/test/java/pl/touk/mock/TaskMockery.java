package pl.touk.mock;

import java.util.HashSet;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;

import pl.touk.humantask.dao.AssigneeDao;
import pl.touk.humantask.dao.TaskDao;
import pl.touk.humantask.exceptions.HTException;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Person;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.model.spec.TaskDefinition;

public class TaskMockery extends Mockery {
    
    TaskDao taskDao;
    AssigneeDao assigneeDao;
    
    Task task = null;
    
    Person jacek = new Person("Jacek");
    Person witek = new Person("Witek");
    Person admin = new Person("admin");

    public TaskMockery(TaskDao taskDao, AssigneeDao assigneeDao) {
        this.assigneeDao = assigneeDao;
        this.taskDao = taskDao;
    }

    public Task getGoodTaskMock(boolean onlyOnePotentialOwner) {

        setImposteriser(ClassImposteriser.INSTANCE);  
       
        final TaskDefinition taskDefinition = mock(TaskDefinition.class);

        final Set<Assignee> assignees = new HashSet<Assignee>();
        assignees.add(jacek);
        
        final Set<Assignee> stakeholders = new HashSet<Assignee>();
        stakeholders.add(jacek);

        if (!onlyOnePotentialOwner) {
            assignees.add(witek);
            stakeholders.add(witek);
        }

        this.assigneeDao.create(jacek);
        this.assigneeDao.create(witek);
        this.assigneeDao.create(admin);

        checking(new Expectations() {{
            try{
                allowing(taskDefinition).getTaskName();
                will(returnValue("Task1"));
                allowing(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS, with(any(Task.class)));
                will(returnValue(assignees));
                allowing(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, with(any(Task.class)));
                will(returnValue(new HashSet()));
                allowing(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.EXCLUDED_OWNERS, with(any(Task.class)));
                will(returnValue(new HashSet()));
                allowing(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS, with(any(Task.class)));
                will(returnValue(new HashSet()));
                allowing(taskDefinition).evaluateHumanRoleAssignees(GenericHumanRole.TASK_STAKEHOLDERS, with(any(Task.class)));
                will(returnValue(stakeholders));
            } catch (Exception e){
                
                e.printStackTrace();
                
            }
        }});

        try {
            
            task = new Task(taskDefinition, null, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ClaimApprovalRequest><cust><firstname>witek</firstname></cust><amount>1</amount></ClaimApprovalRequest>");

            if (onlyOnePotentialOwner) {
                Assert.assertTrue(task.getActualOwner().equals(jacek));
            } else {
                Assert.assertNull(task.getActualOwner());
            }
            
        } catch (HTException ex) {
           
            ex.printStackTrace();
        }

        taskDao.create(task);
      
        return task;
    }

    public Person getImpossibleOwner() {
       return admin;
    }
    
    public Person getPossibleOwner() {
        return jacek;
    }
    
}