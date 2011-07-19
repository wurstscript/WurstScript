package de.peeeq.pscript.ui.contentassist;

import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.xtext.ui.editor.autoedit.CommandInfo;
import org.eclipse.xtext.ui.editor.autoedit.MultiLineTerminalsEditStrategy;

public class PscriptMultiLineTerminalsEditStrategy extends MultiLineTerminalsEditStrategy {

	public PscriptMultiLineTerminalsEditStrategy(String leftTerminal, String indentationString, String rightTerminal) {
		super(leftTerminal, indentationString, rightTerminal);
		// 
	}

	@Override
	protected void internalCustomizeDocumentCommand(IDocument document, DocumentCommand command)
			throws BadLocationException {
		if (command.length != 0)
			return;
		String originalText = command.text;
		String[] lineDelimiters = document.getLegalLineDelimiters();
		int delimiterIndex = TextUtilities.startsWith(lineDelimiters, originalText);
		if (delimiterIndex != -1) { // new line entered
			IRegion startTerminal = findStartTerminal(document, command.offset);
			if (startTerminal == null)
				return;
			IRegion stopTerminal = findStopTerminal(document, command.offset);
			// check whether this is our stop terminal
			if (stopTerminal != null) {
				IRegion previousStart = startTerminal;
				IRegion previousStop = stopTerminal;
				while(stopTerminal != null && previousStart != null && previousStop != null) {
					previousStart = findStartTerminal(document, previousStart.getOffset() - 1);
					if (previousStart != null) {
						previousStop = findStopTerminal(document, previousStop.getOffset() + 1);
						if (previousStop == null) {
							stopTerminal = null;
						}
					}
				}
			}
			if (util.isSameLine(document, startTerminal.getOffset(), command.offset)) {
				if (stopTerminal != null && stopTerminal.getLength() < getRightTerminal().length())
					stopTerminal = null;
				CommandInfo newC = handleCursorInFirstLine(document, command, startTerminal, stopTerminal);
				if (newC != null)
					newC.modifyCommand(command);
				return;
			} else if (stopTerminal == null) {
				// TODO this seems to be defect
//				CommandInfo newC = handleNoStopTerminal(document, command, startTerminal, stopTerminal);
//				if (newC != null)
//					newC.modifyCommand(command);
//				return;
			} else if (!util.isSameLine(document, stopTerminal.getOffset(), command.offset)) {
				CommandInfo newC = handleCursorBetweenStartAndStopLine(document, command, startTerminal, stopTerminal);
				if (newC != null)
					newC.modifyCommand(command);
				return;
			} else {
				CommandInfo newC = handleCursorInStopLine(document, command, startTerminal, stopTerminal);
				if (newC != null)
					newC.modifyCommand(command);
				return;
			}
		}
	}
	
	
	protected IRegion findStartTerminal(IDocument document, int offset) throws BadLocationException {
		int stopOffset = offset;
		int startOffset = offset;
		while(true) {
			IRegion start = searchBackwardsInSamePartition(getLeftTerminal(), document, startOffset);
			if (start==null)
				return null;
			IRegion stop = searchBackwardsInSamePartition(getRightTerminal(), document, stopOffset);
			if (stop == null || stop.getOffset()<start.getOffset())
				return start;
			stopOffset = stop.getOffset();
			startOffset = start.getOffset();
		}
	}
	
	
	/**
	 * searches backwards for the given string within the same partition type
	 * @return the region of the match or <code>null</code> if no match were found
	 */
	public IRegion searchBackwardsInSamePartition(String toFind, IDocument document, int endOffset) throws BadLocationException {
		if (endOffset<0)
			return null;
		ITypedRegion partition = document.getPartition(endOffset);
		
		String text = document.get(0, endOffset);
		int indexOf = lastIndexOfWord(text, toFind);
		
		
		while (indexOf>=0) {
			ITypedRegion partition2 = document.getPartition(indexOf);
			if (partition2.getType().equals(partition.getType())) {
				return new Region(indexOf,toFind.length());
			} 
			text = document.get(0, partition2.getOffset());
			indexOf = lastIndexOfWord(text, toFind);
		}
		return null;
	}

	/**
	 * finds the last index of toFind in text but only searches real words
	 * for example "if" will not be found in "endif" but in " if"
	 * @param text
	 * @param toFind
	 * @return
	 */
	private int lastIndexOfWord(String text, String toFind) {
		int result = text.lastIndexOf(toFind);
		
		while (result >= 0) {
			if (result == 0) {
				return 0;
			}
			char charBeforeFind = text.charAt(result-1);
			if (!Character.isLetter(charBeforeFind)) {
				return result;
			}
			result = text.lastIndexOf(toFind, result-1);
		}
		return -1;
	}
	

}
