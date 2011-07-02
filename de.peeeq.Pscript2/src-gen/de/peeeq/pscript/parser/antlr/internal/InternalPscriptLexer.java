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
    public static final int T__29=29;
    public static final int T__28=28;
    public static final int T__27=27;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int T__21=21;
    public static final int RULE_ANY_OTHER=12;
    public static final int T__20=20;
    public static final int EOF=-1;
    public static final int T__55=55;
    public static final int T__19=19;
    public static final int T__51=51;
    public static final int T__16=16;
    public static final int T__15=15;
    public static final int T__52=52;
    public static final int T__53=53;
    public static final int T__18=18;
    public static final int T__54=54;
    public static final int T__17=17;
    public static final int RULE_NUMBER=7;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int RULE_INT=6;
    public static final int T__50=50;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__46=46;
    public static final int T__47=47;
    public static final int T__44=44;
    public static final int T__45=45;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int RULE_NL=4;
    public static final int RULE_SL_COMMENT=10;
    public static final int RULE_ML_COMMENT=9;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int RULE_STRING=8;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int RULE_WS=11;

    // delegates
    // delegators

    public InternalPscriptLexer() {;} 
    public InternalPscriptLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public InternalPscriptLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g"; }

    // $ANTLR start "T__13"
    public final void mT__13() throws RecognitionException {
        try {
            int _type = T__13;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:11:7: ( 'package' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:11:9: 'package'
            {
            match("package"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__13"

    // $ANTLR start "T__14"
    public final void mT__14() throws RecognitionException {
        try {
            int _type = T__14;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:12:7: ( '{' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:12:9: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__14"

    // $ANTLR start "T__15"
    public final void mT__15() throws RecognitionException {
        try {
            int _type = T__15;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:13:7: ( '}' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:13:9: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__15"

    // $ANTLR start "T__16"
    public final void mT__16() throws RecognitionException {
        try {
            int _type = T__16;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:14:7: ( 'import' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:14:9: 'import'
            {
            match("import"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__16"

    // $ANTLR start "T__17"
    public final void mT__17() throws RecognitionException {
        try {
            int _type = T__17;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:15:7: ( '.' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:15:9: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__17"

    // $ANTLR start "T__18"
    public final void mT__18() throws RecognitionException {
        try {
            int _type = T__18;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:16:7: ( '.*' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:16:9: '.*'
            {
            match(".*"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__18"

    // $ANTLR start "T__19"
    public final void mT__19() throws RecognitionException {
        try {
            int _type = T__19;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:17:7: ( 'native' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:17:9: 'native'
            {
            match("native"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__19"

    // $ANTLR start "T__20"
    public final void mT__20() throws RecognitionException {
        try {
            int _type = T__20;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:18:7: ( 'type' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:18:9: 'type'
            {
            match("type"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__20"

    // $ANTLR start "T__21"
    public final void mT__21() throws RecognitionException {
        try {
            int _type = T__21;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:19:7: ( '=' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:19:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__21"

    // $ANTLR start "T__22"
    public final void mT__22() throws RecognitionException {
        try {
            int _type = T__22;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:20:7: ( 'extends' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:20:9: 'extends'
            {
            match("extends"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__22"

    // $ANTLR start "T__23"
    public final void mT__23() throws RecognitionException {
        try {
            int _type = T__23;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:21:7: ( 'class' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:21:9: 'class'
            {
            match("class"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__23"

    // $ANTLR start "T__24"
    public final void mT__24() throws RecognitionException {
        try {
            int _type = T__24;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:22:7: ( 'var' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:22:9: 'var'
            {
            match("var"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__24"

    // $ANTLR start "T__25"
    public final void mT__25() throws RecognitionException {
        try {
            int _type = T__25;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:23:7: ( 'val' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:23:9: 'val'
            {
            match("val"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__25"

    // $ANTLR start "T__26"
    public final void mT__26() throws RecognitionException {
        try {
            int _type = T__26;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:24:7: ( ':' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:24:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__26"

    // $ANTLR start "T__27"
    public final void mT__27() throws RecognitionException {
        try {
            int _type = T__27;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:25:7: ( 'function' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:25:9: 'function'
            {
            match("function"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__27"

    // $ANTLR start "T__28"
    public final void mT__28() throws RecognitionException {
        try {
            int _type = T__28;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:26:7: ( '(' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:26:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__28"

    // $ANTLR start "T__29"
    public final void mT__29() throws RecognitionException {
        try {
            int _type = T__29;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:27:7: ( ',' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:27:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__29"

    // $ANTLR start "T__30"
    public final void mT__30() throws RecognitionException {
        try {
            int _type = T__30;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:28:7: ( ')' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:28:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__30"

    // $ANTLR start "T__31"
    public final void mT__31() throws RecognitionException {
        try {
            int _type = T__31;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:29:7: ( 'return' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:29:9: 'return'
            {
            match("return"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__31"

    // $ANTLR start "T__32"
    public final void mT__32() throws RecognitionException {
        try {
            int _type = T__32;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:30:7: ( 'if' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:30:9: 'if'
            {
            match("if"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__32"

    // $ANTLR start "T__33"
    public final void mT__33() throws RecognitionException {
        try {
            int _type = T__33;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:31:7: ( 'else' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:31:9: 'else'
            {
            match("else"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__33"

    // $ANTLR start "T__34"
    public final void mT__34() throws RecognitionException {
        try {
            int _type = T__34;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:32:7: ( 'while' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:32:9: 'while'
            {
            match("while"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__34"

    // $ANTLR start "T__35"
    public final void mT__35() throws RecognitionException {
        try {
            int _type = T__35;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:33:7: ( '+=' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:33:9: '+='
            {
            match("+="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__35"

    // $ANTLR start "T__36"
    public final void mT__36() throws RecognitionException {
        try {
            int _type = T__36;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:34:7: ( '-=' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:34:9: '-='
            {
            match("-="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__36"

    // $ANTLR start "T__37"
    public final void mT__37() throws RecognitionException {
        try {
            int _type = T__37;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:35:7: ( 'or' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:35:9: 'or'
            {
            match("or"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__37"

    // $ANTLR start "T__38"
    public final void mT__38() throws RecognitionException {
        try {
            int _type = T__38;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:36:7: ( 'and' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:36:9: 'and'
            {
            match("and"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__38"

    // $ANTLR start "T__39"
    public final void mT__39() throws RecognitionException {
        try {
            int _type = T__39;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:37:7: ( '!=' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:37:9: '!='
            {
            match("!="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__39"

    // $ANTLR start "T__40"
    public final void mT__40() throws RecognitionException {
        try {
            int _type = T__40;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:38:7: ( '==' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:38:9: '=='
            {
            match("=="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__40"

    // $ANTLR start "T__41"
    public final void mT__41() throws RecognitionException {
        try {
            int _type = T__41;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:39:7: ( '<=' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:39:9: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__41"

    // $ANTLR start "T__42"
    public final void mT__42() throws RecognitionException {
        try {
            int _type = T__42;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:40:7: ( '<' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:40:9: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__42"

    // $ANTLR start "T__43"
    public final void mT__43() throws RecognitionException {
        try {
            int _type = T__43;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:41:7: ( '>=' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:41:9: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__43"

    // $ANTLR start "T__44"
    public final void mT__44() throws RecognitionException {
        try {
            int _type = T__44;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:42:7: ( '>' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:42:9: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__44"

    // $ANTLR start "T__45"
    public final void mT__45() throws RecognitionException {
        try {
            int _type = T__45;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:43:7: ( '+' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:43:9: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__45"

    // $ANTLR start "T__46"
    public final void mT__46() throws RecognitionException {
        try {
            int _type = T__46;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:44:7: ( '-' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:44:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__46"

    // $ANTLR start "T__47"
    public final void mT__47() throws RecognitionException {
        try {
            int _type = T__47;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:45:7: ( '*' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:45:9: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__47"

    // $ANTLR start "T__48"
    public final void mT__48() throws RecognitionException {
        try {
            int _type = T__48;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:46:7: ( '/' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:46:9: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__48"

    // $ANTLR start "T__49"
    public final void mT__49() throws RecognitionException {
        try {
            int _type = T__49;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:47:7: ( '%' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:47:9: '%'
            {
            match('%'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__49"

    // $ANTLR start "T__50"
    public final void mT__50() throws RecognitionException {
        try {
            int _type = T__50;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:48:7: ( 'mod' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:48:9: 'mod'
            {
            match("mod"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__50"

    // $ANTLR start "T__51"
    public final void mT__51() throws RecognitionException {
        try {
            int _type = T__51;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:49:7: ( 'div' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:49:9: 'div'
            {
            match("div"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__51"

    // $ANTLR start "T__52"
    public final void mT__52() throws RecognitionException {
        try {
            int _type = T__52;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:50:7: ( 'not' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:50:9: 'not'
            {
            match("not"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__52"

    // $ANTLR start "T__53"
    public final void mT__53() throws RecognitionException {
        try {
            int _type = T__53;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:51:7: ( 'true' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:51:9: 'true'
            {
            match("true"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__53"

    // $ANTLR start "T__54"
    public final void mT__54() throws RecognitionException {
        try {
            int _type = T__54;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:52:7: ( 'false' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:52:9: 'false'
            {
            match("false"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__54"

    // $ANTLR start "T__55"
    public final void mT__55() throws RecognitionException {
        try {
            int _type = T__55;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:53:7: ( 'buildin' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:53:9: 'buildin'
            {
            match("buildin"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__55"

    // $ANTLR start "RULE_ID"
    public final void mRULE_ID() throws RecognitionException {
        try {
            int _type = RULE_ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2672:9: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2672:11: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2672:35: ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_ID"

    // $ANTLR start "RULE_INT"
    public final void mRULE_INT() throws RecognitionException {
        try {
            int _type = RULE_INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2674:10: ( ( '0' .. '9' )+ )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2674:12: ( '0' .. '9' )+
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2674:12: ( '0' .. '9' )+
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
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2674:13: '0' .. '9'
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

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_INT"

    // $ANTLR start "RULE_NUMBER"
    public final void mRULE_NUMBER() throws RecognitionException {
        try {
            int _type = RULE_NUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2676:13: ( ( '0..9' )+ '.' ( '0..9' )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2676:15: ( '0..9' )+ '.' ( '0..9' )*
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2676:15: ( '0..9' )+
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
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2676:15: '0..9'
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
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2676:27: ( '0..9' )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0=='0') ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2676:27: '0..9'
            	    {
            	    match("0..9"); 


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_NUMBER"

    // $ANTLR start "RULE_STRING"
    public final void mRULE_STRING() throws RecognitionException {
        try {
            int _type = RULE_STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2678:13: ( ( '\"' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )* '\"' | '\\'' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\\'' ) ) )* '\\'' ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2678:15: ( '\"' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )* '\"' | '\\'' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\\'' ) ) )* '\\'' )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2678:15: ( '\"' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )* '\"' | '\\'' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\\'' ) ) )* '\\'' )
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
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2678:16: '\"' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )* '\"'
                    {
                    match('\"'); 
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2678:20: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )*
                    loop5:
                    do {
                        int alt5=3;
                        int LA5_0 = input.LA(1);

                        if ( (LA5_0=='\\') ) {
                            alt5=1;
                        }
                        else if ( ((LA5_0>='\u0000' && LA5_0<='!')||(LA5_0>='#' && LA5_0<='[')||(LA5_0>=']' && LA5_0<='\uFFFF')) ) {
                            alt5=2;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2678:21: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' )
                    	    {
                    	    match('\\'); 
                    	    if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t' ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;
                    	case 2 :
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2678:62: ~ ( ( '\\\\' | '\"' ) )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


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
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2678:82: '\\'' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\\'' ) ) )* '\\''
                    {
                    match('\''); 
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2678:87: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\\'' ) ) )*
                    loop6:
                    do {
                        int alt6=3;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0=='\\') ) {
                            alt6=1;
                        }
                        else if ( ((LA6_0>='\u0000' && LA6_0<='&')||(LA6_0>='(' && LA6_0<='[')||(LA6_0>=']' && LA6_0<='\uFFFF')) ) {
                            alt6=2;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2678:88: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' )
                    	    {
                    	    match('\\'); 
                    	    if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t' ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;
                    	case 2 :
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2678:129: ~ ( ( '\\\\' | '\\'' ) )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


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

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_STRING"

    // $ANTLR start "RULE_ML_COMMENT"
    public final void mRULE_ML_COMMENT() throws RecognitionException {
        try {
            int _type = RULE_ML_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2680:17: ( '/*' ( options {greedy=false; } : . )* '*/' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2680:19: '/*' ( options {greedy=false; } : . )* '*/'
            {
            match("/*"); 

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2680:24: ( options {greedy=false; } : . )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0=='*') ) {
                    int LA8_1 = input.LA(2);

                    if ( (LA8_1=='/') ) {
                        alt8=2;
                    }
                    else if ( ((LA8_1>='\u0000' && LA8_1<='.')||(LA8_1>='0' && LA8_1<='\uFFFF')) ) {
                        alt8=1;
                    }


                }
                else if ( ((LA8_0>='\u0000' && LA8_0<=')')||(LA8_0>='+' && LA8_0<='\uFFFF')) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2680:52: .
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

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_ML_COMMENT"

    // $ANTLR start "RULE_SL_COMMENT"
    public final void mRULE_SL_COMMENT() throws RecognitionException {
        try {
            int _type = RULE_SL_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2682:17: ( '//' (~ ( ( '\\n' | '\\r' ) ) )* ( ( '\\r' )? '\\n' )? )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2682:19: '//' (~ ( ( '\\n' | '\\r' ) ) )* ( ( '\\r' )? '\\n' )?
            {
            match("//"); 

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2682:24: (~ ( ( '\\n' | '\\r' ) ) )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( ((LA9_0>='\u0000' && LA9_0<='\t')||(LA9_0>='\u000B' && LA9_0<='\f')||(LA9_0>='\u000E' && LA9_0<='\uFFFF')) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2682:24: ~ ( ( '\\n' | '\\r' ) )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2682:40: ( ( '\\r' )? '\\n' )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0=='\n'||LA11_0=='\r') ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2682:41: ( '\\r' )? '\\n'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2682:41: ( '\\r' )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0=='\r') ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2682:41: '\\r'
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

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_SL_COMMENT"

    // $ANTLR start "RULE_WS"
    public final void mRULE_WS() throws RecognitionException {
        try {
            int _type = RULE_WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2684:9: ( ( ' ' | '\\t' )+ )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2684:11: ( ' ' | '\\t' )+
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2684:11: ( ' ' | '\\t' )+
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
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:
            	    {
            	    if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


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

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_WS"

    // $ANTLR start "RULE_NL"
    public final void mRULE_NL() throws RecognitionException {
        try {
            int _type = RULE_NL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2686:9: ( ( '\\n\\r' | '\\n' | '\\r' | '\\r\\n' ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2686:11: ( '\\n\\r' | '\\n' | '\\r' | '\\r\\n' )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2686:11: ( '\\n\\r' | '\\n' | '\\r' | '\\r\\n' )
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
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2686:12: '\\n\\r'
                    {
                    match("\n\r"); 


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2686:19: '\\n'
                    {
                    match('\n'); 

                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2686:24: '\\r'
                    {
                    match('\r'); 

                    }
                    break;
                case 4 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2686:29: '\\r\\n'
                    {
                    match("\r\n"); 


                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_NL"

    // $ANTLR start "RULE_ANY_OTHER"
    public final void mRULE_ANY_OTHER() throws RecognitionException {
        try {
            int _type = RULE_ANY_OTHER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2688:16: ( . )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2688:18: .
            {
            matchAny(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_ANY_OTHER"

    public void mTokens() throws RecognitionException {
        // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:8: ( T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | RULE_ID | RULE_INT | RULE_NUMBER | RULE_STRING | RULE_ML_COMMENT | RULE_SL_COMMENT | RULE_WS | RULE_NL | RULE_ANY_OTHER )
        int alt14=52;
        alt14 = dfa14.predict(input);
        switch (alt14) {
            case 1 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:10: T__13
                {
                mT__13(); 

                }
                break;
            case 2 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:16: T__14
                {
                mT__14(); 

                }
                break;
            case 3 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:22: T__15
                {
                mT__15(); 

                }
                break;
            case 4 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:28: T__16
                {
                mT__16(); 

                }
                break;
            case 5 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:34: T__17
                {
                mT__17(); 

                }
                break;
            case 6 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:40: T__18
                {
                mT__18(); 

                }
                break;
            case 7 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:46: T__19
                {
                mT__19(); 

                }
                break;
            case 8 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:52: T__20
                {
                mT__20(); 

                }
                break;
            case 9 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:58: T__21
                {
                mT__21(); 

                }
                break;
            case 10 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:64: T__22
                {
                mT__22(); 

                }
                break;
            case 11 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:70: T__23
                {
                mT__23(); 

                }
                break;
            case 12 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:76: T__24
                {
                mT__24(); 

                }
                break;
            case 13 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:82: T__25
                {
                mT__25(); 

                }
                break;
            case 14 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:88: T__26
                {
                mT__26(); 

                }
                break;
            case 15 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:94: T__27
                {
                mT__27(); 

                }
                break;
            case 16 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:100: T__28
                {
                mT__28(); 

                }
                break;
            case 17 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:106: T__29
                {
                mT__29(); 

                }
                break;
            case 18 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:112: T__30
                {
                mT__30(); 

                }
                break;
            case 19 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:118: T__31
                {
                mT__31(); 

                }
                break;
            case 20 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:124: T__32
                {
                mT__32(); 

                }
                break;
            case 21 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:130: T__33
                {
                mT__33(); 

                }
                break;
            case 22 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:136: T__34
                {
                mT__34(); 

                }
                break;
            case 23 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:142: T__35
                {
                mT__35(); 

                }
                break;
            case 24 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:148: T__36
                {
                mT__36(); 

                }
                break;
            case 25 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:154: T__37
                {
                mT__37(); 

                }
                break;
            case 26 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:160: T__38
                {
                mT__38(); 

                }
                break;
            case 27 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:166: T__39
                {
                mT__39(); 

                }
                break;
            case 28 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:172: T__40
                {
                mT__40(); 

                }
                break;
            case 29 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:178: T__41
                {
                mT__41(); 

                }
                break;
            case 30 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:184: T__42
                {
                mT__42(); 

                }
                break;
            case 31 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:190: T__43
                {
                mT__43(); 

                }
                break;
            case 32 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:196: T__44
                {
                mT__44(); 

                }
                break;
            case 33 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:202: T__45
                {
                mT__45(); 

                }
                break;
            case 34 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:208: T__46
                {
                mT__46(); 

                }
                break;
            case 35 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:214: T__47
                {
                mT__47(); 

                }
                break;
            case 36 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:220: T__48
                {
                mT__48(); 

                }
                break;
            case 37 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:226: T__49
                {
                mT__49(); 

                }
                break;
            case 38 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:232: T__50
                {
                mT__50(); 

                }
                break;
            case 39 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:238: T__51
                {
                mT__51(); 

                }
                break;
            case 40 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:244: T__52
                {
                mT__52(); 

                }
                break;
            case 41 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:250: T__53
                {
                mT__53(); 

                }
                break;
            case 42 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:256: T__54
                {
                mT__54(); 

                }
                break;
            case 43 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:262: T__55
                {
                mT__55(); 

                }
                break;
            case 44 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:268: RULE_ID
                {
                mRULE_ID(); 

                }
                break;
            case 45 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:276: RULE_INT
                {
                mRULE_INT(); 

                }
                break;
            case 46 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:285: RULE_NUMBER
                {
                mRULE_NUMBER(); 

                }
                break;
            case 47 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:297: RULE_STRING
                {
                mRULE_STRING(); 

                }
                break;
            case 48 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:309: RULE_ML_COMMENT
                {
                mRULE_ML_COMMENT(); 

                }
                break;
            case 49 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:325: RULE_SL_COMMENT
                {
                mRULE_SL_COMMENT(); 

                }
                break;
            case 50 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:341: RULE_WS
                {
                mRULE_WS(); 

                }
                break;
            case 51 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:349: RULE_NL
                {
                mRULE_NL(); 

                }
                break;
            case 52 :
                // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1:357: RULE_ANY_OTHER
                {
                mRULE_ANY_OTHER(); 

                }
                break;

        }

    }


    protected DFA14 dfa14 = new DFA14(this);
    static final String DFA14_eotS =
        "\1\uffff\1\52\2\uffff\1\52\1\60\2\52\1\66\3\52\1\uffff\1\52\3\uffff"+
        "\2\52\1\104\1\106\2\52\1\50\1\113\1\115\1\uffff\1\121\1\uffff\3"+
        "\52\1\uffff\1\127\1\uffff\2\50\4\uffff\1\52\3\uffff\1\52\1\135\2"+
        "\uffff\4\52\2\uffff\4\52\1\uffff\2\52\3\uffff\2\52\4\uffff\1\153"+
        "\1\52\12\uffff\3\52\5\uffff\2\52\1\uffff\1\52\1\163\5\52\1\171\1"+
        "\172\4\52\1\uffff\1\177\1\u0080\1\u0081\4\52\1\uffff\1\u0086\1\u0087"+
        "\1\52\1\u0089\1\52\2\uffff\4\52\3\uffff\4\52\2\uffff\1\52\1\uffff"+
        "\1\u0094\1\52\1\u0096\1\52\1\u0098\2\52\1\u009b\1\u009c\1\52\1\uffff"+
        "\1\52\1\uffff\1\u009f\1\uffff\1\52\1\u00a1\2\uffff\1\u00a2\1\52"+
        "\1\uffff\1\u00a4\2\uffff\1\u00a5\2\uffff";
    static final String DFA14_eofS =
        "\u00a6\uffff";
    static final String DFA14_minS =
        "\1\0\1\141\2\uffff\1\146\1\52\1\141\1\162\1\75\2\154\1\141\1\uffff"+
        "\1\141\3\uffff\1\145\1\150\2\75\1\162\1\156\3\75\1\uffff\1\52\1"+
        "\uffff\1\157\1\151\1\165\1\uffff\1\56\1\uffff\2\0\4\uffff\1\143"+
        "\3\uffff\1\160\1\60\2\uffff\2\164\1\160\1\165\2\uffff\1\164\1\163"+
        "\1\141\1\154\1\uffff\1\156\1\154\3\uffff\1\164\1\151\4\uffff\1\60"+
        "\1\144\12\uffff\1\144\1\166\1\151\5\uffff\1\153\1\157\1\uffff\1"+
        "\151\1\60\4\145\1\163\2\60\1\143\1\163\1\165\1\154\1\uffff\3\60"+
        "\1\154\1\141\1\162\1\166\1\uffff\2\60\1\156\1\60\1\163\2\uffff\1"+
        "\164\1\145\1\162\1\145\3\uffff\1\144\1\147\1\164\1\145\2\uffff\1"+
        "\144\1\uffff\1\60\1\151\1\60\1\156\1\60\1\151\1\145\2\60\1\163\1"+
        "\uffff\1\157\1\uffff\1\60\1\uffff\1\156\1\60\2\uffff\1\60\1\156"+
        "\1\uffff\1\60\2\uffff\1\60\2\uffff";
    static final String DFA14_maxS =
        "\1\uffff\1\141\2\uffff\1\155\1\52\1\157\1\171\1\75\1\170\1\154"+
        "\1\141\1\uffff\1\165\3\uffff\1\145\1\150\2\75\1\162\1\156\3\75\1"+
        "\uffff\1\57\1\uffff\1\157\1\151\1\165\1\uffff\1\56\1\uffff\2\uffff"+
        "\4\uffff\1\143\3\uffff\1\160\1\172\2\uffff\2\164\1\160\1\165\2\uffff"+
        "\1\164\1\163\1\141\1\162\1\uffff\1\156\1\154\3\uffff\1\164\1\151"+
        "\4\uffff\1\172\1\144\12\uffff\1\144\1\166\1\151\5\uffff\1\153\1"+
        "\157\1\uffff\1\151\1\172\4\145\1\163\2\172\1\143\1\163\1\165\1\154"+
        "\1\uffff\3\172\1\154\1\141\1\162\1\166\1\uffff\2\172\1\156\1\172"+
        "\1\163\2\uffff\1\164\1\145\1\162\1\145\3\uffff\1\144\1\147\1\164"+
        "\1\145\2\uffff\1\144\1\uffff\1\172\1\151\1\172\1\156\1\172\1\151"+
        "\1\145\2\172\1\163\1\uffff\1\157\1\uffff\1\172\1\uffff\1\156\1\172"+
        "\2\uffff\1\172\1\156\1\uffff\1\172\2\uffff\1\172\2\uffff";
    static final String DFA14_acceptS =
        "\2\uffff\1\2\1\3\10\uffff\1\16\1\uffff\1\20\1\21\1\22\11\uffff"+
        "\1\43\1\uffff\1\45\3\uffff\1\54\1\uffff\1\55\2\uffff\1\62\2\63\1"+
        "\64\1\uffff\1\54\1\2\1\3\2\uffff\1\6\1\5\4\uffff\1\34\1\11\4\uffff"+
        "\1\16\2\uffff\1\20\1\21\1\22\2\uffff\1\27\1\41\1\30\1\42\2\uffff"+
        "\1\33\1\35\1\36\1\37\1\40\1\43\1\60\1\61\1\44\1\45\3\uffff\1\56"+
        "\1\55\1\57\1\62\1\63\2\uffff\1\24\15\uffff\1\31\7\uffff\1\50\5\uffff"+
        "\1\14\1\15\4\uffff\1\32\1\46\1\47\4\uffff\1\10\1\51\1\uffff\1\25"+
        "\12\uffff\1\13\1\uffff\1\52\1\uffff\1\26\2\uffff\1\4\1\7\2\uffff"+
        "\1\23\1\uffff\1\1\1\12\1\uffff\1\53\1\17";
    static final String DFA14_specialS =
        "\1\1\42\uffff\1\2\1\0\u0081\uffff}>";
    static final String[] DFA14_transitionS = {
            "\11\50\1\45\1\46\2\50\1\47\22\50\1\45\1\27\1\43\2\50\1\34\1"+
            "\50\1\44\1\16\1\20\1\32\1\23\1\17\1\24\1\5\1\33\1\41\11\42\1"+
            "\14\1\50\1\30\1\10\1\31\2\50\32\40\4\50\1\40\1\50\1\26\1\37"+
            "\1\12\1\36\1\11\1\15\2\40\1\4\3\40\1\35\1\6\1\25\1\1\1\40\1"+
            "\21\1\40\1\7\1\40\1\13\1\22\3\40\1\2\1\50\1\3\uff82\50",
            "\1\51",
            "",
            "",
            "\1\56\6\uffff\1\55",
            "\1\57",
            "\1\61\15\uffff\1\62",
            "\1\64\6\uffff\1\63",
            "\1\65",
            "\1\70\13\uffff\1\67",
            "\1\71",
            "\1\72",
            "",
            "\1\75\23\uffff\1\74",
            "",
            "",
            "",
            "\1\101",
            "\1\102",
            "\1\103",
            "\1\105",
            "\1\107",
            "\1\110",
            "\1\111",
            "\1\112",
            "\1\114",
            "",
            "\1\117\4\uffff\1\120",
            "",
            "\1\123",
            "\1\124",
            "\1\125",
            "",
            "\1\126",
            "",
            "\0\130",
            "\0\130",
            "",
            "",
            "",
            "",
            "\1\133",
            "",
            "",
            "",
            "\1\134",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "",
            "\1\136",
            "\1\137",
            "\1\140",
            "\1\141",
            "",
            "",
            "\1\142",
            "\1\143",
            "\1\144",
            "\1\146\5\uffff\1\145",
            "",
            "\1\147",
            "\1\150",
            "",
            "",
            "",
            "\1\151",
            "\1\152",
            "",
            "",
            "",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\154",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\155",
            "\1\156",
            "\1\157",
            "",
            "",
            "",
            "",
            "",
            "\1\160",
            "\1\161",
            "",
            "\1\162",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\164",
            "\1\165",
            "\1\166",
            "\1\167",
            "\1\170",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\173",
            "\1\174",
            "\1\175",
            "\1\176",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u0082",
            "\1\u0083",
            "\1\u0084",
            "\1\u0085",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u0088",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u008a",
            "",
            "",
            "\1\u008b",
            "\1\u008c",
            "\1\u008d",
            "\1\u008e",
            "",
            "",
            "",
            "\1\u008f",
            "\1\u0090",
            "\1\u0091",
            "\1\u0092",
            "",
            "",
            "\1\u0093",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u0095",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u0097",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u0099",
            "\1\u009a",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u009d",
            "",
            "\1\u009e",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "\1\u00a0",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u00a3",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            ""
    };

    static final short[] DFA14_eot = DFA.unpackEncodedString(DFA14_eotS);
    static final short[] DFA14_eof = DFA.unpackEncodedString(DFA14_eofS);
    static final char[] DFA14_min = DFA.unpackEncodedStringToUnsignedChars(DFA14_minS);
    static final char[] DFA14_max = DFA.unpackEncodedStringToUnsignedChars(DFA14_maxS);
    static final short[] DFA14_accept = DFA.unpackEncodedString(DFA14_acceptS);
    static final short[] DFA14_special = DFA.unpackEncodedString(DFA14_specialS);
    static final short[][] DFA14_transition;

    static {
        int numStates = DFA14_transitionS.length;
        DFA14_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA14_transition[i] = DFA.unpackEncodedString(DFA14_transitionS[i]);
        }
    }

    class DFA14 extends DFA {

        public DFA14(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 14;
            this.eot = DFA14_eot;
            this.eof = DFA14_eof;
            this.min = DFA14_min;
            this.max = DFA14_max;
            this.accept = DFA14_accept;
            this.special = DFA14_special;
            this.transition = DFA14_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | RULE_ID | RULE_INT | RULE_NUMBER | RULE_STRING | RULE_ML_COMMENT | RULE_SL_COMMENT | RULE_WS | RULE_NL | RULE_ANY_OTHER );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA14_36 = input.LA(1);

                        s = -1;
                        if ( ((LA14_36>='\u0000' && LA14_36<='\uFFFF')) ) {s = 88;}

                        else s = 40;

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA14_0 = input.LA(1);

                        s = -1;
                        if ( (LA14_0=='p') ) {s = 1;}

                        else if ( (LA14_0=='{') ) {s = 2;}

                        else if ( (LA14_0=='}') ) {s = 3;}

                        else if ( (LA14_0=='i') ) {s = 4;}

                        else if ( (LA14_0=='.') ) {s = 5;}

                        else if ( (LA14_0=='n') ) {s = 6;}

                        else if ( (LA14_0=='t') ) {s = 7;}

                        else if ( (LA14_0=='=') ) {s = 8;}

                        else if ( (LA14_0=='e') ) {s = 9;}

                        else if ( (LA14_0=='c') ) {s = 10;}

                        else if ( (LA14_0=='v') ) {s = 11;}

                        else if ( (LA14_0==':') ) {s = 12;}

                        else if ( (LA14_0=='f') ) {s = 13;}

                        else if ( (LA14_0=='(') ) {s = 14;}

                        else if ( (LA14_0==',') ) {s = 15;}

                        else if ( (LA14_0==')') ) {s = 16;}

                        else if ( (LA14_0=='r') ) {s = 17;}

                        else if ( (LA14_0=='w') ) {s = 18;}

                        else if ( (LA14_0=='+') ) {s = 19;}

                        else if ( (LA14_0=='-') ) {s = 20;}

                        else if ( (LA14_0=='o') ) {s = 21;}

                        else if ( (LA14_0=='a') ) {s = 22;}

                        else if ( (LA14_0=='!') ) {s = 23;}

                        else if ( (LA14_0=='<') ) {s = 24;}

                        else if ( (LA14_0=='>') ) {s = 25;}

                        else if ( (LA14_0=='*') ) {s = 26;}

                        else if ( (LA14_0=='/') ) {s = 27;}

                        else if ( (LA14_0=='%') ) {s = 28;}

                        else if ( (LA14_0=='m') ) {s = 29;}

                        else if ( (LA14_0=='d') ) {s = 30;}

                        else if ( (LA14_0=='b') ) {s = 31;}

                        else if ( ((LA14_0>='A' && LA14_0<='Z')||LA14_0=='_'||(LA14_0>='g' && LA14_0<='h')||(LA14_0>='j' && LA14_0<='l')||LA14_0=='q'||LA14_0=='s'||LA14_0=='u'||(LA14_0>='x' && LA14_0<='z')) ) {s = 32;}

                        else if ( (LA14_0=='0') ) {s = 33;}

                        else if ( ((LA14_0>='1' && LA14_0<='9')) ) {s = 34;}

                        else if ( (LA14_0=='\"') ) {s = 35;}

                        else if ( (LA14_0=='\'') ) {s = 36;}

                        else if ( (LA14_0=='\t'||LA14_0==' ') ) {s = 37;}

                        else if ( (LA14_0=='\n') ) {s = 38;}

                        else if ( (LA14_0=='\r') ) {s = 39;}

                        else if ( ((LA14_0>='\u0000' && LA14_0<='\b')||(LA14_0>='\u000B' && LA14_0<='\f')||(LA14_0>='\u000E' && LA14_0<='\u001F')||(LA14_0>='#' && LA14_0<='$')||LA14_0=='&'||LA14_0==';'||(LA14_0>='?' && LA14_0<='@')||(LA14_0>='[' && LA14_0<='^')||LA14_0=='`'||LA14_0=='|'||(LA14_0>='~' && LA14_0<='\uFFFF')) ) {s = 40;}

                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA14_35 = input.LA(1);

                        s = -1;
                        if ( ((LA14_35>='\u0000' && LA14_35<='\uFFFF')) ) {s = 88;}

                        else s = 40;

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 14, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

}