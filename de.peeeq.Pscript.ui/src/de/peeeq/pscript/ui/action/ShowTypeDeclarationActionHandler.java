package de.peeeq.pscript.ui.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import com.google.inject.Inject;


public class ShowTypeDeclarationActionHandler extends AbstractHandler {

//	@Inject
//	private ITypesystem ts;
	
	@Inject
	private IQualifiedNameProvider qfnp;
	
	private final class ShowTypeDeclarationAction extends Action {
		private XtextEditor activeEditor;
		private int offset;
		private XtextResource resource;

		private ShowTypeDeclarationAction(XtextEditor activeEditor, int offset, XtextResource resource) {
			this.activeEditor = activeEditor;
			this.offset = offset;
			this.resource = resource;
		}

		public void run() {
			String description = getDescription(offset, resource);
			Point positionAbsolute = getPosition(activeEditor);
			new TypeInformationPopUp(activeEditor.getSite().getShell(), description, positionAbsolute).open();
		}

		private Point getPosition(final XtextEditor activeEditor) {
			ITextSelection selection = (ITextSelection) activeEditor.getSelectionProvider().getSelection();
			Point positionRelative = activeEditor.getInternalSourceViewer().getTextWidget().getLocationAtOffset(
					selection.getOffset());
			Point positionAbsolute = activeEditor.getInternalSourceViewer().getTextWidget().toDisplay(positionRelative);
			positionAbsolute.y += 15;
			return positionAbsolute;
		}

		private String getDescription(final int offset, final XtextResource resource) {
//			IParseResult parseResult = resource.getParseResult();
//			ICompositeNode rootNode = parseResult.getRootNode();
//			ILeafNode node = NodeModelUtils.findLeafNodeAtOffset(rootNode, offset);
//			EObject semanticObject = NodeModelUtils.findActualSemanticObjectFor(node);
//			StringBuffer bf = new StringBuffer();
//			bf.append( "QName: "+qfnp.getFullyQualifiedName(semanticObject )+"\n" );
//			bf.append( "Metaclass: "+semanticObject.eClass().getName()+"\n" );
//			TypeCalculationTrace trace = new TypeCalculationTrace();
//			EObject type = ts.typeof(semanticObject, trace);
//			if ( type != null ) {
//				bf.append("Runtime Type: "+ts.typeString(type));
//			} else {
//				bf.append("Runtime Type: <no type>");
//			}
//			bf.append("\n\nTrace: ");
//			for (String s: trace.toStringArray()) {
//				bf.append("\n  "+s);
//			}
			return "roflkopter \n" +
					"offset = " + offset + "\n" +
							"resource = " + resource;
		}
	}

	private final class TypeInformationPopUp extends PopupDialog {
		private String description;

		private final Point position;

		public TypeInformationPopUp(Shell parent, String description, Point position) {
			super(parent, PopupDialog.HOVER_SHELLSTYLE, true, false, false, false, false, "Type Info", null);
			this.description = description;
			this.position = position;
		}

		protected Point getInitialLocation(Point initialSize) {
			return position;
		}

		protected Control createDialogArea(Composite parent) {
			Label label = new Label(parent, SWT.WRAP);
			label.setText(description);
			label.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent event) {
					close();
				}
			});
			// Use the compact margins employed by
			// PopupDialog.
			GridData gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
			gd.horizontalIndent = PopupDialog.POPUP_HORIZONTALSPACING;
			gd.verticalIndent = PopupDialog.POPUP_VERTICALSPACING;
			label.setLayoutData(gd);
			return label;
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		final XtextEditor activeEditor = (XtextEditor) HandlerUtil.getActiveEditor(event);
		final IXtextDocument document = activeEditor.getDocument();
		final int offset = ((StyledText) activeEditor.getAdapter(Control.class)).getCaretOffset();
		document.readOnly(new IUnitOfWork.Void<XtextResource>() {
			public void process(final XtextResource resource) throws Exception {
				new ShowTypeDeclarationAction(activeEditor, offset, resource).run();
			}
		});
		return this;
	}

}
