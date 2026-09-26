package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBoundTypeParam;
import de.peeeq.wurstscript.types.WurstTypeInt;
import de.peeeq.wurstscript.types.WurstTypeReal;
import de.peeeq.wurstscript.types.WurstTypeString;
import de.peeeq.wurstscript.types.WurstTypeTypeParam;

import java.util.*;

/**
 * Decides whether a primitive-typed IM value can be nil on Lua.
 *
 * <p>Erased generic storage has no typed default: an untouched slot reads as nil where Jass reads
 * 0, 0., "" or false. The front end therefore wraps every erased value which reaches a concrete
 * primitive context in {@code __wurst_ensureInt} and friends. Specialisation copies a generic
 * declaration with a concrete type, but it does not change where the copy's values come from: a
 * specialised function declared to return {@code integer} can still return what an erased call
 * gave it. So a declaration alone never proves that a value is typed; its provenance does.
 *
 * <p>A value is typed when every source it can have is typed:
 * <ul>
 * <li>a literal;</li>
 * <li>a read of a primitive array slot, which the array's metatable defaults;</li>
 * <li>a variable or field whose source declaration is already the primitive (the front end
 *     normalised every write to it) or whose every write in the program is a typed value;</li>
 * <li>a call whose callee is a native, declares the primitive in source, or returns a typed
 *     value on every path; a method call when that holds for every implementation.</li>
 * </ul>
 * Anything else, including a parameter of a specialised copy, is treated as possibly nil.
 *
 * <p>Must run before inlining and the optimizer, while writes are still the assignments the
 * translation produced.
 */
final class LuaTypedValues {

    private final Map<ImVar, List<ImExpr>> writes = new IdentityHashMap<>();
    private final Map<ImVar, Boolean> varTyped = new IdentityHashMap<>();
    private final Map<ImFunction, Boolean> funcTyped = new IdentityHashMap<>();
    private final Set<ImVar> varsInProgress = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<ImFunction> funcsInProgress = Collections.newSetFromMap(new IdentityHashMap<>());

    LuaTypedValues(ImProg prog) {
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImSet set) {
                super.visit(set);
                ImVar target = writtenVar(set.getLeft());
                if (target != null) {
                    writes.computeIfAbsent(target, v -> new ArrayList<>()).add(set.getRight());
                }
            }

