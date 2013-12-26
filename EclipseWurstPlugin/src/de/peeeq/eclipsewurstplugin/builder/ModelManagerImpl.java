package de.peeeq.eclipsewurstplugin.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.peeeq.eclipsewurstplugin.WurstConstants;
import de.peeeq.eclipsewurstplugin.editor.CompilationUnitChangeListener;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstConfig;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.validation.WurstValidator;

/**
 * keeps a version of the model which is always the most recent one 
 */
public class ModelManagerImpl implements ModelManager {

	private volatile WurstModel model;
	private final WurstNature nature;
	private final Multimap<String, CompilationUnitChangeListener> changeListeners = HashMultimap.create();
	private volatile boolean needsFullBuild = true;
	private final Set<String> dependencies = Sets.newLinkedHashSet();
	
	public ModelManagerImpl(WurstNature nature) {
		this.nature = nature;
	}
	
	@Override
	public synchronized boolean removeCompilationUnit(IResource resource) {
		if (model == null) {
			return false;
		}
		if (!(resource instanceof IFile)) {
			return false;
		}
		ListIterator<CompilationUnit> it = model.listIterator();
		while (it.hasNext()) {
			CompilationUnit cu = it.next();
			if (cu.getFile().equals(resource.getProjectRelativePath().toString())) {
				it.remove();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean needsFullBuild() {
		return needsFullBuild;
	}

	@Override
	public synchronized void clean() {
		model = null;
		dependencies.clear();
		needsFullBuild = true;
		WLogger.info("Clean done.");
	}
	
	
	@Override
	public void typeCheckModel(WurstGui gui, boolean addErrorMarkers, boolean refreshAttributes) {
		if (needsFullBuild) {
			WLogger.info("needs full build...");
			try {
				nature.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			// full build will trigger a new run of typeCheckModel ...
			return;
		}
		doTypeCheck(gui, addErrorMarkers, true /*refreshAttributes*/);
	}

	private void doTypeCheck(WurstGui gui, boolean addErrorMarkers, boolean refreshAttributes) {
		synchronized (this) {
			WLogger.info("#typechecking with refresh = " + refreshAttributes);
			long time = System.currentTimeMillis();
			if (gui.getErrorCount() > 0) {
				if (addErrorMarkers) {
					nature.addErrorMarkers(gui, WurstBuilder.MARKER_TYPE_GRAMMAR);
				}
				WLogger.info("finished typechecking* in " + (System.currentTimeMillis() - time) + "ms");
				return;
			}
			if (model == null) {
				return;
			}
			if (refreshAttributes) {
				model.clearAttributes();
			}
			WurstCompilerJassImpl comp = getCompiler(gui);
			
			
			try {
				comp.addImportedLibs(model);		
				comp.checkProg(model);
			} catch (CompileError e) {
				gui.sendError(e);
			}
			WLogger.info("finished typechecking in " + (System.currentTimeMillis() - time) + "ms");
			if (addErrorMarkers) {
				nature.clearMarkers(WurstBuilder.MARKER_TYPE_TYPES);
				nature.addErrorMarkers(gui, WurstBuilder.MARKER_TYPE_TYPES);
			}
		}
	}

	private WurstCompilerJassImpl getCompiler(WurstGui gui) {
		try {
			nature.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Error(e);
		}
		WurstConfig config = new WurstConfig();
		WLogger.info("dependencies = " + dependencies);
		config.setSetting("lib", Utils.join(dependencies, ";"));
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(config, gui, RunArgs.defaults());
		comp.setHasCommonJ(true);
		return comp;
	}

	
	@Override
	public synchronized void updateModel(CompilationUnit cu, WurstGui gui) {
		if (model == null) {
			model = newModel(cu, gui);
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
		for (CompilationUnitChangeListener cl : changeListeners.get(cu.getFile())) {
			cl.onCompilationUnitChanged(cu);
		}
	}

	private WurstModel newModel(CompilationUnit cu, WurstGui gui) {
		Bundle bundle = Platform.getBundle(WurstConstants.PLUGIN_ID);
		if (bundle == null) {
			throw new Error("could not locate wurst bundle");
		}
		try {
			CompilationUnit commonJ = compileFromBundle(gui, bundle, "resources/common.j");
			CompilationUnit blizzardJ = compileFromBundle(gui, bundle, "resources/Blizzard.j");
			return Ast.WurstModel(blizzardJ , commonJ, cu);
		} catch (Exception e) {
			e.printStackTrace();
			return Ast.WurstModel(cu);
		}
	}

	private CompilationUnit compileFromBundle(WurstGui gui, Bundle bundle, String fileName) throws IOException {
		InputStream source = FileLocator.openStream(bundle, new Path(fileName), false);
		WurstConfig config = new WurstConfig();
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(config, gui, RunArgs.defaults());
		InputStreamReader reader = new InputStreamReader(source);

		URL fileUrl = FileLocator.find(bundle, new Path(fileName), Collections.emptyMap());
		String bundleFile = FileLocator.toFileURL(fileUrl).getFile();
		if (bundleFile.matches("^/[a-zA-Z]:/.*")) {
			bundleFile = bundleFile.substring(1);
		}
		CompilationUnit cu = comp.parse(bundleFile, reader);
		cu.setFile(bundleFile);
		return cu;
	}

	@Override
	public CompilationUnit getCompilationUnit(String fileName) {
		if (model == null) {
			return null;
		}
		for (CompilationUnit cu : model) {
			if (cu.getFile().equals(fileName)) {
				return cu;
			}
		}
		return null;
	}

	@Override
	public synchronized void registerChangeListener(String fileName, CompilationUnitChangeListener listener) {
		this.changeListeners.put(fileName, listener);
		
	}

	@Override
	public synchronized CompilationUnit parse(WurstGui gui, String fileName, Reader source) {
		WurstConfig config = new WurstConfig();
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(config, gui, RunArgs.defaults());
		comp.setHasCommonJ(true); // we always want to have a common.j if we have an eclipse plugin
		CompilationUnit cu = comp.parse(fileName, source);
		
		IFile file = nature.getProject().getFile(fileName);
		if (file != null) {
			WurstNature.deleteMarkers(file, WurstBuilder.MARKER_TYPE_GRAMMAR);
		}
		nature.renewErrorMarkers(gui, file);
		
		if (cu != null && cu.getJassDecls().size() + cu.getPackages().size() > 0) {
			cu.setFile(fileName);
			updateModel(cu, gui);
		}
		return cu;
	}

	@Override
	public synchronized void fullBuildDone() {
		needsFullBuild = false;
		WLogger.info("Full build done.");
	}

	@Override
	public synchronized void addDependency(File f) {
		dependencies.add(f.getAbsolutePath());
	}

	@Override
	public synchronized void clearDependencies() {
		dependencies.clear();
	}

	@Override
	public WurstModel getModel() {
		return model;
	}
	
	public WurstNature getNature() {
		return nature;
	}

	@Override
	public void removeCompilationUnitByName(String fileName) {
		ListIterator<CompilationUnit> it = model.listIterator();
		while (it.hasNext()) {
			CompilationUnit cu = it.next();
			if (cu.getFile().equals(fileName)) {
				it.remove();
				break;
			}
		}
	}

	@Override
	public void resolveImports(WurstGui gui) {
		WurstCompilerJassImpl comp = getCompiler(gui);
		
		try {
			comp.addImportedLibs(model);		
		} catch (CompileError e) {
			gui.sendError(e);
		}
	}

	
}
