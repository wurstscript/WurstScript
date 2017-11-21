package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import org.eclipse.lsp4j.PublishDiagnosticsParams;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface ModelManager {

    boolean removeCompilationUnit(WFile filename);

    /**
     * synchronizes with file system
     *
     * @throws IOException
     */
    void replaceCompilationUnit(WFile filename);

    /**
     * cleans the model
     */
    void clean();

    List<CompileError> getParseErrors();

    void updateCompilationUnit(WFile filename, String contents, boolean reportErrors);

    void onCompilationResult(Consumer<PublishDiagnosticsParams> f);

    void buildProject();

    void syncCompilationUnit(WFile changedFilePath);

    void syncCompilationUnitContent(WFile filename, String contents);

    CompilationUnit replaceCompilationUnitContent(WFile filename, String contents, boolean reportErrors);

    /**
     * get all wurst files in dependency folders
     */
    Set<File> getDependencyWurstFiles();

    CompilationUnit getCompilationUnit(WFile filename);

    WurstModel getModel();

    boolean hasErrors();
}