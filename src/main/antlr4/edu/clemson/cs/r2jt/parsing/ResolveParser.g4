parser grammar ResolveParser;

options {
    tokenVocab=ResolveLexer;
}

module
    :   precisModule
    |   facilityModule
    |   shortFacilityModule
    |   conceptModule
    |   enhancementModule
    |   enhancementImplModule
    |   conceptImplModule
    |   conceptPerformanceModule
    |   enhancementPerformanceModule
    ;

// precis module

precisModule
    :   PRECIS name=IDENTIFIER SEMICOLON
        (usesList)?
        (precisItems)?
        END closename=IDENTIFIER SEMICOLON
    ;

precisItems
    :   (precisItem)+
    ;

precisItem
    :   mathTypeTheoremDecl
    |   mathDefinitionDecl
    |   mathAssertionDecl
    ;

// facility module

facilityModule
    :   FACILITY name=IDENTIFIER SEMICOLON
        (usesList)?
        (facilityItems)?
        END closename=IDENTIFIER SEMICOLON EOF
    ;

facilityItems
    :   (facilityItem)+
    ;

facilityItem
    :   stateVariableDecl
    |   facilityDecl
    |   operationProcedureDecl
    |   mathDefinitionDecl
    |   moduleFacilityInit
    |   moduleFacilityFinal
    ;

// short facility module

shortFacilityModule
    :   facilityDecl EOF
    ;

// concept module

conceptModule
    :   CONCEPT name=IDENTIFIER (moduleParameterList)? SEMICOLON
        (usesList)?
        (requiresClause)?
        (conceptItems)?
        END closename=IDENTIFIER SEMICOLON EOF
    ;

conceptItems
    :   (conceptItem)+
    ;

conceptItem
    :   moduleStateVariableDecl
    |   confirmMathTypeDecl
    |   constraintClause
    |   moduleSpecInit
    |   moduleSpecFinal
    |   operationDecl
    |   typeModelDecl
    |   mathDefinitionDecl
    |   mathDefinesDecl
    ;

// concept impl module

conceptImplModule
    :   REALIZATION name=IDENTIFIER
        (WITH_PROFILE profile=IDENTIFIER)?
        FOR concept=IDENTIFIER
        (ENHANCED BY enhancement=IDENTIFIER)* SEMICOLON
        (usesList)?
        (requiresClause)?
        (implItems)?
        END closename=IDENTIFIER SEMICOLON EOF
    ;

// enhancement module

enhancementModule
    :   ENHANCEMENT name=IDENTIFIER (moduleParameterList)?
        FOR concept=IDENTIFIER SEMICOLON
        (usesList)?
        (requiresClause)?
        (enhancementItems)?
        END closename=IDENTIFIER SEMICOLON EOF
    ;

enhancementItems
    :   (enhancementItem)+
    ;

enhancementItem
    :   moduleStateVariableDecl
    |   operationDecl
    |   typeModelDecl
    |   mathDefinitionDecl
    |   mathDefinesDecl
    ;

// enhancement impl module

enhancementImplModule
    :   REALIZATION name=IDENTIFIER (moduleParameterList)?
        (WITH_PROFILE profile=IDENTIFIER)?
        FOR enhancement=IDENTIFIER
        OF concept=IDENTIFIER
        (ENHANCED BY cEnhancement=IDENTIFIER (moduleParameterList)?
         REALIZED BY cRealization=IDENTIFIER (WITH_PROFILE cProfile=IDENTIFIER)? (moduleParameterList)?)* SEMICOLON
        (usesList)?
        (requiresClause)?
        (implItems)?
        END closename=IDENTIFIER SEMICOLON
    ;

implItems
    :   (implItem)+
    ;

implItem
    :   stateVariableDecl
    |   operationProcedureDecl
    |   facilityDecl
    |   procedureDecl
    |   mathDefinitionDecl
    |   typeRepresentationDecl
    |   conventionClause
    |   correspondenceClause
    |   moduleImplInit
    |   moduleImplFinal
    ;

// concept performance module

