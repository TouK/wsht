/*
 * Copyright (c) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.Before;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.util.TestUtil;
import junit.framework.Assert;

/**
 * TODO switch to unit test
 *
 * @author Witek Wołejszo
 */
public class TaskDefinitionUnitTest {

    private final Log log = LogFactory.getLog(TaskDefinitionUnitTest.class);

    private HumanInteractionsManagerInterface humanInteractionsManager;

    @Before
    public void setUpTestContext() throws HumanTaskException {

        this.humanInteractionsManager = TestUtil.createHumanInteractionsManager("htd1.xml", "testHtd1.xml");

    }

    @Test
    public void testGetDescriptionPlain() throws HumanTaskException {
        TaskDefinition td = humanInteractionsManager.getTaskDefinition("ApproveClaim");
        String description = td.getDescription("en-US", "text/plain", null);

        assertEquals("Approve this claim following corporate guideline #4711.0815/7 ...", description.trim());

    }

    @Test
    public void testEvaluateHumanRoleAssignees() throws HumanTaskException {
        TaskDefinition td = humanInteractionsManager.getTaskDefinition("ApproveClaim1");
        List<Assignee> assigneeList = td.evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS, null);
        Assert.assertEquals(0, assigneeList.size());
        List<Assignee> bussinessAdministrators = td.evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, null);
        Assert.assertEquals(0, bussinessAdministrators.size());
    }

    @Test
    public void testGenericHumanRoleNotFoundInTaskDefinition() throws HumanTaskException {
        TaskDefinition td = humanInteractionsManager.getTaskDefinition("ApproveClaim");
        List<Assignee> assigneeList = td.evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS, null);
        Assert.assertEquals(0, assigneeList.size());
    }

    @Test
    public void testGetSubject() throws HumanTaskException {
        TaskDefinition td = humanInteractionsManager.getTaskDefinition("ApproveClaim");
        String expResult = "Approve the insurance claim for €$euroAmount$ on";
        String result = td.getSubject("en-US", null);
        assertTrue(result.contains(expResult));
    }

    @Test
    public void testGetKey() throws HumanTaskException {
        TaskDefinition td = humanInteractionsManager.getTaskDefinition("ApproveClaim");
        String expResult = "ApproveClaim";
        String result = td.getTaskName();
        assertTrue(result.startsWith(expResult));
    }


}
