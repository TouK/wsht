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
import pl.touk.humantask.util.XmlUtils;

import pl.touk.humantask.model.htd.TDescription;
import pl.touk.humantask.model.htd.TGenericHumanRole;
import pl.touk.humantask.model.htd.TPresentationParameter;
import pl.touk.humantask.model.htd.TTask;
import pl.touk.humantask.model.htd.TText;

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
    
    /**
     * XML namespaces supported in human task definitions.
     */
    private Map<String, String> xmlNamespaces;

    // ==================== CONSTRUCTOR =========================

    public TaskDefinition(TTask taskDefinition, PeopleQuery peopleQuery, Map<String, String> xmlNamespaces) {
        
        super();
        
        Validate.notNull(taskDefinition);
        Validate.notNull(peopleQuery);

        this.tTask = taskDefinition;
        this.peopleQuery = peopleQuery;
        this.xmlNamespaces = xmlNamespaces;
        
        this.xPathFactory = XPathFactory.newInstance();
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

        Validate.notNull(lang);
        Validate.notNull(contentType);
        Validate.notNull(task);
        
        String descriptionTamplate = null;

        List<TDescription> tDescriptions = this.tTask.getPresentationElements().getDescription();
        for (TDescription x : tDescriptions) {
            if (lang.equals(x.getLang()) && contentType.equals(x.getContentType())) {
                descriptionTamplate = x.getContent().get(0).toString();
                break;
            }
        }
        
        if (descriptionTamplate == null) {
            return "error";
        }

        //retrieve presentation parameters
        Map<String, Object> presentationParameters = task.getPresentationParameterValues();

        return new TemplateEngine().merge(descriptionTamplate, presentationParameters).trim();
    }

    /**
     * Return values of Task presentation parameters.
     * @param task      The task presentation parameters values are evaluated for.
     * @return          Map from parameter name to its value.
     */
    public Map<String, Object> getTaskPresentationParameters(Task task) {

        Validate.notNull(task);

        Map<String, Object> result = new HashMap<String, Object>();

        List<TPresentationParameter> presentationParameters = this.tTask.getPresentationElements().getPresentationParameters().getPresentationParameter();

        for(TPresentationParameter presentationParameter : presentationParameters) {

            //TODO do not instantiate each time
            QName returnType = new XmlUtils().getReturnType(presentationParameter.getType());
            String parameterName = presentationParameter.getName().trim();
            String parameterXPath = presentationParameter.getContent().get(0).toString().trim();
            
            Validate.notNull(returnType);
            Validate.notNull(parameterName);
            Validate.notNull(parameterXPath);
            
            if (log.isDebugEnabled()) {
                log.debug("Evaluating: " + parameterName);
                log.debug("XPath: " + parameterXPath);
                log.debug("Type: " + presentationParameter.getType());
                log.debug("Return: " + returnType);
            }
                
            Object o = task.evaluateXPath(parameterXPath, returnType);

            if (o != null) {
                log.info("Evaluated to: " + o + " of: " + o.getClass());
            } else {
                log.info("Evaluated to: null");
            }

            result.put(parameterName, o);
        }

        return result;
    }

    /**
     * Evaluates assignees of generic human role.
     * TODO - get rid of xpaths
     * @param humanRoleName The generic human role.
     * @param task          The task instance we evaluate assignees for.
     * @return list of task assignees or empty list, when no assignments were made to this task.
     */
    public Set<Assignee> evaluateHumanRoleAssignees(GenericHumanRole humanRoleName, Task task) throws HTException {
        
        Validate.notNull(humanRoleName);
        Validate.notNull(task);
        
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
            NodeList nl = (NodeList) expr.evaluate(this.humanInteractions.getDocument(), XPathConstants.NODESET);
            for (int i = 0; i < nl.getLength(); i++) {
                evaluatedAssigneeList.add(new Group(nl.item(i).getFirstChild().getNodeValue()));
            }
            
            expr = xpath.compile(usersXPath);
            nl = (NodeList) expr.evaluate(this.humanInteractions.getDocument(), XPathConstants.NODESET);
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
        
        Validate.notNull(lang);
        Validate.notNull(task);
        
        String subjectTemplate = null;

        List<TText> tTexts = this.tTask.getPresentationElements().getSubject();
        for (TText x : tTexts) {
            if (lang.equals(x.getLang())) {
                subjectTemplate = x.getContent().toString();
                break;
            }
        }
        
        if (subjectTemplate == null) {
            return "error";
        }
        
        Map<String, Object> presentationParameterValues = task.getPresentationParameterValues();

        return new TemplateEngine().merge(subjectTemplate, presentationParameterValues).trim();
    }

    /**
     * @see NamespaceContext
     * TODO get rid of xpaths in taskdefinition and this class 
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

        /**
         * {@inheritDoc}
         */
        public String getPrefix(String namespaceURI) {
            // TODO ???
            throw new NullPointerException("???");
        }

        /**
         * {@inheritDoc}
         */
        public Iterator getPrefixes(String namespaceURI) {
            // ???
            throw new NullPointerException("???");
        }
    }

    // ================ GETTERS + SETTERS ===================

    public boolean getInstantiable() {
        return this.instantiable;
    }

    public void setDefinition(HumanInteractions definition) {
        this.humanInteractions = definition;
    }

    public HumanInteractions getDefinition() {
        return this.humanInteractions;
    }

    public String getTaskName() {
        return this.tTask.getName();
    }

    // ================== HELPER METHODS ==================
    
    private XPath createXPathInstance() {
        XPath xpath = this.xPathFactory.newXPath();
        xpath.setNamespaceContext(new HtdNamespaceContext());
        return xpath;
    }

    /**
     * Returns namespace URI for namespace registered in HumanInteractionsManager.
     * @param prefix     The xml namespace prefix.
     * @return namespace Namespace URI or null if it does not exist for a given prefix.
     */
    public String getNamespaceURI(String prefix) {
        return this.xmlNamespaces == null ? null : this.xmlNamespaces.get(prefix);
    }

}
