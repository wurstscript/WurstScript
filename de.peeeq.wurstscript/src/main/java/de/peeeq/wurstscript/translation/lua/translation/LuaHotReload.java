package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.luaAst.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Opt-in replacement of code in a running Lua map ({@code -luaHotReload}, docs/LUA_HOT_RELOAD.md).
 *
 * <p>A hot-reload build names every declaration from its identity, so the script of an edited program differs from
 * the running one only where the program changed. The manifest of a build lists that script in parts a payload can
 * apply one by one: every function's definition, every top-level declaration (globals, field stores, class tables,
 * native fallbacks), and the statements of the bootstrap and of the global initialisers (defaults, class-table slots,
 * class data, initial values). The payload of an edit compares the running script's manifest with the edited one:
 * <ul>
 *   <li>a new or changed function is defined, and its class-table slots are bound again;</li>
 *   <li>a new top-level declaration runs; a changed one is rejected;</li>
 *   <li>a new initialisation statement runs, and so does a changed slot binding or table of a class's supertypes; a
 *   changed initial value of a constant which only computes its value runs again, and so do the constants computed
 *   from it ({@link Constant}); a changed initial value of any other global which exists is skipped with a warning, since the map keeps its value; any other
 *   changed one is rejected;</li>
 *   <li>the init of a new package runs once; a changed init of a package which ran warns that it needs a restart.</li>
 * </ul>
 * Payloads are cumulative from the start of the map. The map records each statement a payload ran
 * ({@link #RAN_TABLE}), only once it ran, so a later payload runs what an earlier one did not reach and nothing twice.
 * A function stored as a value anywhere but a class table keeps the body it was taken from, so a change to one is
 * rejected. One which is removed, or a class-table slot nothing binds any more, keeps its old body for what the map
 * made before, which the new code cannot reach: that warns that a restart drops it.
 */
public final class LuaHotReload {

    public static final String ABI_GLOBAL = "__wurst_hotreload_abi";
    public static final String VERSION_GLOBAL = "__wurst_hotreload_version";
    public static final String APPLY_FUNCTION = "__wurst_hotreload_apply";
    public static final String POLL_FUNCTION = "__wurst_hotreload_poll";
    /**
     * Answers what became of the last payload the poll read, for a map's own diagnostics: applied, with what it printed
     * it could not apply ({@link #WARN_FUNCTION}), or rejected, with the reason.
     */
    public static final String STATUS_FUNCTION = "__wurst_hotreload_status";
    /** Prints what a payload carries but cannot apply, and keeps it for the status of the payload. */
    private static final String WARN_FUNCTION = "__wurst_hotreload_warn";
    /** Runs a reload hook for a payload: reports a failure, and leaves the -stacktraces stack as it found it. */
    private static final String HOOK_FUNCTION = "__wurst_hotreload_runHook";
    public static final String WARNINGS = "__wurst_hotreload_warnings";
    /**
     * The map's record of the statements payloads ran, by {@link #runKey}, and of the new package inits which succeeded.
     * The map keeps it, not the editor: a payload is delivered as a file, and only the map knows how far it ran.
     */
    public static final String RAN_TABLE = "__wurst_hotreload_ran";
    /** Semantic declaration identities actually installed by payloads, including their source-level types. */
    public static final String BINDINGS_TABLE = "__wurst_hotreload_bindings";
    /** The order in which source initializer units first completed in this map, not the editor's delivery order. */
    private static final String INITIALIZER_POSITIONS = "__wurst_hotreload_initPositions";
    private static final String INITIALIZER_COUNT = "__wurst_hotreload_initCount";
    /** For each source initializer unit, when it first completed: 0 when the map started, n in payload n. */
    private static final String INITIALIZER_BATCHES = "__wurst_hotreload_initBatches";
    /** The payload being installed, 0 before the first. */
    private static final String CURRENT_BATCH = "__wurst_hotreload_batch";
    /** Initializer units first introduced after their target globals already had live values. */
    private static final String KEPT_INITIALIZERS = "__wurst_hotreload_keptInitializers";
    /**
     * The constants whose initializers payloads ran, each with the {@link #effectiveRevision} it ran: the map, not the
     * editor, knows which payload reached it.
     */
    private static final String CONSTANT_VALUES = "__wurst_hotreload_constants";
    /**
     * For each array constant a payload ran ({@link #CONSTANT_VALUES}), how many entries that run wrote: the next run
     * which writes fewer clears the rest, so they read the array's default as in a restarted map.
     */
    private static final String CONSTANT_LENGTHS = "__wurst_hotreload_constantLengths";
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

    /** What a function is to a payload. */
    public enum Kind {
        /** Translated from the program: called by name or through its class-table slots. */
        PROGRAM,
        /** A package init: it ran when the map started and does not run again. */
        PACKAGE_INIT,
        /** main or config: they ran when the map started; what they call is reloaded by its own parts. */
        ENTRY,
        /** Generated by the backend: allocators, dispatch stubs, callback adapters, helpers. */
        HELPER
    }

    private LuaHotReload() {
    }

    /**
     * A function of the script: its definition, the class-table slots which bind it, and where it is stored as a value.
     * {@code shape} is a hash of the definition with the source positions of its calls (-stacktraces) blanked: an edit
     * which only moves lines changes the definition, which a payload ships, but not the code, which is what a function
     * whose body must not change ({@code captures}) is compared by. {@code closureSource} is a hash of a closure's source
     * as written, null for any other function: the closures told apart by their order are compared by it, since each
     * one's Lua names its own class.
     */
    public record Function(String name, String semanticId, String definition, List<String> slots, List<String> captures,
                           Kind kind, String shape, String closureSource) {
        public Function(String name, String semanticId, String definition, List<String> slots, List<String> captures,
                        Kind kind) {
            this(name, semanticId, definition, slots, captures, kind, identityHash(definition), null);
        }
    }

    /**
     * A statement of the script outside the functions: {@code scope} is {@code top} (a top-level declaration),
     * {@code boot} (the bootstrap), {@code globals} (the global initialisers) or {@code init} (a package init); {@code key}
     * identifies the assignment or complete source initializer;
     * {@code kind} is {@code slot} (a function bound in a table, or a class's table of supertypes), {@code global} (a value given to a global) or
     * {@code other}; {@code owner} is the package init it is part of, or empty. {@code revision} is the initializer's
     * complete definition for an initializer call, otherwise the statement text. {@code targets} identifies all
     * retained storage written by a source initializer, including tuple leaves and generic specializations.
     */
    public record Statement(String scope, String key, String text, String kind, String owner, String revision,
                            List<String> targets) {
    }

    /**
     * The source initializer of a constant global. It is {@code pure} when it only computes the value
     * ({@link ConstantInitializers}): running it again then gives the value a restarted map would have, since nothing
     * else writes a constant. {@code reads} names the initializers of the constants it reads, which run before it, and
     * {@code calls} the functions of the program it calls, directly or through others: an edit of one changes its value.
     * {@code length} is the number of entries the initializer of an array constant writes, at indices 0 to length - 1,
     * when those are constants, and null for any other constant: a payload which runs it again clears the entries past
     * it which the map's last run wrote.
     */
    public record Constant(boolean pure, List<String> reads, List<String> calls, Integer length) {
    }

    /**
     * What a hot-reload build emitted: its fingerprint, which the script holds, and its parts; {@code initOrder} names
     * the package inits in the order main calls them, {@code constants} the initializers of constant globals,
     * {@code variables} the storage of the globals the program declares as variables, initialised or not, and
     * {@code hooks} the functions annotated {@code @onHotReload}, in the order a payload calls them.
     * {@code compiletimeReferenceOwners} maps each materialized reference's source path to its actual object/hashtable
     * global's semantic identity, so aliasing cannot silently move an existing graph to a new snapshot owner.
     */
    public record Manifest(String abi, String compilerVersion, Map<String, Function> functions, List<Statement> statements,
                           List<String> bindings, List<String> initOrder, Map<String, Constant> constants,
                           List<String> variables, List<String> hooks, Map<String, Closure> closures,
                           Map<String, Integer> compiletimeGroups, boolean compiletimeIntegerReferences,
                           Map<String, String> compiletimeReferenceOwners) {
        public Manifest(String abi, String compilerVersion, Map<String, Function> functions, List<Statement> statements,
                        List<String> bindings, List<String> initOrder, Map<String, Constant> constants,
                        List<String> variables, List<String> hooks, Map<String, Closure> closures,
                        Map<String, Integer> compiletimeGroups, boolean compiletimeIntegerReferences) {
            this(abi, compilerVersion, functions, statements, bindings, initOrder, constants, variables, hooks, closures,
                compiletimeGroups, compiletimeIntegerReferences, null);
        }
        public Manifest(String abi, String compilerVersion, Map<String, Function> functions, List<Statement> statements,
                        List<String> bindings, List<String> initOrder, Map<String, Constant> constants,
                        List<String> variables, List<String> hooks, Map<String, Closure> closures,
                        Map<String, Integer> compiletimeGroups) {
            this(abi, compilerVersion, functions, statements, bindings, initOrder, constants, variables, hooks,
                closures, compiletimeGroups, false);
        }
        public Manifest(String abi, String compilerVersion, Map<String, Function> functions, List<Statement> statements,
                        List<String> bindings, List<String> initOrder, Map<String, Constant> constants,
                        List<String> variables, List<String> hooks, Map<String, Closure> closures) {
            this(abi, compilerVersion, functions, statements, bindings, initOrder, constants, variables, hooks, closures, Map.of());
        }

        public Manifest(String abi, String compilerVersion, Map<String, Function> functions, List<Statement> statements,
                        List<String> bindings, List<String> initOrder) {
            this(abi, compilerVersion, functions, statements, bindings, initOrder, Map.of(), List.of(), List.of(), Map.of(), Map.of());
        }

        public Manifest(String abi, String compilerVersion, Map<String, Function> functions, List<Statement> statements,
                        List<String> bindings, List<String> initOrder, Map<String, Constant> constants,
                        List<String> variables, List<String> hooks) {
            this(abi, compilerVersion, functions, statements, bindings, initOrder, constants, variables, hooks, Map.of(), Map.of());
        }
    }

    /**
     * Where the objects of a closure keep their state, by the closure's function: {@code tables} names, by role
     * ({@code class}, or {@code field:} and the field), the class table an object holds and the stores of its captured
     * values; {@code fresh} gives, by table name, the expression which makes an empty one; {@code classData} names the
     * bootstrap statements which write the class table (its slots, supertypes, type id); {@code captureTypes} records
     * source field types independently of the names a remap gives their stores. An object holds its class
     * table itself (__wurst_objectClass), so a payload can give the names of the closures of an order group to other
     * closures while each table, and so each object made before, stays with the closure it was made from.
     */
    public record Closure(Map<String, String> tables, Map<String, String> fresh, List<String> classData,
                          Map<String, String> captureTypes) {
    }

    /**
     * A closure of a payload's program as the payload matched it: its function's {@code name} in the program, its
     * {@code group} (the identity it shares with the others of an order group), its {@code lineage} (which closure the map
     * keeps state of for it: a closure of the started map by its name there, or one a payload added) and its source.
     */
    public record GroupMember(String group, String name, String lineage, String source) {
    }

    /**
     * What earlier payloads sent to the running map: the functions they defined, and the functions they stored as
     * values, each with the hash of the definition it had then ({@link #definitionHash}).
     * {@code slots} preserves the class-table bindings they may have installed; {@code initializers} maps initializer
     * names to their run-once keys, whose presence in the map confirms whether they ran. {@code variables} names the
     * storage of globals one of those versions declared as variables, which code may then have written.
     * {@code groupShapes} holds every shape sent of each closure told apart by its order ({@link #orderGroups}), by its
     * lineage: which of them the map runs depends on which payloads it installed, which the editor does not know.
     * {@code groupLayout} is how the last payload matched the closures of its program ({@link GroupMember}), which the
     * next one matches its own against; null means no layout was recorded, while empty means all closures were removed.
     * {@code lineageOrigins} names the tables of each closure a payload added where
     * that payload's program kept them ({@link Closure#tables}), empty for a role whose table it had to make anew;
     * {@code permutedGroups} names the groups whose tables a payload gave to other closures.
     * {@code lineageTypes} preserves captured field types even when a later payload removes a capture or its closure.
     * {@code tableReaders} retains the historical lineages whose emitted bodies may still read each table name.
     * {@code compiletimeReferenceOwners} retains snapshot paths introduced by earlier payloads, including removed ones.
     */
    public record History(Set<String> reloaded, Map<String, String> captured, Map<String, List<String>> slots,
                          Map<String, String> initializers, Set<String> packageInits, Set<String> variables,
                          Map<String, List<String>> groupShapes, List<GroupMember> groupLayout,
                          Map<String, Map<String, String>> lineageOrigins, Set<String> permutedGroups, Set<String> reboundSlots,
                          Map<String, Map<String, String>> lineageTypes, Map<String, Set<String>> tableReaders,
                          Map<String, Integer> compiletimeGroups, Map<String, String> compiletimeReferenceOwners) {
        public History(Set<String> reloaded, Map<String, String> captured, Map<String, List<String>> slots,
                       Map<String, String> initializers, Set<String> packageInits, Set<String> variables,
                       Map<String, List<String>> groupShapes) {
            this(reloaded, captured, slots, initializers, packageInits, variables, groupShapes, null, Map.of(), Set.of(), Set.of(), Map.of(), Map.of(), Map.of(), Map.of());
        }

        public History(Set<String> reloaded, Map<String, String> captured, Map<String, List<String>> slots,
                       Map<String, String> initializers, Set<String> packageInits, Set<String> variables,
                       Map<String, List<String>> groupShapes, Set<String> reboundSlots) {
            this(reloaded, captured, slots, initializers, packageInits, variables, groupShapes, null, Map.of(), Set.of(), reboundSlots, Map.of(), Map.of(), Map.of(), Map.of());
        }

        public History(Set<String> reloaded, Map<String, String> captured) {
            this(reloaded, captured, Map.of(), Map.of(), Set.of());
        }

        public History(Set<String> reloaded, Map<String, String> captured, Map<String, List<String>> slots,
                       Map<String, String> initializers) {
            this(reloaded, captured, slots, initializers, Set.of());
        }

        public History(Set<String> reloaded, Map<String, String> captured, Map<String, List<String>> slots,
                       Map<String, String> initializers, Set<String> packageInits) {
            this(reloaded, captured, slots, initializers, packageInits, Set.of());
        }

        public History(Set<String> reloaded, Map<String, String> captured, Map<String, List<String>> slots,
                       Map<String, String> initializers, Set<String> packageInits, Set<String> variables) {
            this(reloaded, captured, slots, initializers, packageInits, variables, Map.of());
        }

        public static final History NONE = new History(Set.of(), Map.of());
    }

    /**
     * A payload: Lua source which {@code load} compiles and which applies {@code functions}. {@code warnings} name what
     * it carries but cannot apply; {@code captured} the functions its version stores as values, with their definitions'
     * hashes. {@code slots}, {@code initializers}, {@code variables}, {@code groupShapes} and {@code reboundSlots} carry the installation
     * metadata subsequent payloads need.
     */
    public record Payload(int version, List<String> functions, String source, List<String> warnings,
                          Map<String, String> captured, Map<String, List<String>> slots, Map<String, String> initializers,
                          Set<String> packageInits, Set<String> variables, Map<String, String> groupShapes,
                          List<GroupMember> groupLayout, Map<String, Map<String, String>> lineageOrigins,
                          Set<String> permutedGroups, Set<String> reboundSlots, Map<String, Map<String, String>> lineageTypes,
                          Map<String, Set<String>> tableReaders, Map<String, Integer> compiletimeGroups,
                          Map<String, String> compiletimeReferenceOwners) {
        public Payload(int version, List<String> functions, String source, List<String> warnings,
                       Map<String, String> captured, Map<String, List<String>> slots, Map<String, String> initializers,
                       Set<String> packageInits, Set<String> variables, Map<String, String> groupShapes, Set<String> reboundSlots) {
            this(version, functions, source, warnings, captured, slots, initializers, packageInits, variables, groupShapes,
                List.of(), Map.of(), Set.of(), reboundSlots, Map.of(), Map.of(), Map.of(), Map.of());
        }

        public Payload(int version, List<String> functions, String source, List<String> warnings,
                       Map<String, String> captured, Map<String, List<String>> slots, Map<String, String> initializers,
                       Set<String> packageInits, Set<String> variables, Map<String, String> groupShapes) {
            this(version, functions, source, warnings, captured, slots, initializers, packageInits, variables, groupShapes,
                List.of(), Map.of(), Set.of(), Set.of(), Map.of(), Map.of(), Map.of(), Map.of());
        }
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
     * Lists the translated script in the parts a payload applies, fingerprints it, appends the runtime which applies
     * payloads, and returns the manifest. Called once, on the final model; {@code reloadable} maps each function
     * translated from the program to its IM function.
     */
    static Manifest install(LuaTranslator tr, Map<LuaFunction, ImFunction> reloadable, List<ImFunction> packageInits,
                            Set<ImFunction> entries, String compilerVersion) {
        // Older scripts did not record completed action units; applying these units to one would repeat setup.
        compilerVersion += "+hotreload-init-actions-v1";
        Set<ImFunction> inits = Collections.newSetFromMap(new IdentityHashMap<>());
        inits.addAll(packageInits);
        Map<ImFunction, String> initNames = new IdentityHashMap<>();
        LuaCompilationUnit model = tr.luaModel;
        LuaFunction bootstrap = tr.bootstrapFunction();
        ImFunction globalInitIm = tr.imTr.getGlobalInitFunc();
        LuaFunction globalInit = globalInitIm == null ? null : tr.luaFunc.getFor(globalInitIm);
        Set<LuaFunction> compiletimeFunctions = Collections.newSetFromMap(new IdentityHashMap<>());
        ImFunction compiletimeRoot = tr.imTr.compiletimeStateFunction();
        if (compiletimeRoot != null) {
            compiletimeFunctions.add(tr.luaFunc.getFor(compiletimeRoot));
            tr.imTr.compiletimeStateParts().forEach(f -> compiletimeFunctions.add(tr.luaFunc.getFor(f)));
        }

        Map<LuaFunction, List<String>> slots = new IdentityHashMap<>();
        Map<LuaFunction, List<String>> captures = new IdentityHashMap<>();
        collectFunctionValues(model, slots, captures);
        // Closures which share an identity are told apart by their order; payload() refuses an edit which moves them.
        // Only an assignment to one of these gives a global its value; one to a local of a function body does not.
        Set<LuaVariable> globals = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ImVar global : tr.prog.getGlobals()) {
            globals.add(tr.luaVar.getFor(global));
        }
        Map<LuaFunction, String> actions = new IdentityHashMap<>();
        for (var entry : reloadable.entrySet()) {
            if (entry.getValue().hasFlag(FunctionFlagEnum.HOT_RELOAD_INIT_ACTION)) {
                actions.put(entry.getKey(), withoutPositions(tr, entry.getKey()));
            }
        }
        Map<LuaFunction, ImVar> initializers = new IdentityHashMap<>();
        Map<LuaFunction, List<String>> initializerTargets = new IdentityHashMap<>();
        ImTranslator.GlobalStorage storage = tr.imTr.globalStorage();
        for (Map.Entry<LuaFunction, ImFunction> entry : reloadable.entrySet()) {
            ImVar global = tr.imTr.luaGlobalInitializers.get(entry.getValue());
            if (global != null) {
                initializers.put(entry.getKey(), global);
                List<String> targets = new ArrayList<>();
                for (ImVar component : storage.of(global)) {
                    targets.add(tr.luaVar.getFor(component).getName());
                }
                initializerTargets.put(entry.getKey(), List.copyOf(targets));
            }
        }
        // The source initializers in the order the map runs them.
        List<ImFunction> initializerOrder = new ArrayList<>();
        for (ImFunction init : packageInits) {
            LuaFunction owner = tr.luaFunc.getFor(init);
            for (LuaStatement statement : owner.getBody()) {
                if (statement instanceof LuaExprFunctionCall call && actions.containsKey(call.getFunc())) {
                    String marker = RAN_TABLE + "[" + luaString(runKey("init", owner.getName(), call.getFunc().getName())) + "]";
                    call.getFunc().getBody().add(0, LuaAst.LuaLiteral("if " + marker + " then return end"));
                    call.getFunc().getBody().add(LuaAst.LuaLiteral(marker + " = " + luaString(identityHash(actions.get(call.getFunc())))));
                }
                if (statement instanceof LuaExprFunctionCall call && initializers.containsKey(call.getFunc())) {
                    initializerOrder.add(reloadable.get(call.getFunc()));
                    String key = luaString(runKey("init", owner.getName(), call.getFunc().getName()));
                    String position = INITIALIZER_POSITIONS + "[" + key + "]";
                    // This guard also applies when a newly introduced package-init function calls the unit itself.
                    call.getFunc().getBody().add(0, LuaAst.LuaLiteral("if " + KEPT_INITIALIZERS + "[" + key + "] then return end"));
                    // Before the manifest is printed: every definition, including one a payload replaces, records
                    // completion here. A failing initializer never reaches it; a skipped delivery records nothing.
                    call.getFunc().getBody().add(LuaAst.LuaLiteral("if not " + position + " then\n"
                        + INITIALIZER_COUNT + " = " + INITIALIZER_COUNT + " + 1\n"
                        + position + " = " + INITIALIZER_COUNT + "\n"
                        + INITIALIZER_BATCHES + "[" + key + "] = " + CURRENT_BATCH + "\nend"));
                }
            }
        }

        List<Statement> statements = new ArrayList<>();
        Map<String, Function> functions = new LinkedHashMap<>();
        List<String> bindings = new ArrayList<>();
        for (LuaStatement statement : model) {
            if (statement instanceof LuaFunction f) {
                if (compiletimeFunctions.contains(f)) {
                    continue;
                }
                if (f == bootstrap || f == globalInit) {
                    String scope = f == bootstrap ? "boot" : "globals";
                    for (LuaStatement s : f.getBody()) {
                        statements.add(statement(tr, scope, "", s, globals, initializerTargets, actions));
                    }
                    continue;
                }
                ImFunction im = reloadable.get(f);
                Kind kind = im == null ? Kind.HELPER
                    : inits.contains(im) ? Kind.PACKAGE_INIT
                    : entries.contains(im) ? Kind.ENTRY
                    : Kind.PROGRAM;
                String id = im == null ? "generated:" + f.getName() : functionId(im);
                ImVar initialized = initializers.get(f);
                if (initialized != null) {
                    id += ":initializes:" + semanticId(initialized, initialized.getName()) + ":" + typeKey(initialized.getType())
                        + ":storage:" + initializerTargets.get(f);
                }
                if (im != null) {
                    bindings.add(f.getName() + "=" + id);
                }
                if (kind == Kind.PACKAGE_INIT) {
                    initNames.put(im, f.getName());
                    // The initial values of a package's globals are given here: a new global's must run.
                    for (LuaStatement s : f.getBody()) {
                        statements.add(statement(tr, "init", f.getName(), s, globals, initializerTargets, actions));
                    }
                }
                StringBuilder definition = new StringBuilder();
                f.print(definition, 0);
                functions.put(f.getName(), new Function(f.getName(), id, definition.toString(),
                    List.copyOf(slots.getOrDefault(f, List.of())), List.copyOf(captures.getOrDefault(f, List.of())), kind,
                    kind == Kind.ENTRY ? entryShape(tr, f) : identityHash(withoutPositions(tr, f)),
                    im != null && im.getTrace() instanceof ExprClosure closure ? identityHash(Utils.prettyPrint(closure)) : null));
            } else {
                statements.add(statement(tr, "top", "", statement, globals, initializerTargets, actions));
            }
        }

        if (compiletimeRoot != null) {
            Set<LuaVariable> objects = Collections.newSetFromMap(new IdentityHashMap<>());
            tr.imTr.compiletimeGlobals().forEach(v -> objects.add(tr.luaVar.getFor(v)));
            Set<LuaFunction> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            collectCompiletimeStatements(tr, tr.luaFunc.getFor(compiletimeRoot), compiletimeFunctions, objects,
                visited, new HashMap<>(), statements);
            // Some split parts can be dropped as empty/dead; every part still emitted must be reached by the root.
            for (LuaStatement s : model) {
                if (s instanceof LuaFunction f && compiletimeFunctions.contains(f) && !visited.contains(f)) {
                    throw new IllegalStateException("Unreached compile-time state function " + f.getName());
                }
            }
        }

        for (ImVar global : tr.prog.getGlobals()) {
            bindings.add(tr.luaVar.getFor(global).getName() + "=" + variableId(tr.imTr, global)
                + ":" + typeKey(global.getType()));
        }
        Set<ImVar> boundFields = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ImClass c : tr.prog.getClasses()) {
            bindings.add(tr.luaClassVar.getFor(c).getName() + "=" + semanticId(c, c.getName()));
            for (ImVar field : c.getFields()) {
                ImVar canonical = tr.imTr.canonical(field);
                if (!boundFields.add(canonical)) {
                    continue;
                }
                // With its type: objects which exist keep the values of the old one, which two class types
                // with the same nil default and storage would not reveal.
                bindings.add(tr.fieldStorage(field).getName() + "=" + fieldId(tr, canonical)
                    + ":" + typeKey(canonical.getType()));
            }
        }

        String abi = manifestAbi(compilerVersion, functions, statements, bindings,
            tr.imTr.compiletimeExpressionCounts(), tr.imTr.compiletimeIntegerReferences());
        appendRuntime(tr, model, abi);
        List<String> initOrder = new ArrayList<>();
        for (ImFunction init : packageInits) {
            String name = initNames.get(init);
            if (name != null) {
                initOrder.add(name);
            }
        }
        Map<String, Constant> constants = ConstantInitializers.analyse(tr.imTr, initializerOrder,
            f -> tr.luaFunc.getFor(f).getName());
        // Code may assign an element of a constant array: such storage holds the map's state as a variable does.
        Set<ImVar> writtenArrays = ConstantInitializers.writtenElsewhere(tr.imTr, tr.prog, initializerOrder);
        List<String> variables = new ArrayList<>();
        for (ImVar global : tr.prog.getGlobals()) {
            if (global.getTrace() instanceof GlobalVarDef g && !g.attrIsConstant() || writtenArrays.contains(global)) {
                variables.add(tr.luaVar.getFor(global).getName());
            }
        }
        // The hooks run in the order the map initialises their packages, each package's in the order it declares them.
        Map<WPackage, Integer> packageOrder = new HashMap<>();
        for (ImFunction init : packageInits) {
            if (init.getTrace() instanceof WPackage wp) {
                packageOrder.putIfAbsent(wp, packageOrder.size());
            }
        }
        List<ImFunction> hookFunctions = new ArrayList<>(tr.imTr.hotReloadHooks());
        hookFunctions.sort(Comparator
            .comparingInt((ImFunction f) -> f.getTrace().attrNearestPackage() instanceof WPackage wp
                ? packageOrder.getOrDefault(wp, Integer.MAX_VALUE) : Integer.MAX_VALUE)
            .thenComparing(f -> f.getTrace().attrNearestPackage() instanceof WPackage wp ? wp.getName() : "")
            .thenComparingInt(f -> f.getTrace().attrSource().getLeftPos()));
        List<String> hooks = new ArrayList<>();
        for (ImFunction hook : hookFunctions) {
            hooks.add(tr.luaFunc.getFor(hook).getName());
        }
        return new Manifest(abi, compilerVersion, Collections.unmodifiableMap(functions), List.copyOf(statements),
            List.copyOf(bindings), List.copyOf(initOrder), Collections.unmodifiableMap(constants), List.copyOf(variables),
            List.copyOf(hooks), closures(tr, reloadable, bootstrap), tr.imTr.compiletimeExpressionCounts(),
            tr.imTr.compiletimeIntegerReferences(), Collections.unmodifiableMap(new TreeMap<>(tr.imTr.compiletimeReferenceOwners())));
    }

    private static String manifestAbi(String compilerVersion, Map<String, Function> functions, List<Statement> statements,
        List<String> bindings, Map<String, Integer> compiletimeGroups, boolean integerReferences) {
        List<String> parts = new ArrayList<>();
        for (Function f : functions.values()) {
            parts.add(f.definition());
        }
        for (Statement s : statements) {
            parts.add(s.scope() + " " + s.text() + " " + s.targets());
        }
        compiletimeGroups.forEach((group, count) -> parts.add("compiletime expressions:" + group + "=" + count));
        parts.add("compiletime integer references:" + integerReferences);
        return fingerprint(compilerVersion, parts, bindings);
    }

    /** Closed scalar edits change definitions and their shapes, never declaration/initialisation metadata. */
    static Manifest replaceScalarDefinitions(Manifest previous, Map<String, String> replacements) {
        Map<String, Function> functions = new LinkedHashMap<>(previous.functions());
        replacements.forEach((name, definition) -> {
            Function old = Objects.requireNonNull(functions.get(name), "Unknown scalar function");
            functions.put(name, new Function(name, old.semanticId(), definition, old.slots(), old.captures(), old.kind(),
                identityHash(definition), old.closureSource()));
        });
        String abi = manifestAbi(previous.compilerVersion(), functions, previous.statements(), previous.bindings(),
            previous.compiletimeGroups(), previous.compiletimeIntegerReferences());
        return new Manifest(abi, previous.compilerVersion(), Collections.unmodifiableMap(functions), previous.statements(),
            previous.bindings(), previous.initOrder(), previous.constants(), previous.variables(), previous.hooks(),
            previous.closures(), previous.compiletimeGroups(), previous.compiletimeIntegerReferences(), previous.compiletimeReferenceOwners());
    }

    static String abiDefinition(String abi) { return ABI_GLOBAL + " = \"" + abi + "\""; }

    /** Expand the startup replay in execution order, retaining the object each statement initializes. */
    private static void collectCompiletimeStatements(LuaTranslator tr, LuaFunction function, Set<LuaFunction> parts,
            Set<LuaVariable> objects, Set<LuaFunction> visited, Map<String, Integer> ordinals, List<Statement> out) {
        if (!visited.add(function)) {
            throw new IllegalStateException("Repeated compile-time state function " + function.getName());
        }
        for (LuaStatement s : function.getBody()) {
            if (s instanceof LuaExprFunctionCall call && parts.contains(call.getFunc())) {
                collectCompiletimeStatements(tr, call.getFunc(), parts, objects, visited, ordinals, out);
                continue;
            }
            LuaVariable owner = compiletimeOwner(s, objects);
            StringBuilder text = new StringBuilder();
            s.print(text, 0);
            // Startup-only allocator state still distinguishes baselines, but must never execute in a payload.
            if (owner == null) {
                out.add(new Statement("compiletime", "startup:" + ordinals.merge("", 1, Integer::sum),
                    text.toString(), "startup", "", withoutPositions(tr, s), List.of()));
                continue;
            }
            String name = owner.getName();
            out.add(new Statement("compiletime", name + ":" + ordinals.merge(name, 1, Integer::sum), text.toString(),
                "global", name, withoutPositions(tr, s), List.of(name)));
        }
    }

    private static LuaVariable compiletimeOwner(de.peeeq.wurstscript.luaAst.Element e, Set<LuaVariable> objects) {
        if (e instanceof LuaExprVarAccess access && objects.contains(access.getVar())) {
            return access.getVar();
        }
        for (int i = 0; i < e.size(); i++) {
            LuaVariable found = compiletimeOwner(e.get(i), objects);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static Map<String, List<Statement>> compiletimeState(Manifest manifest) {
        Map<String, List<Statement>> result = new LinkedHashMap<>();
        for (Statement s : manifest.statements()) {
            if (s.scope().equals("compiletime") && s.kind().equals("global")) {
                result.computeIfAbsent(s.owner(), k -> new ArrayList<>()).add(s);
            }
        }
        return result;
    }

    private static String compiletimeRevision(List<Statement> statements) {
        return identityHash(String.join("\n", statements.stream().map(Statement::revision).toList()));
    }

    /**
     * New objects replay once, before source initializers read them. Keep the original interleaving: all allocations
     * precede field writes, including cycles. Per-statement markers allow retry after a failed write; the owner marker
     * freezes the complete snapshot once installed, so a later edit cannot partially overwrite runtime mutations.
     */
    private static void appendCompiletimeState(StringBuilder sb, Manifest baseline, Manifest current) {
        Map<String, List<Statement>> owners = compiletimeState(current);
        owners.keySet().removeAll(compiletimeState(baseline).keySet());
        if (owners.isEmpty()) {
            return;
        }
        sb.append("local compiletimeNew = {}\n");
        for (Map.Entry<String, List<Statement>> owner : owners.entrySet()) {
            String key = luaString("compiletime:" + owner.getKey());
            String marker = RAN_TABLE + "[" + key + "]";
            sb.append("compiletimeNew[").append(key).append("] = not ").append(marker).append("\n")
                .append("if ").append(marker).append(" and ").append(marker).append(" ~= ")
                .append(luaString(compiletimeRevision(owner.getValue()))).append(" then\n")
                .append(WARN_FUNCTION).append("(").append(luaString("Wurst hot reload: " + owner.getKey()
                    + " keeps its value; restart the map to apply changed compile-time state")).append(")\nend\n");
        }
        for (Statement s : current.statements()) {
            List<Statement> group = s.scope().equals("compiletime") ? owners.get(s.owner()) : null;
            if (group == null) {
                continue;
            }
            String key = luaString("compiletime:" + s.owner());
            sb.append("if compiletimeNew[").append(key).append("] then\n");
            appendOnce(sb, s);
            if (s == group.getLast()) {
                sb.append(RAN_TABLE).append("[").append(key).append("] = ")
                    .append(luaString(compiletimeRevision(group))).append("\n");
            }
            sb.append("end\n");
        }
    }

    /** The tables the objects of each closure keep their state in ({@link Closure}), by the closure's function. */
    private static Map<String, Closure> closures(LuaTranslator tr, Map<LuaFunction, ImFunction> reloadable,
                                                 LuaFunction bootstrap) {
        Set<LuaVariable> declared = Collections.newSetFromMap(new IdentityHashMap<>());
        for (LuaStatement statement : tr.luaModel) {
            if (statement instanceof LuaVariable v) {
                declared.add(v);
            }
        }
        Map<LuaVariable, Closure> byClassTable = new IdentityHashMap<>();
        Map<String, Closure> closures = new TreeMap<>();
        for (LuaStatement statement : tr.luaModel) {
            if (!(statement instanceof LuaFunction f) || !(reloadable.get(f) instanceof ImFunction im)
                || !(im.getTrace() instanceof ExprClosure) || !(im.getParent() != null
                && im.getParent().getParent() instanceof ImClass c)) {
                continue;
            }
            LuaVariable classTable = tr.luaClassVar.getFor(c);
            if (!declared.contains(classTable)) {
                continue;
            }
            Map<String, String> tables = new TreeMap<>();
            Map<String, String> fresh = new TreeMap<>();
            Map<String, String> captureTypes = new TreeMap<>();
            tables.put("class", classTable.getName());
            fresh.put(classTable.getName(), print(classTable.getInitialValue()));
            for (ImVar field : c.getFields()) {
                LuaVariable store = tr.fieldStorage(field);
                if (declared.contains(store)) {
                    tables.put("field:" + field.getName(), store.getName());
                    captureTypes.put("field:" + field.getName(), typeKey(field.getType()));
                    fresh.put(store.getName(), print(store.getInitialValue()));
                }
            }
            Closure closure = new Closure(Collections.unmodifiableMap(tables), Collections.unmodifiableMap(fresh),
                new ArrayList<>(), Collections.unmodifiableMap(captureTypes));
            byClassTable.put(classTable, closure);
            closures.put(f.getName(), closure);
        }
        if (bootstrap != null) {
            for (LuaStatement s : bootstrap.getBody()) {
                if (s instanceof LuaAssignment a && a.getLeft() instanceof LuaExprFieldAccess fa
                    && fa.getReceiver() instanceof LuaExprVarAccess table && byClassTable.get(table.getVar()) instanceof Closure c) {
                    // The key statement() gives it.
                    c.classData().add(print(a.getLeft()));
                }
            }
        }
        closures.replaceAll((name, c) -> new Closure(c.tables(), c.fresh(), List.copyOf(c.classData()), c.captureTypes()));
        return Collections.unmodifiableMap(closures);
    }

    /** An expression as the script has it; a declaration without an initial value is an empty table. */
    private static String print(de.peeeq.wurstscript.luaAst.Element e) {
        if (!(e instanceof LuaExpr expr)) {
            return "({})";
        }
        StringBuilder text = new StringBuilder();
        expr.print(text, 0);
        return text.toString();
    }

    /**
     * What {@code s} prints as with the source positions of its calls blanked ({@link LuaTranslator#stackPositions}): an
     * edit which only moves a line changes those, which is no change of the code.
     */
    private static String entryShape(LuaTranslator translator, LuaFunction function) {
        StringBuilder shape = new StringBuilder(function.getName());
        function.getParams().forEach(parameter -> shape.append(':').append(parameter.getName()));
        for (LuaStatement statement : function.getBody()) {
            if (!translator.isPackageInitDispatch(statement)) shape.append('\n').append(withoutPositions(translator, statement));
        }
        return identityHash(shape.toString());
    }

    private static String withoutPositions(LuaTranslator tr, LuaStatement s) {
        List<LuaExprStringVal> positions = new ArrayList<>();
        if (!tr.stackPositions.isEmpty()) {
            collectPositions(tr, s, positions);
        }
        List<String> values = new ArrayList<>();
        for (LuaExprStringVal position : positions) {
            values.add(position.getValS());
            position.setValS("");
        }
        try {
            StringBuilder text = new StringBuilder();
            s.print(text, 0);
            return text.toString();
        } finally {
            for (int i = 0; i < positions.size(); i++) {
                positions.get(i).setValS(values.get(i));
            }
        }
    }

    private static void collectPositions(LuaTranslator tr, de.peeeq.wurstscript.luaAst.Element e, List<LuaExprStringVal> out) {
        if (e instanceof LuaExprStringVal value && tr.stackPositions.contains(value)) {
            out.add(value);
        }
        for (int i = 0; i < e.size(); i++) {
            collectPositions(tr, e.get(i), out);
        }
    }

    private static Statement statement(LuaTranslator tr, String scope, String owner, LuaStatement s, Set<LuaVariable> globals,
                                       Map<LuaFunction, List<String>> initializerTargets, Map<LuaFunction, String> actions) {
        StringBuilder text = new StringBuilder();
        String revision = null;
        List<String> targets = List.of();
        String key;
        String kind = "other";
        if (s instanceof LuaVariable v) {
            // At the top level a variable is a global; printed as an assignment it runs in the payload as one.
            key = v.getName();
            text.append(v.getName()).append(" = ");
            if (v.getInitialValue() instanceof LuaExpr init) {
                init.print(text, 0);
            } else {
                text.append("nil");
            }
            kind = scope.equals("top") ? "global" : "other";
            if (!scope.equals("top")) {
                text.insert(0, "local ");
            }
        } else if (s instanceof LuaAssignment a) {
            StringBuilder left = new StringBuilder();
            a.getLeft().print(left, 0);
            key = left.toString();
            s.print(text, 0);
            revision = withoutPositions(tr, s);
            if (a.getLeft() instanceof LuaExprFieldAccess field && (a.getRight() instanceof LuaExprFuncRef
                || field.getFieldName().equals(ExprTranslation.WURST_SUPERTYPES))) {
                // A class's supertypes are read only by instanceof, when it runs, so a changed table is assigned again
                // as a slot is bound again: objects made before then answer for the class as it is now.
                kind = "slot";
            } else if (!scope.equals("init") && a.getLeft() instanceof LuaExprVarAccess target && globals.contains(target.getVar())) {
                kind = "global";
            }
        } else if (s instanceof LuaExprFunctionCall call && actions.containsKey(call.getFunc())) {
            key = call.getFunc().getName(); kind = "action";
            s.print(text, 0); revision = actions.get(call.getFunc());
        } else if (s instanceof LuaExprFunctionCall call && initializerTargets.containsKey(call.getFunc())) {
            // This is the complete source initializer, not an arbitrary assignment in a package's init block.
            key = call.getFunc().getName();
            kind = "global";
            s.print(text, 0);
            revision = withoutPositions(tr, call.getFunc());
            targets = initializerTargets.get(call.getFunc());
        } else {
            s.print(text, 0);
            key = text.toString();
            revision = withoutPositions(tr, s);
        }
        return new Statement(scope, key, text.toString(), kind, owner, revision == null ? text.toString() : revision, targets);
    }

    /** The payload which takes a map running {@code baseline} to {@code current}, which earlier payloads did not reach. */
    public static Payload payload(Manifest baseline, Manifest current, Set<String> reloadedBefore, int version)
        throws Rejected {
        return payload(baseline, current, new History(reloadedBefore, Map.of()), version);
    }

    /**
     * The payload which takes a map running {@code baseline}, which earlier payloads took as far as {@code history}
     * says, to {@code current}. Payloads are cumulative: every function which differs from the baseline, or which an
     * earlier payload defined, is defined again, so an edit which is undone is undone in the map too.
     */
    public static Payload payload(Manifest baseline, Manifest current, History history, int version) throws Rejected {
        List<String> reasons = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, String> baselineOwners = Objects.requireNonNullElse(baseline.compiletimeReferenceOwners(), Map.of());
        Map<String, String> oldOwners = new TreeMap<>(baselineOwners);
        Objects.requireNonNullElse(history.compiletimeReferenceOwners(), Map.<String, String>of()).forEach(oldOwners::putIfAbsent);
        Map<String, String> currentOwners = new TreeMap<>(Objects.requireNonNullElse(current.compiletimeReferenceOwners(), Map.of()));
        Map<String, String> introducedOwners = new TreeMap<>();
        for (Map.Entry<String, String> reference : currentOwners.entrySet()) {
            if (!baselineOwners.containsKey(reference.getKey())) {
                introducedOwners.put(reference.getKey(), reference.getValue());
            }
            String before = oldOwners.get(reference.getKey());
            if (before != null && !before.equals(reference.getValue())) {
                reasons.add("compile-time snapshot reference " + reference.getKey() + " now points to " + reference.getValue()
                    + " instead of " + before + "; restart the map to preserve shared live objects");
                break;
            }
        }
        if (current.compiletimeIntegerReferences()) {
            Set<String> newSnapshots = new TreeSet<>(compiletimeState(current).keySet());
            newSnapshots.removeAll(compiletimeState(baseline).keySet());
            if (!newSnapshots.isEmpty()) {
                reasons.add("compile-time evaluation read integer-encoded references; new snapshots " + newSnapshots
                    + " cannot preserve those identities with the live allocator: restart the map");
            }
        }
        Map<String, Integer> oldGroups = new TreeMap<>(Objects.requireNonNullElse(baseline.compiletimeGroups(), Map.of()));
        Objects.requireNonNullElse(history.compiletimeGroups(), Map.<String, Integer>of()).forEach(oldGroups::putIfAbsent);
        for (Map.Entry<String, Integer> group : Objects.requireNonNullElse(current.compiletimeGroups(), Map.<String, Integer>of()).entrySet()) {
            Integer before = oldGroups.get(group.getKey());
            if (before != null && !before.equals(group.getValue())) {
                reasons.add("compile-time expressions sharing " + group.getKey() + " changed count from " + before + " to "
                    + group.getValue() + "; their objects are told apart by order: restart the map");
            }
        }
        if (!Objects.equals(baseline.compilerVersion(), current.compilerVersion())) {
            reasons.add("the edit was compiled by another compiler (" + current.compilerVersion() + ") than the running script ("
                + baseline.compilerVersion() + ")");
            // A manifest of another compiler need not have the parts read below.
            throw new Rejected(reasons);
        }
        if (baseline.compiletimeReferenceOwners() == null && !compiletimeState(baseline).isEmpty()
            || current.compiletimeReferenceOwners() == null && !compiletimeState(current).isEmpty()
            || history.compiletimeReferenceOwners() == null && (!baselineOwners.isEmpty() || !currentOwners.isEmpty()
                || !Objects.requireNonNullElse(history.compiletimeGroups(), Map.of()).isEmpty())) {
            reasons.add("the session lacks compile-time snapshot reference ownership: restart the map");
        }
        Map<String, String> oldBindings = bindingMap(baseline.bindings());
        for (Map.Entry<String, String> b : bindingMap(current.bindings()).entrySet()) {
            String before = oldBindings.get(b.getKey());
            if (before != null && !before.equals(b.getValue())) {
                reasons.add("the name " + b.getKey() + " now means " + b.getValue() + " instead of " + before);
            }
        }
        // Declarations which share an identity are numbered in order. One added after the others keeps their order; a
        // move is found by its body below. Some removed while others remain gives the remaining ones the names of others,
        // and what was made from them would reach those; all of them removed leaves nothing to confuse. The closures, their
        // class tables and the stores of their captured values are matched by the Lineages below instead.
        Set<String> closureNames = new HashSet<>();
        for (Manifest m : List.of(baseline, current)) {
            Objects.requireNonNullElse(m.closures(), Map.<String, Closure>of()).forEach((name, c) -> {
                closureNames.add(name);
                closureNames.addAll(c.tables().values());
            });
        }
        Map<String, Integer> oldCounts = identityCounts(baseline.bindings());
        Map<String, Integer> newCounts = identityCounts(current.bindings());
        Map<String, Set<String>> oldNames = new HashMap<>();
        bindingMap(baseline.bindings()).forEach((name, id) -> oldNames.computeIfAbsent(id, k -> new HashSet<>()).add(name));
        Set<String> identities = new TreeSet<>(oldCounts.keySet());
        identities.addAll(newCounts.keySet());
        for (String id : identities) {
            int before = oldCounts.getOrDefault(id, 0);
            int after = newCounts.getOrDefault(id, 0);
            if (after < before && after > 0 && !closureNames.containsAll(oldNames.getOrDefault(id, Set.of()))) {
                reasons.add(before + " declarations share the identity " + id + " and are told apart by their order, and "
                    + (before - after) + " of them were removed while others remain: restart the map");
            }
        }
        // Closures: each one of the edit continues a closure of the running map, its lineage, whose objects keep their
        // class table and captured values, whatever name the edit gives it (Lineages).
        Lineages lineages = new Lineages(baseline, current, history, version, reasons, warnings);
        // Any other functions which share an identity are told apart by their place among those of it. An edit of a
        // body keeps the places; one which now has the body another one had, which that one lost, has moved, and what
        // was made from it at its old place would run the other one's body. A copy of a body another one keeps moved
        // nothing.
        Map<String, Set<String>> groups = orderGroups(baseline, current);
        Map<String, String> groupShapes = new TreeMap<>(lineages.shapes);
        for (Map.Entry<String, Set<String>> group : groups.entrySet()) {
            for (String name : group.getValue()) {
                if (lineages.isClosure(name)) {
                    continue;
                }
                Function now = current.functions().get(name);
                if (now == null) {
                    continue;
                }
                String source = sourceOf(now);
                groupShapes.put(name, source);
                if (knownShapes(name, baseline, history).contains(source)) {
                    continue;
                }
                for (String other : group.getValue()) {
                    Function otherNow = current.functions().get(other);
                    if (!other.equals(name) && !lineages.isClosure(other) && knownShapes(other, baseline, history).contains(source)
                        && (otherNow == null || !sourceOf(otherNow).equals(source))) {
                        reasons.add("closures which share the identity " + group.getKey() + " are told apart by their order,"
                            + " and " + name + " now has the body " + other + " had: they moved; restart the map");
                        break;
                    }
                }
            }
        }

        // What a running version stores which no function of the edit binds any more: a removed function stored as a
        // value (a registered callback, a closure object's class table), or a class-table slot nothing binds now. The
        // map keeps such a value with its old body, and only what it made before reaches it: a slot no class binds is
        // never dispatched by the new code, and a removed function is not called by it. So the payload applies, and
        // says what keeps the old body until the map restarts, as it does for package init it cannot run again.
        // A surviving function which abandons a slot is the same case: objects of a class which no longer binds that
        // slot run the body it held, and nothing else does. Only a function whose body is redefined here can differ
        // from what the slot holds, so one which stays as it was warns of nothing. Changing a function which remains
        // stored as a value is another case, refused below: the new code still reaches that value.
        Set<String> previousFunctions = new TreeSet<>(baseline.functions().keySet());
        previousFunctions.addAll(history.captured().keySet());
        previousFunctions.addAll(history.slots().keySet());
        Set<String> reboundSlots = new HashSet<>();
        current.functions().values().forEach(f -> reboundSlots.addAll(f.slots()));
        for (String name : previousFunctions) {
            Function old = baseline.functions().get(name);
            Function now = current.functions().get(name);
            boolean removed = now == null;
            if (!removed && old != null && old.definition().equals(now.definition()) && !history.reloaded().contains(name)) {
                // Not redefined: every slot it left still holds this very body.
                continue;
            }
            List<String> stored = new ArrayList<>(!removed || old == null ? List.of() : old.captures());
            if (removed && history.captured().containsKey(name)) {
                stored.add("by an earlier payload");
            }
            Set<String> retainedSlots = new LinkedHashSet<>(old == null ? List.of() : old.slots());
            retainedSlots.addAll(history.slots().getOrDefault(name, List.of()));
            retainedSlots.removeAll(reboundSlots);
            for (String slot : retainedSlots) {
                stored.add("in class-table slot " + slot);
            }
            if (!stored.isEmpty()) {
                warnings.add("function " + name + (removed ? " was removed" : " changed") + ", but what the map made before"
                    + " keeps its old body (" + String.join("; ", new LinkedHashSet<>(stored)) + "): restart the map to drop it.");
            }
        }

        List<String> defined = new ArrayList<>();
        List<String> changedInits = new ArrayList<>();
        Set<String> newInits = new LinkedHashSet<>();
        List<String> keptValues = new ArrayList<>();
        List<String> changedEntries = new ArrayList<>();
        for (Function f : current.functions().values()) {
            Function old = baseline.functions().get(f.name());
            boolean changed = old == null || !old.definition().equals(f.definition());
            if (!changed && !history.reloaded().contains(f.name())) {
                continue;
            }
            // Its new source positions ship anyway; what must not change, and what ran, is compared without them.
            boolean reshaped = old != null && !old.shape().equals(f.shape());
            List<String> stored = new ArrayList<>(f.captures());
            if (old != null) {
                stored.addAll(old.captures());
            }
            // A function stored by an earlier payload keeps the body it had then, which may not be the baseline's.
            String capturedAs = history.captured().get(f.name());
            boolean changedSinceStored = capturedAs != null && !capturedAs.equals(definitionHash(f));
            if (changedSinceStored) {
                stored.add("by an earlier payload");
            }
            if ((reshaped || changedSinceStored) && !stored.isEmpty()) {
                reasons.add("function " + f.name() + " is stored as a value (" + String.join(", ", new LinkedHashSet<>(stored))
                    + "), which keeps the old body");
            }
            defined.add(f.name());
            if (f.kind() == Kind.PACKAGE_INIT && old == null) {
                newInits.add(f.name());
            }
            if (f.kind() == Kind.ENTRY && reshaped) {
                changedEntries.add(f.name());
            }
        }
        Set<String> existingPackages = new HashSet<>(baseline.initOrder()); existingPackages.retainAll(current.initOrder());
        if (!baseline.initOrder().stream().filter(existingPackages::contains).toList()
            .equals(current.initOrder().stream().filter(existingPackages::contains).toList())) {
            warnings.add("Existing package initialization order changed; restart the map to apply that order.");
        }
        if (!changedEntries.isEmpty()) {
            warnings.add("Changed " + String.join(", ", changedEntries) + " ran when the map started and does not run again"
                + " (the payload starts new packages itself): restart the map to apply other changes to it.");
        }

        List<Statement> declarations = new ArrayList<>();
        List<Statement> initialisations = new ArrayList<>();
        Set<String> sentSlots = new LinkedHashSet<>(history.reboundSlots());
        Map<String, List<Statement>> packageValues = new LinkedHashMap<>();
        Map<String, String> currentInitializers = initializers(current);
        Map<String, String> baselineInitializers = initializers(baseline);
        for (String initializer : baselineInitializers.keySet()) {
            if (!currentInitializers.containsKey(initializer)) {
                keptValues.add(initializer + " (removed initializer)");
            }
        }
        Set<String> commonInitializers = new HashSet<>(baselineInitializers.keySet());
        commonInitializers.retainAll(currentInitializers.keySet());
        Map<String, Statement> oldActions = initActions(baseline), newActions = initActions(current);
        for (Statement oldAction : oldActions.values()) {
            Statement now = newActions.get(oldAction.key());
            if (now == null || !oldAction.revision().equals(now.revision())) {
                warnings.add("Initialization action " + oldAction.key() + " already ran; restart the map to "
                    + (now == null ? "undo its effects." : "apply its changed setup."));
            }
        }
        Set<String> commonActions = new HashSet<>(oldActions.keySet()); commonActions.retainAll(newActions.keySet());
        commonInitializers.addAll(commonActions);
        Map<String, List<String>> oldInitCode = initCode(baseline, commonInitializers);
        Map<String, List<String>> newInitCode = initCode(current, commonInitializers);
        for (Map.Entry<String, List<String>> init : oldInitCode.entrySet()) {
            if (!init.getValue().equals(newInitCode.getOrDefault(init.getKey(), List.of()))) {
                changedInits.add(init.getKey());
            }
        }
        Set<String> knownNames = bindingMap(baseline.bindings()).keySet();
        Map<String, List<Statement>> unitsByOwner = initializerUnits(current);
        Constants constants = new Constants(baseline, current, history);
        // In the order the map runs them: a constant which reads one that runs again runs after it.
        Set<String> definedNames = new HashSet<>(defined);
        Set<String> refreshed = new LinkedHashSet<>();
        for (String init : current.initOrder()) {
            for (Statement s : unitsByOwner.getOrDefault(init, List.of())) {
                // One a payload added runs again only after it ran once, as an added global (appendOnce).
                boolean runs = baselineInitializers.containsKey(s.key()) || !initializedBefore(s, knownNames);
                if (runs && constants.refreshable(s.key()) && (definedNames.contains(s.key())
                    || constants.calls(s.key()).stream().anyMatch(definedNames::contains)
                    || constants.reads(s.key()).stream().anyMatch(refreshed::contains))) {
                    refreshed.add(s.key());
                }
            }
        }
        for (String scope : List.of("top", "boot", "globals", "init")) {
            Map<String, Statement> oldByKey = new LinkedHashMap<>();
            Set<String> oldTexts = new HashSet<>();
            for (Statement s : baseline.statements()) {
                if (s.scope().equals(scope)) {
                    oldByKey.put(s.key(), s);
                    oldTexts.add(s.revision());
                }
            }
            for (Statement s : current.statements()) {
                if (!s.scope().equals(scope) || oldTexts.contains(s.revision())
                    && !(s.kind().equals("slot") && sentSlots.contains(runKey(s)))) {
                    continue;
                }
                Statement old = oldByKey.get(s.key());
                boolean slot = s.kind().equals("slot");
                if (slot) {
                    // Remember every binding sent, even when a later edit restores the baseline. A map may have
                    // installed any earlier payload, so that baseline assignment must keep shipping too.
                    sentSlots.add(runKey(s));
                }
                if (scope.equals("init") && s.kind().equals("global")) {
                    long existing = s.targets().stream().filter(knownNames::contains).count();
                    if (existing > 0 && existing < s.targets().size()) {
                        reasons.add("the initializer of " + initializerLabel(s)
                            + " spans old and new storage; restart the map");
                    }
                }
                if (scope.equals("init") && s.kind().equals("global") && old != null && !old.targets().equals(s.targets())) {
                    reasons.add("the storage initialized by " + initializerLabel(s) + " changed from " + old.targets()
                        + " to " + s.targets() + "; restart the map");
                    continue;
                }
                // A package introduced by an earlier payload is still new to the baseline. Its whole init
                // may already be complete, but later actions must be scheduled with their own completion markers.
                if (scope.equals("init") && s.kind().equals("action")) {
                    if (!oldActions.containsKey(s.key())) {
                        packageValues.computeIfAbsent(s.owner(), k -> new ArrayList<>()).add(s);
                    }
                    continue;
                }
                if (scope.equals("init") && newInits.contains(s.owner())) {
                    // The init of a new package runs as a whole, below, and records its initial values as run. One added
                    // to it after it ran runs on its own, after it.
                    if (s.kind().equals("global")) {
                        if (initializedBefore(s, knownNames)) {
                            if (!refreshed.contains(s.key())) {
                                keptValues.add(initializerLabel(s));
                            }
                        } else {
                            packageValues.computeIfAbsent(s.owner(), k -> new ArrayList<>()).add(s);
                        }
                    }
                    continue;
                }
                if (scope.equals("init")) {
                    // A package init ran when the map started. Of what changed in one, only the initial value of a global
                    // the running script does not have can run now, and that of a constant which runs again; the map
                    // keeps the values of the other globals it has. Ordinary code is compared as a sequence above and
                    // never rerun.
                    if (s.kind().equals("global")) {
                        if (initializedBefore(s, knownNames)) {
                            if (!refreshed.contains(s.key())) {
                                keptValues.add(initializerLabel(s));
                            }
                        } else {
                            packageValues.computeIfAbsent(s.owner(), k -> new ArrayList<>()).add(s);
                        }
                    }
                    continue;
                }
                if (old != null && !slot) {
                    if (s.kind().equals("global") && !scope.equals("top")) {
                        keptValues.add(s.key());
                    } else {
                        reasons.add("the running script's " + describe(scope) + " " + s.key() + " changed: was "
                            + firstLine(old.text()) + ", now " + firstLine(s.text()));
                    }
                    continue;
                }
                (scope.equals("top") ? declarations : initialisations).add(s);
            }
        }
        if (!changedInits.isEmpty()) {
            warnings.add("Changed package init code ran when the map started and does not run again ("
                + String.join(", ", changedInits) + "): restart the map to apply it.");
        }
        Map<String, List<Statement>> oldCompiletime = compiletimeState(baseline);
        for (Map.Entry<String, List<Statement>> owner : compiletimeState(current).entrySet()) {
            List<Statement> old = oldCompiletime.get(owner.getKey());
            if (old != null && !compiletimeRevision(old).equals(compiletimeRevision(owner.getValue()))) {
                keptValues.add(owner.getKey() + " (compile-time state)");
            }
        }
        if (!keptValues.isEmpty()) {
            warnings.add("Changed initial values (" + String.join(", ", keptValues) + ") do not apply: the running map keeps"
                + " the values it has. Restart the map to apply them.");
        }
        if (!reasons.isEmpty()) {
            throw new Rejected(reasons);
        }

        List<Statement> once = new ArrayList<>(declarations);
        for (Statement s : initialisations) {
            if (!s.kind().equals("slot")) {
                once.add(s);
            }
        }
        packageValues.values().forEach(once::addAll);

        StringBuilder sb = new StringBuilder();
        sb.append("-- Wurst hot reload payload ").append(version).append(": ").append(String.join(", ", defined)).append("\n");
        // The running script's fingerprint: the payload applies to that script, whatever the edit made of the rest.
        sb.append("return \"").append(baseline.abi()).append("\", ").append(version).append(", function()\n");
        // A cumulative payload may be made without delivery history. Check what this map installed before changing
        // anything; snapshot-reference keys cannot collide with the Lua identifiers this table otherwise records.
        for (Map.Entry<String, String> reference : introducedOwners.entrySet()) {
            String installed = BINDINGS_TABLE + "[" + luaString("snapshot-reference:" + reference.getKey()) + "]";
            sb.append("if ").append(installed).append(" and ").append(installed).append(" ~= ")
                .append(luaString(reference.getValue())).append(" then error(")
                .append(luaString("compile-time snapshot reference " + reference.getKey()
                    + " changed owner; restart the map to preserve shared live objects")).append(", 0) end\n");
        }
        Map<String, String> newBindings = new LinkedHashMap<>(bindingMap(current.bindings()));
        newBindings.keySet().removeAll(oldBindings.keySet());
        for (Map.Entry<String, String> binding : newBindings.entrySet()) {
            String installed = BINDINGS_TABLE + "[" + luaString(binding.getKey()) + "]";
            sb.append("if ").append(installed).append(" and ").append(installed).append(" ~= ")
                .append(luaString(binding.getValue())).append(" then error(")
                .append(luaString("an earlier payload installed another declaration or type for " + binding.getKey()
                    + ": restart the map")).append(", 0) end\n");
        }
        // Before anything changes: a declaration or class data an earlier payload installed which this edit changes
        // cannot be kept (a field store of another type), and the running script has no way to replace it.
        for (Statement s : once) {
            if (!s.kind().equals("action") && !keepsValue(s) && !s.text().startsWith("local ")) {
                String marker = RAN_TABLE + "[\"" + runKey(s) + "\"]";
                sb.append("if ").append(marker).append(" and ").append(marker).append(" ~= \"").append(identityHash(s.revision()))
                    .append("\" then error(").append(luaString("an earlier payload installed another version of the "
                        + describe(s.scope()) + " " + firstLine(s.key()) + ": restart the map")).append(", 0) end\n");
            }
        }
        // An indivisible source unit cannot initialize only its newly added specialization while retaining the old
        // ones. Check actual runtime bindings too: delivery history does not say which declarations were installed.
        // Storage the started map has is all old, so a unit with no other has nothing to check.
        for (Statement s : current.statements()) {
            if (s.scope().equals("init") && s.kind().equals("global") && s.targets().size() > 1
                && !knownNames.containsAll(s.targets())) {
                List<String> existing = s.targets().stream()
                    .map(target -> knownNames.contains(target) ? "true" : BINDINGS_TABLE + "[" + luaString(target) + "]")
                    .toList();
                sb.append("if (").append(String.join(" or ", existing)).append(") and not (")
                    .append(String.join(" and ", existing)).append(") then error(")
                    .append(luaString("the initializer of " + initializerLabel(s)
                        + " spans old and new storage; restart the map")).append(", 0) end\n");
            }
        }
        // Before recording this payload's declarations: a newly added initializer must not overwrite a global
        // which already existed, including one an earlier payload introduced without an initializer. An initializer
        // already installed but not reached after a failure remains retryable instead.
        for (Statement s : current.statements()) {
            if (s.scope().equals("init") && s.kind().equals("global") && !s.targets().isEmpty() && !knownNames.contains(s.key())) {
                List<String> existing = new ArrayList<>();
                for (String target : s.targets()) {
                    existing.add(knownNames.contains(target) ? "true" : BINDINGS_TABLE + "[" + luaString(target) + "]");
                }
                sb.append("if not ").append(BINDINGS_TABLE).append("[").append(luaString(s.key())).append("] and (")
                    .append(String.join(" or ", existing)).append(") then\n")
                    .append(KEPT_INITIALIZERS).append("[").append(luaString(runKey(s))).append("] = true\nend\n");
            }
        }
        // Reserve ownership before work which may fail partway through installation, like delivery history does.
        for (Map.Entry<String, String> reference : introducedOwners.entrySet()) {
            sb.append(BINDINGS_TABLE).append("[").append(luaString("snapshot-reference:" + reference.getKey())).append("] = ")
                .append(luaString(reference.getValue())).append("\n");
        }
        // Functions first: a new declaration may hold one (a metatable's __index). Then the declarations, which the
        // slots of a new class need, then the slots of what changed.
        for (String name : defined) {
            sb.append(current.functions().get(name).definition()).append("\n");
            appendBinding(sb, name, newBindings);
        }
        for (Statement s : declarations) {
            appendOnce(sb, s);
            appendBinding(sb, s.key(), newBindings);
        }
        // After the declarations, which make the tables of new names, and before the slots are bound: the names of the
        // closures of a group whose names moved now hold the tables of the closures the edit continues.
        lineages.appendTables(sb, current);
        for (String name : defined) {
            for (String slot : current.functions().get(name).slots()) {
                sb.append(slot).append(" = ").append(name).append("\n");
            }
        }
        for (Statement s : initialisations) {
            if (s.kind().equals("slot")) {
                // Binding a slot again changes nothing, and one changed back must be bound again.
                sb.append(s.text()).append("\n");
            } else {
                appendOnce(sb, s);
            }
            appendBinding(sb, s.key(), newBindings);
        }
        appendCompiletimeState(sb, baseline, current);
        // All package work shares main's dependency order. A new package may depend on a new global in an existing
        // package, and an existing package's new global may depend on a new package (including an earlier payload's).
        for (String init : current.initOrder()) {
            if (newInits.contains(init)) {
                // A failed init fails the install and is not recorded: the next payload runs it again.
                String marker = RAN_TABLE + "[\"init:" + init + "\"]";
                // The map knows whether this package ran; the editor only knows which payload files it delivered.
                // Keep its definition's hash so a later edit can diagnose the setup code it cannot run again.
                String initCode = definitionHash(current.functions().get(init));
                sb.append("if not ").append(marker).append(" then\n");
                sb.append("if not xpcall(").append(init).append(", __wurst_init_error or function(e) BJDebugMsg(tostring(e)) end) then\n");
                sb.append("error(\"the init of a new package (").append(init).append(") failed; the next payload runs it again\", 0)\n");
                sb.append("end\n");
                sb.append(marker).append(" = \"").append(initCode).append("\"\n");
                for (Statement s : current.statements()) {
                    if (s.owner().equals(init) && s.kind().equals("global")) {
                        sb.append("if not ").append(KEPT_INITIALIZERS).append("[").append(luaString(runKey(s))).append("] then\n");
                        sb.append(RAN_TABLE).append("[\"").append(runKey(s)).append("\"] = \"").append(identityHash(s.revision()))
                            .append("\"\n");
                        appendConstantValue(sb, s, constants.currentRevision(s.key()), constants.length(s.key()));
                        sb.append("end\n");
                    }
                }
                sb.append("elseif ").append(marker).append(" ~= \"").append(initCode).append("\" then\n")
                    .append(WARN_FUNCTION + "(").append(luaString("Wurst hot reload: " + init
                        + " already ran; restart the map to apply changed package init code"
                        + " beyond the added globals initialized separately")).append(")\nend\n");
            }
            // If the whole init just ran, its globals are marked already. Otherwise only the values it has not yet
            // initialised run here, and the constants which run again, before any dependent package's work.
            List<Statement> values = packageValues.getOrDefault(init, List.of());
            for (Statement s : unitsByOwner.getOrDefault(init, List.of())) {
                if (values.contains(s)) {
                    if (s.kind().equals("action")) appendInitAction(sb, s);
                    else appendOnce(sb, s, constants.currentRevision(s.key()), constants.length(s.key()));
                }
                if (refreshed.contains(s.key())) {
                    appendRefresh(sb, s, constants.currentRevision(s.key()), constants.baselineRevision(s.key()),
                        constants.length(s.key()), constants.baselineLength(s.key()));
                }
            }
        }
        for (String initializer : new TreeSet<>(history.initializers().keySet())) {
            if (!currentInitializers.containsKey(initializer) && !baselineInitializers.containsKey(initializer)) {
                sb.append("if ").append(RAN_TABLE).append("[").append(luaString(history.initializers().get(initializer))).append("] then\n")
                    .append(WARN_FUNCTION + "(").append(luaString("Wurst hot reload: removed initializer " + initializer
                        + " keeps its value; restart the map to apply the removal")).append(")\nend\n");
            }
        }
        for (String init : new TreeSet<>(history.packageInits())) {
            if (!baseline.functions().containsKey(init) && !current.functions().containsKey(init)) {
                sb.append("if ").append(RAN_TABLE).append("[").append(luaString("init:" + init)).append("] then\n")
                    .append(WARN_FUNCTION + "(").append(luaString("Wurst hot reload: removed package init " + init
                        + " already ran; restart the map to apply its removal")).append(")\nend\n");
            }
        }
        List<String> initializerOrder = initializerOrder(current);
        List<String> startOrder = initializerOrder(baseline);
        if (!swapsAny(startOrder, initializerOrder)) {
            // The started map ran its initializers in the baseline's order, and this one keeps it: only those payloads
            // added can have run in another. Listing every initializer made most of each payload of a large map.
            initializerOrder.removeAll(new HashSet<>(startOrder));
        }
        if (initializerOrder.size() > 1) {
            // Only initializers which first ran together (when the map started, or in one payload) have an order to keep:
            // one a payload added runs after every one which ran before it, wherever it is in the source.
            // Run keys are fixed-width hex hashes. Packing them avoids the quotes/separators and a temporary Lua
            // array of every key, while retaining every entry and its exact order, including absent initializers.
            sb.append("local previous = {}\nfor key in string.gmatch(").append(luaString(String.join("", initializerOrder)))
                .append(", ").append(luaString(".".repeat(initializerOrder.getFirst().length()))).append(") do\n")
                .append("local position = ").append(INITIALIZER_POSITIONS).append("[key]\nif position then\n")
                .append("local batch = ").append(INITIALIZER_BATCHES).append("[key]\n")
                .append("if previous[batch] and position < previous[batch] then\n").append(WARN_FUNCTION).append("(")
                .append(luaString("Wurst hot reload: global initializer order changed; the map keeps the order its initializers ran in; restart the map to apply the new order"))
                .append(")\nbreak\nend\nprevious[batch] = position\nend\nend\n");
        }
        // Last, with everything installed: the reload hooks, with the bodies this payload has. One which fails does not
        // stop the others; what it raised is in the status.
        for (String hook : Objects.requireNonNullElse(current.hooks(), List.<String>of())) {
            sb.append(HOOK_FUNCTION).append("(").append(hook).append(", ").append(luaString(hook)).append(")\n");
        }
        sb.append("end\n");
        Map<String, String> captured = new LinkedHashMap<>();
        Map<String, List<String>> slots = new LinkedHashMap<>();
        Set<String> packageInits = new LinkedHashSet<>();
        for (Function f : current.functions().values()) {
            if (!f.captures().isEmpty()) {
                captured.put(f.name(), definitionHash(f));
            }
            if (!f.slots().isEmpty()) {
                slots.put(f.name(), f.slots());
            }
            if (f.kind() == Kind.PACKAGE_INIT) {
                packageInits.add(f.name());
            }
        }
        return new Payload(version, List.copyOf(defined), sb.toString(), List.copyOf(warnings),
            Collections.unmodifiableMap(captured), Collections.unmodifiableMap(slots), Collections.unmodifiableMap(currentInitializers),
            Collections.unmodifiableSet(packageInits), Collections.unmodifiableSet(constants.newVariables()),
            Collections.unmodifiableMap(groupShapes), List.copyOf(lineages.layout),
            Collections.unmodifiableMap(lineages.newOrigins), Collections.unmodifiableSet(lineages.permuted), Collections.unmodifiableSet(sentSlots),
            Collections.unmodifiableMap(lineages.types), Collections.unmodifiableMap(lineages.readers),
            Objects.requireNonNullElse(current.compiletimeGroups(), Map.of()), Collections.unmodifiableMap(introducedOwners));
    }

    /**
     * Which closure of the running map each closure of the edited program continues: its lineage. Closures of one
     * interface method in one function with no binding of their own share an identity, so their names are numbered in
     * the order of the source (an order group), and one inserted before others gives each of them the name of the one
     * before it. The build stays as it is, a function of the source alone; the payload, which is made for the running
     * map anyway, carries the difference. Each object holds its class table, not the name of it, so the payload gives
     * each name of the group the tables (class table, stores of captured values) of the closure it now names, and every
     * object made before keeps running the closure it was made from, with the values it captured.
     *
     * <p>The edit's closures of a group are matched with those the last payload sent (or the started map's): those of a
     * source which only one has on either side are the same closure, wherever they are; one whose source is new takes
     * the place of one which lost its source between the same unchanged ones, as an edited body; and one which has no such
     * place is a new closure. Closures both added and changed in one gap cannot be told apart (an insertion and an edit,
     * or two edits and an append), including after the last unchanged one, and the edit is refused.
     *
     * <p>The map keeps the tables by lineage in {@code __wurst_hotreload_lineages}, taken from the names the closure had
     * when it began (the started map's, or those of the payload which added it) before any name is given another table,
     * so a payload applies to a map whatever earlier payloads it installed.
     */
    private static final class Lineages {
        private static final String TABLE = "__wurst_hotreload_lineages";

        /** The closure functions of either program. */
        private final Set<String> closures = new HashSet<>();
        /** The last source of each closure of an order group, by lineage. */
        final Map<String, String> shapes = new TreeMap<>();
        final List<GroupMember> layout = new ArrayList<>();
        /** Where the closures this payload adds began. */
        final Map<String, Map<String, String>> newOrigins = new TreeMap<>();
        final Map<String, Map<String, String>> types = new TreeMap<>();
        /** Every lineage whose emitted body may still read a table name, including removed, previously remapped ones. */
        final Map<String, Set<String>> readers = new TreeMap<>();
        /** The groups whose names hold other tables than those the started map gave them. */
        final Set<String> permuted = new TreeSet<>();
        private final Map<String, String> lineageOf = new HashMap<>();
        private final Map<String, Map<String, String>> origins = new TreeMap<>();
        private final Map<String, List<String>> membersByGroup = new TreeMap<>();

        Lineages(Manifest baseline, Manifest current, History history, int version, List<String> reasons,
                 List<String> warnings) {
            Map<String, Closure> baseClosures = Objects.requireNonNullElse(baseline.closures(), Map.of());
            Map<String, Closure> curClosures = Objects.requireNonNullElse(current.closures(), Map.of());
            closures.addAll(baseClosures.keySet());
            closures.addAll(curClosures.keySet());
            Map<String, String> baseIds = bindingMap(baseline.bindings());
            Map<String, String> curIds = bindingMap(current.bindings());
            List<GroupMember> lastLayout = Objects.requireNonNullElse(history.groupLayout(), List.of());
            Map<String, Map<String, String>> knownOrigins = Objects.requireNonNullElse(history.lineageOrigins(), Map.of());
            Map<String, List<String>> knownShapes = Objects.requireNonNullElse(history.groupShapes(), Map.of());
            if ((!knownOrigins.isEmpty() || !Objects.requireNonNullElse(history.permutedGroups(), Set.of()).isEmpty())
                && Objects.requireNonNullElse(history.tableReaders(), Map.of()).isEmpty()) {
                reasons.add("the session lacks historical closure table readers: restart the map");
            }
            Objects.requireNonNullElse(history.tableReaders(), Map.<String, Set<String>>of())
                .forEach((name, lineages) -> readers.put(name, new TreeSet<>(lineages)));
            baseClosures.forEach((lineage, closure) -> closure.tables().values().forEach(name ->
                readers.computeIfAbsent(name, k -> new TreeSet<>()).add(lineage)));
            if (!knownOrigins.isEmpty() && Objects.requireNonNullElse(history.lineageTypes(), Map.of()).isEmpty()) {
                reasons.add("the session lacks the captured types of earlier closure lineages: restart the map");
            }
            baseClosures.forEach((name, closure) -> {
                if (closure.captureTypes() == null && closure.tables().keySet().stream().anyMatch(k -> k.startsWith("field:"))) {
                    reasons.add("the baseline lacks the captured types of closure " + name + ": restart the map");
                }
            });
            Objects.requireNonNullElse(history.lineageTypes(), Map.<String, Map<String, String>>of())
                .forEach((lineage, fields) -> types.put(lineage, new TreeMap<>(fields)));
            baseClosures.forEach((lineage, closure) -> types.computeIfAbsent(lineage, k -> new TreeMap<>())
                .putAll(Objects.requireNonNullElse(closure.captureTypes(), Map.of())));

            // What the last payload sent, or else the started map: the closures to match, by group, in order.
            Map<String, List<GroupMember>> before = new TreeMap<>();
            if (history.groupLayout() != null) {
                for (GroupMember m : lastLayout) {
                    before.computeIfAbsent(m.group(), k -> new ArrayList<>()).add(m);
                }
            } else {
                for (Function f : baseline.functions().values()) {
                    if (baseClosures.containsKey(f.name())) {
                        before.computeIfAbsent(baseIds.get(f.name()), k -> new ArrayList<>())
                            .add(new GroupMember(baseIds.get(f.name()), f.name(), f.name(), sourceOf(f)));
                    }
                }
            }
            Map<String, List<Function>> now = new TreeMap<>();
            for (Function f : current.functions().values()) {
                if (curClosures.containsKey(f.name())) {
                    now.computeIfAbsent(curIds.get(f.name()), k -> new ArrayList<>()).add(f);
                }
            }
            // Lineages already taken: a new closure takes its own name as its lineage when it is free.
            Set<String> taken = new HashSet<>(baseClosures.keySet());
            taken.addAll(knownOrigins.keySet());
            before.values().forEach(ms -> ms.forEach(m -> taken.add(m.lineage())));
            // A name used as a remapping destination may hold a survivor's table even when no lineage began there.
            // A new closure must not adopt any table name earlier bodies have read; it makes a fresh table instead.
            Set<String> occupied = new HashSet<>(readers.keySet());
            baseClosures.values().forEach(c -> occupied.addAll(c.tables().values()));
            knownOrigins.values().forEach(o -> occupied.addAll(o.values()));
            for (Map.Entry<String, Closure> c : baseClosures.entrySet()) {
                origins.put(c.getKey(), c.getValue().tables());
            }
            origins.putAll(knownOrigins);
            // Which closure began with each table, and the tables new closures take which another one began with.
            Map<String, String> beganWith = new HashMap<>();
            origins.forEach((lineage, tables) -> tables.values().forEach(t -> beganWith.putIfAbsent(t, lineage)));
            // The closures the map may still make objects of: those the last payload sent, or the started map's.
            Set<String> live = new HashSet<>();
            before.values().forEach(ms -> ms.forEach(m -> live.add(m.lineage())));

            Set<String> groupIds = new TreeSet<>(before.keySet());
            groupIds.addAll(now.keySet());
            for (String group : groupIds) {
                List<GroupMember> p = before.getOrDefault(group, List.of());
                List<Function> n = now.getOrDefault(group, List.of());
                List<String> pSources = p.stream().map(GroupMember::source).toList();
                List<String> nSources = n.stream().map(LuaHotReload::sourceOf).toList();
                Match match = Match.of(pSources, nSources);
                if (match == null) {
                    reasons.add("closures which share the identity " + group + " are told apart by their order, and between"
                        + " the same unchanged ones the edit both adds or removes some and changes others, which cannot be"
                        + " told apart: make the edits one at a time, or restart the map");
                    continue;
                }
                List<String> members = new ArrayList<>();
                for (int i = 0; i < n.size(); i++) {
                    Function f = n.get(i);
                    String lineage;
                    boolean continued = true;
                    String removed = beganWith.get(curClosures.get(f.name()).tables().get("class"));
                    if (match.pairs()[i] >= 0) {
                        lineage = p.get(match.pairs()[i]).lineage();
                    } else if (removed != null && !live.contains(removed) && known(removed, baseline, knownShapes).contains(sourceOf(f))) {
                        // A closure an earlier edit removed is back where it was, as it was.
                        lineage = removed;
                        live.add(removed);
                    } else {
                        continued = false;
                        lineage = taken.add(f.name()) ? f.name() : f.name() + "@" + version;
                        taken.add(lineage);
                        if (!origins.containsKey(lineage)) {
                            Map<String, String> origin = new TreeMap<>();
                            for (Map.Entry<String, String> table : curClosures.get(f.name()).tables().entrySet()) {
                                if (occupied.add(table.getValue())) {
                                    origin.put(table.getKey(), table.getValue());
                                }
                            }
                            origins.put(lineage, Collections.unmodifiableMap(origin));
                            newOrigins.put(lineage, Collections.unmodifiableMap(origin));
                        }
                    }
                    lineageOf.put(f.name(), lineage);
                    for (String table : curClosures.get(f.name()).tables().values()) {
                        readers.computeIfAbsent(table, k -> new TreeSet<>()).add(lineage);
                    }
                    Map<String, String> capturedTypes = types.computeIfAbsent(lineage, k -> new TreeMap<>());
                    Set<String> addedCaptures = new TreeSet<>();
                    for (Map.Entry<String, String> field : curClosures.get(f.name()).captureTypes().entrySet()) {
                        String previousType = capturedTypes.putIfAbsent(field.getKey(), field.getValue());
                        if (continued && previousType == null) {
                            addedCaptures.add(field.getKey());
                        }
                        if (previousType != null && !previousType.equals(field.getValue())) {
                            reasons.add("closure " + f.name() + " captures " + field.getKey() + " as " + field.getValue()
                                + ", which the closure it continues captured as " + previousType + "; restart the map");
                        }
                    }
                    if (!addedCaptures.isEmpty()) {
                        warnings.add("closure " + f.name() + " now captures " + String.join(", ", addedCaptures)
                            + "; objects made before these captures were added read their typed default: restart the map"
                            + " to reconstruct them with captured values.");
                    }
                    members.add(f.name());
                    layout.add(new GroupMember(group, f.name(), lineage, sourceOf(f)));
                    shapes.put(lineage, sourceOf(f));
                }
                membersByGroup.put(group, members);
                boolean moves = Objects.requireNonNullElse(history.permutedGroups(), Set.<String>of()).contains(group);
                for (String name : members) {
                    Map<String, String> origin = origins.getOrDefault(lineageOf.get(name), Map.of());
                    for (Map.Entry<String, String> table : curClosures.get(name).tables().entrySet()) {
                        String began = origin.get(table.getKey());
                        moves |= !table.getValue().equals(began);
                    }
                }
                if (moves) {
                    permuted.add(group);
                }
                // A removed closure keeps its tables and its last body, which reads its captured values by the names it
                // had: only while no other closure of its group takes them is that the warning of a removed function.
                for (int removed : match.removed()) {
                    GroupMember m = p.get(removed);
                    if (moves && !members.isEmpty()) {
                        reasons.add("closures which share the identity " + group + " are told apart by their order, and "
                            + m.name() + " was removed while the others' names changed: restart the map");
                    }
                }
                // A closure which now has a source another one had, which that one lost, moved: the map may run either.
                for (String name : members) {
                    String lineage = lineageOf.get(name);
                    String source = shapes.get(lineage);
                    if (known(lineage, baseline, knownShapes).contains(source)) {
                        continue;
                    }
                    Set<String> others = new TreeSet<>();
                    p.forEach(m -> others.add(m.lineage()));
                    members.forEach(m -> others.add(lineageOf.get(m)));
                    for (String other : others) {
                        boolean kept = members.stream().anyMatch(m -> lineageOf.get(m).equals(other) && source.equals(shapes.get(other)));
                        if (!other.equals(lineage) && known(other, baseline, knownShapes).contains(source) && !kept) {
                            reasons.add("closures which share the identity " + group + " are told apart by their order,"
                                + " and " + name + " now has the body " + other + " had: they moved; restart the map");
                            break;
                        }
                    }
                }
            }
            refuseNamesOfRemovedClosures(curClosures, reasons);
            types.replaceAll((lineage, fields) -> Collections.unmodifiableMap(new TreeMap<>(fields)));
            readers.replaceAll((name, lineages) -> Collections.unmodifiableSet(new TreeSet<>(lineages)));
        }

        /**
         * A closure removed by an earlier edit keeps its tables and its last body, which reads its captured values by the
         * names it had. A new closure which takes those names would give that body the new closure's values.
         */
        private void refuseNamesOfRemovedClosures(Map<String, Closure> current, List<String> reasons) {
            Set<String> live = new HashSet<>(lineageOf.values());
            // appendTables assigns every current name in a permuted group, even when the destination happens to be
            // its original name. An absent lineage may still execute any body a delivered payload gave its objects.
            for (String group : permuted) {
                for (String name : membersByGroup.getOrDefault(group, List.of())) {
                    for (String table : current.get(name).tables().values()) {
                        for (String reader : readers.getOrDefault(table, Set.of())) {
                            if (!live.contains(reader)) {
                                reasons.add("closure " + name + " takes " + table + ", which closure " + reader
                                    + " had before an earlier edit removed it, and what the map made from that one still reads it:"
                                    + " restart the map");
                            }
                        }
                    }
                }
            }
        }

        private static Set<String> known(String lineage, Manifest baseline, Map<String, List<String>> history) {
            Set<String> shapes = new HashSet<>(history.getOrDefault(lineage, List.of()));
            Function old = baseline.functions().get(lineage);
            if (old != null) {
                shapes.add(sourceOf(old));
            }
            return shapes;
        }

        boolean isClosure(String name) {
            return closures.contains(name);
        }

        /**
         * Gives each name of a group whose names moved the tables of the closure it now names: every lineage takes its
         * tables from the names it began with, unless the map took them before, then every name gets its lineage's,
         * and the class data of each class table is written again (its slots bind the functions of its new name).
         */
        void appendTables(StringBuilder sb, Manifest current) {
            if (permuted.isEmpty()) {
                return;
            }
            Map<String, Closure> curClosures = current.closures();
            Map<String, Statement> boot = new HashMap<>();
            for (Statement s : current.statements()) {
                if (s.scope().equals("boot")) {
                    boot.put(s.key(), s);
                }
            }
            sb.append("local lineages = ").append(TABLE).append("\nif not lineages then lineages = {} ").append(TABLE)
                .append(" = lineages end\n");
            for (String group : permuted) {
                for (String name : membersByGroup.getOrDefault(group, List.of())) {
                    String lineage = lineageOf.get(name);
                    Map<String, String> origin = origins.getOrDefault(lineage, Map.of());
                    Closure closure = curClosures.get(name);
                    for (Map.Entry<String, String> table : closure.tables().entrySet()) {
                        String key = "lineages[" + luaString(lineage + "\n" + table.getKey()) + "]";
                        String began = origin.get(table.getKey());
                        sb.append("if ").append(key).append(" == nil then ").append(key).append(" = ")
                            .append(began == null ? "" : began + " or ").append(closure.fresh().get(table.getValue()))
                            .append(" end\n");
                    }
                }
            }
            for (String group : permuted) {
                for (String name : membersByGroup.getOrDefault(group, List.of())) {
                    String lineage = lineageOf.get(name);
                    for (Map.Entry<String, String> table : curClosures.get(name).tables().entrySet()) {
                        sb.append(table.getValue()).append(" = lineages[")
                            .append(luaString(lineage + "\n" + table.getKey())).append("]\n");
                    }
                }
            }
            for (String group : permuted) {
                for (String name : membersByGroup.getOrDefault(group, List.of())) {
                    for (String key : curClosures.get(name).classData()) {
                        Statement s = boot.get(key);
                        if (s != null) {
                            sb.append(s.text()).append("\n");
                        }
                    }
                }
            }
        }
    }

    /**
     * How the closures of a group in one program continue those of another, by their sources in order: {@code pairs}
     * gives for each closure of the new program the index of the one it continues, or -1 for a new one; {@code removed}
     * the indices of the old ones none continues.
     */
    record Match(int[] pairs, List<Integer> removed) {
        /** The match, or null when closures both added (or removed) and changed between the same unchanged ones. */
        static Match of(List<String> before, List<String> now) {
            int[] pairs = new int[now.size()];
            Arrays.fill(pairs, -1);
            boolean[] taken = new boolean[before.size()];
            Map<String, Integer> beforeCounts = new HashMap<>();
            Map<String, Integer> nowCounts = new HashMap<>();
            before.forEach(s -> beforeCounts.merge(s, 1, Integer::sum));
            now.forEach(s -> nowCounts.merge(s, 1, Integer::sum));
            // A source which only one closure has on either side: the same closure, wherever it is now.
            List<int[]> anchors = new ArrayList<>();
            for (int i = 0; i < before.size(); i++) {
                String s = before.get(i);
                if (beforeCounts.get(s) == 1 && nowCounts.getOrDefault(s, 0) == 1) {
                    int j = now.indexOf(s);
                    pairs[j] = i;
                    taken[i] = true;
                    anchors.add(new int[] {i, j});
                }
            }
            boolean inOrder = true;
            for (int k = 1; k < anchors.size(); k++) {
                inOrder &= anchors.get(k)[1] > anchors.get(k - 1)[1];
            }
            List<Integer> removed = new ArrayList<>();
            // The rest, between the same unchanged ones; when those moved past each other, all the rest at once.
            int gaps = inOrder ? anchors.size() + 1 : 1;
            for (int g = 0; g < gaps; g++) {
                int fromBefore = !inOrder || g == 0 ? 0 : anchors.get(g - 1)[0] + 1;
                int toBefore = !inOrder || g == anchors.size() ? before.size() : anchors.get(g)[0];
                int fromNow = !inOrder || g == 0 ? 0 : anchors.get(g - 1)[1] + 1;
                int toNow = !inOrder || g == anchors.size() ? now.size() : anchors.get(g)[1];
                List<Integer> left = new ArrayList<>();
                List<Integer> right = new ArrayList<>();
                for (int i = fromBefore; i < toBefore; i++) {
                    if (!taken[i]) {
                        left.add(i);
                    }
                }
                for (int j = fromNow; j < toNow; j++) {
                    if (pairs[j] < 0) {
                        right.add(j);
                    }
                }
                // With no count change, body edits keep their places. Matching a surviving copy first would swap
                // [A, A] -> [B, A], giving the edit to the second callback's existing objects and captures.
                if (left.size() == right.size()) {
                    for (int k = 0; k < left.size(); k++) {
                        pairs[right.get(k)] = left.get(k);
                    }
                    continue;
                }
                // Copies of one source: match unchanged ones while finding additions or removals.
                for (Iterator<Integer> it = left.iterator(); it.hasNext(); ) {
                    int i = it.next();
                    for (Iterator<Integer> jt = right.iterator(); jt.hasNext(); ) {
                        int j = jt.next();
                        if (before.get(i).equals(now.get(j))) {
                            pairs[j] = i;
                            it.remove();
                            jt.remove();
                            break;
                        }
                    }
                }
                if (left.isEmpty()) {
                    continue;
                } else if (right.isEmpty()) {
                    removed.addAll(left);
                } else {
                    return null;
                }
            }
            return new Match(pairs, List.copyOf(removed));
        }
    }

    /** The fixed-width run keys of the source initializers, in the order the map runs them. */
    private static List<String> initializerOrder(Manifest manifest) {
        Map<String, List<String>> keysByOwner = new LinkedHashMap<>();
        for (Statement s : manifest.statements()) {
            if (s.scope().equals("init") && s.kind().equals("global")) {
                keysByOwner.computeIfAbsent(s.owner(), k -> new ArrayList<>()).add(runKey(s));
            }
        }
        List<String> order = new ArrayList<>();
        for (String init : manifest.initOrder()) {
            order.addAll(keysByOwner.getOrDefault(init, List.of()));
        }
        return order;
    }

    /** Whether {@code now} puts two of the keys {@code before} has in another order. */
    private static boolean swapsAny(List<String> before, List<String> now) {
        Map<String, Integer> positions = new HashMap<>();
        for (String key : before) {
            positions.putIfAbsent(key, positions.size());
        }
        int last = -1;
        for (String key : now) {
            Integer position = positions.get(key);
            if (position != null) {
                if (position < last) {
                    return true;
                }
                last = position;
            }
        }
        return false;
    }

    private static Map<String, String> initializers(Manifest manifest) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Statement s : manifest.statements()) {
            if (s.scope().equals("init") && s.kind().equals("global")) {
                result.put(s.key(), runKey(s));
            }
        }
        return result;
    }

    /** Init code is a sequence. Existing initializer units retain their positions without comparing their bodies,
     * whose changed values have their own warning; added and removed units are handled separately. */
    private static Map<String, Statement> initActions(Manifest manifest) {
        Map<String, Statement> result = new LinkedHashMap<>();
        for (Statement statement : manifest.statements()) {
            if (statement.scope().equals("init") && statement.kind().equals("action")) result.put(statement.key(), statement);
        }
        return result;
    }

    private static void appendInitAction(StringBuilder out, Statement action) {
        String marker = RAN_TABLE + "[" + luaString(runKey(action)) + "]";
        out.append("if not ").append(marker).append(" then\n").append(action.text()).append("\n")
            .append("elseif ").append(marker).append(" ~= ").append(luaString(identityHash(action.revision()))).append(" then\n")
            .append(WARN_FUNCTION).append("(").append(luaString("Wurst hot reload: initialization action " + action.key()
                + " already ran; restart the map to apply changed setup.")).append(")\nend\n");
    }

    private static Map<String, List<String>> initCode(Manifest manifest, Set<String> commonInitializers) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Function f : manifest.functions().values()) {
            if (f.kind() == Kind.PACKAGE_INIT) {
                result.put(f.name(), new ArrayList<>());
            }
        }
        for (Statement s : manifest.statements()) {
            if (s.scope().equals("init") && (!(s.kind().equals("global") || s.kind().equals("action")) || commonInitializers.contains(s.key()))) {
                result.computeIfAbsent(s.owner(), k -> new ArrayList<>())
                    .add((s.kind().equals("global") || s.kind().equals("action")) ? "initializer:" + s.key() : "code:" + s.revision());
            }
        }
        return result;
    }

    /** What tells a function's definitions apart, apart from source positions ({@link Function#shape}), for {@link History#captured}. */
    public static String definitionHash(Function f) {
        return f.shape();
    }

    private static void appendBinding(StringBuilder sb, String name, Map<String, String> bindings) {
        String identity = bindings.get(name);
        if (identity != null) {
            sb.append(BINDINGS_TABLE).append("[").append(luaString(name)).append("] = ")
                .append(luaString(identity)).append("\n");
        }
    }

    /**
     * A statement which must run once in the map: it runs unless the map recorded it, and is recorded after it ran, so
     * one a failed payload did not reach runs with the next. The record is kept under what the statement assigns, with
     * the hash of its text: a global whose initial value a later edit changes keeps its value, and the map says so. A
     * local is a temporary of the statements after it and runs each time.
     */
    private static void appendOnce(StringBuilder sb, Statement s) {
        appendOnce(sb, s, null, null);
    }

    /**
     * {@link #appendOnce} for the initial value of a global, where {@code constantRevision} is the
     * {@link #effectiveRevision} of a constant which runs again when it changes ({@link #appendRefresh}), and null for
     * any other global, which keeps its value; {@code length} is the number of entries of an array constant.
     */
    private static void appendOnce(StringBuilder sb, Statement s, String constantRevision, Integer length) {
        if (s.text().startsWith("local ")) {
            sb.append(s.text()).append("\n");
            return;
        }
        String marker = RAN_TABLE + "[\"" + runKey(s) + "\"]";
        String text = "\"" + identityHash(s.revision()) + "\"";
        if (s.scope().equals("init") && s.kind().equals("global")) {
            sb.append("if ").append(KEPT_INITIALIZERS).append("[").append(luaString(runKey(s))).append("] then\n")
                .append(WARN_FUNCTION + "(").append(luaString("Wurst hot reload: " + initializerLabel(s)
                    + " keeps its value; restart the map to apply its new initial value")).append(")\nelseif not ");
        } else {
            sb.append("if not ");
        }
        sb.append(marker).append(" then\n").append(s.text()).append("\n")
            .append(marker).append(" = ").append(text).append("\n");
        appendConstantValue(sb, s, constantRevision, length);
        if (keepsValue(s) && constantRevision == null) {
            sb.append("elseif ").append(marker).append(" ~= ").append(text).append(" then\n")
                .append(WARN_FUNCTION + "(\"Wurst hot reload: ").append(initializerLabel(s))
                .append(" keeps its value; restart the map to apply its new initial value\")\n");
        }
        sb.append("end\n");
    }

    /**
     * Records in the map which revision of a constant's initializer ran, and how many entries it wrote when it is an
     * array constant ({@code length}); nothing for any other global (null).
     */
    private static void appendConstantValue(StringBuilder sb, Statement s, String constantRevision, Integer length) {
        if (constantRevision != null) {
            sb.append(CONSTANT_VALUES).append("[\"").append(runKey(s)).append("\"] = \"").append(constantRevision).append("\"\n");
            if (length != null) {
                sb.append(CONSTANT_LENGTHS).append("[\"").append(runKey(s)).append("\"] = ").append(length).append("\n");
            }
        }
    }

    /**
     * Runs the initializer of a constant again, unless the revision the map ran last is {@code revision}: the map's own
     * record, or else {@code baselineRevision}, which ran when the map started. One a payload added ({@code
     * baselineRevision} null) runs again only once it ran ({@link #appendOnce}).
     *
     * <p>An array constant writes {@code length} entries; the run the map made last wrote the number it recorded, or
     * else {@code baselineLength}. The entries past the new length are cleared, so they read the array's default as in
     * a restarted map. They are cleared in place, by index: the table stays the one every function reaches.
     */
    private static void appendRefresh(StringBuilder sb, Statement s, String revision, String baselineRevision,
                                      Integer length, Integer baselineLength) {
        String value = CONSTANT_VALUES + "[\"" + runKey(s) + "\"]";
        sb.append("if ");
        if (baselineRevision == null) {
            sb.append(RAN_TABLE).append("[\"").append(runKey(s)).append("\"] and ").append(value);
        } else {
            sb.append("(").append(value).append(" or \"").append(baselineRevision).append("\")");
        }
        sb.append(" ~= \"").append(revision).append("\" then\n").append(s.text()).append("\n");
        if (length != null) {
            String written = CONSTANT_LENGTHS + "[\"" + runKey(s) + "\"]";
            sb.append("for i = ").append(length).append(", (").append(written).append(" or ")
                .append(baselineRevision == null ? 0 : Objects.requireNonNull(baselineLength).intValue()).append(") - 1 do\n");
            for (String target : s.targets()) {
                sb.append(target).append("[i] = nil\n");
            }
            sb.append("end\n");
        }
        appendConstantValue(sb, s, revision, length);
        sb.append("end\n");
    }

    /** The source initializers of each package init, in the order it runs them. */
    private static Map<String, List<Statement>> initializerUnits(Manifest manifest) {
        Map<String, List<Statement>> result = new LinkedHashMap<>();
        for (Statement s : manifest.statements()) {
            if (s.scope().equals("init") && (s.kind().equals("global") || s.kind().equals("action"))) {
                result.computeIfAbsent(s.owner(), k -> new ArrayList<>()).add(s);
            }
        }
        return result;
    }

    /**
     * What the map ran of a constant: its initializer's definition, with the functions it calls and what the map ran of
     * each pure constant it reads, since those give it its value. A constant runs again when this changes.
     */
    private static String effectiveRevision(Manifest manifest, Map<String, Statement> units, Map<String, Constant> constants,
                                            String key, Map<String, String> known) {
        String revision = known.get(key);
        if (revision != null) {
            return revision;
        }
        StringBuilder text = new StringBuilder(units.get(key).revision());
        Constant constant = constants.get(key);
        if (constant != null && constant.pure()) {
            // The functions it calls compute its value as much as its own definition does.
            for (String call : Objects.requireNonNullElse(constant.calls(), List.<String>of())) {
                Function f = manifest.functions().get(call);
                text.append('\n').append(call).append(' ').append(f == null ? "" : f.shape());
            }
            for (String read : constant.reads()) {
                Constant c = constants.get(read);
                if (c != null && c.pure() && units.containsKey(read)) {
                    text.append('\n').append(effectiveRevision(manifest, units, constants, read, known));
                }
            }
        }
        revision = identityHash(text.toString());
        known.put(key, revision);
        return revision;
    }

    /**
     * Which constants a payload may run again, and what each revision is ({@link Constant}). A pure constant runs again
     * when every constant it reads, directly or through others, holds what its initializer gave it: one which the map's
     * baseline or an earlier payload declared as a variable may hold a value code wrote, so the constants computed from
     * it keep their values.
     */
    private static final class Constants {
        private final Manifest baselineManifest;
        private final Manifest currentManifest;
        private final Map<String, Constant> current;
        private final Map<String, Constant> baseline;
        private final Map<String, Statement> currentUnits = new HashMap<>();
        private final Map<String, Statement> baselineUnits = new HashMap<>();
        private final Set<String> variables = new HashSet<>();
        private final List<String> currentVariables;
        private final Map<String, Boolean> reliable = new HashMap<>();
        private final Map<String, String> currentRevisions = new HashMap<>();
        private final Map<String, String> baselineRevisions = new HashMap<>();

        Constants(Manifest baseline, Manifest current, History history) {
            this.baselineManifest = baseline;
            this.currentManifest = current;
            this.current = Objects.requireNonNullElse(current.constants(), Map.of());
            this.baseline = Objects.requireNonNullElse(baseline.constants(), Map.of());
            initializerUnits(current).values().forEach(units -> units.forEach(s -> currentUnits.put(s.key(), s)));
            initializerUnits(baseline).values().forEach(units -> units.forEach(s -> baselineUnits.put(s.key(), s)));
            // A variable need not have an initializer, so variables are told by their storage.
            variables.addAll(history.variables());
            variables.addAll(Objects.requireNonNullElse(baseline.variables(), List.of()));
            currentVariables = Objects.requireNonNullElse(current.variables(), List.of());
        }

        /** The storage of globals this version declares as variables, which the map did not know as such. */
        Set<String> newVariables() {
            Set<String> result = new TreeSet<>(currentVariables);
            result.removeAll(variables);
            return result;
        }

        /**
         * A pure constant whose storage, and that of every constant it reads, only its initializer ever wrote. An array
         * constant also needs to know how many entries the map's first run wrote, to clear those an edit drops.
         */
        boolean refreshable(String key) {
            Constant c = current.get(key);
            return c != null && c.pure() && reliable(key)
                && (c.length() == null || !baselineUnits.containsKey(key) || baselineLength(key) != null);
        }

        /** How many entries the current initializer of an array constant writes; null for any other constant. */
        Integer length(String key) {
            Constant c = current.get(key);
            return c == null ? null : c.length();
        }

        /** How many entries the initializer of an array constant wrote when the map started. */
        Integer baselineLength(String key) {
            Constant c = baseline.get(key);
            return c == null ? null : c.length();
        }

        List<String> calls(String key) {
            Constant c = current.get(key);
            return c == null ? List.of() : Objects.requireNonNullElse(c.calls(), List.of());
        }

        List<String> reads(String key) {
            Constant c = current.get(key);
            return c == null ? List.of() : c.reads();
        }

        private boolean reliable(String key) {
            Boolean known = reliable.get(key);
            if (known == null) {
                Constant c = current.get(key);
                Statement unit = currentUnits.get(key);
                known = c != null && unit != null && unit.targets().stream().noneMatch(variables::contains)
                    && c.reads().stream().allMatch(this::reliable);
                reliable.put(key, known);
            }
            return known;
        }

        /** The revision of a constant which runs again when it changes; null for any other global. */
        String currentRevision(String key) {
            return refreshable(key) ? effectiveRevision(currentManifest, currentUnits, current, key, currentRevisions) : null;
        }

        /** The revision of the initializer the map ran when it started; null for one a payload added. */
        String baselineRevision(String key) {
            return baselineUnits.containsKey(key)
                ? effectiveRevision(baselineManifest, baselineUnits, baseline, key, baselineRevisions) : null;
        }
    }

    private static boolean initializedBefore(Statement s, Set<String> knownNames) {
        return knownNames.contains(s.key()) || !s.targets().isEmpty() && s.targets().stream().allMatch(knownNames::contains);
    }

    private static String initializerLabel(Statement s) {
        return s.targets().isEmpty() ? s.key() : String.join(", ", s.targets());
    }

    /**
     * Whether a later edit of {@code s} is skipped with a message rather than refused: the initial value of a global,
     * which the map keeps as it keeps its own globals'. A top-level declaration (a class table, a field store) cannot be
     * kept: code compiled for the new one would use the old.
     */
    private static boolean keepsValue(Statement s) {
        return s.kind().equals("global") && !s.scope().equals("top");
    }

    /** {@code s} as a Lua string literal. */
    private static String luaString(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }

    /** The key under which the map records that a statement ran: where it is and what it assigns, not its text. */
    static String runKey(Statement s) {
        return runKey(s.scope(), s.owner(), s.key());
    }

    private static String runKey(String scope, String owner, String key) {
        return identityHash(scope + "\n" + owner + "\n" + key);
    }

    private static String describe(String scope) {
        return switch (scope) {
            case "top" -> "declaration";
            case "boot" -> "class or runtime data";
            default -> "initialisation";
        };
    }

    /**
     * The functions which share an identity, by that identity, in either manifest: the closures of one interface method
     * in one function, which are told apart by their order alone.
     */
    private static Map<String, Set<String>> orderGroups(Manifest baseline, Manifest current) {
        Map<String, Set<String>> groups = new TreeMap<>();
        for (Manifest m : List.of(baseline, current)) {
            for (Map.Entry<String, String> b : bindingMap(m.bindings()).entrySet()) {
                if (m.functions().containsKey(b.getKey())) {
                    groups.computeIfAbsent(b.getValue(), k -> new TreeSet<>()).add(b.getKey());
                }
            }
        }
        groups.values().removeIf(names -> names.size() < 2);
        return groups;
    }

    /** What a function of an order group is compared by: its closure's source, or else its shape. */
    private static String sourceOf(Function f) {
        return f.closureSource() != null ? f.closureSource() : f.shape();
    }

    /** The sources the map may run for a function: the baseline's, and every one an earlier payload sent. */
    private static Set<String> knownShapes(String name, Manifest baseline, History history) {
        Set<String> shapes = new HashSet<>();
        Function old = baseline.functions().get(name);
        if (old != null) {
            shapes.add(sourceOf(old));
        }
        shapes.addAll(Objects.requireNonNullElse(history.groupShapes(), Map.<String, List<String>>of()).getOrDefault(name, List.of()));
        return shapes;
    }

    /** How many names each identity has: more than one for the closures of one interface method in one function. */
    private static Map<String, Integer> identityCounts(List<String> bindings) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String id : bindingMap(bindings).values()) {
            counts.merge(id, 1, Integer::sum);
        }
        return counts;
    }

    private static Map<String, String> bindingMap(List<String> bindings) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String b : bindings) {
            int eq = b.indexOf('=');
            map.put(b.substring(0, eq), b.substring(eq + 1));
        }
        return map;
    }

    private static String firstLine(String s) {
        int nl = s.indexOf('\n');
        return nl < 0 ? s : s.substring(0, nl) + " ...";
    }

    /**
     * Finds where a function is used as a value rather than called: a class-table binding the payload repeats
     * ({@code slots}), or a store it cannot reach ({@code captures}). A callback adapter passes its target and its error
     * handler to xpcall by name each time it runs, so it reaches their new bodies without either.
     */
    private static void collectFunctionValues(LuaCompilationUnit model, Map<LuaFunction, List<String>> slots,
                                              Map<LuaFunction, List<String>> captures) {
        for (LuaStatement statement : model) {
            String where = statement instanceof LuaFunction f ? f.getName() : "the main chunk";
            visitFuncRefs(statement, ref -> {
                LuaFunction target = ref.getFunc();
                de.peeeq.wurstscript.luaAst.Element parent = ref.getParent();
                if (parent instanceof LuaAssignment a && a.getRight() == ref
                    && a.getLeft() instanceof LuaExprFieldAccess fa && fa.getReceiver() instanceof LuaExprVarAccess table) {
                    slots.computeIfAbsent(target, t -> new ArrayList<>()).add(table.getVar().getName() + "." + fa.getFieldName());
                } else if (parent instanceof LuaExprlist args && args.indexOf(ref) <= 1
                    && args.getParent() instanceof LuaExprFunctionCallByName call && call.getFuncName().equals("xpcall")) {
                    // the function and the message handler, resolved by name on every call
                } else if (parent instanceof LuaIf condition && condition.getCond() == ref) {
                    // a native fallback asks whether the runtime defines the native
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

    /**
     * The source identity of an emitted name: its package and its IM name, which carries the class prefix. Generated
     * Lua names are numbered in translation order, so two declarations of one name can swap numbers; the ids tell them
     * apart.
     */
    private static String semanticId(ElementWithTrace element, String name) {
        de.peeeq.wurstscript.ast.Element trace = element.attrTrace();
        PackageOrGlobal p = trace.attrNearestPackage();
        return (p instanceof WPackage wp ? wp.getName() : "global") + ":" + enclosingStructures(trace) + name;
    }

    /**
     * The classes (and modules) a nested one is declared in, outermost first: "A." for a member of A's inner class N, or
     * for N itself. Two inner classes of one name in one package are otherwise one identity, with one field type.
     */
    private static String enclosingStructures(de.peeeq.wurstscript.ast.Element trace) {
        StringBuilder path = new StringBuilder();
        de.peeeq.wurstscript.ast.Element inner = trace instanceof StructureDef s ? s : trace.attrNearestStructureDef();
        while (inner != null && inner.getParent() != null) {
            StructureDef outer = inner.getParent().attrNearestStructureDef();
            if (outer == null) {
                break;
            }
            path.insert(0, outer.getName() + ".");
            inner = outer;
        }
        return path.toString();
    }

    /** The identity of a function: its package, IM name and signature. */
    public static String functionId(ImFunction f) {
        return semanticId(f, signature(f));
    }

    /** The identity of a global or a field (with its class), or of a class. */
    public static String variableId(ImVar v) {
        de.peeeq.wurstscript.jassIm.Element owner = v.getParent() == null ? null : v.getParent().getParent();
        return semanticId(v, (owner instanceof ImClass c ? c.getName() + "." : "") + v.getName());
    }

    /** Specializations share the original field's store, so its name and binding use that same declaration. */
    static String fieldId(LuaTranslator tr, ImVar field) {
        ImVar canonical = tr.imTr.canonical(field);
        String identity = variableId(canonical);
        if (canonical.getParent() != null && canonical.getParent().getParent() instanceof ImClass owner) {
            identity += captureOwner(tr, owner);
        }
        return identity;
    }

    /** {@link #variableId}, or for a global holding a compile-time object or hashtable, what made it. */
    static String variableId(ImTranslator imTr, ImVar v) {
        String compiletime = imTr.compiletimeGlobalIdentity(v);
        return compiletime != null ? compiletime : variableId(v);
    }

    static String classId(ImClass c) {
        return semanticId(c, c.getName());
    }

    /**
     * What tells the captures of closures of one identity apart, which closures told apart by their order share: the
     * source identity and occurrence of the closure's class, which follow that order. A capture's storage is named after it, so a closure which
     * starts or stops capturing a variable renames no sibling's storage. Empty for any other class.
     */
    static String captureOwner(LuaTranslator tr, ImClass c) {
        return c.getTrace() instanceof ExprClosure ? "@closure:" + classId(c) + "#" + tr.closureOrdinal(c) : "";
    }

    /** The identity of a method: with its implementation's signature, which tells overloads apart. */
    static String methodId(ImMethod m) {
        ImFunction implementation = m.getImplementation();
        return semanticId(m, m.getName() + (implementation == null ? "" : "/" + signature(implementation)));
    }

    /**
     * Twelve hex digits of a hash of {@code identity}: the suffix which makes a hot-reload build's name of a declaration
     * depend on that declaration alone, so adding or removing another one renames nothing. The width is fixed, so a name
     * never depends on which other declarations exist. Two identities which share it (about one chance in 50,000 for a
     * program of 100,000 declarations) are numbered by {@link LuaTranslator#uniqueName}; a payload whose names then mean
     * other declarations is rejected by the binding check.
     */
    public static String identityHash(String identity) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(identity.getBytes(StandardCharsets.UTF_8)), 0, 6);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
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
            // By package as well: two packages may each have a class of the same name.
            StringJoiner args = new StringJoiner(",", semanticId(ct.getClassDef(), ct.getClassDef().getName()) + "<", ">");
            for (ImTypeArgument ta : ct.getTypeArguments()) {
                args.add(typeKey(ta.getType()));
            }
            return args.toString();
        } else if (t instanceof ImArrayType a) {
            return typeKey(a.getEntryType()) + "[]";
        } else if (t instanceof ImArrayTypeMulti a) {
            // The dimensions, not their sizes: a Lua array is a table of any length, so an array literal which gains
            // an entry (string[74] to string[75]) holds the same kind of value.
            return typeKey(a.getEntryType()) + "[" + ",".repeat(Math.max(0, a.getArraySize().size() - 1)) + "]";
        } else if (t instanceof ImTupleType tuple) {
            StringJoiner types = new StringJoiner(",", "(", ")");
            for (ImType component : tuple.getTypes()) {
                types.add(typeKey(component));
            }
            return types.toString();
        }
        return t.toString();
    }

    private static String fingerprint(String compilerVersion, List<String> parts, List<String> bindings) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(compilerVersion.getBytes(StandardCharsets.UTF_8));
            for (List<String> part : List.of(parts, bindings)) {
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
        LuaStatement abiStatement = LuaAst.LuaLiteral(abiDefinition(abi));
        tr.setHotReloadAbiStatement(abiStatement);
        model.add(abiStatement);
        model.add(LuaAst.LuaLiteral(VERSION_GLOBAL + " = 0"));
        model.add(LuaAst.LuaLiteral("__wurst_hotreload_nextFile = 1"));
        model.add(LuaAst.LuaLiteral(RAN_TABLE + " = {}"));
        model.add(LuaAst.LuaLiteral(BINDINGS_TABLE + " = {}"));
        model.add(LuaAst.LuaLiteral(INITIALIZER_POSITIONS + " = {}"));
        model.add(LuaAst.LuaLiteral(INITIALIZER_COUNT + " = 0"));
        model.add(LuaAst.LuaLiteral(INITIALIZER_BATCHES + " = {}"));
        model.add(LuaAst.LuaLiteral(CURRENT_BATCH + " = 0"));
        model.add(LuaAst.LuaLiteral(KEPT_INITIALIZERS + " = {}"));
        model.add(LuaAst.LuaLiteral(CONSTANT_VALUES + " = {}"));
        model.add(LuaAst.LuaLiteral(CONSTANT_LENGTHS + " = {}"));
        // With -stacktraces the code a payload runs pushes frames, and an install which fails (the init of a package it
        // adds raises) never pops them: the depth is put back after it, as after each hook.
        LuaVariable depthVar = tr.stackTraceDepth();
        String depth = depthVar == null ? null : depthVar.getName();
        List<String> apply = new ArrayList<>(List.of(
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
            "    " + WARNINGS + " = {}",
            "    " + CURRENT_BATCH + " = version"));
        if (depth != null) {
            apply.add("    local depth = " + depth);
        }
        apply.add("    local installed, installErr = pcall(install)");
        if (depth != null) {
            apply.add("    " + depth + " = depth");
        }
        apply.addAll(List.of(
            "    if not installed then",
            "        return false, \"the payload failed while it was installed, so the map may run old and new bodies: \" .. tostring(installErr)",
            "    end",
            "    " + VERSION_GLOBAL + " = version",
            "    return true",
            "end"));
        model.add(LuaAst.LuaLiteral(String.join("\n", apply)));
        model.add(LuaAst.LuaLiteral(WARNINGS + " = {}"));
        model.add(LuaAst.LuaLiteral(String.join("\n",
            "function " + WARN_FUNCTION + "(text)",
            "    BJDebugMsg(text)",
            "    local prefix = \"Wurst hot reload: \"",
            "    if text:sub(1, #prefix) == prefix then text = text:sub(#prefix + 1) end",
            "    " + WARNINGS + "[#" + WARNINGS + " + 1] = text",
            "end")));
        // A hook takes no parameters but, with -stacktraces, the position of its call, as every function does; without,
        // Lua drops the argument. The frames a failing hook pushed are never popped, so the depth is put back after it.
        List<String> runHook = new ArrayList<>(List.of("function " + HOOK_FUNCTION + "(hook, name)"));
        if (depth != null) {
            runHook.add("    local depth = " + depth);
        }
        runHook.add("    local ok, err = pcall(hook, \"when a hot reload ran its reload hooks\")");
        if (depth != null) {
            runHook.add("    " + depth + " = depth");
        }
        runHook.add("    if not ok then " + WARN_FUNCTION
            + "(\"Wurst hot reload: the reload hook \" .. name .. \" failed: \" .. tostring(err)) end");
        runHook.add("end");
        model.add(LuaAst.LuaLiteral(String.join("\n", runHook)));
        model.add(LuaAst.LuaLiteral("__wurst_hotreload_lastStatus = \"no payload read\""));
        model.add(LuaAst.LuaLiteral("function " + STATUS_FUNCTION + "() return __wurst_hotreload_lastStatus end"));
        // A payload reaches one player's game only, so hot reload is for a single-player game. The users playing are
        // counted as blizzard.j's InitBlizzardGlobals counts them for bj_isSinglePlayer, which a map whose main skips
        // InitBlizzard never sets: such a map read no payload and said nothing.
        model.add(LuaAst.LuaLiteral(String.join("\n",
            "function __wurst_hotreload_singlePlayer()",
            "    if __wurst_hotreload_users == nil then",
            "        local users = 0",
            "        for i = 0, GetBJMaxPlayers() - 1 do",
            "            local p = Player(i)",
            "            if GetPlayerController(p) == MAP_CONTROL_USER and GetPlayerSlotState(p) == PLAYER_SLOT_STATE_PLAYING then",
            "                users = users + 1",
            "            end",
            "        end",
            "        __wurst_hotreload_users = users",
            "        if users ~= 1 then",
            "            __wurst_hotreload_lastStatus = \"hot reload is off: it needs a single-player game, and this one has \" .. users .. \" users\"",
            "            BJDebugMsg(\"Wurst \" .. __wurst_hotreload_lastStatus)",
            "        end",
            "    end",
            "    return __wurst_hotreload_users == 1",
            "end")));
        model.add(LuaAst.LuaLiteral(String.join("\n",
            "function " + POLL_FUNCTION + "()",
            "    if not __wurst_hotreload_singlePlayer() then return end",
            "    local channel = FourCC(\"" + CHANNEL_ABILITY + "\")",
            // The map may show this ability: its tooltip is put back after each poll.
            "    local shown = BlzGetAbilityTooltip(channel, 0)",
            // "" does not clear a tooltip, so the payload last read would be read again.
            "    BlzSetAbilityTooltip(channel, \" \", 0)",
            "    Preloader(\"" + PRELOAD_FILE_PREFIX.replace("\\", "\\\\") + "\" .. __wurst_hotreload_nextFile .. \".pld\")",
            "    local text = BlzGetAbilityTooltip(channel, 0)",
            "    BlzSetAbilityTooltip(channel, shown, 0)",
            "    local marker = \"" + PAYLOAD_MARKER + "\"",
            "    if text:sub(1, #marker) ~= marker then return end",
            "    local source = text:sub(#marker + 1)",
            "    __wurst_hotreload_nextFile = __wurst_hotreload_nextFile + 1",
            "    local ok, err = " + APPLY_FUNCTION + "(source)",
            "    if ok then",
            "        __wurst_hotreload_lastStatus = \"applied payload \" .. " + VERSION_GLOBAL,
            // What it printed besides: what it carried but could not apply.
            "        if #" + WARNINGS + " > 0 then",
            "            __wurst_hotreload_lastStatus = __wurst_hotreload_lastStatus .. \"; \" .. table.concat(" + WARNINGS + ", \"; \")",
            "        end",
            "    else",
            "        __wurst_hotreload_lastStatus = \"rejected: \" .. tostring(err)",
            "    end",
            "    BJDebugMsg(\"Wurst hot reload: \" .. __wurst_hotreload_lastStatus)",
            "end")));
    }
}
