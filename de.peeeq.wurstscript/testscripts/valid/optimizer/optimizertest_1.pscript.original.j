globals
endglobals
function test_foo takes integer xjhgfjh returns nothing
	local real i
	local integer array blub
	set i = 0.1
	set i = 0.321
	set blub[12] = 353
endfunction

function test_blub takes nothing returns nothing
	local real rer
	call test_foo(3)
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

