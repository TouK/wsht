package pl.touk.humantask.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Person;

import com.trg.dao.dao.original.GenericDAOImpl;

public class HibernateAssigneeDao extends GenericDAOImpl<Assignee, Long> {

    public Person getPerson(String name) {

        Criteria criteria = getSession().createCriteria(Person.class);
        criteria.add(Restrictions.eq("name", name));
        return (Person) criteria.uniqueResult();
    }
}
