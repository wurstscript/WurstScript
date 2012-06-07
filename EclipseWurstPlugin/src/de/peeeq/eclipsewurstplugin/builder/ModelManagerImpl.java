package de.peeeq.eclipsewurstplugin.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.peeeq.eclipsewurstplugin.WurstConstants;
import de.peeeq.eclipsewurstplugin.editor.CompilationUnitChangeListener;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.WurstConfig;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.attr;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.utils.Utils;

/**
 * keeps a version of the model which is always the most recent one 
 */
public class ModelManagerImpl implements ModelManager {

	private WurstModel model;
	private final WurstNature nature;
	private Multimap<String, CompilationUnitChangeListener> changeListeners = HashMultimap.create();
	private boolean needsFullBuild = true;
	private List<String> dependencies = Lists.newArrayList();
	
	public ModelManagerImpl(WurstNature nature) {
		this.nature = nature;
	}
	
	@Override
	public void removeCompilationUnit(IResource resource) {
		if (model == null) {
			return;
		}
		ListIterator<CompilationUnit> it = model.listIterator();
		while (it.hasNext()) {
			CompilationUnit cu = it.next();
			if (cu.getFile().equals(resource.getName())) {
				it.remove();
				break;
			}
		}

	}

	@Override
	public boolean needsFullBuild() {
		return needsFullBuild;
	}

	@Override
	public void clean() {
		model = null;
		needsFullBuild = true;
	}
	
	@Override
	public void typeCheckModel(WurstGui gui) {
		System.out.println("#typechecking");
		if (needsFullBuild) {
			System.out.println("needs full build...");
		}
		if (gui.getErrorCount() > 0) {
			return;
		}
		model.clearAttributes();
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(gui, RunArgs.defaults());
		WurstConfig.get().setSetting("lib", Utils.join(dependencies, ";"));
		try {
			comp.addImportedLibs(model);		
			comp.checkProg(model);
		} catch (CompileError e) {
			attr.addError(e.getSource(), e.getMessage());
		}
		nature.clearMarkers();
		for (CompileError e : gui.getErrorList()) {
			System.out.println("typecheck error: " + e);
			WurstNature.addErrorMarker(getFile(e.getSource().getFile()), e);
		}
	}
	
	private IFile getFile(String file) {
		return nature.getProject().getFile(file);
	}
	
	@Override
	public void updateModel(CompilationUnit cu, WurstGui gui) {
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
		System.out.print("model updated: ");
		for (CompilationUnit c : model) {
			System.out.print(c.getFile() +", ");
		}
		System.out.println();
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
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(gui, RunArgs.defaults());
		InputStreamReader reader = new InputStreamReader(source);
		CompilationUnit cu = comp.parse(reader, fileName);
		cu.setFile(fileName);
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
	public void registerChangeListener(String fileName, CompilationUnitChangeListener listener) {
		this.changeListeners.put(fileName, listener);
		
	}

	@Override
	public CompilationUnit parse(WurstGui gui, String fileName, Reader source) {
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(gui, RunArgs.defaults());
		comp.setHasCommonJ(true); // we always want to have a common.j if we have an eclipse plugin
		CompilationUnit cu = comp.parse(source, fileName);
		if (cu != null && cu.getJassDecls().size() + cu.getPackages().size() > 0) {
			cu.setFile(fileName);
			updateModel(cu, gui);
		}
		return cu;
	}

	@Override
	public void fullBuildDone() {
		needsFullBuild = false;
	}

	@Override
	public void addDependency(File f) {
		dependencies.add(f.getAbsolutePath());
	}
	
}
