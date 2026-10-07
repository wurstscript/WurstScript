package objEditing.abilities;

import com.google.common.base.Charsets;
import com.google.common.collect.LinkedHashMultimap;
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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.WEStrings;

public class GenAbilities {
    static WEStrings strings;
    static StringBuilder sb = new StringBuilder();

    static void println(String s) {
        sb.append(s);
        sb.append("\n");
    }

    static void print(String s) {
        sb.append(s);
    }

    static class FieldData {
        // AbilityMetaSLK labels several integer-backed flag/enum fields as strings.
        // ObjectDefinition's typed setters and the game object format store these as ints.
        private static final Set<String> INTEGER_STORAGE_FIELDS = Sets.newHashSet(
                "Eme2", "Hca4", "Hsb3", "Ncl2", "Ncl3", "Nsi1", "Poa5", "Poi4", "Rej3", "Spo4", "irl3");

        String id;
        String displayName;
        String type;
        int data;
        boolean useLevels;

        public FieldData(String id, String displayName, String type, int data, boolean useLevels) {
            this.id = id;
            this.displayName = displayName.trim();
            this.type = INTEGER_STORAGE_FIELDS.contains(id) ? "int" : type;
            this.data = data;
            this.useLevels = useLevels;
        }

        public String printFunc(Set<String> usedFuncs, Set<String> inheritedFuncs, Set<String> inheritedPresets,
                boolean includePresetSupport) {
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

            print("\t");
            if (inheritedFuncs.contains(funcName)) {
                print("override ");
            }
            print("function set" + funcName + "(");
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
            if (useLevels && includePresetSupport) {
                print(presetFunction(funcName, inheritedPresets.contains(funcName)));
            }
            return funcName;
        }

        public String presetFunction(String funcName, boolean isOverride) {
            StringBuilder result = new StringBuilder();
            result.append("\n\t");
            if (isOverride) result.append("override ");
            result.append("function preset").append(funcName).append("(")
                    .append(levelClosureType()).append(" lc)\n");
            result.append("\t\tdef.setLevelsData").append(typePost())
                    .append("(\"").append(id).append("\", lvls, ").append(data).append(", lc)\n");
            result.append("\t\taddTooltipProperty(\"").append(escapeWurstString(displayName)).append("\", lc)\n");
            return result.toString();
        }

        private String levelClosureType() {
            switch (type) {
                case "bool":    return "BooleanLevelClosure";
                case "int":     return "IntLevelClosure";
                case "real":
                case "unreal": return "RealLevelClosure";
                default:        return "StringLevelClosure";
            }
        }

