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
 * Implements basic JPA DAO for {@link Assignee} and convenience search
 * methods.
 *
 * @author Witek Wołejszo
 */
@Repository
public class JpaAssigneeDao implements AssigneeDao {

    @PersistenceContext(name = "TOUK-WSHT-PU")
    protected EntityManager entityManager;
    
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;
    
    /**
     * Returns {@link Person} by name.
     * 
     * @param name the name of a person.
     * @return the {@link Person} with specified name or null if no {@link Person} can be found
     */
    public Person getPerson(String name) {

        Query query = entityManager.createQuery("SELECT p FROM Person p WHERE p.name = :name");
        query.setParameter("name", name);
        
        try {
            
            return (Person) query.getSingleResult();
        } catch (NoResultException e) {
            
            return null;
        }
    }

    /**
     * Retrieves domain object from persistent store.
     * @param id Identifier of requested domain object
     * @return fetched object
     */
    public Assignee fetch(Long id) {
        return entityManager.find(Assignee.class, id);
    }
    
    /**
     * Saves domain object in persistent store. 
     * @param entity Domain object to save
     */
    public void update(Assignee entity) {
        entityManager.merge(entity);
    }
    
    /**
     * Creates domain object in persistent store. 
     * @param entity Domain object to create
     */
    public void create(Assignee entity) {
        entityManager.persist(entity);
    }
    
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

}
