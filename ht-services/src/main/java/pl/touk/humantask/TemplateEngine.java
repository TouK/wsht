/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask;

import java.util.Map;

import pl.touk.humantask.util.RegexpTemplateEngine;

/**
 * Responsible for merging presentation template with presentation parameters. 
 * Default implementation {@link RegexpTemplateEngine} works according to specification. But other
 * more flexible implementations can be made.
 *
 * @author Witek Wołejszo
 */
public interface TemplateEngine {

    /**
     * Merges template with presentationParameterValues. 
     * @param template The template String.
     * @param presentationParameterValues Presentation parameters.
     * @return The template string with filled in values.
     */
    String merge(String template, Map<String, Object> presentationParameterValues);

}