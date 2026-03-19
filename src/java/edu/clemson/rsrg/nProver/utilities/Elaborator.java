/*
 * Elaborator.java
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
package edu.clemson.rsrg.nProver.utilities;

import edu.clemson.rsrg.absyn.expressions.Exp;
import edu.clemson.rsrg.absyn.expressions.mathexpr.AbstractFunctionExp;
import edu.clemson.rsrg.absyn.expressions.mathexpr.QuantExp;
import edu.clemson.rsrg.nProver.registry.CongruenceClassRegistry;
import edu.clemson.rsrg.nProver.utilities.theorems.ElaborationRule;
import edu.clemson.rsrg.nProver.utilities.theorems.RuleInstance;
import edu.clemson.rsrg.nProver.utilities.treewakers.RegisterAntecedent;
import edu.clemson.rsrg.treewalk.TreeWalker;

import java.util.*;

/**
 * <p>
 * Encapsulates the elaboration and matching logic used by the nProver.
 * </p>
 *
 * @author 2025-2026 Rose-Hulman Insitute of Technology Team
 *
 * @version 1.0
 */
public class Elaborator {

    private final CongruenceClassRegistry myRegistry;
    private final Map<String, Integer> myExpLabels;
    private final List<String> myMappings;
    private final boolean myDebug;

    /**
     * <p>
     * Creates an {@code Elaborator} bound to the given registry and symbol maps. All three objects are shared by
     * reference
     * </p>
     *
     * @param registry
     *            The live congruence class registry for the current VC.
     * @param expLabels
     *            Mapping from symbol strings to their integer labels.
     * @param mappings
     *            Inverse of {@code expLabels} — index is the label, value is the symbol.
     */
    public Elaborator(CongruenceClassRegistry registry, Map<String, Integer> expLabels, List<String> mappings,
            boolean debug) {
        myRegistry = registry;
        myExpLabels = expLabels;
        myMappings = mappings;
        myDebug = debug;
    }

    private ArrayList<RuleInstance> elaborate(List<ElaborationRule> rules) {
        int ruleCounter = 0;
        ArrayList<RuleInstance> result = new ArrayList<>();

        for (ElaborationRule elaborationRule : rules) {
            Map<Exp, Integer> variableBindings = new HashMap<>();
            ruleCounter++;

            for (Exp precursor : elaborationRule.getPrecursorClauses()) {
                int matchedCluster = -1;
                if (precursor.toString().matches("[0-9]+") || precursor.toString().matches("Empty_String")) {
                    matchedCluster = myExpLabels.getOrDefault(precursor.toString(), -1);
                } else if (!myExpLabels.containsKey(precursor.getTopLevelOperator())) {
                    if (myDebug) {
                        System.out
                                .println("The key " + precursor.getTopLevelOperator() + " does not exist in expLabels");
                    }
                } else if (!myRegistry.isRegistryLabel(myExpLabels.get(precursor.getTopLevelOperator()))) {
                    debugLog("Operator is not registered. Skipping.");
                } else if (precursor instanceof AbstractFunctionExp) {

                    int operator = myExpLabels.get(precursor.getTopLevelOperator());
                    int currentCCAccessor = 0;
                    boolean firstLoop = true;
                    HashSet<Integer> visited = new HashSet<>();

                    while (firstLoop || !myRegistry.isVarietyMaximal(operator, currentCCAccessor)) {
                        // Loop through the congruence classes

                        currentCCAccessor = firstLoop ? myRegistry.firstCCAccessorForTreeNodeLabel(operator)
                                : myRegistry.advanceCClassAccessor(operator, currentCCAccessor);
                        firstLoop = false;

                        if (visited.contains(currentCCAccessor))
                            break;
                        visited.add(currentCCAccessor);

                        if (!myRegistry.isMinimalVCCDesignator(operator, currentCCAccessor))
                            continue;

                        matchedCluster = ccMatchesExpression(precursor, currentCCAccessor, operator, variableBindings);
                        if (matchedCluster != -1)
                            break;
                    }
                }

                if (matchedCluster != -1) {
                    debugLog("[Rule #" + ruleCounter + "] \u001B[42m Matched! \u001B[49m :" + precursor);
                    result.add(new RuleInstance(variableBindings, elaborationRule, matchedCluster));
                } else {
                    debugLog("[Rule #" + ruleCounter + "] \u001B[41m Not Matched \u001B[49m :" + precursor);
                }
                debugLog(myRegistry.toPrettyString(myMappings));
            }
        }

        return result;
    }

