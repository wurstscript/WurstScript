package de.peeeq.wurstscript;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import java_cup.runtime.Symbol;
import de.peeeq.wurstscript.ast.AST;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.CompilationUnitPos;
import de.peeeq.wurstscript.ast.TopLevelDeclaration;
import de.peeeq.wurstscript.attributes.Attributes;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.intermediateLang.ILprog;
import de.peeeq.wurstscript.intermediateLang.translator.IntermediateLangTranslator;
import de.peeeq.wurstscript.parser.ExtendedParser;
import de.peeeq.wurstscript.parser.WurstScriptScanner;
import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.validation.WurstValidator;

public class WurstCompilerImpl implements WurstCompiler {

	private File[] files;
	private int parseErrors;
	private ILprog ilProg;
	private File outputMapFile;
	private WurstGui gui;

	
	public WurstCompilerImpl(WurstGui gui) {
		this.gui = gui;
	}

	@Override
	public void loadFiles(String[] filenames) {
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
	
	@Override public void parseFiles() {
		// parse all the files:
		List<CompilationUnit> compilationUnits = new NotNullList<CompilationUnit>();
		
		for (File file : files) {
			if (file.getName().endsWith(".w3x") || file.getName().endsWith(".w3m")) {
				compilationUnits.add(processMap(file));				
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
		CompilationUnitPos rootPos = AST.CompilationUnitPos(root);
		
		// create new attributes instance:
		Attributes attr = new Attributes(gui);
		
		// validate the resource:
		WurstValidator validator = new WurstValidator(rootPos, attr);
		validator.validate();
		
		if (attr.getErrorCount() > 0) {
			for (CompileError err : attr.getErrors()) {
				gui.sendError(err);
			}
			return;
		}
		
		
		// translate to intermediate lang:
		IntermediateLangTranslator translator = new IntermediateLangTranslator(rootPos, attr);
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
		List<TopLevelDeclaration> decls = new NotNullList<TopLevelDeclaration>();
		for (CompilationUnit compilationUnit : compilationUnits) {
			for (TopLevelDeclaration tld : compilationUnit) {
				decls.add(tld);
			}
		}
		return AST.CompilationUnit(decls.toArray(new TopLevelDeclaration[decls.size()]));
	}

	private CompilationUnit processMap(File file) {
		// TODO Auto-generated method stub
		throw new Error("Not implemented yet.");
	}

	private CompilationUnit parseFile(File file) {
		try {
			FileReader reader = new FileReader(file);
			// scanning
			WurstScriptScanner scanner = new WurstScriptScanner(reader);
			ExtendedParser parser = new ExtendedParser(scanner );
			parser.setFilename(file.getAbsolutePath());
			Symbol sym = parser.parse();
			parseErrors = parser.getErrorCount();
			if (parseErrors > 0) {
				return null;
			}	
			CompilationUnit root = (CompilationUnit) sym.value;
			return root;
			
			
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	
	public ILprog getILprog() {
		return ilProg;
	}
	
}
