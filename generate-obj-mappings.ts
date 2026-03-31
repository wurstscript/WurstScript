#!/usr/bin/env deno run --allow-read --allow-write
/**
 * Generates StdlibObjectMappings.java from WurstStdlib2 object editing wurst files.
 * Run this script after stdlib changes to regenerate the mappings used by the compiler
 * when producing enriched object editing output.
 *
 * Usage: deno run --allow-read --allow-write generate-obj-mappings.ts
 */

// Prefer the sibling WurstStdlib2 repo if it exists; fall back to the temp copy used in tests.
function resolveStdlibBase(): string {
  const sibling = "../WurstStdlib2/wurst";
  try { Deno.statSync(sibling); return sibling; } catch (_) {}
  return "./de.peeeq.wurstscript/temp/WurstStdlib2/wurst";
}
const STDLIB_BASE = resolveStdlibBase();
const HELPER_ABILITY_FILE = "./HelperScripts/AbilityObjEditing.wurst";
const UNIT_BALANCE_SLK = "./HelperScripts/unitbalance.slk";
const OUT_FILE =
  "./de.peeeq.wurstscript/src/main/resources/stdlib-obj-mappings.json";

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

interface MethodMapping {
  methodName: string;
  fieldId: string;
  /** dataPtr from setLvlData*(fieldId, level, dataPtr, value) */
  dataPtr: number;
  /** "Int", "Unreal", "String", "Boolean", "Real" as found in the wurst method call */
  wurstCallType: string;
  /** true if method signature has (int level, ...) */
  hasLevel: boolean;
}

interface ClassDef {
  className: string;
  parentClass: string;
  /** AbilityIds constant name, e.g. "slow" from super(newAbilityId, AbilityIds.slow) */
  abilityIdsConstant?: string;
  /** Raw 4-char base ID when super() uses a literal, e.g. super(newAbilityId, 'ACpa') */
  rawBaseId?: string;
  methods: MethodMapping[];
}

// ---------------------------------------------------------------------------
// Parse unitbalance.slk to identify building and hero base IDs.
// Column 1 = unitBalanceID, col 53 = isbldg (1=building), col 49 = Primary (INT/STR/AGI = hero)
// ---------------------------------------------------------------------------

function parseUnitBalance(content: string): { buildingIds: Set<string>; heroIds: Set<string> } {
  const buildingIds = new Set<string>();
  const heroIds = new Set<string>();
  const rows: Record<number, Record<number, string>> = {};
  let curY: number | null = null;

  for (const line of content.split("\n")) {
    if (!line.startsWith("C;")) continue;
    const parts = line.replace(/\r/g, "").slice(2).split(";");
    let xVal: number | null = null;
    let yVal: number | null = null;
    let kRaw: string | null = null;
    for (const p of parts) {
      if (p.startsWith("X")) xVal = parseInt(p.slice(1));
      else if (p.startsWith("Y")) yVal = parseInt(p.slice(1));
      else if (p.startsWith("K")) kRaw = p.slice(1);
    }
    if (yVal !== null) { curY = yVal; if (!rows[curY]) rows[curY] = {}; }
    if (curY !== null && xVal !== null && kRaw !== null) {
      let val = kRaw;
      if (val.startsWith('"') && val.endsWith('"')) val = val.slice(1, -1);
      rows[curY][xVal] = val;
    }
  }

  for (const y of Object.keys(rows).map(Number).sort((a, b) => a - b)) {
    if (y === 1) continue; // header
    const row = rows[y];
    const id = row[1];
    if (!id) continue;
    if (row[53] === "1") buildingIds.add(id);
    // Heroes have a primary attribute (INT, STR, or AGI); non-heroes have "_" or "-"
    const primary = row[49];
    if (primary && primary !== "_" && primary !== "-" && primary !== " - ") heroIds.add(id);
  }

  return { buildingIds, heroIds };
}

// ---------------------------------------------------------------------------
// Parse AbilityIds.wurst
// Extracts: constantName -> rawId (4-char string)
// ---------------------------------------------------------------------------

