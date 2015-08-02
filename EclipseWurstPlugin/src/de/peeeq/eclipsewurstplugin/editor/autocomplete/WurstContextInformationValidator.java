package de.peeeq.eclipsewurstplugin.editor.autocomplete;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

import com.google.common.base.Preconditions;

import de.peeeq.eclipsewurstplugin.editor.WurstEditor;
import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ExprEmpty;
import de.peeeq.wurstscript.utils.Utils;

public class WurstContextInformationValidator implements IContextInformationValidator, IContextInformationPresenter {

	private ITextViewer viewer;
	private IContextInformation info;
	private int installOffset;
	private int fCurrentParameter = -1;
	private WurstEditor editor;

	@SuppressWarnings("null") // not initialized by constructor, but by install method
	public WurstContextInformationValidator(WurstEditor editor) {
		this.editor = editor;
	}

	@Override
	public void install(@Nullable IContextInformation info, @Nullable ITextViewer viewer, int offset) {
		Preconditions.checkNotNull(info);
		Preconditions.checkNotNull(viewer);
		this.info = info;
		this.viewer = viewer;
		this.installOffset = offset;
		fCurrentParameter= -1;
		
		// adjust installOffset to the beginning of the argument list
		editor.doWithCompilationUnit(cu -> {
			
			AstElement elem = Utils.getAstElementAtPos(cu, offset, false);
			if (elem instanceof ExprEmpty) {
				if (elem.getParent() instanceof Arguments) {
					Arguments args = (Arguments) elem.getParent();
					if (!args.isEmpty()) {
						installOffset = args.get(0).getSource().getLeftPos();
					}
					
				}
			}
		});
	}

	@Override
	public boolean isContextInformationValid(int offset) {
		try {
			IDocument doc = viewer.getDocument();
			IRegion lineInfo = doc.getLineInformationOfOffset(installOffset);
			if (offset < installOffset) {
				// cursor left of the initial parenthesis
				return false;
			}
			if (offset > lineInfo.getOffset() + lineInfo.getLength()) {
				//cursor in next line
				return false;
			}
			String line = doc.get(lineInfo.getOffset(), lineInfo.getLength());
			
			// count parenthesis
			int parenCount = 0;
			int parenInit = 0;
			int parenNow = 0;
			for (int i=0; i <line.length(); i++) {
				int absoluteI = i + lineInfo.getOffset();
				char c = line.charAt(i);
				if (c == '(') {
					parenCount++;
				} else if (c == ')') {
					parenCount--;
				}
				if (absoluteI+1 == installOffset) {
					parenInit = parenCount;
				}
				if (absoluteI+1 == offset) {
					parenNow = parenCount;
				}
			}
			return parenNow >= parenInit;
//			if (parenNow < parenInit) {
//				return false;
//			} else {
//				return true;
//			}
		} catch (BadLocationException e) {
		}
		return false;
	}

	// everything below stolen and adapted from org.eclipse.jdt.internal.ui.text.java.JavaParameterListValidator
	
	@Override
	public boolean updatePresentation(int offset, @Nullable TextPresentation presentation) {
		Preconditions.checkNotNull(presentation);
		int currentParameter= -1;

		try {
			currentParameter= getCharCount(viewer.getDocument(), installOffset, offset, ",", "", true);  //$NON-NLS-1$//$NON-NLS-2$
		} catch (BadLocationException x) {
			return false;
		}

		if (fCurrentParameter != -1) {
			if (currentParameter == fCurrentParameter)
				return false;
		}

		presentation.clear();
		fCurrentParameter= currentParameter;

		String s= info.getInformationDisplayString();
		int[] commas= computeCommaPositions(s);

		if (commas.length - 2 < fCurrentParameter) {
			presentation.addStyleRange(new StyleRange(0, s.length(), null, null, SWT.NORMAL));
			return true;
		}

		int start= commas[fCurrentParameter] + 1;
		int end= commas[fCurrentParameter + 1];
		if (start > 0)
			presentation.addStyleRange(new StyleRange(0, start, null, null, SWT.NORMAL));

		if (end > start)
			presentation.addStyleRange(new StyleRange(start, end - start, null, null, SWT.BOLD));

		if (end < s.length())
			presentation.addStyleRange(new StyleRange(end, s.length() - end, null, null, SWT.NORMAL));

		return true;
	}
	
