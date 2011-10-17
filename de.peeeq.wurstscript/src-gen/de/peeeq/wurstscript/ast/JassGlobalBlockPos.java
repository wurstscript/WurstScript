package de.peeeq.wurstscript.ast;

import java.io.IOException;
import java.util.List;

import katja.common.KatjaList;
import katja.common.KatjaListImpl;
import katja.common.KatjaListPosImpl;
import katja.common.KatjaNodePos;
import katja.common.KatjaSort;

public interface JassGlobalBlockPos extends de.peeeq.wurstscript.ast.JassToplevelDeclarationPos, de.peeeq.wurstscript.ast.AST.ListPos<de.peeeq.wurstscript.ast.JassGlobalBlock, de.peeeq.wurstscript.ast.GlobalVarDefPos, de.peeeq.wurstscript.ast.GlobalVarDef> {

    //----- methods of JassGlobalBlockPos -----

    public de.peeeq.wurstscript.ast.JassGlobalBlock termJassToplevelDeclaration();
    public de.peeeq.wurstscript.ast.JassGlobalBlock termTopLevelDeclaration();
    public de.peeeq.wurstscript.ast.JassGlobalBlock term();
    public de.peeeq.wurstscript.ast.JassGlobalBlockPos add(de.peeeq.wurstscript.ast.GlobalVarDef element);
    public de.peeeq.wurstscript.ast.JassGlobalBlockPos addAll(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list);
    public de.peeeq.wurstscript.ast.JassGlobalBlockPos remove(de.peeeq.wurstscript.ast.GlobalVarDef element);
    public de.peeeq.wurstscript.ast.JassGlobalBlockPos removeAll(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list);
    public de.peeeq.wurstscript.ast.JassGlobalBlockPos appBack(de.peeeq.wurstscript.ast.GlobalVarDef element);
    public de.peeeq.wurstscript.ast.JassGlobalBlockPos appFront(de.peeeq.wurstscript.ast.GlobalVarDef element);
    public de.peeeq.wurstscript.ast.JassGlobalBlockPos conc(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list);
    public de.peeeq.wurstscript.ast.JassGlobalBlockPos reverse();
    public de.peeeq.wurstscript.ast.JassGlobalBlockPos toSet();
    public de.peeeq.wurstscript.ast.JassGlobalBlockPos setAdd(de.peeeq.wurstscript.ast.GlobalVarDef element);
    public de.peeeq.wurstscript.ast.JassGlobalBlockPos setRemove(de.peeeq.wurstscript.ast.GlobalVarDef element);
    public de.peeeq.wurstscript.ast.JassGlobalBlockPos setUnion(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list);
    public de.peeeq.wurstscript.ast.JassGlobalBlockPos setWithout(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list);
    public de.peeeq.wurstscript.ast.JassGlobalBlockPos setIntersection(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list);
    public KatjaList<de.peeeq.wurstscript.ast.GlobalVarDefPos> toList();
    public de.peeeq.wurstscript.ast.JassGlobalBlockPos replace(de.peeeq.wurstscript.ast.JassGlobalBlock term);
    public de.peeeq.wurstscript.ast.CompilationUnitPos parent();
    public de.peeeq.wurstscript.ast.TopLevelDeclarationPos lsib();
    public de.peeeq.wurstscript.ast.TopLevelDeclarationPos rsib();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrder();
    public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart();
    public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path);
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.JassToplevelDeclarationPos.Switch<CT, E> switchClass) throws E;
    public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.TopLevelDeclarationPos.Switch<CT, E> switchClass) throws E;

    //----- nested classes of JassGlobalBlockPos -----

    static class ListImpl extends KatjaListImpl<de.peeeq.wurstscript.ast.GlobalVarDefPos> implements KatjaList<de.peeeq.wurstscript.ast.GlobalVarDefPos> {

        //----- methods of ListImpl -----

        ListImpl(de.peeeq.wurstscript.ast.GlobalVarDefPos... elements) {
            super(elements);

            for(de.peeeq.wurstscript.ast.GlobalVarDefPos element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of katja list for position sort GlobalVarDefPos invoked with null element");
        }

        private ListImpl() {
        }

        protected KatjaList<de.peeeq.wurstscript.ast.GlobalVarDefPos> createInstance(de.peeeq.wurstscript.ast.GlobalVarDefPos[] elements, boolean isSet) {
            for(de.peeeq.wurstscript.ast.GlobalVarDefPos element : elements)
                if(element == null)
                    throw new IllegalArgumentException("constructor of katja list for position sort GlobalVarDefPos invoked with null element");

            ListImpl temp = new ListImpl();
            temp.values = elements;
            temp = (ListImpl) AST.unique(temp);
            if(isSet) temp.set = temp;

            return temp;
        }

        protected de.peeeq.wurstscript.ast.GlobalVarDefPos[] createArray(int size) {
            return new de.peeeq.wurstscript.ast.GlobalVarDefPos[size];
        }

        public Appendable toString(Appendable builder) throws IOException {
            boolean first = true;

            builder.append("KatjaList( ");
            for(de.peeeq.wurstscript.ast.GlobalVarDefPos element : values) {
                if(first) first = false;
                else builder.append(", ");
                element.toString(builder);
            }
            builder.append(" )");

            return builder;
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            throw new UnsupportedOperationException("toJavaCode invoked on an anonymous list of positions, you do not want to do that");
        }

        public final String sortName() {
            return "<anonymous>";
        }
    }

    static interface VisitorType<E extends Throwable> {

        //----- methods of VisitorType<E extends Throwable> -----

        public void visit(de.peeeq.wurstscript.ast.GlobalVarDefPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.WPosPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityModifierPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.BooleanPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OptTypeExprPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.StringPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OptExprPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityPublicPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityPrivatePos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityPublicreadPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityProtectedPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.VisibilityDefaultPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.NoTypeExprPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.TypeExprPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.NoExprPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprAssignablePos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprBinaryPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprUnaryPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberVarPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberArrayVarPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprMemberMethodPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprFunctionCallPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprNewObjectPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprCastPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprAtomicPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprVarAccessPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprVarArrayAccessPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprIntValPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprRealValPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprStringValPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprBoolValPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprFuncRefPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprThisPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ExprNullPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.IntegerPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ArraySizesPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpBinaryPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpUnaryPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.IndexesPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.ArgumentsPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.DoublePos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpOrPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpAndPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpEqualsPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpUnequalsPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpLessEqPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpLessPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpGreaterEqPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpGreaterPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpPlusPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpMinusPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpMultPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpDivRealPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpModRealPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpModIntPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpDivIntPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.OpNotPos term) throws E;
        public void visit(de.peeeq.wurstscript.ast.JassGlobalBlockPos term) throws E;
    }

    public static abstract class Visitor<E extends Throwable> implements de.peeeq.wurstscript.ast.JassGlobalBlockPos.VisitorType<E> {

        //----- attributes of Visitor<E extends Throwable> -----

        private final de.peeeq.wurstscript.ast.ExprAssignablePos.Switch<Object, E> variantVisit$ExprAssignablePos = new de.peeeq.wurstscript.ast.ExprAssignablePos.Switch<Object, E>() { public final Object CaseExprMemberVarPos(de.peeeq.wurstscript.ast.ExprMemberVarPos term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVarPos(de.peeeq.wurstscript.ast.ExprMemberArrayVarPos term) throws E { visit(term); return null; } public final Object CaseExprVarAccessPos(de.peeeq.wurstscript.ast.ExprVarAccessPos term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccessPos(de.peeeq.wurstscript.ast.ExprVarArrayAccessPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.ExprAtomicPos.Switch<Object, E> variantVisit$ExprAtomicPos = new de.peeeq.wurstscript.ast.ExprAtomicPos.Switch<Object, E>() { public final Object CaseExprIntValPos(de.peeeq.wurstscript.ast.ExprIntValPos term) throws E { visit(term); return null; } public final Object CaseExprRealValPos(de.peeeq.wurstscript.ast.ExprRealValPos term) throws E { visit(term); return null; } public final Object CaseExprStringValPos(de.peeeq.wurstscript.ast.ExprStringValPos term) throws E { visit(term); return null; } public final Object CaseExprBoolValPos(de.peeeq.wurstscript.ast.ExprBoolValPos term) throws E { visit(term); return null; } public final Object CaseExprFuncRefPos(de.peeeq.wurstscript.ast.ExprFuncRefPos term) throws E { visit(term); return null; } public final Object CaseExprVarAccessPos(de.peeeq.wurstscript.ast.ExprVarAccessPos term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccessPos(de.peeeq.wurstscript.ast.ExprVarArrayAccessPos term) throws E { visit(term); return null; } public final Object CaseExprThisPos(de.peeeq.wurstscript.ast.ExprThisPos term) throws E { visit(term); return null; } public final Object CaseExprNullPos(de.peeeq.wurstscript.ast.ExprNullPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.ExprPos.Switch<Object, E> variantVisit$ExprPos = new de.peeeq.wurstscript.ast.ExprPos.Switch<Object, E>() { public final Object CaseExprBinaryPos(de.peeeq.wurstscript.ast.ExprBinaryPos term) throws E { visit(term); return null; } public final Object CaseExprUnaryPos(de.peeeq.wurstscript.ast.ExprUnaryPos term) throws E { visit(term); return null; } public final Object CaseExprMemberVarPos(de.peeeq.wurstscript.ast.ExprMemberVarPos term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVarPos(de.peeeq.wurstscript.ast.ExprMemberArrayVarPos term) throws E { visit(term); return null; } public final Object CaseExprMemberMethodPos(de.peeeq.wurstscript.ast.ExprMemberMethodPos term) throws E { visit(term); return null; } public final Object CaseExprFunctionCallPos(de.peeeq.wurstscript.ast.ExprFunctionCallPos term) throws E { visit(term); return null; } public final Object CaseExprNewObjectPos(de.peeeq.wurstscript.ast.ExprNewObjectPos term) throws E { visit(term); return null; } public final Object CaseExprCastPos(de.peeeq.wurstscript.ast.ExprCastPos term) throws E { visit(term); return null; } public final Object CaseExprVarAccessPos(de.peeeq.wurstscript.ast.ExprVarAccessPos term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccessPos(de.peeeq.wurstscript.ast.ExprVarArrayAccessPos term) throws E { visit(term); return null; } public final Object CaseExprIntValPos(de.peeeq.wurstscript.ast.ExprIntValPos term) throws E { visit(term); return null; } public final Object CaseExprRealValPos(de.peeeq.wurstscript.ast.ExprRealValPos term) throws E { visit(term); return null; } public final Object CaseExprStringValPos(de.peeeq.wurstscript.ast.ExprStringValPos term) throws E { visit(term); return null; } public final Object CaseExprBoolValPos(de.peeeq.wurstscript.ast.ExprBoolValPos term) throws E { visit(term); return null; } public final Object CaseExprFuncRefPos(de.peeeq.wurstscript.ast.ExprFuncRefPos term) throws E { visit(term); return null; } public final Object CaseExprThisPos(de.peeeq.wurstscript.ast.ExprThisPos term) throws E { visit(term); return null; } public final Object CaseExprNullPos(de.peeeq.wurstscript.ast.ExprNullPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpBinaryPos.Switch<Object, E> variantVisit$OpBinaryPos = new de.peeeq.wurstscript.ast.OpBinaryPos.Switch<Object, E>() { public final Object CaseOpOrPos(de.peeeq.wurstscript.ast.OpOrPos term) throws E { visit(term); return null; } public final Object CaseOpAndPos(de.peeeq.wurstscript.ast.OpAndPos term) throws E { visit(term); return null; } public final Object CaseOpEqualsPos(de.peeeq.wurstscript.ast.OpEqualsPos term) throws E { visit(term); return null; } public final Object CaseOpUnequalsPos(de.peeeq.wurstscript.ast.OpUnequalsPos term) throws E { visit(term); return null; } public final Object CaseOpLessEqPos(de.peeeq.wurstscript.ast.OpLessEqPos term) throws E { visit(term); return null; } public final Object CaseOpLessPos(de.peeeq.wurstscript.ast.OpLessPos term) throws E { visit(term); return null; } public final Object CaseOpGreaterEqPos(de.peeeq.wurstscript.ast.OpGreaterEqPos term) throws E { visit(term); return null; } public final Object CaseOpGreaterPos(de.peeeq.wurstscript.ast.OpGreaterPos term) throws E { visit(term); return null; } public final Object CaseOpPlusPos(de.peeeq.wurstscript.ast.OpPlusPos term) throws E { visit(term); return null; } public final Object CaseOpMinusPos(de.peeeq.wurstscript.ast.OpMinusPos term) throws E { visit(term); return null; } public final Object CaseOpMultPos(de.peeeq.wurstscript.ast.OpMultPos term) throws E { visit(term); return null; } public final Object CaseOpDivRealPos(de.peeeq.wurstscript.ast.OpDivRealPos term) throws E { visit(term); return null; } public final Object CaseOpModRealPos(de.peeeq.wurstscript.ast.OpModRealPos term) throws E { visit(term); return null; } public final Object CaseOpModIntPos(de.peeeq.wurstscript.ast.OpModIntPos term) throws E { visit(term); return null; } public final Object CaseOpDivIntPos(de.peeeq.wurstscript.ast.OpDivIntPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OpUnaryPos.Switch<Object, E> variantVisit$OpUnaryPos = new de.peeeq.wurstscript.ast.OpUnaryPos.Switch<Object, E>() { public final Object CaseOpNotPos(de.peeeq.wurstscript.ast.OpNotPos term) throws E { visit(term); return null; } public final Object CaseOpMinusPos(de.peeeq.wurstscript.ast.OpMinusPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OptExprPos.Switch<Object, E> variantVisit$OptExprPos = new de.peeeq.wurstscript.ast.OptExprPos.Switch<Object, E>() { public final Object CaseNoExprPos(de.peeeq.wurstscript.ast.NoExprPos term) throws E { visit(term); return null; } public final Object CaseExprBinaryPos(de.peeeq.wurstscript.ast.ExprBinaryPos term) throws E { visit(term); return null; } public final Object CaseExprUnaryPos(de.peeeq.wurstscript.ast.ExprUnaryPos term) throws E { visit(term); return null; } public final Object CaseExprMemberVarPos(de.peeeq.wurstscript.ast.ExprMemberVarPos term) throws E { visit(term); return null; } public final Object CaseExprMemberArrayVarPos(de.peeeq.wurstscript.ast.ExprMemberArrayVarPos term) throws E { visit(term); return null; } public final Object CaseExprMemberMethodPos(de.peeeq.wurstscript.ast.ExprMemberMethodPos term) throws E { visit(term); return null; } public final Object CaseExprFunctionCallPos(de.peeeq.wurstscript.ast.ExprFunctionCallPos term) throws E { visit(term); return null; } public final Object CaseExprNewObjectPos(de.peeeq.wurstscript.ast.ExprNewObjectPos term) throws E { visit(term); return null; } public final Object CaseExprCastPos(de.peeeq.wurstscript.ast.ExprCastPos term) throws E { visit(term); return null; } public final Object CaseExprVarAccessPos(de.peeeq.wurstscript.ast.ExprVarAccessPos term) throws E { visit(term); return null; } public final Object CaseExprVarArrayAccessPos(de.peeeq.wurstscript.ast.ExprVarArrayAccessPos term) throws E { visit(term); return null; } public final Object CaseExprIntValPos(de.peeeq.wurstscript.ast.ExprIntValPos term) throws E { visit(term); return null; } public final Object CaseExprRealValPos(de.peeeq.wurstscript.ast.ExprRealValPos term) throws E { visit(term); return null; } public final Object CaseExprStringValPos(de.peeeq.wurstscript.ast.ExprStringValPos term) throws E { visit(term); return null; } public final Object CaseExprBoolValPos(de.peeeq.wurstscript.ast.ExprBoolValPos term) throws E { visit(term); return null; } public final Object CaseExprFuncRefPos(de.peeeq.wurstscript.ast.ExprFuncRefPos term) throws E { visit(term); return null; } public final Object CaseExprThisPos(de.peeeq.wurstscript.ast.ExprThisPos term) throws E { visit(term); return null; } public final Object CaseExprNullPos(de.peeeq.wurstscript.ast.ExprNullPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.OptTypeExprPos.Switch<Object, E> variantVisit$OptTypeExprPos = new de.peeeq.wurstscript.ast.OptTypeExprPos.Switch<Object, E>() { public final Object CaseNoTypeExprPos(de.peeeq.wurstscript.ast.NoTypeExprPos term) throws E { visit(term); return null; } public final Object CaseTypeExprPos(de.peeeq.wurstscript.ast.TypeExprPos term) throws E { visit(term); return null; } };
        private final de.peeeq.wurstscript.ast.VisibilityModifierPos.Switch<Object, E> variantVisit$VisibilityModifierPos = new de.peeeq.wurstscript.ast.VisibilityModifierPos.Switch<Object, E>() { public final Object CaseVisibilityPublicPos(de.peeeq.wurstscript.ast.VisibilityPublicPos term) throws E { visit(term); return null; } public final Object CaseVisibilityPrivatePos(de.peeeq.wurstscript.ast.VisibilityPrivatePos term) throws E { visit(term); return null; } public final Object CaseVisibilityPublicreadPos(de.peeeq.wurstscript.ast.VisibilityPublicreadPos term) throws E { visit(term); return null; } public final Object CaseVisibilityProtectedPos(de.peeeq.wurstscript.ast.VisibilityProtectedPos term) throws E { visit(term); return null; } public final Object CaseVisibilityDefaultPos(de.peeeq.wurstscript.ast.VisibilityDefaultPos term) throws E { visit(term); return null; } };

        //----- methods of Visitor<E extends Throwable> -----

        public final void visit(de.peeeq.wurstscript.ast.ExprAssignablePos term) throws E {
            term.Switch(variantVisit$ExprAssignablePos);
        }

        public final void visit(de.peeeq.wurstscript.ast.ExprAtomicPos term) throws E {
            term.Switch(variantVisit$ExprAtomicPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.ExprPos term) throws E {
            term.Switch(variantVisit$ExprPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.OpBinaryPos term) throws E {
            term.Switch(variantVisit$OpBinaryPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.OpUnaryPos term) throws E {
            term.Switch(variantVisit$OpUnaryPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.OptExprPos term) throws E {
            term.Switch(variantVisit$OptExprPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.OptTypeExprPos term) throws E {
            term.Switch(variantVisit$OptTypeExprPos);
        }

        public final void visit(de.peeeq.wurstscript.ast.VisibilityModifierPos term) throws E {
            term.Switch(variantVisit$VisibilityModifierPos);
        }
    }

    static class Impl extends KatjaListPosImpl<de.peeeq.wurstscript.ast.CompilationUnitPos, de.peeeq.wurstscript.ast.JassGlobalBlock, de.peeeq.wurstscript.ast.GlobalVarDefPos, de.peeeq.wurstscript.ast.GlobalVarDef> implements de.peeeq.wurstscript.ast.JassGlobalBlockPos {

        //----- methods of Impl -----

        public de.peeeq.wurstscript.ast.JassGlobalBlock termJassToplevelDeclaration() {
            return term();
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlock termTopLevelDeclaration() {
            return term();
        }

        Impl(KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, de.peeeq.wurstscript.ast.JassGlobalBlock term, int pos) {
            super(parent, term, pos);
        }

        protected de.peeeq.wurstscript.ast.GlobalVarDefPos getElementInstance(int pos) {
            return de.peeeq.wurstscript.ast.AST.GlobalVarDefPos(this, _term.get(pos), pos);
        }

        protected de.peeeq.wurstscript.ast.GlobalVarDefPos[] createArray(int size) {
            return new de.peeeq.wurstscript.ast.GlobalVarDefPos[size];
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlockPos add(de.peeeq.wurstscript.ast.GlobalVarDef element) {
            return replace(_term.add(element));
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlockPos addAll(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list) {
            return replace(_term.addAll(list));
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlockPos remove(de.peeeq.wurstscript.ast.GlobalVarDef element) {
            return replace(_term.remove(element));
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlockPos removeAll(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list) {
            return replace(_term.removeAll(list));
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlockPos appBack(de.peeeq.wurstscript.ast.GlobalVarDef element) {
            return replace(_term.appBack(element));
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlockPos appFront(de.peeeq.wurstscript.ast.GlobalVarDef element) {
            return replace(_term.appFront(element));
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlockPos conc(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list) {
            return replace(_term.conc(list));
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlockPos reverse() {
            return replace(_term.reverse());
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlockPos toSet() {
            return replace(_term.toSet());
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlockPos setAdd(de.peeeq.wurstscript.ast.GlobalVarDef element) {
            return replace(_term.setAdd(element));
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlockPos setRemove(de.peeeq.wurstscript.ast.GlobalVarDef element) {
            return replace(_term.setRemove(element));
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlockPos setUnion(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list) {
            return replace(_term.setUnion(list));
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlockPos setWithout(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list) {
            return replace(_term.setWithout(list));
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlockPos setIntersection(KatjaList<? extends de.peeeq.wurstscript.ast.GlobalVarDef> list) {
            return replace(_term.setIntersection(list));
        }

        public KatjaList<de.peeeq.wurstscript.ast.GlobalVarDefPos> toList() {
            for(int i = 0; i < size(); i++) if(values[i] == null) values[i] = getElementInstance(i);
            return new JassGlobalBlockPos.ListImpl(values);
        }

        public de.peeeq.wurstscript.ast.JassGlobalBlockPos replace(de.peeeq.wurstscript.ast.JassGlobalBlock term) {
            return (de.peeeq.wurstscript.ast.JassGlobalBlockPos) super.replace(term);
        }

        protected de.peeeq.wurstscript.ast.CompilationUnitPos freshRootPosition(KatjaSort term) {
            if(!(term instanceof de.peeeq.wurstscript.ast.CompilationUnit))
                throw new IllegalArgumentException("given term to replace root position has not the correct sort CompilationUnit");

            return AST.CompilationUnitPos((CompilationUnit) term);
        }

        public de.peeeq.wurstscript.ast.CompilationUnitPos parent() {
            return (de.peeeq.wurstscript.ast.CompilationUnitPos) super.parent();
        }

        public de.peeeq.wurstscript.ast.TopLevelDeclarationPos lsib() {
            return (de.peeeq.wurstscript.ast.TopLevelDeclarationPos) super.lsib();
        }

        public de.peeeq.wurstscript.ast.TopLevelDeclarationPos rsib() {
            return (de.peeeq.wurstscript.ast.TopLevelDeclarationPos) super.rsib();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos preOrder() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.preOrder();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos preOrderSkip() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.preOrderSkip();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos postOrder() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.postOrder();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos postOrderStart() {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.postOrderStart();
        }

        public de.peeeq.wurstscript.ast.AST.SortPos follow(List<Integer> path) {
            return (de.peeeq.wurstscript.ast.AST.SortPos) super.follow(path);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.JassToplevelDeclarationPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseJassGlobalBlockPos(this);
        }

        public <CT, E extends Throwable> CT Switch(de.peeeq.wurstscript.ast.TopLevelDeclarationPos.Switch<CT, E> switchClass) throws E {
            return switchClass.CaseJassGlobalBlockPos(this);
        }

        public Appendable toJavaCode(Appendable builder) throws IOException {
            builder.append("AST.CompilationUnitPos");
            builder.append("( ");
            root().term().toJavaCode(builder);
            builder.append(" )");
            for(int pos : path()) builder.append(".get("+pos+")");

            return builder;
        }

        public Appendable toString(Appendable builder) throws IOException {
            term().toString(builder);
            builder.append("@CompilationUnit");
            for(int pos : path()) builder.append("."+pos);

            return builder;
        }

        public final String sortName() {
            return "JassGlobalBlockPos";
        }

        //----- nested classes of Impl -----

        static class ListImpl extends KatjaListImpl<de.peeeq.wurstscript.ast.GlobalVarDefPos> implements KatjaList<de.peeeq.wurstscript.ast.GlobalVarDefPos> {

            //----- methods of ListImpl -----

            ListImpl(de.peeeq.wurstscript.ast.GlobalVarDefPos... elements) {
                super(elements);

                for(de.peeeq.wurstscript.ast.GlobalVarDefPos element : elements)
                    if(element == null)
                        throw new IllegalArgumentException("constructor of katja list for position sort GlobalVarDefPos invoked with null element");
            }

            private ListImpl() {
            }

            protected KatjaList<de.peeeq.wurstscript.ast.GlobalVarDefPos> createInstance(de.peeeq.wurstscript.ast.GlobalVarDefPos[] elements, boolean isSet) {
                for(de.peeeq.wurstscript.ast.GlobalVarDefPos element : elements)
                    if(element == null)
                        throw new IllegalArgumentException("constructor of katja list for position sort GlobalVarDefPos invoked with null element");

                ListImpl temp = new ListImpl();
                temp.values = elements;
                temp = (ListImpl) AST.unique(temp);
                if(isSet) temp.set = temp;

                return temp;
            }

            protected de.peeeq.wurstscript.ast.GlobalVarDefPos[] createArray(int size) {
                return new de.peeeq.wurstscript.ast.GlobalVarDefPos[size];
            }

            public Appendable toString(Appendable builder) throws IOException {
                boolean first = true;

                builder.append("KatjaList( ");
                for(de.peeeq.wurstscript.ast.GlobalVarDefPos element : values) {
                    if(first) first = false;
                    else builder.append(", ");
                    element.toString(builder);
                }
                builder.append(" )");

                return builder;
            }

            public Appendable toJavaCode(Appendable builder) throws IOException {
                throw new UnsupportedOperationException("toJavaCode invoked on an anonymous list of positions, you do not want to do that");
            }

            public final String sortName() {
                return "<anonymous>";
            }
        }
    }
}

