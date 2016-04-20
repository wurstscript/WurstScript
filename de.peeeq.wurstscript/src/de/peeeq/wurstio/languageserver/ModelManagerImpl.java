package de.peeeq.wurstio.languageserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Sets;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;

/**
 * keeps a version of the model which is always the most recent one
 */
public class ModelManagerImpl implements ModelManager {

	private volatile @Nullable WurstModel model;
	private File projectPath;
	private boolean needsFullBuild;
	// dependency folders (folders mentioned in wurst.dependencies) 
	private final Set<String> dependencies = Sets.newLinkedHashSet();
	private WurstGui gui = new WurstGuiCliImpl();
	private List<Consumer<CompilationResult>> onCompilationResultListeners = new ArrayList<>();

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
	public synchronized boolean removeCompilationUnit(String resource) {
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
	public synchronized void clean() {
		model = null;
		dependencies.clear();
		needsFullBuild = true;
		WLogger.info("Clean done.");
	}

	/** does a full build, reading whole directory 
	 * @throws IOException */
	public void buildProject() throws IOException {
		readDependencies();
		
		File wurstFolder = new File(projectPath, "wurst");
		processWurstFiles(wurstFolder);
		
		resolveImports(gui);
		
		doTypeCheck(gui, true);
		// TODO report errors
		
		Map<String, List<CompileError>> groupedByFile = gui.getErrorList().stream().collect(Collectors.groupingBy(err -> err.getSource().getFile()));
		for (Entry<String, List<CompileError>> err : groupedByFile.entrySet()) {
			CompilationResult r = new CompilationResult(err.getKey(), err.getValue());
			onCompilationResultListeners.forEach(c -> c.accept(r));
		}
	}



	private void processWurstFiles(File dir) throws IOException {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				processWurstFiles(f);
			} else if (f.getName().endsWith(".wurst")
					|| f.getName().endsWith(".jurst")) {
				processWurstFile(f);
			}
		}
	}


	private void processWurstFile(File f) throws IOException {
		System.out.println("processing file " + f);
		syncCompilationUnit(f);
	}


	private void readDependencies() throws IOException {
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
				if (line == null) break;
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
	


	private void addDependency(WurstGui gui, File depfile, String fileName, LineOffsets lineOffsets, int offset, int endOffset) {
		File f = new File(fileName);
		WPos pos = new WPos(getProjectRelativePath(depfile), lineOffsets, offset, endOffset);
		if (!f.exists()) {
			gui.sendError(new CompileError(pos, "Path '"+fileName + "' could not be found."));
			return;
		} else if (!f.isDirectory()) {
			gui.sendError(new CompileError(pos, "Path '"+fileName + "' is not a folder."));
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
	 * 'toCheck'
	 */
	private void clearAttributes(List<CompilationUnit> toCheck) {
		WurstModel model2 = model;
		if (model2 == null) {
			return;
		}
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
			}
		}

	}

	/** check whether cu imports something from 'toCheck' */
	private boolean imports(CompilationUnit cu, Set<String> packageNames, boolean importPublic) {
		for (WPackage p : cu.getPackages()) {
			if (imports(p, packageNames, false, Sets.<WPackage> newHashSet())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * check whether p imports something from 'toCheck'
	 * 
	 * @param hashSet
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
		// this line is not synchronized, because it can trigger a build in a
		// different thread
		WurstCompilerJassImpl comp = getCompiler(gui);
		synchronized (this) {
			long time = System.currentTimeMillis();
			if (gui.getErrorCount() > 0) {
				if (addErrorMarkers) {
					reportErrorsForProject(gui);
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
				reportErrorsForProject(gui);
			}
		}
	}


	private void reportErrorsForProject(WurstGui gui) {
		// TODO Auto-generated method stub
		
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
	}

	

	private CompilationUnit compileFromJar(WurstGui gui, String filename) throws IOException {
		InputStream source = this.getClass().getResourceAsStream(filename);
		if (source == null) {
			System.err.println("could not find " + filename + " in jar");
			source = new FileInputStream("./resources/" +filename);
		}
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(gui, null, RunArgs.defaults());

		try (InputStreamReader reader = new InputStreamReader(source)) {
			CompilationUnit cu = comp.parse(filename, reader);
			cu.setFile(filename);
			return cu;
		}
	}






	private void resolveImports(WurstGui gui) {
		WurstCompilerJassImpl comp = getCompiler(gui);
		synchronized (this) {
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
	}


	@Override
	public boolean syncCompilationUnit(String filename) throws IOException {
		File f = new File(projectPath, filename);
		return syncCompilationUnit(f);
	}


	private boolean syncCompilationUnit(File f) throws IOException, FileNotFoundException {
		String filename = getProjectRelativePath(f);
		WurstCompilerJassImpl c = getCompiler(gui);
		CompilationUnit cu;
		try (FileReader reader = new FileReader(f)) {
			cu = c.parse(filename, reader);
			cu.setFile(filename);
		}
		updateModel(cu, gui);
		return false;
	}


	@Override
	public boolean updateCompilationUnit(String filename, String contents) {
		WurstCompilerJassImpl c = getCompiler(gui);
		CompilationUnit cu = c.parse(filename, new StringReader(contents));
		updateModel(cu, gui);
		return false;
	}


	@Override
	public void onCompilationResult(Consumer<CompilationResult> f) {
		onCompilationResultListeners.add(f);
	}


	public static void main(String[] args) throws IOException {
		ModelManagerImpl m = new ModelManagerImpl("/home/peter/work/EBR");
		m.onCompilationResult(cr -> {
			System.err.println(cr.getFilename());
			for (CompileError err : cr.getErrors()) {
				System.err.println("	" + err);
			}
		});
		m.buildProject();
		System.out.println("done:");
//		for (CompilationUnit cu : m.model) {
//			System.out.println("   " + cu.getFile());	
//		}
		
	}
	



}
