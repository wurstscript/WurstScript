package de.peeeq.wurstscript.gui;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.utils.NotNullList;

public class WurstGuiLogger implements WurstGui {

	private List<CompileError> errors = new NotNullList<CompileError>();
	
	@Override
	public void sendError(CompileError err) {
		errors.add(err);
	}

	@Override
	public void sendProgress(String whatsRunningNow, double percent) {
		// ignore
	}

	@Override
	public void sendFinished() {
		// ignore
	}
	
	public String getErrors() {
		String result = "";
		for (CompileError e : errors) {
			result += e + "\n";
		}
		return result;
	}

	public int getErrorCount() {
		return errors.size();
	}
	
	@Override
	public List<CompileError> getErrorList() {
		return Lists.newArrayList(errors);
	}

}
