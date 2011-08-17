package de.peeeq.pscript.intermediateLang;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.google.common.base.Function;
import com.google.inject.Inject;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.pscript.attributes.AttrExprType;
import de.peeeq.pscript.attributes.AttrTypeExprType;
import de.peeeq.pscript.attributes.AttrVarDefType;
import de.peeeq.pscript.attributes.infrastructure.AttributeManager;
import de.peeeq.pscript.pscript.ClassDef;
import de.peeeq.pscript.pscript.ClassMember;
import de.peeeq.pscript.pscript.ClassSlots;
import de.peeeq.pscript.pscript.ConstructorDef;
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
import de.peeeq.pscript.pscript.ExprMult;
import de.peeeq.pscript.pscript.ExprNewObject;
import de.peeeq.pscript.pscript.ExprNot;
import de.peeeq.pscript.pscript.ExprNumVal;
import de.peeeq.pscript.pscript.ExprOr;
import de.peeeq.pscript.pscript.ExprSign;
import de.peeeq.pscript.pscript.ExprStrval;
import de.peeeq.pscript.pscript.ExprThis;
import de.peeeq.pscript.pscript.FuncDef;
import de.peeeq.pscript.pscript.InitBlock;
import de.peeeq.pscript.pscript.NativeFunc;
import de.peeeq.pscript.pscript.NativeType;
import de.peeeq.pscript.pscript.OnDestroyDef;
import de.peeeq.pscript.pscript.OpAdditive;
import de.peeeq.pscript.pscript.OpComparison;
import de.peeeq.pscript.pscript.OpDivInt;
import de.peeeq.pscript.pscript.OpDivReal;
import de.peeeq.pscript.pscript.OpEquals;
import de.peeeq.pscript.pscript.OpGreater;
import de.peeeq.pscript.pscript.OpGreaterEq;
import de.peeeq.pscript.pscript.OpLess;
import de.peeeq.pscript.pscript.OpLessEq;
import de.peeeq.pscript.pscript.OpMinus;
import de.peeeq.pscript.pscript.OpModInt;
import de.peeeq.pscript.pscript.OpModReal;
import de.peeeq.pscript.pscript.OpMult;
import de.peeeq.pscript.pscript.OpMultiplicative;
import de.peeeq.pscript.pscript.OpPlus;
import de.peeeq.pscript.pscript.OpUnequals;
import de.peeeq.pscript.pscript.PackageDeclaration;
import de.peeeq.pscript.pscript.Program;
import de.peeeq.pscript.pscript.Statement;
import de.peeeq.pscript.pscript.Statements;
import de.peeeq.pscript.pscript.StmtCall;
import de.peeeq.pscript.pscript.StmtChangeRefCount;
import de.peeeq.pscript.pscript.StmtDestroy;
import de.peeeq.pscript.pscript.StmtIf;
import de.peeeq.pscript.pscript.StmtReturn;
import de.peeeq.pscript.pscript.StmtSet;
import de.peeeq.pscript.pscript.StmtSetOrCallOrVarDef;
import de.peeeq.pscript.pscript.StmtWhile;
import de.peeeq.pscript.pscript.TypeDef;
import de.peeeq.pscript.pscript.VarDef;
import de.peeeq.pscript.pscript.util.ClassMemberSwitch;
import de.peeeq.pscript.pscript.util.ClassMemberSwitchVoid;
import de.peeeq.pscript.pscript.util.ClassSlotsSwitchVoid;
import de.peeeq.pscript.pscript.util.EntitySwitchVoid;
import de.peeeq.pscript.pscript.util.ExprSwitch;
import de.peeeq.pscript.pscript.util.OpAdditiveSwitch;
import de.peeeq.pscript.pscript.util.OpComparisonSwitch;
import de.peeeq.pscript.pscript.util.OpEqualitySwitch;
import de.peeeq.pscript.pscript.util.OpMultiplicativeSwitch;
import de.peeeq.pscript.pscript.util.StatementSwitch;
import de.peeeq.pscript.pscript.util.StmtSetOrCallOrVarDefSwitchVoid;
import de.peeeq.pscript.pscript.util.TypeDefSwitchVoid;
import de.peeeq.pscript.types.PScriptTypeArray;
import de.peeeq.pscript.types.PScriptTypeBool;
import de.peeeq.pscript.types.PScriptTypeInt;
import de.peeeq.pscript.types.PScriptTypeVoid;
import de.peeeq.pscript.types.PscriptType;
import de.peeeq.pscript.types.PscriptTypeClass;
import de.peeeq.pscript.utils.NotNullList;
import de.peeeq.pscript.utils.Utils;

