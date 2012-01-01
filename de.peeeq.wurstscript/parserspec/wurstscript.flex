package de.peeeq.wurstscript.parser;

import java.util.Stack;

import java_cup.runtime.Symbol;
import de.peeeq.wurstscript.utils.LineOffsets;
import de.peeeq.wurstscript.utils.Utils;



%%

%class WurstScriptScannerIntern
%unicode
%line
//%public
%column
%char
%eofval{
  return symbol(TokenType.EOF);
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
	int mode = 0; // 0: unknown mode, 1: space mode, 2: tab mode
	boolean inPackage = false; // are we inside a package? -> wurstmode on
	
	LineOffsets lineStartOffsets = new LineOffsets(); 
	int currentLine = -1;
	
	int numberOfParantheses = 0;

	{
		indentationLevels.push(0);
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
		if (inPackage) {
			if (type == TokenType.LPAR) {
				numberOfParantheses++;
			} else if (type == TokenType.RPAR) {
				numberOfParantheses--;
			} else if (type == TokenType.NL) {
				isStart = true;
				currentLineWhiteSpace = 0;
			} else if (type == TokenType.ENDPACKAGE) {
				inPackage = false; 
				mode = 0; 
			} 
			if (isStart) {
				isStart = false;
				if (indentationLevels.peek() > currentLineWhiteSpace) {
					returnStack.push(s);
					while (indentationLevels.peek() > currentLineWhiteSpace) {
						indentationLevels.pop();
						returnStack.push(new Symbol(TokenType.UNINDENT, yychar-1, yychar));
					}
					
					if (indentationLevels.peek() < currentLineWhiteSpace) {
						returnStack.push(new Symbol(TokenType.CUSTOM_ERROR, yychar-1, yychar, "Level of indentation does not align with previous lines."));
					}
					
					
					return returnStack.pop();
				} else if (indentationLevels.peek() < currentLineWhiteSpace) {
					returnStack.push(s);
					if (currentLineWhiteSpace - indentationLevels.peek() < 2) {
						// indent with at least two spaces
						return new Symbol(TokenType.CUSTOM_ERROR, yychar-1, yychar, "Indentation difference too small.");
					}
					
					indentationLevels.push(currentLineWhiteSpace);
					
					return new Symbol(TokenType.INDENT, yychar-1, yychar);
				}
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

%%

<YYINITIAL> {
	[\t]                    { 
								if (inPackage && isStart) {
									currentLineWhiteSpace += 4;
									if (mode == 2) {
										returnStack.push(new Symbol(TokenType.CUSTOM_ERROR, yychar-1, yychar, "Mixing tabs and spaces is not allowed."));
									}
									mode = 1;
								}
							}
	[ ]						{ 
								if (inPackage && isStart) {	
									currentLineWhiteSpace += 1; 
									if (mode == 1) {
										returnStack.push(new Symbol(TokenType.CUSTOM_ERROR, yychar-1, yychar, "Mixing spaces and tabs is not allowed."));
									}
									mode = 2;
								}
							}
	{NEWLINE}							
					{ 
						if (inPackage) {
							if (numberOfParantheses <= 0) {
								numberOfParantheses = 0;
								currentLineWhiteSpace = 0;
								if (!isStart) {
									isStart = true;
									return new Symbol(TokenType.NL, yychar-1, yychar);
								} else {
									return null;
								}
							} // else: ignore newlines inside parantheses
						} else {
							return new Symbol(TokenType.NL, yychar-1, yychar);
						}
					}	
	"//" [^\r\n]* 			           { }
	"/*" ~"*/"                        { }
	";"									{ return symbol(TokenType.SEMICOLON); }
	"class"                           	{ return symbol(TokenType.CLASS); }
	"return"                          	{ return symbol(TokenType.RETURN); }
	"if"                              	{ return symbol(TokenType.IF); }
	"else"                            	{ return symbol(TokenType.ELSE); }
	"while"                           	{ return symbol(TokenType.WHILE); }
	"for"                           	{ return symbol(TokenType.FOR); }
	"in"                           		{ return symbol(TokenType.IN); }
	"break"                        		{ return symbol(TokenType.BREAK); }
	"new"                             	{ return symbol(TokenType.NEW); }
	"null"                            	{ return symbol(TokenType.NULL); }
	"package"							{ inPackage = true; return symbol(TokenType.PACKAGE); }
	"endpackage"						{ return symbol(TokenType.ENDPACKAGE); }
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
	"override"							{ return symbol(TokenType.OVERRIDE); }
	"immutable"							{ return symbol(TokenType.IMMUTABLE); }
	"it"								{ return symbol(TokenType.IT); }
	
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
	"true"                            { return symbol(TokenType.TRUE); }
	"false"                           { return symbol(TokenType.FALSE); }
	"div"                               { return symbol(TokenType.DIV_INT); }
	"mod"                               { return symbol(TokenType.MOD_INT); } 
	"("                               { return symbol(TokenType.LPAR); }
	")"                               { return symbol(TokenType.RPAR); }
	","                               { return symbol(TokenType.COMMA); }
	"{"                               { return symbol(TokenType.LBRACK); }
	"}"                               { return symbol(TokenType.RBRACK); }
	"["                               { return symbol(TokenType.LSQUARE); }
	"]"                               { return symbol(TokenType.RSQUARE); }
	"+"                               { return symbol(TokenType.PLUS); }
	"-"                               { return symbol(TokenType.MINUS); }
	"*"                               { return symbol(TokenType.MULT); }
	"/"                               { return symbol(TokenType.DIV_REAL); }
	"%"                               { return symbol(TokenType.MOD_REAL); }
	"=="                              { return symbol(TokenType.EQEQ); }
	"!="                              { return symbol(TokenType.NOTEQ); }
	">="                              { return symbol(TokenType.GTEQ); }
	"<="                              { return symbol(TokenType.LTEQ); }
	"<"                              { return symbol(TokenType.LT); }
	">"                              { return symbol(TokenType.GT); }
	"="                               { return symbol(TokenType.EQ); }
	"+="                               { return symbol(TokenType.PLUS_EQ); }
	"-="                               { return symbol(TokenType.MINUS_EQ); }
	"*="                               { return symbol(TokenType.MULT_EQ); }
	"++"                               { return symbol(TokenType.PLUS_PLUS); }
	"--"                               { return symbol(TokenType.MINUS_MINUS); }
	"-->"                              { return symbol(TokenType.ARROW); }
	{DIGIT}+                          { return symbol(TokenType.INTEGER_LITERAL, Utils.parseInt(yytext())); }
	"0x" [0-9a-fA-F]+                          { return symbol(TokenType.INTEGER_LITERAL, Utils.parseHexInt(yytext())); }
	"'" . "'"						  { return symbol(TokenType.INTEGER_LITERAL, Utils.parseAsciiInt1(yytext())); }
	"'" . . . . "'"					{ return symbol(TokenType.INTEGER_LITERAL, Utils.parseAsciiInt4(yytext())); }
	{DIGIT}+ "." {DIGIT}*			  { return symbol(TokenType.REAL_LITERAL, Double.parseDouble(yytext())); }
	[ \t\n\r]* "." {DIGIT}+			 { return symbol(TokenType.REAL_LITERAL, Double.parseDouble(yytext())); }
	[ \t\n\r]* "."                    { return symbol(TokenType.DOT); } 
	{IDENT}                           { return symbol(TokenType.IDENTIFIER, yytext()); }
	[\"]                             		{ string.setLength(0); yybegin(STRING); }
	// error fallback:
	.                              { return symbol(TokenType.error, yytext()); }
}


<STRING> {
  [\"]                             { yybegin(YYINITIAL); 
                                   return symbol(TokenType.STRING_LITERAL, string.toString()); }
	{NEWLINE}			{ yybegin(YYINITIAL); 
								return symbol(TokenType.CUSTOM_ERROR, "unterminated String"); }
  [^\n\r\"\\]+                   { string.append( yytext() ); }

  \\r                            { string.append('\r'); }
  \\\"                           { string.append('\"'); }
  \\                             { string.append('\\'); }
  
}

//error fallback:
.                                 { return symbol(TokenType.error, yytext()); }