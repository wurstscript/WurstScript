globals
endglobals
function test_d takes nothing returns nothing
	call testSuccess()
endfunction

function test_c takes nothing returns nothing
	call test_d()
endfunction

function test_b takes nothing returns nothing
	call test_c()
endfunction

function test_a takes nothing returns nothing
	call test_b()
endfunction

function test_init takes nothing returns nothing
	call test_a()
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

