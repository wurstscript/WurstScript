-- Maintainer-run probe for the Lua hot reload delivery path (docs/LUA_HOT_RELOAD.md).
-- Append to the end of a Lua map's war3map.lua; it starts one second after main, in single player.
-- Before starting the map, copy hot-reload-probe.pld (next to this file) to
--   <Documents>\Warcraft III\CustomMapData\Wurst\hotreload-probe.pld
-- Record the exact client version together with every "Wurst probe" line.
-- The compiler never injects this file and the tests never run it.
do
    local CHANNEL = FourCC("Aamk")
    local GAME_FILE = "Wurst\\hotreload-probe-game.pld"
    local TOOL_FILE = "Wurst\\hotreload-probe.pld"
    local TOOL_TEXT = "wurst-hot-reload-payload:wurst hot reload probe: \"quoted\" back\\slash //! marker\nsecond line"

    local function report(name, ok, detail)
        BJDebugMsg("Wurst probe " .. name .. ": " .. tostring(ok) .. " " .. tostring(detail or ""))
    end

    local function check(name, f)
        local ok, detail = pcall(f)
        report(name, ok, detail)
    end

    -- A file written with the Preload natives in the layout the editor writes: a Lua section between user-code markers.
    local function writeGameFile(lua)
        PreloadGenClear()
        PreloadGenStart()
        Preload("\")\nendfunction\n//!beginusercode\n" .. lua .. "\n//!endusercode\nfunction a takes nothing returns nothing\n//")
        PreloadGenEnd(GAME_FILE)
    end

    local function readChannel(level, path)
        BlzSetAbilityTooltip(CHANNEL, "", level)
        Preloader(path)
        return BlzGetAbilityTooltip(CHANNEL, level)
    end

    WurstProbeGlobal = 42

    function WurstHotReloadProbe()
        report("single-player", bj_isSinglePlayer, "ReloadGameCachesFromDisk=" .. tostring(ReloadGameCachesFromDisk()))

        check("load-with-env", function()
            assert(type(load) == "function", "load is unavailable")
            local chunk = assert(load("return function() return WurstProbeGlobal + 1 end", "=probe", "t", _ENV))
            assert(chunk()() == 43, "the loaded body does not see the script's globals")
            assert(load("this is not Lua", "=probe", "t", _ENV) == nil, "a syntax error was not reported")
            return "a loaded body reads the script's globals; a syntax error returns nil"
        end)

        check("tooltip-channel", function()
            local text = string.rep("x", 100000)
            BlzSetAbilityTooltip(CHANNEL, text, 3)
            local back = BlzGetAbilityTooltip(CHANNEL, 3)
            assert(back == text, "the tooltip kept " .. #back .. " of " .. #text .. " characters")
            return "100000 characters kept"
        end)

        check("preloader-runs-user-code", function()
            writeGameFile("BlzSetAbilityTooltip(FourCC(\"Aamk\"), \"user code ran\", 0)")
            local back = readChannel(0, GAME_FILE)
            assert(back == "user code ran", "the tooltip after Preloader is '" .. back .. "'")
            return "a Lua section of a preload file runs"
        end)

        check("preloader-sees-map-globals", function()
            writeGameFile("BlzSetAbilityTooltip(FourCC(\"Aamk\"), tostring(WurstProbeGlobal) .. ' ' .. tostring(load), 0)")
            return "the section reads WurstProbeGlobal and load as: " .. readChannel(0, GAME_FILE)
        end)

        check("preloader-large-section", function()
            local parts = {"local p = {}"}
            for i = 1, 100 do
                parts[#parts + 1] = "p[#p + 1] = \"" .. string.rep("y", 1000) .. "\""
            end
            parts[#parts + 1] = "BlzSetAbilityTooltip(FourCC(\"Aamk\"), table.concat(p), 0)"
            writeGameFile(table.concat(parts, "\n"))
            local back = readChannel(0, GAME_FILE)
            assert(#back == 100000, "received " .. #back .. " of 100000 characters")
            return "100 lines of 1000 characters received"
        end)

        check("tool-written-file", function()
            local back = readChannel(0, TOOL_FILE)
            assert(back == TOOL_TEXT, "received '" .. back .. "'")
            return "the file in the editor's layout hands its text over unchanged"
        end)

        check("apply-in-timer", function()
            function WurstProbeBody() return 1 end
            local done = false
            TimerStart(CreateTimer(), 0.0, false, function()
                local chunk = assert(load("return 2, function() function WurstProbeBody() return 2 end end", "=probe", "t", _ENV))
                local _, install = chunk()
                install()
                done = WurstProbeBody() == 2
                report("apply-in-timer-result", done, "a body replaced in a timer callback runs when called by name")
            end)
            return "scheduled"
        end)
    end

    local originalMain = main
    function main()
        originalMain()
        TimerStart(CreateTimer(), 1.0, false, WurstHotReloadProbe)
    end
end
