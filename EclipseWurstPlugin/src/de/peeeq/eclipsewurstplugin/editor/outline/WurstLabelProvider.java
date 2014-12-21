package de.peeeq.eclipsewurstplugin.editor.outline;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class WurstLabelProvider extends LabelProvider implements ILabelProvider {

	@Override
	public Image getImage(Object element) {
		if (element instanceof OutlineNode) {
			OutlineNode outlineNode = (OutlineNode) element;
			return outlineNode.getImage();
		} else if (element instanceof IFile) {
			IFile iFile = (IFile) element;
			return Icons.file;
		}
		return Icons.file;
	}

	@Override
	public String getText(Object element) {
		return element.toString();
	}


}