conceptPerformanceModule
    :   PROFILE name=IDENTIFIER (moduleParameterList)?
        SHORT_FOR fullName=IDENTIFIER FOR concept=IDENTIFIER SEMICOLON
        (usesList)?
        (requiresClause)?
        END closename=IDENTIFIER SEMICOLON EOF
    ;

conceptPerformanceItems
    :   (conceptPerformanceItem)+
    ;

conceptPerformanceItem
    :   moduleStateVariableDecl
    |   confirmMathTypeDecl
    |   constraintClause
    |   performanceModuleSpecInit
    |   performanceModuleSpecFinal
    |   performanceOperationDecl
    |   performanceTypeModelDecl
    |   mathDefinitionDecl
    |   mathDefinesDecl
    ;

// enhancement performance module

enhancementPerformanceModule
    :   PROFILE name=IDENTIFIER (moduleParameterList)?
        SHORT_FOR fullName=IDENTIFIER FOR enhancement=IDENTIFIER
        WITH_PROFILE conceptProfile=IDENTIFIER SEMICOLON
        (usesList)?
        (requiresClause)?
        END closename=IDENTIFIER SEMICOLON EOF
    ;

enhancementPerformanceItems
    :   (enhancementPerformanceItem)+
    ;

enhancementPerformanceItem
    :   confirmMathTypeDecl
    |   performanceOperationDecl
    |   performanceTypeModelDecl
    |   mathDefinitionDecl
    |   mathDefinesDecl
    ;

// uses, imports

usesList
    :   USES IDENTIFIER (COMMA IDENTIFIER)* SEMICOLON
    ;

// parameter related rules

operationParameterList
    :   LPAREN (parameterDecl (SEMICOLON parameterDecl)*)? RPAREN
    ;

moduleParameterList
    :   LPAREN moduleParameterDecl (SEMICOLON moduleParameterDecl)* RPAREN
    ;

moduleParameterDecl
    :   definitionParameterDecl
    |   typeParameterDecl
    |   constantParameterDecl
    |   operationParameterDecl
    |   conceptImplParameterDecl
    ;

definitionParameterDecl
    :   DEFINITION definitionSignature
    ;

typeParameterDecl
    :   TYPE name=IDENTIFIER
    ;

constantParameterDecl
    :   EVALUATES variableDeclGroup
    ;

operationParameterDecl
    :   operationDecl
    ;

conceptImplParameterDecl
    :   REALIZATION name=IDENTIFIER
        FOR (CONCEPT)? concept=IDENTIFIER
    ;

parameterDecl
    :   parameterMode name=IDENTIFIER COLON type
    ;

parameterMode
    :   ( ALTERS
        | UPDATES
        | CLEARS
        | RESTORES
        | PRESERVES
        | REPLACES
        | EVALUATES )
    ;

// type and record related rules

type
    :   (qualifier=IDENTIFIER QUALIFIER)? name=IDENTIFIER
    ;

record
    :   RECORD (recordVariableDeclGroup)+ END
    ;

recordVariableDeclGroup
    :   IDENTIFIER (COMMA IDENTIFIER)* COLON type SEMICOLON
    ;

typeModelDecl
    :   TYPE FAMILY name=IDENTIFIER IS MODELED BY mathTypeExp SEMICOLON
        EXEMPLAR exemplar=IDENTIFIER SEMICOLON
        (constraintClause)?
        (typeModelInit)?
        (typeModelFinal)?
        END
    ;

typeRepresentationDecl
    :   TYPE name=IDENTIFIER (EQL | IS REPRESENTED BY) (record|type) SEMICOLON
        (conventionClause)?
        (correspondenceClause)?
        (typeRepresentationInit)?
        (typeRepresentationFinal)?
        END
    ;

facilityTypeRepresentationDecl
    :   TYPE name=IDENTIFIER (EQL | IS REPRESENTED BY) (record|type) SEMICOLON
        (conventionClause)?
        (facilityTypeRepresentationInit)?
        (facilityTypeRepresentationFinal)?
        END
    ;

performanceTypeModelDecl
    :   TYPE FAMILY IS MODELED BY mathTypeExp SEMICOLON
        (constraintClause)?
        (performanceTypeModelInit)?
        (performanceTypeModelFinal)?
        END
    ;

