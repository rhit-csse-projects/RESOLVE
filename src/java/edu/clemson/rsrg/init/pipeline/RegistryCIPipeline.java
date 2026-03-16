/*
 * RegistryCIPipeline.java
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
package edu.clemson.rsrg.init.pipeline;

import edu.clemson.rsrg.init.CompileEnvironment;
import edu.clemson.rsrg.init.flag.Flag;
import edu.clemson.rsrg.nProver.registryCI.RegistryCI;
import edu.clemson.rsrg.nProver.utilities.theorems.TheoremStore;
import edu.clemson.rsrg.typeandpopulate.symboltables.MathSymbolTableBuilder;
import edu.clemson.rsrg.typeandpopulate.utilities.ModuleIdentifier;

public class RegistryCIPipeline extends AbstractPipeline {

    public static final Flag FLAG_REGISTRY_CI = new Flag("Prover", "registryCI",
            "After analysis, drop into an interactive RegistryCI session backed by the module's theorem store.");

    public RegistryCIPipeline(CompileEnvironment ce, MathSymbolTableBuilder symbolTable) {
        super(ce, symbolTable);
    }

    @Override
    public void process(ModuleIdentifier currentTarget) {
        TheoremStore theoremStore = new TheoremStore(mySymbolTable.getModuleScope(currentTarget));
        new RegistryCI(theoremStore).runCommandLoop();
    }

    public static void setUpFlags() {
        // only exists because of the "flag sealing" mechanism
    }
}
