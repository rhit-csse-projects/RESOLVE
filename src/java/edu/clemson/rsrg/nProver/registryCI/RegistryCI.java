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

import static edu.clemson.rsrg.nProver.utilities.treewakers.AbstractRegisterSequent.OP_EQUALS;
import static edu.clemson.rsrg.nProver.utilities.treewakers.AbstractRegisterSequent.OP_LESS_THAN_OR_EQUALS;

public class RegistryCI {
    private final CongruenceClassRegistry registry;
    private final Map<String, Integer> symbolToMapping;
    private final List<String> mappingToSymbol;
    private final Scanner scan;
    private int currentMapping;

    private boolean isUltimate;

    public RegistryCI() {
        registry = new CongruenceClassRegistry(100, 100, 100, 100);
        symbolToMapping = new HashMap<>();
        mappingToSymbol = new ArrayList<>();
        scan = new Scanner(System.in);
        currentMapping = 3; // Start at 3 since 1=OP_LESS_THAN_OR_EQUALS, 2=OP_EQUALS
        isUltimate = false;

        // add operators
        symbolToMapping.put("<=", OP_LESS_THAN_OR_EQUALS);
        symbolToMapping.put("=", OP_EQUALS);
        mappingToSymbol.add(null);
        mappingToSymbol.add("<=");
        mappingToSymbol.add("=");
    }

    public static void sendStartupMessage() {
        System.out.println("""
                == Antecedent Commands ==
                RV <symbol>      - Register Variable/Constant (antecedent leaf)
                RF <symbol>      - Register Function/Operator (antecedent, uses arg list)
                RE               - Register Equals (antecedent) - makes args congruent

                == Succedent Commands ==
                RVS <symbol>     - Register Variable/Constant (succedent leaf)
                RFS <symbol>     - Register Function/Operator (succedent, uses arg list)
                RES              - Register Equals (succedent, reflexive check)
                RLES             - Register Less-Than-Equals (succedent, reflexive check)

                === Level Commands ==
                U                - Mark next registration as Ultimate (root level)
                NU               - Mark next registration as Non-Ultimate (nested)

                == Generic Commands ==
                A <designator>   - Append class designator to argument list
                M <CC1> <CC2>    - Make two classes congruent
                P                - Check if proved
                D                - Display all congruence classes
                DA               - Display argument list
                RA               - Remove last arg from argument list
                G                - Display all roots
                ?                - Show this help
                Q                - Quit
                """);
    }

    public void runCommandLoop() {
        sendStartupMessage();
        label: while (true) {
            String prompt = isUltimate ? "[U]> " : "[NU]> ";
            System.out.print(prompt);
            String input = scan.nextLine().trim();

            if (input.isEmpty())
                continue;

            switch (input.split(" ")[0]) {
                case "Q":
                    break label;
                case "?":
                    sendStartupMessage();
                    break;
                case "D":
                    displayAllClasses();
                    break;
                case "P":
                    String result = registry.checkIfProved() ? "PROVED" : "Not Proved";
                    System.out.println(result);
                    break;
                case "RA":
                    int rc = registry.removeFromClusterArgList();
                    if (rc == -1)
                        System.out.println("Cluster Argument List is Empty. Cannot Remove");
                    else
                        System.out.println("Removed CC" + rc + " from Cluster Argument List");
                    break;
                case "DA":
                    registry.displayClusterArgumentList();
                    break;
                case "G":
                    displayAllRoots();
                    break;
                case "U":
                    isUltimate = true;
                    System.out.println("Entered ULTIMATE Mode (root level)");
                    break;
                case "NU":
                    isUltimate = false;
                    System.out.println("Entered NON-ULTIMATE Mode (nested)");
                    break;
                default:
                    processCommand(input);
                    break;
            }
        }
    }

