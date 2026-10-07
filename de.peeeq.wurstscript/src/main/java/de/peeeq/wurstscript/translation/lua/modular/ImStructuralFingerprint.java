package de.peeeq.wurstscript.translation.lua.modular;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlag;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

/**
 * Stable structural fingerprint of a package's lowered IM partition.
 *
 * <p>Why this exists: the chunk cache must invalidate a consumer when an <i>implementation</i>
 * change upstream alters what the consumer lowers to, even though the upstream public ABI is
 * untouched. Examples: a {@code compiletime()} constant folded from an imported function body,
 * or tuple field indices shifting after a field reorder. Hashing transitive <i>sources</i> also
 * catches those, but at the price of re-emitting every transitive importer on any body-only
 * edit (no selective invalidation). Hashing the lowered result is precise: if this fingerprint
 * is unchanged, re-emission would print the same Lua.
 *
 * <p>Rules the implementation follows:
 * <ul>
 *   <li>Only containment children ( {@link Element#size()}/{@link Element#get(int)}) are
 *       traversed. References ({@code getFunc}, {@code getVar}, traces, parents) are hashed
 *       <b>by name</b>, never followed: following them would inline foreign packages'
 *       definitions and make every fingerprint as coarse as a transitive source hash. A callee
 *       whose body changed re-emits under its own package; callers reuse their chunk precisely
 *       when their own lowered code is unaffected.</li>
 *   <li>Never call {@code toString()} on model elements: {@code ImPrinter} disambiguates
 *       variables with identity hash codes, which differ between compilations and would make
 *       every fingerprint unique.</li>
 *   <li>State that lives outside the child list (names, literal values, operators, flags,
 *       dispatch aliases, tuple indices, …) is hashed explicitly per node type. Missing one
 *       would be a soundness hole, so the inventory below mirrors every scalar getter on the
 *       generated {@code jassIm} classes.</li>
 *   <li>Global numbering (class {@code typeId}s) is <i>not</i> covered here: identical lowered
 *       code can still carry stale integer literals after the class inventory changes. That is
 *       what {@link PackageChunkCache#computeProgramShapeFingerprint} is for.</li>
 * </ul>
 */
public final class ImStructuralFingerprint {

    private ImStructuralFingerprint() {
    }

    public static String fingerprint(
        List<ImFunction> funcs,
        List<ImVar> globals,
        List<ImClass> classes
    ) {
        Hasher h = Hashing.sha256().newHasher();
        funcs.stream().sorted(Comparator.comparing(ImFunction::getName)).forEach(f -> hashFunction(h, f));
        globals.stream().sorted(Comparator.comparing(ImVar::getName)).forEach(v -> hashVar(h, v));
        classes.stream().sorted(Comparator.comparing(ImClass::getName)).forEach(c -> hashClass(h, c));
        return h.hash().toString().substring(0, 16);
    }

    private static void str(Hasher h, String s) {
        h.putString(s == null ? "" : s, StandardCharsets.UTF_8);
        h.putByte((byte) 0);
    }

    private static void hashFunction(Hasher h, ImFunction f) {
        str(h, "fun:" + f.getName());
        h.putBoolean(f.isNative());
        h.putBoolean(f.isBj());
        h.putBoolean(f.isExtern());
        h.putBoolean(f.isCompiletime());
        try {
            for (FunctionFlag flag : f.getFlags()) {
                str(h, "flag:" + flag.getClass().getSimpleName());
            }
        } catch (Exception ignored) {
        }
        hashType(h, f.getReturnType());
        hashChildren(h, f);
    }

    private static void hashVar(Hasher h, ImVar v) {
        str(h, "var:" + v.getName());
        h.putBoolean(v.getIsBJ());
        h.putBoolean(v.isGlobal());
        hashType(h, v.getType());
        hashChildren(h, v);
    }

    private static void hashClass(Hasher h, ImClass c) {
        str(h, "class:" + c.getName());
        try {
            for (ImClassType sup : c.getSuperClasses()) {
                str(h, "super:" + classDefName(sup));
            }
        } catch (Exception ignored) {
        }
        hashChildren(h, c);
    }

    private static String classDefName(ImClassType t) {
        try {
            ImClass def = t.getClassDef();
            if (def != null) {
                return def.getName();
            }
        } catch (Exception ignored) {
        }
        return "?";
    }

    private static void hashType(Hasher h, ImType t) {
        if (t == null) {
            str(h, "type:null");
            return;
        }
        if (t instanceof ImSimpleType) {
            str(h, "simple:" + ((ImSimpleType) t).getTypename());
            return;
        }
        if (t instanceof ImClassType) {
            str(h, "classtype:" + classDefName((ImClassType) t));
            hashChildren(h, t);
            return;
        }
        if (t instanceof ImTypeVar) {
            str(h, "typevar:" + ((ImTypeVar) t).getName());
            return;
        }
        if (t instanceof ImTypeClassFunc) {
            str(h, "typeclassfunc:" + ((ImTypeClassFunc) t).getName());
            return;
        }
        str(h, "type:" + t.getClass().getSimpleName());
        hashChildren(h, t);
    }

