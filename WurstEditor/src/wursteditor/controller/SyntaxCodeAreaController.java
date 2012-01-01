package wursteditor.controller;

import java.util.List;
import java.util.Map;

import javax.swing.JList;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.FoldManager;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;

import com.google.common.collect.Maps;

import wursteditor.rsyntax.IndentationFoldParser;
import wursteditor.rsyntax.WurstCompletionProvider;
import wursteditor.rsyntax.WurstParser;
import wursteditor.rsyntax.WurstTokenMaker;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.attributes.CompileError;

public class SyntaxCodeAreaController {

	private static Map<RSyntaxTextArea, SyntaxCodeAreaController> areaToController = Maps.newHashMap();
	private JList errorList;
	private CompilationUnit ast;
	private RSyntaxTextArea syntaxCodeArea;
	private WurstParser parser;

	public SyntaxCodeAreaController(RSyntaxTextArea syntaxCodeArea, JList errorList) {
		this.syntaxCodeArea = syntaxCodeArea;
		areaToController.put(syntaxCodeArea, this);
		this.errorList = errorList;
		initWurst();

		syntaxCodeArea.setSyntaxEditingStyle("wurstscript");
		
		// install auto completion
		CompletionProvider wurstAutoCompletion = new WurstCompletionProvider();
		AutoCompletion ac = new AutoCompletion(wurstAutoCompletion);
		ac.install(syntaxCodeArea);

		// code folding
		FoldParserManager.get().addFoldParserMapping("wurstscript", new IndentationFoldParser());
		FoldManager foldManager = new FoldManager(syntaxCodeArea);
		foldManager.setCodeFoldingEnabled(true);
		syntaxCodeArea.setCodeFoldingEnabled(true); // this seems bugged
		
		syntaxCodeArea.setAutoIndentEnabled(true);
		this.parser = new WurstParser(this);
	    syntaxCodeArea.addParser(parser);

	}

	private void initWurst() {
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
		atmf.putMapping("wurstscript", WurstTokenMaker.class.getCanonicalName());
		TokenMakerFactory.setDefaultInstance(atmf);
	}

	public void setErrors(List<CompileError> errorList) {
		this.errorList.setListData(errorList.toArray());	
	}

	public void setAst(CompilationUnit cu) {
		if (cu != null) {
			if (ast == null || cu.size() > 0) {
				this.ast = cu;
			}
		}
	}
	
	public CompilationUnit getAst() {
		return ast;
	}

	public static SyntaxCodeAreaController getFor(RSyntaxTextArea comp) {
		return areaToController.get(comp);
	}

	public void reparseDocument() {
		syntaxCodeArea.addNotify();		
	}

	/**
	 * tries to get a new AST, before returning it
	 */
	public CompilationUnit getNewAst() {
		parser.parse((RSyntaxDocument) syntaxCodeArea.getDocument(), false);
		return ast;
	}

}
