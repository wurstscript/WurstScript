# Lua body hot reload (prototype)

Status: implemented behind `-luaHotReload`. The whole path (a payload compiled by the editor, written as a preload
file, picked up by the running map and installed) was measured on the 3.0.1.24371 client in a test map, last with ten
payloads in one game across every kind of edit; see "Measured in the game", where the editor side was also driven
through the language server as VS Code drives it (22 Oct).
Background: [LUA_RUNTIME_OPPORTUNITIES.md](LUA_RUNTIME_OPPORTUNITIES.md), "Lua hot reload".

## What it does

A map built with `-luaHotReload` takes edits of its program while it runs: changed and new functions, changed
signatures, new globals, class fields, classes, interfaces and lambdas, and new packages. The editor compiles the
edited project and builds a payload from the difference between the script the map runs and the edited one. The map
compiles the payload with `load`, checks it, and applies it. Globals, field stores, class tables, objects and the
allocator keep their state; a tuned constant takes its new value, and so do the constants computed from it (see
"Constants"). What a payload cannot change is rejected with the reasons, and the map is left untouched.

```
edit ─► language server: compile with -luaHotReload ─► LuaHotReload.payload(baseline, current, history)
     ─► CustomMapData\Wurst\hotreload-<n>.pld ─► map: poll ─► Preloader ─► tooltip ─► load ─► check ─► install
```

## Design

### Names from identities

Release builds number generated Lua names in translation order. A hot-reload build names each function, global,
class, field store, class allocator and dispatch stub after its source name and a hash of its identity (package, IM
name and signature), and numbers locals within their own function. So an edited program's script differs from the
running one only where the program changed: an addition or removal renames nothing else.

### The script in parts

The manifest of a build lists its script in the parts a payload applies one by one: every function's definition,
every top-level declaration (field stores, class tables, native fallbacks), and the statements of the bootstrap (type
defaults, class-table slots, class data), of the global initialisers and of each package init (initial values).

In a hot-reload build, source global initializers become separate functions before IM flattening. Each call in a
package init denotes the complete initializer, including its locals, control flow, array stores and tuple components;
the manifest records that function's definition as the call's revision. A payload guards the call as one unit. It
never extracts a final assignment from the flattened code or confuses an assignment in an init block with a source
global initializer. This adds an initialization-time call only in opt-in development builds.

Each source initializer also records the globals it writes after tuple lowering and generic specialization.
Adding an initializer to an existing global preserves its live value, including globals an earlier payload added
without an initializer. A runtime marker preserves that decision across later cumulative payloads and whole-package
initialization. Changing the storage covered by an existing initializer requires a restart: replaying the complete
unit would also overwrite its existing values. A new initializer spanning both old and new storage is rejected
for the same reason, using both baseline identities and declarations actually installed by earlier payloads.

A hot-reload build therefore keeps the storage of every global declared in the source, whether anything reads it or
not (`ImTranslator.isHotReloadSourceGlobal`, a root in the garbage removal, the tree shaker and the Lua garbage
removal; not the globals of common.j, blizzard.j or generic classes). Otherwise a constant nothing reads, or a
variable only updated from itself, loses its storage while its initializer stays, and the unit's storage changes
between `[]` and `[x]` when an edit starts or stops reading it: in a replay of a real map's history that was one of the
two largest causes of refused payloads. Kept, a global an edit starts to read has the value its initializer gave it
when the map started (`startingToReadAGlobalKeepsItsStorage`). A build without the flag still removes them.

The payload compares the running script's manifest with the edited one:

- A new or changed function is defined, and every class-table slot which binds it is bound again. One stored as a
  value anywhere else keeps the body it was taken from, so a change to or removal of it is rejected. This includes
  callback adapters already registered by the baseline or an earlier payload.
  A removed method is also rejected if an existing class-table slot can still bind it; rebinding that slot to a
  current implementation is safe. This check also covers removed slot relationships whose function survives as an
  ordinary method. The session preserves slot bindings from earlier payloads for this check.
- A new top-level declaration runs (a new class table, a new field store); a changed one is rejected.
- A new bootstrap or initialiser statement runs; a changed slot binding runs; the initial value of a new global runs.
- A changed initial value of a constant which only computes its value runs again (see "Constants"). A changed initial
  value of any other global the map has is skipped with a warning: the map keeps the value it has.
  Removing the initializer has the same warning. For a global added by an earlier payload, its run-once marker in
  the map determines whether that removed initializer actually ran before the map warns.
- Any other changed bootstrap statement (class data of a class which exists) is rejected, and so is a name which
  now means another declaration or a type, such as a field which holds another class.
