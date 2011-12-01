globals
integer test_xy
integer test_sdfsdf
real test_aasf
integer test_asdas
real test_aaa
integer test_PLAYER_COLOR_RED
string test_blub
endglobals
function test_foo takes integer xjhgfjh returns nothing
	local real i
	local integer array blub
	local integer aa
	set i = test_aasf
	set i = 0.321
	set blub[12] = 353
	set aa = test_PLAYER_COLOR_RED
endfunction

function test_blub1 takes nothing returns nothing
	local real rer
	call test_foo(3)
	set rer = 2.0 + 1.0
	if rer > 1.0 or rer < 10.0 then
		set test_asdas = 2
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
	set test_aasf = 0.1
	set test_blub = "kjgh"
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
endfunction

