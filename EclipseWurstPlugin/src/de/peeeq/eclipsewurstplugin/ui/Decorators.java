package de.peeeq.eclipsewurstplugin.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;

import com.google.common.base.Preconditions;

import de.peeeq.eclipsewurstplugin.editor.outline.Icons;

public class Decorators extends LabelProvider implements ILightweightLabelDecorator, IResourceChangeListener {

	private List<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();
	private Set<IResource> resources = new HashSet<>();
	
	public Decorators() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}
	
	@Override
	public void addListener(@Nullable ILabelProviderListener listener) {
		if (listener == null) return;
		listeners.add(listener);
	}
	
	@Override
	public void removeListener(@Nullable ILabelProviderListener listener) {
		listeners.remove(listener);
	}

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

	
	
	
	@Override
	public void resourceChanged(@Nullable IResourceChangeEvent event) {
		Preconditions.checkNotNull(event);
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			IResourceDelta delta = event.getDelta();
			
			
			IResourceDeltaVisitor visitor = delta1 -> {
				resources.add(delta1.getResource());
				return true;
			};
			try {
				synchronized (this) {
					visitor.visit(delta);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					fireLabelChangedEvent();
				}
			});
		}
	}

	private synchronized void fireLabelChangedEvent() {
		if (resources.isEmpty()) {
			return;
		}
		Object[] elements = resources.toArray();
		resources.clear();
		for (ILabelProviderListener listener : listeners) {
			listener.labelProviderChanged(new LabelProviderChangedEvent(this));
		}
	}

}
