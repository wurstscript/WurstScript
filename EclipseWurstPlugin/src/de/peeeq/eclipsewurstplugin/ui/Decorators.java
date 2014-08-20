package de.peeeq.eclipsewurstplugin.ui;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import de.peeeq.eclipsewurstplugin.editor.outline.Icons;

public class Decorators extends LabelProvider implements ILightweightLabelDecorator  {


	@Override
	public void decorate(@Nullable Object element, @Nullable IDecoration decoration) {
		if (element == null || decoration == null) {
			return;
		}
		if (element instanceof IResource) {
			IResource r = (IResource) element;
			try {
				if (!r.isAccessible()) {
					return;
				}
				IMarker[] markers = r.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
				if (markers == null || markers.length == 0) {
					return;
				}
				int severity = IMarker.SEVERITY_WARNING;
				for (IMarker m : markers) {
					if (m.getAttribute(IMarker.SEVERITY, 0) == IMarker.SEVERITY_ERROR) {
						severity = IMarker.SEVERITY_ERROR;
					}
				}
				ImageDescriptor img;
				if (severity == IMarker.SEVERITY_WARNING) {
					img = ImageDescriptor.createFromImage(Icons.wwarning);
				} else {
					img = ImageDescriptor.createFromImage(Icons.werror);
				}
				decoration.addOverlay(img, IDecoration.BOTTOM_LEFT);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

}
