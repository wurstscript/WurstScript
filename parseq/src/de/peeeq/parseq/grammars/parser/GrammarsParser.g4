grammar GrammarsParser;

/*
Parser for parseq grammars
*/	

@header {
	package de.peeeq.parseq.grammars.parser;	
	import de.peeeq.parseq.asts.ast.*;
	import com.google.common.collect.Lists;
	import java.util.ArrayList;
	import java.util.List;
}

grammarFilee: 
	grammarRule+ 
	EOF
;

grammarRule:
ID ID? ':' gExpr ';'
;

gExpr:
	  gExprParts ('|' gExprParts)*
;

gExprParts:
	gExprPart+
;

gExprPart:
	gExprAtomic ('+' | '-' | '*' )? // repeat-modifier
;

gExprAtomic:
	  LEX 
	| ID
	| '(' gExpr ')'
;


// Lexer rules:

fragment ID_START :  'a' .. 'z' | 'A' .. 'Z' ;
fragment ID_PART :  ID_START | '_' | '0' ..'9' ;

ID : ID_START ID_PART*;




LEX : '\'' (~('\''))* '\'';
STRVAL : '"' (~('"'))* '"';
WS  :   (' '|'\t'|'\n'|'\r')+ {skip();} ;
COMMENT : '//' (~('\n'|'\r'))* {skip();};
