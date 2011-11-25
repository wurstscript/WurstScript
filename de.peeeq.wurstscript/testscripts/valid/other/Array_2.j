globals
integer array test_blub
endglobals
function test_init takes nothing returns nothing
	set test_blub[2] = 3
	if test_blub[2] == 3 then
	call testSuccess()
	else
	call testFail("Array Test 2 failed.")
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