- Interface/class closures bound to source variables include those variable names in their class identities in
  hot-reload builds. Adding or reordering distinct named siblings keeps existing closures' identities and captured
  values; their methods can be replaced while old objects remain alive. Nested closures include outer variable
  bindings too. The source owner's scope and structural signature distinguish equal binding names in overloaded
  functions, including packages and enclosing scopes of nested parameter types. A closure in a package's `init` block
  is owned by the package, one in a constructor by the constructor's function. Removing or renaming a closure still requires a restart
  when its old class-table bindings survive. Identities name the classes a nested class is declared in, so two inner
  classes of one name in one package (`A.N`, `B.N`) are two.
  Siblings still sharing an identity (anonymous closures, such as several `doAfter` callbacks in one function, or equal
  binding names within one callable) are told apart by their order, which matches an object to the closure it was made
  from. Editing their bodies keeps that order, and so does adding one after them: both reload. A closure whose source
  (`Function.closureSource`, the closure as written) is now what another one of the group had, which that one lost,
  moved, and an object made from the closure at its old place would run the other one's body: the map refuses that,
  and removing one, with a restart. A copy of a body another one keeps, appended or edited in, moves nothing. What the
  map runs of each is the baseline's source or one an earlier payload sent; the editor does not know which payloads
  the map installed, so it keeps every one sent (`History.groupShapes`). In Castle Fight, 1,050 of its 2,712 closures
  are in such groups.
- With `-stacktraces` a call passes its source position, so an edit which moves lines changes the bodies below it. The
  payload ships them with their new positions, but every comparison of what must not change or what already ran (a
  protected body, a package init, an initial value) leaves the positions out (`Function.shape`): a moved line is not
  an edit.
- All package initialization follows the order the map starts packages: after their dependencies. At each package,
  a new package's whole init runs once; an existing package only initializes its added globals. This includes packages
  an earlier payload added, so their later globals are ready before dependent packages initialize. If a new init fails, the
  install fails with that reason and the next payload runs it again. Other changed package
  init code, and a change to `main` or `config`, ran when the map started and does not run again: the payload carries
  the change, with a warning that applying it needs a restart.
  For packages added by payloads, the map records the init code it ran and prints the restart warning when a later
  payload changes it; resending unchanged code gives no warning.
  Init comparisons preserve statement order and multiplicity, so removing one repeated call or reordering existing
  initialization work also warns.
  Each initializer unit also records its first successful completion position in the map, and when that was: at the
  start or in which payload. Payloads compare their requested initializer order against those positions, among the
  units which first ran together, covering globals added by earlier payloads without assuming that delivered files ran.
  A global a payload adds runs after every one the map has, wherever it is in the source; that is no reordering. A map
  which missed an earlier payload records only the order it actually executes. The started map ran its initializers
  in the baseline's order, so a payload lists them only when the edit puts two of them in another order; otherwise it
  lists those payloads added. Likewise it checks storage for a mix of old and new only for a unit with storage the
  started map lacks. Listing every initializer made a payload of Castle Fight 403,624 bytes with nothing changed, past
  the 200,000 characters the delivery was measured with; it is now 93 bytes.
- A field added to a class gives objects made before it its default; an array or tuple default is made once per object
  and kept, so what is stored in it stays.

A field store of a hot-reload build answers its type's default for an object it has no entry for, so an object made
before a payload added the field reads the default; objects made after run the new constructor.

The payload of an edit is cumulative from the start of the map: every function which differs from the baseline, or
which an earlier payload defined, is defined again, so an edit which is undone is undone in the map too. Each
statement which must run once (a declaration, an initial value, a new package's init) is guarded by the map's own
record (`__wurst_hotreload_ran`), kept under what the statement assigns, and recorded only after it ran. The editor
writes a file and cannot tell how far a payload got, so the map decides: a statement a failed payload did not reach
runs with the next one, and none runs twice. A global a payload added keeps its value when a later edit changes its
initial value, as the map's own globals do, and the map says so. A declaration a payload installed (a class table, a
field store) which a later edit changes cannot be kept: the map refuses that payload before it changes anything. A
separate runtime record (`__wurst_hotreload_bindings`) preserves the semantic identities and types of declarations
each payload actually installed. These are checked before mutation as well: equal Lua text does not make a field
which held one class compatible with a field which now holds another. The record is updated as each declaration is
installed, so a failure partway through a payload still protects the declarations it reached. A
function stored as a value keeps the body it had
then: the session records that definition, and a later change to it is rejected. The fingerprint identifies the running script; a payload carries the running script's fingerprint.

