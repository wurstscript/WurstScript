package editor;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;

public class MainView extends JFrame {
	
	private JPanel 			MainPanel;
	private JSplitPane		textAndErrorPane;
	private JSplitPane		packageAndRightPane;
	private RSyntaxTextArea textArea;
	private RTextScrollPane textAreaScrollPane;
	private JScrollPane		packageScrollPane;
	private JTree			packageTree;
	private JScrollPane		errorScrollPane;
	private JTable			errorTable;
	private JToolBar		toolBar;
	private JMenuBar		menuBar;
	private JMenu			fileMenu;
	
	private WurstController controller = new WurstController(this);
	
	public MainView(String title) {
		// Main JPanel
		MainPanel = new JPanel(new BorderLayout());
		
		
		
		
		// RyntaxTextFrame Creation and Configuration
		createTextAreaPanel();
	    
	    // TextScrollPane to add the SyntaxFrame
		textAreaScrollPane = new RTextScrollPane(textArea);
		
		// Menubar
		createMenu();
		
		// Toolbar
		createToolbar();
		
		// PackagePanel Creation
		createPackagePanel();
		
		// ErrorPanel Creation
		createErrorPanel();
		
		// Text And Error SplitPane
		createTextAndErrorSplitPane();
		
		createPackageAndRightSplitPane();
	    
	    // Add the ScrollPane with the SyntaxFrame to the MainPanel
	    MainPanel.add(packageAndRightPane);
	    
	    setContentPane(MainPanel);
	    
	    // Title
	    setTitle(title);
	    
	    // CloseOperation
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    
	    pack();
	    
	    setLocationRelativeTo(null);
	}
	
	private void createMenu() {
		setJMenuBar(new WurstMenuBar(controller));
	}

	private void createToolbar() {
		toolBar = new JToolBar();
		JButton jb = new JButton("Test", null);
		toolBar.add(jb);
		MainPanel.add(toolBar, BorderLayout.PAGE_START);
	}

	private void createTextAndErrorSplitPane() {
		textAndErrorPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textAreaScrollPane, errorScrollPane);
		textAndErrorPane.setDividerLocation(.2);
		textAndErrorPane.setDividerSize(3);
	}
	
	private void createPackageAndRightSplitPane() {
		packageAndRightPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, packageScrollPane, textAndErrorPane );
		packageAndRightPane.setDividerLocation(200);
		packageAndRightPane.setDividerSize(3);
	}

	private void createPackagePanel() {
		packageTree = new PackageTree();
		packageScrollPane = new JScrollPane(packageTree);
		packageScrollPane.setSize(150, 600);
	}
	
	private void createErrorPanel() {
		String[] columnNames = {"Description",
                "Resource",
                "Path",
                "Location",
                "Type"};
		Object[][] data = {
			    {"", "",
			     "", "", ""}};
		errorTable = new JTable(data, columnNames);
		errorScrollPane = new JScrollPane(errorTable);
		errorScrollPane.setSize(650, 150);
	}
	
	private void createTextAreaPanel() {
		textArea = new RSyntaxTextArea(20, 60);
		addWurstSyntax();
		textArea.setSyntaxEditingStyle("wurstscript");
	    textArea.setFont(new Font("Consolas", Font.PLAIN, 12));	
	    textArea.setAntiAliasingEnabled(true);
	}
	
	private void addWurstSyntax() { 		
		//Defines the codehighlighting for RsyntaxPane
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
		atmf.putMapping("wurstscript", "editor.WurstTokenMaker");
		TokenMakerFactory.setDefaultInstance(atmf);
	}
	
	
	public static void main(String[] args) {
	      // Start all Swing applications on the EDT.
	      SwingUtilities.invokeLater(new Runnable() {
	    	  
	         public void run() {
				try {
					
					// Look & Feel
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					
					new MainView("Wurst Editor").setVisible(true);					
				} catch ( Exception e) {}
	         }
	      });
	   }
	
}
