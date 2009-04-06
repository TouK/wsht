/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.ws;

import java.math.BigInteger;
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
import org.example.ws_ht.api.xsd.TTime;

import pl.touk.humantask.Services;

@WebService
public class TaskOperationsImpl implements TaskOperations {

    private Services services;

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

    public void claim(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub
        return null;
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

    public TTask getTaskInfo(String identifier) throws IllegalArgumentFault {
        // TODO Auto-generated method stub
        return null;
    }

    public void nominate(String identifier, TOrganizationalEntity organizationalEntity) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub

    }

    public TTaskQueryResultSet query(String selectClause, String whereClause, String orderByClause, Integer maxTasks, Integer taskIndexOffset)
            throws IllegalArgumentFault, IllegalStateFault {
        // TODO Auto-generated method stub
        return null;
    }

    public void release(String identifier) throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub

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

    public void setServices(Services services) {
        this.services = services;
    }

    public Services getServices() {
        return services;
    }

    public void getFault(String identifier, Holder<String> faultName, Holder<Object> faultData) throws IllegalAccessFault, IllegalStateFault,
            IllegalArgumentFault, IllegalOperationFault {
        // TODO Auto-generated method stub
        
    }

}
