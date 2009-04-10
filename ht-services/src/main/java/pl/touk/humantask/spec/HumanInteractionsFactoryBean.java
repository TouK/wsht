/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.spec;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.example.ws_ht.THumanInteractions;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;

/**
 * 
 * @author witek
 */
public class HumanInteractionsFactoryBean implements FactoryBean {

    private final Log log = LogFactory.getLog(HumanInteractionsFactoryBean.class);

    Resource htdXml;

    public Resource getHtdXml() {
        return htdXml;
    }

    public void setHtdXml(Resource htdXml) {
        this.htdXml = htdXml;
    }

    public Object getObject() throws Exception {

        JAXBContext jc = JAXBContext.newInstance("org.example.ws_ht");
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        // jaxb
        InputStream is = htdXml.getInputStream();
        THumanInteractions hiDoc = ((JAXBElement<THumanInteractions>) unmarshaller.unmarshal(is)).getValue();
        String md5;

        // dom
        is = htdXml.getInputStream();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(is);

        is = htdXml.getInputStream();

        MessageDigest digest = MessageDigest.getInstance("MD5");

        byte[] buffer = new byte[8192];
        int read = 0;
        try {

            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            md5 = bigInt.toString(16);

        } catch (IOException e) {

            log.error("Error reading: " + htdXml);
            throw new RuntimeException("Error reading: " + htdXml, e);

        } finally {

            try {
                is.close();
            } catch (IOException e) {
                log.error("Error closing: " + htdXml);
                throw new RuntimeException("Error closing: " + htdXml, e);
            }

        }

        HumanInteractions hi = new HumanInteractions(hiDoc, document, md5);

        return hi;
    }

    public Class getObjectType() {
        log.debug("getObjectType");
        return HumanInteractions.class;
    }

    public boolean isSingleton() {
        log.debug("isSingleton");
        return true;
    }

}
