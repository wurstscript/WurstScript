package de.peeeq.eclipsewurstplugin.launch;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import com.google.common.collect.Lists;

import de.peeeq.eclipsewurstplugin.builder.WurstNature;
import de.peeeq.eclipsewurstplugin.console.WurstConsole;
import de.peeeq.eclipsewurstplugin.ui.WurstPerspective;

public class LaunchDelegate implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		File file = new File(config.getAttribute(LaunchConstants.MAP_FILE, ""));
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(config.getAttribute(LaunchConstants.PROJECT_NAME, ""));
		monitor.beginTask("start preparing " + file, 1);
		launchMap(project, file, monitor);
		
	}
	
	private void launchMap(IProject project, File file, IProgressMonitor monitor) {
		WurstConsole console = WurstPerspective.findConsole();
		console.setModelManager(WurstNature.get(project).getModelManager());
		List<String> args = Lists.newArrayList("-stacktraces","-runcompiletimefunctions","-injectobjects");
		console.launchMap(file, args, monitor);
	}

}
