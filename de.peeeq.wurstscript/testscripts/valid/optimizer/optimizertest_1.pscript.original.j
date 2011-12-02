globals
integer test_xy
integer test_sdfsdf
real test_aasf
integer test_asdas
real test_aaa
integer test_PLAYER_COLOR_RED
string test_blub
integer array test_C_nextFree
integer test_C_firstFree
integer test_C_maxIndex
endglobals
function test_returnfive takes nothing returns integer
	return 5
endfunction

function test_foo takes integer xjhgfjh returns nothing
	local real i
	local integer array blub
	local integer aa
	local integer bb
	set i = test_aasf
	set i = 0.321
	set blub[12] = 353
	set aa = test_PLAYER_COLOR_RED
	set bb = test_returnfive()
endfunction

function test_blub1 takes nothing returns nothing
	local real rer
	call test_foo(3)
	set rer = 2.0 + 1.0
	if rer > 1.0 or rer < 10.0 then
		set test_asdas = 2
	endif
endfunction

function test_C_A_foo takes integer this returns integer
	return 2
endfunction

function test_C_B_foo takes integer this returns integer
	return 3
endfunction

function test_C_foo takes integer this returns integer
	return 4
endfunction

function test_C_construct takes nothing returns integer
	local integer this
	if test_C_firstFree > 0 then
		set this = test_C_firstFree
		set test_C_firstFree = test_C_nextFree[this]
	else
		set test_C_maxIndex = test_C_maxIndex + 1
		set this = test_C_maxIndex
	endif
	set test_C_nextFree[this] = -1
	return this
endfunction

function test_C_destroy takes integer this returns nothing
	if test_C_nextFree[this] >= 0 then
		call BJDebugMsg("Double free of C")
	else
		set test_C_nextFree[this] = test_C_firstFree
		set test_C_firstFree = this
	endif
endfunction

function test_init takes nothing returns nothing
	local integer c
	set c = test_C_construct()
endfunction

function wurst__init_globals takes nothing returns nothing
	set test_aasf = 0.1
	set test_blub = "kjgh"
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

