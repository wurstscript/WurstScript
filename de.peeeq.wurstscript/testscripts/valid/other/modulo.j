globals
endglobals
function ModuloInteger2 takes integer dividend, integer divisor returns integer
	local integer modulus
	set modulus = dividend - dividend / divisor * divisor
	if modulus < 0 then
	set modulus = modulus + divisor
	endif
	return modulus
endfunction

function test_init takes nothing returns nothing
	local integer i
	set i = ModuloInteger2(10, 3)
	if i == 1 then
	call testSuccess()
	else
	call testFail("modulo wrong:" + I2S(i))
	endif
endfunction

function wurst__init_globals takes nothing returns nothing
endfunction

function main takes nothing returns nothing
	call wurst__init_globals()
	call test_init()
endfunction