// initialization, finalization rules

typeModelInit
    :   INITIALIZATION
        (requiresClause)?
        (ensuresClause)?
    ;

typeModelFinal
    :   FINALIZATION
        (requiresClause)?
        (ensuresClause)?
    ;

typeRepresentationInit
    :   INITIALIZATION
        (affectsClause)*
        (facilityDecl)*
        (variableDecl)*
        (auxVariableDecl)*
        (stmt)*
        END SEMICOLON
    ;

typeRepresentationFinal
    :   FINALIZATION
        (affectsClause)*
        (facilityDecl)*
        (variableDecl)*
        (auxVariableDecl)*
        (stmt)*
        END SEMICOLON
    ;

facilityTypeRepresentationInit
    :   INITIALIZATION
        (affectsClause)*
        (facilityDecl)*
        (variableDecl)*
        (auxVariableDecl)*
        (stmt)*
        END SEMICOLON
    ;

facilityTypeRepresentationFinal
    :   FINALIZATION
        (affectsClause)*
        (requiresClause)?
        (ensuresClause)?
        (facilityDecl)*
        (variableDecl)*
        (auxVariableDecl)*
        (stmt)*
        END SEMICOLON
    ;

performanceTypeModelInit
    :   INITIALIZATION
        (durationClause)?
        (manipulationDispClause)?
    ;

performanceTypeModelFinal
    :   FINALIZATION
        (durationClause)?
        (manipulationDispClause)?
    ;

//We use special rules for facility module init and final to allow requires
//and ensures clauses (which aren't allowed in normal impl modules)...
moduleFacilityInit
    :   FAC_INIT
        (affectsClause)*
        (requiresClause)?
        (ensuresClause)?
        (facilityDecl)*
        (variableDecl)*
        (auxVariableDecl)*
        (stmt)*
    ;

moduleFacilityFinal
    :   FAC_FINAL
        (affectsClause)*
        (requiresClause)?
        (ensuresClause)?
        (facilityDecl)*
        (variableDecl)*
        (auxVariableDecl)*
        (stmt)*
    ;

moduleSpecInit
    :   INITIALIZATION
        (requiresClause)?
        (ensuresClause)?
    ;

moduleSpecFinal
    :   FAC_FINAL
        (requiresClause)?
        (ensuresClause)?
    ;

moduleImplInit
    :   FAC_INIT
        (affectsClause)*
        (facilityDecl)*
        (variableDecl)*
        (auxVariableDecl)*
        (stmt)*
    ;

moduleImplFinal
    :   FAC_FINAL
        (facilityDecl)*
        (variableDecl)*
        (auxVariableDecl)*
        (stmt)*
    ;

performanceModuleSpecInit
    :   PERF_INIT
        (durationClause)?
        (manipulationDispClause)?
    ;

performanceModuleSpecFinal
    :   PERF_FINAL
        (durationClause)?
        (manipulationDispClause)?
    ;

// functions

procedureDecl
    :   (recursive=RECURSIVE)? PROCEDURE name=IDENTIFIER
        operationParameterList (COLON type)? SEMICOLON
        (variableDecl)*
        (stmt)*
        END closename=IDENTIFIER SEMICOLON
    ;

operationProcedureDecl
    :   (recursive=RECURSIVE)? OPERATION
        name=IDENTIFIER operationParameterList SEMICOLON
        (requiresClause)?
        (ensuresClause)?
        PROCEDURE
        (variableDecl)*
        (stmt)*
        END closename=IDENTIFIER SEMICOLON
    ;

operationDecl
    :   OPERATION name=IDENTIFIER operationParameterList (COLON type)? SEMICOLON
            (requiresClause)?
            (ensuresClause)?
    ;

performanceOperationDecl
    :   OPERATION name=IDENTIFIER operationParameterList (COLON type)? SEMICOLON
            (ensuresClause)?
            (durationClause)?
            (manipulationDispClause)?
    ;

// facility and enhancements

