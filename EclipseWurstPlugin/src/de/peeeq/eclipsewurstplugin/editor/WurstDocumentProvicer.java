package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import de.peeeq.eclipsewurstplugin.WurstPlugin;
import de.peeeq.eclipsewurstplugin.WurstConstants;

public class WurstDocumentProvicer extends FileDocumentProvider {

	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
//		if (document != null) {
//			WurstPartitionScanner scanner = new WurstPartitionScanner();                         
//			IDocumentPartitioner partitioner = new FastPartitioner(scanner, WurstPlugin.PARTITION_TYPES);
//			document.setDocumentPartitioner(partitioner);                                    
//			partitioner.connect(document);  
//		}
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3= (IDocumentExtension3) document;
			IDocumentPartitioner partitioner= new FastPartitioner(WurstPlugin.getDefault().scanners().wurstPartitionScanner(), WurstPartitionScanner.PARTITION_TYPES);
			extension3.setDocumentPartitioner(WurstConstants.WURST_PARTITIONING, partitioner);
			partitioner.connect(document);
		}
		return document;
	}
	
//	@Override
//	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
//		if (element instanceof IFileEditorInput) {
//			IFileEditorInput input = (IFileEditorInput) element;
//			return new WurstMarkerAnnotationModel(input.getFile());
//		}
//		return super.createAnnotationModel(element);
//	}
}
