/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.spec;

import org.example.ws_ht.THumanInteractions;
import org.w3c.dom.Document;

public class HumanInteractions {

    // private THumanInteractions tHumanInteractions;
    private Document document;
    private String md5;

    private HumanInteractions() {
    }

    HumanInteractions(THumanInteractions tHumanInteractions, Document document, String md5) {
        super();
        this.setDocument(document);
        this.md5 = md5;
    }

    public String getMd5() {
        return md5;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }

}