public class IntermediateCodeGeneratorImpl implements IntermediateCodeGenerator  {

	
	@Inject
	private AttributeManager attrManager;

	private ILprog prog;

	private int localVarNumberOffset;
	
	private WurstNames wurstNames;

	@Override
	public ILprog translateProg(Resource resource) {
		
		try {
			EList<EObject> programs = resource.getContents();
			translatePrograms(programs);
			
			
		} catch (Throwable t) {
			System.err.println("Unexpected Error while translating to intermediate language: ");
			t.printStackTrace();
		}		
		
		return prog; 
	}

	@Override
	public ILprog translatePrograms(Iterable<? extends EObject> programs) {
		wurstNames = new WurstNames();
		prog = new ILprog(attrManager);
		List<PackageDeclaration> packages = new NotNullList<PackageDeclaration>();
		for (EObject p : programs) {
			if (p instanceof Program) {
				Program program = (Program) p;
				for (PackageDeclaration pack : program.getPackages()) {
					packages.add(pack);
				}
			} else {
				throw new Error("A resource should only consist of programs, but " + p + " is of type " + p.getClass());
			}
		}
		
		
		for (PackageDeclaration pack : packages) {
			translatePackage(pack);
		}
		return prog;
	}

	private void translatePackage(final PackageDeclaration pack) {
		for (Entity elem : pack.getElements()) {
			new EntitySwitchVoid() {

				@Override
				public void caseInitBlock(InitBlock initBlock) {
					String name = prog.getNewName(pack.getName() + "_init");
					List<ILvar> params = new NotNullList<ILvar>();
					List<ILvar> locals = collectLocals(initBlock.getBody());
					List<ILStatement> body = translateStatements(initBlock.getBody(), locals);
					
					
					PscriptType returntype = PScriptTypeVoid.instance();
					ILfunction function = new ILfunction(name, params , returntype , locals, body);
					prog.addFunction(function);
				}

				@Override
				public void caseTypeDef(TypeDef typeDef) {
					new TypeDefSwitchVoid() {
						
						@Override
						public void caseNativeType(NativeType nativeType) {
							// native types are just here to help the compiler		
							// so nothing to do here
						}

						@Override
						public void caseClassDef(ClassDef classDef) {
							final List<OnDestroyDef> onDestroyDefs = new NotNullList<OnDestroyDef>();
							final List<ConstructorDef> constructors = new NotNullList<ConstructorDef>();
							final List<VarDef> fields = new NotNullList<VarDef>();
							final List<FuncDef> methods = new NotNullList<FuncDef>();
							for (ClassSlots elem : classDef.getMembers()) {
								new ClassSlotsSwitchVoid() {
									
									@Override
									public void caseOnDestroyDef(OnDestroyDef d) {
										 onDestroyDefs.add(d);										
									}
									
									@Override
									public void caseConstructorDef(ConstructorDef constructorDef) {
										constructors.add(constructorDef);
									}
									
									@Override
									public void caseClassMember(ClassMember classMember) {
										new ClassMemberSwitchVoid() {
											
											@Override
											public void caseVarDef(VarDef varDef) {
												fields.add(varDef);
											}
											
											@Override
											public void caseFuncDef(FuncDef funcDef) {
												methods.add(funcDef);
											}
										}.doSwitch(classMember);
										
									}
								}.doSwitch(elem);
							}
							
							// create an array for every field:
									
							
							// translate methods:
							
							// generate/translate constructors:
							
							// generate/translate destructor:
							
							// generate incref/decref methods:						
							
						}
					}.doSwitch(typeDef);
					
				}

				@Override
				public void caseFuncDef(FuncDef funcDef) {
					if (funcDef instanceof NativeFunc) {
						// do not translate native functions
						return;
						
					}
					
					ILfunction function = prog.getFunc(funcDef);
					
					List<ILvar> params = translateParams(funcDef.getParameters());
					List<ILvar> locals = collectLocals(funcDef.getBody());
					locals.addAll(params);
					List<ILStatement> body = translateStatements(funcDef.getBody(), locals);
					
					PscriptType returnType;
					if (funcDef.getType() != null) {
						returnType = attrManager.getAttValue(AttrTypeExprType.class, funcDef.getType());
					} else {
						returnType = PScriptTypeVoid.instance();
					}
					function.set(params, returnType , locals, body);  
//					prog.addFunction(function);
				}

				@Override
				public void caseVarDef(VarDef varDef) { // global variable
					// TODO add init code
				}

				
			}.doSwitch(elem);
		}		
	}

