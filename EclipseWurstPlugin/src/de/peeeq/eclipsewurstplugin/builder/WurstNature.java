package de.peeeq.eclipsewurstplugin.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocumentExtension;

import de.peeeq.eclipsewurstplugin.WurstConstants;
import de.peeeq.eclipsewurstplugin.editor.WurstEditor;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.CompileError.ErrorType;
import de.peeeq.wurstscript.gui.WurstGui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.google.common.collect.Lists;
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
			if (e.getErrorType() == ErrorType.ERROR) {
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			} else {
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			}

			marker.setAttribute(IMarker.LINE_NUMBER, e.getSource().getLine());
			marker.setAttribute(WurstConstants.START_POS, e.getSource().getLeftPos());
			marker.setAttribute(WurstConstants.END_POS, e.getSource().getRightPos());
		} catch (CoreException ex) {
		}
		
	}
	
	
	public void renewErrorMarkers(WurstGui gui, IFile file) {
		if (gui.getErrorCount() > 0) {
			// when there are parse errors we also should clear the type errors:
			if (file != null) {
				WurstNature.deleteMarkers(file, WurstBuilder.MARKER_TYPE_TYPES);
			}
			addErrorMarkers(gui, WurstBuilder.MARKER_TYPE_GRAMMAR);
//			gui.clearErrors();
		}
	}
	
	public void addErrorMarkers(WurstGui gui, String markerType) {
		addMarkers(markerType, gui.getErrorList());
		addMarkers(markerType, gui.getWarningList());
//		gui.clearErrors();
	}

	private void addMarkers(String markerType, List<CompileError> list) {
		for (CompileError e : list) {
			IFile file = getProject().getFile(e.getSource().getFile());
			if (file != null) {
				addErrorMarker(file, e, markerType);
			}
		}
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
	
	public void clearMarkers(final String markerType, final List<String> fileNames) {
			try {
				getProject().accept(new IResourceVisitor() {
					
					@Override
					public boolean visit(IResource resource) throws CoreException {
						// TODO maybe I have to clear markers in folders?
						if (fileNames.contains(resource.getProjectRelativePath().toString())) {
							resource.deleteMarkers(markerType, false, IResource.DEPTH_ZERO);
						}
						return true;
					}
				});
			} catch (CoreException e) {
				e.printStackTrace();
			}
	}

	public static WurstNature get(final IProject p) {
		return get(p, false);
	}
	
	public static WurstNature get(final IProject p, boolean askAddNature) {
		if (p == null) {
			return null;
		}
		try {
			IProjectNature nat = p.getNature(NATURE_ID);
			if (nat instanceof WurstNature) {
				return (WurstNature) nat;
			} else if (askAddNature) {
				final boolean answer[] = new boolean[] {false};
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						answer[0] = MessageDialog.openQuestion(null, "No Wurst nature", "No Wurst nature was found for the project " + p.getName() + ".\n"
								+ "Do you want to add the Wurst nature?");
					}
				});
				if (answer[0]) {
					addNatureToProject(p);
					return get(p);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void addNatureToProject(IProject  p) throws CoreException {
		IProjectDescription desc = p.getDescription();
		ArrayList<String> natureIds = Lists.newArrayList(Arrays.asList(desc.getNatureIds()));
		natureIds.add(NATURE_ID);
		desc.setNatureIds(natureIds.toArray(new String[0]));
		p.setDescription(desc, null);
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
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(fileName));
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
