package de.peeeq.wurstscript.parser.antlr;

import de.peeeq.wurstscript.parser.WPos;

/**
 *
 */
public class CommentToken {
    private final WPos pos;
    private final String text;

    public CommentToken(WPos wPos, String text) {

        this.pos = wPos;
        this.text = text;
    }

    public WPos getPos() {
        return pos;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "line " + pos.getLine() + ": " + text;
    }
}
