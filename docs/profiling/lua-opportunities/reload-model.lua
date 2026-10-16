-- Stock Lua 5.3 feasibility probe, not an in-game reload implementation.
-- No debug, io, os, package, or require is needed by this mechanism.
local stateValues = {[1] = 7}
local allocator = {max = 1, freeCount = 0}
local classA, classChild = {}, {}
local api = {}
local function original(id) return stateValues[id] + 1 end
api.score = original
classA.value, classA.alias = original, original
classChild.value = original
local oldReference = api.score
-- This is the emitted adapter's behavior: resolve the global implementation on each invocation.
api.callback = function(id)
    local ok, result = xpcall(api.score, function(err) return err end, id)
    assert(ok, result)
    return result
end
local registeredCallback = api.callback
assert(api.score(1) == 8 and registeredCallback(1) == 8)

-- Rebinding one global does not update class slots or existing raw function values.
api.score = function(id) return stateValues[id] + 10 end
assert(api.score(1) == 17)
assert(registeredCallback(1) == 17)
assert(classA.value(1) == 8 and oldReference(1) == 8)
api.score = original

-- A separately loaded body cannot see lexical storage captured by the initial map chunk.
local unbound = assert(load("return function(id) return stateValues[id] + 10 end", "@unbound", "t", {}))()
assert(not pcall(unbound, 1))

local manifest = {
    abi = "schema-v1",
    version = 1,
    state = {values = stateValues, allocator = allocator},
    slots = {
        {classA, "value"},
        {classA, "alias"},
        {classChild, "value"},
    },
}

local function stage(source, expectedAbi, expectedVersion)
    if expectedAbi ~= manifest.abi or expectedVersion ~= manifest.version then return nil, "stale ABI/version" end
    local chunk, err = load(source, "@score-patch", "t", {})
    if not chunk then return nil, err end
    local ok, factory = pcall(chunk)
    if not ok or type(factory) ~= "function" then return nil, "invalid patch factory" end
    local okBody, replacement = pcall(factory, manifest.state)
    if not okBody or type(replacement) ~= "function" then return nil, "invalid patch body" end
    return {replacement = replacement, version = expectedVersion}
end

local function commit(patch)
    if patch.version ~= manifest.version then return false, "stale staged patch" end
    -- The commit has no user code, native calls, or yield points. Slots are an explicit ordered list.
    api.score = patch.replacement
    for i = 1, #manifest.slots do
        local slot = manifest.slots[i]
        slot[1][slot[2]] = patch.replacement
    end
    manifest.version = manifest.version + 1
    return true
end

local source = [[
return function(bindings)
    local stateValues = bindings.values
    return function(id) return stateValues[id] + 100 end
end
]]
assert(stage(source, "other-schema", 1) == nil)
assert(stage("this is not valid lua", "schema-v1", 1) == nil)
assert(api.score(1) == 8 and classA.value(1) == 8 and manifest.version == 1)
local patch = assert(stage(source, "schema-v1", 1))
local competing = assert(stage(source, "schema-v1", 1))
assert(commit(patch))
assert(not commit(competing))
assert(api.score(1) == 107)
assert(classA.value(1) == 107 and classA.alias(1) == 107 and classChild.value(1) == 107)
assert(registeredCallback(1) == 107)
assert(oldReference(1) == 8) -- Arbitrary captured function values still need a policy.
assert(manifest.state.values == stateValues and stateValues[1] == 7)
assert(manifest.state.allocator == allocator and allocator.max == 1 and allocator.freeCount == 0)
print("HOT_RELOAD_PROBE,global-and-slot-patching,pass")
print("HOT_RELOAD_PROBE,registered-adapter-survives,pass")
print("HOT_RELOAD_PROBE,storage-and-allocator-preserved,pass")
print("HOT_RELOAD_PROBE,syntax-ABI-version-failures-preserve-live-state,pass")
print("HOT_RELOAD_PROBE,raw-captured-functions-stay-old,confirmed")
print("HOT_RELOAD_PROBE,lexical-storage-requires-explicit-binding,confirmed")
