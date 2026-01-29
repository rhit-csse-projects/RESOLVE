/*
 * ElaborationRulesTest.java
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
import edu.clemson.rsrg.absyn.expressions.mathexpr.FunctionExp;
import edu.clemson.rsrg.absyn.expressions.mathexpr.InfixExp;
import edu.clemson.rsrg.absyn.expressions.mathexpr.VarExp;
import edu.clemson.rsrg.parsing.data.PosSymbol;
import edu.clemson.rsrg.typeandpopulate.entry.TheoremEntry;
import edu.clemson.rsrg.typeandpopulate.utilities.ModuleIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ElaborationRulesTest {

    @Mock
    private TheoremEntry mockTheorem;

    @Mock
    private Exp mockAssertion;

    @Mock
    private ModuleIdentifier mockModuleIdentifier;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Set up the mock module identifier to return a string
        when(mockModuleIdentifier.toString()).thenReturn("TestModule");
        // Set up the mock theorem to return the mock module identifier
        when(mockTheorem.getSourceModuleIdentifier()).thenReturn(mockModuleIdentifier);
        // Set up a default name for the theorem
        when(mockTheorem.getName()).thenReturn("TestTheorem");
    }

    private VarExp createVarExp(String name) {
        return new VarExp(null, null, new PosSymbol(null, name));
    }

    private FunctionExp createFunctionExp(String name, List<Exp> args) {
        return new FunctionExp(null, createVarExp(name), null, args);
    }

    private InfixExp createInfixExp(Exp left, String op, Exp right) {
        return new InfixExp(null, left, null, new PosSymbol(null, op), right);
    }

    @Test
    public void testIsDeterministic_SimpleFunction() {
        // Rule: f(x) -> y
        // Precursors should be [f(x)], Resultant y.
        // But isDeterministic takes a list that includes the resultant (which it removes).

        VarExp x = createVarExp("x");
        VarExp y = createVarExp("y");

        FunctionExp f_x = createFunctionExp("f", Collections.singletonList(x));

        ElaborationRules rules = new ElaborationRules(Collections.emptyList());

        // Case 1: Check if f(x) determines y.
        // List passed should contain f(x) and y.
        List<Exp> theoremList1 = new ArrayList<>();
        theoremList1.add(f_x);
        theoremList1.add(y);

        // f(x) has variable x. y has no sub-variables (VarExp subExpressions is empty).
        // precursors after remove(y): [f(x)]. vars: {x}.
        // y sub-vars: {}. {} is subset of {x}. -> True.
        assertTrue(rules.isDeterministic(theoremList1, y));

        // Case 2: Check if y determines f(x).
        List<Exp> theoremList2 = new ArrayList<>();
        theoremList2.add(f_x);
        theoremList2.add(y);

        // precursors after remove(f(x)): [y]. vars: {}.
        // f(x) sub-vars: {x}. {x} is NOT subset of {}. -> False.
        assertFalse(rules.isDeterministic(theoremList2, f_x));
    }

    @Test
    public void testCreateElaborationRules_Equality() {
        // Theorem: f(x) = y
        // We assume the theorem assertion has subexpressions [f(x), y].

        VarExp x = createVarExp("x");
        VarExp y = createVarExp("y");
        FunctionExp f_x = createFunctionExp("f", Collections.singletonList(x));

        // Mock assertion to return [f(x), y]
        // We use the same objects so reference equality works in remove()
        List<Exp> subExps = Arrays.asList(f_x, y);
        when(mockAssertion.getSubExpressions()).thenAnswer(i -> new ArrayList<>(subExps));
        when(mockTheorem.getAssertion()).thenReturn(mockAssertion);

        ElaborationRules rules = new ElaborationRules(Collections.singletonList(mockTheorem));
        List<ElaborationRule> generatedRules = rules.getMyElaborationRules();

        // Expecting 1 rule: f(x) -> y (because y has no sub-vars, so it is determined by f(x))
        // y -> f(x) is NOT generated because f(x) has x, and y has no vars.
        assertEquals(1, generatedRules.size());
        ElaborationRule rule = generatedRules.get(0);

        // Check resultant
        assertEquals(mockAssertion, rule.getResultantClause());

        // Check precursors
        List<Exp> precursors = rule.getPrecursorClauses();
        assertEquals(1, precursors.size());
        assertTrue(precursors.contains(f_x));

        // Check source theorem info
        assertEquals("TestTheorem", rule.getSourceTheoremName());
        assertEquals("TestModule", rule.getSourceModuleName());
    }

    @Test
    public void testCreateElaborationRules_SingleClause() {
        // Theorem: P(x) and Q(x)
        // Treated as single clause containing sub-clauses.
        // This triggers the if (myTheoremExpressions.size() == 1) block.

        VarExp x = createVarExp("x");
        FunctionExp p_x = createFunctionExp("P", Collections.singletonList(x));
        FunctionExp q_x = createFunctionExp("Q", Collections.singletonList(x));

        InfixExp container = createInfixExp(p_x, "and", q_x);

        // Assertion returns [container]
        when(mockAssertion.getSubExpressions()).thenAnswer(i -> new ArrayList<>(Collections.singletonList(container)));
        when(mockTheorem.getAssertion()).thenReturn(mockAssertion);

        ElaborationRules rules = new ElaborationRules(Collections.singletonList(mockTheorem));
        List<ElaborationRule> generatedRules = rules.getMyElaborationRules();

        // P(x) determines Q(x) (x in x) -> Rule 1
        // Q(x) determines P(x) (x in x) -> Rule 2
        assertEquals(2, generatedRules.size());

        // Verify rules
        // In the single clause case, the resultant is always the whole theorem assertion
        boolean foundPtoContainer = false;
        boolean foundQtoContainer = false;

        for (ElaborationRule rule : generatedRules) {
            assertEquals(mockAssertion, rule.getResultantClause());

            // Verify source theorem info
            assertEquals("TestTheorem", rule.getSourceTheoremName());
            assertEquals("TestModule", rule.getSourceModuleName());

            List<Exp> precursors = rule.getPrecursorClauses();
            assertEquals(1, precursors.size());

            Exp precursor = precursors.get(0);
            if (precursor.equals(p_x)) {
                foundPtoContainer = true;
            } else if (precursor.equals(q_x)) {
                foundQtoContainer = true;
            }
        }

        assertTrue(foundPtoContainer, "Should have rule with P(x) as precursor");
        assertTrue(foundQtoContainer, "Should have rule with Q(x) as precursor");
    }

    @Test
    public void testCollectVariables() {
        VarExp x = createVarExp("x");
        List<Exp> argsList = new ArrayList<>();
        argsList.add(x);
        FunctionExp f_x = createFunctionExp("f", argsList);

        List<Exp> precursors = new ArrayList<>();
        precursors.add(f_x);

        Set<Exp> vars = ElaborationRules.collectVariables(precursors);
        assertTrue(vars.contains(x));
        assertEquals(1, vars.size());
    }

    @Test
    public void testCollectVariables_Nested() {
        // Verify that collectVariables is shallow (only checks direct subexpressions)
        VarExp x = createVarExp("x");
        FunctionExp g_x = createFunctionExp("g", Collections.singletonList(x));
        FunctionExp f_g_x = createFunctionExp("f", Collections.singletonList(g_x));

        ElaborationRules rules = new ElaborationRules(Collections.emptyList());
        List<Exp> precursors = new ArrayList<>();
        precursors.add(f_g_x);

        Set<Exp> vars = rules.collectVariables(precursors);
        // Based on code analysis, this should be empty because g(x) is not VarExp
        // and collectVariables does not recurse.
        assertTrue(vars.isEmpty());
    }

    @Test
    public void testVarExpEquality() {
        VarExp x1 = createVarExp("x");
        VarExp x2 = createVarExp("x");
        assertEquals(x1, x2);
        assertEquals(x1.hashCode(), x2.hashCode());
    }
}