package pl.touk.humantask.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.trg.dao.dao.original.GenericDAOImpl;

import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Person;

public class HibernateAssigneeDao extends GenericDAOImpl<Assignee, Long> {

    public Person getPerson(String name) {

        Criteria criteria = getSession().createCriteria(Person.class);
        criteria.add(Restrictions.eq("name", name));
        return (Person) criteria.uniqueResult();
    }
}
