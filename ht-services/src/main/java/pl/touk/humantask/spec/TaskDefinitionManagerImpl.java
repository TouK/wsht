/*
 * Copyright (c) (2005 - )2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.spec;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.example.ws_ht.THumanInteractions;
import org.example.ws_ht.TLogicalPeopleGroup;
import org.example.ws_ht.TTask;
import org.springframework.core.io.Resource;

import pl.touk.humantask.HumanTaskException;
import pl.touk.humantask.model.Group;

/* 
 * PoC for the task definition manager.
 * TODO: dowiedzieć się więcej n.t. obiektu HumanInteractions, czy będzie on nadal potrzebny w sposób, w jaki jest używany przez HumanInteractionsFactoryBean oraz Services ??
 *
 * @author Jakub Kurlenda
 * @author <a href="mailto:jkr@touk.pl">Jakub Kurlenda</a>
 * 
 */

public class TaskDefinitionManagerImpl implements TaskDefinitionManagerInterface {

    private final Log LOG = LogFactory.getLog(getClass());

    // ============= FIELDS ===================
    
    private final List<TaskDefinition> taskDefinitions = new ArrayList<TaskDefinition>();
    private final List<Group> peopleGroups = new ArrayList<Group>();

    // ============= CONSTRUCTOR ===================
    
    public TaskDefinitionManagerImpl(Resource htdXml) throws HumanTaskException {
        Validate.notNull(htdXml);

        THumanInteractions hiDoc = null;

        try {
            
            hiDoc = unmarshallHumanIntegrationsData(htdXml);

        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error while unmarshalling XML data");
            }
        }

        extractTaskDefinitionList(hiDoc);

        extractLogicalPeopleGroup(hiDoc);
    }
    
    // ============= INTERFACE IMPLEMENTATION  ===================
    
    public TaskDefinition getTaskDefinitionByName(String taskName) throws HumanTaskException {
        Validate.notNull(taskName);

        for (TaskDefinition taskDefinition : taskDefinitions) {
            if (taskName.equals(taskDefinition.getName())) {
                return taskDefinition;
            }
        }
        throw new HumanTaskException("Task definition with a given name: " + taskName + " not found!");
    }

    public List<TaskDefinition> getTaskDefinitions() {
        return taskDefinitions;
    }

    public List<Group> getLogicalPeopleGroups() {
        return peopleGroups;
    }

    public Group getLogicalPeopleGroupByName(String groupName) throws HumanTaskException{
        Validate.notNull(groupName);

        for (Group peopleGroup : peopleGroups) {
            if (groupName.equals(peopleGroup.getName())) {
                return peopleGroup;
            }
        }
        throw new HumanTaskException("People group with a given name: " + groupName + " not found!");
    }

    // ============= HELPER METHODS ===================
    
    private void extractLogicalPeopleGroup(THumanInteractions hiDoc) {
        for (TLogicalPeopleGroup logicalPeopleGroup : hiDoc.getLogicalPeopleGroups().getLogicalPeopleGroup()) {
            Group group = new Group();
            group.setName(logicalPeopleGroup.getName());

            peopleGroups.add(group);
        }
    }

    private void extractTaskDefinitionList(THumanInteractions hiDoc) {
        for (TTask tTask : hiDoc.getTasks().getTask()) {
            TaskDefinition taskDefinition = new TaskDefinition();
            taskDefinition.setName(tTask.getName());
            taskDefinitions.add(taskDefinition);
        }
    }

    private THumanInteractions unmarshallHumanIntegrationsData(Resource htdXml) throws JAXBException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance("org.example.ws_ht");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        InputStream is = htdXml.getInputStream();

        THumanInteractions hiDoc = ((JAXBElement<THumanInteractions>) unmarshaller.unmarshal(is)).getValue();
        return hiDoc;
    }
}