"""Export committed Wurst states into a new isolated replay fixture; never change the source checkout."""
import argparse
import hashlib
import io
import json
import re
from pathlib import Path, PurePosixPath
import shutil
import stat
import subprocess
import zipfile


def git(repo, *args):
    return subprocess.check_output(['git', '-C', str(repo), *args])


def changed_paths(repo, commit):
    return git(repo, 'diff', '--name-only', '-z', commit + '^', commit).decode('utf-8').rstrip('\0').split('\0')


def is_test_source(path):
    parts = PurePosixPath(path).parts
    return any(p.lower() in ['tests', 'e2e'] for p in parts) or path.endswith(('Test.wurst', 'Tests.wurst', 'E2E.wurst'))


def tree_digest(root):
    digest = hashlib.sha256()
    count = 0
    for path in sorted(root.rglob('*')):
        if not path.is_file():
            continue
        name = path.relative_to(root).as_posix().encode('utf-8')
        content = path.read_bytes()
        digest.update(len(name).to_bytes(8, 'big'))
        digest.update(name)
        digest.update(len(content).to_bytes(8, 'big'))
        digest.update(content)
        count += 1
    return dict(sha256=digest.hexdigest(), files=count)


def export(repo, revision, target):
    target.mkdir()
    archive = git(repo, 'archive', '--format=zip', revision, '--', 'wurst', 'wurst.build', 'wurst_run.args')
    with zipfile.ZipFile(io.BytesIO(archive)) as files:
        for entry in files.infolist():
            path = PurePosixPath(entry.filename)
            if path.is_absolute() or '..' in path.parts or stat.S_ISLNK(entry.external_attr >> 16):
                raise ValueError(f'Unsafe archive entry: {entry.filename}')
            output = target.joinpath(*path.parts).resolve()
            if not output.is_relative_to(target.resolve()):
                raise ValueError(f'Archive entry escapes destination: {entry.filename}')
            if entry.is_dir():
                output.mkdir(parents=True, exist_ok=True)
            else:
                output.parent.mkdir(parents=True, exist_ok=True)
                output.write_bytes(files.read(entry))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--repository', type=Path, required=True)
    parser.add_argument('--ref', required=True)
    parser.add_argument('--count', type=int, default=12)
    parser.add_argument('--template', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--dependency', action='append', default=[], metavar='NAME=PATH',
                        help='Explicitly override one installed dependency with another local source tree.')
    args = parser.parse_args()
    if args.count <= 0:
        parser.error('--count must be positive')
    overrides = {}
    dependencies = args.repository / '_build/dependencies'
    for specification in args.dependency:
        name, separator, source = specification.partition('=')
        if not separator or not re.fullmatch(r'[A-Za-z0-9_.-]+', name) or name in ['.', '..'] or not (dependencies / name).is_dir():
            parser.error('Unknown installed dependency: ' + specification)
        if name in overrides or not Path(source).is_dir():
            parser.error('Duplicate dependency or missing source directory: ' + specification)
        overrides[name] = Path(source)
    args.output.mkdir(parents=True)  # Refuse to replace an existing fixture.
    candidates = git(args.repository, 'rev-list', '--first-parent', '--min-parents=1', args.ref, '--', 'wurst').decode().splitlines()
    commits = []
    for commit in candidates:
        paths = changed_paths(args.repository, commit)
        if any(p.startswith('wurst/') and p.endswith(('.wurst', '.jurst', '.j')) for p in paths):
            commits.append(commit)
        if len(commits) == args.count:
            break
    commits.reverse()
    if not commits:
        raise ValueError('No Wurst-changing commits found')
    revisions = []
    for commit in commits:
        parent = git(args.repository, 'rev-parse', commit + '^').decode().strip()
        for revision in [parent, commit]:
            if revision not in revisions:
                revisions.append(revision)
    states = args.output / 'states'
    states.mkdir()
    for revision in revisions:
        export(args.repository, revision, states / revision)
    project = args.output / 'project'
    project.mkdir()
    for name in ['base.w3x', 'raw']:
        source = args.template / name
        if source.is_dir():
            shutil.copytree(source, project / name)
        else:
            shutil.copy2(source, project / name)
    (project / '_build/dependencies').mkdir(parents=True)
    for dependency in sorted(dependencies.iterdir()):
        if dependency.is_dir():
            shutil.copytree(overrides.get(dependency.name, dependency), project / '_build/dependencies' / dependency.name,
                            ignore=shutil.ignore_patterns('.git'))
    pairs = []
    for commit in commits:
        parent = git(args.repository, 'rev-parse', commit + '^').decode().strip()
        paths = changed_paths(args.repository, commit)
        sources = [p for p in paths if p.startswith('wurst/') and p.endswith(('.wurst', '.jurst', '.j'))]
        runtime = [p for p in sources if not is_test_source(p)]
        pairs.append(dict(commit=commit, parent=parent, title=git(args.repository, 'show', '-s', '--format=%s', commit).decode().strip(),
                          paths=paths, production=bool(runtime), runtimePaths=runtime,
                          testPaths=[p for p in sources if is_test_source(p)], otherPaths=[p for p in paths if p not in sources]))
    metadata = dict(repository=str(args.repository.resolve()), ref=args.ref, head=git(args.repository, 'rev-parse', args.ref).decode().strip(),
                    baseMapSha256=hashlib.sha256((project/'base.w3x').read_bytes()).hexdigest(),
                    raw=tree_digest(project/'raw'),
                    dependencies={p.name: tree_digest(p) for p in sorted((project/'_build/dependencies').iterdir()) if p.is_dir()},
                    dependencyOverrides={name: str(source.resolve()) for name, source in overrides.items()},
                    environment='Frozen current dependencies, base map and raw assets; not historical dependency/asset replay.',
                    pairs=pairs)
    (args.output / 'corpus.json').write_text(json.dumps(metadata, indent=2), encoding='utf-8')
    print(json.dumps(dict(fixture=str(args.output.resolve()), commits=len(pairs), states=len(revisions))))


if __name__ == '__main__':
    main()
