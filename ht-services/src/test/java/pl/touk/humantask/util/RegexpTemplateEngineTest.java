/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import pl.touk.humantask.TemplateEngine;

/**
 * Tests for {@link RegexpTemplateEngine}.
 * @author Witek Wołejszo
 */
public class RegexpTemplateEngineTest {

    @Test
    public void mergeTest1() {
        
        TemplateEngine te = new RegexpTemplateEngine();
        String r1 = te.merge("Raz dwa trzy.", null);
        
        Assert.assertEquals("Raz dwa trzy.", r1);
        
        Map<String, Object> pp = new HashMap<String, Object>();
        
        pp.put("Raz", "1");
        String r2 = te.merge("$Raz$ dwa trzy.", pp);
        Assert.assertEquals("1 dwa trzy.", r2);
        
        pp.put("dwa", "2");
        String r3 = te.merge("$Raz$ $dwa$ trzy.", pp);
        Assert.assertEquals("1 2 trzy.", r3);
        
        pp.put("trzy", "3");
        String r4 = te.merge("$Raz$ $dwa$ $trzy$.", pp);
        Assert.assertEquals("1 2 3.", r4);
    }
    
    @Test
    public void mergeTest2() {
        
        TemplateEngine te = new RegexpTemplateEngine();
        
        Map<String, Object> pp = new HashMap<String, Object>();
        pp.put("euroAmount", Double.valueOf(1));
        pp.put("firstname", "jan");
        pp.put("lastname", "kowalski");
        
        String r1 = te.merge("Approve the insurance claim for €$euroAmount$ on behalf of $firstname$ $lastname$", pp);
        Assert.assertEquals("Approve the insurance claim for €1.0 on behalf of jan kowalski", r1);
    }
    
    @Test
    public void removeTest1() {
        TemplateEngine te = new RegexpTemplateEngine();
        Map<String, Object> pp = new HashMap<String, Object>();
        //no x in pp
        String r1 = te.merge("?IF-x?bla bla bla?ENDIF-x?", pp);
        Assert.assertEquals("", r1);
    }
    
    @Test
    public void combinedTest1() {
        TemplateEngine te = new RegexpTemplateEngine();
        Map<String, Object> pp = new HashMap<String, Object>();
        pp.put("y", "1");
        String r1 = te.merge("?IF-x?bla bla bla?ENDIF-x?$y$", pp);
        Assert.assertEquals("1", r1);
    }
    
    @Test
    public void mergeTestNoPresentationValue() {
        
        TemplateEngine te = new RegexpTemplateEngine();
        
        Map<String, Object> pp = new HashMap<String, Object>();
        String r1 = te.merge("$Raz$ dwa $trzy$.", pp);
        
        Assert.assertEquals("error:Raz dwa error:trzy.", r1);
    }
    
}
