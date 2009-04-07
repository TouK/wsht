package pl.touk.humantask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.example.ws_ht.api.wsdl.IllegalArgumentFault;
import org.example.ws_ht.api.wsdl.IllegalOperationFault;

import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import pl.touk.humantask.dao.impl.HibernateAssigneeDao;
import pl.touk.humantask.dao.impl.HibernateTaskDao;
import pl.touk.humantask.model.Person;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.model.Task.Status;

@ContextConfiguration(locations = "classpath:/test.xml")
public class ServicesIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

    private static final Log LOG = LogFactory.getLog(ServicesIntegrationTest.class);

    @Resource(name = "humanTaskServices")
    Services services;
    
    @Resource(name = "taskDao")
    HibernateTaskDao taskDao;

    @Resource(name = "assigneeDao")
    HibernateAssigneeDao assigneeDao;
    
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

        LOG.info("Task key: " + t.getTaskDefinition().getKey());
        LOG.info("Task description: " + t.getTaskDefinition().getDescription("en-US", "text/plain"));

    }

    @Test
    @Transactional
    @Rollback
    public void testGetMyTasks() throws HumanTaskException {

        // Services services = (Services)
        // applicationContext.getBean("humanTaskServices");

        //IAssigneeDao assigneeDao = (IAssigneeDao) applicationContext.getBean("assigneeDao");

        Task t1 = services.createTask("ApproveClaim", "ww", "request");
        services.claimTask(t1, "witek");

        Task t2 = services.createTask("ApproveClaim", "ww", "request");
        //services.claimTask(t2, "kamil");

//        Task t3 = services.createTask("ApproveClaim", "ww", "request");
//        services.claimTask(t3, "witek");

        List<Task> tasks = services.getMyTasks("witek");

        for (Task task : tasks) {
            LOG.info("Task: " + task);
        }

    }

    /**
     * No exceptions expected.
     * @throws HumanTaskException
     */
    @Test
    @Transactional
    @Rollback
    public void testStartAndClaimTask() throws HumanTaskException {

        Task t = services.createTask("ApproveClaim", "ww", "request");
    
        services.startTask(t, "kamil");
        services.releaseTask(t, "kamil");
        services.claimTask(t, "kamil");
        services.startTask(t, "kamil");

    }

    @Test
    @Transactional
    @Rollback
    public void testTaskLifecycle() throws HumanTaskException {

        //ApproveClaim has several potential owners, so it is not reserved
        Task t = services.createTask("ApproveClaim", "ww", "request");
        Assert.assertEquals(Task.Status.READY, t.getStatus());

        //TODO the rest of default lifecycle
    }

    @Test
    @Transactional
    @Rollback
    public void testActualOwnersStates() throws HumanTaskException {

        Task task1 = services.createTask("ApproveClaim", "kamil", "request");
        Task task2 = services.createTask("ApproveClaim", "witek", "request");
        
        LOG.info("Task status after create: " + task1.getStatus());
        
        Person person1 = assigneeDao.getPerson("kamil");
        Person person2 = assigneeDao.getPerson("witek");

        // creating task - staus CREATED

        task1 = services.startTask(task1, "kamil");
        task1 = services.stopTaskInProgress(task1);
        task1 = services.releaseTask(task1, "kamil");

        task1 = services.startTask(task1, "witek");
        task1 = services.delegateTask(task1, "kamil");
        task1 = services.startTask(task1, "kamil");
        // t = services.forwardTask(t, p2);

        task2 = services.startTask(task2, "kamil");

        List<Task> tasks = new ArrayList<Task>();
        tasks = services.getMyTasks("kamil");

        // taking person from IAssigneeDao

        LOG.info("Task status after set on ready: " + task1.getStatus());

        // services.startTask(t, p);
        services.releaseTask(task1, "kamil");
        services.claimTask(task1, "kamil");

        /*
         * t = services.startTask(t, p); t = services.delegateTask(t, p2);
         * 
         * List<Assignee> lista = new ArrayList<Assignee>(); lista.add(p2); t =
         * services.forwardTask(t, p, lista);
         */

    }

    @Test
    @Transactional
    @Rollback
    public void testGetOutput() throws HumanTaskException, IllegalArgumentFault, IllegalOperationFault, InterruptedException {

        Task task = services.createTask("ApproveClaim", "kamil", "request");

        services.startTask(task, "kamil");

        services.setTaskOutput(task, "output", "new output", "kamil");

        services.getOutput(task, "kamil");

        services.deleteOutput(task);

    }

    //@Test
    @Transactional
    @Rollback
    public void testSuspendUntil() throws HumanTaskException, InterruptedException {

        Task task1 = services.createTask("ApproveClaim", "kamil", "request");
        Task task2 = services.createTask("ApproveClaim", "kamil", "request");

        services.suspendUntilPeriod(task1, 2000);
        services.suspendUntil(task2, new Date(2000));

        synchronized (this) {
            this.wait(3000);
        }

        task1 = taskDao.fetch(task1.getId());
        Assert.assertEquals(task1.getStatus(), Status.READY);

        task2 = services.loadTask(task2.getId());
        Assert.assertEquals(task2.getStatus(), Status.READY);

    }

}
