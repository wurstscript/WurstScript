package de.peeeq.wurstscript.gui;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.CompileError.ErrorType;
import de.peeeq.wurstscript.utils.Utils;

public abstract class WurstGui {
	
	private List<CompileError> errors = Lists.newArrayList();
	
	
	
	public abstract void sendProgress(String whatsRunningNow, double percent);
	
	public abstract void sendFinished();

	public abstract void showInfoMessage(String message);
	
	public void sendError(CompileError err) {
		errors.add(err);
	}
	
	public void clearErrors() {
		errors.clear();
	}

	public final int getErrorCount() {
		return getErrorList().size();
	}

	public final String getErrors() {
		return Utils.join(errors, "\n");
	}

	public final List<CompileError> getErrorList() {
		return Utils.filter(errors, new Function<CompileError,Boolean>() {
			@Override
			public Boolean apply(CompileError e) {
				return e.getErrorType() == ErrorType.ERROR;
			}
		});
	}
	
	public final List<CompileError> getWarningList() {
		return Utils.filter(errors, new Function<CompileError,Boolean>() {
			@Override
			public Boolean apply(CompileError e) {
				return e.getErrorType() == ErrorType.WARNING;
			}
		});
	}

	
}
