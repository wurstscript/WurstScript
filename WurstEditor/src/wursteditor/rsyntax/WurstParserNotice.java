package wursteditor.rsyntax;

import java.awt.Color;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;

import com.google.common.base.Preconditions;

import de.peeeq.wurstscript.attributes.CompileError;

public class WurstParserNotice implements ParserNotice {

	private CompileError err;
	private WurstParser parser;

	public WurstParserNotice(WurstParser parser, CompileError e) {
		this.parser = parser;
		this.err = e;
	}

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean containsPosition(int i) {
		return i >getOffset2() && i < getOffset2() + getLength();
	}

	@Override
	public Color getColor() {
		return Color.RED;
	}

	@Override
	public int getLength() {
		return 5;
	}

	@Override
	public int getLevel() {
		return 1;
	}

	@Override
	public int getLine() {
		return err.getSource().getLine() -1;
	}

	@Override
	public String getMessage() {
		return err.getMessage();
	}

	@Override
	public int getOffset() {
		return -1;
	}
	private int getOffset2() {
		RSyntaxDocument doc = parser.getDoc();
		Preconditions.checkNotNull(doc);
		Token firstToken = doc.getTokenListForLine(err.getSource().getLine());
		return firstToken.offset + err.getSource().getColumn();
	}

	@Override
	public Parser getParser() {
		return parser;
	}

	@Override
	public boolean getShowInEditor() {
		return true;
	}

	@Override
	public String getToolTipText() {
		return getMessage();
	}

}
