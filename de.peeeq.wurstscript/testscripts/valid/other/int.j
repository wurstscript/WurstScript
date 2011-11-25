globals
endglobals
function test_init takes nothing returns nothing
	local integer i
	set i = 3
	if i == 3 then
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

