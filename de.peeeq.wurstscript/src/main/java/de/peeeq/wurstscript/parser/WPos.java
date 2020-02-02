package de.peeeq.wurstscript.parser;

import de.peeeq.wurstscript.utils.LineOffsets;
import org.eclipse.jdt.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WPos {

    private FileInfo fileInfo;
    private final int leftPos;
    private final int rightPos;

    public static WPos noSource() {
        return new WPos(new FileInfo("", null), 0, 0);
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public static class FileInfo {
        private final String file;
        private final @Nullable LineOffsets lineOffsets;

        public FileInfo(String file, @Nullable LineOffsets lineOffsets) {
            this.file = file;
            this.lineOffsets = lineOffsets;
        }
    }

    public WPos(String file, LineOffsets lo, int leftPos, int rightPos) {
        this(new FileInfo(file, lo), leftPos, rightPos);
    }

    public WPos(FileInfo fileInfo, int leftPos, int rightPos) {
        this.fileInfo = fileInfo;
        this.leftPos = leftPos;
        this.rightPos = rightPos;
    }

    public String getFile() {
        return fileInfo.file;
    }

    public @Nullable LineOffsets getLineOffsets() {
        return fileInfo.lineOffsets;
    }

    public int getLeftPos() {
        return leftPos;
    }

    public int getRightPos() {
        return rightPos;
    }


    public int getLine() {
        LineOffsets lo = getLineOffsets();
        if (lo == null)
            return 0;
        return lo.getLine(leftPos);
    }

    public int getEndLine() {
        LineOffsets lo = getLineOffsets();
        if (lo == null)
            return 0;
        return lo.getLine(rightPos);
    }

    public int getStartColumn() {
        LineOffsets lo = getLineOffsets();
        if (lo == null)
            return 0;
        return lo.getColumn(leftPos);
    }

    public int getEndColumn() {
        LineOffsets lo = getLineOffsets();
        if (lo == null)
            return 0;
        return lo.getColumn(rightPos);
    }

    public WPos withRightPos(int rightPos) {
        return new WPos(fileInfo, leftPos, rightPos);
    }

    @Override
    public String toString() {
        // returning empty string to make AST.toString more readable
        return "";
    }

    public String print() {
        return "[" + getFile() + " line " + getLine() + "]";
    }

    public String printShort() {
        Pattern p = Pattern.compile("^.*[/\\\\]([^/\\\\]+)\\.[^.]*$");
        String shortFile = getFile();
        Matcher m = p.matcher(getFile());
        if (m.find()) {
            shortFile = m.group(1);
        }
        return shortFile + ", line " + getLine();
    }

    public WPos withLeftPos(int leftPos) {
        return new WPos(fileInfo, leftPos, rightPos);
    }

    public WPos withFile(String file) {
        return new WPos(new FileInfo(file, fileInfo.lineOffsets), leftPos, rightPos);
    }

    public String shortFile() {
        String s = getFile();
        s = s.substring(s.lastIndexOf("lib/") + 4);
        s = s.replace(".wurst", "");
        return s;
    }

    /**
     * makes this position artificial by setting the rightPost = leftPos-1
     */
    public WPos artificial() {
        return new WPos(fileInfo, leftPos, leftPos - 1);
    }

    /**
     * an artificial position is one created by the compiler (the element is not directly part of the input file)
     */
    public boolean isArtificial() {
        return getRightPos() < getLeftPos();
    }

}
