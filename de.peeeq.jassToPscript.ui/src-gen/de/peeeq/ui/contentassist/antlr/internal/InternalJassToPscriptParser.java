package de.peeeq.ui.contentassist.antlr.internal; 

import java.io.InputStream;
import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.xtext.parsetree.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.ui.editor.contentassist.antlr.internal.AbstractInternalContentAssistParser;
import org.eclipse.xtext.ui.editor.contentassist.antlr.internal.DFA;
import de.peeeq.services.JassToPscriptGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalJassToPscriptParser extends AbstractInternalContentAssistParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_INT", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'true'", "'false'", "'globals'", "'endglobals'", "'constant'", "'='", "'*'", "'('", "')'", "','", "'native'", "'takes'", "'returns'", "'nothing'", "'type'", "'extends'"
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
    public String getGrammarFileName() { return "../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g"; }


     
     	private JassToPscriptGrammarAccess grammarAccess;
     	
        public void setGrammarAccess(JassToPscriptGrammarAccess grammarAccess) {
        	this.grammarAccess = grammarAccess;
        }
        
        @Override
        protected Grammar getGrammar() {
        	return grammarAccess.getGrammar();
        }
        
        @Override
        protected String getValueForTokenName(String tokenName) {
        	return tokenName;
        }




    // $ANTLR start "entryRuleProg"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:61:1: entryRuleProg : ruleProg EOF ;
    public final void entryRuleProg() throws RecognitionException {
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:62:1: ( ruleProg EOF )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:63:1: ruleProg EOF
            {
             before(grammarAccess.getProgRule()); 
            pushFollow(FOLLOW_ruleProg_in_entryRuleProg61);
            ruleProg();

            state._fsp--;

             after(grammarAccess.getProgRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleProg68); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleProg"


    // $ANTLR start "ruleProg"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:70:1: ruleProg : ( ( rule__Prog__ElemsAssignment )* ) ;
    public final void ruleProg() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:74:2: ( ( ( rule__Prog__ElemsAssignment )* ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:75:1: ( ( rule__Prog__ElemsAssignment )* )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:75:1: ( ( rule__Prog__ElemsAssignment )* )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:76:1: ( rule__Prog__ElemsAssignment )*
            {
             before(grammarAccess.getProgAccess().getElemsAssignment()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:77:1: ( rule__Prog__ElemsAssignment )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==13||LA1_0==15||LA1_0==21||LA1_0==25) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:77:2: rule__Prog__ElemsAssignment
            	    {
            	    pushFollow(FOLLOW_rule__Prog__ElemsAssignment_in_ruleProg94);
            	    rule__Prog__ElemsAssignment();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

             after(grammarAccess.getProgAccess().getElemsAssignment()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleProg"


    // $ANTLR start "entryRuleEntity"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:89:1: entryRuleEntity : ruleEntity EOF ;
    public final void entryRuleEntity() throws RecognitionException {
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:90:1: ( ruleEntity EOF )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:91:1: ruleEntity EOF
            {
             before(grammarAccess.getEntityRule()); 
            pushFollow(FOLLOW_ruleEntity_in_entryRuleEntity122);
            ruleEntity();

            state._fsp--;

             after(grammarAccess.getEntityRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleEntity129); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleEntity"


    // $ANTLR start "ruleEntity"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:98:1: ruleEntity : ( ( rule__Entity__Alternatives ) ) ;
    public final void ruleEntity() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:102:2: ( ( ( rule__Entity__Alternatives ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:103:1: ( ( rule__Entity__Alternatives ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:103:1: ( ( rule__Entity__Alternatives ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:104:1: ( rule__Entity__Alternatives )
            {
             before(grammarAccess.getEntityAccess().getAlternatives()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:105:1: ( rule__Entity__Alternatives )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:105:2: rule__Entity__Alternatives
            {
            pushFollow(FOLLOW_rule__Entity__Alternatives_in_ruleEntity155);
            rule__Entity__Alternatives();

            state._fsp--;


            }

             after(grammarAccess.getEntityAccess().getAlternatives()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleEntity"


    // $ANTLR start "entryRuleGlobalBlock"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:117:1: entryRuleGlobalBlock : ruleGlobalBlock EOF ;
    public final void entryRuleGlobalBlock() throws RecognitionException {
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:118:1: ( ruleGlobalBlock EOF )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:119:1: ruleGlobalBlock EOF
            {
             before(grammarAccess.getGlobalBlockRule()); 
            pushFollow(FOLLOW_ruleGlobalBlock_in_entryRuleGlobalBlock182);
            ruleGlobalBlock();

            state._fsp--;

             after(grammarAccess.getGlobalBlockRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleGlobalBlock189); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleGlobalBlock"


    // $ANTLR start "ruleGlobalBlock"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:126:1: ruleGlobalBlock : ( ( rule__GlobalBlock__Group__0 ) ) ;
    public final void ruleGlobalBlock() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:130:2: ( ( ( rule__GlobalBlock__Group__0 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:131:1: ( ( rule__GlobalBlock__Group__0 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:131:1: ( ( rule__GlobalBlock__Group__0 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:132:1: ( rule__GlobalBlock__Group__0 )
            {
             before(grammarAccess.getGlobalBlockAccess().getGroup()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:133:1: ( rule__GlobalBlock__Group__0 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:133:2: rule__GlobalBlock__Group__0
            {
            pushFollow(FOLLOW_rule__GlobalBlock__Group__0_in_ruleGlobalBlock215);
            rule__GlobalBlock__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getGlobalBlockAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleGlobalBlock"


    // $ANTLR start "entryRuleConstant"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:145:1: entryRuleConstant : ruleConstant EOF ;
    public final void entryRuleConstant() throws RecognitionException {
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:146:1: ( ruleConstant EOF )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:147:1: ruleConstant EOF
            {
             before(grammarAccess.getConstantRule()); 
            pushFollow(FOLLOW_ruleConstant_in_entryRuleConstant242);
            ruleConstant();

            state._fsp--;

             after(grammarAccess.getConstantRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleConstant249); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleConstant"


    // $ANTLR start "ruleConstant"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:154:1: ruleConstant : ( ( rule__Constant__Group__0 ) ) ;
    public final void ruleConstant() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:158:2: ( ( ( rule__Constant__Group__0 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:159:1: ( ( rule__Constant__Group__0 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:159:1: ( ( rule__Constant__Group__0 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:160:1: ( rule__Constant__Group__0 )
            {
             before(grammarAccess.getConstantAccess().getGroup()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:161:1: ( rule__Constant__Group__0 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:161:2: rule__Constant__Group__0
            {
            pushFollow(FOLLOW_rule__Constant__Group__0_in_ruleConstant275);
            rule__Constant__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getConstantAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleConstant"


    // $ANTLR start "entryRuleExpr"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:173:1: entryRuleExpr : ruleExpr EOF ;
    public final void entryRuleExpr() throws RecognitionException {
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:174:1: ( ruleExpr EOF )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:175:1: ruleExpr EOF
            {
             before(grammarAccess.getExprRule()); 
            pushFollow(FOLLOW_ruleExpr_in_entryRuleExpr302);
            ruleExpr();

            state._fsp--;

             after(grammarAccess.getExprRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExpr309); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleExpr"


    // $ANTLR start "ruleExpr"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:182:1: ruleExpr : ( ( rule__Expr__Alternatives ) ) ;
    public final void ruleExpr() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:186:2: ( ( ( rule__Expr__Alternatives ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:187:1: ( ( rule__Expr__Alternatives ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:187:1: ( ( rule__Expr__Alternatives ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:188:1: ( rule__Expr__Alternatives )
            {
             before(grammarAccess.getExprAccess().getAlternatives()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:189:1: ( rule__Expr__Alternatives )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:189:2: rule__Expr__Alternatives
            {
            pushFollow(FOLLOW_rule__Expr__Alternatives_in_ruleExpr335);
            rule__Expr__Alternatives();

            state._fsp--;


            }

             after(grammarAccess.getExprAccess().getAlternatives()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleExpr"


    // $ANTLR start "entryRuleMult"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:201:1: entryRuleMult : ruleMult EOF ;
    public final void entryRuleMult() throws RecognitionException {
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:202:1: ( ruleMult EOF )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:203:1: ruleMult EOF
            {
             before(grammarAccess.getMultRule()); 
            pushFollow(FOLLOW_ruleMult_in_entryRuleMult362);
            ruleMult();

            state._fsp--;

             after(grammarAccess.getMultRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleMult369); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleMult"


    // $ANTLR start "ruleMult"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:210:1: ruleMult : ( ( rule__Mult__Group__0 ) ) ;
    public final void ruleMult() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:214:2: ( ( ( rule__Mult__Group__0 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:215:1: ( ( rule__Mult__Group__0 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:215:1: ( ( rule__Mult__Group__0 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:216:1: ( rule__Mult__Group__0 )
            {
             before(grammarAccess.getMultAccess().getGroup()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:217:1: ( rule__Mult__Group__0 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:217:2: rule__Mult__Group__0
            {
            pushFollow(FOLLOW_rule__Mult__Group__0_in_ruleMult395);
            rule__Mult__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getMultAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleMult"


    // $ANTLR start "entryRuleFunctionCall"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:229:1: entryRuleFunctionCall : ruleFunctionCall EOF ;
    public final void entryRuleFunctionCall() throws RecognitionException {
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:230:1: ( ruleFunctionCall EOF )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:231:1: ruleFunctionCall EOF
            {
             before(grammarAccess.getFunctionCallRule()); 
            pushFollow(FOLLOW_ruleFunctionCall_in_entryRuleFunctionCall422);
            ruleFunctionCall();

            state._fsp--;

             after(grammarAccess.getFunctionCallRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleFunctionCall429); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleFunctionCall"


    // $ANTLR start "ruleFunctionCall"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:238:1: ruleFunctionCall : ( ( rule__FunctionCall__Group__0 ) ) ;
    public final void ruleFunctionCall() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:242:2: ( ( ( rule__FunctionCall__Group__0 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:243:1: ( ( rule__FunctionCall__Group__0 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:243:1: ( ( rule__FunctionCall__Group__0 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:244:1: ( rule__FunctionCall__Group__0 )
            {
             before(grammarAccess.getFunctionCallAccess().getGroup()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:245:1: ( rule__FunctionCall__Group__0 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:245:2: rule__FunctionCall__Group__0
            {
            pushFollow(FOLLOW_rule__FunctionCall__Group__0_in_ruleFunctionCall455);
            rule__FunctionCall__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getFunctionCallAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleFunctionCall"


    // $ANTLR start "entryRuleLiteral"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:257:1: entryRuleLiteral : ruleLiteral EOF ;
    public final void entryRuleLiteral() throws RecognitionException {
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:258:1: ( ruleLiteral EOF )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:259:1: ruleLiteral EOF
            {
             before(grammarAccess.getLiteralRule()); 
            pushFollow(FOLLOW_ruleLiteral_in_entryRuleLiteral482);
            ruleLiteral();

            state._fsp--;

             after(grammarAccess.getLiteralRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleLiteral489); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleLiteral"


    // $ANTLR start "ruleLiteral"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:266:1: ruleLiteral : ( ( rule__Literal__Alternatives ) ) ;
    public final void ruleLiteral() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:270:2: ( ( ( rule__Literal__Alternatives ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:271:1: ( ( rule__Literal__Alternatives ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:271:1: ( ( rule__Literal__Alternatives ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:272:1: ( rule__Literal__Alternatives )
            {
             before(grammarAccess.getLiteralAccess().getAlternatives()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:273:1: ( rule__Literal__Alternatives )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:273:2: rule__Literal__Alternatives
            {
            pushFollow(FOLLOW_rule__Literal__Alternatives_in_ruleLiteral515);
            rule__Literal__Alternatives();

            state._fsp--;


            }

             after(grammarAccess.getLiteralAccess().getAlternatives()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleLiteral"


    // $ANTLR start "entryRuleNativeDef"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:285:1: entryRuleNativeDef : ruleNativeDef EOF ;
    public final void entryRuleNativeDef() throws RecognitionException {
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:286:1: ( ruleNativeDef EOF )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:287:1: ruleNativeDef EOF
            {
             before(grammarAccess.getNativeDefRule()); 
            pushFollow(FOLLOW_ruleNativeDef_in_entryRuleNativeDef542);
            ruleNativeDef();

            state._fsp--;

             after(grammarAccess.getNativeDefRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleNativeDef549); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleNativeDef"


    // $ANTLR start "ruleNativeDef"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:294:1: ruleNativeDef : ( ( rule__NativeDef__Group__0 ) ) ;
    public final void ruleNativeDef() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:298:2: ( ( ( rule__NativeDef__Group__0 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:299:1: ( ( rule__NativeDef__Group__0 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:299:1: ( ( rule__NativeDef__Group__0 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:300:1: ( rule__NativeDef__Group__0 )
            {
             before(grammarAccess.getNativeDefAccess().getGroup()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:301:1: ( rule__NativeDef__Group__0 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:301:2: rule__NativeDef__Group__0
            {
            pushFollow(FOLLOW_rule__NativeDef__Group__0_in_ruleNativeDef575);
            rule__NativeDef__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getNativeDefAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleNativeDef"


    // $ANTLR start "entryRuleReturnType"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:313:1: entryRuleReturnType : ruleReturnType EOF ;
    public final void entryRuleReturnType() throws RecognitionException {
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:314:1: ( ruleReturnType EOF )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:315:1: ruleReturnType EOF
            {
             before(grammarAccess.getReturnTypeRule()); 
            pushFollow(FOLLOW_ruleReturnType_in_entryRuleReturnType602);
            ruleReturnType();

            state._fsp--;

             after(grammarAccess.getReturnTypeRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleReturnType609); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleReturnType"


    // $ANTLR start "ruleReturnType"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:322:1: ruleReturnType : ( ( rule__ReturnType__Alternatives ) ) ;
    public final void ruleReturnType() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:326:2: ( ( ( rule__ReturnType__Alternatives ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:327:1: ( ( rule__ReturnType__Alternatives ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:327:1: ( ( rule__ReturnType__Alternatives ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:328:1: ( rule__ReturnType__Alternatives )
            {
             before(grammarAccess.getReturnTypeAccess().getAlternatives()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:329:1: ( rule__ReturnType__Alternatives )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:329:2: rule__ReturnType__Alternatives
            {
            pushFollow(FOLLOW_rule__ReturnType__Alternatives_in_ruleReturnType635);
            rule__ReturnType__Alternatives();

            state._fsp--;


            }

             after(grammarAccess.getReturnTypeAccess().getAlternatives()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleReturnType"


    // $ANTLR start "entryRuleFormalParameters"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:341:1: entryRuleFormalParameters : ruleFormalParameters EOF ;
    public final void entryRuleFormalParameters() throws RecognitionException {
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:342:1: ( ruleFormalParameters EOF )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:343:1: ruleFormalParameters EOF
            {
             before(grammarAccess.getFormalParametersRule()); 
            pushFollow(FOLLOW_ruleFormalParameters_in_entryRuleFormalParameters662);
            ruleFormalParameters();

            state._fsp--;

             after(grammarAccess.getFormalParametersRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleFormalParameters669); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleFormalParameters"


    // $ANTLR start "ruleFormalParameters"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:350:1: ruleFormalParameters : ( ( rule__FormalParameters__Alternatives ) ) ;
    public final void ruleFormalParameters() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:354:2: ( ( ( rule__FormalParameters__Alternatives ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:355:1: ( ( rule__FormalParameters__Alternatives ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:355:1: ( ( rule__FormalParameters__Alternatives ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:356:1: ( rule__FormalParameters__Alternatives )
            {
             before(grammarAccess.getFormalParametersAccess().getAlternatives()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:357:1: ( rule__FormalParameters__Alternatives )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:357:2: rule__FormalParameters__Alternatives
            {
            pushFollow(FOLLOW_rule__FormalParameters__Alternatives_in_ruleFormalParameters695);
            rule__FormalParameters__Alternatives();

            state._fsp--;


            }

             after(grammarAccess.getFormalParametersAccess().getAlternatives()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleFormalParameters"


    // $ANTLR start "entryRuleFormalParameter"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:369:1: entryRuleFormalParameter : ruleFormalParameter EOF ;
    public final void entryRuleFormalParameter() throws RecognitionException {
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:370:1: ( ruleFormalParameter EOF )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:371:1: ruleFormalParameter EOF
            {
             before(grammarAccess.getFormalParameterRule()); 
            pushFollow(FOLLOW_ruleFormalParameter_in_entryRuleFormalParameter722);
            ruleFormalParameter();

            state._fsp--;

             after(grammarAccess.getFormalParameterRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleFormalParameter729); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleFormalParameter"


    // $ANTLR start "ruleFormalParameter"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:378:1: ruleFormalParameter : ( ( rule__FormalParameter__Group__0 ) ) ;
    public final void ruleFormalParameter() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:382:2: ( ( ( rule__FormalParameter__Group__0 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:383:1: ( ( rule__FormalParameter__Group__0 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:383:1: ( ( rule__FormalParameter__Group__0 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:384:1: ( rule__FormalParameter__Group__0 )
            {
             before(grammarAccess.getFormalParameterAccess().getGroup()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:385:1: ( rule__FormalParameter__Group__0 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:385:2: rule__FormalParameter__Group__0
            {
            pushFollow(FOLLOW_rule__FormalParameter__Group__0_in_ruleFormalParameter755);
            rule__FormalParameter__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getFormalParameterAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleFormalParameter"


    // $ANTLR start "entryRuleTypeDef"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:397:1: entryRuleTypeDef : ruleTypeDef EOF ;
    public final void entryRuleTypeDef() throws RecognitionException {
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:398:1: ( ruleTypeDef EOF )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:399:1: ruleTypeDef EOF
            {
             before(grammarAccess.getTypeDefRule()); 
            pushFollow(FOLLOW_ruleTypeDef_in_entryRuleTypeDef782);
            ruleTypeDef();

            state._fsp--;

             after(grammarAccess.getTypeDefRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleTypeDef789); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleTypeDef"


    // $ANTLR start "ruleTypeDef"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:406:1: ruleTypeDef : ( ( rule__TypeDef__Group__0 ) ) ;
    public final void ruleTypeDef() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:410:2: ( ( ( rule__TypeDef__Group__0 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:411:1: ( ( rule__TypeDef__Group__0 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:411:1: ( ( rule__TypeDef__Group__0 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:412:1: ( rule__TypeDef__Group__0 )
            {
             before(grammarAccess.getTypeDefAccess().getGroup()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:413:1: ( rule__TypeDef__Group__0 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:413:2: rule__TypeDef__Group__0
            {
            pushFollow(FOLLOW_rule__TypeDef__Group__0_in_ruleTypeDef815);
            rule__TypeDef__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getTypeDefAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleTypeDef"


    // $ANTLR start "rule__Entity__Alternatives"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:425:1: rule__Entity__Alternatives : ( ( ruleTypeDef ) | ( ruleGlobalBlock ) | ( ruleNativeDef ) );
    public final void rule__Entity__Alternatives() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:429:1: ( ( ruleTypeDef ) | ( ruleGlobalBlock ) | ( ruleNativeDef ) )
            int alt2=3;
            switch ( input.LA(1) ) {
            case 25:
                {
                alt2=1;
                }
                break;
            case 13:
                {
                alt2=2;
                }
                break;
            case 15:
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
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:430:1: ( ruleTypeDef )
                    {
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:430:1: ( ruleTypeDef )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:431:1: ruleTypeDef
                    {
                     before(grammarAccess.getEntityAccess().getTypeDefParserRuleCall_0()); 
                    pushFollow(FOLLOW_ruleTypeDef_in_rule__Entity__Alternatives851);
                    ruleTypeDef();

                    state._fsp--;

                     after(grammarAccess.getEntityAccess().getTypeDefParserRuleCall_0()); 

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:436:6: ( ruleGlobalBlock )
                    {
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:436:6: ( ruleGlobalBlock )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:437:1: ruleGlobalBlock
                    {
                     before(grammarAccess.getEntityAccess().getGlobalBlockParserRuleCall_1()); 
                    pushFollow(FOLLOW_ruleGlobalBlock_in_rule__Entity__Alternatives868);
                    ruleGlobalBlock();

                    state._fsp--;

                     after(grammarAccess.getEntityAccess().getGlobalBlockParserRuleCall_1()); 

                    }


                    }
                    break;
                case 3 :
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:442:6: ( ruleNativeDef )
                    {
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:442:6: ( ruleNativeDef )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:443:1: ruleNativeDef
                    {
                     before(grammarAccess.getEntityAccess().getNativeDefParserRuleCall_2()); 
                    pushFollow(FOLLOW_ruleNativeDef_in_rule__Entity__Alternatives885);
                    ruleNativeDef();

                    state._fsp--;

                     after(grammarAccess.getEntityAccess().getNativeDefParserRuleCall_2()); 

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Entity__Alternatives"


    // $ANTLR start "rule__Expr__Alternatives"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:453:1: rule__Expr__Alternatives : ( ( ruleLiteral ) | ( ruleFunctionCall ) | ( ruleMult ) );
    public final void rule__Expr__Alternatives() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:457:1: ( ( ruleLiteral ) | ( ruleFunctionCall ) | ( ruleMult ) )
            int alt3=3;
            switch ( input.LA(1) ) {
            case RULE_INT:
                {
                int LA3_1 = input.LA(2);

                if ( (LA3_1==EOF||(LA3_1>=14 && LA3_1<=15)||(LA3_1>=19 && LA3_1<=20)) ) {
                    alt3=1;
                }
                else if ( (LA3_1==17) ) {
                    alt3=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 1, input);

                    throw nvae;
                }
                }
                break;
            case 11:
                {
                int LA3_2 = input.LA(2);

                if ( (LA3_2==EOF||(LA3_2>=14 && LA3_2<=15)||(LA3_2>=19 && LA3_2<=20)) ) {
                    alt3=1;
                }
                else if ( (LA3_2==17) ) {
                    alt3=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 2, input);

                    throw nvae;
                }
                }
                break;
            case 12:
                {
                int LA3_3 = input.LA(2);

                if ( (LA3_3==EOF||(LA3_3>=14 && LA3_3<=15)||(LA3_3>=19 && LA3_3<=20)) ) {
                    alt3=1;
                }
                else if ( (LA3_3==17) ) {
                    alt3=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 3, input);

                    throw nvae;
                }
                }
                break;
            case RULE_ID:
                {
                alt3=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:458:1: ( ruleLiteral )
                    {
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:458:1: ( ruleLiteral )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:459:1: ruleLiteral
                    {
                     before(grammarAccess.getExprAccess().getLiteralParserRuleCall_0()); 
                    pushFollow(FOLLOW_ruleLiteral_in_rule__Expr__Alternatives917);
                    ruleLiteral();

                    state._fsp--;

                     after(grammarAccess.getExprAccess().getLiteralParserRuleCall_0()); 

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:464:6: ( ruleFunctionCall )
                    {
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:464:6: ( ruleFunctionCall )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:465:1: ruleFunctionCall
                    {
                     before(grammarAccess.getExprAccess().getFunctionCallParserRuleCall_1()); 
                    pushFollow(FOLLOW_ruleFunctionCall_in_rule__Expr__Alternatives934);
                    ruleFunctionCall();

                    state._fsp--;

                     after(grammarAccess.getExprAccess().getFunctionCallParserRuleCall_1()); 

                    }


                    }
                    break;
                case 3 :
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:470:6: ( ruleMult )
                    {
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:470:6: ( ruleMult )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:471:1: ruleMult
                    {
                     before(grammarAccess.getExprAccess().getMultParserRuleCall_2()); 
                    pushFollow(FOLLOW_ruleMult_in_rule__Expr__Alternatives951);
                    ruleMult();

                    state._fsp--;

                     after(grammarAccess.getExprAccess().getMultParserRuleCall_2()); 

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Expr__Alternatives"


    // $ANTLR start "rule__Literal__Alternatives"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:481:1: rule__Literal__Alternatives : ( ( ( rule__Literal__Group_0__0 ) ) | ( ( rule__Literal__Group_1__0 ) ) );
    public final void rule__Literal__Alternatives() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:485:1: ( ( ( rule__Literal__Group_0__0 ) ) | ( ( rule__Literal__Group_1__0 ) ) )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==RULE_INT) ) {
                alt4=1;
            }
            else if ( ((LA4_0>=11 && LA4_0<=12)) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:486:1: ( ( rule__Literal__Group_0__0 ) )
                    {
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:486:1: ( ( rule__Literal__Group_0__0 ) )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:487:1: ( rule__Literal__Group_0__0 )
                    {
                     before(grammarAccess.getLiteralAccess().getGroup_0()); 
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:488:1: ( rule__Literal__Group_0__0 )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:488:2: rule__Literal__Group_0__0
                    {
                    pushFollow(FOLLOW_rule__Literal__Group_0__0_in_rule__Literal__Alternatives983);
                    rule__Literal__Group_0__0();

                    state._fsp--;


                    }

                     after(grammarAccess.getLiteralAccess().getGroup_0()); 

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:492:6: ( ( rule__Literal__Group_1__0 ) )
                    {
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:492:6: ( ( rule__Literal__Group_1__0 ) )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:493:1: ( rule__Literal__Group_1__0 )
                    {
                     before(grammarAccess.getLiteralAccess().getGroup_1()); 
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:494:1: ( rule__Literal__Group_1__0 )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:494:2: rule__Literal__Group_1__0
                    {
                    pushFollow(FOLLOW_rule__Literal__Group_1__0_in_rule__Literal__Alternatives1001);
                    rule__Literal__Group_1__0();

                    state._fsp--;


                    }

                     after(grammarAccess.getLiteralAccess().getGroup_1()); 

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Literal__Alternatives"


    // $ANTLR start "rule__Literal__BoolValAlternatives_1_1_0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:503:1: rule__Literal__BoolValAlternatives_1_1_0 : ( ( 'true' ) | ( 'false' ) );
    public final void rule__Literal__BoolValAlternatives_1_1_0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:507:1: ( ( 'true' ) | ( 'false' ) )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==11) ) {
                alt5=1;
            }
            else if ( (LA5_0==12) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:508:1: ( 'true' )
                    {
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:508:1: ( 'true' )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:509:1: 'true'
                    {
                     before(grammarAccess.getLiteralAccess().getBoolValTrueKeyword_1_1_0_0()); 
                    match(input,11,FOLLOW_11_in_rule__Literal__BoolValAlternatives_1_1_01035); 
                     after(grammarAccess.getLiteralAccess().getBoolValTrueKeyword_1_1_0_0()); 

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:516:6: ( 'false' )
                    {
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:516:6: ( 'false' )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:517:1: 'false'
                    {
                     before(grammarAccess.getLiteralAccess().getBoolValFalseKeyword_1_1_0_1()); 
                    match(input,12,FOLLOW_12_in_rule__Literal__BoolValAlternatives_1_1_01055); 
                     after(grammarAccess.getLiteralAccess().getBoolValFalseKeyword_1_1_0_1()); 

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Literal__BoolValAlternatives_1_1_0"


    // $ANTLR start "rule__ReturnType__Alternatives"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:529:1: rule__ReturnType__Alternatives : ( ( ( rule__ReturnType__Group_0__0 ) ) | ( ( rule__ReturnType__NameAssignment_1 ) ) );
    public final void rule__ReturnType__Alternatives() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:533:1: ( ( ( rule__ReturnType__Group_0__0 ) ) | ( ( rule__ReturnType__NameAssignment_1 ) ) )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==24) ) {
                alt6=1;
            }
            else if ( (LA6_0==RULE_ID) ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:534:1: ( ( rule__ReturnType__Group_0__0 ) )
                    {
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:534:1: ( ( rule__ReturnType__Group_0__0 ) )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:535:1: ( rule__ReturnType__Group_0__0 )
                    {
                     before(grammarAccess.getReturnTypeAccess().getGroup_0()); 
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:536:1: ( rule__ReturnType__Group_0__0 )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:536:2: rule__ReturnType__Group_0__0
                    {
                    pushFollow(FOLLOW_rule__ReturnType__Group_0__0_in_rule__ReturnType__Alternatives1089);
                    rule__ReturnType__Group_0__0();

                    state._fsp--;


                    }

                     after(grammarAccess.getReturnTypeAccess().getGroup_0()); 

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:540:6: ( ( rule__ReturnType__NameAssignment_1 ) )
                    {
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:540:6: ( ( rule__ReturnType__NameAssignment_1 ) )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:541:1: ( rule__ReturnType__NameAssignment_1 )
                    {
                     before(grammarAccess.getReturnTypeAccess().getNameAssignment_1()); 
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:542:1: ( rule__ReturnType__NameAssignment_1 )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:542:2: rule__ReturnType__NameAssignment_1
                    {
                    pushFollow(FOLLOW_rule__ReturnType__NameAssignment_1_in_rule__ReturnType__Alternatives1107);
                    rule__ReturnType__NameAssignment_1();

                    state._fsp--;


                    }

                     after(grammarAccess.getReturnTypeAccess().getNameAssignment_1()); 

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__ReturnType__Alternatives"


    // $ANTLR start "rule__FormalParameters__Alternatives"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:551:1: rule__FormalParameters__Alternatives : ( ( ( rule__FormalParameters__Group_0__0 ) ) | ( ( rule__FormalParameters__Group_1__0 ) ) );
    public final void rule__FormalParameters__Alternatives() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:555:1: ( ( ( rule__FormalParameters__Group_0__0 ) ) | ( ( rule__FormalParameters__Group_1__0 ) ) )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==24) ) {
                alt7=1;
            }
            else if ( (LA7_0==RULE_ID) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:556:1: ( ( rule__FormalParameters__Group_0__0 ) )
                    {
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:556:1: ( ( rule__FormalParameters__Group_0__0 ) )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:557:1: ( rule__FormalParameters__Group_0__0 )
                    {
                     before(grammarAccess.getFormalParametersAccess().getGroup_0()); 
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:558:1: ( rule__FormalParameters__Group_0__0 )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:558:2: rule__FormalParameters__Group_0__0
                    {
                    pushFollow(FOLLOW_rule__FormalParameters__Group_0__0_in_rule__FormalParameters__Alternatives1140);
                    rule__FormalParameters__Group_0__0();

                    state._fsp--;


                    }

                     after(grammarAccess.getFormalParametersAccess().getGroup_0()); 

                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:562:6: ( ( rule__FormalParameters__Group_1__0 ) )
                    {
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:562:6: ( ( rule__FormalParameters__Group_1__0 ) )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:563:1: ( rule__FormalParameters__Group_1__0 )
                    {
                     before(grammarAccess.getFormalParametersAccess().getGroup_1()); 
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:564:1: ( rule__FormalParameters__Group_1__0 )
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:564:2: rule__FormalParameters__Group_1__0
                    {
                    pushFollow(FOLLOW_rule__FormalParameters__Group_1__0_in_rule__FormalParameters__Alternatives1158);
                    rule__FormalParameters__Group_1__0();

                    state._fsp--;


                    }

                     after(grammarAccess.getFormalParametersAccess().getGroup_1()); 

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameters__Alternatives"


    // $ANTLR start "rule__GlobalBlock__Group__0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:575:1: rule__GlobalBlock__Group__0 : rule__GlobalBlock__Group__0__Impl rule__GlobalBlock__Group__1 ;
    public final void rule__GlobalBlock__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:579:1: ( rule__GlobalBlock__Group__0__Impl rule__GlobalBlock__Group__1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:580:2: rule__GlobalBlock__Group__0__Impl rule__GlobalBlock__Group__1
            {
            pushFollow(FOLLOW_rule__GlobalBlock__Group__0__Impl_in_rule__GlobalBlock__Group__01189);
            rule__GlobalBlock__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__GlobalBlock__Group__1_in_rule__GlobalBlock__Group__01192);
            rule__GlobalBlock__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__GlobalBlock__Group__0"


    // $ANTLR start "rule__GlobalBlock__Group__0__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:587:1: rule__GlobalBlock__Group__0__Impl : ( () ) ;
    public final void rule__GlobalBlock__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:591:1: ( ( () ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:592:1: ( () )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:592:1: ( () )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:593:1: ()
            {
             before(grammarAccess.getGlobalBlockAccess().getGlobalBlockAction_0()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:594:1: ()
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:596:1: 
            {
            }

             after(grammarAccess.getGlobalBlockAccess().getGlobalBlockAction_0()); 

            }


            }

        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__GlobalBlock__Group__0__Impl"


    // $ANTLR start "rule__GlobalBlock__Group__1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:606:1: rule__GlobalBlock__Group__1 : rule__GlobalBlock__Group__1__Impl rule__GlobalBlock__Group__2 ;
    public final void rule__GlobalBlock__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:610:1: ( rule__GlobalBlock__Group__1__Impl rule__GlobalBlock__Group__2 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:611:2: rule__GlobalBlock__Group__1__Impl rule__GlobalBlock__Group__2
            {
            pushFollow(FOLLOW_rule__GlobalBlock__Group__1__Impl_in_rule__GlobalBlock__Group__11250);
            rule__GlobalBlock__Group__1__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__GlobalBlock__Group__2_in_rule__GlobalBlock__Group__11253);
            rule__GlobalBlock__Group__2();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__GlobalBlock__Group__1"


    // $ANTLR start "rule__GlobalBlock__Group__1__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:618:1: rule__GlobalBlock__Group__1__Impl : ( 'globals' ) ;
    public final void rule__GlobalBlock__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:622:1: ( ( 'globals' ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:623:1: ( 'globals' )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:623:1: ( 'globals' )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:624:1: 'globals'
            {
             before(grammarAccess.getGlobalBlockAccess().getGlobalsKeyword_1()); 
            match(input,13,FOLLOW_13_in_rule__GlobalBlock__Group__1__Impl1281); 
             after(grammarAccess.getGlobalBlockAccess().getGlobalsKeyword_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__GlobalBlock__Group__1__Impl"


    // $ANTLR start "rule__GlobalBlock__Group__2"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:637:1: rule__GlobalBlock__Group__2 : rule__GlobalBlock__Group__2__Impl rule__GlobalBlock__Group__3 ;
    public final void rule__GlobalBlock__Group__2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:641:1: ( rule__GlobalBlock__Group__2__Impl rule__GlobalBlock__Group__3 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:642:2: rule__GlobalBlock__Group__2__Impl rule__GlobalBlock__Group__3
            {
            pushFollow(FOLLOW_rule__GlobalBlock__Group__2__Impl_in_rule__GlobalBlock__Group__21312);
            rule__GlobalBlock__Group__2__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__GlobalBlock__Group__3_in_rule__GlobalBlock__Group__21315);
            rule__GlobalBlock__Group__3();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__GlobalBlock__Group__2"


    // $ANTLR start "rule__GlobalBlock__Group__2__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:649:1: rule__GlobalBlock__Group__2__Impl : ( ( rule__GlobalBlock__VarsAssignment_2 )* ) ;
    public final void rule__GlobalBlock__Group__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:653:1: ( ( ( rule__GlobalBlock__VarsAssignment_2 )* ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:654:1: ( ( rule__GlobalBlock__VarsAssignment_2 )* )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:654:1: ( ( rule__GlobalBlock__VarsAssignment_2 )* )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:655:1: ( rule__GlobalBlock__VarsAssignment_2 )*
            {
             before(grammarAccess.getGlobalBlockAccess().getVarsAssignment_2()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:656:1: ( rule__GlobalBlock__VarsAssignment_2 )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==15) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:656:2: rule__GlobalBlock__VarsAssignment_2
            	    {
            	    pushFollow(FOLLOW_rule__GlobalBlock__VarsAssignment_2_in_rule__GlobalBlock__Group__2__Impl1342);
            	    rule__GlobalBlock__VarsAssignment_2();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

             after(grammarAccess.getGlobalBlockAccess().getVarsAssignment_2()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__GlobalBlock__Group__2__Impl"


    // $ANTLR start "rule__GlobalBlock__Group__3"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:666:1: rule__GlobalBlock__Group__3 : rule__GlobalBlock__Group__3__Impl ;
    public final void rule__GlobalBlock__Group__3() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:670:1: ( rule__GlobalBlock__Group__3__Impl )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:671:2: rule__GlobalBlock__Group__3__Impl
            {
            pushFollow(FOLLOW_rule__GlobalBlock__Group__3__Impl_in_rule__GlobalBlock__Group__31373);
            rule__GlobalBlock__Group__3__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__GlobalBlock__Group__3"


    // $ANTLR start "rule__GlobalBlock__Group__3__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:677:1: rule__GlobalBlock__Group__3__Impl : ( 'endglobals' ) ;
    public final void rule__GlobalBlock__Group__3__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:681:1: ( ( 'endglobals' ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:682:1: ( 'endglobals' )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:682:1: ( 'endglobals' )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:683:1: 'endglobals'
            {
             before(grammarAccess.getGlobalBlockAccess().getEndglobalsKeyword_3()); 
            match(input,14,FOLLOW_14_in_rule__GlobalBlock__Group__3__Impl1401); 
             after(grammarAccess.getGlobalBlockAccess().getEndglobalsKeyword_3()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__GlobalBlock__Group__3__Impl"


    // $ANTLR start "rule__Constant__Group__0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:704:1: rule__Constant__Group__0 : rule__Constant__Group__0__Impl rule__Constant__Group__1 ;
    public final void rule__Constant__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:708:1: ( rule__Constant__Group__0__Impl rule__Constant__Group__1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:709:2: rule__Constant__Group__0__Impl rule__Constant__Group__1
            {
            pushFollow(FOLLOW_rule__Constant__Group__0__Impl_in_rule__Constant__Group__01440);
            rule__Constant__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__Constant__Group__1_in_rule__Constant__Group__01443);
            rule__Constant__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Constant__Group__0"


    // $ANTLR start "rule__Constant__Group__0__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:716:1: rule__Constant__Group__0__Impl : ( 'constant' ) ;
    public final void rule__Constant__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:720:1: ( ( 'constant' ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:721:1: ( 'constant' )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:721:1: ( 'constant' )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:722:1: 'constant'
            {
             before(grammarAccess.getConstantAccess().getConstantKeyword_0()); 
            match(input,15,FOLLOW_15_in_rule__Constant__Group__0__Impl1471); 
             after(grammarAccess.getConstantAccess().getConstantKeyword_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Constant__Group__0__Impl"


    // $ANTLR start "rule__Constant__Group__1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:735:1: rule__Constant__Group__1 : rule__Constant__Group__1__Impl rule__Constant__Group__2 ;
    public final void rule__Constant__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:739:1: ( rule__Constant__Group__1__Impl rule__Constant__Group__2 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:740:2: rule__Constant__Group__1__Impl rule__Constant__Group__2
            {
            pushFollow(FOLLOW_rule__Constant__Group__1__Impl_in_rule__Constant__Group__11502);
            rule__Constant__Group__1__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__Constant__Group__2_in_rule__Constant__Group__11505);
            rule__Constant__Group__2();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Constant__Group__1"


    // $ANTLR start "rule__Constant__Group__1__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:747:1: rule__Constant__Group__1__Impl : ( ( rule__Constant__TypeAssignment_1 ) ) ;
    public final void rule__Constant__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:751:1: ( ( ( rule__Constant__TypeAssignment_1 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:752:1: ( ( rule__Constant__TypeAssignment_1 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:752:1: ( ( rule__Constant__TypeAssignment_1 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:753:1: ( rule__Constant__TypeAssignment_1 )
            {
             before(grammarAccess.getConstantAccess().getTypeAssignment_1()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:754:1: ( rule__Constant__TypeAssignment_1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:754:2: rule__Constant__TypeAssignment_1
            {
            pushFollow(FOLLOW_rule__Constant__TypeAssignment_1_in_rule__Constant__Group__1__Impl1532);
            rule__Constant__TypeAssignment_1();

            state._fsp--;


            }

             after(grammarAccess.getConstantAccess().getTypeAssignment_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Constant__Group__1__Impl"


    // $ANTLR start "rule__Constant__Group__2"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:764:1: rule__Constant__Group__2 : rule__Constant__Group__2__Impl rule__Constant__Group__3 ;
    public final void rule__Constant__Group__2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:768:1: ( rule__Constant__Group__2__Impl rule__Constant__Group__3 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:769:2: rule__Constant__Group__2__Impl rule__Constant__Group__3
            {
            pushFollow(FOLLOW_rule__Constant__Group__2__Impl_in_rule__Constant__Group__21562);
            rule__Constant__Group__2__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__Constant__Group__3_in_rule__Constant__Group__21565);
            rule__Constant__Group__3();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Constant__Group__2"


    // $ANTLR start "rule__Constant__Group__2__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:776:1: rule__Constant__Group__2__Impl : ( ( rule__Constant__NameAssignment_2 ) ) ;
    public final void rule__Constant__Group__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:780:1: ( ( ( rule__Constant__NameAssignment_2 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:781:1: ( ( rule__Constant__NameAssignment_2 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:781:1: ( ( rule__Constant__NameAssignment_2 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:782:1: ( rule__Constant__NameAssignment_2 )
            {
             before(grammarAccess.getConstantAccess().getNameAssignment_2()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:783:1: ( rule__Constant__NameAssignment_2 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:783:2: rule__Constant__NameAssignment_2
            {
            pushFollow(FOLLOW_rule__Constant__NameAssignment_2_in_rule__Constant__Group__2__Impl1592);
            rule__Constant__NameAssignment_2();

            state._fsp--;


            }

             after(grammarAccess.getConstantAccess().getNameAssignment_2()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Constant__Group__2__Impl"


    // $ANTLR start "rule__Constant__Group__3"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:793:1: rule__Constant__Group__3 : rule__Constant__Group__3__Impl rule__Constant__Group__4 ;
    public final void rule__Constant__Group__3() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:797:1: ( rule__Constant__Group__3__Impl rule__Constant__Group__4 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:798:2: rule__Constant__Group__3__Impl rule__Constant__Group__4
            {
            pushFollow(FOLLOW_rule__Constant__Group__3__Impl_in_rule__Constant__Group__31622);
            rule__Constant__Group__3__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__Constant__Group__4_in_rule__Constant__Group__31625);
            rule__Constant__Group__4();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Constant__Group__3"


    // $ANTLR start "rule__Constant__Group__3__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:805:1: rule__Constant__Group__3__Impl : ( '=' ) ;
    public final void rule__Constant__Group__3__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:809:1: ( ( '=' ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:810:1: ( '=' )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:810:1: ( '=' )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:811:1: '='
            {
             before(grammarAccess.getConstantAccess().getEqualsSignKeyword_3()); 
            match(input,16,FOLLOW_16_in_rule__Constant__Group__3__Impl1653); 
             after(grammarAccess.getConstantAccess().getEqualsSignKeyword_3()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Constant__Group__3__Impl"


    // $ANTLR start "rule__Constant__Group__4"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:824:1: rule__Constant__Group__4 : rule__Constant__Group__4__Impl ;
    public final void rule__Constant__Group__4() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:828:1: ( rule__Constant__Group__4__Impl )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:829:2: rule__Constant__Group__4__Impl
            {
            pushFollow(FOLLOW_rule__Constant__Group__4__Impl_in_rule__Constant__Group__41684);
            rule__Constant__Group__4__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Constant__Group__4"


    // $ANTLR start "rule__Constant__Group__4__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:835:1: rule__Constant__Group__4__Impl : ( ( rule__Constant__ValueAssignment_4 ) ) ;
    public final void rule__Constant__Group__4__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:839:1: ( ( ( rule__Constant__ValueAssignment_4 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:840:1: ( ( rule__Constant__ValueAssignment_4 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:840:1: ( ( rule__Constant__ValueAssignment_4 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:841:1: ( rule__Constant__ValueAssignment_4 )
            {
             before(grammarAccess.getConstantAccess().getValueAssignment_4()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:842:1: ( rule__Constant__ValueAssignment_4 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:842:2: rule__Constant__ValueAssignment_4
            {
            pushFollow(FOLLOW_rule__Constant__ValueAssignment_4_in_rule__Constant__Group__4__Impl1711);
            rule__Constant__ValueAssignment_4();

            state._fsp--;


            }

             after(grammarAccess.getConstantAccess().getValueAssignment_4()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Constant__Group__4__Impl"


    // $ANTLR start "rule__Mult__Group__0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:862:1: rule__Mult__Group__0 : rule__Mult__Group__0__Impl rule__Mult__Group__1 ;
    public final void rule__Mult__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:866:1: ( rule__Mult__Group__0__Impl rule__Mult__Group__1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:867:2: rule__Mult__Group__0__Impl rule__Mult__Group__1
            {
            pushFollow(FOLLOW_rule__Mult__Group__0__Impl_in_rule__Mult__Group__01751);
            rule__Mult__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__Mult__Group__1_in_rule__Mult__Group__01754);
            rule__Mult__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Mult__Group__0"


    // $ANTLR start "rule__Mult__Group__0__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:874:1: rule__Mult__Group__0__Impl : ( ( rule__Mult__LeftAssignment_0 ) ) ;
    public final void rule__Mult__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:878:1: ( ( ( rule__Mult__LeftAssignment_0 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:879:1: ( ( rule__Mult__LeftAssignment_0 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:879:1: ( ( rule__Mult__LeftAssignment_0 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:880:1: ( rule__Mult__LeftAssignment_0 )
            {
             before(grammarAccess.getMultAccess().getLeftAssignment_0()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:881:1: ( rule__Mult__LeftAssignment_0 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:881:2: rule__Mult__LeftAssignment_0
            {
            pushFollow(FOLLOW_rule__Mult__LeftAssignment_0_in_rule__Mult__Group__0__Impl1781);
            rule__Mult__LeftAssignment_0();

            state._fsp--;


            }

             after(grammarAccess.getMultAccess().getLeftAssignment_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Mult__Group__0__Impl"


    // $ANTLR start "rule__Mult__Group__1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:891:1: rule__Mult__Group__1 : rule__Mult__Group__1__Impl rule__Mult__Group__2 ;
    public final void rule__Mult__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:895:1: ( rule__Mult__Group__1__Impl rule__Mult__Group__2 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:896:2: rule__Mult__Group__1__Impl rule__Mult__Group__2
            {
            pushFollow(FOLLOW_rule__Mult__Group__1__Impl_in_rule__Mult__Group__11811);
            rule__Mult__Group__1__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__Mult__Group__2_in_rule__Mult__Group__11814);
            rule__Mult__Group__2();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Mult__Group__1"


    // $ANTLR start "rule__Mult__Group__1__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:903:1: rule__Mult__Group__1__Impl : ( '*' ) ;
    public final void rule__Mult__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:907:1: ( ( '*' ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:908:1: ( '*' )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:908:1: ( '*' )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:909:1: '*'
            {
             before(grammarAccess.getMultAccess().getAsteriskKeyword_1()); 
            match(input,17,FOLLOW_17_in_rule__Mult__Group__1__Impl1842); 
             after(grammarAccess.getMultAccess().getAsteriskKeyword_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Mult__Group__1__Impl"


    // $ANTLR start "rule__Mult__Group__2"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:922:1: rule__Mult__Group__2 : rule__Mult__Group__2__Impl ;
    public final void rule__Mult__Group__2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:926:1: ( rule__Mult__Group__2__Impl )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:927:2: rule__Mult__Group__2__Impl
            {
            pushFollow(FOLLOW_rule__Mult__Group__2__Impl_in_rule__Mult__Group__21873);
            rule__Mult__Group__2__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Mult__Group__2"


    // $ANTLR start "rule__Mult__Group__2__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:933:1: rule__Mult__Group__2__Impl : ( ( rule__Mult__RightAssignment_2 ) ) ;
    public final void rule__Mult__Group__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:937:1: ( ( ( rule__Mult__RightAssignment_2 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:938:1: ( ( rule__Mult__RightAssignment_2 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:938:1: ( ( rule__Mult__RightAssignment_2 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:939:1: ( rule__Mult__RightAssignment_2 )
            {
             before(grammarAccess.getMultAccess().getRightAssignment_2()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:940:1: ( rule__Mult__RightAssignment_2 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:940:2: rule__Mult__RightAssignment_2
            {
            pushFollow(FOLLOW_rule__Mult__RightAssignment_2_in_rule__Mult__Group__2__Impl1900);
            rule__Mult__RightAssignment_2();

            state._fsp--;


            }

             after(grammarAccess.getMultAccess().getRightAssignment_2()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Mult__Group__2__Impl"


    // $ANTLR start "rule__FunctionCall__Group__0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:956:1: rule__FunctionCall__Group__0 : rule__FunctionCall__Group__0__Impl rule__FunctionCall__Group__1 ;
    public final void rule__FunctionCall__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:960:1: ( rule__FunctionCall__Group__0__Impl rule__FunctionCall__Group__1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:961:2: rule__FunctionCall__Group__0__Impl rule__FunctionCall__Group__1
            {
            pushFollow(FOLLOW_rule__FunctionCall__Group__0__Impl_in_rule__FunctionCall__Group__01936);
            rule__FunctionCall__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__FunctionCall__Group__1_in_rule__FunctionCall__Group__01939);
            rule__FunctionCall__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__Group__0"


    // $ANTLR start "rule__FunctionCall__Group__0__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:968:1: rule__FunctionCall__Group__0__Impl : ( ( rule__FunctionCall__NameAssignment_0 ) ) ;
    public final void rule__FunctionCall__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:972:1: ( ( ( rule__FunctionCall__NameAssignment_0 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:973:1: ( ( rule__FunctionCall__NameAssignment_0 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:973:1: ( ( rule__FunctionCall__NameAssignment_0 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:974:1: ( rule__FunctionCall__NameAssignment_0 )
            {
             before(grammarAccess.getFunctionCallAccess().getNameAssignment_0()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:975:1: ( rule__FunctionCall__NameAssignment_0 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:975:2: rule__FunctionCall__NameAssignment_0
            {
            pushFollow(FOLLOW_rule__FunctionCall__NameAssignment_0_in_rule__FunctionCall__Group__0__Impl1966);
            rule__FunctionCall__NameAssignment_0();

            state._fsp--;


            }

             after(grammarAccess.getFunctionCallAccess().getNameAssignment_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__Group__0__Impl"


    // $ANTLR start "rule__FunctionCall__Group__1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:985:1: rule__FunctionCall__Group__1 : rule__FunctionCall__Group__1__Impl rule__FunctionCall__Group__2 ;
    public final void rule__FunctionCall__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:989:1: ( rule__FunctionCall__Group__1__Impl rule__FunctionCall__Group__2 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:990:2: rule__FunctionCall__Group__1__Impl rule__FunctionCall__Group__2
            {
            pushFollow(FOLLOW_rule__FunctionCall__Group__1__Impl_in_rule__FunctionCall__Group__11996);
            rule__FunctionCall__Group__1__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__FunctionCall__Group__2_in_rule__FunctionCall__Group__11999);
            rule__FunctionCall__Group__2();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__Group__1"


    // $ANTLR start "rule__FunctionCall__Group__1__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:997:1: rule__FunctionCall__Group__1__Impl : ( '(' ) ;
    public final void rule__FunctionCall__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1001:1: ( ( '(' ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1002:1: ( '(' )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1002:1: ( '(' )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1003:1: '('
            {
             before(grammarAccess.getFunctionCallAccess().getLeftParenthesisKeyword_1()); 
            match(input,18,FOLLOW_18_in_rule__FunctionCall__Group__1__Impl2027); 
             after(grammarAccess.getFunctionCallAccess().getLeftParenthesisKeyword_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__Group__1__Impl"


    // $ANTLR start "rule__FunctionCall__Group__2"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1016:1: rule__FunctionCall__Group__2 : rule__FunctionCall__Group__2__Impl rule__FunctionCall__Group__3 ;
    public final void rule__FunctionCall__Group__2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1020:1: ( rule__FunctionCall__Group__2__Impl rule__FunctionCall__Group__3 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1021:2: rule__FunctionCall__Group__2__Impl rule__FunctionCall__Group__3
            {
            pushFollow(FOLLOW_rule__FunctionCall__Group__2__Impl_in_rule__FunctionCall__Group__22058);
            rule__FunctionCall__Group__2__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__FunctionCall__Group__3_in_rule__FunctionCall__Group__22061);
            rule__FunctionCall__Group__3();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__Group__2"


    // $ANTLR start "rule__FunctionCall__Group__2__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1028:1: rule__FunctionCall__Group__2__Impl : ( ( rule__FunctionCall__Group_2__0 )? ) ;
    public final void rule__FunctionCall__Group__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1032:1: ( ( ( rule__FunctionCall__Group_2__0 )? ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1033:1: ( ( rule__FunctionCall__Group_2__0 )? )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1033:1: ( ( rule__FunctionCall__Group_2__0 )? )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1034:1: ( rule__FunctionCall__Group_2__0 )?
            {
             before(grammarAccess.getFunctionCallAccess().getGroup_2()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1035:1: ( rule__FunctionCall__Group_2__0 )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( ((LA9_0>=RULE_ID && LA9_0<=RULE_INT)||(LA9_0>=11 && LA9_0<=12)) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1035:2: rule__FunctionCall__Group_2__0
                    {
                    pushFollow(FOLLOW_rule__FunctionCall__Group_2__0_in_rule__FunctionCall__Group__2__Impl2088);
                    rule__FunctionCall__Group_2__0();

                    state._fsp--;


                    }
                    break;

            }

             after(grammarAccess.getFunctionCallAccess().getGroup_2()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__Group__2__Impl"


    // $ANTLR start "rule__FunctionCall__Group__3"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1045:1: rule__FunctionCall__Group__3 : rule__FunctionCall__Group__3__Impl ;
    public final void rule__FunctionCall__Group__3() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1049:1: ( rule__FunctionCall__Group__3__Impl )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1050:2: rule__FunctionCall__Group__3__Impl
            {
            pushFollow(FOLLOW_rule__FunctionCall__Group__3__Impl_in_rule__FunctionCall__Group__32119);
            rule__FunctionCall__Group__3__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__Group__3"


    // $ANTLR start "rule__FunctionCall__Group__3__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1056:1: rule__FunctionCall__Group__3__Impl : ( ')' ) ;
    public final void rule__FunctionCall__Group__3__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1060:1: ( ( ')' ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1061:1: ( ')' )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1061:1: ( ')' )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1062:1: ')'
            {
             before(grammarAccess.getFunctionCallAccess().getRightParenthesisKeyword_3()); 
            match(input,19,FOLLOW_19_in_rule__FunctionCall__Group__3__Impl2147); 
             after(grammarAccess.getFunctionCallAccess().getRightParenthesisKeyword_3()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__Group__3__Impl"


    // $ANTLR start "rule__FunctionCall__Group_2__0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1083:1: rule__FunctionCall__Group_2__0 : rule__FunctionCall__Group_2__0__Impl rule__FunctionCall__Group_2__1 ;
    public final void rule__FunctionCall__Group_2__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1087:1: ( rule__FunctionCall__Group_2__0__Impl rule__FunctionCall__Group_2__1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1088:2: rule__FunctionCall__Group_2__0__Impl rule__FunctionCall__Group_2__1
            {
            pushFollow(FOLLOW_rule__FunctionCall__Group_2__0__Impl_in_rule__FunctionCall__Group_2__02186);
            rule__FunctionCall__Group_2__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__FunctionCall__Group_2__1_in_rule__FunctionCall__Group_2__02189);
            rule__FunctionCall__Group_2__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__Group_2__0"


    // $ANTLR start "rule__FunctionCall__Group_2__0__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1095:1: rule__FunctionCall__Group_2__0__Impl : ( ( rule__FunctionCall__ParametersAssignment_2_0 ) ) ;
    public final void rule__FunctionCall__Group_2__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1099:1: ( ( ( rule__FunctionCall__ParametersAssignment_2_0 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1100:1: ( ( rule__FunctionCall__ParametersAssignment_2_0 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1100:1: ( ( rule__FunctionCall__ParametersAssignment_2_0 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1101:1: ( rule__FunctionCall__ParametersAssignment_2_0 )
            {
             before(grammarAccess.getFunctionCallAccess().getParametersAssignment_2_0()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1102:1: ( rule__FunctionCall__ParametersAssignment_2_0 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1102:2: rule__FunctionCall__ParametersAssignment_2_0
            {
            pushFollow(FOLLOW_rule__FunctionCall__ParametersAssignment_2_0_in_rule__FunctionCall__Group_2__0__Impl2216);
            rule__FunctionCall__ParametersAssignment_2_0();

            state._fsp--;


            }

             after(grammarAccess.getFunctionCallAccess().getParametersAssignment_2_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__Group_2__0__Impl"


    // $ANTLR start "rule__FunctionCall__Group_2__1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1112:1: rule__FunctionCall__Group_2__1 : rule__FunctionCall__Group_2__1__Impl ;
    public final void rule__FunctionCall__Group_2__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1116:1: ( rule__FunctionCall__Group_2__1__Impl )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1117:2: rule__FunctionCall__Group_2__1__Impl
            {
            pushFollow(FOLLOW_rule__FunctionCall__Group_2__1__Impl_in_rule__FunctionCall__Group_2__12246);
            rule__FunctionCall__Group_2__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__Group_2__1"


    // $ANTLR start "rule__FunctionCall__Group_2__1__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1123:1: rule__FunctionCall__Group_2__1__Impl : ( ( rule__FunctionCall__Group_2_1__0 )* ) ;
    public final void rule__FunctionCall__Group_2__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1127:1: ( ( ( rule__FunctionCall__Group_2_1__0 )* ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1128:1: ( ( rule__FunctionCall__Group_2_1__0 )* )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1128:1: ( ( rule__FunctionCall__Group_2_1__0 )* )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1129:1: ( rule__FunctionCall__Group_2_1__0 )*
            {
             before(grammarAccess.getFunctionCallAccess().getGroup_2_1()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1130:1: ( rule__FunctionCall__Group_2_1__0 )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==20) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1130:2: rule__FunctionCall__Group_2_1__0
            	    {
            	    pushFollow(FOLLOW_rule__FunctionCall__Group_2_1__0_in_rule__FunctionCall__Group_2__1__Impl2273);
            	    rule__FunctionCall__Group_2_1__0();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);

             after(grammarAccess.getFunctionCallAccess().getGroup_2_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__Group_2__1__Impl"


    // $ANTLR start "rule__FunctionCall__Group_2_1__0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1144:1: rule__FunctionCall__Group_2_1__0 : rule__FunctionCall__Group_2_1__0__Impl rule__FunctionCall__Group_2_1__1 ;
    public final void rule__FunctionCall__Group_2_1__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1148:1: ( rule__FunctionCall__Group_2_1__0__Impl rule__FunctionCall__Group_2_1__1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1149:2: rule__FunctionCall__Group_2_1__0__Impl rule__FunctionCall__Group_2_1__1
            {
            pushFollow(FOLLOW_rule__FunctionCall__Group_2_1__0__Impl_in_rule__FunctionCall__Group_2_1__02308);
            rule__FunctionCall__Group_2_1__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__FunctionCall__Group_2_1__1_in_rule__FunctionCall__Group_2_1__02311);
            rule__FunctionCall__Group_2_1__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__Group_2_1__0"


    // $ANTLR start "rule__FunctionCall__Group_2_1__0__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1156:1: rule__FunctionCall__Group_2_1__0__Impl : ( ',' ) ;
    public final void rule__FunctionCall__Group_2_1__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1160:1: ( ( ',' ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1161:1: ( ',' )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1161:1: ( ',' )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1162:1: ','
            {
             before(grammarAccess.getFunctionCallAccess().getCommaKeyword_2_1_0()); 
            match(input,20,FOLLOW_20_in_rule__FunctionCall__Group_2_1__0__Impl2339); 
             after(grammarAccess.getFunctionCallAccess().getCommaKeyword_2_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__Group_2_1__0__Impl"


    // $ANTLR start "rule__FunctionCall__Group_2_1__1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1175:1: rule__FunctionCall__Group_2_1__1 : rule__FunctionCall__Group_2_1__1__Impl ;
    public final void rule__FunctionCall__Group_2_1__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1179:1: ( rule__FunctionCall__Group_2_1__1__Impl )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1180:2: rule__FunctionCall__Group_2_1__1__Impl
            {
            pushFollow(FOLLOW_rule__FunctionCall__Group_2_1__1__Impl_in_rule__FunctionCall__Group_2_1__12370);
            rule__FunctionCall__Group_2_1__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__Group_2_1__1"


    // $ANTLR start "rule__FunctionCall__Group_2_1__1__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1186:1: rule__FunctionCall__Group_2_1__1__Impl : ( ( rule__FunctionCall__ParametersAssignment_2_1_1 ) ) ;
    public final void rule__FunctionCall__Group_2_1__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1190:1: ( ( ( rule__FunctionCall__ParametersAssignment_2_1_1 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1191:1: ( ( rule__FunctionCall__ParametersAssignment_2_1_1 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1191:1: ( ( rule__FunctionCall__ParametersAssignment_2_1_1 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1192:1: ( rule__FunctionCall__ParametersAssignment_2_1_1 )
            {
             before(grammarAccess.getFunctionCallAccess().getParametersAssignment_2_1_1()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1193:1: ( rule__FunctionCall__ParametersAssignment_2_1_1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1193:2: rule__FunctionCall__ParametersAssignment_2_1_1
            {
            pushFollow(FOLLOW_rule__FunctionCall__ParametersAssignment_2_1_1_in_rule__FunctionCall__Group_2_1__1__Impl2397);
            rule__FunctionCall__ParametersAssignment_2_1_1();

            state._fsp--;


            }

             after(grammarAccess.getFunctionCallAccess().getParametersAssignment_2_1_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__Group_2_1__1__Impl"


    // $ANTLR start "rule__Literal__Group_0__0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1207:1: rule__Literal__Group_0__0 : rule__Literal__Group_0__0__Impl rule__Literal__Group_0__1 ;
    public final void rule__Literal__Group_0__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1211:1: ( rule__Literal__Group_0__0__Impl rule__Literal__Group_0__1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1212:2: rule__Literal__Group_0__0__Impl rule__Literal__Group_0__1
            {
            pushFollow(FOLLOW_rule__Literal__Group_0__0__Impl_in_rule__Literal__Group_0__02431);
            rule__Literal__Group_0__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__Literal__Group_0__1_in_rule__Literal__Group_0__02434);
            rule__Literal__Group_0__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Literal__Group_0__0"


    // $ANTLR start "rule__Literal__Group_0__0__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1219:1: rule__Literal__Group_0__0__Impl : ( () ) ;
    public final void rule__Literal__Group_0__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1223:1: ( ( () ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1224:1: ( () )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1224:1: ( () )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1225:1: ()
            {
             before(grammarAccess.getLiteralAccess().getIntLiteralAction_0_0()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1226:1: ()
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1228:1: 
            {
            }

             after(grammarAccess.getLiteralAccess().getIntLiteralAction_0_0()); 

            }


            }

        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Literal__Group_0__0__Impl"


    // $ANTLR start "rule__Literal__Group_0__1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1238:1: rule__Literal__Group_0__1 : rule__Literal__Group_0__1__Impl ;
    public final void rule__Literal__Group_0__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1242:1: ( rule__Literal__Group_0__1__Impl )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1243:2: rule__Literal__Group_0__1__Impl
            {
            pushFollow(FOLLOW_rule__Literal__Group_0__1__Impl_in_rule__Literal__Group_0__12492);
            rule__Literal__Group_0__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Literal__Group_0__1"


    // $ANTLR start "rule__Literal__Group_0__1__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1249:1: rule__Literal__Group_0__1__Impl : ( ( rule__Literal__IntValAssignment_0_1 ) ) ;
    public final void rule__Literal__Group_0__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1253:1: ( ( ( rule__Literal__IntValAssignment_0_1 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1254:1: ( ( rule__Literal__IntValAssignment_0_1 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1254:1: ( ( rule__Literal__IntValAssignment_0_1 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1255:1: ( rule__Literal__IntValAssignment_0_1 )
            {
             before(grammarAccess.getLiteralAccess().getIntValAssignment_0_1()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1256:1: ( rule__Literal__IntValAssignment_0_1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1256:2: rule__Literal__IntValAssignment_0_1
            {
            pushFollow(FOLLOW_rule__Literal__IntValAssignment_0_1_in_rule__Literal__Group_0__1__Impl2519);
            rule__Literal__IntValAssignment_0_1();

            state._fsp--;


            }

             after(grammarAccess.getLiteralAccess().getIntValAssignment_0_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Literal__Group_0__1__Impl"


    // $ANTLR start "rule__Literal__Group_1__0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1270:1: rule__Literal__Group_1__0 : rule__Literal__Group_1__0__Impl rule__Literal__Group_1__1 ;
    public final void rule__Literal__Group_1__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1274:1: ( rule__Literal__Group_1__0__Impl rule__Literal__Group_1__1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1275:2: rule__Literal__Group_1__0__Impl rule__Literal__Group_1__1
            {
            pushFollow(FOLLOW_rule__Literal__Group_1__0__Impl_in_rule__Literal__Group_1__02553);
            rule__Literal__Group_1__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__Literal__Group_1__1_in_rule__Literal__Group_1__02556);
            rule__Literal__Group_1__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Literal__Group_1__0"


    // $ANTLR start "rule__Literal__Group_1__0__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1282:1: rule__Literal__Group_1__0__Impl : ( () ) ;
    public final void rule__Literal__Group_1__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1286:1: ( ( () ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1287:1: ( () )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1287:1: ( () )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1288:1: ()
            {
             before(grammarAccess.getLiteralAccess().getBoolLiteralAction_1_0()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1289:1: ()
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1291:1: 
            {
            }

             after(grammarAccess.getLiteralAccess().getBoolLiteralAction_1_0()); 

            }


            }

        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Literal__Group_1__0__Impl"


    // $ANTLR start "rule__Literal__Group_1__1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1301:1: rule__Literal__Group_1__1 : rule__Literal__Group_1__1__Impl ;
    public final void rule__Literal__Group_1__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1305:1: ( rule__Literal__Group_1__1__Impl )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1306:2: rule__Literal__Group_1__1__Impl
            {
            pushFollow(FOLLOW_rule__Literal__Group_1__1__Impl_in_rule__Literal__Group_1__12614);
            rule__Literal__Group_1__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Literal__Group_1__1"


    // $ANTLR start "rule__Literal__Group_1__1__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1312:1: rule__Literal__Group_1__1__Impl : ( ( rule__Literal__BoolValAssignment_1_1 ) ) ;
    public final void rule__Literal__Group_1__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1316:1: ( ( ( rule__Literal__BoolValAssignment_1_1 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1317:1: ( ( rule__Literal__BoolValAssignment_1_1 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1317:1: ( ( rule__Literal__BoolValAssignment_1_1 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1318:1: ( rule__Literal__BoolValAssignment_1_1 )
            {
             before(grammarAccess.getLiteralAccess().getBoolValAssignment_1_1()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1319:1: ( rule__Literal__BoolValAssignment_1_1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1319:2: rule__Literal__BoolValAssignment_1_1
            {
            pushFollow(FOLLOW_rule__Literal__BoolValAssignment_1_1_in_rule__Literal__Group_1__1__Impl2641);
            rule__Literal__BoolValAssignment_1_1();

            state._fsp--;


            }

             after(grammarAccess.getLiteralAccess().getBoolValAssignment_1_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Literal__Group_1__1__Impl"


    // $ANTLR start "rule__NativeDef__Group__0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1333:1: rule__NativeDef__Group__0 : rule__NativeDef__Group__0__Impl rule__NativeDef__Group__1 ;
    public final void rule__NativeDef__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1337:1: ( rule__NativeDef__Group__0__Impl rule__NativeDef__Group__1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1338:2: rule__NativeDef__Group__0__Impl rule__NativeDef__Group__1
            {
            pushFollow(FOLLOW_rule__NativeDef__Group__0__Impl_in_rule__NativeDef__Group__02675);
            rule__NativeDef__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__NativeDef__Group__1_in_rule__NativeDef__Group__02678);
            rule__NativeDef__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__Group__0"


    // $ANTLR start "rule__NativeDef__Group__0__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1345:1: rule__NativeDef__Group__0__Impl : ( ( 'constant' )? ) ;
    public final void rule__NativeDef__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1349:1: ( ( ( 'constant' )? ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1350:1: ( ( 'constant' )? )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1350:1: ( ( 'constant' )? )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1351:1: ( 'constant' )?
            {
             before(grammarAccess.getNativeDefAccess().getConstantKeyword_0()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1352:1: ( 'constant' )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==15) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1353:2: 'constant'
                    {
                    match(input,15,FOLLOW_15_in_rule__NativeDef__Group__0__Impl2707); 

                    }
                    break;

            }

             after(grammarAccess.getNativeDefAccess().getConstantKeyword_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__Group__0__Impl"


    // $ANTLR start "rule__NativeDef__Group__1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1364:1: rule__NativeDef__Group__1 : rule__NativeDef__Group__1__Impl rule__NativeDef__Group__2 ;
    public final void rule__NativeDef__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1368:1: ( rule__NativeDef__Group__1__Impl rule__NativeDef__Group__2 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1369:2: rule__NativeDef__Group__1__Impl rule__NativeDef__Group__2
            {
            pushFollow(FOLLOW_rule__NativeDef__Group__1__Impl_in_rule__NativeDef__Group__12740);
            rule__NativeDef__Group__1__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__NativeDef__Group__2_in_rule__NativeDef__Group__12743);
            rule__NativeDef__Group__2();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__Group__1"


    // $ANTLR start "rule__NativeDef__Group__1__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1376:1: rule__NativeDef__Group__1__Impl : ( 'native' ) ;
    public final void rule__NativeDef__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1380:1: ( ( 'native' ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1381:1: ( 'native' )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1381:1: ( 'native' )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1382:1: 'native'
            {
             before(grammarAccess.getNativeDefAccess().getNativeKeyword_1()); 
            match(input,21,FOLLOW_21_in_rule__NativeDef__Group__1__Impl2771); 
             after(grammarAccess.getNativeDefAccess().getNativeKeyword_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__Group__1__Impl"


    // $ANTLR start "rule__NativeDef__Group__2"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1395:1: rule__NativeDef__Group__2 : rule__NativeDef__Group__2__Impl rule__NativeDef__Group__3 ;
    public final void rule__NativeDef__Group__2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1399:1: ( rule__NativeDef__Group__2__Impl rule__NativeDef__Group__3 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1400:2: rule__NativeDef__Group__2__Impl rule__NativeDef__Group__3
            {
            pushFollow(FOLLOW_rule__NativeDef__Group__2__Impl_in_rule__NativeDef__Group__22802);
            rule__NativeDef__Group__2__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__NativeDef__Group__3_in_rule__NativeDef__Group__22805);
            rule__NativeDef__Group__3();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__Group__2"


    // $ANTLR start "rule__NativeDef__Group__2__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1407:1: rule__NativeDef__Group__2__Impl : ( ( rule__NativeDef__NameAssignment_2 ) ) ;
    public final void rule__NativeDef__Group__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1411:1: ( ( ( rule__NativeDef__NameAssignment_2 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1412:1: ( ( rule__NativeDef__NameAssignment_2 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1412:1: ( ( rule__NativeDef__NameAssignment_2 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1413:1: ( rule__NativeDef__NameAssignment_2 )
            {
             before(grammarAccess.getNativeDefAccess().getNameAssignment_2()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1414:1: ( rule__NativeDef__NameAssignment_2 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1414:2: rule__NativeDef__NameAssignment_2
            {
            pushFollow(FOLLOW_rule__NativeDef__NameAssignment_2_in_rule__NativeDef__Group__2__Impl2832);
            rule__NativeDef__NameAssignment_2();

            state._fsp--;


            }

             after(grammarAccess.getNativeDefAccess().getNameAssignment_2()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__Group__2__Impl"


    // $ANTLR start "rule__NativeDef__Group__3"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1424:1: rule__NativeDef__Group__3 : rule__NativeDef__Group__3__Impl rule__NativeDef__Group__4 ;
    public final void rule__NativeDef__Group__3() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1428:1: ( rule__NativeDef__Group__3__Impl rule__NativeDef__Group__4 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1429:2: rule__NativeDef__Group__3__Impl rule__NativeDef__Group__4
            {
            pushFollow(FOLLOW_rule__NativeDef__Group__3__Impl_in_rule__NativeDef__Group__32862);
            rule__NativeDef__Group__3__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__NativeDef__Group__4_in_rule__NativeDef__Group__32865);
            rule__NativeDef__Group__4();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__Group__3"


    // $ANTLR start "rule__NativeDef__Group__3__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1436:1: rule__NativeDef__Group__3__Impl : ( 'takes' ) ;
    public final void rule__NativeDef__Group__3__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1440:1: ( ( 'takes' ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1441:1: ( 'takes' )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1441:1: ( 'takes' )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1442:1: 'takes'
            {
             before(grammarAccess.getNativeDefAccess().getTakesKeyword_3()); 
            match(input,22,FOLLOW_22_in_rule__NativeDef__Group__3__Impl2893); 
             after(grammarAccess.getNativeDefAccess().getTakesKeyword_3()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__Group__3__Impl"


    // $ANTLR start "rule__NativeDef__Group__4"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1455:1: rule__NativeDef__Group__4 : rule__NativeDef__Group__4__Impl rule__NativeDef__Group__5 ;
    public final void rule__NativeDef__Group__4() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1459:1: ( rule__NativeDef__Group__4__Impl rule__NativeDef__Group__5 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1460:2: rule__NativeDef__Group__4__Impl rule__NativeDef__Group__5
            {
            pushFollow(FOLLOW_rule__NativeDef__Group__4__Impl_in_rule__NativeDef__Group__42924);
            rule__NativeDef__Group__4__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__NativeDef__Group__5_in_rule__NativeDef__Group__42927);
            rule__NativeDef__Group__5();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__Group__4"


    // $ANTLR start "rule__NativeDef__Group__4__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1467:1: rule__NativeDef__Group__4__Impl : ( ( rule__NativeDef__ParamsAssignment_4 ) ) ;
    public final void rule__NativeDef__Group__4__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1471:1: ( ( ( rule__NativeDef__ParamsAssignment_4 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1472:1: ( ( rule__NativeDef__ParamsAssignment_4 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1472:1: ( ( rule__NativeDef__ParamsAssignment_4 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1473:1: ( rule__NativeDef__ParamsAssignment_4 )
            {
             before(grammarAccess.getNativeDefAccess().getParamsAssignment_4()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1474:1: ( rule__NativeDef__ParamsAssignment_4 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1474:2: rule__NativeDef__ParamsAssignment_4
            {
            pushFollow(FOLLOW_rule__NativeDef__ParamsAssignment_4_in_rule__NativeDef__Group__4__Impl2954);
            rule__NativeDef__ParamsAssignment_4();

            state._fsp--;


            }

             after(grammarAccess.getNativeDefAccess().getParamsAssignment_4()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__Group__4__Impl"


    // $ANTLR start "rule__NativeDef__Group__5"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1484:1: rule__NativeDef__Group__5 : rule__NativeDef__Group__5__Impl rule__NativeDef__Group__6 ;
    public final void rule__NativeDef__Group__5() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1488:1: ( rule__NativeDef__Group__5__Impl rule__NativeDef__Group__6 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1489:2: rule__NativeDef__Group__5__Impl rule__NativeDef__Group__6
            {
            pushFollow(FOLLOW_rule__NativeDef__Group__5__Impl_in_rule__NativeDef__Group__52984);
            rule__NativeDef__Group__5__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__NativeDef__Group__6_in_rule__NativeDef__Group__52987);
            rule__NativeDef__Group__6();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__Group__5"


    // $ANTLR start "rule__NativeDef__Group__5__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1496:1: rule__NativeDef__Group__5__Impl : ( 'returns' ) ;
    public final void rule__NativeDef__Group__5__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1500:1: ( ( 'returns' ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1501:1: ( 'returns' )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1501:1: ( 'returns' )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1502:1: 'returns'
            {
             before(grammarAccess.getNativeDefAccess().getReturnsKeyword_5()); 
            match(input,23,FOLLOW_23_in_rule__NativeDef__Group__5__Impl3015); 
             after(grammarAccess.getNativeDefAccess().getReturnsKeyword_5()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__Group__5__Impl"


    // $ANTLR start "rule__NativeDef__Group__6"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1515:1: rule__NativeDef__Group__6 : rule__NativeDef__Group__6__Impl ;
    public final void rule__NativeDef__Group__6() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1519:1: ( rule__NativeDef__Group__6__Impl )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1520:2: rule__NativeDef__Group__6__Impl
            {
            pushFollow(FOLLOW_rule__NativeDef__Group__6__Impl_in_rule__NativeDef__Group__63046);
            rule__NativeDef__Group__6__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__Group__6"


    // $ANTLR start "rule__NativeDef__Group__6__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1526:1: rule__NativeDef__Group__6__Impl : ( ( rule__NativeDef__ReturnTypeAssignment_6 ) ) ;
    public final void rule__NativeDef__Group__6__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1530:1: ( ( ( rule__NativeDef__ReturnTypeAssignment_6 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1531:1: ( ( rule__NativeDef__ReturnTypeAssignment_6 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1531:1: ( ( rule__NativeDef__ReturnTypeAssignment_6 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1532:1: ( rule__NativeDef__ReturnTypeAssignment_6 )
            {
             before(grammarAccess.getNativeDefAccess().getReturnTypeAssignment_6()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1533:1: ( rule__NativeDef__ReturnTypeAssignment_6 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1533:2: rule__NativeDef__ReturnTypeAssignment_6
            {
            pushFollow(FOLLOW_rule__NativeDef__ReturnTypeAssignment_6_in_rule__NativeDef__Group__6__Impl3073);
            rule__NativeDef__ReturnTypeAssignment_6();

            state._fsp--;


            }

             after(grammarAccess.getNativeDefAccess().getReturnTypeAssignment_6()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__Group__6__Impl"


    // $ANTLR start "rule__ReturnType__Group_0__0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1557:1: rule__ReturnType__Group_0__0 : rule__ReturnType__Group_0__0__Impl rule__ReturnType__Group_0__1 ;
    public final void rule__ReturnType__Group_0__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1561:1: ( rule__ReturnType__Group_0__0__Impl rule__ReturnType__Group_0__1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1562:2: rule__ReturnType__Group_0__0__Impl rule__ReturnType__Group_0__1
            {
            pushFollow(FOLLOW_rule__ReturnType__Group_0__0__Impl_in_rule__ReturnType__Group_0__03117);
            rule__ReturnType__Group_0__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__ReturnType__Group_0__1_in_rule__ReturnType__Group_0__03120);
            rule__ReturnType__Group_0__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__ReturnType__Group_0__0"


    // $ANTLR start "rule__ReturnType__Group_0__0__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1569:1: rule__ReturnType__Group_0__0__Impl : ( () ) ;
    public final void rule__ReturnType__Group_0__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1573:1: ( ( () ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1574:1: ( () )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1574:1: ( () )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1575:1: ()
            {
             before(grammarAccess.getReturnTypeAccess().getReturnsNothingAction_0_0()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1576:1: ()
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1578:1: 
            {
            }

             after(grammarAccess.getReturnTypeAccess().getReturnsNothingAction_0_0()); 

            }


            }

        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__ReturnType__Group_0__0__Impl"


    // $ANTLR start "rule__ReturnType__Group_0__1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1588:1: rule__ReturnType__Group_0__1 : rule__ReturnType__Group_0__1__Impl ;
    public final void rule__ReturnType__Group_0__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1592:1: ( rule__ReturnType__Group_0__1__Impl )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1593:2: rule__ReturnType__Group_0__1__Impl
            {
            pushFollow(FOLLOW_rule__ReturnType__Group_0__1__Impl_in_rule__ReturnType__Group_0__13178);
            rule__ReturnType__Group_0__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__ReturnType__Group_0__1"


    // $ANTLR start "rule__ReturnType__Group_0__1__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1599:1: rule__ReturnType__Group_0__1__Impl : ( 'nothing' ) ;
    public final void rule__ReturnType__Group_0__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1603:1: ( ( 'nothing' ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1604:1: ( 'nothing' )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1604:1: ( 'nothing' )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1605:1: 'nothing'
            {
             before(grammarAccess.getReturnTypeAccess().getNothingKeyword_0_1()); 
            match(input,24,FOLLOW_24_in_rule__ReturnType__Group_0__1__Impl3206); 
             after(grammarAccess.getReturnTypeAccess().getNothingKeyword_0_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__ReturnType__Group_0__1__Impl"


    // $ANTLR start "rule__FormalParameters__Group_0__0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1622:1: rule__FormalParameters__Group_0__0 : rule__FormalParameters__Group_0__0__Impl rule__FormalParameters__Group_0__1 ;
    public final void rule__FormalParameters__Group_0__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1626:1: ( rule__FormalParameters__Group_0__0__Impl rule__FormalParameters__Group_0__1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1627:2: rule__FormalParameters__Group_0__0__Impl rule__FormalParameters__Group_0__1
            {
            pushFollow(FOLLOW_rule__FormalParameters__Group_0__0__Impl_in_rule__FormalParameters__Group_0__03241);
            rule__FormalParameters__Group_0__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__FormalParameters__Group_0__1_in_rule__FormalParameters__Group_0__03244);
            rule__FormalParameters__Group_0__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameters__Group_0__0"


    // $ANTLR start "rule__FormalParameters__Group_0__0__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1634:1: rule__FormalParameters__Group_0__0__Impl : ( () ) ;
    public final void rule__FormalParameters__Group_0__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1638:1: ( ( () ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1639:1: ( () )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1639:1: ( () )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1640:1: ()
            {
             before(grammarAccess.getFormalParametersAccess().getParamNothingAction_0_0()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1641:1: ()
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1643:1: 
            {
            }

             after(grammarAccess.getFormalParametersAccess().getParamNothingAction_0_0()); 

            }


            }

        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameters__Group_0__0__Impl"


    // $ANTLR start "rule__FormalParameters__Group_0__1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1653:1: rule__FormalParameters__Group_0__1 : rule__FormalParameters__Group_0__1__Impl ;
    public final void rule__FormalParameters__Group_0__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1657:1: ( rule__FormalParameters__Group_0__1__Impl )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1658:2: rule__FormalParameters__Group_0__1__Impl
            {
            pushFollow(FOLLOW_rule__FormalParameters__Group_0__1__Impl_in_rule__FormalParameters__Group_0__13302);
            rule__FormalParameters__Group_0__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameters__Group_0__1"


    // $ANTLR start "rule__FormalParameters__Group_0__1__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1664:1: rule__FormalParameters__Group_0__1__Impl : ( 'nothing' ) ;
    public final void rule__FormalParameters__Group_0__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1668:1: ( ( 'nothing' ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1669:1: ( 'nothing' )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1669:1: ( 'nothing' )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1670:1: 'nothing'
            {
             before(grammarAccess.getFormalParametersAccess().getNothingKeyword_0_1()); 
            match(input,24,FOLLOW_24_in_rule__FormalParameters__Group_0__1__Impl3330); 
             after(grammarAccess.getFormalParametersAccess().getNothingKeyword_0_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameters__Group_0__1__Impl"


    // $ANTLR start "rule__FormalParameters__Group_1__0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1687:1: rule__FormalParameters__Group_1__0 : rule__FormalParameters__Group_1__0__Impl rule__FormalParameters__Group_1__1 ;
    public final void rule__FormalParameters__Group_1__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1691:1: ( rule__FormalParameters__Group_1__0__Impl rule__FormalParameters__Group_1__1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1692:2: rule__FormalParameters__Group_1__0__Impl rule__FormalParameters__Group_1__1
            {
            pushFollow(FOLLOW_rule__FormalParameters__Group_1__0__Impl_in_rule__FormalParameters__Group_1__03365);
            rule__FormalParameters__Group_1__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__FormalParameters__Group_1__1_in_rule__FormalParameters__Group_1__03368);
            rule__FormalParameters__Group_1__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameters__Group_1__0"


    // $ANTLR start "rule__FormalParameters__Group_1__0__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1699:1: rule__FormalParameters__Group_1__0__Impl : ( ( rule__FormalParameters__ParametersAssignment_1_0 ) ) ;
    public final void rule__FormalParameters__Group_1__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1703:1: ( ( ( rule__FormalParameters__ParametersAssignment_1_0 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1704:1: ( ( rule__FormalParameters__ParametersAssignment_1_0 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1704:1: ( ( rule__FormalParameters__ParametersAssignment_1_0 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1705:1: ( rule__FormalParameters__ParametersAssignment_1_0 )
            {
             before(grammarAccess.getFormalParametersAccess().getParametersAssignment_1_0()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1706:1: ( rule__FormalParameters__ParametersAssignment_1_0 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1706:2: rule__FormalParameters__ParametersAssignment_1_0
            {
            pushFollow(FOLLOW_rule__FormalParameters__ParametersAssignment_1_0_in_rule__FormalParameters__Group_1__0__Impl3395);
            rule__FormalParameters__ParametersAssignment_1_0();

            state._fsp--;


            }

             after(grammarAccess.getFormalParametersAccess().getParametersAssignment_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameters__Group_1__0__Impl"


    // $ANTLR start "rule__FormalParameters__Group_1__1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1716:1: rule__FormalParameters__Group_1__1 : rule__FormalParameters__Group_1__1__Impl ;
    public final void rule__FormalParameters__Group_1__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1720:1: ( rule__FormalParameters__Group_1__1__Impl )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1721:2: rule__FormalParameters__Group_1__1__Impl
            {
            pushFollow(FOLLOW_rule__FormalParameters__Group_1__1__Impl_in_rule__FormalParameters__Group_1__13425);
            rule__FormalParameters__Group_1__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameters__Group_1__1"


    // $ANTLR start "rule__FormalParameters__Group_1__1__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1727:1: rule__FormalParameters__Group_1__1__Impl : ( ( rule__FormalParameters__Group_1_1__0 )* ) ;
    public final void rule__FormalParameters__Group_1__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1731:1: ( ( ( rule__FormalParameters__Group_1_1__0 )* ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1732:1: ( ( rule__FormalParameters__Group_1_1__0 )* )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1732:1: ( ( rule__FormalParameters__Group_1_1__0 )* )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1733:1: ( rule__FormalParameters__Group_1_1__0 )*
            {
             before(grammarAccess.getFormalParametersAccess().getGroup_1_1()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1734:1: ( rule__FormalParameters__Group_1_1__0 )*
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0==20) ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1734:2: rule__FormalParameters__Group_1_1__0
            	    {
            	    pushFollow(FOLLOW_rule__FormalParameters__Group_1_1__0_in_rule__FormalParameters__Group_1__1__Impl3452);
            	    rule__FormalParameters__Group_1_1__0();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);

             after(grammarAccess.getFormalParametersAccess().getGroup_1_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameters__Group_1__1__Impl"


    // $ANTLR start "rule__FormalParameters__Group_1_1__0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1748:1: rule__FormalParameters__Group_1_1__0 : rule__FormalParameters__Group_1_1__0__Impl rule__FormalParameters__Group_1_1__1 ;
    public final void rule__FormalParameters__Group_1_1__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1752:1: ( rule__FormalParameters__Group_1_1__0__Impl rule__FormalParameters__Group_1_1__1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1753:2: rule__FormalParameters__Group_1_1__0__Impl rule__FormalParameters__Group_1_1__1
            {
            pushFollow(FOLLOW_rule__FormalParameters__Group_1_1__0__Impl_in_rule__FormalParameters__Group_1_1__03487);
            rule__FormalParameters__Group_1_1__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__FormalParameters__Group_1_1__1_in_rule__FormalParameters__Group_1_1__03490);
            rule__FormalParameters__Group_1_1__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameters__Group_1_1__0"


    // $ANTLR start "rule__FormalParameters__Group_1_1__0__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1760:1: rule__FormalParameters__Group_1_1__0__Impl : ( ',' ) ;
    public final void rule__FormalParameters__Group_1_1__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1764:1: ( ( ',' ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1765:1: ( ',' )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1765:1: ( ',' )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1766:1: ','
            {
             before(grammarAccess.getFormalParametersAccess().getCommaKeyword_1_1_0()); 
            match(input,20,FOLLOW_20_in_rule__FormalParameters__Group_1_1__0__Impl3518); 
             after(grammarAccess.getFormalParametersAccess().getCommaKeyword_1_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameters__Group_1_1__0__Impl"


    // $ANTLR start "rule__FormalParameters__Group_1_1__1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1779:1: rule__FormalParameters__Group_1_1__1 : rule__FormalParameters__Group_1_1__1__Impl ;
    public final void rule__FormalParameters__Group_1_1__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1783:1: ( rule__FormalParameters__Group_1_1__1__Impl )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1784:2: rule__FormalParameters__Group_1_1__1__Impl
            {
            pushFollow(FOLLOW_rule__FormalParameters__Group_1_1__1__Impl_in_rule__FormalParameters__Group_1_1__13549);
            rule__FormalParameters__Group_1_1__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameters__Group_1_1__1"


    // $ANTLR start "rule__FormalParameters__Group_1_1__1__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1790:1: rule__FormalParameters__Group_1_1__1__Impl : ( ( rule__FormalParameters__ParametersAssignment_1_1_1 ) ) ;
    public final void rule__FormalParameters__Group_1_1__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1794:1: ( ( ( rule__FormalParameters__ParametersAssignment_1_1_1 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1795:1: ( ( rule__FormalParameters__ParametersAssignment_1_1_1 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1795:1: ( ( rule__FormalParameters__ParametersAssignment_1_1_1 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1796:1: ( rule__FormalParameters__ParametersAssignment_1_1_1 )
            {
             before(grammarAccess.getFormalParametersAccess().getParametersAssignment_1_1_1()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1797:1: ( rule__FormalParameters__ParametersAssignment_1_1_1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1797:2: rule__FormalParameters__ParametersAssignment_1_1_1
            {
            pushFollow(FOLLOW_rule__FormalParameters__ParametersAssignment_1_1_1_in_rule__FormalParameters__Group_1_1__1__Impl3576);
            rule__FormalParameters__ParametersAssignment_1_1_1();

            state._fsp--;


            }

             after(grammarAccess.getFormalParametersAccess().getParametersAssignment_1_1_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameters__Group_1_1__1__Impl"


    // $ANTLR start "rule__FormalParameter__Group__0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1811:1: rule__FormalParameter__Group__0 : rule__FormalParameter__Group__0__Impl rule__FormalParameter__Group__1 ;
    public final void rule__FormalParameter__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1815:1: ( rule__FormalParameter__Group__0__Impl rule__FormalParameter__Group__1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1816:2: rule__FormalParameter__Group__0__Impl rule__FormalParameter__Group__1
            {
            pushFollow(FOLLOW_rule__FormalParameter__Group__0__Impl_in_rule__FormalParameter__Group__03610);
            rule__FormalParameter__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__FormalParameter__Group__1_in_rule__FormalParameter__Group__03613);
            rule__FormalParameter__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameter__Group__0"


    // $ANTLR start "rule__FormalParameter__Group__0__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1823:1: rule__FormalParameter__Group__0__Impl : ( ( rule__FormalParameter__TypeAssignment_0 ) ) ;
    public final void rule__FormalParameter__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1827:1: ( ( ( rule__FormalParameter__TypeAssignment_0 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1828:1: ( ( rule__FormalParameter__TypeAssignment_0 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1828:1: ( ( rule__FormalParameter__TypeAssignment_0 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1829:1: ( rule__FormalParameter__TypeAssignment_0 )
            {
             before(grammarAccess.getFormalParameterAccess().getTypeAssignment_0()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1830:1: ( rule__FormalParameter__TypeAssignment_0 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1830:2: rule__FormalParameter__TypeAssignment_0
            {
            pushFollow(FOLLOW_rule__FormalParameter__TypeAssignment_0_in_rule__FormalParameter__Group__0__Impl3640);
            rule__FormalParameter__TypeAssignment_0();

            state._fsp--;


            }

             after(grammarAccess.getFormalParameterAccess().getTypeAssignment_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameter__Group__0__Impl"


    // $ANTLR start "rule__FormalParameter__Group__1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1840:1: rule__FormalParameter__Group__1 : rule__FormalParameter__Group__1__Impl ;
    public final void rule__FormalParameter__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1844:1: ( rule__FormalParameter__Group__1__Impl )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1845:2: rule__FormalParameter__Group__1__Impl
            {
            pushFollow(FOLLOW_rule__FormalParameter__Group__1__Impl_in_rule__FormalParameter__Group__13670);
            rule__FormalParameter__Group__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameter__Group__1"


    // $ANTLR start "rule__FormalParameter__Group__1__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1851:1: rule__FormalParameter__Group__1__Impl : ( ( rule__FormalParameter__NameAssignment_1 ) ) ;
    public final void rule__FormalParameter__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1855:1: ( ( ( rule__FormalParameter__NameAssignment_1 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1856:1: ( ( rule__FormalParameter__NameAssignment_1 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1856:1: ( ( rule__FormalParameter__NameAssignment_1 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1857:1: ( rule__FormalParameter__NameAssignment_1 )
            {
             before(grammarAccess.getFormalParameterAccess().getNameAssignment_1()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1858:1: ( rule__FormalParameter__NameAssignment_1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1858:2: rule__FormalParameter__NameAssignment_1
            {
            pushFollow(FOLLOW_rule__FormalParameter__NameAssignment_1_in_rule__FormalParameter__Group__1__Impl3697);
            rule__FormalParameter__NameAssignment_1();

            state._fsp--;


            }

             after(grammarAccess.getFormalParameterAccess().getNameAssignment_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameter__Group__1__Impl"


    // $ANTLR start "rule__TypeDef__Group__0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1872:1: rule__TypeDef__Group__0 : rule__TypeDef__Group__0__Impl rule__TypeDef__Group__1 ;
    public final void rule__TypeDef__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1876:1: ( rule__TypeDef__Group__0__Impl rule__TypeDef__Group__1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1877:2: rule__TypeDef__Group__0__Impl rule__TypeDef__Group__1
            {
            pushFollow(FOLLOW_rule__TypeDef__Group__0__Impl_in_rule__TypeDef__Group__03731);
            rule__TypeDef__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__TypeDef__Group__1_in_rule__TypeDef__Group__03734);
            rule__TypeDef__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__TypeDef__Group__0"


    // $ANTLR start "rule__TypeDef__Group__0__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1884:1: rule__TypeDef__Group__0__Impl : ( 'type' ) ;
    public final void rule__TypeDef__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1888:1: ( ( 'type' ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1889:1: ( 'type' )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1889:1: ( 'type' )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1890:1: 'type'
            {
             before(grammarAccess.getTypeDefAccess().getTypeKeyword_0()); 
            match(input,25,FOLLOW_25_in_rule__TypeDef__Group__0__Impl3762); 
             after(grammarAccess.getTypeDefAccess().getTypeKeyword_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__TypeDef__Group__0__Impl"


    // $ANTLR start "rule__TypeDef__Group__1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1903:1: rule__TypeDef__Group__1 : rule__TypeDef__Group__1__Impl rule__TypeDef__Group__2 ;
    public final void rule__TypeDef__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1907:1: ( rule__TypeDef__Group__1__Impl rule__TypeDef__Group__2 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1908:2: rule__TypeDef__Group__1__Impl rule__TypeDef__Group__2
            {
            pushFollow(FOLLOW_rule__TypeDef__Group__1__Impl_in_rule__TypeDef__Group__13793);
            rule__TypeDef__Group__1__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__TypeDef__Group__2_in_rule__TypeDef__Group__13796);
            rule__TypeDef__Group__2();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__TypeDef__Group__1"


    // $ANTLR start "rule__TypeDef__Group__1__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1915:1: rule__TypeDef__Group__1__Impl : ( ( rule__TypeDef__NameAssignment_1 ) ) ;
    public final void rule__TypeDef__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1919:1: ( ( ( rule__TypeDef__NameAssignment_1 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1920:1: ( ( rule__TypeDef__NameAssignment_1 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1920:1: ( ( rule__TypeDef__NameAssignment_1 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1921:1: ( rule__TypeDef__NameAssignment_1 )
            {
             before(grammarAccess.getTypeDefAccess().getNameAssignment_1()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1922:1: ( rule__TypeDef__NameAssignment_1 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1922:2: rule__TypeDef__NameAssignment_1
            {
            pushFollow(FOLLOW_rule__TypeDef__NameAssignment_1_in_rule__TypeDef__Group__1__Impl3823);
            rule__TypeDef__NameAssignment_1();

            state._fsp--;


            }

             after(grammarAccess.getTypeDefAccess().getNameAssignment_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__TypeDef__Group__1__Impl"


    // $ANTLR start "rule__TypeDef__Group__2"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1932:1: rule__TypeDef__Group__2 : rule__TypeDef__Group__2__Impl rule__TypeDef__Group__3 ;
    public final void rule__TypeDef__Group__2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1936:1: ( rule__TypeDef__Group__2__Impl rule__TypeDef__Group__3 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1937:2: rule__TypeDef__Group__2__Impl rule__TypeDef__Group__3
            {
            pushFollow(FOLLOW_rule__TypeDef__Group__2__Impl_in_rule__TypeDef__Group__23853);
            rule__TypeDef__Group__2__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__TypeDef__Group__3_in_rule__TypeDef__Group__23856);
            rule__TypeDef__Group__3();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__TypeDef__Group__2"


    // $ANTLR start "rule__TypeDef__Group__2__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1944:1: rule__TypeDef__Group__2__Impl : ( 'extends' ) ;
    public final void rule__TypeDef__Group__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1948:1: ( ( 'extends' ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1949:1: ( 'extends' )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1949:1: ( 'extends' )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1950:1: 'extends'
            {
             before(grammarAccess.getTypeDefAccess().getExtendsKeyword_2()); 
            match(input,26,FOLLOW_26_in_rule__TypeDef__Group__2__Impl3884); 
             after(grammarAccess.getTypeDefAccess().getExtendsKeyword_2()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__TypeDef__Group__2__Impl"


    // $ANTLR start "rule__TypeDef__Group__3"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1963:1: rule__TypeDef__Group__3 : rule__TypeDef__Group__3__Impl ;
    public final void rule__TypeDef__Group__3() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1967:1: ( rule__TypeDef__Group__3__Impl )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1968:2: rule__TypeDef__Group__3__Impl
            {
            pushFollow(FOLLOW_rule__TypeDef__Group__3__Impl_in_rule__TypeDef__Group__33915);
            rule__TypeDef__Group__3__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__TypeDef__Group__3"


    // $ANTLR start "rule__TypeDef__Group__3__Impl"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1974:1: rule__TypeDef__Group__3__Impl : ( ( rule__TypeDef__ExtendNameAssignment_3 ) ) ;
    public final void rule__TypeDef__Group__3__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1978:1: ( ( ( rule__TypeDef__ExtendNameAssignment_3 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1979:1: ( ( rule__TypeDef__ExtendNameAssignment_3 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1979:1: ( ( rule__TypeDef__ExtendNameAssignment_3 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1980:1: ( rule__TypeDef__ExtendNameAssignment_3 )
            {
             before(grammarAccess.getTypeDefAccess().getExtendNameAssignment_3()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1981:1: ( rule__TypeDef__ExtendNameAssignment_3 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:1981:2: rule__TypeDef__ExtendNameAssignment_3
            {
            pushFollow(FOLLOW_rule__TypeDef__ExtendNameAssignment_3_in_rule__TypeDef__Group__3__Impl3942);
            rule__TypeDef__ExtendNameAssignment_3();

            state._fsp--;


            }

             after(grammarAccess.getTypeDefAccess().getExtendNameAssignment_3()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__TypeDef__Group__3__Impl"


    // $ANTLR start "rule__Prog__ElemsAssignment"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2000:1: rule__Prog__ElemsAssignment : ( ruleEntity ) ;
    public final void rule__Prog__ElemsAssignment() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2004:1: ( ( ruleEntity ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2005:1: ( ruleEntity )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2005:1: ( ruleEntity )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2006:1: ruleEntity
            {
             before(grammarAccess.getProgAccess().getElemsEntityParserRuleCall_0()); 
            pushFollow(FOLLOW_ruleEntity_in_rule__Prog__ElemsAssignment3985);
            ruleEntity();

            state._fsp--;

             after(grammarAccess.getProgAccess().getElemsEntityParserRuleCall_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Prog__ElemsAssignment"


    // $ANTLR start "rule__GlobalBlock__VarsAssignment_2"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2015:1: rule__GlobalBlock__VarsAssignment_2 : ( ruleConstant ) ;
    public final void rule__GlobalBlock__VarsAssignment_2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2019:1: ( ( ruleConstant ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2020:1: ( ruleConstant )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2020:1: ( ruleConstant )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2021:1: ruleConstant
            {
             before(grammarAccess.getGlobalBlockAccess().getVarsConstantParserRuleCall_2_0()); 
            pushFollow(FOLLOW_ruleConstant_in_rule__GlobalBlock__VarsAssignment_24016);
            ruleConstant();

            state._fsp--;

             after(grammarAccess.getGlobalBlockAccess().getVarsConstantParserRuleCall_2_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__GlobalBlock__VarsAssignment_2"


    // $ANTLR start "rule__Constant__TypeAssignment_1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2030:1: rule__Constant__TypeAssignment_1 : ( RULE_ID ) ;
    public final void rule__Constant__TypeAssignment_1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2034:1: ( ( RULE_ID ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2035:1: ( RULE_ID )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2035:1: ( RULE_ID )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2036:1: RULE_ID
            {
             before(grammarAccess.getConstantAccess().getTypeIDTerminalRuleCall_1_0()); 
            match(input,RULE_ID,FOLLOW_RULE_ID_in_rule__Constant__TypeAssignment_14047); 
             after(grammarAccess.getConstantAccess().getTypeIDTerminalRuleCall_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Constant__TypeAssignment_1"


    // $ANTLR start "rule__Constant__NameAssignment_2"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2045:1: rule__Constant__NameAssignment_2 : ( RULE_ID ) ;
    public final void rule__Constant__NameAssignment_2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2049:1: ( ( RULE_ID ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2050:1: ( RULE_ID )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2050:1: ( RULE_ID )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2051:1: RULE_ID
            {
             before(grammarAccess.getConstantAccess().getNameIDTerminalRuleCall_2_0()); 
            match(input,RULE_ID,FOLLOW_RULE_ID_in_rule__Constant__NameAssignment_24078); 
             after(grammarAccess.getConstantAccess().getNameIDTerminalRuleCall_2_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Constant__NameAssignment_2"


    // $ANTLR start "rule__Constant__ValueAssignment_4"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2060:1: rule__Constant__ValueAssignment_4 : ( ruleExpr ) ;
    public final void rule__Constant__ValueAssignment_4() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2064:1: ( ( ruleExpr ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2065:1: ( ruleExpr )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2065:1: ( ruleExpr )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2066:1: ruleExpr
            {
             before(grammarAccess.getConstantAccess().getValueExprParserRuleCall_4_0()); 
            pushFollow(FOLLOW_ruleExpr_in_rule__Constant__ValueAssignment_44109);
            ruleExpr();

            state._fsp--;

             after(grammarAccess.getConstantAccess().getValueExprParserRuleCall_4_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Constant__ValueAssignment_4"


    // $ANTLR start "rule__Mult__LeftAssignment_0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2075:1: rule__Mult__LeftAssignment_0 : ( ruleLiteral ) ;
    public final void rule__Mult__LeftAssignment_0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2079:1: ( ( ruleLiteral ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2080:1: ( ruleLiteral )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2080:1: ( ruleLiteral )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2081:1: ruleLiteral
            {
             before(grammarAccess.getMultAccess().getLeftLiteralParserRuleCall_0_0()); 
            pushFollow(FOLLOW_ruleLiteral_in_rule__Mult__LeftAssignment_04140);
            ruleLiteral();

            state._fsp--;

             after(grammarAccess.getMultAccess().getLeftLiteralParserRuleCall_0_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Mult__LeftAssignment_0"


    // $ANTLR start "rule__Mult__RightAssignment_2"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2090:1: rule__Mult__RightAssignment_2 : ( ruleLiteral ) ;
    public final void rule__Mult__RightAssignment_2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2094:1: ( ( ruleLiteral ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2095:1: ( ruleLiteral )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2095:1: ( ruleLiteral )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2096:1: ruleLiteral
            {
             before(grammarAccess.getMultAccess().getRightLiteralParserRuleCall_2_0()); 
            pushFollow(FOLLOW_ruleLiteral_in_rule__Mult__RightAssignment_24171);
            ruleLiteral();

            state._fsp--;

             after(grammarAccess.getMultAccess().getRightLiteralParserRuleCall_2_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Mult__RightAssignment_2"


    // $ANTLR start "rule__FunctionCall__NameAssignment_0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2105:1: rule__FunctionCall__NameAssignment_0 : ( RULE_ID ) ;
    public final void rule__FunctionCall__NameAssignment_0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2109:1: ( ( RULE_ID ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2110:1: ( RULE_ID )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2110:1: ( RULE_ID )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2111:1: RULE_ID
            {
             before(grammarAccess.getFunctionCallAccess().getNameIDTerminalRuleCall_0_0()); 
            match(input,RULE_ID,FOLLOW_RULE_ID_in_rule__FunctionCall__NameAssignment_04202); 
             after(grammarAccess.getFunctionCallAccess().getNameIDTerminalRuleCall_0_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__NameAssignment_0"


    // $ANTLR start "rule__FunctionCall__ParametersAssignment_2_0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2120:1: rule__FunctionCall__ParametersAssignment_2_0 : ( ruleExpr ) ;
    public final void rule__FunctionCall__ParametersAssignment_2_0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2124:1: ( ( ruleExpr ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2125:1: ( ruleExpr )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2125:1: ( ruleExpr )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2126:1: ruleExpr
            {
             before(grammarAccess.getFunctionCallAccess().getParametersExprParserRuleCall_2_0_0()); 
            pushFollow(FOLLOW_ruleExpr_in_rule__FunctionCall__ParametersAssignment_2_04233);
            ruleExpr();

            state._fsp--;

             after(grammarAccess.getFunctionCallAccess().getParametersExprParserRuleCall_2_0_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__ParametersAssignment_2_0"


    // $ANTLR start "rule__FunctionCall__ParametersAssignment_2_1_1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2135:1: rule__FunctionCall__ParametersAssignment_2_1_1 : ( ruleExpr ) ;
    public final void rule__FunctionCall__ParametersAssignment_2_1_1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2139:1: ( ( ruleExpr ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2140:1: ( ruleExpr )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2140:1: ( ruleExpr )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2141:1: ruleExpr
            {
             before(grammarAccess.getFunctionCallAccess().getParametersExprParserRuleCall_2_1_1_0()); 
            pushFollow(FOLLOW_ruleExpr_in_rule__FunctionCall__ParametersAssignment_2_1_14264);
            ruleExpr();

            state._fsp--;

             after(grammarAccess.getFunctionCallAccess().getParametersExprParserRuleCall_2_1_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FunctionCall__ParametersAssignment_2_1_1"


    // $ANTLR start "rule__Literal__IntValAssignment_0_1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2150:1: rule__Literal__IntValAssignment_0_1 : ( RULE_INT ) ;
    public final void rule__Literal__IntValAssignment_0_1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2154:1: ( ( RULE_INT ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2155:1: ( RULE_INT )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2155:1: ( RULE_INT )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2156:1: RULE_INT
            {
             before(grammarAccess.getLiteralAccess().getIntValINTTerminalRuleCall_0_1_0()); 
            match(input,RULE_INT,FOLLOW_RULE_INT_in_rule__Literal__IntValAssignment_0_14295); 
             after(grammarAccess.getLiteralAccess().getIntValINTTerminalRuleCall_0_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Literal__IntValAssignment_0_1"


    // $ANTLR start "rule__Literal__BoolValAssignment_1_1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2165:1: rule__Literal__BoolValAssignment_1_1 : ( ( rule__Literal__BoolValAlternatives_1_1_0 ) ) ;
    public final void rule__Literal__BoolValAssignment_1_1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2169:1: ( ( ( rule__Literal__BoolValAlternatives_1_1_0 ) ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2170:1: ( ( rule__Literal__BoolValAlternatives_1_1_0 ) )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2170:1: ( ( rule__Literal__BoolValAlternatives_1_1_0 ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2171:1: ( rule__Literal__BoolValAlternatives_1_1_0 )
            {
             before(grammarAccess.getLiteralAccess().getBoolValAlternatives_1_1_0()); 
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2172:1: ( rule__Literal__BoolValAlternatives_1_1_0 )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2172:2: rule__Literal__BoolValAlternatives_1_1_0
            {
            pushFollow(FOLLOW_rule__Literal__BoolValAlternatives_1_1_0_in_rule__Literal__BoolValAssignment_1_14326);
            rule__Literal__BoolValAlternatives_1_1_0();

            state._fsp--;


            }

             after(grammarAccess.getLiteralAccess().getBoolValAlternatives_1_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Literal__BoolValAssignment_1_1"


    // $ANTLR start "rule__NativeDef__NameAssignment_2"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2181:1: rule__NativeDef__NameAssignment_2 : ( RULE_ID ) ;
    public final void rule__NativeDef__NameAssignment_2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2185:1: ( ( RULE_ID ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2186:1: ( RULE_ID )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2186:1: ( RULE_ID )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2187:1: RULE_ID
            {
             before(grammarAccess.getNativeDefAccess().getNameIDTerminalRuleCall_2_0()); 
            match(input,RULE_ID,FOLLOW_RULE_ID_in_rule__NativeDef__NameAssignment_24359); 
             after(grammarAccess.getNativeDefAccess().getNameIDTerminalRuleCall_2_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__NameAssignment_2"


    // $ANTLR start "rule__NativeDef__ParamsAssignment_4"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2196:1: rule__NativeDef__ParamsAssignment_4 : ( ruleFormalParameters ) ;
    public final void rule__NativeDef__ParamsAssignment_4() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2200:1: ( ( ruleFormalParameters ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2201:1: ( ruleFormalParameters )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2201:1: ( ruleFormalParameters )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2202:1: ruleFormalParameters
            {
             before(grammarAccess.getNativeDefAccess().getParamsFormalParametersParserRuleCall_4_0()); 
            pushFollow(FOLLOW_ruleFormalParameters_in_rule__NativeDef__ParamsAssignment_44390);
            ruleFormalParameters();

            state._fsp--;

             after(grammarAccess.getNativeDefAccess().getParamsFormalParametersParserRuleCall_4_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__ParamsAssignment_4"


    // $ANTLR start "rule__NativeDef__ReturnTypeAssignment_6"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2211:1: rule__NativeDef__ReturnTypeAssignment_6 : ( ruleReturnType ) ;
    public final void rule__NativeDef__ReturnTypeAssignment_6() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2215:1: ( ( ruleReturnType ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2216:1: ( ruleReturnType )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2216:1: ( ruleReturnType )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2217:1: ruleReturnType
            {
             before(grammarAccess.getNativeDefAccess().getReturnTypeReturnTypeParserRuleCall_6_0()); 
            pushFollow(FOLLOW_ruleReturnType_in_rule__NativeDef__ReturnTypeAssignment_64421);
            ruleReturnType();

            state._fsp--;

             after(grammarAccess.getNativeDefAccess().getReturnTypeReturnTypeParserRuleCall_6_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__NativeDef__ReturnTypeAssignment_6"


    // $ANTLR start "rule__ReturnType__NameAssignment_1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2226:1: rule__ReturnType__NameAssignment_1 : ( RULE_ID ) ;
    public final void rule__ReturnType__NameAssignment_1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2230:1: ( ( RULE_ID ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2231:1: ( RULE_ID )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2231:1: ( RULE_ID )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2232:1: RULE_ID
            {
             before(grammarAccess.getReturnTypeAccess().getNameIDTerminalRuleCall_1_0()); 
            match(input,RULE_ID,FOLLOW_RULE_ID_in_rule__ReturnType__NameAssignment_14452); 
             after(grammarAccess.getReturnTypeAccess().getNameIDTerminalRuleCall_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__ReturnType__NameAssignment_1"


    // $ANTLR start "rule__FormalParameters__ParametersAssignment_1_0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2241:1: rule__FormalParameters__ParametersAssignment_1_0 : ( ruleFormalParameter ) ;
    public final void rule__FormalParameters__ParametersAssignment_1_0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2245:1: ( ( ruleFormalParameter ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2246:1: ( ruleFormalParameter )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2246:1: ( ruleFormalParameter )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2247:1: ruleFormalParameter
            {
             before(grammarAccess.getFormalParametersAccess().getParametersFormalParameterParserRuleCall_1_0_0()); 
            pushFollow(FOLLOW_ruleFormalParameter_in_rule__FormalParameters__ParametersAssignment_1_04483);
            ruleFormalParameter();

            state._fsp--;

             after(grammarAccess.getFormalParametersAccess().getParametersFormalParameterParserRuleCall_1_0_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameters__ParametersAssignment_1_0"


    // $ANTLR start "rule__FormalParameters__ParametersAssignment_1_1_1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2256:1: rule__FormalParameters__ParametersAssignment_1_1_1 : ( ruleFormalParameter ) ;
    public final void rule__FormalParameters__ParametersAssignment_1_1_1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2260:1: ( ( ruleFormalParameter ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2261:1: ( ruleFormalParameter )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2261:1: ( ruleFormalParameter )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2262:1: ruleFormalParameter
            {
             before(grammarAccess.getFormalParametersAccess().getParametersFormalParameterParserRuleCall_1_1_1_0()); 
            pushFollow(FOLLOW_ruleFormalParameter_in_rule__FormalParameters__ParametersAssignment_1_1_14514);
            ruleFormalParameter();

            state._fsp--;

             after(grammarAccess.getFormalParametersAccess().getParametersFormalParameterParserRuleCall_1_1_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameters__ParametersAssignment_1_1_1"


    // $ANTLR start "rule__FormalParameter__TypeAssignment_0"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2271:1: rule__FormalParameter__TypeAssignment_0 : ( RULE_ID ) ;
    public final void rule__FormalParameter__TypeAssignment_0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2275:1: ( ( RULE_ID ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2276:1: ( RULE_ID )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2276:1: ( RULE_ID )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2277:1: RULE_ID
            {
             before(grammarAccess.getFormalParameterAccess().getTypeIDTerminalRuleCall_0_0()); 
            match(input,RULE_ID,FOLLOW_RULE_ID_in_rule__FormalParameter__TypeAssignment_04545); 
             after(grammarAccess.getFormalParameterAccess().getTypeIDTerminalRuleCall_0_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameter__TypeAssignment_0"


    // $ANTLR start "rule__FormalParameter__NameAssignment_1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2286:1: rule__FormalParameter__NameAssignment_1 : ( RULE_ID ) ;
    public final void rule__FormalParameter__NameAssignment_1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2290:1: ( ( RULE_ID ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2291:1: ( RULE_ID )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2291:1: ( RULE_ID )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2292:1: RULE_ID
            {
             before(grammarAccess.getFormalParameterAccess().getNameIDTerminalRuleCall_1_0()); 
            match(input,RULE_ID,FOLLOW_RULE_ID_in_rule__FormalParameter__NameAssignment_14576); 
             after(grammarAccess.getFormalParameterAccess().getNameIDTerminalRuleCall_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__FormalParameter__NameAssignment_1"


    // $ANTLR start "rule__TypeDef__NameAssignment_1"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2301:1: rule__TypeDef__NameAssignment_1 : ( RULE_ID ) ;
    public final void rule__TypeDef__NameAssignment_1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2305:1: ( ( RULE_ID ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2306:1: ( RULE_ID )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2306:1: ( RULE_ID )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2307:1: RULE_ID
            {
             before(grammarAccess.getTypeDefAccess().getNameIDTerminalRuleCall_1_0()); 
            match(input,RULE_ID,FOLLOW_RULE_ID_in_rule__TypeDef__NameAssignment_14607); 
             after(grammarAccess.getTypeDefAccess().getNameIDTerminalRuleCall_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__TypeDef__NameAssignment_1"


    // $ANTLR start "rule__TypeDef__ExtendNameAssignment_3"
    // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2316:1: rule__TypeDef__ExtendNameAssignment_3 : ( RULE_ID ) ;
    public final void rule__TypeDef__ExtendNameAssignment_3() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2320:1: ( ( RULE_ID ) )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2321:1: ( RULE_ID )
            {
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2321:1: ( RULE_ID )
            // ../de.peeeq.jassToPscript.ui/src-gen/de/peeeq/ui/contentassist/antlr/internal/InternalJassToPscript.g:2322:1: RULE_ID
            {
             before(grammarAccess.getTypeDefAccess().getExtendNameIDTerminalRuleCall_3_0()); 
            match(input,RULE_ID,FOLLOW_RULE_ID_in_rule__TypeDef__ExtendNameAssignment_34638); 
             after(grammarAccess.getTypeDefAccess().getExtendNameIDTerminalRuleCall_3_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__TypeDef__ExtendNameAssignment_3"

    // Delegated rules


 

    public static final BitSet FOLLOW_ruleProg_in_entryRuleProg61 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleProg68 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Prog__ElemsAssignment_in_ruleProg94 = new BitSet(new long[]{0x000000000220A002L});
    public static final BitSet FOLLOW_ruleEntity_in_entryRuleEntity122 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleEntity129 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Entity__Alternatives_in_ruleEntity155 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleGlobalBlock_in_entryRuleGlobalBlock182 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleGlobalBlock189 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__GlobalBlock__Group__0_in_ruleGlobalBlock215 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleConstant_in_entryRuleConstant242 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleConstant249 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Constant__Group__0_in_ruleConstant275 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExpr_in_entryRuleExpr302 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExpr309 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Expr__Alternatives_in_ruleExpr335 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleMult_in_entryRuleMult362 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleMult369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Mult__Group__0_in_ruleMult395 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFunctionCall_in_entryRuleFunctionCall422 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleFunctionCall429 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FunctionCall__Group__0_in_ruleFunctionCall455 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleLiteral_in_entryRuleLiteral482 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleLiteral489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Literal__Alternatives_in_ruleLiteral515 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNativeDef_in_entryRuleNativeDef542 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleNativeDef549 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__NativeDef__Group__0_in_ruleNativeDef575 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleReturnType_in_entryRuleReturnType602 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleReturnType609 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ReturnType__Alternatives_in_ruleReturnType635 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFormalParameters_in_entryRuleFormalParameters662 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleFormalParameters669 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FormalParameters__Alternatives_in_ruleFormalParameters695 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFormalParameter_in_entryRuleFormalParameter722 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleFormalParameter729 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FormalParameter__Group__0_in_ruleFormalParameter755 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleTypeDef_in_entryRuleTypeDef782 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleTypeDef789 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__TypeDef__Group__0_in_ruleTypeDef815 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleTypeDef_in_rule__Entity__Alternatives851 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleGlobalBlock_in_rule__Entity__Alternatives868 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNativeDef_in_rule__Entity__Alternatives885 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleLiteral_in_rule__Expr__Alternatives917 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFunctionCall_in_rule__Expr__Alternatives934 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleMult_in_rule__Expr__Alternatives951 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Literal__Group_0__0_in_rule__Literal__Alternatives983 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Literal__Group_1__0_in_rule__Literal__Alternatives1001 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_11_in_rule__Literal__BoolValAlternatives_1_1_01035 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_12_in_rule__Literal__BoolValAlternatives_1_1_01055 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ReturnType__Group_0__0_in_rule__ReturnType__Alternatives1089 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ReturnType__NameAssignment_1_in_rule__ReturnType__Alternatives1107 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FormalParameters__Group_0__0_in_rule__FormalParameters__Alternatives1140 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FormalParameters__Group_1__0_in_rule__FormalParameters__Alternatives1158 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__GlobalBlock__Group__0__Impl_in_rule__GlobalBlock__Group__01189 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_rule__GlobalBlock__Group__1_in_rule__GlobalBlock__Group__01192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__GlobalBlock__Group__1__Impl_in_rule__GlobalBlock__Group__11250 = new BitSet(new long[]{0x000000000000C000L});
    public static final BitSet FOLLOW_rule__GlobalBlock__Group__2_in_rule__GlobalBlock__Group__11253 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_13_in_rule__GlobalBlock__Group__1__Impl1281 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__GlobalBlock__Group__2__Impl_in_rule__GlobalBlock__Group__21312 = new BitSet(new long[]{0x000000000000C000L});
    public static final BitSet FOLLOW_rule__GlobalBlock__Group__3_in_rule__GlobalBlock__Group__21315 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__GlobalBlock__VarsAssignment_2_in_rule__GlobalBlock__Group__2__Impl1342 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_rule__GlobalBlock__Group__3__Impl_in_rule__GlobalBlock__Group__31373 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_14_in_rule__GlobalBlock__Group__3__Impl1401 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Constant__Group__0__Impl_in_rule__Constant__Group__01440 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_rule__Constant__Group__1_in_rule__Constant__Group__01443 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_15_in_rule__Constant__Group__0__Impl1471 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Constant__Group__1__Impl_in_rule__Constant__Group__11502 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_rule__Constant__Group__2_in_rule__Constant__Group__11505 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Constant__TypeAssignment_1_in_rule__Constant__Group__1__Impl1532 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Constant__Group__2__Impl_in_rule__Constant__Group__21562 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_rule__Constant__Group__3_in_rule__Constant__Group__21565 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Constant__NameAssignment_2_in_rule__Constant__Group__2__Impl1592 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Constant__Group__3__Impl_in_rule__Constant__Group__31622 = new BitSet(new long[]{0x0000000000001830L});
    public static final BitSet FOLLOW_rule__Constant__Group__4_in_rule__Constant__Group__31625 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_16_in_rule__Constant__Group__3__Impl1653 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Constant__Group__4__Impl_in_rule__Constant__Group__41684 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Constant__ValueAssignment_4_in_rule__Constant__Group__4__Impl1711 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Mult__Group__0__Impl_in_rule__Mult__Group__01751 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_rule__Mult__Group__1_in_rule__Mult__Group__01754 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Mult__LeftAssignment_0_in_rule__Mult__Group__0__Impl1781 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Mult__Group__1__Impl_in_rule__Mult__Group__11811 = new BitSet(new long[]{0x0000000000001820L});
    public static final BitSet FOLLOW_rule__Mult__Group__2_in_rule__Mult__Group__11814 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_17_in_rule__Mult__Group__1__Impl1842 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Mult__Group__2__Impl_in_rule__Mult__Group__21873 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Mult__RightAssignment_2_in_rule__Mult__Group__2__Impl1900 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FunctionCall__Group__0__Impl_in_rule__FunctionCall__Group__01936 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_rule__FunctionCall__Group__1_in_rule__FunctionCall__Group__01939 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FunctionCall__NameAssignment_0_in_rule__FunctionCall__Group__0__Impl1966 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FunctionCall__Group__1__Impl_in_rule__FunctionCall__Group__11996 = new BitSet(new long[]{0x0000000000081830L});
    public static final BitSet FOLLOW_rule__FunctionCall__Group__2_in_rule__FunctionCall__Group__11999 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_18_in_rule__FunctionCall__Group__1__Impl2027 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FunctionCall__Group__2__Impl_in_rule__FunctionCall__Group__22058 = new BitSet(new long[]{0x0000000000081830L});
    public static final BitSet FOLLOW_rule__FunctionCall__Group__3_in_rule__FunctionCall__Group__22061 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FunctionCall__Group_2__0_in_rule__FunctionCall__Group__2__Impl2088 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FunctionCall__Group__3__Impl_in_rule__FunctionCall__Group__32119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_19_in_rule__FunctionCall__Group__3__Impl2147 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FunctionCall__Group_2__0__Impl_in_rule__FunctionCall__Group_2__02186 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_rule__FunctionCall__Group_2__1_in_rule__FunctionCall__Group_2__02189 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FunctionCall__ParametersAssignment_2_0_in_rule__FunctionCall__Group_2__0__Impl2216 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FunctionCall__Group_2__1__Impl_in_rule__FunctionCall__Group_2__12246 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FunctionCall__Group_2_1__0_in_rule__FunctionCall__Group_2__1__Impl2273 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_rule__FunctionCall__Group_2_1__0__Impl_in_rule__FunctionCall__Group_2_1__02308 = new BitSet(new long[]{0x0000000000001830L});
    public static final BitSet FOLLOW_rule__FunctionCall__Group_2_1__1_in_rule__FunctionCall__Group_2_1__02311 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_20_in_rule__FunctionCall__Group_2_1__0__Impl2339 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FunctionCall__Group_2_1__1__Impl_in_rule__FunctionCall__Group_2_1__12370 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FunctionCall__ParametersAssignment_2_1_1_in_rule__FunctionCall__Group_2_1__1__Impl2397 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Literal__Group_0__0__Impl_in_rule__Literal__Group_0__02431 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_rule__Literal__Group_0__1_in_rule__Literal__Group_0__02434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Literal__Group_0__1__Impl_in_rule__Literal__Group_0__12492 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Literal__IntValAssignment_0_1_in_rule__Literal__Group_0__1__Impl2519 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Literal__Group_1__0__Impl_in_rule__Literal__Group_1__02553 = new BitSet(new long[]{0x0000000000001820L});
    public static final BitSet FOLLOW_rule__Literal__Group_1__1_in_rule__Literal__Group_1__02556 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Literal__Group_1__1__Impl_in_rule__Literal__Group_1__12614 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Literal__BoolValAssignment_1_1_in_rule__Literal__Group_1__1__Impl2641 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__NativeDef__Group__0__Impl_in_rule__NativeDef__Group__02675 = new BitSet(new long[]{0x000000000220A000L});
    public static final BitSet FOLLOW_rule__NativeDef__Group__1_in_rule__NativeDef__Group__02678 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_15_in_rule__NativeDef__Group__0__Impl2707 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__NativeDef__Group__1__Impl_in_rule__NativeDef__Group__12740 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_rule__NativeDef__Group__2_in_rule__NativeDef__Group__12743 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_21_in_rule__NativeDef__Group__1__Impl2771 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__NativeDef__Group__2__Impl_in_rule__NativeDef__Group__22802 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_rule__NativeDef__Group__3_in_rule__NativeDef__Group__22805 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__NativeDef__NameAssignment_2_in_rule__NativeDef__Group__2__Impl2832 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__NativeDef__Group__3__Impl_in_rule__NativeDef__Group__32862 = new BitSet(new long[]{0x0000000001000010L});
    public static final BitSet FOLLOW_rule__NativeDef__Group__4_in_rule__NativeDef__Group__32865 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_22_in_rule__NativeDef__Group__3__Impl2893 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__NativeDef__Group__4__Impl_in_rule__NativeDef__Group__42924 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_rule__NativeDef__Group__5_in_rule__NativeDef__Group__42927 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__NativeDef__ParamsAssignment_4_in_rule__NativeDef__Group__4__Impl2954 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__NativeDef__Group__5__Impl_in_rule__NativeDef__Group__52984 = new BitSet(new long[]{0x0000000001000010L});
    public static final BitSet FOLLOW_rule__NativeDef__Group__6_in_rule__NativeDef__Group__52987 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_23_in_rule__NativeDef__Group__5__Impl3015 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__NativeDef__Group__6__Impl_in_rule__NativeDef__Group__63046 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__NativeDef__ReturnTypeAssignment_6_in_rule__NativeDef__Group__6__Impl3073 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ReturnType__Group_0__0__Impl_in_rule__ReturnType__Group_0__03117 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_rule__ReturnType__Group_0__1_in_rule__ReturnType__Group_0__03120 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ReturnType__Group_0__1__Impl_in_rule__ReturnType__Group_0__13178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_rule__ReturnType__Group_0__1__Impl3206 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FormalParameters__Group_0__0__Impl_in_rule__FormalParameters__Group_0__03241 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_rule__FormalParameters__Group_0__1_in_rule__FormalParameters__Group_0__03244 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FormalParameters__Group_0__1__Impl_in_rule__FormalParameters__Group_0__13302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_rule__FormalParameters__Group_0__1__Impl3330 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FormalParameters__Group_1__0__Impl_in_rule__FormalParameters__Group_1__03365 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_rule__FormalParameters__Group_1__1_in_rule__FormalParameters__Group_1__03368 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FormalParameters__ParametersAssignment_1_0_in_rule__FormalParameters__Group_1__0__Impl3395 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FormalParameters__Group_1__1__Impl_in_rule__FormalParameters__Group_1__13425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FormalParameters__Group_1_1__0_in_rule__FormalParameters__Group_1__1__Impl3452 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_rule__FormalParameters__Group_1_1__0__Impl_in_rule__FormalParameters__Group_1_1__03487 = new BitSet(new long[]{0x0000000001000010L});
    public static final BitSet FOLLOW_rule__FormalParameters__Group_1_1__1_in_rule__FormalParameters__Group_1_1__03490 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_20_in_rule__FormalParameters__Group_1_1__0__Impl3518 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FormalParameters__Group_1_1__1__Impl_in_rule__FormalParameters__Group_1_1__13549 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FormalParameters__ParametersAssignment_1_1_1_in_rule__FormalParameters__Group_1_1__1__Impl3576 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FormalParameter__Group__0__Impl_in_rule__FormalParameter__Group__03610 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_rule__FormalParameter__Group__1_in_rule__FormalParameter__Group__03613 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FormalParameter__TypeAssignment_0_in_rule__FormalParameter__Group__0__Impl3640 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FormalParameter__Group__1__Impl_in_rule__FormalParameter__Group__13670 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__FormalParameter__NameAssignment_1_in_rule__FormalParameter__Group__1__Impl3697 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__TypeDef__Group__0__Impl_in_rule__TypeDef__Group__03731 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_rule__TypeDef__Group__1_in_rule__TypeDef__Group__03734 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_25_in_rule__TypeDef__Group__0__Impl3762 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__TypeDef__Group__1__Impl_in_rule__TypeDef__Group__13793 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_rule__TypeDef__Group__2_in_rule__TypeDef__Group__13796 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__TypeDef__NameAssignment_1_in_rule__TypeDef__Group__1__Impl3823 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__TypeDef__Group__2__Impl_in_rule__TypeDef__Group__23853 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_rule__TypeDef__Group__3_in_rule__TypeDef__Group__23856 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_rule__TypeDef__Group__2__Impl3884 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__TypeDef__Group__3__Impl_in_rule__TypeDef__Group__33915 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__TypeDef__ExtendNameAssignment_3_in_rule__TypeDef__Group__3__Impl3942 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleEntity_in_rule__Prog__ElemsAssignment3985 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleConstant_in_rule__GlobalBlock__VarsAssignment_24016 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_rule__Constant__TypeAssignment_14047 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_rule__Constant__NameAssignment_24078 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExpr_in_rule__Constant__ValueAssignment_44109 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleLiteral_in_rule__Mult__LeftAssignment_04140 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleLiteral_in_rule__Mult__RightAssignment_24171 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_rule__FunctionCall__NameAssignment_04202 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExpr_in_rule__FunctionCall__ParametersAssignment_2_04233 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExpr_in_rule__FunctionCall__ParametersAssignment_2_1_14264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_INT_in_rule__Literal__IntValAssignment_0_14295 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Literal__BoolValAlternatives_1_1_0_in_rule__Literal__BoolValAssignment_1_14326 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_rule__NativeDef__NameAssignment_24359 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFormalParameters_in_rule__NativeDef__ParamsAssignment_44390 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleReturnType_in_rule__NativeDef__ReturnTypeAssignment_64421 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_rule__ReturnType__NameAssignment_14452 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFormalParameter_in_rule__FormalParameters__ParametersAssignment_1_04483 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFormalParameter_in_rule__FormalParameters__ParametersAssignment_1_1_14514 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_rule__FormalParameter__TypeAssignment_04545 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_rule__FormalParameter__NameAssignment_14576 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_rule__TypeDef__NameAssignment_14607 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_rule__TypeDef__ExtendNameAssignment_34638 = new BitSet(new long[]{0x0000000000000002L});

}