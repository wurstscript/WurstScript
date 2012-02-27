globals
camerasetup temp_camerasetup
event temp_event
boolean temp_boolean
unit temp_unit
group temp_group
force temp_force
player temp_player
item temp_item
integer udg_anzahl
unit array udg_gh
trigger gg_trg_GameOptions
trigger gg_trg_GH
trigger gg_trg_GameInit
trigger gg_trg_GameLoop
trigger gg_trg_Entity
trigger gg_trg_Node
trigger gg_trg_EntityManagement
trigger gg_trg_Missile
trigger gg_trg_SpellData
trigger gg_trg_SpellModule
trigger gg_trg_Arcane_Missile
trigger gg_trg_Bouncing_Missile
trigger gg_trg_Magic_Watercurrent
trigger gg_trg_KillingSystem
trigger gg_trg_DmgTexttag
trigger gg_trg_Arena
trigger gg_trg_Obelisk
trigger gg_trg_Magician
trigger gg_trg_PlayerData
trigger gg_trg_Item
trigger gg_trg_OptimizedMaths
trigger gg_trg_TextFunctions
trigger gg_trg_TerrainUtils
trigger gg_trg_Untitled_Trigger_001
integer mode
real gravity
real gravity1
real gravity2
integer array Node_Node_nextFree
integer Node_Node_firstFree
integer Node_Node_maxIndex
integer array entity
integer array entity1
integer array nodes
integer entityCount
integer TYPE_MAGICIAN
integer TYPE_OBELISK
integer TYPE_MISSILE
real COLLISION_SIZE
real EXPLOSION_AOE
integer array ArcaneMissile_ArcaneMissile_nextFree
integer ArcaneMissile_ArcaneMissile_firstFree
integer ArcaneMissile_ArcaneMissile_maxIndex
real wurst__tupleReturnVar_real_1
real wurst__tupleReturnVar_real_2
real array time
boolean array decay
integer array fx
real array angle6
real array speed4
real array velocity2
real array velocity21
real array velocity22
real temp_real
real array position1
real array position11
real array position12
real array radius16
player array owner2
boolean array done1
trigger t15
integer spellId
real COLLISION_SIZE1
integer array ArcaneMissile_ArcaneMissile_nextFree1
integer ArcaneMissile_ArcaneMissile_firstFree1
integer ArcaneMissile_ArcaneMissile_maxIndex1
real array time1
boolean array decay1
integer array fx1
real array angle9
real array speed7
real array velocity3
real array velocity31
real array velocity32
real array position2
real array position21
real array position22
real array radius19
player array owner4
boolean array done2
trigger t16
integer spellId1
integer TIME_FOR_MULTI
integer TIME_REDUCTION
string array multiMsgs
string array spreeMsgs
integer array DmgTexttag_DmgTexttag_nextFree
integer DmgTexttag_DmgTexttag_firstFree
integer DmgTexttag_DmgTexttag_maxIndex
texttag array tt12
integer currentArena
integer array Arena_Arena_nextFree
integer Arena_Arena_firstFree
integer Arena_Arena_maxIndex
real array centerPoint
real array centerPoint1
real array centerPoint2
real array maxX2
real array maxY2
real array minX2
real array minY2
real array halfX
real array halfY
integer OBELISK_ID
real COLLISION_SIZE2
integer array Obelisk_Obelisk_nextFree
integer Obelisk_Obelisk_firstFree
integer Obelisk_Obelisk_maxIndex
unit array actor
real array position3
real array position31
real array position32
real array radius22
player array owner6
boolean array done3
integer MAGICIAN_ID
real COLLISION_SIZE3
integer array Magician_Magician_nextFree
integer Magician_Magician_firstFree
integer Magician_Magician_maxIndex
real array hp
real array dmgRecieved
real array walkVelocity
real array walkVelocity1
real array walkVelocity2
unit array actor1
real array mass
real array restitution
real array weight
real array frictionModifier
real array terrainFriction
boolean array affectedByGravity
real array velocity4
real array velocity41
real array velocity42
real array position4
real array position41
real array position42
real array radius24
player array owner8
boolean array done4
integer array players_data_row
integer array player_data
integer player_count
string array player_colors
integer array PlayerData_PlayerData_nextFree
integer PlayerData_PlayerData_firstFree
integer PlayerData_PlayerData_maxIndex
integer array magician
string array coloredName
player array thePlayer6
integer array id7
integer array gameId
integer array points
real PI
real DEGTORAD
real RADTODEG
real MAX_TILES
real ARENA_TILES
real TILE_SECTION_LENGTH
integer array TerrainUtils_TerrainUtils_nextFree
integer TerrainUtils_TerrainUtils_firstFree
integer TerrainUtils_TerrainUtils_maxIndex
real maxHeight
rect worldRect
location tempLoc
real minWorldX
real minWorldY
real maxWorldX
real maxWorldY
player DUMMY_PLAYER
integer array MovingEffect_MovingEffect_nextFree
integer MovingEffect_MovingEffect_firstFree
integer MovingEffect_MovingEffect_maxIndex
unit array dummy
effect array fx2
real array zang
string array sfxPath
integer array red24
integer array green24
integer array blue24
integer array alpha2
integer array abil
integer DUMMY_UNIT_ID
integer DEBUG_LEVEL
endglobals
// function InitGlobals
function InitGlobals takes nothing returns nothing
	local integer i2
	set i2 = 0
	set udg_anzahl = 0
endfunction

function flushChild takes hashtable this, integer parentKey returns hashtable
	call FlushChildHashtable(this, parentKey)
	return this
endfunction

function flushParent takes hashtable this returns hashtable
	call FlushParentHashtable(this)
	return this
endfunction

// function OptimizedMaths.pPY
function pPY takes real y33, real dist4, real angle16 returns real
	return y33 + dist4 * Sin(angle16 * DEGTORAD)
endfunction

// function PrintingHelper.print
function print takes string msg2 returns nothing
	local integer i13
	set i13 = 0
	loop
		exitwhen i13 > 12
		call DisplayTimedTextToPlayer(Player(i13), 0.0, 0.0, 60.0, msg2)
		set i13 = i13 + 1
	endloop
endfunction

// function OptimizedMaths.pPX
function pPX takes real x33, real dist3, real angle15 returns real
	return x33 + dist3 * Cos(angle15 * DEGTORAD)
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.enableZ
function enableZ takes integer this returns nothing
	if UnitAddAbility(actor1[this], 1097691750) then
		call UnitRemoveAbility(actor1[this], 1097691750)
	endif
	call print("fly added")
endfunction

function toString takes real this returns string
	return R2S(this)
endfunction

function toString1 takes real this, real this2, real this3 returns string
	return "Vector3 [ " + toString(this) + ", " + toString(this2) + ", " + toString(this3) + " ]"
endfunction

// function PrintingHelper.debugPrint
function debugPrint takes string msg1, integer level4 returns nothing
	if level4 >= DEBUG_LEVEL then
		call print(msg1)
	endif
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.EntityModule.StaticEntityModule.setUpEntity
function setUpEntity3 takes integer this, real x32, real y32, real z12, real radius25, player owner9 returns nothing
	local real temp63
	local real temp64
	local real temp65
	set radius24[this] = radius25
	set temp63 = x32
	set temp64 = y32
	set temp65 = z12
	set position4[this] = temp63
	set position41[this] = temp64
	set position42[this] = temp65
	set owner8[this] = owner9
	call debugPrint("setUp:" + toString1(position4[this], position41[this], position42[this]), 0)
endfunction

// constructor function for Node
function Node_Node_construct takes integer entity2, integer entity21 returns integer
	local integer this
	if Node_Node_firstFree > 0 then
		set this = Node_Node_firstFree
		set Node_Node_firstFree = Node_Node_nextFree[this]
	else
		set Node_Node_maxIndex = Node_Node_maxIndex + 1
		set this = Node_Node_maxIndex
	endif
	set Node_Node_nextFree[this] = -1
	set entity[this] = entity2
	set entity1[this] = entity21
	return this
endfunction

// function EntityManagement.addEntity
function addEntity takes integer e4, integer e41 returns nothing
	set nodes[entityCount] = Node_Node_construct(e4, e41)
	set entityCount = entityCount + 1
endfunction

// function TerrainUtils.TerrainUtils.getZ
function getZ takes real x34, real y34 returns real
	call MoveLocation(tempLoc, x34, y34)
	return GetLocationZ(tempLoc)
endfunction

// function Magician.Magician.updatePosition
function updatePosition1 takes integer this returns nothing
	call SetUnitX(actor1[this], position4[this])
	call SetUnitY(actor1[this], position41[this])
	call SetUnitFlyHeight(actor1[this], position42[this] - getZ(position4[this], position41[this]), 0.0)
endfunction

// constructor function for Magician
function Magician_Magician_construct takes real x30, real y30, real z10, real facing7, player p5 returns integer
	local integer this
	local real temp51
	local real temp52
	local real temp53
	if Magician_Magician_firstFree > 0 then
		set this = Magician_Magician_firstFree
		set Magician_Magician_firstFree = Magician_Magician_nextFree[this]
	else
		set Magician_Magician_maxIndex = Magician_Magician_maxIndex + 1
		set this = Magician_Magician_maxIndex
	endif
	set Magician_Magician_nextFree[this] = -1
	set done4[this] = false
	set temp51 = 0.0
	set temp52 = 0.0
	set temp53 = 0.0
	set velocity4[this] = temp51
	set velocity41[this] = temp52
	set velocity42[this] = temp53
	set mass[this] = 10.0
	set restitution[this] = 0.8
	set frictionModifier[this] = 1.0
	set terrainFriction[this] = 0.96
	set affectedByGravity[this] = true
	set actor1[this] = null
	call setUpEntity3(this, x30, y30, z10, COLLISION_SIZE3, p5)
	set actor1[this] = CreateUnit(p5, MAGICIAN_ID, x30, y30, facing7)
	call enableZ(this)
	call updatePosition1(this)
	call addEntity(this, 1)
	return this
endfunction

// function Magician.initMagicians
function initMagicians takes nothing returns nothing
	local real distance2
	local integer i10
	local integer temp70
	local real angle14
	if 0.03125 != 0.03125 then
		call print("ya")
	endif
	call print("initializing Magicians..")
	set distance2 = MAX_TILES * 128.0 - 64.0
	set i10 = 0
	set temp70 = player_count - 1
	loop
		exitwhen i10 > temp70
		set angle14 = 180 + 360 * 1.0 / player_count * i10
		set magician[player_data[i10]] = Magician_Magician_construct(pPX(0.0, distance2, angle14), pPY(0.0, distance2, angle14), 500.0, angle14 - 180, thePlayer6[player_data[i10]])
		set i10 = i10 + 1
	endloop
	call print("initializing Magicians..done!")
endfunction

// function PrintingHelper.printError
function printError takes string msg5 returns nothing
	local integer i16
	set i16 = 0
	loop
		exitwhen i16 > 12
		call DisplayTimedTextToPlayer(Player(i16), 0.0, 0.0, 60.0, "|cffFF0000[ERROR] - " + msg5 + "|r")
		set i16 = i16 + 1
	endloop
endfunction

// function Obelisk.Obelisk.StaticEntityModule.getPosition
function getPosition2 takes integer this returns real
	set temp_real = position3[this]
	set wurst__tupleReturnVar_real_1 = position31[this]
	set wurst__tupleReturnVar_real_2 = position32[this]
	return temp_real
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.getPosition
function getPosition4 takes integer this returns real
	set temp_real = position1[this]
	set wurst__tupleReturnVar_real_1 = position11[this]
	set wurst__tupleReturnVar_real_2 = position12[this]
	return temp_real
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.getPosition
function getPosition3 takes integer this returns real
	set temp_real = position2[this]
	set wurst__tupleReturnVar_real_1 = position21[this]
	set wurst__tupleReturnVar_real_2 = position22[this]
	return temp_real
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.EntityModule.StaticEntityModule.getPosition
function getPosition1 takes integer this returns real
	set temp_real = position4[this]
	set wurst__tupleReturnVar_real_1 = position41[this]
	set wurst__tupleReturnVar_real_2 = position42[this]
	return temp_real
endfunction

// interface dispatch function getPosition for interface Entity
function getPosition takes integer this, integer thistype returns real
	if thistype <= 2 then
		if thistype <= 1 then
			return getPosition1(this)
		else
			return getPosition2(this)
		endif
	else
		if thistype <= 3 then
			return getPosition3(this)
		else
			return getPosition4(this)
		endif
	endif
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.EntityModule.StaticEntityModule.getRadius
function getRadius1 takes integer this returns real
	return radius24[this]
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.getRadius
function getRadius3 takes integer this returns real
	return radius19[this]
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.getRadius
function getRadius4 takes integer this returns real
	return radius16[this]
endfunction

// function Obelisk.Obelisk.StaticEntityModule.getRadius
function getRadius2 takes integer this returns real
	return radius22[this]
endfunction

// interface dispatch function getRadius for interface Entity
function getRadius takes integer this, integer thistype returns real
	if thistype <= 2 then
		if thistype <= 1 then
			return getRadius1(this)
		else
			return getRadius2(this)
		endif
	else
		if thistype <= 3 then
			return getRadius3(this)
		else
			return getRadius4(this)
		endif
	endif
endfunction

// function OptimizedMaths.distanceBetweenCoords
function distanceBetweenCoords takes real x111, real y111, real x211, real y211 returns real
	return SquareRoot((x211 - x111) * (x211 - x111) + (y211 - y111) * (y211 - y111))
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.touchesCircle
function touchesCircle4 takes integer this, integer e8, integer e81 returns boolean
	local real v8
	local real v81
	local real v82
	set v8 = getPosition(e8, e81)
	set v81 = wurst__tupleReturnVar_real_1
	set v82 = wurst__tupleReturnVar_real_2
	return distanceBetweenCoords(position1[this], position11[this], v8, v81) < radius16[this] + getRadius(e8, e81)
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.touchesCircle
function touchesCircle3 takes integer this, integer e11, integer e111 returns boolean
	local real v13
	local real v131
	local real v132
	set v13 = getPosition(e11, e111)
	set v131 = wurst__tupleReturnVar_real_1
	set v132 = wurst__tupleReturnVar_real_2
	return distanceBetweenCoords(position2[this], position21[this], v13, v131) < radius19[this] + getRadius(e11, e111)
