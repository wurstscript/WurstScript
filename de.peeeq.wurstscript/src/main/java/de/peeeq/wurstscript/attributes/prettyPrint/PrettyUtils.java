package de.peeeq.wurstscript.attributes.prettyPrint;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.WurstModel;
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
    public static void pretty(List<String> args) throws IOException {
        if (args.size() == 0) {
            return;
        }
        String arg = args.get(0);
        if (args.equals("...")) {
            prettyAll(".");
        }
        if (arg.equals("tree") && args.size() >= 2) {
            debug(args.get(1));
            return;
        }

        String clean = pretty(new File(arg));
        System.out.println(clean);
    }

    public static String pretty(String source, String ending) {
        WurstGui gui = new WurstGuiCliImpl();
        CompilationUnit cu = parse(source, ending, gui);
        
        // Check for compilation errors before pretty printing
        if (gui.getErrorCount() > 0) {
            throw new RuntimeException("Cannot format code with compilation errors:\n" + gui.getErrors());
        }

        Spacer spacer = new MaxOneSpacer();
        StringBuilder sb = new StringBuilder();
        cu.prettyPrint(spacer, sb, 0);

        return sb.toString();
    }

    private static void prettyPrint(String filename) {
        try {
            String clean = pretty(filename, filename.substring(filename.lastIndexOf(".")));
            System.out.println(clean);
        } catch (RuntimeException e) {
            System.err.println("Error formatting " + filename + ": " + e.getMessage());
            throw e;
        }
    }

    public static void pretty(CompilationUnit cu) {
        pretty(cu, new WurstGuiCliImpl());
    }
    
    public static void pretty(CompilationUnit cu, WurstGui gui) {
        // Check for compilation errors before pretty printing
        if (gui.getErrorCount() > 0) {
            throw new RuntimeException("Cannot format code with compilation errors:\n" + gui.getErrors());
        }
        
        Spacer spacer = new MaxOneSpacer();
        StringBuilder sb = new StringBuilder();
        cu.prettyPrint(spacer, sb, 0);

        System.out.println(sb);
    }

    private static void debug(String filename) {
        String contents = readFile(filename);
        WurstGui gui = new WurstGuiCliImpl();
        CompilationUnit cu = parse(contents, filename.substring(filename.lastIndexOf(".")), gui);
        
        // Check for compilation errors before debugging
        if (gui.getErrorCount() > 0) {
            throw new RuntimeException("Cannot debug code with compilation errors:\n" + gui.getErrors());
        }

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
        WurstGui gui = new WurstGuiCliImpl();
        CompilationUnit cu = parse(contents, f.getName().substring(f.getName().lastIndexOf(".")), gui);
        
        // Check for compilation errors before pretty printing
        if (gui.getErrorCount() > 0) {
            throw new RuntimeException("Cannot format code with compilation errors:\n" + gui.getErrors());
        }

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
        return parse(input, ending, gui);
    }
    
    private static CompilationUnit parse(String input, String ending, WurstGui gui) {
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, new RunArgs("-prettyPrint"));
        CompilationUnit cu = compiler.parse("format" + ending, new StringReader(input));
        
        // Create a minimal model to run validation
        WurstModel model = Ast.WurstModel();
        model.add(cu);
        
        // Run validation to detect compilation errors
        try {
            compiler.checkProg(model);
        } catch (Exception e) {
            // Errors are already added to gui during validation
        }
        
        return cu;
    }
}
