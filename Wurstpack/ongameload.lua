-- Load libraries here that need to run after game.dll loads but after
-- native registration
-- e.g. war3err, japi, user created natives

if getregpair("Software\\Grimoire\\","Enable war3err") == "on" then
	loaddll("bin\\war3err.dll")
end
if getregpair("Software\\Grimoire\\","Enable warmachine") == "on" then
  loaddll("bin\\warmachine.dll") -- disable war3err.dll to use this
end
if getregpair("Software\\Grimoire\\","Enable japi") == "on" then
  loaddll("bin\\japi.dll")
  loaddll("bin\\nativepack.dll") -- requires japi.dll
end
--loaddll("bin\\warsoc.dll")
