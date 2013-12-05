package de.peeeq.eclipsewurstplugin.editor;

import java.util.Iterator;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

import de.peeeq.eclipsewurstplugin.builder.WurstBuilder;
import de.peeeq.eclipsewurstplugin.editor.autocomplete.WurstCompletionProcessor;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Documentable;
import de.peeeq.wurstscript.ast.ExprVarAccess;
import de.peeeq.wurstscript.ast.ExprVarArrayAccess;
import de.peeeq.wurstscript.ast.FuncRef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.utils.Utils;

public class WurstTextHover implements ITextHover,
//		ITextHoverExtension,
		ITextHoverExtension2 {

	private ISourceViewer sourceViewer;
	private WurstEditor editor;

	public WurstTextHover(ISourceViewer sourceViewer, WurstEditor editor) {
		this.sourceViewer = sourceViewer;
		this.editor = editor;
	}

	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		String annotationHover = getAnnotationHover(hoverRegion);
		if (annotationHover != null) {
			return annotationHover;
		}
		
		
		return getAstHover(textViewer, hoverRegion);
	}

	private String getAstHover(ITextViewer textViewer, IRegion hoverRegion) {
		CompilationUnit cu = editor.getCompilationUnit();
		if (cu == null) {
			return null;
		}
		AstElement elem = Utils.getAstElementAtPosIgnoringLists(cu, hoverRegion.getOffset(), true);
		try {
			return elem.descriptionHtml();
		} catch (Throwable t) {
//			t.printStackTrace();
			return null;
		}
	}

	public String getAnnotationHover(IRegion hoverRegion) {
		IAnnotationModel model= getAnnotationModel(sourceViewer);
		if (model == null)
			return null;

		Iterator<?> e= model.getAnnotationIterator();
		while (e.hasNext()) {
			Annotation a= (Annotation) e.next();
			if (isIncluded(a)) {
				Position p= model.getPosition(a);
				if (p != null && p.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
					String msg= a.getText();
					if (msg != null && msg.trim().length() > 0)
						return Utils.escapeHtml(msg);
				}
			}
		}
		return null;
	}

	private boolean isIncluded(Annotation annotation) {
		if(annotation instanceof SimpleMarkerAnnotation){
			SimpleMarkerAnnotation markerannotation = (SimpleMarkerAnnotation)annotation;
			return markerannotation.getMarker().exists() 
					&& WurstBuilder.isWurstMarker(markerannotation.getMarker());
		}
		return false;
	}

	private IAnnotationModel getAnnotationModel(ISourceViewer viewer) {
		if (viewer instanceof ISourceViewerExtension2) {
			ISourceViewerExtension2 extension= (ISourceViewerExtension2) viewer;
			return extension.getVisualAnnotationModel();
		}
		return viewer.getAnnotationModel();
	}
	
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		return getHoverInfo2(textViewer, hoverRegion).toString();
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		// TODO smarter region
		return new Region(offset,0);
	}

}
