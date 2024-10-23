package edu.clemson.rsrg.init.pipeline;

import edu.clemson.rsrg.absyn.declarations.moduledecl.ModuleDec;
import edu.clemson.rsrg.init.CompileEnvironment;
import edu.clemson.rsrg.init.ResolveCompiler;
import edu.clemson.rsrg.statushandling.StatusHandler;
import edu.clemson.rsrg.typeandpopulate.symboltables.MathSymbolTableBuilder;
import edu.clemson.rsrg.typeandpopulate.utilities.ModuleIdentifier;
import jdk.jshell.Snippet;

public class ParameterCheckPipeline extends AbstractPipeline{
    /**
     * <p>
     * An helper constructor that allow us to store the {@link CompileEnvironment} and {@link MathSymbolTableBuilder}
     * from a class that inherits from {@code AbstractPipeline}.
     * </p>
     *
     * @param ce          The current compilation environment.
     * @param symbolTable The symbol table.
     */
    protected ParameterCheckPipeline(CompileEnvironment ce, MathSymbolTableBuilder symbolTable) {
        super(ce, symbolTable);
    }

    @Override
    public void process(ModuleIdentifier currentTarget) {
        ModuleDec moduleDec = myCompileEnvironment.getModuleAST(currentTarget);
        StatusHandler statusHandler = myCompileEnvironment.getStatusHandler();

        if (myCompileEnvironment.flags.isFlagSet(ResolveCompiler.FLAG_DEBUG)) {
            StringBuffer sb = new StringBuffer();
            sb.append("\n-----------------Checking Parameter Modes-----------------\n\n");
            sb.append("Checking Parameter Modes for: ");
            sb.append(moduleDec.getName());

            statusHandler.info(null, sb.toString());
        }
    }
}
