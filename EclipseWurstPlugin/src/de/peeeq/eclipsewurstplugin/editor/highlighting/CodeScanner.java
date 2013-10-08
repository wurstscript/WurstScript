package de.peeeq.eclipsewurstplugin.editor.highlighting;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import static de.peeeq.eclipsewurstplugin.WurstConstants.*;
import de.peeeq.eclipsewurstplugin.util.UtilityFunctions;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.parser.TokenType;
import de.peeeq.wurstscript.parser.WurstScriptScanner;
import java_cup.runtime.Symbol;

public class CodeScanner implements WurstScanner {

	private static final IToken DEFAULT_RETURN_TOKEN = new Token(null);
	WurstScriptScanner scanner = null;
	private Token keywordToken;
	private int lastLength;
	private int lastOffset;
	private int startOffset;
	private int endOFfset;
	private Token jasstypeToken;
	
	public CodeScanner() {
		IPreferenceStore preferencestore = UtilityFunctions.getDefaultPreferenceStore();
		keywordToken = makeToken(preferencestore, SYNTAXCOLOR_KEYWORD);
		jasstypeToken = makeToken(preferencestore, SYNTAXCOLOR_JASSTYPE);
	}

	private Token makeToken(IPreferenceStore preferencestore, String key) {
		return new Token(new TextAttribute(new Color(Display.getCurrent(),
				PreferenceConverter.getColor(preferencestore, SYNTAXCOLOR_COLOR + key)), null,
				UtilityFunctions.computeAttributes(preferencestore, key)));
	}
	
	@Override
	public void setRange(IDocument document, int offset, int length) {
		try {
			scanner = new WurstScriptScanner(new StringReader(document.get()));
			startOffset = offset;
			endOFfset = offset + length;
			lastLength = 0;
			lastOffset = 0;
		} catch (Error e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public IToken nextToken() {
		if (scanner != null) {
			try {
				Symbol t = scanner.next_token();
				if (t == null) {
					return null;
				}
				lastOffset = t.left;
				lastLength = t.right - t.left;
				if (t.right >= endOFfset) {
					return Token.EOF;
				}
				WLogger.info("t.sym = " + t.sym + ", " + t.value);
				switch (t.sym) {
					case 0: return Token.EOF;
					case TokenType.IDENTIFIER:
						return DEFAULT_RETURN_TOKEN;
				}
				if (t.sym > 0) {
//					return keywordToken;
				}
				return DEFAULT_RETURN_TOKEN;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return Token.EOF;
	}

	@Override
	public int getTokenOffset() {
		WLogger.info("offset = " + lastOffset);
		return lastOffset;
	}

	@Override
	public int getTokenLength() {
		WLogger.info("lastLength = " + lastLength);
		return lastLength;
	}

	@Override
	public String getPartitionType() {
		return IDocument.DEFAULT_CONTENT_TYPE;
	}

}
