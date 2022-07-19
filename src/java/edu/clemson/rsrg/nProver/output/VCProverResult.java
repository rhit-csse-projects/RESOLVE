/*
 * VCProverResult.java
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
package edu.clemson.rsrg.nProver.output;

import edu.clemson.rsrg.nProver.GeneralPurposeProver;
import edu.clemson.rsrg.vcgeneration.VCGenerator;
import edu.clemson.rsrg.vcgeneration.utilities.VerificationCondition;
import java.util.Objects;

/**
 * <p>
 * This class contains the prover results for a {@code VCs} generated by {@link VCGenerator}.
 * </p>
 *
 * @author Yu-Shan Sun
 * @author Nicodemus Msafiri J. M.
 *
 * @version 1.0
 */
public class VCProverResult {

    // ===========================================================
    // Member Fields
    // ===========================================================

    /**
     * <p>
     * A flag that indicates if this VC was proved or not.
     * </p>
     */
    private final boolean myIsProvedFlag;

    /**
     * <p>
     * The time that the prover spent on this VC.
     * </p>
     */
    private final long myProofTime;

    /**
     * <p>
     * A flag that indicates if this VC timed out during the proof process.
     * </p>
     */
    private final boolean myTimedOutFlag;

    /**
     * <p>
     * A verification condition (VC).
     * </p>
     */
    private final VerificationCondition myVerificationCondition;

    /**
     * <p>
     * A flag that indicates if this VC was skipped in the proof process due to multiple other timed out / unproved VCs.
     * </p>
     */
    private final boolean myWasSkippedFlag;

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * <p>
     * This generates an object to store the prover results produced by the {@link GeneralPurposeProver} for each
     * {@code VC}.
     * </p>
     *
     * @param vc
     *            A {@link VerificationCondition}.
     * @param proofTime
     *            Time (in ms) spent on proving the {@code VC}.
     * @param isProved
     *            A flag that indicates whether the {@code VC} was proved or not.
     * @param timedOut
     *            A flag that indicates whether the {@code VC} timed out during the proof process.
     * @param wasSkipped
     *            A flag that indicates whether the {@code VC} was skipped due to other {@code VCs} timing out or was
     *            unproved.
     */
    public VCProverResult(VerificationCondition vc, long proofTime, boolean isProved, boolean timedOut,
            boolean wasSkipped) {
        myVerificationCondition = vc;
        myProofTime = proofTime;
        myIsProvedFlag = isProved;
        myTimedOutFlag = timedOut;
        myWasSkippedFlag = wasSkipped;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

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
        VCProverResult that = (VCProverResult) o;
        return myIsProvedFlag == that.myIsProvedFlag && myProofTime == that.myProofTime
                && myTimedOutFlag == that.myTimedOutFlag && myWasSkippedFlag == that.myWasSkippedFlag
                && myVerificationCondition.equals(that.myVerificationCondition);
    }

    /**
     * <p>
     * This method returns the amount of time (in ms) spent on proving this {@code VC}.
     * </p>
     *
     * @return Time spent proving this {@code VC}.
     */
    public final long getProofTime() {
        return myProofTime;
    }

    /**
     * <p>
     * This method returns whether the {@code VC} timed out during the proof process.
     * </p>
     *
     * @return {@code true} if the {@code VC} timed out, {@code false} otherwise.
     */
    public final boolean getTimedOutFlag() {
        return myTimedOutFlag;
    }

    /**
     * <p>
     * This method returns the {@code VC} associated with this result.
     * </p>
     *
     * @return A {@link VerificationCondition}.
     */
    public final VerificationCondition getVerificationCondition() {
        return myVerificationCondition;
    }

    /**
     * <p>
     * This method returns whether the {@code VC} was skipped or not.
     * </p>
     *
     * @return {@code true} if the {@code VC} was skipped, {@code false} otherwise.
     */
    public final boolean getWasSkippedFlag() {
        return myWasSkippedFlag;
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
        return Objects.hash(myIsProvedFlag, myProofTime, myTimedOutFlag, myVerificationCondition, myWasSkippedFlag);
    }

    /**
     * <p>
     * This method returns whether the {@code VC} was proved or not.
     * </p>
     *
     * @return {@code true} if the {@code VC} was proved, {@code false} otherwise.
     */
    public final boolean isProved() {
        return myIsProvedFlag;
    }

    /**
     * <p>
     * This method returns the {@code VC} prover result in string format.
     * </p>
     *
     * @return A string.
     */
    @Override
    public final String toString() {
        return "VCProverResult{" + "myIsProvedFlag=" + myIsProvedFlag + ", myProofTime=" + myProofTime
                + ", myTimedOutFlag=" + myTimedOutFlag + ", myVerificationCondition=" + myVerificationCondition
                + ", myWasSkippedFlag=" + myWasSkippedFlag + '}';
    }
}