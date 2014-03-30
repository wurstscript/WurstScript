package de.peeeq.eclipsewurstplugin.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;

public class WurstBuilder extends IncrementalProjectBuilder {

	class SampleDeltaVisitor implements IResourceDeltaVisitor {
		private WurstGui gui;

		public SampleDeltaVisitor(WurstGui gui) {
			this.gui = gui;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				WLogger.info("added " + resource.getName());
				changed |= checkCompilatinUnit(gui, resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				WLogger.info("removed " + resource.getName());
				changed |= getModelManager().removeCompilationUnit(resource);
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				WLogger.info("changed " + resource.getName());
				changed |= checkCompilatinUnit(gui, resource);
				break;
			}
			// return true to continue visiting children.
			return true;
		}

	}

	

	class SampleResourceVisitor implements IResourceVisitor {
		private WurstGui gui;

		public SampleResourceVisitor(WurstGui gui) {
			this.gui = gui;
		}
		public boolean visit(IResource resource) {
			checkCompilatinUnit(gui, resource);
			// return true to continue visiting children.
			return true;
		}
	}
	

	

	public static final String BUILDER_ID = "EclipseWurstPlugin.wurstBuilder";

	public static final String MARKER_TYPE_GRAMMAR = "EclipseWurstPlugin.wurstProblemGrammar";
	public static final String MARKER_TYPE_TYPES = "EclipseWurstPlugin.wurstProblemTypes";

	private boolean changed;

	private ModelManager getModelManager() {
		try {
			IProjectNature nature = getProject().getNature(WurstNature.NATURE_ID);
			if (nature instanceof WurstNature) {
				WurstNature wurstNature = (WurstNature) nature;
				return wurstNature.getModelManager();
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return new ModelManagerStub();
	}
	


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		WLogger.info("build ...");
		if (kind == FULL_BUILD || getModelManager().needsFullBuild()) {
			WLogger.info("needs full build: " + kind + ", " + getModelManager().needsFullBuild());
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				WLogger.info("delta is null");
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}
	
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		super.clean(monitor);
		WurstNature.get(getProject()).clearAllMarkers();
		getModelManager().clean();
	}

	boolean checkCompilatinUnit(WurstGui gui, IResource resource) {
		// TODO move to model manager?
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			if (!file.exists()) {
				return false;
			}
			if (isWurstOrJassFile(file)) {
				handleWurstFile(gui, file);
				return true;
			} else if (file.getName().equals("wurst.dependencies")) {
				handleWurstDependencyFile(gui, file);
				return true;
			}
		}
		return false;
	}


	private void handleWurstDependencyFile(WurstGui gui, IFile file) {
		WurstNature.deleteAllMarkers(file);
		try {
			getModelManager().clearDependencies();
			BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				addDependency(gui, file, line);						
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	private void handleWurstFile(WurstGui gui, IFile file) {
		WurstNature.deleteAllMarkers(file);

		Reader reader;
		boolean doChecks = true;
		try {
			reader = new InputStreamReader(file.getContents());
			String fileName = file.getProjectRelativePath().toString();
			getModelManager().parse(gui, fileName, reader);
		} catch (CoreException e) {
			WurstNature.addErrorMarker(file, new CompileError(new WPos(file.getFullPath().toString(), LineOffsets.dummy, 0, 0), e.getMessage()), MARKER_TYPE_GRAMMAR);
			e.printStackTrace();
		}

		if (doChecks) {
			WurstNature.get(file.getProject()).addErrorMarkers(gui, WurstBuilder.MARKER_TYPE_GRAMMAR);
		}
	}



	private boolean isWurstOrJassFile(IFile file) {
		return file.getName().endsWith(".wurst")
				|| file.getName().endsWith(".jurst")
				|| file.getName().endsWith(".j");
	}
	
	

	private void addDependency(WurstGui gui, IFile depfile, String fileName) {
		File f = new File(fileName);
		if (!f.exists()) {
			gui.sendError(new CompileError(new WPos(depfile.getProjectRelativePath().toString(), null, 0, 0), "Path '"+fileName + "' could not be found."));
			return;
		} else if (!f.isDirectory()) {
			gui.sendError(new CompileError(new WPos(depfile.getProjectRelativePath().toString(), null, 0, 0), "Path '"+fileName + "' is not a folder."));
			return;
		}
		
		getModelManager().addDependency(f);
	}

	

	

	


	private void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			WLogger.info("full build ...");
			WurstGui gui = new WurstGuiEclipse(monitor);
			getProject().accept(new SampleResourceVisitor(gui));
			getModelManager().fullBuildDone();
			getModelManager().typeCheckModel(gui, true, true);
		} catch (CoreException e) {
		}
	}

	

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		WLogger.info("incremental build ...");
		WurstGui gui = new WurstGuiEclipse(monitor);
		changed = false;
		delta.accept(new SampleDeltaVisitor(gui));
		if (changed) {
			getModelManager().typeCheckModel(gui, true, true);
		}
	}



	public static boolean isWurstMarker(IMarker marker) {
		try {
			return marker.isSubtypeOf(WurstBuilder.MARKER_TYPE_GRAMMAR) 
					|| marker.isSubtypeOf(WurstBuilder.MARKER_TYPE_TYPES);
		} catch (CoreException e) {
			return false;
		}
	}
}
