globals
integer array test_Pair_nextFree
integer test_Pair_firstFree
integer test_Pair_maxIndex
integer array test_Pair_a
integer array test_Pair_b
endglobals
function test_Pair_construct takes nothing returns integer
	local integer this
	if test_Pair_firstFree > 0 then
	set this = test_Pair_firstFree
	set test_Pair_firstFree = test_Pair_nextFree[this]
	else
	set test_Pair_maxIndex = test_Pair_maxIndex + 1
	set this = test_Pair_maxIndex
	endif
	set test_Pair_nextFree[this] = -1
	return this
endfunction

function test_Pair_destroy takes integer this returns nothing
	if test_Pair_nextFree[this] < 0 then
	call BJDebugMsg("Double free of Pair")
	else
	set test_Pair_nextFree[this] = test_Pair_firstFree
	set test_Pair_firstFree = this
	endif
endfunction

function test_init takes nothing returns nothing
	local integer p
	set p = test_Pair_construct()
	set test_Pair_a[p] = 4
	if test_Pair_a[p] == 4 then
	call testSuccess()
	else
	call testFail("Array Test 1 failed.")
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

