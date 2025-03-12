/*
 * LoopChangingTest.java
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
package edu.clemson.rsrg;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoopChangingTest {

    private static String JAR_FILE_NAME;

    public static void executeCommand(String command, String directory) {
        try {
            // Detect the operating system
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;

            // Set the command based on the operating system
            if (os.contains("win")) {
                // Windows
                processBuilder = new ProcessBuilder("powershell.exe", "-Command", command);
            } else {
                // Linux or MacOS
                processBuilder = new ProcessBuilder("bash", "-c", command);
            }

            // Set the working directory if provided
            if (directory != null && !directory.isEmpty()) {
                processBuilder.directory(new File(directory));
            } else {
                processBuilder.directory(new File(System.getProperty("user.dir")));
            }

            // Start the process
            Process process = processBuilder.start();

            // Capture output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BeforeAll
    public static void checkforJarFile() {
        // issue a maven clean install command
        // mvn clean install
        // check if the jar file exists in the target folder

        // Get the project directory

        File projectDir = new File(System.getProperty("user.dir"));
        File jarFileFolder = new File(projectDir, "target/");

        // Get list of .jar files in target folder
        FilenameFilter jarFilter = (dir, name) -> name.toLowerCase().endsWith(".jar");

        // get list of files in target folder as string array
        String[] files = jarFileFolder.list(jarFilter);
        if (files == null || files.length == 0) {
            System.out.println("No files in target folder");

            // issue a maven clean package command
            executeCommand("mvn package -DskipTests", "");
        }

        Pattern pattern = Pattern.compile("RESOLVE-.*-jar-with-dependencies.jar", Pattern.CASE_INSENSITIVE);
        files = jarFileFolder.list(jarFilter);
        // Check if the jar file exists
        boolean jarFileExists = false;
        for (String file : files) {
            Matcher matcher = pattern.matcher(file);
            if (matcher.find()) {
                System.out.println("Jar file found: " + file);
                jarFileExists = true;
            }
        }

        if (!jarFileExists) {
            System.out.println("Jar file not found. Exiting...");
            System.exit(1);
        }

        File jarFile = new File(jarFileFolder, files[0]);
        JAR_FILE_NAME = jarFile.getAbsolutePath();

    }

    @Test
    public void loopChangingTest_no_modifications_expectSuccess() {
        // Run the RESOLVE compiler
        executeCommand("java -jar " + JAR_FILE_NAME + " -VCs Inject_Front_Realiz.rb", "RESOLVE-Workspace/Main/Concepts/Queue_Template/");

        // Check if the output file exists
        assert new File("RESOLVE-Workspace/Main/Concepts/Queue_Template/Inject_Front_Realiz.asrt").exists();
    }

    @AfterEach
    public void delete_output_files() {
        executeCommand("rm -r *.asrt", "RESOLVE-Workspace/Main/Concepts/Queue_Template/");
    }

}
