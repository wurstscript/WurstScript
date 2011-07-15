package de.peeeq.pscript.serializer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import de.peeeq.pscript.pscript.ClassDef;
import de.peeeq.pscript.pscript.ExprAdditive;
import de.peeeq.pscript.pscript.ExprAnd;
import de.peeeq.pscript.pscript.ExprAssignment;
import de.peeeq.pscript.pscript.ExprBoolVal;
import de.peeeq.pscript.pscript.ExprBuildinFunction;
import de.peeeq.pscript.pscript.ExprComparison;
import de.peeeq.pscript.pscript.ExprEquality;
import de.peeeq.pscript.pscript.ExprFunctioncall;
import de.peeeq.pscript.pscript.ExprIdentifier;
import de.peeeq.pscript.pscript.ExprIntVal;
import de.peeeq.pscript.pscript.ExprList;
import de.peeeq.pscript.pscript.ExprMember;
import de.peeeq.pscript.pscript.ExprMult;
import de.peeeq.pscript.pscript.ExprNot;
import de.peeeq.pscript.pscript.ExprNumVal;
import de.peeeq.pscript.pscript.ExprOr;
import de.peeeq.pscript.pscript.ExprSign;
import de.peeeq.pscript.pscript.ExprStrval;
import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.Import;
import de.peeeq.pscript.pscript.InitBlock;
import de.peeeq.pscript.pscript.NativeType;
import de.peeeq.pscript.pscript.OpAssign;
import de.peeeq.pscript.pscript.OpDivReal;
import de.peeeq.pscript.pscript.OpEquals;
import de.peeeq.pscript.pscript.OpGreater;
import de.peeeq.pscript.pscript.OpGreaterEq;
import de.peeeq.pscript.pscript.OpLess;
import de.peeeq.pscript.pscript.OpLessEq;
import de.peeeq.pscript.pscript.OpMinus;
import de.peeeq.pscript.pscript.OpMinusAssign;
import de.peeeq.pscript.pscript.OpModInt;
import de.peeeq.pscript.pscript.OpModReal;
import de.peeeq.pscript.pscript.OpMult;
import de.peeeq.pscript.pscript.OpPlus;
import de.peeeq.pscript.pscript.OpPlusAssign;
import de.peeeq.pscript.pscript.OpUnequals;
import de.peeeq.pscript.pscript.PackageDeclaration;
import de.peeeq.pscript.pscript.ParameterDef;
import de.peeeq.pscript.pscript.Program;
import de.peeeq.pscript.pscript.PscriptPackage;
import de.peeeq.pscript.pscript.Statements;
import de.peeeq.pscript.pscript.StmtExpr;
import de.peeeq.pscript.pscript.StmtIf;
import de.peeeq.pscript.pscript.StmtReturn;
import de.peeeq.pscript.pscript.StmtWhile;
import de.peeeq.pscript.pscript.TypeExpr;
import de.peeeq.pscript.pscript.VarDef;
import de.peeeq.pscript.services.PscriptGrammarAccess;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.serializer.acceptor.ISemanticSequenceAcceptor;
import org.eclipse.xtext.serializer.acceptor.SequenceFeeder;
import org.eclipse.xtext.serializer.diagnostic.ISemanticSequencerDiagnosticProvider;
import org.eclipse.xtext.serializer.diagnostic.ISerializationDiagnostic.Acceptor;
import org.eclipse.xtext.serializer.sequencer.AbstractSemanticSequencer;
import org.eclipse.xtext.serializer.sequencer.GenericSequencer;
import org.eclipse.xtext.serializer.sequencer.ISemanticNodeProvider.INodesForEObjectProvider;
import org.eclipse.xtext.serializer.sequencer.ISemanticSequencer;
import org.eclipse.xtext.serializer.sequencer.ITransientValueService;
import org.eclipse.xtext.serializer.sequencer.ITransientValueService.ValueTransient;

@SuppressWarnings("restriction")
public class AbstractPscriptSemanticSequencer extends AbstractSemanticSequencer {

