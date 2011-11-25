globals
integer array blub
endglobals
function foo takes nothing returns nothing
	set blub[5] = 3
	if blub[5] == 3 then
	call testSuccess()
	else
	call testFail("Array Test 1 failed.")
	endif
endfunction

function test_init takes nothing returns nothing
	call foo()
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