endfunction

// function Obelisk.Obelisk.StaticEntityModule.touchesCircle
function touchesCircle2 takes integer this, integer e14, integer e141 returns boolean
	local real v17
	local real v171
	local real v172
	set v17 = getPosition(e14, e141)
	set v171 = wurst__tupleReturnVar_real_1
	set v172 = wurst__tupleReturnVar_real_2
	return distanceBetweenCoords(position3[this], position31[this], v17, v171) < radius22[this] + getRadius(e14, e141)
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.EntityModule.StaticEntityModule.touchesCircle
function touchesCircle1 takes integer this, integer e18, integer e181 returns boolean
	local real v25
	local real v251
	local real v252
	set v25 = getPosition(e18, e181)
	set v251 = wurst__tupleReturnVar_real_1
	set v252 = wurst__tupleReturnVar_real_2
	return distanceBetweenCoords(position4[this], position41[this], v25, v251) < radius24[this] + getRadius(e18, e181)
endfunction

// interface dispatch function touchesCircle for interface Entity
function touchesCircle takes integer this, integer thistype, integer e2, integer e21 returns boolean
	if thistype <= 2 then
		if thistype <= 1 then
			return touchesCircle1(this, e2, e21)
		else
			return touchesCircle2(this, e2, e21)
		endif
	else
		if thistype <= 3 then
			return touchesCircle3(this, e2, e21)
		else
			return touchesCircle4(this, e2, e21)
		endif
	endif
endfunction

// function Obelisk.Obelisk.onCollision
function onCollision2 takes integer this, integer e12, integer e121 returns nothing
endfunction

function addReals takes real this, real this2, real this3, real x36, real y36, real z13 returns real
	local real temp77
	local real temp78
	local real temp79
	set temp77 = this + x36
	set temp78 = this2 + y36
	set temp79 = this3 + z13
	set temp_real = temp77
	set wurst__tupleReturnVar_real_1 = temp78
	set wurst__tupleReturnVar_real_2 = temp79
	return temp_real
endfunction

// function Obelisk.Obelisk.StaticEntityModule.isStatic
function isStatic2 takes integer this returns boolean
	return true
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.EntityModule.isStatic
function isStatic1 takes integer this returns boolean
	return false
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.isStatic
function isStatic3 takes integer this returns boolean
	return false
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.isStatic
function isStatic4 takes integer this returns boolean
	return false
endfunction

// interface dispatch function isStatic for interface Entity
function isStatic takes integer this, integer thistype returns boolean
	if thistype <= 2 then
		if thistype <= 1 then
			return isStatic1(this)
		else
			return isStatic2(this)
		endif
	else
		if thistype <= 3 then
			return isStatic3(this)
		else
			return isStatic4(this)
		endif
	endif
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.EntityModule.getVelocity
function getVelocity1 takes integer this returns real
	set temp_real = velocity4[this]
	set wurst__tupleReturnVar_real_1 = velocity41[this]
	set wurst__tupleReturnVar_real_2 = velocity42[this]
	return temp_real
endfunction

// function Obelisk.Obelisk.getVelocity
function getVelocity2 takes integer this returns real
	local real temp38
	local real temp39
	local real temp40
	set temp38 = 0.0
	set temp39 = 0.0
	set temp40 = 0.0
	set temp_real = temp38
	set wurst__tupleReturnVar_real_1 = temp39
	set wurst__tupleReturnVar_real_2 = temp40
	return temp_real
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.getVelocity
function getVelocity4 takes integer this returns real
	set temp_real = velocity2[this]
	set wurst__tupleReturnVar_real_1 = velocity21[this]
	set wurst__tupleReturnVar_real_2 = velocity22[this]
	return temp_real
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.getVelocity
function getVelocity3 takes integer this returns real
	set temp_real = velocity3[this]
	set wurst__tupleReturnVar_real_1 = velocity31[this]
	set wurst__tupleReturnVar_real_2 = velocity32[this]
	return temp_real
endfunction

// interface dispatch function getVelocity for interface Entity
function getVelocity takes integer this, integer thistype returns real
	if thistype <= 2 then
		if thistype <= 1 then
			return getVelocity1(this)
		else
			return getVelocity2(this)
		endif
	else
		if thistype <= 3 then
			return getVelocity3(this)
		else
			return getVelocity4(this)
		endif
	endif
endfunction

// function OptimizedMaths.angleBetweenCoords
function angleBetweenCoords takes real x110, real y110, real x210, real y210 returns real
	return Atan2(y210 - y110, x210 - x110)
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.EntityModule.StaticEntityModule.getOwner
function getOwner1 takes integer this returns player
	return owner8[this]
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.getOwner
function getOwner4 takes integer this returns player
	return owner2[this]
endfunction

// function Obelisk.Obelisk.StaticEntityModule.getOwner
function getOwner2 takes integer this returns player
	return owner6[this]
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.getOwner
function getOwner3 takes integer this returns player
	return owner4[this]
endfunction

// interface dispatch function getOwner for interface Entity
function getOwner takes integer this, integer thistype returns player
	if thistype <= 2 then
		if thistype <= 1 then
			return getOwner1(this)
		else
			return getOwner2(this)
		endif
	else
		if thistype <= 3 then
			return getOwner3(this)
		else
			return getOwner4(this)
		endif
	endif
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.setVelocity
function setVelocity3 takes integer this, real v11, real v111, real v112 returns nothing
	local real v22
	local real v221
	local real v222
	set v22 = v11
	set v221 = v111
	set v222 = v112
	set velocity3[this] = v22
	set velocity31[this] = v221
	set velocity32[this] = v222
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.EntityModule.setVelocity
function setVelocity1 takes integer this, real v20, real v201, real v202 returns nothing
	local real v23
	local real v231
	local real v232
	set v23 = v20
	set v231 = v201
	set v232 = v202
	set velocity4[this] = v23
	set velocity41[this] = v231
	set velocity42[this] = v232
endfunction

// function Obelisk.Obelisk.setVelocity
function setVelocity2 takes integer this, real v15, real v151, real v152 returns nothing
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.setVelocity
function setVelocity4 takes integer this, real v6, real v61, real v62 returns nothing
	local real v21
	local real v211
	local real v212
	set v21 = v6
	set v211 = v61
	set v212 = v62
	set velocity2[this] = v21
	set velocity21[this] = v211
	set velocity22[this] = v212
endfunction

// interface dispatch function setVelocity for interface Entity
function setVelocity takes integer this, integer thistype, real v3, real v31, real v32 returns nothing
	if thistype <= 2 then
		if thistype <= 1 then
			call setVelocity1(this, v3, v31, v32)
		else
			call setVelocity2(this, v3, v31, v32)
		endif
	else
		if thistype <= 3 then
			call setVelocity3(this, v3, v31, v32)
		else
			call setVelocity4(this, v3, v31, v32)
		endif
	endif
endfunction

// function ArcaneMissile.ArcaneMissile.onDeath
function onDeath takes integer this returns nothing
	local integer i6
	local integer temp5
	local integer e5
	local integer e51
	local real v5
	local real v51
	local real v52
	local real angle4
	set i6 = 0
	set temp5 = entityCount - 1
	loop
		exitwhen i6 > temp5
		set e5 = entity[nodes[i6]]
		set e51 = entity1[nodes[i6]]
		set v5 = getPosition(e5, e51)
		set v51 = wurst__tupleReturnVar_real_1
		set v52 = wurst__tupleReturnVar_real_2
		if getOwner(e5, e51) != owner2[this] and  not isStatic(e5, e51) and distanceBetweenCoords(position1[this], position11[this], v5, v51) <= EXPLOSION_AOE then
			set angle4 = angleBetweenCoords(position1[this], position11[this], v5, v51)
			call setVelocity(e5, e51, addReals(getVelocity(e5, e51), wurst__tupleReturnVar_real_1, wurst__tupleReturnVar_real_2, Cos(angle4) * speed4[this], Sin(angle4) * speed4[this], 25.0), wurst__tupleReturnVar_real_1, wurst__tupleReturnVar_real_2)
		endif
		set i6 = i6 + 1
	endloop
endfunction

// function ArcaneMissile.ArcaneMissile.onCollision
function onCollision4 takes integer this, integer e6, integer e61 returns nothing
	if getOwner(e6, e61) != owner2[this] then
		call onDeath(this)
		set done1[this] = true
	endif
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.getType
function getType4 takes integer this returns integer
	return TYPE_MISSILE
endfunction

// function Magician.Magician.getType
function getType1 takes integer this returns integer
	return TYPE_MAGICIAN
endfunction

// function Obelisk.Obelisk.getType
function getType2 takes integer this returns integer
	return TYPE_OBELISK
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.getType
function getType3 takes integer this returns integer
	return TYPE_MISSILE
endfunction

// interface dispatch function getType for interface Entity
function getType takes integer this, integer thistype returns integer
	if thistype <= 2 then
		if thistype <= 1 then
			return getType1(this)
		else
			return getType2(this)
		endif
	else
		if thistype <= 3 then
			return getType3(this)
		else
			return getType4(this)
		endif
	endif
endfunction

function length1 takes real this, real this2, real this3 returns real
	return SquareRoot(this * this + this2 * this2 + this3 * this3)
endfunction

function trim takes real this, real this2, real this3, real value16 returns real
	local real result3
	local real result31
	local real result32
	set result3 = this
	set result31 = this2
	set result32 = this3
	if result3 >  - value16 and result3 < value16 then
		set result3 = 0.0
	endif
	if result31 >  - value16 and result31 < value16 then
		set result31 = 0.0
	endif
	if result32 >  - value16 and result32 < value16 then
		set result32 = 0.0
	endif
	set temp_real = result3
	set wurst__tupleReturnVar_real_1 = result31
	set wurst__tupleReturnVar_real_2 = result32
	return temp_real
endfunction

function dot takes real this, real this2, real this3, real v34, real v341, real v342 returns real
	return this * v34 + this2 * v341 + this3 * v342
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.getMass
function getMass4 takes integer this returns real
	return 999999.0
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.getMass
function getMass3 takes integer this returns real
	return 999999.0
endfunction

// function Obelisk.Obelisk.StaticEntityModule.getMass
function getMass2 takes integer this returns real
	return 999999.0
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.getMass
function getMass1 takes integer this returns real
	return mass[this]
endfunction

// interface dispatch function getMass for interface Entity
function getMass takes integer this, integer thistype returns real
	if thistype <= 2 then
		if thistype <= 1 then
			return getMass1(this)
		else
			return getMass2(this)
		endif
	else
		if thistype <= 3 then
			return getMass3(this)
		else
			return getMass4(this)
		endif
	endif
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.setPosition
function setPosition3 takes integer this, real v14, real v141, real v142 returns nothing
	set position2[this] = v14
	set position21[this] = v141
	set position22[this] = v142
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.EntityModule.StaticEntityModule.setPosition
function setPosition1 takes integer this, real v26, real v261, real v262 returns nothing
	set position4[this] = v26
	set position41[this] = v261
	set position42[this] = v262
endfunction

// function Obelisk.Obelisk.StaticEntityModule.setPosition
function setPosition2 takes integer this, real v18, real v181, real v182 returns nothing
	set position3[this] = v18
	set position31[this] = v181
	set position32[this] = v182
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.setPosition
function setPosition4 takes integer this, real v9, real v91, real v92 returns nothing
	set position1[this] = v9
	set position11[this] = v91
	set position12[this] = v92
endfunction

// interface dispatch function setPosition for interface Entity
function setPosition takes integer this, integer thistype, real v4, real v41, real v42 returns nothing
	if thistype <= 2 then
		if thistype <= 1 then
			call setPosition1(this, v4, v41, v42)
		else
			call setPosition2(this, v4, v41, v42)
		endif
	else
		if thistype <= 3 then
			call setPosition3(this, v4, v41, v42)
		else
			call setPosition4(this, v4, v41, v42)
		endif
	endif
endfunction

function scale4 takes real this, real this2, real this3, real factor1 returns real
	local real temp86
	local real temp87
	local real temp88
	set temp86 = this * factor1
	set temp87 = this2 * factor1
	set temp88 = this3 * factor1
	set temp_real = temp86
	set wurst__tupleReturnVar_real_1 = temp87
	set wurst__tupleReturnVar_real_2 = temp88
	return temp_real
endfunction

function norm takes real this, real this2, real this3 returns real
	local real len
	local real x37
	local real y37
	local real z14
	local real temp95
	local real temp96
	local real temp97
	set len = length1(this, this2, this3)
	set x37 = 0
	set y37 = 0
	set z14 = 0
	if len != 0.0 then
		set x37 = this / len
		set y37 = this2 / len
		set z14 = this3 / len
	endif
	set temp95 = x37
	set temp96 = y37
	set temp97 = z14
	set temp_real = temp95
	set wurst__tupleReturnVar_real_1 = temp96
	set wurst__tupleReturnVar_real_2 = temp97
	return temp_real
endfunction

function sub takes real this, real this2, real this3, real v29, real v291, real v292 returns real
	local real temp80
	local real temp81
	local real temp82
	set temp80 = this - v29
	set temp81 = this2 - v291
	set temp82 = this3 - v292
	set temp_real = temp80
	set wurst__tupleReturnVar_real_1 = temp81
	set wurst__tupleReturnVar_real_2 = temp82
	return temp_real
endfunction

// function Magician.Magician.getWalkVelocity
function getWalkVelocity1 takes integer this returns real
	set temp_real = walkVelocity[this]
	set wurst__tupleReturnVar_real_1 = walkVelocity1[this]
	set wurst__tupleReturnVar_real_2 = walkVelocity2[this]
	return temp_real
endfunction

// function Obelisk.Obelisk.StaticEntityModule.getWalkVelocity
function getWalkVelocity2 takes integer this returns real
	local real temp41
	local real temp42
	local real temp43
	set temp41 = 0.0
	set temp42 = 0.0
	set temp43 = 0.0
	set temp_real = temp41
	set wurst__tupleReturnVar_real_1 = temp42
	set wurst__tupleReturnVar_real_2 = temp43
	return temp_real
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.getWalkVelocity
function getWalkVelocity4 takes integer this returns real
	local real temp9
	local real temp10
	local real temp11
	set temp9 = 0.0
	set temp10 = 0.0
	set temp11 = 0.0
	set temp_real = temp9
	set wurst__tupleReturnVar_real_1 = temp10
	set wurst__tupleReturnVar_real_2 = temp11
	return temp_real
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.getWalkVelocity
function getWalkVelocity3 takes integer this returns real
	local real temp25
	local real temp26
	local real temp27
	set temp25 = 0.0
	set temp26 = 0.0
	set temp27 = 0.0
	set temp_real = temp25
	set wurst__tupleReturnVar_real_1 = temp26
	set wurst__tupleReturnVar_real_2 = temp27
	return temp_real
