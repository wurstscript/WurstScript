package de.peeeq.eclipsewurstplugin.builder;

import java.io.Reader;

import org.eclipse.core.resources.IResource;

import de.peeeq.eclipsewurstplugin.editor.CompilationUnitChangeListener;
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

	@Override
	public CompilationUnit getCompilationUnit(String fileName) {
		return null;
	}

	@Override
	public void registerChangeListener(String fileName, CompilationUnitChangeListener listener) {
	}

	@Override
	public void parse(WurstGui gui, String fileName, Reader source) {
		
	}

	@Override
	public void fullBuildDone() {
	}

}
