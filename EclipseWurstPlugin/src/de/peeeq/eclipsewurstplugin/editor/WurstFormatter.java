package de.peeeq.eclipsewurstplugin.editor;

import de.peeeq.wurstscript.attributes.prettyPrint.MaxOneSpacer;
import de.peeeq.wurstscript.attributes.prettyPrint.PrettyPrinter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class WurstFormatter implements IEditorActionDelegate {

    private IEditorPart editor;

    @Override
    public void run(IAction action) {
        if (!(editor instanceof WurstEditor)) {
            return;
        }
        WurstEditor editor = (WurstEditor) this.editor;

        // Pretty print CU
        StringBuilder prettyBuilder = new StringBuilder();
        editor.doWithCompilationUnitR(cu -> {
            if (cu != null) {
                PrettyPrinter.prettyPrint(cu, new MaxOneSpacer(), prettyBuilder, 0);
            }
            return null;
        });

        IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        doc.set(prettyBuilder.toString());

    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }

    @Override
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        this.editor = targetEditor;

    }
}
