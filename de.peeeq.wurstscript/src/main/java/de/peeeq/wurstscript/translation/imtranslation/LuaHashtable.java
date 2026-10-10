package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.ImFunction;
import org.eclipse.jdt.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Jass hashtable natives on Lua. A hashtable is a table with one subtable per kind of value, as Jass keeps the
 * kinds apart under the same keys: {@code { __wurst_ht_int = {}, __wurst_ht_bool = {}, ... }}. A subtable maps a
 * parent key to a child table, which maps a child key to the value. Only InitHashtable creates one (a compile-time
 * hashtable is emitted as a call of it and its saves) and FlushParentHashtable gives it five new subtables, so every
 * subtable is always there.
 *
 * <p>{@link LuaNativeLowering} points every call of one of these natives at a stub {@code __wurst_<native>}, made once
 * per native and kept in {@link ImTranslator#luaHashtableStubs}. The optimizer and the backend recognise the stub by
 * identity: a user function of the same name is an ordinary function.
 */
public final class LuaHashtable {

    /** What an operation does, and the number of its arguments. */
    public enum Kind {
        INIT(0), LOAD(3), HAVE(3), SAVE(4), REMOVE(3), FLUSH_CHILD(2), FLUSH_PARENT(1);

        public final int arity;

        Kind(int arity) {
            this.arity = arity;
        }
    }

    /** The kinds of value, each kept in its own subtable. */
    public enum Values {
        INT("__wurst_ht_int"), BOOL("__wurst_ht_bool"), REAL("__wurst_ht_real"), STR("__wurst_ht_str"),
        HANDLE("__wurst_ht_handle");

        /** The field of the hashtable which holds the subtable. */
        public final String field;

        Values(String field) {
            this.field = field;
        }
    }

    /** An operation on the values of one kind, or on all of them ({@code values} is null). */
    public record Op(Kind kind, @Nullable Values values) {
    }

    /** The handle types with a Save and a Load native of their own; agent has only a Save. */
    private static final List<String> HANDLE_TYPES = List.of(
        "Player", "Widget", "Destructable", "Item", "Unit", "Ability", "Timer", "Trigger", "TriggerCondition",
        "TriggerAction", "TriggerEvent", "Force", "Group", "Location", "Rect", "BooleanExpr", "Sound", "Effect",
        "UnitPool", "ItemPool", "Quest", "QuestItem", "DefeatCondition", "TimerDialog", "Leaderboard", "Multiboard",
        "MultiboardItem", "Trackable", "Dialog", "Button", "TextTag", "Lightning", "Image", "Ubersplat", "Region",
        "FogState", "FogModifier", "Hashtable", "Frame");

    /** The operations by stub name. Looked up, never iterated. */
    private static final Map<String, Op> OPS = new HashMap<>();

    static {
        put("InitHashtable", Kind.INIT, null);
        put("FlushChildHashtable", Kind.FLUSH_CHILD, null);
        put("FlushParentHashtable", Kind.FLUSH_PARENT, null);
        family(Values.INT, "SaveInteger", "LoadInteger", "HaveSavedInteger", "RemoveSavedInteger");
        family(Values.BOOL, "SaveBoolean", "LoadBoolean", "HaveSavedBoolean", "RemoveSavedBoolean");
        family(Values.REAL, "SaveReal", "LoadReal", "HaveSavedReal", "RemoveSavedReal");
        family(Values.STR, "SaveStr", "LoadStr", "HaveSavedString", "RemoveSavedString");
        put("HaveSavedHandle", Kind.HAVE, Values.HANDLE);
        put("RemoveSavedHandle", Kind.REMOVE, Values.HANDLE);
        put("SaveAgentHandle", Kind.SAVE, Values.HANDLE);
        for (String type : HANDLE_TYPES) {
            put("Save" + type + "Handle", Kind.SAVE, Values.HANDLE);
            put("Load" + type + "Handle", Kind.LOAD, Values.HANDLE);
        }
    }

    private static void family(Values values, String save, String load, String have, String remove) {
        put(save, Kind.SAVE, values);
        put(load, Kind.LOAD, values);
        put(have, Kind.HAVE, values);
        put(remove, Kind.REMOVE, values);
    }

    private static void put(String nativeName, Kind kind, @Nullable Values values) {
        OPS.put(stubName(nativeName), new Op(kind, values));
    }

    private LuaHashtable() {
    }

    /** Whether the Jass native {@code name} is one of the hashtable natives. */
    public static boolean isNative(String name) {
        return OPS.containsKey(stubName(name));
    }

    /** The name of the stub the calls of the native {@code nativeName} are lowered to. */
    public static String stubName(String nativeName) {
        return "__wurst_" + nativeName;
    }

    /** The operation {@code f} is the stub of, or null when it is no hashtable stub. */
    public static @Nullable Op op(ImTranslator translator, ImFunction f) {
        if (!f.isNative() || translator.luaHashtableStubs.get(f.getName()) != f) {
            return null;
        }
        return OPS.get(f.getName());
    }

    /** Whether {@code f} is the stub of a load or a HaveSaved test, which only read the hashtable. */
    public static boolean isRead(ImTranslator translator, ImFunction f) {
        Op op = op(translator, f);
        return op != null && (op.kind() == Kind.LOAD || op.kind() == Kind.HAVE);
    }
}
