dofile("findpath.lua")

if path==0 or path=="" then
	path = "."
end
exe = path.."\\war3.exe"

if not grim.exists(exe) then
	grim.messagebox("Could not find warcraft at "..exe.."\nTry setting path to warcraft folder in HKEY_CURRENT_USER\\Software\\Blizzard Entertainment\\Warcraft III\\InstallPath or HKEY_CURRENT_USER\\Software\\Grimoire\\War3InstallPath","war3.lua",true)
	return
end

--proc = grim.startexe(exe,grim.getargs())
proc = grim.startinject1(exe,"war3.exe "..grim.getargs(),"bin\\ongameload.dll")
if proc == 0 then
	grim.messagebox("Couldn't start warcraft III","war3.lua")
	return
end

--dofile("war3.conf.lua")

if not grim.exestarted(proc) then
	grim.messagebox("Couldn't start Warcraft III. An instance of Warcraft III may already be running.\nIf running a patch prior to 1.21b make sure the Warcraft III CD is in your drive and try again.\nIf you are using Kaspersky antivirus, turn it off before running NewGen.","we.lua")
end
