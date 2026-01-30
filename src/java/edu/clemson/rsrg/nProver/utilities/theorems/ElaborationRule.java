/*
 * ElaborationRule.java
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

import java.util.List;

public class ElaborationRule {
    private List<Exp> myPrecursorClauses;
    private Exp myResultantClause;
    private boolean fromAntecendent;
    private String mySourceTheoremName;
    private String mySourceModuleName;

    public ElaborationRule(List<Exp> precursorClauses, Exp resultantClause, boolean fromAntecendent,
            String sourceTheoremName, String sourceModuleName) {
        myPrecursorClauses = precursorClauses;
        myResultantClause = resultantClause;
        this.fromAntecendent = fromAntecendent;
        this.mySourceTheoremName = sourceTheoremName;
        this.mySourceModuleName = sourceModuleName;
    }

    public List<Exp> getPrecursorClauses() {
        return myPrecursorClauses;
    }

    public Exp getResultantClause() {
        return myResultantClause;
    }

    /**
     * @return true if this elaboration rule comes from the VC's antecdent, or false if it comes from the VC's
     *         consequent
     */
    public boolean isAntecedent() {
        return fromAntecendent;
    }

    public String getSourceTheoremName() {
        return mySourceTheoremName;
    }

    public String getSourceModuleName() {
        return mySourceModuleName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (mySourceModuleName != null) {
            sb.append("Source Theorem: ");
            sb.append("\u001B[33m");
            sb.append(mySourceTheoremName);
            sb.append("\u001B[0m");
            sb.append(" (from ");
            sb.append("\u001B[34m");
            sb.append(mySourceModuleName);
            sb.append("\u001B[0m");
            sb.append(")\n");
        } else {
            sb.append("No Source Theorem for this Rule \n");
        }
        sb.append("Precursor Clauses: ");
        sb.append(myPrecursorClauses.toString());
        sb.append("\n Resultant Clause: ");
        sb.append(myResultantClause.toString());
        sb.append("\n From Antecedent: ");
        sb.append("\u001B[35m");
        sb.append(fromAntecendent);
        sb.append("\u001B[0m");
        sb.append("\n\n");
        return sb.toString();
    }
}
