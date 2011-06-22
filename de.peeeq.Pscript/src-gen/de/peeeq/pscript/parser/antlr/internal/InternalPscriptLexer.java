package de.peeeq.pscript.parser.antlr.internal;

// Hack: Use our own Lexer superclass by means of import. 
// Currently there is no other way to specify the superclass for the lexer.
import org.eclipse.xtext.parser.antlr.Lexer;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalPscriptLexer extends Lexer {
    public static final int RULE_ID=5;
    public static final int RULE_ANY_OTHER=13;
    public static final int T29=29;
    public static final int T28=28;
    public static final int T27=27;
    public static final int T26=26;
    public static final int T25=25;
    public static final int EOF=-1;
    public static final int T24=24;
    public static final int T23=23;
    public static final int T22=22;
    public static final int T21=21;
    public static final int T20=20;
    public static final int RULE_NUMBER=8;
    public static final int RULE_INT=7;
    public static final int T38=38;
    public static final int T37=37;
    public static final int T39=39;
    public static final int T34=34;
    public static final int T33=33;
    public static final int T36=36;
    public static final int T35=35;
    public static final int T30=30;
    public static final int T32=32;
    public static final int T31=31;
    public static final int RULE_NL=4;
    public static final int T49=49;
    public static final int T48=48;
    public static final int T43=43;
    public static final int Tokens=54;
    public static final int RULE_SL_COMMENT=11;
    public static final int T42=42;
    public static final int T41=41;
    public static final int T40=40;
    public static final int T47=47;
    public static final int T46=46;
    public static final int T45=45;
    public static final int RULE_ML_COMMENT=10;
    public static final int T44=44;
    public static final int RULE_STRING=9;
    public static final int T50=50;
    public static final int T14=14;
    public static final int RULE_OPERATOR=6;
    public static final int T52=52;
    public static final int T15=15;
    public static final int RULE_WS=12;
    public static final int T51=51;
    public static final int T16=16;
    public static final int T17=17;
    public static final int T53=53;
    public static final int T18=18;
    public static final int T19=19;
    public InternalPscriptLexer() {;} 
    public InternalPscriptLexer(CharStream input) {
        super(input);
    }
    public String getGrammarFileName() { return "../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g"; }

    // $ANTLR start T14
    public final void mT14() throws RecognitionException {
        try {
            int _type = T14;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:10:5: ( 'package' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:10:7: 'package'
            {
            match("package"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T14

    // $ANTLR start T15
    public final void mT15() throws RecognitionException {
        try {
            int _type = T15;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:11:5: ( '{' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:11:7: '{'
            {
            match('{'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T15

    // $ANTLR start T16
    public final void mT16() throws RecognitionException {
        try {
            int _type = T16;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:12:5: ( '}' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:12:7: '}'
            {
            match('}'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T16

    // $ANTLR start T17
    public final void mT17() throws RecognitionException {
        try {
            int _type = T17;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:13:5: ( 'import' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:13:7: 'import'
            {
            match("import"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T17

    // $ANTLR start T18
    public final void mT18() throws RecognitionException {
        try {
            int _type = T18;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:14:5: ( '.' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:14:7: '.'
            {
            match('.'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T18

    // $ANTLR start T19
    public final void mT19() throws RecognitionException {
        try {
            int _type = T19;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:15:5: ( '.*' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:15:7: '.*'
            {
            match(".*"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T19

    // $ANTLR start T20
    public final void mT20() throws RecognitionException {
        try {
            int _type = T20;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:16:5: ( 'native' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:16:7: 'native'
            {
            match("native"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T20

    // $ANTLR start T21
    public final void mT21() throws RecognitionException {
        try {
            int _type = T21;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:17:5: ( 'type' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:17:7: 'type'
            {
            match("type"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T21

    // $ANTLR start T22
    public final void mT22() throws RecognitionException {
        try {
            int _type = T22;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:18:5: ( '=' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:18:7: '='
            {
            match('='); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T22

    // $ANTLR start T23
    public final void mT23() throws RecognitionException {
        try {
            int _type = T23;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:19:5: ( 'class' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:19:7: 'class'
            {
            match("class"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T23

    // $ANTLR start T24
    public final void mT24() throws RecognitionException {
        try {
            int _type = T24;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:20:5: ( 'var' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:20:7: 'var'
            {
            match("var"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T24

    // $ANTLR start T25
    public final void mT25() throws RecognitionException {
        try {
            int _type = T25;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:21:5: ( 'val' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:21:7: 'val'
            {
            match("val"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T25

    // $ANTLR start T26
    public final void mT26() throws RecognitionException {
        try {
            int _type = T26;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:22:5: ( ':' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:22:7: ':'
            {
            match(':'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T26

    // $ANTLR start T27
    public final void mT27() throws RecognitionException {
        try {
            int _type = T27;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:23:5: ( 'function' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:23:7: 'function'
            {
            match("function"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T27

    // $ANTLR start T28
    public final void mT28() throws RecognitionException {
        try {
            int _type = T28;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:24:5: ( '(' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:24:7: '('
            {
            match('('); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T28

    // $ANTLR start T29
    public final void mT29() throws RecognitionException {
        try {
            int _type = T29;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:25:5: ( ',' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:25:7: ','
            {
            match(','); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T29

    // $ANTLR start T30
    public final void mT30() throws RecognitionException {
        try {
            int _type = T30;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:26:5: ( ')' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:26:7: ')'
            {
            match(')'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T30

    // $ANTLR start T31
    public final void mT31() throws RecognitionException {
        try {
            int _type = T31;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:27:5: ( 'return' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:27:7: 'return'
            {
            match("return"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T31

    // $ANTLR start T32
    public final void mT32() throws RecognitionException {
        try {
            int _type = T32;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:28:5: ( 'if' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:28:7: 'if'
            {
            match("if"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T32

    // $ANTLR start T33
    public final void mT33() throws RecognitionException {
        try {
            int _type = T33;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:29:5: ( 'else' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:29:7: 'else'
            {
            match("else"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T33

    // $ANTLR start T34
    public final void mT34() throws RecognitionException {
        try {
            int _type = T34;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:30:5: ( 'while' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:30:7: 'while'
            {
            match("while"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T34

    // $ANTLR start T35
    public final void mT35() throws RecognitionException {
        try {
            int _type = T35;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:31:5: ( '+=' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:31:7: '+='
            {
            match("+="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T35

    // $ANTLR start T36
    public final void mT36() throws RecognitionException {
        try {
            int _type = T36;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:32:5: ( '-=' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:32:7: '-='
            {
            match("-="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T36

    // $ANTLR start T37
    public final void mT37() throws RecognitionException {
        try {
            int _type = T37;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:33:5: ( 'or' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:33:7: 'or'
            {
            match("or"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T37

    // $ANTLR start T38
    public final void mT38() throws RecognitionException {
        try {
            int _type = T38;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:34:5: ( 'and' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:34:7: 'and'
            {
            match("and"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T38

    // $ANTLR start T39
    public final void mT39() throws RecognitionException {
        try {
            int _type = T39;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:35:5: ( '!=' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:35:7: '!='
            {
            match("!="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T39

    // $ANTLR start T40
    public final void mT40() throws RecognitionException {
        try {
            int _type = T40;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:36:5: ( '==' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:36:7: '=='
            {
            match("=="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T40

    // $ANTLR start T41
    public final void mT41() throws RecognitionException {
        try {
            int _type = T41;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:37:5: ( '<=' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:37:7: '<='
            {
            match("<="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T41

    // $ANTLR start T42
    public final void mT42() throws RecognitionException {
        try {
            int _type = T42;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:38:5: ( '<' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:38:7: '<'
            {
            match('<'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T42

    // $ANTLR start T43
    public final void mT43() throws RecognitionException {
        try {
            int _type = T43;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:39:5: ( '>=' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:39:7: '>='
            {
            match(">="); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T43

    // $ANTLR start T44
    public final void mT44() throws RecognitionException {
        try {
            int _type = T44;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:40:5: ( '>' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:40:7: '>'
            {
            match('>'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T44

    // $ANTLR start T45
    public final void mT45() throws RecognitionException {
        try {
            int _type = T45;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:41:5: ( '+' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:41:7: '+'
            {
            match('+'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T45

    // $ANTLR start T46
    public final void mT46() throws RecognitionException {
        try {
            int _type = T46;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:42:5: ( '-' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:42:7: '-'
            {
            match('-'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T46

    // $ANTLR start T47
    public final void mT47() throws RecognitionException {
        try {
            int _type = T47;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:43:5: ( '*' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:43:7: '*'
            {
            match('*'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T47

    // $ANTLR start T48
    public final void mT48() throws RecognitionException {
        try {
            int _type = T48;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:44:5: ( '/' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:44:7: '/'
            {
            match('/'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T48

    // $ANTLR start T49
    public final void mT49() throws RecognitionException {
        try {
            int _type = T49;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:45:5: ( '%' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:45:7: '%'
            {
            match('%'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T49

    // $ANTLR start T50
    public final void mT50() throws RecognitionException {
        try {
            int _type = T50;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:46:5: ( 'mod' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:46:7: 'mod'
            {
            match("mod"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T50

    // $ANTLR start T51
    public final void mT51() throws RecognitionException {
        try {
            int _type = T51;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:47:5: ( 'div' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:47:7: 'div'
            {
            match("div"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T51

    // $ANTLR start T52
    public final void mT52() throws RecognitionException {
        try {
            int _type = T52;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:48:5: ( 'not' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:48:7: 'not'
            {
            match("not"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T52

    // $ANTLR start T53
    public final void mT53() throws RecognitionException {
        try {
            int _type = T53;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:49:5: ( 'buildin' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:49:7: 'buildin'
            {
            match("buildin"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T53

    // $ANTLR start RULE_ID
    public final void mRULE_ID() throws RecognitionException {
        try {
            int _type = RULE_ID;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3472:9: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )* )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3472:11: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3472:35: ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RULE_ID

    // $ANTLR start RULE_INT
    public final void mRULE_INT() throws RecognitionException {
        try {
            int _type = RULE_INT;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3474:10: ( ( '0' .. '9' )+ )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3474:12: ( '0' .. '9' )+
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3474:12: ( '0' .. '9' )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='0' && LA2_0<='9')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3474:13: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RULE_INT

    // $ANTLR start RULE_NUMBER
    public final void mRULE_NUMBER() throws RecognitionException {
        try {
            int _type = RULE_NUMBER;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3476:13: ( ( '0..9' )+ '.' ( '0..9' )* )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3476:15: ( '0..9' )+ '.' ( '0..9' )*
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3476:15: ( '0..9' )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='0') ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3476:15: '0..9'
            	    {
            	    match("0..9"); 


            	    }
            	    break;

            	default :
            	    if ( cnt3 >= 1 ) break loop3;
                        EarlyExitException eee =
                            new EarlyExitException(3, input);
                        throw eee;
                }
                cnt3++;
            } while (true);

            match('.'); 
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3476:27: ( '0..9' )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0=='0') ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3476:27: '0..9'
            	    {
            	    match("0..9"); 


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RULE_NUMBER

    // $ANTLR start RULE_STRING
    public final void mRULE_STRING() throws RecognitionException {
        try {
            int _type = RULE_STRING;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3478:13: ( ( '\"' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )* '\"' | '\\'' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\\'' ) ) )* '\\'' ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3478:15: ( '\"' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )* '\"' | '\\'' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\\'' ) ) )* '\\'' )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3478:15: ( '\"' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )* '\"' | '\\'' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\\'' ) ) )* '\\'' )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='\"') ) {
                alt7=1;
            }
            else if ( (LA7_0=='\'') ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("3478:15: ( '\"' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )* '\"' | '\\'' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\\'' ) ) )* '\\'' )", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3478:16: '\"' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )* '\"'
                    {
                    match('\"'); 
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3478:20: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )*
                    loop5:
                    do {
                        int alt5=3;
                        int LA5_0 = input.LA(1);

                        if ( (LA5_0=='\\') ) {
                            alt5=1;
                        }
                        else if ( ((LA5_0>='\u0000' && LA5_0<='!')||(LA5_0>='#' && LA5_0<='[')||(LA5_0>=']' && LA5_0<='\uFFFE')) ) {
                            alt5=2;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3478:21: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' )
                    	    {
                    	    match('\\'); 
                    	    if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t' ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse =
                    	            new MismatchedSetException(null,input);
                    	        recover(mse);    throw mse;
                    	    }


                    	    }
                    	    break;
                    	case 2 :
                    	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3478:62: ~ ( ( '\\\\' | '\"' ) )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFE') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse =
                    	            new MismatchedSetException(null,input);
                    	        recover(mse);    throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop5;
                        }
                    } while (true);

                    match('\"'); 

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3478:82: '\\'' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\\'' ) ) )* '\\''
                    {
                    match('\''); 
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3478:87: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\\'' ) ) )*
                    loop6:
                    do {
                        int alt6=3;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0=='\\') ) {
                            alt6=1;
                        }
                        else if ( ((LA6_0>='\u0000' && LA6_0<='&')||(LA6_0>='(' && LA6_0<='[')||(LA6_0>=']' && LA6_0<='\uFFFE')) ) {
                            alt6=2;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3478:88: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' )
                    	    {
                    	    match('\\'); 
                    	    if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t' ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse =
                    	            new MismatchedSetException(null,input);
                    	        recover(mse);    throw mse;
                    	    }


                    	    }
                    	    break;
                    	case 2 :
                    	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3478:129: ~ ( ( '\\\\' | '\\'' ) )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFE') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse =
                    	            new MismatchedSetException(null,input);
                    	        recover(mse);    throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop6;
                        }
                    } while (true);

                    match('\''); 

                    }
                    break;

            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RULE_STRING

    // $ANTLR start RULE_ML_COMMENT
    public final void mRULE_ML_COMMENT() throws RecognitionException {
        try {
            int _type = RULE_ML_COMMENT;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3480:17: ( '/*' ( options {greedy=false; } : . )* '*/' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3480:19: '/*' ( options {greedy=false; } : . )* '*/'
            {
            match("/*"); 

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3480:24: ( options {greedy=false; } : . )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0=='*') ) {
                    int LA8_1 = input.LA(2);

                    if ( (LA8_1=='/') ) {
                        alt8=2;
                    }
                    else if ( ((LA8_1>='\u0000' && LA8_1<='.')||(LA8_1>='0' && LA8_1<='\uFFFE')) ) {
                        alt8=1;
                    }


                }
                else if ( ((LA8_0>='\u0000' && LA8_0<=')')||(LA8_0>='+' && LA8_0<='\uFFFE')) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3480:52: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            match("*/"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RULE_ML_COMMENT

    // $ANTLR start RULE_SL_COMMENT
    public final void mRULE_SL_COMMENT() throws RecognitionException {
        try {
            int _type = RULE_SL_COMMENT;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3482:17: ( '//' (~ ( ( '\\n' | '\\r' ) ) )* ( ( '\\r' )? '\\n' )? )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3482:19: '//' (~ ( ( '\\n' | '\\r' ) ) )* ( ( '\\r' )? '\\n' )?
            {
            match("//"); 

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3482:24: (~ ( ( '\\n' | '\\r' ) ) )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( ((LA9_0>='\u0000' && LA9_0<='\t')||(LA9_0>='\u000B' && LA9_0<='\f')||(LA9_0>='\u000E' && LA9_0<='\uFFFE')) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3482:24: ~ ( ( '\\n' | '\\r' ) )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFE') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3482:40: ( ( '\\r' )? '\\n' )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0=='\n'||LA11_0=='\r') ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3482:41: ( '\\r' )? '\\n'
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3482:41: ( '\\r' )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0=='\r') ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3482:41: '\\r'
                            {
                            match('\r'); 

                            }
                            break;

                    }

                    match('\n'); 

                    }
                    break;

            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RULE_SL_COMMENT

    // $ANTLR start RULE_WS
    public final void mRULE_WS() throws RecognitionException {
        try {
            int _type = RULE_WS;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3484:9: ( ( ' ' | '\\t' )+ )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3484:11: ( ' ' | '\\t' )+
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3484:11: ( ' ' | '\\t' )+
            int cnt12=0;
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0=='\t'||LA12_0==' ') ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:
            	    {
            	    if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt12 >= 1 ) break loop12;
                        EarlyExitException eee =
                            new EarlyExitException(12, input);
                        throw eee;
                }
                cnt12++;
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RULE_WS

    // $ANTLR start RULE_NL
    public final void mRULE_NL() throws RecognitionException {
        try {
            int _type = RULE_NL;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3486:9: ( ( '\\n\\r' | '\\n' | '\\r' | '\\r\\n' ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3486:11: ( '\\n\\r' | '\\n' | '\\r' | '\\r\\n' )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3486:11: ( '\\n\\r' | '\\n' | '\\r' | '\\r\\n' )
            int alt13=4;
            int LA13_0 = input.LA(1);

            if ( (LA13_0=='\n') ) {
                int LA13_1 = input.LA(2);

                if ( (LA13_1=='\r') ) {
                    alt13=1;
                }
                else {
                    alt13=2;}
            }
            else if ( (LA13_0=='\r') ) {
                int LA13_2 = input.LA(2);

                if ( (LA13_2=='\n') ) {
                    alt13=4;
                }
                else {
                    alt13=3;}
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("3486:11: ( '\\n\\r' | '\\n' | '\\r' | '\\r\\n' )", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3486:12: '\\n\\r'
                    {
                    match("\n\r"); 


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3486:19: '\\n'
                    {
                    match('\n'); 

                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3486:24: '\\r'
                    {
                    match('\r'); 

                    }
                    break;
                case 4 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3486:29: '\\r\\n'
                    {
                    match("\r\n"); 


                    }
                    break;

            }


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RULE_NL

    // $ANTLR start RULE_OPERATOR
    public final void mRULE_OPERATOR() throws RecognitionException {
        try {
            int _type = RULE_OPERATOR;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3488:15: ( ( '+' | '-' | '*' | '/' | '%' | '$' | '<' | '>' | '=' | '~' | '!' | '^' | '|' | '&' | ':' )+ )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3488:17: ( '+' | '-' | '*' | '/' | '%' | '$' | '<' | '>' | '=' | '~' | '!' | '^' | '|' | '&' | ':' )+
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3488:17: ( '+' | '-' | '*' | '/' | '%' | '$' | '<' | '>' | '=' | '~' | '!' | '^' | '|' | '&' | ':' )+
            int cnt14=0;
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0=='!'||(LA14_0>='$' && LA14_0<='&')||(LA14_0>='*' && LA14_0<='+')||LA14_0=='-'||LA14_0=='/'||LA14_0==':'||(LA14_0>='<' && LA14_0<='>')||LA14_0=='^'||LA14_0=='|'||LA14_0=='~') ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:
            	    {
            	    if ( input.LA(1)=='!'||(input.LA(1)>='$' && input.LA(1)<='&')||(input.LA(1)>='*' && input.LA(1)<='+')||input.LA(1)=='-'||input.LA(1)=='/'||input.LA(1)==':'||(input.LA(1)>='<' && input.LA(1)<='>')||input.LA(1)=='^'||input.LA(1)=='|'||input.LA(1)=='~' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt14 >= 1 ) break loop14;
                        EarlyExitException eee =
                            new EarlyExitException(14, input);
                        throw eee;
                }
                cnt14++;
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RULE_OPERATOR

    // $ANTLR start RULE_ANY_OTHER
    public final void mRULE_ANY_OTHER() throws RecognitionException {
        try {
            int _type = RULE_ANY_OTHER;
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3490:16: ( . )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3490:18: .
            {
            matchAny(); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RULE_ANY_OTHER

    public void mTokens() throws RecognitionException {
        // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:8: ( T14 | T15 | T16 | T17 | T18 | T19 | T20 | T21 | T22 | T23 | T24 | T25 | T26 | T27 | T28 | T29 | T30 | T31 | T32 | T33 | T34 | T35 | T36 | T37 | T38 | T39 | T40 | T41 | T42 | T43 | T44 | T45 | T46 | T47 | T48 | T49 | T50 | T51 | T52 | T53 | RULE_ID | RULE_INT | RULE_NUMBER | RULE_STRING | RULE_ML_COMMENT | RULE_SL_COMMENT | RULE_WS | RULE_NL | RULE_OPERATOR | RULE_ANY_OTHER )
        int alt15=50;
        alt15 = dfa15.predict(input);
        switch (alt15) {
            case 1 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:10: T14
                {
                mT14(); 

                }
                break;
            case 2 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:14: T15
                {
                mT15(); 

                }
                break;
            case 3 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:18: T16
                {
                mT16(); 

                }
                break;
            case 4 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:22: T17
                {
                mT17(); 

                }
                break;
            case 5 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:26: T18
                {
                mT18(); 

                }
                break;
            case 6 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:30: T19
                {
                mT19(); 

                }
                break;
            case 7 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:34: T20
                {
                mT20(); 

                }
                break;
            case 8 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:38: T21
                {
                mT21(); 

                }
                break;
            case 9 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:42: T22
                {
                mT22(); 

                }
                break;
            case 10 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:46: T23
                {
                mT23(); 

                }
                break;
            case 11 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:50: T24
                {
                mT24(); 

                }
                break;
            case 12 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:54: T25
                {
                mT25(); 

                }
                break;
            case 13 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:58: T26
                {
                mT26(); 

                }
                break;
            case 14 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:62: T27
                {
                mT27(); 

                }
                break;
            case 15 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:66: T28
                {
                mT28(); 

                }
                break;
            case 16 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:70: T29
                {
                mT29(); 

                }
                break;
            case 17 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:74: T30
                {
                mT30(); 

                }
                break;
            case 18 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:78: T31
                {
                mT31(); 

                }
                break;
            case 19 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:82: T32
                {
                mT32(); 

                }
                break;
            case 20 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:86: T33
                {
                mT33(); 

                }
                break;
            case 21 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:90: T34
                {
                mT34(); 

                }
                break;
            case 22 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:94: T35
                {
                mT35(); 

                }
                break;
            case 23 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:98: T36
                {
                mT36(); 

                }
                break;
            case 24 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:102: T37
                {
                mT37(); 

                }
                break;
            case 25 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:106: T38
                {
                mT38(); 

                }
                break;
            case 26 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:110: T39
                {
                mT39(); 

                }
                break;
            case 27 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:114: T40
                {
                mT40(); 

                }
                break;
            case 28 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:118: T41
                {
                mT41(); 

                }
                break;
            case 29 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:122: T42
                {
                mT42(); 

                }
                break;
            case 30 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:126: T43
                {
                mT43(); 

                }
                break;
            case 31 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:130: T44
                {
                mT44(); 

                }
                break;
            case 32 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:134: T45
                {
                mT45(); 

                }
                break;
            case 33 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:138: T46
                {
                mT46(); 

                }
                break;
            case 34 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:142: T47
                {
                mT47(); 

                }
                break;
            case 35 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:146: T48
                {
                mT48(); 

                }
                break;
            case 36 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:150: T49
                {
                mT49(); 

                }
                break;
            case 37 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:154: T50
                {
                mT50(); 

                }
                break;
            case 38 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:158: T51
                {
                mT51(); 

                }
                break;
            case 39 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:162: T52
                {
                mT52(); 

                }
                break;
            case 40 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:166: T53
                {
                mT53(); 

                }
                break;
            case 41 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:170: RULE_ID
                {
                mRULE_ID(); 

                }
                break;
            case 42 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:178: RULE_INT
                {
                mRULE_INT(); 

                }
                break;
            case 43 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:187: RULE_NUMBER
                {
                mRULE_NUMBER(); 

                }
                break;
            case 44 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:199: RULE_STRING
                {
                mRULE_STRING(); 

                }
                break;
            case 45 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:211: RULE_ML_COMMENT
                {
                mRULE_ML_COMMENT(); 

                }
                break;
            case 46 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:227: RULE_SL_COMMENT
                {
                mRULE_SL_COMMENT(); 

                }
                break;
            case 47 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:243: RULE_WS
                {
                mRULE_WS(); 

                }
                break;
            case 48 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:251: RULE_NL
                {
                mRULE_NL(); 

                }
                break;
            case 49 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:259: RULE_OPERATOR
                {
                mRULE_OPERATOR(); 

                }
                break;
            case 50 :
                // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:273: RULE_ANY_OTHER
                {
                mRULE_ANY_OTHER(); 

                }
                break;

        }

    }


    protected DFA15 dfa15 = new DFA15(this);
    static final String DFA15_eotS =
        "\1\uffff\1\53\2\uffff\1\53\1\61\2\53\1\66\2\53\1\72\1\53\3\uffff"+
        "\3\53\1\103\1\105\2\53\1\67\1\112\1\114\1\115\1\120\1\121\3\53\1"+
        "\uffff\1\126\1\uffff\2\51\5\uffff\1\53\3\uffff\1\53\1\134\2\uffff"+
        "\3\53\1\140\2\uffff\2\53\1\uffff\1\53\3\uffff\3\53\1\150\1\uffff"+
        "\1\151\1\uffff\1\152\1\53\1\154\1\155\1\uffff\1\156\2\uffff\1\160"+
        "\1\67\2\uffff\3\53\5\uffff\2\53\1\uffff\1\53\1\172\1\53\1\uffff"+
        "\1\53\1\175\1\176\4\53\3\uffff\1\u0083\3\uffff\1\160\1\uffff\2\67"+
        "\1\uffff\1\u0085\1\u0086\4\53\1\uffff\1\u008b\1\53\2\uffff\2\53"+
        "\1\u008f\1\53\1\uffff\1\163\2\uffff\4\53\1\uffff\1\u0095\2\53\1"+
        "\uffff\1\u0098\2\53\1\u009b\1\u009c\1\uffff\1\53\1\u009e\1\uffff"+
        "\1\53\1\u00a0\2\uffff\1\53\1\uffff\1\u00a2\1\uffff\1\u00a3\2\uffff";
    static final String DFA15_eofS =
        "\u00a4\uffff";
    static final String DFA15_minS =
        "\1\0\1\141\2\uffff\1\146\1\52\1\141\1\171\1\41\1\154\1\141\1\41"+
        "\1\165\3\uffff\1\145\1\154\1\150\2\41\1\162\1\156\1\75\5\41\1\157"+
        "\1\151\1\165\1\uffff\1\56\1\uffff\2\0\5\uffff\1\143\3\uffff\1\160"+
        "\1\60\2\uffff\2\164\1\160\1\41\2\uffff\1\141\1\154\1\uffff\1\156"+
        "\3\uffff\1\164\1\163\1\151\1\41\1\uffff\1\41\1\uffff\1\60\1\144"+
        "\2\41\1\uffff\1\41\2\uffff\1\41\1\0\2\uffff\1\144\1\166\1\151\5"+
        "\uffff\1\153\1\157\1\uffff\1\151\1\60\1\145\1\uffff\1\163\2\60\1"+
        "\143\1\165\1\145\1\154\3\uffff\1\60\3\uffff\1\41\1\uffff\2\0\1\uffff"+
        "\2\60\1\154\1\141\1\162\1\166\1\uffff\1\60\1\163\2\uffff\1\164\1"+
        "\162\1\60\1\145\1\uffff\1\41\2\uffff\1\144\1\147\1\164\1\145\1\uffff"+
        "\1\60\1\151\1\156\1\uffff\1\60\1\151\1\145\2\60\1\uffff\1\157\1"+
        "\60\1\uffff\1\156\1\60\2\uffff\1\156\1\uffff\1\60\1\uffff\1\60\2"+
        "\uffff";
    static final String DFA15_maxS =
        "\1\ufffe\1\141\2\uffff\1\155\1\52\1\157\1\171\1\176\1\154\1\141"+
        "\1\176\1\165\3\uffff\1\145\1\154\1\150\2\176\1\162\1\156\1\75\5"+
        "\176\1\157\1\151\1\165\1\uffff\1\56\1\uffff\2\ufffe\5\uffff\1\143"+
        "\3\uffff\1\160\1\172\2\uffff\2\164\1\160\1\176\2\uffff\1\141\1\162"+
        "\1\uffff\1\156\3\uffff\1\164\1\163\1\151\1\176\1\uffff\1\176\1\uffff"+
        "\1\172\1\144\2\176\1\uffff\1\176\2\uffff\1\176\1\ufffe\2\uffff\1"+
        "\144\1\166\1\151\5\uffff\1\153\1\157\1\uffff\1\151\1\172\1\145\1"+
        "\uffff\1\163\2\172\1\143\1\165\1\145\1\154\3\uffff\1\172\3\uffff"+
        "\1\176\1\uffff\2\ufffe\1\uffff\2\172\1\154\1\141\1\162\1\166\1\uffff"+
        "\1\172\1\163\2\uffff\1\164\1\162\1\172\1\145\1\uffff\1\176\2\uffff"+
        "\1\144\1\147\1\164\1\145\1\uffff\1\172\1\151\1\156\1\uffff\1\172"+
        "\1\151\1\145\2\172\1\uffff\1\157\1\172\1\uffff\1\156\1\172\2\uffff"+
        "\1\156\1\uffff\1\172\1\uffff\1\172\2\uffff";
    static final String DFA15_acceptS =
        "\2\uffff\1\2\1\3\11\uffff\1\17\1\20\1\21\20\uffff\1\51\1\uffff\1"+
        "\52\2\uffff\1\57\2\60\1\61\1\62\1\uffff\1\51\1\2\1\3\2\uffff\1\6"+
        "\1\5\4\uffff\1\11\1\61\2\uffff\1\15\1\uffff\1\17\1\20\1\21\4\uffff"+
        "\1\40\1\uffff\1\41\4\uffff\1\35\1\uffff\1\37\1\42\2\uffff\1\43\1"+
        "\44\3\uffff\1\53\1\52\1\54\1\57\1\60\2\uffff\1\23\3\uffff\1\33\7"+
        "\uffff\1\26\1\27\1\30\1\uffff\1\32\1\34\1\36\1\uffff\1\56\2\uffff"+
        "\1\55\6\uffff\1\47\2\uffff\1\13\1\14\4\uffff\1\31\1\uffff\1\45\1"+
        "\46\4\uffff\1\10\3\uffff\1\24\5\uffff\1\12\2\uffff\1\25\2\uffff"+
        "\1\4\1\7\1\uffff\1\22\1\uffff\1\1\1\uffff\1\50\1\16";
    static final String DFA15_specialS =
        "\u00a4\uffff}>";
    static final String[] DFA15_transitionS = {
            "\11\51\1\45\1\46\2\51\1\47\22\51\1\45\1\27\1\43\1\51\1\50\1"+
            "\34\1\50\1\44\1\15\1\17\1\32\1\23\1\16\1\24\1\5\1\33\1\41\11"+
            "\42\1\13\1\51\1\30\1\10\1\31\2\51\32\40\3\51\1\50\1\40\1\51"+
            "\1\26\1\37\1\11\1\36\1\21\1\14\2\40\1\4\3\40\1\35\1\6\1\25\1"+
            "\1\1\40\1\20\1\40\1\7\1\40\1\12\1\22\3\40\1\2\1\50\1\3\1\50"+
            "\uff80\51",
            "\1\52",
            "",
            "",
            "\1\57\6\uffff\1\56",
            "\1\60",
            "\1\62\15\uffff\1\63",
            "\1\64",
            "\1\67\2\uffff\3\67\3\uffff\2\67\1\uffff\1\67\1\uffff\1\67\12"+
            "\uffff\1\67\1\uffff\1\67\1\65\1\67\37\uffff\1\67\35\uffff\1"+
            "\67\1\uffff\1\67",
            "\1\70",
            "\1\71",
            "\1\67\2\uffff\3\67\3\uffff\2\67\1\uffff\1\67\1\uffff\1\67\12"+
            "\uffff\1\67\1\uffff\3\67\37\uffff\1\67\35\uffff\1\67\1\uffff"+
            "\1\67",
            "\1\73",
            "",
            "",
            "",
            "\1\77",
            "\1\100",
            "\1\101",
            "\1\67\2\uffff\3\67\3\uffff\2\67\1\uffff\1\67\1\uffff\1\67\12"+
            "\uffff\1\67\1\uffff\1\67\1\102\1\67\37\uffff\1\67\35\uffff\1"+
            "\67\1\uffff\1\67",
            "\1\67\2\uffff\3\67\3\uffff\2\67\1\uffff\1\67\1\uffff\1\67\12"+
            "\uffff\1\67\1\uffff\1\67\1\104\1\67\37\uffff\1\67\35\uffff\1"+
            "\67\1\uffff\1\67",
            "\1\106",
            "\1\107",
            "\1\110",
            "\1\67\2\uffff\3\67\3\uffff\2\67\1\uffff\1\67\1\uffff\1\67\12"+
            "\uffff\1\67\1\uffff\1\67\1\111\1\67\37\uffff\1\67\35\uffff\1"+
            "\67\1\uffff\1\67",
            "\1\67\2\uffff\3\67\3\uffff\2\67\1\uffff\1\67\1\uffff\1\67\12"+
            "\uffff\1\67\1\uffff\1\67\1\113\1\67\37\uffff\1\67\35\uffff\1"+
            "\67\1\uffff\1\67",
            "\1\67\2\uffff\3\67\3\uffff\2\67\1\uffff\1\67\1\uffff\1\67\12"+
            "\uffff\1\67\1\uffff\3\67\37\uffff\1\67\35\uffff\1\67\1\uffff"+
            "\1\67",
            "\1\67\2\uffff\3\67\3\uffff\1\117\1\67\1\uffff\1\67\1\uffff\1"+
            "\116\12\uffff\1\67\1\uffff\3\67\37\uffff\1\67\35\uffff\1\67"+
            "\1\uffff\1\67",
            "\1\67\2\uffff\3\67\3\uffff\2\67\1\uffff\1\67\1\uffff\1\67\12"+
            "\uffff\1\67\1\uffff\3\67\37\uffff\1\67\35\uffff\1\67\1\uffff"+
            "\1\67",
            "\1\122",
            "\1\123",
            "\1\124",
            "",
            "\1\125",
            "",
            "\uffff\127",
            "\uffff\127",
            "",
            "",
            "",
            "",
            "",
            "\1\132",
            "",
            "",
            "",
            "\1\133",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "",
            "\1\135",
            "\1\136",
            "\1\137",
            "\1\67\2\uffff\3\67\3\uffff\2\67\1\uffff\1\67\1\uffff\1\67\12"+
            "\uffff\1\67\1\uffff\3\67\37\uffff\1\67\35\uffff\1\67\1\uffff"+
            "\1\67",
            "",
            "",
            "\1\141",
            "\1\143\5\uffff\1\142",
            "",
            "\1\144",
            "",
            "",
            "",
            "\1\145",
            "\1\146",
            "\1\147",
            "\1\67\2\uffff\3\67\3\uffff\2\67\1\uffff\1\67\1\uffff\1\67\12"+
            "\uffff\1\67\1\uffff\3\67\37\uffff\1\67\35\uffff\1\67\1\uffff"+
            "\1\67",
            "",
            "\1\67\2\uffff\3\67\3\uffff\2\67\1\uffff\1\67\1\uffff\1\67\12"+
            "\uffff\1\67\1\uffff\3\67\37\uffff\1\67\35\uffff\1\67\1\uffff"+
            "\1\67",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\153",
            "\1\67\2\uffff\3\67\3\uffff\2\67\1\uffff\1\67\1\uffff\1\67\12"+
            "\uffff\1\67\1\uffff\3\67\37\uffff\1\67\35\uffff\1\67\1\uffff"+
            "\1\67",
            "\1\67\2\uffff\3\67\3\uffff\2\67\1\uffff\1\67\1\uffff\1\67\12"+
            "\uffff\1\67\1\uffff\3\67\37\uffff\1\67\35\uffff\1\67\1\uffff"+
            "\1\67",
            "",
            "\1\67\2\uffff\3\67\3\uffff\2\67\1\uffff\1\67\1\uffff\1\67\12"+
            "\uffff\1\67\1\uffff\3\67\37\uffff\1\67\35\uffff\1\67\1\uffff"+
            "\1\67",
            "",
            "",
            "\1\157\2\uffff\3\157\3\uffff\2\157\1\uffff\1\157\1\uffff\1\157"+
            "\12\uffff\1\157\1\uffff\3\157\37\uffff\1\157\35\uffff\1\157"+
            "\1\uffff\1\157",
            "\41\163\1\162\2\163\3\162\3\163\1\161\1\162\1\163\1\162\1\163"+
            "\1\162\12\163\1\162\1\163\3\162\37\163\1\162\35\163\1\162\1"+
            "\163\1\162\uff80\163",
            "",
            "",
            "\1\164",
            "\1\165",
            "\1\166",
            "",
            "",
            "",
            "",
            "",
            "\1\167",
            "\1\170",
            "",
            "\1\171",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\173",
            "",
            "\1\174",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\177",
            "\1\u0080",
            "\1\u0081",
            "\1\u0082",
            "",
            "",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "",
            "",
            "\1\157\2\uffff\3\157\3\uffff\2\157\1\uffff\1\157\1\uffff\1\157"+
            "\12\uffff\1\157\1\uffff\3\157\37\uffff\1\157\35\uffff\1\157"+
            "\1\uffff\1\157",
            "",
            "\41\163\1\162\2\163\3\162\3\163\1\161\1\162\1\163\1\162\1\163"+
            "\1\u0084\12\163\1\162\1\163\3\162\37\163\1\162\35\163\1\162"+
            "\1\163\1\162\uff80\163",
            "\41\163\1\162\2\163\3\162\3\163\1\161\1\162\1\163\1\162\1\163"+
            "\1\162\12\163\1\162\1\163\3\162\37\163\1\162\35\163\1\162\1"+
            "\163\1\162\uff80\163",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0087",
            "\1\u0088",
            "\1\u0089",
            "\1\u008a",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u008c",
            "",
            "",
            "\1\u008d",
            "\1\u008e",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0090",
            "",
            "\1\162\2\uffff\3\162\3\uffff\1\161\1\162\1\uffff\1\162\1\uffff"+
            "\1\162\12\uffff\1\162\1\uffff\3\162\37\uffff\1\162\35\uffff"+
            "\1\162\1\uffff\1\162",
            "",
            "",
            "\1\u0091",
            "\1\u0092",
            "\1\u0093",
            "\1\u0094",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0096",
            "\1\u0097",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0099",
            "\1\u009a",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\1\u009d",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\1\u009f",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "",
            "\1\u00a1",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            ""
    };

    static final short[] DFA15_eot = DFA.unpackEncodedString(DFA15_eotS);
    static final short[] DFA15_eof = DFA.unpackEncodedString(DFA15_eofS);
    static final char[] DFA15_min = DFA.unpackEncodedStringToUnsignedChars(DFA15_minS);
    static final char[] DFA15_max = DFA.unpackEncodedStringToUnsignedChars(DFA15_maxS);
    static final short[] DFA15_accept = DFA.unpackEncodedString(DFA15_acceptS);
    static final short[] DFA15_special = DFA.unpackEncodedString(DFA15_specialS);
    static final short[][] DFA15_transition;

    static {
        int numStates = DFA15_transitionS.length;
        DFA15_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA15_transition[i] = DFA.unpackEncodedString(DFA15_transitionS[i]);
        }
    }

    class DFA15 extends DFA {

        public DFA15(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 15;
            this.eot = DFA15_eot;
            this.eof = DFA15_eof;
            this.min = DFA15_min;
            this.max = DFA15_max;
            this.accept = DFA15_accept;
            this.special = DFA15_special;
            this.transition = DFA15_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T14 | T15 | T16 | T17 | T18 | T19 | T20 | T21 | T22 | T23 | T24 | T25 | T26 | T27 | T28 | T29 | T30 | T31 | T32 | T33 | T34 | T35 | T36 | T37 | T38 | T39 | T40 | T41 | T42 | T43 | T44 | T45 | T46 | T47 | T48 | T49 | T50 | T51 | T52 | T53 | RULE_ID | RULE_INT | RULE_NUMBER | RULE_STRING | RULE_ML_COMMENT | RULE_SL_COMMENT | RULE_WS | RULE_NL | RULE_OPERATOR | RULE_ANY_OTHER );";
        }
    }
 

}