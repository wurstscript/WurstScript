package de.peeeq.eclipsewurstplugin.launch;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

import de.peeeq.eclipsewurstplugin.WurstPlugin;
import de.peeeq.eclipsewurstplugin.builder.WurstNature;
import de.peeeq.eclipsewurstplugin.console.WurstConsole;
import de.peeeq.eclipsewurstplugin.editor.WurstEditor;
import de.peeeq.eclipsewurstplugin.ui.WurstPerspective;

public class Shortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		if (selection instanceof TreeSelection) {
			TreeSelection sel = (TreeSelection) selection;
			Object elem = sel.getFirstElement();
			if (elem instanceof IFile) {
				IFile file = (IFile) elem;
				if (file.getName().endsWith(".w3x")) {
					launchMap(file.getProject(), new File(file.getLocationURI()));
					return;
				}
				chooseMapAndLaunch(file.getProject());
				return;
			} else if (elem instanceof IProject) {
				chooseMapAndLaunch((IProject) elem);
			}
		}
		throw new Error("Cannot launch selection.");
	}

	private void chooseMapAndLaunch(IProject project) {
		final Display display = Display.getDefault();
		FileDialog dialog = new FileDialog(display.getActiveShell(), SWT.OPEN);
		dialog.setFilterNames(new String[] { "Warcraft Files (*.w3x)" });
		dialog.setText("Select map to run with " + project.getName() + ".");
		dialog.setFilterExtensions(new String[] { "*.w3x"});
		String f = dialog.open();
		if (f != null) {
			launchMap(project, new File(f));
		}
	    
		
	}

	

	@Override
	public void launch(IEditorPart editor, String mode) {
		if (editor instanceof WurstEditor) {
			WurstEditor we = (WurstEditor) editor;
			chooseMapAndLaunch(we.getProject());
		}
	}
	
    private void launchMap(IProject project, File mapFile) {
        try {
            String launchName = getLaunchManager().generateLaunchConfigurationName(project.getName());
            
            ILaunchConfigurationWorkingCopy launchConfig =
                    getLaunchConfigType().newInstance(null, launchName);

            launchConfig.setAttribute(LaunchConstants.MAP_FILE, mapFile.getAbsolutePath());
            launchConfig.setAttribute(LaunchConstants.PROJECT_NAME, project.getName());

            boolean exists = false;
            for (ILaunchConfiguration cfg : getLaunchManager().getLaunchConfigurations(getLaunchConfigType())) {
				if (cfg.getAttribute(LaunchConstants.MAP_FILE, "").equals(mapFile.getAbsolutePath())
						&& cfg.getAttribute(LaunchConstants.PROJECT_NAME, "").equals(project.getName())) {
					exists = true;
				}
			}
            if (!exists) {
            	launchConfig.doSave();
            }
            DebugUITools.launch(launchConfig, "run");
        } catch (Exception e) {
        	throw new Error(e);
        }
    }

    public static ILaunchConfigurationType getLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType(LaunchConstants.LAUNCH_CONFIG_ID);
    }

    public static ILaunchManager getLaunchManager() {
        return DebugPlugin.getDefault().getLaunchManager();
    }

}
