globals
integer bla_magic
endglobals
function test_init takes nothing returns nothing
	local integer x
	set x = bla_magic
	call testSuccess()
endfunction

function wurst__init_globals takes nothing returns nothing
	set bla_magic = 7
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

