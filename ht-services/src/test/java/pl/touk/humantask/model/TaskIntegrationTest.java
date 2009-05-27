package pl.touk.humantask.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.annotation.Resource;
import javax.xml.xpath.XPathConstants;

import org.junit.Assume;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import pl.touk.humantask.HumanInteractionsManager;
import pl.touk.humantask.exceptions.HTException;


/**
 * {@link Task} integration tests. Tests compile time weaving.
 *
 * @author Witek Wołejszo
 */
@ContextConfiguration(locations = "classpath:/test.xml")
public class TaskIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Resource
    HumanInteractionsManager humanInteractionsManager;
    
    @Test
    public void testTaskConstructor() {
        Task task = new Task();
        assertNotNull(task.humanInteractionsManager);
    }
    
    //@Test
    public void testEvaluateXPathGetInput_Namespace() throws HTException {
        
        Assume.assumeNotNull(this.humanInteractionsManager);
        
        String xmlRequest = "<enterOrder xmlns:sch='http://www.wsht/wsht/schema' orderNumber='O26195' caseNumber='C81794' caseType='1' suggestedOwner='1' submitter='1' source='1' issueDate='1' priority='1' note='Niesłychanie pilne. Proszę się pośpieszyć.'>" +
                            "    <sch:correctiveInvoice customerId='1' customerCode='KLIENT_27959' correctedInvoiceNumber='1' correctionAmount='353.78' issueReason='1'>" +
                            "        <sch:correctiveInvoiceItem name='Usługi telekomunikacyjne.' newNetValue='424.68' newVat='93.4296' newVatRate='22'/>" +
                            "        <sch:correctiveInvoiceItem name='Usługi telekomunikacyjne.' newNetValue='1' newVat='0.22' newVatRate='22'/>" +
                            "    </sch:correctiveInvoice>" +
                            "</enterOrder>";

        Task t = new Task(this.humanInteractionsManager.getTaskDefinition("Task1"), null, xmlRequest);
        Assume.assumeNotNull(t.humanInteractionsManager);
        
        t.getInput().put("enterOrder", new Message(xmlRequest));

        Object o = t.evaluateXPath("htd:getInput(\"enterOrder\")/wsht:correctiveInvoice/@customerId", XPathConstants.STRING);

        assertNotNull(o);
        assertEquals("1", o);
    }
}
