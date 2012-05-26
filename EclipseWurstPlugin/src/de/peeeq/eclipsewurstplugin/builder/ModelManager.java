package de.peeeq.eclipsewurstplugin.builder;

import java.io.Reader;

import org.eclipse.core.resources.IResource;

import de.peeeq.eclipsewurstplugin.editor.CompilationUnitChangeListener;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.gui.WurstGui;

public interface ModelManager {

	public abstract void removeCompilationUnit(IResource resource);

	public abstract boolean needsFullBuild();

	public abstract void clean();

	void typeCheckModel(WurstGui gui);

	void updateModel(CompilationUnit cu, WurstGui gui);

	public abstract CompilationUnit getCompilationUnit(String fileName);

	public abstract void registerChangeListener(String fileName, CompilationUnitChangeListener listener);

	public abstract void parse(WurstGui gui, String fileName, Reader source);

}