package de.peeeq.serializer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import de.peeeq.jassToPscript.BoolLiteral;
import de.peeeq.jassToPscript.Constant;
import de.peeeq.jassToPscript.FormalParameter;
import de.peeeq.jassToPscript.FormalParameters;
import de.peeeq.jassToPscript.FunctionCall;
import de.peeeq.jassToPscript.GlobalBlock;
import de.peeeq.jassToPscript.IntLiteral;
import de.peeeq.jassToPscript.JassToPscriptPackage;
import de.peeeq.jassToPscript.Mult;
import de.peeeq.jassToPscript.NativeDef;
import de.peeeq.jassToPscript.ParamNothing;
import de.peeeq.jassToPscript.Prog;
import de.peeeq.jassToPscript.ReturnType;
import de.peeeq.jassToPscript.ReturnsNothing;
import de.peeeq.jassToPscript.TypeDef;
import de.peeeq.services.JassToPscriptGrammarAccess;
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
public class AbstractJassToPscriptSemanticSequencer extends AbstractSemanticSequencer {

	@Inject
	protected JassToPscriptGrammarAccess grammarAccess;
	
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
		if(semanticObject.eClass().getEPackage() == JassToPscriptPackage.eINSTANCE) switch(semanticObject.eClass().getClassifierID()) {
			case JassToPscriptPackage.BOOL_LITERAL:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getLiteralRule()) {
					sequence_Literal_BoolLiteral(context, (BoolLiteral) semanticObject); 
					return; 
				}
				else break;
			case JassToPscriptPackage.CONSTANT:
				if(context == grammarAccess.getConstantRule()) {
					sequence_Constant_Constant(context, (Constant) semanticObject); 
					return; 
				}
				else break;
			case JassToPscriptPackage.FORMAL_PARAMETER:
				if(context == grammarAccess.getFormalParameterRule()) {
					sequence_FormalParameter_FormalParameter(context, (FormalParameter) semanticObject); 
					return; 
				}
				else break;
			case JassToPscriptPackage.FORMAL_PARAMETERS:
				if(context == grammarAccess.getFormalParametersRule()) {
					sequence_FormalParameters_FormalParameters(context, (FormalParameters) semanticObject); 
					return; 
				}
				else break;
			case JassToPscriptPackage.FUNCTION_CALL:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getFunctionCallRule()) {
					sequence_FunctionCall_FunctionCall(context, (FunctionCall) semanticObject); 
					return; 
				}
				else break;
			case JassToPscriptPackage.GLOBAL_BLOCK:
				if(context == grammarAccess.getEntityRule() ||
				   context == grammarAccess.getGlobalBlockRule()) {
					sequence_GlobalBlock_GlobalBlock(context, (GlobalBlock) semanticObject); 
					return; 
				}
				else break;
			case JassToPscriptPackage.INT_LITERAL:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getLiteralRule()) {
					sequence_Literal_IntLiteral(context, (IntLiteral) semanticObject); 
					return; 
				}
				else break;
			case JassToPscriptPackage.MULT:
				if(context == grammarAccess.getExprRule() ||
				   context == grammarAccess.getMultRule()) {
					sequence_Mult_Mult(context, (Mult) semanticObject); 
					return; 
				}
				else break;
			case JassToPscriptPackage.NATIVE_DEF:
				if(context == grammarAccess.getEntityRule() ||
				   context == grammarAccess.getNativeDefRule()) {
					sequence_NativeDef_NativeDef(context, (NativeDef) semanticObject); 
					return; 
				}
				else break;
			case JassToPscriptPackage.PARAM_NOTHING:
				if(context == grammarAccess.getFormalParametersRule()) {
					sequence_FormalParameters_ParamNothing(context, (ParamNothing) semanticObject); 
					return; 
				}
				else break;
			case JassToPscriptPackage.PROG:
				if(context == grammarAccess.getProgRule()) {
					sequence_Prog_Prog(context, (Prog) semanticObject); 
					return; 
				}
				else break;
			case JassToPscriptPackage.RETURN_TYPE:
				if(context == grammarAccess.getReturnTypeRule()) {
					sequence_ReturnType_ReturnType(context, (ReturnType) semanticObject); 
					return; 
				}
				else break;
			case JassToPscriptPackage.RETURNS_NOTHING:
				if(context == grammarAccess.getReturnTypeRule()) {
					sequence_ReturnType_ReturnsNothing(context, (ReturnsNothing) semanticObject); 
					return; 
				}
				else break;
			case JassToPscriptPackage.TYPE_DEF:
				if(context == grammarAccess.getEntityRule() ||
				   context == grammarAccess.getTypeDefRule()) {
					sequence_TypeDef_TypeDef(context, (TypeDef) semanticObject); 
					return; 
				}
				else break;
			}
		if (errorAcceptor != null) errorAcceptor.accept(diagnosticProvider.createInvalidContextOrTypeDiagnostic(semanticObject, context));
	}
	
	/**
	 * Constraint:
	 *     (type=ID name=ID value=Expr)
	 *
	 * Features:
	 *    type[1, 1]
	 *    name[1, 1]
	 *    value[1, 1]
	 */
	protected void sequence_Constant_Constant(EObject context, Constant semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, JassToPscriptPackage.Literals.CONSTANT__TYPE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JassToPscriptPackage.Literals.CONSTANT__TYPE));
			if(transientValues.isValueTransient(semanticObject, JassToPscriptPackage.Literals.CONSTANT__NAME) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JassToPscriptPackage.Literals.CONSTANT__NAME));
			if(transientValues.isValueTransient(semanticObject, JassToPscriptPackage.Literals.CONSTANT__VALUE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JassToPscriptPackage.Literals.CONSTANT__VALUE));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getConstantAccess().getTypeIDTerminalRuleCall_1_0(), semanticObject.getType());
		feeder.accept(grammarAccess.getConstantAccess().getNameIDTerminalRuleCall_2_0(), semanticObject.getName());
		feeder.accept(grammarAccess.getConstantAccess().getValueExprParserRuleCall_4_0(), semanticObject.getValue());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (type=ID name=ID)
	 *
	 * Features:
	 *    type[1, 1]
	 *    name[1, 1]
	 */
	protected void sequence_FormalParameter_FormalParameter(EObject context, FormalParameter semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, JassToPscriptPackage.Literals.FORMAL_PARAMETER__TYPE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JassToPscriptPackage.Literals.FORMAL_PARAMETER__TYPE));
			if(transientValues.isValueTransient(semanticObject, JassToPscriptPackage.Literals.FORMAL_PARAMETER__NAME) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JassToPscriptPackage.Literals.FORMAL_PARAMETER__NAME));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getFormalParameterAccess().getTypeIDTerminalRuleCall_0_0(), semanticObject.getType());
		feeder.accept(grammarAccess.getFormalParameterAccess().getNameIDTerminalRuleCall_1_0(), semanticObject.getName());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (parameters+=FormalParameter parameters+=FormalParameter*)
	 *
	 * Features:
	 *    parameters[1, *]
	 */
	protected void sequence_FormalParameters_FormalParameters(EObject context, FormalParameters semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     {ParamNothing}
	 *
	 * Features:
	 */
	protected void sequence_FormalParameters_ParamNothing(EObject context, ParamNothing semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     (name=ID (parameters+=Expr parameters+=Expr*)?)
	 *
	 * Features:
	 *    name[1, 1]
	 *    parameters[0, *]
	 */
	protected void sequence_FunctionCall_FunctionCall(EObject context, FunctionCall semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     (vars+=Constant*)
	 *
	 * Features:
	 *    vars[0, *]
	 */
	protected void sequence_GlobalBlock_GlobalBlock(EObject context, GlobalBlock semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     (boolVal='true' | boolVal='false')
	 *
	 * Features:
	 *    boolVal[0, 2]
	 */
	protected void sequence_Literal_BoolLiteral(EObject context, BoolLiteral semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     intVal=INT
	 *
	 * Features:
	 *    intVal[1, 1]
	 */
	protected void sequence_Literal_IntLiteral(EObject context, IntLiteral semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, JassToPscriptPackage.Literals.INT_LITERAL__INT_VAL) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JassToPscriptPackage.Literals.INT_LITERAL__INT_VAL));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getLiteralAccess().getIntValINTTerminalRuleCall_0_1_0(), semanticObject.getIntVal());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (left=Literal right=Literal)
	 *
	 * Features:
	 *    left[1, 1]
	 *    right[1, 1]
	 */
	protected void sequence_Mult_Mult(EObject context, Mult semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, JassToPscriptPackage.Literals.MULT__LEFT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JassToPscriptPackage.Literals.MULT__LEFT));
			if(transientValues.isValueTransient(semanticObject, JassToPscriptPackage.Literals.MULT__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JassToPscriptPackage.Literals.MULT__RIGHT));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getMultAccess().getLeftLiteralParserRuleCall_0_0(), semanticObject.getLeft());
		feeder.accept(grammarAccess.getMultAccess().getRightLiteralParserRuleCall_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (name=ID params=FormalParameters returnType=ReturnType)
	 *
	 * Features:
	 *    name[1, 1]
	 *    params[1, 1]
	 *    returnType[1, 1]
	 */
	protected void sequence_NativeDef_NativeDef(EObject context, NativeDef semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, JassToPscriptPackage.Literals.NATIVE_DEF__NAME) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JassToPscriptPackage.Literals.NATIVE_DEF__NAME));
			if(transientValues.isValueTransient(semanticObject, JassToPscriptPackage.Literals.NATIVE_DEF__PARAMS) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JassToPscriptPackage.Literals.NATIVE_DEF__PARAMS));
			if(transientValues.isValueTransient(semanticObject, JassToPscriptPackage.Literals.NATIVE_DEF__RETURN_TYPE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JassToPscriptPackage.Literals.NATIVE_DEF__RETURN_TYPE));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getNativeDefAccess().getNameIDTerminalRuleCall_2_0(), semanticObject.getName());
		feeder.accept(grammarAccess.getNativeDefAccess().getParamsFormalParametersParserRuleCall_4_0(), semanticObject.getParams());
		feeder.accept(grammarAccess.getNativeDefAccess().getReturnTypeReturnTypeParserRuleCall_6_0(), semanticObject.getReturnType());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     elems+=Entity*
	 *
	 * Features:
	 *    elems[0, *]
	 */
	protected void sequence_Prog_Prog(EObject context, Prog semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     name=ID
	 *
	 * Features:
	 *    name[1, 1]
	 */
	protected void sequence_ReturnType_ReturnType(EObject context, ReturnType semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, JassToPscriptPackage.Literals.RETURN_TYPE__NAME) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JassToPscriptPackage.Literals.RETURN_TYPE__NAME));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getReturnTypeAccess().getNameIDTerminalRuleCall_1_0(), semanticObject.getName());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     {ReturnsNothing}
	 *
	 * Features:
	 */
	protected void sequence_ReturnType_ReturnsNothing(EObject context, ReturnsNothing semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     (name=ID extendName=ID)
	 *
	 * Features:
	 *    name[1, 1]
	 *    extendName[1, 1]
	 */
	protected void sequence_TypeDef_TypeDef(EObject context, TypeDef semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, JassToPscriptPackage.Literals.TYPE_DEF__NAME) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JassToPscriptPackage.Literals.TYPE_DEF__NAME));
			if(transientValues.isValueTransient(semanticObject, JassToPscriptPackage.Literals.TYPE_DEF__EXTEND_NAME) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JassToPscriptPackage.Literals.TYPE_DEF__EXTEND_NAME));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getTypeDefAccess().getNameIDTerminalRuleCall_1_0(), semanticObject.getName());
		feeder.accept(grammarAccess.getTypeDefAccess().getExtendNameIDTerminalRuleCall_3_0(), semanticObject.getExtendName());
		feeder.finish();
	}
}
