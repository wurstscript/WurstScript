package de.peeeq.parseq.parser;

import de.peeeq.parseq.Symbol;
import de.peeeq.parseq.TokenType;
%%

%class ParseqScanner
%unicode
%line
%public
%column
%eofval{
  return symbol(TokenType.EOF);
%eofval}
%type Symbol


%{
	  StringBuffer string = new StringBuffer();

  private Symbol symbol(TokenType type) {
    return new Symbol(type, yyline+1, yycolumn);
  }
    
  private Symbol symbol(TokenType type, Object value) {
    return new Symbol(type, yyline+1, yycolumn, value);
  }
%}

DIGIT = [0-9]
LETTER = [a-zA-Z]
WHITESPACE = [ \t\n\r]
IDENT = ({LETTER}|_)({LETTER}|{DIGIT}|_)* 

%state STRING

%%

<YYINITIAL> {
	{WHITESPACE}*                     { }
	"//" [^\r\n]* 			           { }
	"/*" ~"*/"                        { }
	"abstract syntax:"                	{ return symbol(TokenType.ABSTRACT_SYNTAX); }
	"attributes:"                	{ return symbol(TokenType.ATTRIBUTES); }
	"implemented by"                	{ return symbol(TokenType.IMPLEMENTED_BY); }
	"returns"							{ return symbol(TokenType.RETURNS); }
	"package"							{ return symbol(TokenType.PACKAGE); }
	"."									{ return symbol(TokenType.DOT); }
	"<"                               { return symbol(TokenType.LT); }
	">"                               { return symbol(TokenType.GT); }
	"("                               { return symbol(TokenType.LPAR); }
	")"                               { return symbol(TokenType.RPAR); }
	"="                               { return symbol(TokenType.EQ); }
	"|"                               { return symbol(TokenType.PIPE); }
	","                               { return symbol(TokenType.COMMA); }
	"*"                               { return symbol(TokenType.STAR); }
	{IDENT}                           { return symbol(TokenType.IDENTIFIER, yytext()); }
	\"                             		{ string.setLength(0); yybegin(STRING); }
}


<STRING> {
  \"                             { yybegin(YYINITIAL); 
                                   return symbol(TokenType.STRING_LITERAL, 
                                   string.toString()); }
  [^\n\r\"\\]+                   { string.append( yytext() ); }
  \\t                            { string.append('\t'); }
  \\n                            { string.append('\n'); }

  \\r                            { string.append('\r'); }
  \\\"                           { string.append('\"'); }
  \\                             { string.append('\\'); }
}

// error fallback:
.                                 { return symbol(TokenType.ERROR, yytext()); }