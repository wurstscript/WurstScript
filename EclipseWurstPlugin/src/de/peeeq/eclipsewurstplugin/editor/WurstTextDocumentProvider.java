package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.peeeq.eclipsewurstplugin.WurstConstants;
import de.peeeq.eclipsewurstplugin.WurstPlugin;

public class WurstTextDocumentProvider extends TextFileDocumentProvider implements IDocumentProvider {

	@Override
	protected FileInfo createFileInfo(Object element) throws CoreException {
		FileInfo info = super.createFileInfo(element);
		if(info==null){
			info = createEmptyFileInfo();
		}
		IDocument document = info.fTextFileBuffer.getDocument();
		if (document != null) {

			/* register your partitioner and other things here 
                       same way as in your fisrt document provider */
			if (document instanceof IDocumentExtension3) {
				IDocumentExtension3 extension3= (IDocumentExtension3) document;
				IDocumentPartitioner partitioner= new FastPartitioner(WurstPlugin.getDefault().scanners().wurstPartitionScanner(), WurstPartitionScanner.PARTITION_TYPES);
				extension3.setDocumentPartitioner(WurstConstants.WURST_PARTITIONING, partitioner);
				partitioner.connect(document);
			}
		}
		return info;
	}

}
