package de.peeeq.pscript.serializer;

import com.google.inject.Inject;
import de.peeeq.pscript.services.PscriptGrammarAccess;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.AbstractElementAlias;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.AlternativeAlias;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.GroupAlias;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.TokenAlias;
import org.eclipse.xtext.serializer.analysis.ISyntacticSequencerPDAProvider.ISynTransition;
import org.eclipse.xtext.serializer.sequencer.AbstractSyntacticSequencer;

@SuppressWarnings("restriction")
public class AbstractPscriptSyntacticSequencer extends AbstractSyntacticSequencer {

	protected PscriptGrammarAccess grammarAccess;
	protected AbstractElementAlias match_ClassDef_NLTerminalRuleCall_4_0_a;
	protected AbstractElementAlias match_ClassDef_NLTerminalRuleCall_4_0_p;
	protected AbstractElementAlias match_ExprSingle_LeftParenthesisKeyword_1_0_a;
	protected AbstractElementAlias match_ExprSingle_LeftParenthesisKeyword_1_0_p;
	protected AbstractElementAlias match_FuncDef___LeftParenthesisKeyword_2_0_0_RightParenthesisKeyword_2_0_2___or___TakesKeyword_2_1_0_NothingKeyword_2_1_1__;
	protected AbstractElementAlias match_NativeFunc_ConstantKeyword_1_q;
	protected AbstractElementAlias match_NativeFunc___LeftParenthesisKeyword_4_0_0_RightParenthesisKeyword_4_0_2___or___TakesKeyword_4_1_0_NothingKeyword_4_1_1__;
	protected AbstractElementAlias match_PackageDeclaration_NLTerminalRuleCall_5_a;
	protected AbstractElementAlias match_PackageDeclaration_NLTerminalRuleCall_5_p;
	protected AbstractElementAlias match_PackageDeclaration_NLTerminalRuleCall_6_1_a;
	protected AbstractElementAlias match_Program_NLTerminalRuleCall_0_p;
	protected AbstractElementAlias match_Program_NLTerminalRuleCall_3_p;
	protected AbstractElementAlias match_Statements_NLTerminalRuleCall_1_0_p;
	protected AbstractElementAlias match_StmtIf_ThenKeyword_2_q;
	protected AbstractElementAlias match_StmtIf_ThenKeyword_5_2_q;
	
	@Inject
	protected void init(IGrammarAccess access) {
		grammarAccess = (PscriptGrammarAccess) access;
		match_ClassDef_NLTerminalRuleCall_4_0_a = new TokenAlias(true, true, grammarAccess.getClassDefAccess().getNLTerminalRuleCall_4_0());
		match_ClassDef_NLTerminalRuleCall_4_0_p = new TokenAlias(false, true, grammarAccess.getClassDefAccess().getNLTerminalRuleCall_4_0());
		match_ExprSingle_LeftParenthesisKeyword_1_0_a = new TokenAlias(true, true, grammarAccess.getExprSingleAccess().getLeftParenthesisKeyword_1_0());
		match_ExprSingle_LeftParenthesisKeyword_1_0_p = new TokenAlias(false, true, grammarAccess.getExprSingleAccess().getLeftParenthesisKeyword_1_0());
		match_FuncDef___LeftParenthesisKeyword_2_0_0_RightParenthesisKeyword_2_0_2___or___TakesKeyword_2_1_0_NothingKeyword_2_1_1__ = new AlternativeAlias(false, false, new GroupAlias(false, false, new TokenAlias(false, false, grammarAccess.getFuncDefAccess().getLeftParenthesisKeyword_2_0_0()), new TokenAlias(false, false, grammarAccess.getFuncDefAccess().getRightParenthesisKeyword_2_0_2())), new GroupAlias(false, false, new TokenAlias(false, false, grammarAccess.getFuncDefAccess().getTakesKeyword_2_1_0()), new TokenAlias(false, false, grammarAccess.getFuncDefAccess().getNothingKeyword_2_1_1())));
		match_NativeFunc_ConstantKeyword_1_q = new TokenAlias(true, false, grammarAccess.getNativeFuncAccess().getConstantKeyword_1());
		match_NativeFunc___LeftParenthesisKeyword_4_0_0_RightParenthesisKeyword_4_0_2___or___TakesKeyword_4_1_0_NothingKeyword_4_1_1__ = new AlternativeAlias(false, false, new GroupAlias(false, false, new TokenAlias(false, false, grammarAccess.getNativeFuncAccess().getLeftParenthesisKeyword_4_0_0()), new TokenAlias(false, false, grammarAccess.getNativeFuncAccess().getRightParenthesisKeyword_4_0_2())), new GroupAlias(false, false, new TokenAlias(false, false, grammarAccess.getNativeFuncAccess().getTakesKeyword_4_1_0()), new TokenAlias(false, false, grammarAccess.getNativeFuncAccess().getNothingKeyword_4_1_1())));
		match_PackageDeclaration_NLTerminalRuleCall_5_a = new TokenAlias(true, true, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_5());
		match_PackageDeclaration_NLTerminalRuleCall_5_p = new TokenAlias(false, true, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_5());
		match_PackageDeclaration_NLTerminalRuleCall_6_1_a = new TokenAlias(true, true, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_6_1());
		match_Program_NLTerminalRuleCall_0_p = new TokenAlias(false, true, grammarAccess.getProgramAccess().getNLTerminalRuleCall_0());
		match_Program_NLTerminalRuleCall_3_p = new TokenAlias(false, true, grammarAccess.getProgramAccess().getNLTerminalRuleCall_3());
		match_Statements_NLTerminalRuleCall_1_0_p = new TokenAlias(false, true, grammarAccess.getStatementsAccess().getNLTerminalRuleCall_1_0());
		match_StmtIf_ThenKeyword_2_q = new TokenAlias(true, false, grammarAccess.getStmtIfAccess().getThenKeyword_2());
		match_StmtIf_ThenKeyword_5_2_q = new TokenAlias(true, false, grammarAccess.getStmtIfAccess().getThenKeyword_5_2());
	}
	
