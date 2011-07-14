package de.peeeq.pscript.intermediateLang.interpreter;

import de.peeeq.pscript.intermediateLang.ILconst;
import de.peeeq.pscript.intermediateLang.ILprog;

public interface ILInterpreter {
	
	/**
	 * Load a program
	 * @param prog
	 */
	void LoadProgram(ILprog prog);
	
	/**
	 * execute a function
	 * @param name name of the function
	 * @param arguments arguments of the function
	 * @return function return value
	 */
	ILconst executeFunction(String name, ILconst ... arguments);
	

}