    private static void hashChildren(Hasher h, Element e) {
        int n;
        try {
            n = e.size();
        } catch (Exception ex) {
            return;
        }
        h.putInt(n);
        for (int i = 0; i < n; i++) {
            hashElement(h, e.get(i));
        }
    }

    static void hashElement(Hasher h, Element e) {
        if (e == null) {
            str(h, "null");
            return;
        }
        str(h, e.getClass().getSimpleName());
        if (e instanceof ImMethod) {
            ImMethod m = (ImMethod) e;
            str(h, "method:" + m.getName());
            h.putBoolean(m.getIsAbstract());
            try {
                str(h, "group:" + m.getLuaDispatchGroupKey());
                List<String> aliases = m.getLuaMethodDispatchAliases();
                if (aliases != null) {
                    for (String a : aliases) {
                        str(h, "alias:" + a);
                    }
                }
            } catch (Exception ignored) {
            }
            try {
                ImClassType owner = m.getMethodClass();
                if (owner != null) {
                    str(h, "owner:" + classDefName(owner));
                }
            } catch (Exception ignored) {
            }
            // Implementations live in the function partition; sub-method references are covered
            // by name through the alias computation above. No descent.
            return;
        }
        if (e instanceof ImFunction) {
            hashFunction(h, (ImFunction) e);
            return;
        }
        if (e instanceof ImVar) {
            hashVar(h, (ImVar) e);
            return;
        }
        if (e instanceof ImClass) {
            hashClass(h, (ImClass) e);
            return;
        }
        if (e instanceof ImType) {
            hashType(h, (ImType) e);
            return;
        }
        if (e instanceof ImIntVal) {
            h.putInt(((ImIntVal) e).getValI());
        } else if (e instanceof ImRealVal) {
            str(h, ((ImRealVal) e).getValR());
        } else if (e instanceof ImStringVal) {
            str(h, ((ImStringVal) e).getValS());
        } else if (e instanceof ImBoolVal) {
            h.putBoolean(((ImBoolVal) e).getValB());
        } else if (e instanceof ImOperatorCall) {
            try {
                str(h, "op:" + ((ImOperatorCall) e).getOp().name());
            } catch (Exception ignored) {
                str(h, "op:?");
            }
        } else if (e instanceof ImFunctionCall) {
            ImFunction target;
            try {
                target = ((ImFunctionCall) e).getFunc();
            } catch (Exception ex) {
                target = null;
            }
            str(h, "call:" + (target == null ? "?" : target.getName()));
            try {
                str(h, "calltype:" + ((ImFunctionCall) e).getCallType().name());
            } catch (Exception ignored) {
                str(h, "calltype:?");
            }
        } else if (e instanceof ImMethodCall) {
            ImMethod target;
            try {
                target = ((ImMethodCall) e).getMethod();
            } catch (Exception ex) {
                target = null;
            }
            str(h, "mcall:" + (target == null ? "?" : target.getName()));
            try {
                h.putBoolean(((ImMethodCall) e).getTuplesEliminated());
            } catch (Exception ignored) {
            }
        } else if (e instanceof ImVarAccess) {
            ImVar target;
            try {
                target = ((ImVarAccess) e).getVar();
            } catch (Exception ex) {
                target = null;
            }
            str(h, "varref:" + (target == null ? "?" : target.getName()));
        } else if (e instanceof ImMemberAccess) {
            ImVar target;
            try {
                target = ((ImMemberAccess) e).getVar();
            } catch (Exception ex) {
                target = null;
            }
            str(h, "fieldref:" + (target == null ? "?" : target.getName()));
            try {
                h.putBoolean(((ImMemberAccess) e).isUsedAsLValue());
            } catch (Exception ignored) {
            }
        } else if (e instanceof ImVarArrayAccess) {
            ImVar target;
            try {
                target = ((ImVarArrayAccess) e).getVar();
            } catch (Exception ex) {
                target = null;
            }
            str(h, "arrayref:" + (target == null ? "?" : target.getName()));
            try {
                h.putBoolean(((ImVarArrayAccess) e).isUsedAsLValue());
            } catch (Exception ignored) {
            }
        } else if (e instanceof ImTupleSelection) {
            try {
                h.putInt(((ImTupleSelection) e).getTupleIndex());
                h.putBoolean(((ImTupleSelection) e).isUsedAsLValue());
            } catch (Exception ignored) {
            }
        } else if (e instanceof ImStatementExpr) {
            try {
                h.putBoolean(((ImStatementExpr) e).isUsedAsLValue());
            } catch (Exception ignored) {
            }
        } else if (e instanceof ImTupleExpr) {
            try {
                h.putBoolean(((ImTupleExpr) e).isUsedAsLValue());
            } catch (Exception ignored) {
            }
        } else if (e instanceof ImTypeIdOfClass) {
            ImClassType clazz;
            try {
                clazz = ((ImTypeIdOfClass) e).getClazz();
            } catch (Exception ex) {
                clazz = null;
            }
            str(h, "typeof:" + (clazz == null ? "?" : classDefName(clazz)));
        } else if (e instanceof ImCompiletimeExpr) {
            try {
                h.putInt(((ImCompiletimeExpr) e).getExecutionOrderIndex());
            } catch (Exception ignored) {
            }
        }
        hashChildren(h, e);
    }
}
