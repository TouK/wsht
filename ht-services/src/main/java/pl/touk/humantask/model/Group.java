/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Unresolvd group of people.
 *
 * @author Witek Wołejszo
 * @author Kamil Eisenbart
 * @author Mateusz Lipczyński
 */
@Entity
public class Group extends Assignee {

    @Column(unique = true)
    private String name;

    /**
     * Unresolved group of people constructor.
     */
    public Group() {
        super();
    }

    /**
     * Unresolved group of people constructor.
     * @param name          group name
     */
    Group(String name) {
        super();
        this.setName(name);
    }

    /***************************************************************
     * Getters & Setters *
     ***************************************************************/
    
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return ((name == null) ? 0 : name.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Group == false) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Group rhs = (Group) obj;
        return new EqualsBuilder().append(name, rhs.name).isEquals();
    }

}
