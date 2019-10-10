grammar Wurst;

@header {
    package de.peeeq.wurstscript.antlr;
}

compilationUnit : NL* decls+=topLevelDeclaration* EOF;

topLevelDeclaration:
					 wpackage
				   | jassTopLevelDeclaration
				   ;


jassTopLevelDeclaration:
						 jassGlobalsBlock
					   | jassFuncDef
					   | jassTypeDecl
					   | jassNativeDecl
					;


jassGlobalsBlock:
					'globals' NL vars+=jassGlobalDecl* 'endglobals' NL
				;

jassGlobalDecl:
				  constant='constant'? typeExpr name=ID ('=' initial=expr)? NL
			  ;

jassFuncDef:
			   constant='constant'? 'function' jassFuncSignature NL
			   (jassLocals+=jassLocal)*
			   jassStatements
			   'endfunction' NL
		   ;

jassLocal: 'local' typeExpr name=ID ('=' initial=expr)? NL;

jassStatements: stmts+=jassStatement*;

jassStatement:
				 jassStatementIf
			 | jassStatementLoop
			 | jassStatementExithwhen
			 | jassStatementReturn
			 | jassStatementSet
			 | jassStatementCall
			 ;

jassStatementIf:
				   'if' cond=expr 'then' NL thenStatements=jassStatements jassElseIfs
;

jassElseIfs:
			   'elseif' cond=expr 'then' NL thenStatements=jassStatements jassElseIfs
		   | 'else' NL elseStmts=jassStatements 'endif' NL
		   | 'endif' NL
		   ;

jassStatementLoop:
					 'loop' NL jassStatements 'endloop' NL
				;
jassStatementExithwhen:
						  'exitwhen' cond=expr NL
;
jassStatementReturn:
					'return' expr NL
;
jassStatementSet:
					'set' left=exprAssignable '=' right=expr NL
;
jassStatementCall:
					 'call' exprFunctionCall NL
				 ;


jassNativeDecl:
				  constant='constant'? 'native' jassFuncSignature NL
			  ;

jassFuncSignature:
					 name=ID 'takes' ('nothing' | args+=formalParameter (',' args+=formalParameter)*)
					 'returns' ('nothing'|returnType=typeExpr)
				 ;

jassTypeDecl: 'type' name=ID 'extends' extended=typeExpr NL;


wpackage: 'package' name=ID NL 
	(
	imports+=wImport* entities+=entity*
	| STARTBLOCK imports+=wImport* entities+=entity* ENDBLOCK
	) 'endpackage' NL
	;

wImport: 
    'import' isPublic='public'? isInitLater='initlater'? importedPackage=ID NL 
    ;



entity: 
        nativeType
      | funcDef
      | varDef
      | initBlock
      | nativeDef
      | classDef
      | enumDef
      | moduleDef
      | interfaceDef
      | tupleDef
      | extensionFuncDef
;

interfaceDef:
                modifiersWithDoc 'interface' name=ID typeParams 
                ('extends' extended+=typeExpr (',' extended+=typeExpr)*)? 
                NL (STARTBLOCK
                    classSlots
                ENDBLOCK)?
            ;
             
 
classDef:
            modifiersWithDoc 'class' name=ID typeParams 
            ('extends' extended=typeExpr)? 
            ('implements' implemented+=typeExpr (',' implemented+=typeExpr)*)?
            NL (STARTBLOCK
                classSlots
            ENDBLOCK)?
        ;

typeclassDef:
    modifiersWithDoc 'typeclass' name=ID typeParams
    ('extends' implemented+=typeExpr (',' implemented+=typeExpr)*)?
    NL (STARTBLOCK
       classSlots
    ENDBLOCK)?
    ;


enumDef: modifiersWithDoc 'enum' name=ID NL (STARTBLOCK 
      (enumMembers+=ID NL)*
ENDBLOCK)?;

moduleDef:
            modifiersWithDoc 'module' name=ID typeParams
            NL STARTBLOCK
                classSlots
            ENDBLOCK
         ;

