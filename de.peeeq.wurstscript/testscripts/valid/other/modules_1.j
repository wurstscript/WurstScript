globals
integer array test_CounterImpl_nextFree
integer test_CounterImpl_firstFree
integer test_CounterImpl_maxIndex
integer array test_CounterImpl_Counter_count
endglobals
function test_CounterImpl_Counter_incCounter takes integer this returns nothing
	set test_CounterImpl_Counter_count[this] = test_CounterImpl_Counter_count[this] + 1
endfunction

function test_CounterImpl_Counter_getCounter takes integer this returns integer
	return test_CounterImpl_Counter_count[this]
endfunction

function test_CounterImpl_construct takes nothing returns integer
	local integer this
	if test_CounterImpl_firstFree > 0 then
	set this = test_CounterImpl_firstFree
	set test_CounterImpl_firstFree = test_CounterImpl_nextFree[this]
	else
	set test_CounterImpl_maxIndex = test_CounterImpl_maxIndex + 1
	set this = test_CounterImpl_maxIndex
	endif
	set test_CounterImpl_nextFree[this] = -1
	return this
endfunction

function test_CounterImpl_destroy takes integer this returns nothing
	if test_CounterImpl_nextFree[this] < 0 then
	call BJDebugMsg("Double free of CounterImpl")
	else
	set test_CounterImpl_nextFree[this] = test_CounterImpl_firstFree
	set test_CounterImpl_firstFree = this
	endif
endfunction

function test_init takes nothing returns nothing
	local integer c
	set c = test_CounterImpl_construct()
	call test_CounterImpl_Counter_incCounter(c)
	call test_CounterImpl_Counter_incCounter(c)
	if test_CounterImpl_Counter_getCounter(c) == 2 then
	call testSuccess()
	else
	call testFail("defect in equality if")
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

