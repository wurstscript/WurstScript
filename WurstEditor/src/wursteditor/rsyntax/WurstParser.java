package wursteditor.rsyntax;

import java.io.Reader;
import java.io.StringReader;

import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;

import wursteditor.controller.SyntaxCodeAreaController;
import wursteditor.controller.WurstEditorController;

import de.peeeq.wurstscript.WurstCompiler;
import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;

public class WurstParser extends AbstractParser {

	private RSyntaxDocument doc;
	private SyntaxCodeAreaController syntaxCodeAreaController;

	public WurstParser(SyntaxCodeAreaController syntaxCodeAreaController) {
		this.syntaxCodeAreaController = syntaxCodeAreaController;
	}

	@Override
	public ParseResult parse(RSyntaxDocument doc, String lang) {
		this.doc = doc;
		if (!lang.equals("wurstscript")) {
			return new DefaultParseResult(this); 
		}
		return parse(doc, true);
	}

	public ParseResult parse(RSyntaxDocument doc, boolean doChecks) {
		DefaultParseResult result = new DefaultParseResult(this);
		WurstGui gui = new WurstGuiLogger();
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(gui);
		Reader reader;
		try {
			reader = new StringReader(doc.getText(0, doc.getLength()));
			CompilationUnit cu = comp.parse(reader, "temp.wurst");
			syntaxCodeAreaController.setAst(cu);
			if (doChecks) {
				comp.checkProg(cu);
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (doChecks) {
			for (CompileError e : gui.getErrorList()) {
				System.out.println(e);
				ParserNotice notice = new WurstParserNotice(this, e);
				result.addNotice(notice);
			}
			syntaxCodeAreaController.setErrors(gui.getErrorList());
		}
		return result;
	}

	public RSyntaxDocument getDoc() {
		return doc;
	}



	
}