facilityDecl
    :   FACILITY name=IDENTIFIER IS concept=IDENTIFIER
        (specArgs=moduleArgumentList)?
        (conceptEnhancementDecl)*
        (externally=EXTERNALLY)? REALIZED
        BY impl=IDENTIFIER (WITH_PROFILE profile=IDENTIFIER)? (implArgs=moduleArgumentList)?
        (enhancementPairDecl)* SEMICOLON
    ;

conceptEnhancementDecl
    :   ENHANCED BY spec=IDENTIFIER (specArgs=moduleArgumentList)?
    ;

enhancementPairDecl
    :   ENHANCED BY spec=IDENTIFIER (specArgs=moduleArgumentList)?
        (externally=EXTERNALLY)? REALIZED BY impl=IDENTIFIER
        (WITH_PROFILE profile=IDENTIFIER)?
        (implArgs=moduleArgumentList)?
    ;

moduleArgumentList
    :   LPAREN moduleArgument (COMMA moduleArgument)* RPAREN
    ;

moduleArgument
    :   progExp
    ;

// variable declarations

mathVariableDeclGroup
    :   IDENTIFIER (COMMA IDENTIFIER)* COLON mathTypeExp
    ;

mathVariableDecl
    :   IDENTIFIER COLON mathTypeExp
    ;

variableDeclGroup
    :   IDENTIFIER (COMMA IDENTIFIER)* COLON type
    ;

variableDecl
    :   VAR variableDeclGroup SEMICOLON
    ;

auxVariableDeclGroup
    :   IDENTIFIER (COMMA IDENTIFIER)* COLON type
    ;

auxVariableDecl
    :   AUX_VAR auxVariableDeclGroup SEMICOLON
    ;

// state variable declaration

moduleStateVariableDecl
    :   VAR mathVariableDeclGroup SEMICOLON
    ;

stateVariableDecl
    :   VAR variableDeclGroup SEMICOLON
    ;

// statements

stmt
    :   assignStmt
    |   swapStmt
    |   callStmt
    |   confirmStmt
    |   ifStmt
    |   whileStmt
    ;

assignStmt
    :   left=progExp ASSIGN_OP right=progExp SEMICOLON
    ;

swapStmt
    :   left=progExp SWAP_OP right=progExp SEMICOLON
    ;

callStmt
    :   progParamExp SEMICOLON
    ;

confirmStmt
    :   CONFIRM mathAssertionExp SEMICOLON
    ;

ifStmt
    :   IF progExp THEN (stmt)*  (elsePart)? END SEMICOLON
    ;

elsePart
    :   ELSE stmt*
    ;

whileStmt
    :   WHILE progExp (changingClause)?
        (maintainingClause)? (decreasingClause)? DO stmt* END SEMICOLON
    ;

// mathematical type theorems

mathTypeTheoremDecl
    :   TYPE THEOREM name=IDENTIFIER COLON
        (FOR ALL mathVariableDeclGroup COMMA)+ mathImpliesExp SEMICOLON
    ;

// mathematical theorems, corollaries, etc

mathAssertionDecl
    :   (AXIOM | COROLLARY | LEMMA | PROPERTY | THEOREM ) name=mathTheoremIdent
        COLON mathAssertionExp SEMICOLON
    ;

mathTheoremIdent
    :   IDENTIFIER
    |   NUMERIC_LITERAL
    ;

// mathematical definitions

mathDefinesDecl
    :   DEFINES definitionSignature SEMICOLON
    ;

mathDefinitionDecl
    :   mathStandardDefinitionDecl
    |   mathInductiveDefinitionDecl
    |   mathCategoricalDecl
    ;

mathCategoricalDecl
    :   CATEGORICAL DEFINITION INTRODUCES categoricalDefinitionSignature
        RELATED BY mathAssertionExp SEMICOLON
    ;

mathInductiveDefinitionDecl
    :   INDUCTIVE DEFINITION inductiveDefinitionSignature
        IS INDUCTIVE_BASE_NUM mathAssertionExp SEMICOLON
        INDUCTIVE_HYP_NUM mathAssertionExp SEMICOLON
    ;

mathStandardDefinitionDecl
    :   DEFINITION definitionSignature (IS mathAssertionExp)? SEMICOLON
    ;