	protected List<ILvar> collectLocals(Statements body) {
		ImmutableList<VarDef> varDefs = Utils.collectRec(body, VarDef.class);
		
		List<ILvar> vars = Utils.map(varDefs, new Function<VarDef, ILvar>() {

			@Override
			public ILvar apply(VarDef from) {
				PscriptType type = attrManager.getAttValue(AttrVarDefType.class, from);
				// we do not have to rename vars, as they are already unique 
				//(or should they be renamed as the might shadow global variables?)
				return new ILvar(from.getName(), type);
			}
		});
		
		return Utils.removeDuplicates(vars);	
	}

	protected List<ILStatement> translateStatements(Statements body, List<ILvar> locals) {
		if (body == null) {
			return new LinkedList<ILStatement>();
		}
		return translateStatements(body.getStatements(), locals);
	}

	private List<ILStatement> translateStatements(EList<Statement> statements, List<ILvar> locals) {
		List<ILStatement> result = new NotNullList<ILStatement>();
		for (Statement s : statements) {
			result.addAll(translateStatement(s, locals));
		}
		return result ;
	}

	private List<ILStatement> translateStatement(Statement s, final List<ILvar> locals) {
		final List<ILStatement> result = new NotNullList<ILStatement>();
		return new StatementSwitch<List<ILStatement>> () {

			@Override
			public List<ILStatement> caseStmtWhile(StmtWhile stmtWhile) {
				ILvar var_cond = getNewLocalVar("whileCond", PScriptTypeBool.instance(), locals);
				ILvar var_condNot = getNewLocalVar("not_whileCond", PScriptTypeBool.instance(), locals);
				List<ILStatement> cond = translateExpr(stmtWhile.getCond(), var_cond, locals);
				List<ILStatement> whileBody = translateStatements(stmtWhile.getBody(), locals);
				
				List<ILStatement> loopBody = new NotNullList<ILStatement>();
				loopBody.addAll(cond);
				loopBody.add(new IlsetUnary(var_condNot, Iloperator.NOT, var_cond));
				loopBody.add(new ILexitwhen(var_condNot));
				loopBody.addAll(whileBody);
				result.add(new ILloop(loopBody));	
				
				return result;
			}

			@Override
			public List<ILStatement> caseStmtReturn(StmtReturn stmtReturn) {
				PscriptType returnType = attrManager.getAttValue(AttrExprType.class, stmtReturn.getE());
				ILvar resultVar = getNewLocalVar("returnResult", returnType , locals);
				result.addAll(translateExpr(stmtReturn.getE(), resultVar, locals));
				result.add(new ILreturn(resultVar));
				return result;
			}

			@Override
			public List<ILStatement> caseStmtIf(StmtIf stmtIf) {
				ILvar resultVar = getNewLocalVar("ifCond", PScriptTypeBool.instance(), locals);
				List<ILStatement> cond = translateExpr(stmtIf.getCond(), resultVar, locals);
				List<ILStatement> thenBlock = translateStatements(stmtIf.getThenBlock(), locals);
				List<ILStatement> elseBlock = translateStatements(stmtIf.getElseBlock(), locals); 
//						translateElseBlock(0, stmtIf.getElseIfConds(), stmtIf.getElseIfBlocks(), 
//						stmtIf.getElseBlock(), locals);
				result.addAll(cond);
				result.add(new ILif(resultVar, thenBlock, elseBlock));
				return result;
			}

			@Override
			public List<ILStatement> caseVarDef(VarDef varDef) {
				if (varDef.getE() == null) {
					return new LinkedList<ILStatement>();
				} else {
					return translateAssignment(getLocalVar(varDef.getName(), locals), varDef.getE(), locals);
				}
			}

			@Override
			public List<ILStatement> caseStmtSetOrCallOrVarDef(	StmtSetOrCallOrVarDef stmtSetOrCall) {
				new StmtSetOrCallOrVarDefSwitchVoid() {
					

					@Override
					public void caseStmtSet(StmtSet stmtSet) {
						translateStmtSet(stmtSet);
						
					}

					@Override
					public void caseStmtCall(StmtCall stmtCall) {
						result.addAll(translateExpr(stmtCall.getE(), null , locals));
					}

//					@Override
//					public void caseVarDef(VarDef varDef) {
//						translateStmtVarDef(varDef);
//					}
					
				}.doSwitch(stmtSetOrCall);
				return result;
			}

//			@Override
//			public List<ILStatement> caseStmtLoop(StmtLoop stmtLoop) {
//				List<ILStatement> body = translateStatements(stmtLoop.getBody(), locals);
//				result.add(new ILloop(body));
//				return result;
//			}

//			@Override
//			public List<ILStatement> caseStmtExitwhen(StmtExitwhen stmtExitwhen) {
//				ILvar exitWhenVar = getNewLocalVar("exitwhenVar", PScriptTypeBool.instance(), locals);
//				result.addAll(translateExpr(stmtExitwhen.getE(), exitWhenVar, locals));
//				result.add(new ILexitwhen(exitWhenVar));
//				return result;
//			}

//			@Override
//			public List<ILStatement> caseStmtSet(StmtSet stmtSet) {
//				translateStmtSet(stmtSet);
//				return result;
//			}

			private void translateStmtSet(StmtSet stmtSet) {
				Expr left = stmtSet.getLeft().getE();
				if (left instanceof ExprIdentifier) {
					ExprIdentifier left2 = (ExprIdentifier) left;
					ILvar leftVar = getVar(left2.getNameVal(), locals);
					
					if (left2.getArrayIndizes().size() == 0) {
						result.addAll(translateExpr(stmtSet.getRight(), leftVar , locals));
					} else { // array access
						EList<Expr> indizes = left2.getArrayIndizes();
						PScriptTypeArray type = (PScriptTypeArray) attrManager.getAttValue(AttrVarDefType.class, left2.getNameVal());
						
						// TODO calculate index sum
						ILvar indexSum = getNewLocalVar("index", PScriptTypeInt.instance(), locals);
						result.addAll(calculateIndexes(type, indexSum, indizes, locals));
						
						
						ILvar tempResult = getNewLocalVar("tempResult", type.getBaseType(), locals);
						result.addAll(translateExpr(stmtSet.getRight(), tempResult, locals));
						
						result.add(new ILarraySetVar(leftVar, indexSum, tempResult));
					}
					
				} else {
					// TODO assignments to arrays, classmembers
					throw new Error("not implemented Assignment to " + left);
				}
			}
			
//			private void translateStmtVarDef(VarDef v) {
//				ILvar leftVar = getVar(v, locals);
//				if (v.getE() != null) {
//					result.addAll(translateExpr(v.getE(), leftVar , locals));
//				}
//			}

			@Override
			public List<ILStatement> caseStmtDestroy(StmtDestroy stmtDestroy) {
				// get the type of class we want to destroy:
				PscriptType objType = attrManager.getAttValue(AttrExprType.class, stmtDestroy.getObj());
				if (objType instanceof PscriptTypeClass) {
					// we know the exact class and can directly call the destructor:
					PscriptTypeClass classType = (PscriptTypeClass) objType;
					PscriptType[] argumentTypes = new PscriptType[0];
					ILvar[] argumentVars = new ILvar[0];
					// call the destroy method:
					result.add(new ILfunctionCall(null, wurstNames.getDestructorFunctionName(classType.getClassDef()), argumentTypes, argumentVars));
					
				} else {
					// TODO implement for destroying interfaces and such things
					throw new Error("Destroying not implemented for type " + objType);
				}
				return result;
			}

			@Override
			public List<ILStatement> caseStmtChangeRefCount(
					StmtChangeRefCount stmtChangeRefCount) {
				// TODO Statement chage Ref-Count
				throw new Error("refcount not implemented");
			}
			

//			@Override
//			public List<ILStatement> caseStmtCall(StmtCall stmtCall) {
//				result.addAll(translateExpr(stmtCall.getE(), null , locals));
//				return result;
//			}

			
		}.doSwitch(s);
	}



