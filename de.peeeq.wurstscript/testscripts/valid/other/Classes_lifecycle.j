globals
integer array test_Blub_nextFree
integer test_Blub_firstFree
integer test_Blub_maxIndex
integer array test_Blub_x
integer array test_b
endglobals
function test_Blub_construct takes integer x returns integer
	local integer this
	if test_Blub_firstFree > 0 then
	set this = test_Blub_firstFree
	set test_Blub_firstFree = test_Blub_nextFree[this]
	else
	set test_Blub_maxIndex = test_Blub_maxIndex + 1
	set this = test_Blub_maxIndex
	endif
	set test_Blub_nextFree[this] = -1
	set test_Blub_x[this] = x
	return this
endfunction

function test_Blub_destroy takes integer this returns nothing
	if test_Blub_nextFree[this] < 0 then
	call BJDebugMsg("Double free of Blub")
	else
	set test_Blub_nextFree[this] = test_Blub_firstFree
	set test_Blub_firstFree = this
	endif
endfunction

function test_init takes nothing returns nothing
	local integer i
	set test_b[0] = test_Blub_construct(0)
	set test_b[1] = test_Blub_construct(1)
	set test_b[2] = test_Blub_construct(2)
	set test_b[3] = test_Blub_construct(3)
	set test_b[4] = test_Blub_construct(4)
	set test_b[5] = test_Blub_construct(5)
	set test_b[6] = test_Blub_construct(6)
	set test_b[7] = test_Blub_construct(7)
	call test_Blub_destroy(test_b[0])
	call test_Blub_destroy(test_b[6])
	call test_Blub_destroy(test_b[2])
	call test_Blub_destroy(test_b[4])
	set test_b[8] = test_Blub_construct(8)
	set test_b[9] = test_Blub_construct(9)
	set i = 0
	loop
	exitwhen not (i < 10)
	if ModuloInteger(i, 2) == 1 and test_Blub_x[test_b[i]] != i then
	call testFail("fail " + I2S(i))
	endif
	set i = i + 1
	endloop
	call testSuccess()
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

