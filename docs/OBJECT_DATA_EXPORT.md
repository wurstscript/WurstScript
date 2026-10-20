# Exporting generated objects for World Editor placement

In VS Code, run **wurst: Export object data (.w3o) for World Editor** and select the
input map. The command runs compiletime generation and writes
`_build/wurstCreatedObjects.w3o`, then offers **Reveal Export**. It does not package
a release map or launch Warcraft III. No change to `wurst_run.args` is needed.

## Exporting during a build

Add `-exportw3o` to `wurst_run.args` and build the map. This opt-in export writes
`objectEditingOutput/wurstCreatedObjects.w3o` beside the map being compiled.
For the normal build/run pipeline, that map is the cached map under `_build/cache`,
so the export is `_build/cache/objectEditingOutput/wurstCreatedObjects.w3o`.
Without a map, it is written under `_build/objectEditingOutput`.

The export combines the input map's object modifications with the objects created
by compiletime functions, across all seven object types. It is produced even when
the object data is unchanged and the build skips rewriting it into the cached map.
Ordinary builds do not produce the combined file.

For a direct compiler invocation, use `-exportw3o` together with
`-runcompiletimefunctions` and the usual project/map arguments. `-injectobjects`
controls injection into the compiled map separately. The existing `-exportobjects`
option exports map object data to Wurst source; it does not produce a `.w3o`.

Import the `.w3o` through the World Editor Object Editor's **Import All Object Data**
action, place the generated units or doodads, then save the map and use it as the
project's input map again. The combined export includes the input map's editor
objects because this import replaces all object data.

Generated custom objects carry the integer modification `wurs = 42`. On the next
build, Wurst removes those tagged definitions before running the generators again.
Keep their rawcodes stable so existing placements refer to the regenerated objects.
Changes made to tagged definitions in the editor are replaced by generation too;
change their Wurst definitions instead.

Modifications to existing Blizzard objects (where the new id equals the base id)
are not tagged. Once imported into the input map, those modifications become part
of its base data; removing the corresponding Wurst code does not remove them from
that input map. Tagging and deleting the entire original-object entry would also
delete editor-authored modifications that Wurst merges with. Safe cleanup would
need to track and restore the original fields; `wurs = 42` alone cannot do that.
