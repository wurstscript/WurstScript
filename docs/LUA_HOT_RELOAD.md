# Lua body hot reload (prototype)

Status: implemented behind `-luaHotReload`. The whole path (a payload compiled by the editor, written as a preload
file, picked up by the running map and installed) was measured once on the 3.0.1.24371 client in a test map; see
"Measured in the game", where the editor side was also driven through the language server as VS Code drives it.
Background: [LUA_RUNTIME_OPPORTUNITIES.md](LUA_RUNTIME_OPPORTUNITIES.md), "Lua hot reload".

## What it does

A map built with `-luaHotReload` can have the bodies of its functions replaced while it runs. The editor compiles
the edited project, and the payload carries the definitions of the functions whose printed Lua changed. The map
compiles the payload with `load`, checks it, and rebinds the functions. Globals, field stores, class tables, objects
and the allocator are left as they are, so the running game keeps its state. An edit outside the bodies is rejected
with the reason, and the map is left untouched.

```
edit ─► language server: compile with -luaHotReload ─► LuaHotReload.payload(baseline, current)
     ─► CustomMapData\Wurst\hotreload-<n>.pld ─► map: poll ─► Preloader ─► tooltip ─► load ─► check ─► install
```

## Design

### The script is a skeleton plus bodies

`LuaHotReload` cuts the translated script in two. The bodies are those of the functions translated from the
program (`LuaTranslator.translateFunc`). The skeleton is everything else: globals and their initial values, field
stores, class tables, the bootstrap with its slot bindings, generated helpers (allocators, dispatch stubs, callback
adapters, polyfills), and each function's name and parameters.

A payload replaces bodies and nothing else, so it is valid only for a map running the same skeleton. The **ABI
fingerprint** is a SHA-256 of the skeleton text, of the source identity of every emitted name, and of the compiler
version. It is emitted as `__wurst_hotreload_abi` and carried in every payload.

The payload of an edit is **every function whose printed definition differs**, compared with the map's baseline,
plus every function an earlier payload replaced. This one rule covers what a list of dependencies would otherwise
have to track:

- A fact which one body's edit changes in another body (a call the effect analysis now keeps, a folded constant) changes
  that body's text, so the body ships too.
- A source position which moves under `-stacktraces` changes the text of the bodies which print it, so they ship
  (`LuaHotReloadTests.movedLinesShipTheBodiesWhosePositionsChanged`). No position is held outside the bodies.
- Anything which moves into or out of the skeleton changes the fingerprint and is rejected. Examples: a new
  function, specialisation, closure class or dispatch stub; a global; class metadata which a body starts to read;
  a compile-time result.

Payloads are **cumulative**, so the editor needs only the baseline manifest and the set of functions it has sent
(`LuaHotReloadSession`). A map which missed a payload still ends up running the current program, and an edit which
is undone is undone in the map.

### Answers to the staged plan

