package de.peeeq.eclipsewurstplugin.editor.autocomplete;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class WurstContextInformationValidator implements IContextInformationValidator {

	private ITextViewer viewer;
	private IContextInformation info;
	private int installOffset;

	@Override
	public void install(IContextInformation info, ITextViewer viewer, int offset) {
		this.info = info;
		this.viewer = viewer;
		this.installOffset = offset;

	}

	@Override
	public boolean isContextInformationValid(int offset) {
		try {
			IDocument doc = viewer.getDocument();
			IRegion lineInfo = doc.getLineInformationOfOffset(installOffset);
			if (offset < installOffset) {
				// cursor left of the initial parenthesis
				return false;
			}
			if (offset > lineInfo.getOffset() + lineInfo.getLength()) {
				//cursor in next line
				return false;
			}
			String line = doc.get(lineInfo.getOffset(), lineInfo.getLength());
			
			// count parenthesis
			int parenCount = 0;
			int parenInit = 0;
			int parenNow = 0;
			for (int i=0; i <line.length(); i++) {
				int absoluteI = i + lineInfo.getOffset();
				char c = line.charAt(i);
				if (c == '(') {
					parenCount++;
				} else if (c == ')') {
					parenCount--;
				}
				if (absoluteI+1 == installOffset) {
					parenInit = parenCount;
				}
				if (absoluteI+1 == offset) {
					parenNow = parenCount;
				}
			}
			return parenNow >= parenInit;
//			if (parenNow < parenInit) {
//				return false;
//			} else {
//				return true;
//			}
		} catch (BadLocationException e) {
		}
		return false;
	}

}

