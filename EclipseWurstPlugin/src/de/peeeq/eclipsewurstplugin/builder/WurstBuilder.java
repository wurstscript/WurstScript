package de.peeeq.eclipsewurstplugin.builder;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.text.BadLocationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.peeeq.eclipsewurstplugin.WurstPlugin;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;

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
				checkCompilatinUnit(gui, resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				getModelManager().removeCompilationUnit(resource);
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				checkCompilatinUnit(gui, resource);
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

	public static final String MARKER_TYPE = "EclipseWurstPlugin.wurstProblem";

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
		System.out.println("build ...");
		if (kind == FULL_BUILD || getModelManager().needsFullBuild()) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
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
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
		getModelManager().clean();
	}

	void checkCompilatinUnit(WurstGui gui, IResource resource) {
		// TODO move to model manager?
		if (resource instanceof IFile && resource.getName().endsWith(".wurst")) {
			IFile file = (IFile) resource;
			WurstNature.deleteMarkers(file);

			Reader reader;
			boolean doChecks = true;
			try {
				reader = new InputStreamReader(file.getContents());
				String fileName = file.getProjectRelativePath().toString();
				getModelManager().parse(gui, fileName, reader);
			} catch (CoreException e) {
				e.printStackTrace();
			}

			if (doChecks) {
				for (CompileError e : gui.getErrorList()) {
					System.out.println(e);
					WurstNature.addErrorMarker(file, e);
				}
			}
		}
	}
	
	

	

	

	

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			System.out.println("full build ...");
			WurstGui gui = new WurstGuiEclipse(monitor);
			getProject().accept(new SampleResourceVisitor(gui));
			getModelManager().typeCheckModel(gui);
			getModelManager().fullBuildDone();
		} catch (CoreException e) {
		}
	}

	

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		System.out.println("incremental build ...");
		WurstGui gui = new WurstGuiEclipse(monitor);
		delta.accept(new SampleDeltaVisitor(gui));
		getModelManager().typeCheckModel(gui);
	}
}
