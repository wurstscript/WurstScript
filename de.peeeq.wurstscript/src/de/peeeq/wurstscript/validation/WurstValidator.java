package de.peeeq.wurstscript.validation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithTypeArgs;
import de.peeeq.wurstscript.ast.AstElementWithTypeParameters;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberArrayVar;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FuncRef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.FunctionImplementation;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.HasModifier;
import de.peeeq.wurstscript.ast.HasTypeArgs;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.ModAbstract;
import de.peeeq.wurstscript.ast.ModConstant;
import de.peeeq.wurstscript.ast.ModOverride;
import de.peeeq.wurstscript.ast.ModStatic;
import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.ast.Modifiers;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.StmtDestroy;
import de.peeeq.wurstscript.ast.StmtIf;
import de.peeeq.wurstscript.ast.StmtReturn;
import de.peeeq.wurstscript.ast.StmtSet;
import de.peeeq.wurstscript.ast.StmtWhile;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.VisibilityModifier;
import de.peeeq.wurstscript.ast.VisibilityPrivate;
import de.peeeq.wurstscript.ast.VisibilityProtected;
import de.peeeq.wurstscript.ast.VisibilityPublic;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WParameters;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.attributes.CheckHelper;
import de.peeeq.wurstscript.attributes.attr;
import de.peeeq.wurstscript.gui.ProgressHelper;
import de.peeeq.wurstscript.types.PScriptTypeBool;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PScriptTypeReal;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.types.PscriptTypeModule;
import de.peeeq.wurstscript.types.PscriptTypeNamedScope;
import de.peeeq.wurstscript.types.PscriptTypeTypeParam;
import de.peeeq.wurstscript.utils.Utils;

/**
 * this class validates a pscript program
 * 
 * it has visit methods for different elements in the AST and checks whether
 * these are correct
 * 
 * the validation phase might not find all errors, code transformation and
 * optimization phases might detect other errors because they do a more
 * sophisticated analysis of the program
 * 
 * also note that many cases are already caught by the calculation of the
 * attributes
 * 
 */
public class WurstValidator {

	private CompilationUnit prog;
	private int functionCount;
	private int visitedFunctions;
	private List<Method> checkMethods = Lists.newArrayList();
	private HashMultimap<Class<?>, Method> typeToMethod = HashMultimap.create();
	private Set<Class<?>> knownTypes = Sets.newHashSet();
	
	public WurstValidator(CompilationUnit prog) {
		this.prog = prog;
	}

	public void validate() {
		functionCount = countFunctions();
		visitedFunctions = 0;

//		prog.accept(this);
		// TODO reflection mechanism
		
		
		for (Method m : WurstValidator.class.getMethods()) {
			if (m.getAnnotation(CheckMethod.class) != null) {
				// this is a checkmethod
				checkMethods.add(m);
				if (m.getParameterTypes().length != 1) {
					throw new Error("check method must have exactly one parameter: " + m);
				}
			}
		}
		
		walkTree(prog);		
	}

	private void walkTree(AstElement e) {
		check(e);
		for (int i=0; i<e.size(); i++) {
			walkTree(e.get(i));
		}
	}

