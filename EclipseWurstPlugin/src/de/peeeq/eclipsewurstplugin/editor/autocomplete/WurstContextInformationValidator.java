package de.peeeq.eclipsewurstplugin.editor.autocomplete;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class WurstContextInformationValidator implements IContextInformationValidator {

	private ITextViewer viewer;
	private IContextInformation info;
	private int installOffset;

	@Override
	public void install(IContextInformation info, ITextViewer viewer, int offset) {
		this.info = info;
		this.viewer = viewer;
		this.installOffset = offset;

	}

	@Override
	public boolean isContextInformationValid(int offset) {
		// TODO real implementation
		return Math.abs(installOffset - offset) < 5;
	}

}
