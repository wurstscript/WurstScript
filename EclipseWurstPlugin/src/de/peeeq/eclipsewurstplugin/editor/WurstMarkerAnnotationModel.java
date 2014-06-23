package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;
import de.peeeq.eclipsewurstplugin.WurstConstants;
import de.peeeq.eclipsewurstplugin.builder.WurstBuilder;
import de.peeeq.wurstscript.attributes.CompileError;


public class WurstMarkerAnnotationModel extends ResourceMarkerAnnotationModel {

	public WurstMarkerAnnotationModel(IResource resource) {
		super(resource);
	}

	@Override
	protected Position createPositionFromMarker(IMarker marker) {
		if(WurstBuilder.isWurstMarker(marker)) {
			if (marker.getAttribute(WurstConstants.END_POS, -1) == -1) {
				return null;
			}
			int start = marker.getAttribute(WurstConstants.START_POS, -1);
			int end = marker.getAttribute(WurstConstants.END_POS, -1);
			if (start >= 0 && end >= start) {
				return new Position(start, end-start);
				
			}
		}
		return super.createPositionFromMarker(marker);
	}

}
