globals
endglobals
function test_init takes nothing returns nothing
	if 1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9 == 45 then
	call testSuccess()
	else
	call testFail("defect in long binary operation")
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

