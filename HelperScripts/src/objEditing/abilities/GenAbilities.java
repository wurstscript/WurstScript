package objEditing.abilities;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import net.moonlightflower.wc3libs.misc.FieldId;
import net.moonlightflower.wc3libs.misc.ObjId;
import net.moonlightflower.wc3libs.slk.MetaSLK;
import net.moonlightflower.wc3libs.slk.app.meta.AbilityMetaSLK;
import net.moonlightflower.wc3libs.slk.app.objs.AbilSLK;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.WEStrings;

public class GenAbilities {
    static WEStrings strings = new WEStrings().parseFile(new File("./WorldEditStrings.txt"));
    static StringBuilder sb = new StringBuilder();

    static void println(String s) {
        sb.append(s);
        sb.append("\n");
    }

    static void print(String s) {
        sb.append(s);
    }

    static class FieldData {
        String id;
        String displayName;
        String type;
        int data;
        boolean useLevels;

        public FieldData(String id, String displayName, String type, int data, boolean useLevels) {
            this.id = id;
            this.displayName = displayName;
            this.type = type;
            this.data = data;
            this.useLevels = useLevels;
        }

        public void printFunc(Set<String> usedFuncs) {
            println("");
            String funcName = camelize(displayName);
            int i = 0;
            if (usedFuncs.contains(funcName)) {
                do {
                    i++;
                } while (usedFuncs.contains(funcName + i));
                funcName = funcName + i;
            }
            usedFuncs.add(funcName);

            print("\tfunction set" + funcName + "(");
            if (useLevels) {
                print("int level, ");
            }
            print(type() + " value)");
            println("");
            print("\t\tdef.setLvlData");
            print(typePost());
            print("(\"" + id + "\", ");
            if (useLevels) {
                print("level, " + data + ", ");
            } else {
                print("0, " + data + ", ");
            }
            println("value)");
        }

        private String type() {
            switch (type) {
                case "string":  return "string";
                case "bool":    return "bool";
                case "int":     return "int";
                case "real":    return "real";
                case "unreal":  return "real";
                default:        return "string";
            }
        }

        private String typePost() {
            switch (type) {
                case "string":  return "String";
                case "bool":    return "Boolean";
                case "int":     return "Int";
                case "real":    return "Real";
                case "unreal":  return "Unreal";
                default:        return "String";
            }
        }
    }

    /** Null-safe field getter for SLK objects — returns null instead of throwing when the field is absent. */
    private static String safeGet(net.moonlightflower.wc3libs.slk.SLK.Obj<?> obj, String field) {
        net.moonlightflower.wc3libs.dataTypes.DataType val = obj.get(FieldId.valueOf(field));
        return val == null ? null : val.toString();
    }

