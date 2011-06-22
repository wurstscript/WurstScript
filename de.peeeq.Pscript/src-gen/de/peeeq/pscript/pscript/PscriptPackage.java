/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript;

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
 * @see de.peeeq.pscript.pscript.PscriptFactory
 * @model kind="package"
 * @generated
 */
public interface PscriptPackage extends EPackage
{
  /**
   * The package name.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  String eNAME = "pscript";

  /**
   * The package namespace URI.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  String eNS_URI = "http://www.peeeq.de/pscript/Pscript";

  /**
   * The package namespace name.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  String eNS_PREFIX = "pscript";

  /**
   * The singleton instance of the package.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  PscriptPackage eINSTANCE = de.peeeq.pscript.pscript.impl.PscriptPackageImpl.init();

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ProgramImpl <em>Program</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ProgramImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getProgram()
   * @generated
   */
  int PROGRAM = 0;

  /**
   * The feature id for the '<em><b>Packages</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PROGRAM__PACKAGES = 0;

  /**
   * The number of structural features of the '<em>Program</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PROGRAM_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.PackageDeclarationImpl <em>Package Declaration</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.PackageDeclarationImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getPackageDeclaration()
   * @generated
   */
  int PACKAGE_DECLARATION = 1;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PACKAGE_DECLARATION__NAME = 0;

  /**
   * The feature id for the '<em><b>Imports</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PACKAGE_DECLARATION__IMPORTS = 1;

  /**
   * The feature id for the '<em><b>Elements</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PACKAGE_DECLARATION__ELEMENTS = 2;

  /**
   * The number of structural features of the '<em>Package Declaration</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PACKAGE_DECLARATION_FEATURE_COUNT = 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ImportImpl <em>Import</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ImportImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getImport()
   * @generated
   */
  int IMPORT = 2;

  /**
   * The feature id for the '<em><b>Imported Namespace</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int IMPORT__IMPORTED_NAMESPACE = 0;

  /**
   * The number of structural features of the '<em>Import</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int IMPORT_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.StatementImpl <em>Statement</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.StatementImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStatement()
   * @generated
   */
  int STATEMENT = 6;

  /**
   * The number of structural features of the '<em>Statement</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STATEMENT_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.NameDefImpl <em>Name Def</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.NameDefImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getNameDef()
   * @generated
   */
  int NAME_DEF = 3;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NAME_DEF__NAME = STATEMENT_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Name Def</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NAME_DEF_FEATURE_COUNT = STATEMENT_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.TypeExprImpl <em>Type Expr</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.TypeExprImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getTypeExpr()
   * @generated
   */
  int TYPE_EXPR = 4;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TYPE_EXPR__NAME = 0;

  /**
   * The number of structural features of the '<em>Type Expr</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TYPE_EXPR_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.StatementsImpl <em>Statements</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.StatementsImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStatements()
   * @generated
   */
  int STATEMENTS = 5;

  /**
   * The feature id for the '<em><b>Statements</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STATEMENTS__STATEMENTS = 0;

  /**
   * The number of structural features of the '<em>Statements</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STATEMENTS_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.StmtReturnImpl <em>Stmt Return</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.StmtReturnImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtReturn()
   * @generated
   */
  int STMT_RETURN = 7;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_RETURN__E = STATEMENT_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Stmt Return</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_RETURN_FEATURE_COUNT = STATEMENT_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.StmtIfImpl <em>Stmt If</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.StmtIfImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtIf()
   * @generated
   */
  int STMT_IF = 8;

  /**
   * The feature id for the '<em><b>Cond</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_IF__COND = STATEMENT_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Then Block</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_IF__THEN_BLOCK = STATEMENT_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Else Block</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_IF__ELSE_BLOCK = STATEMENT_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Stmt If</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_IF_FEATURE_COUNT = STATEMENT_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.StmtWhileImpl <em>Stmt While</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.StmtWhileImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtWhile()
   * @generated
   */
  int STMT_WHILE = 9;

  /**
   * The feature id for the '<em><b>Cond</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_WHILE__COND = STATEMENT_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Body</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_WHILE__BODY = STATEMENT_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Stmt While</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_WHILE_FEATURE_COUNT = STATEMENT_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.StmtExprImpl <em>Stmt Expr</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.StmtExprImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtExpr()
   * @generated
   */
  int STMT_EXPR = 10;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_EXPR__E = STATEMENT_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Stmt Expr</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_EXPR_FEATURE_COUNT = STATEMENT_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprImpl <em>Expr</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExpr()
   * @generated
   */
  int EXPR = 11;

  /**
   * The number of structural features of the '<em>Expr</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprListImpl <em>Expr List</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprListImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprList()
   * @generated
   */
  int EXPR_LIST = 12;

  /**
   * The feature id for the '<em><b>Params</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_LIST__PARAMS = 0;

  /**
   * The number of structural features of the '<em>Expr List</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_LIST_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.NativeTypeImpl <em>Native Type</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.NativeTypeImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getNativeType()
   * @generated
   */
  int NATIVE_TYPE = 13;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_TYPE__NAME = NAME_DEF__NAME;

  /**
   * The feature id for the '<em><b>Orig Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_TYPE__ORIG_NAME = NAME_DEF_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Native Type</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_TYPE_FEATURE_COUNT = NAME_DEF_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ClassDefImpl <em>Class Def</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ClassDefImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getClassDef()
   * @generated
   */
  int CLASS_DEF = 14;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CLASS_DEF__NAME = NAME_DEF__NAME;

  /**
   * The feature id for the '<em><b>Members</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CLASS_DEF__MEMBERS = NAME_DEF_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Class Def</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CLASS_DEF_FEATURE_COUNT = NAME_DEF_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.VarDefImpl <em>Var Def</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.VarDefImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getVarDef()
   * @generated
   */
  int VAR_DEF = 15;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VAR_DEF__NAME = NAME_DEF__NAME;

  /**
   * The feature id for the '<em><b>Constant</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VAR_DEF__CONSTANT = NAME_DEF_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Type</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VAR_DEF__TYPE = NAME_DEF_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VAR_DEF__E = NAME_DEF_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Var Def</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VAR_DEF_FEATURE_COUNT = NAME_DEF_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.FuncDefImpl <em>Func Def</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.FuncDefImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getFuncDef()
   * @generated
   */
  int FUNC_DEF = 16;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNC_DEF__NAME = NAME_DEF__NAME;

