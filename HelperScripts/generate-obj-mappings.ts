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
const KB_OUT_FILE = "./HelperScripts/wc3-knowledge-base.json";
const GAMEDATA_DIR = "./HelperScripts/gamedata";

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

// ===========================================================================
// KNOWLEDGE BASE GENERATION
// Reads all gamedata SLK/txt files and produces wc3-knowledge-base.json —
// a self-contained description of all WC3 object types, their field schemas
// (display names, types, categories) and all base-game object records.
// ===========================================================================

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function tryRead(path: string): string | null {
  try { return Deno.readTextFileSync(path); } catch (_) { return null; }
}

function gdPath(file: string): string {
  return `${GAMEDATA_DIR}/${file}`;
}

// ---------------------------------------------------------------------------
// WorldEditStrings.txt  →  Map<"WESTRING_FOO", "Foo">
// ---------------------------------------------------------------------------

function parseWEStrings(content: string): Map<string, string> {
  const map = new Map<string, string>();
  for (const rawLine of content.split("\n")) {
    const line = rawLine.replace(/\r$/, "").trim();
    if (!line || line.startsWith("[") || line.startsWith(";") || line.startsWith("//")) continue;
    const eq = line.indexOf("=");
    if (eq < 1) continue;
    const key = line.slice(0, eq).trim();
    let val = line.slice(eq + 1).trim();
    if (val.startsWith('"') && val.endsWith('"')) val = val.slice(1, -1);
    map.set(key, val);
  }
  return map;
}

// ---------------------------------------------------------------------------
// Generic SLK parser
// Parses the SYLK-subset format used by WC3 game data files.
// Returns a flat list of row-objects keyed by the column names from row Y=1.
// ---------------------------------------------------------------------------

interface SLKRow extends Record<string, string | number | null> {}

function parseSLKRows(content: string): { idColName: string; rows: SLKRow[] } {
  const grid: Record<number, Record<number, string | number | null>> = {};
  let curY: number | null = null;

  for (const rawLine of content.split("\n")) {
    const line = rawLine.replace(/\r$/, "");
    if (!line.startsWith("C;")) continue;

    const parts = line.slice(2).split(";");
    let x: number | null = null;
    let y: number | null = null;
    let k: string | number | null = null;

    for (const p of parts) {
      if (p.startsWith("X")) {
        x = parseInt(p.slice(1));
      } else if (p.startsWith("Y")) {
        y = parseInt(p.slice(1));
      } else if (p.startsWith("K")) {
        const raw = p.slice(1);
        if (!raw || raw === "-" || raw === "_" || raw === " - ") {
          k = null;
        } else if (raw.startsWith('"')) {
          k = raw.endsWith('"') ? raw.slice(1, -1) : raw.slice(1);
        } else {
          const n = parseFloat(raw);
          k = isNaN(n) ? raw : n;
        }
      }
    }

    if (y !== null) curY = y;
    if (curY === null || x === null) continue;
    if (!grid[curY]) grid[curY] = {};
    grid[curY][x] = k;
  }

  // Y=1 is the header row; X=1 is always the ID column.
  const headerRow = grid[1] ?? {};
  const columns: Record<number, string> = {};
  for (const [xStr, val] of Object.entries(headerRow)) {
    if (typeof val === "string") columns[parseInt(xStr)] = val;
  }
  const idColName = columns[1] ?? "id";

  const rows: SLKRow[] = [];
  for (const y of Object.keys(grid).map(Number).sort((a, b) => a - b)) {
    if (y === 1) continue;
    const gridRow = grid[y];
    const idVal = gridRow[1];
    if (idVal === null || idVal === undefined) continue;

    const obj: SLKRow = {};
    for (const [xStr, val] of Object.entries(gridRow)) {
      const colName = columns[parseInt(xStr)];
      if (colName && val !== null && val !== undefined) obj[colName] = val;
    }
    if (Object.keys(obj).length > 0) rows.push(obj);
  }

  return { idColName, rows };
}

