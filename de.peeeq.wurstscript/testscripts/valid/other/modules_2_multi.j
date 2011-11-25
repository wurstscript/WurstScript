globals
integer array test_C_nextFree
integer test_C_firstFree
integer test_C_maxIndex
endglobals
function test_C_B_A_bar takes integer this returns integer
	return 2
endfunction

function test_C_B_foo takes integer this returns integer
	return 3
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
	if test_C_nextFree[this] < 0 then
	call BJDebugMsg("Double free of C")
	else
	set test_C_nextFree[this] = test_C_firstFree
	set test_C_firstFree = this
	endif
endfunction

function test_init takes nothing returns nothing
	local integer c
	set c = test_C_construct()
	if test_C_B_foo(c) != 3 then
	call testFail("foo fail")
	endif
	if test_C_B_A_bar(c) != 2 then
	call testFail("bar fail")
	endif
	call testSuccess()
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

