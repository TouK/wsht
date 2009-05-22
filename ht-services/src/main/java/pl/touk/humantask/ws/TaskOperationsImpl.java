/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */
package pl.touk.humantask.ws;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import pl.touk.humantask.model.htd.TOrganizationalEntity;
import pl.touk.humantask.model.ws.TAttachment;
import pl.touk.humantask.model.ws.TAttachmentInfo;
import pl.touk.humantask.model.ws.TComment;
import pl.touk.humantask.model.ws.TStatus;
import pl.touk.humantask.model.ws.TTask;
import pl.touk.humantask.model.ws.TTaskAbstract;
import pl.touk.humantask.model.ws.TTaskQueryResultSet;

import org.springframework.beans.factory.annotation.Configurable;

import pl.touk.humantask.HumanTaskServices;
import pl.touk.humantask.dao.TaskDao;

import pl.touk.humantask.exceptions.HTException;
import pl.touk.humantask.exceptions.HTIllegalArgumentException;
import pl.touk.humantask.exceptions.HTIllegalOperationException;

import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.model.Task.TaskTypes;
import pl.touk.humantask.ws.api.IllegalState;
import pl.touk.humantask.ws.api.TTime;

/**
 * Implementation of WS-HT API.
 * Operations are executed by end users, i.e. actual or potential owners. The identity of
 * the user is implicitly passed when invoking any of the operations listed in the table
 * below. The participant operations listed below only apply to tasks unless explicitly
 * noted otherwise. The authorization column indicates people of which roles are
 * authorized to perform the operation. Stakeholders of the task are not mentioned
 * explicitly. They have the same authorization rights as business administrators.
 *
 * @author Witek Wołejszo
 */
@WebService
@Configurable
public class TaskOperationsImpl implements TaskOperationsInterface {

    private TaskDao taskDao;

    /**
     * Implementation of WH-HT services.
     */
    private HumanTaskServices services;

    /**
     * Security context used to retrieve implicit user information.
     */
    private SecurityContextInterface securityContext;

    //@LogMethodEntranceInfo
    public void claim(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        try {
            if (null == identifier) {
                throw new pl.touk.humantask.exceptions.HTIllegalArgumentException("Must specific a Task id.","Id");
            }

            Task task = this.taskDao.fetch(Long.valueOf(identifier));

            if (null == task) {
                throw new pl.touk.humantask.exceptions.HTIllegalArgumentException("Task not found.","Id: " + identifier);
            }

            this.services.claimTask(translateTaskIdentifier(identifier),this.securityContext.getLoggedInUser().getUsername());

        } catch (HTException xHT) {
            this.translateIllegalArgumentException(xHT);
            this.translateIllegalStateException(xHT);
            this.translateIllegalAccessException(xHT);
        } catch (NumberFormatException xNF) {
            throw new IllegalArgumentFault("Task identifier must be a number.","Id: " + identifier);
        }
    }

    public List<TTask> getMyTasks(String taskType, String genericHumanRole, String workQueue, List<TStatus> status, String whereClause, String createdOnClause,
            Integer maxTasks) throws IllegalArgumentFault, IllegalStateFault {
        try {
            return translateTaskAPI(this.services.getMyTasks(this.securityContext.getLoggedInUser().getUsername(), TaskTypes.valueOf(taskType),
                    GenericHumanRole.valueOf(genericHumanRole), workQueue, translateStatusAPI(status), whereClause, null, createdOnClause, maxTasks, 0));
        } catch (HTException xHT) {
            this.translateIllegalStateException(xHT);
            this.translateIllegalArgumentException(xHT);
            return null;
        }
    }

    private void translateIllegalStateException(HTException xHT) throws IllegalStateFault {
        if (xHT instanceof pl.touk.humantask.exceptions.HTIllegalStateException) {
            IllegalState state = new IllegalState();

            state.setStatus(translateStatusAPI(((pl.touk.humantask.exceptions.HTIllegalStateException)xHT).getExceptionInfo()));
            throw new IllegalStateFault(xHT.getMessage(), state, xHT);
        }
    }

    private void translateIllegalAccessException(HTException xHT) throws IllegalAccessFault {
        if (xHT instanceof pl.touk.humantask.exceptions.HTIllegalAccessException) {
            throw new IllegalAccessFault(xHT.getMessage(), ((pl.touk.humantask.exceptions.HTIllegalAccessException)xHT).getExceptionInfo(), xHT);
        }
    }