/** Index SLK rows by their ID column (X=1). */
function slkToMap(content: string): Map<string, SLKRow> {
  const { idColName, rows } = parseSLKRows(content);
  const map = new Map<string, SLKRow>();
  for (const row of rows) {
    const id = row[idColName];
    if (typeof id === "string" && id) map.set(id, row);
  }
  return map;
}

// ---------------------------------------------------------------------------
// INI-style func.txt / skin.txt parser
// Sections like [UnitId] followed by Key=Value lines.
// ---------------------------------------------------------------------------

function parseFuncTxt(content: string): Map<string, Record<string, string>> {
  const result = new Map<string, Record<string, string>>();
  let current: Record<string, string> | null = null;

  for (const rawLine of content.split("\n")) {
    const line = rawLine.replace(/\r$/, "");
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("//")) continue;

    if (trimmed.startsWith("[") && trimmed.includes("]")) {
      const id = trimmed.slice(1, trimmed.indexOf("]")).trim();
      if (!result.has(id)) result.set(id, {});
      current = result.get(id)!;
    } else if (current !== null) {
      const eq = line.indexOf("=");
      if (eq >= 1) {
        const key = line.slice(0, eq).trim();
        const val = line.slice(eq + 1).trim();
        if (key) current[key] = val;
      }
    }
  }
  return result;
}

/** Merge multiple func maps; first writer per key wins. */
function mergeFuncMaps(
  ...maps: Map<string, Record<string, string>>[]
): Map<string, Record<string, string>> {
  const result = new Map<string, Record<string, string>>();
  for (const m of maps) {
    for (const [id, fields] of m) {
      if (!result.has(id)) result.set(id, {});
      const target = result.get(id)!;
      for (const [k, v] of Object.entries(fields)) {
        if (!(k in target)) target[k] = v;
      }
    }
  }
  return result;
}

// ---------------------------------------------------------------------------
// Field schema — derived from *metadata.slk files
// ---------------------------------------------------------------------------

interface KBFieldSchema {
  /** 4-char field ID used in war3map binary formats, e.g. "unam" */
  id: string;
  /** Column name inside the source SLK, or key in Profile (func.txt) */
  field: string;
  /** Source SLK tag, e.g. "unitData", "unitUI", "Profile", "ItemData" */
  slk: string;
  /** Level index (-1 = not leveled; ≥ 0 = explicit level base) */
  index: number;
  /** 1 = value repeats per level (ability leveled fields) */
  repeat: number;
  /** Ability data slot pointer */
  data: number;
  /** UI category key, e.g. "stats", "combat", "text" */
  category: string;
  /** Human-readable display name (resolved from WorldEditStrings) */
  displayName: string;
  /** Editor sort key */
  sort: string;
  /** Data type: "string", "int", "real", "bool", "abilityList", etc. */
  type: string;
  minVal: string | null;
  maxVal: string | null;
  useHero: boolean;
  useUnit: boolean;
  useBuilding: boolean;
  useItem: boolean;
  useCreep: boolean;
  section: string | null;
}

function buildSchemas(
  metadataContent: string,
  westrings: Map<string, string>
): KBFieldSchema[] {
  const { rows } = parseSLKRows(metadataContent);
  const schemas: KBFieldSchema[] = [];

  function resolveStr(v: string | number | null | undefined): string {
    if (v === null || v === undefined) return "";
    const s = String(v);
    if (s.startsWith("WESTRING_")) return westrings.get(s) ?? s;
    return s;
  }
  function toBool(v: string | number | null | undefined): boolean {
    return v === 1 || v === "1";
  }
  function toNum(v: string | number | null | undefined): number {
    if (v === null || v === undefined) return 0;
    const n = typeof v === "number" ? v : parseFloat(String(v));
    return isNaN(n) ? 0 : n;
  }
  function toNullStr(v: string | number | null | undefined): string | null {
    return (v !== null && v !== undefined) ? String(v) : null;
  }

  for (const row of rows) {
    const id = row["ID"] as string;
    if (!id) continue;
    schemas.push({
      id,
      field: resolveStr(row["field"]),
      slk: resolveStr(row["slk"]),
      index: toNum(row["index"]),
      repeat: toNum(row["repeat"]),
      data: toNum(row["data"]),
      category: resolveStr(row["category"]),
      displayName: resolveStr(row["displayName"]),
      sort: resolveStr(row["sort"]),
      type: resolveStr(row["type"]) || "string",
      minVal: toNullStr(row["minVal"]),
      maxVal: toNullStr(row["maxVal"]),
      useHero: toBool(row["useHero"]),
      useUnit: toBool(row["useUnit"]),
      useBuilding: toBool(row["useBuilding"]),
      useItem: toBool(row["useItem"]),
      useCreep: toBool(row["useCreep"]),
      section: toNullStr(row["section"]),
    });
  }
  return schemas;
}

