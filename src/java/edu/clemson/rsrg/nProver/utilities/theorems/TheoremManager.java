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
    List<TheoremEntry> getRelevantTheorems(List<Exp> expressions);
}
