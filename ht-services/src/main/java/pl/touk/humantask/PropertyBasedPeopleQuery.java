/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;

import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.model.Person;

/**
 * Loads property file. Returns all people listed in property file together with
 * users specified literally.
 * 
 * TODO cannot depend on dao
 * 
 * @author Witek Wołejszo
 * @author Mateusz Lipczyński
 */
public final class PropertyBasedPeopleQuery implements PeopleQuery {

    private final Log log = LogFactory.getLog(PropertyBasedPeopleQuery.class);

    private Resource configuration;
    
    /**
     * 
     * @param logicalPeopleGroupName
     * @param parameters
     * @return
     */
    public List<Assignee> evaluate(String logicalPeopleGroupName, Map<String, Object> parameters) {
        
        log.info("Evaluating members of logical people group: " + logicalPeopleGroupName);
        
        List<Assignee> result = new ArrayList<Assignee>();
        Properties p = new Properties();
        
        try {
            p.load(configuration.getInputStream());
            String value = (String) p.get(logicalPeopleGroupName);
            // parse
            String[] peopleInGroup = value.split(",");
            for (String name : peopleInGroup) {
                result.add(new Person(name));
            }
        } catch (IOException e) {
            // Access error should not affect evaluation TODO: ref to specs
            try {
                
                log.error("Error reading: " + configuration.getURL());
            } catch (IOException e1) {
            } finally {
                
                log.error("Error reading file.");
            }
        }
        return result;
    }

    public void setConfiguration(Resource configuration) {
        this.configuration = configuration;
    }

    public Resource getConfiguration() {
        return configuration;
    }

}