	protected List<ILStatement> translateElseBlock(int i, EList<Expr> elseIfConds, EList<Statements> elseIfBlocks,
			Statements elseBlock, List<ILvar> locals) {
		if (i >= elseIfConds.size()) {
			return translateStatements(elseBlock, locals);
		} else {
			List<ILStatement> result = new LinkedList<ILStatement>();
			ILvar condVar = getNewLocalVar("elseifCond", PScriptTypeBool.instance(), locals);
			List<ILStatement> cond = translateExpr(elseIfConds.get(i), condVar , locals);
			result.addAll(cond);
			
			List<ILStatement> then1 = translateStatements(elseIfBlocks.get(i), locals);
			List<ILStatement> else1 = translateElseBlock(i+1, elseIfConds, elseIfBlocks, elseBlock, locals);
			
			result.add(new ILif(condVar, then1, else1));
			
			return result ;
		}
		
	}

	protected List<ILStatement> translateAssignment(ILvar localVar, Expr e, List<ILvar> locals ) {
		return translateExpr(e, localVar, locals);
	}

	protected ILvar getLocalVar(String name, List<ILvar> locals) {
		for (ILvar v : locals) {
			if (v.getName().equals(name)) {
				return v;
			}
		}
		throw new Error("Variable " + name + " not found.");
	}

