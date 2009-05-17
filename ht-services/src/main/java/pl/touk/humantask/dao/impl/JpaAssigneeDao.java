/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.dao.impl;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import pl.touk.humantask.dao.AssigneeDao;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Group;
import pl.touk.humantask.model.Person;

/**
 * Implements basic JPA DAO for {@link Assignee} and convenience search
 * methods.
 *
 * @author Witek Wołejszo
 */
@Repository
public class JpaAssigneeDao implements AssigneeDao {
    
    private final Log log = LogFactory.getLog(AssigneeDao.class);

    @PersistenceContext(name = "TOUK-WSHT-PU")
    protected EntityManager entityManager;
    
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;
    
    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public Assignee fetch(Long id) {
        return entityManager.find(Assignee.class, id);
    }
    
    /**
     * {@inheritDoc}
     */
    public void update(Assignee entity) {
        entityManager.merge(entity);
    }
    
    /**
     * {@inheritDoc}
     */
    public void create(Assignee entity) {
        entityManager.persist(entity);
    }

    /**
     * {@inheritDoc}
     */
    public Group getGroup(String name) {
        
        Query query = entityManager.createQuery("SELECT g FROM Group g WHERE g.name = :name");
        query.setParameter("name", name);
        
        try {
            
            return (Group) query.getSingleResult();
        } catch (NoResultException e) {
            
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * TODO should work when ids are set
     */
    public Set<Assignee> saveNotExistingAssignees(Set<Assignee> assignees) {
        
        Set<Assignee> result = new HashSet<Assignee>();
        
        for (Assignee a : assignees) {

            if (a instanceof Person) {

                Person p = this.getPerson(a.getName());
                if (p == null) {
                    this.create(a);
                    p = (Person) a;
                }
                result.add(p);
               
            } else if (a instanceof Group) {
                
                Group g = this.getGroup(a.getName());
                if (g == null) {
                    this.create(a);
                    g = (Group) a;
                }
                result.add(g);      
            }
        }
        
        log.debug("retrieveExistingAssignees " + assignees.size() + " -> " + result.size() );
        return result;
    }
    
//    public EntityManagerFactory getEntityManagerFactory() {
//        return this.entityManagerFactory;
//    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

}
