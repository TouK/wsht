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

import org.example.ws_ht.TOrganizationalEntity;
import org.example.ws_ht.api.TAttachment;
import org.example.ws_ht.api.TAttachmentInfo;
import org.example.ws_ht.api.TComment;
import org.example.ws_ht.api.TStatus;
import org.example.ws_ht.api.TTask;
import org.example.ws_ht.api.TTaskAbstract;
import org.example.ws_ht.api.TTaskQueryResultSet;
import org.example.ws_ht.api.wsdl.IllegalAccessFault;
import org.example.ws_ht.api.wsdl.IllegalArgumentFault;
import org.example.ws_ht.api.wsdl.IllegalOperationFault;
import org.example.ws_ht.api.wsdl.IllegalStateFault;
import org.example.ws_ht.api.wsdl.RecipientNotAllowed;
import org.example.ws_ht.api.wsdl.TaskOperations;
import org.example.ws_ht.api.xsd.IllegalState;
import org.example.ws_ht.api.xsd.TTime;
import org.springframework.beans.factory.annotation.Configurable;

import pl.touk.humantask.HumanTaskServicesInterface;
import pl.touk.humantask.dao.TaskDao;
import pl.touk.humantask.exceptions.HTIllegalArgumentException;
import pl.touk.humantask.exceptions.HTIllegalOperationException;
import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.model.GenericHumanRole;
import pl.touk.humantask.model.Task;
import pl.touk.humantask.model.Task.TaskType;
import pl.touk.humantask.model.Task.TaskTypes;

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
public class TaskOperationsImpl implements TaskOperations {

    private TaskDao taskDao;

    /**
     * Implementation of WH-HT services.
     */
    private HumanTaskServicesInterface services;
    /**
     * Security context used to retrieve implicit user information.
     */
    private SecurityContextInterface securityContext;

    public void activate(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // String username = Session.getUserName();
        // TODO Auto-generated method stub
    }