categoricalDefinitionSignature
    :   definitionSignature (COMMA definitionSignature)*
    ;

inductiveDefinitionSignature
    :   inductivePrefixSignature
    |   inductiveInfixSignature
    ;

inductivePrefixSignature
    :   ON mathVariableDecl OF prefixOp
        LPAREN (inductiveParameterList COMMA)? IDENTIFIER RPAREN COLON mathTypeExp
    ;

inductiveInfixSignature
    :   ON mathVariableDecl OF LPAREN mathVariableDecl RPAREN infixOp
        LPAREN IDENTIFIER RPAREN COLON mathTypeExp
    ;

inductiveParameterList
    :   mathVariableDeclGroup (COMMA mathVariableDeclGroup)*
    ;

definitionSignature
    :   standardInfixSignature
    |   standardOutfixSignature
    |   standardPrefixSignature
    ;

standardInfixSignature
    :   LPAREN mathVariableDecl RPAREN
        infixOp
        LPAREN mathVariableDecl RPAREN COLON mathTypeExp
    ;

standardOutfixSignature
    :   ( lOp=BAR LPAREN mathVariableDecl RPAREN rOp=BAR
    |     lOp=DBL_BAR LPAREN mathVariableDecl RPAREN rOp=DBL_BAR
    |     lOp=LT LPAREN mathVariableDecl RPAREN rOp=GT) COLON mathTypeExp
    ;

standardPrefixSignature
    :   prefixOp (definitionParameterList)? COLON mathTypeExp
    ;

prefixOp
    :   infixOp
    |   INTEGER_LITERAL
    ;

infixOp
    :   (IMPLIES | PLUS | CONCAT | MINUS | DIVIDE | MULTIPLY | RANGE | AND | OR)
    |   (UNION | INTERSECT | IN | NOT_IN | GT | LT | GT_EQL | LT_EQL)
    |   IDENTIFIER
    ;

definitionParameterList
    :   LPAREN mathVariableDeclGroup (COMMA mathVariableDeclGroup)* RPAREN
    ;

// mathematical clauses

affectsClause
    :   parameterMode IDENTIFIER (COMMA IDENTIFIER)*
    ;

requiresClause
    :   REQUIRES mathAssertionExp SEMICOLON
    ;

ensuresClause
    :   ENSURES mathAssertionExp SEMICOLON
    ;

constraintClause
    :   CONSTRAINT mathAssertionExp SEMICOLON
    ;

changingClause
    :   CHANGING progVariableExp (COMMA progVariableExp)*
    ;

maintainingClause
    :   MAINTAINING mathAssertionExp SEMICOLON
    ;

decreasingClause
    :   DECREASING mathAssertionExp SEMICOLON
    ;

whereClause
    :   WHERE mathAssertionExp
    ;

correspondenceClause
    :   CORR mathAssertionExp SEMICOLON
    ;

conventionClause
    :   CONVENTION mathAssertionExp SEMICOLON
    ;

durationClause
    :   DURATION mathAssertionExp SEMICOLON
    ;

manipulationDispClause
    :   MAINP_DISP mathAssertionExp SEMICOLON
    ;

// mathematical type declarations

confirmMathTypeDecl
    :   CONFIRM MATH TYPE mathVariableDecl SEMICOLON
    ;

// mathematical expressions

mathTypeExp
    :   mathExp
    ;

mathAssertionExp
    :   mathExp
    |   mathQuantifiedExp
    ;

mathQuantifiedExp
    :   FOR ALL mathVariableDeclGroup (whereClause)? COMMA
         mathAssertionExp
    ;

