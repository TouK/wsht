/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.dao;

import java.io.Serializable;

import org.springframework.stereotype.Repository;

import pl.touk.humantask.model.Base;

/**
 * 
 *
 * @author Witek Wołejszo
 */
public interface BasicDao<T extends Base, ID extends Serializable> {

    public T fetch(ID id);
    
    public void update(T entity);
    
    public void create(T entity);

}
