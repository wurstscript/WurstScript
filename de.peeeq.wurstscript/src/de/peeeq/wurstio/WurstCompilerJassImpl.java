package de.peeeq.wurstio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstio.utils.FileReading;
import de.peeeq.wurstscript.ErrorReporting;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstChecker;
import de.peeeq.wurstscript.WurstCompiler;
import de.peeeq.wurstscript.WurstParser;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.translation.imoptimizer.ImOptimizer;
import de.peeeq.wurstscript.translation.imtojass.ImToJassTranslator;
import de.peeeq.wurstscript.translation.imtranslation.AssertProperty;
import de.peeeq.wurstscript.translation.imtranslation.CyclicFunctionRemover;
import de.peeeq.wurstscript.translation.imtranslation.DebugMessageRemover;
import de.peeeq.wurstscript.translation.imtranslation.EliminateClasses;
import de.peeeq.wurstscript.translation.imtranslation.FuncRefRemover;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.translation.imtranslation.StackTraceInjector;
import de.peeeq.wurstscript.utils.LineOffsets;
import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.utils.Utils;

public class WurstCompilerJassImpl implements WurstCompiler {

	private List<File> files = Lists.newArrayList();
	private Map<String, Reader> otherInputs = Maps.newLinkedHashMap();
	private JassProg prog;
	private WurstGui gui;
	private boolean hasCommonJ;
	private RunArgs runArgs;
	private File mapFile;
	private ErrorHandler errorHandler;
	private Map<String, File> libCache = null;
	private ImProg imProg;
	private List<File> parsedFiles = Lists.newArrayList();
	private final WurstParser parser;
	private final WurstChecker checker;
	private ImTranslator imTranslator;
	private List<File> dependencies = Lists.newArrayList();

	
	public WurstCompilerJassImpl(WurstGui gui, RunArgs runArgs) {
		this.gui = gui;
		this.runArgs = runArgs;
		this.errorHandler = new ErrorHandler(gui);
		this.parser = new WurstParser(errorHandler, gui);
		this.checker = new WurstChecker(gui, errorHandler);
	}

	@Override
	public void loadFiles(String ... filenames) {
		gui.sendProgress("Loading Files", 0.01);
		for (String filename : filenames) {
			File file = new File(filename);
			if (!file.exists()) {
				throw new Error("File " + filename + " does not exist.");
			}
			files.add(file);
		}
	}
	
	@Override
	public void loadFiles(File ... files) {
		gui.sendProgress("Loading Files", 0.01);
		for (File file : files) {
			loadFile(file);
		}
	}

	private void loadFile(File file) throws Error {
		if (file == null) {
			throw new Error("File must not be null");
		}
		if (!file.exists()) {
			throw new Error("File " + file + " does not exist.");
		}
		this.files.add(file);
	}
	
