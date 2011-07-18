/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see de.peeeq.pscript.pscript.PscriptPackage
 * @generated
 */
public interface PscriptFactory extends EFactory
{
  /**
   * The singleton instance of the factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  PscriptFactory eINSTANCE = de.peeeq.pscript.pscript.impl.PscriptFactoryImpl.init();

  /**
   * Returns a new object of class '<em>Program</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Program</em>'.
   * @generated
   */
  Program createProgram();

  /**
   * Returns a new object of class '<em>Package Declaration</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Package Declaration</em>'.
   * @generated
   */
  PackageDeclaration createPackageDeclaration();

  /**
   * Returns a new object of class '<em>Import</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Import</em>'.
   * @generated
   */
  Import createImport();

  /**
   * Returns a new object of class '<em>Entity</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Entity</em>'.
   * @generated
   */
  Entity createEntity();

  /**
   * Returns a new object of class '<em>Init Block</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Init Block</em>'.
   * @generated
   */
  InitBlock createInitBlock();

  /**
   * Returns a new object of class '<em>Type Def</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Type Def</em>'.
   * @generated
   */
  TypeDef createTypeDef();

  /**
   * Returns a new object of class '<em>Func Def</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Func Def</em>'.
   * @generated
   */
  FuncDef createFuncDef();

  /**
   * Returns a new object of class '<em>Class Member</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Class Member</em>'.
   * @generated
   */
  ClassMember createClassMember();

  /**
   * Returns a new object of class '<em>Var Def</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Var Def</em>'.
   * @generated
   */
  VarDef createVarDef();

  /**
   * Returns a new object of class '<em>Type Expr</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Type Expr</em>'.
   * @generated
   */
  TypeExpr createTypeExpr();

  /**
   * Returns a new object of class '<em>Statements</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Statements</em>'.
   * @generated
   */
  Statements createStatements();

  /**
   * Returns a new object of class '<em>Statement</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Statement</em>'.
   * @generated
   */
  Statement createStatement();

  /**
   * Returns a new object of class '<em>Stmt Exitwhen</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Stmt Exitwhen</em>'.
   * @generated
   */
  StmtExitwhen createStmtExitwhen();

  /**
   * Returns a new object of class '<em>Stmt Loop</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Stmt Loop</em>'.
   * @generated
   */
  StmtLoop createStmtLoop();

  /**
   * Returns a new object of class '<em>Stmt Return</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Stmt Return</em>'.
   * @generated
   */
  StmtReturn createStmtReturn();

  /**
   * Returns a new object of class '<em>Stmt If</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Stmt If</em>'.
   * @generated
   */
  StmtIf createStmtIf();

  /**
   * Returns a new object of class '<em>Stmt While</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Stmt While</em>'.
   * @generated
   */
  StmtWhile createStmtWhile();

  /**
   * Returns a new object of class '<em>Stmt Set Or Call</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Stmt Set Or Call</em>'.
   * @generated
   */
  StmtSetOrCall createStmtSetOrCall();

  /**
   * Returns a new object of class '<em>Op Assignment</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Assignment</em>'.
   * @generated
   */
  OpAssignment createOpAssignment();

  /**
   * Returns a new object of class '<em>Expr</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr</em>'.
   * @generated
   */
  Expr createExpr();

  /**
   * Returns a new object of class '<em>Op Equality</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Equality</em>'.
   * @generated
   */
  OpEquality createOpEquality();

  /**
   * Returns a new object of class '<em>Op Comparison</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Comparison</em>'.
   * @generated
   */
  OpComparison createOpComparison();

  /**
   * Returns a new object of class '<em>Op Additive</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Additive</em>'.
   * @generated
   */
  OpAdditive createOpAdditive();

  /**
   * Returns a new object of class '<em>Op Multiplicative</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Multiplicative</em>'.
   * @generated
   */
  OpMultiplicative createOpMultiplicative();

  /**
   * Returns a new object of class '<em>Expr List</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr List</em>'.
   * @generated
   */
  ExprList createExprList();

  /**
   * Returns a new object of class '<em>Native Func</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Native Func</em>'.
   * @generated
   */
  NativeFunc createNativeFunc();

  /**
   * Returns a new object of class '<em>Native Type</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Native Type</em>'.
   * @generated
   */
  NativeType createNativeType();

  /**
   * Returns a new object of class '<em>Class Def</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Class Def</em>'.
   * @generated
   */
  ClassDef createClassDef();

  /**
   * Returns a new object of class '<em>Type Expr Ref</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Type Expr Ref</em>'.
   * @generated
   */
  TypeExprRef createTypeExprRef();

  /**
   * Returns a new object of class '<em>Type Expr Buildin</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Type Expr Buildin</em>'.
   * @generated
   */
  TypeExprBuildin createTypeExprBuildin();

  /**
   * Returns a new object of class '<em>Parameter Def</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Parameter Def</em>'.
   * @generated
   */
  ParameterDef createParameterDef();

  /**
   * Returns a new object of class '<em>Stmt Call</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Stmt Call</em>'.
   * @generated
   */
  StmtCall createStmtCall();

