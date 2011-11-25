globals
integer test_y
integer test_x
integer test_z
endglobals
function test_init takes nothing returns nothing
	if test_z == 9 then
	call testSuccess()
	else
	call testFail("Array Test 1 failed.")
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
	set test_x = 7
	set test_y = 1 + test_x
	set test_z = test_y + 1
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

