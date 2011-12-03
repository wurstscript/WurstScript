globals
integer testpackage_tollerName
integer testpackage_langerName
endglobals
function testpackage_testFunction takes nothing returns nothing
	local integer neuerName
	set neuerName = testpackage_tollerName + testpackage_langerName
endfunction

function testpackage_ret takes nothing returns integer
	return 121
endfunction

function testpackage_aktionen takes integer parameterInt returns nothing
	local integer tollerInt
	set tollerInt = parameterInt + testpackage_ret()
endfunction

function wurst__init_globals takes nothing returns nothing
	set testpackage_tollerName = 55
	set testpackage_langerName = testpackage_tollerName
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
endfunction

