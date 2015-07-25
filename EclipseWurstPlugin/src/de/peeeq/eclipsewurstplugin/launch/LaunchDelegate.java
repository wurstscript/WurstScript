package de.peeeq.eclipsewurstplugin.launch;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import com.google.common.collect.Lists;

import de.peeeq.eclipsewurstplugin.builder.ModelManager;
import de.peeeq.eclipsewurstplugin.builder.WurstNature;
import de.peeeq.eclipsewurstplugin.console.WurstConsole;
import de.peeeq.eclipsewurstplugin.ui.WurstPerspective;

public class LaunchDelegate implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		File file = new File(config.getAttribute(LaunchConstants.MAP_FILE, ""));
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(config.getAttribute(LaunchConstants.PROJECT_NAME, ""));
		monitor.beginTask("start preparing " + file, 1);
		launchMap(project, file, monitor);

	}

	private void launchMap(IProject project, File file, IProgressMonitor monitor) {
		WurstConsole console = WurstPerspective.findConsole();
		WurstNature wurstNature = WurstNature.get(project, true);
		if (wurstNature == null) {
			throw new RuntimeException("Wurst nature not set for project, but it should be.");
		}
		if (!containsWar3Map(project)) {
			final Display display =  Display.getDefault();
			display.syncExec(new Runnable() {
				
				@Override
				public void run() {
					MessageBox dialog = 
							  new MessageBox(display.getActiveShell(), SWT.ICON_WARNING | SWT.OK);
							dialog.setText("No war3map.j");
							dialog.setMessage("There is no war3map.j in the current project. Try to save the map in the WurstPack editor first.");

							dialog.open(); 
				}
			});
			return;
		}

		ModelManager modelManager = wurstNature.getModelManager();
		if (modelManager == null) {
			throw new RuntimeException("No model manager set for project " + project.getName() + " .");
		}
		console.setModelManager(modelManager);
		List<String> args = Lists.newArrayList("-stacktraces", "-runcompiletimefunctions", "-injectobjects");
		console.launchMap(file, args, monitor);
	}

	private boolean containsWar3Map(IProject project) {
		Queue<IContainer> todo = new ArrayDeque<>();
		todo.add(project);
		while (!todo.isEmpty()) {
			IContainer container = todo.poll();
			for (IResource member : members(container)) {
				if (member instanceof IContainer) {
					todo.add((IContainer) member);
				} else if (member instanceof IFile) {
					if (member.getName().endsWith("war3map.j")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private IResource[] members(IContainer container) {
		try {
			return container.members();
		} catch (CoreException e) {
			return new IResource[0];
		}
	}

}
