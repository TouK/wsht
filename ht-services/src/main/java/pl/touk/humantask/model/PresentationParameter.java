/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */
package pl.touk.humantask.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Evaluated value of Task's presentation parameter.
 * @author Witek Wołejszo
 */
@Entity
@Table(name = "PRESENTATION_PARAMETERS")
public class PresentationParameter extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String name;

    private String stringValue;
    
    @Temporal(TemporalType.DATE)
    private Date dateValue;
    
    private BigDecimal numericValue;
    
    private Boolean booleanValue;
    
    /**
     * The invoice of this item
     */
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    /**
     * @param stringValue the stringValue to set
     */
    public void setValue(String stringValue) {
        cleanValues();
        this.stringValue = stringValue;
    }

    /**
     * @param dateValue the dateValue to set
     */
    public void setValue(Date dateValue) {
        cleanValues();
        this.dateValue = dateValue;
    }
    
    /**
     * @param numericValue the numericValue to set
     */
    public void setValue(BigDecimal numericValue) {
        cleanValues();
        this.numericValue = numericValue;
    }
    
    /**
     * @param booleanValue the booleanValue to set
     */
    public void setValue(Boolean booleanValue) {
        cleanValues();
        this.booleanValue = booleanValue;
    }
    
    /**
     * Returns value of proper type.
     * @return value
     */
    public Object getValue() {
        if (this.stringValue != null) {
        
            return this.stringValue;
        
        } else if (this.dateValue != null) {

            return this.dateValue;
        
        } else if (this.numericValue != null) {
            
            return this.numericValue;
            
        } else if (this.booleanValue != null) {
            
            return this.booleanValue;
            
        } else {
            
            return null;
        }
    }
    
    /**
     * @param task the task to set
     */
    public void setTask(Task task) {
        this.task = task;
    }


    private void cleanValues() {
        
        this.stringValue = null;
        this.dateValue = null;
        this.numericValue = null;
        this.booleanValue = null;        
    }
    
    /**
     * Returns the presentation element hash code.
     * @return presentation element hash code
     */
    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        return hcb.append(this.id).toHashCode();
    }

    /**
     * Checks whether the presentation element is equal to another object.
     * @param obj object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PresentationParameter == false) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        PresentationParameter pp = (PresentationParameter) obj;
        return new EqualsBuilder().append(id, pp.id).isEquals();
    }

}
