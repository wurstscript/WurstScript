lexer grammar InternalPscript;
@header {
package de.peeeq.pscript.parser.antlr.internal;

// Hack: Use our own Lexer superclass by means of import. 
// Currently there is no other way to specify the superclass for the lexer.
import org.eclipse.xtext.parser.antlr.Lexer;
}

T14 : 'package' ;
T15 : '{' ;
T16 : '}' ;
T17 : 'import' ;
T18 : '.' ;
T19 : '.*' ;
T20 : 'native' ;
T21 : 'type' ;
T22 : '=' ;
T23 : 'class' ;
T24 : 'var' ;
T25 : 'val' ;
T26 : ':' ;
T27 : 'function' ;
T28 : '(' ;
T29 : ',' ;
T30 : ')' ;
T31 : 'return' ;
T32 : 'if' ;
T33 : 'else' ;
T34 : 'while' ;
T35 : '+=' ;
T36 : '-=' ;
T37 : 'or' ;
T38 : 'and' ;
T39 : '!=' ;
T40 : '==' ;
T41 : '<=' ;
T42 : '<' ;
T43 : '>=' ;
T44 : '>' ;
T45 : '+' ;
T46 : '-' ;
T47 : '*' ;
T48 : '/' ;
T49 : '%' ;
T50 : 'mod' ;
T51 : 'div' ;
T52 : 'not' ;
T53 : 'buildin' ;

// $ANTLR src "../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g" 3472
RULE_ID : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*;

// $ANTLR src "../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g" 3474
RULE_INT : ('0'..'9')+;

// $ANTLR src "../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g" 3476
RULE_NUMBER : '0..9'+ '.' '0..9'*;

// $ANTLR src "../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g" 3478
RULE_STRING : ('"' ('\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')|~(('\\'|'"')))* '"'|'\'' ('\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')|~(('\\'|'\'')))* '\'');

// $ANTLR src "../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g" 3480
RULE_ML_COMMENT : '/*' ( options {greedy=false;} : . )*'*/';

// $ANTLR src "../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g" 3482
RULE_SL_COMMENT : '//' ~(('\n'|'\r'))* ('\r'? '\n')?;

// $ANTLR src "../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g" 3484
RULE_WS : (' '|'\t')+;

// $ANTLR src "../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g" 3486
RULE_NL : ('\n\r'|'\n'|'\r'|'\r\n');

// $ANTLR src "../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g" 3488
RULE_OPERATOR : ('+'|'-'|'*'|'/'|'%'|'$'|'<'|'>'|'='|'~'|'!'|'^'|'|'|'&'|':')+;

// $ANTLR src "../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g" 3490
RULE_ANY_OTHER : .;


