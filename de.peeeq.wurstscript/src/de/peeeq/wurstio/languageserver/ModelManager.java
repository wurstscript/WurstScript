package de.peeeq.wurstio.languageserver;

import java.io.IOException;
import java.util.function.Consumer;

public interface ModelManager {

	boolean removeCompilationUnit(String filename);

	/** synchronizes with file system 
	 * @throws IOException */
	boolean syncCompilationUnit(String filename) throws IOException;

	/** cleans the model */
	void clean();

	boolean updateCompilationUnit(String filename, String contents);

	void onCompilationResult(Consumer<CompilationResult> f);

	void buildProject() throws IOException;
	
}