    public void processCommand(String command) {
        String[] parsedCommand = command.split(" ");
        String cmd = parsedCommand[0];

        switch (cmd) {
            // ANTECEDENT COMMANDS
            case "RV": // Register Variable (antecedent)
                if (parsedCommand.length < 2) {
                    System.out.println("Usage: RV <symbol>");
                    return;
                }
                registerAntecedentVariable(parsedCommand[1]);
                break;

            case "RF": // Register Function (antecedent)
                if (parsedCommand.length < 2) {
                    System.out.println("Usage: RF <symbol>");
                    return;
                }
                registerAntecedentFunction(parsedCommand[1]);
                break;

            case "RE": // Register Equals (antecedent) - makes congruent
                registerAntecedentEquals();
                break;

            // SUCCEDENT COMMANDS
            case "RVS": // Register Variable (succedent)
                if (parsedCommand.length < 2) {
                    System.out.println("Usage: RVS <symbol>");
                    return;
                }
                registerSuccedentVariable(parsedCommand[1]);
                break;

            case "RFS": // Register Function (succedent)
                if (parsedCommand.length < 2) {
                    System.out.println("Usage: RFS <symbol>");
                    return;
                }
                registerSuccedentFunction(parsedCommand[1]);
                break;

            case "RES": // Register Equals (succedent)
                registerSuccedentEquals();
                break;

            case "RLES": // Register Less-Than-Equals (succedent)
                registerSuccedentLessThanEquals();
                break;

            // COMMANDS
            case "A": // Append to arg list
                if (parsedCommand.length < 2) {
                    System.out.println("Usage: A <designator>");
                    return;
                }
                appendToArgList(parsedCommand[1]);
                break;

            case "M": // Make congruent
                if (parsedCommand.length < 3) {
                    System.out.println("Usage: M <designator1> <designator2>");
                    return;
                }
                makeCongruent(parsedCommand[1], parsedCommand[2]);
                break;

            case "C": // Check if registered
                if (parsedCommand.length < 2) {
                    System.out.println("Usage: C <symbol>");
                    return;
                }
                checkIfRegistered(parsedCommand[1]);
                break;

            default:
                System.out.println("Unknown command: " + cmd + ". Type ? for help.");
        }
    }

    private void registerAntecedentVariable(String symbol) {
        int mapping = getOrCreateMapping(symbol);
        int designator;

        if (registry.checkIfRegistered(mapping)) {
            designator = registry.getAccessorFor(mapping);
            System.out.println("Already registered. Designator: CC" + designator);
        } else {
            designator = registry.registerCluster(mapping);

            // Probably shouldn't happen a lot since these variables are typically just args (leaves in the tree)
            if (isUltimate) {
                BitSet attb = new BitSet();
                attb.set(0); // antecedent
                attb.set(2); // ultimate
                registry.updateClassAttributes(designator, attb);
                System.out.println("Registered ULTIMATE antecedent variable. Designator: CC" + designator);
            } else {
                System.out.println("Registered antecedent variable. Designator: CC" + designator);
            }
        }
    }

    private void registerAntecedentFunction(String symbol) {
        int mapping = getOrCreateMapping(symbol);
        int designator;

        if (registry.checkIfRegistered(mapping)) {
            designator = registry.getAccessorFor(mapping);
            System.out.println("Already registered. Designator: CC" + designator);
        } else {
            designator = registry.registerCluster(mapping);

            if (isUltimate) {
                BitSet attb = new BitSet();
                attb.set(0); // antecedent
                attb.set(2); // ultimate
                registry.updateClassAttributes(designator, attb);
                System.out.println("Registered ULTIMATE antecedent function. Designator: CC" + designator);
            } else {
                System.out.println("Registered antecedent function. Designator: CC" + designator);
            }
        }
    }

    private void registerAntecedentEquals() {
        int lhs = registry.removeFromClusterArgList();
        int rhs = registry.removeFromClusterArgList();

        if (lhs == -1 || rhs == -1) {
            System.out.println("ERROR: Need exactly 2 arguments in arg list for antecedent equals (arg list is preserved)");
            if (lhs != -1)
                registry.appendToClusterArgList(lhs);
            return;
        }

        if (!registry.areCongruent(lhs, rhs)) {
            registry.makeCongruent(lhs, rhs);
            System.out.println("Made CC" + lhs + " congruent with CC" + rhs);
        } else {
            System.out.println("CC" + lhs + " and CC" + rhs + " are already congruent");
        }

        if (registry.checkIfProved()) {
            System.out.println("*** PROVED! ***");
        }
    }

    private void registerSuccedentVariable(String symbol) {
        int mapping = getOrCreateMapping(symbol);
        int designator;

        if (registry.checkIfRegistered(mapping)) {
            designator = registry.getAccessorFor(mapping);
            System.out.println("Already registered. Designator: CC" + designator);
        } else {
            designator = registry.registerCluster(mapping);

            if (isUltimate) {
                BitSet attb = new BitSet();
                attb.set(1); // succedent
                attb.set(2); // ultimate
                registry.updateClassAttributes(designator, attb);
                System.out.println("Registered ULTIMATE succedent variable. Designator: CC" + designator);
            } else {
                System.out.println("Registered succedent variable. Designator: CC" + designator);
            }
        }
    }

