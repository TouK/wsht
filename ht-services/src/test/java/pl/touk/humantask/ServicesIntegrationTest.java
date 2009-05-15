package pl.touk.humantask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import pl.touk.humantask.dao.AssigneeDao;
import pl.touk.humantask.dao.TaskDao;
import pl.touk.humantask.exceptions.HTException;
import pl.touk.humantask.exceptions.HTIllegalAccessException;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.model.Task.Status;
import pl.touk.humantask.model.Task.TaskTypes;
import pl.touk.mock.TaskMockery;

/**
 * {@link HumanTaskServicesImpl} integration tests.
 *
 * @author Witek Wołejszo
 * @author Warren Crossing
 * @author Kamil Eisenbart
 * @author Piotr Jagielski
 * @author Mateusz Lipczyński
 */
@ContextConfiguration(locations = {"classpath:/test.xml"})
public class ServicesIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

    private final Log LOG = LogFactory.getLog(ServicesIntegrationTest.class);

    @Resource(name = "humanTaskServices")
    HumanTaskServices services;

    @Resource(name = "taskDao")
    TaskDao taskDao;

    @Resource(name = "assigneeDao")
    AssigneeDao assigneeDao;

    @Test
    @Transactional
    @Rollback
    public void testCreateTask() throws HTException {

        Task task = services.createTask("Task1", "ww2", "</request>");
        String taskDefinitionName = task.getTaskDefinition().getTaskName();

        LOG.info("Task name: " + taskDefinitionName);
        Assert.assertEquals("Task1", taskDefinitionName);
    }

//    @Test
//    @Transactional
//    @Rollback
//    public void testGetMyTasks() throws HumanTaskException {
//
//        Task t1 = services.createTask("ApproveClaim", "user", "request1");
//        Task t2 = services.createTask("ApproveClaim", "user", "request2");
//
//        //TODO replace with JPA
//        //sessionFactory.getCurrentSession().flush();
//        //sessionFactory.getCurrentSession().clear();
//        //taskDao.getJpaTemplate().flush();
//        
//        //List<Task> tasks = services.getMyTasks("user");
//
//        
//        for (Task task : tasks) {
//            LOG.info("Task: " + task);
//        }
//        
//        assertEquals(2, tasks.size());
//
//    }
    /**
     * TODO wcr - fails, what we test here? please describe in javadoc
     */
    @Test
    @Transactional
    @Rollback
    public void testGetMyTasksNoCreate() throws HTException {

        TaskMockery mockery = new TaskMockery(taskDao, assigneeDao);
        Task mockTask = mockery.getGoodTaskMock();

        mockery.assignOwner();

        List<Task> results = services.getMyTasks("Jacek", TaskTypes.ALL,
                GenericHumanRole.TASK_STAKEHOLDERS, null,
                Arrays.asList(Status.IN_PROGRESS, Status.OBSOLETE), null, null, null, 1, 0);

        Assert.assertEquals(1, results.size());

        Task taskToCheck = results.get(0);

        Assert.assertEquals(mockTask.getActualOwner(), taskToCheck.getActualOwner());

        //check with no statuses specified
        //TODO
        //results = services.getMyTasks("Jacek", TaskTypes.ALL,
        //      GenericHumanRole.TASK_STAKEHOLDERS, "admin", new ArrayList(), null, null, 1);
        results = services.getMyTasks("Jacek", TaskTypes.ALL, 
                GenericHumanRole.TASK_STAKEHOLDERS, null, new ArrayList<Status>(), null, null, null, 1, 0);

        Assert.assertEquals(1, results.size());

        //TODO check with notifications
        //results = services.getMyTasks("Jacek", TaskTypes.NOTIFICATIONS,
        //      GenericHumanRole.TASK_STAKEHOLDERS, "admin", new ArrayList(), null, null, 1);

        //Assert.assertEquals(0, results.size());

        mockery.assertIsSatisfied();
    }

