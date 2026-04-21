/*
 * RuleInstance.java
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
import edu.clemson.rsrg.absyn.expressions.mathexpr.ClusterExp;
import edu.clemson.rsrg.typeandpopulate.entry.TheoremEntry;

import java.util.HashMap;
import java.util.Map;

public class RuleInstance {
    private final Exp resultantClause;
    private final boolean forConsequent;
    private final int counter;
    private final String sourceTheoremName;
    private final String sourceModuleName;

    public RuleInstance(Map<Exp, Integer> argBindings, ElaborationRule rule, int counter) {
        Map<Exp, Exp> argReplacements = new HashMap<>();
        for (Exp replacee : argBindings.keySet())
            argReplacements.put(replacee, new ClusterExp(argBindings.get(replacee)));
        this.resultantClause = rule.getResultantClause().substitute(argReplacements);
        this.forConsequent = rule.forConsequent();
        this.counter = counter;
        sourceModuleName = rule.getSourceModuleName();
        sourceTheoremName = rule.getSourceTheoremName();

    }

    public boolean isForConsequent() {
        return forConsequent;
    }

    public Exp getResultantClause() {
        return resultantClause;
    }

    public String getCounter() {
        return Integer.toString(counter);
    }

    public String getSourceTheoremName() {
        return sourceTheoremName;
    }

    public String getSourceModuleName() {
        return sourceModuleName;
    }

}
