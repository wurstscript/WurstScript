package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.ImError;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.JassIm;

public class DebugMessageRemover {

	public static void removeDebugMessages(ImProg prog) {
		prog.accept(new ImProg.DefaultVisitor() {
			@Override
			public void visit(ImError e) {
				e.replaceWith(JassIm.ImNull());
			}
		});
		
	}

}
