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
 * Holds start or completion deadline information.
 * 
 * @author Witek Wołejszo
 * 
 */
@Entity(name = "DEADLINE")
public class Deadline extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
