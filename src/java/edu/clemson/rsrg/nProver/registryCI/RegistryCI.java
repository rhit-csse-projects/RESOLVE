package edu.clemson.rsrg.nProver.registryCI;

import edu.clemson.rsrg.nProver.registry.CongruenceClassRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistryCI {
    private CongruenceClassRegistry registry;
    private Map<String, Integer> symbolToMapping;
    private List<String> mappingToSymbol;



    public RegistryCI() {
        registry = new CongruenceClassRegistry<>(100, 100, 100, 100);
        symbolToMapping = new HashMap<String, Integer>();
        mappingToSymbol = new ArrayList<String>();
    }


    public static void sendStartupMessage() {
        System.out.println("R - registerCluster\n? - isRegistryLabel\nA - appendToClusterArgList\nM - makeCongruent\nQ - quit");
    }

    public void processCommand(String command, String arg) {
        switch (command) {
            case "Q":
            case "R":
            case "A":
            case "?":
            case "M":
                System.out.println("UNIMPLEMENTED FUNCTION");
            default:
                System.out.println("Unspecified command: " + command);
        }
    }

    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
