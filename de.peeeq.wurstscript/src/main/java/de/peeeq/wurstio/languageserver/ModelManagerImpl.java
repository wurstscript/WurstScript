package de.peeeq.wurstio.languageserver;

import com.google.common.base.Charsets;
import com.google.common.collect.*;
import com.google.common.io.Files;
import de.peeeq.wurstio.ModelChangedException;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.utils.FileUtils;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.lsp4j.PublishDiagnosticsParams;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * keeps a version of the model which is always the most recent one
 */
public class ModelManagerImpl implements ModelManager {

    private final BufferManager bufferManager;
    private volatile @Nullable WurstModel model;
    private File projectPath;
    // dependency folders (folders mentioned in wurst.dependencies)
    private final Set<File> dependencies = Sets.newLinkedHashSet();
    // private WurstGui gui = new WurstGuiLogger();
    private List<Consumer<PublishDiagnosticsParams>> onCompilationResultListeners = new ArrayList<>();
    // compile errors for each file
    private Map<WFile, List<CompileError>> parseErrors = new LinkedHashMap<>();
    // other errors for each file
    private Map<WFile, List<CompileError>> otherErrors = new LinkedHashMap<>();

    // hashcode for each compilation unit content as string
    private Map<WFile, Integer> fileHashcodes = new HashMap<>();

    // file for each compilation unit
    private WeakHashMap<CompilationUnit, WFile> compilationunitFile = new WeakHashMap<>();

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

        for (File f : jassdoc.toFile().listFiles()) {
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

        return units;
    }

    @Override
    public Changes removeCompilationUnit(WFile resource) {
        parseErrors.remove(resource);
        WurstModel model2 = model;
        if (model2 == null) {
            return Changes.empty();
        }

        syncCompilationUnitContent(resource, "");
        List<CompilationUnit> toRemove = model2.stream()
            .filter(cu -> wFile(cu).equals(resource))
            .collect(Collectors.toList());
        model2.removeAll(toRemove);
        return new Changes(toRemove.stream()
            .map(this::wFile),
            toRemove.stream()
                .flatMap(cu -> cu.getPackages().stream())
                .map(WPackage::getName));
    }

    @Override
    public void clean() {
        fileHashcodes.clear();
        parseErrors.clear();
        model = null;
        dependencies.clear();
        WLogger.info("Clean done.");
    }

