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
				removeCompilationUnit(resource);
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
	
	private void removeCompilationUnit(IResource resource) {
		ListIterator<CompilationUnit> it = model.listIterator();
		while (it.hasNext()) {
			CompilationUnit cu = it.next();
			if (cu.getFile().equals(resource.getName())) {
				it.remove();
				break;
			}
		}

	}

	public static final String BUILDER_ID = "EclipseWurstPlugin.wurstBuilder";

	public static final String MARKER_TYPE = "EclipseWurstPlugin.wurstProblem";

	private WurstModel model;

	private void addMarker(IFile file, String message, int lineNumber, int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}

	private void addErrorMarker(IFile file, CompileError e) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, e.getMessage());
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

			marker.setAttribute(IMarker.LINE_NUMBER, e.getSource().getLine());
			marker.setAttribute(WurstPlugin.START_POS, e.getSource().getLeftPos());
			marker.setAttribute(WurstPlugin.END_POS, e.getSource().getRightPos());
		} catch (CoreException ex) {
		}

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
		if (kind == FULL_BUILD || model == null) {
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
		model = null;
	}

	void checkCompilatinUnit(WurstGui gui, IResource resource) {
		if (resource instanceof IFile && resource.getName().endsWith(".wurst")) {
			IFile file = (IFile) resource;
			deleteMarkers(file);

			WurstCompilerJassImpl comp = new WurstCompilerJassImpl(gui, RunArgs.defaults());
			Reader reader;
			boolean doChecks = true;
			try {
				reader = new InputStreamReader(file.getContents());
				CompilationUnit cu = comp.parse(reader, file.getName());
				cu.setFile(file.getName());
				// syntaxCodeAreaController.setAst(cu);
				// if (doChecks) {
				// // TODO do checks
				// comp.checkProg(Ast.WurstModel(cu));
				// }
				updateModel(resource, cu);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (doChecks) {
				for (CompileError e : gui.getErrorList()) {
					System.out.println(e);
					addErrorMarker(file, e);
				}
			}
		}
	}
	
	private void typeCheckModel(WurstGui gui) {
		if (gui.getErrorCount() > 0) {
			return;
		}
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(gui, RunArgs.defaults());
		comp.checkProg(model);
		for (CompileError e : gui.getErrorList()) {
			System.out.println(e);
			addErrorMarker(getFile(e.getSource().getFile()), e);
		}
	}

	private IFile getFile(String file) {
		return this.getProject().getFile(file);
	}

	private void updateModel(IResource resource, CompilationUnit cu) {
		if (model == null) {
			model = Ast.WurstModel(cu);
		} else {
			ListIterator<CompilationUnit> it = model.listIterator();
			boolean updated = false;
			while (it.hasNext()) {
				CompilationUnit c = it.next();
				if (c.getFile().equals(cu.getFile())) {
					// replace old compilationunit with new one:
					it.set(cu);
					updated = true;
					break;
				}
			}
			if (!updated) {
				model.add(cu);
			}
		}
		System.out.println("updated cu for " + cu.getFile());
		for (CompilationUnit c : model) {
			System.out.println("	- " + c.getFile());
		}
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			System.out.println("full build ...");
			WurstGui gui = new WurstGuiLogger();
			getProject().accept(new SampleResourceVisitor(gui));
			typeCheckModel(gui);
		} catch (CoreException e) {
		}
	}

	

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		System.out.println("incremental build ...");
		WurstGui gui = new WurstGuiLogger();
		delta.accept(new SampleDeltaVisitor(gui));
		typeCheckModel(gui);
	}
}
