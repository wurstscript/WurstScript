grammar ParseqAntlrParser;

	

@header {
	package de.peeeq.parseq.asts.parser;	
	import de.peeeq.parseq.asts.ast.*;
	import com.google.common.collect.Lists;
	import java.util.ArrayList;
	import java.util.List;
}



spec returns [Program prog]:
	'package' p=qID
	{
	 $prog = new Program($p.text);
	}
	'abstract syntax:'
	element[$prog]*
	'attributes:'
	attributeDef[$prog]*
	EOF
	;
	
element[Program prog]:
	contructorDef[prog]
	| listDef[prog]
	| caseDef[prog]
	;	

contructorDef[Program prog] returns [ConstructorDef c]:
	name=ID 
	{
		$c = new ConstructorDef($name.text);
		prog.addConstructorDef($c);		
	}
	'(' (paramDef[$c] (',' paramDef[$c] )*)? ')'
	;
	
paramDef[ConstructorDef c]:
	{
		boolean ref = false;
	}
	('&' {ref = true;})?
	 t=javaType n=ID 
	{ 
		$c.addParam(ref, $t.name, $n.text);
	}
	;
	
listDef[Program prog]:
	name=ID '*' of=ID
	{
		$prog.addListDef(new ListDef($name.text, $of.text));
	}
	;
	
caseDef[Program prog] returns [CaseDef cd]:
	name=ID 
	{
		$cd = new CaseDef($name.text);
		prog.addCaseDef($cd);
	}
	'=' c=choice[prog, $cd] ('|' c=choice[prog, $cd])*
	;
	
choice[Program prog, CaseDef cd]:
	  name=ID { cd.addAlternative($name.text); }
	| c=contructorDef[prog] { cd.addAlternative($c.c.getName()); }
	;

attributeDef[Program prog]:
	{
		List<Parameter> parameters = null;
		String circ = null;
	}
	elem=ID '.' attrName=ID
	 (
		'('
		 {
		 	parameters = new ArrayList<Parameter>();
		 }
		 (t=javaType n=ID 
			{ 
				parameters.add(new Parameter($t.name, $n.text));
			}
			(',' t=javaType n=ID 
			{
				parameters.add(new Parameter($t.name, $n.text));
			}
			)*
		)?
		')'
	 )?
	 (doc=STRVAL)? 'returns' returnType=javaType 'implemented' 'by' implementedBy=qID
	 ('circular' circ1=qID {circ=$circ1.s;} )?
	{
		prog.addAttribute(parameters, $elem.text, $attrName.text, $returnType.name, $implementedBy.s, $doc.text, circ);	
	}
	
	;
	
javaType returns [String name]: 
	n1=qID { $name = $n1.text; } ('<' n=javaType { $name += "<" + $n.name; } (',' n=javaType { $name +=", " + $n.name; } )* { $name += ">"; } '>')?;


qID returns [String s]: n=ID {$s=$n.text;} ('.' n=ID {$s+="."+$n.text;})*;

// Lexer rules:

fragment ID_START :  'a' .. 'z' | 'A' .. 'Z' ;
fragment ID_PART :  ID_START | '_' | '0' ..'9' ;

ID : ID_START ID_PART*;




STRVAL : '"' (~('"'))* '"';
WS  :   (' '|'\t'|'\n'|'\r')+ {skip();} ;
COMMENT : '//' (~('\n'|'\r'))* {skip();};
