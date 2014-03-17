grammar Wurst;

compilationUnit : wpackage;

wpackage: 'package' name=ID NL imports=wImport* entities=entity*;

wImport: 
    'import' isPublic='public'?  importedPackage=ID NL 
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
                NL STARTBLOCK
                    classSlots
                ENDBLOCK
            ;
             
 
classDef:
            modifiersWithDoc 'class' name=ID typeParams 
            ('extends' extended=typeExpr)? 
            ('implements' implemented+=typeExpr (',' implemented+=typeExpr)*)?
            NL STARTBLOCK
                classSlots
            ENDBLOCK
        ;

enumDef: modifiersWithDoc 'enum' name=ID NL (STARTBLOCK 
      (enumMembers+=ID NL)*
ENDBLOCK)?;

moduleDef:
            modifiersWithDoc 'module' typeParams
            NL STARTBLOCK
                classSlots
            ENDBLOCK
         ;

classSlots: classSlot*;

classSlot:
           constructorDef
         | moduleUse
         | ondestroyDef
         | varDef
         | funcDef
         ;

constructorDef:
                  modifiersWithDoc 'construct' formalParameters NL 
                  statementsBlock
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
	modifier*
;

modifier:
			'public' 
		| 'private'
		| 'protected'
		| 'publicread'
		| 'static'
		| 'override'
		| 'abstract' 
		| annotation
		;

annotation: ANNOTATION;

funcSignature:
				 name=ID typeParams formalParameters
			 ;

formalParameters: '(' (formalParameter (',' formalParameter)*)? ')';

formalParameter:
				   typeExpr name=ID
			   ;

typeExpr:
		 'thistype'
		| typeName=ID typeArgs
		| typeExpr 'array'
		;

varDef:
		  modifiersWithDoc 
		  ('var'|'constant' typeExpr?|'let'|typeExpr)
		  name=ID ('=' expr)? NL 
	  ;		  

// line 523 TODO


statements:
			statement*
		  ;

statementsBlock:
			   STARTBLOCK statement* ENDBLOCK;


statement:
			 stmtIf
		 | stmtWhile
		 | localVarDef
		 | stmtSet
		 | stmtCall
		 | stmtReturn
		 | exprDestroy
		 | stmtForLoop
		 | stmtBreak
		 | stmtSkip
		 | stmtSwitch
		 ;

exprDestroy:
			   'destroy' expr
		   ;

stmtReturn:
			  'return' expr?
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
			  'case' expr NL statementsBlock
		  ;

switchDefaultCase:
					 'default' NL statementsBlock
				 ;
			  

stmtWhile: 
			 'while' cond=expr NL statementsBlock
		 ;

localVarDef:
		  ('var'|'let'|typeExpr)
		  name=ID ('=' expr)? NL 
	  ;	

localVarDefInline:
					 typeExpr? name=ID
				 ;

stmtSet:
		   left=exprAssignable 
		   (('='|'+='|'-='|'*='|'/=') right=expr
			| '++'
			| '--'
			) 
		   NL
	   ;


exprAssignable:
				  exprMemberVar
			  | exprMemberArrayVar
			  | exprVarAccess
			  | exprVarArrayAccess
			  ;

exprVarAccess:
				 name=ID
			 ;

exprVarArrayAccess:
					  name=ID indexes
				  ;

indexes:
		   '[' expr ']'
	   ;

stmtCall:
			exprMemberMethod NL
		| exprFunctionCall NL
		| exprNewObject NL
		;

expr:
		exprPrimary
	  | expr ('.'|'..') ID typeArgs '(' exprList ')'
	  | expr '.' ID indexes?
	  | '-' expr
	  | 'not' expr
      | expr ('*'|'/'|'div'|'mod') expr
      | expr ('+'|'-') expr
	  | expr ('<='|'<'|'>'|'>=') expr
	  | expr ('=='|'!=') expr
	  | expr 'and' expr
	  | expr 'or' expr
	  | expr 'castTo' typeExpr
	  | expr 'instanceof' typeExpr
	;

exprPrimary:
	  | exprFunctionCall
      | exprNewObject
	  | exprClosure
	  | exprStatementsBlock
      | ID indexes?
      | INT
      | REAL
	  | STRING
	  | 'null'
	  | 'true'
	  | 'false'
	  | 'this'
	  | exprFuncRef
      | '(' expr ')' 
	;

exprFuncRef: 'function' (scopeName=ID '.')funcName=ID;

exprStatementsBlock:
					   'begin' NL statementsBlock 'end'
				   ;


exprFunctionCall:
					ID '(' exprList ')'
				;
	  
exprNewObject:'new' ID typeArgs ('(' exprList ')')?;

exprClosure: formalParameters '->' expr;
		  
exprMemberMethod:'TODO';

exprMemberVar:'TODO';
exprMemberArrayVar:'TODO';
typeParams:'TODO';
stmtForLoop:'TODO';
stmtBreak:'TODO';
stmtSkip:'TODO';



typeArgs: ('<' '>')?;

exprList : (expr (',' expr)*)?;



nativeType: 'native';
 initBlock: 'init'; 
nativeDef: 'native'; 
tupleDef: 'tuple'; 
extensionFuncDef: 'extension';

NL: [\r\n]+;
ID: [a-zA-Z_][a-zA-Z0-9_]* ;
ANNOTATION: '@' [a-zA-Z0-9_]+;

STRING: '"' ( EscapeSequence | ~('\\'|'"') )* '"';
REAL: [0-9]+ '.' [0-9]* | '.'[0-9]+;
INT: [0-9]+ | '0x' [0-9a-fA-F]+ | '\'' . . . . '\'' | '\'' . '\'';

fragment EscapeSequence: '\\' [abfnrtvz"'\\];


TAB: [\t];
WS : [ ]+ -> skip ;
ML_COMMENT: '/*' .*? '*/' -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;