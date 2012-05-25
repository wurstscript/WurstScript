package de.peeeq.eclipsewurstplugin.builder;

import org.eclipse.core.resources.IResource;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.gui.WurstGui;

public interface ModelManager {

	public abstract void removeCompilationUnit(IResource resource);

	public abstract boolean needsFullBuild();

	public abstract void clean();

	void typeCheckModel(WurstGui gui);

	void updateModel(CompilationUnit cu, WurstGui gui);

}