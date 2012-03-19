package de.peeeq.wurstscript.translation.imtojass;

import static de.peeeq.wurstscript.jassAst.JassAst.JassFunction;
import static de.peeeq.wurstscript.jassAst.JassAst.JassFunctions;
import static de.peeeq.wurstscript.jassAst.JassAst.JassNatives;
import static de.peeeq.wurstscript.jassAst.JassAst.JassProg;
import static de.peeeq.wurstscript.jassAst.JassAst.JassSimpleVars;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStatements;
import static de.peeeq.wurstscript.jassAst.JassAst.JassTypeDefs;
import static de.peeeq.wurstscript.jassAst.JassAst.JassVars;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassFunctions;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassSimpleVars;
import de.peeeq.wurstscript.jassAst.JassStatement;
import de.peeeq.wurstscript.jassAst.JassStatements;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.jassAst.JassVars;
import de.peeeq.wurstscript.jassIm.ImArrayType;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.utils.Pair;

public class ImToJassTranslator {

	private ImProg imProg;
	private Multimap<ImFunction, ImFunction> calledFunctions;
	private ImFunction mainFunc;
	private ImFunction confFunction;
	private JassProg prog;
	private JassVars globals;
	private JassFunctions functions;
	private Set<ImFunction> translatingFunctions = Sets.newHashSet();
	private Set<ImFunction> translatedFunctions = Sets.newHashSet();
	private Set<String> usedNames = Sets.newHashSet();

	public ImToJassTranslator(ImProg imProg, Multimap<ImFunction, ImFunction> calledFunctions, 
			ImFunction mainFunc, ImFunction confFunction) {
		this.imProg = imProg;
		this.calledFunctions = calledFunctions;
		this.mainFunc = mainFunc;
		this.confFunction = confFunction;
	}
	
	public JassProg translate() {
		globals = JassVars();
		functions = JassFunctions();
		prog = JassProg(JassTypeDefs(), globals, JassNatives(), functions);
		
		translateFunctionTransitive(mainFunc);
		translateFunctionTransitive(confFunction);
		
		return prog;
	}

	private void translateFunctionTransitive(ImFunction imFunc) {
		if (translatedFunctions.contains(imFunc)) {
			// already translated
			return;
		}
		if (translatingFunctions.contains(imFunc)) {
			throw new Error("cyclic dependency between functions ");
		}
		translatingFunctions.add(imFunc);
		for (ImFunction f : calledFunctions.get(imFunc)) {
			translateFunctionTransitive(f);
		}
		
		translateFunction(imFunc);
		
		// translation finished
		translatedFunctions.add(imFunc);
	}

	private void translateFunction(ImFunction imFunc) {
		String name = getUniqueName(imFunc.getName());
		JassSimpleVars params = JassSimpleVars(); // TODO params
		String returnType = imFunc.getReturnType().translateTypeFirst();
		JassVars locals = JassVars();
		JassStatements body = JassStatements();
		JassFunction f = JassFunction(name, params, returnType, locals, body);
		
		imFunc.getBody().translate(f.getBody(), f, this);
	}



	private String getUniqueName(String name) {
		if (!usedNames.contains(name)) {
			usedNames.add(name);
			return name;
		}
		String name2;
		int i = 1;
		do {
			i++;
			name2 = name + "_" + i;
		} while (usedNames.contains(name2));
		usedNames.add(name2);
		return name2;
	}

	Map<Pair<String, Integer>, JassVar> tempReturnVars = Maps.newHashMap();
	
	public JassVar getTempReturnVar(String type, int nr) {
		Pair<String, Integer> key = Pair.create(type, nr);
		JassVar v = tempReturnVars.get(key);
		if (v == null) {
			v = JassAst.JassSimpleVar(type, getUniqueName("temp_return_"+type+"_"+nr));
			globals.add(v);
			tempReturnVars.put(key, v);
		}
		return v;
	}

	Map<ImVar, List<JassVar>> jassVars = Maps.newHashMap();
	
	public List<JassVar> getJassVarsFor(ImVar v) {
		List<JassVar> vars = jassVars.get(v);
		if (vars == null) {
			if (v.getType() instanceof ImArrayType) {
				
			} else if (v.getType() instanceof ImTupleType) {
				
			}
			vars = Lists.newArrayList();
			for (String type : v.getType().translateType()) {
				
			}
		}
		return vars ;
	}

	
}