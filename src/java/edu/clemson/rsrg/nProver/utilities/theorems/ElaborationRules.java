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
import edu.clemson.rsrg.absyn.expressions.Exp.Side;
import edu.clemson.rsrg.absyn.expressions.mathexpr.AbstractFunctionExp;
import edu.clemson.rsrg.absyn.expressions.mathexpr.QuantExp;
import edu.clemson.rsrg.misc.DebuggerHelper;
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
        HashSet<ElaborationRule> elaborationRules = new LinkedHashSet<>();
        // list of sub sub expressions for theorems with one clause
        // pick each relevant theorem one at a time
        for (TheoremEntry t : myRelevantTheorems) {

            Exp thisTheorem = t.getAssertion();// myTheoremExpressions.getFirst();

            if (thisTheorem instanceof QuantExp) {
                thisTheorem = ((QuantExp) thisTheorem).getBody();
            }

            List<Exp> myTheoremSubExpressions = thisTheorem.getSubExpressions();

            if (thisTheorem.getTopLevelOperator().equals("=")) {
                // If the top level operator is =, create two ERs, one in each direction
                Exp firstPart = myTheoremSubExpressions.getFirst();
                Exp lastPart = myTheoremSubExpressions.getLast();

                List<Exp> firstPartPrecursors = processPrecursors(firstPart);
                if (isDeterministic(firstPartPrecursors, lastPart)) {
                    elaborationRules.add(mkRule(firstPartPrecursors, t, thisTheorem, "="));
                }

                if (lastPart.getSubExpressions().isEmpty()) { // This theorem is not reversible
                    continue;
                }

                List<Exp> lastPartPrecursors = processPrecursors(lastPart);
                if (isDeterministic(lastPartPrecursors, firstPart)) {
                    elaborationRules.add(mkRule(lastPartPrecursors, t, thisTheorem, "="));
                }

            } else if (thisTheorem.getTopLevelOperator().equals("implies")) {
                Exp firstPart = myTheoremSubExpressions.getFirst();
                Exp lastPart = myTheoremSubExpressions.getLast();

                List<Exp> firstPartPrecursors = processPrecursors(firstPart);
                disjunctiveNormalForm(firstPartPrecursors, lastPart, t, elaborationRules);
            } else if (thisTheorem.getTopLevelOperator().equals("<=")) {
                // If the top level operator is =, create two ERs, one in each direction
                Exp firstPart = myTheoremSubExpressions.getFirst();
                Exp lastPart = myTheoremSubExpressions.getLast();

                List<Exp> firstPartPrecursors = processPrecursors(firstPart);
                if (isDeterministic(firstPartPrecursors, lastPart)) {
                    elaborationRules.add(mkRule(firstPartPrecursors, t, thisTheorem, "="));
                }
            } else if (thisTheorem.getTopLevelOperator().equals("or")) {
                disjunctiveNormalForm(new ArrayList<>(), thisTheorem, t, elaborationRules);
            } else {
                DebuggerHelper.debugLog(
                        "WARNING: Unable to process ER with top level operator " + thisTheorem.getTopLevelOperator());
            }
        }
        return new ArrayList<>(elaborationRules);
    }

    private void disjunctiveNormalForm(List<Exp> firstPartPrecursors, Exp lastPart, TheoremEntry t,
            HashSet<ElaborationRule> elaborationRules) {
        // if resultant has ands, break apart into more DNFs
        // (might be incorrect, but just refactoring current logic)
        if (lastPart.getTopLevelOperator().equals("and")) {
            for (Exp subResultant : lastPart.getSubExpressions()) {
                disjunctiveNormalForm(firstPartPrecursors, subResultant, t, elaborationRules);
            }
            return;
        }
        List<Exp> exps = new ArrayList<>();
        firstPartPrecursors.forEach(e -> {
            Exp newExp = e.clone();
            newExp.setElaborationTag(Exp.ElaborationTag.NEGATIVE);
            exps.add(newExp);
        });
        // or theorems are already DNF so just make them positive
        if (lastPart.getTopLevelOperator().equals("or")) {
            for (Exp subExp : lastPart.getSubExpressions()) {
                Exp cloned = subExp.clone();
                cloned.setElaborationTag(Exp.ElaborationTag.POSITIVE);
                exps.add(cloned);
            }
        } else {
            Exp newLastPart = lastPart.clone();
            newLastPart.setElaborationTag(Exp.ElaborationTag.POSITIVE);
            exps.add(newLastPart);
        }
        for (Exp expressionForSuccedent : exps) {
            List<Exp> precursors = new ArrayList<>();
            for (Exp expressionForAntecedent : exps) {
                // This is intentional. We're trying to ensure that these 2 expressions are not referring to the same
                // object in memory.
                if (expressionForSuccedent != expressionForAntecedent) {
                    Exp cloned = expressionForAntecedent.clone();
                    if (expressionForAntecedent.getElaborationTag() == Exp.ElaborationTag.POSITIVE) {
                        cloned.setSide(Side.SUCCEDENT);
                    } else {
                        cloned.setSide(Side.ANTECEDENT);
                    }
                    precursors.add(cloned);
                }

            }
            Exp clonedSuccedent = expressionForSuccedent.clone();
            if (expressionForSuccedent.getElaborationTag() == Exp.ElaborationTag.POSITIVE) {
                clonedSuccedent.setSide(Side.ANTECEDENT);
            } else {
                clonedSuccedent.setSide(Side.SUCCEDENT);
            }
            if (isDeterministic(precursors, clonedSuccedent)) {
                elaborationRules.add(mkRule(precursors, t, clonedSuccedent, "implies"));
            }
        }
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

    /**
     * <p>
     * The operator should make a new elaboration rule to be added to the rules list
     * </p>
     *
     * @param precursorExps
     *            all precursor clauses to be added
     * @param t
     *            the theorem entry associated with the rule
     * @param resultant
     *            The resultant clause to be added to the registry once precursors matched
     * @param operator
     *            The top level operator
     *
     * @return the operation returns a new Elaboration Rule to be added to the rules list
     */
    private ElaborationRule mkRule(List<Exp> precursorExps, TheoremEntry t, Exp resultant, String operator) {
        String sourceTheoremName = null;
        String sourceModuleName = null;
        if (t.getName() != null)
            sourceTheoremName = t.getName();
        if (t.getSourceModuleIdentifier().toString() != null)
            sourceModuleName = t.getSourceModuleIdentifier().toString();
        if (precursorExps.size() == 1 && precursorExps.getFirst().getSubExpressions().isEmpty()) {
            List<Exp> l = new ArrayList<>();
            l.add(t.getAssertion());
            return new ElaborationRule(l, resultant, sourceTheoremName, sourceModuleName, operator);
        }
        return new ElaborationRule(precursorExps, resultant, sourceTheoremName, sourceModuleName, operator);
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
        // probably because of the location stuff in Exps
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
