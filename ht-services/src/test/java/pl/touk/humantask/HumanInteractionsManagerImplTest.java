/*
 * Copyright (c) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import pl.touk.humantask.model.htd.THumanInteractions;

public class HumanInteractionsManagerImplTest {

    //private final Log log = LogFactory.getLog(HumanInteractionsManagerImplTest.class);
    
    @Test
    public void testUnmarshallHumanInteractionsData() throws JAXBException, IOException {
        Resource htdXml = new ClassPathResource("testHtd1.xml");
        HumanInteractionsManagerImpl humanInteractionsManagerImpl = new HumanInteractionsManagerImpl();
        THumanInteractions hi = humanInteractionsManagerImpl.unmarshallHumanInteractionsData(htdXml);
        Assert.assertNotNull(hi);
    }

}
