-- DLLs that need to be injected into the worldeditor should go here

loaddll("bin\\wehack.dll")
loaddll("bin\\japi.dll")
loaddll("bin\\nativepack.dll")
--loaddll("bin\\warsoc.dll")
--loaddll("bin\\listfile.dll")
loaddll("bin\\loadmpq.dll")

if exists("tesh\\tesh.dll") and getregpair("Software\\Grimoire\\","Enable TESH") ~= "off" then
	loaddll("tesh\\tesh.dll")
end
if isdotnetinstalled() and getregpair("Software\\Grimoire\\","Enable Colorizer") ~= "off"  then
	loaddll("bin\\PELoader.dll")
end
