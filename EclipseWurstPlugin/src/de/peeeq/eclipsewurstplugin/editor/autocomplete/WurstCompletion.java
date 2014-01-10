package de.peeeq.eclipsewurstplugin.editor.autocomplete;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;

public class WurstCompletion implements Comparable<WurstCompletion> {
	private final String replacementString;
	private final int replacementOffset;
	private final int replacementLength;
	private final int cursorPosition;
	private final Image image;
	private final String displayString;
	private final IContextInformation contextInformation;
	private final String additionalProposalInfo;
	private final double rating;
	
	

	public WurstCompletion(String replacementString, int replacementOffset,
			int replacementLength, int cursorPosition, Image image,
			String displayString, IContextInformation contextInformation,
			String additionalProposalInfo,
			double rating) {
		super();
		this.replacementString = replacementString;
		this.replacementOffset = replacementOffset;
		this.replacementLength = replacementLength;
		this.cursorPosition = cursorPosition;
		this.image = image;
		this.displayString = displayString;
		this.contextInformation = contextInformation;
		this.additionalProposalInfo = additionalProposalInfo;
		this.rating = rating;
	}

	public String getDisplayString() {
		return displayString;
	}

	public ICompletionProposal getProposal() {
		return new CompletionProposal(replacementString, replacementOffset, replacementLength, cursorPosition, image, getDisplayString(), contextInformation, additionalProposalInfo);
	}

	@Override
	public int compareTo(WurstCompletion o) {
		if (o.rating == rating) {
//			 if rating is equal, sort alphabetically
			return displayString.compareTo(o.displayString);
		}
		return Double.compare(o.rating, rating) ;
	}
	
	@Override
	public String toString() {
		return displayString;
	}

	public double getRating() {
		return rating;
	}

}
