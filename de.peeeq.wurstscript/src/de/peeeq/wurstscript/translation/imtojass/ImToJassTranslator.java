package de.peeeq.wurstscript.translation.imtojass;

import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import static de.peeeq.wurstscript.jassAst.JassAst.*;
import de.peeeq.wurstscript.jassAst.JassFunctions;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassVars;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;

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
		
		
		// translation finished
		translatedFunctions.add(imFunc);
	}
	
	
}