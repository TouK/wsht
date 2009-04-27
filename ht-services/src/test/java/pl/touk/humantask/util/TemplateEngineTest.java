/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link TemplateEngine}.
 * @author Witek Wołejszo
 */
public class TemplateEngineTest {

    @Test
    public void mergeTest() {
        
        TemplateEngine te = new TemplateEngine();
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
    public void mergeTestNoPresentationValue() {
        
        TemplateEngine te = new TemplateEngine();
        
        Map<String, Object> pp = new HashMap<String, Object>();
        String r1 = te.merge("$Raz$ dwa $trzy$.", pp);
        
        Assert.assertEquals("$Raz$ dwa $trzy$.", r1);
    }
    
}