	@Inject
	protected PscriptGrammarAccess grammarAccess;
	
	@Inject
	protected ISemanticSequencerDiagnosticProvider diagnosticProvider;
	
	@Inject
	protected ITransientValueService transientValues;
	
	@Inject
	@GenericSequencer
	protected Provider<ISemanticSequencer> genericSequencerProvider;
	
	protected ISemanticSequencer genericSequencer;
	
	
	@Override
	public void init(ISemanticSequencer sequencer, ISemanticSequenceAcceptor sequenceAcceptor, Acceptor errorAcceptor) {
		super.init(sequencer, sequenceAcceptor, errorAcceptor);
		this.genericSequencer = genericSequencerProvider.get();
		this.genericSequencer.init(sequencer, sequenceAcceptor, errorAcceptor);
	}
	
	public void createSequence(EObject context, EObject semanticObject) {
		if(semanticObject.eClass().getEPackage() == PscriptPackage.eINSTANCE) switch(semanticObject.eClass().getClassifierID()) {
			case PscriptPackage.CLASS_DEF:
				if(context == grammarAccess.getEntityRule() ||
				   context == grammarAccess.getTypeDefRule() ||
				   context == grammarAccess.getClassDefRule()) {
					sequence_ClassDef_ClassDef(context, (ClassDef) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_ADDITIVE:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule()) {
					sequence_ExprAdditive_ExprAdditive(context, (ExprAdditive) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_AND:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule()) {
					sequence_ExprAnd_ExprAnd(context, (ExprAnd) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_ASSIGNMENT:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule()) {
					sequence_ExprAssignment_ExprAssignment(context, (ExprAssignment) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_BOOL_VAL:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule()) {
					sequence_ExprSingle_ExprBoolVal(context, (ExprBoolVal) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_BUILDIN_FUNCTION:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule()) {
					sequence_ExprSingle_ExprBuildinFunction(context, (ExprBuildinFunction) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_COMPARISON:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule()) {
					sequence_ExprComparison_ExprComparison(context, (ExprComparison) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_EQUALITY:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule()) {
					sequence_ExprEquality_ExprEquality(context, (ExprEquality) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_FUNCTIONCALL:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule() ||
				   context == grammarAccess.getExprAtomicRule()) {
					sequence_ExprAtomic_ExprFunctioncall(context, (ExprFunctioncall) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_IDENTIFIER:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule() ||
				   context == grammarAccess.getExprAtomicRule()) {
					sequence_ExprAtomic_ExprIdentifier(context, (ExprIdentifier) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_INT_VAL:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule()) {
					sequence_ExprSingle_ExprIntVal(context, (ExprIntVal) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_LIST:
				if(context == grammarAccess.getExprListRule()) {
					sequence_ExprList_ExprList(context, (ExprList) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_MEMBER:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule()) {
					sequence_ExprMember_ExprMember(context, (ExprMember) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_MULT:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule()) {
					sequence_ExprMult_ExprMult(context, (ExprMult) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_NOT:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule()) {
					sequence_ExprNot_ExprNot(context, (ExprNot) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_NUM_VAL:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule()) {
					sequence_ExprSingle_ExprNumVal(context, (ExprNumVal) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_OR:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule()) {
					sequence_ExprOr_ExprOr(context, (ExprOr) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_SIGN:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule()) {
					sequence_ExprSign_ExprSign(context, (ExprSign) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.EXPR_STRVAL:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getExprAssignmentRule() ||
				   context == grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0() ||
				   context == grammarAccess.getExprOrRule() ||
				   context == grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0() ||
				   context == grammarAccess.getExprAndRule() ||
				   context == grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0() ||
				   context == grammarAccess.getExprEqualityRule() ||
				   context == grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0() ||
				   context == grammarAccess.getExprComparisonRule() ||
				   context == grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0() ||
				   context == grammarAccess.getExprAdditiveRule() ||
				   context == grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0() ||
				   context == grammarAccess.getExprMultRule() ||
				   context == grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0() ||
				   context == grammarAccess.getExprSignRule() ||
				   context == grammarAccess.getExprNotRule() ||
				   context == grammarAccess.getExprMemberRule() ||
				   context == grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0() ||
				   context == grammarAccess.getExprSingleRule()) {
					sequence_ExprSingle_ExprStrval(context, (ExprStrval) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.FUNC_DEF:
				if(context == grammarAccess.getEntityRule() ||
				   context == grammarAccess.getClassMemberRule() ||
				   context == grammarAccess.getFuncDefRule()) {
					sequence_FuncDef_FuncDef(context, (FuncDef) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.IMPORT:
				if(context == grammarAccess.getImportRule()) {
					sequence_Import_Import(context, (Import) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.INIT_BLOCK:
				if(context == grammarAccess.getEntityRule() ||
				   context == grammarAccess.getInitBlockRule()) {
					sequence_InitBlock_InitBlock(context, (InitBlock) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.NATIVE_TYPE:
				if(context == grammarAccess.getEntityRule() ||
				   context == grammarAccess.getTypeDefRule() ||
				   context == grammarAccess.getNativeTypeRule()) {
					sequence_NativeType_NativeType(context, (NativeType) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.OP_ASSIGN:
				if(context == grammarAccess.getOpAssignmentRule()) {
					sequence_OpAssignment_OpAssign(context, (OpAssign) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.OP_DIV_REAL:
				if(context == grammarAccess.getOpMultiplicativeRule()) {
					sequence_OpMultiplicative_OpDivReal(context, (OpDivReal) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.OP_EQUALS:
				if(context == grammarAccess.getOpEqualityRule()) {
					sequence_OpEquality_OpEquals(context, (OpEquals) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.OP_GREATER:
				if(context == grammarAccess.getOpComparisonRule()) {
					sequence_OpComparison_OpGreater(context, (OpGreater) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.OP_GREATER_EQ:
				if(context == grammarAccess.getOpComparisonRule()) {
					sequence_OpComparison_OpGreaterEq(context, (OpGreaterEq) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.OP_LESS:
				if(context == grammarAccess.getOpComparisonRule()) {
					sequence_OpComparison_OpLess(context, (OpLess) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.OP_LESS_EQ:
				if(context == grammarAccess.getOpComparisonRule()) {
					sequence_OpComparison_OpLessEq(context, (OpLessEq) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.OP_MINUS:
				if(context == grammarAccess.getOpAdditiveRule()) {
					sequence_OpAdditive_OpMinus(context, (OpMinus) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.OP_MINUS_ASSIGN:
				if(context == grammarAccess.getOpAssignmentRule()) {
					sequence_OpAssignment_OpMinusAssign(context, (OpMinusAssign) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.OP_MOD_INT:
				if(context == grammarAccess.getOpMultiplicativeRule()) {
					sequence_OpMultiplicative_OpModInt(context, (OpModInt) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.OP_MOD_REAL:
				if(context == grammarAccess.getOpMultiplicativeRule()) {
					sequence_OpMultiplicative_OpModReal(context, (OpModReal) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.OP_MULT:
				if(context == grammarAccess.getOpMultiplicativeRule()) {
					sequence_OpMultiplicative_OpMult(context, (OpMult) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.OP_PLUS:
				if(context == grammarAccess.getOpAdditiveRule()) {
					sequence_OpAdditive_OpPlus(context, (OpPlus) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.OP_PLUS_ASSIGN:
				if(context == grammarAccess.getOpAssignmentRule()) {
					sequence_OpAssignment_OpPlusAssign(context, (OpPlusAssign) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.OP_UNEQUALS:
				if(context == grammarAccess.getOpEqualityRule()) {
					sequence_OpEquality_OpUnequals(context, (OpUnequals) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.PACKAGE_DECLARATION:
				if(context == grammarAccess.getPackageDeclarationRule()) {
					sequence_PackageDeclaration_PackageDeclaration(context, (PackageDeclaration) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.PARAMETER_DEF:
				if(context == grammarAccess.getParameterDefRule()) {
					sequence_ParameterDef_ParameterDef(context, (ParameterDef) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.PROGRAM:
				if(context == grammarAccess.getProgramRule()) {
					sequence_Program_Program(context, (Program) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.STATEMENTS:
				if(context == grammarAccess.getElseBlockRule()) {
					sequence_ElseBlock_Statements(context, (Statements) semanticObject); 
					return; 
				}
				else if(context == grammarAccess.getStatementsRule()) {
					sequence_Statements_Statements(context, (Statements) semanticObject); 
					return; 
				}
				else if(context == grammarAccess.getElseIfBlockRule()) {
					sequence_ElseIfBlock_Statements(context, (Statements) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.STMT_EXPR:
				if(context == grammarAccess.getStatementRule() ||
				   context == grammarAccess.getStmtExprRule()) {
					sequence_StmtExpr_StmtExpr(context, (StmtExpr) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.STMT_IF:
				if(context == grammarAccess.getStatementRule() ||
				   context == grammarAccess.getStmtIfRule()) {
					sequence_StmtIf_StmtIf(context, (StmtIf) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.STMT_RETURN:
				if(context == grammarAccess.getStatementRule() ||
				   context == grammarAccess.getStmtReturnRule()) {
					sequence_StmtReturn_StmtReturn(context, (StmtReturn) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.STMT_WHILE:
				if(context == grammarAccess.getStatementRule() ||
				   context == grammarAccess.getStmtWhileRule()) {
					sequence_StmtWhile_StmtWhile(context, (StmtWhile) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.TYPE_EXPR:
				if(context == grammarAccess.getTypeExprRule()) {
					sequence_TypeExpr_TypeExpr(context, (TypeExpr) semanticObject); 
					return; 
				}
				else break;
			case PscriptPackage.VAR_DEF:
				if(context == grammarAccess.getEntityRule() ||
				   context == grammarAccess.getClassMemberRule() ||
				   context == grammarAccess.getVarDefRule() ||
				   context == grammarAccess.getStatementRule()) {
					sequence_VarDef_VarDef(context, (VarDef) semanticObject); 
					return; 
				}
				else break;
			}
		if (errorAcceptor != null) errorAcceptor.accept(diagnosticProvider.createInvalidContextOrTypeDiagnostic(semanticObject, context));
	}
	
	/**
	 * Constraint:
	 *     (name=ID members+=ClassMember*)
	 *
	 * Features:
	 *    name[1, 1]
	 *    members[0, *]
	 */
	protected void sequence_ClassDef_ClassDef(EObject context, ClassDef semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     ((statements+=Statement*) | statements+=StmtIf)
	 *
	 * Features:
	 *    statements[0, *]
	 */
	protected void sequence_ElseBlock_Statements(EObject context, Statements semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     statements+=StmtIf
	 *
	 * Features:
	 *    statements[1, 1]
	 */
	protected void sequence_ElseIfBlock_Statements(EObject context, Statements semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     (left=ExprAdditive_ExprAdditive_1_0 op=OpAdditive right=ExprMult)
	 *
	 * Features:
	 *    left[1, 1]
	 *    op[1, 1]
	 *    right[1, 1]
	 */
	protected void sequence_ExprAdditive_ExprAdditive(EObject context, ExprAdditive semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_ADDITIVE__LEFT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_ADDITIVE__LEFT));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_ADDITIVE__OP) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_ADDITIVE__OP));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_ADDITIVE__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_ADDITIVE__RIGHT));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getExprAdditiveAccess().getExprAdditiveLeftAction_1_0(), semanticObject.getLeft());
		feeder.accept(grammarAccess.getExprAdditiveAccess().getOpOpAdditiveParserRuleCall_1_1_0(), semanticObject.getOp());
		feeder.accept(grammarAccess.getExprAdditiveAccess().getRightExprMultParserRuleCall_1_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (left=ExprAnd_ExprAnd_1_0 op='and' right=ExprEquality)
	 *
	 * Features:
	 *    left[1, 1]
	 *    op[1, 1]
	 *    right[1, 1]
	 */
	protected void sequence_ExprAnd_ExprAnd(EObject context, ExprAnd semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_AND__LEFT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_AND__LEFT));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_AND__OP) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_AND__OP));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_AND__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_AND__RIGHT));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getExprAndAccess().getExprAndLeftAction_1_0(), semanticObject.getLeft());
		feeder.accept(grammarAccess.getExprAndAccess().getOpAndKeyword_1_1_0(), semanticObject.getOp());
		feeder.accept(grammarAccess.getExprAndAccess().getRightExprEqualityParserRuleCall_1_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (left=ExprAssignment_ExprAssignment_1_0 op=OpAssignment right=ExprOr)
	 *
	 * Features:
	 *    left[1, 1]
	 *    op[1, 1]
	 *    right[1, 1]
	 */
	protected void sequence_ExprAssignment_ExprAssignment(EObject context, ExprAssignment semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_ASSIGNMENT__LEFT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_ASSIGNMENT__LEFT));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_ASSIGNMENT__OP) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_ASSIGNMENT__OP));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_ASSIGNMENT__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_ASSIGNMENT__RIGHT));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getExprAssignmentAccess().getExprAssignmentLeftAction_1_0(), semanticObject.getLeft());
		feeder.accept(grammarAccess.getExprAssignmentAccess().getOpOpAssignmentParserRuleCall_1_1_0(), semanticObject.getOp());
		feeder.accept(grammarAccess.getExprAssignmentAccess().getRightExprOrParserRuleCall_1_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     ((nameVal=[FuncDef|ID] parameters=ExprList) | nameVal=[FuncDef|ID])
	 *
	 * Features:
	 *    nameVal[1, 2]
	 *    parameters[1, 1]
	 *         EXCLUDE_IF_UNSET nameVal
	 *         MANDATORY_IF_SET nameVal
	 *         EXCLUDE_IF_SET nameVal
	 */
	protected void sequence_ExprAtomic_ExprFunctioncall(EObject context, ExprFunctioncall semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     nameVal=[VarDef|ID]
	 *
	 * Features:
	 *    nameVal[1, 1]
	 */
	protected void sequence_ExprAtomic_ExprIdentifier(EObject context, ExprIdentifier semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_IDENTIFIER__NAME_VAL) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_IDENTIFIER__NAME_VAL));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getExprAtomicAccess().getNameValVarDefIDTerminalRuleCall_2_1_0_1(), semanticObject.getNameVal());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (left=ExprComparison_ExprComparison_1_0 op=OpComparison right=ExprAdditive)
	 *
	 * Features:
	 *    left[1, 1]
	 *    op[1, 1]
	 *    right[1, 1]
	 */
	protected void sequence_ExprComparison_ExprComparison(EObject context, ExprComparison semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_COMPARISON__LEFT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_COMPARISON__LEFT));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_COMPARISON__OP) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_COMPARISON__OP));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_COMPARISON__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_COMPARISON__RIGHT));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getExprComparisonAccess().getExprComparisonLeftAction_1_0(), semanticObject.getLeft());
		feeder.accept(grammarAccess.getExprComparisonAccess().getOpOpComparisonParserRuleCall_1_1_0(), semanticObject.getOp());
		feeder.accept(grammarAccess.getExprComparisonAccess().getRightExprAdditiveParserRuleCall_1_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (left=ExprEquality_ExprEquality_1_0 op=OpEquality right=ExprComparison)
	 *
	 * Features:
	 *    left[1, 1]
	 *    op[1, 1]
	 *    right[1, 1]
	 */
	protected void sequence_ExprEquality_ExprEquality(EObject context, ExprEquality semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_EQUALITY__LEFT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_EQUALITY__LEFT));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_EQUALITY__OP) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_EQUALITY__OP));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_EQUALITY__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_EQUALITY__RIGHT));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getExprEqualityAccess().getExprEqualityLeftAction_1_0(), semanticObject.getLeft());
		feeder.accept(grammarAccess.getExprEqualityAccess().getOpOpEqualityParserRuleCall_1_1_0(), semanticObject.getOp());
		feeder.accept(grammarAccess.getExprEqualityAccess().getRightExprComparisonParserRuleCall_1_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (params+=Expr params+=Expr*)
	 *
	 * Features:
	 *    params[1, *]
	 */
	protected void sequence_ExprList_ExprList(EObject context, ExprList semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     (left=ExprMember_ExprMember_1_0 right=ExprAtomic)
	 *
	 * Features:
	 *    left[1, 1]
	 *    right[1, 1]
	 */
	protected void sequence_ExprMember_ExprMember(EObject context, ExprMember semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_MEMBER__LEFT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_MEMBER__LEFT));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_MEMBER__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_MEMBER__RIGHT));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getExprMemberAccess().getExprMemberLeftAction_1_0(), semanticObject.getLeft());
		feeder.accept(grammarAccess.getExprMemberAccess().getRightExprAtomicParserRuleCall_1_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (left=ExprMult_ExprMult_1_0 op=OpMultiplicative right=ExprSign)
	 *
	 * Features:
	 *    left[1, 1]
	 *    op[1, 1]
	 *    right[1, 1]
	 */
	protected void sequence_ExprMult_ExprMult(EObject context, ExprMult semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_MULT__LEFT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_MULT__LEFT));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_MULT__OP) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_MULT__OP));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_MULT__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_MULT__RIGHT));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getExprMultAccess().getExprMultLeftAction_1_0(), semanticObject.getLeft());
		feeder.accept(grammarAccess.getExprMultAccess().getOpOpMultiplicativeParserRuleCall_1_1_0(), semanticObject.getOp());
		feeder.accept(grammarAccess.getExprMultAccess().getRightExprSignParserRuleCall_1_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     right=ExprMember
	 *
	 * Features:
	 *    right[1, 1]
	 */
	protected void sequence_ExprNot_ExprNot(EObject context, ExprNot semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_NOT__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_NOT__RIGHT));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getExprNotAccess().getRightExprMemberParserRuleCall_0_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (left=ExprOr_ExprOr_1_0 op='or' right=ExprAnd)
	 *
	 * Features:
	 *    left[1, 1]
	 *    op[1, 1]
	 *    right[1, 1]
	 */
	protected void sequence_ExprOr_ExprOr(EObject context, ExprOr semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_OR__LEFT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_OR__LEFT));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_OR__OP) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_OR__OP));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_OR__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_OR__RIGHT));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getExprOrAccess().getExprOrLeftAction_1_0(), semanticObject.getLeft());
		feeder.accept(grammarAccess.getExprOrAccess().getOpOrKeyword_1_1_0(), semanticObject.getOp());
		feeder.accept(grammarAccess.getExprOrAccess().getRightExprAndParserRuleCall_1_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (op=OpAdditive right=ExprNot)
	 *
	 * Features:
	 *    op[1, 1]
	 *    right[1, 1]
	 */
	protected void sequence_ExprSign_ExprSign(EObject context, ExprSign semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_SIGN__OP) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_SIGN__OP));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_SIGN__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_SIGN__RIGHT));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getExprSignAccess().getOpOpAdditiveParserRuleCall_0_1_0(), semanticObject.getOp());
		feeder.accept(grammarAccess.getExprSignAccess().getRightExprNotParserRuleCall_0_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (boolVal='true' | boolVal='false')
	 *
	 * Features:
	 *    boolVal[0, 2]
	 */
	protected void sequence_ExprSingle_ExprBoolVal(EObject context, ExprBoolVal semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     (name=ID parameters=ExprList?)
	 *
	 * Features:
	 *    name[1, 1]
	 *    parameters[0, 1]
	 */
	protected void sequence_ExprSingle_ExprBuildinFunction(EObject context, ExprBuildinFunction semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     intVal=INT
	 *
	 * Features:
	 *    intVal[1, 1]
	 */
	protected void sequence_ExprSingle_ExprIntVal(EObject context, ExprIntVal semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_INT_VAL__INT_VAL) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_INT_VAL__INT_VAL));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getExprSingleAccess().getIntValINTTerminalRuleCall_2_1_0(), semanticObject.getIntVal());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     numVal=Number
	 *
	 * Features:
	 *    numVal[1, 1]
	 */
	protected void sequence_ExprSingle_ExprNumVal(EObject context, ExprNumVal semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_NUM_VAL__NUM_VAL) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_NUM_VAL__NUM_VAL));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getExprSingleAccess().getNumValNumberParserRuleCall_3_1_0(), semanticObject.getNumVal());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     strVal=STRING
	 *
	 * Features:
	 *    strVal[1, 1]
	 */
	protected void sequence_ExprSingle_ExprStrval(EObject context, ExprStrval semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.EXPR_STRVAL__STR_VAL) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.EXPR_STRVAL__STR_VAL));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getExprSingleAccess().getStrValSTRINGTerminalRuleCall_4_1_0(), semanticObject.getStrVal());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (name=ID (parameters+=ParameterDef parameters+=ParameterDef*)? type=TypeExpr? body=Statements)
	 *
	 * Features:
	 *    name[1, 1]
	 *    parameters[0, *]
	 *    type[0, 1]
	 *    body[1, 1]
	 */
	protected void sequence_FuncDef_FuncDef(EObject context, FuncDef semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     importedNamespace=ImportString
	 *
	 * Features:
	 *    importedNamespace[1, 1]
	 */
	protected void sequence_Import_Import(EObject context, Import semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.IMPORT__IMPORTED_NAMESPACE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.IMPORT__IMPORTED_NAMESPACE));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getImportAccess().getImportedNamespaceImportStringParserRuleCall_1_0(), semanticObject.getImportedNamespace());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (name='init' body=Statements)
	 *
	 * Features:
	 *    name[1, 1]
	 *    body[1, 1]
	 */
	protected void sequence_InitBlock_InitBlock(EObject context, InitBlock semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.ENTITY__NAME) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.ENTITY__NAME));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.INIT_BLOCK__BODY) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.INIT_BLOCK__BODY));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getInitBlockAccess().getNameInitKeyword_0_0(), semanticObject.getName());
		feeder.accept(grammarAccess.getInitBlockAccess().getBodyStatementsParserRuleCall_2_0(), semanticObject.getBody());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (name=ID origName=ID superName=TypeExpr?)
	 *
	 * Features:
	 *    name[1, 1]
	 *    origName[1, 1]
	 *    superName[0, 1]
	 */
	protected void sequence_NativeType_NativeType(EObject context, NativeType semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     {OpMinus}
	 *
	 * Features:
	 */
	protected void sequence_OpAdditive_OpMinus(EObject context, OpMinus semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     {OpPlus}
	 *
	 * Features:
	 */
	protected void sequence_OpAdditive_OpPlus(EObject context, OpPlus semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     {OpAssign}
	 *
	 * Features:
	 */
	protected void sequence_OpAssignment_OpAssign(EObject context, OpAssign semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     {OpMinusAssign}
	 *
	 * Features:
	 */
	protected void sequence_OpAssignment_OpMinusAssign(EObject context, OpMinusAssign semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     {OpPlusAssign}
	 *
	 * Features:
	 */
	protected void sequence_OpAssignment_OpPlusAssign(EObject context, OpPlusAssign semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     {OpGreater}
	 *
	 * Features:
	 */
	protected void sequence_OpComparison_OpGreater(EObject context, OpGreater semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     {OpGreaterEq}
	 *
	 * Features:
	 */
	protected void sequence_OpComparison_OpGreaterEq(EObject context, OpGreaterEq semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     {OpLess}
	 *
	 * Features:
	 */
	protected void sequence_OpComparison_OpLess(EObject context, OpLess semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     {OpLessEq}
	 *
	 * Features:
	 */
	protected void sequence_OpComparison_OpLessEq(EObject context, OpLessEq semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     {OpEquals}
	 *
	 * Features:
	 */
	protected void sequence_OpEquality_OpEquals(EObject context, OpEquals semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     {OpUnequals}
	 *
	 * Features:
	 */
	protected void sequence_OpEquality_OpUnequals(EObject context, OpUnequals semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     {OpDivReal}
	 *
	 * Features:
	 */
	protected void sequence_OpMultiplicative_OpDivReal(EObject context, OpDivReal semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     {OpModInt}
	 *
	 * Features:
	 */
	protected void sequence_OpMultiplicative_OpModInt(EObject context, OpModInt semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     {OpModReal}
	 *
	 * Features:
	 */
	protected void sequence_OpMultiplicative_OpModReal(EObject context, OpModReal semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     {OpMult}
	 *
	 * Features:
	 */
	protected void sequence_OpMultiplicative_OpMult(EObject context, OpMult semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     (name=ID imports+=Import* elements+=Entity*)
	 *
	 * Features:
	 *    name[1, 1]
	 *    imports[0, *]
	 *    elements[0, *]
	 */
	protected void sequence_PackageDeclaration_PackageDeclaration(EObject context, PackageDeclaration semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     (name=ID type=TypeExpr)
	 *
	 * Features:
	 *    name[1, 1]
	 *    type[1, 1]
	 */
	protected void sequence_ParameterDef_ParameterDef(EObject context, ParameterDef semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     (packages+=PackageDeclaration packages+=PackageDeclaration*)
	 *
	 * Features:
	 *    packages[1, *]
	 */
	protected void sequence_Program_Program(EObject context, Program semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     (statements+=Statement*)
	 *
	 * Features:
	 *    statements[0, *]
	 */
	protected void sequence_Statements_Statements(EObject context, Statements semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     e=Expr
	 *
	 * Features:
	 *    e[1, 1]
	 */
	protected void sequence_StmtExpr_StmtExpr(EObject context, StmtExpr semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.STMT_EXPR__E) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.STMT_EXPR__E));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getStmtExprAccess().getEExprParserRuleCall_0_0(), semanticObject.getE());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (cond=Expr thenBlock=Statements elseBlock=ElseBlock?)
	 *
	 * Features:
	 *    cond[1, 1]
	 *    thenBlock[1, 1]
	 *    elseBlock[0, 1]
	 */
	protected void sequence_StmtIf_StmtIf(EObject context, StmtIf semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     (e=Expr?)
	 *
	 * Features:
	 *    e[0, 1]
	 */
	protected void sequence_StmtReturn_StmtReturn(EObject context, StmtReturn semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     (cond=Expr body=Statements)
	 *
	 * Features:
	 *    cond[1, 1]
	 *    body[1, 1]
	 */
	protected void sequence_StmtWhile_StmtWhile(EObject context, StmtWhile semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.STMT_WHILE__COND) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.STMT_WHILE__COND));
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.STMT_WHILE__BODY) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.STMT_WHILE__BODY));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getStmtWhileAccess().getCondExprParserRuleCall_1_0(), semanticObject.getCond());
		feeder.accept(grammarAccess.getStmtWhileAccess().getBodyStatementsParserRuleCall_3_0(), semanticObject.getBody());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     name=[TypeDef|ID]
	 *
	 * Features:
	 *    name[1, 1]
	 */
	protected void sequence_TypeExpr_TypeExpr(EObject context, TypeExpr semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, PscriptPackage.Literals.TYPE_EXPR__NAME) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, PscriptPackage.Literals.TYPE_EXPR__NAME));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getTypeExprAccess().getNameTypeDefIDTerminalRuleCall_1_0_1(), semanticObject.getName());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (constant?='val'? name=ID type=TypeExpr? e=Expr?)
	 *
	 * Features:
	 *    name[1, 1]
	 *    constant[0, 1]
	 *    type[0, 1]
	 *    e[0, 1]
	 */
	protected void sequence_VarDef_VarDef(EObject context, VarDef semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
}
