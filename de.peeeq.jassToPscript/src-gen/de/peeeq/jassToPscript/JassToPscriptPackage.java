/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.jassToPscript;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see de.peeeq.jassToPscript.JassToPscriptFactory
 * @model kind="package"
 * @generated
 */
public interface JassToPscriptPackage extends EPackage
{
  /**
   * The package name.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  String eNAME = "jassToPscript";

  /**
   * The package namespace URI.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  String eNS_URI = "http://www.JassToPscript";

  /**
   * The package namespace name.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  String eNS_PREFIX = "jassToPscript";

  /**
   * The singleton instance of the package.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  JassToPscriptPackage eINSTANCE = de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl.init();

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.ProgImpl <em>Prog</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.ProgImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getProg()
   * @generated
   */
  int PROG = 0;

  /**
   * The feature id for the '<em><b>Elems</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PROG__ELEMS = 0;

  /**
   * The number of structural features of the '<em>Prog</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PROG_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.EntityImpl <em>Entity</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.EntityImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getEntity()
   * @generated
   */
  int ENTITY = 1;

  /**
   * The number of structural features of the '<em>Entity</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ENTITY_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.GlobalBlockImpl <em>Global Block</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.GlobalBlockImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getGlobalBlock()
   * @generated
   */
  int GLOBAL_BLOCK = 2;

  /**
   * The feature id for the '<em><b>Vars</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int GLOBAL_BLOCK__VARS = ENTITY_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Global Block</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int GLOBAL_BLOCK_FEATURE_COUNT = ENTITY_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.ConstantImpl <em>Constant</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.ConstantImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getConstant()
   * @generated
   */
  int CONSTANT = 3;

  /**
   * The feature id for the '<em><b>Type</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CONSTANT__TYPE = 0;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CONSTANT__NAME = 1;

  /**
   * The feature id for the '<em><b>Value</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CONSTANT__VALUE = 2;

  /**
   * The number of structural features of the '<em>Constant</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CONSTANT_FEATURE_COUNT = 3;

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.ExprImpl <em>Expr</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.ExprImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getExpr()
   * @generated
   */
  int EXPR = 4;

  /**
   * The number of structural features of the '<em>Expr</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.MultImpl <em>Mult</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.MultImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getMult()
   * @generated
   */
  int MULT = 5;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int MULT__LEFT = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int MULT__RIGHT = EXPR_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Mult</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int MULT_FEATURE_COUNT = EXPR_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.FunctionCallImpl <em>Function Call</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.FunctionCallImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getFunctionCall()
   * @generated
   */
  int FUNCTION_CALL = 6;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_CALL__NAME = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_CALL__PARAMETERS = EXPR_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Function Call</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNCTION_CALL_FEATURE_COUNT = EXPR_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.LiteralImpl <em>Literal</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.LiteralImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getLiteral()
   * @generated
   */
  int LITERAL = 7;

  /**
   * The number of structural features of the '<em>Literal</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int LITERAL_FEATURE_COUNT = EXPR_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.NativeDefImpl <em>Native Def</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.NativeDefImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getNativeDef()
   * @generated
   */
  int NATIVE_DEF = 8;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_DEF__NAME = ENTITY_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Params</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_DEF__PARAMS = ENTITY_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Return Type</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_DEF__RETURN_TYPE = ENTITY_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Native Def</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_DEF_FEATURE_COUNT = ENTITY_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.ReturnTypeImpl <em>Return Type</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.ReturnTypeImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getReturnType()
   * @generated
   */
  int RETURN_TYPE = 9;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int RETURN_TYPE__NAME = 0;

  /**
   * The number of structural features of the '<em>Return Type</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int RETURN_TYPE_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.FormalParametersImpl <em>Formal Parameters</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.FormalParametersImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getFormalParameters()
   * @generated
   */
  int FORMAL_PARAMETERS = 10;

  /**
   * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FORMAL_PARAMETERS__PARAMETERS = 0;

  /**
   * The number of structural features of the '<em>Formal Parameters</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FORMAL_PARAMETERS_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.FormalParameterImpl <em>Formal Parameter</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.FormalParameterImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getFormalParameter()
   * @generated
   */
  int FORMAL_PARAMETER = 11;

