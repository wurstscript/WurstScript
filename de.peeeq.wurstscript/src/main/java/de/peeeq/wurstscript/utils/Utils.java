package de.peeeq.wurstscript.utils;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.io.Files;
import de.peeeq.wurstio.Pjass;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.prettyPrint.DefaultSpacer;
import de.peeeq.wurstscript.jassIm.JassImElementWithName;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeUnknown;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Utils {

    @SuppressWarnings("rawtypes")
    public static int size(Iterable<?> i) {
        if (i instanceof Collection) {
            return ((Collection) i).size();
        }
        int size = 0;
        for (@SuppressWarnings("unused")
                Object o : i) {
            size++;
        }
        return size;
    }

    public static void printIndent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append("\t");
        }
    }


    @SafeVarargs
    public static <T> List<T> list(T... args) {
        List<T> result = new NotNullList<>();
        Collections.addAll(result, args);
        return result;
    }

    public static <T> List<T> removedDuplicates(List<T> list) {
        List<T> result = new NotNullList<>();
        for (T t : list) {
            if (!result.contains(t)) {
                result.add(t);
            }
        }
        return result;

    }


    public static <T> void printSep(StringBuilder sb, String seperator, T[] args) {
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(seperator);
            }
            sb.append(args[i]);
        }
    }


    public static int parseInt(String yytext) {
        if (yytext.startsWith("0")) {
            return Integer.parseInt(yytext, 8);
        } else {
            return Integer.parseInt(yytext);
        }
    }

    public static int parseAsciiInt(String yytext) throws NumberFormatException {
        if (yytext.length() == 3) {
            // 'x' can directly return
            return yytext.charAt(1);
        }
        int result = 0;
        int i = 1;
        int chars = 0;
        for (; i < yytext.length() - 1; i++) {
            if (yytext.charAt(i) == '\\') {
                i++;
            }
            result = result * 256 + yytext.charAt(i);
            chars++;
        }
        if (chars != 1 && chars != 4) {
            throw new NumberFormatException("Ascii ints must have 4 or 1 characters but this one has " + chars + " characters.");
        }
        return result;
    }

    public static int parseOctalInt(String yytext) {
        return (int) Long.parseLong(yytext, 8);
    }

    public static int parseHexInt(String yytext, int offset) {
        if (yytext.startsWith("-")) {
            return (int) -Long.parseLong(yytext.substring(offset+1), 16);
        } else {
            return (int) Long.parseLong(yytext.substring(offset), 16);
        }
    }

    public static <T> String printSep(String sep, T[] args) {
        StringBuilder sb = new StringBuilder();
        printSep(sb, sep, args);
        return sb.toString();
    }

    public static String printSep(String sep, List<?> args) {
        return args.stream().map(String::valueOf).collect(Collectors.joining(sep));
    }

    /**
     * is a piece of code jass code?
     */
    public static boolean isJassCode(Optional<Element> pos) {
        while (pos.isPresent()) {
            if (pos.get() instanceof WPackage) {
                return false; // code is inside package -> wurstscript code
            }
            pos = Optional.ofNullable(pos.get().getParent());
        }
        return true; // no package found -> jass code
    }


    public static <T> String join(Iterable<T> hints, String seperator) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (T s : hints) {
            if (!first) {
                builder.append(seperator);
            }
            builder.append(s);
            first = false;
        }
        return builder.toString();
    }

    public static <T> String join(T[] arguments, String seperator) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (T s : arguments) {
            if (!first) {
                builder.append(seperator);
            }
            builder.append(s);
            first = false;
        }
        return builder.toString();
    }


    /**
     * sorts a list with partitial ordering topologically. If a > b then a will
     * appear before b in the result list
     *
     * @param items       items to sort
     * @param biggerItems a function to get all the bigger items for a given item
     * @return a sorted list
     * @throws TopsortCycleException if there exist items a,b so that a > b and b > a
     */
    public static <T> List<T> topSort(Collection<T> items,
                                      java.util.function.Function<T, ? extends Collection<T>> biggerItems)
            throws TopsortCycleException {
        Set<T> visitedItems = new HashSet<>();
        List<T> result = new ArrayList<>(items.size());
        LinkedList<T> activeItems = Lists.newLinkedList();
        for (T t : items) {
            if (t == null)
                throw new IllegalArgumentException();
            topSortHelper(result, visitedItems, activeItems, biggerItems::apply, t);
        }
        return result;
    }


    private static <T> void topSortHelper(List<T> result, Set<T> visitedItems,
                                          LinkedList<T> activeItems,
                                          Function<T, ? extends Collection<T>> biggerItems, T item)
            throws TopsortCycleException {
        if (visitedItems.contains(item)) {
            return;
        }
        if (activeItems.contains(item)) { // This is not constant time, could be
            // more efficient
            while (activeItems.get(0) != item) {
                activeItems.remove(0);
            }
            throw new TopsortCycleException(activeItems);
        }
        activeItems.add(item);
        visitedItems.add(item);
        for (T t : biggerItems.apply(item)) {
            if (t == null)
                throw new IllegalArgumentException();
            topSortHelper(result, visitedItems, activeItems, biggerItems, t);
        }
        result.add(item);
        activeItems.removeLast();
    }

    @SafeVarargs
    public static <T> boolean oneOf(T obj, T... ts) {
        for (T t : ts) {
            if (t.equals(obj)) {
                return true;
            }
        }
        return false;
    }


    public static <T> T getFirst(Iterable<T> ts) {
        for (T t : ts) {
            return t;
        }
        throw new Error("collection has no first element");
    }

    public static <T> T getLast(List<T> ts) {
        return ts.get(ts.size() - 1);
    }


    public static <T extends Element> String printElement(Optional<T> el) {
        return el.map(e -> {
            String type = makeReadableTypeName(e);
            String name = "";
            if (e instanceof ExprFunctionCall) {
                ExprFunctionCall fc = (ExprFunctionCall) e;
                return "function call " + fc.getFuncName() + "()";
            } else if (e instanceof FuncDef) {
                FuncDef fd = (FuncDef) e;
                return "function " + fd.getName();
            } else if (e instanceof OnDestroyDef) {
                return "destroy function for "
                        + e.attrNearestStructureDef().getName();
            } else if (e instanceof ConstructorDef) {
                return "constructor for " + e.attrNearestStructureDef().getName();
            } else if (e instanceof LocalVarDef) {
                LocalVarDef l = (LocalVarDef) e;
                return "local variable " + l.getName();
            } else if (e instanceof VarDef) {
                VarDef l = (VarDef) e;
                return "variable " + l.getName();
            } else if (e instanceof AstElementWithNameId) {
                name = ((AstElementWithNameId) e).getNameId().getName();
            } else if (e instanceof WImport) {
                WImport wImport = (WImport) e;
                return "import " + wImport.getPackagename();
            } else if (e instanceof TypeExprSimple) {
                TypeExprSimple t = (TypeExprSimple) e;
                name = t.getTypeName();
                if (t.getTypeArgs().size() > 0) {
                    name += "{";
                    boolean first = true;
                    StringBuilder builder = new StringBuilder();
                    builder.append(name);
                    for (TypeExpr ta : t.getTypeArgs()) {
                        if (!first) {
                            builder.append(", ");
                        }
                        builder.append(printElement(ta));
                        first = false;
                    }
                    name = builder.toString();
                    name += "}";
                }
                type = "type";
            }
            return type + " " + name;
        }).orElse("null");
    }

    public static String printElement(@Nullable Element e) {
        return printElement(Optional.ofNullable(e));
    }

    private static String makeReadableTypeName(Element e) {
        String type = e.getClass().getName()
                .replaceAll("de.peeeq.wurstscript.ast.", "")
                .replaceAll("Impl$", "").replaceAll("Def$", "").toLowerCase();
        if (type.equals("wpackage")) {
            type = "package";
        }
        return type;
    }

    public static int inBorders(int min, int x, int max) {
        return Math.max(min, Math.min(max, x));
    }

    public static String printStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder builder = new StringBuilder();
        for (StackTraceElement s : stackTrace) {
            builder.append(s.toString());
            builder.append("\n");
        }
        return builder.toString();
    }

    public static String printExceptionWithStackTrace(Throwable t) {
        StringBuilder builder = new StringBuilder();
        builder.append(t);
        builder.append("\n");
        for (; ; ) {
            for (StackTraceElement s : t.getStackTrace()) {
                builder.append(s.toString());
                builder.append("\n");
            }
            t = t.getCause();
            if (t == null) {
                break;
            }
            builder.append("Caused by: ").append(t.toString()).append("\n");
        }
        return builder.toString();
    }


    public static Optional<Element> getAstElementAtPos(Element elem,
                                             int caretPosition, boolean usesMouse) {
        List<Element> betterResults = Lists.newArrayList();
        for (int i = 0; i < elem.size(); i++) {
            getAstElementAtPos(elem.get(i), caretPosition, usesMouse).map(el -> betterResults.add(el));
        }
        if (betterResults.size() == 0) {
            if (elem instanceof Identifier) {
                return Optional.ofNullable(elem.getParent());
            }
            return Optional.ofNullable(elem);
        } else {
            return bestResult(betterResults);
        }
    }

    public static Optional<Element> getAstElementAtPos(Element elem, int line, int column, boolean usesMouse) {
        if (elem instanceof ModuleInstanciation) {
            // do not helicopter into module instantiations
            return Optional.of(elem);
        }
        List<Element> betterResults = Lists.newArrayList();
        for (int i = 0; i < elem.size(); i++) {
            Element e = elem.get(i);
            if (elementContainsPos(e, line, column, usesMouse) || e.attrSource().isArtificial()) {
                getAstElementAtPos(e, line, column, usesMouse).map(el -> betterResults.add(el));
            }
        }
        Optional<Element> bestResult = bestResult(betterResults);
        if (!bestResult.isPresent()) {
            if (elem instanceof Identifier) {
                return Optional.ofNullable(elem.getParent());
            }
            return Optional.of(elem);
        } else {
            return bestResult;
        }
    }


    public static Optional<Element> getAstElementAtPosIgnoringLists(Element elem,
                                                          int caretPosition, boolean usesMouse) {
        Optional<Element> r = getAstElementAtPos(elem, caretPosition, usesMouse);
        while (r.isPresent() && r.get() instanceof List<?>) {
            r = r.flatMap(el -> Optional.ofNullable(el.getParent()));
        }
        return r;
    }

    /**
     * return the element with the smallest size
     */
    private static Optional<Element> bestResult(List<Element> betterResults) {
        int minSize = Integer.MAX_VALUE;
        Optional<Element> min = Optional.empty();
        for (Element e : betterResults) {
            WPos source = e.attrSource();
            int size = source.isArtificial() ?
                    Integer.MAX_VALUE
                    : source.getRightPos() - source.getLeftPos();
            if (size < minSize) {
                minSize = size;
                min = Optional.of(e);
            }
        }
        return min;
    }

    public static boolean elementContainsPos(Element e, int pos, boolean usesMouse) {
        return e.attrSource().getLeftPos() <= pos
                && e.attrSource().getRightPos() >= pos + (usesMouse ? 1 : 0);
    }

    private static boolean elementContainsPos(Element e, int line, int column, boolean usesMouse) {
        WPos pos = e.attrSource();
        if (pos.getLine() > line) {
            return false;
        }
        if (pos.getEndLine() < line) {
            return false;
        }
        return (pos.getLine() < line || pos.getStartColumn() <= column)
                && (pos.getEndLine() > line || pos.getEndColumn() >= column);
    }


    public static <T extends NameDef> List<T> sortByName(
            Collection<T> c) {
        List<T> r = Lists.newArrayList(c);
        r.sort(Comparator.comparing(NameDef::getName));
        return r;
    }

    public static String printPos(WPos source) {
        return source.getFile() + ", line " + source.getLine();
    }

    public static boolean isEmptyCU(Optional<CompilationUnit> cu) {
        return !cu.isPresent()
                || (cu.get().getJassDecls().size() + cu.get().getPackages().size() == 0);
    }

    public static String printElementWithSource(Optional<Element> e) {
        Optional<WPos> src = e.map(el -> el.attrSource());
        return printElement(e) + " (" + src.map(sf -> sf.getFile()) + ":"
                + src.map(sf -> sf.getLine()) + ")";
    }

    public static int[] copyArray(int[] ar) {
        int[] r = new int[ar.length];
        System.arraycopy(ar, 0, r, 0, ar.length);
        return r;
    }

    public static String toFirstUpper(String s) {
        if (s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static Optional<VarDef> getParentVarDef(Optional<Element> node) {
        while (node.isPresent()) {
            if (node.get() instanceof VarDef) {
                return node.map(n -> (VarDef) n);
            }
            node = node.map(n -> n.getParent());
        }
        return null;
    }


    public static String printAlternatives(Collection<? extends NameLink> alternatives) {
        List<String> result = Lists.newArrayList();
        for (NameLink a : alternatives) {
            WPos source = a.getDef().attrSource();
            String s = Utils.printElement(a.getDef()) + " defined in line "
                    + source.getLine() + " (" + source.getFile() + ")";
            result.add(s);
        }
        return " * " + Utils.join(result, "\n * ");
    }

    public static <T, S> Multimap<T, S> inverse(Multimap<S, T> orig) {
        Multimap<T, S> result = LinkedHashMultimap.create();
        for (Entry<S, T> e : orig.entries()) {
            result.put(e.getValue(), e.getKey());
        }
        return result;
    }

    public static <T extends Comparable<? extends T>, S> TreeMap<T, Set<S>> inverseMapToSet(Map<S, T> orig) {
        TreeMap<T, Set<S>> result = new TreeMap<>();
        for (Entry<S, T> e : orig.entrySet()) {
            result.computeIfAbsent(e.getValue(), x -> new LinkedHashSet<>()).add(e.getKey());
        }
        return result;
    }


    public static boolean isSubsequenceIgnoreCase(String a, String b) {
        int bPos = -1;
        for (int i = 0; i < a.length(); i++) {
            char c = Character.toLowerCase(a.charAt(i));
            do {
                bPos++;
                if (bPos >= b.length()) {
                    return false;
                }
            } while (Character.toLowerCase(b.charAt(bPos)) != c);
        }
        return true;
    }

    public static boolean isSubsequence(String a, String b) {
        int bPos = -1;
        for (int i = 0; i < a.length(); i++) {
            char c = a.charAt(i);
            do {
                bPos++;
                if (bPos >= b.length()) {
                    return false;
                }
            } while (b.charAt(bPos) != c);
        }
        return true;
    }


    public static List<Integer> subsequenceLengthes(String a, String b) {
        List<Integer> subseqLength = Lists.newArrayList();
        while (!a.isEmpty()) {
            int prefixlen = a.length();
            while (prefixlen > 0 && !containsPrefix(b, a, prefixlen)) {
                prefixlen--;
            }
            if (prefixlen == 0) {
                subseqLength.add(0);
                break;
            }
            subseqLength.add(prefixlen);
            String found = a.substring(0, prefixlen);
            b = b.substring(prefixlen + b.indexOf(found));
            a = a.substring(prefixlen);

        }
        return subseqLength;
    }

    /**
     * checks if b contains the first n characters of a as a substring
     */
    private static boolean containsPrefix(String b, String a, int n) {
        return b.contains(a.substring(0, n));
    }

    public static <T> T getFirstAndOnly(Collection<T> c) {
        if (c.size() != 1) {
            throw new Error("Size must be 1 but was " + c.size());
        }
        return getFirst(c);
    }

    private static void escapeStringParts(String v, StringBuilder builder) {
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            switch (c) {
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\"':
                    builder.append("\\\"");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                default:
                    builder.append(c);
            }
        }
    }

    public static String escapeStringWithoutQuotes(String v) {
        StringBuilder builder = new StringBuilder();
        escapeStringParts(v, builder);
        return builder.toString();
    }

    public static String escapeString(String v) {
        StringBuilder builder = new StringBuilder();
        builder.append("\"");
        escapeStringParts(v, builder);
        builder.append("\"");
        return builder.toString();
    }

    public static String escapeHtml(String s) {
        s = s.replace("<", "&lt;");
        s = s.replace(">", "&gt;");
        return s;
    }

    /**
     * returns the filename from the given path
     */
    public static String fileName(String path) {
        int pos = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (pos > 0) {
            return path.substring(pos + 1);
        }
        return path;
    }

    public static String printException(Throwable e) {
        return e + "\n" + Utils.printExceptionWithStackTrace(e);
    }


    public static String stripHtml(String s) {
        return s.replaceAll("<.*?>", "");
    }


    public static <T> Iterable<T> iterateReverse(final List<T> elements) {
        return () -> new Iterator<T>() {

            ListIterator<T> it = elements.listIterator(elements.size());

            @Override
            public boolean hasNext() {
                return it.hasPrevious();
            }

            @Override
            public T next() {
                return it.previous();
            }

            @Override
            public void remove() {
                it.remove();
            }
        };
    }


    public static String readWholeStream(BufferedReader r) throws IOException {
        StringBuilder sb = new StringBuilder();
        Optional<String> line;
        while ((line = Optional.ofNullable(r.readLine())).isPresent()) {
            line.map(l -> sb.append(l));
        }
        return sb.toString();
    }

    public static String readWholeStream(InputStream inputStream) throws IOException {
        return readWholeStream(new BufferedReader(new InputStreamReader(inputStream)));
    }


    @SuppressWarnings("unchecked")
    public static <T extends Element> Optional<T> getNearestByType(Optional<Element> e, Class<T> clazz) {
        while (e.isPresent()) {
            if (clazz.isInstance(e.get())) {
                return Optional.of((T) e.get());
            }
            e = e.flatMap(el -> Optional.ofNullable(el.getParent()));
        }
        return Optional.empty();
    }


    public static <T extends JassImElementWithName> Comparator<T> compareByNameIm() {
        return Comparator.comparing(JassImElementWithName::getName);
    }

    public static String getParameterListText(AstElementWithParameters f) {
        StringBuilder descr = new StringBuilder();
        for (WParameter p : f.getParameters()) {
            if (descr.length() > 0) {
                descr.append(", ");
            }
            descr.append(p.attrTyp()).append(" ").append(p.getName());
        }
        return descr.toString();
    }

    public static <T> List<T> subList(List<T> l, int start) {
        return subList(l, start, l.size() - 1);
    }

    public static <T> List<T> subList(List<T> l, int start, int stop) {
        List<T> result = Lists.newArrayListWithCapacity(stop - start);
        for (int i = start; i <= stop; i++) {
            result.add(l.get(i));
        }
        return result;
    }

    public static <K, V> ImmutableMap<K, V> mergeMaps(
            ImmutableMap<K, V> a,
            ImmutableMap<K, V> b,
            Function2<V, V, V> mergeFunc) {
        if (a.isEmpty()) return b;
        if (b.isEmpty()) return a;

        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();

        for (Entry<K, V> e : a.entrySet()) {
            K key = e.getKey();
            if (b.containsKey(key)) {
                builder.put(key, mergeFunc.apply(e.getValue(), b.get(key)));
            } else {
                builder.put(e);
            }
        }

        for (Entry<K, V> e : b.entrySet()) {
            K key = e.getKey();
            if (!a.containsKey(key)) {
                builder.put(e);
            }
        }

        return builder.build();
    }

    public static <K, V> ImmutableSetMultimap<K, V> mergeMultiMaps(
            ImmutableSetMultimap<K, V> a, ImmutableSetMultimap<K, V> b) {
        if (a.isEmpty()) return b;
        if (b.isEmpty()) return a;

        ImmutableSetMultimap.Builder<K, V> builder = ImmutableSetMultimap.builder();
        builder.putAll(a);
        builder.putAll(b);
        return builder.build();
    }

    public static <T> ImmutableSet<T> mergeSets(ImmutableSet<T> a, ImmutableSet<T> b) {
        ImmutableSet.Builder<T> builder = ImmutableSet.<T>builder();
        builder.addAll(a).addAll(b);
        return builder.build();
    }

    public static <T> T[] joinArrays(T[] a, T[] b) {
        T[] res = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, res, a.length, b.length);
        return res;
    }

    public static <K, V> void removeValuesFromMap(Map<K, V> map, Collection<V> removed) {
        map.entrySet().removeIf(e -> removed.contains(e.getValue()));
    }

    public static <T> ImmutableList<T> emptyList() {
        return ImmutableList.of();
    }

    public static <T>
    Collector<T, ?, ImmutableList<T>> toImmutableList() {
        return new Collector<T, Builder<T>, ImmutableList<T>>() {

            @Override
            public Supplier<Builder<T>> supplier() {
                return ImmutableList::builder;
            }

            @Override
            public BiConsumer<Builder<T>, T> accumulator() {
                return Builder::add;
            }

            @Override
            public BinaryOperator<Builder<T>> combiner() {
                return (a, b) -> a.addAll(b.build());
            }

            @Override
            public java.util.function.Function<Builder<T>, ImmutableList<T>> finisher() {
                return Builder::build;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
    }

    public static MouseListener onClickDo(Consumer<MouseEvent> onclick) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(@Nullable MouseEvent e) {
                Preconditions.checkNotNull(e);
                onclick.accept(e);
            }
        };
    }

    public static boolean isWurstFile(File f) {
        return isWurstFile(f.getName());
    }

    public static boolean isWurstFile(String fileName) {
        return fileName.endsWith(".wurst") || fileName.endsWith(".jurst");
    }

    public static String getLibName(File f) {
        return f.getName().replaceAll("\\.[jw]urst$", "");
    }


    public static String repeat(char c, int size) {
        StringBuilder result = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            result.append(c);
        }
        return result.toString();
    }

    public static boolean elementContained(Optional<Element> e, Element in) {
        while (e.isPresent()) {
            if (e.get() == in) {
                return true;
            }
            e = e.flatMap(el -> Optional.ofNullable(el.getParent()));
        }
        return false;
    }

    /**
     * Executes a shell command in a given folder and returns the output of the executed command
     */
    public static String exec(File folder, String... cmds) {
        try {
            Process p = new ProcessBuilder(cmds)
                    .directory(folder)
                    .start();
            int res = p.waitFor();
            if (res != 0) {
                throw new RuntimeException("Could not execute " + Utils.join(Arrays.asList(cmds), " ")
                        + "\nErrors:\n"
                        + convertStreamToString(p.getErrorStream())
                        + "\nOutput:\n"
                        + convertStreamToString(p.getInputStream()));
            }
            return convertStreamToString(p.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts an input stream to a string
     * <p>
     * see http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
     */
    public static String convertStreamToString(InputStream is) {
        try(Scanner s = new Scanner(is).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    public static byte[] convertStreamToBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    /**
     * join lines
     */
    public static String string(String... lines) {
        return Arrays.stream(lines).collect(Collectors.joining("\n")) + "\n";
    }

    /**
     * Extracts a resource from the jar, stores it in a temp file and returns the abolute path to the tempfile
     */
    public static synchronized String getResourceFile(String name) {
        return getResourceFileF(name).getAbsolutePath();
    }

    /**
     * Extracts a resource from the jar, stores it in a temp file and returns the abolute path to the tempfile
     */
    public static synchronized File getResourceFileF(String name) {
        try {
            Optional<File> f = Optional.ofNullable(resourceMap.get(name));
            if (f.isPresent() && f.get().exists()) {
                return f.get();
            }

            String[] parts = splitFilename(name);
            File fi = File.createTempFile(parts[0], parts[1]);
            fi.deleteOnExit();
            try (InputStream is = Pjass.class.getClassLoader().getResourceAsStream(name)) {
                if (is == null) {
                    throw new RuntimeException("Could not find resource file " + name);
                }
                byte[] bytes = Utils.convertStreamToBytes(is);
                Files.write(bytes, fi);
                resourceMap.put(name, fi);
                return fi;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static String[] splitFilename(String name) {
        int dotPos = name.lastIndexOf('.');
        if (dotPos >= 0) {
            return new String[] { name.substring(0, dotPos), name.substring(dotPos + 1) };
        }
        return new String[] { name, "" };
    }

    private static Map<String, File> resourceMap = new HashMap<>();

    public static String elementNameWithPath(AstElementWithNameId n) {
        StringBuilder result = new StringBuilder(n.getNameId().getName());
        Optional<Element> e = Optional.ofNullable(n.getParent());
        while (e.isPresent()) {
            if (e.get() instanceof AstElementWithNameId) {
                result.insert(0, ((AstElementWithNameId) e.get()).getNameId().getName() + "_");
            }
            e = e.flatMap(el -> Optional.ofNullable(el.getParent()));
        }
        return result.toString();
    }

    @SafeVarargs
    public static <T> ImmutableList<T> append(List<T> list, T ... elems) {
        Builder<T> builder = ImmutableList.builderWithExpectedSize(list.size() + elems.length);
        builder.addAll(list);
        for (T elem : elems) {
            builder.add(elem);
        }
        return builder.build();
    }

    @SafeVarargs
    public static <T> ImmutableList<T> concatLists(List<T> ...lists) {
        Builder<T> builder = ImmutableList.builder();
        for (List<T> list : lists) {
            builder.addAll(list);
        }
        return builder.build();
    }

    public static String prettyPrint(Element e) {
        StringBuilder sb = new StringBuilder();
        e.prettyPrint(new DefaultSpacer(), sb, 0);
        return sb.toString();
    }

    public static String prettyPrintWithLine(Element e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.attrSource().getFile()).append(":").append(e.attrSource().getLine()).append(": ");
        e.prettyPrint(new DefaultSpacer(), sb, 4);
        return sb.toString();
    }

    public static String printTypeExpr(TypeExpr t) {
        WurstType wt = t.attrTyp();
        if (wt instanceof WurstTypeUnknown) {
            if (t instanceof TypeExprSimple) {
                return ((TypeExprSimple) t).getTypeName();
            }
            return "???";
        }
        return wt.toString();
    }

    /**
     * Replaces oldElement with newElement in parent
     */
    public static void replace(de.peeeq.wurstscript.jassIm.Element parent, de.peeeq.wurstscript.jassIm.Element oldElement, de.peeeq.wurstscript.jassIm.Element newElement) {
        if (oldElement == newElement) {
            return;
        }
        de.peeeq.wurstscript.jassIm.Element oldElementParent = oldElement.getParent();
        for (int i=0; i<parent.size(); i++) {
            if (parent.get(i) == oldElement) {
                parent.set(i, newElement);
                // reset parent, because might be changed
                oldElement.setParent(oldElementParent);
                return;
            }
        }
        throw new CompileError(parent.attrTrace().attrSource(), "Could not find " + oldElement + " in " + parent);
    }

    /**
     * Copy of the list without its last element
     */
    public static <T> List<T> init(List<T> list) {
        return list.stream().limit(list.size() - 1).collect(Collectors.toList());
    }

    public static class ExecResult {
        private final String stdOut;
        private final String stdErr;

        public ExecResult(String stdOut, String stdErr) {
            this.stdOut = stdOut;
            this.stdErr = stdErr;
        }

        public String getStdOut() {
            return stdOut;
        }

        public String getStdErr() {
            return stdErr;
        }
    }

    public static ExecResult exec(ProcessBuilder pb, Duration duration, Consumer<String> onInput) throws IOException, InterruptedException {
        Process process = pb.start();
        class Collector extends Thread {
            private final StringBuilder sb = new StringBuilder();
            private final InputStream in;

            Collector(InputStream in) {
                this.in = in;
            }

            @Override
            public void run() {
                try (BufferedReader input = new BufferedReader(new InputStreamReader(in))) {
                    Optional<String> line;
                    while ((line = Optional.ofNullable(input.readLine())).isPresent()) {
                        onInput.accept(line.get());
                        sb.append(line.get()).append("\n");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            String getContents() {
                return sb.toString();
            }
        }

        Collector cIn = new Collector(process.getInputStream());
        cIn.start();
        Collector cErr = new Collector(process.getErrorStream());
        cErr.start();

        boolean r = process.waitFor(duration.toMillis(), TimeUnit.MILLISECONDS);
        process.destroyForcibly();
        cIn.join();
        cErr.join();
        if (!r) {
            throw new IOException("Timeout running external tool");
        }
        if (process.exitValue() != 0) {
            throw new IOException("Failure when running external tool");
        }
        return new ExecResult(cIn.getContents(), cErr.getContents());
    }

    public static String makeUniqueName(String baseName, Predicate<String> isValid) {
        if (isValid.test(baseName)) {
            return baseName;
        }
        int minI = 1;
        int maxI = 1;
        while (true) {
            String name = baseName + "_" + maxI;
            if (isValid.test(name)) {
                break;
            }
            minI = maxI;
            maxI *= 2;
        }

        while (minI < maxI) {
            int mid = minI + (maxI - minI) / 2;
            String name = baseName + "_" + mid;
            if (isValid.test(name)) {
                maxI = mid;
            } else {
                minI = mid + 1;
            }
        }
        return baseName + "_" + maxI;



    }




}
