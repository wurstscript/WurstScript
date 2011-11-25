globals
endglobals
function test_init takes nothing returns nothing
	local integer i
	set i = 0
	loop
	exitwhen not (i < 10)
	set i = i + 1
	endloop
	if i == 10 then
	call testSuccess()
	else
	call testFail("defect in whileLoop")
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

