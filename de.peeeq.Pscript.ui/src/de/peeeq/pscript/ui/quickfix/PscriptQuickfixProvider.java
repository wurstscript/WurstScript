
package de.peeeq.pscript.ui.quickfix;

import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.edit.IModification;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.quickfix.DefaultQuickfixProvider;
import org.eclipse.xtext.ui.editor.quickfix.Fix;
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionAcceptor;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.validation.Issue;

import de.peeeq.pscript.validation.PscriptJavaValidator;

public class PscriptQuickfixProvider extends DefaultQuickfixProvider {

//	@Fix(MyJavaValidator.INVALID_NAME)
//	public void capitalizeName(final Issue issue, IssueResolutionAcceptor acceptor) {
//		acceptor.accept(issue, "Capitalize name", "Capitalize the name.", "upcase.png", new IModification() {
//			public void apply(IModificationContext context) throws BadLocationException {
//				IXtextDocument xtextDocument = context.getXtextDocument();
//				String firstLetter = xtextDocument.get(issue.getOffset(), 1);
//				xtextDocument.replace(issue.getOffset(), 1, firstLetter.toUpperCase());
//			}
//		});
//	}
	@Fix(PscriptJavaValidator.INVALID_TYPE_NAME)
	public void fixName(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, 
			    "Capitalize name", // quick fix label
			    "Capitalize name  of '" + issue.getData()[0] + "'",  // description 
			    "upcase.png",      // quick fix icon
			    new IModification() {

					@Override
					public void apply(IModificationContext context)
							throws Exception {
						// TODO Auto-generated method stub
						IXtextDocument xtextDocument = context.getXtextDocument();
						String firstLetter = xtextDocument.get(issue.getOffset(), 1);
						xtextDocument.replace(issue.getOffset(), 1, Strings.toFirstUpper(firstLetter));
					}
			    }
//		new ISemanticModification() {
//	        public void apply(EObject element, IModificationContext context) {
//	        	ClassDef c = (ClassDef) element;
//	        	c.setName(Strings.toFirstUpper(c.getName()));
//	        }
//	      }
			  );
	}

}
