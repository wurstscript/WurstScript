/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript.impl;

import de.peeeq.pscript.pscript.ClassDef;
import de.peeeq.pscript.pscript.ClassMember;
import de.peeeq.pscript.pscript.Entity;
import de.peeeq.pscript.pscript.Expr;
import de.peeeq.pscript.pscript.ExprAdditive;
import de.peeeq.pscript.pscript.ExprAnd;
import de.peeeq.pscript.pscript.ExprBoolVal;
import de.peeeq.pscript.pscript.ExprComparison;
import de.peeeq.pscript.pscript.ExprEquality;
import de.peeeq.pscript.pscript.ExprFuncRef;
import de.peeeq.pscript.pscript.ExprFunctioncall;
import de.peeeq.pscript.pscript.ExprIdentifier;
import de.peeeq.pscript.pscript.ExprIntVal;
import de.peeeq.pscript.pscript.ExprMember;
import de.peeeq.pscript.pscript.ExprMemberRight;
import de.peeeq.pscript.pscript.ExprMult;
import de.peeeq.pscript.pscript.ExprNot;
import de.peeeq.pscript.pscript.ExprNumVal;
import de.peeeq.pscript.pscript.ExprOr;
import de.peeeq.pscript.pscript.ExprSign;
import de.peeeq.pscript.pscript.ExprStrval;
import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.Import;
import de.peeeq.pscript.pscript.InitBlock;
import de.peeeq.pscript.pscript.NativeFunc;
import de.peeeq.pscript.pscript.NativeType;
import de.peeeq.pscript.pscript.OpAdditive;
import de.peeeq.pscript.pscript.OpAssign;
import de.peeeq.pscript.pscript.OpAssignment;
import de.peeeq.pscript.pscript.OpComparison;
import de.peeeq.pscript.pscript.OpDivInt;
import de.peeeq.pscript.pscript.OpDivReal;
import de.peeeq.pscript.pscript.OpEquality;
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
import de.peeeq.pscript.pscript.OpMultiplicative;
import de.peeeq.pscript.pscript.OpPlus;
import de.peeeq.pscript.pscript.OpPlusAssign;
import de.peeeq.pscript.pscript.OpUnequals;
import de.peeeq.pscript.pscript.PackageDeclaration;
import de.peeeq.pscript.pscript.ParameterDef;
import de.peeeq.pscript.pscript.Program;
import de.peeeq.pscript.pscript.PscriptFactory;
import de.peeeq.pscript.pscript.PscriptPackage;
import de.peeeq.pscript.pscript.Statement;
import de.peeeq.pscript.pscript.Statements;
import de.peeeq.pscript.pscript.StmtCall;
import de.peeeq.pscript.pscript.StmtIf;
import de.peeeq.pscript.pscript.StmtReturn;
import de.peeeq.pscript.pscript.StmtSet;
import de.peeeq.pscript.pscript.StmtSetOrCallOrVarDef;
import de.peeeq.pscript.pscript.StmtWhile;
import de.peeeq.pscript.pscript.TypeDef;
import de.peeeq.pscript.pscript.TypeExpr;
import de.peeeq.pscript.pscript.VarDef;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class PscriptPackageImpl extends EPackageImpl implements PscriptPackage
{
  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass programEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass packageDeclarationEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass importEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass entityEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass initBlockEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass typeDefEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass funcDefEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass classMemberEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass varDefEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass typeExprEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass statementsEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass statementEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass stmtReturnEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass stmtIfEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass stmtWhileEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass stmtSetOrCallOrVarDefEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opAssignmentEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opEqualityEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opComparisonEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opAdditiveEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opMultiplicativeEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprMemberRightEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass nativeFuncEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass nativeTypeEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass classDefEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass parameterDefEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass stmtCallEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass stmtSetEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opAssignEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opPlusAssignEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opMinusAssignEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprOrEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprAndEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprEqualityEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opEqualsEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opUnequalsEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprComparisonEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opLessEqEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opLessEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opGreaterEqEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opGreaterEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprAdditiveEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opPlusEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opMinusEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprMultEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opMultEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opDivRealEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opModRealEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opModIntEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass opDivIntEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprSignEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprNotEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprMemberEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprIntValEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprNumValEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprStrvalEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprBoolValEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprFuncRefEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprIdentifierEClass = null;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass exprFunctioncallEClass = null;

  /**
   * Creates an instance of the model <b>Package</b>, registered with
   * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
   * package URI value.
   * <p>Note: the correct way to create the package is via the static
   * factory method {@link #init init()}, which also performs
   * initialization of the package, or returns the registered package,
   * if one already exists.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.eclipse.emf.ecore.EPackage.Registry
   * @see de.peeeq.pscript.pscript.PscriptPackage#eNS_URI
   * @see #init()
   * @generated
   */
  private PscriptPackageImpl()
  {
    super(eNS_URI, PscriptFactory.eINSTANCE);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private static boolean isInited = false;

  /**
   * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
   * 
   * <p>This method is used to initialize {@link PscriptPackage#eINSTANCE} when that field is accessed.
   * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #eNS_URI
   * @see #createPackageContents()
   * @see #initializePackageContents()
   * @generated
   */
  public static PscriptPackage init()
  {
    if (isInited) return (PscriptPackage)EPackage.Registry.INSTANCE.getEPackage(PscriptPackage.eNS_URI);

    // Obtain or create and register package
    PscriptPackageImpl thePscriptPackage = (PscriptPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof PscriptPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new PscriptPackageImpl());

    isInited = true;

    // Create package meta-data objects
    thePscriptPackage.createPackageContents();

    // Initialize created meta-data
    thePscriptPackage.initializePackageContents();

    // Mark meta-data to indicate it can't be changed
    thePscriptPackage.freeze();

  
    // Update the registry and return the package
    EPackage.Registry.INSTANCE.put(PscriptPackage.eNS_URI, thePscriptPackage);
    return thePscriptPackage;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getProgram()
  {
    return programEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getProgram_Packages()
  {
    return (EReference)programEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getPackageDeclaration()
  {
    return packageDeclarationEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EAttribute getPackageDeclaration_Name()
  {
    return (EAttribute)packageDeclarationEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getPackageDeclaration_Imports()
  {
    return (EReference)packageDeclarationEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getPackageDeclaration_Elements()
  {
    return (EReference)packageDeclarationEClass.getEStructuralFeatures().get(2);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getImport()
  {
    return importEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EAttribute getImport_ImportedNamespace()
  {
    return (EAttribute)importEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getEntity()
  {
    return entityEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EAttribute getEntity_Name()
  {
    return (EAttribute)entityEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getInitBlock()
  {
    return initBlockEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getInitBlock_Body()
  {
    return (EReference)initBlockEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getTypeDef()
  {
    return typeDefEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getFuncDef()
  {
    return funcDefEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getFuncDef_Parameters()
  {
    return (EReference)funcDefEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getFuncDef_Type()
  {
    return (EReference)funcDefEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getFuncDef_Body()
  {
    return (EReference)funcDefEClass.getEStructuralFeatures().get(2);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getClassMember()
  {
    return classMemberEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getVarDef()
  {
    return varDefEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EAttribute getVarDef_Constant()
  {
    return (EAttribute)varDefEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getVarDef_Type()
  {
    return (EReference)varDefEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getVarDef_E()
  {
    return (EReference)varDefEClass.getEStructuralFeatures().get(2);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getTypeExpr()
  {
    return typeExprEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getTypeExpr_Name()
  {
    return (EReference)typeExprEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EAttribute getTypeExpr_Array()
  {
    return (EAttribute)typeExprEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EAttribute getTypeExpr_Sizes()
  {
    return (EAttribute)typeExprEClass.getEStructuralFeatures().get(2);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getStatements()
  {
    return statementsEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getStatements_Statements()
  {
    return (EReference)statementsEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getStatement()
  {
    return statementEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getStmtReturn()
  {
    return stmtReturnEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getStmtReturn_E()
  {
    return (EReference)stmtReturnEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getStmtIf()
  {
    return stmtIfEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getStmtIf_Cond()
  {
    return (EReference)stmtIfEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getStmtIf_ThenBlock()
  {
    return (EReference)stmtIfEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getStmtIf_ElseBlock()
  {
    return (EReference)stmtIfEClass.getEStructuralFeatures().get(2);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getStmtWhile()
  {
    return stmtWhileEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getStmtWhile_Cond()
  {
    return (EReference)stmtWhileEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getStmtWhile_Body()
  {
    return (EReference)stmtWhileEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getStmtSetOrCallOrVarDef()
  {
    return stmtSetOrCallOrVarDefEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpAssignment()
  {
    return opAssignmentEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExpr()
  {
    return exprEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpEquality()
  {
    return opEqualityEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpComparison()
  {
    return opComparisonEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpAdditive()
  {
    return opAdditiveEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpMultiplicative()
  {
    return opMultiplicativeEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprMemberRight()
  {
    return exprMemberRightEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprMemberRight_NameVal()
  {
    return (EReference)exprMemberRightEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprMemberRight_Params()
  {
    return (EReference)exprMemberRightEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getNativeFunc()
  {
    return nativeFuncEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getNativeType()
  {
    return nativeTypeEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getNativeType_SuperName()
  {
    return (EReference)nativeTypeEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getClassDef()
  {
    return classDefEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getClassDef_Members()
  {
    return (EReference)classDefEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getParameterDef()
  {
    return parameterDefEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getStmtCall()
  {
    return stmtCallEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getStmtCall_E()
  {
    return (EReference)stmtCallEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getStmtSet()
  {
    return stmtSetEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getStmtSet_Left()
  {
    return (EReference)stmtSetEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getStmtSet_OpAssignment()
  {
    return (EReference)stmtSetEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getStmtSet_Right()
  {
    return (EReference)stmtSetEClass.getEStructuralFeatures().get(2);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpAssign()
  {
    return opAssignEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpPlusAssign()
  {
    return opPlusAssignEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpMinusAssign()
  {
    return opMinusAssignEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprOr()
  {
    return exprOrEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprOr_Left()
  {
    return (EReference)exprOrEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EAttribute getExprOr_Op()
  {
    return (EAttribute)exprOrEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprOr_Right()
  {
    return (EReference)exprOrEClass.getEStructuralFeatures().get(2);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprAnd()
  {
    return exprAndEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprAnd_Left()
  {
    return (EReference)exprAndEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EAttribute getExprAnd_Op()
  {
    return (EAttribute)exprAndEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprAnd_Right()
  {
    return (EReference)exprAndEClass.getEStructuralFeatures().get(2);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprEquality()
  {
    return exprEqualityEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprEquality_Left()
  {
    return (EReference)exprEqualityEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprEquality_Op()
  {
    return (EReference)exprEqualityEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprEquality_Right()
  {
    return (EReference)exprEqualityEClass.getEStructuralFeatures().get(2);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpEquals()
  {
    return opEqualsEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpUnequals()
  {
    return opUnequalsEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprComparison()
  {
    return exprComparisonEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprComparison_Left()
  {
    return (EReference)exprComparisonEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprComparison_Op()
  {
    return (EReference)exprComparisonEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprComparison_Right()
  {
    return (EReference)exprComparisonEClass.getEStructuralFeatures().get(2);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpLessEq()
  {
    return opLessEqEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpLess()
  {
    return opLessEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpGreaterEq()
  {
    return opGreaterEqEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpGreater()
  {
    return opGreaterEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprAdditive()
  {
    return exprAdditiveEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprAdditive_Left()
  {
    return (EReference)exprAdditiveEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprAdditive_Op()
  {
    return (EReference)exprAdditiveEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprAdditive_Right()
  {
    return (EReference)exprAdditiveEClass.getEStructuralFeatures().get(2);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpPlus()
  {
    return opPlusEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpMinus()
  {
    return opMinusEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprMult()
  {
    return exprMultEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprMult_Left()
  {
    return (EReference)exprMultEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprMult_Op()
  {
    return (EReference)exprMultEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprMult_Right()
  {
    return (EReference)exprMultEClass.getEStructuralFeatures().get(2);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpMult()
  {
    return opMultEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpDivReal()
  {
    return opDivRealEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpModReal()
  {
    return opModRealEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpModInt()
  {
    return opModIntEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getOpDivInt()
  {
    return opDivIntEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprSign()
  {
    return exprSignEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprSign_Op()
  {
    return (EReference)exprSignEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprSign_Right()
  {
    return (EReference)exprSignEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprNot()
  {
    return exprNotEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprNot_Right()
  {
    return (EReference)exprNotEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprMember()
  {
    return exprMemberEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprMember_Left()
  {
    return (EReference)exprMemberEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprMember_Message()
  {
    return (EReference)exprMemberEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprIntVal()
  {
    return exprIntValEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EAttribute getExprIntVal_IntVal()
  {
    return (EAttribute)exprIntValEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprNumVal()
  {
    return exprNumValEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EAttribute getExprNumVal_NumVal()
  {
    return (EAttribute)exprNumValEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprStrval()
  {
    return exprStrvalEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EAttribute getExprStrval_StrVal()
  {
    return (EAttribute)exprStrvalEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprBoolVal()
  {
    return exprBoolValEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EAttribute getExprBoolVal_BoolVal()
  {
    return (EAttribute)exprBoolValEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprFuncRef()
  {
    return exprFuncRefEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprFuncRef_NameVal()
  {
    return (EReference)exprFuncRefEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprIdentifier()
  {
    return exprIdentifierEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprIdentifier_NameVal()
  {
    return (EReference)exprIdentifierEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprIdentifier_ArrayIndizes()
  {
    return (EReference)exprIdentifierEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getExprFunctioncall()
  {
    return exprFunctioncallEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprFunctioncall_NameVal()
  {
    return (EReference)exprFunctioncallEClass.getEStructuralFeatures().get(0);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EReference getExprFunctioncall_Params()
  {
    return (EReference)exprFunctioncallEClass.getEStructuralFeatures().get(1);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public PscriptFactory getPscriptFactory()
  {
    return (PscriptFactory)getEFactoryInstance();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private boolean isCreated = false;

  /**
   * Creates the meta-model objects for the package.  This method is
   * guarded to have no affect on any invocation but its first.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void createPackageContents()
  {
    if (isCreated) return;
    isCreated = true;

    // Create classes and their features
    programEClass = createEClass(PROGRAM);
    createEReference(programEClass, PROGRAM__PACKAGES);

    packageDeclarationEClass = createEClass(PACKAGE_DECLARATION);
    createEAttribute(packageDeclarationEClass, PACKAGE_DECLARATION__NAME);
    createEReference(packageDeclarationEClass, PACKAGE_DECLARATION__IMPORTS);
    createEReference(packageDeclarationEClass, PACKAGE_DECLARATION__ELEMENTS);

    importEClass = createEClass(IMPORT);
    createEAttribute(importEClass, IMPORT__IMPORTED_NAMESPACE);

    entityEClass = createEClass(ENTITY);
    createEAttribute(entityEClass, ENTITY__NAME);

    initBlockEClass = createEClass(INIT_BLOCK);
    createEReference(initBlockEClass, INIT_BLOCK__BODY);

    typeDefEClass = createEClass(TYPE_DEF);

    funcDefEClass = createEClass(FUNC_DEF);
    createEReference(funcDefEClass, FUNC_DEF__PARAMETERS);
    createEReference(funcDefEClass, FUNC_DEF__TYPE);
    createEReference(funcDefEClass, FUNC_DEF__BODY);

    classMemberEClass = createEClass(CLASS_MEMBER);

    varDefEClass = createEClass(VAR_DEF);
    createEAttribute(varDefEClass, VAR_DEF__CONSTANT);
    createEReference(varDefEClass, VAR_DEF__TYPE);
    createEReference(varDefEClass, VAR_DEF__E);

    typeExprEClass = createEClass(TYPE_EXPR);
    createEReference(typeExprEClass, TYPE_EXPR__NAME);
    createEAttribute(typeExprEClass, TYPE_EXPR__ARRAY);
    createEAttribute(typeExprEClass, TYPE_EXPR__SIZES);

    statementsEClass = createEClass(STATEMENTS);
    createEReference(statementsEClass, STATEMENTS__STATEMENTS);

    statementEClass = createEClass(STATEMENT);

    stmtReturnEClass = createEClass(STMT_RETURN);
    createEReference(stmtReturnEClass, STMT_RETURN__E);

    stmtIfEClass = createEClass(STMT_IF);
    createEReference(stmtIfEClass, STMT_IF__COND);
    createEReference(stmtIfEClass, STMT_IF__THEN_BLOCK);
    createEReference(stmtIfEClass, STMT_IF__ELSE_BLOCK);

    stmtWhileEClass = createEClass(STMT_WHILE);
    createEReference(stmtWhileEClass, STMT_WHILE__COND);
    createEReference(stmtWhileEClass, STMT_WHILE__BODY);

    stmtSetOrCallOrVarDefEClass = createEClass(STMT_SET_OR_CALL_OR_VAR_DEF);

    opAssignmentEClass = createEClass(OP_ASSIGNMENT);

    exprEClass = createEClass(EXPR);

    opEqualityEClass = createEClass(OP_EQUALITY);

    opComparisonEClass = createEClass(OP_COMPARISON);

    opAdditiveEClass = createEClass(OP_ADDITIVE);

    opMultiplicativeEClass = createEClass(OP_MULTIPLICATIVE);

    exprMemberRightEClass = createEClass(EXPR_MEMBER_RIGHT);
    createEReference(exprMemberRightEClass, EXPR_MEMBER_RIGHT__NAME_VAL);
    createEReference(exprMemberRightEClass, EXPR_MEMBER_RIGHT__PARAMS);

    nativeFuncEClass = createEClass(NATIVE_FUNC);

    nativeTypeEClass = createEClass(NATIVE_TYPE);
    createEReference(nativeTypeEClass, NATIVE_TYPE__SUPER_NAME);

    classDefEClass = createEClass(CLASS_DEF);
    createEReference(classDefEClass, CLASS_DEF__MEMBERS);

    parameterDefEClass = createEClass(PARAMETER_DEF);

    stmtCallEClass = createEClass(STMT_CALL);
    createEReference(stmtCallEClass, STMT_CALL__E);

    stmtSetEClass = createEClass(STMT_SET);
    createEReference(stmtSetEClass, STMT_SET__LEFT);
    createEReference(stmtSetEClass, STMT_SET__OP_ASSIGNMENT);
    createEReference(stmtSetEClass, STMT_SET__RIGHT);

    opAssignEClass = createEClass(OP_ASSIGN);

    opPlusAssignEClass = createEClass(OP_PLUS_ASSIGN);

    opMinusAssignEClass = createEClass(OP_MINUS_ASSIGN);

    exprOrEClass = createEClass(EXPR_OR);
    createEReference(exprOrEClass, EXPR_OR__LEFT);
    createEAttribute(exprOrEClass, EXPR_OR__OP);
    createEReference(exprOrEClass, EXPR_OR__RIGHT);

    exprAndEClass = createEClass(EXPR_AND);
    createEReference(exprAndEClass, EXPR_AND__LEFT);
    createEAttribute(exprAndEClass, EXPR_AND__OP);
    createEReference(exprAndEClass, EXPR_AND__RIGHT);

    exprEqualityEClass = createEClass(EXPR_EQUALITY);
    createEReference(exprEqualityEClass, EXPR_EQUALITY__LEFT);
    createEReference(exprEqualityEClass, EXPR_EQUALITY__OP);
    createEReference(exprEqualityEClass, EXPR_EQUALITY__RIGHT);

    opEqualsEClass = createEClass(OP_EQUALS);

    opUnequalsEClass = createEClass(OP_UNEQUALS);

    exprComparisonEClass = createEClass(EXPR_COMPARISON);
    createEReference(exprComparisonEClass, EXPR_COMPARISON__LEFT);
    createEReference(exprComparisonEClass, EXPR_COMPARISON__OP);
    createEReference(exprComparisonEClass, EXPR_COMPARISON__RIGHT);

    opLessEqEClass = createEClass(OP_LESS_EQ);

    opLessEClass = createEClass(OP_LESS);

    opGreaterEqEClass = createEClass(OP_GREATER_EQ);

    opGreaterEClass = createEClass(OP_GREATER);

    exprAdditiveEClass = createEClass(EXPR_ADDITIVE);
    createEReference(exprAdditiveEClass, EXPR_ADDITIVE__LEFT);
    createEReference(exprAdditiveEClass, EXPR_ADDITIVE__OP);
    createEReference(exprAdditiveEClass, EXPR_ADDITIVE__RIGHT);

    opPlusEClass = createEClass(OP_PLUS);

    opMinusEClass = createEClass(OP_MINUS);

    exprMultEClass = createEClass(EXPR_MULT);
    createEReference(exprMultEClass, EXPR_MULT__LEFT);
    createEReference(exprMultEClass, EXPR_MULT__OP);
    createEReference(exprMultEClass, EXPR_MULT__RIGHT);

    opMultEClass = createEClass(OP_MULT);

    opDivRealEClass = createEClass(OP_DIV_REAL);

    opModRealEClass = createEClass(OP_MOD_REAL);

    opModIntEClass = createEClass(OP_MOD_INT);

    opDivIntEClass = createEClass(OP_DIV_INT);

    exprSignEClass = createEClass(EXPR_SIGN);
    createEReference(exprSignEClass, EXPR_SIGN__OP);
    createEReference(exprSignEClass, EXPR_SIGN__RIGHT);

    exprNotEClass = createEClass(EXPR_NOT);
    createEReference(exprNotEClass, EXPR_NOT__RIGHT);

    exprMemberEClass = createEClass(EXPR_MEMBER);
    createEReference(exprMemberEClass, EXPR_MEMBER__LEFT);
    createEReference(exprMemberEClass, EXPR_MEMBER__MESSAGE);

    exprIntValEClass = createEClass(EXPR_INT_VAL);
    createEAttribute(exprIntValEClass, EXPR_INT_VAL__INT_VAL);

    exprNumValEClass = createEClass(EXPR_NUM_VAL);
    createEAttribute(exprNumValEClass, EXPR_NUM_VAL__NUM_VAL);

    exprStrvalEClass = createEClass(EXPR_STRVAL);
    createEAttribute(exprStrvalEClass, EXPR_STRVAL__STR_VAL);

    exprBoolValEClass = createEClass(EXPR_BOOL_VAL);
    createEAttribute(exprBoolValEClass, EXPR_BOOL_VAL__BOOL_VAL);

    exprFuncRefEClass = createEClass(EXPR_FUNC_REF);
    createEReference(exprFuncRefEClass, EXPR_FUNC_REF__NAME_VAL);

    exprIdentifierEClass = createEClass(EXPR_IDENTIFIER);
    createEReference(exprIdentifierEClass, EXPR_IDENTIFIER__NAME_VAL);
    createEReference(exprIdentifierEClass, EXPR_IDENTIFIER__ARRAY_INDIZES);

    exprFunctioncallEClass = createEClass(EXPR_FUNCTIONCALL);
    createEReference(exprFunctioncallEClass, EXPR_FUNCTIONCALL__NAME_VAL);
    createEReference(exprFunctioncallEClass, EXPR_FUNCTIONCALL__PARAMS);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private boolean isInitialized = false;

  /**
   * Complete the initialization of the package and its meta-model.  This
   * method is guarded to have no affect on any invocation but its first.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void initializePackageContents()
  {
    if (isInitialized) return;
    isInitialized = true;

    // Initialize package
    setName(eNAME);
    setNsPrefix(eNS_PREFIX);
    setNsURI(eNS_URI);

    // Create type parameters

    // Set bounds for type parameters

    // Add supertypes to classes
    initBlockEClass.getESuperTypes().add(this.getEntity());
    typeDefEClass.getESuperTypes().add(this.getEntity());
    funcDefEClass.getESuperTypes().add(this.getEntity());
    funcDefEClass.getESuperTypes().add(this.getClassMember());
    varDefEClass.getESuperTypes().add(this.getEntity());
    varDefEClass.getESuperTypes().add(this.getClassMember());
    varDefEClass.getESuperTypes().add(this.getStatement());
    stmtReturnEClass.getESuperTypes().add(this.getStatement());
    stmtIfEClass.getESuperTypes().add(this.getStatement());
    stmtWhileEClass.getESuperTypes().add(this.getStatement());
    stmtSetOrCallOrVarDefEClass.getESuperTypes().add(this.getStatement());
    nativeFuncEClass.getESuperTypes().add(this.getFuncDef());
    nativeTypeEClass.getESuperTypes().add(this.getTypeDef());
    classDefEClass.getESuperTypes().add(this.getTypeDef());
    parameterDefEClass.getESuperTypes().add(this.getVarDef());
    stmtCallEClass.getESuperTypes().add(this.getStmtSetOrCallOrVarDef());
    stmtSetEClass.getESuperTypes().add(this.getStmtSetOrCallOrVarDef());
    opAssignEClass.getESuperTypes().add(this.getOpAssignment());
    opPlusAssignEClass.getESuperTypes().add(this.getOpAssignment());
    opMinusAssignEClass.getESuperTypes().add(this.getOpAssignment());
    exprOrEClass.getESuperTypes().add(this.getExpr());
    exprAndEClass.getESuperTypes().add(this.getExpr());
    exprEqualityEClass.getESuperTypes().add(this.getExpr());
    opEqualsEClass.getESuperTypes().add(this.getOpEquality());
    opUnequalsEClass.getESuperTypes().add(this.getOpEquality());
    exprComparisonEClass.getESuperTypes().add(this.getExpr());
    opLessEqEClass.getESuperTypes().add(this.getOpComparison());
    opLessEClass.getESuperTypes().add(this.getOpComparison());
    opGreaterEqEClass.getESuperTypes().add(this.getOpComparison());
    opGreaterEClass.getESuperTypes().add(this.getOpComparison());
    exprAdditiveEClass.getESuperTypes().add(this.getExpr());
    opPlusEClass.getESuperTypes().add(this.getOpAdditive());
    opMinusEClass.getESuperTypes().add(this.getOpAdditive());
    exprMultEClass.getESuperTypes().add(this.getExpr());
    opMultEClass.getESuperTypes().add(this.getOpMultiplicative());
    opDivRealEClass.getESuperTypes().add(this.getOpMultiplicative());
    opModRealEClass.getESuperTypes().add(this.getOpMultiplicative());
    opModIntEClass.getESuperTypes().add(this.getOpMultiplicative());
    opDivIntEClass.getESuperTypes().add(this.getOpMultiplicative());
    exprSignEClass.getESuperTypes().add(this.getExpr());
    exprNotEClass.getESuperTypes().add(this.getExpr());
    exprMemberEClass.getESuperTypes().add(this.getExpr());
    exprIntValEClass.getESuperTypes().add(this.getExpr());
    exprNumValEClass.getESuperTypes().add(this.getExpr());
    exprStrvalEClass.getESuperTypes().add(this.getExpr());
    exprBoolValEClass.getESuperTypes().add(this.getExpr());
    exprFuncRefEClass.getESuperTypes().add(this.getExpr());
    exprIdentifierEClass.getESuperTypes().add(this.getExpr());
    exprFunctioncallEClass.getESuperTypes().add(this.getExpr());

    // Initialize classes and features; add operations and parameters
    initEClass(programEClass, Program.class, "Program", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getProgram_Packages(), this.getPackageDeclaration(), null, "packages", null, 0, -1, Program.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(packageDeclarationEClass, PackageDeclaration.class, "PackageDeclaration", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEAttribute(getPackageDeclaration_Name(), ecorePackage.getEString(), "name", null, 0, 1, PackageDeclaration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getPackageDeclaration_Imports(), this.getImport(), null, "imports", null, 0, -1, PackageDeclaration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getPackageDeclaration_Elements(), this.getEntity(), null, "elements", null, 0, -1, PackageDeclaration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(importEClass, Import.class, "Import", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEAttribute(getImport_ImportedNamespace(), ecorePackage.getEString(), "importedNamespace", null, 0, 1, Import.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(entityEClass, Entity.class, "Entity", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEAttribute(getEntity_Name(), ecorePackage.getEString(), "name", null, 0, 1, Entity.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(initBlockEClass, InitBlock.class, "InitBlock", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getInitBlock_Body(), this.getStatements(), null, "body", null, 0, 1, InitBlock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(typeDefEClass, TypeDef.class, "TypeDef", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(funcDefEClass, FuncDef.class, "FuncDef", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getFuncDef_Parameters(), this.getVarDef(), null, "parameters", null, 0, -1, FuncDef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getFuncDef_Type(), this.getTypeExpr(), null, "type", null, 0, 1, FuncDef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getFuncDef_Body(), this.getStatements(), null, "body", null, 0, 1, FuncDef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(classMemberEClass, ClassMember.class, "ClassMember", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(varDefEClass, VarDef.class, "VarDef", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEAttribute(getVarDef_Constant(), ecorePackage.getEBoolean(), "constant", null, 0, 1, VarDef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getVarDef_Type(), this.getTypeExpr(), null, "type", null, 0, 1, VarDef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getVarDef_E(), this.getExpr(), null, "e", null, 0, 1, VarDef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(typeExprEClass, TypeExpr.class, "TypeExpr", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getTypeExpr_Name(), this.getTypeDef(), null, "name", null, 0, 1, TypeExpr.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEAttribute(getTypeExpr_Array(), ecorePackage.getEBoolean(), "array", null, 0, 1, TypeExpr.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEAttribute(getTypeExpr_Sizes(), ecorePackage.getEInt(), "sizes", null, 0, -1, TypeExpr.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(statementsEClass, Statements.class, "Statements", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getStatements_Statements(), this.getStatement(), null, "statements", null, 0, -1, Statements.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(statementEClass, Statement.class, "Statement", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(stmtReturnEClass, StmtReturn.class, "StmtReturn", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getStmtReturn_E(), this.getExpr(), null, "e", null, 0, 1, StmtReturn.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(stmtIfEClass, StmtIf.class, "StmtIf", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getStmtIf_Cond(), this.getExpr(), null, "cond", null, 0, 1, StmtIf.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getStmtIf_ThenBlock(), this.getStatements(), null, "thenBlock", null, 0, 1, StmtIf.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getStmtIf_ElseBlock(), this.getStatements(), null, "elseBlock", null, 0, 1, StmtIf.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(stmtWhileEClass, StmtWhile.class, "StmtWhile", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getStmtWhile_Cond(), this.getExpr(), null, "cond", null, 0, 1, StmtWhile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getStmtWhile_Body(), this.getStatements(), null, "body", null, 0, 1, StmtWhile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(stmtSetOrCallOrVarDefEClass, StmtSetOrCallOrVarDef.class, "StmtSetOrCallOrVarDef", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(opAssignmentEClass, OpAssignment.class, "OpAssignment", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(exprEClass, Expr.class, "Expr", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(opEqualityEClass, OpEquality.class, "OpEquality", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(opComparisonEClass, OpComparison.class, "OpComparison", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(opAdditiveEClass, OpAdditive.class, "OpAdditive", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(opMultiplicativeEClass, OpMultiplicative.class, "OpMultiplicative", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(exprMemberRightEClass, ExprMemberRight.class, "ExprMemberRight", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getExprMemberRight_NameVal(), this.getClassMember(), null, "nameVal", null, 0, 1, ExprMemberRight.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getExprMemberRight_Params(), this.getExpr(), null, "params", null, 0, -1, ExprMemberRight.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(nativeFuncEClass, NativeFunc.class, "NativeFunc", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(nativeTypeEClass, NativeType.class, "NativeType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getNativeType_SuperName(), this.getTypeExpr(), null, "superName", null, 0, 1, NativeType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(classDefEClass, ClassDef.class, "ClassDef", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getClassDef_Members(), this.getClassMember(), null, "members", null, 0, -1, ClassDef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(parameterDefEClass, ParameterDef.class, "ParameterDef", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(stmtCallEClass, StmtCall.class, "StmtCall", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getStmtCall_E(), this.getExpr(), null, "e", null, 0, 1, StmtCall.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(stmtSetEClass, StmtSet.class, "StmtSet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getStmtSet_Left(), this.getStmtCall(), null, "left", null, 0, 1, StmtSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getStmtSet_OpAssignment(), this.getOpAssignment(), null, "opAssignment", null, 0, 1, StmtSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getStmtSet_Right(), this.getExpr(), null, "right", null, 0, 1, StmtSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(opAssignEClass, OpAssign.class, "OpAssign", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(opPlusAssignEClass, OpPlusAssign.class, "OpPlusAssign", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(opMinusAssignEClass, OpMinusAssign.class, "OpMinusAssign", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(exprOrEClass, ExprOr.class, "ExprOr", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getExprOr_Left(), this.getExpr(), null, "left", null, 0, 1, ExprOr.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEAttribute(getExprOr_Op(), ecorePackage.getEString(), "op", null, 0, 1, ExprOr.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getExprOr_Right(), this.getExpr(), null, "right", null, 0, 1, ExprOr.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(exprAndEClass, ExprAnd.class, "ExprAnd", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getExprAnd_Left(), this.getExpr(), null, "left", null, 0, 1, ExprAnd.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEAttribute(getExprAnd_Op(), ecorePackage.getEString(), "op", null, 0, 1, ExprAnd.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getExprAnd_Right(), this.getExpr(), null, "right", null, 0, 1, ExprAnd.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(exprEqualityEClass, ExprEquality.class, "ExprEquality", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getExprEquality_Left(), this.getExpr(), null, "left", null, 0, 1, ExprEquality.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getExprEquality_Op(), this.getOpEquality(), null, "op", null, 0, 1, ExprEquality.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getExprEquality_Right(), this.getExpr(), null, "right", null, 0, 1, ExprEquality.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(opEqualsEClass, OpEquals.class, "OpEquals", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(opUnequalsEClass, OpUnequals.class, "OpUnequals", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(exprComparisonEClass, ExprComparison.class, "ExprComparison", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getExprComparison_Left(), this.getExpr(), null, "left", null, 0, 1, ExprComparison.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getExprComparison_Op(), this.getOpComparison(), null, "op", null, 0, 1, ExprComparison.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getExprComparison_Right(), this.getExpr(), null, "right", null, 0, 1, ExprComparison.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(opLessEqEClass, OpLessEq.class, "OpLessEq", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(opLessEClass, OpLess.class, "OpLess", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(opGreaterEqEClass, OpGreaterEq.class, "OpGreaterEq", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(opGreaterEClass, OpGreater.class, "OpGreater", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(exprAdditiveEClass, ExprAdditive.class, "ExprAdditive", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getExprAdditive_Left(), this.getExpr(), null, "left", null, 0, 1, ExprAdditive.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getExprAdditive_Op(), this.getOpAdditive(), null, "op", null, 0, 1, ExprAdditive.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getExprAdditive_Right(), this.getExpr(), null, "right", null, 0, 1, ExprAdditive.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(opPlusEClass, OpPlus.class, "OpPlus", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(opMinusEClass, OpMinus.class, "OpMinus", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(exprMultEClass, ExprMult.class, "ExprMult", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getExprMult_Left(), this.getExpr(), null, "left", null, 0, 1, ExprMult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getExprMult_Op(), this.getOpMultiplicative(), null, "op", null, 0, 1, ExprMult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getExprMult_Right(), this.getExpr(), null, "right", null, 0, 1, ExprMult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(opMultEClass, OpMult.class, "OpMult", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(opDivRealEClass, OpDivReal.class, "OpDivReal", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(opModRealEClass, OpModReal.class, "OpModReal", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(opModIntEClass, OpModInt.class, "OpModInt", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(opDivIntEClass, OpDivInt.class, "OpDivInt", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    initEClass(exprSignEClass, ExprSign.class, "ExprSign", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getExprSign_Op(), this.getOpAdditive(), null, "op", null, 0, 1, ExprSign.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getExprSign_Right(), this.getExpr(), null, "right", null, 0, 1, ExprSign.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(exprNotEClass, ExprNot.class, "ExprNot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getExprNot_Right(), this.getExpr(), null, "right", null, 0, 1, ExprNot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(exprMemberEClass, ExprMember.class, "ExprMember", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getExprMember_Left(), this.getExpr(), null, "left", null, 0, 1, ExprMember.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getExprMember_Message(), this.getExprMemberRight(), null, "message", null, 0, 1, ExprMember.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(exprIntValEClass, ExprIntVal.class, "ExprIntVal", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEAttribute(getExprIntVal_IntVal(), ecorePackage.getEInt(), "intVal", null, 0, 1, ExprIntVal.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(exprNumValEClass, ExprNumVal.class, "ExprNumVal", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEAttribute(getExprNumVal_NumVal(), ecorePackage.getEString(), "numVal", null, 0, 1, ExprNumVal.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(exprStrvalEClass, ExprStrval.class, "ExprStrval", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEAttribute(getExprStrval_StrVal(), ecorePackage.getEString(), "strVal", null, 0, 1, ExprStrval.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(exprBoolValEClass, ExprBoolVal.class, "ExprBoolVal", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEAttribute(getExprBoolVal_BoolVal(), ecorePackage.getEString(), "boolVal", null, 0, 1, ExprBoolVal.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(exprFuncRefEClass, ExprFuncRef.class, "ExprFuncRef", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getExprFuncRef_NameVal(), this.getFuncDef(), null, "nameVal", null, 0, 1, ExprFuncRef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(exprIdentifierEClass, ExprIdentifier.class, "ExprIdentifier", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getExprIdentifier_NameVal(), this.getVarDef(), null, "nameVal", null, 0, 1, ExprIdentifier.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getExprIdentifier_ArrayIndizes(), this.getExpr(), null, "arrayIndizes", null, 0, -1, ExprIdentifier.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    initEClass(exprFunctioncallEClass, ExprFunctioncall.class, "ExprFunctioncall", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
    initEReference(getExprFunctioncall_NameVal(), this.getFuncDef(), null, "nameVal", null, 0, 1, ExprFunctioncall.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
    initEReference(getExprFunctioncall_Params(), this.getExpr(), null, "params", null, 0, -1, ExprFunctioncall.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

    // Create resource
    createResource(eNS_URI);
  }

} //PscriptPackageImpl