classSlots: slots+=classSlot*;

classSlot:
           constructorDef
         | moduleUse
         | ondestroyDef
         | varDef
         | funcDef
         | classDef
         ;

constructorDef:
                 modifiersWithDoc 'construct' formalParameters NL (STARTBLOCK
                superCall?
                stmts+=statement*
                 ENDBLOCK)?
             ;

superCall:
   superKeyword='super' '(' superArgs=exprList ')' NL
   ;



moduleUse: 
         modifiersWithDoc 'use' moduleName=ID typeArgs NL
         ;

ondestroyDef:
            'ondestroy' NL statementsBlock
            ;


funcDef:
       modifiersWithDoc 'function' funcSignature NL statementsBlock
       ;






modifiersWithDoc:
	(hotdocComment NL)?
	(modifiers+=modifier NL?)*
;

modifier:
			modType=(
		  'public' 
		| 'private'
		| 'protected'
		| 'publicread'
		| 'static'
		| 'override'
		| 'abstract' 
		| 'constant'
			)
		| annotation
		;

annotation: name=ANNOTATION argumentList?;

hotdocComment: HOTDOC_COMMENT;

funcSignature:
				 name=ID typeParams formalParameters ('returns' returnType=typeExpr)?
			 ;

formalParameters: '(' (params+=formalParameter (',' params+=formalParameter)*)? ')';

formalParameter:
				 vararg=VARARG? typeExpr name=ID
			   ;

typeExpr:
		  thistype='thistype'
		| typeName=ID typeArgs
		| typeExpr 'array' ('[' arraySize=expr ']')?
		| typeExpr '.' typeName=ID typeArgs
		| typeExpr '.' thistype='thistype'
		;

varDef:
		  modifiersWithDoc 
		  ('var'|constant='constant' varType=typeExpr?|constant='let'|varType=typeExpr)
		  name=ID ('=' variableInit)? NL
	  ;		  

variableInit: (arrayInit | initial=expr);


arrayInit: '[' exprList ']';


statements:
			statement*
		  ;

statementsBlock:
			   (STARTBLOCK statement* ENDBLOCK)?;


statement:
		   localVarDef (externalLambda|NL)
		 | stmtSet (externalLambda|NL)
		 | stmtReturn (externalLambda|NL)
		 | stmtBreak NL
		 | stmtSkip NL
		 | expr (externalLambda|NL)
		 | stmtIf
		 | stmtWhile
		 | stmtForLoop
		 | stmtSwitch
		 ;

externalLambda: shortFormalParameters arrow='->' NL statementsBlock;

exprDestroy:
			   'destroy' expr
		   ;

stmtReturn:
			  'return' expr
		  ;

stmtIf:
		  'if' cond=expr NL
		  thenStatements=statementsBlock
		  ('else' elseStatements)?
	  ;

elseStatements:
				  stmtIf
			  | NL statementsBlock
			  ;

stmtSwitch:
			  'switch' expr NL (STARTBLOCK
				switchCase*
				switchDefaultCase?
			  ENDBLOCK)?
		  ;
			
switchCase:
			  'case' (caseExpressions+=expr ('|' caseExpressions+=expr)*) NL statementsBlock
		  ;

switchDefaultCase:
					 'default' NL statementsBlock
				 ;
			  

stmtWhile: 
			 'while' cond=expr NL statementsBlock
		 ;

localVarDef:
		  (var='var'|let='let'|type=typeExpr)
		  name=ID ('=' variableInit)?
	  ;


localVarDefInline:
					 typeExpr? name=ID
				 ;

stmtSet:
		   left=exprAssignable 
		   (assignOp=('='|'+='|'-='|'*='|'/=') right=expr
			| incOp='++'
			| decOp='--'
			) 
	   ;


exprAssignable:
				exprMemberVar
			  | exprVarAccess
			  ;

exprMemberVar: 
				 expr dots=('.'|'..') varname=ID? indexes?
			 ;


