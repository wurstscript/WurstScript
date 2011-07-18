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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_NL", "RULE_ID", "RULE_INT", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'package'", "'endpackage'", "'import'", "'.'", "'*'", "'init'", "'endinit'", "'constant'", "'native'", "'('", "','", "')'", "'takes'", "'nothing'", "'returns'", "'type'", "'extends'", "'class'", "'endclass'", "'val'", "'='", "'glob'", "'integer'", "'real'", "'string'", "'boolean'", "'handle'", "'code'", "'function'", "'endfunction'", "'local'", "'exitwhen'", "'loop'", "'endloop'", "'return'", "'if'", "'then'", "'elseif'", "'else'", "'endif'", "'while'", "'endwhile'", "'call'", "'set'", "'+='", "'-='", "'or'", "'and'", "'=='", "'!='", "'<='", "'<'", "'>='", "'>'", "'+'", "'-'", "'/'", "'%'", "'mod'", "'div'", "'not'", "'true'", "'false'"
    };
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int RULE_ID=5;
    public static final int T__66=66;
    public static final int T__67=67;
    public static final int T__64=64;
    public static final int T__29=29;
    public static final int T__65=65;
    public static final int T__28=28;
    public static final int T__62=62;
    public static final int T__27=27;
    public static final int T__63=63;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int RULE_ANY_OTHER=11;
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int T__61=61;
    public static final int T__60=60;
    public static final int EOF=-1;
    public static final int T__55=55;
    public static final int T__56=56;
    public static final int T__19=19;
    public static final int T__57=57;
    public static final int T__58=58;
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
    public static final int T__59=59;
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
    public static final int T__71=71;
    public static final int T__33=33;
    public static final int T__72=72;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__70=70;
    public static final int T__36=36;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int RULE_WS=10;
    public static final int T__74=74;
    public static final int T__73=73;

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:141:1: rulePackageDeclaration returns [EObject current=null] : ( () otherlv_1= 'package' ( (lv_name_2_0= RULE_ID ) ) ( (this_NL_3= RULE_NL )* ( (lv_imports_4_0= ruleImport ) ) )* (this_NL_5= RULE_NL )* ( ( (lv_elements_6_0= ruleEntity ) ) (this_NL_7= RULE_NL )* )* otherlv_8= 'endpackage' (this_NL_9= RULE_NL )* ) ;
    public final EObject rulePackageDeclaration() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token this_NL_3=null;
        Token this_NL_5=null;
        Token this_NL_7=null;
        Token otherlv_8=null;
        Token this_NL_9=null;
        EObject lv_imports_4_0 = null;

        EObject lv_elements_6_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:144:28: ( ( () otherlv_1= 'package' ( (lv_name_2_0= RULE_ID ) ) ( (this_NL_3= RULE_NL )* ( (lv_imports_4_0= ruleImport ) ) )* (this_NL_5= RULE_NL )* ( ( (lv_elements_6_0= ruleEntity ) ) (this_NL_7= RULE_NL )* )* otherlv_8= 'endpackage' (this_NL_9= RULE_NL )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:145:1: ( () otherlv_1= 'package' ( (lv_name_2_0= RULE_ID ) ) ( (this_NL_3= RULE_NL )* ( (lv_imports_4_0= ruleImport ) ) )* (this_NL_5= RULE_NL )* ( ( (lv_elements_6_0= ruleEntity ) ) (this_NL_7= RULE_NL )* )* otherlv_8= 'endpackage' (this_NL_9= RULE_NL )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:145:1: ( () otherlv_1= 'package' ( (lv_name_2_0= RULE_ID ) ) ( (this_NL_3= RULE_NL )* ( (lv_imports_4_0= ruleImport ) ) )* (this_NL_5= RULE_NL )* ( ( (lv_elements_6_0= ruleEntity ) ) (this_NL_7= RULE_NL )* )* otherlv_8= 'endpackage' (this_NL_9= RULE_NL )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:145:2: () otherlv_1= 'package' ( (lv_name_2_0= RULE_ID ) ) ( (this_NL_3= RULE_NL )* ( (lv_imports_4_0= ruleImport ) ) )* (this_NL_5= RULE_NL )* ( ( (lv_elements_6_0= ruleEntity ) ) (this_NL_7= RULE_NL )* )* otherlv_8= 'endpackage' (this_NL_9= RULE_NL )*
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

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:173:2: ( (this_NL_3= RULE_NL )* ( (lv_imports_4_0= ruleImport ) ) )*
            loop5:
            do {
                int alt5=2;
                alt5 = dfa5.predict(input);
                switch (alt5) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:173:3: (this_NL_3= RULE_NL )* ( (lv_imports_4_0= ruleImport ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:173:3: (this_NL_3= RULE_NL )*
            	    loop4:
            	    do {
            	        int alt4=2;
            	        int LA4_0 = input.LA(1);

            	        if ( (LA4_0==RULE_NL) ) {
            	            alt4=1;
            	        }


            	        switch (alt4) {
            	    	case 1 :
            	    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:173:4: this_NL_3= RULE_NL
            	    	    {
            	    	    this_NL_3=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_rulePackageDeclaration306); 
            	    	     
            	    	        newLeafNode(this_NL_3, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_3_0()); 
            	    	        

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop4;
            	        }
            	    } while (true);

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:177:3: ( (lv_imports_4_0= ruleImport ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:178:1: (lv_imports_4_0= ruleImport )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:178:1: (lv_imports_4_0= ruleImport )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:179:3: lv_imports_4_0= ruleImport
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getPackageDeclarationAccess().getImportsImportParserRuleCall_3_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleImport_in_rulePackageDeclaration328);
            	    lv_imports_4_0=ruleImport();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getPackageDeclarationRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"imports",
            	            		lv_imports_4_0, 
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

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:195:4: (this_NL_5= RULE_NL )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==RULE_NL) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:195:5: this_NL_5= RULE_NL
            	    {
            	    this_NL_5=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_rulePackageDeclaration342); 
            	     
            	        newLeafNode(this_NL_5, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_4()); 
            	        

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:199:3: ( ( (lv_elements_6_0= ruleEntity ) ) (this_NL_7= RULE_NL )* )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==RULE_ID||LA8_0==17||(LA8_0>=19 && LA8_0<=20)||LA8_0==27||LA8_0==29||LA8_0==31||(LA8_0>=33 && LA8_0<=40)) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:199:4: ( (lv_elements_6_0= ruleEntity ) ) (this_NL_7= RULE_NL )*
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:199:4: ( (lv_elements_6_0= ruleEntity ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:200:1: (lv_elements_6_0= ruleEntity )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:200:1: (lv_elements_6_0= ruleEntity )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:201:3: lv_elements_6_0= ruleEntity
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getPackageDeclarationAccess().getElementsEntityParserRuleCall_5_0_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleEntity_in_rulePackageDeclaration365);
            	    lv_elements_6_0=ruleEntity();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getPackageDeclarationRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"elements",
            	            		lv_elements_6_0, 
            	            		"Entity");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:217:2: (this_NL_7= RULE_NL )*
            	    loop7:
            	    do {
            	        int alt7=2;
            	        int LA7_0 = input.LA(1);

            	        if ( (LA7_0==RULE_NL) ) {
            	            alt7=1;
            	        }


            	        switch (alt7) {
            	    	case 1 :
            	    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:217:3: this_NL_7= RULE_NL
            	    	    {
            	    	    this_NL_7=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_rulePackageDeclaration377); 
            	    	     
            	    	        newLeafNode(this_NL_7, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_5_1()); 
            	    	        

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

            otherlv_8=(Token)match(input,13,FOLLOW_13_in_rulePackageDeclaration392); 

                	newLeafNode(otherlv_8, grammarAccess.getPackageDeclarationAccess().getEndpackageKeyword_6());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:225:1: (this_NL_9= RULE_NL )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==RULE_NL) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:225:2: this_NL_9= RULE_NL
            	    {
            	    this_NL_9=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_rulePackageDeclaration404); 
            	     
            	        newLeafNode(this_NL_9, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_7()); 
            	        

            	    }
            	    break;

            	default :
            	    break loop9;
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
    // $ANTLR end "rulePackageDeclaration"


    // $ANTLR start "entryRuleImport"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:237:1: entryRuleImport returns [EObject current=null] : iv_ruleImport= ruleImport EOF ;
    public final EObject entryRuleImport() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleImport = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:238:2: (iv_ruleImport= ruleImport EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:239:2: iv_ruleImport= ruleImport EOF
            {
             newCompositeNode(grammarAccess.getImportRule()); 
            pushFollow(FOLLOW_ruleImport_in_entryRuleImport441);
            iv_ruleImport=ruleImport();

            state._fsp--;

             current =iv_ruleImport; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleImport451); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:246:1: ruleImport returns [EObject current=null] : (otherlv_0= 'import' ( (lv_importedNamespace_1_0= ruleImportString ) ) this_NL_2= RULE_NL ) ;
    public final EObject ruleImport() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token this_NL_2=null;
        AntlrDatatypeRuleToken lv_importedNamespace_1_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:249:28: ( (otherlv_0= 'import' ( (lv_importedNamespace_1_0= ruleImportString ) ) this_NL_2= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:250:1: (otherlv_0= 'import' ( (lv_importedNamespace_1_0= ruleImportString ) ) this_NL_2= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:250:1: (otherlv_0= 'import' ( (lv_importedNamespace_1_0= ruleImportString ) ) this_NL_2= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:250:3: otherlv_0= 'import' ( (lv_importedNamespace_1_0= ruleImportString ) ) this_NL_2= RULE_NL
            {
            otherlv_0=(Token)match(input,14,FOLLOW_14_in_ruleImport488); 

                	newLeafNode(otherlv_0, grammarAccess.getImportAccess().getImportKeyword_0());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:254:1: ( (lv_importedNamespace_1_0= ruleImportString ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:255:1: (lv_importedNamespace_1_0= ruleImportString )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:255:1: (lv_importedNamespace_1_0= ruleImportString )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:256:3: lv_importedNamespace_1_0= ruleImportString
            {
             
            	        newCompositeNode(grammarAccess.getImportAccess().getImportedNamespaceImportStringParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleImportString_in_ruleImport509);
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

            this_NL_2=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleImport520); 
             
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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:284:1: entryRuleImportString returns [String current=null] : iv_ruleImportString= ruleImportString EOF ;
    public final String entryRuleImportString() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleImportString = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:285:2: (iv_ruleImportString= ruleImportString EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:286:2: iv_ruleImportString= ruleImportString EOF
            {
             newCompositeNode(grammarAccess.getImportStringRule()); 
            pushFollow(FOLLOW_ruleImportString_in_entryRuleImportString556);
            iv_ruleImportString=ruleImportString();

            state._fsp--;

             current =iv_ruleImportString.getText(); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleImportString567); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:293:1: ruleImportString returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_ID_0= RULE_ID kw= '.' (kw= '*' | this_ID_3= RULE_ID ) ) ;
    public final AntlrDatatypeRuleToken ruleImportString() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_ID_0=null;
        Token kw=null;
        Token this_ID_3=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:296:28: ( (this_ID_0= RULE_ID kw= '.' (kw= '*' | this_ID_3= RULE_ID ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:297:1: (this_ID_0= RULE_ID kw= '.' (kw= '*' | this_ID_3= RULE_ID ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:297:1: (this_ID_0= RULE_ID kw= '.' (kw= '*' | this_ID_3= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:297:6: this_ID_0= RULE_ID kw= '.' (kw= '*' | this_ID_3= RULE_ID )
            {
            this_ID_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleImportString607); 

            		current.merge(this_ID_0);
                
             
                newLeafNode(this_ID_0, grammarAccess.getImportStringAccess().getIDTerminalRuleCall_0()); 
                
            kw=(Token)match(input,15,FOLLOW_15_in_ruleImportString625); 

                    current.merge(kw);
                    newLeafNode(kw, grammarAccess.getImportStringAccess().getFullStopKeyword_1()); 
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:310:1: (kw= '*' | this_ID_3= RULE_ID )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==16) ) {
                alt10=1;
            }
            else if ( (LA10_0==RULE_ID) ) {
                alt10=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:311:2: kw= '*'
                    {
                    kw=(Token)match(input,16,FOLLOW_16_in_ruleImportString639); 

                            current.merge(kw);
                            newLeafNode(kw, grammarAccess.getImportStringAccess().getAsteriskKeyword_2_0()); 
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:317:10: this_ID_3= RULE_ID
                    {
                    this_ID_3=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleImportString660); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:332:1: entryRuleEntity returns [EObject current=null] : iv_ruleEntity= ruleEntity EOF ;
    public final EObject entryRuleEntity() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEntity = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:333:2: (iv_ruleEntity= ruleEntity EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:334:2: iv_ruleEntity= ruleEntity EOF
            {
             newCompositeNode(grammarAccess.getEntityRule()); 
            pushFollow(FOLLOW_ruleEntity_in_entryRuleEntity706);
            iv_ruleEntity=ruleEntity();

            state._fsp--;

             current =iv_ruleEntity; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleEntity716); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:341:1: ruleEntity returns [EObject current=null] : (this_TypeDef_0= ruleTypeDef | this_FuncDef_1= ruleFuncDef | this_VarDef_2= ruleVarDef | this_InitBlock_3= ruleInitBlock | this_NativeFunc_4= ruleNativeFunc ) ;
    public final EObject ruleEntity() throws RecognitionException {
        EObject current = null;

        EObject this_TypeDef_0 = null;

        EObject this_FuncDef_1 = null;

        EObject this_VarDef_2 = null;

        EObject this_InitBlock_3 = null;

        EObject this_NativeFunc_4 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:344:28: ( (this_TypeDef_0= ruleTypeDef | this_FuncDef_1= ruleFuncDef | this_VarDef_2= ruleVarDef | this_InitBlock_3= ruleInitBlock | this_NativeFunc_4= ruleNativeFunc ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:345:1: (this_TypeDef_0= ruleTypeDef | this_FuncDef_1= ruleFuncDef | this_VarDef_2= ruleVarDef | this_InitBlock_3= ruleInitBlock | this_NativeFunc_4= ruleNativeFunc )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:345:1: (this_TypeDef_0= ruleTypeDef | this_FuncDef_1= ruleFuncDef | this_VarDef_2= ruleVarDef | this_InitBlock_3= ruleInitBlock | this_NativeFunc_4= ruleNativeFunc )
            int alt11=5;
            switch ( input.LA(1) ) {
            case 27:
            case 29:
                {
                alt11=1;
                }
                break;
            case 40:
                {
                alt11=2;
                }
                break;
            case RULE_ID:
            case 31:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
                {
                alt11=3;
                }
                break;
            case 19:
                {
                int LA11_4 = input.LA(2);

                if ( (LA11_4==20) ) {
                    alt11=5;
                }
                else if ( (LA11_4==RULE_ID||(LA11_4>=34 && LA11_4<=39)) ) {
                    alt11=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 11, 4, input);

                    throw nvae;
                }
                }
                break;
            case 17:
                {
                alt11=4;
                }
                break;
            case 20:
                {
                alt11=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }

            switch (alt11) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:346:5: this_TypeDef_0= ruleTypeDef
                    {
                     
                            newCompositeNode(grammarAccess.getEntityAccess().getTypeDefParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleTypeDef_in_ruleEntity763);
                    this_TypeDef_0=ruleTypeDef();

                    state._fsp--;

                     
                            current = this_TypeDef_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:356:5: this_FuncDef_1= ruleFuncDef
                    {
                     
                            newCompositeNode(grammarAccess.getEntityAccess().getFuncDefParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleFuncDef_in_ruleEntity790);
                    this_FuncDef_1=ruleFuncDef();

                    state._fsp--;

                     
                            current = this_FuncDef_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:366:5: this_VarDef_2= ruleVarDef
                    {
                     
                            newCompositeNode(grammarAccess.getEntityAccess().getVarDefParserRuleCall_2()); 
                        
                    pushFollow(FOLLOW_ruleVarDef_in_ruleEntity817);
                    this_VarDef_2=ruleVarDef();

                    state._fsp--;

                     
                            current = this_VarDef_2; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 4 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:376:5: this_InitBlock_3= ruleInitBlock
                    {
                     
                            newCompositeNode(grammarAccess.getEntityAccess().getInitBlockParserRuleCall_3()); 
                        
                    pushFollow(FOLLOW_ruleInitBlock_in_ruleEntity844);
                    this_InitBlock_3=ruleInitBlock();

                    state._fsp--;

                     
                            current = this_InitBlock_3; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 5 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:386:5: this_NativeFunc_4= ruleNativeFunc
                    {
                     
                            newCompositeNode(grammarAccess.getEntityAccess().getNativeFuncParserRuleCall_4()); 
                        
                    pushFollow(FOLLOW_ruleNativeFunc_in_ruleEntity871);
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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:402:1: entryRuleInitBlock returns [EObject current=null] : iv_ruleInitBlock= ruleInitBlock EOF ;
    public final EObject entryRuleInitBlock() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleInitBlock = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:403:2: (iv_ruleInitBlock= ruleInitBlock EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:404:2: iv_ruleInitBlock= ruleInitBlock EOF
            {
             newCompositeNode(grammarAccess.getInitBlockRule()); 
            pushFollow(FOLLOW_ruleInitBlock_in_entryRuleInitBlock906);
            iv_ruleInitBlock=ruleInitBlock();

            state._fsp--;

             current =iv_ruleInitBlock; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleInitBlock916); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:411:1: ruleInitBlock returns [EObject current=null] : ( ( (lv_name_0_0= 'init' ) ) ( (lv_body_1_0= ruleStatements ) ) otherlv_2= 'endinit' ) ;
    public final EObject ruleInitBlock() throws RecognitionException {
        EObject current = null;

        Token lv_name_0_0=null;
        Token otherlv_2=null;
        EObject lv_body_1_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:414:28: ( ( ( (lv_name_0_0= 'init' ) ) ( (lv_body_1_0= ruleStatements ) ) otherlv_2= 'endinit' ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:415:1: ( ( (lv_name_0_0= 'init' ) ) ( (lv_body_1_0= ruleStatements ) ) otherlv_2= 'endinit' )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:415:1: ( ( (lv_name_0_0= 'init' ) ) ( (lv_body_1_0= ruleStatements ) ) otherlv_2= 'endinit' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:415:2: ( (lv_name_0_0= 'init' ) ) ( (lv_body_1_0= ruleStatements ) ) otherlv_2= 'endinit'
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:415:2: ( (lv_name_0_0= 'init' ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:416:1: (lv_name_0_0= 'init' )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:416:1: (lv_name_0_0= 'init' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:417:3: lv_name_0_0= 'init'
            {
            lv_name_0_0=(Token)match(input,17,FOLLOW_17_in_ruleInitBlock959); 

                    newLeafNode(lv_name_0_0, grammarAccess.getInitBlockAccess().getNameInitKeyword_0_0());
                

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getInitBlockRule());
            	        }
                   		setWithLastConsumed(current, "name", lv_name_0_0, "init");
            	    

            }


            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:430:2: ( (lv_body_1_0= ruleStatements ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:431:1: (lv_body_1_0= ruleStatements )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:431:1: (lv_body_1_0= ruleStatements )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:432:3: lv_body_1_0= ruleStatements
            {
             
            	        newCompositeNode(grammarAccess.getInitBlockAccess().getBodyStatementsParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleStatements_in_ruleInitBlock993);
            lv_body_1_0=ruleStatements();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getInitBlockRule());
            	        }
                   		set(
                   			current, 
                   			"body",
                    		lv_body_1_0, 
                    		"Statements");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_2=(Token)match(input,18,FOLLOW_18_in_ruleInitBlock1005); 

                	newLeafNode(otherlv_2, grammarAccess.getInitBlockAccess().getEndinitKeyword_2());
                

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:460:1: entryRuleTypeDef returns [EObject current=null] : iv_ruleTypeDef= ruleTypeDef EOF ;
    public final EObject entryRuleTypeDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeDef = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:461:2: (iv_ruleTypeDef= ruleTypeDef EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:462:2: iv_ruleTypeDef= ruleTypeDef EOF
            {
             newCompositeNode(grammarAccess.getTypeDefRule()); 
            pushFollow(FOLLOW_ruleTypeDef_in_entryRuleTypeDef1041);
            iv_ruleTypeDef=ruleTypeDef();

            state._fsp--;

             current =iv_ruleTypeDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleTypeDef1051); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:469:1: ruleTypeDef returns [EObject current=null] : (this_NativeType_0= ruleNativeType | this_ClassDef_1= ruleClassDef ) ;
    public final EObject ruleTypeDef() throws RecognitionException {
        EObject current = null;

        EObject this_NativeType_0 = null;

        EObject this_ClassDef_1 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:472:28: ( (this_NativeType_0= ruleNativeType | this_ClassDef_1= ruleClassDef ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:473:1: (this_NativeType_0= ruleNativeType | this_ClassDef_1= ruleClassDef )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:473:1: (this_NativeType_0= ruleNativeType | this_ClassDef_1= ruleClassDef )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==27) ) {
                alt12=1;
            }
            else if ( (LA12_0==29) ) {
                alt12=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:474:5: this_NativeType_0= ruleNativeType
                    {
                     
                            newCompositeNode(grammarAccess.getTypeDefAccess().getNativeTypeParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleNativeType_in_ruleTypeDef1098);
                    this_NativeType_0=ruleNativeType();

                    state._fsp--;

                     
                            current = this_NativeType_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:484:5: this_ClassDef_1= ruleClassDef
                    {
                     
                            newCompositeNode(grammarAccess.getTypeDefAccess().getClassDefParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleClassDef_in_ruleTypeDef1125);
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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:500:1: entryRuleNativeFunc returns [EObject current=null] : iv_ruleNativeFunc= ruleNativeFunc EOF ;
    public final EObject entryRuleNativeFunc() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleNativeFunc = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:501:2: (iv_ruleNativeFunc= ruleNativeFunc EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:502:2: iv_ruleNativeFunc= ruleNativeFunc EOF
            {
             newCompositeNode(grammarAccess.getNativeFuncRule()); 
            pushFollow(FOLLOW_ruleNativeFunc_in_entryRuleNativeFunc1160);
            iv_ruleNativeFunc=ruleNativeFunc();

            state._fsp--;

             current =iv_ruleNativeFunc; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleNativeFunc1170); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:509:1: ruleNativeFunc returns [EObject current=null] : ( () (otherlv_1= 'constant' )? otherlv_2= 'native' ( (lv_name_3_0= RULE_ID ) ) ( (otherlv_4= '(' ( ( (lv_parameters_5_0= ruleParameterDef ) ) (otherlv_6= ',' ( (lv_parameters_7_0= ruleParameterDef ) ) )* )? otherlv_8= ')' ) | (otherlv_9= 'takes' otherlv_10= 'nothing' ) | (otherlv_11= 'takes' ( (lv_parameters_12_0= ruleParameterDef ) ) (otherlv_13= ',' ( (lv_parameters_14_0= ruleParameterDef ) ) )* ) ) ( (otherlv_15= 'returns' ( (lv_type_16_0= ruleTypeExpr ) ) ) | (otherlv_17= 'returns' otherlv_18= 'nothing' )? ) this_NL_19= RULE_NL ) ;
    public final EObject ruleNativeFunc() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token lv_name_3_0=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        Token otherlv_8=null;
        Token otherlv_9=null;
        Token otherlv_10=null;
        Token otherlv_11=null;
        Token otherlv_13=null;
        Token otherlv_15=null;
        Token otherlv_17=null;
        Token otherlv_18=null;
        Token this_NL_19=null;
        EObject lv_parameters_5_0 = null;

        EObject lv_parameters_7_0 = null;

        EObject lv_parameters_12_0 = null;

        EObject lv_parameters_14_0 = null;

        EObject lv_type_16_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:512:28: ( ( () (otherlv_1= 'constant' )? otherlv_2= 'native' ( (lv_name_3_0= RULE_ID ) ) ( (otherlv_4= '(' ( ( (lv_parameters_5_0= ruleParameterDef ) ) (otherlv_6= ',' ( (lv_parameters_7_0= ruleParameterDef ) ) )* )? otherlv_8= ')' ) | (otherlv_9= 'takes' otherlv_10= 'nothing' ) | (otherlv_11= 'takes' ( (lv_parameters_12_0= ruleParameterDef ) ) (otherlv_13= ',' ( (lv_parameters_14_0= ruleParameterDef ) ) )* ) ) ( (otherlv_15= 'returns' ( (lv_type_16_0= ruleTypeExpr ) ) ) | (otherlv_17= 'returns' otherlv_18= 'nothing' )? ) this_NL_19= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:513:1: ( () (otherlv_1= 'constant' )? otherlv_2= 'native' ( (lv_name_3_0= RULE_ID ) ) ( (otherlv_4= '(' ( ( (lv_parameters_5_0= ruleParameterDef ) ) (otherlv_6= ',' ( (lv_parameters_7_0= ruleParameterDef ) ) )* )? otherlv_8= ')' ) | (otherlv_9= 'takes' otherlv_10= 'nothing' ) | (otherlv_11= 'takes' ( (lv_parameters_12_0= ruleParameterDef ) ) (otherlv_13= ',' ( (lv_parameters_14_0= ruleParameterDef ) ) )* ) ) ( (otherlv_15= 'returns' ( (lv_type_16_0= ruleTypeExpr ) ) ) | (otherlv_17= 'returns' otherlv_18= 'nothing' )? ) this_NL_19= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:513:1: ( () (otherlv_1= 'constant' )? otherlv_2= 'native' ( (lv_name_3_0= RULE_ID ) ) ( (otherlv_4= '(' ( ( (lv_parameters_5_0= ruleParameterDef ) ) (otherlv_6= ',' ( (lv_parameters_7_0= ruleParameterDef ) ) )* )? otherlv_8= ')' ) | (otherlv_9= 'takes' otherlv_10= 'nothing' ) | (otherlv_11= 'takes' ( (lv_parameters_12_0= ruleParameterDef ) ) (otherlv_13= ',' ( (lv_parameters_14_0= ruleParameterDef ) ) )* ) ) ( (otherlv_15= 'returns' ( (lv_type_16_0= ruleTypeExpr ) ) ) | (otherlv_17= 'returns' otherlv_18= 'nothing' )? ) this_NL_19= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:513:2: () (otherlv_1= 'constant' )? otherlv_2= 'native' ( (lv_name_3_0= RULE_ID ) ) ( (otherlv_4= '(' ( ( (lv_parameters_5_0= ruleParameterDef ) ) (otherlv_6= ',' ( (lv_parameters_7_0= ruleParameterDef ) ) )* )? otherlv_8= ')' ) | (otherlv_9= 'takes' otherlv_10= 'nothing' ) | (otherlv_11= 'takes' ( (lv_parameters_12_0= ruleParameterDef ) ) (otherlv_13= ',' ( (lv_parameters_14_0= ruleParameterDef ) ) )* ) ) ( (otherlv_15= 'returns' ( (lv_type_16_0= ruleTypeExpr ) ) ) | (otherlv_17= 'returns' otherlv_18= 'nothing' )? ) this_NL_19= RULE_NL
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:513:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:514:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getNativeFuncAccess().getNativeFuncAction_0(),
                        current);
                

            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:519:2: (otherlv_1= 'constant' )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==19) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:519:4: otherlv_1= 'constant'
                    {
                    otherlv_1=(Token)match(input,19,FOLLOW_19_in_ruleNativeFunc1217); 

                        	newLeafNode(otherlv_1, grammarAccess.getNativeFuncAccess().getConstantKeyword_1());
                        

                    }
                    break;

            }

            otherlv_2=(Token)match(input,20,FOLLOW_20_in_ruleNativeFunc1231); 

                	newLeafNode(otherlv_2, grammarAccess.getNativeFuncAccess().getNativeKeyword_2());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:527:1: ( (lv_name_3_0= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:528:1: (lv_name_3_0= RULE_ID )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:528:1: (lv_name_3_0= RULE_ID )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:529:3: lv_name_3_0= RULE_ID
            {
            lv_name_3_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleNativeFunc1248); 

            			newLeafNode(lv_name_3_0, grammarAccess.getNativeFuncAccess().getNameIDTerminalRuleCall_3_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getNativeFuncRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_3_0, 
                    		"ID");
            	    

            }


            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:545:2: ( (otherlv_4= '(' ( ( (lv_parameters_5_0= ruleParameterDef ) ) (otherlv_6= ',' ( (lv_parameters_7_0= ruleParameterDef ) ) )* )? otherlv_8= ')' ) | (otherlv_9= 'takes' otherlv_10= 'nothing' ) | (otherlv_11= 'takes' ( (lv_parameters_12_0= ruleParameterDef ) ) (otherlv_13= ',' ( (lv_parameters_14_0= ruleParameterDef ) ) )* ) )
            int alt17=3;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==21) ) {
                alt17=1;
            }
            else if ( (LA17_0==24) ) {
                int LA17_2 = input.LA(2);

                if ( (LA17_2==25) ) {
                    alt17=2;
                }
                else if ( (LA17_2==RULE_ID||(LA17_2>=34 && LA17_2<=39)) ) {
                    alt17=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:545:3: (otherlv_4= '(' ( ( (lv_parameters_5_0= ruleParameterDef ) ) (otherlv_6= ',' ( (lv_parameters_7_0= ruleParameterDef ) ) )* )? otherlv_8= ')' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:545:3: (otherlv_4= '(' ( ( (lv_parameters_5_0= ruleParameterDef ) ) (otherlv_6= ',' ( (lv_parameters_7_0= ruleParameterDef ) ) )* )? otherlv_8= ')' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:545:5: otherlv_4= '(' ( ( (lv_parameters_5_0= ruleParameterDef ) ) (otherlv_6= ',' ( (lv_parameters_7_0= ruleParameterDef ) ) )* )? otherlv_8= ')'
                    {
                    otherlv_4=(Token)match(input,21,FOLLOW_21_in_ruleNativeFunc1267); 

                        	newLeafNode(otherlv_4, grammarAccess.getNativeFuncAccess().getLeftParenthesisKeyword_4_0_0());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:549:1: ( ( (lv_parameters_5_0= ruleParameterDef ) ) (otherlv_6= ',' ( (lv_parameters_7_0= ruleParameterDef ) ) )* )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0==RULE_ID||(LA15_0>=34 && LA15_0<=39)) ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:549:2: ( (lv_parameters_5_0= ruleParameterDef ) ) (otherlv_6= ',' ( (lv_parameters_7_0= ruleParameterDef ) ) )*
                            {
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:549:2: ( (lv_parameters_5_0= ruleParameterDef ) )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:550:1: (lv_parameters_5_0= ruleParameterDef )
                            {
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:550:1: (lv_parameters_5_0= ruleParameterDef )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:551:3: lv_parameters_5_0= ruleParameterDef
                            {
                             
                            	        newCompositeNode(grammarAccess.getNativeFuncAccess().getParametersParameterDefParserRuleCall_4_0_1_0_0()); 
                            	    
                            pushFollow(FOLLOW_ruleParameterDef_in_ruleNativeFunc1289);
                            lv_parameters_5_0=ruleParameterDef();

                            state._fsp--;


                            	        if (current==null) {
                            	            current = createModelElementForParent(grammarAccess.getNativeFuncRule());
                            	        }
                                   		add(
                                   			current, 
                                   			"parameters",
                                    		lv_parameters_5_0, 
                                    		"ParameterDef");
                            	        afterParserOrEnumRuleCall();
                            	    

                            }


                            }

                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:567:2: (otherlv_6= ',' ( (lv_parameters_7_0= ruleParameterDef ) ) )*
                            loop14:
                            do {
                                int alt14=2;
                                int LA14_0 = input.LA(1);

                                if ( (LA14_0==22) ) {
                                    alt14=1;
                                }


                                switch (alt14) {
                            	case 1 :
                            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:567:4: otherlv_6= ',' ( (lv_parameters_7_0= ruleParameterDef ) )
                            	    {
                            	    otherlv_6=(Token)match(input,22,FOLLOW_22_in_ruleNativeFunc1302); 

                            	        	newLeafNode(otherlv_6, grammarAccess.getNativeFuncAccess().getCommaKeyword_4_0_1_1_0());
                            	        
                            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:571:1: ( (lv_parameters_7_0= ruleParameterDef ) )
                            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:572:1: (lv_parameters_7_0= ruleParameterDef )
                            	    {
                            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:572:1: (lv_parameters_7_0= ruleParameterDef )
                            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:573:3: lv_parameters_7_0= ruleParameterDef
                            	    {
                            	     
                            	    	        newCompositeNode(grammarAccess.getNativeFuncAccess().getParametersParameterDefParserRuleCall_4_0_1_1_1_0()); 
                            	    	    
                            	    pushFollow(FOLLOW_ruleParameterDef_in_ruleNativeFunc1323);
                            	    lv_parameters_7_0=ruleParameterDef();

                            	    state._fsp--;


                            	    	        if (current==null) {
                            	    	            current = createModelElementForParent(grammarAccess.getNativeFuncRule());
                            	    	        }
                            	           		add(
                            	           			current, 
                            	           			"parameters",
                            	            		lv_parameters_7_0, 
                            	            		"ParameterDef");
                            	    	        afterParserOrEnumRuleCall();
                            	    	    

                            	    }


                            	    }


                            	    }
                            	    break;

                            	default :
                            	    break loop14;
                                }
                            } while (true);


                            }
                            break;

                    }

                    otherlv_8=(Token)match(input,23,FOLLOW_23_in_ruleNativeFunc1339); 

                        	newLeafNode(otherlv_8, grammarAccess.getNativeFuncAccess().getRightParenthesisKeyword_4_0_2());
                        

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:594:6: (otherlv_9= 'takes' otherlv_10= 'nothing' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:594:6: (otherlv_9= 'takes' otherlv_10= 'nothing' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:594:8: otherlv_9= 'takes' otherlv_10= 'nothing'
                    {
                    otherlv_9=(Token)match(input,24,FOLLOW_24_in_ruleNativeFunc1359); 

                        	newLeafNode(otherlv_9, grammarAccess.getNativeFuncAccess().getTakesKeyword_4_1_0());
                        
                    otherlv_10=(Token)match(input,25,FOLLOW_25_in_ruleNativeFunc1371); 

                        	newLeafNode(otherlv_10, grammarAccess.getNativeFuncAccess().getNothingKeyword_4_1_1());
                        

                    }


                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:603:6: (otherlv_11= 'takes' ( (lv_parameters_12_0= ruleParameterDef ) ) (otherlv_13= ',' ( (lv_parameters_14_0= ruleParameterDef ) ) )* )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:603:6: (otherlv_11= 'takes' ( (lv_parameters_12_0= ruleParameterDef ) ) (otherlv_13= ',' ( (lv_parameters_14_0= ruleParameterDef ) ) )* )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:603:8: otherlv_11= 'takes' ( (lv_parameters_12_0= ruleParameterDef ) ) (otherlv_13= ',' ( (lv_parameters_14_0= ruleParameterDef ) ) )*
                    {
                    otherlv_11=(Token)match(input,24,FOLLOW_24_in_ruleNativeFunc1391); 

                        	newLeafNode(otherlv_11, grammarAccess.getNativeFuncAccess().getTakesKeyword_4_2_0());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:607:1: ( (lv_parameters_12_0= ruleParameterDef ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:608:1: (lv_parameters_12_0= ruleParameterDef )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:608:1: (lv_parameters_12_0= ruleParameterDef )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:609:3: lv_parameters_12_0= ruleParameterDef
                    {
                     
                    	        newCompositeNode(grammarAccess.getNativeFuncAccess().getParametersParameterDefParserRuleCall_4_2_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleParameterDef_in_ruleNativeFunc1412);
                    lv_parameters_12_0=ruleParameterDef();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getNativeFuncRule());
                    	        }
                           		add(
                           			current, 
                           			"parameters",
                            		lv_parameters_12_0, 
                            		"ParameterDef");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:625:2: (otherlv_13= ',' ( (lv_parameters_14_0= ruleParameterDef ) ) )*
                    loop16:
                    do {
                        int alt16=2;
                        int LA16_0 = input.LA(1);

                        if ( (LA16_0==22) ) {
                            alt16=1;
                        }


                        switch (alt16) {
                    	case 1 :
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:625:4: otherlv_13= ',' ( (lv_parameters_14_0= ruleParameterDef ) )
                    	    {
                    	    otherlv_13=(Token)match(input,22,FOLLOW_22_in_ruleNativeFunc1425); 

                    	        	newLeafNode(otherlv_13, grammarAccess.getNativeFuncAccess().getCommaKeyword_4_2_2_0());
                    	        
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:629:1: ( (lv_parameters_14_0= ruleParameterDef ) )
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:630:1: (lv_parameters_14_0= ruleParameterDef )
                    	    {
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:630:1: (lv_parameters_14_0= ruleParameterDef )
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:631:3: lv_parameters_14_0= ruleParameterDef
                    	    {
                    	     
                    	    	        newCompositeNode(grammarAccess.getNativeFuncAccess().getParametersParameterDefParserRuleCall_4_2_2_1_0()); 
                    	    	    
                    	    pushFollow(FOLLOW_ruleParameterDef_in_ruleNativeFunc1446);
                    	    lv_parameters_14_0=ruleParameterDef();

                    	    state._fsp--;


                    	    	        if (current==null) {
                    	    	            current = createModelElementForParent(grammarAccess.getNativeFuncRule());
                    	    	        }
                    	           		add(
                    	           			current, 
                    	           			"parameters",
                    	            		lv_parameters_14_0, 
                    	            		"ParameterDef");
                    	    	        afterParserOrEnumRuleCall();
                    	    	    

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop16;
                        }
                    } while (true);


                    }


                    }
                    break;

            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:647:6: ( (otherlv_15= 'returns' ( (lv_type_16_0= ruleTypeExpr ) ) ) | (otherlv_17= 'returns' otherlv_18= 'nothing' )? )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==26) ) {
                int LA19_1 = input.LA(2);

                if ( (LA19_1==25) ) {
                    alt19=2;
                }
                else if ( (LA19_1==RULE_ID||(LA19_1>=34 && LA19_1<=39)) ) {
                    alt19=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 19, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA19_0==RULE_NL) ) {
                alt19=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:647:7: (otherlv_15= 'returns' ( (lv_type_16_0= ruleTypeExpr ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:647:7: (otherlv_15= 'returns' ( (lv_type_16_0= ruleTypeExpr ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:647:9: otherlv_15= 'returns' ( (lv_type_16_0= ruleTypeExpr ) )
                    {
                    otherlv_15=(Token)match(input,26,FOLLOW_26_in_ruleNativeFunc1464); 

                        	newLeafNode(otherlv_15, grammarAccess.getNativeFuncAccess().getReturnsKeyword_5_0_0());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:651:1: ( (lv_type_16_0= ruleTypeExpr ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:652:1: (lv_type_16_0= ruleTypeExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:652:1: (lv_type_16_0= ruleTypeExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:653:3: lv_type_16_0= ruleTypeExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getNativeFuncAccess().getTypeTypeExprParserRuleCall_5_0_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleTypeExpr_in_ruleNativeFunc1485);
                    lv_type_16_0=ruleTypeExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getNativeFuncRule());
                    	        }
                           		set(
                           			current, 
                           			"type",
                            		lv_type_16_0, 
                            		"TypeExpr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:670:6: (otherlv_17= 'returns' otherlv_18= 'nothing' )?
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:670:6: (otherlv_17= 'returns' otherlv_18= 'nothing' )?
                    int alt18=2;
                    int LA18_0 = input.LA(1);

                    if ( (LA18_0==26) ) {
                        alt18=1;
                    }
                    switch (alt18) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:670:8: otherlv_17= 'returns' otherlv_18= 'nothing'
                            {
                            otherlv_17=(Token)match(input,26,FOLLOW_26_in_ruleNativeFunc1505); 

                                	newLeafNode(otherlv_17, grammarAccess.getNativeFuncAccess().getReturnsKeyword_5_1_0());
                                
                            otherlv_18=(Token)match(input,25,FOLLOW_25_in_ruleNativeFunc1517); 

                                	newLeafNode(otherlv_18, grammarAccess.getNativeFuncAccess().getNothingKeyword_5_1_1());
                                

                            }
                            break;

                    }


                    }
                    break;

            }

            this_NL_19=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleNativeFunc1531); 
             
                newLeafNode(this_NL_19, grammarAccess.getNativeFuncAccess().getNLTerminalRuleCall_6()); 
                

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:690:1: entryRuleNativeType returns [EObject current=null] : iv_ruleNativeType= ruleNativeType EOF ;
    public final EObject entryRuleNativeType() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleNativeType = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:691:2: (iv_ruleNativeType= ruleNativeType EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:692:2: iv_ruleNativeType= ruleNativeType EOF
            {
             newCompositeNode(grammarAccess.getNativeTypeRule()); 
            pushFollow(FOLLOW_ruleNativeType_in_entryRuleNativeType1566);
            iv_ruleNativeType=ruleNativeType();

            state._fsp--;

             current =iv_ruleNativeType; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleNativeType1576); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:699:1: ruleNativeType returns [EObject current=null] : ( () otherlv_1= 'type' ( (lv_name_2_0= RULE_ID ) ) (otherlv_3= 'extends' ( (lv_superName_4_0= ruleTypeExpr ) ) )? this_NL_5= RULE_NL ) ;
    public final EObject ruleNativeType() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token this_NL_5=null;
        EObject lv_superName_4_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:702:28: ( ( () otherlv_1= 'type' ( (lv_name_2_0= RULE_ID ) ) (otherlv_3= 'extends' ( (lv_superName_4_0= ruleTypeExpr ) ) )? this_NL_5= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:703:1: ( () otherlv_1= 'type' ( (lv_name_2_0= RULE_ID ) ) (otherlv_3= 'extends' ( (lv_superName_4_0= ruleTypeExpr ) ) )? this_NL_5= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:703:1: ( () otherlv_1= 'type' ( (lv_name_2_0= RULE_ID ) ) (otherlv_3= 'extends' ( (lv_superName_4_0= ruleTypeExpr ) ) )? this_NL_5= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:703:2: () otherlv_1= 'type' ( (lv_name_2_0= RULE_ID ) ) (otherlv_3= 'extends' ( (lv_superName_4_0= ruleTypeExpr ) ) )? this_NL_5= RULE_NL
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:703:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:704:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getNativeTypeAccess().getNativeTypeAction_0(),
                        current);
                

            }

            otherlv_1=(Token)match(input,27,FOLLOW_27_in_ruleNativeType1622); 

                	newLeafNode(otherlv_1, grammarAccess.getNativeTypeAccess().getTypeKeyword_1());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:713:1: ( (lv_name_2_0= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:714:1: (lv_name_2_0= RULE_ID )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:714:1: (lv_name_2_0= RULE_ID )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:715:3: lv_name_2_0= RULE_ID
            {
            lv_name_2_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleNativeType1639); 

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

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:731:2: (otherlv_3= 'extends' ( (lv_superName_4_0= ruleTypeExpr ) ) )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==28) ) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:731:4: otherlv_3= 'extends' ( (lv_superName_4_0= ruleTypeExpr ) )
                    {
                    otherlv_3=(Token)match(input,28,FOLLOW_28_in_ruleNativeType1657); 

                        	newLeafNode(otherlv_3, grammarAccess.getNativeTypeAccess().getExtendsKeyword_3_0());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:735:1: ( (lv_superName_4_0= ruleTypeExpr ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:736:1: (lv_superName_4_0= ruleTypeExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:736:1: (lv_superName_4_0= ruleTypeExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:737:3: lv_superName_4_0= ruleTypeExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getNativeTypeAccess().getSuperNameTypeExprParserRuleCall_3_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleTypeExpr_in_ruleNativeType1678);
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

            this_NL_5=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleNativeType1691); 
             
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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:765:1: entryRuleClassDef returns [EObject current=null] : iv_ruleClassDef= ruleClassDef EOF ;
    public final EObject entryRuleClassDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleClassDef = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:766:2: (iv_ruleClassDef= ruleClassDef EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:767:2: iv_ruleClassDef= ruleClassDef EOF
            {
             newCompositeNode(grammarAccess.getClassDefRule()); 
            pushFollow(FOLLOW_ruleClassDef_in_entryRuleClassDef1726);
            iv_ruleClassDef=ruleClassDef();

            state._fsp--;

             current =iv_ruleClassDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleClassDef1736); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:774:1: ruleClassDef returns [EObject current=null] : ( () otherlv_1= 'class' ( (lv_name_2_0= RULE_ID ) ) this_NL_3= RULE_NL (this_NL_4= RULE_NL | ( (lv_members_5_0= ruleClassMember ) ) )* otherlv_6= 'endclass' this_NL_7= RULE_NL ) ;
    public final EObject ruleClassDef() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token this_NL_3=null;
        Token this_NL_4=null;
        Token otherlv_6=null;
        Token this_NL_7=null;
        EObject lv_members_5_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:777:28: ( ( () otherlv_1= 'class' ( (lv_name_2_0= RULE_ID ) ) this_NL_3= RULE_NL (this_NL_4= RULE_NL | ( (lv_members_5_0= ruleClassMember ) ) )* otherlv_6= 'endclass' this_NL_7= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:778:1: ( () otherlv_1= 'class' ( (lv_name_2_0= RULE_ID ) ) this_NL_3= RULE_NL (this_NL_4= RULE_NL | ( (lv_members_5_0= ruleClassMember ) ) )* otherlv_6= 'endclass' this_NL_7= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:778:1: ( () otherlv_1= 'class' ( (lv_name_2_0= RULE_ID ) ) this_NL_3= RULE_NL (this_NL_4= RULE_NL | ( (lv_members_5_0= ruleClassMember ) ) )* otherlv_6= 'endclass' this_NL_7= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:778:2: () otherlv_1= 'class' ( (lv_name_2_0= RULE_ID ) ) this_NL_3= RULE_NL (this_NL_4= RULE_NL | ( (lv_members_5_0= ruleClassMember ) ) )* otherlv_6= 'endclass' this_NL_7= RULE_NL
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:778:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:779:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getClassDefAccess().getClassDefAction_0(),
                        current);
                

            }

            otherlv_1=(Token)match(input,29,FOLLOW_29_in_ruleClassDef1782); 

                	newLeafNode(otherlv_1, grammarAccess.getClassDefAccess().getClassKeyword_1());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:788:1: ( (lv_name_2_0= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:789:1: (lv_name_2_0= RULE_ID )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:789:1: (lv_name_2_0= RULE_ID )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:790:3: lv_name_2_0= RULE_ID
            {
            lv_name_2_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleClassDef1799); 

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

            this_NL_3=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleClassDef1815); 
             
                newLeafNode(this_NL_3, grammarAccess.getClassDefAccess().getNLTerminalRuleCall_3()); 
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:810:1: (this_NL_4= RULE_NL | ( (lv_members_5_0= ruleClassMember ) ) )*
            loop21:
            do {
                int alt21=3;
                int LA21_0 = input.LA(1);

                if ( (LA21_0==RULE_NL) ) {
                    alt21=1;
                }
                else if ( (LA21_0==RULE_ID||LA21_0==19||LA21_0==31||(LA21_0>=33 && LA21_0<=40)) ) {
                    alt21=2;
                }


                switch (alt21) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:810:2: this_NL_4= RULE_NL
            	    {
            	    this_NL_4=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleClassDef1826); 
            	     
            	        newLeafNode(this_NL_4, grammarAccess.getClassDefAccess().getNLTerminalRuleCall_4_0()); 
            	        

            	    }
            	    break;
            	case 2 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:815:6: ( (lv_members_5_0= ruleClassMember ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:815:6: ( (lv_members_5_0= ruleClassMember ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:816:1: (lv_members_5_0= ruleClassMember )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:816:1: (lv_members_5_0= ruleClassMember )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:817:3: lv_members_5_0= ruleClassMember
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getClassDefAccess().getMembersClassMemberParserRuleCall_4_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleClassMember_in_ruleClassDef1852);
            	    lv_members_5_0=ruleClassMember();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getClassDefRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"members",
            	            		lv_members_5_0, 
            	            		"ClassMember");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);

            otherlv_6=(Token)match(input,30,FOLLOW_30_in_ruleClassDef1866); 

                	newLeafNode(otherlv_6, grammarAccess.getClassDefAccess().getEndclassKeyword_5());
                
            this_NL_7=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleClassDef1877); 
             
                newLeafNode(this_NL_7, grammarAccess.getClassDefAccess().getNLTerminalRuleCall_6()); 
                

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:849:1: entryRuleClassMember returns [EObject current=null] : iv_ruleClassMember= ruleClassMember EOF ;
    public final EObject entryRuleClassMember() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleClassMember = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:850:2: (iv_ruleClassMember= ruleClassMember EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:851:2: iv_ruleClassMember= ruleClassMember EOF
            {
             newCompositeNode(grammarAccess.getClassMemberRule()); 
            pushFollow(FOLLOW_ruleClassMember_in_entryRuleClassMember1912);
            iv_ruleClassMember=ruleClassMember();

            state._fsp--;

             current =iv_ruleClassMember; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleClassMember1922); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:858:1: ruleClassMember returns [EObject current=null] : (this_VarDef_0= ruleVarDef | this_FuncDef_1= ruleFuncDef ) ;
    public final EObject ruleClassMember() throws RecognitionException {
        EObject current = null;

        EObject this_VarDef_0 = null;

        EObject this_FuncDef_1 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:861:28: ( (this_VarDef_0= ruleVarDef | this_FuncDef_1= ruleFuncDef ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:862:1: (this_VarDef_0= ruleVarDef | this_FuncDef_1= ruleFuncDef )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:862:1: (this_VarDef_0= ruleVarDef | this_FuncDef_1= ruleFuncDef )
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==RULE_ID||LA22_0==19||LA22_0==31||(LA22_0>=33 && LA22_0<=39)) ) {
                alt22=1;
            }
            else if ( (LA22_0==40) ) {
                alt22=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }
            switch (alt22) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:863:5: this_VarDef_0= ruleVarDef
                    {
                     
                            newCompositeNode(grammarAccess.getClassMemberAccess().getVarDefParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleVarDef_in_ruleClassMember1969);
                    this_VarDef_0=ruleVarDef();

                    state._fsp--;

                     
                            current = this_VarDef_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:873:5: this_FuncDef_1= ruleFuncDef
                    {
                     
                            newCompositeNode(grammarAccess.getClassMemberAccess().getFuncDefParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleFuncDef_in_ruleClassMember1996);
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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:889:1: entryRuleVarDef returns [EObject current=null] : iv_ruleVarDef= ruleVarDef EOF ;
    public final EObject entryRuleVarDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVarDef = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:890:2: (iv_ruleVarDef= ruleVarDef EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:891:2: iv_ruleVarDef= ruleVarDef EOF
            {
             newCompositeNode(grammarAccess.getVarDefRule()); 
            pushFollow(FOLLOW_ruleVarDef_in_entryRuleVarDef2031);
            iv_ruleVarDef=ruleVarDef();

            state._fsp--;

             current =iv_ruleVarDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleVarDef2041); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:898:1: ruleVarDef returns [EObject current=null] : ( () ( ( ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) ) | ( ( (lv_constant_6_0= 'constant' ) )? ( (lv_type_7_0= ruleTypeExpr ) ) ( (lv_name_8_0= RULE_ID ) ) (otherlv_9= '=' ( (lv_e_10_0= ruleExpr ) ) )? ) | (otherlv_11= 'glob' ( (lv_name_12_0= RULE_ID ) ) otherlv_13= '=' ( (lv_e_14_0= ruleExpr ) ) ) ) this_NL_15= RULE_NL ) ;
    public final EObject ruleVarDef() throws RecognitionException {
        EObject current = null;

        Token lv_constant_1_0=null;
        Token lv_name_3_0=null;
        Token otherlv_4=null;
        Token lv_constant_6_0=null;
        Token lv_name_8_0=null;
        Token otherlv_9=null;
        Token otherlv_11=null;
        Token lv_name_12_0=null;
        Token otherlv_13=null;
        Token this_NL_15=null;
        EObject lv_type_2_0 = null;

        EObject lv_e_5_0 = null;

        EObject lv_type_7_0 = null;

        EObject lv_e_10_0 = null;

        EObject lv_e_14_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:901:28: ( ( () ( ( ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) ) | ( ( (lv_constant_6_0= 'constant' ) )? ( (lv_type_7_0= ruleTypeExpr ) ) ( (lv_name_8_0= RULE_ID ) ) (otherlv_9= '=' ( (lv_e_10_0= ruleExpr ) ) )? ) | (otherlv_11= 'glob' ( (lv_name_12_0= RULE_ID ) ) otherlv_13= '=' ( (lv_e_14_0= ruleExpr ) ) ) ) this_NL_15= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:902:1: ( () ( ( ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) ) | ( ( (lv_constant_6_0= 'constant' ) )? ( (lv_type_7_0= ruleTypeExpr ) ) ( (lv_name_8_0= RULE_ID ) ) (otherlv_9= '=' ( (lv_e_10_0= ruleExpr ) ) )? ) | (otherlv_11= 'glob' ( (lv_name_12_0= RULE_ID ) ) otherlv_13= '=' ( (lv_e_14_0= ruleExpr ) ) ) ) this_NL_15= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:902:1: ( () ( ( ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) ) | ( ( (lv_constant_6_0= 'constant' ) )? ( (lv_type_7_0= ruleTypeExpr ) ) ( (lv_name_8_0= RULE_ID ) ) (otherlv_9= '=' ( (lv_e_10_0= ruleExpr ) ) )? ) | (otherlv_11= 'glob' ( (lv_name_12_0= RULE_ID ) ) otherlv_13= '=' ( (lv_e_14_0= ruleExpr ) ) ) ) this_NL_15= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:902:2: () ( ( ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) ) | ( ( (lv_constant_6_0= 'constant' ) )? ( (lv_type_7_0= ruleTypeExpr ) ) ( (lv_name_8_0= RULE_ID ) ) (otherlv_9= '=' ( (lv_e_10_0= ruleExpr ) ) )? ) | (otherlv_11= 'glob' ( (lv_name_12_0= RULE_ID ) ) otherlv_13= '=' ( (lv_e_14_0= ruleExpr ) ) ) ) this_NL_15= RULE_NL
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:902:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:903:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getVarDefAccess().getVarDefAction_0(),
                        current);
                

            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:908:2: ( ( ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) ) | ( ( (lv_constant_6_0= 'constant' ) )? ( (lv_type_7_0= ruleTypeExpr ) ) ( (lv_name_8_0= RULE_ID ) ) (otherlv_9= '=' ( (lv_e_10_0= ruleExpr ) ) )? ) | (otherlv_11= 'glob' ( (lv_name_12_0= RULE_ID ) ) otherlv_13= '=' ( (lv_e_14_0= ruleExpr ) ) ) )
            int alt26=3;
            switch ( input.LA(1) ) {
            case 31:
                {
                alt26=1;
                }
                break;
            case RULE_ID:
            case 19:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
                {
                alt26=2;
                }
                break;
            case 33:
                {
                alt26=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;
            }

            switch (alt26) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:908:3: ( ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:908:3: ( ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:908:4: ( (lv_constant_1_0= 'val' ) ) ( (lv_type_2_0= ruleTypeExpr ) )? ( (lv_name_3_0= RULE_ID ) ) otherlv_4= '=' ( (lv_e_5_0= ruleExpr ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:908:4: ( (lv_constant_1_0= 'val' ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:909:1: (lv_constant_1_0= 'val' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:909:1: (lv_constant_1_0= 'val' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:910:3: lv_constant_1_0= 'val'
                    {
                    lv_constant_1_0=(Token)match(input,31,FOLLOW_31_in_ruleVarDef2095); 

                            newLeafNode(lv_constant_1_0, grammarAccess.getVarDefAccess().getConstantValKeyword_1_0_0_0());
                        

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getVarDefRule());
                    	        }
                           		setWithLastConsumed(current, "constant", true, "val");
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:923:2: ( (lv_type_2_0= ruleTypeExpr ) )?
                    int alt23=2;
                    int LA23_0 = input.LA(1);

                    if ( (LA23_0==RULE_ID) ) {
                        int LA23_1 = input.LA(2);

                        if ( (LA23_1==RULE_ID) ) {
                            alt23=1;
                        }
                    }
                    else if ( ((LA23_0>=34 && LA23_0<=39)) ) {
                        alt23=1;
                    }
                    switch (alt23) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:924:1: (lv_type_2_0= ruleTypeExpr )
                            {
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:924:1: (lv_type_2_0= ruleTypeExpr )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:925:3: lv_type_2_0= ruleTypeExpr
                            {
                             
                            	        newCompositeNode(grammarAccess.getVarDefAccess().getTypeTypeExprParserRuleCall_1_0_1_0()); 
                            	    
                            pushFollow(FOLLOW_ruleTypeExpr_in_ruleVarDef2129);
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

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:941:3: ( (lv_name_3_0= RULE_ID ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:942:1: (lv_name_3_0= RULE_ID )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:942:1: (lv_name_3_0= RULE_ID )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:943:3: lv_name_3_0= RULE_ID
                    {
                    lv_name_3_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleVarDef2147); 

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

                    otherlv_4=(Token)match(input,32,FOLLOW_32_in_ruleVarDef2164); 

                        	newLeafNode(otherlv_4, grammarAccess.getVarDefAccess().getEqualsSignKeyword_1_0_3());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:963:1: ( (lv_e_5_0= ruleExpr ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:964:1: (lv_e_5_0= ruleExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:964:1: (lv_e_5_0= ruleExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:965:3: lv_e_5_0= ruleExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getVarDefAccess().getEExprParserRuleCall_1_0_4_0()); 
                    	    
                    pushFollow(FOLLOW_ruleExpr_in_ruleVarDef2185);
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
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:982:6: ( ( (lv_constant_6_0= 'constant' ) )? ( (lv_type_7_0= ruleTypeExpr ) ) ( (lv_name_8_0= RULE_ID ) ) (otherlv_9= '=' ( (lv_e_10_0= ruleExpr ) ) )? )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:982:6: ( ( (lv_constant_6_0= 'constant' ) )? ( (lv_type_7_0= ruleTypeExpr ) ) ( (lv_name_8_0= RULE_ID ) ) (otherlv_9= '=' ( (lv_e_10_0= ruleExpr ) ) )? )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:982:7: ( (lv_constant_6_0= 'constant' ) )? ( (lv_type_7_0= ruleTypeExpr ) ) ( (lv_name_8_0= RULE_ID ) ) (otherlv_9= '=' ( (lv_e_10_0= ruleExpr ) ) )?
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:982:7: ( (lv_constant_6_0= 'constant' ) )?
                    int alt24=2;
                    int LA24_0 = input.LA(1);

                    if ( (LA24_0==19) ) {
                        alt24=1;
                    }
                    switch (alt24) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:983:1: (lv_constant_6_0= 'constant' )
                            {
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:983:1: (lv_constant_6_0= 'constant' )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:984:3: lv_constant_6_0= 'constant'
                            {
                            lv_constant_6_0=(Token)match(input,19,FOLLOW_19_in_ruleVarDef2211); 

                                    newLeafNode(lv_constant_6_0, grammarAccess.getVarDefAccess().getConstantConstantKeyword_1_1_0_0());
                                

                            	        if (current==null) {
                            	            current = createModelElement(grammarAccess.getVarDefRule());
                            	        }
                                   		setWithLastConsumed(current, "constant", true, "constant");
                            	    

                            }


                            }
                            break;

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:997:3: ( (lv_type_7_0= ruleTypeExpr ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:998:1: (lv_type_7_0= ruleTypeExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:998:1: (lv_type_7_0= ruleTypeExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:999:3: lv_type_7_0= ruleTypeExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getVarDefAccess().getTypeTypeExprParserRuleCall_1_1_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleTypeExpr_in_ruleVarDef2246);
                    lv_type_7_0=ruleTypeExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getVarDefRule());
                    	        }
                           		set(
                           			current, 
                           			"type",
                            		lv_type_7_0, 
                            		"TypeExpr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1015:2: ( (lv_name_8_0= RULE_ID ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1016:1: (lv_name_8_0= RULE_ID )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1016:1: (lv_name_8_0= RULE_ID )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1017:3: lv_name_8_0= RULE_ID
                    {
                    lv_name_8_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleVarDef2263); 

                    			newLeafNode(lv_name_8_0, grammarAccess.getVarDefAccess().getNameIDTerminalRuleCall_1_1_2_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getVarDefRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"name",
                            		lv_name_8_0, 
                            		"ID");
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1033:2: (otherlv_9= '=' ( (lv_e_10_0= ruleExpr ) ) )?
                    int alt25=2;
                    int LA25_0 = input.LA(1);

                    if ( (LA25_0==32) ) {
                        alt25=1;
                    }
                    switch (alt25) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1033:4: otherlv_9= '=' ( (lv_e_10_0= ruleExpr ) )
                            {
                            otherlv_9=(Token)match(input,32,FOLLOW_32_in_ruleVarDef2281); 

                                	newLeafNode(otherlv_9, grammarAccess.getVarDefAccess().getEqualsSignKeyword_1_1_3_0());
                                
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1037:1: ( (lv_e_10_0= ruleExpr ) )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1038:1: (lv_e_10_0= ruleExpr )
                            {
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1038:1: (lv_e_10_0= ruleExpr )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1039:3: lv_e_10_0= ruleExpr
                            {
                             
                            	        newCompositeNode(grammarAccess.getVarDefAccess().getEExprParserRuleCall_1_1_3_1_0()); 
                            	    
                            pushFollow(FOLLOW_ruleExpr_in_ruleVarDef2302);
                            lv_e_10_0=ruleExpr();

                            state._fsp--;


                            	        if (current==null) {
                            	            current = createModelElementForParent(grammarAccess.getVarDefRule());
                            	        }
                                   		set(
                                   			current, 
                                   			"e",
                                    		lv_e_10_0, 
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
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1056:6: (otherlv_11= 'glob' ( (lv_name_12_0= RULE_ID ) ) otherlv_13= '=' ( (lv_e_14_0= ruleExpr ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1056:6: (otherlv_11= 'glob' ( (lv_name_12_0= RULE_ID ) ) otherlv_13= '=' ( (lv_e_14_0= ruleExpr ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1056:8: otherlv_11= 'glob' ( (lv_name_12_0= RULE_ID ) ) otherlv_13= '=' ( (lv_e_14_0= ruleExpr ) )
                    {
                    otherlv_11=(Token)match(input,33,FOLLOW_33_in_ruleVarDef2324); 

                        	newLeafNode(otherlv_11, grammarAccess.getVarDefAccess().getGlobKeyword_1_2_0());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1060:1: ( (lv_name_12_0= RULE_ID ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1061:1: (lv_name_12_0= RULE_ID )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1061:1: (lv_name_12_0= RULE_ID )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1062:3: lv_name_12_0= RULE_ID
                    {
                    lv_name_12_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleVarDef2341); 

                    			newLeafNode(lv_name_12_0, grammarAccess.getVarDefAccess().getNameIDTerminalRuleCall_1_2_1_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getVarDefRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"name",
                            		lv_name_12_0, 
                            		"ID");
                    	    

                    }


                    }

                    otherlv_13=(Token)match(input,32,FOLLOW_32_in_ruleVarDef2358); 

                        	newLeafNode(otherlv_13, grammarAccess.getVarDefAccess().getEqualsSignKeyword_1_2_2());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1082:1: ( (lv_e_14_0= ruleExpr ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1083:1: (lv_e_14_0= ruleExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1083:1: (lv_e_14_0= ruleExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1084:3: lv_e_14_0= ruleExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getVarDefAccess().getEExprParserRuleCall_1_2_3_0()); 
                    	    
                    pushFollow(FOLLOW_ruleExpr_in_ruleVarDef2379);
                    lv_e_14_0=ruleExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getVarDefRule());
                    	        }
                           		set(
                           			current, 
                           			"e",
                            		lv_e_14_0, 
                            		"Expr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }


                    }
                    break;

            }

            this_NL_15=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleVarDef2392); 
             
                newLeafNode(this_NL_15, grammarAccess.getVarDefAccess().getNLTerminalRuleCall_2()); 
                

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1112:1: entryRuleTypeExpr returns [EObject current=null] : iv_ruleTypeExpr= ruleTypeExpr EOF ;
    public final EObject entryRuleTypeExpr() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeExpr = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1113:2: (iv_ruleTypeExpr= ruleTypeExpr EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1114:2: iv_ruleTypeExpr= ruleTypeExpr EOF
            {
             newCompositeNode(grammarAccess.getTypeExprRule()); 
            pushFollow(FOLLOW_ruleTypeExpr_in_entryRuleTypeExpr2427);
            iv_ruleTypeExpr=ruleTypeExpr();

            state._fsp--;

             current =iv_ruleTypeExpr; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleTypeExpr2437); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1121:1: ruleTypeExpr returns [EObject current=null] : ( ( () ( (otherlv_1= RULE_ID ) ) ) | ( () ( ( (lv_name_3_1= 'integer' | lv_name_3_2= 'real' | lv_name_3_3= 'string' | lv_name_3_4= 'boolean' | lv_name_3_5= 'handle' | lv_name_3_6= 'code' ) ) ) ) ) ;
    public final EObject ruleTypeExpr() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token lv_name_3_1=null;
        Token lv_name_3_2=null;
        Token lv_name_3_3=null;
        Token lv_name_3_4=null;
        Token lv_name_3_5=null;
        Token lv_name_3_6=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1124:28: ( ( ( () ( (otherlv_1= RULE_ID ) ) ) | ( () ( ( (lv_name_3_1= 'integer' | lv_name_3_2= 'real' | lv_name_3_3= 'string' | lv_name_3_4= 'boolean' | lv_name_3_5= 'handle' | lv_name_3_6= 'code' ) ) ) ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1125:1: ( ( () ( (otherlv_1= RULE_ID ) ) ) | ( () ( ( (lv_name_3_1= 'integer' | lv_name_3_2= 'real' | lv_name_3_3= 'string' | lv_name_3_4= 'boolean' | lv_name_3_5= 'handle' | lv_name_3_6= 'code' ) ) ) ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1125:1: ( ( () ( (otherlv_1= RULE_ID ) ) ) | ( () ( ( (lv_name_3_1= 'integer' | lv_name_3_2= 'real' | lv_name_3_3= 'string' | lv_name_3_4= 'boolean' | lv_name_3_5= 'handle' | lv_name_3_6= 'code' ) ) ) ) )
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( (LA28_0==RULE_ID) ) {
                alt28=1;
            }
            else if ( ((LA28_0>=34 && LA28_0<=39)) ) {
                alt28=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                throw nvae;
            }
            switch (alt28) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1125:2: ( () ( (otherlv_1= RULE_ID ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1125:2: ( () ( (otherlv_1= RULE_ID ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1125:3: () ( (otherlv_1= RULE_ID ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1125:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1126:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getTypeExprAccess().getTypeExprRefAction_0_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1131:2: ( (otherlv_1= RULE_ID ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1132:1: (otherlv_1= RULE_ID )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1132:1: (otherlv_1= RULE_ID )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1133:3: otherlv_1= RULE_ID
                    {

                    			if (current==null) {
                    	            current = createModelElement(grammarAccess.getTypeExprRule());
                    	        }
                            
                    otherlv_1=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleTypeExpr2492); 

                    		newLeafNode(otherlv_1, grammarAccess.getTypeExprAccess().getNameTypeDefCrossReference_0_1_0()); 
                    	

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1145:6: ( () ( ( (lv_name_3_1= 'integer' | lv_name_3_2= 'real' | lv_name_3_3= 'string' | lv_name_3_4= 'boolean' | lv_name_3_5= 'handle' | lv_name_3_6= 'code' ) ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1145:6: ( () ( ( (lv_name_3_1= 'integer' | lv_name_3_2= 'real' | lv_name_3_3= 'string' | lv_name_3_4= 'boolean' | lv_name_3_5= 'handle' | lv_name_3_6= 'code' ) ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1145:7: () ( ( (lv_name_3_1= 'integer' | lv_name_3_2= 'real' | lv_name_3_3= 'string' | lv_name_3_4= 'boolean' | lv_name_3_5= 'handle' | lv_name_3_6= 'code' ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1145:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1146:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getTypeExprAccess().getTypeExprBuildinAction_1_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1151:2: ( ( (lv_name_3_1= 'integer' | lv_name_3_2= 'real' | lv_name_3_3= 'string' | lv_name_3_4= 'boolean' | lv_name_3_5= 'handle' | lv_name_3_6= 'code' ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1152:1: ( (lv_name_3_1= 'integer' | lv_name_3_2= 'real' | lv_name_3_3= 'string' | lv_name_3_4= 'boolean' | lv_name_3_5= 'handle' | lv_name_3_6= 'code' ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1152:1: ( (lv_name_3_1= 'integer' | lv_name_3_2= 'real' | lv_name_3_3= 'string' | lv_name_3_4= 'boolean' | lv_name_3_5= 'handle' | lv_name_3_6= 'code' ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1153:1: (lv_name_3_1= 'integer' | lv_name_3_2= 'real' | lv_name_3_3= 'string' | lv_name_3_4= 'boolean' | lv_name_3_5= 'handle' | lv_name_3_6= 'code' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1153:1: (lv_name_3_1= 'integer' | lv_name_3_2= 'real' | lv_name_3_3= 'string' | lv_name_3_4= 'boolean' | lv_name_3_5= 'handle' | lv_name_3_6= 'code' )
                    int alt27=6;
                    switch ( input.LA(1) ) {
                    case 34:
                        {
                        alt27=1;
                        }
                        break;
                    case 35:
                        {
                        alt27=2;
                        }
                        break;
                    case 36:
                        {
                        alt27=3;
                        }
                        break;
                    case 37:
                        {
                        alt27=4;
                        }
                        break;
                    case 38:
                        {
                        alt27=5;
                        }
                        break;
                    case 39:
                        {
                        alt27=6;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 27, 0, input);

                        throw nvae;
                    }

                    switch (alt27) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1154:3: lv_name_3_1= 'integer'
                            {
                            lv_name_3_1=(Token)match(input,34,FOLLOW_34_in_ruleTypeExpr2529); 

                                    newLeafNode(lv_name_3_1, grammarAccess.getTypeExprAccess().getNameIntegerKeyword_1_1_0_0());
                                

                            	        if (current==null) {
                            	            current = createModelElement(grammarAccess.getTypeExprRule());
                            	        }
                                   		setWithLastConsumed(current, "name", lv_name_3_1, null);
                            	    

                            }
                            break;
                        case 2 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1166:8: lv_name_3_2= 'real'
                            {
                            lv_name_3_2=(Token)match(input,35,FOLLOW_35_in_ruleTypeExpr2558); 

                                    newLeafNode(lv_name_3_2, grammarAccess.getTypeExprAccess().getNameRealKeyword_1_1_0_1());
                                

                            	        if (current==null) {
                            	            current = createModelElement(grammarAccess.getTypeExprRule());
                            	        }
                                   		setWithLastConsumed(current, "name", lv_name_3_2, null);
                            	    

                            }
                            break;
                        case 3 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1178:8: lv_name_3_3= 'string'
                            {
                            lv_name_3_3=(Token)match(input,36,FOLLOW_36_in_ruleTypeExpr2587); 

                                    newLeafNode(lv_name_3_3, grammarAccess.getTypeExprAccess().getNameStringKeyword_1_1_0_2());
                                

                            	        if (current==null) {
                            	            current = createModelElement(grammarAccess.getTypeExprRule());
                            	        }
                                   		setWithLastConsumed(current, "name", lv_name_3_3, null);
                            	    

                            }
                            break;
                        case 4 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1190:8: lv_name_3_4= 'boolean'
                            {
                            lv_name_3_4=(Token)match(input,37,FOLLOW_37_in_ruleTypeExpr2616); 

                                    newLeafNode(lv_name_3_4, grammarAccess.getTypeExprAccess().getNameBooleanKeyword_1_1_0_3());
                                

                            	        if (current==null) {
                            	            current = createModelElement(grammarAccess.getTypeExprRule());
                            	        }
                                   		setWithLastConsumed(current, "name", lv_name_3_4, null);
                            	    

                            }
                            break;
                        case 5 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1202:8: lv_name_3_5= 'handle'
                            {
                            lv_name_3_5=(Token)match(input,38,FOLLOW_38_in_ruleTypeExpr2645); 

                                    newLeafNode(lv_name_3_5, grammarAccess.getTypeExprAccess().getNameHandleKeyword_1_1_0_4());
                                

                            	        if (current==null) {
                            	            current = createModelElement(grammarAccess.getTypeExprRule());
                            	        }
                                   		setWithLastConsumed(current, "name", lv_name_3_5, null);
                            	    

                            }
                            break;
                        case 6 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1214:8: lv_name_3_6= 'code'
                            {
                            lv_name_3_6=(Token)match(input,39,FOLLOW_39_in_ruleTypeExpr2674); 

                                    newLeafNode(lv_name_3_6, grammarAccess.getTypeExprAccess().getNameCodeKeyword_1_1_0_5());
                                

                            	        if (current==null) {
                            	            current = createModelElement(grammarAccess.getTypeExprRule());
                            	        }
                                   		setWithLastConsumed(current, "name", lv_name_3_6, null);
                            	    

                            }
                            break;

                    }


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
    // $ANTLR end "ruleTypeExpr"


    // $ANTLR start "entryRuleFuncDef"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1237:1: entryRuleFuncDef returns [EObject current=null] : iv_ruleFuncDef= ruleFuncDef EOF ;
    public final EObject entryRuleFuncDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFuncDef = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1238:2: (iv_ruleFuncDef= ruleFuncDef EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1239:2: iv_ruleFuncDef= ruleFuncDef EOF
            {
             newCompositeNode(grammarAccess.getFuncDefRule()); 
            pushFollow(FOLLOW_ruleFuncDef_in_entryRuleFuncDef2727);
            iv_ruleFuncDef=ruleFuncDef();

            state._fsp--;

             current =iv_ruleFuncDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleFuncDef2737); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1246:1: ruleFuncDef returns [EObject current=null] : (otherlv_0= 'function' ( (lv_name_1_0= RULE_ID ) ) ( (otherlv_2= '(' ( ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )* )? otherlv_6= ')' ) | (otherlv_7= 'takes' otherlv_8= 'nothing' ) | (otherlv_9= 'takes' ( (lv_parameters_10_0= ruleParameterDef ) ) (otherlv_11= ',' ( (lv_parameters_12_0= ruleParameterDef ) ) )* ) ) ( (otherlv_13= 'returns' ( (lv_type_14_0= ruleTypeExpr ) ) ) | (otherlv_15= 'returns' otherlv_16= 'nothing' )? ) this_NL_17= RULE_NL ( (lv_body_18_0= ruleStatements ) ) otherlv_19= 'endfunction' ) ;
    public final EObject ruleFuncDef() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        Token otherlv_7=null;
        Token otherlv_8=null;
        Token otherlv_9=null;
        Token otherlv_11=null;
        Token otherlv_13=null;
        Token otherlv_15=null;
        Token otherlv_16=null;
        Token this_NL_17=null;
        Token otherlv_19=null;
        EObject lv_parameters_3_0 = null;

        EObject lv_parameters_5_0 = null;

        EObject lv_parameters_10_0 = null;

        EObject lv_parameters_12_0 = null;

        EObject lv_type_14_0 = null;

        EObject lv_body_18_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1249:28: ( (otherlv_0= 'function' ( (lv_name_1_0= RULE_ID ) ) ( (otherlv_2= '(' ( ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )* )? otherlv_6= ')' ) | (otherlv_7= 'takes' otherlv_8= 'nothing' ) | (otherlv_9= 'takes' ( (lv_parameters_10_0= ruleParameterDef ) ) (otherlv_11= ',' ( (lv_parameters_12_0= ruleParameterDef ) ) )* ) ) ( (otherlv_13= 'returns' ( (lv_type_14_0= ruleTypeExpr ) ) ) | (otherlv_15= 'returns' otherlv_16= 'nothing' )? ) this_NL_17= RULE_NL ( (lv_body_18_0= ruleStatements ) ) otherlv_19= 'endfunction' ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1250:1: (otherlv_0= 'function' ( (lv_name_1_0= RULE_ID ) ) ( (otherlv_2= '(' ( ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )* )? otherlv_6= ')' ) | (otherlv_7= 'takes' otherlv_8= 'nothing' ) | (otherlv_9= 'takes' ( (lv_parameters_10_0= ruleParameterDef ) ) (otherlv_11= ',' ( (lv_parameters_12_0= ruleParameterDef ) ) )* ) ) ( (otherlv_13= 'returns' ( (lv_type_14_0= ruleTypeExpr ) ) ) | (otherlv_15= 'returns' otherlv_16= 'nothing' )? ) this_NL_17= RULE_NL ( (lv_body_18_0= ruleStatements ) ) otherlv_19= 'endfunction' )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1250:1: (otherlv_0= 'function' ( (lv_name_1_0= RULE_ID ) ) ( (otherlv_2= '(' ( ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )* )? otherlv_6= ')' ) | (otherlv_7= 'takes' otherlv_8= 'nothing' ) | (otherlv_9= 'takes' ( (lv_parameters_10_0= ruleParameterDef ) ) (otherlv_11= ',' ( (lv_parameters_12_0= ruleParameterDef ) ) )* ) ) ( (otherlv_13= 'returns' ( (lv_type_14_0= ruleTypeExpr ) ) ) | (otherlv_15= 'returns' otherlv_16= 'nothing' )? ) this_NL_17= RULE_NL ( (lv_body_18_0= ruleStatements ) ) otherlv_19= 'endfunction' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1250:3: otherlv_0= 'function' ( (lv_name_1_0= RULE_ID ) ) ( (otherlv_2= '(' ( ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )* )? otherlv_6= ')' ) | (otherlv_7= 'takes' otherlv_8= 'nothing' ) | (otherlv_9= 'takes' ( (lv_parameters_10_0= ruleParameterDef ) ) (otherlv_11= ',' ( (lv_parameters_12_0= ruleParameterDef ) ) )* ) ) ( (otherlv_13= 'returns' ( (lv_type_14_0= ruleTypeExpr ) ) ) | (otherlv_15= 'returns' otherlv_16= 'nothing' )? ) this_NL_17= RULE_NL ( (lv_body_18_0= ruleStatements ) ) otherlv_19= 'endfunction'
            {
            otherlv_0=(Token)match(input,40,FOLLOW_40_in_ruleFuncDef2774); 

                	newLeafNode(otherlv_0, grammarAccess.getFuncDefAccess().getFunctionKeyword_0());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1254:1: ( (lv_name_1_0= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1255:1: (lv_name_1_0= RULE_ID )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1255:1: (lv_name_1_0= RULE_ID )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1256:3: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleFuncDef2791); 

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

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1272:2: ( (otherlv_2= '(' ( ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )* )? otherlv_6= ')' ) | (otherlv_7= 'takes' otherlv_8= 'nothing' ) | (otherlv_9= 'takes' ( (lv_parameters_10_0= ruleParameterDef ) ) (otherlv_11= ',' ( (lv_parameters_12_0= ruleParameterDef ) ) )* ) )
            int alt32=3;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==21) ) {
                alt32=1;
            }
            else if ( (LA32_0==24) ) {
                int LA32_2 = input.LA(2);

                if ( (LA32_2==RULE_ID||(LA32_2>=34 && LA32_2<=39)) ) {
                    alt32=3;
                }
                else if ( (LA32_2==25) ) {
                    alt32=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 32, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }
            switch (alt32) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1272:3: (otherlv_2= '(' ( ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )* )? otherlv_6= ')' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1272:3: (otherlv_2= '(' ( ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )* )? otherlv_6= ')' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1272:5: otherlv_2= '(' ( ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )* )? otherlv_6= ')'
                    {
                    otherlv_2=(Token)match(input,21,FOLLOW_21_in_ruleFuncDef2810); 

                        	newLeafNode(otherlv_2, grammarAccess.getFuncDefAccess().getLeftParenthesisKeyword_2_0_0());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1276:1: ( ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )* )?
                    int alt30=2;
                    int LA30_0 = input.LA(1);

                    if ( (LA30_0==RULE_ID||(LA30_0>=34 && LA30_0<=39)) ) {
                        alt30=1;
                    }
                    switch (alt30) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1276:2: ( (lv_parameters_3_0= ruleParameterDef ) ) (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )*
                            {
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1276:2: ( (lv_parameters_3_0= ruleParameterDef ) )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1277:1: (lv_parameters_3_0= ruleParameterDef )
                            {
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1277:1: (lv_parameters_3_0= ruleParameterDef )
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1278:3: lv_parameters_3_0= ruleParameterDef
                            {
                             
                            	        newCompositeNode(grammarAccess.getFuncDefAccess().getParametersParameterDefParserRuleCall_2_0_1_0_0()); 
                            	    
                            pushFollow(FOLLOW_ruleParameterDef_in_ruleFuncDef2832);
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

                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1294:2: (otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) ) )*
                            loop29:
                            do {
                                int alt29=2;
                                int LA29_0 = input.LA(1);

                                if ( (LA29_0==22) ) {
                                    alt29=1;
                                }


                                switch (alt29) {
                            	case 1 :
                            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1294:4: otherlv_4= ',' ( (lv_parameters_5_0= ruleParameterDef ) )
                            	    {
                            	    otherlv_4=(Token)match(input,22,FOLLOW_22_in_ruleFuncDef2845); 

                            	        	newLeafNode(otherlv_4, grammarAccess.getFuncDefAccess().getCommaKeyword_2_0_1_1_0());
                            	        
                            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1298:1: ( (lv_parameters_5_0= ruleParameterDef ) )
                            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1299:1: (lv_parameters_5_0= ruleParameterDef )
                            	    {
                            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1299:1: (lv_parameters_5_0= ruleParameterDef )
                            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1300:3: lv_parameters_5_0= ruleParameterDef
                            	    {
                            	     
                            	    	        newCompositeNode(grammarAccess.getFuncDefAccess().getParametersParameterDefParserRuleCall_2_0_1_1_1_0()); 
                            	    	    
                            	    pushFollow(FOLLOW_ruleParameterDef_in_ruleFuncDef2866);
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
                            	    break loop29;
                                }
                            } while (true);


                            }
                            break;

                    }

                    otherlv_6=(Token)match(input,23,FOLLOW_23_in_ruleFuncDef2882); 

                        	newLeafNode(otherlv_6, grammarAccess.getFuncDefAccess().getRightParenthesisKeyword_2_0_2());
                        

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1321:6: (otherlv_7= 'takes' otherlv_8= 'nothing' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1321:6: (otherlv_7= 'takes' otherlv_8= 'nothing' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1321:8: otherlv_7= 'takes' otherlv_8= 'nothing'
                    {
                    otherlv_7=(Token)match(input,24,FOLLOW_24_in_ruleFuncDef2902); 

                        	newLeafNode(otherlv_7, grammarAccess.getFuncDefAccess().getTakesKeyword_2_1_0());
                        
                    otherlv_8=(Token)match(input,25,FOLLOW_25_in_ruleFuncDef2914); 

                        	newLeafNode(otherlv_8, grammarAccess.getFuncDefAccess().getNothingKeyword_2_1_1());
                        

                    }


                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1330:6: (otherlv_9= 'takes' ( (lv_parameters_10_0= ruleParameterDef ) ) (otherlv_11= ',' ( (lv_parameters_12_0= ruleParameterDef ) ) )* )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1330:6: (otherlv_9= 'takes' ( (lv_parameters_10_0= ruleParameterDef ) ) (otherlv_11= ',' ( (lv_parameters_12_0= ruleParameterDef ) ) )* )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1330:8: otherlv_9= 'takes' ( (lv_parameters_10_0= ruleParameterDef ) ) (otherlv_11= ',' ( (lv_parameters_12_0= ruleParameterDef ) ) )*
                    {
                    otherlv_9=(Token)match(input,24,FOLLOW_24_in_ruleFuncDef2934); 

                        	newLeafNode(otherlv_9, grammarAccess.getFuncDefAccess().getTakesKeyword_2_2_0());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1334:1: ( (lv_parameters_10_0= ruleParameterDef ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1335:1: (lv_parameters_10_0= ruleParameterDef )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1335:1: (lv_parameters_10_0= ruleParameterDef )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1336:3: lv_parameters_10_0= ruleParameterDef
                    {
                     
                    	        newCompositeNode(grammarAccess.getFuncDefAccess().getParametersParameterDefParserRuleCall_2_2_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleParameterDef_in_ruleFuncDef2955);
                    lv_parameters_10_0=ruleParameterDef();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getFuncDefRule());
                    	        }
                           		add(
                           			current, 
                           			"parameters",
                            		lv_parameters_10_0, 
                            		"ParameterDef");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1352:2: (otherlv_11= ',' ( (lv_parameters_12_0= ruleParameterDef ) ) )*
                    loop31:
                    do {
                        int alt31=2;
                        int LA31_0 = input.LA(1);

                        if ( (LA31_0==22) ) {
                            alt31=1;
                        }


                        switch (alt31) {
                    	case 1 :
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1352:4: otherlv_11= ',' ( (lv_parameters_12_0= ruleParameterDef ) )
                    	    {
                    	    otherlv_11=(Token)match(input,22,FOLLOW_22_in_ruleFuncDef2968); 

                    	        	newLeafNode(otherlv_11, grammarAccess.getFuncDefAccess().getCommaKeyword_2_2_2_0());
                    	        
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1356:1: ( (lv_parameters_12_0= ruleParameterDef ) )
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1357:1: (lv_parameters_12_0= ruleParameterDef )
                    	    {
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1357:1: (lv_parameters_12_0= ruleParameterDef )
                    	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1358:3: lv_parameters_12_0= ruleParameterDef
                    	    {
                    	     
                    	    	        newCompositeNode(grammarAccess.getFuncDefAccess().getParametersParameterDefParserRuleCall_2_2_2_1_0()); 
                    	    	    
                    	    pushFollow(FOLLOW_ruleParameterDef_in_ruleFuncDef2989);
                    	    lv_parameters_12_0=ruleParameterDef();

                    	    state._fsp--;


                    	    	        if (current==null) {
                    	    	            current = createModelElementForParent(grammarAccess.getFuncDefRule());
                    	    	        }
                    	           		add(
                    	           			current, 
                    	           			"parameters",
                    	            		lv_parameters_12_0, 
                    	            		"ParameterDef");
                    	    	        afterParserOrEnumRuleCall();
                    	    	    

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop31;
                        }
                    } while (true);


                    }


                    }
                    break;

            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1374:6: ( (otherlv_13= 'returns' ( (lv_type_14_0= ruleTypeExpr ) ) ) | (otherlv_15= 'returns' otherlv_16= 'nothing' )? )
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==26) ) {
                int LA34_1 = input.LA(2);

                if ( (LA34_1==25) ) {
                    alt34=2;
                }
                else if ( (LA34_1==RULE_ID||(LA34_1>=34 && LA34_1<=39)) ) {
                    alt34=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 34, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA34_0==RULE_NL) ) {
                alt34=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;
            }
            switch (alt34) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1374:7: (otherlv_13= 'returns' ( (lv_type_14_0= ruleTypeExpr ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1374:7: (otherlv_13= 'returns' ( (lv_type_14_0= ruleTypeExpr ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1374:9: otherlv_13= 'returns' ( (lv_type_14_0= ruleTypeExpr ) )
                    {
                    otherlv_13=(Token)match(input,26,FOLLOW_26_in_ruleFuncDef3007); 

                        	newLeafNode(otherlv_13, grammarAccess.getFuncDefAccess().getReturnsKeyword_3_0_0());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1378:1: ( (lv_type_14_0= ruleTypeExpr ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1379:1: (lv_type_14_0= ruleTypeExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1379:1: (lv_type_14_0= ruleTypeExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1380:3: lv_type_14_0= ruleTypeExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getFuncDefAccess().getTypeTypeExprParserRuleCall_3_0_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleTypeExpr_in_ruleFuncDef3028);
                    lv_type_14_0=ruleTypeExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getFuncDefRule());
                    	        }
                           		set(
                           			current, 
                           			"type",
                            		lv_type_14_0, 
                            		"TypeExpr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1397:6: (otherlv_15= 'returns' otherlv_16= 'nothing' )?
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1397:6: (otherlv_15= 'returns' otherlv_16= 'nothing' )?
                    int alt33=2;
                    int LA33_0 = input.LA(1);

                    if ( (LA33_0==26) ) {
                        alt33=1;
                    }
                    switch (alt33) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1397:8: otherlv_15= 'returns' otherlv_16= 'nothing'
                            {
                            otherlv_15=(Token)match(input,26,FOLLOW_26_in_ruleFuncDef3048); 

                                	newLeafNode(otherlv_15, grammarAccess.getFuncDefAccess().getReturnsKeyword_3_1_0());
                                
                            otherlv_16=(Token)match(input,25,FOLLOW_25_in_ruleFuncDef3060); 

                                	newLeafNode(otherlv_16, grammarAccess.getFuncDefAccess().getNothingKeyword_3_1_1());
                                

                            }
                            break;

                    }


                    }
                    break;

            }

            this_NL_17=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleFuncDef3074); 
             
                newLeafNode(this_NL_17, grammarAccess.getFuncDefAccess().getNLTerminalRuleCall_4()); 
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1409:1: ( (lv_body_18_0= ruleStatements ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1410:1: (lv_body_18_0= ruleStatements )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1410:1: (lv_body_18_0= ruleStatements )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1411:3: lv_body_18_0= ruleStatements
            {
             
            	        newCompositeNode(grammarAccess.getFuncDefAccess().getBodyStatementsParserRuleCall_5_0()); 
            	    
            pushFollow(FOLLOW_ruleStatements_in_ruleFuncDef3094);
            lv_body_18_0=ruleStatements();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getFuncDefRule());
            	        }
                   		set(
                   			current, 
                   			"body",
                    		lv_body_18_0, 
                    		"Statements");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_19=(Token)match(input,41,FOLLOW_41_in_ruleFuncDef3106); 

                	newLeafNode(otherlv_19, grammarAccess.getFuncDefAccess().getEndfunctionKeyword_6());
                

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1439:1: entryRuleParameterDef returns [EObject current=null] : iv_ruleParameterDef= ruleParameterDef EOF ;
    public final EObject entryRuleParameterDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleParameterDef = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1440:2: (iv_ruleParameterDef= ruleParameterDef EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1441:2: iv_ruleParameterDef= ruleParameterDef EOF
            {
             newCompositeNode(grammarAccess.getParameterDefRule()); 
            pushFollow(FOLLOW_ruleParameterDef_in_entryRuleParameterDef3142);
            iv_ruleParameterDef=ruleParameterDef();

            state._fsp--;

             current =iv_ruleParameterDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleParameterDef3152); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1448:1: ruleParameterDef returns [EObject current=null] : ( () ( (lv_type_1_0= ruleTypeExpr ) ) ( (lv_name_2_0= RULE_ID ) ) ) ;
    public final EObject ruleParameterDef() throws RecognitionException {
        EObject current = null;

        Token lv_name_2_0=null;
        EObject lv_type_1_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1451:28: ( ( () ( (lv_type_1_0= ruleTypeExpr ) ) ( (lv_name_2_0= RULE_ID ) ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1452:1: ( () ( (lv_type_1_0= ruleTypeExpr ) ) ( (lv_name_2_0= RULE_ID ) ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1452:1: ( () ( (lv_type_1_0= ruleTypeExpr ) ) ( (lv_name_2_0= RULE_ID ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1452:2: () ( (lv_type_1_0= ruleTypeExpr ) ) ( (lv_name_2_0= RULE_ID ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1452:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1453:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getParameterDefAccess().getParameterDefAction_0(),
                        current);
                

            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1458:2: ( (lv_type_1_0= ruleTypeExpr ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1459:1: (lv_type_1_0= ruleTypeExpr )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1459:1: (lv_type_1_0= ruleTypeExpr )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1460:3: lv_type_1_0= ruleTypeExpr
            {
             
            	        newCompositeNode(grammarAccess.getParameterDefAccess().getTypeTypeExprParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleTypeExpr_in_ruleParameterDef3207);
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

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1476:2: ( (lv_name_2_0= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1477:1: (lv_name_2_0= RULE_ID )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1477:1: (lv_name_2_0= RULE_ID )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1478:3: lv_name_2_0= RULE_ID
            {
            lv_name_2_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleParameterDef3224); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1502:1: entryRuleStatements returns [EObject current=null] : iv_ruleStatements= ruleStatements EOF ;
    public final EObject entryRuleStatements() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatements = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1503:2: (iv_ruleStatements= ruleStatements EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1504:2: iv_ruleStatements= ruleStatements EOF
            {
             newCompositeNode(grammarAccess.getStatementsRule()); 
            pushFollow(FOLLOW_ruleStatements_in_entryRuleStatements3265);
            iv_ruleStatements=ruleStatements();

            state._fsp--;

             current =iv_ruleStatements; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStatements3275); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1511:1: ruleStatements returns [EObject current=null] : ( () (this_NL_1= RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )* ) ;
    public final EObject ruleStatements() throws RecognitionException {
        EObject current = null;

        Token this_NL_1=null;
        EObject lv_statements_2_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1514:28: ( ( () (this_NL_1= RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1515:1: ( () (this_NL_1= RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1515:1: ( () (this_NL_1= RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1515:2: () (this_NL_1= RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )*
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1515:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1516:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getStatementsAccess().getStatementsAction_0(),
                        current);
                

            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1521:2: (this_NL_1= RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )*
            loop35:
            do {
                int alt35=3;
                int LA35_0 = input.LA(1);

                if ( (LA35_0==RULE_NL) ) {
                    alt35=1;
                }
                else if ( ((LA35_0>=RULE_ID && LA35_0<=RULE_STRING)||LA35_0==21||LA35_0==31||LA35_0==40||(LA35_0>=42 && LA35_0<=44)||(LA35_0>=46 && LA35_0<=47)||LA35_0==52||(LA35_0>=54 && LA35_0<=55)||(LA35_0>=66 && LA35_0<=67)||(LA35_0>=72 && LA35_0<=74)) ) {
                    alt35=2;
                }


                switch (alt35) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1521:3: this_NL_1= RULE_NL
            	    {
            	    this_NL_1=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStatements3321); 
            	     
            	        newLeafNode(this_NL_1, grammarAccess.getStatementsAccess().getNLTerminalRuleCall_1_0()); 
            	        

            	    }
            	    break;
            	case 2 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1526:6: ( (lv_statements_2_0= ruleStatement ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1526:6: ( (lv_statements_2_0= ruleStatement ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1527:1: (lv_statements_2_0= ruleStatement )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1527:1: (lv_statements_2_0= ruleStatement )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1528:3: lv_statements_2_0= ruleStatement
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getStatementsAccess().getStatementsStatementParserRuleCall_1_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleStatement_in_ruleStatements3347);
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
            	    break loop35;
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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1552:1: entryRuleStatement returns [EObject current=null] : iv_ruleStatement= ruleStatement EOF ;
    public final EObject entryRuleStatement() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatement = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1553:2: (iv_ruleStatement= ruleStatement EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1554:2: iv_ruleStatement= ruleStatement EOF
            {
             newCompositeNode(grammarAccess.getStatementRule()); 
            pushFollow(FOLLOW_ruleStatement_in_entryRuleStatement3385);
            iv_ruleStatement=ruleStatement();

            state._fsp--;

             current =iv_ruleStatement; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStatement3395); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1561:1: ruleStatement returns [EObject current=null] : (this_StmtIf_0= ruleStmtIf | this_StmtWhile_1= ruleStmtWhile | this_LocalVarDef_2= ruleLocalVarDef | this_StmtSetOrCall_3= ruleStmtSetOrCall | this_StmtReturn_4= ruleStmtReturn | this_StmtLoop_5= ruleStmtLoop | this_StmtExitwhen_6= ruleStmtExitwhen ) ;
    public final EObject ruleStatement() throws RecognitionException {
        EObject current = null;

        EObject this_StmtIf_0 = null;

        EObject this_StmtWhile_1 = null;

        EObject this_LocalVarDef_2 = null;

        EObject this_StmtSetOrCall_3 = null;

        EObject this_StmtReturn_4 = null;

        EObject this_StmtLoop_5 = null;

        EObject this_StmtExitwhen_6 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1564:28: ( (this_StmtIf_0= ruleStmtIf | this_StmtWhile_1= ruleStmtWhile | this_LocalVarDef_2= ruleLocalVarDef | this_StmtSetOrCall_3= ruleStmtSetOrCall | this_StmtReturn_4= ruleStmtReturn | this_StmtLoop_5= ruleStmtLoop | this_StmtExitwhen_6= ruleStmtExitwhen ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1565:1: (this_StmtIf_0= ruleStmtIf | this_StmtWhile_1= ruleStmtWhile | this_LocalVarDef_2= ruleLocalVarDef | this_StmtSetOrCall_3= ruleStmtSetOrCall | this_StmtReturn_4= ruleStmtReturn | this_StmtLoop_5= ruleStmtLoop | this_StmtExitwhen_6= ruleStmtExitwhen )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1565:1: (this_StmtIf_0= ruleStmtIf | this_StmtWhile_1= ruleStmtWhile | this_LocalVarDef_2= ruleLocalVarDef | this_StmtSetOrCall_3= ruleStmtSetOrCall | this_StmtReturn_4= ruleStmtReturn | this_StmtLoop_5= ruleStmtLoop | this_StmtExitwhen_6= ruleStmtExitwhen )
            int alt36=7;
            switch ( input.LA(1) ) {
            case 47:
                {
                alt36=1;
                }
                break;
            case 52:
                {
                alt36=2;
                }
                break;
            case 31:
            case 42:
                {
                alt36=3;
                }
                break;
            case RULE_ID:
            case RULE_INT:
            case RULE_STRING:
            case 21:
            case 40:
            case 54:
            case 55:
            case 66:
            case 67:
            case 72:
            case 73:
            case 74:
                {
                alt36=4;
                }
                break;
            case 46:
                {
                alt36=5;
                }
                break;
            case 44:
                {
                alt36=6;
                }
                break;
            case 43:
                {
                alt36=7;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;
            }

            switch (alt36) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1566:5: this_StmtIf_0= ruleStmtIf
                    {
                     
                            newCompositeNode(grammarAccess.getStatementAccess().getStmtIfParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleStmtIf_in_ruleStatement3442);
                    this_StmtIf_0=ruleStmtIf();

                    state._fsp--;

                     
                            current = this_StmtIf_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1576:5: this_StmtWhile_1= ruleStmtWhile
                    {
                     
                            newCompositeNode(grammarAccess.getStatementAccess().getStmtWhileParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleStmtWhile_in_ruleStatement3469);
                    this_StmtWhile_1=ruleStmtWhile();

                    state._fsp--;

                     
                            current = this_StmtWhile_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1586:5: this_LocalVarDef_2= ruleLocalVarDef
                    {
                     
                            newCompositeNode(grammarAccess.getStatementAccess().getLocalVarDefParserRuleCall_2()); 
                        
                    pushFollow(FOLLOW_ruleLocalVarDef_in_ruleStatement3496);
                    this_LocalVarDef_2=ruleLocalVarDef();

                    state._fsp--;

                     
                            current = this_LocalVarDef_2; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 4 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1596:5: this_StmtSetOrCall_3= ruleStmtSetOrCall
                    {
                     
                            newCompositeNode(grammarAccess.getStatementAccess().getStmtSetOrCallParserRuleCall_3()); 
                        
                    pushFollow(FOLLOW_ruleStmtSetOrCall_in_ruleStatement3523);
                    this_StmtSetOrCall_3=ruleStmtSetOrCall();

                    state._fsp--;

                     
                            current = this_StmtSetOrCall_3; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 5 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1606:5: this_StmtReturn_4= ruleStmtReturn
                    {
                     
                            newCompositeNode(grammarAccess.getStatementAccess().getStmtReturnParserRuleCall_4()); 
                        
                    pushFollow(FOLLOW_ruleStmtReturn_in_ruleStatement3550);
                    this_StmtReturn_4=ruleStmtReturn();

                    state._fsp--;

                     
                            current = this_StmtReturn_4; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 6 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1616:5: this_StmtLoop_5= ruleStmtLoop
                    {
                     
                            newCompositeNode(grammarAccess.getStatementAccess().getStmtLoopParserRuleCall_5()); 
                        
                    pushFollow(FOLLOW_ruleStmtLoop_in_ruleStatement3577);
                    this_StmtLoop_5=ruleStmtLoop();

                    state._fsp--;

                     
                            current = this_StmtLoop_5; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 7 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1626:5: this_StmtExitwhen_6= ruleStmtExitwhen
                    {
                     
                            newCompositeNode(grammarAccess.getStatementAccess().getStmtExitwhenParserRuleCall_6()); 
                        
                    pushFollow(FOLLOW_ruleStmtExitwhen_in_ruleStatement3604);
                    this_StmtExitwhen_6=ruleStmtExitwhen();

                    state._fsp--;

                     
                            current = this_StmtExitwhen_6; 
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


    // $ANTLR start "entryRuleLocalVarDef"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1642:1: entryRuleLocalVarDef returns [EObject current=null] : iv_ruleLocalVarDef= ruleLocalVarDef EOF ;
    public final EObject entryRuleLocalVarDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLocalVarDef = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1643:2: (iv_ruleLocalVarDef= ruleLocalVarDef EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1644:2: iv_ruleLocalVarDef= ruleLocalVarDef EOF
            {
             newCompositeNode(grammarAccess.getLocalVarDefRule()); 
            pushFollow(FOLLOW_ruleLocalVarDef_in_entryRuleLocalVarDef3639);
            iv_ruleLocalVarDef=ruleLocalVarDef();

            state._fsp--;

             current =iv_ruleLocalVarDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleLocalVarDef3649); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1651:1: ruleLocalVarDef returns [EObject current=null] : ( () (otherlv_1= 'local' | ( (lv_constant_2_0= 'val' ) ) ) ( (lv_type_3_0= ruleTypeExpr ) )? ( (lv_name_4_0= RULE_ID ) ) (otherlv_5= '=' ( (lv_e_6_0= ruleExpr ) ) )? this_NL_7= RULE_NL ) ;
    public final EObject ruleLocalVarDef() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token lv_constant_2_0=null;
        Token lv_name_4_0=null;
        Token otherlv_5=null;
        Token this_NL_7=null;
        EObject lv_type_3_0 = null;

        EObject lv_e_6_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1654:28: ( ( () (otherlv_1= 'local' | ( (lv_constant_2_0= 'val' ) ) ) ( (lv_type_3_0= ruleTypeExpr ) )? ( (lv_name_4_0= RULE_ID ) ) (otherlv_5= '=' ( (lv_e_6_0= ruleExpr ) ) )? this_NL_7= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1655:1: ( () (otherlv_1= 'local' | ( (lv_constant_2_0= 'val' ) ) ) ( (lv_type_3_0= ruleTypeExpr ) )? ( (lv_name_4_0= RULE_ID ) ) (otherlv_5= '=' ( (lv_e_6_0= ruleExpr ) ) )? this_NL_7= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1655:1: ( () (otherlv_1= 'local' | ( (lv_constant_2_0= 'val' ) ) ) ( (lv_type_3_0= ruleTypeExpr ) )? ( (lv_name_4_0= RULE_ID ) ) (otherlv_5= '=' ( (lv_e_6_0= ruleExpr ) ) )? this_NL_7= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1655:2: () (otherlv_1= 'local' | ( (lv_constant_2_0= 'val' ) ) ) ( (lv_type_3_0= ruleTypeExpr ) )? ( (lv_name_4_0= RULE_ID ) ) (otherlv_5= '=' ( (lv_e_6_0= ruleExpr ) ) )? this_NL_7= RULE_NL
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1655:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1656:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getLocalVarDefAccess().getVarDefAction_0(),
                        current);
                

            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1661:2: (otherlv_1= 'local' | ( (lv_constant_2_0= 'val' ) ) )
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==42) ) {
                alt37=1;
            }
            else if ( (LA37_0==31) ) {
                alt37=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 37, 0, input);

                throw nvae;
            }
            switch (alt37) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1661:4: otherlv_1= 'local'
                    {
                    otherlv_1=(Token)match(input,42,FOLLOW_42_in_ruleLocalVarDef3696); 

                        	newLeafNode(otherlv_1, grammarAccess.getLocalVarDefAccess().getLocalKeyword_1_0());
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1666:6: ( (lv_constant_2_0= 'val' ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1666:6: ( (lv_constant_2_0= 'val' ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1667:1: (lv_constant_2_0= 'val' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1667:1: (lv_constant_2_0= 'val' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1668:3: lv_constant_2_0= 'val'
                    {
                    lv_constant_2_0=(Token)match(input,31,FOLLOW_31_in_ruleLocalVarDef3720); 

                            newLeafNode(lv_constant_2_0, grammarAccess.getLocalVarDefAccess().getConstantValKeyword_1_1_0());
                        

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getLocalVarDefRule());
                    	        }
                           		setWithLastConsumed(current, "constant", true, "val");
                    	    

                    }


                    }


                    }
                    break;

            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1681:3: ( (lv_type_3_0= ruleTypeExpr ) )?
            int alt38=2;
            int LA38_0 = input.LA(1);

            if ( (LA38_0==RULE_ID) ) {
                int LA38_1 = input.LA(2);

                if ( (LA38_1==RULE_ID) ) {
                    alt38=1;
                }
            }
            else if ( ((LA38_0>=34 && LA38_0<=39)) ) {
                alt38=1;
            }
            switch (alt38) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1682:1: (lv_type_3_0= ruleTypeExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1682:1: (lv_type_3_0= ruleTypeExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1683:3: lv_type_3_0= ruleTypeExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getLocalVarDefAccess().getTypeTypeExprParserRuleCall_2_0()); 
                    	    
                    pushFollow(FOLLOW_ruleTypeExpr_in_ruleLocalVarDef3755);
                    lv_type_3_0=ruleTypeExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getLocalVarDefRule());
                    	        }
                           		set(
                           			current, 
                           			"type",
                            		lv_type_3_0, 
                            		"TypeExpr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }
                    break;

            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1699:3: ( (lv_name_4_0= RULE_ID ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1700:1: (lv_name_4_0= RULE_ID )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1700:1: (lv_name_4_0= RULE_ID )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1701:3: lv_name_4_0= RULE_ID
            {
            lv_name_4_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleLocalVarDef3773); 

            			newLeafNode(lv_name_4_0, grammarAccess.getLocalVarDefAccess().getNameIDTerminalRuleCall_3_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getLocalVarDefRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_4_0, 
                    		"ID");
            	    

            }


            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1717:2: (otherlv_5= '=' ( (lv_e_6_0= ruleExpr ) ) )?
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==32) ) {
                alt39=1;
            }
            switch (alt39) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1717:4: otherlv_5= '=' ( (lv_e_6_0= ruleExpr ) )
                    {
                    otherlv_5=(Token)match(input,32,FOLLOW_32_in_ruleLocalVarDef3791); 

                        	newLeafNode(otherlv_5, grammarAccess.getLocalVarDefAccess().getEqualsSignKeyword_4_0());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1721:1: ( (lv_e_6_0= ruleExpr ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1722:1: (lv_e_6_0= ruleExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1722:1: (lv_e_6_0= ruleExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1723:3: lv_e_6_0= ruleExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getLocalVarDefAccess().getEExprParserRuleCall_4_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleExpr_in_ruleLocalVarDef3812);
                    lv_e_6_0=ruleExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getLocalVarDefRule());
                    	        }
                           		set(
                           			current, 
                           			"e",
                            		lv_e_6_0, 
                            		"Expr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }
                    break;

            }

            this_NL_7=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleLocalVarDef3825); 
             
                newLeafNode(this_NL_7, grammarAccess.getLocalVarDefAccess().getNLTerminalRuleCall_5()); 
                

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


    // $ANTLR start "entryRuleStmtExitwhen"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1751:1: entryRuleStmtExitwhen returns [EObject current=null] : iv_ruleStmtExitwhen= ruleStmtExitwhen EOF ;
    public final EObject entryRuleStmtExitwhen() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStmtExitwhen = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1752:2: (iv_ruleStmtExitwhen= ruleStmtExitwhen EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1753:2: iv_ruleStmtExitwhen= ruleStmtExitwhen EOF
            {
             newCompositeNode(grammarAccess.getStmtExitwhenRule()); 
            pushFollow(FOLLOW_ruleStmtExitwhen_in_entryRuleStmtExitwhen3860);
            iv_ruleStmtExitwhen=ruleStmtExitwhen();

            state._fsp--;

             current =iv_ruleStmtExitwhen; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStmtExitwhen3870); 

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
    // $ANTLR end "entryRuleStmtExitwhen"


    // $ANTLR start "ruleStmtExitwhen"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1760:1: ruleStmtExitwhen returns [EObject current=null] : (otherlv_0= 'exitwhen' ( (lv_e_1_0= ruleExpr ) ) this_NL_2= RULE_NL ) ;
    public final EObject ruleStmtExitwhen() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token this_NL_2=null;
        EObject lv_e_1_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1763:28: ( (otherlv_0= 'exitwhen' ( (lv_e_1_0= ruleExpr ) ) this_NL_2= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1764:1: (otherlv_0= 'exitwhen' ( (lv_e_1_0= ruleExpr ) ) this_NL_2= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1764:1: (otherlv_0= 'exitwhen' ( (lv_e_1_0= ruleExpr ) ) this_NL_2= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1764:3: otherlv_0= 'exitwhen' ( (lv_e_1_0= ruleExpr ) ) this_NL_2= RULE_NL
            {
            otherlv_0=(Token)match(input,43,FOLLOW_43_in_ruleStmtExitwhen3907); 

                	newLeafNode(otherlv_0, grammarAccess.getStmtExitwhenAccess().getExitwhenKeyword_0());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1768:1: ( (lv_e_1_0= ruleExpr ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1769:1: (lv_e_1_0= ruleExpr )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1769:1: (lv_e_1_0= ruleExpr )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1770:3: lv_e_1_0= ruleExpr
            {
             
            	        newCompositeNode(grammarAccess.getStmtExitwhenAccess().getEExprParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleExpr_in_ruleStmtExitwhen3928);
            lv_e_1_0=ruleExpr();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getStmtExitwhenRule());
            	        }
                   		set(
                   			current, 
                   			"e",
                    		lv_e_1_0, 
                    		"Expr");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            this_NL_2=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtExitwhen3939); 
             
                newLeafNode(this_NL_2, grammarAccess.getStmtExitwhenAccess().getNLTerminalRuleCall_2()); 
                

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
    // $ANTLR end "ruleStmtExitwhen"


    // $ANTLR start "entryRuleStmtLoop"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1798:1: entryRuleStmtLoop returns [EObject current=null] : iv_ruleStmtLoop= ruleStmtLoop EOF ;
    public final EObject entryRuleStmtLoop() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStmtLoop = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1799:2: (iv_ruleStmtLoop= ruleStmtLoop EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1800:2: iv_ruleStmtLoop= ruleStmtLoop EOF
            {
             newCompositeNode(grammarAccess.getStmtLoopRule()); 
            pushFollow(FOLLOW_ruleStmtLoop_in_entryRuleStmtLoop3974);
            iv_ruleStmtLoop=ruleStmtLoop();

            state._fsp--;

             current =iv_ruleStmtLoop; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStmtLoop3984); 

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
    // $ANTLR end "entryRuleStmtLoop"


    // $ANTLR start "ruleStmtLoop"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1807:1: ruleStmtLoop returns [EObject current=null] : (otherlv_0= 'loop' this_NL_1= RULE_NL ( (lv_body_2_0= ruleStatements ) ) otherlv_3= 'endloop' this_NL_4= RULE_NL ) ;
    public final EObject ruleStmtLoop() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token this_NL_1=null;
        Token otherlv_3=null;
        Token this_NL_4=null;
        EObject lv_body_2_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1810:28: ( (otherlv_0= 'loop' this_NL_1= RULE_NL ( (lv_body_2_0= ruleStatements ) ) otherlv_3= 'endloop' this_NL_4= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1811:1: (otherlv_0= 'loop' this_NL_1= RULE_NL ( (lv_body_2_0= ruleStatements ) ) otherlv_3= 'endloop' this_NL_4= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1811:1: (otherlv_0= 'loop' this_NL_1= RULE_NL ( (lv_body_2_0= ruleStatements ) ) otherlv_3= 'endloop' this_NL_4= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1811:3: otherlv_0= 'loop' this_NL_1= RULE_NL ( (lv_body_2_0= ruleStatements ) ) otherlv_3= 'endloop' this_NL_4= RULE_NL
            {
            otherlv_0=(Token)match(input,44,FOLLOW_44_in_ruleStmtLoop4021); 

                	newLeafNode(otherlv_0, grammarAccess.getStmtLoopAccess().getLoopKeyword_0());
                
            this_NL_1=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtLoop4032); 
             
                newLeafNode(this_NL_1, grammarAccess.getStmtLoopAccess().getNLTerminalRuleCall_1()); 
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1819:1: ( (lv_body_2_0= ruleStatements ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1820:1: (lv_body_2_0= ruleStatements )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1820:1: (lv_body_2_0= ruleStatements )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1821:3: lv_body_2_0= ruleStatements
            {
             
            	        newCompositeNode(grammarAccess.getStmtLoopAccess().getBodyStatementsParserRuleCall_2_0()); 
            	    
            pushFollow(FOLLOW_ruleStatements_in_ruleStmtLoop4052);
            lv_body_2_0=ruleStatements();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getStmtLoopRule());
            	        }
                   		set(
                   			current, 
                   			"body",
                    		lv_body_2_0, 
                    		"Statements");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_3=(Token)match(input,45,FOLLOW_45_in_ruleStmtLoop4064); 

                	newLeafNode(otherlv_3, grammarAccess.getStmtLoopAccess().getEndloopKeyword_3());
                
            this_NL_4=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtLoop4075); 
             
                newLeafNode(this_NL_4, grammarAccess.getStmtLoopAccess().getNLTerminalRuleCall_4()); 
                

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
    // $ANTLR end "ruleStmtLoop"


    // $ANTLR start "entryRuleStmtReturn"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1853:1: entryRuleStmtReturn returns [EObject current=null] : iv_ruleStmtReturn= ruleStmtReturn EOF ;
    public final EObject entryRuleStmtReturn() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStmtReturn = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1854:2: (iv_ruleStmtReturn= ruleStmtReturn EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1855:2: iv_ruleStmtReturn= ruleStmtReturn EOF
            {
             newCompositeNode(grammarAccess.getStmtReturnRule()); 
            pushFollow(FOLLOW_ruleStmtReturn_in_entryRuleStmtReturn4110);
            iv_ruleStmtReturn=ruleStmtReturn();

            state._fsp--;

             current =iv_ruleStmtReturn; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStmtReturn4120); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1862:1: ruleStmtReturn returns [EObject current=null] : ( () otherlv_1= 'return' ( (lv_e_2_0= ruleExpr ) )? this_NL_3= RULE_NL ) ;
    public final EObject ruleStmtReturn() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token this_NL_3=null;
        EObject lv_e_2_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1865:28: ( ( () otherlv_1= 'return' ( (lv_e_2_0= ruleExpr ) )? this_NL_3= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1866:1: ( () otherlv_1= 'return' ( (lv_e_2_0= ruleExpr ) )? this_NL_3= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1866:1: ( () otherlv_1= 'return' ( (lv_e_2_0= ruleExpr ) )? this_NL_3= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1866:2: () otherlv_1= 'return' ( (lv_e_2_0= ruleExpr ) )? this_NL_3= RULE_NL
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1866:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1867:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getStmtReturnAccess().getStmtReturnAction_0(),
                        current);
                

            }

            otherlv_1=(Token)match(input,46,FOLLOW_46_in_ruleStmtReturn4166); 

                	newLeafNode(otherlv_1, grammarAccess.getStmtReturnAccess().getReturnKeyword_1());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1876:1: ( (lv_e_2_0= ruleExpr ) )?
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( ((LA40_0>=RULE_ID && LA40_0<=RULE_STRING)||LA40_0==21||LA40_0==40||(LA40_0>=66 && LA40_0<=67)||(LA40_0>=72 && LA40_0<=74)) ) {
                alt40=1;
            }
            switch (alt40) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1877:1: (lv_e_2_0= ruleExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1877:1: (lv_e_2_0= ruleExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1878:3: lv_e_2_0= ruleExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getStmtReturnAccess().getEExprParserRuleCall_2_0()); 
                    	    
                    pushFollow(FOLLOW_ruleExpr_in_ruleStmtReturn4187);
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

            this_NL_3=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtReturn4199); 
             
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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1906:1: entryRuleStmtIf returns [EObject current=null] : iv_ruleStmtIf= ruleStmtIf EOF ;
    public final EObject entryRuleStmtIf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStmtIf = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1907:2: (iv_ruleStmtIf= ruleStmtIf EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1908:2: iv_ruleStmtIf= ruleStmtIf EOF
            {
             newCompositeNode(grammarAccess.getStmtIfRule()); 
            pushFollow(FOLLOW_ruleStmtIf_in_entryRuleStmtIf4234);
            iv_ruleStmtIf=ruleStmtIf();

            state._fsp--;

             current =iv_ruleStmtIf; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStmtIf4244); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1915:1: ruleStmtIf returns [EObject current=null] : (otherlv_0= 'if' ( (lv_cond_1_0= ruleExpr ) ) (otherlv_2= 'then' )? this_NL_3= RULE_NL ( (lv_thenBlock_4_0= ruleStatements ) ) (otherlv_5= 'elseif' ( (lv_elseIfConds_6_0= ruleExpr ) ) (otherlv_7= 'then' )? this_NL_8= RULE_NL ( (lv_elseIfBlocks_9_0= ruleStatements ) ) )* (otherlv_10= 'else' this_NL_11= RULE_NL ( (lv_elseBlock_12_0= ruleStatements ) ) )? otherlv_13= 'endif' this_NL_14= RULE_NL ) ;
    public final EObject ruleStmtIf() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_2=null;
        Token this_NL_3=null;
        Token otherlv_5=null;
        Token otherlv_7=null;
        Token this_NL_8=null;
        Token otherlv_10=null;
        Token this_NL_11=null;
        Token otherlv_13=null;
        Token this_NL_14=null;
        EObject lv_cond_1_0 = null;

        EObject lv_thenBlock_4_0 = null;

        EObject lv_elseIfConds_6_0 = null;

        EObject lv_elseIfBlocks_9_0 = null;

        EObject lv_elseBlock_12_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1918:28: ( (otherlv_0= 'if' ( (lv_cond_1_0= ruleExpr ) ) (otherlv_2= 'then' )? this_NL_3= RULE_NL ( (lv_thenBlock_4_0= ruleStatements ) ) (otherlv_5= 'elseif' ( (lv_elseIfConds_6_0= ruleExpr ) ) (otherlv_7= 'then' )? this_NL_8= RULE_NL ( (lv_elseIfBlocks_9_0= ruleStatements ) ) )* (otherlv_10= 'else' this_NL_11= RULE_NL ( (lv_elseBlock_12_0= ruleStatements ) ) )? otherlv_13= 'endif' this_NL_14= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1919:1: (otherlv_0= 'if' ( (lv_cond_1_0= ruleExpr ) ) (otherlv_2= 'then' )? this_NL_3= RULE_NL ( (lv_thenBlock_4_0= ruleStatements ) ) (otherlv_5= 'elseif' ( (lv_elseIfConds_6_0= ruleExpr ) ) (otherlv_7= 'then' )? this_NL_8= RULE_NL ( (lv_elseIfBlocks_9_0= ruleStatements ) ) )* (otherlv_10= 'else' this_NL_11= RULE_NL ( (lv_elseBlock_12_0= ruleStatements ) ) )? otherlv_13= 'endif' this_NL_14= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1919:1: (otherlv_0= 'if' ( (lv_cond_1_0= ruleExpr ) ) (otherlv_2= 'then' )? this_NL_3= RULE_NL ( (lv_thenBlock_4_0= ruleStatements ) ) (otherlv_5= 'elseif' ( (lv_elseIfConds_6_0= ruleExpr ) ) (otherlv_7= 'then' )? this_NL_8= RULE_NL ( (lv_elseIfBlocks_9_0= ruleStatements ) ) )* (otherlv_10= 'else' this_NL_11= RULE_NL ( (lv_elseBlock_12_0= ruleStatements ) ) )? otherlv_13= 'endif' this_NL_14= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1919:3: otherlv_0= 'if' ( (lv_cond_1_0= ruleExpr ) ) (otherlv_2= 'then' )? this_NL_3= RULE_NL ( (lv_thenBlock_4_0= ruleStatements ) ) (otherlv_5= 'elseif' ( (lv_elseIfConds_6_0= ruleExpr ) ) (otherlv_7= 'then' )? this_NL_8= RULE_NL ( (lv_elseIfBlocks_9_0= ruleStatements ) ) )* (otherlv_10= 'else' this_NL_11= RULE_NL ( (lv_elseBlock_12_0= ruleStatements ) ) )? otherlv_13= 'endif' this_NL_14= RULE_NL
            {
            otherlv_0=(Token)match(input,47,FOLLOW_47_in_ruleStmtIf4281); 

                	newLeafNode(otherlv_0, grammarAccess.getStmtIfAccess().getIfKeyword_0());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1923:1: ( (lv_cond_1_0= ruleExpr ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1924:1: (lv_cond_1_0= ruleExpr )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1924:1: (lv_cond_1_0= ruleExpr )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1925:3: lv_cond_1_0= ruleExpr
            {
             
            	        newCompositeNode(grammarAccess.getStmtIfAccess().getCondExprParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleExpr_in_ruleStmtIf4302);
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

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1941:2: (otherlv_2= 'then' )?
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==48) ) {
                alt41=1;
            }
            switch (alt41) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1941:4: otherlv_2= 'then'
                    {
                    otherlv_2=(Token)match(input,48,FOLLOW_48_in_ruleStmtIf4315); 

                        	newLeafNode(otherlv_2, grammarAccess.getStmtIfAccess().getThenKeyword_2());
                        

                    }
                    break;

            }

            this_NL_3=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtIf4328); 
             
                newLeafNode(this_NL_3, grammarAccess.getStmtIfAccess().getNLTerminalRuleCall_3()); 
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1949:1: ( (lv_thenBlock_4_0= ruleStatements ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1950:1: (lv_thenBlock_4_0= ruleStatements )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1950:1: (lv_thenBlock_4_0= ruleStatements )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1951:3: lv_thenBlock_4_0= ruleStatements
            {
             
            	        newCompositeNode(grammarAccess.getStmtIfAccess().getThenBlockStatementsParserRuleCall_4_0()); 
            	    
            pushFollow(FOLLOW_ruleStatements_in_ruleStmtIf4348);
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

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1967:2: (otherlv_5= 'elseif' ( (lv_elseIfConds_6_0= ruleExpr ) ) (otherlv_7= 'then' )? this_NL_8= RULE_NL ( (lv_elseIfBlocks_9_0= ruleStatements ) ) )*
            loop43:
            do {
                int alt43=2;
                int LA43_0 = input.LA(1);

                if ( (LA43_0==49) ) {
                    alt43=1;
                }


                switch (alt43) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1967:4: otherlv_5= 'elseif' ( (lv_elseIfConds_6_0= ruleExpr ) ) (otherlv_7= 'then' )? this_NL_8= RULE_NL ( (lv_elseIfBlocks_9_0= ruleStatements ) )
            	    {
            	    otherlv_5=(Token)match(input,49,FOLLOW_49_in_ruleStmtIf4361); 

            	        	newLeafNode(otherlv_5, grammarAccess.getStmtIfAccess().getElseifKeyword_5_0());
            	        
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1971:1: ( (lv_elseIfConds_6_0= ruleExpr ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1972:1: (lv_elseIfConds_6_0= ruleExpr )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1972:1: (lv_elseIfConds_6_0= ruleExpr )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1973:3: lv_elseIfConds_6_0= ruleExpr
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getStmtIfAccess().getElseIfCondsExprParserRuleCall_5_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExpr_in_ruleStmtIf4382);
            	    lv_elseIfConds_6_0=ruleExpr();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getStmtIfRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"elseIfConds",
            	            		lv_elseIfConds_6_0, 
            	            		"Expr");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1989:2: (otherlv_7= 'then' )?
            	    int alt42=2;
            	    int LA42_0 = input.LA(1);

            	    if ( (LA42_0==48) ) {
            	        alt42=1;
            	    }
            	    switch (alt42) {
            	        case 1 :
            	            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1989:4: otherlv_7= 'then'
            	            {
            	            otherlv_7=(Token)match(input,48,FOLLOW_48_in_ruleStmtIf4395); 

            	                	newLeafNode(otherlv_7, grammarAccess.getStmtIfAccess().getThenKeyword_5_2());
            	                

            	            }
            	            break;

            	    }

            	    this_NL_8=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtIf4408); 
            	     
            	        newLeafNode(this_NL_8, grammarAccess.getStmtIfAccess().getNLTerminalRuleCall_5_3()); 
            	        
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1997:1: ( (lv_elseIfBlocks_9_0= ruleStatements ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1998:1: (lv_elseIfBlocks_9_0= ruleStatements )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1998:1: (lv_elseIfBlocks_9_0= ruleStatements )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1999:3: lv_elseIfBlocks_9_0= ruleStatements
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getStmtIfAccess().getElseIfBlocksStatementsParserRuleCall_5_4_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleStatements_in_ruleStmtIf4428);
            	    lv_elseIfBlocks_9_0=ruleStatements();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getStmtIfRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"elseIfBlocks",
            	            		lv_elseIfBlocks_9_0, 
            	            		"Statements");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop43;
                }
            } while (true);

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2015:4: (otherlv_10= 'else' this_NL_11= RULE_NL ( (lv_elseBlock_12_0= ruleStatements ) ) )?
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==50) ) {
                alt44=1;
            }
            switch (alt44) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2015:6: otherlv_10= 'else' this_NL_11= RULE_NL ( (lv_elseBlock_12_0= ruleStatements ) )
                    {
                    otherlv_10=(Token)match(input,50,FOLLOW_50_in_ruleStmtIf4443); 

                        	newLeafNode(otherlv_10, grammarAccess.getStmtIfAccess().getElseKeyword_6_0());
                        
                    this_NL_11=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtIf4454); 
                     
                        newLeafNode(this_NL_11, grammarAccess.getStmtIfAccess().getNLTerminalRuleCall_6_1()); 
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2023:1: ( (lv_elseBlock_12_0= ruleStatements ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2024:1: (lv_elseBlock_12_0= ruleStatements )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2024:1: (lv_elseBlock_12_0= ruleStatements )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2025:3: lv_elseBlock_12_0= ruleStatements
                    {
                     
                    	        newCompositeNode(grammarAccess.getStmtIfAccess().getElseBlockStatementsParserRuleCall_6_2_0()); 
                    	    
                    pushFollow(FOLLOW_ruleStatements_in_ruleStmtIf4474);
                    lv_elseBlock_12_0=ruleStatements();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getStmtIfRule());
                    	        }
                           		set(
                           			current, 
                           			"elseBlock",
                            		lv_elseBlock_12_0, 
                            		"Statements");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }
                    break;

            }

            otherlv_13=(Token)match(input,51,FOLLOW_51_in_ruleStmtIf4488); 

                	newLeafNode(otherlv_13, grammarAccess.getStmtIfAccess().getEndifKeyword_7());
                
            this_NL_14=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtIf4499); 
             
                newLeafNode(this_NL_14, grammarAccess.getStmtIfAccess().getNLTerminalRuleCall_8()); 
                

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2057:1: entryRuleStmtWhile returns [EObject current=null] : iv_ruleStmtWhile= ruleStmtWhile EOF ;
    public final EObject entryRuleStmtWhile() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStmtWhile = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2058:2: (iv_ruleStmtWhile= ruleStmtWhile EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2059:2: iv_ruleStmtWhile= ruleStmtWhile EOF
            {
             newCompositeNode(grammarAccess.getStmtWhileRule()); 
            pushFollow(FOLLOW_ruleStmtWhile_in_entryRuleStmtWhile4534);
            iv_ruleStmtWhile=ruleStmtWhile();

            state._fsp--;

             current =iv_ruleStmtWhile; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStmtWhile4544); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2066:1: ruleStmtWhile returns [EObject current=null] : (otherlv_0= 'while' ( (lv_cond_1_0= ruleExpr ) ) this_NL_2= RULE_NL ( (lv_body_3_0= ruleStatements ) ) otherlv_4= 'endwhile' this_NL_5= RULE_NL ) ;
    public final EObject ruleStmtWhile() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token this_NL_2=null;
        Token otherlv_4=null;
        Token this_NL_5=null;
        EObject lv_cond_1_0 = null;

        EObject lv_body_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2069:28: ( (otherlv_0= 'while' ( (lv_cond_1_0= ruleExpr ) ) this_NL_2= RULE_NL ( (lv_body_3_0= ruleStatements ) ) otherlv_4= 'endwhile' this_NL_5= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2070:1: (otherlv_0= 'while' ( (lv_cond_1_0= ruleExpr ) ) this_NL_2= RULE_NL ( (lv_body_3_0= ruleStatements ) ) otherlv_4= 'endwhile' this_NL_5= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2070:1: (otherlv_0= 'while' ( (lv_cond_1_0= ruleExpr ) ) this_NL_2= RULE_NL ( (lv_body_3_0= ruleStatements ) ) otherlv_4= 'endwhile' this_NL_5= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2070:3: otherlv_0= 'while' ( (lv_cond_1_0= ruleExpr ) ) this_NL_2= RULE_NL ( (lv_body_3_0= ruleStatements ) ) otherlv_4= 'endwhile' this_NL_5= RULE_NL
            {
            otherlv_0=(Token)match(input,52,FOLLOW_52_in_ruleStmtWhile4581); 

                	newLeafNode(otherlv_0, grammarAccess.getStmtWhileAccess().getWhileKeyword_0());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2074:1: ( (lv_cond_1_0= ruleExpr ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2075:1: (lv_cond_1_0= ruleExpr )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2075:1: (lv_cond_1_0= ruleExpr )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2076:3: lv_cond_1_0= ruleExpr
            {
             
            	        newCompositeNode(grammarAccess.getStmtWhileAccess().getCondExprParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleExpr_in_ruleStmtWhile4602);
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

            this_NL_2=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtWhile4613); 
             
                newLeafNode(this_NL_2, grammarAccess.getStmtWhileAccess().getNLTerminalRuleCall_2()); 
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2096:1: ( (lv_body_3_0= ruleStatements ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2097:1: (lv_body_3_0= ruleStatements )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2097:1: (lv_body_3_0= ruleStatements )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2098:3: lv_body_3_0= ruleStatements
            {
             
            	        newCompositeNode(grammarAccess.getStmtWhileAccess().getBodyStatementsParserRuleCall_3_0()); 
            	    
            pushFollow(FOLLOW_ruleStatements_in_ruleStmtWhile4633);
            lv_body_3_0=ruleStatements();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getStmtWhileRule());
            	        }
                   		set(
                   			current, 
                   			"body",
                    		lv_body_3_0, 
                    		"Statements");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_4=(Token)match(input,53,FOLLOW_53_in_ruleStmtWhile4645); 

                	newLeafNode(otherlv_4, grammarAccess.getStmtWhileAccess().getEndwhileKeyword_4());
                
            this_NL_5=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtWhile4656); 
             
                newLeafNode(this_NL_5, grammarAccess.getStmtWhileAccess().getNLTerminalRuleCall_5()); 
                

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


    // $ANTLR start "entryRuleStmtSetOrCall"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2130:1: entryRuleStmtSetOrCall returns [EObject current=null] : iv_ruleStmtSetOrCall= ruleStmtSetOrCall EOF ;
    public final EObject entryRuleStmtSetOrCall() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStmtSetOrCall = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2131:2: (iv_ruleStmtSetOrCall= ruleStmtSetOrCall EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2132:2: iv_ruleStmtSetOrCall= ruleStmtSetOrCall EOF
            {
             newCompositeNode(grammarAccess.getStmtSetOrCallRule()); 
            pushFollow(FOLLOW_ruleStmtSetOrCall_in_entryRuleStmtSetOrCall4691);
            iv_ruleStmtSetOrCall=ruleStmtSetOrCall();

            state._fsp--;

             current =iv_ruleStmtSetOrCall; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStmtSetOrCall4701); 

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
    // $ANTLR end "entryRuleStmtSetOrCall"


    // $ANTLR start "ruleStmtSetOrCall"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2139:1: ruleStmtSetOrCall returns [EObject current=null] : ( () (otherlv_1= 'call' | otherlv_2= 'set' )? ( (lv_e_3_0= ruleExpr ) ) ( () ( (lv_opAssignment_5_0= ruleOpAssignment ) ) ( (lv_right_6_0= ruleExpr ) ) )? this_NL_7= RULE_NL ) ;
    public final EObject ruleStmtSetOrCall() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token this_NL_7=null;
        EObject lv_e_3_0 = null;

        EObject lv_opAssignment_5_0 = null;

        EObject lv_right_6_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2142:28: ( ( () (otherlv_1= 'call' | otherlv_2= 'set' )? ( (lv_e_3_0= ruleExpr ) ) ( () ( (lv_opAssignment_5_0= ruleOpAssignment ) ) ( (lv_right_6_0= ruleExpr ) ) )? this_NL_7= RULE_NL ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2143:1: ( () (otherlv_1= 'call' | otherlv_2= 'set' )? ( (lv_e_3_0= ruleExpr ) ) ( () ( (lv_opAssignment_5_0= ruleOpAssignment ) ) ( (lv_right_6_0= ruleExpr ) ) )? this_NL_7= RULE_NL )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2143:1: ( () (otherlv_1= 'call' | otherlv_2= 'set' )? ( (lv_e_3_0= ruleExpr ) ) ( () ( (lv_opAssignment_5_0= ruleOpAssignment ) ) ( (lv_right_6_0= ruleExpr ) ) )? this_NL_7= RULE_NL )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2143:2: () (otherlv_1= 'call' | otherlv_2= 'set' )? ( (lv_e_3_0= ruleExpr ) ) ( () ( (lv_opAssignment_5_0= ruleOpAssignment ) ) ( (lv_right_6_0= ruleExpr ) ) )? this_NL_7= RULE_NL
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2143:2: ()
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2144:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getStmtSetOrCallAccess().getStmtCallAction_0(),
                        current);
                

            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2149:2: (otherlv_1= 'call' | otherlv_2= 'set' )?
            int alt45=3;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==54) ) {
                alt45=1;
            }
            else if ( (LA45_0==55) ) {
                alt45=2;
            }
            switch (alt45) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2149:4: otherlv_1= 'call'
                    {
                    otherlv_1=(Token)match(input,54,FOLLOW_54_in_ruleStmtSetOrCall4748); 

                        	newLeafNode(otherlv_1, grammarAccess.getStmtSetOrCallAccess().getCallKeyword_1_0());
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2154:7: otherlv_2= 'set'
                    {
                    otherlv_2=(Token)match(input,55,FOLLOW_55_in_ruleStmtSetOrCall4766); 

                        	newLeafNode(otherlv_2, grammarAccess.getStmtSetOrCallAccess().getSetKeyword_1_1());
                        

                    }
                    break;

            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2158:3: ( (lv_e_3_0= ruleExpr ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2159:1: (lv_e_3_0= ruleExpr )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2159:1: (lv_e_3_0= ruleExpr )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2160:3: lv_e_3_0= ruleExpr
            {
             
            	        newCompositeNode(grammarAccess.getStmtSetOrCallAccess().getEExprParserRuleCall_2_0()); 
            	    
            pushFollow(FOLLOW_ruleExpr_in_ruleStmtSetOrCall4789);
            lv_e_3_0=ruleExpr();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getStmtSetOrCallRule());
            	        }
                   		set(
                   			current, 
                   			"e",
                    		lv_e_3_0, 
                    		"Expr");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2176:2: ( () ( (lv_opAssignment_5_0= ruleOpAssignment ) ) ( (lv_right_6_0= ruleExpr ) ) )?
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==32||(LA46_0>=56 && LA46_0<=57)) ) {
                alt46=1;
            }
            switch (alt46) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2176:3: () ( (lv_opAssignment_5_0= ruleOpAssignment ) ) ( (lv_right_6_0= ruleExpr ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2176:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2177:5: 
                    {

                            current = forceCreateModelElementAndSet(
                                grammarAccess.getStmtSetOrCallAccess().getStmtSetLeftAction_3_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2182:2: ( (lv_opAssignment_5_0= ruleOpAssignment ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2183:1: (lv_opAssignment_5_0= ruleOpAssignment )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2183:1: (lv_opAssignment_5_0= ruleOpAssignment )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2184:3: lv_opAssignment_5_0= ruleOpAssignment
                    {
                     
                    	        newCompositeNode(grammarAccess.getStmtSetOrCallAccess().getOpAssignmentOpAssignmentParserRuleCall_3_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleOpAssignment_in_ruleStmtSetOrCall4820);
                    lv_opAssignment_5_0=ruleOpAssignment();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getStmtSetOrCallRule());
                    	        }
                           		set(
                           			current, 
                           			"opAssignment",
                            		lv_opAssignment_5_0, 
                            		"OpAssignment");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2200:2: ( (lv_right_6_0= ruleExpr ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2201:1: (lv_right_6_0= ruleExpr )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2201:1: (lv_right_6_0= ruleExpr )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2202:3: lv_right_6_0= ruleExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getStmtSetOrCallAccess().getRightExprParserRuleCall_3_2_0()); 
                    	    
                    pushFollow(FOLLOW_ruleExpr_in_ruleStmtSetOrCall4841);
                    lv_right_6_0=ruleExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getStmtSetOrCallRule());
                    	        }
                           		set(
                           			current, 
                           			"right",
                            		lv_right_6_0, 
                            		"Expr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }
                    break;

            }

            this_NL_7=(Token)match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtSetOrCall4854); 
             
                newLeafNode(this_NL_7, grammarAccess.getStmtSetOrCallAccess().getNLTerminalRuleCall_4()); 
                

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
    // $ANTLR end "ruleStmtSetOrCall"


    // $ANTLR start "entryRuleOpAssignment"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2230:1: entryRuleOpAssignment returns [EObject current=null] : iv_ruleOpAssignment= ruleOpAssignment EOF ;
    public final EObject entryRuleOpAssignment() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOpAssignment = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2231:2: (iv_ruleOpAssignment= ruleOpAssignment EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2232:2: iv_ruleOpAssignment= ruleOpAssignment EOF
            {
             newCompositeNode(grammarAccess.getOpAssignmentRule()); 
            pushFollow(FOLLOW_ruleOpAssignment_in_entryRuleOpAssignment4889);
            iv_ruleOpAssignment=ruleOpAssignment();

            state._fsp--;

             current =iv_ruleOpAssignment; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleOpAssignment4899); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2239:1: ruleOpAssignment returns [EObject current=null] : ( ( () otherlv_1= '=' ) | ( () otherlv_3= '+=' ) | ( () otherlv_5= '-=' ) ) ;
    public final EObject ruleOpAssignment() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        Token otherlv_5=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2242:28: ( ( ( () otherlv_1= '=' ) | ( () otherlv_3= '+=' ) | ( () otherlv_5= '-=' ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2243:1: ( ( () otherlv_1= '=' ) | ( () otherlv_3= '+=' ) | ( () otherlv_5= '-=' ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2243:1: ( ( () otherlv_1= '=' ) | ( () otherlv_3= '+=' ) | ( () otherlv_5= '-=' ) )
            int alt47=3;
            switch ( input.LA(1) ) {
            case 32:
                {
                alt47=1;
                }
                break;
            case 56:
                {
                alt47=2;
                }
                break;
            case 57:
                {
                alt47=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;
            }

            switch (alt47) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2243:2: ( () otherlv_1= '=' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2243:2: ( () otherlv_1= '=' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2243:3: () otherlv_1= '='
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2243:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2244:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpAssignmentAccess().getOpAssignAction_0_0(),
                                current);
                        

                    }

                    otherlv_1=(Token)match(input,32,FOLLOW_32_in_ruleOpAssignment4946); 

                        	newLeafNode(otherlv_1, grammarAccess.getOpAssignmentAccess().getEqualsSignKeyword_0_1());
                        

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2254:6: ( () otherlv_3= '+=' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2254:6: ( () otherlv_3= '+=' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2254:7: () otherlv_3= '+='
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2254:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2255:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpAssignmentAccess().getOpPlusAssignAction_1_0(),
                                current);
                        

                    }

                    otherlv_3=(Token)match(input,56,FOLLOW_56_in_ruleOpAssignment4975); 

                        	newLeafNode(otherlv_3, grammarAccess.getOpAssignmentAccess().getPlusSignEqualsSignKeyword_1_1());
                        

                    }


                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2265:6: ( () otherlv_5= '-=' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2265:6: ( () otherlv_5= '-=' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2265:7: () otherlv_5= '-='
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2265:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2266:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpAssignmentAccess().getOpMinusAssignAction_2_0(),
                                current);
                        

                    }

                    otherlv_5=(Token)match(input,57,FOLLOW_57_in_ruleOpAssignment5004); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2283:1: entryRuleExpr returns [EObject current=null] : iv_ruleExpr= ruleExpr EOF ;
    public final EObject entryRuleExpr() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExpr = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2284:2: (iv_ruleExpr= ruleExpr EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2285:2: iv_ruleExpr= ruleExpr EOF
            {
             newCompositeNode(grammarAccess.getExprRule()); 
            pushFollow(FOLLOW_ruleExpr_in_entryRuleExpr5041);
            iv_ruleExpr=ruleExpr();

            state._fsp--;

             current =iv_ruleExpr; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExpr5051); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2292:1: ruleExpr returns [EObject current=null] : this_ExprOr_0= ruleExprOr ;
    public final EObject ruleExpr() throws RecognitionException {
        EObject current = null;

        EObject this_ExprOr_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2295:28: (this_ExprOr_0= ruleExprOr )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2297:5: this_ExprOr_0= ruleExprOr
            {
             
                    newCompositeNode(grammarAccess.getExprAccess().getExprOrParserRuleCall()); 
                
            pushFollow(FOLLOW_ruleExprOr_in_ruleExpr5097);
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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2313:1: entryRuleExprOr returns [EObject current=null] : iv_ruleExprOr= ruleExprOr EOF ;
    public final EObject entryRuleExprOr() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprOr = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2314:2: (iv_ruleExprOr= ruleExprOr EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2315:2: iv_ruleExprOr= ruleExprOr EOF
            {
             newCompositeNode(grammarAccess.getExprOrRule()); 
            pushFollow(FOLLOW_ruleExprOr_in_entryRuleExprOr5131);
            iv_ruleExprOr=ruleExprOr();

            state._fsp--;

             current =iv_ruleExprOr; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprOr5141); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2322:1: ruleExprOr returns [EObject current=null] : (this_ExprAnd_0= ruleExprAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )* ) ;
    public final EObject ruleExprOr() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_ExprAnd_0 = null;

        EObject lv_right_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2325:28: ( (this_ExprAnd_0= ruleExprAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2326:1: (this_ExprAnd_0= ruleExprAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2326:1: (this_ExprAnd_0= ruleExprAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2327:5: this_ExprAnd_0= ruleExprAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )*
            {
             
                    newCompositeNode(grammarAccess.getExprOrAccess().getExprAndParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleExprAnd_in_ruleExprOr5188);
            this_ExprAnd_0=ruleExprAnd();

            state._fsp--;

             
                    current = this_ExprAnd_0; 
                    afterParserOrEnumRuleCall();
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2335:1: ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )*
            loop48:
            do {
                int alt48=2;
                int LA48_0 = input.LA(1);

                if ( (LA48_0==58) ) {
                    alt48=1;
                }


                switch (alt48) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2335:2: () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2335:2: ()
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2336:5: 
            	    {

            	            current = forceCreateModelElementAndSet(
            	                grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0(),
            	                current);
            	        

            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2341:2: ( (lv_op_2_0= 'or' ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2342:1: (lv_op_2_0= 'or' )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2342:1: (lv_op_2_0= 'or' )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2343:3: lv_op_2_0= 'or'
            	    {
            	    lv_op_2_0=(Token)match(input,58,FOLLOW_58_in_ruleExprOr5215); 

            	            newLeafNode(lv_op_2_0, grammarAccess.getExprOrAccess().getOpOrKeyword_1_1_0());
            	        

            	    	        if (current==null) {
            	    	            current = createModelElement(grammarAccess.getExprOrRule());
            	    	        }
            	           		setWithLastConsumed(current, "op", lv_op_2_0, "or");
            	    	    

            	    }


            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2356:2: ( (lv_right_3_0= ruleExprAnd ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2357:1: (lv_right_3_0= ruleExprAnd )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2357:1: (lv_right_3_0= ruleExprAnd )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2358:3: lv_right_3_0= ruleExprAnd
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprOrAccess().getRightExprAndParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprAnd_in_ruleExprOr5249);
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
    // $ANTLR end "ruleExprOr"


    // $ANTLR start "entryRuleExprAnd"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2382:1: entryRuleExprAnd returns [EObject current=null] : iv_ruleExprAnd= ruleExprAnd EOF ;
    public final EObject entryRuleExprAnd() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprAnd = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2383:2: (iv_ruleExprAnd= ruleExprAnd EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2384:2: iv_ruleExprAnd= ruleExprAnd EOF
            {
             newCompositeNode(grammarAccess.getExprAndRule()); 
            pushFollow(FOLLOW_ruleExprAnd_in_entryRuleExprAnd5287);
            iv_ruleExprAnd=ruleExprAnd();

            state._fsp--;

             current =iv_ruleExprAnd; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprAnd5297); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2391:1: ruleExprAnd returns [EObject current=null] : (this_ExprEquality_0= ruleExprEquality ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )* ) ;
    public final EObject ruleExprAnd() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_ExprEquality_0 = null;

        EObject lv_right_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2394:28: ( (this_ExprEquality_0= ruleExprEquality ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2395:1: (this_ExprEquality_0= ruleExprEquality ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2395:1: (this_ExprEquality_0= ruleExprEquality ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2396:5: this_ExprEquality_0= ruleExprEquality ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )*
            {
             
                    newCompositeNode(grammarAccess.getExprAndAccess().getExprEqualityParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleExprEquality_in_ruleExprAnd5344);
            this_ExprEquality_0=ruleExprEquality();

            state._fsp--;

             
                    current = this_ExprEquality_0; 
                    afterParserOrEnumRuleCall();
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2404:1: ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )*
            loop49:
            do {
                int alt49=2;
                int LA49_0 = input.LA(1);

                if ( (LA49_0==59) ) {
                    alt49=1;
                }


                switch (alt49) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2404:2: () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2404:2: ()
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2405:5: 
            	    {

            	            current = forceCreateModelElementAndSet(
            	                grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0(),
            	                current);
            	        

            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2410:2: ( (lv_op_2_0= 'and' ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2411:1: (lv_op_2_0= 'and' )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2411:1: (lv_op_2_0= 'and' )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2412:3: lv_op_2_0= 'and'
            	    {
            	    lv_op_2_0=(Token)match(input,59,FOLLOW_59_in_ruleExprAnd5371); 

            	            newLeafNode(lv_op_2_0, grammarAccess.getExprAndAccess().getOpAndKeyword_1_1_0());
            	        

            	    	        if (current==null) {
            	    	            current = createModelElement(grammarAccess.getExprAndRule());
            	    	        }
            	           		setWithLastConsumed(current, "op", lv_op_2_0, "and");
            	    	    

            	    }


            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2425:2: ( (lv_right_3_0= ruleExprEquality ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2426:1: (lv_right_3_0= ruleExprEquality )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2426:1: (lv_right_3_0= ruleExprEquality )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2427:3: lv_right_3_0= ruleExprEquality
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprAndAccess().getRightExprEqualityParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprEquality_in_ruleExprAnd5405);
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
            	    break loop49;
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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2451:1: entryRuleExprEquality returns [EObject current=null] : iv_ruleExprEquality= ruleExprEquality EOF ;
    public final EObject entryRuleExprEquality() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprEquality = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2452:2: (iv_ruleExprEquality= ruleExprEquality EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2453:2: iv_ruleExprEquality= ruleExprEquality EOF
            {
             newCompositeNode(grammarAccess.getExprEqualityRule()); 
            pushFollow(FOLLOW_ruleExprEquality_in_entryRuleExprEquality5443);
            iv_ruleExprEquality=ruleExprEquality();

            state._fsp--;

             current =iv_ruleExprEquality; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprEquality5453); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2460:1: ruleExprEquality returns [EObject current=null] : (this_ExprComparison_0= ruleExprComparison ( () ( (lv_op_2_0= ruleOpEquality ) ) ( (lv_right_3_0= ruleExprComparison ) ) )* ) ;
    public final EObject ruleExprEquality() throws RecognitionException {
        EObject current = null;

        EObject this_ExprComparison_0 = null;

        EObject lv_op_2_0 = null;

        EObject lv_right_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2463:28: ( (this_ExprComparison_0= ruleExprComparison ( () ( (lv_op_2_0= ruleOpEquality ) ) ( (lv_right_3_0= ruleExprComparison ) ) )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2464:1: (this_ExprComparison_0= ruleExprComparison ( () ( (lv_op_2_0= ruleOpEquality ) ) ( (lv_right_3_0= ruleExprComparison ) ) )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2464:1: (this_ExprComparison_0= ruleExprComparison ( () ( (lv_op_2_0= ruleOpEquality ) ) ( (lv_right_3_0= ruleExprComparison ) ) )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2465:5: this_ExprComparison_0= ruleExprComparison ( () ( (lv_op_2_0= ruleOpEquality ) ) ( (lv_right_3_0= ruleExprComparison ) ) )*
            {
             
                    newCompositeNode(grammarAccess.getExprEqualityAccess().getExprComparisonParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleExprComparison_in_ruleExprEquality5500);
            this_ExprComparison_0=ruleExprComparison();

            state._fsp--;

             
                    current = this_ExprComparison_0; 
                    afterParserOrEnumRuleCall();
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2473:1: ( () ( (lv_op_2_0= ruleOpEquality ) ) ( (lv_right_3_0= ruleExprComparison ) ) )*
            loop50:
            do {
                int alt50=2;
                int LA50_0 = input.LA(1);

                if ( ((LA50_0>=60 && LA50_0<=61)) ) {
                    alt50=1;
                }


                switch (alt50) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2473:2: () ( (lv_op_2_0= ruleOpEquality ) ) ( (lv_right_3_0= ruleExprComparison ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2473:2: ()
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2474:5: 
            	    {

            	            current = forceCreateModelElementAndSet(
            	                grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0(),
            	                current);
            	        

            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2479:2: ( (lv_op_2_0= ruleOpEquality ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2480:1: (lv_op_2_0= ruleOpEquality )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2480:1: (lv_op_2_0= ruleOpEquality )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2481:3: lv_op_2_0= ruleOpEquality
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprEqualityAccess().getOpOpEqualityParserRuleCall_1_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleOpEquality_in_ruleExprEquality5530);
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

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2497:2: ( (lv_right_3_0= ruleExprComparison ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2498:1: (lv_right_3_0= ruleExprComparison )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2498:1: (lv_right_3_0= ruleExprComparison )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2499:3: lv_right_3_0= ruleExprComparison
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprEqualityAccess().getRightExprComparisonParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprComparison_in_ruleExprEquality5551);
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
            	    break loop50;
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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2523:1: entryRuleOpEquality returns [EObject current=null] : iv_ruleOpEquality= ruleOpEquality EOF ;
    public final EObject entryRuleOpEquality() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOpEquality = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2524:2: (iv_ruleOpEquality= ruleOpEquality EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2525:2: iv_ruleOpEquality= ruleOpEquality EOF
            {
             newCompositeNode(grammarAccess.getOpEqualityRule()); 
            pushFollow(FOLLOW_ruleOpEquality_in_entryRuleOpEquality5589);
            iv_ruleOpEquality=ruleOpEquality();

            state._fsp--;

             current =iv_ruleOpEquality; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleOpEquality5599); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2532:1: ruleOpEquality returns [EObject current=null] : ( ( () otherlv_1= '==' ) | ( () otherlv_3= '!=' ) ) ;
    public final EObject ruleOpEquality() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2535:28: ( ( ( () otherlv_1= '==' ) | ( () otherlv_3= '!=' ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2536:1: ( ( () otherlv_1= '==' ) | ( () otherlv_3= '!=' ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2536:1: ( ( () otherlv_1= '==' ) | ( () otherlv_3= '!=' ) )
            int alt51=2;
            int LA51_0 = input.LA(1);

            if ( (LA51_0==60) ) {
                alt51=1;
            }
            else if ( (LA51_0==61) ) {
                alt51=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 51, 0, input);

                throw nvae;
            }
            switch (alt51) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2536:2: ( () otherlv_1= '==' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2536:2: ( () otherlv_1= '==' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2536:3: () otherlv_1= '=='
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2536:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2537:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpEqualityAccess().getOpEqualsAction_0_0(),
                                current);
                        

                    }

                    otherlv_1=(Token)match(input,60,FOLLOW_60_in_ruleOpEquality5646); 

                        	newLeafNode(otherlv_1, grammarAccess.getOpEqualityAccess().getEqualsSignEqualsSignKeyword_0_1());
                        

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2547:6: ( () otherlv_3= '!=' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2547:6: ( () otherlv_3= '!=' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2547:7: () otherlv_3= '!='
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2547:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2548:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpEqualityAccess().getOpUnequalsAction_1_0(),
                                current);
                        

                    }

                    otherlv_3=(Token)match(input,61,FOLLOW_61_in_ruleOpEquality5675); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2565:1: entryRuleExprComparison returns [EObject current=null] : iv_ruleExprComparison= ruleExprComparison EOF ;
    public final EObject entryRuleExprComparison() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprComparison = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2566:2: (iv_ruleExprComparison= ruleExprComparison EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2567:2: iv_ruleExprComparison= ruleExprComparison EOF
            {
             newCompositeNode(grammarAccess.getExprComparisonRule()); 
            pushFollow(FOLLOW_ruleExprComparison_in_entryRuleExprComparison5712);
            iv_ruleExprComparison=ruleExprComparison();

            state._fsp--;

             current =iv_ruleExprComparison; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprComparison5722); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2574:1: ruleExprComparison returns [EObject current=null] : (this_ExprAdditive_0= ruleExprAdditive ( () ( (lv_op_2_0= ruleOpComparison ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )* ) ;
    public final EObject ruleExprComparison() throws RecognitionException {
        EObject current = null;

        EObject this_ExprAdditive_0 = null;

        EObject lv_op_2_0 = null;

        EObject lv_right_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2577:28: ( (this_ExprAdditive_0= ruleExprAdditive ( () ( (lv_op_2_0= ruleOpComparison ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2578:1: (this_ExprAdditive_0= ruleExprAdditive ( () ( (lv_op_2_0= ruleOpComparison ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2578:1: (this_ExprAdditive_0= ruleExprAdditive ( () ( (lv_op_2_0= ruleOpComparison ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2579:5: this_ExprAdditive_0= ruleExprAdditive ( () ( (lv_op_2_0= ruleOpComparison ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )*
            {
             
                    newCompositeNode(grammarAccess.getExprComparisonAccess().getExprAdditiveParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleExprAdditive_in_ruleExprComparison5769);
            this_ExprAdditive_0=ruleExprAdditive();

            state._fsp--;

             
                    current = this_ExprAdditive_0; 
                    afterParserOrEnumRuleCall();
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2587:1: ( () ( (lv_op_2_0= ruleOpComparison ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )*
            loop52:
            do {
                int alt52=2;
                int LA52_0 = input.LA(1);

                if ( ((LA52_0>=62 && LA52_0<=65)) ) {
                    alt52=1;
                }


                switch (alt52) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2587:2: () ( (lv_op_2_0= ruleOpComparison ) ) ( (lv_right_3_0= ruleExprAdditive ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2587:2: ()
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2588:5: 
            	    {

            	            current = forceCreateModelElementAndSet(
            	                grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0(),
            	                current);
            	        

            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2593:2: ( (lv_op_2_0= ruleOpComparison ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2594:1: (lv_op_2_0= ruleOpComparison )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2594:1: (lv_op_2_0= ruleOpComparison )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2595:3: lv_op_2_0= ruleOpComparison
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprComparisonAccess().getOpOpComparisonParserRuleCall_1_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleOpComparison_in_ruleExprComparison5799);
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

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2611:2: ( (lv_right_3_0= ruleExprAdditive ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2612:1: (lv_right_3_0= ruleExprAdditive )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2612:1: (lv_right_3_0= ruleExprAdditive )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2613:3: lv_right_3_0= ruleExprAdditive
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprComparisonAccess().getRightExprAdditiveParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprAdditive_in_ruleExprComparison5820);
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
            	    break loop52;
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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2637:1: entryRuleOpComparison returns [EObject current=null] : iv_ruleOpComparison= ruleOpComparison EOF ;
    public final EObject entryRuleOpComparison() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOpComparison = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2638:2: (iv_ruleOpComparison= ruleOpComparison EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2639:2: iv_ruleOpComparison= ruleOpComparison EOF
            {
             newCompositeNode(grammarAccess.getOpComparisonRule()); 
            pushFollow(FOLLOW_ruleOpComparison_in_entryRuleOpComparison5858);
            iv_ruleOpComparison=ruleOpComparison();

            state._fsp--;

             current =iv_ruleOpComparison; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleOpComparison5868); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2646:1: ruleOpComparison returns [EObject current=null] : ( ( () otherlv_1= '<=' ) | ( () otherlv_3= '<' ) | ( () otherlv_5= '>=' ) | ( () otherlv_7= '>' ) ) ;
    public final EObject ruleOpComparison() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_7=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2649:28: ( ( ( () otherlv_1= '<=' ) | ( () otherlv_3= '<' ) | ( () otherlv_5= '>=' ) | ( () otherlv_7= '>' ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2650:1: ( ( () otherlv_1= '<=' ) | ( () otherlv_3= '<' ) | ( () otherlv_5= '>=' ) | ( () otherlv_7= '>' ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2650:1: ( ( () otherlv_1= '<=' ) | ( () otherlv_3= '<' ) | ( () otherlv_5= '>=' ) | ( () otherlv_7= '>' ) )
            int alt53=4;
            switch ( input.LA(1) ) {
            case 62:
                {
                alt53=1;
                }
                break;
            case 63:
                {
                alt53=2;
                }
                break;
            case 64:
                {
                alt53=3;
                }
                break;
            case 65:
                {
                alt53=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 53, 0, input);

                throw nvae;
            }

            switch (alt53) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2650:2: ( () otherlv_1= '<=' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2650:2: ( () otherlv_1= '<=' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2650:3: () otherlv_1= '<='
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2650:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2651:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpComparisonAccess().getOpLessEqAction_0_0(),
                                current);
                        

                    }

                    otherlv_1=(Token)match(input,62,FOLLOW_62_in_ruleOpComparison5915); 

                        	newLeafNode(otherlv_1, grammarAccess.getOpComparisonAccess().getLessThanSignEqualsSignKeyword_0_1());
                        

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2661:6: ( () otherlv_3= '<' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2661:6: ( () otherlv_3= '<' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2661:7: () otherlv_3= '<'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2661:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2662:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpComparisonAccess().getOpLessAction_1_0(),
                                current);
                        

                    }

                    otherlv_3=(Token)match(input,63,FOLLOW_63_in_ruleOpComparison5944); 

                        	newLeafNode(otherlv_3, grammarAccess.getOpComparisonAccess().getLessThanSignKeyword_1_1());
                        

                    }


                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2672:6: ( () otherlv_5= '>=' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2672:6: ( () otherlv_5= '>=' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2672:7: () otherlv_5= '>='
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2672:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2673:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpComparisonAccess().getOpGreaterEqAction_2_0(),
                                current);
                        

                    }

                    otherlv_5=(Token)match(input,64,FOLLOW_64_in_ruleOpComparison5973); 

                        	newLeafNode(otherlv_5, grammarAccess.getOpComparisonAccess().getGreaterThanSignEqualsSignKeyword_2_1());
                        

                    }


                    }
                    break;
                case 4 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2683:6: ( () otherlv_7= '>' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2683:6: ( () otherlv_7= '>' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2683:7: () otherlv_7= '>'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2683:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2684:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpComparisonAccess().getOpGreaterAction_3_0(),
                                current);
                        

                    }

                    otherlv_7=(Token)match(input,65,FOLLOW_65_in_ruleOpComparison6002); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2701:1: entryRuleExprAdditive returns [EObject current=null] : iv_ruleExprAdditive= ruleExprAdditive EOF ;
    public final EObject entryRuleExprAdditive() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprAdditive = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2702:2: (iv_ruleExprAdditive= ruleExprAdditive EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2703:2: iv_ruleExprAdditive= ruleExprAdditive EOF
            {
             newCompositeNode(grammarAccess.getExprAdditiveRule()); 
            pushFollow(FOLLOW_ruleExprAdditive_in_entryRuleExprAdditive6039);
            iv_ruleExprAdditive=ruleExprAdditive();

            state._fsp--;

             current =iv_ruleExprAdditive; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprAdditive6049); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2710:1: ruleExprAdditive returns [EObject current=null] : (this_ExprMult_0= ruleExprMult ( () ( (lv_op_2_0= ruleOpAdditive ) ) ( (lv_right_3_0= ruleExprMult ) ) )* ) ;
    public final EObject ruleExprAdditive() throws RecognitionException {
        EObject current = null;

        EObject this_ExprMult_0 = null;

        EObject lv_op_2_0 = null;

        EObject lv_right_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2713:28: ( (this_ExprMult_0= ruleExprMult ( () ( (lv_op_2_0= ruleOpAdditive ) ) ( (lv_right_3_0= ruleExprMult ) ) )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2714:1: (this_ExprMult_0= ruleExprMult ( () ( (lv_op_2_0= ruleOpAdditive ) ) ( (lv_right_3_0= ruleExprMult ) ) )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2714:1: (this_ExprMult_0= ruleExprMult ( () ( (lv_op_2_0= ruleOpAdditive ) ) ( (lv_right_3_0= ruleExprMult ) ) )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2715:5: this_ExprMult_0= ruleExprMult ( () ( (lv_op_2_0= ruleOpAdditive ) ) ( (lv_right_3_0= ruleExprMult ) ) )*
            {
             
                    newCompositeNode(grammarAccess.getExprAdditiveAccess().getExprMultParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleExprMult_in_ruleExprAdditive6096);
            this_ExprMult_0=ruleExprMult();

            state._fsp--;

             
                    current = this_ExprMult_0; 
                    afterParserOrEnumRuleCall();
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2723:1: ( () ( (lv_op_2_0= ruleOpAdditive ) ) ( (lv_right_3_0= ruleExprMult ) ) )*
            loop54:
            do {
                int alt54=2;
                int LA54_0 = input.LA(1);

                if ( ((LA54_0>=66 && LA54_0<=67)) ) {
                    alt54=1;
                }


                switch (alt54) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2723:2: () ( (lv_op_2_0= ruleOpAdditive ) ) ( (lv_right_3_0= ruleExprMult ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2723:2: ()
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2724:5: 
            	    {

            	            current = forceCreateModelElementAndSet(
            	                grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0(),
            	                current);
            	        

            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2729:2: ( (lv_op_2_0= ruleOpAdditive ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2730:1: (lv_op_2_0= ruleOpAdditive )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2730:1: (lv_op_2_0= ruleOpAdditive )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2731:3: lv_op_2_0= ruleOpAdditive
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprAdditiveAccess().getOpOpAdditiveParserRuleCall_1_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleOpAdditive_in_ruleExprAdditive6126);
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

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2747:2: ( (lv_right_3_0= ruleExprMult ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2748:1: (lv_right_3_0= ruleExprMult )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2748:1: (lv_right_3_0= ruleExprMult )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2749:3: lv_right_3_0= ruleExprMult
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprAdditiveAccess().getRightExprMultParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprMult_in_ruleExprAdditive6147);
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
            	    break loop54;
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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2773:1: entryRuleOpAdditive returns [EObject current=null] : iv_ruleOpAdditive= ruleOpAdditive EOF ;
    public final EObject entryRuleOpAdditive() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOpAdditive = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2774:2: (iv_ruleOpAdditive= ruleOpAdditive EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2775:2: iv_ruleOpAdditive= ruleOpAdditive EOF
            {
             newCompositeNode(grammarAccess.getOpAdditiveRule()); 
            pushFollow(FOLLOW_ruleOpAdditive_in_entryRuleOpAdditive6185);
            iv_ruleOpAdditive=ruleOpAdditive();

            state._fsp--;

             current =iv_ruleOpAdditive; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleOpAdditive6195); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2782:1: ruleOpAdditive returns [EObject current=null] : ( ( () otherlv_1= '+' ) | ( () otherlv_3= '-' ) ) ;
    public final EObject ruleOpAdditive() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2785:28: ( ( ( () otherlv_1= '+' ) | ( () otherlv_3= '-' ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2786:1: ( ( () otherlv_1= '+' ) | ( () otherlv_3= '-' ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2786:1: ( ( () otherlv_1= '+' ) | ( () otherlv_3= '-' ) )
            int alt55=2;
            int LA55_0 = input.LA(1);

            if ( (LA55_0==66) ) {
                alt55=1;
            }
            else if ( (LA55_0==67) ) {
                alt55=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 55, 0, input);

                throw nvae;
            }
            switch (alt55) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2786:2: ( () otherlv_1= '+' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2786:2: ( () otherlv_1= '+' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2786:3: () otherlv_1= '+'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2786:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2787:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpAdditiveAccess().getOpPlusAction_0_0(),
                                current);
                        

                    }

                    otherlv_1=(Token)match(input,66,FOLLOW_66_in_ruleOpAdditive6242); 

                        	newLeafNode(otherlv_1, grammarAccess.getOpAdditiveAccess().getPlusSignKeyword_0_1());
                        

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2797:6: ( () otherlv_3= '-' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2797:6: ( () otherlv_3= '-' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2797:7: () otherlv_3= '-'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2797:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2798:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpAdditiveAccess().getOpMinusAction_1_0(),
                                current);
                        

                    }

                    otherlv_3=(Token)match(input,67,FOLLOW_67_in_ruleOpAdditive6271); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2815:1: entryRuleExprMult returns [EObject current=null] : iv_ruleExprMult= ruleExprMult EOF ;
    public final EObject entryRuleExprMult() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprMult = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2816:2: (iv_ruleExprMult= ruleExprMult EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2817:2: iv_ruleExprMult= ruleExprMult EOF
            {
             newCompositeNode(grammarAccess.getExprMultRule()); 
            pushFollow(FOLLOW_ruleExprMult_in_entryRuleExprMult6308);
            iv_ruleExprMult=ruleExprMult();

            state._fsp--;

             current =iv_ruleExprMult; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprMult6318); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2824:1: ruleExprMult returns [EObject current=null] : (this_ExprSign_0= ruleExprSign ( () ( (lv_op_2_0= ruleOpMultiplicative ) ) ( (lv_right_3_0= ruleExprSign ) ) )* ) ;
    public final EObject ruleExprMult() throws RecognitionException {
        EObject current = null;

        EObject this_ExprSign_0 = null;

        EObject lv_op_2_0 = null;

        EObject lv_right_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2827:28: ( (this_ExprSign_0= ruleExprSign ( () ( (lv_op_2_0= ruleOpMultiplicative ) ) ( (lv_right_3_0= ruleExprSign ) ) )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2828:1: (this_ExprSign_0= ruleExprSign ( () ( (lv_op_2_0= ruleOpMultiplicative ) ) ( (lv_right_3_0= ruleExprSign ) ) )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2828:1: (this_ExprSign_0= ruleExprSign ( () ( (lv_op_2_0= ruleOpMultiplicative ) ) ( (lv_right_3_0= ruleExprSign ) ) )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2829:5: this_ExprSign_0= ruleExprSign ( () ( (lv_op_2_0= ruleOpMultiplicative ) ) ( (lv_right_3_0= ruleExprSign ) ) )*
            {
             
                    newCompositeNode(grammarAccess.getExprMultAccess().getExprSignParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleExprSign_in_ruleExprMult6365);
            this_ExprSign_0=ruleExprSign();

            state._fsp--;

             
                    current = this_ExprSign_0; 
                    afterParserOrEnumRuleCall();
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2837:1: ( () ( (lv_op_2_0= ruleOpMultiplicative ) ) ( (lv_right_3_0= ruleExprSign ) ) )*
            loop56:
            do {
                int alt56=2;
                int LA56_0 = input.LA(1);

                if ( (LA56_0==16||(LA56_0>=68 && LA56_0<=71)) ) {
                    alt56=1;
                }


                switch (alt56) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2837:2: () ( (lv_op_2_0= ruleOpMultiplicative ) ) ( (lv_right_3_0= ruleExprSign ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2837:2: ()
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2838:5: 
            	    {

            	            current = forceCreateModelElementAndSet(
            	                grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0(),
            	                current);
            	        

            	    }

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2843:2: ( (lv_op_2_0= ruleOpMultiplicative ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2844:1: (lv_op_2_0= ruleOpMultiplicative )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2844:1: (lv_op_2_0= ruleOpMultiplicative )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2845:3: lv_op_2_0= ruleOpMultiplicative
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprMultAccess().getOpOpMultiplicativeParserRuleCall_1_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleOpMultiplicative_in_ruleExprMult6395);
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

            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2861:2: ( (lv_right_3_0= ruleExprSign ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2862:1: (lv_right_3_0= ruleExprSign )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2862:1: (lv_right_3_0= ruleExprSign )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2863:3: lv_right_3_0= ruleExprSign
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprMultAccess().getRightExprSignParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprSign_in_ruleExprMult6416);
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
            	    break loop56;
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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2887:1: entryRuleOpMultiplicative returns [EObject current=null] : iv_ruleOpMultiplicative= ruleOpMultiplicative EOF ;
    public final EObject entryRuleOpMultiplicative() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOpMultiplicative = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2888:2: (iv_ruleOpMultiplicative= ruleOpMultiplicative EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2889:2: iv_ruleOpMultiplicative= ruleOpMultiplicative EOF
            {
             newCompositeNode(grammarAccess.getOpMultiplicativeRule()); 
            pushFollow(FOLLOW_ruleOpMultiplicative_in_entryRuleOpMultiplicative6454);
            iv_ruleOpMultiplicative=ruleOpMultiplicative();

            state._fsp--;

             current =iv_ruleOpMultiplicative; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleOpMultiplicative6464); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2896:1: ruleOpMultiplicative returns [EObject current=null] : ( ( () otherlv_1= '*' ) | ( () otherlv_3= '/' ) | ( () otherlv_5= '%' ) | ( () otherlv_7= 'mod' ) | ( () otherlv_9= 'div' ) ) ;
    public final EObject ruleOpMultiplicative() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_7=null;
        Token otherlv_9=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2899:28: ( ( ( () otherlv_1= '*' ) | ( () otherlv_3= '/' ) | ( () otherlv_5= '%' ) | ( () otherlv_7= 'mod' ) | ( () otherlv_9= 'div' ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2900:1: ( ( () otherlv_1= '*' ) | ( () otherlv_3= '/' ) | ( () otherlv_5= '%' ) | ( () otherlv_7= 'mod' ) | ( () otherlv_9= 'div' ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2900:1: ( ( () otherlv_1= '*' ) | ( () otherlv_3= '/' ) | ( () otherlv_5= '%' ) | ( () otherlv_7= 'mod' ) | ( () otherlv_9= 'div' ) )
            int alt57=5;
            switch ( input.LA(1) ) {
            case 16:
                {
                alt57=1;
                }
                break;
            case 68:
                {
                alt57=2;
                }
                break;
            case 69:
                {
                alt57=3;
                }
                break;
            case 70:
                {
                alt57=4;
                }
                break;
            case 71:
                {
                alt57=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 57, 0, input);

                throw nvae;
            }

            switch (alt57) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2900:2: ( () otherlv_1= '*' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2900:2: ( () otherlv_1= '*' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2900:3: () otherlv_1= '*'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2900:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2901:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpMultiplicativeAccess().getOpMultAction_0_0(),
                                current);
                        

                    }

                    otherlv_1=(Token)match(input,16,FOLLOW_16_in_ruleOpMultiplicative6511); 

                        	newLeafNode(otherlv_1, grammarAccess.getOpMultiplicativeAccess().getAsteriskKeyword_0_1());
                        

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2911:6: ( () otherlv_3= '/' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2911:6: ( () otherlv_3= '/' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2911:7: () otherlv_3= '/'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2911:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2912:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpMultiplicativeAccess().getOpDivRealAction_1_0(),
                                current);
                        

                    }

                    otherlv_3=(Token)match(input,68,FOLLOW_68_in_ruleOpMultiplicative6540); 

                        	newLeafNode(otherlv_3, grammarAccess.getOpMultiplicativeAccess().getSolidusKeyword_1_1());
                        

                    }


                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2922:6: ( () otherlv_5= '%' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2922:6: ( () otherlv_5= '%' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2922:7: () otherlv_5= '%'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2922:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2923:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpMultiplicativeAccess().getOpModRealAction_2_0(),
                                current);
                        

                    }

                    otherlv_5=(Token)match(input,69,FOLLOW_69_in_ruleOpMultiplicative6569); 

                        	newLeafNode(otherlv_5, grammarAccess.getOpMultiplicativeAccess().getPercentSignKeyword_2_1());
                        

                    }


                    }
                    break;
                case 4 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2933:6: ( () otherlv_7= 'mod' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2933:6: ( () otherlv_7= 'mod' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2933:7: () otherlv_7= 'mod'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2933:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2934:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpMultiplicativeAccess().getOpModIntAction_3_0(),
                                current);
                        

                    }

                    otherlv_7=(Token)match(input,70,FOLLOW_70_in_ruleOpMultiplicative6598); 

                        	newLeafNode(otherlv_7, grammarAccess.getOpMultiplicativeAccess().getModKeyword_3_1());
                        

                    }


                    }
                    break;
                case 5 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2944:6: ( () otherlv_9= 'div' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2944:6: ( () otherlv_9= 'div' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2944:7: () otherlv_9= 'div'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2944:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2945:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getOpMultiplicativeAccess().getOpModRealAction_4_0(),
                                current);
                        

                    }

                    otherlv_9=(Token)match(input,71,FOLLOW_71_in_ruleOpMultiplicative6627); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2962:1: entryRuleExprSign returns [EObject current=null] : iv_ruleExprSign= ruleExprSign EOF ;
    public final EObject entryRuleExprSign() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprSign = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2963:2: (iv_ruleExprSign= ruleExprSign EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2964:2: iv_ruleExprSign= ruleExprSign EOF
            {
             newCompositeNode(grammarAccess.getExprSignRule()); 
            pushFollow(FOLLOW_ruleExprSign_in_entryRuleExprSign6664);
            iv_ruleExprSign=ruleExprSign();

            state._fsp--;

             current =iv_ruleExprSign; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprSign6674); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2971:1: ruleExprSign returns [EObject current=null] : ( ( () ( (lv_op_1_0= ruleOpAdditive ) ) ( (lv_right_2_0= ruleExprNot ) ) ) | this_ExprNot_3= ruleExprNot ) ;
    public final EObject ruleExprSign() throws RecognitionException {
        EObject current = null;

        EObject lv_op_1_0 = null;

        EObject lv_right_2_0 = null;

        EObject this_ExprNot_3 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2974:28: ( ( ( () ( (lv_op_1_0= ruleOpAdditive ) ) ( (lv_right_2_0= ruleExprNot ) ) ) | this_ExprNot_3= ruleExprNot ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2975:1: ( ( () ( (lv_op_1_0= ruleOpAdditive ) ) ( (lv_right_2_0= ruleExprNot ) ) ) | this_ExprNot_3= ruleExprNot )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2975:1: ( ( () ( (lv_op_1_0= ruleOpAdditive ) ) ( (lv_right_2_0= ruleExprNot ) ) ) | this_ExprNot_3= ruleExprNot )
            int alt58=2;
            int LA58_0 = input.LA(1);

            if ( ((LA58_0>=66 && LA58_0<=67)) ) {
                alt58=1;
            }
            else if ( ((LA58_0>=RULE_ID && LA58_0<=RULE_STRING)||LA58_0==21||LA58_0==40||(LA58_0>=72 && LA58_0<=74)) ) {
                alt58=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 58, 0, input);

                throw nvae;
            }
            switch (alt58) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2975:2: ( () ( (lv_op_1_0= ruleOpAdditive ) ) ( (lv_right_2_0= ruleExprNot ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2975:2: ( () ( (lv_op_1_0= ruleOpAdditive ) ) ( (lv_right_2_0= ruleExprNot ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2975:3: () ( (lv_op_1_0= ruleOpAdditive ) ) ( (lv_right_2_0= ruleExprNot ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2975:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2976:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprSignAccess().getExprSignAction_0_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2981:2: ( (lv_op_1_0= ruleOpAdditive ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2982:1: (lv_op_1_0= ruleOpAdditive )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2982:1: (lv_op_1_0= ruleOpAdditive )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2983:3: lv_op_1_0= ruleOpAdditive
                    {
                     
                    	        newCompositeNode(grammarAccess.getExprSignAccess().getOpOpAdditiveParserRuleCall_0_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleOpAdditive_in_ruleExprSign6730);
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

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2999:2: ( (lv_right_2_0= ruleExprNot ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3000:1: (lv_right_2_0= ruleExprNot )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3000:1: (lv_right_2_0= ruleExprNot )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3001:3: lv_right_2_0= ruleExprNot
                    {
                     
                    	        newCompositeNode(grammarAccess.getExprSignAccess().getRightExprNotParserRuleCall_0_2_0()); 
                    	    
                    pushFollow(FOLLOW_ruleExprNot_in_ruleExprSign6751);
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
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3019:5: this_ExprNot_3= ruleExprNot
                    {
                     
                            newCompositeNode(grammarAccess.getExprSignAccess().getExprNotParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleExprNot_in_ruleExprSign6780);
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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3035:1: entryRuleExprNot returns [EObject current=null] : iv_ruleExprNot= ruleExprNot EOF ;
    public final EObject entryRuleExprNot() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprNot = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3036:2: (iv_ruleExprNot= ruleExprNot EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3037:2: iv_ruleExprNot= ruleExprNot EOF
            {
             newCompositeNode(grammarAccess.getExprNotRule()); 
            pushFollow(FOLLOW_ruleExprNot_in_entryRuleExprNot6815);
            iv_ruleExprNot=ruleExprNot();

            state._fsp--;

             current =iv_ruleExprNot; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprNot6825); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3044:1: ruleExprNot returns [EObject current=null] : ( ( () otherlv_1= 'not' ( (lv_right_2_0= ruleExprMember ) ) ) | this_ExprMember_3= ruleExprMember ) ;
    public final EObject ruleExprNot() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        EObject lv_right_2_0 = null;

        EObject this_ExprMember_3 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3047:28: ( ( ( () otherlv_1= 'not' ( (lv_right_2_0= ruleExprMember ) ) ) | this_ExprMember_3= ruleExprMember ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3048:1: ( ( () otherlv_1= 'not' ( (lv_right_2_0= ruleExprMember ) ) ) | this_ExprMember_3= ruleExprMember )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3048:1: ( ( () otherlv_1= 'not' ( (lv_right_2_0= ruleExprMember ) ) ) | this_ExprMember_3= ruleExprMember )
            int alt59=2;
            int LA59_0 = input.LA(1);

            if ( (LA59_0==72) ) {
                alt59=1;
            }
            else if ( ((LA59_0>=RULE_ID && LA59_0<=RULE_STRING)||LA59_0==21||LA59_0==40||(LA59_0>=73 && LA59_0<=74)) ) {
                alt59=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 59, 0, input);

                throw nvae;
            }
            switch (alt59) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3048:2: ( () otherlv_1= 'not' ( (lv_right_2_0= ruleExprMember ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3048:2: ( () otherlv_1= 'not' ( (lv_right_2_0= ruleExprMember ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3048:3: () otherlv_1= 'not' ( (lv_right_2_0= ruleExprMember ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3048:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3049:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprNotAccess().getExprNotAction_0_0(),
                                current);
                        

                    }

                    otherlv_1=(Token)match(input,72,FOLLOW_72_in_ruleExprNot6872); 

                        	newLeafNode(otherlv_1, grammarAccess.getExprNotAccess().getNotKeyword_0_1());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3058:1: ( (lv_right_2_0= ruleExprMember ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3059:1: (lv_right_2_0= ruleExprMember )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3059:1: (lv_right_2_0= ruleExprMember )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3060:3: lv_right_2_0= ruleExprMember
                    {
                     
                    	        newCompositeNode(grammarAccess.getExprNotAccess().getRightExprMemberParserRuleCall_0_2_0()); 
                    	    
                    pushFollow(FOLLOW_ruleExprMember_in_ruleExprNot6893);
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
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3078:5: this_ExprMember_3= ruleExprMember
                    {
                     
                            newCompositeNode(grammarAccess.getExprNotAccess().getExprMemberParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleExprMember_in_ruleExprNot6922);
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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3094:1: entryRuleExprMember returns [EObject current=null] : iv_ruleExprMember= ruleExprMember EOF ;
    public final EObject entryRuleExprMember() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprMember = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3095:2: (iv_ruleExprMember= ruleExprMember EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3096:2: iv_ruleExprMember= ruleExprMember EOF
            {
             newCompositeNode(grammarAccess.getExprMemberRule()); 
            pushFollow(FOLLOW_ruleExprMember_in_entryRuleExprMember6957);
            iv_ruleExprMember=ruleExprMember();

            state._fsp--;

             current =iv_ruleExprMember; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprMember6967); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3103:1: ruleExprMember returns [EObject current=null] : (this_ExprSingle_0= ruleExprSingle ( () otherlv_2= '.' ( (lv_right_3_0= ruleExprAtomic ) ) )* ) ;
    public final EObject ruleExprMember() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        EObject this_ExprSingle_0 = null;

        EObject lv_right_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3106:28: ( (this_ExprSingle_0= ruleExprSingle ( () otherlv_2= '.' ( (lv_right_3_0= ruleExprAtomic ) ) )* ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3107:1: (this_ExprSingle_0= ruleExprSingle ( () otherlv_2= '.' ( (lv_right_3_0= ruleExprAtomic ) ) )* )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3107:1: (this_ExprSingle_0= ruleExprSingle ( () otherlv_2= '.' ( (lv_right_3_0= ruleExprAtomic ) ) )* )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3108:5: this_ExprSingle_0= ruleExprSingle ( () otherlv_2= '.' ( (lv_right_3_0= ruleExprAtomic ) ) )*
            {
             
                    newCompositeNode(grammarAccess.getExprMemberAccess().getExprSingleParserRuleCall_0()); 
                
            pushFollow(FOLLOW_ruleExprSingle_in_ruleExprMember7014);
            this_ExprSingle_0=ruleExprSingle();

            state._fsp--;

             
                    current = this_ExprSingle_0; 
                    afterParserOrEnumRuleCall();
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3116:1: ( () otherlv_2= '.' ( (lv_right_3_0= ruleExprAtomic ) ) )*
            loop60:
            do {
                int alt60=2;
                int LA60_0 = input.LA(1);

                if ( (LA60_0==15) ) {
                    alt60=1;
                }


                switch (alt60) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3116:2: () otherlv_2= '.' ( (lv_right_3_0= ruleExprAtomic ) )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3116:2: ()
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3117:5: 
            	    {

            	            current = forceCreateModelElementAndSet(
            	                grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0(),
            	                current);
            	        

            	    }

            	    otherlv_2=(Token)match(input,15,FOLLOW_15_in_ruleExprMember7035); 

            	        	newLeafNode(otherlv_2, grammarAccess.getExprMemberAccess().getFullStopKeyword_1_1());
            	        
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3126:1: ( (lv_right_3_0= ruleExprAtomic ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3127:1: (lv_right_3_0= ruleExprAtomic )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3127:1: (lv_right_3_0= ruleExprAtomic )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3128:3: lv_right_3_0= ruleExprAtomic
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprMemberAccess().getRightExprAtomicParserRuleCall_1_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprAtomic_in_ruleExprMember7056);
            	    lv_right_3_0=ruleExprAtomic();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getExprMemberRule());
            	    	        }
            	           		set(
            	           			current, 
            	           			"right",
            	            		lv_right_3_0, 
            	            		"ExprAtomic");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop60;
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


    // $ANTLR start "entryRuleExprSingle"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3152:1: entryRuleExprSingle returns [EObject current=null] : iv_ruleExprSingle= ruleExprSingle EOF ;
    public final EObject entryRuleExprSingle() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprSingle = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3153:2: (iv_ruleExprSingle= ruleExprSingle EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3154:2: iv_ruleExprSingle= ruleExprSingle EOF
            {
             newCompositeNode(grammarAccess.getExprSingleRule()); 
            pushFollow(FOLLOW_ruleExprSingle_in_entryRuleExprSingle7094);
            iv_ruleExprSingle=ruleExprSingle();

            state._fsp--;

             current =iv_ruleExprSingle; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprSingle7104); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3161:1: ruleExprSingle returns [EObject current=null] : (this_ExprAtomic_0= ruleExprAtomic | (otherlv_1= '(' this_Expr_2= ruleExpr otherlv_3= ')' ) | ( () ( (lv_intVal_5_0= RULE_INT ) ) ) | ( () ( (lv_numVal_7_0= ruleNumber ) ) ) | ( () ( (lv_strVal_9_0= RULE_STRING ) ) ) | ( () ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) ) ) ) ;
    public final EObject ruleExprSingle() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        Token lv_intVal_5_0=null;
        Token lv_strVal_9_0=null;
        Token lv_boolVal_11_1=null;
        Token lv_boolVal_11_2=null;
        EObject this_ExprAtomic_0 = null;

        EObject this_Expr_2 = null;

        AntlrDatatypeRuleToken lv_numVal_7_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3164:28: ( (this_ExprAtomic_0= ruleExprAtomic | (otherlv_1= '(' this_Expr_2= ruleExpr otherlv_3= ')' ) | ( () ( (lv_intVal_5_0= RULE_INT ) ) ) | ( () ( (lv_numVal_7_0= ruleNumber ) ) ) | ( () ( (lv_strVal_9_0= RULE_STRING ) ) ) | ( () ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) ) ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3165:1: (this_ExprAtomic_0= ruleExprAtomic | (otherlv_1= '(' this_Expr_2= ruleExpr otherlv_3= ')' ) | ( () ( (lv_intVal_5_0= RULE_INT ) ) ) | ( () ( (lv_numVal_7_0= ruleNumber ) ) ) | ( () ( (lv_strVal_9_0= RULE_STRING ) ) ) | ( () ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) ) ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3165:1: (this_ExprAtomic_0= ruleExprAtomic | (otherlv_1= '(' this_Expr_2= ruleExpr otherlv_3= ')' ) | ( () ( (lv_intVal_5_0= RULE_INT ) ) ) | ( () ( (lv_numVal_7_0= ruleNumber ) ) ) | ( () ( (lv_strVal_9_0= RULE_STRING ) ) ) | ( () ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) ) ) )
            int alt62=6;
            switch ( input.LA(1) ) {
            case RULE_ID:
            case 40:
                {
                alt62=1;
                }
                break;
            case 21:
                {
                alt62=2;
                }
                break;
            case RULE_INT:
                {
                int LA62_3 = input.LA(2);

                if ( (LA62_3==15) ) {
                    int LA62_6 = input.LA(3);

                    if ( (LA62_6==RULE_INT) ) {
                        alt62=4;
                    }
                    else if ( (LA62_6==RULE_ID||LA62_6==40) ) {
                        alt62=3;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 62, 6, input);

                        throw nvae;
                    }
                }
                else if ( (LA62_3==EOF||LA62_3==RULE_NL||LA62_3==16||(LA62_3>=22 && LA62_3<=23)||LA62_3==32||LA62_3==48||(LA62_3>=56 && LA62_3<=71)) ) {
                    alt62=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 62, 3, input);

                    throw nvae;
                }
                }
                break;
            case RULE_STRING:
                {
                alt62=5;
                }
                break;
            case 73:
            case 74:
                {
                alt62=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 62, 0, input);

                throw nvae;
            }

            switch (alt62) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3166:5: this_ExprAtomic_0= ruleExprAtomic
                    {
                     
                            newCompositeNode(grammarAccess.getExprSingleAccess().getExprAtomicParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleExprAtomic_in_ruleExprSingle7151);
                    this_ExprAtomic_0=ruleExprAtomic();

                    state._fsp--;

                     
                            current = this_ExprAtomic_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3175:6: (otherlv_1= '(' this_Expr_2= ruleExpr otherlv_3= ')' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3175:6: (otherlv_1= '(' this_Expr_2= ruleExpr otherlv_3= ')' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3175:8: otherlv_1= '(' this_Expr_2= ruleExpr otherlv_3= ')'
                    {
                    otherlv_1=(Token)match(input,21,FOLLOW_21_in_ruleExprSingle7169); 

                        	newLeafNode(otherlv_1, grammarAccess.getExprSingleAccess().getLeftParenthesisKeyword_1_0());
                        
                     
                            newCompositeNode(grammarAccess.getExprSingleAccess().getExprParserRuleCall_1_1()); 
                        
                    pushFollow(FOLLOW_ruleExpr_in_ruleExprSingle7191);
                    this_Expr_2=ruleExpr();

                    state._fsp--;

                     
                            current = this_Expr_2; 
                            afterParserOrEnumRuleCall();
                        
                    otherlv_3=(Token)match(input,23,FOLLOW_23_in_ruleExprSingle7202); 

                        	newLeafNode(otherlv_3, grammarAccess.getExprSingleAccess().getRightParenthesisKeyword_1_2());
                        

                    }


                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3193:6: ( () ( (lv_intVal_5_0= RULE_INT ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3193:6: ( () ( (lv_intVal_5_0= RULE_INT ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3193:7: () ( (lv_intVal_5_0= RULE_INT ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3193:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3194:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprSingleAccess().getExprIntValAction_2_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3199:2: ( (lv_intVal_5_0= RULE_INT ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3200:1: (lv_intVal_5_0= RULE_INT )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3200:1: (lv_intVal_5_0= RULE_INT )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3201:3: lv_intVal_5_0= RULE_INT
                    {
                    lv_intVal_5_0=(Token)match(input,RULE_INT,FOLLOW_RULE_INT_in_ruleExprSingle7236); 

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
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3218:6: ( () ( (lv_numVal_7_0= ruleNumber ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3218:6: ( () ( (lv_numVal_7_0= ruleNumber ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3218:7: () ( (lv_numVal_7_0= ruleNumber ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3218:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3219:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprSingleAccess().getExprNumValAction_3_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3224:2: ( (lv_numVal_7_0= ruleNumber ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3225:1: (lv_numVal_7_0= ruleNumber )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3225:1: (lv_numVal_7_0= ruleNumber )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3226:3: lv_numVal_7_0= ruleNumber
                    {
                     
                    	        newCompositeNode(grammarAccess.getExprSingleAccess().getNumValNumberParserRuleCall_3_1_0()); 
                    	    
                    pushFollow(FOLLOW_ruleNumber_in_ruleExprSingle7279);
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
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3243:6: ( () ( (lv_strVal_9_0= RULE_STRING ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3243:6: ( () ( (lv_strVal_9_0= RULE_STRING ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3243:7: () ( (lv_strVal_9_0= RULE_STRING ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3243:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3244:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprSingleAccess().getExprStrvalAction_4_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3249:2: ( (lv_strVal_9_0= RULE_STRING ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3250:1: (lv_strVal_9_0= RULE_STRING )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3250:1: (lv_strVal_9_0= RULE_STRING )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3251:3: lv_strVal_9_0= RULE_STRING
                    {
                    lv_strVal_9_0=(Token)match(input,RULE_STRING,FOLLOW_RULE_STRING_in_ruleExprSingle7313); 

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
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3268:6: ( () ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3268:6: ( () ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3268:7: () ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3268:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3269:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprSingleAccess().getExprBoolValAction_5_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3274:2: ( ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3275:1: ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3275:1: ( (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3276:1: (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3276:1: (lv_boolVal_11_1= 'true' | lv_boolVal_11_2= 'false' )
                    int alt61=2;
                    int LA61_0 = input.LA(1);

                    if ( (LA61_0==73) ) {
                        alt61=1;
                    }
                    else if ( (LA61_0==74) ) {
                        alt61=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 61, 0, input);

                        throw nvae;
                    }
                    switch (alt61) {
                        case 1 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3277:3: lv_boolVal_11_1= 'true'
                            {
                            lv_boolVal_11_1=(Token)match(input,73,FOLLOW_73_in_ruleExprSingle7355); 

                                    newLeafNode(lv_boolVal_11_1, grammarAccess.getExprSingleAccess().getBoolValTrueKeyword_5_1_0_0());
                                

                            	        if (current==null) {
                            	            current = createModelElement(grammarAccess.getExprSingleRule());
                            	        }
                                   		setWithLastConsumed(current, "boolVal", lv_boolVal_11_1, null);
                            	    

                            }
                            break;
                        case 2 :
                            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3289:8: lv_boolVal_11_2= 'false'
                            {
                            lv_boolVal_11_2=(Token)match(input,74,FOLLOW_74_in_ruleExprSingle7384); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3312:1: entryRuleNumber returns [String current=null] : iv_ruleNumber= ruleNumber EOF ;
    public final String entryRuleNumber() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleNumber = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3313:2: (iv_ruleNumber= ruleNumber EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3314:2: iv_ruleNumber= ruleNumber EOF
            {
             newCompositeNode(grammarAccess.getNumberRule()); 
            pushFollow(FOLLOW_ruleNumber_in_entryRuleNumber7438);
            iv_ruleNumber=ruleNumber();

            state._fsp--;

             current =iv_ruleNumber.getText(); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleNumber7449); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3321:1: ruleNumber returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_INT_0= RULE_INT kw= '.' this_INT_2= RULE_INT ) ;
    public final AntlrDatatypeRuleToken ruleNumber() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_INT_0=null;
        Token kw=null;
        Token this_INT_2=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3324:28: ( (this_INT_0= RULE_INT kw= '.' this_INT_2= RULE_INT ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3325:1: (this_INT_0= RULE_INT kw= '.' this_INT_2= RULE_INT )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3325:1: (this_INT_0= RULE_INT kw= '.' this_INT_2= RULE_INT )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3325:6: this_INT_0= RULE_INT kw= '.' this_INT_2= RULE_INT
            {
            this_INT_0=(Token)match(input,RULE_INT,FOLLOW_RULE_INT_in_ruleNumber7489); 

            		current.merge(this_INT_0);
                
             
                newLeafNode(this_INT_0, grammarAccess.getNumberAccess().getINTTerminalRuleCall_0()); 
                
            kw=(Token)match(input,15,FOLLOW_15_in_ruleNumber7507); 

                    current.merge(kw);
                    newLeafNode(kw, grammarAccess.getNumberAccess().getFullStopKeyword_1()); 
                
            this_INT_2=(Token)match(input,RULE_INT,FOLLOW_RULE_INT_in_ruleNumber7522); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3353:1: entryRuleExprAtomic returns [EObject current=null] : iv_ruleExprAtomic= ruleExprAtomic EOF ;
    public final EObject entryRuleExprAtomic() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprAtomic = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3354:2: (iv_ruleExprAtomic= ruleExprAtomic EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3355:2: iv_ruleExprAtomic= ruleExprAtomic EOF
            {
             newCompositeNode(grammarAccess.getExprAtomicRule()); 
            pushFollow(FOLLOW_ruleExprAtomic_in_entryRuleExprAtomic7567);
            iv_ruleExprAtomic=ruleExprAtomic();

            state._fsp--;

             current =iv_ruleExprAtomic; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprAtomic7577); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3362:1: ruleExprAtomic returns [EObject current=null] : (this_ExprFunctionCall_0= ruleExprFunctionCall | ( () otherlv_2= 'function' ( (otherlv_3= RULE_ID ) ) ) | ( () ( (otherlv_5= RULE_ID ) ) ) ) ;
    public final EObject ruleExprAtomic() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        EObject this_ExprFunctionCall_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3365:28: ( (this_ExprFunctionCall_0= ruleExprFunctionCall | ( () otherlv_2= 'function' ( (otherlv_3= RULE_ID ) ) ) | ( () ( (otherlv_5= RULE_ID ) ) ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3366:1: (this_ExprFunctionCall_0= ruleExprFunctionCall | ( () otherlv_2= 'function' ( (otherlv_3= RULE_ID ) ) ) | ( () ( (otherlv_5= RULE_ID ) ) ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3366:1: (this_ExprFunctionCall_0= ruleExprFunctionCall | ( () otherlv_2= 'function' ( (otherlv_3= RULE_ID ) ) ) | ( () ( (otherlv_5= RULE_ID ) ) ) )
            int alt63=3;
            int LA63_0 = input.LA(1);

            if ( (LA63_0==RULE_ID) ) {
                int LA63_1 = input.LA(2);

                if ( (LA63_1==21) ) {
                    alt63=1;
                }
                else if ( (LA63_1==EOF||LA63_1==RULE_NL||(LA63_1>=15 && LA63_1<=16)||(LA63_1>=22 && LA63_1<=23)||LA63_1==32||LA63_1==48||(LA63_1>=56 && LA63_1<=71)) ) {
                    alt63=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 63, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA63_0==40) ) {
                alt63=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 63, 0, input);

                throw nvae;
            }
            switch (alt63) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3367:5: this_ExprFunctionCall_0= ruleExprFunctionCall
                    {
                     
                            newCompositeNode(grammarAccess.getExprAtomicAccess().getExprFunctionCallParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleExprFunctionCall_in_ruleExprAtomic7624);
                    this_ExprFunctionCall_0=ruleExprFunctionCall();

                    state._fsp--;

                     
                            current = this_ExprFunctionCall_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3376:6: ( () otherlv_2= 'function' ( (otherlv_3= RULE_ID ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3376:6: ( () otherlv_2= 'function' ( (otherlv_3= RULE_ID ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3376:7: () otherlv_2= 'function' ( (otherlv_3= RULE_ID ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3376:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3377:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprAtomicAccess().getExprFuncRefAction_1_0(),
                                current);
                        

                    }

                    otherlv_2=(Token)match(input,40,FOLLOW_40_in_ruleExprAtomic7651); 

                        	newLeafNode(otherlv_2, grammarAccess.getExprAtomicAccess().getFunctionKeyword_1_1());
                        
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3386:1: ( (otherlv_3= RULE_ID ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3387:1: (otherlv_3= RULE_ID )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3387:1: (otherlv_3= RULE_ID )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3388:3: otherlv_3= RULE_ID
                    {

                    			if (current==null) {
                    	            current = createModelElement(grammarAccess.getExprAtomicRule());
                    	        }
                            
                    otherlv_3=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleExprAtomic7671); 

                    		newLeafNode(otherlv_3, grammarAccess.getExprAtomicAccess().getNameValFuncDefCrossReference_1_2_0()); 
                    	

                    }


                    }


                    }


                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3400:6: ( () ( (otherlv_5= RULE_ID ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3400:6: ( () ( (otherlv_5= RULE_ID ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3400:7: () ( (otherlv_5= RULE_ID ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3400:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3401:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprAtomicAccess().getExprIdentifierAction_2_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3406:2: ( (otherlv_5= RULE_ID ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3407:1: (otherlv_5= RULE_ID )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3407:1: (otherlv_5= RULE_ID )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3408:3: otherlv_5= RULE_ID
                    {

                    			if (current==null) {
                    	            current = createModelElement(grammarAccess.getExprAtomicRule());
                    	        }
                            
                    otherlv_5=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleExprAtomic7708); 

                    		newLeafNode(otherlv_5, grammarAccess.getExprAtomicAccess().getNameValVarDefCrossReference_2_1_0()); 
                    	

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
    // $ANTLR end "ruleExprAtomic"


    // $ANTLR start "entryRuleExprFunctionCall"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3427:1: entryRuleExprFunctionCall returns [EObject current=null] : iv_ruleExprFunctionCall= ruleExprFunctionCall EOF ;
    public final EObject entryRuleExprFunctionCall() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprFunctionCall = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3428:2: (iv_ruleExprFunctionCall= ruleExprFunctionCall EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3429:2: iv_ruleExprFunctionCall= ruleExprFunctionCall EOF
            {
             newCompositeNode(grammarAccess.getExprFunctionCallRule()); 
            pushFollow(FOLLOW_ruleExprFunctionCall_in_entryRuleExprFunctionCall7745);
            iv_ruleExprFunctionCall=ruleExprFunctionCall();

            state._fsp--;

             current =iv_ruleExprFunctionCall; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprFunctionCall7755); 

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
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3436:1: ruleExprFunctionCall returns [EObject current=null] : ( ( () ( (otherlv_1= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) ) ) | ( () ( (otherlv_4= RULE_ID ) ) otherlv_5= '(' otherlv_6= ')' ) ) ;
    public final EObject ruleExprFunctionCall() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_4=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        EObject lv_parameters_2_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3439:28: ( ( ( () ( (otherlv_1= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) ) ) | ( () ( (otherlv_4= RULE_ID ) ) otherlv_5= '(' otherlv_6= ')' ) ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3440:1: ( ( () ( (otherlv_1= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) ) ) | ( () ( (otherlv_4= RULE_ID ) ) otherlv_5= '(' otherlv_6= ')' ) )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3440:1: ( ( () ( (otherlv_1= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) ) ) | ( () ( (otherlv_4= RULE_ID ) ) otherlv_5= '(' otherlv_6= ')' ) )
            int alt64=2;
            int LA64_0 = input.LA(1);

            if ( (LA64_0==RULE_ID) ) {
                int LA64_1 = input.LA(2);

                if ( (LA64_1==21) ) {
                    int LA64_2 = input.LA(3);

                    if ( ((LA64_2>=RULE_ID && LA64_2<=RULE_STRING)||LA64_2==21||LA64_2==40||(LA64_2>=66 && LA64_2<=67)||(LA64_2>=72 && LA64_2<=74)) ) {
                        alt64=1;
                    }
                    else if ( (LA64_2==23) ) {
                        alt64=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 64, 2, input);

                        throw nvae;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 64, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 64, 0, input);

                throw nvae;
            }
            switch (alt64) {
                case 1 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3440:2: ( () ( (otherlv_1= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3440:2: ( () ( (otherlv_1= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3440:3: () ( (otherlv_1= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3440:3: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3441:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprFunctionCallAccess().getExprFunctioncallAction_0_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3446:2: ( (otherlv_1= RULE_ID ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3447:1: (otherlv_1= RULE_ID )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3447:1: (otherlv_1= RULE_ID )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3448:3: otherlv_1= RULE_ID
                    {

                    			if (current==null) {
                    	            current = createModelElement(grammarAccess.getExprFunctionCallRule());
                    	        }
                            
                    otherlv_1=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleExprFunctionCall7810); 

                    		newLeafNode(otherlv_1, grammarAccess.getExprFunctionCallAccess().getNameValFuncDefCrossReference_0_1_0()); 
                    	

                    }


                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3459:2: ( (lv_parameters_2_0= ruleExprList ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3460:1: (lv_parameters_2_0= ruleExprList )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3460:1: (lv_parameters_2_0= ruleExprList )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3461:3: lv_parameters_2_0= ruleExprList
                    {
                     
                    	        newCompositeNode(grammarAccess.getExprFunctionCallAccess().getParametersExprListParserRuleCall_0_2_0()); 
                    	    
                    pushFollow(FOLLOW_ruleExprList_in_ruleExprFunctionCall7831);
                    lv_parameters_2_0=ruleExprList();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getExprFunctionCallRule());
                    	        }
                           		set(
                           			current, 
                           			"parameters",
                            		lv_parameters_2_0, 
                            		"ExprList");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3478:6: ( () ( (otherlv_4= RULE_ID ) ) otherlv_5= '(' otherlv_6= ')' )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3478:6: ( () ( (otherlv_4= RULE_ID ) ) otherlv_5= '(' otherlv_6= ')' )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3478:7: () ( (otherlv_4= RULE_ID ) ) otherlv_5= '(' otherlv_6= ')'
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3478:7: ()
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3479:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getExprFunctionCallAccess().getExprFunctioncallAction_1_0(),
                                current);
                        

                    }

                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3484:2: ( (otherlv_4= RULE_ID ) )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3485:1: (otherlv_4= RULE_ID )
                    {
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3485:1: (otherlv_4= RULE_ID )
                    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3486:3: otherlv_4= RULE_ID
                    {

                    			if (current==null) {
                    	            current = createModelElement(grammarAccess.getExprFunctionCallRule());
                    	        }
                            
                    otherlv_4=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleExprFunctionCall7868); 

                    		newLeafNode(otherlv_4, grammarAccess.getExprFunctionCallAccess().getNameValFuncDefCrossReference_1_1_0()); 
                    	

                    }


                    }

                    otherlv_5=(Token)match(input,21,FOLLOW_21_in_ruleExprFunctionCall7880); 

                        	newLeafNode(otherlv_5, grammarAccess.getExprFunctionCallAccess().getLeftParenthesisKeyword_1_2());
                        
                    otherlv_6=(Token)match(input,23,FOLLOW_23_in_ruleExprFunctionCall7892); 

                        	newLeafNode(otherlv_6, grammarAccess.getExprFunctionCallAccess().getRightParenthesisKeyword_1_3());
                        

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
    // $ANTLR end "ruleExprFunctionCall"


    // $ANTLR start "entryRuleExprList"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3513:1: entryRuleExprList returns [EObject current=null] : iv_ruleExprList= ruleExprList EOF ;
    public final EObject entryRuleExprList() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprList = null;


        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3514:2: (iv_ruleExprList= ruleExprList EOF )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3515:2: iv_ruleExprList= ruleExprList EOF
            {
             newCompositeNode(grammarAccess.getExprListRule()); 
            pushFollow(FOLLOW_ruleExprList_in_entryRuleExprList7929);
            iv_ruleExprList=ruleExprList();

            state._fsp--;

             current =iv_ruleExprList; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprList7939); 

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
    // $ANTLR end "entryRuleExprList"


    // $ANTLR start "ruleExprList"
    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3522:1: ruleExprList returns [EObject current=null] : (otherlv_0= '(' ( (lv_params_1_0= ruleExpr ) ) (otherlv_2= ',' ( (lv_params_3_0= ruleExpr ) ) )* otherlv_4= ')' ) ;
    public final EObject ruleExprList() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_params_1_0 = null;

        EObject lv_params_3_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3525:28: ( (otherlv_0= '(' ( (lv_params_1_0= ruleExpr ) ) (otherlv_2= ',' ( (lv_params_3_0= ruleExpr ) ) )* otherlv_4= ')' ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3526:1: (otherlv_0= '(' ( (lv_params_1_0= ruleExpr ) ) (otherlv_2= ',' ( (lv_params_3_0= ruleExpr ) ) )* otherlv_4= ')' )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3526:1: (otherlv_0= '(' ( (lv_params_1_0= ruleExpr ) ) (otherlv_2= ',' ( (lv_params_3_0= ruleExpr ) ) )* otherlv_4= ')' )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3526:3: otherlv_0= '(' ( (lv_params_1_0= ruleExpr ) ) (otherlv_2= ',' ( (lv_params_3_0= ruleExpr ) ) )* otherlv_4= ')'
            {
            otherlv_0=(Token)match(input,21,FOLLOW_21_in_ruleExprList7976); 

                	newLeafNode(otherlv_0, grammarAccess.getExprListAccess().getLeftParenthesisKeyword_0());
                
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3530:1: ( (lv_params_1_0= ruleExpr ) )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3531:1: (lv_params_1_0= ruleExpr )
            {
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3531:1: (lv_params_1_0= ruleExpr )
            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3532:3: lv_params_1_0= ruleExpr
            {
             
            	        newCompositeNode(grammarAccess.getExprListAccess().getParamsExprParserRuleCall_1_0()); 
            	    
            pushFollow(FOLLOW_ruleExpr_in_ruleExprList7997);
            lv_params_1_0=ruleExpr();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getExprListRule());
            	        }
                   		add(
                   			current, 
                   			"params",
                    		lv_params_1_0, 
                    		"Expr");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3548:2: (otherlv_2= ',' ( (lv_params_3_0= ruleExpr ) ) )*
            loop65:
            do {
                int alt65=2;
                int LA65_0 = input.LA(1);

                if ( (LA65_0==22) ) {
                    alt65=1;
                }


                switch (alt65) {
            	case 1 :
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3548:4: otherlv_2= ',' ( (lv_params_3_0= ruleExpr ) )
            	    {
            	    otherlv_2=(Token)match(input,22,FOLLOW_22_in_ruleExprList8010); 

            	        	newLeafNode(otherlv_2, grammarAccess.getExprListAccess().getCommaKeyword_2_0());
            	        
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3552:1: ( (lv_params_3_0= ruleExpr ) )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3553:1: (lv_params_3_0= ruleExpr )
            	    {
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3553:1: (lv_params_3_0= ruleExpr )
            	    // ../de.peeeq.Pscript2/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3554:3: lv_params_3_0= ruleExpr
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getExprListAccess().getParamsExprParserRuleCall_2_1_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleExpr_in_ruleExprList8031);
            	    lv_params_3_0=ruleExpr();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getExprListRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"params",
            	            		lv_params_3_0, 
            	            		"Expr");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop65;
                }
            } while (true);

            otherlv_4=(Token)match(input,23,FOLLOW_23_in_ruleExprList8045); 

                	newLeafNode(otherlv_4, grammarAccess.getExprListAccess().getRightParenthesisKeyword_3());
                

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
    // $ANTLR end "ruleExprList"

    // Delegated rules


    protected DFA5 dfa5 = new DFA5(this);
    static final String DFA5_eotS =
        "\4\uffff";
    static final String DFA5_eofS =
        "\4\uffff";
    static final String DFA5_minS =
        "\2\4\2\uffff";
    static final String DFA5_maxS =
        "\2\50\2\uffff";
    static final String DFA5_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA5_specialS =
        "\4\uffff}>";
    static final String[] DFA5_transitionS = {
            "\1\1\1\2\7\uffff\1\2\1\3\2\uffff\1\2\1\uffff\2\2\6\uffff\1"+
            "\2\1\uffff\1\2\1\uffff\1\2\1\uffff\10\2",
            "\1\1\1\2\7\uffff\1\2\1\3\2\uffff\1\2\1\uffff\2\2\6\uffff\1"+
            "\2\1\uffff\1\2\1\uffff\1\2\1\uffff\10\2",
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
            return "()* loopback of 173:2: ( (this_NL_3= RULE_NL )* ( (lv_imports_4_0= ruleImport ) ) )*";
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
    public static final BitSet FOLLOW_RULE_ID_in_rulePackageDeclaration288 = new BitSet(new long[]{0x000001FEA81A6030L});
    public static final BitSet FOLLOW_RULE_NL_in_rulePackageDeclaration306 = new BitSet(new long[]{0x0000000000004010L});
    public static final BitSet FOLLOW_ruleImport_in_rulePackageDeclaration328 = new BitSet(new long[]{0x000001FEA81A6030L});
    public static final BitSet FOLLOW_RULE_NL_in_rulePackageDeclaration342 = new BitSet(new long[]{0x000001FEA81A2030L});
    public static final BitSet FOLLOW_ruleEntity_in_rulePackageDeclaration365 = new BitSet(new long[]{0x000001FEA81A2030L});
    public static final BitSet FOLLOW_RULE_NL_in_rulePackageDeclaration377 = new BitSet(new long[]{0x000001FEA81A2030L});
    public static final BitSet FOLLOW_13_in_rulePackageDeclaration392 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_RULE_NL_in_rulePackageDeclaration404 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_ruleImport_in_entryRuleImport441 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleImport451 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_14_in_ruleImport488 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ruleImportString_in_ruleImport509 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleImport520 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleImportString_in_entryRuleImportString556 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleImportString567 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleImportString607 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_ruleImportString625 = new BitSet(new long[]{0x0000000000010020L});
    public static final BitSet FOLLOW_16_in_ruleImportString639 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleImportString660 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleEntity_in_entryRuleEntity706 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleEntity716 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleTypeDef_in_ruleEntity763 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFuncDef_in_ruleEntity790 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleVarDef_in_ruleEntity817 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleInitBlock_in_ruleEntity844 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNativeFunc_in_ruleEntity871 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleInitBlock_in_entryRuleInitBlock906 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleInitBlock916 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_17_in_ruleInitBlock959 = new BitSet(new long[]{0x00D0DD00802400F0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleStatements_in_ruleInitBlock993 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_18_in_ruleInitBlock1005 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleTypeDef_in_entryRuleTypeDef1041 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleTypeDef1051 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNativeType_in_ruleTypeDef1098 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleClassDef_in_ruleTypeDef1125 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNativeFunc_in_entryRuleNativeFunc1160 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleNativeFunc1170 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_19_in_ruleNativeFunc1217 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_20_in_ruleNativeFunc1231 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleNativeFunc1248 = new BitSet(new long[]{0x0000000001200000L});
    public static final BitSet FOLLOW_21_in_ruleNativeFunc1267 = new BitSet(new long[]{0x000000FC00880020L});
    public static final BitSet FOLLOW_ruleParameterDef_in_ruleNativeFunc1289 = new BitSet(new long[]{0x0000000000C00000L});
    public static final BitSet FOLLOW_22_in_ruleNativeFunc1302 = new BitSet(new long[]{0x000000FC00080020L});
    public static final BitSet FOLLOW_ruleParameterDef_in_ruleNativeFunc1323 = new BitSet(new long[]{0x0000000000C00000L});
    public static final BitSet FOLLOW_23_in_ruleNativeFunc1339 = new BitSet(new long[]{0x0000000004000010L});
    public static final BitSet FOLLOW_24_in_ruleNativeFunc1359 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_25_in_ruleNativeFunc1371 = new BitSet(new long[]{0x0000000004000010L});
    public static final BitSet FOLLOW_24_in_ruleNativeFunc1391 = new BitSet(new long[]{0x000000FC00080020L});
    public static final BitSet FOLLOW_ruleParameterDef_in_ruleNativeFunc1412 = new BitSet(new long[]{0x0000000004400010L});
    public static final BitSet FOLLOW_22_in_ruleNativeFunc1425 = new BitSet(new long[]{0x000000FC00080020L});
    public static final BitSet FOLLOW_ruleParameterDef_in_ruleNativeFunc1446 = new BitSet(new long[]{0x0000000004400010L});
    public static final BitSet FOLLOW_26_in_ruleNativeFunc1464 = new BitSet(new long[]{0x000000FC00080020L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleNativeFunc1485 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_26_in_ruleNativeFunc1505 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_25_in_ruleNativeFunc1517 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleNativeFunc1531 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNativeType_in_entryRuleNativeType1566 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleNativeType1576 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_ruleNativeType1622 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleNativeType1639 = new BitSet(new long[]{0x0000000010000010L});
    public static final BitSet FOLLOW_28_in_ruleNativeType1657 = new BitSet(new long[]{0x000000FC00080020L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleNativeType1678 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleNativeType1691 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleClassDef_in_entryRuleClassDef1726 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleClassDef1736 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_ruleClassDef1782 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleClassDef1799 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleClassDef1815 = new BitSet(new long[]{0x000001FEC0080030L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleClassDef1826 = new BitSet(new long[]{0x000001FEC0080030L});
    public static final BitSet FOLLOW_ruleClassMember_in_ruleClassDef1852 = new BitSet(new long[]{0x000001FEC0080030L});
    public static final BitSet FOLLOW_30_in_ruleClassDef1866 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleClassDef1877 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleClassMember_in_entryRuleClassMember1912 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleClassMember1922 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleVarDef_in_ruleClassMember1969 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFuncDef_in_ruleClassMember1996 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleVarDef_in_entryRuleVarDef2031 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleVarDef2041 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_ruleVarDef2095 = new BitSet(new long[]{0x000000FC00080020L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleVarDef2129 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleVarDef2147 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_32_in_ruleVarDef2164 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExpr_in_ruleVarDef2185 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_19_in_ruleVarDef2211 = new BitSet(new long[]{0x000000FC00080020L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleVarDef2246 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleVarDef2263 = new BitSet(new long[]{0x0000000100000010L});
    public static final BitSet FOLLOW_32_in_ruleVarDef2281 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExpr_in_ruleVarDef2302 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_33_in_ruleVarDef2324 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleVarDef2341 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_32_in_ruleVarDef2358 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExpr_in_ruleVarDef2379 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleVarDef2392 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_entryRuleTypeExpr2427 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleTypeExpr2437 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleTypeExpr2492 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_34_in_ruleTypeExpr2529 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_35_in_ruleTypeExpr2558 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_36_in_ruleTypeExpr2587 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_37_in_ruleTypeExpr2616 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_38_in_ruleTypeExpr2645 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_39_in_ruleTypeExpr2674 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFuncDef_in_entryRuleFuncDef2727 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleFuncDef2737 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_ruleFuncDef2774 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleFuncDef2791 = new BitSet(new long[]{0x0000000001200000L});
    public static final BitSet FOLLOW_21_in_ruleFuncDef2810 = new BitSet(new long[]{0x000000FC00880020L});
    public static final BitSet FOLLOW_ruleParameterDef_in_ruleFuncDef2832 = new BitSet(new long[]{0x0000000000C00000L});
    public static final BitSet FOLLOW_22_in_ruleFuncDef2845 = new BitSet(new long[]{0x000000FC00080020L});
    public static final BitSet FOLLOW_ruleParameterDef_in_ruleFuncDef2866 = new BitSet(new long[]{0x0000000000C00000L});
    public static final BitSet FOLLOW_23_in_ruleFuncDef2882 = new BitSet(new long[]{0x0000000004000010L});
    public static final BitSet FOLLOW_24_in_ruleFuncDef2902 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_25_in_ruleFuncDef2914 = new BitSet(new long[]{0x0000000004000010L});
    public static final BitSet FOLLOW_24_in_ruleFuncDef2934 = new BitSet(new long[]{0x000000FC00080020L});
    public static final BitSet FOLLOW_ruleParameterDef_in_ruleFuncDef2955 = new BitSet(new long[]{0x0000000004400010L});
    public static final BitSet FOLLOW_22_in_ruleFuncDef2968 = new BitSet(new long[]{0x000000FC00080020L});
    public static final BitSet FOLLOW_ruleParameterDef_in_ruleFuncDef2989 = new BitSet(new long[]{0x0000000004400010L});
    public static final BitSet FOLLOW_26_in_ruleFuncDef3007 = new BitSet(new long[]{0x000000FC00080020L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleFuncDef3028 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_26_in_ruleFuncDef3048 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_25_in_ruleFuncDef3060 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleFuncDef3074 = new BitSet(new long[]{0x00D0DF00802000F0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleStatements_in_ruleFuncDef3094 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_41_in_ruleFuncDef3106 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleParameterDef_in_entryRuleParameterDef3142 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleParameterDef3152 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleParameterDef3207 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleParameterDef3224 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStatements_in_entryRuleStatements3265 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStatements3275 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStatements3321 = new BitSet(new long[]{0x00D0DD00802000F2L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleStatement_in_ruleStatements3347 = new BitSet(new long[]{0x00D0DD00802000F2L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleStatement_in_entryRuleStatement3385 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStatement3395 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtIf_in_ruleStatement3442 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtWhile_in_ruleStatement3469 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleLocalVarDef_in_ruleStatement3496 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtSetOrCall_in_ruleStatement3523 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtReturn_in_ruleStatement3550 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtLoop_in_ruleStatement3577 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtExitwhen_in_ruleStatement3604 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleLocalVarDef_in_entryRuleLocalVarDef3639 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleLocalVarDef3649 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_ruleLocalVarDef3696 = new BitSet(new long[]{0x000000FC00080020L});
    public static final BitSet FOLLOW_31_in_ruleLocalVarDef3720 = new BitSet(new long[]{0x000000FC00080020L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleLocalVarDef3755 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleLocalVarDef3773 = new BitSet(new long[]{0x0000000100000010L});
    public static final BitSet FOLLOW_32_in_ruleLocalVarDef3791 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExpr_in_ruleLocalVarDef3812 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleLocalVarDef3825 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtExitwhen_in_entryRuleStmtExitwhen3860 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStmtExitwhen3870 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_43_in_ruleStmtExitwhen3907 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExpr_in_ruleStmtExitwhen3928 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtExitwhen3939 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtLoop_in_entryRuleStmtLoop3974 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStmtLoop3984 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_ruleStmtLoop4021 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtLoop4032 = new BitSet(new long[]{0x00D0FD00802000F0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleStatements_in_ruleStmtLoop4052 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_45_in_ruleStmtLoop4064 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtLoop4075 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtReturn_in_entryRuleStmtReturn4110 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStmtReturn4120 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_46_in_ruleStmtReturn4166 = new BitSet(new long[]{0x00C00100002000F0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExpr_in_ruleStmtReturn4187 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtReturn4199 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtIf_in_entryRuleStmtIf4234 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStmtIf4244 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_47_in_ruleStmtIf4281 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExpr_in_ruleStmtIf4302 = new BitSet(new long[]{0x0001000000000010L});
    public static final BitSet FOLLOW_48_in_ruleStmtIf4315 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtIf4328 = new BitSet(new long[]{0x00DEDD00802000F0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleStatements_in_ruleStmtIf4348 = new BitSet(new long[]{0x000E000000000000L});
    public static final BitSet FOLLOW_49_in_ruleStmtIf4361 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExpr_in_ruleStmtIf4382 = new BitSet(new long[]{0x0001000000000010L});
    public static final BitSet FOLLOW_48_in_ruleStmtIf4395 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtIf4408 = new BitSet(new long[]{0x00DEDD00802000F0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleStatements_in_ruleStmtIf4428 = new BitSet(new long[]{0x000E000000000000L});
    public static final BitSet FOLLOW_50_in_ruleStmtIf4443 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtIf4454 = new BitSet(new long[]{0x00D8DD00802000F0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleStatements_in_ruleStmtIf4474 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_ruleStmtIf4488 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtIf4499 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtWhile_in_entryRuleStmtWhile4534 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStmtWhile4544 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_52_in_ruleStmtWhile4581 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExpr_in_ruleStmtWhile4602 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtWhile4613 = new BitSet(new long[]{0x00F0DD00802000F0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleStatements_in_ruleStmtWhile4633 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_53_in_ruleStmtWhile4645 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtWhile4656 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtSetOrCall_in_entryRuleStmtSetOrCall4691 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStmtSetOrCall4701 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_54_in_ruleStmtSetOrCall4748 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_55_in_ruleStmtSetOrCall4766 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExpr_in_ruleStmtSetOrCall4789 = new BitSet(new long[]{0x0300000100000010L});
    public static final BitSet FOLLOW_ruleOpAssignment_in_ruleStmtSetOrCall4820 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExpr_in_ruleStmtSetOrCall4841 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtSetOrCall4854 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleOpAssignment_in_entryRuleOpAssignment4889 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleOpAssignment4899 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_ruleOpAssignment4946 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_56_in_ruleOpAssignment4975 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_57_in_ruleOpAssignment5004 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExpr_in_entryRuleExpr5041 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExpr5051 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprOr_in_ruleExpr5097 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprOr_in_entryRuleExprOr5131 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprOr5141 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprAnd_in_ruleExprOr5188 = new BitSet(new long[]{0x0400000000000002L});
    public static final BitSet FOLLOW_58_in_ruleExprOr5215 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExprAnd_in_ruleExprOr5249 = new BitSet(new long[]{0x0400000000000002L});
    public static final BitSet FOLLOW_ruleExprAnd_in_entryRuleExprAnd5287 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprAnd5297 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprEquality_in_ruleExprAnd5344 = new BitSet(new long[]{0x0800000000000002L});
    public static final BitSet FOLLOW_59_in_ruleExprAnd5371 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExprEquality_in_ruleExprAnd5405 = new BitSet(new long[]{0x0800000000000002L});
    public static final BitSet FOLLOW_ruleExprEquality_in_entryRuleExprEquality5443 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprEquality5453 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprComparison_in_ruleExprEquality5500 = new BitSet(new long[]{0x3000000000000002L});
    public static final BitSet FOLLOW_ruleOpEquality_in_ruleExprEquality5530 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExprComparison_in_ruleExprEquality5551 = new BitSet(new long[]{0x3000000000000002L});
    public static final BitSet FOLLOW_ruleOpEquality_in_entryRuleOpEquality5589 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleOpEquality5599 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_60_in_ruleOpEquality5646 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_61_in_ruleOpEquality5675 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprComparison_in_entryRuleExprComparison5712 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprComparison5722 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprAdditive_in_ruleExprComparison5769 = new BitSet(new long[]{0xC000000000000002L,0x0000000000000003L});
    public static final BitSet FOLLOW_ruleOpComparison_in_ruleExprComparison5799 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExprAdditive_in_ruleExprComparison5820 = new BitSet(new long[]{0xC000000000000002L,0x0000000000000003L});
    public static final BitSet FOLLOW_ruleOpComparison_in_entryRuleOpComparison5858 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleOpComparison5868 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_62_in_ruleOpComparison5915 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_63_in_ruleOpComparison5944 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_64_in_ruleOpComparison5973 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_65_in_ruleOpComparison6002 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprAdditive_in_entryRuleExprAdditive6039 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprAdditive6049 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprMult_in_ruleExprAdditive6096 = new BitSet(new long[]{0x0000000000000002L,0x000000000000000CL});
    public static final BitSet FOLLOW_ruleOpAdditive_in_ruleExprAdditive6126 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExprMult_in_ruleExprAdditive6147 = new BitSet(new long[]{0x0000000000000002L,0x000000000000000CL});
    public static final BitSet FOLLOW_ruleOpAdditive_in_entryRuleOpAdditive6185 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleOpAdditive6195 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_66_in_ruleOpAdditive6242 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_67_in_ruleOpAdditive6271 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprMult_in_entryRuleExprMult6308 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprMult6318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprSign_in_ruleExprMult6365 = new BitSet(new long[]{0x0000000000010002L,0x00000000000000F0L});
    public static final BitSet FOLLOW_ruleOpMultiplicative_in_ruleExprMult6395 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExprSign_in_ruleExprMult6416 = new BitSet(new long[]{0x0000000000010002L,0x00000000000000F0L});
    public static final BitSet FOLLOW_ruleOpMultiplicative_in_entryRuleOpMultiplicative6454 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleOpMultiplicative6464 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_16_in_ruleOpMultiplicative6511 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_68_in_ruleOpMultiplicative6540 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_69_in_ruleOpMultiplicative6569 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_70_in_ruleOpMultiplicative6598 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_71_in_ruleOpMultiplicative6627 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprSign_in_entryRuleExprSign6664 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprSign6674 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleOpAdditive_in_ruleExprSign6730 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExprNot_in_ruleExprSign6751 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprNot_in_ruleExprSign6780 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprNot_in_entryRuleExprNot6815 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprNot6825 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_72_in_ruleExprNot6872 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExprMember_in_ruleExprNot6893 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprMember_in_ruleExprNot6922 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprMember_in_entryRuleExprMember6957 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprMember6967 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprSingle_in_ruleExprMember7014 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_15_in_ruleExprMember7035 = new BitSet(new long[]{0x0000010000000020L});
    public static final BitSet FOLLOW_ruleExprAtomic_in_ruleExprMember7056 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_ruleExprSingle_in_entryRuleExprSingle7094 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprSingle7104 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprAtomic_in_ruleExprSingle7151 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_21_in_ruleExprSingle7169 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExpr_in_ruleExprSingle7191 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_ruleExprSingle7202 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_INT_in_ruleExprSingle7236 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNumber_in_ruleExprSingle7279 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_STRING_in_ruleExprSingle7313 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_73_in_ruleExprSingle7355 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_74_in_ruleExprSingle7384 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNumber_in_entryRuleNumber7438 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleNumber7449 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_INT_in_ruleNumber7489 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_ruleNumber7507 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_RULE_INT_in_ruleNumber7522 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprAtomic_in_entryRuleExprAtomic7567 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprAtomic7577 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprFunctionCall_in_ruleExprAtomic7624 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_ruleExprAtomic7651 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleExprAtomic7671 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleExprAtomic7708 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprFunctionCall_in_entryRuleExprFunctionCall7745 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprFunctionCall7755 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleExprFunctionCall7810 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_ruleExprList_in_ruleExprFunctionCall7831 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleExprFunctionCall7868 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_21_in_ruleExprFunctionCall7880 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_ruleExprFunctionCall7892 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprList_in_entryRuleExprList7929 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprList7939 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_21_in_ruleExprList7976 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExpr_in_ruleExprList7997 = new BitSet(new long[]{0x0000000000C00000L});
    public static final BitSet FOLLOW_22_in_ruleExprList8010 = new BitSet(new long[]{0x00C00100002000E0L,0x000000000000070CL});
    public static final BitSet FOLLOW_ruleExpr_in_ruleExprList8031 = new BitSet(new long[]{0x0000000000C00000L});
    public static final BitSet FOLLOW_23_in_ruleExprList8045 = new BitSet(new long[]{0x0000000000000002L});

}