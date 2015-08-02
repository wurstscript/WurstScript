package de.peeeq.eclipsewurstplugin.builder;

import java.io.File;
import java.io.Reader;
import java.util.Collections;
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

/*
 * model manager which does nothing
 */
public class ModelManagerStub implements ModelManager {

	@Override
	public boolean removeCompilationUnit(IResource resource) {
		return false;
	}

	@Override
	public boolean needsFullBuild() {
		return false;
	}

	@Override
	public void clean() {
	}

	@Override
	public void updateModel(CompilationUnit cu, WurstGui gui) {
	}

	@Override
	public void registerChangeListener(String fileName, CompilationUnitChangeListener listener) {
	}

	@Override
	public void fullBuildDone() {
	}

	@Override
	public void addDependency(File f) {
	}

	@Override
	public void clearDependencies() {

	}

	@Override
	public WurstNature getNature() {
		return null;
	}

	@Override
	public void removeCompilationUnitByName(String replDummyFilename) {
	}

	@Override
	public void resolveImports(WurstGui gui) {
	}

	@Override
	public void typeCheckModel(WurstGui gui, boolean addErrorMarkers) {
	}

	@Override
	public void typeCheckModelPartial(WurstGui gui, boolean addErrorMarkers, List<String> toCheck) {
	}

	@Override
	public Set<String> getDependencies() {
		return Collections.emptySet();
	}

	@Override
	public Set<File> getDependencyWurstFiles() {
		return Collections.emptySet();
	}

	@Override
	public void doWithCompilationUnit(String fileName, Consumer<CompilationUnit> action) {
	}

	@Override
	public void parse(WurstGui gui, String fileName, Reader source) {
	}

	@Override
	public void doWithModel(Consumer<@Nullable WurstModel> action) {
	}

	@Override
	public <T> T doWithCompilationUnitR(String fileName, Function<CompilationUnit, T> action) {
		return null;
	}

	@Override
	public <T> T doWithModelR(Function<@Nullable WurstModel, T> action) {
		return null;
	}

}
