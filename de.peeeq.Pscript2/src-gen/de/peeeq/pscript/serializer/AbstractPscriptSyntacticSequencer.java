package de.peeeq.pscript.serializer;

import com.google.inject.Inject;
import de.peeeq.pscript.services.PscriptGrammarAccess;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.AbstractElementAlias;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.GroupAlias;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.TokenAlias;
import org.eclipse.xtext.serializer.analysis.ISyntacticSequencerPDAProvider.ISynNavigable;
import org.eclipse.xtext.serializer.analysis.ISyntacticSequencerPDAProvider.ISynTransition;
import org.eclipse.xtext.serializer.sequencer.AbstractSyntacticSequencer;

@SuppressWarnings("restriction")
public class AbstractPscriptSyntacticSequencer extends AbstractSyntacticSequencer {

	protected PscriptGrammarAccess grammarAccess;
	protected AbstractElementAlias match_ClassDef_NLTerminalRuleCall_5_0_a;
	protected AbstractElementAlias match_ExprMemberRight___LeftParenthesisKeyword_1_0_RightParenthesisKeyword_1_2__q;
	protected AbstractElementAlias match_ExprSingle_LeftParenthesisKeyword_1_0_a;
	protected AbstractElementAlias match_ExprSingle_LeftParenthesisKeyword_1_0_p;
	protected AbstractElementAlias match_PackageDeclaration_NLTerminalRuleCall_5_0_a;
	protected AbstractElementAlias match_PackageDeclaration_NLTerminalRuleCall_6_a;
	protected AbstractElementAlias match_PackageDeclaration_NLTerminalRuleCall_7_1_a;
	protected AbstractElementAlias match_Program_NLTerminalRuleCall_0_a;
	protected AbstractElementAlias match_Program_NLTerminalRuleCall_3_a;
	protected AbstractElementAlias match_Statements_NLTerminalRuleCall_1_0_a;
	
	@Inject
	protected void init(IGrammarAccess access) {
		grammarAccess = (PscriptGrammarAccess) access;
		match_ClassDef_NLTerminalRuleCall_5_0_a = new TokenAlias(true, true, grammarAccess.getClassDefAccess().getNLTerminalRuleCall_5_0());
		match_ExprMemberRight___LeftParenthesisKeyword_1_0_RightParenthesisKeyword_1_2__q = new GroupAlias(true, false, new TokenAlias(false, false, grammarAccess.getExprMemberRightAccess().getLeftParenthesisKeyword_1_0()), new TokenAlias(false, false, grammarAccess.getExprMemberRightAccess().getRightParenthesisKeyword_1_2()));
		match_ExprSingle_LeftParenthesisKeyword_1_0_a = new TokenAlias(true, true, grammarAccess.getExprSingleAccess().getLeftParenthesisKeyword_1_0());
		match_ExprSingle_LeftParenthesisKeyword_1_0_p = new TokenAlias(false, true, grammarAccess.getExprSingleAccess().getLeftParenthesisKeyword_1_0());
		match_PackageDeclaration_NLTerminalRuleCall_5_0_a = new TokenAlias(true, true, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_5_0());
		match_PackageDeclaration_NLTerminalRuleCall_6_a = new TokenAlias(true, true, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_6());
		match_PackageDeclaration_NLTerminalRuleCall_7_1_a = new TokenAlias(true, true, grammarAccess.getPackageDeclarationAccess().getNLTerminalRuleCall_7_1());
		match_Program_NLTerminalRuleCall_0_a = new TokenAlias(true, true, grammarAccess.getProgramAccess().getNLTerminalRuleCall_0());
		match_Program_NLTerminalRuleCall_3_a = new TokenAlias(true, true, grammarAccess.getProgramAccess().getNLTerminalRuleCall_3());
		match_Statements_NLTerminalRuleCall_1_0_a = new TokenAlias(true, true, grammarAccess.getStatementsAccess().getNLTerminalRuleCall_1_0());
	}
	
	@Override
	protected String getUnassignedRuleCallToken(EObject semanticObject, RuleCall ruleCall, INode node) {
		if(ruleCall.getRule() == grammarAccess.getNLRule())
			return getNLToken(semanticObject, ruleCall, node);
		return "";
	}
	
	protected String getNLToken(EObject semanticObject, RuleCall ruleCall, INode node) {
		if (node != null)
			return getTokenText(node);
		return "\n\r";
	}
	
