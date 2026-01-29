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

import edu.clemson.rsrg.nProver.registry.CongruenceClassRegistry;

import java.util.*;

public class RegistryCI {
    private final CongruenceClassRegistry registry;
    private final Map<String, Integer> symbolToMapping;
    private final List<String> mappingToSymbol;
    private final Scanner scan;
    private int currentMapping;
    private int succedentLevel;

    public RegistryCI() {
        registry = new CongruenceClassRegistry(100, 100, 100, 100);
        symbolToMapping = new HashMap<>();
        mappingToSymbol = new ArrayList<>();
        scan = new Scanner(System.in);
        currentMapping = 0;
        succedentLevel = 0;
    }

    public static void sendStartupMessage() {
        System.out.println("""
                R - registerCluster (uses arg list)
                RS - registerClusterForSuccedent (uses arg list)
                C - checkIfRegistered (uses arg list)
                ? - isRegistryLabel
                A - appendToClusterArgList
                M - makeCongruent
                P - checkIfProved
                D - display
                RA - remove last arg from clusterArgList
                DA - display clusterArgList
                G - display all roots
                Q - quit
                """);
    }

    public void runCommandLoop() {
        sendStartupMessage();
        label: while (true) {
            String prompt = "> ";
            System.out.print(prompt);
            String input = scan.nextLine();
            switch (input) {
                case "Q":
                    break label;
                case "?":
                    sendStartupMessage();
                    break;
                case "D":
                    for (int i = 1; registry.isClassDesignator(i); i++)
                        registry.displayCongruence(mappingToSymbol, i);
                    break;
                case "P":
                    String result = registry.checkIfProved() ? "Proved" : "Not Proved";
                    System.out.println(result);
                    break;
                case "RA":
                    int rc = registry.removeFromClusterArgList();
                    if (rc == -1)
                        System.out.println("Cluster Argument List is Empty. Cannot Remove");
                    else
                        System.out.println("Removed " + rc + "from Cluster Argument List");
                    break;
                case "DA":
                    registry.displayClusterArgumentList();
                    break;
                case "G":
                    Set<Integer> tempSet = registry.getAllRoots();
                    for (Integer i : tempSet) {
                        System.out.println("Root: " + (i + 1) + " Symbol: " + mappingToSymbol.get(i));
                    }
                    break;
                default:
                    processCommand(input);
                    break;
            }
        }
    }

    public void processCommand(String command) {
        String[] parsedCommand = command.split(" ");
        if (parsedCommand.length < 2) {
            System.out.println("Invalid input \"" + command + "\". Command must have an argument.");
            return;
        }
        int mapping;

        switch (parsedCommand[0]) {
            case "R":
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
            case "RS":
                if (symbolToMapping.containsKey(parsedCommand[1])) {
                    mapping = symbolToMapping.get(parsedCommand[1]);
                } else {
                    symbolToMapping.put(parsedCommand[1], currentMapping);
                    mappingToSymbol.add(parsedCommand[1]);
                    mapping = currentMapping;
                    currentMapping++;
                }
                int accessor = 0;
                if (succedentLevel == 0) {
                    BitSet attb = new BitSet();
                    attb.set(1); // set the class succedent
                    attb.set(2); // set the class ultimate

                    if (Objects.equals(parsedCommand[1], "=")) { // if it is succedent equal
                        registry.addOperatorToSuccedentReflexiveOperatorSet(mapping);
                        accessor = registry.registerCluster(mapping);
                        if (!registry.checkIfProved()) {
                            registry.updateClassAttributes(accessor, attb);
                        }
                        System.out.println("Designator: " + accessor + " (Succedent Equal Operator)");

                    } else if (Objects.equals(parsedCommand[1], "<=")) { // if it is succedent <=
                        registry.addOperatorToSuccedentReflexiveOperatorSet(mapping);
                        if (registry.checkIfRegistered(mapping)) {
                            registry.updateClassAttributes(registry.getAccessorFor(mapping), attb);
                            System.out.println("Designator: " + accessor);

                        } else {
                            accessor = registry.registerCluster(mapping);
                            registry.updateClassAttributes(accessor, attb);
                            System.out.println("Designator: " + accessor + " (Succedent Less-Than-Equal Operator)");

                        }
                    } else {
                        if (registry.checkIfRegistered(mapping)) {
                            accessor = registry.getAccessorFor(mapping);

                        } else {
                            accessor = registry.registerCluster(mapping);

                        }
                        registry.updateClassAttributes(accessor, attb);
                        System.out.println("Designator: " + accessor + " (Succedent Operator)");
                    }
                } else {
                    if (!registry.isRegistryLabel(mapping)) {
                        accessor = registry.registerCluster(mapping);
                    } else {
                        accessor = registry.getAccessorFor(mapping);
                    }
                    System.out.println("Designator: " + accessor + " (Level " + succedentLevel + ")");
                }
                succedentLevel++;
                break;
            case "A":
                try {
                    int num = Integer.parseInt(parsedCommand[1]);
                    if (num < 0) {
                        System.out.println("Argument must be non-negative.");
                        break;
                    }
                    // should check if it's a valid designator first but not sure how - only checks appear to be for
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
            case "C":
                if (symbolToMapping.containsKey(parsedCommand[1])) {
                    int label = symbolToMapping.get(parsedCommand[1]);
                    if (registry.checkIfRegistered(label)) {
                        System.out.println("Registered");
                        break;
                    }
                }
                System.out.println("Not registered");
                break;
            default:
                System.out.println("Unspecified command: " + command);
        }
    }

    public static void main(String[] args) {
        RegistryCI registryCI = new RegistryCI();
        registryCI.runCommandLoop();
    }
}
