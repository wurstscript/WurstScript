package de.peeeq.eclipsewurstplugin.builder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ListIterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;

/**
 * keeps a version of the model which is always the most recent one 
 */
public class ModelManagerImpl implements ModelManager {

	private WurstModel model;
	private final WurstNature nature;
	
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
		return model == null;
	}

	@Override
	public void clean() {
		model = null;
	}
	
	@Override
	public void typeCheckModel(WurstGui gui) {
		System.out.println("#typechecking");
		if (gui.getErrorCount() > 0) {
			return;
		}
		model.clearAttributes();
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(gui, RunArgs.defaults());
		comp.checkProg(model);
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
	}

	private WurstModel newModel(CompilationUnit cu, WurstGui gui) {
		Bundle bundle = Platform.getBundle("EclipseWurstPlugin");
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
	
}
