/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.jassToPscript.impl;

import de.peeeq.jassToPscript.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class JassToPscriptFactoryImpl extends EFactoryImpl implements JassToPscriptFactory
{
  /**
   * Creates the default factory implementation.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public static JassToPscriptFactory init()
  {
    try
    {
      JassToPscriptFactory theJassToPscriptFactory = (JassToPscriptFactory)EPackage.Registry.INSTANCE.getEFactory("http://www.JassToPscript"); 
      if (theJassToPscriptFactory != null)
      {
        return theJassToPscriptFactory;
      }
    }
    catch (Exception exception)
    {
      EcorePlugin.INSTANCE.log(exception);
    }
    return new JassToPscriptFactoryImpl();
  }

  /**
   * Creates an instance of the factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public JassToPscriptFactoryImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public EObject create(EClass eClass)
  {
    switch (eClass.getClassifierID())
    {
      case JassToPscriptPackage.PROG: return createProg();
      case JassToPscriptPackage.ENTITY: return createEntity();
      case JassToPscriptPackage.GLOBAL_BLOCK: return createGlobalBlock();
      case JassToPscriptPackage.CONSTANT: return createConstant();
      case JassToPscriptPackage.EXPR: return createExpr();
      case JassToPscriptPackage.MULT: return createMult();
      case JassToPscriptPackage.FUNCTION_CALL: return createFunctionCall();
      case JassToPscriptPackage.LITERAL: return createLiteral();
      case JassToPscriptPackage.NATIVE_DEF: return createNativeDef();
      case JassToPscriptPackage.RETURN_TYPE: return createReturnType();
      case JassToPscriptPackage.FORMAL_PARAMETERS: return createFormalParameters();
      case JassToPscriptPackage.FORMAL_PARAMETER: return createFormalParameter();
      case JassToPscriptPackage.TYPE_DEF: return createTypeDef();
      case JassToPscriptPackage.INT_LITERAL: return createIntLiteral();
      case JassToPscriptPackage.BOOL_LITERAL: return createBoolLiteral();
      case JassToPscriptPackage.RETURNS_NOTHING: return createReturnsNothing();
      case JassToPscriptPackage.PARAM_NOTHING: return createParamNothing();
      default:
        throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
    }
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Prog createProg()
  {
    ProgImpl prog = new ProgImpl();
    return prog;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Entity createEntity()
  {
    EntityImpl entity = new EntityImpl();
    return entity;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GlobalBlock createGlobalBlock()
  {
    GlobalBlockImpl globalBlock = new GlobalBlockImpl();
    return globalBlock;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Constant createConstant()
  {
    ConstantImpl constant = new ConstantImpl();
    return constant;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Expr createExpr()
  {
    ExprImpl expr = new ExprImpl();
    return expr;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Mult createMult()
  {
    MultImpl mult = new MultImpl();
    return mult;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FunctionCall createFunctionCall()
  {
    FunctionCallImpl functionCall = new FunctionCallImpl();
    return functionCall;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Literal createLiteral()
  {
    LiteralImpl literal = new LiteralImpl();
    return literal;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NativeDef createNativeDef()
  {
    NativeDefImpl nativeDef = new NativeDefImpl();
    return nativeDef;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ReturnType createReturnType()
  {
    ReturnTypeImpl returnType = new ReturnTypeImpl();
    return returnType;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FormalParameters createFormalParameters()
  {
    FormalParametersImpl formalParameters = new FormalParametersImpl();
    return formalParameters;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FormalParameter createFormalParameter()
  {
    FormalParameterImpl formalParameter = new FormalParameterImpl();
    return formalParameter;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public TypeDef createTypeDef()
  {
    TypeDefImpl typeDef = new TypeDefImpl();
    return typeDef;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public IntLiteral createIntLiteral()
  {
    IntLiteralImpl intLiteral = new IntLiteralImpl();
    return intLiteral;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public BoolLiteral createBoolLiteral()
  {
    BoolLiteralImpl boolLiteral = new BoolLiteralImpl();
    return boolLiteral;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ReturnsNothing createReturnsNothing()
  {
    ReturnsNothingImpl returnsNothing = new ReturnsNothingImpl();
    return returnsNothing;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ParamNothing createParamNothing()
  {
    ParamNothingImpl paramNothing = new ParamNothingImpl();
    return paramNothing;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public JassToPscriptPackage getJassToPscriptPackage()
  {
    return (JassToPscriptPackage)getEPackage();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @deprecated
   * @generated
   */
  @Deprecated
  public static JassToPscriptPackage getPackage()
  {
    return JassToPscriptPackage.eINSTANCE;
  }

} //JassToPscriptFactoryImpl
