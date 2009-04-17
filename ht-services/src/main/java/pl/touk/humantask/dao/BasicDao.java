/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.dao;

import java.io.Serializable;

import pl.touk.humantask.model.Base;

/**
 * Basic DAO operations for domain objects extending {@link Base}.
 * @author Witek Wołejszo
 */
public interface BasicDao<T extends Base, ID extends Serializable> {

    /**
     * Retrieves domain object from persistent store.
     * @param id Identifier of the object requested
     * @return requested domain object
     */
    T fetch(ID id);

    /**
     * Saves domain object in persistent store.
     * @param entity Domain object to be updated
     */
    void update(T entity);

    /**
     * Creates domain object in persistent store.
     * @param entity Domain object to be created
     */
    void create(T entity);

}
