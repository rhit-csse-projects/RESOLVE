/*
 * ProverRegressionTest.java
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
package edu.clemson.rsrg.nProver;

import edu.clemson.rsrg.init.ResolveCompiler;
import edu.clemson.rsrg.init.output.OutputListener;
import edu.clemson.rsrg.nProver.output.VCProverResult;
import edu.clemson.rsrg.prover.output.Metrics;
import edu.clemson.rsrg.prover.output.PerVCProverModel;
import edu.clemson.rsrg.statushandling.SystemStdHandler;
import edu.clemson.rsrg.vcgeneration.utilities.AssertiveCodeBlock;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>
 * Regression tests that invoke the RESOLVE nProver on realizations we expect to fully prove. Adding a new file to test
 * is just adding a path to the {@code @ValueSource} array.
 * </p>
 * <p>
 * Uses the existing ResolveCompiler.invokeCompiler for the WebAPI/WebIDE
 * </p>
 */
class ProverRegressionTest {

    /**
     * <p>
     * Each value is a path relative to {@code RESOLVE-Workspace/Main/} pointing to a realization file that we expect
     * the nProver to fully prove.
     * </p>
     */
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = { "Concepts/Stack_Template/Do_Nothing_Realiz.rb",
            "Concepts/Globally_Bounded_Stack_Template/Obvious_Flipping_Realiz.rb" })
    void allVCsProved(String relativePath) {
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        Path workspaceDir = projectRoot.resolve("RESOLVE-Workspace").resolve("Main");
        Path targetFile = workspaceDir.resolve(relativePath);

        assertTrue(targetFile.toFile().exists(), "Target file not found: " + targetFile);

        String[] args = { "-addConstraints", "-sprove", targetFile.toAbsolutePath().toString() };

        ProverResultCapture capture = new ProverResultCapture();
        ResolveCompiler compiler = new ResolveCompiler(args);
        compiler.invokeCompiler(Collections.emptyMap(), Collections.emptyMap(), new SystemStdHandler(), capture);

        List<VCProverResult> results = capture.getResults();

        assertFalse(results.isEmpty(), "No VCs were generated for " + relativePath);

        int proved = 0;
        int notProved = 0;
        StringBuilder failures = new StringBuilder();

        for (VCProverResult r : results) {
            if (r.isProved()) {
                proved++;
            } else {
                notProved++;
                failures.append("  VC #").append(r.getVC().getName()).append("\n");
            }
        }

        assertEquals(0, notProved,
                notProved + " of " + results.size() + " VC(s) not proved in " + relativePath + ":\n" + failures);
        assertEquals(results.size(), proved, "Proved count mismatch for " + relativePath);
    }

    /**
     * <p>
     * A minimal {@link OutputListener} that only captures prover results. All other listener methods are no-ops.
     * </p>
     */
    private static class ProverResultCapture implements OutputListener {

        private final List<VCProverResult> myResults = new ArrayList<>();

        List<VCProverResult> getResults() {
            return myResults;
        }

        @Override
        public void nProverResult(String inputFileName, String outputFileName, long timeOut, int numTries,
                List<VCProverResult> results, long totalTime, String verboseOutput) {
            myResults.addAll(results);
        }

        @Override
        public void astGraphvizModelResult(String outputFileName, String graphvizModel) {
        }

        @Override
        public void cTranslationResult(String inputFileName, String outputFileName, String cTranslation) {
        }

        @Override
        public void javaTranslationResult(String inputFileName, String outputFileName, String javaTranslation) {
        }

        @Override
        public void proverResult(String inputFileName, String outputFileName) {
        }

        @Override
        public void vcGeneratorResult(String inputFileName, String outputFileName, List<AssertiveCodeBlock> blocks,
                String verboseOutput) {
        }

        @Override
        public void vcResult(boolean proved, PerVCProverModel finalModel, Metrics m) {
        }
    }
}
