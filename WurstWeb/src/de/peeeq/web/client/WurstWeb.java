package de.peeeq.web.client;

import java.io.StringReader;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WurstChecker;
import de.peeeq.wurstscript.WurstConfig;
import de.peeeq.wurstscript.WurstParser;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.compilationserver.ResourceInputReader;
import de.peeeq.wurstscript.gui.WurstGuiLogger;
import de.peeeq.wurstscript.ast.Ast;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WurstWeb implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		exportStaticMethod();
		
//		final TextArea codeArea = new TextArea();
//		codeArea.setWidth("800px");
//		codeArea.setHeight("400px");
//		final Button compileButton = new Button("Compile");
//		
//		final Label errorLabel = new Label();
//	
//
//		// We can add style names to widgets
//		compileButton.addStyleName("sendButton");
//
//		
//		
//		RootPanel.get("editorArea").add(codeArea);
//		RootPanel.get("buttonArea").add(compileButton);
//		RootPanel.get("outputArea").add(errorLabel);
//
//		// Focus the cursor on the name field when the app loads
//		codeArea.setFocus(true);
//
//		
//
//		class MyHandler implements ClickHandler {
//			/**
//			 * Fired when the user clicks on the sendButton.
//			 */
//			public void onClick(ClickEvent event) {
//				
//			}
//		}
//
//		// Add a handler to send the name to the server
//		MyHandler handler = new MyHandler();
//		compileButton.addClickHandler(handler);
	}
	
	
	public static String parseWurst(String input) {
		WurstGuiLogger gui = new WurstGuiLogger();
		RunArgs runArgs = new RunArgs(new String[] {});
		
		WurstConfig config = new WurstConfig();
		ErrorHandler errorHandler = new ErrorHandler(gui);;
		
		WurstParser parser = new WurstParser(errorHandler, gui);
		
		CompilationUnit cu = parser.parse(new StringReader(input), "web", false);
		
		WurstModel model =  Ast.WurstModel(cu);
		
		WurstChecker checker = new WurstChecker(gui, errorHandler);
		checker.checkProg(model);
		
		if (gui.getErrorCount() > 0) {
			StringBuilder errs = new StringBuilder();
			for (CompileError err : gui.getErrorList()) {
				errs.append(err + "\n\n");
			}
			return errs.toString();
		} else {
			return "Ok";
		}
	}
	
	public static native void exportStaticMethod() /*-{
	    $wnd.wurst_parse =
	       $entry(@de.peeeq.web.client.WurstWeb::parseWurst(Ljava/lang/String;));
 	}-*/;

}