| Point | Answer in the prototype |
| --- | --- |
| Engine `load` and delivery | Measured on the 3.0.1.24371 client: the preload file's Lua section runs, the tooltip carries the payload, `load` compiles it in the map's environment and the bodies are replaced (see "Measured in the game"). |
| Stable semantic ids | Release builds number generated names in translation order. A hot-reload build names each function, global, class, field, allocator and dispatch stub from its own identity instead: the source name and twelve hex digits of a hash of its package, IM name and signature (`score_` and the digits). Adding or removing a declaration therefore renames nothing else (`addedDeclarationsRenameNothing`, `addedDeclarationWithSimilarHashRenamesNothing`), and every function is named before any body is translated, so a new local cannot take a function's name (`newLocalNamedLikeALaterFunctionIsABodyEdit`). The fingerprint still binds each name to its identity and type. |
| Explicit lexical storage bindings | A payload is a separate chunk and cannot see the main chunk's locals. A hot-reload build therefore declares no main-chunk local: the allocator state, deallocator, hashtable sentinels and the old-generics zero become globals (`LuaTranslator.declareChunkLocal`). Every binding a body reaches is a global of the script's environment, and the payload is compiled with that environment (`load(source, name, "t", _ENV)`). Function-level aliases of field stores are inside the bodies and are rebuilt with them. |
| All slot aliases | `LuaHotReload.collectFunctionValues` finds every place a reloadable function is used as a value. A class-table binding `Class.slot = f` is repeated after the new definition (`Square.Circle_area = Square_Square_area` in the test), for every class which binds it. A callback adapter passes its target to `xpcall` by name on each call and needs nothing. Any other stored value would keep the old body, so such a function is refused by name. Neither the test programs nor a program over `LinkedList` and `ClosureTimers` from the standard library has one. |
| ABI / version rejection | The map checks the fingerprint and that the payload's version is newer than the last applied one before it installs anything. A syntax error, a foreign script or a stale payload returns a reason and changes nothing. The poll also skips text it has already seen. |
| Inlining dependencies | `-luaHotReload` refuses `-inline` and `-opt` with an explicit error. Inlining copies bodies into callers, which the text rule would still handle, but a helper inlined everywhere disappears from the skeleton, and `-opt` renames every declaration per build. `-localOptimizations` is allowed: its interprocedural facts only change body text. |
| Saved callbacks | `ImFuncRef` is emitted as a callback adapter which calls its target by name, so triggers, timers and enumerations registered before a reload run the new body. A closure is an object whose class table binds its method, so its body is replaced like an override (`closureBodyIsReplacedWithTheStandardLibrary`). The closure's captured values are fields and keep their values. |
| Active frames | A call in progress finishes in the body it started in, because a Lua function value does not change. The next call by name or through a class table runs the new body (`replacedBodiesRunAndStateSurvives` replaces bodies while `init` is running and calls them from it). The poll runs in a timer callback of its own, so when a payload is installed the only other frames are threads suspended by a wait. Measured on the 3.0.1.24371 client: a thread waiting in `TriggerSleepAction` through a reload resumed in its old body and called the new body by name. Package inits and `main` never run again: a changed init body ships but has no effect until the map restarts. |
| Scope | Named function and closure bodies; single-player development builds. No schema, init or closure-state migration. |
| No release overhead | Without the flag the emission is unchanged, byte for byte (see "Release output"). With it, the runtime costs a global lookup where a main-chunk upvalue was, plus a once-a-second poll. |
| Multiplayer | Not supported: a preload file is local to one machine, so applying it would desync the game. The poll returns at once unless `bj_isSinglePlayer`. Multiplayer would need the payload sent through a synchronised channel (for example `BlzSendSyncData` from the host, in chunks), applied by every client at the same point in the game's order, with the fingerprint checked identically on every client. |

### Delivery

Editor side (implemented, `LuaHotReloadSession`, `RunMap`):

1. Start the map with hot run (`wurst.hotstartmap`, which sends `-hotstart`). On a Lua project that is a
   `-luaHotReload` build (`RunArgs.isLuaHotReload`); an ordinary run with `-luaHotReload` in `wurst_run.args` does the
   same. The run saves the manifest of the started script in `_build/luaHotReload/session.json`.
2. `wurst.hotreload` sends `-hotreload`. For a Lua project, `RunMap` compiles the project, builds the next cumulative
   payload against the saved baseline and writes payload n to `CustomMapData\Wurst\hotreload-<n>.pld`
   (`customMapDataPath` in the configuration overrides the folder). A hot run deletes the files of earlier sessions. JHCR stays the path for Jass maps. A rejected edit fails the request
   with the reasons. While a hot run is active, the VS Code extension sends it on every save of a Wurst file.

Game side (measured, see below): `main` starts a one-second timer which calls `__wurst_hotreload_poll`. The poll sets
the tooltip of ability `Aamk` to a space, calls `Preloader` on the next payload file, and reads the tooltip back. The
file's Lua section (between `//!beginusercode` and `//!endusercode`) puts `wurst-hot-reload-payload:` and the payload
in that tooltip, in chunks of 1,000 characters. Text with that marker moves the poll on to the next file and goes to
`__wurst_hotreload_apply`; `__wurst_hotreload_status()` answers what became of it, and the poll prints it. The writer
escapes `//!` inside the payload so that no marker can end the section early. Three facts of the game shape this,
each measured on the 3.0.1.24371 client:

