package edu.clemson.cs.r2jt.absyn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.clemson.cs.r2jt.analysis.TypeResolutionException;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.type.Type;

/**
 * <p>The value of a type that looks like:</p>
 * 
 * <pre>
 * CART_PROD
 *     X1 : T1;
 *     X2 : T2;
 *     ...
 *     Xn : Tn;
 * END;
 * </pre>
 */
public class CrossTypeExpression extends Exp {

    private final List<Exp> myFields = new LinkedList<Exp>();
    private final Map<PosSymbol, Exp> myTagsToFields =
            new HashMap<PosSymbol, Exp>();
    private final Map<Exp, PosSymbol> myFieldsToTags =
            new HashMap<Exp, PosSymbol>();

    private final Location myLocation;

    public CrossTypeExpression(Location location) {
        myLocation = location;
    }

    public void addField(Exp field) {
        myFields.add(field);
    }

    public void addTaggedField(PosSymbol tag, Exp field) {
        myTagsToFields.put(tag, field);
        myFieldsToTags.put(field, tag);
        addField(field);
    }

    public int getFieldCount() {
        return myFields.size();
    }

    public Exp getField(int index) {
        return myFields.get(index);
    }

    public Exp getField(PosSymbol e) {
        return myTagsToFields.get(e);
    }

    public boolean fieldIsTagged(int index) {
        return myFieldsToTags.containsKey(myFields.get(index));
    }

    public PosSymbol getTag(int index) {
        return myFieldsToTags.get(myFields.get(index));
    }

    @Override
    public void accept(ResolveConceptualVisitor v) {
        v.visitCrossTypeExpression(this);
    }

    @Override
    public Type accept(TypeResolutionVisitor v) throws TypeResolutionException {
        return v.getCrossTypeExpType(this);
    }

    @Override
    public String asString(int indent, int increment) {
        String result = "(";

        boolean first = true;
        for (Exp field : myFields) {
            if (first) {
                first = false;
            }
            else {
                result += ", ";
            }

            if (myFieldsToTags.containsKey(field)) {
                result += myFieldsToTags.get(field) + " : ";
            }

            result += field;
        }

        result += ")";

        return result;
    }

    @Override
    public Location getLocation() {
        return myLocation;
    }

    @Override
    public boolean containsVar(String varName, boolean IsOldExp) {
        boolean result = false;

        Iterator<Exp> fields = myFields.iterator();
        while (!result && fields.hasNext()) {
            result = fields.next().containsVar(varName, IsOldExp);
        }

        return result;
    }

    @Override
    public edu.clemson.cs.r2jt.collections.List<Exp> getSubExpressions() {
        return new edu.clemson.cs.r2jt.collections.List<Exp>(myFields);
    }

    @Override
    public void setSubExpression(int index, Exp e) {
        throw new UnsupportedOperationException("Cannot replace "
                + "subexpression in a cross type.");
    }

    @Override
    protected Exp substituteChildren(Map<Exp, Exp> substitutions) {
        throw new UnsupportedOperationException("Cannot substitute in a "
                + "cross type.");
    }

}