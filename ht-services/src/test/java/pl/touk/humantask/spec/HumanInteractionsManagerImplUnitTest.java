/*
 * Copyright (c) (2005 - )2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import pl.touk.humantask.exceptions.HumanTaskException;
import pl.touk.humantask.util.TestUtil;

import java.util.List;
import java.util.ArrayList;

/**
 * {@link HumanInteractionsManagerImpl} class unit tests.
 *
 * @author <a href="mailto:jkr@touk.pl">Jakub Kurlenda</a>
 */
public class HumanInteractionsManagerImplUnitTest {

    private HumanInteractionsManagerInterface taskManager;

    @Before
    public void setUp() throws Exception {

        this.taskManager = TestUtil.createHumanInteractionsManager("htd1.xml", "testHtd1.xml");
    }

    @Test
    public void testGetTaskDefinitionByName() throws HumanTaskException {
        TaskDefinition taskDefinition = taskManager.getTaskDefinition("ApproveClaim");
        assertNotNull(taskDefinition);
    }

    @Test(expected = HumanTaskException.class)
    public void testTaskDefinitionNotFound() throws HumanTaskException {
        taskManager.getTaskDefinition("JKR");
    }

    /**
     * Sprawdzenie, czy w przypadku duplikacji nazwy tasku konstruktor managera wyrzuci wyjątek.
     *
     * @throws HumanTaskException
     */
    @Test(expected = HumanTaskException.class)
    public void testNonUniqueTaskDefinitionsNameException() throws HumanTaskException {
        TestUtil.createHumanInteractionsManager("htd2.xml");
    }
}