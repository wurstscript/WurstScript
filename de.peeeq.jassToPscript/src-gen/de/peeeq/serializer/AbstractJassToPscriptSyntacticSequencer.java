package de.peeeq.serializer;

import com.google.inject.Inject;
import de.peeeq.services.JassToPscriptGrammarAccess;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.AbstractElementAlias;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.TokenAlias;
import org.eclipse.xtext.serializer.analysis.ISyntacticSequencerPDAProvider.ISynTransition;
import org.eclipse.xtext.serializer.sequencer.AbstractSyntacticSequencer;

@SuppressWarnings("restriction")
public class AbstractJassToPscriptSyntacticSequencer extends AbstractSyntacticSequencer {

	protected JassToPscriptGrammarAccess grammarAccess;
	protected AbstractElementAlias match_NativeDef_ConstantKeyword_0_q;
	
	@Inject
	protected void init(IGrammarAccess access) {
		grammarAccess = (JassToPscriptGrammarAccess) access;
		match_NativeDef_ConstantKeyword_0_q = new TokenAlias(true, false, grammarAccess.getNativeDefAccess().getConstantKeyword_0());
	}
	
	@Override
	protected String getUnassignedRuleCallToken(RuleCall ruleCall, INode node) {
		return "";
	}
	
	
	@Override
	protected void emitUnassignedTokens(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		if (!transition.isSyntacticallyAmbiguous())
			return;
		if(match_NativeDef_ConstantKeyword_0_q.equals(transition.getAmbiguousSyntax()))
			emit_NativeDef_ConstantKeyword_0_q(semanticObject, transition, fromNode, toNode);
		else acceptNodes(transition, fromNode, toNode);
	}

	/**
	 * Syntax:
	 *     'constant'?
	 */
	protected void emit_NativeDef_ConstantKeyword_0_q(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		acceptNodes(transition, fromNode, toNode);
	}
	
}
