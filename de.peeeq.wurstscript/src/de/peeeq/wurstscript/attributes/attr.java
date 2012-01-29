package de.peeeq.wurstscript.attributes;

import java.util.List;

import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.utils.NotNullList;

public class attr {
	
	private static List<CompileError> errors;
	private static WurstGui gui;
	
	
	public static void init(WurstGui gui) {
		errors = new NotNullList<CompileError>();
		attr.gui = gui;
	}

	public static void addError(WPos pos, String msg) {
		for (CompileError err : errors) {
			if (err.getSource().getFile().equals(pos.getFile())
					&& err.getSource().getLine() == pos.getLine()) {
				// return only one error per line
				return;
			}
		}
		CompileError c = new CompileError(pos, msg);
		errors.add(c);
		gui.sendError(c);
	}
	
	
	public static int getErrorCount() {
		return errors.size();
	}
	
	public static List<CompileError> getErrors() {
		return errors;
	}
	
	public static void setProgress(String message, double percent) {
		gui.sendProgress(message, percent);
	}

	
}