  /**
   * The feature id for the '<em><b>Type</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FORMAL_PARAMETER__TYPE = 0;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FORMAL_PARAMETER__NAME = 1;

  /**
   * The number of structural features of the '<em>Formal Parameter</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FORMAL_PARAMETER_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.TypeDefImpl <em>Type Def</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.TypeDefImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getTypeDef()
   * @generated
   */
  int TYPE_DEF = 12;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TYPE_DEF__NAME = ENTITY_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Extend Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TYPE_DEF__EXTEND_NAME = ENTITY_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Type Def</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TYPE_DEF_FEATURE_COUNT = ENTITY_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.IntLiteralImpl <em>Int Literal</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.IntLiteralImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getIntLiteral()
   * @generated
   */
  int INT_LITERAL = 13;

  /**
   * The feature id for the '<em><b>Int Val</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int INT_LITERAL__INT_VAL = LITERAL_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Int Literal</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int INT_LITERAL_FEATURE_COUNT = LITERAL_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.BoolLiteralImpl <em>Bool Literal</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.BoolLiteralImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getBoolLiteral()
   * @generated
   */
  int BOOL_LITERAL = 14;

  /**
   * The feature id for the '<em><b>Bool Val</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int BOOL_LITERAL__BOOL_VAL = LITERAL_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Bool Literal</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int BOOL_LITERAL_FEATURE_COUNT = LITERAL_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.ReturnsNothingImpl <em>Returns Nothing</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.ReturnsNothingImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getReturnsNothing()
   * @generated
   */
  int RETURNS_NOTHING = 15;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int RETURNS_NOTHING__NAME = RETURN_TYPE__NAME;

  /**
   * The number of structural features of the '<em>Returns Nothing</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int RETURNS_NOTHING_FEATURE_COUNT = RETURN_TYPE_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.jassToPscript.impl.ParamNothingImpl <em>Param Nothing</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.jassToPscript.impl.ParamNothingImpl
   * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getParamNothing()
   * @generated
   */
  int PARAM_NOTHING = 16;

  /**
   * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PARAM_NOTHING__PARAMETERS = FORMAL_PARAMETERS__PARAMETERS;

  /**
   * The number of structural features of the '<em>Param Nothing</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PARAM_NOTHING_FEATURE_COUNT = FORMAL_PARAMETERS_FEATURE_COUNT + 0;


  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.Prog <em>Prog</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Prog</em>'.
   * @see de.peeeq.jassToPscript.Prog
   * @generated
   */
  EClass getProg();

  /**
   * Returns the meta object for the containment reference list '{@link de.peeeq.jassToPscript.Prog#getElems <em>Elems</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Elems</em>'.
   * @see de.peeeq.jassToPscript.Prog#getElems()
   * @see #getProg()
   * @generated
   */
  EReference getProg_Elems();

  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.Entity <em>Entity</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Entity</em>'.
   * @see de.peeeq.jassToPscript.Entity
   * @generated
   */
  EClass getEntity();

  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.GlobalBlock <em>Global Block</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Global Block</em>'.
   * @see de.peeeq.jassToPscript.GlobalBlock
   * @generated
   */
  EClass getGlobalBlock();

  /**
   * Returns the meta object for the containment reference list '{@link de.peeeq.jassToPscript.GlobalBlock#getVars <em>Vars</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Vars</em>'.
   * @see de.peeeq.jassToPscript.GlobalBlock#getVars()
   * @see #getGlobalBlock()
   * @generated
   */
  EReference getGlobalBlock_Vars();

  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.Constant <em>Constant</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Constant</em>'.
   * @see de.peeeq.jassToPscript.Constant
   * @generated
   */
  EClass getConstant();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.jassToPscript.Constant#getType <em>Type</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Type</em>'.
   * @see de.peeeq.jassToPscript.Constant#getType()
   * @see #getConstant()
   * @generated
   */
  EAttribute getConstant_Type();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.jassToPscript.Constant#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see de.peeeq.jassToPscript.Constant#getName()
   * @see #getConstant()
   * @generated
   */
  EAttribute getConstant_Name();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.jassToPscript.Constant#getValue <em>Value</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Value</em>'.
   * @see de.peeeq.jassToPscript.Constant#getValue()
   * @see #getConstant()
   * @generated
   */
  EReference getConstant_Value();

  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.Expr <em>Expr</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr</em>'.
   * @see de.peeeq.jassToPscript.Expr
   * @generated
   */
  EClass getExpr();

  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.Mult <em>Mult</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Mult</em>'.
   * @see de.peeeq.jassToPscript.Mult
   * @generated
   */
  EClass getMult();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.jassToPscript.Mult#getLeft <em>Left</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Left</em>'.
   * @see de.peeeq.jassToPscript.Mult#getLeft()
   * @see #getMult()
   * @generated
   */
  EReference getMult_Left();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.jassToPscript.Mult#getRight <em>Right</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Right</em>'.
   * @see de.peeeq.jassToPscript.Mult#getRight()
   * @see #getMult()
   * @generated
   */
  EReference getMult_Right();

  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.FunctionCall <em>Function Call</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Function Call</em>'.
   * @see de.peeeq.jassToPscript.FunctionCall
   * @generated
   */
  EClass getFunctionCall();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.jassToPscript.FunctionCall#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see de.peeeq.jassToPscript.FunctionCall#getName()
   * @see #getFunctionCall()
   * @generated
   */
  EAttribute getFunctionCall_Name();

