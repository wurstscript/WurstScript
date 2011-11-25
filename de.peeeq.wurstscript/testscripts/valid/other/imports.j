globals
endglobals
function otherPackage_foo takes nothing returns integer
	return 4
endfunction

function test_init takes nothing returns nothing
	if otherPackage_foo() == 4 then
	call testSuccess()
	else
	call testFail("defect in equality if")
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

