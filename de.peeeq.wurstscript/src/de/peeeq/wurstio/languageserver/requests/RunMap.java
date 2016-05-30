package de.peeeq.wurstio.languageserver.requests;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import de.peeeq.wurstio.CompiletimeFunctionRunner;
import de.peeeq.wurstio.WurstCompilerJassImpl;
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
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by peter on 16.05.16.
 */
public class RunMap extends UserRequest {


	private final String wc3Path;
	private final File map;
	private final List<String> compileArgs;

	public RunMap(int requestNr, String wc3Path, File map, List<String> compileArgs) {
		super(requestNr);
		this.wc3Path = wc3Path;
		this.map = map;
		this.compileArgs = compileArgs;
	}

	@Override
	public Object execute(ModelManager modelManager) {

		// TODO use normal compiler for this, avoid code duplication
		WLogger.info("runMap " + map.getAbsolutePath() + " " + compileArgs);
		try {
			File frozenThroneExe = new File(wc3Path, "Frozen Throne.exe");


			if (!map.exists()) {
				return map.getAbsolutePath() + " does not exist.";
			}

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
			File compiledScript = compileScript(modelManager, compileArgs, testMap);


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

			println("preparing testmap ... ");






			// then inject the script into the map
			File outputMapscript = compiledScript;
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

			println("running " + cmd);
			Process p = Runtime.getRuntime().exec(cmd.toArray(new String[0]));
		} catch (CompileError e) {
			e.printStackTrace();
			showMessage("There was an error when compiling the map: \n" + e);
			print("\n" + e + "\n");
		} catch (final Exception e) {
			WLogger.severe(e);
			final String message = "Could not start the map.\n\nPlease check the configuration in Window>Preferences>Wurst. \n\n"
					+ "The exact error message is:\n " + e.getMessage();
			showMessage(message);
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

	private @Nullable File compileScript(ModelManager modelManager, List<String> compileArgs, File mapFile) throws Exception {

		if (compileArgs.contains("-clean")) {
			println("Cleaning project ... ");
			cleanProject(modelManager);
			compileArgs.remove("-clean");
		}


		modelManager.buildProject();

		if (modelManager.hasErrors()) {
			return null;
		}

		WurstModel modelCopy = modelManager.getModel().copy();

		RunArgs runArgs = new RunArgs(compileArgs);

		MpqEditor mpqEditor = null;
		if (mapFile != null) {
			mpqEditor = MpqEditorFactory.getEditor(mapFile, runArgs);
		}

		WurstGui gui = new WurstGuiLogger();
		WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui, mpqEditor, runArgs);
		compiler.setMapFile(mapFile);
		WurstModel model = modelCopy;
		purgeUnimportedFiles(model);
		model.clearAttributes();



		print("checking program ... ");
		compiler.checkProg(model);

		if (gui.getErrorCount() > 0) {
			print("Could not compile project\n");
			return null;
		}

		print("translating program ... ");
		compiler.translateProgToIm(model);

		if (gui.getErrorCount() > 0) {
			print("Could not compile project\n");
			return null;
		}


		if (runArgs.runCompiletimeFunctions()) {
			print("running compiletime functions ... ");
			// compile & inject object-editor data
			// TODO run optimizations later?
			gui.sendProgress("Running compiletime functions", 0.91);
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
			return null;
		}


		JassPrinter printer = new JassPrinter(true, jassProg);
		String mapScript = printer.printProg();


		// TODO create file
		return null;
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
		return file.matches("(.*/|^)wurst/.*") && Utils.isWurstFile(file);
	}
}
