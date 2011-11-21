package de.peeeq.wurstscript.jasstranslation;

import java.util.Map;
import java.util.Set;

import org.testng.collections.Maps;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.attributes.FuncDefInstance;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;

/**
 * manages mapping from wurstscript elements to jass elements 
 *
 */
public class JassManager {
	private Map<Pair<ImmutableList<ClassOrModule>, FunctionDefinition>, JassFunction> functions = Maps.newHashMap();
	private Map<JassFunction, AstElement> functionSources = Maps.newHashMap(); 
	private Map<InitBlock, JassFunction> initFunctions = Maps.newHashMap();
	private Map<ClassDef, JassFunction> destroyFunctions = Maps.newHashMap();
	private Map<ConstructorDef, JassFunction> constructorFunctions = Maps.newHashMap();
	private Map<Pair<ImmutableList<ClassOrModule>, VarDef>, JassVar> variables = Maps.newHashMap();
	private Map<AstElement, String> names = Maps.newHashMap();
	private Set<String> givenNames = Sets.newHashSet();
	private JassTranslator jassTranslator;
	
	public JassManager(JassTranslator jassTranslator) {
		this.jassTranslator = jassTranslator;
	}
	
	
	public String getUniqueName(AstElement element, String baseName) {
		if (baseName.contains("(")) throw new Error();
		
		String name = names.get(element);
		if (name != null) {
			return name;
		}
		name = baseName;
		int i = 0;
		while (givenNames.contains(name)) {
			name = baseName + ++i;
		}
		givenNames.add(name);
		names.put(element, name);
		return name;
	}
	
	private void markNameAsUsed(String name) {
		givenNames.add(name);
	}
	
	
	public String getUniqueName(String baseName) {
		String name = baseName;
		int i = 0;
		while (givenNames.contains(name)) {
			name = baseName + ++i;
		}
		givenNames.add(name);
		return name;
	}
	
	public JassFunction getJassFunctionFor(ImmutableList<ClassOrModule> context, FunctionDefinition f) {
		Pair<ImmutableList<ClassOrModule>, FunctionDefinition> key = Pair.create(context, f);
		if (functions.containsKey(key)) {
			return functions.get(key);
		}
		String name = f.getSignature().getName();
		if (f instanceof NativeFunc) {
			// do not change name
			markNameAsUsed(name);
		} else {
			// for normal functions change the name according to class and package
			if (context.size() > 0) {
				name = contextToString(context) + "_" + name;
			}
			if (f.attrNearestPackage() instanceof WPackage) {
				name = ((WPackage) f.attrNearestPackage()).getName() + "_" + name;
			}
			name = getUniqueName(f, name);
		}
		JassFunction func = JassAst.JassFunction(name, JassAst.JassSimpleVars(), "nothing", JassAst.JassVars(), JassAst.JassStatements());
		functions.put(key, func);
		functionSources.put(func, f);
		return func;
	}
	
	public JassVar getJassVarFor(ImmutableList<ClassOrModule> context, VarDef v) { // TODO add parameters for type, array, etc..
		Pair<ImmutableList<ClassOrModule>, VarDef> key = Pair.create(context, v);
		if (variables.containsKey(key)) {
			return variables.get(key);
		}
		String name = v.getName();
		if (context.size() > 0) {
			name = contextToString(context) + "_" + name;
		}
		if (v.attrNearestPackage() instanceof WPackage) {
			name = ((WPackage) v.attrNearestPackage()).getName() + "_" + name;
		}
		name = getUniqueName(v, name);
		JassVar var = jassTranslator.translateVarDef(v); // FIXME do not call jass translator
		variables.put(key, var);
		return var;
	}
	

	

	private String contextToString(ImmutableList<ClassOrModule> context) {
		return Utils.join(Utils.map(context, new Function<ClassOrModule, String>() {

			@Override
			public String apply(ClassOrModule input) {
				return input.getName();
			}
		}), "_");
	}


	public JassFunction getJassDestroyFunctionFor(ClassDef c) {
		if (destroyFunctions.containsKey(c)) {
			return destroyFunctions.get(c);
		}
		String name = c.getName() + "_destroy";
		if (c.attrNearestPackage() instanceof WPackage) {
			name = ((WPackage) c.attrNearestPackage()).getName() + "_" + name;
		}
		name = getUniqueName(c, name);
		JassFunction func = JassAst.JassFunction(name, JassAst.JassSimpleVars(JassAst.JassSimpleVar("integer", "this")), "nothing", JassAst.JassVars(), JassAst.JassStatements());
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
		name = getUniqueName(f, name);
		JassFunction func = JassAst.JassFunction(name, JassAst.JassSimpleVars(), "nothing", JassAst.JassVars(), JassAst.JassStatements());
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
		name = getUniqueName(f, name);
		JassFunction func = JassAst.JassFunction(name, JassAst.JassSimpleVars(), "nothing", JassAst.JassVars(), JassAst.JassStatements());
		initFunctions.put(f, func);
		functionSources.put(func, f);
		return func;
	}


	public AstElement getFunctionSource(JassFunction f) {
		return functionSources.get(f);
	}


	public JassFunction getJassFunctionFor(ImmutableList<ClassOrModule> context, FuncDefInstance calledFunc) {
		Preconditions.checkNotNull(context);
		// calculate the complete path:
		ImmutableList<ClassOrModule> consContext = context.cons(calledFunc.getContext());
		return getJassFunctionFor(consContext, calledFunc.getDef());
	}


	


	

}
