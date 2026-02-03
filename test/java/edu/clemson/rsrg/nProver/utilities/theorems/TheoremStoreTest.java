/*
 * TheoremStoreTest.java
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
import edu.clemson.rsrg.typeandpopulate.entry.TheoremEntry;
import edu.clemson.rsrg.typeandpopulate.query.EntryTypeQuery;
import edu.clemson.rsrg.typeandpopulate.symboltables.ModuleScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TheoremStoreTest {

    @Mock
    private ModuleScope mockModuleScope;

    @Mock
    private TheoremEntry mockTheorem1;

    @Mock
    private TheoremEntry mockTheorem2;

    @Mock
    private Exp mockOp1;

    @Mock
    private Exp mockOp2;

    @Mock
    private Exp mockExp;

    private String opString1 = "op1";

    private String opString2 = "op2";

    private TheoremStore theoremStore;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock operators
        when(mockOp1.toString()).thenReturn(opString1);
        when(mockOp2.toString()).thenReturn(opString2);
        when(mockOp1.getOperatorStrings()).thenReturn(Collections.emptySet());
        when(mockOp2.getOperatorStrings()).thenReturn(Collections.emptySet());

        // Mock theorems
        when(mockTheorem1.getName()).thenReturn("theorem1");
        when(mockTheorem1.getOperators()).thenReturn(new HashSet<>(Collections.singletonList(mockOp1)));

        when(mockTheorem2.getName()).thenReturn("theorem2");
        when(mockTheorem2.getOperators()).thenReturn(new HashSet<>(Arrays.asList(mockOp1, mockOp2)));

        // Mock module scope
        List<TheoremEntry> theorems = Arrays.asList(mockTheorem1, mockTheorem2);
        when(mockModuleScope.query(any(EntryTypeQuery.class))).thenReturn(theorems);

        theoremStore = new TheoremStore(mockModuleScope);
    }

    @Test
    void getRelevantTheorems_shouldReturnAllMatchingTheorems() {
        // Arrange
        when(mockExp.toString()).thenReturn("op1 and op2");
        when(mockExp.getOperatorStrings()).thenReturn(Set.of(opString1, opString2));

        List<Exp> expressions = Collections.singletonList(mockExp);

        // Act
        Set<TheoremEntry> relevantTheorems = theoremStore.getRelevantTheorems(expressions, Collections.emptyList());

        // Assert
        assertEquals(2, relevantTheorems.size());
        assertTrue(relevantTheorems.contains(mockTheorem1));
        assertTrue(relevantTheorems.contains(mockTheorem2));
    }

    @Test
    void getRelevantTheorems_shouldReturnEmptySetWhenNoOperatorsMatch() {
        // Arrange
        Exp mockOtherOp = mock(Exp.class);
        String otherOpString = "op3";
        when(mockOtherOp.toString()).thenReturn(otherOpString);
        when(mockOtherOp.getOperatorStrings()).thenReturn(Collections.emptySet());
        when(mockExp.toString()).thenReturn(otherOpString);
        when(mockExp.getOperatorStrings()).thenReturn(Set.of(otherOpString));
        List<Exp> expressions = Collections.singletonList(mockExp);

        // Act
        Set<TheoremEntry> relevantTheorems = theoremStore.getRelevantTheorems(expressions, Collections.emptyList());

        // Assert
        assertTrue(relevantTheorems.isEmpty());
    }

    @Test
    void getRelevantTheorems_shouldReturnPartiallyMatchingTheorems() {
        // Arrange
        when(mockExp.toString()).thenReturn(opString1);
        when(mockExp.getOperatorStrings()).thenReturn(Set.of(opString1));
        List<Exp> expressions = Collections.singletonList(mockExp);

        // Act
        Set<TheoremEntry> relevantTheorems = theoremStore.getRelevantTheorems(expressions, Collections.emptyList());

        // Assert
        assertEquals(1, relevantTheorems.size());
        assertTrue(relevantTheorems.contains(mockTheorem1));
        assertFalse(relevantTheorems.contains(mockTheorem2)); // mockTheorem2 needs op1 and op2
    }

    @Test
    void getRelevantTheorems_shouldReturnEmptySetForEmptyExpressions() {
        // Arrange
        List<Exp> expressions = Collections.emptyList();

        // Act
        Set<TheoremEntry> relevantTheorems = theoremStore.getRelevantTheorems(expressions, Collections.emptyList());

        // Assert
        assertTrue(relevantTheorems.isEmpty());
    }

    @Test
    void getRelevantTheorems_shouldBeIdempotent() {
        // Arrange
        when(mockExp.toString()).thenReturn("op1 and op2");
        when(mockExp.getOperatorStrings()).thenReturn(Set.of(opString1, opString2));

        List<Exp> expressions = Collections.singletonList(mockExp);

        // Act
        Set<TheoremEntry> result1 = theoremStore.getRelevantTheorems(expressions, Collections.emptyList());
        Set<TheoremEntry> result2 = theoremStore.getRelevantTheorems(expressions, Collections.emptyList());

        // Assert
        assertEquals(result1, result2);
        assertEquals(2, result2.size());
    }
}
