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
import pl.touk.humantask.exceptions.HTIllegalStateException;
import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.exceptions.RecipientNotAllowedException;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.model.TaskTypes;
import pl.touk.humantask.model.Task.Status;
import pl.touk.mock.TaskMockery;

/**
 * {@link Services} integration tests.
 *
 * @author Witek Wołejszo
 * @author Warren Crossing
 * @author Kamil Eisenbart
 * @author Piotr Jagielski
 * @author Mateusz Lipczyński
 */
@ContextConfiguration(locations = "classpath:/test.xml")
public class ServicesIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

    private final Log LOG = LogFactory.getLog(ServicesIntegrationTest.class);
    @Resource(name = "humanTaskServices")
    HumanTaskServicesInterface services;
    @Resource(name = "taskDao")
    TaskDao taskDao;
    @Resource(name = "assigneeDao")
    AssigneeDao assigneeDao;

    //@Resource(name = "hibernateSessionFactory")
    //SessionFactory sessionFactory;
    // ApplicationContext applicationContext;

    // @Override
    // protected String[] getConfigLocations() {
    // return new String[] { "classpath:test.xml" };
    // }

    // @Override
    // protected void setUp() throws Exception {
    //
    // super.setUp();
    // applicationContext = new ClassPathXmlApplicationContext(new String[] {
    // "services.xml", "quartz.xml", "test.xml" });
    //
    // }
    @Test
    @Transactional
    @Rollback
    public void testCreateTask() throws HumanTaskException {

        // Services services = (Services)
        // applicationContext.getBean("humanTaskServices");

        Task t = services.createTask("ApproveClaim", "ww", "request");

        String description = t.getTaskDefinition().getDescription("en-US", "text/plain", null);
        String key = t.getTaskDefinition().getTaskName();

        LOG.info("Task name: " + t.getTaskDefinition().getTaskName());
        LOG.info("Task description: " + description);
        Assert.assertTrue(description.contains("claim"));
        Assert.assertTrue(key.contains("Appr"));

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
    public void testGetMyTasksNoCreate() throws HumanTaskException {

        TaskMockery mock = new TaskMockery(taskDao, assigneeDao);
        Task mockTask = mock.getGoodTaskMock();

        mock.assignOwner();

        List<Task> results = services.getMyTasks("Jacek", TaskTypes.ALL,
                GenericHumanRole.TASK_STAKEHOLDERS, null,
                Arrays.asList(Status.IN_PROGRESS, Status.OBSOLETE), null, null, 1);

        Assert.assertEquals(1, results.size());

        Task taskToCheck = results.get(0);

        Assert.assertEquals(mockTask.getActualOwner(), taskToCheck.getActualOwner());

        //check with no statuses specified
        //TODO
        //results = services.getMyTasks("Jacek", TaskTypes.ALL,
        //      GenericHumanRole.TASK_STAKEHOLDERS, "admin", new ArrayList(), null, null, 1);
        results = services.getMyTasks("Jacek", TaskTypes.ALL, 
                GenericHumanRole.TASK_STAKEHOLDERS, null, new ArrayList<Status>(), null, null, 1);

        Assert.assertEquals(1, results.size());

        //TODO check with notifications
        //results = services.getMyTasks("Jacek", TaskTypes.NOTIFICATIONS,
        //      GenericHumanRole.TASK_STAKEHOLDERS, "admin", new ArrayList(), null, null, 1);

        //Assert.assertEquals(0, results.size());

        mock.assertIsSatisfied();
    }

    @Test
    @Transactional
    @Rollback
    public void testClaimOwner() throws HumanTaskException {

        TaskMockery mock = new TaskMockery(taskDao, assigneeDao);
        Task mockTask = mock.getGoodTaskMock();

        Task task = services.claimTask(mockTask, mock.getPossibleOwner().getName());

        Assert.assertNotNull(task);

        try {
            task = services.claimTask(mockTask, mock.getPossibleOwner().getName());
            Assert.fail();
        }catch(HTIllegalStateException xRNA){
            //sucess
        }

        mock.assertIsSatisfied();
    }

    @Test
    @Transactional
    @Rollback
    public void testClaimNotOwner() throws HumanTaskException {

        TaskMockery mock = new TaskMockery(taskDao, assigneeDao);
        Task mockTask = mock.getGoodTaskMock();
        
        try {
            Task task = services.claimTask(mockTask, mock.getImpossibleOwner().getName());
            Assert.fail();
        }catch(RecipientNotAllowedException xRNA){
            //sucess
        }

        mock.assertIsSatisfied();
    }

//    /**
//     * No exceptions expected.
//     * @throws HumanTaskException
//     */
//    @Test
//    @Transactional
//    @Rollback
//    public void testStartAndClaimTask() throws HumanTaskException {
//
//        Task t = services.createTask("ApproveClaim", "ww", "request");
//    
//        services.startTask(t, "kamil");
//        services.releaseTask(t, "kamil");
//        services.claimTask(t, "kamil");
//        services.startTask(t, "kamil");
//
//    }
    @Test
    @Transactional
    @Rollback
    public void testTaskLifecycle() throws HumanTaskException {

        //ApproveClaim has several potential owners, so it is not reserved
        Task t = services.createTask("ApproveClaim", "ww", "<?xml version='1.0'?><root/>");

        Assert.assertTrue(t.getStatus() == Task.Status.READY || t.getStatus() == Task.Status.CREATED || t.getStatus() == Task.Status.RESERVED);

    //TODO the rest of default lifecycle
    }

//    @Test
//    @Transactional
//    @Rollback
//    public void testActualOwnersStates() throws HumanTaskException {
//
//        Task task1 = services.createTask("ApproveClaim", "kamil", "request");
//        Task task2 = services.createTask("ApproveClaim", "witek", "request");
//        
//        LOG.info("Task status after create: " + task1.getStatus());
//        
//        Person person1 = assigneeDao.getPerson("kamil");
//        Person person2 = assigneeDao.getPerson("witek");
//
//        // creating task - staus CREATED
//
//        task1 = services.startTask(task1, "kamil");
//        task1 = services.stopTaskInProgress(task1);
//        task1 = services.releaseTask(task1, "kamil");
//
//        task1 = services.startTask(task1, "witek");
//        task1 = services.delegateTask(task1, "kamil");
//        task1 = services.startTask(task1, "kamil");
//        // t = services.forwardTask(t, p2);
//
//        task2 = services.startTask(task2, "kamil");
//
//        List<Task> tasks = new ArrayList<Task>();
//        tasks = services.getMyTasks("kamil",TaskTypes.ALL,null,null,null,null,null,null);
//        
//
//        // taking person from IAssigneeDao
//
//        LOG.info("Task status after set on ready: " + task1.getStatus());
//
//        // services.startTask(t, p);
//        services.releaseTask(task1, "kamil");
//        services.claimTask(task1, "kamil");
//
//        /*
//         * t = services.startTask(t, p); t = services.delegateTask(t, p2);
//         * 
//         * List<Assignee> lista = new ArrayList<Assignee>(); lista.add(p2); t =
//         * services.forwardTask(t, p, lista);
//         */
//
//    }

//    @Test
//    @Transactional
//    @Rollback
//    public void testGetOutput() throws HumanTaskException, IllegalArgumentFault, IllegalOperationFault, InterruptedException {
//
//        Task task = services.createTask("ApproveClaim", "kamil", "request");
//
//        services.startTask(task, "kamil");
//
//        services.setTaskOutput(task, "output", "new output", "kamil");
//
//        services.getOutput(task, "kamil");
//
//        services.deleteOutput(task);
//
//    }

//    //@Test
//    @Transactional
//    @Rollback
//    public void testSuspendUntil() throws HumanTaskException, InterruptedException {
//
//        Task task1 = services.createTask("ApproveClaim", "kamil", "request");
//        Task task2 = services.createTask("ApproveClaim", "kamil", "request");
//
//        services.suspendUntilPeriod(task1, 2000);
//        services.suspendUntil(task2, new Date(2000));
//
//        synchronized (this) {
//            this.wait(3000);
//        }
//
//        task1 = taskDao.fetch(task1.getId());
//        assertEquals(task1.getStatus(), Status.READY);
//
//        task2 = services.loadTask(task2.getId());
//        assertEquals(task2.getStatus(), Status.READY);
//
//    }
}
