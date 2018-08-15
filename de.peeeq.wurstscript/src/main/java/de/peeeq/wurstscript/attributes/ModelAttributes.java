package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.CompilationUnits;
import de.peeeq.wurstscript.ast.Library;
import de.peeeq.wurstscript.ast.WurstModel;

import java.util.AbstractList;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 */
public class ModelAttributes {
    public static List<CompilationUnit> getCompilationUnits(WurstModel wurstModel) {
        // return a mutable view on the model
        return new AbstractList<CompilationUnit>() {

            public CompilationUnit set(int index, CompilationUnit cu) {
                int i = index;
                for (Library lib : wurstModel.getLibraries()) {
                    if (i < 0) {
                        throw new IndexOutOfBoundsException("index out of bounds: " + index + " / " + size());
                    }
                    CompilationUnits cus = lib.getCompilationUnits();
                    int size = cus.size();
                    if (i < size) {
                        return cus.set(i, cu);
                    }
                    i -= size;
                }
                throw new IndexOutOfBoundsException("index out of bounds: " + index + " / " + size());
            }

            public void add(int index, CompilationUnit cu) {
                int i = index;
                for (Library lib : wurstModel.getLibraries()) {
                    if (i < 0) {
                        throw new IndexOutOfBoundsException("index out of bounds: " + index + " / " + size());
                    }
                    CompilationUnits cus = lib.getCompilationUnits();
                    int size = cus.size();
                    if (i < size) {
                        cus.add(i, cu);
                    }
                    i -= size;
                }
                throw new IndexOutOfBoundsException("index out of bounds: " + index + " / " + size());
            }

            public CompilationUnit remove(int index) {
                int i = index;
                for (Library lib : wurstModel.getLibraries()) {
                    if (i < 0) {
                        throw new IndexOutOfBoundsException("index out of bounds: " + index + " / " + size());
                    }
                    CompilationUnits cus = lib.getCompilationUnits();
                    int size = cus.size();
                    if (i < size) {
                        return cus.remove(i);
                    }
                    i -= size;
                }
                throw new IndexOutOfBoundsException("index out of bounds: " + index + " / " + size());
            }


            @Override
            public CompilationUnit get(int index) {
                int i = index;
                for (Library lib : wurstModel.getLibraries()) {
                    if (i < 0) {
                        throw new IndexOutOfBoundsException("index out of bounds: " + index + " / " + size());
                    }
                    CompilationUnits cus = lib.getCompilationUnits();
                    int size = cus.size();
                    if (i < size) {
                        return cus.get(i);
                    }
                    i -= size;
                }
                throw new IndexOutOfBoundsException("index out of bounds: " + index + " / " + size());
            }

            @Override
            public int size() {
                return wurstModel.getLibraries().stream()
                        .mapToInt(l -> l.getCompilationUnits().size())
                        .sum();
            }
        };
    }

    public static Stream<CompilationUnit> getCompilationUnitsStream(WurstModel wurstModel) {
        return wurstModel.attrCompilationUnits().stream();
    }

    public static boolean containsCompilationUnit(WurstModel wurstModel, CompilationUnit cu) {
        return wurstModel.attrCompilationUnits().contains(cu);
    }
}
