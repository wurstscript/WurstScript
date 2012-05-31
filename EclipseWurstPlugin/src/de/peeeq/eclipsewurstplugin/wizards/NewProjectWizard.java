package de.peeeq.eclipsewurstplugin.wizards;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import de.peeeq.eclipsewurstplugin.WurstConstants;
import de.peeeq.eclipsewurstplugin.builder.WurstNature;
import de.peeeq.eclipsewurstplugin.util.UtilityFunctions;

public class NewProjectWizard extends Wizard implements INewWizard {

	private IWorkbench workbench;
	private WizardNewProjectCreationPage mainPage;

	private final String OVERWRITE_TITLE = "Overwriting project";
	private final String OVERWRITE_TEXT  = "A .project file exist in the given location" +
			" (maybe an existing project). Should the project be overwritten?";

	
	public NewProjectWizard() {
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		setWindowTitle("New Wurst project");
	}

	@Override
	public void addPages() {
		mainPage = new WizardNewProjectCreationPage("New Wurst project");
		mainPage.setDescription("Create a new Wurst project");
		mainPage.setTitle("New Wurst project");
	    addPage(mainPage);
	}
	
	@Override
	public boolean performFinish() {
		try{
			IProject project = mainPage.getProjectHandle();
			IProjectDescription desc;
			File projectFile;
			if(!mainPage.useDefaults()){
				projectFile = new File(mainPage.getLocationPath().toFile(), ".project");
				
				desc = ResourcesPlugin.getWorkspace().newProjectDescription(mainPage.getProjectName());
				desc.setLocation(mainPage.getLocationPath());
			} else {
				File projectDir = new File(mainPage.getLocationPath().toFile(), mainPage.getProjectName());
				projectFile = new File(projectDir, ".project");
				desc = ResourcesPlugin.getWorkspace().newProjectDescription(mainPage.getProjectName());
			}
			if(projectFile.exists()){
				boolean overwrite = MessageDialog.
					openQuestion(getShell(), OVERWRITE_TITLE,OVERWRITE_TEXT);
				if(overwrite)
					projectFile.delete();
				else
					return false;
			}
			project.create(desc, null);
			project.open(null);
			desc.setNatureIds(new String[]{WurstNature.NATURE_ID});
			project.setDescription(desc, null);
			
//			workbench.showPerspective(WurstPlugin.WURST_PERSPECTIVE_ID, workbench.getActiveWorkbenchWindow());
			return true;
		}catch(CoreException ex){
			ex.printStackTrace();
			UtilityFunctions.showErrorMessage("Could not create new Project: \n"+ex.getLocalizedMessage());
		}
		return false;
	}

	
}