function parseAbilityIds(content: string): Map<string, string> {
  const result = new Map<string, string>();
  for (const line of content.split("\n")) {
    const m = line.match(/static constant\s+(\w+)\s*=\s*'([^']{4})'/);
    if (m) {
      result.set(m[1], m[2]);
    }
  }
  return result;
}

// ---------------------------------------------------------------------------
// Parse a wurst ObjEditing file for class/method/field mappings.
//
// Handles the pattern:
//   public class AbilityDefinitionXxx extends AbilityDefinition
//     construct(int newAbilityId)
//       super(newAbilityId, AbilityIds.xxx)
//     function setXxx(int level, type value)
//       def.setLvlData[Type]("fieldId", level, dataPtr, value)
//     function setYyy(type value)    <- no level parameter
//       def.setLvlData[Type]("fieldId", 0, dataPtr, value)
//
// Also handles non-level setters (used in UnitObjEditing etc.):
//     function setZzz(type value)
//       def.set[Type]("fieldId", value)    <- no level at all
// ---------------------------------------------------------------------------

function parseObjEditingFile(content: string): ClassDef[] {
  const lines = content.split("\n");
  const classes: ClassDef[] = [];
  let currentClass: ClassDef | null = null;
  let currentMethodName: string | null = null;
  let currentMethodHasLevel = false;
  let insidePreset = false;

  for (const line of lines) {
    // ---- Class declaration (no leading whitespace) ----
    const classMatch = line.match(/^public class (\w+)(?:\s+extends\s+(\w+))?/);
    if (classMatch) {
      currentClass = {
        className: classMatch[1],
        parentClass: classMatch[2] ?? "",
        methods: [],
      };
      classes.push(currentClass);
      currentMethodName = null;
      insidePreset = false;
      continue;
    }

    if (!currentClass) continue;

    // ---- Constructor super call ----
    // super(newAbilityId, AbilityIds.xxx) or super(newAbilityId, 'XXXX')
    const superMatch = line.match(
      /^\t\tsuper\(\w+,\s*AbilityIds\.(\w+)\)/
    );
    if (superMatch && !currentClass.abilityIdsConstant) {
      currentClass.abilityIdsConstant = superMatch[1];
      continue;
    }
    const rawSuperMatch = line.match(/^\t\tsuper\(\w+,\s*'([^']{4})'\)/);
    if (rawSuperMatch && !currentClass.abilityIdsConstant && !currentClass.rawBaseId) {
      currentClass.rawBaseId = rawSuperMatch[1];
      continue;
    }

    // ---- Function declaration (tab-indented, direct class member) ----
    // Skip preset* methods (they're just convenience wrappers, not direct setters)
    const funcMatch = line.match(/^\tfunction ((\w+))\(/);
    if (funcMatch) {
      const fnName = funcMatch[1];
      if (fnName.startsWith("preset") || fnName.startsWith("get")) {
        currentMethodName = null;
        insidePreset = true;
      } else if (fnName.startsWith("set")) {
        insidePreset = false;
        currentMethodName = fnName;
        // Does the signature include "int level" as first parameter?
        currentMethodHasLevel = /\(int level[,)]/.test(line);
      } else {
        currentMethodName = null;
        insidePreset = true; // skip non-set/get functions too
      }
      continue;
    }

    if (insidePreset || !currentMethodName) continue;

    // ---- Function body: look for def.setLvlData*(...) or def.set*(...) ----

    // Pattern 1: def.setLvlData[Type]("fieldId", level_or_0, dataPtr, value)
    // Note: some lines have extra spaces, e.g. def.setLvlDataString("fnam",  level, 0,  value)
    // Field IDs are 3-4 chars (e.g. "Crs" is 3 chars)
    const lvlMatch = line.match(
      /^\t\tdef\.setLvlData(\w+)\("([^"]{3,5})",\s*\w+,\s*(\d+),/
    );
    if (lvlMatch) {
      const wurstCallType = lvlMatch[1]; // e.g. "Unreal", "Int", "String", "Boolean"
      const fieldId = lvlMatch[2];
      const dataPtr = parseInt(lvlMatch[3]);
      currentClass.methods.push({
        methodName: currentMethodName,
        fieldId,
        dataPtr,
        wurstCallType,
        hasLevel: currentMethodHasLevel,
      });
      currentMethodName = null; // one mapping per method
      continue;
    }

    // Pattern 2: def.set[Type]("fieldId", value)  — no level at all (units, items, buffs)
    const noLvlMatch = line.match(
      /^\t\tdef\.(set(?!Lvl|Levels)(?:String|Int|Unreal|Real|Boolean))\("([^"]{4})",/
    );
    if (noLvlMatch) {
      const callFn = noLvlMatch[1]; // e.g. "setString", "setInt"
      const fieldId = noLvlMatch[2];
      const wurstCallType = callFn.replace("set", ""); // "String", "Int", "Unreal", "Boolean"
      currentClass.methods.push({
        methodName: currentMethodName,
        fieldId,
        dataPtr: 0, // WC3 WE stores non-level fields as ExtendedMod with dataPtr=0
        wurstCallType,
        hasLevel: false,
      });
      currentMethodName = null;
      continue;
    }
  }

  return classes;
}