	protected ILvar getNewLocalVar(String name, PscriptType typ, List<ILvar> locals) {
		int counter = locals.size() + localVarNumberOffset;
		while (varExists(locals, name + counter)) {
			counter++;
			localVarNumberOffset++;
		}
		ILvar v = new ILvar(name + counter, typ);
		locals.add(v);
		return v;
	}

	private boolean varExists(List<ILvar> locals, String name) {
		for (ILvar v : locals) {
			if (v.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	protected List<ILStatement> translateExpr(final Expr e, final ILvar resultVar, final List<ILvar> locals) {
		if (e == null) throw new IllegalArgumentException("e must not be null");
		
		final List<ILStatement> result = new NotNullList<ILStatement>();
		return new ExprSwitch<List<ILStatement>>() {


			private List<ILStatement> translateBinary(Expr left, Iloperator op, Expr right) {
				PscriptType leftType = attrManager.getAttValue(AttrExprType.class, left);
				PscriptType rightType = attrManager.getAttValue(AttrExprType.class, right);				
				ILvar leftResult = getNewLocalVar("tempL", leftType, locals);
				ILvar rightResult = getNewLocalVar("tempR", rightType, locals);
				result.addAll(translateExpr(left, leftResult, locals));
				result.addAll(translateExpr(right, rightResult, locals));
				result.add(new ILsetBinary(resultVar, leftResult, op, rightResult));
				return result;
			}
			
			private List<ILStatement> translateUnary(Expr right, Iloperator op,
					List<ILvar> locals) {
				PscriptType rightType = attrManager.getAttValue(AttrExprType.class, right);
				ILvar rightResult = getNewLocalVar("tempU", rightType , locals);
				result.addAll(translateExpr(right, rightResult, locals));
				result.add(new IlsetUnary(resultVar, op, rightResult));
				return result;
			}
			
			@Override
			public List<ILStatement> caseExprEquality(ExprEquality exprEquality) {
				Iloperator op = new OpEqualitySwitch<Iloperator>() {

					@Override
					public Iloperator caseOpUnequals(OpUnequals opUnequals) {
						return Iloperator.UNEQUALITY;
					}

					@Override
					public Iloperator caseOpEquals(OpEquals opEquals) {
						return Iloperator.EQUALITY;
					}
				}.doSwitch(exprEquality.getOp());
				
				return translateBinary(exprEquality.getLeft(), op , exprEquality.getRight());
			}

			

			@Override
			public List<ILStatement> caseExprIdentifier(ExprIdentifier exprIdentifier) {
				VarDef decl = exprIdentifier.getNameVal();
				ILvar var = getVar(decl, locals);
				if (exprIdentifier.getArrayIndizes().size() == 0) {
					result.add(new ILsetVar(resultVar, var));
					return result;
				} else {
					ILvar index = getNewLocalVar("index", PScriptTypeInt.instance(), locals);
					result.addAll(calculateIndexes((PScriptTypeArray) var.getType(), index, exprIdentifier.getArrayIndizes(), locals));
					result.add(new ILsetVarArray(resultVar, var, index));
					return result;
				}
			}

			@Override
			public List<ILStatement> caseExprAdditive(ExprAdditive exprAdditive) {
				Iloperator op = translateOp(exprAdditive.getOp());
				return translateBinary(exprAdditive.getLeft(), op , exprAdditive.getRight());
			}

//			@Override
//			public List<ILStatement> caseExprBuildinFunction(ExprBuildinFunction exprBuildinFunction) {
//				ILvar[] paramVars = new ILvar[0];
//				if (exprBuildinFunction.getParameters() != null && exprBuildinFunction.getParameters().getParams() != null) {
//					EList<Expr> params = exprBuildinFunction.getParameters().getParams();
//					// translate params
//					paramVars = new ILvar[params.size()];
//					for (int i=0; i<params.size(); i++) {
//						PscriptType typ = attrManager.getAttValue(AttrExprType.class, params.get(i));
//						//						paramVars[i] = getLocalVar( exprBuildinFunction.getName() +  "_param" + i , locals);
//						paramVars[i] = getNewLocalVar( exprBuildinFunction.getName() +  "_param" + i, typ, locals);
//						result.addAll(translateExpr(params.get(i), paramVars[i], locals));
//					}
//					
//				}
//				result.add(new IlbuildinFunctionCall(resultVar, exprBuildinFunction.getName(), paramVars));
//				return result;
//			}

			@Override
			public List<ILStatement> caseExprSign(ExprSign exprSign) {
				return translateUnary(exprSign.getRight(), translateOp(exprSign.getOp()), locals);
			}

			

			@Override
			public List<ILStatement> caseExprMember(final ExprMember exprMember) {
				ClassMember classMember = exprMember.getMessage().getNameVal();
				return new ClassMemberSwitch<List<ILStatement>>() {

					@Override
					public List<ILStatement> caseVarDef(VarDef varDef) {
						// TODO interfaces & getter/setter methods
						
						
						
						// TODO class members
						throw new Error("Not implemented");
					}

					@Override
					public List<ILStatement> caseFuncDef(FuncDef funcDef) {
						EList<Expr> argList = exprMember.getMessage().getParams();
						
						List<Expr> args = new NotNullList<Expr>();
						args.add(exprMember.getLeft());
						if (argList != null) {
							args.addAll(argList);
						}
						return translateFunctionCall(resultVar, funcDef, args, locals);
					}
				}.doSwitch(classMember);
				
//				// this is either a function call or a class variable access
//				if (exprMember.getRight() instanceof ExprFunctioncall) {
//					ExprFunctioncall fc = (ExprFunctioncall) exprMember.getRight();
//					ExprList argList = fc.getParameters();
//					List<Expr> args = new NotNullList<Expr>();
//					args.add(exprMember.getLeft());
//					if (argList != null && argList.getParams() != null) {
//						args.addAll(argList.getParams());
//					}
//					return translateFunctionCall(resultVar, fc.getNameVal(), args, locals);
//				}
//				if (exprMember.getRight() instanceof ExprIdentifier) {
//					// TODO class variable access
//					throw new Error("not implemented");
//				}
//				throw new Error("no other case possible? / not implemented");
			}

			@Override
			public List<ILStatement> caseExprNumVal(ExprNumVal exprNumVal) {
				result.add(new IlsetConst(resultVar, new ILconstNum(exprNumVal.getNumVal())));
				return result;
			}

//			@Override
//			public List<ILStatement> caseExprAssignment(final ExprAssignment exprAssignment) {
//				
//				new OpAssignmentSwitchVoid() {
//
//					@Override
//					public void caseOpPlusAssign(OpPlusAssign opPlusAssign) {
//						throw new Error("not implemented"); // TODO
//					}
//
//					@Override
//					public void caseOpAssign(OpAssign opAssign) {
//						Expr left = exprAssignment.getLeft();
//						if (left instanceof ExprIdentifier) {
//							ExprIdentifier id = (ExprIdentifier) left;
//							VarDef decl = id.getNameVal();
//							ILvar var = getVar(decl, locals);
//							result.addAll(translateExpr(exprAssignment.getRight(), var, locals));
//						}
//						// TODO other assignments
//					}
//
//					@Override
//					public void caseOpMinusAssign(OpMinusAssign opMinusAssign) {
//						throw new Error("not implemented"); // TODO
//					}
//					
//				}.doSwitch(exprAssignment.getOp());
//				// TODO different assignment operators
//				
//								
//				return result;
//			}

			// Constants:
			
			@Override
			public List<ILStatement> caseExprStrval(ExprStrval exprStrval) {
				result.add(new IlsetConst(resultVar, new ILconstString(exprStrval.getStrVal())));
				return result;
			}
			
			@Override
			public List<ILStatement> caseExprIntVal(ExprIntVal exprIntVal) {
				result.add(new IlsetConst(resultVar, new ILconstInt(exprIntVal.getIntVal())));
				return result;
			}
			
			@Override
			public List<ILStatement> caseExprBoolVal(ExprBoolVal exprBoolVal) {
				result.add(new IlsetConst(resultVar, new ILconstBool(exprBoolVal.getBoolVal())));
				return result;
			}

			@Override
			public List<ILStatement> caseExprNot(ExprNot exprNot) {
				return translateUnary(exprNot.getRight(), Iloperator.NOT , locals);
			}

			@Override
			public List<ILStatement> caseExprOr(ExprOr exprOr) {
				Expr left = exprOr.getLeft();
				Expr right = exprOr.getRight();
				result.addAll(translateExpr(left, resultVar, locals));
				result.add(new ILif(resultVar, new NotNullList<ILStatement>(), translateExpr(right, resultVar, locals)));
				return result;
			}

			@Override
			public List<ILStatement> caseExprAnd(ExprAnd exprAnd) {
				Expr left = exprAnd.getLeft();
				Expr right = exprAnd.getRight();
				result.addAll(translateExpr(left, resultVar, locals));
				result.add(new ILif(resultVar, translateExpr(right, resultVar, locals), new NotNullList<ILStatement>()));
				return result;
			}

			@Override
			public List<ILStatement> caseExprMult(ExprMult exprMult) {
				return translateBinary(exprMult.getLeft(), translateOp(exprMult.getOp()), exprMult.getRight());
			}
			
			@Override
			public List<ILStatement> caseExprComparison(ExprComparison exprComparison) {
				return translateBinary(exprComparison.getLeft(), translateOp(exprComparison.getOp()), exprComparison.getRight());
			}

			@Override
			public List<ILStatement> caseExprFunctioncall(ExprFunctioncall exprFunctioncall) {
				List<Expr> args = new NotNullList<Expr>();
				if (exprFunctioncall.getParams() != null) {
					args = exprFunctioncall.getParams();
				}
				return translateFunctionCall(resultVar, exprFunctioncall.getNameVal(), args , locals);
			}

			@Override
			public List<ILStatement> caseExprFuncRef(ExprFuncRef exprFuncRef) {
				// TODO translate function references
				return null;
			}

			@Override
			public List<ILStatement> caseExprThis(ExprThis exprThis) {
				ILvar thisVar = getThis(locals);
				result.add(new ILsetVar(resultVar, thisVar ));
				return result;
			}

			@Override
			public List<ILStatement> caseExprNewObject(
					ExprNewObject exprNewObject) {
				EList<Expr> args = exprNewObject.getParams();
				return translateConstructorCall(resultVar, exprNewObject.getClassDef(), args , locals);
			}

			

			

			

		}.doSwitch(e);
	}



	/**
	 * returns the variable which represents the if or creates one if it does not exist yet
	 */
	protected ILvar getThis(List<ILvar> locals) {
		for (ILvar l : locals) {
			if (l.getName().startsWith("w__this")) {
				return l;
			}
		}
		ILvar result = new ILvar("w__this", PScriptTypeInt.instance());
		locals.add(result);
		return result;
	}

	protected List<ILStatement> calculateIndexes( PScriptTypeArray type, ILvar indexResult, EList<Expr> indizes, List<ILvar> locals) {
		LinkedList<ILStatement> result = new LinkedList<ILStatement>();
		ILvar[] indexVar = new ILvar[indizes.size()];
		ILvar[] indexVarM = new ILvar[indizes.size()];
		
		// calculate indizes
		for (int i = 0; i < indizes.size(); i++) {
			indexVar[i] = getNewLocalVar("index", PScriptTypeInt.instance(), locals);
			indexVarM[i] = getNewLocalVar("indexM", PScriptTypeInt.instance(), locals);
			
			int rightSize = 1;
			for (int j = i+1; j < indizes.size(); j++) {
				rightSize *= type.getSize(j);
			}
			
			result.addAll(translateExpr(indizes.get(i), indexVar[i], locals));
			result.add(new ILsetBinaryCR(indexVarM[i], indexVar[i], Iloperator.MULT, new ILconstInt(rightSize)));
		}
		
		
		result.add(new ILsetVar(indexResult, indexVarM[0]));
		
		// calculate the sum of the indizes2:
		for (int i=1; i < indizes.size(); i++) {
			result.add(new ILsetBinary(indexResult, indexResult, Iloperator.PLUS, indexVarM[i]));
		}
		return result;
	}

	protected Iloperator translateOp(OpComparison op) {
		return new OpComparisonSwitch<Iloperator>() {

			@Override
			public Iloperator caseOpLessEq(OpLessEq opLessEq) {
				return Iloperator.LESS_EQ;
			}

			@Override
			public Iloperator caseOpGreater(OpGreater opGreater) {
				return Iloperator.GREATER;
			}

			@Override
			public Iloperator caseOpGreaterEq(OpGreaterEq opGreaterEq) {
				return Iloperator.GREATER_EQ;
			}

			@Override
			public Iloperator caseOpLess(OpLess opLess) {
				return Iloperator.LESS;
			}
			
		}.doSwitch(op);
	}

	protected Iloperator translateOp(OpMultiplicative op) {
		return new OpMultiplicativeSwitch<Iloperator>() {

			@Override
			public Iloperator caseOpMult(OpMult opMult) {
				return Iloperator.MULT;
			}

			@Override
			public Iloperator caseOpModInt(OpModInt opModInt) {
				return Iloperator.MOD_INT;
			}

			@Override
			public Iloperator caseOpDivReal(OpDivReal opDivReal) {
				return Iloperator.DIV_REAL;
			}

			@Override
			public Iloperator caseOpModReal(OpModReal opModReal) {
				return Iloperator.MOD_REAL;
			}

			@Override
			public Iloperator caseOpDivInt(OpDivInt opDivInt) {
				return Iloperator.DIV_INT;
			}
		}.doSwitch(op);
	}

	protected Iloperator translateOp(OpAdditive op) {
		return new OpAdditiveSwitch<Iloperator>() {

			@Override
			public Iloperator caseOpPlus(OpPlus opPlus) {
				return Iloperator.PLUS;
			}

			@Override
			public Iloperator caseOpMinus(OpMinus opMinus) {
				return Iloperator.MINUS;
			}
			
		}.doSwitch(op);
	}

	protected List<ILStatement> translateFunctionCall(ILvar resultVar, FuncDef func, List<Expr> args, List<ILvar> locals) {
		List<ILStatement> result = new NotNullList<ILStatement>();
		ILvar[] argumentVars = new ILvar[args.size()];
		PscriptType[] argumentTypes = new PscriptType[args.size()];
		for (int i=0; i<argumentVars.length; i++) {
			Expr arg = args.get(i);
			argumentTypes[i] = attrManager.getAttValue(AttrExprType.class, arg);
			argumentVars[i] = getNewLocalVar(func.getName() + "_param" + i, argumentTypes[i], locals);
			
			result.addAll(translateExpr(arg, argumentVars[i], locals));
		}
		
		String name = prog.getFuncDefName(func);
		if (func instanceof NativeFunc) {
			result.add(new IlbuildinFunctionCall(resultVar, name, argumentVars));
		} else {
			result.add(new ILfunctionCall(resultVar, name, argumentTypes, argumentVars));
		}
		
		return result;
	}


	protected List<ILStatement> translateConstructorCall(ILvar resultVar,
			ClassDef classDef, EList<Expr> args, List<ILvar> locals) {
		List<ILStatement> result = new NotNullList<ILStatement>();
		ILvar[] argumentVars = new ILvar[args.size()];
		PscriptType[] argumentTypes = new PscriptType[args.size()];
		for (int i=0; i<argumentVars.length; i++) {
			Expr arg = args.get(i);
			argumentTypes[i] = attrManager.getAttValue(AttrExprType.class, arg);
			argumentVars[i] = getNewLocalVar("construct_param" + i, argumentTypes[i], locals);
			
			result.addAll(translateExpr(arg, argumentVars[i], locals));
		}
		
		// TODO search constructors, use right name		
		result.add(new ILfunctionCall(resultVar, "w__" + classDef.getName() + "_constructer", argumentTypes, argumentVars));
		return result;
	}
	
	protected ILvar getVar(VarDef decl, List<ILvar> locals) {
		for (ILvar v : locals) {
			if (v.getName().equals(decl.getName())) {
				return v;
			}
		}
		ILvar v = prog.getGlobalVarDef(decl);
		if (v != null) {
			return v;
		}
		throw new Error("Variable " + decl + " not found.");
	}

	protected List<ILvar> translateParams(EList<VarDef> parameters) {
		return Utils.map(parameters, new Function<VarDef, ILvar>() {

			@Override
			public ILvar apply(VarDef from) {
				PscriptType type = attrManager.getAttValue(AttrTypeExprType.class, from.getType()); 
				return new ILvar(from.getName(), type);
			}
		});
	}

}

/**
 * this class gives you the names for generated elements 
 */
class WurstNames {

	public String getDestructorFunctionName(ClassDef c) {
		return "w__" + c.getName() + "_destroy";
	}
	
}
