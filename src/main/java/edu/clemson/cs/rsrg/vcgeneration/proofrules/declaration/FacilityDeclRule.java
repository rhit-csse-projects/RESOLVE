/*
 * FacilityDeclRule.java
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
package edu.clemson.cs.rsrg.vcgeneration.proofrules.declaration;

import edu.clemson.cs.rsrg.absyn.declarations.Dec;
import edu.clemson.cs.rsrg.absyn.declarations.facilitydecl.FacilityDec;
import edu.clemson.cs.rsrg.absyn.declarations.moduledecl.*;
import edu.clemson.cs.rsrg.absyn.declarations.operationdecl.OperationDec;
import edu.clemson.cs.rsrg.absyn.declarations.paramdecl.ModuleParameterDec;
import edu.clemson.cs.rsrg.absyn.declarations.typedecl.TypeFamilyDec;
import edu.clemson.cs.rsrg.absyn.declarations.variabledecl.ParameterVarDec;
import edu.clemson.cs.rsrg.absyn.expressions.Exp;
import edu.clemson.cs.rsrg.absyn.expressions.mathexpr.InfixExp;
import edu.clemson.cs.rsrg.absyn.expressions.mathexpr.MathExp;
import edu.clemson.cs.rsrg.absyn.expressions.mathexpr.VarExp;
import edu.clemson.cs.rsrg.absyn.expressions.programexpr.*;
import edu.clemson.cs.rsrg.absyn.items.programitems.EnhancementSpecRealizItem;
import edu.clemson.cs.rsrg.absyn.items.programitems.ModuleArgumentItem;
import edu.clemson.cs.rsrg.parsing.data.Location;
import edu.clemson.cs.rsrg.parsing.data.PosSymbol;
import edu.clemson.cs.rsrg.statushandling.exception.MiscErrorException;
import edu.clemson.cs.rsrg.treewalk.TreeWalker;
import edu.clemson.cs.rsrg.typeandpopulate.entry.OperationEntry;
import edu.clemson.cs.rsrg.typeandpopulate.exception.DuplicateSymbolException;
import edu.clemson.cs.rsrg.typeandpopulate.exception.NoSuchSymbolException;
import edu.clemson.cs.rsrg.typeandpopulate.query.NameQuery;
import edu.clemson.cs.rsrg.typeandpopulate.symboltables.MathSymbolTable;
import edu.clemson.cs.rsrg.typeandpopulate.symboltables.MathSymbolTableBuilder;
import edu.clemson.cs.rsrg.typeandpopulate.symboltables.ModuleScope;
import edu.clemson.cs.rsrg.typeandpopulate.typereasoning.TypeGraph;
import edu.clemson.cs.rsrg.typeandpopulate.utilities.ModuleIdentifier;
import edu.clemson.cs.rsrg.vcgeneration.proofrules.AbstractProofRuleApplication;
import edu.clemson.cs.rsrg.vcgeneration.proofrules.ProofRuleApplication;
import edu.clemson.cs.rsrg.vcgeneration.utilities.AssertiveCodeBlock;
import edu.clemson.cs.rsrg.vcgeneration.utilities.InstantiatedFacilityDecl;
import edu.clemson.cs.rsrg.vcgeneration.utilities.Utilities;
import edu.clemson.cs.rsrg.vcgeneration.utilities.treewalkers.ConceptTypeExtractor;
import java.util.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

/**
 * <p>This class contains the logic for a {@code facility} declaration
 * rule.</p>
 *
 * @author Yu-Shan Sun
 * @version 1.0
 */
