package de.peeeq.wurstscript.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Generates the Asset package from mpq listfiles
 */
public class AssetsGenerator {
    private static File getFile(String name) {
        return new File(AssetsGenerator.class.getClassLoader().getResource(name).getFile());
    }

    public static void main(String[] args) {
        try {
            generate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    interface Checker {
        boolean run(String filename);
    }

    private static final Pattern namePattern = Pattern.compile("\\d\\w+");
    static HashSet<String> forbidden = new HashSet<>(Arrays.asList("step", "switch", "nothing", "lightning"));
    static HashSet<String> names = new HashSet<>();

    static class Entry implements Comparable<Entry> {
        Category category;
        Type type;
        String path;
        String name;

        public Entry(Category category, Type type, String path, String name) {
            this.category = category;
            this.type = type;
            this.path = path.replaceAll("\\\\", "\\\\\\\\");
            this.name = name.replaceAll("['&! ]", "").replaceAll("-", "_");
            this.name = this.name.substring(0, 1).toLowerCase() + this.name.substring(1);
            if (forbidden.contains(this.name)) {
                this.name += "0";
            }

            while (namePattern.matcher(this.name).matches()) {
                this.name = this.name.substring(1);
            }
            if (names.contains(this.name)) {
                this.name += "1";
            }
            names.add(this.name);
        }

        @Override
        public int compareTo(Entry o) {
            return name.compareTo(o.name);
        }

        public String print() {
            return "static constant " + name + " = \"" + path + "\"\n";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (category != entry.category) return false;
            if (type != entry.type) return false;
            if (!Objects.equals(path, entry.path)) return false;
            return Objects.equals(name, entry.name);
        }

        @Override
        public int hashCode() {
            int result = category != null ? category.hashCode() : 0;
            result = 31 * result + (type != null ? type.hashCode() : 0);
            result = 31 * result + (path != null ? path.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }

    enum Type {
        MODEL((String s) -> s.endsWith(".mdx") | s.endsWith(".mdl")),
        ICON((String s) -> s.endsWith(".blp") && s.contains("BTN")),
        TEXTURE((String s) -> s.endsWith(".blp") | s.endsWith(".dds")),
        SOUND((String s) -> s.endsWith(".mp3") | s.endsWith(".wav"));

        Checker checker;

        Type(Checker checker) {
            this.checker = checker;
        }
    }

    enum Category {
        UNIT((String l) -> l.startsWith("Units"), "Units"),
        BUILDING((String l) -> l.startsWith("buildings") || l.startsWith("Buildings"), "Buildings"),
        ABILITIES((String l) -> l.startsWith("Abilities"), "Abilities"),
        OBJECTS((String l) -> l.startsWith("Objects"), "Objects"),
        DOODAD((String l) -> l.startsWith("Doodads"), "Doodads"),
        SOUND((String l) -> l.startsWith("Sound") || l.startsWith("Music"), "Sounds"),
        UI((String l) -> l.startsWith("UI"), "UI"),
        TEXTURES((String l) -> l.startsWith("Textures") | l.startsWith("ReplaceableTextures"), "Textures"),
        ICONS((String l) -> l.startsWith("ReplaceableTextures"), "Icons");

        Checker checker;
        private final String name;

        Category(Checker checker, String name) {
            this.checker = checker;
            this.name = name;
        }

        public String toName() {
            return name;
        }
    }

    private static final TreeSet<Entry>[] entries = new TreeSet[Category.values().length];

    static {
        for (Category category : Category.values()) {
            entries[category.ordinal()] = new TreeSet<>();
        }
    }

    public static void generate() throws IOException {
        File rocfile = getFile("rocfile");
        File tftfile = getFile("tftfile");
        File localfile = getFile("localfile");
        File file = new File("output.txt");

        String input = new String(Files.readAllBytes(rocfile.toPath())) + new String(Files.readAllBytes(tftfile.toPath())) +
                new String(Files.readAllBytes(localfile.toPath()));


        Scanner scanner = new Scanner(input);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String filename = line.substring(line.lastIndexOf("\\") + 1);
            Type type = getType(filename);
            if (type == null) continue;
            Category category;
            switch (type) {
                case ICON:
                    category = Category.ICONS;
                    break;
                case SOUND:
                    category = Category.SOUND;
                    break;
                case TEXTURE:
                    category = Category.TEXTURES;
                    break;
                default:
                    category = getCategory(line);
            }
            Entry entry = new Entry(category, type, line, filename.substring(0, filename.indexOf(".")));
            if (category != null) {
                entries[category.ordinal()].add(entry);
            }

        }
        StringBuilder output = new StringBuilder("package Assets\nimport NoWurst\n\n");
        for (Category category : Category.values()) {
            output.append("public class ").append(category.toName()).append("\n");
            TreeSet<Entry> entries = AssetsGenerator.entries[category.ordinal()];
            for (Entry e : entries) {
                output.append("\t").append(e.print());
            }
            output.append("\n\n");
        }

        Files.write(file.toPath(), output.toString().getBytes(), StandardOpenOption.CREATE);
    }

    private static Type getType(String filename) {
        for (Type t : Type.values()) {
            if (t.checker.run(filename)) {
                return t;
            }
        }
        return null;
    }


    public static Category getCategory(String line) {
        for (Category c : Category.values()) {
            if (c.checker.run(line)) {
                return c;
            }
        }
        return null;
    }
}