  /**
   * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNC_DEF__PARAMETERS = NAME_DEF_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Type</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNC_DEF__TYPE = NAME_DEF_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Body</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNC_DEF__BODY = NAME_DEF_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Func Def</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNC_DEF_FEATURE_COUNT = NAME_DEF_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ParameterDefImpl <em>Parameter Def</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ParameterDefImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getParameterDef()
   * @generated
   */
  int PARAMETER_DEF = 17;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PARAMETER_DEF__NAME = NAME_DEF__NAME;

  /**
   * The feature id for the '<em><b>Type</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PARAMETER_DEF__TYPE = NAME_DEF_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Parameter Def</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PARAMETER_DEF_FEATURE_COUNT = NAME_DEF_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprAssignmentImpl <em>Expr Assignment</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprAssignmentImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprAssignment()
   * @generated
   */
  int EXPR_ASSIGNMENT = 18;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_ASSIGNMENT__LEFT = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Op</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_ASSIGNMENT__OP = EXPR_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_ASSIGNMENT__RIGHT = EXPR_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Expr Assignment</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_ASSIGNMENT_FEATURE_COUNT = EXPR_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprOrImpl <em>Expr Or</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprOrImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprOr()
   * @generated
   */
  int EXPR_OR = 19;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_OR__LEFT = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Op</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_OR__OP = EXPR_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_OR__RIGHT = EXPR_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Expr Or</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_OR_FEATURE_COUNT = EXPR_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprAndImpl <em>Expr And</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprAndImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprAnd()
   * @generated
   */
  int EXPR_AND = 20;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_AND__LEFT = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Op</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_AND__OP = EXPR_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_AND__RIGHT = EXPR_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Expr And</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_AND_FEATURE_COUNT = EXPR_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprEqualityImpl <em>Expr Equality</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprEqualityImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprEquality()
   * @generated
   */
  int EXPR_EQUALITY = 21;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_EQUALITY__LEFT = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Op</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_EQUALITY__OP = EXPR_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_EQUALITY__RIGHT = EXPR_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Expr Equality</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_EQUALITY_FEATURE_COUNT = EXPR_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprComparisonImpl <em>Expr Comparison</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprComparisonImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprComparison()
   * @generated
   */
  int EXPR_COMPARISON = 22;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_COMPARISON__LEFT = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Op</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_COMPARISON__OP = EXPR_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_COMPARISON__RIGHT = EXPR_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Expr Comparison</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_COMPARISON_FEATURE_COUNT = EXPR_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprAdditiveImpl <em>Expr Additive</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprAdditiveImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprAdditive()
   * @generated
   */
  int EXPR_ADDITIVE = 23;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_ADDITIVE__LEFT = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Op</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_ADDITIVE__OP = EXPR_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_ADDITIVE__RIGHT = EXPR_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Expr Additive</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_ADDITIVE_FEATURE_COUNT = EXPR_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprMultImpl <em>Expr Mult</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprMultImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprMult()
   * @generated
   */
  int EXPR_MULT = 24;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_MULT__LEFT = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Op</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_MULT__OP = EXPR_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_MULT__RIGHT = EXPR_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Expr Mult</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_MULT_FEATURE_COUNT = EXPR_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprSignImpl <em>Expr Sign</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprSignImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprSign()
   * @generated
   */
  int EXPR_SIGN = 25;

  /**
   * The feature id for the '<em><b>Op</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_SIGN__OP = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_SIGN__RIGHT = EXPR_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Expr Sign</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_SIGN_FEATURE_COUNT = EXPR_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprNotImpl <em>Expr Not</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprNotImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprNot()
   * @generated
   */
  int EXPR_NOT = 26;

  /**
   * The feature id for the '<em><b>Op</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_NOT__OP = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_NOT__RIGHT = EXPR_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Expr Not</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_NOT_FEATURE_COUNT = EXPR_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprCustomOperatorImpl <em>Expr Custom Operator</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprCustomOperatorImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprCustomOperator()
   * @generated
   */
  int EXPR_CUSTOM_OPERATOR = 27;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_CUSTOM_OPERATOR__LEFT = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Op</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_CUSTOM_OPERATOR__OP = EXPR_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_CUSTOM_OPERATOR__RIGHT = EXPR_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Expr Custom Operator</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_CUSTOM_OPERATOR_FEATURE_COUNT = EXPR_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprMemberImpl <em>Expr Member</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprMemberImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprMember()
   * @generated
   */
  int EXPR_MEMBER = 28;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_MEMBER__LEFT = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Op</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_MEMBER__OP = EXPR_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_MEMBER__RIGHT = EXPR_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Expr Member</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_MEMBER_FEATURE_COUNT = EXPR_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprFunctioncallImpl <em>Expr Functioncall</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprFunctioncallImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprFunctioncall()
   * @generated
   */
  int EXPR_FUNCTIONCALL = 29;

  /**
   * The feature id for the '<em><b>Name Val</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_FUNCTIONCALL__NAME_VAL = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Parameters</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_FUNCTIONCALL__PARAMETERS = EXPR_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Expr Functioncall</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_FUNCTIONCALL_FEATURE_COUNT = EXPR_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprIdentifierImpl <em>Expr Identifier</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprIdentifierImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprIdentifier()
   * @generated
   */
  int EXPR_IDENTIFIER = 30;

  /**
   * The feature id for the '<em><b>Name Val</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_IDENTIFIER__NAME_VAL = EXPR_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Expr Identifier</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_IDENTIFIER_FEATURE_COUNT = EXPR_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprIntValImpl <em>Expr Int Val</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprIntValImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprIntVal()
   * @generated
   */
  int EXPR_INT_VAL = 31;

  /**
   * The feature id for the '<em><b>Int Val</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_INT_VAL__INT_VAL = EXPR_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Expr Int Val</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_INT_VAL_FEATURE_COUNT = EXPR_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprNumValImpl <em>Expr Num Val</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprNumValImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprNumVal()
   * @generated
   */
  int EXPR_NUM_VAL = 32;

  /**
   * The feature id for the '<em><b>Num Val</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_NUM_VAL__NUM_VAL = EXPR_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Expr Num Val</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_NUM_VAL_FEATURE_COUNT = EXPR_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprStrvalImpl <em>Expr Strval</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprStrvalImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprStrval()
   * @generated
   */
  int EXPR_STRVAL = 33;

