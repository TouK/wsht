/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * {@link Message} tests.
 * @author Witek Wołejszo
 */
public class MessageTest {

    @Test
    public void testGetDomDocument() throws ParserConfigurationException, SAXException, IOException {
        Message message = new Message("<ClaimApprovalRequest><cust><firstname>witek</firstname></cust></ClaimApprovalRequest>");
        Document doc = message.getDomDocument();
        assertNotNull(doc);
    }

    @Test
    public void testGetRootNodeName() {
        Message message = new Message("<ClaimApprovalRequest><cust><firstname>witek</firstname></cust></ClaimApprovalRequest>");
        String r = message.getRootNodeName();
        assertEquals("ClaimApprovalRequest", r);
    }
    
    @Test
    public void testGetRootNodeName_XML_WithProcessingInstruction() {
        Message message = new Message("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ClaimApprovalRequest><cust><firstname>witek</firstname></cust></ClaimApprovalRequest>");
        String r = message.getRootNodeName();
        assertEquals("ClaimApprovalRequest", r);
    }

}
