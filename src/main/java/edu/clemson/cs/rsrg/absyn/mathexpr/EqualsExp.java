/**
 * EqualsExp.java
 * ---------------------------------
 * Copyright (c) 2016
 * RESOLVE Software Research Group
 * School of Computing
 * Clemson University
 * All rights reserved.
 * ---------------------------------
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package edu.clemson.cs.rsrg.absyn.mathexpr;

import edu.clemson.cs.rsrg.absyn.Exp;
import edu.clemson.cs.rsrg.errorhandling.exception.MiscErrorException;
import edu.clemson.cs.rsrg.parsing.data.Location;
import edu.clemson.cs.rsrg.parsing.data.PosSymbol;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>This is the class for all the mathematical equality/inequality expression objects
 * that the compiler builds using the ANTLR4 AST nodes.</p>
 *
 * @version 2.0
 */
public class EqualsExp extends InfixExp {

    // ===========================================================
    // Operators
    // ===========================================================

    public enum Operator {
        EQUAL {

            @Override
            public String toString() {
                return "=";
            }

        },
        NOT_EQUAL {

            @Override
            public String toString() {
                return "/=";
            }

        };

        /**
         * <p>This method returns a deep copy of the operator name.</p>
         *
         * @param l A {@link Location} representation object.
         *
         * @return A {@link PosSymbol} object containing the operator.
         */
        public PosSymbol getOperatorAsPosSymbol(Location l) {
            return new PosSymbol(new Location(l), toString());
        }
    }

    // ===========================================================
    // Member Fields
    // ===========================================================

    /** <p>The expression's operation.</p> */
    private final Operator myOperator;

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * <p>This constructs an equality/inequality expression.</p>
     *
     * @param l A {@link Location} representation object.
     * @param qual A {@link PosSymbol} representing the expression's qualifier.
     * @param left A {@link Exp} representing the left hand side.
     * @param operator A {@link Operator} representing the operator.
     * @param right A {@link Exp} representing the right hand side.
     */
    public EqualsExp(Location l, PosSymbol qual, Exp left, Operator operator,
            Exp right) {
        super(l, qual, left, operator.getOperatorAsPosSymbol(l), right);
        myOperator = operator;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        EqualsExp equalsExp = (EqualsExp) o;

        return myOperator == equalsExp.myOperator;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equivalent(Exp e) {
        boolean retval = e instanceof EqualsExp;
        if (retval) {
            EqualsExp eAsEquals = (EqualsExp) e;
            retval =
                    (myOperator == eAsEquals.myOperator)
                            && (myLeftHandSide
                                    .equivalent(eAsEquals.myLeftHandSide))
                            && (myRightHandSide
                                    .equivalent(eAsEquals.myRightHandSide));
        }

        return retval;
    }

    /**
     * <p>This method returns the operator.</p>
     *
     * @return A {@link Operator} object containing the operator.
     */
    public final Operator getOperator() {
        return myOperator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return myOperator.hashCode();
    }

    /**
     * <p>This method applies VC Generator's remember rule.
     * For all inherited programming expression classes, this method
     * should throw an exception.</p>
     *
     * @return The resulting {@link EqualsExp} from applying the remember rule.
     */
    @Override
    public final EqualsExp remember() {
        Exp newLeft = ((MathExp) myLeftHandSide).remember();
        Exp newRight = ((MathExp) myRightHandSide).remember();

        PosSymbol qualifier = null;
        if (myQualifier != null) {
            qualifier = myQualifier.clone();
        }

        return new EqualsExp(new Location(myLoc), qualifier, newLeft,
                myOperator, newRight);
    }

    /**
     * <p>This method applies the VC Generator's simplification step.</p>
     *
     * @return The resulting {@link MathExp} from applying the simplification step.
     */
    @Override
    public final MathExp simplify() {
        Exp simplified;
        if (myLeftHandSide.equivalent(myRightHandSide)) {
            simplified =
                    MathExp.getTrueVarExp(myLoc, myMathType.getTypeGraph());
        }
        else {
            simplified = this.clone();
        }

        return (MathExp) simplified;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<InfixExp> split(MathExp assumpts, boolean single) {
        List<InfixExp> lst = new ArrayList<>();

        if (myOperator == Operator.EQUAL) {
            throw new MiscErrorException("Cannot split an EqualsExp!", new IllegalStateException());
        }
        else {
            if (myLeftHandSide != null) {
                lst.addAll(((MathExp) myLeftHandSide).split(assumpts, single));
            }
            if (myRightHandSide != null) {
                lst.addAll(((MathExp) myRightHandSide).split(assumpts, single));
            }
        }

        return lst;
    }

    // ===========================================================
    // Protected Methods
    // ===========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Exp copy() {
        PosSymbol qualifier = null;
        if (myQualifier != null) {
            qualifier = myQualifier.clone();
        }

        return new EqualsExp(new Location(myLoc), qualifier, myLeftHandSide
                .clone(), myOperator, myRightHandSide.clone());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Exp substituteChildren(Map<Exp, Exp> substitutions) {
        PosSymbol qualifier = null;
        if (myQualifier != null) {
            qualifier = myQualifier.clone();
        }

        return new EqualsExp(new Location(myLoc), qualifier, substitute(
                myLeftHandSide, substitutions), myOperator, substitute(
                myRightHandSide, substitutions));
    }

}