package de.peeeq.wurstscript.parser;

import com.google.common.collect.ImmutableList;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.utils.LineOffsets;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WPosWithComments extends WPos {


    private List<Comment> commentsBefore = Collections.emptyList();
    private List<Comment> commentsAfter = Collections.emptyList();


    public WPosWithComments(String file, @Nullable LineOffsets lineOffsets, int leftPos, int rightPos) {
        super(file, lineOffsets, leftPos, rightPos);
    }

    public static List<Comment> createList(CompilationUnit cu) {
        return new ArrayList<>();
    }

    public void addCommentBefore(Comment comment) {
        commentsBefore = ImmutableList.<Comment>builder()
                .addAll(commentsBefore)
                .add(comment)
                .build();
    }

    public void addCommentAfter(Comment comment) {
        commentsAfter = ImmutableList.<Comment>builder()
                .addAll(commentsAfter)
                .add(comment)
                .build();
    }

    public List<Comment> getCommentsBefore() {
        return commentsBefore;
    }

    public List<Comment> getCommentsAfter() {
        return commentsAfter;
    }

    public static class Comment {
        private final WPos pos;
        private final String content;
        private final boolean singleLine;

        public Comment(WPos pos, String content, boolean singleLine) {
            this.pos = pos;
            this.content = content;
            this.singleLine = singleLine;
        }

        public WPos getPos() {
            return pos;
        }

        public String getContent() {
            return content;
        }

        public boolean isSingleLine() {
            return singleLine;
        }

    }
}
