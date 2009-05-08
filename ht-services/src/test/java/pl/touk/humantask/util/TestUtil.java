/*
 * Copyright (c) (2005 - )2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import pl.touk.humantask.HumanInteractionsManager;
import pl.touk.humantask.HumanInteractionsManagerImpl;
import pl.touk.humantask.PeopleQuery;
import pl.touk.humantask.exceptions.HTException;
import pl.touk.humantask.model.Assignee;

/*
 * TODO: ADD SHORT DESCRIPTION 
 *
 * @author <a href="mailto:jkr@touk.pl">Jakub Kurlenda</a>
 */
@org.junit.Ignore
public class TestUtil {
    
    /**
     * Helper method for creating HumanInteractionsManager instance from a given fileset.
     *
     * @param htdFiles - files, which contain human interactions definitions.
     * @return
     * @throws HTException
     */
    public static HumanInteractionsManager createHumanInteractionsManager(String... htdFiles) throws HTException {
        
        List<Resource> resources = new ArrayList<Resource>();
        
        for (String htdFile : htdFiles) {
            resources.add(new ClassPathResource(htdFile));
        }
        
        return new HumanInteractionsManagerImpl(resources, new PeopleQuery() {

            public List<Assignee> evaluate(String logicalPeopleGroupName, Map<String, Object> parameters) {
                return new ArrayList<Assignee>();
            }
            
        });
    }
}
