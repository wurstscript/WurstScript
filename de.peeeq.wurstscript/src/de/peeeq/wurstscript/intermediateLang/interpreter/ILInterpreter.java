package de.peeeq.wurstscript.intermediateLang.interpreter;

import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILprog;

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

	void trace(boolean b);
	

}
