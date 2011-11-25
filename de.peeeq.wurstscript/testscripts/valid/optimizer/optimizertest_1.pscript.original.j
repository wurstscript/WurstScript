globals
endglobals
function test_foo takes nothing returns nothing
	local integer i
	set i = 0
endfunction

function test_blub takes nothing returns nothing
	local real r
	call test_foo()
	set r = 2.0 + 1.0
endfunction

function test_ExecuteFunc takes string s returns nothing
endfunction

function test_bar takes nothing returns nothing
	call test_ExecuteFunc("test_blub")
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
endfunction