endfunction

// interface dispatch function getWalkVelocity for interface Entity
function getWalkVelocity takes integer this, integer thistype returns real
	if thistype <= 2 then
		if thistype <= 1 then
			return getWalkVelocity1(this)
		else
			return getWalkVelocity2(this)
		endif
	else
		if thistype <= 3 then
			return getWalkVelocity3(this)
		else
			return getWalkVelocity4(this)
		endif
	endif
endfunction

function add2 takes real this, real this2, real this3, real v27, real v271, real v272 returns real
	local real temp71
	local real temp72
	local real temp73
	set temp71 = this + v27
	set temp72 = this2 + v271
	set temp73 = this3 + v272
	set temp_real = temp71
	set wurst__tupleReturnVar_real_1 = temp72
	set wurst__tupleReturnVar_real_2 = temp73
	return temp_real
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.physicCollision
function physicCollision takes integer this, integer e16, integer e161 returns nothing
	local real vRad
	local real vPos
	local real vPos1
	local real vPos2
	local real vVel
	local real vVel1
	local real vVel2
	local real delta3
	local real delta31
	local real delta32
	local real r34
	local real dist21
	local real d20
	local real mtd
	local real mtd1
	local real mtd2
	local real temp54
	local real temp55
	local real temp56
	local real im1
	local real im2
	local real v19
	local real v191
	local real v192
	local real vn
	local real i9
	local real impulse
	local real impulse1
	local real impulse2
	set vRad = getRadius(e16, e161)
	set vPos = getPosition(e16, e161)
	set vPos1 = wurst__tupleReturnVar_real_1
	set vPos2 = wurst__tupleReturnVar_real_2
	set vVel = getVelocity(e16, e161)
	set vVel1 = wurst__tupleReturnVar_real_1
	set vVel2 = wurst__tupleReturnVar_real_2
	set delta3 = sub(position4[this], position41[this], position42[this], vPos, vPos1, vPos2)
	set delta31 = wurst__tupleReturnVar_real_1
	set delta32 = wurst__tupleReturnVar_real_2
	set r34 = radius24[this] + vRad
	set dist21 = dot(delta3, delta31, delta32, delta3, delta31, delta32)
	if length1(trim(vVel, vVel1, vVel2, 0.5), wurst__tupleReturnVar_real_1, wurst__tupleReturnVar_real_2) == 0.0 then
		set vVel = getWalkVelocity(e16, e161)
		set vVel1 = wurst__tupleReturnVar_real_1
		set vVel2 = wurst__tupleReturnVar_real_2
	endif
	if dist21 > r34 * r34 then
		return
	endif
	set d20 = length1(delta3, delta31, delta32)
	if d20 == 0.0 then
		set d20 = vRad + radius24[this] - 1.0
		set temp54 = vRad + radius24[this]
		set temp55 = 0.0
		set temp56 = 0.0
		set delta3 = temp54
		set delta31 = temp55
		set delta32 = temp56
	endif
	set mtd = scale4(delta3, delta31, delta32, (radius24[this] + vRad - d20) / d20)
	set mtd1 = wurst__tupleReturnVar_real_1
	set mtd2 = wurst__tupleReturnVar_real_2
	set im1 = 1 / mass[this]
	set im2 = 1 / getMass(e16, e161)
	set position4[this] = add2(position4[this], position41[this], position42[this], scale4(mtd, mtd1, mtd2, im1 / (im1 + im2)), wurst__tupleReturnVar_real_1, wurst__tupleReturnVar_real_2)
	set position41[this] = wurst__tupleReturnVar_real_1
	set position42[this] = wurst__tupleReturnVar_real_2
	if  not isStatic(e16, e161) then
		call setPosition(e16, e161, sub(vPos, vPos1, vPos2, scale4(mtd, mtd1, mtd2, im2 / (im1 + im2)), wurst__tupleReturnVar_real_1, wurst__tupleReturnVar_real_2), wurst__tupleReturnVar_real_1, wurst__tupleReturnVar_real_2)
	endif
	set v19 = sub(velocity4[this], velocity41[this], velocity42[this], vVel, vVel1, vVel2)
	set v191 = wurst__tupleReturnVar_real_1
	set v192 = wurst__tupleReturnVar_real_2
	set vn = dot(v19, v191, v192, norm(mtd, mtd1, mtd2), wurst__tupleReturnVar_real_1, wurst__tupleReturnVar_real_2)
	if vn > 0.0 then
		return
	endif
	set i9 = (1.0 + restitution[this]) *  - 1 * vn / (im1 + im2)
	set impulse = scale4(norm(mtd, mtd1, mtd2), wurst__tupleReturnVar_real_1, wurst__tupleReturnVar_real_2, i9)
	set impulse1 = wurst__tupleReturnVar_real_1
	set impulse2 = wurst__tupleReturnVar_real_2
	set velocity4[this] = add2(velocity4[this], velocity41[this], velocity42[this], scale4(impulse, impulse1, impulse2, im1), wurst__tupleReturnVar_real_1, wurst__tupleReturnVar_real_2)
	set velocity41[this] = wurst__tupleReturnVar_real_1
	set velocity42[this] = wurst__tupleReturnVar_real_2
	if  not isStatic(e16, e161) then
		call setVelocity(e16, e161, sub(vVel, vVel1, vVel2, scale4(impulse, impulse1, impulse2, im2), wurst__tupleReturnVar_real_1, wurst__tupleReturnVar_real_2), wurst__tupleReturnVar_real_1, wurst__tupleReturnVar_real_2)
	endif
endfunction

// function Magician.Magician.onCollision
function onCollision1 takes integer this, integer e15, integer e151 returns nothing
	local integer typ
	set typ = getType(e15, e151)
	if typ == TYPE_OBELISK or typ == TYPE_MAGICIAN then
		call physicCollision(this, e15, e151)
	endif
endfunction

// function ArcaneMissile.ArcaneMissile.onCollision
function onCollision3 takes integer this, integer e9, integer e91 returns nothing
	local real v10
	local real v101
	local real v102
	local real angle10
	if getOwner(e9, e91) != owner4[this] then
		if  not isStatic(e9, e91) then
			call print("not owner not static")
			set v10 = getPosition(e9, e91)
			set v101 = wurst__tupleReturnVar_real_1
			set v102 = wurst__tupleReturnVar_real_2
			set angle10 = angleBetweenCoords(position2[this], position21[this], v10, v101)
			call setVelocity(e9, e91, addReals(getVelocity(e9, e91), wurst__tupleReturnVar_real_1, wurst__tupleReturnVar_real_2, Cos(angle10) * speed7[this], Sin(angle10) * speed7[this], 25.0), wurst__tupleReturnVar_real_1, wurst__tupleReturnVar_real_2)
		endif
		set done2[this] = true
	endif
endfunction

// interface dispatch function onCollision for interface Entity
function onCollision takes integer this, integer thistype, integer e3, integer e31 returns nothing
	if thistype <= 2 then
		if thistype <= 1 then
			call onCollision1(this, e3, e31)
		else
			call onCollision2(this, e3, e31)
		endif
	else
		if thistype <= 3 then
			call onCollision3(this, e3, e31)
		else
			call onCollision4(this, e3, e31)
		endif
	endif
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.touchesBox
function touchesBox3 takes integer this, integer e10, integer e101 returns boolean
	local integer temp31
	local integer temp32
	local integer temp33
	local integer temp34
	local real v12
	local real v121
	local real v122
	set temp31 = e10
	set temp32 = e101
	set temp33 = 0
	set temp34 = 0
	if temp31 == temp33 and temp32 == temp34 then
		call printError("touchesBox, Entity e == null")
	endif
	set v12 = getPosition(e10, e101)
	set v121 = wurst__tupleReturnVar_real_1
	set v122 = wurst__tupleReturnVar_real_2
	return (v12 - position2[this]) * (v12 - position2[this]) + (v121 - position21[this]) * (v121 - position21[this]) < (radius19[this] + getRadius(e10, e101)) * (radius19[this] + getRadius(e10, e101))
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.touchesBox
function touchesBox4 takes integer this, integer e7, integer e71 returns boolean
	local integer temp15
	local integer temp16
	local integer temp17
	local integer temp18
	local real v7
	local real v71
	local real v72
	set temp15 = e7
	set temp16 = e71
	set temp17 = 0
	set temp18 = 0
	if temp15 == temp17 and temp16 == temp18 then
		call printError("touchesBox, Entity e == null")
	endif
	set v7 = getPosition(e7, e71)
	set v71 = wurst__tupleReturnVar_real_1
	set v72 = wurst__tupleReturnVar_real_2
	return (v7 - position1[this]) * (v7 - position1[this]) + (v71 - position11[this]) * (v71 - position11[this]) < (radius16[this] + getRadius(e7, e71)) * (radius16[this] + getRadius(e7, e71))
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.EntityModule.StaticEntityModule.touchesBox
function touchesBox1 takes integer this, integer e17, integer e171 returns boolean
	local integer temp66
	local integer temp67
	local integer temp68
	local integer temp69
	local real v24
	local real v241
	local real v242
	set temp66 = e17
	set temp67 = e171
	set temp68 = 0
	set temp69 = 0
	if temp66 == temp68 and temp67 == temp69 then
		call printError("touchesBox, Entity e == null")
	endif
	set v24 = getPosition(e17, e171)
	set v241 = wurst__tupleReturnVar_real_1
	set v242 = wurst__tupleReturnVar_real_2
	return (v24 - position4[this]) * (v24 - position4[this]) + (v241 - position41[this]) * (v241 - position41[this]) < (radius24[this] + getRadius(e17, e171)) * (radius24[this] + getRadius(e17, e171))
endfunction

// function Obelisk.Obelisk.StaticEntityModule.touchesBox
function touchesBox2 takes integer this, integer e13, integer e131 returns boolean
	local integer temp47
	local integer temp48
	local integer temp49
	local integer temp50
	local real v16
	local real v161
	local real v162
	set temp47 = e13
	set temp48 = e131
	set temp49 = 0
	set temp50 = 0
	if temp47 == temp49 and temp48 == temp50 then
		call printError("touchesBox, Entity e == null")
	endif
	set v16 = getPosition(e13, e131)
	set v161 = wurst__tupleReturnVar_real_1
	set v162 = wurst__tupleReturnVar_real_2
	return (v16 - position3[this]) * (v16 - position3[this]) + (v161 - position31[this]) * (v161 - position31[this]) < (radius22[this] + getRadius(e13, e131)) * (radius22[this] + getRadius(e13, e131))
endfunction

// interface dispatch function touchesBox for interface Entity
function touchesBox takes integer this, integer thistype, integer e, integer e1 returns boolean
	if thistype <= 2 then
		if thistype <= 1 then
			return touchesBox1(this, e, e1)
		else
			return touchesBox2(this, e, e1)
		endif
	else
		if thistype <= 3 then
			return touchesBox3(this, e, e1)
		else
			return touchesBox4(this, e, e1)
		endif
	endif
endfunction

// function GameLoop.checkCollisions
function checkCollisions takes nothing returns nothing
	local integer i5
	local integer temp
	local integer i21
	local integer temp1
	local integer a9
	local integer a91
	local integer b5
	local integer b51
	set i5 = 0
	set temp = entityCount - 2
	loop
		exitwhen i5 > temp
		set i21 = i5 + 1
		set temp1 = entityCount - 1
		loop
			exitwhen i21 > temp1
			set a9 = entity[nodes[i5]]
			set a91 = entity1[nodes[i5]]
			set b5 = entity[nodes[i21]]
			set b51 = entity1[nodes[i21]]
			if touchesBox(a9, a91, b5, b51) and touchesCircle(a9, a91, b5, b51) then
				call onCollision(a9, a91, b5, b51)
				call onCollision(b5, b51, a9, a91)
			endif
			set i21 = i21 + 1
		endloop
		set i5 = i5 + 1
	endloop
endfunction

// destroy function for Magician
function Magician_Magician_destroy takes integer this returns nothing
	call RemoveUnit(actor1[this])
	call RemoveUnit(actor1[this])
	if Magician_Magician_nextFree[this] >= 0 then
		call BJDebugMsg("Double free of Magician")
	else
		set Magician_Magician_nextFree[this] = Magician_Magician_firstFree
		set Magician_Magician_firstFree = this
	endif
endfunction

// function Magician.Magician.remove
function remove1 takes integer this returns nothing
	call Magician_Magician_destroy(this)
endfunction

function destr takes effect this returns nothing
	call DestroyEffect(this)
endfunction

// destroy function for MovingEffect
function MovingEffect_MovingEffect_destroy takes integer this returns nothing
	if fx2[this] != null then
		call destr(fx2[this])
	endif
	call KillUnit(dummy[this])
	if MovingEffect_MovingEffect_nextFree[this] >= 0 then
		call BJDebugMsg("Double free of MovingEffect")
	else
		set MovingEffect_MovingEffect_nextFree[this] = MovingEffect_MovingEffect_firstFree
		set MovingEffect_MovingEffect_firstFree = this
	endif
endfunction

// destroy function for ArcaneMissile
function ArcaneMissile_ArcaneMissile_destroy takes integer this returns nothing
	call MovingEffect_MovingEffect_destroy(fx[this])
	if ArcaneMissile_ArcaneMissile_nextFree[this] >= 0 then
		call BJDebugMsg("Double free of ArcaneMissile")
	else
		set ArcaneMissile_ArcaneMissile_nextFree[this] = ArcaneMissile_ArcaneMissile_firstFree
		set ArcaneMissile_ArcaneMissile_firstFree = this
	endif
endfunction

// function ArcaneMissile.ArcaneMissile.remove
function remove4 takes integer this returns nothing
	call ArcaneMissile_ArcaneMissile_destroy(this)
endfunction