exprVarAccess:
				 varname=ID indexes?
			 ;


indexes:
		   '[' expr ']'
	   ;


expr:
		exprPrimary	
	  | left=expr 'castTo' castToType=typeExpr
	  | left=expr 'instanceof' instaneofType=typeExpr
	  | receiver=expr dotsCall=('.'|'..') funcName=ID? typeArgs argumentList
	  | receiver=expr dotsVar=('.'|'..') varName=ID? indexes?
      | left=expr op=('*'|'/'|'%'|'div'|'mod') right=expr
	  | op='-' right=expr // TODO move unary minus one up to be compatible with Java etc.
		                  // currently it is here to be backwards compatible with the old wurst parser
      | left=expr op=('+'|'-') right=expr
	  | left=expr op=('<='|'<'|'>'|'>=') right=expr
	  | left=expr op=('=='|'!=') right=expr
	  | op='not' right=expr
	  | left=expr op='and' right=expr
	  | left=expr op='or' right=expr
	  | cond=expr '?' ifTrueExpr=expr ':' ifFalseExpr=expr
	  |
	;


exprPrimary:
	    exprFunctionCall
      | exprNewObject
	  | exprClosure
	  | exprStatementsBlock
	  | exprDestroy
      | varname=ID indexes?
      | atom=(INT
      | REAL
	  | STRING
	  | 'null'
	  | 'true'
	  | 'false'
	  | 'this'
	  | 'super')
	  | exprFuncRef
      | '(' expr ')' 
	;

exprFuncRef: 'function' (scopeName=ID '.')? funcName=ID;

exprStatementsBlock:
					   'begin' NL statementsBlock 'end'
				   ;

argumentList:
    '(' exprList ')'
    ;

exprFunctionCall:
					funcName=ID typeArgs argumentList
				;
	  
exprNewObject:'new' className=ID typeArgs argumentList?;

exprClosure: shortFormalParameters arrow='->' (skip='skip'|expr);

shortFormalParameters:
      singleParam=shortFormalParameter
    | '(' (params+=shortFormalParameter (',' params+=shortFormalParameter)*)? ')'
    | /* empty */
    ;

shortFormalParameter: typeExpr? name=ID;

typeParams: ('<' (params+=typeParam (',' params+=typeParam)*)? '>')?;

typeParam: name=ID typeParamConstraints?;

typeParamConstraints: ':' (constraints+=typeExpr ('and' constraints+=typeExpr)*)?;


stmtForLoop:
			   forRangeLoop
		   | forIteratorLoop
		   ;

forRangeLoop:
	'for' loopVar=localVarDefInline '=' start=expr direction=('to'|'downto') end=expr ('step' step=expr)? NL statementsBlock
;

forIteratorLoop:
   'for' loopVar=localVarDefInline iterStyle=('in'|'from') iteratorExpr=expr NL statementsBlock
;


stmtBreak:'break';
stmtSkip:'skip';



typeArgs: ('<' (args+=typeExpr (',' args+=typeExpr)*)? '>')?;

exprList : exprs+=expr (',' exprs+=expr)*;



nativeType: 'nativetype' name=ID ('extends' extended=ID)? NL;
initBlock: 'init' NL statementsBlock; 
nativeDef: modifiersWithDoc 'native' funcSignature NL; 
tupleDef: modifiersWithDoc 'tuple' name=ID formalParameters NL; 
extensionFuncDef: modifiersWithDoc 'function' receiverType=typeExpr '.' funcSignature NL statementsBlock;

