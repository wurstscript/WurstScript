// $ANTLR 3.4 /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g 2012-02-01 00:40:33

	package de.peeeq.parseq.parser;	


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class ParseqAntlrParserLexer extends Lexer {
    public static final int EOF=-1;
    public static final int T__10=10;
    public static final int T__11=11;
    public static final int T__12=12;
    public static final int T__13=13;
    public static final int T__14=14;
    public static final int T__15=15;
    public static final int T__16=16;
    public static final int T__17=17;
    public static final int T__18=18;
    public static final int T__19=19;
    public static final int T__20=20;
    public static final int T__21=21;
    public static final int T__22=22;
    public static final int T__23=23;
    public static final int T__24=24;
    public static final int COMMENT=4;
    public static final int ID=5;
    public static final int ID_PART=6;
    public static final int ID_START=7;
    public static final int STRVAL=8;
    public static final int WS=9;

    // delegates
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[] {};
    }

    public ParseqAntlrParserLexer() {} 
    public ParseqAntlrParserLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public ParseqAntlrParserLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "/home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g"; }

    // $ANTLR start "T__10"
    public final void mT__10() throws RecognitionException {
        try {
            int _type = T__10;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:6:7: ( '(' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:6:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__10"

    // $ANTLR start "T__11"
    public final void mT__11() throws RecognitionException {
        try {
            int _type = T__11;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:7:7: ( ')' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:7:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__11"

    // $ANTLR start "T__12"
    public final void mT__12() throws RecognitionException {
        try {
            int _type = T__12;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:8:7: ( '*' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:8:9: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__12"

    // $ANTLR start "T__13"
    public final void mT__13() throws RecognitionException {
        try {
            int _type = T__13;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:9:7: ( ',' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:9:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__13"

    // $ANTLR start "T__14"
    public final void mT__14() throws RecognitionException {
        try {
            int _type = T__14;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:10:7: ( '.' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:10:9: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__14"

    // $ANTLR start "T__15"
    public final void mT__15() throws RecognitionException {
        try {
            int _type = T__15;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:11:7: ( '<' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:11:9: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__15"

    // $ANTLR start "T__16"
    public final void mT__16() throws RecognitionException {
        try {
            int _type = T__16;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:12:7: ( '=' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:12:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__16"

    // $ANTLR start "T__17"
    public final void mT__17() throws RecognitionException {
        try {
            int _type = T__17;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:13:7: ( '>' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:13:9: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__17"

    // $ANTLR start "T__18"
    public final void mT__18() throws RecognitionException {
        try {
            int _type = T__18;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:14:7: ( 'abstract syntax:' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:14:9: 'abstract syntax:'
            {
            match("abstract syntax:"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__18"

    // $ANTLR start "T__19"
    public final void mT__19() throws RecognitionException {
        try {
            int _type = T__19;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:15:7: ( 'attributes:' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:15:9: 'attributes:'
            {
            match("attributes:"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__19"

    // $ANTLR start "T__20"
    public final void mT__20() throws RecognitionException {
        try {
            int _type = T__20;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:16:7: ( 'by' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:16:9: 'by'
            {
            match("by"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__20"

    // $ANTLR start "T__21"
    public final void mT__21() throws RecognitionException {
        try {
            int _type = T__21;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:17:7: ( 'implemented' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:17:9: 'implemented'
            {
            match("implemented"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__21"

    // $ANTLR start "T__22"
    public final void mT__22() throws RecognitionException {
        try {
            int _type = T__22;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:18:7: ( 'package' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:18:9: 'package'
            {
            match("package"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__22"

    // $ANTLR start "T__23"
    public final void mT__23() throws RecognitionException {
        try {
            int _type = T__23;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:19:7: ( 'returns' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:19:9: 'returns'
            {
            match("returns"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__23"

    // $ANTLR start "T__24"
    public final void mT__24() throws RecognitionException {
        try {
            int _type = T__24;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:20:7: ( '|' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:20:9: '|'
            {
            match('|'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__24"

    // $ANTLR start "ID_START"
    public final void mID_START() throws RecognitionException {
        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:123:19: ( 'a' .. 'z' | 'A' .. 'Z' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:
            {
            if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ID_START"

    // $ANTLR start "ID_PART"
    public final void mID_PART() throws RecognitionException {
        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:124:18: ( ID_START | '_' | '0' .. '9' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:
            {
            if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ID_PART"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:126:4: ( ID_START ( ID_PART )* )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:126:6: ID_START ( ID_PART )*
            {
            mID_START(); 


            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:126:15: ( ID_PART )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0 >= '0' && LA1_0 <= '9')||(LA1_0 >= 'A' && LA1_0 <= 'Z')||LA1_0=='_'||(LA1_0 >= 'a' && LA1_0 <= 'z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


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
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ID"

    // $ANTLR start "STRVAL"
    public final void mSTRVAL() throws RecognitionException {
        try {
            int _type = STRVAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:131:8: ( '\"' (~ ( '\"' ) )* '\"' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:131:10: '\"' (~ ( '\"' ) )* '\"'
            {
            match('\"'); 

            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:131:14: (~ ( '\"' ) )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0 >= '\u0000' && LA2_0 <= '!')||(LA2_0 >= '#' && LA2_0 <= '\uFFFF')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:
            	    {
            	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '!')||(input.LA(1) >= '#' && input.LA(1) <= '\uFFFF') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "STRVAL"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:132:5: ( ( ' ' | '\\t' | '\\n' | '\\r' )+ )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:132:9: ( ' ' | '\\t' | '\\n' | '\\r' )+
            {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:132:9: ( ' ' | '\\t' | '\\n' | '\\r' )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0 >= '\t' && LA3_0 <= '\n')||LA3_0=='\r'||LA3_0==' ') ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:
            	    {
            	    if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


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


            skip();

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:133:9: ( '//' (~ ( '\\n' | '\\r' ) )* )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:133:11: '//' (~ ( '\\n' | '\\r' ) )*
            {
            match("//"); 



            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:133:16: (~ ( '\\n' | '\\r' ) )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0 >= '\u0000' && LA4_0 <= '\t')||(LA4_0 >= '\u000B' && LA4_0 <= '\f')||(LA4_0 >= '\u000E' && LA4_0 <= '\uFFFF')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:
            	    {
            	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\uFFFF') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            skip();

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "COMMENT"

    public void mTokens() throws RecognitionException {
        // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:8: ( T__10 | T__11 | T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | ID | STRVAL | WS | COMMENT )
        int alt5=19;
        alt5 = dfa5.predict(input);
        switch (alt5) {
            case 1 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:10: T__10
                {
                mT__10(); 


                }
                break;
            case 2 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:16: T__11
                {
                mT__11(); 


                }
                break;
            case 3 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:22: T__12
                {
                mT__12(); 


                }
                break;
            case 4 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:28: T__13
                {
                mT__13(); 


                }
                break;
            case 5 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:34: T__14
                {
                mT__14(); 


                }
                break;
            case 6 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:40: T__15
                {
                mT__15(); 


                }
                break;
            case 7 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:46: T__16
                {
                mT__16(); 


                }
                break;
            case 8 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:52: T__17
                {
                mT__17(); 


                }
                break;
            case 9 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:58: T__18
                {
                mT__18(); 


                }
                break;
            case 10 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:64: T__19
                {
                mT__19(); 


                }
                break;
            case 11 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:70: T__20
                {
                mT__20(); 


                }
                break;
            case 12 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:76: T__21
                {
                mT__21(); 


                }
                break;
            case 13 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:82: T__22
                {
                mT__22(); 


                }
                break;
            case 14 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:88: T__23
                {
                mT__23(); 


                }
                break;
            case 15 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:94: T__24
                {
                mT__24(); 


                }
                break;
            case 16 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:100: ID
                {
                mID(); 


                }
                break;
            case 17 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:103: STRVAL
                {
                mSTRVAL(); 


                }
                break;
            case 18 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:110: WS
                {
                mWS(); 


                }
                break;
            case 19 :
                // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:1:113: COMMENT
                {
                mCOMMENT(); 


                }
                break;

        }

    }


    protected DFA5 dfa5 = new DFA5(this);
    static final String DFA5_eotS =
        "\11\uffff\5\17\5\uffff\2\17\1\33\5\17\1\uffff\25\17\1\66\1\67\3"+
        "\17\3\uffff\4\17\1\uffff\1\77\1\uffff";
    static final String DFA5_eofS =
        "\100\uffff";
    static final String DFA5_minS =
        "\1\11\10\uffff\1\142\1\171\1\155\1\141\1\145\5\uffff\1\163\1\164"+
        "\1\60\1\160\1\143\2\164\1\162\1\uffff\1\154\1\153\1\165\1\162\1"+
        "\151\1\145\1\141\1\162\1\141\1\142\1\155\1\147\1\156\1\143\1\165"+
        "\2\145\1\163\2\164\1\156\2\60\1\40\1\145\1\164\3\uffff\1\163\1\145"+
        "\1\72\1\144\1\uffff\1\60\1\uffff";
    static final String DFA5_maxS =
        "\1\174\10\uffff\1\164\1\171\1\155\1\141\1\145\5\uffff\1\163\1\164"+
        "\1\172\1\160\1\143\2\164\1\162\1\uffff\1\154\1\153\1\165\1\162\1"+
        "\151\1\145\1\141\1\162\1\141\1\142\1\155\1\147\1\156\1\143\1\165"+
        "\2\145\1\163\2\164\1\156\2\172\1\40\1\145\1\164\3\uffff\1\163\1"+
        "\145\1\72\1\144\1\uffff\1\172\1\uffff";
    static final String DFA5_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\5\uffff\1\17\1\20\1\21"+
        "\1\22\1\23\10\uffff\1\13\32\uffff\1\15\1\16\1\11\4\uffff\1\12\1"+
        "\uffff\1\14";
    static final String DFA5_specialS =
        "\100\uffff}>";
    static final String[] DFA5_transitionS = {
            "\2\21\2\uffff\1\21\22\uffff\1\21\1\uffff\1\20\5\uffff\1\1\1"+
            "\2\1\3\1\uffff\1\4\1\uffff\1\5\1\22\14\uffff\1\6\1\7\1\10\2"+
            "\uffff\32\17\6\uffff\1\11\1\12\6\17\1\13\6\17\1\14\1\17\1\15"+
            "\10\17\1\uffff\1\16",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\23\21\uffff\1\24",
            "\1\25",
            "\1\26",
            "\1\27",
            "\1\30",
            "",
            "",
            "",
            "",
            "",
            "\1\31",
            "\1\32",
            "\12\17\7\uffff\32\17\4\uffff\1\17\1\uffff\32\17",
            "\1\34",
            "\1\35",
            "\1\36",
            "\1\37",
            "\1\40",
            "",
            "\1\41",
            "\1\42",
            "\1\43",
            "\1\44",
            "\1\45",
            "\1\46",
            "\1\47",
            "\1\50",
            "\1\51",
            "\1\52",
            "\1\53",
            "\1\54",
            "\1\55",
            "\1\56",
            "\1\57",
            "\1\60",
            "\1\61",
            "\1\62",
            "\1\63",
            "\1\64",
            "\1\65",
            "\12\17\7\uffff\32\17\4\uffff\1\17\1\uffff\32\17",
            "\12\17\7\uffff\32\17\4\uffff\1\17\1\uffff\32\17",
            "\1\70",
            "\1\71",
            "\1\72",
            "",
            "",
            "",
            "\1\73",
            "\1\74",
            "\1\75",
            "\1\76",
            "",
            "\12\17\7\uffff\32\17\4\uffff\1\17\1\uffff\32\17",
            ""
    };

    static final short[] DFA5_eot = DFA.unpackEncodedString(DFA5_eotS);
    static final short[] DFA5_eof = DFA.unpackEncodedString(DFA5_eofS);
    static final char[] DFA5_min = DFA.unpackEncodedStringToUnsignedChars(DFA5_minS);
    static final char[] DFA5_max = DFA.unpackEncodedStringToUnsignedChars(DFA5_maxS);
    static final short[] DFA5_accept = DFA.unpackEncodedString(DFA5_acceptS);
    static final short[] DFA5_special = DFA.unpackEncodedString(DFA5_specialS);
    static final short[][] DFA5_transition;

    static {
        int numStates = DFA5_transitionS.length;
        DFA5_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA5_transition[i] = DFA.unpackEncodedString(DFA5_transitionS[i]);
        }
    }

    class DFA5 extends DFA {

        public DFA5(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 5;
            this.eot = DFA5_eot;
            this.eof = DFA5_eof;
            this.min = DFA5_min;
            this.max = DFA5_max;
            this.accept = DFA5_accept;
            this.special = DFA5_special;
            this.transition = DFA5_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__10 | T__11 | T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | ID | STRVAL | WS | COMMENT );";
        }
    }
 

}