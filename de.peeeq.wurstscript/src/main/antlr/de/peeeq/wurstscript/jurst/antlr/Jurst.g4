grammar Jurst;


@header {
    package de.peeeq.wurstscript.jurst.antlr;
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
				  constant='constant'? typeExpr name=id ('=' initial=expr)? NL
			  ;

jassFuncDef:
			   constant='constant'? 'function' jassFuncSignature NL
			   (jassLocals+=jassLocal)*
			   jassStatements
			   'endfunction' NL
		   ;

jassLocal: 'local' typeExpr name=id ('=' initial=expr)? NL;

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
					 name=id 'takes' ('nothing' | args+=formalParameter (',' args+=formalParameter)*)
					 'returns' ('nothing'|returnType=typeExpr)
				 ;

jassTypeDecl: 'type' name=id 'extends' extended=typeExpr NL;


wpackage: ('package'|'library'|'scope') name=id 
		  ('initializer' initializer=id)?
		  (('uses'|'requires'|'needs') 'optional'? requires+=id (',' 'optional'? requires+=id)*)?	  
		  NL 
	imports+=wImport* entities+=entity*
	('endpackage'|'endlibrary'|'endscope'|'end') NL
	;


wImport: 
    'import' isPublic='public'? isInitLater='initlater'? importedPackage=id NL 
    ;



entity: 
        nativeType
      | funcDef
      | functionInterfaceDef
	  | globalsBlock
      | initBlock
      | nativeDef
      | classDef
      | enumDef
      | moduleDef
      | interfaceDef
      | tupleDef
      | extensionFuncDef
      | varDef
;


globalsBlock: 'globals' NL vars+=varDef* ('endglobals' | 'end') NL;

interfaceDef:
                modifiersWithDoc 'interface' name=id typeParams 
                ('extends' extended+=typeExpr (',' extended+=typeExpr)*)? 
                NL
                    classSlots
                ('end'|'endinterface') NL
            ;
             
 
classDef:
            modifiersWithDoc ('class'|'struct') name=id typeParams 
            ('extends' (extended=typeExpr | 'array'))? 
            ('implements' implemented+=typeExpr (',' implemented+=typeExpr)*)?
            NL
                classSlots
			('end'|'endclass'|'endstruct') NL
        ;

enumDef: modifiersWithDoc 'enum' name=id NL 
      (enumMembers+=id NL)*
	('end'|'endenum') NL
	   ;

moduleDef:
            modifiersWithDoc 'module' name=id typeParams
            NL
                classSlots
            ('end'|'endmodule') NL
         ;

classSlots: slots+=classSlot*;

classSlot:
           constructorDef
         | moduleUse
         | ondestroyDef
         | varDef
         | funcDef
         ;

constructorDef:
                  modifiersWithDoc 'construct' formalParameters NL 
					('super' '(' superArgs=exprList ')' NL)?
					stmts+=statement*
                  ('end'|'endconstruct') NL
              ;
       
moduleUse: 
         modifiersWithDoc ('use'|'implement') optional='optional'? moduleName=id typeArgs NL
         ;

ondestroyDef:
            'ondestroy' NL statementsBlock ('end'|'endondestroy') NL
            ;


funcDef:
       modifiersWithDoc ('function'|'method') funcSignature 
       (
	         NL statementsBlock ('end'|'endfunction'|'endmethod')
	       | 'defaults' (defaultExpr=expr|defaultsNothing='nothing')
       )? 
       NL 
       ;

functionInterfaceDef:
	modifiersWithDoc 'function' 'interface' funcSignature NL
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
		| 'readonly'
		| 'static'
		| 'override'
		| 'abstract' 
		| 'constant'
		| 'delegate'
		| 'stub'
			)
		| annotation
		;

annotation: name=ANNOTATION (PAREN_LEFT message=STRING PAREN_RIGHT)?;

hotdocComment: HOTDOC_COMMENT;

funcSignature:
				 name=id typeParams formalParameters ('returns' ('nothing' | returnType=typeExpr))?
			 ;

formalParameters: 
				  '(' (params+=formalParameter (',' params+=formalParameter)*)? ')'
				| 'takes' 'nothing'
				| 'takes' params+=formalParameter (',' params+=formalParameter)*
				;

