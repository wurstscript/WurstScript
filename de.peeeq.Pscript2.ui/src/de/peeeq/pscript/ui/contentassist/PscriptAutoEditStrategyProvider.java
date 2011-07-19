package de.peeeq.pscript.ui.contentassist;

import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.xtext.ui.editor.autoedit.DefaultAutoEditStrategyProvider;

public class PscriptAutoEditStrategyProvider extends
		DefaultAutoEditStrategyProvider {

	
	@Override
	protected void configure(IEditStrategyAcceptor acceptor) {
		super.configure(acceptor);
		
		addOpenClosePair(acceptor, "package", "endpackage");
		addOpenClosePair(acceptor, "class", "endclass");
		addOpenClosePair(acceptor, "interface", "endinterface");
		addOpenClosePair(acceptor, "function", "endfunction");
		addOpenClosePair(acceptor, "construct", "endconstruct");
		
		addOpenClosePair(acceptor, "if", "endif");
		addOpenClosePair(acceptor, "loop", "endloop");
		addOpenClosePair(acceptor, "while", "endwhile");
		addOpenClosePair(acceptor, "for", "endfor");
		addOpenClosePair(acceptor, "foreach", "endforeach");		
		
		
	}

	private void addOpenClosePair(IEditStrategyAcceptor acceptor,
			String open, String close) {
		acceptor.accept(
				new PscriptMultiLineTerminalsEditStrategy(open, "\t",close)		
				, IDocument.DEFAULT_CONTENT_TYPE);
		
	}
	
}
