/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * Task Assignee - member of generic human role.
 * @author Witek Wołejszo
 */
@Entity
@Table(name = "ASSIGNEE")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Assignee extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }
    
    public abstract String getName();

    public abstract void setName(String name);

}
