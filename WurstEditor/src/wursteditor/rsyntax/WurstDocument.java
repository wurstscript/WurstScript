package wursteditor.rsyntax;

import java.util.Set;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

import com.google.common.collect.Sets;

public class WurstDocument extends RSyntaxDocument {

	private static final Set<String> breakWords = Sets.newHashSet(
		"package", "function", "if", "else", "while", "loop", "class", "module"
	);

	public WurstDocument(String syntaxStyle) {
		super(syntaxStyle);
	}
	
	@Override
	public boolean getShouldIndentNextLine(int line) {
		Token token = getTokenListForLine(line);
		while (token != null) {
			if (token.type == TokenTypes.RESERVED_WORD) {
				String content = token.getLexeme();
				if (breakWords.contains(content)) {
					return true;
				}
			}
			token = token.getNextToken();
			
		}
		return false;
	}

}
