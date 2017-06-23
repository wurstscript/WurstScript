package de.peeeq.wurstio.languageserver;

/**
 * a position in a text document
 */
public class TextPos {
    private int line;
    private int column;

    public TextPos(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