formalParameter:
				   typeExpr name=id
			   ;

typeExpr:
		  thistype='thistype'
		| typeName=ID typeArgs
		| typeExpr 'array' ('[' arraySizes+=expr ']')*
		;

varDef:
		  modifiersWithDoc 
		  ('var'|constant='constant' varType=typeExpr?|constant='let'|varType=typeExpr)
		  name=id ('[' arraySizes+=expr ']')* ('=' initial=expr)? NL 
	  ;		  

statements: statement*;

statementsBlock: statement*;


statement:
			 stmtIf
		 | stmtLoop
		 | stmtExitwhen
		 | stmtWhile
		 | localVarDef
		 | stmtSet
		 | stmtCall
		 | stmtReturn		 
		 | stmtForLoop
		 | stmtBreak
		 | stmtSkip
		 | stmtSwitch
		 ;

stmtLoop: 'loop' NL statementsBlock ('end'|'endloop') NL;

stmtExitwhen: 'exitwhen' expr NL;

exprDestroy:
			   'destroy' expr
		     | expr '.' 'destroy' ('(' ')')?
		   ;

stmtReturn:
			  'return' expr NL
		  ;

stmtIf:
		  isStatic='static'? 'if' cond=expr 'then'? NL
		  thenStatements=statementsBlock
		  elseStatements
	  ;

elseStatements:
				'elseif' cond=expr 'then'? NL
				thenStatements=statementsBlock
				elseStatements
			  | 'else' NL statementsBlock ('endif'|'end') NL
			  | ('endif'|'end') NL
			  ;

stmtSwitch:
			  'switch' expr NL
				switchCase*
				switchDefaultCase?
			  ('end'|'endswitch')
		  ;
			
switchCase:
			  'case' expr NL statementsBlock
		  ;

switchDefaultCase:
					 'default' NL statementsBlock
				 ;
			  

stmtWhile: 
			 'while' cond=expr NL statementsBlock
			 ('end'|'endwhile')
		 ;

localVarDef:
		  (var='var'|let='let'|'local'? type=typeExpr)
		  name=id ('=' initial=expr)? NL 
	  ;	

localVarDefInline:
					 typeExpr? name=id
				 ;

stmtSet:
		   'set'? left=exprAssignable 
		   (assignOp=('='|'+='|'-='|'*='|'/=') right=expr
			| incOp='++'
			| decOp='--'
			) 
		   NL
	   ;


exprAssignable:
				exprMemberVar
			  | exprVarAccess
			  ;

exprMemberVar: 
				 expr dots=('.'|'..') varname=id indexes*
			 ;


exprVarAccess:
				 varname=id indexes*
			 ;


indexes:
		   '[' expr ']'
	   ;

stmtCall: 'call'?
		(
			exprMemberMethod
		  | exprFunctionCall
		  | exprNewObject
		  | exprDestroy
		)  NL
		;

exprMemberMethod:
					receiver=expr dots=('.'|'..') funcName=id? typeArgs ('(' exprList ')')?
				;

expr:
		exprPrimary	
	  | left=expr 'castTo' castToType=typeExpr
	  | left=expr 'instanceof' instaneofType=typeExpr
	  | receiver=expr dotsCall=('.'|'..') funcName=id? typeArgs '(' exprList ')'
	  | receiver=expr dotsVar=('.'|'..') varName=id? indexes*
	  |	'destroy' destroyedObject=expr
	  | destroyedObject=expr '.' 'destroy' '(' ')'
      | left=expr op=('*'|'/'|'%'|'div'|'mod') right=expr
	  | op='-' right=expr // TODO move unary minus one up to be compatible with Java etc.
		                  // currently it is here to be backwards compatible with the old wurst parser
      | left=expr op=('+'|'-') right=expr
	  | left=expr op=('<='|'<'|'>'|'>=') right=expr
	  | left=expr op=('=='|'!=') right=expr
	  | op='not' right=expr
	  // The order here is intentional, Jass is maybe the only language where 'or' has higher precedence than 'and'
	  // We inherit it for Jurst, because we use the Jurst parser for parsing Jass code and we want Jurst to be close to Jass
	  | left=expr op='or' right=expr
	  | left=expr op='and' right=expr
	  |
	;


