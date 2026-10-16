-- Optional maintainer probe: insert in a Lua map after BJDebugMsg is defined.
-- Run explicitly; this file is never injected by the compiler or the stock-Lua tests.
-- Record the exact Warcraft client version with the messages.
do
    local function report(name, ok, detail)
        BJDebugMsg("Wurst capability " .. name .. ": " .. tostring(ok) .. " " .. tostring(detail or ""))
    end
    if type(load) ~= "function" then
        report("load-text-with-environment", false, "load is unavailable")
    else
        local ok, result = pcall(function()
            local factory = assert(load(
                "return function(bindings) return function(id) return bindings[id] + 1 end end",
                "@wurst-capability-probe", "t", {}))()
            local state = {[1] = 7}
            local body = factory(state)
            assert(body(1) == 8)
            state[1] = 10
            assert(body(1) == 11)
            assert(load("this is invalid Lua", "@wurst-invalid-probe", "t", {}) == nil)
            return "explicit storage binding and syntax rejection passed"
        end)
        report("load-text-with-environment", ok, result)
    end
    if type(table) ~= "table" or type(table.move) ~= "function" then
        report("table-move", false, "table.move is unavailable")
    else
        local ok, result = pcall(function()
            local defaults = {__index = function() return 0 end}
            local source = setmetatable({[1] = 4, [3] = 9}, defaults)
            local destination = setmetatable({[2] = 100}, defaults)
            table.move(source, 1, 3, 1, destination)
            assert(destination[1] == 4 and destination[2] == 0 and destination[3] == 9)
            assert(getmetatable(destination) == defaults)
            return "typed defaults and destination metatable preserved"
        end)
        report("table-move", ok, result)
    end
end