	@Override
	protected void emitUnassignedTokens(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		if (transition.getAmbiguousSyntaxes().isEmpty()) return;
		List<INode> transitionNodes = collectNodes(fromNode, toNode);
		for (AbstractElementAlias syntax : transition.getAmbiguousSyntaxes()) {
			List<INode> syntaxNodes = getNodesFor(transitionNodes, syntax);
			if(match_ClassDef_NLTerminalRuleCall_5_0_a.equals(syntax))
				emit_ClassDef_NLTerminalRuleCall_5_0_a(semanticObject, getLastNavigableState(), syntaxNodes);
			else if(match_ExprMemberRight___LeftParenthesisKeyword_1_0_RightParenthesisKeyword_1_2__q.equals(syntax))
				emit_ExprMemberRight___LeftParenthesisKeyword_1_0_RightParenthesisKeyword_1_2__q(semanticObject, getLastNavigableState(), syntaxNodes);
			else if(match_ExprSingle_LeftParenthesisKeyword_1_0_a.equals(syntax))
				emit_ExprSingle_LeftParenthesisKeyword_1_0_a(semanticObject, getLastNavigableState(), syntaxNodes);
			else if(match_ExprSingle_LeftParenthesisKeyword_1_0_p.equals(syntax))
				emit_ExprSingle_LeftParenthesisKeyword_1_0_p(semanticObject, getLastNavigableState(), syntaxNodes);
			else if(match_PackageDeclaration_NLTerminalRuleCall_5_0_a.equals(syntax))
				emit_PackageDeclaration_NLTerminalRuleCall_5_0_a(semanticObject, getLastNavigableState(), syntaxNodes);
			else if(match_PackageDeclaration_NLTerminalRuleCall_6_a.equals(syntax))
				emit_PackageDeclaration_NLTerminalRuleCall_6_a(semanticObject, getLastNavigableState(), syntaxNodes);
			else if(match_PackageDeclaration_NLTerminalRuleCall_7_1_a.equals(syntax))
				emit_PackageDeclaration_NLTerminalRuleCall_7_1_a(semanticObject, getLastNavigableState(), syntaxNodes);
			else if(match_Program_NLTerminalRuleCall_0_a.equals(syntax))
				emit_Program_NLTerminalRuleCall_0_a(semanticObject, getLastNavigableState(), syntaxNodes);
			else if(match_Program_NLTerminalRuleCall_3_a.equals(syntax))
				emit_Program_NLTerminalRuleCall_3_a(semanticObject, getLastNavigableState(), syntaxNodes);
			else if(match_Statements_NLTerminalRuleCall_1_0_a.equals(syntax))
				emit_Statements_NLTerminalRuleCall_1_0_a(semanticObject, getLastNavigableState(), syntaxNodes);
			else acceptNodes(getLastNavigableState(), syntaxNodes);
		}
	}

	/**
	 * Syntax:
	 *     NL*
	 */
	protected void emit_ClassDef_NLTerminalRuleCall_5_0_a(EObject semanticObject, ISynNavigable transition, List<INode> nodes) {
		acceptNodes(transition, nodes);
	}
	
	/**
	 * Syntax:
	 *     ('(' ')')?
	 */
	protected void emit_ExprMemberRight___LeftParenthesisKeyword_1_0_RightParenthesisKeyword_1_2__q(EObject semanticObject, ISynNavigable transition, List<INode> nodes) {
		acceptNodes(transition, nodes);
	}
	
	/**
	 * Syntax:
	 *     '('*
	 */
	protected void emit_ExprSingle_LeftParenthesisKeyword_1_0_a(EObject semanticObject, ISynNavigable transition, List<INode> nodes) {
		acceptNodes(transition, nodes);
	}
	
	/**
	 * Syntax:
	 *     '('+
	 */
	protected void emit_ExprSingle_LeftParenthesisKeyword_1_0_p(EObject semanticObject, ISynNavigable transition, List<INode> nodes) {
		acceptNodes(transition, nodes);
	}
	
	/**
	 * Syntax:
	 *     NL*
	 */
	protected void emit_PackageDeclaration_NLTerminalRuleCall_5_0_a(EObject semanticObject, ISynNavigable transition, List<INode> nodes) {
		acceptNodes(transition, nodes);
	}
	
	/**
	 * Syntax:
	 *     NL*
	 */
	protected void emit_PackageDeclaration_NLTerminalRuleCall_6_a(EObject semanticObject, ISynNavigable transition, List<INode> nodes) {
		acceptNodes(transition, nodes);
	}
	
	/**
	 * Syntax:
	 *     NL*
	 */
	protected void emit_PackageDeclaration_NLTerminalRuleCall_7_1_a(EObject semanticObject, ISynNavigable transition, List<INode> nodes) {
		acceptNodes(transition, nodes);
	}
	
	/**
	 * Syntax:
	 *     NL*
	 */
	protected void emit_Program_NLTerminalRuleCall_0_a(EObject semanticObject, ISynNavigable transition, List<INode> nodes) {
		acceptNodes(transition, nodes);
	}
	
	/**
	 * Syntax:
	 *     NL*
	 */
	protected void emit_Program_NLTerminalRuleCall_3_a(EObject semanticObject, ISynNavigable transition, List<INode> nodes) {
		acceptNodes(transition, nodes);
	}
	
	/**
	 * Syntax:
	 *     NL*
	 */
	protected void emit_Statements_NLTerminalRuleCall_1_0_a(EObject semanticObject, ISynNavigable transition, List<INode> nodes) {
		acceptNodes(transition, nodes);
	}
	
}
