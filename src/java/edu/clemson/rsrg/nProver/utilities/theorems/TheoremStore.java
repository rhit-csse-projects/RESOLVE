/*
 * TheoremStore.java
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
import edu.clemson.rsrg.nProver.utilities.treewakers.AbstractRegisterSequent;
import edu.clemson.rsrg.typeandpopulate.entry.TheoremEntry;
import edu.clemson.rsrg.typeandpopulate.query.EntryTypeQuery;
import edu.clemson.rsrg.typeandpopulate.symboltables.MathSymbolTable;
import edu.clemson.rsrg.typeandpopulate.symboltables.ModuleScope;
import java.util.*;

public final class TheoremStore implements TheoremManager {

    private final Map<TheoremEntry, Set<String>> theoremToOps;
    private final Set<String> opStrings;

    public TheoremStore(ModuleScope scope) {
        Objects.requireNonNull(scope, "scope");
        // Query all theorems once
        List<TheoremEntry> programTheorems = scope
                .query(new EntryTypeQuery<>(TheoremEntry.class, MathSymbolTable.ImportStrategy.IMPORT_RECURSIVE,
                        MathSymbolTable.FacilityStrategy.FACILITY_INSTANTIATE));

        this.theoremToOps = new LinkedHashMap<>(programTheorems.size());
        this.opStrings = new HashSet<>();

        // Build indices
        for (TheoremEntry te : programTheorems) {
            Set<String> opStrings = toOperatorStrings(te.getOperators());
            this.opStrings.addAll(opStrings);
            theoremToOps.put(te, opStrings);
        }
    }

    /** Convert a set of Exp operators to their canonical string form used by the prover's labeling. */
    private static Set<String> toOperatorStrings(Set<Exp> ops) {
        if (ops == null || ops.isEmpty()) {
            return Collections.emptySet();
        }
        // Use LinkedHashSet to preserve deterministic iteration order
        Set<String> res = new LinkedHashSet<>(ops.size());
        for (Exp e : ops) {
            if (e != null) {
                res.add(e.toString());
            }
        }
        return res;
    }

    private Set<String> getAllExpStrings(Exp exp) {
        Set<String> expStrings = new HashSet<>();
        expStrings.add(exp.toString());
        System.out.println(exp);
        for (Exp subExp : exp.getSubExpressions()) {
            expStrings.addAll(getAllExpStrings(subExp));
            System.out.println(subExp);
        }
        return expStrings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (TheoremEntry theoremEntry : theoremToOps.keySet()) {
            sb.append("Name: ");
            sb.append(theoremEntry.getName());
            sb.append("\n");
            sb.append(theoremEntry.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<TheoremEntry> getRelevantTheorems(List<Exp> expressions) {
        // The following line is the only thing from TheoremSelector that did anything.
        // It seems to exist to filter out expressions by their operators, but we seem to be doing that anyways.
        // What's worse is that the unit tests show that this line just removes all the expressions
        // List<Exp> filteredList = expressions.stream().filter(exp -> opStrings.contains(this.getAllExpStrings(exp)))
        // .collect(Collectors.toList());
        Set<TheoremEntry> theorems = new HashSet<>();
        for (Exp expr : expressions) {
            Set<String> exprStrings = getAllExpStrings(expr);
            for (TheoremEntry theorem : theoremToOps.keySet()) {
                Set<String> ops = theoremToOps.get(theorem);
                // TODO show that this isn't masking a bug (yell at Carson if it is)
                if (ops != null) {
                    ops.removeIf((exp) -> !opStrings.contains(exp));
                    if (exprStrings.containsAll(ops)) {
                        theorems.add(theorem);
                    }
                }
            }
        }
        return theorems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Integer> getExpLabels() {
        // NM: 0, 1 are spared for <= (1), = (2), etc., the list can expand with more
        // reflexive operators
        // preload <=, = into the map
        Map<String, Integer> expLabels = new LinkedHashMap<>();
        expLabels.put("<=", AbstractRegisterSequent.OP_LESS_THAN_OR_EQUALS);
        expLabels.put("=", AbstractRegisterSequent.OP_EQUALS);
        int i = 3;
        for (String opString : opStrings) {
            expLabels.put(opString, i);
            i++;
        }
        return expLabels;
    }
}
