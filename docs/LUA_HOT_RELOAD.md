# Lua body hot reload (prototype)

Status: implemented behind `-luaHotReload`. The whole path (a payload compiled by the editor, written as a preload
file, picked up by the running map and installed) was measured on the 3.0.1.24371 client in a test map, last with
fourteen payloads in one game across every kind of edit, and in Castle Fight with real edits; see "Measured in the
game", where the editor side was also driven through the language server as VS Code drives it (22 Oct).
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

### Incremental frontend translation

The language server retains the program before compile-time execution and backend lowering. An ordinary function
body edit updates that input while its declaration dependencies and generated declarations stay unchanged.
Unchanged bodies keep their source nodes, with refreshed positions, so closures and diagnostics retain their
identities. Declaration edits, changed dependencies, constructors, init blocks, extension functions and nested-class
body edits rebuild the frontend input. A function edit which creates or removes a closure also rebuilds it.

The general path copies the complete IM graph and frontend metadata into private state, then reruns compile-time
evaluation and backend lowering. Compile-time results are never reused by this cache. A failed or cancelled private
compilation cannot change the retained input; cleaning the editor model releases it. CLI processes still compile
from scratch. There is one compilation mode; cache invalidation is automatic.

This reduces frontend work but retains another IM program in memory, and a rebuild also pays for the private copy.
Unsupported backend edits still lower and emit the whole program; the supported slices below reuse function artifacts. See [RUNMAP_PERFORMANCE.md](RUNMAP_PERFORMANCE.md) for
measurements and the remaining work.

### First incremental backend slice

After a successful compilation with matching arguments, the managed map-compilation path can reuse immutable Lua
fragments and materialise only edited scalar functions. This covers live, top-level non-generic integer-returning
functions with integer/boolean parameters and locals, assignments, branches, loops and returns. Expressions may use
owned variables, literals, integer `+`, `-`, `*`, unary minus, comparisons and boolean operators. Calls, storage access,
other types and annotations are excluded. Only literal changes preserving the body structure, variable slots and
source positions are supported. A changed parameter, call, operator structure, declaration or source position takes
the general path automatically.

Cold capture independently lowers each eligible frozen source body with the shared body passes and requires equality
with the batch-lowered body: a function whose unused locals or assignments were removed is not admitted. This admission
work runs only when seeding a batch snapshot. A literal edit preserves these read/write facts. Parameters and locals
retain the names assigned by the cold emitter. The private task uses the same statement cleanup, counted-loop
producer, register-limit demotion and locals-table spilling as batch compilation, visiting only its owned function.
The cold spilling producer records the generated table's binding in the frozen frame, including local/global name
collisions; the private task consumes that binding directly. Numeric spill slots and nested initialisations retain
their existing representation. Large scalar functions are eligible without a separate register-budget restriction.

The slice excludes requests with compile-time execution, stack traces, inlining, optimisations, tests or object/import
processing. It introduces no setting and changes no emitted runtime representation. The private task uses the existing
flattening, local-type lowering and Lua function emitter with frozen declaration bindings, and publishes a new artifact
snapshot only after successful generation. Function modification counts detect an uncompiled edit even after an earlier task was cancelled.

`IncrementalScalarBackendTests` compares scripts, manifests and payloads with independent fresh builds. Work counters
at graph copying, flattening and function emission show one lowered/emitted function and zero graph-copy nodes after
a supported edit, including with 1,000 additional live functions. A stock-Lua test applies the payload and calls the
updated function through its unchanged caller. These checks establish the narrow reuse contract, not a speedup on
Castle Fight or Zombie Defense with their normal compile-time/stack-trace options.

The source/frontend eligibility scans, full-script assembly/write, ABI hashing and payload comparison are still
whole-program work. The remaining design and acceptance gates are in [INCREMENTAL_BACKEND.md](INCREMENTAL_BACKEND.md).

### Ordinary functions after fresh compile-time execution

A second slice uses the generator's owned graph snapshots and symbolic operands. It runs after this request's fresh
compile-time evaluation and object injection. Eligible functions use primitive/void signatures, primitive locals or
arrays, ordinary calls, global storage, array access, branches and loops. Ordinary annotations and preserved names
retain the cold emitter's bindings. Changing a literal can lower and emit only its owning function; unchanged callers
continue to call the replaced global function without being regenerated. Cold admission requires exact lowered-body
and emitted-definition agreement with the batch compiler.

