package de.peeeq.wurstscript.intermediateLang;

import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.utils.Utils;

/**
 * represents the program in the intermediate language 
 *
 */
public class ILprog implements CodePrinting {
	private List<ILvar> globals = new NotNullList<ILvar>();
	private List<ILfunction> functions = new NotNullList<ILfunction>();
	private List<ILfunction> initFunctions = new NotNullList<ILfunction>();
	private Multimap<ILfunction, ILfunction> function_calls = ArrayListMultimap.create();
	private Multimap<ILfunction, ILfunction> function_calledBy = ArrayListMultimap.create();
	
	
	
	public ILprog() {
	}
	
	
	public List<ILfunction> getFunctions() {
		return functions;
	}


	public List<ILvar> getGlobals() {
		return globals;
	}


	public void addFunction(ILfunction function) {
		functions.add(function);
	}

	public void addInitializer(ILfunction initFunc) {
		addFunction(initFunc);
		initFunctions.add(initFunc);
		
	}

	public void addGlobalVar(ILvar v) {
		globals.add(v);
		
	}


	@Override
	public void printJass(StringBuilder sb, int indent) {
		// TODO sort functions
		// TODO global initializers
		
		// print globals
		sb.append("globals\n");
		for (ILvar v : globals) {
			Utils.printIndent(sb, indent+1);
			v.printJass(sb, 0);
			sb.append("\n");
		}
		sb.append("endglobals\n");
		
		// print functions:
		for (ILfunction f : functions) {
			f.printJass(sb, 0);
		}
	}


	public void addCallDependency(ILfunction func, ILfunction calledFunc) {
		function_calls.put(calledFunc, func);
		function_calledBy.put(func, calledFunc);
	}


}
