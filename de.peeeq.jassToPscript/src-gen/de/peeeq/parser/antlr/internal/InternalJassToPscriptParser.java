package de.peeeq.parser.antlr.internal; 

import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
import de.peeeq.services.JassToPscriptGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalJassToPscriptParser extends AbstractInternalAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_INT", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'globals'", "'endglobals'", "'constant'", "'='", "'*'", "'('", "','", "')'", "'true'", "'false'", "'native'", "'takes'", "'returns'", "'nothing'", "'type'", "'extends'"
    };
    public static final int RULE_ID=4;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int RULE_ANY_OTHER=10;
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int RULE_SL_COMMENT=8;
    public static final int EOF=-1;
    public static final int RULE_ML_COMMENT=7;
    public static final int T__19=19;
    public static final int RULE_STRING=6;
    public static final int T__16=16;
    public static final int T__15=15;
    public static final int T__18=18;
    public static final int T__17=17;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int RULE_INT=5;
    public static final int RULE_WS=9;

    // delegates
    // delegators


        public InternalJassToPscriptParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public InternalJassToPscriptParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return InternalJassToPscriptParser.tokenNames; }
    public String getGrammarFileName() { return "../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g"; }



     	private JassToPscriptGrammarAccess grammarAccess;
     	
        public InternalJassToPscriptParser(TokenStream input, JassToPscriptGrammarAccess grammarAccess) {
            this(input);
            this.grammarAccess = grammarAccess;
            registerRules(grammarAccess.getGrammar());
        }
        
        @Override
        protected String getFirstRuleName() {
        	return "Prog";	
       	}
       	
       	@Override
       	protected JassToPscriptGrammarAccess getGrammarAccess() {
       		return grammarAccess;
       	}



    // $ANTLR start "entryRuleProg"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:67:1: entryRuleProg returns [EObject current=null] : iv_ruleProg= ruleProg EOF ;
    public final EObject entryRuleProg() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleProg = null;


        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:68:2: (iv_ruleProg= ruleProg EOF )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:69:2: iv_ruleProg= ruleProg EOF
            {
             newCompositeNode(grammarAccess.getProgRule()); 
            pushFollow(FOLLOW_ruleProg_in_entryRuleProg75);
            iv_ruleProg=ruleProg();

            state._fsp--;

             current =iv_ruleProg; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleProg85); 

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
    // $ANTLR end "entryRuleProg"


    // $ANTLR start "ruleProg"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:76:1: ruleProg returns [EObject current=null] : ( (lv_elems_0_0= ruleEntity ) )* ;
    public final EObject ruleProg() throws RecognitionException {
        EObject current = null;

        EObject lv_elems_0_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:79:28: ( ( (lv_elems_0_0= ruleEntity ) )* )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:80:1: ( (lv_elems_0_0= ruleEntity ) )*
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:80:1: ( (lv_elems_0_0= ruleEntity ) )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==11||LA1_0==13||LA1_0==21||LA1_0==25) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:81:1: (lv_elems_0_0= ruleEntity )
            	    {
            	    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:81:1: (lv_elems_0_0= ruleEntity )
            	    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:82:3: lv_elems_0_0= ruleEntity
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getProgAccess().getElemsEntityParserRuleCall_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleEntity_in_ruleProg130);
            	    lv_elems_0_0=ruleEntity();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getProgRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"elems",
            	            		lv_elems_0_0, 
            	            		"Entity");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


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
    // $ANTLR end "ruleProg"


    // $ANTLR start "entryRuleEntity"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:106:1: entryRuleEntity returns [EObject current=null] : iv_ruleEntity= ruleEntity EOF ;
    public final EObject entryRuleEntity() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEntity = null;


        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:107:2: (iv_ruleEntity= ruleEntity EOF )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:108:2: iv_ruleEntity= ruleEntity EOF
            {
             newCompositeNode(grammarAccess.getEntityRule()); 
            pushFollow(FOLLOW_ruleEntity_in_entryRuleEntity166);
            iv_ruleEntity=ruleEntity();

            state._fsp--;

             current =iv_ruleEntity; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleEntity176); 

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
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:115:1: ruleEntity returns [EObject current=null] : (this_TypeDef_0= ruleTypeDef | this_GlobalBlock_1= ruleGlobalBlock | this_NativeDef_2= ruleNativeDef ) ;
    public final EObject ruleEntity() throws RecognitionException {
        EObject current = null;

        EObject this_TypeDef_0 = null;

        EObject this_GlobalBlock_1 = null;

        EObject this_NativeDef_2 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:118:28: ( (this_TypeDef_0= ruleTypeDef | this_GlobalBlock_1= ruleGlobalBlock | this_NativeDef_2= ruleNativeDef ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:119:1: (this_TypeDef_0= ruleTypeDef | this_GlobalBlock_1= ruleGlobalBlock | this_NativeDef_2= ruleNativeDef )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:119:1: (this_TypeDef_0= ruleTypeDef | this_GlobalBlock_1= ruleGlobalBlock | this_NativeDef_2= ruleNativeDef )
            int alt2=3;
            switch ( input.LA(1) ) {
            case 25:
                {
                alt2=1;
                }
                break;
            case 11:
                {
                alt2=2;
                }
                break;
            case 13:
            case 21:
                {
                alt2=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }

            switch (alt2) {
                case 1 :
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:120:5: this_TypeDef_0= ruleTypeDef
                    {
                     
                            newCompositeNode(grammarAccess.getEntityAccess().getTypeDefParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleTypeDef_in_ruleEntity223);
                    this_TypeDef_0=ruleTypeDef();

                    state._fsp--;

                     
                            current = this_TypeDef_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:130:5: this_GlobalBlock_1= ruleGlobalBlock
                    {
                     
                            newCompositeNode(grammarAccess.getEntityAccess().getGlobalBlockParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleGlobalBlock_in_ruleEntity250);
                    this_GlobalBlock_1=ruleGlobalBlock();

                    state._fsp--;

                     
                            current = this_GlobalBlock_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 3 :
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:140:5: this_NativeDef_2= ruleNativeDef
                    {
                     
                            newCompositeNode(grammarAccess.getEntityAccess().getNativeDefParserRuleCall_2()); 
                        
                    pushFollow(FOLLOW_ruleNativeDef_in_ruleEntity277);
                    this_NativeDef_2=ruleNativeDef();

                    state._fsp--;

                     
                            current = this_NativeDef_2; 
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


    // $ANTLR start "entryRuleGlobalBlock"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:156:1: entryRuleGlobalBlock returns [EObject current=null] : iv_ruleGlobalBlock= ruleGlobalBlock EOF ;
    public final EObject entryRuleGlobalBlock() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleGlobalBlock = null;


        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:157:2: (iv_ruleGlobalBlock= ruleGlobalBlock EOF )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:158:2: iv_ruleGlobalBlock= ruleGlobalBlock EOF
            {
             newCompositeNode(grammarAccess.getGlobalBlockRule()); 
            pushFollow(FOLLOW_ruleGlobalBlock_in_entryRuleGlobalBlock312);
            iv_ruleGlobalBlock=ruleGlobalBlock();

            state._fsp--;

             current =iv_ruleGlobalBlock; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleGlobalBlock322); 

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
    // $ANTLR end "entryRuleGlobalBlock"


    // $ANTLR start "ruleGlobalBlock"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:165:1: ruleGlobalBlock returns [EObject current=null] : ( () otherlv_1= 'globals' ( (lv_vars_2_0= ruleConstant ) )* otherlv_3= 'endglobals' ) ;
    public final EObject ruleGlobalBlock() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_vars_2_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:168:28: ( ( () otherlv_1= 'globals' ( (lv_vars_2_0= ruleConstant ) )* otherlv_3= 'endglobals' ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:169:1: ( () otherlv_1= 'globals' ( (lv_vars_2_0= ruleConstant ) )* otherlv_3= 'endglobals' )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:169:1: ( () otherlv_1= 'globals' ( (lv_vars_2_0= ruleConstant ) )* otherlv_3= 'endglobals' )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:169:2: () otherlv_1= 'globals' ( (lv_vars_2_0= ruleConstant ) )* otherlv_3= 'endglobals'
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:169:2: ()
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:170:5: 
            {

                    current = forceCreateModelElement(
                        grammarAccess.getGlobalBlockAccess().getGlobalBlockAction_0(),
                        current);
                

            }

            otherlv_1=(Token)match(input,11,FOLLOW_11_in_ruleGlobalBlock368); 

                	newLeafNode(otherlv_1, grammarAccess.getGlobalBlockAccess().getGlobalsKeyword_1());
                
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:179:1: ( (lv_vars_2_0= ruleConstant ) )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==13) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:180:1: (lv_vars_2_0= ruleConstant )
            	    {
            	    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:180:1: (lv_vars_2_0= ruleConstant )
            	    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:181:3: lv_vars_2_0= ruleConstant
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getGlobalBlockAccess().getVarsConstantParserRuleCall_2_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleConstant_in_ruleGlobalBlock389);
            	    lv_vars_2_0=ruleConstant();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getGlobalBlockRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"vars",
            	            		lv_vars_2_0, 
            	            		"Constant");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            otherlv_3=(Token)match(input,12,FOLLOW_12_in_ruleGlobalBlock402); 

                	newLeafNode(otherlv_3, grammarAccess.getGlobalBlockAccess().getEndglobalsKeyword_3());
                

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
    // $ANTLR end "ruleGlobalBlock"


    // $ANTLR start "entryRuleConstant"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:209:1: entryRuleConstant returns [EObject current=null] : iv_ruleConstant= ruleConstant EOF ;
    public final EObject entryRuleConstant() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleConstant = null;


        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:210:2: (iv_ruleConstant= ruleConstant EOF )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:211:2: iv_ruleConstant= ruleConstant EOF
            {
             newCompositeNode(grammarAccess.getConstantRule()); 
            pushFollow(FOLLOW_ruleConstant_in_entryRuleConstant438);
            iv_ruleConstant=ruleConstant();

            state._fsp--;

             current =iv_ruleConstant; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleConstant448); 

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
    // $ANTLR end "entryRuleConstant"


    // $ANTLR start "ruleConstant"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:218:1: ruleConstant returns [EObject current=null] : (otherlv_0= 'constant' ( (lv_type_1_0= RULE_ID ) ) ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '=' ( (lv_value_4_0= ruleExpr ) ) ) ;
    public final EObject ruleConstant() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_type_1_0=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        EObject lv_value_4_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:221:28: ( (otherlv_0= 'constant' ( (lv_type_1_0= RULE_ID ) ) ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '=' ( (lv_value_4_0= ruleExpr ) ) ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:222:1: (otherlv_0= 'constant' ( (lv_type_1_0= RULE_ID ) ) ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '=' ( (lv_value_4_0= ruleExpr ) ) )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:222:1: (otherlv_0= 'constant' ( (lv_type_1_0= RULE_ID ) ) ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '=' ( (lv_value_4_0= ruleExpr ) ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:222:3: otherlv_0= 'constant' ( (lv_type_1_0= RULE_ID ) ) ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '=' ( (lv_value_4_0= ruleExpr ) )
            {
            otherlv_0=(Token)match(input,13,FOLLOW_13_in_ruleConstant485); 

                	newLeafNode(otherlv_0, grammarAccess.getConstantAccess().getConstantKeyword_0());
                
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:226:1: ( (lv_type_1_0= RULE_ID ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:227:1: (lv_type_1_0= RULE_ID )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:227:1: (lv_type_1_0= RULE_ID )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:228:3: lv_type_1_0= RULE_ID
            {
            lv_type_1_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleConstant502); 

            			newLeafNode(lv_type_1_0, grammarAccess.getConstantAccess().getTypeIDTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getConstantRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"type",
                    		lv_type_1_0, 
                    		"ID");
            	    

            }


            }

            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:244:2: ( (lv_name_2_0= RULE_ID ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:245:1: (lv_name_2_0= RULE_ID )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:245:1: (lv_name_2_0= RULE_ID )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:246:3: lv_name_2_0= RULE_ID
            {
            lv_name_2_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleConstant524); 

            			newLeafNode(lv_name_2_0, grammarAccess.getConstantAccess().getNameIDTerminalRuleCall_2_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getConstantRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_2_0, 
                    		"ID");
            	    

            }


            }

            otherlv_3=(Token)match(input,14,FOLLOW_14_in_ruleConstant541); 

                	newLeafNode(otherlv_3, grammarAccess.getConstantAccess().getEqualsSignKeyword_3());
                
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:266:1: ( (lv_value_4_0= ruleExpr ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:267:1: (lv_value_4_0= ruleExpr )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:267:1: (lv_value_4_0= ruleExpr )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:268:3: lv_value_4_0= ruleExpr
            {
             
            	        newCompositeNode(grammarAccess.getConstantAccess().getValueExprParserRuleCall_4_0()); 
            	    
            pushFollow(FOLLOW_ruleExpr_in_ruleConstant562);
            lv_value_4_0=ruleExpr();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getConstantRule());
            	        }
                   		set(
                   			current, 
                   			"value",
                    		lv_value_4_0, 
                    		"Expr");
            	        afterParserOrEnumRuleCall();
            	    

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
    // $ANTLR end "ruleConstant"


    // $ANTLR start "entryRuleExpr"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:292:1: entryRuleExpr returns [EObject current=null] : iv_ruleExpr= ruleExpr EOF ;
    public final EObject entryRuleExpr() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExpr = null;


        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:293:2: (iv_ruleExpr= ruleExpr EOF )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:294:2: iv_ruleExpr= ruleExpr EOF
            {
             newCompositeNode(grammarAccess.getExprRule()); 
            pushFollow(FOLLOW_ruleExpr_in_entryRuleExpr598);
            iv_ruleExpr=ruleExpr();

            state._fsp--;

             current =iv_ruleExpr; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExpr608); 

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
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:301:1: ruleExpr returns [EObject current=null] : (this_Literal_0= ruleLiteral | this_FunctionCall_1= ruleFunctionCall | this_Mult_2= ruleMult ) ;
    public final EObject ruleExpr() throws RecognitionException {
        EObject current = null;

        EObject this_Literal_0 = null;

        EObject this_FunctionCall_1 = null;

        EObject this_Mult_2 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:304:28: ( (this_Literal_0= ruleLiteral | this_FunctionCall_1= ruleFunctionCall | this_Mult_2= ruleMult ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:305:1: (this_Literal_0= ruleLiteral | this_FunctionCall_1= ruleFunctionCall | this_Mult_2= ruleMult )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:305:1: (this_Literal_0= ruleLiteral | this_FunctionCall_1= ruleFunctionCall | this_Mult_2= ruleMult )
            int alt4=3;
            switch ( input.LA(1) ) {
            case RULE_INT:
                {
                int LA4_1 = input.LA(2);

                if ( (LA4_1==15) ) {
                    alt4=3;
                }
                else if ( (LA4_1==EOF||(LA4_1>=12 && LA4_1<=13)||(LA4_1>=17 && LA4_1<=18)) ) {
                    alt4=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 1, input);

                    throw nvae;
                }
                }
                break;
            case 19:
                {
                int LA4_2 = input.LA(2);

                if ( (LA4_2==15) ) {
                    alt4=3;
                }
                else if ( (LA4_2==EOF||(LA4_2>=12 && LA4_2<=13)||(LA4_2>=17 && LA4_2<=18)) ) {
                    alt4=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 2, input);

                    throw nvae;
                }
                }
                break;
            case 20:
                {
                int LA4_3 = input.LA(2);

                if ( (LA4_3==15) ) {
                    alt4=3;
                }
                else if ( (LA4_3==EOF||(LA4_3>=12 && LA4_3<=13)||(LA4_3>=17 && LA4_3<=18)) ) {
                    alt4=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 3, input);

                    throw nvae;
                }
                }
                break;
            case RULE_ID:
                {
                alt4=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }

            switch (alt4) {
                case 1 :
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:306:5: this_Literal_0= ruleLiteral
                    {
                     
                            newCompositeNode(grammarAccess.getExprAccess().getLiteralParserRuleCall_0()); 
                        
                    pushFollow(FOLLOW_ruleLiteral_in_ruleExpr655);
                    this_Literal_0=ruleLiteral();

                    state._fsp--;

                     
                            current = this_Literal_0; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:316:5: this_FunctionCall_1= ruleFunctionCall
                    {
                     
                            newCompositeNode(grammarAccess.getExprAccess().getFunctionCallParserRuleCall_1()); 
                        
                    pushFollow(FOLLOW_ruleFunctionCall_in_ruleExpr682);
                    this_FunctionCall_1=ruleFunctionCall();

                    state._fsp--;

                     
                            current = this_FunctionCall_1; 
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;
                case 3 :
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:326:5: this_Mult_2= ruleMult
                    {
                     
                            newCompositeNode(grammarAccess.getExprAccess().getMultParserRuleCall_2()); 
                        
                    pushFollow(FOLLOW_ruleMult_in_ruleExpr709);
                    this_Mult_2=ruleMult();

                    state._fsp--;

                     
                            current = this_Mult_2; 
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
    // $ANTLR end "ruleExpr"


    // $ANTLR start "entryRuleMult"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:342:1: entryRuleMult returns [EObject current=null] : iv_ruleMult= ruleMult EOF ;
    public final EObject entryRuleMult() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMult = null;


        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:343:2: (iv_ruleMult= ruleMult EOF )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:344:2: iv_ruleMult= ruleMult EOF
            {
             newCompositeNode(grammarAccess.getMultRule()); 
            pushFollow(FOLLOW_ruleMult_in_entryRuleMult744);
            iv_ruleMult=ruleMult();

            state._fsp--;

             current =iv_ruleMult; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleMult754); 

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
    // $ANTLR end "entryRuleMult"


    // $ANTLR start "ruleMult"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:351:1: ruleMult returns [EObject current=null] : ( ( (lv_left_0_0= ruleLiteral ) ) otherlv_1= '*' ( (lv_right_2_0= ruleLiteral ) ) ) ;
    public final EObject ruleMult() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        EObject lv_left_0_0 = null;

        EObject lv_right_2_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:354:28: ( ( ( (lv_left_0_0= ruleLiteral ) ) otherlv_1= '*' ( (lv_right_2_0= ruleLiteral ) ) ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:355:1: ( ( (lv_left_0_0= ruleLiteral ) ) otherlv_1= '*' ( (lv_right_2_0= ruleLiteral ) ) )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:355:1: ( ( (lv_left_0_0= ruleLiteral ) ) otherlv_1= '*' ( (lv_right_2_0= ruleLiteral ) ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:355:2: ( (lv_left_0_0= ruleLiteral ) ) otherlv_1= '*' ( (lv_right_2_0= ruleLiteral ) )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:355:2: ( (lv_left_0_0= ruleLiteral ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:356:1: (lv_left_0_0= ruleLiteral )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:356:1: (lv_left_0_0= ruleLiteral )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:357:3: lv_left_0_0= ruleLiteral
            {
             
            	        newCompositeNode(grammarAccess.getMultAccess().getLeftLiteralParserRuleCall_0_0()); 
            	    
            pushFollow(FOLLOW_ruleLiteral_in_ruleMult800);
            lv_left_0_0=ruleLiteral();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getMultRule());
            	        }
                   		set(
                   			current, 
                   			"left",
                    		lv_left_0_0, 
                    		"Literal");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_1=(Token)match(input,15,FOLLOW_15_in_ruleMult812); 

                	newLeafNode(otherlv_1, grammarAccess.getMultAccess().getAsteriskKeyword_1());
                
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:377:1: ( (lv_right_2_0= ruleLiteral ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:378:1: (lv_right_2_0= ruleLiteral )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:378:1: (lv_right_2_0= ruleLiteral )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:379:3: lv_right_2_0= ruleLiteral
            {
             
            	        newCompositeNode(grammarAccess.getMultAccess().getRightLiteralParserRuleCall_2_0()); 
            	    
            pushFollow(FOLLOW_ruleLiteral_in_ruleMult833);
            lv_right_2_0=ruleLiteral();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getMultRule());
            	        }
                   		set(
                   			current, 
                   			"right",
                    		lv_right_2_0, 
                    		"Literal");
            	        afterParserOrEnumRuleCall();
            	    

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
    // $ANTLR end "ruleMult"


    // $ANTLR start "entryRuleFunctionCall"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:403:1: entryRuleFunctionCall returns [EObject current=null] : iv_ruleFunctionCall= ruleFunctionCall EOF ;
    public final EObject entryRuleFunctionCall() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFunctionCall = null;


        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:404:2: (iv_ruleFunctionCall= ruleFunctionCall EOF )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:405:2: iv_ruleFunctionCall= ruleFunctionCall EOF
            {
             newCompositeNode(grammarAccess.getFunctionCallRule()); 
            pushFollow(FOLLOW_ruleFunctionCall_in_entryRuleFunctionCall869);
            iv_ruleFunctionCall=ruleFunctionCall();

            state._fsp--;

             current =iv_ruleFunctionCall; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleFunctionCall879); 

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
    // $ANTLR end "entryRuleFunctionCall"


    // $ANTLR start "ruleFunctionCall"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:412:1: ruleFunctionCall returns [EObject current=null] : ( ( (lv_name_0_0= RULE_ID ) ) otherlv_1= '(' ( ( (lv_parameters_2_0= ruleExpr ) ) (otherlv_3= ',' ( (lv_parameters_4_0= ruleExpr ) ) )* )? otherlv_5= ')' ) ;
    public final EObject ruleFunctionCall() throws RecognitionException {
        EObject current = null;

        Token lv_name_0_0=null;
        Token otherlv_1=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        EObject lv_parameters_2_0 = null;

        EObject lv_parameters_4_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:415:28: ( ( ( (lv_name_0_0= RULE_ID ) ) otherlv_1= '(' ( ( (lv_parameters_2_0= ruleExpr ) ) (otherlv_3= ',' ( (lv_parameters_4_0= ruleExpr ) ) )* )? otherlv_5= ')' ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:416:1: ( ( (lv_name_0_0= RULE_ID ) ) otherlv_1= '(' ( ( (lv_parameters_2_0= ruleExpr ) ) (otherlv_3= ',' ( (lv_parameters_4_0= ruleExpr ) ) )* )? otherlv_5= ')' )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:416:1: ( ( (lv_name_0_0= RULE_ID ) ) otherlv_1= '(' ( ( (lv_parameters_2_0= ruleExpr ) ) (otherlv_3= ',' ( (lv_parameters_4_0= ruleExpr ) ) )* )? otherlv_5= ')' )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:416:2: ( (lv_name_0_0= RULE_ID ) ) otherlv_1= '(' ( ( (lv_parameters_2_0= ruleExpr ) ) (otherlv_3= ',' ( (lv_parameters_4_0= ruleExpr ) ) )* )? otherlv_5= ')'
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:416:2: ( (lv_name_0_0= RULE_ID ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:417:1: (lv_name_0_0= RULE_ID )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:417:1: (lv_name_0_0= RULE_ID )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:418:3: lv_name_0_0= RULE_ID
            {
            lv_name_0_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleFunctionCall921); 

            			newLeafNode(lv_name_0_0, grammarAccess.getFunctionCallAccess().getNameIDTerminalRuleCall_0_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getFunctionCallRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_0_0, 
                    		"ID");
            	    

            }


            }

            otherlv_1=(Token)match(input,16,FOLLOW_16_in_ruleFunctionCall938); 

                	newLeafNode(otherlv_1, grammarAccess.getFunctionCallAccess().getLeftParenthesisKeyword_1());
                
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:438:1: ( ( (lv_parameters_2_0= ruleExpr ) ) (otherlv_3= ',' ( (lv_parameters_4_0= ruleExpr ) ) )* )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( ((LA6_0>=RULE_ID && LA6_0<=RULE_INT)||(LA6_0>=19 && LA6_0<=20)) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:438:2: ( (lv_parameters_2_0= ruleExpr ) ) (otherlv_3= ',' ( (lv_parameters_4_0= ruleExpr ) ) )*
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:438:2: ( (lv_parameters_2_0= ruleExpr ) )
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:439:1: (lv_parameters_2_0= ruleExpr )
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:439:1: (lv_parameters_2_0= ruleExpr )
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:440:3: lv_parameters_2_0= ruleExpr
                    {
                     
                    	        newCompositeNode(grammarAccess.getFunctionCallAccess().getParametersExprParserRuleCall_2_0_0()); 
                    	    
                    pushFollow(FOLLOW_ruleExpr_in_ruleFunctionCall960);
                    lv_parameters_2_0=ruleExpr();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getFunctionCallRule());
                    	        }
                           		add(
                           			current, 
                           			"parameters",
                            		lv_parameters_2_0, 
                            		"Expr");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }

                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:456:2: (otherlv_3= ',' ( (lv_parameters_4_0= ruleExpr ) ) )*
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( (LA5_0==17) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:456:4: otherlv_3= ',' ( (lv_parameters_4_0= ruleExpr ) )
                    	    {
                    	    otherlv_3=(Token)match(input,17,FOLLOW_17_in_ruleFunctionCall973); 

                    	        	newLeafNode(otherlv_3, grammarAccess.getFunctionCallAccess().getCommaKeyword_2_1_0());
                    	        
                    	    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:460:1: ( (lv_parameters_4_0= ruleExpr ) )
                    	    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:461:1: (lv_parameters_4_0= ruleExpr )
                    	    {
                    	    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:461:1: (lv_parameters_4_0= ruleExpr )
                    	    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:462:3: lv_parameters_4_0= ruleExpr
                    	    {
                    	     
                    	    	        newCompositeNode(grammarAccess.getFunctionCallAccess().getParametersExprParserRuleCall_2_1_1_0()); 
                    	    	    
                    	    pushFollow(FOLLOW_ruleExpr_in_ruleFunctionCall994);
                    	    lv_parameters_4_0=ruleExpr();

                    	    state._fsp--;


                    	    	        if (current==null) {
                    	    	            current = createModelElementForParent(grammarAccess.getFunctionCallRule());
                    	    	        }
                    	           		add(
                    	           			current, 
                    	           			"parameters",
                    	            		lv_parameters_4_0, 
                    	            		"Expr");
                    	    	        afterParserOrEnumRuleCall();
                    	    	    

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop5;
                        }
                    } while (true);


                    }
                    break;

            }

            otherlv_5=(Token)match(input,18,FOLLOW_18_in_ruleFunctionCall1010); 

                	newLeafNode(otherlv_5, grammarAccess.getFunctionCallAccess().getRightParenthesisKeyword_3());
                

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
    // $ANTLR end "ruleFunctionCall"


    // $ANTLR start "entryRuleLiteral"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:490:1: entryRuleLiteral returns [EObject current=null] : iv_ruleLiteral= ruleLiteral EOF ;
    public final EObject entryRuleLiteral() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLiteral = null;


        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:491:2: (iv_ruleLiteral= ruleLiteral EOF )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:492:2: iv_ruleLiteral= ruleLiteral EOF
            {
             newCompositeNode(grammarAccess.getLiteralRule()); 
            pushFollow(FOLLOW_ruleLiteral_in_entryRuleLiteral1046);
            iv_ruleLiteral=ruleLiteral();

            state._fsp--;

             current =iv_ruleLiteral; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleLiteral1056); 

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
    // $ANTLR end "entryRuleLiteral"


    // $ANTLR start "ruleLiteral"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:499:1: ruleLiteral returns [EObject current=null] : ( ( () ( (lv_intVal_1_0= RULE_INT ) ) ) | ( () ( ( (lv_boolVal_3_1= 'true' | lv_boolVal_3_2= 'false' ) ) ) ) ) ;
    public final EObject ruleLiteral() throws RecognitionException {
        EObject current = null;

        Token lv_intVal_1_0=null;
        Token lv_boolVal_3_1=null;
        Token lv_boolVal_3_2=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:502:28: ( ( ( () ( (lv_intVal_1_0= RULE_INT ) ) ) | ( () ( ( (lv_boolVal_3_1= 'true' | lv_boolVal_3_2= 'false' ) ) ) ) ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:503:1: ( ( () ( (lv_intVal_1_0= RULE_INT ) ) ) | ( () ( ( (lv_boolVal_3_1= 'true' | lv_boolVal_3_2= 'false' ) ) ) ) )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:503:1: ( ( () ( (lv_intVal_1_0= RULE_INT ) ) ) | ( () ( ( (lv_boolVal_3_1= 'true' | lv_boolVal_3_2= 'false' ) ) ) ) )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==RULE_INT) ) {
                alt8=1;
            }
            else if ( ((LA8_0>=19 && LA8_0<=20)) ) {
                alt8=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:503:2: ( () ( (lv_intVal_1_0= RULE_INT ) ) )
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:503:2: ( () ( (lv_intVal_1_0= RULE_INT ) ) )
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:503:3: () ( (lv_intVal_1_0= RULE_INT ) )
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:503:3: ()
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:504:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getLiteralAccess().getIntLiteralAction_0_0(),
                                current);
                        

                    }

                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:509:2: ( (lv_intVal_1_0= RULE_INT ) )
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:510:1: (lv_intVal_1_0= RULE_INT )
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:510:1: (lv_intVal_1_0= RULE_INT )
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:511:3: lv_intVal_1_0= RULE_INT
                    {
                    lv_intVal_1_0=(Token)match(input,RULE_INT,FOLLOW_RULE_INT_in_ruleLiteral1108); 

                    			newLeafNode(lv_intVal_1_0, grammarAccess.getLiteralAccess().getIntValINTTerminalRuleCall_0_1_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getLiteralRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"intVal",
                            		lv_intVal_1_0, 
                            		"INT");
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:528:6: ( () ( ( (lv_boolVal_3_1= 'true' | lv_boolVal_3_2= 'false' ) ) ) )
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:528:6: ( () ( ( (lv_boolVal_3_1= 'true' | lv_boolVal_3_2= 'false' ) ) ) )
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:528:7: () ( ( (lv_boolVal_3_1= 'true' | lv_boolVal_3_2= 'false' ) ) )
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:528:7: ()
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:529:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getLiteralAccess().getBoolLiteralAction_1_0(),
                                current);
                        

                    }

                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:534:2: ( ( (lv_boolVal_3_1= 'true' | lv_boolVal_3_2= 'false' ) ) )
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:535:1: ( (lv_boolVal_3_1= 'true' | lv_boolVal_3_2= 'false' ) )
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:535:1: ( (lv_boolVal_3_1= 'true' | lv_boolVal_3_2= 'false' ) )
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:536:1: (lv_boolVal_3_1= 'true' | lv_boolVal_3_2= 'false' )
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:536:1: (lv_boolVal_3_1= 'true' | lv_boolVal_3_2= 'false' )
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0==19) ) {
                        alt7=1;
                    }
                    else if ( (LA7_0==20) ) {
                        alt7=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 7, 0, input);

                        throw nvae;
                    }
                    switch (alt7) {
                        case 1 :
                            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:537:3: lv_boolVal_3_1= 'true'
                            {
                            lv_boolVal_3_1=(Token)match(input,19,FOLLOW_19_in_ruleLiteral1150); 

                                    newLeafNode(lv_boolVal_3_1, grammarAccess.getLiteralAccess().getBoolValTrueKeyword_1_1_0_0());
                                

                            	        if (current==null) {
                            	            current = createModelElement(grammarAccess.getLiteralRule());
                            	        }
                                   		setWithLastConsumed(current, "boolVal", lv_boolVal_3_1, null);
                            	    

                            }
                            break;
                        case 2 :
                            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:549:8: lv_boolVal_3_2= 'false'
                            {
                            lv_boolVal_3_2=(Token)match(input,20,FOLLOW_20_in_ruleLiteral1179); 

                                    newLeafNode(lv_boolVal_3_2, grammarAccess.getLiteralAccess().getBoolValFalseKeyword_1_1_0_1());
                                

                            	        if (current==null) {
                            	            current = createModelElement(grammarAccess.getLiteralRule());
                            	        }
                                   		setWithLastConsumed(current, "boolVal", lv_boolVal_3_2, null);
                            	    

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
    // $ANTLR end "ruleLiteral"


    // $ANTLR start "entryRuleNativeDef"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:572:1: entryRuleNativeDef returns [EObject current=null] : iv_ruleNativeDef= ruleNativeDef EOF ;
    public final EObject entryRuleNativeDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleNativeDef = null;


        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:573:2: (iv_ruleNativeDef= ruleNativeDef EOF )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:574:2: iv_ruleNativeDef= ruleNativeDef EOF
            {
             newCompositeNode(grammarAccess.getNativeDefRule()); 
            pushFollow(FOLLOW_ruleNativeDef_in_entryRuleNativeDef1232);
            iv_ruleNativeDef=ruleNativeDef();

            state._fsp--;

             current =iv_ruleNativeDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleNativeDef1242); 

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
    // $ANTLR end "entryRuleNativeDef"


    // $ANTLR start "ruleNativeDef"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:581:1: ruleNativeDef returns [EObject current=null] : ( (otherlv_0= 'constant' )? otherlv_1= 'native' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= 'takes' ( (lv_params_4_0= ruleFormalParameters ) ) otherlv_5= 'returns' ( (lv_returnType_6_0= ruleReturnType ) ) ) ;
    public final EObject ruleNativeDef() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        EObject lv_params_4_0 = null;

        EObject lv_returnType_6_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:584:28: ( ( (otherlv_0= 'constant' )? otherlv_1= 'native' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= 'takes' ( (lv_params_4_0= ruleFormalParameters ) ) otherlv_5= 'returns' ( (lv_returnType_6_0= ruleReturnType ) ) ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:585:1: ( (otherlv_0= 'constant' )? otherlv_1= 'native' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= 'takes' ( (lv_params_4_0= ruleFormalParameters ) ) otherlv_5= 'returns' ( (lv_returnType_6_0= ruleReturnType ) ) )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:585:1: ( (otherlv_0= 'constant' )? otherlv_1= 'native' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= 'takes' ( (lv_params_4_0= ruleFormalParameters ) ) otherlv_5= 'returns' ( (lv_returnType_6_0= ruleReturnType ) ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:585:2: (otherlv_0= 'constant' )? otherlv_1= 'native' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= 'takes' ( (lv_params_4_0= ruleFormalParameters ) ) otherlv_5= 'returns' ( (lv_returnType_6_0= ruleReturnType ) )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:585:2: (otherlv_0= 'constant' )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==13) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:585:4: otherlv_0= 'constant'
                    {
                    otherlv_0=(Token)match(input,13,FOLLOW_13_in_ruleNativeDef1280); 

                        	newLeafNode(otherlv_0, grammarAccess.getNativeDefAccess().getConstantKeyword_0());
                        

                    }
                    break;

            }

            otherlv_1=(Token)match(input,21,FOLLOW_21_in_ruleNativeDef1294); 

                	newLeafNode(otherlv_1, grammarAccess.getNativeDefAccess().getNativeKeyword_1());
                
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:593:1: ( (lv_name_2_0= RULE_ID ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:594:1: (lv_name_2_0= RULE_ID )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:594:1: (lv_name_2_0= RULE_ID )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:595:3: lv_name_2_0= RULE_ID
            {
            lv_name_2_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleNativeDef1311); 

            			newLeafNode(lv_name_2_0, grammarAccess.getNativeDefAccess().getNameIDTerminalRuleCall_2_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getNativeDefRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_2_0, 
                    		"ID");
            	    

            }


            }

            otherlv_3=(Token)match(input,22,FOLLOW_22_in_ruleNativeDef1328); 

                	newLeafNode(otherlv_3, grammarAccess.getNativeDefAccess().getTakesKeyword_3());
                
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:615:1: ( (lv_params_4_0= ruleFormalParameters ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:616:1: (lv_params_4_0= ruleFormalParameters )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:616:1: (lv_params_4_0= ruleFormalParameters )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:617:3: lv_params_4_0= ruleFormalParameters
            {
             
            	        newCompositeNode(grammarAccess.getNativeDefAccess().getParamsFormalParametersParserRuleCall_4_0()); 
            	    
            pushFollow(FOLLOW_ruleFormalParameters_in_ruleNativeDef1349);
            lv_params_4_0=ruleFormalParameters();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getNativeDefRule());
            	        }
                   		set(
                   			current, 
                   			"params",
                    		lv_params_4_0, 
                    		"FormalParameters");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }

            otherlv_5=(Token)match(input,23,FOLLOW_23_in_ruleNativeDef1361); 

                	newLeafNode(otherlv_5, grammarAccess.getNativeDefAccess().getReturnsKeyword_5());
                
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:637:1: ( (lv_returnType_6_0= ruleReturnType ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:638:1: (lv_returnType_6_0= ruleReturnType )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:638:1: (lv_returnType_6_0= ruleReturnType )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:639:3: lv_returnType_6_0= ruleReturnType
            {
             
            	        newCompositeNode(grammarAccess.getNativeDefAccess().getReturnTypeReturnTypeParserRuleCall_6_0()); 
            	    
            pushFollow(FOLLOW_ruleReturnType_in_ruleNativeDef1382);
            lv_returnType_6_0=ruleReturnType();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getNativeDefRule());
            	        }
                   		set(
                   			current, 
                   			"returnType",
                    		lv_returnType_6_0, 
                    		"ReturnType");
            	        afterParserOrEnumRuleCall();
            	    

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
    // $ANTLR end "ruleNativeDef"


    // $ANTLR start "entryRuleReturnType"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:663:1: entryRuleReturnType returns [EObject current=null] : iv_ruleReturnType= ruleReturnType EOF ;
    public final EObject entryRuleReturnType() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleReturnType = null;


        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:664:2: (iv_ruleReturnType= ruleReturnType EOF )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:665:2: iv_ruleReturnType= ruleReturnType EOF
            {
             newCompositeNode(grammarAccess.getReturnTypeRule()); 
            pushFollow(FOLLOW_ruleReturnType_in_entryRuleReturnType1418);
            iv_ruleReturnType=ruleReturnType();

            state._fsp--;

             current =iv_ruleReturnType; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleReturnType1428); 

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
    // $ANTLR end "entryRuleReturnType"


    // $ANTLR start "ruleReturnType"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:672:1: ruleReturnType returns [EObject current=null] : ( ( () otherlv_1= 'nothing' ) | ( (lv_name_2_0= RULE_ID ) ) ) ;
    public final EObject ruleReturnType() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token lv_name_2_0=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:675:28: ( ( ( () otherlv_1= 'nothing' ) | ( (lv_name_2_0= RULE_ID ) ) ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:676:1: ( ( () otherlv_1= 'nothing' ) | ( (lv_name_2_0= RULE_ID ) ) )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:676:1: ( ( () otherlv_1= 'nothing' ) | ( (lv_name_2_0= RULE_ID ) ) )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==24) ) {
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
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:676:2: ( () otherlv_1= 'nothing' )
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:676:2: ( () otherlv_1= 'nothing' )
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:676:3: () otherlv_1= 'nothing'
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:676:3: ()
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:677:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getReturnTypeAccess().getReturnsNothingAction_0_0(),
                                current);
                        

                    }

                    otherlv_1=(Token)match(input,24,FOLLOW_24_in_ruleReturnType1475); 

                        	newLeafNode(otherlv_1, grammarAccess.getReturnTypeAccess().getNothingKeyword_0_1());
                        

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:687:6: ( (lv_name_2_0= RULE_ID ) )
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:687:6: ( (lv_name_2_0= RULE_ID ) )
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:688:1: (lv_name_2_0= RULE_ID )
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:688:1: (lv_name_2_0= RULE_ID )
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:689:3: lv_name_2_0= RULE_ID
                    {
                    lv_name_2_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleReturnType1499); 

                    			newLeafNode(lv_name_2_0, grammarAccess.getReturnTypeAccess().getNameIDTerminalRuleCall_1_0()); 
                    		

                    	        if (current==null) {
                    	            current = createModelElement(grammarAccess.getReturnTypeRule());
                    	        }
                           		setWithLastConsumed(
                           			current, 
                           			"name",
                            		lv_name_2_0, 
                            		"ID");
                    	    

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
    // $ANTLR end "ruleReturnType"


    // $ANTLR start "entryRuleFormalParameters"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:713:1: entryRuleFormalParameters returns [EObject current=null] : iv_ruleFormalParameters= ruleFormalParameters EOF ;
    public final EObject entryRuleFormalParameters() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFormalParameters = null;


        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:714:2: (iv_ruleFormalParameters= ruleFormalParameters EOF )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:715:2: iv_ruleFormalParameters= ruleFormalParameters EOF
            {
             newCompositeNode(grammarAccess.getFormalParametersRule()); 
            pushFollow(FOLLOW_ruleFormalParameters_in_entryRuleFormalParameters1540);
            iv_ruleFormalParameters=ruleFormalParameters();

            state._fsp--;

             current =iv_ruleFormalParameters; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleFormalParameters1550); 

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
    // $ANTLR end "entryRuleFormalParameters"


    // $ANTLR start "ruleFormalParameters"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:722:1: ruleFormalParameters returns [EObject current=null] : ( ( () otherlv_1= 'nothing' ) | ( ( (lv_parameters_2_0= ruleFormalParameter ) ) (otherlv_3= ',' ( (lv_parameters_4_0= ruleFormalParameter ) ) )* ) ) ;
    public final EObject ruleFormalParameters() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_parameters_2_0 = null;

        EObject lv_parameters_4_0 = null;


         enterRule(); 
            
        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:725:28: ( ( ( () otherlv_1= 'nothing' ) | ( ( (lv_parameters_2_0= ruleFormalParameter ) ) (otherlv_3= ',' ( (lv_parameters_4_0= ruleFormalParameter ) ) )* ) ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:726:1: ( ( () otherlv_1= 'nothing' ) | ( ( (lv_parameters_2_0= ruleFormalParameter ) ) (otherlv_3= ',' ( (lv_parameters_4_0= ruleFormalParameter ) ) )* ) )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:726:1: ( ( () otherlv_1= 'nothing' ) | ( ( (lv_parameters_2_0= ruleFormalParameter ) ) (otherlv_3= ',' ( (lv_parameters_4_0= ruleFormalParameter ) ) )* ) )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==24) ) {
                alt12=1;
            }
            else if ( (LA12_0==RULE_ID) ) {
                alt12=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:726:2: ( () otherlv_1= 'nothing' )
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:726:2: ( () otherlv_1= 'nothing' )
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:726:3: () otherlv_1= 'nothing'
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:726:3: ()
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:727:5: 
                    {

                            current = forceCreateModelElement(
                                grammarAccess.getFormalParametersAccess().getParamNothingAction_0_0(),
                                current);
                        

                    }

                    otherlv_1=(Token)match(input,24,FOLLOW_24_in_ruleFormalParameters1597); 

                        	newLeafNode(otherlv_1, grammarAccess.getFormalParametersAccess().getNothingKeyword_0_1());
                        

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:737:6: ( ( (lv_parameters_2_0= ruleFormalParameter ) ) (otherlv_3= ',' ( (lv_parameters_4_0= ruleFormalParameter ) ) )* )
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:737:6: ( ( (lv_parameters_2_0= ruleFormalParameter ) ) (otherlv_3= ',' ( (lv_parameters_4_0= ruleFormalParameter ) ) )* )
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:737:7: ( (lv_parameters_2_0= ruleFormalParameter ) ) (otherlv_3= ',' ( (lv_parameters_4_0= ruleFormalParameter ) ) )*
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:737:7: ( (lv_parameters_2_0= ruleFormalParameter ) )
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:738:1: (lv_parameters_2_0= ruleFormalParameter )
                    {
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:738:1: (lv_parameters_2_0= ruleFormalParameter )
                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:739:3: lv_parameters_2_0= ruleFormalParameter
                    {
                     
                    	        newCompositeNode(grammarAccess.getFormalParametersAccess().getParametersFormalParameterParserRuleCall_1_0_0()); 
                    	    
                    pushFollow(FOLLOW_ruleFormalParameter_in_ruleFormalParameters1626);
                    lv_parameters_2_0=ruleFormalParameter();

                    state._fsp--;


                    	        if (current==null) {
                    	            current = createModelElementForParent(grammarAccess.getFormalParametersRule());
                    	        }
                           		add(
                           			current, 
                           			"parameters",
                            		lv_parameters_2_0, 
                            		"FormalParameter");
                    	        afterParserOrEnumRuleCall();
                    	    

                    }


                    }

                    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:755:2: (otherlv_3= ',' ( (lv_parameters_4_0= ruleFormalParameter ) ) )*
                    loop11:
                    do {
                        int alt11=2;
                        int LA11_0 = input.LA(1);

                        if ( (LA11_0==17) ) {
                            alt11=1;
                        }


                        switch (alt11) {
                    	case 1 :
                    	    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:755:4: otherlv_3= ',' ( (lv_parameters_4_0= ruleFormalParameter ) )
                    	    {
                    	    otherlv_3=(Token)match(input,17,FOLLOW_17_in_ruleFormalParameters1639); 

                    	        	newLeafNode(otherlv_3, grammarAccess.getFormalParametersAccess().getCommaKeyword_1_1_0());
                    	        
                    	    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:759:1: ( (lv_parameters_4_0= ruleFormalParameter ) )
                    	    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:760:1: (lv_parameters_4_0= ruleFormalParameter )
                    	    {
                    	    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:760:1: (lv_parameters_4_0= ruleFormalParameter )
                    	    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:761:3: lv_parameters_4_0= ruleFormalParameter
                    	    {
                    	     
                    	    	        newCompositeNode(grammarAccess.getFormalParametersAccess().getParametersFormalParameterParserRuleCall_1_1_1_0()); 
                    	    	    
                    	    pushFollow(FOLLOW_ruleFormalParameter_in_ruleFormalParameters1660);
                    	    lv_parameters_4_0=ruleFormalParameter();

                    	    state._fsp--;


                    	    	        if (current==null) {
                    	    	            current = createModelElementForParent(grammarAccess.getFormalParametersRule());
                    	    	        }
                    	           		add(
                    	           			current, 
                    	           			"parameters",
                    	            		lv_parameters_4_0, 
                    	            		"FormalParameter");
                    	    	        afterParserOrEnumRuleCall();
                    	    	    

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop11;
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
    // $ANTLR end "ruleFormalParameters"


    // $ANTLR start "entryRuleFormalParameter"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:785:1: entryRuleFormalParameter returns [EObject current=null] : iv_ruleFormalParameter= ruleFormalParameter EOF ;
    public final EObject entryRuleFormalParameter() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFormalParameter = null;


        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:786:2: (iv_ruleFormalParameter= ruleFormalParameter EOF )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:787:2: iv_ruleFormalParameter= ruleFormalParameter EOF
            {
             newCompositeNode(grammarAccess.getFormalParameterRule()); 
            pushFollow(FOLLOW_ruleFormalParameter_in_entryRuleFormalParameter1699);
            iv_ruleFormalParameter=ruleFormalParameter();

            state._fsp--;

             current =iv_ruleFormalParameter; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleFormalParameter1709); 

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
    // $ANTLR end "entryRuleFormalParameter"


    // $ANTLR start "ruleFormalParameter"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:794:1: ruleFormalParameter returns [EObject current=null] : ( ( (lv_type_0_0= RULE_ID ) ) ( (lv_name_1_0= RULE_ID ) ) ) ;
    public final EObject ruleFormalParameter() throws RecognitionException {
        EObject current = null;

        Token lv_type_0_0=null;
        Token lv_name_1_0=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:797:28: ( ( ( (lv_type_0_0= RULE_ID ) ) ( (lv_name_1_0= RULE_ID ) ) ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:798:1: ( ( (lv_type_0_0= RULE_ID ) ) ( (lv_name_1_0= RULE_ID ) ) )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:798:1: ( ( (lv_type_0_0= RULE_ID ) ) ( (lv_name_1_0= RULE_ID ) ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:798:2: ( (lv_type_0_0= RULE_ID ) ) ( (lv_name_1_0= RULE_ID ) )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:798:2: ( (lv_type_0_0= RULE_ID ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:799:1: (lv_type_0_0= RULE_ID )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:799:1: (lv_type_0_0= RULE_ID )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:800:3: lv_type_0_0= RULE_ID
            {
            lv_type_0_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleFormalParameter1751); 

            			newLeafNode(lv_type_0_0, grammarAccess.getFormalParameterAccess().getTypeIDTerminalRuleCall_0_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getFormalParameterRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"type",
                    		lv_type_0_0, 
                    		"ID");
            	    

            }


            }

            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:816:2: ( (lv_name_1_0= RULE_ID ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:817:1: (lv_name_1_0= RULE_ID )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:817:1: (lv_name_1_0= RULE_ID )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:818:3: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleFormalParameter1773); 

            			newLeafNode(lv_name_1_0, grammarAccess.getFormalParameterAccess().getNameIDTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getFormalParameterRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_1_0, 
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
    // $ANTLR end "ruleFormalParameter"


    // $ANTLR start "entryRuleTypeDef"
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:842:1: entryRuleTypeDef returns [EObject current=null] : iv_ruleTypeDef= ruleTypeDef EOF ;
    public final EObject entryRuleTypeDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeDef = null;


        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:843:2: (iv_ruleTypeDef= ruleTypeDef EOF )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:844:2: iv_ruleTypeDef= ruleTypeDef EOF
            {
             newCompositeNode(grammarAccess.getTypeDefRule()); 
            pushFollow(FOLLOW_ruleTypeDef_in_entryRuleTypeDef1814);
            iv_ruleTypeDef=ruleTypeDef();

            state._fsp--;

             current =iv_ruleTypeDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleTypeDef1824); 

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
    // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:851:1: ruleTypeDef returns [EObject current=null] : (otherlv_0= 'type' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= 'extends' ( (lv_extendName_3_0= RULE_ID ) ) ) ;
    public final EObject ruleTypeDef() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token lv_extendName_3_0=null;

         enterRule(); 
            
        try {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:854:28: ( (otherlv_0= 'type' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= 'extends' ( (lv_extendName_3_0= RULE_ID ) ) ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:855:1: (otherlv_0= 'type' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= 'extends' ( (lv_extendName_3_0= RULE_ID ) ) )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:855:1: (otherlv_0= 'type' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= 'extends' ( (lv_extendName_3_0= RULE_ID ) ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:855:3: otherlv_0= 'type' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= 'extends' ( (lv_extendName_3_0= RULE_ID ) )
            {
            otherlv_0=(Token)match(input,25,FOLLOW_25_in_ruleTypeDef1861); 

                	newLeafNode(otherlv_0, grammarAccess.getTypeDefAccess().getTypeKeyword_0());
                
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:859:1: ( (lv_name_1_0= RULE_ID ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:860:1: (lv_name_1_0= RULE_ID )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:860:1: (lv_name_1_0= RULE_ID )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:861:3: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleTypeDef1878); 

            			newLeafNode(lv_name_1_0, grammarAccess.getTypeDefAccess().getNameIDTerminalRuleCall_1_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getTypeDefRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_1_0, 
                    		"ID");
            	    

            }


            }

            otherlv_2=(Token)match(input,26,FOLLOW_26_in_ruleTypeDef1895); 

                	newLeafNode(otherlv_2, grammarAccess.getTypeDefAccess().getExtendsKeyword_2());
                
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:881:1: ( (lv_extendName_3_0= RULE_ID ) )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:882:1: (lv_extendName_3_0= RULE_ID )
            {
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:882:1: (lv_extendName_3_0= RULE_ID )
            // ../de.peeeq.jassToPscript/src-gen/de/peeeq/parser/antlr/internal/InternalJassToPscript.g:883:3: lv_extendName_3_0= RULE_ID
            {
            lv_extendName_3_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleTypeDef1912); 

            			newLeafNode(lv_extendName_3_0, grammarAccess.getTypeDefAccess().getExtendNameIDTerminalRuleCall_3_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getTypeDefRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"extendName",
                    		lv_extendName_3_0, 
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
    // $ANTLR end "ruleTypeDef"

    // Delegated rules


 

    public static final BitSet FOLLOW_ruleProg_in_entryRuleProg75 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleProg85 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleEntity_in_ruleProg130 = new BitSet(new long[]{0x0000000002202802L});
    public static final BitSet FOLLOW_ruleEntity_in_entryRuleEntity166 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleEntity176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleTypeDef_in_ruleEntity223 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleGlobalBlock_in_ruleEntity250 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNativeDef_in_ruleEntity277 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleGlobalBlock_in_entryRuleGlobalBlock312 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleGlobalBlock322 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_11_in_ruleGlobalBlock368 = new BitSet(new long[]{0x0000000000003000L});
    public static final BitSet FOLLOW_ruleConstant_in_ruleGlobalBlock389 = new BitSet(new long[]{0x0000000000003000L});
    public static final BitSet FOLLOW_12_in_ruleGlobalBlock402 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleConstant_in_entryRuleConstant438 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleConstant448 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_13_in_ruleConstant485 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleConstant502 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleConstant524 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_ruleConstant541 = new BitSet(new long[]{0x0000000000180030L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleConstant562 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExpr_in_entryRuleExpr598 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExpr608 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleLiteral_in_ruleExpr655 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFunctionCall_in_ruleExpr682 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleMult_in_ruleExpr709 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleMult_in_entryRuleMult744 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleMult754 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleLiteral_in_ruleMult800 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_ruleMult812 = new BitSet(new long[]{0x0000000000180020L});
    public static final BitSet FOLLOW_ruleLiteral_in_ruleMult833 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFunctionCall_in_entryRuleFunctionCall869 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleFunctionCall879 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleFunctionCall921 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_16_in_ruleFunctionCall938 = new BitSet(new long[]{0x00000000001C0030L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleFunctionCall960 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_17_in_ruleFunctionCall973 = new BitSet(new long[]{0x0000000000180030L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleFunctionCall994 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_18_in_ruleFunctionCall1010 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleLiteral_in_entryRuleLiteral1046 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleLiteral1056 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_INT_in_ruleLiteral1108 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_19_in_ruleLiteral1150 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_20_in_ruleLiteral1179 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNativeDef_in_entryRuleNativeDef1232 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleNativeDef1242 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_13_in_ruleNativeDef1280 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_21_in_ruleNativeDef1294 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleNativeDef1311 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_ruleNativeDef1328 = new BitSet(new long[]{0x0000000001000010L});
    public static final BitSet FOLLOW_ruleFormalParameters_in_ruleNativeDef1349 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_ruleNativeDef1361 = new BitSet(new long[]{0x0000000001000010L});
    public static final BitSet FOLLOW_ruleReturnType_in_ruleNativeDef1382 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleReturnType_in_entryRuleReturnType1418 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleReturnType1428 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_ruleReturnType1475 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleReturnType1499 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFormalParameters_in_entryRuleFormalParameters1540 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleFormalParameters1550 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_ruleFormalParameters1597 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFormalParameter_in_ruleFormalParameters1626 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_17_in_ruleFormalParameters1639 = new BitSet(new long[]{0x0000000001000010L});
    public static final BitSet FOLLOW_ruleFormalParameter_in_ruleFormalParameters1660 = new BitSet(new long[]{0x0000000000020002L});
    public static final BitSet FOLLOW_ruleFormalParameter_in_entryRuleFormalParameter1699 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleFormalParameter1709 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleFormalParameter1751 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleFormalParameter1773 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleTypeDef_in_entryRuleTypeDef1814 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleTypeDef1824 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_25_in_ruleTypeDef1861 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleTypeDef1878 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_ruleTypeDef1895 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleTypeDef1912 = new BitSet(new long[]{0x0000000000000002L});

}