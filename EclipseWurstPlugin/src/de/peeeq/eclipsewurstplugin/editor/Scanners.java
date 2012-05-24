package de.peeeq.eclipsewurstplugin.editor;

import static de.peeeq.eclipsewurstplugin.WurstPlugin.SYNTAXCOLOR_BOLD;
import static de.peeeq.eclipsewurstplugin.WurstPlugin.SYNTAXCOLOR_COLOR;
import static de.peeeq.eclipsewurstplugin.WurstPlugin.SYNTAXCOLOR_COMMENT;
import static de.peeeq.eclipsewurstplugin.WurstPlugin.SYNTAXCOLOR_ITALIC;
import static de.peeeq.eclipsewurstplugin.WurstPlugin.SYNTAXCOLOR_STRIKETHROUGH;
import static de.peeeq.eclipsewurstplugin.WurstPlugin.SYNTAXCOLOR_STRING;
import static de.peeeq.eclipsewurstplugin.WurstPlugin.SYNTAXCOLOR_UNDERLINE;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import de.peeeq.eclipsewurstplugin.WurstPlugin;
import de.peeeq.eclipsewurstplugin.util.UtilityFunctions;

import eclipsewurstplugin.Activator;

public class Scanners {
	
	
	
	
	public static class SingleCommentScanner extends RuleBasedScanner implements WurstScanner {
		public SingleCommentScanner() {
			IPreferenceStore store = UtilityFunctions.getDefaultPreferenceStore();
			Color color = new Color(Display.getCurrent(),PreferenceConverter.getColor(store, SYNTAXCOLOR_COLOR + SYNTAXCOLOR_COMMENT));
			IToken token = new Token(new TextAttribute(color, null, UtilityFunctions.computeAttributes(store, SYNTAXCOLOR_COMMENT)));
			IRule singleLineRule = new EndOfLineRule("//",token); 
			setRules(new IRule[] {singleLineRule});
		}

		@Override
		public String getPartitionType() {
			return WurstPlugin.PARTITION_SINLGE_LINE_COMMENT;
		}


		
	}
	public static class MultiCommentScanner extends RuleBasedScanner implements WurstScanner {
		public MultiCommentScanner(){
			IPreferenceStore store = UtilityFunctions.getDefaultPreferenceStore();
			Color color = new Color(Display.getCurrent(),PreferenceConverter.getColor(store, SYNTAXCOLOR_COLOR + SYNTAXCOLOR_COMMENT));
			IToken token = new Token(new TextAttribute(color, null, UtilityFunctions.computeAttributes(store, SYNTAXCOLOR_COMMENT)));
			IRule multiLineRule = new MultiLineRule("/*", "*/", token, '\\', true);
			setRules(new IRule[] {multiLineRule});
		}

		@Override
		public String getPartitionType() {
			return WurstPlugin.PARTITION_MULTI_LINE_COMMENT;
		}
	}
	public static class StringScanner extends RuleBasedScanner implements WurstScanner {
		public StringScanner() {
			IPreferenceStore store = UtilityFunctions.getDefaultPreferenceStore();
			Color color = new Color(Display.getCurrent(),PreferenceConverter.getColor(store, SYNTAXCOLOR_COLOR + SYNTAXCOLOR_STRING));
			
//			color = new Color(Display.getCurrent(), 255, 0, 0);
			System.out.println("color = " + color);
			IToken token = new Token(new TextAttribute(color, null, UtilityFunctions.computeAttributes(store, SYNTAXCOLOR_STRING)));
			IRule singleLineRule = new SingleLineRule("\"", "\"", token, '\\');
			setRules(new IRule[] {singleLineRule});
		}

		@Override
		public String getPartitionType() {
			return WurstPlugin.PARTITION_STRING;
		}
	}
	public static class CharacterScanner extends RuleBasedScanner implements WurstScanner {
		public CharacterScanner() {
			IPreferenceStore store = UtilityFunctions.getDefaultPreferenceStore();
			Color color = new Color(Display.getCurrent(),PreferenceConverter.getColor(store, SYNTAXCOLOR_COLOR + SYNTAXCOLOR_STRING));
			IToken token = new Token(new TextAttribute(color, null, UtilityFunctions.computeAttributes(store, SYNTAXCOLOR_STRING)));
			IRule singleLineRule = new SingleLineRule("'", "'", token, '\\');
			setRules(new IRule[] {singleLineRule});
		}

		@Override
		public String getPartitionType() {
			return WurstPlugin.PARTITION_CHARACTER;
		}
	}

}