public class FacilityDeclRule extends AbstractProofRuleApplication
        implements
            ProofRuleApplication {

    // ===========================================================
    // Member Fields
    // ===========================================================

    // -----------------------------------------------------------
    // General
    // -----------------------------------------------------------

    /**
     * <p>A list that will be populated with the arguments used to
     * instantiate the {@code Concept}.</p>
     */
    private final List<Exp> myConceptActualArgList;

    /**
     * <p>A list that will be populated with the instantiating
     * {@code Concept}'s formal parameters.</p>
     */
    private final List<VarExp> myConceptFormalParamList;

    /**
     * <p>The module scope for the file we are generating
     * {@code VCs} for.</p>
     */
    private final ModuleScope myCurrentModuleScope;

    /** <p>The {@code facility} declaration we are applying the rule to.</p> */
    private final FacilityDec myFacilityDec;

    /** <p>A flag that indicates if this is a local facility declaration or not.</p> */
    private final boolean myIsLocalFacilityDec;

    /** <p>The list of processed {@link InstantiatedFacilityDecl}. </p> */
    private final List<InstantiatedFacilityDecl> myProcessedInstFacilityDecls;

    /** <p>The symbol table we are currently building.</p> */
    private final MathSymbolTableBuilder mySymbolTable;

    /**
     * <p>This is the math type graph that indicates relationship
     * between different math types.</p>
     */
    private final TypeGraph myTypeGraph;

    // -----------------------------------------------------------
    // InstantiatedFacilityDecl - Related
    // -----------------------------------------------------------

    /**
     * <p>This maps all {@code Concept} formal arguments to the instantiated
     * actual arguments.</p>
     */
    private final Map<Exp, Exp> myConceptArgMap;

    /** <p>This contains all the types declared by the {@code Concept}.</p> */
    private final List<TypeFamilyDec> myConceptDeclaredTypes;

    /**
     * <p>This maps all {@code Concept Realization} formal arguments to the instantiated
     * actual arguments.</p>
     */
    private final Map<Exp, Exp> myConceptRealizArgMap;

    /**
     * <p>This maps all {@code Enhancement} and {@code Enhancement Realization} formal arguments
     * to the instantiated actual arguments.</p>
     */
    private final Map<PosSymbol, Map<Exp, Exp>> myEnhancementArgMaps;

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * <p>This creates a new application for a {@code facility}
     * declaration rule.</p>
     *
     * @param facilityDec The {@code facility} declaration we are applying the
     *                    rule to.
     * @param isLocalFacDec A flag that indicates if this is a local {@link FacilityDec}.
     * @param processedInstFacDecs The list of processed {@link InstantiatedFacilityDecl}.
     * @param symbolTableBuilder The current symbol table.
     * @param moduleScope The current module scope we are visiting.
     * @param block The assertive code block that the subclasses are
     *              applying the rule to.
     * @param stGroup The string template group we will be using.
     * @param blockModel The model associated with {@code block}.
     */
    public FacilityDeclRule(FacilityDec facilityDec, boolean isLocalFacDec,
            List<InstantiatedFacilityDecl> processedInstFacDecs,
            MathSymbolTableBuilder symbolTableBuilder, ModuleScope moduleScope,
            AssertiveCodeBlock block, STGroup stGroup, ST blockModel) {
        super(block, stGroup, blockModel);
        myConceptActualArgList = new ArrayList<>();
        myConceptFormalParamList = new ArrayList<>();
        myCurrentModuleScope = moduleScope;
        myFacilityDec = facilityDec;
        myIsLocalFacilityDec = isLocalFacDec;
        myProcessedInstFacilityDecls = processedInstFacDecs;
        mySymbolTable = symbolTableBuilder;
        myTypeGraph = symbolTableBuilder.getTypeGraph();

        // Objects needed to create a new InstantiatedFacilityDecl
        myConceptDeclaredTypes = new LinkedList<>();
        myConceptArgMap = new LinkedHashMap<>();
        myConceptRealizArgMap = new LinkedHashMap<>();
        myEnhancementArgMaps = new LinkedHashMap<>();
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /**
     * <p>This method applies the {@code Proof Rule}.</p>
     */
    @Override
    public final void applyRule() {
        // Apply the part of the rule that deals with the facility
        // concept and its associated realization.
        Exp retExpPart1 = applyConceptRelatedPart();

        // Apply the part of the rule that deals with the facility
        // enhancements and its associated realizations.
        Exp retExpPart2 = applyEnhacementRelatedPart();

        // This class is used by any importing facility declarations as well as
        // any local facility declarations. We really don't need to display
        // anything to our models if it isn't local. - YS
        if (myIsLocalFacilityDec) {
            // Add the different details to the various different output models
            ST stepModel = mySTGroup.getInstanceOf("outputVCGenStep");
            stepModel.add("proofRuleName", getRuleDescription()).add(
                    "currentStateOfBlock", myCurrentAssertiveCodeBlock);
            myBlockModel.add("vcGenSteps", stepModel.render());
        }
    }

    /**
     * <p>This method returns an object that records all relevant information
     * for the instantiated {@code Facility}.</p>
     *
     * @return A {@link InstantiatedFacilityDecl} containing all the information.
     */
    public final InstantiatedFacilityDecl getInstantiatedFacilityDecl() {
        return new InstantiatedFacilityDecl(myFacilityDec,
                myConceptDeclaredTypes, myConceptArgMap, myConceptRealizArgMap,
                myEnhancementArgMaps);
    }

    /**
     * <p>This method returns a description associated with
     * the {@code Proof Rule}.</p>
     *
     * @return A string.
     */
    @Override
    public final String getRuleDescription() {
        return "Facility Instantiation Rule";
    }

    // ===========================================================
    // Private Methods
    // ===========================================================

    // -----------------------------------------------------------
    // General
    // -----------------------------------------------------------

    /**
     * <p>An helper method that creates a list of {@link Exp Exps}
     * representing each of the {@link ModuleArgumentItem ModuleArgumentItems}.
     * It is possible that the passed in programming expression
     * contains nested function calls, therefore we will need to deal
     * with it appropriately.</p>
     *
     * @param actualArgs List of module instantiated arguments.
     *
     * @return A list containing the {@link Exp Exps} representing
     * each actual argument.
     */
    private List<Exp> createModuleArgExpList(List<ModuleArgumentItem> actualArgs) {
        List<Exp> retExpList = new ArrayList<>();

        for (ModuleArgumentItem item : actualArgs) {
            // Convert the module argument items into the equivalent
            // mathematical expression.
            ProgramExp moduleArgumentExp = item.getArgumentExp();
            Exp moduleArgumentAsExp;
            if (moduleArgumentExp instanceof ProgramFunctionExp) {
                // TODO: Use the program function expression walker to generate moduleArgumentAsExp.
                throw new MiscErrorException("[VCGenerator] Insert program function expression walker here!",
                        new RuntimeException());
            }
            else {
                // Simply convert to the math equivalent expression
                moduleArgumentAsExp = Utilities.convertExp(moduleArgumentExp, myCurrentModuleScope);
            }

            // Add this to our return list
            retExpList.add(moduleArgumentAsExp);
        }

        return retExpList;
    }

    /**
     * <p>An helper method that creates a list of {@link VarExp VarExps}
     * representing each of the {@link ModuleParameterDec ModuleParameterDecs}.</p>
     *
     * @param formalParams List of module formal parameters.
     *
     * @return A list containing the {@link VarExp VarExps} representing
     * each formal parameter.
     */
    private List<VarExp> createModuleParamExpList(List<ModuleParameterDec> formalParams) {
        List<VarExp> retExpList = new ArrayList<>(formalParams.size());

        // Create a VarExp representing each of the module arguments
        for (ModuleParameterDec dec : formalParams) {
            // Use the wrapped declaration name and type to create a VarExp.
            Dec wrappedDec = dec.getWrappedDec();
            retExpList.add(Utilities.createVarExp(wrappedDec.getLocation(),
                    null, wrappedDec.getName(), wrappedDec.getMathType(), null));
        }

        return retExpList;
    }

    /**
     * <p>An helper method that creates a list of {@link VarExp VarExps}
     * representing each of the {@code Operation's} {@link ParameterVarDec ParameterVarDecs}.</p>
     *
     * @param parameterVarDecs List of operation parameters.
     *
     * @return A list containing the {@link VarExp VarExps} representing
     * each operation parameter.
     */
    private List<VarExp> createOperationParamExpList(List<ParameterVarDec> parameterVarDecs) {
        List<VarExp> retExpList = new ArrayList<>(parameterVarDecs.size());

        // Create a VarExp representing each of the operation parameters
        for (ParameterVarDec dec : parameterVarDecs) {
            retExpList.add(Utilities.createVarExp(dec.getLocation(),
                    null, dec.getName(), dec.getMathType(), null));
        }

        return retExpList;
    }

    /**
     * <p>An helper method that replaces the module parameters with the
     * actual instantiated arguments. Note that both of these have been
     * converted to mathematical expressions.</p>
     *
     * @param exp The expression to be replaced.
     * @param formalParams List of module formal parameters.
     * @param actualArgs List of module instantiated arguments.
     *
     * @return The modified expression.
     */
    private Exp replaceFormalWithActual(Exp exp, List<VarExp> formalParams, List<Exp> actualArgs) {
        // YS: We need two replacement maps in case we happen to have the
        // same names in formal parameters expressions and in the argument list.
        Map<Exp, Exp> paramToTemp = new HashMap<>();
        Map<Exp, Exp> tempToActual = new HashMap<>();

        Exp retExp = exp.clone();
        if (formalParams.size() == actualArgs.size()) {
            // Loop through both lists
            for (int i = 0; i < formalParams.size(); i++) {
                VarExp formalParam = formalParams.get(i);
                Exp actualArg = actualArgs.get(i);

                // A temporary VarExp that avoids any formal with the same name as the actual.
                VarExp tempExp = Utilities.createVarExp(formalParam.getLocation(), null,
                        new PosSymbol(formalParam.getLocation(), "_" + formalParam.getName().getName()),
                        actualArg.getMathType(), actualArg.getMathTypeValue());

                // Add a substitution entry from formal parameter to tempExp.
                paramToTemp.put(formalParam, tempExp);

                // Add a substitution entry from tempExp to actual parameter.
                tempToActual.put(tempExp, actualArg);
            }

            // Replace from formal to temp and then from temp to actual
            retExp = retExp.substitute(paramToTemp);
            retExp = retExp.substitute(tempToActual);
        }
        else {
            // Something went wrong while obtaining the parameter and argument lists.
            throw new MiscErrorException(
                    "[VCGenerator] Formal parameter size is different than actual argument size.",
                    new RuntimeException());
        }

        return retExp;
    }

    /**
     * <p>An helper method that searches for an {@link OperationEntry} using
     * the provided qualifier and name.</p>
     *
     * @param loc The location in the AST that we are currently visiting.
     * @param qualifier The qualifier of the operation.
     * @param name The name of the operation.
     *
     * @return An {@link OperationEntry} from the symbol table.
     */
    private OperationEntry searchOperation(Location loc, PosSymbol qualifier,
            PosSymbol name) {
        // Query for the corresponding operation
        OperationEntry op = null;
        try {
            op =
                    myCurrentModuleScope
                            .queryForOne(
                                    new NameQuery(
                                            qualifier,
                                            name,
                                            MathSymbolTable.ImportStrategy.IMPORT_NAMED,
                                            MathSymbolTable.FacilityStrategy.FACILITY_INSTANTIATE,
                                            true)).toOperationEntry(loc);
        }
        catch (NoSuchSymbolException nsse) {
            Utilities.noSuchSymbol(qualifier, name.getName(), loc);
        }
        catch (DuplicateSymbolException dse) {
            //This should be caught earlier, when the duplicate operation is
            //created
            throw new RuntimeException(dse);
        }

        return op;
    }

    // -----------------------------------------------------------
    // Proof Rule - Related
    // -----------------------------------------------------------

    /**
     * <p>An helper method that applies the part of the rule that deals with
     * {@code Concept} and {@code Concept Realizations}.</p>
     *
     * @return An {@link Exp} that contains the {@code Concept}'s and
     * {@code Concept Realization}'s modified requires clauses and any
     * passed-in operations requires clauses and ensures clause.
     */
    private Exp applyConceptRelatedPart() {
        Exp retExp =
                VarExp.getTrueVarExp(myFacilityDec.getLocation().clone(),
                        myTypeGraph);
        try {
            // Obtain the concept module for the facility
            ConceptModuleDec facConceptDec =
                    (ConceptModuleDec) mySymbolTable.getModuleScope(
                            new ModuleIdentifier(myFacilityDec.getConceptName()
                                    .getName())).getDefiningElement();

            // Extract the concept's type declarations
            ConceptTypeExtractor typeExtractor = new ConceptTypeExtractor();
            TreeWalker.visit(typeExtractor, facConceptDec);
            myConceptDeclaredTypes.addAll(typeExtractor.getTypeFamilyDecs());

            // Obtain the concept's requires clause
            Exp conceptReq =
                    facConceptDec.getRequires().getAssertionExp().clone();

            // Convert the concept's module parameters and the instantiated
            // concept's arguments into the appropriate mathematical expressions.
            // Note that any nested function calls will be dealt with appropriately.
            myConceptFormalParamList
                    .addAll(createModuleParamExpList(facConceptDec
                            .getParameterDecs()));
            myConceptActualArgList.addAll(createModuleArgExpList(myFacilityDec
                    .getConceptParams()));

            // Create a mapping from concept formal parameters
            // to actual arguments for future use.
            for (int i = 0; i < myConceptFormalParamList.size(); i++) {
                myConceptArgMap.put(myConceptFormalParamList.get(i),
                        myConceptActualArgList.get(i));
            }

            // Step 1: Substitute concept realization's formal parameters with
            //         actual instantiation arguments for the concept realization's
            //         requires clause.
            //         ( RPC[ rn~>rn_exp, RR~>IRR ] )
            // Note: Only to this step if we don't have an external realization
            Exp conceptRealizReq =
                    VarExp.getTrueVarExp(myFacilityDec.getLocation().clone(),
                            myTypeGraph);
            Exp conceptRealizOperationPart =
                    VarExp.getTrueVarExp(myFacilityDec.getLocation().clone(),
                            myTypeGraph);
            if (!myFacilityDec.getExternallyRealizedFlag()) {
                try {
                    // Obtain the concept realization module for the facility
                    ConceptRealizModuleDec facConceptRealizDec =
                            (ConceptRealizModuleDec) mySymbolTable
                                    .getModuleScope(
                                            new ModuleIdentifier(myFacilityDec
                                                    .getConceptRealizName()
                                                    .getName()))
                                    .getDefiningElement();

                    // Obtain the concept's requires clause
                    conceptRealizReq =
                            facConceptRealizDec.getRequires().getAssertionExp()
                                    .clone();

                    // Convert the concept realization's module parameters and the instantiated
                    // concept's arguments into the appropriate mathematical expressions.
                    // Note that any nested function calls will be dealt with appropriately.
                    List<VarExp> conceptRealizFormalParamList =
                            createModuleParamExpList(facConceptRealizDec
                                    .getParameterDecs());
                    List<Exp> conceptRealizActualArgList =
                            createModuleArgExpList(myFacilityDec
                                    .getConceptRealizParams());

                    // Replace the formal with the actual (if conceptRealizReq /= true)
                    if (!MathExp.isLiteralTrue(conceptRealizReq)) {
                        conceptRealizReq =
                                replaceFormalWithActual(conceptRealizReq,
                                        conceptRealizFormalParamList,
                                        conceptRealizActualArgList);

                        // Step 2a: Substitute concept's formal parameters with actual
                        //          instantiation arguments for the concept realization's
                        //          requires clause.
                        //          ( ( RPC[ rn~>rn_exp, RR~>IRR ] ∧ CPC )[ n~>n_exp, R~>IR ] )
                        //
                        // YS: This isn't exactly what the rule says, but it makes it easier
                        //     to record the location details for displaying purposes. Doing
                        //     the substitution first and then forming the conjunct is the same
                        //     as forming the conjunct first and then doing the substitution.
                        conceptRealizReq =
                                replaceFormalWithActual(conceptRealizReq,
                                        myConceptFormalParamList,
                                        conceptRealizActualArgList);

                        // Store the location detail for this requires clause
                        myLocationDetails.put(conceptRealizReq.getLocation(),
                                "Requires Clause for "
                                        + facConceptRealizDec.getName()
                                                .getName() + " in "
                                        + getRuleDescription());
                    }

                    // Iterate through searching for any operations being passed as parameters.
                    Iterator<ModuleParameterDec> realizFormalParams =
                            facConceptRealizDec.getParameterDecs().iterator();
                    Iterator<ModuleArgumentItem> realizActualArgs =
                            myFacilityDec.getConceptRealizParams().iterator();
                    while (realizFormalParams.hasNext()) {
                        ModuleParameterDec moduleParameterDec =
                                realizFormalParams.next();
                        ModuleArgumentItem moduleArgumentItem =
                                realizActualArgs.next();

                        // Only care about OperationDecs
                        if (moduleParameterDec.getWrappedDec() instanceof OperationDec) {
                            // Formal operation defined in the specifications and
                            // the operation being passed as argument
                            OperationDec formalOperationDec =
                                    (OperationDec) moduleParameterDec
                                            .getWrappedDec();

                            ProgramVariableNameExp operationNameExp =
                                    (ProgramVariableNameExp) moduleArgumentItem
                                            .getArgumentExp();
                            OperationEntry actualOperationEntry =
                                    searchOperation(moduleArgumentItem
                                            .getLocation(), operationNameExp
                                            .getQualifier(), operationNameExp
                                            .getName());
                            OperationDec actualOperationDec =
                                    (OperationDec) actualOperationEntry
                                            .getDefiningElement();

                            // Step 3: Substitute any operations's requires and ensures clauses
                            //         passed to the concept realization instantiation.
                            //         ( preRP[ rn~>rn_exp, rx~>irx ] => preIRP ) ∧
                            //         ( postIRP => postRP[ rn~>rn_exp, #rx~>#irx, rx~>irx ] )
                            Exp processedOperationPart =
                                    applyOperationRelatedPart(
                                            moduleArgumentItem.getLocation()
                                                    .clone(),
                                            formalOperationDec,
                                            operationNameExp.getQualifier(),
                                            actualOperationDec,
                                            new ArrayList<VarExp>(),
                                            new ArrayList<Exp>(),
                                            conceptRealizFormalParamList,
                                            conceptRealizActualArgList);
                            if (VarExp
                                    .isLiteralTrue(conceptRealizOperationPart)) {
                                conceptRealizOperationPart =
                                        processedOperationPart;
                            }
                            else {
                                // YS - Don't need to form a conjunct if processed operation part is "true".
                                if (!VarExp
                                        .isLiteralTrue(processedOperationPart)) {
                                    conceptRealizOperationPart =
                                            InfixExp.formConjunct(myFacilityDec
                                                    .getLocation().clone(),
                                                    conceptRealizOperationPart,
                                                    processedOperationPart);
                                }
                            }
                        }
                    }

                    // Create a mapping from concept realization formal parameters
                    // to actual arguments for future use.
                    for (int i = 0; i < conceptRealizFormalParamList.size(); i++) {
                        myConceptRealizArgMap.put(conceptRealizFormalParamList
                                .get(i), conceptRealizActualArgList.get(i));
                    }
                }
                catch (NoSuchSymbolException e) {
                    Utilities.noSuchModule(myFacilityDec.getConceptRealizName()
                            .getLocation());
                }
            }

            // Step 2b: Substitute concept's formal parameters with actual
            //          instantiation arguments for the concept's requires clause.
            //          Form the appropriate step 1 expression!
            //          ( ( RPC[ rn~>rn_exp, RR~>IRR ] ∧ CPC )[ n~>n_exp, R~>IR ] )
            //
            // YS: Replace the formal with the actual (if conceptReq /= true)
            if (!MathExp.isLiteralTrue(conceptReq)) {
                conceptReq =
                        replaceFormalWithActual(conceptReq,
                                myConceptFormalParamList,
                                myConceptActualArgList);

                // Store the location detail for this requires clause
                myLocationDetails.put(conceptReq.getLocation(),
                        "Requires Clause for "
                                + facConceptDec.getName().getName() + " in "
                                + getRuleDescription());
            }

            // Results from applying steps 1, 2a and 2b.
            Exp conceptRequiresConjuct;
            if (VarExp.isLiteralTrue(conceptRealizReq)) {
                conceptRequiresConjuct = conceptReq;
            }
            else {
                if (VarExp.isLiteralTrue(conceptReq)) {
                    conceptRequiresConjuct = conceptRealizReq;
                }
                else {
                    // YS: The rule does put the CPC in the second part of the conjunct,
                    //     But I want the requires clause VC for concept to come before
                    //     it's realizations.
                    conceptRequiresConjuct =
                            InfixExp.formConjunct(myFacilityDec.getLocation()
                                    .clone(), conceptReq, conceptRealizReq);
                }
            }

            // Combine with any expressions generated by step 3.
            if (VarExp.isLiteralTrue(conceptRealizOperationPart)) {
                retExp = conceptRequiresConjuct;
            }
            else {
                if (VarExp.isLiteralTrue(conceptRequiresConjuct)) {
                    retExp = conceptRealizOperationPart;
                }
                else {
                    retExp =
                            InfixExp.formConjunct(myFacilityDec.getLocation()
                                    .clone(), conceptRequiresConjuct,
                                    conceptRealizOperationPart);
                }
            }
        }
        catch (NoSuchSymbolException e) {
            Utilities
                    .noSuchModule(myFacilityDec.getConceptName().getLocation());
        }

        return retExp;
    }

    /**
     * <p>An helper method that applies the part of the rule that deals with
     * {@code Enhancement} and {@code Enhancement Realizations}.</p>
     *
     * @return An {@link Exp} that contains the {@code Enhancement}'s and
     * {@code Enhancement Realization}'s modified requires clauses and any
     * passed-in operations requires clauses and ensures clause.
     */
    private Exp applyEnhacementRelatedPart() {
        Exp retExp =
                VarExp.getTrueVarExp(myFacilityDec.getLocation().clone(),
                        myTypeGraph);

        for (EnhancementSpecRealizItem specRealizItem : myFacilityDec
                .getEnhancementRealizPairs()) {
            // Enhancement part of the rule
            List<VarExp> enhancementFormalParamList;
            List<Exp> enhancementActualArgList;
            Map<Exp, Exp> enhancementArgMap = new LinkedHashMap<>();
            try {
                // Obtain the enhancement module for the facility
                EnhancementModuleDec enhancementModuleDec =
                        (EnhancementModuleDec) mySymbolTable.getModuleScope(
                                new ModuleIdentifier(specRealizItem
                                        .getEnhancementName().getName()))
                                .getDefiningElement();

                // Obtain the enhancement's requires clause
                Exp enhancementReq =
                        enhancementModuleDec.getRequires().getAssertionExp().clone();

                // Convert the enhancement's module parameters and the instantiated
                // concept's arguments into the appropriate mathematical expressions.
                // Note that any nested function calls will be dealt with appropriately.
                enhancementFormalParamList =
                        createModuleParamExpList(enhancementModuleDec.getParameterDecs());
                enhancementActualArgList =
                        createModuleArgExpList(specRealizItem.getEnhancementParams());

                // Create a mapping from concept formal parameters
                // to actual arguments for future use.
                for (int i = 0; i < enhancementFormalParamList.size(); i++) {
                    enhancementArgMap.put(enhancementFormalParamList.get(i),
                            enhancementActualArgList.get(i));
                }
            }
            catch (NoSuchSymbolException e) {
                Utilities.noSuchModule(specRealizItem.getEnhancementName()
                        .getLocation());
            }

            // Enhancement realization part of the rule
            try {
                // Obtain the enhancement module for the facility
                EnhancementRealizModuleDec enhancementRealizModuleDec =
                        (EnhancementRealizModuleDec) mySymbolTable
                                .getModuleScope(
                                        new ModuleIdentifier(specRealizItem
                                                .getEnhancementRealizName()
                                                .getName()))
                                .getDefiningElement();

                // Obtain the enhancement realization's requires clause
                Exp realizationReq =
                        enhancementRealizModuleDec.getRequires().getAssertionExp().clone();

                // Convert the enhancement's module parameters and the instantiated
                // concept's arguments into the appropriate mathematical expressions.
                // Note that any nested function calls will be dealt with appropriately.
                List<VarExp> enhancementRealizFormalParamList =
                        createModuleParamExpList(enhancementRealizModuleDec.getParameterDecs());
                List<Exp> enhancementRealizActualArgList =
                        createModuleArgExpList(specRealizItem.getEnhancementRealizParams());

                // Create a mapping from concept formal parameters
                // to actual arguments for future use.
                Map<Exp, Exp> enhancementRealizArgMap = new LinkedHashMap<>();
                for (int i = 0; i < enhancementRealizFormalParamList.size(); i++) {
                    enhancementArgMap.put(enhancementRealizFormalParamList.get(i),
                            enhancementRealizActualArgList.get(i));
                }
            }
            catch (NoSuchSymbolException e) {
                Utilities.noSuchModule(specRealizItem
                        .getEnhancementRealizName().getLocation());
            }
        }

        return retExp;
    }

    /**
     * <p>An helper method that applies the part of the rule that deals with
     * passing {@code Operations} as parameters.</p>
     *
     * @param argLoc A {@link Location} object that indicates where the {@code Operation}
     *               is being passed as argument.
     * @param formalOpDec The formal {@link OperationDec} specified in the formal parameters.
     * @param actualOpQualifier The module qualifier indicating where the {@code actualOpDec}
     *                          originated from.
     * @param actualOpDec The actual {@link OperationDec} being passed to the instantiation.
     * @param enhancementFormalParamList The list of {@code Enhancement} formal parameters.
     *                                   If we are processing {@code Concept Realizations},
     *                                   this list will be empty.
     * @param enhancementActualArgList The list of arguments instantiating the {@code Enhancement}.
     *                                 If we are processing {@code Concept Realizations},
     *                                 this list will be empty.
     * @param realizFormalParamList The list of {@code Realization} formal parameters.
     * @param realizActualArgList The list of arguments instantiating the {@code Realization}.
     *
     * @return An {@link Exp} that contains the passed-in operations requires clauses
     * and ensures clause that must be true to successfully pass the operation as parameter.
     */
    private Exp applyOperationRelatedPart(Location argLoc,
            OperationDec formalOpDec, PosSymbol actualOpQualifier,
            OperationDec actualOpDec, List<VarExp> enhancementFormalParamList,
            List<Exp> enhancementActualArgList,
            List<VarExp> realizFormalParamList, List<Exp> realizActualArgList) {
        Exp retExp = VarExp.getTrueVarExp(argLoc, myTypeGraph);

        // Replace concept/enhancement/realization formals with actual
        // instantiations in the formalOpRequires/formalOpEnsures clauses
        Exp formalOpRequires = formalOpDec.getRequires().getAssertionExp();
        Exp formalOpEnsures = formalOpDec.getEnsures().getAssertionExp();
        formalOpRequires =
                replaceFormalWithActual(formalOpRequires, myConceptFormalParamList, myConceptActualArgList);
        formalOpRequires =
                replaceFormalWithActual(formalOpRequires, enhancementFormalParamList, enhancementActualArgList);
        formalOpRequires =
                replaceFormalWithActual(formalOpRequires, realizFormalParamList, realizActualArgList);

        formalOpEnsures =
                replaceFormalWithActual(formalOpEnsures, myConceptFormalParamList, myConceptActualArgList);
        formalOpEnsures =
                replaceFormalWithActual(formalOpEnsures, enhancementFormalParamList, enhancementActualArgList);
        formalOpEnsures =
                replaceFormalWithActual(formalOpEnsures, realizFormalParamList, realizActualArgList);

        // Things related to actualOpDec
        Exp actualOpRequires = actualOpDec.getRequires().getAssertionExp();
        Exp actualOpEnsures = actualOpDec.getEnsures().getAssertionExp();

        // YS - We need to replace the requires/ensures clauses to include any
        // qualifiers to distinguish the operation from others with the same name.
        if (actualOpQualifier != null) {
            List<VarExp> actualOpParamsAsVarExp =
                    createOperationParamExpList(actualOpDec.getParameters());
            if (actualOpDec.getReturnTy() != null) {
                actualOpParamsAsVarExp.add(Utilities.createVarExp(actualOpDec
                                .getReturnTy().getLocation(), actualOpQualifier,
                        actualOpDec.getName(), actualOpDec.getReturnTy()
                                .getMathType(), null));
            }

            Map<Exp, Exp> varExpReplacementMap = new HashMap<>(actualOpParamsAsVarExp.size());
            for (VarExp paramExp : actualOpParamsAsVarExp) {
                varExpReplacementMap.put(paramExp, Utilities.createVarExp(paramExp.getLocation(),
                        actualOpQualifier.clone(), paramExp.getName().clone(),
                        paramExp.getMathType(), paramExp.getMathTypeValue()));
            }

            // Apply the replacement for any outgoing variables
            actualOpRequires = actualOpRequires.substitute(varExpReplacementMap);
            actualOpEnsures = actualOpEnsures.substitute(varExpReplacementMap);
        }

        // Facility Decl Rule (Operations as Parameters Part 1):
        // preRP [ rn ~> rn_exp, rx ~> irx ] implies preIRP
        // YS - Only do this if preIRP isn't just true.
        if (!VarExp.isLiteralTrue(actualOpRequires)) {
            retExp = InfixExp.formImplies(actualOpRequires.getLocation(),
                    formalOpRequires, actualOpRequires);

            // Store the location detail for this implication
            String message = "Requires Clause of " +
                    formalOpDec.getName().getName() + " implies the Requires Clause of ";
            if (actualOpQualifier != null) {
                message += (actualOpQualifier.getName() + "::");
            }
            myLocationDetails.put(retExp.getLocation(),
                    message + actualOpDec.getName().getName() + " in " + getRuleDescription());
        }

        // Facility Decl Rule (Operations as Parameters Part 2):
        // postIRP implies postRP [ rn ~> rn_exp, #rx ~> #irx, rx ~> irx ]
        // YS - Only do this if postRP isn't just true.
        if (!VarExp.isLiteralTrue(formalOpEnsures)) {
            Exp impliesExp = InfixExp.formImplies(actualOpEnsures.getLocation(),
                    actualOpEnsures, formalOpEnsures);

            // Store the location detail for this implication
            String message = "Ensures Clause of ";
            if (actualOpQualifier != null) {
                message += (actualOpQualifier.getName() + "::");
            }
            myLocationDetails.put(retExp.getLocation(), message + actualOpDec.getName().getName() +
                    " implies the Ensures Clause of " + formalOpDec.getName().getName() +
                    " in " + getRuleDescription());

            // Form a conjunct if needed
            if (!VarExp.isLiteralTrue(retExp)) {
                retExp = InfixExp.formConjunct(actualOpDec.getLocation(), retExp, impliesExp);
            }
            else {
                retExp = impliesExp;
            }
        }

        return retExp;
    }
}