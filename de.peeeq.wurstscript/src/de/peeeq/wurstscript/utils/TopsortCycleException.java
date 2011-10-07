package de.peeeq.wurstscript.utils;

import java.util.LinkedList;
import java.util.List;

public class TopsortCycleException extends Exception {

	public List<?> activeItems;

	public TopsortCycleException(LinkedList<?> activeItems2) {
		this.activeItems = activeItems2;
	}

}
