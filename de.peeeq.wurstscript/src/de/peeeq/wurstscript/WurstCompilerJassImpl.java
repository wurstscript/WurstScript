package de.peeeq.wurstscript;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ListIterator;

import org.testng.collections.Lists;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java_cup.runtime.Symbol;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.TopLevelDeclaration;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.attr;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jasstranslation.JassTranslator;
import de.peeeq.wurstscript.parser.ExtendedParser;
import de.peeeq.wurstscript.parser.WurstScriptScanner;
import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.utils.TopsortCycleException;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.validation.WurstValidator;

public class WurstCompilerJassImpl implements WurstCompiler {

	private File[] files;
	private int parseErrors;
	private JassProg prog;
	private File outputMapFile;
	private WurstGui gui;

	
	public WurstCompilerJassImpl(WurstGui gui) {
		this.gui = gui;
	}

	@Override
	public void loadFiles(String ... filenames) {
		gui.sendProgress("Loading Files", 0.01);
		files = new File[filenames.length];
		int i = 0;
		for (String filename : filenames) {
			files[i] = new File(filename);
			if (!files[i].exists()) {
				throw new Error("File " + filename + " does not exist.");
			}
			i++;
		}
	}
	
	@Override
	public void loadFiles(File ... files) {
		gui.sendProgress("Loading Files", 0.01);
		this.files = new File[files.length];
		int i = 0;
		for (File file : files) {
			if (file == null) {
				throw new Error("File must not be null");
			}
			this.files[i] = file;
			if (!this.files[i].exists()) {
				throw new Error("File " + file + " does not exist.");
			}
			i++;
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
				outputMapFile = file;
			} else {
				compilationUnits.add(parseFile(file));
			}
		}
		
		// merge the compilationUnits:
		CompilationUnit merged = mergeCompilationUnits(compilationUnits);
		
		checkAndTranslate(merged);
	}
	

	private void checkAndTranslate(CompilationUnit root) {
		gui.sendProgress("Checking Files", 0.2);
		
		// create new attributes instance:
		attr.init(gui);
		
		// handle syntactic sugar
		removeSyntacticSugar(root);
		
//		expandModules(root);
		
		// validate the resource:
		WurstValidator validator = new WurstValidator(root);
		validator.validate();
		
		if (attr.getErrorCount() > 0) {
			return;
		}
		
		
		
		
		
		if (attr.getErrorCount() > 0) {
			return;
		}
		
		
		// translate to intermediate lang:
		JassTranslator translator = new JassTranslator(root);
		prog = translator.translate();
		
		if (attr.getErrorCount() > 0) {
			prog = null;
			return;
		}
	}

	private void removeSyntacticSugar(CompilationUnit root) {
		addDefaultConstructors(root);
	}

	/**
	 * add a empty default constructor to every class without any constructor 
	 */
	private void addDefaultConstructors(CompilationUnit root) {
		outerLoop: for (ClassDef c : getAllClasses(root)) {
			for (ClassSlot s : c.getSlots()) {
				if (s instanceof ConstructorDef) {
					continue outerLoop;
				}
			}
			c.getSlots().add(Ast.ConstructorDef(
					c.getSource().copy(), 
					Ast.Modifiers(Ast.VisibilityPublic(c.getSource().copy())), 
					Ast.WParameters(), 
					Ast.WStatements()));
		}
	}

	/**
	 * return all classes occuring in a compilation unit 
	 */
	private List<ClassDef> getAllClasses(CompilationUnit root) {
		List<ClassDef> result = Lists.newArrayList();
		for (TopLevelDeclaration t : root) {
			if (t instanceof WPackage) {
				WPackage p = (WPackage) t;
				for (WEntity e : p.getElements()) {
					if (e instanceof ClassDef) {
						result.add((ClassDef) e);
					}
				}
			}
		}
		return result;
	}

	
	private List<ModuleDef> getAllModules(CompilationUnit root) {
		List<ModuleDef> result = Lists.newArrayList();
		for (TopLevelDeclaration t : root) {
			if (t instanceof WPackage) {
				WPackage p = (WPackage) t;
				for (WEntity e : p.getElements()) {
					if (e instanceof ModuleDef) {
						result.add((ModuleDef) e);
					}
				}
			}
		}
		return result;
	}
	
