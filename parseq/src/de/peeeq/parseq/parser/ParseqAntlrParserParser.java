// $ANTLR 3.4 /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g 2012-02-01 00:40:32

	package de.peeeq.parseq.parser;	
	import de.peeeq.parseq.ast.*;
	import com.google.common.collect.Lists;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class ParseqAntlrParserParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "COMMENT", "ID", "ID_PART", "ID_START", "STRVAL", "WS", "'('", "')'", "'*'", "','", "'.'", "'<'", "'='", "'>'", "'abstract syntax:'", "'attributes:'", "'by'", "'implemented'", "'package'", "'returns'", "'|'"
    };

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
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public ParseqAntlrParserParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public ParseqAntlrParserParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

    public String[] getTokenNames() { return ParseqAntlrParserParser.tokenNames; }
    public String getGrammarFileName() { return "/home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g"; }


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



    // $ANTLR start "spec"
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:31:1: spec returns [Program prog] : 'package' p= qID 'abstract syntax:' ( element[$prog] )* 'attributes:' ( attributeDef[$prog] )* EOF ;
    public final Program spec() throws RecognitionException {
        Program prog = null;


        String p =null;


        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:31:28: ( 'package' p= qID 'abstract syntax:' ( element[$prog] )* 'attributes:' ( attributeDef[$prog] )* EOF )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:32:2: 'package' p= qID 'abstract syntax:' ( element[$prog] )* 'attributes:' ( attributeDef[$prog] )* EOF
            {
            match(input,22,FOLLOW_22_in_spec40); 

            pushFollow(FOLLOW_qID_in_spec44);
            p=qID();

            state._fsp--;



            	 prog = new Program(p);
            	

            match(input,18,FOLLOW_18_in_spec50); 

            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:37:2: ( element[$prog] )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==ID) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:37:2: element[$prog]
            	    {
            	    pushFollow(FOLLOW_element_in_spec53);
            	    element(prog);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            match(input,19,FOLLOW_19_in_spec58); 

            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:39:2: ( attributeDef[$prog] )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==ID) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:39:2: attributeDef[$prog]
            	    {
            	    pushFollow(FOLLOW_attributeDef_in_spec61);
            	    attributeDef(prog);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            match(input,EOF,FOLLOW_EOF_in_spec66); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return prog;
    }
    // $ANTLR end "spec"



    // $ANTLR start "element"
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:43:1: element[Program prog] : ( contructorDef[prog] | listDef[prog] | caseDef[prog] );
    public final void element(Program prog) throws RecognitionException {
        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:43:22: ( contructorDef[prog] | listDef[prog] | caseDef[prog] )
            int alt3=3;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==ID) ) {
                switch ( input.LA(2) ) {
                case 12:
                    {
                    alt3=2;
                    }
                    break;
                case 10:
                    {
                    alt3=1;
                    }
                    break;
                case 16:
                    {
                    alt3=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 1, input);

                    throw nvae;

                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;

            }
            switch (alt3) {
                case 1 :
                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:44:2: contructorDef[prog]
                    {
                    pushFollow(FOLLOW_contructorDef_in_element78);
                    contructorDef(prog);

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:45:4: listDef[prog]
                    {
                    pushFollow(FOLLOW_listDef_in_element84);
                    listDef(prog);

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:46:4: caseDef[prog]
                    {
                    pushFollow(FOLLOW_caseDef_in_element90);
                    caseDef(prog);

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "element"



    // $ANTLR start "contructorDef"
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:49:1: contructorDef[Program prog] returns [ConstructorDef c] : name= ID '(' (t= javaType n= ID ( ',' t= javaType n= ID )* )? ')' ;
    public final ConstructorDef contructorDef(Program prog) throws RecognitionException {
        ConstructorDef c = null;


        Token name=null;
        Token n=null;
        String t =null;


        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:49:55: (name= ID '(' (t= javaType n= ID ( ',' t= javaType n= ID )* )? ')' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:50:2: name= ID '(' (t= javaType n= ID ( ',' t= javaType n= ID )* )? ')'
            {
            name=(Token)match(input,ID,FOLLOW_ID_in_contructorDef109); 


            		c = new ConstructorDef(name.getText());
            		prog.addConstructorDef(c);		
            	

            match(input,10,FOLLOW_10_in_contructorDef116); 

            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:55:6: (t= javaType n= ID ( ',' t= javaType n= ID )* )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==ID) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:55:7: t= javaType n= ID ( ',' t= javaType n= ID )*
                    {
                    pushFollow(FOLLOW_javaType_in_contructorDef121);
                    t=javaType();

                    state._fsp--;


                    n=(Token)match(input,ID,FOLLOW_ID_in_contructorDef125); 

                     
                    		c.addParam(t, n.getText());
                    	

                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:59:2: ( ',' t= javaType n= ID )*
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( (LA4_0==13) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:59:3: ',' t= javaType n= ID
                    	    {
                    	    match(input,13,FOLLOW_13_in_contructorDef133); 

                    	    pushFollow(FOLLOW_javaType_in_contructorDef137);
                    	    t=javaType();

                    	    state._fsp--;


                    	    n=(Token)match(input,ID,FOLLOW_ID_in_contructorDef141); 


                    	    		c.addParam(t, n.getText());
                    	    	

                    	    }
                    	    break;

                    	default :
                    	    break loop4;
                        }
                    } while (true);


                    }
                    break;

            }


            match(input,11,FOLLOW_11_in_contructorDef153); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return c;
    }
    // $ANTLR end "contructorDef"



    // $ANTLR start "listDef"
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:66:1: listDef[Program prog] : name= ID '*' of= ID ;
    public final void listDef(Program prog) throws RecognitionException {
        Token name=null;
        Token of=null;

        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:66:22: (name= ID '*' of= ID )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:67:2: name= ID '*' of= ID
            {
            name=(Token)match(input,ID,FOLLOW_ID_in_listDef167); 

            match(input,12,FOLLOW_12_in_listDef169); 

            of=(Token)match(input,ID,FOLLOW_ID_in_listDef173); 


            		prog.addListDef(new ListDef(name.getText(), of.getText()));
            	

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "listDef"



    // $ANTLR start "caseDef"
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:73:1: caseDef[Program prog] returns [CaseDef caseDef] : name= ID '=' c= choice[prog, caseDef] ( '|' c= choice[prog, caseDef] )* ;
    public final CaseDef caseDef(Program prog) throws RecognitionException {
        CaseDef caseDef = null;


        Token name=null;

        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:73:48: (name= ID '=' c= choice[prog, caseDef] ( '|' c= choice[prog, caseDef] )* )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:74:2: name= ID '=' c= choice[prog, caseDef] ( '|' c= choice[prog, caseDef] )*
            {
            name=(Token)match(input,ID,FOLLOW_ID_in_caseDef194); 


            		caseDef = new CaseDef(name.getText());
            		prog.addCaseDef(caseDef);
            	

            match(input,16,FOLLOW_16_in_caseDef201); 

            pushFollow(FOLLOW_choice_in_caseDef205);
            choice(prog, caseDef);

            state._fsp--;


            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:79:30: ( '|' c= choice[prog, caseDef] )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==24) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:79:31: '|' c= choice[prog, caseDef]
            	    {
            	    match(input,24,FOLLOW_24_in_caseDef209); 

            	    pushFollow(FOLLOW_choice_in_caseDef213);
            	    choice(prog, caseDef);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return caseDef;
    }
    // $ANTLR end "caseDef"



    // $ANTLR start "choice"
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:82:1: choice[Program prog, CaseDef caseDef] : (name= ID |c= contructorDef[prog] );
    public final void choice(Program prog, CaseDef caseDef) throws RecognitionException {
        Token name=null;
        ConstructorDef c =null;


        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:82:38: (name= ID |c= contructorDef[prog] )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==ID) ) {
                int LA7_1 = input.LA(2);

                if ( (LA7_1==ID||LA7_1==19||LA7_1==24) ) {
                    alt7=1;
                }
                else if ( (LA7_1==10) ) {
                    alt7=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 7, 1, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;

            }
            switch (alt7) {
                case 1 :
                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:83:4: name= ID
                    {
                    name=(Token)match(input,ID,FOLLOW_ID_in_choice232); 

                     caseDef.addAlternative(name.getText()); 

                    }
                    break;
                case 2 :
                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:84:4: c= contructorDef[prog]
                    {
                    pushFollow(FOLLOW_contructorDef_in_choice241);
                    c=contructorDef(prog);

                    state._fsp--;


                     caseDef.addAlternative(c.getName()); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "choice"



    // $ANTLR start "attributeDef"
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:87:1: attributeDef[Program prog] :elem= ID '.' attrName= ID ( '(' (t= javaType n= ID ( ',' t= javaType n= ID )* )? ')' )? (doc= STRVAL )? 'returns' returnType= javaType 'implemented' 'by' implementedBy= qID ;
    public final void attributeDef(Program prog) throws RecognitionException {
        Token elem=null;
        Token attrName=null;
        Token n=null;
        Token doc=null;
        String t =null;

        String returnType =null;

        String implementedBy =null;


        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:87:27: (elem= ID '.' attrName= ID ( '(' (t= javaType n= ID ( ',' t= javaType n= ID )* )? ')' )? (doc= STRVAL )? 'returns' returnType= javaType 'implemented' 'by' implementedBy= qID )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:88:2: elem= ID '.' attrName= ID ( '(' (t= javaType n= ID ( ',' t= javaType n= ID )* )? ')' )? (doc= STRVAL )? 'returns' returnType= javaType 'implemented' 'by' implementedBy= qID
            {

            		List<Parameter> parameters = null;
            	

            elem=(Token)match(input,ID,FOLLOW_ID_in_attributeDef260); 

            match(input,14,FOLLOW_14_in_attributeDef262); 

            attrName=(Token)match(input,ID,FOLLOW_ID_in_attributeDef266); 

            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:92:3: ( '(' (t= javaType n= ID ( ',' t= javaType n= ID )* )? ')' )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==10) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:93:3: '(' (t= javaType n= ID ( ',' t= javaType n= ID )* )? ')'
                    {
                    match(input,10,FOLLOW_10_in_attributeDef274); 


                    		 	parameters = new ArrayList<Parameter>();
                    		 

                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:97:4: (t= javaType n= ID ( ',' t= javaType n= ID )* )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0==ID) ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:97:5: t= javaType n= ID ( ',' t= javaType n= ID )*
                            {
                            pushFollow(FOLLOW_javaType_in_attributeDef287);
                            t=javaType();

                            state._fsp--;


                            n=(Token)match(input,ID,FOLLOW_ID_in_attributeDef291); 

                             
                            				parameters.add(new Parameter(t, n.getText()));
                            			

                            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:101:4: ( ',' t= javaType n= ID )*
                            loop8:
                            do {
                                int alt8=2;
                                int LA8_0 = input.LA(1);

                                if ( (LA8_0==13) ) {
                                    alt8=1;
                                }


                                switch (alt8) {
                            	case 1 :
                            	    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:101:5: ',' t= javaType n= ID
                            	    {
                            	    match(input,13,FOLLOW_13_in_attributeDef303); 

                            	    pushFollow(FOLLOW_javaType_in_attributeDef307);
                            	    t=javaType();

                            	    state._fsp--;


                            	    n=(Token)match(input,ID,FOLLOW_ID_in_attributeDef311); 


                            	    				parameters.add(new Parameter(t, n.getText()));
                            	    			

                            	    }
                            	    break;

                            	default :
                            	    break loop8;
                                }
                            } while (true);


                            }
                            break;

                    }


                    match(input,11,FOLLOW_11_in_attributeDef332); 

                    }
                    break;

            }


            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:109:3: (doc= STRVAL )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==STRVAL) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:109:4: doc= STRVAL
                    {
                    doc=(Token)match(input,STRVAL,FOLLOW_STRVAL_in_attributeDef344); 

                    }
                    break;

            }


            match(input,23,FOLLOW_23_in_attributeDef348); 

            pushFollow(FOLLOW_javaType_in_attributeDef352);
            returnType=javaType();

            state._fsp--;


            match(input,21,FOLLOW_21_in_attributeDef354); 

            match(input,20,FOLLOW_20_in_attributeDef356); 

            pushFollow(FOLLOW_qID_in_attributeDef360);
            implementedBy=qID();

            state._fsp--;



            		prog.addAttribute(parameters, elem.getText(), attrName.getText(), returnType, implementedBy, doc);	
            	

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "attributeDef"



    // $ANTLR start "javaType"
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:115:1: javaType returns [String name] : n1= qID ( '<' n= javaType ( ',' n= javaType )* '>' )? ;
    public final String javaType() throws RecognitionException {
        String name = null;


        String n1 =null;

        String n =null;


        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:115:31: (n1= qID ( '<' n= javaType ( ',' n= javaType )* '>' )? )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:116:2: n1= qID ( '<' n= javaType ( ',' n= javaType )* '>' )?
            {
            pushFollow(FOLLOW_qID_in_javaType381);
            n1=qID();

            state._fsp--;


             name = n1; 

            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:116:25: ( '<' n= javaType ( ',' n= javaType )* '>' )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==15) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:116:26: '<' n= javaType ( ',' n= javaType )* '>'
                    {
                    match(input,15,FOLLOW_15_in_javaType386); 

                    pushFollow(FOLLOW_javaType_in_javaType390);
                    n=javaType();

                    state._fsp--;


                     name += "<" + n; 

                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:116:63: ( ',' n= javaType )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( (LA12_0==13) ) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:116:64: ',' n= javaType
                    	    {
                    	    match(input,13,FOLLOW_13_in_javaType395); 

                    	    pushFollow(FOLLOW_javaType_in_javaType399);
                    	    n=javaType();

                    	    state._fsp--;


                    	     name +=", " + n; 

                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);


                     name += ">"; 

                    match(input,17,FOLLOW_17_in_javaType408); 

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return name;
    }
    // $ANTLR end "javaType"



    // $ANTLR start "qID"
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:119:1: qID returns [String s] : n= ID ( '.' n= ID )* ;
    public final String qID() throws RecognitionException {
        String s = null;


        Token n=null;

        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:119:23: (n= ID ( '.' n= ID )* )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:119:25: n= ID ( '.' n= ID )*
            {
            n=(Token)match(input,ID,FOLLOW_ID_in_qID424); 

            s =n.getText();

            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:119:48: ( '.' n= ID )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==14) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:119:49: '.' n= ID
            	    {
            	    match(input,14,FOLLOW_14_in_qID429); 

            	    n=(Token)match(input,ID,FOLLOW_ID_in_qID433); 

            	    s+="."+n.getText();

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return s;
    }
    // $ANTLR end "qID"

    // Delegated rules


 

    public static final BitSet FOLLOW_22_in_spec40 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_qID_in_spec44 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_18_in_spec50 = new BitSet(new long[]{0x0000000000080020L});
    public static final BitSet FOLLOW_element_in_spec53 = new BitSet(new long[]{0x0000000000080020L});
    public static final BitSet FOLLOW_19_in_spec58 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_attributeDef_in_spec61 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_EOF_in_spec66 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_contructorDef_in_element78 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listDef_in_element84 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_caseDef_in_element90 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_contructorDef109 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_contructorDef116 = new BitSet(new long[]{0x0000000000000820L});
    public static final BitSet FOLLOW_javaType_in_contructorDef121 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_contructorDef125 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_13_in_contructorDef133 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_javaType_in_contructorDef137 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_contructorDef141 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_11_in_contructorDef153 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_listDef167 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_12_in_listDef169 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_listDef173 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_caseDef194 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_16_in_caseDef201 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_choice_in_caseDef205 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_24_in_caseDef209 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_choice_in_caseDef213 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_ID_in_choice232 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_contructorDef_in_choice241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_attributeDef260 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_attributeDef262 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_attributeDef266 = new BitSet(new long[]{0x0000000000800500L});
    public static final BitSet FOLLOW_10_in_attributeDef274 = new BitSet(new long[]{0x0000000000000820L});
    public static final BitSet FOLLOW_javaType_in_attributeDef287 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_attributeDef291 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_13_in_attributeDef303 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_javaType_in_attributeDef307 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_attributeDef311 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_11_in_attributeDef332 = new BitSet(new long[]{0x0000000000800100L});
    public static final BitSet FOLLOW_STRVAL_in_attributeDef344 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_attributeDef348 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_javaType_in_attributeDef352 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_21_in_attributeDef354 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_20_in_attributeDef356 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_qID_in_attributeDef360 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qID_in_javaType381 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_15_in_javaType386 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_javaType_in_javaType390 = new BitSet(new long[]{0x0000000000022000L});
    public static final BitSet FOLLOW_13_in_javaType395 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_javaType_in_javaType399 = new BitSet(new long[]{0x0000000000022000L});
    public static final BitSet FOLLOW_17_in_javaType408 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_qID424 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_14_in_qID429 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_qID433 = new BitSet(new long[]{0x0000000000004002L});

}