package pl.touk.humantask.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

import com.trg.dao.dao.original.GenericDAOImpl;

import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Person;
import pl.touk.humantask.model.Task;

/**
 * Implements simple hibernate dao for Task {@link Task} and convenience search
 * methods.
 * 
 * @author Witek Wołejszo
 */
public class HibernateTaskDao extends GenericDAOImpl<Task, Long> {

    /**
     * Returns tasks currently owned by specified person.
     * @param owner
     * @return
     */
    public List<Task> getTasks(Assignee owner) {

        Criteria criteria = getSession().createCriteria(Task.class);
        criteria.add(Expression.eq("actualOwner", owner));
        criteria.addOrder(Order.asc("activationTime"));

        return criteria.list();
    }

    /*
     * (non-Javadoc)
     * 
     * @see pl.touk.humantask.dao.ITaskDao#getTasks(java.lang.String,
     * java.util.List)
     */
    public List<Task> getTasks(Assignee owner, List<Task.Status> statuses) {

        Criteria criteria = getSession().createCriteria(Task.class);
        criteria.add(Expression.eq("actualOwner", owner));
        criteria.add(Expression.in("status", statuses));
        criteria.addOrder(Order.asc("activationTime"));
        
        return criteria.list();
    }

    public List<Task> getTasksToResume(Task.Status status, Date date) {

        Criteria criteria = getSession().createCriteria(Task.class);
        criteria.add(Expression.eq("status", status));
        
        return criteria.list();
    }

}
