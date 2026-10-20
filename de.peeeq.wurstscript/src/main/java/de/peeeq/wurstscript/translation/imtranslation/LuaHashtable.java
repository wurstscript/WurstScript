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
 * hashtable is emitted as a call of it and its saves), so every subtable is always there.
 *
 * <p>Each operation answers what Jass answers, measured on the 3.0.0 client:
 * <ul>
 *     <li>A handle load is typed: a saved unit loads as itself through LoadUnitHandle and LoadWidgetHandle and as
 *     null through LoadItemHandle or LoadTimerHandle. Lua cannot tell a handle's type, so a save also writes the
 *     type of its native as a tag into {@link Values#HANDLE_TYPE}, and a load answers the handle only for a tag it
 *     accepts ({@link #acceptedTags}).</li>
 *     <li>Saving a null handle stores nothing, and a handle saved there before stays.</li>
 *     <li>FlushParentHashtable destroys the hashtable: later saves do not stick and every load answers its default.
 *     It points every subtable at one shared table which ignores writes.</li>
 *     <li>A null hashtable answers every load with its default and ignores every write.</li>
 * </ul>
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
        HANDLE("__wurst_ht_handle"),
        /** The tag of each saved handle, the type of the native which saved it, under the same keys. */
        HANDLE_TYPE("__wurst_ht_htype");

        /** The field of the hashtable which holds the subtable. */
        public final String field;

        Values(String field) {
            this.field = field;
        }
    }

    /**
     * An operation on the values of one kind, or on all of them ({@code values} is null). {@code handleType} is the
     * type of a handle save or load, the tag a save writes and the type a load asks for.
     */
    public record Op(Kind kind, @Nullable Values values, @Nullable String handleType) {
    }

    /** The handle types with a Save and a Load native of their own; agent has only a Save. */
    private static final List<String> HANDLE_TYPES = List.of(
        "Player", "Widget", "Destructable", "Item", "Unit", "Ability", "Timer", "Trigger", "TriggerCondition",
        "TriggerAction", "TriggerEvent", "Force", "Group", "Location", "Rect", "BooleanExpr", "Sound", "Effect",
        "UnitPool", "ItemPool", "Quest", "QuestItem", "DefeatCondition", "TimerDialog", "Leaderboard", "Multiboard",
        "MultiboardItem", "Trackable", "Dialog", "Button", "TextTag", "Lightning", "Image", "Ubersplat", "Region",
        "FogState", "FogModifier", "Hashtable", "Frame");

    /** The tag of a handle saved as an agent, whose own type is not known. */
    public static final String AGENT = "Agent";

    /** The handle types a widget can be; a handle saved as one of them is a widget. */
    public static final List<String> WIDGET_TYPES = List.of("Unit", "Item", "Destructable");

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
        OPS.put(stubName("SaveAgentHandle"), new Op(Kind.SAVE, Values.HANDLE, AGENT));
        for (String type : HANDLE_TYPES) {
            OPS.put(stubName("Save" + type + "Handle"), new Op(Kind.SAVE, Values.HANDLE, type));
            OPS.put(stubName("Load" + type + "Handle"), new Op(Kind.LOAD, Values.HANDLE, type));
        }
    }

    /**
     * The tags a load of {@code type} answers the handle for: its own type and, for a widget, the widget types. A
     * handle saved through a supertype (a widget, an agent) may be of the type loaded, which Lua cannot tell, so
     * those tags are accepted too.
     */
    public static List<String> acceptedTags(String type) {
        List<String> tags = new java.util.ArrayList<>();
        tags.add(type);
        if (type.equals("Widget")) {
            tags.addAll(WIDGET_TYPES);
        } else if (WIDGET_TYPES.contains(type)) {
            tags.add("Widget");
        }
        tags.add(AGENT);
        return tags;
    }

    private static void family(Values values, String save, String load, String have, String remove) {
        put(save, Kind.SAVE, values);
        put(load, Kind.LOAD, values);
        put(have, Kind.HAVE, values);
        put(remove, Kind.REMOVE, values);
    }

    private static void put(String nativeName, Kind kind, @Nullable Values values) {
        OPS.put(stubName(nativeName), new Op(kind, values, null));
    }

    /** The operation of the hashtable native or stub {@code name}, or null. */
    public static @Nullable Op opOfName(String name) {
        Op op = OPS.get(name);
        return op != null ? op : OPS.get(stubName(name));
    }

    /** The handle type of a handle save or load native ("SaveUnitHandle" is "Unit"), as the tables tag it. */
    public static String handleTypeOf(String nativeName) {
        Op op = opOfName(nativeName);
        if (op == null || op.handleType() == null) {
            throw new IllegalStateException("not a handle save or load: " + nativeName);
        }
        return op.handleType();
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

    /**
     * Whether {@code f} is the string load: the common.j native before {@link LuaNativeLowering#transform} replaces
     * its calls, or the stub after. It answers the null string, "", when nothing is stored.
     */
    public static boolean isStringLoad(ImTranslator translator, ImFunction f) {
        Op op = f.isBj() ? (isNative(f.getName()) ? opOfName(f.getName()) : null) : op(translator, f);
        return op != null && op.kind() == Kind.LOAD && op.values() == Values.STR;
    }

    /** Whether {@code f} is the stub of a load or a HaveSaved test, which only read the hashtable. */
    public static boolean isRead(ImTranslator translator, ImFunction f) {
        Op op = op(translator, f);
        return op != null && (op.kind() == Kind.LOAD || op.kind() == Kind.HAVE);
    }
}
