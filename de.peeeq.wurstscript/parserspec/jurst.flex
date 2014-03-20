package de.peeeq.wurstscript.parser;

import java.util.List;
import java.util.Stack;

import com.google.common.collect.Lists;

import java_cup.runtime.Symbol;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.utils.LineOffsets;
import de.peeeq.wurstscript.utils.Utils;


%%

%class JurstScannerIntern
%unicode
%line
//%public
%column
%char
%eofval{
  return symbol(JurstTokenType.EOF);
%eofval}
%cup
//%cupdebug


%{
	StringBuffer string = new StringBuffer();
	
	// a stack of indentation levels
	Stack<Integer> indentationLevels = new Stack<Integer>();
	Stack<Symbol> returnStack = new Stack<Symbol>();
	
	int currentLineWhiteSpace = 0;
	boolean isStart = true; // are we at the start of a line before the text begins
//	int mode = 0; // 0: unknown mode, 1: space mode, 2: tab mode
	
	int spaceErrorStart = -1; // was there already an error in this line
	protected List<ScannerError> errors = Lists.newArrayList();		
	
	LineOffsets lineStartOffsets = new LineOffsets(); 
	int currentLine = -1;
	
	int numberOfParantheses = 0;
	Stack<Integer> beginEndParenthesesStack = new Stack<Integer>();

	{
		indentationLevels.push(0);
	}
	
	private Symbol jassSymbol(int type) {
		return jassSymbol(type, null);
	}

	private Symbol jassSymbol(int type, Object value) {
		if (yyline > currentLine) {
			lineStartOffsets.set(yyline, yychar);
			currentLine = yyline;
		}
		return new Symbol(type, yychar, yychar+yylength(), value);
	}
	
	// a symbol at which point the number of parantheses must be balanced
	private Symbol symbolP(int type) {
		if (numberOfParantheses > 0) {
			return symbol(JurstTokenType.error, "missing closing parantheses");
		} else {
			return symbol(type, null);
		}
	}
	
	private Symbol symbol(int type) {
		return symbol(type, null);
	}

	private Symbol symbol(int type, Object value) {
		if (yyline > currentLine) {
			lineStartOffsets.set(yyline, yychar);
			currentLine = yyline;
		}
		Symbol s = new Symbol(type, yychar, yychar+yylength(), value);
		if (isStart) {
			if (spaceErrorStart >= 0) {
				errors.add(new ScannerError(spaceErrorStart, yychar, "Spaces are not allowed for indentation. Use tabs."));
			}
			isStart = false;
			if (indentationLevels.peek() > currentLineWhiteSpace) {
				returnStack.push(s);
				while (indentationLevels.peek() > currentLineWhiteSpace) {
					indentationLevels.pop();
					returnStack.push(new Symbol(JurstTokenType.UNINDENT, yychar-1, yychar));
				}
				
				if (indentationLevels.peek() < currentLineWhiteSpace) {
					returnStack.push(new Symbol(JurstTokenType.CUSTOM_ERROR, yychar-1, yychar, "Level of indentation does not align with previous lines."));
				}
				
				
				return returnStack.pop();
			} else if (indentationLevels.peek() < currentLineWhiteSpace) {
				returnStack.push(s);
				if (currentLineWhiteSpace - indentationLevels.peek() < 2) {
					// indent with at least two spaces
					return new Symbol(JurstTokenType.CUSTOM_ERROR, yychar-1, yychar, "Indentation difference too small.");
				}
				
				indentationLevels.push(currentLineWhiteSpace);
				
				return new Symbol(JurstTokenType.INDENT, yychar-1, yychar);
			}
		}
		return s;
	}
%}

DIGIT = [0-9]
LETTER = [a-zA-Z]
NEWLINE = \n | \r | \r\n 
WHITESPACE = [ \t]
IDENT = ({LETTER}|_)({LETTER}|{DIGIT}|_)* 

%state STRING
%state WURST

%%

