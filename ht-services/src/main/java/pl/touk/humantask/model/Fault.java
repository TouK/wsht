/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Fault.
 * @author Witek Wołejszo
 */
@Embeddable
public class Fault {

    /**
     * Logger.
     */
    @Transient
    private final Log log = LogFactory.getLog(Fault.class);
    
    /**
     * Fault name.
     */
    @Column(name = "fault_name", nullable = true)
    private String name;
    
    /**
     * Fault data.
     */
    @Column(name = "fault_data", nullable = true)
    private String data;
    
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    /***************************************************************
     * Infrastructure methods.                                     *
     ***************************************************************/

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", this.name).append("data", this.data).toString();
    }

}
