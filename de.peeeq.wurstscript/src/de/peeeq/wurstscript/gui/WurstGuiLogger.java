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
	
	@Override
	public String getErrors() {
		String result = "";
		for (CompileError e : errors) {
			result += e + "\n";
		}
		return result;
	}

	@Override
	public int getErrorCount() {
		return errors.size();
	}
	
	@Override
	public List<CompileError> getErrorList() {
		return Lists.newArrayList(errors);
	}

	@Override
	public void clearErrors() {
		errors.clear();
	}

	@Override
	public void showInfoMessage(String message) {
		System.out.println(message);
	}

}
