package de.peeeq.wurstscript.attributes.prettyPrint;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static de.peeeq.wurstscript.utils.Utils.printElement;


public class PrettyUtils {

    /**
     * @param args
     */
    /**
     * What {@link #pretty(List)} does with a given argument list.
     *
     * <p>Split out from the dispatch so it can be asserted directly: the alternative is running the
     * real thing, and both the directory walk and the single-file branch print to stdout while
     * readFile swallows its own exceptions, so neither outcome is distinguishable from the other.
     */
    public enum PrettyAction {
        /** No arguments; nothing to do. */
        NONE,
        /** "..." - format every .wurst file below the root. */
        ALL,
        /** "tree <file>" - dump the parse tree. */
        TREE,
        /** Anything else is taken as a file name. */
        SINGLE_FILE
    }

    public static PrettyAction selectAction(List<String> args) {
        if (args.isEmpty()) {
            return PrettyAction.NONE;
        }
        String arg = args.get(0);
        // This used to read args.equals("..."), comparing the List itself to a String, which is
        // never true - so "..." fell through and was treated as a file name.
        if (arg.equals("...")) {
            return PrettyAction.ALL;
        }
        if (arg.equals("tree") && args.size() >= 2) {
            return PrettyAction.TREE;
        }
        return PrettyAction.SINGLE_FILE;
    }

    public static void pretty(List<String> args) throws IOException {
        switch (selectAction(args)) {
            case NONE -> {
            }
            case ALL -> prettyAll(".");
            case TREE -> debug(args.get(1));
            case SINGLE_FILE -> System.out.println(pretty(new File(args.get(0))));
        }
    }

    public static String pretty(String source, String ending) {
        CompilationUnit cu = parse(source, ending);

        Spacer spacer = new MaxOneSpacer();
        StringBuilder sb = new StringBuilder();
        cu.prettyPrint(spacer, sb, 0);

        return sb.toString();
    }

    private static void prettyPrint(String filename) {
        String clean = pretty(filename, filename.substring(filename.lastIndexOf(".")));
        System.out.println(clean);
    }

    public static void pretty(CompilationUnit cu) {
        Spacer spacer = new MaxOneSpacer();
        StringBuilder sb = new StringBuilder();
        cu.prettyPrint(spacer, sb, 0);

        System.out.println(sb);
    }

    private static void debug(String filename) {
        String contents = readFile(filename);
        CompilationUnit cu = parse(contents, filename.substring(filename.lastIndexOf(".")));

        walkTree(cu, 0);
    }

    private static void check(Element e, int indent) {
        System.out.println(StringUtils.repeat("\t", indent) + printElement(e));
    }

    private static @Nullable Element lastElement = null;
    private static void walkTree(Element e, int indent) {
        lastElement = e;
        check(e, indent);
        lastElement = null;
        for (int i = 0; i < e.size(); i++) {
            walkTree(e.get(i), indent+1);
        }
    }

    private static void prettyAll(String root) throws IOException {
        Files.walk(Paths.get(root))
                .filter(p -> p.toString().endsWith(".wurst"))
                .forEach(p -> prettyPrint(p.toString()));
    }

    private static String pretty(File f) {
        String contents = readFile(f.toString());
        CompilationUnit cu = parse(contents, f.getName().substring(f.getName().lastIndexOf(".")));

        Spacer spacer = new MaxOneSpacer();
        StringBuilder sb = new StringBuilder();
        cu.prettyPrint(spacer, sb, 0);

        return sb.toString();
    }

    private static String readFile(String filename) {
        String everything = "";
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            everything = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return everything;
    }

    private static CompilationUnit parse(String input, String ending) {
        WurstGui gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, new RunArgs("-prettyPrint"));
        return compiler.parse("format" + ending, new StringReader(input));
    }
}