<YYINITIAL> { // jass code
	{WHITESPACE} 	{
						// ignore					
					}
	{NEWLINE}							
					{ 
						return jassSymbol(JurstTokenType.NL);
					}	
	"//" [^\r\n]* 			           { }
	"/*" ~"*/"                        { }
	
	
	// end
	"end"                           	{ return symbolP(JurstTokenType.END); }
	// compatibility ends:
	"endif"|"endfunction"|"endloop"|"endstruct"
	|"endmethod"|"endstruct"    	
		{ return symbolP(JurstTokenType.END); }
	
		// classes
	"class"|"struct"                  	{ return symbolP(JurstTokenType.CLASS); }
	"construct"							{ return symbolP(JurstTokenType.CONSTRUCT); }
	"ondestroy"							{ return symbolP(JurstTokenType.ONDESTROY); }
	
	
	// tuples
	"tuple"								{ return symbolP(JurstTokenType.TUPLE); }

	// packages
	"package"|"library"|"scope"			{ return symbolP(JurstTokenType.PACKAGE); }
	"endpackage"|"endlibrary"|"endscope"	{ return symbolP(JurstTokenType.ENDPACKAGE); }
	"import"							{ return symbolP(JurstTokenType.IMPORT); }	
	"initlater"							{ return symbolP(JurstTokenType.INITLATER); }
	"requires"|"uses"|"needs"			{ return symbolP(JurstTokenType.REQUIRES); }
	"init"								{ return symbolP(JurstTokenType.INIT); }
	
	
	// modules
		"module"							{ return symbolP(JurstTokenType.MODULE); }
		"use"								{ return symbolP(JurstTokenType.USE); }
	
	// functions
	"function"|"method"					{ return symbol(JurstTokenType.FUNCTION); }
	"takes"								{ return symbolP(JurstTokenType.TAKES); }
	"returns"							{ return symbolP(JurstTokenType.RETURNS); }
	"native"							{ return symbolP(JurstTokenType.NATIVE); }
	
	
	"nativetype"						{ return symbolP(JurstTokenType.NATIVETYPE); }
	
	// interfaces
	"extends"							{ return symbolP(JurstTokenType.EXTENDS); }
	"interface"							{ return symbolP(JurstTokenType.INTERFACE); }
	"implements"						{ return symbolP(JurstTokenType.IMPLEMENTS); }
	"instance"							{ return symbolP(JurstTokenType.INSTANCE); }
	"super"								{ return symbolP(JurstTokenType.SUPER); }
	
	// enums
	"enum"								{ return symbolP(JurstTokenType.ENUM); } 
	
	// switch
	"switch"							{ return symbolP(JurstTokenType.SWITCH); } 
	"case"								{ return symbolP(JurstTokenType.CASE); } 
	"default"							{ return symbolP(JurstTokenType.DEFAULT); } 
	
	// modifiers
	"abstract"							{ return symbolP(JurstTokenType.ABSTRACT); }
	"static"							{ return symbolP(JurstTokenType.STATIC); }
	"override"							{ return symbolP(JurstTokenType.OVERRIDE); }
	"immutable"							{ return symbolP(JurstTokenType.IMMUTABLE); }
	"public"							{ return symbolP(JurstTokenType.PUBLIC); }
	"publicread"						{ return symbolP(JurstTokenType.PUBLICREAD); }
	"private"							{ return symbolP(JurstTokenType.PRIVATE); }
	"protected"							{ return symbolP(JurstTokenType.PROTECTED); }
	"constant"							{ return symbol(JurstTokenType.CONSTANT); }

	//statements
	"loop"								{ return symbolP(JurstTokenType.LOOP); }
	"skip"								{ return symbolP(JurstTokenType.SKIP); }
	"let"								{ return symbolP(JurstTokenType.LET); }
	"var"								{ return symbolP(JurstTokenType.VAR); }
	"local"								{ return symbolP(JurstTokenType.LOCAL); }
	"return"                          	{ return symbolP(JurstTokenType.RETURN); }
	"if"                              	{ return symbolP(JurstTokenType.IF); }
	"then"                             	{ return symbolP(JurstTokenType.THEN); }
	"else"                            	{ return symbolP(JurstTokenType.ELSE); }
	"elseif"                           	{ return symbolP(JurstTokenType.ELSEIF); }
	"while"                           	{ return symbolP(JurstTokenType.WHILE); }
	"destroy"							{ return symbol(JurstTokenType.DESTROY); }
	"for"                           	{ return symbolP(JurstTokenType.FOR); }
	"in"                           		{ return symbol(JurstTokenType.IN); }
	"from"                         		{ return symbol(JurstTokenType.FROM); }
	"to"                           		{ return symbol(JurstTokenType.TO); }
	"downto"                           	{ return symbol(JurstTokenType.DOWNTO); }
	"step"                           	{ return symbol(JurstTokenType.STEP); }
	"break"                        		{ return symbolP(JurstTokenType.BREAK); }
	"call"                        		{ return symbolP(JurstTokenType.CALL); }
	"set"                        		{ return symbolP(JurstTokenType.SET); }
	
	
	// special vars/constants
	"it"								{ return symbol(JurstTokenType.IT); }
	"thistype"							{ return symbol(JurstTokenType.THISTYPE); }
	"this"								{ return symbol(JurstTokenType.THIS); }
	"null"                            	{ return symbol(JurstTokenType.NULL); }
	"true"                            { return symbol(JurstTokenType.TRUE); }
	"false"                           { return symbol(JurstTokenType.FALSE); }
	
	// types
	"array"								{ return symbol(JurstTokenType.ARRAY); }
	"nothing"								{ return symbol(JurstTokenType.NOTHING); }
	
	// operators
	"instanceof"						{ return symbol(JurstTokenType.INSTANCEOF); }
	"and"								{ return symbol(JurstTokenType.AND); }
	"or"								{ return symbol(JurstTokenType.OR); }
	"not"								{ return symbol(JurstTokenType.NOT); }
	"new"                             	{ return symbol(JurstTokenType.NEW); }
	"castTo"							{ return symbol(JurstTokenType.CASTTO); }
	"div"                               { return symbol(JurstTokenType.DIV_INT); }
	"mod"                               { return symbol(JurstTokenType.MOD_INT); } 
	"exitwhen"							{ return symbol(JurstTokenType.EXITWHEN); }
	
	
	"begin"							{ return symbol(JurstTokenType.BEGIN); }
	
	
	
	"->"								{ return symbol(JurstTokenType.ARROW); }
	
	"("                               { numberOfParantheses++; return symbol(JurstTokenType.LPAR); }
	")"                               { numberOfParantheses--; return symbol(JurstTokenType.RPAR); }
	";"									{ return symbol(JurstTokenType.SEMICOLON); }
	":"									{ return symbol(JurstTokenType.COLON); }
	","                               { return symbol(JurstTokenType.COMMA); }
	"{"                               { return symbol(JurstTokenType.LBRACK); }
	"}"                               { return symbol(JurstTokenType.RBRACK); }
	"["                               { return symbol(JurstTokenType.LSQUARE); }
	"]"                               { return symbol(JurstTokenType.RSQUARE); }
	"+"                               { return symbol(JurstTokenType.PLUS); }
	"-"                               { return symbol(JurstTokenType.MINUS); }
	"*"                               { return symbol(JurstTokenType.MULT); }
	"/"                               { return symbol(JurstTokenType.DIV_REAL); }
	"%"                               { return symbol(JurstTokenType.MOD_REAL); }
	"=="                              { return symbol(JurstTokenType.EQEQ); }
	"!="                              { return symbol(JurstTokenType.NOTEQ); }
	">="                              { return symbol(JurstTokenType.GTEQ); }
	"<="                              { return symbol(JurstTokenType.LTEQ); }
	"<"                              { return symbol(JurstTokenType.LT); }
	">"                              { return symbol(JurstTokenType.GT); }
	"="                               { return symbol(JurstTokenType.EQ); }
	"+="                               { return symbol(JurstTokenType.PLUS_EQ); }
	"-="                               { return symbol(JurstTokenType.MINUS_EQ); }
	"*="                               { return symbol(JurstTokenType.MULT_EQ); }
	"/="                               { return symbol(JurstTokenType.DIV_EQ); }
	"++"                               { return symbol(JurstTokenType.PLUS_PLUS); }
	"--"                               { return symbol(JurstTokenType.MINUS_MINUS); }
	"-->"                              { return symbol(JurstTokenType.ARROW); }
	{DIGIT}+                          { return symbol(JurstTokenType.INTEGER_LITERAL, yytext()); }
	"0x" [0-9a-fA-F]+                 { return symbol(JurstTokenType.INTEGER_LITERAL, yytext()); }
	"'" . "'"						  { return symbol(JurstTokenType.INTEGER_LITERAL, yytext()); }
	"'" . . . . "'"					  { return symbol(JurstTokenType.INTEGER_LITERAL, yytext()); }
	{DIGIT}+ "." {DIGIT}*			  { return symbol(JurstTokenType.REAL_LITERAL, yytext()); }
	[ \t\n\r]* "." {DIGIT}+			 { return symbol(JurstTokenType.REAL_LITERAL, yytext()); }
	[ \t\n\r]* "."                    { return symbol(JurstTokenType.DOT); }
	[ \t\n\r]* ".."                    { return symbol(JurstTokenType.DOTDOT); }  
	"wurst__" {IDENT}				  { return symbol(JurstTokenType.IDENTIFIER, "w" + yytext()); }
	"@" {IDENT}							{ return symbol(JurstTokenType.ANNOTATION, yytext()); }
	{IDENT}                           { return symbol(JurstTokenType.IDENTIFIER, yytext()); }
	{IDENT} "<"                          { return symbol(JurstTokenType.IDENTIFIER_LT, yytext().substring(0, yylength()-1)); }
	[\"]                             		{ string.setLength(0); yybegin(STRING); }
	// error fallback:
	.                              { return symbol(JurstTokenType.error, yytext()); }
}


<STRING> {
  [\"]                             { yybegin(YYINITIAL); 
                                   Symbol s = symbol(JurstTokenType.STRING_LITERAL, string.toString());
                                   // correct string position
                                   s.left = s.right - string.length();
                                   return s; }
	{NEWLINE}			{ yybegin(YYINITIAL); 
								return symbol(JurstTokenType.CUSTOM_ERROR, "unterminated String"); }
  [^\n\r\"\\]+                   { string.append( yytext() ); }

  \\r                            { string.append('\r'); }
  \\n                            { string.append('\n'); }
  \\t                            { string.append('\t'); }
  \\\"                           { string.append('\"'); }
  \\\\                             { string.append('\\'); }
  //error fallback:
  .                                 { return symbol(JurstTokenType.error, yytext()); }
}

//error fallback:
.                                 { return symbol(JurstTokenType.error, yytext()); }