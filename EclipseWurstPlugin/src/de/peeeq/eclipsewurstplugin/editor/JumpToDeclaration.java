package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class JumpToDeclaration implements IEditorActionDelegate {

	private IEditorPart editor;

	@Override
	public void run(IAction action) {
		if (!(editor instanceof WurstEditor)) {
			return;
		}
		WurstEditor editor = (WurstEditor) this.editor;
		TextSelection  sel = (TextSelection) editor.getSelectionProvider().getSelection();
		IHyperlink[] links = new WurstHyperlinkDetector(editor).getHyperlinks(sel.getOffset()); 
		if (links != null && links.length > 0) {
			links[0].open();
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
