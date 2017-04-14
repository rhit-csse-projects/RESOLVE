/*
 * RawASTOutputPipeline.java
 * ---------------------------------
 * Copyright (c) 2017
 * RESOLVE Software Research Group
 * School of Computing
 * Clemson University
 * All rights reserved.
 * ---------------------------------
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package edu.clemson.cs.rsrg.init.pipeline;

import edu.clemson.cs.rsrg.absyn.declarations.moduledecl.ModuleDec;
import edu.clemson.cs.rsrg.init.CompileEnvironment;
import edu.clemson.cs.rsrg.statushandling.StatusHandler;
import edu.clemson.cs.rsrg.typeandpopulate.symboltables.MathSymbolTableBuilder;
import edu.clemson.cs.rsrg.typeandpopulate.utilities.ModuleIdentifier;

/**
 * <p>This is pipeline that outputs the module AST
 * as a string.</p>
 *
 * @author Yu-Shan Sun
 * @version 1.0
 */
public class RawASTOutputPipeline extends AbstractPipeline {

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * <p>This generates a pipeline to output a module's AST
     * as a string.</p>
     *
     * @param ce The current compilation environment.
     * @param symbolTable The symbol table.
     */
    public RawASTOutputPipeline(CompileEnvironment ce,
            MathSymbolTableBuilder symbolTable) {
        super(ce, symbolTable);
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public final void process(ModuleIdentifier currentTarget) {
        ModuleDec dec = myCompileEnvironment.getModuleAST(currentTarget);
        StatusHandler statusHandler = myCompileEnvironment.getStatusHandler();

        StringBuffer sb = new StringBuffer();
        sb.append("\n---------------Print Module---------------\n\n");
        sb.append(dec.asString(0, 4));
        sb.append("\n---------------Print Module---------------\n");
        statusHandler.info(null, sb.toString());
    }

}