  /**
   * The feature id for the '<em><b>Str Val</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_STRVAL__STR_VAL = EXPR_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Expr Strval</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_STRVAL_FEATURE_COUNT = EXPR_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprBuildinFunctionImpl <em>Expr Buildin Function</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprBuildinFunctionImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprBuildinFunction()
   * @generated
   */
  int EXPR_BUILDIN_FUNCTION = 34;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_BUILDIN_FUNCTION__NAME = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Parameters</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_BUILDIN_FUNCTION__PARAMETERS = EXPR_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Expr Buildin Function</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_BUILDIN_FUNCTION_FEATURE_COUNT = EXPR_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprBuildinOperatorImpl <em>Expr Buildin Operator</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprBuildinOperatorImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprBuildinOperator()
   * @generated
   */
  int EXPR_BUILDIN_OPERATOR = 35;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_BUILDIN_OPERATOR__LEFT = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Op</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_BUILDIN_OPERATOR__OP = EXPR_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_BUILDIN_OPERATOR__RIGHT = EXPR_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Expr Buildin Operator</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_BUILDIN_OPERATOR_FEATURE_COUNT = EXPR_FEATURE_COUNT + 3;


  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.Program <em>Program</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Program</em>'.
   * @see de.peeeq.pscript.pscript.Program
   * @generated
   */
  EClass getProgram();

  /**
   * Returns the meta object for the containment reference list '{@link de.peeeq.pscript.pscript.Program#getPackages <em>Packages</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Packages</em>'.
   * @see de.peeeq.pscript.pscript.Program#getPackages()
   * @see #getProgram()
   * @generated
   */
  EReference getProgram_Packages();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.PackageDeclaration <em>Package Declaration</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Package Declaration</em>'.
   * @see de.peeeq.pscript.pscript.PackageDeclaration
   * @generated
   */
  EClass getPackageDeclaration();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.PackageDeclaration#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see de.peeeq.pscript.pscript.PackageDeclaration#getName()
   * @see #getPackageDeclaration()
   * @generated
   */
  EAttribute getPackageDeclaration_Name();

  /**
   * Returns the meta object for the containment reference list '{@link de.peeeq.pscript.pscript.PackageDeclaration#getImports <em>Imports</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Imports</em>'.
   * @see de.peeeq.pscript.pscript.PackageDeclaration#getImports()
   * @see #getPackageDeclaration()
   * @generated
   */
  EReference getPackageDeclaration_Imports();

  /**
   * Returns the meta object for the containment reference list '{@link de.peeeq.pscript.pscript.PackageDeclaration#getElements <em>Elements</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Elements</em>'.
   * @see de.peeeq.pscript.pscript.PackageDeclaration#getElements()
   * @see #getPackageDeclaration()
   * @generated
   */
  EReference getPackageDeclaration_Elements();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.Import <em>Import</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Import</em>'.
   * @see de.peeeq.pscript.pscript.Import
   * @generated
   */
  EClass getImport();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.Import#getImportedNamespace <em>Imported Namespace</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Imported Namespace</em>'.
   * @see de.peeeq.pscript.pscript.Import#getImportedNamespace()
   * @see #getImport()
   * @generated
   */
  EAttribute getImport_ImportedNamespace();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.NameDef <em>Name Def</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Name Def</em>'.
   * @see de.peeeq.pscript.pscript.NameDef
   * @generated
   */
  EClass getNameDef();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.NameDef#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see de.peeeq.pscript.pscript.NameDef#getName()
   * @see #getNameDef()
   * @generated
   */
  EAttribute getNameDef_Name();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.TypeExpr <em>Type Expr</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Type Expr</em>'.
   * @see de.peeeq.pscript.pscript.TypeExpr
   * @generated
   */
  EClass getTypeExpr();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.TypeExpr#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see de.peeeq.pscript.pscript.TypeExpr#getName()
   * @see #getTypeExpr()
   * @generated
   */
  EAttribute getTypeExpr_Name();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.Statements <em>Statements</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Statements</em>'.
   * @see de.peeeq.pscript.pscript.Statements
   * @generated
   */
  EClass getStatements();

  /**
   * Returns the meta object for the containment reference list '{@link de.peeeq.pscript.pscript.Statements#getStatements <em>Statements</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Statements</em>'.
   * @see de.peeeq.pscript.pscript.Statements#getStatements()
   * @see #getStatements()
   * @generated
   */
  EReference getStatements_Statements();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.Statement <em>Statement</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Statement</em>'.
   * @see de.peeeq.pscript.pscript.Statement
   * @generated
   */
  EClass getStatement();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.StmtReturn <em>Stmt Return</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Stmt Return</em>'.
   * @see de.peeeq.pscript.pscript.StmtReturn
   * @generated
   */
  EClass getStmtReturn();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.StmtReturn#getE <em>E</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>E</em>'.
   * @see de.peeeq.pscript.pscript.StmtReturn#getE()
   * @see #getStmtReturn()
   * @generated
   */
  EReference getStmtReturn_E();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.StmtIf <em>Stmt If</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Stmt If</em>'.
   * @see de.peeeq.pscript.pscript.StmtIf
   * @generated
   */
  EClass getStmtIf();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.StmtIf#getCond <em>Cond</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Cond</em>'.
   * @see de.peeeq.pscript.pscript.StmtIf#getCond()
   * @see #getStmtIf()
   * @generated
   */
  EReference getStmtIf_Cond();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.StmtIf#getThenBlock <em>Then Block</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Then Block</em>'.
   * @see de.peeeq.pscript.pscript.StmtIf#getThenBlock()
   * @see #getStmtIf()
   * @generated
   */
  EReference getStmtIf_ThenBlock();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.StmtIf#getElseBlock <em>Else Block</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Else Block</em>'.
   * @see de.peeeq.pscript.pscript.StmtIf#getElseBlock()
   * @see #getStmtIf()
   * @generated
   */
  EReference getStmtIf_ElseBlock();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.StmtWhile <em>Stmt While</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Stmt While</em>'.
   * @see de.peeeq.pscript.pscript.StmtWhile
   * @generated
   */
  EClass getStmtWhile();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.StmtWhile#getCond <em>Cond</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Cond</em>'.
   * @see de.peeeq.pscript.pscript.StmtWhile#getCond()
   * @see #getStmtWhile()
   * @generated
   */
  EReference getStmtWhile_Cond();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.StmtWhile#getBody <em>Body</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Body</em>'.
   * @see de.peeeq.pscript.pscript.StmtWhile#getBody()
   * @see #getStmtWhile()
   * @generated
   */
  EReference getStmtWhile_Body();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.StmtExpr <em>Stmt Expr</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Stmt Expr</em>'.
   * @see de.peeeq.pscript.pscript.StmtExpr
   * @generated
   */
  EClass getStmtExpr();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.StmtExpr#getE <em>E</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>E</em>'.
   * @see de.peeeq.pscript.pscript.StmtExpr#getE()
   * @see #getStmtExpr()
   * @generated
   */
  EReference getStmtExpr_E();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.Expr <em>Expr</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr</em>'.
   * @see de.peeeq.pscript.pscript.Expr
   * @generated
   */
  EClass getExpr();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprList <em>Expr List</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr List</em>'.
   * @see de.peeeq.pscript.pscript.ExprList
   * @generated
   */
  EClass getExprList();