	@Override
	protected String getUnassignedRuleCallToken(RuleCall ruleCall, INode node) {
		if(ruleCall.getRule() == grammarAccess.getNLRule())
			return getNLToken(ruleCall, node);
		return "";
	}
	
	protected String getNLToken(RuleCall ruleCall, INode node) {
		if (node != null)
			return getTokenText(node);
		return "\n\r";
	}
	
	@Override
	protected void emitUnassignedTokens(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		if (!transition.isSyntacticallyAmbiguous())
			return;
		if(match_ClassDef_NLTerminalRuleCall_4_0_a.equals(transition.getAmbiguousSyntax()))
			emit_ClassDef_NLTerminalRuleCall_4_0_a(semanticObject, transition, fromNode, toNode);
		else if(match_ClassDef_NLTerminalRuleCall_4_0_p.equals(transition.getAmbiguousSyntax()))
			emit_ClassDef_NLTerminalRuleCall_4_0_p(semanticObject, transition, fromNode, toNode);
		else if(match_ExprSingle_LeftParenthesisKeyword_1_0_a.equals(transition.getAmbiguousSyntax()))
			emit_ExprSingle_LeftParenthesisKeyword_1_0_a(semanticObject, transition, fromNode, toNode);
		else if(match_ExprSingle_LeftParenthesisKeyword_1_0_p.equals(transition.getAmbiguousSyntax()))
			emit_ExprSingle_LeftParenthesisKeyword_1_0_p(semanticObject, transition, fromNode, toNode);
		else if(match_FuncDef___LeftParenthesisKeyword_2_0_0_RightParenthesisKeyword_2_0_2___or___TakesKeyword_2_1_0_NothingKeyword_2_1_1__.equals(transition.getAmbiguousSyntax()))
			emit_FuncDef___LeftParenthesisKeyword_2_0_0_RightParenthesisKeyword_2_0_2___or___TakesKeyword_2_1_0_NothingKeyword_2_1_1__(semanticObject, transition, fromNode, toNode);
		else if(match_NativeFunc_ConstantKeyword_1_q.equals(transition.getAmbiguousSyntax()))
			emit_NativeFunc_ConstantKeyword_1_q(semanticObject, transition, fromNode, toNode);
		else if(match_NativeFunc___LeftParenthesisKeyword_4_0_0_RightParenthesisKeyword_4_0_2___or___TakesKeyword_4_1_0_NothingKeyword_4_1_1__.equals(transition.getAmbiguousSyntax()))
			emit_NativeFunc___LeftParenthesisKeyword_4_0_0_RightParenthesisKeyword_4_0_2___or___TakesKeyword_4_1_0_NothingKeyword_4_1_1__(semanticObject, transition, fromNode, toNode);
		else if(match_PackageDeclaration_NLTerminalRuleCall_5_a.equals(transition.getAmbiguousSyntax()))
			emit_PackageDeclaration_NLTerminalRuleCall_5_a(semanticObject, transition, fromNode, toNode);
		else if(match_PackageDeclaration_NLTerminalRuleCall_5_p.equals(transition.getAmbiguousSyntax()))
			emit_PackageDeclaration_NLTerminalRuleCall_5_p(semanticObject, transition, fromNode, toNode);
		else if(match_PackageDeclaration_NLTerminalRuleCall_6_1_a.equals(transition.getAmbiguousSyntax()))
			emit_PackageDeclaration_NLTerminalRuleCall_6_1_a(semanticObject, transition, fromNode, toNode);
		else if(match_Program_NLTerminalRuleCall_0_p.equals(transition.getAmbiguousSyntax()))
			emit_Program_NLTerminalRuleCall_0_p(semanticObject, transition, fromNode, toNode);
		else if(match_Program_NLTerminalRuleCall_3_p.equals(transition.getAmbiguousSyntax()))
			emit_Program_NLTerminalRuleCall_3_p(semanticObject, transition, fromNode, toNode);
		else if(match_Statements_NLTerminalRuleCall_1_0_p.equals(transition.getAmbiguousSyntax()))
			emit_Statements_NLTerminalRuleCall_1_0_p(semanticObject, transition, fromNode, toNode);
		else if(match_StmtIf_ThenKeyword_2_q.equals(transition.getAmbiguousSyntax()))
			emit_StmtIf_ThenKeyword_2_q(semanticObject, transition, fromNode, toNode);
		else if(match_StmtIf_ThenKeyword_5_2_q.equals(transition.getAmbiguousSyntax()))
			emit_StmtIf_ThenKeyword_5_2_q(semanticObject, transition, fromNode, toNode);
		else acceptNodes(transition, fromNode, toNode);
	}