            @Override
            public void visit(ImVarargLoopVar loopVar) {
                super.visit(loopVar);
                // Bound from the call's varargs, which this analysis does not follow.
                writes.computeIfAbsent(loopVar.getVar(), v -> new ArrayList<>()).add(null);
            }
        });
    }

    /** The variable or field an assignment stores into as a whole, or null for an array slot. */
    private static ImVar writtenVar(ImLExpr left) {
        if (left instanceof ImVarAccess access) {
            return access.getVar();
        }
        if (left instanceof ImMemberAccess access && access.getIndexes().isEmpty()) {
            return access.getVar();
        }
        return null;
    }

    /** Whether {@code value}, of primitive type {@code type}, is never nil at runtime. */
    boolean isTyped(ImExpr value, ImType type) {
        if (!isPrimitive(type)) {
            return false;
        }
        if (value instanceof ImIntVal || value instanceof ImRealVal || value instanceof ImStringVal) {
            return true;
        }
        if (value instanceof ImStatementExpr statementExpr) {
            return isTyped(statementExpr.getExpr(), type);
        }
        if (value instanceof ImVarArrayAccess access) {
            return isSlotOf(access.getVar().getType(), access.getIndexes().size(), type);
        }
        if (value instanceof ImMemberAccess access) {
            if (!access.getIndexes().isEmpty()) {
                return isSlotOf(access.getVar().getType(), access.getIndexes().size(), type);
            }
            return isTypedVar(access.getVar(), type);
        }
        if (value instanceof ImVarAccess access) {
            return isTypedVar(access.getVar(), type);
        }
        if (value instanceof ImFunctionCall call) {
            return isTypedFunction(call.getFunc(), type);
        }
        if (value instanceof ImMethodCall call) {
            return isTypedMethod(call.getMethod(), type, Collections.newSetFromMap(new IdentityHashMap<>()));
        }
        return false;
    }

    /** A primitive array slot always reads its typed default through the array's metatable. */
    private static boolean isSlotOf(ImType arrayType, int indexCount, ImType type) {
        ImType entry = arrayType;
        for (int i = 0; i < indexCount; i++) {
            if (entry instanceof ImArrayType array) {
                entry = array.getEntryType();
            } else if (entry instanceof ImArrayTypeMulti array) {
                entry = array.getEntryType();
            } else {
                return false;
            }
        }
        return isSame(entry, type);
    }

    private boolean isTypedVar(ImVar var, ImType type) {
        if (!isSame(var.getType(), type)) {
            return false;
        }
        Boolean known = varTyped.get(var);
        if (known != null) {
            return known;
        }
        if (isDeclaredPrimitive(var)) {
            varTyped.put(var, true);
            return true;
        }
        if (isParameter(var) || !varsInProgress.add(var)) {
            return false;
        }
        boolean typed = true;
        for (ImExpr write : writes.getOrDefault(var, Collections.emptyList())) {
            if (write == null || !isTyped(write, type)) {
                typed = false;
                break;
            }
        }
        varsInProgress.remove(var);
        varTyped.put(var, typed);
        return typed;
    }

    private static boolean isParameter(ImVar var) {
        return var.getParent() instanceof ImVars vars
            && vars.getParent() instanceof ImFunction f
            && f.getParameters() == vars;
    }

    private boolean isTypedFunction(ImFunction f, ImType type) {
        if (!isSame(f.getReturnType(), type)) {
            return false;
        }
        Boolean known = funcTyped.get(f);
        if (known != null) {
            return known;
        }
        if (f.isNative() || isDeclaredPrimitive(f)) {
            funcTyped.put(f, true);
            return true;
        }
        if (!funcsInProgress.add(f)) {
            return false;
        }
        boolean[] typed = {true};
        f.getBody().accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImReturn ret) {
                super.visit(ret);
                if (!(ret.getReturnValue() instanceof ImExpr value) || !isTyped(value, type)) {
                    typed[0] = false;
                }
            }
        });
        funcsInProgress.remove(f);
        funcTyped.put(f, typed[0]);
        return typed[0];
    }

    private boolean isTypedMethod(ImMethod method, ImType type, Set<ImMethod> visited) {
        if (!visited.add(method)) {
            return true;
        }
        ImFunction implementation = method.getImplementation();
        if (implementation == null || !isTypedFunction(implementation, type)) {
            return false;
        }
        for (ImMethod sub : method.getSubMethods()) {
            if (!isTypedMethod(sub, type, visited)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether the source declaration behind the IM element names the primitive itself. Then the
     * type checker admitted only values of that type, and the front end normalised every erased
     * one. A specialised copy keeps the generic declaration as its trace and is not covered.
     */
    private static boolean isDeclaredPrimitive(ImVar var) {
        return var.getTrace() instanceof VarDef varDef && isPrimitiveDeclaration(varDef.attrTyp());
    }

    private static boolean isDeclaredPrimitive(ImFunction f) {
        return f.getTrace() instanceof FunctionDefinition funcDef && isPrimitiveDeclaration(funcDef.attrReturnTyp());
    }

    private static boolean isPrimitiveDeclaration(WurstType declared) {
        if (declared instanceof WurstTypeTypeParam || declared instanceof WurstTypeBoundTypeParam) {
            return false;
        }
        return declared instanceof WurstTypeInt || declared instanceof WurstTypeReal || declared instanceof WurstTypeString;
    }

    private static boolean isPrimitive(ImType type) {
        return TypesHelper.isIntType(type) || TypesHelper.isRealType(type) || TypesHelper.isStringType(type);
    }

    private static boolean isSame(ImType a, ImType b) {
        return (TypesHelper.isIntType(a) && TypesHelper.isIntType(b))
            || (TypesHelper.isRealType(a) && TypesHelper.isRealType(b))
            || (TypesHelper.isStringType(a) && TypesHelper.isStringType(b));
    }
}
