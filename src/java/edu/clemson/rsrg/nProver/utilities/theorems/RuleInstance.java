package edu.clemson.rsrg.nProver.utilities.theorems;

import edu.clemson.rsrg.absyn.expressions.Exp;

import java.util.Map;

public class RuleInstance {
    private Map<Exp, Integer> argBindings;
    private ElaborationRule rule;

    private int precursorAccessor;

    public RuleInstance(Map<Exp, Integer> argBindings, ElaborationRule rule, int precursorAccessor) {
        this.argBindings = argBindings;
        this.rule = rule;
        this.precursorAccessor = precursorAccessor;
    }

    public Map<Exp, Integer> getArgBindings() {
        return argBindings;
    }

    public ElaborationRule getRule() {
        return rule;
    }

    public int getPrecursorAccessor() {
        return precursorAccessor;
    }
}
