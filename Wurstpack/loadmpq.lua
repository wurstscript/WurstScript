-- Use loadmpq(priority,mappath) to load mpqs
-- e.g. loadmpq(10,"C:\\War3\\Maps\\hello\\bar.w3x")
if grim.getmodulehandle("worldedit.exe") ~= 0 or grim.getmodulehandle("worldedit121.exe") then
	-- loads english window titles
	loadmpq(16,"windows.mpq")

	if (grim.getregpair("HKEY_CURRENT_USER\\Software\\Grimoire\\","Mute editor sounds") == "on") then
		loadmpq(16,"nosound.mpq")
	end

	-- loads WEU if user wants that
	weukey = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\WE Unlimited_is1"
	weuval = "InstallLocation"
	weupath = grim.getregpair(weukey,weuval)
	haveweu = grim.exists(weupath .. "WE Unlimited.exe")
	if haveweu then
		if (grim.getregpair("HKEY_CURRENT_USER\\Software\\Grimoire\\","Integrate WEU") == "on") then
   		loadmpq(14,weupath .. "WE Unlimited.exe")
   	end
  end
  
	-- load UMSWE if enabled
	if grim.exists("umswe\\umswe.mpq") and grim.exists("grimext\\grimex.dll") and grim.getregpair("Software\\Grimoire\\","Enable UMSWE") == "on" then
		loadmpq(15,"umswe\\umswe.mpq")
	end
end