// ---------------------------------------------------------------------------
// Resolve all methods for a class including inherited ones
// ---------------------------------------------------------------------------

function resolveAllMethods(
  classMap: Map<string, ClassDef>,
  className: string,
  visited = new Set<string>()
): MethodMapping[] {
  if (visited.has(className)) return [];
  visited.add(className);

  const cls = classMap.get(className);
  if (!cls) return [];

  // Build own method keys (fieldId:dataPtr)
  const ownKeys = new Set(
    cls.methods.map((m) => `${m.fieldId}:${m.dataPtr}`)
  );

  const parentMethods = resolveAllMethods(classMap, cls.parentClass, visited);

  // Own methods take priority; fill in parent methods not already covered
  const result = [...cls.methods];
  for (const pm of parentMethods) {
    if (!ownKeys.has(`${pm.fieldId}:${pm.dataPtr}`)) {
      result.push(pm);
    }
  }
  return result;
}

// ---------------------------------------------------------------------------
// Generate JSON
//
// Uses a compact class-hierarchy format to avoid repeating inherited fields
// for every ability class. The Java loader resolves inheritance at startup.
//
// Schema:
// {
//   "abilityClassByBaseId": { "Aslo": "AbilityDefinitionSlow", ... },
//   "classParents": { "AbilityDefinitionSlow": "AbilityDefinition", ... },
//   "classOwnFields": {
//     "AbilityDefinition": { "acdn:0": ["setCooldown", true, false], ... },
//     "AbilityDefinitionSlow": { "Slo1:1": ["setMovementSpeedFactor", true, false] },
//     ...
//   },
//   "unitFieldMethods":  { "unam:-1": ["setName", false, false], ... },
//   "buffFieldMethods":  { ... },
//   "itemFieldMethods":  { ... }
// }
//
// Field entry is a 3-element array [methodName, hasLevel, isBool] to keep the
// file compact.
// ---------------------------------------------------------------------------

type FieldEntry = [string, boolean, boolean]; // [methodName, hasLevel, isBool]

