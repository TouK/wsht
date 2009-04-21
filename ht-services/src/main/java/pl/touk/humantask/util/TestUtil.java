/*
 * Copyright (c) (2005 - )2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.util;

import pl.touk.humantask.model.Assignee;
import pl.touk.humantask.spec.HumanInteractionsManagerInterface;
import pl.touk.humantask.spec.HumanInteractionsManagerImpl;
import pl.touk.humantask.spec.PeopleQuery;
import pl.touk.humantask.exceptions.HumanTaskException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
/*
 * TODO: ADD SHORT DESCRIPTION 
 *
 * @author <a href="mailto:jkr@touk.pl">Jakub Kurlenda</a>
 */

public class TestUtil {
    /**
     * Helper method for creating HumanInteractionsManagerInterface instance from a given fileset.
     *
     * @param htdFiles - files, which contain human interactions definitions.
     * @return
     * @throws HumanTaskException
     */
    
    public static HumanInteractionsManagerInterface createHumanInteractionsManager(String... htdFiles) throws HumanTaskException {
        List<Resource> resources = new ArrayList<Resource>();
        for (String htdFile : htdFiles) {
            resources.add(new ClassPathResource(htdFile));
        }
        return new HumanInteractionsManagerImpl(resources, new PeopleQuery() {

            public List<Assignee> evaluate(String logicalPeopleGroupName, Map<String, Object> parameters) {
                return new ArrayList();
            }
            
        });
    }
}