    private void translateIllegalOperationException(HTException xHT) throws IllegalOperationFault {
        if (xHT instanceof pl.touk.humantask.exceptions.HTIllegalOperationException) {
            throw new IllegalOperationFault(xHT.getMessage(), ((HTIllegalOperationException) xHT).getExceptionInfo(), xHT);
        }
    }

    private void translateIllegalArgumentException(HTException xHT) throws IllegalArgumentFault {
        if (xHT instanceof pl.touk.humantask.exceptions.HTIllegalArgumentException) {
            throw new IllegalArgumentFault(xHT.getMessage(), ((pl.touk.humantask.exceptions.HTIllegalArgumentException) xHT).getExceptionInfo(), xHT);
        }
    }

    private void translateRecipientNotAllowedException(HTException xHT) throws RecipientNotAllowed {
        if (xHT instanceof pl.touk.humantask.exceptions.HTRecipientNotAllowedException) {
            throw new RecipientNotAllowed(xHT.getMessage(), ((pl.touk.humantask.exceptions.HTRecipientNotAllowedException) xHT).getExceptionInfo(), xHT);
        }
    }

    private Long translateTaskIdentifier(String identifier) throws HTIllegalArgumentException {
        if (null == identifier) {
            throw new pl.touk.humantask.exceptions.HTIllegalArgumentException("Must specific a Task id.","Id");
        }

        try {
            return Long.valueOf(identifier);
        } catch (NumberFormatException xNF) {
            throw new HTIllegalArgumentException("Task identifier must be a number.", "Id: " + identifier);
        }
    }

    /**
     * Translates a single task to TTask.
     *
     * @param task  The input task object.
     * @return      The Human Task WebService API task object.
     */
    private TTask translateOneTaskAPI(Task task) {
        TTask ttask = new TTask();

        ttask.setId(Long.toString(task.getId()));
        ttask.setTaskType("TASK");
        /*
        ttask.setName(task.getName());
         */
        ttask.setStatus(this.translateStatusAPI(task.getStatus()));
        /*
        ttask.setPriority(task.getPriority());
         */
        //ttask.setTaskInitiator(task.getCreatedBy());
        /*ttask.setTaskStakeholders(task.getTaskStakeholders());
        ttask.setPotentialOwners(task.getPotentialOwners());
        ttask.setBusinessAdministrators(task.getBusinessAdministrators());
        ttask.setActualOwner(task.getActualOwner());
        ttask.setNotificationRecipients(task.getNotificationRecipients());
         */
        //ttask.setCreatedOn(task.getCreatedOn());
        ttask.setCreatedBy(task.getCreatedBy().toString());
        ttask.setActivationTime(task.getActivationTime());
        ttask.setExpirationTime(task.getExpirationTime());
        ttask.setIsSkipable(task.isSkippable());
        /*ttask.setHasPotentialOwners(task.getHasPotentialOwners());
        ttask.setStartByExists(task.getStartByExists());
        ttask.setCompleteByExists(task.getCompleteByExists());
        ttask.setPresentationName(task.getPresentationName());
        ttask.setPresentationSubject(task.getPresentationSubject());
        ttask.setRenderingMethodExists(task.getRenderingMethodExists());
        ttask.setHasOutput(task.getHasOutput());
         */

        //TODO implement cjeck
        //ttask.setHasFault(null != task.getFault());
        ttask.setHasFault(false);

        ttask.setHasAttachments(!task.getAttachments().isEmpty());
        //ttask.setHasComments(!task.getComments().isEmpty());

        ttask.setEscalated(task.isEscalated());
        return ttask;
    }

    private List<TTask> translateTaskAPI(List<Task> in) {
        List<TTask> result = new ArrayList<TTask>();
        for (Task task : in) {
            result.add(this.translateOneTaskAPI(task));
        }
        return result;
    }

    private List<Task.Status> translateStatusAPI(List<TStatus> in) {
        List<Task.Status> result = new ArrayList<Task.Status>();
        for (TStatus status : in) {
            result.add(Task.Status.fromValue(in.toString()));
        }

        return result;
    }

    private TStatus translateStatusAPI(Task.Status in) {
        return TStatus.fromValue(in.toString());
    }

    /**
     * Gets task information by a given identifier.
     * 
     * @param identifier task identifier as a number
     * @return task info
     * @throws org.example.ws_ht.api.wsdl.IllegalArgumentFault the number format is invalid or the task does not exist
     */
    public TTask getTaskInfo(String identifier) throws IllegalArgumentFault {
        try {
            return this.translateOneTaskAPI(this.services.getTaskInfo(Long.parseLong(identifier)));
        } catch (HTException xHT) {
            this.translateIllegalArgumentException(xHT);
            return null;
        }
    }

