/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */
package pl.touk.humantask.spec;

import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.example.ws_ht.TPresentationParameter;
import org.example.ws_ht.TTask;
import org.springframework.beans.factory.annotation.Configurable;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Message;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.exceptions.HumanTaskException;

/**
 * Holds information about task version runnable in TouK Human Task engine. Task
 * consists of:
 * <p/>
 * - HumanInteractions object - task name (reference to single task in HumanInteractions object)
 *
 * @author Witek Wołejszo
 * @author Kamil Eisenbart
 * @author Mateusz Lipczyński
 */
@Configurable(dependencyCheck = true)
public class TaskDefinition {

    private final Log log = LogFactory.getLog(TaskDefinition.class);
    
    /**
     * Human Interactions specification containing this {@link TaskDefinition}.
     */
    private HumanInteractions humanInteractions;
    
    private TTask tTask;

    private boolean instantiable;

    private XPathFactory xPathFactory;
    
    private PeopleQuery peopleQuery;

    // ==================== CONSTRUCTOR =========================

    public TaskDefinition(TTask taskDefinition, PeopleQuery peopleQuery) {
        
        super();
        
        Validate.notNull(taskDefinition);
        Validate.notNull(peopleQuery);

        this.tTask = taskDefinition;
        this.peopleQuery = peopleQuery;
        
        xPathFactory = XPathFactory.newInstance();
    }


    /**
     * Returns description of the Task.
     * TODO change input to Task
     * TODO rewrite xpath - > jaxb
     * @param lang
     * @param contentType
     * @param input
     * @return
     */
    public String getDescription(String lang, String contentType, Map<String, Message> input) {

        XPath xpath = createXPathInstance();

        try {

            String XPATH_EXPRESSION_FOR_DESCRIPTION_EVALUATION = "" +
                    "/htd:humanInteractions" +
                    "/htd:tasks" +
                    "/htd:task[@name='" + tTask.getName() + "']" +
                    "/htd:presentationElements" +
                    "/htd:description[@xml:lang='" + lang + "' and @contentType='" + contentType + "']";

            XPathExpression expr = xpath.compile(XPATH_EXPRESSION_FOR_DESCRIPTION_EVALUATION);

            Node node = (Node) expr.evaluate(humanInteractions.getDocument(), XPathConstants.NODE);

            return node.getTextContent();

        } catch (XPathExpressionException e) {

            log.error("Error evaluating XPath.", e);

        }

        return null;
    }
    
    /**
     * Return values of Task presentation parameters.
     * TODO change input to Task
     * @param input
     * @return
     */
    protected Map<String, Object> getTaskPresentationParameters(Task task) {
        
        Map<String, Object> result = new HashMap<String, Object>();
        
        List<TPresentationParameter> presentationParameters = tTask.getPresentationElements().getPresentationParameters().getPresentationParameter();
        
        for(TPresentationParameter presentationParameter : presentationParameters) {
            log.info("Evaluating: " + presentationParameter.getContent().get(0).toString().trim());
            Object o = task.evaluateXPath(presentationParameter.getContent().get(0).toString().trim());
            result.put(presentationParameter.getName(), o);
        }
        
        return result;
    }

