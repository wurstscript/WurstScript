package de.peeeq.wurstscript.gui;

import de.peeeq.wurstscript.ast.AST;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.utils.Utils;

public class TestStartGui {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WurstGuiImpl gui = new WurstGuiImpl();

		gui.sendProgress(null, 0.1);
		Utils.sleep(1000);
		gui.sendError(new CompileError(AST.WPos("C:/pscript/de.peeeq.wurstscript/testscripts/valid/testIf_1.pscript", 1, 8), "test"));
		Utils.sleep(1000);
		gui.sendError(new CompileError(AST.WPos("C:/pscript/de.peeeq.wurstscript/testscripts/valid/testIf_1.pscript", 3, 14), "string"));
		gui.sendProgress(null, 0.4);
		Utils.sleep(1000);
		gui.sendError(new CompileError(AST.WPos("C:/pscript/de.peeeq.wurstscript/testscripts/valid/testIf_1.pscript", 4, 3), "nativetype"));
		gui.sendError(new CompileError(AST.WPos("C:/pscript/de.peeeq.wurstscript/testscripts/valid/testIf_1.pscript", 46, 9), "2 == 2"));
		gui.sendError(new CompileError(AST.WPos("C:/pscript/de.peeeq.wurstscript/testscripts/valid/testIf_1.pscript", 49, 9), "testFail?"));
		gui.sendProgress(null, 0.7);
		Utils.sleep(1000);
		Utils.sleep(1000);
		gui.sendError(new CompileError(
				AST.WPos("C:/pscript/de.peeeq.wurstscript/testscripts/valid/natives_bj.pscript", 10000, 9),
				"Some really large Text "
						+ "which might not fit in here. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. Bla blub blabbel probel blub. "));
		gui.sendProgress(null, 0.9);
		Utils.sleep(1000);
		gui.sendFinished();

	}

}