exprPrimary:
	    exprFunctionCall
      | exprNewObject
	  | exprClosure
	  | exprStatementsBlock
      | atom=(INT
      | REAL
	  | STRING
	  | 'null'
	  | 'true'
	  | 'false'
	  | 'this'
	  | 'super')
	  | exprFuncRef
      | varname=id indexes*
      | '(' expr ')' 
	;

exprFuncRef: 'function' (scopeName=id '.')? funcName=id;

exprStatementsBlock:
					   'begin' NL statementsBlock 'end'
				   ;


exprFunctionCall:
					funcName=id typeArgs '(' exprList ')'
				;
	  
exprNewObject:'new' className=id typeArgs ('(' exprList ')')?;

exprClosure: formalParameters arrow='->' expr;
		  
typeParams: ('<' (params+=typeParam (',' params+=typeParam)*)? '>')?;

typeParam: name=id;

stmtForLoop:
			   forRangeLoop
		   | forIteratorLoop
		   ;

forRangeLoop:
	'for' loopVar=localVarDefInline '=' start=expr direction=('to'|'downto') end=expr ('step' step=expr)? NL 
		statementsBlock 
	('end'|'endfor')
;

forIteratorLoop:
   'for' loopVar=localVarDefInline iterStyle=('in'|'from') iteratorExpr=expr NL 
	statementsBlock
   ('end'|'endfor')
;


stmtBreak:'break' NL;
stmtSkip:'skip' NL;



typeArgs: ('<' (args+=typeExpr (',' args+=typeExpr)*)? '>')?;

exprList : exprs+=expr (',' exprs+=expr)*;



nativeType: 'type' name=id ('extends' extended=id)? NL;

initBlock: 'init' NL 
				statementsBlock
			('end'|'endinit') NL; 

nativeDef: modifiersWithDoc 'native' funcSignature NL; 

tupleDef: modifiersWithDoc 'tuple' name=id formalParameters NL; 

extensionFuncDef: modifiersWithDoc 'function' receiverType=typeExpr '.' funcSignature NL 
		statementsBlock 
	('end'|'endfunction');


// some keywords are also valid identifiers for backwards compatibility reasons
id: ID|'end'|'init'|'this'|'new'|'destroy'|'it'|'to'|'from'|'class'|'thistype';

// Lexer:

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
READONLY: 'readonly';
DELEGATE: 'delegate';
STUB: 'stub';
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
INITIALIZER: 'initializer';
ENDINTERFACE: 'endinterface';
ENDCLASS: 'endclass';
ENDENUM: 'endenum';
ENDMODULE: 'endmodule';
ENDCONSTRUCT: 'endconstruct';
IMPLEMENT: 'implement';
ENDONDESTROY: 'endondestroy';
ENDSWITCH: 'endswitch';
ENDWHILE: 'endwhile';
ENDFOR: 'endfor';
ENDINIT: 'endinit';

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

INVALID:[()];


JASS_GLOBALS: 'globals';

JASS_ENDGLOBALS: 'endglobals';
JASS_LOCAL: 'local';
JASS_ELSEIF: 'elseif';


NL: [\r\n]+;
ID: [a-zA-Z_][a-zA-Z0-9_]* ;
ANNOTATION: '@' [a-zA-Z0-9_]+;

STRING: '"' ( EscapeSequence | ~('\\'|'"'|'\r'|'\n') | NL )* '"';
REAL: [0-9]+ '.' [0-9]* | '.'[0-9]+;

fragment HexInt: '$'[0-9a-fA-F]+ | '0'[xX][0-9a-fA-F]+;

fragment CharIntPart: ('\\' [btrnf"\\]) | ~[\\'];
fragment CharInt: '\'' CharIntPart+ '\'';

INT: [0-9]+ | HexInt | CharInt;

fragment EscapeSequence: '\\' [abfnrtvz"'\\];

DEBUG: 'debug' -> skip;
WS : [ \t]+ -> skip ;
HOTDOC_COMMENT: '/**' .*? '*/';
ML_COMMENT: '/*' .*? '*/' -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;
