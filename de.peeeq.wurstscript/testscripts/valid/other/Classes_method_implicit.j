globals
integer array test_Person_nextFree
integer test_Person_firstFree
integer test_Person_maxIndex
string array test_Person_name
endglobals
function test_Person_setName takes integer this, string n returns nothing
	set test_Person_name[this] = n
endfunction

function test_Person_getName takes integer this returns string
	return test_Person_name[this]
endfunction

function test_Person_construct takes string n returns integer
	local integer this
	if test_Person_firstFree > 0 then
	set this = test_Person_firstFree
	set test_Person_firstFree = test_Person_nextFree[this]
	else
	set test_Person_maxIndex = test_Person_maxIndex + 1
	set this = test_Person_maxIndex
	endif
	set test_Person_nextFree[this] = -1
	set test_Person_name[this] = n
	return this
endfunction

function test_Person_destroy takes integer this returns nothing
	if test_Person_nextFree[this] < 0 then
	call BJDebugMsg("Double free of Person")
	else
	set test_Person_nextFree[this] = test_Person_firstFree
	set test_Person_firstFree = this
	endif
endfunction

function test_init takes nothing returns nothing
	local integer p
	set p = test_Person_construct("peq")
	if test_Person_getName(p) != "peq" then
	call testFail("name != peq")
	else
	call test_Person_setName(p, "Frotty")
	if test_Person_getName(p) == "Frotty" then
	call testSuccess()
	else
	call testFail("name != Frotty.")
	endif
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

