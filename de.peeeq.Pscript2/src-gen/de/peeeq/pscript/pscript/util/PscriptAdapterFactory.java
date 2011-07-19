/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript.util;

import de.peeeq.pscript.pscript.*;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see de.peeeq.pscript.pscript.PscriptPackage
 * @generated
 */
public class PscriptAdapterFactory extends AdapterFactoryImpl
{
  /**
   * The cached model package.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected static PscriptPackage modelPackage;

  /**
   * Creates an instance of the adapter factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public PscriptAdapterFactory()
  {
    if (modelPackage == null)
    {
      modelPackage = PscriptPackage.eINSTANCE;
    }
  }

  /**
   * Returns whether this factory is applicable for the type of the object.
   * <!-- begin-user-doc -->
   * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
   * <!-- end-user-doc -->
   * @return whether this factory is applicable for the type of the object.
   * @generated
   */
  @Override
  public boolean isFactoryForType(Object object)
  {
    if (object == modelPackage)
    {
      return true;
    }
    if (object instanceof EObject)
    {
      return ((EObject)object).eClass().getEPackage() == modelPackage;
    }
    return false;
  }

  /**
   * The switch that delegates to the <code>createXXX</code> methods.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected PscriptSwitch<Adapter> modelSwitch =
    new PscriptSwitch<Adapter>()
    {
      @Override
      public Adapter caseProgram(Program object)
      {
        return createProgramAdapter();
      }
      @Override
      public Adapter casePackageDeclaration(PackageDeclaration object)
      {
        return createPackageDeclarationAdapter();
      }
      @Override
      public Adapter caseImport(Import object)
      {
        return createImportAdapter();
      }
      @Override
      public Adapter caseEntity(Entity object)
      {
        return createEntityAdapter();
      }
      @Override
      public Adapter caseInitBlock(InitBlock object)
      {
        return createInitBlockAdapter();
      }
      @Override
      public Adapter caseTypeDef(TypeDef object)
      {
        return createTypeDefAdapter();
      }
      @Override
      public Adapter caseFuncDef(FuncDef object)
      {
        return createFuncDefAdapter();
      }
      @Override
      public Adapter caseClassMember(ClassMember object)
      {
        return createClassMemberAdapter();
      }
      @Override
      public Adapter caseVarDef(VarDef object)
      {
        return createVarDefAdapter();
      }
      @Override
      public Adapter caseTypeExpr(TypeExpr object)
      {
        return createTypeExprAdapter();
      }
      @Override
      public Adapter caseStatements(Statements object)
      {
        return createStatementsAdapter();
      }
      @Override
      public Adapter caseStatement(Statement object)
      {
        return createStatementAdapter();
      }
      @Override
      public Adapter caseStmtExitwhen(StmtExitwhen object)
      {
        return createStmtExitwhenAdapter();
      }
      @Override
      public Adapter caseStmtLoop(StmtLoop object)
      {
        return createStmtLoopAdapter();
      }
      @Override
      public Adapter caseStmtReturn(StmtReturn object)
      {
        return createStmtReturnAdapter();
      }
      @Override
      public Adapter caseStmtIf(StmtIf object)
      {
        return createStmtIfAdapter();
      }
      @Override
      public Adapter caseStmtWhile(StmtWhile object)
      {
        return createStmtWhileAdapter();
      }
      @Override
      public Adapter caseStmtSet(StmtSet object)
      {
        return createStmtSetAdapter();
      }
      @Override
      public Adapter caseStmtCall(StmtCall object)
      {
        return createStmtCallAdapter();
      }
      @Override
      public Adapter caseStmtSetOrCallOrVarDef(StmtSetOrCallOrVarDef object)
      {
        return createStmtSetOrCallOrVarDefAdapter();
      }
      @Override
      public Adapter caseOpAssignment(OpAssignment object)
      {
        return createOpAssignmentAdapter();
      }
      @Override
      public Adapter caseExpr(Expr object)
      {
        return createExprAdapter();
      }
      @Override
      public Adapter caseOpEquality(OpEquality object)
      {
        return createOpEqualityAdapter();
      }
      @Override
      public Adapter caseOpComparison(OpComparison object)
      {
        return createOpComparisonAdapter();
      }
      @Override
      public Adapter caseOpAdditive(OpAdditive object)
      {
        return createOpAdditiveAdapter();
      }
      @Override
      public Adapter caseOpMultiplicative(OpMultiplicative object)
      {
        return createOpMultiplicativeAdapter();
      }
      @Override
      public Adapter caseExprMemberRight(ExprMemberRight object)
      {
        return createExprMemberRightAdapter();
      }
      @Override
      public Adapter caseNativeFunc(NativeFunc object)
      {
        return createNativeFuncAdapter();
      }
      @Override
      public Adapter caseNativeType(NativeType object)
      {
        return createNativeTypeAdapter();
      }
      @Override
      public Adapter caseClassDef(ClassDef object)
      {
        return createClassDefAdapter();
      }
      @Override
      public Adapter caseTypeExprRef(TypeExprRef object)
      {
        return createTypeExprRefAdapter();
      }
      @Override
      public Adapter caseTypeExprBuildin(TypeExprBuildin object)
      {
        return createTypeExprBuildinAdapter();
      }
      @Override
      public Adapter caseParameterDef(ParameterDef object)
      {
        return createParameterDefAdapter();
      }
      @Override
      public Adapter caseOpAssign(OpAssign object)
      {
        return createOpAssignAdapter();
      }
      @Override
      public Adapter caseOpPlusAssign(OpPlusAssign object)
      {
        return createOpPlusAssignAdapter();
      }
      @Override
      public Adapter caseOpMinusAssign(OpMinusAssign object)
      {
        return createOpMinusAssignAdapter();
      }
      @Override
      public Adapter caseExprOr(ExprOr object)
      {
        return createExprOrAdapter();
      }
      @Override
      public Adapter caseExprAnd(ExprAnd object)
      {
        return createExprAndAdapter();
      }
      @Override
      public Adapter caseExprEquality(ExprEquality object)
      {
        return createExprEqualityAdapter();
      }
      @Override
      public Adapter caseOpEquals(OpEquals object)
      {
        return createOpEqualsAdapter();
      }
      @Override
      public Adapter caseOpUnequals(OpUnequals object)
      {
        return createOpUnequalsAdapter();
      }
      @Override
      public Adapter caseExprComparison(ExprComparison object)
      {
        return createExprComparisonAdapter();
      }
      @Override
      public Adapter caseOpLessEq(OpLessEq object)
      {
        return createOpLessEqAdapter();
      }
      @Override
      public Adapter caseOpLess(OpLess object)
      {
        return createOpLessAdapter();
      }
      @Override
      public Adapter caseOpGreaterEq(OpGreaterEq object)
      {
        return createOpGreaterEqAdapter();
      }
      @Override
      public Adapter caseOpGreater(OpGreater object)
      {
        return createOpGreaterAdapter();
      }
      @Override
      public Adapter caseExprAdditive(ExprAdditive object)
      {
        return createExprAdditiveAdapter();
      }
      @Override
      public Adapter caseOpPlus(OpPlus object)
      {
        return createOpPlusAdapter();
      }
      @Override
      public Adapter caseOpMinus(OpMinus object)
      {
        return createOpMinusAdapter();
      }
      @Override
      public Adapter caseExprMult(ExprMult object)
      {
        return createExprMultAdapter();
      }
      @Override
      public Adapter caseOpMult(OpMult object)
      {
        return createOpMultAdapter();
      }
      @Override
      public Adapter caseOpDivReal(OpDivReal object)
      {
        return createOpDivRealAdapter();
      }
      @Override
      public Adapter caseOpModReal(OpModReal object)
      {
        return createOpModRealAdapter();
      }
      @Override
      public Adapter caseOpModInt(OpModInt object)
      {
        return createOpModIntAdapter();
      }
      @Override
      public Adapter caseExprSign(ExprSign object)
      {
        return createExprSignAdapter();
      }
      @Override
      public Adapter caseExprNot(ExprNot object)
      {
        return createExprNotAdapter();
      }
      @Override
      public Adapter caseExprMember(ExprMember object)
      {
        return createExprMemberAdapter();
      }
      @Override
      public Adapter caseExprIntVal(ExprIntVal object)
      {
        return createExprIntValAdapter();
      }
      @Override
      public Adapter caseExprNumVal(ExprNumVal object)
      {
        return createExprNumValAdapter();
      }
      @Override
      public Adapter caseExprStrval(ExprStrval object)
      {
        return createExprStrvalAdapter();
      }
      @Override
      public Adapter caseExprBoolVal(ExprBoolVal object)
      {
        return createExprBoolValAdapter();
      }
      @Override
      public Adapter caseExprFuncRef(ExprFuncRef object)
      {
        return createExprFuncRefAdapter();
      }
      @Override
      public Adapter caseExprIdentifier(ExprIdentifier object)
      {
        return createExprIdentifierAdapter();
      }
      @Override
      public Adapter caseExprFunctioncall(ExprFunctioncall object)
      {
        return createExprFunctioncallAdapter();
      }
      @Override
      public Adapter defaultCase(EObject object)
      {
        return createEObjectAdapter();
      }
    };

  /**
   * Creates an adapter for the <code>target</code>.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param target the object to adapt.
   * @return the adapter for the <code>target</code>.
   * @generated
   */
  @Override
  public Adapter createAdapter(Notifier target)
  {
    return modelSwitch.doSwitch((EObject)target);
  }


  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.Program <em>Program</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.Program
   * @generated
   */
  public Adapter createProgramAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.PackageDeclaration <em>Package Declaration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.PackageDeclaration
   * @generated
   */
  public Adapter createPackageDeclarationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.Import <em>Import</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.Import
   * @generated
   */
  public Adapter createImportAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.Entity <em>Entity</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.Entity
   * @generated
   */
  public Adapter createEntityAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.InitBlock <em>Init Block</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.InitBlock
   * @generated
   */
  public Adapter createInitBlockAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.TypeDef <em>Type Def</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.TypeDef
   * @generated
   */
  public Adapter createTypeDefAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.FuncDef <em>Func Def</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.FuncDef
   * @generated
   */
  public Adapter createFuncDefAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ClassMember <em>Class Member</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ClassMember
   * @generated
   */
  public Adapter createClassMemberAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.VarDef <em>Var Def</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.VarDef
   * @generated
   */
  public Adapter createVarDefAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.TypeExpr <em>Type Expr</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.TypeExpr
   * @generated
   */
  public Adapter createTypeExprAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.Statements <em>Statements</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.Statements
   * @generated
   */
  public Adapter createStatementsAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.Statement <em>Statement</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.Statement
   * @generated
   */
  public Adapter createStatementAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.StmtExitwhen <em>Stmt Exitwhen</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.StmtExitwhen
   * @generated
   */
  public Adapter createStmtExitwhenAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.StmtLoop <em>Stmt Loop</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.StmtLoop
   * @generated
   */
  public Adapter createStmtLoopAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.StmtReturn <em>Stmt Return</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.StmtReturn
   * @generated
   */
  public Adapter createStmtReturnAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.StmtIf <em>Stmt If</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.StmtIf
   * @generated
   */
  public Adapter createStmtIfAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.StmtWhile <em>Stmt While</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.StmtWhile
   * @generated
   */
  public Adapter createStmtWhileAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.StmtSet <em>Stmt Set</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.StmtSet
   * @generated
   */
  public Adapter createStmtSetAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.StmtCall <em>Stmt Call</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.StmtCall
   * @generated
   */
  public Adapter createStmtCallAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.StmtSetOrCallOrVarDef <em>Stmt Set Or Call Or Var Def</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.StmtSetOrCallOrVarDef
   * @generated
   */
  public Adapter createStmtSetOrCallOrVarDefAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpAssignment <em>Op Assignment</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpAssignment
   * @generated
   */
  public Adapter createOpAssignmentAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.Expr <em>Expr</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.Expr
   * @generated
   */
  public Adapter createExprAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpEquality <em>Op Equality</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpEquality
   * @generated
   */
  public Adapter createOpEqualityAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpComparison <em>Op Comparison</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpComparison
   * @generated
   */
  public Adapter createOpComparisonAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpAdditive <em>Op Additive</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpAdditive
   * @generated
   */
  public Adapter createOpAdditiveAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpMultiplicative <em>Op Multiplicative</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpMultiplicative
   * @generated
   */
  public Adapter createOpMultiplicativeAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprMemberRight <em>Expr Member Right</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprMemberRight
   * @generated
   */
  public Adapter createExprMemberRightAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.NativeFunc <em>Native Func</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.NativeFunc
   * @generated
   */
  public Adapter createNativeFuncAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.NativeType <em>Native Type</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.NativeType
   * @generated
   */
  public Adapter createNativeTypeAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ClassDef <em>Class Def</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ClassDef
   * @generated
   */
  public Adapter createClassDefAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.TypeExprRef <em>Type Expr Ref</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.TypeExprRef
   * @generated
   */
  public Adapter createTypeExprRefAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.TypeExprBuildin <em>Type Expr Buildin</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.TypeExprBuildin
   * @generated
   */
  public Adapter createTypeExprBuildinAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ParameterDef <em>Parameter Def</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ParameterDef
   * @generated
   */
  public Adapter createParameterDefAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpAssign <em>Op Assign</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpAssign
   * @generated
   */
  public Adapter createOpAssignAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpPlusAssign <em>Op Plus Assign</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpPlusAssign
   * @generated
   */
  public Adapter createOpPlusAssignAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpMinusAssign <em>Op Minus Assign</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpMinusAssign
   * @generated
   */
  public Adapter createOpMinusAssignAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprOr <em>Expr Or</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprOr
   * @generated
   */
  public Adapter createExprOrAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprAnd <em>Expr And</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprAnd
   * @generated
   */
  public Adapter createExprAndAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprEquality <em>Expr Equality</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprEquality
   * @generated
   */
  public Adapter createExprEqualityAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpEquals <em>Op Equals</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpEquals
   * @generated
   */
  public Adapter createOpEqualsAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpUnequals <em>Op Unequals</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpUnequals
   * @generated
   */
  public Adapter createOpUnequalsAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprComparison <em>Expr Comparison</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprComparison
   * @generated
   */
  public Adapter createExprComparisonAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpLessEq <em>Op Less Eq</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpLessEq
   * @generated
   */
  public Adapter createOpLessEqAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpLess <em>Op Less</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpLess
   * @generated
   */
  public Adapter createOpLessAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpGreaterEq <em>Op Greater Eq</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpGreaterEq
   * @generated
   */
  public Adapter createOpGreaterEqAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpGreater <em>Op Greater</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpGreater
   * @generated
   */
  public Adapter createOpGreaterAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprAdditive <em>Expr Additive</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprAdditive
   * @generated
   */
  public Adapter createExprAdditiveAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpPlus <em>Op Plus</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpPlus
   * @generated
   */
  public Adapter createOpPlusAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpMinus <em>Op Minus</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpMinus
   * @generated
   */
  public Adapter createOpMinusAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprMult <em>Expr Mult</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprMult
   * @generated
   */
  public Adapter createExprMultAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpMult <em>Op Mult</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpMult
   * @generated
   */
  public Adapter createOpMultAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpDivReal <em>Op Div Real</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpDivReal
   * @generated
   */
  public Adapter createOpDivRealAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpModReal <em>Op Mod Real</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpModReal
   * @generated
   */
  public Adapter createOpModRealAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.OpModInt <em>Op Mod Int</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.OpModInt
   * @generated
   */
  public Adapter createOpModIntAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprSign <em>Expr Sign</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprSign
   * @generated
   */
  public Adapter createExprSignAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprNot <em>Expr Not</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprNot
   * @generated
   */
  public Adapter createExprNotAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprMember <em>Expr Member</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprMember
   * @generated
   */
  public Adapter createExprMemberAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprIntVal <em>Expr Int Val</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprIntVal
   * @generated
   */
  public Adapter createExprIntValAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprNumVal <em>Expr Num Val</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprNumVal
   * @generated
   */
  public Adapter createExprNumValAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprStrval <em>Expr Strval</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprStrval
   * @generated
   */
  public Adapter createExprStrvalAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprBoolVal <em>Expr Bool Val</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprBoolVal
   * @generated
   */
  public Adapter createExprBoolValAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprFuncRef <em>Expr Func Ref</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprFuncRef
   * @generated
   */
  public Adapter createExprFuncRefAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprIdentifier <em>Expr Identifier</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprIdentifier
   * @generated
   */
  public Adapter createExprIdentifierAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprFunctioncall <em>Expr Functioncall</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprFunctioncall
   * @generated
   */
  public Adapter createExprFunctioncallAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for the default case.
   * <!-- begin-user-doc -->
   * This default implementation returns null.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @generated
   */
  public Adapter createEObjectAdapter()
  {
    return null;
  }

} //PscriptAdapterFactory
