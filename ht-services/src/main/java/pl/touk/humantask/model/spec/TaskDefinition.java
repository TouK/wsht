/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model.spec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import pl.touk.humantask.PeopleQuery;
import pl.touk.humantask.TemplateEngine;
import pl.touk.humantask.exceptions.HTException;
import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Group;
import pl.touk.humantask.model.Person;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.model.htd.TDescription;
import pl.touk.humantask.model.htd.TGenericHumanRole;
import pl.touk.humantask.model.htd.TGrouplist;
import pl.touk.humantask.model.htd.TOrganizationalEntity;
import pl.touk.humantask.model.htd.TPresentationParameter;
import pl.touk.humantask.model.htd.TTask;
import pl.touk.humantask.model.htd.TText;
import pl.touk.humantask.model.htd.TUserlist;
import pl.touk.humantask.util.RegexpTemplateEngine;
import pl.touk.humantask.util.XmlUtils;

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
public class TaskDefinition {

    private final Log log = LogFactory.getLog(TaskDefinition.class);
    
    private TemplateEngine templateEngine;

    private final PeopleQuery peopleQuery;

    private final TTask tTask;

    private final boolean instantiable = true;

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
        
        //TODO make it confugurable
        this.templateEngine = new RegexpTemplateEngine();
    }

    /**
     * Returns description of the Task.
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

        return this.templateEngine.merge(descriptionTamplate, presentationParameters).trim();
    }

    /**
     * Returns task's priority. 0 is the highest priority, larger numbers identify lower priorities.
     * @param task  The task priority is evaluated for.
     * @return      Priority or null if it is not specified.
     */
    public Integer getPriority(Task task) {
        
        Validate.notNull(task);
        
        if (this.tTask.getPriority() != null) {
            String priorityXPath = this.tTask.getPriority().getContent().get(0).toString().trim();
            Validate.notNull(priorityXPath);
            
            Double d = (Double) task.evaluateXPath(priorityXPath, XPathConstants.NUMBER);
            
            return d.intValue();
        }
        
        return null;
    }
    
    /**
     * Returns values of Task presentation parameters.
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

            Object o = task.evaluateXPath(parameterXPath, returnType);

            result.put(parameterName, o);
        }

        return result;
    }

    /**
     * Evaluates assignees of generic human role.
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
                    List<Assignee> peopleQueryResult = this.peopleQuery.evaluate(logicalPeopleGroupName, null);
                    evaluatedAssigneeList.addAll(peopleQueryResult);
                }
            }
        }

        //look for human role
        for (JAXBElement<TGenericHumanRole> ghr : this.tTask.getPeopleAssignments().getGenericHumanRole()) {

            if (humanRoleName.toString().equals(ghr.getName().getLocalPart())) {
                
                //get extension element by localPart name
                Element e = (Element) new XmlUtils().getElementByLocalPart(ghr.getValue().getAny(), "literal");
                if (e != null) {
                    
                    Node organizationalEntityNode = e.getFirstChild();
                    
                    try {
                        
                        JAXBContext jaxbContext = JAXBContext.newInstance("pl.touk.humantask.model.htd");
                        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                        //InputStream is = organizationalEntity
                        JAXBElement<TOrganizationalEntity> organizationalEntity = (JAXBElement<TOrganizationalEntity>) unmarshaller.unmarshal(organizationalEntityNode);
                        
                        TGrouplist groupList =  organizationalEntity.getValue().getGroups();
                        if (groupList != null) {
                            for (String group : groupList.getGroup()) {
                                evaluatedAssigneeList.add(new Group(group));
                            }
                        }
                        
                        TUserlist userList =  organizationalEntity.getValue().getUsers();
                        if (userList != null) {
                            for (String user : userList.getUser()) {
                                evaluatedAssigneeList.add(new Person(user));
                            }
                        }
                        
                    } catch (JAXBException e2) {
                        log.error(e2);
                        throw  new HTException("Error evaluating human role for task: " + this.tTask.getName());
                    }
                }
            }
        }

        return evaluatedAssigneeList;
    }

    /**
     * TODO test
     * Returns task's presentation name in a required language.
     * @param lang subject language according ISO, e.g. en-US, pl, de-DE
     * @return name
     */
    public String getName(String lang) {

        Validate.notNull(lang);
        
        List<TText> tTexts = this.tTask.getPresentationElements().getName();
        for (TText x : tTexts) {
            if (lang.equals(x.getLang())) {
                return x.getContent().get(0).toString();
            }
        }
        
        return "error";
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
                subjectTemplate = x.getContent().get(0).toString();
                break;
            }
        }
        
        if (subjectTemplate == null) {
            return "error";
        }
        
        Map<String, Object> presentationParameterValues = task.getPresentationParameterValues();

        return this.templateEngine.merge(subjectTemplate, presentationParameterValues).trim();
    }

    // ================ GETTERS + SETTERS ===================

    public boolean getInstantiable() {
        return this.instantiable;
    }

    public String getTaskName() {
        return this.tTask.getName();
    }

    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    // ================== HELPER METHODS ==================

    /**
     * Returns namespace URI for namespace registered in HumanInteractionsManager.
     * @param prefix     The xml namespace prefix.
     * @return namespace Namespace URI or null if it does not exist for a given prefix.
     */
    public String getNamespaceURI(String prefix) {
        return this.xmlNamespaces == null ? null : this.xmlNamespaces.get(prefix);
    }

}
