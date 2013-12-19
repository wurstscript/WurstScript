package de.peeeq.eclipsewurstplugin.builder;

import java.io.File;
import java.io.Reader;

import org.eclipse.core.resources.IResource;

import de.peeeq.eclipsewurstplugin.editor.CompilationUnitChangeListener;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGui;

public interface ModelManager {

	 boolean removeCompilationUnit(IResource resource);

	 boolean needsFullBuild();

	 void clean();

	 void typeCheckModel(WurstGui gui, boolean addErrorMarkers, boolean refreshAttributes);
	 
	 void updateModel(CompilationUnit cu, WurstGui gui);

	 CompilationUnit getCompilationUnit(String fileName);

	 void registerChangeListener(String fileName, CompilationUnitChangeListener listener);

	 CompilationUnit parse(WurstGui gui, String fileName, Reader source);

	 void fullBuildDone();

	void addDependency(File f);

	void clearDependencies();

	WurstModel getModel();
	
	WurstNature getNature();

	void removeCompilationUnitByName(String replDummyFilename);

}