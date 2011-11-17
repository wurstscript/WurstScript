package de.peeeq.wurstscript;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import java_cup.runtime.Symbol;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.TopLevelDeclaration;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.attr;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jasstranslation.JassTranslator;
import de.peeeq.wurstscript.parser.ExtendedParser;
import de.peeeq.wurstscript.parser.WurstScriptScanner;
import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.validation.WurstValidator;

public class WurstCompilerImpl implements WurstCompiler {

	private File[] files;
	private int parseErrors;
	private JassProg ilProg;
	private File outputMapFile;
	private WurstGui gui;

	
	public WurstCompilerImpl(WurstGui gui) {
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
		
		// validate the resource:
		WurstValidator validator = new WurstValidator(root);
		validator.validate();
		
		if (attr.getErrorCount() > 0) {
			for (CompileError err : attr.getErrors()) {
				gui.sendError(err);
			}
			return;
		}
		
		
		// translate to intermediate lang:
		JassTranslator translator = new JassTranslator(root);
		ilProg = translator.translate();
		
		if (attr.getErrorCount() > 0) {
			for (CompileError err : attr.getErrors()) {
				gui.sendError(err);
			}
			ilProg = null;
			return;
		}
	}

	private CompilationUnit mergeCompilationUnits(List<CompilationUnit> compilationUnits) {
		gui.sendProgress("Merging Files", 0.2);
//		List<TopLevelDeclaration> decls = new NotNullList<TopLevelDeclaration>();
//		for (CompilationUnit compilationUnit : compilationUnits) {
//			for (TopLevelDeclaration tld : compilationUnit) {
//				decls.add(tld);
//			}
//		}
		CompilationUnit result = Ast.CompilationUnit();
		for (CompilationUnit compilationUnit : compilationUnits) {
			while (!compilationUnit.isEmpty()) {
				TopLevelDeclaration x = compilationUnit.remove(0);
				result.add(x);
			}
		}
		return result; //Ast.CompilationUnit(decls.toArray(new TopLevelDeclaration[decls.size()]));
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

	
	public JassProg getILprog() {
		return ilProg;
	}

	
	
}