    /**
     * Evaluates assignees of generic human role.
     *
     * @param humanRoleName generic human role
     * @param input         the input message that created the task
     * @return list of task assignees or empty list, when no assignments were made to this task.
     */
    public List<Assignee> evaluateHumanRoleAssignees(GenericHumanRole humanRoleName, Map<String, Message> input) throws HumanTaskException {
        List<String> groupNames = new ArrayList<String>();
        List<Assignee> evaluatedAssigneeList = new ArrayList<Assignee>();

        //read htd
        XPath xpath = createXPathInstance();

        try {

            String XPATH_EXPRESSION_FOR_HUMAN_ROLES_EVALUATION = "" +
                    "/htd:humanInteractions" +
                    "/htd:tasks" +
                    "/htd:task[@name='" + tTask.getName() + "']" +
                    "/htd:peopleAssignments" +
                    "/htd:" + humanRoleName.toString();

            XPathExpression expr = xpath.compile(XPATH_EXPRESSION_FOR_HUMAN_ROLES_EVALUATION);

            NodeList nl = (NodeList) expr.evaluate(humanInteractions.getDocument(), XPathConstants.NODESET);

            if (nl.getLength() == 0) {
                return Collections.EMPTY_LIST;
            }
            
            for (int i = 0; i < nl.getLength(); i++) {

                NodeList children = nl.item(i).getChildNodes();
                NamedNodeMap map = children.item(1).getAttributes();
                Node item = map.getNamedItem("logicalPeopleGroup");

                if (null == item)
                    continue;
                
                String groupName = item.getNodeValue();
                groupNames.add(groupName);
            }


        } catch (XPathExpressionException e) {
            if (log.isErrorEnabled()) {
                log.error("Error evaluating XPath for task: " + tTask.getName(), e);
            }

            throw  new HumanTaskException("Error evaluating XPath for task: " + tTask.getName());
        }
        
        for (String groupName : groupNames) {
            //TODO add parameters
            List<Assignee> peopleQueryResult = peopleQuery.evaluate(groupName, null);
            evaluatedAssigneeList.addAll(peopleQueryResult);
        }

        return evaluatedAssigneeList;
    }

    /**
     * Returns an unformatted task subject in a required language
     *
     * @param lang subject language according ISO, e.g. en-US, pl, de-DE
     * @return subject
     */
    public String getSubject(String lang, Map<String, Message> input) {
        String result = "";
        XPath xpath = createXPathInstance();
        try {
            String XPATH_EXPRESSION_FOR_SUBJECT_EVALUATION = "" +
                    "/htd:humanInteractions/" +
                    "htd:tasks/" +
                    "htd:task[@name='" +
                    getTaskName() + "']/" +
                    "htd:presentationElements/" +
                    "htd:subject[@xml:lang='" + lang + "']";

            XPathExpression expr = xpath.compile(XPATH_EXPRESSION_FOR_SUBJECT_EVALUATION);
            
            Node node = (Node) expr.evaluate(humanInteractions.getDocument(), XPathConstants.NODE);
            
            result = node.getTextContent();
            
        } catch (XPathExpressionException e) {
            log.error("Error evaluating XPath.", e);
        }
        return result;
    }

//
//
//    public List<LogicalPeopleGroup> getLogicalpeopleGroups() {
//
//        List<LogicalPeopleGroup> result = new ArrayList<LogicalPeopleGroup>();
//
//        XPath xpath = createXpathInstance();
//
//        try {
//
//            String XPATH_EXPRESSION_FOR_LOGICAL_PEOPLE_GROUPS_EVALUATION = "" +
//                    "/htd:humanInteractions" +
//                    "/htd:logicalPeopleGroups" +
//                    "/htd:logicalPeopleGroup";
//
//            XPathExpression expr = xpath.compile(XPATH_EXPRESSION_FOR_LOGICAL_PEOPLE_GROUPS_EVALUATION);
//
//            NodeList nl = (NodeList) expr.evaluate(humanInteractions.getDocument(), XPathConstants.NODESET);
//            for (int i = 0; i < nl.getLength(); i++) {
//
//                Node n = nl.item(i);
//
//                LogicalPeopleGroup pg = new LogicalPeopleGroup();
//                pg.setName(n.getAttributes().getNamedItem("name").getNodeValue());
//                // TODO set parameters
//
//                result.add(pg);
//
//            }
//
//            return result;
//
//        } catch (XPathExpressionException e) {
//
//            log.error("Error evaluating XPath.", e);
//            // TODO throw
//
//        }
//
//        return null;
//    }
//
//    public static class LogicalPeopleGroup {
//
//        private String name;
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public String getName() {
//            return name;
//        }
//    }

    /**
     * TODO ww
     */
    public static class HtdNamespaceContext implements NamespaceContext {

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

    // ================ GETTERS + SETTERS ===================

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

    public String getTaskName() {
        return tTask.getName();
    }

    // ================== HELPER METHODS ==================
    
    private XPath createXPathInstance() {
        XPath xpath = xPathFactory.newXPath();
        xpath.setNamespaceContext(new HtdNamespaceContext());
        return xpath;
    }

}
