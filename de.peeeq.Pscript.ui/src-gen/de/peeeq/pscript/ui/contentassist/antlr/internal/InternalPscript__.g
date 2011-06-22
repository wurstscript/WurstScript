lexer grammar InternalPscript;
@header {
package de.peeeq.pscript.ui.contentassist.antlr.internal;

// Hack: Use our own Lexer superclass by means of import. 
// Currently there is no other way to specify the superclass for the lexer.
import org.eclipse.xtext.ui.editor.contentassist.antlr.internal.Lexer;
}

T14 : 'var' ;
T15 : '=' ;
T16 : '+=' ;
T17 : '-=' ;
T18 : '!=' ;
T19 : '==' ;
T20 : '<=' ;
T21 : '<' ;
T22 : '>=' ;
T23 : '>' ;
T24 : '+' ;
T25 : '-' ;
T26 : '*' ;
T27 : '/' ;
T28 : '%' ;
T29 : 'mod' ;
T30 : 'div' ;
T31 : 'package' ;
T32 : '{' ;
T33 : '}' ;
T34 : 'import' ;
T35 : '.' ;
T36 : '.*' ;
T37 : 'native' ;
T38 : 'type' ;
T39 : 'class' ;
T40 : ':' ;
T41 : 'function' ;
T42 : '(' ;
T43 : ')' ;
T44 : ',' ;
T45 : 'return' ;
T46 : 'if' ;
T47 : 'else' ;
T48 : 'while' ;
T49 : 'buildin' ;
T50 : 'val' ;
T51 : 'or' ;
T52 : 'and' ;
T53 : 'not' ;

// $ANTLR src "../de.peeeq.Pscript.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g" 8322
RULE_ID : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*;

// $ANTLR src "../de.peeeq.Pscript.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g" 8324
RULE_INT : ('0'..'9')+;

// $ANTLR src "../de.peeeq.Pscript.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g" 8326
RULE_NUMBER : '0..9'+ '.' '0..9'*;

// $ANTLR src "../de.peeeq.Pscript.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g" 8328
RULE_STRING : ('"' ('\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')|~(('\\'|'"')))* '"'|'\'' ('\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')|~(('\\'|'\'')))* '\'');

// $ANTLR src "../de.peeeq.Pscript.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g" 8330
RULE_ML_COMMENT : '/*' ( options {greedy=false;} : . )*'*/';

// $ANTLR src "../de.peeeq.Pscript.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g" 8332
RULE_SL_COMMENT : '//' ~(('\n'|'\r'))* ('\r'? '\n')?;

// $ANTLR src "../de.peeeq.Pscript.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g" 8334
RULE_WS : (' '|'\t')+;

// $ANTLR src "../de.peeeq.Pscript.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g" 8336
RULE_NL : ('\n\r'|'\n'|'\r'|'\r\n');

// $ANTLR src "../de.peeeq.Pscript.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g" 8338
RULE_OPERATOR : ('+'|'-'|'*'|'/'|'%'|'$'|'<'|'>'|'='|'~'|'!'|'^'|'|'|'&'|':')+;

// $ANTLR src "../de.peeeq.Pscript.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g" 8340
RULE_ANY_OTHER : .;


