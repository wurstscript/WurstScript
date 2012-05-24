package de.peeeq.eclipsewurstplugin.editor;

import org.eclipse.jface.text.rules.ITokenScanner;

public interface WurstScanner extends ITokenScanner {
	String getPartitionType();
}