    public TTaskQueryResultSet query(String selectClause, String whereClause, String orderByClause, Integer maxTasks, Integer taskIndexOffset)
        throws IllegalArgumentFault, IllegalStateFault {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Releases a task by its identifier.
     * @param identifier task identifier
     * @throws org.example.ws_ht.api.wsdl.IllegalArgumentFault  Identifier is invalid
     * @throws org.example.ws_ht.api.wsdl.IllegalStateFault     The current state of the task doesn't allow to release it
     * @throws org.example.ws_ht.api.wsdl.IllegalAccessFault    The logged in user has no right to release the task
     */
    public void release(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
       try {
            if (null == identifier) {
                throw new pl.touk.humantask.exceptions.HTIllegalArgumentException("Must specific a Task id.","Id");
            }

            services.releaseTask(translateTaskIdentifier(identifier),securityContext.getLoggedInUser().getUsername());

        } catch (HTException xHT) {
            translateIllegalArgumentException(xHT);
            translateIllegalStateException(xHT);
            translateIllegalAccessException(xHT);
        }
    }

    public void start(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
       try {
            services.startTask(translateTaskIdentifier(identifier),securityContext.getLoggedInUser().getUsername());
        } catch (HTException xHT) {
            translateIllegalArgumentException(xHT);
            translateIllegalStateException(xHT);
            translateIllegalAccessException(xHT);
        }
    }
    
    public void activate(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public void addAttachment(String identifier, String name, String accessType, Object attachment) throws IllegalAccessFault, IllegalStateFault,
            IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public void addComment(String identifier, String text) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public void complete(String identifier, Object taskData) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public void delegate(String identifier, TOrganizationalEntity organizationalEntity) throws IllegalAccessFault, IllegalStateFault, RecipientNotAllowed,
            IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public void deleteAttachments(String identifier, String attachmentName) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public void deleteFault(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public void deleteOutput(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public void fail(String identifier, String faultName, Object faultData) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault,
            IllegalOperationFault {
        // TODO Auto-generated method stub
        
    }

    public void forward(String identifier, TOrganizationalEntity organizationalEntity) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public List<TAttachmentInfo> getAttachmentInfos(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public List<TAttachment> getAttachments(String identifier, String attachmentName) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public List<TComment> getComments(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public void getFault(String identifier, Holder<String> faultName, Holder<Object> faultData) throws IllegalAccessFault, IllegalStateFault,
            IllegalArgumentFault, IllegalOperationFault {
        // TODO Auto-generated method stub
        
    }

    public Object getInput(String identifier, String part) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public List<TTaskAbstract> getMyTaskAbstracts(String taskType, String genericHumanRole, String workQueue, List<TStatus> status, String whereClause,
            String createdOnClause, Integer maxTasks) throws IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getOutput(String identifier, String part) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getRendering(Object identifier, QName renderingType) throws IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public List<QName> getRenderingTypes(Object identifier) throws IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public String getTaskDescription(String identifier, String contentType) throws IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public void nominate(String identifier, TOrganizationalEntity organizationalEntity) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public void remove(String identifier) throws IllegalAccessFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public void resume(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public void setFault(String identifier, String faultName, Object faultData) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault,
            IllegalOperationFault {
        // TODO Auto-generated method stub
        
    }

    public void setGenericHumanRole(String identifier, String genericHumanRole, TOrganizationalEntity organizationalEntity) throws IllegalAccessFault,
            IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public void setOutput(String identifier, String part, Object taskData) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public void setPriority(String identifier, BigInteger priority) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public void skip(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault, IllegalOperationFault {
        // TODO Auto-generated method stub
        
    }

    public void stop(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public void suspend(String identifier) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    public void suspendUntil(String identifier, TTime time) throws IllegalAccessFault, IllegalStateFault, IllegalArgumentFault {
        // TODO Auto-generated method stub
        
    }

    //Getters & Setters 

    public void setServices(HumanTaskServices services) {
        this.services = services;
    }

    public HumanTaskServices getServices() {
        return this.services;
    }

    public void setSecurityContext(SecurityContextInterface securityContext) {
        this.securityContext = securityContext;
    }

    public SecurityContextInterface getSecurityContext() {
        return this.securityContext;
    }

}