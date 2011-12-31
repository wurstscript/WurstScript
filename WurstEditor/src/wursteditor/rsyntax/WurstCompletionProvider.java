package wursteditor.rsyntax;

import java.awt.Point;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.text.JTextComponent;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.LanguageAwareCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public class WurstCompletionProvider extends LanguageAwareCompletionProvider {


//	@Override
//	public String getAlreadyEnteredText(JTextComponent t) {
//		Caret caret = t.getCaret();
//		int pos = caret.getDot();
//		String text = t.getText();
//		int startPos = pos-1;
//		if (startPos < 0) {
//			return "";
//		}
//		while (startPos >= 0 && Character.isJavaIdentifierPart(text.charAt(startPos))) {
//			startPos--;
//		}
//		return text.substring(startPos+1, pos);
//	}
	
	/**
	 * Constructor.
	 */
	public WurstCompletionProvider() {
		setDefaultCompletionProvider(createCodeCompletionProvider());
		setStringCompletionProvider(createStringCompletionProvider());
		setCommentCompletionProvider(createCommentCompletionProvider());
	}


	@Override
	protected List<Completion> getCompletionsImpl(JTextComponent comp) {
		@SuppressWarnings("unchecked")
		List<Completion> completions = super.getCompletionsImpl(comp);


		
		completions.add(new BasicCompletion(this, "caret@ " + comp.getCaretPosition()));
		
		

		return completions;

	}
	
	
	
	/**
	 * Adds shorthand completions to the code completion provider.
	 *
	 * @param codeCP The code completion provider.
	 */
	protected void addShorthandCompletions(DefaultCompletionProvider codeCP) {
//		codeCP.addCompletion(new ShorthandCompletion(codeCP, "main", "int main(int argc, char **argv)"));
	}


	/**
	 * Returns the provider to use when editing code.
	 *
	 * @return The provider.
	 * @see #createCommentCompletionProvider()
	 * @see #createStringCompletionProvider()
	 * @see #loadCodeCompletionsFromXml(DefaultCompletionProvider)
	 * @see #addShorthandCompletions(DefaultCompletionProvider)
	 */
	protected CompletionProvider createCodeCompletionProvider() {
		DefaultCompletionProvider cp = new DefaultCompletionProvider() {
			@Override
			public List getCompletionsAt(JTextComponent tc, Point p) {
				System.out.println("get completions at " + p.x + " : " + p.y);
				return super.getCompletionsAt(tc, p);
			}
		};
		addDefaultKeywordComletions(cp);
		addShorthandCompletions(cp);
		return cp;

	}


	private void addDefaultKeywordComletions(DefaultCompletionProvider cp) {
		for (String keyword : WurstKeywords.KEYWORDS) {
			Completion c = new BasicCompletion(cp, keyword);
			cp.addCompletion(c);
		}
		for (String keyword : WurstKeywords.TYPES) {
			Completion c = new BasicCompletion(cp, keyword);
			cp.addCompletion(c);
		}
	}


	/**
	 * Returns the provider to use when in a comment.
	 *
	 * @return The provider.
	 * @see #createCodeCompletionProvider()
	 * @see #createStringCompletionProvider()
	 */
	protected CompletionProvider createCommentCompletionProvider() {
		DefaultCompletionProvider cp = new DefaultCompletionProvider();
		cp.addCompletion(new BasicCompletion(cp, "TODO:", "A to-do reminder"));
		cp.addCompletion(new BasicCompletion(cp, "FIXME:", "A bug that needs to be fixed"));
		return cp;
	}


	/**
	 * Returns the completion provider to use when the caret is in a string.
	 *
	 * @return The provider.
	 * @see #createCodeCompletionProvider()
	 * @see #createCommentCompletionProvider()
	 */
	protected CompletionProvider createStringCompletionProvider() {
		DefaultCompletionProvider cp = new DefaultCompletionProvider();
		cp.addCompletion(new BasicCompletion(cp, "%c", "char", "Prints a character"));
		cp.addCompletion(new BasicCompletion(cp, "%i", "signed int", "Prints a signed integer"));
		cp.addCompletion(new BasicCompletion(cp, "%f", "float", "Prints a float"));
		cp.addCompletion(new BasicCompletion(cp, "%s", "string", "Prints a string"));
		cp.addCompletion(new BasicCompletion(cp, "%u", "unsigned int", "Prints an unsigned integer"));
		cp.addCompletion(new BasicCompletion(cp, "\\n", "Newline", "Prints a newline"));
		return cp;
	}



	

	
}
