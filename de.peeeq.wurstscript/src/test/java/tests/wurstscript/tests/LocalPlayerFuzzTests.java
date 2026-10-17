package tests.wurstscript.tests;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.luaAst.LuaCompilationUnit;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.wurstscript.projectconfig.WurstProjectConfigData;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.testng.AssertJUnit.assertTrue;

/**
 * Differential fuzzing of the optimiser on code which depends on the local player.
 *
 * <p>Each seed makes a random program full of client-local values ({@code GetLocalPlayer()}, client width and
 * activity, as UI code reads them), branches, loops, early returns and breaks on them, copies, globals, fields and
 * calls. The program is compiled to Lua without optimisations and with each optimised configuration, and every
 * build runs in Lua once per simulated client, with the client-local natives answering for that client. On every
 * client an optimised build must make the same calls with the same arguments as the unoptimised one: an
 * optimisation which changed what one client does could make clients diverge.
 *
 * <p>{@code WURST_FUZZ_PROGRAMS} and {@code WURST_FUZZ_SEED} (environment) run a larger or different campaign. A
 * failing seed leaves its program and both scripts in {@code test-output/fuzz}.
 */
public class LocalPlayerFuzzTests extends WurstScriptTest {

    private static final int CLIENTS = 4;
    private static final String[][] OPTIMISED = {
        {"-lua", "-inline", "-localOptimizations"},
        {"-lua", "-localOptimizations"},
    };

    @Test
    public void optimisedLuaDoesOnEveryClientWhatUnoptimisedLuaDoes() throws Exception {
        int programs = envInt("WURST_FUZZ_PROGRAMS", 25);
        long seed = envLong("WURST_FUZZ_SEED", 20261017L);
        boolean keepAll = System.getenv("WURST_FUZZ_KEEP") != null;
        String lua;
        try {
            lua = getLuaExecutable();
        } catch (IllegalStateException e) {
            throw new SkipException("Skipped the fuzzer, which runs the scripts in Lua: " + e.getMessage());
        }
        List<String> failures = new ArrayList<>();
        int clientsDiffer = 0;
        int scriptsDiffer = 0;
        for (int i = 0; i < programs; i++) {
            long programSeed = seed + i;
            String source = new ProgramGenerator(new Random(programSeed)).program();
            String referenceScript = compile(source, "-lua");
            String reference = run(lua, referenceScript);
            // A reference which stops early would compare truncated runs: the generator or the prelude is wrong.
            String stopped = stopped(reference);
            if (stopped != null) {
                failures.add("seed " + programSeed + ": the unoptimised program stops early: " + stopped);
                keep(programSeed, source, referenceScript, null, reference, null);
                continue;
            }
            if (clientsDiffer(reference)) {
                clientsDiffer++;
            }
            for (String[] args : OPTIMISED) {
                String optimisedScript = compile(source, args);
                if (!optimisedScript.equals(referenceScript)) {
                    scriptsDiffer++;
                }
                String optimised = run(lua, optimisedScript);
                if (!reference.equals(optimised)) {
                    failures.add("seed " + programSeed + " " + String.join(" ", args) + ": "
                        + firstDifference(reference, optimised));
                    keep(programSeed, source, referenceScript, optimisedScript, reference, optimised);
                } else if (keepAll) {
                    keep(programSeed, source, referenceScript, optimisedScript, reference, optimised);
                }
            }
        }
        // The campaign only says something if the clients do different things and the optimiser changes the code.
        System.out.println("[fuzz] programs " + programs + ", clients differ in " + clientsDiffer
            + ", optimised scripts differ from the reference in " + scriptsDiffer + " of " + programs * OPTIMISED.length
            + ", failures " + failures.size());
        assertTrue(failures.size() + " optimised builds of " + programs + " programs differ:\n"
            + String.join("\n", failures), failures.isEmpty());
        assertTrue("too few programs behave differently on different clients: " + clientsDiffer,
            clientsDiffer * 2 >= programs);
        assertTrue("too few optimised scripts differ from the reference: " + scriptsDiffer,
            scriptsDiffer * 2 >= programs * OPTIMISED.length);
    }

