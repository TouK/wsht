/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Holds start or completion deadline information.
 * 
 * @author Witek Wołejszo
 * 
 */
@Entity
@Table(name = "DEADLINE")
public class Deadline extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ddln_seq")
    @SequenceGenerator(name = "ddln_seq", sequenceName = "ddln_seq")
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
