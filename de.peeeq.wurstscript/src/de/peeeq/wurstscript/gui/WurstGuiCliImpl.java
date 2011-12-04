package de.peeeq.wurstscript.gui;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.utils.Utils;

/**
 * implementation for use with cli interfaces
 */
public class WurstGuiCliImpl implements WurstGui {

	private List<CompileError> errors = Lists.newLinkedList();
	
	@Override
	public void sendError(CompileError err) {
		errors.add(err);
		System.out.println(err);
	}

	@Override
	public void sendProgress(String msg, double percent) {
	}

	@Override
	public void sendFinished() {
		System.out.println("done");
	}

	@Override
	public int getErrorCount() {
		return errors.size();
	}

	@Override
	public String getErrors() {
		return Utils.join(errors, "\n");
	}

	@Override
	public List<CompileError> getErrorList() {
		return Lists.newLinkedList(errors);
	}

}
