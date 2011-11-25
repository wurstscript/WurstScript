globals
endglobals
function test_init takes nothing returns nothing
	if 4 * 2 == 8 then
	call testSuccess()
	else
	call testFail("defect in binary operation")
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

