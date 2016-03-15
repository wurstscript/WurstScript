package de.peeeq.eclipsewurstplugin.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.Bundle;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.peeeq.eclipsewurstplugin.WurstPlugin;
import de.peeeq.eclipsewurstplugin.editor.CompilationUnitChangeListener;
import de.peeeq.wurstio.ModelChangedException;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.utils.Utils;

/**
 * keeps a version of the model which is always the most recent one
 */
public class ModelManagerImpl implements ModelManager {

	private volatile @Nullable WurstModel model;
	private final WurstNature nature;
	private final Multimap<String, CompilationUnitChangeListener> changeListeners = HashMultimap.create();
	private volatile boolean needsFullBuild = true;
	private final Set<String> dependencies = Sets.newLinkedHashSet();

	public ModelManagerImpl(WurstNature nature) {
		this.nature = nature;
	}

	@Override
	public synchronized boolean removeCompilationUnit(IResource resource) {
		WurstModel model2 = model;
		if (model2 == null) {
			return false;
		}
		if (!(resource instanceof IFile)) {
			return false;
		}
		ListIterator<CompilationUnit> it = model2.listIterator();
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
	public void typeCheckModel(WurstGui gui, boolean addErrorMarkers) {
		if (needsFullBuild) {
			WLogger.info("needs full build...");
			try {

				buildProject(IncrementalProjectBuilder.FULL_BUILD);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			// full build will trigger a new run of typeCheckModel ...
			return;
		}
		doTypeCheck(gui, addErrorMarkers);
	}

	private void buildProject(int buildType) throws CoreException {
		if (Thread.holdsLock(this)) {
			throw new RuntimeException("Modelmanager was locked for build");
		}
		nature.getProject().build(buildType, null);
	}

	@Override
	public void typeCheckModelPartial(WurstGui gui, boolean addErrorMarkers, List<String> toCheckFilenames) {
		if (needsFullBuild) {
			WLogger.info("needs full build...");
			try {
				buildProject(IncrementalProjectBuilder.FULL_BUILD);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			// full build will trigger a new run of typeCheckModel ...
			return;
		}
		doTypeCheckPartial(gui, addErrorMarkers, toCheckFilenames);
	}

	private void doTypeCheckPartial(WurstGui gui, boolean addErrorMarkers, List<String> toCheckFilenames) {
		// this line is not synchronized, because it can trigger a build in a
		// different thread
		WurstCompilerJassImpl comp = getCompiler(gui);
		synchronized (this) {
			List<CompilationUnit> toCheck = getCompilationUnits(toCheckFilenames);
			long time = System.currentTimeMillis();
			if (gui.getErrorCount() > 0) {
				if (addErrorMarkers) {
					nature.addErrorMarkers(gui, WurstBuilder.MARKER_TYPE_GRAMMAR);
				}
				WLogger.info("finished partial typechecking* in " + (System.currentTimeMillis() - time) + "ms");
				return;
			}
			WurstModel model2 = model;
			if (model2 == null) {
				return;
			}

			try {
				clearAttributes(toCheck);
				comp.addImportedLibs(model2);
				comp.checkProg(model2, toCheck);
			} catch (ModelChangedException e) {
				// model changed, early return
				return;
			} catch (CompileError e) {
				gui.sendError(e);
			}
			List<String> fileNames = getfileNames(toCheck);
			WLogger.info(
					"finished partial typechecking " + fileNames + " in " + (System.currentTimeMillis() - time) + "ms");
			if (addErrorMarkers) {
				nature.clearMarkers(WurstBuilder.MARKER_TYPE_TYPES, fileNames);
				nature.addErrorMarkers(gui, WurstBuilder.MARKER_TYPE_TYPES);
			}
		}
	}

	private List<CompilationUnit> getCompilationUnits(List<String> fileNames) {
		WurstModel model2 = model;
		if (model2 == null) {
			return Collections.emptyList();
		}
		return model2.stream().filter(cu -> fileNames.contains(cu.getFile())).collect(Collectors.toList());
	}

	private List<String> getfileNames(List<CompilationUnit> compilationUnits) {
		return compilationUnits.stream().map(CompilationUnit::getFile).collect(Collectors.toList());
	}

	/**
	 * clear the attributes for all compilation units that import something from
	 * 'toCheck'
	 */
	private void clearAttributes(List<CompilationUnit> toCheck) {
		WurstModel model2 = model;
		if (model2 == null) {
			return;
		}
		model2.clearAttributesLocal();
		Set<String> packageNames = Sets.newHashSet();
		for (CompilationUnit cu : toCheck) {
			cu.clearAttributes();
			for (WPackage p : cu.getPackages()) {
				packageNames.add(p.getName());
			}
		}
		for (CompilationUnit cu : model2) {
			if (imports(cu, packageNames, false)) {
				cu.clearAttributes();
			}
		}

	}

	/** check whether cu imports something from 'toCheck' */
	private boolean imports(CompilationUnit cu, Set<String> packageNames, boolean importPublic) {
		for (WPackage p : cu.getPackages()) {
			if (imports(p, packageNames, false, Sets.<WPackage> newHashSet())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * check whether p imports something from 'toCheck'
	 * 
	 * @param hashSet
	 */
	private boolean imports(WPackage p, Set<String> packageNames, boolean importPublic, HashSet<WPackage> visited) {
		if (visited.contains(p)) {
			return false;
		}
		visited.add(p);
		for (WImport imp : p.getImports()) {
			if ((!importPublic || imp.getIsPublic()) && packageNames.contains(imp.getPackagename())) {
				return true;
			} else {
				WPackage importedPackage = imp.attrImportedPackage();
				if (imp.getIsPublic() && importedPackage != null) {
					if (imports(importedPackage, packageNames, true, visited)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void doTypeCheck(WurstGui gui, boolean addErrorMarkers) {
		// this line is not synchronized, because it can trigger a build in a
		// different thread
		WurstCompilerJassImpl comp = getCompiler(gui);
		synchronized (this) {
			long time = System.currentTimeMillis();
			if (gui.getErrorCount() > 0) {
				if (addErrorMarkers) {
					nature.addErrorMarkers(gui, WurstBuilder.MARKER_TYPE_GRAMMAR);
				}
				WLogger.info("finished typechecking* in " + (System.currentTimeMillis() - time) + "ms");
				return;
			}
			@Nullable
			WurstModel model2 = model;
			if (model2 == null) {
				return;
			}

			try {
				model2.clearAttributes();
				comp.addImportedLibs(model2);
				comp.checkProg(model2);
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
			buildProject(IncrementalProjectBuilder.INCREMENTAL_BUILD);
		} catch (CoreException e) {
			e.printStackTrace();
			throw new Error(e);
		}
		WLogger.info("dependencies = " + dependencies);
		// config.setSetting("lib", Utils.join(dependencies, ";"));
		// TODO set dependencies
		RunArgs runArgs = RunArgs.defaults();
		runArgs.addLibs(dependencies);
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(gui, null, runArgs);
		comp.setHasCommonJ(true);
		return comp;
	}

	@Override
	public synchronized void updateModel(CompilationUnit cu, WurstGui gui) {
		@Nullable
		WurstModel model2 = model;
		if (model2 == null) {
			model = model2 = newModel(cu, gui);
		} else {
			ListIterator<CompilationUnit> it = model2.listIterator();
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
				model2.add(cu);
			}
		}
		for (CompilationUnitChangeListener cl : changeListeners.get(cu.getFile())) {
			cl.onCompilationUnitChanged(cu);
		}
	}

	private WurstModel newModel(CompilationUnit cu, WurstGui gui) {
		Bundle bundle = Platform.getBundle(WurstPlugin.PLUGIN_ID);
		if (bundle == null) {
			throw new Error("could not locate wurst bundle");
		}
		try {
			CompilationUnit commonJ = compileFromBundle(gui, bundle, "resources/common.j");
			CompilationUnit blizzardJ = compileFromBundle(gui, bundle, "resources/Blizzard.j");
			return Ast.WurstModel(blizzardJ, commonJ, cu);
		} catch (Exception e) {
			e.printStackTrace();
			return Ast.WurstModel(cu);
		}
	}

	private CompilationUnit compileFromBundle(WurstGui gui, Bundle bundle, String fileName) throws IOException {
		InputStream source = FileLocator.openStream(bundle, new Path(fileName), false);
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(gui, null, RunArgs.defaults());

		try (InputStreamReader reader = new InputStreamReader(source)) {
			URL fileUrl = FileLocator.find(bundle, new Path(fileName), Collections.emptyMap());
			String bundleFile = FileLocator.toFileURL(fileUrl).getFile();
			if (bundleFile.matches("^/[a-zA-Z]:/.*")) {
				bundleFile = bundleFile.substring(1);
			}
			CompilationUnit cu = comp.parse(bundleFile, reader);
			cu.setFile(bundleFile);
			return cu;
		}
	}

	@Override
	public synchronized void doWithCompilationUnit(String fileName, Consumer<CompilationUnit> action) {
		doWithCompilationUnitR(fileName, cu -> {
			action.accept(cu);
			return null;
		});
	}

	@Override
	public synchronized <T> @Nullable T doWithCompilationUnitR(String fileName, Function<CompilationUnit, T> action) {
		WurstModel model2 = model;
		if (model2 == null) {
			return null;
		}
		for (CompilationUnit cu : model2) {
			if (cu.getFile().equals(fileName)) {
				return action.apply(cu);
			}
		}
		return null;
	}

	@Override
	public synchronized void registerChangeListener(String fileName, CompilationUnitChangeListener listener) {
		this.changeListeners.put(fileName, listener);

	}

	@Override
	public synchronized void parse(WurstGui gui, String fileName, Reader source) {
		gui = new WurstGuiLogger();
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(gui, null, RunArgs.defaults());
		comp.setHasCommonJ(true); // we always want to have a common.j if we
									// have an eclipse plugin
		CompilationUnit cu = comp.parse(fileName, source);

		IFile file = nature.getProject().getFile(fileName);
		if (file != null) {
			WurstNature.deleteMarkers(file, WurstBuilder.MARKER_TYPE_GRAMMAR);
		}
		nature.renewErrorMarkers(gui, file);

		if (cu.getJassDecls().size() + cu.getPackages().size() > 0) {
			cu.setFile(fileName);
			updateModel(cu, gui);
		}
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

	public WurstNature getNature() {
		return nature;
	}

	@Override
	public synchronized void removeCompilationUnitByName(String fileName) {
		WurstModel m = model;
		if (m == null) {
			return;
		}
		ListIterator<CompilationUnit> it = m.listIterator();
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
		synchronized (this) {
			try {
				WurstModel m = model;
				if (m == null) {
					return;
				}
				comp.addImportedLibs(m);
			} catch (CompileError e) {
				gui.sendError(e);
			}
		}
	}

	@Override
	public synchronized Set<String> getDependencies() {
		return Collections.unmodifiableSet(dependencies);
	}

	@Override
	public synchronized Set<File> getDependencyWurstFiles() {
		Set<File> result = Sets.newHashSet();
		for (String dep : dependencies) {
			addDependencyWurstFiles(result, new File(dep));
		}
		return result;
	}

	private void addDependencyWurstFiles(Set<File> result, File file) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				addDependencyWurstFiles(result, child);
			}
		} else if (Utils.isWurstFile(file)) {
			result.add(file);
		}
	}

	@Override
	public synchronized void doWithModel(Consumer<@Nullable WurstModel> action) {
		action.accept(model);
	}

	@Override
	public synchronized <T> T doWithModelR(Function<@Nullable WurstModel, T> action) {
		return action.apply(model);
	}

}
