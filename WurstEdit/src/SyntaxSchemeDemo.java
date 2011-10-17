import java.awt.*;
import javax.swing.*;

import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;

/**
 * A simple example showing how to modify the fonts and colors used in an
 * RSyntaxTextArea.<p>
 *
 * This example uses RSyntaxTextArea 1.4.<p>
 *
 * Project Home: http://fifesoft.com/rsyntaxtextarea<br>
 * Downloads:    https://sourceforge.net/projects/rsyntaxtextarea
 */
public class SyntaxSchemeDemo extends JFrame {

   private static final long serialVersionUID = 1L;

   private static final String text =
      "public class ExampleSource {\n\n" +
      "   // Check out the crazy modified styles!\n" +
      "   public static void main(String[] args) {\n" +
      "      System.out.println(\"Hello, world!\");\n" +
      "   }\n\n" +
      "}\n";

   public SyntaxSchemeDemo() {

      JPanel cp = new JPanel(new BorderLayout());

      RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
      textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
      RTextScrollPane sp = new RTextScrollPane(textArea);
      cp.add(sp);

      textArea.setText(text);

      // Set the font for all token types.
      setFont(textArea, new Font("Comic Sans MS", Font.PLAIN, 16));

      // Change a few things here and there.
      SyntaxScheme scheme = textArea.getSyntaxScheme();
      scheme.styles[Token.RESERVED_WORD].background = Color.pink;
      scheme.styles[Token.DATA_TYPE].foreground = Color.blue;
      scheme.styles[Token.LITERAL_STRING_DOUBLE_QUOTE].underline = true;
      scheme.styles[Token.COMMENT_EOL].font = new Font("Georgia", Font.ITALIC, 12);

      setContentPane(cp);
      setTitle("Syntax Scheme Demo");
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      pack();
      setLocationRelativeTo(null);

   }

   /**
    * Set the font for all token types.
    *
    * @param textArea The text area to modify.
    * @param font The font to use.
    */
   public static void setFont(RSyntaxTextArea textArea, Font font) {
      if (font!=null) {
         SyntaxScheme ss = textArea.getSyntaxScheme();
         ss = (SyntaxScheme)ss.clone();
         for (int i=0; i<ss.styles.length; i++) {
            if (ss.styles[i]!=null) {
               ss.styles[i].font = font;
		    }
		 }
		 textArea.setSyntaxScheme(ss);
		 textArea.setFont(font);
      }
   }

   public static void main(String[] args) {
      // Start all Swing applications on the EDT.
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            new SyntaxSchemeDemo().setVisible(true);
         }
      });
   }

}