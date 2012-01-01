/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wursteditor;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

/**
 *
 * @author Frotty
 */
class ErrorListCellRenderer extends DefaultListCellRenderer {
    /* This is the only method defined by ListCellRenderer.  We just
     * reconfigure the Jlabel each time we're called.
     */
    @Override
    public Component getListCellRendererComponent(
        JList list,
	Object value,   // value to display
	int index,      // cell index
	boolean iss,    // is the cell selected
	boolean chf)    // the list and the cell have the focus
    {
        /* The DefaultListCellRenderer class will take care of
         * the JLabels text property, it's foreground and background
         * colors, and so on.
         */
        super.getListCellRendererComponent(list, value, index, iss, chf);

        /* We additionally set the JLabels icon property here.
         */
        String s = value.toString();
        if ( index % 2 > 0 ) {
            setBackground(new Color(.15f,.15f,.15f, .12f));
            
        }
        if (iss) {
            System.out.println("selected");
            setBackground(new Color(0,0,255,100));
            this.setForeground(Color.black);
        }
	return this;
    }
}