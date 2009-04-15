/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import pl.touk.humantask.Services;
import pl.touk.humantask.dao.TaskDao;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Person;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.model.TaskTypes;
import pl.touk.humantask.model.Task.Status;

/**
 * Implements basic JPA DAO for Task {@link Task} and convenience search
 * methods.
 * 
 * @author Witek Wołejszo
 * @author Warren Crossing
 */
@Repository
public class JpaTaskDao implements TaskDao {

    @PersistenceContext(name = "TOUK-WSHT-PU")
    protected EntityManager entityManager;
    
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;
    
    /**
     * Returns all {@link Task}s currenty owned by specifed {@link Person}.
     * 
     * @param   owner the owner's name
     * @return  list of {@link Task}s
     */
    public List<Task> getTasks(Person owner) {

        Query query = entityManager.createQuery("SELECT t FROM Task t WHERE t.actualOwner = :owner");
        query.setParameter("owner", owner);
        return query.getResultList();
    }

    /**
     * Returns tasks. See {@link HumanTaskServicesInterface#getMyTasks(String, TaskTypes, GenericHumanRole, String, List, String, String, Integer)}
     * for method contract.
     * 
     * @param owner
     * @param taskType
     * @param genericHumanRole
     * @param workQueue
     * @param status
     * @param whereClause
     * @param createdOnClause
     * @param maxTasks
     * @return
     */
    public List<Task> getTasks(Assignee owner, TaskTypes taskType, GenericHumanRole genericHumanRole, String workQueue, List<Status> statuses,
            String whereClause, String createdOnClause, Integer maxTasks) {

        Map<String, Object> namedParameters = new HashMap<String, Object>();
        
        StringBuilder queryBuilder = new StringBuilder("SELECT t FROM Task t ");
        
        //query for human role must be first
        
        if (genericHumanRole != null) {
            
            switch (genericHumanRole) {
            
                //TODO: owner for now but should be workQueue 
                
                case ACTUAL_OWNER:
                    //duplicates statement set when workQueue is null - not an error 
                    queryBuilder.append("WHERE t.actualOwner = :owner AND ");
                    namedParameters.put("owner", owner);
                    break;
                    
                case BUSINESS_ADMINISTRATORS:
                    //TODO implement
                    throw new UnsupportedOperationException("Query for BUSINESS_ADMINISTRATORS not implemented.");
                    //break;
                    
                case NOTIFICATION_RECIPIENTS:
                    queryBuilder.append("JOIN t.notificationRecipients ghr WHERE ghr = :owner AND ");
                    namedParameters.put("owner", owner);
                    break;
                    
                case POTENTIAL_OWNERS:
                    queryBuilder.append("JOIN t.potentialOwners ghr WHERE ghr = :owner AND ");
                    namedParameters.put("owner", owner);
                    break;
                    
                case TASK_INITIATOR:
                    //TODO implement
                    throw new UnsupportedOperationException("Query for TASK_INITIATOR not implemented.");
                    //break;
                    
                case TASK_STAKEHOLDERS:
                    queryBuilder.append("JOIN t.taskStakeholders ghr WHERE ghr = :owner AND ");
                    namedParameters.put("owner", owner);
                    break;
                    
                default:
            }
         
        } else {
            
            queryBuilder.append("WHERE ");
        }

        if (workQueue == null) {
            
            //workQueue = owner.toString();
            //TODO ACTUAL_OWNER.toString() ?
            queryBuilder.append("t.actualOwner = :actualOwner AND ");
            namedParameters.put("actualOwner", owner);
        }
        
        if (statuses != null && !statuses.isEmpty()) {
            
            queryBuilder.append("t.status in (:statuses) AND ");
            namedParameters.put("statuses", statuses);
        }
        
        if (createdOnClause != null) {
            
            queryBuilder.append("t.createdOn = :createdOnClause AND ");
            namedParameters.put("createdOn", createdOnClause);
        }
        
        if (whereClause != null) {
            
            queryBuilder.append(whereClause);
            queryBuilder.append(" AND ");
        }        

        String queryString = queryBuilder.toString();
        
        if (queryString.endsWith(" AND ")) {
            queryString = queryString.substring(0, queryString.length() - 4);
        }

        if (queryString.endsWith(" WHERE ")) {
            queryString = queryString.substring(0, queryString.length() - 6);
        }
        
        queryBuilder = new StringBuilder(queryString);
        queryBuilder.append(" ORDER BY t.activationTime ");
        
        queryString = queryBuilder.toString();
        
        Query query = entityManager.createQuery(queryString);
        
        if (maxTasks != null && maxTasks > 0) {
            query.setFirstResult(0);
            query.setMaxResults(maxTasks);
        }
        
        for (Map.Entry<String, Object> parameter : namedParameters.entrySet()) {
            query.setParameter(parameter.getKey(), parameter.getValue());
        }
        
        return query.getResultList();
    }

    public boolean exists(Long id) {
        try{
            entityManager.find(Task.class,id);
            return true;
        }catch(EntityNotFoundException xENF) {
            return false;
        }
    }
    /**
     * Retrieves domain object from persistent store.
     * @param id
     * @return
     */
    public Task fetch(Long id) {
        return entityManager.find(Task.class, id);
    }
    
    /**
     * Saves domain object in persistent store. 
     * @param entity
     */
    public void update(Task entity) {
        entityManager.merge(entity);
    }
    
    /**
     * Creates domain object in persistent store. 
     * @param entity
     */
    public void create(Task entity) {
        entityManager.persist(entity);
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }
    
}
