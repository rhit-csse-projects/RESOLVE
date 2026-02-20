/*
 * ClusterExp.java
 * ---------------------------------
 * Copyright (c) 2024
 * RESOLVE Software Research Group
 * School of Computing
 * Clemson University
 * All rights reserved.
 * ---------------------------------
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package edu.clemson.rsrg.absyn.expressions.mathexpr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.clemson.rsrg.absyn.expressions.Exp;
import edu.clemson.rsrg.nProver.registry.CongruenceClassRegistry;

/**
 * This class is a placeholder for clusters in the {@link CongruenceClassRegistry}. It should only be used during the
 * matching & elaboration process.
 */
public class ClusterExp extends MathExp {
    private int clusterId;

    public ClusterExp(int clusterId) {
        super(null);
        this.clusterId = clusterId;
    }

    @Override
    public boolean containsExp(Exp exp) {
        return equals(exp);
    }

    @Override
    public List<Exp> getSubExpressions() {
        return new ArrayList<>();
    }

    @Override
    protected Exp substituteChildren(Map<Exp, Exp> substitutions) {
        return this;
    }

    @Override
    public String asString(int indentSize, int innerIndentInc) {
        return "ClusterID: " + clusterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof ClusterExp) {
            return this.clusterId == ((ClusterExp) o).clusterId;
        }
        return false;
    }

    public int getClusterId() {
        return clusterId;
    }
}
