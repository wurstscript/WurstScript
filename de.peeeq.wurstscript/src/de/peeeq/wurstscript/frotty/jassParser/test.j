globals
endglobals

function foo takes nothing returns nothing

endfunction

function bla takes nothing returns nothing
	local integer blub
	local trigger t = CreateTrigger()
	local integer i = 12
	set blub = i
	call TriggerAddAction( t, function foo )
endfunction
