package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

import static de.peeeq.eclipsewurstplugin.WurstPlugin.*;

public class WurstPartitionScanner extends RuleBasedPartitionScanner {
	public WurstPartitionScanner() {
		IToken singleLineCommentToken = new Token(PARTITION_SINLGE_LINE_COMMENT);
		IToken multiLineCommentToken  = new Token(PARTITION_MULTI_LINE_COMMENT);
		IToken stringToken            = new Token(PARTITION_STRING);
		IToken charToken              = new Token(PARTITION_CHARACTER);
		
		IPredicateRule[] rules = {
				new EndOfLineRule("//", singleLineCommentToken),
				new MultiLineRule("/*","*/", multiLineCommentToken, '\\', true),
				new SingleLineRule("\"", "\"", stringToken, '\\'),
				new SingleLineRule("'", "'", charToken, '\\'),
		};
		setPredicateRules(rules);
	}	
}
