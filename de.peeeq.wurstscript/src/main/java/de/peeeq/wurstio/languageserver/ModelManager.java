package de.peeeq.wurstio.languageserver;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ModuleInstanciations;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import io.vavr.collection.HashSet;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.lsp4j.PublishDiagnosticsParams;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface ModelManager {

    Changes removeCompilationUnit(WFile filename);

    /**
     * cleans the model
     */
    void clean();

    List<CompileError> getParseErrors();

    /**
     * The warnings the parser reported for the units of the model, such as inconsistent indentation. No check of the
     * model reports them again.
     */
    List<CompileError> getParseWarnings();

    void onCompilationResult(Consumer<PublishDiagnosticsParams> f);

    void reportBuildDiagnostics(List<CompileError> diagnostics);

    /**
     * does a full build, reading the whole project and checking it
     */
    default void buildProject() {
        loadProject();
        checkProject();
    }

    /**
     * Reads the whole project into the model, and does not check it: every unit of the model counts as unchecked
     * until {@link #checkProject()}. For a caller which changes the model before its first check, so that the
     * model is checked once and not once for each version of it.
     */
    void loadProject();

    /**
     * Resolves the imports of the loaded model, checks all of it and reports the errors.
     */
    default void checkProject() {
        checkProject(new WurstGuiLogger());
    }

    /**
     * The same, with the errors and warnings of the check sent to {@code gui}: for a caller which shows them itself,
     * as the command line build does.
     */
    void checkProject(WurstGui gui);

    /**
     * refresh discovered dependency roots (e.g. _build/dependencies after grill install)
     */
    void refreshDependencies();

    /**
     * refresh and synchronize all dependency compilation units.
     * This handles dependency delete/replace/move/rename scenarios robustly.
     */
    Changes syncDependencyCompilationUnits();

    Changes syncCompilationUnit(WFile changedFilePath);

    Changes syncCompilationUnitContent(WFile filename, String contents);

    CompilationUnit replaceCompilationUnitContent(WFile filename, String buffer, boolean reportErrors);

    /**
     * get all wurst files in dependency folders
     */
    Set<File> getDependencyWurstFiles();

    @Nullable CompilationUnit getCompilationUnit(WFile filename);

    WurstModel getModel();

    /**
     * Removes the compilation units that do not satisfy {@code keep} from the given model, which is the
     * managed model or a copy of it. The managed model is only changed through the manager, so that
     * lookups from other threads stay safe.
     */
    void retainCompilationUnits(WurstModel model, Predicate<CompilationUnit> keep);

    boolean hasErrors();

    /**
     * Whether the model is the managed one and every compilation unit in it was validated since it last
     * changed, so that checking it again would only repeat the result. A manager which does not track
     * this answers false.
     */
    default boolean isFullyChecked(WurstModel model) {
        return false;
    }

    /**
     * Tells the manager that someone else's check of the whole model validated every unit of it and found
     * no error, with the same checks the manager makes. Nothing may have changed the model meanwhile.
     */
    default void markFullyChecked(WurstModel model) {
    }

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
        private final boolean jassNamesChanged;

        public Changes(Iterable<WFile> affectedFiles, Iterable<String> affectedPackageNames) {
            this(affectedFiles, affectedPackageNames, false);
        }

        /**
         * @param jassNamesChanged whether a unit which declared Jass names was replaced or removed (see
         *                         {@link #isJassNamesChanged()})
         */
        public Changes(Iterable<WFile> affectedFiles, Iterable<String> affectedPackageNames, boolean jassNamesChanged) {
            this.affectedFiles = HashSet.ofAll(affectedFiles);
            this.affectedPackageNames = HashSet.ofAll(affectedPackageNames);
            this.jassNamesChanged = jassNamesChanged;
        }

        public Changes(Stream<WFile> affectedFiles, Stream<String> affectedPackageNames) {
            this.affectedFiles = HashSet.ofAll(affectedFiles);
            this.affectedPackageNames = HashSet.ofAll(affectedPackageNames);
            this.jassNamesChanged = false;
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

        /**
         * Whether a unit which declared Jass names (a .j file, or Jass outside of the packages of a .wurst or .jurst
         * file) was replaced or removed. Every package sees those names without an import, so every unit has to be
         * checked again, and a removed unit is not in the model any more to tell.
         */
        public boolean isJassNamesChanged() {
            return jassNamesChanged;
        }

        public Changes mergeWith(Changes affected) {
            HashSet<WFile> newF = affectedFiles.addAll(affected.affectedFiles);
            HashSet<String> newP = affectedPackageNames.addAll(affected.affectedPackageNames);
            boolean newJ = jassNamesChanged || affected.jassNamesChanged;
            if (newF == affectedFiles && newP == affectedPackageNames && newJ == jassNamesChanged) {
                return this;
            }
            return new Changes(newF, newP, newJ);
        }

        public boolean isEmpty() {
            return affectedFiles.isEmpty() && affectedPackageNames.isEmpty() && !jassNamesChanged;
        }
    }

}
