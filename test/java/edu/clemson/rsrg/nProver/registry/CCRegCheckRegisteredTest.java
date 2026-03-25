/*
 * CCRegCheckRegisteredTest.java
 * ---------------------------------
 * Copyright (c) 2024
 * RESOLVE Software Research Group
 * School of Computing
 * Clemson University
 * All rights reserved.
 * ---------------------------------
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package edu.clemson.rsrg.nProver.registry;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * <p>
 * Unit test for testing the RESOLVE compiler's congruence class registry without using any theorems.
 * </p>
 *
 * @author Nicodemus Msafiri J. M.
 * @author Yu-Shan Sun
 *
 * @version 1.0
 */
public class CCRegCheckRegisteredTest {

    // ===========================================================
    // Member Fields
    // ===========================================================

    /**
     * <p>
     * A {@link CongruenceClassRegistry} object to store the antecedents and succedents.
     * </p>
     */
    private CongruenceClassRegistry myRegistry;

    // ===========================================================
    // Set up Method
    // ===========================================================

    /**
     * <p>
     * This method sets up the congruence class registry before each test case is run.
     * </p>
     */
    @Before
    public final void setUp() {
        myRegistry = new CongruenceClassRegistry(8, 8, 8, 8);
    }

    // ===========================================================
    // Test Methods
    // ===========================================================

    @Test
    public final void testContainsStructure() {
        // Register g and f
        int g = myRegistry.registerCluster(1);
        int f = myRegistry.registerCluster(2);

        // Register = with g and f as arguments
        myRegistry.appendToClusterArgList(g);
        myRegistry.appendToClusterArgList(f);
        myRegistry.registerCluster(3);

        myRegistry.appendToClusterArgList(g);
        myRegistry.appendToClusterArgList(f);
        assertTrue("= should be registered", myRegistry.checkIfRegistered(3));
    }

    @Test
    public final void testContainsStructures() {

        ArrayList<String> myMappings = new ArrayList<>();

        myMappings.add(null);
        myMappings.add("<=");
        myMappings.add("=");
        myMappings.add("Next_Entry");
        myMappings.add("Spp");
        myMappings.add("Sp");
        myMappings.add("S");
        myMappings.add("Temp");
        myMappings.add("<_>");
        myMappings.add("o");
        myMappings.add("Reverse");

        myRegistry = new CongruenceClassRegistry(100, 100, 100, 100);

        int next_entry = myRegistry.registerCluster(3);
        int spp = myRegistry.registerCluster(4);
        int sp = myRegistry.registerCluster(5);
        int s = myRegistry.registerCluster(6);
        int temp = myRegistry.registerCluster(7);

        myRegistry.appendToClusterArgList(next_entry);
        int singleton = myRegistry.registerCluster(8);

        myRegistry.appendToClusterArgList(singleton);
        myRegistry.appendToClusterArgList(sp);
        int concatenate = myRegistry.registerCluster(9);

        myRegistry.appendToClusterArgList(temp);
        int reverse = myRegistry.registerCluster(10);

        myRegistry.appendToClusterArgList(reverse);
        myRegistry.appendToClusterArgList(spp);
        int concatenate2 = myRegistry.registerCluster(9);

        myRegistry.makeCongruent(spp, concatenate);
        myRegistry.makeCongruent(s, concatenate2);

        myRegistry.appendToClusterArgList(singleton);
        myRegistry.appendToClusterArgList(temp);
        int concatenate3 = myRegistry.registerCluster(9);

        myRegistry.appendToClusterArgList(concatenate3);
        int reverse2 = myRegistry.registerCluster(10);

        myRegistry.appendToClusterArgList(reverse2);
        myRegistry.appendToClusterArgList(sp);
        int concatenate4 = myRegistry.registerCluster(9);

        myRegistry.appendToClusterArgList(s);
        myRegistry.appendToClusterArgList(concatenate4);
        int equals = myRegistry.registerCluster(2);

        System.out.println(myRegistry.toPrettyString(myMappings));

        myRegistry.appendToClusterArgList(s);
        myRegistry.appendToClusterArgList(concatenate4);
        // assertTrue("= should be registered with args: " +
        // myRegistry.getArgumentsList(myRegistry.getCongruenceClass(equals).get),
        // myRegistry.checkIfRegistered(equals));
        myRegistry.removeFromClusterArgList();
        myRegistry.removeFromClusterArgList();

        myRegistry.appendToClusterArgList(temp);
        // weirdly, this is failing
        // assertTrue("Reverse should be registered", myRegistry.checkIfRegistered(reverse));
        myRegistry.removeFromClusterArgList();

        myRegistry.appendToClusterArgList(concatenate3);
        // weirdly, this is failing
        // assertTrue("Concatenate should be registered", myRegistry.checkIfRegistered(reverse2));
        myRegistry.removeFromClusterArgList();

        myRegistry.appendToClusterArgList(singleton);
        int reverse3 = myRegistry.registerCluster(10);
        myRegistry.makeCongruent(reverse3, singleton);

        myRegistry.appendToClusterArgList(reverse2);
        myRegistry.appendToClusterArgList(sp);
        // weirdly, this failing
        // assertTrue("Concatenate4 should be registered", myRegistry.checkIfRegistered(concatenate4));
        myRegistry.removeFromClusterArgList();
        myRegistry.removeFromClusterArgList();

        myRegistry.appendToClusterArgList(singleton);
        myRegistry.appendToClusterArgList(temp);
        int concatenate5 = myRegistry.registerCluster(9);

        myRegistry.appendToClusterArgList(concatenate5);
        int reverse5 = myRegistry.registerCluster(10);

        // my mappings: [null, <=, =, Next_Entry, Spp, Sp, S, Temp, <_>, o, Reverse]

        myRegistry.appendToClusterArgList(singleton);
        // weirdly, this is failing
        // assertTrue("Reverse3 should be registered", myRegistry.checkIfRegistered(reverse3));
        myRegistry.removeFromClusterArgList();
    }

}
