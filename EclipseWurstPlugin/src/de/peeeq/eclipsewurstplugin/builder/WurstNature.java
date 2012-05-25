package de.peeeq.eclipsewurstplugin.builder;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import de.peeeq.eclipsewurstplugin.WurstPlugin;
import de.peeeq.wurstscript.attributes.CompileError;

public class WurstNature implements IProjectNature {

	/**
	 * ID of this project nature
	 */
	public static final String NATURE_ID = "EclipseWurstPlugin.wurstNature";

	private IProject project;

	private ModelManager modelManager;

	public WurstNature() {
		this.modelManager = new ModelManagerImpl(this);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();

		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(WurstBuilder.BUILDER_ID)) {
				return;
			}
		}

		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		ICommand command = desc.newCommand();
		command.setBuilderName(WurstBuilder.BUILDER_ID);
		newCommands[newCommands.length - 1] = command;
		desc.setBuildSpec(newCommands);
		project.setDescription(desc, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(WurstBuilder.BUILDER_ID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i,
						commands.length - i - 1);
				description.setBuildSpec(newCommands);
				project.setDescription(description, null);			
				return;
			}
		}
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public ModelManager getModelManager() {
		return modelManager;
	}

	public static void addErrorMarker(IFile file, CompileError e) {
		try {
			IMarker marker = file.createMarker(WurstBuilder.MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, e.getMessage());
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

			marker.setAttribute(IMarker.LINE_NUMBER, e.getSource().getLine());
			marker.setAttribute(WurstPlugin.START_POS, e.getSource().getLeftPos());
			marker.setAttribute(WurstPlugin.END_POS, e.getSource().getRightPos());
		} catch (CoreException ex) {
		}
		
	}
	
	public static void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(WurstBuilder.MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	public static WurstNature get(IProject p) {
		try {
			IProjectNature nat = p.getNature(NATURE_ID);
			if (nat instanceof WurstNature) {
				return (WurstNature) nat;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

}