// destroy function for ArcaneMissile
function ArcaneMissile_ArcaneMissile_destroy1 takes integer this returns nothing
	call MovingEffect_MovingEffect_destroy(fx1[this])
	if ArcaneMissile_ArcaneMissile_nextFree1[this] >= 0 then
		call BJDebugMsg("Double free of ArcaneMissile")
	else
		set ArcaneMissile_ArcaneMissile_nextFree1[this] = ArcaneMissile_ArcaneMissile_firstFree1
		set ArcaneMissile_ArcaneMissile_firstFree1 = this
	endif
endfunction

// function ArcaneMissile.ArcaneMissile.remove
function remove3 takes integer this returns nothing
	call ArcaneMissile_ArcaneMissile_destroy1(this)
endfunction

// destroy function for Obelisk
function Obelisk_Obelisk_destroy takes integer this returns nothing
	if Obelisk_Obelisk_nextFree[this] >= 0 then
		call BJDebugMsg("Double free of Obelisk")
	else
		set Obelisk_Obelisk_nextFree[this] = Obelisk_Obelisk_firstFree
		set Obelisk_Obelisk_firstFree = this
	endif
endfunction

// function Obelisk.Obelisk.remove
function remove2 takes integer this returns nothing
	call Obelisk_Obelisk_destroy(this)
endfunction

// interface dispatch function remove for interface Entity
function remove takes integer this, integer thistype returns nothing
	if thistype <= 2 then
		if thistype <= 1 then
			call remove1(this)
		else
			call remove2(this)
		endif
	else
		if thistype <= 3 then
			call remove3(this)
		else
			call remove4(this)
		endif
	endif
endfunction

// destroy function for Node
function Node_Node_destroy takes integer this returns nothing
	call remove(entity[this], entity1[this])
	if Node_Node_nextFree[this] >= 0 then
		call BJDebugMsg("Double free of Node")
	else
		set Node_Node_nextFree[this] = Node_Node_firstFree
		set Node_Node_firstFree = this
	endif
endfunction

// function EntityManagement.removeEntity
function removeEntity takes integer index34 returns nothing
	call Node_Node_destroy(nodes[index34])
	set nodes[index34] = nodes[entityCount - 1]
	set entityCount = entityCount - 1
endfunction

// function ArcaneMissile.ArcaneMissile.checkArenaBounds
function checkArenaBounds takes integer this returns nothing
	if position1[this] < minX2[currentArena] or position1[this] > maxX2[currentArena] or position11[this] < minY2[currentArena] or position11[this] > maxY2[currentArena] then
		set done1[this] = true
	endif
endfunction

// function MovingEffect.MovingEffect.getZAngle
function getZAngle takes integer this returns real
	return zang[this]
endfunction

function setFacing takes unit this, real deg returns unit
	call SetUnitFacing(this, deg)
	return this
endfunction

// function MovingEffect.MovingEffect.setXYAngle
function setXYAngle takes integer this, real value19 returns integer
	call setFacing(dummy[this], value19 * bj_RADTODEG)
	return this
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.calculatePosition
function calculatePosition5 takes integer this returns nothing
	set position1[this] = position1[this] + Cos(angle6[this]) * speed4[this]
	set position11[this] = position11[this] + Sin(angle6[this]) * speed4[this]
	set position12[this] = position12[this] + Sin(getZAngle(fx[this])) * speed4[this]
	set position1[this] = add2(position1[this], position11[this], position12[this], velocity2[this], velocity21[this], velocity22[this])
	set position11[this] = wurst__tupleReturnVar_real_1
	set position12[this] = wurst__tupleReturnVar_real_2
	call checkArenaBounds(this)
	call setXYAngle(fx[this], angle6[this])
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.calculatePosition
function calculatePosition4 takes integer this returns nothing
	call calculatePosition5(this)
	if decay[this] then
		set time[this] = time[this] - 0.03125
		call print(toString(time[this]))
	endif
	if time[this] < 0 then
		call onDeath(this)
		set done1[this] = true
	endif
endfunction

// function Obelisk.Obelisk.calculatePosition
function calculatePosition2 takes integer this returns nothing
endfunction

// function ArcaneMissile.ArcaneMissile.onDeath
function onDeath1 takes integer this returns nothing
endfunction

function addAbility takes unit this, integer abil1 returns unit
	call UnitAddAbility(this, abil1)
	return this
endfunction

function addEffect takes unit this, string fx4, string attachment returns effect
	return AddSpecialEffectTarget(fx4, this, attachment)
endfunction

// function MovingEffect.MovingEffect.setFx
function setFx takes integer this, string newpath returns integer
	if fx2[this] != null then
		call destr(fx2[this])
	endif
	if newpath == "" then
		set fx2[this] = null
	else
		set fx2[this] = addEffect(dummy[this], newpath, "origin")
	endif
	set sfxPath[this] = newpath
	return this
endfunction

function removeAbility takes unit this, integer abil2 returns unit
	call UnitRemoveAbility(this, abil2)
	return this
endfunction

function setPos takes unit this, real x45, real y45 returns unit
	call SetUnitX(this, x45)
	call SetUnitY(this, y45)
	return this
endfunction

function getX1 takes unit this returns real
	return GetUnitX(this)
endfunction

// function MovingEffect.MovingEffect.getX
function getX takes integer this returns real
	return getX1(dummy[this])
endfunction

function getY1 takes unit this returns real
	return GetUnitY(this)
endfunction

// function MovingEffect.MovingEffect.getY
function getY takes integer this returns real
	return getY1(dummy[this])
endfunction

// function MovingEffect.MovingEffect.setXYAngleInstant
function setXYAngleInstant takes integer this, real value20 returns integer
	local real x41
	local real y41
	set x41 = getX(this)
	set y41 = getY(this)
	call destr(fx2[this])
	call RemoveUnit(dummy[this])
	set dummy[this] = setPos(addAbility(removeAbility(addAbility(CreateUnit(DUMMY_PLAYER, DUMMY_UNIT_ID, x41, y41, value20 * bj_RADTODEG), 1097691750), 1097691750), 1097625443), x41, y41)
	call setFx(this, sfxPath[this])
	return this
endfunction

// constructor function for MovingEffect
function MovingEffect_MovingEffect_construct1 takes real x40, real y40, real facing9, string fxpath4 returns integer
	local integer this
	if MovingEffect_MovingEffect_firstFree > 0 then
		set this = MovingEffect_MovingEffect_firstFree
		set MovingEffect_MovingEffect_firstFree = MovingEffect_MovingEffect_nextFree[this]
	else
		set MovingEffect_MovingEffect_maxIndex = MovingEffect_MovingEffect_maxIndex + 1
		set this = MovingEffect_MovingEffect_maxIndex
	endif
	set MovingEffect_MovingEffect_nextFree[this] = -1
	set fx2[this] = null
	set zang[this] = 0.0
	set red24[this] = 255
	set green24[this] = 255
	set blue24[this] = 255
	set alpha2[this] = 255
	set abil[this] = 0
	set dummy[this] = setPos(addAbility(removeAbility(addAbility(CreateUnit(DUMMY_PLAYER, DUMMY_UNIT_ID, x40, y40, facing9 * bj_RADTODEG), 1097691750), 1097691750), 1097625443), x40, y40)
	call setFx(this, fxpath4)
	return this
endfunction

// function ArcaneMissile.ArcaneMissile.fixProjectile
function fixProjectile takes integer this, real side returns nothing
	local integer ef
	call setXYAngleInstant(fx1[this], angle9[this])
	set ef = MovingEffect_MovingEffect_construct1(position2[this], position21[this], side, "Abilities\\Spells\\Human\\Defend\\DefendCaster.mdl")
	call MovingEffect_MovingEffect_destroy(ef)
endfunction

// function ArcaneMissile.ArcaneMissile.checkArenaBounds
function checkArenaBounds3 takes integer this returns nothing
	if position2[this] - radius19[this] < minX2[currentArena] then
		call print("unter minx")
		set position2[this] = minX2[currentArena] + radius19[this]
		set velocity3[this] =  - velocity3[this]
		set velocity31[this] = velocity31[this]
		set angle9[this] = 180 * DEGTORAD - angle9[this]
		call fixProjectile(this, 0.0)
	else
		if position2[this] + radius19[this] > maxX2[currentArena] then
			call print("ueber maxX")
			set position2[this] = maxX2[currentArena] - radius19[this]
			set velocity3[this] =  - velocity3[this]
			set velocity31[this] = velocity31[this]
			set angle9[this] = 180 * DEGTORAD - angle9[this]
			call fixProjectile(this, 180 * DEGTORAD)
		endif
	endif
	if position21[this] - radius19[this] < minY2[currentArena] then
		call print("unter miny")
		set position21[this] = minY2[currentArena] + radius19[this]
		set velocity3[this] = velocity3[this]
		set velocity31[this] =  - velocity31[this]
		set angle9[this] = 360 * DEGTORAD - angle9[this]
		call fixProjectile(this, 90 * DEGTORAD)
	else
		if position21[this] + radius19[this] > maxY2[currentArena] then
			call print("ueber maxY")
			set position21[this] = maxY2[currentArena] - radius19[this]
			set velocity3[this] = velocity3[this]
			set velocity31[this] =  - velocity31[this]
			set angle9[this] = 360 * DEGTORAD - angle9[this]
			call fixProjectile(this, 270 * DEGTORAD)
		endif
	endif
	if position22[this] - radius19[this] <= 0.0 then
		set position22[this] = radius19[this] + 1.0
		set velocity32[this] =  - velocity32[this]
	endif
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.calculatePosition
function calculatePosition6 takes integer this returns nothing
	set position2[this] = position2[this] + Cos(angle9[this]) * speed7[this]
	set position21[this] = position21[this] + Sin(angle9[this]) * speed7[this]
	set position22[this] = position22[this] + Sin(getZAngle(fx1[this])) * speed7[this]
	set position2[this] = add2(position2[this], position21[this], position22[this], velocity3[this], velocity31[this], velocity32[this])
	set position21[this] = wurst__tupleReturnVar_real_1
	set position22[this] = wurst__tupleReturnVar_real_2
	call checkArenaBounds3(this)
	call setXYAngle(fx1[this], angle9[this])
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.calculatePosition
function calculatePosition3 takes integer this returns nothing
	call calculatePosition6(this)
	if decay1[this] then
		set time1[this] = time1[this] - 0.03125
		call print(toString(time1[this]))
	endif
	if time1[this] < 0 then
		call onDeath1(this)
		set done2[this] = true
	endif
endfunction

function scaleXY takes real this, real this2, real this3, real factor2 returns real
	local real temp89
	local real temp90
	local real temp91
	set temp89 = this * factor2
	set temp90 = this2 * factor2
	set temp91 = this3
	set temp_real = temp89
	set wurst__tupleReturnVar_real_1 = temp90
	set wurst__tupleReturnVar_real_2 = temp91
	return temp_real
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.checkArenaBounds
function checkArenaBounds7 takes integer this returns nothing
	if position4[this] - radius24[this] < minX2[currentArena] then
		call print("unter minx")
		set position4[this] = minX2[currentArena] + radius24[this]
		set velocity4[this] =  - (velocity4[this] * restitution[this])
		set velocity41[this] = velocity41[this] * restitution[this]
	else
		if position4[this] + radius24[this] > maxX2[currentArena] then
			call print("ueber maxX")
			set position4[this] = maxX2[currentArena] - radius24[this]
			set velocity4[this] =  - (velocity4[this] * restitution[this])
			set velocity41[this] = velocity41[this] * restitution[this]
		endif
	endif
	if position41[this] - radius24[this] < minY2[currentArena] then
		call print("unter miny")
		set position41[this] = minY2[currentArena] + radius24[this]
		set velocity4[this] = velocity4[this] * restitution[this]
		set velocity41[this] =  - (velocity41[this] * restitution[this])
	else
		if position41[this] + radius24[this] > maxY2[currentArena] then
			call print("ueber maxY")
			set position41[this] = maxY2[currentArena] - radius24[this]
			set velocity4[this] = velocity4[this] * restitution[this]
			set velocity41[this] =  - (velocity41[this] * restitution[this])
		endif
	endif
	if position42[this] - radius24[this] <= 0.0 then
		set position42[this] = radius24[this] + 1.0
		set velocity42[this] =  - (velocity42[this] * (restitution[this] / 2))
	endif
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.calculateVelocity
function calculateVelocity1 takes integer this returns nothing
	call checkArenaBounds7(this)
	if affectedByGravity[this] then
		set velocity4[this] = add2(velocity4[this], velocity41[this], velocity42[this], gravity, gravity1, gravity2)
		set velocity41[this] = wurst__tupleReturnVar_real_1
		set velocity42[this] = wurst__tupleReturnVar_real_2
	endif
	set velocity4[this] = scaleXY(velocity4[this], velocity41[this], velocity42[this], terrainFriction[this] * frictionModifier[this])
	set velocity41[this] = wurst__tupleReturnVar_real_1
	set velocity42[this] = wurst__tupleReturnVar_real_2
	set velocity4[this] = trim(velocity4[this], velocity41[this], velocity42[this], 0.3)
	set velocity41[this] = wurst__tupleReturnVar_real_1
	set velocity42[this] = wurst__tupleReturnVar_real_2
endfunction

// function Magician.Magician.calculateVelocity
function calculateVelocity takes integer this returns nothing
	call calculateVelocity1(this)
	set walkVelocity[this] = sub(add2(position4[this], position41[this], position42[this], velocity4[this], velocity41[this], velocity42[this]), wurst__tupleReturnVar_real_1, wurst__tupleReturnVar_real_2, position4[this], position41[this], position42[this])
	set walkVelocity1[this] = wurst__tupleReturnVar_real_1
	set walkVelocity2[this] = wurst__tupleReturnVar_real_2
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.doActor
function doActor takes integer this returns nothing
	local real x31
	local real y31
	local real z11
	set x31 = GetUnitX(actor1[this])
	set y31 = GetUnitY(actor1[this])
	set z11 = getZ(position4[this], position41[this]) + GetUnitFlyHeight(actor1[this])
	set position4[this] = x31
	set position41[this] = y31
	set position42[this] = z11
endfunction

