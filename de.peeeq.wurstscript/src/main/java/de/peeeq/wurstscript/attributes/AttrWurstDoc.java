package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.utils.Utils;

public class AttrWurstDoc {

    public static String getComment(NameDef nameDef) {
        String triviaComment = getCommentFromTrivia(nameDef);
        if (!triviaComment.isEmpty()) {
            return triviaComment;
        }
        if (nameDef instanceof HasModifier) {
            HasModifier HasModifier = nameDef;
            return getCommmentHelper(HasModifier);
        }
        return "";
    }

    private static String getCommmentHelper(
            HasModifier HasModifier) {
        Modifiers modifiers = HasModifier.getModifiers();
        for (Modifier m : modifiers) {
            if (m instanceof WurstDoc) {
                WurstDoc wurstDoc = (WurstDoc) m;
                return comment(wurstDoc);
            }
        }
        return "";
    }

    public static String getComment(ConstructorDef constructorDef) {
        String triviaComment = getCommentFromTrivia(constructorDef);
        if (!triviaComment.isEmpty()) {
            return triviaComment;
        }
        return getCommmentHelper(constructorDef);
    }


    private static String getCommentFromTrivia(AstElementWithSource e) {
        CompilationUnit cu = e.attrCompilationUnit();
        if (cu == null || cu.getCuInfo() == null) {
            return "";
        }
        return cu.getCuInfo().getTriviaIndex().findLeadingHotdoc(e.getSource().getLeftPos());
    }

    private static String comment(WurstDoc wurstDoc) {
        return normalizeHotdocComment(wurstDoc.getRawComment());
    }

    public static String normalizeHotdocComment(String rawComment) {
        String result = rawComment;
        if (result.length() <= 5) {
            return "";
        }
        // strip of "/**" and */"
        result = result.replaceAll("^/\\*\\*+", "").replaceAll("\\*+/$", "");

        String[] lines = result.split("(\n|\r|\n\r|\r\n)[\t ]*(\\*)? ?");

        result = Utils.join(lines, "\n");

        return result;
    }


}
