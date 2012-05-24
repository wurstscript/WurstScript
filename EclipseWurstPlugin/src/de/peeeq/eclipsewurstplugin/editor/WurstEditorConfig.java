package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;


public class WurstEditorConfig extends SourceViewerConfiguration {

	private WurstEditor editor;

	public WurstEditorConfig(WurstEditor editor) {
		this.editor = editor;
		System.out.println("blas");
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
}
