globals
endglobals
function w takes nothing returns nothing
endfunction

function zw takes nothing returns nothing
	call w()
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