    public void addAttachment(String identifier, String name, String accessType, Object attachment) throws IllegalArgumentFault, IllegalStateFault,
            IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void addComment(String identifier, String text) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
    }

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

        } catch (HumanTaskException xHT) {
            this.translateIllegalArgumentException(xHT);
            this.translateIllegalStateException(xHT);
            this.translateIllegalAccessException(xHT);
        } catch (NumberFormatException xNF) {
            throw new IllegalArgumentFault("Task identifier must be a number.","Id: " + identifier);
        }
    }

    public void complete(String identifier, Object taskData) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void delegate(String identifier, TOrganizationalEntity organizationalEntity) throws RecipientNotAllowed, IllegalArgumentFault, IllegalStateFault,
            IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void deleteAttachments(String identifier, String attachmentName) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void deleteFault(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void deleteOutput(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void fail(String identifier, String faultName, Object faultData) throws IllegalArgumentFault, IllegalStateFault, IllegalOperationFault,
            IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void forward(String identifier, TOrganizationalEntity organizationalEntity) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public List<TAttachmentInfo> getAttachmentInfos(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
        return null;
    }

    public List<TAttachment> getAttachments(String identifier, String attachmentName) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
        return null;
    }

    public List<TComment> getComments(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getInput(String identifier, String part) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
        return null;
    }

    public List<TTaskAbstract> getMyTaskAbstracts(String taskType, String genericHumanRole, String workQueue, List<TStatus> status, String whereClause,
            String createdOnClause, Integer maxTasks) throws IllegalArgumentFault, IllegalStateFault {
        // TODO Auto-generated method stub
        return null;
    }

    public List<TTask> getMyTasks(String taskType, String genericHumanRole, String workQueue, List<TStatus> status, String whereClause, String createdOnClause,
            Integer maxTasks) throws IllegalArgumentFault, IllegalStateFault {
        try {
            return translateTaskAPI(this.services.getMyTasks(this.securityContext.getLoggedInUser().getUsername(), TaskTypes.valueOf(taskType),
                    GenericHumanRole.valueOf(genericHumanRole), workQueue, translateStatusAPI(status), whereClause, createdOnClause, maxTasks));
        } catch (HumanTaskException xHT) {
            this.translateIllegalStateException(xHT);
            this.translateIllegalArgumentException(xHT);
            return null;
        }
    }

    private void translateIllegalStateException(HumanTaskException xHT) throws IllegalStateFault {
        if (xHT instanceof pl.touk.humantask.exceptions.HTIllegalStateException) {
            IllegalState state = new IllegalState();

            state.setStatus(translateStatusAPI(((pl.touk.humantask.exceptions.HTIllegalStateException)xHT).getExceptionInfo()));
            throw new IllegalStateFault(xHT.getMessage(), state, xHT);
        }
    }

    private void translateIllegalAccessException(HumanTaskException xHT) throws IllegalAccessFault {
        if (xHT instanceof pl.touk.humantask.exceptions.HTIllegalAccessException) {
            throw new IllegalAccessFault(xHT.getMessage(), ((pl.touk.humantask.exceptions.HTIllegalAccessException)xHT).getExceptionInfo(), xHT);
        }
    }

    private void translateIllegalOperationException(HumanTaskException xHT) throws IllegalOperationFault {
        if (xHT instanceof pl.touk.humantask.exceptions.HTIllegalOperationException) {
            throw new IllegalOperationFault(xHT.getMessage(), ((HTIllegalOperationException) xHT).getExceptionInfo(), xHT);
        }
    }

    private void translateIllegalArgumentException(HumanTaskException xHT) throws IllegalArgumentFault {
        if (xHT instanceof pl.touk.humantask.exceptions.HTIllegalArgumentException) {
            throw new IllegalArgumentFault(xHT.getMessage(), ((pl.touk.humantask.exceptions.HTIllegalArgumentException) xHT).getExceptionInfo(), xHT);
        }
    }

    private void translateRecipientNotAllowedException(HumanTaskException xHT) throws RecipientNotAllowed {
        if (xHT instanceof pl.touk.humantask.exceptions.RecipientNotAllowedException) {
            throw new RecipientNotAllowed(xHT.getMessage(), ((pl.touk.humantask.exceptions.RecipientNotAllowedException) xHT).getExceptionInfo(), xHT);
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
     * @param task  input task object
     * @return      output task object
     */
    private TTask translateOneTaskAPI(Task task) {
        TTask ttask = new TTask();

        ttask.setId(Long.toString(task.getId()));
        ttask.setTaskType(TaskType.TASK.toString());
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
        ttask.setCreatedBy(task.getCreatedBy());
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

    public Object getOutput(String identifier, String part) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
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
        } catch (HumanTaskException xHT) {
            this.translateIllegalArgumentException(xHT);
            return null;
        }
    }

    public void nominate(String identifier, TOrganizationalEntity organizationalEntity) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
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

        } catch (HumanTaskException xHT) {
            translateIllegalArgumentException(xHT);
            translateIllegalStateException(xHT);
            translateIllegalAccessException(xHT);
        }
    }

    public void remove(String identifier) throws IllegalArgumentFault, IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void resume(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void setFault(String identifier, String faultName, Object faultData) throws IllegalArgumentFault, IllegalStateFault, IllegalOperationFault,
            IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void setGenericHumanRole(String identifier, String genericHumanRole, TOrganizationalEntity organizationalEntity) throws IllegalArgumentFault,
            IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void setOutput(String identifier, String part, Object taskData) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void setPriority(String identifier, BigInteger priority) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void skip(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalOperationFault, IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void start(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
       try {
            services.startTask(translateTaskIdentifier(identifier),securityContext.getLoggedInUser().getUsername());
        } catch (HumanTaskException xHT) {
            translateIllegalArgumentException(xHT);
            translateIllegalStateException(xHT);
            translateIllegalAccessException(xHT);
        }
    }

    public void stop(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void suspend(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void suspendUntil(String identifier, TTime time) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub
    }

    public void getFault(String identifier, Holder<String> faultName, Holder<Object> faultData) throws IllegalAccessFault, IllegalStateFault,
            IllegalArgumentFault, IllegalOperationFault {
        // TODO Auto-generated method stub
    }

    public void setServices(HumanTaskServicesInterface services) {
        this.services = services;
    }

    public HumanTaskServicesInterface getServices() {
        return this.services;
    }

    public void setSecurityContext(SecurityContextInterface securityContext) {
        this.securityContext = securityContext;
    }

    public SecurityContextInterface getSecurityContext() {
        return this.securityContext;
    }

    protected TaskDao getTaskDao() {
        return this.taskDao;
    }

    public void setTaskDao(TaskDao taskDao) {
        this.taskDao = taskDao;
    }
}