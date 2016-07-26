package de.peeeq.wurstio.languageserver.requests;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import de.peeeq.wurstio.CompiletimeFunctionRunner;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.gui.WurstGuiImpl;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.utils.Utils;

/**
 * Created by peter on 16.05.16.
 */
public class RunMap extends UserRequest {


	private final String wc3Path;
	private final File map;
	private final List<String> compileArgs;
	private final String workspaceRoot;
	/** makes the compilation slower, but more safe by discarding results from the editor and working on a copy of the model */
	private boolean safeCompilation = false;

	public RunMap(int requestNr, String workspaceRoot, String wc3Path, File map, List<String> compileArgs) {
		super(requestNr);
		this.workspaceRoot = workspaceRoot;
		this.wc3Path = wc3Path;
		this.map = map;
		this.compileArgs = compileArgs;
	}

	@Override
	public Object execute(ModelManager modelManager) {

		// TODO use normal compiler for this, avoid code duplication
		WLogger.info("runMap " + map.getAbsolutePath() + " " + compileArgs);
		WurstGui gui = new WurstGuiImpl(workspaceRoot);
		try {
			File frozenThroneExe = new File(wc3Path, "Frozen Throne.exe");
			

			if (!map.exists()) {
				throw new RuntimeException(map.getAbsolutePath() + " does not exist.");
			}
			
			
			gui.sendProgress("Copying map");

			// now copy the map so that we do not corrupt the original
			// create the file in the wc3 maps directory, because otherwise it does not work sometimes
			String testMapName = "wurstTestMap1.w3x";
			File testMap = new File(new File(wc3Path, "Maps"), testMapName);
			if (testMap.exists()) {
				testMap.delete();
			}
			testMap.delete();
			Files.copy(map, testMap);



			// first compile the script:
			File compiledScript = compileScript(gui, modelManager, compileArgs, testMap);
			

			WurstModel model = modelManager.getModel();
			if (model == null
					|| !model.stream()
					.anyMatch((CompilationUnit cu) -> cu.getFile().endsWith("war3map.j"))) {
				println("No 'war3map.j' file could be found");
				println("If you compile the map with WurstPack once, this file should be in your wurst-folder. ");
				println("We will try to start the map now, but it will probably fail. ");
			}

			@SuppressWarnings("unused") // for side effects!
			RunArgs runArgs = new RunArgs(compileArgs);

			gui.sendProgress("preparing testmap ... ");






			// then inject the script into the map
			File outputMapscript = compiledScript;
			
			gui.sendProgress("Injecting mapscript");
			try (MpqEditor mpqEditor = MpqEditorFactory.getEditor(testMap, runArgs)) {
				//			MpqEditor mpqEditor = new WinMpq("C:\\work\\WurstScript\\Wurstpack\\winmpq\\WinMPQ.exe");
				mpqEditor.deleteFile("war3map.j");
				mpqEditor.insertFile("war3map.j", Files.toByteArray(outputMapscript));
			}


			String testMapName2 = "wurstTestMap.w3x";
			File testMap2 = new File(new File(wc3Path, "Maps"), testMapName2);
			Files.copy(testMap, testMap2);

			println("Starting wc3 ... ");

			// now start the map
			List<String> cmd = Lists.newArrayList(
					frozenThroneExe.getAbsolutePath(),
					"-window",
					"-loadfile",
					"Maps" + File.separator + testMapName2);

			if (!System.getProperty("os.name").startsWith("Windows")) {
				// run with wine
				cmd.add(0, "wine");
			}

			gui.sendProgress("running " + cmd);
			Process p = Runtime.getRuntime().exec(cmd.toArray(new String[0]));
		} catch (CompileError e) {
			e.printStackTrace();
			showMessage("There was an error when compiling the map: \n" + e);
			print("\n" + e + "\n");
		} catch (final Throwable e) {
			e.printStackTrace();
			WLogger.severe(e);
			final String message = "Could not start the map.\n\nPlease check the configuration in Window>Preferences>Wurst. \n\n"
					+ "The exact error message is:\n " + e.getMessage();
			showMessage(message);
		} finally {
			gui.sendFinished();
		}
		return "ok"; // TODO
	}

	private void showMessage(String message) {
		WLogger.warning(message);
	}

	private void print(String s) {
		WLogger.info(s);
	}

	private void println(String s) {
		WLogger.info(s);
	}