### Constants

A changed initial value of a constant (`constant` or `let`, including a `@config` override of one) runs again, and so
does every constant computed from it, in the order the map initialises them. Nothing but its initializer writes a
constant, so it gets the value a restarted map would give it. `ConstantInitializers` decides which initializers may
run again: those which only compute their value. Such an initializer reads its own locals, constants initialised before
it and the game's constants; it writes only its own global; it calls only natives whose result depends on their
arguments alone (not getters of game state such as `GetPlayerTeam`), the backend's operator intrinsics and functions
which follow the same rules. The stack-trace bookkeeping does not count. Every other global
keeps its value, with the warning:

- a variable, which holds the map's state;
- a constant which allocates an object or calls a native with effects: running it again would leak what it made;
- an array constant: running it again would keep the entries an edit drops;
- a constant which reads a variable, or a constant initialised after it (from a package an `initlater` import
  initialises later), which held its default when the map started;
- a constant which reads an element of an array: code may assign an element of a constant array.

The manifest lists the initializer of each constant, whether it is pure, which constants it reads and which functions
of the program it calls. A payload runs one again when its definition changed, a function it calls changed, or a
constant it reads runs again. The map records the revision each one last ran (`__wurst_hotreload_constants`): its
definition together with the functions it calls and the revisions of the constants it reads. So an undone edit runs
the original initializer again, a map which missed a payload still ends at the current values, and a constant a
payload added runs again when a later edit changes it. A constant which the baseline or an earlier payload declared as
a variable, with an initial value or without, may hold a value code wrote; the manifest lists the storage of every
variable, the session records those of earlier payloads (`History.variables`), and such a constant keeps its value,
with the warning, and so does every constant computed from one, rather than the map mixing new values with ones
computed from the old.

Copies taken at startup do not follow: a unit's life set from a constant in `init`, or a timer started with a constant
period: a reload hook takes them again (see "Reload hooks"). Measured on the 3.0.1.24371 client: phases 7 and 9 of the
in-game suite (see "Measured in the game"); the unit tests run it with and without `-stacktraces`
(`tunedConstantsReachTheRunningMap`).

### Answers to the staged plan

| Point | Answer in the prototype |
| --- | --- |
| Engine `load` and delivery | Measured on the 3.0.1.24371 client: the preload file's Lua section runs, the tooltip carries the payload, `load` compiles it in the map's environment and the bodies are replaced (see "Measured in the game"). |
| Stable semantic ids | Release builds number generated names in translation order. A hot-reload build names each function, global, class, field, allocator and dispatch stub from its own identity instead: the source name and twelve hex digits of a hash of its package, IM name and signature (`score_` and the digits). Adding or removing a declaration therefore renames nothing else (`addedDeclarationsRenameNothing`, `addedDeclarationWithSimilarHashRenamesNothing`), and every function is named before any body is translated, so a new local cannot take a function's name (`newLocalNamedLikeALaterFunctionIsABodyEdit`). The fingerprint still binds each name to its identity and type. |
| Explicit lexical storage bindings | A payload is a separate chunk and cannot see the main chunk's locals. A hot-reload build therefore declares no main-chunk local: the allocator state, deallocator, hashtable sentinels and the old-generics zero become globals (`LuaTranslator.declareChunkLocal`). Every binding a body reaches is a global of the script's environment, and the payload is compiled with that environment (`load(source, name, "t", _ENV)`). Function-level aliases of field stores are inside the bodies and are rebuilt with them. |
| All slot aliases | `LuaHotReload.collectFunctionValues` finds every place a reloadable function is used as a value. A class-table binding `Class.slot = f` is repeated after the new definition (`Square.Circle_area = Square_Square_area` in the test), for every class which binds it. A callback adapter passes its target to `xpcall` by name on each call and needs nothing. Any other stored value would keep the old body, so a change to such a function is refused by name; it can still be reloaded while it does not change. Neither the test programs nor a program over `LinkedList` and `ClosureTimers` from the standard library has one. |
| ABI / version rejection | The map checks the fingerprint and that the payload's version is newer than the last applied one before it installs anything. A syntax error, a foreign script or a stale payload returns a reason and changes nothing. The poll also skips text it has already seen. |
| Inlining dependencies | `-luaHotReload` refuses `-inline` and `-opt` with an explicit error. Inlining copies bodies into callers, which the text rule would still handle, but a helper inlined everywhere disappears from the skeleton, and `-opt` renames every declaration per build. `-localOptimizations` is allowed: its interprocedural facts only change body text. |
| Saved callbacks | `ImFuncRef` is emitted as a callback adapter which calls its target by name, so triggers, timers and enumerations registered before a reload run the new body. A closure is an object whose class table binds its method, so its body is replaced like an override (`closureBodyIsReplacedWithTheStandardLibrary`). The closure's captured values are fields and keep their values. |
| Active frames | A call in progress finishes in the body it started in, because a Lua function value does not change. The next call by name or through a class table runs the new body (`replacedBodiesRunAndStateSurvives` replaces bodies while `init` is running and calls them from it). The poll runs in a timer callback of its own, so when a payload is installed the only other frames are threads suspended by a wait. Measured on the 3.0.1.24371 client: a thread waiting in `TriggerSleepAction` through a reload resumed in its old body and called the new body by name. The init of a package the payload adds runs once; other package inits and `main` never run again, so a change to them ships with a warning that it needs a restart. |
| Scope | Functions, globals, fields, classes, interfaces, lambdas and packages which are new or changed, and the values of tuned constants, in single-player development builds (see "The script in parts" and "Constants"). No migration of state: a changed field type, a changed initial value of an existing variable or changed class data is rejected or kept, never converted. |
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

