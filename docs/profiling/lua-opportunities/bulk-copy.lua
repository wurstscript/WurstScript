-- Stock Lua 5.3 only; no claim about Warcraft's library availability or timings.
local rounds = tonumber(arg[1]) or 7
local totalElements = tonumber(arg[2]) or 16000000
local move = assert(table.move)
local default = {__index = function() return 0 end}
local source = setmetatable({[1] = 4, [3] = 9}, default)
local destination = setmetatable({[2] = 100}, default)
move(source, 1, 3, 1, destination)
assert(destination[1] == 4 and destination[2] == 0 and destination[3] == 9)
assert(getmetatable(destination) == default)
local forward, bulk = {1, 2, 3, 4}, {1, 2, 3, 4}
for i = 1, 3 do forward[i + 1] = forward[i] end
move(bulk, 1, 3, 2, bulk)
assert(forward[4] == 1 and bulk[4] == 3)
print("COPY_SAFETY,typed-defaults-preserved-and-forward-overlap-differs,pass")
for _, size in ipairs({8, 64, 1024}) do
    local iterations = math.floor(totalElements / size)
    local src, dst = {}, {}
    for i = 1, size do src[i] = i end
    local variants = {
        {"element-loop", function()
            for iteration = 1, iterations do
                for i = 1, size do dst[i] = src[i] end
            end
        end},
        {"table-move", function()
            for iteration = 1, iterations do move(src, 1, size, 1, dst) end
        end},
    }
    for round = 1, rounds do
        for offset = 1, #variants do
            local variant = variants[(round + offset - 2) % #variants + 1]
            collectgarbage("collect")
            local start = os.clock()
            variant[2]()
            local elapsed = os.clock() - start
            for i = 1, size do assert(dst[i] == src[i]) end
            print(string.format("COPY,%s,%d,%d,%.6f,%d", variant[1], size, round, elapsed, dst[size]))
        end
    end
end
