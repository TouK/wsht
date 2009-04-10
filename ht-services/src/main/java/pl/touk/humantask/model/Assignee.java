/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Witek Wołejszo
 */
@Entity
public abstract class Assignee extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public int hashCode() {

        if (getId() == null) {
            return super.hashCode();
        }

        return getId().hashCode();

    }

    @Override
    public boolean equals(Object obj) {

        if (getId() == null) {
            return super.equals(obj);
        }

        if (obj == null) {
            return false;
        }

        return getId().equals(((Assignee) obj).getId());

    }

}
