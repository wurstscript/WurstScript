package de.peeeq.wurstio.languageserver;

import com.google.common.base.Charsets;
import com.google.common.collect.*;
import com.google.common.io.Files;
import de.peeeq.wurstio.ModelChangedException;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.utils.FileUtils;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.SyntacticSugar;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CofigOverridePackages;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.validation.GlobalCaches;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.lsp4j.PublishDiagnosticsParams;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * keeps a version of the model which is always the most recent one
 */
public class ModelManagerImpl implements ModelManager {

    @Override
    public void reportBuildDiagnostics(List<CompileError> diagnostics) {
        Set<WFile> affected = new LinkedHashSet<>(buildDiagnostics.keySet());
        buildDiagnostics.clear();
        for (CompileError diagnostic : diagnostics) {
            WFile file = WFile.create(diagnostic.getSource().getFile());
            buildDiagnostics.computeIfAbsent(file, ignored -> new ArrayList<>()).add(diagnostic);
            affected.add(file);
        }
        for (WFile file : affected) {
            List<CompileError> combined = new ArrayList<>(otherErrors.getOrDefault(file,
                parseErrors.getOrDefault(file, Collections.emptyList())));
            combined.addAll(buildDiagnostics.getOrDefault(file, Collections.emptyList()));
            publishDiagnostics(file, combined);
        }
    }

    private final BufferManager bufferManager;
    private volatile @Nullable WurstModel model;
    private final File projectPath;
    // dependency project folders discovered in _build/dependencies
    private final Set<File> dependencies = Sets.newLinkedHashSet();
    // private WurstGui gui = new WurstGuiLogger();
    private final List<Consumer<PublishDiagnosticsParams>> onCompilationResultListeners = new ArrayList<>();
    // compile errors for each file
    private final Map<WFile, List<CompileError>> parseErrors = new LinkedHashMap<>();
    // other errors for each file
    private final Map<WFile, List<CompileError>> otherErrors = new LinkedHashMap<>();
    // Map-build diagnostics must not change the editor model's typecheck/error state.
    private final Map<WFile, List<CompileError>> buildDiagnostics = new LinkedHashMap<>();

    // hashcode for each compilation unit content as string
    private final Map<WFile, Integer> fileHashcodes = new HashMap<>();

    // file for each compilation unit; also read by lookups from other threads
    private final Map<CompilationUnit, WFile> compilationunitFile = Collections.synchronizedMap(new WeakHashMap<>());

    // Guards the compilation unit list of the model: the language worker adds, replaces, removes and purges
    // units while other threads look them up (getCompilationUnit). Every change to that list is made here,
    // under this lock, and the lock is held only around the list operations.
    private final Object modelLock = new Object();

    // The compilation units which were added, replaced or invalidated, or whose check was planned, and which
    // no check has validated since. A check which stops before validation (an import which does not resolve)
    // leaves its units here, so the next check takes them along. Guarded by modelLock.
    private final Set<CompilationUnit> uncheckedUnits = Collections.newSetFromMap(new IdentityHashMap<>());

    public ModelManagerImpl(File projectPath, BufferManager bufferManager) {
        this.projectPath = projectPath;
        this.bufferManager = bufferManager;
    }

    private WurstModel newModel(CompilationUnit cu, WurstGui gui) {
        try {
            CompilationUnit commonJ = compileFromJar(gui, "common.j");
            CompilationUnit blizzardJ = compileFromJar(gui, "blizzard.j");
            return Ast.WurstModel(blizzardJ, commonJ, cu);
        } catch (IOException e) {
            WLogger.severe(e);
            return Ast.WurstModel(cu);
        }
    }

