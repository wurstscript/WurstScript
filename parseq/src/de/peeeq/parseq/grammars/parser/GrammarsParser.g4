grammar GrammarsParser;

/*
Parser for parseq grammars
*/	

@header {
	package de.peeeq.parseq.grammars.parser;	
	import de.peeeq.parseq.grammars.ast.*;
	import com.google.common.collect.Lists;
	import java.util.ArrayList;
	import java.util.List;
}

grammarFile returns [GrammarFile result]: 
	rules+=grammarRule+ 
	EOF
	{ $result = new GrammarFile($rules); }
;

grammarRule returns [Rule result]:
t=ID? n=ID ':' p=gExpr ';' 
	{ $result = new Rule($t,$n,$p.result); }
;

gExpr returns [Production result]:
	  parts+=gExprParts ('|' parts+=gExprParts)*
	  { $result = new ProdAlternative($parts); }
;

gExprParts returns [Production result]:
	parts+=gExprPart+ 
		{ $result = new ProdSequence($parts); }
;

gExprPart returns [Production result]:
	e=gExprAtomic mod=('+' | '-' | '*')?
	{ $result = ProdRepeat.create($e.result, $mod);}
;

gExprAtomic returns [Production result]:
	(name=ID op=('='|'+='))?
	(
	   lex=LEX  			{ $result = new ProdLex($name, $op, $lex); }
	|  i=ID					{ $result = new ProdId($name, $op, $i); }
	| '(' e=gExpr ')'	{ $result = $e.result; }
	)		
;


// Lexer rules:

fragment ID_START :  'a' .. 'z' | 'A' .. 'Z' ;
fragment ID_PART :  ID_START | '_' | '0' ..'9' ;

ID : ID_START ID_PART*;




LEX : '\'' (~('\''))* '\'';
STRVAL : '"' (~('"'))* '"';
WS  :   (' '|'\t'|'\n'|'\r')+ {skip();} ;
COMMENT : '//' (~('\n'|'\r'))* {skip();};