function generateJson(
  abilityIdMap: Map<string, string>,
  abilityClasses: ClassDef[],
  unitClasses: ClassDef[],
  buffClasses: ClassDef[],
  itemClasses: ClassDef[],
  destructableClasses: ClassDef[],
  doodadClasses: ClassDef[],
  buildingIds: Set<string>,
  heroIds: Set<string>
): string {
  // ability base ID -> class name (first match wins)
  const abilityClassByBaseId: Record<string, string> = {};
  for (const cls of abilityClasses) {
    let rawId: string | undefined;
    if (cls.abilityIdsConstant) {
      rawId = abilityIdMap.get(cls.abilityIdsConstant);
      if (!rawId) {
        for (const [k, v] of abilityIdMap) {
          if (k.toLowerCase() === cls.abilityIdsConstant.toLowerCase()) { rawId = v; break; }
        }
      }
    } else if (cls.rawBaseId) {
      rawId = cls.rawBaseId;
    }
    if (rawId && !(rawId in abilityClassByBaseId)) {
      abilityClassByBaseId[rawId] = cls.className;
    }
  }

  // Collect all classes from all files, build parent map and own-fields map
  const allClasses = [...abilityClasses, ...unitClasses, ...buffClasses, ...itemClasses];
  const classParents: Record<string, string> = {};
  const classOwnFields: Record<string, Record<string, FieldEntry>> = {};

  for (const cls of allClasses) {
    if (cls.parentClass) classParents[cls.className] = cls.parentClass;
    if (cls.methods.length > 0) {
      const own: Record<string, FieldEntry> = {};
      for (const m of cls.methods) {
        const key = `${m.fieldId}:${m.dataPtr}`;
        if (!(key in own)) {
          own[key] = [m.methodName, m.hasLevel, m.wurstCallType === "Boolean"];
        }
      }
      // sort by key for deterministic output
      classOwnFields[cls.className] = Object.fromEntries(
        Object.entries(own).sort(([a], [b]) => a.localeCompare(b))
      );
    }
  }

  // Generic field maps for unit/buff/item (resolved with inheritance, stored flat
  // since these are small and only resolve one root class each)
  function resolveFlat(
    classMap: Map<string, ClassDef>,
    root: string
  ): Record<string, FieldEntry> {
    const all = resolveAllMethods(classMap, root);
    const result: Record<string, FieldEntry> = {};
    for (const m of all) {
      const key = `${m.fieldId}:${m.dataPtr}`;
      if (!(key in result)) {
        result[key] = [m.methodName, m.hasLevel, m.wurstCallType === "Boolean"];
      }
    }
    return Object.fromEntries(Object.entries(result).sort(([a], [b]) => a.localeCompare(b)));
  }

  const unitClassMap = new Map(unitClasses.map((c) => [c.className, c]));
  const buffClassMap = new Map(buffClasses.map((c) => [c.className, c]));
  const itemClassMap = new Map(itemClasses.map((c) => [c.className, c]));
  const destructableClassMap = new Map(destructableClasses.map((c) => [c.className, c]));
  const doodadClassMap = new Map(doodadClasses.map((c) => [c.className, c]));

  const unitRoot = unitClassMap.has("UnitDefinition") ? "UnitDefinition" : "UnitOrBuildingOrHeroDefinition";
  const buildingRoot = unitClassMap.has("BuildingDefinition") ? "BuildingDefinition" : unitRoot;
  const heroRoot = unitClassMap.has("HeroDefinition") ? "HeroDefinition" : unitRoot;

  const json = {
    abilityClassByBaseId: Object.fromEntries(
      Object.entries(abilityClassByBaseId).sort(([a], [b]) => a.localeCompare(b))
    ),
    classParents: Object.fromEntries(
      Object.entries(classParents).sort(([a], [b]) => a.localeCompare(b))
    ),
    classOwnFields: Object.fromEntries(
      Object.entries(classOwnFields).sort(([a], [b]) => a.localeCompare(b))
    ),
    buildingBaseIds: [...buildingIds].sort(),
    heroBaseIds: [...heroIds].sort(),
    unitFieldMethods: resolveFlat(unitClassMap, unitRoot),
    buildingFieldMethods: resolveFlat(unitClassMap, buildingRoot),
    heroFieldMethods: resolveFlat(unitClassMap, heroRoot),
    buffFieldMethods: resolveFlat(buffClassMap, "BuffDefinition"),
    itemFieldMethods: resolveFlat(itemClassMap, "ItemDefinition"),
    destructableFieldMethods: resolveFlat(destructableClassMap, "DestructableDefinition"),
    doodadFieldMethods: resolveFlat(doodadClassMap, "DoodadDefinition"),
  };

  return JSON.stringify(json, null, 2);
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------

const abilityIdsContent = Deno.readTextFileSync(
  `${STDLIB_BASE}/_wurst/assets/AbilityIds.wurst`
);
const abilityIdMap = parseAbilityIds(abilityIdsContent);
console.log(`Parsed ${abilityIdMap.size} AbilityIds constants`);

const abilityClasses = parseObjEditingFile(
  Deno.readTextFileSync(`${STDLIB_BASE}/objediting/AbilityObjEditing.wurst`)
);
console.log(`Parsed ${abilityClasses.length} ability class definitions from stdlib`);

// Supplement with HelperScripts generated file: add classes for base IDs not already
// covered by the stdlib (these are abilities with only common fields — no specific fields).
try {
  const helperClasses = parseObjEditingFile(Deno.readTextFileSync(HELPER_ABILITY_FILE));
  // Collect already-mapped base IDs from stdlib so we don't override them
  const stdlibBaseIds = new Set<string>();
  for (const cls of abilityClasses) {
    if (cls.rawBaseId) stdlibBaseIds.add(cls.rawBaseId);
    if (cls.abilityIdsConstant) {
      const r = abilityIdMap.get(cls.abilityIdsConstant);
      if (r) stdlibBaseIds.add(r);
    }
  }
  let supplemented = 0;
  for (const cls of helperClasses) {
    const rid = cls.rawBaseId ?? (cls.abilityIdsConstant ? abilityIdMap.get(cls.abilityIdsConstant) : undefined);
    if (rid && !stdlibBaseIds.has(rid)) {
      abilityClasses.push(cls);
      stdlibBaseIds.add(rid);
      supplemented++;
    }
  }
  console.log(`Supplemented ${supplemented} base IDs from HelperScripts`);
} catch (_) {
  console.log("HelperScripts/AbilityObjEditing.wurst not found, skipping supplement");
}

const unitClasses = parseObjEditingFile(
  Deno.readTextFileSync(`${STDLIB_BASE}/objediting/UnitObjEditing.wurst`)
);
console.log(`Parsed ${unitClasses.length} unit class definitions`);

const buffClasses = parseObjEditingFile(
  Deno.readTextFileSync(`${STDLIB_BASE}/objediting/BuffObjEditing.wurst`)
);
console.log(`Parsed ${buffClasses.length} buff class definitions`);

const itemClasses = parseObjEditingFile(
  Deno.readTextFileSync(`${STDLIB_BASE}/objediting/ItemObjEditing.wurst`)
);
console.log(`Parsed ${itemClasses.length} item class definitions`);

const destructableClasses = parseObjEditingFile(
  Deno.readTextFileSync(`${STDLIB_BASE}/objediting/DestructableObjEditing.wurst`)
);
console.log(`Parsed ${destructableClasses.length} destructable class definitions`);

const doodadClasses = parseObjEditingFile(
  Deno.readTextFileSync(`${STDLIB_BASE}/objediting/DoodadObjEditing.wurst`)
);
console.log(`Parsed ${doodadClasses.length} doodad class definitions`);

// Parse unitbalance.slk for building/hero base ID detection
let buildingIds = new Set<string>();
let heroIds = new Set<string>();
try {
  const unitBalanceContent = Deno.readTextFileSync(UNIT_BALANCE_SLK);
  const parsed = parseUnitBalance(unitBalanceContent);
  buildingIds = parsed.buildingIds;
  heroIds = parsed.heroIds;
  console.log(`Parsed ${buildingIds.size} building base IDs and ${heroIds.size} hero base IDs from unitbalance.slk`);
} catch (_) {
  console.log("HelperScripts/unitbalance.slk not found, skipping unit type detection");
}

const json = generateJson(
  abilityIdMap,
  abilityClasses,
  unitClasses,
  buffClasses,
  itemClasses,
  destructableClasses,
  doodadClasses,
  buildingIds,
  heroIds
);

Deno.writeTextFileSync(OUT_FILE, json);
console.log(`\nGenerated: ${OUT_FILE}`);

const parsed = JSON.parse(json);
const mappedCount = Object.keys(parsed.abilityClassByBaseId).length;
const totalClasses = Object.keys(parsed.classOwnFields).length;
console.log(`\nAbility base IDs mapped: ${mappedCount}`);
console.log(`Total classes with own fields: ${totalClasses}`);
