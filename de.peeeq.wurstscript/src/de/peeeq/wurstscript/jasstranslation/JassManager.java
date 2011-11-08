package de.peeeq.wurstscript.jasstranslation;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.testng.collections.Maps;

import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassSimpleVar;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.types.PScriptTypeArray;

/**
 * manages mapping from wurstscript elements to jass elements 
 *
 */
public class JassManager {
	private Map<FunctionDefinition, JassFunction> functions = Maps.newHashMap();
	private Map<InitBlock, JassFunction> initFunctions = Maps.newHashMap();
	private Map<ClassDef, JassFunction> destroyFunctions = Maps.newHashMap();
	private Map<ConstructorDef, JassFunction> constructorFunctions = Maps.newHashMap();
	private Map<VarDef, JassVar> variables = Maps.newHashMap();
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
	
	
	public String getUniqueName(String baseName) {
		String name = baseName;
		int i = 0;
		while (givenNames.contains(name)) {
			name = baseName + ++i;
		}
		givenNames.add(name);
		return name;
	}
	
	public JassFunction getJassFunctionFor(FunctionDefinition f) {
		if (functions.containsKey(f)) {
			return functions.get(f);
		}
		String name = f.getSignature().getName();
		if (f.attrNearestClassDef() != null) {
			name = f.attrNearestClassDef().getName() + "_" + name;
		}
		if (f.attrNearestPackage() instanceof WPackage) {
			name = ((WPackage) f.attrNearestPackage()).getName() + "_" + name;
		}
		name = getUniqueName(f, name);
		JassFunction func = JassAst.JassFunction(name, JassAst.JassSimpleVars(), "", JassAst.JassVars(), JassAst.JassStatements());
		functions.put(f, func);
		return func;
	}
	
	public JassVar getJassVarFor(VarDef v) {
		if (variables.containsKey(v)) {
			return variables.get(v);
		}
		String name = v.getName();
		if (v.attrNearestClassDef() != null) {
			name = v.attrNearestClassDef().getName() + "_" + name;
		}
		if (v.attrNearestPackage() instanceof WPackage) {
			name = ((WPackage) v.attrNearestPackage()).getName() + "_" + name;
		}
		name = getUniqueName(v, name);
		JassVar var = jassTranslator.translateVarDef(v);
		variables.put(v, var);
		return var;
	}
	

	

	public JassFunction getJassDestroyFunctionFor(ClassDef c) {
		if (destroyFunctions.containsKey(c)) {
			return destroyFunctions.get(c);
		}
		String name = c.getName() + "_destroy";
		if (c.attrNearestPackage() instanceof WPackage) {
			name = ((ClassDef) c.attrNearestPackage()).getName() + "_" + name;
		}
		name = getUniqueName(c, name);
		JassFunction func = JassAst.JassFunction(name, JassAst.JassSimpleVars(), "", JassAst.JassVars(), JassAst.JassStatements());
		destroyFunctions.put(c, func);
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
		JassFunction func = JassAst.JassFunction(name, JassAst.JassSimpleVars(), "", JassAst.JassVars(), JassAst.JassStatements());
		constructorFunctions.put(f, func);
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
		JassFunction func = JassAst.JassFunction(name, JassAst.JassSimpleVars(), "", JassAst.JassVars(), JassAst.JassStatements());
		initFunctions.put(f, func);
		return func;
	}


	


	

}