- `Preloader` reads a file once: a file rewritten during the game was never seen again (20 s), while a file created
  during the game was read within 0.5 s. So each payload has a file of its own, and the poll reads each file once,
  in order, whether its payload applies or not.
- Setting a tooltip to `""` does not clear it: the poll then read the ability's own text. So the poll resets the
  tooltip to a space and only takes text after the marker.
- A tooltip and a preload file each carried 200,000 characters intact. The payload is relayed through the tooltip
  because whether the file's Lua section runs in the map's environment is not measured.

The test runtime runs the same path: `TestConfig.luaHotReload` writes the preload file, its `Preloader` stub runs the
Lua section, and the program's `hotReload()` calls the emitted poll. The stubs keep a tooltip's text when it is set to
`""`, as the game does. `pollReadsEachPayloadFileOnceInOrder` drives the poll through a payload, a broken file and
the next payload.

### Why not a reactive emission

The suggestion was to emit the script "reactively", using a signal/watcher package (`Reactive`: `Signal`,
`Computed`, `watch`). That package is project code, not part of the standard library. Signals and watchers are the
wrong place to replace bodies:

- Lua already gives a mutable cell per function: the global, or the class-table slot, which every call reads.
  Rebinding it is the whole mechanism, at zero cost to a call. Routing calls through signal objects would add an
  object read and dependency tracking to every call. That is the compiler-introduced call overhead AGENTS.md §7
  forbids, and it would make the development build behave and perform unlike the release build.
- What a reactive library could add is the part this prototype leaves out: re-running setup code after a reload,
  such as UI layout or derived values. A library-level hook (a reload counter as a signal, or an `onHotReload`
  callback which the poll fires after an install) would let a map opt into that. It belongs in the library and
  needs no compiler support beyond the poll calling it. It is a follow-up, not part of this prototype.

### Release output

With the flag off, the change only adds unused fields and branches to the translator. Proof: a large public map,
built from master and from this branch with the same command line, produces byte-identical Lua (see
"Measurements").

## Measured in the game

On the 3.0.1.24371 client, single player, a test map built with `-lua -luaHotReload` (no `-inline`) ran a suite which
read two bodies, wrote a ready file, and waited for them to change. When the ready file appeared, the editor's
payload was copied to `CustomMapData\Wurst`. The payload was built by `LuaHotReloadSession` from the
manifests of the map and of the same map with the two bodies edited, and held exactly those two functions. Results:

| What | Before | After |
| --- | --- | --- |
| a function called by name (`return 1`, edited to `return 2`) | 1 | 2 |
| an override reached through its class table (`4`, edited to `40`), on an object made before the reload and one made after | 4 | 40, 40 |
| the sibling class's override (unchanged) | 3 | 3 |
| a global set before the reload | 7 | 7 |
| `bj_isSinglePlayer` | 1 | |
| seconds from the first check after the ready file to the new body | | 0.5 |
| characters in the `Aamk` tooltip after the poll (the payload) | | 343 |

An object allocated after the reload got a higher id than the objects allocated before it (16 more, other
allocations between), so the allocator was not restarted. The first attempt on the real map found a defect which the
unit tests had missed: the id of a function whose signature has a new-generics type variable included the IM
printer's identity hash, so two compilations of the same map never matched. Ids now print types structurally
(`genericSignaturesHaveTheSameIdInEveryCompilation`).

The same suite then ran with the editor side driven through the language server, over the protocol, as the VS Code
extension drives it: `wurst.hotstartmap` compiled the map and started the session (the game was launched for the test,
from the map that request prepared); when the map was ready, the probe file was edited and saved (`didChange`,
`didSave`) and `wurst.hotreload` sent, as the extension does on save. The request compiled the project and wrote the
payload in 3.7 s, and the map ran the new bodies 4.5 s after the ready file, with the same results as above.

A matrix then ran five payloads in one game, with `-stacktraces` on as a project's run arguments have it, and the
language server driven as above (edit, `didChange`, `didSave`, `wurst.hotreload` per phase):

