package de.peeeq.wurstscript.llvm.printer;


import de.peeeq.wurstscript.llvm.ast.Element;

public class SourceInfo {

    private String comment = null;
    private int minijavaLineNr = 0;

    public static SourceInfo get(Element e) {
        return new SourceInfo();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getMinijavaLineNr() {
        return minijavaLineNr;
    }

    public void setMinijavaLineNr(int minijavaLineNr) {
        this.minijavaLineNr = minijavaLineNr;
    }


}
