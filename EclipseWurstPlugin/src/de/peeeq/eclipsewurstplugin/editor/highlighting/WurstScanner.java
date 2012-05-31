package de.peeeq.eclipsewurstplugin.editor.highlighting;

import org.eclipse.jface.text.rules.ITokenScanner;

public interface WurstScanner extends ITokenScanner {
	String getPartitionType();
}
