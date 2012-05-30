package de.peeeq.eclipsewurstplugin.editor.autoedit;

import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;

import de.peeeq.eclipsewurstplugin.editor.WurstEditor;

public class WurstAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy implements IAutoEditStrategy {


	Pattern incIndent = Pattern.compile("^(if |else|while |for |function |package |class |interface |module ).*");
	
	private void autoIndentAfterNewLine(IDocument d, DocumentCommand c) {

		if (c.offset == -1 || d.getLength() == 0)
			return;

		try {
			// find start of line
			int p= (c.offset == d.getLength() ? c.offset  - 1 : c.offset);
			IRegion info= d.getLineInformationOfOffset(p);
			int start= info.getOffset();

			String line = d.get(info.getOffset(), info.getLength());
			int spaces = 0;
			int startOfWord = start + info.getLength();
			for (int i=0; i< line.length(); i++) {
				if (line.charAt(i) == ' ') {
					spaces++;
				} else if (line.charAt(i) == '\t') {
					spaces+=4;
				} else {
					startOfWord = start + i; 
					break;
				}
			}
			int indent = spaces/4; 
			boolean usingSpaces = line.length() > 0 && line.charAt(0) == ' ';
			String lineT = line.substring(startOfWord-start);
			if (incIndent.matcher(lineT).matches()) {
				indent++;
			} else {
				indent=-1;
			}
			StringBuffer buf= new StringBuffer(c.text);
			if (indent >= 0) {
				for (int i=0; i<indent; i++) {
					buf.append('\t');
				}
			} else {
				// use same indentation:
				if (startOfWord > start) {
					// append to input
					buf.append(d.get(start, startOfWord - start));
				}
				
			}
			c.text= buf.toString();
		} catch (BadLocationException excp) {
			// stop work
		}
	}

	@Override
	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
		if (c.length == 0 && c.text != null) {
			if (TextUtilities.endsWith(d.getLegalLineDelimiters(), c.text) != -1) {
				autoIndentAfterNewLine(d, c);
			}
		}
	}

}
