/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;

import pl.touk.humantask.dao.impl.HibernateAssigneeDao;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Person;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.spec.HumanInteractionsFactoryBean;
import pl.touk.humantask.spec.TaskDefinition;

/**
 * Loads property file. Returns all people listed in property file together with
 * users specified literally.
 * 
 * @author Witek Wołejszo
 */
public class PropertyBasedPeopleQuery implements PeopleQuery {

    private final Log log = LogFactory.getLog(HumanInteractionsFactoryBean.class);

    private Resource configuration;

    private HibernateAssigneeDao assigneeDao;

    /**
     * 
     */
    public List<Assignee> evaluate(TaskDefinition.LogicalPeopleGroup logicalPeopleGroup, Task task) {

        log.info("Evaluating members of logical people group: " + logicalPeopleGroup.getName());

        List<Assignee> result = new ArrayList<Assignee>();

        Properties p = new Properties();
        try {

            p.load(configuration.getInputStream());

            String value = (String) p.get(logicalPeopleGroup.getName());

            // parse
            String[] peopleInGroup = value.split(",");

            for (String name : peopleInGroup) {

                Person person = assigneeDao.getPerson(name);

                if (person == null) {

                    person = new Person();
                    person.setName(name);
                    assigneeDao.create(person);
                }

                result.add(person);
            }

        } catch (IOException e) {

            // Access error should not affect evaluation TODO: ref to specs
            try {
                log.error("Error reading: " + configuration.getURL());
            } catch (IOException e1) {
            } finally {
                log.error("Error reading file.");
            }

        }

        return result;
    }

    public void setConfiguration(Resource configuration) {
        this.configuration = configuration;
    }

    public Resource getConfiguration() {
        return configuration;
    }

    public void setAssigneeDao(HibernateAssigneeDao assigneeDao) {
        this.assigneeDao = assigneeDao;
    }

    public HibernateAssigneeDao getAssigneeDao() {
        return assigneeDao;
    }

}
