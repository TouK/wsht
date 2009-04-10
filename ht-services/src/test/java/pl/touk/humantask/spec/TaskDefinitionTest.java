package pl.touk.humantask.spec;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * TODO JUNIT4
 * @author Witek Wołejszo
 */
public class TaskDefinitionTest extends TestCase {

    private final Log log = LogFactory.getLog(TaskDefinitionTest.class);

    ApplicationContext applicationContext;

    @Override
    protected void setUp() throws Exception {

        super.setUp();
        applicationContext = new ClassPathXmlApplicationContext("test.xml");

    }

    public void xtestGetDescriptionPlain() {

        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
        String description = td.getDescription("en-US", "text/plain");

        log.debug(description);

        assertEquals("Approve this claim following corporate guideline #4711.0815/7 ...", description.trim());

    }

    public void xtestGetDescriptionHtml() {

        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
        String description = td.getDescription("en-US", "text/html");

        log.debug(description);

        assertEquals("<p> Approve this claim following corporate guideline <b>#4711.0815/7</b> ... </p>", description.trim());

    }

    public void xtestGetPotentialOwners() {

        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
        List<String> r = td.getPotentialOwners();

        log.debug(r);

        assertTrue(r.contains("regionalClerks"));

    }
    
    public void testX() {
        assertTrue(true);
    }

}
