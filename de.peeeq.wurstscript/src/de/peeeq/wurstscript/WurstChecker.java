package de.peeeq.wurstscript;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.validation.WurstValidator;

public class WurstChecker {

	private final WurstGui gui;
	private ErrorHandler errorHandler;

	public WurstChecker(WurstGui gui, ErrorHandler errorHandler) {
		this.gui = gui;
		this.errorHandler = errorHandler;
	}

	public void checkProg(WurstModel root) {
		gui.sendProgress("Checking Files", 0.2);
		
		if (errorHandler.getErrorCount() > 0) return;
		
		attachErrorHanlder(root);
		
		expandModules(root);
		
		if (errorHandler.getErrorCount() > 0) return;
		
		// compute the flow attributes
		WurstValidator.computeFlowAttributes(root);
		
		
		// validate the resource:
		WurstValidator validator = new WurstValidator(root);
		validator.validate();
		WLogger.info("debug - finished checkProg");
	}

	private void attachErrorHanlder(WurstModel root) {
		for (CompilationUnit cu : root) {
			cu.setCuErrorHandler(errorHandler);
		}
	}
	
	private void expandModules(WurstModel root) {
		for (CompilationUnit cu : root) {
			new ModuleExpander().expandModules(cu);
		}
	}
	
}
