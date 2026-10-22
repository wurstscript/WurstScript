"""Run unchanged compiler output, rotating old/new order; no hand-written collection operations."""
import argparse
import csv
import hashlib
import platform
from pathlib import Path
import statistics
import subprocess

p = argparse.ArgumentParser()
p.add_argument('--lua', required=True)
p.add_argument('--compiler-root', type=Path, default=Path('de.peeeq.wurstscript'))
p.add_argument('--output', type=Path, required=True)
p.add_argument('--rounds', type=int, default=7)
args = p.parse_args()
root = args.compiler_root.resolve()
lua = str(Path(args.lua).resolve())
variants = {
    'arena': root / 'test-output/lua/LuaArrayListBenchmark_arena.lua',
    'native': root / 'test-output/lua/LuaArrayListBenchmark_nativeTables.lua'
}
scripts = {variant: script.read_text(encoding='utf-8') for variant, script in variants.items()}
assert 'nextFreeIndex' in scripts['arena'], 'Arena benchmark did not contain the real arena implementation'
assert '__wurst_resetArray' in scripts['native'], 'Native benchmark did not contain buffer recycling'
for arena_state in ['nextFreeIndex', 'freeSection', 'startIndex']:
    assert arena_state not in scripts['native'], f'Native benchmark retained {arena_state}'
assert scripts['arena'] != scripts['native'], 'Both variants accidentally compiled the same source'
driver = Path(__file__).with_name('generated-arraylist.lua').resolve()
rows = []
for gc in ['running', 'stopped']:
    for round in range(1, args.rounds + 1):
        order = list(variants) if round % 2 else list(reversed(variants))
        for variant in order:
            script = variants[variant]
            run = subprocess.run([lua, str(driver), str(script), str(round), gc], cwd=root,
                                 capture_output=True, text=True)
            if run.returncode:
                raise RuntimeError(f'{variant} exited {run.returncode}:\n{run.stdout}\n{run.stderr}')
            samples = [line.removeprefix('SAMPLE,') for line in run.stdout.splitlines() if line.startswith('SAMPLE,')]
            if len(samples) != 5:
                raise RuntimeError(f'Expected five generated workload results: {run.stdout}\n{run.stderr}')
            digest = hashlib.sha256(script.read_bytes()).hexdigest()
            for sample in csv.reader(samples):
                if variant == 'native' and gc == 'stopped' and float(sample[4]) > 8:
                    raise RuntimeError(f'Native buffers allocated more than 8 KiB after warm-up: {sample}')
                rows.append([variant, *sample, digest])
            print(f'{gc} round {round}: {variant}', flush=True)
args.output.parent.mkdir(parents=True, exist_ok=True)
with args.output.open('w', newline='', encoding='utf-8') as out:
    writer = csv.writer(out)
    writer.writerow(['variant', 'round', 'gc', 'workload', 'seconds', 'heap_delta_kib', 'checksum',
                     'iterations', 'width', 'generated_sha256'])
    writer.writerows(rows)
print(platform.platform())
print(subprocess.run([lua, '-v'], capture_output=True, text=True, check=True).stdout.strip())
for gc in ['running', 'stopped']:
    for workload in sorted({row[3] for row in rows}):
        groups = {v: [r for r in rows if r[0] == v and r[2] == gc and r[3] == workload] for v in variants}
        medians = {v: statistics.median(float(r[4]) for r in group) for v, group in groups.items()}
        by_round = {v: {r[1]: float(r[4]) for r in group} for v, group in groups.items()}
        ratios = [by_round['arena'][round] / by_round['native'][round] for round in by_round['arena']]
        heap = max(float(r[5]) for r in groups['native'])
        print(f'{gc:7} {workload:20} arena={medians["arena"]:.3f}s native={medians["native"]:.3f}s '
              f'paired_ratio={statistics.median(ratios):.2f}x range={min(ratios):.2f}..{max(ratios):.2f} '
              f'native_max_heap_delta={heap:.3f} KiB')
