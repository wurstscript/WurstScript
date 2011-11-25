globals
endglobals
function w takes nothing returns nothing
local integer i
set i=0
endfunction
function zw takes nothing returns nothing
local real r
call w()
set r=2.0+1.0
endfunction
function u takes string s returns nothing
endfunction
function r takes nothing returns nothing
call u("zw")
endfunction
function s takes nothing returns nothing
endfunction
function t takes nothing returns nothing
call s()
endfunction
