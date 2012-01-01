package wursteditor;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;

import javax.swing.JList;
import javax.swing.text.Document;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.FoldManager;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.RTextScrollPane;

import wursteditor.controller.SyntaxCodeAreaController;
import wursteditor.rsyntax.IndentationFoldParser;
import wursteditor.rsyntax.WurstCompletionProvider;
import wursteditor.rsyntax.WurstDocument;
import wursteditor.rsyntax.WurstParser;
import wursteditor.rsyntax.WurstTokenMaker;

public class WurstEditFileView extends RTextScrollPane {
	private static final long serialVersionUID = 6736983431743186359L;
	
	private RSyntaxTextArea syntaxCodeArea;

	private String fileName;


	public RSyntaxTextArea getSyntaxCodeArea() {
		return syntaxCodeArea;
	}

	public WurstEditFileView(String fileName, JList errorList) {
		this.fileName = fileName;
		syntaxCodeArea = new RSyntaxTextArea();
		
		new SyntaxCodeAreaController(syntaxCodeArea, errorList);
		
		syntaxCodeArea.setDocument(new WurstDocument("wurstscript"));
		
		this.setName("jScrollPane2"); // NOI18N

        syntaxCodeArea.setColumns(20);
        syntaxCodeArea.setRows(60);
        syntaxCodeArea.setName("syntaxCodeArea"); // NOI18N
        
        
        
        syntaxCodeArea.setFont(new Font("Consolas", Font.PLAIN, 14));	
        syntaxCodeArea.setAntiAliasingEnabled(true);
        
        syntaxCodeArea.setAnimateBracketMatching(false);
        
        this.setViewportView(syntaxCodeArea);

	}

	public String getFileName() {
		return fileName;
	}
}
