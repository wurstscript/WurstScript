package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;
import org.eclipse.jdt.annotation.Nullable;

public class AttrPos {

    /**
     * makes a best effort to get a precise position for this element
     *
     * @param e
     * @return
     */
    public static WPos getPos(Element e) {
        if (e instanceof AstElementWithSource) {
            AstElementWithSource ws = (AstElementWithSource) e;
            return ws.getSource();
        }
        if (e.size() > 0) { // try to find the position by examining the childs
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (int i = 0; i < e.size(); i++) {
                Element child = e.get(i);
                WPos childSource = child.attrSource();
                if (childSource.getRightPos() < childSource.getLeftPos()) {
                    // artificial position, ignore
                    continue;
                }
                min = Math.min(min, childSource.getLeftPos());
                max = Math.max(max, childSource.getRightPos());
            }
            return new WPos(e.get(0).attrSource().getFile(), e.get(0).attrSource().getLineOffsets(), min, max);
        }
        // if no childs exist, search a parent element with a explicit position
        return getParentSource(e);
    }

    public static WPos getPos(WImports e) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (WImport i : e) {
            if (i.getPackagename().equals("Wurst")) {
                continue;
            }
            WPos childSource = i.getSource();
            min = Math.min(min, childSource.getLeftPos());
            max = Math.max(max, childSource.getRightPos());
        }
        if (min != Integer.MAX_VALUE) {
            return new WPos(e.get(0).attrSource().getFile(), e.get(0).attrSource().getLineOffsets(), min, max);
        } else {
            return getParentSource(e);
        }
    }


    private static WPos getParentSource(Element e) {
        Element parent = e.getParent();
        while (parent != null) {
            if (parent instanceof AstElementWithSource) {
                WPos parentSource = ((AstElementWithSource) parent).getSource();
                // use parent position but with size -1, so we do not go into this
                return new WPos(parentSource.getFile(), parentSource.getLineOffsets(), parentSource.getLeftPos(), parentSource.getLeftPos() - 1);
            }
            parent = parent.getParent();
        }
        return new WPos("<source of " + e + " not found>", new LineOffsets(), 0, -1);
    }


    private static @Nullable LineOffsets getLineOffsets(WPos p) {
        LineOffsets lineOffsets;
        if (p.getLineOffsets() instanceof LineOffsets) {
            lineOffsets = p.getLineOffsets();
        } else {
            lineOffsets = new LineOffsets();
        }
        return lineOffsets;
    }


    public static WPos getErrorPos(Element e) {
        return e.attrSource();
    }

    public static WPos getErrorPos(SomeSuperConstructorCall e) {
        return e.getKeywordSource();
    }

    public static WPos getErrorPos(WPackage e) {
        return identifierPos(e, e.getNameId());
    }

    public static WPos getErrorPos(NameDef e) {
        return identifierPos(e, e.getNameId());
    }

    public static WPos getErrorPos(FuncDef e) {
        return identifierPos(e, e.getNameId());
    }

    public static WPos getErrorPos(ExtensionFuncDef e) {
        return identifierPos(e, e.getNameId());
    }

    public static WPos getErrorPos(ClassDef e) {
        return identifierPos(e, e.getNameId());
    }

    public static WPos getErrorPos(ConstructorDef e) {
        WPos pos = e.getSource();
        return updateRight(pos, pos.getLeftPos() + ("construct").length());
    }

    public static WPos getErrorPos(InitBlock e) {
        WPos pos = e.getSource();
        return updateRight(pos, pos.getLeftPos() + ("init").length());
    }

    public static WPos getErrorPos(OnDestroyDef e) {
        WPos pos = e.getSource();
        return updateRight(pos, pos.getLeftPos() + ("ondestroy").length());
    }

    public static WPos getErrorPos(StructureDef e) {
        return identifierPos(e, e.getNameId());
    }


    public static WPos getErrorPos(LoopStatement e) {
        WPos pos = e.getSource();
        return updateRight(pos, pos.getLeftPos() + 3);
    }

    public static WPos getErrorPos(StmtWhile e) {
        WPos pos = e.getSource();
        return updateRight(pos, pos.getLeftPos() + 5);
    }

    public static WPos getErrorPos(StmtLoop e) {
        WPos pos = e.getSource();
        return updateRight(pos, pos.getLeftPos() + 5);
    }

    public static WPos getErrorPos(StmtIf e) {
        WPos pos = e.getSource();
        return updateRight(pos, pos.getLeftPos() + 2);
    }

    public static WPos getErrorPos(SwitchStmt e) {
        WPos pos = e.getSource();
        return updateRight(pos, pos.getLeftPos() + 6);
    }

    public static WPos getErrorPos(FuncRef e) {
        return identifierPos(e, e.getFuncNameId());
    }

    /**
     * if an identifer only has an artificial position, try to
     * find a better position from the parent
     */
    private static WPos identifierPos(Element e, Identifier ident) {
        WPos source = ident.getSource();
        if (!source.isArtificial()) {
            return source;
        }

        WPos eSource = e.attrSource();
        int left = eSource.getLeftPos();
        int right = eSource.getRightPos();


        for (int i = 0; i < e.size(); i++) {
            Element child = e.get(i);
            if (child == ident) {
                break;
            }
            WPos childSource = child.attrSource();
            if (childSource.getRightPos() < right) {
                left = Math.max(left, childSource.getRightPos());
            }
        }

        for (int i = e.size() - 1; i >= 0; i--) {
            Element child = e.get(i);
            if (child == ident) {
                break;
            }
            WPos childSource = child.attrSource();
            if (childSource.getLeftPos() > left) {
                right = Math.min(right, childSource.getLeftPos());
            }
        }

        return source.withLeftPos(left).withRightPos(right);
    }

    public static WPos getErrorPos(FunctionCall e) {
        return identifierPos(e, e.getFuncNameId());
    }

    public static WPos getErrorPos(ExprMemberVar e) {
        return identifierPos(e, e.getVarNameId());
    }

    public static WPos getErrorPos(ExprMemberMethod e) {
        return identifierPos(e, e.getFuncNameId());
    }

    public static WPos getErrorPos(ExprFunctionCall e) {
        return identifierPos(e, e.getFuncNameId());
    }

    public static WPos getErrorPos(ExprClosure e) {
        return e.getArrowSource();
    }


    public static WPos getErrorPos(ExprNewObject e) {
        return identifierPos(e, e.getTypeNameId());
    }

    public static WPos getErrorPos(VarDef e) {
        return identifierPos(e, e.getNameId());
    }

    private static WPos updateRight(WPos pos, int newRight) {
        if (newRight > pos.getLeftPos()) {
            return pos.withRightPos(newRight);
        }
        return pos;
    }


}
