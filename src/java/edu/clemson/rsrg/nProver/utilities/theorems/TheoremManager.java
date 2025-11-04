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

import java.util.Set;

public interface TheoremManager {
    Set<TheoremEntry> getRelevantTheorems(Set<Exp> expressions);
}
