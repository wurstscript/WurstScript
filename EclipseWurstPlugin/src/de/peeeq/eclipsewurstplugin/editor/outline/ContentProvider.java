package de.peeeq.eclipsewurstplugin.editor.outline;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY = new Object[] {};

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof OutlineNode) {
			OutlineNode outlineNode = (OutlineNode) inputElement;
			return getChildren(outlineNode);
		}
		return EMPTY;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof OutlineNode) {
			OutlineNode outlineNode = (OutlineNode) parentElement;
			return outlineNode.getChildren();
		}
		return null;
	}


	@Override
	public Object getParent(Object element) {
		if (element instanceof OutlineNode) {
			OutlineNode outlineNode = (OutlineNode) element;
			return outlineNode.getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof OutlineNode) {
			OutlineNode outlineNode = (OutlineNode) element;
			return outlineNode.getChildren() != null && outlineNode.getChildren().length > 0;
		}
		return false;
	}

}
