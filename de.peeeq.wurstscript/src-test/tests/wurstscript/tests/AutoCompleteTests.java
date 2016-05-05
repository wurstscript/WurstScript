package tests.wurstscript.tests;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.languageserver.LanguageServer;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.requests.GetCompletions;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

public class AutoCompleteTests extends WurstScriptTest {




	@Test
	public void parenthesis1() {



		String buffer = input(
				"package test",
				"	init",
				"		int x = 5",
				"		x.",
				"endpackage"
		);


		GetCompletions c = new GetCompletions(1, "test", buffer, 4, 5);

		ModelManager modelManager = new ModelManagerImpl(new File("."));

		Handler h = new ConsoleHandler();
		WLogger.setHandler(h);

		Object result = c.execute(modelManager);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(gson.toJson(result));

	}

	private String input(String...lines) {
		return String.join("\n", lines);
	}

	private WurstModel compile(String ... lines) {
		String input = String.join("\n", lines);
		WurstGui gui = new WurstGuiLogger();
		RunArgs runArgs = new RunArgs();
		WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui, null, runArgs);
		compiler.getErrorHandler().enableUnitTestMode();
		Map<String, String> inputMap = ImmutableMap.of("test" , input);
		return parseFiles(Collections.<File>emptyList(), inputMap, false, compiler);
	}


}
