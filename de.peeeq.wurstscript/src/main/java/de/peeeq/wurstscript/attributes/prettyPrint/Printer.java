package de.peeeq.wurstscript.attributes.prettyPrint;

import de.peeeq.wurstscript.ast.AstElementWithSource;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.parser.antlr.CommentToken;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 *
 */
public class Printer {
    protected StringBuilder sb = new StringBuilder();
    protected int indent = 0;
    protected Deque<CommentToken> commentTokens = new ArrayDeque<>();



    public void addComments(WPos before) {
        CommentToken last = null;
        while (!commentTokens.isEmpty()) {
            CommentToken t = commentTokens.peekFirst();
            if (t.getPos().getRightPos() <= before.getLeftPos()) {
                last = commentTokens.removeFirst();
                sb.append(t.getText());
            } else {
                break;
            }
        }
        if (last != null) {
            if (last.getPos().getEndLine() < before.getLine()) {
                newline();
            }
        }
    }

    public void printRemainingComments() {
        while (!commentTokens.isEmpty()) {
            CommentToken t = commentTokens.removeFirst();
            sb.append(t.getText());
            newline();
        }
    }


    void newline() {
        appendStr("\n");
    }

    public void setCommentTokens(List<CommentToken> commentTokens) {
        this.commentTokens = new ArrayDeque<>(commentTokens);
    }


    void appendStr(String s) {
        sb.append(s);
    }

    void appendWithPos(String s, WPos position) {
        addComments(position);
        appendStr(s);
    }

    void append(String s, Element elem) {
        if (elem instanceof AstElementWithSource) {
            addComments(((AstElementWithSource) elem).getSource());
        }
        appendStr(s);
    }

    void addSpace() {
        sb.append(" ");
    }


    public int length() {
        return sb.length();
    }

    public char charAt(int i) {
        return sb.charAt(i);
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    public void indent() {
        if (sb.toString().substring(sb.length() - 1).equals("\n")) {
            for (int i = 0; i < indent; i++) {
                sb.append("\t");
            }
        }
    }

    public void startIndent() {
        indent++;
    }

    public void endIndent() {
        indent--;
    }



}
