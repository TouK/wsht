/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */
package pl.touk.humantask.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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

    private String value;

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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
