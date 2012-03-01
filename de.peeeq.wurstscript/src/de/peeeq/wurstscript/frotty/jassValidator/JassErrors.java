package de.peeeq.wurstscript.frotty.jassValidator;

import java.util.List;

import com.google.common.collect.Lists;

public class JassErrors {
	
	static final private List<String> errors = Lists.newArrayList();
	
	public static void addError(String error, int line) {
		errors.add("Line : " + line + " - " + error );
	}

	public static int errorCount() {
		return errors.size();
	}

	public static List<String> getErrors() {
		return Lists.newArrayList(errors);
	}

}
