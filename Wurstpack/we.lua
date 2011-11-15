dofile("findpath.lua")

if path==0 or path=="" then
	path = "."
end
exe = path.."\\worldedit.exe"

if not grim.exists(exe) then
	grim.messagebox("Could not find worldeditor at "..exe.."\nTry setting path to warcraft folder in HKEY_CURRENT_USER\\Software\\Blizzard Entertainment\\Warcraft III\\InstallPath or HKEY_CURRENT_USER\\Software\\Grimoire\\War3InstallPath","we.lua",true)
	return
end

-- put 1.21 world editor exe in place replacing possible newer versions
exe = path.."\\worldedit121.exe"
if not grim.exists(exe) then
  if not grim.copyfile("bin\\worldedit.exe",exe) then
    grim.messagebox("An instance of World Editor seems to be running already.","we.lua")
    return
  end
end

--proc = grim.startexe(exe,"worldedit.exe "..grim.getargs())
proc = grim.startinject1(exe,"worldedit.exe "..grim.getargs(),"bin\\weload.dll")
if proc == 0 then
	grim.messagebox("Couldn't start World Editor","we.lua")
	return
end

--dofile("we.conf.lua")

if not grim.exestarted(proc) then
	grim.messagebox("Couldn't start World Editor. An instance of World Editor may already be running.\nIf running a patch prior to 1.21b make sure the Warcraft III CD is in your drive and try again.\nIf you are using Kaspersky antivirus, turn it off before running NewGen.","we.lua")
end