  /**
   * Returns a new object of class '<em>Stmt Set</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Stmt Set</em>'.
   * @generated
   */
  StmtSet createStmtSet();

  /**
   * Returns a new object of class '<em>Op Assign</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Assign</em>'.
   * @generated
   */
  OpAssign createOpAssign();

  /**
   * Returns a new object of class '<em>Op Plus Assign</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Plus Assign</em>'.
   * @generated
   */
  OpPlusAssign createOpPlusAssign();

  /**
   * Returns a new object of class '<em>Op Minus Assign</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Minus Assign</em>'.
   * @generated
   */
  OpMinusAssign createOpMinusAssign();

  /**
   * Returns a new object of class '<em>Expr Or</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Or</em>'.
   * @generated
   */
  ExprOr createExprOr();

  /**
   * Returns a new object of class '<em>Expr And</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr And</em>'.
   * @generated
   */
  ExprAnd createExprAnd();

  /**
   * Returns a new object of class '<em>Expr Equality</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Equality</em>'.
   * @generated
   */
  ExprEquality createExprEquality();

  /**
   * Returns a new object of class '<em>Op Equals</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Equals</em>'.
   * @generated
   */
  OpEquals createOpEquals();

  /**
   * Returns a new object of class '<em>Op Unequals</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Unequals</em>'.
   * @generated
   */
  OpUnequals createOpUnequals();

  /**
   * Returns a new object of class '<em>Expr Comparison</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Comparison</em>'.
   * @generated
   */
  ExprComparison createExprComparison();

  /**
   * Returns a new object of class '<em>Op Less Eq</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Less Eq</em>'.
   * @generated
   */
  OpLessEq createOpLessEq();

  /**
   * Returns a new object of class '<em>Op Less</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Less</em>'.
   * @generated
   */
  OpLess createOpLess();

  /**
   * Returns a new object of class '<em>Op Greater Eq</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Greater Eq</em>'.
   * @generated
   */
  OpGreaterEq createOpGreaterEq();

  /**
   * Returns a new object of class '<em>Op Greater</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Greater</em>'.
   * @generated
   */
  OpGreater createOpGreater();

  /**
   * Returns a new object of class '<em>Expr Additive</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Additive</em>'.
   * @generated
   */
  ExprAdditive createExprAdditive();

  /**
   * Returns a new object of class '<em>Op Plus</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Plus</em>'.
   * @generated
   */
  OpPlus createOpPlus();

  /**
   * Returns a new object of class '<em>Op Minus</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Minus</em>'.
   * @generated
   */
  OpMinus createOpMinus();

  /**
   * Returns a new object of class '<em>Expr Mult</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Mult</em>'.
   * @generated
   */
  ExprMult createExprMult();

  /**
   * Returns a new object of class '<em>Op Mult</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Mult</em>'.
   * @generated
   */
  OpMult createOpMult();

  /**
   * Returns a new object of class '<em>Op Div Real</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Div Real</em>'.
   * @generated
   */
  OpDivReal createOpDivReal();

  /**
   * Returns a new object of class '<em>Op Mod Real</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Mod Real</em>'.
   * @generated
   */
  OpModReal createOpModReal();

  /**
   * Returns a new object of class '<em>Op Mod Int</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Op Mod Int</em>'.
   * @generated
   */
  OpModInt createOpModInt();

  /**
   * Returns a new object of class '<em>Expr Sign</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Sign</em>'.
   * @generated
   */
  ExprSign createExprSign();

  /**
   * Returns a new object of class '<em>Expr Not</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Not</em>'.
   * @generated
   */
  ExprNot createExprNot();

  /**
   * Returns a new object of class '<em>Expr Member</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Member</em>'.
   * @generated
   */
  ExprMember createExprMember();

  /**
   * Returns a new object of class '<em>Expr Int Val</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Int Val</em>'.
   * @generated
   */
  ExprIntVal createExprIntVal();

  /**
   * Returns a new object of class '<em>Expr Num Val</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Num Val</em>'.
   * @generated
   */
  ExprNumVal createExprNumVal();

  /**
   * Returns a new object of class '<em>Expr Strval</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Strval</em>'.
   * @generated
   */
  ExprStrval createExprStrval();

  /**
   * Returns a new object of class '<em>Expr Bool Val</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Bool Val</em>'.
   * @generated
   */
  ExprBoolVal createExprBoolVal();

  /**
   * Returns a new object of class '<em>Expr Func Ref</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Func Ref</em>'.
   * @generated
   */
  ExprFuncRef createExprFuncRef();

  /**
   * Returns a new object of class '<em>Expr Identifier</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Identifier</em>'.
   * @generated
   */
  ExprIdentifier createExprIdentifier();

  /**
   * Returns a new object of class '<em>Expr Functioncall</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Functioncall</em>'.
   * @generated
   */
  ExprFunctioncall createExprFunctioncall();

  /**
   * Returns the package supported by this factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the package supported by this factory.
   * @generated
   */
  PscriptPackage getPscriptPackage();

} //PscriptFactory
