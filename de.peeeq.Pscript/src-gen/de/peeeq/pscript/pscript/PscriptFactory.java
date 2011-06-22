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
   * Returns a new object of class '<em>Name Def</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Name Def</em>'.
   * @generated
   */
  NameDef createNameDef();

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
   * Returns a new object of class '<em>Stmt Expr</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Stmt Expr</em>'.
   * @generated
   */
  StmtExpr createStmtExpr();

  /**
   * Returns a new object of class '<em>Expr</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr</em>'.
   * @generated
   */
  Expr createExpr();

  /**
   * Returns a new object of class '<em>Expr List</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr List</em>'.
   * @generated
   */
  ExprList createExprList();

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
   * Returns a new object of class '<em>Var Def</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Var Def</em>'.
   * @generated
   */
  VarDef createVarDef();

  /**
   * Returns a new object of class '<em>Func Def</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Func Def</em>'.
   * @generated
   */
  FuncDef createFuncDef();

  /**
   * Returns a new object of class '<em>Parameter Def</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Parameter Def</em>'.
   * @generated
   */
  ParameterDef createParameterDef();

  /**
   * Returns a new object of class '<em>Expr Assignment</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Assignment</em>'.
   * @generated
   */
  ExprAssignment createExprAssignment();

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
   * Returns a new object of class '<em>Expr Comparison</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Comparison</em>'.
   * @generated
   */
  ExprComparison createExprComparison();

  /**
   * Returns a new object of class '<em>Expr Additive</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Additive</em>'.
   * @generated
   */
  ExprAdditive createExprAdditive();

  /**
   * Returns a new object of class '<em>Expr Mult</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Mult</em>'.
   * @generated
   */
  ExprMult createExprMult();

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
   * Returns a new object of class '<em>Expr Custom Operator</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Custom Operator</em>'.
   * @generated
   */
  ExprCustomOperator createExprCustomOperator();

  /**
   * Returns a new object of class '<em>Expr Member</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Member</em>'.
   * @generated
   */
  ExprMember createExprMember();

  /**
   * Returns a new object of class '<em>Expr Functioncall</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Functioncall</em>'.
   * @generated
   */
  ExprFunctioncall createExprFunctioncall();

  /**
   * Returns a new object of class '<em>Expr Identifier</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Identifier</em>'.
   * @generated
   */
  ExprIdentifier createExprIdentifier();

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
   * Returns a new object of class '<em>Expr Buildin Function</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Buildin Function</em>'.
   * @generated
   */
  ExprBuildinFunction createExprBuildinFunction();

  /**
   * Returns a new object of class '<em>Expr Buildin Operator</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Expr Buildin Operator</em>'.
   * @generated
   */
  ExprBuildinOperator createExprBuildinOperator();

  /**
   * Returns the package supported by this factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the package supported by this factory.
   * @generated
   */
  PscriptPackage getPscriptPackage();

} //PscriptFactory
