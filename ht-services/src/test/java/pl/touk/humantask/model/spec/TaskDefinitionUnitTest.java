/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import pl.touk.humantask.HumanInteractionsManager;
import pl.touk.humantask.exceptions.HTException;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Group;
import pl.touk.humantask.model.Person;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.model.spec.TaskDefinition;
import pl.touk.humantask.util.TestUtil;

/**
 * Test of {@link TaskDefinition} class.
 *
 * @author Witek Wołejszo
 */
public class TaskDefinitionUnitTest {

    private final Log log = LogFactory.getLog(TaskDefinitionUnitTest.class);

    private HumanInteractionsManager humanInteractionsManager;
    
    private static final String REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ClaimApprovalRequest><priority>7</priority><cust><firstname>jan</firstname><lastname>kowalski</lastname></cust><amount>1</amount></ClaimApprovalRequest>";

    @Before
    public void setUpTestContext() throws HTException {

        this.humanInteractionsManager = TestUtil.createHumanInteractionsManager("testHtd1.xml");
    }

    @Test
    public void testGetDescriptionPlain() throws HTException {       
        TaskDefinition td = this.humanInteractionsManager.getTaskDefinition("Task1");
        Task task = new Task(td, null, REQUEST);
        String description = td.getDescription("en-US", "text/plain", task);
        assertEquals("Approve this claim following corporate guideline #4711.0815/7 ...".trim(), description.trim());
    }
    
    @Test
    public void testGetPriority() throws HTException {       
        TaskDefinition td = this.humanInteractionsManager.getTaskDefinition("Task1");
        Task task = new Task(td, null, REQUEST);
        Integer priority = td.getPriority(task);
        assertEquals(Integer.valueOf(7), priority);
    }

    /**
     * Test people query implementation returns zero.
     * @throws HTException
     */
    @Test
    public void testEvaluateHumanRoleAssignees() throws HTException {
        
        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};
        final Task task = mockery.mock(Task.class);
        
        TaskDefinition td = this.humanInteractionsManager.getTaskDefinition("Task1");
        
        Set<Assignee> assigneeList = td.evaluateHumanRoleAssignees(GenericHumanRole.POTENTIAL_OWNERS, task);
        Assert.assertEquals(1, assigneeList.size());
        
        Set<Assignee> bussinessAdministrators = td.evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, task);
        Assert.assertEquals(4, bussinessAdministrators.size());
    }
    
    @Test
    public void testEvaluateHumanRoleAssigneesUnresolvedGroupOfPeople() throws HTException {
        
        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};
        final Task task = mockery.mock(Task.class);
        
        TaskDefinition td = humanInteractionsManager.getTaskDefinition("Task1");
        Set<Assignee> assigneeList = td.evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, task);
        
        Assert.assertTrue(assigneeList.contains(new Group("group1")));
        Assert.assertTrue(assigneeList.contains(new Group("group2")));
    }
    
    @Test
    public void testEvaluateHumanRoleAssigneesPeople() throws HTException {
        
        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};
        final Task task = mockery.mock(Task.class);
        
        TaskDefinition td = humanInteractionsManager.getTaskDefinition("Task1");
        Set<Assignee> assigneeList = td.evaluateHumanRoleAssignees(GenericHumanRole.BUSINESS_ADMINISTRATORS, task);
        
        Assert.assertTrue(assigneeList.contains(new Person("user1")));
        Assert.assertTrue(assigneeList.contains(new Person("user2")));
    }

    @Test
    public void testGenericHumanRoleNotFoundInTaskDefinition() throws HTException {
        
        Mockery mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};
        final Task task = mockery.mock(Task.class);
        
        TaskDefinition td = humanInteractionsManager.getTaskDefinition("Task1");
        Set<Assignee> assigneeList = td.evaluateHumanRoleAssignees(GenericHumanRole.NOTIFICATION_RECIPIENTS, task);
        Assert.assertEquals(0, assigneeList.size());
    }

    @Test
    public void testGetSubject() throws HTException {
        TaskDefinition td = humanInteractionsManager.getTaskDefinition("Task1");
        Task task = new Task(td, null, REQUEST);
        String expResult = "Approve the insurance claim for PLN 1.0";
        String result = td.getSubject("en-US", task);
        log.debug("result: " + result);
        assertTrue(result.contains(expResult));
    }

    @Test
    public void testGetKey() throws HTException {
        TaskDefinition td = humanInteractionsManager.getTaskDefinition("Task1");
        String result = td.getTaskName();
        assertEquals("Task1", result);
    }

    /**
     * Checks for existance and value of presenation parameter:
     * <htd:presentationParameter name="firstname" type="xsd:string">
     *     htd:getInput("ClaimApprovalRequest")/cust/firstname
     * </htd:presentationParameter>
     * 
     * @throws HTException
     */
    @Test
    public void testGetTaskPresentationParameters() throws HTException {
        
        TaskDefinition td = humanInteractionsManager.getTaskDefinition("Task1");

        Task task = new Task(td, null, REQUEST);
        
        Map<String, Object> result = td.getTaskPresentationParameters(task);
        
        assertTrue(result.containsKey("firstname"));
        assertTrue(result.containsKey("lastname"));
        assertTrue(result.containsKey("euroAmount"));

        log.info(result.get("firstname"));
        log.info(result.get("lastname"));
        log.info(result.get("euroAmount"));
        
        assertEquals("jan", result.get("firstname"));
    }

}
