// $ANTLR 3.4 /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g 2012-02-07 22:39:50

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "COMMENT", "ID", "ID_PART", "ID_START", "STRVAL", "WS", "'&'", "'('", "')'", "'*'", "','", "'.'", "'<'", "'='", "'>'", "'abstract syntax:'", "'attributes:'", "'by'", "'implemented'", "'package'", "'returns'", "'|'"
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
    public static final int T__25=25;
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
            match(input,23,FOLLOW_23_in_spec40); 

            pushFollow(FOLLOW_qID_in_spec44);
            p=qID();

            state._fsp--;



            	 prog = new Program(p);
            	

            match(input,19,FOLLOW_19_in_spec50); 

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


            match(input,20,FOLLOW_20_in_spec58); 

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
                case 13:
                    {
                    alt3=2;
                    }
                    break;
                case 11:
                    {
                    alt3=1;
                    }
                    break;
                case 17:
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
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:49:1: contructorDef[Program prog] returns [ConstructorDef c] : name= ID '(' ( paramDef[$c] ( ',' paramDef[$c] )* )? ')' ;
    public final ConstructorDef contructorDef(Program prog) throws RecognitionException {
        ConstructorDef c = null;


        Token name=null;

        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:49:55: (name= ID '(' ( paramDef[$c] ( ',' paramDef[$c] )* )? ')' )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:50:2: name= ID '(' ( paramDef[$c] ( ',' paramDef[$c] )* )? ')'
            {
            name=(Token)match(input,ID,FOLLOW_ID_in_contructorDef109); 


            		c = new ConstructorDef(name.getText());
            		prog.addConstructorDef(c);		
            	

            match(input,11,FOLLOW_11_in_contructorDef116); 

            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:55:6: ( paramDef[$c] ( ',' paramDef[$c] )* )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==ID||LA5_0==10) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:55:7: paramDef[$c] ( ',' paramDef[$c] )*
                    {
                    pushFollow(FOLLOW_paramDef_in_contructorDef119);
                    paramDef(c);

                    state._fsp--;


                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:55:20: ( ',' paramDef[$c] )*
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( (LA4_0==14) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:55:21: ',' paramDef[$c]
                    	    {
                    	    match(input,14,FOLLOW_14_in_contructorDef123); 

                    	    pushFollow(FOLLOW_paramDef_in_contructorDef125);
                    	    paramDef(c);

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop4;
                        }
                    } while (true);


                    }
                    break;

            }


            match(input,12,FOLLOW_12_in_contructorDef133); 

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



    // $ANTLR start "paramDef"
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:58:1: paramDef[ConstructorDef c] : ( '&' )? t= javaType n= ID ;
    public final void paramDef(ConstructorDef c) throws RecognitionException {
        Token n=null;
        String t =null;


        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:58:27: ( ( '&' )? t= javaType n= ID )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:59:2: ( '&' )? t= javaType n= ID
            {

            		boolean ref = false;
            	

            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:62:2: ( '&' )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==10) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:62:3: '&'
                    {
                    match(input,10,FOLLOW_10_in_paramDef149); 

                    ref = true;

                    }
                    break;

            }


            pushFollow(FOLLOW_javaType_in_paramDef159);
            t=javaType();

            state._fsp--;


            n=(Token)match(input,ID,FOLLOW_ID_in_paramDef163); 

             
            		c.addParam(ref, t, n.getText());
            	

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
    // $ANTLR end "paramDef"



    // $ANTLR start "listDef"
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:69:1: listDef[Program prog] : name= ID '*' of= ID ;
    public final void listDef(Program prog) throws RecognitionException {
        Token name=null;
        Token of=null;

        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:69:22: (name= ID '*' of= ID )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:70:2: name= ID '*' of= ID
            {
            name=(Token)match(input,ID,FOLLOW_ID_in_listDef181); 

            match(input,13,FOLLOW_13_in_listDef183); 

            of=(Token)match(input,ID,FOLLOW_ID_in_listDef187); 


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
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:76:1: caseDef[Program prog] returns [CaseDef caseDef] : name= ID '=' c= choice[prog, caseDef] ( '|' c= choice[prog, caseDef] )* ;
    public final CaseDef caseDef(Program prog) throws RecognitionException {
        CaseDef caseDef = null;


        Token name=null;

        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:76:48: (name= ID '=' c= choice[prog, caseDef] ( '|' c= choice[prog, caseDef] )* )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:77:2: name= ID '=' c= choice[prog, caseDef] ( '|' c= choice[prog, caseDef] )*
            {
            name=(Token)match(input,ID,FOLLOW_ID_in_caseDef208); 


            		caseDef = new CaseDef(name.getText());
            		prog.addCaseDef(caseDef);
            	

            match(input,17,FOLLOW_17_in_caseDef215); 

            pushFollow(FOLLOW_choice_in_caseDef219);
            choice(prog, caseDef);

            state._fsp--;


            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:82:30: ( '|' c= choice[prog, caseDef] )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==25) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:82:31: '|' c= choice[prog, caseDef]
            	    {
            	    match(input,25,FOLLOW_25_in_caseDef223); 

            	    pushFollow(FOLLOW_choice_in_caseDef227);
            	    choice(prog, caseDef);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop7;
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
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:85:1: choice[Program prog, CaseDef caseDef] : (name= ID |c= contructorDef[prog] );
    public final void choice(Program prog, CaseDef caseDef) throws RecognitionException {
        Token name=null;
        ConstructorDef c =null;


        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:85:38: (name= ID |c= contructorDef[prog] )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==ID) ) {
                int LA8_1 = input.LA(2);

                if ( (LA8_1==ID||LA8_1==20||LA8_1==25) ) {
                    alt8=1;
                }
                else if ( (LA8_1==11) ) {
                    alt8=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;

            }
            switch (alt8) {
                case 1 :
                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:86:4: name= ID
                    {
                    name=(Token)match(input,ID,FOLLOW_ID_in_choice246); 

                     caseDef.addAlternative(name.getText()); 

                    }
                    break;
                case 2 :
                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:87:4: c= contructorDef[prog]
                    {
                    pushFollow(FOLLOW_contructorDef_in_choice255);
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
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:90:1: attributeDef[Program prog] :elem= ID '.' attrName= ID ( '(' (t= javaType n= ID ( ',' t= javaType n= ID )* )? ')' )? (doc= STRVAL )? 'returns' returnType= javaType 'implemented' 'by' implementedBy= qID ;
    public final void attributeDef(Program prog) throws RecognitionException {
        Token elem=null;
        Token attrName=null;
        Token n=null;
        Token doc=null;
        String t =null;

        String returnType =null;

        String implementedBy =null;


        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:90:27: (elem= ID '.' attrName= ID ( '(' (t= javaType n= ID ( ',' t= javaType n= ID )* )? ')' )? (doc= STRVAL )? 'returns' returnType= javaType 'implemented' 'by' implementedBy= qID )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:91:2: elem= ID '.' attrName= ID ( '(' (t= javaType n= ID ( ',' t= javaType n= ID )* )? ')' )? (doc= STRVAL )? 'returns' returnType= javaType 'implemented' 'by' implementedBy= qID
            {

            		List<Parameter> parameters = null;
            	

            elem=(Token)match(input,ID,FOLLOW_ID_in_attributeDef274); 

            match(input,15,FOLLOW_15_in_attributeDef276); 

            attrName=(Token)match(input,ID,FOLLOW_ID_in_attributeDef280); 

            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:95:3: ( '(' (t= javaType n= ID ( ',' t= javaType n= ID )* )? ')' )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==11) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:96:3: '(' (t= javaType n= ID ( ',' t= javaType n= ID )* )? ')'
                    {
                    match(input,11,FOLLOW_11_in_attributeDef288); 


                    		 	parameters = new ArrayList<Parameter>();
                    		 

                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:100:4: (t= javaType n= ID ( ',' t= javaType n= ID )* )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==ID) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:100:5: t= javaType n= ID ( ',' t= javaType n= ID )*
                            {
                            pushFollow(FOLLOW_javaType_in_attributeDef301);
                            t=javaType();

                            state._fsp--;


                            n=(Token)match(input,ID,FOLLOW_ID_in_attributeDef305); 

                             
                            				parameters.add(new Parameter(t, n.getText()));
                            			

                            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:104:4: ( ',' t= javaType n= ID )*
                            loop9:
                            do {
                                int alt9=2;
                                int LA9_0 = input.LA(1);

                                if ( (LA9_0==14) ) {
                                    alt9=1;
                                }


                                switch (alt9) {
                            	case 1 :
                            	    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:104:5: ',' t= javaType n= ID
                            	    {
                            	    match(input,14,FOLLOW_14_in_attributeDef317); 

                            	    pushFollow(FOLLOW_javaType_in_attributeDef321);
                            	    t=javaType();

                            	    state._fsp--;


                            	    n=(Token)match(input,ID,FOLLOW_ID_in_attributeDef325); 


                            	    				parameters.add(new Parameter(t, n.getText()));
                            	    			

                            	    }
                            	    break;

                            	default :
                            	    break loop9;
                                }
                            } while (true);


                            }
                            break;

                    }


                    match(input,12,FOLLOW_12_in_attributeDef346); 

                    }
                    break;

            }


            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:112:3: (doc= STRVAL )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==STRVAL) ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:112:4: doc= STRVAL
                    {
                    doc=(Token)match(input,STRVAL,FOLLOW_STRVAL_in_attributeDef358); 

                    }
                    break;

            }


            match(input,24,FOLLOW_24_in_attributeDef362); 

            pushFollow(FOLLOW_javaType_in_attributeDef366);
            returnType=javaType();

            state._fsp--;


            match(input,22,FOLLOW_22_in_attributeDef368); 

            match(input,21,FOLLOW_21_in_attributeDef370); 

            pushFollow(FOLLOW_qID_in_attributeDef374);
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
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:118:1: javaType returns [String name] : n1= qID ( '<' n= javaType ( ',' n= javaType )* '>' )? ;
    public final String javaType() throws RecognitionException {
        String name = null;


        String n1 =null;

        String n =null;


        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:118:31: (n1= qID ( '<' n= javaType ( ',' n= javaType )* '>' )? )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:119:2: n1= qID ( '<' n= javaType ( ',' n= javaType )* '>' )?
            {
            pushFollow(FOLLOW_qID_in_javaType395);
            n1=qID();

            state._fsp--;


             name = n1; 

            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:119:25: ( '<' n= javaType ( ',' n= javaType )* '>' )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==16) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:119:26: '<' n= javaType ( ',' n= javaType )* '>'
                    {
                    match(input,16,FOLLOW_16_in_javaType400); 

                    pushFollow(FOLLOW_javaType_in_javaType404);
                    n=javaType();

                    state._fsp--;


                     name += "<" + n; 

                    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:119:63: ( ',' n= javaType )*
                    loop13:
                    do {
                        int alt13=2;
                        int LA13_0 = input.LA(1);

                        if ( (LA13_0==14) ) {
                            alt13=1;
                        }


                        switch (alt13) {
                    	case 1 :
                    	    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:119:64: ',' n= javaType
                    	    {
                    	    match(input,14,FOLLOW_14_in_javaType409); 

                    	    pushFollow(FOLLOW_javaType_in_javaType413);
                    	    n=javaType();

                    	    state._fsp--;


                    	     name +=", " + n; 

                    	    }
                    	    break;

                    	default :
                    	    break loop13;
                        }
                    } while (true);


                     name += ">"; 

                    match(input,18,FOLLOW_18_in_javaType422); 

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
    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:122:1: qID returns [String s] : n= ID ( '.' n= ID )* ;
    public final String qID() throws RecognitionException {
        String s = null;


        Token n=null;

        try {
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:122:23: (n= ID ( '.' n= ID )* )
            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:122:25: n= ID ( '.' n= ID )*
            {
            n=(Token)match(input,ID,FOLLOW_ID_in_qID438); 

            s =n.getText();

            // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:122:48: ( '.' n= ID )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==15) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // /home/peter/workspace/pscript-lang/parseq/src/de/peeeq/parseq/parser/ParseqAntlrParser.g:122:49: '.' n= ID
            	    {
            	    match(input,15,FOLLOW_15_in_qID443); 

            	    n=(Token)match(input,ID,FOLLOW_ID_in_qID447); 

            	    s+="."+n.getText();

            	    }
            	    break;

            	default :
            	    break loop15;
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


 

    public static final BitSet FOLLOW_23_in_spec40 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_qID_in_spec44 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_19_in_spec50 = new BitSet(new long[]{0x0000000000100020L});
    public static final BitSet FOLLOW_element_in_spec53 = new BitSet(new long[]{0x0000000000100020L});
    public static final BitSet FOLLOW_20_in_spec58 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_attributeDef_in_spec61 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_EOF_in_spec66 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_contructorDef_in_element78 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listDef_in_element84 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_caseDef_in_element90 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_contructorDef109 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_contructorDef116 = new BitSet(new long[]{0x0000000000001420L});
    public static final BitSet FOLLOW_paramDef_in_contructorDef119 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_14_in_contructorDef123 = new BitSet(new long[]{0x0000000000000420L});
    public static final BitSet FOLLOW_paramDef_in_contructorDef125 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_12_in_contructorDef133 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_10_in_paramDef149 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_javaType_in_paramDef159 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_paramDef163 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_listDef181 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_listDef183 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_listDef187 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_caseDef208 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_17_in_caseDef215 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_choice_in_caseDef219 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_25_in_caseDef223 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_choice_in_caseDef227 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_ID_in_choice246 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_contructorDef_in_choice255 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_attributeDef274 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_attributeDef276 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_attributeDef280 = new BitSet(new long[]{0x0000000001000900L});
    public static final BitSet FOLLOW_11_in_attributeDef288 = new BitSet(new long[]{0x0000000000001020L});
    public static final BitSet FOLLOW_javaType_in_attributeDef301 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_attributeDef305 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_14_in_attributeDef317 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_javaType_in_attributeDef321 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_attributeDef325 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_12_in_attributeDef346 = new BitSet(new long[]{0x0000000001000100L});
    public static final BitSet FOLLOW_STRVAL_in_attributeDef358 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_24_in_attributeDef362 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_javaType_in_attributeDef366 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_attributeDef368 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_21_in_attributeDef370 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_qID_in_attributeDef374 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qID_in_javaType395 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_16_in_javaType400 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_javaType_in_javaType404 = new BitSet(new long[]{0x0000000000044000L});
    public static final BitSet FOLLOW_14_in_javaType409 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_javaType_in_javaType413 = new BitSet(new long[]{0x0000000000044000L});
    public static final BitSet FOLLOW_18_in_javaType422 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_qID438 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_15_in_qID443 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_qID447 = new BitSet(new long[]{0x0000000000008002L});

}