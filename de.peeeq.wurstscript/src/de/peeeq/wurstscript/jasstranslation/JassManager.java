package de.peeeq.wurstscript.jasstranslation;

import java.util.Map;
import java.util.Set;

import org.testng.collections.Maps;

import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.types.PScriptTypeArray;

/**
 * manages mapping from wurstscript elements to jass elements 
 *
 */
public class JassManager {

	private Map<FuncDef, JassFunction> functions = Maps.newHashMap();
	private Map<VarDef, JassVar> variables = Maps.newHashMap();
	private Map<AstElement, String> names = Maps.newHashMap();
	private Set<String> givenNames = Sets.newHashSet();
	
	public JassFunction getJassFunctionFor(FunctionDefinition f) {
		if (functions.containsKey(f)) {
			return functions.get(f);
		}
		String name = f.getSignature().getName();
		if (f.attrNearestClassDef() != null) {
			name = f.attrNearestClassDef() + "_" + name;
		}
		if (f.attrNearestPackage() != null) {
			name = f.attrNearestPackage() + "_" + name;
		}
		name = getUniqueName(f, name);
		JassFunction func = JassAst.JassFunction(name, JassAst.JassSimpleVars(), "", JassAst.JassVars(), JassAst.JassStatements());
		return func;
	}
	
	public JassVar getJassVarFor(VarDef f) {
		if (variables.containsKey(f)) {
			return variables.get(f);
		}
		String name = f.getName();
		if (f.attrNearestClassDef() != null) {
			name = f.attrNearestClassDef() + "_" + name;
		}
		if (f.attrNearestPackage() != null) {
			name = f.attrNearestPackage() + "_" + name;
		}
		name = getUniqueName(f, name);
		JassVar var = JassTranslator.translateVarDef(f);
		return var;
	}
	

	private String getUniqueName(AstElement element, String baseName) {
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

	

}
