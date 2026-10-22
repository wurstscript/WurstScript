package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.luaAst.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Opt-in replacement of function bodies in a running Lua map ({@code -luaHotReload}, docs/LUA_HOT_RELOAD.md).
 *
 * <p>The script of a hot-reload build is cut in two: the bodies of the functions translated from the program, and the
 * skeleton, which is everything else (globals, field stores, class tables, the bootstrap, generated helpers, and each
 * function's name and parameters). The running map keeps its skeleton and its state; a payload replaces bodies. So a
 * payload is only valid for a script whose skeleton is the skeleton it was compiled against: the ABI fingerprint is a
 * hash of the skeleton, of the source identity of each emitted name, and of the compiler version. The payload of an
 * edit is the definition of every function whose printed text differs, so a change which reaches other bodies (a
 * folded fact, a removed call) ships those bodies too, and an edit which changes anything outside the bodies is
 * rejected, never patched.
 *
 * <p>A function value stored in a class table by the bootstrap is rebound with the function. A callback adapter
 * calls its target by name and needs nothing. A function value stored anywhere else would keep the old body, so such
 * a function is not reloadable.
 */
public final class LuaHotReload {

    public static final String ABI_GLOBAL = "__wurst_hotreload_abi";
    public static final String VERSION_GLOBAL = "__wurst_hotreload_version";
    public static final String APPLY_FUNCTION = "__wurst_hotreload_apply";
    public static final String POLL_FUNCTION = "__wurst_hotreload_poll";
    /** Answers what became of the last payload the poll read, for a map's own diagnostics. */
    public static final String STATUS_FUNCTION = "__wurst_hotreload_status";
    /**
     * The files payloads are delivered in, relative to the game's CustomMapData folder: payload n in
     * {@code Wurst\hotreload-<n>.pld}. Each payload needs a file of its own, because the game reads a file once:
     * measured on the 3.0.1.24371 client, {@code Preloader} never saw a file rewritten during the game (20 s), while a
     * file created during the game was read within 0.5 s.
     */
    public static final String PRELOAD_FILE_PREFIX = "Wurst\\hotreload-";

    /** The file of payload {@code version}, relative to CustomMapData, with Windows separators. */
    public static String preloadFile(int version) {
        return PRELOAD_FILE_PREFIX + version + ".pld";
    }
    /** The ability whose tooltip carries the payload from the preload file to the map; unverified in game. */
    public static final String CHANNEL_ABILITY = "Aamk";
    /**
     * What a payload file puts before the payload in the tooltip. Setting a tooltip to "" does not clear it: in the
     * 3.0.1.24371 client the tooltip then read back is the ability's own text, so only text after this marker comes
     * from a payload file.
     */
    public static final String PAYLOAD_MARKER = "wurst-hot-reload-payload:";

    private LuaHotReload() {
    }

    /** A function whose body a payload can replace. */
    public record Function(String name, String semanticId, String definition, List<String> slots, List<String> captures) {
    }

    /**
     * What a hot-reload build emitted: the ABI fingerprint written into the script, the reloadable functions by Lua
     * name, and the skeleton, kept to explain a rejection.
     */
    public record Manifest(String abi, Map<String, Function> functions, List<String> skeleton, List<String> bindings) {
    }

    /** A payload which rebinds {@code functions}, in Lua source which {@code load} compiles. */
    public record Payload(int version, List<String> functions, String source) {
    }

    /** An edit which a payload cannot carry: the map has to be rebuilt and restarted. */
    public static final class Rejected extends Exception {
        private final List<String> reasons;

        public Rejected(List<String> reasons) {
            super("The edit cannot be hot reloaded, rebuild and restart the map:\n  " + String.join("\n  ", reasons));
            this.reasons = List.copyOf(reasons);
        }

        public List<String> getReasons() {
            return reasons;
        }
    }

    /**
     * Fingerprints the translated script, appends the runtime which applies payloads, and returns the manifest.
     * Called once, on the final model; {@code reloadable} maps each function translated from the program to its IM
     * function, in translation order.
     */
    static Manifest install(LuaTranslator tr, Map<LuaFunction, ImFunction> reloadable, String compilerVersion) {
        LuaCompilationUnit model = tr.luaModel;
        Map<LuaFunction, List<String>> slots = new IdentityHashMap<>();
        Map<LuaFunction, List<String>> captures = new IdentityHashMap<>();
        collectFunctionValues(model, reloadable.keySet(), slots, captures);

        List<String> skeleton = new ArrayList<>();
        for (LuaStatement statement : model) {
            if (statement instanceof LuaFunction f && reloadable.containsKey(f)) {
                skeleton.add(header(f));
            } else {
                StringBuilder sb = new StringBuilder();
                statement.print(sb, 0);
                skeleton.add(sb.toString());
            }
        }

        List<String> bindings = new ArrayList<>();
        for (ImVar global : tr.prog.getGlobals()) {
            bindings.add(tr.luaVar.getFor(global).getName() + "=" + semanticId(global, global.getName())
                + ":" + typeKey(global.getType()));
        }
        for (ImClass c : tr.prog.getClasses()) {
            bindings.add(tr.luaClassVar.getFor(c).getName() + "=" + semanticId(c, c.getName()));
            for (ImVar field : c.getFields()) {
                // With its type: objects which exist keep the values of the old one, which two class types
                // with the same nil default and storage would not reveal.
                bindings.add(tr.fieldStorage(field).getName() + "=" + semanticId(field, c.getName() + "." + field.getName())
                    + ":" + typeKey(field.getType()));
            }
        }

        Map<String, Function> functions = new LinkedHashMap<>();
        for (Map.Entry<LuaFunction, ImFunction> e : reloadable.entrySet()) {
            LuaFunction lf = e.getKey();
            String id = semanticId(e.getValue(), signature(e.getValue()));
            bindings.add(lf.getName() + "=" + id);
            StringBuilder definition = new StringBuilder();
            lf.print(definition, 0);
            functions.put(lf.getName(), new Function(lf.getName(), id, definition.toString(),
                List.copyOf(slots.getOrDefault(lf, List.of())), List.copyOf(captures.getOrDefault(lf, List.of()))));
        }

        String abi = fingerprint(compilerVersion, skeleton, bindings);
        appendRuntime(tr, model, abi);
        return new Manifest(abi, Collections.unmodifiableMap(functions), List.copyOf(skeleton), List.copyOf(bindings));
    }

    /**
     * The payload which takes a map running {@code baseline} to {@code current}: every function whose definition
     * differs from the baseline, and every function an earlier payload replaced, so that payloads are cumulative and
     * an edit which is undone is undone in the map too.
     */
    public static Payload payload(Manifest baseline, Manifest current, Set<String> reloadedBefore, int version)
        throws Rejected {
        List<String> reasons = new ArrayList<>();
        if (!baseline.abi().equals(current.abi())) {
            describeAbiChange(baseline, current, reasons);
        }
        List<String> changed = new ArrayList<>();
        for (Function f : current.functions().values()) {
            Function old = baseline.functions().get(f.name());
            if (old == null) {
                continue; // a new function changes the skeleton, reported above
            }
            if (old.definition().equals(f.definition()) && !reloadedBefore.contains(f.name())) {
                continue;
            }
            if (!f.captures().isEmpty()) {
                reasons.add("function " + f.name() + " is stored as a value (" + String.join(", ", f.captures())
                    + "), which keeps the old body");
            }
            changed.add(f.name());
        }
        if (!reasons.isEmpty()) {
            throw new Rejected(reasons);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("-- Wurst hot reload payload ").append(version).append(": ").append(String.join(", ", changed)).append("\n");
        sb.append("return \"").append(current.abi()).append("\", ").append(version).append(", function()\n");
        for (String name : changed) {
            Function f = current.functions().get(name);
            sb.append(f.definition()).append("\n");
            for (String slot : f.slots()) {
                sb.append(slot).append(" = ").append(name).append("\n");
            }
        }
        sb.append("end\n");
        return new Payload(version, List.copyOf(changed), sb.toString());
    }

    private static void describeAbiChange(Manifest baseline, Manifest current, List<String> reasons) {
        Set<String> added = new TreeSet<>(current.functions().keySet());
        added.removeAll(baseline.functions().keySet());
        Set<String> removed = new TreeSet<>(baseline.functions().keySet());
        removed.removeAll(current.functions().keySet());
        if (!added.isEmpty()) {
            reasons.add("functions the running script does not have: " + String.join(", ", added));
        }
        if (!removed.isEmpty()) {
            reasons.add("functions the edit removes: " + String.join(", ", removed));
        }
        // An added or removed function shifts every binding after it, which says nothing more.
        List<String> oldBindings = added.isEmpty() && removed.isEmpty() ? baseline.bindings() : List.of();
        List<String> newBindings = current.bindings();
        for (int i = 0; i < Math.min(oldBindings.size(), newBindings.size()); i++) {
            if (!oldBindings.get(i).equals(newBindings.get(i))) {
                reasons.add("a name now means something else: " + oldBindings.get(i) + " became " + newBindings.get(i));
                break;
            }
        }
        List<String> oldSkeleton = baseline.skeleton();
        List<String> newSkeleton = current.skeleton();
        for (int i = 0; i < Math.max(oldSkeleton.size(), newSkeleton.size()); i++) {
            String before = i < oldSkeleton.size() ? oldSkeleton.get(i) : "(nothing)";
            String after = i < newSkeleton.size() ? newSkeleton.get(i) : "(nothing)";
            if (!before.equals(after)) {
                reasons.add("the script outside the function bodies changed (globals, class tables or initialisation):\n"
                    + "    was: " + firstLine(before) + "\n    now: " + firstLine(after));
                break;
            }
        }
        if (reasons.isEmpty()) {
            reasons.add("the payload was compiled by a different compiler than the running script");
        }
    }

    private static String firstLine(String s) {
        int nl = s.indexOf('\n');
        return nl < 0 ? s : s.substring(0, nl) + " ...";
    }

    /**
     * Finds where a reloadable function is used as a value rather than called: a class-table binding the payload
     * repeats ({@code slots}), or a store it cannot reach ({@code captures}). A callback adapter passes its target to
     * xpcall by name each time it runs, so it reaches the new body without either.
     */
    private static void collectFunctionValues(LuaCompilationUnit model, Set<LuaFunction> reloadable,
                                              Map<LuaFunction, List<String>> slots,
                                              Map<LuaFunction, List<String>> captures) {
        for (LuaStatement statement : model) {
            String where = statement instanceof LuaFunction f ? f.getName() : "the main chunk";
            visitFuncRefs(statement, ref -> {
                LuaFunction target = ref.getFunc();
                if (!reloadable.contains(target)) {
                    return;
                }
                de.peeeq.wurstscript.luaAst.Element parent = ref.getParent();
                if (parent instanceof LuaAssignment a && a.getRight() == ref
                    && a.getLeft() instanceof LuaExprFieldAccess fa && fa.getReceiver() instanceof LuaExprVarAccess table) {
                    slots.computeIfAbsent(target, t -> new ArrayList<>()).add(table.getVar().getName() + "." + fa.getFieldName());
                } else if (parent instanceof LuaExprlist args && args.indexOf(ref) == 0
                    && args.getParent() instanceof LuaExprFunctionCallByName call && call.getFuncName().equals("xpcall")) {
                    // resolved by name on every call
                } else {
                    captures.computeIfAbsent(target, t -> new ArrayList<>()).add("in " + where);
                }
            });
        }
    }

    private static void visitFuncRefs(de.peeeq.wurstscript.luaAst.Element e, java.util.function.Consumer<LuaExprFuncRef> action) {
        if (e instanceof LuaExprFuncRef ref) {
            action.accept(ref);
        }
        for (int i = 0; i < e.size(); i++) {
            visitFuncRefs(e.get(i), action);
        }
    }

    private static String header(LuaFunction f) {
        StringJoiner params = new StringJoiner(", ", "function " + f.getName() + "(", ")");
        for (LuaVariable p : f.getParams()) {
            params.add(p.getName());
        }
        return params.toString();
    }

    /**
     * The source identity of an emitted name: its package and its IM name, which carries the class prefix. Generated
     * Lua names are numbered in translation order, so two declarations of one name can swap numbers; the ids tell them
     * apart.
     */
    private static String semanticId(ElementWithTrace element, String name) {
        PackageOrGlobal p = element.attrTrace().attrNearestPackage();
        return (p instanceof WPackage wp ? wp.getName() : "global") + ":" + name;
    }

    private static String signature(ImFunction f) {
        StringJoiner sig = new StringJoiner(",", f.getName() + "(", ")");
        for (ImVar p : f.getParameters()) {
            sig.add(typeKey(p.getType()));
        }
        return sig + typeKey(f.getReturnType());
    }

    /**
     * A type as the same text in every compilation. The IM printer appends an identity hash to a type variable, which
     * differs from one compilation to the next, so it cannot be part of an id.
     */
    private static String typeKey(ImType t) {
        if (t instanceof ImTypeVarRef r) {
            return r.getTypeVariable().getName();
        } else if (t instanceof ImClassType ct) {
            StringJoiner args = new StringJoiner(",", ct.getClassDef().getName() + "<", ">");
            for (ImTypeArgument ta : ct.getTypeArguments()) {
                args.add(typeKey(ta.getType()));
            }
            return args.toString();
        } else if (t instanceof ImArrayType a) {
            return typeKey(a.getEntryType()) + "[]";
        } else if (t instanceof ImArrayTypeMulti a) {
            return typeKey(a.getEntryType()) + a.getArraySize();
        } else if (t instanceof ImTupleType tuple) {
            StringJoiner types = new StringJoiner(",", "(", ")");
            for (ImType component : tuple.getTypes()) {
                types.add(typeKey(component));
            }
            return types.toString();
        }
        return t.toString();
    }

    private static String fingerprint(String compilerVersion, List<String> skeleton, List<String> bindings) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(compilerVersion.getBytes(StandardCharsets.UTF_8));
            for (List<String> part : List.of(skeleton, bindings)) {
                for (String s : part) {
                    digest.update((byte) 0);
                    digest.update(s.getBytes(StandardCharsets.UTF_8));
                }
                digest.update((byte) 1);
            }
            return HexFormat.of().formatHex(digest.digest(), 0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * The runtime of a hot-reload build: the fingerprint, the applied payload version, the function which applies a
     * payload, and the poll which reads the next payload file (single-player development only; the files are local,
     * so a multiplayer game would desync). The poll reads each file once, in order, whatever its payload does: a
     * rejected payload is not read again, and the next one is in the next file.
     *
     * <p>A payload is compiled with the script's own environment, so its bodies reach the same globals. Compiling it
     * and calling its chunk run no program code; the ABI and version are checked before the install function, which
     * only assigns, is called. A rejected payload leaves the map untouched.
     */
    private static void appendRuntime(LuaTranslator tr, LuaCompilationUnit model, String abi) {
        model.add(LuaAst.LuaLiteral(ABI_GLOBAL + " = \"" + abi + "\""));
        model.add(LuaAst.LuaLiteral(VERSION_GLOBAL + " = 0"));
        model.add(LuaAst.LuaLiteral("__wurst_hotreload_nextFile = 1"));
        model.add(LuaAst.LuaLiteral(String.join("\n",
            "function " + APPLY_FUNCTION + "(source)",
            "    local chunk, err = load(source, \"=wurst hot reload\", \"t\", _ENV)",
            "    if not chunk then return false, err end",
            "    local ok, abi, version, install = pcall(chunk)",
            "    if not ok then return false, abi end",
            "    if abi ~= " + ABI_GLOBAL + " then",
            "        return false, \"the payload was compiled for another script: rebuild and restart the map\"",
            "    end",
            "    if type(version) ~= \"number\" or version <= " + VERSION_GLOBAL + " then",
            "        return false, \"payload \" .. tostring(version) .. \" is not newer than \" .. " + VERSION_GLOBAL,
            "    end",
            "    local installed, installErr = pcall(install)",
            "    if not installed then",
            "        return false, \"the payload failed while it was installed, so the map may run old and new bodies: \" .. tostring(installErr)",
            "    end",
            "    " + VERSION_GLOBAL + " = version",
            "    return true",
            "end")));
        model.add(LuaAst.LuaLiteral("__wurst_hotreload_lastStatus = \"no payload read\""));
        model.add(LuaAst.LuaLiteral("function " + STATUS_FUNCTION + "() return __wurst_hotreload_lastStatus end"));
        model.add(LuaAst.LuaLiteral(String.join("\n",
            "function " + POLL_FUNCTION + "()",
            "    if not bj_isSinglePlayer then return end",
            "    local channel = FourCC(\"" + CHANNEL_ABILITY + "\")",
            // "" does not clear a tooltip, so the payload last read would be read again.
            "    BlzSetAbilityTooltip(channel, \" \", 0)",
            "    Preloader(\"" + PRELOAD_FILE_PREFIX.replace("\\", "\\\\") + "\" .. __wurst_hotreload_nextFile .. \".pld\")",
            "    local text = BlzGetAbilityTooltip(channel, 0)",
            "    local marker = \"" + PAYLOAD_MARKER + "\"",
            "    if text:sub(1, #marker) ~= marker then return end",
            "    local source = text:sub(#marker + 1)",
            "    __wurst_hotreload_nextFile = __wurst_hotreload_nextFile + 1",
            "    local ok, err = " + APPLY_FUNCTION + "(source)",
            "    if ok then",
            "        __wurst_hotreload_lastStatus = \"applied payload \" .. " + VERSION_GLOBAL,
            "    else",
            "        __wurst_hotreload_lastStatus = \"rejected: \" .. tostring(err)",
            "    end",
            "    BJDebugMsg(\"Wurst hot reload: \" .. __wurst_hotreload_lastStatus)",
            "end")));
    }
}
