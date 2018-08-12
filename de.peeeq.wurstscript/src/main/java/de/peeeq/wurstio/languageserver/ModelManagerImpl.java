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
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.lsp4j.PublishDiagnosticsParams;

import java.io.*;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Override
    public boolean removeCompilationUnit(WFile resource) {
        parseErrors.remove(resource);
        WurstModel model2 = model;
        if (model2 == null) {
            return false;
        }
        ListIterator<CompilationUnit> it = model2.listIterator();
        while (it.hasNext()) {
            CompilationUnit cu = it.next();
            if (wFile(cu).equals(resource)) {
                clearAttributes(Collections.singletonList(cu));
                it.remove();
                return true;
            }
        }
        return false;
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
        try (BufferedReader reader = new BufferedReader(new FileReader(depFile))) {
            LineOffsets lineOffsets = new LineOffsets();
            int offset = 0;
            int lineNr = 0;
            while (true) {
                String line = reader.readLine();
                lineNr++;
                lineOffsets.set(lineNr, offset);
                if (line == null)
                    break;
                int endOffset = offset + line.length();
                addDependency(gui, depFile, line, lineOffsets, offset, endOffset);
                offset = endOffset;
            }
        }
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

    private void addDependency(WurstGui gui, File depfile, String fileName, LineOffsets lineOffsets, int offset, int endOffset) {
        WLogger.info("Adding dependency: " + fileName);
        File f = new File(fileName);
        WPos pos = new WPos(getCanonicalPath(depfile), lineOffsets, offset, endOffset);
        if (!f.exists()) {
            gui.sendError(new CompileError(pos, "Path '" + fileName + "' could not be found."));
            return;
        } else if (!f.isDirectory()) {
            gui.sendError(new CompileError(pos, "Path '" + fileName + "' is not a folder."));
            return;
        }

        dependencies.add(new File(fileName));
    }

    private List<CompilationUnit> getCompilationUnits(List<WFile> fileNames) {
        WurstModel model2 = model;
        if (model2 == null) {
            return Collections.emptyList();
        }
        return model2.stream().filter(cu -> fileNames.contains(wFile(cu))).collect(Collectors.toList());
    }

    private List<WFile> getfileNames(List<CompilationUnit> compilationUnits) {
        return compilationUnits.stream()
                .map(this::wFile)
                .collect(Collectors.toList());
    }

    /**
     * clear the attributes for all compilation units that import something from
     * 'toCheck' and for 'toCheck'
     *
     * @return a list of all the CUs which have been cleared
     */
    private List<CompilationUnit> clearAttributes(List<CompilationUnit> toCheck) {
        WurstModel model2 = model;
        if (model2 == null) {
            return Collections.emptyList();
        }
        List<CompilationUnit> cleared = new ArrayList<>(toCheck);
        model2.clearAttributesLocal();
        Set<String> packageNames = Sets.newHashSet();
        for (CompilationUnit cu : toCheck) {
            cu.clearAttributes();
            for (WPackage p : cu.getPackages()) {
                packageNames.add(p.getName());
            }
        }
        for (CompilationUnit cu : model2) {
            if (imports(cu, packageNames)) {
                cu.clearAttributes();
                cleared.add(cu);
            }
        }
        return cleared;
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
    private boolean imports(WPackage p, Set<String> packageNames, boolean importPublic, HashSet<WPackage> visited) {
        if (visited.contains(p)) {
            return false;
        }
        visited.add(p);
        for (WImport imp : p.getImports()) {
            if ((!importPublic || imp.getIsPublic()) && packageNames.contains(imp.getPackagename())) {
                return true;
            } else {
                WPackage importedPackage = imp.attrImportedPackage();
                if (imp.getIsPublic() && importedPackage != null && imports(importedPackage, packageNames, true, visited)) {
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
            comp.addImportedLibs(model2);
            comp.checkProg(model2);
        } catch (CompileError e) {
            gui.sendError(e);
        }
        WLogger.info("finished typechecking in " + (System.currentTimeMillis() - time) + "ms");
        reportErrorsForProject("build project, doTypecheck, end", gui);
    }

    private void reportErrorsForProject(String extra, WurstGui gui) {
        Multimap<WFile, CompileError> typeErrors = ArrayListMultimap.create();
        for (CompileError e : gui.getErrorsAndWarnings()) {
            typeErrors.put(WFile.create(e.getSource().getFile()), e);
        }

        for (WFile file : parseErrors.keySet()) {
            List<CompileError> errors = new ArrayList<>(parseErrors.getOrDefault(file, Collections.emptyList()));
            errors.addAll(typeErrors.get(file));
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
        WurstCompilerJassImpl comp = new WurstCompilerJassImpl(gui, null, runArgs);
        comp.setHasCommonJ(true);
        return comp;
    }

    private void updateModel(CompilationUnit cu, WurstGui gui) {
        WLogger.info("update model with " + cu.getFile());
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
                    clearAttributes(Collections.singletonList(cu));
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

        WurstCompilerJassImpl comp = new WurstCompilerJassImpl(gui, null, RunArgs.defaults());

        try (InputStreamReader reader = new FileReader(sourceFile)) {
            CompilationUnit cu = comp.parse(sourceFile.getAbsolutePath(), reader);
            cu.setFile(getCanonicalPath(sourceFile));
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
            comp.addImportedLibs(m);
        } catch (CompileError e) {
            gui.sendError(e);
        }
    }

    @Override
    public void replaceCompilationUnit(WFile filename) {
        File f = filename.getFile();
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
    public void syncCompilationUnitContent(WFile filename, String contents) {
        WLogger.info("sync contents for " + filename);
        replaceCompilationUnit(filename, contents, true);
        WurstGui gui = new WurstGuiLogger();
        doTypeCheckPartial(gui, ImmutableList.of(filename));
    }

    @Override
    public CompilationUnit replaceCompilationUnitContent(WFile filename, String contents, boolean reportErrors) {
        return replaceCompilationUnit(filename, contents, reportErrors);
    }


    @Override
    public void syncCompilationUnit(WFile f) {
        WLogger.info("syncCompilationUnit File " + f);
        replaceCompilationUnit(f);
        WLogger.info("replaced file " + f);
        WurstGui gui = new WurstGuiLogger();
        doTypeCheckPartial(gui, ImmutableList.of(f));
    }

    private CompilationUnit replaceCompilationUnit(WFile filename, String contents, boolean reportErrors) {
        if (!isInWurstFolder(filename)) {
            return null;
        }
        if (fileHashcodes.containsKey(filename)) {
            int oldHash = fileHashcodes.get(filename);
            WLogger.info("oldHash = " + oldHash + " == " + contents.hashCode());
            if (oldHash == contents.hashCode()) {
                // no change
                WLogger.info("CU " + filename + " was unchanged.");
                return getCompilationUnit(filename);
            }
        }

        WLogger.info("replace CU " + filename);
        WurstGui gui = new WurstGuiLogger();
        WurstCompilerJassImpl c = getCompiler(gui);
        CompilationUnit cu = c.parse(filename.toString(), new StringReader(contents));
        cu.setFile(filename.toString());
        updateModel(cu, gui);
        fileHashcodes.put(filename, contents.hashCode());
        if (reportErrors) {
            WLogger.info("found " + gui.getErrorCount() + " errors in file " + filename);
            reportErrors("sync cu " + filename, filename, gui.getErrorsAndWarnings());
        }
        return cu;
    }

    @Override
    public CompilationUnit getCompilationUnit(WFile filename) {
        List<CompilationUnit> matches = getCompilationUnits(Collections.singletonList(filename));
        if (matches.isEmpty()) {
            WLogger.info("compilation unit not found: " + filename);
            WLogger.info("available: " + model.stream().map(CompilationUnit::getFile).collect(Collectors.joining(", ")));
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
    public void updateCompilationUnit(WFile filename, String contents, boolean reportErrors) {
        replaceCompilationUnit(filename, contents, reportErrors);
    }

    @Override
    public void onCompilationResult(Consumer<PublishDiagnosticsParams> f) {
        onCompilationResultListeners.add(f);
    }

    private void doTypeCheckPartial(WurstGui gui, List<WFile> toCheckFilenames) {
        WLogger.info("do typecheck partial of " + toCheckFilenames);
        WurstCompilerJassImpl comp = getCompiler(gui);
        List<CompilationUnit> toCheck = getCompilationUnits(toCheckFilenames);

        WurstModel model2 = model;
        if (model2 == null) {
            return;
        }

        toCheck = new ArrayList<>(addPackageDependencies(toCheck, model2));

        List<CompilationUnit> clearedCUs = Collections.emptyList();
        try {
            clearedCUs = clearAttributes(toCheck);
            comp.addImportedLibs(model2);
            comp.checkProg(model2, toCheck);
        } catch (ModelChangedException e) {
            // model changed, early return
            return;
        } catch (CompileError e) {
            gui.sendError(e);
        }
        List<WFile> fileNames = getfileNames(clearedCUs);
        reportErrorsForFiles(fileNames, gui);
    }

    private Set<CompilationUnit> addPackageDependencies(List<CompilationUnit> toCheck, WurstModel model) {

        Set<CompilationUnit> result = new TreeSet<>(Comparator.comparing(CompilationUnit::getFile));
        result.addAll(toCheck);

        if (toCheck.stream().anyMatch(cu -> cu.getFile().endsWith(".j"))) {
            // when plain Jass files are changed, everything must be checked again:
            result.addAll(model);
            return result;
        }

        Collection<String> providedPackages = result.stream().flatMap(cu -> cu.getPackages().stream()).map(WPackage::getName).collect(Collectors.toSet());

        addImportingPackages(providedPackages, model, result);

        return result;
    }

    private void addImportingPackages(Collection<String> providedPackages, WurstModel model2, Set<CompilationUnit> result) {
        nextCu:
        for (CompilationUnit compilationUnit : model2) {
            if (result.contains(compilationUnit)) {
                continue;
            }
            for (WPackage p : compilationUnit.getPackages()) {
                for (WImport imp : p.getImports()) {
                    String importedPackage = imp.getPackagenameId().getName();
                    if (providedPackages.contains(importedPackage)) {
                        result.add(compilationUnit);
                        if (imp.getIsPublic()) {
                            // recursive call terminates, because it is only called for packages not yet in result 
                            addImportingPackages(Collections.singletonList(p.getName()), model2, result);
                        }
                        continue nextCu;
                    }
                }
            }
        }
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
        return compilationunitFile.computeIfAbsent(cu, c -> WFile.create(cu.getFile()));
    }

    /**
     * checks if the given file is in the wurst folder or inside a dependency
     */
    private boolean isInWurstFolder(WFile file) {
        return Stream.concat(Stream.of(projectPath), dependencies.stream()).anyMatch(p ->
                FileUtils.isInDirectoryTrans(file, WFile.create(new File(p, "wurst"))));

    }


}
