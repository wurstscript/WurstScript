package de.peeeq.wurstio.languageserver;

import java.io.IOException;
import java.util.function.Consumer;

public interface ModelManager {

	boolean removeCompilationUnit(String filename);

	/** synchronizes with file system 
	 * @throws IOException */
	void syncCompilationUnit(String filename);

	/** cleans the model */
	void clean();

	void updateCompilationUnit(String filename, String contents, boolean reportErrors);

	void onCompilationResult(Consumer<CompilationResult> f);

	void buildProject();
	
}