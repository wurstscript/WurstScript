package de.peeeq.wurstio.languageserver;

import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import de.peeeq.wurstio.ModelChangedException;
import de.peeeq.wurstio.WurstCompilerJassImpl;
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

import java.io.*;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * keeps a version of the model which is always the most recent one
 */
public class ModelManagerImpl implements ModelManager {

	private volatile @Nullable WurstModel model;
	private File projectPath;
	private boolean needsFullBuild = true;
	// dependency folders (folders mentioned in wurst.dependencies)
	private final Set<String> dependencies = Sets.newLinkedHashSet();
	// private WurstGui gui = new WurstGuiLogger();
	private List<Consumer<CompilationResult>> onCompilationResultListeners = new ArrayList<>();
	// compile errors for each file
	private Map<String, List<CompileError>> parseErrors = new LinkedHashMap<>();

	// hashcode for each compilation unit content as string
	private Map<String, Integer> fileHashcodes = new HashMap<>();


	public ModelManagerImpl(String projectPath) {
		this(new File(projectPath));
	}

	public ModelManagerImpl(File projectPath) {
		this.projectPath = projectPath;
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
	public boolean removeCompilationUnit(String resource) {
		parseErrors.remove(resource);
		WurstModel model2 = model;
		if (model2 == null) {
			return false;
		}
		ListIterator<CompilationUnit> it = model2.listIterator();
		while (it.hasNext()) {
			CompilationUnit cu = it.next();
			if (cu.getFile().equals(resource)) {
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
		needsFullBuild = true;
		WLogger.info("Clean done.");
	}

	/**
	 * does a full build, reading whole directory
	 *
	 * @throws IOException
	 */
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

			doTypeCheck(gui, true);
			// TODO report errors

			needsFullBuild = false;
			// Map<String, List<CompileError>> groupedByFile =
			// gui.getErrorsAndWarnings().stream().collect(Collectors.groupingBy(err
			// -> err.getSource().getFile()));
			// for (Entry<String, List<CompileError>> err :
			// groupedByFile.entrySet()) {
			// CompilationResult r = CompilationResult.create(err.getKey(),
			// err.getValue());
			// onCompilationResultListeners.forEach(c -> c.accept(r));
			// }
		} catch (IOException e) {
			WLogger.severe(e);
			throw new ModelManagerException(e);
		}
	}



	private void processWurstFiles(File dir) throws IOException {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				processWurstFiles(f);
			} else if (f.getName().endsWith(".wurst") || f.getName().endsWith(".jurst")) {
				processWurstFile(f);
			}
		}
	}

	private void processWurstFile(File f) throws IOException {
		System.out.println("processing file " + f);
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

	}

	private String getProjectRelativePath(File f) {
		Path pathRelative = projectPath.getAbsoluteFile().toPath().relativize(f.getAbsoluteFile().toPath());
		return pathRelative.toString();
	}

	private void addDependency(WurstGui gui, File depfile, String fileName, LineOffsets lineOffsets, int offset,
							   int endOffset) {
		WLogger.info("Adding dependency: " + fileName);
		File f = new File(fileName);
		WPos pos = new WPos(getProjectRelativePath(depfile), lineOffsets, offset, endOffset);
		if (!f.exists()) {
			gui.sendError(new CompileError(pos, "Path '" + fileName + "' could not be found."));
			return;
		} else if (!f.isDirectory()) {
			gui.sendError(new CompileError(pos, "Path '" + fileName + "' is not a folder."));
			return;
		}

		dependencies.add(fileName);
	}

	private List<CompilationUnit> getCompilationUnits(List<String> fileNames) {
		WurstModel model2 = model;
		if (model2 == null) {
			return Collections.emptyList();
		}
		return model2.stream().filter(cu -> fileNames.contains(cu.getFile())).collect(Collectors.toList());
	}

	private List<String> getfileNames(List<CompilationUnit> compilationUnits) {
		return compilationUnits.stream().map(CompilationUnit::getFile).collect(Collectors.toList());
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
			if (imports(cu, packageNames, false)) {
				cu.clearAttributes();
				cleared.add(cu);
			}
		}
		return cleared;
	}

	/**
	 * check whether cu imports something from 'toCheck'
	 */
	private boolean imports(CompilationUnit cu, Set<String> packageNames, boolean importPublic) {
		for (WPackage p : cu.getPackages()) {
			if (imports(p, packageNames, false, Sets.<WPackage>newHashSet())) {
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
				if (imp.getIsPublic() && importedPackage != null) {
					if (imports(importedPackage, packageNames, true, visited)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void doTypeCheck(WurstGui gui, boolean addErrorMarkers) {
		WurstCompilerJassImpl comp = getCompiler(gui);
		long time = System.currentTimeMillis();
		if (gui.getErrorCount() > 0) {
			if (addErrorMarkers) {
				reportErrorsForProject("build project, doTypecheck, early", gui);
			}
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
		if (addErrorMarkers) {
			reportErrorsForProject("build project, doTypecheck, end", gui);
		}
	}

	private void reportErrorsForProject(String extra, WurstGui gui) {
		Multimap<String, CompileError> typeErrors = ArrayListMultimap.create();
		for (CompileError e : gui.getErrorsAndWarnings()) {
			typeErrors.put(e.getSource().getFile(), e);
		}

		for (String file : parseErrors.keySet()) {
			List<CompileError> errors = new ArrayList<>(parseErrors.get(file));
			errors.addAll(typeErrors.get(file));
			reportErrors(extra, file, errors);
		}
	}

	private void reportErrorsForFiles(String extra, List<String> filenames, WurstGui gui) {
		Multimap<String, CompileError> typeErrors = ArrayListMultimap.create();
		for (CompileError e : gui.getErrorsAndWarnings()) {
			typeErrors.put(e.getSource().getFile(), e);
		}

		for (String file : filenames) {
			List<CompileError> errors = new ArrayList<>(parseErrors.get(file));
			errors.addAll(typeErrors.get(file));
			reportErrors(extra, file, errors);
		}
	}

	private void reportErrors(String extra, String filename, List<CompileError> errors) {
		CompilationResult cr = CompilationResult.create(extra, filename, errors);
		for (Consumer<CompilationResult> consumer : onCompilationResultListeners) {
			consumer.accept(cr);
		}
	}

	private WurstCompilerJassImpl getCompiler(WurstGui gui) {
		RunArgs runArgs = RunArgs.defaults();
		runArgs.addLibs(dependencies);
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(gui, null, runArgs);
		comp.setHasCommonJ(true);
		return comp;
	}

	private void updateModel(CompilationUnit cu, WurstGui gui) {
		System.out.println("update model with " + cu.getFile());
		parseErrors.put(cu.getFile(), new ArrayList<>(gui.getErrorsAndWarnings()));


		WurstModel model2 = model;
		if (model2 == null) {
			model = model2 = newModel(cu, gui);
		} else {
			ListIterator<CompilationUnit> it = model2.listIterator();
			boolean updated = false;
			while (it.hasNext()) {
				CompilationUnit c = it.next();
				if (c.getFile().equals(cu.getFile())) {
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
			System.err.println("could not find " + filename + " in jar");
			sourceFile = new File("./resources/" + filename);
		} else {
			sourceFile = File.createTempFile("wurst", filename);
			java.nio.file.Files.copy(source, sourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}


		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(gui, null, RunArgs.defaults());

		try (InputStreamReader reader = new FileReader(sourceFile)) {
			CompilationUnit cu = comp.parse(sourceFile.getAbsolutePath(), reader);
			cu.setFile(sourceFile.getAbsolutePath());
			return cu;
		}
	}

	private void resolveImports(WurstGui gui) {
		WurstCompilerJassImpl comp = getCompiler(gui);
		try {
			WurstModel m = model;
			if (m == null) {
				return;
			}
			comp.addImportedLibs(m);
		} catch (CompileError e) {
			gui.sendError(e);
		}
	}

	@Override
	public void replaceCompilationUnit(String filename) {
		try {
			File f = getFile(filename);
			replaceCompilationUnit(f);
		} catch (IOException e) {
			WLogger.severe(e);
			throw new ModelManagerException(e);
		}
	}

	/**
	 * get a file object, where relative files are resolved with respect to
	 * the project root
	 */
	private File getFile(String filename) {
		File f = new File(filename);
		if (f.isAbsolute()) {
			return f;
		}
		return new File(projectPath, filename);
	}

	private void replaceCompilationUnit(File f) throws IOException {
		if (!f.exists()) {
			removeCompilationUnit(getProjectRelativePath(f));
			return;
		}
		WLogger.info("replaceCompilationUnit 1 " + f);
		String filename = getProjectRelativePath(f);
		WLogger.info("replaceCompilationUnit 2 " + f);
		String contents = Files.toString(f, Charsets.UTF_8);
		replaceCompilationUnit(filename, contents, true);
		WLogger.info("replaceCompilationUnit 3 " + f);
	}

	@Override
	public void syncCompilationUnit(String filename) {
		WLogger.info("syncCompilationUnit " + filename);
		try {
			File f = getFile(filename);
			syncCompilationUnit(f);
		} catch (IOException e) {
			WLogger.severe(e);
			throw new ModelManagerException(e);
		}
	}

	@Override
	public void syncCompilationUnitContent(String filename, String contents) {
		WLogger.info("sync contents for " + filename);
		filename = normalizeFilename(filename);
		replaceCompilationUnit(filename, contents, true);
		WurstGui gui = new WurstGuiLogger();
		doTypeCheckPartial(gui, true, ImmutableList.of(filename));
	}

	@Override
	public CompilationUnit replaceCompilationUnitContent(String filename, String contents, boolean reportErrors) {
		filename = normalizeFilename(filename);
		return replaceCompilationUnit(filename, contents, reportErrors);
	}

	private String normalizeFilename(String filename) {
		File f = getFile(filename);
		filename = getProjectRelativePath(f);
		return filename;
	}

	private void syncCompilationUnit(File f) throws IOException {
		WLogger.info("syncCompilationUnit File " + f);
		replaceCompilationUnit(f);
		WLogger.info("replaced file " + f);
		WurstGui gui = new WurstGuiLogger();
		doTypeCheckPartial(gui, true, ImmutableList.of(getProjectRelativePath(f)));
	}



	private CompilationUnit replaceCompilationUnit(String filename, String contents, boolean reportErrors) {
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
		CompilationUnit cu = c.parse(filename, new StringReader(contents));
		cu.setFile(filename);
		updateModel(cu, gui);
		fileHashcodes.put(filename, contents.hashCode());
		if (reportErrors) {
			System.out.println("found " + gui.getErrorCount() + " errors in file " + filename);
			reportErrors("sync cu " + filename, filename, gui.getErrorsAndWarnings());
		}
		return cu;
	}

	@Override
	public CompilationUnit getCompilationUnit(String filename) {
		filename = normalizeFilename(filename);
		List<CompilationUnit> matches = getCompilationUnits(Collections.singletonList(filename));
		if (matches.isEmpty()) {
			WLogger.info("compilation unit not found: " + filename);
			WLogger.info("available: " + model.stream().map(cu -> cu.getFile()).collect(Collectors.joining(", ")));
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
		// TODO add type errors as well
		return parseErrors.values().stream().anyMatch(l -> !l.isEmpty());
	}


	@Override
	public void updateCompilationUnit(String filename, String contents, boolean reportErrors) {
		replaceCompilationUnit(filename, contents, reportErrors);
	}

	@Override
	public void onCompilationResult(Consumer<CompilationResult> f) {
		onCompilationResultListeners.add(f);
	}

	public static void main(String[] args) throws IOException {
		// WurstGui gui = new WurstGuiCliImpl();
		// WurstCompilerJassImpl c = new WurstCompilerJassImpl(gui, null, new
		// RunArgs(Arrays.asList()));
		// CompilationUnit cu = c.parse("RegionData.wurst", new
		// FileReader("/home/peter/work/EBR/wurst/region/RegionData.wurst"));
		//
		// for (CompileError err : gui.getErrorsAndWarnings()) {
		// System.out.println(err);
		// System.out.println(ExternCompileError.convert(err));
		// }

		ModelManagerImpl m = new ModelManagerImpl("/home/peter/work/EBR");
		m.onCompilationResult(cr -> {
			for (ExternCompileError err : cr.getErrors()) {
				System.err.println(cr.getFilename());
				System.err.println("	" + err);
			}
		});
		m.buildProject();

	}

	private void doTypeCheckPartial(WurstGui gui, boolean addErrorMarkers, List<String> toCheckFilenames) {
		WLogger.info("do typecheck partial of " + toCheckFilenames);
		WurstCompilerJassImpl comp = getCompiler(gui);
		List<CompilationUnit> toCheck = getCompilationUnits(toCheckFilenames);
		WurstModel model2 = model;
		if (model2 == null) {
			return;
		}

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
		if (addErrorMarkers) {
			List<String> fileNames = getfileNames(clearedCUs);
			reportErrorsForFiles("partial ", fileNames, gui);
		}
	}


	@Override
	public synchronized Set<File> getDependencyWurstFiles() {
		Set<File> result = Sets.newHashSet();
		for (String dep : dependencies) {
			addDependencyWurstFiles(result, new File(dep));
		}
		return result;
	}

	private void addDependencyWurstFiles(Set<File> result, File file) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				addDependencyWurstFiles(result, child);
			}
		} else if (Utils.isWurstFile(file)) {
			result.add(file);
		}
	}

}