// function Magician.Magician.calculatePosition
function calculatePosition1 takes integer this returns nothing
	call doActor(this)
	call calculateVelocity(this)
	set position4[this] = add2(position4[this], position41[this], position42[this], velocity4[this], velocity41[this], velocity42[this])
	set position41[this] = wurst__tupleReturnVar_real_1
	set position42[this] = wurst__tupleReturnVar_real_2
	if GetTerrainType(position4[this], position41[this]) == 1097101927 then
		call SetUnitState(actor1[this], UNIT_STATE_LIFE, GetUnitState(actor1[this], UNIT_STATE_LIFE) - 0.5)
	endif
endfunction

// interface dispatch function calculatePosition for interface Entity
function calculatePosition takes integer this, integer thistype returns nothing
	if thistype <= 2 then
		if thistype <= 1 then
			call calculatePosition1(this)
		else
			call calculatePosition2(this)
		endif
	else
		if thistype <= 3 then
			call calculatePosition3(this)
		else
			call calculatePosition4(this)
		endif
	endif
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.isDone
function isDone3 takes integer this returns boolean
	return done2[this]
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.isDone
function isDone4 takes integer this returns boolean
	return done1[this]
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.EntityModule.StaticEntityModule.isDone
function isDone1 takes integer this returns boolean
	return done4[this]
endfunction

// function Obelisk.Obelisk.StaticEntityModule.isDone
function isDone2 takes integer this returns boolean
	return done3[this]
endfunction

// interface dispatch function isDone for interface Entity
function isDone takes integer this, integer thistype returns boolean
	if thistype <= 2 then
		if thistype <= 1 then
			return isDone1(this)
		else
			return isDone2(this)
		endif
	else
		if thistype <= 3 then
			return isDone3(this)
		else
			return isDone4(this)
		endif
	endif
endfunction

// function GameLoop.calculatePositions
function calculatePositions takes nothing returns nothing
	local integer i3
	set i3 = entityCount - 1
	loop
		exitwhen i3 < 0
		if isDone(entity[nodes[i3]], entity1[nodes[i3]]) then
			call removeEntity(i3)
		else
			call calculatePosition(entity[nodes[i3]], entity1[nodes[i3]])
		endif
		set i3 = i3 - 1
	endloop
endfunction

function setY1 takes unit this, real y44 returns unit
	call SetUnitY(this, y44)
	return this
endfunction

// function MovingEffect.MovingEffect.setY
function setY takes integer this, real y42 returns integer
	call setY1(dummy[this], y42)
	return this
endfunction

function setFlyHeight takes unit this, real height3, real rate2 returns unit
	call SetUnitFlyHeight(this, height3, rate2)
	return this
endfunction

// function MovingEffect.MovingEffect.setZ
function setZ takes integer this, real z15 returns integer
	call setFlyHeight(dummy[this], z15, 0.0)
	return this
endfunction

// function ArcaneMissile.ArcaneMissile.onLoop
function onLoop takes integer this returns nothing
endfunction

function setX1 takes unit this, real x44 returns unit
	call SetUnitX(this, x44)
	return this
endfunction

// function MovingEffect.MovingEffect.setX
function setX takes integer this, real x42 returns integer
	call setX1(dummy[this], x42)
	return this
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.updatePosition
function updatePosition4 takes integer this returns nothing
	call setX(fx[this], position1[this])
	call setY(fx[this], position11[this])
	call setZ(fx[this], position12[this])
	call onLoop(this)
endfunction

// function ArcaneMissile.ArcaneMissile.onLoop
function onLoop1 takes integer this returns nothing
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.updatePosition
function updatePosition3 takes integer this returns nothing
	call setX(fx1[this], position2[this])
	call setY(fx1[this], position21[this])
	call setZ(fx1[this], position22[this])
	call onLoop1(this)
endfunction

// function Obelisk.Obelisk.updatePosition
function updatePosition2 takes integer this returns nothing
	call SetUnitX(actor[this], position3[this])
	call SetUnitY(actor[this], position31[this])
	call SetUnitFlyHeight(actor[this], position32[this] - getZ(position3[this], position31[this]), 0.0)
endfunction

// interface dispatch function updatePosition for interface Entity
function updatePosition takes integer this, integer thistype returns nothing
	if thistype <= 2 then
		if thistype <= 1 then
			call updatePosition1(this)
		else
			call updatePosition2(this)
		endif
	else
		if thistype <= 3 then
			call updatePosition3(this)
		else
			call updatePosition4(this)
		endif
	endif
endfunction

// function GameLoop.updatePositions
function updatePositions takes nothing returns nothing
	local integer i4
	set i4 = entityCount - 1
	loop
		exitwhen i4 < 0
		call updatePosition(entity[nodes[i4]], entity1[nodes[i4]])
		set i4 = i4 - 1
	endloop
endfunction

// function GameLoop.mainLoop
function mainLoop takes nothing returns nothing
	if entityCount > 0 then
		call calculatePositions()
		call updatePositions()
		call checkCollisions()
	endif
endfunction

// function GameLoop.startGame
function startGame takes nothing returns boolean
	local timer t14
	set t14 = CreateTimer()
	call TimerStart(t14, 0.03125, true, function mainLoop)
	call print("Game started...")
	set t14 = null
	return true
endfunction

// constructor function for Arena
function Arena_Arena_construct takes real maxX3, real maxY3, real minX3, real minY3 returns integer
	local integer this
	local real x26
	local real y26
	local real temp35
	local real temp36
	local real temp37
	local integer i7
	if Arena_Arena_firstFree > 0 then
		set this = Arena_Arena_firstFree
		set Arena_Arena_firstFree = Arena_Arena_nextFree[this]
	else
		set Arena_Arena_maxIndex = Arena_Arena_maxIndex + 1
		set this = Arena_Arena_maxIndex
	endif
	set Arena_Arena_nextFree[this] = -1
	set maxX2[this] = maxX3
	set maxY2[this] = maxY3
	set minX2[this] = minX3
	set minY2[this] = minY3
	set halfX[this] = minX3 + (maxX3 - minX3) / 2
	set halfY[this] = minY3 + (maxY3 - minY3) / 2
	set x26 = maxX3 - minX3 - (maxX3 - minX3) / 2
	set y26 = maxY3 - minY3 - (maxY3 - minY3) / 2
	set temp35 = x26
	set temp36 = y26
	set temp37 = getZ(x26, y26)
	set centerPoint[this] = temp35
	set centerPoint1[this] = temp36
	set centerPoint2[this] = temp37
	call print("X " + R2S(x26) + " Y " + R2S(y26) + " Z " + R2S(centerPoint2[this]))
	set i7 = 0
	loop
		exitwhen i7 > 12
		call FogModifierStart(CreateFogModifierRect(Player(i7), FOG_OF_WAR_VISIBLE, worldRect, true, false))
		set i7 = i7 + 1
	endloop
	return this
endfunction

// function Arena.initArena
function initArena takes nothing returns nothing
	set currentArena = Arena_Arena_construct(1152.0, 1152.0,  - 1152.0,  - 1152.0)
endfunction

// constructor function for PlayerData
function PlayerData_PlayerData_construct takes player p6, integer id8, integer realId returns integer
	local integer this
	if PlayerData_PlayerData_firstFree > 0 then
		set this = PlayerData_PlayerData_firstFree
		set PlayerData_PlayerData_firstFree = PlayerData_PlayerData_nextFree[this]
	else
		set PlayerData_PlayerData_maxIndex = PlayerData_PlayerData_maxIndex + 1
		set this = PlayerData_PlayerData_maxIndex
	endif
	set PlayerData_PlayerData_nextFree[this] = -1
	set thePlayer6[this] = p6
	set id7[this] = id8
	set gameId[this] = realId
	set magician[this] = magician[this]
	set coloredName[this] = player_colors[id8] + GetPlayerName(p6) + "|r"
	return this
endfunction

function toString2 takes integer this returns string
	return I2S(this)
endfunction

// function PlayerData.initPlayers
function initPlayers takes nothing returns nothing
	local integer j
	local integer i11
	call print("initializing PlayerData..")
	call print("    codes..")
	set player_colors[0] = "|CFFFF0303"
	set player_colors[1] = "|CFF0042FF"
	set player_colors[2] = "|CFF1CB619"
	set player_colors[3] = "|CFF540081"
	set player_colors[4] = "|CFFFFFF01"
	set player_colors[5] = "|CFFFE8A0E"
	set player_colors[6] = "|CFF20C000"
	set player_colors[7] = "|CFFE55BB0"
	set player_colors[8] = "|CFF959697"
	set player_colors[9] = "|CFF7EBFF1"
	set player_colors[10] = "|CFF106246"
	set player_colors[11] = "|CFF4E2A04"
	call print("    codes..done!")
	call print("    player_datas..")
	set j = 0
	set i11 = 0
	loop
		exitwhen i11 > 11
		if GetPlayerSlotState(Player(i11)) == PLAYER_SLOT_STATE_PLAYING then
			set player_data[i11] = PlayerData_PlayerData_construct(Player(i11), i11, j)
			set players_data_row[j] = player_data[i11]
			set j = j + 1
		endif
		set i11 = i11 + 1
	endloop
	call print("    player_datas..done!")
	call print("    player_count..")
	set player_count = j
	call print("player_count = " + toString2(player_count))
	call print("    player_count..done!")
	call print("initializing PlayerData..done!")
endfunction

// function Obelisk.Obelisk.StaticEntityModule.setUpEntity
function setUpEntity2 takes integer this, real x28, real y28, real z9, real radius23, player owner7 returns nothing
	local real temp44
	local real temp45
	local real temp46
	set radius22[this] = radius23
	set temp44 = x28
	set temp45 = y28
	set temp46 = z9
	set position3[this] = temp44
	set position31[this] = temp45
	set position32[this] = temp46
	set owner6[this] = owner7
	call debugPrint("setUp:" + toString1(position3[this], position31[this], position32[this]), 0)
endfunction

// constructor function for Obelisk
function Obelisk_Obelisk_construct takes real x27, real y27, real facing6 returns integer
	local integer this
	if Obelisk_Obelisk_firstFree > 0 then
		set this = Obelisk_Obelisk_firstFree
		set Obelisk_Obelisk_firstFree = Obelisk_Obelisk_nextFree[this]
	else
		set Obelisk_Obelisk_maxIndex = Obelisk_Obelisk_maxIndex + 1
		set this = Obelisk_Obelisk_maxIndex
	endif
	set Obelisk_Obelisk_nextFree[this] = -1
	set done3[this] = false
	call setUpEntity2(this, x27, y27, 0.0, COLLISION_SIZE2, Player(13))
	set actor[this] = CreateUnit(owner6[this], OBELISK_ID, x27, y27, facing6)
	call addEntity(this, 2)
	return this
endfunction

// function Obelisk.initObelisks
function initObelisks takes nothing returns nothing
	local real distanceFromMid
	local real distanceFromPoint
	local integer i8
	local real angle13
	local real x29
	local real y29
	call print("initializing Obelisks..")
	set distanceFromMid = (MAX_TILES - 1.5) * TILE_SECTION_LENGTH
	set distanceFromPoint = TILE_SECTION_LENGTH
	set i8 = 0
	loop
		exitwhen i8 > 3
		set angle13 = i8 * 90.0 + 45.0
		set x29 = pPX(0.0, distanceFromMid, angle13)
		set y29 = pPY(0.0, distanceFromMid, angle13)
		call Obelisk_Obelisk_construct(pPX(x29, distanceFromPoint, angle13 + 90.0), pPY(y29, distanceFromPoint, angle13 + 90.0), 0.0)
		call Obelisk_Obelisk_construct(pPX(x29, distanceFromPoint, angle13 - 90.0), pPY(y29, distanceFromPoint, angle13 - 90.0), 0.0)
		set i8 = i8 + 1
	endloop
	call print("initializing Obelisks..done!")
endfunction

// function GameInit.initGame
function initGame takes nothing returns nothing
	call print("Starting Init")
	call initPlayers()
	call initObelisks()
	call initArena()
	call initMagicians()
	if startGame() then
		call print("Finished Init - Game started")
	else
		call printError("gamestart malfunction")
	endif
endfunction

// init block for WPackage GameInit
function GameInit_init takes nothing returns nothing
	local trigger t13
	set t13 = CreateTrigger()
	call TriggerRegisterTimerEvent(t13, 0.2, false)
	call TriggerAddAction(t13, function initGame)
	set t13 = null
endfunction

// constructor function for MovingEffect
function MovingEffect_MovingEffect_construct takes real x39, real y39, real facing8 returns integer
	local integer this
	if MovingEffect_MovingEffect_firstFree > 0 then
		set this = MovingEffect_MovingEffect_firstFree
		set MovingEffect_MovingEffect_firstFree = MovingEffect_MovingEffect_nextFree[this]
	else
		set MovingEffect_MovingEffect_maxIndex = MovingEffect_MovingEffect_maxIndex + 1
		set this = MovingEffect_MovingEffect_maxIndex
	endif
	set MovingEffect_MovingEffect_nextFree[this] = -1
	set fx2[this] = null
	set zang[this] = 0.0
	set red24[this] = 255
	set green24[this] = 255
	set blue24[this] = 255
	set alpha2[this] = 255
	set abil[this] = 0
	set dummy[this] = setPos(addAbility(removeAbility(addAbility(CreateUnit(DUMMY_PLAYER, DUMMY_UNIT_ID, x39, y39, facing8 * bj_RADTODEG), 1097691750), 1097691750), 1097625443), x39, y39)
	return this
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.setUpEntity
function setUpEntity takes integer this, real x22, real y22, real z5, real radius17, player owner3 returns nothing
	local real temp12
	local real temp13
	local real temp14
	set radius16[this] = radius17
	set temp12 = x22
	set temp13 = y22
	set temp14 = z5
	set position1[this] = temp12
	set position11[this] = temp13
	set position12[this] = temp14
	set owner2[this] = owner3
	call debugPrint("setUp:" + toString1(position1[this], position11[this], position12[this]), 0)
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.setUpMissile
function setUpMissile takes integer this, real x21, real y21, real z3, string fxpath1, real angle7, real speed5, real radius15, player p2 returns nothing
	call setUpEntity(this, x21, y21, z3, radius15, p2)
	set fx[this] = MovingEffect_MovingEffect_construct(x21, y21, angle7)
	call setZ(fx[this], z3)
	call setFx(fx[this], fxpath1)
	call setXYAngle(fx[this], angle7)
	set speed4[this] = speed5
	set angle6[this] = angle7
endfunction