    private int ccMatchesExpression(Exp needToMatch, int currentCCAccessor, int operator,
            Map<Exp, Integer> variableBindings) {

        // Congruence Class matches the Exp

        // A cluster's argument is a single CC, so no need to loop through those or use
        // a variety at this point
        int currentClusterAccessor = myRegistry.getFirstClusterAccessorForCC(currentCCAccessor, operator);

        HashSet<Integer> visited = new HashSet<>();
        do { // Loop through the clusters in the stand
             // If we've made it this far, then we have at least one cluster with the correct
             // root node
            if (visited.contains(currentClusterAccessor))
                break;
            visited.add(currentClusterAccessor);

            Map<Exp, Integer> tempBindings = new HashMap<>();

            List<Integer> clusterArgs = myRegistry
                    .getArgumentsList(myRegistry.getCongruenceCluster(currentClusterAccessor));
            List<Exp> subExpressions = needToMatch.getSubExpressions();

            if (clusterArgs.size() != subExpressions.size())
                continue; // If the # of args don't match, then this is not the cluster we're looking
                          // for

            boolean matchedAllArgs = true;
            for (int i = 0; i < subExpressions.size(); i++) {
                Exp subExp = subExpressions.get(i);
                if (!(subExp instanceof AbstractFunctionExp)) {
                    // Base case: leaf node
                    int arg = clusterArgs.get(subExpressions.size() - i - 1);
                    if (!matchLeafNodes(subExp, arg)) {
                        matchedAllArgs = false;
                        break;
                    } else {
                        tempBindings.put(subExp, arg);
                    }
                } else {
                    boolean matchedThisSubExp = false;
                    int subExpOperator = myExpLabels.get(subExp.getTopLevelOperator());

                    for (int arg : clusterArgs) {
                        int clusterArgOperator = myRegistry.getCongruenceCluster(arg).getTreeNodeLabel();
                        if (subExpOperator != clusterArgOperator)
                            continue;

                        matchedThisSubExp = ccMatchesExpression(subExp, arg, subExpOperator, variableBindings) != -1;
                        if (matchedThisSubExp) // We found a match! No need to check the rest of
                            // the clusters
                            break;
                    }

                    matchedAllArgs = matchedThisSubExp;
                    if (!matchedThisSubExp) // None of this cluster's arguments matched subExp.
                        // Therefore this is not
                        // the cluster we're looking for.
                        break;
                }
            }

            if (matchedAllArgs) {// All of the arguments in this cluster matched all of the arguments in our
                // expression!
                variableBindings.putAll(tempBindings);
                return currentClusterAccessor;
            }

            currentClusterAccessor = myRegistry.advanceClusterAccessor(operator, currentClusterAccessor);

        } while (!myRegistry.isStandMaximal(operator, currentClusterAccessor));

        // If we've reached this point, we looped through the entire stand without
        // matching a cluster.
        return -1;
    }

    private boolean matchLeafNodes(Exp subExp, int arg) {
        String expString = subExp.toString();
        int expInt = myExpLabels.getOrDefault(expString, -1);

        boolean isInt;
        try { // Horrible code because RegEx hates us
            Integer.parseInt(expString);
            isInt = true;
        } catch (Exception e) {
            isInt = false;
        }

        if (expString.equals("Empty_String") || isInt) {
            if (expInt == -1)
                return false;
            if (myRegistry.getFirstClusterAccessorForCC(arg, expInt) == -1)
                return false;
        }

        return true; // variables always match
    }

    private void applyRules(List<RuleInstance> ruleInstances) {
        for (RuleInstance rule : ruleInstances) {
            Exp resultant = rule.getResultantClause();
            if (resultant instanceof QuantExp) {
                resultant = ((QuantExp) resultant).getBody();
            }
            String topOp = resultant.getTopLevelOperator();
            if (topOp.equals("=") || topOp.equals("<=")) {
                addToRegistry(resultant, rule.isForConsequent());
            }
        }
    }

    private void addToRegistry(Exp resultant, boolean isForConsequent) {
        // we'll need to add the resultant to the correct side(s) of the registry
        // if(isFromAntencedent) {
        TreeWalker.visit(new RegisterAntecedent(myRegistry, myExpLabels, myExpLabels.size(), myMappings), resultant);
        // } else {
        // TreeWalker.visit(new RegisterSuccedent(myRegistry, myExpLabels,
        // myExpLabels.size(), myMappings), resultant);
        // }
    }

    public final void debugLog(Object log) {
        if (myDebug) {
            System.out.println(log);
        }
    }

    public void elaborateAndApply(List<ElaborationRule> rules) {
        applyRules(elaborate(rules));
    }
}
