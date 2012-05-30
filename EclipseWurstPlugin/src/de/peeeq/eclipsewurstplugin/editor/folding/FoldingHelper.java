package de.peeeq.eclipsewurstplugin.editor.folding;

import java.util.Iterator;

import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;

import de.peeeq.eclipsewurstplugin.editor.WurstEditor;
import de.peeeq.wurstscript.ast.AstElementWithSource;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.ModuleDef;

public class FoldingHelper {

	WurstEditor editor;
	private ProjectionSupport fProjectionSupport;
	
	public FoldingHelper(WurstEditor editor) {
		this.editor = editor;
	}
	
	
	// forward to this
	public void adjustHighlightRange(ISourceViewer viewer, int offset, int length) {
		// if range is highlighted, then this region should be exposed and not hidden:
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			extension.exposeModelRange(new Region(offset, length));
		}
	}
	
	private IAnnotationModel getAnnotationModel() {
		return (IAnnotationModel) editor.getAdapter(ProjectionAnnotationModel.class);
	}
	
//	// overwrite this
//	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
//		// we override this method to create a projectionviewer
//		// this viewer can fold code.
//		fAnnotationAccess= createAnnotationAccess();
//		fOverviewRuler= createOverviewRuler(getSharedColors());
//
//		ISourceViewer viewer= new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
//		// ensure decoration support has been created and configured.
//		getSourceViewerDecorationSupport(viewer);
//
//		return viewer;
//	}
	
	// call from getAdaper()
	public Object getAdapter(Class<?> adapter, ISourceViewer sourceViewer) {
		if (fProjectionSupport != null) {
			Object result= fProjectionSupport.getAdapter(sourceViewer, adapter);
			if (result != null)
				return result;
		}
		return null;
	}

	// call this when compilation unit is updated
	public void updateFolding() {
		final IAnnotationModel model= getAnnotationModel();
		if (model == null) {
			System.err.println("no annotation model defined");
			return;
		}
		clearFoldings(model);
		editor.getCompilationUnit().accept(new CompilationUnit.DefaultVisitor() {
			@Override
			public void visit(ClassDef e) {
				addFolding(model, e);
			}
			
			@Override
			public void visit(InterfaceDef e) {
				addFolding(model, e);
			}
			
			@Override
			public void visit(ModuleDef e) {
				addFolding(model, e);
			}
			
			@Override
			public void visit(FuncDef e) {
				if (e.getBody().size() > 0) {
					addFolding(model, e);
				}
			}
			
			@Override
			public void visit(ExtensionFuncDef e) {
				addFolding(model, e);
			}
		});
	}


	private void clearFoldings(IAnnotationModel model) {
		Iterator<?> it = model.getAnnotationIterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof ProjectionAnnotation) {
				ProjectionAnnotation a = (ProjectionAnnotation) o;
				if (a.isCollapsed()) {
					continue;
				}
				a.markCollapsed();
			}
			it.remove();
		}
	}


	private void addFolding(IAnnotationModel model, AstElementWithSource e) {
		addFolding(model, e.getSource().getLeftPos(), e.getSource().getRightPos() + 1);
	}
	
	private void addFolding(IAnnotationModel model, int startOffset, int endOffset) {
		Position position= new Position(startOffset, endOffset - startOffset);
		if (isPositionCollapsed(model, position)) {
			return;
		}
		ProjectionAnnotation a = new ProjectionAnnotation();
		model.addAnnotation(a, position);
	}
	
	private boolean isPositionCollapsed(IAnnotationModel model, Position position) {
		Iterator<?> it = model.getAnnotationIterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof ProjectionAnnotation) {
				ProjectionAnnotation a = (ProjectionAnnotation) o;
				if (a.isCollapsed()) {
					Position aPos = model.getPosition(a);
					if (aPos.getOffset() <= position.getOffset()
							&& aPos.getOffset() + aPos.getLength() >= position.getOffset() + position.getLength()) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	// call from createPartControl
	public ProjectionSupport initProjectionSupport(ProjectionViewer viewer, IAnnotationAccess annotationAccess, ISharedTextColors sharedColors) {
		fProjectionSupport = new ProjectionSupport(viewer, annotationAccess, sharedColors);
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
		fProjectionSupport.install();
		viewer.doOperation(ProjectionViewer.TOGGLE);
		return fProjectionSupport;
	}
	
}
