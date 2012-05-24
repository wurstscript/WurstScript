package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;
import de.peeeq.eclipsewurstplugin.WurstPlugin;
import de.peeeq.eclipsewurstplugin.builder.WurstBuilder;
import de.peeeq.wurstscript.attributes.CompileError;


public class WurstMarkerAnnotationModel extends ResourceMarkerAnnotationModel {

	public WurstMarkerAnnotationModel(IResource resource) {
		super(resource);
	}

	@Override
	protected Position createPositionFromMarker(IMarker marker) {
		try {
			if(marker.isSubtypeOf(WurstBuilder.MARKER_TYPE)
					&& marker.getAttribute(IMarker.CHAR_START, -1) == -1){
				int start = marker.getAttribute(WurstPlugin.START_POS, -1);
				int end = marker.getAttribute(WurstPlugin.END_POS, -1);
				if (start >= 0 && end >= start) {
					return new Position(start, end-start);
					
				}
			}
		} catch (CoreException e) {
			return super.createPositionFromMarker(marker);
		}
		return super.createPositionFromMarker(marker);
	}
}
