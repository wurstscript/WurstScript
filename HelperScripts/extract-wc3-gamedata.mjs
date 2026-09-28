import { mkdir, writeFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { CascStorage, closeAllSegments } from '../../casc-ts/dist/index.js';

const installDir = process.argv[2] ?? 'C:/Program Files (x86)/Warcraft III';
const scriptDir = dirname(fileURLToPath(import.meta.url));
const outputDir = join(scriptDir, 'gamedata');

const requiredFiles = `
abilitybuffdata.slk
abilitybuffmetadata.slk
abilitydata.slk
abilitymetadata.slk
abilityskin.txt
campaignabilityfunc.txt
campaignunitfunc.txt
campaignupgradefunc.txt
commandfunc.txt
commonabilityfunc.txt
destructableaddons.txt
destructabledata.slk
destructablemetadata.slk
destructableskin.txt
humanabilityfunc.txt
humanunitfunc.txt
humanupgradefunc.txt
itemabilityfunc.txt
itemaddons.txt
itemdata.slk
itemfunc.txt
itemskin.txt
miscdata.txt
miscgame.txt
miscmetadata.slk
neutralabilityfunc.txt
neutralunitfunc.txt
neutralupgradefunc.txt
nightelfabilityfunc.txt
nightelfunitfunc.txt
nightelfupgradefunc.txt
orcabilityfunc.txt
orcunitfunc.txt
orcupgradefunc.txt
undeadabilityfunc.txt
undeadunitfunc.txt
undeadupgradefunc.txt
unitabilities.slk
unitaddons.txt
unitbalance.slk
unitdata.slk
unitmetadata.slk
unitskin.txt
unitui.slk
unitweapons.slk
unitweaponsfunc.txt
unitweaponsskin.txt
upgradedata.slk
upgradeeffectmetadata.slk
upgrademetadata.slk
upgradeskin.txt
WorldEditStrings.txt
`.trim().split(/\r?\n/);
const preferredPaths = new Map([
  ['miscdata.txt', 'war3.w3mod:units/miscdata.txt'],
]);

const storage = await CascStorage.openAsync(installDir);
try {
  // Expand the base data archive and the enUS locale archive before resolving paths.
  await storage.readFileAsync('war3.w3mod');
  await storage.readFileAsync('war3.w3mod:_locales/enus.w3mod');
  const paths = storage.listFiles();
  const unresolved = requiredFiles.flatMap(name => {
    const suffix = `/${name}`.toLowerCase();
    const matches = paths.filter(path => path.toLowerCase().endsWith(suffix));
    const preferred = preferredPaths.get(name);
    return matches.length === 1 || (preferred && matches.includes(preferred))
      ? []
      : [`${name}: ${matches.join(', ') || '(missing)'}`];
  });
  if (unresolved.length) {
    throw new Error(`Could not uniquely resolve required game data files:\n${unresolved.join('\n')}`);
  }

  await mkdir(outputDir, { recursive: true });
  for (const name of requiredFiles) {
    const suffix = `/${name}`.toLowerCase();
    const matches = paths.filter(path => path.toLowerCase().endsWith(suffix));
    const path = preferredPaths.get(name) ?? matches[0];
    if (matches.length !== 1 && !preferredPaths.has(name)) {
      throw new Error(`Expected one CASC path for ${name}, found ${matches.length}: ${matches.join(', ')}`);
    }
    const data = await storage.readFileAsync(path);
    await writeFile(join(outputDir, name), data);
    console.log(`${name} <- ${path}`);
  }
} finally {
  await closeAllSegments();
}
