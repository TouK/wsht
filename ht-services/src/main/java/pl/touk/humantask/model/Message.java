/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Message part related to Task. It can be either part of input message or part of output message.
 * 
 * @author Witek Wołejszo
 */
@Entity
@Table(name = "MESSAGE")
public class Message extends Base {
    
    public static final String  DEFAULT_PART_NAME_KEY = "WSHT_DEFAULT_PART_NAME_KEY";
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String partName;

    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    @Lob
    private String message;

    /**
     * TODO ww
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
