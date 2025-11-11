/*
 * TheoremSelector.java
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
package edu.clemson.rsrg.nProver.utilities.theorems;

import edu.clemson.rsrg.absyn.expressions.Exp;
import edu.clemson.rsrg.nProver.registry.CongruenceClassRegistry;
import edu.clemson.rsrg.typeandpopulate.entry.TheoremEntry;
import edu.clemson.rsrg.nProver.registry.CongruenceCluster;
import edu.clemson.rsrg.vcgeneration.sequents.Sequent;


import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class TheoremSelector {
    private TheoremManager theorems;
    private CongruenceClassRegistry registry;
    //private Sequent sequent;
    public TheoremSelector(TheoremManager tm, CongruenceClassRegistry registry){
        this.theorems = tm;
        this.registry = registry;

    }

    public List<TheoremEntry> selectTheorems(List<Exp> expressions, List<String> operators) {


        List<Exp> filteredList = expressions.stream()
                .filter(exp -> operators.contains(this.getAllExpStrings(exp)))
                .collect(Collectors.toList());

        CongruenceCluster[] clusters = this.registry.getClusterArray();

//        // Iterate through all clusters
//        for (CongruenceCluster cluster : clusters) {
//            if (cluster != null) {
//                // Get the tree node label (operator) from each cluster
//                Integer operatorLabel = cluster.getTreeNodeLabel();
//
//                // You'll need to determine how to create an Exp from the string label
//                //Ok
//
//
//
//
//
//            }
//        }
        List<TheoremEntry> relevantTheorems = theorems.getRelevantTheorems(filteredList);
        return relevantTheorems;

    }

    //copied from theorem store
    private Set<String> getAllExpStrings(Exp exp) {
        Set<String> expStrings = new HashSet<>();
        expStrings.add(exp.toString());
        for (Exp subExp : exp.getSubExpressions()) {
            expStrings.addAll(getAllExpStrings(subExp));
        }
        return expStrings;
    }

}
