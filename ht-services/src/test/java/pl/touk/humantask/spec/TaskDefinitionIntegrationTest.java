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

/**
 * TODO switch to unit test
 * @author Witek Wołejszo
 */
@ContextConfiguration(locations = "classpath:/test.xml")
public class TaskDefinitionIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

    private final Log log = LogFactory.getLog(TaskDefinitionIntegrationTest.class);

    //@Test
    public void testGetDescriptionPlain() {

        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
        String description = td.getDescription("en-US", "text/plain");

        log.debug(description);

        assertEquals("Approve this claim following corporate guideline #4711.0815/7 ...", description.trim());

    }

    //@Test
    public void testGetDescriptionHtml() {

        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
        String description = td.getDescription("en-US", "text/html");

        log.debug(description);

        assertEquals("<p> Approve this claim following corporate guideline <b>#4711.0815/7</b> ... </p>", description.trim());

    }

    @Test
    public void testGetPotentialOwners() {

        TaskDefinition td = (TaskDefinition) applicationContext.getBean("ApproveClaimTask");
        List<String> r = td.getPotentialOwners();

        assertTrue(r.contains("regionalClerks"));

    }

}
