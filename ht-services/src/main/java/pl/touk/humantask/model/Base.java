/*
 * Copyright (c) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import javax.persistence.MappedSuperclass;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Base class for WS-HumanTask domain model. Implements common methods like: toString, equals, hashCode. Class
 * is {@link Configurable} and expects container to initialise some of its values.
 * 
 * @author Witold Wołejszo
 */
@Configurable
@MappedSuperclass
public abstract class Base {

    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
    }

    @Override
    public abstract int hashCode();
    
    @Override
    public abstract boolean equals(Object obj);
}
