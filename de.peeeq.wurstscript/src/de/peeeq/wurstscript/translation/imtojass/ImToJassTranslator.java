package de.peeeq.wurstscript.translation.imtojass;

import static de.peeeq.wurstscript.jassAst.JassAst.JassFunction;
import static de.peeeq.wurstscript.jassAst.JassAst.JassFunctions;
import static de.peeeq.wurstscript.jassAst.JassAst.JassNatives;
import static de.peeeq.wurstscript.jassAst.JassAst.JassProg;
import static de.peeeq.wurstscript.jassAst.JassAst.JassSimpleVars;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStatements;
import static de.peeeq.wurstscript.jassAst.JassAst.JassTypeDefs;
import static de.peeeq.wurstscript.jassAst.JassAst.JassVars;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassFunctions;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassSimpleVar;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.jassAst.JassVars;
import de.peeeq.wurstscript.jassIm.ImArrayType;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImTupleArrayType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassImElement;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;

public class ImToJassTranslator {

	private ImProg imProg;
	private Multimap<ImFunction, ImFunction> calledFunctions;
	private ImFunction mainFunc;
	private ImFunction confFunction;
	private JassProg prog;
	private Stack<ImFunction> translatingFunctions = new Stack<ImFunction>();
	private Set<ImFunction> translatedFunctions = Sets.newHashSet();
	private Set<String> usedNames = Sets.newHashSet();
	private Map<JassImElement, AstElement> trace;

	public ImToJassTranslator(ImProg imProg, Multimap<ImFunction, ImFunction> calledFunctions, 
			ImFunction mainFunc, ImFunction confFunction, Map<JassImElement, AstElement> trace) {
		this.imProg = imProg;
		this.calledFunctions = calledFunctions;
		this.mainFunc = mainFunc;
		this.confFunction = confFunction;
		this.trace = trace;
	}
	
	public JassProg translate() {
		JassVars globals = JassVars();
		JassFunctions functions = JassFunctions();
		prog = JassProg(JassTypeDefs(), globals, JassNatives(), functions);
		
		collectGlobalVars();
		
		translateFunctionTransitive(mainFunc);
		translateFunctionTransitive(confFunction);
		
		return prog;
	}

	private void collectGlobalVars() {
		for (ImVar v : imProg.getGlobals()) {
			globalImVars.add(v);
		}
	}

	private void translateFunctionTransitive(ImFunction imFunc) {
		if (translatedFunctions.contains(imFunc)) {
			// already translated
			return;
		}
		if (translatingFunctions.contains(imFunc)) {
			if (imFunc != translatingFunctions.peek()) {
				String msg = "cyclic dependency between functions: " ;
				boolean start = false;
				for (ImFunction f : translatingFunctions) {
					if (imFunc == f) {
						start = true;
					}
					if (start) {
						msg += "\n - " + Utils.printElement(getTrace(f));
					}
				}
				WPos src = getTrace(imFunc).attrSource();
				throw new CompileError(src, msg);
			}
			// already translating, recursive function
			return;
		}
		translatingFunctions.push(imFunc);
		for (ImFunction f : sorted(calledFunctions.get(imFunc))) {
			translateFunctionTransitive(f);
		}
		
		translateFunction(imFunc);
		
		// translation finished
		if (translatingFunctions.pop() != imFunc) {
			throw new Error("something went wrong...");
		}
		translatedFunctions.add(imFunc);
	}

	private List<ImFunction> sorted(Collection<ImFunction> collection) {
		List<ImFunction> r = Lists.newArrayList();
		Collections.sort(r, new Comparator<ImFunction>() {

			@Override
			public int compare(ImFunction f, ImFunction g) {
				return f.getName().compareTo(g.getName());
			}
		});
		return r;
	}

	private AstElement getTrace(JassImElement elem) {
		while (elem != null) {
			AstElement t = trace.get(elem);
			if (t != null) {
				return t;
			}
			elem = elem.getParent();
		}
		throw new Error("Could not get trace to original program.");
	}

	private void translateFunction(ImFunction imFunc) {
		JassFunction f = getJassFuncFor(imFunc);
		
		f.setReturnType(imFunc.getReturnType().translateTypeFirst());
		// translate parameters
		for (ImVar v : imFunc.getParameters()) {
			f.getParams().addAll(simpleVars(getJassVarsFor(v)));
		}
		// translate locals
		for (ImVar v : imFunc.getLocals()) {
			f.getLocals().addAll(getJassVarsFor(v));
		}
		imFunc.getBody().translate(f.getBody(), f, this);
		
	}



	private List<JassSimpleVar> simpleVars(List<JassVar> l) {
		List<JassSimpleVar> result = Lists.newArrayListWithCapacity(l.size());
		for (JassVar i : l) {
			result.add((JassSimpleVar) i);
		}
		return result;
	}

	private String getUniqueName(String name) { // TODO find local names
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
			prog.getGlobals().add(v);
			tempReturnVars.put(key, v);
		}
		return v;
	}

	Map<ImVar, List<JassVar>> jassVars = Maps.newHashMap();
	private Set<ImVar> globalImVars = Sets.newHashSet();
	
	public List<JassVar> getJassVarsFor(ImVar v) {
		List<JassVar> vars = jassVars.get(v);
		if (vars == null) {
			boolean isArray = v.getType() instanceof ImArrayType || v.getType() instanceof ImTupleArrayType;
			vars = Lists.newArrayList();
			int i = 0;
			for (String type : v.getType().translateType()) {
				String name = v.getName();
				if (i > 0) {
					name += "_" + i; 
				}
				name = getUniqueName(name);
				if (isArray) {
					vars.add(JassAst.JassArrayVar(type, name));
				} else {
					vars.add(JassAst.JassSimpleVar(type, name));
				}
				i++;
			}
			if (isGlobal(v) && !v.getIsBJ()) {
				for (JassVar var : vars) {
					prog.getGlobals().add(var);
				}
			}
			jassVars.put(v, vars);
		}
		return vars ;
	}

	private boolean isGlobal(ImVar v) {
		return globalImVars.contains(v);
	}

	public JassVar newTempVar(JassFunction f, String type, String name) {
		JassSimpleVar v = JassAst.JassSimpleVar(type, getUniqueName(name));
		f.getLocals().add(v);
		return v;
	}

	Map<ImFunction, JassFunction> jassFuncs = Maps.newHashMap();
	
	public JassFunction getJassFuncFor(ImFunction func) {
		JassFunction f = jassFuncs.get(func);
		if (f == null) {
			f = JassFunction(getUniqueName(func.getName()), JassSimpleVars(), "nothing", JassVars(), JassStatements());
			if (!func.getIsNative() && !func.getIsBJ()) {
				prog.getFunctions().add(f);
			}
			jassFuncs.put(func, f);
		}
		return f;
	}

	
}