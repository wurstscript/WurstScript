-- Minimal native shim so common.j.lua and blizzard.j.lua (the Reforged Lua
-- dumps from github.com/wurstscript/jass-history) can be loaded in a plain
-- Lua 5.3 interpreter for test execution.
--
-- Load order in tests: wc3shim.lua -> common.j.lua -> blizzard.j.lua ->
-- <generated wurst script>. The generated script defines its own fallbacks
-- for natives that are still undefined afterwards (guarded "if X then").

-- type declarations carry no runtime information here
TypeDefine = function() end

-- ClosureForGroups creates a filter handle at package initialization.
-- Keep its callback without invoking it, as the native factory does.
function Filter(callback) return { callback = callback } end

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

-- As the interpreter's GetHandleId: a null handle is 0; a converted enum handle is the value
-- it was made from (measured on the 3.0.0 client: GetHandleId(UNIT_STATE_MANA) is 2); any other
-- handle gets the next id from 0x100000, where the game's agent ids start.
local handleIds = {}
local nextHandleId = 0x100000
function GetHandleId(h)
    if h == nil then
        return 0
    end
    if h.handleKind ~= nil then
        return h.value
    end
    local id = handleIds[h]
    if id == nil then
        id = nextHandleId
        nextHandleId = nextHandleId + 1
        handleIds[h] = id
    end
    return id
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

-- As the interpreter's S2I and the game (measured on the 3.0.0 client), C's strtol: leading
-- whitespace is skipped, then a sign and digits are read, and the value saturates at the
-- integer limits. S2I("  -4x") is -4, S2I("2147483648") is 2147483647.
function S2I(s)
    if s == nil then
        return 0
    end
    local sign, digits = string.match(s, "^%s*([%+%-]?)(%d*)")
    local value = 0
    for i = 1, #digits do
        -- stop growing once past the limit
        value = math.min(value * 10 + string.byte(digits, i) - 48, 2147483648)
    end
    if sign == "-" then
        return -value
    end
    return math.min(value, 2147483647)
end

