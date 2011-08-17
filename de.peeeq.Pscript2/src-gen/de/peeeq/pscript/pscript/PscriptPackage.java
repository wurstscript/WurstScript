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
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.EntityImpl <em>Entity</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.EntityImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getEntity()
   * @generated
   */
  int ENTITY = 3;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ENTITY__NAME = 0;

  /**
   * The number of structural features of the '<em>Entity</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ENTITY_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.InitBlockImpl <em>Init Block</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.InitBlockImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getInitBlock()
   * @generated
   */
  int INIT_BLOCK = 4;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int INIT_BLOCK__NAME = ENTITY__NAME;

  /**
   * The feature id for the '<em><b>Body</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int INIT_BLOCK__BODY = ENTITY_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Init Block</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int INIT_BLOCK_FEATURE_COUNT = ENTITY_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.TypeDefImpl <em>Type Def</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.TypeDefImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getTypeDef()
   * @generated
   */
  int TYPE_DEF = 5;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TYPE_DEF__NAME = ENTITY__NAME;

  /**
   * The number of structural features of the '<em>Type Def</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TYPE_DEF_FEATURE_COUNT = ENTITY_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.FuncDefImpl <em>Func Def</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.FuncDefImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getFuncDef()
   * @generated
   */
  int FUNC_DEF = 6;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNC_DEF__NAME = ENTITY__NAME;

  /**
   * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNC_DEF__PARAMETERS = ENTITY_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Type</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNC_DEF__TYPE = ENTITY_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Body</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNC_DEF__BODY = ENTITY_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Func Def</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int FUNC_DEF_FEATURE_COUNT = ENTITY_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ClassSlotsImpl <em>Class Slots</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ClassSlotsImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getClassSlots()
   * @generated
   */
  int CLASS_SLOTS = 7;

  /**
   * The number of structural features of the '<em>Class Slots</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CLASS_SLOTS_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ClassMemberImpl <em>Class Member</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ClassMemberImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getClassMember()
   * @generated
   */
  int CLASS_MEMBER = 8;

  /**
   * The number of structural features of the '<em>Class Member</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CLASS_MEMBER_FEATURE_COUNT = CLASS_SLOTS_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.VarDefImpl <em>Var Def</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.VarDefImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getVarDef()
   * @generated
   */
  int VAR_DEF = 9;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VAR_DEF__NAME = ENTITY__NAME;

  /**
   * The feature id for the '<em><b>Constant</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VAR_DEF__CONSTANT = ENTITY_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Type</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VAR_DEF__TYPE = ENTITY_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VAR_DEF__E = ENTITY_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Var Def</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int VAR_DEF_FEATURE_COUNT = ENTITY_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.TypeExprImpl <em>Type Expr</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.TypeExprImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getTypeExpr()
   * @generated
   */
  int TYPE_EXPR = 10;

  /**
   * The feature id for the '<em><b>Name</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TYPE_EXPR__NAME = 0;

  /**
   * The feature id for the '<em><b>Array</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TYPE_EXPR__ARRAY = 1;

  /**
   * The feature id for the '<em><b>Sizes</b></em>' attribute list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TYPE_EXPR__SIZES = 2;

  /**
   * The number of structural features of the '<em>Type Expr</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int TYPE_EXPR_FEATURE_COUNT = 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ConstructorDefImpl <em>Constructor Def</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ConstructorDefImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getConstructorDef()
   * @generated
   */
  int CONSTRUCTOR_DEF = 11;

  /**
   * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CONSTRUCTOR_DEF__PARAMETERS = CLASS_SLOTS_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Body</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CONSTRUCTOR_DEF__BODY = CLASS_SLOTS_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Constructor Def</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CONSTRUCTOR_DEF_FEATURE_COUNT = CLASS_SLOTS_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OnDestroyDefImpl <em>On Destroy Def</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OnDestroyDefImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOnDestroyDef()
   * @generated
   */
  int ON_DESTROY_DEF = 12;

  /**
   * The feature id for the '<em><b>Body</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ON_DESTROY_DEF__BODY = CLASS_SLOTS_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>On Destroy Def</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ON_DESTROY_DEF_FEATURE_COUNT = CLASS_SLOTS_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.StatementsImpl <em>Statements</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.StatementsImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStatements()
   * @generated
   */
  int STATEMENTS = 13;

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
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.StatementImpl <em>Statement</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.StatementImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStatement()
   * @generated
   */
  int STATEMENT = 14;

  /**
   * The number of structural features of the '<em>Statement</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STATEMENT_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.StmtChangeRefCountImpl <em>Stmt Change Ref Count</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.StmtChangeRefCountImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtChangeRefCount()
   * @generated
   */
  int STMT_CHANGE_REF_COUNT = 15;

  /**
   * The feature id for the '<em><b>Increase</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_CHANGE_REF_COUNT__INCREASE = STATEMENT_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Decrease</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_CHANGE_REF_COUNT__DECREASE = STATEMENT_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Obj</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_CHANGE_REF_COUNT__OBJ = STATEMENT_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Stmt Change Ref Count</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_CHANGE_REF_COUNT_FEATURE_COUNT = STATEMENT_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.StmtDestroyImpl <em>Stmt Destroy</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.StmtDestroyImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtDestroy()
   * @generated
   */
  int STMT_DESTROY = 16;

  /**
   * The feature id for the '<em><b>Obj</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_DESTROY__OBJ = STATEMENT_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Stmt Destroy</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_DESTROY_FEATURE_COUNT = STATEMENT_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.StmtReturnImpl <em>Stmt Return</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.StmtReturnImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtReturn()
   * @generated
   */
  int STMT_RETURN = 17;

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
  int STMT_IF = 18;

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
  int STMT_WHILE = 19;

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
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.StmtSetOrCallOrVarDefImpl <em>Stmt Set Or Call Or Var Def</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.StmtSetOrCallOrVarDefImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtSetOrCallOrVarDef()
   * @generated
   */
  int STMT_SET_OR_CALL_OR_VAR_DEF = 20;

  /**
   * The number of structural features of the '<em>Stmt Set Or Call Or Var Def</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_SET_OR_CALL_OR_VAR_DEF_FEATURE_COUNT = STATEMENT_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpAssignmentImpl <em>Op Assignment</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpAssignmentImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpAssignment()
   * @generated
   */
  int OP_ASSIGNMENT = 21;

  /**
   * The number of structural features of the '<em>Op Assignment</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_ASSIGNMENT_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprImpl <em>Expr</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExpr()
   * @generated
   */
  int EXPR = 22;

  /**
   * The number of structural features of the '<em>Expr</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpEqualityImpl <em>Op Equality</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpEqualityImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpEquality()
   * @generated
   */
  int OP_EQUALITY = 23;

  /**
   * The number of structural features of the '<em>Op Equality</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_EQUALITY_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpComparisonImpl <em>Op Comparison</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpComparisonImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpComparison()
   * @generated
   */
  int OP_COMPARISON = 24;

  /**
   * The number of structural features of the '<em>Op Comparison</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_COMPARISON_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpAdditiveImpl <em>Op Additive</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpAdditiveImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpAdditive()
   * @generated
   */
  int OP_ADDITIVE = 25;

  /**
   * The number of structural features of the '<em>Op Additive</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_ADDITIVE_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpMultiplicativeImpl <em>Op Multiplicative</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpMultiplicativeImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpMultiplicative()
   * @generated
   */
  int OP_MULTIPLICATIVE = 26;

  /**
   * The number of structural features of the '<em>Op Multiplicative</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_MULTIPLICATIVE_FEATURE_COUNT = 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprMemberRightImpl <em>Expr Member Right</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprMemberRightImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprMemberRight()
   * @generated
   */
  int EXPR_MEMBER_RIGHT = 27;

  /**
   * The feature id for the '<em><b>Name Val</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_MEMBER_RIGHT__NAME_VAL = 0;

  /**
   * The feature id for the '<em><b>Params</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_MEMBER_RIGHT__PARAMS = 1;

  /**
   * The number of structural features of the '<em>Expr Member Right</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_MEMBER_RIGHT_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.NativeFuncImpl <em>Native Func</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.NativeFuncImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getNativeFunc()
   * @generated
   */
  int NATIVE_FUNC = 28;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_FUNC__NAME = FUNC_DEF__NAME;

  /**
   * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_FUNC__PARAMETERS = FUNC_DEF__PARAMETERS;

  /**
   * The feature id for the '<em><b>Type</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_FUNC__TYPE = FUNC_DEF__TYPE;

  /**
   * The feature id for the '<em><b>Body</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_FUNC__BODY = FUNC_DEF__BODY;

  /**
   * The number of structural features of the '<em>Native Func</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_FUNC_FEATURE_COUNT = FUNC_DEF_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.NativeTypeImpl <em>Native Type</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.NativeTypeImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getNativeType()
   * @generated
   */
  int NATIVE_TYPE = 29;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_TYPE__NAME = TYPE_DEF__NAME;

  /**
   * The feature id for the '<em><b>Super Name</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_TYPE__SUPER_NAME = TYPE_DEF_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Native Type</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int NATIVE_TYPE_FEATURE_COUNT = TYPE_DEF_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ClassDefImpl <em>Class Def</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ClassDefImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getClassDef()
   * @generated
   */
  int CLASS_DEF = 30;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CLASS_DEF__NAME = TYPE_DEF__NAME;

  /**
   * The feature id for the '<em><b>Unmanaged</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CLASS_DEF__UNMANAGED = TYPE_DEF_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Members</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CLASS_DEF__MEMBERS = TYPE_DEF_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Class Def</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int CLASS_DEF_FEATURE_COUNT = TYPE_DEF_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ParameterDefImpl <em>Parameter Def</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ParameterDefImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getParameterDef()
   * @generated
   */
  int PARAMETER_DEF = 31;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PARAMETER_DEF__NAME = VAR_DEF__NAME;

  /**
   * The feature id for the '<em><b>Constant</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PARAMETER_DEF__CONSTANT = VAR_DEF__CONSTANT;

  /**
   * The feature id for the '<em><b>Type</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PARAMETER_DEF__TYPE = VAR_DEF__TYPE;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PARAMETER_DEF__E = VAR_DEF__E;

  /**
   * The number of structural features of the '<em>Parameter Def</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int PARAMETER_DEF_FEATURE_COUNT = VAR_DEF_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.StmtCallImpl <em>Stmt Call</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.StmtCallImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtCall()
   * @generated
   */
  int STMT_CALL = 32;

  /**
   * The feature id for the '<em><b>E</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_CALL__E = STMT_SET_OR_CALL_OR_VAR_DEF_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Stmt Call</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_CALL_FEATURE_COUNT = STMT_SET_OR_CALL_OR_VAR_DEF_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.StmtSetImpl <em>Stmt Set</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.StmtSetImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtSet()
   * @generated
   */
  int STMT_SET = 33;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_SET__LEFT = STMT_SET_OR_CALL_OR_VAR_DEF_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Op Assignment</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_SET__OP_ASSIGNMENT = STMT_SET_OR_CALL_OR_VAR_DEF_FEATURE_COUNT + 1;

  /**
   * The feature id for the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_SET__RIGHT = STMT_SET_OR_CALL_OR_VAR_DEF_FEATURE_COUNT + 2;

  /**
   * The number of structural features of the '<em>Stmt Set</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int STMT_SET_FEATURE_COUNT = STMT_SET_OR_CALL_OR_VAR_DEF_FEATURE_COUNT + 3;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpAssignImpl <em>Op Assign</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpAssignImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpAssign()
   * @generated
   */
  int OP_ASSIGN = 34;

  /**
   * The number of structural features of the '<em>Op Assign</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_ASSIGN_FEATURE_COUNT = OP_ASSIGNMENT_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpPlusAssignImpl <em>Op Plus Assign</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpPlusAssignImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpPlusAssign()
   * @generated
   */
  int OP_PLUS_ASSIGN = 35;

  /**
   * The number of structural features of the '<em>Op Plus Assign</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_PLUS_ASSIGN_FEATURE_COUNT = OP_ASSIGNMENT_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpMinusAssignImpl <em>Op Minus Assign</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpMinusAssignImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpMinusAssign()
   * @generated
   */
  int OP_MINUS_ASSIGN = 36;

  /**
   * The number of structural features of the '<em>Op Minus Assign</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_MINUS_ASSIGN_FEATURE_COUNT = OP_ASSIGNMENT_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprOrImpl <em>Expr Or</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprOrImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprOr()
   * @generated
   */
  int EXPR_OR = 37;

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
  int EXPR_AND = 38;

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
  int EXPR_EQUALITY = 39;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_EQUALITY__LEFT = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Op</b></em>' containment reference.
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
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpEqualsImpl <em>Op Equals</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpEqualsImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpEquals()
   * @generated
   */
  int OP_EQUALS = 40;

  /**
   * The number of structural features of the '<em>Op Equals</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_EQUALS_FEATURE_COUNT = OP_EQUALITY_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpUnequalsImpl <em>Op Unequals</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpUnequalsImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpUnequals()
   * @generated
   */
  int OP_UNEQUALS = 41;

  /**
   * The number of structural features of the '<em>Op Unequals</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_UNEQUALS_FEATURE_COUNT = OP_EQUALITY_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprComparisonImpl <em>Expr Comparison</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprComparisonImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprComparison()
   * @generated
   */
  int EXPR_COMPARISON = 42;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_COMPARISON__LEFT = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Op</b></em>' containment reference.
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
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpLessEqImpl <em>Op Less Eq</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpLessEqImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpLessEq()
   * @generated
   */
  int OP_LESS_EQ = 43;

  /**
   * The number of structural features of the '<em>Op Less Eq</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_LESS_EQ_FEATURE_COUNT = OP_COMPARISON_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpLessImpl <em>Op Less</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpLessImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpLess()
   * @generated
   */
  int OP_LESS = 44;

  /**
   * The number of structural features of the '<em>Op Less</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_LESS_FEATURE_COUNT = OP_COMPARISON_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpGreaterEqImpl <em>Op Greater Eq</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpGreaterEqImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpGreaterEq()
   * @generated
   */
  int OP_GREATER_EQ = 45;

  /**
   * The number of structural features of the '<em>Op Greater Eq</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_GREATER_EQ_FEATURE_COUNT = OP_COMPARISON_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpGreaterImpl <em>Op Greater</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpGreaterImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpGreater()
   * @generated
   */
  int OP_GREATER = 46;

  /**
   * The number of structural features of the '<em>Op Greater</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_GREATER_FEATURE_COUNT = OP_COMPARISON_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprAdditiveImpl <em>Expr Additive</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprAdditiveImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprAdditive()
   * @generated
   */
  int EXPR_ADDITIVE = 47;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_ADDITIVE__LEFT = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Op</b></em>' containment reference.
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
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpPlusImpl <em>Op Plus</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpPlusImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpPlus()
   * @generated
   */
  int OP_PLUS = 48;

  /**
   * The number of structural features of the '<em>Op Plus</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_PLUS_FEATURE_COUNT = OP_ADDITIVE_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpMinusImpl <em>Op Minus</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpMinusImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpMinus()
   * @generated
   */
  int OP_MINUS = 49;

  /**
   * The number of structural features of the '<em>Op Minus</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_MINUS_FEATURE_COUNT = OP_ADDITIVE_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprMultImpl <em>Expr Mult</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprMultImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprMult()
   * @generated
   */
  int EXPR_MULT = 50;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_MULT__LEFT = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Op</b></em>' containment reference.
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
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpMultImpl <em>Op Mult</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpMultImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpMult()
   * @generated
   */
  int OP_MULT = 51;

  /**
   * The number of structural features of the '<em>Op Mult</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_MULT_FEATURE_COUNT = OP_MULTIPLICATIVE_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpDivRealImpl <em>Op Div Real</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpDivRealImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpDivReal()
   * @generated
   */
  int OP_DIV_REAL = 52;

  /**
   * The number of structural features of the '<em>Op Div Real</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_DIV_REAL_FEATURE_COUNT = OP_MULTIPLICATIVE_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpModRealImpl <em>Op Mod Real</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpModRealImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpModReal()
   * @generated
   */
  int OP_MOD_REAL = 53;

  /**
   * The number of structural features of the '<em>Op Mod Real</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_MOD_REAL_FEATURE_COUNT = OP_MULTIPLICATIVE_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpModIntImpl <em>Op Mod Int</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpModIntImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpModInt()
   * @generated
   */
  int OP_MOD_INT = 54;

  /**
   * The number of structural features of the '<em>Op Mod Int</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_MOD_INT_FEATURE_COUNT = OP_MULTIPLICATIVE_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.OpDivIntImpl <em>Op Div Int</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.OpDivIntImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpDivInt()
   * @generated
   */
  int OP_DIV_INT = 55;

  /**
   * The number of structural features of the '<em>Op Div Int</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int OP_DIV_INT_FEATURE_COUNT = OP_MULTIPLICATIVE_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprSignImpl <em>Expr Sign</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprSignImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprSign()
   * @generated
   */
  int EXPR_SIGN = 56;

  /**
   * The feature id for the '<em><b>Op</b></em>' containment reference.
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
  int EXPR_NOT = 57;

  /**
   * The feature id for the '<em><b>Right</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_NOT__RIGHT = EXPR_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Expr Not</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_NOT_FEATURE_COUNT = EXPR_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprMemberImpl <em>Expr Member</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprMemberImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprMember()
   * @generated
   */
  int EXPR_MEMBER = 58;

  /**
   * The feature id for the '<em><b>Left</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_MEMBER__LEFT = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Message</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_MEMBER__MESSAGE = EXPR_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Expr Member</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_MEMBER_FEATURE_COUNT = EXPR_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprIntValImpl <em>Expr Int Val</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprIntValImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprIntVal()
   * @generated
   */
  int EXPR_INT_VAL = 59;

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
  int EXPR_NUM_VAL = 60;

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
  int EXPR_STRVAL = 61;

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
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprBoolValImpl <em>Expr Bool Val</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprBoolValImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprBoolVal()
   * @generated
   */
  int EXPR_BOOL_VAL = 62;

  /**
   * The feature id for the '<em><b>Bool Val</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_BOOL_VAL__BOOL_VAL = EXPR_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Expr Bool Val</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_BOOL_VAL_FEATURE_COUNT = EXPR_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprFuncRefImpl <em>Expr Func Ref</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprFuncRefImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprFuncRef()
   * @generated
   */
  int EXPR_FUNC_REF = 63;

  /**
   * The feature id for the '<em><b>Name Val</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_FUNC_REF__NAME_VAL = EXPR_FEATURE_COUNT + 0;

  /**
   * The number of structural features of the '<em>Expr Func Ref</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_FUNC_REF_FEATURE_COUNT = EXPR_FEATURE_COUNT + 1;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprIdentifierImpl <em>Expr Identifier</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprIdentifierImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprIdentifier()
   * @generated
   */
  int EXPR_IDENTIFIER = 64;

  /**
   * The feature id for the '<em><b>Name Val</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_IDENTIFIER__NAME_VAL = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Array Indizes</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_IDENTIFIER__ARRAY_INDIZES = EXPR_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Expr Identifier</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_IDENTIFIER_FEATURE_COUNT = EXPR_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprNewObjectImpl <em>Expr New Object</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprNewObjectImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprNewObject()
   * @generated
   */
  int EXPR_NEW_OBJECT = 65;

  /**
   * The feature id for the '<em><b>Class Def</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_NEW_OBJECT__CLASS_DEF = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Params</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_NEW_OBJECT__PARAMS = EXPR_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Expr New Object</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_NEW_OBJECT_FEATURE_COUNT = EXPR_FEATURE_COUNT + 2;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprThisImpl <em>Expr This</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprThisImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprThis()
   * @generated
   */
  int EXPR_THIS = 66;

  /**
   * The number of structural features of the '<em>Expr This</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_THIS_FEATURE_COUNT = EXPR_FEATURE_COUNT + 0;

  /**
   * The meta object id for the '{@link de.peeeq.pscript.pscript.impl.ExprFunctioncallImpl <em>Expr Functioncall</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see de.peeeq.pscript.pscript.impl.ExprFunctioncallImpl
   * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprFunctioncall()
   * @generated
   */
  int EXPR_FUNCTIONCALL = 67;

  /**
   * The feature id for the '<em><b>Name Val</b></em>' reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_FUNCTIONCALL__NAME_VAL = EXPR_FEATURE_COUNT + 0;

  /**
   * The feature id for the '<em><b>Params</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_FUNCTIONCALL__PARAMS = EXPR_FEATURE_COUNT + 1;

  /**
   * The number of structural features of the '<em>Expr Functioncall</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int EXPR_FUNCTIONCALL_FEATURE_COUNT = EXPR_FEATURE_COUNT + 2;


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
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.Entity <em>Entity</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Entity</em>'.
   * @see de.peeeq.pscript.pscript.Entity
   * @generated
   */
  EClass getEntity();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.Entity#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see de.peeeq.pscript.pscript.Entity#getName()
   * @see #getEntity()
   * @generated
   */
  EAttribute getEntity_Name();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.InitBlock <em>Init Block</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Init Block</em>'.
   * @see de.peeeq.pscript.pscript.InitBlock
   * @generated
   */
  EClass getInitBlock();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.InitBlock#getBody <em>Body</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Body</em>'.
   * @see de.peeeq.pscript.pscript.InitBlock#getBody()
   * @see #getInitBlock()
   * @generated
   */
  EReference getInitBlock_Body();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.TypeDef <em>Type Def</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Type Def</em>'.
   * @see de.peeeq.pscript.pscript.TypeDef
   * @generated
   */
  EClass getTypeDef();

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
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ClassSlots <em>Class Slots</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Class Slots</em>'.
   * @see de.peeeq.pscript.pscript.ClassSlots
   * @generated
   */
  EClass getClassSlots();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ClassMember <em>Class Member</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Class Member</em>'.
   * @see de.peeeq.pscript.pscript.ClassMember
   * @generated
   */
  EClass getClassMember();

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
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.TypeExpr <em>Type Expr</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Type Expr</em>'.
   * @see de.peeeq.pscript.pscript.TypeExpr
   * @generated
   */
  EClass getTypeExpr();

  /**
   * Returns the meta object for the reference '{@link de.peeeq.pscript.pscript.TypeExpr#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the reference '<em>Name</em>'.
   * @see de.peeeq.pscript.pscript.TypeExpr#getName()
   * @see #getTypeExpr()
   * @generated
   */
  EReference getTypeExpr_Name();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.TypeExpr#isArray <em>Array</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Array</em>'.
   * @see de.peeeq.pscript.pscript.TypeExpr#isArray()
   * @see #getTypeExpr()
   * @generated
   */
  EAttribute getTypeExpr_Array();

  /**
   * Returns the meta object for the attribute list '{@link de.peeeq.pscript.pscript.TypeExpr#getSizes <em>Sizes</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute list '<em>Sizes</em>'.
   * @see de.peeeq.pscript.pscript.TypeExpr#getSizes()
   * @see #getTypeExpr()
   * @generated
   */
  EAttribute getTypeExpr_Sizes();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ConstructorDef <em>Constructor Def</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Constructor Def</em>'.
   * @see de.peeeq.pscript.pscript.ConstructorDef
   * @generated
   */
  EClass getConstructorDef();

  /**
   * Returns the meta object for the containment reference list '{@link de.peeeq.pscript.pscript.ConstructorDef#getParameters <em>Parameters</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Parameters</em>'.
   * @see de.peeeq.pscript.pscript.ConstructorDef#getParameters()
   * @see #getConstructorDef()
   * @generated
   */
  EReference getConstructorDef_Parameters();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ConstructorDef#getBody <em>Body</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Body</em>'.
   * @see de.peeeq.pscript.pscript.ConstructorDef#getBody()
   * @see #getConstructorDef()
   * @generated
   */
  EReference getConstructorDef_Body();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OnDestroyDef <em>On Destroy Def</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>On Destroy Def</em>'.
   * @see de.peeeq.pscript.pscript.OnDestroyDef
   * @generated
   */
  EClass getOnDestroyDef();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.OnDestroyDef#getBody <em>Body</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Body</em>'.
   * @see de.peeeq.pscript.pscript.OnDestroyDef#getBody()
   * @see #getOnDestroyDef()
   * @generated
   */
  EReference getOnDestroyDef_Body();

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
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.StmtChangeRefCount <em>Stmt Change Ref Count</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Stmt Change Ref Count</em>'.
   * @see de.peeeq.pscript.pscript.StmtChangeRefCount
   * @generated
   */
  EClass getStmtChangeRefCount();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.StmtChangeRefCount#isIncrease <em>Increase</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Increase</em>'.
   * @see de.peeeq.pscript.pscript.StmtChangeRefCount#isIncrease()
   * @see #getStmtChangeRefCount()
   * @generated
   */
  EAttribute getStmtChangeRefCount_Increase();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.StmtChangeRefCount#isDecrease <em>Decrease</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Decrease</em>'.
   * @see de.peeeq.pscript.pscript.StmtChangeRefCount#isDecrease()
   * @see #getStmtChangeRefCount()
   * @generated
   */
  EAttribute getStmtChangeRefCount_Decrease();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.StmtChangeRefCount#getObj <em>Obj</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Obj</em>'.
   * @see de.peeeq.pscript.pscript.StmtChangeRefCount#getObj()
   * @see #getStmtChangeRefCount()
   * @generated
   */
  EReference getStmtChangeRefCount_Obj();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.StmtDestroy <em>Stmt Destroy</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Stmt Destroy</em>'.
   * @see de.peeeq.pscript.pscript.StmtDestroy
   * @generated
   */
  EClass getStmtDestroy();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.StmtDestroy#getObj <em>Obj</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Obj</em>'.
   * @see de.peeeq.pscript.pscript.StmtDestroy#getObj()
   * @see #getStmtDestroy()
   * @generated
   */
  EReference getStmtDestroy_Obj();

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
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.StmtSetOrCallOrVarDef <em>Stmt Set Or Call Or Var Def</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Stmt Set Or Call Or Var Def</em>'.
   * @see de.peeeq.pscript.pscript.StmtSetOrCallOrVarDef
   * @generated
   */
  EClass getStmtSetOrCallOrVarDef();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpAssignment <em>Op Assignment</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Assignment</em>'.
   * @see de.peeeq.pscript.pscript.OpAssignment
   * @generated
   */
  EClass getOpAssignment();

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
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpEquality <em>Op Equality</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Equality</em>'.
   * @see de.peeeq.pscript.pscript.OpEquality
   * @generated
   */
  EClass getOpEquality();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpComparison <em>Op Comparison</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Comparison</em>'.
   * @see de.peeeq.pscript.pscript.OpComparison
   * @generated
   */
  EClass getOpComparison();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpAdditive <em>Op Additive</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Additive</em>'.
   * @see de.peeeq.pscript.pscript.OpAdditive
   * @generated
   */
  EClass getOpAdditive();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpMultiplicative <em>Op Multiplicative</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Multiplicative</em>'.
   * @see de.peeeq.pscript.pscript.OpMultiplicative
   * @generated
   */
  EClass getOpMultiplicative();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprMemberRight <em>Expr Member Right</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Member Right</em>'.
   * @see de.peeeq.pscript.pscript.ExprMemberRight
   * @generated
   */
  EClass getExprMemberRight();

  /**
   * Returns the meta object for the reference '{@link de.peeeq.pscript.pscript.ExprMemberRight#getNameVal <em>Name Val</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the reference '<em>Name Val</em>'.
   * @see de.peeeq.pscript.pscript.ExprMemberRight#getNameVal()
   * @see #getExprMemberRight()
   * @generated
   */
  EReference getExprMemberRight_NameVal();

  /**
   * Returns the meta object for the containment reference list '{@link de.peeeq.pscript.pscript.ExprMemberRight#getParams <em>Params</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Params</em>'.
   * @see de.peeeq.pscript.pscript.ExprMemberRight#getParams()
   * @see #getExprMemberRight()
   * @generated
   */
  EReference getExprMemberRight_Params();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.NativeFunc <em>Native Func</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Native Func</em>'.
   * @see de.peeeq.pscript.pscript.NativeFunc
   * @generated
   */
  EClass getNativeFunc();

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
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.NativeType#getSuperName <em>Super Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Super Name</em>'.
   * @see de.peeeq.pscript.pscript.NativeType#getSuperName()
   * @see #getNativeType()
   * @generated
   */
  EReference getNativeType_SuperName();

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
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ClassDef#isUnmanaged <em>Unmanaged</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Unmanaged</em>'.
   * @see de.peeeq.pscript.pscript.ClassDef#isUnmanaged()
   * @see #getClassDef()
   * @generated
   */
  EAttribute getClassDef_Unmanaged();

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
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ParameterDef <em>Parameter Def</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Parameter Def</em>'.
   * @see de.peeeq.pscript.pscript.ParameterDef
   * @generated
   */
  EClass getParameterDef();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.StmtCall <em>Stmt Call</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Stmt Call</em>'.
   * @see de.peeeq.pscript.pscript.StmtCall
   * @generated
   */
  EClass getStmtCall();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.StmtCall#getE <em>E</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>E</em>'.
   * @see de.peeeq.pscript.pscript.StmtCall#getE()
   * @see #getStmtCall()
   * @generated
   */
  EReference getStmtCall_E();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.StmtSet <em>Stmt Set</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Stmt Set</em>'.
   * @see de.peeeq.pscript.pscript.StmtSet
   * @generated
   */
  EClass getStmtSet();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.StmtSet#getLeft <em>Left</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Left</em>'.
   * @see de.peeeq.pscript.pscript.StmtSet#getLeft()
   * @see #getStmtSet()
   * @generated
   */
  EReference getStmtSet_Left();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.StmtSet#getOpAssignment <em>Op Assignment</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Op Assignment</em>'.
   * @see de.peeeq.pscript.pscript.StmtSet#getOpAssignment()
   * @see #getStmtSet()
   * @generated
   */
  EReference getStmtSet_OpAssignment();

  /**
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.StmtSet#getRight <em>Right</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Right</em>'.
   * @see de.peeeq.pscript.pscript.StmtSet#getRight()
   * @see #getStmtSet()
   * @generated
   */
  EReference getStmtSet_Right();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpAssign <em>Op Assign</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Assign</em>'.
   * @see de.peeeq.pscript.pscript.OpAssign
   * @generated
   */
  EClass getOpAssign();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpPlusAssign <em>Op Plus Assign</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Plus Assign</em>'.
   * @see de.peeeq.pscript.pscript.OpPlusAssign
   * @generated
   */
  EClass getOpPlusAssign();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpMinusAssign <em>Op Minus Assign</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Minus Assign</em>'.
   * @see de.peeeq.pscript.pscript.OpMinusAssign
   * @generated
   */
  EClass getOpMinusAssign();

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
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprEquality#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprEquality#getOp()
   * @see #getExprEquality()
   * @generated
   */
  EReference getExprEquality_Op();

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
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpEquals <em>Op Equals</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Equals</em>'.
   * @see de.peeeq.pscript.pscript.OpEquals
   * @generated
   */
  EClass getOpEquals();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpUnequals <em>Op Unequals</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Unequals</em>'.
   * @see de.peeeq.pscript.pscript.OpUnequals
   * @generated
   */
  EClass getOpUnequals();

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
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprComparison#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprComparison#getOp()
   * @see #getExprComparison()
   * @generated
   */
  EReference getExprComparison_Op();

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
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpLessEq <em>Op Less Eq</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Less Eq</em>'.
   * @see de.peeeq.pscript.pscript.OpLessEq
   * @generated
   */
  EClass getOpLessEq();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpLess <em>Op Less</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Less</em>'.
   * @see de.peeeq.pscript.pscript.OpLess
   * @generated
   */
  EClass getOpLess();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpGreaterEq <em>Op Greater Eq</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Greater Eq</em>'.
   * @see de.peeeq.pscript.pscript.OpGreaterEq
   * @generated
   */
  EClass getOpGreaterEq();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpGreater <em>Op Greater</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Greater</em>'.
   * @see de.peeeq.pscript.pscript.OpGreater
   * @generated
   */
  EClass getOpGreater();

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
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprAdditive#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprAdditive#getOp()
   * @see #getExprAdditive()
   * @generated
   */
  EReference getExprAdditive_Op();

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
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpPlus <em>Op Plus</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Plus</em>'.
   * @see de.peeeq.pscript.pscript.OpPlus
   * @generated
   */
  EClass getOpPlus();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpMinus <em>Op Minus</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Minus</em>'.
   * @see de.peeeq.pscript.pscript.OpMinus
   * @generated
   */
  EClass getOpMinus();

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
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprMult#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprMult#getOp()
   * @see #getExprMult()
   * @generated
   */
  EReference getExprMult_Op();

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
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpMult <em>Op Mult</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Mult</em>'.
   * @see de.peeeq.pscript.pscript.OpMult
   * @generated
   */
  EClass getOpMult();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpDivReal <em>Op Div Real</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Div Real</em>'.
   * @see de.peeeq.pscript.pscript.OpDivReal
   * @generated
   */
  EClass getOpDivReal();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpModReal <em>Op Mod Real</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Mod Real</em>'.
   * @see de.peeeq.pscript.pscript.OpModReal
   * @generated
   */
  EClass getOpModReal();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpModInt <em>Op Mod Int</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Mod Int</em>'.
   * @see de.peeeq.pscript.pscript.OpModInt
   * @generated
   */
  EClass getOpModInt();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.OpDivInt <em>Op Div Int</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Op Div Int</em>'.
   * @see de.peeeq.pscript.pscript.OpDivInt
   * @generated
   */
  EClass getOpDivInt();

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
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprSign#getOp <em>Op</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Op</em>'.
   * @see de.peeeq.pscript.pscript.ExprSign#getOp()
   * @see #getExprSign()
   * @generated
   */
  EReference getExprSign_Op();

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
   * Returns the meta object for the containment reference '{@link de.peeeq.pscript.pscript.ExprMember#getMessage <em>Message</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Message</em>'.
   * @see de.peeeq.pscript.pscript.ExprMember#getMessage()
   * @see #getExprMember()
   * @generated
   */
  EReference getExprMember_Message();

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
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprBoolVal <em>Expr Bool Val</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Bool Val</em>'.
   * @see de.peeeq.pscript.pscript.ExprBoolVal
   * @generated
   */
  EClass getExprBoolVal();

  /**
   * Returns the meta object for the attribute '{@link de.peeeq.pscript.pscript.ExprBoolVal#getBoolVal <em>Bool Val</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Bool Val</em>'.
   * @see de.peeeq.pscript.pscript.ExprBoolVal#getBoolVal()
   * @see #getExprBoolVal()
   * @generated
   */
  EAttribute getExprBoolVal_BoolVal();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprFuncRef <em>Expr Func Ref</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr Func Ref</em>'.
   * @see de.peeeq.pscript.pscript.ExprFuncRef
   * @generated
   */
  EClass getExprFuncRef();

  /**
   * Returns the meta object for the reference '{@link de.peeeq.pscript.pscript.ExprFuncRef#getNameVal <em>Name Val</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the reference '<em>Name Val</em>'.
   * @see de.peeeq.pscript.pscript.ExprFuncRef#getNameVal()
   * @see #getExprFuncRef()
   * @generated
   */
  EReference getExprFuncRef_NameVal();

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
   * Returns the meta object for the reference '{@link de.peeeq.pscript.pscript.ExprIdentifier#getNameVal <em>Name Val</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the reference '<em>Name Val</em>'.
   * @see de.peeeq.pscript.pscript.ExprIdentifier#getNameVal()
   * @see #getExprIdentifier()
   * @generated
   */
  EReference getExprIdentifier_NameVal();

  /**
   * Returns the meta object for the containment reference list '{@link de.peeeq.pscript.pscript.ExprIdentifier#getArrayIndizes <em>Array Indizes</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Array Indizes</em>'.
   * @see de.peeeq.pscript.pscript.ExprIdentifier#getArrayIndizes()
   * @see #getExprIdentifier()
   * @generated
   */
  EReference getExprIdentifier_ArrayIndizes();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprNewObject <em>Expr New Object</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr New Object</em>'.
   * @see de.peeeq.pscript.pscript.ExprNewObject
   * @generated
   */
  EClass getExprNewObject();

  /**
   * Returns the meta object for the reference '{@link de.peeeq.pscript.pscript.ExprNewObject#getClassDef <em>Class Def</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the reference '<em>Class Def</em>'.
   * @see de.peeeq.pscript.pscript.ExprNewObject#getClassDef()
   * @see #getExprNewObject()
   * @generated
   */
  EReference getExprNewObject_ClassDef();

  /**
   * Returns the meta object for the containment reference list '{@link de.peeeq.pscript.pscript.ExprNewObject#getParams <em>Params</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Params</em>'.
   * @see de.peeeq.pscript.pscript.ExprNewObject#getParams()
   * @see #getExprNewObject()
   * @generated
   */
  EReference getExprNewObject_Params();

  /**
   * Returns the meta object for class '{@link de.peeeq.pscript.pscript.ExprThis <em>Expr This</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Expr This</em>'.
   * @see de.peeeq.pscript.pscript.ExprThis
   * @generated
   */
  EClass getExprThis();

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
   * Returns the meta object for the reference '{@link de.peeeq.pscript.pscript.ExprFunctioncall#getNameVal <em>Name Val</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the reference '<em>Name Val</em>'.
   * @see de.peeeq.pscript.pscript.ExprFunctioncall#getNameVal()
   * @see #getExprFunctioncall()
   * @generated
   */
  EReference getExprFunctioncall_NameVal();

  /**
   * Returns the meta object for the containment reference list '{@link de.peeeq.pscript.pscript.ExprFunctioncall#getParams <em>Params</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Params</em>'.
   * @see de.peeeq.pscript.pscript.ExprFunctioncall#getParams()
   * @see #getExprFunctioncall()
   * @generated
   */
  EReference getExprFunctioncall_Params();

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
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.EntityImpl <em>Entity</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.EntityImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getEntity()
     * @generated
     */
    EClass ENTITY = eINSTANCE.getEntity();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute ENTITY__NAME = eINSTANCE.getEntity_Name();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.InitBlockImpl <em>Init Block</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.InitBlockImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getInitBlock()
     * @generated
     */
    EClass INIT_BLOCK = eINSTANCE.getInitBlock();

    /**
     * The meta object literal for the '<em><b>Body</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference INIT_BLOCK__BODY = eINSTANCE.getInitBlock_Body();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.TypeDefImpl <em>Type Def</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.TypeDefImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getTypeDef()
     * @generated
     */
    EClass TYPE_DEF = eINSTANCE.getTypeDef();

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
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ClassSlotsImpl <em>Class Slots</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ClassSlotsImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getClassSlots()
     * @generated
     */
    EClass CLASS_SLOTS = eINSTANCE.getClassSlots();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ClassMemberImpl <em>Class Member</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ClassMemberImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getClassMember()
     * @generated
     */
    EClass CLASS_MEMBER = eINSTANCE.getClassMember();

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
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.TypeExprImpl <em>Type Expr</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.TypeExprImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getTypeExpr()
     * @generated
     */
    EClass TYPE_EXPR = eINSTANCE.getTypeExpr();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference TYPE_EXPR__NAME = eINSTANCE.getTypeExpr_Name();

    /**
     * The meta object literal for the '<em><b>Array</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute TYPE_EXPR__ARRAY = eINSTANCE.getTypeExpr_Array();

    /**
     * The meta object literal for the '<em><b>Sizes</b></em>' attribute list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute TYPE_EXPR__SIZES = eINSTANCE.getTypeExpr_Sizes();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ConstructorDefImpl <em>Constructor Def</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ConstructorDefImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getConstructorDef()
     * @generated
     */
    EClass CONSTRUCTOR_DEF = eINSTANCE.getConstructorDef();

    /**
     * The meta object literal for the '<em><b>Parameters</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference CONSTRUCTOR_DEF__PARAMETERS = eINSTANCE.getConstructorDef_Parameters();

    /**
     * The meta object literal for the '<em><b>Body</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference CONSTRUCTOR_DEF__BODY = eINSTANCE.getConstructorDef_Body();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OnDestroyDefImpl <em>On Destroy Def</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OnDestroyDefImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOnDestroyDef()
     * @generated
     */
    EClass ON_DESTROY_DEF = eINSTANCE.getOnDestroyDef();

    /**
     * The meta object literal for the '<em><b>Body</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference ON_DESTROY_DEF__BODY = eINSTANCE.getOnDestroyDef_Body();

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
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.StmtChangeRefCountImpl <em>Stmt Change Ref Count</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.StmtChangeRefCountImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtChangeRefCount()
     * @generated
     */
    EClass STMT_CHANGE_REF_COUNT = eINSTANCE.getStmtChangeRefCount();

    /**
     * The meta object literal for the '<em><b>Increase</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute STMT_CHANGE_REF_COUNT__INCREASE = eINSTANCE.getStmtChangeRefCount_Increase();

    /**
     * The meta object literal for the '<em><b>Decrease</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute STMT_CHANGE_REF_COUNT__DECREASE = eINSTANCE.getStmtChangeRefCount_Decrease();

    /**
     * The meta object literal for the '<em><b>Obj</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference STMT_CHANGE_REF_COUNT__OBJ = eINSTANCE.getStmtChangeRefCount_Obj();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.StmtDestroyImpl <em>Stmt Destroy</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.StmtDestroyImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtDestroy()
     * @generated
     */
    EClass STMT_DESTROY = eINSTANCE.getStmtDestroy();

    /**
     * The meta object literal for the '<em><b>Obj</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference STMT_DESTROY__OBJ = eINSTANCE.getStmtDestroy_Obj();

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
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.StmtSetOrCallOrVarDefImpl <em>Stmt Set Or Call Or Var Def</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.StmtSetOrCallOrVarDefImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtSetOrCallOrVarDef()
     * @generated
     */
    EClass STMT_SET_OR_CALL_OR_VAR_DEF = eINSTANCE.getStmtSetOrCallOrVarDef();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpAssignmentImpl <em>Op Assignment</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpAssignmentImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpAssignment()
     * @generated
     */
    EClass OP_ASSIGNMENT = eINSTANCE.getOpAssignment();

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
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpEqualityImpl <em>Op Equality</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpEqualityImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpEquality()
     * @generated
     */
    EClass OP_EQUALITY = eINSTANCE.getOpEquality();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpComparisonImpl <em>Op Comparison</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpComparisonImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpComparison()
     * @generated
     */
    EClass OP_COMPARISON = eINSTANCE.getOpComparison();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpAdditiveImpl <em>Op Additive</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpAdditiveImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpAdditive()
     * @generated
     */
    EClass OP_ADDITIVE = eINSTANCE.getOpAdditive();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpMultiplicativeImpl <em>Op Multiplicative</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpMultiplicativeImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpMultiplicative()
     * @generated
     */
    EClass OP_MULTIPLICATIVE = eINSTANCE.getOpMultiplicative();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprMemberRightImpl <em>Expr Member Right</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprMemberRightImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprMemberRight()
     * @generated
     */
    EClass EXPR_MEMBER_RIGHT = eINSTANCE.getExprMemberRight();

    /**
     * The meta object literal for the '<em><b>Name Val</b></em>' reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_MEMBER_RIGHT__NAME_VAL = eINSTANCE.getExprMemberRight_NameVal();

    /**
     * The meta object literal for the '<em><b>Params</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_MEMBER_RIGHT__PARAMS = eINSTANCE.getExprMemberRight_Params();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.NativeFuncImpl <em>Native Func</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.NativeFuncImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getNativeFunc()
     * @generated
     */
    EClass NATIVE_FUNC = eINSTANCE.getNativeFunc();

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
     * The meta object literal for the '<em><b>Super Name</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference NATIVE_TYPE__SUPER_NAME = eINSTANCE.getNativeType_SuperName();

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
     * The meta object literal for the '<em><b>Unmanaged</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute CLASS_DEF__UNMANAGED = eINSTANCE.getClassDef_Unmanaged();

    /**
     * The meta object literal for the '<em><b>Members</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference CLASS_DEF__MEMBERS = eINSTANCE.getClassDef_Members();

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
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.StmtCallImpl <em>Stmt Call</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.StmtCallImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtCall()
     * @generated
     */
    EClass STMT_CALL = eINSTANCE.getStmtCall();

    /**
     * The meta object literal for the '<em><b>E</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference STMT_CALL__E = eINSTANCE.getStmtCall_E();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.StmtSetImpl <em>Stmt Set</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.StmtSetImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getStmtSet()
     * @generated
     */
    EClass STMT_SET = eINSTANCE.getStmtSet();

    /**
     * The meta object literal for the '<em><b>Left</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference STMT_SET__LEFT = eINSTANCE.getStmtSet_Left();

    /**
     * The meta object literal for the '<em><b>Op Assignment</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference STMT_SET__OP_ASSIGNMENT = eINSTANCE.getStmtSet_OpAssignment();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference STMT_SET__RIGHT = eINSTANCE.getStmtSet_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpAssignImpl <em>Op Assign</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpAssignImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpAssign()
     * @generated
     */
    EClass OP_ASSIGN = eINSTANCE.getOpAssign();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpPlusAssignImpl <em>Op Plus Assign</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpPlusAssignImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpPlusAssign()
     * @generated
     */
    EClass OP_PLUS_ASSIGN = eINSTANCE.getOpPlusAssign();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpMinusAssignImpl <em>Op Minus Assign</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpMinusAssignImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpMinusAssign()
     * @generated
     */
    EClass OP_MINUS_ASSIGN = eINSTANCE.getOpMinusAssign();

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
     * The meta object literal for the '<em><b>Op</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_EQUALITY__OP = eINSTANCE.getExprEquality_Op();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_EQUALITY__RIGHT = eINSTANCE.getExprEquality_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpEqualsImpl <em>Op Equals</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpEqualsImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpEquals()
     * @generated
     */
    EClass OP_EQUALS = eINSTANCE.getOpEquals();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpUnequalsImpl <em>Op Unequals</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpUnequalsImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpUnequals()
     * @generated
     */
    EClass OP_UNEQUALS = eINSTANCE.getOpUnequals();

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
     * The meta object literal for the '<em><b>Op</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_COMPARISON__OP = eINSTANCE.getExprComparison_Op();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_COMPARISON__RIGHT = eINSTANCE.getExprComparison_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpLessEqImpl <em>Op Less Eq</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpLessEqImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpLessEq()
     * @generated
     */
    EClass OP_LESS_EQ = eINSTANCE.getOpLessEq();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpLessImpl <em>Op Less</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpLessImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpLess()
     * @generated
     */
    EClass OP_LESS = eINSTANCE.getOpLess();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpGreaterEqImpl <em>Op Greater Eq</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpGreaterEqImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpGreaterEq()
     * @generated
     */
    EClass OP_GREATER_EQ = eINSTANCE.getOpGreaterEq();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpGreaterImpl <em>Op Greater</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpGreaterImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpGreater()
     * @generated
     */
    EClass OP_GREATER = eINSTANCE.getOpGreater();

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
     * The meta object literal for the '<em><b>Op</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_ADDITIVE__OP = eINSTANCE.getExprAdditive_Op();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_ADDITIVE__RIGHT = eINSTANCE.getExprAdditive_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpPlusImpl <em>Op Plus</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpPlusImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpPlus()
     * @generated
     */
    EClass OP_PLUS = eINSTANCE.getOpPlus();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpMinusImpl <em>Op Minus</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpMinusImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpMinus()
     * @generated
     */
    EClass OP_MINUS = eINSTANCE.getOpMinus();

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
     * The meta object literal for the '<em><b>Op</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_MULT__OP = eINSTANCE.getExprMult_Op();

    /**
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_MULT__RIGHT = eINSTANCE.getExprMult_Right();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpMultImpl <em>Op Mult</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpMultImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpMult()
     * @generated
     */
    EClass OP_MULT = eINSTANCE.getOpMult();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpDivRealImpl <em>Op Div Real</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpDivRealImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpDivReal()
     * @generated
     */
    EClass OP_DIV_REAL = eINSTANCE.getOpDivReal();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpModRealImpl <em>Op Mod Real</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpModRealImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpModReal()
     * @generated
     */
    EClass OP_MOD_REAL = eINSTANCE.getOpModReal();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpModIntImpl <em>Op Mod Int</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpModIntImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpModInt()
     * @generated
     */
    EClass OP_MOD_INT = eINSTANCE.getOpModInt();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.OpDivIntImpl <em>Op Div Int</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.OpDivIntImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getOpDivInt()
     * @generated
     */
    EClass OP_DIV_INT = eINSTANCE.getOpDivInt();

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
     * The meta object literal for the '<em><b>Op</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_SIGN__OP = eINSTANCE.getExprSign_Op();

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
     * The meta object literal for the '<em><b>Right</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_NOT__RIGHT = eINSTANCE.getExprNot_Right();

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
     * The meta object literal for the '<em><b>Message</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_MEMBER__MESSAGE = eINSTANCE.getExprMember_Message();

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
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprBoolValImpl <em>Expr Bool Val</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprBoolValImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprBoolVal()
     * @generated
     */
    EClass EXPR_BOOL_VAL = eINSTANCE.getExprBoolVal();

    /**
     * The meta object literal for the '<em><b>Bool Val</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute EXPR_BOOL_VAL__BOOL_VAL = eINSTANCE.getExprBoolVal_BoolVal();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprFuncRefImpl <em>Expr Func Ref</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprFuncRefImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprFuncRef()
     * @generated
     */
    EClass EXPR_FUNC_REF = eINSTANCE.getExprFuncRef();

    /**
     * The meta object literal for the '<em><b>Name Val</b></em>' reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_FUNC_REF__NAME_VAL = eINSTANCE.getExprFuncRef_NameVal();

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
     * The meta object literal for the '<em><b>Name Val</b></em>' reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_IDENTIFIER__NAME_VAL = eINSTANCE.getExprIdentifier_NameVal();

    /**
     * The meta object literal for the '<em><b>Array Indizes</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_IDENTIFIER__ARRAY_INDIZES = eINSTANCE.getExprIdentifier_ArrayIndizes();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprNewObjectImpl <em>Expr New Object</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprNewObjectImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprNewObject()
     * @generated
     */
    EClass EXPR_NEW_OBJECT = eINSTANCE.getExprNewObject();

    /**
     * The meta object literal for the '<em><b>Class Def</b></em>' reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_NEW_OBJECT__CLASS_DEF = eINSTANCE.getExprNewObject_ClassDef();

    /**
     * The meta object literal for the '<em><b>Params</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_NEW_OBJECT__PARAMS = eINSTANCE.getExprNewObject_Params();

    /**
     * The meta object literal for the '{@link de.peeeq.pscript.pscript.impl.ExprThisImpl <em>Expr This</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see de.peeeq.pscript.pscript.impl.ExprThisImpl
     * @see de.peeeq.pscript.pscript.impl.PscriptPackageImpl#getExprThis()
     * @generated
     */
    EClass EXPR_THIS = eINSTANCE.getExprThis();

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
     * The meta object literal for the '<em><b>Name Val</b></em>' reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_FUNCTIONCALL__NAME_VAL = eINSTANCE.getExprFunctioncall_NameVal();

    /**
     * The meta object literal for the '<em><b>Params</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference EXPR_FUNCTIONCALL__PARAMS = eINSTANCE.getExprFunctioncall_Params();

  }

} //PscriptPackage
