/*
 * ElaborationRules.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ElaborationRules {

    private Collection<TheoremEntry> myRelevantTheorems;

    // private Exp resultantExpression;

    private List<ElaborationRule> myElaborationRules;

    // constructor
    public ElaborationRules(Collection<TheoremEntry> relevantTheorems) {
        myRelevantTheorems = relevantTheorems;
        myElaborationRules = createElaborationRules();
    }

    /**
     * Creates a list of elaboration rules out of a list of relevant theorems
     */
    private List<ElaborationRule> createElaborationRules() {
        List<ElaborationRule> elaborationRules = new ArrayList<>();
        List<Exp> myTheoremExpressions;
        // list of sub sub expressions for theorems with one clause
        List<Exp> myTheoremSubExpressions;
        // pick each relevant theorem one at a time
        for (TheoremEntry t : myRelevantTheorems) {
            // get the sub expressions out of t
            myTheoremExpressions = t.getAssertion().getSubExpressions();
            // System.out.println(myTheoremExpressions);

            // System.out.println("The size of the clause is: " +
            // myTheoremExpressions.size());
            // for one expression theorem, things will be different
            if (myTheoremExpressions.size() == 1) {
                // break down the expression further it is assumed it will be at index 0
                myTheoremSubExpressions = myTheoremExpressions.get(0).getSubExpressions();

                for (Exp exp : myTheoremSubExpressions) {
                    List<Exp> copyOfMyTheoremSubExpressions = myTheoremExpressions.get(0).getSubExpressions();
                    // x1*x2 = x2 * x1 will create two rules with each sub exp becoming the
                    // precursor
                    if (isDeterministic(copyOfMyTheoremSubExpressions, exp)) {
                        // exp here has to be the whole theorem assertion and not only part of
                        // the expression
                        ElaborationRule rule = new ElaborationRule(copyOfMyTheoremSubExpressions, t.getAssertion(),
                                t.isAntecedent());
                        elaborationRules.add(rule);
                    }
                }

            } else {
                // build the elaboration rule out of each expression by making it a resultant
                // and the reset precursors
                for (Exp te : myTheoremExpressions) {
                    List<Exp> copyOfTheoremExpressions = t.getAssertion().getSubExpressions();
                    // check if the rule will be deterministic, and for the moment, if not
                    // deterministic ignore it
                    if (isDeterministic(copyOfTheoremExpressions, te)) {
                        // System.out.println("It was determinant");
                        ElaborationRule rule = new ElaborationRule(copyOfTheoremExpressions, t.getAssertion(),
                                t.isAntecedent());
                        elaborationRules.add(rule);
                    }
                }
            }
        }
        return elaborationRules;
    }

    /**
     * <p>
     * If all variables in the resultant clause can be found in the precursor clauses, then they are deterministic
     * </p>
     * {@return} true, if the inputs are deterministic
     *
     * @param theoremExpressionList
     *            The precursor clauses.
     * @param resultantExpression
     *            The resultant clause.
     */
    public boolean isDeterministic(List<Exp> theoremExpressionList, Exp resultantExpression) {
        // remove the resultant expression from the precursor expressions
        theoremExpressionList.remove(resultantExpression);
        // get the resultant sub expressions (will be only constants and variables)
        List<Exp> resultantSubExpressions = resultantExpression.getSubExpressions();
        // Declare the set that will collect all variables in the precursor expressions
        Set<Exp> collectionOfPrecursorVars;

        // collect all the variables in the precursor expressions
        collectionOfPrecursorVars = collectVariables(theoremExpressionList);

        // check if collection of precursor vars contain all e's, if so return true
        for (Exp e : resultantSubExpressions) {
            if (!collectionOfPrecursorVars.contains(e))
                return false; // a variable in the resultant is not in the precursors
        }
        return true; // all variables in the resultant are in the precursors

    }

    /**
     * <p>
     * Gets all the elaboration rules created.
     * </p>
     *
     * @return a {@link List} of {@link ElaborationRule}
     */
    public List<ElaborationRule> getMyElaborationRules() {
        return myElaborationRules;
    }

    /**
     * <p>
     * Returns the set of all variables in the precursor expressions
     * </p>
     *
     * @return a {@link Set} of {@link Exp}
     *
     * @param precursorExpList
     *            The list of precursor expressions
     */
    public static Set<Exp> collectVariables(List<Exp> precursorExpList) {
        Set<Exp> setOfPrecursorVars = new HashSet<>();
        for (Exp e : precursorExpList) {
            for (Exp ve : e.getSubExpressions()) {
                if (ve.getClass().getSimpleName().equals("VarExp")) {
                    setOfPrecursorVars.add(ve);
                }
            }
        }
        return setOfPrecursorVars;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Elaboration Rules: \n");
        for (int i = 0; i < myElaborationRules.size(); i++) {
            sb.append(i + 1);
            sb.append(": ");
            sb.append(myElaborationRules.get(i).toString());
        }
        return sb.toString();
    }
}