    private String compile(String source, String... args) {
        RunArgs runArgs = new RunArgs().with(args);
        WurstGui gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, runArgs);
        WurstModel model = parseFiles(Collections.emptyList(),
            Collections.singletonList(new CU("Fuzz.wurst", source)), false, compiler);
        if (model == null || !gui.getErrorList().isEmpty()) {
            throw new AssertionError("generated program does not compile: " + gui.getErrorList() + "\n" + source);
        }
        compiler.checkProg(model);
        if (!gui.getErrorList().isEmpty()) {
            throw new AssertionError("generated program does not compile: " + gui.getErrorList() + "\n" + source);
        }
        compiler.translateProgToIm(model);
        compiler.runCompiletime(WurstProjectConfigData.empty(), false, false);
        LuaCompilationUnit luaCode = compiler.transformProgToLua();
        StringBuilder sb = new StringBuilder();
        luaCode.print(sb, 0);
        return sb.toString();
    }

    /** Runs the script once per client and returns what each client did, one call per line. */
    private String run(String lua, String script) throws IOException, InterruptedException {
        File file = File.createTempFile("fuzz", ".lua");
        File output = File.createTempFile("fuzz", ".txt");
        try {
            Files.writeString(file.toPath(), script, StandardCharsets.UTF_8);
            String chunk = "for c = 0, " + (CLIENTS - 1) + " do "
                + "CLIENT = c; io.write('#client ', c, '\\n'); "
                + "local players = {}; for i = 0, 15 do players[i] = {id = i} end; "
                + "Player = function(i) return players[i] end; "
                + "GetLocalPlayer = function() return players[CLIENT] end; "
                + "GetPlayerId = function(p) if p == nil then return 0 end return p.id end; "
                + "BlzGetLocalClientWidth = function() return 800 + CLIENT * 3 end; "
                + "BlzIsLocalClientActive = function() return CLIENT % 2 == 0 end; "
                // an endless loop which traces stops at the cap instead of filling the disk
                + "local traced = 0; "
                + "local function count() traced = traced + 1; if traced > 20000 then error('trace cap') end end; "
                + "local groups = 0; "
                + "CreateGroup = function() count(); groups = groups + 1; io.write('g', groups, '\\n'); return {} end; "
                + "traceInt = function(x) count(); io.write('i', string.format('%d', x), '\\n') end; "
                + "traceBool = function(b) count(); io.write('b', tostring(b), '\\n') end; "
                // the script's path comes through the environment, as the temporary directory may contain a quote
                + "local ok, err = pcall(function() dofile(os.getenv('WURST_FUZZ_SCRIPT')); main() end); "
                + "if not ok then io.write('#error ', tostring(err), '\\n') end "
                + "end";
            ProcessBuilder builder = new ProcessBuilder(lua, "-e", chunk)
                .redirectErrorStream(true).redirectOutput(output);
            builder.environment().put("WURST_FUZZ_SCRIPT", file.getPath());
            Process p = builder.start();
            // A wrong optimisation can make a loop endless: that is a difference, not a hang of the suite.
            boolean finished = p.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly().waitFor(10, TimeUnit.SECONDS);
            }
            String trace = Files.readString(output.toPath(), StandardCharsets.UTF_8).replace("\r\n", "\n");
            return finished ? trace : trace + "#timeout\n";
        } finally {
            file.delete();
            output.delete();
        }
    }

    private static String firstDifference(String expected, String actual) {
        String[] e = expected.split("\n");
        String[] a = actual.split("\n");
        String client = "?";
        for (int i = 0; i < Math.max(e.length, a.length); i++) {
            String el = i < e.length ? e[i] : "<end>";
            String al = i < a.length ? a[i] : "<end>";
            if (el.startsWith("#client")) {
                client = el;
            }
            if (!el.equals(al)) {
                return client + ", line " + i + ": expected " + el + " but got " + al;
            }
        }
        return "?";
    }

    /** The line which says that a run stopped before its end, or null. */
    private static String stopped(String trace) {
        for (String line : trace.split("\n")) {
            if (line.startsWith("#error") || line.equals("#timeout")) {
                return line;
            }
        }
        return null;
    }

    /** Whether the clients made different calls. */
    private static boolean clientsDiffer(String trace) {
        String[] perClient = trace.split("#client \\d+\n");
        for (int i = 2; i < perClient.length; i++) {
            if (!perClient[i].equals(perClient[1])) {
                return true;
            }
        }
        return false;
    }

    private static void keep(long seed, String source, String referenceScript, String optimisedScript,
                             String reference, String optimised) throws IOException {
        File dir = new File("test-output/fuzz");
        dir.mkdirs();
        Files.writeString(new File(dir, seed + ".wurst").toPath(), source, StandardCharsets.UTF_8);
        Files.writeString(new File(dir, seed + "_reference.lua").toPath(), referenceScript, StandardCharsets.UTF_8);
        Files.writeString(new File(dir, seed + "_reference.txt").toPath(), reference, StandardCharsets.UTF_8);
        if (optimisedScript != null) {
            Files.writeString(new File(dir, seed + "_optimised.lua").toPath(), optimisedScript, StandardCharsets.UTF_8);
            Files.writeString(new File(dir, seed + "_optimised.txt").toPath(), optimised, StandardCharsets.UTF_8);
        }
    }

    private static int envInt(String name, int fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : Integer.parseInt(value);
    }

    private static long envLong(String name, long fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : Long.parseLong(value);
    }

    /** Random programs which type-check, terminate and keep every integer far from overflow. */
    static final class ProgramGenerator {
        private static final int MOD = 9973;
        private final Random r;
        private final StringBuilder out = new StringBuilder();
        private final Deque<Scope> scopes = new ArrayDeque<>();
        private final List<String> helpers = new ArrayList<>();
        private int nextName = 0;
        private int loopDepth = 0;
        private String box;

        ProgramGenerator(Random r) {
            this.r = r;
        }

        private static final class Scope {
            final List<String> ints = new ArrayList<>();
            final List<String> mutableInts = new ArrayList<>();
            final List<String> bools = new ArrayList<>();
            final List<String> players = new ArrayList<>();
        }

        String program() {
            line(0, "type player extends handle");
            line(0, "type group extends handle");
            line(0, "package Fuzz");
            line(0, "@extern native CreateGroup() returns group");
            line(0, "native traceInt(integer x)");
            line(0, "native traceBool(boolean b)");
            line(0, "@extern native GetLocalPlayer() returns player");
            line(0, "@extern native Player(integer i) returns player");
            line(0, "@extern native GetPlayerId(player p) returns integer");
            line(0, "@extern native BlzGetLocalClientWidth() returns integer");
            line(0, "@extern native BlzIsLocalClientActive() returns boolean");
            line(0, "int g0 = 0");
            line(0, "int g1 = 0");
            line(0, "boolean gb = false");
            line(0, "int array ga");
            line(0, "class Box");
            line(1, "int v = 0");
            line(1, "int w = 0");
            int helperCount = 1 + r.nextInt(3);
            for (int i = 0; i < helperCount; i++) {
                String name = "h" + i;
                function(name, r.nextBoolean(), 2 + r.nextInt(3), 2);
                helpers.add(name);
            }
            function("fuzzMain", true, 4 + r.nextInt(6), 3);
            line(0, "init");
            line(1, "traceInt(fuzzMain(" + r.nextInt(10) + ", " + r.nextInt(10) + "))");
            line(1, "traceInt(fuzzMain(" + r.nextInt(10) + ", " + r.nextInt(10) + "))");
            line(1, "traceInt(g0)");
            line(1, "traceInt(g1)");
            for (int k = 0; k < 4; k++) {
                line(1, "traceInt(ga[" + k + "])");
            }
            line(1, "traceBool(gb)");
            return out.toString();
        }

        private void function(String name, boolean noinline, int statements, int depth) {
            line(0, (noinline ? "@noinline " : "") + "function " + name + "(int a, int b) returns int");
            Scope params = new Scope();
            params.ints.add("a");
            params.ints.add("b");
            scopes.push(params);
            box = fresh("box");
            line(1, "let " + box + " = new Box()");
            // Locals which hold opaque values and are read again at the end live across the whole body, so the
            // propagation cannot remove them and the merger has overlapping live ranges to get right.
            List<String> prologue = new ArrayList<>();
            for (int i = 0, n = 2 + r.nextInt(4); i < n; i++) {
                String v = fresh("i");
                line(1, "var " + v + " = " + opaqueInt());
                params.ints.add(v);
                params.mutableInts.add(v);
                prologue.add("traceInt(" + v + ")");
            }
            for (int i = 0, n = 1 + r.nextInt(2); i < n; i++) {
                String v = fresh("c");
                line(1, "var " + v + " = " + (r.nextBoolean() ? "BlzIsLocalClientActive()" : opaqueInt() + " > 805"));
                params.bools.add(v);
                prologue.add("traceBool(" + v + ")");
            }
            for (int i = 0, n = r.nextInt(3); i < n; i++) {
                String v = fresh("p");
                line(1, "var " + v + " = " + (r.nextBoolean() ? "GetLocalPlayer()" : "Player(" + r.nextInt(CLIENTS) + ")"));
                params.players.add(v);
                prologue.add("traceInt(GetPlayerId(" + v + "))");
            }
            block(1, statements, depth);
            Collections.shuffle(prologue, r);
            for (String trace : prologue.subList(0, (prologue.size() + 1) / 2 + r.nextInt(prologue.size() / 2 + 1))) {
                line(1, trace);
            }
            line(1, "return " + intExpr(2));
            scopes.clear();
        }

        /** A value no pass can fold: it depends on the client, a parameter or a call. */
        private String opaqueInt() {
            switch (r.nextInt(5)) {
                case 0:
                    return "GetPlayerId(GetLocalPlayer()) + " + (r.nextBoolean() ? "a" : "b");
                case 1:
                    return "BlzGetLocalClientWidth() - " + (r.nextBoolean() ? "a" : "b");
                case 2: {
                    String call = helperCall();
                    return call != null ? call : "a * 3 + b";
                }
                case 3:
                    return "ga[" + r.nextInt(4) + "] + " + (r.nextBoolean() ? "a" : "b");
                default:
                    return (r.nextBoolean() ? "a" : "b") + " * " + (2 + r.nextInt(4));
            }
        }

        private void block(int indent, int statements, int depth) {
            scopes.push(new Scope());
            int n = 1 + r.nextInt(statements);
            for (int i = 0; i < n; i++) {
                statement(indent, depth);
            }
            scopes.pop();
        }

        private void statement(int ind, int depth) {
            int kind = r.nextInt(depth > 0 ? 15 : 10);
            switch (kind) {
                case 0 -> {
                    String v = fresh("i");
                    line(ind, "var " + v + " = " + intExpr(2));
                    scopes.peek().ints.add(v);
                    scopes.peek().mutableInts.add(v);
                }
                case 1 -> {
                    String v = fresh("c");
                    line(ind, "var " + v + " = " + boolExpr(2));
                    scopes.peek().bools.add(v);
                }
                case 2 -> {
                    String v = fresh("p");
                    line(ind, "var " + v + " = " + playerExpr());
                    scopes.peek().players.add(v);
                }
                case 3, 4 -> {
                    // copies of variables are what the propagation and the merger work on
                    String target = pick(all(s -> s.mutableInts));
                    if (target == null) {
                        line(ind, "traceInt(" + intExpr(2) + ")");
                    } else if (r.nextBoolean()) {
                        String source = pick(all(s -> s.ints));
                        line(ind, target + " = " + source);
                    } else {
                        line(ind, target + " = " + mod(intExpr(2)));
                    }
                }
                case 5 -> {
                    String target = pick(all(s -> s.bools));
                    if (target == null) {
                        line(ind, "traceBool(" + boolExpr(2) + ")");
                    } else {
                        line(ind, target + " = " + boolExpr(2));
                    }
                }
                case 6 -> {
                    String target = pick(all(s -> s.players));
                    if (target == null) {
                        line(ind, "traceInt(GetPlayerId(" + playerExpr() + "))");
                    } else {
                        line(ind, target + " = " + playerExpr());
                    }
                }
                case 7 -> {
                    // An allocation is an effect the game sees (handle ids): one which an optimisation moved into
                    // or out of a local branch, or dropped, would make clients diverge.
                    if (r.nextBoolean()) {
                        line(ind, "CreateGroup()");
                    } else {
                        line(ind, "traceInt(" + intExpr(2) + ")");
                    }
                }
                case 8 -> line(ind, "traceBool(" + boolExpr(2) + ")");
                case 9 -> {
                    switch (r.nextInt(5)) {
                        case 0 -> line(ind, "g0 = " + (r.nextBoolean() ? String.valueOf(r.nextInt(10)) : mod(intExpr(2))));
                        case 1 -> line(ind, "g1 = " + (r.nextBoolean() ? String.valueOf(r.nextInt(10)) : mod(intExpr(2))));
                        case 2 -> line(ind, "ga[" + r.nextInt(4) + "] = " + mod(intExpr(2)));
                        case 3 -> line(ind, "gb = " + boolExpr(2));
                        default -> line(ind, box + "." + (r.nextBoolean() ? "v" : "w") + " = " + mod(intExpr(2)));
                    }
                }
                case 10, 11 -> {
                    line(ind, "if " + condition());
                    block(ind + 1, 3, depth - 1);
                    if (r.nextBoolean()) {
                        line(ind, "else");
                        block(ind + 1, 3, depth - 1);
                    }
                }
                case 12 -> {
                    String counter = fresh("n");
                    line(ind, "var " + counter + " = 0");
                    line(ind, "while " + counter + " < " + (1 + r.nextInt(4)));
                    line(ind + 1, counter + " = " + counter + " + 1");
                    scopes.peek().ints.add(counter);
                    loopDepth++;
                    block(ind + 1, 3, depth - 1);
                    if (r.nextInt(3) == 0) {
                        line(ind + 1, "if " + condition());
                        line(ind + 2, "break");
                    }
                    loopDepth--;
                }
                case 13 -> {
                    String flag = pick(all(s -> s.bools));
                    String target = pick(all(s -> s.mutableInts));
                    if (flag != null && target != null && r.nextBoolean()) {
                        // two ifs on one boolean local: an assignment in the first, reads in the second
                        line(ind, "if " + flag);
                        line(ind + 1, target + " = " + (r.nextBoolean() ? String.valueOf(r.nextInt(10)) : pick(all(s -> s.ints))));
                        line(ind, "if " + flag);
                        line(ind + 1, "traceInt(" + target + " + " + intExpr(1) + ")");
                    } else {
                        line(ind, "if " + condition());
                        line(ind + 1, "return " + intExpr(1));
                    }
                }
                default -> {
                    String target = pick(all(s -> s.mutableInts));
                    String call = helperCall();
                    if (call == null) {
                        line(ind, "traceInt(" + intExpr(2) + ")");
                    } else if (target == null) {
                        line(ind, "traceInt(" + call + ")");
                    } else {
                        line(ind, target + " = " + mod(call));
                    }
                }
            }
        }

        /** Mostly client-local conditions, as UI code has them. */
        private String condition() {
            switch (r.nextInt(6)) {
                case 0:
                    return "GetLocalPlayer() == " + playerExpr();
                case 1:
                    return "BlzIsLocalClientActive()";
                case 2:
                    return "GetPlayerId(GetLocalPlayer()) == " + r.nextInt(CLIENTS);
                case 3:
                    return "BlzGetLocalClientWidth() > " + (800 + r.nextInt(12));
                default:
                    return boolExpr(2);
            }
        }

        private String intExpr(int depth) {
            if (depth <= 0 || r.nextInt(3) == 0) {
                switch (r.nextInt(9)) {
                    case 0:
                    case 1: {
                        String v = pick(all(s -> s.ints));
                        return v != null ? v : String.valueOf(r.nextInt(10));
                    }
                    case 2:
                        return r.nextBoolean() ? "g0" : "g1";
                    case 3:
                        return "ga[" + r.nextInt(4) + "]";
                    case 4:
                        return box + "." + (r.nextBoolean() ? "v" : "w");
                    case 5:
                        return "GetPlayerId(" + playerExpr() + ")";
                    case 6:
                        return "BlzGetLocalClientWidth()";
                    default:
                        return String.valueOf(r.nextInt(10));
                }
            }
            switch (r.nextInt(5)) {
                case 0:
                    return "(" + intExpr(depth - 1) + " + " + intExpr(depth - 1) + ")";
                case 1:
                    return "(" + intExpr(depth - 1) + " - " + intExpr(depth - 1) + ")";
                case 2:
                    return "(" + intExpr(depth - 1) + " * " + r.nextInt(6) + ")";
                case 3: {
                    String call = helperCall();
                    return call != null ? call : intExpr(depth - 1);
                }
                default:
                    return intExpr(depth - 1);
            }
        }

        private String helperCall() {
            if (helpers.isEmpty()) {
                return null;
            }
            return helpers.get(r.nextInt(helpers.size())) + "(" + mod(intExpr(1)) + ", " + mod(intExpr(1)) + ")";
        }

        private String boolExpr(int depth) {
            if (depth <= 0 || r.nextInt(3) == 0) {
                switch (r.nextInt(8)) {
                    case 0: {
                        String v = pick(all(s -> s.bools));
                        return v != null ? v : "true";
                    }
                    case 1:
                        return "gb";
                    case 2:
                        return "BlzIsLocalClientActive()";
                    case 3:
                        return "GetLocalPlayer() == " + playerExpr();
                    case 4:
                        return r.nextBoolean() ? "true" : "false";
                    default:
                        return intExpr(1) + (r.nextBoolean() ? " > " : " == ") + intExpr(1);
                }
            }
            switch (r.nextInt(3)) {
                case 0:
                    return "(" + boolExpr(depth - 1) + " and " + boolExpr(depth - 1) + ")";
                case 1:
                    return "(" + boolExpr(depth - 1) + " or " + boolExpr(depth - 1) + ")";
                default:
                    return "not (" + boolExpr(depth - 1) + ")";
            }
        }

        private String playerExpr() {
            switch (r.nextInt(4)) {
                case 0: {
                    String v = pick(all(s -> s.players));
                    return v != null ? v : "GetLocalPlayer()";
                }
                case 1:
                    return "GetLocalPlayer()";
                default:
                    return "Player(" + r.nextInt(CLIENTS) + ")";
            }
        }

        private static String mod(String e) {
            return "(" + e + ") mod " + MOD;
        }

        private List<String> all(java.util.function.Function<Scope, List<String>> kind) {
            List<String> result = new ArrayList<>();
            for (Scope s : scopes) {
                result.addAll(kind.apply(s));
            }
            return result;
        }

        private String pick(List<String> names) {
            return names.isEmpty() ? null : names.get(r.nextInt(names.size()));
        }

        private String fresh(String prefix) {
            return prefix + nextName++;
        }

        private void line(int indent, String text) {
            out.append("    ".repeat(indent)).append(text).append('\n');
        }
    }
}
