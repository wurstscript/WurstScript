package wursteditor.rsyntax;

import java.awt.Point;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.text.JTextComponent;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.LanguageAwareCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ExprMemberVar;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeNamedScope;
import de.peeeq.wurstscript.utils.Utils;

import wursteditor.controller.SyntaxCodeAreaController;

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
		
		String alreadyEntered = getAlreadyEnteredText(comp);
		
		@SuppressWarnings("unchecked")
		List<Completion> completions = super.getCompletionsImpl(comp);
		if (comp instanceof RSyntaxTextArea) {
			SyntaxCodeAreaController controller = SyntaxCodeAreaController.getFor((RSyntaxTextArea)comp);
			if (controller != null) {
				CompilationUnit ast = controller.getNewAst();
				
				AstElement elem = AstHelper.getAstElementAtPos(ast, comp.getCaretPosition());
				if (elem instanceof ExprMemberVar) {
					ExprMemberVar e = (ExprMemberVar) elem;
					completions.clear();
					PscriptType leftType = e.getLeft().attrTyp();
					if (leftType instanceof PscriptTypeNamedScope) {
						PscriptTypeNamedScope ns = (PscriptTypeNamedScope) leftType;
						Multimap<String, NameDef> visibleNames = ns.getDef().attrVisibleNamesPrivate();
						completionsAddVisibleNames(alreadyEntered, completions, visibleNames);
					}
					
					// add member vars
					WScope scope = elem.attrNearestScope();
					while (scope != null) {
						Multimap<String, NameDef> visibleNames = scope.attrVisibleNamesPrivate();
						
						completionsAddVisibleExtensionFunctions(alreadyEntered, completions, visibleNames, leftType);
						scope = scope.attrNextScope();
					}
				} else {
					WScope scope = elem.attrNearestScope();
					while (scope != null) {
						Multimap<String, NameDef> visibleNames = scope.attrVisibleNamesPrivate();
						completionsAddVisibleNames(alreadyEntered, completions, visibleNames);
						scope = scope.attrNextScope();
					}
				}
				System.out.println("We are in " + elem);
				
			}
		}
		

		return completions;

	}


	private void completionsAddVisibleExtensionFunctions(String alreadyEntered,
			List<Completion> completions,
			Multimap<String, NameDef> visibleNames, PscriptType leftType) {
		for (Entry<String, NameDef> e : visibleNames.entries()) {
			if (!e.getKey().startsWith(alreadyEntered)) {
				continue;
			}
			if (e.getValue() instanceof ExtensionFuncDef) {
				ExtensionFuncDef ef = (ExtensionFuncDef) e.getValue();
				if (ef.getExtendedType().attrTyp().isSupertypeOf(leftType)) {
					completions.add(new BasicCompletion(this, e.getKey()));
				}
			}
		}
		
	}


	private void completionsAddVisibleNames(String alreadyEntered,
			List<Completion> completions, Multimap<String, NameDef> visibleNames) {
		for (Entry<String, NameDef> e : visibleNames.entries()) {
			if (!e.getKey().startsWith(alreadyEntered)) {
				continue;
			}
			if (e.getValue() instanceof FunctionDefinition) {
				FunctionDefinition f = (FunctionDefinition) e.getValue();
				FunctionCompletion completion = new FunctionCompletion(this, f.getName(), f.getName() + "()");
				List<String> params = Utils.map(f.getParameters(), new Function<WParameter, String>() {

					@Override
					public String apply(WParameter arg0) {
						return arg0.getName();
					}
					
				});
				completion.setParams(params);
				completion.setReturnValueDescription(f.getReturnTyp().attrTyp().toString());
				completions.add(completion);
			} else {
				completions.add(new BasicCompletion(this, e.getKey()));
			}
			
		}
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
		DefaultCompletionProvider cp = new DefaultCompletionProvider();
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



	

	@Override
	public boolean isAutoActivateOkay(JTextComponent jtextcomponent) {
		return true;
	}
}