  /**
   * Returns the meta object for the containment reference list '{@link de.peeeq.jassToPscript.FunctionCall#getParameters <em>Parameters</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Parameters</em>'.
   * @see de.peeeq.jassToPscript.FunctionCall#getParameters()
   * @see #getFunctionCall()
   * @generated
   */
  EReference getFunctionCall_Parameters();

  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.Literal <em>Literal</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Literal</em>'.
   * @see de.peeeq.jassToPscript.Literal
   * @generated
   */
  EClass getLiteral();

  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.NativeDef <em>Native Def</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Native Def</em>'.
   * @see de.peeeq.jassToPscript.NativeDef
   * @generated
   */
  EClass getNativeDef();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.jassToPscript.NativeDef#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see de.peeeq.jassToPscript.NativeDef#getName()
   * @see #getNativeDef()
   * @generated
   */
  EAttribute getNativeDef_Name();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.jassToPscript.NativeDef#getParams <em>Params</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Params</em>'.
   * @see de.peeeq.jassToPscript.NativeDef#getParams()
   * @see #getNativeDef()
   * @generated
   */
  EReference getNativeDef_Params();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.jassToPscript.NativeDef#getReturnType <em>Return Type</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Return Type</em>'.
   * @see de.peeeq.jassToPscript.NativeDef#getReturnType()
   * @see #getNativeDef()
   * @generated
   */
  EReference getNativeDef_ReturnType();

  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.ReturnType <em>Return Type</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Return Type</em>'.
   * @see de.peeeq.jassToPscript.ReturnType
   * @generated
   */
  EClass getReturnType();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.jassToPscript.ReturnType#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see de.peeeq.jassToPscript.ReturnType#getName()
   * @see #getReturnType()
   * @generated
   */
  EAttribute getReturnType_Name();

  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.FormalParameters <em>Formal Parameters</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Formal Parameters</em>'.
   * @see de.peeeq.jassToPscript.FormalParameters
   * @generated
   */
  EClass getFormalParameters();

