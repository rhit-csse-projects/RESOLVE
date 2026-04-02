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
import edu.clemson.rsrg.absyn.expressions.mathexpr.AbstractFunctionExp;
import edu.clemson.rsrg.typeandpopulate.entry.TheoremEntry;

import java.util.*;

public class ElaborationRules {

    private final Collection<TheoremEntry> myRelevantTheorems;

    // private Exp resultantExpression;

    private final List<ElaborationRule> myElaborationRules;

    // constructor
    public ElaborationRules(Collection<TheoremEntry> relevantTheorems) {
        myRelevantTheorems = relevantTheorems;
        myElaborationRules = createElaborationRules();
    }

    /**
     * Creates a list of elaboration rules out of a list of relevant theorems
     */
    private List<ElaborationRule> createElaborationRules() {
        HashSet<ElaborationRule> elaborationRules = new HashSet<>();
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
                Exp thisTheorem = myTheoremExpressions.getFirst();
                myTheoremSubExpressions = thisTheorem.getSubExpressions();

                if (thisTheorem.getTopLevelOperator().equals("=")) {
                    // If the top level operator is =, create two ERs, one in each direction
                    Exp firstPart = myTheoremSubExpressions.getFirst();
                    Exp lastPart = myTheoremSubExpressions.getLast();

                    List<Exp> firstPartPrecursors = processPrecursors(firstPart);
                    if (isDeterministic(firstPartPrecursors, lastPart)) {
                        elaborationRules.add(mkRule(firstPartPrecursors, t));
                    }

                    List<Exp> lastPartPrecursors = processPrecursors(lastPart);
                    if (isDeterministic(lastPartPrecursors, firstPart)) {
                        elaborationRules.add(mkRule(lastPartPrecursors, t));
                    }

                } else if (thisTheorem.getTopLevelOperator().equals("implies")) {
                    // If the top level is implies, only create on ER. The inverse is not valid.
                    Exp firstPart = myTheoremSubExpressions.getFirst();

                    List<Exp> firstPartPrecursors = processPrecursors(firstPart);
                    if (isDeterministic(firstPartPrecursors, thisTheorem)) {
                        elaborationRules.add(mkRule(firstPartPrecursors, t));
                    }
                } else if (thisTheorem.getTopLevelOperator().equals("<=")) {
                    // TODO: This should make special ER that automatically proves the VC if it matches
                    // System.out.println("Ignoring <= ER");
                } else {
                    // System.out.println(t);
                    System.err.println("WARNING: Unable to process ER with top level operator "
                            + thisTheorem.getTopLevelOperator());
                }

            } else {
                // build the elaboration rule out of each expression by making it a resultant
                // and the reset precursors
                for (Exp te : myTheoremExpressions) {
                    List<Exp> copyOfTheoremExpressions = t.getAssertion().getSubExpressions();
                    // check if the rule will be deterministic, and for the moment, if not
                    // deterministic ignore it
                    if (isDeterministic(copyOfTheoremExpressions, te))
                        elaborationRules.add(mkRule(copyOfTheoremExpressions, t));
                }
            }
        }
        return new ArrayList<>(elaborationRules);
    }

    private List<Exp> processPrecursors(Exp precursor) {
        if (precursor.getTopLevelOperator().equals("and")) {
            return precursor.getSubExpressions(); // TODO: Investigate if nested ands exist in MT files
        } else {
            List<Exp> output = new ArrayList<>();
            output.add(precursor);
            return output;
        }
    }

    private ElaborationRule mkRule(List<Exp> precursorExps, TheoremEntry t) {
        String sourceTheoremName = null;
        String sourceModuleName = null;
        if (t.getName() != null)
            sourceTheoremName = t.getName();
        if (t.getSourceModuleIdentifier().toString() != null)
            sourceModuleName = t.getSourceModuleIdentifier().toString();
        if (precursorExps.size() == 1 && precursorExps.getFirst().getSubExpressions().isEmpty()) {
            List<Exp> l = new ArrayList<>();
            l.add(t.getAssertion());
            return new ElaborationRule(l, t.getAssertion(), true, sourceTheoremName, sourceModuleName);
        }
        return new ElaborationRule(precursorExps, t.getAssertion(), false, sourceTheoremName, sourceModuleName);
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
        Set<Exp> resultantVars = getAllVars(resultantExpression);
        // Declare the set that will collect all variables in the precursor expressions
        Set<Exp> collectionOfPrecursorVars = new LinkedHashSet<>();

        for (Exp e : theoremExpressionList) {
            collectionOfPrecursorVars.addAll(getAllVars(e));
        }

        // For some reason beyond our understanding, regular containsAll always returns false
        return cursedContainsAll(collectionOfPrecursorVars, resultantVars); // collectionOfPrecursorVars.containsAll(resultantVars);
    }

    public static boolean cursedContainsAll(Set<Exp> precursorVars, Set<Exp> resultantVars) {
        for (Exp resVar : resultantVars) {
            boolean matchedSomething = false;
            for (Exp preVar : precursorVars) {
                if (preVar.equals(resVar)) {
                    matchedSomething = true;
                    break;
                }
            }
            if (!matchedSomething) {
                return false;
            }
        }
        return true;
    }

    public static Set<Exp> getAllVars(Exp theorem) {
        Set<Exp> result = new LinkedHashSet<>();
        if (!(theorem instanceof AbstractFunctionExp)) { // TODO: Don't filter out constants. We should be able to
                                                         // handle a constant being in the resultant
            String expString = theorem.toString();
            boolean isInt;
            try { // Horrible code because RegEx hates us
                Integer.parseInt(expString);
                isInt = true;
            } catch (Exception e) {
                isInt = false;
            }

            if (!expString.equals("Empty_String") && !isInt) {
                result.add(theorem);
            }
        } else {
            for (Exp subExp : theorem.getSubExpressions()) {
                result.addAll(getAllVars(subExp));
            }
        }
        return result;
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Elaboration Rules: \n");
        for (int i = 0; i < myElaborationRules.size(); i++) {
            sb.append("Rule #").append(i + 1);
            sb.append(": ");
            sb.append(myElaborationRules.get(i).toString());
        }
        return sb.toString();
    }
}
