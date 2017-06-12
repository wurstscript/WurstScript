package de.peeeq.wurstscript.attributes;

import java.util.List;

import de.peeeq.wurstscript.attributes.CompileError.ErrorType;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.utils.NotNullList;

public class ErrorHandler {

	private List<CompileError> errors = new NotNullList<CompileError>();
	private List<CompileError> warnings = new NotNullList<CompileError>();
	private WurstGui gui;
	private boolean unitTestMode = false;

	public ErrorHandler(WurstGui gui) {
		this.gui = gui;
	}

	public int getErrorCount() {
		return getErrors().size();
	}
	
	public List<CompileError> getWarnings() {
		return warnings;
	}
	
	public List<CompileError> getErrors() {
		return errors;
	}
	
	public void setProgress(String message, double percent) {
		getGui().sendProgress(message);
	}

	public WurstGui getGui() {
		return gui;
	}

	public void sendError(CompileError err) {
		if (err.getErrorType() == ErrorType.ERROR) {
			errors.add(err);
		} else {
			warnings.add(err);
		}
		gui.sendError(err);
	}

	public void enableUnitTestMode() {
		unitTestMode = true;
	}

	public boolean isUnitTestMode() {
		return unitTestMode;
	}

}
