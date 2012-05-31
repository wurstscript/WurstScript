package de.peeeq.eclipsewurstplugin.editor.highlighting;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * parses input as one big token 
 */
public class SingleTokenScanner extends BufferedRuleBasedScanner {
	public SingleTokenScanner(TextAttribute attribute) {
		setDefaultReturnToken(new Token(attribute));
	}
}
