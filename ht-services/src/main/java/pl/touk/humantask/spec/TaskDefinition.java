/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.spec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

/**
 * Holds information about task version runnable in TouK Human Task engine. Task
 * consists of:
 * 
 * - HumanInteractions object - task name (reference to single task in HumanInteractions object)
 * 
 * @author Witek Wołejszo
 */
public class TaskDefinition {

    private final Log log = LogFactory.getLog(TaskDefinition.class);

    private String name;
    private HumanInteractions humanInteractions;
    private boolean instantiable;

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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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

            XPathExpression expr = xpath.compile("/htd:humanInteractions/htd:tasks/htd:task[@name='" + name
                    + "']/htd:presentationElements/htd:description[@xml:lang='" + lang + "' and @contentType='" + contentType + "']");

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

    // TODO make this function generic to handle all roles
    public List<String> getPotentialOwners() {

        List<String> result = new ArrayList<String>();

        XPath xpath = xPathFactory.newXPath();
        xpath.setNamespaceContext(new HtdNamespaceContext());

        // logical groups
        try {

            XPathExpression expr = xpath.compile("/htd:humanInteractions/htd:tasks/htd:task[@name='" + name
                    + "']/htd:peopleAssignments/htd:potentialOwners/htd:from[@logicalPeopleGroup]");

            NodeList nl = (NodeList) expr.evaluate(humanInteractions.getDocument(), XPathConstants.NODESET);

            for (int i = 0; i < nl.getLength(); i++) {

                Node n = nl.item(i);

                String logicalGroupName = n.getAttributes().getNamedItem("logicalPeopleGroup").getNodeValue();

                // String newname = (String)
                // n.getAttributes().getNamedItem("logicalPeopleGroup").getAttributes().toString();
                // get parameters

                result.add(logicalGroupName);

            }

        } catch (XPathExpressionException e) {

            log.error("Error evaluating XPath.", e);

        }

        // TODO literal people & groups
        // result.add("kamil");
        // result.add("witek");
        return result;
    }

    public String getSubject() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns globally unique key identifying task.
     */
    public String getKey() {
        return name + "_" + humanInteractions.getMd5();
    }

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
            if (prefix == null)
                throw new NullPointerException("Null prefix");
            else if ("htd".equals(prefix))
                return "http://www.example.org/WS-HT";
            else if ("xml".equals(prefix))
                return XMLConstants.XML_NS_URI;
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
