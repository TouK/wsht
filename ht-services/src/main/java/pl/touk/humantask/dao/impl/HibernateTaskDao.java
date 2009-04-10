package pl.touk.humantask.dao.impl;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Task;

import com.trg.dao.dao.original.GenericDAOImpl;
import org.example.ws_ht.api.TStatus;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.TaskTypes;
import static pl.touk.humantask.model.GenericHumanRole.*;

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

    
    public List<Task> getTasks(Assignee owner, TaskTypes taskType, GenericHumanRole genericHumanRole, String workQueue, List<Task.Status> status, String whereClause, String createdOnClause, Integer maxTasks) {

        Criteria criteria = getSession().createCriteria(Task.class);

        
        if (null == workQueue) {
            workQueue = owner.toString();
            criteria.add(Expression.eq(ACTUAL_OWNER.toString(), owner));
        }
        
        if (null != status) {
            criteria.add(Expression.in("status", status));
        }
        
        if (null != createdOnClause) {
            criteria.add(Expression.eq("createdOn", Expression.sql(createdOnClause)));
        }
        if (null != maxTasks && maxTasks > -1) {
            criteria.setMaxResults(maxTasks);
        }
        if (null != whereClause) {
            criteria.add( Expression.sql(whereClause) );
        }
        
        if (null != genericHumanRole) {
            switch (genericHumanRole) {
                //TODO: owner for now but should be workQueue 
                
                case ACTUAL_OWNER:
                    criteria.add(Expression.eq(ACTUAL_OWNER.toString(), owner));
                    break;
                case BUSINESS_ADMINISTRATORS:
                    //criteria.add(Expression.eq(BUSINESS_ADMINISTRATORS.toString(), owner.toString()));
                    break;
                case NOTIFICATION_RECIPIENTS:
                    criteria.add(Expression.eq(NOTIFICATION_RECIPIENTS.toString(), owner));
                    break;
                case POTENTIAL_OWNERS:
                    criteria.add(Expression.eq(POTENTIAL_OWNERS.toString(), owner));
                    break;
                case TASK_INITIATOR:
                    criteria.add(Expression.eq(TASK_INITIATOR.toString(), owner));
                    break;
                case TASK_STAKEHOLDERS:
                    criteria.add(Expression.eq(TASK_STAKEHOLDERS.toString(), owner));
                    break;
                default:
            }
         
        }
        
        
        criteria.addOrder(Order.asc("activationTime"));
        
        return criteria.list();
    }

    public List<Task> getTasksToResume(Task.Status status, Date date) {

        Criteria criteria = getSession().createCriteria(Task.class);
        criteria.add(Expression.eq("status", status));

        return criteria.list();
    }
}
