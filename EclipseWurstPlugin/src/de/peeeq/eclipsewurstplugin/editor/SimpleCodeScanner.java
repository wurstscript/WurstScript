package de.peeeq.eclipsewurstplugin.editor;

import static de.peeeq.eclipsewurstplugin.WurstPlugin.SYNTAXCOLOR_COLOR;
import static de.peeeq.eclipsewurstplugin.WurstPlugin.SYNTAXCOLOR_KEYWORD;

import javax.swing.text.html.parser.Parser;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import de.peeeq.eclipsewurstplugin.WurstPlugin;
import de.peeeq.eclipsewurstplugin.util.UtilityFunctions;

public class SimpleCodeScanner extends RuleBasedScanner implements WurstScanner {

	private static final String[] KEYWORDS = new String[] { "class", "return", "if", "else", "while", "for", "in", "break", "new", "null",
			"package", "endpackage", "function", "returns", "public", "private", "protected", "import", "native", "nativetype", "extends",
			"interface", "implements", "module", "use", "abstract", "static", "thistype", "override", "immutable", "it", "array", "and",
			"or", "not", "this", "construct", "ondestroy", "destroy", "type", "globals", "endglobals", "constant", "endfunction",
			"nothing", "takes", "local", "loop", "endloop", "exitwhen", "set", "call", "then", "elseif", "endif", "init", "castTo",
			"tuple", "div", "mod", "let", "from", "to", "downto", "step", "endpackage", "skip" };
	private Token keywordToken;
	private Token commentToken;
	private Token stringToken;

	public SimpleCodeScanner() {
		IPreferenceStore preferencestore = UtilityFunctions.getDefaultPreferenceStore();
		keywordToken = makeToken(preferencestore, WurstPlugin.SYNTAXCOLOR_KEYWORD);
		commentToken = makeToken(preferencestore, WurstPlugin.SYNTAXCOLOR_COMMENT);
		stringToken = makeToken(preferencestore, WurstPlugin.SYNTAXCOLOR_STRING);
		IToken identifierToken = new Token(new TextAttribute(new Color(Display.getCurrent(), 0,0,0), null, 0));
		
		WordRule keywordRule = new WordRule(new IWordDetector() {
			public boolean isWordStart(char c) {
				return Character.isJavaIdentifierStart(c);
			}

			public boolean isWordPart(char c) {
				return Character.isJavaIdentifierPart(c);
			}
		}, identifierToken, false);
		// add tokens for each reserved word
		for (String keyword : KEYWORDS) {
			keywordRule.addWord(keyword, keywordToken);
		}
		
		WhitespaceRule whitespaceRule = new WhitespaceRule(new IWhitespaceDetector() {
			public boolean isWhitespace(char c) {
				return Character.isWhitespace(c);
			}
		});
		setRules(new IRule[] { 
				new SingleLineRule("//", null, commentToken), 
				new SingleLineRule("\"", "\"", stringToken, '\\'),
				new SingleLineRule("'", "'", stringToken, '\\'), 
				new MultiLineRule("/*", "*/", commentToken),
				whitespaceRule,
				keywordRule
			});
	}

	private Token makeToken(IPreferenceStore preferencestore, String key) {
		return new Token(new TextAttribute(new Color(Display.getCurrent(), PreferenceConverter.getColor(preferencestore, SYNTAXCOLOR_COLOR
				+ key)), null, UtilityFunctions.computeAttributes(preferencestore, key)));
	}

	@Override
	public String getPartitionType() {
		return IDocument.DEFAULT_CONTENT_TYPE;
	}

}
