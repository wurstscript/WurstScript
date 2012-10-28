package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class ToggleComment implements IEditorActionDelegate {

	private IEditorPart editor;

	@Override
	public void run(IAction action) {
		System.out.println("in toggle");
		if (!(editor instanceof WurstEditor)) {
			return;
		}
		WurstEditor editor = (WurstEditor) this.editor;
		TextSelection  sel = (TextSelection) editor.getSelectionProvider().getSelection();
		int offset = sel.getOffset();
		int lines = sel.getEndLine() - sel.getStartLine();
		IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		
		try {
			int addedOffset = 0;
			String text = sel.getText();
			for( int i = 0; i <= lines; i++) {
				System.out.println(text);
				doc.replace(doc.getLineOffset(sel.getStartLine() + i), 0,  "//");
				addedOffset = text.indexOf("\n", i);
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("end toggle");
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.editor = targetEditor;

	}

}
