package tablelayout;

import javax.swing.*;
import java.awt.*;

public class Stack extends JPanel {
    public Stack() {
        super(new LayoutManager() {
            public void layoutContainer(Container parent) {
                int width = parent.getWidth();
                int height = parent.getHeight();
                for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
                    parent.getComponent(i).setLocation(0, 0);
                    parent.getComponent(i).setSize(width, height);
                }
            }

            public Dimension preferredLayoutSize(Container parent) {
                Dimension size = new Dimension();
                for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
                    Dimension pref = parent.getComponent(i).getPreferredSize();
                    size.width = Math.max(size.width, pref.width);
                    size.height = Math.max(size.height, pref.height);
                }
                return size;
            }

            public Dimension minimumLayoutSize(Container parent) {
                Dimension size = new Dimension();
                for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
                    Dimension min = parent.getComponent(i).getMinimumSize();
                    size.width = Math.max(size.width, min.width);
                    size.height = Math.max(size.height, min.height);
                }
                return size;
            }

            public void addLayoutComponent(String name, Component comp) {
            }

            public void removeLayoutComponent(Component comp) {
            }
        });
    }
}