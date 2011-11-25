globals
integer array test_Bla_nextFree
integer test_Bla_firstFree
integer test_Bla_maxIndex
integer array test_Bla_i
integer array test_Bla_j
integer test_Bla_k
endglobals
function test_Bla_construct takes integer j returns integer
	local integer this
	if test_Bla_firstFree > 0 then
	set this = test_Bla_firstFree
	set test_Bla_firstFree = test_Bla_nextFree[this]
	else
	set test_Bla_maxIndex = test_Bla_maxIndex + 1
	set this = test_Bla_maxIndex
	endif
	set test_Bla_nextFree[this] = -1
	set test_Bla_i[this] = 13
	set test_Bla_j[this] = 27
	set test_Bla_j[this] = j
	return this
endfunction

function test_Bla_getI takes integer this returns integer
	return test_Bla_i[this]
endfunction

function test_Bla_getJ takes integer this returns integer
	return test_Bla_j[this]
endfunction

function test_Bla_getK takes integer this returns integer
	return test_Bla_k
endfunction

function test_Bla_destroy takes integer this returns nothing
	if test_Bla_nextFree[this] < 0 then
	call BJDebugMsg("Double free of Bla")
	else
	set test_Bla_nextFree[this] = test_Bla_firstFree
	set test_Bla_firstFree = this
	endif
endfunction

function test_init takes nothing returns nothing
	local integer b
	set b = test_Bla_construct(14)
	if test_Bla_getI(b) != 13 then
	call testFail("i")
	else
	if test_Bla_getJ(b) != 14 then
	call testFail("j")
	else
	if test_Bla_getK(b) != 36 then
	call testFail("k")
	else
	call testSuccess()
	endif
	endif
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
	set test_Bla_k = 36
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