	private File compileScript(WurstGui gui, ModelManager modelManager, List<String> compileArgs, File mapFile) throws Exception {
		RunArgs runArgs = new RunArgs(compileArgs);
		
		
		File war3mapFile = new File(new File(new File(workspaceRoot), "wurst"), "war3map.j");
		if (!war3mapFile.exists()) {
			// if war3map file does not exist yet, try to get it from the map:
			byte[] mapScript;
			try (MpqEditor mpqEditor = MpqEditorFactory.getEditor(mapFile, runArgs)) {
				mapScript = mpqEditor.extractFile("war3map.j");
			}
			if (new String(mapScript, StandardCharsets.UTF_8).startsWith(JassPrinter.WURST_COMMENT_RAW)) {
				throw new RuntimeException("Cannot use mapscript from map file, because it already was compiled with wurst. Please add war3map.j to the wurst directory.");
			}
			// write it to workspace, will be picked up by later build process
			Files.write(mapScript, war3mapFile);
		}
		
		if (compileArgs.contains("-clean")) {
			gui.sendProgress("Cleaning project");
			println("Cleaning project ... ");
			cleanProject(modelManager);
			compileArgs.remove("-clean");
		}


		gui.sendProgress("Building project");
		modelManager.buildProject();

		if (modelManager.hasErrors()) {
			throw new RuntimeException("Model has errors");
		}

		WurstModel modelCopy = modelManager.getModel().copy();
		

		MpqEditor mpqEditor = null;
		if (mapFile != null) {
			mpqEditor = MpqEditorFactory.getEditor(mapFile, runArgs);
		}

		//WurstGui gui = new WurstGuiLogger();
		
		WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui, mpqEditor, runArgs);
		compiler.setMapFile(mapFile);
		WurstModel model = modelCopy;
		purgeUnimportedFiles(model);
		
		if (safeCompilation) {
			gui.sendProgress("Clearing model");
			model.clearAttributes();
		}



		gui.sendProgress("Check program");
		compiler.checkProg(model);

		if (gui.getErrorCount() > 0) {
			print("Could not compile project\n");
			System.err.println("Could not compile project: " + gui.getErrorList().get(0));
			throw new RuntimeException("Could not compile project: " + gui.getErrorList().get(0));
		}

		print("translating program ... ");
		compiler.translateProgToIm(model);

		if (gui.getErrorCount() > 0) {
			print("Could not compile project (error in translation)\n");
			System.err.println("Could not compile project (error in translation): " + gui.getErrorList().get(0));
			throw new RuntimeException("Could not compile project (error in translation): " + gui.getErrorList().get(0));
		}


		if (runArgs.runCompiletimeFunctions()) {
			print("running compiletime functions ... ");
			// compile & inject object-editor data
			// TODO run optimizations later?
			gui.sendProgress("Running compiletime functions");
			CompiletimeFunctionRunner ctr = new CompiletimeFunctionRunner(compiler.getImProg(), compiler.getMapFile(), compiler.getMapfileMpqEditor(), gui, FunctionFlagEnum.IS_COMPILETIME);
			ctr.setInjectObjects(runArgs.isInjectObjects());
			ctr.setOutputStream(new PrintStream(System.out));
			ctr.run();
		}

		print("translating program to jass ... ");
//		compiler.checkAndTranslate(model);
		compiler.transformProgToJass();

		JassProg jassProg = compiler.getProg();
		if (jassProg == null) {
			print("Could not compile project\n");
			throw new RuntimeException("Could not compile project (error in JASS translation)");
		}


		gui.sendProgress("Printing program");
		JassPrinter printer = new JassPrinter(true, jassProg);
		String mapScript = printer.printProg();


		// TODO create file
		
		File outFile = new File(new File(workspaceRoot), "compiled.j.txt");
		Files.write(mapScript.getBytes(Charsets.UTF_8), outFile);
		return outFile;
		/*
		IFile f = project.getFile("compiled.j.txt");
		if (f.exists()) {
			f.delete(true, null);
		}
		InputStream source = new ByteArrayInputStream(mapScript.getBytes(Charsets.UTF_8));

		f.create(source, true, null);

		print("Output created in " + f.getFullPath() + "\n");
		if (mpqEditor != null) {
			mpqEditor.close();
		}

		return f;
		*/
	}

	private void cleanProject(ModelManager modelManager) {
		modelManager.clean();
	}

	/**
	 * removes everything compilation unit which is neither
	 *  - inside a wurst folder
	 *  - a jass file
	 *  - imported by a file in a wurst folder
	 */
	private void purgeUnimportedFiles(WurstModel model) {
		Set<CompilationUnit> inWurstFolder = model.stream()
				.filter(cu -> isInWurstFolder(cu.getFile())
						|| cu.getFile().endsWith(".j"))
				.collect(Collectors.toSet());


		Set<CompilationUnit> imported = new HashSet<>(inWurstFolder);
		addImports(imported, imported);

		model.removeIf(cu -> !imported.contains(cu));
	}

	private void addImports(Set<CompilationUnit> result, Set<CompilationUnit> toAdd) {
		Set<CompilationUnit> imported = toAdd.stream()
				.flatMap((CompilationUnit cu) -> cu.getPackages().stream())
				.flatMap((WPackage p) -> p.getImports().stream())
				.map(WImport::attrImportedPackage)
				.filter(p -> p != null)
				.map(WPackage::attrCompilationUnit)
				.collect(Collectors.toSet());
		boolean changed = result.addAll(imported);
		if (changed) {
			// recursive call terminates, as there are only finitely many compilation units
			addImports(result, imported);
		}
	}

	private boolean isInWurstFolder(String file) {
		File f = new File(workspaceRoot + "/" + file);
		return f.exists() && Utils.isWurstFile(file);
	}
}
