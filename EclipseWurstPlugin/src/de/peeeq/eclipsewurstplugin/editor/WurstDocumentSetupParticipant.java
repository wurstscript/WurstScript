package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

import de.peeeq.eclipsewurstplugin.WurstPlugin;

import eclipsewurstplugin.Activator;

public class WurstDocumentSetupParticipant  implements IDocumentSetupParticipant {

		@Override
		public void setup(IDocument document) {
			if (document instanceof IDocumentExtension3) {
				IDocumentExtension3 extension3= (IDocumentExtension3) document;
				IDocumentPartitioner partitioner= new FastPartitioner(Activator.getDefault().scanners().wurstPartitionScanner(), WurstPlugin.PARTITION_TYPES);
				extension3.setDocumentPartitioner(WurstPlugin.WURST_PARTITIONING, partitioner);
				partitioner.connect(document);
			}
		}
	}