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
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.model.Group;

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

/* 
 * PoC for the task definition manager.
 * TODO: dowiedzieć się więcej n.t. obiektu HumanInteractions, czy będzie on nadal potrzebny w sposób, w jaki jest używany przez HumanInteractionsFactoryBean oraz Services ??
 * TODO cannot depend on model
 * @author Jakub Kurlenda
 * @author <a href="mailto:jkr@touk.pl">Jakub Kurlenda</a>
 * 
 */

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

//    public List<TaskDefinition> getTaskDefinitions() {
//        List<TaskDefinition> taskDefinitions = new ArrayList<TaskDefinition>();
//        for (HumanInteractions humanInteractions : humanInteractionsList) {
//            taskDefinitions.addAll(humanInteractions.getTaskDefinitions());
//        }
//        return taskDefinitions;
//    }
//
//    public Group getLogicalPeopleGroupByName(String groupName) throws HumanTaskException {
//        Validate.notNull(groupName);
//
//        for (HumanInteractions humanInteractions : humanInteractionsList) {
//            for (Group group : humanInteractions.getGroupList()) {
//                if (groupName.equals(group.getName())) {
//                    return group;
//                }
//            }
//        }
//        throw new HumanTaskException("People group with a given name: " + groupName + " not found!");
//    }
//
//    public TaskDefinition getTaskDefinitionByKey(String key) throws HumanTaskException {
//        List<TaskDefinition> taskDefinitions = getTaskDefinitions();
//        for (TaskDefinition taskDefinition : taskDefinitions) {
//            if (key.equals(taskDefinition.getKey())) {
//                return taskDefinition;
//            }
//        }
//
//        throw new HumanTaskException("No task definition with a given key: " + key + " was found");
//    }

    public List<HumanInteractions> getHumanInteractions() {
        return humanInteractionsList;
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
            TaskDefinition taskDefinition = new TaskDefinition();
            taskDefinition.setTaskName(tTask.getName());
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
        String md5;

        // dom
        is = htdXml.getInputStream();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(is);

        is = htdXml.getInputStream();

        MessageDigest digest = MessageDigest.getInstance("MD5");

        byte[] buffer = new byte[8192];
        int read = 0;
        try {

            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            md5 = bigInt.toString(16);

        } catch (IOException e) {

            //log.error("Error reading: " + htdXml);
            throw new RuntimeException("Error reading: " + htdXml, e);

        } finally {

            try {
                is.close();
            } catch (IOException e) {
                //  log.error("Error closing: " + htdXml);
                throw new RuntimeException("Error closing: " + htdXml, e);
            }

        }

        return new HumanInteractions(document);
    }
}