	public void loadWurstFilesInDir(File dir) {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				loadWurstFilesInDir(f);
			} else if (f.getName().endsWith(".wurst") || f.getName().endsWith(".jurst")) {
				loadFile(f);
			} else if (f.getName().equals("wurst.dependencies")) {
				addDependencyFile(f);
			}
		}
	}
	
	private void addDependencyFile(File f) {
		try (FileReader fr = new FileReader(f);
			BufferedReader reader = new BufferedReader(fr)) {
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				addDependencyFolder(f, line);						
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error(e);
		}
	}


	private void addDependencyFolder(File f, String folderName) {
		File folder = new File(folderName);
		if (!folder.exists()) {
			gui.sendError(new CompileError(new WPos(f.getAbsolutePath(), new LineOffsets(), 0, 1), "Folder " + folderName + " not found."));
		} else if (!folder.isDirectory()) {
			gui.sendError(new CompileError(new WPos(f.getAbsolutePath(), new LineOffsets(), 0, 1), "" + folderName + " is not a folder."));
		} else {
			dependencies.add(folder);
		}
	}

	@Override public WurstModel parseFiles() {

		// search mapFile
		for (File file : files) {
			if (file.getName().endsWith(".w3x") || file.getName().endsWith(".w3m")) {
				mapFile = file;
			}
		}
		
		// import wurst folder if it exists
		if (mapFile != null) {
			File relativeWurstDir = new File(mapFile.getParentFile(), "wurst");
			if (relativeWurstDir.exists()) {
				WLogger.info("Importing wurst files from " + relativeWurstDir);
				loadWurstFilesInDir(relativeWurstDir);
			} else {
				WLogger.info("No wurst folder found in " + relativeWurstDir);
			}
			File dependencyFile = new File(mapFile.getParentFile(), "wurst.dependencies");
			if (dependencyFile.exists()) {
				addDependencyFile(dependencyFile);
			}
			
		}
		
		// add directories:
		List<File> dirs = Lists.newArrayList();
		for (File file : files) {
			if (file.isDirectory()) {
				dirs.add(file);
			}
		}
		for (File dir : dirs) {
			loadWurstFilesInDir(dir);
		}
		
		
		gui.sendProgress("Parsing Files", 0.02);
		// parse all the files:
		List<CompilationUnit> compilationUnits = new NotNullList<CompilationUnit>();
		
		for (File file : files) {
			if (file.isDirectory()) {
				// ignore dirs
			} else if (file.getName().endsWith(".w3x") || file.getName().endsWith(".w3m")) {
				CompilationUnit r = processMap(file);
				compilationUnits.add(r );				
			} else {
				if (file.getName().endsWith("common.j")) {
					hasCommonJ = true;
				}
				compilationUnits.add(parseFile(file));
			}
		}
		for (Entry<String, Reader> in : otherInputs.entrySet()) {
			compilationUnits.add(parse(in.getKey(), in.getValue()));
		}
		
		try {
			if (hasCommonJ) {
				loadLibPackage(compilationUnits, "Wurst");
			}
			addImportedLibs(compilationUnits);
		} catch (CompileError e) {
			gui.sendError(e);
			return null;
		}
		
		if (errorHandler.getErrorCount() > 0) return null;
		
		// merge the compilationUnits:
		WurstModel merged = mergeCompilationUnits(compilationUnits);
		StringBuilder sb = new StringBuilder();
		for (CompilationUnit cu:merged) {
			sb.append(cu.getFile() + ", ");
		}
		WLogger.info("Compiling compilation units: " + sb);
		
		return merged;
	}
	

	

	/**
	 * this method scans for unsatisfied imports and tries to find them in the lib-path 
	 */
	public void addImportedLibs(List<CompilationUnit> compilationUnits) {
		Set<String> packages = Sets.newLinkedHashSet();
		Map<String, WImport> imports = Maps.newLinkedHashMap();
		for (CompilationUnit c : compilationUnits) {
			c.setCuErrorHandler(errorHandler);
			for (WPackage p : c.getPackages()) {
				packages.add(p.getName());
				for (WImport i : p.getImports()) {
					imports.put(i.getPackagename(), i);
				}
			}
		}	
		
		for (WImport imp : imports.values()) {
			resolveImport(compilationUnits, packages, imports, imp);
		}
		
		
	}

	private void resolveImport(List<CompilationUnit> compilationUnits, Set<String> packages, Map<String, WImport> imports, WImport imp)
			throws CompileError {
//		WLogger.info("resolving import: " + imp.getPackagename());
		if (!packages.contains(imp.getPackagename())) {
			if (getLibs().containsKey(imp.getPackagename())) {
				CompilationUnit lib = loadLibPackage(compilationUnits, imp.getPackagename());
				boolean foundPackage = false;
				for (WPackage p : lib.getPackages()) {
					packages.add(p.getName());
					if (p.getName().equals(imp.getPackagename())) {
						foundPackage = true;
					}
					for (WImport i : p.getImports()) {
						resolveImport(compilationUnits, packages, imports, i);
					}
				}
				if (!foundPackage) {
					throw new CompileError(imp.getSource(), "The import " + imp.getPackagename() + " could not be found in file " + lib.getFile());
				}
			} else {
				if (imp.getPackagename().equals("Wurst")) {
					throw new CompileError(imp.getSource(), "The standard library could not be imported.");
				}
				if (imp.getPackagename().equals("NoWurst")) {
					// ignore this package
				} else {
					throw new CompileError(imp.getSource(), "The import '" + imp.getPackagename() + "' could not be resolved.\n" + 
						"Available packages: " + Utils.join(getLibs().keySet(), ", "));
				}
			}
		} else {
//			WLogger.info("already imported: " + imp.getPackagename());
		}
	}

	private CompilationUnit loadLibPackage(List<CompilationUnit> compilationUnits, String imp) {
		File file = getLibs().get(imp);
		if (file == null) {
			gui.sendError(new CompileError(new WPos("", null, 0, 0), "Could not find lib-package " + imp));
			return Ast.CompilationUnit("", errorHandler, Ast.JassToplevelDeclarations(), Ast.WPackages());
		} else {
			CompilationUnit lib = parseFile(file);
			lib.setFile(file.getAbsolutePath());
			compilationUnits.add(lib);
			return lib;
		}
	}

	
	
	public Map<String, File> getLibs() {
		if (libCache == null) {
			libCache = Maps.newLinkedHashMap();
			for (File libDir: runArgs.getAdditionalLibDirs()) {
				addLibDir(libDir);
			}
			for (File libDir: dependencies) {
				addLibDir(libDir);
			}
		}
		return libCache;
	}

	private void addLibDir(File libDir) throws Error {
		if (!libDir.exists() || !libDir.isDirectory()) {
			throw new Error("Library folder " + libDir + " does not exist.");
		}
		for (File f : libDir.listFiles()) {
			if (f.isDirectory()) {
				// recursively scan directory
				addLibDir(f);
			}
			if (f.getName().endsWith(".wurst") || f.getName().endsWith(".jurst")) {
				String libName = f.getName().replaceAll("\\.[jw]urst$", "");
				libCache.put(libName, f);
			}
		}
	}

	public void checkAndTranslate(WurstModel root) {
		WLogger.info("begin checkAndTranslate");
		checkProg(root);
		
		
		if (errorHandler.getErrorCount() > 0) {
			return;
		}
		
		try {
			prog = translateProg(root);
		} catch (CompileError e) {
			WLogger.severe(e);
			errorHandler.sendError(e);
		}
	}

	public void checkProg(WurstModel root) {
		checker.checkProg(root);
	}

	

	public JassProg translateProg(WurstModel root) {
		translateProgToIm(root);
		return transformProgToJass();
	}

	public JassProg transformProgToJass() {
		int stage = 2;
		// eliminate classes
		beginPhase(2, "translate classes");
		new EliminateClasses(imTranslator, imProg).eliminateClasses();

		printDebugImProg("./test-output/im " + stage++ + "_classesEliminated.im");
		
		
		if (runArgs.isNoDebugMessages()) {
			beginPhase(3, "remove debug messages");
			DebugMessageRemover.removeDebugMessages(imProg);
		} else {
			// debug: add stacktraces
			if (runArgs.isIncludeStacktraces()) {
				beginPhase(4, "add stack traces");
				new StackTraceInjector(imProg).transform();
			}
		}
		
		
		ImOptimizer optimizer = new ImOptimizer(imTranslator);
		
		
		
		// inliner
		if (runArgs.isInline()) {
			beginPhase(5, "inlining");
			optimizer.doInlining();
			
			printDebugImProg("./test-output/im " + stage++ + "_afterinline.im");
		}
		
		
		printDebugImProg("./test-output/test_opt.im");
	
		
		// eliminate tuples
		beginPhase(6, "eliminate tuples");
		imProg.eliminateTuples(imTranslator);
		imTranslator.assertProperties(AssertProperty.NOTUPLES);
		
		printDebugImProg("./test-output/im " + stage++ + "_withouttuples.im");

		
		beginPhase(7, "remove func refs");
		new FuncRefRemover(imProg, imTranslator).run();
		
		// remove cycles:
		beginPhase(8, "remove cyclic functions");
		new CyclicFunctionRemover(imTranslator, imProg).work();;
		
		printDebugImProg("./test-output/im " + stage++ + "_nocyc.im");
		
		
		// flatten
		beginPhase(9, "flatten");
		imProg.flatten(imTranslator);
		imTranslator.assertProperties(AssertProperty.NOTUPLES, AssertProperty.FLAT);
		
		printDebugImProg("./test-output/im " + stage++ + "_flat.im");
		
		
		if (runArgs.isLocalOptimizations()) {
			beginPhase(10, "local optimizations");
			optimizer.localOptimizations();
		}
		
		
		if (runArgs.isNullsetting()) {
			beginPhase(11, "null setting");
			optimizer.doNullsetting();
			printDebugImProg("./test-output/im " + stage++ + "_afternullsetting.im");
		}
		
		
		optimizer.removeGarbage();
		
		if (runArgs.isOptimize()) {
			beginPhase(12, "froptimize");
			optimizer.optimize();
			
			printDebugImProg("./test-output/im " + stage++ + "_afteroptimize.im");
		}
		
		
		
		// translate flattened intermediate lang to jass:
		
		beginPhase(13, "translate to jass");
		imTranslator.calculateCallRelationsAndUsedVariables();
		ImToJassTranslator translator = new ImToJassTranslator(imProg, imTranslator.getCalledFunctions()
				, imTranslator.getMainFunc(), imTranslator.getConfFunc());
		prog = translator.translate();
		if (errorHandler.getErrorCount() > 0) {
			prog = null;
		}
		return prog;
	}

	public ImProg translateProgToIm(WurstModel root) {
		beginPhase(1, "to intermediate lang");
		
		// translate wurst to intermediate lang:
		imTranslator = new ImTranslator(root, errorHandler.isUnitTestMode());
		imProg = imTranslator.translateProg();
		int stage = 1;
		
		printDebugImProg("./test-output/im " + stage++ + ".im");
		return imProg;
	}

	private void beginPhase(int phase, String description) {
		errorHandler.setProgress("Translating wurst. Phase " + phase +  ": " + description, 0.6 + 0.01*phase);
	}

	private void printDebugImProg(String debugFile) {
		if (!errorHandler.isUnitTestMode()) {
			// output only in unit test mode
			return;
		}
		try {
			// TODO remove test output
			StringBuilder sb = new StringBuilder();
			imProg.print(sb, 0);
			File file = new File(debugFile);
			file.getParentFile().mkdirs();
			Files.write(sb.toString(), file, Charsets.UTF_8);
		} catch (IOException e) {
			ErrorReporting.instance.handleSevere(e, getCompleteSourcecode());
		}
	}

	

	

	

	

	

	
