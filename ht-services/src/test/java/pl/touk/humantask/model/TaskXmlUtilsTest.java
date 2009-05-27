/*
 * Copyright (c) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;

import org.junit.Test;

public class TaskXmlUtilsTest {

    @Test
    public void testEvaluateXPath() {
        
        String xmlRequest = "<enterOrder xmlns:sch='http://www.wsht/wsht/schema' orderNumber='O26195' caseNumber='C81794' caseType='1' suggestedOwner='1' submitter='1' source='1' issueDate='1' priority='1' note='Niesłychanie pilne. Proszę się pośpieszyć.'>" +
                            "    <sch:correctiveInvoice customerId='1' customerCode='KLIENT_27959' correctedInvoiceNumber='1' correctionAmount='353.78' issueReason='1'>" +
                            "        <sch:correctiveInvoiceItem name='Usługi telekomunikacyjne.' newNetValue='424.68' newVat='93.4296' newVatRate='22'/>" +
                            "        <sch:correctiveInvoiceItem name='Usługi telekomunikacyjne.' newNetValue='1' newVat='0.22' newVatRate='22'/>" +
                            "    </sch:correctiveInvoice>" +
                            "</enterOrder>";

        Map<String, Message> input = new HashMap<String, Message>();
        input.put("enterOrder", new Message(xmlRequest));
        
        TaskXmlUtils txu = new TaskXmlUtils(new NamespaceContext() {

            public String getNamespaceURI(String prefix) {
                
                if (prefix.equals("wsht")) {
                    return "http://www.wsht/wsht/schema";
                }
                if (prefix.equals("htd")) {
                    return "http://www.example.org/WS-HT";
                }
                
                return null;
            }

            public String getPrefix(String namespaceURI) {
                return null;
            }

            public Iterator getPrefixes(String namespaceURI) {
                return null;
            }
            
        }, input, null);
        
        Object o = txu.evaluateXPath("htd:getInput(\"enterOrder\")/wsht:correctiveInvoice/@customerId", XPathConstants.STRING);
        
        assertNotNull(o);
        assertEquals("1", o);
    }
    
}
