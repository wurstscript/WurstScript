package tests.wurstscript.tests;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.jassinterpreter.JassInterpreter;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassinterpreter.TestFailException;
import de.peeeq.wurstscript.jassinterpreter.TestSuccessException;
import de.peeeq.wurstscript.luaAst.LuaCompilationUnit;
import de.peeeq.wurstscript.translation.imtranslation.RecycleCodeGeneratorQueue;
import de.peeeq.wurstscript.translation.lua.translation.LuaTranslator;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wurstscript.projectconfig.WurstProjectConfigData;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

/**
 * Bounded-exhaustive differential test of the Lua backend's targeted specialisation of new generics.
 *
 * <p>On Lua a new generic stays erased except where code needs its concrete type
 * ({@code EliminateGenerics.transformGenericNewOnly}); Jass specialises everything. The defects come from
 * the seams: one path writes the erased form of a store while another reads its specialised form. So each
 * case puts one thing which depends on {@code T} ({@link Dep}) in one place of a generic class ({@link Site}),
 * writes it through one way of reaching the class from concrete code and reads it through another
 * ({@link Path}), at one instantiation type or two ({@link Inst}), with or without the optimiser. The program
 * checks itself against values the generator works out, and runs translated to Jass in the Jass interpreter
 * (the oracle: full specialisation) and translated to Lua in Lua.
 *
 * <p>A combination which is not a program is skipped with the reason ({@link #notAProgram}); one the compiler
 * rejects without such a rule fails. Cases known to fail are listed with their defect in
 * {@link #KNOWN_FAILURES}: they are skipped with their failure, and any other failure fails the test.
 *
 * <p>A pull request runs a covering subset (384 cases, about a minute): every pair of dimension values, and
 * every combination of dependency, writer and reader. {@code WURST_GENERIC_MATRIX=full} runs the whole product
 * (36,864 cases, about an hour), which is for master or nightly runs, split by
 * {@code WURST_GENERIC_MATRIX_SHARD=i/n} so that each run stays inside the test task's twenty minutes (n = 6
 * or more). The list of known failures covers the subset only. {@code WURST_GENERIC_MATRIX_KEEP_GOING}
 * records unexpected failures instead of failing them and fails the summary at the end, since the suite stops
 * at its first failure. {@code WURST_GENERIC_MATRIX_FILTER} (a regular expression on the case name) picks
 * cases. Environment variables are not test inputs to Gradle: run with {@code --rerun} after changing one.
 * Every case's outcome goes to {@code test-output/generic-matrix/results.txt}, and a failing case leaves its
 * program and its Lua script there.
 */
public class GenericSpecialisationMatrixTests extends WurstScriptTest {

    /** What depends on the type parameter. */
    enum Dep {
        /** A static field of type T, and a static counter, both per instantiation. */
        STATIC,
        /** A store with a fixed type argument ({@code Cell<X>}) used from inside the generic class, as #1498. */
        FIXED_STATIC,
        /** A requirement of a type-class bound, {@code T.kmk(n)}. */
        BOUND,
        /** {@code wurstNewInstance<T>()}. */
        NEW_INSTANCE,
        /** {@code x instanceof Box<T>}. */
        INSTANCEOF,
        /** An instance field of type T, tuple-typed when T is a tuple. */
        FIELD
    }

    /** Where the code which depends on T sits. */
    enum Site {
        METHOD,
        /**
         * In a second constructor of the generic class, which works on the receiver it is given: the method
         * constructs and destroys a helper instance. Field initialisers appear only as the fixed store's
         * {@code new Cell<X>()}.
         */
        CONSTRUCTOR,
        /** In the ondestroy of a helper instance, as for {@link #CONSTRUCTOR}. */
        ONDESTROY,
        /** In a closure created inside the generic class. */
        CLOSURE,
        /** In a method of a generic superclass, which the class inherits. */
        INHERITED,
        /** In a member injected from a module with a type parameter. */
        MODULE
    }

    /** How concrete code reaches the generic class. */
    enum Path {
        DIRECT,
        /** Through a parameter of a non-generic abstract base class. */
        BASE,
        /** Through a parameter of a non-generic interface. */
        INTERFACE,
        /** Through a variable of a generic interface. */
        GENERIC_INTERFACE,
        /** Through a closure created by concrete code. */
        CLOSURE,
        /** Through a concrete subclass which overrides the method with a super call. */
        SUPER,
        /** Through the ondestroy of a generic helper, destroyed through its non-generic base class. */
        DESTROY,
        /** Through a generic function, which is erased on Lua. */
        ERASED_FUNCTION
    }

    /** The instantiation types: the first and, when there are two instantiations, the second. */
    enum Inst {
        INT(Ty.INT, Ty.STRING), STRING(Ty.STRING, Ty.INT), CLASS(Ty.CA, Ty.CB), TUPLE(Ty.PA, Ty.PB);

        final Ty first;
        final Ty second;

        Inst(Ty first, Ty second) {
            this.first = first;
            this.second = second;
        }
    }

    /** A concrete type, with the functions that make one of its values from an int and read it back. */
    enum Ty {
        INT("int", "Int"), STRING("string", "Str"), CA("CA", "CA"), CB("CB", "CB"), PA("pa", "Pa"), PB("pb", "Pb");

        final String wurst;
        final String suffix;

        Ty(String wurst, String suffix) {
            this.wurst = wurst;
            this.suffix = suffix;
        }

        /** Added by the bound's instance for this type, so that a call of another type's instance shows. */
        int boundTag() {
            return 100 * (ordinal() + 1);
        }