  /**
   * Returns the meta object for the containment reference list '{@link de.peeeq.jassToPscript.FormalParameters#getParameters <em>Parameters</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Parameters</em>'.
   * @see de.peeeq.jassToPscript.FormalParameters#getParameters()
   * @see #getFormalParameters()
   * @generated
   */
  EReference getFormalParameters_Parameters();

  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.FormalParameter <em>Formal Parameter</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Formal Parameter</em>'.
   * @see de.peeeq.jassToPscript.FormalParameter
   * @generated
   */
  EClass getFormalParameter();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.jassToPscript.FormalParameter#getType <em>Type</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Type</em>'.
   * @see de.peeeq.jassToPscript.FormalParameter#getType()
   * @see #getFormalParameter()
   * @generated
   */
  EAttribute getFormalParameter_Type();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.jassToPscript.FormalParameter#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see de.peeeq.jassToPscript.FormalParameter#getName()
   * @see #getFormalParameter()
   * @generated
   */
  EAttribute getFormalParameter_Name();

  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.TypeDef <em>Type Def</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Type Def</em>'.
   * @see de.peeeq.jassToPscript.TypeDef
   * @generated
   */
  EClass getTypeDef();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.jassToPscript.TypeDef#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see de.peeeq.jassToPscript.TypeDef#getName()
   * @see #getTypeDef()
   * @generated
   */
  EAttribute getTypeDef_Name();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.jassToPscript.TypeDef#getExtendName <em>Extend Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Extend Name</em>'.
   * @see de.peeeq.jassToPscript.TypeDef#getExtendName()
   * @see #getTypeDef()
   * @generated
   */
  EAttribute getTypeDef_ExtendName();

  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.IntLiteral <em>Int Literal</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Int Literal</em>'.
   * @see de.peeeq.jassToPscript.IntLiteral
   * @generated
   */
  EClass getIntLiteral();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.jassToPscript.IntLiteral#getIntVal <em>Int Val</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Int Val</em>'.
   * @see de.peeeq.jassToPscript.IntLiteral#getIntVal()
   * @see #getIntLiteral()
   * @generated
   */
  EAttribute getIntLiteral_IntVal();

  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.BoolLiteral <em>Bool Literal</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Bool Literal</em>'.
   * @see de.peeeq.jassToPscript.BoolLiteral
   * @generated
   */
  EClass getBoolLiteral();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.jassToPscript.BoolLiteral#getBoolVal <em>Bool Val</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Bool Val</em>'.
   * @see de.peeeq.jassToPscript.BoolLiteral#getBoolVal()
   * @see #getBoolLiteral()
   * @generated
   */
  EAttribute getBoolLiteral_BoolVal();

  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.ReturnsNothing <em>Returns Nothing</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Returns Nothing</em>'.
   * @see de.peeeq.jassToPscript.ReturnsNothing
   * @generated
   */
  EClass getReturnsNothing();

  /**
   * Returns the meta object for class '{@link de.peeeq.jassToPscript.ParamNothing <em>Param Nothing</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Param Nothing</em>'.
   * @see de.peeeq.jassToPscript.ParamNothing
   * @generated
   */
  EClass getParamNothing();

  /**
   * Returns the factory that creates the instances of the model.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the factory that creates the instances of the model.
   * @generated
   */
  JassToPscriptFactory getJassToPscriptFactory();

  /**
   * <!-- begin-user-doc -->
   * Defines literals for the meta objects that represent
   * <ul>
   *   <li>each class,</li>
   *   <li>each feature of each class,</li>
   *   <li>each enum,</li>
   *   <li>and each data type</li>
   * </ul>
   * <!-- end-user-doc -->
   * @generated
   */
  interface Literals
  {
    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.ProgImpl <em>Prog</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.ProgImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getProg()
     * @generated
     */
    EClass PROG = eINSTANCE.getProg();

    /**
     * The meta object literal for the '<em><b>Elems</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference PROG__ELEMS = eINSTANCE.getProg_Elems();

    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.EntityImpl <em>Entity</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.EntityImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getEntity()
     * @generated
     */
    EClass ENTITY = eINSTANCE.getEntity();

    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.GlobalBlockImpl <em>Global Block</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.GlobalBlockImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getGlobalBlock()
     * @generated
     */
    EClass GLOBAL_BLOCK = eINSTANCE.getGlobalBlock();

    /**
     * The meta object literal for the '<em><b>Vars</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference GLOBAL_BLOCK__VARS = eINSTANCE.getGlobalBlock_Vars();

    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.ConstantImpl <em>Constant</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.ConstantImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getConstant()
     * @generated
     */
    EClass CONSTANT = eINSTANCE.getConstant();

    /**
     * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute CONSTANT__TYPE = eINSTANCE.getConstant_Type();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute CONSTANT__NAME = eINSTANCE.getConstant_Name();

    /**
     * The meta object literal for the '<em><b>Value</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference CONSTANT__VALUE = eINSTANCE.getConstant_Value();

    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.ExprImpl <em>Expr</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.ExprImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getExpr()
     * @generated
     */
    EClass EXPR = eINSTANCE.getExpr();

    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.MultImpl <em>Mult</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.MultImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getMult()
     * @generated
     */
    EClass MULT = eINSTANCE.getMult();

