/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * TODO what is group?
 *
 * @author Witek Wołejszo
 * @author Kamil Eisenbart
 */
@Entity
public class Group extends Assignee {

    @ManyToMany
    public List<Person> people;

    @Column(unique = true)
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Group() {
        this.people = new ArrayList<Person>();
    }

    public List<Person> getPeople() {
        return people;
    }

    public void setPeople(List<Person> people) {
        this.people = people;
    }
    
    @Override
    public int hashCode() {
        int result = ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Task == false) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Group rhs = (Group) obj;
        return new EqualsBuilder().append(name, rhs.name).isEquals();
    }

}