    public static void main(String[] args) throws IOException {
        // Load ability names and parent codes from abilitydata.slk
        Map<String, String> abilityNames = new HashMap<>();
        Map<String, String> abilityParent = new HashMap<>(); // alias -> code (base ability)
        loadAbilityData(new File("./abilitydata.slk"), abilityNames, abilityParent);
        System.err.println("Loaded " + abilityNames.size() + " ability names from abilitydata.slk");

        List<FieldData> commonData = Lists.newArrayList();
        Multimap<String, FieldData> specificData = HashMultimap.create();

        // Parse abilitymetadata.slk via wc3libs
        AbilityMetaSLK metaSlk = new AbilityMetaSLK();
        metaSlk.read(new File("./abilitymetadata.slk"));

        for (MetaSLK.Obj metaObj : metaSlk.getObjs().values()) {
            // The SLK row ID is the object key, not a regular field
            String id = metaObj.getId().toString();
            if (id == null || id.isEmpty()) continue;

            String displayNameKey = safeGet(metaObj, "displayName");
            String displayName = strings.get(displayNameKey);
            if (displayName == null || displayName.startsWith("WESTRING_")) {
                displayName = safeGet(metaObj, "field");
            }
            if (displayName == null || displayName.isEmpty()) {
                displayName = id;
            }

            String type = safeGet(metaObj, "type");
            if (type == null) type = "string";

            String repeatStr = safeGet(metaObj, "repeat");
            boolean useLevels = repeatStr != null && !repeatStr.equals("0");

            String dataStr = safeGet(metaObj, "data");
            int data = 0;
            try {
                if (dataStr != null) data = Integer.parseInt(dataStr);
            } catch (NumberFormatException ignored) {}

            String useSpecific = safeGet(metaObj, "useSpecific");

            FieldData fd = new FieldData(id, displayName, type, data, useLevels);

            if (useSpecific == null || useSpecific.isEmpty()) {
                commonData.add(fd);
            } else {
                for (String spell : useSpecific.split("[,\\.]+")) {
                    spell = spell.trim();
                    if (!spell.isEmpty()) {
                        specificData.put(spell, fd);
                    }
                }
            }
        }

        System.err.println("Common fields: " + commonData.size());
        System.err.println("Specific ability groups: " + specificData.keySet().size());

        // Propagate specific fields to child abilities via inheritance (alias -> code parent).
        // e.g. ACpa (Parasite Eredar) has code=ANpa, so inherits ANpa's specific fields.
        // Also handles partial inheritance: Afbt has fbk5 own + inherits fbk1-4 from Afbk.
        int inherited = 0;
        for (String alias : abilityNames.keySet()) {
            String parent = abilityParent.get(alias);
            if (parent == null || parent.equals(alias) || !specificData.containsKey(parent)) continue;
            // Add parent fields that the child doesn't already define (by field id)
            Set<String> ownFieldIds = new java.util.HashSet<>();
            for (FieldData fd : specificData.get(alias)) ownFieldIds.add(fd.id);
            boolean added = false;
            for (FieldData fd : specificData.get(parent)) {
                if (!ownFieldIds.contains(fd.id)) {
                    specificData.put(alias, fd);
                    added = true;
                }
            }
            if (added) inherited++;
        }
        System.err.println("Inherited specific fields for: " + inherited + " abilities");

        println("package AbilityObjEditing");
        println("import public ObjEditingNatives");
        println("");
        println("public class AbilityDefinition");
        println("\tprotected ObjectDefinition def");
        println("\t");
        println("\tconstruct(int newAbilityId, int origAbilityId)");
        println("\t\tdef = createObjectDefinition(\"w3a\", newAbilityId, origAbilityId)");

        Set<String> usedNames = Sets.newHashSet();
        for (FieldData fd : commonData) {
            fd.printFunc(usedNames);
        }


        // Build a constant-name map for ALL abilities (used so stdlib classes reference AbilityIds.xxx)
        // We need this before generating any class output.
        Map<String, String> spellToConstant = new HashMap<>(); // spell -> camelCase constant name
        {
            Set<String> usedConstantNames = new java.util.TreeSet<>();
            for (String spell : abilityNames.keySet()) {
                String spellName = abilityNames.get(spell);
                String constantName = toCamelCase(spellName);
                String base = constantName;
                int ci = 0;
                while (!usedConstantNames.add(constantName)) {
                    constantName = base + (++ci);
                }
                spellToConstant.put(spell, constantName);
            }
        }

        // Abilities with specific fields (including inherited ones) — output to main wurst file
        // and to the additions file (for stdlib integration), both using AbilityIds.xxx
        StringBuilder idsBlock = new StringBuilder();
        StringBuilder classesBlock = new StringBuilder();

        for (String spell : specificData.keySet()) {
            usedNames.clear();
            String spellName = abilityNames.getOrDefault(spell, spell);
            String constantName = spellToConstant.getOrDefault(spell, toCamelCase(spellName));

            // Main HelperScripts output (standalone, uses raw id)
            println("");
            println("");
            println("");
            println("public class AbilityDefinition" + spellName + " extends AbilityDefinition");
            println("\tconstruct(int newAbilityId)");
            println("\t\tsuper(newAbilityId, '" + spell + "')");
            for (FieldData fd : specificData.get(spell)) {
                fd.printFunc(usedNames);
            }

            // Additions file (for stdlib) uses AbilityIds reference
            idsBlock.append("\tstatic constant ").append(constantName)
                    .append("\t\t\t\t= '").append(spell).append("'\n");
            classesBlock.append("\n\n\npublic class AbilityDefinition").append(spellName)
                    .append(" extends AbilityDefinition\n");
            classesBlock.append("\tconstruct(int newAbilityId)\n");
            classesBlock.append("\t\tsuper(newAbilityId, AbilityIds.").append(constantName).append(")\n");
            // Copy field methods
            Set<String> addUsedNames = Sets.newHashSet();
            for (FieldData fd : specificData.get(spell)) {
                // Re-generate method into classesBlock directly
                String funcName = camelize(fd.displayName);
                int i2 = 0;
                while (!addUsedNames.add(funcName)) { i2++; funcName = camelize(fd.displayName) + i2; }
                classesBlock.append("\n\tfunction set").append(funcName).append("(");
                if (fd.useLevels) classesBlock.append("int level, ");
                classesBlock.append(fd.type()).append(" value)\n");
                classesBlock.append("\t\tdef.setLvlData").append(fd.typePost())
                        .append("(\"").append(fd.id).append("\", ");
                if (fd.useLevels) classesBlock.append("level, ").append(fd.data).append(", ");
                else classesBlock.append("0, ").append(fd.data).append(", ");
                classesBlock.append("value)\n");
            }
        }

        // Abilities with only common fields
        int commonOnly = 0;
        for (String spell : abilityNames.keySet()) {
            if (specificData.containsKey(spell)) continue;
            String spellName = abilityNames.get(spell);
            String constantName = spellToConstant.getOrDefault(spell, toCamelCase(spellName));

            idsBlock.append("\tstatic constant ").append(constantName)
                    .append("\t\t\t\t= '").append(spell).append("'\n");
            classesBlock.append("\n\n\npublic class AbilityDefinition").append(spellName)
                    .append(" extends AbilityDefinition\n");
            classesBlock.append("\tconstruct(int newAbilityId)\n");
            classesBlock.append("\t\tsuper(newAbilityId, AbilityIds.").append(constantName).append(")\n");

            // Main HelperScripts output (standalone, uses raw id)
            println("");
            println("");
            println("");
            println("public class AbilityDefinition" + spellName + " extends AbilityDefinition");
            println("\tconstruct(int newAbilityId)");
            println("\t\tsuper(newAbilityId, '" + spell + "')");
            commonOnly++;
        }
        System.err.println("Common-only ability classes generated: " + commonOnly);

        Files.write(idsBlock.toString().getBytes(Charsets.UTF_8),
                new File("./AbilityIds_additions.wurst"));
        Files.write(classesBlock.toString().getBytes(Charsets.UTF_8),
                new File("./AbilityObjEditing_additions.wurst"));
        System.err.println("Wrote AbilityIds_additions.wurst and AbilityObjEditing_additions.wurst");

        System.out.println(sb.toString());
        Files.write(sb, new File("./AbilityObjEditing.wurst"), Charsets.UTF_8);
    }

