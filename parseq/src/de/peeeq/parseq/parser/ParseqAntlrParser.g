grammar ParseqAntlrParser;

	

@lexer::header {
	package de.peeeq.parseq.parser;	
}

@header {
	package de.peeeq.parseq.parser;	
	import de.peeeq.parseq.ast.*;
	import com.google.common.collect.Lists;
}

@members {
    private List<String> errors = new ArrayList<String>();
    public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, tokenNames);
        errors.add(hdr + " " + msg);
        //throw new Error(e);
    }
    public List<String> getErrors() {
        return errors;
    }
}



spec returns [Program prog]:
	'package' p=qID
	{
	 $prog = new Program(p);
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
		$c = new ConstructorDef(name.getText());
		prog.addConstructorDef($c);		
	}
	'(' (t=javaType n=ID 
	{ 
		$c.addParam(t, n.getText());
	}
	(',' t=javaType n=ID 
	{
		$c.addParam(t, n.getText());
	}
	)*)? ')'
	;
	
listDef[Program prog]:
	name=ID '*' of=ID
	{
		$prog.addListDef(new ListDef(name.getText(), of.getText()));
	}
	;
	
caseDef[Program prog] returns [CaseDef caseDef]:
	name=ID 
	{
		$caseDef = new CaseDef(name.getText());
		prog.addCaseDef($caseDef);
	}
	'=' c=choice[prog, caseDef] ('|' c=choice[prog, caseDef])*
	;
	
choice[Program prog, CaseDef caseDef]:
	  name=ID { caseDef.addAlternative(name.getText()); }
	| c=contructorDef[prog] { prog.addConstructorDef(c); caseDef.addAlternative(c.getName()); }
	;

attributeDef[Program prog]:
	{
		List<Parameter> parameters = null;
	}
	elem=ID '.' attrName=ID
	 (
		'('
		 {
		 	parameters = new ArrayList<Parameter>();
		 }
		 (t=javaType n=ID 
			{ 
				parameters.add(new Parameter(t, n.getText()));
			}
			(',' t=javaType n=ID 
			{
				parameters.add(new Parameter(t, n.getText()));
			}
			)*
		)?
		')'
	 )?
	 (doc=STRVAL)? 'returns' returnType=javaType 'implemented' 'by' implementedBy=qID
	{
		prog.addAttribute(parameters, elem.getText(), attrName.getText(), returnType, implementedBy, doc);	
	}
	;
	
javaType returns [String name]: 
	n1=qID { $name = n1; } ('<' n=javaType { $name += "<" + n; } (',' n=javaType { $name +=", " + n; } )* { $name += ">"; } '>')?;


qID returns [String s]: n=ID {$s=n.getText();} ('.' n=ID {$s+="."+n.getText();})*;

// Lexer rules:

fragment ID_START :  'a' .. 'z' | 'A' .. 'Z' ;
fragment ID_PART :  ID_START | '_' | '0' ..'9' ;

ID : ID_START ID_PART*;




STRVAL : '"' (~('"'))* '"';
WS  :   (' '|'\t'|'\n'|'\r')+ {skip();} ;
COMMENT : '//' (~('\n'|'\r'))* {skip();};