Without the editor, a map built with `-build ... -luaHotReload` (with run arguments: `-luaHotReload` refuses `-inline`
and `-opt`) starts a session and takes payloads from the compiler's command line, which is how the in-game test drives
it:

```
java -cp wurstscript.jar de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession start <build dir> <CustomMapData>
java -cp wurstscript.jar de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession send <build dir> <CustomMapData>
```

`start` takes the map of the last build as the running one and deletes the payload files of an earlier session; `send`
writes the next cumulative payload for the last build, or exits with 1 and the reasons.

Game side (measured, see below): `main` starts a one-second timer which calls `__wurst_hotreload_poll`. The poll sets
the tooltip of ability `Aamk` to a space, calls `Preloader` on the next payload file, and reads the tooltip back. The
file's Lua section (between `//!beginusercode` and `//!endusercode`) puts `wurst-hot-reload-payload:` and the payload
in that tooltip, in chunks of 1,000 characters. Text with that marker moves the poll on to the next file and goes to
`__wurst_hotreload_apply`; `__wurst_hotreload_status()` answers what became of it (applied, followed by anything it
printed it could not apply, or rejected with the reason), and the poll prints it. The writer
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
- What a reactive library could add is re-running setup code after a reload, such as UI layout or derived values.
  The reload hooks below give a map that without a library runtime.

### Reload hooks

