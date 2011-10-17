import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;

/**
 * A simple example showing how to do search and replace in a RSyntaxTextArea.
 * <p>
 * 
 * This example uses RSyntaxTextArea 1.4.
 * <p>
 * 
 * Project Home: http://fifesoft.com/rsyntaxtextarea<br>
 * Downloads: https://sourceforge.net/projects/rsyntaxtextarea
 */
public class FindAndReplaceDemo extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private RSyntaxTextArea textArea;
	private JTextField searchField;
	private JCheckBox regexCB;
	private JCheckBox matchCaseCB;

	public FindAndReplaceDemo() {

		JPanel cp = new JPanel(new BorderLayout());

		textArea = new RSyntaxTextArea(20, 60);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		RTextScrollPane sp = new RTextScrollPane(textArea);
		cp.add(sp);

		// Create a toolbar with searching options.
		JToolBar toolBar = new JToolBar();
		searchField = new JTextField(30);
		toolBar.add(searchField);
		JButton b = new JButton("Find Next");
		b.setActionCommand("FindNext");
		b.addActionListener(this);
		toolBar.add(b);
		b = new JButton("Find Previous");
		b.setActionCommand("FindPrev");
		b.addActionListener(this);
		toolBar.add(b);
		regexCB = new JCheckBox("Regex");
		toolBar.add(regexCB);
		matchCaseCB = new JCheckBox("Match Case");
		toolBar.add(matchCaseCB);
		cp.add(toolBar, BorderLayout.NORTH);

		setContentPane(cp);
		setTitle("Find and Replace Demo");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);

	}

	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if ("FindNext".equals(command)) {
			String text = searchField.getText();
			if (text.length() == 0) {
				return;
			}
			boolean forward = true;
			boolean matchCase = matchCaseCB.isSelected();
			boolean wholeWord = false;
			boolean regex = regexCB.isSelected();
			boolean found = SearchEngine.find(textArea, text, forward,
					matchCase, wholeWord, regex);
			if (!found) {
				JOptionPane.showMessageDialog(this, "Text not found");
			}
		}

		else if ("FindPrev".equals(command)) {
			String text = searchField.getText();
			if (text.length() == 0) {
				return;
			}
			boolean forward = false;
			boolean matchCase = matchCaseCB.isSelected();
			boolean wholeWord = false;
			boolean regex = regexCB.isSelected();
			boolean found = SearchEngine.find(textArea, text, forward,
					matchCase, wholeWord, regex);
			if (!found) {
				JOptionPane.showMessageDialog(this, "Text not found");
			}
		}

	}

	public static void main(String[] args) {
		// Start all Swing applications on the EDT.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					String laf = UIManager.getSystemLookAndFeelClassName();
					UIManager.setLookAndFeel(laf);
				} catch (Exception e) {}
				FindAndReplaceDemo demo = new FindAndReplaceDemo();
				demo.setVisible(true);
				demo.textArea.requestFocusInWindow();
			}
		});
	}

}