// Lexer:
VARARG: 'vararg';
CLASS: 'class';
RETURN: 'return';
IF: 'if';
ELSE: 'else';
WHILE: 'while';
FOR: 'for';
IN: 'in';
BREAK: 'break';
NEW: 'new';
NULL: 'null';
PACKAGE: 'package';
ENDPACKAGE: 'endpackage';
FUNCTION: 'function';
RETURNS: 'returns';
PUBLIC: 'public';
PULBICREAD: 'publicread';
PRIVATE: 'private';
PROTECTED: 'protected';
IMPORT: 'import';
INITLATER: 'initlater';
NATIVE: 'native';
NATIVETYPE: 'nativetype';
EXTENDS: 'extends';
INTERFACE: 'interface';
IMPLEMENTS: 'implements';
MODULE: 'module';
USE: 'use';
ABSTRACT: 'abstract';
STATIC: 'static';
THISTYPE: 'thistype';
OVERRIDE: 'override';
IMMUTABLE: 'immutable';
IT: 'it';
ARRAY: 'array';
AND: 'and';
OR: 'or';
NOT: 'not';
THIS: 'this';
CONSTRUCT: 'construct';
ONDESTROY: 'ondestroy';
DESTROY: 'destroy';
TYPE: 'type';
CONSTANT: 'constant';
ENDFUNCTION: 'endfunction';
NOTHING: 'nothing';
INIT: 'init';
CASTTO: 'castTo';
TUPLE: 'tuple';
DIV: 'div';
MOD: 'mod';
LET: 'let';
FROM: 'from';
TO: 'to';
DOWNTO: 'downto';
STEP: 'step';
SKIP_: 'skip';
TRUE: 'true';
FALSE: 'false';
VAR: 'var';
INSTANCEOF: 'instanceof';
SUPER: 'super';
ENUM: 'enum';
SWITCH: 'switch';
CASE: 'case';
DEFAULT: 'default';
BEGIN: 'begin';
END: 'end';
LIBRARY: 'library';
ENDLIBRARY: 'endlibrary';
SCOPE: 'scope';
ENDSCOPE: 'endscope';
REQUIRES: 'requires';
USES: 'uses';
NEEDS: 'needs';
STRUCT: 'struct';
ENDSTRUCT: 'endstruct';
THEN: 'then';
ENDIF: 'endif';
LOOP: 'loop';
EXITHWHEN: 'exithwhen';
ENDLOOP: 'endloop';
METHOD: 'method';
TAKES: 'takes';
ENDMETHOD: 'endmethod';
SET: 'set';
CALL: 'call';
EXITWHEN: 'exitwhen';

COMMA: ',';
PLUS: '+';
PLUSPLUS: '++';
MINUS: '-';
MINUSMINUS: '--';
MULT: '*';
DIV_REAL: '/';
MOD_REAL: '%';
DOT: '.';
DOTDOT: '..';
QUESTION: '?';
COLON: ':';
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

PLUS_EQ: '+=';
MINUS_EQ: '-=';
MULT_EQ: '*=';
DIV_EQ: '/=';
ARROW: '->';

STARTBLOCK:[()];
ENDBLOCK:[()];
INVALID:[()];


JASS_GLOBALS: 'globals';

JASS_ENDGLOBALS: 'endglobals';
JASS_LOCAL: 'local';
JASS_ELSEIF: 'elseif';


NL: [\r\n]+;
ID: [a-zA-Z_][a-zA-Z0-9_]* ;
ANNOTATION: '@' [a-zA-Z0-9_]+;

STRING: '"' ( EscapeSequence | ~('\\'|'"'|'\r'|'\n') )* '"';
REAL: [0-9]+ '.' [0-9]* | '.'[0-9]+;

fragment HexInt: '$'[0-9a-fA-F]+ | '0'[xX][0-9a-fA-F]+;

fragment CharIntPart: ('\\' [btrnf"\\]) | ~[\\'];
fragment CharInt: '\'' CharIntPart+ '\'' | '\'' [\\'] '\'';

INT: [0-9]+ | HexInt | CharInt;

fragment EscapeSequence: '\\' [abfnrtvz"'\\];


TAB: [\t]+;
SPACETAB:' ' ' '+;
SPACES: ' ' -> skip;
HOTDOC_COMMENT: '/**' .*? '*/';
ML_COMMENT: '/*' .*? '*/' -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;
