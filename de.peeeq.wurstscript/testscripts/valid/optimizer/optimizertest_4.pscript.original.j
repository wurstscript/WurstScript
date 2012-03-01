globals
endglobals
function testpackage_abc takes nothing returns string
	return "hans"
endfunction

function testpackage_funcA takes nothing returns nothing
	call ExecuteFunction("testpackage_funcB" + testpackage_abc())
	call ExecuteFunction("testpackage_abc")
	call ExecuteFunction("testpackage_foo" + I2S(1289))
endfunction

function testpackage_funcBblub takes nothing returns nothing
	local integer i
	set i = 0
endfunction

function testpackage_foobar takes nothing returns nothing
	local integer i1
	set i1 = 0
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
endfunction

