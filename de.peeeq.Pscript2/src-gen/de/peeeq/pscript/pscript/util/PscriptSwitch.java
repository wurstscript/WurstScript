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
      case PscriptPackage.ENTITY:
      {
        Entity entity = (Entity)theEObject;
        T result = caseEntity(entity);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.INIT_BLOCK:
      {
        InitBlock initBlock = (InitBlock)theEObject;
        T result = caseInitBlock(initBlock);
        if (result == null) result = caseEntity(initBlock);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.TYPE_DEF:
      {
        TypeDef typeDef = (TypeDef)theEObject;
        T result = caseTypeDef(typeDef);
        if (result == null) result = caseEntity(typeDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.FUNC_DEF:
      {
        FuncDef funcDef = (FuncDef)theEObject;
        T result = caseFuncDef(funcDef);
        if (result == null) result = caseEntity(funcDef);
        if (result == null) result = caseClassMember(funcDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.CLASS_MEMBER:
      {
        ClassMember classMember = (ClassMember)theEObject;
        T result = caseClassMember(classMember);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.VAR_DEF:
      {
        VarDef varDef = (VarDef)theEObject;
        T result = caseVarDef(varDef);
        if (result == null) result = caseEntity(varDef);
        if (result == null) result = caseClassMember(varDef);
        if (result == null) result = caseStatement(varDef);
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
      case PscriptPackage.STMT_EXITWHEN:
      {
        StmtExitwhen stmtExitwhen = (StmtExitwhen)theEObject;
        T result = caseStmtExitwhen(stmtExitwhen);
        if (result == null) result = caseStatement(stmtExitwhen);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.STMT_LOOP:
      {
        StmtLoop stmtLoop = (StmtLoop)theEObject;
        T result = caseStmtLoop(stmtLoop);
        if (result == null) result = caseStatement(stmtLoop);
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
      case PscriptPackage.STMT_SET_OR_CALL:
      {
        StmtSetOrCall stmtSetOrCall = (StmtSetOrCall)theEObject;
        T result = caseStmtSetOrCall(stmtSetOrCall);
        if (result == null) result = caseStatement(stmtSetOrCall);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.OP_ASSIGNMENT:
      {
        OpAssignment opAssignment = (OpAssignment)theEObject;
        T result = caseOpAssignment(opAssignment);
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
      case PscriptPackage.OP_EQUALITY:
      {
        OpEquality opEquality = (OpEquality)theEObject;
        T result = caseOpEquality(opEquality);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.OP_COMPARISON:
      {
        OpComparison opComparison = (OpComparison)theEObject;
        T result = caseOpComparison(opComparison);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.OP_ADDITIVE:
      {
        OpAdditive opAdditive = (OpAdditive)theEObject;
        T result = caseOpAdditive(opAdditive);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.OP_MULTIPLICATIVE:
      {
        OpMultiplicative opMultiplicative = (OpMultiplicative)theEObject;
        T result = caseOpMultiplicative(opMultiplicative);
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
      case PscriptPackage.NATIVE_FUNC:
      {
        NativeFunc nativeFunc = (NativeFunc)theEObject;
        T result = caseNativeFunc(nativeFunc);
        if (result == null) result = caseFuncDef(nativeFunc);
        if (result == null) result = caseEntity(nativeFunc);
        if (result == null) result = caseClassMember(nativeFunc);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.NATIVE_TYPE:
      {
        NativeType nativeType = (NativeType)theEObject;
        T result = caseNativeType(nativeType);
        if (result == null) result = caseTypeDef(nativeType);
        if (result == null) result = caseEntity(nativeType);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.CLASS_DEF:
      {
        ClassDef classDef = (ClassDef)theEObject;
        T result = caseClassDef(classDef);
        if (result == null) result = caseTypeDef(classDef);
        if (result == null) result = caseEntity(classDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.TYPE_EXPR_REF:
      {
        TypeExprRef typeExprRef = (TypeExprRef)theEObject;
        T result = caseTypeExprRef(typeExprRef);
        if (result == null) result = caseTypeExpr(typeExprRef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.TYPE_EXPR_BUILDIN:
      {
        TypeExprBuildin typeExprBuildin = (TypeExprBuildin)theEObject;
        T result = caseTypeExprBuildin(typeExprBuildin);
        if (result == null) result = caseTypeExpr(typeExprBuildin);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.PARAMETER_DEF:
      {
        ParameterDef parameterDef = (ParameterDef)theEObject;
        T result = caseParameterDef(parameterDef);
        if (result == null) result = caseVarDef(parameterDef);
        if (result == null) result = caseEntity(parameterDef);
        if (result == null) result = caseClassMember(parameterDef);
        if (result == null) result = caseStatement(parameterDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.STMT_CALL:
      {
        StmtCall stmtCall = (StmtCall)theEObject;
        T result = caseStmtCall(stmtCall);
        if (result == null) result = caseStmtSetOrCall(stmtCall);
        if (result == null) result = caseStatement(stmtCall);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.STMT_SET:
      {
        StmtSet stmtSet = (StmtSet)theEObject;
        T result = caseStmtSet(stmtSet);
        if (result == null) result = caseStmtSetOrCall(stmtSet);
        if (result == null) result = caseStatement(stmtSet);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.OP_ASSIGN:
      {
        OpAssign opAssign = (OpAssign)theEObject;
        T result = caseOpAssign(opAssign);
        if (result == null) result = caseOpAssignment(opAssign);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.OP_PLUS_ASSIGN:
      {
        OpPlusAssign opPlusAssign = (OpPlusAssign)theEObject;
        T result = caseOpPlusAssign(opPlusAssign);
        if (result == null) result = caseOpAssignment(opPlusAssign);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.OP_MINUS_ASSIGN:
      {
        OpMinusAssign opMinusAssign = (OpMinusAssign)theEObject;
        T result = caseOpMinusAssign(opMinusAssign);
        if (result == null) result = caseOpAssignment(opMinusAssign);
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
      case PscriptPackage.OP_EQUALS:
      {
        OpEquals opEquals = (OpEquals)theEObject;
        T result = caseOpEquals(opEquals);
        if (result == null) result = caseOpEquality(opEquals);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.OP_UNEQUALS:
      {
        OpUnequals opUnequals = (OpUnequals)theEObject;
        T result = caseOpUnequals(opUnequals);
        if (result == null) result = caseOpEquality(opUnequals);
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
      case PscriptPackage.OP_LESS_EQ:
      {
        OpLessEq opLessEq = (OpLessEq)theEObject;
        T result = caseOpLessEq(opLessEq);
        if (result == null) result = caseOpComparison(opLessEq);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.OP_LESS:
      {
        OpLess opLess = (OpLess)theEObject;
        T result = caseOpLess(opLess);
        if (result == null) result = caseOpComparison(opLess);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.OP_GREATER_EQ:
      {
        OpGreaterEq opGreaterEq = (OpGreaterEq)theEObject;
        T result = caseOpGreaterEq(opGreaterEq);
        if (result == null) result = caseOpComparison(opGreaterEq);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.OP_GREATER:
      {
        OpGreater opGreater = (OpGreater)theEObject;
        T result = caseOpGreater(opGreater);
        if (result == null) result = caseOpComparison(opGreater);
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
      case PscriptPackage.OP_PLUS:
      {
        OpPlus opPlus = (OpPlus)theEObject;
        T result = caseOpPlus(opPlus);
        if (result == null) result = caseOpAdditive(opPlus);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.OP_MINUS:
      {
        OpMinus opMinus = (OpMinus)theEObject;
        T result = caseOpMinus(opMinus);
        if (result == null) result = caseOpAdditive(opMinus);
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
      case PscriptPackage.OP_MULT:
      {
        OpMult opMult = (OpMult)theEObject;
        T result = caseOpMult(opMult);
        if (result == null) result = caseOpMultiplicative(opMult);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.OP_DIV_REAL:
      {
        OpDivReal opDivReal = (OpDivReal)theEObject;
        T result = caseOpDivReal(opDivReal);
        if (result == null) result = caseOpMultiplicative(opDivReal);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.OP_MOD_REAL:
      {
        OpModReal opModReal = (OpModReal)theEObject;
        T result = caseOpModReal(opModReal);
        if (result == null) result = caseOpMultiplicative(opModReal);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.OP_MOD_INT:
      {
        OpModInt opModInt = (OpModInt)theEObject;
        T result = caseOpModInt(opModInt);
        if (result == null) result = caseOpMultiplicative(opModInt);
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
      case PscriptPackage.EXPR_MEMBER:
      {
        ExprMember exprMember = (ExprMember)theEObject;
        T result = caseExprMember(exprMember);
        if (result == null) result = caseExpr(exprMember);
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
      case PscriptPackage.EXPR_BOOL_VAL:
      {
        ExprBoolVal exprBoolVal = (ExprBoolVal)theEObject;
        T result = caseExprBoolVal(exprBoolVal);
        if (result == null) result = caseExpr(exprBoolVal);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case PscriptPackage.EXPR_FUNC_REF:
      {
        ExprFuncRef exprFuncRef = (ExprFuncRef)theEObject;
        T result = caseExprFuncRef(exprFuncRef);
        if (result == null) result = caseExpr(exprFuncRef);
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
      case PscriptPackage.EXPR_FUNCTIONCALL:
      {
        ExprFunctioncall exprFunctioncall = (ExprFunctioncall)theEObject;
        T result = caseExprFunctioncall(exprFunctioncall);
        if (result == null) result = caseExpr(exprFunctioncall);
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
   * Returns the result of interpreting the object as an instance of '<em>Entity</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Entity</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseEntity(Entity object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Init Block</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Init Block</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseInitBlock(InitBlock object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Type Def</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Type Def</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseTypeDef(TypeDef object)
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
   * Returns the result of interpreting the object as an instance of '<em>Class Member</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Class Member</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseClassMember(ClassMember object)
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
   * Returns the result of interpreting the object as an instance of '<em>Stmt Exitwhen</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Stmt Exitwhen</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseStmtExitwhen(StmtExitwhen object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Stmt Loop</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Stmt Loop</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseStmtLoop(StmtLoop object)
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
   * Returns the result of interpreting the object as an instance of '<em>Stmt Set Or Call</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Stmt Set Or Call</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseStmtSetOrCall(StmtSetOrCall object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Op Assignment</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Assignment</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpAssignment(OpAssignment object)
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
   * Returns the result of interpreting the object as an instance of '<em>Op Equality</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Equality</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpEquality(OpEquality object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Op Comparison</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Comparison</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpComparison(OpComparison object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Op Additive</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Additive</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpAdditive(OpAdditive object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Op Multiplicative</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Multiplicative</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpMultiplicative(OpMultiplicative object)
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
   * Returns the result of interpreting the object as an instance of '<em>Native Func</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Native Func</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseNativeFunc(NativeFunc object)
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
   * Returns the result of interpreting the object as an instance of '<em>Type Expr Ref</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Type Expr Ref</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseTypeExprRef(TypeExprRef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Type Expr Buildin</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Type Expr Buildin</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseTypeExprBuildin(TypeExprBuildin object)
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
   * Returns the result of interpreting the object as an instance of '<em>Stmt Call</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Stmt Call</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseStmtCall(StmtCall object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Stmt Set</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Stmt Set</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseStmtSet(StmtSet object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Op Assign</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Assign</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpAssign(OpAssign object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Op Plus Assign</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Plus Assign</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpPlusAssign(OpPlusAssign object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Op Minus Assign</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Minus Assign</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpMinusAssign(OpMinusAssign object)
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
   * Returns the result of interpreting the object as an instance of '<em>Op Equals</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Equals</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpEquals(OpEquals object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Op Unequals</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Unequals</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpUnequals(OpUnequals object)
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
   * Returns the result of interpreting the object as an instance of '<em>Op Less Eq</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Less Eq</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpLessEq(OpLessEq object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Op Less</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Less</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpLess(OpLess object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Op Greater Eq</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Greater Eq</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpGreaterEq(OpGreaterEq object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Op Greater</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Greater</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpGreater(OpGreater object)
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
   * Returns the result of interpreting the object as an instance of '<em>Op Plus</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Plus</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpPlus(OpPlus object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Op Minus</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Minus</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpMinus(OpMinus object)
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
   * Returns the result of interpreting the object as an instance of '<em>Op Mult</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Mult</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpMult(OpMult object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Op Div Real</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Div Real</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpDivReal(OpDivReal object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Op Mod Real</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Mod Real</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpModReal(OpModReal object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Op Mod Int</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Op Mod Int</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseOpModInt(OpModInt object)
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
   * Returns the result of interpreting the object as an instance of '<em>Expr Bool Val</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Bool Val</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprBoolVal(ExprBoolVal object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Expr Func Ref</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Expr Func Ref</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseExprFuncRef(ExprFuncRef object)
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