    /**
     * Parses abilitydata.slk and extracts:
     *  - names: alias -> sanitized PascalCase class-name suffix (from "comments" column)
     *  - parents: alias -> code (the base/parent ability ID, from "code" column)
     */
    private static void loadAbilityData(File slkFile,
            Map<String, String> names, Map<String, String> parents) throws IOException {
        AbilSLK abilSlk = new AbilSLK(slkFile);
        Set<String> usedNames = Sets.newHashSet();

        for (AbilSLK.Obj obj : abilSlk.getObjs().values()) {
            String alias = obj.getId().toString();
            if (alias == null || alias.isEmpty()) continue;

            String comment = safeGet(obj, "comments");
            String name;
            if (comment != null && !comment.isEmpty()) {
                name = sanitizeName(comment);
            } else {
                name = alias;
            }
            // Ensure uniqueness
            String base = name;
            int i = 0;
            while (!usedNames.add(name)) {
                name = base + (++i);
            }
            names.put(alias, name);

            // Track parent (code column)
            String code = safeGet(obj, "code");
            if (code != null && !code.isEmpty()) {
                parents.put(alias, code);
            }
        }
    }

    /**
     * Converts a raw WC3 comment string to PascalCase suitable for a class name suffix.
     * E.g. "Parasite(eredar)" -> "ParasiteEredar", "On Fire!" -> "OnFire"
     * Leading digit words are moved to the end: "200 Mana Bonus" -> "ManaBonusPlus200"
     */
    private static String sanitizeName(String raw) {
        raw = raw.replace("+", "Plus");
        String[] parts = raw.split("[^a-zA-Z0-9]+");
        StringBuilder leading = new StringBuilder();
        StringBuilder rest = new StringBuilder();
        boolean foundAlpha = false;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!foundAlpha && Character.isDigit(part.charAt(0))) {
                leading.append(part);
            } else {
                foundAlpha = true;
                rest.append(Character.toUpperCase(part.charAt(0)));
                rest.append(part.substring(1));
            }
        }
        return rest.append(leading).toString();
    }

    /** Converts a PascalCase class-name suffix to a camelCase AbilityIds constant name. */
    public static String toCamelCase(String pascalCase) {
        if (pascalCase.isEmpty()) return pascalCase;
        return Character.toLowerCase(pascalCase.charAt(0)) + pascalCase.substring(1);
    }

    public static String camelize(String displayName) {
        return displayName.replaceAll("[^a-zA-Z]", "");
    }
}
