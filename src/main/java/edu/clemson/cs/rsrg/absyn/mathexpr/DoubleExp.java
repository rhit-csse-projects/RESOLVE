/**
 * DoubleExp.java
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
import edu.clemson.cs.rsrg.parsing.data.Location;
import java.util.Map;

/**
 * <p>This is the class for all the mathematical double expression objects
 * that the compiler builds using the ANTLR4 AST nodes.</p>
 *
 * @version 2.0
 */
public class DoubleExp extends LiteralExp {

    // ===========================================================
    // Member Fields
    // ===========================================================

    /** <p>The double representing this mathematical double</p> */
    private final double myDouble;

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * <p>This constructs a mathematical double expression.</p>
     *
     * @param l A {@link Location} representation object.
     * @param d A double value.
     */
    public DoubleExp(Location l, double d) {
        super(l);
        myDouble = d;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public final String asString(int indentSize, int innerIndentInc) {
        StringBuffer sb = new StringBuffer();
        printSpace(indentSize, sb);

        printSpace(indentSize + innerIndentInc, sb);
        sb.append(myDouble);
        sb.append("\n");

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DoubleExp doubleExp = (DoubleExp) o;

        return Double.compare(doubleExp.myDouble, myDouble) == 0;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equivalent(Exp e) {
        boolean retval = (e instanceof DoubleExp);
        if (retval) {
            DoubleExp eAsDoubleExp = (DoubleExp) e;
            retval = (myDouble == eAsDoubleExp.myDouble);
        }

        return retval;
    }

    /**
     * <p>This method returns the double value.</p>
     *
     * @return The {@link double} value.
     */
    public final double getValue() {
        return myDouble;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        long temp = Double.doubleToLongBits(myDouble);
        return (int) (temp ^ (temp >>> 32));
    }

    /**
     * <p>This method applies VC Generator's remember rule.
     * For all inherited programming expression classes, this method
     * should throw an exception.</p>
     *
     * @return The resulting {@link DoubleExp} from applying the remember rule.
     */
    @Override
    public final DoubleExp remember() {
        return (DoubleExp) this.clone();
    }

    /**
     * <p>This method applies the VC Generator's simplification step.</p>
     *
     * @return The resulting {@link MathExp} from applying the simplification step.
     */
    @Override
    public final MathExp simplify() {
        return this.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(myDouble);

        return sb.toString();
    }

    // ===========================================================
    // Protected Methods
    // ===========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Exp copy() {
        return new DoubleExp(new Location(myLoc), myDouble);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Exp substituteChildren(Map<Exp, Exp> substitutions) {
        return new DoubleExp(new Location(myLoc), myDouble);
    }

}