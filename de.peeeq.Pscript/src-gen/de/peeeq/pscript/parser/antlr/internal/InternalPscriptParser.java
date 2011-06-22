package de.peeeq.pscript.parser.antlr.internal; 

import java.io.InputStream;
import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.xtext.parsetree.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
import org.eclipse.xtext.conversion.ValueConverterException;
import de.peeeq.pscript.services.PscriptGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalPscriptParser extends AbstractInternalAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_NL", "RULE_ID", "RULE_OPERATOR", "RULE_INT", "RULE_NUMBER", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'package'", "'{'", "'}'", "'import'", "'.'", "'.*'", "'native'", "'type'", "'='", "'class'", "'var'", "'val'", "':'", "'function'", "'('", "','", "')'", "'return'", "'if'", "'else'", "'while'", "'+='", "'-='", "'or'", "'and'", "'!='", "'=='", "'<='", "'<'", "'>='", "'>'", "'+'", "'-'", "'*'", "'/'", "'%'", "'mod'", "'div'", "'not'", "'buildin'"
    };
    public static final int RULE_ID=5;
    public static final int RULE_STRING=9;
    public static final int RULE_NUMBER=8;
    public static final int RULE_ANY_OTHER=13;
    public static final int RULE_NL=4;
    public static final int RULE_INT=7;
    public static final int RULE_OPERATOR=6;
    public static final int RULE_WS=12;
    public static final int RULE_SL_COMMENT=11;
    public static final int EOF=-1;
    public static final int RULE_ML_COMMENT=10;

        public InternalPscriptParser(TokenStream input) {
            super(input);
        }
        

    public String[] getTokenNames() { return tokenNames; }
    public String getGrammarFileName() { return "../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g"; }



     	private PscriptGrammarAccess grammarAccess;
     	
        public InternalPscriptParser(TokenStream input, IAstFactory factory, PscriptGrammarAccess grammarAccess) {
            this(input);
            this.factory = factory;
            registerRules(grammarAccess.getGrammar());
            this.grammarAccess = grammarAccess;
        }
        
        @Override
        protected InputStream getTokenFile() {
        	ClassLoader classLoader = getClass().getClassLoader();
        	return classLoader.getResourceAsStream("de/peeeq/pscript/parser/antlr/internal/InternalPscript.tokens");
        }
        
        @Override
        protected String getFirstRuleName() {
        	return "Program";	
       	}
       	
       	@Override
       	protected PscriptGrammarAccess getGrammarAccess() {
       		return grammarAccess;
       	}



    // $ANTLR start entryRuleProgram
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:77:1: entryRuleProgram returns [EObject current=null] : iv_ruleProgram= ruleProgram EOF ;
    public final EObject entryRuleProgram() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleProgram = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:78:2: (iv_ruleProgram= ruleProgram EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:79:2: iv_ruleProgram= ruleProgram EOF
            {
             currentNode = createCompositeNode(grammarAccess.getProgramRule(), currentNode); 
            pushFollow(FOLLOW_ruleProgram_in_entryRuleProgram75);
            iv_ruleProgram=ruleProgram();
            _fsp--;

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
    // $ANTLR end entryRuleProgram


    // $ANTLR start ruleProgram
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:86:1: ruleProgram returns [EObject current=null] : ( ( RULE_NL )* ( (lv_packages_1_0= rulePackageDeclaration ) ) ( (lv_packages_2_0= rulePackageDeclaration ) )* ( RULE_NL )* ) ;
    public final EObject ruleProgram() throws RecognitionException {
        EObject current = null;

        EObject lv_packages_1_0 = null;

        EObject lv_packages_2_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:91:6: ( ( ( RULE_NL )* ( (lv_packages_1_0= rulePackageDeclaration ) ) ( (lv_packages_2_0= rulePackageDeclaration ) )* ( RULE_NL )* ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:92:1: ( ( RULE_NL )* ( (lv_packages_1_0= rulePackageDeclaration ) ) ( (lv_packages_2_0= rulePackageDeclaration ) )* ( RULE_NL )* )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:92:1: ( ( RULE_NL )* ( (lv_packages_1_0= rulePackageDeclaration ) ) ( (lv_packages_2_0= rulePackageDeclaration ) )* ( RULE_NL )* )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:92:2: ( RULE_NL )* ( (lv_packages_1_0= rulePackageDeclaration ) ) ( (lv_packages_2_0= rulePackageDeclaration ) )* ( RULE_NL )*
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:92:2: ( RULE_NL )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==RULE_NL) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:92:3: RULE_NL
            	    {
            	    match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleProgram120); 
            	     
            	        createLeafNode(grammarAccess.getProgramAccess().getNLTerminalRuleCall_0(), null); 
            	        

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:96:3: ( (lv_packages_1_0= rulePackageDeclaration ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:97:1: (lv_packages_1_0= rulePackageDeclaration )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:97:1: (lv_packages_1_0= rulePackageDeclaration )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:98:3: lv_packages_1_0= rulePackageDeclaration
            {
             
            	        currentNode=createCompositeNode(grammarAccess.getProgramAccess().getPackagesPackageDeclarationParserRuleCall_1_0(), currentNode); 
            	    
            pushFollow(FOLLOW_rulePackageDeclaration_in_ruleProgram142);
            lv_packages_1_0=rulePackageDeclaration();
            _fsp--;


            	        if (current==null) {
            	            current = factory.create(grammarAccess.getProgramRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode.getParent(), current);
            	        }
            	        try {
            	       		add(
            	       			current, 
            	       			"packages",
            	        		lv_packages_1_0, 
            	        		"PackageDeclaration", 
            	        		currentNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	        currentNode = currentNode.getParent();
            	    

            }


            }

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:120:2: ( (lv_packages_2_0= rulePackageDeclaration ) )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==14) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:121:1: (lv_packages_2_0= rulePackageDeclaration )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:121:1: (lv_packages_2_0= rulePackageDeclaration )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:122:3: lv_packages_2_0= rulePackageDeclaration
            	    {
            	     
            	    	        currentNode=createCompositeNode(grammarAccess.getProgramAccess().getPackagesPackageDeclarationParserRuleCall_2_0(), currentNode); 
            	    	    
            	    pushFollow(FOLLOW_rulePackageDeclaration_in_ruleProgram163);
            	    lv_packages_2_0=rulePackageDeclaration();
            	    _fsp--;


            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getProgramRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode.getParent(), current);
            	    	        }
            	    	        try {
            	    	       		add(
            	    	       			current, 
            	    	       			"packages",
            	    	        		lv_packages_2_0, 
            	    	        		"PackageDeclaration", 
            	    	        		currentNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	        currentNode = currentNode.getParent();
            	    	    

            	    }


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:144:3: ( RULE_NL )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==RULE_NL) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:144:4: RULE_NL
            	    {
            	    match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleProgram174); 
            	     
            	        createLeafNode(grammarAccess.getProgramAccess().getNLTerminalRuleCall_3(), null); 
            	        

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleProgram


    // $ANTLR start entryRulePackageDeclaration
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:156:1: entryRulePackageDeclaration returns [EObject current=null] : iv_rulePackageDeclaration= rulePackageDeclaration EOF ;
    public final EObject entryRulePackageDeclaration() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePackageDeclaration = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:157:2: (iv_rulePackageDeclaration= rulePackageDeclaration EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:158:2: iv_rulePackageDeclaration= rulePackageDeclaration EOF
            {
             currentNode = createCompositeNode(grammarAccess.getPackageDeclarationRule(), currentNode); 
            pushFollow(FOLLOW_rulePackageDeclaration_in_entryRulePackageDeclaration211);
            iv_rulePackageDeclaration=rulePackageDeclaration();
            _fsp--;

             current =iv_rulePackageDeclaration; 
            match(input,EOF,FOLLOW_EOF_in_entryRulePackageDeclaration221); 

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
    // $ANTLR end entryRulePackageDeclaration


    // $ANTLR start rulePackageDeclaration
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:165:1: rulePackageDeclaration returns [EObject current=null] : ( 'package' ( (lv_name_1_0= ruleQualifiedName ) ) '{' ( ( RULE_NL )* ( (lv_imports_4_0= ruleImport ) ) )* ( RULE_NL )* ( ( (lv_elements_6_0= ruleEntity ) ) ( RULE_NL )* )* '}' ( RULE_NL )* ) ;
    public final EObject rulePackageDeclaration() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_name_1_0 = null;

        EObject lv_imports_4_0 = null;

        EObject lv_elements_6_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:170:6: ( ( 'package' ( (lv_name_1_0= ruleQualifiedName ) ) '{' ( ( RULE_NL )* ( (lv_imports_4_0= ruleImport ) ) )* ( RULE_NL )* ( ( (lv_elements_6_0= ruleEntity ) ) ( RULE_NL )* )* '}' ( RULE_NL )* ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:171:1: ( 'package' ( (lv_name_1_0= ruleQualifiedName ) ) '{' ( ( RULE_NL )* ( (lv_imports_4_0= ruleImport ) ) )* ( RULE_NL )* ( ( (lv_elements_6_0= ruleEntity ) ) ( RULE_NL )* )* '}' ( RULE_NL )* )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:171:1: ( 'package' ( (lv_name_1_0= ruleQualifiedName ) ) '{' ( ( RULE_NL )* ( (lv_imports_4_0= ruleImport ) ) )* ( RULE_NL )* ( ( (lv_elements_6_0= ruleEntity ) ) ( RULE_NL )* )* '}' ( RULE_NL )* )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:171:3: 'package' ( (lv_name_1_0= ruleQualifiedName ) ) '{' ( ( RULE_NL )* ( (lv_imports_4_0= ruleImport ) ) )* ( RULE_NL )* ( ( (lv_elements_6_0= ruleEntity ) ) ( RULE_NL )* )* '}' ( RULE_NL )*
            {
            match(input,14,FOLLOW_14_in_rulePackageDeclaration256); 

                    createLeafNode(grammarAccess.getPackageDeclarationAccess().getPackageKeyword_0(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:175:1: ( (lv_name_1_0= ruleQualifiedName ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:176:1: (lv_name_1_0= ruleQualifiedName )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:176:1: (lv_name_1_0= ruleQualifiedName )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:177:3: lv_name_1_0= ruleQualifiedName
            {
             
            	        currentNode=createCompositeNode(grammarAccess.getPackageDeclarationAccess().getNameQualifiedNameParserRuleCall_1_0(), currentNode); 
            	    
            pushFollow(FOLLOW_ruleQualifiedName_in_rulePackageDeclaration277);
            lv_name_1_0=ruleQualifiedName();
            _fsp--;


            	        if (current==null) {
            	            current = factory.create(grammarAccess.getPackageDeclarationRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode.getParent(), current);
            	        }
            	        try {
            	       		set(
            	       			current, 
            	       			"name",
            	        		lv_name_1_0, 
            	        		"QualifiedName", 
            	        		currentNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	        currentNode = currentNode.getParent();
            	    

            }


            }

            match(input,15,FOLLOW_15_in_rulePackageDeclaration287); 

                    createLeafNode(grammarAccess.getPackageDeclarationAccess().getLeftCurlyBracketKeyword_2(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:203:1: ( ( RULE_NL )* ( (lv_imports_4_0= ruleImport ) ) )*
            loop5:
            do {
                int alt5=2;
                alt5 = dfa5.predict(input);
                switch (alt5) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:203:2: ( RULE_NL )* ( (lv_imports_4_0= ruleImport ) )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:203:2: ( RULE_NL )*
            	    loop4:
            	    do {
            	        int alt4=2;
            	        int LA4_0 = input.LA(1);

            	        if ( (LA4_0==RULE_NL) ) {
            	            alt4=1;
            	        }


            	        switch (alt4) {
            	    	case 1 :
            	    	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:203:3: RULE_NL
            	    	    {
            	    	    match(input,RULE_NL,FOLLOW_RULE_NL_in_rulePackageDeclaration298); 
            	    	     
            	    	        createLeafNode(grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_3_0(), null); 
            	    	        

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop4;
            	        }
            	    } while (true);

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:207:3: ( (lv_imports_4_0= ruleImport ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:208:1: (lv_imports_4_0= ruleImport )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:208:1: (lv_imports_4_0= ruleImport )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:209:3: lv_imports_4_0= ruleImport
            	    {
            	     
            	    	        currentNode=createCompositeNode(grammarAccess.getPackageDeclarationAccess().getImportsImportParserRuleCall_3_1_0(), currentNode); 
            	    	    
            	    pushFollow(FOLLOW_ruleImport_in_rulePackageDeclaration320);
            	    lv_imports_4_0=ruleImport();
            	    _fsp--;


            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getPackageDeclarationRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode.getParent(), current);
            	    	        }
            	    	        try {
            	    	       		add(
            	    	       			current, 
            	    	       			"imports",
            	    	        		lv_imports_4_0, 
            	    	        		"Import", 
            	    	        		currentNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	        currentNode = currentNode.getParent();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:231:4: ( RULE_NL )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==RULE_NL) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:231:5: RULE_NL
            	    {
            	    match(input,RULE_NL,FOLLOW_RULE_NL_in_rulePackageDeclaration332); 
            	     
            	        createLeafNode(grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_4(), null); 
            	        

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:235:3: ( ( (lv_elements_6_0= ruleEntity ) ) ( RULE_NL )* )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==20||(LA8_0>=23 && LA8_0<=25)||LA8_0==27) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:235:4: ( (lv_elements_6_0= ruleEntity ) ) ( RULE_NL )*
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:235:4: ( (lv_elements_6_0= ruleEntity ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:236:1: (lv_elements_6_0= ruleEntity )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:236:1: (lv_elements_6_0= ruleEntity )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:237:3: lv_elements_6_0= ruleEntity
            	    {
            	     
            	    	        currentNode=createCompositeNode(grammarAccess.getPackageDeclarationAccess().getElementsEntityParserRuleCall_5_0_0(), currentNode); 
            	    	    
            	    pushFollow(FOLLOW_ruleEntity_in_rulePackageDeclaration355);
            	    lv_elements_6_0=ruleEntity();
            	    _fsp--;


            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getPackageDeclarationRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode.getParent(), current);
            	    	        }
            	    	        try {
            	    	       		add(
            	    	       			current, 
            	    	       			"elements",
            	    	        		lv_elements_6_0, 
            	    	        		"Entity", 
            	    	        		currentNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	        currentNode = currentNode.getParent();
            	    	    

            	    }


            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:259:2: ( RULE_NL )*
            	    loop7:
            	    do {
            	        int alt7=2;
            	        int LA7_0 = input.LA(1);

            	        if ( (LA7_0==RULE_NL) ) {
            	            alt7=1;
            	        }


            	        switch (alt7) {
            	    	case 1 :
            	    	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:259:3: RULE_NL
            	    	    {
            	    	    match(input,RULE_NL,FOLLOW_RULE_NL_in_rulePackageDeclaration365); 
            	    	     
            	    	        createLeafNode(grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_5_1(), null); 
            	    	        

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

            match(input,16,FOLLOW_16_in_rulePackageDeclaration378); 

                    createLeafNode(grammarAccess.getPackageDeclarationAccess().getRightCurlyBracketKeyword_6(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:267:1: ( RULE_NL )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==RULE_NL) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:267:2: RULE_NL
            	    {
            	    match(input,RULE_NL,FOLLOW_RULE_NL_in_rulePackageDeclaration388); 
            	     
            	        createLeafNode(grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_7(), null); 
            	        

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);


            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end rulePackageDeclaration


    // $ANTLR start entryRuleImport
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:279:1: entryRuleImport returns [EObject current=null] : iv_ruleImport= ruleImport EOF ;
    public final EObject entryRuleImport() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleImport = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:280:2: (iv_ruleImport= ruleImport EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:281:2: iv_ruleImport= ruleImport EOF
            {
             currentNode = createCompositeNode(grammarAccess.getImportRule(), currentNode); 
            pushFollow(FOLLOW_ruleImport_in_entryRuleImport425);
            iv_ruleImport=ruleImport();
            _fsp--;

             current =iv_ruleImport; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleImport435); 

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
    // $ANTLR end entryRuleImport


    // $ANTLR start ruleImport
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:288:1: ruleImport returns [EObject current=null] : ( 'import' ( (lv_importedNamespace_1_0= ruleQualifiedNameWithWildCard ) ) RULE_NL ) ;
    public final EObject ruleImport() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_importedNamespace_1_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:293:6: ( ( 'import' ( (lv_importedNamespace_1_0= ruleQualifiedNameWithWildCard ) ) RULE_NL ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:294:1: ( 'import' ( (lv_importedNamespace_1_0= ruleQualifiedNameWithWildCard ) ) RULE_NL )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:294:1: ( 'import' ( (lv_importedNamespace_1_0= ruleQualifiedNameWithWildCard ) ) RULE_NL )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:294:3: 'import' ( (lv_importedNamespace_1_0= ruleQualifiedNameWithWildCard ) ) RULE_NL
            {
            match(input,17,FOLLOW_17_in_ruleImport470); 

                    createLeafNode(grammarAccess.getImportAccess().getImportKeyword_0(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:298:1: ( (lv_importedNamespace_1_0= ruleQualifiedNameWithWildCard ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:299:1: (lv_importedNamespace_1_0= ruleQualifiedNameWithWildCard )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:299:1: (lv_importedNamespace_1_0= ruleQualifiedNameWithWildCard )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:300:3: lv_importedNamespace_1_0= ruleQualifiedNameWithWildCard
            {
             
            	        currentNode=createCompositeNode(grammarAccess.getImportAccess().getImportedNamespaceQualifiedNameWithWildCardParserRuleCall_1_0(), currentNode); 
            	    
            pushFollow(FOLLOW_ruleQualifiedNameWithWildCard_in_ruleImport491);
            lv_importedNamespace_1_0=ruleQualifiedNameWithWildCard();
            _fsp--;


            	        if (current==null) {
            	            current = factory.create(grammarAccess.getImportRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode.getParent(), current);
            	        }
            	        try {
            	       		set(
            	       			current, 
            	       			"importedNamespace",
            	        		lv_importedNamespace_1_0, 
            	        		"QualifiedNameWithWildCard", 
            	        		currentNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	        currentNode = currentNode.getParent();
            	    

            }


            }

            match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleImport500); 
             
                createLeafNode(grammarAccess.getImportAccess().getNLTerminalRuleCall_2(), null); 
                

            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleImport


    // $ANTLR start entryRuleQualifiedName
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:334:1: entryRuleQualifiedName returns [String current=null] : iv_ruleQualifiedName= ruleQualifiedName EOF ;
    public final String entryRuleQualifiedName() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleQualifiedName = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:335:2: (iv_ruleQualifiedName= ruleQualifiedName EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:336:2: iv_ruleQualifiedName= ruleQualifiedName EOF
            {
             currentNode = createCompositeNode(grammarAccess.getQualifiedNameRule(), currentNode); 
            pushFollow(FOLLOW_ruleQualifiedName_in_entryRuleQualifiedName536);
            iv_ruleQualifiedName=ruleQualifiedName();
            _fsp--;

             current =iv_ruleQualifiedName.getText(); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleQualifiedName547); 

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
    // $ANTLR end entryRuleQualifiedName


    // $ANTLR start ruleQualifiedName
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:343:1: ruleQualifiedName returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_ID_0= RULE_ID (kw= '.' this_ID_2= RULE_ID )* ) ;
    public final AntlrDatatypeRuleToken ruleQualifiedName() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_ID_0=null;
        Token kw=null;
        Token this_ID_2=null;

         setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:348:6: ( (this_ID_0= RULE_ID (kw= '.' this_ID_2= RULE_ID )* ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:349:1: (this_ID_0= RULE_ID (kw= '.' this_ID_2= RULE_ID )* )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:349:1: (this_ID_0= RULE_ID (kw= '.' this_ID_2= RULE_ID )* )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:349:6: this_ID_0= RULE_ID (kw= '.' this_ID_2= RULE_ID )*
            {
            this_ID_0=(Token)input.LT(1);
            match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleQualifiedName587); 

            		current.merge(this_ID_0);
                
             
                createLeafNode(grammarAccess.getQualifiedNameAccess().getIDTerminalRuleCall_0(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:356:1: (kw= '.' this_ID_2= RULE_ID )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==18) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:357:2: kw= '.' this_ID_2= RULE_ID
            	    {
            	    kw=(Token)input.LT(1);
            	    match(input,18,FOLLOW_18_in_ruleQualifiedName606); 

            	            current.merge(kw);
            	            createLeafNode(grammarAccess.getQualifiedNameAccess().getFullStopKeyword_1_0(), null); 
            	        
            	    this_ID_2=(Token)input.LT(1);
            	    match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleQualifiedName621); 

            	    		current.merge(this_ID_2);
            	        
            	     
            	        createLeafNode(grammarAccess.getQualifiedNameAccess().getIDTerminalRuleCall_1_1(), null); 
            	        

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);


            }


            }

             resetLookahead(); 
            	    lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleQualifiedName


    // $ANTLR start entryRuleQualifiedNameWithWildCard
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:377:1: entryRuleQualifiedNameWithWildCard returns [String current=null] : iv_ruleQualifiedNameWithWildCard= ruleQualifiedNameWithWildCard EOF ;
    public final String entryRuleQualifiedNameWithWildCard() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleQualifiedNameWithWildCard = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:378:2: (iv_ruleQualifiedNameWithWildCard= ruleQualifiedNameWithWildCard EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:379:2: iv_ruleQualifiedNameWithWildCard= ruleQualifiedNameWithWildCard EOF
            {
             currentNode = createCompositeNode(grammarAccess.getQualifiedNameWithWildCardRule(), currentNode); 
            pushFollow(FOLLOW_ruleQualifiedNameWithWildCard_in_entryRuleQualifiedNameWithWildCard669);
            iv_ruleQualifiedNameWithWildCard=ruleQualifiedNameWithWildCard();
            _fsp--;

             current =iv_ruleQualifiedNameWithWildCard.getText(); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleQualifiedNameWithWildCard680); 

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
    // $ANTLR end entryRuleQualifiedNameWithWildCard


    // $ANTLR start ruleQualifiedNameWithWildCard
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:386:1: ruleQualifiedNameWithWildCard returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_QualifiedName_0= ruleQualifiedName (kw= '.*' )? ) ;
    public final AntlrDatatypeRuleToken ruleQualifiedNameWithWildCard() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;
        AntlrDatatypeRuleToken this_QualifiedName_0 = null;


         setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:391:6: ( (this_QualifiedName_0= ruleQualifiedName (kw= '.*' )? ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:392:1: (this_QualifiedName_0= ruleQualifiedName (kw= '.*' )? )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:392:1: (this_QualifiedName_0= ruleQualifiedName (kw= '.*' )? )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:393:5: this_QualifiedName_0= ruleQualifiedName (kw= '.*' )?
            {
             
                    currentNode=createCompositeNode(grammarAccess.getQualifiedNameWithWildCardAccess().getQualifiedNameParserRuleCall_0(), currentNode); 
                
            pushFollow(FOLLOW_ruleQualifiedName_in_ruleQualifiedNameWithWildCard727);
            this_QualifiedName_0=ruleQualifiedName();
            _fsp--;


            		current.merge(this_QualifiedName_0);
                
             
                    currentNode = currentNode.getParent();
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:403:1: (kw= '.*' )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==19) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:404:2: kw= '.*'
                    {
                    kw=(Token)input.LT(1);
                    match(input,19,FOLLOW_19_in_ruleQualifiedNameWithWildCard746); 

                            current.merge(kw);
                            createLeafNode(grammarAccess.getQualifiedNameWithWildCardAccess().getFullStopAsteriskKeyword_1(), null); 
                        

                    }
                    break;

            }


            }


            }

             resetLookahead(); 
            	    lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleQualifiedNameWithWildCard


    // $ANTLR start entryRuleEntity
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:417:1: entryRuleEntity returns [EObject current=null] : iv_ruleEntity= ruleEntity EOF ;
    public final EObject entryRuleEntity() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleEntity = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:418:2: (iv_ruleEntity= ruleEntity EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:419:2: iv_ruleEntity= ruleEntity EOF
            {
             currentNode = createCompositeNode(grammarAccess.getEntityRule(), currentNode); 
            pushFollow(FOLLOW_ruleEntity_in_entryRuleEntity788);
            iv_ruleEntity=ruleEntity();
            _fsp--;

             current =iv_ruleEntity; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleEntity798); 

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
    // $ANTLR end entryRuleEntity


    // $ANTLR start ruleEntity
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:426:1: ruleEntity returns [EObject current=null] : (this_ClassDef_0= ruleClassDef | this_NativeType_1= ruleNativeType | this_FuncDef_2= ruleFuncDef | this_VarDef_3= ruleVarDef ) ;
    public final EObject ruleEntity() throws RecognitionException {
        EObject current = null;

        EObject this_ClassDef_0 = null;

        EObject this_NativeType_1 = null;

        EObject this_FuncDef_2 = null;

        EObject this_VarDef_3 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:431:6: ( (this_ClassDef_0= ruleClassDef | this_NativeType_1= ruleNativeType | this_FuncDef_2= ruleFuncDef | this_VarDef_3= ruleVarDef ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:432:1: (this_ClassDef_0= ruleClassDef | this_NativeType_1= ruleNativeType | this_FuncDef_2= ruleFuncDef | this_VarDef_3= ruleVarDef )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:432:1: (this_ClassDef_0= ruleClassDef | this_NativeType_1= ruleNativeType | this_FuncDef_2= ruleFuncDef | this_VarDef_3= ruleVarDef )
            int alt12=4;
            switch ( input.LA(1) ) {
            case 23:
                {
                alt12=1;
                }
                break;
            case 20:
                {
                alt12=2;
                }
                break;
            case 27:
                {
                alt12=3;
                }
                break;
            case 24:
            case 25:
                {
                alt12=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("432:1: (this_ClassDef_0= ruleClassDef | this_NativeType_1= ruleNativeType | this_FuncDef_2= ruleFuncDef | this_VarDef_3= ruleVarDef )", 12, 0, input);

                throw nvae;
            }

            switch (alt12) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:433:5: this_ClassDef_0= ruleClassDef
                    {
                     
                            currentNode=createCompositeNode(grammarAccess.getEntityAccess().getClassDefParserRuleCall_0(), currentNode); 
                        
                    pushFollow(FOLLOW_ruleClassDef_in_ruleEntity845);
                    this_ClassDef_0=ruleClassDef();
                    _fsp--;

                     
                            current = this_ClassDef_0; 
                            currentNode = currentNode.getParent();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:443:5: this_NativeType_1= ruleNativeType
                    {
                     
                            currentNode=createCompositeNode(grammarAccess.getEntityAccess().getNativeTypeParserRuleCall_1(), currentNode); 
                        
                    pushFollow(FOLLOW_ruleNativeType_in_ruleEntity872);
                    this_NativeType_1=ruleNativeType();
                    _fsp--;

                     
                            current = this_NativeType_1; 
                            currentNode = currentNode.getParent();
                        

                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:453:5: this_FuncDef_2= ruleFuncDef
                    {
                     
                            currentNode=createCompositeNode(grammarAccess.getEntityAccess().getFuncDefParserRuleCall_2(), currentNode); 
                        
                    pushFollow(FOLLOW_ruleFuncDef_in_ruleEntity899);
                    this_FuncDef_2=ruleFuncDef();
                    _fsp--;

                     
                            current = this_FuncDef_2; 
                            currentNode = currentNode.getParent();
                        

                    }
                    break;
                case 4 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:463:5: this_VarDef_3= ruleVarDef
                    {
                     
                            currentNode=createCompositeNode(grammarAccess.getEntityAccess().getVarDefParserRuleCall_3(), currentNode); 
                        
                    pushFollow(FOLLOW_ruleVarDef_in_ruleEntity926);
                    this_VarDef_3=ruleVarDef();
                    _fsp--;

                     
                            current = this_VarDef_3; 
                            currentNode = currentNode.getParent();
                        

                    }
                    break;

            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleEntity


    // $ANTLR start entryRuleNativeType
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:479:1: entryRuleNativeType returns [EObject current=null] : iv_ruleNativeType= ruleNativeType EOF ;
    public final EObject entryRuleNativeType() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleNativeType = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:480:2: (iv_ruleNativeType= ruleNativeType EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:481:2: iv_ruleNativeType= ruleNativeType EOF
            {
             currentNode = createCompositeNode(grammarAccess.getNativeTypeRule(), currentNode); 
            pushFollow(FOLLOW_ruleNativeType_in_entryRuleNativeType961);
            iv_ruleNativeType=ruleNativeType();
            _fsp--;

             current =iv_ruleNativeType; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleNativeType971); 

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
    // $ANTLR end entryRuleNativeType


    // $ANTLR start ruleNativeType
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:488:1: ruleNativeType returns [EObject current=null] : ( () 'native' 'type' ( (lv_name_3_0= RULE_ID ) ) '=' ( (lv_origName_5_0= RULE_ID ) ) RULE_NL ) ;
    public final EObject ruleNativeType() throws RecognitionException {
        EObject current = null;

        Token lv_name_3_0=null;
        Token lv_origName_5_0=null;

         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:493:6: ( ( () 'native' 'type' ( (lv_name_3_0= RULE_ID ) ) '=' ( (lv_origName_5_0= RULE_ID ) ) RULE_NL ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:494:1: ( () 'native' 'type' ( (lv_name_3_0= RULE_ID ) ) '=' ( (lv_origName_5_0= RULE_ID ) ) RULE_NL )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:494:1: ( () 'native' 'type' ( (lv_name_3_0= RULE_ID ) ) '=' ( (lv_origName_5_0= RULE_ID ) ) RULE_NL )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:494:2: () 'native' 'type' ( (lv_name_3_0= RULE_ID ) ) '=' ( (lv_origName_5_0= RULE_ID ) ) RULE_NL
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:494:2: ()
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:495:5: 
            {
             
                    temp=factory.create(grammarAccess.getNativeTypeAccess().getNativeTypeAction_0().getType().getClassifier());
                    current = temp; 
                    temp = null;
                    CompositeNode newNode = createCompositeNode(grammarAccess.getNativeTypeAccess().getNativeTypeAction_0(), currentNode.getParent());
                newNode.getChildren().add(currentNode);
                moveLookaheadInfo(currentNode, newNode);
                currentNode = newNode; 
                    associateNodeWithAstElement(currentNode, current); 
                

            }

            match(input,20,FOLLOW_20_in_ruleNativeType1015); 

                    createLeafNode(grammarAccess.getNativeTypeAccess().getNativeKeyword_1(), null); 
                
            match(input,21,FOLLOW_21_in_ruleNativeType1025); 

                    createLeafNode(grammarAccess.getNativeTypeAccess().getTypeKeyword_2(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:513:1: ( (lv_name_3_0= RULE_ID ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:514:1: (lv_name_3_0= RULE_ID )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:514:1: (lv_name_3_0= RULE_ID )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:515:3: lv_name_3_0= RULE_ID
            {
            lv_name_3_0=(Token)input.LT(1);
            match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleNativeType1042); 

            			createLeafNode(grammarAccess.getNativeTypeAccess().getNameIDTerminalRuleCall_3_0(), "name"); 
            		

            	        if (current==null) {
            	            current = factory.create(grammarAccess.getNativeTypeRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode, current);
            	        }
            	        try {
            	       		set(
            	       			current, 
            	       			"name",
            	        		lv_name_3_0, 
            	        		"ID", 
            	        		lastConsumedNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	    

            }


            }

            match(input,22,FOLLOW_22_in_ruleNativeType1057); 

                    createLeafNode(grammarAccess.getNativeTypeAccess().getEqualsSignKeyword_4(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:541:1: ( (lv_origName_5_0= RULE_ID ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:542:1: (lv_origName_5_0= RULE_ID )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:542:1: (lv_origName_5_0= RULE_ID )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:543:3: lv_origName_5_0= RULE_ID
            {
            lv_origName_5_0=(Token)input.LT(1);
            match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleNativeType1074); 

            			createLeafNode(grammarAccess.getNativeTypeAccess().getOrigNameIDTerminalRuleCall_5_0(), "origName"); 
            		

            	        if (current==null) {
            	            current = factory.create(grammarAccess.getNativeTypeRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode, current);
            	        }
            	        try {
            	       		set(
            	       			current, 
            	       			"origName",
            	        		lv_origName_5_0, 
            	        		"ID", 
            	        		lastConsumedNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	    

            }


            }

            match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleNativeType1088); 
             
                createLeafNode(grammarAccess.getNativeTypeAccess().getNLTerminalRuleCall_6(), null); 
                

            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleNativeType


    // $ANTLR start entryRuleClassDef
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:577:1: entryRuleClassDef returns [EObject current=null] : iv_ruleClassDef= ruleClassDef EOF ;
    public final EObject entryRuleClassDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleClassDef = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:578:2: (iv_ruleClassDef= ruleClassDef EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:579:2: iv_ruleClassDef= ruleClassDef EOF
            {
             currentNode = createCompositeNode(grammarAccess.getClassDefRule(), currentNode); 
            pushFollow(FOLLOW_ruleClassDef_in_entryRuleClassDef1123);
            iv_ruleClassDef=ruleClassDef();
            _fsp--;

             current =iv_ruleClassDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleClassDef1133); 

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
    // $ANTLR end entryRuleClassDef


    // $ANTLR start ruleClassDef
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:586:1: ruleClassDef returns [EObject current=null] : ( () 'class' ( (lv_name_2_0= RULE_ID ) ) '{' ( (lv_members_4_0= ruleClassMember ) )* ( RULE_NL )* '}' RULE_NL ) ;
    public final EObject ruleClassDef() throws RecognitionException {
        EObject current = null;

        Token lv_name_2_0=null;
        EObject lv_members_4_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:591:6: ( ( () 'class' ( (lv_name_2_0= RULE_ID ) ) '{' ( (lv_members_4_0= ruleClassMember ) )* ( RULE_NL )* '}' RULE_NL ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:592:1: ( () 'class' ( (lv_name_2_0= RULE_ID ) ) '{' ( (lv_members_4_0= ruleClassMember ) )* ( RULE_NL )* '}' RULE_NL )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:592:1: ( () 'class' ( (lv_name_2_0= RULE_ID ) ) '{' ( (lv_members_4_0= ruleClassMember ) )* ( RULE_NL )* '}' RULE_NL )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:592:2: () 'class' ( (lv_name_2_0= RULE_ID ) ) '{' ( (lv_members_4_0= ruleClassMember ) )* ( RULE_NL )* '}' RULE_NL
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:592:2: ()
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:593:5: 
            {
             
                    temp=factory.create(grammarAccess.getClassDefAccess().getClassDefAction_0().getType().getClassifier());
                    current = temp; 
                    temp = null;
                    CompositeNode newNode = createCompositeNode(grammarAccess.getClassDefAccess().getClassDefAction_0(), currentNode.getParent());
                newNode.getChildren().add(currentNode);
                moveLookaheadInfo(currentNode, newNode);
                currentNode = newNode; 
                    associateNodeWithAstElement(currentNode, current); 
                

            }

            match(input,23,FOLLOW_23_in_ruleClassDef1177); 

                    createLeafNode(grammarAccess.getClassDefAccess().getClassKeyword_1(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:607:1: ( (lv_name_2_0= RULE_ID ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:608:1: (lv_name_2_0= RULE_ID )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:608:1: (lv_name_2_0= RULE_ID )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:609:3: lv_name_2_0= RULE_ID
            {
            lv_name_2_0=(Token)input.LT(1);
            match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleClassDef1194); 

            			createLeafNode(grammarAccess.getClassDefAccess().getNameIDTerminalRuleCall_2_0(), "name"); 
            		

            	        if (current==null) {
            	            current = factory.create(grammarAccess.getClassDefRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode, current);
            	        }
            	        try {
            	       		set(
            	       			current, 
            	       			"name",
            	        		lv_name_2_0, 
            	        		"ID", 
            	        		lastConsumedNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	    

            }


            }

            match(input,15,FOLLOW_15_in_ruleClassDef1209); 

                    createLeafNode(grammarAccess.getClassDefAccess().getLeftCurlyBracketKeyword_3(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:635:1: ( (lv_members_4_0= ruleClassMember ) )*
            loop13:
            do {
                int alt13=2;
                alt13 = dfa13.predict(input);
                switch (alt13) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:636:1: (lv_members_4_0= ruleClassMember )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:636:1: (lv_members_4_0= ruleClassMember )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:637:3: lv_members_4_0= ruleClassMember
            	    {
            	     
            	    	        currentNode=createCompositeNode(grammarAccess.getClassDefAccess().getMembersClassMemberParserRuleCall_4_0(), currentNode); 
            	    	    
            	    pushFollow(FOLLOW_ruleClassMember_in_ruleClassDef1230);
            	    lv_members_4_0=ruleClassMember();
            	    _fsp--;


            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getClassDefRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode.getParent(), current);
            	    	        }
            	    	        try {
            	    	       		add(
            	    	       			current, 
            	    	       			"members",
            	    	        		lv_members_4_0, 
            	    	        		"ClassMember", 
            	    	        		currentNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	        currentNode = currentNode.getParent();
            	    	    

            	    }


            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:659:3: ( RULE_NL )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==RULE_NL) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:659:4: RULE_NL
            	    {
            	    match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleClassDef1241); 
            	     
            	        createLeafNode(grammarAccess.getClassDefAccess().getNLTerminalRuleCall_5(), null); 
            	        

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);

            match(input,16,FOLLOW_16_in_ruleClassDef1252); 

                    createLeafNode(grammarAccess.getClassDefAccess().getRightCurlyBracketKeyword_6(), null); 
                
            match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleClassDef1261); 
             
                createLeafNode(grammarAccess.getClassDefAccess().getNLTerminalRuleCall_7(), null); 
                

            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleClassDef


    // $ANTLR start entryRuleClassMember
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:679:1: entryRuleClassMember returns [EObject current=null] : iv_ruleClassMember= ruleClassMember EOF ;
    public final EObject entryRuleClassMember() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleClassMember = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:680:2: (iv_ruleClassMember= ruleClassMember EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:681:2: iv_ruleClassMember= ruleClassMember EOF
            {
             currentNode = createCompositeNode(grammarAccess.getClassMemberRule(), currentNode); 
            pushFollow(FOLLOW_ruleClassMember_in_entryRuleClassMember1296);
            iv_ruleClassMember=ruleClassMember();
            _fsp--;

             current =iv_ruleClassMember; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleClassMember1306); 

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
    // $ANTLR end entryRuleClassMember


    // $ANTLR start ruleClassMember
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:688:1: ruleClassMember returns [EObject current=null] : ( ( RULE_NL )* (this_VarDef_1= ruleVarDef | this_FuncDef_2= ruleFuncDef ) ) ;
    public final EObject ruleClassMember() throws RecognitionException {
        EObject current = null;

        EObject this_VarDef_1 = null;

        EObject this_FuncDef_2 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:693:6: ( ( ( RULE_NL )* (this_VarDef_1= ruleVarDef | this_FuncDef_2= ruleFuncDef ) ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:694:1: ( ( RULE_NL )* (this_VarDef_1= ruleVarDef | this_FuncDef_2= ruleFuncDef ) )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:694:1: ( ( RULE_NL )* (this_VarDef_1= ruleVarDef | this_FuncDef_2= ruleFuncDef ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:694:2: ( RULE_NL )* (this_VarDef_1= ruleVarDef | this_FuncDef_2= ruleFuncDef )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:694:2: ( RULE_NL )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==RULE_NL) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:694:3: RULE_NL
            	    {
            	    match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleClassMember1341); 
            	     
            	        createLeafNode(grammarAccess.getClassMemberAccess().getNLTerminalRuleCall_0(), null); 
            	        

            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:698:3: (this_VarDef_1= ruleVarDef | this_FuncDef_2= ruleFuncDef )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( ((LA16_0>=24 && LA16_0<=25)) ) {
                alt16=1;
            }
            else if ( (LA16_0==27) ) {
                alt16=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("698:3: (this_VarDef_1= ruleVarDef | this_FuncDef_2= ruleFuncDef )", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:699:5: this_VarDef_1= ruleVarDef
                    {
                     
                            currentNode=createCompositeNode(grammarAccess.getClassMemberAccess().getVarDefParserRuleCall_1_0(), currentNode); 
                        
                    pushFollow(FOLLOW_ruleVarDef_in_ruleClassMember1365);
                    this_VarDef_1=ruleVarDef();
                    _fsp--;

                     
                            current = this_VarDef_1; 
                            currentNode = currentNode.getParent();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:709:5: this_FuncDef_2= ruleFuncDef
                    {
                     
                            currentNode=createCompositeNode(grammarAccess.getClassMemberAccess().getFuncDefParserRuleCall_1_1(), currentNode); 
                        
                    pushFollow(FOLLOW_ruleFuncDef_in_ruleClassMember1392);
                    this_FuncDef_2=ruleFuncDef();
                    _fsp--;

                     
                            current = this_FuncDef_2; 
                            currentNode = currentNode.getParent();
                        

                    }
                    break;

            }


            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleClassMember


    // $ANTLR start entryRuleVarDef
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:725:1: entryRuleVarDef returns [EObject current=null] : iv_ruleVarDef= ruleVarDef EOF ;
    public final EObject entryRuleVarDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleVarDef = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:726:2: (iv_ruleVarDef= ruleVarDef EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:727:2: iv_ruleVarDef= ruleVarDef EOF
            {
             currentNode = createCompositeNode(grammarAccess.getVarDefRule(), currentNode); 
            pushFollow(FOLLOW_ruleVarDef_in_entryRuleVarDef1428);
            iv_ruleVarDef=ruleVarDef();
            _fsp--;

             current =iv_ruleVarDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleVarDef1438); 

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
    // $ANTLR end entryRuleVarDef


    // $ANTLR start ruleVarDef
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:734:1: ruleVarDef returns [EObject current=null] : ( () ( 'var' | ( (lv_constant_2_0= 'val' ) ) ) ( (lv_name_3_0= RULE_ID ) ) ( ':' ( (lv_type_5_0= ruleTypeExpr ) ) )? ( '=' ( (lv_e_7_0= ruleExpr ) ) )? RULE_NL ) ;
    public final EObject ruleVarDef() throws RecognitionException {
        EObject current = null;

        Token lv_constant_2_0=null;
        Token lv_name_3_0=null;
        EObject lv_type_5_0 = null;

        EObject lv_e_7_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:739:6: ( ( () ( 'var' | ( (lv_constant_2_0= 'val' ) ) ) ( (lv_name_3_0= RULE_ID ) ) ( ':' ( (lv_type_5_0= ruleTypeExpr ) ) )? ( '=' ( (lv_e_7_0= ruleExpr ) ) )? RULE_NL ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:740:1: ( () ( 'var' | ( (lv_constant_2_0= 'val' ) ) ) ( (lv_name_3_0= RULE_ID ) ) ( ':' ( (lv_type_5_0= ruleTypeExpr ) ) )? ( '=' ( (lv_e_7_0= ruleExpr ) ) )? RULE_NL )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:740:1: ( () ( 'var' | ( (lv_constant_2_0= 'val' ) ) ) ( (lv_name_3_0= RULE_ID ) ) ( ':' ( (lv_type_5_0= ruleTypeExpr ) ) )? ( '=' ( (lv_e_7_0= ruleExpr ) ) )? RULE_NL )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:740:2: () ( 'var' | ( (lv_constant_2_0= 'val' ) ) ) ( (lv_name_3_0= RULE_ID ) ) ( ':' ( (lv_type_5_0= ruleTypeExpr ) ) )? ( '=' ( (lv_e_7_0= ruleExpr ) ) )? RULE_NL
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:740:2: ()
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:741:5: 
            {
             
                    temp=factory.create(grammarAccess.getVarDefAccess().getVarDefAction_0().getType().getClassifier());
                    current = temp; 
                    temp = null;
                    CompositeNode newNode = createCompositeNode(grammarAccess.getVarDefAccess().getVarDefAction_0(), currentNode.getParent());
                newNode.getChildren().add(currentNode);
                moveLookaheadInfo(currentNode, newNode);
                currentNode = newNode; 
                    associateNodeWithAstElement(currentNode, current); 
                

            }

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:751:2: ( 'var' | ( (lv_constant_2_0= 'val' ) ) )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==24) ) {
                alt17=1;
            }
            else if ( (LA17_0==25) ) {
                alt17=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("751:2: ( 'var' | ( (lv_constant_2_0= 'val' ) ) )", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:751:4: 'var'
                    {
                    match(input,24,FOLLOW_24_in_ruleVarDef1483); 

                            createLeafNode(grammarAccess.getVarDefAccess().getVarKeyword_1_0(), null); 
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:756:6: ( (lv_constant_2_0= 'val' ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:756:6: ( (lv_constant_2_0= 'val' ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:757:1: (lv_constant_2_0= 'val' )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:757:1: (lv_constant_2_0= 'val' )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:758:3: lv_constant_2_0= 'val'
                    {
                    lv_constant_2_0=(Token)input.LT(1);
                    match(input,25,FOLLOW_25_in_ruleVarDef1507); 

                            createLeafNode(grammarAccess.getVarDefAccess().getConstantValKeyword_1_1_0(), "constant"); 
                        

                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getVarDefRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode, current);
                    	        }
                    	        
                    	        try {
                    	       		set(current, "constant", true, "val", lastConsumedNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	    

                    }


                    }


                    }
                    break;

            }

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:777:3: ( (lv_name_3_0= RULE_ID ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:778:1: (lv_name_3_0= RULE_ID )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:778:1: (lv_name_3_0= RULE_ID )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:779:3: lv_name_3_0= RULE_ID
            {
            lv_name_3_0=(Token)input.LT(1);
            match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleVarDef1538); 

            			createLeafNode(grammarAccess.getVarDefAccess().getNameIDTerminalRuleCall_2_0(), "name"); 
            		

            	        if (current==null) {
            	            current = factory.create(grammarAccess.getVarDefRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode, current);
            	        }
            	        try {
            	       		set(
            	       			current, 
            	       			"name",
            	        		lv_name_3_0, 
            	        		"ID", 
            	        		lastConsumedNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	    

            }


            }

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:801:2: ( ':' ( (lv_type_5_0= ruleTypeExpr ) ) )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==26) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:801:4: ':' ( (lv_type_5_0= ruleTypeExpr ) )
                    {
                    match(input,26,FOLLOW_26_in_ruleVarDef1554); 

                            createLeafNode(grammarAccess.getVarDefAccess().getColonKeyword_3_0(), null); 
                        
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:805:1: ( (lv_type_5_0= ruleTypeExpr ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:806:1: (lv_type_5_0= ruleTypeExpr )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:806:1: (lv_type_5_0= ruleTypeExpr )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:807:3: lv_type_5_0= ruleTypeExpr
                    {
                     
                    	        currentNode=createCompositeNode(grammarAccess.getVarDefAccess().getTypeTypeExprParserRuleCall_3_1_0(), currentNode); 
                    	    
                    pushFollow(FOLLOW_ruleTypeExpr_in_ruleVarDef1575);
                    lv_type_5_0=ruleTypeExpr();
                    _fsp--;


                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getVarDefRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode.getParent(), current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"type",
                    	        		lv_type_5_0, 
                    	        		"TypeExpr", 
                    	        		currentNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	        currentNode = currentNode.getParent();
                    	    

                    }


                    }


                    }
                    break;

            }

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:829:4: ( '=' ( (lv_e_7_0= ruleExpr ) ) )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==22) ) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:829:6: '=' ( (lv_e_7_0= ruleExpr ) )
                    {
                    match(input,22,FOLLOW_22_in_ruleVarDef1588); 

                            createLeafNode(grammarAccess.getVarDefAccess().getEqualsSignKeyword_4_0(), null); 
                        
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:833:1: ( (lv_e_7_0= ruleExpr ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:834:1: (lv_e_7_0= ruleExpr )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:834:1: (lv_e_7_0= ruleExpr )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:835:3: lv_e_7_0= ruleExpr
                    {
                     
                    	        currentNode=createCompositeNode(grammarAccess.getVarDefAccess().getEExprParserRuleCall_4_1_0(), currentNode); 
                    	    
                    pushFollow(FOLLOW_ruleExpr_in_ruleVarDef1609);
                    lv_e_7_0=ruleExpr();
                    _fsp--;


                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getVarDefRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode.getParent(), current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"e",
                    	        		lv_e_7_0, 
                    	        		"Expr", 
                    	        		currentNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	        currentNode = currentNode.getParent();
                    	    

                    }


                    }


                    }
                    break;

            }

            match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleVarDef1620); 
             
                createLeafNode(grammarAccess.getVarDefAccess().getNLTerminalRuleCall_5(), null); 
                

            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleVarDef


    // $ANTLR start entryRuleTypeExpr
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:869:1: entryRuleTypeExpr returns [EObject current=null] : iv_ruleTypeExpr= ruleTypeExpr EOF ;
    public final EObject entryRuleTypeExpr() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTypeExpr = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:870:2: (iv_ruleTypeExpr= ruleTypeExpr EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:871:2: iv_ruleTypeExpr= ruleTypeExpr EOF
            {
             currentNode = createCompositeNode(grammarAccess.getTypeExprRule(), currentNode); 
            pushFollow(FOLLOW_ruleTypeExpr_in_entryRuleTypeExpr1655);
            iv_ruleTypeExpr=ruleTypeExpr();
            _fsp--;

             current =iv_ruleTypeExpr; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleTypeExpr1665); 

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
    // $ANTLR end entryRuleTypeExpr


    // $ANTLR start ruleTypeExpr
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:878:1: ruleTypeExpr returns [EObject current=null] : ( () ( (lv_name_1_0= RULE_ID ) ) ) ;
    public final EObject ruleTypeExpr() throws RecognitionException {
        EObject current = null;

        Token lv_name_1_0=null;

         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:883:6: ( ( () ( (lv_name_1_0= RULE_ID ) ) ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:884:1: ( () ( (lv_name_1_0= RULE_ID ) ) )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:884:1: ( () ( (lv_name_1_0= RULE_ID ) ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:884:2: () ( (lv_name_1_0= RULE_ID ) )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:884:2: ()
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:885:5: 
            {
             
                    temp=factory.create(grammarAccess.getTypeExprAccess().getTypeExprAction_0().getType().getClassifier());
                    current = temp; 
                    temp = null;
                    CompositeNode newNode = createCompositeNode(grammarAccess.getTypeExprAccess().getTypeExprAction_0(), currentNode.getParent());
                newNode.getChildren().add(currentNode);
                moveLookaheadInfo(currentNode, newNode);
                currentNode = newNode; 
                    associateNodeWithAstElement(currentNode, current); 
                

            }

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:895:2: ( (lv_name_1_0= RULE_ID ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:896:1: (lv_name_1_0= RULE_ID )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:896:1: (lv_name_1_0= RULE_ID )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:897:3: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)input.LT(1);
            match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleTypeExpr1716); 

            			createLeafNode(grammarAccess.getTypeExprAccess().getNameIDTerminalRuleCall_1_0(), "name"); 
            		

            	        if (current==null) {
            	            current = factory.create(grammarAccess.getTypeExprRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode, current);
            	        }
            	        try {
            	       		set(
            	       			current, 
            	       			"name",
            	        		lv_name_1_0, 
            	        		"ID", 
            	        		lastConsumedNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	    

            }


            }


            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleTypeExpr


    // $ANTLR start entryRuleFuncDef
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:927:1: entryRuleFuncDef returns [EObject current=null] : iv_ruleFuncDef= ruleFuncDef EOF ;
    public final EObject entryRuleFuncDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFuncDef = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:928:2: (iv_ruleFuncDef= ruleFuncDef EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:929:2: iv_ruleFuncDef= ruleFuncDef EOF
            {
             currentNode = createCompositeNode(grammarAccess.getFuncDefRule(), currentNode); 
            pushFollow(FOLLOW_ruleFuncDef_in_entryRuleFuncDef1757);
            iv_ruleFuncDef=ruleFuncDef();
            _fsp--;

             current =iv_ruleFuncDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleFuncDef1767); 

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
    // $ANTLR end entryRuleFuncDef


    // $ANTLR start ruleFuncDef
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:936:1: ruleFuncDef returns [EObject current=null] : ( () 'function' ( (lv_name_2_0= RULE_ID ) ) '(' ( ( (lv_parameters_4_0= ruleParameterDef ) ) ( ',' ( (lv_parameters_6_0= ruleParameterDef ) ) )* )? ')' ( ':' ( (lv_type_9_0= ruleTypeExpr ) ) )? '{' ( (lv_body_11_0= ruleStatements ) ) '}' ) ;
    public final EObject ruleFuncDef() throws RecognitionException {
        EObject current = null;

        Token lv_name_2_0=null;
        EObject lv_parameters_4_0 = null;

        EObject lv_parameters_6_0 = null;

        EObject lv_type_9_0 = null;

        EObject lv_body_11_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:941:6: ( ( () 'function' ( (lv_name_2_0= RULE_ID ) ) '(' ( ( (lv_parameters_4_0= ruleParameterDef ) ) ( ',' ( (lv_parameters_6_0= ruleParameterDef ) ) )* )? ')' ( ':' ( (lv_type_9_0= ruleTypeExpr ) ) )? '{' ( (lv_body_11_0= ruleStatements ) ) '}' ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:942:1: ( () 'function' ( (lv_name_2_0= RULE_ID ) ) '(' ( ( (lv_parameters_4_0= ruleParameterDef ) ) ( ',' ( (lv_parameters_6_0= ruleParameterDef ) ) )* )? ')' ( ':' ( (lv_type_9_0= ruleTypeExpr ) ) )? '{' ( (lv_body_11_0= ruleStatements ) ) '}' )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:942:1: ( () 'function' ( (lv_name_2_0= RULE_ID ) ) '(' ( ( (lv_parameters_4_0= ruleParameterDef ) ) ( ',' ( (lv_parameters_6_0= ruleParameterDef ) ) )* )? ')' ( ':' ( (lv_type_9_0= ruleTypeExpr ) ) )? '{' ( (lv_body_11_0= ruleStatements ) ) '}' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:942:2: () 'function' ( (lv_name_2_0= RULE_ID ) ) '(' ( ( (lv_parameters_4_0= ruleParameterDef ) ) ( ',' ( (lv_parameters_6_0= ruleParameterDef ) ) )* )? ')' ( ':' ( (lv_type_9_0= ruleTypeExpr ) ) )? '{' ( (lv_body_11_0= ruleStatements ) ) '}'
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:942:2: ()
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:943:5: 
            {
             
                    temp=factory.create(grammarAccess.getFuncDefAccess().getFuncDefAction_0().getType().getClassifier());
                    current = temp; 
                    temp = null;
                    CompositeNode newNode = createCompositeNode(grammarAccess.getFuncDefAccess().getFuncDefAction_0(), currentNode.getParent());
                newNode.getChildren().add(currentNode);
                moveLookaheadInfo(currentNode, newNode);
                currentNode = newNode; 
                    associateNodeWithAstElement(currentNode, current); 
                

            }

            match(input,27,FOLLOW_27_in_ruleFuncDef1811); 

                    createLeafNode(grammarAccess.getFuncDefAccess().getFunctionKeyword_1(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:957:1: ( (lv_name_2_0= RULE_ID ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:958:1: (lv_name_2_0= RULE_ID )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:958:1: (lv_name_2_0= RULE_ID )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:959:3: lv_name_2_0= RULE_ID
            {
            lv_name_2_0=(Token)input.LT(1);
            match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleFuncDef1828); 

            			createLeafNode(grammarAccess.getFuncDefAccess().getNameIDTerminalRuleCall_2_0(), "name"); 
            		

            	        if (current==null) {
            	            current = factory.create(grammarAccess.getFuncDefRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode, current);
            	        }
            	        try {
            	       		set(
            	       			current, 
            	       			"name",
            	        		lv_name_2_0, 
            	        		"ID", 
            	        		lastConsumedNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	    

            }


            }

            match(input,28,FOLLOW_28_in_ruleFuncDef1843); 

                    createLeafNode(grammarAccess.getFuncDefAccess().getLeftParenthesisKeyword_3(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:985:1: ( ( (lv_parameters_4_0= ruleParameterDef ) ) ( ',' ( (lv_parameters_6_0= ruleParameterDef ) ) )* )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==RULE_ID) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:985:2: ( (lv_parameters_4_0= ruleParameterDef ) ) ( ',' ( (lv_parameters_6_0= ruleParameterDef ) ) )*
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:985:2: ( (lv_parameters_4_0= ruleParameterDef ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:986:1: (lv_parameters_4_0= ruleParameterDef )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:986:1: (lv_parameters_4_0= ruleParameterDef )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:987:3: lv_parameters_4_0= ruleParameterDef
                    {
                     
                    	        currentNode=createCompositeNode(grammarAccess.getFuncDefAccess().getParametersParameterDefParserRuleCall_4_0_0(), currentNode); 
                    	    
                    pushFollow(FOLLOW_ruleParameterDef_in_ruleFuncDef1865);
                    lv_parameters_4_0=ruleParameterDef();
                    _fsp--;


                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getFuncDefRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode.getParent(), current);
                    	        }
                    	        try {
                    	       		add(
                    	       			current, 
                    	       			"parameters",
                    	        		lv_parameters_4_0, 
                    	        		"ParameterDef", 
                    	        		currentNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	        currentNode = currentNode.getParent();
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1009:2: ( ',' ( (lv_parameters_6_0= ruleParameterDef ) ) )*
                    loop20:
                    do {
                        int alt20=2;
                        int LA20_0 = input.LA(1);

                        if ( (LA20_0==29) ) {
                            alt20=1;
                        }


                        switch (alt20) {
                    	case 1 :
                    	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1009:4: ',' ( (lv_parameters_6_0= ruleParameterDef ) )
                    	    {
                    	    match(input,29,FOLLOW_29_in_ruleFuncDef1876); 

                    	            createLeafNode(grammarAccess.getFuncDefAccess().getCommaKeyword_4_1_0(), null); 
                    	        
                    	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1013:1: ( (lv_parameters_6_0= ruleParameterDef ) )
                    	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1014:1: (lv_parameters_6_0= ruleParameterDef )
                    	    {
                    	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1014:1: (lv_parameters_6_0= ruleParameterDef )
                    	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1015:3: lv_parameters_6_0= ruleParameterDef
                    	    {
                    	     
                    	    	        currentNode=createCompositeNode(grammarAccess.getFuncDefAccess().getParametersParameterDefParserRuleCall_4_1_1_0(), currentNode); 
                    	    	    
                    	    pushFollow(FOLLOW_ruleParameterDef_in_ruleFuncDef1897);
                    	    lv_parameters_6_0=ruleParameterDef();
                    	    _fsp--;


                    	    	        if (current==null) {
                    	    	            current = factory.create(grammarAccess.getFuncDefRule().getType().getClassifier());
                    	    	            associateNodeWithAstElement(currentNode.getParent(), current);
                    	    	        }
                    	    	        try {
                    	    	       		add(
                    	    	       			current, 
                    	    	       			"parameters",
                    	    	        		lv_parameters_6_0, 
                    	    	        		"ParameterDef", 
                    	    	        		currentNode);
                    	    	        } catch (ValueConverterException vce) {
                    	    				handleValueConverterException(vce);
                    	    	        }
                    	    	        currentNode = currentNode.getParent();
                    	    	    

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop20;
                        }
                    } while (true);


                    }
                    break;

            }

            match(input,30,FOLLOW_30_in_ruleFuncDef1911); 

                    createLeafNode(grammarAccess.getFuncDefAccess().getRightParenthesisKeyword_5(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1041:1: ( ':' ( (lv_type_9_0= ruleTypeExpr ) ) )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==26) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1041:3: ':' ( (lv_type_9_0= ruleTypeExpr ) )
                    {
                    match(input,26,FOLLOW_26_in_ruleFuncDef1922); 

                            createLeafNode(grammarAccess.getFuncDefAccess().getColonKeyword_6_0(), null); 
                        
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1045:1: ( (lv_type_9_0= ruleTypeExpr ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1046:1: (lv_type_9_0= ruleTypeExpr )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1046:1: (lv_type_9_0= ruleTypeExpr )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1047:3: lv_type_9_0= ruleTypeExpr
                    {
                     
                    	        currentNode=createCompositeNode(grammarAccess.getFuncDefAccess().getTypeTypeExprParserRuleCall_6_1_0(), currentNode); 
                    	    
                    pushFollow(FOLLOW_ruleTypeExpr_in_ruleFuncDef1943);
                    lv_type_9_0=ruleTypeExpr();
                    _fsp--;


                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getFuncDefRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode.getParent(), current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"type",
                    	        		lv_type_9_0, 
                    	        		"TypeExpr", 
                    	        		currentNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	        currentNode = currentNode.getParent();
                    	    

                    }


                    }


                    }
                    break;

            }

            match(input,15,FOLLOW_15_in_ruleFuncDef1955); 

                    createLeafNode(grammarAccess.getFuncDefAccess().getLeftCurlyBracketKeyword_7(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1073:1: ( (lv_body_11_0= ruleStatements ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1074:1: (lv_body_11_0= ruleStatements )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1074:1: (lv_body_11_0= ruleStatements )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1075:3: lv_body_11_0= ruleStatements
            {
             
            	        currentNode=createCompositeNode(grammarAccess.getFuncDefAccess().getBodyStatementsParserRuleCall_8_0(), currentNode); 
            	    
            pushFollow(FOLLOW_ruleStatements_in_ruleFuncDef1976);
            lv_body_11_0=ruleStatements();
            _fsp--;


            	        if (current==null) {
            	            current = factory.create(grammarAccess.getFuncDefRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode.getParent(), current);
            	        }
            	        try {
            	       		set(
            	       			current, 
            	       			"body",
            	        		lv_body_11_0, 
            	        		"Statements", 
            	        		currentNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	        currentNode = currentNode.getParent();
            	    

            }


            }

            match(input,16,FOLLOW_16_in_ruleFuncDef1986); 

                    createLeafNode(grammarAccess.getFuncDefAccess().getRightCurlyBracketKeyword_9(), null); 
                

            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleFuncDef


    // $ANTLR start entryRuleParameterDef
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1109:1: entryRuleParameterDef returns [EObject current=null] : iv_ruleParameterDef= ruleParameterDef EOF ;
    public final EObject entryRuleParameterDef() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleParameterDef = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1110:2: (iv_ruleParameterDef= ruleParameterDef EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1111:2: iv_ruleParameterDef= ruleParameterDef EOF
            {
             currentNode = createCompositeNode(grammarAccess.getParameterDefRule(), currentNode); 
            pushFollow(FOLLOW_ruleParameterDef_in_entryRuleParameterDef2022);
            iv_ruleParameterDef=ruleParameterDef();
            _fsp--;

             current =iv_ruleParameterDef; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleParameterDef2032); 

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
    // $ANTLR end entryRuleParameterDef


    // $ANTLR start ruleParameterDef
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1118:1: ruleParameterDef returns [EObject current=null] : ( () ( (lv_name_1_0= RULE_ID ) ) ':' ( (lv_type_3_0= ruleTypeExpr ) ) ) ;
    public final EObject ruleParameterDef() throws RecognitionException {
        EObject current = null;

        Token lv_name_1_0=null;
        EObject lv_type_3_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1123:6: ( ( () ( (lv_name_1_0= RULE_ID ) ) ':' ( (lv_type_3_0= ruleTypeExpr ) ) ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1124:1: ( () ( (lv_name_1_0= RULE_ID ) ) ':' ( (lv_type_3_0= ruleTypeExpr ) ) )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1124:1: ( () ( (lv_name_1_0= RULE_ID ) ) ':' ( (lv_type_3_0= ruleTypeExpr ) ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1124:2: () ( (lv_name_1_0= RULE_ID ) ) ':' ( (lv_type_3_0= ruleTypeExpr ) )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1124:2: ()
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1125:5: 
            {
             
                    temp=factory.create(grammarAccess.getParameterDefAccess().getParameterDefAction_0().getType().getClassifier());
                    current = temp; 
                    temp = null;
                    CompositeNode newNode = createCompositeNode(grammarAccess.getParameterDefAccess().getParameterDefAction_0(), currentNode.getParent());
                newNode.getChildren().add(currentNode);
                moveLookaheadInfo(currentNode, newNode);
                currentNode = newNode; 
                    associateNodeWithAstElement(currentNode, current); 
                

            }

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1135:2: ( (lv_name_1_0= RULE_ID ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1136:1: (lv_name_1_0= RULE_ID )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1136:1: (lv_name_1_0= RULE_ID )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1137:3: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)input.LT(1);
            match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleParameterDef2083); 

            			createLeafNode(grammarAccess.getParameterDefAccess().getNameIDTerminalRuleCall_1_0(), "name"); 
            		

            	        if (current==null) {
            	            current = factory.create(grammarAccess.getParameterDefRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode, current);
            	        }
            	        try {
            	       		set(
            	       			current, 
            	       			"name",
            	        		lv_name_1_0, 
            	        		"ID", 
            	        		lastConsumedNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	    

            }


            }

            match(input,26,FOLLOW_26_in_ruleParameterDef2098); 

                    createLeafNode(grammarAccess.getParameterDefAccess().getColonKeyword_2(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1163:1: ( (lv_type_3_0= ruleTypeExpr ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1164:1: (lv_type_3_0= ruleTypeExpr )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1164:1: (lv_type_3_0= ruleTypeExpr )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1165:3: lv_type_3_0= ruleTypeExpr
            {
             
            	        currentNode=createCompositeNode(grammarAccess.getParameterDefAccess().getTypeTypeExprParserRuleCall_3_0(), currentNode); 
            	    
            pushFollow(FOLLOW_ruleTypeExpr_in_ruleParameterDef2119);
            lv_type_3_0=ruleTypeExpr();
            _fsp--;


            	        if (current==null) {
            	            current = factory.create(grammarAccess.getParameterDefRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode.getParent(), current);
            	        }
            	        try {
            	       		set(
            	       			current, 
            	       			"type",
            	        		lv_type_3_0, 
            	        		"TypeExpr", 
            	        		currentNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	        currentNode = currentNode.getParent();
            	    

            }


            }


            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleParameterDef


    // $ANTLR start entryRuleStatements
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1195:1: entryRuleStatements returns [EObject current=null] : iv_ruleStatements= ruleStatements EOF ;
    public final EObject entryRuleStatements() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatements = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1196:2: (iv_ruleStatements= ruleStatements EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1197:2: iv_ruleStatements= ruleStatements EOF
            {
             currentNode = createCompositeNode(grammarAccess.getStatementsRule(), currentNode); 
            pushFollow(FOLLOW_ruleStatements_in_entryRuleStatements2155);
            iv_ruleStatements=ruleStatements();
            _fsp--;

             current =iv_ruleStatements; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStatements2165); 

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
    // $ANTLR end entryRuleStatements


    // $ANTLR start ruleStatements
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1204:1: ruleStatements returns [EObject current=null] : ( () ( RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )* ) ;
    public final EObject ruleStatements() throws RecognitionException {
        EObject current = null;

        EObject lv_statements_2_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1209:6: ( ( () ( RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )* ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1210:1: ( () ( RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )* )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1210:1: ( () ( RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )* )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1210:2: () ( RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )*
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1210:2: ()
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1211:5: 
            {
             
                    temp=factory.create(grammarAccess.getStatementsAccess().getStatementsAction_0().getType().getClassifier());
                    current = temp; 
                    temp = null;
                    CompositeNode newNode = createCompositeNode(grammarAccess.getStatementsAccess().getStatementsAction_0(), currentNode.getParent());
                newNode.getChildren().add(currentNode);
                moveLookaheadInfo(currentNode, newNode);
                currentNode = newNode; 
                    associateNodeWithAstElement(currentNode, current); 
                

            }

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1221:2: ( RULE_NL | ( (lv_statements_2_0= ruleStatement ) ) )*
            loop23:
            do {
                int alt23=3;
                int LA23_0 = input.LA(1);

                if ( (LA23_0==RULE_NL) ) {
                    alt23=1;
                }
                else if ( (LA23_0==RULE_ID||(LA23_0>=RULE_INT && LA23_0<=RULE_STRING)||(LA23_0>=24 && LA23_0<=25)||LA23_0==28||(LA23_0>=31 && LA23_0<=32)||LA23_0==34||(LA23_0>=45 && LA23_0<=46)||(LA23_0>=52 && LA23_0<=53)) ) {
                    alt23=2;
                }


                switch (alt23) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1221:3: RULE_NL
            	    {
            	    match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStatements2209); 
            	     
            	        createLeafNode(grammarAccess.getStatementsAccess().getNLTerminalRuleCall_1_0(), null); 
            	        

            	    }
            	    break;
            	case 2 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1226:6: ( (lv_statements_2_0= ruleStatement ) )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1226:6: ( (lv_statements_2_0= ruleStatement ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1227:1: (lv_statements_2_0= ruleStatement )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1227:1: (lv_statements_2_0= ruleStatement )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1228:3: lv_statements_2_0= ruleStatement
            	    {
            	     
            	    	        currentNode=createCompositeNode(grammarAccess.getStatementsAccess().getStatementsStatementParserRuleCall_1_1_0(), currentNode); 
            	    	    
            	    pushFollow(FOLLOW_ruleStatement_in_ruleStatements2235);
            	    lv_statements_2_0=ruleStatement();
            	    _fsp--;


            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getStatementsRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode.getParent(), current);
            	    	        }
            	    	        try {
            	    	       		add(
            	    	       			current, 
            	    	       			"statements",
            	    	        		lv_statements_2_0, 
            	    	        		"Statement", 
            	    	        		currentNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	        currentNode = currentNode.getParent();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);


            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleStatements


    // $ANTLR start entryRuleStatement
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1258:1: entryRuleStatement returns [EObject current=null] : iv_ruleStatement= ruleStatement EOF ;
    public final EObject entryRuleStatement() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatement = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1259:2: (iv_ruleStatement= ruleStatement EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1260:2: iv_ruleStatement= ruleStatement EOF
            {
             currentNode = createCompositeNode(grammarAccess.getStatementRule(), currentNode); 
            pushFollow(FOLLOW_ruleStatement_in_entryRuleStatement2273);
            iv_ruleStatement=ruleStatement();
            _fsp--;

             current =iv_ruleStatement; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStatement2283); 

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
    // $ANTLR end entryRuleStatement


    // $ANTLR start ruleStatement
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1267:1: ruleStatement returns [EObject current=null] : (this_StmtIf_0= ruleStmtIf | this_StmtWhile_1= ruleStmtWhile | this_VarDef_2= ruleVarDef | this_StmtExpr_3= ruleStmtExpr | this_StmtReturn_4= ruleStmtReturn ) ;
    public final EObject ruleStatement() throws RecognitionException {
        EObject current = null;

        EObject this_StmtIf_0 = null;

        EObject this_StmtWhile_1 = null;

        EObject this_VarDef_2 = null;

        EObject this_StmtExpr_3 = null;

        EObject this_StmtReturn_4 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1272:6: ( (this_StmtIf_0= ruleStmtIf | this_StmtWhile_1= ruleStmtWhile | this_VarDef_2= ruleVarDef | this_StmtExpr_3= ruleStmtExpr | this_StmtReturn_4= ruleStmtReturn ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1273:1: (this_StmtIf_0= ruleStmtIf | this_StmtWhile_1= ruleStmtWhile | this_VarDef_2= ruleVarDef | this_StmtExpr_3= ruleStmtExpr | this_StmtReturn_4= ruleStmtReturn )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1273:1: (this_StmtIf_0= ruleStmtIf | this_StmtWhile_1= ruleStmtWhile | this_VarDef_2= ruleVarDef | this_StmtExpr_3= ruleStmtExpr | this_StmtReturn_4= ruleStmtReturn )
            int alt24=5;
            switch ( input.LA(1) ) {
            case 32:
                {
                alt24=1;
                }
                break;
            case 34:
                {
                alt24=2;
                }
                break;
            case 24:
            case 25:
                {
                alt24=3;
                }
                break;
            case RULE_ID:
            case RULE_INT:
            case RULE_NUMBER:
            case RULE_STRING:
            case 28:
            case 45:
            case 46:
            case 52:
            case 53:
                {
                alt24=4;
                }
                break;
            case 31:
                {
                alt24=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("1273:1: (this_StmtIf_0= ruleStmtIf | this_StmtWhile_1= ruleStmtWhile | this_VarDef_2= ruleVarDef | this_StmtExpr_3= ruleStmtExpr | this_StmtReturn_4= ruleStmtReturn )", 24, 0, input);

                throw nvae;
            }

            switch (alt24) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1274:5: this_StmtIf_0= ruleStmtIf
                    {
                     
                            currentNode=createCompositeNode(grammarAccess.getStatementAccess().getStmtIfParserRuleCall_0(), currentNode); 
                        
                    pushFollow(FOLLOW_ruleStmtIf_in_ruleStatement2330);
                    this_StmtIf_0=ruleStmtIf();
                    _fsp--;

                     
                            current = this_StmtIf_0; 
                            currentNode = currentNode.getParent();
                        

                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1284:5: this_StmtWhile_1= ruleStmtWhile
                    {
                     
                            currentNode=createCompositeNode(grammarAccess.getStatementAccess().getStmtWhileParserRuleCall_1(), currentNode); 
                        
                    pushFollow(FOLLOW_ruleStmtWhile_in_ruleStatement2357);
                    this_StmtWhile_1=ruleStmtWhile();
                    _fsp--;

                     
                            current = this_StmtWhile_1; 
                            currentNode = currentNode.getParent();
                        

                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1294:5: this_VarDef_2= ruleVarDef
                    {
                     
                            currentNode=createCompositeNode(grammarAccess.getStatementAccess().getVarDefParserRuleCall_2(), currentNode); 
                        
                    pushFollow(FOLLOW_ruleVarDef_in_ruleStatement2384);
                    this_VarDef_2=ruleVarDef();
                    _fsp--;

                     
                            current = this_VarDef_2; 
                            currentNode = currentNode.getParent();
                        

                    }
                    break;
                case 4 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1304:5: this_StmtExpr_3= ruleStmtExpr
                    {
                     
                            currentNode=createCompositeNode(grammarAccess.getStatementAccess().getStmtExprParserRuleCall_3(), currentNode); 
                        
                    pushFollow(FOLLOW_ruleStmtExpr_in_ruleStatement2411);
                    this_StmtExpr_3=ruleStmtExpr();
                    _fsp--;

                     
                            current = this_StmtExpr_3; 
                            currentNode = currentNode.getParent();
                        

                    }
                    break;
                case 5 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1314:5: this_StmtReturn_4= ruleStmtReturn
                    {
                     
                            currentNode=createCompositeNode(grammarAccess.getStatementAccess().getStmtReturnParserRuleCall_4(), currentNode); 
                        
                    pushFollow(FOLLOW_ruleStmtReturn_in_ruleStatement2438);
                    this_StmtReturn_4=ruleStmtReturn();
                    _fsp--;

                     
                            current = this_StmtReturn_4; 
                            currentNode = currentNode.getParent();
                        

                    }
                    break;

            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleStatement


    // $ANTLR start entryRuleStmtReturn
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1330:1: entryRuleStmtReturn returns [EObject current=null] : iv_ruleStmtReturn= ruleStmtReturn EOF ;
    public final EObject entryRuleStmtReturn() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStmtReturn = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1331:2: (iv_ruleStmtReturn= ruleStmtReturn EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1332:2: iv_ruleStmtReturn= ruleStmtReturn EOF
            {
             currentNode = createCompositeNode(grammarAccess.getStmtReturnRule(), currentNode); 
            pushFollow(FOLLOW_ruleStmtReturn_in_entryRuleStmtReturn2473);
            iv_ruleStmtReturn=ruleStmtReturn();
            _fsp--;

             current =iv_ruleStmtReturn; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStmtReturn2483); 

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
    // $ANTLR end entryRuleStmtReturn


    // $ANTLR start ruleStmtReturn
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1339:1: ruleStmtReturn returns [EObject current=null] : ( () 'return' ( (lv_e_2_0= ruleExpr ) )? RULE_NL ) ;
    public final EObject ruleStmtReturn() throws RecognitionException {
        EObject current = null;

        EObject lv_e_2_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1344:6: ( ( () 'return' ( (lv_e_2_0= ruleExpr ) )? RULE_NL ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1345:1: ( () 'return' ( (lv_e_2_0= ruleExpr ) )? RULE_NL )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1345:1: ( () 'return' ( (lv_e_2_0= ruleExpr ) )? RULE_NL )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1345:2: () 'return' ( (lv_e_2_0= ruleExpr ) )? RULE_NL
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1345:2: ()
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1346:5: 
            {
             
                    temp=factory.create(grammarAccess.getStmtReturnAccess().getStmtReturnAction_0().getType().getClassifier());
                    current = temp; 
                    temp = null;
                    CompositeNode newNode = createCompositeNode(grammarAccess.getStmtReturnAccess().getStmtReturnAction_0(), currentNode.getParent());
                newNode.getChildren().add(currentNode);
                moveLookaheadInfo(currentNode, newNode);
                currentNode = newNode; 
                    associateNodeWithAstElement(currentNode, current); 
                

            }

            match(input,31,FOLLOW_31_in_ruleStmtReturn2527); 

                    createLeafNode(grammarAccess.getStmtReturnAccess().getReturnKeyword_1(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1360:1: ( (lv_e_2_0= ruleExpr ) )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==RULE_ID||(LA25_0>=RULE_INT && LA25_0<=RULE_STRING)||LA25_0==28||(LA25_0>=45 && LA25_0<=46)||(LA25_0>=52 && LA25_0<=53)) ) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1361:1: (lv_e_2_0= ruleExpr )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1361:1: (lv_e_2_0= ruleExpr )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1362:3: lv_e_2_0= ruleExpr
                    {
                     
                    	        currentNode=createCompositeNode(grammarAccess.getStmtReturnAccess().getEExprParserRuleCall_2_0(), currentNode); 
                    	    
                    pushFollow(FOLLOW_ruleExpr_in_ruleStmtReturn2548);
                    lv_e_2_0=ruleExpr();
                    _fsp--;


                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getStmtReturnRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode.getParent(), current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"e",
                    	        		lv_e_2_0, 
                    	        		"Expr", 
                    	        		currentNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	        currentNode = currentNode.getParent();
                    	    

                    }


                    }
                    break;

            }

            match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtReturn2558); 
             
                createLeafNode(grammarAccess.getStmtReturnAccess().getNLTerminalRuleCall_3(), null); 
                

            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleStmtReturn


    // $ANTLR start entryRuleStmtIf
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1396:1: entryRuleStmtIf returns [EObject current=null] : iv_ruleStmtIf= ruleStmtIf EOF ;
    public final EObject entryRuleStmtIf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStmtIf = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1397:2: (iv_ruleStmtIf= ruleStmtIf EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1398:2: iv_ruleStmtIf= ruleStmtIf EOF
            {
             currentNode = createCompositeNode(grammarAccess.getStmtIfRule(), currentNode); 
            pushFollow(FOLLOW_ruleStmtIf_in_entryRuleStmtIf2593);
            iv_ruleStmtIf=ruleStmtIf();
            _fsp--;

             current =iv_ruleStmtIf; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStmtIf2603); 

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
    // $ANTLR end entryRuleStmtIf


    // $ANTLR start ruleStmtIf
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1405:1: ruleStmtIf returns [EObject current=null] : ( 'if' ( (lv_cond_1_0= ruleExpr ) ) '{' ( (lv_thenBlock_3_0= ruleStatements ) ) '}' ( 'else' '{' ( (lv_elseBlock_7_0= ruleStatements ) ) '}' )? ) ;
    public final EObject ruleStmtIf() throws RecognitionException {
        EObject current = null;

        EObject lv_cond_1_0 = null;

        EObject lv_thenBlock_3_0 = null;

        EObject lv_elseBlock_7_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1410:6: ( ( 'if' ( (lv_cond_1_0= ruleExpr ) ) '{' ( (lv_thenBlock_3_0= ruleStatements ) ) '}' ( 'else' '{' ( (lv_elseBlock_7_0= ruleStatements ) ) '}' )? ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1411:1: ( 'if' ( (lv_cond_1_0= ruleExpr ) ) '{' ( (lv_thenBlock_3_0= ruleStatements ) ) '}' ( 'else' '{' ( (lv_elseBlock_7_0= ruleStatements ) ) '}' )? )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1411:1: ( 'if' ( (lv_cond_1_0= ruleExpr ) ) '{' ( (lv_thenBlock_3_0= ruleStatements ) ) '}' ( 'else' '{' ( (lv_elseBlock_7_0= ruleStatements ) ) '}' )? )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1411:3: 'if' ( (lv_cond_1_0= ruleExpr ) ) '{' ( (lv_thenBlock_3_0= ruleStatements ) ) '}' ( 'else' '{' ( (lv_elseBlock_7_0= ruleStatements ) ) '}' )?
            {
            match(input,32,FOLLOW_32_in_ruleStmtIf2638); 

                    createLeafNode(grammarAccess.getStmtIfAccess().getIfKeyword_0(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1415:1: ( (lv_cond_1_0= ruleExpr ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1416:1: (lv_cond_1_0= ruleExpr )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1416:1: (lv_cond_1_0= ruleExpr )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1417:3: lv_cond_1_0= ruleExpr
            {
             
            	        currentNode=createCompositeNode(grammarAccess.getStmtIfAccess().getCondExprParserRuleCall_1_0(), currentNode); 
            	    
            pushFollow(FOLLOW_ruleExpr_in_ruleStmtIf2659);
            lv_cond_1_0=ruleExpr();
            _fsp--;


            	        if (current==null) {
            	            current = factory.create(grammarAccess.getStmtIfRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode.getParent(), current);
            	        }
            	        try {
            	       		set(
            	       			current, 
            	       			"cond",
            	        		lv_cond_1_0, 
            	        		"Expr", 
            	        		currentNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	        currentNode = currentNode.getParent();
            	    

            }


            }

            match(input,15,FOLLOW_15_in_ruleStmtIf2669); 

                    createLeafNode(grammarAccess.getStmtIfAccess().getLeftCurlyBracketKeyword_2(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1443:1: ( (lv_thenBlock_3_0= ruleStatements ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1444:1: (lv_thenBlock_3_0= ruleStatements )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1444:1: (lv_thenBlock_3_0= ruleStatements )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1445:3: lv_thenBlock_3_0= ruleStatements
            {
             
            	        currentNode=createCompositeNode(grammarAccess.getStmtIfAccess().getThenBlockStatementsParserRuleCall_3_0(), currentNode); 
            	    
            pushFollow(FOLLOW_ruleStatements_in_ruleStmtIf2690);
            lv_thenBlock_3_0=ruleStatements();
            _fsp--;


            	        if (current==null) {
            	            current = factory.create(grammarAccess.getStmtIfRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode.getParent(), current);
            	        }
            	        try {
            	       		set(
            	       			current, 
            	       			"thenBlock",
            	        		lv_thenBlock_3_0, 
            	        		"Statements", 
            	        		currentNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	        currentNode = currentNode.getParent();
            	    

            }


            }

            match(input,16,FOLLOW_16_in_ruleStmtIf2700); 

                    createLeafNode(grammarAccess.getStmtIfAccess().getRightCurlyBracketKeyword_4(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1471:1: ( 'else' '{' ( (lv_elseBlock_7_0= ruleStatements ) ) '}' )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==33) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1471:3: 'else' '{' ( (lv_elseBlock_7_0= ruleStatements ) ) '}'
                    {
                    match(input,33,FOLLOW_33_in_ruleStmtIf2711); 

                            createLeafNode(grammarAccess.getStmtIfAccess().getElseKeyword_5_0(), null); 
                        
                    match(input,15,FOLLOW_15_in_ruleStmtIf2721); 

                            createLeafNode(grammarAccess.getStmtIfAccess().getLeftCurlyBracketKeyword_5_1(), null); 
                        
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1479:1: ( (lv_elseBlock_7_0= ruleStatements ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1480:1: (lv_elseBlock_7_0= ruleStatements )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1480:1: (lv_elseBlock_7_0= ruleStatements )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1481:3: lv_elseBlock_7_0= ruleStatements
                    {
                     
                    	        currentNode=createCompositeNode(grammarAccess.getStmtIfAccess().getElseBlockStatementsParserRuleCall_5_2_0(), currentNode); 
                    	    
                    pushFollow(FOLLOW_ruleStatements_in_ruleStmtIf2742);
                    lv_elseBlock_7_0=ruleStatements();
                    _fsp--;


                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getStmtIfRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode.getParent(), current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"elseBlock",
                    	        		lv_elseBlock_7_0, 
                    	        		"Statements", 
                    	        		currentNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	        currentNode = currentNode.getParent();
                    	    

                    }


                    }

                    match(input,16,FOLLOW_16_in_ruleStmtIf2752); 

                            createLeafNode(grammarAccess.getStmtIfAccess().getRightCurlyBracketKeyword_5_3(), null); 
                        

                    }
                    break;

            }


            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleStmtIf


    // $ANTLR start entryRuleStmtWhile
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1515:1: entryRuleStmtWhile returns [EObject current=null] : iv_ruleStmtWhile= ruleStmtWhile EOF ;
    public final EObject entryRuleStmtWhile() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStmtWhile = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1516:2: (iv_ruleStmtWhile= ruleStmtWhile EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1517:2: iv_ruleStmtWhile= ruleStmtWhile EOF
            {
             currentNode = createCompositeNode(grammarAccess.getStmtWhileRule(), currentNode); 
            pushFollow(FOLLOW_ruleStmtWhile_in_entryRuleStmtWhile2790);
            iv_ruleStmtWhile=ruleStmtWhile();
            _fsp--;

             current =iv_ruleStmtWhile; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStmtWhile2800); 

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
    // $ANTLR end entryRuleStmtWhile


    // $ANTLR start ruleStmtWhile
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1524:1: ruleStmtWhile returns [EObject current=null] : ( 'while' ( (lv_cond_1_0= ruleExpr ) ) '{' ( (lv_body_3_0= ruleStatements ) ) '}' ) ;
    public final EObject ruleStmtWhile() throws RecognitionException {
        EObject current = null;

        EObject lv_cond_1_0 = null;

        EObject lv_body_3_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1529:6: ( ( 'while' ( (lv_cond_1_0= ruleExpr ) ) '{' ( (lv_body_3_0= ruleStatements ) ) '}' ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1530:1: ( 'while' ( (lv_cond_1_0= ruleExpr ) ) '{' ( (lv_body_3_0= ruleStatements ) ) '}' )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1530:1: ( 'while' ( (lv_cond_1_0= ruleExpr ) ) '{' ( (lv_body_3_0= ruleStatements ) ) '}' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1530:3: 'while' ( (lv_cond_1_0= ruleExpr ) ) '{' ( (lv_body_3_0= ruleStatements ) ) '}'
            {
            match(input,34,FOLLOW_34_in_ruleStmtWhile2835); 

                    createLeafNode(grammarAccess.getStmtWhileAccess().getWhileKeyword_0(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1534:1: ( (lv_cond_1_0= ruleExpr ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1535:1: (lv_cond_1_0= ruleExpr )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1535:1: (lv_cond_1_0= ruleExpr )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1536:3: lv_cond_1_0= ruleExpr
            {
             
            	        currentNode=createCompositeNode(grammarAccess.getStmtWhileAccess().getCondExprParserRuleCall_1_0(), currentNode); 
            	    
            pushFollow(FOLLOW_ruleExpr_in_ruleStmtWhile2856);
            lv_cond_1_0=ruleExpr();
            _fsp--;


            	        if (current==null) {
            	            current = factory.create(grammarAccess.getStmtWhileRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode.getParent(), current);
            	        }
            	        try {
            	       		set(
            	       			current, 
            	       			"cond",
            	        		lv_cond_1_0, 
            	        		"Expr", 
            	        		currentNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	        currentNode = currentNode.getParent();
            	    

            }


            }

            match(input,15,FOLLOW_15_in_ruleStmtWhile2866); 

                    createLeafNode(grammarAccess.getStmtWhileAccess().getLeftCurlyBracketKeyword_2(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1562:1: ( (lv_body_3_0= ruleStatements ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1563:1: (lv_body_3_0= ruleStatements )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1563:1: (lv_body_3_0= ruleStatements )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1564:3: lv_body_3_0= ruleStatements
            {
             
            	        currentNode=createCompositeNode(grammarAccess.getStmtWhileAccess().getBodyStatementsParserRuleCall_3_0(), currentNode); 
            	    
            pushFollow(FOLLOW_ruleStatements_in_ruleStmtWhile2887);
            lv_body_3_0=ruleStatements();
            _fsp--;


            	        if (current==null) {
            	            current = factory.create(grammarAccess.getStmtWhileRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode.getParent(), current);
            	        }
            	        try {
            	       		set(
            	       			current, 
            	       			"body",
            	        		lv_body_3_0, 
            	        		"Statements", 
            	        		currentNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	        currentNode = currentNode.getParent();
            	    

            }


            }

            match(input,16,FOLLOW_16_in_ruleStmtWhile2897); 

                    createLeafNode(grammarAccess.getStmtWhileAccess().getRightCurlyBracketKeyword_4(), null); 
                

            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleStmtWhile


    // $ANTLR start entryRuleStmtExpr
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1598:1: entryRuleStmtExpr returns [EObject current=null] : iv_ruleStmtExpr= ruleStmtExpr EOF ;
    public final EObject entryRuleStmtExpr() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStmtExpr = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1599:2: (iv_ruleStmtExpr= ruleStmtExpr EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1600:2: iv_ruleStmtExpr= ruleStmtExpr EOF
            {
             currentNode = createCompositeNode(grammarAccess.getStmtExprRule(), currentNode); 
            pushFollow(FOLLOW_ruleStmtExpr_in_entryRuleStmtExpr2933);
            iv_ruleStmtExpr=ruleStmtExpr();
            _fsp--;

             current =iv_ruleStmtExpr; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleStmtExpr2943); 

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
    // $ANTLR end entryRuleStmtExpr


    // $ANTLR start ruleStmtExpr
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1607:1: ruleStmtExpr returns [EObject current=null] : ( ( (lv_e_0_0= ruleExpr ) ) RULE_NL ) ;
    public final EObject ruleStmtExpr() throws RecognitionException {
        EObject current = null;

        EObject lv_e_0_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1612:6: ( ( ( (lv_e_0_0= ruleExpr ) ) RULE_NL ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1613:1: ( ( (lv_e_0_0= ruleExpr ) ) RULE_NL )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1613:1: ( ( (lv_e_0_0= ruleExpr ) ) RULE_NL )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1613:2: ( (lv_e_0_0= ruleExpr ) ) RULE_NL
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1613:2: ( (lv_e_0_0= ruleExpr ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1614:1: (lv_e_0_0= ruleExpr )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1614:1: (lv_e_0_0= ruleExpr )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1615:3: lv_e_0_0= ruleExpr
            {
             
            	        currentNode=createCompositeNode(grammarAccess.getStmtExprAccess().getEExprParserRuleCall_0_0(), currentNode); 
            	    
            pushFollow(FOLLOW_ruleExpr_in_ruleStmtExpr2989);
            lv_e_0_0=ruleExpr();
            _fsp--;


            	        if (current==null) {
            	            current = factory.create(grammarAccess.getStmtExprRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode.getParent(), current);
            	        }
            	        try {
            	       		set(
            	       			current, 
            	       			"e",
            	        		lv_e_0_0, 
            	        		"Expr", 
            	        		currentNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	        currentNode = currentNode.getParent();
            	    

            }


            }

            match(input,RULE_NL,FOLLOW_RULE_NL_in_ruleStmtExpr2998); 
             
                createLeafNode(grammarAccess.getStmtExprAccess().getNLTerminalRuleCall_1(), null); 
                

            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleStmtExpr


    // $ANTLR start entryRuleExpr
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1649:1: entryRuleExpr returns [EObject current=null] : iv_ruleExpr= ruleExpr EOF ;
    public final EObject entryRuleExpr() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExpr = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1650:2: (iv_ruleExpr= ruleExpr EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1651:2: iv_ruleExpr= ruleExpr EOF
            {
             currentNode = createCompositeNode(grammarAccess.getExprRule(), currentNode); 
            pushFollow(FOLLOW_ruleExpr_in_entryRuleExpr3033);
            iv_ruleExpr=ruleExpr();
            _fsp--;

             current =iv_ruleExpr; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExpr3043); 

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
    // $ANTLR end entryRuleExpr


    // $ANTLR start ruleExpr
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1658:1: ruleExpr returns [EObject current=null] : this_ExprAssignment_0= ruleExprAssignment ;
    public final EObject ruleExpr() throws RecognitionException {
        EObject current = null;

        EObject this_ExprAssignment_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1663:6: (this_ExprAssignment_0= ruleExprAssignment )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1665:5: this_ExprAssignment_0= ruleExprAssignment
            {
             
                    currentNode=createCompositeNode(grammarAccess.getExprAccess().getExprAssignmentParserRuleCall(), currentNode); 
                
            pushFollow(FOLLOW_ruleExprAssignment_in_ruleExpr3089);
            this_ExprAssignment_0=ruleExprAssignment();
            _fsp--;

             
                    current = this_ExprAssignment_0; 
                    currentNode = currentNode.getParent();
                

            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleExpr


    // $ANTLR start entryRuleExprAssignment
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1681:1: entryRuleExprAssignment returns [EObject current=null] : iv_ruleExprAssignment= ruleExprAssignment EOF ;
    public final EObject entryRuleExprAssignment() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprAssignment = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1682:2: (iv_ruleExprAssignment= ruleExprAssignment EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1683:2: iv_ruleExprAssignment= ruleExprAssignment EOF
            {
             currentNode = createCompositeNode(grammarAccess.getExprAssignmentRule(), currentNode); 
            pushFollow(FOLLOW_ruleExprAssignment_in_entryRuleExprAssignment3123);
            iv_ruleExprAssignment=ruleExprAssignment();
            _fsp--;

             current =iv_ruleExprAssignment; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprAssignment3133); 

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
    // $ANTLR end entryRuleExprAssignment


    // $ANTLR start ruleExprAssignment
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1690:1: ruleExprAssignment returns [EObject current=null] : (this_ExprOr_0= ruleExprOr ( () ( ( (lv_op_2_1= '=' | lv_op_2_2= '+=' | lv_op_2_3= '-=' ) ) ) ( (lv_right_3_0= ruleExprOr ) ) )* ) ;
    public final EObject ruleExprAssignment() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_1=null;
        Token lv_op_2_2=null;
        Token lv_op_2_3=null;
        EObject this_ExprOr_0 = null;

        EObject lv_right_3_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1695:6: ( (this_ExprOr_0= ruleExprOr ( () ( ( (lv_op_2_1= '=' | lv_op_2_2= '+=' | lv_op_2_3= '-=' ) ) ) ( (lv_right_3_0= ruleExprOr ) ) )* ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1696:1: (this_ExprOr_0= ruleExprOr ( () ( ( (lv_op_2_1= '=' | lv_op_2_2= '+=' | lv_op_2_3= '-=' ) ) ) ( (lv_right_3_0= ruleExprOr ) ) )* )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1696:1: (this_ExprOr_0= ruleExprOr ( () ( ( (lv_op_2_1= '=' | lv_op_2_2= '+=' | lv_op_2_3= '-=' ) ) ) ( (lv_right_3_0= ruleExprOr ) ) )* )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1697:5: this_ExprOr_0= ruleExprOr ( () ( ( (lv_op_2_1= '=' | lv_op_2_2= '+=' | lv_op_2_3= '-=' ) ) ) ( (lv_right_3_0= ruleExprOr ) ) )*
            {
             
                    currentNode=createCompositeNode(grammarAccess.getExprAssignmentAccess().getExprOrParserRuleCall_0(), currentNode); 
                
            pushFollow(FOLLOW_ruleExprOr_in_ruleExprAssignment3180);
            this_ExprOr_0=ruleExprOr();
            _fsp--;

             
                    current = this_ExprOr_0; 
                    currentNode = currentNode.getParent();
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1705:1: ( () ( ( (lv_op_2_1= '=' | lv_op_2_2= '+=' | lv_op_2_3= '-=' ) ) ) ( (lv_right_3_0= ruleExprOr ) ) )*
            loop28:
            do {
                int alt28=2;
                switch ( input.LA(1) ) {
                case 22:
                    {
                    alt28=1;
                    }
                    break;
                case 35:
                    {
                    alt28=1;
                    }
                    break;
                case 36:
                    {
                    alt28=1;
                    }
                    break;

                }

                switch (alt28) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1705:2: () ( ( (lv_op_2_1= '=' | lv_op_2_2= '+=' | lv_op_2_3= '-=' ) ) ) ( (lv_right_3_0= ruleExprOr ) )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1705:2: ()
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1706:5: 
            	    {
            	     
            	            temp=factory.create(grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0().getType().getClassifier());
            	            try {
            	            	factory.set(temp, "left", current, null /*ParserRule*/, currentNode);
            	            } catch(ValueConverterException vce) {
            	            	handleValueConverterException(vce);
            	            }
            	            current = temp; 
            	            temp = null;
            	            CompositeNode newNode = createCompositeNode(grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0(), currentNode.getParent());
            	        newNode.getChildren().add(currentNode);
            	        moveLookaheadInfo(currentNode, newNode);
            	        currentNode = newNode; 
            	            associateNodeWithAstElement(currentNode, current); 
            	        

            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1721:2: ( ( (lv_op_2_1= '=' | lv_op_2_2= '+=' | lv_op_2_3= '-=' ) ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1722:1: ( (lv_op_2_1= '=' | lv_op_2_2= '+=' | lv_op_2_3= '-=' ) )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1722:1: ( (lv_op_2_1= '=' | lv_op_2_2= '+=' | lv_op_2_3= '-=' ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1723:1: (lv_op_2_1= '=' | lv_op_2_2= '+=' | lv_op_2_3= '-=' )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1723:1: (lv_op_2_1= '=' | lv_op_2_2= '+=' | lv_op_2_3= '-=' )
            	    int alt27=3;
            	    switch ( input.LA(1) ) {
            	    case 22:
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
            	    default:
            	        NoViableAltException nvae =
            	            new NoViableAltException("1723:1: (lv_op_2_1= '=' | lv_op_2_2= '+=' | lv_op_2_3= '-=' )", 27, 0, input);

            	        throw nvae;
            	    }

            	    switch (alt27) {
            	        case 1 :
            	            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1724:3: lv_op_2_1= '='
            	            {
            	            lv_op_2_1=(Token)input.LT(1);
            	            match(input,22,FOLLOW_22_in_ruleExprAssignment3209); 

            	                    createLeafNode(grammarAccess.getExprAssignmentAccess().getOpEqualsSignKeyword_1_1_0_0(), "op"); 
            	                

            	            	        if (current==null) {
            	            	            current = factory.create(grammarAccess.getExprAssignmentRule().getType().getClassifier());
            	            	            associateNodeWithAstElement(currentNode, current);
            	            	        }
            	            	        
            	            	        try {
            	            	       		set(current, "op", lv_op_2_1, null, lastConsumedNode);
            	            	        } catch (ValueConverterException vce) {
            	            				handleValueConverterException(vce);
            	            	        }
            	            	    

            	            }
            	            break;
            	        case 2 :
            	            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1742:8: lv_op_2_2= '+='
            	            {
            	            lv_op_2_2=(Token)input.LT(1);
            	            match(input,35,FOLLOW_35_in_ruleExprAssignment3238); 

            	                    createLeafNode(grammarAccess.getExprAssignmentAccess().getOpPlusSignEqualsSignKeyword_1_1_0_1(), "op"); 
            	                

            	            	        if (current==null) {
            	            	            current = factory.create(grammarAccess.getExprAssignmentRule().getType().getClassifier());
            	            	            associateNodeWithAstElement(currentNode, current);
            	            	        }
            	            	        
            	            	        try {
            	            	       		set(current, "op", lv_op_2_2, null, lastConsumedNode);
            	            	        } catch (ValueConverterException vce) {
            	            				handleValueConverterException(vce);
            	            	        }
            	            	    

            	            }
            	            break;
            	        case 3 :
            	            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1760:8: lv_op_2_3= '-='
            	            {
            	            lv_op_2_3=(Token)input.LT(1);
            	            match(input,36,FOLLOW_36_in_ruleExprAssignment3267); 

            	                    createLeafNode(grammarAccess.getExprAssignmentAccess().getOpHyphenMinusEqualsSignKeyword_1_1_0_2(), "op"); 
            	                

            	            	        if (current==null) {
            	            	            current = factory.create(grammarAccess.getExprAssignmentRule().getType().getClassifier());
            	            	            associateNodeWithAstElement(currentNode, current);
            	            	        }
            	            	        
            	            	        try {
            	            	       		set(current, "op", lv_op_2_3, null, lastConsumedNode);
            	            	        } catch (ValueConverterException vce) {
            	            				handleValueConverterException(vce);
            	            	        }
            	            	    

            	            }
            	            break;

            	    }


            	    }


            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1781:2: ( (lv_right_3_0= ruleExprOr ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1782:1: (lv_right_3_0= ruleExprOr )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1782:1: (lv_right_3_0= ruleExprOr )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1783:3: lv_right_3_0= ruleExprOr
            	    {
            	     
            	    	        currentNode=createCompositeNode(grammarAccess.getExprAssignmentAccess().getRightExprOrParserRuleCall_1_2_0(), currentNode); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprOr_in_ruleExprAssignment3304);
            	    lv_right_3_0=ruleExprOr();
            	    _fsp--;


            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getExprAssignmentRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode.getParent(), current);
            	    	        }
            	    	        try {
            	    	       		set(
            	    	       			current, 
            	    	       			"right",
            	    	        		lv_right_3_0, 
            	    	        		"ExprOr", 
            	    	        		currentNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	        currentNode = currentNode.getParent();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);


            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleExprAssignment


    // $ANTLR start entryRuleExprOr
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1813:1: entryRuleExprOr returns [EObject current=null] : iv_ruleExprOr= ruleExprOr EOF ;
    public final EObject entryRuleExprOr() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprOr = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1814:2: (iv_ruleExprOr= ruleExprOr EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1815:2: iv_ruleExprOr= ruleExprOr EOF
            {
             currentNode = createCompositeNode(grammarAccess.getExprOrRule(), currentNode); 
            pushFollow(FOLLOW_ruleExprOr_in_entryRuleExprOr3342);
            iv_ruleExprOr=ruleExprOr();
            _fsp--;

             current =iv_ruleExprOr; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprOr3352); 

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
    // $ANTLR end entryRuleExprOr


    // $ANTLR start ruleExprOr
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1822:1: ruleExprOr returns [EObject current=null] : (this_ExprAnd_0= ruleExprAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )* ) ;
    public final EObject ruleExprOr() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_ExprAnd_0 = null;

        EObject lv_right_3_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1827:6: ( (this_ExprAnd_0= ruleExprAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )* ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1828:1: (this_ExprAnd_0= ruleExprAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )* )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1828:1: (this_ExprAnd_0= ruleExprAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )* )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1829:5: this_ExprAnd_0= ruleExprAnd ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )*
            {
             
                    currentNode=createCompositeNode(grammarAccess.getExprOrAccess().getExprAndParserRuleCall_0(), currentNode); 
                
            pushFollow(FOLLOW_ruleExprAnd_in_ruleExprOr3399);
            this_ExprAnd_0=ruleExprAnd();
            _fsp--;

             
                    current = this_ExprAnd_0; 
                    currentNode = currentNode.getParent();
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1837:1: ( () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) ) )*
            loop29:
            do {
                int alt29=2;
                int LA29_0 = input.LA(1);

                if ( (LA29_0==37) ) {
                    alt29=1;
                }


                switch (alt29) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1837:2: () ( (lv_op_2_0= 'or' ) ) ( (lv_right_3_0= ruleExprAnd ) )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1837:2: ()
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1838:5: 
            	    {
            	     
            	            temp=factory.create(grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0().getType().getClassifier());
            	            try {
            	            	factory.set(temp, "left", current, null /*ParserRule*/, currentNode);
            	            } catch(ValueConverterException vce) {
            	            	handleValueConverterException(vce);
            	            }
            	            current = temp; 
            	            temp = null;
            	            CompositeNode newNode = createCompositeNode(grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0(), currentNode.getParent());
            	        newNode.getChildren().add(currentNode);
            	        moveLookaheadInfo(currentNode, newNode);
            	        currentNode = newNode; 
            	            associateNodeWithAstElement(currentNode, current); 
            	        

            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1853:2: ( (lv_op_2_0= 'or' ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1854:1: (lv_op_2_0= 'or' )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1854:1: (lv_op_2_0= 'or' )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1855:3: lv_op_2_0= 'or'
            	    {
            	    lv_op_2_0=(Token)input.LT(1);
            	    match(input,37,FOLLOW_37_in_ruleExprOr3426); 

            	            createLeafNode(grammarAccess.getExprOrAccess().getOpOrKeyword_1_1_0(), "op"); 
            	        

            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getExprOrRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode, current);
            	    	        }
            	    	        
            	    	        try {
            	    	       		set(current, "op", lv_op_2_0, "or", lastConsumedNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	    

            	    }


            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1874:2: ( (lv_right_3_0= ruleExprAnd ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1875:1: (lv_right_3_0= ruleExprAnd )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1875:1: (lv_right_3_0= ruleExprAnd )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1876:3: lv_right_3_0= ruleExprAnd
            	    {
            	     
            	    	        currentNode=createCompositeNode(grammarAccess.getExprOrAccess().getRightExprAndParserRuleCall_1_2_0(), currentNode); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprAnd_in_ruleExprOr3460);
            	    lv_right_3_0=ruleExprAnd();
            	    _fsp--;


            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getExprOrRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode.getParent(), current);
            	    	        }
            	    	        try {
            	    	       		set(
            	    	       			current, 
            	    	       			"right",
            	    	        		lv_right_3_0, 
            	    	        		"ExprAnd", 
            	    	        		currentNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	        currentNode = currentNode.getParent();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop29;
                }
            } while (true);


            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleExprOr


    // $ANTLR start entryRuleExprAnd
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1906:1: entryRuleExprAnd returns [EObject current=null] : iv_ruleExprAnd= ruleExprAnd EOF ;
    public final EObject entryRuleExprAnd() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprAnd = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1907:2: (iv_ruleExprAnd= ruleExprAnd EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1908:2: iv_ruleExprAnd= ruleExprAnd EOF
            {
             currentNode = createCompositeNode(grammarAccess.getExprAndRule(), currentNode); 
            pushFollow(FOLLOW_ruleExprAnd_in_entryRuleExprAnd3498);
            iv_ruleExprAnd=ruleExprAnd();
            _fsp--;

             current =iv_ruleExprAnd; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprAnd3508); 

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
    // $ANTLR end entryRuleExprAnd


    // $ANTLR start ruleExprAnd
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1915:1: ruleExprAnd returns [EObject current=null] : (this_ExprEquality_0= ruleExprEquality ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )* ) ;
    public final EObject ruleExprAnd() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_ExprEquality_0 = null;

        EObject lv_right_3_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1920:6: ( (this_ExprEquality_0= ruleExprEquality ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )* ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1921:1: (this_ExprEquality_0= ruleExprEquality ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )* )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1921:1: (this_ExprEquality_0= ruleExprEquality ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )* )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1922:5: this_ExprEquality_0= ruleExprEquality ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )*
            {
             
                    currentNode=createCompositeNode(grammarAccess.getExprAndAccess().getExprEqualityParserRuleCall_0(), currentNode); 
                
            pushFollow(FOLLOW_ruleExprEquality_in_ruleExprAnd3555);
            this_ExprEquality_0=ruleExprEquality();
            _fsp--;

             
                    current = this_ExprEquality_0; 
                    currentNode = currentNode.getParent();
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1930:1: ( () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) ) )*
            loop30:
            do {
                int alt30=2;
                int LA30_0 = input.LA(1);

                if ( (LA30_0==38) ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1930:2: () ( (lv_op_2_0= 'and' ) ) ( (lv_right_3_0= ruleExprEquality ) )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1930:2: ()
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1931:5: 
            	    {
            	     
            	            temp=factory.create(grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0().getType().getClassifier());
            	            try {
            	            	factory.set(temp, "left", current, null /*ParserRule*/, currentNode);
            	            } catch(ValueConverterException vce) {
            	            	handleValueConverterException(vce);
            	            }
            	            current = temp; 
            	            temp = null;
            	            CompositeNode newNode = createCompositeNode(grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0(), currentNode.getParent());
            	        newNode.getChildren().add(currentNode);
            	        moveLookaheadInfo(currentNode, newNode);
            	        currentNode = newNode; 
            	            associateNodeWithAstElement(currentNode, current); 
            	        

            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1946:2: ( (lv_op_2_0= 'and' ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1947:1: (lv_op_2_0= 'and' )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1947:1: (lv_op_2_0= 'and' )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1948:3: lv_op_2_0= 'and'
            	    {
            	    lv_op_2_0=(Token)input.LT(1);
            	    match(input,38,FOLLOW_38_in_ruleExprAnd3582); 

            	            createLeafNode(grammarAccess.getExprAndAccess().getOpAndKeyword_1_1_0(), "op"); 
            	        

            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getExprAndRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode, current);
            	    	        }
            	    	        
            	    	        try {
            	    	       		set(current, "op", lv_op_2_0, "and", lastConsumedNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	    

            	    }


            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1967:2: ( (lv_right_3_0= ruleExprEquality ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1968:1: (lv_right_3_0= ruleExprEquality )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1968:1: (lv_right_3_0= ruleExprEquality )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1969:3: lv_right_3_0= ruleExprEquality
            	    {
            	     
            	    	        currentNode=createCompositeNode(grammarAccess.getExprAndAccess().getRightExprEqualityParserRuleCall_1_2_0(), currentNode); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprEquality_in_ruleExprAnd3616);
            	    lv_right_3_0=ruleExprEquality();
            	    _fsp--;


            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getExprAndRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode.getParent(), current);
            	    	        }
            	    	        try {
            	    	       		set(
            	    	       			current, 
            	    	       			"right",
            	    	        		lv_right_3_0, 
            	    	        		"ExprEquality", 
            	    	        		currentNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	        currentNode = currentNode.getParent();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop30;
                }
            } while (true);


            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleExprAnd


    // $ANTLR start entryRuleExprEquality
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:1999:1: entryRuleExprEquality returns [EObject current=null] : iv_ruleExprEquality= ruleExprEquality EOF ;
    public final EObject entryRuleExprEquality() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprEquality = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2000:2: (iv_ruleExprEquality= ruleExprEquality EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2001:2: iv_ruleExprEquality= ruleExprEquality EOF
            {
             currentNode = createCompositeNode(grammarAccess.getExprEqualityRule(), currentNode); 
            pushFollow(FOLLOW_ruleExprEquality_in_entryRuleExprEquality3654);
            iv_ruleExprEquality=ruleExprEquality();
            _fsp--;

             current =iv_ruleExprEquality; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprEquality3664); 

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
    // $ANTLR end entryRuleExprEquality


    // $ANTLR start ruleExprEquality
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2008:1: ruleExprEquality returns [EObject current=null] : (this_ExprComparison_0= ruleExprComparison ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '==' ) ) ) ( (lv_right_3_0= ruleExprComparison ) ) )* ) ;
    public final EObject ruleExprEquality() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_1=null;
        Token lv_op_2_2=null;
        EObject this_ExprComparison_0 = null;

        EObject lv_right_3_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2013:6: ( (this_ExprComparison_0= ruleExprComparison ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '==' ) ) ) ( (lv_right_3_0= ruleExprComparison ) ) )* ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2014:1: (this_ExprComparison_0= ruleExprComparison ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '==' ) ) ) ( (lv_right_3_0= ruleExprComparison ) ) )* )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2014:1: (this_ExprComparison_0= ruleExprComparison ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '==' ) ) ) ( (lv_right_3_0= ruleExprComparison ) ) )* )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2015:5: this_ExprComparison_0= ruleExprComparison ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '==' ) ) ) ( (lv_right_3_0= ruleExprComparison ) ) )*
            {
             
                    currentNode=createCompositeNode(grammarAccess.getExprEqualityAccess().getExprComparisonParserRuleCall_0(), currentNode); 
                
            pushFollow(FOLLOW_ruleExprComparison_in_ruleExprEquality3711);
            this_ExprComparison_0=ruleExprComparison();
            _fsp--;

             
                    current = this_ExprComparison_0; 
                    currentNode = currentNode.getParent();
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2023:1: ( () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '==' ) ) ) ( (lv_right_3_0= ruleExprComparison ) ) )*
            loop32:
            do {
                int alt32=2;
                int LA32_0 = input.LA(1);

                if ( (LA32_0==39) ) {
                    alt32=1;
                }
                else if ( (LA32_0==40) ) {
                    alt32=1;
                }


                switch (alt32) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2023:2: () ( ( (lv_op_2_1= '!=' | lv_op_2_2= '==' ) ) ) ( (lv_right_3_0= ruleExprComparison ) )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2023:2: ()
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2024:5: 
            	    {
            	     
            	            temp=factory.create(grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0().getType().getClassifier());
            	            try {
            	            	factory.set(temp, "left", current, null /*ParserRule*/, currentNode);
            	            } catch(ValueConverterException vce) {
            	            	handleValueConverterException(vce);
            	            }
            	            current = temp; 
            	            temp = null;
            	            CompositeNode newNode = createCompositeNode(grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0(), currentNode.getParent());
            	        newNode.getChildren().add(currentNode);
            	        moveLookaheadInfo(currentNode, newNode);
            	        currentNode = newNode; 
            	            associateNodeWithAstElement(currentNode, current); 
            	        

            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2039:2: ( ( (lv_op_2_1= '!=' | lv_op_2_2= '==' ) ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2040:1: ( (lv_op_2_1= '!=' | lv_op_2_2= '==' ) )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2040:1: ( (lv_op_2_1= '!=' | lv_op_2_2= '==' ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2041:1: (lv_op_2_1= '!=' | lv_op_2_2= '==' )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2041:1: (lv_op_2_1= '!=' | lv_op_2_2= '==' )
            	    int alt31=2;
            	    int LA31_0 = input.LA(1);

            	    if ( (LA31_0==39) ) {
            	        alt31=1;
            	    }
            	    else if ( (LA31_0==40) ) {
            	        alt31=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("2041:1: (lv_op_2_1= '!=' | lv_op_2_2= '==' )", 31, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt31) {
            	        case 1 :
            	            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2042:3: lv_op_2_1= '!='
            	            {
            	            lv_op_2_1=(Token)input.LT(1);
            	            match(input,39,FOLLOW_39_in_ruleExprEquality3740); 

            	                    createLeafNode(grammarAccess.getExprEqualityAccess().getOpExclamationMarkEqualsSignKeyword_1_1_0_0(), "op"); 
            	                

            	            	        if (current==null) {
            	            	            current = factory.create(grammarAccess.getExprEqualityRule().getType().getClassifier());
            	            	            associateNodeWithAstElement(currentNode, current);
            	            	        }
            	            	        
            	            	        try {
            	            	       		set(current, "op", lv_op_2_1, null, lastConsumedNode);
            	            	        } catch (ValueConverterException vce) {
            	            				handleValueConverterException(vce);
            	            	        }
            	            	    

            	            }
            	            break;
            	        case 2 :
            	            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2060:8: lv_op_2_2= '=='
            	            {
            	            lv_op_2_2=(Token)input.LT(1);
            	            match(input,40,FOLLOW_40_in_ruleExprEquality3769); 

            	                    createLeafNode(grammarAccess.getExprEqualityAccess().getOpEqualsSignEqualsSignKeyword_1_1_0_1(), "op"); 
            	                

            	            	        if (current==null) {
            	            	            current = factory.create(grammarAccess.getExprEqualityRule().getType().getClassifier());
            	            	            associateNodeWithAstElement(currentNode, current);
            	            	        }
            	            	        
            	            	        try {
            	            	       		set(current, "op", lv_op_2_2, null, lastConsumedNode);
            	            	        } catch (ValueConverterException vce) {
            	            				handleValueConverterException(vce);
            	            	        }
            	            	    

            	            }
            	            break;

            	    }


            	    }


            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2081:2: ( (lv_right_3_0= ruleExprComparison ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2082:1: (lv_right_3_0= ruleExprComparison )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2082:1: (lv_right_3_0= ruleExprComparison )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2083:3: lv_right_3_0= ruleExprComparison
            	    {
            	     
            	    	        currentNode=createCompositeNode(grammarAccess.getExprEqualityAccess().getRightExprComparisonParserRuleCall_1_2_0(), currentNode); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprComparison_in_ruleExprEquality3806);
            	    lv_right_3_0=ruleExprComparison();
            	    _fsp--;


            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getExprEqualityRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode.getParent(), current);
            	    	        }
            	    	        try {
            	    	       		set(
            	    	       			current, 
            	    	       			"right",
            	    	        		lv_right_3_0, 
            	    	        		"ExprComparison", 
            	    	        		currentNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	        currentNode = currentNode.getParent();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop32;
                }
            } while (true);


            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleExprEquality


    // $ANTLR start entryRuleExprComparison
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2113:1: entryRuleExprComparison returns [EObject current=null] : iv_ruleExprComparison= ruleExprComparison EOF ;
    public final EObject entryRuleExprComparison() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprComparison = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2114:2: (iv_ruleExprComparison= ruleExprComparison EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2115:2: iv_ruleExprComparison= ruleExprComparison EOF
            {
             currentNode = createCompositeNode(grammarAccess.getExprComparisonRule(), currentNode); 
            pushFollow(FOLLOW_ruleExprComparison_in_entryRuleExprComparison3844);
            iv_ruleExprComparison=ruleExprComparison();
            _fsp--;

             current =iv_ruleExprComparison; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprComparison3854); 

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
    // $ANTLR end entryRuleExprComparison


    // $ANTLR start ruleExprComparison
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2122:1: ruleExprComparison returns [EObject current=null] : (this_ExprAdditive_0= ruleExprAdditive ( () ( ( (lv_op_2_1= '<=' | lv_op_2_2= '<' | lv_op_2_3= '>=' | lv_op_2_4= '>' ) ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )* ) ;
    public final EObject ruleExprComparison() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_1=null;
        Token lv_op_2_2=null;
        Token lv_op_2_3=null;
        Token lv_op_2_4=null;
        EObject this_ExprAdditive_0 = null;

        EObject lv_right_3_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2127:6: ( (this_ExprAdditive_0= ruleExprAdditive ( () ( ( (lv_op_2_1= '<=' | lv_op_2_2= '<' | lv_op_2_3= '>=' | lv_op_2_4= '>' ) ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )* ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2128:1: (this_ExprAdditive_0= ruleExprAdditive ( () ( ( (lv_op_2_1= '<=' | lv_op_2_2= '<' | lv_op_2_3= '>=' | lv_op_2_4= '>' ) ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )* )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2128:1: (this_ExprAdditive_0= ruleExprAdditive ( () ( ( (lv_op_2_1= '<=' | lv_op_2_2= '<' | lv_op_2_3= '>=' | lv_op_2_4= '>' ) ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )* )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2129:5: this_ExprAdditive_0= ruleExprAdditive ( () ( ( (lv_op_2_1= '<=' | lv_op_2_2= '<' | lv_op_2_3= '>=' | lv_op_2_4= '>' ) ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )*
            {
             
                    currentNode=createCompositeNode(grammarAccess.getExprComparisonAccess().getExprAdditiveParserRuleCall_0(), currentNode); 
                
            pushFollow(FOLLOW_ruleExprAdditive_in_ruleExprComparison3901);
            this_ExprAdditive_0=ruleExprAdditive();
            _fsp--;

             
                    current = this_ExprAdditive_0; 
                    currentNode = currentNode.getParent();
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2137:1: ( () ( ( (lv_op_2_1= '<=' | lv_op_2_2= '<' | lv_op_2_3= '>=' | lv_op_2_4= '>' ) ) ) ( (lv_right_3_0= ruleExprAdditive ) ) )*
            loop34:
            do {
                int alt34=2;
                switch ( input.LA(1) ) {
                case 41:
                    {
                    alt34=1;
                    }
                    break;
                case 42:
                    {
                    alt34=1;
                    }
                    break;
                case 43:
                    {
                    alt34=1;
                    }
                    break;
                case 44:
                    {
                    alt34=1;
                    }
                    break;

                }

                switch (alt34) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2137:2: () ( ( (lv_op_2_1= '<=' | lv_op_2_2= '<' | lv_op_2_3= '>=' | lv_op_2_4= '>' ) ) ) ( (lv_right_3_0= ruleExprAdditive ) )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2137:2: ()
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2138:5: 
            	    {
            	     
            	            temp=factory.create(grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0().getType().getClassifier());
            	            try {
            	            	factory.set(temp, "left", current, null /*ParserRule*/, currentNode);
            	            } catch(ValueConverterException vce) {
            	            	handleValueConverterException(vce);
            	            }
            	            current = temp; 
            	            temp = null;
            	            CompositeNode newNode = createCompositeNode(grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0(), currentNode.getParent());
            	        newNode.getChildren().add(currentNode);
            	        moveLookaheadInfo(currentNode, newNode);
            	        currentNode = newNode; 
            	            associateNodeWithAstElement(currentNode, current); 
            	        

            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2153:2: ( ( (lv_op_2_1= '<=' | lv_op_2_2= '<' | lv_op_2_3= '>=' | lv_op_2_4= '>' ) ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2154:1: ( (lv_op_2_1= '<=' | lv_op_2_2= '<' | lv_op_2_3= '>=' | lv_op_2_4= '>' ) )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2154:1: ( (lv_op_2_1= '<=' | lv_op_2_2= '<' | lv_op_2_3= '>=' | lv_op_2_4= '>' ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2155:1: (lv_op_2_1= '<=' | lv_op_2_2= '<' | lv_op_2_3= '>=' | lv_op_2_4= '>' )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2155:1: (lv_op_2_1= '<=' | lv_op_2_2= '<' | lv_op_2_3= '>=' | lv_op_2_4= '>' )
            	    int alt33=4;
            	    switch ( input.LA(1) ) {
            	    case 41:
            	        {
            	        alt33=1;
            	        }
            	        break;
            	    case 42:
            	        {
            	        alt33=2;
            	        }
            	        break;
            	    case 43:
            	        {
            	        alt33=3;
            	        }
            	        break;
            	    case 44:
            	        {
            	        alt33=4;
            	        }
            	        break;
            	    default:
            	        NoViableAltException nvae =
            	            new NoViableAltException("2155:1: (lv_op_2_1= '<=' | lv_op_2_2= '<' | lv_op_2_3= '>=' | lv_op_2_4= '>' )", 33, 0, input);

            	        throw nvae;
            	    }

            	    switch (alt33) {
            	        case 1 :
            	            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2156:3: lv_op_2_1= '<='
            	            {
            	            lv_op_2_1=(Token)input.LT(1);
            	            match(input,41,FOLLOW_41_in_ruleExprComparison3930); 

            	                    createLeafNode(grammarAccess.getExprComparisonAccess().getOpLessThanSignEqualsSignKeyword_1_1_0_0(), "op"); 
            	                

            	            	        if (current==null) {
            	            	            current = factory.create(grammarAccess.getExprComparisonRule().getType().getClassifier());
            	            	            associateNodeWithAstElement(currentNode, current);
            	            	        }
            	            	        
            	            	        try {
            	            	       		set(current, "op", lv_op_2_1, null, lastConsumedNode);
            	            	        } catch (ValueConverterException vce) {
            	            				handleValueConverterException(vce);
            	            	        }
            	            	    

            	            }
            	            break;
            	        case 2 :
            	            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2174:8: lv_op_2_2= '<'
            	            {
            	            lv_op_2_2=(Token)input.LT(1);
            	            match(input,42,FOLLOW_42_in_ruleExprComparison3959); 

            	                    createLeafNode(grammarAccess.getExprComparisonAccess().getOpLessThanSignKeyword_1_1_0_1(), "op"); 
            	                

            	            	        if (current==null) {
            	            	            current = factory.create(grammarAccess.getExprComparisonRule().getType().getClassifier());
            	            	            associateNodeWithAstElement(currentNode, current);
            	            	        }
            	            	        
            	            	        try {
            	            	       		set(current, "op", lv_op_2_2, null, lastConsumedNode);
            	            	        } catch (ValueConverterException vce) {
            	            				handleValueConverterException(vce);
            	            	        }
            	            	    

            	            }
            	            break;
            	        case 3 :
            	            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2192:8: lv_op_2_3= '>='
            	            {
            	            lv_op_2_3=(Token)input.LT(1);
            	            match(input,43,FOLLOW_43_in_ruleExprComparison3988); 

            	                    createLeafNode(grammarAccess.getExprComparisonAccess().getOpGreaterThanSignEqualsSignKeyword_1_1_0_2(), "op"); 
            	                

            	            	        if (current==null) {
            	            	            current = factory.create(grammarAccess.getExprComparisonRule().getType().getClassifier());
            	            	            associateNodeWithAstElement(currentNode, current);
            	            	        }
            	            	        
            	            	        try {
            	            	       		set(current, "op", lv_op_2_3, null, lastConsumedNode);
            	            	        } catch (ValueConverterException vce) {
            	            				handleValueConverterException(vce);
            	            	        }
            	            	    

            	            }
            	            break;
            	        case 4 :
            	            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2210:8: lv_op_2_4= '>'
            	            {
            	            lv_op_2_4=(Token)input.LT(1);
            	            match(input,44,FOLLOW_44_in_ruleExprComparison4017); 

            	                    createLeafNode(grammarAccess.getExprComparisonAccess().getOpGreaterThanSignKeyword_1_1_0_3(), "op"); 
            	                

            	            	        if (current==null) {
            	            	            current = factory.create(grammarAccess.getExprComparisonRule().getType().getClassifier());
            	            	            associateNodeWithAstElement(currentNode, current);
            	            	        }
            	            	        
            	            	        try {
            	            	       		set(current, "op", lv_op_2_4, null, lastConsumedNode);
            	            	        } catch (ValueConverterException vce) {
            	            				handleValueConverterException(vce);
            	            	        }
            	            	    

            	            }
            	            break;

            	    }


            	    }


            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2231:2: ( (lv_right_3_0= ruleExprAdditive ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2232:1: (lv_right_3_0= ruleExprAdditive )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2232:1: (lv_right_3_0= ruleExprAdditive )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2233:3: lv_right_3_0= ruleExprAdditive
            	    {
            	     
            	    	        currentNode=createCompositeNode(grammarAccess.getExprComparisonAccess().getRightExprAdditiveParserRuleCall_1_2_0(), currentNode); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprAdditive_in_ruleExprComparison4054);
            	    lv_right_3_0=ruleExprAdditive();
            	    _fsp--;


            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getExprComparisonRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode.getParent(), current);
            	    	        }
            	    	        try {
            	    	       		set(
            	    	       			current, 
            	    	       			"right",
            	    	        		lv_right_3_0, 
            	    	        		"ExprAdditive", 
            	    	        		currentNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	        currentNode = currentNode.getParent();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop34;
                }
            } while (true);


            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleExprComparison


    // $ANTLR start entryRuleExprAdditive
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2263:1: entryRuleExprAdditive returns [EObject current=null] : iv_ruleExprAdditive= ruleExprAdditive EOF ;
    public final EObject entryRuleExprAdditive() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprAdditive = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2264:2: (iv_ruleExprAdditive= ruleExprAdditive EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2265:2: iv_ruleExprAdditive= ruleExprAdditive EOF
            {
             currentNode = createCompositeNode(grammarAccess.getExprAdditiveRule(), currentNode); 
            pushFollow(FOLLOW_ruleExprAdditive_in_entryRuleExprAdditive4092);
            iv_ruleExprAdditive=ruleExprAdditive();
            _fsp--;

             current =iv_ruleExprAdditive; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprAdditive4102); 

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
    // $ANTLR end entryRuleExprAdditive


    // $ANTLR start ruleExprAdditive
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2272:1: ruleExprAdditive returns [EObject current=null] : (this_ExprMult_0= ruleExprMult ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ( (lv_right_3_0= ruleExprMult ) ) )* ) ;
    public final EObject ruleExprAdditive() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_1=null;
        Token lv_op_2_2=null;
        EObject this_ExprMult_0 = null;

        EObject lv_right_3_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2277:6: ( (this_ExprMult_0= ruleExprMult ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ( (lv_right_3_0= ruleExprMult ) ) )* ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2278:1: (this_ExprMult_0= ruleExprMult ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ( (lv_right_3_0= ruleExprMult ) ) )* )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2278:1: (this_ExprMult_0= ruleExprMult ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ( (lv_right_3_0= ruleExprMult ) ) )* )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2279:5: this_ExprMult_0= ruleExprMult ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ( (lv_right_3_0= ruleExprMult ) ) )*
            {
             
                    currentNode=createCompositeNode(grammarAccess.getExprAdditiveAccess().getExprMultParserRuleCall_0(), currentNode); 
                
            pushFollow(FOLLOW_ruleExprMult_in_ruleExprAdditive4149);
            this_ExprMult_0=ruleExprMult();
            _fsp--;

             
                    current = this_ExprMult_0; 
                    currentNode = currentNode.getParent();
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2287:1: ( () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ( (lv_right_3_0= ruleExprMult ) ) )*
            loop36:
            do {
                int alt36=2;
                int LA36_0 = input.LA(1);

                if ( (LA36_0==45) ) {
                    alt36=1;
                }
                else if ( (LA36_0==46) ) {
                    alt36=1;
                }


                switch (alt36) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2287:2: () ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) ) ( (lv_right_3_0= ruleExprMult ) )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2287:2: ()
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2288:5: 
            	    {
            	     
            	            temp=factory.create(grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0().getType().getClassifier());
            	            try {
            	            	factory.set(temp, "left", current, null /*ParserRule*/, currentNode);
            	            } catch(ValueConverterException vce) {
            	            	handleValueConverterException(vce);
            	            }
            	            current = temp; 
            	            temp = null;
            	            CompositeNode newNode = createCompositeNode(grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0(), currentNode.getParent());
            	        newNode.getChildren().add(currentNode);
            	        moveLookaheadInfo(currentNode, newNode);
            	        currentNode = newNode; 
            	            associateNodeWithAstElement(currentNode, current); 
            	        

            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2303:2: ( ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2304:1: ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2304:1: ( (lv_op_2_1= '+' | lv_op_2_2= '-' ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2305:1: (lv_op_2_1= '+' | lv_op_2_2= '-' )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2305:1: (lv_op_2_1= '+' | lv_op_2_2= '-' )
            	    int alt35=2;
            	    int LA35_0 = input.LA(1);

            	    if ( (LA35_0==45) ) {
            	        alt35=1;
            	    }
            	    else if ( (LA35_0==46) ) {
            	        alt35=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("2305:1: (lv_op_2_1= '+' | lv_op_2_2= '-' )", 35, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt35) {
            	        case 1 :
            	            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2306:3: lv_op_2_1= '+'
            	            {
            	            lv_op_2_1=(Token)input.LT(1);
            	            match(input,45,FOLLOW_45_in_ruleExprAdditive4178); 

            	                    createLeafNode(grammarAccess.getExprAdditiveAccess().getOpPlusSignKeyword_1_1_0_0(), "op"); 
            	                

            	            	        if (current==null) {
            	            	            current = factory.create(grammarAccess.getExprAdditiveRule().getType().getClassifier());
            	            	            associateNodeWithAstElement(currentNode, current);
            	            	        }
            	            	        
            	            	        try {
            	            	       		set(current, "op", lv_op_2_1, null, lastConsumedNode);
            	            	        } catch (ValueConverterException vce) {
            	            				handleValueConverterException(vce);
            	            	        }
            	            	    

            	            }
            	            break;
            	        case 2 :
            	            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2324:8: lv_op_2_2= '-'
            	            {
            	            lv_op_2_2=(Token)input.LT(1);
            	            match(input,46,FOLLOW_46_in_ruleExprAdditive4207); 

            	                    createLeafNode(grammarAccess.getExprAdditiveAccess().getOpHyphenMinusKeyword_1_1_0_1(), "op"); 
            	                

            	            	        if (current==null) {
            	            	            current = factory.create(grammarAccess.getExprAdditiveRule().getType().getClassifier());
            	            	            associateNodeWithAstElement(currentNode, current);
            	            	        }
            	            	        
            	            	        try {
            	            	       		set(current, "op", lv_op_2_2, null, lastConsumedNode);
            	            	        } catch (ValueConverterException vce) {
            	            				handleValueConverterException(vce);
            	            	        }
            	            	    

            	            }
            	            break;

            	    }


            	    }


            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2345:2: ( (lv_right_3_0= ruleExprMult ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2346:1: (lv_right_3_0= ruleExprMult )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2346:1: (lv_right_3_0= ruleExprMult )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2347:3: lv_right_3_0= ruleExprMult
            	    {
            	     
            	    	        currentNode=createCompositeNode(grammarAccess.getExprAdditiveAccess().getRightExprMultParserRuleCall_1_2_0(), currentNode); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprMult_in_ruleExprAdditive4244);
            	    lv_right_3_0=ruleExprMult();
            	    _fsp--;


            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getExprAdditiveRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode.getParent(), current);
            	    	        }
            	    	        try {
            	    	       		set(
            	    	       			current, 
            	    	       			"right",
            	    	        		lv_right_3_0, 
            	    	        		"ExprMult", 
            	    	        		currentNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	        currentNode = currentNode.getParent();
            	    	    

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

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleExprAdditive


    // $ANTLR start entryRuleExprMult
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2377:1: entryRuleExprMult returns [EObject current=null] : iv_ruleExprMult= ruleExprMult EOF ;
    public final EObject entryRuleExprMult() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprMult = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2378:2: (iv_ruleExprMult= ruleExprMult EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2379:2: iv_ruleExprMult= ruleExprMult EOF
            {
             currentNode = createCompositeNode(grammarAccess.getExprMultRule(), currentNode); 
            pushFollow(FOLLOW_ruleExprMult_in_entryRuleExprMult4282);
            iv_ruleExprMult=ruleExprMult();
            _fsp--;

             current =iv_ruleExprMult; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprMult4292); 

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
    // $ANTLR end entryRuleExprMult


    // $ANTLR start ruleExprMult
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2386:1: ruleExprMult returns [EObject current=null] : (this_ExprSign_0= ruleExprSign ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '%' | lv_op_2_4= 'mod' | lv_op_2_5= 'div' ) ) ) ( (lv_right_3_0= ruleExprSign ) ) )* ) ;
    public final EObject ruleExprMult() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_1=null;
        Token lv_op_2_2=null;
        Token lv_op_2_3=null;
        Token lv_op_2_4=null;
        Token lv_op_2_5=null;
        EObject this_ExprSign_0 = null;

        EObject lv_right_3_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2391:6: ( (this_ExprSign_0= ruleExprSign ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '%' | lv_op_2_4= 'mod' | lv_op_2_5= 'div' ) ) ) ( (lv_right_3_0= ruleExprSign ) ) )* ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2392:1: (this_ExprSign_0= ruleExprSign ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '%' | lv_op_2_4= 'mod' | lv_op_2_5= 'div' ) ) ) ( (lv_right_3_0= ruleExprSign ) ) )* )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2392:1: (this_ExprSign_0= ruleExprSign ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '%' | lv_op_2_4= 'mod' | lv_op_2_5= 'div' ) ) ) ( (lv_right_3_0= ruleExprSign ) ) )* )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2393:5: this_ExprSign_0= ruleExprSign ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '%' | lv_op_2_4= 'mod' | lv_op_2_5= 'div' ) ) ) ( (lv_right_3_0= ruleExprSign ) ) )*
            {
             
                    currentNode=createCompositeNode(grammarAccess.getExprMultAccess().getExprSignParserRuleCall_0(), currentNode); 
                
            pushFollow(FOLLOW_ruleExprSign_in_ruleExprMult4339);
            this_ExprSign_0=ruleExprSign();
            _fsp--;

             
                    current = this_ExprSign_0; 
                    currentNode = currentNode.getParent();
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2401:1: ( () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '%' | lv_op_2_4= 'mod' | lv_op_2_5= 'div' ) ) ) ( (lv_right_3_0= ruleExprSign ) ) )*
            loop38:
            do {
                int alt38=2;
                switch ( input.LA(1) ) {
                case 47:
                    {
                    alt38=1;
                    }
                    break;
                case 48:
                    {
                    alt38=1;
                    }
                    break;
                case 49:
                    {
                    alt38=1;
                    }
                    break;
                case 50:
                    {
                    alt38=1;
                    }
                    break;
                case 51:
                    {
                    alt38=1;
                    }
                    break;

                }

                switch (alt38) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2401:2: () ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '%' | lv_op_2_4= 'mod' | lv_op_2_5= 'div' ) ) ) ( (lv_right_3_0= ruleExprSign ) )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2401:2: ()
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2402:5: 
            	    {
            	     
            	            temp=factory.create(grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0().getType().getClassifier());
            	            try {
            	            	factory.set(temp, "left", current, null /*ParserRule*/, currentNode);
            	            } catch(ValueConverterException vce) {
            	            	handleValueConverterException(vce);
            	            }
            	            current = temp; 
            	            temp = null;
            	            CompositeNode newNode = createCompositeNode(grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0(), currentNode.getParent());
            	        newNode.getChildren().add(currentNode);
            	        moveLookaheadInfo(currentNode, newNode);
            	        currentNode = newNode; 
            	            associateNodeWithAstElement(currentNode, current); 
            	        

            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2417:2: ( ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '%' | lv_op_2_4= 'mod' | lv_op_2_5= 'div' ) ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2418:1: ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '%' | lv_op_2_4= 'mod' | lv_op_2_5= 'div' ) )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2418:1: ( (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '%' | lv_op_2_4= 'mod' | lv_op_2_5= 'div' ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2419:1: (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '%' | lv_op_2_4= 'mod' | lv_op_2_5= 'div' )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2419:1: (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '%' | lv_op_2_4= 'mod' | lv_op_2_5= 'div' )
            	    int alt37=5;
            	    switch ( input.LA(1) ) {
            	    case 47:
            	        {
            	        alt37=1;
            	        }
            	        break;
            	    case 48:
            	        {
            	        alt37=2;
            	        }
            	        break;
            	    case 49:
            	        {
            	        alt37=3;
            	        }
            	        break;
            	    case 50:
            	        {
            	        alt37=4;
            	        }
            	        break;
            	    case 51:
            	        {
            	        alt37=5;
            	        }
            	        break;
            	    default:
            	        NoViableAltException nvae =
            	            new NoViableAltException("2419:1: (lv_op_2_1= '*' | lv_op_2_2= '/' | lv_op_2_3= '%' | lv_op_2_4= 'mod' | lv_op_2_5= 'div' )", 37, 0, input);

            	        throw nvae;
            	    }

            	    switch (alt37) {
            	        case 1 :
            	            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2420:3: lv_op_2_1= '*'
            	            {
            	            lv_op_2_1=(Token)input.LT(1);
            	            match(input,47,FOLLOW_47_in_ruleExprMult4368); 

            	                    createLeafNode(grammarAccess.getExprMultAccess().getOpAsteriskKeyword_1_1_0_0(), "op"); 
            	                

            	            	        if (current==null) {
            	            	            current = factory.create(grammarAccess.getExprMultRule().getType().getClassifier());
            	            	            associateNodeWithAstElement(currentNode, current);
            	            	        }
            	            	        
            	            	        try {
            	            	       		set(current, "op", lv_op_2_1, null, lastConsumedNode);
            	            	        } catch (ValueConverterException vce) {
            	            				handleValueConverterException(vce);
            	            	        }
            	            	    

            	            }
            	            break;
            	        case 2 :
            	            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2438:8: lv_op_2_2= '/'
            	            {
            	            lv_op_2_2=(Token)input.LT(1);
            	            match(input,48,FOLLOW_48_in_ruleExprMult4397); 

            	                    createLeafNode(grammarAccess.getExprMultAccess().getOpSolidusKeyword_1_1_0_1(), "op"); 
            	                

            	            	        if (current==null) {
            	            	            current = factory.create(grammarAccess.getExprMultRule().getType().getClassifier());
            	            	            associateNodeWithAstElement(currentNode, current);
            	            	        }
            	            	        
            	            	        try {
            	            	       		set(current, "op", lv_op_2_2, null, lastConsumedNode);
            	            	        } catch (ValueConverterException vce) {
            	            				handleValueConverterException(vce);
            	            	        }
            	            	    

            	            }
            	            break;
            	        case 3 :
            	            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2456:8: lv_op_2_3= '%'
            	            {
            	            lv_op_2_3=(Token)input.LT(1);
            	            match(input,49,FOLLOW_49_in_ruleExprMult4426); 

            	                    createLeafNode(grammarAccess.getExprMultAccess().getOpPercentSignKeyword_1_1_0_2(), "op"); 
            	                

            	            	        if (current==null) {
            	            	            current = factory.create(grammarAccess.getExprMultRule().getType().getClassifier());
            	            	            associateNodeWithAstElement(currentNode, current);
            	            	        }
            	            	        
            	            	        try {
            	            	       		set(current, "op", lv_op_2_3, null, lastConsumedNode);
            	            	        } catch (ValueConverterException vce) {
            	            				handleValueConverterException(vce);
            	            	        }
            	            	    

            	            }
            	            break;
            	        case 4 :
            	            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2474:8: lv_op_2_4= 'mod'
            	            {
            	            lv_op_2_4=(Token)input.LT(1);
            	            match(input,50,FOLLOW_50_in_ruleExprMult4455); 

            	                    createLeafNode(grammarAccess.getExprMultAccess().getOpModKeyword_1_1_0_3(), "op"); 
            	                

            	            	        if (current==null) {
            	            	            current = factory.create(grammarAccess.getExprMultRule().getType().getClassifier());
            	            	            associateNodeWithAstElement(currentNode, current);
            	            	        }
            	            	        
            	            	        try {
            	            	       		set(current, "op", lv_op_2_4, null, lastConsumedNode);
            	            	        } catch (ValueConverterException vce) {
            	            				handleValueConverterException(vce);
            	            	        }
            	            	    

            	            }
            	            break;
            	        case 5 :
            	            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2492:8: lv_op_2_5= 'div'
            	            {
            	            lv_op_2_5=(Token)input.LT(1);
            	            match(input,51,FOLLOW_51_in_ruleExprMult4484); 

            	                    createLeafNode(grammarAccess.getExprMultAccess().getOpDivKeyword_1_1_0_4(), "op"); 
            	                

            	            	        if (current==null) {
            	            	            current = factory.create(grammarAccess.getExprMultRule().getType().getClassifier());
            	            	            associateNodeWithAstElement(currentNode, current);
            	            	        }
            	            	        
            	            	        try {
            	            	       		set(current, "op", lv_op_2_5, null, lastConsumedNode);
            	            	        } catch (ValueConverterException vce) {
            	            				handleValueConverterException(vce);
            	            	        }
            	            	    

            	            }
            	            break;

            	    }


            	    }


            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2513:2: ( (lv_right_3_0= ruleExprSign ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2514:1: (lv_right_3_0= ruleExprSign )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2514:1: (lv_right_3_0= ruleExprSign )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2515:3: lv_right_3_0= ruleExprSign
            	    {
            	     
            	    	        currentNode=createCompositeNode(grammarAccess.getExprMultAccess().getRightExprSignParserRuleCall_1_2_0(), currentNode); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprSign_in_ruleExprMult4521);
            	    lv_right_3_0=ruleExprSign();
            	    _fsp--;


            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getExprMultRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode.getParent(), current);
            	    	        }
            	    	        try {
            	    	       		set(
            	    	       			current, 
            	    	       			"right",
            	    	        		lv_right_3_0, 
            	    	        		"ExprSign", 
            	    	        		currentNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	        currentNode = currentNode.getParent();
            	    	    

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

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleExprMult


    // $ANTLR start entryRuleExprSign
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2545:1: entryRuleExprSign returns [EObject current=null] : iv_ruleExprSign= ruleExprSign EOF ;
    public final EObject entryRuleExprSign() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprSign = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2546:2: (iv_ruleExprSign= ruleExprSign EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2547:2: iv_ruleExprSign= ruleExprSign EOF
            {
             currentNode = createCompositeNode(grammarAccess.getExprSignRule(), currentNode); 
            pushFollow(FOLLOW_ruleExprSign_in_entryRuleExprSign4559);
            iv_ruleExprSign=ruleExprSign();
            _fsp--;

             current =iv_ruleExprSign; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprSign4569); 

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
    // $ANTLR end entryRuleExprSign


    // $ANTLR start ruleExprSign
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2554:1: ruleExprSign returns [EObject current=null] : ( ( () ( ( (lv_op_1_1= '+' | lv_op_1_2= '-' ) ) ) ( (lv_right_2_0= ruleExprNot ) ) ) | this_ExprNot_3= ruleExprNot ) ;
    public final EObject ruleExprSign() throws RecognitionException {
        EObject current = null;

        Token lv_op_1_1=null;
        Token lv_op_1_2=null;
        EObject lv_right_2_0 = null;

        EObject this_ExprNot_3 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2559:6: ( ( ( () ( ( (lv_op_1_1= '+' | lv_op_1_2= '-' ) ) ) ( (lv_right_2_0= ruleExprNot ) ) ) | this_ExprNot_3= ruleExprNot ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2560:1: ( ( () ( ( (lv_op_1_1= '+' | lv_op_1_2= '-' ) ) ) ( (lv_right_2_0= ruleExprNot ) ) ) | this_ExprNot_3= ruleExprNot )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2560:1: ( ( () ( ( (lv_op_1_1= '+' | lv_op_1_2= '-' ) ) ) ( (lv_right_2_0= ruleExprNot ) ) ) | this_ExprNot_3= ruleExprNot )
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( ((LA40_0>=45 && LA40_0<=46)) ) {
                alt40=1;
            }
            else if ( (LA40_0==RULE_ID||(LA40_0>=RULE_INT && LA40_0<=RULE_STRING)||LA40_0==28||(LA40_0>=52 && LA40_0<=53)) ) {
                alt40=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("2560:1: ( ( () ( ( (lv_op_1_1= '+' | lv_op_1_2= '-' ) ) ) ( (lv_right_2_0= ruleExprNot ) ) ) | this_ExprNot_3= ruleExprNot )", 40, 0, input);

                throw nvae;
            }
            switch (alt40) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2560:2: ( () ( ( (lv_op_1_1= '+' | lv_op_1_2= '-' ) ) ) ( (lv_right_2_0= ruleExprNot ) ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2560:2: ( () ( ( (lv_op_1_1= '+' | lv_op_1_2= '-' ) ) ) ( (lv_right_2_0= ruleExprNot ) ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2560:3: () ( ( (lv_op_1_1= '+' | lv_op_1_2= '-' ) ) ) ( (lv_right_2_0= ruleExprNot ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2560:3: ()
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2561:5: 
                    {
                     
                            temp=factory.create(grammarAccess.getExprSignAccess().getExprSignAction_0_0().getType().getClassifier());
                            current = temp; 
                            temp = null;
                            CompositeNode newNode = createCompositeNode(grammarAccess.getExprSignAccess().getExprSignAction_0_0(), currentNode.getParent());
                        newNode.getChildren().add(currentNode);
                        moveLookaheadInfo(currentNode, newNode);
                        currentNode = newNode; 
                            associateNodeWithAstElement(currentNode, current); 
                        

                    }

                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2571:2: ( ( (lv_op_1_1= '+' | lv_op_1_2= '-' ) ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2572:1: ( (lv_op_1_1= '+' | lv_op_1_2= '-' ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2572:1: ( (lv_op_1_1= '+' | lv_op_1_2= '-' ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2573:1: (lv_op_1_1= '+' | lv_op_1_2= '-' )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2573:1: (lv_op_1_1= '+' | lv_op_1_2= '-' )
                    int alt39=2;
                    int LA39_0 = input.LA(1);

                    if ( (LA39_0==45) ) {
                        alt39=1;
                    }
                    else if ( (LA39_0==46) ) {
                        alt39=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("2573:1: (lv_op_1_1= '+' | lv_op_1_2= '-' )", 39, 0, input);

                        throw nvae;
                    }
                    switch (alt39) {
                        case 1 :
                            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2574:3: lv_op_1_1= '+'
                            {
                            lv_op_1_1=(Token)input.LT(1);
                            match(input,45,FOLLOW_45_in_ruleExprSign4624); 

                                    createLeafNode(grammarAccess.getExprSignAccess().getOpPlusSignKeyword_0_1_0_0(), "op"); 
                                

                            	        if (current==null) {
                            	            current = factory.create(grammarAccess.getExprSignRule().getType().getClassifier());
                            	            associateNodeWithAstElement(currentNode, current);
                            	        }
                            	        
                            	        try {
                            	       		set(current, "op", lv_op_1_1, null, lastConsumedNode);
                            	        } catch (ValueConverterException vce) {
                            				handleValueConverterException(vce);
                            	        }
                            	    

                            }
                            break;
                        case 2 :
                            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2592:8: lv_op_1_2= '-'
                            {
                            lv_op_1_2=(Token)input.LT(1);
                            match(input,46,FOLLOW_46_in_ruleExprSign4653); 

                                    createLeafNode(grammarAccess.getExprSignAccess().getOpHyphenMinusKeyword_0_1_0_1(), "op"); 
                                

                            	        if (current==null) {
                            	            current = factory.create(grammarAccess.getExprSignRule().getType().getClassifier());
                            	            associateNodeWithAstElement(currentNode, current);
                            	        }
                            	        
                            	        try {
                            	       		set(current, "op", lv_op_1_2, null, lastConsumedNode);
                            	        } catch (ValueConverterException vce) {
                            				handleValueConverterException(vce);
                            	        }
                            	    

                            }
                            break;

                    }


                    }


                    }

                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2613:2: ( (lv_right_2_0= ruleExprNot ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2614:1: (lv_right_2_0= ruleExprNot )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2614:1: (lv_right_2_0= ruleExprNot )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2615:3: lv_right_2_0= ruleExprNot
                    {
                     
                    	        currentNode=createCompositeNode(grammarAccess.getExprSignAccess().getRightExprNotParserRuleCall_0_2_0(), currentNode); 
                    	    
                    pushFollow(FOLLOW_ruleExprNot_in_ruleExprSign4690);
                    lv_right_2_0=ruleExprNot();
                    _fsp--;


                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getExprSignRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode.getParent(), current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"right",
                    	        		lv_right_2_0, 
                    	        		"ExprNot", 
                    	        		currentNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	        currentNode = currentNode.getParent();
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2639:5: this_ExprNot_3= ruleExprNot
                    {
                     
                            currentNode=createCompositeNode(grammarAccess.getExprSignAccess().getExprNotParserRuleCall_1(), currentNode); 
                        
                    pushFollow(FOLLOW_ruleExprNot_in_ruleExprSign4719);
                    this_ExprNot_3=ruleExprNot();
                    _fsp--;

                     
                            current = this_ExprNot_3; 
                            currentNode = currentNode.getParent();
                        

                    }
                    break;

            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleExprSign


    // $ANTLR start entryRuleExprNot
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2655:1: entryRuleExprNot returns [EObject current=null] : iv_ruleExprNot= ruleExprNot EOF ;
    public final EObject entryRuleExprNot() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprNot = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2656:2: (iv_ruleExprNot= ruleExprNot EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2657:2: iv_ruleExprNot= ruleExprNot EOF
            {
             currentNode = createCompositeNode(grammarAccess.getExprNotRule(), currentNode); 
            pushFollow(FOLLOW_ruleExprNot_in_entryRuleExprNot4754);
            iv_ruleExprNot=ruleExprNot();
            _fsp--;

             current =iv_ruleExprNot; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprNot4764); 

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
    // $ANTLR end entryRuleExprNot


    // $ANTLR start ruleExprNot
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2664:1: ruleExprNot returns [EObject current=null] : ( ( () ( (lv_op_1_0= 'not' ) ) ( (lv_right_2_0= ruleExprCustomOperator ) ) ) | this_ExprCustomOperator_3= ruleExprCustomOperator ) ;
    public final EObject ruleExprNot() throws RecognitionException {
        EObject current = null;

        Token lv_op_1_0=null;
        EObject lv_right_2_0 = null;

        EObject this_ExprCustomOperator_3 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2669:6: ( ( ( () ( (lv_op_1_0= 'not' ) ) ( (lv_right_2_0= ruleExprCustomOperator ) ) ) | this_ExprCustomOperator_3= ruleExprCustomOperator ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2670:1: ( ( () ( (lv_op_1_0= 'not' ) ) ( (lv_right_2_0= ruleExprCustomOperator ) ) ) | this_ExprCustomOperator_3= ruleExprCustomOperator )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2670:1: ( ( () ( (lv_op_1_0= 'not' ) ) ( (lv_right_2_0= ruleExprCustomOperator ) ) ) | this_ExprCustomOperator_3= ruleExprCustomOperator )
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==52) ) {
                alt41=1;
            }
            else if ( (LA41_0==RULE_ID||(LA41_0>=RULE_INT && LA41_0<=RULE_STRING)||LA41_0==28||LA41_0==53) ) {
                alt41=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("2670:1: ( ( () ( (lv_op_1_0= 'not' ) ) ( (lv_right_2_0= ruleExprCustomOperator ) ) ) | this_ExprCustomOperator_3= ruleExprCustomOperator )", 41, 0, input);

                throw nvae;
            }
            switch (alt41) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2670:2: ( () ( (lv_op_1_0= 'not' ) ) ( (lv_right_2_0= ruleExprCustomOperator ) ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2670:2: ( () ( (lv_op_1_0= 'not' ) ) ( (lv_right_2_0= ruleExprCustomOperator ) ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2670:3: () ( (lv_op_1_0= 'not' ) ) ( (lv_right_2_0= ruleExprCustomOperator ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2670:3: ()
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2671:5: 
                    {
                     
                            temp=factory.create(grammarAccess.getExprNotAccess().getExprNotAction_0_0().getType().getClassifier());
                            current = temp; 
                            temp = null;
                            CompositeNode newNode = createCompositeNode(grammarAccess.getExprNotAccess().getExprNotAction_0_0(), currentNode.getParent());
                        newNode.getChildren().add(currentNode);
                        moveLookaheadInfo(currentNode, newNode);
                        currentNode = newNode; 
                            associateNodeWithAstElement(currentNode, current); 
                        

                    }

                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2681:2: ( (lv_op_1_0= 'not' ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2682:1: (lv_op_1_0= 'not' )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2682:1: (lv_op_1_0= 'not' )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2683:3: lv_op_1_0= 'not'
                    {
                    lv_op_1_0=(Token)input.LT(1);
                    match(input,52,FOLLOW_52_in_ruleExprNot4817); 

                            createLeafNode(grammarAccess.getExprNotAccess().getOpNotKeyword_0_1_0(), "op"); 
                        

                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getExprNotRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode, current);
                    	        }
                    	        
                    	        try {
                    	       		set(current, "op", lv_op_1_0, "not", lastConsumedNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2702:2: ( (lv_right_2_0= ruleExprCustomOperator ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2703:1: (lv_right_2_0= ruleExprCustomOperator )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2703:1: (lv_right_2_0= ruleExprCustomOperator )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2704:3: lv_right_2_0= ruleExprCustomOperator
                    {
                     
                    	        currentNode=createCompositeNode(grammarAccess.getExprNotAccess().getRightExprCustomOperatorParserRuleCall_0_2_0(), currentNode); 
                    	    
                    pushFollow(FOLLOW_ruleExprCustomOperator_in_ruleExprNot4851);
                    lv_right_2_0=ruleExprCustomOperator();
                    _fsp--;


                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getExprNotRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode.getParent(), current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"right",
                    	        		lv_right_2_0, 
                    	        		"ExprCustomOperator", 
                    	        		currentNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	        currentNode = currentNode.getParent();
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2728:5: this_ExprCustomOperator_3= ruleExprCustomOperator
                    {
                     
                            currentNode=createCompositeNode(grammarAccess.getExprNotAccess().getExprCustomOperatorParserRuleCall_1(), currentNode); 
                        
                    pushFollow(FOLLOW_ruleExprCustomOperator_in_ruleExprNot4880);
                    this_ExprCustomOperator_3=ruleExprCustomOperator();
                    _fsp--;

                     
                            current = this_ExprCustomOperator_3; 
                            currentNode = currentNode.getParent();
                        

                    }
                    break;

            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleExprNot


    // $ANTLR start entryRuleExprCustomOperator
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2744:1: entryRuleExprCustomOperator returns [EObject current=null] : iv_ruleExprCustomOperator= ruleExprCustomOperator EOF ;
    public final EObject entryRuleExprCustomOperator() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprCustomOperator = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2745:2: (iv_ruleExprCustomOperator= ruleExprCustomOperator EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2746:2: iv_ruleExprCustomOperator= ruleExprCustomOperator EOF
            {
             currentNode = createCompositeNode(grammarAccess.getExprCustomOperatorRule(), currentNode); 
            pushFollow(FOLLOW_ruleExprCustomOperator_in_entryRuleExprCustomOperator4915);
            iv_ruleExprCustomOperator=ruleExprCustomOperator();
            _fsp--;

             current =iv_ruleExprCustomOperator; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprCustomOperator4925); 

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
    // $ANTLR end entryRuleExprCustomOperator


    // $ANTLR start ruleExprCustomOperator
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2753:1: ruleExprCustomOperator returns [EObject current=null] : (this_ExprMember_0= ruleExprMember ( () ( (lv_op_2_0= RULE_OPERATOR ) ) ( (lv_right_3_0= ruleExpr ) ) )* ) ;
    public final EObject ruleExprCustomOperator() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_ExprMember_0 = null;

        EObject lv_right_3_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2758:6: ( (this_ExprMember_0= ruleExprMember ( () ( (lv_op_2_0= RULE_OPERATOR ) ) ( (lv_right_3_0= ruleExpr ) ) )* ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2759:1: (this_ExprMember_0= ruleExprMember ( () ( (lv_op_2_0= RULE_OPERATOR ) ) ( (lv_right_3_0= ruleExpr ) ) )* )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2759:1: (this_ExprMember_0= ruleExprMember ( () ( (lv_op_2_0= RULE_OPERATOR ) ) ( (lv_right_3_0= ruleExpr ) ) )* )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2760:5: this_ExprMember_0= ruleExprMember ( () ( (lv_op_2_0= RULE_OPERATOR ) ) ( (lv_right_3_0= ruleExpr ) ) )*
            {
             
                    currentNode=createCompositeNode(grammarAccess.getExprCustomOperatorAccess().getExprMemberParserRuleCall_0(), currentNode); 
                
            pushFollow(FOLLOW_ruleExprMember_in_ruleExprCustomOperator4972);
            this_ExprMember_0=ruleExprMember();
            _fsp--;

             
                    current = this_ExprMember_0; 
                    currentNode = currentNode.getParent();
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2768:1: ( () ( (lv_op_2_0= RULE_OPERATOR ) ) ( (lv_right_3_0= ruleExpr ) ) )*
            loop42:
            do {
                int alt42=2;
                int LA42_0 = input.LA(1);

                if ( (LA42_0==RULE_OPERATOR) ) {
                    alt42=1;
                }


                switch (alt42) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2768:2: () ( (lv_op_2_0= RULE_OPERATOR ) ) ( (lv_right_3_0= ruleExpr ) )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2768:2: ()
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2769:5: 
            	    {
            	     
            	            temp=factory.create(grammarAccess.getExprCustomOperatorAccess().getExprCustomOperatorLeftAction_1_0().getType().getClassifier());
            	            try {
            	            	factory.set(temp, "left", current, null /*ParserRule*/, currentNode);
            	            } catch(ValueConverterException vce) {
            	            	handleValueConverterException(vce);
            	            }
            	            current = temp; 
            	            temp = null;
            	            CompositeNode newNode = createCompositeNode(grammarAccess.getExprCustomOperatorAccess().getExprCustomOperatorLeftAction_1_0(), currentNode.getParent());
            	        newNode.getChildren().add(currentNode);
            	        moveLookaheadInfo(currentNode, newNode);
            	        currentNode = newNode; 
            	            associateNodeWithAstElement(currentNode, current); 
            	        

            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2784:2: ( (lv_op_2_0= RULE_OPERATOR ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2785:1: (lv_op_2_0= RULE_OPERATOR )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2785:1: (lv_op_2_0= RULE_OPERATOR )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2786:3: lv_op_2_0= RULE_OPERATOR
            	    {
            	    lv_op_2_0=(Token)input.LT(1);
            	    match(input,RULE_OPERATOR,FOLLOW_RULE_OPERATOR_in_ruleExprCustomOperator4998); 

            	    			createLeafNode(grammarAccess.getExprCustomOperatorAccess().getOpOPERATORTerminalRuleCall_1_1_0(), "op"); 
            	    		

            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getExprCustomOperatorRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode, current);
            	    	        }
            	    	        try {
            	    	       		set(
            	    	       			current, 
            	    	       			"op",
            	    	        		lv_op_2_0, 
            	    	        		"OPERATOR", 
            	    	        		lastConsumedNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	    

            	    }


            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2808:2: ( (lv_right_3_0= ruleExpr ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2809:1: (lv_right_3_0= ruleExpr )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2809:1: (lv_right_3_0= ruleExpr )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2810:3: lv_right_3_0= ruleExpr
            	    {
            	     
            	    	        currentNode=createCompositeNode(grammarAccess.getExprCustomOperatorAccess().getRightExprParserRuleCall_1_2_0(), currentNode); 
            	    	    
            	    pushFollow(FOLLOW_ruleExpr_in_ruleExprCustomOperator5024);
            	    lv_right_3_0=ruleExpr();
            	    _fsp--;


            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getExprCustomOperatorRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode.getParent(), current);
            	    	        }
            	    	        try {
            	    	       		set(
            	    	       			current, 
            	    	       			"right",
            	    	        		lv_right_3_0, 
            	    	        		"Expr", 
            	    	        		currentNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	        currentNode = currentNode.getParent();
            	    	    

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

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleExprCustomOperator


    // $ANTLR start entryRuleExprMember
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2840:1: entryRuleExprMember returns [EObject current=null] : iv_ruleExprMember= ruleExprMember EOF ;
    public final EObject entryRuleExprMember() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprMember = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2841:2: (iv_ruleExprMember= ruleExprMember EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2842:2: iv_ruleExprMember= ruleExprMember EOF
            {
             currentNode = createCompositeNode(grammarAccess.getExprMemberRule(), currentNode); 
            pushFollow(FOLLOW_ruleExprMember_in_entryRuleExprMember5062);
            iv_ruleExprMember=ruleExprMember();
            _fsp--;

             current =iv_ruleExprMember; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprMember5072); 

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
    // $ANTLR end entryRuleExprMember


    // $ANTLR start ruleExprMember
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2849:1: ruleExprMember returns [EObject current=null] : (this_ExprAtomic_0= ruleExprAtomic ( () ( (lv_op_2_0= '.' ) ) ( (lv_right_3_0= ruleExprAtomic ) ) )* ) ;
    public final EObject ruleExprMember() throws RecognitionException {
        EObject current = null;

        Token lv_op_2_0=null;
        EObject this_ExprAtomic_0 = null;

        EObject lv_right_3_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2854:6: ( (this_ExprAtomic_0= ruleExprAtomic ( () ( (lv_op_2_0= '.' ) ) ( (lv_right_3_0= ruleExprAtomic ) ) )* ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2855:1: (this_ExprAtomic_0= ruleExprAtomic ( () ( (lv_op_2_0= '.' ) ) ( (lv_right_3_0= ruleExprAtomic ) ) )* )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2855:1: (this_ExprAtomic_0= ruleExprAtomic ( () ( (lv_op_2_0= '.' ) ) ( (lv_right_3_0= ruleExprAtomic ) ) )* )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2856:5: this_ExprAtomic_0= ruleExprAtomic ( () ( (lv_op_2_0= '.' ) ) ( (lv_right_3_0= ruleExprAtomic ) ) )*
            {
             
                    currentNode=createCompositeNode(grammarAccess.getExprMemberAccess().getExprAtomicParserRuleCall_0(), currentNode); 
                
            pushFollow(FOLLOW_ruleExprAtomic_in_ruleExprMember5119);
            this_ExprAtomic_0=ruleExprAtomic();
            _fsp--;

             
                    current = this_ExprAtomic_0; 
                    currentNode = currentNode.getParent();
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2864:1: ( () ( (lv_op_2_0= '.' ) ) ( (lv_right_3_0= ruleExprAtomic ) ) )*
            loop43:
            do {
                int alt43=2;
                int LA43_0 = input.LA(1);

                if ( (LA43_0==18) ) {
                    alt43=1;
                }


                switch (alt43) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2864:2: () ( (lv_op_2_0= '.' ) ) ( (lv_right_3_0= ruleExprAtomic ) )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2864:2: ()
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2865:5: 
            	    {
            	     
            	            temp=factory.create(grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0().getType().getClassifier());
            	            try {
            	            	factory.set(temp, "left", current, null /*ParserRule*/, currentNode);
            	            } catch(ValueConverterException vce) {
            	            	handleValueConverterException(vce);
            	            }
            	            current = temp; 
            	            temp = null;
            	            CompositeNode newNode = createCompositeNode(grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0(), currentNode.getParent());
            	        newNode.getChildren().add(currentNode);
            	        moveLookaheadInfo(currentNode, newNode);
            	        currentNode = newNode; 
            	            associateNodeWithAstElement(currentNode, current); 
            	        

            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2880:2: ( (lv_op_2_0= '.' ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2881:1: (lv_op_2_0= '.' )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2881:1: (lv_op_2_0= '.' )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2882:3: lv_op_2_0= '.'
            	    {
            	    lv_op_2_0=(Token)input.LT(1);
            	    match(input,18,FOLLOW_18_in_ruleExprMember5146); 

            	            createLeafNode(grammarAccess.getExprMemberAccess().getOpFullStopKeyword_1_1_0(), "op"); 
            	        

            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getExprMemberRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode, current);
            	    	        }
            	    	        
            	    	        try {
            	    	       		set(current, "op", lv_op_2_0, ".", lastConsumedNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	    

            	    }


            	    }

            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2901:2: ( (lv_right_3_0= ruleExprAtomic ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2902:1: (lv_right_3_0= ruleExprAtomic )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2902:1: (lv_right_3_0= ruleExprAtomic )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2903:3: lv_right_3_0= ruleExprAtomic
            	    {
            	     
            	    	        currentNode=createCompositeNode(grammarAccess.getExprMemberAccess().getRightExprAtomicParserRuleCall_1_2_0(), currentNode); 
            	    	    
            	    pushFollow(FOLLOW_ruleExprAtomic_in_ruleExprMember5180);
            	    lv_right_3_0=ruleExprAtomic();
            	    _fsp--;


            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getExprMemberRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode.getParent(), current);
            	    	        }
            	    	        try {
            	    	       		set(
            	    	       			current, 
            	    	       			"right",
            	    	        		lv_right_3_0, 
            	    	        		"ExprAtomic", 
            	    	        		currentNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	        currentNode = currentNode.getParent();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop43;
                }
            } while (true);


            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleExprMember


    // $ANTLR start entryRuleExprAtomic
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2933:1: entryRuleExprAtomic returns [EObject current=null] : iv_ruleExprAtomic= ruleExprAtomic EOF ;
    public final EObject entryRuleExprAtomic() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprAtomic = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2934:2: (iv_ruleExprAtomic= ruleExprAtomic EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2935:2: iv_ruleExprAtomic= ruleExprAtomic EOF
            {
             currentNode = createCompositeNode(grammarAccess.getExprAtomicRule(), currentNode); 
            pushFollow(FOLLOW_ruleExprAtomic_in_entryRuleExprAtomic5218);
            iv_ruleExprAtomic=ruleExprAtomic();
            _fsp--;

             current =iv_ruleExprAtomic; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprAtomic5228); 

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
    // $ANTLR end entryRuleExprAtomic


    // $ANTLR start ruleExprAtomic
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2942:1: ruleExprAtomic returns [EObject current=null] : ( ( () ( (lv_nameVal_1_0= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) ) ) | ( () ( (lv_nameVal_4_0= RULE_ID ) ) '(' ')' ) | ( () ( (lv_nameVal_8_0= RULE_ID ) ) ) | ( '(' this_Expr_10= ruleExpr ')' ) | ( () ( (lv_intVal_13_0= RULE_INT ) ) ) | ( () ( (lv_numVal_15_0= RULE_NUMBER ) ) ) | ( () ( (lv_strVal_17_0= RULE_STRING ) ) ) | ( () 'buildin' ( (lv_name_20_0= RULE_ID ) ) ( ( (lv_parameters_21_0= ruleExprList ) ) | ( '(' ')' ) ) ) | ( () 'buildin' '(' ( (lv_left_27_0= ruleExpr ) ) ( (lv_op_28_0= RULE_OPERATOR ) ) ( (lv_right_29_0= ruleExpr ) ) ')' ) ) ;
    public final EObject ruleExprAtomic() throws RecognitionException {
        EObject current = null;

        Token lv_nameVal_1_0=null;
        Token lv_nameVal_4_0=null;
        Token lv_nameVal_8_0=null;
        Token lv_intVal_13_0=null;
        Token lv_numVal_15_0=null;
        Token lv_strVal_17_0=null;
        Token lv_name_20_0=null;
        Token lv_op_28_0=null;
        EObject lv_parameters_2_0 = null;

        EObject this_Expr_10 = null;

        EObject lv_parameters_21_0 = null;

        EObject lv_left_27_0 = null;

        EObject lv_right_29_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2947:6: ( ( ( () ( (lv_nameVal_1_0= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) ) ) | ( () ( (lv_nameVal_4_0= RULE_ID ) ) '(' ')' ) | ( () ( (lv_nameVal_8_0= RULE_ID ) ) ) | ( '(' this_Expr_10= ruleExpr ')' ) | ( () ( (lv_intVal_13_0= RULE_INT ) ) ) | ( () ( (lv_numVal_15_0= RULE_NUMBER ) ) ) | ( () ( (lv_strVal_17_0= RULE_STRING ) ) ) | ( () 'buildin' ( (lv_name_20_0= RULE_ID ) ) ( ( (lv_parameters_21_0= ruleExprList ) ) | ( '(' ')' ) ) ) | ( () 'buildin' '(' ( (lv_left_27_0= ruleExpr ) ) ( (lv_op_28_0= RULE_OPERATOR ) ) ( (lv_right_29_0= ruleExpr ) ) ')' ) ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2948:1: ( ( () ( (lv_nameVal_1_0= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) ) ) | ( () ( (lv_nameVal_4_0= RULE_ID ) ) '(' ')' ) | ( () ( (lv_nameVal_8_0= RULE_ID ) ) ) | ( '(' this_Expr_10= ruleExpr ')' ) | ( () ( (lv_intVal_13_0= RULE_INT ) ) ) | ( () ( (lv_numVal_15_0= RULE_NUMBER ) ) ) | ( () ( (lv_strVal_17_0= RULE_STRING ) ) ) | ( () 'buildin' ( (lv_name_20_0= RULE_ID ) ) ( ( (lv_parameters_21_0= ruleExprList ) ) | ( '(' ')' ) ) ) | ( () 'buildin' '(' ( (lv_left_27_0= ruleExpr ) ) ( (lv_op_28_0= RULE_OPERATOR ) ) ( (lv_right_29_0= ruleExpr ) ) ')' ) )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2948:1: ( ( () ( (lv_nameVal_1_0= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) ) ) | ( () ( (lv_nameVal_4_0= RULE_ID ) ) '(' ')' ) | ( () ( (lv_nameVal_8_0= RULE_ID ) ) ) | ( '(' this_Expr_10= ruleExpr ')' ) | ( () ( (lv_intVal_13_0= RULE_INT ) ) ) | ( () ( (lv_numVal_15_0= RULE_NUMBER ) ) ) | ( () ( (lv_strVal_17_0= RULE_STRING ) ) ) | ( () 'buildin' ( (lv_name_20_0= RULE_ID ) ) ( ( (lv_parameters_21_0= ruleExprList ) ) | ( '(' ')' ) ) ) | ( () 'buildin' '(' ( (lv_left_27_0= ruleExpr ) ) ( (lv_op_28_0= RULE_OPERATOR ) ) ( (lv_right_29_0= ruleExpr ) ) ')' ) )
            int alt45=9;
            switch ( input.LA(1) ) {
            case RULE_ID:
                {
                int LA45_1 = input.LA(2);

                if ( (LA45_1==28) ) {
                    int LA45_7 = input.LA(3);

                    if ( (LA45_7==30) ) {
                        alt45=2;
                    }
                    else if ( (LA45_7==RULE_ID||(LA45_7>=RULE_INT && LA45_7<=RULE_STRING)||LA45_7==28||(LA45_7>=45 && LA45_7<=46)||(LA45_7>=52 && LA45_7<=53)) ) {
                        alt45=1;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("2948:1: ( ( () ( (lv_nameVal_1_0= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) ) ) | ( () ( (lv_nameVal_4_0= RULE_ID ) ) '(' ')' ) | ( () ( (lv_nameVal_8_0= RULE_ID ) ) ) | ( '(' this_Expr_10= ruleExpr ')' ) | ( () ( (lv_intVal_13_0= RULE_INT ) ) ) | ( () ( (lv_numVal_15_0= RULE_NUMBER ) ) ) | ( () ( (lv_strVal_17_0= RULE_STRING ) ) ) | ( () 'buildin' ( (lv_name_20_0= RULE_ID ) ) ( ( (lv_parameters_21_0= ruleExprList ) ) | ( '(' ')' ) ) ) | ( () 'buildin' '(' ( (lv_left_27_0= ruleExpr ) ) ( (lv_op_28_0= RULE_OPERATOR ) ) ( (lv_right_29_0= ruleExpr ) ) ')' ) )", 45, 7, input);

                        throw nvae;
                    }
                }
                else if ( (LA45_1==EOF||LA45_1==RULE_NL||LA45_1==RULE_OPERATOR||LA45_1==15||LA45_1==18||LA45_1==22||(LA45_1>=29 && LA45_1<=30)||(LA45_1>=35 && LA45_1<=51)) ) {
                    alt45=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("2948:1: ( ( () ( (lv_nameVal_1_0= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) ) ) | ( () ( (lv_nameVal_4_0= RULE_ID ) ) '(' ')' ) | ( () ( (lv_nameVal_8_0= RULE_ID ) ) ) | ( '(' this_Expr_10= ruleExpr ')' ) | ( () ( (lv_intVal_13_0= RULE_INT ) ) ) | ( () ( (lv_numVal_15_0= RULE_NUMBER ) ) ) | ( () ( (lv_strVal_17_0= RULE_STRING ) ) ) | ( () 'buildin' ( (lv_name_20_0= RULE_ID ) ) ( ( (lv_parameters_21_0= ruleExprList ) ) | ( '(' ')' ) ) ) | ( () 'buildin' '(' ( (lv_left_27_0= ruleExpr ) ) ( (lv_op_28_0= RULE_OPERATOR ) ) ( (lv_right_29_0= ruleExpr ) ) ')' ) )", 45, 1, input);

                    throw nvae;
                }
                }
                break;
            case 28:
                {
                alt45=4;
                }
                break;
            case RULE_INT:
                {
                alt45=5;
                }
                break;
            case RULE_NUMBER:
                {
                alt45=6;
                }
                break;
            case RULE_STRING:
                {
                alt45=7;
                }
                break;
            case 53:
                {
                int LA45_6 = input.LA(2);

                if ( (LA45_6==RULE_ID) ) {
                    alt45=8;
                }
                else if ( (LA45_6==28) ) {
                    alt45=9;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("2948:1: ( ( () ( (lv_nameVal_1_0= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) ) ) | ( () ( (lv_nameVal_4_0= RULE_ID ) ) '(' ')' ) | ( () ( (lv_nameVal_8_0= RULE_ID ) ) ) | ( '(' this_Expr_10= ruleExpr ')' ) | ( () ( (lv_intVal_13_0= RULE_INT ) ) ) | ( () ( (lv_numVal_15_0= RULE_NUMBER ) ) ) | ( () ( (lv_strVal_17_0= RULE_STRING ) ) ) | ( () 'buildin' ( (lv_name_20_0= RULE_ID ) ) ( ( (lv_parameters_21_0= ruleExprList ) ) | ( '(' ')' ) ) ) | ( () 'buildin' '(' ( (lv_left_27_0= ruleExpr ) ) ( (lv_op_28_0= RULE_OPERATOR ) ) ( (lv_right_29_0= ruleExpr ) ) ')' ) )", 45, 6, input);

                    throw nvae;
                }
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("2948:1: ( ( () ( (lv_nameVal_1_0= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) ) ) | ( () ( (lv_nameVal_4_0= RULE_ID ) ) '(' ')' ) | ( () ( (lv_nameVal_8_0= RULE_ID ) ) ) | ( '(' this_Expr_10= ruleExpr ')' ) | ( () ( (lv_intVal_13_0= RULE_INT ) ) ) | ( () ( (lv_numVal_15_0= RULE_NUMBER ) ) ) | ( () ( (lv_strVal_17_0= RULE_STRING ) ) ) | ( () 'buildin' ( (lv_name_20_0= RULE_ID ) ) ( ( (lv_parameters_21_0= ruleExprList ) ) | ( '(' ')' ) ) ) | ( () 'buildin' '(' ( (lv_left_27_0= ruleExpr ) ) ( (lv_op_28_0= RULE_OPERATOR ) ) ( (lv_right_29_0= ruleExpr ) ) ')' ) )", 45, 0, input);

                throw nvae;
            }

            switch (alt45) {
                case 1 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2948:2: ( () ( (lv_nameVal_1_0= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2948:2: ( () ( (lv_nameVal_1_0= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2948:3: () ( (lv_nameVal_1_0= RULE_ID ) ) ( (lv_parameters_2_0= ruleExprList ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2948:3: ()
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2949:5: 
                    {
                     
                            temp=factory.create(grammarAccess.getExprAtomicAccess().getExprFunctioncallAction_0_0().getType().getClassifier());
                            current = temp; 
                            temp = null;
                            CompositeNode newNode = createCompositeNode(grammarAccess.getExprAtomicAccess().getExprFunctioncallAction_0_0(), currentNode.getParent());
                        newNode.getChildren().add(currentNode);
                        moveLookaheadInfo(currentNode, newNode);
                        currentNode = newNode; 
                            associateNodeWithAstElement(currentNode, current); 
                        

                    }

                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2959:2: ( (lv_nameVal_1_0= RULE_ID ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2960:1: (lv_nameVal_1_0= RULE_ID )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2960:1: (lv_nameVal_1_0= RULE_ID )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2961:3: lv_nameVal_1_0= RULE_ID
                    {
                    lv_nameVal_1_0=(Token)input.LT(1);
                    match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleExprAtomic5280); 

                    			createLeafNode(grammarAccess.getExprAtomicAccess().getNameValIDTerminalRuleCall_0_1_0(), "nameVal"); 
                    		

                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getExprAtomicRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode, current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"nameVal",
                    	        		lv_nameVal_1_0, 
                    	        		"ID", 
                    	        		lastConsumedNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2983:2: ( (lv_parameters_2_0= ruleExprList ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2984:1: (lv_parameters_2_0= ruleExprList )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2984:1: (lv_parameters_2_0= ruleExprList )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:2985:3: lv_parameters_2_0= ruleExprList
                    {
                     
                    	        currentNode=createCompositeNode(grammarAccess.getExprAtomicAccess().getParametersExprListParserRuleCall_0_2_0(), currentNode); 
                    	    
                    pushFollow(FOLLOW_ruleExprList_in_ruleExprAtomic5306);
                    lv_parameters_2_0=ruleExprList();
                    _fsp--;


                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getExprAtomicRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode.getParent(), current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"parameters",
                    	        		lv_parameters_2_0, 
                    	        		"ExprList", 
                    	        		currentNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	        currentNode = currentNode.getParent();
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3008:6: ( () ( (lv_nameVal_4_0= RULE_ID ) ) '(' ')' )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3008:6: ( () ( (lv_nameVal_4_0= RULE_ID ) ) '(' ')' )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3008:7: () ( (lv_nameVal_4_0= RULE_ID ) ) '(' ')'
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3008:7: ()
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3009:5: 
                    {
                     
                            temp=factory.create(grammarAccess.getExprAtomicAccess().getExprFunctioncallAction_1_0().getType().getClassifier());
                            current = temp; 
                            temp = null;
                            CompositeNode newNode = createCompositeNode(grammarAccess.getExprAtomicAccess().getExprFunctioncallAction_1_0(), currentNode.getParent());
                        newNode.getChildren().add(currentNode);
                        moveLookaheadInfo(currentNode, newNode);
                        currentNode = newNode; 
                            associateNodeWithAstElement(currentNode, current); 
                        

                    }

                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3019:2: ( (lv_nameVal_4_0= RULE_ID ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3020:1: (lv_nameVal_4_0= RULE_ID )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3020:1: (lv_nameVal_4_0= RULE_ID )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3021:3: lv_nameVal_4_0= RULE_ID
                    {
                    lv_nameVal_4_0=(Token)input.LT(1);
                    match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleExprAtomic5340); 

                    			createLeafNode(grammarAccess.getExprAtomicAccess().getNameValIDTerminalRuleCall_1_1_0(), "nameVal"); 
                    		

                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getExprAtomicRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode, current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"nameVal",
                    	        		lv_nameVal_4_0, 
                    	        		"ID", 
                    	        		lastConsumedNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	    

                    }


                    }

                    match(input,28,FOLLOW_28_in_ruleExprAtomic5355); 

                            createLeafNode(grammarAccess.getExprAtomicAccess().getLeftParenthesisKeyword_1_2(), null); 
                        
                    match(input,30,FOLLOW_30_in_ruleExprAtomic5365); 

                            createLeafNode(grammarAccess.getExprAtomicAccess().getRightParenthesisKeyword_1_3(), null); 
                        

                    }


                    }
                    break;
                case 3 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3052:6: ( () ( (lv_nameVal_8_0= RULE_ID ) ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3052:6: ( () ( (lv_nameVal_8_0= RULE_ID ) ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3052:7: () ( (lv_nameVal_8_0= RULE_ID ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3052:7: ()
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3053:5: 
                    {
                     
                            temp=factory.create(grammarAccess.getExprAtomicAccess().getExprIdentifierAction_2_0().getType().getClassifier());
                            current = temp; 
                            temp = null;
                            CompositeNode newNode = createCompositeNode(grammarAccess.getExprAtomicAccess().getExprIdentifierAction_2_0(), currentNode.getParent());
                        newNode.getChildren().add(currentNode);
                        moveLookaheadInfo(currentNode, newNode);
                        currentNode = newNode; 
                            associateNodeWithAstElement(currentNode, current); 
                        

                    }

                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3063:2: ( (lv_nameVal_8_0= RULE_ID ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3064:1: (lv_nameVal_8_0= RULE_ID )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3064:1: (lv_nameVal_8_0= RULE_ID )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3065:3: lv_nameVal_8_0= RULE_ID
                    {
                    lv_nameVal_8_0=(Token)input.LT(1);
                    match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleExprAtomic5399); 

                    			createLeafNode(grammarAccess.getExprAtomicAccess().getNameValIDTerminalRuleCall_2_1_0(), "nameVal"); 
                    		

                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getExprAtomicRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode, current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"nameVal",
                    	        		lv_nameVal_8_0, 
                    	        		"ID", 
                    	        		lastConsumedNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 4 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3088:6: ( '(' this_Expr_10= ruleExpr ')' )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3088:6: ( '(' this_Expr_10= ruleExpr ')' )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3088:8: '(' this_Expr_10= ruleExpr ')'
                    {
                    match(input,28,FOLLOW_28_in_ruleExprAtomic5422); 

                            createLeafNode(grammarAccess.getExprAtomicAccess().getLeftParenthesisKeyword_3_0(), null); 
                        
                     
                            currentNode=createCompositeNode(grammarAccess.getExprAtomicAccess().getExprParserRuleCall_3_1(), currentNode); 
                        
                    pushFollow(FOLLOW_ruleExpr_in_ruleExprAtomic5444);
                    this_Expr_10=ruleExpr();
                    _fsp--;

                     
                            current = this_Expr_10; 
                            currentNode = currentNode.getParent();
                        
                    match(input,30,FOLLOW_30_in_ruleExprAtomic5453); 

                            createLeafNode(grammarAccess.getExprAtomicAccess().getRightParenthesisKeyword_3_2(), null); 
                        

                    }


                    }
                    break;
                case 5 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3106:6: ( () ( (lv_intVal_13_0= RULE_INT ) ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3106:6: ( () ( (lv_intVal_13_0= RULE_INT ) ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3106:7: () ( (lv_intVal_13_0= RULE_INT ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3106:7: ()
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3107:5: 
                    {
                     
                            temp=factory.create(grammarAccess.getExprAtomicAccess().getExprIntValAction_4_0().getType().getClassifier());
                            current = temp; 
                            temp = null;
                            CompositeNode newNode = createCompositeNode(grammarAccess.getExprAtomicAccess().getExprIntValAction_4_0(), currentNode.getParent());
                        newNode.getChildren().add(currentNode);
                        moveLookaheadInfo(currentNode, newNode);
                        currentNode = newNode; 
                            associateNodeWithAstElement(currentNode, current); 
                        

                    }

                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3117:2: ( (lv_intVal_13_0= RULE_INT ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3118:1: (lv_intVal_13_0= RULE_INT )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3118:1: (lv_intVal_13_0= RULE_INT )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3119:3: lv_intVal_13_0= RULE_INT
                    {
                    lv_intVal_13_0=(Token)input.LT(1);
                    match(input,RULE_INT,FOLLOW_RULE_INT_in_ruleExprAtomic5487); 

                    			createLeafNode(grammarAccess.getExprAtomicAccess().getIntValINTTerminalRuleCall_4_1_0(), "intVal"); 
                    		

                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getExprAtomicRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode, current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"intVal",
                    	        		lv_intVal_13_0, 
                    	        		"INT", 
                    	        		lastConsumedNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 6 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3142:6: ( () ( (lv_numVal_15_0= RULE_NUMBER ) ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3142:6: ( () ( (lv_numVal_15_0= RULE_NUMBER ) ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3142:7: () ( (lv_numVal_15_0= RULE_NUMBER ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3142:7: ()
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3143:5: 
                    {
                     
                            temp=factory.create(grammarAccess.getExprAtomicAccess().getExprNumValAction_5_0().getType().getClassifier());
                            current = temp; 
                            temp = null;
                            CompositeNode newNode = createCompositeNode(grammarAccess.getExprAtomicAccess().getExprNumValAction_5_0(), currentNode.getParent());
                        newNode.getChildren().add(currentNode);
                        moveLookaheadInfo(currentNode, newNode);
                        currentNode = newNode; 
                            associateNodeWithAstElement(currentNode, current); 
                        

                    }

                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3153:2: ( (lv_numVal_15_0= RULE_NUMBER ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3154:1: (lv_numVal_15_0= RULE_NUMBER )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3154:1: (lv_numVal_15_0= RULE_NUMBER )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3155:3: lv_numVal_15_0= RULE_NUMBER
                    {
                    lv_numVal_15_0=(Token)input.LT(1);
                    match(input,RULE_NUMBER,FOLLOW_RULE_NUMBER_in_ruleExprAtomic5526); 

                    			createLeafNode(grammarAccess.getExprAtomicAccess().getNumValNUMBERTerminalRuleCall_5_1_0(), "numVal"); 
                    		

                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getExprAtomicRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode, current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"numVal",
                    	        		lv_numVal_15_0, 
                    	        		"NUMBER", 
                    	        		lastConsumedNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 7 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3178:6: ( () ( (lv_strVal_17_0= RULE_STRING ) ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3178:6: ( () ( (lv_strVal_17_0= RULE_STRING ) ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3178:7: () ( (lv_strVal_17_0= RULE_STRING ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3178:7: ()
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3179:5: 
                    {
                     
                            temp=factory.create(grammarAccess.getExprAtomicAccess().getExprStrvalAction_6_0().getType().getClassifier());
                            current = temp; 
                            temp = null;
                            CompositeNode newNode = createCompositeNode(grammarAccess.getExprAtomicAccess().getExprStrvalAction_6_0(), currentNode.getParent());
                        newNode.getChildren().add(currentNode);
                        moveLookaheadInfo(currentNode, newNode);
                        currentNode = newNode; 
                            associateNodeWithAstElement(currentNode, current); 
                        

                    }

                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3189:2: ( (lv_strVal_17_0= RULE_STRING ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3190:1: (lv_strVal_17_0= RULE_STRING )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3190:1: (lv_strVal_17_0= RULE_STRING )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3191:3: lv_strVal_17_0= RULE_STRING
                    {
                    lv_strVal_17_0=(Token)input.LT(1);
                    match(input,RULE_STRING,FOLLOW_RULE_STRING_in_ruleExprAtomic5565); 

                    			createLeafNode(grammarAccess.getExprAtomicAccess().getStrValSTRINGTerminalRuleCall_6_1_0(), "strVal"); 
                    		

                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getExprAtomicRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode, current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"strVal",
                    	        		lv_strVal_17_0, 
                    	        		"STRING", 
                    	        		lastConsumedNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	    

                    }


                    }


                    }


                    }
                    break;
                case 8 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3214:6: ( () 'buildin' ( (lv_name_20_0= RULE_ID ) ) ( ( (lv_parameters_21_0= ruleExprList ) ) | ( '(' ')' ) ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3214:6: ( () 'buildin' ( (lv_name_20_0= RULE_ID ) ) ( ( (lv_parameters_21_0= ruleExprList ) ) | ( '(' ')' ) ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3214:7: () 'buildin' ( (lv_name_20_0= RULE_ID ) ) ( ( (lv_parameters_21_0= ruleExprList ) ) | ( '(' ')' ) )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3214:7: ()
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3215:5: 
                    {
                     
                            temp=factory.create(grammarAccess.getExprAtomicAccess().getExprBuildinFunctionAction_7_0().getType().getClassifier());
                            current = temp; 
                            temp = null;
                            CompositeNode newNode = createCompositeNode(grammarAccess.getExprAtomicAccess().getExprBuildinFunctionAction_7_0(), currentNode.getParent());
                        newNode.getChildren().add(currentNode);
                        moveLookaheadInfo(currentNode, newNode);
                        currentNode = newNode; 
                            associateNodeWithAstElement(currentNode, current); 
                        

                    }

                    match(input,53,FOLLOW_53_in_ruleExprAtomic5597); 

                            createLeafNode(grammarAccess.getExprAtomicAccess().getBuildinKeyword_7_1(), null); 
                        
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3229:1: ( (lv_name_20_0= RULE_ID ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3230:1: (lv_name_20_0= RULE_ID )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3230:1: (lv_name_20_0= RULE_ID )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3231:3: lv_name_20_0= RULE_ID
                    {
                    lv_name_20_0=(Token)input.LT(1);
                    match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleExprAtomic5614); 

                    			createLeafNode(grammarAccess.getExprAtomicAccess().getNameIDTerminalRuleCall_7_2_0(), "name"); 
                    		

                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getExprAtomicRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode, current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"name",
                    	        		lv_name_20_0, 
                    	        		"ID", 
                    	        		lastConsumedNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3253:2: ( ( (lv_parameters_21_0= ruleExprList ) ) | ( '(' ')' ) )
                    int alt44=2;
                    int LA44_0 = input.LA(1);

                    if ( (LA44_0==28) ) {
                        int LA44_1 = input.LA(2);

                        if ( (LA44_1==30) ) {
                            alt44=2;
                        }
                        else if ( (LA44_1==RULE_ID||(LA44_1>=RULE_INT && LA44_1<=RULE_STRING)||LA44_1==28||(LA44_1>=45 && LA44_1<=46)||(LA44_1>=52 && LA44_1<=53)) ) {
                            alt44=1;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("3253:2: ( ( (lv_parameters_21_0= ruleExprList ) ) | ( '(' ')' ) )", 44, 1, input);

                            throw nvae;
                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("3253:2: ( ( (lv_parameters_21_0= ruleExprList ) ) | ( '(' ')' ) )", 44, 0, input);

                        throw nvae;
                    }
                    switch (alt44) {
                        case 1 :
                            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3253:3: ( (lv_parameters_21_0= ruleExprList ) )
                            {
                            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3253:3: ( (lv_parameters_21_0= ruleExprList ) )
                            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3254:1: (lv_parameters_21_0= ruleExprList )
                            {
                            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3254:1: (lv_parameters_21_0= ruleExprList )
                            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3255:3: lv_parameters_21_0= ruleExprList
                            {
                             
                            	        currentNode=createCompositeNode(grammarAccess.getExprAtomicAccess().getParametersExprListParserRuleCall_7_3_0_0(), currentNode); 
                            	    
                            pushFollow(FOLLOW_ruleExprList_in_ruleExprAtomic5641);
                            lv_parameters_21_0=ruleExprList();
                            _fsp--;


                            	        if (current==null) {
                            	            current = factory.create(grammarAccess.getExprAtomicRule().getType().getClassifier());
                            	            associateNodeWithAstElement(currentNode.getParent(), current);
                            	        }
                            	        try {
                            	       		set(
                            	       			current, 
                            	       			"parameters",
                            	        		lv_parameters_21_0, 
                            	        		"ExprList", 
                            	        		currentNode);
                            	        } catch (ValueConverterException vce) {
                            				handleValueConverterException(vce);
                            	        }
                            	        currentNode = currentNode.getParent();
                            	    

                            }


                            }


                            }
                            break;
                        case 2 :
                            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3278:6: ( '(' ')' )
                            {
                            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3278:6: ( '(' ')' )
                            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3278:8: '(' ')'
                            {
                            match(input,28,FOLLOW_28_in_ruleExprAtomic5658); 

                                    createLeafNode(grammarAccess.getExprAtomicAccess().getLeftParenthesisKeyword_7_3_1_0(), null); 
                                
                            match(input,30,FOLLOW_30_in_ruleExprAtomic5668); 

                                    createLeafNode(grammarAccess.getExprAtomicAccess().getRightParenthesisKeyword_7_3_1_1(), null); 
                                

                            }


                            }
                            break;

                    }


                    }


                    }
                    break;
                case 9 :
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3287:6: ( () 'buildin' '(' ( (lv_left_27_0= ruleExpr ) ) ( (lv_op_28_0= RULE_OPERATOR ) ) ( (lv_right_29_0= ruleExpr ) ) ')' )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3287:6: ( () 'buildin' '(' ( (lv_left_27_0= ruleExpr ) ) ( (lv_op_28_0= RULE_OPERATOR ) ) ( (lv_right_29_0= ruleExpr ) ) ')' )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3287:7: () 'buildin' '(' ( (lv_left_27_0= ruleExpr ) ) ( (lv_op_28_0= RULE_OPERATOR ) ) ( (lv_right_29_0= ruleExpr ) ) ')'
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3287:7: ()
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3288:5: 
                    {
                     
                            temp=factory.create(grammarAccess.getExprAtomicAccess().getExprBuildinOperatorAction_8_0().getType().getClassifier());
                            current = temp; 
                            temp = null;
                            CompositeNode newNode = createCompositeNode(grammarAccess.getExprAtomicAccess().getExprBuildinOperatorAction_8_0(), currentNode.getParent());
                        newNode.getChildren().add(currentNode);
                        moveLookaheadInfo(currentNode, newNode);
                        currentNode = newNode; 
                            associateNodeWithAstElement(currentNode, current); 
                        

                    }

                    match(input,53,FOLLOW_53_in_ruleExprAtomic5697); 

                            createLeafNode(grammarAccess.getExprAtomicAccess().getBuildinKeyword_8_1(), null); 
                        
                    match(input,28,FOLLOW_28_in_ruleExprAtomic5707); 

                            createLeafNode(grammarAccess.getExprAtomicAccess().getLeftParenthesisKeyword_8_2(), null); 
                        
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3306:1: ( (lv_left_27_0= ruleExpr ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3307:1: (lv_left_27_0= ruleExpr )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3307:1: (lv_left_27_0= ruleExpr )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3308:3: lv_left_27_0= ruleExpr
                    {
                     
                    	        currentNode=createCompositeNode(grammarAccess.getExprAtomicAccess().getLeftExprParserRuleCall_8_3_0(), currentNode); 
                    	    
                    pushFollow(FOLLOW_ruleExpr_in_ruleExprAtomic5728);
                    lv_left_27_0=ruleExpr();
                    _fsp--;


                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getExprAtomicRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode.getParent(), current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"left",
                    	        		lv_left_27_0, 
                    	        		"Expr", 
                    	        		currentNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	        currentNode = currentNode.getParent();
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3330:2: ( (lv_op_28_0= RULE_OPERATOR ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3331:1: (lv_op_28_0= RULE_OPERATOR )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3331:1: (lv_op_28_0= RULE_OPERATOR )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3332:3: lv_op_28_0= RULE_OPERATOR
                    {
                    lv_op_28_0=(Token)input.LT(1);
                    match(input,RULE_OPERATOR,FOLLOW_RULE_OPERATOR_in_ruleExprAtomic5745); 

                    			createLeafNode(grammarAccess.getExprAtomicAccess().getOpOPERATORTerminalRuleCall_8_4_0(), "op"); 
                    		

                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getExprAtomicRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode, current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"op",
                    	        		lv_op_28_0, 
                    	        		"OPERATOR", 
                    	        		lastConsumedNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	    

                    }


                    }

                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3354:2: ( (lv_right_29_0= ruleExpr ) )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3355:1: (lv_right_29_0= ruleExpr )
                    {
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3355:1: (lv_right_29_0= ruleExpr )
                    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3356:3: lv_right_29_0= ruleExpr
                    {
                     
                    	        currentNode=createCompositeNode(grammarAccess.getExprAtomicAccess().getRightExprParserRuleCall_8_5_0(), currentNode); 
                    	    
                    pushFollow(FOLLOW_ruleExpr_in_ruleExprAtomic5771);
                    lv_right_29_0=ruleExpr();
                    _fsp--;


                    	        if (current==null) {
                    	            current = factory.create(grammarAccess.getExprAtomicRule().getType().getClassifier());
                    	            associateNodeWithAstElement(currentNode.getParent(), current);
                    	        }
                    	        try {
                    	       		set(
                    	       			current, 
                    	       			"right",
                    	        		lv_right_29_0, 
                    	        		"Expr", 
                    	        		currentNode);
                    	        } catch (ValueConverterException vce) {
                    				handleValueConverterException(vce);
                    	        }
                    	        currentNode = currentNode.getParent();
                    	    

                    }


                    }

                    match(input,30,FOLLOW_30_in_ruleExprAtomic5781); 

                            createLeafNode(grammarAccess.getExprAtomicAccess().getRightParenthesisKeyword_8_6(), null); 
                        

                    }


                    }
                    break;

            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleExprAtomic


    // $ANTLR start entryRuleExprList
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3390:1: entryRuleExprList returns [EObject current=null] : iv_ruleExprList= ruleExprList EOF ;
    public final EObject entryRuleExprList() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExprList = null;


        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3391:2: (iv_ruleExprList= ruleExprList EOF )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3392:2: iv_ruleExprList= ruleExprList EOF
            {
             currentNode = createCompositeNode(grammarAccess.getExprListRule(), currentNode); 
            pushFollow(FOLLOW_ruleExprList_in_entryRuleExprList5818);
            iv_ruleExprList=ruleExprList();
            _fsp--;

             current =iv_ruleExprList; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleExprList5828); 

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
    // $ANTLR end entryRuleExprList


    // $ANTLR start ruleExprList
    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3399:1: ruleExprList returns [EObject current=null] : ( '(' ( (lv_params_1_0= ruleExpr ) ) ( ',' ( (lv_params_3_0= ruleExpr ) ) )* ')' ) ;
    public final EObject ruleExprList() throws RecognitionException {
        EObject current = null;

        EObject lv_params_1_0 = null;

        EObject lv_params_3_0 = null;


         EObject temp=null; setCurrentLookahead(); resetLookahead(); 
            
        try {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3404:6: ( ( '(' ( (lv_params_1_0= ruleExpr ) ) ( ',' ( (lv_params_3_0= ruleExpr ) ) )* ')' ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3405:1: ( '(' ( (lv_params_1_0= ruleExpr ) ) ( ',' ( (lv_params_3_0= ruleExpr ) ) )* ')' )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3405:1: ( '(' ( (lv_params_1_0= ruleExpr ) ) ( ',' ( (lv_params_3_0= ruleExpr ) ) )* ')' )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3405:3: '(' ( (lv_params_1_0= ruleExpr ) ) ( ',' ( (lv_params_3_0= ruleExpr ) ) )* ')'
            {
            match(input,28,FOLLOW_28_in_ruleExprList5863); 

                    createLeafNode(grammarAccess.getExprListAccess().getLeftParenthesisKeyword_0(), null); 
                
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3409:1: ( (lv_params_1_0= ruleExpr ) )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3410:1: (lv_params_1_0= ruleExpr )
            {
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3410:1: (lv_params_1_0= ruleExpr )
            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3411:3: lv_params_1_0= ruleExpr
            {
             
            	        currentNode=createCompositeNode(grammarAccess.getExprListAccess().getParamsExprParserRuleCall_1_0(), currentNode); 
            	    
            pushFollow(FOLLOW_ruleExpr_in_ruleExprList5884);
            lv_params_1_0=ruleExpr();
            _fsp--;


            	        if (current==null) {
            	            current = factory.create(grammarAccess.getExprListRule().getType().getClassifier());
            	            associateNodeWithAstElement(currentNode.getParent(), current);
            	        }
            	        try {
            	       		add(
            	       			current, 
            	       			"params",
            	        		lv_params_1_0, 
            	        		"Expr", 
            	        		currentNode);
            	        } catch (ValueConverterException vce) {
            				handleValueConverterException(vce);
            	        }
            	        currentNode = currentNode.getParent();
            	    

            }


            }

            // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3433:2: ( ',' ( (lv_params_3_0= ruleExpr ) ) )*
            loop46:
            do {
                int alt46=2;
                int LA46_0 = input.LA(1);

                if ( (LA46_0==29) ) {
                    alt46=1;
                }


                switch (alt46) {
            	case 1 :
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3433:4: ',' ( (lv_params_3_0= ruleExpr ) )
            	    {
            	    match(input,29,FOLLOW_29_in_ruleExprList5895); 

            	            createLeafNode(grammarAccess.getExprListAccess().getCommaKeyword_2_0(), null); 
            	        
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3437:1: ( (lv_params_3_0= ruleExpr ) )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3438:1: (lv_params_3_0= ruleExpr )
            	    {
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3438:1: (lv_params_3_0= ruleExpr )
            	    // ../de.peeeq.Pscript/src-gen/de/peeeq/pscript/parser/antlr/internal/InternalPscript.g:3439:3: lv_params_3_0= ruleExpr
            	    {
            	     
            	    	        currentNode=createCompositeNode(grammarAccess.getExprListAccess().getParamsExprParserRuleCall_2_1_0(), currentNode); 
            	    	    
            	    pushFollow(FOLLOW_ruleExpr_in_ruleExprList5916);
            	    lv_params_3_0=ruleExpr();
            	    _fsp--;


            	    	        if (current==null) {
            	    	            current = factory.create(grammarAccess.getExprListRule().getType().getClassifier());
            	    	            associateNodeWithAstElement(currentNode.getParent(), current);
            	    	        }
            	    	        try {
            	    	       		add(
            	    	       			current, 
            	    	       			"params",
            	    	        		lv_params_3_0, 
            	    	        		"Expr", 
            	    	        		currentNode);
            	    	        } catch (ValueConverterException vce) {
            	    				handleValueConverterException(vce);
            	    	        }
            	    	        currentNode = currentNode.getParent();
            	    	    

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop46;
                }
            } while (true);

            match(input,30,FOLLOW_30_in_ruleExprList5928); 

                    createLeafNode(grammarAccess.getExprListAccess().getRightParenthesisKeyword_3(), null); 
                

            }


            }

             resetLookahead(); 
                	lastConsumedNode = currentNode;
                
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end ruleExprList


    protected DFA5 dfa5 = new DFA5(this);
    protected DFA13 dfa13 = new DFA13(this);
    static final String DFA5_eotS =
        "\4\uffff";
    static final String DFA5_eofS =
        "\4\uffff";
    static final String DFA5_minS =
        "\2\4\2\uffff";
    static final String DFA5_maxS =
        "\2\33\2\uffff";
    static final String DFA5_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA5_specialS =
        "\4\uffff}>";
    static final String[] DFA5_transitionS = {
            "\1\1\13\uffff\1\2\1\3\2\uffff\1\2\2\uffff\3\2\1\uffff\1\2",
            "\1\1\13\uffff\1\2\1\3\2\uffff\1\2\2\uffff\3\2\1\uffff\1\2",
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
            return "()* loopback of 203:1: ( ( RULE_NL )* ( (lv_imports_4_0= ruleImport ) ) )*";
        }
    }
    static final String DFA13_eotS =
        "\4\uffff";
    static final String DFA13_eofS =
        "\4\uffff";
    static final String DFA13_minS =
        "\2\4\2\uffff";
    static final String DFA13_maxS =
        "\2\33\2\uffff";
    static final String DFA13_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA13_specialS =
        "\4\uffff}>";
    static final String[] DFA13_transitionS = {
            "\1\1\13\uffff\1\2\7\uffff\2\3\1\uffff\1\3",
            "\1\1\13\uffff\1\2\7\uffff\2\3\1\uffff\1\3",
            "",
            ""
    };

    static final short[] DFA13_eot = DFA.unpackEncodedString(DFA13_eotS);
    static final short[] DFA13_eof = DFA.unpackEncodedString(DFA13_eofS);
    static final char[] DFA13_min = DFA.unpackEncodedStringToUnsignedChars(DFA13_minS);
    static final char[] DFA13_max = DFA.unpackEncodedStringToUnsignedChars(DFA13_maxS);
    static final short[] DFA13_accept = DFA.unpackEncodedString(DFA13_acceptS);
    static final short[] DFA13_special = DFA.unpackEncodedString(DFA13_specialS);
    static final short[][] DFA13_transition;

    static {
        int numStates = DFA13_transitionS.length;
        DFA13_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA13_transition[i] = DFA.unpackEncodedString(DFA13_transitionS[i]);
        }
    }

    class DFA13 extends DFA {

        public DFA13(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 13;
            this.eot = DFA13_eot;
            this.eof = DFA13_eof;
            this.min = DFA13_min;
            this.max = DFA13_max;
            this.accept = DFA13_accept;
            this.special = DFA13_special;
            this.transition = DFA13_transition;
        }
        public String getDescription() {
            return "()* loopback of 635:1: ( (lv_members_4_0= ruleClassMember ) )*";
        }
    }
 

    public static final BitSet FOLLOW_ruleProgram_in_entryRuleProgram75 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleProgram85 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleProgram120 = new BitSet(new long[]{0x0000000000004010L});
    public static final BitSet FOLLOW_rulePackageDeclaration_in_ruleProgram142 = new BitSet(new long[]{0x0000000000004012L});
    public static final BitSet FOLLOW_rulePackageDeclaration_in_ruleProgram163 = new BitSet(new long[]{0x0000000000004012L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleProgram174 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_rulePackageDeclaration_in_entryRulePackageDeclaration211 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRulePackageDeclaration221 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_14_in_rulePackageDeclaration256 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ruleQualifiedName_in_rulePackageDeclaration277 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_rulePackageDeclaration287 = new BitSet(new long[]{0x000000000B930010L});
    public static final BitSet FOLLOW_RULE_NL_in_rulePackageDeclaration298 = new BitSet(new long[]{0x0000000000020010L});
    public static final BitSet FOLLOW_ruleImport_in_rulePackageDeclaration320 = new BitSet(new long[]{0x000000000B930010L});
    public static final BitSet FOLLOW_RULE_NL_in_rulePackageDeclaration332 = new BitSet(new long[]{0x000000000B910010L});
    public static final BitSet FOLLOW_ruleEntity_in_rulePackageDeclaration355 = new BitSet(new long[]{0x000000000B910010L});
    public static final BitSet FOLLOW_RULE_NL_in_rulePackageDeclaration365 = new BitSet(new long[]{0x000000000B910010L});
    public static final BitSet FOLLOW_16_in_rulePackageDeclaration378 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_RULE_NL_in_rulePackageDeclaration388 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_ruleImport_in_entryRuleImport425 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleImport435 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_17_in_ruleImport470 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ruleQualifiedNameWithWildCard_in_ruleImport491 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleImport500 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleQualifiedName_in_entryRuleQualifiedName536 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleQualifiedName547 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleQualifiedName587 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_18_in_ruleQualifiedName606 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleQualifiedName621 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_ruleQualifiedNameWithWildCard_in_entryRuleQualifiedNameWithWildCard669 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleQualifiedNameWithWildCard680 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleQualifiedName_in_ruleQualifiedNameWithWildCard727 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_19_in_ruleQualifiedNameWithWildCard746 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleEntity_in_entryRuleEntity788 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleEntity798 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleClassDef_in_ruleEntity845 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNativeType_in_ruleEntity872 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFuncDef_in_ruleEntity899 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleVarDef_in_ruleEntity926 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleNativeType_in_entryRuleNativeType961 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleNativeType971 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_20_in_ruleNativeType1015 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_21_in_ruleNativeType1025 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleNativeType1042 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_ruleNativeType1057 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleNativeType1074 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleNativeType1088 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleClassDef_in_entryRuleClassDef1123 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleClassDef1133 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_23_in_ruleClassDef1177 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleClassDef1194 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_ruleClassDef1209 = new BitSet(new long[]{0x000000000B010010L});
    public static final BitSet FOLLOW_ruleClassMember_in_ruleClassDef1230 = new BitSet(new long[]{0x000000000B010010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleClassDef1241 = new BitSet(new long[]{0x0000000000010010L});
    public static final BitSet FOLLOW_16_in_ruleClassDef1252 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleClassDef1261 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleClassMember_in_entryRuleClassMember1296 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleClassMember1306 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleClassMember1341 = new BitSet(new long[]{0x000000000B000010L});
    public static final BitSet FOLLOW_ruleVarDef_in_ruleClassMember1365 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFuncDef_in_ruleClassMember1392 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleVarDef_in_entryRuleVarDef1428 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleVarDef1438 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_ruleVarDef1483 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_25_in_ruleVarDef1507 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleVarDef1538 = new BitSet(new long[]{0x0000000004400010L});
    public static final BitSet FOLLOW_26_in_ruleVarDef1554 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleVarDef1575 = new BitSet(new long[]{0x0000000000400010L});
    public static final BitSet FOLLOW_22_in_ruleVarDef1588 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleVarDef1609 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleVarDef1620 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_entryRuleTypeExpr1655 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleTypeExpr1665 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleTypeExpr1716 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleFuncDef_in_entryRuleFuncDef1757 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleFuncDef1767 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_ruleFuncDef1811 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleFuncDef1828 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_ruleFuncDef1843 = new BitSet(new long[]{0x0000000040000020L});
    public static final BitSet FOLLOW_ruleParameterDef_in_ruleFuncDef1865 = new BitSet(new long[]{0x0000000060000000L});
    public static final BitSet FOLLOW_29_in_ruleFuncDef1876 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ruleParameterDef_in_ruleFuncDef1897 = new BitSet(new long[]{0x0000000060000000L});
    public static final BitSet FOLLOW_30_in_ruleFuncDef1911 = new BitSet(new long[]{0x0000000004008000L});
    public static final BitSet FOLLOW_26_in_ruleFuncDef1922 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleFuncDef1943 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_ruleFuncDef1955 = new BitSet(new long[]{0x00306005930103B0L});
    public static final BitSet FOLLOW_ruleStatements_in_ruleFuncDef1976 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_16_in_ruleFuncDef1986 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleParameterDef_in_entryRuleParameterDef2022 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleParameterDef2032 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleParameterDef2083 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_ruleParameterDef2098 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ruleTypeExpr_in_ruleParameterDef2119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStatements_in_entryRuleStatements2155 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStatements2165 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStatements2209 = new BitSet(new long[]{0x00306005930003B2L});
    public static final BitSet FOLLOW_ruleStatement_in_ruleStatements2235 = new BitSet(new long[]{0x00306005930003B2L});
    public static final BitSet FOLLOW_ruleStatement_in_entryRuleStatement2273 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStatement2283 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtIf_in_ruleStatement2330 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtWhile_in_ruleStatement2357 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleVarDef_in_ruleStatement2384 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtExpr_in_ruleStatement2411 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtReturn_in_ruleStatement2438 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtReturn_in_entryRuleStmtReturn2473 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStmtReturn2483 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_ruleStmtReturn2527 = new BitSet(new long[]{0x00306000100003B0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleStmtReturn2548 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtReturn2558 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtIf_in_entryRuleStmtIf2593 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStmtIf2603 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_ruleStmtIf2638 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleStmtIf2659 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_ruleStmtIf2669 = new BitSet(new long[]{0x00306005930103B0L});
    public static final BitSet FOLLOW_ruleStatements_in_ruleStmtIf2690 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_16_in_ruleStmtIf2700 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_33_in_ruleStmtIf2711 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_ruleStmtIf2721 = new BitSet(new long[]{0x00306005930103B0L});
    public static final BitSet FOLLOW_ruleStatements_in_ruleStmtIf2742 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_16_in_ruleStmtIf2752 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtWhile_in_entryRuleStmtWhile2790 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStmtWhile2800 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_34_in_ruleStmtWhile2835 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleStmtWhile2856 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_ruleStmtWhile2866 = new BitSet(new long[]{0x00306005930103B0L});
    public static final BitSet FOLLOW_ruleStatements_in_ruleStmtWhile2887 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_16_in_ruleStmtWhile2897 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleStmtExpr_in_entryRuleStmtExpr2933 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleStmtExpr2943 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleStmtExpr2989 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_RULE_NL_in_ruleStmtExpr2998 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExpr_in_entryRuleExpr3033 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExpr3043 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprAssignment_in_ruleExpr3089 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprAssignment_in_entryRuleExprAssignment3123 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprAssignment3133 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprOr_in_ruleExprAssignment3180 = new BitSet(new long[]{0x0000001800400002L});
    public static final BitSet FOLLOW_22_in_ruleExprAssignment3209 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_35_in_ruleExprAssignment3238 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_36_in_ruleExprAssignment3267 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_ruleExprOr_in_ruleExprAssignment3304 = new BitSet(new long[]{0x0000001800400002L});
    public static final BitSet FOLLOW_ruleExprOr_in_entryRuleExprOr3342 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprOr3352 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprAnd_in_ruleExprOr3399 = new BitSet(new long[]{0x0000002000000002L});
    public static final BitSet FOLLOW_37_in_ruleExprOr3426 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_ruleExprAnd_in_ruleExprOr3460 = new BitSet(new long[]{0x0000002000000002L});
    public static final BitSet FOLLOW_ruleExprAnd_in_entryRuleExprAnd3498 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprAnd3508 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprEquality_in_ruleExprAnd3555 = new BitSet(new long[]{0x0000004000000002L});
    public static final BitSet FOLLOW_38_in_ruleExprAnd3582 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_ruleExprEquality_in_ruleExprAnd3616 = new BitSet(new long[]{0x0000004000000002L});
    public static final BitSet FOLLOW_ruleExprEquality_in_entryRuleExprEquality3654 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprEquality3664 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprComparison_in_ruleExprEquality3711 = new BitSet(new long[]{0x0000018000000002L});
    public static final BitSet FOLLOW_39_in_ruleExprEquality3740 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_40_in_ruleExprEquality3769 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_ruleExprComparison_in_ruleExprEquality3806 = new BitSet(new long[]{0x0000018000000002L});
    public static final BitSet FOLLOW_ruleExprComparison_in_entryRuleExprComparison3844 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprComparison3854 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprAdditive_in_ruleExprComparison3901 = new BitSet(new long[]{0x00001E0000000002L});
    public static final BitSet FOLLOW_41_in_ruleExprComparison3930 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_42_in_ruleExprComparison3959 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_43_in_ruleExprComparison3988 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_44_in_ruleExprComparison4017 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_ruleExprAdditive_in_ruleExprComparison4054 = new BitSet(new long[]{0x00001E0000000002L});
    public static final BitSet FOLLOW_ruleExprAdditive_in_entryRuleExprAdditive4092 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprAdditive4102 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprMult_in_ruleExprAdditive4149 = new BitSet(new long[]{0x0000600000000002L});
    public static final BitSet FOLLOW_45_in_ruleExprAdditive4178 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_46_in_ruleExprAdditive4207 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_ruleExprMult_in_ruleExprAdditive4244 = new BitSet(new long[]{0x0000600000000002L});
    public static final BitSet FOLLOW_ruleExprMult_in_entryRuleExprMult4282 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprMult4292 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprSign_in_ruleExprMult4339 = new BitSet(new long[]{0x000F800000000002L});
    public static final BitSet FOLLOW_47_in_ruleExprMult4368 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_48_in_ruleExprMult4397 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_49_in_ruleExprMult4426 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_50_in_ruleExprMult4455 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_51_in_ruleExprMult4484 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_ruleExprSign_in_ruleExprMult4521 = new BitSet(new long[]{0x000F800000000002L});
    public static final BitSet FOLLOW_ruleExprSign_in_entryRuleExprSign4559 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprSign4569 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_45_in_ruleExprSign4624 = new BitSet(new long[]{0x00300000100003A0L});
    public static final BitSet FOLLOW_46_in_ruleExprSign4653 = new BitSet(new long[]{0x00300000100003A0L});
    public static final BitSet FOLLOW_ruleExprNot_in_ruleExprSign4690 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprNot_in_ruleExprSign4719 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprNot_in_entryRuleExprNot4754 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprNot4764 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_52_in_ruleExprNot4817 = new BitSet(new long[]{0x00200000100003A0L});
    public static final BitSet FOLLOW_ruleExprCustomOperator_in_ruleExprNot4851 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprCustomOperator_in_ruleExprNot4880 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprCustomOperator_in_entryRuleExprCustomOperator4915 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprCustomOperator4925 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprMember_in_ruleExprCustomOperator4972 = new BitSet(new long[]{0x0000000000000042L});
    public static final BitSet FOLLOW_RULE_OPERATOR_in_ruleExprCustomOperator4998 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleExprCustomOperator5024 = new BitSet(new long[]{0x0000000000000042L});
    public static final BitSet FOLLOW_ruleExprMember_in_entryRuleExprMember5062 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprMember5072 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprAtomic_in_ruleExprMember5119 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_18_in_ruleExprMember5146 = new BitSet(new long[]{0x00200000100003A0L});
    public static final BitSet FOLLOW_ruleExprAtomic_in_ruleExprMember5180 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_ruleExprAtomic_in_entryRuleExprAtomic5218 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprAtomic5228 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleExprAtomic5280 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_ruleExprList_in_ruleExprAtomic5306 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleExprAtomic5340 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_ruleExprAtomic5355 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_ruleExprAtomic5365 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleExprAtomic5399 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_ruleExprAtomic5422 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleExprAtomic5444 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_ruleExprAtomic5453 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_INT_in_ruleExprAtomic5487 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_NUMBER_in_ruleExprAtomic5526 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_STRING_in_ruleExprAtomic5565 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_53_in_ruleExprAtomic5597 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleExprAtomic5614 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_ruleExprList_in_ruleExprAtomic5641 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_ruleExprAtomic5658 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_ruleExprAtomic5668 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_53_in_ruleExprAtomic5697 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_ruleExprAtomic5707 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleExprAtomic5728 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_RULE_OPERATOR_in_ruleExprAtomic5745 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleExprAtomic5771 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_ruleExprAtomic5781 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleExprList_in_entryRuleExprList5818 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleExprList5828 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_ruleExprList5863 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleExprList5884 = new BitSet(new long[]{0x0000000060000000L});
    public static final BitSet FOLLOW_29_in_ruleExprList5895 = new BitSet(new long[]{0x00306000100003A0L});
    public static final BitSet FOLLOW_ruleExpr_in_ruleExprList5916 = new BitSet(new long[]{0x0000000060000000L});
    public static final BitSet FOLLOW_30_in_ruleExprList5928 = new BitSet(new long[]{0x0000000000000002L});

}