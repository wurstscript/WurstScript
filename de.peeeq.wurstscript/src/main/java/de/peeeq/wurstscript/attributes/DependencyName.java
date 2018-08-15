package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Library;

/**
 *
 */
public class DependencyName {
    public static String getDependencyName(CompilationUnit cu) {
        Library lib = (Library) cu.getParent().getParent();
        return lib.getLibraryName();
    }
}
