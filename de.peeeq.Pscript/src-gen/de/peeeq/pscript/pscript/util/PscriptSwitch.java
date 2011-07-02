/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript.util;

import de.peeeq.pscript.pscript.*;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see de.peeeq.pscript.pscript.PscriptPackage
 * @generated
 */
public class PscriptSwitch<T> extends Switch<T>
{
  /**
   * The cached model package
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected static PscriptPackage modelPackage;

  /**
   * Creates an instance of the switch.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public PscriptSwitch()
  {
    if (modelPackage == null)
    {
      modelPackage = PscriptPackage.eINSTANCE;
    }
  }

  /**
   * Checks whether this is a switch for the given package.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @parameter ePackage the package in question.
   * @return whether this is a switch for the given package.
   * @generated
   */
  @Override
  protected boolean isSwitchFor(EPackage ePackage)
  {
    return ePackage == modelPackage;
  }

  /**
   * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the first non-null result returned by a <code>caseXXX</code> call.
   * @generated
   */
  @Override
  protected T doSwitch(int classifierID, EObject theEObject)
  {
    switch (classifierID)
    {
      case PscriptPackage.PROGRAM:
      {
        Program program = (Program)theEObject;
        T result = caseProgram(program);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.PACKAGE_DECLARATION:
      {
        PackageDeclaration packageDeclaration = (PackageDeclaration)theEObject;
        T result = casePackageDeclaration(packageDeclaration);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.IMPORT:
      {
        Import import_ = (Import)theEObject;
        T result = caseImport(import_);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.NAME_DEF:
      {
        NameDef nameDef = (NameDef)theEObject;
        T result = caseNameDef(nameDef);
        if (result == null) result = caseStatement(nameDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.TYPE_EXPR:
      {
        TypeExpr typeExpr = (TypeExpr)theEObject;
        T result = caseTypeExpr(typeExpr);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.STATEMENTS:
      {
        Statements statements = (Statements)theEObject;
        T result = caseStatements(statements);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.STATEMENT:
      {
        Statement statement = (Statement)theEObject;
        T result = caseStatement(statement);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.STMT_RETURN:
      {
        StmtReturn stmtReturn = (StmtReturn)theEObject;
        T result = caseStmtReturn(stmtReturn);
        if (result == null) result = caseStatement(stmtReturn);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.STMT_IF:
      {
        StmtIf stmtIf = (StmtIf)theEObject;
        T result = caseStmtIf(stmtIf);
        if (result == null) result = caseStatement(stmtIf);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.STMT_WHILE:
      {
        StmtWhile stmtWhile = (StmtWhile)theEObject;
        T result = caseStmtWhile(stmtWhile);
        if (result == null) result = caseStatement(stmtWhile);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.STMT_EXPR:
      {
        StmtExpr stmtExpr = (StmtExpr)theEObject;
        T result = caseStmtExpr(stmtExpr);
        if (result == null) result = caseStatement(stmtExpr);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR:
      {
        Expr expr = (Expr)theEObject;
        T result = caseExpr(expr);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_LIST:
      {
        ExprList exprList = (ExprList)theEObject;
        T result = caseExprList(exprList);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.NATIVE_TYPE:
      {
        NativeType nativeType = (NativeType)theEObject;
        T result = caseNativeType(nativeType);
        if (result == null) result = caseNameDef(nativeType);
        if (result == null) result = caseStatement(nativeType);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.CLASS_DEF:
      {
        ClassDef classDef = (ClassDef)theEObject;
        T result = caseClassDef(classDef);
        if (result == null) result = caseNameDef(classDef);
        if (result == null) result = caseStatement(classDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.VAR_DEF:
      {
        VarDef varDef = (VarDef)theEObject;
        T result = caseVarDef(varDef);
        if (result == null) result = caseNameDef(varDef);
        if (result == null) result = caseStatement(varDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.FUNC_DEF:
      {
        FuncDef funcDef = (FuncDef)theEObject;
        T result = caseFuncDef(funcDef);
        if (result == null) result = caseNameDef(funcDef);
        if (result == null) result = caseStatement(funcDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.PARAMETER_DEF:
      {
        ParameterDef parameterDef = (ParameterDef)theEObject;
        T result = caseParameterDef(parameterDef);
        if (result == null) result = caseNameDef(parameterDef);
        if (result == null) result = caseStatement(parameterDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_ASSIGNMENT:
      {
        ExprAssignment exprAssignment = (ExprAssignment)theEObject;
        T result = caseExprAssignment(exprAssignment);
        if (result == null) result = caseExpr(exprAssignment);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_OR:
      {
        ExprOr exprOr = (ExprOr)theEObject;
        T result = caseExprOr(exprOr);
        if (result == null) result = caseExpr(exprOr);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_AND:
      {
        ExprAnd exprAnd = (ExprAnd)theEObject;
        T result = caseExprAnd(exprAnd);
        if (result == null) result = caseExpr(exprAnd);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_EQUALITY:
      {
        ExprEquality exprEquality = (ExprEquality)theEObject;
        T result = caseExprEquality(exprEquality);
        if (result == null) result = caseExpr(exprEquality);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_COMPARISON:
      {
        ExprComparison exprComparison = (ExprComparison)theEObject;
        T result = caseExprComparison(exprComparison);
        if (result == null) result = caseExpr(exprComparison);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_ADDITIVE:
      {
        ExprAdditive exprAdditive = (ExprAdditive)theEObject;
        T result = caseExprAdditive(exprAdditive);
        if (result == null) result = caseExpr(exprAdditive);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_MULT:
      {
        ExprMult exprMult = (ExprMult)theEObject;
        T result = caseExprMult(exprMult);
        if (result == null) result = caseExpr(exprMult);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_SIGN:
      {
        ExprSign exprSign = (ExprSign)theEObject;
        T result = caseExprSign(exprSign);
        if (result == null) result = caseExpr(exprSign);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_NOT:
      {
        ExprNot exprNot = (ExprNot)theEObject;
        T result = caseExprNot(exprNot);
        if (result == null) result = caseExpr(exprNot);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_CUSTOM_OPERATOR:
      {
        ExprCustomOperator exprCustomOperator = (ExprCustomOperator)theEObject;
        T result = caseExprCustomOperator(exprCustomOperator);
        if (result == null) result = caseExpr(exprCustomOperator);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_MEMBER:
      {
        ExprMember exprMember = (ExprMember)theEObject;
        T result = caseExprMember(exprMember);
        if (result == null) result = caseExpr(exprMember);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_FUNCTIONCALL:
      {
        ExprFunctioncall exprFunctioncall = (ExprFunctioncall)theEObject;
        T result = caseExprFunctioncall(exprFunctioncall);
        if (result == null) result = caseExpr(exprFunctioncall);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_IDENTIFIER:
      {
        ExprIdentifier exprIdentifier = (ExprIdentifier)theEObject;
        T result = caseExprIdentifier(exprIdentifier);
        if (result == null) result = caseExpr(exprIdentifier);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_INT_VAL:
      {
        ExprIntVal exprIntVal = (ExprIntVal)theEObject;
        T result = caseExprIntVal(exprIntVal);
        if (result == null) result = caseExpr(exprIntVal);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_NUM_VAL:
      {
        ExprNumVal exprNumVal = (ExprNumVal)theEObject;
        T result = caseExprNumVal(exprNumVal);
        if (result == null) result = caseExpr(exprNumVal);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_STRVAL:
      {
        ExprStrval exprStrval = (ExprStrval)theEObject;
        T result = caseExprStrval(exprStrval);
        if (result == null) result = caseExpr(exprStrval);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_BUILDIN_FUNCTION:
      {
        ExprBuildinFunction exprBuildinFunction = (ExprBuildinFunction)theEObject;
        T result = caseExprBuildinFunction(exprBuildinFunction);
        if (result == null) result = caseExpr(exprBuildinFunction);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_BUILDIN_OPERATOR:
      {
        ExprBuildinOperator exprBuildinOperator = (ExprBuildinOperator)theEObject;
        T result = caseExprBuildinOperator(exprBuildinOperator);
        if (result == null) result = caseExpr(exprBuildinOperator);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      default: return defaultCase(theEObject);
    }
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Program</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Program</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseProgram(Program object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Package Declaration</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Package Declaration</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T casePackageDeclaration(PackageDeclaration object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Import</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Import</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseImport(Import object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Name Def</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Name Def</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseNameDef(NameDef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Type Expr</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Type Expr</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseTypeExpr(TypeExpr object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Statements</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Statements</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseStatements(Statements object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Statement</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Statement</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseStatement(Statement object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Stmt Return</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Stmt Return</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseStmtReturn(StmtReturn object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Stmt If</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Stmt If</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseStmtIf(StmtIf object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Stmt While</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Stmt While</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseStmtWhile(StmtWhile object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Stmt Expr</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Stmt Expr</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseStmtExpr(StmtExpr object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExpr(Expr object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr List</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr List</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprList(ExprList object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Native Type</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Native Type</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseNativeType(NativeType object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Class Def</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Class Def</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseClassDef(ClassDef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Var Def</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Var Def</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseVarDef(VarDef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Func Def</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Func Def</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseFuncDef(FuncDef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Parameter Def</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Parameter Def</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseParameterDef(ParameterDef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Assignment</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Assignment</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprAssignment(ExprAssignment object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Or</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Or</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprOr(ExprOr object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr And</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr And</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprAnd(ExprAnd object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Equality</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Equality</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprEquality(ExprEquality object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Comparison</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Comparison</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprComparison(ExprComparison object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Additive</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Additive</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprAdditive(ExprAdditive object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Mult</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Mult</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprMult(ExprMult object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Sign</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Sign</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprSign(ExprSign object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Not</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Not</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprNot(ExprNot object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Custom Operator</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Custom Operator</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprCustomOperator(ExprCustomOperator object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Member</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Member</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprMember(ExprMember object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Functioncall</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Functioncall</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprFunctioncall(ExprFunctioncall object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Identifier</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Identifier</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprIdentifier(ExprIdentifier object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Int Val</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Int Val</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprIntVal(ExprIntVal object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Num Val</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Num Val</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprNumVal(ExprNumVal object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Strval</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Strval</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprStrval(ExprStrval object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Buildin Function</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Buildin Function</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprBuildinFunction(ExprBuildinFunction object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Buildin Operator</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Buildin Operator</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprBuildinOperator(ExprBuildinOperator object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch, but this is the last case anyway.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject)
   * @generated
   */
  @Override
  public T defaultCase(EObject object)
  {
    return null;
  }

} //PscriptSwitch
