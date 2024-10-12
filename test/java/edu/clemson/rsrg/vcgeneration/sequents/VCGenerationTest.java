/*
 * TestFactoryTest.java
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
package edu.clemson.rsrg.vcgeneration.sequents;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.fail;

public class VCGenerationTest {

    @TestFactory
    Collection<DynamicTest> testVCGeneration() {
        String directoryPath = "/RESOLVE-Workspace/RESOLVE/Main/";
        File current_directory = new File(System.getProperty("user.dir"));
        File parentDirFile = current_directory.getParentFile();

        File directory = new File(parentDirFile.getAbsolutePath(), directoryPath);
        System.out.println(directory.getAbsolutePath());

        ArrayList<String> testFiles = new ArrayList<>();
        ArrayList<String> controlFiles = new ArrayList<>();
        ArrayList<String> fileNames = new ArrayList<>();

        // Check if the specified path is a directory
        if (directory.isDirectory()) {
            // Use a FilenameFilter to get only .asrt files
            FilenameFilter asrtFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".asrt");
                }
            };

            // Get the list of .asrt files
            String[] asrtFiles = directory.list(asrtFilter);

            // Print the list of .asrt files
            if (asrtFiles != null && asrtFiles.length > 0) {
                System.out.println(".asrt files found:");
                String controlFilesDir = "/test/validVCs/";
                File vcDir = new File(current_directory.getAbsolutePath(), controlFilesDir);
                System.out.println(vcDir.getAbsolutePath());
                for (String fileName : asrtFiles) {
                    File controlFile = new File(vcDir.getAbsolutePath(), fileName);
                    File testFile = new File(directory.getAbsolutePath(), fileName);

                    controlFiles.add(controlFile.getAbsolutePath());
                    testFiles.add(testFile.getAbsolutePath());
                    fileNames.add(fileName);

                }
            } else {
                Assertions.fail("No .asrt files found in the directory.");

            }
        } else {
            Assertions.fail("The specified path is not a directory.");
        }

        return IntStream.range(0, fileNames.size()).mapToObj(i -> DynamicTest.dynamicTest(fileNames.get(i), () -> {
            String testFile = testFiles.get(i);
            String controlFile = controlFiles.get(i);
            File test = new File(testFile);
            File control = new File(controlFile);
            // Assertions.assertEquals(0, 1);

            System.out.println("Test file: " + testFile);
            System.out.println("Control file: " + controlFile);

            if (!control.exists()) {
                fail("Control file does not exist: " + controlFile);
            }
            if (!test.exists()) {
                fail("Test file does not exist: " + testFile);
            }

            try {
                boolean filesAreEqual = filesAreEqualIgnoreLineEndingsAndEncoding(control, test);
                if (!filesAreEqual) {
                    fail("Files are not equal: " + controlFile + " and " + testFile);
                }
            } catch (IOException e) {
                fail("Error comparing files: " + e.getMessage());
            }
        })).toList();
    }

    public static boolean filesAreEqualIgnoreLineEndingsAndEncoding(File file1, File file2) throws IOException {
        // Read lines from both files (ignoring line ending differences)
        List<String> linesFile1 = Files.readAllLines(file1.toPath(), StandardCharsets.UTF_8);
        List<String> linesFile2 = Files.readAllLines(file2.toPath(), StandardCharsets.UTF_8);

        // Compare line-by-line
        if (linesFile1.size() != linesFile2.size()) {
            return false;
        }

        // starting at index 1 to ignore the first line which is the file header and contains timestamp
        for (int i = 1; i < linesFile1.size(); i++) {
            String line1 = linesFile1.get(i).trim(); // Trims to ignore spaces and CR/LF differences
            String line2 = linesFile2.get(i).trim();
            if (!line1.equals(line2)) {
                return false;
            }
        }
        return true;
    }
}
