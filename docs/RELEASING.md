# Compiler releases

Master is the development branch. Merging a pull request runs CI but does not publish or move a release tag. The extension installs published semantic-version releases, and users can select an older compiler through **Wurst: Choose Compiler Version**.

The release baseline is **2.0.0**, stored in `de.peeeq.wurstscript/version.txt`. Use three numeric components without a `v` prefix. Increment the patch for compatible fixes, the minor for compatible features, and the major for breaking compiler/language changes. Choose the version as part of release preparation; master can contain unreleased work throughout that interval.

## Publish a release

1. Merge the intended changes and version bump to master. Review the release scope and CI results.
2. Fetch master and tags, then check out the intended master commit. A release can use an older master commit when newer work is not ready.
3. Create an annotated tag matching that commit's version file, for example `git tag -a v2.0.0 -m "WurstScript 2.0.0"`.
4. Push that one tag: `git push origin v2.0.0`.
5. Watch **Release versioned compiler**. It validates the tag, version file, and master ancestry, runs tests and packages Windows x64, Linux x64, macOS x64, and macOS arm64. Publication waits for every platform to pass.

The workflow assembles the complete assets in a draft release, uploads checksums, then publishes it. A published version is never overwritten. For a failed build, rerun the failed workflow; for a defect in a published compiler, bump the version and publish a new tag. Do not move an existing release tag.

## Version metadata

At the exact `v2.0.0` tag, `wurstscript -version` prints `2.0.0`. An untagged build prints `2.0.0-dev+<commit>`, so a development compiler cannot be mistaken for a released compiler. Full and abbreviated commit metadata remain available separately in `CompileTimeInfo`.

Each release has `wurst-compiler-VERSION-PLATFORM.zip` plus `.sha256` assets. The archive keeps the runtime, compiler, and launcher layout used by the extension.

## Transition from nightly

The old nightly release remains available for existing installations, but master no longer updates it. Merge the compiler release workflow, publish the first `v2.0.0` release, then ship the extension's release-selection changes. Before the first versioned release exists, that extension has no stable compiler to install and reports that clearly.

Automatic checks offer newer released versions when following latest stable. Explicit selection supports upgrading or downgrading and pins that version until the user chooses **Follow latest stable**. Version changes retain the existing staged installation and rollback behavior. Source builds remain identifiable as development versions; no published release is created by this migration PR itself.

## Local checks

Run `python scripts/test-release.py` for the release gate's Git-history regression tests, and `python scripts/test-version-metadata.py` to exercise the production Gradle metadata task against nightly, exact release, and post-release commits. From `de.peeeq.wurstscript`, run `./gradlew test --tests de.peeeq.wurstio.MainTests shadowJar` to check version output and build the compiler. The four-platform release workflow supplies the release packaging checks.
