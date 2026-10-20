"""Validate the versioned tag before any release build or publication."""
from pathlib import Path
import re
import subprocess
import sys


def validate_release(repository, ref):
    match = re.fullmatch(r"refs/tags/v((?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*))", ref)
    if not match:
        raise ValueError("Releases require a vMAJOR.MINOR.PATCH tag; branches and nightly tags cannot publish.")
    version = match.group(1)
    repository = Path(repository)
    if (repository / "de.peeeq.wurstscript/version.txt").read_text().strip() != version:
        raise ValueError("The tag must match de.peeeq.wurstscript/version.txt at the tagged commit.")

    def git(*args):
        return subprocess.check_output(["git", "-C", str(repository), *args], text=True).strip()

    head = git("rev-parse", "HEAD")
    if git("rev-parse", f"{ref}^{{commit}}") != head:
        raise ValueError("The release tag must point to the current checkout.")
    ancestor = subprocess.run(["git", "-C", str(repository), "merge-base", "--is-ancestor", head, "origin/master"], check=False)
    if ancestor.returncode != 0:
        raise ValueError("Release tags must point to commits on origin/master.")
    return version


if __name__ == "__main__":
    try:
        print(validate_release(Path(__file__).resolve().parent.parent, sys.argv[1]))
    except (ValueError, OSError, subprocess.CalledProcessError, IndexError) as error:
        print(f"Release validation failed: {error}", file=sys.stderr)
        sys.exit(1)
