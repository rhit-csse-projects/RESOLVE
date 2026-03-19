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
import edu.clemson.rsrg.typeandpopulate.entry.TheoremEntry;
import edu.clemson.rsrg.typeandpopulate.query.EntryTypeQuery;
import edu.clemson.rsrg.typeandpopulate.symboltables.MathSymbolTable;
import edu.clemson.rsrg.typeandpopulate.symboltables.ModuleScope;
import java.util.*;

public final class TheoremStore {

    private final Map<TheoremEntry, Set<String>> theoremToOps;
    private final Set<String> opStrings;

    public TheoremStore(ModuleScope scope) {
        Objects.requireNonNull(scope, "scope");
        // Query all theorems once
        List<TheoremEntry> programTheorems = scope
                .query(new EntryTypeQuery<>(TheoremEntry.class, MathSymbolTable.ImportStrategy.IMPORT_RECURSIVE,
                        MathSymbolTable.FacilityStrategy.FACILITY_INSTANTIATE));

        this.theoremToOps = new LinkedHashMap<>(programTheorems.size());
        this.opStrings = new LinkedHashSet<>();

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

    /**
     * {@inheritDoc}
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (TheoremEntry theoremEntry : theoremToOps.keySet()) {
            sb.append("Name: ");
            sb.append(theoremEntry.getName());
            sb.append("\n");
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns all theorems who only contain operators that are in the set.
     */
    public Set<TheoremEntry> getRelevantTheoremsByOperators(Set<String> knownOperators) {
        Set<TheoremEntry> result = new HashSet<>();
        for (Map.Entry<TheoremEntry, Set<String>> entry : theoremToOps.entrySet()) {
            Set<String> ops = new HashSet<>(entry.getValue());
            ops.removeIf(op -> !opStrings.contains(op));
            ops.remove("=");
            ops.remove("implies");
            ops.remove(">=");
            ops.remove("<=");
            ops.remove("or");
            ops.remove("and");
            if (knownOperators.containsAll(ops)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
}
