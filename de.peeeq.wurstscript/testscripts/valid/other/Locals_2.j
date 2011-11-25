globals
endglobals
function test_init takes nothing returns nothing
	local integer x
	set x = 1
	if x == 1 then
	call testSuccess()
	else
	call testFail("defect in initialising variable on declaration")
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