function setScale1 takes unit this, real scale6 returns unit
	call SetUnitScale(this, scale6, scale6, scale6)
	return this
endfunction

// function MovingEffect.MovingEffect.setScale
function setScale takes integer this, real value18 returns integer
	call setScale1(dummy[this], value18)
	return this
endfunction

// constructor function for ArcaneMissile
function ArcaneMissile_ArcaneMissile_construct takes real x19, real y19, real z2, string fxpath, real angle3, real speed3, real radius14, player p1 returns integer
	local integer this
	local real temp2
	local real temp3
	local real temp4
	if ArcaneMissile_ArcaneMissile_firstFree > 0 then
		set this = ArcaneMissile_ArcaneMissile_firstFree
		set ArcaneMissile_ArcaneMissile_firstFree = ArcaneMissile_ArcaneMissile_nextFree[this]
	else
		set ArcaneMissile_ArcaneMissile_maxIndex = ArcaneMissile_ArcaneMissile_maxIndex + 1
		set this = ArcaneMissile_ArcaneMissile_maxIndex
	endif
	set ArcaneMissile_ArcaneMissile_nextFree[this] = -1
	set done1[this] = false
	set temp2 = 0.0
	set temp3 = 0.0
	set temp4 = 0.0
	set velocity2[this] = temp2
	set velocity21[this] = temp3
	set velocity22[this] = temp4
	set decay[this] = true
	call setUpMissile(this, x19, y19, z2, fxpath, angle3, speed3, radius14, p1)
	set time[this] = 1.2
	call addEntity(this, 4)
	call setScale(fx[this], 0.7)
	call print("added")
	return this
endfunction

// function MovingEffect.MovingEffect.setZAngle
function setZAngle takes integer this, real value21 returns integer
	local integer i12
	set i12 = R2I(value21 * bj_RADTODEG + 90.5)
	set zang[this] = value21
	if i12 >= 180 then
		set i12 = 179
	else
		if i12 < 0 then
			set i12 = 0
		endif
	endif
	call SetUnitAnimationByIndex(dummy[this], i12)
	return this
endfunction

// function ArcaneMissile.ArcaneMissile.onCast
function onCast takes nothing returns nothing
	local unit u
	local real angle5
	local integer n
	call print("casted")
	set u = GetSpellAbilityUnit()
	set angle5 = angleBetweenCoords(GetUnitX(u), GetUnitY(u), GetSpellTargetX(), GetSpellTargetY())
	call print(R2S(angle5) + "  " + R2S(angle5 * RADTODEG))
	set n = ArcaneMissile_ArcaneMissile_construct(GetUnitX(u), GetUnitY(u), 45.0, "Doodads\\Terrain\\CliffDoodad\\Waterfall\\Waterfall.mdl", angle5, 32.0, COLLISION_SIZE, GetOwningPlayer(u))
	call setZAngle(fx[n], 90.0)
	set u = null
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.checkArenaBounds
function checkArenaBounds1 takes integer this returns nothing
	local real temp6
	local real temp7
	local real temp8
	if position1[this] < minX2[currentArena] or position1[this] > maxX2[currentArena] or position11[this] < minY2[currentArena] or position11[this] > maxY2[currentArena] then
		call printError("Entity out of Arenabounds")
		set position1[this] = 0.0
		set position11[this] = 0.0
		set temp6 = 0.0
		set temp7 = 0.0
		set temp8 = 0.0
		set velocity2[this] = temp6
		set velocity21[this] = temp7
		set velocity22[this] = temp8
	endif
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.checkArenaBounds
function checkArenaBounds2 takes integer this returns nothing
	if position1[this] < minX2[currentArena] or position1[this] > maxX2[currentArena] or position11[this] < minY2[currentArena] or position11[this] > maxY2[currentArena] then
		call printError("Entity out of Arenabounds")
		set position1[this] = 0.0
		set position11[this] = 0.0
	endif
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.isStatic
function isStatic5 takes integer this returns boolean
	return true
endfunction

// function ArcaneMissile.ArcaneMissile.SpellModule.condition
function condition1 takes nothing returns boolean
	return GetSpellAbilityId() == spellId
endfunction

// function ArcaneMissile.ArcaneMissile.SpellModule.setUpSpellEvent
function setUpSpellEvent takes integer id5 returns nothing
	set t15 = CreateTrigger()
	call TriggerRegisterAnyUnitEventBJ(t15, EVENT_PLAYER_UNIT_SPELL_EFFECT)
	call TriggerAddCondition(t15, Condition(function condition1))
	call TriggerAddAction(t15, function onCast)
	set spellId = id5
endfunction

// init block for WPackage ArcaneMissile
function ArcaneMissile_init takes nothing returns nothing
	call setUpSpellEvent(1362112561)
	call print("setup event Q")
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.setUpEntity
function setUpEntity1 takes integer this, real x25, real y25, real z8, real radius21, player owner5 returns nothing
	local real temp28
	local real temp29
	local real temp30
	set radius19[this] = radius21
	set temp28 = x25
	set temp29 = y25
	set temp30 = z8
	set position2[this] = temp28
	set position21[this] = temp29
	set position22[this] = temp30
	set owner4[this] = owner5
	call debugPrint("setUp:" + toString1(position2[this], position21[this], position22[this]), 0)
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.setUpMissile
function setUpMissile1 takes integer this, real x24, real y24, real z7, string fxpath3, real angle12, real speed8, real radius20, player p4 returns nothing
	call setUpEntity1(this, x24, y24, z7, radius20, p4)
	set fx1[this] = MovingEffect_MovingEffect_construct(x24, y24, angle12)
	call setZ(fx1[this], z7)
	call setFx(fx1[this], fxpath3)
	call setXYAngle(fx1[this], angle12)
	set speed7[this] = speed8
	set angle9[this] = angle12
endfunction

// constructor function for ArcaneMissile
function ArcaneMissile_ArcaneMissile_construct1 takes real x23, real y23, real z6, string fxpath2, real angle8, real speed6, real radius18, player p3 returns integer
	local integer this
	local real temp19
	local real temp20
	local real temp21
	if ArcaneMissile_ArcaneMissile_firstFree1 > 0 then
		set this = ArcaneMissile_ArcaneMissile_firstFree1
		set ArcaneMissile_ArcaneMissile_firstFree1 = ArcaneMissile_ArcaneMissile_nextFree1[this]
	else
		set ArcaneMissile_ArcaneMissile_maxIndex1 = ArcaneMissile_ArcaneMissile_maxIndex1 + 1
		set this = ArcaneMissile_ArcaneMissile_maxIndex1
	endif
	set ArcaneMissile_ArcaneMissile_nextFree1[this] = -1
	set done2[this] = false
	set temp19 = 0.0
	set temp20 = 0.0
	set temp21 = 0.0
	set velocity3[this] = temp19
	set velocity31[this] = temp20
	set velocity32[this] = temp21
	set decay1[this] = true
	call setUpMissile1(this, x23, y23, z6, fxpath2, angle8, speed6, radius18, p3)
	set time1[this] = 4.0
	call addEntity(this, 3)
	call print("added")
	return this
endfunction

// function ArcaneMissile.ArcaneMissile.onCast
function onCast1 takes nothing returns nothing
	local unit u1
	local real angle11
	call print("casted")
	set u1 = GetSpellAbilityUnit()
	set angle11 = angleBetweenCoords(GetUnitX(u1), GetUnitY(u1), GetSpellTargetX(), GetSpellTargetY())
	call print(R2S(angle11) + "  " + R2S(angle11 * RADTODEG))
	call ArcaneMissile_ArcaneMissile_construct1(GetUnitX(u1), GetUnitY(u1), 45.0, "war3mapImported\\BouncingMissile.mdx", angle11, 32.0, COLLISION_SIZE1, GetOwningPlayer(u1))
	set u1 = null
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.checkArenaBounds
function checkArenaBounds4 takes integer this returns nothing
	local real temp22
	local real temp23
	local real temp24
	if position2[this] < minX2[currentArena] or position2[this] > maxX2[currentArena] or position21[this] < minY2[currentArena] or position21[this] > maxY2[currentArena] then
		call printError("Entity out of Arenabounds")
		set position2[this] = 0.0
		set position21[this] = 0.0
		set temp22 = 0.0
		set temp23 = 0.0
		set temp24 = 0.0
		set velocity3[this] = temp22
		set velocity31[this] = temp23
		set velocity32[this] = temp24
	endif
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.checkArenaBounds
function checkArenaBounds5 takes integer this returns nothing
	if position2[this] < minX2[currentArena] or position2[this] > maxX2[currentArena] or position21[this] < minY2[currentArena] or position21[this] > maxY2[currentArena] then
		call printError("Entity out of Arenabounds")
		set position2[this] = 0.0
		set position21[this] = 0.0
	endif
endfunction

// function ArcaneMissile.ArcaneMissile.TimedMissile.PlainMissile.EntityModule.StaticEntityModule.isStatic
function isStatic6 takes integer this returns boolean
	return true
endfunction

// function ArcaneMissile.ArcaneMissile.SpellModule.condition
function condition2 takes nothing returns boolean
	return GetSpellAbilityId() == spellId1
endfunction

// function ArcaneMissile.ArcaneMissile.SpellModule.setUpSpellEvent
function setUpSpellEvent1 takes integer id6 returns nothing
	set t16 = CreateTrigger()
	call TriggerRegisterAnyUnitEventBJ(t16, EVENT_PLAYER_UNIT_SPELL_EFFECT)
	call TriggerAddCondition(t16, Condition(function condition2))
	call TriggerAddAction(t16, function onCast1)
	set spellId1 = id6
endfunction

// init block for WPackage ArcaneMissile
function ArcaneMissile_init1 takes nothing returns nothing
	call setUpSpellEvent1(1362112562)
	call print("setup event Q2")
endfunction

// function Trig_Magic_Watercurrent_Actions
function Trig_Magic_Watercurrent_Actions takes nothing returns nothing
	call AddSpecialEffectLocBJ(GetRectCenter(GetPlayableMapRect()), "Abilities\\Spells\\Human\\Defend\\DefendCaster.mdl")
endfunction

// function InitTrig_Magic_Watercurrent
function InitTrig_Magic_Watercurrent takes nothing returns nothing
	set gg_trg_Magic_Watercurrent = CreateTrigger()
	call TriggerAddAction(gg_trg_Magic_Watercurrent, function Trig_Magic_Watercurrent_Actions)
endfunction

// init block for WPackage KillingSystem
function KillingSystem_init takes nothing returns nothing
	set multiMsgs[0] = ""
	set multiMsgs[1] = "Doublekill"
	set multiMsgs[2] = "Multikill"
	set spreeMsgs[0] = ""
	set spreeMsgs[1] = "killing spree"
	set spreeMsgs[2] = "dominating"
	set spreeMsgs[3] = "unstobable"
	set spreeMsgs[4] = "godlike"
	set spreeMsgs[5] = "wicked sick"
	set spreeMsgs[6] = "holy shit"
	set spreeMsgs[7] = "ludacris kill"
	set spreeMsgs[8] = "somebody stop me"
	set spreeMsgs[9] = "nice"
	set spreeMsgs[10] = "fair enough"
endfunction

function length takes string this returns integer
	return StringLength(this)
endfunction

function substring takes string this, integer start1, integer end1 returns string
	return SubString(this, start1, end1)
endfunction

// constructor function for DmgTexttag
function DmgTexttag_DmgTexttag_construct takes unit u2, real dmg, integer dmgType returns integer
	local integer this
	local string s8
	if DmgTexttag_DmgTexttag_firstFree > 0 then
		set this = DmgTexttag_DmgTexttag_firstFree
		set DmgTexttag_DmgTexttag_firstFree = DmgTexttag_DmgTexttag_nextFree[this]
	else
		set DmgTexttag_DmgTexttag_maxIndex = DmgTexttag_DmgTexttag_maxIndex + 1
		set this = DmgTexttag_DmgTexttag_maxIndex
	endif
	set DmgTexttag_DmgTexttag_nextFree[this] = -1
	set tt12[this] = CreateTextTag()
	set s8 = toString(dmg)
	loop
		exitwhen  not (substring(s8, length(s8) - 1, length(s8)) == "0" or substring(s8, length(s8) - 1, length(s8)) == ".")
		set s8 = substring(s8, 0, length(s8) - 1)
	endloop
	call SetTextTagText(tt12[this], s8, dmg / 400)
	call SetTextTagPos(tt12[this], GetUnitX(u2), GetUnitY(u2), 40.0)
	call SetTextTagVelocity(tt12[this], 0.0, 0.02)
	call SetTextTagLifespan(tt12[this], 2.0 + dmg / 10)
	call SetTextTagFadepoint(tt12[this], 0.2)
	call SetTextTagPermanent(tt12[this], false)
	return this
endfunction

// destroy function for DmgTexttag
function DmgTexttag_DmgTexttag_destroy takes integer this returns nothing
	if DmgTexttag_DmgTexttag_nextFree[this] >= 0 then
		call BJDebugMsg("Double free of DmgTexttag")
	else
		set DmgTexttag_DmgTexttag_nextFree[this] = DmgTexttag_DmgTexttag_firstFree
		set DmgTexttag_DmgTexttag_firstFree = this
	endif
endfunction

// destroy function for Arena
function Arena_Arena_destroy takes integer this returns nothing
	if Arena_Arena_nextFree[this] >= 0 then
		call BJDebugMsg("Double free of Arena")
	else
		set Arena_Arena_nextFree[this] = Arena_Arena_firstFree
		set Arena_Arena_firstFree = this
	endif
endfunction

