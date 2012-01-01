package wursteditor.controller;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Map;

import javax.swing.JList;
import javax.swing.ToolTipManager;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.FoldManager;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.ToolTipSupplier;

import com.google.common.collect.Maps;

import wursteditor.rsyntax.AstHelper;
import wursteditor.rsyntax.IndentationFoldParser;
import wursteditor.rsyntax.WurstCompletionProvider;
import wursteditor.rsyntax.WurstParser;
import wursteditor.rsyntax.WurstTokenMaker;
import wursteditor.rsyntax.WurstToolTipSupplier;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.FuncRef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.attributes.CompileError;

public class SyntaxCodeAreaController {

	private static Map<RSyntaxTextArea, SyntaxCodeAreaController> areaToController = Maps.newHashMap();
	private JList errorList;
	private CompilationUnit ast;
	private RSyntaxTextArea syntaxCodeArea;
	private WurstParser parser;

	public SyntaxCodeAreaController(final RSyntaxTextArea syntaxCodeArea, JList errorList) {
		this.syntaxCodeArea = syntaxCodeArea;
		areaToController.put(syntaxCodeArea, this);
		this.errorList = errorList;
		initWurst();

		syntaxCodeArea.setSyntaxEditingStyle("wurstscript");
		
		// install auto completion
		CompletionProvider wurstAutoCompletion = new WurstCompletionProvider();
		AutoCompletion ac = new AutoCompletion(wurstAutoCompletion);
		ac.install(syntaxCodeArea);

		// code folding
		FoldParserManager.get().addFoldParserMapping("wurstscript", new IndentationFoldParser());
		FoldManager foldManager = new FoldManager(syntaxCodeArea);
		foldManager.setCodeFoldingEnabled(true);
		syntaxCodeArea.setCodeFoldingEnabled(true); // this seems bugged
		
		syntaxCodeArea.setAutoIndentEnabled(true);
		this.parser = new WurstParser(this);
	    syntaxCodeArea.addParser(parser);
	    
	    
	    
		// tooltips
	    ToolTipSupplier tooltipSupplier = new WurstToolTipSupplier();
	    syntaxCodeArea.setToolTipSupplier(tooltipSupplier);
	    ToolTipManager.sharedInstance().registerComponent(syntaxCodeArea);
	    
	    
	    // strg+click
	    addJumpToDeclHandler(syntaxCodeArea);

	}

	private void addJumpToDeclHandler(final RSyntaxTextArea syntaxCodeArea) {
		syntaxCodeArea.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if ((e.getModifiers() & InputEvent.CTRL_MASK)== InputEvent.CTRL_MASK) {
					// ctrl was pressed
					int pos = syntaxCodeArea.getCaretPosition();
					jumpToDeclOfElementAtPos(syntaxCodeArea, pos);
	    		}
			}
			
		});
		syntaxCodeArea.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent keyevent) {
			}
			
			@Override
			public void keyReleased(KeyEvent keyevent) {
			}
			
			@Override
			public void keyPressed(KeyEvent keyevent) {
				if (KeyEvent.getKeyText(keyevent.getKeyCode()).equals("F3")) {
					int pos = syntaxCodeArea.getCaretPosition();
					jumpToDeclOfElementAtPos(syntaxCodeArea, pos);
				}
			}
		});
	}
	
	private void jumpToDeclOfElementAtPos(final RSyntaxTextArea syntaxCodeArea, int pos) {
		ast = getNewAst();
		AstElement element = AstHelper.getAstElementAtPos(ast, pos);
		if (element instanceof NameRef) {
			NameRef nameRef = (NameRef) element;
			NameDef def = nameRef.attrNameDef();
			if (def != null) {
				jumpToPos(syntaxCodeArea, def.getSource());
			}
		} else if (element instanceof FuncRef) {
			FuncRef fr = (FuncRef) element;
			FunctionDefinition def = fr.attrFuncDef();
			if (def != null) {
				syntaxCodeArea.setCaretPosition(def.getSource().getLeftPos());
			}
		}
	}

	private void jumpToPos(final RSyntaxTextArea syntaxCodeArea, WPos wPos) {
		// TODO check if we are in the correct file and switch file if necessary
		syntaxCodeArea.setCaretPosition(wPos.getLeftPos());
	}
	
	private void initWurst() {
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
		atmf.putMapping("wurstscript", WurstTokenMaker.class.getCanonicalName());
		TokenMakerFactory.setDefaultInstance(atmf);
	}

	public void setErrors(List<CompileError> errorList) {
		this.errorList.setListData(errorList.toArray());	
	}

	public void setAst(CompilationUnit cu) {
		if (cu != null) {
			if (ast == null || cu.size() > 0) {
				this.ast = cu;
			}
		}
	}
	
	public CompilationUnit getAst() {
		return ast;
	}

	public static SyntaxCodeAreaController getFor(RSyntaxTextArea comp) {
		return areaToController.get(comp);
	}

	public void reparseDocument() {
		syntaxCodeArea.addNotify();		
	}

	/**
	 * tries to get a new AST, before returning it
	 */
	public CompilationUnit getNewAst() {
		parser.parse((RSyntaxDocument) syntaxCodeArea.getDocument(), false);
		return ast;
	}

}
