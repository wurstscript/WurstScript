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
import de.peeeq.wurstscript.gui.WurstGui;

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

	public static void addErrorMarker(IFile file, CompileError e, String markerType) {
		try {
			IMarker marker = file.createMarker(markerType);
			marker.setAttribute(IMarker.MESSAGE, e.getMessage());
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

			marker.setAttribute(IMarker.LINE_NUMBER, e.getSource().getLine());
			marker.setAttribute(WurstConstants.START_POS, e.getSource().getLeftPos());
			marker.setAttribute(WurstConstants.END_POS, e.getSource().getRightPos());
		} catch (CoreException ex) {
		}
		
	}
	
	
	public void addErrorMarkers(WurstGui gui, String markerType) {
		for (CompileError e : gui.getErrorList()) {
			IFile file = getProject().getFile(e.getSource().getFile());
			if (file != null) {
				addErrorMarker(file, e, markerType);
			}
		}
		gui.clearErrors();
	}
	
	public static void deleteMarkers(IFile file, String markerType) {
		try {
			file.deleteMarkers(markerType, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}
	
	public void clearMarkers(String markerType) {
		try {
			getProject().deleteMarkers(markerType, false, IResource.DEPTH_INFINITE);
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

	private WurstEditor open(String fileName, int offset) {
		IFile file = getProject().getFile(fileName);
		if (file.exists()) {
			WurstEditor editor = open(file, offset);
			return editor;
		} else { // open external file
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(file.getFullPath().toString()));
			if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
			    try {
			        IEditorPart editor = IDE.openEditorOnFileStore(getActiveWorkbenchPage(), fileStore);
			        if (editor instanceof WurstEditor) {
						WurstEditor wurstEditor = (WurstEditor) editor;
						wurstEditor.setHighlightRange(offset, 0, true);
						return wurstEditor;
					}
			    } catch (PartInitException e) {
			    	e.printStackTrace();
			    }
			}
		}
		return null;
	}
	
	public WurstEditor open(IFile file, int offset) {
		try {
			IEditorPart editor = IDE.openEditor(getActiveWorkbenchPage(), file);
			if (editor instanceof WurstEditor) {
				WurstEditor wurstEditor = (WurstEditor) editor;
				wurstEditor.setHighlightRange(offset, 0, true);
				return wurstEditor;
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	private static IWorkbenchPage getActiveWorkbenchPage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	public void clearAllMarkers() {
		clearMarkers(WurstBuilder.MARKER_TYPE_GRAMMAR);
		clearMarkers(WurstBuilder.MARKER_TYPE_TYPES);
	}

	public static void deleteAllMarkers(IFile file) {
		deleteMarkers(file, WurstBuilder.MARKER_TYPE_GRAMMAR);
		deleteMarkers(file, WurstBuilder.MARKER_TYPE_TYPES);
		
	}

	

	

	

}
