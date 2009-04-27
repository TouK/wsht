/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class used to merge presentation parameters into template strings.
 * @author Witek Wołejszo
 */
public class TemplateEngine {
    
    private final Log log = LogFactory.getLog(TemplateEngine.class);

    /**
     * Replaces occurrences of "$key$" in a template string with values provided in presentationParameters. 
     * @param template The template String.
     * @param presentationParameters Presentation parameters.
     * @return The template string with filled in values.
     */
    public String merge(String template, Map<String, Object> presentationParameters) {
    
        log.info("merge");
        
        Pattern p = Pattern.compile("\\$[A-Za-z]*\\$");
        Matcher m = p.matcher(template);
        
        while (m.find() == true) {
            
            String key = m.group().replace("$", "");
            String substitution = (String) ((presentationParameters == null) ? null : presentationParameters.get(key));
            
            if (substitution == null) {
                
                log.warn("Cannot find presentation parameter: " + key);
                
            } else {
                
                log.debug("Replacing: " + m.group() + " with: " + substitution);
                template = m.replaceFirst(substitution);
                m = p.matcher(template);
            }
        }
        
        log.info("Returning: " + template);
        return template;
    }
    
}
