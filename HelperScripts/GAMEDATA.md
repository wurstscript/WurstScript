# Warcraft III game data generation

The checked-in game data resources are generated from the Warcraft III installation, not from an online mirror. Use the in-house sibling reader in `../casc-ts`; do not use CascView.

## Which tool does what

There is one pipeline. Its three tools are stages that produce different outputs, not alternatives for the same job, and none of them is deprecated.

| Stage | Tool | Reads | Writes |
|---|---|---|---|
| 1. Extract | `extract-wc3-gamedata.mjs` (Node, uses `../casc-ts`) | the Warcraft III install | the ignored `gamedata/` snapshot |
| 2. Ability wrappers | `src/objEditing/abilities/GenAbilities.java` (Gradle `run`) | `gamedata/`, the sibling WurstStdlib2 ability IDs | `AbilityIds.wurst`, `AbilityIds_additions.wurst`, `AbilityObjEditing.wurst`, `AbilityObjEditing_additions.wurst` in this directory (ignored; additions merged into WurstStdlib2 by hand) |
| 3. Compiler resources | `generate-obj-mappings.ts` (Deno) | `gamedata/` and the sibling WurstStdlib2 object-editing sources | `stdlib-obj-mappings.json` and `wc3-knowledge-base.json` in the compiler resources (checked in) |

Stage 3 parses the stdlib's `objediting/AbilityObjEditing.wurst`, not the stage 2 output. When the ability wrappers change, run stage 2, merge its output into WurstStdlib2, then run stage 3. Only stage 2 has tests; CI runs them with `./gradlew -p ../HelperScripts test`.

## Refreshing the inputs and compiler resources

From the WurstScript repository root:

```powershell
# Build the sibling reader once if ../casc-ts/dist/index.js is missing or stale.
Push-Location ..\casc-ts
npm run build
Pop-Location

# Extract the inputs from the installed game. An alternate install directory is optional.
node HelperScripts/extract-wc3-gamedata.mjs
# node HelperScripts/extract-wc3-gamedata.mjs 'D:/Games/Warcraft III'

# Regenerate both compiler JSON resources.
deno run --allow-read --allow-write HelperScripts/generate-obj-mappings.ts

# Regenerate the ability editing source and its stdlib additions.
.\de.peeeq.wurstscript\gradlew.bat -p HelperScripts run --console=plain
```

The extractor reads game object data from `war3.w3mod:units/` and enUS names from `war3.w3mod:_locales/enus.w3mod:ui/worldeditstrings.txt`. It writes the required SLK/TXT files to the ignored `HelperScripts/gamedata/` directory. `generate-obj-mappings.ts` consumes that snapshot to produce `stdlib-obj-mappings.json` and `wc3-knowledge-base.json` under the compiler resources directory.

The Gradle generator reads `HelperScripts/gamedata/abilitydata.slk`, `abilitymetadata.slk`, and `WorldEditStrings.txt`. It reads the sibling WurstStdlib2 ability IDs and `AbilityDefinition` base class to keep public names stable for IDs still present in the current game data, disambiguate current name collisions with the rawcode suffix, and mark preset overrides correctly. Old patch-only IDs and classes are not carried into the Reforged stdlib; their patch-specific branches retain them. `AbilityIds_additions.wurst` contains current game IDs missing from that stdlib snapshot. The standalone `AbilityObjEditing.wurst` output keeps direct setters only; preset methods go into `AbilityObjEditing_additions.wurst`, whose classes inherit the stdlib level-count and tooltip helpers. Merge classes by raw ability ID: update an existing wrapper when the ID already exists, add wrappers for new current IDs, and remove wrappers whose IDs are absent from the current generated data. Keep separate wrappers only when current game data defines distinct abilities; do not retain old-name duplicates for the same raw ID. Multi-level fields need both `setX(level, value)` and `presetX(LevelClosure)` methods; the preset writes all levels and adds the tooltip property. Some flag/enum fields are marked `string` in ability metadata but stored as integers; `GenAbilities.FieldData.INTEGER_STORAGE_FIELDS` is the authoritative override list for those fields. The object-mapping generator must parse `override function setX(...)` declarations as well as ordinary setters. Run the stdlib typecheck after that merge.

Keep the extractor's required input list aligned with the filenames consumed by `generate-obj-mappings.ts` and `GenAbilities.java`. The extractor rejects missing or ambiguous paths before writing files so a game archive layout change cannot silently produce a partial snapshot.

`AbilityIds.wurst` is the complete standalone name-to-rawcode mapping for the generated abilities. Both ability-editing outputs reference these `AbilityIds` constants instead of embedding rawcodes in constructors. Each generated wrapper has a documentation comment containing its WC3 four-character ID and the corresponding `AbilityIds` reference, making the mapping visible in completion, hover, and generated API documentation. `AbilityIds_additions.wurst` remains an integration fragment containing only constants missing from the sibling stdlib; it can be empty when that stdlib is already current. Specific fields retain metadata insertion order so repeated generation preserves method ordering and collision suffixes.

Run the generator regression suite with `.\de.peeeq.wurstscript\gradlew.bat -p HelperScripts test --console=plain`. Unit coverage runs without external data. The full generation test requires the extracted game-data snapshot and sibling stdlib; it skips visibly when either is absent. When available, it regenerates the outputs, checks every wrapper's documented named ID against the complete mapping, and checks repeated generation for identical output.

For stdlib integration, merge the additions rather than replacing the existing ID package with the standalone current-game mapping: other stdlib packages may still reference compatibility constants absent from the current game snapshot. The generated documentation becomes public after merging the wrappers into WurstStdlib2 and rebuilding its API documentation.

Generated setters get a compact World Editor label/field-ID comment only when numeric or symbolic notation is lost from the method name. Ordinary labels and spelled-out units already preserved in method names are not repeated. Level numbering and preset behavior are documented once on the base definition. Keep the package declaration first. The generator does not infer units, ranges, or runtime constraints from field names or storage types, and escapes comment terminators and line breaks in metadata labels.
