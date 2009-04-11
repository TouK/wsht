/*
 * Copyright (c) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import jdepend.framework.DependencyConstraint;
import jdepend.framework.JDepend;
import jdepend.framework.JavaPackage;

import org.h2.command.ddl.Analyze;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.log.Log;

import static org.junit.Assert.*;

/**
 * 
 * @author Witek Wołejszo
 */
public class ArchitectureTest {

    private JDepend jdepend;
    private JavaPackage service;
    private JavaPackage exceptions;
    private JavaPackage model;
    private JavaPackage dao;
    private JavaPackage daoImpl;
    private JavaPackage spec;
    private JavaPackage ws;

    @Before
    public void setUp() throws IOException {
        
        jdepend = new JDepend();
        jdepend.addDirectory("target/classes");
        
        service = jdepend.addPackage("pl.touk.humantask");
        exceptions = jdepend.addPackage("pl.touk.humantask.exceptions");
        model = jdepend.addPackage("pl.touk.humantask.model");
        dao = jdepend.addPackage("pl.touk.humantask.dao");
        daoImpl = jdepend.addPackage("pl.touk.humantask.dao.impl");
        spec = jdepend.addPackage("pl.touk.humantask.spec");
        ws = jdepend.addPackage("pl.touk.humantask.ws");

    }

    //@Test
    public void testPackageCycle() {
        
        jdepend.analyze();
        assertFalse("Packages contain cycles", jdepend.containsCycles());
    }
    
    @Test
    public void testPackageAbstractness() {
        
        jdepend.analyze();
        assertEquals(1, dao.abstractness(), 0);
    }

    /**
     * This test fails if any package dependency other than those declared in the dependency constraint are detected.
     */
    //@Test
    public void testBadPackageDependencies() {

        //TODO how?
    }

}
