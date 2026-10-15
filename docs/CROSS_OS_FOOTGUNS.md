# Cross-OS footguns

PR CI intentionally runs Ubuntu only. Master runs compiler tests on Windows x64, Linux x64, macOS arm64 and
macOS x64; the versioned release workflow also packages those hosts. Keep that inexpensive PR loop. Agents own
portability in both production code and tests: inspect the affected host boundaries, construct a focused regression
that exposes the assumption, run the relevant test class, and state any platform-specific validation still missing.
One passing host is not a completed portability review.

## File identity is not path spelling

[The object-export master failure](https://github.com/wurstscript/WurstScript/actions/runs/38700366418)
was a test assertion, not a failed export. Windows created a temp path under `C:\Users\RUNNER~1`; `WFile.create`
canonicalised it to `C:\Users\runneradmin`. `File.equals` compares the path spelling, so it rejected the correct
output file. Content assertions had already passed.

Use `Files.isSameFile(actual, expected)` for existing files. For a contract about canonical paths, canonicalise both
sides explicitly. `toAbsolutePath()` and `normalize()` do not resolve short names or symbolic links. Include `./`
in an expected path to make a spelling-dependent identity assertion fail on every host, without requiring Windows
short-name support or privileges to create symlinks. Keep assertions about exact user-visible spelling separate
from assertions about which file was written.

Build paths with `Path.resolve` or `File` constructors. Check case sensitivity, spaces and non-ASCII characters
when filenames enter a parser or subprocess. Close files and archives before replacing or deleting them: a successful
unlink on Unix does not establish that Windows can replace an open file.

## A compile-only test must never launch installation discovery

[The map-model fixture fix](https://github.com/wurstscript/WurstScript/pull/1461) prevents tests from opening
Warcraft folder choosers on Windows/macOS runners. A runner has no game installation, and a modal chooser can
hang until the job times out. Supply the fixture's installation/discovery boundary explicitly; install throwing
`WinGameExeFinder` and `MacGameExeFinder` services when testing that discovery cannot occur, and restore them in
`finally`. See `MapModelReuseTests.runProbeDoesNotDiscoverAnInstalledGame`. Timeouts bound damage but do not fix
an unintended interactive call.

## Test tools and packaged runtimes differ by host

The [Intel macOS packaging failure](https://github.com/wurstscript/WurstScript/actions/runs/37778085905)
is one example of a host-specific runtime problem. See `deploy.gradle` and AGENTS.md's startup-archive notes:
macOS copies a signed full JDK, other hosts use jlink, and a copied JDK need not contain a base CDS archive.
Generate one when absent and verify the shipped runtime with `-Xshare:on`. Check the actual runtime image, and
clear JVM option environment variables when generating/validating its archive. JDK availability also differs:
the workflows use Zulu for Java 27 on Intel macOS.

The checked-in Linux Lua fallback needs `libreadline.so.6`, absent on current Ubuntu images. CI installs Lua 5.3
instead. Windows uses bundled `.exe` tools; macOS installs Homebrew Lua, whose version is not pinned to 5.3.
Never assume that a command in PATH, a checked-in binary, or a successful Lua run means another host has the same
interpreter, shared libraries, permissions or arithmetic. Inspect `WurstScriptTest` discovery and the runtime notes
before changing these assumptions.

When changing process execution, pass arguments individually and check exit status. Specify text encodings and
handle CRLF at text-protocol boundaries; the release workflow already strips Java's trailing Windows carriage
return before comparing its version. Preserve executable permissions where required, account for `.exe`/`.cmd`,
and reap subprocesses on cancellation. Run a focused packaging check for packaging changes, rather than treating
an Ubuntu compiler test as validation of a macOS or Windows distribution.