| Phase | What | Result |
| --- | --- | --- |
| 1 | one payload editing a free function, an override, a trigger action and a periodic callback both registered before the reload, a new-generics function and method, an old-generics function, a tuple result, varargs, a constructor, `ondestroy`, a string with quotes, a backslash, `ü` and `//!`, a static function | applied 8.5 s after the map was ready; every body new, objects made before keep their fields |
| 2 | 300 small functions and one with 210 locals (a 93.7 KB file) | applied in 6 s; the 210-local function, whose locals the compiler moves into a table, returns its new value |
| 3 | an edit of phase 1 undone | the original body is back in 5 s; the other edits stay |
| 4 | a stale payload, one for another script, one which does not compile | each rejected (the last status: a syntax error); nothing changed |
| 5 | an edit adding a function (the language server rejects it), then a body edit | the server's reasons name the new function; the next payload applied in 8 s |

The language server took 3.1 to 7.9 s per reload. A thread waiting through phase 1 resumed in its old body and called
the new one by name. The matrix found three defects, each fixed with a test: payloads after the first were never
read (one file rewritten, see "Delivery"); with no payload file the poll took the ability's tooltip for one; and a
local added to a body could take the name of a function translated later.

Not measured: whether the preload file's Lua section sees the map's globals (it does not need to); a real project's
edits; multiplayer, which is refused.

## Probe for other clients

The probe is `profiling/lua-opportunities/hot-reload-probe.lua`, plus the file `hot-reload-probe.pld` next to it,
which is in the editor's layout and kept equal to the writer's output by `preloadFileCarriesThePayloadText`. It
reports, for the maintainer to record with the client version:

- `single-player`: `bj_isSinglePlayer` and `ReloadGameCachesFromDisk()` in a single-player game;
- `load-with-env`: `load` exists, a loaded body reads the script's globals, and a syntax error returns nil;
- `tooltip-channel`: an ability tooltip keeps 100,000 characters;
- `preloader-runs-user-code`: `Preloader` runs the Lua section of a file the game wrote;
- `preloader-sees-map-globals`: whether that section sees the map's globals and `load`; if it does, the tooltip relay
  can be dropped;
- `preloader-large-section`: 100 lines of 1,000 characters arrive whole;
- `tool-written-file`: a file in the editor's layout (copied by hand into `CustomMapData\Wurst`) hands its text over
  unchanged, `//!` included;
- `apply-in-timer`: a body replaced inside a timer callback runs when next called by name.

If `Aamk` turns out to be unsuitable as the channel, any ability whose tooltip the map never shows will do; it is
`LuaHotReload.CHANNEL_ABILITY`.

## Measurements

On 22 Oct 2026, with Windows, Java 27 and the public map `wurst-castle-fight`, built with `-build ... -lua
-runcompiletimefunctions -injectobjects -noExtractMapScript -compiletimeCache`, once with master (964784183)
and once with this branch:

| Configuration | Master | Branch, flag off | Same bytes |
| --- | --- | --- | --- |
| as above | 9,590,423 bytes | 9,590,423 bytes | yes (SHA-256 `84c45f7e…`) |
| plus `-inline -localOptimizations -opt` | 3,915,973 bytes | 3,915,973 bytes | yes (SHA-256 `030d6abc…`) |

With `-luaHotReload`, the same map compiles to 9,591,776 bytes (1,353 more) and passes `luac -p`. The script
declares no main-chunk local (master declares 11), and has 10,927 reloadable functions, none of them stored as a
value. The build took about as long (27 s, against 24 to 29 s). No edit was reloaded on this map: that needs the
editor loop in the game.

## Next stages

Stable names are in place. The goal is that nearly every edit reloads:

1. New functions and signature changes: the payload defines the new functions; callers of a changed signature ship
   anyway, since their text changes.
2. New globals and class fields: a new global runs its initialiser once; a new field gets its own storage table, whose
   default existing objects read.
3. New lambdas and classes: the payload builds their class tables and dispatch slots.
4. A reload hook for libraries (see "Why not a reactive emission").
5. Multiplayer through a synchronised channel, only with measurements of its ordering guarantees.
