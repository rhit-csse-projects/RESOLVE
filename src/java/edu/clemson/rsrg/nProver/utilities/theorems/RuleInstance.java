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

import java.util.HashMap;
import java.util.Map;

public class RuleInstance {
    private Exp resultantClause;
    private boolean forConsequent;
    private int precursorAccessor;

    public RuleInstance(Map<Exp, Integer> argBindings, ElaborationRule rule, int precursorAccessor) {
        this.precursorAccessor = precursorAccessor;
        Map<Exp, Exp> argReplacements = new HashMap<>();
        for (Exp replacee : argBindings.keySet())
            argReplacements.put(replacee, new ClusterExp(argBindings.get(replacee)));
        this.resultantClause = rule.getResultantClause().substitute(argReplacements);
        this.forConsequent = rule.forConsequent();
    }

    public boolean isForConsequent() {
        return forConsequent;
    }

    public Exp getResultantClause() {
        return resultantClause;
    }

    public int getPrecursorAccessor() {
        return precursorAccessor;
    }
}
