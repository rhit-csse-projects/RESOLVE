/*
 * DebuggerHelper.java
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
package edu.clemson.rsrg.misc;

public class DebuggerHelper {
    private static boolean myDebug = false;

    public static void debugLog(Object log) {
        if (myDebug) {
            System.out.println(log);
        }
    }

    public static void setDebug(boolean debug) {
        myDebug = debug;
    }
}