// function Obelisk.Obelisk.StaticEntityModule.checkArenaBounds
function checkArenaBounds6 takes integer this returns nothing
	if position3[this] < minX2[currentArena] or position3[this] > maxX2[currentArena] or position31[this] < minY2[currentArena] or position31[this] > maxY2[currentArena] then
		call printError("Entity out of Arenabounds")
		set position3[this] = 0.0
		set position31[this] = 0.0
	endif
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.getActor
function getActor takes integer this returns unit
	return actor1[this]
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.getFriction
function getFriction takes integer this returns nothing
	if GetTerrainType(position4[this], position41[this]) == 1852400485 then
		set terrainFriction[this] = 0.8
	endif
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.EntityModule.checkArenaBounds
function checkArenaBounds8 takes integer this returns nothing
	local real temp57
	local real temp58
	local real temp59
	if position4[this] < minX2[currentArena] or position4[this] > maxX2[currentArena] or position41[this] < minY2[currentArena] or position41[this] > maxY2[currentArena] then
		call printError("Entity out of Arenabounds")
		set position4[this] = 0.0
		set position41[this] = 0.0
		set temp57 = 0.0
		set temp58 = 0.0
		set temp59 = 0.0
		set velocity4[this] = temp57
		set velocity41[this] = temp58
		set velocity42[this] = temp59
	endif
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.EntityModule.StaticEntityModule.getWalkVelocity
function getWalkVelocity5 takes integer this returns real
	local real temp60
	local real temp61
	local real temp62
	set temp60 = 0.0
	set temp61 = 0.0
	set temp62 = 0.0
	set temp_real = temp60
	set wurst__tupleReturnVar_real_1 = temp61
	set wurst__tupleReturnVar_real_2 = temp62
	return temp_real
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.EntityModule.StaticEntityModule.getMass
function getMass5 takes integer this returns real
	return 999999.0
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.EntityModule.StaticEntityModule.checkArenaBounds
function checkArenaBounds9 takes integer this returns nothing
	if position4[this] < minX2[currentArena] or position4[this] > maxX2[currentArena] or position41[this] < minY2[currentArena] or position41[this] > maxY2[currentArena] then
		call printError("Entity out of Arenabounds")
		set position4[this] = 0.0
		set position41[this] = 0.0
	endif
endfunction

// function Magician.Magician.PhysicsEntityUnitModule.PhysicsEntityModule.EntityModule.StaticEntityModule.isStatic
function isStatic7 takes integer this returns boolean
	return true
endfunction

// destroy function for PlayerData
function PlayerData_PlayerData_destroy takes integer this returns nothing
	if PlayerData_PlayerData_nextFree[this] >= 0 then
		call BJDebugMsg("Double free of PlayerData")
	else
		set PlayerData_PlayerData_nextFree[this] = PlayerData_PlayerData_firstFree
		set PlayerData_PlayerData_firstFree = this
	endif
endfunction

// constructor function for TerrainUtils
function TerrainUtils_TerrainUtils_construct takes nothing returns integer
	local integer this
	if TerrainUtils_TerrainUtils_firstFree > 0 then
		set this = TerrainUtils_TerrainUtils_firstFree
		set TerrainUtils_TerrainUtils_firstFree = TerrainUtils_TerrainUtils_nextFree[this]
	else
		set TerrainUtils_TerrainUtils_maxIndex = TerrainUtils_TerrainUtils_maxIndex + 1
		set this = TerrainUtils_TerrainUtils_maxIndex
	endif
	set TerrainUtils_TerrainUtils_nextFree[this] = -1
	return this
endfunction

// destroy function for TerrainUtils
function TerrainUtils_TerrainUtils_destroy takes integer this returns nothing
	if TerrainUtils_TerrainUtils_nextFree[this] >= 0 then
		call BJDebugMsg("Double free of TerrainUtils")
	else
		set TerrainUtils_TerrainUtils_nextFree[this] = TerrainUtils_TerrainUtils_firstFree
		set TerrainUtils_TerrainUtils_firstFree = this
	endif
endfunction

// init block for WPackage TerrainUtils
function TerrainUtils_init takes nothing returns nothing
	set minWorldX = GetRectMinX(worldRect)
	set minWorldY = GetRectMinY(worldRect)
	set maxWorldX = GetRectMaxX(worldRect)
	set maxWorldY = GetRectMaxY(worldRect)
endfunction

// function Trig_Untitled_Trigger_001_Actions
function Trig_Untitled_Trigger_001_Actions takes nothing returns nothing
endfunction

// function InitTrig_Untitled_Trigger_001
function InitTrig_Untitled_Trigger_001 takes nothing returns nothing
	set gg_trg_Untitled_Trigger_001 = CreateTrigger()
	call TriggerAddAction(gg_trg_Untitled_Trigger_001, function Trig_Untitled_Trigger_001_Actions)
endfunction

// function InitCustomTriggers
function InitCustomTriggers takes nothing returns nothing
	call InitTrig_Magic_Watercurrent()
	call InitTrig_Untitled_Trigger_001()
endfunction

// function InitCustomPlayerSlots
function InitCustomPlayerSlots takes nothing returns nothing
	call SetPlayerStartLocation(Player(0), 0)
	call ForcePlayerStartLocation(Player(0), 0)
	call SetPlayerColor(Player(0), ConvertPlayerColor(0))
	call SetPlayerRacePreference(Player(0), RACE_PREF_HUMAN)
	call SetPlayerRaceSelectable(Player(0), false)
	call SetPlayerController(Player(0), MAP_CONTROL_USER)
	call SetPlayerStartLocation(Player(1), 1)
	call ForcePlayerStartLocation(Player(1), 1)
	call SetPlayerColor(Player(1), ConvertPlayerColor(1))
	call SetPlayerRacePreference(Player(1), RACE_PREF_HUMAN)
	call SetPlayerRaceSelectable(Player(1), false)
	call SetPlayerController(Player(1), MAP_CONTROL_COMPUTER)
	call SetPlayerStartLocation(Player(2), 2)
	call ForcePlayerStartLocation(Player(2), 2)
	call SetPlayerColor(Player(2), ConvertPlayerColor(2))
	call SetPlayerRacePreference(Player(2), RACE_PREF_HUMAN)
	call SetPlayerRaceSelectable(Player(2), false)
	call SetPlayerController(Player(2), MAP_CONTROL_COMPUTER)
	call SetPlayerStartLocation(Player(3), 3)
	call ForcePlayerStartLocation(Player(3), 3)
	call SetPlayerColor(Player(3), ConvertPlayerColor(3))
	call SetPlayerRacePreference(Player(3), RACE_PREF_HUMAN)
	call SetPlayerRaceSelectable(Player(3), false)
	call SetPlayerController(Player(3), MAP_CONTROL_COMPUTER)
	call SetPlayerStartLocation(Player(4), 4)
	call ForcePlayerStartLocation(Player(4), 4)
	call SetPlayerColor(Player(4), ConvertPlayerColor(4))
	call SetPlayerRacePreference(Player(4), RACE_PREF_HUMAN)
	call SetPlayerRaceSelectable(Player(4), false)
	call SetPlayerController(Player(4), MAP_CONTROL_COMPUTER)
	call SetPlayerStartLocation(Player(5), 5)
	call ForcePlayerStartLocation(Player(5), 5)
	call SetPlayerColor(Player(5), ConvertPlayerColor(5))
	call SetPlayerRacePreference(Player(5), RACE_PREF_HUMAN)
	call SetPlayerRaceSelectable(Player(5), false)
	call SetPlayerController(Player(5), MAP_CONTROL_COMPUTER)
	call SetPlayerStartLocation(Player(6), 6)
	call ForcePlayerStartLocation(Player(6), 6)
	call SetPlayerColor(Player(6), ConvertPlayerColor(6))
	call SetPlayerRacePreference(Player(6), RACE_PREF_HUMAN)
	call SetPlayerRaceSelectable(Player(6), false)
	call SetPlayerController(Player(6), MAP_CONTROL_USER)
	call SetPlayerStartLocation(Player(7), 7)
	call ForcePlayerStartLocation(Player(7), 7)
	call SetPlayerColor(Player(7), ConvertPlayerColor(7))
	call SetPlayerRacePreference(Player(7), RACE_PREF_HUMAN)
	call SetPlayerRaceSelectable(Player(7), false)
	call SetPlayerController(Player(7), MAP_CONTROL_USER)
	call SetPlayerStartLocation(Player(8), 8)
	call ForcePlayerStartLocation(Player(8), 8)
	call SetPlayerColor(Player(8), ConvertPlayerColor(8))
	call SetPlayerRacePreference(Player(8), RACE_PREF_HUMAN)
	call SetPlayerRaceSelectable(Player(8), false)
	call SetPlayerController(Player(8), MAP_CONTROL_USER)
	call SetPlayerStartLocation(Player(9), 9)
	call ForcePlayerStartLocation(Player(9), 9)
	call SetPlayerColor(Player(9), ConvertPlayerColor(9))
	call SetPlayerRacePreference(Player(9), RACE_PREF_HUMAN)
	call SetPlayerRaceSelectable(Player(9), false)
	call SetPlayerController(Player(9), MAP_CONTROL_USER)
endfunction

// function InitCustomTeams
function InitCustomTeams takes nothing returns nothing
	call SetPlayerTeam(Player(0), 0)
	call SetPlayerTeam(Player(1), 0)
	call SetPlayerTeam(Player(2), 0)
	call SetPlayerTeam(Player(3), 0)
	call SetPlayerTeam(Player(4), 0)
	call SetPlayerTeam(Player(5), 0)
	call SetPlayerTeam(Player(6), 0)
	call SetPlayerTeam(Player(7), 0)
	call SetPlayerTeam(Player(8), 0)
	call SetPlayerTeam(Player(9), 1)
endfunction

