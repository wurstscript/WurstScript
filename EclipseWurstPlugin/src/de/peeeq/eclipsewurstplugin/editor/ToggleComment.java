package de.peeeq.eclipsewurstplugin.editor;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import com.google.common.collect.Lists;

public class ToggleComment implements IEditorActionDelegate {

	private IEditorPart editor;

	@Override
	public void run(IAction action) {
		if (!(editor instanceof WurstEditor)) {
			return;
		}
		WurstEditor editor = (WurstEditor) this.editor;
		TextSelection  sel = (TextSelection) editor.getSelectionProvider().getSelection();
		int startLine = sel.getStartLine();
		IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		
		try {
			int startOffset = doc.getLineOffset(startLine);
			int endOffset = sel.getOffset() + sel.getLength();
			int len = endOffset - startOffset;
			String text = doc.get(startOffset, len);
			
			
			if (text.matches("^(//(.*)(\\r?\\n|\\r))*//.*$")) {
				// remove comments
				text = text.substring(2).replaceAll("(\\r?\\n|\\r)//", "$1");
			} else {
				// add comments
				text = "//" + text.replaceAll("(\\r?\\n|\\r)", "$1//");
			}
			
			doc.replace(startOffset, len, text);
			// select new text
			editor.selectAndReveal(startOffset, text.length());
			
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.editor = targetEditor;

	}

}
