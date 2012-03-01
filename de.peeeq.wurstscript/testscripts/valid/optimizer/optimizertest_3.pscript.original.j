globals
endglobals
function testpackage_abc takes nothing returns string
	return "blub"
endfunction

function testpackage_bar takes string st returns nothing
	local integer i
	set i = 0
endfunction

function testpackage_foo takes nothing returns nothing
	local string s
	set s = testpackage_abc()
	call testpackage_bar(testpackage_abc())
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
endfunction