	private void check(AstElement e) {
		if (!knownTypes.contains(e)) {
			for (Method m : checkMethods) {
				if (m.getParameterTypes()[0].isInstance(e)) {
					typeToMethod.put(e.getClass(), m);
				}
			}
		}
		for (Method m : typeToMethod.get(e.getClass())) {
			try {
				m.invoke(this, e);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	private int countFunctions() {
		final int functionCount[] = new int[1];
		prog.accept(new CompilationUnit.DefaultVisitor() {
			@CheckMethod
			public void visit(FuncDef f) {
				functionCount[0]++;
			}
		});
		return functionCount[0];
	}

	@CheckMethod
	public void visit(StmtSet s) {

		PscriptType leftType = s.getUpdatedExpr().attrTyp();
		PscriptType rightType = s.getRight().attrTyp();

		checkAssignment(Utils.isJassCode(s), s.getSource(), leftType, rightType);
		
		checkIfAssigningToConstant(s.getUpdatedExpr());
		
		

	}

	private void checkIfAssigningToConstant(final NameRef left) {
		left.match(new NameRef.MatcherVoid() {
			
			@Override
			public void case_ExprVarArrayAccess(ExprVarArrayAccess e) {
				
			}
			
			@Override
			public void case_ExprVarAccess(ExprVarAccess e) {
				checkVar(e.attrNameDef());
			}
			
			private void checkVar(NameDef var) {
				if (var instanceof GlobalVarDef) {
					GlobalVarDef g = (GlobalVarDef) var;
					if (g.attrIsConstant()) {
						attr.addError(left.getSource(), "Cannot assign a new value to constant variable " + g.getName());
					}
				}
			}

			@Override
			public void case_ExprMemberVar(ExprMemberVar e) {
				checkVar(e.attrNameDef());
			}
			
			@Override
			public void case_ExprMemberArrayVar(ExprMemberArrayVar e) {
				
			}
		});
	}

	private void checkAssignment(boolean isJassCode, WPos pos, PscriptType leftType, PscriptType rightType) {
		if (!rightType.isSubtypeOf(leftType)) {
			if (isJassCode) {
				if (leftType instanceof PScriptTypeReal && rightType instanceof PScriptTypeInt) {
					// special case: jass allows to assign an integer to a real
					// variable
					return;
				}
			}
			attr.addError(pos, "Cannot assign " + rightType + " to " + leftType);
		}
		if (leftType instanceof PscriptTypeNamedScope) {
			PscriptTypeNamedScope ns = (PscriptTypeNamedScope) leftType;
			if (ns.isStaticRef()) {
				attr.addError(pos, "Missing variable name in variable declaration.");
			}
		}
	}

	@CheckMethod
	public void visit(LocalVarDef s) {
		checkVarName(s, false);
		if (s.getInitialExpr() instanceof Expr) {
			Expr initial = (Expr) s.getInitialExpr();
			PscriptType leftType = s.attrTyp();
			PscriptType rightType = initial.attrTyp();

			checkAssignment(Utils.isJassCode(s), s.getSource(), leftType, rightType);
		}
	}

	private void checkVarName(VarDef s, boolean isConstant) {
		String varName = s.getName(); 
		if (!Utils.isJassCode(s)) {
			if (!Character.isLowerCase(varName.charAt(0))) {
				if (!varName.matches("[A-Z0-9_]+")) {
					attr.addError(s.getSource(), "Variable names must start with a lower case character. (" + varName + ")" );
				}
			}
		}
	}
	
	@CheckMethod
	public void visit(WParameter wParameter) {
		checkVarName(wParameter, false);
	}

	@CheckMethod
	public void visit(GlobalVarDef s) {
		checkVarName(s, s.attrIsConstant());
		if (s.getInitialExpr() instanceof Expr) {
			Expr initial = (Expr) s.getInitialExpr();
			PscriptType leftType = s.attrTyp();
			PscriptType rightType = initial.attrTyp();
			checkAssignment(Utils.isJassCode(s), s.getSource(), leftType, rightType);
		}
	}

	@CheckMethod
	public void visit(StmtIf stmtIf) {
		PscriptType condType = stmtIf.getCond().attrTyp();
		if (!(condType instanceof PScriptTypeBool)) {
			attr.addError(stmtIf.getCond().getSource(), "If condition must be a boolean but found " + condType);
		}
	}

	@CheckMethod
	public void visit(StmtWhile stmtWhile) {
		PscriptType condType = stmtWhile.getCond().attrTyp();
		if (!(condType instanceof PScriptTypeBool)) {
			attr.addError(stmtWhile.getCond().getSource(), "While condition must be a boolean but found " + condType);
		}
	}

	@CheckMethod
	public void visit(ExtensionFuncDef func) {
		checkFunctionName(func);
		
		
		checkReturn(func);
		UninitializedVars.checkFunc(func.attrDefinedNames().values(), func.getBody());
	}

	private void checkFunctionName(FunctionDefinition f) {
		if (!Utils.isJassCode(f)) {
			if (Character.isUpperCase(f.getName().charAt(0))) {
				attr.addError(f.getSource(), "Function names must start with an lower case character.");
			}
		}
	}

	

	

	

	private void checkReturn(FunctionImplementation func) {
		String functionName = func.getName();
		if (func.getReturnTyp() instanceof TypeExpr) {
			if (!func.getBody().attrDoesReturn()) {
				attr.addError(func.getSource(), "Function " + functionName + " is missing a return statement.");
			}
		}
	}

	@CheckMethod
	public void visit(FuncDef func) {
		visitedFunctions++;
		attr.setProgress(null, ProgressHelper.getValidatorPercent(visitedFunctions, functionCount));

		checkFunctionName(func);
		
		String functionName = func.getName();
		if (func.attrIsAbstract()) {
			if (func.getBody().size() > 0) {
				attr.addError(func.getBody().get(0).getSource(), "The abstract function " + functionName
						+ " must not have any statements.");
			}
		} else { // not abstract
			checkReturn(func);
			UninitializedVars.checkFunc(func.attrDefinedNames().values(), func.getBody());
		}

		// check is override is correct:
		for (FunctionDefinition overriddenFunc : func.attrOverriddenFunctions()) {
			CheckHelper.checkIfIsRefinement(func, overriddenFunc);
		}

	}
	
	@CheckMethod
	public void visit(InitBlock initBlock) {
		UninitializedVars.checkFunc(initBlock.attrDefinedNames().values(), initBlock.getBody());
	}
	
	@CheckMethod
	public void visit(OnDestroyDef onDestroyDef) {
		UninitializedVars.checkFunc(onDestroyDef.attrDefinedNames().values(), onDestroyDef.getBody());
	}
	
	@CheckMethod
	public void visit(ConstructorDef constructorDef) {
		UninitializedVars.checkFunc(constructorDef.attrDefinedNames().values(), constructorDef.getBody());
	}

	@CheckMethod
	public void visit(ExprFunctionCall stmtCall) {
		String funcName = stmtCall.getFuncName();
		// calculating the exprType should reveal most errors:
		stmtCall.attrTyp();
		
		List<Expr> args = Lists.newArrayList();
		if (stmtCall.attrImplicitParameter() instanceof Expr) {
			args.add((Expr) stmtCall.attrImplicitParameter());
		}
		args.addAll(stmtCall.getArgs());
		checkParams(stmtCall, args, stmtCall.attrFuncDef());
		
		FunctionImplementation nearestFunc = stmtCall.attrNearestFuncDef();
		if (stmtCall.attrFuncDef() != null) {
			
			FunctionDefinition calledFunc = stmtCall.attrFuncDef();
			if (calledFunc.attrIsDynamicClassMember()) {
				if (!stmtCall.attrIsDynamicContext()) {
							attr.addError(stmtCall.getSource(), "Cannot call dynamic function " + funcName  +
									" from static function " + nearestFunc.getName());
				}
			}
		}
		
		// special check for filter & condition:
		if (Utils.oneOf(funcName, "Condition", "Filter")) {
			Expr firstArg = stmtCall.getArgs().get(0);
			if (firstArg instanceof ExprFuncRef) {
				ExprFuncRef exprFuncRef = (ExprFuncRef) firstArg;
				FunctionDefinition f = exprFuncRef.attrFuncDef();
				if (f != null) {
					if (f.getParameters().size() > 0) {
						attr.addError(firstArg.getSource(), "Functions passed to Filter or Condition must have no parameters.");
					}
					if (!(f.getReturnTyp().attrTyp() instanceof PScriptTypeBool)) {
						attr.addError(firstArg.getSource(), "Functions passed to Filter or Condition must return boolean.");
					}
				}
			}
		}
	}

	private void checkParams(AstElement where, List<Expr> args, FunctionDefinition calledFunc) {
		if (calledFunc == null) {
			return;
		}
		List<PscriptType> parameterTypes = calledFunc.attrParameterTypes();
		if (args.size() > parameterTypes.size()) {
			attr.addError(where.attrSource(), "Too many parameters.");
			
		} else if (args.size() < parameterTypes.size()) {
			attr.addError(where.attrSource(), "Missing parameters.");
		} else {
			for (int i=0; i<args.size(); i++) {
				PscriptType actual = args.get(i).attrTyp();
				PscriptType expected = parameterTypes.get(i);
				if (expected instanceof AstElementWithTypeArgs)
				if (!actual.isSubtypeOf(expected)) {
					attr.addError(args.get(i).getSource(), "Expected " + expected + " as parameter " + i + " but found " + actual);
				}
			}
		}
		
	}

	@CheckMethod
	public void visit(ExprMemberMethod stmtCall) {
		// calculating the exprType should reveal all errors:
		stmtCall.attrTyp();
		
		List<Expr> args = Lists.newLinkedList();
		if (stmtCall.attrImplicitParameter() instanceof Expr) {
			args.add((Expr) stmtCall.attrImplicitParameter());
		}
		args.addAll(stmtCall.getArgs());
		checkParams(stmtCall, args, stmtCall.attrFuncDef());
	}

	@CheckMethod
	public void visit(ExprNewObject stmtCall) {
		stmtCall.attrTyp();
		stmtCall.attrConstructorDef();
	}

	@CheckMethod
	public void visit(Modifiers modifiers) {
		boolean hasVis = false;
		boolean isStatic = false;
		for (Modifier m : modifiers) {
			if (m instanceof VisibilityModifier) {
				if (hasVis) {
					attr.addError(m.getSource(), "Each element can only have one visibility modfifier (public, private, ...)");
				}
				hasVis = true;
			} else if (m instanceof ModStatic) {
				if (isStatic) {
					attr.addError(m.getSource(), "double static? - what r u trying to do?");
				}
				isStatic = true;
			}
		}
	}

	@CheckMethod
	public void visit(StmtReturn s) {
		FunctionImplementation func = s.attrNearestFuncDef();
		if (func == null) {
			attr.addError(s.getSource(), "return statements can only be used inside functions");
			return;
		}
		PscriptType returnType = func.getReturnTyp().attrTyp();
		if (s.getReturnedObj() instanceof Expr) {
			Expr returned = (Expr) s.getReturnedObj();
			if (returnType.isSubtypeOf(PScriptTypeVoid.instance())) {
				attr.addError(s.getSource(), "Cannot return a value from a function which returns nothing");
			} else {
				PscriptType returnedType = returned.attrTyp();
				if (!returnedType.isSubtypeOf(returnType)) {
					attr.addError(s.getSource(), "Cannot return " + returnedType + ", expected expression of type "
							+ returnType);
				}
			}
		} else { // empty return
			if (!returnType.isSubtypeOf(PScriptTypeVoid.instance())) {
				attr.addError(s.getSource(), "Missing return value");
			}
		}
	}

	@CheckMethod
	public void visit(ClassDef classDef) {
		checkTypeName(classDef.getSource(), classDef.getName());
		
		// calculate all functions to find possible errors
		Map<String, FuncDef> functions = classDef.attrAllFunctions();
		
		// check that there are no abstract functions in a class
		for (FunctionDefinition f : functions.values()) {
			if (f.attrIsAbstract()) {
				attr.addError(classDef.getSource(), "The abstract method " + f.getName()
						+ " must be implemented in class " + classDef.getName() + ".");
			}
		}
	}

	private void checkTypeName(WPos source, String name) {
		if (!Character.isUpperCase(name.charAt(0))) {
			attr.addError(source, "Type names must start with upper case characters.");
		}
	}

	@CheckMethod
	public void visit(ModuleDef moduleDef) {
		checkTypeName(moduleDef.getSource(), moduleDef.getName());
		// calculate all functions to find possible errors
		moduleDef.attrAllFunctions();
	}
	

	@CheckMethod
	public void visit(StmtDestroy stmtDestroy) {
		PscriptType typ = stmtDestroy.getDestroyedObj().attrTyp();
		if (!(typ instanceof PscriptTypeModule || typ instanceof PscriptTypeClass)) {
			attr.addError(stmtDestroy.getSource(), "Cannot destroy objects of type " + typ);
		}
	}
	
	@CheckMethod 
	public void visit(ExprVarAccess e) {
		checkVarRef(e, e.attrIsDynamicContext());
	}

	
	
	@CheckMethod
	public void visit(WImport wImport) {
		if (wImport.attrImportedPackage() == null) {
			attr.addError(wImport.getSource(), "Could not find imported package " + wImport.getPackagename());
		}
	}

	/**
	 * check if the nameRef e is accessed correctly
	 * i.e. not using a dynamic variable from a static context
	 * @param e
	 * @param dynamicContext
	 */
	private void checkVarRef(NameRef e, boolean dynamicContext) {
		NameDef def = e.attrNameDef();
		if (def instanceof GlobalVarDef) {
			GlobalVarDef g = (GlobalVarDef) def;
			if (g.attrIsDynamicClassMember() && !dynamicContext) {
				attr.addError(e.getSource(), "Cannot reference dynamic variable " +e.getVarName() + " from static context.");
			}
		}
	}
	
	@CheckMethod
	public void checkTypeBinding(HasTypeArgs e) {
		for (Entry<TypeParamDef, PscriptType> t : e.attrTypeParameterBindings().entrySet()) {
			PscriptType typ = t.getValue();
			if (!(typ instanceof PScriptTypeInt)
					&& !(typ instanceof PscriptTypeNamedScope)
					&& !(typ instanceof PscriptTypeTypeParam)) {
				attr.addError(e.getSource(), "Type parameters can only be bound to ints and class types, but " +
					"not to " + typ);
			}
		}
	}
	
	@CheckMethod
	public void checkFuncRef(FuncRef ref) {
		ref.attrFuncDef();
	}
	
	@CheckMethod
	public void checkModifiers(final HasModifier e) {
		for (final Modifier m : e.getModifiers()) {
			final StringBuilder error = new StringBuilder();
			
			e.match(new HasModifier.MatcherVoid() {
				
				@Override
				public void case_WParameter(WParameter wParameter) {
					error.append("Parameters must not have modifiers");
				}
				
				@Override
				public void case_TypeParamDef(TypeParamDef typeParamDef) {
					error.append("Type Parameters must not have modifiers");
				}
				
				@Override
				public void case_NativeType(NativeType nativeType) {
					check(VisibilityPublic.class);
				}
				
				private void check(Class<? extends Modifier> ...allowed) {
					boolean isAllowed = false;
					for (Class<? extends Modifier> a : allowed) {
						if (a.isInstance(m)) {
							isAllowed  = true;
							break;
						}
					}
					if (!isAllowed) {
						error.append("Modifier " + printMod(m) + " not allowed for " +
								Utils.printElement(e) + ".\n Allowed are the following" +
								" modifiers: ");
						boolean first = true;
						for (Class<? extends Modifier> c : allowed) {
							if (!first) {
								error.append(", ");
							}
							error.append(printMod(c));
							first = false;
						}
					}
				}

				@Override
				public void case_NativeFunc(NativeFunc nativeFunc) {
					check(VisibilityPublic.class);
				}
				
				@Override
				public void case_ModuleInstanciation(ModuleInstanciation moduleInstanciation) {
					check(VisibilityPrivate.class, VisibilityProtected.class);
				}
				
				@Override
				public void case_ModuleDef(ModuleDef moduleDef) {
					check(VisibilityPublic.class);
				}
				
				@Override
				public void case_LocalVarDef(LocalVarDef localVarDef) {
					check(ModConstant.class);
				}
				
				@Override
				public void case_GlobalVarDef(GlobalVarDef g) {
					if (g.attrNearestClassOrModule() != null) {
						check(VisibilityPrivate.class, VisibilityProtected.class,
								ModStatic.class, ModConstant.class);
					} else {
						check(VisibilityPublic.class, ModConstant.class);
					}
				}
				
				@Override
				public void case_FuncDef(FuncDef f) {
					if (f.attrNearestClassOrModule() != null) {
						check(VisibilityPrivate.class, VisibilityProtected.class,
								ModAbstract.class, ModOverride.class, ModStatic.class);
					} else {
						check(VisibilityPublic.class);
					}
				}
				
				@Override
				public void case_ExtensionFuncDef(ExtensionFuncDef extensionFuncDef) {
					check(VisibilityPublic.class);
				}
				
				@Override
				public void case_ConstructorDef(ConstructorDef constructorDef) {
					check(VisibilityPrivate.class);
				}
				
				@Override
				public void case_ClassDef(ClassDef classDef) {
					check(VisibilityPublic.class);
				}
			});
			if (error.length() > 0) {
				attr.addError(e.getSource(), error.toString());
			}
		}
	}

	protected String printMod(Class<? extends Modifier> c) {
		String name = c.getSimpleName().toLowerCase();
		name = name.replaceAll("^(mod|visibility)", "");
		name = name.replaceAll("impl$", "");
		return name;
	}

	protected String printMod(Modifier m) {
		return printMod(m.getClass());
	}
}