// function InitAllyPriorities
function InitAllyPriorities takes nothing returns nothing
	call SetStartLocPrioCount(0, 4)
	call SetStartLocPrio(0, 0, 6, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(0, 1, 7, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(0, 2, 8, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(0, 3, 9, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrioCount(1, 9)
	call SetStartLocPrio(1, 0, 0, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(1, 1, 2, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(1, 2, 3, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(1, 3, 4, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(1, 4, 5, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(1, 5, 6, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(1, 6, 7, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(1, 7, 8, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(1, 8, 9, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrioCount(2, 9)
	call SetStartLocPrio(2, 0, 0, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(2, 1, 1, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(2, 2, 3, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(2, 3, 4, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(2, 4, 5, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(2, 5, 6, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(2, 6, 7, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(2, 7, 8, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(2, 8, 9, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrioCount(3, 9)
	call SetStartLocPrio(3, 0, 0, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(3, 1, 1, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(3, 2, 2, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(3, 3, 4, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(3, 4, 5, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(3, 5, 6, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(3, 6, 7, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(3, 7, 8, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(3, 8, 9, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrioCount(4, 9)
	call SetStartLocPrio(4, 0, 0, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(4, 1, 1, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(4, 2, 2, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(4, 3, 3, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(4, 4, 5, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(4, 5, 6, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(4, 6, 7, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(4, 7, 8, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(4, 8, 9, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrioCount(5, 9)
	call SetStartLocPrio(5, 0, 0, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(5, 1, 1, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(5, 2, 2, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(5, 3, 3, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(5, 4, 4, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(5, 5, 6, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(5, 6, 7, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(5, 7, 8, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(5, 8, 9, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrioCount(6, 4)
	call SetStartLocPrio(6, 0, 0, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(6, 1, 7, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(6, 2, 8, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(6, 3, 9, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrioCount(7, 4)
	call SetStartLocPrio(7, 0, 0, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(7, 1, 6, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(7, 2, 8, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(7, 3, 9, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrioCount(8, 4)
	call SetStartLocPrio(8, 0, 0, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(8, 1, 6, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(8, 2, 7, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(8, 3, 9, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrioCount(9, 4)
	call SetStartLocPrio(9, 0, 0, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(9, 1, 6, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(9, 2, 7, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(9, 3, 8, MAP_LOC_PRIO_HIGH)
endfunction

function wurst__init_globals takes nothing returns nothing
	local real temp108
	local real temp109
	local real temp110
	set udg_anzahl = 0
	set gg_trg_GameOptions = null
	set gg_trg_GH = null
	set gg_trg_GameInit = null
	set gg_trg_GameLoop = null
	set gg_trg_Entity = null
	set gg_trg_Node = null
	set gg_trg_EntityManagement = null
	set gg_trg_Missile = null
	set gg_trg_SpellData = null
	set gg_trg_SpellModule = null
	set gg_trg_Arcane_Missile = null
	set gg_trg_Bouncing_Missile = null
	set gg_trg_Magic_Watercurrent = null
	set gg_trg_KillingSystem = null
	set gg_trg_DmgTexttag = null
	set gg_trg_Arena = null
	set gg_trg_Obelisk = null
	set gg_trg_Magician = null
	set gg_trg_PlayerData = null
	set gg_trg_Item = null
	set gg_trg_OptimizedMaths = null
	set gg_trg_TextFunctions = null
	set gg_trg_TerrainUtils = null
	set gg_trg_Untitled_Trigger_001 = null
	set entityCount = 0
	set TYPE_MAGICIAN = 0
	set TYPE_OBELISK = 1
	set TYPE_MISSILE = 2
	set COLLISION_SIZE = 28.0
	set EXPLOSION_AOE = 128.0
	set t15 = null
	set COLLISION_SIZE1 = 22.0
	set t16 = null
	set TIME_FOR_MULTI = 16
	set TIME_REDUCTION = 2
	set OBELISK_ID = 1818850155
	set COLLISION_SIZE2 = 42.0
	set MAGICIAN_ID = 1835100009
	set COLLISION_SIZE3 = 32.0
	set PI = 3.14159265
	set DEGTORAD = 0.01745329
	set RADTODEG = 57.2957795
	set MAX_TILES = 6.0
	set ARENA_TILES = 9.0
	set TILE_SECTION_LENGTH = 181.019336
	set maxHeight = 10000.0
	set DUMMY_UNIT_ID = 1697656880
	set DEBUG_LEVEL = 1
	set temp_camerasetup = null
	set temp_event = null
	set temp_boolean = false
	set temp_unit = null
	set temp_group = null
	set temp_force = null
	set temp_player = null
	set temp_item = null
	set mode = 0
	set gravity = 0.0
	set gravity1 = 0.0
	set gravity2 = 0.0
	set Node_Node_firstFree = 0
	set Node_Node_maxIndex = 0
	set ArcaneMissile_ArcaneMissile_firstFree = 0
	set ArcaneMissile_ArcaneMissile_maxIndex = 0
	set wurst__tupleReturnVar_real_1 = 0.0
	set wurst__tupleReturnVar_real_2 = 0.0
	set temp_real = 0.0
	set spellId = 0
	set ArcaneMissile_ArcaneMissile_firstFree1 = 0
	set ArcaneMissile_ArcaneMissile_maxIndex1 = 0
	set spellId1 = 0
	set DmgTexttag_DmgTexttag_firstFree = 0
	set DmgTexttag_DmgTexttag_maxIndex = 0
	set currentArena = 0
	set Arena_Arena_firstFree = 0
	set Arena_Arena_maxIndex = 0
	set Obelisk_Obelisk_firstFree = 0
	set Obelisk_Obelisk_maxIndex = 0
	set Magician_Magician_firstFree = 0
	set Magician_Magician_maxIndex = 0
	set player_count = 0
	set PlayerData_PlayerData_firstFree = 0
	set PlayerData_PlayerData_maxIndex = 0
	set TerrainUtils_TerrainUtils_firstFree = 0
	set TerrainUtils_TerrainUtils_maxIndex = 0
	set worldRect = null
	set tempLoc = null
	set minWorldX = 0.0
	set minWorldY = 0.0
	set maxWorldX = 0.0
	set maxWorldY = 0.0
	set DUMMY_PLAYER = null
	set MovingEffect_MovingEffect_firstFree = 0
	set MovingEffect_MovingEffect_maxIndex = 0
	set temp108 = 0.0
	set temp109 = 0.0
	set temp110 =  - 4.0
	set gravity = temp108
	set gravity1 = temp109
	set gravity2 = temp110
	set worldRect = GetWorldBounds()
	set DUMMY_PLAYER = Player(15)
	set tempLoc = Location(0.0, 0.0)
endfunction

// function main
function main takes nothing returns nothing
	call wurst__init_globals()
	call SetCameraBounds( - 2048.0 + GetCameraMargin(CAMERA_MARGIN_LEFT),  - 2048.0 + GetCameraMargin(CAMERA_MARGIN_BOTTOM), 2048.0 - GetCameraMargin(CAMERA_MARGIN_RIGHT), 2048.0 - GetCameraMargin(CAMERA_MARGIN_TOP),  - 2048.0 + GetCameraMargin(CAMERA_MARGIN_LEFT), 2048.0 - GetCameraMargin(CAMERA_MARGIN_TOP), 2048.0 - GetCameraMargin(CAMERA_MARGIN_RIGHT),  - 2048.0 + GetCameraMargin(CAMERA_MARGIN_BOTTOM))
	call SetDayNightModels("Environment\\DNC\\DNCLordaeron\\DNCLordaeronTerrain\\DNCLordaeronTerrain.mdl", "Environment\\DNC\\DNCLordaeron\\DNCLordaeronUnit\\DNCLordaeronUnit.mdl")
	call NewSoundEnvironment("Default")
	call SetAmbientDaySound("BlackCitadelDay")
	call SetAmbientNightSound("BlackCitadelNight")
	call SetMapMusic("Music", true, 0)
	call InitBlizzard()
	call InitGlobals()
	call InitCustomTriggers()
	call TerrainUtils_init()
	call GameInit_init()
	call ArcaneMissile_init()
	call ArcaneMissile_init1()
	call KillingSystem_init()
endfunction

// function config
function config takes nothing returns nothing
	call SetMapName("TRIGSTR_001")
	call SetMapDescription("TRIGSTR_003")
	call SetPlayers(10)
	call SetTeams(10)
	call SetGamePlacement(MAP_PLACEMENT_TEAMS_TOGETHER)
	call DefineStartLocation(0, 0.0, 0.0)
	call DefineStartLocation(1, 0.0, 0.0)
	call DefineStartLocation(2, 0.0, 0.0)
	call DefineStartLocation(3, 0.0, 0.0)
	call DefineStartLocation(4, 0.0, 0.0)
	call DefineStartLocation(5, 0.0, 0.0)
	call DefineStartLocation(6, 0.0, 0.0)
	call DefineStartLocation(7, 0.0, 0.0)
	call DefineStartLocation(8, 0.0, 0.0)
	call DefineStartLocation(9, 0.0, 0.0)
	call InitCustomPlayerSlots()
	call InitCustomTeams()
	call InitAllyPriorities()
endfunction

function addVec2 takes real this, real this2, real this3, real v28, real v281 returns real
	local real temp74
	local real temp75
	local real temp76
	set temp74 = this + v28
	set temp75 = this2 + v281
	set temp76 = this3
	set temp_real = temp74
	set wurst__tupleReturnVar_real_1 = temp75
	set wurst__tupleReturnVar_real_2 = temp76
	return temp_real
endfunction

function subVec2 takes real this, real this2, real this3, real v30, real v301 returns real
	local real temp83
	local real temp84
	local real temp85
	set temp83 = this - v30
	set temp84 = this2 - v301
	set temp85 = this3
	set temp_real = temp83
	set wurst__tupleReturnVar_real_1 = temp84
	set wurst__tupleReturnVar_real_2 = temp85
	return temp_real
endfunction

function cross takes real this, real this2, real this3, real v33, real v331, real v332 returns real
	local real temp92
	local real temp93
	local real temp94
	set temp92 = this2 * v332 - this3 * v331
	set temp93 = this3 * v33 - this * v332
	set temp94 = this * v331 - this2 * v33
	set temp_real = temp92
	set wurst__tupleReturnVar_real_1 = temp93
	set wurst__tupleReturnVar_real_2 = temp94
	return temp_real
endfunction

function toVec2 takes real this, real this2, real this3 returns real
	local real temp98
	local real temp99
	set temp98 = this
	set temp99 = this2
	set temp_real = temp98
	set wurst__tupleReturnVar_real_1 = temp99
	return temp_real
endfunction

function add3 takes real this, real this2, real v35, real v351 returns real
	local real temp100
	local real temp101
	set temp100 = this + v35
	set temp101 = this2 + v351
	set temp_real = temp100
	set wurst__tupleReturnVar_real_1 = temp101
	return temp_real
endfunction

function sub1 takes real this, real this2, real v36, real v361 returns real
	local real temp102
	local real temp103
	set temp102 = this - v36
	set temp103 = this2 - v361
	set temp_real = temp102
	set wurst__tupleReturnVar_real_1 = temp103
	return temp_real
endfunction

function scale5 takes real this, real this2, real factor3 returns real
	local real temp104
	local real temp105
	set temp104 = this * factor3
	set temp105 = this2 * factor3
	set temp_real = temp104
	set wurst__tupleReturnVar_real_1 = temp105
	return temp_real
endfunction

function length2 takes real this, real this2 returns real
	return SquareRoot(this * this + this2 * this2)
endfunction

function norm1 takes real this, real this2 returns real
	local real len1
	local real x38
	local real y38
	local real temp106
	local real temp107
	set len1 = length2(this, this2)
	set x38 = 0
	set y38 = 0
	if len1 != 0.0 then
		set x38 = this / len1
		set y38 = this2 / len1
	endif
	set temp106 = x38
	set temp107 = y38
	set temp_real = temp106
	set wurst__tupleReturnVar_real_1 = temp107
	return temp_real
endfunction

function dot1 takes real this, real this2, real v37, real v371 returns real
	return this * v37 + this2 * v371
endfunction

function trim1 takes real this, real this2, real value17 returns real
	local real result4
	local real result41
	set result4 = this
	set result41 = this2
	if result4 >  - value17 and result4 < value17 then
		set result4 = 0.0
	endif
	if this2 >  - value17 and this2 < value17 then
		set result41 = 0.0
	endif
	set temp_real = result4
	set wurst__tupleReturnVar_real_1 = result41
	return temp_real
endfunction

function toString3 takes real this, real this2 returns string
	return "Vector2 [ " + toString(this) + ", " + toString(this2) + " ]"
endfunction

function getOwner6 takes unit this returns player
	return GetOwningPlayer(this)
endfunction

// function MovingEffect.MovingEffect.getOwner
function getOwner5 takes integer this returns player
	return getOwner6(dummy[this])
endfunction

function setOwner1 takes unit this, player p8, boolean changeColor4 returns unit
	call SetUnitOwner(this, p8, changeColor4)
	return this
endfunction

// function MovingEffect.MovingEffect.setOwner
function setOwner takes integer this, player p7 returns integer
	call setOwner1(dummy[this], p7, false)
	return this
endfunction

// function MovingEffect.MovingEffect.setTeamcolor
function setTeamcolor takes integer this, playercolor c returns integer
	call SetUnitColor(dummy[this], c)
	return this
endfunction

// function MovingEffect.MovingEffect.getGreen
function getGreen takes integer this returns integer
	return green24[this]
endfunction

// function MovingEffect.MovingEffect.getBlue
function getBlue takes integer this returns integer
	return blue24[this]
endfunction

// function MovingEffect.MovingEffect.getRed
function getRed takes integer this returns integer
	return red24[this]
endfunction

// function MovingEffect.MovingEffect.getAlpha
function getAlpha takes integer this returns integer
	return alpha2[this]
endfunction

function setVertexColor takes unit this, integer r36, integer g25, integer b7, integer a11 returns unit
	call SetUnitVertexColor(this, r36, g25, b7, a11)
	return this
endfunction

// function MovingEffect.MovingEffect.recolor
function recolor takes integer this, integer r35, integer g24, integer b6, integer a10 returns integer
	set red24[this] = r35
	set green24[this] = g24
	set blue24[this] = b6
	set alpha2[this] = a10
	call setVertexColor(dummy[this], r35, g24, b6, a10)
	return this
endfunction

// function MovingEffect.MovingEffect.flash
function flash takes integer this, string fx3 returns integer
	call destr(addEffect(dummy[this], fx3, "origin"))
	return this
endfunction

function getFacing takes unit this returns real
	return GetUnitFacing(this)
endfunction

// function MovingEffect.MovingEffect.getXYAngle
function getXYAngle takes integer this returns real
	return getFacing(dummy[this]) * bj_DEGTORAD
endfunction

function getFlyHeight takes unit this returns real
	return GetUnitFlyHeight(this)
endfunction

// function MovingEffect.MovingEffect.getZ
function getZ1 takes integer this returns real
	return getFlyHeight(dummy[this])
endfunction

// function MovingEffect.MovingEffect.setPos
function setPos1 takes integer this, real x43, real y43 returns integer
	call setPos(dummy[this], x43, y43)
	return this
endfunction

function hide takes unit this returns unit
	call ShowUnit(this, false)
	return this
endfunction

// function MovingEffect.MovingEffect.hiddenDestroy
function hiddenDestroy takes integer this returns nothing
	call hide(dummy[this])
	call MovingEffect_MovingEffect_destroy(this)
endfunction

function kill takes unit this returns unit
	call KillUnit(this)
	return this
endfunction

function show7 takes unit this returns unit
	call ShowUnit(this, true)
	return this
endfunction

function charAt takes string this, integer index35 returns string
	return SubString(this, index35, index35 + 1)
endfunction

function enable4 takes trigger this returns trigger
	call EnableTrigger(this)
	return this
endfunction

function disable takes trigger this returns trigger
	call DisableTrigger(this)
	return this
endfunction

function destr1 takes trigger this returns nothing
	call DestroyTrigger(this)
endfunction

function reset takes trigger this returns trigger
	call ResetTrigger(this)
	return this
endfunction

function addAction takes trigger this, code actionFunc3 returns trigger
	call TriggerAddAction(this, actionFunc3)
	return this
endfunction

function addCondition takes trigger this, boolexpr condition3 returns trigger
	call TriggerAddCondition(this, condition3)
	return this
endfunction

function evaluate takes trigger this returns trigger
	call TriggerEvaluate(this)
	return this
endfunction

function execute takes trigger this returns trigger
	call TriggerExecute(this)
	return this
endfunction

function registerAnyUnitEvent takes trigger this, playerunitevent whichEvent3 returns trigger
	call TriggerRegisterAnyUnitEventBJ(this, whichEvent3)
	return this
endfunction

function getName takes item this returns string
	return GetItemName(this)
endfunction

function getX2 takes item this returns real
	return GetItemX(this)
endfunction

function getY2 takes item this returns real
	return GetItemY(this)
endfunction

function iterator takes group this returns group
	set bj_groupAddGroupDest = CreateGroup()
	call ForGroup(this, function GroupAddGroupEnum)
	return bj_groupAddGroupDest
endfunction

function hasNext takes group this returns boolean
	return FirstOfGroup(this) != null
endfunction

function next takes group this returns unit
	return FirstOfGroup(this)
endfunction

function close takes group this returns nothing
	call DestroyGroup(this)
endfunction

// function PrintingHelper.printNote
function printNote takes string msg3 returns nothing
	local integer i14
	set i14 = 0
	loop
		exitwhen i14 > 12
		call DisplayTimedTextToPlayer(Player(i14), 0.0, 0.0, 60.0, "|cffFFFF00[NOTIFICATION] - " + msg3 + "|r")
		set i14 = i14 + 1
	endloop
endfunction

// function PrintingHelper.printWarning
function printWarning takes string msg4 returns nothing
	local integer i15
	set i15 = 0
	loop
		exitwhen i15 > 12
		call DisplayTimedTextToPlayer(Player(i15), 0.0, 0.0, 60.0, "|cffFF8000[WARNING] - " + msg4 + "|r")
		set i15 = i15 + 1
	endloop
endfunction

function toString4 takes real this returns string
	return R2S(this)
endfunction

function toString5 takes boolean this returns string
	if this then
		return "true"
	else
		return "false"
	endif
endfunction

function abs takes real this returns real
	if this >= 0 then
		return this
	else
		return  - this
	endif
endfunction

function pow takes real this returns real
	return this * this
endfunction

function sign takes real this returns real
	if this > 0.0 then
		return 1.0
	else
		if this < 0.0 then
			return  - 1.0
		else
			return 0.0
		endif
	endif
endfunction

function toInt takes real this returns integer
	return R2I(this)
endfunction

function acos takes real this returns real
	return Acos(this)
endfunction

function asin takes real this returns real
	return Asin(this)
endfunction

function atan takes real this returns real
	return Atan(this)
endfunction

function cos takes real this returns real
	return Cos(this)
endfunction

function sin takes real this returns real
	return Sin(this)
endfunction

function abs1 takes integer this returns integer
	if this >= 0 then
		return this
	else
		return  - this
	endif
endfunction

function pow1 takes integer this returns integer
	return this * this
endfunction

function sign1 takes integer this returns integer
	if this > 0 then
		return 1
	else
		if this < 0 then
			return  - 1
		else
			return 0
		endif
	endif
endfunction

function toReal takes integer this returns real
	return I2R(this)
endfunction

function toString6 takes integer this returns string
	return I2S(this)
endfunction

