/*
 * TheoremManager.java
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
package edu.clemson.rsrg.nProver.utilities.theorems;

import edu.clemson.rsrg.absyn.expressions.Exp;
import edu.clemson.rsrg.typeandpopulate.entry.TheoremEntry;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TheoremManager {
    /**
     * <p>
     * Given a list of expressions, this method returns a list of relevant theorems based on the operators found.
     * Requires: true
     * </p>
     * <p>
     * Restores: expressions
     * </p>
     * <p>
     * Ensures: getRelevantTheorems = all theorems whose operators are a subset of the provide set (list).
     * </p>
     * {@param} expressions: The expressions to be analyzed for relevant theorems. {@return}A list of relevant theorems.
     */
    Set<TheoremEntry> getRelevantTheorems(List<Exp> expressions);

    /**
     * <p>
     * Returns a map of operators to their numbers. <= & = are always 1 & 2, respectively. Requires: true
     * </p>
     * <p>
     * Ensures getExpLabels = all operators mapped to their unique integer labels.
     * </p>
     */
    Map<String, Integer> getExpLabels();
}