    private List<CompilationUnit> getJassdocCUs(Path jassdoc, WurstGui gui) {
        ArrayList<CompilationUnit> units = new ArrayList<>();
        WurstCompilerJassImpl comp = new WurstCompilerJassImpl(projectPath, gui, null, RunArgs.defaults());

        File[] jassdocFiles = jassdoc.toFile().listFiles();
        if (jassdocFiles == null) {
            WLogger.warning("Could not list jassdoc folder " + jassdoc);
        } else {
            Arrays.sort(jassdocFiles, Comparator.comparing(File::getName));
            for (File f : jassdocFiles) {
                if (f.getName().endsWith(".j") && ! f.getName().startsWith("builtin-types")) {
                    try (InputStreamReader reader = new FileReader(f)) {
                        CompilationUnit cu = comp.parse(f.getAbsolutePath(), reader);
                        cu.getCuInfo().setFile(getCanonicalPath(f));
                        units.add(cu);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return units;
    }

    @Override
    public Changes removeCompilationUnit(WFile resource) {
        WurstModel model2 = model;
        List<CompilationUnit> toRemove = new ArrayList<>();
        if (model2 != null) {
            for (CompilationUnit compilationUnit : model2) {
                if (wFile(compilationUnit).equals(resource)) {
                    toRemove.add(compilationUnit);
                }
            }
            GlobalCaches.clearLookupCacheFor(toRemove);
            toRemove.forEach(SyntacticSugar::restoreDirectFieldIterations);
            Set<String> removedPackages = toRemove.stream()
                .flatMap(cu -> cu.getPackages().stream())
                .map(WPackage::getName)
                .collect(Collectors.toSet());
            Set<CompilationUnit> dependents = toRemove.isEmpty() ? Collections.emptySet()
                : calculateCUsToUpdate(Collections.emptyList(), removedPackages, model2);
            synchronized (modelLock) {
                model2.removeAll(toRemove);
                uncheckedUnits.removeAll(toRemove);
                dependents.removeAll(toRemove);
                uncheckedUnits.addAll(dependents);
                if (toRemove.stream().anyMatch(cu -> cu.getCuInfo().getFile().endsWith(".j"))) {
                    // Jass names are visible everywhere
                    uncheckedUnits.addAll(model2);
                }
            }
        }

        // Always clear state and diagnostics for removed files.
        clearFileState(resource);
        reportErrors("remove cu ", resource, Collections.emptyList());

        toRemove.forEach(compilationunitFile::remove);

        return new Changes(
            java.util.Collections.singletonList(resource),
            toRemove.stream()
                .flatMap(cu -> cu.getPackages().stream())
                .map(WPackage::getName)
                .collect(Collectors.toList())
        );
    }

    @Override
    public void clean() {
        fileHashcodes.clear();
        parseErrors.clear();
        model = null;
        synchronized (modelLock) {
            uncheckedUnits.clear();
        }
        dependencies.clear();
        WLogger.info("Clean done.");
    }

    /**
     * reads the whole directory, which leaves every unit unchecked
     */
    @Override
    public void loadProject() {
        try {
            readDependencies();

            if (!projectPath.exists()) {
                throw new RuntimeException("Folder " + projectPath + " does not exist!");
            }

            File wurstFolder = new File(projectPath, "wurst");
            if (!wurstFolder.exists()) {
                System.err.println("No wurst folder found, using complete directory instead.");
                wurstFolder = projectPath;
            }
            processWurstFiles(wurstFolder);
        } catch (Exception e) {
            WLogger.severe(e);
            throw new ModelManagerException(e);
        }
    }

    @Override
    public void checkProject() {
        try {
            WurstGui gui = new WurstGuiLogger();
            resolveImports(gui);

            doTypeCheck(gui);
        } catch (Exception e) {
            WLogger.severe(e);
            throw new ModelManagerException(e);
        }
    }

    private void processWurstFiles(File dir) {
        for (File f : getFiles(dir)) {
            if (f.isDirectory()) {
                processWurstFiles(f);
            } else if (f.getName().endsWith(".wurst") || f.getName().endsWith(".jurst") || f.getName().endsWith(".j")) {
                processWurstFile(WFile.create(f));
            }
        }
    }

    private File[] getFiles(File dir) {
        File[] res = dir.listFiles();
        if (res == null) {
            return new File[0];
        }
        Arrays.sort(res, Comparator.comparing(File::getName));
        return res;
    }

    private void processWurstFile(WFile f) {
        WLogger.debug("processing file " + f);
        replaceCompilationUnit(f);
    }

    private void readDependencies() {
        dependencies.clear();
        WurstCompilerJassImpl.addDependenciesFromFolder(projectPath, dependencies);
    }

    @Override
    public void refreshDependencies() {
        readDependencies();
    }

    @Override
    public synchronized Changes syncDependencyCompilationUnits() {
        readDependencies();

        WurstModel model2 = model;
        if (model2 == null) {
            return Changes.empty();
        }

        Set<WFile> expectedDependencyFiles = getDependencyWurstFiles().stream()
            .map(WFile::create)
            .collect(Collectors.toSet());

        List<WFile> loadedDependencyFiles = model2.stream()
            .map(this::wFile)
            .filter(this::isUnderDependenciesFolder)
            .collect(Collectors.toList());

        Changes changes = Changes.empty();

        for (WFile loadedFile : loadedDependencyFiles) {
            if (!expectedDependencyFiles.contains(loadedFile)) {
                changes = changes.mergeWith(removeCompilationUnit(loadedFile));
            }
        }

        for (WFile dependencyFile : expectedDependencyFiles) {
            changes = changes.mergeWith(syncCompilationUnit(dependencyFile));
        }

        return changes;
    }

    private String getCanonicalPath(File f) {
        try {
            return f.getCanonicalPath();
        } catch (IOException e) {
            WLogger.info(e);
            // fall back to absolute path
            return f.getAbsolutePath();
        }
    }

    private List<CompilationUnit> getCompilationUnits(List<WFile> fileNames) {
        WurstModel model2 = model;
        if (model2 == null) {
            return Collections.emptyList();
        }
        List<CompilationUnit> list = new ArrayList<>();
        synchronized (modelLock) {
            for (CompilationUnit cu : model2) {
                if (fileNames.contains(wFile(cu))) {
                    list.add(cu);
                }
            }
        }
        return list;
    }

    private List<WFile> getfileNames(Collection<CompilationUnit> compilationUnits) {
        List<WFile> list = new ArrayList<>();
        for (CompilationUnit compilationUnit : compilationUnits) {
            WFile wFile = wFile(compilationUnit);
            list.add(wFile);
        }
        return list;
    }

    /**
     * clear the attributes and module instantiations for all compilation units in the given collection
     */
    private void clearCompilationUnits(Collection<CompilationUnit> toCheck) {
        WurstModel model2 = model;
        if (model2 == null) {
            return;
        }
        model2.clearAttributesLocal();
        for (CompilationUnit cu : toCheck) {
            clearCompilationUnit(cu);
        }
    }

    private void clearCompilationUnit(CompilationUnit cu) {
        SyntacticSugar.restoreDirectFieldIterations(cu);
        cu.clearAttributes();
        // clear module instantiations
        for (WPackage p : cu.getPackages()) {
            for (WEntity elem : p.getElements()) {
                if (elem instanceof ClassOrModuleInstanciation classOrModuleInstanciation) {
                    clearModuleInstantiation(classOrModuleInstanciation);
                }
            }
        }
    }

    private void clearModuleInstantiation(ClassOrModuleInstanciation elem) {
        elem.getP_moduleInstanciations().clear();
        for (ClassDef innerClass : elem.getInnerClasses()) {
            clearModuleInstantiation(innerClass);
        }
    }

    /**
     * check whether cu imports something from 'toCheck'
     */
    private boolean imports(CompilationUnit cu, Set<String> packageNames) {
        for (WPackage p : cu.getPackages()) {
            if (imports(p, packageNames, false, Sets.newHashSet())) {
                return true;
            }
        }

        return false;
    }

    /**
     * check whether p imports something from 'toCheck'
     */
    private boolean imports(WPackage p, Set<String> packageNames, boolean onlyPublic, HashSet<WPackage> visited) {
        if (visited.contains(p)) {
            return false;
        }
        visited.add(p);
        for (WImport imp : p.getImports()) {
            if ((!onlyPublic || imp.getIsPublic()) && packageNames.contains(imp.getPackagename())) {
                return true;
            } else {
                WPackage importedPackage = imp.attrImportedPackage();
                if ((!onlyPublic || imp.getIsPublic())
                        && importedPackage != null
                        && imports(importedPackage, packageNames, true, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void doTypeCheck(WurstGui gui) {
        WurstCompilerJassImpl comp = getCompiler(gui);
        long time = System.currentTimeMillis();
        if (gui.getErrorCount() > 0) {
            reportErrorsForProject("build project, doTypecheck, early", gui);
            WLogger.info("finished typechecking* in " + (System.currentTimeMillis() - time) + "ms");
            return;
        }
        @Nullable
        WurstModel model2 = model;
        if (model2 == null) {
            return;
        }

        try {
            model2.clearAttributes();
            comp.addImportedLibs(model2, this::addCompilationUnit);
            synchronized (modelLock) {
                uncheckedUnits.addAll(model2);
            }
            if (comp.checkProg(model2)) {
                synchronized (modelLock) {
                    uncheckedUnits.clear();
                }
            }
        } catch (CompileError e) {
            gui.sendError(e);
        }
        WLogger.info("finished typechecking in " + (System.currentTimeMillis() - time) + "ms");
        reportErrorsForProject("build project, doTypecheck, end", gui);
    }

    private CompilationUnit addCompilationUnit(File file) {
        WFile wFile = WFile.create(file);
        try {
            String contents = new String(java.nio.file.Files.readAllBytes(file.toPath()), UTF_8);
            return replaceCompilationUnit(wFile, contents, true);
        } catch (IOException e) {
            WLogger.severe(e);
            return null;
        }
    }

    private void reportErrorsForProject(String extra, WurstGui gui) {
        Multimap<WFile, CompileError> typeErrors = ArrayListMultimap.create();
        for (CompileError e : gui.getErrorsAndWarnings()) {
            typeErrors.put(WFile.create(e.getSource().getFile()), e);
        }
        Set<WFile> files = ImmutableSet.<WFile>builder()
            .addAll(parseErrors.keySet())
            .addAll(typeErrors.keySet())
            .build();
        for (WFile file : files) {
            List<CompileError> errors = ImmutableList.<CompileError>builder()
                .addAll(parseErrors.getOrDefault(file, Collections.emptyList()))
                .addAll(typeErrors.get(file))
                .build();
            reportErrors(extra, file, errors);
        }
    }

    private void reportErrorsForFiles(List<WFile> filenames, WurstGui gui) {
        Multimap<WFile, CompileError> typeErrors = ArrayListMultimap.create();
        for (CompileError e : gui.getErrorsAndWarnings()) {
            typeErrors.put(WFile.create(e.getSource().getFile()), e);
        }

        for (WFile file : filenames) {
            List<CompileError> errors = new ArrayList<>(parseErrors.getOrDefault(file, Collections.emptyList()));
            errors.addAll(typeErrors.get(file));
            reportErrors("partial ", file, errors);
        }
    }

    private void reportErrors(String extra, WFile filename, List<CompileError> errors) {
        buildDiagnostics.remove(filename);
        otherErrors.put(filename, ImmutableList.copyOf(errors));
        publishDiagnostics(filename, errors);
    }

    private void publishDiagnostics(WFile filename, List<CompileError> errors) {
        PublishDiagnosticsParams cr = Convert.createDiagnostics("", filename, errors);
        for (Consumer<PublishDiagnosticsParams> consumer : onCompilationResultListeners) {
            consumer.accept(cr);
        }
    }

    private WurstCompilerJassImpl getCompiler(WurstGui gui) {
        RunArgs runArgs = RunArgs.defaults();
        runArgs.addLibDirs(dependencies);
        WurstCompilerJassImpl comp = new WurstCompilerJassImpl(projectPath, gui, null, runArgs);
        comp.setHasCommonJ(true);
        return comp;
    }

    private void updateModel(CompilationUnit cu, WurstGui gui) {
        parseErrors.put(wFile(cu), new ArrayList<>(gui.getErrorsAndWarnings()));

        WurstModel model2 = model;
        if (model2 == null) {
            model = newModel(cu, gui);
            synchronized (modelLock) {
                uncheckedUnits.addAll(model);
            }
        } else {
            ListIterator<CompilationUnit> it = model2.listIterator();
            boolean updated = false;
            while (it.hasNext()) {
                CompilationUnit c = it.next();
                if (wFile(c).equals(wFile(cu))) {
                    // get old provided packages:
                    Set<String> oldPackages = providedPackages(c);
                    Set<CompilationUnit> mustUpdate = calculateCUsToUpdate(Collections.singletonList(cu), oldPackages, model2);

                    GlobalCaches.clearLookupCacheFor(Collections.singletonList(c));
                    clearCompilationUnits(mustUpdate);
                    // replace old compilationunit with new one:
                    synchronized (modelLock) {
                        it.set(cu);
                        uncheckedUnits.remove(c);
                        uncheckedUnits.addAll(mustUpdate);
                    }
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                synchronized (modelLock) {
                    model2.add(cu);
                }
                // what imports the new packages (or sees the new Jass names) has to be checked again
                Set<CompilationUnit> mustUpdate = calculateCUsToUpdate(Collections.singletonList(cu), Collections.emptySet(), model2);
                synchronized (modelLock) {
                    uncheckedUnits.addAll(mustUpdate);
                }
            }
        }
        //doTypeCheckPartial(gui, false, ImmutableList.of(cu.getFile()));
    }

    private Set<String> providedPackages(CompilationUnit c) {
        Set<String> set = new HashSet<>();
        for (WPackage wPackage : c.getPackages()) {
            String name = wPackage.getName();
            set.add(name);
        }
        return set;
    }

    private CompilationUnit compileFromJar(WurstGui gui, String filename) throws IOException {
        File sourceFile = findProjectCoreJassFile(filename).orElse(null);
        if (sourceFile != null) {
            sourceFile = copyCoreJassToBuildRoot(sourceFile, filename);
        } else {
            InputStream source = this.getClass().getResourceAsStream("/" + filename);
            if (source == null) {
                WLogger.severe("could not find " + filename + " in jar");
                System.err.println("could not find " + filename + " in jar");
                sourceFile = new File("./resources/" + filename);
            } else {
                try {
                    File buildDir = getBuildDir();
                    //noinspection ResultOfMethodCallIgnored
                    buildDir.mkdirs();
                    sourceFile = new File(buildDir, filename);
                    java.nio.file.Files.copy(source, sourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    WLogger.info("Loaded bundled fallback " + filename);
                } finally {
                    source.close();
                }
            }
        }

        WurstCompilerJassImpl comp = getCompiler(gui);

        try (InputStreamReader reader = new FileReader(sourceFile)) {
            CompilationUnit cu = comp.parse(sourceFile.getAbsolutePath(), reader);
            cu.getCuInfo().setFile(getCanonicalPath(sourceFile));
            return cu;
        }
    }

    private Optional<File> findProjectCoreJassFile(String filename) {
        File projectCopy = new File(getBuildDir(), filename);
        if (projectCopy.exists()) {
            return Optional.of(projectCopy);
        }
        return Optional.empty();
    }

    private File copyCoreJassToBuildRoot(File sourceFile, String filename) throws IOException {
        File buildDir = getBuildDir();
        //noinspection ResultOfMethodCallIgnored
        buildDir.mkdirs();
        File buildCopy = new File(buildDir, filename);
        if (!sourceFile.getCanonicalFile().equals(buildCopy.getCanonicalFile())) {
            java.nio.file.Files.copy(sourceFile.toPath(), buildCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        WLogger.info("Loaded project " + filename + " from " + sourceFile.getAbsolutePath());
        return buildCopy;
    }

    private File getBuildDir() {
        return new File(projectPath, "_build");
    }

    private void resolveImports(WurstGui gui) {
        WurstCompilerJassImpl comp = getCompiler(gui);
        try {
            WurstModel m = model;
            if (m == null) {
                return;
            }
            m.clearAttributes();
            comp.addImportedLibs(m, this::addCompilationUnit);
        } catch (CompileError e) {
            gui.sendError(e);
        }
    }

    private void replaceCompilationUnit(WFile filename) {
        try {
            String contents = readCompilationUnitContents(filename, true);
            if (contents == null) {
                return;
            }
            replaceCompilationUnit(filename, contents, true);
        } catch (IOException e) {
            WLogger.severe(e);
            throw new ModelManagerException(e);
        }
    }


    @Override
    public Changes syncCompilationUnitContent(WFile filename, String contents) {
        WLogger.debug("sync contents for " + filename);
        int newHash = contentHash(contents);
        Integer oldHash = fileHashcodes.get(filename);
        CompilationUnit existing = getCompilationUnit(filename);
        if (oldHash != null && oldHash == newHash && existing != null) {
            // No content change and CU still present -> skip expensive reconcile/typecheck work.
            return Changes.empty();
        }
        Set<String> oldPackages = declaredPackages(filename);
        replaceCompilationUnit(filename, contents, false);
        return new Changes(io.vavr.collection.HashSet.of(filename), oldPackages);
    }

    private Set<String> declaredPackages(WFile f) {
        WurstModel model = this.model;
        if (model == null) {
            return Collections.emptySet();
        }
        for (CompilationUnit cu : model) {
            if (wFile(cu).equals(f)) {
                Set<String> set = new HashSet<>();
                for (WPackage wPackage : cu.getPackages()) {
                    String name = wPackage.getName();
                    set.add(name);
                }
                return set;
            }
        }
        return Collections.emptySet();
    }

    @Override
    public CompilationUnit replaceCompilationUnitContent(WFile filename, String contents, boolean reportErrors) {
        return replaceCompilationUnit(filename, contents, reportErrors);
    }


    @Override
    public Changes syncCompilationUnit(WFile f) {
        WLogger.debug("syncCompilationUnit File " + f);
        String contents;
        try {
            contents = readCompilationUnitContents(f, true);
            if (contents == null) {
                // A watcher can report a change for a path that has already
                // disappeared (for example, during a move). Keep the removal
                // changes so dependents of the old packages are reconciled.
                return removeCompilationUnit(f);
            }
        } catch (IOException e) {
            WLogger.severe(e);
            throw new ModelManagerException(e);
        }
        int newHash = contentHash(contents);
        Integer oldHash = fileHashcodes.get(f);
        CompilationUnit existing = getCompilationUnit(f);
        if (oldHash != null && oldHash == newHash && existing != null) {
            WLogger.trace(() -> "syncCompilationUnit no-op for " + f);
            return Changes.empty();
        }
        Set<String> oldPackages = declaredPackages(f);
        replaceCompilationUnit(f, contents, true);
        WLogger.debug("replaced file " + f);
        WurstGui gui = new WurstGuiLogger();
        doTypeCheckPartial(gui, ImmutableList.of(f), oldPackages);
        return new Changes(io.vavr.collection.HashSet.of(f), oldPackages);
    }

    private @Nullable String readCompilationUnitContents(WFile filename, boolean preferOpenBuffer) throws IOException {
        if (preferOpenBuffer && bufferManager.getTextDocumentVersion(filename) >= 0) {
            return bufferManager.getBuffer(filename);
        }
        File file;
        try {
            file = filename.getFile();
        } catch (FileNotFoundException e) {
            WLogger.info("Cannot read compilation unit for " + filename + "\n" + e);
            return null;
        }
        if (!file.exists()) {
            return null;
        }
        String contents = Files.toString(file, Charsets.UTF_8);
        if (bufferManager.getTextDocumentVersion(filename) < 0) {
            bufferManager.updateFile(WFile.create(file), contents);
        }
        return contents;
    }

    private CompilationUnit replaceCompilationUnit(WFile filename, String contents, boolean reportErrors) {
        if (!isInWurstFolder(filename) && !isAlreadyLoaded(filename)) {
            return null;
        }
        int newHash = contentHash(contents);
        if (fileHashcodes.containsKey(filename)) {
            int oldHash = fileHashcodes.get(filename);
            if (oldHash == newHash) {
                CompilationUnit existing = getCompilationUnit(filename);
                if (existing != null) {
                    // no change
                    WLogger.trace(() -> "CU " + filename + " was unchanged.");
                    return existing;
                }
                // Stale hash cache after remove/move; CU is gone, so reparse.
                WLogger.debug("CU hash unchanged but model entry missing for " + filename + ", reparsing.");
            } else {
                WLogger.debug("CU changed. oldHash = " + oldHash + " == " + newHash);
            }
        }

        WLogger.trace(() -> "replace CU " + filename);
        WurstGui gui = new WurstGuiLogger();
        WurstCompilerJassImpl c = getCompiler(gui);
        CompilationUnit cu = c.parse(filename.toString(), new StringReader(contents));
        cu.getCuInfo().setFile(filename.toString());
        if (isUnderDependenciesFolder(filename)) {
            cu.getCuInfo().setLibrary(true);
        }
        updateModel(cu, gui);
        fileHashcodes.put(filename, newHash);
        if (reportErrors) {
            if (gui.getErrorCount() > 0) {
                WLogger.debug("found " + gui.getErrorCount() + " errors in file " + filename);
            }
            ImmutableList.Builder<CompileError> errors = ImmutableList.<CompileError>builder()
                .addAll(gui.getErrorsAndWarnings());

            if (otherErrors.containsKey(filename)) {
                errors.addAll(otherErrors.get(filename));
            }

            reportErrors("sync cu " + filename, filename, errors.build());
        }
        return cu;
    }

    private void clearFileState(WFile file) {
        parseErrors.remove(file);
        otherErrors.remove(file);
        fileHashcodes.remove(file);
    }

    @Override
    public CompilationUnit getCompilationUnit(WFile filename) {
        List<CompilationUnit> matches = getCompilationUnits(Collections.singletonList(filename));
        if (matches.isEmpty()) {
            WLogger.trace("compilation unit not found: " + filename);
            return null;
        }
        return matches.get(0);
    }

    @Override
    public WurstModel getModel() {
        return model;
    }

    @Override
    public void retainCompilationUnits(WurstModel model, Predicate<CompilationUnit> keep) {
        synchronized (modelLock) {
            boolean removed = model.removeIf(cu -> !keep.test(cu));
            if (removed && model == this.model) {
                uncheckedUnits.removeIf(cu -> !keep.test(cu));
                // what is left may have used what went: it all has to be checked again
                uncheckedUnits.addAll(model);
            }
        }
    }

    @Override
    public void markFullyChecked(WurstModel checkedModel) {
        synchronized (modelLock) {
            if (checkedModel != null && checkedModel == model) {
                uncheckedUnits.clear();
            }
        }
    }

    @Override
    public boolean isFullyChecked(WurstModel checkedModel) {
        synchronized (modelLock) {
            return checkedModel != null && checkedModel == model && uncheckedUnits.isEmpty();
        }
    }

    @Override
    public boolean hasErrors() {
        return errorStream().findAny().isPresent();
    }


    @Override
    public String getFirstErrorDescription() {
        Optional<CompileError> first = errorStream().findFirst();
        return first.map(CompileError::toString).orElse("no errors");
    }


    @Override
    public List<CompileError> getParseErrors() {
        return parseErrorStream().collect(Collectors.toList());
    }

    private Stream<CompileError> parseErrorStream() {
        return parseErrors.values().stream()
                .flatMap(Collection::stream)
                .filter(err -> err.getErrorType() == CompileError.ErrorType.ERROR);
    }

    private Stream<CompileError> otherErrorStream() {
        return otherErrors.values().stream()
                .flatMap(Collection::stream)
                .filter(err -> err.getErrorType() == CompileError.ErrorType.ERROR);
    }

    private Stream<CompileError> errorStream() {
        return Streams.concat(parseErrorStream(), otherErrorStream());
    }


    @Override
    public void onCompilationResult(Consumer<PublishDiagnosticsParams> f) {
        onCompilationResultListeners.add(f);
    }

    private void doTypeCheckPartial(WurstGui gui, List<WFile> toCheckFilenames, Set<String> oldPackages) {
        WLogger.debug("do typecheck partial of " + toCheckFilenames);
        WurstCompilerJassImpl comp = getCompiler(gui);
        List<CompilationUnit> toCheck = getCompilationUnits(toCheckFilenames);

        WurstModel model2 = model;
        if (model2 == null) {
            return;
        }

        Collection<CompilationUnit> toCheckRec = calculateCUsToUpdate(toCheck, oldPackages, model2);

        partialTypecheck(model2, toCheckRec, gui, comp);
    }

    @Override
    public void reconcile(Changes changes) {
        WurstModel model2 = model;
        if (model2 == null) {
            return;
        }
        Collection<CompilationUnit> toCheck1 = new HashSet<>();
        for (CompilationUnit cu : model2) {
            if (changes.getAffectedFiles().contains(WFile.create(cu.getCuInfo().getFile()))) {
                toCheck1.add(cu);
            }
        }
        Set<String> oldPackageNames = changes.getAffectedPackageNames().toJavaSet();
        Collection<CompilationUnit> toCheckRec = calculateCUsToUpdate(toCheck1, oldPackageNames, model2);
        boolean jassFileChanged = changes.getAffectedFiles().toJavaSet().stream()
            .anyMatch(file -> file.getUriString().endsWith(".j"));
        if (jassFileChanged) {
            // A removed Jass CU is no longer in the model, so calculateCUsToUpdate
            // cannot see it among the changed compilation units.
            toCheckRec.addAll(model2);
        }
        WurstGui gui = new WurstGuiLogger();
        WurstCompilerJassImpl comp = getCompiler(gui);
        partialTypecheck(model2, toCheckRec, gui, comp);
    }

    private void partialTypecheck(WurstModel model2, Collection<CompilationUnit> toCheckRec, WurstGui gui, WurstCompilerJassImpl comp) {
        Collection<CompilationUnit> toCheck = withUncheckedUnits(model2, toCheckRec);
        boolean validated = false;
        try {
            clearCompilationUnits(toCheck);
            comp.addImportedLibs(model2, this::addCompilationUnit);
            // the libraries which were just loaded are not checked either
            toCheck = withUncheckedUnits(model2, toCheck);
            validated = comp.checkProg(model2, toCheck);
        } catch (ModelChangedException e) {
            // model changed, early return
            return;
        } catch (CompileError e) {
            gui.sendError(e);
        }
        if (validated) {
            synchronized (modelLock) {
                uncheckedUnits.removeAll(toCheck);
            }
        }
        List<WFile> fileNames = getfileNames(toCheck);
        reportErrorsForFiles(fileNames, gui);
    }

    /**
     * The given units, which are unchecked from now on, and every other unit of the model which still is
     * unchecked, because an earlier check did not get to validate it.
     */
    private Collection<CompilationUnit> withUncheckedUnits(WurstModel model2, Collection<CompilationUnit> units) {
        Set<CompilationUnit> result = new TreeSet<>(Comparator.comparing(cu -> cu.getCuInfo().getFile()));
        synchronized (modelLock) {
            uncheckedUnits.addAll(units);
            Set<CompilationUnit> inModel = Collections.newSetFromMap(new IdentityHashMap<>());
            inModel.addAll(model2);
            uncheckedUnits.retainAll(inModel);
            result.addAll(uncheckedUnits);
        }
        return result;
    }


    /**
     * Calculates compilation
     *
     *
     * @param changed the set of compilation units that were changed
     * @param oldPackages packages that were provided before the update (which might have been removed now)
     * @param model the complete AST
     * @return the set of compilation units that might be affected by the changes, including the changed compilation units
     */
    private Set<CompilationUnit> calculateCUsToUpdate(Collection<CompilationUnit> changed, Set<String> oldPackages, WurstModel model) {

        Set<CompilationUnit> result = new TreeSet<>(Comparator.comparing(cu -> cu.getCuInfo().getFile()));
        result.addAll(changed);

        boolean b = false;
        for (CompilationUnit compilationUnit : changed) {
            if (compilationUnit.getCuInfo().getFile().endsWith(".j")) {
                b = true;
                break;
            }
        }
        if (b) {
            // when plain Jass files are changed, everything must be checked again:
            result.addAll(model);
            return result;
        }

        // get packages provided by the changed CUs
        Stream<String> providedPackages = changed.stream()
                .flatMap(cu -> cu.getPackages().stream())
                .map(WPackage::getName);

        // affected packages are new ones and old ones
        Set<String> affectedPackages = Stream.concat(providedPackages, oldPackages.stream())
                .collect(Collectors.toSet());
        affectedPackages.addAll(configRelatives(affectedPackages));

        addPossiblyAffectedPackages(affectedPackages, model, result);

        return result;
    }


    /**
     * A config package replaces definitions of the package it configures, and that package decides which of its
     * definitions are replaced, though neither imports the other. What changes in one changes what the other,
     * and so everything importing the configured package, resolves to.
     */
    private static Set<String> configRelatives(Set<String> packageNames) {
        Set<String> result = new HashSet<>();
        for (String name : packageNames) {
            if (name.endsWith(CofigOverridePackages.CONFIG_POSTFIX)) {
                result.add(name.substring(0, name.length() - CofigOverridePackages.CONFIG_POSTFIX.length()));
            } else {
                result.add(name + CofigOverridePackages.CONFIG_POSTFIX);
            }
        }
        return result;
    }

    /**
     * Add all packages that directly or indirectly depend on the providedPackages
     */
    private void addPossiblyAffectedPackages(Collection<String> providedPackages, WurstModel model, Set<CompilationUnit> result) {

        nextCu:
        for (CompilationUnit compilationUnit : model) {
            if (result.contains(compilationUnit)) {
                continue;
            }
            for (WPackage p : compilationUnit.getPackages()) {
                if (providedPackages.contains(p.getName())) {
                    // declares the same package: one of the files is reported for defining it twice
                    result.add(compilationUnit);
                    continue nextCu;
                }
                for (WImport imp : p.getImports()) {
                    String importedPackage = imp.getPackagenameId().getName();
                    if (providedPackages.contains(importedPackage)) {
                        result.add(compilationUnit);
                        continue nextCu;
                    }
                }
            }
        }

        addTransitiveDeps(result, model);
        // a config package is checked with the package it configures, and what follows from either
        while (addConfigRelatives(result, model)) {
            addTransitiveDeps(result, model);
        }
    }

    /**
     * Adds the units declaring the config package of a package in result, or the package a config package in result
     * configures, see {@link #configRelatives}.
     *
     * @return whether a unit was added
     */
    private boolean addConfigRelatives(Set<CompilationUnit> result, WurstModel model) {
        Set<String> declared = new HashSet<>();
        for (CompilationUnit cu : result) {
            for (WPackage p : cu.getPackages()) {
                declared.add(p.getName());
            }
        }
        Set<String> relatives = configRelatives(declared);
        boolean added = false;
        for (CompilationUnit cu : model) {
            if (!result.contains(cu) && cu.getPackages().stream().anyMatch(p -> relatives.contains(p.getName()))) {
                result.add(cu);
                added = true;
            }
        }
        return added;
    }

    /**
     * Add all compilation units that transitively depend on the units in result
     */
    private void addTransitiveDeps(Set<CompilationUnit> result, WurstModel model) {
        Multimap<CompilationUnit, CompilationUnit> dependencyMap = calculateDirectDependencies(model);
        ArrayDeque<CompilationUnit> todo = new ArrayDeque<>(result);
        while (!todo.isEmpty()) {
            CompilationUnit cu = todo.remove();
            Collection<CompilationUnit> directDeps = dependencyMap.get(cu);
            for (CompilationUnit c : directDeps) {
                if (result.add(c)) {
                    todo.add(c);
                }
            }
        }
    }

    /**
     * Calculates a map from a compilation unit to the compilation units that directly depend on it.
     * E.g. if package A imports C and package B imports C, then
     * result.get(C) would return A and B
     **/
    private Multimap<CompilationUnit, CompilationUnit> calculateDirectDependencies(WurstModel model) {
        Multimap<CompilationUnit, CompilationUnit> result = HashMultimap.create();
        Map<String, CompilationUnit> cuForPackage = new HashMap<>();
        for (CompilationUnit cu : model) {
            for (WPackage p : cu.getPackages()) {
                cuForPackage.put(p.getName(), cu);
            }
        }

        for (CompilationUnit cu : model) {
            for (WPackage p : cu.getPackages()) {
                for (WImport i : p.getImports()) {
                    CompilationUnit dep = cuForPackage.get(i.getPackagename());
                    if (dep != null) {
                        result.put(dep, cu);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public synchronized Set<File> getDependencyWurstFiles() {
        Set<File> result = Sets.newHashSet();
        for (File dep : dependencies) {
            addDependencyWurstFiles(result, dep);
        }
        return result;
    }

    private void addDependencyWurstFiles(Set<File> result, File file) {
        if (file.isDirectory()) {
            for (File child : getFiles(file)) {
                addDependencyWurstFiles(result, child);
            }
        } else if (Utils.isWurstFile(file)) {
            result.add(file);
        }
    }

    private WFile wFile(CompilationUnit cu) {
        return compilationunitFile.computeIfAbsent(cu, c -> WFile.create(cu.getCuInfo().getFile()));
    }

    /**
     * checks if the given file is in the wurst folder, inside a dependency,
     * or a CU that has already been loaded into the model.
     */
    private boolean isInWurstFolder(WFile file) {
        return Stream.concat(Stream.of(projectPath), dependencies.stream()).anyMatch(p ->
                FileUtils.isInDirectoryTrans(file, WFile.create(new File(p, "wurst"))));

    }

    private boolean isAlreadyLoaded(WFile file) {
        WurstModel model2 = model;
        if (model2 == null) {
            return false;
        }
        for (CompilationUnit cu : model2) {
            if (wFile(cu).equals(file)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUnderDependenciesFolder(WFile file) {
        try {
            Path filePath = file.getPath().toAbsolutePath().normalize();
            Path dependencyRoot = Paths.get(projectPath.getAbsolutePath(), "_build", "dependencies")
                .toAbsolutePath()
                .normalize();
            return filePath.startsWith(dependencyRoot) && Utils.isWurstFile(filePath.toString());
        } catch (FileNotFoundException e) {
            return false;
        }
    }


    public File getProjectPath() {
        return projectPath;
    }

    private int contentHash(String s) {
        // Normalize line endings to avoid false "changed" signals between editor buffers and disk text.
        String normalized = s.replace("\r\n", "\n").replace('\r', '\n');
        return normalized.hashCode();
    }
}