        /** The tag the class's zero-argument constructor sets. */
        int constructedTag() {
            return 73 + ordinal() - CA.ordinal();
        }

        boolean isClass() {
            return this == CA || this == CB;
        }
    }

    record Combo(Dep dep, Site site, Path writer, Path reader, Inst inst, int instantiations, boolean optimised) {
        String name() {
            return dep + "_" + site + "_w" + writer + "_r" + reader + "_" + inst + "x" + instantiations
                + (optimised ? "_opt" : "");
        }

        Ty type(int which) {
            return which == 0 ? inst.first : inst.second;
        }

        boolean uses(Path p) {
            return writer == p || reader == p;
        }

        @Override
        public String toString() {
            return name();
        }
    }

    // ------------------------------------------------------------------------------------------------------
    // Which combinations are programs

    /**
     * Why a combination is not a valid program, or null. Every combination the compiler rejects has a rule
     * here, so a rejection the rules do not predict is reported as a failure, not skipped: it is either a
     * compiler defect or a gap in these rules, and both are worth knowing.
     */
    static String notAProgram(Combo c) {
        if (c.dep == Dep.NEW_INSTANCE && !(c.inst.first.isClass() && c.inst.second.isClass())) {
            return "wurstNewInstance<T>() needs a class with a zero-argument constructor";
        }
        if (c.site == Site.MODULE && c.uses(Path.BASE)) {
            // The override check of a module member sees the module, which has no superclass.
            return "a module member cannot override a method of the class's superclass";
        }
        return null;
    }

    static List<Combo> fullProduct() {
        List<Combo> all = new ArrayList<>();
        for (Dep dep : Dep.values()) {
            for (Site site : Site.values()) {
                for (Path writer : Path.values()) {
                    for (Path reader : Path.values()) {
                        for (Inst inst : Inst.values()) {
                            for (int n = 1; n <= 2; n++) {
                                for (boolean opt : new boolean[]{false, true}) {
                                    all.add(new Combo(dep, site, writer, reader, inst, n, opt));
                                }
                            }
                        }
                    }
                }
            }
        }
        return all;
    }

    /** The values of each dimension of a combination, in a fixed order, for the covering array. */
    private static int[] coordinates(Combo c) {
        return new int[]{c.dep.ordinal(), c.site.ordinal(), c.writer.ordinal(), c.reader.ordinal(),
            c.inst.ordinal(), c.instantiations - 1, c.optimised ? 1 : 0};
    }

    private static final int[] DIMENSION_SIZES = {Dep.values().length, Site.values().length, Path.values().length,
        Path.values().length, Inst.values().length, 2, 2};

    /** Dimensions whose every combination of values the pull-request tier covers: dependency, writer, reader. */
    private static final int[][] COVERED_TRIPLES = {{0, 2, 3}};

