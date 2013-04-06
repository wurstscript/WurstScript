globals
integer w
integer u
endglobals
function r takes nothing returns nothing
local integer s
set s=w+u
endfunction
function t takes nothing returns integer
return 121
endfunction
function q takes integer e returns nothing
local integer i
set i=e+t()
endfunction
function o takes nothing returns nothing
set w=55
set u=w
endfunction
function main takes nothing returns nothing
call o()
endfunction
