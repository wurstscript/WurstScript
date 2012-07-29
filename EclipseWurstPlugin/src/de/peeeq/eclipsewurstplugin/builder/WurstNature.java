package de.peeeq.eclipsewurstplugin.builder;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocumentExtension;

import de.peeeq.eclipsewurstplugin.WurstConstants;
import de.peeeq.eclipsewurstplugin.editor.WurstEditor;
import de.peeeq.wurstscript.attributes.CompileError;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
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
			marker.setAttribute(WurstConstants.START_POS, e.getSource().getLeftPos());
			marker.setAttribute(WurstConstants.END_POS, e.getSource().getRightPos());
		} catch (CoreException ex) {
		}
		
	}
	
	public static void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(WurstBuilder.MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}
	
	public void clearMarkers() {
		try {
			getProject().deleteMarkers(WurstBuilder.MARKER_TYPE, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
		}
		
	}

	public static WurstNature get(IProject p) {
		if (p == null) {
			return null;
		}
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

	public static void open(IProject p, String fileName, int offset) {
		WurstNature nature = get(p);
		if (nature != null) {
			nature.open(fileName, offset);
		}
		// TODO Auto-generated method stub
		
	}

	private void open(String fileName, int offset) {
		IFile file = getProject().getFile(fileName);
		IEditorPart editor = null;
		if (file.exists()) {
			try {
				editor  = IDE.openEditor(getActiveWorkbenchPage(), file);
				
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		} else { // open external file
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(fileName));
			if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
			    try {
			        editor = IDE.openEditorOnFileStore(getActiveWorkbenchPage(), fileStore);
			    } catch (PartInitException e) {
			    	e.printStackTrace();
			    }
			}
		}
		if (editor instanceof WurstEditor) {
			WurstEditor wurstEditor = (WurstEditor) editor;
			wurstEditor.setHighlightRange(offset, 0, true);
		}
		
	}
	
	private static IWorkbenchPage getActiveWorkbenchPage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	

}
