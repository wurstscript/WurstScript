globals
endglobals
function test_init takes nothing returns nothing
	local integer x
	set x = 0
	if x == 0 then
	call testSuccess()
	else
	call testFail("defect in testing uninitialised variable")
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

