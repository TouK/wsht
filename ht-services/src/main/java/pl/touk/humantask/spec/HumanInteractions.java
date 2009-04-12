/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.spec;

import org.w3c.dom.Document;

/**
 * Provides access methods to Human Interactions document defined
 * using http://www.example.org/WS-HT schema.
 * 
 * Instance of this class can be obtained using {@link HumanInteractionsFactoryBean}.
 *
 * @author Witek Wołejszo
 */
public class HumanInteractions {

    private Document document;
    private String humanInteractionsDefinitionKey;

    /**
     * Private constructor to prevent instantiation.
     */
    private HumanInteractions() {
    }

    /**
     * Constructor called by {@link HumanInteractionsFactoryBean}.
     * 
     * @param document                          the human interactions DOM document
     * @param humanInteractionsDefinitionKey    the key Human Interactions definition can be looked up by  
     */
    HumanInteractions(Document document, String humanInteractionsDefinitionKey) {
        super();
        this.setDocument(document);
        this.humanInteractionsDefinitionKey = humanInteractionsDefinitionKey;
    }

    public String getDefinitionKey() {
        return humanInteractionsDefinitionKey;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }

}
