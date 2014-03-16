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


typeParams:;

statementsBlock:;
typeArgs: ('<' '>')?;
             
expr : 
     
      expr '.' ID '(' exprList ')'
      | expr '.' ID 
      | expr ('*'|'/') expr
      | expr ('+'|'-') expr
      | '-' expr
      | ID
      | INT
      | REAL
      | '(' expr ')'      
     ;

exprList : (expr (',' expr)*)?;



nativeType: 'native';
 initBlock: 'init'; 
nativeDef: 'native'; 
tupleDef: 'tuple'; 
extensionFuncDef: 'extension';

NL: [\r\n]+ | '//' .*? '\n';
ID: [a-zA-Z_][a-zA-Z0-9_]* ;
ANNOTATION: '@' [a-zA-Z0-9_]+;
TAB: [\t];
WS : [ ]+ -> skip ;
ML_COMMENT: '/*' .*? '*/' -> skip;