/*
 * CCRegGetFunctionTest.java
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
public class CCRegGetFunctionTest {

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
        // Register a and b
        int a = myRegistry.registerCluster(1);
        int b = myRegistry.registerCluster(2);

        // Register = with a and b as arguments
        myRegistry.appendToClusterArgList(a);
        myRegistry.appendToClusterArgList(b);
        myRegistry.registerCluster(3);

        System.out.println(myRegistry.toString());

        assertTrue("= should be registered", myRegistry.checkIfRegistered(3));

        // Check if registry contains "=" with 2 arguments
        assertTrue("Registry should contain '=' with 2 arguments.", myRegistry.containsStructure(3, 2));
    }

}
