/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.util;

import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Element;


/**
 * XML Functions and constants.
 * @author Witek Wołejszo
 */
public class XmlUtils {

    public static final QName SCHEMA_STRING = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string");
    public static final QName SCHEMA_DOUBLE = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "double");
    public static final QName SCHEMA_BOOLEAN = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "boolean");

    /**
     * Calculates XPath return type based on XMLSchema type.
     * @param type Schema type: string, boolean or double.
     * @return The return type. See {@link XPathConstants}.
     */
    public QName getReturnType(QName type) {
        
        if (type.equals(SCHEMA_STRING)) {
            
            return XPathConstants.STRING;
            
        } if (type.equals(SCHEMA_DOUBLE)) {
        
            return XPathConstants.NUMBER;
            
        } if (type.equals(SCHEMA_BOOLEAN)) {
        
            return XPathConstants.BOOLEAN;
        }
        
        throw new RuntimeException("Cannot map: " + type + " to XPath result type.");
    }
    
    public Object getElementByLocalPart(List<Object> any, String localPart) {
        for (Object o : any) {
            Element e = (Element) o;
            if (localPart.equals(e.getLocalName())) {
                return e;
            }
        }
        return null;
    }
    
//  //look for human role
//  //List<Object> x = this.tTask.getPeopleAssignments().getGenericHumanRole().get(0).getValue().getAny();
//  for (JAXBElement<TGenericHumanRole> ghr : this.tTask.getPeopleAssignments().getGenericHumanRole()) {
//      if (humanRoleName.toString().equals(ghr.getName().getLocalPart())) {
//          
//          List<Object> any = ghr.getValue().getAny();
//          for (Object o : any) {
//              Element e = (Element) o;
//              if ("literal".equals(e.getElementName().getLocalPart())) {
//                  //e.
//              }
//          }
//          
//      }
//  }
}