        private String escapeWurstString(String value) {
            return value.replace("\\", "\\\\").replace("\"", "\\\"");
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

    static String resolveDisplayName(String id, String displayName, String fallback) {
        if (displayName == null || displayName.startsWith("WESTRING_")) {
            displayName = fallback;
        }
        // The generic metadata label for Tau2 is wrong; World Editor names this field "Prefer Friendlies".
        if ("Tau2".equals(id)) {
            displayName = "Prefer Friendlies";
        }
        if (displayName == null || displayName.isEmpty()) {
            displayName = id;
        }
        return displayName;
    }

    public static void main(String[] args) throws IOException {
        sb.setLength(0);
        strings = new WEStrings().parseFile(new File("./gamedata/WorldEditStrings.txt"));
        // Load ability names and parent codes from abilitydata.slk
        Map<String, String> abilityNames = new HashMap<>();
        Map<String, String> abilityParent = new HashMap<>(); // alias -> code (base ability)
        loadAbilityData(new File("./gamedata/abilitydata.slk"), abilityNames, abilityParent);
        System.err.println("Loaded " + abilityNames.size() + " ability names from abilitydata.slk");

        List<FieldData> commonData = Lists.newArrayList();
        Multimap<String, FieldData> specificData = LinkedHashMultimap.create();

        // Parse abilitymetadata.slk via wc3libs
        AbilityMetaSLK metaSlk = new AbilityMetaSLK();
        metaSlk.read(new File("./gamedata/abilitymetadata.slk"));

        for (MetaSLK.Obj metaObj : metaSlk.getObjs().values()) {
            // The SLK row ID is the object key, not a regular field
            String id = metaObj.getId().toString();
            if (id == null || id.isEmpty()) continue;

            String displayNameKey = safeGet(metaObj, "displayName");
            String displayName = resolveDisplayName(id, strings.get(displayNameKey), safeGet(metaObj, "field"));

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

        Set<String> currentAbilityIds = new java.util.HashSet<>(abilityNames.keySet());
        currentAbilityIds.addAll(specificData.keySet());
        ExistingAbilityData existingAbilityData = readExistingAbilityData(currentAbilityIds);
        Set<String> basePresetFunctionNames = existingAbilityData.basePresetFunctionNames;

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
        println("import public AbilityIds");
        println("");
        println("public class AbilityDefinition");
        println("\tprotected ObjectDefinition def");
        println("\t");
        println("\tconstruct(int newAbilityId, int origAbilityId)");
        println("\t\tdef = createObjectDefinition(\"w3a\", newAbilityId, origAbilityId)");

        Set<String> usedNames = Sets.newHashSet();
        Set<String> commonPresetFunctionNames = Sets.newHashSet();
        for (FieldData fd : commonData) {
            String funcName = fd.printFunc(usedNames, Sets.newHashSet(), Sets.newHashSet(), false);
            if (fd.useLevels) commonPresetFunctionNames.add(funcName);
        }
        Set<String> commonFunctionNames = Sets.newHashSet(usedNames);


        // Build a constant-name map for ALL abilities (used so stdlib classes reference AbilityIds.xxx)
        // We need this before generating any class output.
        Map<String, String> spellToConstant = new HashMap<>(); // spell -> stable AbilityIds constant name
        Map<String, String> spellToClassName = new HashMap<>(); // spell -> collision-safe stdlib class name
        {
            Set<String> allSpells = new java.util.TreeSet<>(abilityNames.keySet());
            allSpells.addAll(specificData.keySet());
            for (String spell : allSpells) {
                String spellName = abilityNames.get(spell);
                if (spellName == null) spellName = spell;
                String constantName = resolveConstantName(toCamelCase(spellName), spell,
                        existingAbilityData.constantToId, existingAbilityData.constantNameById);
                String className = resolveClassName("AbilityDefinition" + spellName, spell,
                        existingAbilityData.classNameToId, existingAbilityData.classNameById);
                spellToConstant.put(spell, constantName);
                spellToClassName.put(spell, className);
            }
        }

        // Abilities with specific fields (including inherited ones) — output to main wurst file
        // and to the additions file (for stdlib integration), both using AbilityIds.xxx
        StringBuilder idsBlock = new StringBuilder();
        StringBuilder completeIds = new StringBuilder("package AbilityIds\nimport NoWurst\n\npublic class AbilityIds\n");
        Set<String> completeAddedIds = Sets.newHashSet();
        for (String spell : new java.util.TreeSet<>(spellToConstant.keySet())) {
            appendIdConstant(completeIds, completeAddedIds, java.util.Collections.emptyMap(),
                    spellToConstant.get(spell), spell);
        }
        StringBuilder classesBlock = new StringBuilder();
        Set<String> addedIds = Sets.newHashSet();

        for (String spell : specificData.keySet()) {
            usedNames.clear();
            String spellName = abilityNames.getOrDefault(spell, spell);
            String constantName = spellToConstant.getOrDefault(spell, toCamelCase(spellName));
            String className = spellToClassName.getOrDefault(spell, resolveClassName("AbilityDefinition" + spellName,
                    spell, existingAbilityData.classNameToId, existingAbilityData.classNameById));

            // Both outputs share the documented named-ID constructor.
            print(abilityClassHeader(className, constantName, spell));
            for (FieldData fd : specificData.get(spell)) {
                fd.printFunc(usedNames, commonFunctionNames, commonPresetFunctionNames, false);
            }

            // Additions file (for stdlib) uses AbilityIds reference
            appendIdConstant(idsBlock, addedIds, existingAbilityData.originalConstantToId, constantName, spell);
            classesBlock.append(abilityClassHeader(className, constantName, spell));
            // Copy field methods
            Set<String> addUsedNames = Sets.newHashSet();
            for (FieldData fd : specificData.get(spell)) {
                // Re-generate method into classesBlock directly
                String funcName = camelize(fd.displayName);
                int i2 = 0;
                while (!addUsedNames.add(funcName)) { i2++; funcName = camelize(fd.displayName) + i2; }
                classesBlock.append("\n\t");
                if (commonFunctionNames.contains(funcName)) classesBlock.append("override ");
                classesBlock.append("function set").append(funcName).append("(");
                if (fd.useLevels) classesBlock.append("int level, ");
                classesBlock.append(fd.type()).append(" value)\n");
                classesBlock.append("\t\tdef.setLvlData").append(fd.typePost())
                        .append("(\"").append(fd.id).append("\", ");
                if (fd.useLevels) classesBlock.append("level, ").append(fd.data).append(", ");
                else classesBlock.append("0, ").append(fd.data).append(", ");
                classesBlock.append("value)\n");
                if (fd.useLevels) {
                    classesBlock.append(fd.presetFunction(funcName, basePresetFunctionNames.contains(funcName)));
                }
            }
        }

        // Abilities with only common fields
        int commonOnly = 0;
        for (String spell : abilityNames.keySet()) {
            if (specificData.containsKey(spell)) continue;
            String spellName = abilityNames.get(spell);
            String constantName = spellToConstant.getOrDefault(spell, toCamelCase(spellName));
            String className = spellToClassName.getOrDefault(spell, "AbilityDefinition" + spellName);

            appendIdConstant(idsBlock, addedIds, existingAbilityData.originalConstantToId, constantName, spell);
            classesBlock.append(abilityClassHeader(className, constantName, spell));
            print(abilityClassHeader(className, constantName, spell));
            commonOnly++;
        }
        System.err.println("Common-only ability classes generated: " + commonOnly);

        Files.write(completeIds.toString().getBytes(Charsets.UTF_8), new File("./AbilityIds.wurst"));
        Files.write(idsBlock.toString().getBytes(Charsets.UTF_8),
                new File("./AbilityIds_additions.wurst"));
        Files.write(classesBlock.toString().getBytes(Charsets.UTF_8),
                new File("./AbilityObjEditing_additions.wurst"));
        System.err.println("Wrote AbilityIds.wurst, AbilityIds_additions.wurst and AbilityObjEditing_additions.wurst");

        System.out.println(sb.toString());
        Files.write(sb, new File("./AbilityObjEditing.wurst"), Charsets.UTF_8);
    }

    static String abilityClassHeader(String className, String constantName, String rawId) {
        return "\n\n\n/** '" + rawId + "' / AbilityIds." + constantName + " */\n"
                + "public class " + className + " extends AbilityDefinition\n"
                + "\tconstruct(int newAbilityId)\n"
                + "\t\tsuper(newAbilityId, AbilityIds." + constantName + ")\n";
    }

    private static class ExistingAbilityData {
        Map<String, String> constantToId = new HashMap<>();
        Map<String, String> originalConstantToId = new HashMap<>();
        Map<String, String> constantNameById = new HashMap<>();
        Map<String, String> classNameToId = new HashMap<>();
        Map<String, String> classNameById = new HashMap<>();
        Set<String> basePresetFunctionNames = Sets.newHashSet();
    }

    private static ExistingAbilityData readExistingAbilityData(Set<String> currentAbilityIds) throws IOException {
        File stdlibAbilityIds = new File("../../WurstStdlib2/wurst/_wurst/assets/AbilityIds.wurst");
        File stdlibAbilityEditing = new File("../../WurstStdlib2/wurst/objediting/AbilityObjEditing.wurst");
        if (!stdlibAbilityIds.isFile() || !stdlibAbilityEditing.isFile()) {
            throw new IOException("Ability generation requires the sibling WurstStdlib2 files: "
                    + stdlibAbilityIds.getPath() + " and " + stdlibAbilityEditing.getPath());
        }

        ExistingAbilityData result = new ExistingAbilityData();
        String idsSource = Files.toString(stdlibAbilityIds, Charsets.UTF_8);
        Matcher idMatcher = Pattern.compile("(?m)^\\s*static constant\\s+(\\w+)\\s*=\\s*'([^']{4})'")
                .matcher(idsSource);
        while (idMatcher.find()) {
            result.constantToId.put(idMatcher.group(1), idMatcher.group(2));
        }
        // Keep stable public names only for IDs that still exist in the current game data.
        // Older patch-only entries belong to their patch-specific stdlib branches.
        result.constantToId.entrySet().removeIf(entry -> !currentAbilityIds.contains(entry.getValue()));
        for (Map.Entry<String, String> entry : result.constantToId.entrySet()) {
            result.constantNameById.putIfAbsent(entry.getValue(), entry.getKey());
        }
        result.originalConstantToId.putAll(result.constantToId);

        String source = Files.toString(stdlibAbilityEditing, Charsets.UTF_8);
        Matcher classMatcher = Pattern.compile("(?ms)^public class (\\w+) extends AbilityDefinition\\b(.*?)(?=^public class |\\z)")
                .matcher(source);
        Pattern classIdPattern = Pattern.compile("(?m)^\\t\\tsuper\\(newAbilityId,\\s*AbilityIds\\.(\\w+)\\)");
        while (classMatcher.find()) {
            Matcher classIdMatcher = classIdPattern.matcher(classMatcher.group(2));
            if (classIdMatcher.find()) {
                String rawId = result.constantToId.get(classIdMatcher.group(1));
                if (rawId != null) {
                    result.classNameToId.put(classMatcher.group(1), rawId);
                    result.classNameById.putIfAbsent(rawId, classMatcher.group(1));
                }
            }
        }

        String baseMarker = "public class AbilityDefinition";
        int baseStart = source.indexOf(baseMarker);
        int nextClass = source.indexOf("\npublic class ", baseStart + baseMarker.length());
        if (baseStart < 0 || nextClass < 0) {
            throw new IOException("Could not find the AbilityDefinition base class in " + stdlibAbilityEditing.getPath());
        }
        String baseClass = source.substring(baseStart, nextClass);
        Matcher presetMatcher = Pattern.compile("(?m)^\\s*(?:override\\s+)?function preset(\\w+)\\s*\\(")
                .matcher(baseClass);
        while (presetMatcher.find()) result.basePresetFunctionNames.add(presetMatcher.group(1));
        return result;
    }

    private static String resolveConstantName(String preferred, String rawId, Map<String, String> knownIds,
            Map<String, String> namesById) {
        String existingName = namesById.get(rawId);
        if (existingName != null) return existingName;
        String candidate = preferred;
        String candidateId = knownIds.get(candidate);
        if (candidateId != null && !candidateId.equals(rawId)) {
            candidate = preferred + classIdSuffix(rawId);
            candidateId = knownIds.get(candidate);
        }
        int suffix = 2;
        while (candidateId != null && !candidateId.equals(rawId)) {
            candidate = preferred + classIdSuffix(rawId) + suffix++;
            candidateId = knownIds.get(candidate);
        }
        knownIds.putIfAbsent(candidate, rawId);
        namesById.putIfAbsent(rawId, candidate);
        return candidate;
    }

    private static String resolveClassName(String preferred, String rawId, Map<String, String> knownClasses,
            Map<String, String> namesById) {
        String existingName = namesById.get(rawId);
        if (existingName != null) return existingName;
        String candidate = preferred;
        String candidateId = knownClasses.get(candidate);
        if (candidateId != null && !candidateId.equals(rawId)) {
            candidate = preferred + classIdSuffix(rawId);
            candidateId = knownClasses.get(candidate);
        }
        int suffix = 2;
        while (candidateId != null && !candidateId.equals(rawId)) {
            candidate = preferred + classIdSuffix(rawId) + suffix++;
            candidateId = knownClasses.get(candidate);
        }
        knownClasses.putIfAbsent(candidate, rawId);
        namesById.putIfAbsent(rawId, candidate);
        return candidate;
    }

    private static String classIdSuffix(String rawId) {
        return rawId.substring(0, 1).toUpperCase(Locale.ROOT) + rawId.substring(1).toLowerCase(Locale.ROOT);
    }

    private static void appendIdConstant(StringBuilder output, Set<String> addedIds,
            Map<String, String> originalIds, String constantName, String rawId) {
        if (rawId.equals(originalIds.get(constantName)) || !addedIds.add(constantName)) return;
        output.append("\tstatic constant ").append(constantName)
                .append("\t\t\t\t= '").append(rawId).append("'\n");
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
