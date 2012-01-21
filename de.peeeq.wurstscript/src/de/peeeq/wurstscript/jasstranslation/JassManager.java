package de.peeeq.wurstscript.jasstranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.InstanceDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassSimpleVar;
import de.peeeq.wurstscript.jassAst.JassVar;

/**
 * manages mapping from wurstscript elements to jass elements
 * 
 */
public class JassManager {
	private Map<FunctionDefinition, JassFunction> functions = Maps.newHashMap();
	private Map<JassFunction, AstElement> functionSources = Maps.newHashMap();
	private Map<InitBlock, JassFunction> initFunctions = Maps.newHashMap();
	private Map<ClassDef, JassFunction> destroyFunctions = Maps.newHashMap();
	private Map<ConstructorDef, JassFunction> constructorFunctions = Maps.newHashMap();
	private Map<VarDef, List<JassVar>> variables = Maps.newHashMap();
	private Map<VarDef, String> variableNames = Maps.newHashMap();
	private Map<AstElement, String> names = Maps.newHashMap();
	private Set<String> givenNames = Sets.newHashSet();
	private JassTranslator jassTranslator;

	public JassManager(JassTranslator jassTranslator) {
		this.jassTranslator = jassTranslator;
	}

	// public String getUniqueName(AstElement element, String baseName) {
	// if (baseName.contains("(")) throw new Error();
	//
	// String name = names.get(element);
	// if (name != null) {
	// return name;
	// }
	// name = baseName;
	// int i = 0;
	// while (givenNames.contains(name)) {
	// name = baseName + ++i;
	// }
	// givenNames.add(name);
	// names.put(element, name);
	// return name;
	// }

	void markNameAsUsed(String name) {
		givenNames.add(name);
	}

	public String getUniqueName(String baseName) {
		// OPTIMIZE performance
		String name = baseName;
		int i = 0;
		while (givenNames.contains(name)) {
			name = baseName + ++i;
		}
		givenNames.add(name);
		return name;
	}

	public JassFunction getJassFunctionFor(FunctionDefinition f) {
		Preconditions.checkNotNull(f);
		if (functions.containsKey(f)) {
			return functions.get(f);
		}
		String name = f.getName();
		if (f instanceof NativeFunc) {
			// do not change name
			markNameAsUsed(name);
		} else {
			// for normal functions change the name according to class and
			// package
			name = addContext(f, name);
			name = getUniqueName(name);
		}
		JassFunction func = JassAst.JassFunction(name, JassAst.JassSimpleVars(), "nothing", JassAst.JassVars(),
				JassAst.JassStatements());
		functions.put(f, func);
		functionSources.put(func, f);
		return func;
	}

	private String addContext(AstElement node, String name) {
		node = node.getParent();
		while (node != null) {
			if (node instanceof NameDef) {
				NameDef n = (NameDef) node;
				name = n.getName() + "_" + name;
			}
			node = node.getParent();
		}
		return name;
	}

//	public JassVar getJassVarFor(VarDef v, int index, String type, boolean isArray) {
//		return getJassVarFor(v, index, type, isArray, false);
//	}
//
//	public JassVar getJassVarFor(VarDef v, int index, String type, boolean isArray, boolean isLocal) {
//		List<JassVar> list;
//		if (variables.containsKey(v)) {
//			list = variables.get(v);
//		} else {
//			list = new ArrayList<JassVar>();
//			variables.put(v, list);
//		}
//		JassVar var = null;
//		if (index >= list.size()) {
//			// create new var
//			for (int i=list.size()-1; i <= index; i++) {
//				String name = getJassVarNameFor(v, isLocal);
//				if (i > 0) {
//					name += i;
//				}
//				
//				if (isArray) {
//					var = JassAst.JassArrayVar(type, name);
//				} else {
//					var = JassAst.JassSimpleVar(type, name);
//				}
//				list.add(var);
//			}
//		} else {
//			var = list.get(index);
//			if (var instanceof JassSimpleVar == isArray) {
//				throw new Error("inconsistent isArray");
//			}
//			if (!var.getType().equals(type)) {
//				throw new Error("inconsistent type");
//			}
//		}
//		return var;
//	}
//
//	public JassVar getJassVarForTranslatedVar(VarDef varDef) {
//		return getJassVarForTranslatedVar(varDef, false);
//	}
//
//	public JassVar getJassVarForTranslatedVar(VarDef varDef, boolean isLocal) {
//		if (variables.containsKey(varDef)) {
//			return variables.get(varDef).get(0);
//		} else {
//			throw new Error("Variable " + getJassVarNameFor(varDef, isLocal) + " has not been translated.");
//		}
//	}
	
	
	public List<JassVar> getJassVarsFor(VarDef v) {
		if (variables.containsKey(v)) {
			return variables.get(v);
		}
		List<JassVar> jassVars = jassTranslator.createVarsForDef(v, getJassVarNameFor(v));
		variables.put(v, jassVars);
		return jassVars;
	}

