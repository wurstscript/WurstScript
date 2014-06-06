package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Shell;

import de.peeeq.eclipsewurstplugin.WurstConstants;
import de.peeeq.eclipsewurstplugin.WurstPlugin;
import de.peeeq.eclipsewurstplugin.editor.autocomplete.WurstCompletionProcessor;
import de.peeeq.eclipsewurstplugin.editor.autoedit.WurstAutoIndentStrategy;
import de.peeeq.eclipsewurstplugin.editor.highlighting.WurstScanner;
import de.peeeq.eclipsewurstplugin.editor.reconciling.WurstReconcilingStategy;
import de.peeeq.wurstscript.WLogger;



public class WurstEditorConfig extends SourceViewerConfiguration {

	private WurstEditor editor;

	public WurstEditorConfig(WurstEditor editor) {
		this.editor = editor;
		WLogger.info("blas");
	}
	
	@Override
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
		return getIndentPrefixesForTab(4);
	}
	
	

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
//		PresentationReconciler reconciler = new PresentationReconciler();
////		registerRepairer(reconciler, new Scanners.SingleCommentScanner());
////		registerRepairer(reconciler, new Scanners.MultiCommentScanner());
////		registerRepairer(reconciler, new Scanners.StringScanner());
////		registerRepairer(reconciler, new Scanners.CharacterScanner());
//		registerRepairer(reconciler, new SimpleCodeScanner());
//		return reconciler;
		
		WurstPlugin plugin = WurstPlugin.getDefault();
		PresentationReconciler reconciler= new PresentationReconciler();
//		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		reconciler.setDocumentPartitioning(WurstConstants.WURST_PARTITIONING);

		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(plugin.scanners().wurstCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr= new DefaultDamagerRepairer(plugin.scanners().hotDocScanner());
		reconciler.setDamager(dr, WurstPartitionScanner.HOT_DOC);
		reconciler.setRepairer(dr, WurstPartitionScanner.HOT_DOC);

		dr= new DefaultDamagerRepairer(plugin.scanners().multilineCommentScanner());
		reconciler.setDamager(dr, WurstPartitionScanner.WURST_MULTILINE_COMMENT);
		reconciler.setRepairer(dr, WurstPartitionScanner.WURST_MULTILINE_COMMENT);

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
		if (WurstPlugin.config().reconilationEnabled()) {
			r.setDelay((int) (WurstPlugin.config().reconilationDelay()*1000));
		} else {
			r.setDelay(Integer.MAX_VALUE);
		}
		return r;
	}
	
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType){
		return new WurstTextHover(sourceViewer, editor);
	}
	
	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
	    return new DefaultAnnotationHover();
	}
	
	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		return new IHyperlinkDetector[] {new WurstHyperlinkDetector(editor)};
	}
	
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		return new IAutoEditStrategy[] {
				new WurstAutoIndentStrategy()
		};
	}
	
	ContentAssistant assistant;

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if (assistant == null) {
			assistant = new ContentAssistant();
			assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
			assistant.setContentAssistProcessor(new WurstCompletionProcessor(editor), IDocument.DEFAULT_CONTENT_TYPE);
			
			assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
			assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
	//		assistant.setContextInformationPopupBackground(...);
			assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
			assistant.enableAutoInsert(true);
		}
		if (WurstPlugin.config().autocompleteEnabled()) {
			assistant.enableAutoActivation(true);
			assistant.setAutoActivationDelay((int) (1000*WurstPlugin.config().autocompleteDelay()));
		} else {
			assistant.enableAutoActivation(false);
		}
		return assistant;
	}
	
    @Override
    public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
        return new IInformationControlCreator() {
            public IInformationControl createInformationControl(Shell parent) {
                //return new DefaultInformationControl(parent,new HTMLTextPresenter(false));
//            	return new BrowserInformationControl(parent, "sans", false);
            	return new WurstInformationControl(parent);
            }

        };
    }
	
}