-- As the interpreter's R2SW (measured on the 3.0.0 client): the sign first, then the integer
-- part left-padded to width - precision, then the fraction rounded half up; precision 0 prints
-- ".0". R2SW(1.5, 8, 2) is "     1.50", R2SW(-1.5, 8, 3) is "-    1.500". The test Lua
-- computes in double, the game in float, so the last digits can differ from the game's.
function R2SW(r, width, precision)
    local digits = math.max(0, precision)
    if digits > 18 or width - digits > 1000 then
        -- As the interpreter: what the game prints there was not measured, and padding that far
        -- could exhaust memory.
        error("R2SW called with width " .. width .. " and precision " .. precision
            .. ", beyond what the test runtime models (precision up to 18, width up to 1000).")
    end
    local a = math.abs(r)
    local whole = math.floor(a)
    local limit = 1
    for _ = 1, digits do
        limit = limit * 10
    end
    local fraction = math.floor((a - whole) * limit + 0.5)
    if fraction >= limit then
        whole = whole + 1
        fraction = fraction - limit
    end
    local wholeText = string.format("%.0f", whole)
    local sign = r < 0 and "-" or ""
    local fractionText = digits == 0 and "0" or string.format("%0" .. digits .. "d", fraction)
    return sign .. string.rep(" ", width - digits - #wholeText) .. wholeText .. "." .. fractionText
end

-- Math natives, as the interpreter's MathProvider and IntegerProvider. Measured on the 3.0.0
-- client: SquareRoot of a negative value is 0, not NaN.
function SquareRoot(r)
    if r < 0 then
        return 0.0
    end
    return math.sqrt(r)
end

-- Any value between the bounds; the interpreter's generator also counts up from the first
-- bound when the second is below it. Not measured in game, and the game's sequence is not modelled.
function GetRandomInt(low, high)
    return low + math.random(0, math.abs(high - low))
end

-- As the interpreter (not measured in game): the natives work on 32 bit integers. The test Lua
-- does not wrap integer arithmetic, so the result is brought back to the signed 32 bit range.
local function signed32(v)
    v = v & 0xFFFFFFFF
    if v >= 0x80000000 then
        return v - 0x100000000
    end
    return v
end
function BlzBitOr(x, y) return signed32(x | y) end
function BlzBitAnd(x, y) return signed32(x & y) end
function BlzBitXor(x, y) return signed32(x ~ y) end

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

-- Player handles: one cached {id = x} table per id, the same shape and
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

-- Player states by the id of the state handle, as the interpreter's PlayerProvider. Measured on
-- the 3.0.0 client: a null state is id 0, the game result, which reads 3 while the game runs;
-- gold (1) and lumber (2) are clamped to 0.
local function playerStateId(state)
    if state == nil then
        return 0
    end
    return state.value
end
function SetPlayerState(p, state, value)
    local id = playerStateId(state)
    if id == 1 or id == 2 then
        value = math.max(0, value)
    end
    p.states = p.states or {}
    p.states[id] = value
end
function GetPlayerState(p, state)
    local id = playerStateId(state)
    local value = p.states and p.states[id]
    if value == nil then
        return id == 0 and 3 or 0
    end
    return value
end

-- As the interpreter's mock, unset reads 0; not measured in game.
function SetPlayerTechMaxAllowed(p, techId, maximum)
    p.techMaxAllowed = p.techMaxAllowed or {}
    p.techMaxAllowed[techId] = maximum
end
function GetPlayerTechMaxAllowed(p, techId)
    return p.techMaxAllowed and p.techMaxAllowed[techId] or 0
end

-- Forces hold their members by player id. ForForce visits them in id order (by index, never with
-- pairs, so the order is the same on every run) and GetEnumPlayer answers the one being visited,
-- also inside a nested ForForce. The stdlib's ErrorHandling creates a force at init and runs its
-- error listeners through one.
function CreateForce() return { members = {} } end
function DestroyForce(f) end
function ForceAddPlayer(f, p) f.members[p.id] = p end
function ForceRemovePlayer(f, p) f.members[p.id] = nil end
function ForceClear(f) f.members = {} end
-- A null player or force is not a member, matching the native null-handle default.
function IsPlayerInForce(p, f) return p ~= nil and f ~= nil and f.members[p.id] ~= nil end
local enumPlayers = {}
function GetEnumPlayer() return enumPlayers[#enumPlayers] end
function ForForce(f, callback)
    for id = 0, GetBJMaxPlayerSlots() - 1 do
        local p = f.members[id]
        if p ~= nil then
            enumPlayers[#enumPlayers + 1] = p
            local ok, err = pcall(callback)
            enumPlayers[#enumPlayers] = nil
            if not ok then
                error(err, 0)
            end
        end
    end
end

-- Groups hold their units in an array in creation order, not in the order they were added (measured on the 3.0.0
-- client, as the interpreter's GroupProvider keeps them). Measured on the 3.0.0.24268 client, on
-- Lua as on Jass: ForGroup visits the units the group held when it was called (a unit removed during the
-- enumeration is still visited, one added is not), and GetEnumUnit answers the unit being visited, also after
-- a nested ForGroup returns. CreateGroup is defined above for blizzard.j.lua.
function GroupAddUnit(g, u)
    if u == nil then return false end
    local at = #g.units + 1
    for i = #g.units, 1, -1 do
        if g.units[i] == u then return false end
        if g.units[i].serial > u.serial then at = i end
    end
    table.insert(g.units, at, u)
    return true
end
function GroupRemoveUnit(g, u)
    for i = 1, #g.units do
        if g.units[i] == u then
            table.remove(g.units, i)
            return true
        end
    end
    return false
end
function GroupClear(g) g.units = {} end
function DestroyGroup(g) g.units = {} end
function CountUnitsInGroup(g) return #g.units end
function BlzGroupGetSize(g) return #g.units end
function BlzGroupUnitAt(g, index) return g.units[index + 1] end
function FirstOfGroup(g) return g.units[1] end
function IsUnitInGroup(u, g)
    for i = 1, #g.units do
        if g.units[i] == u then return true end
    end
    return false
end
local enumUnits = {}
function GetEnumUnit() return enumUnits[#enumUnits] end
function ForGroup(g, callback)
    local snapshot = table.move(g.units, 1, #g.units, 1, {})
    for i = 1, #snapshot do
        enumUnits[#enumUnits + 1] = snapshot[i]
        local ok, err = pcall(callback)
        enumUnits[#enumUnits] = nil
        if not ok then
            error(err, 0)
        end
    end
end

-- The stdlib's Framehandle initializes globals from the game's frames at init; a hot-reload build keeps every
-- global of the source, read or not, so those initializers run. Plain records, not a model of the game's frames.
function BlzGetOriginFrame(frameType, index) return { originFrame = frameType, index = index } end
function BlzGetFrameByName(name, index) return { name = name, index = index } end

-- Units and destructables keep what they were created with; nothing in a test moves them. Plain
-- records, not a model of the game's units.
local unitsCreated = 0
function CreateUnit(owner, unitId, x, y, face)
    unitsCreated = unitsCreated + 1
    return { owner = owner, typeId = unitId, x = x, y = y, face = face, serial = unitsCreated }
end
-- A killed unit stays in its groups (measured on the 3.0.0 client).
function KillUnit(u) u.dead = true end
function GetUnitTypeId(u) return u.typeId end
-- Null-handle defaults let Blizzard helpers evaluate their own conditions. Live unit state is not modeled.
function GetUnitState(u, state)
    if u == nil then return 0 end
    error("GetUnitState: live unit state is not modeled by the test runtime")
end
function CreateDestructable(objectId, x, y, face, scale, variation)
    return { typeId = objectId, x = x, y = y, face = face, scale = scale, variation = variation }
end

-- text output: route to stdout so error handlers (BJDebugMsg) work in tests
function GetLocalPlayer() return Player(0) end
function DisplayTimedTextToPlayer(toPlayer, x, y, duration, message)
    print(message)
end
function DisplayTextToPlayer(toPlayer, x, y, message)
    print(message)
end

-- Trigger actions use the compiler fallback's actions list (CreateTrigger / TriggerAddAction).
-- Execute existing registrations too, so hot-reload tests exercise callbacks stored before a payload.
function TriggerExecute(t)
    if t == nil then return end
    for _, action in ipairs(t.actions) do action() end
end