	public String getJassVarNameFor(VarDef v) {
		return getJassVarNameFor(v, false);
	}

	public String getJassVarNameFor(VarDef v, boolean isLocal) {
		String name;
		if (variableNames.containsKey(v)) {
			name = variableNames.get(v);
		} else {
			name = v.getName();
			if (!isLocal) {
				// non local variables get some name extensions
				name = addContext(v, name);
				name = getUniqueName(name);
			} else {
				// TODO local variables do not need a globally unique name
				name = getUniqueName(name);
			}
			variableNames.put(v, name);
		}
		return name;
	}

	public JassFunction getJassDestroyFunctionFor(ClassDef c) {
		if (destroyFunctions.containsKey(c)) {
			return destroyFunctions.get(c);
		}
		String name = c.getName() + "_destroy";
		if (c.attrNearestPackage() instanceof WPackage) {
			name = ((WPackage) c.attrNearestPackage()).getName() + "_" + name;
		}
		name = getUniqueName(name);
		JassFunction func = JassAst.JassFunction(name, JassAst.JassSimpleVars(JassAst.JassSimpleVar("integer", "this")),
				"nothing", JassAst.JassVars(), JassAst.JassStatements());
		destroyFunctions.put(c, func);
		functionSources.put(func, c);
		return func;
	}

	public JassFunction getJassConstructorFor(ConstructorDef f) {
		if (constructorFunctions.containsKey(f)) {
			return constructorFunctions.get(f);
		}
		String name = "construct";
		if (f.attrNearestClassDef() != null) {
			name = f.attrNearestClassDef().getName() + "_" + name;
		}
		if (f.attrNearestPackage() instanceof WPackage) {
			name = ((WPackage) f.attrNearestPackage()).getName() + "_" + name;
		}
		name = getUniqueName(name);
		JassFunction func = JassAst.JassFunction(name, JassAst.JassSimpleVars(), "nothing", JassAst.JassVars(),
				JassAst.JassStatements());
		constructorFunctions.put(f, func);
		functionSources.put(func, f);
		return func;
	}

	public JassFunction getJassInitFunctionFor(InitBlock f) {
		if (initFunctions.containsKey(f)) {
			return initFunctions.get(f);
		}
		String name = "init";
		if (f.attrNearestPackage() instanceof WPackage) {
			name = ((WPackage) f.attrNearestPackage()).getName() + "_" + name;
		}
		name = getUniqueName(name);
		JassFunction func = JassAst.JassFunction(name, JassAst.JassSimpleVars(), "nothing", JassAst.JassVars(),
				JassAst.JassStatements());
		initFunctions.put(f, func);
		functionSources.put(func, f);
		return func;
	}

	public AstElement getFunctionSource(JassFunction f) {
		return functionSources.get(f);
	}

	private Map<String, JassVar> returnVars = Maps.newHashMap();

	public JassVar getTempReturnVar(String returnTyp, JassProg prog) {
		if (returnVars.containsKey(returnTyp)) {
			return returnVars.get(returnTyp);
		}
		String name = getUniqueName("temp_" + returnTyp);
		JassVar v = JassAst.JassSimpleVar(returnTyp, name);
		prog.getGlobals().add(v);
		returnVars.put(returnTyp, v);
		return v;
	}

	private Map<InstanceDef, Integer> typeIds = Maps.newHashMap();
	private int typeIdCounter = 0;
	
	public int getTypeId(InstanceDef o1) {
		if (!typeIds.containsKey(o1)) {
			typeIdCounter++;
			typeIds.put(o1, typeIdCounter);
			return typeIdCounter;
		}
		return typeIds.get(o1);
	}

}
