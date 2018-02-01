package de.peeeq.wurstscript.attributes.prettyPrint;

import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;

import org.eclipse.jdt.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.io.StringReader;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import org.apache.commons.lang.StringUtils;

import static de.peeeq.wurstscript.utils.Utils.printElement;


public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            return;
        }
        String arg = args[0];
        if (arg.equals("...")) {
            prettyWalk(".");
            return;
        } 
        if (arg.equals("tree")) {
            debug(args[1]);
            return;
        }

        pretty(arg);
    }

    private static void debug(String filename) {
        String contents = readFile(filename);
        CompilationUnit cu = parse(contents);

        walkTree(cu, 0);
    }

    private static void check(Element e, int indent) {
//        System.out.println(StringUtils.repeat("\t", indent) + e.getClass().getSimpleName());
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


    private static void prettyWalk(String rootPath) {
        File root = new File(rootPath);
        File[] list = root.listFiles();
        if (list == null) {
            return;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                prettyWalk(f.getAbsolutePath());
            }
            else {
                String path = f.getAbsolutePath();

                // Prettify our wurst files.
                if (extension(path).equals("wurst")) {
                    pretty(path);
                }
            }
        }
    }

    // Note: only works with single extension files, i.e it doesn't work with:
    // asdf.tar.gz
    private static String extension(String filename) {
        String extension = "";

        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i+1);
        }
        return extension;
    }

    private static void pretty(String filename) {
        String contents = readFile(filename);
        CompilationUnit cu = parse(contents);

        // Spacer spacer = new DefaultSpacer();
        Spacer spacer = new MaxOneSpacer();
        StringBuilder sb = new StringBuilder();
        cu.prettyPrint(spacer, sb, 0);

        System.out.println(sb.toString());
    }

    private static String readFile(String filename) {
        String everything = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
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

    private static CompilationUnit parse(String input) {
        WurstGui gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui, null, new RunArgs());
        return compiler.parse("test", new StringReader(input));
    }
}