	/**
	 * Syntax:
	 *     NL*
	 */
	protected void emit_ClassDef_NLTerminalRuleCall_4_0_a(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
	/**
	 * Syntax:
	 *     NL+
	 */
	protected void emit_ClassDef_NLTerminalRuleCall_4_0_p(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
	/**
	 * Syntax:
	 *     '('*
	 */
	protected void emit_ExprSingle_LeftParenthesisKeyword_1_0_a(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
	/**
	 * Syntax:
	 *     '('+
	 */
	protected void emit_ExprSingle_LeftParenthesisKeyword_1_0_p(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
	/**
	 * Syntax:
	 *     ('takes' 'nothing') | ('(' ')')
	 */
	protected void emit_FuncDef___LeftParenthesisKeyword_2_0_0_RightParenthesisKeyword_2_0_2___or___TakesKeyword_2_1_0_NothingKeyword_2_1_1__(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
	/**
	 * Syntax:
	 *     'constant'?
	 */
	protected void emit_NativeFunc_ConstantKeyword_1_q(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
	/**
	 * Syntax:
	 *     ('takes' 'nothing') | ('(' ')')
	 */
	protected void emit_NativeFunc___LeftParenthesisKeyword_4_0_0_RightParenthesisKeyword_4_0_2___or___TakesKeyword_4_1_0_NothingKeyword_4_1_1__(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
	/**
	 * Syntax:
	 *     NL*
	 */
	protected void emit_PackageDeclaration_NLTerminalRuleCall_5_a(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
	/**
	 * Syntax:
	 *     NL+
	 */
	protected void emit_PackageDeclaration_NLTerminalRuleCall_5_p(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
	/**
	 * Syntax:
	 *     NL*
	 */
	protected void emit_PackageDeclaration_NLTerminalRuleCall_6_1_a(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
	/**
	 * Syntax:
	 *     NL+
	 */
	protected void emit_Program_NLTerminalRuleCall_0_p(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
	/**
	 * Syntax:
	 *     NL+
	 */
	protected void emit_Program_NLTerminalRuleCall_3_p(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
	/**
	 * Syntax:
	 *     NL+
	 */
	protected void emit_Statements_NLTerminalRuleCall_1_0_p(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
	/**
	 * Syntax:
	 *     'then'?
	 */
	protected void emit_StmtIf_ThenKeyword_2_q(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
	/**
	 * Syntax:
	 *     'then'?
	 */
	protected void emit_StmtIf_ThenKeyword_5_2_q(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
}
