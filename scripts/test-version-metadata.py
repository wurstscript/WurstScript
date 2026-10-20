"""Exercise the production Gradle metadata task in an isolated tagged Git history."""
from pathlib import Path
import os
import re
import subprocess
import tempfile

root = Path(__file__).resolve().parent.parent
build = (root / "de.peeeq.wurstscript/build.gradle").read_text()
version_config = build[build.index("version = providers.fileContents"):build.index("\njava {")]
start = build.index("tasks.register('versionInfoFile')")
end = build.index("/** -------- Aggregate generation", start)
metadata_task = build[start:end]
wrapper = root / "de.peeeq.wurstscript" / ("gradlew.bat" if os.name == "nt" else "gradlew")

with tempfile.TemporaryDirectory(prefix="wurst-version-metadata-") as temp:
    repository = Path(temp)
    project = repository / "de.peeeq.wurstscript"
    project.mkdir()
    (project / "settings.gradle").write_text("rootProject.name = 'version-metadata-test'\n")
    (project / "build.gradle").write_text("def genDir = file('src-gen')\n" + version_config + metadata_task)
    (project / "version.txt").write_text("2.0.0\n")

    def git(*args):
        return subprocess.check_output(["git", "-C", str(repository), *args], text=True, stderr=subprocess.STDOUT).strip()

    git("init", "--initial-branch=master")
    git("config", "user.name", "Version test")
    git("config", "user.email", "version-test@example.invalid")
    git("add", ".")
    git("commit", "-m", "Version baseline")
    git("tag", "nightly")

    def generated_version():
        subprocess.run([str(wrapper), "versionInfoFile", "--console=plain", "--configuration-cache"], cwd=project, check=True)
        content = (project / "src-gen/de/peeeq/wurstscript/CompileTimeInfo.java").read_text()
        return re.search(r'public static final String version="([^"]+)"', content).group(1)

    development = f"2.0.0-dev+{git('rev-parse', '--short=8', 'HEAD')}"
    assert generated_version() == development, "nightly must not identify a stable compiler"
    git("tag", "-a", "v2.0.0", "-m", "Release baseline")
    assert generated_version() == "2.0.0", "the exact release tag must emit its semantic version"
    git("commit", "--allow-empty", "-m", "Unreleased work")
    assert generated_version() == f"2.0.0-dev+{git('rev-parse', '--short=8', 'HEAD')}"

print("Development, exact release, and post-release metadata checks passed.")