  /**
   * Returns the meta object for the containment reference list '{@link de.peeeq.pscript.pscript.ExprList#getParams <em>Params</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Params</em>'.
   * @see de.peeeq.pscript.pscript.ExprList#getParams()
   * @see #getExprList()
   * @generated
   */
  EReference getExprList_Params();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.NativeType <em>Native Type</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Native Type</em>'.
   * @see de.peeeq.pscript.pscript.NativeType
   * @generated
   */
  EClass getNativeType();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.NativeType#getOrigName <em>Orig Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Orig Name</em>'.
   * @see de.peeeq.pscript.pscript.NativeType#getOrigName()
   * @see #getNativeType()
   * @generated
   */
  EAttribute getNativeType_OrigName();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ClassDef <em>Class Def</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Class Def</em>'.
   * @see de.peeeq.pscript.pscript.ClassDef
   * @generated
   */
  EClass getClassDef();

  /**
   * Returns the meta object for the containment reference list '{@link de.peeeq.pscript.pscript.ClassDef#getMembers <em>Members</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Members</em>'.
   * @see de.peeeq.pscript.pscript.ClassDef#getMembers()
   * @see #getClassDef()
   * @generated
   */
  EReference getClassDef_Members();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.VarDef <em>Var Def</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Var Def</em>'.
   * @see de.peeeq.pscript.pscript.VarDef
   * @generated
   */
  EClass getVarDef();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.VarDef#isConstant <em>Constant</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Constant</em>'.
   * @see de.peeeq.pscript.pscript.VarDef#isConstant()
   * @see #getVarDef()
   * @generated
   */
  EAttribute getVarDef_Constant();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.VarDef#getType <em>Type</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Type</em>'.
   * @see de.peeeq.pscript.pscript.VarDef#getType()
   * @see #getVarDef()
   * @generated
   */
  EReference getVarDef_Type();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.VarDef#getE <em>E</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>E</em>'.
   * @see de.peeeq.pscript.pscript.VarDef#getE()
   * @see #getVarDef()
   * @generated
   */
  EReference getVarDef_E();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.FuncDef <em>Func Def</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Func Def</em>'.
   * @see de.peeeq.pscript.pscript.FuncDef
   * @generated
   */
  EClass getFuncDef();

