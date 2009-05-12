/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Message part related to Task. It can be either part of input message or part of output message.
 * 
 * @author Witek Wołejszo
 */
@Entity
@Table(name = "MESSAGE")
public class Message extends Base {
    
    //public static final String  DEFAULT_PART_NAME_KEY = "WSHT_DEFAULT_PART_NAME_KEY";
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String partName;

    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    @Lob
    private String message;
    
    @Transient
    private Document messageDocument;

    /**
     * Constructs Message.
     * @param message
     */
    public Message(String message) {
        super();
        this.message = message;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
    
    /**
     * Returns {@link InputStream} with message contents using platform encoding. 
     * @return
     */
    private InputStream getMessageInputStream() {
        return new ByteArrayInputStream(message.getBytes());
    }
    
    /**
     * Returns DOM Document with parsed message part.
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public Document getDomDocument() throws ParserConfigurationException, SAXException, IOException {
        
        if (messageDocument == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.messageDocument = builder.parse(this.getMessageInputStream());
        }
        
        return this.messageDocument;
    }
    
    /**
     * Retruns root element name.
     * TODO exceptions???
     * @return the root element name
     */
    public String getRootNodeName() {
        
        try {
            
            return this.getDomDocument().getDocumentElement().getNodeName();
            
        } catch (ParserConfigurationException e) {
            
        } catch (SAXException e) {
            
        } catch (IOException e) {
            
        }
        
        return null;
    }

    /***************************************************************
     * Infrastructure methods. *
     ***************************************************************/

    /**
     * Returns the message hashcode.
     * @return message hash code
     */
    @Override
    public int hashCode() {
        int result = ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * Checks whether the message is equal to another object.
     * @param obj object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Message == false) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Message m = (Message) obj;
        return new EqualsBuilder().append(id, m.id).isEquals();
    }

}
