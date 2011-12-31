package wursteditor;

import java.awt.Font;
import java.io.File;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.FoldManager;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.RTextScrollPane;

import wursteditor.rsyntax.IndentationFoldParser;
import wursteditor.rsyntax.WurstCompletionProvider;
import wursteditor.rsyntax.WurstDocument;
import wursteditor.rsyntax.WurstTokenMaker;

public class WurstEditFileView extends RTextScrollPane {
	private static final long serialVersionUID = 6736983431743186359L;
	
	private RSyntaxTextArea syntaxCodeArea;

	private String fileName;


	public RSyntaxTextArea getSyntaxCodeArea() {
		return syntaxCodeArea;
	}

	public WurstEditFileView(String fileName) {
		this.fileName = fileName;
		syntaxCodeArea = new RSyntaxTextArea();
		syntaxCodeArea.setDocument(new WurstDocument("wurstscript"));
		
		this.setName("jScrollPane2"); // NOI18N

        syntaxCodeArea.setColumns(20);
        syntaxCodeArea.setRows(60);
        syntaxCodeArea.setName("syntaxCodeArea"); // NOI18N
        
        
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("wurstscript", WurstTokenMaker.class.getCanonicalName());
        TokenMakerFactory.setDefaultInstance(atmf);
        
        CompletionProvider wurstAutoCompletion = new WurstCompletionProvider();
		AutoCompletion ac = new AutoCompletion(wurstAutoCompletion);
		ac.install(syntaxCodeArea);
        
        FoldParserManager.get().addFoldParserMapping("wurstscript", new IndentationFoldParser());
        
        FoldManager foldManager = new FoldManager(syntaxCodeArea);
        foldManager.setCodeFoldingEnabled(true);
        
        
        
        syntaxCodeArea.setSyntaxEditingStyle("wurstscript");
        syntaxCodeArea.setFont(new Font("Consolas", Font.PLAIN, 14));	
        syntaxCodeArea.setAntiAliasingEnabled(true);
        syntaxCodeArea.setCodeFoldingEnabled(false); // this seems bugged
        syntaxCodeArea.setAutoIndentEnabled(true);
        syntaxCodeArea.setPaintTabLines(true);
        syntaxCodeArea.setClearWhitespaceLinesEnabled(false);
        
        this.setViewportView(syntaxCodeArea);

	}

	public String getFileName() {
		return fileName;
	}
}