//	private List<ModuleDef> getAllModules(CompilationUnit root) {
//		List<ModuleDef> result = Lists.newArrayList();
//		for (TopLevelDeclaration t : root) {
//			if (t instanceof WPackage) {
//				WPackage p = (WPackage) t;
//				for (WEntity e : p.getElements()) {
//					if (e instanceof ModuleDef) {
//						result.add((ModuleDef) e);
//					}
//				}
//			}
//		}
//		return result;
//	}
	
	private WurstModel mergeCompilationUnits(List<CompilationUnit> compilationUnits) {
		gui.sendProgress("Merging Files", 0.19);
		WurstModel result = Ast.WurstModel();
		for (CompilationUnit compilationUnit : compilationUnits) {
			// remove from old parent
			compilationUnit.setParent(null);
			result.add(compilationUnit);
		}
		return result;
	}

	private CompilationUnit processMap(File file) {
		gui.sendProgress("Processing Map " + file.getName(), 0.05);		

		// extract mapscript:
		if ( MpqEditorFactory.getFilepath().equals("")) {
			MpqEditorFactory.setFilepath("./mpqedit/mpqeditor.exe");
		}
		MpqEditor mpqEditor = null;
		try {
			mpqEditor = MpqEditorFactory.getEditor();
			File tempFile = mpqEditor.extractFile(file, "war3map.j");
			
			if (isWurstGenerated(tempFile)) {
				// the war3map.j file was generated by wurst
				// this should not be the case, as we will get duplicate function errors in this case
				throw new AbortCompilationException("Map was not saved correctly. Please try saving the map again.\n\n" +
						"This usually happens if you change the name of the map or \n" +
						"if you have used the test-map-button without saving the map first.");
			}
			
			// move file to wurst directory
			File wurstFolder = new File(file.getParentFile(), "wurst");
			wurstFolder.mkdirs();
			if (!wurstFolder.isDirectory()) {
				throw new AbortCompilationException("Could not create Wurst folder at " + wurstFolder + ".");
			}
			File wurstwar3map = new File(wurstFolder, "war3map.j");
			wurstwar3map.delete();
			if (tempFile.renameTo(wurstwar3map)) {
				return parseFile(wurstwar3map);
			} else {
				throw new Error("Could not move war3map.j from " + tempFile + " to " + wurstwar3map);
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new Error(e);
		}

	}

	private boolean isWurstGenerated(File tempFile) {
		try (FileReader fr = new FileReader(tempFile);
				BufferedReader in = new BufferedReader(fr)) {
			String firstLine = in.readLine();
			WLogger.info("firstLine = '"+ firstLine + "'");
			return firstLine.equals(JassPrinter.WURST_COMMENT);
		} catch (IOException e) {
			WLogger.severe(e);
		}
		return false;
	}

	private CompilationUnit parseFile(File file) {
		if (file.isDirectory()) {
			throw new Error("Is a directory: " + file);
		}
		parsedFiles .add(file);
		
		gui.sendProgress("Parsing File " + file.getName(), 0.05);
		String source = file.getAbsolutePath();
		Reader reader = null;
		try {
			reader = FileReading.getFileReader(file);
			
			// scanning
			return parse(source, reader);
			
		} catch (CompileError e) {
			gui.sendError(e);
			return emptyCompilationUnit();
		} catch (FileNotFoundException e) {
			gui.sendError(new CompileError(new WPos(source, LineOffsets.dummy, 0, 0), "File not found."));
			return emptyCompilationUnit();
		} catch (IOException e) {
			gui.sendError(new CompileError(new WPos(source, LineOffsets.dummy, 0, 0), "Could not read file."));
			return emptyCompilationUnit();
		} finally {
			try {
				if (reader != null) reader.close();
			} catch (IOException e) {
			}
		}
	}

	public CompilationUnit parse(String source, Reader reader) {
		if (source.endsWith(".jurst")) {
			return parser.parseJurst(reader, source, hasCommonJ);
		}
		return parser.parse(reader, source, hasCommonJ);
	}

	private CompilationUnit emptyCompilationUnit() {
		return parser.emptyCompilationUnit();
	}

	public JassProg getProg() {
		return prog;
	}

	public void loadReader(String name, Reader input) {
		otherInputs.put(name, input);
	}

	public void setHasCommonJ(boolean hasCommonJ) {
		this.hasCommonJ = hasCommonJ;
	}

	public ImProg getImProg() {
		return imProg;
	}

	public File getMapFile() {
		return mapFile;
	}

	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public String getCompleteSourcecode() {
		
		StringBuilder sb = new StringBuilder();
		try {
			for (File f: parsedFiles) {
				sb.append(" //######################################################\n");
				sb.append(" // File " + f.getAbsolutePath() + "\n");
				sb.append(" //######################################################\n");
				sb.append(Files.toString(f, Charsets.UTF_8));
			}
			
			for (Entry<String, Reader> entry : otherInputs.entrySet()) {
				sb.append(" //######################################################\n");
				sb.append(" // Input " + entry.getKey() + "\n");
				sb.append(" //######################################################\n");
				Reader reader = entry.getValue();
				char[] buffer = new char[1024];
				while (true) {
					int len = reader.read(buffer);
					if (len < 0) {
						break;
					}
					sb.append(buffer, 0, len);
				}
			}
		} catch (Throwable t) {
			sb.append(t.getMessage());
			sb.append(Utils.printStackTrace(t.getStackTrace()));
			WLogger.severe(t);
		}
		return sb.toString();
	}

	public void setRunArgs(RunArgs runArgs) {
		this.runArgs = runArgs;
	}

	public void setMapFile(File mapFile) {
		this.mapFile = mapFile;
	}


	
}
