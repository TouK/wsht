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

import pl.touk.humantask.exceptions.HTConfigurationException;
import pl.touk.humantask.exceptions.HTException;

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
    public void testGetTaskDefinitionByName() throws HTException {
        TaskDefinition taskDefinition = taskManager.getTaskDefinition("ApproveClaim");
        assertNotNull(taskDefinition);
    }

    @Test(expected = HTConfigurationException.class)
    public void testTaskDefinitionNotFound() {
        taskManager.getTaskDefinition("JKR");
    }

    /**
     * Sprawdzenie, czy w przypadku duplikacji nazwy tasku konstruktor managera wyrzuci wyjątek.
     *
     * @throws HTException
     */
    @Test(expected = HTException.class)
    public void testNonUniqueTaskDefinitionsNameException() throws HTException {
        TestUtil.createHumanInteractionsManager("testHtdDuplicateTaskDefinition.xml");
    }
}