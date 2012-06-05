package de.peeeq.wurstscript.attributes;

import java.util.List;

import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.utils.NotNullList;

public class attr {
	
	private static ThreadLocal<List<CompileError>> errors = new ThreadLocal<List<CompileError>>();
	private static ThreadLocal<WurstGui> gui = new ThreadLocal<WurstGui>();
	
	// set this to true, when running unit tests. 
	//This will throw compiler errors instead of adding them to the list
	public static boolean unitTestMode = false;
	
	public static void init(WurstGui gui) {
		setErrors(new NotNullList<CompileError>());
		attr.setGui(gui);
	}

	public static void addError(WPos pos, String msg) {
		if (unitTestMode) {
			throw new CompileError(pos, msg);
		}
		for (CompileError err : getErrors()) {
			if (err.getSource().getFile().equals(pos.getFile())
					&& err.getSource().getLine() == pos.getLine()) {
				// return only one error per line
				return;
			}
		}
		CompileError c = new CompileError(pos, msg);
		getErrors().add(c);
		getGui().sendError(c);
	}
	
	
	public static int getErrorCount() {
		return getErrors().size();
	}
	
	public static List<CompileError> getErrors() {
		return errors.get();
	}
	
	public static void setProgress(String message, double percent) {
		getGui().sendProgress(message, percent);
	}

	public static void setErrors(List<CompileError> errors) {
		attr.errors.set(errors);
	}

	public static WurstGui getGui() {
		return gui.get();
	}

	public static void setGui(WurstGui gui) {
		attr.gui.set(gui);
	}

	
}
