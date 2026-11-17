# Moving object data into SLK tables (`-slkobjects`)

Add `-slkobjects` to `wurst_run.args` (or `+slkobjects` for release builds
only) to move the built map's object modifications (`war3map.w3u`, `.w3t`, `.w3b`,
`.w3a`, `.w3h`, `.w3q`) into SLK tables and profile `.txt` files, which the game loads
faster than it applies object modification files. The option is off by default and
also works on a direct compiler invocation with `-build`.

The merge uses wc3libs' `ObjMerger`. A map's table replaces the game's table of the
same path, so the merge writes whole tables: it starts from the Warcraft III 3.x game
data bundled with wc3libs, in the data set the map's `war3map.w3i` selects, under any
tables and profile files the map already ships (for example from `imports`). What the
formats cannot hold stays in a smaller object modification file which the game still
applies: ability levels above four, `TRIGSTR_` references, values the formats cannot
quote, profile changes of modified base objects, doodads and `war3mapSkin.*` files.
A file left empty is removed. The build log lists how many objects were merged and
each object that was kept with its reason.

Only the published map is merged. A map build merges the output map in `_build`;
Run Map launches an SLK copy (`_build/cache/<cached map>_slk.w3x`). The cached map
keeps its object files, because the next build compares them with the objects it
generates.

The merged map is larger by the size of the tables it ships. Strings a custom object
inherits from its base object are baked in from the bundled English data. The game's
table reader can read a decimal one float step off (`1.5` as 1.5000001), so a real
moved into a table can differ from the original in its last bit.

A project whose `wurst.build` pins a classic `wc3Patch` (before 1.32) cannot use the
option: the build fails before compiling, because the merged tables are Reforged data.