The slice validates declaration metadata, non-admitted bodies, admitted body shapes and fresh compile-time products
before reuse. Cache hits freeze/materialise only changed admitted bodies; unchanged snapshots remain detached.
Entry points, package initialisers and compile-time replay metadata retain their batch-owned manifest products.
A frontend rebuild invalidates it. Calls must retain their targets and ABI; class operations, generic
bodies, varargs, helper-dependent arithmetic (integer division and modulo), structural edits, stack traces and
optimisations remain on the batch path. A native call requiring a rewritten wrapper is admitted only when cold
lowering leaves the body identical. No new user setting or runtime indirection is introduced.

`IncrementalSymbolBackendTests` proves one runtime function lowered/emitted with 1,000 unrelated live functions,
multiple changed owners, zero-work no-op requests, unchanged caller identity in stock Lua, cancellation/retry and
fresh compile-time execution. Complete Lua, manifests and payloads are compared with independent fresh builds.
Counters distinguish compile-time and runtime lowering. The private full-graph copy for compile-time evaluation,
whole-program receipt validation, output assembly/write, ABI hashing and payload comparison remain measurable work;
this is not completion of the fully incremental design. Real-map probes and timings are recorded in
[MAP_COMMIT_REPLAY.md](MAP_COMMIT_REPLAY.md).

### Adding and editing event registrations

Hot-reload callback identities include their source callable and named bindings for both class closures and native
`code` callbacks. Direct free-function calls and qualified static method calls with stable non-callback arguments use the resolved target and those
arguments as anchors. Handlers registered for distinct event constants are separate identities, so inserting one
and editing another need not shift existing callbacks. Repeated calls with indistinguishable operands still use the
conservative order-group checks below.

Void interface/class callbacks retain the declared void ABI even when their expression body computes a value;
changing such an expression into a guarded block preserves the callback identity and discards only the unused result.

Unique, independent direct callback calls (inline closures or named function references), including qualified static calls such as
`EventListener.add(EVENT_PLAYER_UNIT_CONSTRUCT_START, ...)` and `DamageEvent.addListener(102, ...)`, at the top level of package `init` blocks become source-owned initialization
actions in hot-reload builds. A new action runs once in the live map, in package dependency order, without repeating
existing setup. Each complete statement owns its closure construction and temporaries; the backend does not extract
registration calls from flattened code. Calls that depend on locals outside the statement, computed/mutable arguments,
instance receivers (even constant instances), generic qualifiers/callees and duplicate action identities remain ordinary init code and retain restart warnings. Actions run
against current live state, not a reconstruction of startup state.

Completed actions are recorded by the running script. Repeated/cumulative payloads do not repeat them; a failed action
can be retried, while completed siblings remain recorded. As with other effectful initialization, failure after partial
side effects cannot roll those effects back. Removing an action cannot unregister its old engine effects automatically,
and changing completed setup still warns. Adding captures to old closures still requires a reconstruction/migration;
these changes do not relax that guard.

Package dispatch emitted by the compiler is tracked by its producer, so a new package whose initialization the payload
already applies no longer produces a misleading changed-`main` warning. Actual entry-point edits and changes to the
relative initialization order of existing packages still warn. The action-completion protocol requires a **new game
session after upgrading from an older compiler**, even if the compiler's version label is unchanged. Earlier action-aware compilers
that tracked only free-function calls also require a new session before using qualified static registrations.

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

A static of a new-generic class is storage of its own per specialisation (`ArrayList_nextFreeIndex⟪string⟫`), and so
is its initializer: generic elimination copies the initial value into a unit of its own for each specialisation
(`init_global_ArrayList_nextFreeIndex⟪string⟫`), called right after the source unit and the specialisations found
before it (`ImTranslator.addSpecializedGlobalInitializer`). The values are computed as they were in one unit, once per
specialisation and in the same order, as on Jass, where each specialisation is a copy with its own initializer. So a
first `ArrayList<SomeNewType>` adds a unit which initialises only the new storage, and the specialisations the map has
keep their values; removing the last use of one removes its unit and its storage, and the others keep theirs
(`newSpecializationOfAGenericStaticIsInitializedAlone`, `removedSpecializationOfAGenericStaticKeepsTheOthers`). One
unit used to initialise every specialisation, so each of these edits changed its storage and needed a restart. An
initializer of several values of which one has an effect (an array literal of calls) stays one unit: the program
computes each value for every specialisation in turn, and units would compute all of one first, which a counter shows
(`effectfulGenericArrayInitializerKeepsItsOrder`); a new type argument of such a static still needs a restart.

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

