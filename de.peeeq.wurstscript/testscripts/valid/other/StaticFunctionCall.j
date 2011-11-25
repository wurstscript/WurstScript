globals
integer array test_Math_nextFree
integer test_Math_firstFree
integer test_Math_maxIndex
endglobals
function test_Math_add takes integer x, integer y returns integer
	local integer z
	set z = x + y
	return x + y
endfunction

function test_Math_construct takes nothing returns integer
	local integer this
	if test_Math_firstFree > 0 then
	set this = test_Math_firstFree
	set test_Math_firstFree = test_Math_nextFree[this]
	else
	set test_Math_maxIndex = test_Math_maxIndex + 1
	set this = test_Math_maxIndex
	endif
	set test_Math_nextFree[this] = -1
	return this
endfunction

function test_Math_destroy takes integer this returns nothing
	if test_Math_nextFree[this] < 0 then
	call BJDebugMsg("Double free of Math")
	else
	set test_Math_nextFree[this] = test_Math_firstFree
	set test_Math_firstFree = this
	endif
endfunction

function test_init takes nothing returns nothing
	if test_Math_add(3, 4) == 7 then
	call testSuccess()
	else
	call testFail("testFunctionCall")
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

