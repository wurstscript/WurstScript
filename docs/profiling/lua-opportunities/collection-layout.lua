-- Stock-Lua mechanism probe, not generated Wurst and not an in-game benchmark.
-- Compare a shared, offset-indexed arena with one dense table per logical list.
-- Object IDs/lifetimes, bounds checks and callback costs are common/excluded.
-- The arena-hoisted control separates representation from loop-invariant hoisting.
-- Run: lua collection-layout.lua [rounds=7] [repetitions=128] [output.csv]
local rounds = tonumber(arg[1]) or 7
local repetitions = tonumber(arg[2]) or 128
local listCount, elements = 256, 64
local output = arg[3] and assert(io.open(arg[3], "w"))
local function emit(line)
    print(line)
    if output then output:write(line, "\n") end
end

local function layout(stride, warm)
    local arena, starts, lists = {}, {}, {}
    for id = 1, listCount do
        local base = (id - 1) * stride
        starts[id] = base
        local data = {}
        lists[id] = data
        for i = 0, (warm and stride or elements) - 1 do
            local value = id + i
            arena[base + i] = value
            data[i + 1] = value
        end
    end
    if warm then
        -- Populate the entire arena before deleting any gap. Historical
        -- occupancy can keep array parts dense after deletion, so sparse live
        -- keys do not alone establish hash storage. Warm both representations.
        for id = 1, listCount do
            local base, data = starts[id], lists[id]
            for i = elements, stride - 1 do
                arena[base + i] = nil
                data[i + 1] = nil
            end
        end
    end
    return arena, starts, lists
end

local function arenaRead(arena, starts)
    local sum = 0
    for _ = 1, repetitions do
        for id = 1, listCount do
            for i = 0, elements - 1 do
                sum = sum + arena[starts[id] + i]
            end
        end
    end
    return sum
end

local function arenaReadHoisted(arena, starts)
    local sum = 0
    for _ = 1, repetitions do
        for id = 1, listCount do
            local base = starts[id]
            for i = 0, elements - 1 do
                sum = sum + arena[base + i]
            end
        end
    end
    return sum
end

local function nativeRead(lists)
    local sum = 0
    for _ = 1, repetitions do
        for id = 1, listCount do
            local data = lists[id]
            for i = 0, elements - 1 do
                sum = sum + data[i + 1]
            end
        end
    end
    return sum
end

local function median(values)
    table.sort(values)
    return values[math.floor(#values / 2) + 1]
end

emit("workload,round,variant,seconds,checksum")
for _, workload in ipairs({
    {"dense", 64, false},
    {"sparse-aligned", 4096, false},
    {"sparse-staggered", 4099, false},
    {"sparse-warmed", 4096, true},
}) do
    local name = workload[1]
    local arena, starts, lists = layout(workload[2], workload[3])
    local variants = {
        {"arena", function() return arenaRead(arena, starts) end},
        {"arena-hoisted", function() return arenaReadHoisted(arena, starts) end},
        {"native", function() return nativeRead(lists) end},
    }
    local samples, checksum = {{}, {}, {}}, nil
    for round = 1, rounds do
        for offset = 0, 2 do
            local index = (round + offset - 1) % 3 + 1
            collectgarbage("collect")
            local started = os.clock()
            local result = variants[index][2]()
            local elapsed = os.clock() - started
            checksum = checksum or result
            assert(result == checksum, "representation changed the result")
            samples[index][#samples[index] + 1] = elapsed
            emit(string.format("%s,%d,%s,%.6f,%d", name, round, variants[index][1], elapsed, result))
        end
    end
    for index, variant in ipairs(variants) do
        emit(string.format("%s,median,%s,%.6f,%d", name, variant[1], median(samples[index]), checksum))
    end
end

-- Growth-only control: reuse one shared store across logical list lifetimes.
-- Give the arena perfect offset reclamation between lists; free-section search
-- and compaction are excluded, not approximated. Both representations clear
-- their final live references. Only the native variant allocates a table per
-- list. This is a favorable arena control, not the full stdlib allocator.
local function arenaGrowth()
    local sum = 0
    local store = {}
    for _ = 1, repetitions do
        local base, capacity, head = 0, 16, 16
        for i = 0, 4095 do
            if i >= capacity then
                local old = base
                base = head
                head = head + capacity * 2
                capacity = capacity * 2
                for j = 0, i - 1 do
                    store[base + j] = store[old + j]
                end
                for j = 0, i - 1 do
                    store[old + j] = nil
                end
            end
            store[base + i] = i
        end
        sum = sum + store[base + 4095]
        for i = 0, 4095 do
            store[base + i] = nil
        end
    end
    return sum
end

local function nativeGrowth()
    local sum = 0
    for _ = 1, repetitions do
        local store = {}
        for i = 0, 4095 do
            store[i + 1] = i
        end
        sum = sum + store[4096]
        for i = 1, 4096 do
            store[i] = nil
        end
    end
    return sum
end

local variants = {{"arena-copy-clear", arenaGrowth}, {"native", nativeGrowth}}
local samples, checksum = {{}, {}}, nil
for round = 1, rounds do
    for offset = 0, 1 do
        local index = (round + offset - 1) % 2 + 1
        collectgarbage("collect")
        local started = os.clock()
        local result = variants[index][2]()
        local elapsed = os.clock() - started
        checksum = checksum or result
        assert(result == checksum, "growth changed the result")
        samples[index][#samples[index] + 1] = elapsed
        emit(string.format("growth,%d,%s,%.6f,%d", round, variants[index][1], elapsed, result))
    end
end
for index, variant in ipairs(variants) do
    emit(string.format("growth,median,%s,%.6f,%d", variant[1], median(samples[index]), checksum))
end
if output then assert(output:close()) end
