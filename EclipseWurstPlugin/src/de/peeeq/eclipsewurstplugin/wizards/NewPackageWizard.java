package de.peeeq.eclipsewurstplugin.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import de.peeeq.eclipsewurstplugin.builder.WurstNature;
import de.peeeq.eclipsewurstplugin.editor.WurstEditor;
import de.peeeq.wurstscript.utils.Utils;

public class NewPackageWizard extends Wizard implements INewWizard {


	private IWorkbench workbench;
	private IStructuredSelection selection;
	private WizardNewFileCreationPage mainPage;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
		setWindowTitle("Create new Wurst package");
	}

    @Override
    public void addPages() {
            IStructuredSelection projectSelectionFromModulePath = getProjectSelection();
            mainPage = new WizardNewFileCreationPage("Create new Wurst package", projectSelectionFromModulePath);
            mainPage.setTitle("Create new Wurst package");
            mainPage.setFileExtension("wurst");
            mainPage.setDescription("Create a new Wurst package");
            addPage(mainPage);
    }
	
	private IStructuredSelection getProjectSelection() {
		IStructuredSelection s = selection;
//		TODO
		return s;
	}


	@Override
	public boolean performFinish() {
		mainPage.setFileName(Utils.toFirstUpper(mainPage.getFileName()));
        IFile file = mainPage.createNewFile();
        if (file != null) {
                    WurstNature nature = WurstNature.get(file.getProject());
                    WurstEditor editor = nature.open(file, 0);
                	
                    IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
                    String code = "package " + file.getName().replace(".wurst", "") + "\n\n\n";
					doc.set(code);
					editor.setHighlightRange(code.length()-1, 0, true); 
                       
                return true;
        } else
                return false;
	}

}