	private int[] computeCommaPositions(String code) {
		final int length= code.length();
	    int pos= 0;
	    int angleLevel= 0;
		List<Integer> positions= new ArrayList<Integer>();
		positions.add(new Integer(-1));
		while (pos < length && pos != -1) {
			char ch= code.charAt(pos);
			switch (ch) {
	            case ',':
	            	if (angleLevel == 0)
	            		positions.add(new Integer(pos));
		            break;
	            case '<':
	            	angleLevel++;
	            	break;
	            case '>':
	            	angleLevel--;
	            	break;
	            case '[':
	            	pos= code.indexOf(']', pos);
	            	break;
	            default:
	            	break;
            }
			if (pos != -1)
				pos++;
		}
		positions.add(new Integer(length));

		int[] fields= new int[positions.size()];
		for (int i= 0; i < fields.length; i++)
	        fields[i]= positions.get(i).intValue();
	    return fields;
    }
	private int getCharCount(IDocument document, final int start, final int end, String increments, String decrements, boolean considerNesting) throws BadLocationException {

		Assert.isTrue((increments.length() != 0 || decrements.length() != 0) && !increments.equals(decrements));

		final int NONE= 0;
		final int BRACKET= 1;
		final int BRACE= 2;
		final int PAREN= 3;
		final int ANGLE= 4;

		int nestingMode= NONE;
		int nestingLevel= 0;

		int charCount= 0;
		int offset= start;
		while (offset < end) {
			char curr= document.getChar(offset++);
			switch (curr) {
				case '/':
					if (offset < end) {
						char next= document.getChar(offset);
						if (next == '*') {
							// a comment starts, advance to the comment end
							offset= getCommentEnd(document, offset + 1, end);
						} else if (next == '/') {
							// '//'-comment: nothing to do anymore on this line
							int nextLine= document.getLineOfOffset(offset) + 1;
							if (nextLine == document.getNumberOfLines())
								offset= end;
							else
								offset= document.getLineOffset(nextLine);
						}
					}
					break;
				case '*':
					if (offset < end) {
						char next= document.getChar(offset);
						if (next == '/') {
							// we have been in a comment: forget what we read before
							charCount= 0;
							++ offset;
						}
					}
					break;
				case '"':
				case '\'':
					offset= getStringEnd(document, offset, end, curr);
					break;
				case '[':
					if (considerNesting) {
						if (nestingMode == BRACKET || nestingMode == NONE) {
							nestingMode= BRACKET;
							nestingLevel++;
						}
						break;
					}
					//$FALL-THROUGH$
				case ']':
					if (considerNesting) {
						if (nestingMode == BRACKET)
							if (--nestingLevel == 0)
								nestingMode= NONE;
						break;
					}
					//$FALL-THROUGH$
				case '(':
					if (considerNesting) {
						if (nestingMode == ANGLE) {
							// generics heuristic failed
							nestingMode=PAREN;
							nestingLevel= 1;
						}
						if (nestingMode == PAREN || nestingMode == NONE) {
							nestingMode= PAREN;
							nestingLevel++;
						}
						break;
					}
					//$FALL-THROUGH$
				case ')':
					if (considerNesting) {
						if (nestingMode == PAREN)
							if (--nestingLevel == 0)
								nestingMode= NONE;
						break;
					}
					//$FALL-THROUGH$
				case '{':
					if (considerNesting) {
						if (nestingMode == ANGLE) {
							// generics heuristic failed
							nestingMode=BRACE;
							nestingLevel= 1;
						}
						if (nestingMode == BRACE || nestingMode == NONE) {
							nestingMode= BRACE;
							nestingLevel++;
						}
						break;
					}
					//$FALL-THROUGH$
				case '}':
					if (considerNesting) {
						if (nestingMode == BRACE)
							if (--nestingLevel == 0)
								nestingMode= NONE;
						break;
					}
					//$FALL-THROUGH$
				case '<':
					if (considerNesting) {
						if (nestingMode == ANGLE || nestingMode == NONE && checkGenericsHeuristic(document, offset - 1, start - 1)) {
							nestingMode= ANGLE;
							nestingLevel++;
						}
						break;
					}
					//$FALL-THROUGH$
				case '>':
					if (considerNesting) {
						if (nestingMode == ANGLE)
							if (--nestingLevel == 0)
								nestingMode= NONE;
						break;
					}
					//$FALL-THROUGH$
				default:
					if (nestingLevel != 0)
						continue;

					if (increments.indexOf(curr) >= 0) {
						++ charCount;
					}

					if (decrements.indexOf(curr) >= 0) {
						-- charCount;
					}
			}
		}

		return charCount;
	}

	private boolean checkGenericsHeuristic(IDocument document, int i, int j) {
		return false;
	}
	
	private int getCommentEnd(IDocument d, int pos, int end) throws BadLocationException {
		while (pos < end) {
			char curr= d.getChar(pos);
			pos++;
			if (curr == '*') {
				if (pos < end && d.getChar(pos) == '/') {
					return pos + 1;
				}
			}
		}
		return end;
	}

	private int getStringEnd(IDocument d, int pos, int end, char ch) throws BadLocationException {
		while (pos < end) {
			char curr= d.getChar(pos);
			pos++;
			if (curr == '\\') {
				// ignore escaped characters
				pos++;
			} else if (curr == ch) {
				return pos;
			}
		}
		return end;
	}

}

