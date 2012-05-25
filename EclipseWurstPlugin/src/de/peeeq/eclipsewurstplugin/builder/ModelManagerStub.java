package de.peeeq.eclipsewurstplugin.builder;

import org.eclipse.core.resources.IResource;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.gui.WurstGui;
/*
 * model manager which does nothing
 */
public class ModelManagerStub implements ModelManager {

	@Override
	public void removeCompilationUnit(IResource resource) {
	}

	@Override
	public boolean needsFullBuild() {
		return false;
	}

	@Override
	public void clean() {
	}

	@Override
	public void typeCheckModel(WurstGui gui) {
	}

	@Override
	public void updateModel(CompilationUnit cu, WurstGui gui) {
	}

}
