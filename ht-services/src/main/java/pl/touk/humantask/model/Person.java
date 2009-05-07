/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import javax.persistence.Entity;

import org.apache.commons.lang.builder.EqualsBuilder;

@Entity
public class Person extends Assignee {

    public Person() {
        super();
    }

    public Person(String name) {
        this.name = name;
    }

    /**
     * Returns the person hashcode.
     * @return person hash code
     */
    @Override
    public int hashCode() {
        return ((name == null) ? 0 : name.hashCode());
    }

    /**
     * Checks whether the person is equal to another object.
     * @param obj object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Person == false) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Person rhs = (Person) obj;
        return new EqualsBuilder().append(name, rhs.name).isEquals();
    }

}
