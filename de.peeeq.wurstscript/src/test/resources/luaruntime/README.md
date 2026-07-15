# Lua test runtime

Enables executing translated Lua output (`test().testLua(true).executeProg()`)
against the real Blizzard.j implementations instead of minimal mocks.

- `common.j.lua`, `blizzard.j.lua`: Reforged Lua dumps from
  <https://github.com/wurstscript/jass-history> (`lua-dump/`).
- `wc3shim.lua`: hand-written shim providing the natives those two files need
  at load time (TypeDefine, Convert* enum-handle factories, `__jarray`,
  FourCC, player-layout constants, text output).

Load order in `WurstScriptTest.translateAndTestLua`:
`wc3shim.lua` → `common.j.lua` → `blizzard.j.lua` → generated script → `main()`.

The generated script only installs its own fallback for a native if it is
still undefined after these files are loaded (guarded `if X then ... end`),
so real BJ implementations always win.

Execution additionally requires a Lua 5.3 interpreter: either
`src/test/resources/lua53.exe` (+ `lua53.dll`) on Windows /
`src/test/resources/lua53` on Linux (official binaries from
<https://luabinaries.sourceforge.net/>, version 5.3.6), or `lua53`/`lua` on
the PATH. If none is found, executeProg Lua tests are skipped with a
SkipException.
