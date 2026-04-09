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

import static edu.clemson.rsrg.misc.DebuggerHelper.debugLog;

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
    public Elaborator(CongruenceClassRegistry registry, Map<String, Integer> expLabels, List<String> mappings) {
        myRegistry = registry;
        myExpLabels = expLabels;
        myMappings = mappings;
    }

    private ArrayList<RuleInstance> elaborate(List<ElaborationRule> rules) {
        int ruleCounter = 0;
        ArrayList<RuleInstance> result = new ArrayList<>();

        for (ElaborationRule elaborationRule : rules) {
            Map<Exp, Integer> variableBindings = new HashMap<>();
            ruleCounter++;
            ArrayList<Integer> matchedClusters = new ArrayList<>();
            boolean anyMatched = true; // stops from adding duplicate resultants to the registry

            if (elaborationRule.getPrecursorClauses().size() != 1) {
                continue;
            }
            for (Exp precursor : elaborationRule.getPrecursorClauses()) {
                if (precursor.getTopLevelOperator().equals("=")) {
                    Exp leftSide = precursor.getSubExpressions().getFirst();
                    Exp rightSide = precursor.getSubExpressions().getLast();
                    Map<Exp, Integer> leftBindings = new HashMap<>();
                    Map<Exp, Integer> rightBindings = new HashMap<>();
                    int leftMatched = matchExpression(leftSide, leftBindings, ruleCounter, new HashMap<>());
                    int rightMatched = matchExpression(rightSide, rightBindings, ruleCounter, leftBindings);
                    if (rightMatched != -1 && leftMatched != -1) {
                        int leftCC = myRegistry.getClusterArray()[leftMatched].getIndexToCongruenceClass();
                        int rightCC = myRegistry.getClusterArray()[rightMatched].getIndexToCongruenceClass();
                        if (myRegistry.areCongruent(leftCC, rightCC)) {
                            variableBindings.putAll(rightBindings);
                            variableBindings.putAll(leftBindings);
                            matchedClusters.add(leftMatched);
                            matchedClusters.add(rightMatched);
                        }
                    }
                } else {
                    int matchedCluster = matchExpression(precursor, variableBindings, ruleCounter, new HashMap<>());
                    if (matchedCluster != -1) {
                        matchedClusters.add(matchedCluster);
                    }
                }
                String body = getExpBodyString(precursor);
                if (!matchedClusters.isEmpty()) {
                    debugLog("[Rule #" + ruleCounter + "] \u001B[42m Matched! \u001B[49m :" + body);
                    for (Integer matchedCluster : matchedClusters) {
                        debugLog("[Rule #" + ruleCounter + "]" + " Matched Cluster: " + "CC"
                                + myRegistry.getClusterArray()[matchedCluster].getTreeNodeLabel() + " -> (CR"
                                + matchedCluster + ": "
                                + myMappings.get(myRegistry.getClusterArray()[matchedCluster].getTreeNodeLabel()) + " "
                                + getArgsAsStrings(matchedCluster) + ")");
                    }
                } else {
                    debugLog("[Rule #" + ruleCounter + "] \u001B[41m Not Matched \u001B[49m :" + body);
                }
            }

            if (anyMatched) {
                if (matchedClusters.isEmpty()) {
                    continue;
                }
                result.add(new RuleInstance(variableBindings, elaborationRule));
            }
        }

        return result;
    }

    private int matchExpression(Exp precursor, Map<Exp, Integer> variableBindings, int ruleCounter,
            Map<Exp, Integer> requiredBindings) {
        int matchedCluster = -1;
        if (precursor.toString().matches("[0-9]+") || precursor.toString().matches("Empty_String")) {
            matchedCluster = myExpLabels.getOrDefault(precursor.toString(), -1);
        } else if (!myExpLabels.containsKey(precursor.getTopLevelOperator())) {
            debugLog("\u001B[31m[Rule #" + ruleCounter + " Error]\u001B[0m: " + "The key `\u001B[31m"
                    + precursor.getTopLevelOperator() + "\u001B[0m` does not exist in expLabels");
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

                matchedCluster = ccMatchesExpression(precursor, currentCCAccessor, operator, variableBindings,
                        requiredBindings);
                if (matchedCluster != -1)
                    break;
            }
        }
        return matchedCluster;
    }

    private static String getExpBodyString(Exp precursor) {
        String body = precursor.toString();
        if (precursor instanceof QuantExp) {
            body = ((QuantExp) precursor).getBody().toString();
        }
        return body;
    }

    private int ccMatchesExpression(Exp needToMatch, int currentCCAccessor, int operator,
            Map<Exp, Integer> variableBindings, Map<Exp, Integer> requiredBindings) {

        // Congruence Class matches the Exp

        // A cluster's argument is a single CC, so no need to loop through those or use
        // a variety at this point
        int currentClusterAccessor = myRegistry.getFirstClusterAccessorForCC(currentCCAccessor, operator);

        if (currentClusterAccessor == -1)
            return -1; // this is probably not correct

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
                int arg = clusterArgs.get(subExpressions.size() - i - 1);
                if (!(subExp instanceof AbstractFunctionExp)) {
                    // Base case: leaf node
                    if (!matchLeafNodes(subExp, arg, requiredBindings)) {
                        matchedAllArgs = false;
                        break;
                    } else {
                        tempBindings.put(subExp, arg);
                    }
                } else {
                    int subExpOperator = myExpLabels.get(subExp.getTopLevelOperator());
                    // we don't need to loop through args here, because we are already looping through the args in the
                    // outer for loop
                    int clusterAccessor = myRegistry.getFirstClusterAccessorForCC(arg, subExpOperator);
                    if (clusterAccessor == -1) {
                        matchedAllArgs = false;
                        break;
                    }

                    matchedAllArgs = ccMatchesExpression(subExp, arg, subExpOperator, variableBindings,
                            requiredBindings) != -1;
                    if (!matchedAllArgs)
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

    private boolean matchLeafNodes(Exp subExp, int arg, Map<Exp, Integer> requiredBindings) {
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
            return myRegistry.getFirstClusterAccessorForCC(arg, expInt) != -1;
        } else if (requiredBindings.containsKey(subExp)) {
            int cc = myRegistry.getClusterArray()[requiredBindings.get(subExp)].getIndexToCongruenceClass();
            return cc == arg; // this is a variable that requires an exact match
        } else {
            // Ensure that arg does not contain any cluster in requiredBindings
            for (Integer forbiddenCluster : requiredBindings.values()) {
                if (myRegistry.getClusterArray()[forbiddenCluster].getIndexToCongruenceClass() == arg)
                    return false;
            }
        }

        return true; // variables not in requiredBindings always match
    }

    private void applyRules(List<RuleInstance> ruleInstances) {
        for (RuleInstance rule : ruleInstances) {
            addToRegistry(rule);
        }
    }

    private void addToRegistry(RuleInstance rule) {
        // we'll need to add the resultant to the correct side(s) of the registry
        // if(isFromAntencedent) {
        Exp resultant = rule.getResultantClause();

        debugLog("Trying to add " + resultant + " to the registry");

        int CCDesLeftInitial = myRegistry.remainingCCDesignatorCap();
        TreeWalker.visit(new RegisterAntecedent(myRegistry, myExpLabels, myExpLabels.size(), myMappings), resultant);
        int CCDesLeftLater = myRegistry.remainingCCDesignatorCap();
        debugLog("Resultant: " + resultant + " \u001B[33mAdded " + (CCDesLeftInitial - CCDesLeftLater)
                + " CCs to the Registry\u001B[0m");
        // } else {
        // TreeWalker.visit(new RegisterSuccedent(myRegistry, myExpLabels,
        // myExpLabels.size(), myMappings), resultant);
        // }
    }

    public void elaborateAndApply(List<ElaborationRule> rules) {
        applyRules(elaborate(rules));
    }

    private String getArgsAsStrings(int matchedCluster) {
        List<Integer> args = myRegistry.getArgumentsList(myRegistry.getCongruenceCluster(matchedCluster));
        StringBuilder sb = new StringBuilder();
        for (Integer arg : args) {
            sb.append("CC").append(myRegistry.getCongruenceCluster(arg).getTreeNodeLabel()).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }
}
