package de.peeeq.wurstio.languageserver;

import com.google.common.collect.ImmutableSet;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ModuleInstanciations;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import io.vavr.collection.HashSet;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.lsp4j.PublishDiagnosticsParams;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface ModelManager {

    Changes removeCompilationUnit(WFile filename);

    /**
     * cleans the model
     */
    void clean();

    List<CompileError> getParseErrors();

    void onCompilationResult(Consumer<PublishDiagnosticsParams> f);

    void buildProject();

    Changes syncCompilationUnit(WFile changedFilePath);

    Changes syncCompilationUnitContent(WFile filename, String contents);

    CompilationUnit replaceCompilationUnitContent(WFile filename, String buffer, boolean reportErrors);

    /**
     * get all wurst files in dependency folders
     */
    Set<File> getDependencyWurstFiles();

    @Nullable CompilationUnit getCompilationUnit(WFile filename);

    WurstModel getModel();

    boolean hasErrors();

    static WurstModel copy(WurstModel model) {
        WurstModel m = model.copy();
        // clear all module instantiations, since they might include old stuff
        m.accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(ModuleInstanciations mis) {
                super.visit(mis);
                mis.clear();
            }
        });
        return m;
    }

    File getProjectPath();

    String getFirstErrorDescription();

    /** clean and typecheck the given files */
    void reconcile(Changes changes);

    class Changes {
        private static final Changes EMPTY = new Changes(HashSet.empty(), HashSet.empty());
        private final HashSet<WFile> affectedFiles;
        private final HashSet<String> affectedPackageNames;

        public Changes(Iterable<WFile> affectedFiles, Iterable<String> affectedPackageNames) {
            this.affectedFiles = HashSet.ofAll(affectedFiles);
            this.affectedPackageNames = HashSet.ofAll(affectedPackageNames);
        }

        public Changes(Stream<WFile> affectedFiles, Stream<String> affectedPackageNames) {
            this.affectedFiles = HashSet.ofAll(affectedFiles);
            this.affectedPackageNames = HashSet.ofAll(affectedPackageNames);
        }

        public static Changes empty() {
            return EMPTY;
        }

        public HashSet<WFile> getAffectedFiles() {
            return affectedFiles;
        }

        public HashSet<String> getAffectedPackageNames() {
            return affectedPackageNames;
        }

        public Changes mergeWith(Changes affected) {
            HashSet<WFile> newF = affectedFiles.addAll(affected.affectedFiles);
            HashSet<String> newP = affectedPackageNames.addAll(affected.affectedPackageNames);
            if (newF == affectedFiles && newP == affectedPackageNames) {
                return this;
            }
            return new Changes(newF, newP);
        }

        public boolean isEmpty() {
            return affectedFiles.isEmpty() && affectedPackageNames.isEmpty();
        }
    }

}
