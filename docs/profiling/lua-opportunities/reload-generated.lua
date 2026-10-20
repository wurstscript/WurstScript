-- Stock Lua only. File reading belongs to the test driver, not the reload mechanism.
local file = assert(io.open(assert(arg[1]), "rb"))
local source = file:read("*a")
file:close()
local environment = setmetatable({
    sink = function(_) end,
    flag = function() return true end,
    BJDebugMsg = function(message) error(message) end,
}, {__index = _G})
-- Model a compiler-emitted binding manifest by appending exports in the original lexical scope.
local exports = [[
return {
    descriptors = __wurst_objectClass,
    freeIds = __wurst_objectFree,
    values = A_data_storage,
    allocationState = function() return __wurst_objectMax, __wurst_objectFreeCount end,
}
]]
local manifest = assert(load(source .. exports, "@generated-map-probe", "t", environment))()
environment.main()
local receiver = environment.Probe_left
local descriptor = manifest.descriptors[receiver]
local original = descriptor.A_value
local maxId, freeCount = manifest.allocationState()
assert(environment.effectfulReceiver(true) == 1)
local factory = assert(load([[
return function(bindings)
    local values = bindings.values
    return function(id) return values[id] + 100 end
end
]], "@generated-map-patch", "t", {}))()
local replacement = factory(manifest)
environment.A_A_value = replacement
assert(environment.effectfulReceiver(true) == 1) -- Slot still holds the old function.
descriptor.A_value = replacement
assert(environment.effectfulReceiver(true) == 101)
assert(environment.repeated(receiver) == 204)
assert(environment.A_A_value(receiver) == 101 and original(receiver) == 1)
assert(manifest.descriptors[receiver] == descriptor)
assert(manifest.values[receiver] == 1)
local afterMax, afterFreeCount = manifest.allocationState()
assert(maxId == afterMax and freeCount == afterFreeCount)
print("GENERATED_RELOAD_PROBE,existing-object-observes-patched-body,pass")
print("GENERATED_RELOAD_PROBE,descriptor-storage-allocator-preserved,pass")
print("GENERATED_RELOAD_PROBE,global-rebinding-alone-is-insufficient,confirmed")
