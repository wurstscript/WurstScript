package de.peeeq.pscript.ui.contentassist.antlr.internal;

// Hack: Use our own Lexer superclass by means of import. 
// Currently there is no other way to specify the superclass for the lexer.
import org.eclipse.xtext.ui.editor.contentassist.antlr.internal.Lexer;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalPscriptLexer extends Lexer {
    public static final int RULE_ID=4;
    public static final int T__29=29;
    public static final int T__28=28;
    public static final int T__27=27;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int T__21=21;
    public static final int RULE_ANY_OTHER=11;
    public static final int T__20=20;
    public static final int EOF=-1;
    public static final int T__55=55;
    public static final int T__19=19;
    public static final int T__51=51;
    public static final int T__16=16;
    public static final int T__52=52;
    public static final int T__15=15;
    public static final int T__53=53;
    public static final int T__18=18;
    public static final int T__54=54;
    public static final int T__17=17;
    public static final int T__12=12;
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
    public static final int RULE_NL=5;
    public static final int RULE_SL_COMMENT=9;
    public static final int RULE_ML_COMMENT=8;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int RULE_STRING=7;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int RULE_WS=10;

    // delegates
    // delegators

    public InternalPscriptLexer() {;} 
    public InternalPscriptLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public InternalPscriptLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g"; }

    // $ANTLR start "T__12"
    public final void mT__12() throws RecognitionException {
        try {
            int _type = T__12;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11:7: ( '*' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11:9: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__12"

    // $ANTLR start "T__13"
    public final void mT__13() throws RecognitionException {
        try {
            int _type = T__13;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:12:7: ( 'true' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:12:9: 'true'
            {
            match("true"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:13:7: ( 'false' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:13:9: 'false'
            {
            match("false"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:14:7: ( 'package' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:14:9: 'package'
            {
            match("package"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:15:7: ( '{' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:15:9: '{'
            {
            match('{'); 

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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:16:7: ( '}' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:16:9: '}'
            {
            match('}'); 

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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:17:7: ( 'import' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:17:9: 'import'
            {
            match("import"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:18:7: ( '.' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:18:9: '.'
            {
            match('.'); 

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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:19:7: ( 'native' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:19:9: 'native'
            {
            match("native"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:20:7: ( '(' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:20:9: '('
            {
            match('('); 

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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:21:7: ( ')' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:21:9: ')'
            {
            match(')'); 

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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:22:7: ( ',' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:22:9: ','
            {
            match(','); 

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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:23:7: ( 'returns' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:23:9: 'returns'
            {
            match("returns"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:24:7: ( 'nativetype' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:24:9: 'nativetype'
            {
            match("nativetype"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:25:7: ( 'extends' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:25:9: 'extends'
            {
            match("extends"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:26:7: ( 'class' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:26:9: 'class'
            {
            match("class"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:27:7: ( '=' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:27:9: '='
            {
            match('='); 

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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:28:7: ( '[' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:28:9: '['
            {
            match('['); 

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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:29:7: ( ']' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:29:9: ']'
            {
            match(']'); 

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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:30:7: ( 'function' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:30:9: 'function'
            {
            match("function"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:31:7: ( 'return' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:31:9: 'return'
            {
            match("return"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:32:7: ( 'if' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:32:9: 'if'
            {
            match("if"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:33:7: ( 'else' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:33:9: 'else'
            {
            match("else"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:34:7: ( 'while' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:34:9: 'while'
            {
            match("while"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:35:7: ( '+=' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:35:9: '+='
            {
            match("+="); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:36:7: ( '-=' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:36:9: '-='
            {
            match("-="); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:37:7: ( '==' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:37:9: '=='
            {
            match("=="); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:38:7: ( '!=' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:38:9: '!='
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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:39:7: ( '<=' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:39:9: '<='
            {
            match("<="); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:40:7: ( '<' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:40:9: '<'
            {
            match('<'); 

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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:41:7: ( '>=' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:41:9: '>='
            {
            match(">="); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:42:7: ( '>' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:42:9: '>'
            {
            match('>'); 

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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:43:7: ( '+' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:43:9: '+'
            {
            match('+'); 

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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:44:7: ( '-' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:44:9: '-'
            {
            match('-'); 

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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:45:7: ( '/' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:45:9: '/'
            {
            match('/'); 

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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:46:7: ( '%' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:46:9: '%'
            {
            match('%'); 

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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:47:7: ( 'mod' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:47:9: 'mod'
            {
            match("mod"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:48:7: ( 'div' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:48:9: 'div'
            {
            match("div"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:49:7: ( 'not' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:49:9: 'not'
            {
            match("not"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:50:7: ( 'init' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:50:9: 'init'
            {
            match("init"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:51:7: ( 'val' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:51:9: 'val'
            {
            match("val"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:52:7: ( 'array' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:52:9: 'array'
            {
            match("array"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:53:7: ( 'or' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:53:9: 'or'
            {
            match("or"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:54:7: ( 'and' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:54:9: 'and'
            {
            match("and"); 


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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11223:9: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )* )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11223:11: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11223:35: ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:
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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11225:10: ( ( '0' .. '9' )+ )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11225:12: ( '0' .. '9' )+
            {
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11225:12: ( '0' .. '9' )+
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
            	    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11225:13: '0' .. '9'
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

    // $ANTLR start "RULE_STRING"
    public final void mRULE_STRING() throws RecognitionException {
        try {
            int _type = RULE_STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11227:13: ( '\"' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )* '\"' )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11227:15: '\"' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )* '\"'
            {
            match('\"'); 
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11227:19: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )*
            loop3:
            do {
                int alt3=3;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='\\') ) {
                    alt3=1;
                }
                else if ( ((LA3_0>='\u0000' && LA3_0<='!')||(LA3_0>='#' && LA3_0<='[')||(LA3_0>=']' && LA3_0<='\uFFFF')) ) {
                    alt3=2;
                }


                switch (alt3) {
            	case 1 :
            	    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11227:20: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\\'' | '\\\\' )
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
            	    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11227:61: ~ ( ( '\\\\' | '\"' ) )
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
            	    break loop3;
                }
            } while (true);

            match('\"'); 

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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11229:17: ( '/*' ( options {greedy=false; } : . )* '*/' ( ' ' | '\\t' | '\\n' | '\\r' )* )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11229:19: '/*' ( options {greedy=false; } : . )* '*/' ( ' ' | '\\t' | '\\n' | '\\r' )*
            {
            match("/*"); 

            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11229:24: ( options {greedy=false; } : . )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0=='*') ) {
                    int LA4_1 = input.LA(2);

                    if ( (LA4_1=='/') ) {
                        alt4=2;
                    }
                    else if ( ((LA4_1>='\u0000' && LA4_1<='.')||(LA4_1>='0' && LA4_1<='\uFFFF')) ) {
                        alt4=1;
                    }


                }
                else if ( ((LA4_0>='\u0000' && LA4_0<=')')||(LA4_0>='+' && LA4_0<='\uFFFF')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11229:52: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            match("*/"); 

            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11229:61: ( ' ' | '\\t' | '\\n' | '\\r' )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>='\t' && LA5_0<='\n')||LA5_0=='\r'||LA5_0==' ') ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11231:17: ( '//' (~ ( ( '\\n' | '\\r' ) ) )* ( ( '\\r' )? '\\n' )? )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11231:19: '//' (~ ( ( '\\n' | '\\r' ) ) )* ( ( '\\r' )? '\\n' )?
            {
            match("//"); 

            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11231:24: (~ ( ( '\\n' | '\\r' ) ) )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='\u0000' && LA6_0<='\t')||(LA6_0>='\u000B' && LA6_0<='\f')||(LA6_0>='\u000E' && LA6_0<='\uFFFF')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11231:24: ~ ( ( '\\n' | '\\r' ) )
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
            	    break loop6;
                }
            } while (true);

            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11231:40: ( ( '\\r' )? '\\n' )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='\n'||LA8_0=='\r') ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11231:41: ( '\\r' )? '\\n'
                    {
                    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11231:41: ( '\\r' )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0=='\r') ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11231:41: '\\r'
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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11233:9: ( ( '\\\\\\n\\r' | '\\\\\\n' | '\\\\\\r' | '\\\\\\r\\n' | ' ' | '\\t' )+ )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11233:11: ( '\\\\\\n\\r' | '\\\\\\n' | '\\\\\\r' | '\\\\\\r\\n' | ' ' | '\\t' )+
            {
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11233:11: ( '\\\\\\n\\r' | '\\\\\\n' | '\\\\\\r' | '\\\\\\r\\n' | ' ' | '\\t' )+
            int cnt9=0;
            loop9:
            do {
                int alt9=7;
                alt9 = dfa9.predict(input);
                switch (alt9) {
            	case 1 :
            	    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11233:12: '\\\\\\n\\r'
            	    {
            	    match("\\\n\r"); 


            	    }
            	    break;
            	case 2 :
            	    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11233:21: '\\\\\\n'
            	    {
            	    match("\\\n"); 


            	    }
            	    break;
            	case 3 :
            	    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11233:28: '\\\\\\r'
            	    {
            	    match("\\\r"); 


            	    }
            	    break;
            	case 4 :
            	    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11233:35: '\\\\\\r\\n'
            	    {
            	    match("\\\r\n"); 


            	    }
            	    break;
            	case 5 :
            	    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11233:44: ' '
            	    {
            	    match(' '); 

            	    }
            	    break;
            	case 6 :
            	    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11233:48: '\\t'
            	    {
            	    match('\t'); 

            	    }
            	    break;

            	default :
            	    if ( cnt9 >= 1 ) break loop9;
                        EarlyExitException eee =
                            new EarlyExitException(9, input);
                        throw eee;
                }
                cnt9++;
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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11235:9: ( ( '\\n\\r' | '\\n' | '\\r' | '\\r\\n' ) )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11235:11: ( '\\n\\r' | '\\n' | '\\r' | '\\r\\n' )
            {
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11235:11: ( '\\n\\r' | '\\n' | '\\r' | '\\r\\n' )
            int alt10=4;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='\n') ) {
                int LA10_1 = input.LA(2);

                if ( (LA10_1=='\r') ) {
                    alt10=1;
                }
                else {
                    alt10=2;}
            }
            else if ( (LA10_0=='\r') ) {
                int LA10_2 = input.LA(2);

                if ( (LA10_2=='\n') ) {
                    alt10=4;
                }
                else {
                    alt10=3;}
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11235:12: '\\n\\r'
                    {
                    match("\n\r"); 


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11235:19: '\\n'
                    {
                    match('\n'); 

                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11235:24: '\\r'
                    {
                    match('\r'); 

                    }
                    break;
                case 4 :
                    // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11235:29: '\\r\\n'
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
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11237:16: ( . )
            // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:11237:18: .
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
        // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:8: ( T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | RULE_ID | RULE_INT | RULE_STRING | RULE_ML_COMMENT | RULE_SL_COMMENT | RULE_WS | RULE_NL | RULE_ANY_OTHER )
        int alt11=52;
        alt11 = dfa11.predict(input);
        switch (alt11) {
            case 1 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:10: T__12
                {
                mT__12(); 

                }
                break;
            case 2 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:16: T__13
                {
                mT__13(); 

                }
                break;
            case 3 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:22: T__14
                {
                mT__14(); 

                }
                break;
            case 4 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:28: T__15
                {
                mT__15(); 

                }
                break;
            case 5 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:34: T__16
                {
                mT__16(); 

                }
                break;
            case 6 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:40: T__17
                {
                mT__17(); 

                }
                break;
            case 7 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:46: T__18
                {
                mT__18(); 

                }
                break;
            case 8 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:52: T__19
                {
                mT__19(); 

                }
                break;
            case 9 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:58: T__20
                {
                mT__20(); 

                }
                break;
            case 10 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:64: T__21
                {
                mT__21(); 

                }
                break;
            case 11 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:70: T__22
                {
                mT__22(); 

                }
                break;
            case 12 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:76: T__23
                {
                mT__23(); 

                }
                break;
            case 13 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:82: T__24
                {
                mT__24(); 

                }
                break;
            case 14 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:88: T__25
                {
                mT__25(); 

                }
                break;
            case 15 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:94: T__26
                {
                mT__26(); 

                }
                break;
            case 16 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:100: T__27
                {
                mT__27(); 

                }
                break;
            case 17 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:106: T__28
                {
                mT__28(); 

                }
                break;
            case 18 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:112: T__29
                {
                mT__29(); 

                }
                break;
            case 19 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:118: T__30
                {
                mT__30(); 

                }
                break;
            case 20 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:124: T__31
                {
                mT__31(); 

                }
                break;
            case 21 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:130: T__32
                {
                mT__32(); 

                }
                break;
            case 22 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:136: T__33
                {
                mT__33(); 

                }
                break;
            case 23 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:142: T__34
                {
                mT__34(); 

                }
                break;
            case 24 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:148: T__35
                {
                mT__35(); 

                }
                break;
            case 25 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:154: T__36
                {
                mT__36(); 

                }
                break;
            case 26 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:160: T__37
                {
                mT__37(); 

                }
                break;
            case 27 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:166: T__38
                {
                mT__38(); 

                }
                break;
            case 28 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:172: T__39
                {
                mT__39(); 

                }
                break;
            case 29 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:178: T__40
                {
                mT__40(); 

                }
                break;
            case 30 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:184: T__41
                {
                mT__41(); 

                }
                break;
            case 31 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:190: T__42
                {
                mT__42(); 

                }
                break;
            case 32 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:196: T__43
                {
                mT__43(); 

                }
                break;
            case 33 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:202: T__44
                {
                mT__44(); 

                }
                break;
            case 34 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:208: T__45
                {
                mT__45(); 

                }
                break;
            case 35 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:214: T__46
                {
                mT__46(); 

                }
                break;
            case 36 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:220: T__47
                {
                mT__47(); 

                }
                break;
            case 37 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:226: T__48
                {
                mT__48(); 

                }
                break;
            case 38 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:232: T__49
                {
                mT__49(); 

                }
                break;
            case 39 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:238: T__50
                {
                mT__50(); 

                }
                break;
            case 40 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:244: T__51
                {
                mT__51(); 

                }
                break;
            case 41 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:250: T__52
                {
                mT__52(); 

                }
                break;
            case 42 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:256: T__53
                {
                mT__53(); 

                }
                break;
            case 43 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:262: T__54
                {
                mT__54(); 

                }
                break;
            case 44 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:268: T__55
                {
                mT__55(); 

                }
                break;
            case 45 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:274: RULE_ID
                {
                mRULE_ID(); 

                }
                break;
            case 46 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:282: RULE_INT
                {
                mRULE_INT(); 

                }
                break;
            case 47 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:291: RULE_STRING
                {
                mRULE_STRING(); 

                }
                break;
            case 48 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:303: RULE_ML_COMMENT
                {
                mRULE_ML_COMMENT(); 

                }
                break;
            case 49 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:319: RULE_SL_COMMENT
                {
                mRULE_SL_COMMENT(); 

                }
                break;
            case 50 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:335: RULE_WS
                {
                mRULE_WS(); 

                }
                break;
            case 51 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:343: RULE_NL
                {
                mRULE_NL(); 

                }
                break;
            case 52 :
                // ../de.peeeq.Pscript2.ui/src-gen/de/peeeq/pscript/ui/contentassist/antlr/internal/InternalPscript.g:1:351: RULE_ANY_OTHER
                {
                mRULE_ANY_OTHER(); 

                }
                break;

        }

    }


    protected DFA9 dfa9 = new DFA9(this);
    protected DFA11 dfa11 = new DFA11(this);
    static final String DFA9_eotS =
        "\1\1\4\uffff\1\10\1\12\4\uffff";
    static final String DFA9_eofS =
        "\13\uffff";
    static final String DFA9_minS =
        "\1\11\1\uffff\1\12\2\uffff\1\15\1\12\4\uffff";
    static final String DFA9_maxS =
        "\1\134\1\uffff\1\15\2\uffff\1\15\1\12\4\uffff";
    static final String DFA9_acceptS =
        "\1\uffff\1\7\1\uffff\1\5\1\6\2\uffff\1\1\1\2\1\4\1\3";
    static final String DFA9_specialS =
        "\13\uffff}>";
    static final String[] DFA9_transitionS = {
            "\1\4\26\uffff\1\3\73\uffff\1\2",
            "",
            "\1\5\2\uffff\1\6",
            "",
            "",
            "\1\7",
            "\1\11",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA9_eot = DFA.unpackEncodedString(DFA9_eotS);
    static final short[] DFA9_eof = DFA.unpackEncodedString(DFA9_eofS);
    static final char[] DFA9_min = DFA.unpackEncodedStringToUnsignedChars(DFA9_minS);
    static final char[] DFA9_max = DFA.unpackEncodedStringToUnsignedChars(DFA9_maxS);
    static final short[] DFA9_accept = DFA.unpackEncodedString(DFA9_acceptS);
    static final short[] DFA9_special = DFA.unpackEncodedString(DFA9_specialS);
    static final short[][] DFA9_transition;

    static {
        int numStates = DFA9_transitionS.length;
        DFA9_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA9_transition[i] = DFA.unpackEncodedString(DFA9_transitionS[i]);
        }
    }

    class DFA9 extends DFA {

        public DFA9(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 9;
            this.eot = DFA9_eot;
            this.eof = DFA9_eof;
            this.min = DFA9_min;
            this.max = DFA9_max;
            this.accept = DFA9_accept;
            this.special = DFA9_special;
            this.transition = DFA9_transition;
        }
        public String getDescription() {
            return "()+ loopback of 11233:11: ( '\\\\\\n\\r' | '\\\\\\n' | '\\\\\\r' | '\\\\\\r\\n' | ' ' | '\\t' )+";
        }
    }
    static final String DFA11_eotS =
        "\2\uffff\3\53\2\uffff\1\53\1\uffff\1\53\3\uffff\3\53\1\77\2\uffff"+
        "\1\53\1\104\1\106\1\50\1\111\1\113\1\116\1\uffff\5\53\2\uffff\2"+
        "\50\6\uffff\1\53\1\uffff\3\53\2\uffff\1\53\1\137\1\53\1\uffff\2"+
        "\53\3\uffff\4\53\4\uffff\1\53\15\uffff\5\53\1\155\4\uffff\5\53\1"+
        "\uffff\2\53\1\165\5\53\1\173\1\174\1\175\1\53\1\177\1\uffff\1\u0080"+
        "\4\53\1\u0085\1\53\1\uffff\2\53\1\u0089\2\53\3\uffff\1\53\2\uffff"+
        "\1\u008d\3\53\1\uffff\3\53\1\uffff\1\u0094\1\u0095\1\u0096\1\uffff"+
        "\2\53\1\u0099\1\u009b\1\u009d\1\53\3\uffff\1\53\1\u00a0\1\uffff"+
        "\1\53\1\uffff\1\u00a2\1\uffff\1\u00a3\1\u00a4\1\uffff\1\53\3\uffff"+
        "\1\53\1\u00a7\1\uffff";
    static final String DFA11_eofS =
        "\u00a8\uffff";
    static final String DFA11_minS =
        "\1\0\1\uffff\1\162\2\141\2\uffff\1\146\1\uffff\1\141\3\uffff\1"+
        "\145\2\154\1\75\2\uffff\1\150\5\75\1\52\1\uffff\1\157\1\151\1\141"+
        "\1\156\1\162\2\uffff\1\0\1\12\6\uffff\1\165\1\uffff\1\154\1\156"+
        "\1\143\2\uffff\1\160\1\60\1\151\1\uffff\2\164\3\uffff\2\164\1\163"+
        "\1\141\4\uffff\1\151\15\uffff\1\144\1\166\1\154\1\162\1\144\1\60"+
        "\4\uffff\1\145\1\163\1\143\1\153\1\157\1\uffff\1\164\1\151\1\60"+
        "\1\165\2\145\1\163\1\154\3\60\1\141\1\60\1\uffff\1\60\1\145\1\164"+
        "\1\141\1\162\1\60\1\166\1\uffff\1\162\1\156\1\60\1\163\1\145\3\uffff"+
        "\1\171\2\uffff\1\60\1\151\1\147\1\164\1\uffff\1\145\1\156\1\144"+
        "\1\uffff\3\60\1\uffff\1\157\1\145\3\60\1\163\3\uffff\1\156\1\60"+
        "\1\uffff\1\171\1\uffff\1\60\1\uffff\2\60\1\uffff\1\160\3\uffff\1"+
        "\145\1\60\1\uffff";
    static final String DFA11_maxS =
        "\1\uffff\1\uffff\1\162\1\165\1\141\2\uffff\1\156\1\uffff\1\157"+
        "\3\uffff\1\145\1\170\1\154\1\75\2\uffff\1\150\5\75\1\57\1\uffff"+
        "\1\157\1\151\1\141\2\162\2\uffff\1\uffff\1\15\6\uffff\1\165\1\uffff"+
        "\1\154\1\156\1\143\2\uffff\1\160\1\172\1\151\1\uffff\2\164\3\uffff"+
        "\2\164\1\163\1\141\4\uffff\1\151\15\uffff\1\144\1\166\1\154\1\162"+
        "\1\144\1\172\4\uffff\1\145\1\163\1\143\1\153\1\157\1\uffff\1\164"+
        "\1\151\1\172\1\165\2\145\1\163\1\154\3\172\1\141\1\172\1\uffff\1"+
        "\172\1\145\1\164\1\141\1\162\1\172\1\166\1\uffff\1\162\1\156\1\172"+
        "\1\163\1\145\3\uffff\1\171\2\uffff\1\172\1\151\1\147\1\164\1\uffff"+
        "\1\145\1\156\1\144\1\uffff\3\172\1\uffff\1\157\1\145\3\172\1\163"+
        "\3\uffff\1\156\1\172\1\uffff\1\171\1\uffff\1\172\1\uffff\2\172\1"+
        "\uffff\1\160\3\uffff\1\145\1\172\1\uffff";
    static final String DFA11_acceptS =
        "\1\uffff\1\1\3\uffff\1\5\1\6\1\uffff\1\10\1\uffff\1\12\1\13\1\14"+
        "\4\uffff\1\22\1\23\7\uffff\1\44\5\uffff\1\55\1\56\2\uffff\2\62\2"+
        "\63\1\64\1\1\1\uffff\1\55\3\uffff\1\5\1\6\3\uffff\1\10\2\uffff\1"+
        "\12\1\13\1\14\4\uffff\1\33\1\21\1\22\1\23\1\uffff\1\31\1\41\1\32"+
        "\1\42\1\34\1\35\1\36\1\37\1\40\1\60\1\61\1\43\1\44\6\uffff\1\56"+
        "\1\57\1\62\1\63\5\uffff\1\26\15\uffff\1\53\7\uffff\1\47\5\uffff"+
        "\1\45\1\46\1\51\1\uffff\1\54\1\2\4\uffff\1\50\3\uffff\1\27\3\uffff"+
        "\1\3\6\uffff\1\20\1\30\1\52\2\uffff\1\7\1\uffff\1\11\1\uffff\1\25"+
        "\2\uffff\1\4\1\uffff\1\15\1\17\1\24\2\uffff\1\16";
    static final String DFA11_specialS =
        "\1\0\41\uffff\1\1\u0085\uffff}>";
    static final String[] DFA11_transitionS = {
            "\11\50\1\45\1\46\2\50\1\47\22\50\1\44\1\26\1\42\2\50\1\32\2"+
            "\50\1\12\1\13\1\1\1\24\1\14\1\25\1\10\1\31\12\41\2\50\1\27\1"+
            "\20\1\30\2\50\32\40\1\21\1\43\1\22\1\50\1\40\1\50\1\36\1\40"+
            "\1\17\1\34\1\16\1\3\2\40\1\7\3\40\1\33\1\11\1\37\1\4\1\40\1"+
            "\15\1\40\1\2\1\40\1\35\1\23\3\40\1\5\1\50\1\6\uff82\50",
            "",
            "\1\52",
            "\1\54\23\uffff\1\55",
            "\1\56",
            "",
            "",
            "\1\62\6\uffff\1\61\1\63",
            "",
            "\1\65\15\uffff\1\66",
            "",
            "",
            "",
            "\1\72",
            "\1\74\13\uffff\1\73",
            "\1\75",
            "\1\76",
            "",
            "",
            "\1\102",
            "\1\103",
            "\1\105",
            "\1\107",
            "\1\110",
            "\1\112",
            "\1\114\4\uffff\1\115",
            "",
            "\1\120",
            "\1\121",
            "\1\122",
            "\1\124\3\uffff\1\123",
            "\1\125",
            "",
            "",
            "\0\127",
            "\1\130\2\uffff\1\130",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\132",
            "",
            "\1\133",
            "\1\134",
            "\1\135",
            "",
            "",
            "\1\136",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\140",
            "",
            "\1\141",
            "\1\142",
            "",
            "",
            "",
            "\1\143",
            "\1\144",
            "\1\145",
            "\1\146",
            "",
            "",
            "",
            "",
            "\1\147",
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
            "",
            "",
            "",
            "\1\150",
            "\1\151",
            "\1\152",
            "\1\153",
            "\1\154",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "",
            "",
            "",
            "\1\156",
            "\1\157",
            "\1\160",
            "\1\161",
            "\1\162",
            "",
            "\1\163",
            "\1\164",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\166",
            "\1\167",
            "\1\170",
            "\1\171",
            "\1\172",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\176",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0081",
            "\1\u0082",
            "\1\u0083",
            "\1\u0084",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u0086",
            "",
            "\1\u0087",
            "\1\u0088",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u008a",
            "\1\u008b",
            "",
            "",
            "",
            "\1\u008c",
            "",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\1\u008e",
            "\1\u008f",
            "\1\u0090",
            "",
            "\1\u0091",
            "\1\u0092",
            "\1\u0093",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\1\u0097",
            "\1\u0098",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\23\53\1\u009a\6"+
            "\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\22\53\1\u009c\7"+
            "\53",
            "\1\u009e",
            "",
            "",
            "",
            "\1\u009f",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\1\u00a1",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            "",
            "\1\u00a5",
            "",
            "",
            "",
            "\1\u00a6",
            "\12\53\7\uffff\32\53\4\uffff\1\53\1\uffff\32\53",
            ""
    };

    static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
    static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
    static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
    static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
    static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
    static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
    static final short[][] DFA11_transition;

    static {
        int numStates = DFA11_transitionS.length;
        DFA11_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
        }
    }

    class DFA11 extends DFA {

        public DFA11(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 11;
            this.eot = DFA11_eot;
            this.eof = DFA11_eof;
            this.min = DFA11_min;
            this.max = DFA11_max;
            this.accept = DFA11_accept;
            this.special = DFA11_special;
            this.transition = DFA11_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | RULE_ID | RULE_INT | RULE_STRING | RULE_ML_COMMENT | RULE_SL_COMMENT | RULE_WS | RULE_NL | RULE_ANY_OTHER );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA11_0 = input.LA(1);

                        s = -1;
                        if ( (LA11_0=='*') ) {s = 1;}

                        else if ( (LA11_0=='t') ) {s = 2;}

                        else if ( (LA11_0=='f') ) {s = 3;}

                        else if ( (LA11_0=='p') ) {s = 4;}

                        else if ( (LA11_0=='{') ) {s = 5;}

                        else if ( (LA11_0=='}') ) {s = 6;}

                        else if ( (LA11_0=='i') ) {s = 7;}

                        else if ( (LA11_0=='.') ) {s = 8;}

                        else if ( (LA11_0=='n') ) {s = 9;}

                        else if ( (LA11_0=='(') ) {s = 10;}

                        else if ( (LA11_0==')') ) {s = 11;}

                        else if ( (LA11_0==',') ) {s = 12;}

                        else if ( (LA11_0=='r') ) {s = 13;}

                        else if ( (LA11_0=='e') ) {s = 14;}

                        else if ( (LA11_0=='c') ) {s = 15;}

                        else if ( (LA11_0=='=') ) {s = 16;}

                        else if ( (LA11_0=='[') ) {s = 17;}

                        else if ( (LA11_0==']') ) {s = 18;}

                        else if ( (LA11_0=='w') ) {s = 19;}

                        else if ( (LA11_0=='+') ) {s = 20;}

                        else if ( (LA11_0=='-') ) {s = 21;}

                        else if ( (LA11_0=='!') ) {s = 22;}

                        else if ( (LA11_0=='<') ) {s = 23;}

                        else if ( (LA11_0=='>') ) {s = 24;}

                        else if ( (LA11_0=='/') ) {s = 25;}

                        else if ( (LA11_0=='%') ) {s = 26;}

                        else if ( (LA11_0=='m') ) {s = 27;}

                        else if ( (LA11_0=='d') ) {s = 28;}

                        else if ( (LA11_0=='v') ) {s = 29;}

                        else if ( (LA11_0=='a') ) {s = 30;}

                        else if ( (LA11_0=='o') ) {s = 31;}

                        else if ( ((LA11_0>='A' && LA11_0<='Z')||LA11_0=='_'||LA11_0=='b'||(LA11_0>='g' && LA11_0<='h')||(LA11_0>='j' && LA11_0<='l')||LA11_0=='q'||LA11_0=='s'||LA11_0=='u'||(LA11_0>='x' && LA11_0<='z')) ) {s = 32;}

                        else if ( ((LA11_0>='0' && LA11_0<='9')) ) {s = 33;}

                        else if ( (LA11_0=='\"') ) {s = 34;}

                        else if ( (LA11_0=='\\') ) {s = 35;}

                        else if ( (LA11_0==' ') ) {s = 36;}

                        else if ( (LA11_0=='\t') ) {s = 37;}

                        else if ( (LA11_0=='\n') ) {s = 38;}

                        else if ( (LA11_0=='\r') ) {s = 39;}

                        else if ( ((LA11_0>='\u0000' && LA11_0<='\b')||(LA11_0>='\u000B' && LA11_0<='\f')||(LA11_0>='\u000E' && LA11_0<='\u001F')||(LA11_0>='#' && LA11_0<='$')||(LA11_0>='&' && LA11_0<='\'')||(LA11_0>=':' && LA11_0<=';')||(LA11_0>='?' && LA11_0<='@')||LA11_0=='^'||LA11_0=='`'||LA11_0=='|'||(LA11_0>='~' && LA11_0<='\uFFFF')) ) {s = 40;}

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA11_34 = input.LA(1);

                        s = -1;
                        if ( ((LA11_34>='\u0000' && LA11_34<='\uFFFF')) ) {s = 87;}

                        else s = 40;

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 11, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

}