package wursteditor.rsyntax;

import java.util.ArrayList;
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

		for (int lineNr = 0; lineNr < lineCount; lineNr++) {
			try {
				String line = textArea.getText(textArea.getLineStartOffset(lineNr), textArea.getLineEndOffset(lineNr)
						- textArea.getLineStartOffset(lineNr));
				int indentations = countIndents(line);


				if (indentations > indentationLevels.peek()) {
					// start of new block:
					Fold fold = new Fold(FoldType.FOLD_TYPE_USER_DEFINED_MIN, textArea, textArea.getLineStartOffset(lineNr));
					foldStack.push(fold);
					indentationLevels.push(indentations);
				}

				while (indentations < indentationLevels.peek()) {
					indentationLevels.pop();
					Fold fold = foldStack.pop();
					fold.setEndOffset(textArea.getLineEndOffset(lineNr - 1));
					folds.add(fold);
				}

			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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