  /**
   * Returns the meta object for the containment reference list '{@link de.peeeq.pscript.pscript.FuncDef#getParameters <em>Parameters</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Parameters</em>'.
   * @see de.peeeq.pscript.pscript.FuncDef#getParameters()
   * @see #getFuncDef()
   * @generated
   */
  EReference getFuncDef_Parameters();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.FuncDef#getType <em>Type</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Type</em>'.
   * @see de.peeeq.pscript.pscript.FuncDef#getType()
   * @see #getFuncDef()
   * @generated
   */
  EReference getFuncDef_Type();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.FuncDef#getBody <em>Body</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Body</em>'.
   * @see de.peeeq.pscript.pscript.FuncDef#getBody()
   * @see #getFuncDef()
   * @generated
   */
  EReference getFuncDef_Body();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ParameterDef <em>Parameter Def</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Parameter Def</em>'.
   * @see de.peeeq.pscript.pscript.ParameterDef
   * @generated
   */
  EClass getParameterDef();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ParameterDef#getType <em>Type</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Type</em>'.
   * @see de.peeeq.pscript.pscript.ParameterDef#getType()
   * @see #getParameterDef()
   * @generated
   */
  EReference getParameterDef_Type();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprAssignment <em>Expr Assignment</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Assignment</em>'.
   * @see de.peeeq.pscript.pscript.ExprAssignment
   * @generated
   */
  EClass getExprAssignment();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprAssignment#getLeft <em>Left</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Left</em>'.
   * @see de.peeeq.pscript.pscript.ExprAssignment#getLeft()
   * @see #getExprAssignment()
   * @generated
   */
  EReference getExprAssignment_Left();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprAssignment#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprAssignment#getOp()
   * @see #getExprAssignment()
   * @generated
   */
  EAttribute getExprAssignment_Op();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprAssignment#getRight <em>Right</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Right</em>'.
   * @see de.peeeq.pscript.pscript.ExprAssignment#getRight()
   * @see #getExprAssignment()
   * @generated
   */
  EReference getExprAssignment_Right();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprOr <em>Expr Or</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Or</em>'.
   * @see de.peeeq.pscript.pscript.ExprOr
   * @generated
   */
  EClass getExprOr();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprOr#getLeft <em>Left</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Left</em>'.
   * @see de.peeeq.pscript.pscript.ExprOr#getLeft()
   * @see #getExprOr()
   * @generated
   */
  EReference getExprOr_Left();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprOr#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprOr#getOp()
   * @see #getExprOr()
   * @generated
   */
  EAttribute getExprOr_Op();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprOr#getRight <em>Right</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Right</em>'.
   * @see de.peeeq.pscript.pscript.ExprOr#getRight()
   * @see #getExprOr()
   * @generated
   */
  EReference getExprOr_Right();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprAnd <em>Expr And</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr And</em>'.
   * @see de.peeeq.pscript.pscript.ExprAnd
   * @generated
   */
  EClass getExprAnd();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprAnd#getLeft <em>Left</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Left</em>'.
   * @see de.peeeq.pscript.pscript.ExprAnd#getLeft()
   * @see #getExprAnd()
   * @generated
   */
  EReference getExprAnd_Left();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprAnd#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprAnd#getOp()
   * @see #getExprAnd()
   * @generated
   */
  EAttribute getExprAnd_Op();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprAnd#getRight <em>Right</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Right</em>'.
   * @see de.peeeq.pscript.pscript.ExprAnd#getRight()
   * @see #getExprAnd()
   * @generated
   */
  EReference getExprAnd_Right();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprEquality <em>Expr Equality</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Equality</em>'.
   * @see de.peeeq.pscript.pscript.ExprEquality
   * @generated
   */
  EClass getExprEquality();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprEquality#getLeft <em>Left</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Left</em>'.
   * @see de.peeeq.pscript.pscript.ExprEquality#getLeft()
   * @see #getExprEquality()
   * @generated
   */
  EReference getExprEquality_Left();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprEquality#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprEquality#getOp()
   * @see #getExprEquality()
   * @generated
   */
  EAttribute getExprEquality_Op();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprEquality#getRight <em>Right</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Right</em>'.
   * @see de.peeeq.pscript.pscript.ExprEquality#getRight()
   * @see #getExprEquality()
   * @generated
   */
  EReference getExprEquality_Right();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprComparison <em>Expr Comparison</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Comparison</em>'.
   * @see de.peeeq.pscript.pscript.ExprComparison
   * @generated
   */
  EClass getExprComparison();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprComparison#getLeft <em>Left</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Left</em>'.
   * @see de.peeeq.pscript.pscript.ExprComparison#getLeft()
   * @see #getExprComparison()
   * @generated
   */
  EReference getExprComparison_Left();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprComparison#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprComparison#getOp()
   * @see #getExprComparison()
   * @generated
   */
  EAttribute getExprComparison_Op();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprComparison#getRight <em>Right</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Right</em>'.
   * @see de.peeeq.pscript.pscript.ExprComparison#getRight()
   * @see #getExprComparison()
   * @generated
   */
  EReference getExprComparison_Right();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprAdditive <em>Expr Additive</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Additive</em>'.
   * @see de.peeeq.pscript.pscript.ExprAdditive
   * @generated
   */
  EClass getExprAdditive();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprAdditive#getLeft <em>Left</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Left</em>'.
   * @see de.peeeq.pscript.pscript.ExprAdditive#getLeft()
   * @see #getExprAdditive()
   * @generated
   */
  EReference getExprAdditive_Left();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprAdditive#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprAdditive#getOp()
   * @see #getExprAdditive()
   * @generated
   */
  EAttribute getExprAdditive_Op();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprAdditive#getRight <em>Right</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Right</em>'.
   * @see de.peeeq.pscript.pscript.ExprAdditive#getRight()
   * @see #getExprAdditive()
   * @generated
   */
  EReference getExprAdditive_Right();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprMult <em>Expr Mult</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Mult</em>'.
   * @see de.peeeq.pscript.pscript.ExprMult
   * @generated
   */
  EClass getExprMult();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprMult#getLeft <em>Left</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Left</em>'.
   * @see de.peeeq.pscript.pscript.ExprMult#getLeft()
   * @see #getExprMult()
   * @generated
   */
  EReference getExprMult_Left();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprMult#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprMult#getOp()
   * @see #getExprMult()
   * @generated
   */
  EAttribute getExprMult_Op();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprMult#getRight <em>Right</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Right</em>'.
   * @see de.peeeq.pscript.pscript.ExprMult#getRight()
   * @see #getExprMult()
   * @generated
   */
  EReference getExprMult_Right();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprSign <em>Expr Sign</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Sign</em>'.
   * @see de.peeeq.pscript.pscript.ExprSign
   * @generated
   */
  EClass getExprSign();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprSign#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprSign#getOp()
   * @see #getExprSign()
   * @generated
   */
  EAttribute getExprSign_Op();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprSign#getRight <em>Right</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Right</em>'.
   * @see de.peeeq.pscript.pscript.ExprSign#getRight()
   * @see #getExprSign()
   * @generated
   */
  EReference getExprSign_Right();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprNot <em>Expr Not</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Not</em>'.
   * @see de.peeeq.pscript.pscript.ExprNot
   * @generated
   */
  EClass getExprNot();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprNot#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprNot#getOp()
   * @see #getExprNot()
   * @generated
   */
  EAttribute getExprNot_Op();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprNot#getRight <em>Right</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Right</em>'.
   * @see de.peeeq.pscript.pscript.ExprNot#getRight()
   * @see #getExprNot()
   * @generated
   */
  EReference getExprNot_Right();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprCustomOperator <em>Expr Custom Operator</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Custom Operator</em>'.
   * @see de.peeeq.pscript.pscript.ExprCustomOperator
   * @generated
   */
  EClass getExprCustomOperator();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprCustomOperator#getLeft <em>Left</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Left</em>'.
   * @see de.peeeq.pscript.pscript.ExprCustomOperator#getLeft()
   * @see #getExprCustomOperator()
   * @generated
   */
  EReference getExprCustomOperator_Left();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprCustomOperator#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprCustomOperator#getOp()
   * @see #getExprCustomOperator()
   * @generated
   */
  EAttribute getExprCustomOperator_Op();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprCustomOperator#getRight <em>Right</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Right</em>'.
   * @see de.peeeq.pscript.pscript.ExprCustomOperator#getRight()
   * @see #getExprCustomOperator()
   * @generated
   */
  EReference getExprCustomOperator_Right();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprMember <em>Expr Member</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Member</em>'.
   * @see de.peeeq.pscript.pscript.ExprMember
   * @generated
   */
  EClass getExprMember();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprMember#getLeft <em>Left</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Left</em>'.
   * @see de.peeeq.pscript.pscript.ExprMember#getLeft()
   * @see #getExprMember()
   * @generated
   */
  EReference getExprMember_Left();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprMember#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprMember#getOp()
   * @see #getExprMember()
   * @generated
   */
  EAttribute getExprMember_Op();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprMember#getRight <em>Right</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Right</em>'.
   * @see de.peeeq.pscript.pscript.ExprMember#getRight()
   * @see #getExprMember()
   * @generated
   */
  EReference getExprMember_Right();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprFunctioncall <em>Expr Functioncall</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Functioncall</em>'.
   * @see de.peeeq.pscript.pscript.ExprFunctioncall
   * @generated
   */
  EClass getExprFunctioncall();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprFunctioncall#getNameVal <em>Name Val</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name Val</em>'.
   * @see de.peeeq.pscript.pscript.ExprFunctioncall#getNameVal()
   * @see #getExprFunctioncall()
   * @generated
   */
  EAttribute getExprFunctioncall_NameVal();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprFunctioncall#getParameters <em>Parameters</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Parameters</em>'.
   * @see de.peeeq.pscript.pscript.ExprFunctioncall#getParameters()
   * @see #getExprFunctioncall()
   * @generated
   */
  EReference getExprFunctioncall_Parameters();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprIdentifier <em>Expr Identifier</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Identifier</em>'.
   * @see de.peeeq.pscript.pscript.ExprIdentifier
   * @generated
   */
  EClass getExprIdentifier();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprIdentifier#getNameVal <em>Name Val</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name Val</em>'.
   * @see de.peeeq.pscript.pscript.ExprIdentifier#getNameVal()
   * @see #getExprIdentifier()
   * @generated
   */
  EAttribute getExprIdentifier_NameVal();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprIntVal <em>Expr Int Val</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Int Val</em>'.
   * @see de.peeeq.pscript.pscript.ExprIntVal
   * @generated
   */
  EClass getExprIntVal();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprIntVal#getIntVal <em>Int Val</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Int Val</em>'.
   * @see de.peeeq.pscript.pscript.ExprIntVal#getIntVal()
   * @see #getExprIntVal()
   * @generated
   */
  EAttribute getExprIntVal_IntVal();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprNumVal <em>Expr Num Val</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Num Val</em>'.
   * @see de.peeeq.pscript.pscript.ExprNumVal
   * @generated
   */
  EClass getExprNumVal();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprNumVal#getNumVal <em>Num Val</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Num Val</em>'.
   * @see de.peeeq.pscript.pscript.ExprNumVal#getNumVal()
   * @see #getExprNumVal()
   * @generated
   */
  EAttribute getExprNumVal_NumVal();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprStrval <em>Expr Strval</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Strval</em>'.
   * @see de.peeeq.pscript.pscript.ExprStrval
   * @generated
   */
  EClass getExprStrval();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprStrval#getStrVal <em>Str Val</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Str Val</em>'.
   * @see de.peeeq.pscript.pscript.ExprStrval#getStrVal()
   * @see #getExprStrval()
   * @generated
   */
  EAttribute getExprStrval_StrVal();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprBuildinFunction <em>Expr Buildin Function</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Buildin Function</em>'.
   * @see de.peeeq.pscript.pscript.ExprBuildinFunction
   * @generated
   */
  EClass getExprBuildinFunction();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprBuildinFunction#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see de.peeeq.pscript.pscript.ExprBuildinFunction#getName()
   * @see #getExprBuildinFunction()
   * @generated
   */
  EAttribute getExprBuildinFunction_Name();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprBuildinFunction#getParameters <em>Parameters</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Parameters</em>'.
   * @see de.peeeq.pscript.pscript.ExprBuildinFunction#getParameters()
   * @see #getExprBuildinFunction()
   * @generated
   */
  EReference getExprBuildinFunction_Parameters();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprBuildinOperator <em>Expr Buildin Operator</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Buildin Operator</em>'.
   * @see de.peeeq.pscript.pscript.ExprBuildinOperator
   * @generated
   */
  EClass getExprBuildinOperator();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprBuildinOperator#getLeft <em>Left</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Left</em>'.
   * @see de.peeeq.pscript.pscript.ExprBuildinOperator#getLeft()
   * @see #getExprBuildinOperator()
   * @generated
   */
  EReference getExprBuildinOperator_Left();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprBuildinOperator#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprBuildinOperator#getOp()
   * @see #getExprBuildinOperator()
   * @generated
   */
  EAttribute getExprBuildinOperator_Op();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprBuildinOperator#getRight <em>Right</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Right</em>'.
   * @see de.peeeq.pscript.pscript.ExprBuildinOperator#getRight()
   * @see #getExprBuildinOperator()
   * @generated
   */
  EReference getExprBuildinOperator_Right();