Specializations which share a canonical instance field also share its Lua storage table. The manifest records that
table once, using the original field's identity and type, as its Lua name does. Adding a specialization therefore
cannot give that same table a different owner in the manifest; concrete capture types remain checked by closure
lineage metadata.

The payload compares the running script's manifest with the edited one:

- A new or changed function is defined, and every class-table slot which binds it is bound again. One stored as a
  value anywhere else keeps the body it was taken from, so a change to it is rejected. This includes callback
  adapters already registered by the baseline or an earlier payload.
  Removed code is no reason to restart. A removed function which a running version stored as a value (a callback
  adapter an engine registration holds), and a class-table slot no function of the edit binds any more (the slot of a
  removed closure or class, or one a shared function such as a `destroy` callback no longer binds), keep their old
  bodies for what the map made before: a timer's or listener's closure object, an engine-registered callback. The new
  code cannot reach them, since it calls no removed function and dispatches through no slot nothing binds. The payload
  applies, with a warning per function: `function X was removed, but what the map made before keeps its old body (in
  class-table slot C.s): restart the map to drop it.` A function which survives but leaves a slot is the same case:
  objects of a class which no longer binds that slot run the body the slot held, so a payload which changes such a
  function says so (`function X changed, but ...`); one whose body stays as it was leaves the slot holding that very
  body and warns of nothing. Rebinding the slot to a current implementation warns of nothing either. The session
  preserves slot bindings and stored functions from earlier payloads for these warnings, and for the rejection of a
  change to a function an earlier payload stored.
- A new top-level declaration runs (a new class table, a new field store); a changed one is rejected.
- A new bootstrap or initialiser statement runs; a changed slot binding or class supertype table runs; the initial
  value of a new global runs. Restoring an earlier interface list also updates objects the map already made.
- A changed initial value of a constant which only computes its value runs again (see "Constants"). A changed initial
  value of any other global the map has is skipped with a warning: the map keeps the value it has.
  Removing the initializer has the same warning. For a global added by an earlier payload, its run-once marker in
  the map determines whether that removed initializer actually ran before the map warns.
- Objects and hashtables materialized by `compiletime(...)` are named for their source binding and the path through
  their fields or tuple components. Dependencies take ownership of their returned graphs before an outer expression
  can alias them, so an earlier alias reuses existing objects, including nested objects and their live mutations.
  Independent roots can share an object too, provided its established snapshot owner stays fixed. Moving ownership
  to an earlier root or removing its original owner requires a restart: replaying under a new owner would duplicate
  the object and split its live aliases. The compiler records each materialized reference's source path and actual
  object/hashtable owner, and checks the baseline plus earlier payloads. Cumulative payloads also check ownership
  installed in the map before changing any functions or state; missing earlier deliveries do not cause a false refusal.
  Ownership is reserved before installation work which can fail, and is preserved through pending-delivery recovery.
  The immutable baseline holds startup references; the session saves only references introduced afterwards. A session
  missing required ownership metadata needs a restart rather than guessing at existing aliases.
  Adding a binding creates its snapshot before new source initializers run; existing
  snapshots retain runtime mutations. A changed snapshot warns that a restart is needed. Nested objects and cycles
  retain their references, and cumulative payloads do not replay snapshots already installed. Startup allocator-ID
  bookkeeping contributes to the baseline fingerprint but never executes in a payload; the large split startup
  replay functions are never shipped as reloadable functions.
  Expressions without distinct bindings (or multiple expressions in the same binding) still form an order group;
  changing its count requires a restart, including groups introduced by earlier payloads. Pure compile-time values
  which materialize no objects or handles do not need that restriction.
  If compile-time evaluation reads an object or non-enum handle's integer identity, adding snapshots requires a
  restart: integer fields and container keys may retain those IDs, while the live allocator gives replayed objects
  different ones. This conservative guard applies to the evaluation as a whole; existing snapshots and ordinary
  body edits remain supported.
