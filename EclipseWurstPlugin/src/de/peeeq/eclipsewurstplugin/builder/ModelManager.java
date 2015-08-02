package de.peeeq.eclipsewurstplugin.builder;

import java.io.File;
import java.io.Reader;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.eclipsewurstplugin.editor.CompilationUnitChangeListener;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGui;

public interface ModelManager {

	 boolean removeCompilationUnit(IResource resource);

	 boolean needsFullBuild();

	 void clean();

	 void typeCheckModel(WurstGui gui, boolean addErrorMarkers);
	 
	 void typeCheckModelPartial(WurstGui gui, boolean addErrorMarkers, List<String> toCheckFilenames);
	 
	 void updateModel(CompilationUnit cu, WurstGui gui);

	 void doWithCompilationUnit(String fileName, Consumer<CompilationUnit> action);
	 <T> @Nullable T doWithCompilationUnitR(String fileName, Function<CompilationUnit, T> action);

	 void registerChangeListener(String fileName, CompilationUnitChangeListener listener);

	 void parse(WurstGui gui, String fileName, Reader source);

	 void fullBuildDone();

	void addDependency(File f);

	void clearDependencies();

	void doWithModel(Consumer<@Nullable WurstModel> action);
	
	<T> @Nullable T doWithModelR(Function<@Nullable WurstModel,T> action);
	
	WurstNature getNature();

	void removeCompilationUnitByName(String replDummyFilename);

	void resolveImports(WurstGui gui);

	Set<String> getDependencies();

	Set<@NonNull File> getDependencyWurstFiles();

}