  /**
   * Returns the factory that creates the instances of the model.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the factory that creates the instances of the model.
   * @generated
   */
  PscriptFactory getPscriptFactory();

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
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ProgramImpl <em>Program</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ProgramImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getProgram()
     * @generated
     */
    EClass PROGRAM = eINSTANCE.getProgram();

    /**
     * The meta object literal for the '<em><b>Packages</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference PROGRAM__PACKAGES = eINSTANCE.getProgram_Packages();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.PackageDeclarationImpl <em>Package Declaration</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.PackageDeclarationImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getPackageDeclaration()
     * @generated
     */
    EClass PACKAGE_DECLARATION = eINSTANCE.getPackageDeclaration();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute PACKAGE_DECLARATION__NAME = eINSTANCE.getPackageDeclaration_Name();

    /**
     * The meta object literal for the '<em><b>Imports</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference PACKAGE_DECLARATION__IMPORTS = eINSTANCE.getPackageDeclaration_Imports();

    /**
     * The meta object literal for the '<em><b>Elements</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference PACKAGE_DECLARATION__ELEMENTS = eINSTANCE.getPackageDeclaration_Elements();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ImportImpl <em>Import</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ImportImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getImport()
     * @generated
     */
    EClass IMPORT = eINSTANCE.getImport();

    /**
     * The meta object literal for the '<em><b>Imported Namespace</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute IMPORT__IMPORTED_NAMESPACE = eINSTANCE.getImport_ImportedNamespace();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.NameDefImpl <em>Name Def</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.NameDefImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getNameDef()
     * @generated
     */
    EClass NAME_DEF = eINSTANCE.getNameDef();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute NAME_DEF__NAME = eINSTANCE.getNameDef_Name();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.TypeExprImpl <em>Type Expr</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.TypeExprImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getTypeExpr()
     * @generated
     */
    EClass TYPE_EXPR = eINSTANCE.getTypeExpr();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute TYPE_EXPR__NAME = eINSTANCE.getTypeExpr_Name();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.StatementsImpl <em>Statements</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.StatementsImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStatements()
     * @generated
     */
    EClass STATEMENTS = eINSTANCE.getStatements();

    /**
     * The meta object literal for the '<em><b>Statements</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference STATEMENTS__STATEMENTS = eINSTANCE.getStatements_Statements();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.StatementImpl <em>Statement</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.StatementImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStatement()
     * @generated
     */
    EClass STATEMENT = eINSTANCE.getStatement();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.StmtReturnImpl <em>Stmt Return</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.StmtReturnImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtReturn()
     * @generated
     */
    EClass STMT_RETURN = eINSTANCE.getStmtReturn();

