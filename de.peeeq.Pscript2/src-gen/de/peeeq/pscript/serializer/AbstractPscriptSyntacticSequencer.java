package de.peeeq.pscript.serializer;

import com.google.inject.Inject;
import de.peeeq.pscript.services.PscriptGrammarAccess;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.AbstractElementAlias;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.GroupAlias;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.TokenAlias;
import org.eclipse.xtext.serializer.analysis.ISyntacticSequencerPDAProvider.ISynTransition;
import org.eclipse.xtext.serializer.sequencer.AbstractSyntacticSequencer;

@SuppressWarnings("restriction")
public class AbstractPscriptSyntacticSequencer extends AbstractSyntacticSequencer {

	protected PscriptGrammarAccess grammarAccess;
	protected AbstractElementAlias match_ClassDef_NLTerminalRuleCall_5_a;
	protected AbstractElementAlias match_ClassMember_NLTerminalRuleCall_0_a;
	protected AbstractElementAlias match_ExprSingle_LeftParenthesisKeyword_1_0_a;
	protected AbstractElementAlias match_ExprSingle_LeftParenthesisKeyword_1_0_p;
	protected AbstractElementAlias match_PackageDeclaration_NLTerminalRuleCall_5_a_RightCurlyBracketKeyword_7_NLTerminalRuleCall_8_p;
	protected AbstractElementAlias match_PackageDeclaration_NLTerminalRuleCall_6_1_a_RightCurlyBracketKeyword_7_NLTerminalRuleCall_8_p;
	protected AbstractElementAlias match_Program_NLTerminalRuleCall_3_p;
	protected AbstractElementAlias match_Statements_NLTerminalRuleCall_1_0_a;
	
	@Inject
	protected void init(IGrammarAccess access) {
		grammarAccess = (PscriptGrammarAccess) access;
		match_ClassDef_NLTerminalRuleCall_5_a = new TokenAlias(true, true, grammarAccess.getClassDefAccess().getNLTerminalRuleCall_5());
		match_ClassMember_NLTerminalRuleCall_0_a = new TokenAlias(true, true, grammarAccess.getClassMemberAccess().getNLTerminalRuleCall_0());
		match_ExprSingle_LeftParenthesisKeyword_1_0_a = new TokenAlias(true, true, grammarAccess.getExprSingleAccess().getLeftParenthesisKeyword_1_0());
		match_ExprSingle_LeftParenthesisKeyword_1_0_p = new TokenAlias(false, true, grammarAccess.getExprSingleAccess().getLeftParenthesisKeyword_1_0());
		match_PackageDeclaration_NLTerminalRuleCall_5_a_RightCurlyBracketKeyword_7_NLTerminalRuleCall_8_p = new GroupAlias(false, false, new TokenAlias(true, true, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_5()), new TokenAlias(false, false, grammarAccess.getPackageDeclarationAccess().getRightCurlyBracketKeyword_7()), new TokenAlias(false, true, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_8()));
		match_PackageDeclaration_NLTerminalRuleCall_6_1_a_RightCurlyBracketKeyword_7_NLTerminalRuleCall_8_p = new GroupAlias(false, false, new TokenAlias(true, true, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_6_1()), new TokenAlias(false, false, grammarAccess.getPackageDeclarationAccess().getRightCurlyBracketKeyword_7()), new TokenAlias(false, true, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_8()));
		match_Program_NLTerminalRuleCall_3_p = new TokenAlias(false, true, grammarAccess.getProgramAccess().getNLTerminalRuleCall_3());
		match_Statements_NLTerminalRuleCall_1_0_a = new TokenAlias(true, true, grammarAccess.getStatementsAccess().getNLTerminalRuleCall_1_0());
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
		if(match_ClassDef_NLTerminalRuleCall_5_a.equals(transition.getAmbiguousSyntax()))
			emit_ClassDef_NLTerminalRuleCall_5_a(semanticObject, transition, fromNode, toNode);
		else if(match_ClassMember_NLTerminalRuleCall_0_a.equals(transition.getAmbiguousSyntax()))
			emit_ClassMember_NLTerminalRuleCall_0_a(semanticObject, transition, fromNode, toNode);
		else if(match_ExprSingle_LeftParenthesisKeyword_1_0_a.equals(transition.getAmbiguousSyntax()))
			emit_ExprSingle_LeftParenthesisKeyword_1_0_a(semanticObject, transition, fromNode, toNode);
		else if(match_ExprSingle_LeftParenthesisKeyword_1_0_p.equals(transition.getAmbiguousSyntax()))
			emit_ExprSingle_LeftParenthesisKeyword_1_0_p(semanticObject, transition, fromNode, toNode);
		else if(match_PackageDeclaration_NLTerminalRuleCall_5_a_RightCurlyBracketKeyword_7_NLTerminalRuleCall_8_p.equals(transition.getAmbiguousSyntax()))
			emit_PackageDeclaration_NLTerminalRuleCall_5_a_RightCurlyBracketKeyword_7_NLTerminalRuleCall_8_p(semanticObject, transition, fromNode, toNode);
		else if(match_PackageDeclaration_NLTerminalRuleCall_6_1_a_RightCurlyBracketKeyword_7_NLTerminalRuleCall_8_p.equals(transition.getAmbiguousSyntax()))
			emit_PackageDeclaration_NLTerminalRuleCall_6_1_a_RightCurlyBracketKeyword_7_NLTerminalRuleCall_8_p(semanticObject, transition, fromNode, toNode);
		else if(match_Program_NLTerminalRuleCall_3_p.equals(transition.getAmbiguousSyntax()))
			emit_Program_NLTerminalRuleCall_3_p(semanticObject, transition, fromNode, toNode);
		else if(match_Statements_NLTerminalRuleCall_1_0_a.equals(transition.getAmbiguousSyntax()))
			emit_Statements_NLTerminalRuleCall_1_0_a(semanticObject, transition, fromNode, toNode);
		else acceptNodes(transition, fromNode, toNode);
	}

	/**
	 * Syntax:
	 *     NL*
	 */
	protected void emit_ClassDef_NLTerminalRuleCall_5_a(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
	/**
	 * Syntax:
	 *     NL*
	 */
	protected void emit_ClassMember_NLTerminalRuleCall_0_a(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
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
	 *     NL* '}' NL+
	 */
	protected void emit_PackageDeclaration_NLTerminalRuleCall_5_a_RightCurlyBracketKeyword_7_NLTerminalRuleCall_8_p(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
	/**
	 * Syntax:
	 *     NL* '}' NL+
	 */
	protected void emit_PackageDeclaration_NLTerminalRuleCall_6_1_a_RightCurlyBracketKeyword_7_NLTerminalRuleCall_8_p(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
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
	 *     NL*
	 */
	protected void emit_Statements_NLTerminalRuleCall_1_0_a(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
}
