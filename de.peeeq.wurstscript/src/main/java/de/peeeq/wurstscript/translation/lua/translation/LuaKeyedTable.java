package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.luaAst.LuaAst;
import de.peeeq.wurstscript.luaAst.LuaFunction;

/**
 * Lua bodies for the {@code KeyedTable} package: a set keyed directly by its element.
 *
 * <p>Jass has no hashing, so every keyed structure in the library bottoms out in the hashtable
 * natives, which take a (parent, child) pair and are emitted on Lua as a nested table plus a nil
 * check. A membership test therefore costs a call and two indexes where Lua needs one, and the
 * element has to be squeezed through {@code castTo int} first. That is a Jass limitation carried
 * into a runtime which is already a hash table, which AGENTS.md section 7 asks us not to do.
 *
 * <p>On Lua each keyed table is its own table and the element is the key, so the four operations
 * become a single index each. {@code castTo int} is already the identity on Lua for class types
 * (see {@code rewriteTypeCastingCompatFunction}), and handles are identity-cached tables, so the
 * value arriving here is a usable key with reference identity either way.
 *
 * <p><b>Iteration is deliberately absent.</b> Enumerating a Lua table needs {@code pairs()}, whose
 * order depends on internal hash layout and therefore differs between clients - which desyncs a
 * lockstep game. Anything that must be iterated needs a separately maintained insertion-ordered
 * array, which is what SparseSet's dense half provides; this primitive is membership only.
 *
 * <p>The Jass path is the ordinary Wurst body of these functions and is left alone: correctness
 * matters there, performance does not.
 */
final class LuaKeyedTable {

    /** Package whose functions get the native Lua bodies below. */
    private static final String PACKAGE = "KeyedTable";

    static final String CREATE = "keyedTableCreate";
    static final String ADD = "keyedTableAdd";
    static final String CONTAINS = "keyedTableContains";
    static final String REMOVE = "keyedTableRemove";

    private LuaKeyedTable() {
    }

    /** The {@code KeyedTable} function {@code f} implements, or null if it is not one. */
    static String operationOf(ImFunction f) {
        if (!(f.attrTrace() instanceof FuncDef fd)) {
            return null;
        }
        if (!(fd.attrNearestPackage() instanceof WPackage p) || !PACKAGE.equals(p.getName())) {
            return null;
        }
        String name = fd.getName();
        return CREATE.equals(name) || ADD.equals(name) || CONTAINS.equals(name) || REMOVE.equals(name)
            ? name
            : null;
    }

    /**
     * Replaces the body of a {@code KeyedTable} function with the Lua-native form.
     *
     * @return whether a body was written; false leaves the ordinary translation in place, so a
     *     signature this does not recognise keeps working rather than silently emitting nothing.
     */
    static boolean rewrite(ImFunction f, LuaFunction lf, LuaTranslator tr) {
        String op = operationOf(f);
        if (op == null) {
            return false;
        }
        if (CREATE.equals(op)) {
            if (!f.getParameters().isEmpty()) {
                return false;
            }
            lf.getBody().clear();
            lf.getBody().add(LuaAst.LuaLiteral("return {}"));
            return true;
        }
        if (f.getParameters().size() != 2) {
            return false;
        }
        String table = luaNameOf(f.getParameters().get(0), tr);
        String key = luaNameOf(f.getParameters().get(1), tr);
        lf.getBody().clear();
        switch (op) {
            case ADD -> lf.getBody().add(LuaAst.LuaLiteral(table + "[" + key + "] = true"));
            case REMOVE -> lf.getBody().add(LuaAst.LuaLiteral(table + "[" + key + "] = nil"));
            case CONTAINS -> lf.getBody().add(LuaAst.LuaLiteral("return " + table + "[" + key + "] ~= nil"));
            default -> throw new IllegalStateException("unhandled KeyedTable operation " + op);
        }
        return true;
    }

    private static String luaNameOf(ImVar param, LuaTranslator tr) {
        return tr.luaVar.getFor(param).getName();
    }
}