    /**
     * The meta object literal for the '<em><b>Left</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference MULT__LEFT = eINSTANCE.getMult_Left();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference MULT__RIGHT = eINSTANCE.getMult_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.FunctionCallImpl <em>Function Call</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.FunctionCallImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getFunctionCall()
     * @generated
     */
    EClass FUNCTION_CALL = eINSTANCE.getFunctionCall();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute FUNCTION_CALL__NAME = eINSTANCE.getFunctionCall_Name();

    /**
     * The meta object literal for the '<em><b>Parameters</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FUNCTION_CALL__PARAMETERS = eINSTANCE.getFunctionCall_Parameters();

    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.LiteralImpl <em>Literal</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.LiteralImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getLiteral()
     * @generated
     */
    EClass LITERAL = eINSTANCE.getLiteral();

    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.NativeDefImpl <em>Native Def</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.NativeDefImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getNativeDef()
     * @generated
     */
    EClass NATIVE_DEF = eINSTANCE.getNativeDef();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute NATIVE_DEF__NAME = eINSTANCE.getNativeDef_Name();

    /**
     * The meta object literal for the '<em><b>Params</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference NATIVE_DEF__PARAMS = eINSTANCE.getNativeDef_Params();

    /**
     * The meta object literal for the '<em><b>Return Type</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference NATIVE_DEF__RETURN_TYPE = eINSTANCE.getNativeDef_ReturnType();

    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.ReturnTypeImpl <em>Return Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.ReturnTypeImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getReturnType()
     * @generated
     */
    EClass RETURN_TYPE = eINSTANCE.getReturnType();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute RETURN_TYPE__NAME = eINSTANCE.getReturnType_Name();

    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.FormalParametersImpl <em>Formal Parameters</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.FormalParametersImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getFormalParameters()
     * @generated
     */
    EClass FORMAL_PARAMETERS = eINSTANCE.getFormalParameters();

    /**
     * The meta object literal for the '<em><b>Parameters</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FORMAL_PARAMETERS__PARAMETERS = eINSTANCE.getFormalParameters_Parameters();

    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.FormalParameterImpl <em>Formal Parameter</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.FormalParameterImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getFormalParameter()
     * @generated
     */
    EClass FORMAL_PARAMETER = eINSTANCE.getFormalParameter();

    /**
     * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute FORMAL_PARAMETER__TYPE = eINSTANCE.getFormalParameter_Type();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute FORMAL_PARAMETER__NAME = eINSTANCE.getFormalParameter_Name();

    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.TypeDefImpl <em>Type Def</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.TypeDefImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getTypeDef()
     * @generated
     */
    EClass TYPE_DEF = eINSTANCE.getTypeDef();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute TYPE_DEF__NAME = eINSTANCE.getTypeDef_Name();

    /**
     * The meta object literal for the '<em><b>Extend Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute TYPE_DEF__EXTEND_NAME = eINSTANCE.getTypeDef_ExtendName();

    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.IntLiteralImpl <em>Int Literal</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.IntLiteralImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getIntLiteral()
     * @generated
     */
    EClass INT_LITERAL = eINSTANCE.getIntLiteral();

    /**
     * The meta object literal for the '<em><b>Int Val</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute INT_LITERAL__INT_VAL = eINSTANCE.getIntLiteral_IntVal();

    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.BoolLiteralImpl <em>Bool Literal</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.BoolLiteralImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getBoolLiteral()
     * @generated
     */
    EClass BOOL_LITERAL = eINSTANCE.getBoolLiteral();

    /**
     * The meta object literal for the '<em><b>Bool Val</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute BOOL_LITERAL__BOOL_VAL = eINSTANCE.getBoolLiteral_BoolVal();

    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.ReturnsNothingImpl <em>Returns Nothing</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.ReturnsNothingImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getReturnsNothing()
     * @generated
     */
    EClass RETURNS_NOTHING = eINSTANCE.getReturnsNothing();

    /**
     * The meta object literal for the '{@link de.peeeq.jassToPscript.impl.ParamNothingImpl <em>Param Nothing</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.jassToPscript.impl.ParamNothingImpl
     * @see de.peeeq.jassToPscript.impl.JassToPscriptPackageImpl#getParamNothing()
     * @generated
     */
    EClass PARAM_NOTHING = eINSTANCE.getParamNothing();

  }

} //JassToPscriptPackage