//	private void expandModules(CompilationUnit root) {
//		List<ModuleDef> allModules = getAllModules(root);
//		
//		// collect usages
//		Multimap<ModuleDef, ModuleDef> usedModules = HashMultimap.create();
//		for (ModuleDef moduleDef : allModules) {
//			for (ClassSlot e : moduleDef.getSlots()) {
//				if (e instanceof ModuleUse) {
//					ModuleUse moduleUse = (ModuleUse) e;
//					usedModules.put(moduleDef, moduleUse.attrModuleDef());
//				}
//			}
//		}
//		
//		// sort the modules:
//		try {
//			allModules = Utils.topSort(allModules, usedModules);
//		} catch (TopsortCycleException e) {
//			@SuppressWarnings("unchecked")
//			List<ModuleDef> conflicts = (List<ModuleDef>) e.activeItems;
//			attr.addError(conflicts.get(0).getSource(), "Cyclic use between modules: " + Utils.join(conflicts, ", "));
//		}
//		
//		// inline modules into modules:
//		for (ModuleDef moduleDef : allModules) {
//			ListIterator<ClassSlot> it = moduleDef.getSlots().listIterator();
//			while (it.hasNext()) {
//				ClassSlot slot = it.next();
//				if (slot instanceof ModuleUse) {
//					ModuleUse moduleUse = (ModuleUse) slot;
//					ModuleDef usedModule = moduleUse.attrModuleDef();
//					
//				}
//			}
//			// TODO clear attributes
//		}
//		
//		
//	}

	private CompilationUnit mergeCompilationUnits(List<CompilationUnit> compilationUnits) {
		gui.sendProgress("Merging Files", 0.2);
		CompilationUnit result = Ast.CompilationUnit();
		for (CompilationUnit compilationUnit : compilationUnits) {
			while (!compilationUnit.isEmpty()) {
				TopLevelDeclaration x = compilationUnit.remove(0);
				result.add(x);
			}
		}
		return result;
	}

	private CompilationUnit processMap(File file) {
		gui.sendProgress("Processing Map " + file.getName(), 0.05);		
        try {
        	File tempFile = new File("temp_war3map.j");
        	
        	// extract mapscript:
        	Runtime rt = Runtime.getRuntime();
			String[] commands = {"MpqCL.exe", "extract", file.getAbsolutePath(), "war3map.j", tempFile.getAbsolutePath()};
			Process proc = rt.exec(commands);
			InputStream procOut = proc.getInputStream();
			BufferedReader procOutReader = new BufferedReader(new InputStreamReader(procOut));
			proc.waitFor();
			String line;
			while ((line = procOutReader.readLine()) != null) {
				WLogger.info(line);
			}
			
			return parseFile(tempFile);
		} catch (IOException e) {
			throw new Error(e);
		} catch (InterruptedException e) {
			throw new Error(e);
		}
	}

	private CompilationUnit parseFile(File file) {
		gui.sendProgress("Parsing File " + file.getName(), 0.05);	
		try {
			FileReader reader = new FileReader(file);
			// scanning
			WurstScriptScanner scanner = new WurstScriptScanner(reader);
			ExtendedParser parser = new ExtendedParser(scanner, gui );
			parser.setFilename(file.getAbsolutePath());
			Symbol sym = parser.parse();
			parseErrors = parser.getErrorCount();
			if (parseErrors > 0) {
				return Ast.CompilationUnit();
			}	
			CompilationUnit root = (CompilationUnit) sym.value;
			return root;
			
		} catch (CompileError e) {
			gui.sendError(e);
			return Ast.CompilationUnit();
		} catch (Exception e) {
			gui.sendError(new CompileError(Ast.WPos(file.toString(), 0, 0), "This is a bug and should not have happened.\n" + e.getMessage()));
			WLogger.severe(e);
			throw new Error(e);
		}
	}

	
	public JassProg getProg() {
		return prog;
	}

	
	
}