// ---------------------------------------------------------------------------
// Object records — merge SLK maps + func/skin map for each object type
// ---------------------------------------------------------------------------

type ObjRecord = Record<string, string | number>;

function buildObjectMap(
  slkMaps: Map<string, SLKRow>[],
  funcMap: Map<string, Record<string, string>>
): Record<string, ObjRecord> {
  // Collect all IDs from every source
  const allIds = new Set<string>();
  for (const m of slkMaps) for (const id of m.keys()) allIds.add(id);
  for (const id of funcMap.keys()) allIds.add(id);

  const result: Record<string, ObjRecord> = {};
  for (const id of allIds) {
    const obj: ObjRecord = {};

    // SLK sources first (order = priority)
    for (const m of slkMaps) {
      const row = m.get(id);
      if (!row) continue;
      for (const [k, v] of Object.entries(row)) {
        if (!(k in obj) && v !== null && v !== undefined) {
          obj[k] = v as string | number;
        }
      }
    }

    // func.txt / skin.txt fields
    const funcRow = funcMap.get(id);
    if (funcRow) {
      for (const [k, v] of Object.entries(funcRow)) {
        if (!(k in obj) && v !== "") obj[k] = v;
      }
    }

    result[id] = obj;
  }
  return result;
}

// ---------------------------------------------------------------------------
// Top-level knowledge base builder
// ---------------------------------------------------------------------------

