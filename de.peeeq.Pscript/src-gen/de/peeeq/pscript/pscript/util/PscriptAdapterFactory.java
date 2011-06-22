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
      public Adapter caseNameDef(NameDef object)
      {
        return createNameDefAdapter();
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
      public Adapter caseStmtExpr(StmtExpr object)
      {
        return createStmtExprAdapter();
      }
      @Override
      public Adapter caseExpr(Expr object)
      {
        return createExprAdapter();
      }
      @Override
      public Adapter caseExprList(ExprList object)
      {
        return createExprListAdapter();
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
      public Adapter caseVarDef(VarDef object)
      {
        return createVarDefAdapter();
      }
      @Override
      public Adapter caseFuncDef(FuncDef object)
      {
        return createFuncDefAdapter();
      }
      @Override
      public Adapter caseParameterDef(ParameterDef object)
      {
        return createParameterDefAdapter();
      }
      @Override
      public Adapter caseExprAssignment(ExprAssignment object)
      {
        return createExprAssignmentAdapter();
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
      public Adapter caseExprComparison(ExprComparison object)
      {
        return createExprComparisonAdapter();
      }
      @Override
      public Adapter caseExprAdditive(ExprAdditive object)
      {
        return createExprAdditiveAdapter();
      }
      @Override
      public Adapter caseExprMult(ExprMult object)
      {
        return createExprMultAdapter();
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
      public Adapter caseExprCustomOperator(ExprCustomOperator object)
      {
        return createExprCustomOperatorAdapter();
      }
      @Override
      public Adapter caseExprMember(ExprMember object)
      {
        return createExprMemberAdapter();
      }
      @Override
      public Adapter caseExprFunctioncall(ExprFunctioncall object)
      {
        return createExprFunctioncallAdapter();
      }
      @Override
      public Adapter caseExprIdentifier(ExprIdentifier object)
      {
        return createExprIdentifierAdapter();
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
      public Adapter caseExprBuildinFunction(ExprBuildinFunction object)
      {
        return createExprBuildinFunctionAdapter();
      }
      @Override
      public Adapter caseExprBuildinOperator(ExprBuildinOperator object)
      {
        return createExprBuildinOperatorAdapter();
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
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.NameDef <em>Name Def</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.NameDef
   * @generated
   */
  public Adapter createNameDefAdapter()
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
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.StmtExpr <em>Stmt Expr</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.StmtExpr
   * @generated
   */
  public Adapter createStmtExprAdapter()
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
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprList <em>Expr List</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprList
   * @generated
   */
  public Adapter createExprListAdapter()
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
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprAssignment <em>Expr Assignment</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprAssignment
   * @generated
   */
  public Adapter createExprAssignmentAdapter()
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
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprCustomOperator <em>Expr Custom Operator</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprCustomOperator
   * @generated
   */
  public Adapter createExprCustomOperatorAdapter()
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
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprBuildinFunction <em>Expr Buildin Function</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprBuildinFunction
   * @generated
   */
  public Adapter createExprBuildinFunctionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link de.peeeq.pscript.pscript.ExprBuildinOperator <em>Expr Buildin Operator</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see de.peeeq.pscript.pscript.ExprBuildinOperator
   * @generated
   */
  public Adapter createExprBuildinOperatorAdapter()
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
