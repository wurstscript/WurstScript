package de.peeeq.wurstscript.intermediateLang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.utils.NotNullList;

/**
 * represents the program in the intermediate language 
 *
 */
public class ILprog implements CodePrinting {
	private List<ILvar> globals = new NotNullList<ILvar>();
	private List<ILfunction> functions = new NotNullList<ILfunction>();
	private List<ILfunction> initFunctions = new NotNullList<ILfunction>();
	
	
	
	
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
	public void printJass(StringBuilder sb) {
		// TODO sort functions
		// TODO global initializers
		
		// print globals
		sb.append("globals\n");
		for (ILvar v : globals) {
			v.printJass(sb);
			sb.append("\n");
		}
		sb.append("endglobals\n");
		
		// print functions:
		for (ILfunction f : functions) {
			f.printJass(sb);
		}
	}


}