A package `init` ran when the map started and does not run again, so setup it did (UI frames, registered events, a
timer's period, values copied from constants) stays as it was. A function annotated `@onHotReload` (declared in the
standard library's `Annotations`) lets a map run such setup again:

```wurst
function setupUi()
	...

init
	setupUi()

@onHotReload function reloadUi()
	destroyUi()
	setupUi()
```

Each payload calls every hook once it installed everything else (functions, declarations, the inits of new packages,
constants), in the order the map initialises their packages, each package's in the order it declares them. A hook
runs with the code the payload has, including a hook the payload adds, and nothing calls a hook when the map starts.
One which fails does not stop the others or undo the payload: what it raised is printed and in the payload's status. A
hook takes no parameters and is static and not generic (`HotReloadHooks`, checked by the validator). The script runs
each one (`__wurst_hotreload_runHook`): with `-stacktraces` it passes the position every function takes, so a stack
trace taken in a hook names the reload, and puts the stack's depth back after it, which a failing hook would leave
raised. Hooks are the functions made for the annotated declarations, so a function reference's bridge to one does not
run it a second time. In a `-luaHotReload`
build the hooks are entry points, as preserved functions are; in any other build the annotation does nothing, so a hook
nothing else calls is not emitted.

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
| 5 | an edit changing a field's type (the language server rejects it), then one adding a function, a global, a class with a field default, an interface, a lambda, and a field to a class whose object was made at the start | the server's reasons name the field and both types; the next payload applied, the new function returned 84: the new class's 52, the new global's 30, the lambda's 2 and the old object's new field, which reads its default 0 |

The language server took 3.1 to 7.9 s per reload. A thread waiting through phase 1 resumed in its old body and called
the new one by name. The matrix found three defects, each fixed with a test: payloads after the first were never
read (one file rewritten, see "Delivery"); with no payload file the poll took the ability's tooltip for one; and a
local added to a body could take the name of a function translated later.

### 24 Oct 2026: nine payloads in one game

On the 3.0.1.24371 client, single player, a test map was built with `-build ... -lua -luaHotReload` and a project's
default run arguments (`-runcompiletimefunctions -injectobjects -stacktraces`), its session started from the compiler's
command line (`LuaHotReloadSession start`, see "Delivery"), and the game launched. Each time the map asked for the next
phase, the edited project was built again and the next payload written (`LuaHotReloadSession send`). 44 checks, all
passed:

| Phase | Edit | In the game |
| --- | --- | --- |
| 1 | the 15 kinds of body of the first matrix (above) | every body new; objects made before keep their fields |
| 2 | 300 bodies, and one of a function with 210 locals | applied |
| 3 | phase 1's edit of a free function undone | the original body is back, the other edits stay |
| 4 | a stale payload, one for another script, one which does not compile | each rejected; nothing changed |
| 5 | a field's type changed, then a new function, global, class with a field default, interface, lambda, and field | the first refused by the compiler with the reasons; then 84, as in the first matrix |
| 6 | a closure bound to another variable added before an existing one, whose body changed | the closure made at the start returns 11 (its new body, its seed 5); new ones 1014 |
| 7 | a configured constant 5 to 7, a real and a variable | 7, the constant computed from it 14, the string computed with `I2S` "lvl7", the real 250; the variable kept 5 |
| 8 | a new package | its init ran once |
| 9 | a constant of that package, 40 to 55 | 55; the init did not run again |

A thread waiting in `TriggerSleepAction` through phase 1 resumed in its old body and then called the new one by name,
as on 22 Oct. Every payload which applied printed nothing but "applied payload n" (each phase checks the status, which
now carries what a payload printed it could not apply). A payload arrived 25 to 51 s after the map asked for it (phase
5 builds twice), almost all of it the build: about 24 s with the game running, 14 s without.

The suite found four defects the unit tests had missed, each fixed with a test: a closure in a package's `init` block
crashed every `-luaHotReload` build, the standard library's `Preloader` among them
(`closuresInAnInitBlockAreNamedAndReloaded`); so did a closure in a constructor
(`closuresInAConstructorAreNamedAndReloaded`); with `-stacktraces` a moved line counted as a change of the closures
told apart by their order, so any edit above them was refused (`movedLinesDoNotFreezeClosuresToldApartByOrder`); and a
global added to a package which starts before another made every later payload print "global initializer order
changed" (`addedGlobalBeforeALaterPackageIsNoReorder`).

### 24 Oct 2026: ten payloads, with a reload hook

The same suite, run as above with a tenth phase and a reload hook. 51 checks, all passed:

| Phase | Edit | In the game |
| --- | --- | --- |
| 10 | of three callbacks of one interface made in one function, told apart by their order, the second's body edited and a third appended | the second, made when the map started, returns its new 20; the first keeps its 1; the appended one returns 3 |
| every phase | an `@onHotReload` function counts the payloads and copies a constant into a variable, as setup in an `init` does | it ran after each of the nine payloads which applied and not at the start; after phase 7 the copy holds the tuned 250; phase 10 edited the hook, and that payload ran the new body (108: eight times 1, then 100) |

Every payload which applied printed nothing else, so no hook failed.

Not measured: whether the preload file's Lua section sees the map's globals (it does not need to); a real project's
edits; the editor's part since 22 Oct (the suite builds from the command line); multiplayer, which is refused.

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

## Edits of a real project

Castle Fight's last 30 commits which change Wurst code, each built with `-lua -luaHotReload` and its payload made
against the commit before it (24 Oct 2026, with the order groups, hooks and payload checks above): 8 are accepted, 22
refused. A commit is a whole pull request, so this overstates what one edit meets, but it ranks the causes. Commits
each cause refuses, and in how many it is the only one:

| Cause | Refuses | Only cause |
| --- | --- | --- |
| a constant array literal changes length, which is part of its type (`string[74]` to `string[75]`) | 14 | 4 |
| a constant starts or stops being read: the dead-code removal drops unread storage, so its initializer's storage changes between `[]` and `[x]` | 7 | 2 |
| a closure removed, which a class-table slot may still hold | 7 | 0 |
| a closure inserted before others told apart by their order | 6 | 2 |
| a new or removed type argument of a generic class with statics, whose one initializer stores every specialisation | 5 | 0 |

Without the first two, 17 of the 30 would be accepted. The accepted payloads also carry far more than the edit: one of
5 changed files shipped 621 functions (2,033,445 bytes), 550 of which differ only in the names of the flatten's
temporaries (`temp159` to `temp162`), which are numbered across the program.

## Next stages

1. The causes above, most first, and temporaries named within their function.
2. Multiplayer through a synchronised channel, only with measurements of its ordering guarantees.
