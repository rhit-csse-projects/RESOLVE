/*
 * CCRegNoTheoremTest.java
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

import edu.clemson.rsrg.nProver.utilities.treewakers.AbstractRegisterSequent;
import org.junit.Before;
import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertFalse;
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
        myRegistry = new CongruenceClassRegistry(100, 100, 100, 100);
    }

    // ===========================================================
    // Test Methods
    // ===========================================================

    @Test
    public final void testContainsStructure() {
        String[] ops = {"g", "f", "="};

        //Register g and f
        myRegistry.registerCluster(0);
        myRegistry.registerCluster(1);

        //Register = with g and f as arguments
        myRegistry.appendToClusterArgList(0);
        myRegistry.appendToClusterArgList(1);
        myRegistry.registerCluster(2);

        assertTrue("= should be registered", myRegistry.checkIfRegistered(2));

        //Check if registry contains "=" with 2 arguments
        assertTrue("Registry should contain '=' with 2 arguments.", myRegistry.containsStructure(2, 2));
    }

}
