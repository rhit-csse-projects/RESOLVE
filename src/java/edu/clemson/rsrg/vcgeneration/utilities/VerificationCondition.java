/*
 * VerificationCondition.java
 * ---------------------------------
 * Copyright (c) 2021
 * RESOLVE Software Research Group
 * School of Computing
 * Clemson University
 * All rights reserved.
 * ---------------------------------
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package edu.clemson.rsrg.vcgeneration.utilities;

import edu.clemson.rsrg.parsing.data.BasicCapabilities;
import edu.clemson.rsrg.parsing.data.Location;
import edu.clemson.rsrg.parsing.data.LocationDetailModel;
import edu.clemson.rsrg.vcgeneration.sequents.Sequent;

/**
 * <p>
 * This class represents a possibly-named {@code verification condition} ({@code VC}) represented by a {@link Sequent}.
 * </p>
 *
 * @author Yu-Shan Sun
 * 
 * @version 1.0
 */
public class VerificationCondition implements BasicCapabilities, Cloneable {

    // ===========================================================
    // Member Fields
    // ===========================================================

    /**
     * <p>
     * {@link Sequent} associated with this {@code VC}.
     * </p>
     */
    private final Sequent mySequent;

    /**
     * <p>
     * A flag that indicates whether or not this {@link VerificationCondition} contains a {@link Sequent} that had an
     * impacting reduction.
     * </p>
     */
    private final boolean myHasImpactingReduction;

    /**
     * <p>
     * The location for this {@code Sequent}.
     * </p>
     */
    private Location myLocation;

    /**
     * <p>
     * An object that contains additional information on why the {@code VCGenerator} generated this {@code VC}. This
     * model should come from the expressions that where generated by applying the various different
     * {@code proof rules}.
     * </p>
     */
    private final LocationDetailModel myLocationDetailModel;

    /**
     * <p>
     * Name given to the {@code VC}.
     * </p>
     */
    private final String myName;

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * <p>
     * This creates a {@code VC} with a name and it's associated {@link Sequent}.
     * </p>
     *
     * @param loc
     *            The location that created this {@code VC}.
     * @param name
     *            Name given to this {@code VC}.
     * @param sequent
     *            {@link Sequent}associated with this {@code VC}.
     * @param hasImpactingReduction
     *            A flag that indicates whether or not this {@code VC} had an impacting reduced {@link Sequent}.
     * @param model
     *            The {@link LocationDetailModel} associated with this {@code VC}.
     */
    public VerificationCondition(Location loc, String name, Sequent sequent, boolean hasImpactingReduction,
            LocationDetailModel model) {
        myHasImpactingReduction = hasImpactingReduction;
        myLocation = loc;
        myLocationDetailModel = model;
        myName = name;
        mySequent = sequent;
    }

    /**
     * <p>
     * This creates a nameless {@code VC} with the associated {@link Sequent}.
     * </p>
     *
     * @param loc
     *            The location that created this {@code VC}.
     * @param sequent
     *            {@link Sequent}associated with this {@code VC}.
     * @param hasImpactingReduction
     *            A flag that indicates whether or not this {@code VC} had an impacting reduced {@link Sequent}.
     * @param model
     *            The {@link LocationDetailModel} associated with this {@code VC}.
     */
    public VerificationCondition(Location loc, Sequent sequent, boolean hasImpactingReduction,
            LocationDetailModel model) {
        this(loc, null, sequent, hasImpactingReduction, model);
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /**
     * <p>
     * This method creates a special indented text version of the instantiated object.
     * </p>
     *
     * @param indentSize
     *            The base indentation to the first line of the text.
     * @param innerIndentInc
     *            The additional indentation increment for the subsequent lines.
     *
     * @return A formatted text string of the class.
     */
    @Override
    public final String asString(int indentSize, int innerIndentInc) {
        return mySequent.asString(indentSize, innerIndentInc) + "\n";
    }

    /**
     * <p>
     * This method overrides the default {@code clone} method implementation.
     * </p>
     *
     * @return A deep copy of the object.
     */
    @Override
    public final VerificationCondition clone() {
        return new VerificationCondition(myLocation.clone(), myName, mySequent.clone(), myHasImpactingReduction,
                myLocationDetailModel.clone());
    }

    /**
     * <p>
     * This method overrides the default {@code equals} method implementation.
     * </p>
     *
     * @param o
     *            Object to be compared.
     *
     * @return {@code true} if all the fields are equal, {@code false} otherwise.
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        VerificationCondition that = (VerificationCondition) o;

        return myHasImpactingReduction == that.myHasImpactingReduction && mySequent.equals(that.mySequent)
                && myLocation.equals(that.myLocation) && myLocationDetailModel.equals(that.myLocationDetailModel)
                && (myName != null ? myName.equals(that.myName) : that.myName == null);
    }

    /**
     * <p>
     * This method indicates if the {@link VerificationCondition} contains a {@link Sequent} that had an impacting
     * reduction or not.
     * </p>
     *
     * @return {@code true} if we have applied some kind of logical reduction to one of the associated {@link Sequent
     *         Sequents}, {@code false} otherwise.
     */
    public final boolean getHasImpactingReductionFlag() {
        return myHasImpactingReduction;
    }

    /**
     * <p>
     * This method returns the location that created this {@code VC}.
     * </p>
     *
     * @return A {@link Location}.
     */
    public final Location getLocation() {
        return myLocation;
    }

    /**
     * <p>
     * This method gets the location details associated with this {@code VC}.
     * </p>
     *
     * @return A {@link LocationDetailModel}.
     */
    public final LocationDetailModel getLocationDetailModel() {
        return myLocationDetailModel;
    }

    /**
     * <p>
     * This method returns the name for this {@code VC}.
     * </p>
     *
     * @return A string.
     */
    public final String getName() {
        return myName;
    }

    /**
     * <p>
     * This method returns the {@code sequent} stored inside this {@code VC}.
     * </p>
     *
     * @return The associated {@link Sequent Sequent}.
     */
    public final Sequent getSequent() {
        return mySequent;
    }

    /**
     * <p>
     * This method overrides the default {@code hashCode} method implementation.
     * </p>
     *
     * @return The hash code associated with the object.
     */
    @Override
    public final int hashCode() {
        int result = mySequent.hashCode();
        result = 31 * result + (myHasImpactingReduction ? 1 : 0);
        result = 31 * result + myLocation.hashCode();
        result = 31 * result + myLocationDetailModel.hashCode();
        result = 31 * result + (myName != null ? myName.hashCode() : 0);
        return result;
    }

    /**
     * <p>
     * This method returns the object in string format.
     * </p>
     *
     * @return Object as a string.
     */
    @Override
    public final String toString() {
        return asString(0, 4);
    }

}