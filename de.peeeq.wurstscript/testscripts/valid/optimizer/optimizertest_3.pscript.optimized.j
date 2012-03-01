globals
endglobals
function w takes nothing returns string
return "blub"
endfunction
function u takes string r returns nothing
local integer s
set s=0
endfunction
function t takes nothing returns nothing
local string q
set q=w()
call u(w())
endfunction
function e takes nothing returns nothing
endfunction
function main takes nothing returns nothing
call e()
endfunction
