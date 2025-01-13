package edu.clemson.rsrg.nProver.registryCI;

import edu.clemson.rsrg.nProver.registry.CongruenceClassRegistry;

import java.util.*;

public class RegistryCI {
    private CongruenceClassRegistry registry;
    private Map<String, Integer> symbolToMapping;
    private List<String> mappingToSymbol;
    private boolean isRunning;
    private Scanner scanner;

    private static final String COMMAND_PROMPT = "> ";

    public RegistryCI() {
        registry = new CongruenceClassRegistry<>(100, 100, 100, 100);
        symbolToMapping = new HashMap<String, Integer>();
        mappingToSymbol = new ArrayList<String>();
        isRunning = true;
        scanner = new Scanner(System.in);
    }


    public static void sendStartupMessage() {
        System.out.println("R - registerCluster\n? - isRegistryLabel\nA - appendToClusterArgList\nM - makeCongruent\nQ - quit");
    }

    public void processCommand(String command, String arg) {
        switch (command) {
            case "Q":
                isRunning = false;
                break;
            case "R":
            case "A":
            case "?":
            case "M":
                System.out.println("UNIMPLEMENTED FUNCTION");
                break;
            default:
                System.out.println("Unspecified command: " + command);
        }
    }

    public void runCommandLoop() {
        sendStartupMessage();
        while (isRunning) {
            System.out.print(COMMAND_PROMPT);
            String input = scanner.nextLine();
            String[] command = input.split(" ");

            //TODO check this
            if (command.length > 2) {
                System.out.println("Max command length is <command> <arg>");
                continue;
            }

            if (command.length == 1) {
                processCommand(command[0], null);
            } else {
                processCommand(command[0], command[1]);
            }
        }
    }

    public static void main(String[] args) {
        RegistryCI registryCI = new RegistryCI();
        registryCI.runCommandLoop();
    }
}
