/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.spec;

import java.util.List;

import org.w3c.dom.Document;

/**
 * Provides access methods to Human Interactions document defined
 * using http://www.example.org/WS-HT schema.
 * <p/>
 * Instance of this class can be obtained using {@link HumanInteractionsFactoryBean}.
 *
 * @author Witek Wołejszo
 */
public class HumanInteractions {

    private Document document;
    //private String humanInteractionsDefinitionKey;

    private List<TaskDefinition> taskDefinitions;

//    private List<Group> groupList;

    public void setTaskDefinitions(List<TaskDefinition> taskDefinitions) {
        this.taskDefinitions = taskDefinitions;
    }

//    public void setGroupList(List<Group> groupList) {
//        this.groupList = groupList;
//    }

    /**
     * Private constructor to prevent instantiation.
     */
    private HumanInteractions() {
    }

    /**
     * Constructor called by {@link HumanInteractionsManagerInterface implementation}.
     *
     * @param document the human interactions DOM document
     */
    HumanInteractions(Document document) {
        super();
        this.setDocument(document);
    }

//    public String getDefinitionKey() {
//        return humanInteractionsDefinitionKey;
//    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }

    public List<TaskDefinition> getTaskDefinitions() {
        return taskDefinitions;
    }

//    public List<Group> getGroupList() {
//        return groupList;
//    }


}
