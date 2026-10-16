-- Stock Lua 5.3 experiment only. os.clock and collectgarbage are not emitted into Warcraft maps.
local n = tonumber(arg[1]) or 2000000
local rounds = tonumber(arg[2]) or 7
local descriptor = {}
local payload = {[1] = 7}
local classA = {
    value = function(id) return payload[id] end,
    other = function(id) return payload[id] + 1 end,
}
descriptor[1] = classA
local boxChild = {[1] = 1}
local function choose(id) return id end
local function dispatch(id, ...) return descriptor[id].value(id, ...) end

local variants = {
    {"repeated-descriptor", function(count)
        local sum = 0
        for i = 1, count do
            local id = i % 1 + 1
            sum = sum + descriptor[id].value(id) + descriptor[id].other(id) + descriptor[id].value(id)
        end
        return sum
    end},
    {"cached-descriptor", function(count)
        local sum = 0
        for i = 1, count do
            local id = i % 1 + 1
            local class = descriptor[id]
            sum = sum + class.value(id) + class.other(id) + class.value(id)
        end
        return sum
    end},
    {"cached-descriptor-and-slot", function(count)
        local sum = 0
        for i = 1, count do
            local id = i % 1 + 1
            local class = descriptor[id]
            local value = class.value
            sum = sum + value(id) + class.other(id) + value(id)
        end
        return sum
    end},
    {"known-direct", function(count)
        local sum = 0
        local value, other = classA.value, classA.other
        for i = 1, count do
            local id = i % 1 + 1
            sum = sum + value(id) + other(id) + value(id)
        end
        return sum
    end},
}
local receiverVariants = {
    {"receiver-stub", function(count)
        local sum = 0
        for i = 1, count do sum = sum + dispatch(choose(1)) end
        return sum
    end},
    {"receiver-temporary", function(count)
        local sum = 0
        for i = 1, count do
            local id = choose(1)
            sum = sum + descriptor[id].value(id)
        end
        return sum
    end},
}
local fieldVariants = {
    {"field-repeated", function(count)
        local sum = 0
        for i = 1, count do
            sum = sum + descriptor[boxChild[1]].value(boxChild[1])
                + descriptor[boxChild[1]].other(boxChild[1])
        end
        return sum
    end},
    {"field-cached-receiver", function(count)
        local sum = 0
        for i = 1, count do
            local id = boxChild[1]
            sum = sum + descriptor[id].value(id) + descriptor[id].other(id)
        end
        return sum
    end},
    {"field-cached-receiver-and-descriptor", function(count)
        local sum = 0
        for i = 1, count do
            local id = boxChild[1]
            local class = descriptor[id]
            sum = sum + class.value(id) + class.other(id)
        end
        return sum
    end},
}

local function measure(group, expected)
    for _, variant in ipairs(group) do assert(variant[2](1000) == expected * 1000) end
    for round = 1, rounds do
        for offset = 1, #group do
            local index = (round + offset - 2) % #group + 1
            local variant = group[index]
            collectgarbage("collect")
            local start = os.clock()
            local checksum = variant[2](n)
            local elapsed = os.clock() - start
            assert(checksum == expected * n)
            print(string.format("BENCH,%s,%d,%.6f,%d", variant[1], round, elapsed, checksum))
        end
    end
end

-- Destroy + allocation can recycle an id, even when the receiver variable itself did not change.
local oldClass = {value = function() return 1 end}
local replacementClass = {value = function() return 20 end}
local function reallocate(id)
    descriptor[id] = nil -- destroy
    descriptor[id] = replacementClass -- allocation reuses the just-freed id
end
descriptor[1] = oldClass
local cached = descriptor[1]
reallocate(1)
assert(descriptor[1].value(1) == 20)
assert(cached.value(1) == 1)
-- A saved receiver field is similarly invalid across a setter/callback changing that field.
boxChild[1] = 1
local saved = boxChild[1]
boxChild[1] = 2
assert(saved ~= boxChild[1])
descriptor[1] = classA
boxChild[1] = 1
print("SAFETY,destroy-reuse-and-field-write-counterexamples,pass")
measure(variants, 22)
measure(receiverVariants, 7)
measure(fieldVariants, 15)
