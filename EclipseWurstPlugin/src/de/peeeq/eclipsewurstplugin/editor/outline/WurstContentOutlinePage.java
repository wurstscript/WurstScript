package de.peeeq.eclipsewurstplugin.editor.outline;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.google.common.collect.Lists;

import de.peeeq.eclipsewurstplugin.editor.CompilationUnitChangeListener;
import de.peeeq.eclipsewurstplugin.editor.WurstEditor;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.parser.WPos;

public class WurstContentOutlinePage extends ContentOutlinePage implements CompilationUnitChangeListener {

	private IEditorInput editorInput;
	private IDocumentProvider documentProvider;
	private WurstEditor editor;
	private ContentProvider contentProvider;
	private boolean selectionMoveCursor;
	private volatile boolean disposed = false;

	public WurstContentOutlinePage(IDocumentProvider documentProvider, WurstEditor wurstEditor) {
		this.documentProvider = documentProvider;
		this.editor = wurstEditor;
		contentProvider = new ContentProvider();
	}

	public void setInput(IEditorInput editorInput) {
		this.editorInput = editorInput;
	}
	
	
	@Override
	public void createControl(Composite parent) {

		super.createControl(parent);

		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new WurstLabelProvider()); // TODO create own label provider
		viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS); // TODO do not expand all levels?
		viewer.addSelectionChangedListener(this);
		

		if (editor.getCompilationUnit() != null) {
			viewer.setInput(new OutlineNode(editor.getCompilationUnit(), null));
		}
		
		editor.registerCompilationUnitChangeListener(this);
	}
	
	
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		
		ISelection selection= event.getSelection();
		if (selection.isEmpty()) {
			editor.resetHighlightRange();
		} else if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object selectedElem = structuredSelection.getFirstElement();
			if (selectedElem instanceof OutlineNode) {
				OutlineNode outlineNode = (OutlineNode) selectedElem;
				WPos pos = outlineNode.getNode().attrSource();
				try {
					int length = Math.max(pos.getRightPos() - pos.getLeftPos(), 0);
					editor.setHighlightRange(pos.getLeftPos(), length, selectionMoveCursor);
				} catch (IllegalArgumentException x) {
					editor.resetHighlightRange();
				}
			}
			
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		disposed  = true;
	}

	@Override
	public void onCompilationUnitChanged(final CompilationUnit newCu) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (getTreeViewer() != null && !disposed) {
					// TODO  java.lang.IllegalStateException: Need an underlying widget to be able to set the input.(Has the widget been disposed?)
					getTreeViewer().setInput(new OutlineNode(newCu, null));
				}
			}
		});
		
		
	}

	public void selectNodeByPos(int offset) {
		Object input = getTreeViewer().getInput();
		if (input instanceof OutlineNode) {
			OutlineNode outlineNode = (OutlineNode) input;
			OutlineNode sel = findNodeInLine(outlineNode, offset);
			if (sel != null) {
//				ISelection selection = new StructuredSelection(sel); 
				List<Object> segments = Lists.newArrayList();
				while (sel != null) {
					segments.add(0, sel);
					sel = sel.getParent();
				}
				ISelection selection = new TreeSelection(new TreePath(segments.toArray()));
				setSelectionWithoutCursorMove(selection);
			}
		}
		
	}

	private void setSelectionWithoutCursorMove(ISelection selection) {
		selectionMoveCursor = false;
		setSelection(selection);
		selectionMoveCursor = true;
	}

	private OutlineNode findNodeInLine(OutlineNode node, int startOffset) {
		if (node.getNode().attrSource().getLeftPos() > startOffset) {
			return null;
		}
		OutlineNode result = node;
		for (Object child : contentProvider.getChildren(node)) {
			if (child instanceof OutlineNode) {
				OutlineNode childNode = (OutlineNode) child;
				OutlineNode r = findNodeInLine(childNode, startOffset);
				if (r != null) {
					result = r;
				} else {
					break;
				}
				
			}
		}
		return result;
	}


}
