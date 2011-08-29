package de.peeeq.wurstscript.ast;

import katja.common.*;
import java.util.*;
import java.lang.ref.*;
import java.io.Reader;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

public class AST extends KatjaSpecification {

    //----- attributes of AST -----

    private static final WeakHashMap<de.peeeq.wurstscript.ast.CompilationUnit, WeakReference<de.peeeq.wurstscript.ast.CompilationUnitPos>> _Poscache = new WeakHashMap<de.peeeq.wurstscript.ast.CompilationUnit, WeakReference<de.peeeq.wurstscript.ast.CompilationUnitPos>>();
    public static final AST$Creator _AST$Creator = new AST$Creator();

    //----- methods of AST -----

    static final KatjaTerm unique(KatjaTerm term) {
        return _termcache.put(term);
    }

    public static final de.peeeq.wurstscript.ast.Arguments Arguments(de.peeeq.wurstscript.ast.Expr... elements) {
        return _termcache.put(new de.peeeq.wurstscript.ast.Arguments.Impl(elements));
    }

    static final de.peeeq.wurstscript.ast.ArgumentsPos ArgumentsPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.Arguments term, final int pos) {
        return new de.peeeq.wurstscript.ast.ArgumentsPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.ArraySizes ArraySizes(de.peeeq.wurstscript.ast.Expr... elements) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ArraySizes.Impl(elements));
    }

    static final de.peeeq.wurstscript.ast.ArraySizesPos ArraySizesPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ArraySizes term, final int pos) {
        return new de.peeeq.wurstscript.ast.ArraySizesPos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.BooleanPos BooleanPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final java.lang.Boolean term, final int pos) {
        return new de.peeeq.wurstscript.ast.BooleanPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.ClassDef ClassDef(de.peeeq.wurstscript.ast.WPos source, java.lang.String name, java.lang.Boolean unmanaged, de.peeeq.wurstscript.ast.ClassSlots slots) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ClassDef.Impl(source, name, unmanaged, slots));
    }

    public static final de.peeeq.wurstscript.ast.ClassDef ClassDef(de.peeeq.wurstscript.ast.WPos source, java.lang.String name, java.lang.Boolean unmanaged, de.peeeq.wurstscript.ast.ClassSlot... slots) {
        de.peeeq.wurstscript.ast.ClassSlots _list = de.peeeq.wurstscript.ast.AST.ClassSlots(slots);
        return _termcache.put(new de.peeeq.wurstscript.ast.ClassDef.Impl(source, name, unmanaged, _list));
    }

    static final de.peeeq.wurstscript.ast.ClassDefPos ClassDefPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ClassDef term, final int pos) {
        return new de.peeeq.wurstscript.ast.ClassDefPos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.ClassMemberPos ClassMemberPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ClassMember term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.ClassMember.Switch<de.peeeq.wurstscript.ast.ClassMemberPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.ClassMemberPos CaseGlobalVarDef(de.peeeq.wurstscript.ast.GlobalVarDef term) { return new de.peeeq.wurstscript.ast.GlobalVarDefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ClassMemberPos CaseFuncDef(de.peeeq.wurstscript.ast.FuncDef term) { return new de.peeeq.wurstscript.ast.FuncDefPos.Impl(parent, term, pos); } });
    }

    static final de.peeeq.wurstscript.ast.ClassSlotPos ClassSlotPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ClassSlot term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.ClassSlot.Switch<de.peeeq.wurstscript.ast.ClassSlotPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.ClassSlotPos CaseConstructorDef(de.peeeq.wurstscript.ast.ConstructorDef term) { return new de.peeeq.wurstscript.ast.ConstructorDefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ClassSlotPos CaseOnDestroyDef(de.peeeq.wurstscript.ast.OnDestroyDef term) { return new de.peeeq.wurstscript.ast.OnDestroyDefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ClassSlotPos CaseGlobalVarDef(de.peeeq.wurstscript.ast.GlobalVarDef term) { return new de.peeeq.wurstscript.ast.GlobalVarDefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ClassSlotPos CaseFuncDef(de.peeeq.wurstscript.ast.FuncDef term) { return new de.peeeq.wurstscript.ast.FuncDefPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.wurstscript.ast.ClassSlots ClassSlots(de.peeeq.wurstscript.ast.ClassSlot... elements) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ClassSlots.Impl(elements));
    }

    static final de.peeeq.wurstscript.ast.ClassSlotsPos ClassSlotsPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ClassSlots term, final int pos) {
        return new de.peeeq.wurstscript.ast.ClassSlotsPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.CompilationUnit CompilationUnit(de.peeeq.wurstscript.ast.TopLevelDeclaration... elements) {
        return _termcache.put(new de.peeeq.wurstscript.ast.CompilationUnit.Impl(elements));
    }

    static final de.peeeq.wurstscript.ast.CompilationUnitPos CompilationUnitPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.CompilationUnit term, final int pos) {
        return new de.peeeq.wurstscript.ast.CompilationUnitPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.CompilationUnitPos CompilationUnitPos(de.peeeq.wurstscript.ast.CompilationUnit term) {
        WeakReference<de.peeeq.wurstscript.ast.CompilationUnitPos> _cached = _Poscache.get(term);
        de.peeeq.wurstscript.ast.CompilationUnitPos _temp = null;
        if(_cached != null) _temp = _cached.get();
        if(_temp != null) return _temp;
        _temp = new de.peeeq.wurstscript.ast.CompilationUnitPos.Impl(term);
        _Poscache.put(term, new WeakReference<de.peeeq.wurstscript.ast.CompilationUnitPos>(_temp));
        return _temp;
    }

    public static final de.peeeq.wurstscript.ast.ConstructorDef ConstructorDef(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.WParameters params, de.peeeq.wurstscript.ast.WStatements body) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ConstructorDef.Impl(source, params, body));
    }

    public static final de.peeeq.wurstscript.ast.ConstructorDef ConstructorDef(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.WParameters params, de.peeeq.wurstscript.ast.WStatement... body) {
        de.peeeq.wurstscript.ast.WStatements _list = de.peeeq.wurstscript.ast.AST.WStatements(body);
        return _termcache.put(new de.peeeq.wurstscript.ast.ConstructorDef.Impl(source, params, _list));
    }

    static final de.peeeq.wurstscript.ast.ConstructorDefPos ConstructorDefPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ConstructorDef term, final int pos) {
        return new de.peeeq.wurstscript.ast.ConstructorDefPos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.DoublePos DoublePos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final java.lang.Double term, final int pos) {
        return new de.peeeq.wurstscript.ast.DoublePos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.ExprAssignablePos ExprAssignablePos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprAssignable term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.ExprAssignable.Switch<de.peeeq.wurstscript.ast.ExprAssignablePos, RuntimeException>() { public final de.peeeq.wurstscript.ast.ExprAssignablePos CaseExprMemberVar(de.peeeq.wurstscript.ast.ExprMemberVar term) { return new de.peeeq.wurstscript.ast.ExprMemberVarPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprAssignablePos CaseExprMemberArrayVar(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) { return new de.peeeq.wurstscript.ast.ExprMemberArrayVarPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprAssignablePos CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) { return new de.peeeq.wurstscript.ast.ExprVarAccessPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprAssignablePos CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) { return new de.peeeq.wurstscript.ast.ExprVarArrayAccessPos.Impl(parent, term, pos); } });
    }

    static final de.peeeq.wurstscript.ast.ExprAtomicPos ExprAtomicPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprAtomic term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.ExprAtomic.Switch<de.peeeq.wurstscript.ast.ExprAtomicPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.ExprAtomicPos CaseExprIntVal(de.peeeq.wurstscript.ast.ExprIntVal term) { return new de.peeeq.wurstscript.ast.ExprIntValPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprAtomicPos CaseExprRealVal(de.peeeq.wurstscript.ast.ExprRealVal term) { return new de.peeeq.wurstscript.ast.ExprRealValPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprAtomicPos CaseExprStringVal(de.peeeq.wurstscript.ast.ExprStringVal term) { return new de.peeeq.wurstscript.ast.ExprStringValPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprAtomicPos CaseExprBoolVal(de.peeeq.wurstscript.ast.ExprBoolVal term) { return new de.peeeq.wurstscript.ast.ExprBoolValPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprAtomicPos CaseExprFuncRef(de.peeeq.wurstscript.ast.ExprFuncRef term) { return new de.peeeq.wurstscript.ast.ExprFuncRefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprAtomicPos CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) { return new de.peeeq.wurstscript.ast.ExprVarAccessPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprAtomicPos CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) { return new de.peeeq.wurstscript.ast.ExprVarArrayAccessPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprAtomicPos CaseExprThis(de.peeeq.wurstscript.ast.ExprThis term) { return new de.peeeq.wurstscript.ast.ExprThisPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprAtomicPos CaseExprNull(de.peeeq.wurstscript.ast.ExprNull term) { return new de.peeeq.wurstscript.ast.ExprNullPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.wurstscript.ast.ExprBinary ExprBinary(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.Expr left, de.peeeq.wurstscript.ast.OpBinary op, de.peeeq.wurstscript.ast.Expr right) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprBinary.Impl(source, left, op, right));
    }

    static final de.peeeq.wurstscript.ast.ExprBinaryPos ExprBinaryPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprBinary term, final int pos) {
        return new de.peeeq.wurstscript.ast.ExprBinaryPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.ExprBoolVal ExprBoolVal(de.peeeq.wurstscript.ast.WPos source, java.lang.Boolean val) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprBoolVal.Impl(source, val));
    }

    static final de.peeeq.wurstscript.ast.ExprBoolValPos ExprBoolValPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprBoolVal term, final int pos) {
        return new de.peeeq.wurstscript.ast.ExprBoolValPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.ExprFuncRef ExprFuncRef(de.peeeq.wurstscript.ast.WPos source, java.lang.String funcName) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprFuncRef.Impl(source, funcName));
    }

    static final de.peeeq.wurstscript.ast.ExprFuncRefPos ExprFuncRefPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprFuncRef term, final int pos) {
        return new de.peeeq.wurstscript.ast.ExprFuncRefPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.ExprFunctionCall ExprFunctionCall(de.peeeq.wurstscript.ast.WPos source, java.lang.String funcName, de.peeeq.wurstscript.ast.Arguments args) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprFunctionCall.Impl(source, funcName, args));
    }

    public static final de.peeeq.wurstscript.ast.ExprFunctionCall ExprFunctionCall(de.peeeq.wurstscript.ast.WPos source, java.lang.String funcName, de.peeeq.wurstscript.ast.Expr... args) {
        de.peeeq.wurstscript.ast.Arguments _list = de.peeeq.wurstscript.ast.AST.Arguments(args);
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprFunctionCall.Impl(source, funcName, _list));
    }

    static final de.peeeq.wurstscript.ast.ExprFunctionCallPos ExprFunctionCallPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprFunctionCall term, final int pos) {
        return new de.peeeq.wurstscript.ast.ExprFunctionCallPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.ExprIntVal ExprIntVal(de.peeeq.wurstscript.ast.WPos source, java.lang.Integer val) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprIntVal.Impl(source, val));
    }

    static final de.peeeq.wurstscript.ast.ExprIntValPos ExprIntValPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprIntVal term, final int pos) {
        return new de.peeeq.wurstscript.ast.ExprIntValPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.ExprMemberArrayVar ExprMemberArrayVar(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.Expr left, java.lang.String varName, de.peeeq.wurstscript.ast.Indexes indexes) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprMemberArrayVar.Impl(source, left, varName, indexes));
    }

    public static final de.peeeq.wurstscript.ast.ExprMemberArrayVar ExprMemberArrayVar(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.Expr left, java.lang.String varName, de.peeeq.wurstscript.ast.Expr... indexes) {
        de.peeeq.wurstscript.ast.Indexes _list = de.peeeq.wurstscript.ast.AST.Indexes(indexes);
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprMemberArrayVar.Impl(source, left, varName, _list));
    }

    static final de.peeeq.wurstscript.ast.ExprMemberArrayVarPos ExprMemberArrayVarPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprMemberArrayVar term, final int pos) {
        return new de.peeeq.wurstscript.ast.ExprMemberArrayVarPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.ExprMemberMethod ExprMemberMethod(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.Expr left, java.lang.String funcName, de.peeeq.wurstscript.ast.Arguments args) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprMemberMethod.Impl(source, left, funcName, args));
    }

    public static final de.peeeq.wurstscript.ast.ExprMemberMethod ExprMemberMethod(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.Expr left, java.lang.String funcName, de.peeeq.wurstscript.ast.Expr... args) {
        de.peeeq.wurstscript.ast.Arguments _list = de.peeeq.wurstscript.ast.AST.Arguments(args);
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprMemberMethod.Impl(source, left, funcName, _list));
    }

    static final de.peeeq.wurstscript.ast.ExprMemberMethodPos ExprMemberMethodPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprMemberMethod term, final int pos) {
        return new de.peeeq.wurstscript.ast.ExprMemberMethodPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.ExprMemberVar ExprMemberVar(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.Expr left, java.lang.String varName) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprMemberVar.Impl(source, left, varName));
    }

    static final de.peeeq.wurstscript.ast.ExprMemberVarPos ExprMemberVarPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprMemberVar term, final int pos) {
        return new de.peeeq.wurstscript.ast.ExprMemberVarPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.ExprNewObject ExprNewObject(de.peeeq.wurstscript.ast.WPos source, java.lang.String typeName, de.peeeq.wurstscript.ast.Arguments args) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprNewObject.Impl(source, typeName, args));
    }

    public static final de.peeeq.wurstscript.ast.ExprNewObject ExprNewObject(de.peeeq.wurstscript.ast.WPos source, java.lang.String typeName, de.peeeq.wurstscript.ast.Expr... args) {
        de.peeeq.wurstscript.ast.Arguments _list = de.peeeq.wurstscript.ast.AST.Arguments(args);
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprNewObject.Impl(source, typeName, _list));
    }

    static final de.peeeq.wurstscript.ast.ExprNewObjectPos ExprNewObjectPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprNewObject term, final int pos) {
        return new de.peeeq.wurstscript.ast.ExprNewObjectPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.ExprNull ExprNull(de.peeeq.wurstscript.ast.WPos source) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprNull.Impl(source));
    }

    static final de.peeeq.wurstscript.ast.ExprNullPos ExprNullPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprNull term, final int pos) {
        return new de.peeeq.wurstscript.ast.ExprNullPos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.ExprPos ExprPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.Expr term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.Expr.Switch<de.peeeq.wurstscript.ast.ExprPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.ExprPos CaseExprBinary(de.peeeq.wurstscript.ast.ExprBinary term) { return new de.peeeq.wurstscript.ast.ExprBinaryPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprPos CaseExprUnary(de.peeeq.wurstscript.ast.ExprUnary term) { return new de.peeeq.wurstscript.ast.ExprUnaryPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprPos CaseExprMemberVar(de.peeeq.wurstscript.ast.ExprMemberVar term) { return new de.peeeq.wurstscript.ast.ExprMemberVarPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprPos CaseExprMemberArrayVar(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) { return new de.peeeq.wurstscript.ast.ExprMemberArrayVarPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprPos CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) { return new de.peeeq.wurstscript.ast.ExprMemberMethodPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprPos CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) { return new de.peeeq.wurstscript.ast.ExprFunctionCallPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprPos CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) { return new de.peeeq.wurstscript.ast.ExprNewObjectPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprPos CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) { return new de.peeeq.wurstscript.ast.ExprVarAccessPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprPos CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) { return new de.peeeq.wurstscript.ast.ExprVarArrayAccessPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprPos CaseExprIntVal(de.peeeq.wurstscript.ast.ExprIntVal term) { return new de.peeeq.wurstscript.ast.ExprIntValPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprPos CaseExprRealVal(de.peeeq.wurstscript.ast.ExprRealVal term) { return new de.peeeq.wurstscript.ast.ExprRealValPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprPos CaseExprStringVal(de.peeeq.wurstscript.ast.ExprStringVal term) { return new de.peeeq.wurstscript.ast.ExprStringValPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprPos CaseExprBoolVal(de.peeeq.wurstscript.ast.ExprBoolVal term) { return new de.peeeq.wurstscript.ast.ExprBoolValPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprPos CaseExprFuncRef(de.peeeq.wurstscript.ast.ExprFuncRef term) { return new de.peeeq.wurstscript.ast.ExprFuncRefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprPos CaseExprThis(de.peeeq.wurstscript.ast.ExprThis term) { return new de.peeeq.wurstscript.ast.ExprThisPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.ExprPos CaseExprNull(de.peeeq.wurstscript.ast.ExprNull term) { return new de.peeeq.wurstscript.ast.ExprNullPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.wurstscript.ast.ExprRealVal ExprRealVal(de.peeeq.wurstscript.ast.WPos source, java.lang.Double val) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprRealVal.Impl(source, val));
    }

    static final de.peeeq.wurstscript.ast.ExprRealValPos ExprRealValPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprRealVal term, final int pos) {
        return new de.peeeq.wurstscript.ast.ExprRealValPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.ExprStringVal ExprStringVal(de.peeeq.wurstscript.ast.WPos source, java.lang.String val) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprStringVal.Impl(source, val));
    }

    static final de.peeeq.wurstscript.ast.ExprStringValPos ExprStringValPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprStringVal term, final int pos) {
        return new de.peeeq.wurstscript.ast.ExprStringValPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.ExprThis ExprThis(de.peeeq.wurstscript.ast.WPos source) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprThis.Impl(source));
    }

    static final de.peeeq.wurstscript.ast.ExprThisPos ExprThisPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprThis term, final int pos) {
        return new de.peeeq.wurstscript.ast.ExprThisPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.ExprUnary ExprUnary(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.OpUnary op, de.peeeq.wurstscript.ast.Expr right) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprUnary.Impl(source, op, right));
    }

    static final de.peeeq.wurstscript.ast.ExprUnaryPos ExprUnaryPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprUnary term, final int pos) {
        return new de.peeeq.wurstscript.ast.ExprUnaryPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.ExprVarAccess ExprVarAccess(de.peeeq.wurstscript.ast.WPos source, java.lang.String varName) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprVarAccess.Impl(source, varName));
    }

    static final de.peeeq.wurstscript.ast.ExprVarAccessPos ExprVarAccessPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprVarAccess term, final int pos) {
        return new de.peeeq.wurstscript.ast.ExprVarAccessPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.ExprVarArrayAccess ExprVarArrayAccess(de.peeeq.wurstscript.ast.WPos source, java.lang.String varName, de.peeeq.wurstscript.ast.Indexes indexes) {
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprVarArrayAccess.Impl(source, varName, indexes));
    }

    public static final de.peeeq.wurstscript.ast.ExprVarArrayAccess ExprVarArrayAccess(de.peeeq.wurstscript.ast.WPos source, java.lang.String varName, de.peeeq.wurstscript.ast.Expr... indexes) {
        de.peeeq.wurstscript.ast.Indexes _list = de.peeeq.wurstscript.ast.AST.Indexes(indexes);
        return _termcache.put(new de.peeeq.wurstscript.ast.ExprVarArrayAccess.Impl(source, varName, _list));
    }

    static final de.peeeq.wurstscript.ast.ExprVarArrayAccessPos ExprVarArrayAccessPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.ExprVarArrayAccess term, final int pos) {
        return new de.peeeq.wurstscript.ast.ExprVarArrayAccessPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.FuncDef FuncDef(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.FuncSignature signature, de.peeeq.wurstscript.ast.WStatements body) {
        return _termcache.put(new de.peeeq.wurstscript.ast.FuncDef.Impl(source, signature, body));
    }

    public static final de.peeeq.wurstscript.ast.FuncDef FuncDef(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.FuncSignature signature, de.peeeq.wurstscript.ast.WStatement... body) {
        de.peeeq.wurstscript.ast.WStatements _list = de.peeeq.wurstscript.ast.AST.WStatements(body);
        return _termcache.put(new de.peeeq.wurstscript.ast.FuncDef.Impl(source, signature, _list));
    }

    static final de.peeeq.wurstscript.ast.FuncDefPos FuncDefPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.FuncDef term, final int pos) {
        return new de.peeeq.wurstscript.ast.FuncDefPos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.FuncRefPos FuncRefPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.FuncRef term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.FuncRef.Switch<de.peeeq.wurstscript.ast.FuncRefPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.FuncRefPos CaseExprFuncRef(de.peeeq.wurstscript.ast.ExprFuncRef term) { return new de.peeeq.wurstscript.ast.ExprFuncRefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.FuncRefPos CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) { return new de.peeeq.wurstscript.ast.ExprMemberMethodPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.FuncRefPos CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) { return new de.peeeq.wurstscript.ast.ExprFunctionCallPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.wurstscript.ast.FuncSignature FuncSignature(de.peeeq.wurstscript.ast.WPos source, java.lang.String name, de.peeeq.wurstscript.ast.WParameters parameters, de.peeeq.wurstscript.ast.OptTypeExpr typ) {
        return _termcache.put(new de.peeeq.wurstscript.ast.FuncSignature.Impl(source, name, parameters, typ));
    }

    static final de.peeeq.wurstscript.ast.FuncSignaturePos FuncSignaturePos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.FuncSignature term, final int pos) {
        return new de.peeeq.wurstscript.ast.FuncSignaturePos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.FunctionDefinitionPos FunctionDefinitionPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.FunctionDefinition term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.FunctionDefinition.Switch<de.peeeq.wurstscript.ast.FunctionDefinitionPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.FunctionDefinitionPos CaseFuncDef(de.peeeq.wurstscript.ast.FuncDef term) { return new de.peeeq.wurstscript.ast.FuncDefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.FunctionDefinitionPos CaseNativeFunc(de.peeeq.wurstscript.ast.NativeFunc term) { return new de.peeeq.wurstscript.ast.NativeFuncPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.wurstscript.ast.GlobalVarDef GlobalVarDef(de.peeeq.wurstscript.ast.WPos source, java.lang.Boolean isConstant, de.peeeq.wurstscript.ast.OptTypeExpr typ, java.lang.String name, de.peeeq.wurstscript.ast.OptExpr initialExpr) {
        return _termcache.put(new de.peeeq.wurstscript.ast.GlobalVarDef.Impl(source, isConstant, typ, name, initialExpr));
    }

    static final de.peeeq.wurstscript.ast.GlobalVarDefPos GlobalVarDefPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.GlobalVarDef term, final int pos) {
        return new de.peeeq.wurstscript.ast.GlobalVarDefPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.Indexes Indexes(de.peeeq.wurstscript.ast.Expr... elements) {
        return _termcache.put(new de.peeeq.wurstscript.ast.Indexes.Impl(elements));
    }

    static final de.peeeq.wurstscript.ast.IndexesPos IndexesPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.Indexes term, final int pos) {
        return new de.peeeq.wurstscript.ast.IndexesPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.InitBlock InitBlock(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.WStatements body) {
        return _termcache.put(new de.peeeq.wurstscript.ast.InitBlock.Impl(source, body));
    }

    public static final de.peeeq.wurstscript.ast.InitBlock InitBlock(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.WStatement... body) {
        de.peeeq.wurstscript.ast.WStatements _list = de.peeeq.wurstscript.ast.AST.WStatements(body);
        return _termcache.put(new de.peeeq.wurstscript.ast.InitBlock.Impl(source, _list));
    }

    static final de.peeeq.wurstscript.ast.InitBlockPos InitBlockPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.InitBlock term, final int pos) {
        return new de.peeeq.wurstscript.ast.InitBlockPos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.IntegerPos IntegerPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final java.lang.Integer term, final int pos) {
        return new de.peeeq.wurstscript.ast.IntegerPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.JassGlobalBlock JassGlobalBlock(de.peeeq.wurstscript.ast.GlobalVarDef... elements) {
        return _termcache.put(new de.peeeq.wurstscript.ast.JassGlobalBlock.Impl(elements));
    }

    static final de.peeeq.wurstscript.ast.JassGlobalBlockPos JassGlobalBlockPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.JassGlobalBlock term, final int pos) {
        return new de.peeeq.wurstscript.ast.JassGlobalBlockPos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.JassToplevelDeclarationPos JassToplevelDeclarationPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.JassToplevelDeclaration term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.JassToplevelDeclaration.Switch<de.peeeq.wurstscript.ast.JassToplevelDeclarationPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.JassToplevelDeclarationPos CaseJassGlobalBlock(de.peeeq.wurstscript.ast.JassGlobalBlock term) { return new de.peeeq.wurstscript.ast.JassGlobalBlockPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.JassToplevelDeclarationPos CaseNativeType(de.peeeq.wurstscript.ast.NativeType term) { return new de.peeeq.wurstscript.ast.NativeTypePos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.JassToplevelDeclarationPos CaseFuncDef(de.peeeq.wurstscript.ast.FuncDef term) { return new de.peeeq.wurstscript.ast.FuncDefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.JassToplevelDeclarationPos CaseNativeFunc(de.peeeq.wurstscript.ast.NativeFunc term) { return new de.peeeq.wurstscript.ast.NativeFuncPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.wurstscript.ast.LocalVarDef LocalVarDef(de.peeeq.wurstscript.ast.WPos source, java.lang.Boolean constant, de.peeeq.wurstscript.ast.OptTypeExpr typ, java.lang.String name, de.peeeq.wurstscript.ast.OptExpr initialExpr) {
        return _termcache.put(new de.peeeq.wurstscript.ast.LocalVarDef.Impl(source, constant, typ, name, initialExpr));
    }

    static final de.peeeq.wurstscript.ast.LocalVarDefPos LocalVarDefPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.LocalVarDef term, final int pos) {
        return new de.peeeq.wurstscript.ast.LocalVarDefPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.NativeFunc NativeFunc(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.FuncSignature signature) {
        return _termcache.put(new de.peeeq.wurstscript.ast.NativeFunc.Impl(source, signature));
    }

    static final de.peeeq.wurstscript.ast.NativeFuncPos NativeFuncPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.NativeFunc term, final int pos) {
        return new de.peeeq.wurstscript.ast.NativeFuncPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.NativeType NativeType(de.peeeq.wurstscript.ast.WPos source, java.lang.String name, de.peeeq.wurstscript.ast.OptTypeExpr typ) {
        return _termcache.put(new de.peeeq.wurstscript.ast.NativeType.Impl(source, name, typ));
    }

    static final de.peeeq.wurstscript.ast.NativeTypePos NativeTypePos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.NativeType term, final int pos) {
        return new de.peeeq.wurstscript.ast.NativeTypePos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.NoExpr NoExpr() {
        return _termcache.put(new de.peeeq.wurstscript.ast.NoExpr.Impl());
    }

    static final de.peeeq.wurstscript.ast.NoExprPos NoExprPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.NoExpr term, final int pos) {
        return new de.peeeq.wurstscript.ast.NoExprPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.NoTypeExpr NoTypeExpr() {
        return _termcache.put(new de.peeeq.wurstscript.ast.NoTypeExpr.Impl());
    }

    static final de.peeeq.wurstscript.ast.NoTypeExprPos NoTypeExprPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.NoTypeExpr term, final int pos) {
        return new de.peeeq.wurstscript.ast.NoTypeExprPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.OnDestroyDef OnDestroyDef(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.WStatements body) {
        return _termcache.put(new de.peeeq.wurstscript.ast.OnDestroyDef.Impl(source, body));
    }

    public static final de.peeeq.wurstscript.ast.OnDestroyDef OnDestroyDef(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.WStatement... body) {
        de.peeeq.wurstscript.ast.WStatements _list = de.peeeq.wurstscript.ast.AST.WStatements(body);
        return _termcache.put(new de.peeeq.wurstscript.ast.OnDestroyDef.Impl(source, _list));
    }

    static final de.peeeq.wurstscript.ast.OnDestroyDefPos OnDestroyDefPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OnDestroyDef term, final int pos) {
        return new de.peeeq.wurstscript.ast.OnDestroyDefPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.OpAnd OpAnd() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpAnd.Impl());
    }

    static final de.peeeq.wurstscript.ast.OpAndPos OpAndPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpAnd term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpAndPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.OpAssign OpAssign() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpAssign.Impl());
    }

    static final de.peeeq.wurstscript.ast.OpAssignPos OpAssignPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpAssign term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpAssignPos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.OpAssignmentPos OpAssignmentPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpAssignment term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.OpAssignment.Switch<de.peeeq.wurstscript.ast.OpAssignmentPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.OpAssignmentPos CaseOpAssign(de.peeeq.wurstscript.ast.OpAssign term) { return new de.peeeq.wurstscript.ast.OpAssignPos.Impl(parent, term, pos); } });
    }

    static final de.peeeq.wurstscript.ast.OpBinaryPos OpBinaryPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpBinary term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.OpBinary.Switch<de.peeeq.wurstscript.ast.OpBinaryPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.OpBinaryPos CaseOpOr(de.peeeq.wurstscript.ast.OpOr term) { return new de.peeeq.wurstscript.ast.OpOrPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpBinaryPos CaseOpAnd(de.peeeq.wurstscript.ast.OpAnd term) { return new de.peeeq.wurstscript.ast.OpAndPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpBinaryPos CaseOpEquals(de.peeeq.wurstscript.ast.OpEquals term) { return new de.peeeq.wurstscript.ast.OpEqualsPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpBinaryPos CaseOpUnequals(de.peeeq.wurstscript.ast.OpUnequals term) { return new de.peeeq.wurstscript.ast.OpUnequalsPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpBinaryPos CaseOpLessEq(de.peeeq.wurstscript.ast.OpLessEq term) { return new de.peeeq.wurstscript.ast.OpLessEqPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpBinaryPos CaseOpLess(de.peeeq.wurstscript.ast.OpLess term) { return new de.peeeq.wurstscript.ast.OpLessPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpBinaryPos CaseOpGreaterEq(de.peeeq.wurstscript.ast.OpGreaterEq term) { return new de.peeeq.wurstscript.ast.OpGreaterEqPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpBinaryPos CaseOpGreater(de.peeeq.wurstscript.ast.OpGreater term) { return new de.peeeq.wurstscript.ast.OpGreaterPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpBinaryPos CaseOpPlus(de.peeeq.wurstscript.ast.OpPlus term) { return new de.peeeq.wurstscript.ast.OpPlusPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpBinaryPos CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) { return new de.peeeq.wurstscript.ast.OpMinusPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpBinaryPos CaseOpMult(de.peeeq.wurstscript.ast.OpMult term) { return new de.peeeq.wurstscript.ast.OpMultPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpBinaryPos CaseOpDivReal(de.peeeq.wurstscript.ast.OpDivReal term) { return new de.peeeq.wurstscript.ast.OpDivRealPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpBinaryPos CaseOpModReal(de.peeeq.wurstscript.ast.OpModReal term) { return new de.peeeq.wurstscript.ast.OpModRealPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpBinaryPos CaseOpModInt(de.peeeq.wurstscript.ast.OpModInt term) { return new de.peeeq.wurstscript.ast.OpModIntPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpBinaryPos CaseOpDivInt(de.peeeq.wurstscript.ast.OpDivInt term) { return new de.peeeq.wurstscript.ast.OpDivIntPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.wurstscript.ast.OpDivInt OpDivInt() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpDivInt.Impl());
    }

    static final de.peeeq.wurstscript.ast.OpDivIntPos OpDivIntPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpDivInt term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpDivIntPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.OpDivReal OpDivReal() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpDivReal.Impl());
    }

    static final de.peeeq.wurstscript.ast.OpDivRealPos OpDivRealPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpDivReal term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpDivRealPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.OpEquals OpEquals() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpEquals.Impl());
    }

    static final de.peeeq.wurstscript.ast.OpEqualsPos OpEqualsPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpEquals term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpEqualsPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.OpGreater OpGreater() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpGreater.Impl());
    }

    public static final de.peeeq.wurstscript.ast.OpGreaterEq OpGreaterEq() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpGreaterEq.Impl());
    }

    static final de.peeeq.wurstscript.ast.OpGreaterEqPos OpGreaterEqPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpGreaterEq term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpGreaterEqPos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.OpGreaterPos OpGreaterPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpGreater term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpGreaterPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.OpLess OpLess() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpLess.Impl());
    }

    public static final de.peeeq.wurstscript.ast.OpLessEq OpLessEq() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpLessEq.Impl());
    }

    static final de.peeeq.wurstscript.ast.OpLessEqPos OpLessEqPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpLessEq term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpLessEqPos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.OpLessPos OpLessPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpLess term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpLessPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.OpMinus OpMinus() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpMinus.Impl());
    }

    static final de.peeeq.wurstscript.ast.OpMinusPos OpMinusPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpMinus term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpMinusPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.OpModInt OpModInt() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpModInt.Impl());
    }

    static final de.peeeq.wurstscript.ast.OpModIntPos OpModIntPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpModInt term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpModIntPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.OpModReal OpModReal() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpModReal.Impl());
    }

    static final de.peeeq.wurstscript.ast.OpModRealPos OpModRealPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpModReal term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpModRealPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.OpMult OpMult() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpMult.Impl());
    }

    static final de.peeeq.wurstscript.ast.OpMultPos OpMultPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpMult term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpMultPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.OpNot OpNot() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpNot.Impl());
    }

    static final de.peeeq.wurstscript.ast.OpNotPos OpNotPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpNot term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpNotPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.OpOr OpOr() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpOr.Impl());
    }

    static final de.peeeq.wurstscript.ast.OpOrPos OpOrPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpOr term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpOrPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.OpPlus OpPlus() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpPlus.Impl());
    }

    static final de.peeeq.wurstscript.ast.OpPlusPos OpPlusPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpPlus term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpPlusPos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.OpPos OpPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.Op term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.Op.Switch<de.peeeq.wurstscript.ast.OpPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.OpPos CaseOpOr(de.peeeq.wurstscript.ast.OpOr term) { return new de.peeeq.wurstscript.ast.OpOrPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpPos CaseOpAnd(de.peeeq.wurstscript.ast.OpAnd term) { return new de.peeeq.wurstscript.ast.OpAndPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpPos CaseOpEquals(de.peeeq.wurstscript.ast.OpEquals term) { return new de.peeeq.wurstscript.ast.OpEqualsPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpPos CaseOpUnequals(de.peeeq.wurstscript.ast.OpUnequals term) { return new de.peeeq.wurstscript.ast.OpUnequalsPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpPos CaseOpLessEq(de.peeeq.wurstscript.ast.OpLessEq term) { return new de.peeeq.wurstscript.ast.OpLessEqPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpPos CaseOpLess(de.peeeq.wurstscript.ast.OpLess term) { return new de.peeeq.wurstscript.ast.OpLessPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpPos CaseOpGreaterEq(de.peeeq.wurstscript.ast.OpGreaterEq term) { return new de.peeeq.wurstscript.ast.OpGreaterEqPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpPos CaseOpGreater(de.peeeq.wurstscript.ast.OpGreater term) { return new de.peeeq.wurstscript.ast.OpGreaterPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpPos CaseOpPlus(de.peeeq.wurstscript.ast.OpPlus term) { return new de.peeeq.wurstscript.ast.OpPlusPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpPos CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) { return new de.peeeq.wurstscript.ast.OpMinusPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpPos CaseOpMult(de.peeeq.wurstscript.ast.OpMult term) { return new de.peeeq.wurstscript.ast.OpMultPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpPos CaseOpDivReal(de.peeeq.wurstscript.ast.OpDivReal term) { return new de.peeeq.wurstscript.ast.OpDivRealPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpPos CaseOpModReal(de.peeeq.wurstscript.ast.OpModReal term) { return new de.peeeq.wurstscript.ast.OpModRealPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpPos CaseOpModInt(de.peeeq.wurstscript.ast.OpModInt term) { return new de.peeeq.wurstscript.ast.OpModIntPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpPos CaseOpDivInt(de.peeeq.wurstscript.ast.OpDivInt term) { return new de.peeeq.wurstscript.ast.OpDivIntPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpPos CaseOpNot(de.peeeq.wurstscript.ast.OpNot term) { return new de.peeeq.wurstscript.ast.OpNotPos.Impl(parent, term, pos); } });
    }

    static final de.peeeq.wurstscript.ast.OpUnaryPos OpUnaryPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpUnary term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.OpUnary.Switch<de.peeeq.wurstscript.ast.OpUnaryPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.OpUnaryPos CaseOpNot(de.peeeq.wurstscript.ast.OpNot term) { return new de.peeeq.wurstscript.ast.OpNotPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OpUnaryPos CaseOpMinus(de.peeeq.wurstscript.ast.OpMinus term) { return new de.peeeq.wurstscript.ast.OpMinusPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.wurstscript.ast.OpUnequals OpUnequals() {
        return _termcache.put(new de.peeeq.wurstscript.ast.OpUnequals.Impl());
    }

    static final de.peeeq.wurstscript.ast.OpUnequalsPos OpUnequalsPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OpUnequals term, final int pos) {
        return new de.peeeq.wurstscript.ast.OpUnequalsPos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.OptExprPos OptExprPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OptExpr term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.OptExpr.Switch<de.peeeq.wurstscript.ast.OptExprPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.OptExprPos CaseNoExpr(de.peeeq.wurstscript.ast.NoExpr term) { return new de.peeeq.wurstscript.ast.NoExprPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptExprPos CaseExprBinary(de.peeeq.wurstscript.ast.ExprBinary term) { return new de.peeeq.wurstscript.ast.ExprBinaryPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptExprPos CaseExprUnary(de.peeeq.wurstscript.ast.ExprUnary term) { return new de.peeeq.wurstscript.ast.ExprUnaryPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptExprPos CaseExprMemberVar(de.peeeq.wurstscript.ast.ExprMemberVar term) { return new de.peeeq.wurstscript.ast.ExprMemberVarPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptExprPos CaseExprMemberArrayVar(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) { return new de.peeeq.wurstscript.ast.ExprMemberArrayVarPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptExprPos CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) { return new de.peeeq.wurstscript.ast.ExprMemberMethodPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptExprPos CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) { return new de.peeeq.wurstscript.ast.ExprFunctionCallPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptExprPos CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) { return new de.peeeq.wurstscript.ast.ExprNewObjectPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptExprPos CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) { return new de.peeeq.wurstscript.ast.ExprVarAccessPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptExprPos CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) { return new de.peeeq.wurstscript.ast.ExprVarArrayAccessPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptExprPos CaseExprIntVal(de.peeeq.wurstscript.ast.ExprIntVal term) { return new de.peeeq.wurstscript.ast.ExprIntValPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptExprPos CaseExprRealVal(de.peeeq.wurstscript.ast.ExprRealVal term) { return new de.peeeq.wurstscript.ast.ExprRealValPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptExprPos CaseExprStringVal(de.peeeq.wurstscript.ast.ExprStringVal term) { return new de.peeeq.wurstscript.ast.ExprStringValPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptExprPos CaseExprBoolVal(de.peeeq.wurstscript.ast.ExprBoolVal term) { return new de.peeeq.wurstscript.ast.ExprBoolValPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptExprPos CaseExprFuncRef(de.peeeq.wurstscript.ast.ExprFuncRef term) { return new de.peeeq.wurstscript.ast.ExprFuncRefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptExprPos CaseExprThis(de.peeeq.wurstscript.ast.ExprThis term) { return new de.peeeq.wurstscript.ast.ExprThisPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptExprPos CaseExprNull(de.peeeq.wurstscript.ast.ExprNull term) { return new de.peeeq.wurstscript.ast.ExprNullPos.Impl(parent, term, pos); } });
    }

    static final de.peeeq.wurstscript.ast.OptTypeExprPos OptTypeExprPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.OptTypeExpr term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.OptTypeExpr.Switch<de.peeeq.wurstscript.ast.OptTypeExprPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.OptTypeExprPos CaseNoTypeExpr(de.peeeq.wurstscript.ast.NoTypeExpr term) { return new de.peeeq.wurstscript.ast.NoTypeExprPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.OptTypeExprPos CaseTypeExpr(de.peeeq.wurstscript.ast.TypeExpr term) { return new de.peeeq.wurstscript.ast.TypeExprPos.Impl(parent, term, pos); } });
    }

    static final de.peeeq.wurstscript.ast.PackageOrGlobalPos PackageOrGlobalPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.PackageOrGlobal term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.PackageOrGlobal.Switch<de.peeeq.wurstscript.ast.PackageOrGlobalPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.PackageOrGlobalPos CaseWPackage(de.peeeq.wurstscript.ast.WPackage term) { return new de.peeeq.wurstscript.ast.WPackagePos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.PackageOrGlobalPos CaseCompilationUnit(de.peeeq.wurstscript.ast.CompilationUnit term) { return new de.peeeq.wurstscript.ast.CompilationUnitPos.Impl(parent, term, pos); } });
    }

    static final de.peeeq.wurstscript.ast.StmtCallPos StmtCallPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.StmtCall term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.StmtCall.Switch<de.peeeq.wurstscript.ast.StmtCallPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.StmtCallPos CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) { return new de.peeeq.wurstscript.ast.ExprMemberMethodPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.StmtCallPos CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) { return new de.peeeq.wurstscript.ast.ExprFunctionCallPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.StmtCallPos CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) { return new de.peeeq.wurstscript.ast.ExprNewObjectPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.wurstscript.ast.StmtDecRefCount StmtDecRefCount(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.Expr obj) {
        return _termcache.put(new de.peeeq.wurstscript.ast.StmtDecRefCount.Impl(source, obj));
    }

    static final de.peeeq.wurstscript.ast.StmtDecRefCountPos StmtDecRefCountPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.StmtDecRefCount term, final int pos) {
        return new de.peeeq.wurstscript.ast.StmtDecRefCountPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.StmtDestroy StmtDestroy(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.Expr obj) {
        return _termcache.put(new de.peeeq.wurstscript.ast.StmtDestroy.Impl(source, obj));
    }

    static final de.peeeq.wurstscript.ast.StmtDestroyPos StmtDestroyPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.StmtDestroy term, final int pos) {
        return new de.peeeq.wurstscript.ast.StmtDestroyPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.StmtErr StmtErr(de.peeeq.wurstscript.ast.WPos source) {
        return _termcache.put(new de.peeeq.wurstscript.ast.StmtErr.Impl(source));
    }

    static final de.peeeq.wurstscript.ast.StmtErrPos StmtErrPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.StmtErr term, final int pos) {
        return new de.peeeq.wurstscript.ast.StmtErrPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.StmtExitwhen StmtExitwhen(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.Expr cond) {
        return _termcache.put(new de.peeeq.wurstscript.ast.StmtExitwhen.Impl(source, cond));
    }

    static final de.peeeq.wurstscript.ast.StmtExitwhenPos StmtExitwhenPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.StmtExitwhen term, final int pos) {
        return new de.peeeq.wurstscript.ast.StmtExitwhenPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.StmtIf StmtIf(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.Expr cond, de.peeeq.wurstscript.ast.WStatements thenBlock, de.peeeq.wurstscript.ast.WStatements elseBlock) {
        return _termcache.put(new de.peeeq.wurstscript.ast.StmtIf.Impl(source, cond, thenBlock, elseBlock));
    }

    public static final de.peeeq.wurstscript.ast.StmtIf StmtIf(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.Expr cond, de.peeeq.wurstscript.ast.WStatements thenBlock, de.peeeq.wurstscript.ast.WStatement... elseBlock) {
        de.peeeq.wurstscript.ast.WStatements _list = de.peeeq.wurstscript.ast.AST.WStatements(elseBlock);
        return _termcache.put(new de.peeeq.wurstscript.ast.StmtIf.Impl(source, cond, thenBlock, _list));
    }

    static final de.peeeq.wurstscript.ast.StmtIfPos StmtIfPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.StmtIf term, final int pos) {
        return new de.peeeq.wurstscript.ast.StmtIfPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.StmtIncRefCount StmtIncRefCount(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.Expr obj) {
        return _termcache.put(new de.peeeq.wurstscript.ast.StmtIncRefCount.Impl(source, obj));
    }

    static final de.peeeq.wurstscript.ast.StmtIncRefCountPos StmtIncRefCountPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.StmtIncRefCount term, final int pos) {
        return new de.peeeq.wurstscript.ast.StmtIncRefCountPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.StmtLoop StmtLoop(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.WStatements body) {
        return _termcache.put(new de.peeeq.wurstscript.ast.StmtLoop.Impl(source, body));
    }

    public static final de.peeeq.wurstscript.ast.StmtLoop StmtLoop(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.WStatement... body) {
        de.peeeq.wurstscript.ast.WStatements _list = de.peeeq.wurstscript.ast.AST.WStatements(body);
        return _termcache.put(new de.peeeq.wurstscript.ast.StmtLoop.Impl(source, _list));
    }

    static final de.peeeq.wurstscript.ast.StmtLoopPos StmtLoopPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.StmtLoop term, final int pos) {
        return new de.peeeq.wurstscript.ast.StmtLoopPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.StmtReturn StmtReturn(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.OptExpr obj) {
        return _termcache.put(new de.peeeq.wurstscript.ast.StmtReturn.Impl(source, obj));
    }

    static final de.peeeq.wurstscript.ast.StmtReturnPos StmtReturnPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.StmtReturn term, final int pos) {
        return new de.peeeq.wurstscript.ast.StmtReturnPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.StmtSet StmtSet(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.ExprAssignable left, de.peeeq.wurstscript.ast.OpAssignment op, de.peeeq.wurstscript.ast.Expr right) {
        return _termcache.put(new de.peeeq.wurstscript.ast.StmtSet.Impl(source, left, op, right));
    }

    static final de.peeeq.wurstscript.ast.StmtSetPos StmtSetPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.StmtSet term, final int pos) {
        return new de.peeeq.wurstscript.ast.StmtSetPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.StmtWhile StmtWhile(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.Expr cond, de.peeeq.wurstscript.ast.WStatements body) {
        return _termcache.put(new de.peeeq.wurstscript.ast.StmtWhile.Impl(source, cond, body));
    }

    public static final de.peeeq.wurstscript.ast.StmtWhile StmtWhile(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.Expr cond, de.peeeq.wurstscript.ast.WStatement... body) {
        de.peeeq.wurstscript.ast.WStatements _list = de.peeeq.wurstscript.ast.AST.WStatements(body);
        return _termcache.put(new de.peeeq.wurstscript.ast.StmtWhile.Impl(source, cond, _list));
    }

    static final de.peeeq.wurstscript.ast.StmtWhilePos StmtWhilePos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.StmtWhile term, final int pos) {
        return new de.peeeq.wurstscript.ast.StmtWhilePos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.StringPos StringPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final java.lang.String term, final int pos) {
        return new de.peeeq.wurstscript.ast.StringPos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.TopLevelDeclarationPos TopLevelDeclarationPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.TopLevelDeclaration term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.TopLevelDeclaration.Switch<de.peeeq.wurstscript.ast.TopLevelDeclarationPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.TopLevelDeclarationPos CaseWPackage(de.peeeq.wurstscript.ast.WPackage term) { return new de.peeeq.wurstscript.ast.WPackagePos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.TopLevelDeclarationPos CaseJassGlobalBlock(de.peeeq.wurstscript.ast.JassGlobalBlock term) { return new de.peeeq.wurstscript.ast.JassGlobalBlockPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.TopLevelDeclarationPos CaseNativeType(de.peeeq.wurstscript.ast.NativeType term) { return new de.peeeq.wurstscript.ast.NativeTypePos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.TopLevelDeclarationPos CaseFuncDef(de.peeeq.wurstscript.ast.FuncDef term) { return new de.peeeq.wurstscript.ast.FuncDefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.TopLevelDeclarationPos CaseNativeFunc(de.peeeq.wurstscript.ast.NativeFunc term) { return new de.peeeq.wurstscript.ast.NativeFuncPos.Impl(parent, term, pos); } });
    }

    static final de.peeeq.wurstscript.ast.TypeDefPos TypeDefPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.TypeDef term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.TypeDef.Switch<de.peeeq.wurstscript.ast.TypeDefPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.TypeDefPos CaseNativeType(de.peeeq.wurstscript.ast.NativeType term) { return new de.peeeq.wurstscript.ast.NativeTypePos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.TypeDefPos CaseClassDef(de.peeeq.wurstscript.ast.ClassDef term) { return new de.peeeq.wurstscript.ast.ClassDefPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.wurstscript.ast.TypeExpr TypeExpr(de.peeeq.wurstscript.ast.WPos source, java.lang.String typeName, java.lang.Boolean isArray, de.peeeq.wurstscript.ast.ArraySizes sizes) {
        return _termcache.put(new de.peeeq.wurstscript.ast.TypeExpr.Impl(source, typeName, isArray, sizes));
    }

    public static final de.peeeq.wurstscript.ast.TypeExpr TypeExpr(de.peeeq.wurstscript.ast.WPos source, java.lang.String typeName, java.lang.Boolean isArray, de.peeeq.wurstscript.ast.Expr... sizes) {
        de.peeeq.wurstscript.ast.ArraySizes _list = de.peeeq.wurstscript.ast.AST.ArraySizes(sizes);
        return _termcache.put(new de.peeeq.wurstscript.ast.TypeExpr.Impl(source, typeName, isArray, _list));
    }

    static final de.peeeq.wurstscript.ast.TypeExprPos TypeExprPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.TypeExpr term, final int pos) {
        return new de.peeeq.wurstscript.ast.TypeExprPos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.TypeRefPos TypeRefPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.TypeRef term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.TypeRef.Switch<de.peeeq.wurstscript.ast.TypeRefPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.TypeRefPos CaseTypeExpr(de.peeeq.wurstscript.ast.TypeExpr term) { return new de.peeeq.wurstscript.ast.TypeExprPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.TypeRefPos CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) { return new de.peeeq.wurstscript.ast.ExprNewObjectPos.Impl(parent, term, pos); } });
    }

    static final de.peeeq.wurstscript.ast.VarDefPos VarDefPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.VarDef term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.VarDef.Switch<de.peeeq.wurstscript.ast.VarDefPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.VarDefPos CaseGlobalVarDef(de.peeeq.wurstscript.ast.GlobalVarDef term) { return new de.peeeq.wurstscript.ast.GlobalVarDefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.VarDefPos CaseLocalVarDef(de.peeeq.wurstscript.ast.LocalVarDef term) { return new de.peeeq.wurstscript.ast.LocalVarDefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.VarDefPos CaseWParameter(de.peeeq.wurstscript.ast.WParameter term) { return new de.peeeq.wurstscript.ast.WParameterPos.Impl(parent, term, pos); } });
    }

    static final de.peeeq.wurstscript.ast.VarRefPos VarRefPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.VarRef term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.VarRef.Switch<de.peeeq.wurstscript.ast.VarRefPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.VarRefPos CaseExprVarArrayAccess(de.peeeq.wurstscript.ast.ExprVarArrayAccess term) { return new de.peeeq.wurstscript.ast.ExprVarArrayAccessPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.VarRefPos CaseExprVarAccess(de.peeeq.wurstscript.ast.ExprVarAccess term) { return new de.peeeq.wurstscript.ast.ExprVarAccessPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.VarRefPos CaseExprMemberVar(de.peeeq.wurstscript.ast.ExprMemberVar term) { return new de.peeeq.wurstscript.ast.ExprMemberVarPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.VarRefPos CaseExprMemberArrayVar(de.peeeq.wurstscript.ast.ExprMemberArrayVar term) { return new de.peeeq.wurstscript.ast.ExprMemberArrayVarPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.wurstscript.ast.WEntities WEntities(de.peeeq.wurstscript.ast.WEntity... elements) {
        return _termcache.put(new de.peeeq.wurstscript.ast.WEntities.Impl(elements));
    }

    static final de.peeeq.wurstscript.ast.WEntitiesPos WEntitiesPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.WEntities term, final int pos) {
        return new de.peeeq.wurstscript.ast.WEntitiesPos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.WEntityPos WEntityPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.WEntity term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.WEntity.Switch<de.peeeq.wurstscript.ast.WEntityPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.WEntityPos CaseFuncDef(de.peeeq.wurstscript.ast.FuncDef term) { return new de.peeeq.wurstscript.ast.FuncDefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WEntityPos CaseGlobalVarDef(de.peeeq.wurstscript.ast.GlobalVarDef term) { return new de.peeeq.wurstscript.ast.GlobalVarDefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WEntityPos CaseInitBlock(de.peeeq.wurstscript.ast.InitBlock term) { return new de.peeeq.wurstscript.ast.InitBlockPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WEntityPos CaseNativeFunc(de.peeeq.wurstscript.ast.NativeFunc term) { return new de.peeeq.wurstscript.ast.NativeFuncPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WEntityPos CaseNativeType(de.peeeq.wurstscript.ast.NativeType term) { return new de.peeeq.wurstscript.ast.NativeTypePos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WEntityPos CaseClassDef(de.peeeq.wurstscript.ast.ClassDef term) { return new de.peeeq.wurstscript.ast.ClassDefPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.wurstscript.ast.WImport WImport(de.peeeq.wurstscript.ast.WPos source, java.lang.String packagename) {
        return _termcache.put(new de.peeeq.wurstscript.ast.WImport.Impl(source, packagename));
    }

    static final de.peeeq.wurstscript.ast.WImportPos WImportPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.WImport term, final int pos) {
        return new de.peeeq.wurstscript.ast.WImportPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.WImports WImports(de.peeeq.wurstscript.ast.WImport... elements) {
        return _termcache.put(new de.peeeq.wurstscript.ast.WImports.Impl(elements));
    }

    static final de.peeeq.wurstscript.ast.WImportsPos WImportsPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.WImports term, final int pos) {
        return new de.peeeq.wurstscript.ast.WImportsPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.WPackage WPackage(de.peeeq.wurstscript.ast.WPos source, java.lang.String name, de.peeeq.wurstscript.ast.WImports imports, de.peeeq.wurstscript.ast.WEntities elements) {
        return _termcache.put(new de.peeeq.wurstscript.ast.WPackage.Impl(source, name, imports, elements));
    }

    public static final de.peeeq.wurstscript.ast.WPackage WPackage(de.peeeq.wurstscript.ast.WPos source, java.lang.String name, de.peeeq.wurstscript.ast.WImports imports, de.peeeq.wurstscript.ast.WEntity... elements) {
        de.peeeq.wurstscript.ast.WEntities _list = de.peeeq.wurstscript.ast.AST.WEntities(elements);
        return _termcache.put(new de.peeeq.wurstscript.ast.WPackage.Impl(source, name, imports, _list));
    }

    static final de.peeeq.wurstscript.ast.WPackagePos WPackagePos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.WPackage term, final int pos) {
        return new de.peeeq.wurstscript.ast.WPackagePos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.WParameter WParameter(de.peeeq.wurstscript.ast.WPos source, de.peeeq.wurstscript.ast.TypeExpr typ, java.lang.String name) {
        return _termcache.put(new de.peeeq.wurstscript.ast.WParameter.Impl(source, typ, name));
    }

    static final de.peeeq.wurstscript.ast.WParameterPos WParameterPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.WParameter term, final int pos) {
        return new de.peeeq.wurstscript.ast.WParameterPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.WParameters WParameters(de.peeeq.wurstscript.ast.WParameter... elements) {
        return _termcache.put(new de.peeeq.wurstscript.ast.WParameters.Impl(elements));
    }

    static final de.peeeq.wurstscript.ast.WParametersPos WParametersPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.WParameters term, final int pos) {
        return new de.peeeq.wurstscript.ast.WParametersPos.Impl(parent, term, pos);
    }

    public static final de.peeeq.wurstscript.ast.WPos WPos(java.lang.String file, java.lang.Integer line, java.lang.Integer column) {
        return _termcache.put(new de.peeeq.wurstscript.ast.WPos.Impl(file, line, column));
    }

    static final de.peeeq.wurstscript.ast.WPosPos WPosPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.WPos term, final int pos) {
        return new de.peeeq.wurstscript.ast.WPosPos.Impl(parent, term, pos);
    }

    static final de.peeeq.wurstscript.ast.WScopePos WScopePos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.WScope term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.WScope.Switch<de.peeeq.wurstscript.ast.WScopePos, RuntimeException>() { public final de.peeeq.wurstscript.ast.WScopePos CaseClassDef(de.peeeq.wurstscript.ast.ClassDef term) { return new de.peeeq.wurstscript.ast.ClassDefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WScopePos CaseFuncDef(de.peeeq.wurstscript.ast.FuncDef term) { return new de.peeeq.wurstscript.ast.FuncDefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WScopePos CaseWPackage(de.peeeq.wurstscript.ast.WPackage term) { return new de.peeeq.wurstscript.ast.WPackagePos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WScopePos CaseCompilationUnit(de.peeeq.wurstscript.ast.CompilationUnit term) { return new de.peeeq.wurstscript.ast.CompilationUnitPos.Impl(parent, term, pos); } });
    }

    static final de.peeeq.wurstscript.ast.WStatementPos WStatementPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.WStatement term, final int pos) {
        return term.Switch(new de.peeeq.wurstscript.ast.WStatement.Switch<de.peeeq.wurstscript.ast.WStatementPos, RuntimeException>() { public final de.peeeq.wurstscript.ast.WStatementPos CaseStmtIf(de.peeeq.wurstscript.ast.StmtIf term) { return new de.peeeq.wurstscript.ast.StmtIfPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WStatementPos CaseStmtWhile(de.peeeq.wurstscript.ast.StmtWhile term) { return new de.peeeq.wurstscript.ast.StmtWhilePos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WStatementPos CaseStmtLoop(de.peeeq.wurstscript.ast.StmtLoop term) { return new de.peeeq.wurstscript.ast.StmtLoopPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WStatementPos CaseLocalVarDef(de.peeeq.wurstscript.ast.LocalVarDef term) { return new de.peeeq.wurstscript.ast.LocalVarDefPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WStatementPos CaseStmtSet(de.peeeq.wurstscript.ast.StmtSet term) { return new de.peeeq.wurstscript.ast.StmtSetPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WStatementPos CaseStmtReturn(de.peeeq.wurstscript.ast.StmtReturn term) { return new de.peeeq.wurstscript.ast.StmtReturnPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WStatementPos CaseStmtDestroy(de.peeeq.wurstscript.ast.StmtDestroy term) { return new de.peeeq.wurstscript.ast.StmtDestroyPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WStatementPos CaseStmtIncRefCount(de.peeeq.wurstscript.ast.StmtIncRefCount term) { return new de.peeeq.wurstscript.ast.StmtIncRefCountPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WStatementPos CaseStmtDecRefCount(de.peeeq.wurstscript.ast.StmtDecRefCount term) { return new de.peeeq.wurstscript.ast.StmtDecRefCountPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WStatementPos CaseStmtErr(de.peeeq.wurstscript.ast.StmtErr term) { return new de.peeeq.wurstscript.ast.StmtErrPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WStatementPos CaseStmtExitwhen(de.peeeq.wurstscript.ast.StmtExitwhen term) { return new de.peeeq.wurstscript.ast.StmtExitwhenPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WStatementPos CaseExprMemberMethod(de.peeeq.wurstscript.ast.ExprMemberMethod term) { return new de.peeeq.wurstscript.ast.ExprMemberMethodPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WStatementPos CaseExprFunctionCall(de.peeeq.wurstscript.ast.ExprFunctionCall term) { return new de.peeeq.wurstscript.ast.ExprFunctionCallPos.Impl(parent, term, pos); } public final de.peeeq.wurstscript.ast.WStatementPos CaseExprNewObject(de.peeeq.wurstscript.ast.ExprNewObject term) { return new de.peeeq.wurstscript.ast.ExprNewObjectPos.Impl(parent, term, pos); } });
    }

    public static final de.peeeq.wurstscript.ast.WStatements WStatements(de.peeeq.wurstscript.ast.WStatement... elements) {
        return _termcache.put(new de.peeeq.wurstscript.ast.WStatements.Impl(elements));
    }

    static final de.peeeq.wurstscript.ast.WStatementsPos WStatementsPos(final KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, ?> parent, final de.peeeq.wurstscript.ast.WStatements term, final int pos) {
        return new de.peeeq.wurstscript.ast.WStatementsPos.Impl(parent, term, pos);
    }

    public static final KatjaElement fromAssembler(Reader reader) throws IOException {
        return parseInternal(reader, _AST$Creator);
    }

    //----- nested classes of AST -----

    public static interface SortPos extends KatjaSortPos<de.peeeq.wurstscript.ast.CompilationUnitPos> {

        //----- methods of SortPos -----

        public AST.SortPos parent();
        public AST.SortPos lsib();
        public AST.SortPos rsib();
        public AST.SortPos preOrder();
        public AST.SortPos preOrderSkip();
        public AST.SortPos postOrder();
        public AST.SortPos postOrderStart();
        public AST.SortPos follow(List<Integer> path);
    }

    public static interface TermPos<T> extends AST.SortPos, KatjaTermPos<de.peeeq.wurstscript.ast.CompilationUnitPos, T> {
    }

    public static interface NodePos<T extends KatjaTerm> extends AST.TermPos<T>, KatjaNodePos<de.peeeq.wurstscript.ast.CompilationUnitPos, T> {
    }

    public static interface LeafPos<T> extends AST.TermPos<T>, KatjaLeafPos<de.peeeq.wurstscript.ast.CompilationUnitPos, T> {
    }

    public static interface TuplePos<T extends KatjaTuple> extends AST.NodePos<T>, KatjaTuplePos<de.peeeq.wurstscript.ast.CompilationUnitPos, T> {
    }

    public static interface ListPos<T extends KatjaList<E>, L extends KatjaSortPos<de.peeeq.wurstscript.ast.CompilationUnitPos>, E> extends AST.NodePos<T>, KatjaListPos<de.peeeq.wurstscript.ast.CompilationUnitPos, T, L, E> {
    }

    private static final class AST$Creator implements KatjaSpecification.ElementCreator {

        //----- attributes of AST$Creator -----

        private TreeMap<String, Integer> _sortnumber = new TreeMap<String, Integer>();

        //----- methods of AST$Creator -----

        AST$Creator() {
            _sortnumber.put("ClassDef", 0);
            _sortnumber.put("ConstructorDef", 1);
            _sortnumber.put("ExprBinary", 2);
            _sortnumber.put("ExprBoolVal", 3);
            _sortnumber.put("ExprFuncRef", 4);
            _sortnumber.put("ExprFunctionCall", 5);
            _sortnumber.put("ExprIntVal", 6);
            _sortnumber.put("ExprMemberArrayVar", 7);
            _sortnumber.put("ExprMemberMethod", 8);
            _sortnumber.put("ExprMemberVar", 9);
            _sortnumber.put("ExprNewObject", 10);
            _sortnumber.put("ExprNull", 11);
            _sortnumber.put("ExprRealVal", 12);
            _sortnumber.put("ExprStringVal", 13);
            _sortnumber.put("ExprThis", 14);
            _sortnumber.put("ExprUnary", 15);
            _sortnumber.put("ExprVarAccess", 16);
            _sortnumber.put("ExprVarArrayAccess", 17);
            _sortnumber.put("FuncDef", 18);
            _sortnumber.put("FuncSignature", 19);
            _sortnumber.put("GlobalVarDef", 20);
            _sortnumber.put("InitBlock", 21);
            _sortnumber.put("LocalVarDef", 22);
            _sortnumber.put("NativeFunc", 23);
            _sortnumber.put("NativeType", 24);
            _sortnumber.put("NoExpr", 25);
            _sortnumber.put("NoTypeExpr", 26);
            _sortnumber.put("OnDestroyDef", 27);
            _sortnumber.put("OpAnd", 28);
            _sortnumber.put("OpAssign", 29);
            _sortnumber.put("OpDivInt", 30);
            _sortnumber.put("OpDivReal", 31);
            _sortnumber.put("OpEquals", 32);
            _sortnumber.put("OpGreater", 33);
            _sortnumber.put("OpGreaterEq", 34);
            _sortnumber.put("OpLess", 35);
            _sortnumber.put("OpLessEq", 36);
            _sortnumber.put("OpMinus", 37);
            _sortnumber.put("OpModInt", 38);
            _sortnumber.put("OpModReal", 39);
            _sortnumber.put("OpMult", 40);
            _sortnumber.put("OpNot", 41);
            _sortnumber.put("OpOr", 42);
            _sortnumber.put("OpPlus", 43);
            _sortnumber.put("OpUnequals", 44);
            _sortnumber.put("StmtDecRefCount", 45);
            _sortnumber.put("StmtDestroy", 46);
            _sortnumber.put("StmtErr", 47);
            _sortnumber.put("StmtExitwhen", 48);
            _sortnumber.put("StmtIf", 49);
            _sortnumber.put("StmtIncRefCount", 50);
            _sortnumber.put("StmtLoop", 51);
            _sortnumber.put("StmtReturn", 52);
            _sortnumber.put("StmtSet", 53);
            _sortnumber.put("StmtWhile", 54);
            _sortnumber.put("TypeExpr", 55);
            _sortnumber.put("WImport", 56);
            _sortnumber.put("WPackage", 57);
            _sortnumber.put("WParameter", 58);
            _sortnumber.put("WPos", 59);
            _sortnumber.put("Arguments", 60);
            _sortnumber.put("ArraySizes", 61);
            _sortnumber.put("ClassSlots", 62);
            _sortnumber.put("CompilationUnit", 63);
            _sortnumber.put("Indexes", 64);
            _sortnumber.put("JassGlobalBlock", 65);
            _sortnumber.put("WEntities", 66);
            _sortnumber.put("WImports", 67);
            _sortnumber.put("WParameters", 68);
            _sortnumber.put("WStatements", 69);
            _sortnumber.put("CompilationUnitPos", 70);
        }

        public final KatjaElement create(String name, Object[] parameters, int line) {
            if(!_sortnumber.containsKey(name))
                throw new IllegalArgumentException("unknown sort name "+name+", in line "+line);

            switch(_sortnumber.get(name)) {
                case 0:
                    if(parameters.length != 4)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ClassDef, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ClassDef constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ClassDef constructor, in line "+line);
                    if(!(parameters[2] instanceof java.lang.Boolean))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of ClassDef constructor, in line "+line);
                    if(!(parameters[3] instanceof de.peeeq.wurstscript.ast.ClassSlots))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 4 of ClassDef constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ClassDef((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.String) parameters[1], (java.lang.Boolean) parameters[2], (de.peeeq.wurstscript.ast.ClassSlots) parameters[3]);
                case 1:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ConstructorDef, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ConstructorDef constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.WParameters))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ConstructorDef constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.wurstscript.ast.WStatements))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of ConstructorDef constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ConstructorDef((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.WParameters) parameters[1], (de.peeeq.wurstscript.ast.WStatements) parameters[2]);
                case 2:
                    if(parameters.length != 4)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ExprBinary, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ExprBinary constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.Expr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ExprBinary constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.wurstscript.ast.OpBinary))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of ExprBinary constructor, in line "+line);
                    if(!(parameters[3] instanceof de.peeeq.wurstscript.ast.Expr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 4 of ExprBinary constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ExprBinary((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.Expr) parameters[1], (de.peeeq.wurstscript.ast.OpBinary) parameters[2], (de.peeeq.wurstscript.ast.Expr) parameters[3]);
                case 3:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ExprBoolVal, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ExprBoolVal constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.Boolean))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ExprBoolVal constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ExprBoolVal((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.Boolean) parameters[1]);
                case 4:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ExprFuncRef, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ExprFuncRef constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ExprFuncRef constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ExprFuncRef((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.String) parameters[1]);
                case 5:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ExprFunctionCall, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ExprFunctionCall constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ExprFunctionCall constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.wurstscript.ast.Arguments))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of ExprFunctionCall constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ExprFunctionCall((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.String) parameters[1], (de.peeeq.wurstscript.ast.Arguments) parameters[2]);
                case 6:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ExprIntVal, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ExprIntVal constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.Integer))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ExprIntVal constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ExprIntVal((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.Integer) parameters[1]);
                case 7:
                    if(parameters.length != 4)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ExprMemberArrayVar, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ExprMemberArrayVar constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.Expr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ExprMemberArrayVar constructor, in line "+line);
                    if(!(parameters[2] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of ExprMemberArrayVar constructor, in line "+line);
                    if(!(parameters[3] instanceof de.peeeq.wurstscript.ast.Indexes))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 4 of ExprMemberArrayVar constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ExprMemberArrayVar((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.Expr) parameters[1], (java.lang.String) parameters[2], (de.peeeq.wurstscript.ast.Indexes) parameters[3]);
                case 8:
                    if(parameters.length != 4)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ExprMemberMethod, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ExprMemberMethod constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.Expr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ExprMemberMethod constructor, in line "+line);
                    if(!(parameters[2] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of ExprMemberMethod constructor, in line "+line);
                    if(!(parameters[3] instanceof de.peeeq.wurstscript.ast.Arguments))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 4 of ExprMemberMethod constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ExprMemberMethod((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.Expr) parameters[1], (java.lang.String) parameters[2], (de.peeeq.wurstscript.ast.Arguments) parameters[3]);
                case 9:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ExprMemberVar, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ExprMemberVar constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.Expr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ExprMemberVar constructor, in line "+line);
                    if(!(parameters[2] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of ExprMemberVar constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ExprMemberVar((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.Expr) parameters[1], (java.lang.String) parameters[2]);
                case 10:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ExprNewObject, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ExprNewObject constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ExprNewObject constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.wurstscript.ast.Arguments))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of ExprNewObject constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ExprNewObject((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.String) parameters[1], (de.peeeq.wurstscript.ast.Arguments) parameters[2]);
                case 11:
                    if(parameters.length != 1)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ExprNull, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ExprNull constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ExprNull((de.peeeq.wurstscript.ast.WPos) parameters[0]);
                case 12:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ExprRealVal, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ExprRealVal constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.Double))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ExprRealVal constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ExprRealVal((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.Double) parameters[1]);
                case 13:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ExprStringVal, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ExprStringVal constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ExprStringVal constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ExprStringVal((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.String) parameters[1]);
                case 14:
                    if(parameters.length != 1)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ExprThis, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ExprThis constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ExprThis((de.peeeq.wurstscript.ast.WPos) parameters[0]);
                case 15:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ExprUnary, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ExprUnary constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.OpUnary))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ExprUnary constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.wurstscript.ast.Expr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of ExprUnary constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ExprUnary((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.OpUnary) parameters[1], (de.peeeq.wurstscript.ast.Expr) parameters[2]);
                case 16:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ExprVarAccess, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ExprVarAccess constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ExprVarAccess constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ExprVarAccess((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.String) parameters[1]);
                case 17:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort ExprVarArrayAccess, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of ExprVarArrayAccess constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of ExprVarArrayAccess constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.wurstscript.ast.Indexes))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of ExprVarArrayAccess constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.ExprVarArrayAccess((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.String) parameters[1], (de.peeeq.wurstscript.ast.Indexes) parameters[2]);
                case 18:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort FuncDef, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of FuncDef constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.FuncSignature))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of FuncDef constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.wurstscript.ast.WStatements))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of FuncDef constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.FuncDef((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.FuncSignature) parameters[1], (de.peeeq.wurstscript.ast.WStatements) parameters[2]);
                case 19:
                    if(parameters.length != 4)
                        throw new IllegalArgumentException("wrong number of arguments given for sort FuncSignature, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of FuncSignature constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of FuncSignature constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.wurstscript.ast.WParameters))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of FuncSignature constructor, in line "+line);
                    if(!(parameters[3] instanceof de.peeeq.wurstscript.ast.OptTypeExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 4 of FuncSignature constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.FuncSignature((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.String) parameters[1], (de.peeeq.wurstscript.ast.WParameters) parameters[2], (de.peeeq.wurstscript.ast.OptTypeExpr) parameters[3]);
                case 20:
                    if(parameters.length != 5)
                        throw new IllegalArgumentException("wrong number of arguments given for sort GlobalVarDef, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of GlobalVarDef constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.Boolean))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of GlobalVarDef constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.wurstscript.ast.OptTypeExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of GlobalVarDef constructor, in line "+line);
                    if(!(parameters[3] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 4 of GlobalVarDef constructor, in line "+line);
                    if(!(parameters[4] instanceof de.peeeq.wurstscript.ast.OptExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 5 of GlobalVarDef constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.GlobalVarDef((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.Boolean) parameters[1], (de.peeeq.wurstscript.ast.OptTypeExpr) parameters[2], (java.lang.String) parameters[3], (de.peeeq.wurstscript.ast.OptExpr) parameters[4]);
                case 21:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort InitBlock, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of InitBlock constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.WStatements))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of InitBlock constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.InitBlock((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.WStatements) parameters[1]);
                case 22:
                    if(parameters.length != 5)
                        throw new IllegalArgumentException("wrong number of arguments given for sort LocalVarDef, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of LocalVarDef constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.Boolean))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of LocalVarDef constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.wurstscript.ast.OptTypeExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of LocalVarDef constructor, in line "+line);
                    if(!(parameters[3] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 4 of LocalVarDef constructor, in line "+line);
                    if(!(parameters[4] instanceof de.peeeq.wurstscript.ast.OptExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 5 of LocalVarDef constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.LocalVarDef((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.Boolean) parameters[1], (de.peeeq.wurstscript.ast.OptTypeExpr) parameters[2], (java.lang.String) parameters[3], (de.peeeq.wurstscript.ast.OptExpr) parameters[4]);
                case 23:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort NativeFunc, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of NativeFunc constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.FuncSignature))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of NativeFunc constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.NativeFunc((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.FuncSignature) parameters[1]);
                case 24:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort NativeType, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of NativeType constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of NativeType constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.wurstscript.ast.OptTypeExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of NativeType constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.NativeType((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.String) parameters[1], (de.peeeq.wurstscript.ast.OptTypeExpr) parameters[2]);
                case 25:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort NoExpr, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.NoExpr();
                case 26:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort NoTypeExpr, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.NoTypeExpr();
                case 27:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OnDestroyDef, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of OnDestroyDef constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.WStatements))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of OnDestroyDef constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.OnDestroyDef((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.WStatements) parameters[1]);
                case 28:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpAnd, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpAnd();
                case 29:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpAssign, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpAssign();
                case 30:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpDivInt, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpDivInt();
                case 31:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpDivReal, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpDivReal();
                case 32:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpEquals, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpEquals();
                case 33:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpGreater, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpGreater();
                case 34:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpGreaterEq, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpGreaterEq();
                case 35:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpLess, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpLess();
                case 36:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpLessEq, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpLessEq();
                case 37:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpMinus, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpMinus();
                case 38:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpModInt, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpModInt();
                case 39:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpModReal, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpModReal();
                case 40:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpMult, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpMult();
                case 41:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpNot, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpNot();
                case 42:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpOr, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpOr();
                case 43:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpPlus, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpPlus();
                case 44:
                    if(parameters.length != 0)
                        throw new IllegalArgumentException("wrong number of arguments given for sort OpUnequals, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.OpUnequals();
                case 45:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort StmtDecRefCount, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of StmtDecRefCount constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.Expr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of StmtDecRefCount constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.StmtDecRefCount((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.Expr) parameters[1]);
                case 46:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort StmtDestroy, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of StmtDestroy constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.Expr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of StmtDestroy constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.StmtDestroy((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.Expr) parameters[1]);
                case 47:
                    if(parameters.length != 1)
                        throw new IllegalArgumentException("wrong number of arguments given for sort StmtErr, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of StmtErr constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.StmtErr((de.peeeq.wurstscript.ast.WPos) parameters[0]);
                case 48:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort StmtExitwhen, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of StmtExitwhen constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.Expr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of StmtExitwhen constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.StmtExitwhen((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.Expr) parameters[1]);
                case 49:
                    if(parameters.length != 4)
                        throw new IllegalArgumentException("wrong number of arguments given for sort StmtIf, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of StmtIf constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.Expr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of StmtIf constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.wurstscript.ast.WStatements))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of StmtIf constructor, in line "+line);
                    if(!(parameters[3] instanceof de.peeeq.wurstscript.ast.WStatements))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 4 of StmtIf constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.StmtIf((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.Expr) parameters[1], (de.peeeq.wurstscript.ast.WStatements) parameters[2], (de.peeeq.wurstscript.ast.WStatements) parameters[3]);
                case 50:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort StmtIncRefCount, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of StmtIncRefCount constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.Expr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of StmtIncRefCount constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.StmtIncRefCount((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.Expr) parameters[1]);
                case 51:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort StmtLoop, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of StmtLoop constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.WStatements))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of StmtLoop constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.StmtLoop((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.WStatements) parameters[1]);
                case 52:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort StmtReturn, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of StmtReturn constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.OptExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of StmtReturn constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.StmtReturn((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.OptExpr) parameters[1]);
                case 53:
                    if(parameters.length != 4)
                        throw new IllegalArgumentException("wrong number of arguments given for sort StmtSet, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of StmtSet constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.ExprAssignable))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of StmtSet constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.wurstscript.ast.OpAssignment))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of StmtSet constructor, in line "+line);
                    if(!(parameters[3] instanceof de.peeeq.wurstscript.ast.Expr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 4 of StmtSet constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.StmtSet((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.ExprAssignable) parameters[1], (de.peeeq.wurstscript.ast.OpAssignment) parameters[2], (de.peeeq.wurstscript.ast.Expr) parameters[3]);
                case 54:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort StmtWhile, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of StmtWhile constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.Expr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of StmtWhile constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.wurstscript.ast.WStatements))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of StmtWhile constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.StmtWhile((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.Expr) parameters[1], (de.peeeq.wurstscript.ast.WStatements) parameters[2]);
                case 55:
                    if(parameters.length != 4)
                        throw new IllegalArgumentException("wrong number of arguments given for sort TypeExpr, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of TypeExpr constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of TypeExpr constructor, in line "+line);
                    if(!(parameters[2] instanceof java.lang.Boolean))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of TypeExpr constructor, in line "+line);
                    if(!(parameters[3] instanceof de.peeeq.wurstscript.ast.ArraySizes))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 4 of TypeExpr constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.TypeExpr((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.String) parameters[1], (java.lang.Boolean) parameters[2], (de.peeeq.wurstscript.ast.ArraySizes) parameters[3]);
                case 56:
                    if(parameters.length != 2)
                        throw new IllegalArgumentException("wrong number of arguments given for sort WImport, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of WImport constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of WImport constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.WImport((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.String) parameters[1]);
                case 57:
                    if(parameters.length != 4)
                        throw new IllegalArgumentException("wrong number of arguments given for sort WPackage, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of WPackage constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of WPackage constructor, in line "+line);
                    if(!(parameters[2] instanceof de.peeeq.wurstscript.ast.WImports))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of WPackage constructor, in line "+line);
                    if(!(parameters[3] instanceof de.peeeq.wurstscript.ast.WEntities))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 4 of WPackage constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.WPackage((de.peeeq.wurstscript.ast.WPos) parameters[0], (java.lang.String) parameters[1], (de.peeeq.wurstscript.ast.WImports) parameters[2], (de.peeeq.wurstscript.ast.WEntities) parameters[3]);
                case 58:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort WParameter, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.WPos))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of WParameter constructor, in line "+line);
                    if(!(parameters[1] instanceof de.peeeq.wurstscript.ast.TypeExpr))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of WParameter constructor, in line "+line);
                    if(!(parameters[2] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of WParameter constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.WParameter((de.peeeq.wurstscript.ast.WPos) parameters[0], (de.peeeq.wurstscript.ast.TypeExpr) parameters[1], (java.lang.String) parameters[2]);
                case 59:
                    if(parameters.length != 3)
                        throw new IllegalArgumentException("wrong number of arguments given for sort WPos, in line "+line);

                    if(!(parameters[0] instanceof java.lang.String))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 1 of WPos constructor, in line "+line);
                    if(!(parameters[1] instanceof java.lang.Integer))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 2 of WPos constructor, in line "+line);
                    if(!(parameters[2] instanceof java.lang.Integer))
                        throw new IllegalArgumentException("wrong sort on stack as parameter 3 of WPos constructor, in line "+line);
                    return de.peeeq.wurstscript.ast.AST.WPos((java.lang.String) parameters[0], (java.lang.Integer) parameters[1], (java.lang.Integer) parameters[2]);
                case 60:
                    de.peeeq.wurstscript.ast.Expr[] _Arguments$array = new de.peeeq.wurstscript.ast.Expr[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.wurstscript.ast.Expr))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for Arguments constructor, in line "+line);
                        _Arguments$array[i] = (de.peeeq.wurstscript.ast.Expr) parameters[i];
                    }

                    return de.peeeq.wurstscript.ast.AST.Arguments(_Arguments$array);
                case 61:
                    de.peeeq.wurstscript.ast.Expr[] _ArraySizes$array = new de.peeeq.wurstscript.ast.Expr[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.wurstscript.ast.Expr))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for ArraySizes constructor, in line "+line);
                        _ArraySizes$array[i] = (de.peeeq.wurstscript.ast.Expr) parameters[i];
                    }

                    return de.peeeq.wurstscript.ast.AST.ArraySizes(_ArraySizes$array);
                case 62:
                    de.peeeq.wurstscript.ast.ClassSlot[] _ClassSlots$array = new de.peeeq.wurstscript.ast.ClassSlot[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.wurstscript.ast.ClassSlot))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for ClassSlots constructor, in line "+line);
                        _ClassSlots$array[i] = (de.peeeq.wurstscript.ast.ClassSlot) parameters[i];
                    }

                    return de.peeeq.wurstscript.ast.AST.ClassSlots(_ClassSlots$array);
                case 63:
                    de.peeeq.wurstscript.ast.TopLevelDeclaration[] _CompilationUnit$array = new de.peeeq.wurstscript.ast.TopLevelDeclaration[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.wurstscript.ast.TopLevelDeclaration))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for CompilationUnit constructor, in line "+line);
                        _CompilationUnit$array[i] = (de.peeeq.wurstscript.ast.TopLevelDeclaration) parameters[i];
                    }

                    return de.peeeq.wurstscript.ast.AST.CompilationUnit(_CompilationUnit$array);
                case 64:
                    de.peeeq.wurstscript.ast.Expr[] _Indexes$array = new de.peeeq.wurstscript.ast.Expr[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.wurstscript.ast.Expr))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for Indexes constructor, in line "+line);
                        _Indexes$array[i] = (de.peeeq.wurstscript.ast.Expr) parameters[i];
                    }

                    return de.peeeq.wurstscript.ast.AST.Indexes(_Indexes$array);
                case 65:
                    de.peeeq.wurstscript.ast.GlobalVarDef[] _JassGlobalBlock$array = new de.peeeq.wurstscript.ast.GlobalVarDef[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.wurstscript.ast.GlobalVarDef))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for JassGlobalBlock constructor, in line "+line);
                        _JassGlobalBlock$array[i] = (de.peeeq.wurstscript.ast.GlobalVarDef) parameters[i];
                    }

                    return de.peeeq.wurstscript.ast.AST.JassGlobalBlock(_JassGlobalBlock$array);
                case 66:
                    de.peeeq.wurstscript.ast.WEntity[] _WEntities$array = new de.peeeq.wurstscript.ast.WEntity[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.wurstscript.ast.WEntity))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for WEntities constructor, in line "+line);
                        _WEntities$array[i] = (de.peeeq.wurstscript.ast.WEntity) parameters[i];
                    }

                    return de.peeeq.wurstscript.ast.AST.WEntities(_WEntities$array);
                case 67:
                    de.peeeq.wurstscript.ast.WImport[] _WImports$array = new de.peeeq.wurstscript.ast.WImport[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.wurstscript.ast.WImport))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for WImports constructor, in line "+line);
                        _WImports$array[i] = (de.peeeq.wurstscript.ast.WImport) parameters[i];
                    }

                    return de.peeeq.wurstscript.ast.AST.WImports(_WImports$array);
                case 68:
                    de.peeeq.wurstscript.ast.WParameter[] _WParameters$array = new de.peeeq.wurstscript.ast.WParameter[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.wurstscript.ast.WParameter))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for WParameters constructor, in line "+line);
                        _WParameters$array[i] = (de.peeeq.wurstscript.ast.WParameter) parameters[i];
                    }

                    return de.peeeq.wurstscript.ast.AST.WParameters(_WParameters$array);
                case 69:
                    de.peeeq.wurstscript.ast.WStatement[] _WStatements$array = new de.peeeq.wurstscript.ast.WStatement[parameters.length];

                    for(int i = 0; i < parameters.length; i++) {
                        if(!(parameters[i] instanceof de.peeeq.wurstscript.ast.WStatement))
                            throw new IllegalArgumentException("wrong sort on stack as parameter "+i+" for WStatements constructor, in line "+line);
                        _WStatements$array[i] = (de.peeeq.wurstscript.ast.WStatement) parameters[i];
                    }

                    return de.peeeq.wurstscript.ast.AST.WStatements(_WStatements$array);
                case 70:
                    if(parameters.length != 1)
                        throw new IllegalArgumentException("wrong number of arguments given root position CompilationUnitPos, in line "+line);

                    if(!(parameters[0] instanceof de.peeeq.wurstscript.ast.CompilationUnit))
                        throw new IllegalArgumentException("wrong sort on stack as parameter for CompilationUnitPos constructor, in line "+line);

                    return de.peeeq.wurstscript.ast.AST.CompilationUnitPos((de.peeeq.wurstscript.ast.CompilationUnit) parameters[0]);
                default:
                    throw new IllegalStateException("impossible state");
            }
        }
    }
}

