grammar Jass;

@header {
    package de.peeeq.wurstscript.antlr;
}

compilationUnit : NL? decls+=jassTopLevelDeclaration* EOF;

jassTopLevelDeclaration:
     jassGlobalsBlock
   | jassFuncDef
   | jassTypeDecl
   | jassNativeDecl
;

jassGlobalsBlock:
    'globals' NL
        vars+=jassGlobalDecl*
    'endglobals' NL
;

jassGlobalDecl:
    constant='constant'? jassTypeExpr name=ID ('=' initial=jassExpr)? NL
;

jassFuncDef:
    constant='constant'? 'function' jassFuncSignature NL
        (jassLocals+=jassLocal)*
        jassStatements
    'endfunction' NL
;

jassLocal:
    'local' jassTypeExpr name=ID ('=' initial=jassExpr)? NL;

jassStatements:
    stmts+=jassStatement*;

jassStatement:
      jassStatementIf
    | jassStatementLoop
    | jassStatementExithwhen
    | jassStatementReturn
    | jassStatementSet
    | jassStatementCall
    ;

jassStatementIf:
    'if' cond=jassExpr 'then' NL
        thenStatements=jassStatements
    jassElseIfs
;

jassElseIfs:
      'elseif' cond=jassExpr 'then' NL
            thenStatements=jassStatements
        jassElseIfs
    | 'else' NL
        elseStmts=jassStatements
      'endif' NL
    | 'endif' NL
;

jassStatementLoop:
    'loop' NL
        jassStatements
    'endloop' NL
;

jassStatementExithwhen: 'exitwhen' cond=jassExpr NL;

jassStatementReturn: 'return' jassExpr NL;

jassStatementSet: 'set' left=exprVarAccess '=' right=jassExpr NL;

jassStatementCall:'call' exprFunctionCall NL;

jassNativeDecl: constant='constant'? 'native' jassFuncSignature NL;

jassFuncSignature:
    name=ID 'takes' ('nothing'|args+=formalParam (',' args+=formalParam)*) 'returns' ('nothing'|returnType=jassTypeExpr)
;

jassTypeDecl: 'type' name=ID 'extends' extended=jassTypeExpr NL;

formalParam: jassTypeExpr name=ID;

jassTypeExpr:
    | typeName=ID
    | jassTypeExpr 'array'
;

exprVarAccess: varname=ID indexes?;

indexes: '[' jassExpr ']';

jassExpr:
      jassExprPrimary
    | left=jassExpr op=('*'|'/'|'%') right=jassExpr
    | op='-' right=jassExpr // TODO move unary minus one up to be compatible with Java etc.
                      // currently it is here to be backwards compatible with the old wurst parser
    | left=jassExpr op=('+'|'-') right=jassExpr
    | left=jassExpr op=('<='|'<'|'>'|'>=') right=jassExpr
    | left=jassExpr op=('=='|'!=') right=jassExpr
    | op='not' right=jassExpr
    | left=jassExpr op='or' right=jassExpr
    | left=jassExpr op='and' right=jassExpr
    |
    ;

jassExprPrimary:
	    exprFunctionCall
      | varname=ID indexes?
      | atom=(INT
      | REAL
	  | STRING
	  | 'null'
	  | 'true'
	  | 'false')
	  | exprFuncRef
      | '(' jassExpr ')'
	;


exprFuncRef: 'function' (scopeName=ID '.')? funcName=ID;

argumentList: '(' exprList ')';

exprFunctionCall: funcName=ID jassTypeExpr argumentList;
	  
exprList : exprs+=jassExpr (',' exprs+=jassExpr)*;

// Lexer:
RETURN: 'return';
IF: 'if';
ELSE: 'else';
NULL: 'null';
FUNCTION: 'function';
RETURNS: 'returns';
NATIVE: 'native';
EXTENDS: 'extends';
ARRAY: 'array';
AND: 'and';
OR: 'or';
NOT: 'not';
TYPE: 'type';
CONSTANT: 'constant';
ENDFUNCTION: 'endfunction';
NOTHING: 'nothing';
TRUE: 'true';
FALSE: 'false';
THEN: 'then';
ENDIF: 'endif';
LOOP: 'loop';
EXITHWHEN: 'exithwhen';
ENDLOOP: 'endloop';
TAKES: 'takes';
SET: 'set';
CALL: 'call';

COMMA: ',';
PLUS: '+';
MINUS: '-';
MULT: '*';
DIV_REAL: '/';
MOD_REAL: '%';
PAREN_LEFT: '(';
PAREN_RIGHT: ')';
BRACKET_LEFT: '[';
BRACKET_RIGHT: ']';
EQ: '=';
EQEQ: '==';
NOT_EQ: '!=';
LESS: '<';
LESS_EQ: '<=';
GREATER: '>';
GREATER_EQ: '>=';

STARTBLOCK:[()];
ENDBLOCK:[()];
INVALID:[()];

JASS_GLOBALS: 'globals';
JASS_LOCAL: 'local';
JASS_ENDGLOBALS: 'endglobals';
JASS_ELSEIF: 'elseif';

NL: [\r\n]+;
ID: [a-zA-Z_][a-zA-Z0-9_]* ;

STRING: '"' ( EscapeSequence | ~('\\'|'"'|'\r'|'\n') | NL )* '"';
REAL: [0-9]+ '.' [0-9]* | '.'[0-9]+;

fragment HexInt: '$'[0-9a-fA-F]+ | '0'[xX][0-9a-fA-F]+;

fragment CharIntPart: ('\\' [btrnf"\\]) | ~[\\'];
fragment CharInt: '\'' CharIntPart+ '\'' | '\'' [\\'] '\'';

INT: [0-9]+ | HexInt | CharInt;

fragment EscapeSequence: '\\' [abfnrtvz"'\\];

DEBUG: 'debug' -> skip;
WS : [ \t]+ -> skip ;
ML_COMMENT: '/*' .*? '*/' -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;
