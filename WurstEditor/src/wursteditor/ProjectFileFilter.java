/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wursteditor;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author Frotty
 */
public class ProjectFileFilter extends javax.swing.filechooser.FileFilter {



    @Override
    public String getDescription() {
        return "Wurst Project Folder";
    }

    @Override
    public boolean accept(File f) {
        return true;
    }
}

