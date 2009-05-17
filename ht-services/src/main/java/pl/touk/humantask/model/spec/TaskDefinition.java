/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model.spec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.example.ws_ht.TGenericHumanRole;
import org.example.ws_ht.TPresentationParameter;
import org.example.ws_ht.TTask;
import org.springframework.beans.factory.annotation.Configurable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pl.touk.humantask.PeopleQuery;
import pl.touk.humantask.exceptions.HTException;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Group;
import pl.touk.humantask.model.Person;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.util.TemplateEngine;

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
     * TODO rewrite xpath - > jaxb
     * @param lang
     * @param contentType
     * @param task
     * @return
     */
    public String getDescription(String lang, String contentType, Task task) {

        XPath xpath = createXPathInstance();

        try {

            //retrieve description template
            
            String XPATH_EXPRESSION_FOR_DESCRIPTION_EVALUATION = "" +
                    "/htd:humanInteractions" +
                    "/htd:tasks" +
                    "/htd:task[@name='" + tTask.getName() + "']" +
                    "/htd:presentationElements" +
                    "/htd:description[@xml:lang='" + lang + "' and @contentType='" + contentType + "']";

            XPathExpression expr = xpath.compile(XPATH_EXPRESSION_FOR_DESCRIPTION_EVALUATION);
            Node node = (Node) expr.evaluate(humanInteractions.getDocument(), XPathConstants.NODE);            
            String descriptionTamplate = node.getTextContent();
            
            //retrieve presentation parameters
            
            //Map<String, Object> presentationParameters = this.getTaskPresentationParameters(task);            
            Map<String, Object> presentationParameters = task.getPresentationParameterValues();
            
            return new TemplateEngine().merge(descriptionTamplate, presentationParameters);

        } catch (XPathExpressionException e) {

            log.error("Error evaluating XPath.", e);

        }

        return null;
    }

    /**
     * Return values of Task presentation parameters.
     * @param task The task presentation parameters values are evaluated for.
     * @return
     */
    public Map<String, Object> getTaskPresentationParameters(Task task) {
        
        Map<String, Object> result = new HashMap<String, Object>();
        
        List<TPresentationParameter> presentationParameters = tTask.getPresentationElements().getPresentationParameters().getPresentationParameter();
        
        for(TPresentationParameter presentationParameter : presentationParameters) {
            
            //TODO get correct type
            log.info("Evaluating: " + presentationParameter.getContent().get(0).toString().trim());
            log.info("Type: " + presentationParameter.getType());
            
            QName parameterType = XPathConstants.STRING;
            Object o = task.evaluateXPath(presentationParameter.getContent().get(0).toString().trim(), parameterType);
            
            result.put(presentationParameter.getName(), o);
        }
        
        return result;
    }

    /**
     * Evaluates assignees of generic human role.
     *
     * @param humanRoleName The generic human role.
     * @param task          The task instance we evaluate assignees for.
     * @return list of task assignees or empty list, when no assignments were made to this task.
     */
    public Set<Assignee> evaluateHumanRoleAssignees(GenericHumanRole humanRoleName, Task task) throws HTException {
        
        Set<Assignee> evaluatedAssigneeList = new HashSet<Assignee>();
        
        //logical people groups
        List<JAXBElement<TGenericHumanRole>> ghrList = this.tTask.getPeopleAssignments().getGenericHumanRole();
        for (int i=0; i < ghrList.size(); i++) {
            
            JAXBElement<TGenericHumanRole> ghr = ghrList.get(i);
            
            if (ghr.getName().getLocalPart().equals(humanRoleName.toString())) {
                
                if (ghr.getValue().getFrom() != null) { 
                
                    String logicalPeopleGroupName = ghr.getValue().getFrom().getLogicalPeopleGroup().toString();
                    List<Assignee> peopleQueryResult = peopleQuery.evaluate(logicalPeopleGroupName, null);
                    evaluatedAssigneeList.addAll(peopleQueryResult);
                }
            }
        }
        
        XPath xpath = createXPathInstance();
        
        //literal
        String groupsXPath = "" +
            "/htd:humanInteractions" +
            "/htd:tasks" +
            "/htd:task[@name='" + tTask.getName() + "']" +
            "/htd:peopleAssignments" +
            "/htd:" + humanRoleName.toString() +
            "/htd:literal" +
            "/htd:organizationalEntity" +
            "/htd:groups" +
            "/htd:group";
        
        String usersXPath = "" +
            "/htd:humanInteractions" +
            "/htd:tasks" +
            "/htd:task[@name='" + tTask.getName() + "']" +
            "/htd:peopleAssignments" +
            "/htd:" + humanRoleName.toString() +
            "/htd:literal" +
            "/htd:organizationalEntity" +
            "/htd:users" +
            "/htd:user";
        
        try {
        
            XPathExpression expr = xpath.compile(groupsXPath);
            NodeList nl = (NodeList) expr.evaluate(humanInteractions.getDocument(), XPathConstants.NODESET);
            for (int i = 0; i < nl.getLength(); i++) {
                evaluatedAssigneeList.add(new Group(nl.item(i).getFirstChild().getNodeValue()));
            }
            
            expr = xpath.compile(usersXPath);
            nl = (NodeList) expr.evaluate(humanInteractions.getDocument(), XPathConstants.NODESET);
            for (int i = 0; i < nl.getLength(); i++) {
                evaluatedAssigneeList.add(new Person(nl.item(i).getFirstChild().getNodeValue()));
            }
            
        } catch (XPathExpressionException e) {
            
            if (log.isErrorEnabled()) {
                log.error("Error evaluating XPath for task: " + tTask.getName(), e);
            }
            
            throw  new HTException("Error evaluating XPath for task: " + tTask.getName());
        }

        return evaluatedAssigneeList;
    }

    /**
     * Returns a task subject in a required language.
     * @param lang subject language according ISO, e.g. en-US, pl, de-DE
     * @param task The task subject value is evaluated for.
     * @return subject
     */
    public String getSubject(String lang, Task task) {
        
        XPath xpath = createXPathInstance();
        
        try {
            
            //retrieve subject template

            String XPATH_EXPRESSION_FOR_SUBJECT_EVALUATION = "" +
                    "/htd:humanInteractions/" +
                    "htd:tasks/" +
                    "htd:task[@name='" +
                    getTaskName() + "']/" +
                    "htd:presentationElements/" +
                    "htd:subject[@xml:lang='" + lang + "']";

            XPathExpression expr = xpath.compile(XPATH_EXPRESSION_FOR_SUBJECT_EVALUATION);
            
            Node node = (Node) expr.evaluate(humanInteractions.getDocument(), XPathConstants.NODE);
            
            String subjectTemplate = node.getTextContent();
            
            //retrieve presentation parameters
            //Map<String, Object> presentationParameters = this.getTaskPresentationParameters(task);            
            Map<String, Object> presentationParameters = task.getPresentationParameterValues();
                
            return new TemplateEngine().merge(subjectTemplate, presentationParameters);
            
        } catch (XPathExpressionException e) {
            log.error("Error evaluating XPath.", e);
        }
        
        return null;
    }

    /**
     * TODO ww - javadoc
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
