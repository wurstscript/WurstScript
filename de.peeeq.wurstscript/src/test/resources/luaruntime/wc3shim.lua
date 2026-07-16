-- Minimal native shim so common.j.lua and blizzard.j.lua (the Reforged Lua
-- dumps from github.com/wurstscript/jass-history) can be loaded in a plain
-- Lua 5.3 interpreter for test execution.
--
-- Load order in tests: wc3shim.lua -> common.j.lua -> blizzard.j.lua ->
-- <generated wurst script>. The generated script defines its own fallbacks
-- for natives that are still undefined afterwards (guarded "if X then").

-- type declarations carry no runtime information here
TypeDefine = function() end

-- Enum-like handles (playercolor, race, attacktype, ...): the game returns
-- the same handle for the same integer, so cache by (kind, value) to keep
-- handle identity comparisons working.
local enumHandleCache = {}
local function enumHandle(kind, value)
    local key = kind .. "#" .. tostring(value)
    local h = enumHandleCache[key]
    if h == nil then
        h = { handleKind = kind, value = value }
        enumHandleCache[key] = h
    end
    return h
end

-- common.j.lua calls ~80 distinct Convert* factories at load time; create
-- them on demand instead of hardcoding the list, so newer dumps keep working.
setmetatable(_G, {
    __index = function(t, k)
        if type(k) == "string" and string.match(k, "^Convert%u") then
            local f = function(i) return enumHandle(k, i) end
            rawset(t, k, f)
            return f
        end
        return nil
    end
})

-- Reforged's array helper: a table returning a default value for unset keys.
function __jarray(default)
    return setmetatable({}, { __index = function() return default end })
end

function FourCC(s)
    return ((string.byte(s, 1) * 256 + string.byte(s, 2)) * 256
        + string.byte(s, 3)) * 256 + string.byte(s, 4)
end

-- Reforged player layout: 24 playable slots, neutrals at 24..27, 28 total.
function GetBJMaxPlayers() return 24 end
function GetBJMaxPlayerSlots() return 28 end
function GetBJPlayerNeutralVictim() return 25 end
function GetBJPlayerNeutralExtra() return 26 end
function GetPlayerNeutralAggressive() return 24 end
function GetPlayerNeutralPassive() return 27 end

-- handles created at blizzard.j.lua load time for bj_ globals
function CreateGroup() return { units = {} } end
function CreateTimer() return {} end

-- Player handles: one cached {id = x} table per id — the same shape and
-- cache the generated LuaNatives fallback uses, so GetPlayerId(p) reads p.id
-- and identity comparisons like Player(0) == GetLocalPlayer() hold.
__wurst_test_players = __wurst_test_players or {}
function Player(id)
    local p = __wurst_test_players[id]
    if p == nil then
        p = { id = id }
        __wurst_test_players[id] = p
    end
    return p
end

function GetPlayerId(p)
    return p.id
end

-- text output: route to stdout so error handlers (BJDebugMsg) work in tests
function GetLocalPlayer() return Player(0) end
function DisplayTimedTextToPlayer(toPlayer, x, y, duration, message)
    print(message)
end
function DisplayTextToPlayer(toPlayer, x, y, message)
    print(message)
end
