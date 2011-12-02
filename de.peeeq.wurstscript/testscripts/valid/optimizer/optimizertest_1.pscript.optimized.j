globals
integer w
integer u
real r
integer s
real t
integer q
string e
integer array i
integer o
integer p
endglobals
function a takes nothing returns integer
return 5
endfunction
function d takes integer f returns nothing
local real g
local integer array h
local integer j
local integer k
set g=r
set g=.321
set h[12]=353
set j=q
set k=a()
endfunction
function l takes nothing returns nothing
local real y
call d(3)
set y=2.+1.
if y>1. or y<10. then
set s=2
endif
endfunction
function x takes integer c returns integer
return 2
endfunction
function v takes integer b returns integer
return 3
endfunction
function n takes integer m returns integer
return 4
endfunction
function Q takes nothing returns integer
local integer W
if o>0 then
set W=o
set o=i[W]
else
set p=p+1
set W=p
endif
set i[W]=-1
return W
endfunction
function E takes integer R returns nothing
if i[R]>=0 then
call BJDebugMsg("Double free of C")
else
set i[R]=o
set o=R
endif
endfunction
function T takes nothing returns nothing
local integer Z
set Z=Q()
endfunction
function U takes nothing returns nothing
set r=.1
set e="kjgh"
endfunction
function main takes nothing returns nothing
call U()
call T()
endfunction