    /**
     * A covering subset of the valid combinations: each pair of values of any two dimensions, and each triple
     * in {@link #COVERED_TRIPLES}, that occurs in some valid combination occurs in one of these. Greedy: each
     * step takes a combination which covers the most interactions not yet covered, choosing among equals with a
     * fixed seed, so that dimensions already covered do not settle on their first value.
     */
    static List<Combo> coveringSubset(List<Combo> valid) {
        List<int[]> interactions = new ArrayList<>();
        for (int a = 0; a < DIMENSION_SIZES.length; a++) {
            for (int b = a + 1; b < DIMENSION_SIZES.length; b++) {
                interactions.add(new int[]{a, b});
            }
        }
        interactions.addAll(Arrays.asList(COVERED_TRIPLES));
        // Each interaction of each candidate as an index into one flat set.
        int[] offsets = new int[interactions.size()];
        int total = 0;
        for (int i = 0; i < interactions.size(); i++) {
            offsets[i] = total;
            int size = 1;
            for (int d : interactions.get(i)) {
                size *= DIMENSION_SIZES[d];
            }
            total += size;
        }
        int[][] keys = new int[valid.size()][interactions.size()];
        boolean[] open = new boolean[total];
        for (int ci = 0; ci < valid.size(); ci++) {
            int[] co = coordinates(valid.get(ci));
            for (int i = 0; i < interactions.size(); i++) {
                int key = 0;
                for (int d : interactions.get(i)) {
                    key = key * DIMENSION_SIZES[d] + co[d];
                }
                keys[ci][i] = offsets[i] + key;
                open[keys[ci][i]] = true;
            }
        }
        List<Combo> chosen = new ArrayList<>();
        boolean[] taken = new boolean[valid.size()];
        Random ties = new Random(20261020L);
        while (true) {
            int best = -1;
            int bestGain = 0;
            int equals = 0;
            for (int ci = 0; ci < valid.size(); ci++) {
                if (taken[ci]) {
                    continue;
                }
                int gain = 0;
                for (int key : keys[ci]) {
                    if (open[key]) {
                        gain++;
                    }
                }
                if (gain > bestGain) {
                    bestGain = gain;
                    best = ci;
                    equals = 1;
                } else if (gain == bestGain && gain > 0 && ties.nextInt(++equals) == 0) {
                    best = ci;
                }
            }
            if (best < 0) {
                return chosen;
            }
            taken[best] = true;
            chosen.add(valid.get(best));
            for (int key : keys[best]) {
                open[key] = false;
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------
    // The program

    /** The value the read of instantiation {@code which} (0 or 1) must give. */
    static int expected(Combo c, int which) {
        Ty t = c.type(which);
        int id = which + 1;
        int k = which;
        int n = id * 10 + k;
        return switch (c.dep) {
            case STATIC, FIELD -> n + (c.dep == Dep.STATIC ? 1000 : 0);
            case FIXED_STATIC -> n + 1000 * c.instantiations;
            case BOUND -> n + t.boundTag();
            case NEW_INSTANCE -> t.constructedTag() + 1000 * k;
            // held is a Box<T> for the first and the first's box for the second; boxX and boxY by type
            case INSTANCEOF -> which == 0 ? 1 + 2 : 4;
        };
    }

    static String program(Combo c) {
        return new ProgramWriter(c).write();
    }

    static final class ProgramWriter {
        private final Combo c;
        private final StringBuilder out = new StringBuilder();
        private final Ty x;
        private final Ty y;
        private final boolean base;
        private final boolean iface;
        private final boolean genericIface;
        private final String typeParam;

        ProgramWriter(Combo c) {
            this.c = c;
            this.x = c.inst.first;
            this.y = c.inst.second;
            this.base = c.uses(Path.BASE);
            this.iface = c.uses(Path.INTERFACE);
            this.genericIface = c.uses(Path.GENERIC_INTERFACE);
            this.typeParam = c.dep == Dep.BOUND ? "T: Kc" : "T:";
        }

        private void line(int depth, String text) {
            out.append("    ".repeat(depth)).append(text).append('\n');
        }

        private boolean usesCodec() {
            return c.dep == Dep.STATIC || c.dep == Dep.NEW_INSTANCE || c.dep == Dep.FIELD;
        }

        private List<Ty> types() {
            return c.instantiations == 2 || c.dep == Dep.INSTANCEOF ? List.of(x, y) : List.of(x);
        }

        String write() {
            line(0, "package Test");
            line(0, "native testSuccess()");
            line(0, "native testFail(string message)");
            prelude();
            for (Ty t : types()) {
                valueFunctions(t);
            }
            if (usesCodec()) {
                codec();
            }
            if (c.dep == Dep.BOUND) {
                bound();
            }
            if (c.dep == Dep.FIXED_STATIC) {
                cell();
            }
            if (c.dep == Dep.INSTANCEOF) {
                line(0, "interface Marker");
                line(0, "class Box<T:> implements Marker");
                line(1, "T item");
                line(0, "Marker boxX = null");
                line(0, "Marker boxY = null");
            }
            if (c.site == Site.CLOSURE || c.uses(Path.CLOSURE)) {
                line(0, "interface Act");
                line(1, "function run()");
                line(0, "interface Fn");
                line(1, "function get() returns int");
            }
            if (base) {
                line(0, "abstract class Base");
                line(1, "abstract function w(int k)");
                line(1, "abstract function r() returns int");
            }
            if (iface) {
                line(0, "interface Iface");
                line(1, "function w(int k)");
                line(1, "function r() returns int");
            }
            if (genericIface) {
                line(0, "interface GIface<T:>");
                line(1, "function w(int k)");
                line(1, "function r() returns int");
            }
            subject();
            paths();
            mainBlock();
            return out.toString();
        }

        private void prelude() {
            line(0, "function digit(int d) returns string");
            for (int d = 0; d < 9; d++) {
                line(1, "if d == " + d);
                line(2, "return \"" + d + "\"");
            }
            line(1, "return \"9\"");
            line(0, "function digits(int i) returns string");
            line(1, "if i < 0");
            line(2, "return \"-\" + digits(-i)");
            line(1, "if i < 10");
            line(2, "return digit(i)");
            line(1, "return digits(i div 10) + digit(i mod 10)");
            line(0, "function b2i(boolean b) returns int");
            line(1, "if b");
            line(2, "return 1");
            line(1, "return 0");
            line(0, "function check(int got, int expected, string what)");
            line(1, "if got != expected");
            line(2, "testFail(what + \": expected \" + digits(expected) + \" but got \" + digits(got))");
        }

        private void valueFunctions(Ty t) {
            String s = t.suffix;
            String w = t.wurst;
            switch (t) {
                case INT -> {
                    line(0, "function mkInt(int n) returns int");
                    line(1, "return n");
                    line(0, "function keyInt(int v) returns int");
                    line(1, "return v");
                }
                case STRING -> {
                    line(0, "function mkStr(int n) returns string");
                    for (int n : new int[]{10, 11, 20, 21}) {
                        line(1, "if n == " + n);
                        line(2, "return \"v" + n + "\"");
                    }
                    line(1, "return \"other\"");
                    line(0, "function keyStr(string v) returns int");
                    for (int n : new int[]{10, 11, 20, 21}) {
                        line(1, "if v == \"v" + n + "\"");
                        line(2, "return " + n);
                    }
                    line(1, "return 0");
                }
                case CA, CB -> {
                    line(0, "class " + w);
                    line(1, "int tag");
                    line(1, "construct()");
                    line(2, "tag = " + t.constructedTag());
                    line(1, "construct(int t)");
                    line(2, "tag = t");
                    line(0, "function mk" + s + "(int n) returns " + w);
                    line(1, "return new " + w + "(n)");
                    line(0, "function key" + s + "(" + w + " v) returns int");
                    line(1, "if v == null");
                    line(2, "return 0");
                    line(1, "return v.tag");
                }
                case PA -> {
                    line(0, "tuple pa(int x, int y)");
                    line(0, "function mkPa(int n) returns pa");
                    line(1, "return pa(n div 10, n mod 10)");
                    line(0, "function keyPa(pa v) returns int");
                    line(1, "return v.x * 10 + v.y");
                }
                case PB -> {
                    line(0, "tuple pb(int a, int b, int c)");
                    line(0, "function mkPb(int n) returns pb");
                    line(1, "return pb(n div 10, 5, n mod 10)");
                    line(0, "function keyPb(pb v) returns int");
                    line(1, "return v.a * 10 + v.c + (v.b - 5) * 500");
                }
            }
        }

        private void codec() {
            line(0, "interface Codec<T:>");
            line(1, "function mk(int n) returns T");
            line(1, "function key(T v) returns int");
            for (Ty t : types()) {
                line(0, "class Codec" + t.suffix + " implements Codec<" + t.wurst + ">");
                line(1, "function mk(int n) returns " + t.wurst);
                line(2, "return mk" + t.suffix + "(n)");
                line(1, "function key(" + t.wurst + " v) returns int");
                line(2, "return key" + t.suffix + "(v)");
            }
        }

        private void bound() {
            line(0, "interface Kc<T:>");
            line(1, "function kmk(int n) returns T");
            line(1, "function kkey(T v) returns int");
            for (Ty t : types()) {
                line(0, "implements Kc<" + t.wurst + ">");
                line(1, "function kmk(int n) returns " + t.wurst);
                line(2, "return mk" + t.suffix + "(n)");
                line(1, "function kkey(" + t.wurst + " v) returns int");
                line(2, "return key" + t.suffix + "(v) + " + t.boundTag());
            }
        }

        private void cell() {
            line(0, "class Cell<U:>");
            line(1, "static U array vals");
            line(1, "static int count = 0");
            line(1, "function put(int i, U v)");
            line(2, "vals[i] = v");
            line(2, "count++");
            line(1, "function get(int i) returns U");
            line(2, "return vals[i]");
            line(1, "function writes() returns int");
            line(2, "return count");
        }

        /** The fields the dependency needs, in the class or module which holds the operations. */
        private void state(int d) {
            line(d, "int id = 0");
            switch (c.dep) {
                case STATIC -> {
                    line(d, "static T sv");
                    line(d, "static int cnt = 0");
                    line(d, "Codec<T> codec = null");
                }
                case FIXED_STATIC -> line(d, "Cell<" + x.wurst + "> cell = new Cell<" + x.wurst + ">()");
                case BOUND -> line(d, "T bv");
                case NEW_INSTANCE -> {
                    line(d, "T made");
                    line(d, "int madeK = 0");
                    line(d, "Codec<T> codec = null");
                }
                case INSTANCEOF -> line(d, "Marker held = null");
                case FIELD -> {
                    line(d, "T fv");
                    line(d, "Codec<T> codec = null");
                }
            }
            if (c.site == Site.CONSTRUCTOR || c.site == Site.ONDESTROY) {
                line(d, "int seen = 0");
            }
            if (c.site == Site.ONDESTROY) {
                line(d, "G<T> target = null");
                line(d, "int opK = 0");
                line(d, "int opMode = 0");
            }
        }

        /** The write, on the object {@code r} names (a prefix such as {@code "t."}, or empty for this). */
        private void writeOp(int d, String r, String k) {
            String n = r + "id * 10 + " + k;
            switch (c.dep) {
                case STATIC -> {
                    line(d, "sv = " + r + "codec.mk(" + n + ")");
                    line(d, "cnt += 1");
                }
                case FIXED_STATIC -> line(d, r + "cell.put(" + r + "id, mk" + x.suffix + "(" + n + "))");
                case BOUND -> line(d, r + "bv = T.kmk(" + n + ")");
                case NEW_INSTANCE -> {
                    line(d, r + "made = wurstNewInstance<T>()");
                    line(d, r + "madeK = " + k);
                }
                case INSTANCEOF -> {
                    line(d, "if " + k + " == 0");
                    line(d + 1, r + "held = new Box<T>()");
                    line(d, "else if " + r + "id == 1");
                    line(d + 1, r + "held = boxY");
                    line(d, "else");
                    line(d + 1, r + "held = boxX");
                }
                case FIELD -> line(d, r + "fv = " + r + "codec.mk(" + n + ")");
            }
        }

        /**
         * The read, as an int expression on the object {@code r} names; lines it needs first go at depth {@code d}.
         * The instanceof reads a local copy of the field: Jass translation rejects {@code this.held instanceof
         * Box<T>} in a generic class ("Could not find field array"), which is reported on its own.
         */
        private String readOp(int d, String r) {
            if (c.dep == Dep.INSTANCEOF) {
                line(d, "let h = " + r + "held");
            }
            return switch (c.dep) {
                case STATIC -> r + "codec.key(sv) + 1000 * cnt";
                case FIXED_STATIC -> "key" + x.suffix + "(" + r + "cell.get(" + r + "id)) + 1000 * " + r + "cell.writes()";
                case BOUND -> "T.kkey(" + r + "bv)";
                case NEW_INSTANCE -> r + "codec.key(" + r + "made) + 1000 * " + r + "madeK";
                case INSTANCEOF -> "b2i(h instanceof Box<T>) + 2 * b2i(boxX instanceof Box<T>)"
                    + " + 4 * b2i(boxY instanceof Box<T>)";
                case FIELD -> r + "codec.key(" + r + "fv)";
            };
        }

        private String ctorParams(String t) {
            return usesCodec() ? "Codec<" + t + "> c, int i" : "int i";
        }

        private String ctorArgs() {
            return usesCodec() ? "c, i" : "i";
        }

        private String supertypes(String t) {
            StringBuilder sb = new StringBuilder();
            if (base) {
                sb.append(" extends Base");
            }
            List<String> interfaces = new ArrayList<>();
            if (iface) {
                interfaces.add("Iface");
            }
            if (genericIface) {
                interfaces.add("GIface<" + t + ">");
            }
            if (!interfaces.isEmpty()) {
                sb.append(" implements ").append(String.join(", ", interfaces));
            }
            return sb.toString();
        }

        private void mainConstructor(int d) {
            line(d, "construct(" + ctorParams("T") + ")");
            if (usesCodec()) {
                line(d + 1, "codec = c");
            }
            line(d + 1, "id = i");
        }

        /** The operations w and r, as the site places them. */
        private void operations(int d) {
            String ov = base ? "override " : "";
            line(d, ov + "function w(int k)");
            switch (c.site) {
                case METHOD, INHERITED, MODULE -> writeOp(d + 1, "", "k");
                case CLOSURE -> {
                    line(d + 1, "Act a = () -> begin");
                    writeOp(d + 2, "", "k");
                    line(d + 1, "end");
                    line(d + 1, "a.run()");
                    line(d + 1, "destroy a");
                }
                case CONSTRUCTOR, ONDESTROY -> {
                    line(d + 1, "let p = new G<T>(this, k, 1)");
                    line(d + 1, "destroy p");
                }
            }
            line(d, ov + "function r() returns int");
            switch (c.site) {
                case METHOD, INHERITED, MODULE -> line(d + 1, "return " + readOp(d + 1, ""));
                case CLOSURE -> {
                    line(d + 1, "Fn f = () -> begin");
                    line(d + 2, "return " + readOp(d + 2, ""));
                    line(d + 1, "end");
                    line(d + 1, "let v = f.get()");
                    line(d + 1, "destroy f");
                    line(d + 1, "return v");
                }
                case CONSTRUCTOR, ONDESTROY -> {
                    line(d + 1, "let p = new G<T>(this, 0, 2)");
                    line(d + 1, "destroy p");
                    line(d + 1, "return seen");
                }
            }
        }

        private void subject() {
            switch (c.site) {
                case INHERITED -> {
                    line(0, "class GBase<" + typeParam + ">" + supertypes("T"));
                    state(1);
                    mainConstructor(1);
                    operations(1);
                    line(0, "class G<" + typeParam + "> extends GBase<T>");
                    line(1, "construct(" + ctorParams("T") + ")");
                    line(2, "super(" + ctorArgs() + ")");
                }
                case MODULE -> {
                    line(0, "module Ops<" + typeParam + ">");
                    state(1);
                    operations(1);
                    line(0, "class G<" + typeParam + ">" + supertypes("T"));
                    line(1, "use Ops<T>");
                    mainConstructor(1);
                }
                default -> {
                    line(0, "class G<" + typeParam + ">" + supertypes("T"));
                    state(1);
                    mainConstructor(1);
                    if (c.site == Site.CONSTRUCTOR) {
                        line(1, "construct(G<T> t, int k, int mode)");
                        line(2, "if mode == 1");
                        writeOp(3, "t.", "k");
                        line(2, "else");
                        line(3, "t.seen = " + readOp(3, "t."));
                    } else if (c.site == Site.ONDESTROY) {
                        line(1, "construct(G<T> t, int k, int mode)");
                        line(2, "target = t");
                        line(2, "opK = k");
                        line(2, "opMode = mode");
                        line(1, "ondestroy");
                        line(2, "if opMode == 1");
                        writeOp(3, "target.", "opK");
                        line(2, "else if opMode == 2");
                        line(3, "target.seen = " + readOp(3, "target."));
                    }
                    operations(1);
                }
            }
            if (c.uses(Path.SUPER)) {
                for (int which = 0; which < c.instantiations; which++) {
                    String t = c.type(which).wurst;
                    line(0, "class Sub" + (which + 1) + " extends G<" + t + ">");
                    line(1, "construct(" + ctorParams(t) + ")");
                    line(2, "super(" + ctorArgs() + ")");
                    if (c.writer == Path.SUPER) {
                        line(1, "override function w(int k)");
                        line(2, "super.w(k)");
                    }
                    if (c.reader == Path.SUPER) {
                        line(1, "override function r() returns int");
                        line(2, "return super.r()");
                    }
                }
            }
        }

        /** The non-generic and generic helpers the paths go through. */
        private void paths() {
            if (base) {
                line(0, "function writeBase(Base b, int k)");
                line(1, "b.w(k)");
                line(0, "function readBase(Base b) returns int");
                line(1, "return b.r()");
            }
            if (iface) {
                line(0, "function writeIface(Iface i, int k)");
                line(1, "i.w(k)");
                line(0, "function readIface(Iface i) returns int");
                line(1, "return i.r()");
            }
            if (c.uses(Path.ERASED_FUNCTION)) {
                line(0, "function callW<" + typeParam + ">(G<T> g, int k)");
                line(1, "g.w(k)");
                line(0, "function callR<" + typeParam + ">(G<T> g) returns int");
                line(1, "return g.r()");
            }
            if (c.uses(Path.DESTROY)) {
                line(0, "int lastRead = 0");
                line(0, "class Firing");
                line(1, "int fired = 0");
                line(0, "class Fire<" + typeParam + "> extends Firing");
                line(1, "G<T> g");
                line(1, "int k");
                line(1, "int mode");
                line(1, "construct(G<T> g, int k, int mode)");
                line(2, "this.g = g");
                line(2, "this.k = k");
                line(2, "this.mode = mode");
                line(1, "ondestroy");
                line(2, "if mode == 1");
                line(3, "g.w(k)");
                line(2, "else");
                line(3, "lastRead = g.r()");
            }
        }

        private void mainBlock() {
            line(0, "init");
            if (c.dep == Dep.INSTANCEOF) {
                line(1, "boxX = new Box<" + x.wurst + ">()");
                line(1, "boxY = new Box<" + y.wurst + ">()");
            }
            for (int which = 0; which < c.instantiations; which++) {
                Ty t = c.type(which);
                String cls = c.uses(Path.SUPER) ? "Sub" + (which + 1) : "G<" + t.wurst + ">";
                String args = (usesCodec() ? "new Codec" + t.suffix + "(), " : "") + (which + 1);
                line(1, "let g" + which + " = new " + cls + "(" + args + ")");
            }
            for (int which = 0; which < c.instantiations; which++) {
                writer("g" + which, c.type(which).wurst, String.valueOf(which), which);
            }
            for (int which = 0; which < c.instantiations; which++) {
                String read = reader("g" + which, c.type(which).wurst, which);
                line(1, "check(" + read + ", " + expected(c, which) + ", \"instantiation " + (which + 1) + "\")");
            }
            line(1, "testSuccess()");
        }

        private void writer(String g, String t, String k, int which) {
            switch (c.writer) {
                case DIRECT, SUPER -> line(1, g + ".w(" + k + ")");
                case BASE -> line(1, "writeBase(" + g + ", " + k + ")");
                case INTERFACE -> line(1, "writeIface(" + g + ", " + k + ")");
                case GENERIC_INTERFACE -> {
                    line(1, "GIface<" + t + "> gw" + which + " = " + g);
                    line(1, "gw" + which + ".w(" + k + ")");
                }
                case CLOSURE -> {
                    line(1, "Act aw" + which + " = () -> " + g + ".w(" + k + ")");
                    line(1, "aw" + which + ".run()");
                }
                case DESTROY -> {
                    line(1, "Firing fw" + which + " = new Fire<" + t + ">(" + g + ", " + k + ", 1)");
                    line(1, "destroy fw" + which);
                }
                case ERASED_FUNCTION -> line(1, "callW(" + g + ", " + k + ")");
            }
        }

        /** Emits what the read needs first and returns the expression which reads. */
        private String reader(String g, String t, int which) {
            return switch (c.reader) {
                case DIRECT, SUPER -> g + ".r()";
                case BASE -> "readBase(" + g + ")";
                case INTERFACE -> "readIface(" + g + ")";
                case GENERIC_INTERFACE -> {
                    line(1, "GIface<" + t + "> gr" + which + " = " + g);
                    yield "gr" + which + ".r()";
                }
                case CLOSURE -> {
                    line(1, "Fn fr" + which + " = () -> " + g + ".r()");
                    yield "fr" + which + ".get()";
                }
                case DESTROY -> {
                    line(1, "Firing fr" + which + " = new Fire<" + t + ">(" + g + ", 0, 2)");
                    line(1, "destroy fr" + which);
                    yield "lastRead";
                }
                case ERASED_FUNCTION -> "callR(" + g + ")";
            };
        }
    }

    // ------------------------------------------------------------------------------------------------------
    // Running a case

    enum Kind { PASS, SKIP, FAIL }

    record Outcome(Kind kind, String detail) {
    }

    private static final Map<String, Integer> skipReasons = new TreeMap<>();
    private static final List<String> failures = new ArrayList<>();
    private static final Map<String, String> knownFailing = new TreeMap<>();
    private static final List<String> nowPassing = new ArrayList<>();
    private static int passed = 0;

    /** The resource which lists the cases known to fail, each with the defect it shows. */
    static final String KNOWN_FAILURES = "genericSpecialisationMatrix-knownFailures.txt";

    /** Case name to defect, from {@link #KNOWN_FAILURES}. */
    static Map<String, String> knownFailures() throws IOException {
        Map<String, String> known = new LinkedHashMap<>();
        try (var in = GenericSpecialisationMatrixTests.class.getClassLoader().getResourceAsStream(KNOWN_FAILURES)) {
            if (in == null) {
                throw new IOException("missing test resource " + KNOWN_FAILURES);
            }
            for (String l : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\\r?\\n")) {
                l = l.strip();
                if (l.isEmpty() || l.startsWith("#")) {
                    continue;
                }
                String[] parts = l.split("\\s+", 2);
                known.put(parts[0], parts.length > 1 ? parts[1] : "?");
            }
        }
        return known;
    }

    private Map<String, String> known;

    @DataProvider
    public Object[][] combinations() {
        List<Combo> valid = new ArrayList<>();
        List<Combo> all = fullProduct();
        String mode = System.getenv("WURST_GENERIC_MATRIX");
        boolean full = "full".equals(mode);
        for (Combo c : all) {
            if (full || notAProgram(c) == null) {
                valid.add(c);
            }
        }
        List<Combo> cases = full ? valid : coveringSubset(valid);
        String shard = System.getenv("WURST_GENERIC_MATRIX_SHARD");
        if (shard != null) {
            String[] parts = shard.split("/");
            int index = Integer.parseInt(parts[0]);
            int count = Integer.parseInt(parts[1]);
            List<Combo> mine = new ArrayList<>();
            for (int i = index; i < cases.size(); i += count) {
                mine.add(cases.get(i));
            }
            cases = mine;
        }
        String filter = System.getenv("WURST_GENERIC_MATRIX_FILTER");
        if (filter != null) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(filter);
            cases = cases.stream().filter(c -> pattern.matcher(c.name()).find()).toList();
        }
        return cases.stream().map(c -> new Object[]{c}).toArray(Object[][]::new);
    }

    @BeforeClass
    public void forgetEarlierResults() throws IOException {
        Files.deleteIfExists(new File(new File(TEST_OUTPUT_PATH, "generic-matrix"), "results.txt").toPath());
    }

    @Test(dataProvider = "combinations")
    public void luaAgreesWithJass(Combo c) throws Exception {
        Outcome outcome = run(c);
        // One line per case as it ends, so that a long run which stops still says what it found.
        File dir = new File(TEST_OUTPUT_PATH, "generic-matrix");
        dir.mkdirs();
        Files.writeString(new File(dir, "results.txt").toPath(),
            outcome.kind + " " + c.name() + " " + outcome.detail.replace('\n', ' ') + "\n", StandardCharsets.UTF_8,
            java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        if (known == null) {
            known = knownFailures();
        }
        String defect = known.get(c.name());
        synchronized (failures) {
            switch (outcome.kind) {
                case PASS -> {
                    passed++;
                    if (defect != null) {
                        nowPassing.add(c.name() + " " + defect);
                    }
                }
                case SKIP -> skipReasons.merge(outcome.detail, 1, Integer::sum);
                case FAIL -> {
                    if (defect != null) {
                        knownFailing.put(c.name(), defect);
                    } else {
                        failures.add(c.name() + ": " + outcome.detail);
                    }
                }
            }
        }
        switch (outcome.kind) {
            case SKIP -> throw new SkipException(outcome.detail);
            case FAIL -> {
                if (defect != null) {
                    throw new SkipException("known defect " + defect + " (BACKLOG.md): " + outcome.detail);
                }
                if (System.getenv("WURST_GENERIC_MATRIX_KEEP_GOING") == null) {
                    fail(c.name() + ": " + outcome.detail + "\n" + program(c));
                }
            }
            default -> {
            }
        }
    }

    @AfterClass
    public void summary() {
        StringBuilder sb = new StringBuilder("[generic-matrix] passed " + passed + ", failed " + failures.size()
            + ", known to fail " + knownFailing.size() + ", skipped "
            + skipReasons.values().stream().mapToInt(i -> i).sum() + "\n");
        // A defect which is fixed leaves its cases in the list: say so, so that the list can follow.
        nowPassing.forEach(n -> sb.append("  known to fail but passes, remove from ").append(KNOWN_FAILURES)
            .append(": ").append(n).append('\n'));
        skipReasons.forEach((reason, count) -> sb.append("  skipped ").append(count).append(": ").append(reason)
            .append('\n'));
        failures.forEach(f -> sb.append("  FAILED ").append(f).append('\n'));
        System.out.println(sb);
        try {
            File dir = new File(TEST_OUTPUT_PATH, "generic-matrix");
            dir.mkdirs();
            Files.writeString(new File(dir, "summary.txt").toPath(), sb.toString(), StandardCharsets.UTF_8);
            // What the list would be after this run, for a change which fixes or finds defects.
            Map<String, String> after = new TreeMap<>(knownFailing);
            failures.forEach(f -> after.put(f.substring(0, f.indexOf(": ")), "new"));
            StringBuilder list = new StringBuilder();
            after.forEach((name, defect) -> list.append(name).append(' ').append(defect).append('\n'));
            Files.writeString(new File(dir, "knownFailures-after-this-run.txt").toPath(), list.toString(),
                StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (System.getenv("WURST_GENERIC_MATRIX_KEEP_GOING") != null) {
            assertTrue(failures.size() + " combinations failed:\n" + String.join("\n", failures), failures.isEmpty());
        }
    }

    private Outcome run(Combo c) throws IOException, InterruptedException {
        String rule = notAProgram(c);
        if (rule != null) {
            return new Outcome(Kind.SKIP, rule);
        }
        return runSource(c.name(), program(c), c.optimised);
    }

    /** Runs one program on both targets; package-visible so that a reduced repro can go through the same driver. */
    Outcome runSource(String name, String source, boolean optimised) throws IOException, InterruptedException {
        RunArgs args = optimised ? new RunArgs().with("-inline", "-localOptimizations") : new RunArgs();
        RecycleCodeGeneratorQueue.setTestMode = true;
        try {
            // Each target gets its own compilation, so that an error on one does not reach the other.
            Compilation forJass = checked(source, args);
            if (forJass.error != null) {
                return keep(name, source, null, new Outcome(Kind.FAIL,
                    "the program does not type-check, which no rule predicts: " + forJass.error));
            }
            String jass = jass(forJass);
            String[] lua = lua(checked(source, args.with("-lua")));
            if (jass == null && lua[0] == null) {
                return new Outcome(Kind.PASS, "");
            }
            String detail = jass == null ? "Lua " + lua[0]
                : "Jass (the oracle) " + jass + "; Lua " + (lua[0] == null ? "passes" : lua[0]);
            return keep(name, source, lua[1], new Outcome(Kind.FAIL, detail));
        } finally {
            RecycleCodeGeneratorQueue.setTestMode = false;
        }
    }

    /** A program parsed and checked, or the first error which stopped that. */
    private record Compilation(WurstCompilerJassImpl compiler, WurstGui gui, WurstModel model, String error) {
    }

    private Compilation checked(String source, RunArgs args) {
        WurstGui gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, args);
        WurstModel model = parseFiles(Collections.emptyList(),
            Collections.singletonList(new CU("Matrix.wurst", source)), false, compiler);
        if (gui.getErrorList().isEmpty()) {
            compiler.checkProg(model);
        }
        String error = gui.getErrorList().isEmpty() ? null : gui.getErrorList().get(0).toString();
        return new Compilation(compiler, gui, model, error);
    }

    /** Why the Jass translation or run did not succeed, or null. */
    private static String jass(Compilation c) {
        WurstCompilerJassImpl compiler = c.compiler;
        JassProg prog;
        try {
            compiler.translateProgToIm(c.model);
            compiler.runCompiletime(WurstProjectConfigData.empty(), false, false);
            prog = compiler.transformProgToJass();
        } catch (CompileError e) {
            return "translation rejects it: " + e;
        } catch (RuntimeException | Error e) {
            rethrowIfFatal(e);
            return "translation fails: " + e;
        }
        if (!c.gui.getErrorList().isEmpty()) {
            return "translation rejects it: " + c.gui.getErrorList().get(0);
        }
        return runJass(prog);
    }

    /** Why the Lua translation or run did not succeed, or null, and the script. */
    private String[] lua(Compilation c) throws IOException, InterruptedException {
        WurstCompilerJassImpl compiler = c.compiler;
        String script = null;
        try {
            compiler.translateProgToIm(c.model);
            compiler.runCompiletime(WurstProjectConfigData.empty(), false, false);
            LuaCompilationUnit luaCode = compiler.transformProgToLua();
            if (!c.gui.getErrorList().isEmpty()) {
                return new String[]{"translation rejects it: " + c.gui.getErrorList().get(0), null};
            }
            StringBuilder sb = new StringBuilder();
            luaCode.print(sb, 0);
            script = sb.toString();
            LuaTranslator translator = compiler.getLuaTranslator();
            return new String[]{runLua(script, translator == null ? List.of() : translator.initFunctionNames()),
                script};
        } catch (CompileError e) {
            return new String[]{"translation rejects it: " + e, script};
        } catch (RuntimeException | Error e) {
            rethrowIfFatal(e);
            return new String[]{"translation fails: " + e, script};
        }
    }

    /** Running out of memory, or a host without Lua, is not a finding about the case. */
    private static void rethrowIfFatal(Throwable t) {
        if (t instanceof VirtualMachineError && !(t instanceof StackOverflowError)) {
            throw (VirtualMachineError) t;
        }
        if (t instanceof SkipException skip) {
            throw skip;
        }
    }

    /** Why the Jass run did not succeed, or null. */
    private static String runJass(JassProg prog) {
        try {
            JassInterpreter interpreter = new JassInterpreter();
            interpreter.loadProgram(prog);
            interpreter.runProgram();
            return "ended without testSuccess";
        } catch (TestSuccessException e) {
            return null;
        } catch (TestFailException e) {
            return "failed: " + e.getMessage();
        } catch (RuntimeException | Error e) {
            rethrowIfFatal(e);
            return "raised " + e;
        }
    }

    /** Why the Lua run did not succeed, or null. */
    private String runLua(String script, List<String> initFunctions) throws IOException, InterruptedException {
        String luaExecutable;
        try {
            luaExecutable = getLuaExecutable();
        } catch (IllegalStateException e) {
            throw new SkipException("Skipped: the matrix runs the scripts in Lua: " + e.getMessage());
        }
        File file = File.createTempFile("matrix", ".lua");
        File output = File.createTempFile("matrix", ".txt");
        try {
            Files.writeString(file.toPath(), script, StandardCharsets.UTF_8);
            StringBuilder chunk = new StringBuilder("dofile(os.getenv('WURST_MATRIX_SCRIPT'));");
            // The script reports a package whose init raised and goes on without it: see the raise itself.
            chunk.append("local inits = {");
            for (String name : initFunctions) {
                chunk.append("'").append(name).append("',");
            }
            chunk.append("};");
            chunk.append("for _, n in ipairs(inits) do local f = rawget(_G, n); if f then"
                + " _G[n] = function(...) local r = table.pack(pcall(f, ...));"
                + " if not r[1] then print('#initFailed ' .. tostring(r[2])); error(r[2], 0) end"
                + " return table.unpack(r, 2, r.n) end end end;");
            chunk.append("testSuccess = function() print('#testSuccess') os.exit() end;");
            chunk.append("testFail = function(message) print('#testFail ' .. message); error(message, 0) end;");
            chunk.append("main()");
            ProcessBuilder builder = new ProcessBuilder(luaExecutable, "-e", chunk.toString())
                .redirectErrorStream(true).redirectOutput(output);
            builder.environment().put("WURST_MATRIX_SCRIPT", file.getPath());
            Process p = builder.start();
            if (!p.waitFor(20, TimeUnit.SECONDS)) {
                p.destroyForcibly().waitFor(10, TimeUnit.SECONDS);
                return "did not end within 20 s";
            }
            String text = Files.readString(output.toPath(), StandardCharsets.UTF_8).replace("\r\n", "\n").trim();
            for (String l : text.split("\n")) {
                if (l.startsWith("#testFail") || l.startsWith("#initFailed")) {
                    return "failed: " + l.substring(1);
                }
            }
            if (!text.contains("#testSuccess")) {
                return "ended without testSuccess: " + text;
            }
            return null;
        } finally {
            file.delete();
            output.delete();
        }
    }

    private static Outcome keep(String name, String source, String lua, Outcome outcome) throws IOException {
        File dir = new File(TEST_OUTPUT_PATH, "generic-matrix");
        dir.mkdirs();
        Files.writeString(new File(dir, name + ".wurst").toPath(), source, StandardCharsets.UTF_8);
        if (lua != null) {
            Files.writeString(new File(dir, name + ".lua").toPath(), lua, StandardCharsets.UTF_8);
        }
        return outcome;
    }
}
