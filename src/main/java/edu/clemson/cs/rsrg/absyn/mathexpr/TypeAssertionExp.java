/**
 * TypeAssertionExp.java
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
import edu.clemson.cs.rsrg.absyn.rawtypes.ArbitraryExpTy;
import edu.clemson.cs.rsrg.parsing.data.Location;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>A <code>TypeAssertionExp</code>, generally, allows a sub-expression to be
 * asserted as belonging to some type.  It is permitted only in two places:
 * 
 * <ul>
 *      <li>Inside the hierarchy of an {@link ArbitraryExpTy ArbitraryExpTy}
 *          when that ty appears in the context of the type of a formal 
 *          parameter to a mathematical definition.</li>
 *      <li>Inside a {@link TypeTheoremDec TypeTheoremDec}, either at the top
 *          level of the overall assertion, or at the top level of the 
 *          consequent of a top-level <em>implies</em> expression.</li>
 * </ul>
 * 
 * <p>In the first case, it defines a slot to be bound implicitly by some 
 * component of the type of the actual parameter.  In this case, the expression
 * must be a {@link VarExp VarExp}.  Some examples:</p>
 * 
 * <pre>
 * Definition Foo(S : Str(T : Powerset(Z)) : T;
 * </pre>
 * 
 * <p>Here, the <code>T : Powerset(Z)</code> part is an implicit type parameter.
 * If one were to pass a <code>Str(N)</code> to <code>Foo</code>, then the 
 * return type would be <code>T</code>.  If one were to pass <code>Str(B)</code>
 * to <code>Foo</code> that would be a typechecking error, since <code>B</code>
 * is not in <code>Powerset(Z)</code>.</p>
 * 
 * <pre>
 * Definition Bar(L : Set(T : MType), E : Powerset(T)) : T;
 * </pre>
 * 
 * <p>Here, <code>T : MType</code> is an implicit type parameter, and the type
 * of both the second parameter and the return value are dependent on the 
 * binding of that parameter.  Implicit parameters are always bound left to 
 * right, so the first parameter's type may not depend on the second's.</p>
 * 
 * <p>In the second case, it asserts that any expression on the left side of the
 * colon is of the type on the right side, provided the antecedents of any
 * top-level implication are fulfilled.  In this case, the expression may be any
 * kind of expression.</p>
 *
 * @version 2.0
 */
public class TypeAssertionExp extends MathExp {

    // ===========================================================
    // Member Fields
    // ===========================================================

    /** <p>The type assertion's name</p> */
    private final Exp myExp;

    /** <p>The type assertion's raw type</p> */
    private final ArbitraryExpTy myAssertedTy;

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * <p>This constructs a type assertion expression.</p>
     *
     * @param l A {@link Location} representation object.
     * @param name Name as an {@link Exp} representation object.
     * @param ty A raw type {@link ArbitraryExpTy} representation object.
     */
    public TypeAssertionExp(Location l, Exp name, ArbitraryExpTy ty) {
        super(l);
        myExp = name;
        myAssertedTy = ty;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public final String asString(int indentSize, int innerIndentInc) {
        return myExp.asString(indentSize + innerIndentInc, innerIndentInc)
                + " : "
                + myAssertedTy.asString(indentSize + innerIndentInc,
                        innerIndentInc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean containsExp(Exp exp) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean containsVar(String varName, boolean IsOldExp) {
        return myExp.containsVar(varName, IsOldExp);
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

        TypeAssertionExp that = (TypeAssertionExp) o;

        if (!myExp.equals(that.myExp))
            return false;
        return myAssertedTy.equals(that.myAssertedTy);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equivalent(Exp e) {
        boolean retval = (e instanceof TypeAssertionExp);
        if (retval) {
            TypeAssertionExp eAsTypeAssertionExp = (TypeAssertionExp) e;
            retval = myExp.equivalent(eAsTypeAssertionExp.myExp);
            retval &= myAssertedTy.equals(eAsTypeAssertionExp.myAssertedTy);
        }

        return retval;
    }

    /**
     * <p>This method returns the type assertion's raw type.</p>
     *
     * @return The {@link ArbitraryExpTy} raw type.
     */
    public final ArbitraryExpTy getAssertedTy() {
        return myAssertedTy;
    }

    /**
     * <p>This method returns the type assertion's expression.</p>
     *
     * @return The {@link Exp} object.
     */
    public final Exp getExp() {
        return myExp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<Exp> getSubExpressions() {
        return new ArrayList(Collections.singletonList(myExp));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        int result = myExp.hashCode();
        result = 31 * result + myAssertedTy.hashCode();
        return result;
    }

    /**
     * <p>This method applies VC Generator's remember rule.
     * For all inherited programming expression classes, this method
     * should throw an exception.</p>
     *
     * @return The resulting {@link TypeAssertionExp} from applying the remember rule.
     */
    @Override
    public final TypeAssertionExp remember() {
        return (TypeAssertionExp) this.clone();
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
        sb.append(myExp.toString());
        sb.append("\t");
        sb.append(myAssertedTy.toString());

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
        return new TypeAssertionExp(new Location(myLoc), myExp.clone(),
                getAssertedTy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Exp substituteChildren(Map<Exp, Exp> substitutions) {
        return new TypeAssertionExp(new Location(myLoc), substitute(myExp,
                substitutions), new ArbitraryExpTy(myAssertedTy
                .getArbitraryExp().substitute(substitutions)));
    }

}