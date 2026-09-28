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

-- String natives. The game counts and indexes bytes, and a Lua string is a
-- byte array, so these are the plain Lua operations. SubString takes a
-- 0-based start and an exclusive end; string.sub is 1-based and inclusive.
function StringLength(s)
    return #s
end

function SubString(s, start, stop)
    return string.sub(s, start + 1, stop)
end

function I2S(i) return tostring(math.floor(i)) end
function S2I(s) return math.floor(tonumber(s) or 0) end

-- StringHash, over bytes, as the game and the interpreter both compute it. Bob Jenkins'
-- lookup2, with the same normalisation: ascii letters upper-cased and a forward slash read as
-- a backslash. Kept in step with Wc3StringHash on the Java side.
local function mix(a, b, c)
    local M = 0xFFFFFFFF
    a = (a - b - c) & M; a = a ~ (c >> 13)
    b = (b - c - a) & M; b = b ~ ((a << 8) & M)
    c = (c - a - b) & M; c = c ~ (b >> 13)
    a = (a - b - c) & M; a = a ~ (c >> 12)
    b = (b - c - a) & M; b = b ~ ((a << 16) & M)
    c = (c - a - b) & M; c = c ~ (b >> 5)
    a = (a - b - c) & M; a = a ~ (c >> 3)
    b = (b - c - a) & M; b = b ~ ((a << 10) & M)
    c = (c - a - b) & M; c = c ~ (b >> 15)
    return a, b, c
end

function StringHash(s)
    if s == nil or #s == 0 then
        return 0
    end
    local bytes = {}
    for i = 1, #s do
        local v = string.byte(s, i)
        if v >= 97 and v <= 122 then
            v = v - 32
        elseif v == 47 then
            v = 92
        end
        bytes[i] = v
    end
    local a, b, c = 0x9e3779b9, 0x9e3779b9, 0
    local len = #bytes
    local i = 1
    local M = 0xFFFFFFFF
    while len >= 12 do
        a = (a + bytes[i] + (bytes[i+1] << 8) + (bytes[i+2] << 16) + (bytes[i+3] << 24)) & M
        b = (b + bytes[i+4] + (bytes[i+5] << 8) + (bytes[i+6] << 16) + (bytes[i+7] << 24)) & M
        c = (c + bytes[i+8] + (bytes[i+9] << 8) + (bytes[i+10] << 16) + (bytes[i+11] << 24)) & M
        a, b, c = mix(a, b, c)
        i = i + 12
        len = len - 12
    end
    c = (c + #bytes) & M
    -- The low byte of c holds the length, so the tail starts at the second.
    if len >= 11 then c = (c + (bytes[i+10] << 24)) & M end
    if len >= 10 then c = (c + (bytes[i+9] << 16)) & M end
    if len >= 9 then c = (c + (bytes[i+8] << 8)) & M end
    if len >= 8 then b = (b + (bytes[i+7] << 24)) & M end
    if len >= 7 then b = (b + (bytes[i+6] << 16)) & M end
    if len >= 6 then b = (b + (bytes[i+5] << 8)) & M end
    if len >= 5 then b = (b + bytes[i+4]) & M end
    if len >= 4 then a = (a + (bytes[i+3] << 24)) & M end
    if len >= 3 then a = (a + (bytes[i+2] << 16)) & M end
    if len >= 2 then a = (a + (bytes[i+1] << 8)) & M end
    if len >= 1 then a = (a + bytes[i]) & M end
    a, b, c = mix(a, b, c)
    -- The game returns a signed 32 bit integer.
    if c >= 0x80000000 then
        c = c - 0x100000000
    end
    return c
end

-- Only ascii letters change case: the bytes of a multibyte character are not letters, and
-- folding one rewrites the character. Same rule as the interpreter's StringCase.
function StringCase(s, upperCase)
    local out = {}
    for i = 1, #s do
        local v = string.byte(s, i)
        if upperCase and v >= 97 and v <= 122 then
            v = v - 32
        elseif not upperCase and v >= 65 and v <= 90 then
            v = v + 32
        end
        out[i] = string.char(v)
    end
    return table.concat(out)
end

-- Locations are a plain pair; nothing in a test reads terrain from one.
function Location(x, y) return { x = x, y = y } end
function GetLocationX(loc) return loc.x end
function GetLocationY(loc) return loc.y end
function MoveLocation(loc, x, y) loc.x = x loc.y = y end
function RemoveLocation(loc) end

-- Timers hold what they were started with and never fire: a test drives its own program rather
-- than waiting on game time, and a package which starts a timer at init only needs the call to
-- succeed.
function TimerStart(t, timeout, periodic, handler)
    t.timeout = timeout
    t.periodic = periodic
    t.handler = handler
end
function TimerGetElapsed(t) return 0.0 end
function TimerGetRemaining(t) return t.timeout or 0.0 end
function TimerGetTimeout(t) return t.timeout or 0.0 end
function PauseTimer(t) end
function ResumeTimer(t) end
function DestroyTimer(t) end

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