    /**
     * The meta object literal for the '<em><b>E</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference STMT_RETURN__E = eINSTANCE.getStmtReturn_E();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.StmtIfImpl <em>Stmt If</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.StmtIfImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtIf()
     * @generated
     */
    EClass STMT_IF = eINSTANCE.getStmtIf();

    /**
     * The meta object literal for the '<em><b>Cond</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference STMT_IF__COND = eINSTANCE.getStmtIf_Cond();

    /**
     * The meta object literal for the '<em><b>Then Block</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference STMT_IF__THEN_BLOCK = eINSTANCE.getStmtIf_ThenBlock();

    /**
     * The meta object literal for the '<em><b>Else Block</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference STMT_IF__ELSE_BLOCK = eINSTANCE.getStmtIf_ElseBlock();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.StmtWhileImpl <em>Stmt While</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.StmtWhileImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtWhile()
     * @generated
     */
    EClass STMT_WHILE = eINSTANCE.getStmtWhile();

    /**
     * The meta object literal for the '<em><b>Cond</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference STMT_WHILE__COND = eINSTANCE.getStmtWhile_Cond();

    /**
     * The meta object literal for the '<em><b>Body</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference STMT_WHILE__BODY = eINSTANCE.getStmtWhile_Body();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.StmtExprImpl <em>Stmt Expr</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.StmtExprImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtExpr()
     * @generated
     */
    EClass STMT_EXPR = eINSTANCE.getStmtExpr();

    /**
     * The meta object literal for the '<em><b>E</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference STMT_EXPR__E = eINSTANCE.getStmtExpr_E();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprImpl <em>Expr</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExpr()
     * @generated
     */
    EClass EXPR = eINSTANCE.getExpr();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprListImpl <em>Expr List</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprListImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprList()
     * @generated
     */
    EClass EXPR_LIST = eINSTANCE.getExprList();

    /**
     * The meta object literal for the '<em><b>Params</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_LIST__PARAMS = eINSTANCE.getExprList_Params();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.NativeTypeImpl <em>Native Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.NativeTypeImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getNativeType()
     * @generated
     */
    EClass NATIVE_TYPE = eINSTANCE.getNativeType();

    /**
     * The meta object literal for the '<em><b>Orig Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute NATIVE_TYPE__ORIG_NAME = eINSTANCE.getNativeType_OrigName();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ClassDefImpl <em>Class Def</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ClassDefImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getClassDef()
     * @generated
     */
    EClass CLASS_DEF = eINSTANCE.getClassDef();

    /**
     * The meta object literal for the '<em><b>Members</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference CLASS_DEF__MEMBERS = eINSTANCE.getClassDef_Members();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.VarDefImpl <em>Var Def</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.VarDefImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getVarDef()
     * @generated
     */
    EClass VAR_DEF = eINSTANCE.getVarDef();

    /**
     * The meta object literal for the '<em><b>Constant</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute VAR_DEF__CONSTANT = eINSTANCE.getVarDef_Constant();

    /**
     * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference VAR_DEF__TYPE = eINSTANCE.getVarDef_Type();

    /**
     * The meta object literal for the '<em><b>E</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference VAR_DEF__E = eINSTANCE.getVarDef_E();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.FuncDefImpl <em>Func Def</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.FuncDefImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getFuncDef()
     * @generated
     */
    EClass FUNC_DEF = eINSTANCE.getFuncDef();

    /**
     * The meta object literal for the '<em><b>Parameters</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FUNC_DEF__PARAMETERS = eINSTANCE.getFuncDef_Parameters();

    /**
     * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FUNC_DEF__TYPE = eINSTANCE.getFuncDef_Type();

    /**
     * The meta object literal for the '<em><b>Body</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference FUNC_DEF__BODY = eINSTANCE.getFuncDef_Body();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ParameterDefImpl <em>Parameter Def</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ParameterDefImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getParameterDef()
     * @generated
     */
    EClass PARAMETER_DEF = eINSTANCE.getParameterDef();

    /**
     * The meta object literal for the '<em><b>Type</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference PARAMETER_DEF__TYPE = eINSTANCE.getParameterDef_Type();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprAssignmentImpl <em>Expr Assignment</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprAssignmentImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprAssignment()
     * @generated
     */
    EClass EXPR_ASSIGNMENT = eINSTANCE.getExprAssignment();

    /**
     * The meta object literal for the '<em><b>Left</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_ASSIGNMENT__LEFT = eINSTANCE.getExprAssignment_Left();

    /**
     * The meta object literal for the '<em><b>Op</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_ASSIGNMENT__OP = eINSTANCE.getExprAssignment_Op();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_ASSIGNMENT__RIGHT = eINSTANCE.getExprAssignment_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprOrImpl <em>Expr Or</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprOrImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprOr()
     * @generated
     */
    EClass EXPR_OR = eINSTANCE.getExprOr();

    /**
     * The meta object literal for the '<em><b>Left</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_OR__LEFT = eINSTANCE.getExprOr_Left();

    /**
     * The meta object literal for the '<em><b>Op</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_OR__OP = eINSTANCE.getExprOr_Op();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_OR__RIGHT = eINSTANCE.getExprOr_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprAndImpl <em>Expr And</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprAndImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprAnd()
     * @generated
     */
    EClass EXPR_AND = eINSTANCE.getExprAnd();

    /**
     * The meta object literal for the '<em><b>Left</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_AND__LEFT = eINSTANCE.getExprAnd_Left();

    /**
     * The meta object literal for the '<em><b>Op</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_AND__OP = eINSTANCE.getExprAnd_Op();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_AND__RIGHT = eINSTANCE.getExprAnd_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprEqualityImpl <em>Expr Equality</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprEqualityImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprEquality()
     * @generated
     */
    EClass EXPR_EQUALITY = eINSTANCE.getExprEquality();

    /**
     * The meta object literal for the '<em><b>Left</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_EQUALITY__LEFT = eINSTANCE.getExprEquality_Left();

    /**
     * The meta object literal for the '<em><b>Op</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_EQUALITY__OP = eINSTANCE.getExprEquality_Op();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_EQUALITY__RIGHT = eINSTANCE.getExprEquality_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprComparisonImpl <em>Expr Comparison</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprComparisonImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprComparison()
     * @generated
     */
    EClass EXPR_COMPARISON = eINSTANCE.getExprComparison();

