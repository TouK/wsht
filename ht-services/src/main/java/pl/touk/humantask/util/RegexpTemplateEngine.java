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

import pl.touk.humantask.TemplateEngine;

/**
 * Utility class used to merge presentation parameters into template strings using regexp replace.
 * @author Witek Wołejszo
 */
public class RegexpTemplateEngine implements TemplateEngine {
    
    private final Log log = LogFactory.getLog(RegexpTemplateEngine.class);

    /**
     * Replaces occurrences of "$key$" in a template string with values provided in presentationParameters.
     * Removes blocks starting with ?IF-key? and ending with ?ENDIF-key? if key is not present in presentationParameters.
     * @param template The template String.
     * @param presentationParameterValues Presentation parameters.
     * @return The template string with filled in values.
     */
    public String merge(String template, Map<String, Object> presentationParameterValues) {
         
        Pattern blockPattern = Pattern.compile("\\?IF\\-[A-Za-z0-9]*\\?.*\\?ENDIF\\-[A-Za-z0-9]*\\?");
        Matcher m = blockPattern.matcher(template);
        
        //- remove blocks from template if the key is not in presentationParameterValues.keySet or value is null
        //- remove block markers otherwise
        while (m.find() == true) {
            
            String key = m.group().substring(4).replaceAll("\\?.*$", "");

            if (presentationParameterValues.get(key) == null) {
                template = m.replaceFirst("");
            } else {
                template = template.replace("?IF-" + key + "?", "").replace("?ENDIF-" + key + "?", "");
            }
            
            m = blockPattern.matcher(template);
        }

        Pattern replacePattern = Pattern.compile("\\$[A-Za-z0-9]*\\$");
        m = replacePattern.matcher(template);
        
        while (m.find() == true) {
            
            String key = m.group().replace("$", "");
            Object substitution = ((presentationParameterValues == null) ? null : presentationParameterValues.get(key));
            String substitutionString = (substitution == null) ? "error:" + key : substitution.toString();
            
            if (substitutionString == null) {
                
                log.warn("Cannot find presentation parameter: " + key);
                
            } else {

                template = m.replaceFirst(substitutionString);
                m = replacePattern.matcher(template);
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Returning: " + template);
        }

        return template;
    }
    
}
