globals
endglobals
function test_foo takes nothing returns nothing
	local real i
	set i = 0.1
	set i = 0.321
endfunction

function test_blub takes nothing returns nothing
	local real rer
	call test_foo()
	set rer = 2.0 + 1.0
	if rer > 1.0 or rer < 10.0 then
	endif
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

