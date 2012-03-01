globals
endglobals
function testpackage_testFunction takes nothing returns nothing
	local integer i
	local integer i2
	set i = 1212
	set i2 = 54363
	if 10 > 289 then
		if i != i2 then
			set i = i2
		endif
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
endfunction

