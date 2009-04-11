/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.apache.commons.lang.builder.EqualsBuilder;

@Entity
public class Person extends Assignee {

    @Column(unique = true)
    private String name;

    public Person() {
        super();
    }

    public Person(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        int result = ((name == null) ? 0 : name.hashCode());
        return result;
    }

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
