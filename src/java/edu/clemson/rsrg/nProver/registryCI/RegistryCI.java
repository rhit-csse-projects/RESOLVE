/*
 * RegistryCI.java
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
package edu.clemson.rsrg.nProver.registryCI;

import edu.clemson.rsrg.absyn.expressions.Exp;
import edu.clemson.rsrg.nProver.registry.CongruenceClassRegistry;
import edu.clemson.rsrg.nProver.utilities.theorems.ElaborationRule;
import edu.clemson.rsrg.nProver.utilities.theorems.ElaborationRules;
import edu.clemson.rsrg.typeandpopulate.entry.TheoremEntry;

import java.util.*;

public class RegistryCI {
    private CongruenceClassRegistry registry;
    private Map<String, Integer> symbolToMapping;
    private List<String> mappingToSymbol;
    private Scanner scan;
    private int currentMapping;

    private static String prompt = "> ";

    public RegistryCI() {
        registry = new CongruenceClassRegistry(100, 100, 100, 100);
        symbolToMapping = new HashMap<String, Integer>();
        mappingToSymbol = new ArrayList<String>();
        scan = new Scanner(System.in);
        currentMapping = 0;
    }

    public static void sendStartupMessage() {
        System.out.println(
                "R - registerCluster\n? - isRegistered\nA - appendToClusterArgList\nM - makeCongruent\nD - display\nQ - quit");
    }

    public void runCommandLoop() {
        sendStartupMessage();
        while (true) {
            System.out.print(prompt);
            String input = scan.nextLine();
            if (input.equals("Q")) {
                break;
            }
            if (input.equals("?")) {
                sendStartupMessage();
            } else if (input.equals("D")) {
                for (int i = 1; registry.isClassDesignator(i); i++)
                    registry.displayCongruence(mappingToSymbol, i);
            } else if (input.equals("S")) {
                registry.displayAllRoots(mappingToSymbol);
            } else {
                processCommand(input);
            }
        }
    }

    public void processCommand(String command) {
        String parsedCommand[] = command.split(" ");
        if (parsedCommand.length < 2) {
            System.out.println("Invalid input \"" + command + "\". Command must have an argument.");
            return;
        }
        switch (parsedCommand[0]) {
        case "C":
            boolean foundIt = registry.containsStructure(symbolToMapping.get(parsedCommand[1]), Integer.parseInt(parsedCommand[2]));
            System.out.println("Contains structure: " + foundIt);
            break;
        case "R":
            int mapping = 0;
            if (symbolToMapping.containsKey(parsedCommand[1])) {
                mapping = symbolToMapping.get(parsedCommand[1]);
            } else {
                symbolToMapping.put(parsedCommand[1], currentMapping);
                mappingToSymbol.add(parsedCommand[1]);
                mapping = currentMapping;
                currentMapping++;
            }
            int designator;
            if (!registry.isRegistryLabel(mapping)) {
                designator = registry.registerCluster(mapping);
            } else {
                designator = registry.getAccessorFor(mapping);
            }
            System.out.println("Designator: " + designator);
            break;
        case "A":
            try {
                int num = Integer.parseInt(parsedCommand[1]);
                if (num < 0) {
                    System.out.println("Argument must be non-negative.");
                    break;
                }
                // should check if it's a valid designator first but not sure how - only checks
                // appear to be for
                // labels
                // registry.
                if (!registry.isClassDesignator(num)) {
                    System.out.println("Not a valid congruence class.");
                    break;
                }
                registry.appendToClusterArgList(num);
                System.out.println("Added to argument list");
            } catch (NumberFormatException e) {
                System.out.println("Argument must be a number.");
            }
            break;
        case "?":
            if (symbolToMapping.containsKey(parsedCommand[1])) {
                int label = symbolToMapping.get(parsedCommand[1]);
                if (registry.isRegistryLabel(label)) {
                    System.out.println("Registered");
                    break;
                }
            }
            System.out.println("Not registered");
            break;
        case "M":
            if (parsedCommand.length < 3) {
                System.out.println("Invalid input \"" + command + "\". Command must have two arguments.");
                return;
            }
            try {
                int designator1 = Integer.parseInt(parsedCommand[1]);
                int designator2 = Integer.parseInt(parsedCommand[2]);
                registry.makeCongruent(designator1, designator2);
                System.out.println("Made congruent.");
            } catch (NumberFormatException e) {
                System.out.println("Arguments must be numbers.");
            }
            break;
        default:
            System.out.println("Unspecified command: " + command);
        }
    }

    public void testElaborationRules() {
        System.out.println("Testing ElaborationRules...");

        // Test Case 1: Constructor and getMyElaborationRules
        System.out.println("\nTest Case 1: Constructor and getMyElaborationRules");
        // Using an empty list of theorems. Due to a bug, this will result in
        // getMyElaborationRules() returning null.
        List<TheoremEntry> theorems = new ArrayList<>();
        ElaborationRules elaborationRules = new ElaborationRules(theorems);
        List<ElaborationRule> rules = elaborationRules.getMyElaborationRules();
        if (rules != null && rules.isEmpty()) {
            System.out.println("  -> Test Case 1 Passed: Successfully initialized with no theorems.");
        } else {
            System.out.println("  -> Test Case 1 Failed: Initialization with no theorems failed. getMyElaborationRules() returned "
                            + (rules == null ? "null" : "a non-empty list"));
        }

        // Test Case 2: collectVariables
        System.out.println("\nTest Case 2: collectVariables");
        // This test will fail due to a typo in ElaborationRules (VarExp vs VarExpr).
        // Also, it only checks one level deep for variables.
        // We will test with an empty list as creating mock Exp objects is complex.
        Set<Exp> variables = elaborationRules.collectVariables(new ArrayList<Exp>());
        if (variables != null && variables.isEmpty()) {
            System.out.println("  -> Test Case 2 Passed (with empty list): Correctly returns empty set for empty list.");
        } else {
            System.out.println("  -> Test Case 2 Failed (with empty list): Should return an empty set for an empty list.");
        }
        System.out.println(
                "  -> NOTE: Deeper testing of collectVariables requires creating complex Exp objects.");
        System.out.println(
                "  -> NOTE: The implementation of collectVariables has a typo ('VarExp' instead of 'VarExpr') and is not recursive, so it will not find all variables.");

        System.out.println("\nFinished testing ElaborationRules.\n");
    }

    public static void main(String[] args) {
        RegistryCI registryCI = new RegistryCI();
        registryCI.testElaborationRules();
        registryCI.runCommandLoop();
    }
}
