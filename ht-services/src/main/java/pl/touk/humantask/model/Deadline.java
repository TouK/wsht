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
@Entity
public class Deadline {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

}
