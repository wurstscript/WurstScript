import java.awt.*;
import javax.swing.*;

import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;

/**
 * A simple example showing how to use RSyntaxTextArea to add Java syntax
 * highlighting to a Swing application.<p>
 *
 * This example uses RSyntaxTextArea 1.4.<p>
 *
 * Project Home: http://fifesoft.com/rsyntaxtextarea<br>
 * Downloads:    https://sourceforge.net/projects/rsyntaxtextarea
 */
public class TextEditorDemo extends JFrame {

	private static final long serialVersionUID = 1L;


	public TextEditorDemo() throws Exception {

      JPanel cp = new JPanel(new BorderLayout());

      RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
      textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
      RTextScrollPane sp = new RTextScrollPane(textArea);
      cp.add(sp);
      textArea.setFont(new Font("Consolas", Font.PLAIN, 12));	
      textArea.setAntiAliasingEnabled(true);
      setContentPane(cp);
      setTitle("Text Editor Demo");
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      pack();
      setLocationRelativeTo(null);
      
   }

   public static void main(String[] args) {
      // Start all Swing applications on the EDT.
      SwingUtilities.invokeLater(new Runnable() {
    	  
         public void run() {
            try {
            	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				new TextEditorDemo().setVisible(true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
         }
      });
   }

}