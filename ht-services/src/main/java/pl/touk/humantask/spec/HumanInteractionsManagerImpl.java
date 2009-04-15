/*
 * Copyright (c) (2005 - )2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.spec;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.example.ws_ht.THumanInteractions;
import org.example.ws_ht.TLogicalPeopleGroup;
import org.example.ws_ht.TTask;
import org.example.ws_ht.TGenericHumanRole;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.model.Group;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Message;
import pl.touk.humantask.PeopleQuery;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/* 
 * PoC for the task definition manager.
 * TODO: dowiedzieć się więcej n.t. obiektu HumanInteractions, czy będzie on nadal potrzebny w sposób, w jaki jest używany przez HumanInteractionsFactoryBean oraz Services ??
 * TODO cannot depend on model
 * 
 * @author <a href="mailto:jkr@touk.pl">Jakub Kurlenda</a>
 * 
 */
@Service
public class HumanInteractionsManagerImpl implements HumanInteractionsManagerInterface {

    private final Log LOG = LogFactory.getLog(getClass());

    // ============= FIELDS ===================

    private final List<HumanInteractions> humanInteractionsList = new ArrayList<HumanInteractions>();
    
    // ============= CONSTRUCTOR ===================

    public HumanInteractionsManagerImpl(List<Resource> resources) throws HumanTaskException {
        Validate.notNull(resources);

        for (Resource htdXml : resources) {
            try {

                HumanInteractions humanInteractions = createHumanIteractionsInstance(htdXml);
                
                THumanInteractions hiDoc = unmarshallHumanInteractionsData(htdXml);
                
                List<TaskDefinition> taskDefinitions = extractTaskDefinitionList(hiDoc, humanInteractions);
                
                humanInteractions.setTaskDefinitions(taskDefinitions);
                
//                List<Group> peopleGroups = extractLogicalPeopleGroup(hiDoc);
//
//                humanInteractions.setGroupList(peopleGroups);

                humanInteractionsList.add(humanInteractions);
                
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Error while unmarshalling XML data");
                }
            }
        }
    }

    // ============= INTERFACE IMPLEMENTATION  ===================

    /*
     * Retrieves task definition by name.
     *
     * @param taskName
     * @return TaskDefinition instance
     * @throws HumanTaskException in case when no such task definition was found
     */
    public TaskDefinition getTaskDefinition(String taskName) throws HumanTaskException {
        Validate.notNull(taskName);

        for (HumanInteractions humanInteractions : humanInteractionsList) {
            for (TaskDefinition taskDefinition : humanInteractions.getTaskDefinitions()) {
                if (taskName.equals(taskDefinition.getTaskName())) {
                    return taskDefinition;
                }
            }
        }
        throw new HumanTaskException("Task definition with a given name: " + taskName + " not found!");
    }

    // ============= HELPER METHODS ===================

    private List<Group> extractLogicalPeopleGroup(THumanInteractions hiDoc) {
        List<Group> peopleGroups = new ArrayList<Group>();
        
        for (TLogicalPeopleGroup logicalPeopleGroup : hiDoc.getLogicalPeopleGroups().getLogicalPeopleGroup()) {
            Group group = new Group();
            group.setName(logicalPeopleGroup.getName());

            peopleGroups.add(group);
        }
        return peopleGroups;
    }

    private List<TaskDefinition> extractTaskDefinitionList(THumanInteractions hiDoc, HumanInteractions humanInteractions) {
        List<TaskDefinition> taskDefinitions = new ArrayList<TaskDefinition>();
        
        for (TTask tTask : hiDoc.getTasks().getTask()) {
            TaskDefinition taskDefinition = new TaskDefinition(tTask.getName(), this);
            taskDefinition.setDefinition(humanInteractions);
            
            taskDefinitions.add(taskDefinition);
        }
        return taskDefinitions;
    }

    private THumanInteractions unmarshallHumanInteractionsData(Resource htdXml) throws JAXBException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance("org.example.ws_ht");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        InputStream is = htdXml.getInputStream();

        return ((JAXBElement<THumanInteractions>) unmarshaller.unmarshal(is)).getValue();
    }

    private HumanInteractions createHumanIteractionsInstance(Resource htdXml) throws IOException, ParserConfigurationException, SAXException, NoSuchAlgorithmException {
        InputStream is;

        // dom
        is = htdXml.getInputStream();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(is);

        return new HumanInteractions(document);
    }

    /**
     * Just a very simple, stub people query implementation, which simply creates Assignee instances with a given name.
     * 
     * @param logicalPeopleGroupName the logical people group name
     * @param input the input message that created the task
     * @return collection of assignees.
     */
    public List<Assignee> evaluate(String logicalPeopleGroupName, Map<String, Message> input) {
        List<Assignee> assignees = new ArrayList<Assignee>();
        Group group = new Group();
        group.setName(logicalPeopleGroupName);
        assignees.add(group);
        return assignees;
    }
}