package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

import de.peeeq.eclipsewurstplugin.builder.WurstBuilder;
import de.peeeq.eclipsewurstplugin.editor.autoedit.WurstAutoIndentStrategy;
import de.peeeq.eclipsewurstplugin.editor.reconciling.WurstReconcilingStategy;


public class WurstEditorConfig extends SourceViewerConfiguration {

	private WurstEditor editor;

	public WurstEditorConfig(WurstEditor editor) {
		this.editor = editor;
		System.out.println("blas");
	}
	
	@Override
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
		return getIndentPrefixesForTab(4);
	}
	
	

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
//		registerRepairer(reconciler, new Scanners.SingleCommentScanner());
//		registerRepairer(reconciler, new Scanners.MultiCommentScanner());
//		registerRepairer(reconciler, new Scanners.StringScanner());
//		registerRepairer(reconciler, new Scanners.CharacterScanner());
		registerRepairer(reconciler, new SimpleCodeScanner());
		return reconciler;
	}

	private void registerRepairer(PresentationReconciler reconciler, WurstScanner scanner) {
		DefaultDamagerRepairer dr =	new DefaultDamagerRepairer(scanner);
		reconciler.setDamager(dr, scanner.getPartitionType());
		reconciler.setRepairer(dr, scanner.getPartitionType());

	}
	
	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		WurstReconcilingStategy strategy = new WurstReconcilingStategy(editor);
		MonoReconciler r = new MonoReconciler(strategy , false);
		// check after x ms:
		r.setDelay(500);
		return r;
	}
	
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType){
		return new DefaultTextHover(sourceViewer){
			@Override
			protected boolean isIncluded(Annotation annotation) {
				if(annotation instanceof SimpleMarkerAnnotation){
					SimpleMarkerAnnotation markerannotation = (SimpleMarkerAnnotation)annotation;
					try {
						return markerannotation.getMarker().exists() 
							&& (markerannotation.getMarker().isSubtypeOf(WurstBuilder.MARKER_TYPE));
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
				return false;
			}
		};
	}
	
	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		return new IHyperlinkDetector[] {new WurstHylerlinkDetector(editor)};
	}
	
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
//		return super.getAutoEditStrategies(sourceViewer, contentType);
		return new IAutoEditStrategy[] {
				new WurstAutoIndentStrategy()
		};
	}
}
