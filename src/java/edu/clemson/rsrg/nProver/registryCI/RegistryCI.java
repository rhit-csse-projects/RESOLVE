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
    private CongruenceClassRegistry registry;
    private Map<String, Integer> symbolToMapping;
    private List<String> mappingToSymbol;
    private Scanner scan;
    private int currentMapping;

    private static String prompt = " >";

    public RegistryCI() {
        registry = new CongruenceClassRegistry<>(100, 100, 100, 100);
        symbolToMapping = new HashMap<String, Integer>();
        mappingToSymbol = new ArrayList<String>();
        scan = new Scanner(System.in);
        currentMapping = 0;
    }

    public static void sendStartupMessage() {
        System.out.println(
                "R - registerCluster\n? - isRegistered\nA - appendToClusterArgList\nM - makeCongruent\nQ - quit");
    }

    public void runCommandLoop() {
        sendStartupMessage();
        while (true) {
            System.out.print(prompt);
            String input = scan.nextLine();
            if (input.equals("Q")) {
                break;
            }
            String command[] = input.split(" ");
            if (command.length < 2) {
                System.out.println("Invalid input \"" + input + "\". Command must have an argument.");
                continue;
            }
            if (command.length > 2) {
                System.out.println("Invalid input \"" + input + "\". Command can only have one argument.");
                continue;
            }
            processCommand(command[0], command[1]);
        }
    }

    public void processCommand(String command, String arg) {
        switch (command) {
        case "R":
            if (symbolToMapping.containsKey(arg)) {
                System.out.println("Symbol \"" + arg + "\" already registered.");
                break;
            }
            symbolToMapping.put(arg, currentMapping);
            mappingToSymbol.add(arg);
            int designator = registry.registerCluster(currentMapping);
            System.out.println("Designator: " + designator);
            currentMapping++;
            break;
        case "A":
            try {
                int num = Integer.parseInt(arg);
                if (num < 0) {
                    System.out.println("Argument must be non-negative.");
                }
                // should check if it's a valid designator first but not sure how - only checks appear to be for labels
                registry.appendToClusterArgList(num);
            } catch (NumberFormatException e) {
                System.out.println("Argument must be a number.");
            }
            break;
        case "?":
            if (!symbolToMapping.containsKey(arg)) {
                System.out.println("Not registered");
                break;
            }
            int label = symbolToMapping.get(arg);
            if (registry.isRegistryLabel(label)) {
                System.out.println("Registered");
            } else {
                System.out.println("Not registered");
            }
            break;
        case "M":
            System.out.println("UNIMPLEMENTED FUNCTION");
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
