package wursteditor;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.ByteArrayInputStream;
import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.JList;
import javax.swing.text.Document;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
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

	public WurstEditFileView(RSyntaxTextArea syntaxCodeArea, String fileName, JList errorList) throws IOException {
			super(syntaxCodeArea);
            this.fileName = fileName;
            this.syntaxCodeArea = syntaxCodeArea;
            Theme theme = Theme.load(new FileInputStream(new File("./lib/wurst.xml")));
            theme.apply(this.syntaxCodeArea);

            System.out.println("1");
            SyntaxCodeAreaController controller = new SyntaxCodeAreaController(syntaxCodeArea, errorList);
            System.out.println("2");
            syntaxCodeArea.setDocument(new WurstDocument("wurstscript"));
            System.out.println("3");
            this.setName("jScrollPane2"); // NO
            syntaxCodeArea.setColumns(20);
            System.out.println("4");
            syntaxCodeArea.setRows(60);
            syntaxCodeArea.setName("syntaxCodeArea"); // NOI18N


            System.out.println("5");
            syntaxCodeArea.setFont(new Font("Consolas", Font.PLAIN, 14));	
            syntaxCodeArea.setAntiAliasingEnabled(true);

            syntaxCodeArea.setAnimateBracketMatching(false);

            this.setViewportView(syntaxCodeArea);


            controller.init();
	}

	public String getFileName() {
		return fileName;
	}
}
