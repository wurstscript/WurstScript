package de.peeeq.pscript.parser.antlr.internal; 

import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
import de.peeeq.pscript.services.PscriptGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalPscriptParser extends AbstractInternalAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_NL", "RULE_ID", "RULE_INT", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'package'", "'{'", "'}'", "'import'", "'.'", "'*'", "'init'", "'native'", "'('", "','", "')'", "'returns'", "'nativetype'", "'extends'", "'class'", "'val'", "'='", "'array'", "'['", "']'", "'function'", "'return'", "'if'", "'else'", "'while'", "'+='", "'-='", "'or'", "'and'", "'=='", "'!='", "'<='", "'<'", "'>='", "'>'", "'+'", "'-'", "'/'", "'%'", "'mod'", "'div'", "'not'", "'true'", "'false'"
    };
    public static final int RULE_ID=5;
    public static final int T__29=29;
    public static final int T__28=28;
    public static final int T__27=27;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int RULE_ANY_OTHER=11;
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int EOF=-1;
    public static final int T__55=55;
    public static final int T__19=19;
    public static final int T__16=16;
    public static final int T__51=51;
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
    public static final int RULE_NL=4;
    public static final int RULE_SL_COMMENT=9;
    public static final int RULE_ML_COMMENT=8;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int RULE_STRING=7;
    public static final int T__32=32;
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


        public InternalPscriptParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public InternalPscriptParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return InternalPscriptParser.tokenNames; }
    public String getGrammarFileName() { return "../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g"; }



     	private PscriptGrammarAccess grammarAccess;
     	
        public InternalPscriptParser(TokenStream input, PscriptGrammarAccess grammarAccess) {
            this(input);
            this.grammarAccess = grammarAccess;
            registerRules(grammarAccess.getGrammar());
        }
        
        @Override
        protected String getFirstRuleName() {
        	return "Program";	
       	}
       	
       	@Override
       	protected PscriptGrammarAccess getGrammarAccess() {
       		return grammarAccess;
       	}



    // $ANTLR start "entryRuleProgram"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:67:1: entryRuleProgram returns [EObject current=null] : iv_ruleProgram= ruleProgram EOF ;
    public final EObject entryRuleProgram() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleProgram = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:68:2: (iv_ruleProgram= ruleProgram EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:69:2: iv_ruleProgram= ruleProgram EOF
            {
             newCompositeNode(grammarAccess.getProgramRule()); 
            pushFollow(FOLLOW_ruleProgram_in_entryRuleProgram75);
            iv_ruleProgram=ruleProgram();

            state._fsp--;

             current =iv_ruleProgram; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleProgram85); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleProgram"


    // $ANTLR start "ruleProgram"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:76:1: ruleProgram returns [EObject current=null] : ( (this_NL_0= RULE_NL )* ( (lv_packages_1_0= rulePackageDeclaration ) ) ( (lv_packages_2_0= rulePackageDeclaration ) )* (this_NL_3= RULE_NL )* ) ;
    public final EObject ruleProgram() throws RecognitionException {
        EObject current = null;

        Token this_NL_0=null;
        Token this_NL_3=null;
        EObject lv_packages_1_0 = null;

        EObject lv_packages_2_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:79:28: ( ( (this_NL_0= RULE_NL )* ( (lv_packages_1_0= rulePackageDeclaration ) ) ( (lv_packages_2_0= rulePackageDeclaration ) )* (this_NL_3= RULE_NL )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:80:1: ( (this_NL_0= RULE_NL )* ( (lv_packages_1_0= rulePackageDeclaration ) ) ( (lv_packages_2_0= rulePackageDeclaration ) )* (this_NL_3= RULE_NL )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:80:1: ( (this_NL_0= RULE_NL )* ( (lv_packages_1_0= rulePackageDeclaration ) ) ( (lv_packages_2_0= rulePackageDeclaration ) )* (this_NL_3= RULE_NL )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:80:2: (this_NL_0= RULE_NL )* ( (lv_packages_1_0= rulePackageDeclaration ) ) ( (lv_packages_2_0= rulePackageDeclaration ) )* (this_NL_3= RULE_NL )*
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:80:2: (this_NL_0= RULE_NL )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==RULE_NL) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:80:3: this_NL_0= RULE_NL
            	    {
            	    this_NL_0=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleProgram122); 
            	     
            	        newLeafNode(this_NL_0, grammarAccess.getProgramAccess().getNLTerminalRuleCall_0()); 
            	        

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:84:3: ( (lv_packages_1_0= rulePackageDeclaration ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:85:1: (lv_packages_1_0= rulePackageDeclaration )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:85:1: (lv_packages_1_0= rulePackageDeclaration )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:86:3: lv_packages_1_0= rulePackageDeclaration
            {
             
            	        newCompositeNode(grammarAccess.getProgramAccess().getPackagesPackageDeclarationParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_rulePackageDeclaration_in_ruleProgram144);
            lv_packages_1_0=rulePackageDeclaration();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getProgramRule());
            	        }
                   		add(
                   			current, 
                   			"packages",
                    		lv_packages_1_0, 
                    		"PackageDeclaration");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:102:2: ( (lv_packages_2_0= rulePackageDeclaration ) )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==12) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:103:1: (lv_packages_2_0= rulePackageDeclaration )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:103:1: (lv_packages_2_0= rulePackageDeclaration )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:104:3: lv_packages_2_0= rulePackageDeclaration
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getProgramAccess().getPackagesPackageDeclarationParserRuleCall_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_rulePackageDeclaration_in_ruleProgram165);
            	    lv_packages_2_0=rulePackageDeclaration();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getProgramRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"packages",
            	            		lv_packages_2_0, 
            	            		"PackageDeclaration");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:120:3: (this_NL_3= RULE_NL )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==RULE_NL) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:120:4: this_NL_3= RULE_NL
            	    {
            	    this_NL_3=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleProgram178); 
            	     
            	        newLeafNode(this_NL_3, grammarAccess.getProgramAccess().getNLTerminalRuleCall_3()); 
            	        

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleProgram"


    // $ANTLR start "entryRulePackageDeclaration"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:132:1: entryRulePackageDeclaration returns [EObject current=null] : iv_rulePackageDeclaration= rulePackageDeclaration EOF ;
    public final EObject entryRulePackageDeclaration() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePackageDeclaration = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:133:2: (iv_rulePackageDeclaration= rulePackageDeclaration EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:134:2: iv_rulePackageDeclaration= rulePackageDeclaration EOF
            {
             newCompositeNode(grammarAccess.getPackageDeclarationRule()); 
            pushFollow(FOLLOW_rulePackageDeclaration_in_entryRulePackageDeclaration215);
            iv_rulePackageDeclaration=rulePackageDeclaration();

            state._fsp--;

             current =iv_rulePackageDeclaration; 
            match(input,EOF,FOLLOW_EOF_in_entryRulePackageDeclaration225); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRulePackageDeclaration"


    // $ANTLR start "rulePackageDeclaration"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:141:1: rulePackageDeclaration returns [EObject current=null] : ( () otherlv_1= 'package' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '{' this_NL_4= RULE_NL ( (this_NL_5= RULE_NL )* ( (lv_imports_6_0= ruleImport ) ) )* (this_NL_7= RULE_NL )* ( ( (lv_elements_8_0= ruleEntity ) ) (this_NL_9= RULE_NL )* )* otherlv_10= '}' this_NL_11= RULE_NL ) ;
    public final EObject rulePackageDeclaration() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token this_NL_4=null;
        Token this_NL_5=null;
        Token this_NL_7=null;
        Token this_NL_9=null;
        Token otherlv_10=null;
        Token this_NL_11=null;
        EObject lv_imports_6_0 = null;

        EObject lv_elements_8_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:144:28: ( ( () otherlv_1= 'package' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '{' this_NL_4= RULE_NL ( (this_NL_5= RULE_NL )* ( (lv_imports_6_0= ruleImport ) ) )* (this_NL_7= RULE_NL )* ( ( (lv_elements_8_0= ruleEntity ) ) (this_NL_9= RULE_NL )* )* otherlv_10= '}' this_NL_11= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:145:1: ( () otherlv_1= 'package' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '{' this_NL_4= RULE_NL ( (this_NL_5= RULE_NL )* ( (lv_imports_6_0= ruleImport ) ) )* (this_NL_7= RULE_NL )* ( ( (lv_elements_8_0= ruleEntity ) ) (this_NL_9= RULE_NL )* )* otherlv_10= '}' this_NL_11= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:145:1: ( () otherlv_1= 'package' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '{' this_NL_4= RULE_NL ( (this_NL_5= RULE_NL )* ( (lv_imports_6_0= ruleImport ) ) )* (this_NL_7= RULE_NL )* ( ( (lv_elements_8_0= ruleEntity ) ) (this_NL_9= RULE_NL )* )* otherlv_10= '}' this_NL_11= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:145:2: () otherlv_1= 'package' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '{' this_NL_4= RULE_NL ( (this_NL_5= RULE_NL )* ( (lv_imports_6_0= ruleImport ) ) )* (this_NL_7= RULE_NL )* ( ( (lv_elements_8_0= ruleEntity ) ) (this_NL_9= RULE_NL )* )* otherlv_10= '}' this_NL_11= RULE_NL
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:145:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:146:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getPackageDeclarationAccess().getPackageDeclarationAction_0(),
                        current);
                

            }

            otherlv_1=(Token)match(input,12,FOLLOW_12_in_rulePackageDeclaration271); 

                	newLeafNode(otherlv_1, grammarAccess.getPackageDeclarationAccess().getPackageKeyword_1());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:155:1: ( (lv_name_2_0= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:156:1: (lv_name_2_0= RULE_ID )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:156:1: (lv_name_2_0= RULE_ID )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:157:3: lv_name_2_0= RULE_ID
            {
            lv_name_2_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_rulePackageDeclaration288); 

            			newLeafNode(lv_name_2_0, grammarAccess.getPackageDeclarationAccess().getNameIDTerminalRuleCall_2_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getPackageDeclarationRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_2_0, 
                    		"ID");
            	    

            }


            }

            otherlv_3=(Token)match(input,13,FOLLOW_13_in_rulePackageDeclaration305); 

                	newLeafNode(otherlv_3, grammarAccess.getPackageDeclarationAccess().getLeftCurlyBracketKeyword_3());
                
            this_NL_4=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_rulePackageDeclaration316); 
             
                newLeafNode(this_NL_4, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_4()); 
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:181:1: ( (this_NL_5= RULE_NL )* ( (lv_imports_6_0= ruleImport ) ) )*
            loop5:
            do {
                int alt5=2;
                alt5 = dfa5.predict(input);
                switch (alt5) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:181:2: (this_NL_5= RULE_NL )* ( (lv_imports_6_0= ruleImport ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:181:2: (this_NL_5= RULE_NL )*
            	    loop4:
            	    do {
            	        int alt4=2;
            	        int LA4_0 = input.LA(1);

            	        if ( (LA4_0==RULE_NL) ) {
            	            alt4=1;
            	        }


            	        switch (alt4) {
            	    	case 1 :
            	    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:181:3: this_NL_5= RULE_NL
            	    	    {
            	    	    this_NL_5=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_rulePackageDeclaration328); 
            	    	     
            	    	        newLeafNode(this_NL_5, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_5_0()); 
            	    	        

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop4;
            	        }
            	    } while (true);

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:185:3: ( (lv_imports_6_0= ruleImport ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:186:1: (lv_imports_6_0= ruleImport )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:186:1: (lv_imports_6_0= ruleImport )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:187:3: lv_imports_6_0= ruleImport
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getPackageDeclarationAccess().getImportsImportParserRuleCall_5_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleImport_in_rulePackageDeclaration350);
            	    lv_imports_6_0=ruleImport();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getPackageDeclarationRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"imports",
            	            		lv_imports_6_0, 
            	            		"Import");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:203:4: (this_NL_7= RULE_NL )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==RULE_NL) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:203:5: this_NL_7= RULE_NL
            	    {
            	    this_NL_7=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_rulePackageDeclaration364); 
            	     
            	        newLeafNode(this_NL_7, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_6()); 
            	        

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:207:3: ( ( (lv_elements_8_0= ruleEntity ) ) (this_NL_9= RULE_NL )* )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==RULE_ID||(LA8_0>=18 && LA8_0<=19)||LA8_0==24||(LA8_0>=26 && LA8_0<=27)||LA8_0==32) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:207:4: ( (lv_elements_8_0= ruleEntity ) ) (this_NL_9= RULE_NL )*
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:207:4: ( (lv_elements_8_0= ruleEntity ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:208:1: (lv_elements_8_0= ruleEntity )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:208:1: (lv_elements_8_0= ruleEntity )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:209:3: lv_elements_8_0= ruleEntity
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getPackageDeclarationAccess().getElementsEntityParserRuleCall_7_0_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleEntity_in_rulePackageDeclaration387);
            	    lv_elements_8_0=ruleEntity();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getPackageDeclarationRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"elements",
            	            		lv_elements_8_0, 
            	            		"Entity");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:225:2: (this_NL_9= RULE_NL )*
            	    loop7:
            	    do {
            	        int alt7=2;
            	        int LA7_0 = input.LA(1);

            	        if ( (LA7_0==RULE_NL) ) {
            	            alt7=1;
            	        }


            	        switch (alt7) {
            	    	case 1 :
            	    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:225:3: this_NL_9= RULE_NL
            	    	    {
            	    	    this_NL_9=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_rulePackageDeclaration399); 
            	    	     
            	    	        newLeafNode(this_NL_9, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_7_1()); 
            	    	        

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop7;
            	        }
            	    } while (true);


            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            otherlv_10=(Token)match(input,14,FOLLOW_14_in_rulePackageDeclaration414); 

                	newLeafNode(otherlv_10, grammarAccess.getPackageDeclarationAccess().getRightCurlyBracketKeyword_8());
                
            this_NL_11=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_rulePackageDeclaration425); 
             
                newLeafNode(this_NL_11, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_9()); 
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "rulePackageDeclaration"


    // $ANTLR start "entryRuleImport"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:245:1: entryRuleImport returns [EObject current=null] : iv_ruleImport= ruleImport EOF ;
    public final EObject entryRuleImport() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleImport = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:246:2: (iv_ruleImport= ruleImport EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:247:2: iv_ruleImport= ruleImport EOF
            {
             newCompositeNode(grammarAccess.getImportRule()); 
            pushFollow(FOLLOW_ruleImport_in_entryRuleImport460);
            iv_ruleImport=ruleImport();

            state._fsp--;

             current =iv_ruleImport; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleImport470); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleImport"


    // $ANTLR start "ruleImport"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:254:1: ruleImport returns [EObject current=null] : (otherlv_0= 'import' ( (lv_importedNamespace_1_0= ruleImportString ) ) this_NL_2= RULE_NL ) ;
    public final EObject ruleImport() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token this_NL_2=null;
        AntlrDatatypeRuleToken lv_importedNamespace_1_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:257:28: ( (otherlv_0= 'import' ( (lv_importedNamespace_1_0= ruleImportString ) ) this_NL_2= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:258:1: (otherlv_0= 'import' ( (lv_importedNamespace_1_0= ruleImportString ) ) this_NL_2= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:258:1: (otherlv_0= 'import' ( (lv_importedNamespace_1_0= ruleImportString ) ) this_NL_2= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:258:3: otherlv_0= 'import' ( (lv_importedNamespace_1_0= ruleImportString ) ) this_NL_2= RULE_NL
            {
            otherlv_0=(Token)match(input,15,FOLLOW_15_in_ruleImport507); 

                	newLeafNode(otherlv_0, grammarAccess.getImportAccess().getImportKeyword_0());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:262:1: ( (lv_importedNamespace_1_0= ruleImportString ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:263:1: (lv_importedNamespace_1_0= ruleImportString )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:263:1: (lv_importedNamespace_1_0= ruleImportString )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:264:3: lv_importedNamespace_1_0= ruleImportString
            {
             
            	        newCompositeNode(grammarAccess.getImportAccess().getImportedNamespaceImportStringParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleImportString_in_ruleImport528);
            lv_importedNamespace_1_0=ruleImportString();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getImportRule());
            	        }
                   		set(
                   			current, 
                   			"importedNamespace",
                    		lv_importedNamespace_1_0, 
                    		"ImportString");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            this_NL_2=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleImport539); 
             
                newLeafNode(this_NL_2, grammarAccess.getImportAccess().getNLTerminalRuleCall_2()); 
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleImport"


    // $ANTLR start "entryRuleImportString"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:292:1: entryRuleImportString returns [String current=null] : iv_ruleImportString= ruleImportString EOF ;
    public final String entryRuleImportString() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleImportString = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:293:2: (iv_ruleImportString= ruleImportString EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:294:2: iv_ruleImportString= ruleImportString EOF
            {
             newCompositeNode(grammarAccess.getImportStringRule()); 
            pushFollow(FOLLOW_ruleImportString_in_entryRuleImportString575);
            iv_ruleImportString=ruleImportString();

            state._fsp--;

             current =iv_ruleImportString.getText(); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleImportString586); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleImportString"


    // $ANTLR start "ruleImportString"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:301:1: ruleImportString returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_ID_0= RULE_ID kw= '.' (kw= '*' | this_ID_3= RULE_ID ) ) ;
    public final AntlrDatatypeRuleToken ruleImportString() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_ID_0=null;
        Token kw=null;
        Token this_ID_3=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:304:28: ( (this_ID_0= RULE_ID kw= '.' (kw= '*' | this_ID_3= RULE_ID ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:305:1: (this_ID_0= RULE_ID kw= '.' (kw= '*' | this_ID_3= RULE_ID ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:305:1: (this_ID_0= RULE_ID kw= '.' (kw= '*' | this_ID_3= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:305:6: this_ID_0= RULE_ID kw= '.' (kw= '*' | this_ID_3= RULE_ID )
            {
            this_ID_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleImportString626); 

            		current.merge(this_ID_0);
                
             
                newLeafNode(this_ID_0, grammarAccess.getImportStringAccess().getIDTerminalRuleCall_0()); 
                
            kw=(Token)match(input,16,FOLLOW_16_in_ruleImportString644); 

                    current.merge(kw);
                    newLeafNode(kw, grammarAccess.getImportStringAccess().getFullStopKeyword_1()); 
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:318:1: (kw= '*' | this_ID_3= RULE_ID )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==17) ) {
                alt9=1;
            }
            else if ( (LA9_0==RULE_ID) ) {
                alt9=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:319:2: kw= '*'
                    {
                    kw=(Token)match(input,17,FOLLOW_17_in_ruleImportString658); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getImportStringAccess().getAsteriskKeyword_2_0()); 
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:325:10: this_ID_3= RULE_ID
                    {
                    this_ID_3=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleImportString679); 

                    		current.merge(this_ID_3);
                        
                     
                        newLeafNode(this_ID_3, grammarAccess.getImportStringAccess().getIDTerminalRuleCall_2_1()); 
                        

                    }
                    break;

            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleImportString"


    // $ANTLR start "entryRuleEntity"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:340:1: entryRuleEntity returns [EObject current=null] : iv_ruleEntity= ruleEntity EOF ;
    public final EObject entryRuleEntity() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEntity = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:341:2: (iv_ruleEntity= ruleEntity EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:342:2: iv_ruleEntity= ruleEntity EOF
            {
             newCompositeNode(grammarAccess.getEntityRule()); 
            pushFollow(FOLLOW_ruleEntity_in_entryRuleEntity725);
            iv_ruleEntity=ruleEntity();

            state._fsp--;

             current =iv_ruleEntity; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleEntity735); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleEntity"


    // $ANTLR start "ruleEntity"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:349:1: ruleEntity returns [EObject current=null] : (this_TypeDef_0= ruleTypeDef | this_FuncDef_1= ruleFuncDef | this_VarDef_2= ruleVarDef | this_InitBlock_3= ruleInitBlock | this_NativeFunc_4= ruleNativeFunc ) ;
    public final EObject ruleEntity() throws RecognitionException {
        EObject current = null;

        EObject this_TypeDef_0 = null;

        EObject this_FuncDef_1 = null;

        EObject this_VarDef_2 = null;

        EObject this_InitBlock_3 = null;

        EObject this_NativeFunc_4 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:352:28: ( (this_TypeDef_0= ruleTypeDef | this_FuncDef_1= ruleFuncDef | this_VarDef_2= ruleVarDef | this_InitBlock_3= ruleInitBlock | this_NativeFunc_4= ruleNativeFunc ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:353:1: (this_TypeDef_0= ruleTypeDef | this_FuncDef_1= ruleFuncDef | this_VarDef_2= ruleVarDef | this_InitBlock_3= ruleInitBlock | this_NativeFunc_4= ruleNativeFunc )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:353:1: (this_TypeDef_0= ruleTypeDef | this_FuncDef_1= ruleFuncDef | this_VarDef_2= ruleVarDef | this_InitBlock_3= ruleInitBlock | this_NativeFunc_4= ruleNativeFunc )
            int alt10=5;
            switch ( input.LA(1) ) {
            case 24:
            case 26:
                {
                alt10=1;
                }
                break;
            case 32:
                {
                alt10=2;
                }
                break;
            case RULE_ID:
            case 27:
                {
                alt10=3;
                }
                break;
            case 18:
                {
                alt10=4;
                }
                break;
            case 19:
                {
                alt10=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }

            switch (alt10) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:354:5: this_TypeDef_0= ruleTypeDef
                    {
                     
                            newCompositeNode(grammarAccess.getEntityAccess().getTypeDefParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleTypeDef_in_ruleEntity782);
                    this_TypeDef_0=ruleTypeDef();

                    state._fsp--;

                     
                            current = this_TypeDef_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:364:5: this_FuncDef_1= ruleFuncDef
                    {
                     
                            newCompositeNode(grammarAccess.getEntityAccess().getFuncDefParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleFuncDef_in_ruleEntity809);
                    this_FuncDef_1=ruleFuncDef();

                    state._fsp--;

                     
                            current = this_FuncDef_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:374:5: this_VarDef_2= ruleVarDef
                    {
                     
                            newCompositeNode(grammarAccess.getEntityAccess().getVarDefParserRuleCall_2()); 
                        
                    pushFollow(FOLLOW_ruleVarDef_in_ruleEntity836);
                    this_VarDef_2=ruleVarDef();

                    state._fsp--;

                     
                            current = this_VarDef_2; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 4 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:384:5: this_InitBlock_3= ruleInitBlock
                    {
                     
                            newCompositeNode(grammarAccess.getEntityAccess().getInitBlockParserRuleCall_3()); 
                        
                    pushFollow(FOLLOW_ruleInitBlock_in_ruleEntity863);
                    this_InitBlock_3=ruleInitBlock();

                    state._fsp--;

                     
                            current = this_InitBlock_3; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 5 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:394:5: this_NativeFunc_4= ruleNativeFunc
                    {
                     
                            newCompositeNode(grammarAccess.getEntityAccess().getNativeFuncParserRuleCall_4()); 
                        
                    pushFollow(FOLLOW_ruleNativeFunc_in_ruleEntity890);
                    this_NativeFunc_4=ruleNativeFunc();

                    state._fsp--;

                     
                            current = this_NativeFunc_4; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleEntity"


    // $ANTLR start "entryRuleInitBlock"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:410:1: entryRuleInitBlock returns [EObject current=null] : iv_ruleInitBlock= ruleInitBlock EOF ;
    public final EObject entryRuleInitBlock() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleInitBlock = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:411:2: (iv_ruleInitBlock= ruleInitBlock EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:412:2: iv_ruleInitBlock= ruleInitBlock EOF
            {
             newCompositeNode(grammarAccess.getInitBlockRule()); 
            pushFollow(FOLLOW_ruleInitBlock_in_entryRuleInitBlock925);
            iv_ruleInitBlock=ruleInitBlock();

            state._fsp--;

             current =iv_ruleInitBlock; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleInitBlock935); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleInitBlock"


    // $ANTLR start "ruleInitBlock"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:419:1: ruleInitBlock returns [EObject current=null] : ( ( (lv_name_0_0= 'init' ) ) otherlv_1= '{' this_NL_2= RULE_NL ( (lv_body_3_0= ruleStatements ) ) otherlv_4= '}' this_NL_5= RULE_NL ) ;
    public final EObject ruleInitBlock() throws RecognitionException {
        EObject current = null;

        Token lv_name_0_0=null;
        Token otherlv_1=null;
        Token this_NL_2=null;
        Token otherlv_4=null;
        Token this_NL_5=null;
        EObject lv_body_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:422:28: ( ( ( (lv_name_0_0= 'init' ) ) otherlv_1= '{' this_NL_2= RULE_NL ( (lv_body_3_0= ruleStatements ) ) otherlv_4= '}' this_NL_5= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:423:1: ( ( (lv_name_0_0= 'init' ) ) otherlv_1= '{' this_NL_2= RULE_NL ( (lv_body_3_0= ruleStatements ) ) otherlv_4= '}' this_NL_5= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:423:1: ( ( (lv_name_0_0= 'init' ) ) otherlv_1= '{' this_NL_2= RULE_NL ( (lv_body_3_0= ruleStatements ) ) otherlv_4= '}' this_NL_5= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:423:2: ( (lv_name_0_0= 'init' ) ) otherlv_1= '{' this_NL_2= RULE_NL ( (lv_body_3_0= ruleStatements ) ) otherlv_4= '}' this_NL_5= RULE_NL
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:423:2: ( (lv_name_0_0= 'init' ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:424:1: (lv_name_0_0= 'init' )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:424:1: (lv_name_0_0= 'init' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:425:3: lv_name_0_0= 'init'
            {
            lv_name_0_0=(Token)match(input,18,FOLLOW_18_in_ruleInitBlock978); 

                    newLeafNode(lv_name_0_0, grammarAccess.getInitBlockAccess().getNameInitKeyword_0_0());
                

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getInitBlockRule());
            	        }
                   		setWithLastConsumed(current, "name", lv_name_0_0, "init");
            	    

            }


            }

            otherlv_1=(Token)match(input,13,FOLLOW_13_in_ruleInitBlock1003); 

                	newLeafNode(otherlv_1, grammarAccess.getInitBlockAccess().getLeftCurlyBracketKeyword_1());
                
            this_NL_2=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleInitBlock1014); 
             
                newLeafNode(this_NL_2, grammarAccess.getInitBlockAccess().getNLTerminalRuleCall_2()); 
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:446:1: ( (lv_body_3_0= ruleStatements ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:447:1: (lv_body_3_0= ruleStatements )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:447:1: (lv_body_3_0= ruleStatements )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:448:3: lv_body_3_0= ruleStatements
            {
             
            	        newCompositeNode(grammarAccess.getInitBlockAccess().getBodyStatementsParserRuleCall_3_0()); 
            	    
            pushFollow(FOLLOW_ruleStatements_in_ruleInitBlock1034);
            lv_body_3_0=ruleStatements();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getInitBlockRule());
            	        }
                   		set(
                   			current, 
                   			"body",
                    		lv_body_3_0, 
                    		"Statements");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_4=(Token)match(input,14,FOLLOW_14_in_ruleInitBlock1046); 

                	newLeafNode(otherlv_4, grammarAccess.getInitBlockAccess().getRightCurlyBracketKeyword_4());
                
            this_NL_5=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleInitBlock1057); 
             
                newLeafNode(this_NL_5, grammarAccess.getInitBlockAccess().getNLTerminalRuleCall_5()); 
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleInitBlock"


    // $ANTLR start "entryRuleTypeDef"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:480:1: entryRuleTypeDef returns [EObject current=null] : iv_ruleTypeDef= ruleTypeDef EOF ;
    public final EObject entryRuleTypeDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeDef = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:481:2: (iv_ruleTypeDef= ruleTypeDef EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:482:2: iv_ruleTypeDef= ruleTypeDef EOF
            {
             newCompositeNode(grammarAccess.getTypeDefRule()); 
            pushFollow(FOLLOW_ruleTypeDef_in_entryRuleTypeDef1092);
            iv_ruleTypeDef=ruleTypeDef();

            state._fsp--;

             current =iv_ruleTypeDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleTypeDef1102); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleTypeDef"


    // $ANTLR start "ruleTypeDef"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:489:1: ruleTypeDef returns [EObject current=null] : (this_NativeType_0= ruleNativeType | this_ClassDef_1= ruleClassDef ) ;
    public final EObject ruleTypeDef() throws RecognitionException {
        EObject current = null;

        EObject this_NativeType_0 = null;

        EObject this_ClassDef_1 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:492:28: ( (this_NativeType_0= ruleNativeType | this_ClassDef_1= ruleClassDef ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:493:1: (this_NativeType_0= ruleNativeType | this_ClassDef_1= ruleClassDef )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:493:1: (this_NativeType_0= ruleNativeType | this_ClassDef_1= ruleClassDef )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==24) ) {
                alt11=1;
            }
            else if ( (LA11_0==26) ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:494:5: this_NativeType_0= ruleNativeType
                    {
                     
                            newCompositeNode(grammarAccess.getTypeDefAccess().getNativeTypeParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleNativeType_in_ruleTypeDef1149);
                    this_NativeType_0=ruleNativeType();

                    state._fsp--;

                     
                            current = this_NativeType_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:504:5: this_ClassDef_1= ruleClassDef
                    {
                     
                            newCompositeNode(grammarAccess.getTypeDefAccess().getClassDefParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleClassDef_in_ruleTypeDef1176);
                    this_ClassDef_1=ruleClassDef();

                    state._fsp--;

                     
                            current = this_ClassDef_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleTypeDef"


    // $ANTLR start "entryRuleNativeFunc"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:520:1: entryRuleNativeFunc returns [EObject current=null] : iv_ruleNativeFunc= ruleNativeFunc EOF ;
    public final EObject entryRuleNativeFunc() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleNativeFunc = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:521:2: (iv_ruleNativeFunc= ruleNativeFunc EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:522:2: iv_ruleNativeFunc= ruleNativeFunc EOF
            {
             newCompositeNode(grammarAccess.getNativeFuncRule()); 
            pushFollow(FOLLOW_ruleNativeFunc_in_entryRuleNativeFunc1211);
            iv_ruleNativeFunc=ruleNativeFunc();

            state._fsp--;

             current =iv_ruleNativeFunc; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleNativeFunc1221); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleNativeFunc"


    // $ANTLR start "ruleNativeFunc"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:529:1: ruleNativeFunc returns [EObject current=null] : ( () otherlv_1= 'native' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '(' ( ( (lv_parameters_4_0= ruleParameterDef ) ) (otherlv_5= ',' ( (lv_parameters_6_0= ruleParameterDef ) ) )* )? otherlv_7= ')' (otherlv_8= 'returns' ( (lv_type_9_0= ruleTypeExpr ) ) )? this_NL_10= RULE_NL ) ;
    public final EObject ruleNativeFunc() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_7=null;
        Token otherlv_8=null;
        Token this_NL_10=null;
        EObject lv_parameters_4_0 = null;

        EObject lv_parameters_6_0 = null;

        EObject lv_type_9_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:532:28: ( ( () otherlv_1= 'native' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '(' ( ( (lv_parameters_4_0= ruleParameterDef ) ) (otherlv_5= ',' ( (lv_parameters_6_0= ruleParameterDef ) ) )* )? otherlv_7= ')' (otherlv_8= 'returns' ( (lv_type_9_0= ruleTypeExpr ) ) )? this_NL_10= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:533:1: ( () otherlv_1= 'native' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '(' ( ( (lv_parameters_4_0= ruleParameterDef ) ) (otherlv_5= ',' ( (lv_parameters_6_0= ruleParameterDef ) ) )* )? otherlv_7= ')' (otherlv_8= 'returns' ( (lv_type_9_0= ruleTypeExpr ) ) )? this_NL_10= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:533:1: ( () otherlv_1= 'native' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '(' ( ( (lv_parameters_4_0= ruleParameterDef ) ) (otherlv_5= ',' ( (lv_parameters_6_0= ruleParameterDef ) ) )* )? otherlv_7= ')' (otherlv_8= 'returns' ( (lv_type_9_0= ruleTypeExpr ) ) )? this_NL_10= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:533:2: () otherlv_1= 'native' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '(' ( ( (lv_parameters_4_0= ruleParameterDef ) ) (otherlv_5= ',' ( (lv_parameters_6_0= ruleParameterDef ) ) )* )? otherlv_7= ')' (otherlv_8= 'returns' ( (lv_type_9_0= ruleTypeExpr ) ) )? this_NL_10= RULE_NL
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:533:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:534:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getNativeFuncAccess().getNativeFuncAction_0(),
                        current);
                

            }

            otherlv_1=(Token)match(input,19,FOLLOW_19_in_ruleNativeFunc1267); 

                	newLeafNode(otherlv_1, grammarAccess.getNativeFuncAccess().getNativeKeyword_1());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:543:1: ( (lv_name_2_0= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:544:1: (lv_name_2_0= RULE_ID )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:544:1: (lv_name_2_0= RULE_ID )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:545:3: lv_name_2_0= RULE_ID
            {
            lv_name_2_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleNativeFunc1284); 

            			newLeafNode(lv_name_2_0, grammarAccess.getNativeFuncAccess().getNameIDTerminalRuleCall_2_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getNativeFuncRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_2_0, 
                    		"ID");
            	    

            }


            }

            otherlv_3=(Token)match(input,20,FOLLOW_20_in_ruleNativeFunc1301); 

                	newLeafNode(otherlv_3, grammarAccess.getNativeFuncAccess().getLeftParenthesisKeyword_3());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:565:1: ( ( (lv_parameters_4_0= ruleParameterDef ) ) (otherlv_5= ',' ( (lv_parameters_6_0= ruleParameterDef ) ) )* )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==RULE_ID) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:565:2: ( (lv_parameters_4_0= ruleParameterDef ) ) (otherlv_5= ',' ( (lv_parameters_6_0= ruleParameterDef ) ) )*
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:565:2: ( (lv_parameters_4_0= ruleParameterDef ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:566:1: (lv_parameters_4_0= ruleParameterDef )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:566:1: (lv_parameters_4_0= ruleParameterDef )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:567:3: lv_parameters_4_0= ruleParameterDef
                    {
                     
                    	        newCompositeNode(grammarAccess.getNativeFuncAccess().getParametersParameterDefParserRuleCall_4_0_0()); 
                    	    
                    pushFollow(FOLLOW_ruleParameterDef_in_ruleNativeFunc1323);
                    lv_parameters_4_0=ruleParameterDef();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getNativeFuncRule());
                    	        }
                           		add(
                           			current, 
                           			"parameters",
                            		lv_parameters_4_0, 
                            		"ParameterDef");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:583:2: (otherlv_5= ',' ( (lv_parameters_6_0= ruleParameterDef ) ) )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( (LA12_0==21) ) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:583:4: otherlv_5= ',' ( (lv_parameters_6_0= ruleParameterDef ) )
                    	    {
                    	    otherlv_5=(Token)match(input,21,FOLLOW_21_in_ruleNativeFunc1336); 

                    	        	newLeafNode(otherlv_5, grammarAccess.getNativeFuncAccess().getCommaKeyword_4_1_0());
                    	        
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:587:1: ( (lv_parameters_6_0= ruleParameterDef ) )
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:588:1: (lv_parameters_6_0= ruleParameterDef )
                    	    {
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:588:1: (lv_parameters_6_0= ruleParameterDef )
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:589:3: lv_parameters_6_0= ruleParameterDef
                    	    {
                    	     
                    	    	        newCompositeNode(grammarAccess.getNativeFuncAccess().getParametersParameterDefParserRuleCall_4_1_1_0()); 
                    	    	    
                    	    pushFollow(FOLLOW_ruleParameterDef_in_ruleNativeFunc1357);
                    	    lv_parameters_6_0=ruleParameterDef();

                    	    state._fsp--;


                    	    	        if (current==null) {
                    	    	            current = createModelElementForParent(grammarAccess.getNativeFuncRule());
                    	    	        }
                    	           		add(
                    	           			current, 
                    	           			"parameters",
                    	            		lv_parameters_6_0, 
                    	            		"ParameterDef");
                    	    	        afterParserOrEnumRuleCall();
                    	    	    

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);


                    }
                    break;

            }

            otherlv_7=(Token)match(input,22,FOLLOW_22_in_ruleNativeFunc1373); 

                	newLeafNode(otherlv_7, grammarAccess.getNativeFuncAccess().getRightParenthesisKeyword_5());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:609:1: (otherlv_8= 'returns' ( (lv_type_9_0= ruleTypeExpr ) ) )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==23) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:609:3: otherlv_8= 'returns' ( (lv_type_9_0= ruleTypeExpr ) )
                    {
                    otherlv_8=(Token)match(input,23,FOLLOW_23_in_ruleNativeFunc1386); 

                        	newLeafNode(otherlv_8, grammarAccess.getNativeFuncAccess().getReturnsKeyword_6_0());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:613:1: ( (lv_type_9_0= ruleTypeExpr ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:614:1: (lv_type_9_0= ruleTypeExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:614:1: (lv_type_9_0= ruleTypeExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:615:3: lv_type_9_0= ruleTypeExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getNativeFuncAccess().getTypeTypeExprParserRuleCall_6_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleTypeExpr_in_ruleNativeFunc1407);
                    lv_type_9_0=ruleTypeExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getNativeFuncRule());
                    	        }
                           		set(
                           			current, 
                           			"type",
                            		lv_type_9_0, 
                            		"TypeExpr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }
                    break;

            }

            this_NL_10=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleNativeFunc1420); 
             
                newLeafNode(this_NL_10, grammarAccess.getNativeFuncAccess().getNLTerminalRuleCall_7()); 
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleNativeFunc"


    // $ANTLR start "entryRuleNativeType"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:643:1: entryRuleNativeType returns [EObject current=null] : iv_ruleNativeType= ruleNativeType EOF ;
    public final EObject entryRuleNativeType() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleNativeType = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:644:2: (iv_ruleNativeType= ruleNativeType EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:645:2: iv_ruleNativeType= ruleNativeType EOF
            {
             newCompositeNode(grammarAccess.getNativeTypeRule()); 
            pushFollow(FOLLOW_ruleNativeType_in_entryRuleNativeType1455);
            iv_ruleNativeType=ruleNativeType();

            state._fsp--;

             current =iv_ruleNativeType; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleNativeType1465); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleNativeType"


    // $ANTLR start "ruleNativeType"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:652:1: ruleNativeType returns [EObject current=null] : ( () otherlv_1= 'nativetype' ( (lv_name_2_0= RULE_ID ) ) (otherlv_3= 'extends' ( (lv_superName_4_0= ruleTypeExpr ) ) )? this_NL_5= RULE_NL ) ;
    public final EObject ruleNativeType() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token this_NL_5=null;
        EObject lv_superName_4_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:655:28: ( ( () otherlv_1= 'nativetype' ( (lv_name_2_0= RULE_ID ) ) (otherlv_3= 'extends' ( (lv_superName_4_0= ruleTypeExpr ) ) )? this_NL_5= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:656:1: ( () otherlv_1= 'nativetype' ( (lv_name_2_0= RULE_ID ) ) (otherlv_3= 'extends' ( (lv_superName_4_0= ruleTypeExpr ) ) )? this_NL_5= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:656:1: ( () otherlv_1= 'nativetype' ( (lv_name_2_0= RULE_ID ) ) (otherlv_3= 'extends' ( (lv_superName_4_0= ruleTypeExpr ) ) )? this_NL_5= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:656:2: () otherlv_1= 'nativetype' ( (lv_name_2_0= RULE_ID ) ) (otherlv_3= 'extends' ( (lv_superName_4_0= ruleTypeExpr ) ) )? this_NL_5= RULE_NL
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:656:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:657:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getNativeTypeAccess().getNativeTypeAction_0(),
                        current);
                

            }

            otherlv_1=(Token)match(input,24,FOLLOW_24_in_ruleNativeType1511); 

                	newLeafNode(otherlv_1, grammarAccess.getNativeTypeAccess().getNativetypeKeyword_1());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:666:1: ( (lv_name_2_0= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:667:1: (lv_name_2_0= RULE_ID )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:667:1: (lv_name_2_0= RULE_ID )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:668:3: lv_name_2_0= RULE_ID
            {
            lv_name_2_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleNativeType1528); 

            			newLeafNode(lv_name_2_0, grammarAccess.getNativeTypeAccess().getNameIDTerminalRuleCall_2_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getNativeTypeRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_2_0, 
                    		"ID");
            	    

            }


            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:684:2: (otherlv_3= 'extends' ( (lv_superName_4_0= ruleTypeExpr ) ) )?
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==25) ) {
                alt15=1;
            }
            switch (alt15) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:684:4: otherlv_3= 'extends' ( (lv_superName_4_0= ruleTypeExpr ) )
                    {
                    otherlv_3=(Token)match(input,25,FOLLOW_25_in_ruleNativeType1546); 

                        	newLeafNode(otherlv_3, grammarAccess.getNativeTypeAccess().getExtendsKeyword_3_0());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:688:1: ( (lv_superName_4_0= ruleTypeExpr ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:689:1: (lv_superName_4_0= ruleTypeExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:689:1: (lv_superName_4_0= ruleTypeExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:690:3: lv_superName_4_0= ruleTypeExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getNativeTypeAccess().getSuperNameTypeExprParserRuleCall_3_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleTypeExpr_in_ruleNativeType1567);
                    lv_superName_4_0=ruleTypeExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getNativeTypeRule());
                    	        }
                           		set(
                           			current, 
                           			"superName",
                            		lv_superName_4_0, 
                            		"TypeExpr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }
                    break;

            }

            this_NL_5=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleNativeType1580); 
             
                newLeafNode(this_NL_5, grammarAccess.getNativeTypeAccess().getNLTerminalRuleCall_4()); 
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleNativeType"


    // $ANTLR start "entryRuleClassDef"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:718:1: entryRuleClassDef returns [EObject current=null] : iv_ruleClassDef= ruleClassDef EOF ;
    public final EObject entryRuleClassDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleClassDef = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:719:2: (iv_ruleClassDef= ruleClassDef EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:720:2: iv_ruleClassDef= ruleClassDef EOF
            {
             newCompositeNode(grammarAccess.getClassDefRule()); 
            pushFollow(FOLLOW_ruleClassDef_in_entryRuleClassDef1615);
            iv_ruleClassDef=ruleClassDef();

            state._fsp--;

             current =iv_ruleClassDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleClassDef1625); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleClassDef"


    // $ANTLR start "ruleClassDef"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:727:1: ruleClassDef returns [EObject current=null] : ( () otherlv_1= 'class' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '{' this_NL_4= RULE_NL (this_NL_5= RULE_NL | ( (lv_members_6_0= ruleClassMember ) ) )* otherlv_7= '}' this_NL_8= RULE_NL ) ;
    public final EObject ruleClassDef() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token this_NL_4=null;
        Token this_NL_5=null;
        Token otherlv_7=null;
        Token this_NL_8=null;
        EObject lv_members_6_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:730:28: ( ( () otherlv_1= 'class' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '{' this_NL_4= RULE_NL (this_NL_5= RULE_NL | ( (lv_members_6_0= ruleClassMember ) ) )* otherlv_7= '}' this_NL_8= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:731:1: ( () otherlv_1= 'class' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '{' this_NL_4= RULE_NL (this_NL_5= RULE_NL | ( (lv_members_6_0= ruleClassMember ) ) )* otherlv_7= '}' this_NL_8= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:731:1: ( () otherlv_1= 'class' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '{' this_NL_4= RULE_NL (this_NL_5= RULE_NL | ( (lv_members_6_0= ruleClassMember ) ) )* otherlv_7= '}' this_NL_8= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:731:2: () otherlv_1= 'class' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '{' this_NL_4= RULE_NL (this_NL_5= RULE_NL | ( (lv_members_6_0= ruleClassMember ) ) )* otherlv_7= '}' this_NL_8= RULE_NL
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:731:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:732:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getClassDefAccess().getClassDefAction_0(),
                        current);
                

            }

            otherlv_1=(Token)match(input,26,FOLLOW_26_in_ruleClassDef1671); 

                	newLeafNode(otherlv_1, grammarAccess.getClassDefAccess().getClassKeyword_1());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:741:1: ( (lv_name_2_0= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:742:1: (lv_name_2_0= RULE_ID )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:742:1: (lv_name_2_0= RULE_ID )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:743:3: lv_name_2_0= RULE_ID
            {
            lv_name_2_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleClassDef1688); 

            			newLeafNode(lv_name_2_0, grammarAccess.getClassDefAccess().getNameIDTerminalRuleCall_2_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getClassDefRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_2_0, 
                    		"ID");
            	    

            }


            }

            otherlv_3=(Token)match(input,13,FOLLOW_13_in_ruleClassDef1705); 

                	newLeafNode(otherlv_3, grammarAccess.getClassDefAccess().getLeftCurlyBracketKeyword_3());
                
            this_NL_4=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleClassDef1716); 
             
                newLeafNode(this_NL_4, grammarAccess.getClassDefAccess().getNLTerminalRuleCall_4()); 
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:767:1: (this_NL_5= RULE_NL | ( (lv_members_6_0= ruleClassMember ) ) )*
            loop16:
            do {
                int alt16=3;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==RULE_NL) ) {
                    alt16=1;
                }
                else if ( (LA16_0==RULE_ID||LA16_0==27||LA16_0==32) ) {
                    alt16=2;
                }


                switch (alt16) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:767:2: this_NL_5= RULE_NL
            	    {
            	    this_NL_5=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleClassDef1727); 
            	     
            	        newLeafNode(this_NL_5, grammarAccess.getClassDefAccess().getNLTerminalRuleCall_5_0()); 
            	        

            	    }
            	    break;
            	case 2 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:772:6: ( (lv_members_6_0= ruleClassMember ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:772:6: ( (lv_members_6_0= ruleClassMember ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:773:1: (lv_members_6_0= ruleClassMember )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:773:1: (lv_members_6_0= ruleClassMember )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:774:3: lv_members_6_0= ruleClassMember
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getClassDefAccess().getMembersClassMemberParserRuleCall_5_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleClassMember_in_ruleClassDef1753);
            	    lv_members_6_0=ruleClassMember();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getClassDefRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"members",
            	            		lv_members_6_0, 
            	            		"ClassMember");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);

            otherlv_7=(Token)match(input,14,FOLLOW_14_in_ruleClassDef1767); 

                	newLeafNode(otherlv_7, grammarAccess.getClassDefAccess().getRightCurlyBracketKeyword_6());
                
            this_NL_8=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleClassDef1778); 
             
                newLeafNode(this_NL_8, grammarAccess.getClassDefAccess().getNLTerminalRuleCall_7()); 
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleClassDef"


    // $ANTLR start "entryRuleClassMember"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:806:1: entryRuleClassMember returns [EObject current=null] : iv_ruleClassMember= ruleClassMember EOF ;
    public final EObject entryRuleClassMember() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleClassMember = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:807:2: (iv_ruleClassMember= ruleClassMember EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:808:2: iv_ruleClassMember= ruleClassMember EOF
            {
             newCompositeNode(grammarAccess.getClassMemberRule()); 
            pushFollow(FOLLOW_ruleClassMember_in_entryRuleClassMember1813);
            iv_ruleClassMember=ruleClassMember();

            state._fsp--;

             current =iv_ruleClassMember; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleClassMember1823); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleClassMember"


    // $ANTLR start "ruleClassMember"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:815:1: ruleClassMember returns [EObject current=null] : (this_VarDef_0= ruleVarDef | this_FuncDef_1= ruleFuncDef ) ;
    public final EObject ruleClassMember() throws RecognitionException {
        EObject current = null;

        EObject this_VarDef_0 = null;

        EObject this_FuncDef_1 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:818:28: ( (this_VarDef_0= ruleVarDef | this_FuncDef_1= ruleFuncDef ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:819:1: (this_VarDef_0= ruleVarDef | this_FuncDef_1= ruleFuncDef )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:819:1: (this_VarDef_0= ruleVarDef | this_FuncDef_1= ruleFuncDef )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==RULE_ID||LA17_0==27) ) {
                alt17=1;
            }
            else if ( (LA17_0==32) ) {
                alt17=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:820:5: this_VarDef_0= ruleVarDef
                    {
                     
                            newCompositeNode(grammarAccess.getClassMemberAccess().getVarDefParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleVarDef_in_ruleClassMember1870);
                    this_VarDef_0=ruleVarDef();

                    state._fsp--;

                     
                            current = this_VarDef_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:830:5: this_FuncDef_1= ruleFuncDef
                    {
                     
                            newCompositeNode(grammarAccess.getClassMemberAccess().getFuncDefParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleFuncDef_in_ruleClassMember1897);
                    this_FuncDef_1=ruleFuncDef();

                    state._fsp--;

                     
                            current = this_FuncDef_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleClassMember"


    // $ANTLR start "entryRuleVarDef"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:846:1: entryRuleVarDef returns [EObject current=null] : iv_ruleVarDef= ruleVarDef EOF ;
    public final EObject entryRuleVarDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVarDef = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:847:2: (iv_ruleVarDef= ruleVarDef EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:848:2: iv_ruleVarDef= ruleVarDef EOF
            {
             newCompositeNode(grammarAccess.getVarDefRule()); 
            pushFollow(FOLLOW_ruleVarDef_in_entryRuleVarDef1932);
            iv_ruleVarDef=ruleVarDef();

            state._fsp--;

             current =iv_ruleVarDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleVarDef1942); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleVarDef"


    // $ANTLR start "ruleVarDef"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:855:1: ruleVarDef returns [EObject current=null] : ( () ( ( ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) ) | ( ( (lv_type_6_0= ruleTypeExpr ) ) ( (lv_name_7_0= RULE_ID ) ) (otherlv_8= '=' ( (lv_e_9_0= ruleExpr ) ) )? ) ) this_NL_10= RULE_NL ) ;
    public final EObject ruleVarDef() throws RecognitionException {
        EObject current = null;

        Token lv_constant_1_0=null;
        Token lv_name_3_0=null;
        Token otherlv_4=null;
        Token lv_name_7_0=null;
        Token otherlv_8=null;
        Token this_NL_10=null;
        EObject lv_type_2_0 = null;

        EObject lv_e_5_0 = null;

        EObject lv_type_6_0 = null;

        EObject lv_e_9_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:858:28: ( ( () ( ( ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) ) | ( ( (lv_type_6_0= ruleTypeExpr ) ) ( (lv_name_7_0= RULE_ID ) ) (otherlv_8= '=' ( (lv_e_9_0= ruleExpr ) ) )? ) ) this_NL_10= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:859:1: ( () ( ( ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) ) | ( ( (lv_type_6_0= ruleTypeExpr ) ) ( (lv_name_7_0= RULE_ID ) ) (otherlv_8= '=' ( (lv_e_9_0= ruleExpr ) ) )? ) ) this_NL_10= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:859:1: ( () ( ( ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) ) | ( ( (lv_type_6_0= ruleTypeExpr ) ) ( (lv_name_7_0= RULE_ID ) ) (otherlv_8= '=' ( (lv_e_9_0= ruleExpr ) ) )? ) ) this_NL_10= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:859:2: () ( ( ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) ) | ( ( (lv_type_6_0= ruleTypeExpr ) ) ( (lv_name_7_0= RULE_ID ) ) (otherlv_8= '=' ( (lv_e_9_0= ruleExpr ) ) )? ) ) this_NL_10= RULE_NL
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:859:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:860:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getVarDefAccess().getVarDefAction_0(),
                        current);
                

            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:865:2: ( ( ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) ) | ( ( (lv_type_6_0= ruleTypeExpr ) ) ( (lv_name_7_0= RULE_ID ) ) (otherlv_8= '=' ( (lv_e_9_0= ruleExpr ) ) )? ) )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==27) ) {
                alt20=1;
            }
            else if ( (LA20_0==RULE_ID) ) {
                alt20=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;
            }
            switch (alt20) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:865:3: ( ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:865:3: ( ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:865:4: ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:865:4: ( (lv_constant_1_0= 'val' ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:866:1: (lv_constant_1_0= 'val' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:866:1: (lv_constant_1_0= 'val' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:867:3: lv_constant_1_0= 'val'
                    {
                    lv_constant_1_0=(Token)match(input,27,FOLLOW_27_in_ruleVarDef1996); 

                            newLeafNode(lv_constant_1_0, grammarAccess.getVarDefAccess().getConstantValKeyword_1_0_0_0());
                        

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getVarDefRule());
                    	        }
                           		setWithLastConsumed(current, "constant", true, "val");
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:880:2: ( (lv_type_2_0= ruleTypeExpr ) )?
                    int alt18=2;
                    int LA18_0 = input.LA(1);

                    if ( (LA18_0==RULE_ID) ) {
                        int LA18_1 = input.LA(2);

                        if ( (LA18_1==RULE_ID||LA18_1==29) ) {
                            alt18=1;
                        }
                    }
                    switch (alt18) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:881:1: (lv_type_2_0= ruleTypeExpr )
                            {
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:881:1: (lv_type_2_0= ruleTypeExpr )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:882:3: lv_type_2_0= ruleTypeExpr
                            {
                             
                            	        newCompositeNode(grammarAccess.getVarDefAccess().getTypeTypeExprParserRuleCall_1_0_1_0()); 
                            	    
                            pushFollow(FOLLOW_ruleTypeExpr_in_ruleVarDef2030);
                            lv_type_2_0=ruleTypeExpr();

                            state._fsp--;


                            	        if (current==null) {
                            	            current = createModelElementForParent(grammarAccess.getVarDefRule());
                            	        }
                                   		set(
                                   			current, 
                                   			"type",
                                    		lv_type_2_0, 
                                    		"TypeExpr");
                            	        afterParserOrEnumRuleCall();
                            	    

                            }


                            }
                            break;

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:898:3: ( (lv_name_3_0= RULE_ID ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:899:1: (lv_name_3_0= RULE_ID )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:899:1: (lv_name_3_0= RULE_ID )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:900:3: lv_name_3_0= RULE_ID
                    {
                    lv_name_3_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleVarDef2048); 

                    			newLeafNode(lv_name_3_0, grammarAccess.getVarDefAccess().getNameIDTerminalRuleCall_1_0_2_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getVarDefRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"name",
                            		lv_name_3_0, 
                            		"ID");
                    	    

                    }


                    }

                    otherlv_4=(Token)match(input,28,FOLLOW_28_in_ruleVarDef2065); 

                        	newLeafNode(otherlv_4, grammarAccess.getVarDefAccess().getEqualsSignKeyword_1_0_3());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:920:1: ( (lv_e_5_0= ruleExpr ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:921:1: (lv_e_5_0= ruleExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:921:1: (lv_e_5_0= ruleExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:922:3: lv_e_5_0= ruleExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getVarDefAccess().getEExprParserRuleCall_1_0_4_0()); 
                    	    
                    pushFollow(FOLLOW_ruleExpr_in_ruleVarDef2086);
                    lv_e_5_0=ruleExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getVarDefRule());
                    	        }
                           		set(
                           			current, 
                           			"e",
                            		lv_e_5_0, 
                            		"Expr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:939:6: ( ( (lv_type_6_0= ruleTypeExpr ) ) ( (lv_name_7_0= RULE_ID ) ) (otherlv_8= '=' ( (lv_e_9_0= ruleExpr ) ) )? )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:939:6: ( ( (lv_type_6_0= ruleTypeExpr ) ) ( (lv_name_7_0= RULE_ID ) ) (otherlv_8= '=' ( (lv_e_9_0= ruleExpr ) ) )? )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:939:7: ( (lv_type_6_0= ruleTypeExpr ) ) ( (lv_name_7_0= RULE_ID ) ) (otherlv_8= '=' ( (lv_e_9_0= ruleExpr ) ) )?
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:939:7: ( (lv_type_6_0= ruleTypeExpr ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:940:1: (lv_type_6_0= ruleTypeExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:940:1: (lv_type_6_0= ruleTypeExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:941:3: lv_type_6_0= ruleTypeExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getVarDefAccess().getTypeTypeExprParserRuleCall_1_1_0_0()); 
                    	    
                    pushFollow(FOLLOW_ruleTypeExpr_in_ruleVarDef2115);
                    lv_type_6_0=ruleTypeExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getVarDefRule());
                    	        }
                           		set(
                           			current, 
                           			"type",
                            		lv_type_6_0, 
                            		"TypeExpr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:957:2: ( (lv_name_7_0= RULE_ID ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:958:1: (lv_name_7_0= RULE_ID )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:958:1: (lv_name_7_0= RULE_ID )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:959:3: lv_name_7_0= RULE_ID
                    {
                    lv_name_7_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleVarDef2132); 

                    			newLeafNode(lv_name_7_0, grammarAccess.getVarDefAccess().getNameIDTerminalRuleCall_1_1_1_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getVarDefRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"name",
                            		lv_name_7_0, 
                            		"ID");
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:975:2: (otherlv_8= '=' ( (lv_e_9_0= ruleExpr ) ) )?
                    int alt19=2;
                    int LA19_0 = input.LA(1);

                    if ( (LA19_0==28) ) {
                        alt19=1;
                    }
                    switch (alt19) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:975:4: otherlv_8= '=' ( (lv_e_9_0= ruleExpr ) )
                            {
                            otherlv_8=(Token)match(input,28,FOLLOW_28_in_ruleVarDef2150); 

                                	newLeafNode(otherlv_8, grammarAccess.getVarDefAccess().getEqualsSignKeyword_1_1_2_0());
                                
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:979:1: ( (lv_e_9_0= ruleExpr ) )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:980:1: (lv_e_9_0= ruleExpr )
                            {
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:980:1: (lv_e_9_0= ruleExpr )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:981:3: lv_e_9_0= ruleExpr
                            {
                             
                            	        newCompositeNode(grammarAccess.getVarDefAccess().getEExprParserRuleCall_1_1_2_1_0()); 
                            	    
                            pushFollow(FOLLOW_ruleExpr_in_ruleVarDef2171);
                            lv_e_9_0=ruleExpr();

                            state._fsp--;


                            	        if (current==null) {
                            	            current = createModelElementForParent(grammarAccess.getVarDefRule());
                            	        }
                                   		set(
                                   			current, 
                                   			"e",
                                    		lv_e_9_0, 
                                    		"Expr");
                            	        afterParserOrEnumRuleCall();
                            	    

                            }


                            }


                            }
                            break;

                    }


                    }


                    }
                    break;

            }

            this_NL_10=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleVarDef2186); 
             
                newLeafNode(this_NL_10, grammarAccess.getVarDefAccess().getNLTerminalRuleCall_2()); 
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleVarDef"


    // $ANTLR start "entryRuleTypeExpr"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1009:1: entryRuleTypeExpr returns [EObject current=null] : iv_ruleTypeExpr= ruleTypeExpr EOF ;
    public final EObject entryRuleTypeExpr() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeExpr = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1010:2: (iv_ruleTypeExpr= ruleTypeExpr EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1011:2: iv_ruleTypeExpr= ruleTypeExpr EOF
            {
             newCompositeNode(grammarAccess.getTypeExprRule()); 
            pushFollow(FOLLOW_ruleTypeExpr_in_entryRuleTypeExpr2221);
            iv_ruleTypeExpr=ruleTypeExpr();

            state._fsp--;

             current =iv_ruleTypeExpr; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleTypeExpr2231); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleTypeExpr"


    // $ANTLR start "ruleTypeExpr"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1018:1: ruleTypeExpr returns [EObject current=null] : ( ( (otherlv_0= RULE_ID ) ) ( ( (lv_array_1_0= 'array' ) ) (otherlv_2= '[' ( (lv_sizes_3_0= RULE_INT ) ) otherlv_4= ']' )* )? ) ;
    public final EObject ruleTypeExpr() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_array_1_0=null;
        Token otherlv_2=null;
        Token lv_sizes_3_0=null;
        Token otherlv_4=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1021:28: ( ( ( (otherlv_0= RULE_ID ) ) ( ( (lv_array_1_0= 'array' ) ) (otherlv_2= '[' ( (lv_sizes_3_0= RULE_INT ) ) otherlv_4= ']' )* )? ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1022:1: ( ( (otherlv_0= RULE_ID ) ) ( ( (lv_array_1_0= 'array' ) ) (otherlv_2= '[' ( (lv_sizes_3_0= RULE_INT ) ) otherlv_4= ']' )* )? )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1022:1: ( ( (otherlv_0= RULE_ID ) ) ( ( (lv_array_1_0= 'array' ) ) (otherlv_2= '[' ( (lv_sizes_3_0= RULE_INT ) ) otherlv_4= ']' )* )? )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1022:2: ( (otherlv_0= RULE_ID ) ) ( ( (lv_array_1_0= 'array' ) ) (otherlv_2= '[' ( (lv_sizes_3_0= RULE_INT ) ) otherlv_4= ']' )* )?
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1022:2: ( (otherlv_0= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1023:1: (otherlv_0= RULE_ID )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1023:1: (otherlv_0= RULE_ID )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1024:3: otherlv_0= RULE_ID
            {

            			if (current==null) {
            	            current = createModelElement(grammarAccess.getTypeExprRule());
            	        }
                    
            otherlv_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleTypeExpr2276); 

            		newLeafNode(otherlv_0, grammarAccess.getTypeExprAccess().getNameTypeDefCrossReference_0_0()); 
            	

            }


            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1035:2: ( ( (lv_array_1_0= 'array' ) ) (otherlv_2= '[' ( (lv_sizes_3_0= RULE_INT ) ) otherlv_4= ']' )* )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==29) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1035:3: ( (lv_array_1_0= 'array' ) ) (otherlv_2= '[' ( (lv_sizes_3_0= RULE_INT ) ) otherlv_4= ']' )*
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1035:3: ( (lv_array_1_0= 'array' ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1036:1: (lv_array_1_0= 'array' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1036:1: (lv_array_1_0= 'array' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1037:3: lv_array_1_0= 'array'
                    {
                    lv_array_1_0=(Token)match(input,29,FOLLOW_29_in_ruleTypeExpr2295); 

                            newLeafNode(lv_array_1_0, grammarAccess.getTypeExprAccess().getArrayArrayKeyword_1_0_0());
                        

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getTypeExprRule());
                    	        }
                           		setWithLastConsumed(current, "array", true, "array");
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1050:2: (otherlv_2= '[' ( (lv_sizes_3_0= RULE_INT ) ) otherlv_4= ']' )*
                    loop21:
                    do {
                        int alt21=2;
                        int LA21_0 = input.LA(1);

                        if ( (LA21_0==30) ) {
                            alt21=1;
                        }


                        switch (alt21) {
                    	case 1 :
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1050:4: otherlv_2= '[' ( (lv_sizes_3_0= RULE_INT ) ) otherlv_4= ']'
                    	    {
                    	    otherlv_2=(Token)match(input,30,FOLLOW_30_in_ruleTypeExpr2321); 

                    	        	newLeafNode(otherlv_2, grammarAccess.getTypeExprAccess().getLeftSquareBracketKeyword_1_1_0());
                    	        
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1054:1: ( (lv_sizes_3_0= RULE_INT ) )
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1055:1: (lv_sizes_3_0= RULE_INT )
                    	    {
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1055:1: (lv_sizes_3_0= RULE_INT )
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1056:3: lv_sizes_3_0= RULE_INT
                    	    {
                    	    lv_sizes_3_0=(Token)match(input,RULE_INT,FOLLOW_RULE_INT_in_ruleTypeExpr2338); 

                    	    			newLeafNode(lv_sizes_3_0, grammarAccess.getTypeExprAccess().getSizesINTTerminalRuleCall_1_1_1_0()); 
                    	    		

                    	    	        if (current==null) {
                    	    	            current = createModelElement(grammarAccess.getTypeExprRule());
                    	    	        }
                    	           		addWithLastConsumed(
                    	           			current, 
                    	           			"sizes",
                    	            		lv_sizes_3_0, 
                    	            		"INT");
                    	    	    

                    	    }


                    	    }

                    	    otherlv_4=(Token)match(input,31,FOLLOW_31_in_ruleTypeExpr2355); 

                    	        	newLeafNode(otherlv_4, grammarAccess.getTypeExprAccess().getRightSquareBracketKeyword_1_1_2());
                    	        

                    	    }
                    	    break;

                    	default :
                    	    break loop21;
                        }
                    } while (true);


                    }
                    break;

            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleTypeExpr"


    // $ANTLR start "entryRuleFuncDef"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1084:1: entryRuleFuncDef returns [EObject current=null] : iv_ruleFuncDef= ruleFuncDef EOF ;
    public final EObject entryRuleFuncDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFuncDef = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1085:2: (iv_ruleFuncDef= ruleFuncDef EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1086:2: iv_ruleFuncDef= ruleFuncDef EOF
            {
             newCompositeNode(grammarAccess.getFuncDefRule()); 
            pushFollow(FOLLOW_ruleFuncDef_in_entryRuleFuncDef2395);
            iv_ruleFuncDef=ruleFuncDef();

            state._fsp--;

             current =iv_ruleFuncDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleFuncDef2405); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleFuncDef"


    // $ANTLR start "ruleFuncDef"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1093:1: ruleFuncDef returns [EObject current=null] : (otherlv_0= 'function' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '(' ( ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )* )? otherlv_6= ')' (otherlv_7= 'returns' ( (lv_type_8_0= ruleTypeExpr ) ) )? otherlv_9= '{' this_NL_10= RULE_NL ( (lv_body_11_0= ruleStatements ) ) otherlv_12= '}' ) ;
    public final EObject ruleFuncDef() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        Token otherlv_7=null;
        Token otherlv_9=null;
        Token this_NL_10=null;
        Token otherlv_12=null;
        EObject lv_parameters_3_0 = null;

        EObject lv_parameters_5_0 = null;

        EObject lv_type_8_0 = null;

        EObject lv_body_11_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1096:28: ( (otherlv_0= 'function' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '(' ( ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )* )? otherlv_6= ')' (otherlv_7= 'returns' ( (lv_type_8_0= ruleTypeExpr ) ) )? otherlv_9= '{' this_NL_10= RULE_NL ( (lv_body_11_0= ruleStatements ) ) otherlv_12= '}' ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1097:1: (otherlv_0= 'function' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '(' ( ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )* )? otherlv_6= ')' (otherlv_7= 'returns' ( (lv_type_8_0= ruleTypeExpr ) ) )? otherlv_9= '{' this_NL_10= RULE_NL ( (lv_body_11_0= ruleStatements ) ) otherlv_12= '}' )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1097:1: (otherlv_0= 'function' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '(' ( ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )* )? otherlv_6= ')' (otherlv_7= 'returns' ( (lv_type_8_0= ruleTypeExpr ) ) )? otherlv_9= '{' this_NL_10= RULE_NL ( (lv_body_11_0= ruleStatements ) ) otherlv_12= '}' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1097:3: otherlv_0= 'function' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '(' ( ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )* )? otherlv_6= ')' (otherlv_7= 'returns' ( (lv_type_8_0= ruleTypeExpr ) ) )? otherlv_9= '{' this_NL_10= RULE_NL ( (lv_body_11_0= ruleStatements ) ) otherlv_12= '}'
            {
            otherlv_0=(Token)match(input,32,FOLLOW_32_in_ruleFuncDef2442); 

                	newLeafNode(otherlv_0, grammarAccess.getFuncDefAccess().getFunctionKeyword_0());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1101:1: ( (lv_name_1_0= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1102:1: (lv_name_1_0= RULE_ID )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1102:1: (lv_name_1_0= RULE_ID )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1103:3: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleFuncDef2459); 

            			newLeafNode(lv_name_1_0, grammarAccess.getFuncDefAccess().getNameIDTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getFuncDefRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_1_0, 
                    		"ID");
            	    

            }


            }

            otherlv_2=(Token)match(input,20,FOLLOW_20_in_ruleFuncDef2476); 

                	newLeafNode(otherlv_2, grammarAccess.getFuncDefAccess().getLeftParenthesisKeyword_2());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1123:1: ( ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )* )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==RULE_ID) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1123:2: ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )*
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1123:2: ( (lv_parameters_3_0= ruleParameterDef ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1124:1: (lv_parameters_3_0= ruleParameterDef )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1124:1: (lv_parameters_3_0= ruleParameterDef )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1125:3: lv_parameters_3_0= ruleParameterDef
                    {
                     
                    	        newCompositeNode(grammarAccess.getFuncDefAccess().getParametersParameterDefParserRuleCall_3_0_0()); 
                    	    
                    pushFollow(FOLLOW_ruleParameterDef_in_ruleFuncDef2498);
                    lv_parameters_3_0=ruleParameterDef();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getFuncDefRule());
                    	        }
                           		add(
                           			current, 
                           			"parameters",
                            		lv_parameters_3_0, 
                            		"ParameterDef");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1141:2: (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )*
                    loop23:
                    do {
                        int alt23=2;
                        int LA23_0 = input.LA(1);

                        if ( (LA23_0==21) ) {
                            alt23=1;
                        }


                        switch (alt23) {
                    	case 1 :
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1141:4: otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) )
                    	    {
                    	    otherlv_4=(Token)match(input,21,FOLLOW_21_in_ruleFuncDef2511); 

                    	        	newLeafNode(otherlv_4, grammarAccess.getFuncDefAccess().getCommaKeyword_3_1_0());
                    	        
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1145:1: ( (lv_parameters_5_0= ruleParameterDef ) )
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1146:1: (lv_parameters_5_0= ruleParameterDef )
                    	    {
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1146:1: (lv_parameters_5_0= ruleParameterDef )
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1147:3: lv_parameters_5_0= ruleParameterDef
                    	    {
                    	     
                    	    	        newCompositeNode(grammarAccess.getFuncDefAccess().getParametersParameterDefParserRuleCall_3_1_1_0()); 
                    	    	    
                    	    pushFollow(FOLLOW_ruleParameterDef_in_ruleFuncDef2532);
                    	    lv_parameters_5_0=ruleParameterDef();

                    	    state._fsp--;


                    	    	        if (current==null) {
                    	    	            current = createModelElementForParent(grammarAccess.getFuncDefRule());
                    	    	        }
                    	           		add(
                    	           			current, 
                    	           			"parameters",
                    	            		lv_parameters_5_0, 
                    	            		"ParameterDef");
                    	    	        afterParserOrEnumRuleCall();
                    	    	    

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop23;
                        }
                    } while (true);


                    }
                    break;

            }

            otherlv_6=(Token)match(input,22,FOLLOW_22_in_ruleFuncDef2548); 

                	newLeafNode(otherlv_6, grammarAccess.getFuncDefAccess().getRightParenthesisKeyword_4());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1167:1: (otherlv_7= 'returns' ( (lv_type_8_0= ruleTypeExpr ) ) )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==23) ) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1167:3: otherlv_7= 'returns' ( (lv_type_8_0= ruleTypeExpr ) )
                    {
                    otherlv_7=(Token)match(input,23,FOLLOW_23_in_ruleFuncDef2561); 

                        	newLeafNode(otherlv_7, grammarAccess.getFuncDefAccess().getReturnsKeyword_5_0());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1171:1: ( (lv_type_8_0= ruleTypeExpr ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1172:1: (lv_type_8_0= ruleTypeExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1172:1: (lv_type_8_0= ruleTypeExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1173:3: lv_type_8_0= ruleTypeExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getFuncDefAccess().getTypeTypeExprParserRuleCall_5_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleTypeExpr_in_ruleFuncDef2582);
                    lv_type_8_0=ruleTypeExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getFuncDefRule());
                    	        }
                           		set(
                           			current, 
                           			"type",
                            		lv_type_8_0, 
                            		"TypeExpr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }
                    break;

            }

            otherlv_9=(Token)match(input,13,FOLLOW_13_in_ruleFuncDef2596); 

                	newLeafNode(otherlv_9, grammarAccess.getFuncDefAccess().getLeftCurlyBracketKeyword_6());
                
            this_NL_10=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleFuncDef2607); 
             
                newLeafNode(this_NL_10, grammarAccess.getFuncDefAccess().getNLTerminalRuleCall_7()); 
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1197:1: ( (lv_body_11_0= ruleStatements ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1198:1: (lv_body_11_0= ruleStatements )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1198:1: (lv_body_11_0= ruleStatements )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1199:3: lv_body_11_0= ruleStatements
            {
             
            	        newCompositeNode(grammarAccess.getFuncDefAccess().getBodyStatementsParserRuleCall_8_0()); 
            	    
            pushFollow(FOLLOW_ruleStatements_in_ruleFuncDef2627);
            lv_body_11_0=ruleStatements();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getFuncDefRule());
            	        }
                   		set(
                   			current, 
                   			"body",
                    		lv_body_11_0, 
                    		"Statements");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_12=(Token)match(input,14,FOLLOW_14_in_ruleFuncDef2639); 

                	newLeafNode(otherlv_12, grammarAccess.getFuncDefAccess().getRightCurlyBracketKeyword_9());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleFuncDef"


    // $ANTLR start "entryRuleParameterDef"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1227:1: entryRuleParameterDef returns [EObject current=null] : iv_ruleParameterDef= ruleParameterDef EOF ;
    public final EObject entryRuleParameterDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleParameterDef = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1228:2: (iv_ruleParameterDef= ruleParameterDef EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1229:2: iv_ruleParameterDef= ruleParameterDef EOF
            {
             newCompositeNode(grammarAccess.getParameterDefRule()); 
            pushFollow(FOLLOW_ruleParameterDef_in_entryRuleParameterDef2675);
            iv_ruleParameterDef=ruleParameterDef();

            state._fsp--;

             current =iv_ruleParameterDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleParameterDef2685); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleParameterDef"


    // $ANTLR start "ruleParameterDef"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1236:1: ruleParameterDef returns [EObject current=null] : ( () ( (lv_type_1_0= ruleTypeExpr ) ) ( (lv_name_2_0= RULE_ID ) ) ) ;
    public final EObject ruleParameterDef() throws RecognitionException {
        EObject current = null;

        Token lv_name_2_0=null;
        EObject lv_type_1_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1239:28: ( ( () ( (lv_type_1_0= ruleTypeExpr ) ) ( (lv_name_2_0= RULE_ID ) ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1240:1: ( () ( (lv_type_1_0= ruleTypeExpr ) ) ( (lv_name_2_0= RULE_ID ) ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1240:1: ( () ( (lv_type_1_0= ruleTypeExpr ) ) ( (lv_name_2_0= RULE_ID ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1240:2: () ( (lv_type_1_0= ruleTypeExpr ) ) ( (lv_name_2_0= RULE_ID ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1240:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1241:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getParameterDefAccess().getParameterDefAction_0(),
                        current);
                

            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1246:2: ( (lv_type_1_0= ruleTypeExpr ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1247:1: (lv_type_1_0= ruleTypeExpr )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1247:1: (lv_type_1_0= ruleTypeExpr )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1248:3: lv_type_1_0= ruleTypeExpr
            {
             
            	        newCompositeNode(grammarAccess.getParameterDefAccess().getTypeTypeExprParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleTypeExpr_in_ruleParameterDef2740);
            lv_type_1_0=ruleTypeExpr();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getParameterDefRule());
            	        }
                   		set(
                   			current, 
                   			"type",
                    		lv_type_1_0, 
                    		"TypeExpr");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1264:2: ( (lv_name_2_0= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1265:1: (lv_name_2_0= RULE_ID )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1265:1: (lv_name_2_0= RULE_ID )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1266:3: lv_name_2_0= RULE_ID
            {
            lv_name_2_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleParameterDef2757); 

            			newLeafNode(lv_name_2_0, grammarAccess.getParameterDefAccess().getNameIDTerminalRuleCall_2_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getParameterDefRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_2_0, 
                    		"ID");
            	    

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleParameterDef"


    // $ANTLR start "entryRuleStatements"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1290:1: entryRuleStatements returns [EObject current=null] : iv_ruleStatements= ruleStatements EOF ;
    public final EObject entryRuleStatements() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatements = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1291:2: (iv_ruleStatements= ruleStatements EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1292:2: iv_ruleStatements= ruleStatements EOF
            {
             newCompositeNode(grammarAccess.getStatementsRule()); 
            pushFollow(FOLLOW_ruleStatements_in_entryRuleStatements2798);
            iv_ruleStatements=ruleStatements();

            state._fsp--;

             current =iv_ruleStatements; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStatements2808); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStatements"


    // $ANTLR start "ruleStatements"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1299:1: ruleStatements returns [EObject current=null] : ( () (this_NL_1= RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )* ) ;
    public final EObject ruleStatements() throws RecognitionException {
        EObject current = null;

        Token this_NL_1=null;
        EObject lv_statements_2_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1302:28: ( ( () (this_NL_1= RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1303:1: ( () (this_NL_1= RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1303:1: ( () (this_NL_1= RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1303:2: () (this_NL_1= RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )*
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1303:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1304:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getStatementsAccess().getStatementsAction_0(),
                        current);
                

            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1309:2: (this_NL_1= RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )*
            loop26:
            do {
                int alt26=3;
                int LA26_0 = input.LA(1);

                if ( (LA26_0==RULE_NL) ) {
                    alt26=1;
                }
                else if ( ((LA26_0>=RULE_ID && LA26_0<=RULE_STRING)||LA26_0==20||LA26_0==27||(LA26_0>=32 && LA26_0<=34)||LA26_0==36||(LA26_0>=47 && LA26_0<=48)||(LA26_0>=53 && LA26_0<=55)) ) {
                    alt26=2;
                }


                switch (alt26) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1309:3: this_NL_1= RULE_NL
            	    {
            	    this_NL_1=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStatements2854); 
            	     
            	        newLeafNode(this_NL_1, grammarAccess.getStatementsAccess().getNLTerminalRuleCall_1_0()); 
            	        

            	    }
            	    break;
            	case 2 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1314:6: ( (lv_statements_2_0= ruleStatement ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1314:6: ( (lv_statements_2_0= ruleStatement ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1315:1: (lv_statements_2_0= ruleStatement )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1315:1: (lv_statements_2_0= ruleStatement )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1316:3: lv_statements_2_0= ruleStatement
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getStatementsAccess().getStatementsStatementParserRuleCall_1_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleStatement_in_ruleStatements2880);
            	    lv_statements_2_0=ruleStatement();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getStatementsRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"statements",
            	            		lv_statements_2_0, 
            	            		"Statement");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop26;
                }
            } while (true);


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStatements"


    // $ANTLR start "entryRuleStatement"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1340:1: entryRuleStatement returns [EObject current=null] : iv_ruleStatement= ruleStatement EOF ;
    public final EObject entryRuleStatement() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatement = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1341:2: (iv_ruleStatement= ruleStatement EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1342:2: iv_ruleStatement= ruleStatement EOF
            {
             newCompositeNode(grammarAccess.getStatementRule()); 
            pushFollow(FOLLOW_ruleStatement_in_entryRuleStatement2918);
            iv_ruleStatement=ruleStatement();

            state._fsp--;

             current =iv_ruleStatement; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStatement2928); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStatement"


    // $ANTLR start "ruleStatement"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1349:1: ruleStatement returns [EObject current=null] : (this_StmtIf_0= ruleStmtIf | this_StmtWhile_1= ruleStmtWhile | this_LocalVarDef_2= ruleLocalVarDef | this_StmtSetOrCallOrVarDef_3= ruleStmtSetOrCallOrVarDef | this_StmtReturn_4= ruleStmtReturn ) ;
    public final EObject ruleStatement() throws RecognitionException {
        EObject current = null;

        EObject this_StmtIf_0 = null;

        EObject this_StmtWhile_1 = null;

        EObject this_LocalVarDef_2 = null;

        EObject this_StmtSetOrCallOrVarDef_3 = null;

        EObject this_StmtReturn_4 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1352:28: ( (this_StmtIf_0= ruleStmtIf | this_StmtWhile_1= ruleStmtWhile | this_LocalVarDef_2= ruleLocalVarDef | this_StmtSetOrCallOrVarDef_3= ruleStmtSetOrCallOrVarDef | this_StmtReturn_4= ruleStmtReturn ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1353:1: (this_StmtIf_0= ruleStmtIf | this_StmtWhile_1= ruleStmtWhile | this_LocalVarDef_2= ruleLocalVarDef | this_StmtSetOrCallOrVarDef_3= ruleStmtSetOrCallOrVarDef | this_StmtReturn_4= ruleStmtReturn )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1353:1: (this_StmtIf_0= ruleStmtIf | this_StmtWhile_1= ruleStmtWhile | this_LocalVarDef_2= ruleLocalVarDef | this_StmtSetOrCallOrVarDef_3= ruleStmtSetOrCallOrVarDef | this_StmtReturn_4= ruleStmtReturn )
            int alt27=5;
            switch ( input.LA(1) ) {
            case 34:
                {
                alt27=1;
                }
                break;
            case 36:
                {
                alt27=2;
                }
                break;
            case 27:
                {
                alt27=3;
                }
                break;
            case RULE_ID:
                {
                int LA27_4 = input.LA(2);

                if ( (LA27_4==RULE_NL||(LA27_4>=16 && LA27_4<=17)||LA27_4==20||LA27_4==28||LA27_4==30||(LA27_4>=37 && LA27_4<=52)) ) {
                    alt27=4;
                }
                else if ( (LA27_4==RULE_ID||LA27_4==29) ) {
                    alt27=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 27, 4, input);

                    throw nvae;
                }
                }
                break;
            case RULE_INT:
            case RULE_STRING:
            case 20:
            case 32:
            case 47:
            case 48:
            case 53:
            case 54:
            case 55:
                {
                alt27=4;
                }
                break;
            case 33:
                {
                alt27=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                throw nvae;
            }

            switch (alt27) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1354:5: this_StmtIf_0= ruleStmtIf
                    {
                     
                            newCompositeNode(grammarAccess.getStatementAccess().getStmtIfParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleStmtIf_in_ruleStatement2975);
                    this_StmtIf_0=ruleStmtIf();

                    state._fsp--;

                     
                            current = this_StmtIf_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1364:5: this_StmtWhile_1= ruleStmtWhile
                    {
                     
                            newCompositeNode(grammarAccess.getStatementAccess().getStmtWhileParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleStmtWhile_in_ruleStatement3002);
                    this_StmtWhile_1=ruleStmtWhile();

                    state._fsp--;

                     
                            current = this_StmtWhile_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1374:5: this_LocalVarDef_2= ruleLocalVarDef
                    {
                     
                            newCompositeNode(grammarAccess.getStatementAccess().getLocalVarDefParserRuleCall_2()); 
                        
                    pushFollow(FOLLOW_ruleLocalVarDef_in_ruleStatement3029);
                    this_LocalVarDef_2=ruleLocalVarDef();

                    state._fsp--;

                     
                            current = this_LocalVarDef_2; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 4 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1384:5: this_StmtSetOrCallOrVarDef_3= ruleStmtSetOrCallOrVarDef
                    {
                     
                            newCompositeNode(grammarAccess.getStatementAccess().getStmtSetOrCallOrVarDefParserRuleCall_3()); 
                        
                    pushFollow(FOLLOW_ruleStmtSetOrCallOrVarDef_in_ruleStatement3056);
                    this_StmtSetOrCallOrVarDef_3=ruleStmtSetOrCallOrVarDef();

                    state._fsp--;

                     
                            current = this_StmtSetOrCallOrVarDef_3; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 5 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1394:5: this_StmtReturn_4= ruleStmtReturn
                    {
                     
                            newCompositeNode(grammarAccess.getStatementAccess().getStmtReturnParserRuleCall_4()); 
                        
                    pushFollow(FOLLOW_ruleStmtReturn_in_ruleStatement3083);
                    this_StmtReturn_4=ruleStmtReturn();

                    state._fsp--;

                     
                            current = this_StmtReturn_4; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStatement"


    // $ANTLR start "entryRuleStmtReturn"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1410:1: entryRuleStmtReturn returns [EObject current=null] : iv_ruleStmtReturn= ruleStmtReturn EOF ;
    public final EObject entryRuleStmtReturn() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStmtReturn = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1411:2: (iv_ruleStmtReturn= ruleStmtReturn EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1412:2: iv_ruleStmtReturn= ruleStmtReturn EOF
            {
             newCompositeNode(grammarAccess.getStmtReturnRule()); 
            pushFollow(FOLLOW_ruleStmtReturn_in_entryRuleStmtReturn3118);
            iv_ruleStmtReturn=ruleStmtReturn();

            state._fsp--;

             current =iv_ruleStmtReturn; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStmtReturn3128); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStmtReturn"


    // $ANTLR start "ruleStmtReturn"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1419:1: ruleStmtReturn returns [EObject current=null] : ( () otherlv_1= 'return' ( (lv_e_2_0= ruleExpr ) )? this_NL_3= RULE_NL ) ;
    public final EObject ruleStmtReturn() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token this_NL_3=null;
        EObject lv_e_2_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1422:28: ( ( () otherlv_1= 'return' ( (lv_e_2_0= ruleExpr ) )? this_NL_3= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1423:1: ( () otherlv_1= 'return' ( (lv_e_2_0= ruleExpr ) )? this_NL_3= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1423:1: ( () otherlv_1= 'return' ( (lv_e_2_0= ruleExpr ) )? this_NL_3= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1423:2: () otherlv_1= 'return' ( (lv_e_2_0= ruleExpr ) )? this_NL_3= RULE_NL
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1423:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1424:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getStmtReturnAccess().getStmtReturnAction_0(),
                        current);
                

            }

            otherlv_1=(Token)match(input,33,FOLLOW_33_in_ruleStmtReturn3174); 

                	newLeafNode(otherlv_1, grammarAccess.getStmtReturnAccess().getReturnKeyword_1());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1433:1: ( (lv_e_2_0= ruleExpr ) )?
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( ((LA28_0>=RULE_ID && LA28_0<=RULE_STRING)||LA28_0==20||LA28_0==32||(LA28_0>=47 && LA28_0<=48)||(LA28_0>=53 && LA28_0<=55)) ) {
                alt28=1;
            }
            switch (alt28) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1434:1: (lv_e_2_0= ruleExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1434:1: (lv_e_2_0= ruleExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1435:3: lv_e_2_0= ruleExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getStmtReturnAccess().getEExprParserRuleCall_2_0()); 
                    	    
                    pushFollow(FOLLOW_ruleExpr_in_ruleStmtReturn3195);
                    lv_e_2_0=ruleExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getStmtReturnRule());
                    	        }
                           		set(
                           			current, 
                           			"e",
                            		lv_e_2_0, 
                            		"Expr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }
                    break;

            }

            this_NL_3=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtReturn3207); 
             
                newLeafNode(this_NL_3, grammarAccess.getStmtReturnAccess().getNLTerminalRuleCall_3()); 
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStmtReturn"


    // $ANTLR start "entryRuleStmtIf"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1463:1: entryRuleStmtIf returns [EObject current=null] : iv_ruleStmtIf= ruleStmtIf EOF ;
    public final EObject entryRuleStmtIf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStmtIf = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1464:2: (iv_ruleStmtIf= ruleStmtIf EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1465:2: iv_ruleStmtIf= ruleStmtIf EOF
            {
             newCompositeNode(grammarAccess.getStmtIfRule()); 
            pushFollow(FOLLOW_ruleStmtIf_in_entryRuleStmtIf3242);
            iv_ruleStmtIf=ruleStmtIf();

            state._fsp--;

             current =iv_ruleStmtIf; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStmtIf3252); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStmtIf"


    // $ANTLR start "ruleStmtIf"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1472:1: ruleStmtIf returns [EObject current=null] : (otherlv_0= 'if' ( (lv_cond_1_0= ruleExpr ) ) otherlv_2= '{' this_NL_3= RULE_NL ( (lv_thenBlock_4_0= ruleStatements ) ) otherlv_5= '}' (otherlv_6= 'else' otherlv_7= '{' this_NL_8= RULE_NL ( (lv_elseBlock_9_0= ruleStatements ) ) otherlv_10= '}' )? this_NL_11= RULE_NL ) ;
    public final EObject ruleStmtIf() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_2=null;
        Token this_NL_3=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_7=null;
        Token this_NL_8=null;
        Token otherlv_10=null;
        Token this_NL_11=null;
        EObject lv_cond_1_0 = null;

        EObject lv_thenBlock_4_0 = null;

        EObject lv_elseBlock_9_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1475:28: ( (otherlv_0= 'if' ( (lv_cond_1_0= ruleExpr ) ) otherlv_2= '{' this_NL_3= RULE_NL ( (lv_thenBlock_4_0= ruleStatements ) ) otherlv_5= '}' (otherlv_6= 'else' otherlv_7= '{' this_NL_8= RULE_NL ( (lv_elseBlock_9_0= ruleStatements ) ) otherlv_10= '}' )? this_NL_11= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1476:1: (otherlv_0= 'if' ( (lv_cond_1_0= ruleExpr ) ) otherlv_2= '{' this_NL_3= RULE_NL ( (lv_thenBlock_4_0= ruleStatements ) ) otherlv_5= '}' (otherlv_6= 'else' otherlv_7= '{' this_NL_8= RULE_NL ( (lv_elseBlock_9_0= ruleStatements ) ) otherlv_10= '}' )? this_NL_11= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1476:1: (otherlv_0= 'if' ( (lv_cond_1_0= ruleExpr ) ) otherlv_2= '{' this_NL_3= RULE_NL ( (lv_thenBlock_4_0= ruleStatements ) ) otherlv_5= '}' (otherlv_6= 'else' otherlv_7= '{' this_NL_8= RULE_NL ( (lv_elseBlock_9_0= ruleStatements ) ) otherlv_10= '}' )? this_NL_11= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1476:3: otherlv_0= 'if' ( (lv_cond_1_0= ruleExpr ) ) otherlv_2= '{' this_NL_3= RULE_NL ( (lv_thenBlock_4_0= ruleStatements ) ) otherlv_5= '}' (otherlv_6= 'else' otherlv_7= '{' this_NL_8= RULE_NL ( (lv_elseBlock_9_0= ruleStatements ) ) otherlv_10= '}' )? this_NL_11= RULE_NL
            {
            otherlv_0=(Token)match(input,34,FOLLOW_34_in_ruleStmtIf3289); 

                	newLeafNode(otherlv_0, grammarAccess.getStmtIfAccess().getIfKeyword_0());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1480:1: ( (lv_cond_1_0= ruleExpr ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1481:1: (lv_cond_1_0= ruleExpr )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1481:1: (lv_cond_1_0= ruleExpr )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1482:3: lv_cond_1_0= ruleExpr
            {
             
            	        newCompositeNode(grammarAccess.getStmtIfAccess().getCondExprParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleExpr_in_ruleStmtIf3310);
            lv_cond_1_0=ruleExpr();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getStmtIfRule());
            	        }
                   		set(
                   			current, 
                   			"cond",
                    		lv_cond_1_0, 
                    		"Expr");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_2=(Token)match(input,13,FOLLOW_13_in_ruleStmtIf3322); 

                	newLeafNode(otherlv_2, grammarAccess.getStmtIfAccess().getLeftCurlyBracketKeyword_2());
                
            this_NL_3=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtIf3333); 
             
                newLeafNode(this_NL_3, grammarAccess.getStmtIfAccess().getNLTerminalRuleCall_3()); 
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1506:1: ( (lv_thenBlock_4_0= ruleStatements ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1507:1: (lv_thenBlock_4_0= ruleStatements )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1507:1: (lv_thenBlock_4_0= ruleStatements )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1508:3: lv_thenBlock_4_0= ruleStatements
            {
             
            	        newCompositeNode(grammarAccess.getStmtIfAccess().getThenBlockStatementsParserRuleCall_4_0()); 
            	    
            pushFollow(FOLLOW_ruleStatements_in_ruleStmtIf3353);
            lv_thenBlock_4_0=ruleStatements();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getStmtIfRule());
            	        }
                   		set(
                   			current, 
                   			"thenBlock",
                    		lv_thenBlock_4_0, 
                    		"Statements");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_5=(Token)match(input,14,FOLLOW_14_in_ruleStmtIf3365); 

                	newLeafNode(otherlv_5, grammarAccess.getStmtIfAccess().getRightCurlyBracketKeyword_5());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1528:1: (otherlv_6= 'else' otherlv_7= '{' this_NL_8= RULE_NL ( (lv_elseBlock_9_0= ruleStatements ) ) otherlv_10= '}' )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==35) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1528:3: otherlv_6= 'else' otherlv_7= '{' this_NL_8= RULE_NL ( (lv_elseBlock_9_0= ruleStatements ) ) otherlv_10= '}'
                    {
                    otherlv_6=(Token)match(input,35,FOLLOW_35_in_ruleStmtIf3378); 

                        	newLeafNode(otherlv_6, grammarAccess.getStmtIfAccess().getElseKeyword_6_0());
                        
                    otherlv_7=(Token)match(input,13,FOLLOW_13_in_ruleStmtIf3390); 

                        	newLeafNode(otherlv_7, grammarAccess.getStmtIfAccess().getLeftCurlyBracketKeyword_6_1());
                        
                    this_NL_8=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtIf3401); 
                     
                        newLeafNode(this_NL_8, grammarAccess.getStmtIfAccess().getNLTerminalRuleCall_6_2()); 
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1540:1: ( (lv_elseBlock_9_0= ruleStatements ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1541:1: (lv_elseBlock_9_0= ruleStatements )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1541:1: (lv_elseBlock_9_0= ruleStatements )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1542:3: lv_elseBlock_9_0= ruleStatements
                    {
                     
                    	        newCompositeNode(grammarAccess.getStmtIfAccess().getElseBlockStatementsParserRuleCall_6_3_0()); 
                    	    
                    pushFollow(FOLLOW_ruleStatements_in_ruleStmtIf3421);
                    lv_elseBlock_9_0=ruleStatements();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getStmtIfRule());
                    	        }
                           		set(
                           			current, 
                           			"elseBlock",
                            		lv_elseBlock_9_0, 
                            		"Statements");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }

                    otherlv_10=(Token)match(input,14,FOLLOW_14_in_ruleStmtIf3433); 

                        	newLeafNode(otherlv_10, grammarAccess.getStmtIfAccess().getRightCurlyBracketKeyword_6_4());
                        

                    }
                    break;

            }

            this_NL_11=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtIf3446); 
             
                newLeafNode(this_NL_11, grammarAccess.getStmtIfAccess().getNLTerminalRuleCall_7()); 
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStmtIf"


    // $ANTLR start "entryRuleStmtWhile"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1574:1: entryRuleStmtWhile returns [EObject current=null] : iv_ruleStmtWhile= ruleStmtWhile EOF ;
    public final EObject entryRuleStmtWhile() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStmtWhile = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1575:2: (iv_ruleStmtWhile= ruleStmtWhile EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1576:2: iv_ruleStmtWhile= ruleStmtWhile EOF
            {
             newCompositeNode(grammarAccess.getStmtWhileRule()); 
            pushFollow(FOLLOW_ruleStmtWhile_in_entryRuleStmtWhile3481);
            iv_ruleStmtWhile=ruleStmtWhile();

            state._fsp--;

             current =iv_ruleStmtWhile; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStmtWhile3491); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStmtWhile"


    // $ANTLR start "ruleStmtWhile"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1583:1: ruleStmtWhile returns [EObject current=null] : (otherlv_0= 'while' ( (lv_cond_1_0= ruleExpr ) ) otherlv_2= '{' this_NL_3= RULE_NL ( (lv_body_4_0= ruleStatements ) ) otherlv_5= '}' this_NL_6= RULE_NL ) ;
    public final EObject ruleStmtWhile() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_2=null;
        Token this_NL_3=null;
        Token otherlv_5=null;
        Token this_NL_6=null;
        EObject lv_cond_1_0 = null;

        EObject lv_body_4_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1586:28: ( (otherlv_0= 'while' ( (lv_cond_1_0= ruleExpr ) ) otherlv_2= '{' this_NL_3= RULE_NL ( (lv_body_4_0= ruleStatements ) ) otherlv_5= '}' this_NL_6= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1587:1: (otherlv_0= 'while' ( (lv_cond_1_0= ruleExpr ) ) otherlv_2= '{' this_NL_3= RULE_NL ( (lv_body_4_0= ruleStatements ) ) otherlv_5= '}' this_NL_6= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1587:1: (otherlv_0= 'while' ( (lv_cond_1_0= ruleExpr ) ) otherlv_2= '{' this_NL_3= RULE_NL ( (lv_body_4_0= ruleStatements ) ) otherlv_5= '}' this_NL_6= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1587:3: otherlv_0= 'while' ( (lv_cond_1_0= ruleExpr ) ) otherlv_2= '{' this_NL_3= RULE_NL ( (lv_body_4_0= ruleStatements ) ) otherlv_5= '}' this_NL_6= RULE_NL
            {
            otherlv_0=(Token)match(input,36,FOLLOW_36_in_ruleStmtWhile3528); 

                	newLeafNode(otherlv_0, grammarAccess.getStmtWhileAccess().getWhileKeyword_0());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1591:1: ( (lv_cond_1_0= ruleExpr ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1592:1: (lv_cond_1_0= ruleExpr )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1592:1: (lv_cond_1_0= ruleExpr )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1593:3: lv_cond_1_0= ruleExpr
            {
             
            	        newCompositeNode(grammarAccess.getStmtWhileAccess().getCondExprParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleExpr_in_ruleStmtWhile3549);
            lv_cond_1_0=ruleExpr();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getStmtWhileRule());
            	        }
                   		set(
                   			current, 
                   			"cond",
                    		lv_cond_1_0, 
                    		"Expr");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_2=(Token)match(input,13,FOLLOW_13_in_ruleStmtWhile3561); 

                	newLeafNode(otherlv_2, grammarAccess.getStmtWhileAccess().getLeftCurlyBracketKeyword_2());
                
            this_NL_3=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtWhile3572); 
             
                newLeafNode(this_NL_3, grammarAccess.getStmtWhileAccess().getNLTerminalRuleCall_3()); 
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1617:1: ( (lv_body_4_0= ruleStatements ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1618:1: (lv_body_4_0= ruleStatements )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1618:1: (lv_body_4_0= ruleStatements )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1619:3: lv_body_4_0= ruleStatements
            {
             
            	        newCompositeNode(grammarAccess.getStmtWhileAccess().getBodyStatementsParserRuleCall_4_0()); 
            	    
            pushFollow(FOLLOW_ruleStatements_in_ruleStmtWhile3592);
            lv_body_4_0=ruleStatements();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getStmtWhileRule());
            	        }
                   		set(
                   			current, 
                   			"body",
                    		lv_body_4_0, 
                    		"Statements");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_5=(Token)match(input,14,FOLLOW_14_in_ruleStmtWhile3604); 

                	newLeafNode(otherlv_5, grammarAccess.getStmtWhileAccess().getRightCurlyBracketKeyword_5());
                
            this_NL_6=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtWhile3615); 
             
                newLeafNode(this_NL_6, grammarAccess.getStmtWhileAccess().getNLTerminalRuleCall_6()); 
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStmtWhile"


    // $ANTLR start "entryRuleLocalVarDef"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1651:1: entryRuleLocalVarDef returns [EObject current=null] : iv_ruleLocalVarDef= ruleLocalVarDef EOF ;
    public final EObject entryRuleLocalVarDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLocalVarDef = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1652:2: (iv_ruleLocalVarDef= ruleLocalVarDef EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1653:2: iv_ruleLocalVarDef= ruleLocalVarDef EOF
            {
             newCompositeNode(grammarAccess.getLocalVarDefRule()); 
            pushFollow(FOLLOW_ruleLocalVarDef_in_entryRuleLocalVarDef3650);
            iv_ruleLocalVarDef=ruleLocalVarDef();

            state._fsp--;

             current =iv_ruleLocalVarDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleLocalVarDef3660); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLocalVarDef"


    // $ANTLR start "ruleLocalVarDef"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1660:1: ruleLocalVarDef returns [EObject current=null] : ( ( () ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) (otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) )? this_NL_6= RULE_NL ) | ( () ( (lv_type_8_0= ruleTypeExpr ) ) ( (lv_name_9_0= RULE_ID ) ) (otherlv_10= '=' ( (lv_e_11_0= ruleExpr ) ) )? this_NL_12= RULE_NL ) ) ;
    public final EObject ruleLocalVarDef() throws RecognitionException {
        EObject current = null;

        Token lv_constant_1_0=null;
        Token lv_name_3_0=null;
        Token otherlv_4=null;
        Token this_NL_6=null;
        Token lv_name_9_0=null;
        Token otherlv_10=null;
        Token this_NL_12=null;
        EObject lv_type_2_0 = null;

        EObject lv_e_5_0 = null;

        EObject lv_type_8_0 = null;

        EObject lv_e_11_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1663:28: ( ( ( () ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) (otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) )? this_NL_6= RULE_NL ) | ( () ( (lv_type_8_0= ruleTypeExpr ) ) ( (lv_name_9_0= RULE_ID ) ) (otherlv_10= '=' ( (lv_e_11_0= ruleExpr ) ) )? this_NL_12= RULE_NL ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1664:1: ( ( () ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) (otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) )? this_NL_6= RULE_NL ) | ( () ( (lv_type_8_0= ruleTypeExpr ) ) ( (lv_name_9_0= RULE_ID ) ) (otherlv_10= '=' ( (lv_e_11_0= ruleExpr ) ) )? this_NL_12= RULE_NL ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1664:1: ( ( () ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) (otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) )? this_NL_6= RULE_NL ) | ( () ( (lv_type_8_0= ruleTypeExpr ) ) ( (lv_name_9_0= RULE_ID ) ) (otherlv_10= '=' ( (lv_e_11_0= ruleExpr ) ) )? this_NL_12= RULE_NL ) )
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==27) ) {
                alt33=1;
            }
            else if ( (LA33_0==RULE_ID) ) {
                alt33=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;
            }
            switch (alt33) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1664:2: ( () ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) (otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) )? this_NL_6= RULE_NL )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1664:2: ( () ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) (otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) )? this_NL_6= RULE_NL )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1664:3: () ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) (otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) )? this_NL_6= RULE_NL
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1664:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1665:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getLocalVarDefAccess().getVarDefAction_0_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1670:2: ( (lv_constant_1_0= 'val' ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1671:1: (lv_constant_1_0= 'val' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1671:1: (lv_constant_1_0= 'val' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1672:3: lv_constant_1_0= 'val'
                    {
                    lv_constant_1_0=(Token)match(input,27,FOLLOW_27_in_ruleLocalVarDef3713); 

                            newLeafNode(lv_constant_1_0, grammarAccess.getLocalVarDefAccess().getConstantValKeyword_0_1_0());
                        

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getLocalVarDefRule());
                    	        }
                           		setWithLastConsumed(current, "constant", true, "val");
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1685:2: ( (lv_type_2_0= ruleTypeExpr ) )?
                    int alt30=2;
                    int LA30_0 = input.LA(1);

                    if ( (LA30_0==RULE_ID) ) {
                        int LA30_1 = input.LA(2);

                        if ( (LA30_1==RULE_ID||LA30_1==29) ) {
                            alt30=1;
                        }
                    }
                    switch (alt30) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1686:1: (lv_type_2_0= ruleTypeExpr )
                            {
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1686:1: (lv_type_2_0= ruleTypeExpr )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1687:3: lv_type_2_0= ruleTypeExpr
                            {
                             
                            	        newCompositeNode(grammarAccess.getLocalVarDefAccess().getTypeTypeExprParserRuleCall_0_2_0()); 
                            	    
                            pushFollow(FOLLOW_ruleTypeExpr_in_ruleLocalVarDef3747);
                            lv_type_2_0=ruleTypeExpr();

                            state._fsp--;


                            	        if (current==null) {
                            	            current = createModelElementForParent(grammarAccess.getLocalVarDefRule());
                            	        }
                                   		set(
                                   			current, 
                                   			"type",
                                    		lv_type_2_0, 
                                    		"TypeExpr");
                            	        afterParserOrEnumRuleCall();
                            	    

                            }


                            }
                            break;

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1703:3: ( (lv_name_3_0= RULE_ID ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1704:1: (lv_name_3_0= RULE_ID )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1704:1: (lv_name_3_0= RULE_ID )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1705:3: lv_name_3_0= RULE_ID
                    {
                    lv_name_3_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleLocalVarDef3765); 

                    			newLeafNode(lv_name_3_0, grammarAccess.getLocalVarDefAccess().getNameIDTerminalRuleCall_0_3_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getLocalVarDefRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"name",
                            		lv_name_3_0, 
                            		"ID");
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1721:2: (otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) )?
                    int alt31=2;
                    int LA31_0 = input.LA(1);

                    if ( (LA31_0==28) ) {
                        alt31=1;
                    }
                    switch (alt31) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1721:4: otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) )
                            {
                            otherlv_4=(Token)match(input,28,FOLLOW_28_in_ruleLocalVarDef3783); 

                                	newLeafNode(otherlv_4, grammarAccess.getLocalVarDefAccess().getEqualsSignKeyword_0_4_0());
                                
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1725:1: ( (lv_e_5_0= ruleExpr ) )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1726:1: (lv_e_5_0= ruleExpr )
                            {
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1726:1: (lv_e_5_0= ruleExpr )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1727:3: lv_e_5_0= ruleExpr
                            {
                             
                            	        newCompositeNode(grammarAccess.getLocalVarDefAccess().getEExprParserRuleCall_0_4_1_0()); 
                            	    
                            pushFollow(FOLLOW_ruleExpr_in_ruleLocalVarDef3804);
                            lv_e_5_0=ruleExpr();

                            state._fsp--;


                            	        if (current==null) {
                            	            current = createModelElementForParent(grammarAccess.getLocalVarDefRule());
                            	        }
                                   		set(
                                   			current, 
                                   			"e",
                                    		lv_e_5_0, 
                                    		"Expr");
                            	        afterParserOrEnumRuleCall();
                            	    

                            }


                            }


                            }
                            break;

                    }

                    this_NL_6=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleLocalVarDef3817); 
                     
                        newLeafNode(this_NL_6, grammarAccess.getLocalVarDefAccess().getNLTerminalRuleCall_0_5()); 
                        

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1748:6: ( () ( (lv_type_8_0= ruleTypeExpr ) ) ( (lv_name_9_0= RULE_ID ) ) (otherlv_10= '=' ( (lv_e_11_0= ruleExpr ) ) )? this_NL_12= RULE_NL )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1748:6: ( () ( (lv_type_8_0= ruleTypeExpr ) ) ( (lv_name_9_0= RULE_ID ) ) (otherlv_10= '=' ( (lv_e_11_0= ruleExpr ) ) )? this_NL_12= RULE_NL )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1748:7: () ( (lv_type_8_0= ruleTypeExpr ) ) ( (lv_name_9_0= RULE_ID ) ) (otherlv_10= '=' ( (lv_e_11_0= ruleExpr ) ) )? this_NL_12= RULE_NL
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1748:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1749:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getLocalVarDefAccess().getVarDefAction_1_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1754:2: ( (lv_type_8_0= ruleTypeExpr ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1755:1: (lv_type_8_0= ruleTypeExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1755:1: (lv_type_8_0= ruleTypeExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1756:3: lv_type_8_0= ruleTypeExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getLocalVarDefAccess().getTypeTypeExprParserRuleCall_1_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleTypeExpr_in_ruleLocalVarDef3854);
                    lv_type_8_0=ruleTypeExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getLocalVarDefRule());
                    	        }
                           		set(
                           			current, 
                           			"type",
                            		lv_type_8_0, 
                            		"TypeExpr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1772:2: ( (lv_name_9_0= RULE_ID ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1773:1: (lv_name_9_0= RULE_ID )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1773:1: (lv_name_9_0= RULE_ID )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1774:3: lv_name_9_0= RULE_ID
                    {
                    lv_name_9_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleLocalVarDef3871); 

                    			newLeafNode(lv_name_9_0, grammarAccess.getLocalVarDefAccess().getNameIDTerminalRuleCall_1_2_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getLocalVarDefRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"name",
                            		lv_name_9_0, 
                            		"ID");
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1790:2: (otherlv_10= '=' ( (lv_e_11_0= ruleExpr ) ) )?
                    int alt32=2;
                    int LA32_0 = input.LA(1);

                    if ( (LA32_0==28) ) {
                        alt32=1;
                    }
                    switch (alt32) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1790:4: otherlv_10= '=' ( (lv_e_11_0= ruleExpr ) )
                            {
                            otherlv_10=(Token)match(input,28,FOLLOW_28_in_ruleLocalVarDef3889); 

                                	newLeafNode(otherlv_10, grammarAccess.getLocalVarDefAccess().getEqualsSignKeyword_1_3_0());
                                
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1794:1: ( (lv_e_11_0= ruleExpr ) )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1795:1: (lv_e_11_0= ruleExpr )
                            {
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1795:1: (lv_e_11_0= ruleExpr )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1796:3: lv_e_11_0= ruleExpr
                            {
                             
                            	        newCompositeNode(grammarAccess.getLocalVarDefAccess().getEExprParserRuleCall_1_3_1_0()); 
                            	    
                            pushFollow(FOLLOW_ruleExpr_in_ruleLocalVarDef3910);
                            lv_e_11_0=ruleExpr();

                            state._fsp--;


                            	        if (current==null) {
                            	            current = createModelElementForParent(grammarAccess.getLocalVarDefRule());
                            	        }
                                   		set(
                                   			current, 
                                   			"e",
                                    		lv_e_11_0, 
                                    		"Expr");
                            	        afterParserOrEnumRuleCall();
                            	    

                            }


                            }


                            }
                            break;

                    }

                    this_NL_12=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleLocalVarDef3923); 
                     
                        newLeafNode(this_NL_12, grammarAccess.getLocalVarDefAccess().getNLTerminalRuleCall_1_4()); 
                        

                    }


                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLocalVarDef"


    // $ANTLR start "entryRuleStmtSetOrCallOrVarDef"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1824:1: entryRuleStmtSetOrCallOrVarDef returns [EObject current=null] : iv_ruleStmtSetOrCallOrVarDef= ruleStmtSetOrCallOrVarDef EOF ;
    public final EObject entryRuleStmtSetOrCallOrVarDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStmtSetOrCallOrVarDef = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1825:2: (iv_ruleStmtSetOrCallOrVarDef= ruleStmtSetOrCallOrVarDef EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1826:2: iv_ruleStmtSetOrCallOrVarDef= ruleStmtSetOrCallOrVarDef EOF
            {
             newCompositeNode(grammarAccess.getStmtSetOrCallOrVarDefRule()); 
            pushFollow(FOLLOW_ruleStmtSetOrCallOrVarDef_in_entryRuleStmtSetOrCallOrVarDef3959);
            iv_ruleStmtSetOrCallOrVarDef=ruleStmtSetOrCallOrVarDef();

            state._fsp--;

             current =iv_ruleStmtSetOrCallOrVarDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStmtSetOrCallOrVarDef3969); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStmtSetOrCallOrVarDef"


    // $ANTLR start "ruleStmtSetOrCallOrVarDef"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1833:1: ruleStmtSetOrCallOrVarDef returns [EObject current=null] : ( () ( (lv_e_1_0= ruleExpr ) ) ( () ( (lv_opAssignment_3_0= ruleOpAssignment ) ) ( (lv_right_4_0= ruleExpr ) ) )? this_NL_5= RULE_NL ) ;
    public final EObject ruleStmtSetOrCallOrVarDef() throws RecognitionException {
        EObject current = null;

        Token this_NL_5=null;
        EObject lv_e_1_0 = null;

        EObject lv_opAssignment_3_0 = null;

        EObject lv_right_4_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1836:28: ( ( () ( (lv_e_1_0= ruleExpr ) ) ( () ( (lv_opAssignment_3_0= ruleOpAssignment ) ) ( (lv_right_4_0= ruleExpr ) ) )? this_NL_5= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1837:1: ( () ( (lv_e_1_0= ruleExpr ) ) ( () ( (lv_opAssignment_3_0= ruleOpAssignment ) ) ( (lv_right_4_0= ruleExpr ) ) )? this_NL_5= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1837:1: ( () ( (lv_e_1_0= ruleExpr ) ) ( () ( (lv_opAssignment_3_0= ruleOpAssignment ) ) ( (lv_right_4_0= ruleExpr ) ) )? this_NL_5= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1837:2: () ( (lv_e_1_0= ruleExpr ) ) ( () ( (lv_opAssignment_3_0= ruleOpAssignment ) ) ( (lv_right_4_0= ruleExpr ) ) )? this_NL_5= RULE_NL
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1837:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1838:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getStmtSetOrCallOrVarDefAccess().getStmtCallAction_0(),
                        current);
                

            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1843:2: ( (lv_e_1_0= ruleExpr ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1844:1: (lv_e_1_0= ruleExpr )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1844:1: (lv_e_1_0= ruleExpr )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1845:3: lv_e_1_0= ruleExpr
            {
             
            	        newCompositeNode(grammarAccess.getStmtSetOrCallOrVarDefAccess().getEExprParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleExpr_in_ruleStmtSetOrCallOrVarDef4024);
            lv_e_1_0=ruleExpr();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getStmtSetOrCallOrVarDefRule());
            	        }
                   		set(
                   			current, 
                   			"e",
                    		lv_e_1_0, 
                    		"Expr");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1861:2: ( () ( (lv_opAssignment_3_0= ruleOpAssignment ) ) ( (lv_right_4_0= ruleExpr ) ) )?
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==28||(LA34_0>=37 && LA34_0<=38)) ) {
                alt34=1;
            }
            switch (alt34) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1861:3: () ( (lv_opAssignment_3_0= ruleOpAssignment ) ) ( (lv_right_4_0= ruleExpr ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1861:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1862:5: 
                    {

                            current = forceCreateModelElementAndSet(
                                grammarAccess.getStmtSetOrCallOrVarDefAccess().getStmtSetLeftAction_2_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1867:2: ( (lv_opAssignment_3_0= ruleOpAssignment ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1868:1: (lv_opAssignment_3_0= ruleOpAssignment )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1868:1: (lv_opAssignment_3_0= ruleOpAssignment )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1869:3: lv_opAssignment_3_0= ruleOpAssignment
                    {
                     
                    	        newCompositeNode(grammarAccess.getStmtSetOrCallOrVarDefAccess().getOpAssignmentOpAssignmentParserRuleCall_2_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleOpAssignment_in_ruleStmtSetOrCallOrVarDef4055);
                    lv_opAssignment_3_0=ruleOpAssignment();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getStmtSetOrCallOrVarDefRule());
                    	        }
                           		set(
                           			current, 
                           			"opAssignment",
                            		lv_opAssignment_3_0, 
                            		"OpAssignment");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1885:2: ( (lv_right_4_0= ruleExpr ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1886:1: (lv_right_4_0= ruleExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1886:1: (lv_right_4_0= ruleExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1887:3: lv_right_4_0= ruleExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getStmtSetOrCallOrVarDefAccess().getRightExprParserRuleCall_2_2_0()); 
                    	    
                    pushFollow(FOLLOW_ruleExpr_in_ruleStmtSetOrCallOrVarDef4076);
                    lv_right_4_0=ruleExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getStmtSetOrCallOrVarDefRule());
                    	        }
                           		set(
                           			current, 
                           			"right",
                            		lv_right_4_0, 
                            		"Expr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }
                    break;

            }

            this_NL_5=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtSetOrCallOrVarDef4089); 
             
                newLeafNode(this_NL_5, grammarAccess.getStmtSetOrCallOrVarDefAccess().getNLTerminalRuleCall_3()); 
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStmtSetOrCallOrVarDef"


    // $ANTLR start "entryRuleOpAssignment"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1915:1: entryRuleOpAssignment returns [EObject current=null] : iv_ruleOpAssignment= ruleOpAssignment EOF ;
    public final EObject entryRuleOpAssignment() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOpAssignment = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1916:2: (iv_ruleOpAssignment= ruleOpAssignment EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1917:2: iv_ruleOpAssignment= ruleOpAssignment EOF
            {
             newCompositeNode(grammarAccess.getOpAssignmentRule()); 
            pushFollow(FOLLOW_ruleOpAssignment_in_entryRuleOpAssignment4124);
            iv_ruleOpAssignment=ruleOpAssignment();

            state._fsp--;

             current =iv_ruleOpAssignment; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleOpAssignment4134); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpAssignment"


    // $ANTLR start "ruleOpAssignment"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1924:1: ruleOpAssignment returns [EObject current=null] : ( ( () otherlv_1= '=' ) | ( () otherlv_3= '+=' ) | ( () otherlv_5= '-=' ) ) ;
    public final EObject ruleOpAssignment() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        Token otherlv_5=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1927:28: ( ( ( () otherlv_1= '=' ) | ( () otherlv_3= '+=' ) | ( () otherlv_5= '-=' ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1928:1: ( ( () otherlv_1= '=' ) | ( () otherlv_3= '+=' ) | ( () otherlv_5= '-=' ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1928:1: ( ( () otherlv_1= '=' ) | ( () otherlv_3= '+=' ) | ( () otherlv_5= '-=' ) )
            int alt35=3;
            switch ( input.LA(1) ) {
            case 28:
                {
                alt35=1;
                }
                break;
            case 37:
                {
                alt35=2;
                }
                break;
            case 38:
                {
                alt35=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 35, 0, input);

                throw nvae;
            }

            switch (alt35) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1928:2: ( () otherlv_1= '=' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1928:2: ( () otherlv_1= '=' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1928:3: () otherlv_1= '='
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1928:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1929:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpAssignmentAccess().getOpAssignAction_0_0(),
                                current);
                        

                    }

                    otherlv_1=(Token)match(input,28,FOLLOW_28_in_ruleOpAssignment4181); 

                        	newLeafNode(otherlv_1, grammarAccess.getOpAssignmentAccess().getEqualsSignKeyword_0_1());
                        

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1939:6: ( () otherlv_3= '+=' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1939:6: ( () otherlv_3= '+=' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1939:7: () otherlv_3= '+='
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1939:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1940:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpAssignmentAccess().getOpPlusAssignAction_1_0(),
                                current);
                        

                    }

                    otherlv_3=(Token)match(input,37,FOLLOW_37_in_ruleOpAssignment4210); 

                        	newLeafNode(otherlv_3, grammarAccess.getOpAssignmentAccess().getPlusSignEqualsSignKeyword_1_1());
                        

                    }


                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1950:6: ( () otherlv_5= '-=' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1950:6: ( () otherlv_5= '-=' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1950:7: () otherlv_5= '-='
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1950:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1951:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpAssignmentAccess().getOpMinusAssignAction_2_0(),
                                current);
                        

                    }

                    otherlv_5=(Token)match(input,38,FOLLOW_38_in_ruleOpAssignment4239); 

                        	newLeafNode(otherlv_5, grammarAccess.getOpAssignmentAccess().getHyphenMinusEqualsSignKeyword_2_1());
                        

                    }


                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpAssignment"


    // $ANTLR start "entryRuleExpr"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1968:1: entryRuleExpr returns [EObject current=null] : iv_ruleExpr= ruleExpr EOF ;
    public final EObject entryRuleExpr() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExpr = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1969:2: (iv_ruleExpr= ruleExpr EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1970:2: iv_ruleExpr= ruleExpr EOF
            {
             newCompositeNode(grammarAccess.getExprRule()); 
            pushFollow(FOLLOW_ruleExpr_in_entryRuleExpr4276);
            iv_ruleExpr=ruleExpr();

            state._fsp--;

             current =iv_ruleExpr; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExpr4286); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExpr"


    // $ANTLR start "ruleExpr"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1977:1: ruleExpr returns [EObject current=null] : this_ExprOr_0= ruleExprOr ;
    public final EObject ruleExpr() throws RecognitionException {
        EObject current = null;

        EObject this_ExprOr_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1980:28: (this_ExprOr_0= ruleExprOr )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1982:5: this_ExprOr_0= ruleExprOr
            {
             
                    newCompositeNode(grammarAccess.getExprAccess().getExprOrParserRuleCall()); 
                
            pushFollow(FOLLOW_ruleExprOr_in_ruleExpr4332);
            this_ExprOr_0=ruleExprOr();

            state._fsp--;

             
                    current = this_ExprOr_0; 
                    afterParserOrEnumRuleCall();
                

            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExpr"


    // $ANTLR start "entryRuleExprOr"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1998:1: entryRuleExprOr returns [EObject current=null] : iv_ruleExprOr= ruleExprOr EOF ;
    public final EObject entryRuleExprOr() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprOr = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1999:2: (iv_ruleExprOr= ruleExprOr EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2000:2: iv_ruleExprOr= ruleExprOr EOF
            {
             newCompositeNode(grammarAccess.getExprOrRule()); 
            pushFollow(FOLLOW_ruleExprOr_in_entryRuleExprOr4366);
            iv_ruleExprOr=ruleExprOr();

            state._fsp--;

             current =iv_ruleExprOr; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprOr4376); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExprOr"


    // $ANTLR start "ruleExprOr"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2007:1: ruleExprOr returns [EObject current=null] : (this_ExprAnd_0= ruleExprAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )* ) ;
    public final EObject ruleExprOr() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_ExprAnd_0 = null;

        EObject lv_right_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2010:28: ( (this_ExprAnd_0= ruleExprAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2011:1: (this_ExprAnd_0= ruleExprAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2011:1: (this_ExprAnd_0= ruleExprAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2012:5: this_ExprAnd_0= ruleExprAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )*
            {
             
                    newCompositeNode(grammarAccess.getExprOrAccess().getExprAndParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleExprAnd_in_ruleExprOr4423);
            this_ExprAnd_0=ruleExprAnd();

            state._fsp--;

             
                    current = this_ExprAnd_0; 
                    afterParserOrEnumRuleCall();
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2020:1: ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )*
            loop36:
            do {
                int alt36=2;
                int LA36_0 = input.LA(1);

                if ( (LA36_0==39) ) {
                    alt36=1;
                }


                switch (alt36) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2020:2: () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2020:2: ()
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2021:5: 
            	    {

            	            current = forceCreateModelElementAndSet(
            	                grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0(),
            	                current);
            	        

            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2026:2: ( (lv_op_2_0= 'or' ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2027:1: (lv_op_2_0= 'or' )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2027:1: (lv_op_2_0= 'or' )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2028:3: lv_op_2_0= 'or'
            	    {
            	    lv_op_2_0=(Token)match(input,39,FOLLOW_39_in_ruleExprOr4450); 

            	            newLeafNode(lv_op_2_0, grammarAccess.getExprOrAccess().getOpOrKeyword_1_1_0());
            	        

            	    	        if (current==null) {
            	    	            current = createModelElement(grammarAccess.getExprOrRule());
            	    	        }
            	           		setWithLastConsumed(current, "op", lv_op_2_0, "or");
            	    	    

            	    }


            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2041:2: ( (lv_right_3_0= ruleExprAnd ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2042:1: (lv_right_3_0= ruleExprAnd )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2042:1: (lv_right_3_0= ruleExprAnd )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2043:3: lv_right_3_0= ruleExprAnd
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprOrAccess().getRightExprAndParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprAnd_in_ruleExprOr4484);
            	    lv_right_3_0=ruleExprAnd();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getExprOrRule());
            	    	        }
            	           		set(
            	           			current, 
            	           			"right",
            	            		lv_right_3_0, 
            	            		"ExprAnd");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop36;
                }
            } while (true);


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExprOr"


    // $ANTLR start "entryRuleExprAnd"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2067:1: entryRuleExprAnd returns [EObject current=null] : iv_ruleExprAnd= ruleExprAnd EOF ;
    public final EObject entryRuleExprAnd() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprAnd = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2068:2: (iv_ruleExprAnd= ruleExprAnd EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2069:2: iv_ruleExprAnd= ruleExprAnd EOF
            {
             newCompositeNode(grammarAccess.getExprAndRule()); 
            pushFollow(FOLLOW_ruleExprAnd_in_entryRuleExprAnd4522);
            iv_ruleExprAnd=ruleExprAnd();

            state._fsp--;

             current =iv_ruleExprAnd; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprAnd4532); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExprAnd"


    // $ANTLR start "ruleExprAnd"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2076:1: ruleExprAnd returns [EObject current=null] : (this_ExprEquality_0= ruleExprEquality ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )* ) ;
    public final EObject ruleExprAnd() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_ExprEquality_0 = null;

        EObject lv_right_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2079:28: ( (this_ExprEquality_0= ruleExprEquality ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2080:1: (this_ExprEquality_0= ruleExprEquality ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2080:1: (this_ExprEquality_0= ruleExprEquality ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2081:5: this_ExprEquality_0= ruleExprEquality ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )*
            {
             
                    newCompositeNode(grammarAccess.getExprAndAccess().getExprEqualityParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleExprEquality_in_ruleExprAnd4579);
            this_ExprEquality_0=ruleExprEquality();

            state._fsp--;

             
                    current = this_ExprEquality_0; 
                    afterParserOrEnumRuleCall();
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2089:1: ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )*
            loop37:
            do {
                int alt37=2;
                int LA37_0 = input.LA(1);

                if ( (LA37_0==40) ) {
                    alt37=1;
                }


                switch (alt37) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2089:2: () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2089:2: ()
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2090:5: 
            	    {

            	            current = forceCreateModelElementAndSet(
            	                grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0(),
            	                current);
            	        

            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2095:2: ( (lv_op_2_0= 'and' ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2096:1: (lv_op_2_0= 'and' )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2096:1: (lv_op_2_0= 'and' )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2097:3: lv_op_2_0= 'and'
            	    {
            	    lv_op_2_0=(Token)match(input,40,FOLLOW_40_in_ruleExprAnd4606); 

            	            newLeafNode(lv_op_2_0, grammarAccess.getExprAndAccess().getOpAndKeyword_1_1_0());
            	        

            	    	        if (current==null) {
            	    	            current = createModelElement(grammarAccess.getExprAndRule());
            	    	        }
            	           		setWithLastConsumed(current, "op", lv_op_2_0, "and");
            	    	    

            	    }


            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2110:2: ( (lv_right_3_0= ruleExprEquality ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2111:1: (lv_right_3_0= ruleExprEquality )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2111:1: (lv_right_3_0= ruleExprEquality )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2112:3: lv_right_3_0= ruleExprEquality
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprAndAccess().getRightExprEqualityParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprEquality_in_ruleExprAnd4640);
            	    lv_right_3_0=ruleExprEquality();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getExprAndRule());
            	    	        }
            	           		set(
            	           			current, 
            	           			"right",
            	            		lv_right_3_0, 
            	            		"ExprEquality");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop37;
                }
            } while (true);


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExprAnd"


    // $ANTLR start "entryRuleExprEquality"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2136:1: entryRuleExprEquality returns [EObject current=null] : iv_ruleExprEquality= ruleExprEquality EOF ;
    public final EObject entryRuleExprEquality() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprEquality = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2137:2: (iv_ruleExprEquality= ruleExprEquality EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2138:2: iv_ruleExprEquality= ruleExprEquality EOF
            {
             newCompositeNode(grammarAccess.getExprEqualityRule()); 
            pushFollow(FOLLOW_ruleExprEquality_in_entryRuleExprEquality4678);
            iv_ruleExprEquality=ruleExprEquality();

            state._fsp--;

             current =iv_ruleExprEquality; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprEquality4688); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExprEquality"


    // $ANTLR start "ruleExprEquality"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2145:1: ruleExprEquality returns [EObject current=null] : (this_ExprComparison_0= ruleExprComparison ( () ( (lv_op_2_0= ruleOpEquality ) ) ( (lv_right_3_0= ruleExprComparison ) ) )* ) ;
    public final EObject ruleExprEquality() throws RecognitionException {
        EObject current = null;

        EObject this_ExprComparison_0 = null;

        EObject lv_op_2_0 = null;

        EObject lv_right_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2148:28: ( (this_ExprComparison_0= ruleExprComparison ( () ( (lv_op_2_0= ruleOpEquality ) ) ( (lv_right_3_0= ruleExprComparison ) ) )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2149:1: (this_ExprComparison_0= ruleExprComparison ( () ( (lv_op_2_0= ruleOpEquality ) ) ( (lv_right_3_0= ruleExprComparison ) ) )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2149:1: (this_ExprComparison_0= ruleExprComparison ( () ( (lv_op_2_0= ruleOpEquality ) ) ( (lv_right_3_0= ruleExprComparison ) ) )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2150:5: this_ExprComparison_0= ruleExprComparison ( () ( (lv_op_2_0= ruleOpEquality ) ) ( (lv_right_3_0= ruleExprComparison ) ) )*
            {
             
                    newCompositeNode(grammarAccess.getExprEqualityAccess().getExprComparisonParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleExprComparison_in_ruleExprEquality4735);
            this_ExprComparison_0=ruleExprComparison();

            state._fsp--;

             
                    current = this_ExprComparison_0; 
                    afterParserOrEnumRuleCall();
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2158:1: ( () ( (lv_op_2_0= ruleOpEquality ) ) ( (lv_right_3_0= ruleExprComparison ) ) )*
            loop38:
            do {
                int alt38=2;
                int LA38_0 = input.LA(1);

                if ( ((LA38_0>=41 && LA38_0<=42)) ) {
                    alt38=1;
                }


                switch (alt38) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2158:2: () ( (lv_op_2_0= ruleOpEquality ) ) ( (lv_right_3_0= ruleExprComparison ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2158:2: ()
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2159:5: 
            	    {

            	            current = forceCreateModelElementAndSet(
            	                grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0(),
            	                current);
            	        

            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2164:2: ( (lv_op_2_0= ruleOpEquality ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2165:1: (lv_op_2_0= ruleOpEquality )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2165:1: (lv_op_2_0= ruleOpEquality )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2166:3: lv_op_2_0= ruleOpEquality
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprEqualityAccess().getOpOpEqualityParserRuleCall_1_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleOpEquality_in_ruleExprEquality4765);
            	    lv_op_2_0=ruleOpEquality();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getExprEqualityRule());
            	    	        }
            	           		set(
            	           			current, 
            	           			"op",
            	            		lv_op_2_0, 
            	            		"OpEquality");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2182:2: ( (lv_right_3_0= ruleExprComparison ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2183:1: (lv_right_3_0= ruleExprComparison )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2183:1: (lv_right_3_0= ruleExprComparison )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2184:3: lv_right_3_0= ruleExprComparison
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprEqualityAccess().getRightExprComparisonParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprComparison_in_ruleExprEquality4786);
            	    lv_right_3_0=ruleExprComparison();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getExprEqualityRule());
            	    	        }
            	           		set(
            	           			current, 
            	           			"right",
            	            		lv_right_3_0, 
            	            		"ExprComparison");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop38;
                }
            } while (true);


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExprEquality"


    // $ANTLR start "entryRuleOpEquality"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2208:1: entryRuleOpEquality returns [EObject current=null] : iv_ruleOpEquality= ruleOpEquality EOF ;
    public final EObject entryRuleOpEquality() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOpEquality = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2209:2: (iv_ruleOpEquality= ruleOpEquality EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2210:2: iv_ruleOpEquality= ruleOpEquality EOF
            {
             newCompositeNode(grammarAccess.getOpEqualityRule()); 
            pushFollow(FOLLOW_ruleOpEquality_in_entryRuleOpEquality4824);
            iv_ruleOpEquality=ruleOpEquality();

            state._fsp--;

             current =iv_ruleOpEquality; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleOpEquality4834); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpEquality"


    // $ANTLR start "ruleOpEquality"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2217:1: ruleOpEquality returns [EObject current=null] : ( ( () otherlv_1= '==' ) | ( () otherlv_3= '!=' ) ) ;
    public final EObject ruleOpEquality() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2220:28: ( ( ( () otherlv_1= '==' ) | ( () otherlv_3= '!=' ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2221:1: ( ( () otherlv_1= '==' ) | ( () otherlv_3= '!=' ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2221:1: ( ( () otherlv_1= '==' ) | ( () otherlv_3= '!=' ) )
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==41) ) {
                alt39=1;
            }
            else if ( (LA39_0==42) ) {
                alt39=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;
            }
            switch (alt39) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2221:2: ( () otherlv_1= '==' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2221:2: ( () otherlv_1= '==' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2221:3: () otherlv_1= '=='
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2221:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2222:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpEqualityAccess().getOpEqualsAction_0_0(),
                                current);
                        

                    }

                    otherlv_1=(Token)match(input,41,FOLLOW_41_in_ruleOpEquality4881); 

                        	newLeafNode(otherlv_1, grammarAccess.getOpEqualityAccess().getEqualsSignEqualsSignKeyword_0_1());
                        

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2232:6: ( () otherlv_3= '!=' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2232:6: ( () otherlv_3= '!=' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2232:7: () otherlv_3= '!='
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2232:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2233:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpEqualityAccess().getOpUnequalsAction_1_0(),
                                current);
                        

                    }

                    otherlv_3=(Token)match(input,42,FOLLOW_42_in_ruleOpEquality4910); 

                        	newLeafNode(otherlv_3, grammarAccess.getOpEqualityAccess().getExclamationMarkEqualsSignKeyword_1_1());
                        

                    }


                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpEquality"


    // $ANTLR start "entryRuleExprComparison"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2250:1: entryRuleExprComparison returns [EObject current=null] : iv_ruleExprComparison= ruleExprComparison EOF ;
    public final EObject entryRuleExprComparison() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprComparison = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2251:2: (iv_ruleExprComparison= ruleExprComparison EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2252:2: iv_ruleExprComparison= ruleExprComparison EOF
            {
             newCompositeNode(grammarAccess.getExprComparisonRule()); 
            pushFollow(FOLLOW_ruleExprComparison_in_entryRuleExprComparison4947);
            iv_ruleExprComparison=ruleExprComparison();

            state._fsp--;

             current =iv_ruleExprComparison; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprComparison4957); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExprComparison"


    // $ANTLR start "ruleExprComparison"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2259:1: ruleExprComparison returns [EObject current=null] : (this_ExprAdditive_0= ruleExprAdditive ( () ( (lv_op_2_0= ruleOpComparison ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )* ) ;
    public final EObject ruleExprComparison() throws RecognitionException {
        EObject current = null;

        EObject this_ExprAdditive_0 = null;

        EObject lv_op_2_0 = null;

        EObject lv_right_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2262:28: ( (this_ExprAdditive_0= ruleExprAdditive ( () ( (lv_op_2_0= ruleOpComparison ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2263:1: (this_ExprAdditive_0= ruleExprAdditive ( () ( (lv_op_2_0= ruleOpComparison ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2263:1: (this_ExprAdditive_0= ruleExprAdditive ( () ( (lv_op_2_0= ruleOpComparison ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2264:5: this_ExprAdditive_0= ruleExprAdditive ( () ( (lv_op_2_0= ruleOpComparison ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )*
            {
             
                    newCompositeNode(grammarAccess.getExprComparisonAccess().getExprAdditiveParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleExprAdditive_in_ruleExprComparison5004);
            this_ExprAdditive_0=ruleExprAdditive();

            state._fsp--;

             
                    current = this_ExprAdditive_0; 
                    afterParserOrEnumRuleCall();
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2272:1: ( () ( (lv_op_2_0= ruleOpComparison ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )*
            loop40:
            do {
                int alt40=2;
                int LA40_0 = input.LA(1);

                if ( ((LA40_0>=43 && LA40_0<=46)) ) {
                    alt40=1;
                }


                switch (alt40) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2272:2: () ( (lv_op_2_0= ruleOpComparison ) ) ( (lv_right_3_0= ruleExprAdditive ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2272:2: ()
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2273:5: 
            	    {

            	            current = forceCreateModelElementAndSet(
            	                grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0(),
            	                current);
            	        

            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2278:2: ( (lv_op_2_0= ruleOpComparison ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2279:1: (lv_op_2_0= ruleOpComparison )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2279:1: (lv_op_2_0= ruleOpComparison )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2280:3: lv_op_2_0= ruleOpComparison
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprComparisonAccess().getOpOpComparisonParserRuleCall_1_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleOpComparison_in_ruleExprComparison5034);
            	    lv_op_2_0=ruleOpComparison();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getExprComparisonRule());
            	    	        }
            	           		set(
            	           			current, 
            	           			"op",
            	            		lv_op_2_0, 
            	            		"OpComparison");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2296:2: ( (lv_right_3_0= ruleExprAdditive ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2297:1: (lv_right_3_0= ruleExprAdditive )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2297:1: (lv_right_3_0= ruleExprAdditive )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2298:3: lv_right_3_0= ruleExprAdditive
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprComparisonAccess().getRightExprAdditiveParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprAdditive_in_ruleExprComparison5055);
            	    lv_right_3_0=ruleExprAdditive();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getExprComparisonRule());
            	    	        }
            	           		set(
            	           			current, 
            	           			"right",
            	            		lv_right_3_0, 
            	            		"ExprAdditive");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop40;
                }
            } while (true);


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExprComparison"


    // $ANTLR start "entryRuleOpComparison"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2322:1: entryRuleOpComparison returns [EObject current=null] : iv_ruleOpComparison= ruleOpComparison EOF ;
    public final EObject entryRuleOpComparison() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOpComparison = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2323:2: (iv_ruleOpComparison= ruleOpComparison EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2324:2: iv_ruleOpComparison= ruleOpComparison EOF
            {
             newCompositeNode(grammarAccess.getOpComparisonRule()); 
            pushFollow(FOLLOW_ruleOpComparison_in_entryRuleOpComparison5093);
            iv_ruleOpComparison=ruleOpComparison();

            state._fsp--;

             current =iv_ruleOpComparison; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleOpComparison5103); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpComparison"


    // $ANTLR start "ruleOpComparison"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2331:1: ruleOpComparison returns [EObject current=null] : ( ( () otherlv_1= '<=' ) | ( () otherlv_3= '<' ) | ( () otherlv_5= '>=' ) | ( () otherlv_7= '>' ) ) ;
    public final EObject ruleOpComparison() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_7=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2334:28: ( ( ( () otherlv_1= '<=' ) | ( () otherlv_3= '<' ) | ( () otherlv_5= '>=' ) | ( () otherlv_7= '>' ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2335:1: ( ( () otherlv_1= '<=' ) | ( () otherlv_3= '<' ) | ( () otherlv_5= '>=' ) | ( () otherlv_7= '>' ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2335:1: ( ( () otherlv_1= '<=' ) | ( () otherlv_3= '<' ) | ( () otherlv_5= '>=' ) | ( () otherlv_7= '>' ) )
            int alt41=4;
            switch ( input.LA(1) ) {
            case 43:
                {
                alt41=1;
                }
                break;
            case 44:
                {
                alt41=2;
                }
                break;
            case 45:
                {
                alt41=3;
                }
                break;
            case 46:
                {
                alt41=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;
            }

            switch (alt41) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2335:2: ( () otherlv_1= '<=' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2335:2: ( () otherlv_1= '<=' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2335:3: () otherlv_1= '<='
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2335:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2336:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpComparisonAccess().getOpLessEqAction_0_0(),
                                current);
                        

                    }

                    otherlv_1=(Token)match(input,43,FOLLOW_43_in_ruleOpComparison5150); 

                        	newLeafNode(otherlv_1, grammarAccess.getOpComparisonAccess().getLessThanSignEqualsSignKeyword_0_1());
                        

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2346:6: ( () otherlv_3= '<' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2346:6: ( () otherlv_3= '<' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2346:7: () otherlv_3= '<'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2346:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2347:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpComparisonAccess().getOpLessAction_1_0(),
                                current);
                        

                    }

                    otherlv_3=(Token)match(input,44,FOLLOW_44_in_ruleOpComparison5179); 

                        	newLeafNode(otherlv_3, grammarAccess.getOpComparisonAccess().getLessThanSignKeyword_1_1());
                        

                    }


                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2357:6: ( () otherlv_5= '>=' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2357:6: ( () otherlv_5= '>=' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2357:7: () otherlv_5= '>='
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2357:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2358:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpComparisonAccess().getOpGreaterEqAction_2_0(),
                                current);
                        

                    }

                    otherlv_5=(Token)match(input,45,FOLLOW_45_in_ruleOpComparison5208); 

                        	newLeafNode(otherlv_5, grammarAccess.getOpComparisonAccess().getGreaterThanSignEqualsSignKeyword_2_1());
                        

                    }


                    }
                    break;
                case 4 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2368:6: ( () otherlv_7= '>' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2368:6: ( () otherlv_7= '>' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2368:7: () otherlv_7= '>'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2368:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2369:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpComparisonAccess().getOpGreaterAction_3_0(),
                                current);
                        

                    }

                    otherlv_7=(Token)match(input,46,FOLLOW_46_in_ruleOpComparison5237); 

                        	newLeafNode(otherlv_7, grammarAccess.getOpComparisonAccess().getGreaterThanSignKeyword_3_1());
                        

                    }


                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpComparison"


    // $ANTLR start "entryRuleExprAdditive"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2386:1: entryRuleExprAdditive returns [EObject current=null] : iv_ruleExprAdditive= ruleExprAdditive EOF ;
    public final EObject entryRuleExprAdditive() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprAdditive = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2387:2: (iv_ruleExprAdditive= ruleExprAdditive EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2388:2: iv_ruleExprAdditive= ruleExprAdditive EOF
            {
             newCompositeNode(grammarAccess.getExprAdditiveRule()); 
            pushFollow(FOLLOW_ruleExprAdditive_in_entryRuleExprAdditive5274);
            iv_ruleExprAdditive=ruleExprAdditive();

            state._fsp--;

             current =iv_ruleExprAdditive; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprAdditive5284); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExprAdditive"


    // $ANTLR start "ruleExprAdditive"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2395:1: ruleExprAdditive returns [EObject current=null] : (this_ExprMult_0= ruleExprMult ( () ( (lv_op_2_0= ruleOpAdditive ) ) ( (lv_right_3_0= ruleExprMult ) ) )* ) ;
    public final EObject ruleExprAdditive() throws RecognitionException {
        EObject current = null;

        EObject this_ExprMult_0 = null;

        EObject lv_op_2_0 = null;

        EObject lv_right_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2398:28: ( (this_ExprMult_0= ruleExprMult ( () ( (lv_op_2_0= ruleOpAdditive ) ) ( (lv_right_3_0= ruleExprMult ) ) )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2399:1: (this_ExprMult_0= ruleExprMult ( () ( (lv_op_2_0= ruleOpAdditive ) ) ( (lv_right_3_0= ruleExprMult ) ) )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2399:1: (this_ExprMult_0= ruleExprMult ( () ( (lv_op_2_0= ruleOpAdditive ) ) ( (lv_right_3_0= ruleExprMult ) ) )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2400:5: this_ExprMult_0= ruleExprMult ( () ( (lv_op_2_0= ruleOpAdditive ) ) ( (lv_right_3_0= ruleExprMult ) ) )*
            {
             
                    newCompositeNode(grammarAccess.getExprAdditiveAccess().getExprMultParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleExprMult_in_ruleExprAdditive5331);
            this_ExprMult_0=ruleExprMult();

            state._fsp--;

             
                    current = this_ExprMult_0; 
                    afterParserOrEnumRuleCall();
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2408:1: ( () ( (lv_op_2_0= ruleOpAdditive ) ) ( (lv_right_3_0= ruleExprMult ) ) )*
            loop42:
            do {
                int alt42=2;
                int LA42_0 = input.LA(1);

                if ( ((LA42_0>=47 && LA42_0<=48)) ) {
                    alt42=1;
                }


                switch (alt42) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2408:2: () ( (lv_op_2_0= ruleOpAdditive ) ) ( (lv_right_3_0= ruleExprMult ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2408:2: ()
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2409:5: 
            	    {

            	            current = forceCreateModelElementAndSet(
            	                grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0(),
            	                current);
            	        

            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2414:2: ( (lv_op_2_0= ruleOpAdditive ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2415:1: (lv_op_2_0= ruleOpAdditive )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2415:1: (lv_op_2_0= ruleOpAdditive )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2416:3: lv_op_2_0= ruleOpAdditive
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprAdditiveAccess().getOpOpAdditiveParserRuleCall_1_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleOpAdditive_in_ruleExprAdditive5361);
            	    lv_op_2_0=ruleOpAdditive();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getExprAdditiveRule());
            	    	        }
            	           		set(
            	           			current, 
            	           			"op",
            	            		lv_op_2_0, 
            	            		"OpAdditive");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2432:2: ( (lv_right_3_0= ruleExprMult ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2433:1: (lv_right_3_0= ruleExprMult )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2433:1: (lv_right_3_0= ruleExprMult )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2434:3: lv_right_3_0= ruleExprMult
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprAdditiveAccess().getRightExprMultParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprMult_in_ruleExprAdditive5382);
            	    lv_right_3_0=ruleExprMult();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getExprAdditiveRule());
            	    	        }
            	           		set(
            	           			current, 
            	           			"right",
            	            		lv_right_3_0, 
            	            		"ExprMult");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop42;
                }
            } while (true);


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExprAdditive"


    // $ANTLR start "entryRuleOpAdditive"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2458:1: entryRuleOpAdditive returns [EObject current=null] : iv_ruleOpAdditive= ruleOpAdditive EOF ;
    public final EObject entryRuleOpAdditive() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOpAdditive = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2459:2: (iv_ruleOpAdditive= ruleOpAdditive EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2460:2: iv_ruleOpAdditive= ruleOpAdditive EOF
            {
             newCompositeNode(grammarAccess.getOpAdditiveRule()); 
            pushFollow(FOLLOW_ruleOpAdditive_in_entryRuleOpAdditive5420);
            iv_ruleOpAdditive=ruleOpAdditive();

            state._fsp--;

             current =iv_ruleOpAdditive; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleOpAdditive5430); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpAdditive"


    // $ANTLR start "ruleOpAdditive"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2467:1: ruleOpAdditive returns [EObject current=null] : ( ( () otherlv_1= '+' ) | ( () otherlv_3= '-' ) ) ;
    public final EObject ruleOpAdditive() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2470:28: ( ( ( () otherlv_1= '+' ) | ( () otherlv_3= '-' ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2471:1: ( ( () otherlv_1= '+' ) | ( () otherlv_3= '-' ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2471:1: ( ( () otherlv_1= '+' ) | ( () otherlv_3= '-' ) )
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==47) ) {
                alt43=1;
            }
            else if ( (LA43_0==48) ) {
                alt43=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;
            }
            switch (alt43) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2471:2: ( () otherlv_1= '+' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2471:2: ( () otherlv_1= '+' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2471:3: () otherlv_1= '+'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2471:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2472:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpAdditiveAccess().getOpPlusAction_0_0(),
                                current);
                        

                    }

                    otherlv_1=(Token)match(input,47,FOLLOW_47_in_ruleOpAdditive5477); 

                        	newLeafNode(otherlv_1, grammarAccess.getOpAdditiveAccess().getPlusSignKeyword_0_1());
                        

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2482:6: ( () otherlv_3= '-' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2482:6: ( () otherlv_3= '-' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2482:7: () otherlv_3= '-'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2482:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2483:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpAdditiveAccess().getOpMinusAction_1_0(),
                                current);
                        

                    }

                    otherlv_3=(Token)match(input,48,FOLLOW_48_in_ruleOpAdditive5506); 

                        	newLeafNode(otherlv_3, grammarAccess.getOpAdditiveAccess().getHyphenMinusKeyword_1_1());
                        

                    }


                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpAdditive"


    // $ANTLR start "entryRuleExprMult"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2500:1: entryRuleExprMult returns [EObject current=null] : iv_ruleExprMult= ruleExprMult EOF ;
    public final EObject entryRuleExprMult() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprMult = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2501:2: (iv_ruleExprMult= ruleExprMult EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2502:2: iv_ruleExprMult= ruleExprMult EOF
            {
             newCompositeNode(grammarAccess.getExprMultRule()); 
            pushFollow(FOLLOW_ruleExprMult_in_entryRuleExprMult5543);
            iv_ruleExprMult=ruleExprMult();

            state._fsp--;

             current =iv_ruleExprMult; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprMult5553); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExprMult"


    // $ANTLR start "ruleExprMult"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2509:1: ruleExprMult returns [EObject current=null] : (this_ExprSign_0= ruleExprSign ( () ( (lv_op_2_0= ruleOpMultiplicative ) ) ( (lv_right_3_0= ruleExprSign ) ) )* ) ;
    public final EObject ruleExprMult() throws RecognitionException {
        EObject current = null;

        EObject this_ExprSign_0 = null;

        EObject lv_op_2_0 = null;

        EObject lv_right_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2512:28: ( (this_ExprSign_0= ruleExprSign ( () ( (lv_op_2_0= ruleOpMultiplicative ) ) ( (lv_right_3_0= ruleExprSign ) ) )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2513:1: (this_ExprSign_0= ruleExprSign ( () ( (lv_op_2_0= ruleOpMultiplicative ) ) ( (lv_right_3_0= ruleExprSign ) ) )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2513:1: (this_ExprSign_0= ruleExprSign ( () ( (lv_op_2_0= ruleOpMultiplicative ) ) ( (lv_right_3_0= ruleExprSign ) ) )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2514:5: this_ExprSign_0= ruleExprSign ( () ( (lv_op_2_0= ruleOpMultiplicative ) ) ( (lv_right_3_0= ruleExprSign ) ) )*
            {
             
                    newCompositeNode(grammarAccess.getExprMultAccess().getExprSignParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleExprSign_in_ruleExprMult5600);
            this_ExprSign_0=ruleExprSign();

            state._fsp--;

             
                    current = this_ExprSign_0; 
                    afterParserOrEnumRuleCall();
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2522:1: ( () ( (lv_op_2_0= ruleOpMultiplicative ) ) ( (lv_right_3_0= ruleExprSign ) ) )*
            loop44:
            do {
                int alt44=2;
                int LA44_0 = input.LA(1);

                if ( (LA44_0==17||(LA44_0>=49 && LA44_0<=52)) ) {
                    alt44=1;
                }


                switch (alt44) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2522:2: () ( (lv_op_2_0= ruleOpMultiplicative ) ) ( (lv_right_3_0= ruleExprSign ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2522:2: ()
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2523:5: 
            	    {

            	            current = forceCreateModelElementAndSet(
            	                grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0(),
            	                current);
            	        

            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2528:2: ( (lv_op_2_0= ruleOpMultiplicative ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2529:1: (lv_op_2_0= ruleOpMultiplicative )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2529:1: (lv_op_2_0= ruleOpMultiplicative )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2530:3: lv_op_2_0= ruleOpMultiplicative
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprMultAccess().getOpOpMultiplicativeParserRuleCall_1_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleOpMultiplicative_in_ruleExprMult5630);
            	    lv_op_2_0=ruleOpMultiplicative();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getExprMultRule());
            	    	        }
            	           		set(
            	           			current, 
            	           			"op",
            	            		lv_op_2_0, 
            	            		"OpMultiplicative");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2546:2: ( (lv_right_3_0= ruleExprSign ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2547:1: (lv_right_3_0= ruleExprSign )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2547:1: (lv_right_3_0= ruleExprSign )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2548:3: lv_right_3_0= ruleExprSign
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprMultAccess().getRightExprSignParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprSign_in_ruleExprMult5651);
            	    lv_right_3_0=ruleExprSign();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getExprMultRule());
            	    	        }
            	           		set(
            	           			current, 
            	           			"right",
            	            		lv_right_3_0, 
            	            		"ExprSign");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop44;
                }
            } while (true);


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExprMult"


    // $ANTLR start "entryRuleOpMultiplicative"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2572:1: entryRuleOpMultiplicative returns [EObject current=null] : iv_ruleOpMultiplicative= ruleOpMultiplicative EOF ;
    public final EObject entryRuleOpMultiplicative() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOpMultiplicative = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2573:2: (iv_ruleOpMultiplicative= ruleOpMultiplicative EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2574:2: iv_ruleOpMultiplicative= ruleOpMultiplicative EOF
            {
             newCompositeNode(grammarAccess.getOpMultiplicativeRule()); 
            pushFollow(FOLLOW_ruleOpMultiplicative_in_entryRuleOpMultiplicative5689);
            iv_ruleOpMultiplicative=ruleOpMultiplicative();

            state._fsp--;

             current =iv_ruleOpMultiplicative; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleOpMultiplicative5699); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpMultiplicative"


    // $ANTLR start "ruleOpMultiplicative"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2581:1: ruleOpMultiplicative returns [EObject current=null] : ( ( () otherlv_1= '*' ) | ( () otherlv_3= '/' ) | ( () otherlv_5= '%' ) | ( () otherlv_7= 'mod' ) | ( () otherlv_9= 'div' ) ) ;
    public final EObject ruleOpMultiplicative() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_7=null;
        Token otherlv_9=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2584:28: ( ( ( () otherlv_1= '*' ) | ( () otherlv_3= '/' ) | ( () otherlv_5= '%' ) | ( () otherlv_7= 'mod' ) | ( () otherlv_9= 'div' ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2585:1: ( ( () otherlv_1= '*' ) | ( () otherlv_3= '/' ) | ( () otherlv_5= '%' ) | ( () otherlv_7= 'mod' ) | ( () otherlv_9= 'div' ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2585:1: ( ( () otherlv_1= '*' ) | ( () otherlv_3= '/' ) | ( () otherlv_5= '%' ) | ( () otherlv_7= 'mod' ) | ( () otherlv_9= 'div' ) )
            int alt45=5;
            switch ( input.LA(1) ) {
            case 17:
                {
                alt45=1;
                }
                break;
            case 49:
                {
                alt45=2;
                }
                break;
            case 50:
                {
                alt45=3;
                }
                break;
            case 51:
                {
                alt45=4;
                }
                break;
            case 52:
                {
                alt45=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 45, 0, input);

                throw nvae;
            }

            switch (alt45) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2585:2: ( () otherlv_1= '*' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2585:2: ( () otherlv_1= '*' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2585:3: () otherlv_1= '*'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2585:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2586:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpMultiplicativeAccess().getOpMultAction_0_0(),
                                current);
                        

                    }

                    otherlv_1=(Token)match(input,17,FOLLOW_17_in_ruleOpMultiplicative5746); 

                        	newLeafNode(otherlv_1, grammarAccess.getOpMultiplicativeAccess().getAsteriskKeyword_0_1());
                        

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2596:6: ( () otherlv_3= '/' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2596:6: ( () otherlv_3= '/' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2596:7: () otherlv_3= '/'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2596:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2597:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpMultiplicativeAccess().getOpDivRealAction_1_0(),
                                current);
                        

                    }

                    otherlv_3=(Token)match(input,49,FOLLOW_49_in_ruleOpMultiplicative5775); 

                        	newLeafNode(otherlv_3, grammarAccess.getOpMultiplicativeAccess().getSolidusKeyword_1_1());
                        

                    }


                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2607:6: ( () otherlv_5= '%' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2607:6: ( () otherlv_5= '%' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2607:7: () otherlv_5= '%'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2607:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2608:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpMultiplicativeAccess().getOpModRealAction_2_0(),
                                current);
                        

                    }

                    otherlv_5=(Token)match(input,50,FOLLOW_50_in_ruleOpMultiplicative5804); 

                        	newLeafNode(otherlv_5, grammarAccess.getOpMultiplicativeAccess().getPercentSignKeyword_2_1());
                        

                    }


                    }
                    break;
                case 4 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2618:6: ( () otherlv_7= 'mod' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2618:6: ( () otherlv_7= 'mod' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2618:7: () otherlv_7= 'mod'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2618:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2619:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpMultiplicativeAccess().getOpModIntAction_3_0(),
                                current);
                        

                    }

                    otherlv_7=(Token)match(input,51,FOLLOW_51_in_ruleOpMultiplicative5833); 

                        	newLeafNode(otherlv_7, grammarAccess.getOpMultiplicativeAccess().getModKeyword_3_1());
                        

                    }


                    }
                    break;
                case 5 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2629:6: ( () otherlv_9= 'div' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2629:6: ( () otherlv_9= 'div' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2629:7: () otherlv_9= 'div'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2629:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2630:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpMultiplicativeAccess().getOpDivIntAction_4_0(),
                                current);
                        

                    }

                    otherlv_9=(Token)match(input,52,FOLLOW_52_in_ruleOpMultiplicative5862); 

                        	newLeafNode(otherlv_9, grammarAccess.getOpMultiplicativeAccess().getDivKeyword_4_1());
                        

                    }


                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpMultiplicative"


    // $ANTLR start "entryRuleExprSign"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2647:1: entryRuleExprSign returns [EObject current=null] : iv_ruleExprSign= ruleExprSign EOF ;
    public final EObject entryRuleExprSign() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprSign = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2648:2: (iv_ruleExprSign= ruleExprSign EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2649:2: iv_ruleExprSign= ruleExprSign EOF
            {
             newCompositeNode(grammarAccess.getExprSignRule()); 
            pushFollow(FOLLOW_ruleExprSign_in_entryRuleExprSign5899);
            iv_ruleExprSign=ruleExprSign();

            state._fsp--;

             current =iv_ruleExprSign; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprSign5909); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExprSign"


    // $ANTLR start "ruleExprSign"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2656:1: ruleExprSign returns [EObject current=null] : ( ( () ( (lv_op_1_0= ruleOpAdditive ) ) ( (lv_right_2_0= ruleExprNot ) ) ) | this_ExprNot_3= ruleExprNot ) ;
    public final EObject ruleExprSign() throws RecognitionException {
        EObject current = null;

        EObject lv_op_1_0 = null;

        EObject lv_right_2_0 = null;

        EObject this_ExprNot_3 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2659:28: ( ( ( () ( (lv_op_1_0= ruleOpAdditive ) ) ( (lv_right_2_0= ruleExprNot ) ) ) | this_ExprNot_3= ruleExprNot ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2660:1: ( ( () ( (lv_op_1_0= ruleOpAdditive ) ) ( (lv_right_2_0= ruleExprNot ) ) ) | this_ExprNot_3= ruleExprNot )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2660:1: ( ( () ( (lv_op_1_0= ruleOpAdditive ) ) ( (lv_right_2_0= ruleExprNot ) ) ) | this_ExprNot_3= ruleExprNot )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( ((LA46_0>=47 && LA46_0<=48)) ) {
                alt46=1;
            }
            else if ( ((LA46_0>=RULE_ID && LA46_0<=RULE_STRING)||LA46_0==20||LA46_0==32||(LA46_0>=53 && LA46_0<=55)) ) {
                alt46=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;
            }
            switch (alt46) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2660:2: ( () ( (lv_op_1_0= ruleOpAdditive ) ) ( (lv_right_2_0= ruleExprNot ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2660:2: ( () ( (lv_op_1_0= ruleOpAdditive ) ) ( (lv_right_2_0= ruleExprNot ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2660:3: () ( (lv_op_1_0= ruleOpAdditive ) ) ( (lv_right_2_0= ruleExprNot ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2660:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2661:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprSignAccess().getExprSignAction_0_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2666:2: ( (lv_op_1_0= ruleOpAdditive ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2667:1: (lv_op_1_0= ruleOpAdditive )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2667:1: (lv_op_1_0= ruleOpAdditive )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2668:3: lv_op_1_0= ruleOpAdditive
                    {
                     
                    	        newCompositeNode(grammarAccess.getExprSignAccess().getOpOpAdditiveParserRuleCall_0_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleOpAdditive_in_ruleExprSign5965);
                    lv_op_1_0=ruleOpAdditive();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getExprSignRule());
                    	        }
                           		set(
                           			current, 
                           			"op",
                            		lv_op_1_0, 
                            		"OpAdditive");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2684:2: ( (lv_right_2_0= ruleExprNot ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2685:1: (lv_right_2_0= ruleExprNot )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2685:1: (lv_right_2_0= ruleExprNot )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2686:3: lv_right_2_0= ruleExprNot
                    {
                     
                    	        newCompositeNode(grammarAccess.getExprSignAccess().getRightExprNotParserRuleCall_0_2_0()); 
                    	    
                    pushFollow(FOLLOW_ruleExprNot_in_ruleExprSign5986);
                    lv_right_2_0=ruleExprNot();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getExprSignRule());
                    	        }
                           		set(
                           			current, 
                           			"right",
                            		lv_right_2_0, 
                            		"ExprNot");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2704:5: this_ExprNot_3= ruleExprNot
                    {
                     
                            newCompositeNode(grammarAccess.getExprSignAccess().getExprNotParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleExprNot_in_ruleExprSign6015);
                    this_ExprNot_3=ruleExprNot();

                    state._fsp--;

                     
                            current = this_ExprNot_3; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExprSign"


    // $ANTLR start "entryRuleExprNot"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2720:1: entryRuleExprNot returns [EObject current=null] : iv_ruleExprNot= ruleExprNot EOF ;
    public final EObject entryRuleExprNot() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprNot = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2721:2: (iv_ruleExprNot= ruleExprNot EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2722:2: iv_ruleExprNot= ruleExprNot EOF
            {
             newCompositeNode(grammarAccess.getExprNotRule()); 
            pushFollow(FOLLOW_ruleExprNot_in_entryRuleExprNot6050);
            iv_ruleExprNot=ruleExprNot();

            state._fsp--;

             current =iv_ruleExprNot; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprNot6060); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExprNot"


    // $ANTLR start "ruleExprNot"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2729:1: ruleExprNot returns [EObject current=null] : ( ( () otherlv_1= 'not' ( (lv_right_2_0= ruleExprMember ) ) ) | this_ExprMember_3= ruleExprMember ) ;
    public final EObject ruleExprNot() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        EObject lv_right_2_0 = null;

        EObject this_ExprMember_3 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2732:28: ( ( ( () otherlv_1= 'not' ( (lv_right_2_0= ruleExprMember ) ) ) | this_ExprMember_3= ruleExprMember ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2733:1: ( ( () otherlv_1= 'not' ( (lv_right_2_0= ruleExprMember ) ) ) | this_ExprMember_3= ruleExprMember )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2733:1: ( ( () otherlv_1= 'not' ( (lv_right_2_0= ruleExprMember ) ) ) | this_ExprMember_3= ruleExprMember )
            int alt47=2;
            int LA47_0 = input.LA(1);

            if ( (LA47_0==53) ) {
                alt47=1;
            }
            else if ( ((LA47_0>=RULE_ID && LA47_0<=RULE_STRING)||LA47_0==20||LA47_0==32||(LA47_0>=54 && LA47_0<=55)) ) {
                alt47=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;
            }
            switch (alt47) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2733:2: ( () otherlv_1= 'not' ( (lv_right_2_0= ruleExprMember ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2733:2: ( () otherlv_1= 'not' ( (lv_right_2_0= ruleExprMember ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2733:3: () otherlv_1= 'not' ( (lv_right_2_0= ruleExprMember ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2733:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2734:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprNotAccess().getExprNotAction_0_0(),
                                current);
                        

                    }

                    otherlv_1=(Token)match(input,53,FOLLOW_53_in_ruleExprNot6107); 

                        	newLeafNode(otherlv_1, grammarAccess.getExprNotAccess().getNotKeyword_0_1());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2743:1: ( (lv_right_2_0= ruleExprMember ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2744:1: (lv_right_2_0= ruleExprMember )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2744:1: (lv_right_2_0= ruleExprMember )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2745:3: lv_right_2_0= ruleExprMember
                    {
                     
                    	        newCompositeNode(grammarAccess.getExprNotAccess().getRightExprMemberParserRuleCall_0_2_0()); 
                    	    
                    pushFollow(FOLLOW_ruleExprMember_in_ruleExprNot6128);
                    lv_right_2_0=ruleExprMember();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getExprNotRule());
                    	        }
                           		set(
                           			current, 
                           			"right",
                            		lv_right_2_0, 
                            		"ExprMember");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2763:5: this_ExprMember_3= ruleExprMember
                    {
                     
                            newCompositeNode(grammarAccess.getExprNotAccess().getExprMemberParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleExprMember_in_ruleExprNot6157);
                    this_ExprMember_3=ruleExprMember();

                    state._fsp--;

                     
                            current = this_ExprMember_3; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExprNot"


    // $ANTLR start "entryRuleExprMember"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2779:1: entryRuleExprMember returns [EObject current=null] : iv_ruleExprMember= ruleExprMember EOF ;
    public final EObject entryRuleExprMember() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprMember = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2780:2: (iv_ruleExprMember= ruleExprMember EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2781:2: iv_ruleExprMember= ruleExprMember EOF
            {
             newCompositeNode(grammarAccess.getExprMemberRule()); 
            pushFollow(FOLLOW_ruleExprMember_in_entryRuleExprMember6192);
            iv_ruleExprMember=ruleExprMember();

            state._fsp--;

             current =iv_ruleExprMember; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprMember6202); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExprMember"


    // $ANTLR start "ruleExprMember"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2788:1: ruleExprMember returns [EObject current=null] : (this_ExprSingle_0= ruleExprSingle ( () otherlv_2= '.' ( (lv_message_3_0= ruleExprMemberRight ) ) )* ) ;
    public final EObject ruleExprMember() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        EObject this_ExprSingle_0 = null;

        EObject lv_message_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2791:28: ( (this_ExprSingle_0= ruleExprSingle ( () otherlv_2= '.' ( (lv_message_3_0= ruleExprMemberRight ) ) )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2792:1: (this_ExprSingle_0= ruleExprSingle ( () otherlv_2= '.' ( (lv_message_3_0= ruleExprMemberRight ) ) )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2792:1: (this_ExprSingle_0= ruleExprSingle ( () otherlv_2= '.' ( (lv_message_3_0= ruleExprMemberRight ) ) )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2793:5: this_ExprSingle_0= ruleExprSingle ( () otherlv_2= '.' ( (lv_message_3_0= ruleExprMemberRight ) ) )*
            {
             
                    newCompositeNode(grammarAccess.getExprMemberAccess().getExprSingleParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleExprSingle_in_ruleExprMember6249);
            this_ExprSingle_0=ruleExprSingle();

            state._fsp--;

             
                    current = this_ExprSingle_0; 
                    afterParserOrEnumRuleCall();
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2801:1: ( () otherlv_2= '.' ( (lv_message_3_0= ruleExprMemberRight ) ) )*
            loop48:
            do {
                int alt48=2;
                int LA48_0 = input.LA(1);

                if ( (LA48_0==16) ) {
                    alt48=1;
                }


                switch (alt48) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2801:2: () otherlv_2= '.' ( (lv_message_3_0= ruleExprMemberRight ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2801:2: ()
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2802:5: 
            	    {

            	            current = forceCreateModelElementAndSet(
            	                grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0(),
            	                current);
            	        

            	    }

            	    otherlv_2=(Token)match(input,16,FOLLOW_16_in_ruleExprMember6270); 

            	        	newLeafNode(otherlv_2, grammarAccess.getExprMemberAccess().getFullStopKeyword_1_1());
            	        
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2811:1: ( (lv_message_3_0= ruleExprMemberRight ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2812:1: (lv_message_3_0= ruleExprMemberRight )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2812:1: (lv_message_3_0= ruleExprMemberRight )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2813:3: lv_message_3_0= ruleExprMemberRight
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprMemberAccess().getMessageExprMemberRightParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprMemberRight_in_ruleExprMember6291);
            	    lv_message_3_0=ruleExprMemberRight();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getExprMemberRule());
            	    	        }
            	           		set(
            	           			current, 
            	           			"message",
            	            		lv_message_3_0, 
            	            		"ExprMemberRight");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop48;
                }
            } while (true);


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExprMember"


    // $ANTLR start "entryRuleExprMemberRight"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2837:1: entryRuleExprMemberRight returns [EObject current=null] : iv_ruleExprMemberRight= ruleExprMemberRight EOF ;
    public final EObject entryRuleExprMemberRight() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprMemberRight = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2838:2: (iv_ruleExprMemberRight= ruleExprMemberRight EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2839:2: iv_ruleExprMemberRight= ruleExprMemberRight EOF
            {
             newCompositeNode(grammarAccess.getExprMemberRightRule()); 
            pushFollow(FOLLOW_ruleExprMemberRight_in_entryRuleExprMemberRight6329);
            iv_ruleExprMemberRight=ruleExprMemberRight();

            state._fsp--;

             current =iv_ruleExprMemberRight; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprMemberRight6339); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExprMemberRight"


    // $ANTLR start "ruleExprMemberRight"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2846:1: ruleExprMemberRight returns [EObject current=null] : ( ( (otherlv_0= RULE_ID ) ) (otherlv_1= '(' ( ( (lv_params_2_0= ruleExpr ) ) (otherlv_3= ',' ( (lv_params_4_0= ruleExpr ) ) )* )? otherlv_5= ')' )? ) ;
    public final EObject ruleExprMemberRight() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        EObject lv_params_2_0 = null;

        EObject lv_params_4_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2849:28: ( ( ( (otherlv_0= RULE_ID ) ) (otherlv_1= '(' ( ( (lv_params_2_0= ruleExpr ) ) (otherlv_3= ',' ( (lv_params_4_0= ruleExpr ) ) )* )? otherlv_5= ')' )? ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2850:1: ( ( (otherlv_0= RULE_ID ) ) (otherlv_1= '(' ( ( (lv_params_2_0= ruleExpr ) ) (otherlv_3= ',' ( (lv_params_4_0= ruleExpr ) ) )* )? otherlv_5= ')' )? )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2850:1: ( ( (otherlv_0= RULE_ID ) ) (otherlv_1= '(' ( ( (lv_params_2_0= ruleExpr ) ) (otherlv_3= ',' ( (lv_params_4_0= ruleExpr ) ) )* )? otherlv_5= ')' )? )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2850:2: ( (otherlv_0= RULE_ID ) ) (otherlv_1= '(' ( ( (lv_params_2_0= ruleExpr ) ) (otherlv_3= ',' ( (lv_params_4_0= ruleExpr ) ) )* )? otherlv_5= ')' )?
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2850:2: ( (otherlv_0= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2851:1: (otherlv_0= RULE_ID )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2851:1: (otherlv_0= RULE_ID )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2852:3: otherlv_0= RULE_ID
            {

            			if (current==null) {
            	            current = createModelElement(grammarAccess.getExprMemberRightRule());
            	        }
                    
            otherlv_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleExprMemberRight6384); 

            		newLeafNode(otherlv_0, grammarAccess.getExprMemberRightAccess().getNameValClassMemberCrossReference_0_0()); 
            	

            }


            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2863:2: (otherlv_1= '(' ( ( (lv_params_2_0= ruleExpr ) ) (otherlv_3= ',' ( (lv_params_4_0= ruleExpr ) ) )* )? otherlv_5= ')' )?
            int alt51=2;
            int LA51_0 = input.LA(1);

            if ( (LA51_0==20) ) {
                alt51=1;
            }
            switch (alt51) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2863:4: otherlv_1= '(' ( ( (lv_params_2_0= ruleExpr ) ) (otherlv_3= ',' ( (lv_params_4_0= ruleExpr ) ) )* )? otherlv_5= ')'
                    {
                    otherlv_1=(Token)match(input,20,FOLLOW_20_in_ruleExprMemberRight6397); 

                        	newLeafNode(otherlv_1, grammarAccess.getExprMemberRightAccess().getLeftParenthesisKeyword_1_0());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2867:1: ( ( (lv_params_2_0= ruleExpr ) ) (otherlv_3= ',' ( (lv_params_4_0= ruleExpr ) ) )* )?
                    int alt50=2;
                    int LA50_0 = input.LA(1);

                    if ( ((LA50_0>=RULE_ID && LA50_0<=RULE_STRING)||LA50_0==20||LA50_0==32||(LA50_0>=47 && LA50_0<=48)||(LA50_0>=53 && LA50_0<=55)) ) {
                        alt50=1;
                    }
                    switch (alt50) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2867:2: ( (lv_params_2_0= ruleExpr ) ) (otherlv_3= ',' ( (lv_params_4_0= ruleExpr ) ) )*
                            {
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2867:2: ( (lv_params_2_0= ruleExpr ) )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2868:1: (lv_params_2_0= ruleExpr )
                            {
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2868:1: (lv_params_2_0= ruleExpr )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2869:3: lv_params_2_0= ruleExpr
                            {
                             
                            	        newCompositeNode(grammarAccess.getExprMemberRightAccess().getParamsExprParserRuleCall_1_1_0_0()); 
                            	    
                            pushFollow(FOLLOW_ruleExpr_in_ruleExprMemberRight6419);
                            lv_params_2_0=ruleExpr();

                            state._fsp--;


                            	        if (current==null) {
                            	            current = createModelElementForParent(grammarAccess.getExprMemberRightRule());
                            	        }
                                   		add(
                                   			current, 
                                   			"params",
                                    		lv_params_2_0, 
                                    		"Expr");
                            	        afterParserOrEnumRuleCall();
                            	    

                            }


                            }

                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2885:2: (otherlv_3= ',' ( (lv_params_4_0= ruleExpr ) ) )*
                            loop49:
                            do {
                                int alt49=2;
                                int LA49_0 = input.LA(1);

                                if ( (LA49_0==21) ) {
                                    alt49=1;
                                }


                                switch (alt49) {
                            	case 1 :
                            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2885:4: otherlv_3= ',' ( (lv_params_4_0= ruleExpr ) )
                            	    {
                            	    otherlv_3=(Token)match(input,21,FOLLOW_21_in_ruleExprMemberRight6432); 

                            	        	newLeafNode(otherlv_3, grammarAccess.getExprMemberRightAccess().getCommaKeyword_1_1_1_0());
                            	        
                            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2889:1: ( (lv_params_4_0= ruleExpr ) )
                            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2890:1: (lv_params_4_0= ruleExpr )
                            	    {
                            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2890:1: (lv_params_4_0= ruleExpr )
                            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2891:3: lv_params_4_0= ruleExpr
                            	    {
                            	     
                            	    	        newCompositeNode(grammarAccess.getExprMemberRightAccess().getParamsExprParserRuleCall_1_1_1_1_0()); 
                            	    	    
                            	    pushFollow(FOLLOW_ruleExpr_in_ruleExprMemberRight6453);
                            	    lv_params_4_0=ruleExpr();

                            	    state._fsp--;


                            	    	        if (current==null) {
                            	    	            current = createModelElementForParent(grammarAccess.getExprMemberRightRule());
                            	    	        }
                            	           		add(
                            	           			current, 
                            	           			"params",
                            	            		lv_params_4_0, 
                            	            		"Expr");
                            	    	        afterParserOrEnumRuleCall();
                            	    	    

                            	    }


                            	    }


                            	    }
                            	    break;

                            	default :
                            	    break loop49;
                                }
                            } while (true);


                            }
                            break;

                    }

                    otherlv_5=(Token)match(input,22,FOLLOW_22_in_ruleExprMemberRight6469); 

                        	newLeafNode(otherlv_5, grammarAccess.getExprMemberRightAccess().getRightParenthesisKeyword_1_2());
                        

                    }
                    break;

            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExprMemberRight"


    // $ANTLR start "entryRuleExprSingle"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2919:1: entryRuleExprSingle returns [EObject current=null] : iv_ruleExprSingle= ruleExprSingle EOF ;
    public final EObject entryRuleExprSingle() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprSingle = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2920:2: (iv_ruleExprSingle= ruleExprSingle EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2921:2: iv_ruleExprSingle= ruleExprSingle EOF
            {
             newCompositeNode(grammarAccess.getExprSingleRule()); 
            pushFollow(FOLLOW_ruleExprSingle_in_entryRuleExprSingle6507);
            iv_ruleExprSingle=ruleExprSingle();

            state._fsp--;

             current =iv_ruleExprSingle; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprSingle6517); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExprSingle"


    // $ANTLR start "ruleExprSingle"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2928:1: ruleExprSingle returns [EObject current=null] : (this_ExprAtomic_0= ruleExprAtomic | (otherlv_1= '(' this_Expr_2= ruleExpr otherlv_3= ')' ) | ( () ( (lv_intVal_5_0= RULE_INT ) ) ) | ( () ( (lv_numVal_7_0= ruleNumber ) ) ) | ( () ( (lv_strVal_9_0= RULE_STRING ) ) ) | ( () ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) ) ) | ( () otherlv_13= 'function' ( (otherlv_14= RULE_ID ) ) ) ) ;
    public final EObject ruleExprSingle() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        Token lv_intVal_5_0=null;
        Token lv_strVal_9_0=null;
        Token lv_boolVal_11_1=null;
        Token lv_boolVal_11_2=null;
        Token otherlv_13=null;
        Token otherlv_14=null;
        EObject this_ExprAtomic_0 = null;

        EObject this_Expr_2 = null;

        AntlrDatatypeRuleToken lv_numVal_7_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2931:28: ( (this_ExprAtomic_0= ruleExprAtomic | (otherlv_1= '(' this_Expr_2= ruleExpr otherlv_3= ')' ) | ( () ( (lv_intVal_5_0= RULE_INT ) ) ) | ( () ( (lv_numVal_7_0= ruleNumber ) ) ) | ( () ( (lv_strVal_9_0= RULE_STRING ) ) ) | ( () ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) ) ) | ( () otherlv_13= 'function' ( (otherlv_14= RULE_ID ) ) ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2932:1: (this_ExprAtomic_0= ruleExprAtomic | (otherlv_1= '(' this_Expr_2= ruleExpr otherlv_3= ')' ) | ( () ( (lv_intVal_5_0= RULE_INT ) ) ) | ( () ( (lv_numVal_7_0= ruleNumber ) ) ) | ( () ( (lv_strVal_9_0= RULE_STRING ) ) ) | ( () ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) ) ) | ( () otherlv_13= 'function' ( (otherlv_14= RULE_ID ) ) ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2932:1: (this_ExprAtomic_0= ruleExprAtomic | (otherlv_1= '(' this_Expr_2= ruleExpr otherlv_3= ')' ) | ( () ( (lv_intVal_5_0= RULE_INT ) ) ) | ( () ( (lv_numVal_7_0= ruleNumber ) ) ) | ( () ( (lv_strVal_9_0= RULE_STRING ) ) ) | ( () ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) ) ) | ( () otherlv_13= 'function' ( (otherlv_14= RULE_ID ) ) ) )
            int alt53=7;
            alt53 = dfa53.predict(input);
            switch (alt53) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2933:5: this_ExprAtomic_0= ruleExprAtomic
                    {
                     
                            newCompositeNode(grammarAccess.getExprSingleAccess().getExprAtomicParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleExprAtomic_in_ruleExprSingle6564);
                    this_ExprAtomic_0=ruleExprAtomic();

                    state._fsp--;

                     
                            current = this_ExprAtomic_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2942:6: (otherlv_1= '(' this_Expr_2= ruleExpr otherlv_3= ')' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2942:6: (otherlv_1= '(' this_Expr_2= ruleExpr otherlv_3= ')' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2942:8: otherlv_1= '(' this_Expr_2= ruleExpr otherlv_3= ')'
                    {
                    otherlv_1=(Token)match(input,20,FOLLOW_20_in_ruleExprSingle6582); 

                        	newLeafNode(otherlv_1, grammarAccess.getExprSingleAccess().getLeftParenthesisKeyword_1_0());
                        
                     
                            newCompositeNode(grammarAccess.getExprSingleAccess().getExprParserRuleCall_1_1()); 
                        
                    pushFollow(FOLLOW_ruleExpr_in_ruleExprSingle6604);
                    this_Expr_2=ruleExpr();

                    state._fsp--;

                     
                            current = this_Expr_2; 
                            afterParserOrEnumRuleCall();
                        
                    otherlv_3=(Token)match(input,22,FOLLOW_22_in_ruleExprSingle6615); 

                        	newLeafNode(otherlv_3, grammarAccess.getExprSingleAccess().getRightParenthesisKeyword_1_2());
                        

                    }


                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2960:6: ( () ( (lv_intVal_5_0= RULE_INT ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2960:6: ( () ( (lv_intVal_5_0= RULE_INT ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2960:7: () ( (lv_intVal_5_0= RULE_INT ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2960:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2961:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprSingleAccess().getExprIntValAction_2_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2966:2: ( (lv_intVal_5_0= RULE_INT ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2967:1: (lv_intVal_5_0= RULE_INT )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2967:1: (lv_intVal_5_0= RULE_INT )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2968:3: lv_intVal_5_0= RULE_INT
                    {
                    lv_intVal_5_0=(Token)match(input,RULE_INT,FOLLOW_RULE_INT_in_ruleExprSingle6649); 

                    			newLeafNode(lv_intVal_5_0, grammarAccess.getExprSingleAccess().getIntValINTTerminalRuleCall_2_1_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getExprSingleRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"intVal",
                            		lv_intVal_5_0, 
                            		"INT");
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 4 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2985:6: ( () ( (lv_numVal_7_0= ruleNumber ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2985:6: ( () ( (lv_numVal_7_0= ruleNumber ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2985:7: () ( (lv_numVal_7_0= ruleNumber ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2985:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2986:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprSingleAccess().getExprNumValAction_3_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2991:2: ( (lv_numVal_7_0= ruleNumber ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2992:1: (lv_numVal_7_0= ruleNumber )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2992:1: (lv_numVal_7_0= ruleNumber )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2993:3: lv_numVal_7_0= ruleNumber
                    {
                     
                    	        newCompositeNode(grammarAccess.getExprSingleAccess().getNumValNumberParserRuleCall_3_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleNumber_in_ruleExprSingle6692);
                    lv_numVal_7_0=ruleNumber();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getExprSingleRule());
                    	        }
                           		set(
                           			current, 
                           			"numVal",
                            		lv_numVal_7_0, 
                            		"Number");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 5 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3010:6: ( () ( (lv_strVal_9_0= RULE_STRING ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3010:6: ( () ( (lv_strVal_9_0= RULE_STRING ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3010:7: () ( (lv_strVal_9_0= RULE_STRING ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3010:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3011:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprSingleAccess().getExprStrvalAction_4_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3016:2: ( (lv_strVal_9_0= RULE_STRING ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3017:1: (lv_strVal_9_0= RULE_STRING )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3017:1: (lv_strVal_9_0= RULE_STRING )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3018:3: lv_strVal_9_0= RULE_STRING
                    {
                    lv_strVal_9_0=(Token)match(input,RULE_STRING,FOLLOW_RULE_STRING_in_ruleExprSingle6726); 

                    			newLeafNode(lv_strVal_9_0, grammarAccess.getExprSingleAccess().getStrValSTRINGTerminalRuleCall_4_1_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getExprSingleRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"strVal",
                            		lv_strVal_9_0, 
                            		"STRING");
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 6 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3035:6: ( () ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3035:6: ( () ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3035:7: () ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3035:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3036:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprSingleAccess().getExprBoolValAction_5_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3041:2: ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3042:1: ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3042:1: ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3043:1: (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3043:1: (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' )
                    int alt52=2;
                    int LA52_0 = input.LA(1);

                    if ( (LA52_0==54) ) {
                        alt52=1;
                    }
                    else if ( (LA52_0==55) ) {
                        alt52=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 52, 0, input);

                        throw nvae;
                    }
                    switch (alt52) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3044:3: lv_boolVal_11_1= 'true'
                            {
                            lv_boolVal_11_1=(Token)match(input,54,FOLLOW_54_in_ruleExprSingle6768); 

                                    newLeafNode(lv_boolVal_11_1, grammarAccess.getExprSingleAccess().getBoolValTrueKeyword_5_1_0_0());
                                

                            	        if (current==null) {
                            	            current = createModelElement(grammarAccess.getExprSingleRule());
                            	        }
                                   		setWithLastConsumed(current, "boolVal", lv_boolVal_11_1, null);
                            	    

                            }
                            break;
                        case 2 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3056:8: lv_boolVal_11_2= 'false'
                            {
                            lv_boolVal_11_2=(Token)match(input,55,FOLLOW_55_in_ruleExprSingle6797); 

                                    newLeafNode(lv_boolVal_11_2, grammarAccess.getExprSingleAccess().getBoolValFalseKeyword_5_1_0_1());
                                

                            	        if (current==null) {
                            	            current = createModelElement(grammarAccess.getExprSingleRule());
                            	        }
                                   		setWithLastConsumed(current, "boolVal", lv_boolVal_11_2, null);
                            	    

                            }
                            break;

                    }


                    }


                    }


                    }


                    }
                    break;
                case 7 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3072:6: ( () otherlv_13= 'function' ( (otherlv_14= RULE_ID ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3072:6: ( () otherlv_13= 'function' ( (otherlv_14= RULE_ID ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3072:7: () otherlv_13= 'function' ( (otherlv_14= RULE_ID ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3072:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3073:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprSingleAccess().getExprFuncRefAction_6_0(),
                                current);
                        

                    }

                    otherlv_13=(Token)match(input,32,FOLLOW_32_in_ruleExprSingle6842); 

                        	newLeafNode(otherlv_13, grammarAccess.getExprSingleAccess().getFunctionKeyword_6_1());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3082:1: ( (otherlv_14= RULE_ID ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3083:1: (otherlv_14= RULE_ID )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3083:1: (otherlv_14= RULE_ID )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3084:3: otherlv_14= RULE_ID
                    {

                    			if (current==null) {
                    	            current = createModelElement(grammarAccess.getExprSingleRule());
                    	        }
                            
                    otherlv_14=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleExprSingle6862); 

                    		newLeafNode(otherlv_14, grammarAccess.getExprSingleAccess().getNameValFuncDefCrossReference_6_2_0()); 
                    	

                    }


                    }


                    }


                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExprSingle"


    // $ANTLR start "entryRuleNumber"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3103:1: entryRuleNumber returns [String current=null] : iv_ruleNumber= ruleNumber EOF ;
    public final String entryRuleNumber() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleNumber = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3104:2: (iv_ruleNumber= ruleNumber EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3105:2: iv_ruleNumber= ruleNumber EOF
            {
             newCompositeNode(grammarAccess.getNumberRule()); 
            pushFollow(FOLLOW_ruleNumber_in_entryRuleNumber6900);
            iv_ruleNumber=ruleNumber();

            state._fsp--;

             current =iv_ruleNumber.getText(); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleNumber6911); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleNumber"


    // $ANTLR start "ruleNumber"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3112:1: ruleNumber returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_INT_0= RULE_INT kw= '.' this_INT_2= RULE_INT ) ;
    public final AntlrDatatypeRuleToken ruleNumber() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_INT_0=null;
        Token kw=null;
        Token this_INT_2=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3115:28: ( (this_INT_0= RULE_INT kw= '.' this_INT_2= RULE_INT ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3116:1: (this_INT_0= RULE_INT kw= '.' this_INT_2= RULE_INT )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3116:1: (this_INT_0= RULE_INT kw= '.' this_INT_2= RULE_INT )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3116:6: this_INT_0= RULE_INT kw= '.' this_INT_2= RULE_INT
            {
            this_INT_0=(Token)match(input,RULE_INT,FOLLOW_RULE_INT_in_ruleNumber6951); 

            		current.merge(this_INT_0);
                
             
                newLeafNode(this_INT_0, grammarAccess.getNumberAccess().getINTTerminalRuleCall_0()); 
                
            kw=(Token)match(input,16,FOLLOW_16_in_ruleNumber6969); 

                    current.merge(kw);
                    newLeafNode(kw, grammarAccess.getNumberAccess().getFullStopKeyword_1()); 
                
            this_INT_2=(Token)match(input,RULE_INT,FOLLOW_RULE_INT_in_ruleNumber6984); 

            		current.merge(this_INT_2);
                
             
                newLeafNode(this_INT_2, grammarAccess.getNumberAccess().getINTTerminalRuleCall_2()); 
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleNumber"


    // $ANTLR start "entryRuleExprAtomic"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3144:1: entryRuleExprAtomic returns [EObject current=null] : iv_ruleExprAtomic= ruleExprAtomic EOF ;
    public final EObject entryRuleExprAtomic() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprAtomic = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3145:2: (iv_ruleExprAtomic= ruleExprAtomic EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3146:2: iv_ruleExprAtomic= ruleExprAtomic EOF
            {
             newCompositeNode(grammarAccess.getExprAtomicRule()); 
            pushFollow(FOLLOW_ruleExprAtomic_in_entryRuleExprAtomic7029);
            iv_ruleExprAtomic=ruleExprAtomic();

            state._fsp--;

             current =iv_ruleExprAtomic; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprAtomic7039); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExprAtomic"


    // $ANTLR start "ruleExprAtomic"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3153:1: ruleExprAtomic returns [EObject current=null] : (this_ExprFunctionCall_0= ruleExprFunctionCall | ( () ( (otherlv_2= RULE_ID ) ) (otherlv_3= '[' ( (lv_arrayIndizes_4_0= ruleExpr ) ) otherlv_5= ']' )* ) ) ;
    public final EObject ruleExprAtomic() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        EObject this_ExprFunctionCall_0 = null;

        EObject lv_arrayIndizes_4_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3156:28: ( (this_ExprFunctionCall_0= ruleExprFunctionCall | ( () ( (otherlv_2= RULE_ID ) ) (otherlv_3= '[' ( (lv_arrayIndizes_4_0= ruleExpr ) ) otherlv_5= ']' )* ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3157:1: (this_ExprFunctionCall_0= ruleExprFunctionCall | ( () ( (otherlv_2= RULE_ID ) ) (otherlv_3= '[' ( (lv_arrayIndizes_4_0= ruleExpr ) ) otherlv_5= ']' )* ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3157:1: (this_ExprFunctionCall_0= ruleExprFunctionCall | ( () ( (otherlv_2= RULE_ID ) ) (otherlv_3= '[' ( (lv_arrayIndizes_4_0= ruleExpr ) ) otherlv_5= ']' )* ) )
            int alt55=2;
            int LA55_0 = input.LA(1);

            if ( (LA55_0==RULE_ID) ) {
                int LA55_1 = input.LA(2);

                if ( (LA55_1==EOF||LA55_1==RULE_NL||LA55_1==13||(LA55_1>=16 && LA55_1<=17)||(LA55_1>=21 && LA55_1<=22)||LA55_1==28||(LA55_1>=30 && LA55_1<=31)||(LA55_1>=37 && LA55_1<=52)) ) {
                    alt55=2;
                }
                else if ( (LA55_1==20) ) {
                    alt55=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 55, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 55, 0, input);

                throw nvae;
            }
            switch (alt55) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3158:5: this_ExprFunctionCall_0= ruleExprFunctionCall
                    {
                     
                            newCompositeNode(grammarAccess.getExprAtomicAccess().getExprFunctionCallParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleExprFunctionCall_in_ruleExprAtomic7086);
                    this_ExprFunctionCall_0=ruleExprFunctionCall();

                    state._fsp--;

                     
                            current = this_ExprFunctionCall_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3167:6: ( () ( (otherlv_2= RULE_ID ) ) (otherlv_3= '[' ( (lv_arrayIndizes_4_0= ruleExpr ) ) otherlv_5= ']' )* )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3167:6: ( () ( (otherlv_2= RULE_ID ) ) (otherlv_3= '[' ( (lv_arrayIndizes_4_0= ruleExpr ) ) otherlv_5= ']' )* )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3167:7: () ( (otherlv_2= RULE_ID ) ) (otherlv_3= '[' ( (lv_arrayIndizes_4_0= ruleExpr ) ) otherlv_5= ']' )*
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3167:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3168:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprAtomicAccess().getExprIdentifierAction_1_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3173:2: ( (otherlv_2= RULE_ID ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3174:1: (otherlv_2= RULE_ID )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3174:1: (otherlv_2= RULE_ID )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3175:3: otherlv_2= RULE_ID
                    {

                    			if (current==null) {
                    	            current = createModelElement(grammarAccess.getExprAtomicRule());
                    	        }
                            
                    otherlv_2=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleExprAtomic7121); 

                    		newLeafNode(otherlv_2, grammarAccess.getExprAtomicAccess().getNameValVarDefCrossReference_1_1_0()); 
                    	

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3186:2: (otherlv_3= '[' ( (lv_arrayIndizes_4_0= ruleExpr ) ) otherlv_5= ']' )*
                    loop54:
                    do {
                        int alt54=2;
                        int LA54_0 = input.LA(1);

                        if ( (LA54_0==30) ) {
                            alt54=1;
                        }


                        switch (alt54) {
                    	case 1 :
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3186:4: otherlv_3= '[' ( (lv_arrayIndizes_4_0= ruleExpr ) ) otherlv_5= ']'
                    	    {
                    	    otherlv_3=(Token)match(input,30,FOLLOW_30_in_ruleExprAtomic7134); 

                    	        	newLeafNode(otherlv_3, grammarAccess.getExprAtomicAccess().getLeftSquareBracketKeyword_1_2_0());
                    	        
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3190:1: ( (lv_arrayIndizes_4_0= ruleExpr ) )
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3191:1: (lv_arrayIndizes_4_0= ruleExpr )
                    	    {
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3191:1: (lv_arrayIndizes_4_0= ruleExpr )
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3192:3: lv_arrayIndizes_4_0= ruleExpr
                    	    {
                    	     
                    	    	        newCompositeNode(grammarAccess.getExprAtomicAccess().getArrayIndizesExprParserRuleCall_1_2_1_0()); 
                    	    	    
                    	    pushFollow(FOLLOW_ruleExpr_in_ruleExprAtomic7155);
                    	    lv_arrayIndizes_4_0=ruleExpr();

                    	    state._fsp--;


                    	    	        if (current==null) {
                    	    	            current = createModelElementForParent(grammarAccess.getExprAtomicRule());
                    	    	        }
                    	           		add(
                    	           			current, 
                    	           			"arrayIndizes",
                    	            		lv_arrayIndizes_4_0, 
                    	            		"Expr");
                    	    	        afterParserOrEnumRuleCall();
                    	    	    

                    	    }


                    	    }

                    	    otherlv_5=(Token)match(input,31,FOLLOW_31_in_ruleExprAtomic7167); 

                    	        	newLeafNode(otherlv_5, grammarAccess.getExprAtomicAccess().getRightSquareBracketKeyword_1_2_2());
                    	        

                    	    }
                    	    break;

                    	default :
                    	    break loop54;
                        }
                    } while (true);


                    }


                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExprAtomic"


    // $ANTLR start "entryRuleExprFunctionCall"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3220:1: entryRuleExprFunctionCall returns [EObject current=null] : iv_ruleExprFunctionCall= ruleExprFunctionCall EOF ;
    public final EObject entryRuleExprFunctionCall() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprFunctionCall = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3221:2: (iv_ruleExprFunctionCall= ruleExprFunctionCall EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3222:2: iv_ruleExprFunctionCall= ruleExprFunctionCall EOF
            {
             newCompositeNode(grammarAccess.getExprFunctionCallRule()); 
            pushFollow(FOLLOW_ruleExprFunctionCall_in_entryRuleExprFunctionCall7206);
            iv_ruleExprFunctionCall=ruleExprFunctionCall();

            state._fsp--;

             current =iv_ruleExprFunctionCall; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprFunctionCall7216); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExprFunctionCall"


    // $ANTLR start "ruleExprFunctionCall"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3229:1: ruleExprFunctionCall returns [EObject current=null] : ( () ( (otherlv_1= RULE_ID ) ) otherlv_2= '(' ( ( (lv_params_3_0= ruleExpr ) ) (otherlv_4= ',' ( (lv_params_5_0= ruleExpr ) ) )* )? otherlv_6= ')' ) ;
    public final EObject ruleExprFunctionCall() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        EObject lv_params_3_0 = null;

        EObject lv_params_5_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3232:28: ( ( () ( (otherlv_1= RULE_ID ) ) otherlv_2= '(' ( ( (lv_params_3_0= ruleExpr ) ) (otherlv_4= ',' ( (lv_params_5_0= ruleExpr ) ) )* )? otherlv_6= ')' ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3233:1: ( () ( (otherlv_1= RULE_ID ) ) otherlv_2= '(' ( ( (lv_params_3_0= ruleExpr ) ) (otherlv_4= ',' ( (lv_params_5_0= ruleExpr ) ) )* )? otherlv_6= ')' )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3233:1: ( () ( (otherlv_1= RULE_ID ) ) otherlv_2= '(' ( ( (lv_params_3_0= ruleExpr ) ) (otherlv_4= ',' ( (lv_params_5_0= ruleExpr ) ) )* )? otherlv_6= ')' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3233:2: () ( (otherlv_1= RULE_ID ) ) otherlv_2= '(' ( ( (lv_params_3_0= ruleExpr ) ) (otherlv_4= ',' ( (lv_params_5_0= ruleExpr ) ) )* )? otherlv_6= ')'
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3233:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3234:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getExprFunctionCallAccess().getExprFunctioncallAction_0(),
                        current);
                

            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3239:2: ( (otherlv_1= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3240:1: (otherlv_1= RULE_ID )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3240:1: (otherlv_1= RULE_ID )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3241:3: otherlv_1= RULE_ID
            {

            			if (current==null) {
            	            current = createModelElement(grammarAccess.getExprFunctionCallRule());
            	        }
                    
            otherlv_1=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleExprFunctionCall7270); 

            		newLeafNode(otherlv_1, grammarAccess.getExprFunctionCallAccess().getNameValFuncDefCrossReference_1_0()); 
            	

            }


            }

            otherlv_2=(Token)match(input,20,FOLLOW_20_in_ruleExprFunctionCall7282); 

                	newLeafNode(otherlv_2, grammarAccess.getExprFunctionCallAccess().getLeftParenthesisKeyword_2());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3256:1: ( ( (lv_params_3_0= ruleExpr ) ) (otherlv_4= ',' ( (lv_params_5_0= ruleExpr ) ) )* )?
            int alt57=2;
            int LA57_0 = input.LA(1);

            if ( ((LA57_0>=RULE_ID && LA57_0<=RULE_STRING)||LA57_0==20||LA57_0==32||(LA57_0>=47 && LA57_0<=48)||(LA57_0>=53 && LA57_0<=55)) ) {
                alt57=1;
            }
            switch (alt57) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3256:2: ( (lv_params_3_0= ruleExpr ) ) (otherlv_4= ',' ( (lv_params_5_0= ruleExpr ) ) )*
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3256:2: ( (lv_params_3_0= ruleExpr ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3257:1: (lv_params_3_0= ruleExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3257:1: (lv_params_3_0= ruleExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3258:3: lv_params_3_0= ruleExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getExprFunctionCallAccess().getParamsExprParserRuleCall_3_0_0()); 
                    	    
                    pushFollow(FOLLOW_ruleExpr_in_ruleExprFunctionCall7304);
                    lv_params_3_0=ruleExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getExprFunctionCallRule());
                    	        }
                           		add(
                           			current, 
                           			"params",
                            		lv_params_3_0, 
                            		"Expr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3274:2: (otherlv_4= ',' ( (lv_params_5_0= ruleExpr ) ) )*
                    loop56:
                    do {
                        int alt56=2;
                        int LA56_0 = input.LA(1);

                        if ( (LA56_0==21) ) {
                            alt56=1;
                        }


                        switch (alt56) {
                    	case 1 :
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3274:4: otherlv_4= ',' ( (lv_params_5_0= ruleExpr ) )
                    	    {
                    	    otherlv_4=(Token)match(input,21,FOLLOW_21_in_ruleExprFunctionCall7317); 

                    	        	newLeafNode(otherlv_4, grammarAccess.getExprFunctionCallAccess().getCommaKeyword_3_1_0());
                    	        
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3278:1: ( (lv_params_5_0= ruleExpr ) )
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3279:1: (lv_params_5_0= ruleExpr )
                    	    {
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3279:1: (lv_params_5_0= ruleExpr )
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3280:3: lv_params_5_0= ruleExpr
                    	    {
                    	     
                    	    	        newCompositeNode(grammarAccess.getExprFunctionCallAccess().getParamsExprParserRuleCall_3_1_1_0()); 
                    	    	    
                    	    pushFollow(FOLLOW_ruleExpr_in_ruleExprFunctionCall7338);
                    	    lv_params_5_0=ruleExpr();

                    	    state._fsp--;


                    	    	        if (current==null) {
                    	    	            current = createModelElementForParent(grammarAccess.getExprFunctionCallRule());
                    	    	        }
                    	           		add(
                    	           			current, 
                    	           			"params",
                    	            		lv_params_5_0, 
                    	            		"Expr");
                    	    	        afterParserOrEnumRuleCall();
                    	    	    

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop56;
                        }
                    } while (true);


                    }
                    break;

            }

            otherlv_6=(Token)match(input,22,FOLLOW_22_in_ruleExprFunctionCall7354); 

                	newLeafNode(otherlv_6, grammarAccess.getExprFunctionCallAccess().getRightParenthesisKeyword_4());
                

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExprFunctionCall"

    // Delegated rules


    protected DFA5 dfa5 = new DFA5(this);
    protected DFA53 dfa53 = new DFA53(this);
    static final String DFA5_eotS =
        "\4\uffff";
    static final String DFA5_eofS =
        "\4\uffff";
    static final String DFA5_minS =
        "\2\4\2\uffff";
    static final String DFA5_maxS =
        "\2\40\2\uffff";
    static final String DFA5_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA5_specialS =
        "\4\uffff}>";
    static final String[] DFA5_transitionS = {
            "\1\1\1\2\10\uffff\1\2\1\3\2\uffff\2\2\4\uffff\1\2\1\uffff\2"+
            "\2\4\uffff\1\2",
            "\1\1\1\2\10\uffff\1\2\1\3\2\uffff\2\2\4\uffff\1\2\1\uffff"+
            "\2\2\4\uffff\1\2",
            "",
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
            return "()* loopback of 181:1: ( (this_NL_5= RULE_NL )* ( (lv_imports_6_0= ruleImport ) ) )*";
        }
    }
    static final String DFA53_eotS =
        "\12\uffff";
    static final String DFA53_eofS =
        "\3\uffff\1\10\6\uffff";
    static final String DFA53_minS =
        "\1\5\2\uffff\1\4\3\uffff\1\5\2\uffff";
    static final String DFA53_maxS =
        "\1\67\2\uffff\1\64\3\uffff\1\6\2\uffff";
    static final String DFA53_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\5\1\6\1\7\1\uffff\1\3\1\4";
    static final String DFA53_specialS =
        "\12\uffff}>";
    static final String[] DFA53_transitionS = {
            "\1\1\1\3\1\4\14\uffff\1\2\13\uffff\1\6\25\uffff\2\5",
            "",
            "",
            "\1\10\10\uffff\1\10\2\uffff\1\7\1\10\3\uffff\2\10\5\uffff"+
            "\1\10\2\uffff\1\10\5\uffff\20\10",
            "",
            "",
            "",
            "\1\10\1\11",
            "",
            ""
    };

    static final short[] DFA53_eot = DFA.unpackEncodedString(DFA53_eotS);
    static final short[] DFA53_eof = DFA.unpackEncodedString(DFA53_eofS);
    static final char[] DFA53_min = DFA.unpackEncodedStringToUnsignedChars(DFA53_minS);
    static final char[] DFA53_max = DFA.unpackEncodedStringToUnsignedChars(DFA53_maxS);
    static final short[] DFA53_accept = DFA.unpackEncodedString(DFA53_acceptS);
    static final short[] DFA53_special = DFA.unpackEncodedString(DFA53_specialS);
    static final short[][] DFA53_transition;

    static {
        int numStates = DFA53_transitionS.length;
        DFA53_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA53_transition[i] = DFA.unpackEncodedString(DFA53_transitionS[i]);
        }
    }

    class DFA53 extends DFA {

        public DFA53(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 53;
            this.eot = DFA53_eot;
            this.eof = DFA53_eof;
            this.min = DFA53_min;
            this.max = DFA53_max;
            this.accept = DFA53_accept;
            this.special = DFA53_special;
            this.transition = DFA53_transition;
        }
        public String getDescription() {
            return "2932:1: (this_ExprAtomic_0= ruleExprAtomic | (otherlv_1= '(' this_Expr_2= ruleExpr otherlv_3= ')' ) | ( () ( (lv_intVal_5_0= RULE_INT ) ) ) | ( () ( (lv_numVal_7_0= ruleNumber ) ) ) | ( () ( (lv_strVal_9_0= RULE_STRING ) ) ) | ( () ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) ) ) | ( () otherlv_13= 'function' ( (otherlv_14= RULE_ID ) ) ) )";
        }
    }
 

    public static final BitSet FOLLOW_ruleProgram_in_entryRuleProgram75 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleProgram85 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleProgram122 = new BitSet(new long[]{0x0000000000001010L});
    public static final BitSet FOLLOW_rulePackageDeclaration_in_ruleProgram144 = new BitSet(new long[]{0x0000000000001012L});
    public static final BitSet FOLLOW_rulePackageDeclaration_in_ruleProgram165 = new BitSet(new long[]{0x0000000000001012L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleProgram178 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_rulePackageDeclaration_in_entryRulePackageDeclaration215 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRulePackageDeclaration225 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_12_in_rulePackageDeclaration271 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_rulePackageDeclaration288 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_rulePackageDeclaration305 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_rulePackageDeclaration316 = new BitSet(new long[]{0x000000010D0CC030L});
    public static final BitSet FOLLOW_RULE_NL_in_rulePackageDeclaration328 = new BitSet(new long[]{0x0000000000008010L});
    public static final BitSet FOLLOW_ruleImport_in_rulePackageDeclaration350 = new BitSet(new long[]{0x000000010D0CC030L});
    public static final BitSet FOLLOW_RULE_NL_in_rulePackageDeclaration364 = new BitSet(new long[]{0x000000010D0C4030L});
    public static final BitSet FOLLOW_ruleEntity_in_rulePackageDeclaration387 = new BitSet(new long[]{0x000000010D0C4030L});
    public static final BitSet FOLLOW_RULE_NL_in_rulePackageDeclaration399 = new BitSet(new long[]{0x000000010D0C4030L});
    public static final BitSet FOLLOW_14_in_rulePackageDeclaration414 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_rulePackageDeclaration425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleImport_in_entryRuleImport460 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleImport470 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_15_in_ruleImport507 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ruleImportString_in_ruleImport528 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleImport539 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleImportString_in_entryRuleImportString575 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleImportString586 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleImportString626 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_16_in_ruleImportString644 = new BitSet(new long[]{0x0000000000020020L});
    public static final BitSet FOLLOW_17_in_ruleImportString658 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleImportString679 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleEntity_in_entryRuleEntity725 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleEntity735 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleTypeDef_in_ruleEntity782 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFuncDef_in_ruleEntity809 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleVarDef_in_ruleEntity836 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleInitBlock_in_ruleEntity863 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNativeFunc_in_ruleEntity890 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleInitBlock_in_entryRuleInitBlock925 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleInitBlock935 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_18_in_ruleInitBlock978 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_ruleInitBlock1003 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleInitBlock1014 = new BitSet(new long[]{0x00E18017081040F0L});
    public static final BitSet FOLLOW_ruleStatements_in_ruleInitBlock1034 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_ruleInitBlock1046 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleInitBlock1057 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleTypeDef_in_entryRuleTypeDef1092 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleTypeDef1102 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNativeType_in_ruleTypeDef1149 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleClassDef_in_ruleTypeDef1176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNativeFunc_in_entryRuleNativeFunc1211 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleNativeFunc1221 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_19_in_ruleNativeFunc1267 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleNativeFunc1284 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_20_in_ruleNativeFunc1301 = new BitSet(new long[]{0x0000000008400020L});
    public static final BitSet FOLLOW_ruleParameterDef_in_ruleNativeFunc1323 = new BitSet(new long[]{0x0000000000600000L});
    public static final BitSet FOLLOW_21_in_ruleNativeFunc1336 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_ruleParameterDef_in_ruleNativeFunc1357 = new BitSet(new long[]{0x0000000000600000L});
    public static final BitSet FOLLOW_22_in_ruleNativeFunc1373 = new BitSet(new long[]{0x0000000000800010L});
    public static final BitSet FOLLOW_23_in_ruleNativeFunc1386 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleNativeFunc1407 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleNativeFunc1420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNativeType_in_entryRuleNativeType1455 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleNativeType1465 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_ruleNativeType1511 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleNativeType1528 = new BitSet(new long[]{0x0000000002000010L});
    public static final BitSet FOLLOW_25_in_ruleNativeType1546 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleNativeType1567 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleNativeType1580 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleClassDef_in_entryRuleClassDef1615 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleClassDef1625 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_ruleClassDef1671 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleClassDef1688 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_ruleClassDef1705 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleClassDef1716 = new BitSet(new long[]{0x0000000108004030L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleClassDef1727 = new BitSet(new long[]{0x0000000108004030L});
    public static final BitSet FOLLOW_ruleClassMember_in_ruleClassDef1753 = new BitSet(new long[]{0x0000000108004030L});
    public static final BitSet FOLLOW_14_in_ruleClassDef1767 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleClassDef1778 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleClassMember_in_entryRuleClassMember1813 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleClassMember1823 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleVarDef_in_ruleClassMember1870 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFuncDef_in_ruleClassMember1897 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleVarDef_in_entryRuleVarDef1932 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleVarDef1942 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_ruleVarDef1996 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleVarDef2030 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleVarDef2048 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_ruleVarDef2065 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleVarDef2086 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleVarDef2115 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleVarDef2132 = new BitSet(new long[]{0x0000000010000010L});
    public static final BitSet FOLLOW_28_in_ruleVarDef2150 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleVarDef2171 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleVarDef2186 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_entryRuleTypeExpr2221 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleTypeExpr2231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleTypeExpr2276 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_29_in_ruleTypeExpr2295 = new BitSet(new long[]{0x0000000040000002L});
    public static final BitSet FOLLOW_30_in_ruleTypeExpr2321 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_RULE_INT_in_ruleTypeExpr2338 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_31_in_ruleTypeExpr2355 = new BitSet(new long[]{0x0000000040000002L});
    public static final BitSet FOLLOW_ruleFuncDef_in_entryRuleFuncDef2395 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleFuncDef2405 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_ruleFuncDef2442 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleFuncDef2459 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_20_in_ruleFuncDef2476 = new BitSet(new long[]{0x0000000008400020L});
    public static final BitSet FOLLOW_ruleParameterDef_in_ruleFuncDef2498 = new BitSet(new long[]{0x0000000000600000L});
    public static final BitSet FOLLOW_21_in_ruleFuncDef2511 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_ruleParameterDef_in_ruleFuncDef2532 = new BitSet(new long[]{0x0000000000600000L});
    public static final BitSet FOLLOW_22_in_ruleFuncDef2548 = new BitSet(new long[]{0x0000000000802000L});
    public static final BitSet FOLLOW_23_in_ruleFuncDef2561 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleFuncDef2582 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_ruleFuncDef2596 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleFuncDef2607 = new BitSet(new long[]{0x00E18017081040F0L});
    public static final BitSet FOLLOW_ruleStatements_in_ruleFuncDef2627 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_ruleFuncDef2639 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleParameterDef_in_entryRuleParameterDef2675 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleParameterDef2685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleParameterDef2740 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleParameterDef2757 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStatements_in_entryRuleStatements2798 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStatements2808 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStatements2854 = new BitSet(new long[]{0x00E18017081000F2L});
    public static final BitSet FOLLOW_ruleStatement_in_ruleStatements2880 = new BitSet(new long[]{0x00E18017081000F2L});
    public static final BitSet FOLLOW_ruleStatement_in_entryRuleStatement2918 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStatement2928 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtIf_in_ruleStatement2975 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtWhile_in_ruleStatement3002 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleLocalVarDef_in_ruleStatement3029 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtSetOrCallOrVarDef_in_ruleStatement3056 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtReturn_in_ruleStatement3083 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtReturn_in_entryRuleStmtReturn3118 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStmtReturn3128 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_33_in_ruleStmtReturn3174 = new BitSet(new long[]{0x00E18001001000F0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleStmtReturn3195 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtReturn3207 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtIf_in_entryRuleStmtIf3242 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStmtIf3252 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_34_in_ruleStmtIf3289 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleStmtIf3310 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_ruleStmtIf3322 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtIf3333 = new BitSet(new long[]{0x00E18017081040F0L});
    public static final BitSet FOLLOW_ruleStatements_in_ruleStmtIf3353 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_ruleStmtIf3365 = new BitSet(new long[]{0x0000000800000010L});
    public static final BitSet FOLLOW_35_in_ruleStmtIf3378 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_ruleStmtIf3390 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtIf3401 = new BitSet(new long[]{0x00E18017081040F0L});
    public static final BitSet FOLLOW_ruleStatements_in_ruleStmtIf3421 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_ruleStmtIf3433 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtIf3446 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtWhile_in_entryRuleStmtWhile3481 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStmtWhile3491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_36_in_ruleStmtWhile3528 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleStmtWhile3549 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_ruleStmtWhile3561 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtWhile3572 = new BitSet(new long[]{0x00E18017081040F0L});
    public static final BitSet FOLLOW_ruleStatements_in_ruleStmtWhile3592 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_ruleStmtWhile3604 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtWhile3615 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleLocalVarDef_in_entryRuleLocalVarDef3650 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleLocalVarDef3660 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_ruleLocalVarDef3713 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleLocalVarDef3747 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleLocalVarDef3765 = new BitSet(new long[]{0x0000000010000010L});
    public static final BitSet FOLLOW_28_in_ruleLocalVarDef3783 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleLocalVarDef3804 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleLocalVarDef3817 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleLocalVarDef3854 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleLocalVarDef3871 = new BitSet(new long[]{0x0000000010000010L});
    public static final BitSet FOLLOW_28_in_ruleLocalVarDef3889 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleLocalVarDef3910 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleLocalVarDef3923 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtSetOrCallOrVarDef_in_entryRuleStmtSetOrCallOrVarDef3959 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStmtSetOrCallOrVarDef3969 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleStmtSetOrCallOrVarDef4024 = new BitSet(new long[]{0x0000006010000010L});
    public static final BitSet FOLLOW_ruleOpAssignment_in_ruleStmtSetOrCallOrVarDef4055 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleStmtSetOrCallOrVarDef4076 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtSetOrCallOrVarDef4089 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleOpAssignment_in_entryRuleOpAssignment4124 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleOpAssignment4134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_ruleOpAssignment4181 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_37_in_ruleOpAssignment4210 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_38_in_ruleOpAssignment4239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExpr_in_entryRuleExpr4276 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExpr4286 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprOr_in_ruleExpr4332 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprOr_in_entryRuleExprOr4366 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprOr4376 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprAnd_in_ruleExprOr4423 = new BitSet(new long[]{0x0000008000000002L});
    public static final BitSet FOLLOW_39_in_ruleExprOr4450 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExprAnd_in_ruleExprOr4484 = new BitSet(new long[]{0x0000008000000002L});
    public static final BitSet FOLLOW_ruleExprAnd_in_entryRuleExprAnd4522 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprAnd4532 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprEquality_in_ruleExprAnd4579 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_40_in_ruleExprAnd4606 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExprEquality_in_ruleExprAnd4640 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_ruleExprEquality_in_entryRuleExprEquality4678 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprEquality4688 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprComparison_in_ruleExprEquality4735 = new BitSet(new long[]{0x0000060000000002L});
    public static final BitSet FOLLOW_ruleOpEquality_in_ruleExprEquality4765 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExprComparison_in_ruleExprEquality4786 = new BitSet(new long[]{0x0000060000000002L});
    public static final BitSet FOLLOW_ruleOpEquality_in_entryRuleOpEquality4824 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleOpEquality4834 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_41_in_ruleOpEquality4881 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_ruleOpEquality4910 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprComparison_in_entryRuleExprComparison4947 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprComparison4957 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprAdditive_in_ruleExprComparison5004 = new BitSet(new long[]{0x0000780000000002L});
    public static final BitSet FOLLOW_ruleOpComparison_in_ruleExprComparison5034 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExprAdditive_in_ruleExprComparison5055 = new BitSet(new long[]{0x0000780000000002L});
    public static final BitSet FOLLOW_ruleOpComparison_in_entryRuleOpComparison5093 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleOpComparison5103 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_43_in_ruleOpComparison5150 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_ruleOpComparison5179 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_45_in_ruleOpComparison5208 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_46_in_ruleOpComparison5237 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprAdditive_in_entryRuleExprAdditive5274 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprAdditive5284 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprMult_in_ruleExprAdditive5331 = new BitSet(new long[]{0x0001800000000002L});
    public static final BitSet FOLLOW_ruleOpAdditive_in_ruleExprAdditive5361 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExprMult_in_ruleExprAdditive5382 = new BitSet(new long[]{0x0001800000000002L});
    public static final BitSet FOLLOW_ruleOpAdditive_in_entryRuleOpAdditive5420 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleOpAdditive5430 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_47_in_ruleOpAdditive5477 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_48_in_ruleOpAdditive5506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprMult_in_entryRuleExprMult5543 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprMult5553 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprSign_in_ruleExprMult5600 = new BitSet(new long[]{0x001E000000020002L});
    public static final BitSet FOLLOW_ruleOpMultiplicative_in_ruleExprMult5630 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExprSign_in_ruleExprMult5651 = new BitSet(new long[]{0x001E000000020002L});
    public static final BitSet FOLLOW_ruleOpMultiplicative_in_entryRuleOpMultiplicative5689 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleOpMultiplicative5699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_17_in_ruleOpMultiplicative5746 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_49_in_ruleOpMultiplicative5775 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_50_in_ruleOpMultiplicative5804 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_51_in_ruleOpMultiplicative5833 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_52_in_ruleOpMultiplicative5862 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprSign_in_entryRuleExprSign5899 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprSign5909 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleOpAdditive_in_ruleExprSign5965 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExprNot_in_ruleExprSign5986 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprNot_in_ruleExprSign6015 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprNot_in_entryRuleExprNot6050 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprNot6060 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_53_in_ruleExprNot6107 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExprMember_in_ruleExprNot6128 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprMember_in_ruleExprNot6157 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprMember_in_entryRuleExprMember6192 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprMember6202 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprSingle_in_ruleExprMember6249 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_16_in_ruleExprMember6270 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ruleExprMemberRight_in_ruleExprMember6291 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_ruleExprMemberRight_in_entryRuleExprMemberRight6329 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprMemberRight6339 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleExprMemberRight6384 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_20_in_ruleExprMemberRight6397 = new BitSet(new long[]{0x00E18001005000E0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleExprMemberRight6419 = new BitSet(new long[]{0x0000000000600000L});
    public static final BitSet FOLLOW_21_in_ruleExprMemberRight6432 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleExprMemberRight6453 = new BitSet(new long[]{0x0000000000600000L});
    public static final BitSet FOLLOW_22_in_ruleExprMemberRight6469 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprSingle_in_entryRuleExprSingle6507 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprSingle6517 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprAtomic_in_ruleExprSingle6564 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_20_in_ruleExprSingle6582 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleExprSingle6604 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_ruleExprSingle6615 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_INT_in_ruleExprSingle6649 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNumber_in_ruleExprSingle6692 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_STRING_in_ruleExprSingle6726 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_54_in_ruleExprSingle6768 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_55_in_ruleExprSingle6797 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_ruleExprSingle6842 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleExprSingle6862 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNumber_in_entryRuleNumber6900 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleNumber6911 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_INT_in_ruleNumber6951 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_16_in_ruleNumber6969 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_RULE_INT_in_ruleNumber6984 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprAtomic_in_entryRuleExprAtomic7029 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprAtomic7039 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprFunctionCall_in_ruleExprAtomic7086 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleExprAtomic7121 = new BitSet(new long[]{0x0000000040000002L});
    public static final BitSet FOLLOW_30_in_ruleExprAtomic7134 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleExprAtomic7155 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_31_in_ruleExprAtomic7167 = new BitSet(new long[]{0x0000000040000002L});
    public static final BitSet FOLLOW_ruleExprFunctionCall_in_entryRuleExprFunctionCall7206 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprFunctionCall7216 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleExprFunctionCall7270 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_20_in_ruleExprFunctionCall7282 = new BitSet(new long[]{0x00E18001005000E0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleExprFunctionCall7304 = new BitSet(new long[]{0x0000000000600000L});
    public static final BitSet FOLLOW_21_in_ruleExprFunctionCall7317 = new BitSet(new long[]{0x00E18001001000E0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleExprFunctionCall7338 = new BitSet(new long[]{0x0000000000600000L});
    public static final BitSet FOLLOW_22_in_ruleExprFunctionCall7354 = new BitSet(new long[]{0x0000000000000002L});

}