- Any other changed bootstrap statement (other class data of a class which exists) is rejected, and so is a name which
  now means another declaration or a type, such as a field which holds another class.
- Interface/class closures bound to source variables include those variable names in their class identities in
  hot-reload builds. Adding or reordering distinct named siblings keeps existing closures' identities and captured
  values; their methods can be replaced while old objects remain alive. A closure passed to a builder call keeps its
  identity when later calls in that chain are added or removed; two closures passed to the same chain method remain
  separate by their source order. Nested closures include outer variable
  bindings too. The source owner's scope and structural signature distinguish equal binding names in overloaded
  functions, including packages and enclosing scopes of nested parameter types. A closure in a package's `init` block
  is owned by the package, one in a constructor by the constructor's function. Removing or renaming a closure applies
  with the warning above: objects made from it before keep its old body (`removedClosureKeepsRunningItsOldBodyInTheMap`).
  Identities name the classes a nested class is declared in, so two inner classes of one name in one package (`A.N`,
  `B.N`) are two, and a closure's owner is its function by name as well as signature: two functions of one signature in
  one package used to give their closures one identity, told apart by their order across both, so a closure added to one
  moved the other's (`closuresOfTwoFunctionsOfOneSignatureAreTwoGroups`).
  A dispatch slot (the class-table key a method call goes through) is named after its group's root, the method none of
  the group's others override (an interface's `run`), and that root's key (`LuaDispatchPreparation.rootOf`). Groups of one
  method name used to be numbered in the order of their classes' names (`run`, `run1`, ... `run13` in Castle Fight), so
  an interface or an implementation added whose class sorted before others renamed their slots, and every closure bound
  to them lost its binding and was refused (`addedInterfaceWithAMethodOfTheSameNameKeepsTheOthersSlots`,
  `addedImplementationSortingFirstKeepsTheSlot`). Builds without `-luaHotReload` name groups as before.
  Siblings still sharing an identity (anonymous closures, such as several `doAfter` callbacks in one function, or equal
  binding names within one callable) form an order group: the build names them by their position, so the build stays a
  function of the source alone. Source lines and columns compare as numbers. Editing their bodies keeps the positions, and so does adding one after them: both
  reload. A closure inserted before others gives each of them the next one's name; the payload carries the rest
  (`LuaHotReload.Lineages`). An object holds its class table itself (`__wurst_objectClass[id]`, read by every dispatch)
  and its captured values sit in field stores keyed by its id, so the payload matches the edit's closures with those
  the map has, each closure's lineage, and gives each name of the group the class table and stores of the closure it
  now names, before it binds the slots and writes the class data of each table again. Every object made before keeps
  the closure it was made from and its values; an allocation reads the name, so one made after gets the closure the
  edit has. The map keeps the tables by lineage (`__wurst_hotreload_lineages`), taken from the names each closure began
  with (the started map's, or those of the payload which added it, `History.lineageOrigins`) before any name is given
  another table, so a payload applies whichever earlier ones the map installed.
  The match is by source (`Function.closureSource`, the closure as written), against what the last payload sent
  (`History.groupLayout`) or the started map: a source only one closure has on either side is that closure, wherever it
  is now (so swapped closures keep their bodies); one whose source is new takes the place of one which lost its source
  between the same unchanged ones, as an edited body; any other is new. Within any gap, including after the last
  unchanged closure, an insertion and an edit cannot be told apart from two edits and an append, so that payload is
  refused: made one at a time, each applies, and an edit after an insertion reaches the objects made before either
  (`editAfterAnInsertionReachesObjectsMadeBeforeEither`). A closure whose source is now one another lineage had and lost
  moved, and the map may run either: refused, as is removing one while the others' names change (its old body reads its
  captured values by its old names). Removing the last ones of a group, or all of them, moves no other and applies with
  the warning of a removed function (`removedCallbacksApplyUnlessOthersTakeTheirNames`); a closure added later in the
  names of a removed one is refused unless it is that one back as it was and no other removed callback still reads
  those names. Other declarations which share an identity
  (none of a closure) are still refused when some of them are removed and others remain. What the map runs of each is the baseline's source or one an earlier payload sent;
  the editor does not know which payloads the map installed, so it keeps every one sent (`History.groupShapes`, by
  lineage). In Castle Fight, 1,050 of its 2,712 closures are in such groups.
  The session also keeps every lineage whose emitted body may still read each table name. Original names alone are
  insufficient after a permutation: removing a group and restoring its original layout must not overwrite names
  used by a removed inserted callback. Each remap checks these historical readers, even when restoring original names.
  A closure's captured variables are fields of its class, and their storage, and its binding, is named after that
  class, whose name follows the order (`LuaHotReload.captureOwner`). They used to be numbered among the captures of one
  name, so a closure which started to capture `s` took a later sibling's storage of `s`, whose objects then read 0, and
  one which stopped capturing was refused as a removed closure (`callbackWhichStartsCapturingLeavesItsSiblingsCaptures`,
  `callbackWhichStopsCapturingIsABodyEdit`). A closure made before the reload holds no value of a variable it did not
  capture then: like an object of a class which gains a field, it reads the typed default. The compiler warns when
  a continuing closure lineage first gains each capture: restart the map to reconstruct old objects with captured
  values. The session remembers the captured fields, so resending that capture, or removing and restoring it,
  does not repeat the warning. A newly introduced closure with captures has no older objects and does not warn.
  Old-generic conversion bridges take the implementing class's source-owner name in hot-reload builds too, with a
  length delimiter so underscores in class and method names cannot merge distinct owners. A bare
  `run_wrapper` gave closures from different named functions one erased signature and one order group, so adding or
  removing an unrelated closure could be refused as moving another one's body. Bridges whose anonymous owners
  still share an identity retain the same order-group safety rules. Naming without `-luaHotReload` is unchanged.
  Successive insertions also reserve every table name earlier closure bodies have read, including remapping
  destinations. A new closure gets a fresh table when its name already holds a survivor's table; two callbacks
  must never share a class table just because no lineage originally started at that name.
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
  When an order check is needed, its fixed-width hash keys are packed into one string and read in order with
  `string.gmatch`; no key is shortened or omitted, and no temporary array of the keys is built. For 23,000 keys,
  that loop is 276,048 bytes rather than 368,026 bytes with individually quoted keys (91,978 bytes, about 25%, saved).
  Missing completion positions and initialization batches are checked as before.
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
it, elements of constant arrays initialised before it and the game's constants; it writes only its own global (the
entries of its own array literal); it calls only natives whose result depends on their
arguments alone (not getters of game state such as `GetPlayerTeam`), the backend's operator intrinsics and functions
which follow the same rules. The stack-trace bookkeeping does not count. Every other global
keeps its value, with the warning:

