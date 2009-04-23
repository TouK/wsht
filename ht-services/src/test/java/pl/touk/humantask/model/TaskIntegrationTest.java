package pl.touk.humantask.model;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;


/**
 * {@link Task} integration tests. Tests compile time weaving.
 *
 * @author Witek Wołejszo
 */
@ContextConfiguration(locations = "classpath:/test.xml")
public class TaskIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Test
    public void testTaksConstructor() {
        Task task = new Task();
        assertNotNull(task.humanInteractionsManager);
    }
    
}
