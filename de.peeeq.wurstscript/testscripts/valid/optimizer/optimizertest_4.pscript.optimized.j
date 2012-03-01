globals
endglobals
function zw takes nothing returns string
return "hans"
endfunction
function w takes nothing returns nothing
call ExecuteFunction("testpackage_funcB"+zw())
call ExecuteFunction("zw")
call ExecuteFunction("testpackage_foo"+I2S(1289))
endfunction
function testpackage_funcBblub takes nothing returns nothing
local integer u
set u=0
endfunction
function r takes nothing returns nothing
local integer s
set s=0
endfunction
function t takes nothing returns nothing
endfunction
function main takes nothing returns nothing
call t()
endfunction
