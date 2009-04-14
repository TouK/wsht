package pl.touk.humantask.spec;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.PeopleQuery;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Person;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.spec.TaskDefinition.LogicalPeopleGroup;

/**
 * 
 * @author Witek Wołejszo
 * @author Mateusz Lipczyński
 */
public class TaskDefinitionTest extends TestCase {

    private final Log log = LogFactory.getLog(TaskDefinitionTest.class);

    ApplicationContext applicationContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        applicationContext = new ClassPathXmlApplicationContext("test.xml");
    }

    @Test
    public void testGetDescriptionPlain() {
        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
        String description = td.getDescription("en-US", "text/plain");
        log.debug(description);
        assertEquals("Approve this claim following corporate guideline #4711.0815/7 ...", description.trim());
    }

    @Test
    public void testGetPotentialOwners() {
        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
        List<String> r = td.getPotentialOwners();
        log.debug(r);
        assertTrue(r.contains("regionalClerks"));
    }

    /**
     * Test of getSubject method, of class TaskDefinition.
     */
    @Test
    public void testGetSubject() {
        System.out.println("getSubject");
        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
        String expResult = "Approve the insurance claim for €$euroAmount$ on";
        String result = td.getSubject("en-US");
        assertTrue(result.contains(expResult));
    }

    /**
     * Test of getKey method, of class TaskDefinition.
     */
    @Test
    public void testGetKey() {
        System.out.println("getKey");
        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
        String expResult = "ApproveClaim";
        String result = td.getKey();
        assertTrue(result.startsWith(expResult));
    }

    /**
     * Test of getLogicalpeopleGroups method, of class TaskDefinition.
     */
    @Test
    public void testGetLogicalpeopleGroups() {
        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
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
