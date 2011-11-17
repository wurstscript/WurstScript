package de.peeeq.wurstscript.parser;

import java_cup.runtime.*;
import de.peeeq.wurstscript.utils.Utils;
%%

%class WurstScriptScanner
%unicode
%line
%public
%column
%eofval{
  return symbol(TokenType.EOF);
%eofval}
%cup
//%cupdebug


%{
	  StringBuffer string = new StringBuffer();

  private Symbol symbol(int type) {
    return new Symbol(type, yyline+1, yycolumn);
  }
    
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline+1, yycolumn, value);
  }
%}

DIGIT = [0-9]
LETTER = [a-zA-Z]
NEWLINE = \n | \r | \r\n 
WHITESPACE = [ \t]
IDENT = ({LETTER}|_)({LETTER}|{DIGIT}|_)* 

%state STRING

%%

<YYINITIAL> {
	{WHITESPACE}*                     { }
	"//" [^\r\n]* 			           { }
	"/*" ~"*/"                        { }
	{NEWLINE}							{ return symbol(TokenType.NL); }	
	"class"                           	{ return symbol(TokenType.CLASS); }
	"return"                          	{ return symbol(TokenType.RETURN); }
	"if"                              	{ return symbol(TokenType.IF); }
	"else"                            	{ return symbol(TokenType.ELSE); }
	"while"                           	{ return symbol(TokenType.WHILE); }
	"for"                           	{ return symbol(TokenType.FOR); }
	"in"                           		{ return symbol(TokenType.IN); }
	"new"                             	{ return symbol(TokenType.NEW); }
	"null"                            	{ return symbol(TokenType.NULL); }
	"package"							{ return symbol(TokenType.PACKAGE); }
	"function"							{ return symbol(TokenType.FUNCTION); }
	"returns"							{ return symbol(TokenType.RETURNS); }
//	"val"								{ return symbol(TokenType.VAL); }
	"public"							{ return symbol(TokenType.PUBLIC); }
	"publicread"						{ return symbol(TokenType.PUBLICREAD); }
	"private"							{ return symbol(TokenType.PRIVATE); }
	"protected"							{ return symbol(TokenType.PROTECTED); }
	"import"							{ return symbol(TokenType.IMPORT); }
	"native"							{ return symbol(TokenType.NATIVE); }
	"nativetype"						{ return symbol(TokenType.NATIVETYPE); }
	"extends"							{ return symbol(TokenType.EXTENDS); }
	"interface"							{ return symbol(TokenType.INTERFACE); }
	"implements"						{ return symbol(TokenType.IMPLEMENTS); }
	
	"module"							{ return symbol(TokenType.MODULE); }
	"use"								{ return symbol(TokenType.USE); }
	"abstract"							{ return symbol(TokenType.ABSTRACT); }
	"static"							{ return symbol(TokenType.STATIC); }
	"thistype"							{ return symbol(TokenType.THISTYPE); }
	
	"array"								{ return symbol(TokenType.ARRAY); }
	"and"								{ return symbol(TokenType.AND); }
	"or"								{ return symbol(TokenType.OR); }
	"not"								{ return symbol(TokenType.NOT); }
	"this"								{ return symbol(TokenType.THIS); }
	"construct"							{ return symbol(TokenType.CONSTRUCT); }
	"ondestroy"							{ return symbol(TokenType.ONDESTROY); }
	"destroy"							{ return symbol(TokenType.DESTROY); }
	"type"								{ return symbol(TokenType.TYPE); }
	"globals"							{ return symbol(TokenType.GLOBALS); }
	"endglobals"						{ return symbol(TokenType.ENDGLOBALS); }
	"constant"							{ return symbol(TokenType.CONSTANT); }
	"endfunction"						{ return symbol(TokenType.ENDFUNCTION); }
	"nothing"							{ return symbol(TokenType.NOTHING); }
	"takes"								{ return symbol(TokenType.TAKES); }
	"local"								{ return symbol(TokenType.LOCAL); }
	"loop"								{ return symbol(TokenType.LOOP); }
	"endloop"							{ return symbol(TokenType.ENDLOOP); }
	"exitwhen"							{ return symbol(TokenType.EXITWHEN); }
	"set"								{ return symbol(TokenType.SET); }
	"call"								{ return symbol(TokenType.CALL); }
	"then"								{ return symbol(TokenType.THEN); }
	"elseif"							{ return symbol(TokenType.ELSEIF); }
	"endif"								{ return symbol(TokenType.ENDIF); }
	"init"								{ return symbol(TokenType.INIT); }
	"castTo"							{ return symbol(TokenType.CASTTO); }
	"("                               { return symbol(TokenType.LPAR); }
	")"                               { return symbol(TokenType.RPAR); }
	","                               { return symbol(TokenType.COMMA); }
	"{"                               { return symbol(TokenType.LBRACK); }
	"}"                               { return symbol(TokenType.RBRACK); }
	"["                               { return symbol(TokenType.LSQUARE); }
	"]"                               { return symbol(TokenType.RSQUARE); }
	"."                               { return symbol(TokenType.DOT); }
	"+"                               { return symbol(TokenType.PLUS); }
	"-"                               { return symbol(TokenType.MINUS); }
	"*"                               { return symbol(TokenType.MULT); }
	"/"                               { return symbol(TokenType.DIV_REAL); }
	"div"                               { return symbol(TokenType.DIV_INT); }
	"%"                               { return symbol(TokenType.MOD_REAL); }
	"mod"                               { return symbol(TokenType.MOD_INT); }
	"=="                              { return symbol(TokenType.EQEQ); }
	"!="                              { return symbol(TokenType.NOTEQ); }
	">="                              { return symbol(TokenType.GTEQ); }
	"<="                              { return symbol(TokenType.LTEQ); }
	"<"                              { return symbol(TokenType.LT); }
	">"                              { return symbol(TokenType.GT); }
	"="                               { return symbol(TokenType.EQ); }
	{DIGIT}+                          { return symbol(TokenType.INTEGER_LITERAL, Utils.parseInt(yytext())); }
	"0x" [0-9a-fA-F]+                          { return symbol(TokenType.INTEGER_LITERAL, Utils.parseHexInt(yytext())); }
	"'" . "'"						  { return symbol(TokenType.INTEGER_LITERAL, Utils.parseAsciiInt1(yytext())); }
	"'" . . . . "'"					{ return symbol(TokenType.INTEGER_LITERAL, Utils.parseAsciiInt4(yytext())); }
	{DIGIT}+ "." {DIGIT}*			  { return symbol(TokenType.REAL_LITERAL, Double.parseDouble(yytext())); }
	"." {DIGIT}+					  { return symbol(TokenType.REAL_LITERAL, Double.parseDouble(yytext())); } 
	"true"                            { return symbol(TokenType.TRUE); }
	"false"                           { return symbol(TokenType.FALSE); }
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
.                                 { return symbol(TokenType.error, yytext()); }