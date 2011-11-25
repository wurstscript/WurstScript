globals
endglobals
function test_add takes integer x, integer y returns integer
	local integer z
	set z = x + y
	return x + y
endfunction

function test_init takes nothing returns nothing
	if test_add(3, 4) == 7 then
	call testSuccess()
	else
	call testFail("testFunctionCall")
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