- a variable, which holds the map's state;
- a constant which allocates an object or calls a native with effects: running it again would leak what it made;
- an array constant which code other than its initializer writes (Wurst allows assigning an element of a constant
  array): the manifest lists its storage with the variables, so it keeps its value, and so does every constant which
  reads one of its elements;
- an array constant whose initializer does not write one dimension at constant indices, which an array literal does;
- a constant which reads a variable, or a constant initialised after it (from a package an `initlater` import
  initialises later), which held its default when the map started.

An array literal is tuned like any other constant: an edit which adds, removes or changes entries runs its initializer
again, and the constants computed from its elements follow. Its length is not part of its identity (the type
`string[74]` of a literal with 74 entries is `string[]` there, since a Lua array is a table of any length), while the
type of its entries is: a literal of strings which becomes one of integers is refused. Running the initializer again
writes the new entries into the table the map has, which every function, and any local alias of the table a function
holds while it runs, already reaches. The entries past the new length must read the array's default again, as in a
restarted map: the manifest records how many entries each array literal writes (`Constant.length`), the map records
how many the run it made last wrote (`__wurst_hotreload_constantLengths`, or else the baseline's), and the payload
clears the indices between the two, by index, without iterating the table
(`editedArrayLiteralsReachTheRunningMap`, `arrayConstantHoldsTheEntriesOfEachPayload`).

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
| All slot aliases | `LuaHotReload.collectFunctionValues` finds every place a reloadable function is used as a value. A class-table binding `Class.slot = f` is repeated after the new definition (`Square.Circle_area = Square_Square_area` in the test), for every class which binds it. A callback adapter passes its target to `xpcall` by name on each call and needs nothing. Any other stored value would keep the old body, so a change to such a function is refused by name; it can still be reloaded while it does not change, and its removal applies with a warning. Neither the test programs nor a program over `LinkedList` and `ClosureTimers` from the standard library has one. |
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
   same. The run saves the manifest of the started script in `_build/luaHotReload/baseline-<SHA-256>.json`, named by
   its content and written once, and the state of the session, which names it, in `session.json`. A large map's
   manifest is tens of megabytes, so a reload saves only the state; the language server reads the manifest once.
2. `wurst.hotreload` sends `-hotreload`. For a Lua project, `RunMap` compiles the project, builds the next cumulative
   payload against the saved baseline and writes payload n to `CustomMapData\Wurst\hotreload-<n>.pld`
   (`customMapDataPath` in the configuration overrides the folder). A hot run deletes the files of earlier sessions. JHCR stays the path for Jass maps. A rejected edit fails the request
   with the reasons as its error message; the VS Code extension shows the first line and writes the rest to its
   output. While a hot run is active, the VS Code extension sends it on every save of a Wurst file.

Without the editor, a map built with `-build ... -luaHotReload` (which takes the run arguments of `wurst_run.args`, not
its build-only `+` lines: `-luaHotReload` refuses `-inline` and `-opt`) takes payloads from the compiler's command line,
which is how the in-game test drives it. Setting `-luaHotReload` on a shared line in `wurst_run.args` selects the same
run arguments for a CLI build. Shared `-inline` or `-opt` entries remain explicit choices and are still rejected;
use `+` for release-only options. Start the session and send payloads with:

```
java -cp wurstscript.jar de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession start <build dir> <CustomMapData>
java -cp wurstscript.jar de.peeeq.wurstio.languageserver.requests.LuaHotReloadSession send <build dir> <CustomMapData>
```

Launch integrations use `prepare`, then `activate` after the launcher succeeds or `abort` if it fails, with the same
two folder arguments. Preparation stages the new baseline and moves old payloads aside; it does not replace the
active session. Aborting restores unread payloads and drops the staged baseline. Activation commits the prepared
baseline and discards the old payloads. Neither activation nor abort reads the most recent build's manifest.
Reloads are refused while a launch is prepared, so they cannot put payloads for the old session in the new map's
polling directory. These commands and the language-server launch share the same session transaction helpers.

`start` takes the map of the last build as the running one and deletes the payload files of an earlier session; `send`
writes the next cumulative payload for the last build, or exits with 1 and the reasons. Both read the manifest a build
leaves in `_build/luaHotReload/manifest.json`; the editor's hot reload builds no map and leaves it as it was.

Game side (measured, see below): `main` starts a one-second timer which calls `__wurst_hotreload_poll`. A payload reaches
one player's game, so the poll does nothing unless exactly one user is playing. It counts them itself, as blizzard.j's
`InitBlizzardGlobals` does for `bj_isSinglePlayer`: a map whose `main` skips `InitBlizzard` (Castle Fight's does) never
sets that global, and read no payload while saying nothing. In any other game it prints once that hot reload is off,
and the status says why. Otherwise the poll sets the tooltip of ability `Aamk` to a space, calls `Preloader` on the next payload file, and reads the tooltip back. The
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

The later closure-lineage safety check now refuses phase 10's combined body edit and append: without another
unchanged callback after it, that source is indistinguishable from an insertion followed by body edits. Send those
as two payloads. The table records the historical in-game result, before that check.

### 24 Oct 2026: fourteen payloads, and Castle Fight

The same suite with four more phases, on the compiler with #1553 to #1556. 68 checks, all passed:

| Phase | Edit | In the game |
| --- | --- | --- |
| 11 | a constant nothing read is read; an array literal grows and has an entry changed; a generic class with a static is used with a new type argument | the constant has the value the map gave it at the start (33); the list sums to 64; only the new specialisation's static was initialised (its counter 2), the `int` one kept its 101 |
| 12 | one line of a function of about 1 MB, shaped like a large map's generated object data | the payload of 1,374,082 bytes applied; the map ran the new body 38 s after it asked, 32.9 s of which was the build |
| 13 | an interface with a `run` whose class sorts before another's is added, and a closure is appended in a function which has another's signature | the closures made at the start still dispatch to their own bodies (31 + 32 through the slot, 41 and 42), and the new ones run (51 + 52) |
| 14 | a function whose closure the map stored at the start is removed | the payload applies with the compiler's warning, and the stored closure keeps its old body (42) |

Castle Fight itself, built with `-lua -luaHotReload -dev` (the compiler of #1551) and kept running by an in-game test
of its own: four payloads made from real edits in one game, each applied 0.5 to 2.5 s after its file was written and
printing nothing it could not apply. A function body (652 bytes); two tuned constants in gameplay packages (2,285
bytes; the production function reading one returned the new 2.5); an added `@onHotReload` function (2,940 bytes; it
ran once, and again with the next payload); the first edit undone. From asking to applying took 90 to 143 s, almost
all of it the build, which #1553 made three times faster. The run found that a map whose `main` skips
`InitBlizzard`, as Castle Fight's does, read no payload and said nothing (fixed in #1553); for that run the test set
`bj_isSinglePlayer` itself.

The fourteen phases then ran with the editor side driven through the language server, as on 22 Oct: all 68 checks
passed. A `wurst.hotreload` request took 4.0 to 14.2 s (the first the longest), where the command line's build took 17
to 21 s per phase, and the map applied each payload 5 to 17 s after asking for it. Without the game, the thirteen
payloads the two paths made were byte-identical, and both refused the field-type edit of phase 5 with the same
reasons. That refusal reached the editor as "Internal error.", its reasons only in the stack trace the error carried;
a map command's failure now carries its own text (`BuildDiagnosticsTests`).

Not measured: whether the preload file's Lua section sees the map's globals (it does not need to); multiplayer, which
is refused.

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
against the commit before it (24 Oct 2026). A commit is a whole pull request, so this overstates what one edit meets,
but it ranks what a real project's edits meet. Accepted, as the changes landed:

| Compiler | Accepted |
| --- | --- |
| order groups, reload hooks, payload checks (#1551) | 8 |
| constants first read, array literals, generic specialisations (#1553) | 19 |
| dispatch slots and closure owners named by what they name (#1555) | 21 |
| removed closures, classes and callbacks apply with a warning | 23 |
| closures of an order group matched by lineage; a whole group removed applies | 27 |

The causes in order of the commits they refused: an array literal changing length (14 commits, fixed: its length is
no part of its identity), a constant starting or stopping being read (7, fixed: a hot-reload build keeps every global's
storage), a closure, class or callback removed (7, now a warning), slots renamed by an added interface or
implementation (the lost bindings of 5, fixed), and a generic class's new type argument (5, fixed). The 7 refused with
#1556 were taken as closures inserted before others; matched by lineage (the last row, measured on the same 31 builds),
they are: 2 real insertions (an event listener added above four others; three spell closures added above another,
in a commit which also removes a whole group), both applied now; 2 closures whose lines crossed 99 to 100, which reorders
a group because classes are ordered by their source position as text (`ImTranslator.getSortKey`), both applied now and, once that order
is numeric, no move at all; 1 group which loses a closure and changes another between the same unchanged ones
(ambiguous, refused); 1 change of a global's type (`boolean` to `Signal<boolean>`), which needs a restart; and 1 field
store of a new-generic specialisation renamed when another specialisation of the class was added
(`ArrayList_specialized_TickCandidates` taking the store names of `..._UnitStat`), which is not a closure at all.

The accepted payloads are 19 KB to 1 MB: Castle Fight's generated object data is one function each of 131 to 167 KB
(`registerUnitObjectMetadata`, `registerUnitStats`), so an edit of a tooltip or a stat carries the whole function. The
game delivered and applied a payload of 1,374,082 bytes (the in-game suite's twelfth payload). Before the temporaries
were named within their function (#1552), one edit of 5 files shipped 621 functions, 550 of them for renamed
temporaries.

## Next stages

1. A closure inserted before others of its order group reloads (not yet measured in the game). What an order group
   still refuses is in BACKLOG.md ("Lua hot reload: what an order group still refuses").
2. Multiplayer through a synchronised channel, only with measurements of its ordering guarantees.