    private void registerSuccedentFunction(String symbol) {
        int mapping = getOrCreateMapping(symbol);
        int designator;

        if (registry.checkIfRegistered(mapping)) {
            designator = registry.getAccessorFor(mapping);
            if (isUltimate) {
                BitSet attb = new BitSet();
                attb.set(1); // succedent
                attb.set(2); // ultimate
                registry.updateClassAttributes(designator, attb);
            }
            System.out.println("Already registered. Designator: CC" + designator);
        } else {
            designator = registry.registerCluster(mapping);

            if (isUltimate) {
                BitSet attb = new BitSet();
                attb.set(1); // succedent
                attb.set(2); // ultimate
                registry.updateClassAttributes(designator, attb);
                System.out.println("Registered ULTIMATE succedent function. Designator: CC" + designator);
            } else {
                System.out.println("Registered succedent function. Designator: CC" + designator);
            }
        }

        if (registry.checkIfProved()) {
            System.out.println("** PROVED! **");
        }
    }

    private void registerSuccedentEquals() {
        registry.addOperatorToSuccedentReflexiveOperatorSet(OP_EQUALS);

        int designator = registry.registerCluster(OP_EQUALS);

        if (!registry.checkIfProved() && isUltimate) {
            BitSet attb = new BitSet();
            attb.set(1); // succedent
            attb.set(2); // ultimate
            registry.updateClassAttributes(designator, attb);
        }

        System.out.println("Registered succedent equals. Designator: CC" + designator);

        if (registry.checkIfProved()) {
            System.out.println("*** PROVED! ***");
        }
    }

    private void registerSuccedentLessThanEquals() {
        registry.addOperatorToSuccedentReflexiveOperatorSet(OP_LESS_THAN_OR_EQUALS);

        int designator;
        if (registry.checkIfRegistered(OP_LESS_THAN_OR_EQUALS)) {
            designator = registry.getAccessorFor(OP_LESS_THAN_OR_EQUALS);
            if (isUltimate) {
                BitSet attb = new BitSet();
                attb.set(1); // succedent
                attb.set(2); // ultimate
                registry.updateClassAttributes(designator, attb);
            }
        } else {
            designator = registry.registerCluster(OP_LESS_THAN_OR_EQUALS);
            if (isUltimate) {
                BitSet attb = new BitSet();
                attb.set(1); // succedent
                attb.set(2); // ultimate
                registry.updateClassAttributes(designator, attb);
            }
        }

        System.out.println("Registered succedent <=. Designator: CC" + designator);

        if (registry.checkIfProved()) {
            System.out.println("*** PROVED! ***");
        }
    }

    private void appendToArgList(String arg) {
        try {
            int num = Integer.parseInt(arg);
            if (num < 0) {
                System.out.println("Argument must be non-negative.");
                return;
            }
            if (!registry.isClassDesignator(num)) {
                System.out.println("Not a valid congruence class designator.");
                return;
            }
            registry.appendToClusterArgList(num);
            System.out.println("Added CC" + num + " to argument list");
        } catch (NumberFormatException e) {
            System.out.println("Argument must be a number.");
        }
    }

    private void makeCongruent(String d1, String d2) {
        try {
            int designator1 = Integer.parseInt(d1);
            int designator2 = Integer.parseInt(d2);
            registry.makeCongruent(designator1, designator2);
            System.out.println("Made CC" + designator1 + " congruent with CC" + designator2);

            if (registry.checkIfProved()) {
                System.out.println("*** PROVED! ***");
            }
        } catch (NumberFormatException e) {
            System.out.println("Arguments must be numbers.");
        }
    }

    private void checkIfRegistered(String symbol) {
        if (symbolToMapping.containsKey(symbol)) {
            int label = symbolToMapping.get(symbol);
            if (registry.checkIfRegistered(label)) {
                int accessor = registry.getAccessorFor(label);
                System.out.println("Registered as CC" + accessor);
                return;
            }
        }
        System.out.println("Not registered");
    }

    private int getOrCreateMapping(String symbol) {
        if (symbolToMapping.containsKey(symbol)) {
            return symbolToMapping.get(symbol);
        } else {
            symbolToMapping.put(symbol, currentMapping);
            mappingToSymbol.add(symbol);
            return currentMapping++;
        }
    }

    private void displayAllClasses() {
        System.out.println("=== Congruence Classes ===");
        for (int i = 1; registry.isClassDesignator(i); i++) {
            registry.displayCongruence(mappingToSymbol, i);
        }
    }

    private void displayAllRoots() {
        Set<Integer> tempSet = registry.getAllRoots();
        System.out.println("=== Root Labels ===");
        for (Integer i : tempSet) {
            String symbol = (i < mappingToSymbol.size()) ? mappingToSymbol.get(i) : "?";
            System.out.println("Root: " + i + " Symbol: " + symbol);
        }
    }

    public static void main(String[] args) {
        RegistryCI registryCI = new RegistryCI();
        registryCI.runCommandLoop();
    }
}
