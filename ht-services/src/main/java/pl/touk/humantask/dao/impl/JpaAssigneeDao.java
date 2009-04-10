/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.dao.impl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import pl.touk.humantask.dao.AssigneeDao;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Person;

/**
 * 
 *
 * @author Witek Wołejszo
 */
@Repository
public class JpaAssigneeDao implements AssigneeDao {

    @PersistenceContext(name = "TOUK-WSHT-PU")
    protected EntityManager entityManager;
    
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;
    
    public Assignee fetch(Long id) {
        return entityManager.find(Assignee.class, id);
    }
    
    public void update(Assignee entity) {
        entityManager.merge(entity);
    }
    
    public void create(Assignee entity) {
        entityManager.persist(entity);
    }
    
    public Person getPerson(String name) {

        Query query = entityManager.createQuery("SELECT p FROM Person p WHERE p.name = :name");
        query.setParameter("name", name);
        
        try {
            
            return (Person) query.getSingleResult();
        } catch (NoResultException e) {
            
            return null;
        }
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

}
