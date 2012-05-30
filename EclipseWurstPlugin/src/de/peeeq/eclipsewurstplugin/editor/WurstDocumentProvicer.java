package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import de.peeeq.eclipsewurstplugin.WurstPlugin;

public class WurstDocumentProvicer extends FileDocumentProvider {

	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			WurstPartitionScanner scanner = new WurstPartitionScanner();                         
			IDocumentPartitioner partitioner = new FastPartitioner(scanner, WurstPlugin.PARTITION_TYPES);
			document.setDocumentPartitioner(partitioner);                                    
			partitioner.connect(document);  
		}
		return document;
	}
	
	@Override
	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		if (element instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput) element;
			return new WurstMarkerAnnotationModel(input.getFile());
		}
		return super.createAnnotationModel(element);
	}
}
