globals
endglobals
function bla_foo takes nothing returns nothing
	call testSuccess()
endfunction

function test_init takes nothing returns nothing
	call bla_foo()
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

