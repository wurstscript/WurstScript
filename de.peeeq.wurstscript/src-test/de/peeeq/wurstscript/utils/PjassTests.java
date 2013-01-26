package de.peeeq.wurstscript.utils;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import de.peeeq.wurstscript.Pjass;
import de.peeeq.wurstscript.Pjass.Result;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiImpl;

public class PjassTests {

	@Test
	public void test() {
		Result result = Pjass.runPjass(new File("./testscripts/invalid/fail.j"));
		System.out.println(result.getMessage());
		
		
		WurstGuiImpl gui = new WurstGuiImpl();
	
		for (CompileError err : result.getErrors()) {
			System.out.println(err);
			System.out.println(err.getSource().getLeftPos());
			gui.sendError(err);
		}

	}
	
	public static void main(String[] args) {
		
		WurstGuiImpl gui = new WurstGuiImpl();
		Result result = Pjass.runPjass(new File("./testscripts/invalid/fail.j"));
		System.out.println(result.getMessage());
		
		
		
	
		for (CompileError err : result.getErrors()) {
			System.out.println(err);
			System.out.println(err.getSource().getLeftPos());
			gui.sendError(err);
		}
		gui.sendFinished();
	}

}
