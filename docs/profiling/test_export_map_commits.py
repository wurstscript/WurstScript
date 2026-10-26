"""Standalone exporter regression checks: python -m unittest discover -s docs/profiling -p 'test_*.py'."""
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


EXPORTER = Path(__file__).with_name('export-map-commits.py').resolve()


class ExportMapCommitsTests(unittest.TestCase):
    def test_short_history_exports_valid_pairs_without_replaying_the_root(self):
        self.export_short_history(".wurst")

    def test_jurst_only_edit_is_exported_and_classified_as_runtime_source(self):
        self.export_short_history(".jurst")

    def export_short_history(self, extension):
        workspace = tempfile.TemporaryDirectory(prefix='wurst-commit-export-')
        root = Path(workspace.name).resolve()
        try:
            repository = root / 'repository'
            repository.mkdir()

            def git(*args):
                return subprocess.check_output(['git', '-C', str(repository), *args], stderr=subprocess.STDOUT).decode('utf-8').strip()

            git('-c', 'init.templateDir=', 'init')
            git('config', 'user.email', 'export-test@example.invalid')
            git('config', 'user.name', 'Exporter test')
            git('config', 'commit.gpgsign', 'false')
            hooks = root / 'empty-hooks'
            hooks.mkdir()
            git('config', 'core.hooksPath', str(hooks))
            (repository / 'wurst').mkdir()
            source = repository / ('wurst/Runtime' + extension)
            source.write_text('package Test\nfunction value() returns int\n    return 1\n', encoding='utf-8')
            (repository / 'wurst.build').write_text('projectName: Fixture\n', encoding='utf-8')
            (repository / 'wurst_run.args').write_text('-lua\n', encoding='utf-8')
            git('add', '.')
            git('commit', '-m', 'Initial Wurst source')
            parent = git('rev-parse', 'HEAD')
            source.write_text(source.read_text(encoding='utf-8').replace('return 1', 'return 2'), encoding='utf-8')
            git('commit', '-am', 'Body edit')
            child = git('rev-parse', 'HEAD')
            (repository / '_build/dependencies').mkdir(parents=True)
            template = root / 'template'
            (template / 'raw').mkdir(parents=True)
            (template / 'base.w3x').write_bytes(b'export fixture, not a runnable map')
            output = root / 'exported'
            result = subprocess.run([sys.executable, str(EXPORTER), '--repository', str(repository), '--ref', 'HEAD',
                                     '--template', str(template), '--output', str(output)], capture_output=True, text=True,
                                    encoding='utf-8')
            self.assertEqual(0, result.returncode, result.stdout + result.stderr)
            corpus = json.loads((output / 'corpus.json').read_text(encoding='utf-8'))
            self.assertEqual([(parent, child)], [(p['parent'], p['commit']) for p in corpus['pairs']])
            self.assertEqual('Body edit', corpus['pairs'][0]['title'])
            self.assertEqual(['wurst/Runtime' + extension], corpus['pairs'][0]['runtimePaths'])
            self.assertTrue(corpus['pairs'][0]['production'])
            self.assertIn('return 1', (output / 'states' / parent / ('wurst/Runtime' + extension)).read_text(encoding='utf-8'))
            self.assertIn('return 2', (output / 'states' / child / ('wurst/Runtime' + extension)).read_text(encoding='utf-8'))
        finally:
            # Verify the absolute recursive-cleanup target remains the temporary workspace we created.
            if root.parent != Path(tempfile.gettempdir()).resolve() or not root.name.startswith('wurst-commit-export-'):
                raise AssertionError('Unexpected cleanup target: ' + str(root))
            workspace.cleanup()


if __name__ == '__main__':
    unittest.main()