    /**
     * does a full build, reading whole directory
     */
    @Override
    public void buildProject() {
        try {
            WurstGui gui = new WurstGuiLogger();
            readDependencies(gui);

            if (!projectPath.exists()) {
                throw new RuntimeException("Folder " + projectPath + " does not exist!");
            }

            File wurstFolder = new File(projectPath, "wurst");
            if (!wurstFolder.exists()) {
                System.err.println("No wurst folder found, using complete directory instead.");
                wurstFolder = projectPath;
            }
            processWurstFiles(wurstFolder);

            resolveImports(gui);

            doTypeCheck(gui);
        } catch (IOException e) {
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
        return res;
    }

    private void processWurstFile(WFile f) {
        WLogger.info("processing file " + f);
        replaceCompilationUnit(f);
    }

    private void readDependencies(WurstGui gui) throws IOException {
        dependencies.clear();
        File depFile = new File(projectPath, "wurst.dependencies");
        if (!depFile.exists()) {
            WLogger.info("no dependency file found.");
            return;
        }
        dependencies.addAll(WurstCompilerJassImpl.checkDependencyFile(depFile, gui));
        WurstCompilerJassImpl.addDependenciesFromFolder(projectPath, dependencies);
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
        return model2.stream().filter(cu -> fileNames.contains(wFile(cu))).collect(Collectors.toList());
    }

    private List<WFile> getfileNames(Collection<CompilationUnit> compilationUnits) {
        return compilationUnits.stream()
                .map(this::wFile)
                .collect(Collectors.toList());
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
        cu.clearAttributes();
        // clear module instantiations
        for (WPackage p : cu.getPackages()) {
            for (WEntity elem : p.getElements()) {
                if (elem instanceof ClassOrModuleInstanciation) {
                    clearModuleInstantiation(((ClassOrModuleInstanciation) elem));
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
            comp.checkProg(model2);
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
        PublishDiagnosticsParams cr = Convert.createDiagnostics(extra, filename, errors);
        otherErrors.put(filename, ImmutableList.copyOf(errors));
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
        WLogger.trace("update model with " + cu.getCuInfo().getFile());
        parseErrors.put(wFile(cu), new ArrayList<>(gui.getErrorsAndWarnings()));

        WurstModel model2 = model;
        if (model2 == null) {
            model = newModel(cu, gui);
        } else {
            ListIterator<CompilationUnit> it = model2.listIterator();
            boolean updated = false;
            while (it.hasNext()) {
                CompilationUnit c = it.next();
                if (wFile(c).equals(wFile(cu))) {
                    // get old provided packages:
                    Set<String> oldPackages = providedPackages(c);
                    Set<CompilationUnit> mustUpdate = calculateCUsToUpdate(Collections.singletonList(cu), oldPackages, model2);

                    clearCompilationUnits(mustUpdate);
                    // replace old compilationunit with new one:
                    it.set(cu);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                model2.add(cu);
            }
        }
        //doTypeCheckPartial(gui, false, ImmutableList.of(cu.getFile()));
    }

    private Set<String> providedPackages(CompilationUnit c) {
        return c.getPackages()
            .stream()
            .map(WPackage::getName)
            .collect(Collectors.toSet());
    }

    private CompilationUnit compileFromJar(WurstGui gui, String filename) throws IOException {
        InputStream source = this.getClass().getResourceAsStream("/" + filename);
        File sourceFile;
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
                if (!sourceFile.exists()) {
                    java.nio.file.Files.copy(source, sourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                source.close();
            }
        }

        WurstCompilerJassImpl comp = getCompiler(gui);

        try (InputStreamReader reader = new FileReader(sourceFile)) {
            CompilationUnit cu = comp.parse(sourceFile.getAbsolutePath(), reader);
            cu.getCuInfo().setFile(getCanonicalPath(sourceFile));
            return cu;
        }
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
        File f;
        try {
            f = filename.getFile();
        } catch (FileNotFoundException e) {
            WLogger.info("Cannot replaceCompilationUnit for " + filename + "\n" + e);
            return;
        }
        if (!f.exists()) {
            removeCompilationUnit(filename);
            return;
        }
        try {
            String contents = Files.toString(f, Charsets.UTF_8);
            bufferManager.updateFile(WFile.create(f), contents);
            replaceCompilationUnit(filename, contents, true);
            WLogger.info("replaceCompilationUnit 3 " + f);
        } catch (IOException e) {
            WLogger.severe(e);
            throw new ModelManagerException(e);
        }
    }


    @Override
    public Changes syncCompilationUnitContent(WFile filename, String contents) {
        WLogger.info("sync contents for " + filename);
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
                return cu.getPackages()
                        .stream()
                        .map(WPackage::getName)
                        .collect(Collectors.toSet());
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
        WLogger.info("syncCompilationUnit File " + f);
        Set<String> oldPackages = declaredPackages(f);
        replaceCompilationUnit(f);
        WLogger.info("replaced file " + f);
        WurstGui gui = new WurstGuiLogger();
        doTypeCheckPartial(gui, ImmutableList.of(f), oldPackages);
        return new Changes(io.vavr.collection.HashSet.of(f), oldPackages);
    }

    private CompilationUnit replaceCompilationUnit(WFile filename, String contents, boolean reportErrors) {
        if (!isInWurstFolder(filename)) {
            return null;
        }
        if (fileHashcodes.containsKey(filename)) {
            int oldHash = fileHashcodes.get(filename);
            if (oldHash == contents.hashCode()) {
                // no change
                WLogger.trace("CU " + filename + " was unchanged.");
                return getCompilationUnit(filename);
            } else {
                WLogger.info("CU changed. oldHash = " + oldHash + " == " + contents.hashCode());
            }
        }

        WLogger.trace("replace CU " + filename);
        WurstGui gui = new WurstGuiLogger();
        WurstCompilerJassImpl c = getCompiler(gui);
        CompilationUnit cu = c.parse(filename.toString(), new StringReader(contents));
        cu.getCuInfo().setFile(filename.toString());
        updateModel(cu, gui);
        fileHashcodes.put(filename, contents.hashCode());
        if (reportErrors) {
            if (gui.getErrorCount() > 0) {
                WLogger.info("found " + gui.getErrorCount() + " errors in file " + filename);
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

    @Override
    public CompilationUnit getCompilationUnit(WFile filename) {
        List<CompilationUnit> matches = getCompilationUnits(Collections.singletonList(filename));
        if (matches.isEmpty()) {
            WLogger.info("compilation unit not found: " + filename);
            return null;
        }
        return matches.get(0);
    }

    @Override
    public WurstModel getModel() {
        return model;
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
        WLogger.info("do typecheck partial of " + toCheckFilenames);
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
        Collection<CompilationUnit> toCheck1 = model2.stream()
            .filter(cu -> changes.getAffectedFiles().contains(WFile.create(cu.getCuInfo().getFile())))
            .collect(Collectors.toSet());
        Set<String> oldPackageNames = changes.getAffectedPackageNames().toJavaSet();
        Collection<CompilationUnit> toCheckRec = calculateCUsToUpdate(toCheck1, oldPackageNames, model2);
        WurstGui gui = new WurstGuiLogger();
        WurstCompilerJassImpl comp = getCompiler(gui);
        partialTypecheck(model2, toCheckRec, gui, comp);
    }

    private void partialTypecheck(WurstModel model2, Collection<CompilationUnit> toCheckRec, WurstGui gui, WurstCompilerJassImpl comp) {
        try {
            clearCompilationUnits(toCheckRec);
            comp.addImportedLibs(model2, this::addCompilationUnit);
            comp.checkProg(model2, toCheckRec);
        } catch (ModelChangedException e) {
            // model changed, early return
            return;
        } catch (CompileError e) {
            gui.sendError(e);
        }
        List<WFile> fileNames = getfileNames(toCheckRec);
        reportErrorsForFiles(fileNames, gui);
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

        if (changed.stream().anyMatch(cu -> cu.getCuInfo().getFile().endsWith(".j"))) {
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

        addPossiblyAffectedPackages(affectedPackages, model, result);

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
     * checks if the given file is in the wurst folder or inside a dependency
     */
    private boolean isInWurstFolder(WFile file) {
        return Stream.concat(Stream.of(projectPath), dependencies.stream()).anyMatch(p ->
                FileUtils.isInDirectoryTrans(file, WFile.create(new File(p, "wurst"))));

    }


    public File getProjectPath() {
        return projectPath;
    }
}
