package pl.touk.humantask.spec;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.spec.TaskDefinition.LogicalPeopleGroup;

import java.util.List;

/**
 * TODO change to *IntegrationTest, use spring-testing
 * @author Witek Wołejszo
 * @author Mateusz Lipczyński
 */
public class TaskDefinitionTest extends TestCase {

    private final Log log = LogFactory.getLog(TaskDefinitionTest.class);

    ApplicationContext applicationContext;

    HumanInteractionsManagerInterface htManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        applicationContext = new ClassPathXmlApplicationContext("test.xml");
        this.htManager = (HumanInteractionsManagerInterface) applicationContext.getBean("taskManager");
    }

    @Test
    public void testGetDescriptionPlain() throws HumanTaskException {
//      TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
        TaskDefinition td = htManager.getTaskDefinition("ApproveClaim");
        String description = td.getDescription("en-US", "text/plain", null);
        log.debug(description);
        assertEquals("Approve this claim following corporate guideline #4711.0815/7 ...", description.trim());
    }

//    @Test
//    public void testGetPotentialOwners() throws HumanTaskException {
////        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
//        TaskDefinition td = htManager.getTaskDefinitionByName("ApproveClaim");
//        List<String> r = td.getPotentialOwners();
//        log.debug(r);
//        assertTrue(r.contains("regionalClerks"));
//    }

    @Test
    public void testEvaluateHumanRoleAssignees() throws HumanTaskException {
        TaskDefinition td = htManager.getTaskDefinition("ApproveClaim");
        List<Assignee> assigneeList = td.evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS, null);
        Assert.assertEquals(2, assigneeList.size());
        List<Assignee> bussinessAdministrators = td.evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, null);
        Assert.assertEquals(1, bussinessAdministrators.size());
    }

//    @Test(expected = HumanTaskException.class)
//    public void testGenericHumanRoleNotFoundInTaskDefinition() throws HumanTaskException {
//        TaskDefinition td = htManager.getTaskDefinition("ApproveClaim");
//        td.evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS, null);
//    }
    
    /**
     * Test of getSubject method, of class TaskDefinition.
     */
    @Test
    public void testGetSubject() throws HumanTaskException {
        System.out.println("getSubject");
//        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
        TaskDefinition td = htManager.getTaskDefinition("ApproveClaim");
        String expResult = "Approve the insurance claim for €$euroAmount$ on";
        String result = td.getSubject("en-US", null);
        assertTrue(result.contains(expResult));
    }

    /**
     * Test of getKey method, of class TaskDefinition.
     */
    @Test
    public void testGetKey() throws HumanTaskException {
        System.out.println("getKey");
//        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
        TaskDefinition td = htManager.getTaskDefinition("ApproveClaim");
        String expResult = "ApproveClaim";
        String result = td.getTaskName();
        assertTrue(result.startsWith(expResult));
    }

    /**
     * Test of getLogicalpeopleGroups method, of class TaskDefinition.
     */
    @Test
    public void testGetLogicalpeopleGroups() throws HumanTaskException {
//        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
        TaskDefinition td = htManager.getTaskDefinition("ApproveClaim");
        List<LogicalPeopleGroup> result = td.getLogicalpeopleGroups();
        assertEquals(6, result.size());
    }

    /**
     * Test of evaluate method, of class TaskDefinition.
     */
    /*
  @Test
  public void testEvaluate() throws HumanTaskException {
      //TODO evaluation test
      LogicalPeopleGroup logicalPeopleGroup = null;
      TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
      Person createdBy = new Person("witek");
      String requestXml = "";
      Task task = new Task(td, createdBy, requestXml);
      List<Assignee> expResult = null;
      List<Assignee> result = td.evaluate(logicalPeopleGroup, task);
      assertEquals(expResult, result);
  }
    */

}
