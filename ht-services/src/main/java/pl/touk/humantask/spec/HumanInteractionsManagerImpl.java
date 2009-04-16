/*
 * Copyright (c) (2005 - )2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.spec;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.example.ws_ht.THumanInteractions;
import org.example.ws_ht.TTask;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Group;
import pl.touk.humantask.model.Message;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/* 
 * PoC for the task definition manager.
 * TODO cannot depend on model
 * 
 * @author <a href="mailto:jkr@touk.pl">Jakub Kurlenda</a>
 * 
 */
@Service
public class HumanInteractionsManagerImpl implements HumanInteractionsManagerInterface {

    private final Log log = LogFactory.getLog(getClass());

    // ============= FIELDS ===================

    private final List<HumanInteractions> humanInteractionsList = new ArrayList<HumanInteractions>();

    // ============= CONSTRUCTOR ===================


    /**
     * Creates HumanInteractionsManagerImpl, with a given human interactions definition list. 
     *
     * @param resources - collection of *.xml files with human interactions definitions.
     * @throws HumanTaskException - if task definition names are duplicated. 
     */
    public HumanInteractionsManagerImpl(List<Resource> resources) throws HumanTaskException {
        Validate.notNull(resources);
        
        Map<String, String> taskDefinitionsNamesMap = new HashMap<String, String>();
        
        for (Resource htdXml : resources) {
            try {
                // obtain HumanInteractions instance from a given resource.
                HumanInteractions humanInteractions = createHumanIteractionsInstance(htdXml);
                
                // Extract JAXB model from a given resource.
                THumanInteractions hiDoc = unmarshallHumanInteractionsData(htdXml);

                // Fetch all task definitions inside JAXB model
                List<TaskDefinition> taskDefinitions = extractTaskDefinitionList(hiDoc, humanInteractions, taskDefinitionsNamesMap);
                 
                humanInteractions.setTaskDefinitions(taskDefinitions);

                humanInteractionsList.add(humanInteractions);
            } catch (Exception e) {
                if (!(e instanceof HumanTaskException)) {
                    if (log.isErrorEnabled()) {
                        log.error("Error while unmarshalling XML data");
                    }
                }
                throw (HumanTaskException) e;
            }
        }
    }


    // ============= INTERFACE IMPLEMENTATION  ===================

    /*
     * Retrieves task definition by name.
     *
     * @param taskName - sought task definition name.
     * @return TaskDefinition instance.
     * @throws HumanTaskException in case when no such task definition was found.
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
    
    /**
     * Checks, if a task with a given name already exists in the context. 
     *
     * @param taskName
     * @throws HumanTaskException if task with a given name already exists.
     */
    private void checkTaskDefinitionUniqueness(String taskName, Map<String, String> taskDefinitionNamesMap) throws HumanTaskException {
        if (taskDefinitionNamesMap.containsKey(taskName)) {
            throw new HumanTaskException("Task definition names must be unique!");
        }
    }

    /**
     * Extracts TaskDefinition object instances from JAXB model.
     *  
     * @param hiDoc JAXB model containing task definition data.
     * @param humanInteractions parent for a given task definition, which contains all the data from the xml file. 
     * @return list of TaskDefinition instances.
     * @throws HumanTaskException if task with a given name already exists.
     */
    private List<TaskDefinition> extractTaskDefinitionList(THumanInteractions hiDoc, HumanInteractions humanInteractions, Map<String, String> taskDefinitionNamesMap) throws HumanTaskException {
        List<TaskDefinition> taskDefinitions = new ArrayList<TaskDefinition>();

        for (TTask tTask : hiDoc.getTasks().getTask()) {
            checkTaskDefinitionUniqueness(tTask.getName(), taskDefinitionNamesMap);
            TaskDefinition taskDefinition = new TaskDefinition(tTask.getName(), this);
            taskDefinition.setDefinition(humanInteractions);
            taskDefinitions.add(taskDefinition);
            taskDefinitionNamesMap.put(tTask.getName(), "XXX");
        }
        return taskDefinitions;
    }

    /**
     * Unmarshalls human interactions data from the XML file.
     * @param htdXml - file containing human interactions definition.
     * @return
     * @throws JAXBException
     * @throws IOException
     */
    private THumanInteractions unmarshallHumanInteractionsData(Resource htdXml) throws JAXBException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance("org.example.ws_ht");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        InputStream is = htdXml.getInputStream();

        return ((JAXBElement<THumanInteractions>) unmarshaller.unmarshal(is)).getValue();
    }

    /**
     * Creates HumanInteractions instance, passing DOM Document instance to its constructor.
     * 
     * @param htdXml
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws NoSuchAlgorithmException
     */
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
     * Just a very simple, stub pl.touk.humantask.spec.PeopleQuery implementation, which simply creates Assignee instances with a given name.
     *
     * @param logicalPeopleGroupName the logical people group name
     * @param input                  the input message that created the task
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