    /**
     * The meta object literal for the '<em><b>Left</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_COMPARISON__LEFT = eINSTANCE.getExprComparison_Left();

    /**
     * The meta object literal for the '<em><b>Op</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_COMPARISON__OP = eINSTANCE.getExprComparison_Op();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_COMPARISON__RIGHT = eINSTANCE.getExprComparison_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprAdditiveImpl <em>Expr Additive</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprAdditiveImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprAdditive()
     * @generated
     */
    EClass EXPR_ADDITIVE = eINSTANCE.getExprAdditive();

    /**
     * The meta object literal for the '<em><b>Left</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_ADDITIVE__LEFT = eINSTANCE.getExprAdditive_Left();

    /**
     * The meta object literal for the '<em><b>Op</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_ADDITIVE__OP = eINSTANCE.getExprAdditive_Op();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_ADDITIVE__RIGHT = eINSTANCE.getExprAdditive_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprMultImpl <em>Expr Mult</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprMultImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprMult()
     * @generated
     */
    EClass EXPR_MULT = eINSTANCE.getExprMult();

    /**
     * The meta object literal for the '<em><b>Left</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_MULT__LEFT = eINSTANCE.getExprMult_Left();

    /**
     * The meta object literal for the '<em><b>Op</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_MULT__OP = eINSTANCE.getExprMult_Op();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_MULT__RIGHT = eINSTANCE.getExprMult_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprSignImpl <em>Expr Sign</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprSignImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprSign()
     * @generated
     */
    EClass EXPR_SIGN = eINSTANCE.getExprSign();

    /**
     * The meta object literal for the '<em><b>Op</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_SIGN__OP = eINSTANCE.getExprSign_Op();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_SIGN__RIGHT = eINSTANCE.getExprSign_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprNotImpl <em>Expr Not</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprNotImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprNot()
     * @generated
     */
    EClass EXPR_NOT = eINSTANCE.getExprNot();

    /**
     * The meta object literal for the '<em><b>Op</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_NOT__OP = eINSTANCE.getExprNot_Op();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_NOT__RIGHT = eINSTANCE.getExprNot_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprCustomOperatorImpl <em>Expr Custom Operator</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprCustomOperatorImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprCustomOperator()
     * @generated
     */
    EClass EXPR_CUSTOM_OPERATOR = eINSTANCE.getExprCustomOperator();

    /**
     * The meta object literal for the '<em><b>Left</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_CUSTOM_OPERATOR__LEFT = eINSTANCE.getExprCustomOperator_Left();

    /**
     * The meta object literal for the '<em><b>Op</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_CUSTOM_OPERATOR__OP = eINSTANCE.getExprCustomOperator_Op();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_CUSTOM_OPERATOR__RIGHT = eINSTANCE.getExprCustomOperator_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprMemberImpl <em>Expr Member</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprMemberImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprMember()
     * @generated
     */
    EClass EXPR_MEMBER = eINSTANCE.getExprMember();

    /**
     * The meta object literal for the '<em><b>Left</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_MEMBER__LEFT = eINSTANCE.getExprMember_Left();

    /**
     * The meta object literal for the '<em><b>Op</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_MEMBER__OP = eINSTANCE.getExprMember_Op();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_MEMBER__RIGHT = eINSTANCE.getExprMember_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprFunctioncallImpl <em>Expr Functioncall</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprFunctioncallImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprFunctioncall()
     * @generated
     */
    EClass EXPR_FUNCTIONCALL = eINSTANCE.getExprFunctioncall();

    /**
     * The meta object literal for the '<em><b>Name Val</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_FUNCTIONCALL__NAME_VAL = eINSTANCE.getExprFunctioncall_NameVal();

    /**
     * The meta object literal for the '<em><b>Parameters</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_FUNCTIONCALL__PARAMETERS = eINSTANCE.getExprFunctioncall_Parameters();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprIdentifierImpl <em>Expr Identifier</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprIdentifierImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprIdentifier()
     * @generated
     */
    EClass EXPR_IDENTIFIER = eINSTANCE.getExprIdentifier();

    /**
     * The meta object literal for the '<em><b>Name Val</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_IDENTIFIER__NAME_VAL = eINSTANCE.getExprIdentifier_NameVal();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprIntValImpl <em>Expr Int Val</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprIntValImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprIntVal()
     * @generated
     */
    EClass EXPR_INT_VAL = eINSTANCE.getExprIntVal();

    /**
     * The meta object literal for the '<em><b>Int Val</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_INT_VAL__INT_VAL = eINSTANCE.getExprIntVal_IntVal();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprNumValImpl <em>Expr Num Val</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprNumValImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprNumVal()
     * @generated
     */
    EClass EXPR_NUM_VAL = eINSTANCE.getExprNumVal();

    /**
     * The meta object literal for the '<em><b>Num Val</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_NUM_VAL__NUM_VAL = eINSTANCE.getExprNumVal_NumVal();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprStrvalImpl <em>Expr Strval</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprStrvalImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprStrval()
     * @generated
     */
    EClass EXPR_STRVAL = eINSTANCE.getExprStrval();

    /**
     * The meta object literal for the '<em><b>Str Val</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_STRVAL__STR_VAL = eINSTANCE.getExprStrval_StrVal();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprBuildinFunctionImpl <em>Expr Buildin Function</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprBuildinFunctionImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprBuildinFunction()
     * @generated
     */
    EClass EXPR_BUILDIN_FUNCTION = eINSTANCE.getExprBuildinFunction();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_BUILDIN_FUNCTION__NAME = eINSTANCE.getExprBuildinFunction_Name();

    /**
     * The meta object literal for the '<em><b>Parameters</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_BUILDIN_FUNCTION__PARAMETERS = eINSTANCE.getExprBuildinFunction_Parameters();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprBuildinOperatorImpl <em>Expr Buildin Operator</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprBuildinOperatorImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprBuildinOperator()
     * @generated
     */
    EClass EXPR_BUILDIN_OPERATOR = eINSTANCE.getExprBuildinOperator();

    /**
     * The meta object literal for the '<em><b>Left</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_BUILDIN_OPERATOR__LEFT = eINSTANCE.getExprBuildinOperator_Left();

    /**
     * The meta object literal for the '<em><b>Op</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_BUILDIN_OPERATOR__OP = eINSTANCE.getExprBuildinOperator_Op();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_BUILDIN_OPERATOR__RIGHT = eINSTANCE.getExprBuildinOperator_Right();

  }

} //PscriptPackage
