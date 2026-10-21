# Lua test runtime

Enables executing translated Lua output (`test().testLua(true).executeProg()`)
against the real Blizzard.j implementations instead of minimal mocks.

- `common.j.lua`, `blizzard.j.lua`: Reforged Lua dumps from
  <https://github.com/wurstscript/jass-history> (`lua-dump/`).
- `wc3shim.lua`: hand-written shim providing the natives those two files need
  at load time (TypeDefine, Convert* enum-handle factories, `__jarray`,
  FourCC, player-layout constants, text output), the natives the
  standard library's package inits call (timers, forces), and common.j
  natives that executing tests call (string, math and bit natives,
  `GetHandleId`, player state, units, groups, destructables), kept in step with the
  interpreter's mocks. With the standard library the generated script
  defines no fallback for a common.j native, so one missing here fails
  with `attempt to call a nil value`.

Load order in `WurstScriptTest.translateAndTestLua`:
`wc3shim.lua` → `common.j.lua` → `blizzard.j.lua` → generated script → `main()`.

The generated script only installs its own fallback for a native if it is
still undefined after these files are loaded (guarded `if X then ... end`),
so real BJ implementations always win.

Execution additionally requires a Lua 5.3 interpreter. On Windows the
tests take the checked-in `src/test/resources/lua.exe` (+ `lua53.dll`),
then `lua53.exe`/`lua` on the PATH; elsewhere `lua5.3` on the PATH first,
then the checked-in `src/test/resources/lua53`, then `lua53`/`lua` on the
PATH. If none is found, executeProg Lua tests are skipped with a
SkipException.
