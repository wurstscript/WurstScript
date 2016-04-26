package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstscript.ast.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;

public interface ModelManager {

	boolean removeCompilationUnit(String filename);

	/** synchronizes with file system 
	 * @throws IOException */
	void replaceCompilationUnit(String filename);

	/** cleans the model */
	void clean();

	void updateCompilationUnit(String filename, String contents, boolean reportErrors);

	void onCompilationResult(Consumer<CompilationResult> f);

	void buildProject();

	void syncCompilationUnit(String changedFilePath);

	void syncCompilationUnitContent(String filename, String contents);

	CompilationUnit replaceCompilationUnitContent(String filename, String buffer);

	/** get all wurst files in dependency folders */
	Set<File> getDependencyWurstFiles();
}