/*
 * Copyright (c) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import pl.touk.humantask.exceptions.HumanTaskException;

/**
 * TODO switch to unit test
 * @author Witek Wołejszo
 */
@ContextConfiguration(locations = "classpath:/test.xml")
public class TaskDefinitionIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

    private final Log log = LogFactory.getLog(TaskDefinitionIntegrationTest.class);

    @Test
    public void test() {
        assertTrue(true);
    }
    
    //@Test
    public void testGetDescriptionPlain() throws HumanTaskException {
        HumanInteractionsManagerInterface htManager = (HumanInteractionsManagerInterface) applicationContext.getBean("taskManager");
//        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
        TaskDefinition td = htManager.getTaskDefinition("ApproveClaim");
        String description = td.getDescription("en-US", "text/plain", null);

        log.debug(description);

        assertEquals("Approve this claim following corporate guideline #4711.0815/7 ...", description.trim());

    }

    //@Test
    public void testGetDescriptionHtml() throws HumanTaskException {

//        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
        HumanInteractionsManagerInterface htManager = (HumanInteractionsManagerInterface) applicationContext.getBean("taskManager");
        
        TaskDefinition td = htManager.getTaskDefinition("ApproveClaim");
        String description = td.getDescription("en-US", "text/html", null);

        log.debug(description);

        assertEquals("<p> Approve this claim following corporate guideline <b>#4711.0815/7</b> ... </p>", description.trim());

    }

//    @Test
//    public void testGetPotentialOwners() throws HumanTaskException {
//        HumanInteractionsManagerInterface htManager = (HumanInteractionsManagerInterface) applicationContext.getBean("taskManager");
////        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
//        TaskDefinition td = htManager.getTaskDefinitionByName("ApproveClaim");
//        List<String> r = td.getPotentialOwners();
//
//        assertTrue(r.contains("regionalClerks"));
//
//    }

}
