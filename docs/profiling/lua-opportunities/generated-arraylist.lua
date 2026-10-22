-- Stock-Lua harness only. The timed workload is compiled Wurst, loaded unchanged below.
local generated, round, gcMode = assert(arg[1]), assert(arg[2]), assert(arg[3])
local mode, iterations, width, result, callback
function benchmarkRegister(f) callback = f end
function benchmarkMode() return mode end
function benchmarkIterations() return iterations end
function benchmarkWidth() return width end
function benchmarkResult(value) result = value end

for _, file in ipairs({"wc3shim.lua", "common.j.lua", "blizzard.j.lua"}) do
    dofile("src/test/resources/luaruntime/" .. file)
end
dofile(generated)
main()
assert(callback, "generated benchmark did not register")

local cases = {
    {"scan", 0, 65536, 512},
    {"interleaved-growth", 1, 16384, 128},
    {"short-lifetimes", 2, 524288, 8},
    {"reset-refill", 3, 32768, 128},
    {"clear-refill", 4, 32768, 128}
}
for _, case in ipairs(cases) do
    mode, width, iterations = case[2], case[4], 16
    callback() -- populate the actual buffers and stacks, outside the measurement
    collectgarbage("collect")
    if gcMode == "stopped" then collectgarbage("stop") else collectgarbage("restart") end
    iterations = case[3]
    local memory = collectgarbage("count")
    local start = os.clock()
    callback()
    local seconds = os.clock() - start
    local delta = collectgarbage("count") - memory
    local expected = (width - 1) * iterations
    if mode == 0 then expected = width * (width - 1) / 2 * iterations end
    if mode == 1 then expected = (2 * width - 1) * iterations end
    assert(result == expected, "checksum mismatch in " .. case[1] .. ": " .. tostring(result))
    io.write(string.format("SAMPLE,%s,%s,%s,%.6f,%.3f,%s,%d,%d\n",
        round, gcMode, case[1], seconds, delta, tostring(result), iterations, width))
    collectgarbage("restart")
end
