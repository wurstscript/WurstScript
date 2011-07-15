/**
 * <copyright>
 * </copyright>
 *

 */
package de.peeeq.pscript.pscript.impl;

import de.peeeq.pscript.pscript.*;

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
public class PscriptFactoryImpl extends EFactoryImpl implements PscriptFactory
{
  /**
   * Creates the default factory implementation.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public static PscriptFactory init()
  {
    try
    {
      PscriptFactory thePscriptFactory = (PscriptFactory)EPackage.Registry.INSTANCE.getEFactory("http://www.peeeq.de/pscript/Pscript"); 
      if (thePscriptFactory != null)
      {
        return thePscriptFactory;
      }
    }
    catch (Exception exception)
    {
      EcorePlugin.INSTANCE.log(exception);
    }
    return new PscriptFactoryImpl();
  }

  /**
   * Creates an instance of the factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public PscriptFactoryImpl()
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
      case PscriptPackage.PROGRAM: return createProgram();
      case PscriptPackage.PACKAGE_DECLARATION: return createPackageDeclaration();
      case PscriptPackage.IMPORT: return createImport();
      case PscriptPackage.ENTITY: return createEntity();
      case PscriptPackage.INIT_BLOCK: return createInitBlock();
      case PscriptPackage.TYPE_DEF: return createTypeDef();
      case PscriptPackage.CLASS_MEMBER: return createClassMember();
      case PscriptPackage.VAR_DEF: return createVarDef();
      case PscriptPackage.TYPE_EXPR: return createTypeExpr();
      case PscriptPackage.FUNC_DEF: return createFuncDef();
      case PscriptPackage.STATEMENTS: return createStatements();
      case PscriptPackage.STATEMENT: return createStatement();
      case PscriptPackage.STMT_RETURN: return createStmtReturn();
      case PscriptPackage.STMT_IF: return createStmtIf();
      case PscriptPackage.ELSE_BLOCK: return createElseBlock();
      case PscriptPackage.STMT_WHILE: return createStmtWhile();
      case PscriptPackage.STMT_EXPR: return createStmtExpr();
      case PscriptPackage.EXPR: return createExpr();
      case PscriptPackage.OP_ASSIGNMENT: return createOpAssignment();
      case PscriptPackage.OP_EQUALITY: return createOpEquality();
      case PscriptPackage.OP_COMPARISON: return createOpComparison();
      case PscriptPackage.OP_ADDITIVE: return createOpAdditive();
      case PscriptPackage.OP_MULTIPLICATIVE: return createOpMultiplicative();
      case PscriptPackage.EXPR_LIST: return createExprList();
      case PscriptPackage.NATIVE_TYPE: return createNativeType();
      case PscriptPackage.CLASS_DEF: return createClassDef();
      case PscriptPackage.PARAMETER_DEF: return createParameterDef();
      case PscriptPackage.EXPR_ASSIGNMENT: return createExprAssignment();
      case PscriptPackage.OP_ASSIGN: return createOpAssign();
      case PscriptPackage.OP_PLUS_ASSIGN: return createOpPlusAssign();
      case PscriptPackage.OP_MINUS_ASSIGN: return createOpMinusAssign();
      case PscriptPackage.EXPR_OR: return createExprOr();
      case PscriptPackage.EXPR_AND: return createExprAnd();
      case PscriptPackage.EXPR_EQUALITY: return createExprEquality();
      case PscriptPackage.OP_EQUALS: return createOpEquals();
      case PscriptPackage.OP_UNEQUALS: return createOpUnequals();
      case PscriptPackage.EXPR_COMPARISON: return createExprComparison();
      case PscriptPackage.OP_LESS_EQ: return createOpLessEq();
      case PscriptPackage.OP_LESS: return createOpLess();
      case PscriptPackage.OP_GREATER_EQ: return createOpGreaterEq();
      case PscriptPackage.OP_GREATER: return createOpGreater();
      case PscriptPackage.EXPR_ADDITIVE: return createExprAdditive();
      case PscriptPackage.OP_PLUS: return createOpPlus();
      case PscriptPackage.OP_MINUS: return createOpMinus();
      case PscriptPackage.EXPR_MULT: return createExprMult();
      case PscriptPackage.OP_MULT: return createOpMult();
      case PscriptPackage.OP_DIV_REAL: return createOpDivReal();
      case PscriptPackage.OP_MOD_REAL: return createOpModReal();
      case PscriptPackage.OP_MOD_INT: return createOpModInt();
      case PscriptPackage.EXPR_SIGN: return createExprSign();
      case PscriptPackage.EXPR_NOT: return createExprNot();
      case PscriptPackage.EXPR_MEMBER: return createExprMember();
      case PscriptPackage.EXPR_INT_VAL: return createExprIntVal();
      case PscriptPackage.EXPR_NUM_VAL: return createExprNumVal();
      case PscriptPackage.EXPR_STRVAL: return createExprStrval();
      case PscriptPackage.EXPR_BOOL_VAL: return createExprBoolVal();
      case PscriptPackage.EXPR_BUILDIN_FUNCTION: return createExprBuildinFunction();
      case PscriptPackage.EXPR_FUNCTIONCALL: return createExprFunctioncall();
      case PscriptPackage.EXPR_IDENTIFIER: return createExprIdentifier();
      default:
        throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
    }
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Program createProgram()
  {
    ProgramImpl program = new ProgramImpl();
    return program;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public PackageDeclaration createPackageDeclaration()
  {
    PackageDeclarationImpl packageDeclaration = new PackageDeclarationImpl();
    return packageDeclaration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Import createImport()
  {
    ImportImpl import_ = new ImportImpl();
    return import_;
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
  public InitBlock createInitBlock()
  {
    InitBlockImpl initBlock = new InitBlockImpl();
    return initBlock;
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
  public ClassMember createClassMember()
  {
    ClassMemberImpl classMember = new ClassMemberImpl();
    return classMember;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public VarDef createVarDef()
  {
    VarDefImpl varDef = new VarDefImpl();
    return varDef;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public TypeExpr createTypeExpr()
  {
    TypeExprImpl typeExpr = new TypeExprImpl();
    return typeExpr;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FuncDef createFuncDef()
  {
    FuncDefImpl funcDef = new FuncDefImpl();
    return funcDef;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Statements createStatements()
  {
    StatementsImpl statements = new StatementsImpl();
    return statements;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Statement createStatement()
  {
    StatementImpl statement = new StatementImpl();
    return statement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public StmtReturn createStmtReturn()
  {
    StmtReturnImpl stmtReturn = new StmtReturnImpl();
    return stmtReturn;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public StmtIf createStmtIf()
  {
    StmtIfImpl stmtIf = new StmtIfImpl();
    return stmtIf;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ElseBlock createElseBlock()
  {
    ElseBlockImpl elseBlock = new ElseBlockImpl();
    return elseBlock;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public StmtWhile createStmtWhile()
  {
    StmtWhileImpl stmtWhile = new StmtWhileImpl();
    return stmtWhile;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public StmtExpr createStmtExpr()
  {
    StmtExprImpl stmtExpr = new StmtExprImpl();
    return stmtExpr;
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
  public OpAssignment createOpAssignment()
  {
    OpAssignmentImpl opAssignment = new OpAssignmentImpl();
    return opAssignment;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpEquality createOpEquality()
  {
    OpEqualityImpl opEquality = new OpEqualityImpl();
    return opEquality;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpComparison createOpComparison()
  {
    OpComparisonImpl opComparison = new OpComparisonImpl();
    return opComparison;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpAdditive createOpAdditive()
  {
    OpAdditiveImpl opAdditive = new OpAdditiveImpl();
    return opAdditive;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpMultiplicative createOpMultiplicative()
  {
    OpMultiplicativeImpl opMultiplicative = new OpMultiplicativeImpl();
    return opMultiplicative;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprList createExprList()
  {
    ExprListImpl exprList = new ExprListImpl();
    return exprList;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NativeType createNativeType()
  {
    NativeTypeImpl nativeType = new NativeTypeImpl();
    return nativeType;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ClassDef createClassDef()
  {
    ClassDefImpl classDef = new ClassDefImpl();
    return classDef;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ParameterDef createParameterDef()
  {
    ParameterDefImpl parameterDef = new ParameterDefImpl();
    return parameterDef;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprAssignment createExprAssignment()
  {
    ExprAssignmentImpl exprAssignment = new ExprAssignmentImpl();
    return exprAssignment;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpAssign createOpAssign()
  {
    OpAssignImpl opAssign = new OpAssignImpl();
    return opAssign;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpPlusAssign createOpPlusAssign()
  {
    OpPlusAssignImpl opPlusAssign = new OpPlusAssignImpl();
    return opPlusAssign;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpMinusAssign createOpMinusAssign()
  {
    OpMinusAssignImpl opMinusAssign = new OpMinusAssignImpl();
    return opMinusAssign;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprOr createExprOr()
  {
    ExprOrImpl exprOr = new ExprOrImpl();
    return exprOr;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprAnd createExprAnd()
  {
    ExprAndImpl exprAnd = new ExprAndImpl();
    return exprAnd;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprEquality createExprEquality()
  {
    ExprEqualityImpl exprEquality = new ExprEqualityImpl();
    return exprEquality;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpEquals createOpEquals()
  {
    OpEqualsImpl opEquals = new OpEqualsImpl();
    return opEquals;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpUnequals createOpUnequals()
  {
    OpUnequalsImpl opUnequals = new OpUnequalsImpl();
    return opUnequals;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprComparison createExprComparison()
  {
    ExprComparisonImpl exprComparison = new ExprComparisonImpl();
    return exprComparison;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpLessEq createOpLessEq()
  {
    OpLessEqImpl opLessEq = new OpLessEqImpl();
    return opLessEq;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpLess createOpLess()
  {
    OpLessImpl opLess = new OpLessImpl();
    return opLess;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpGreaterEq createOpGreaterEq()
  {
    OpGreaterEqImpl opGreaterEq = new OpGreaterEqImpl();
    return opGreaterEq;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpGreater createOpGreater()
  {
    OpGreaterImpl opGreater = new OpGreaterImpl();
    return opGreater;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprAdditive createExprAdditive()
  {
    ExprAdditiveImpl exprAdditive = new ExprAdditiveImpl();
    return exprAdditive;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpPlus createOpPlus()
  {
    OpPlusImpl opPlus = new OpPlusImpl();
    return opPlus;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpMinus createOpMinus()
  {
    OpMinusImpl opMinus = new OpMinusImpl();
    return opMinus;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprMult createExprMult()
  {
    ExprMultImpl exprMult = new ExprMultImpl();
    return exprMult;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpMult createOpMult()
  {
    OpMultImpl opMult = new OpMultImpl();
    return opMult;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpDivReal createOpDivReal()
  {
    OpDivRealImpl opDivReal = new OpDivRealImpl();
    return opDivReal;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpModReal createOpModReal()
  {
    OpModRealImpl opModReal = new OpModRealImpl();
    return opModReal;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpModInt createOpModInt()
  {
    OpModIntImpl opModInt = new OpModIntImpl();
    return opModInt;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprSign createExprSign()
  {
    ExprSignImpl exprSign = new ExprSignImpl();
    return exprSign;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprNot createExprNot()
  {
    ExprNotImpl exprNot = new ExprNotImpl();
    return exprNot;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprMember createExprMember()
  {
    ExprMemberImpl exprMember = new ExprMemberImpl();
    return exprMember;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprIntVal createExprIntVal()
  {
    ExprIntValImpl exprIntVal = new ExprIntValImpl();
    return exprIntVal;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprNumVal createExprNumVal()
  {
    ExprNumValImpl exprNumVal = new ExprNumValImpl();
    return exprNumVal;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprStrval createExprStrval()
  {
    ExprStrvalImpl exprStrval = new ExprStrvalImpl();
    return exprStrval;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprBoolVal createExprBoolVal()
  {
    ExprBoolValImpl exprBoolVal = new ExprBoolValImpl();
    return exprBoolVal;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprBuildinFunction createExprBuildinFunction()
  {
    ExprBuildinFunctionImpl exprBuildinFunction = new ExprBuildinFunctionImpl();
    return exprBuildinFunction;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprFunctioncall createExprFunctioncall()
  {
    ExprFunctioncallImpl exprFunctioncall = new ExprFunctioncallImpl();
    return exprFunctioncall;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ExprIdentifier createExprIdentifier()
  {
    ExprIdentifierImpl exprIdentifier = new ExprIdentifierImpl();
    return exprIdentifier;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public PscriptPackage getPscriptPackage()
  {
    return (PscriptPackage)getEPackage();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @deprecated
   * @generated
   */
  @Deprecated
  public static PscriptPackage getPackage()
  {
    return PscriptPackage.eINSTANCE;
  }

} //PscriptFactoryImpl
