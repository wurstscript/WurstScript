# Warcraft III game data generation

The checked-in game data resources are generated from the Warcraft III installation, not from an online mirror. Use the in-house sibling reader in `../casc-ts`; do not use CascView.

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

The Gradle generator reads `HelperScripts/gamedata/abilitydata.slk`, `abilitymetadata.slk`, and `WorldEditStrings.txt`. It also reads the WurstStdlib2 `AbilityDefinition` base class so it can mark generated preset overrides correctly. It writes its full generated ability source plus additions into `HelperScripts/`. Merge the `_additions.wurst` files into the corresponding WurstStdlib2 ability files by raw ability ID, preserving legacy constants/classes and adding missing classes when a display-name collision maps to a different ID. Multi-level fields need both `setX(level, value)` and `presetX(LevelClosure)` methods; the preset writes all levels and adds the tooltip property. Run the stdlib typecheck after that merge.

Keep the extractor's required input list aligned with the filenames consumed by `generate-obj-mappings.ts` and `GenAbilities.java`. The extractor rejects missing or ambiguous paths before writing files so a game archive layout change cannot silently produce a partial snapshot.