mathExp
    :   mathPrimaryExp                                  #mathPrimeExp
    |   op=(PLUS|MINUS|TILDE|NOT) mathExp               #mathUnaryExp
    |   mathExp op=(MULTIPLY|DIVIDE|TILDE) mathExp      #mathInfixExp
    |   mathExp op=(PLUS|MINUS) mathExp                 #mathInfixExp
    |   mathExp op=(RANGE|FUNCARROW) mathExp            #mathInfixExp
    |   mathExp op=(CONCAT|UNION|INTERSECT) mathExp     #mathInfixExp
    |   mathExp op=(IN|NOT_IN) mathExp                  #mathInfixExp
    |   mathExp op=(LT_EQL|GT_EQL|GT|LT) mathExp        #mathInfixExp
    |   mathExp op=(EQL|NOT_EQL) mathExp                #mathInfixExp
    |   mathImpliesExp                                  #mathInfixExp
    |   mathExp op=(AND|OR) mathExp                     #mathInfixExp
    |   mathExp (COLON) mathExp                         #mathTypeAssertExp
    |   LPAREN mathAssertionExp RPAREN                  #mathNestedExp
    ;

mathImpliesExp
    :   mathExp op=IMPLIES mathExp
    ;

mathPrimaryExp
    :   mathLiteralExp
    |   mathDotExp
    |   mathFunctionApplicationExp
    |   mathOutfixExp
    |   mathSetExp
    |   mathTupleExp
    |   mathLambdaExp
    ;

mathLiteralExp
    :   BOOLEAN_LITERAL      #mathBooleanExp
    |   INTEGER_LITERAL      #mathIntegerExp
    ;

mathDotExp
    :   mathFunctionApplicationExp (DOT mathFunctionApplicationExp)+
    ;

mathFunctionApplicationExp
    :   HASH mathCleanFunctionExp
    |   mathCleanFunctionExp
    ;

mathCleanFunctionExp
    :   name=IDENTIFIER LPAREN mathExp (COMMA mathExp)* RPAREN  #mathFunctionExp
    |   (qualifier=IDENTIFIER QUALIFIER)? name=IDENTIFIER       #mathVariableExp
    |   (PLUS|MINUS|MULTIPLY|DIVIDE)                            #mathOpExp
    ;

mathOutfixExp
    :   lop=LT mathExp rop=GT
    |   lop=BAR mathExp rop=BAR
    |   lop=DBL_BAR mathExp rop=DBL_BAR
    ;

mathSetExp
    :   LBRACE mathVariableDecl BAR mathAssertionExp RBRACE #mathSetBuilderExp//Todo
    |   LBRACE (mathExp (COMMA mathExp)*)? RBRACE           #mathSetCollectionExp
    ;

mathTupleExp
    :   LPAREN mathExp (COMMA mathExp)+ RPAREN
    ;

//NOTE: Allows only very rudimentary lambda expressions.

mathLambdaExp
    :   LAMBDA LPAREN mathVariableDeclGroup (COMMA mathVariableDeclGroup)* RPAREN
        DOT LPAREN mathAssertionExp RPAREN
    ;

// program expressions

progExp
    :   op=(NOT|MINUS) progExp                      #progApplicationExp
    |   progExp op=(MULTIPLY|DIVIDE) progExp        #progApplicationExp
    |   progExp op=(PLUS|MINUS) progExp             #progApplicationExp
    |   progExp op=(LT_EQL|GT_EQL|GT|LT) progExp    #progApplicationExp
    |   progExp op=(EQL|NOT_EQL) progExp            #progApplicationExp
    |   LPAREN progExp RPAREN                       #progNestedExp
    |   progPrimary                                 #progPrimaryExp
    ;

progPrimary
    :   progLiteralExp
    |   progVariableExp
    |   progParamExp
    ;

//This intermediate rule is really only needed to help make
//the 'changingClause' rule a little more strict about what it accepts.
//A root VariableExp class is no longer reflected in the ast.
progVariableExp
    :   progDotExp
    |   progNamedExp
    ;

progDotExp
    :   progNamedExp (DOT progNamedExp)+
    ;

progParamExp
    :   (qualifier=IDENTIFIER QUALIFIER)? name=IDENTIFIER
        LPAREN (progExp (COMMA progExp)*)? RPAREN
    ;

progNamedExp
    :   (qualifier=IDENTIFIER QUALIFIER)? name=IDENTIFIER
    ;

progLiteralExp
    :   INTEGER_LITERAL      #progIntegerExp
    |   CHARACTER_LITERAL    #progCharacterExp
    |   STRING_LITERAL       #progStringExp
    ;