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

import wursteditor.controller.WurstEditorController;

import de.peeeq.wurstscript.WurstCompiler;
import de.peeeq.wurstscript.WurstCompilerJassImpl;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiLogger;

public class WurstParser extends AbstractParser {

	private RSyntaxDocument doc;

	@Override
	public ParseResult parse(RSyntaxDocument doc, String lang) {
		this.doc = doc;
		DefaultParseResult result = new DefaultParseResult(this);
		if (!lang.equals("wurstscript")) {
			return result; 
		}
		WurstGui gui = new WurstGuiLogger();
		WurstCompilerJassImpl comp = new WurstCompilerJassImpl(gui);
		Reader reader;
		try {
			reader = new StringReader(doc.getText(0, doc.getLength()));
			comp.loadReader("temp.wurst", reader );
			comp.parseFiles();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		for (CompileError e : gui.getErrorList()) {
			System.out.println(e);
			ParserNotice notice = new WurstParserNotice(this, e);
			result.addNotice(notice);
		}
		WurstEditorController.getInstance().setErrors(gui.getErrorList());
		return result;
	}

	public RSyntaxDocument getDoc() {
		return doc;
	}


	
}
