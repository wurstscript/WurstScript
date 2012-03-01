globals
endglobals
function zw takes nothing returns nothing
local integer w
set w=0
endfunction
function test_bar3 takes nothing returns nothing
local integer u
set u=0
endfunction
function r takes nothing returns string
return "yes"
endfunction
function s takes nothing returns nothing
call TriggerRegisterVariableEvent(1,"zw",1,100.)
call TriggerRegisterVariableEvent(1,"test_bar"+r(),1,100.)
endfunction
function t takes nothing returns nothing
endfunction
function main takes nothing returns nothing
call t()
call s()
endfunction
