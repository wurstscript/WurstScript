package de.peeeq.pscript.ui.contentassist;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;

public class PscriptMultiLineTerminalsEditStrategy2 implements
		IAutoEditStrategy {

	@Override
	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		if (command.length != 0) {
			return;
		}
		String originalText = command.text;
		String[] lineDelimiters = document.getLegalLineDelimiters();
		int delimiterIndex = TextUtilities.startsWith(lineDelimiters, originalText);
		if (delimiterIndex != -1) { // new line entered
			
		}

	}

}
