/*
 * RegisterAntecedent.java
 * ---------------------------------
 * Copyright (c) 2022
 * RESOLVE Software Research Group
 * School of Computing
 * Clemson University
 * All rights reserved.
 * ---------------------------------
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package edu.clemson.rsrg.nProver.utilities.treewakers;

import edu.clemson.rsrg.absyn.expressions.Exp;
import edu.clemson.rsrg.absyn.expressions.mathexpr.InfixExp;
import edu.clemson.rsrg.absyn.expressions.mathexpr.OutfixExp;
import edu.clemson.rsrg.absyn.expressions.mathexpr.VarExp;
import edu.clemson.rsrg.nProver.registry.CongruenceClassRegistry;
import edu.clemson.rsrg.treewalk.TreeWalkerStackVisitor;

import java.util.BitSet;
import java.util.Map;
import java.util.Queue;

/**
 * <p>
 * This class registers each antecedent expression. This visitor logic is implemented as a
 * {@link TreeWalkerStackVisitor}.
 * </p>
 *
 * @author Yu-Shan Sun
 * @author Nicodemus Msafiri J. M.
 *
 * @version 1.0
 */
public class RegisterAntecedent extends AbstractRegisterSequent {

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * <p>
     * This creates an object that labels all relevant {@link Exp} in the antecedents with a number and registers them.
     * </p>
     *
     * @param arguments
     * @param registry
     *            The registry that will contain the target sequent VC to be proved.
     * @param expLabels
     *            A mapping between expressions and its associated integer number.
     * @param nextLabel
     *            The number to be assigned initially as a label.
     */
    public RegisterAntecedent(Queue<Integer> arguments,
            CongruenceClassRegistry<Integer, String, String, String> registry, Map<String, Integer> expLabels,
            int nextLabel) {
        super(arguments, registry, expLabels, nextLabel);
    }

    // ===========================================================
    // Visitor Methods
    // ===========================================================

    // -----------------------------------------------------------
    // Math Expression-Related
    // -----------------------------------------------------------

    /**
     * <p>
     * Code that gets executed after visiting a {@link InfixExp}.
     * </p>
     *
     * @param exp
     *            An infix expression.
     */
    @Override
    public final void postInfixExp(InfixExp exp) {
        super.postInfixExp(exp);
        int operatorNumber = myExpLabels.get(exp.getOperatorAsString());
        int accessor = 0;

        // Logic for handling infix expressions in the antecedent

        if (operatorNumber == 2) { // if it is antecedent equal
            super.getRegistry().makeCongruent(super.getMyArguments().remove(), super.getMyArguments().remove());
        } else {
            // append arguments usable in registering the infix operator
            for (int i = 0; i < 2; i++) { // should only run twice
                super.getRegistry().appendToClusterArgList(super.getMyArguments().remove());
            }
            // check if registered, no duplicates allowed
            if (super.getRegistry().checkIfRegistered(operatorNumber)) {
                super.getMyArguments().add(super.getRegistry().getAccessorFor(operatorNumber));
            } else {
                // register if new, and make it an argument for the next higher level operator
                accessor = super.getRegistry().registerCluster(operatorNumber);

                // if exp is ultimate i.e., at root
                if (super.getAncestorSize() == 1) {
                    BitSet attb = new BitSet();
                    attb.set(0);// set the class antecedent
                    attb.set(2);// set the class ultimate
                    super.getRegistry().updateClassAttributes(accessor, attb);
                } else {
                    // only non-ultimate classes can be used as arguments in clusters
                    super.getMyArguments().add(accessor);
                }
            }
        }

    }

    /**
     * <p>
     * Code that gets executed after visiting a {@link OutfixExp}.
     * </p>
     *
     * @param exp
     *            An outfix expression.
     */
    @Override
    public void postOutfixExp(OutfixExp exp) {
        super.postOutfixExp(exp);
        int operatorNumber = myExpLabels.get(exp.getOperatorAsString());
        int accessor = 0;

        // Logic for handling outfix expressions in the antecedent

        // Has only one argument, it should run only once
        super.getRegistry().appendToClusterArgList(super.getMyArguments().remove());

        // check if registered, no duplicates allowed
        if (super.getRegistry().checkIfRegistered(operatorNumber)) {
            super.getMyArguments().add(super.getRegistry().getAccessorFor(operatorNumber));
        } else {
            // register if new, and make it an argument for the next higher level operator
            accessor = super.getRegistry().registerCluster(operatorNumber);

            // if exp is ultimate i.e., at root
            if (super.getAncestorSize() == 1) {
                BitSet attb = new BitSet();
                attb.set(0);// set the class antecedent
                attb.set(2);// set the class ultimate
                super.getRegistry().updateClassAttributes(accessor, attb);
            } else {
                // only non-ultimate classes can be used as arguments in clusters
                super.getMyArguments().add(accessor);
            }
        }
    }

    /**
     * <p>
     * Code that gets executed after visiting a {@link VarExp}.
     * </p>
     *
     * @param exp
     *            An outfix expression.
     */
    @Override
    public void postVarExp(VarExp exp) {
        super.postVarExp(exp);
        int variableNumber = myExpLabels.get(exp.toString());
        int accessor = 0;

        // Logic for handling variable expressions in the antecedent

        if (super.getRegistry().checkIfRegistered(variableNumber)) {
            super.getMyArguments().add(super.getRegistry().getAccessorFor(variableNumber));
        } else {
            accessor = super.getRegistry().registerCluster(variableNumber);
            super.getMyArguments().add(accessor);
        }

    }

}