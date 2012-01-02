package wursteditor.rsyntax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import org.fife.ui.rsyntaxtextarea.folding.FoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rsyntaxtextarea.folding.FoldType;

import com.google.common.collect.Lists;

public class IndentationFoldParser implements FoldParser {

	@Override
	public List<Fold> getFolds(RSyntaxTextArea textArea) {
		List<Fold> folds = Lists.newArrayList();

		Stack<Integer> indentationLevels = new Stack<Integer>();
		indentationLevels.push(0);
		Stack<Fold> foldStack = new Stack<Fold>();

		int lineCount = textArea.getLineCount();
		int whiteSpaceLines = 0;
		
		for (int lineNr = 0; lineNr < lineCount; lineNr++) {
			try {
				String line = textArea.getText(textArea.getLineStartOffset(lineNr), textArea.getLineEndOffset(lineNr)
						- textArea.getLineStartOffset(lineNr));
				int indentations = countIndents(line);
				if (indentations == line.length()) {
					// line consists of white space only -> ignore line
					whiteSpaceLines++;
					continue;
				}

				if (indentations > indentationLevels.peek()) {
					// start of new block:
					Fold fold;
					fold = new Fold(FoldType.CODE, textArea, textArea.getLineStartOffset(lineNr-whiteSpaceLines)-1);
//					System.out.println("starting fold at line " + (lineNr - whiteSpaceLines) + ", indents = " + indentations);
					foldStack.push(fold);
					indentationLevels.push(indentations);
				}

				int correctionTerm = -2;
				while (indentations < indentationLevels.peek()) {
//					System.out.println(lineNr + ": "+ indentations + " < " + indentationLevels.peek());
					indentationLevels.pop();
					Fold fold = foldStack.pop();
					int endOffset = textArea.getLineEndOffset(lineNr + correctionTerm - whiteSpaceLines);
					fold.setEndOffset(endOffset);
					folds.add(fold);
//					System.out.println("ending fold at line " + (lineNr - 2 - whiteSpaceLines));
				}

			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			whiteSpaceLines = 0;
		}
		Collections.sort(folds);
		System.out.println("Folds = ");
		for (Fold f : folds) {
			System.out.println("	"  + f);
		}
		
		
		
		return folds;
	}

	private int countIndents(String line) {
		int p = 0;
		while (p < line.length() && Character.isWhitespace(line.charAt(p))) {
			p++;
		}
		return p;
	}

}