function generateKnowledgeBase(westrings: Map<string, string>): object {
  function loadSLK(file: string): Map<string, SLKRow> {
    const c = tryRead(gdPath(file));
    return c ? slkToMap(c) : new Map();
  }
  function loadFunc(...files: string[]): Map<string, Record<string, string>> {
    const maps: Map<string, Record<string, string>>[] = [];
    for (const f of files) {
      const c = tryRead(gdPath(f));
      if (c) maps.push(parseFuncTxt(c));
    }
    return mergeFuncMaps(...maps);
  }
  function loadSchema(file: string): KBFieldSchema[] {
    const c = tryRead(gdPath(file));
    return c ? buildSchemas(c, westrings) : [];
  }

  // --- Field schemas from metadata files ---
  const unitMeta = loadSchema("unitmetadata.slk");
  const abilityMeta = loadSchema("abilitymetadata.slk");
  const buffMeta = loadSchema("abilitybuffmetadata.slk");
  const destructableMeta = loadSchema("destructablemetadata.slk");
  const upgradeMeta = [
    ...loadSchema("upgrademetadata.slk"),
    ...loadSchema("upgradeeffectmetadata.slk"),
  ];

  // --- Object data: merge SLK + func/skin sources per object type ---

  // Units — five SLK files, all keyed by the same 4-char unit ID
  const unitFunc = loadFunc(
    "humanunitfunc.txt", "orcunitfunc.txt", "nightelfunitfunc.txt",
    "undeadunitfunc.txt", "neutralunitfunc.txt", "campaignunitfunc.txt",
    "unitskin.txt", "unitweaponsskin.txt", "unitaddons.txt",
  );
  const unitObjects = buildObjectMap(
    [
      loadSLK("unitdata.slk"),
      loadSLK("unitui.slk"),
      loadSLK("unitbalance.slk"),
      loadSLK("unitweapons.slk"),
      loadSLK("unitabilities.slk"),
    ],
    unitFunc,
  );

  // Abilities
  const abilityFunc = loadFunc(
    "humanabilityfunc.txt", "orcabilityfunc.txt", "nightelfabilityfunc.txt",
    "undeadabilityfunc.txt", "neutralabilityfunc.txt", "campaignabilityfunc.txt",
    "commonabilityfunc.txt", "itemabilityfunc.txt", "commandfunc.txt",
    "abilityskin.txt",
  );
  const abilityObjects = buildObjectMap([loadSLK("abilitydata.slk")], abilityFunc);

  // Buffs/effects — abilitybuffdata.slk; buff IDs may also appear in ability func files
  const buffObjects = buildObjectMap([loadSLK("abilitybuffdata.slk")], abilityFunc);

  // Items
  const itemFunc = loadFunc(
    "itemfunc.txt", "itemskin.txt", "itemaddons.txt",
  );
  const itemObjects = buildObjectMap([loadSLK("itemdata.slk")], itemFunc);

  // Destructables
  const destructableFunc = loadFunc(
    "destructableskin.txt", "destructableaddons.txt",
  );
  const destructableObjects = buildObjectMap(
    [loadSLK("destructabledata.slk")], destructableFunc,
  );

  // Upgrades
  const upgradeFunc = loadFunc(
    "humanupgradefunc.txt", "orcupgradefunc.txt", "nightelfupgradefunc.txt",
    "undeadupgradefunc.txt", "neutralupgradefunc.txt", "campaignupgradefunc.txt",
    "upgradeskin.txt",
  );
  const upgradeObjects = buildObjectMap([loadSLK("upgradedata.slk")], upgradeFunc);

  // unitmetadata.slk covers units, heroes, buildings AND items; split by use-flags.
  return {
    /**
     * Field schemas per object type.
     * Each entry describes one editable field: its 4-char ID, human-readable
     * display name (resolved from WorldEditStrings), data type, UI category,
     * valid range, and which object sub-types use it.
     */
    fieldSchemas: {
      unit: unitMeta.filter((s) => s.useUnit),
      hero: unitMeta.filter((s) => s.useHero),
      building: unitMeta.filter((s) => s.useBuilding),
      item: unitMeta.filter((s) => s.useItem),
      ability: abilityMeta,
      buff: buffMeta,
      destructable: destructableMeta,
      upgrade: upgradeMeta,
    },
    /**
     * All base-game objects keyed by their 4-char ID.
     * Each record is a flat map of field-name → value, merging every SLK
     * and Profile (func.txt/skin.txt) source for that object type.
     */
    objects: {
      unit: unitObjects,
      ability: abilityObjects,
      buff: buffObjects,
      item: itemObjects,
      destructable: destructableObjects,
      upgrade: upgradeObjects,
    },
  };
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

// ---------------------------------------------------------------------------
// Knowledge base generation (reads from HelperScripts/gamedata/)
// ---------------------------------------------------------------------------

const westringsContent = tryRead(gdPath("WorldEditStrings.txt"));
if (!westringsContent) {
  console.log("\nHelperScripts/gamedata/ not found — skipping knowledge base generation.");
  console.log("Place WC3 game data files in HelperScripts/gamedata/ to enable this output.");
} else {
  console.log("\n--- Generating wc3-knowledge-base.json ---");
  const westrings = parseWEStrings(westringsContent);
  console.log(`Parsed ${westrings.size} WorldEditStrings entries`);

  const kb = generateKnowledgeBase(westrings);
  const kbJson = JSON.stringify(kb, null, 2);
  Deno.writeTextFileSync(KB_OUT_FILE, kbJson);
  console.log(`Generated: ${KB_OUT_FILE}`);

  // Summary
  const kbParsed = kb as { fieldSchemas: Record<string, unknown[]>; objects: Record<string, Record<string, unknown>> };
  for (const [type, schemas] of Object.entries(kbParsed.fieldSchemas)) {
    const count = (schemas as unknown[]).length;
    const objCount = Object.keys(kbParsed.objects[type] ?? {}).length;
    if (count > 0 || objCount > 0)
      console.log(`  ${type}: ${count} field schemas, ${objCount} base objects`);
  }
}
