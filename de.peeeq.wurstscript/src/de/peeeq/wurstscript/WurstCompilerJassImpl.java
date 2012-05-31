package de.peeeq.wurstscript;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import java_cup.runtime.Symbol;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.attr;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.mpq.LadikMpq;
import de.peeeq.wurstscript.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.parser.ExtendedParser;
import de.peeeq.wurstscript.parser.WurstScriptScanner;
import de.peeeq.wurstscript.translation.imoptimizer.ImInliner;
import de.peeeq.wurstscript.translation.imtojass.ImToJassTranslator;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.LineOffsets;
import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.validation.WurstValidator;

public class WurstCompilerJassImpl implements WurstCompiler {

	private List<File> files = Lists.newArrayList();
	private Map<String, Reader> otherInputs = Maps.newHashMap();
	private int parseErrors;
	private JassProg prog;
	private WurstGui gui;
	private boolean hasCommonJ;
	private RunArgs runArgs;

	
	public WurstCompilerJassImpl(WurstGui gui, RunArgs runArgs) {
		this.gui = gui;
		this.runArgs = runArgs;
		attr.init(gui);
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
			if (file == null) {
				throw new Error("File must not be null");
			}
			if (!file.exists()) {
				throw new Error("File " + file + " does not exist.");
			}
			this.files.add(file);
		}
	}
	
	@Override public void parseFiles() {
		gui.sendProgress("Parsing Files", 0.02);
		// parse all the files:
		List<CompilationUnit> compilationUnits = new NotNullList<CompilationUnit>();
		
		for (File file : files) {
			if (file.getName().endsWith(".w3x") || file.getName().endsWith(".w3m")) {
				CompilationUnit r = processMap(file);
				if (r == null) {
					return;
				}
				compilationUnits.add(r );				
			} else {
				if (file.getName().endsWith("common.j")) {
					hasCommonJ = true;
				}
				compilationUnits.add(parseFile(file));
			}
		}
		for (Entry<String, Reader> in : otherInputs.entrySet()) {
			compilationUnits.add(parse(in.getValue(), in.getKey()));
			
		}
		
		try {
			if (hasCommonJ) {
				loadLibPackage(compilationUnits, "Wurst");
			}
			addImportedLibs(compilationUnits);
		} catch (CompileError e) {
			gui.sendError(e);
			return;
		}
		
		if (attr.getErrorCount() > 0) return;
		
		// merge the compilationUnits:
		WurstModel merged = mergeCompilationUnits(compilationUnits);
		StringBuilder sb = new StringBuilder();
		for (CompilationUnit cu:merged) {
			sb.append(cu.getFile() + ", ");
		}
		WLogger.info("Compiling compilation units: " + sb);
		
		checkAndTranslate(merged);
		gui.sendProgress("finished parsing", .9);
	}
	

	/**
	 * this method scans for unsatisfied imports and tries to find them in the lib-path 
	 */
	private void addImportedLibs(List<CompilationUnit> compilationUnits) {
		Set<String> packages = Sets.newHashSet();
		Map<String, WImport> imports = Maps.newHashMap();
		for (CompilationUnit c : compilationUnits) {
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
		WLogger.info("resolving import: " + imp.getPackagename());
		if (!packages.contains(imp.getPackagename())) {
			if (getLibs().containsKey(imp.getPackagename())) {
				CompilationUnit lib = loadLibPackage(compilationUnits, imp.getPackagename());
				for (WPackage p : lib.getPackages()) {
					packages.add(p.getName());
					for (WImport i : p.getImports()) {
						resolveImport(compilationUnits, packages, imports, i);
					}
				}
			} else {
				throw new CompileError(imp.getSource(), "The import " + imp.getPackagename() + " could not be resolved.\n" + 
						"Available packages: " + Utils.join(getLibs().keySet(), ", "));
			}
		} else {
			WLogger.info("already imported: " + imp.getPackagename());
		}
	}

	private CompilationUnit loadLibPackage(List<CompilationUnit> compilationUnits, String imp) {
		File file = getLibs().get(imp);
		if (file == null) {
			gui.sendError(new CompileError(Ast.WPos("", 0, 0, 0), "Could not find lib-package " + imp));
			return Ast.CompilationUnit("", Ast.JassToplevelDeclarations(), Ast.WPackages());
		} else {
			CompilationUnit lib = parseFile(file);
			compilationUnits.add(lib);
			return lib;
		}
	}

	private Map<String, File> libCache = null;
	
	public Map<String, File> getLibs() {
		if (libCache == null) {
			libCache = Maps.newHashMap();
			String[] libFolders = WurstConfig.get().getSetting("lib").split(";");
			for (String libDirName : libFolders) {
				File libDir = new File(libDirName);
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
			if (f.getName().endsWith(".wurst")) {
				String libName = f.getName().replaceAll("\\.wurst$", "");
				libCache.put(libName, f);
			}
		}
	}

	public void checkAndTranslate(WurstModel root) {
		checkProg(root);
		
		
		if (attr.getErrorCount() > 0) {
			return;
		}
		
		try {
			prog = translateProg(root);
		} catch (CompileError e) {
			WLogger.severe(e);
			attr.addError(e.getSource(), e.getMessage());
		}
	}

	public void checkProg(WurstModel root) {
		gui.sendProgress("Checking Files", 0.2);
		
		if (attr.getErrorCount() > 0) return;
		
		expandModules(root);
		
		if (attr.getErrorCount() > 0) return;
		
		// validate the resource:
		WurstValidator validator = new WurstValidator(root);
		validator.validate();
	}

	private void expandModules(WurstModel root) {
		for (CompilationUnit cu : root) {
			new ModuleExpander().expandModules(cu);
		}
	}

	public JassProg translateProg(WurstModel root) {
		// translate wurst to intermediate lang:
		ImTranslator imTranslator = new ImTranslator(root);
		ImProg imProg = imTranslator.translateProg();
		
		try {
			// TODO remove test output
			StringBuilder sb = new StringBuilder();
			imProg.print(sb, 0);
			Files.write(sb.toString(), new File("./test-output/test.im"), Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (runArgs.isInline()) {
			ImInliner inliner = new ImInliner(imTranslator);
			inliner.doInlining();
		}
		
		try {
			// TODO remove test output
			StringBuilder sb = new StringBuilder();
			imProg.print(sb, 0);
			Files.write(sb.toString(), new File("./test-output/test_opt.im"), Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// flatten
		imProg.flatten(imTranslator);
		
		
		// translate flattened intermediate lang to jass:
		
		ImToJassTranslator translator = new ImToJassTranslator(imProg, imTranslator.getCalledFunctions()
				, imTranslator.getMainFunc(), imTranslator.getConfFunc(), imTranslator.getTrace());
		JassProg p = translator.translate();
		if (attr.getErrorCount() > 0) {
			return null;
		}
		return p;
	}

	

	private void removeSyntacticSugar(CompilationUnit root) {
		new SyntacticSugar().removeSyntacticSugar(root, hasCommonJ);
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
		gui.sendProgress("Merging Files", 0.22);
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
        try {
        	
        	
        	// extract mapscript:
        	if ( MpqEditorFactory.getFilepath().equals("")) {
        		MpqEditorFactory.setFilepath("./mpqedit/mpqeditor.exe");
        	}
        	LadikMpq mpqEditor = null;
			try {
				mpqEditor = MpqEditorFactory.getEditor();
			} catch (Exception e) {
				e.printStackTrace();
			}
			File tempFile = mpqEditor.extractFile(file, "war3map.j");
//        	Runtime rt = Runtime.getRuntime();
//			String[] commands = {"MpqCL.exe", "extract", file.getAbsolutePath(), "war3map.j", tempFile.getAbsolutePath()};
//			Process proc = rt.exec(commands);
//			InputStream procOut = proc.getInputStream();
//			BufferedReader procOutReader = new BufferedReader(new InputStreamReader(procOut));
//			proc.waitFor();
//			String line;
//			while ((line = procOutReader.readLine()) != null) {
//				WLogger.info(line);
//			}
//			
			return parseFile(tempFile);
		} catch (IOException e) {
			throw new Error(e);
		} catch (InterruptedException e) {
			throw new Error(e);
		}
	}

	private CompilationUnit parseFile(File file) {
		gui.sendProgress("Parsing File " + file.getName(), 0.05);
		String source = file.getAbsolutePath();
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			
			// scanning
			return parse(reader, source);
			
		} catch (CompileError e) {
			gui.sendError(e);
			return emptyCompilationUnit();
		} catch (FileNotFoundException e) {
			gui.sendError(new CompileError(Ast.WPos(source, LineOffsets.dummy, 0, 0), "File not found."));
			return emptyCompilationUnit();
		} finally {
			try {
				if (reader != null) reader.close();
			} catch (IOException e) {
			}
		}
	}

	private CompilationUnit emptyCompilationUnit() {
		return Ast.CompilationUnit("<empty compilation unit>", Ast.JassToplevelDeclarations(), Ast.WPackages());
	}

	public CompilationUnit parse(Reader reader, String source) {
		try {
			WurstScriptScanner scanner = new WurstScriptScanner(reader);
			ExtendedParser parser = new ExtendedParser(scanner, gui );
			parser.setFilename(source);
			Symbol sym = parser.parse();
			parseErrors = parser.getErrorCount();
			if (sym.value instanceof CompilationUnit) {
				CompilationUnit root = (CompilationUnit) sym.value;
				removeSyntacticSugar(root);
				return root;
			}
			return emptyCompilationUnit();
		} catch (CompileError e) {
			gui.sendError(e);
			return emptyCompilationUnit();
		} catch (Exception e) {
			gui.sendError(new CompileError(Ast.WPos(source, LineOffsets.dummy, 0, 0), "This is a bug and should not have happened.\n" + e.getMessage()));
			WLogger.severe(e);
			throw new Error(e);
		}
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

	
	
}
