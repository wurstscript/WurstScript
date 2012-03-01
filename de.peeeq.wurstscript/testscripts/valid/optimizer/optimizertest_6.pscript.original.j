globals
endglobals
function test_foo takes nothing returns nothing
	local integer iii
	set iii = 0
endfunction

function test_bar3 takes nothing returns nothing
	local integer iii1
	set iii1 = 0
endfunction

function test_ff takes nothing returns string
	return "yes"
endfunction

function test_init takes nothing returns nothing
	call TriggerRegisterVariableEvent(1, "test_foo", 1, 100.0)
	call TriggerRegisterVariableEvent(1, "test_bar" + test_ff(), 1, 100.0)
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

