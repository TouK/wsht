/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */
package pl.touk.humantask.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pl.touk.humantask.PeopleQuery;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Message;
import pl.touk.humantask.model.Task;

/**
 * Holds information about task version runnable in TouK Human Task engine. Task
 * consists of:
 * 
 * - HumanInteractions object - task name (reference to single task in HumanInteractions object)
 * 
 * @author Witek Wołejszo
 * @author Kamil Eisenbart
 * @author Mateusz Lipczyński
 */
public class TaskDefinition {

    private final Log log = LogFactory.getLog(TaskDefinition.class);
    /**
     * Human Interactions specification containing this {@link TaskDefinition}.
     */
    private HumanInteractions humanInteractions;

    private String taskName;
    
    private boolean instantiable;
    
    //TODO jkr: here? configured at human interactions manager level
    private PeopleQuery peopleQuery;
    private XPathFactory xPathFactory;

    public TaskDefinition() {
        super();
        xPathFactory = XPathFactory.newInstance();
    }

    public boolean getInstantiable() {
        return instantiable;
    }

    public void setInstantiable(boolean instantiable) {
        this.instantiable = instantiable;
    }

    public void setDefinition(HumanInteractions definition) {
        this.humanInteractions = definition;
    }

    public HumanInteractions getDefinition() {
        return humanInteractions;
    }

    //TODO jkr - do konstruktora
    public void setTaskName(String name) {
        this.taskName = name;
    }

    public String getTaskName() {
        return taskName;
    }

    public PeopleQuery getPeopleQuery() {
        return peopleQuery;
    }

    public void setPeopleQuery(PeopleQuery peopleQuery) {
        this.peopleQuery = peopleQuery;
    }

    // //TODO lazy
    // public TTask getTask() {
    //		
    // List<TTask> l =
    // getDefinition().getTHumanInteractions().getTasks().getTask();
    //		
    // for (TTask t : l) {
    // if (t.getName().equals(name)) {
    // return t;
    // }
    // }
    //
    // return null;
    //		
    // }

    // TODO handle text/html
    public String getDescription(String lang, String contentType) {

        XPath xpath = xPathFactory.newXPath();
        xpath.setNamespaceContext(new HtdNamespaceContext());

        try {

            XPathExpression expr = xpath.compile("/htd:humanInteractions/htd:tasks/htd:task[@name='" + taskName + "']/htd:presentationElements/htd:description[@xml:lang='" + lang + "' and @contentType='" + contentType + "']");

            Node node = (Node) expr.evaluate(humanInteractions.getDocument(), XPathConstants.NODE);

            if ("text/html".equals(contentType)) {

                // TODO serialize ?
                return node.getTextContent();

            } else {

                return node.getTextContent();

            }

        } catch (XPathExpressionException e) {

            log.error("Error evaluating XPath.", e);

        }

        return null;
    }

    /**
     * Evaluates assignees of generic human role.
     * @param genericHumanRole generic human role
     * @param input the input message that created the task
     * @return
     */
    public List<Assignee> evaluateHumanRoleAssignees(GenericHumanRole genericHumanRole, Map<String, Message> input) {
        
        //read htd
        
        //evaluate using people query
        
        //return
        
        return Collections.EMPTY_LIST;
    }

    
//    // TODO make this function generic to handle all roles
//    public List<String> getPotentialOwners() {
//
//        List<String> result = new ArrayList<String>();
//        XPath xpath = xPathFactory.newXPath();
//        xpath.setNamespaceContext(new HtdNamespaceContext());
//
//        // logical groups
//        try {
//            XPathExpression expr = xpath.compile("/htd:humanInteractions/htd:tasks/htd:task[@name='" + taskName + "']/htd:peopleAssignments/htd:potentialOwners/htd:from[@logicalPeopleGroup]");
//            NodeList nl = (NodeList) expr.evaluate(humanInteractions.getDocument(), XPathConstants.NODESET);
//            for (int i = 0; i < nl.getLength(); i++) {
//                Node n = nl.item(i);
//                String logicalGroupName = n.getAttributes().getNamedItem("logicalPeopleGroup").getNodeValue();
//
//                // String newname = (String)
//                // n.getAttributes().getNamedItem("logicalPeopleGroup").getAttributes().toString();
//                // get parameters
//
//                result.add(logicalGroupName);
//
//            }
//
//        } catch (XPathExpressionException e) {
//
//            log.error("Error evaluating XPath.", e);
//
//        }
//
//        // TODO literal people & groups
//        // result.add("kamil");
//        // result.add("witek");
//        return result;
//    }

    /**
     * Returns an unformatted task subject in a required language
     * @param lang  subject language according ISO, e.g. en-US, pl, de-DE
     * @return subject
     */
    public String getSubject(String lang) {
        String result = "";
        XPath xpath = xPathFactory.newXPath();
        xpath.setNamespaceContext(new HtdNamespaceContext());
        try {
            XPathExpression expr = xpath.compile("/htd:humanInteractions/htd:tasks/htd:task[@name='" +
                    taskName + "']/htd:presentationElements/htd:subject[@xml:lang='" + lang + "']");
            Node node = (Node) expr.evaluate(humanInteractions.getDocument(), XPathConstants.NODE);
            result = node.getTextContent();
        } catch (XPathExpressionException e) {
            log.error("Error evaluating XPath.", e);
        }
        return result;
    }

//    /**
//     * Returns globally unique key identifying task.
//     */
//    public String getKey() {
//        return taskName + "_" + humanInteractions.getDefinitionKey();
//    }

    public List<LogicalPeopleGroup> getLogicalpeopleGroups() {

        List<LogicalPeopleGroup> result = new ArrayList<LogicalPeopleGroup>();

        XPath xpath = xPathFactory.newXPath();
        xpath.setNamespaceContext(new HtdNamespaceContext());

        try {

            XPathExpression expr = xpath.compile("/htd:humanInteractions/htd:logicalPeopleGroups/htd:logicalPeopleGroup");

            NodeList nl = (NodeList) expr.evaluate(humanInteractions.getDocument(), XPathConstants.NODESET);
            for (int i = 0; i < nl.getLength(); i++) {

                Node n = nl.item(i);

                LogicalPeopleGroup pg = new LogicalPeopleGroup();
                pg.setName(n.getAttributes().getNamedItem("name").getNodeValue());
                // TODO set parameters

                result.add(pg);

            }

            return result;

        } catch (XPathExpressionException e) {

            log.error("Error evaluating XPath.", e);
        // TODO throw

        }

        return null;
    }

//    /**
//     * TODO mlp: javadoc
//     * @param logicalPeopleGroup
//     * @param task
//     * @return
//     */
//    public List<Assignee> evaluate(LogicalPeopleGroup logicalPeopleGroup, Task task) {
//        return this.peopleQuery.evaluate(logicalPeopleGroup, task);
//    }

    public static class LogicalPeopleGroup {

        private String name;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static class HtdNamespaceContext implements NamespaceContext {

        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new NullPointerException("Null prefix");
            } else if ("htd".equals(prefix)) {
                return "http://www.example.org/WS-HT";
            } else if ("xml".equals(prefix)) {
                return XMLConstants.XML_NS_URI;
            }
            return XMLConstants.NULL_NS_URI;
        }

        public String getPrefix(String namespaceURI) {
            // TODO Auto-generated method stub
            return null;
        }

        public Iterator getPrefixes(String namespaceURI) {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