//    @Test(expected=HTIllegalStateException.class)
//    @Transactional
//    @Rollback
//    public void testClaimByOwner() throws HTException {
//
//        TaskMockery mockery = new TaskMockery(taskDao, assigneeDao);
//        Task mockTask = mockery.getGoodTaskMock();
//
//        services.claimTask(mockTask.getId(), mockery.getPossibleOwner().getName());
//
//        services.claimTask(mockTask.getId(), mockTask.getActualOwner().getName());
//    }

    @Test
    @Transactional
    @Rollback
    /***
     *  This test should not claim the task becuase the owner was incorrect
     */
    public void testClaimNotOwner() throws HTException {

        TaskMockery mockery = new TaskMockery(taskDao, assigneeDao);
        Task mockTask = mockery.getGoodTaskMock();

        Throwable t = null;

        try {
            services.claimTask(mockTask.getId(), mockery.getImpossibleOwner().getName());
            Assert.fail();
        }catch(HTIllegalAccessException xRNA){
            //success
            t = xRNA;
        }

        Assert.assertNotNull("claim Task did not throw on impossible owner",t);

        Assert.assertEquals(Task.Status.READY, mockTask.getStatus());

        mockery.assertIsSatisfied();
    }

    @Test(expected=HTIllegalAccessException.class)
    @Transactional
    @Rollback
    public void testStartImpossibleOwner() throws HTException {

        TaskMockery mockery = new TaskMockery(taskDao, assigneeDao);
        Task mockTask = mockery.getGoodTaskMock();

        services.startTask(mockTask.getId(), mockery.getImpossibleOwner().getName());
    }

    @Test
    @Transactional
    @Rollback
    /***
     *  This test should not claim the task becuase the owner was incorrect
     */
    public void testDelegateNotOwner() throws HTException {

        TaskMockery mockery = new TaskMockery(taskDao, assigneeDao);
        Task mockTask = mockery.getGoodTaskMock();

        Throwable t = null;

        try {
            services.delegateTask(mockTask.getId(), mockery.getImpossibleOwner().getName());
            Assert.fail();
        }catch(HTIllegalAccessException xRNA){
            //success
            t = xRNA;
        }

        Assert.assertNotNull("claim Task did not throw on impossible owner",t);

        //FIXME:
        //Assert.assertEquals(Task.Status.RESERVED, mockTask.getStatus());

        mockery.assertIsSatisfied();
    }
    
//    @Test
//    @Transactional
//    @Rollback
//    public void testStartCorrectOwner() throws HTException {
//
//        TaskMockery mock = new TaskMockery(taskDao, assigneeDao);
//        Task mockTask = mock.getGoodTaskMock();
//
//        try {
//            services.startTask(mockTask.getId(), mock.getPossibleOwner().getName());
//        } catch (HTIllegalAccessException xIA) {
//            Assert.fail();
//        }
//    }

//     /**
//     *
//     * @throws HTException
//     */
//    @Test
//    @Transactional
//    @Rollback
//    public void testStartAfterClaim() throws HTException {
//
//        TaskMockery mockery = new TaskMockery(taskDao, assigneeDao);
//        Task mockTask = mockery.getGoodTaskMock(true);
//
//        try {
//            services.startTask(mockTask.getId(), mockery.getPossibleOwner().getName());
//        } catch (HTIllegalAccessException xIA) {
//            Assert.fail();
//        }
//
//        Assert.assertEquals(Task.Status.IN_PROGRESS, mockTask.getStatus());
//        
//    }
//
//    @Test
//    @Transactional
//    @Rollback
//    public void testReleaseAfterClaim() throws HTException {
//
//        TaskMockery mockery = new TaskMockery(taskDao, assigneeDao);
//        Task mockTask = mockery.getGoodTaskMock(true);
//
//        try {
//            services.startTask(mockTask.getId(), mockery.getPossibleOwner().getName());
//        } catch (HTIllegalAccessException xIA) {
//            Assert.fail();
//        }
//
//        Assert.assertEquals(Task.Status.IN_PROGRESS, mockTask.getStatus());
//
//        try {
//            services.releaseTask(mockTask.getId(), mockery.getPossibleOwner().getName());
//        } catch (HTIllegalAccessException xIA) {
//            Assert.fail();
//        }
//
//        Assert.assertEquals(Task.Status.READY, mockTask.getStatus());
//    }

    @Test
    @Transactional
    @Rollback
    public void testGetTaskInfo() throws HTException {

        TaskMockery mockery = new TaskMockery(taskDao, assigneeDao);
        Task mockTask = mockery.getGoodTaskMock();

        mockery.assignOwner();

        Task resultTask = services.getTaskInfo(mockTask.getId());

        Assert.assertNotNull(resultTask);

        Assert.assertEquals(mockTask.getActualOwner(), resultTask.getActualOwner());

        mockery.assertIsSatisfied();
    }

    @Test
    @Transactional
    @Rollback
    public void testReleaseTask() throws HTException {

        TaskMockery mockery = new TaskMockery(taskDao, assigneeDao);
        Task mockTask = mockery.getGoodTaskMock();

        mockery.assignOwner();

        services.releaseTask(mockTask.getId(),mockTask.getActualOwner().getName());

        Assert.assertEquals(Status.READY,mockTask.getStatus());

        mockery.assertIsSatisfied();
    }

}
