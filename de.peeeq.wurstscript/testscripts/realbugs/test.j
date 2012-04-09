globals
hashtable prizes
real PI
real DEGTORAD
real RADTODEG
integer freeTimersCount
hashtable ht
timer timedLoopTimer
trigger timedLoopTrig
integer i_4
hashtable fromash
trigger gg_trg_SunRescur
trigger gg_trg_ClearUp_Buildings
real udg_x
real udg_y
player udg_p
trigger gg_trg_ReturnBuildingSpaces
trigger gg_trg_DEMONPORTAL
trigger gg_trg_Melee_Initialization
trigger gg_trg_Exploding_Buildings
trigger gg_trg_Destroy
trigger gg_trg_DEMONPORTAL_CANCEL
trigger gg_trg_Volcano_Temple
hashtable ht_2
hashtable toash
trigger gg_trg_TurnToAsh
unit array harvester
integer count_2
integer first
integer array next
integer array prev
integer last
integer array Burningtrees_nextFree
integer Burningtrees_firstFree
unit array owner_3
destructable array d
player array p_6
real array rate
integer Burningtrees_maxIndex
trigger gg_trg_ReviveAshes
integer DEBUG_LEVEL
endglobals
function newvolcano takes unit volcano, player p returns nothing
	local real x_4 = GetUnitX(volcano)
	local real y_4 = GetUnitY(volcano)
	local integer i
	local unit plot1
	call SetBlightLoc(p, Location(x_4, y_4), 750.00, true)
	set plot1 = CreateUnit(p, 1747988535, x_4 + 256., y_4, bj_UNIT_FACING)
	set plot1 = CreateUnit(p, 1747988535, x_4 - 256., y_4, bj_UNIT_FACING)
	set plot1 = CreateUnit(p, 1747988535, x_4 + 192., y_4 - 192., bj_UNIT_FACING)
	set plot1 = CreateUnit(p, 1747988535, x_4 - 192., y_4 - 192., bj_UNIT_FACING)
	set plot1 = CreateUnit(p, 1747988535, x_4, y_4 - 256., bj_UNIT_FACING)
	set plot1 = CreateUnit(p, 1747988535, x_4 - 128., y_4 + 192., bj_UNIT_FACING)
	set plot1 = CreateUnit(p, 1747988535, x_4 + 128., y_4 + 192., bj_UNIT_FACING)
	set i = 0
	loop
		exitwhen i > 15
		call SetTerrainType(GetRandomReal(x_4 - 512., x_4 + 512.), GetRandomReal(y_4 - 512., y_4 + 512.), 1147958883,  - 1, 1, 0)
		set i = i + 1
	endloop
endfunction

function newvolcanosend takes nothing returns nothing
	if GetUnitTypeId(GetConstructedStructure()) == 1747988528 then
		call newvolcano(GetConstructedStructure(), GetOwningPlayer(GetConstructedStructure()))
	endif
endfunction

function remove takes nothing returns nothing
	if GetUnitTypeId(GetEnumUnit()) != 808464432 then
		call RemoveUnit(GetEnumUnit())
	endif
endfunction

function fireSetUp takes player p_2 returns nothing
	local integer i_2
	local unit volcano_2
	if GetPlayerController(p_2) == MAP_CONTROL_USER then
		call ForGroup(GetUnitsOfPlayerAll(p_2), function remove)
		set volcano_2 = CreateUnit(p_2, 1747988528, GetStartLocationX(GetPlayerStartLocation(p_2)), GetStartLocationY(GetPlayerStartLocation(p_2)), bj_UNIT_FACING)
		call newvolcano(volcano_2, p_2)
		set i_2 = 0
		loop
			exitwhen i_2 > 3
			call CreateUnit(p_2, 1697656881, GetStartLocationX(GetPlayerStartLocation(p_2)) + 300., GetStartLocationY(GetPlayerStartLocation(p_2)), bj_UNIT_FACING)
			set i_2 = i_2 + 1
		endloop
	endif
endfunction

function initPrizes takes nothing returns nothing
	set prizes = InitHashtable()
	call SaveInteger(prizes, 1747988532, 0, 15)
	call SaveInteger(prizes, 1747988532, 1, 10)
endfunction

function init_Init takes nothing returns nothing
	local trigger t
	call initPrizes()
	call fireSetUp(Player(0))
	call fireSetUp(Player(1))
	set t = CreateTrigger()
	call TriggerRegisterAnyUnitEventBJ(t, EVENT_PLAYER_UNIT_CONSTRUCT_FINISH)
	call TriggerAddAction(t, function newvolcanosend)
endfunction

function CreateNeutralPassiveBuildings takes nothing returns nothing
	local player p_3 = Player(PLAYER_NEUTRAL_PASSIVE)
	local unit u = CreateUnit(p_3, 1852272492,  - 7168.0,  - 2240.0, 270.000)
	local real life
	local trigger t_2
	local integer unitID
	call SetResourceAmount(u, 19000)
	set u = CreateUnit(p_3, 1852272492,  - 7104.0,  - 6720.0, 270.000)
	call SetResourceAmount(u, 19000)
	set u = CreateUnit(p_3, 1852272492,  - 7296.0, 2368.0, 270.000)
	call SetResourceAmount(u, 19000)
	set u = CreateUnit(p_3, 1852272492,  - 6848.0, 7296.0, 270.000)
	call SetResourceAmount(u, 19000)
	set u = CreateUnit(p_3, 1852272492, 6208.0, 7232.0, 270.000)
	call SetResourceAmount(u, 19000)
	set u = CreateUnit(p_3, 1852272492, 7424.0, 2560.0, 270.000)
	call SetResourceAmount(u, 19000)
	set u = CreateUnit(p_3, 1852272492, 7808.0,  - 4224.0, 270.000)
	call SetResourceAmount(u, 19000)
	set u = CreateUnit(p_3, 1852272492, 6272.0,  - 7232.0, 270.000)
	call SetResourceAmount(u, 19000)
	set u = CreateUnit(p_3, 1852272492, 192.0,  - 448.0, 270.000)
	call SetResourceAmount(u, 30000)
	set u = CreateUnit(p_3, 1852272492, 2496.0, 1792.0, 270.000)
	call SetResourceAmount(u, 20000)
	set u = CreateUnit(p_3, 1852272492, 2496.0,  - 2432.0, 270.000)
	call SetResourceAmount(u, 20000)
	set u = CreateUnit(p_3, 1852272492,  - 1856.0, 1600.0, 270.000)
	call SetResourceAmount(u, 20000)
	set u = CreateUnit(p_3, 1852272492,  - 1664.0,  - 2560.0, 270.000)
	call SetResourceAmount(u, 20000)
	set u = CreateUnit(p_3, 1852272492, 448.0,  - 7808.0, 270.000)
	call SetResourceAmount(u, 20000)
	set u = CreateUnit(p_3, 1852272492, 448.0, 7808.0, 270.000)
	call SetResourceAmount(u, 20000)
	set u = CreateUnit(p_3, 1852206952,  - 1792.0,  - 512.0, 270.000)
	set u = CreateUnit(p_3, 1852206952, 1920.0,  - 512.0, 270.000)
	set u = CreateUnit(p_3, 1852666423, 512.0, 5184.0, 270.000)
	call SetUnitColor(u, ConvertPlayerColor(3))
	set u = CreateUnit(p_3, 1852666423, 320.0,  - 4928.0, 270.000)
	call SetUnitColor(u, ConvertPlayerColor(3))
endfunction

function Unit000036_DropItems takes nothing returns nothing
	local widget trigWidget = null
	local unit trigUnit = null
	local integer itemID = 0
	local boolean canDrop = true
	set trigWidget = bj_lastDyingWidget
	if trigWidget == null then
		set trigUnit = GetTriggerUnit()
	endif
	if trigUnit != null then
		set canDrop =  not IsUnitHidden(trigUnit)
		if canDrop and GetChangingUnit() != null then
			set canDrop = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(6), 100)
		set itemID = RandomDistChoose()
		if trigUnit != null then
			call UnitDropItem(trigUnit, itemID)
		else
			call WidgetDropItem(trigWidget, itemID)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000054_DropItems takes nothing returns nothing
	local widget trigWidget_2 = null
	local unit trigUnit_2 = null
	local integer itemID_2 = 0
	local boolean canDrop_2 = true
	set trigWidget_2 = bj_lastDyingWidget
	if trigWidget_2 == null then
		set trigUnit_2 = GetTriggerUnit()
	endif
	if trigUnit_2 != null then
		set canDrop_2 =  not IsUnitHidden(trigUnit_2)
		if canDrop_2 and GetChangingUnit() != null then
			set canDrop_2 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_2 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(4), 100)
		set itemID_2 = RandomDistChoose()
		if trigUnit_2 != null then
			call UnitDropItem(trigUnit_2, itemID_2)
		else
			call WidgetDropItem(trigWidget_2, itemID_2)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000082_DropItems takes nothing returns nothing
	local widget trigWidget_3 = null
	local unit trigUnit_3 = null
	local integer itemID_3 = 0
	local boolean canDrop_3 = true
	set trigWidget_3 = bj_lastDyingWidget
	if trigWidget_3 == null then
		set trigUnit_3 = GetTriggerUnit()
	endif
	if trigUnit_3 != null then
		set canDrop_3 =  not IsUnitHidden(trigUnit_3)
		if canDrop_3 and GetChangingUnit() != null then
			set canDrop_3 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_3 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(5), 100)
		set itemID_3 = RandomDistChoose()
		if trigUnit_3 != null then
			call UnitDropItem(trigUnit_3, itemID_3)
		else
			call WidgetDropItem(trigWidget_3, itemID_3)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000053_DropItems takes nothing returns nothing
	local widget trigWidget_4 = null
	local unit trigUnit_4 = null
	local integer itemID_4 = 0
	local boolean canDrop_4 = true
	set trigWidget_4 = bj_lastDyingWidget
	if trigWidget_4 == null then
		set trigUnit_4 = GetTriggerUnit()
	endif
	if trigUnit_4 != null then
		set canDrop_4 =  not IsUnitHidden(trigUnit_4)
		if canDrop_4 and GetChangingUnit() != null then
			set canDrop_4 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_4 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(3), 100)
		set itemID_4 = RandomDistChoose()
		if trigUnit_4 != null then
			call UnitDropItem(trigUnit_4, itemID_4)
		else
			call WidgetDropItem(trigWidget_4, itemID_4)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000042_DropItems takes nothing returns nothing
	local widget trigWidget_5 = null
	local unit trigUnit_5 = null
	local integer itemID_5 = 0
	local boolean canDrop_5 = true
	set trigWidget_5 = bj_lastDyingWidget
	if trigWidget_5 == null then
		set trigUnit_5 = GetTriggerUnit()
	endif
	if trigUnit_5 != null then
		set canDrop_5 =  not IsUnitHidden(trigUnit_5)
		if canDrop_5 and GetChangingUnit() != null then
			set canDrop_5 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_5 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(4), 100)
		set itemID_5 = RandomDistChoose()
		if trigUnit_5 != null then
			call UnitDropItem(trigUnit_5, itemID_5)
		else
			call WidgetDropItem(trigWidget_5, itemID_5)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000047_DropItems takes nothing returns nothing
	local widget trigWidget_6 = null
	local unit trigUnit_6 = null
	local integer itemID_6 = 0
	local boolean canDrop_6 = true
	set trigWidget_6 = bj_lastDyingWidget
	if trigWidget_6 == null then
		set trigUnit_6 = GetTriggerUnit()
	endif
	if trigUnit_6 != null then
		set canDrop_6 =  not IsUnitHidden(trigUnit_6)
		if canDrop_6 and GetChangingUnit() != null then
			set canDrop_6 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_6 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(4), 100)
		set itemID_6 = RandomDistChoose()
		if trigUnit_6 != null then
			call UnitDropItem(trigUnit_6, itemID_6)
		else
			call WidgetDropItem(trigWidget_6, itemID_6)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000048_DropItems takes nothing returns nothing
	local widget trigWidget_7 = null
	local unit trigUnit_7 = null
	local integer itemID_7 = 0
	local boolean canDrop_7 = true
	set trigWidget_7 = bj_lastDyingWidget
	if trigWidget_7 == null then
		set trigUnit_7 = GetTriggerUnit()
	endif
	if trigUnit_7 != null then
		set canDrop_7 =  not IsUnitHidden(trigUnit_7)
		if canDrop_7 and GetChangingUnit() != null then
			set canDrop_7 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_7 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(3), 100)
		set itemID_7 = RandomDistChoose()
		if trigUnit_7 != null then
			call UnitDropItem(trigUnit_7, itemID_7)
		else
			call WidgetDropItem(trigWidget_7, itemID_7)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000066_DropItems takes nothing returns nothing
	local widget trigWidget_8 = null
	local unit trigUnit_8 = null
	local integer itemID_8 = 0
	local boolean canDrop_8 = true
	set trigWidget_8 = bj_lastDyingWidget
	if trigWidget_8 == null then
		set trigUnit_8 = GetTriggerUnit()
	endif
	if trigUnit_8 != null then
		set canDrop_8 =  not IsUnitHidden(trigUnit_8)
		if canDrop_8 and GetChangingUnit() != null then
			set canDrop_8 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_8 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(3), 100)
		set itemID_8 = RandomDistChoose()
		if trigUnit_8 != null then
			call UnitDropItem(trigUnit_8, itemID_8)
		else
			call WidgetDropItem(trigWidget_8, itemID_8)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000091_DropItems takes nothing returns nothing
	local widget trigWidget_9 = null
	local unit trigUnit_9 = null
	local integer itemID_9 = 0
	local boolean canDrop_9 = true
	set trigWidget_9 = bj_lastDyingWidget
	if trigWidget_9 == null then
		set trigUnit_9 = GetTriggerUnit()
	endif
	if trigUnit_9 != null then
		set canDrop_9 =  not IsUnitHidden(trigUnit_9)
		if canDrop_9 and GetChangingUnit() != null then
			set canDrop_9 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_9 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(5), 100)
		set itemID_9 = RandomDistChoose()
		if trigUnit_9 != null then
			call UnitDropItem(trigUnit_9, itemID_9)
		else
			call WidgetDropItem(trigWidget_9, itemID_9)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000139_DropItems takes nothing returns nothing
	local widget trigWidget_10 = null
	local unit trigUnit_10 = null
	local integer itemID_10 = 0
	local boolean canDrop_10 = true
	set trigWidget_10 = bj_lastDyingWidget
	if trigWidget_10 == null then
		set trigUnit_10 = GetTriggerUnit()
	endif
	if trigUnit_10 != null then
		set canDrop_10 =  not IsUnitHidden(trigUnit_10)
		if canDrop_10 and GetChangingUnit() != null then
			set canDrop_10 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_10 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(4), 100)
		set itemID_10 = RandomDistChoose()
		if trigUnit_10 != null then
			call UnitDropItem(trigUnit_10, itemID_10)
		else
			call WidgetDropItem(trigWidget_10, itemID_10)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000105_DropItems takes nothing returns nothing
	local widget trigWidget_11 = null
	local unit trigUnit_11 = null
	local integer itemID_11 = 0
	local boolean canDrop_11 = true
	set trigWidget_11 = bj_lastDyingWidget
	if trigWidget_11 == null then
		set trigUnit_11 = GetTriggerUnit()
	endif
	if trigUnit_11 != null then
		set canDrop_11 =  not IsUnitHidden(trigUnit_11)
		if canDrop_11 and GetChangingUnit() != null then
			set canDrop_11 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_11 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(2), 100)
		set itemID_11 = RandomDistChoose()
		if trigUnit_11 != null then
			call UnitDropItem(trigUnit_11, itemID_11)
		else
			call WidgetDropItem(trigWidget_11, itemID_11)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000085_DropItems takes nothing returns nothing
	local widget trigWidget_12 = null
	local unit trigUnit_12 = null
	local integer itemID_12 = 0
	local boolean canDrop_12 = true
	set trigWidget_12 = bj_lastDyingWidget
	if trigWidget_12 == null then
		set trigUnit_12 = GetTriggerUnit()
	endif
	if trigUnit_12 != null then
		set canDrop_12 =  not IsUnitHidden(trigUnit_12)
		if canDrop_12 and GetChangingUnit() != null then
			set canDrop_12 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_12 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(5), 100)
		set itemID_12 = RandomDistChoose()
		if trigUnit_12 != null then
			call UnitDropItem(trigUnit_12, itemID_12)
		else
			call WidgetDropItem(trigWidget_12, itemID_12)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000039_DropItems takes nothing returns nothing
	local widget trigWidget_13 = null
	local unit trigUnit_13 = null
	local integer itemID_13 = 0
	local boolean canDrop_13 = true
	set trigWidget_13 = bj_lastDyingWidget
	if trigWidget_13 == null then
		set trigUnit_13 = GetTriggerUnit()
	endif
	if trigUnit_13 != null then
		set canDrop_13 =  not IsUnitHidden(trigUnit_13)
		if canDrop_13 and GetChangingUnit() != null then
			set canDrop_13 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_13 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(2), 100)
		set itemID_13 = RandomDistChoose()
		if trigUnit_13 != null then
			call UnitDropItem(trigUnit_13, itemID_13)
		else
			call WidgetDropItem(trigWidget_13, itemID_13)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000099_DropItems takes nothing returns nothing
	local widget trigWidget_14 = null
	local unit trigUnit_14 = null
	local integer itemID_14 = 0
	local boolean canDrop_14 = true
	set trigWidget_14 = bj_lastDyingWidget
	if trigWidget_14 == null then
		set trigUnit_14 = GetTriggerUnit()
	endif
	if trigUnit_14 != null then
		set canDrop_14 =  not IsUnitHidden(trigUnit_14)
		if canDrop_14 and GetChangingUnit() != null then
			set canDrop_14 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_14 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(2), 100)
		set itemID_14 = RandomDistChoose()
		if trigUnit_14 != null then
			call UnitDropItem(trigUnit_14, itemID_14)
		else
			call WidgetDropItem(trigWidget_14, itemID_14)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000088_DropItems takes nothing returns nothing
	local widget trigWidget_15 = null
	local unit trigUnit_15 = null
	local integer itemID_15 = 0
	local boolean canDrop_15 = true
	set trigWidget_15 = bj_lastDyingWidget
	if trigWidget_15 == null then
		set trigUnit_15 = GetTriggerUnit()
	endif
	if trigUnit_15 != null then
		set canDrop_15 =  not IsUnitHidden(trigUnit_15)
		if canDrop_15 and GetChangingUnit() != null then
			set canDrop_15 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_15 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(5), 100)
		set itemID_15 = RandomDistChoose()
		if trigUnit_15 != null then
			call UnitDropItem(trigUnit_15, itemID_15)
		else
			call WidgetDropItem(trigWidget_15, itemID_15)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000114_DropItems takes nothing returns nothing
	local widget trigWidget_16 = null
	local unit trigUnit_16 = null
	local integer itemID_16 = 0
	local boolean canDrop_16 = true
	set trigWidget_16 = bj_lastDyingWidget
	if trigWidget_16 == null then
		set trigUnit_16 = GetTriggerUnit()
	endif
	if trigUnit_16 != null then
		set canDrop_16 =  not IsUnitHidden(trigUnit_16)
		if canDrop_16 and GetChangingUnit() != null then
			set canDrop_16 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_16 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(2), 100)
		set itemID_16 = RandomDistChoose()
		if trigUnit_16 != null then
			call UnitDropItem(trigUnit_16, itemID_16)
		else
			call WidgetDropItem(trigWidget_16, itemID_16)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000130_DropItems takes nothing returns nothing
	local widget trigWidget_17 = null
	local unit trigUnit_17 = null
	local integer itemID_17 = 0
	local boolean canDrop_17 = true
	set trigWidget_17 = bj_lastDyingWidget
	if trigWidget_17 == null then
		set trigUnit_17 = GetTriggerUnit()
	endif
	if trigUnit_17 != null then
		set canDrop_17 =  not IsUnitHidden(trigUnit_17)
		if canDrop_17 and GetChangingUnit() != null then
			set canDrop_17 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_17 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(3), 100)
		set itemID_17 = RandomDistChoose()
		if trigUnit_17 != null then
			call UnitDropItem(trigUnit_17, itemID_17)
		else
			call WidgetDropItem(trigWidget_17, itemID_17)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000108_DropItems takes nothing returns nothing
	local widget trigWidget_18 = null
	local unit trigUnit_18 = null
	local integer itemID_18 = 0
	local boolean canDrop_18 = true
	set trigWidget_18 = bj_lastDyingWidget
	if trigWidget_18 == null then
		set trigUnit_18 = GetTriggerUnit()
	endif
	if trigUnit_18 != null then
		set canDrop_18 =  not IsUnitHidden(trigUnit_18)
		if canDrop_18 and GetChangingUnit() != null then
			set canDrop_18 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_18 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(2), 100)
		set itemID_18 = RandomDistChoose()
		if trigUnit_18 != null then
			call UnitDropItem(trigUnit_18, itemID_18)
		else
			call WidgetDropItem(trigWidget_18, itemID_18)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000057_DropItems takes nothing returns nothing
	local widget trigWidget_19 = null
	local unit trigUnit_19 = null
	local integer itemID_19 = 0
	local boolean canDrop_19 = true
	set trigWidget_19 = bj_lastDyingWidget
	if trigWidget_19 == null then
		set trigUnit_19 = GetTriggerUnit()
	endif
	if trigUnit_19 != null then
		set canDrop_19 =  not IsUnitHidden(trigUnit_19)
		if canDrop_19 and GetChangingUnit() != null then
			set canDrop_19 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_19 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(1), 100)
		set itemID_19 = RandomDistChoose()
		if trigUnit_19 != null then
			call UnitDropItem(trigUnit_19, itemID_19)
		else
			call WidgetDropItem(trigWidget_19, itemID_19)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000096_DropItems takes nothing returns nothing
	local widget trigWidget_20 = null
	local unit trigUnit_20 = null
	local integer itemID_20 = 0
	local boolean canDrop_20 = true
	set trigWidget_20 = bj_lastDyingWidget
	if trigWidget_20 == null then
		set trigUnit_20 = GetTriggerUnit()
	endif
	if trigUnit_20 != null then
		set canDrop_20 =  not IsUnitHidden(trigUnit_20)
		if canDrop_20 and GetChangingUnit() != null then
			set canDrop_20 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_20 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(2), 100)
		set itemID_20 = RandomDistChoose()
		if trigUnit_20 != null then
			call UnitDropItem(trigUnit_20, itemID_20)
		else
			call WidgetDropItem(trigWidget_20, itemID_20)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000134_DropItems takes nothing returns nothing
	local widget trigWidget_21 = null
	local unit trigUnit_21 = null
	local integer itemID_21 = 0
	local boolean canDrop_21 = true
	set trigWidget_21 = bj_lastDyingWidget
	if trigWidget_21 == null then
		set trigUnit_21 = GetTriggerUnit()
	endif
	if trigUnit_21 != null then
		set canDrop_21 =  not IsUnitHidden(trigUnit_21)
		if canDrop_21 and GetChangingUnit() != null then
			set canDrop_21 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_21 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(4), 100)
		set itemID_21 = RandomDistChoose()
		if trigUnit_21 != null then
			call UnitDropItem(trigUnit_21, itemID_21)
		else
			call WidgetDropItem(trigWidget_21, itemID_21)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000137_DropItems takes nothing returns nothing
	local widget trigWidget_22 = null
	local unit trigUnit_22 = null
	local integer itemID_22 = 0
	local boolean canDrop_22 = true
	set trigWidget_22 = bj_lastDyingWidget
	if trigWidget_22 == null then
		set trigUnit_22 = GetTriggerUnit()
	endif
	if trigUnit_22 != null then
		set canDrop_22 =  not IsUnitHidden(trigUnit_22)
		if canDrop_22 and GetChangingUnit() != null then
			set canDrop_22 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_22 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(5), 100)
		set itemID_22 = RandomDistChoose()
		if trigUnit_22 != null then
			call UnitDropItem(trigUnit_22, itemID_22)
		else
			call WidgetDropItem(trigWidget_22, itemID_22)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000117_DropItems takes nothing returns nothing
	local widget trigWidget_23 = null
	local unit trigUnit_23 = null
	local integer itemID_23 = 0
	local boolean canDrop_23 = true
	set trigWidget_23 = bj_lastDyingWidget
	if trigWidget_23 == null then
		set trigUnit_23 = GetTriggerUnit()
	endif
	if trigUnit_23 != null then
		set canDrop_23 =  not IsUnitHidden(trigUnit_23)
		if canDrop_23 and GetChangingUnit() != null then
			set canDrop_23 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_23 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(2), 100)
		set itemID_23 = RandomDistChoose()
		if trigUnit_23 != null then
			call UnitDropItem(trigUnit_23, itemID_23)
		else
			call WidgetDropItem(trigWidget_23, itemID_23)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000159_DropItems takes nothing returns nothing
	local widget trigWidget_24 = null
	local unit trigUnit_24 = null
	local integer itemID_24 = 0
	local boolean canDrop_24 = true
	set trigWidget_24 = bj_lastDyingWidget
	if trigWidget_24 == null then
		set trigUnit_24 = GetTriggerUnit()
	endif
	if trigUnit_24 != null then
		set canDrop_24 =  not IsUnitHidden(trigUnit_24)
		if canDrop_24 and GetChangingUnit() != null then
			set canDrop_24 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_24 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(5), 100)
		set itemID_24 = RandomDistChoose()
		if trigUnit_24 != null then
			call UnitDropItem(trigUnit_24, itemID_24)
		else
			call WidgetDropItem(trigWidget_24, itemID_24)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000060_DropItems takes nothing returns nothing
	local widget trigWidget_25 = null
	local unit trigUnit_25 = null
	local integer itemID_25 = 0
	local boolean canDrop_25 = true
	set trigWidget_25 = bj_lastDyingWidget
	if trigWidget_25 == null then
		set trigUnit_25 = GetTriggerUnit()
	endif
	if trigUnit_25 != null then
		set canDrop_25 =  not IsUnitHidden(trigUnit_25)
		if canDrop_25 and GetChangingUnit() != null then
			set canDrop_25 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_25 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(3), 100)
		set itemID_25 = RandomDistChoose()
		if trigUnit_25 != null then
			call UnitDropItem(trigUnit_25, itemID_25)
		else
			call WidgetDropItem(trigWidget_25, itemID_25)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000065_DropItems takes nothing returns nothing
	local widget trigWidget_26 = null
	local unit trigUnit_26 = null
	local integer itemID_26 = 0
	local boolean canDrop_26 = true
	set trigWidget_26 = bj_lastDyingWidget
	if trigWidget_26 == null then
		set trigUnit_26 = GetTriggerUnit()
	endif
	if trigUnit_26 != null then
		set canDrop_26 =  not IsUnitHidden(trigUnit_26)
		if canDrop_26 and GetChangingUnit() != null then
			set canDrop_26 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_26 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(4), 100)
		set itemID_26 = RandomDistChoose()
		if trigUnit_26 != null then
			call UnitDropItem(trigUnit_26, itemID_26)
		else
			call WidgetDropItem(trigWidget_26, itemID_26)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000079_DropItems takes nothing returns nothing
	local widget trigWidget_27 = null
	local unit trigUnit_27 = null
	local integer itemID_27 = 0
	local boolean canDrop_27 = true
	set trigWidget_27 = bj_lastDyingWidget
	if trigWidget_27 == null then
		set trigUnit_27 = GetTriggerUnit()
	endif
	if trigUnit_27 != null then
		set canDrop_27 =  not IsUnitHidden(trigUnit_27)
		if canDrop_27 and GetChangingUnit() != null then
			set canDrop_27 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_27 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(5), 100)
		set itemID_27 = RandomDistChoose()
		if trigUnit_27 != null then
			call UnitDropItem(trigUnit_27, itemID_27)
		else
			call WidgetDropItem(trigWidget_27, itemID_27)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000111_DropItems takes nothing returns nothing
	local widget trigWidget_28 = null
	local unit trigUnit_28 = null
	local integer itemID_28 = 0
	local boolean canDrop_28 = true
	set trigWidget_28 = bj_lastDyingWidget
	if trigWidget_28 == null then
		set trigUnit_28 = GetTriggerUnit()
	endif
	if trigUnit_28 != null then
		set canDrop_28 =  not IsUnitHidden(trigUnit_28)
		if canDrop_28 and GetChangingUnit() != null then
			set canDrop_28 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_28 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(2), 100)
		set itemID_28 = RandomDistChoose()
		if trigUnit_28 != null then
			call UnitDropItem(trigUnit_28, itemID_28)
		else
			call WidgetDropItem(trigWidget_28, itemID_28)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000031_DropItems takes nothing returns nothing
	local widget trigWidget_29 = null
	local unit trigUnit_29 = null
	local integer itemID_29 = 0
	local boolean canDrop_29 = true
	set trigWidget_29 = bj_lastDyingWidget
	if trigWidget_29 == null then
		set trigUnit_29 = GetTriggerUnit()
	endif
	if trigUnit_29 != null then
		set canDrop_29 =  not IsUnitHidden(trigUnit_29)
		if canDrop_29 and GetChangingUnit() != null then
			set canDrop_29 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_29 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(6), 100)
		set itemID_29 = RandomDistChoose()
		if trigUnit_29 != null then
			call UnitDropItem(trigUnit_29, itemID_29)
		else
			call WidgetDropItem(trigWidget_29, itemID_29)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000119_DropItems takes nothing returns nothing
	local widget trigWidget_30 = null
	local unit trigUnit_30 = null
	local integer itemID_30 = 0
	local boolean canDrop_30 = true
	set trigWidget_30 = bj_lastDyingWidget
	if trigWidget_30 == null then
		set trigUnit_30 = GetTriggerUnit()
	endif
	if trigUnit_30 != null then
		set canDrop_30 =  not IsUnitHidden(trigUnit_30)
		if canDrop_30 and GetChangingUnit() != null then
			set canDrop_30 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_30 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(6), 100)
		set itemID_30 = RandomDistChoose()
		if trigUnit_30 != null then
			call UnitDropItem(trigUnit_30, itemID_30)
		else
			call WidgetDropItem(trigWidget_30, itemID_30)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000125_DropItems takes nothing returns nothing
	local widget trigWidget_31 = null
	local unit trigUnit_31 = null
	local integer itemID_31 = 0
	local boolean canDrop_31 = true
	set trigWidget_31 = bj_lastDyingWidget
	if trigWidget_31 == null then
		set trigUnit_31 = GetTriggerUnit()
	endif
	if trigUnit_31 != null then
		set canDrop_31 =  not IsUnitHidden(trigUnit_31)
		if canDrop_31 and GetChangingUnit() != null then
			set canDrop_31 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_31 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(4), 100)
		set itemID_31 = RandomDistChoose()
		if trigUnit_31 != null then
			call UnitDropItem(trigUnit_31, itemID_31)
		else
			call WidgetDropItem(trigWidget_31, itemID_31)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000073_DropItems takes nothing returns nothing
	local widget trigWidget_32 = null
	local unit trigUnit_32 = null
	local integer itemID_32 = 0
	local boolean canDrop_32 = true
	set trigWidget_32 = bj_lastDyingWidget
	if trigWidget_32 == null then
		set trigUnit_32 = GetTriggerUnit()
	endif
	if trigUnit_32 != null then
		set canDrop_32 =  not IsUnitHidden(trigUnit_32)
		if canDrop_32 and GetChangingUnit() != null then
			set canDrop_32 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_32 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(5), 100)
		set itemID_32 = RandomDistChoose()
		if trigUnit_32 != null then
			call UnitDropItem(trigUnit_32, itemID_32)
		else
			call WidgetDropItem(trigWidget_32, itemID_32)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000063_DropItems takes nothing returns nothing
	local widget trigWidget_33 = null
	local unit trigUnit_33 = null
	local integer itemID_33 = 0
	local boolean canDrop_33 = true
	set trigWidget_33 = bj_lastDyingWidget
	if trigWidget_33 == null then
		set trigUnit_33 = GetTriggerUnit()
	endif
	if trigUnit_33 != null then
		set canDrop_33 =  not IsUnitHidden(trigUnit_33)
		if canDrop_33 and GetChangingUnit() != null then
			set canDrop_33 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_33 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(1), 100)
		set itemID_33 = RandomDistChoose()
		if trigUnit_33 != null then
			call UnitDropItem(trigUnit_33, itemID_33)
		else
			call WidgetDropItem(trigWidget_33, itemID_33)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000051_DropItems takes nothing returns nothing
	local widget trigWidget_34 = null
	local unit trigUnit_34 = null
	local integer itemID_34 = 0
	local boolean canDrop_34 = true
	set trigWidget_34 = bj_lastDyingWidget
	if trigWidget_34 == null then
		set trigUnit_34 = GetTriggerUnit()
	endif
	if trigUnit_34 != null then
		set canDrop_34 =  not IsUnitHidden(trigUnit_34)
		if canDrop_34 and GetChangingUnit() != null then
			set canDrop_34 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_34 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(1), 100)
		set itemID_34 = RandomDistChoose()
		if trigUnit_34 != null then
			call UnitDropItem(trigUnit_34, itemID_34)
		else
			call WidgetDropItem(trigWidget_34, itemID_34)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000153_DropItems takes nothing returns nothing
	local widget trigWidget_35 = null
	local unit trigUnit_35 = null
	local integer itemID_35 = 0
	local boolean canDrop_35 = true
	set trigWidget_35 = bj_lastDyingWidget
	if trigWidget_35 == null then
		set trigUnit_35 = GetTriggerUnit()
	endif
	if trigUnit_35 != null then
		set canDrop_35 =  not IsUnitHidden(trigUnit_35)
		if canDrop_35 and GetChangingUnit() != null then
			set canDrop_35 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_35 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(5), 100)
		set itemID_35 = RandomDistChoose()
		if trigUnit_35 != null then
			call UnitDropItem(trigUnit_35, itemID_35)
		else
			call WidgetDropItem(trigWidget_35, itemID_35)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000136_DropItems takes nothing returns nothing
	local widget trigWidget_36 = null
	local unit trigUnit_36 = null
	local integer itemID_36 = 0
	local boolean canDrop_36 = true
	set trigWidget_36 = bj_lastDyingWidget
	if trigWidget_36 == null then
		set trigUnit_36 = GetTriggerUnit()
	endif
	if trigUnit_36 != null then
		set canDrop_36 =  not IsUnitHidden(trigUnit_36)
		if canDrop_36 and GetChangingUnit() != null then
			set canDrop_36 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_36 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(5), 100)
		set itemID_36 = RandomDistChoose()
		if trigUnit_36 != null then
			call UnitDropItem(trigUnit_36, itemID_36)
		else
			call WidgetDropItem(trigWidget_36, itemID_36)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000156_DropItems takes nothing returns nothing
	local widget trigWidget_37 = null
	local unit trigUnit_37 = null
	local integer itemID_37 = 0
	local boolean canDrop_37 = true
	set trigWidget_37 = bj_lastDyingWidget
	if trigWidget_37 == null then
		set trigUnit_37 = GetTriggerUnit()
	endif
	if trigUnit_37 != null then
		set canDrop_37 =  not IsUnitHidden(trigUnit_37)
		if canDrop_37 and GetChangingUnit() != null then
			set canDrop_37 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_37 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(5), 100)
		set itemID_37 = RandomDistChoose()
		if trigUnit_37 != null then
			call UnitDropItem(trigUnit_37, itemID_37)
		else
			call WidgetDropItem(trigWidget_37, itemID_37)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000035_DropItems takes nothing returns nothing
	local widget trigWidget_38 = null
	local unit trigUnit_38 = null
	local integer itemID_38 = 0
	local boolean canDrop_38 = true
	set trigWidget_38 = bj_lastDyingWidget
	if trigWidget_38 == null then
		set trigUnit_38 = GetTriggerUnit()
	endif
	if trigUnit_38 != null then
		set canDrop_38 =  not IsUnitHidden(trigUnit_38)
		if canDrop_38 and GetChangingUnit() != null then
			set canDrop_38 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_38 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(2), 100)
		set itemID_38 = RandomDistChoose()
		if trigUnit_38 != null then
			call UnitDropItem(trigUnit_38, itemID_38)
		else
			call WidgetDropItem(trigWidget_38, itemID_38)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000094_DropItems takes nothing returns nothing
	local widget trigWidget_39 = null
	local unit trigUnit_39 = null
	local integer itemID_39 = 0
	local boolean canDrop_39 = true
	set trigWidget_39 = bj_lastDyingWidget
	if trigWidget_39 == null then
		set trigUnit_39 = GetTriggerUnit()
	endif
	if trigUnit_39 != null then
		set canDrop_39 =  not IsUnitHidden(trigUnit_39)
		if canDrop_39 and GetChangingUnit() != null then
			set canDrop_39 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_39 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(5), 100)
		set itemID_39 = RandomDistChoose()
		if trigUnit_39 != null then
			call UnitDropItem(trigUnit_39, itemID_39)
		else
			call WidgetDropItem(trigWidget_39, itemID_39)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000076_DropItems takes nothing returns nothing
	local widget trigWidget_40 = null
	local unit trigUnit_40 = null
	local integer itemID_40 = 0
	local boolean canDrop_40 = true
	set trigWidget_40 = bj_lastDyingWidget
	if trigWidget_40 == null then
		set trigUnit_40 = GetTriggerUnit()
	endif
	if trigUnit_40 != null then
		set canDrop_40 =  not IsUnitHidden(trigUnit_40)
		if canDrop_40 and GetChangingUnit() != null then
			set canDrop_40 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_40 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(5), 100)
		set itemID_40 = RandomDistChoose()
		if trigUnit_40 != null then
			call UnitDropItem(trigUnit_40, itemID_40)
		else
			call WidgetDropItem(trigWidget_40, itemID_40)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000059_DropItems takes nothing returns nothing
	local widget trigWidget_41 = null
	local unit trigUnit_41 = null
	local integer itemID_41 = 0
	local boolean canDrop_41 = true
	set trigWidget_41 = bj_lastDyingWidget
	if trigWidget_41 == null then
		set trigUnit_41 = GetTriggerUnit()
	endif
	if trigUnit_41 != null then
		set canDrop_41 =  not IsUnitHidden(trigUnit_41)
		if canDrop_41 and GetChangingUnit() != null then
			set canDrop_41 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_41 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(5), 100)
		set itemID_41 = RandomDistChoose()
		if trigUnit_41 != null then
			call UnitDropItem(trigUnit_41, itemID_41)
		else
			call WidgetDropItem(trigWidget_41, itemID_41)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000147_DropItems takes nothing returns nothing
	local widget trigWidget_42 = null
	local unit trigUnit_42 = null
	local integer itemID_42 = 0
	local boolean canDrop_42 = true
	set trigWidget_42 = bj_lastDyingWidget
	if trigWidget_42 == null then
		set trigUnit_42 = GetTriggerUnit()
	endif
	if trigUnit_42 != null then
		set canDrop_42 =  not IsUnitHidden(trigUnit_42)
		if canDrop_42 and GetChangingUnit() != null then
			set canDrop_42 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_42 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(5), 100)
		set itemID_42 = RandomDistChoose()
		if trigUnit_42 != null then
			call UnitDropItem(trigUnit_42, itemID_42)
		else
			call WidgetDropItem(trigWidget_42, itemID_42)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000069_DropItems takes nothing returns nothing
	local widget trigWidget_43 = null
	local unit trigUnit_43 = null
	local integer itemID_43 = 0
	local boolean canDrop_43 = true
	set trigWidget_43 = bj_lastDyingWidget
	if trigWidget_43 == null then
		set trigUnit_43 = GetTriggerUnit()
	endif
	if trigUnit_43 != null then
		set canDrop_43 =  not IsUnitHidden(trigUnit_43)
		if canDrop_43 and GetChangingUnit() != null then
			set canDrop_43 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_43 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(1), 100)
		set itemID_43 = RandomDistChoose()
		if trigUnit_43 != null then
			call UnitDropItem(trigUnit_43, itemID_43)
		else
			call WidgetDropItem(trigWidget_43, itemID_43)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000150_DropItems takes nothing returns nothing
	local widget trigWidget_44 = null
	local unit trigUnit_44 = null
	local integer itemID_44 = 0
	local boolean canDrop_44 = true
	set trigWidget_44 = bj_lastDyingWidget
	if trigWidget_44 == null then
		set trigUnit_44 = GetTriggerUnit()
	endif
	if trigUnit_44 != null then
		set canDrop_44 =  not IsUnitHidden(trigUnit_44)
		if canDrop_44 and GetChangingUnit() != null then
			set canDrop_44 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_44 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(5), 100)
		set itemID_44 = RandomDistChoose()
		if trigUnit_44 != null then
			call UnitDropItem(trigUnit_44, itemID_44)
		else
			call WidgetDropItem(trigWidget_44, itemID_44)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000102_DropItems takes nothing returns nothing
	local widget trigWidget_45 = null
	local unit trigUnit_45 = null
	local integer itemID_45 = 0
	local boolean canDrop_45 = true
	set trigWidget_45 = bj_lastDyingWidget
	if trigWidget_45 == null then
		set trigUnit_45 = GetTriggerUnit()
	endif
	if trigUnit_45 != null then
		set canDrop_45 =  not IsUnitHidden(trigUnit_45)
		if canDrop_45 and GetChangingUnit() != null then
			set canDrop_45 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_45 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(2), 100)
		set itemID_45 = RandomDistChoose()
		if trigUnit_45 != null then
			call UnitDropItem(trigUnit_45, itemID_45)
		else
			call WidgetDropItem(trigWidget_45, itemID_45)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000037_DropItems takes nothing returns nothing
	local widget trigWidget_46 = null
	local unit trigUnit_46 = null
	local integer itemID_46 = 0
	local boolean canDrop_46 = true
	set trigWidget_46 = bj_lastDyingWidget
	if trigWidget_46 == null then
		set trigUnit_46 = GetTriggerUnit()
	endif
	if trigUnit_46 != null then
		set canDrop_46 =  not IsUnitHidden(trigUnit_46)
		if canDrop_46 and GetChangingUnit() != null then
			set canDrop_46 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_46 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(4), 100)
		set itemID_46 = RandomDistChoose()
		if trigUnit_46 != null then
			call UnitDropItem(trigUnit_46, itemID_46)
		else
			call WidgetDropItem(trigWidget_46, itemID_46)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000129_DropItems takes nothing returns nothing
	local widget trigWidget_47 = null
	local unit trigUnit_47 = null
	local integer itemID_47 = 0
	local boolean canDrop_47 = true
	set trigWidget_47 = bj_lastDyingWidget
	if trigWidget_47 == null then
		set trigUnit_47 = GetTriggerUnit()
	endif
	if trigUnit_47 != null then
		set canDrop_47 =  not IsUnitHidden(trigUnit_47)
		if canDrop_47 and GetChangingUnit() != null then
			set canDrop_47 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_47 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(3), 100)
		set itemID_47 = RandomDistChoose()
		if trigUnit_47 != null then
			call UnitDropItem(trigUnit_47, itemID_47)
		else
			call WidgetDropItem(trigWidget_47, itemID_47)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000120_DropItems takes nothing returns nothing
	local widget trigWidget_48 = null
	local unit trigUnit_48 = null
	local integer itemID_48 = 0
	local boolean canDrop_48 = true
	set trigWidget_48 = bj_lastDyingWidget
	if trigWidget_48 == null then
		set trigUnit_48 = GetTriggerUnit()
	endif
	if trigUnit_48 != null then
		set canDrop_48 =  not IsUnitHidden(trigUnit_48)
		if canDrop_48 and GetChangingUnit() != null then
			set canDrop_48 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_48 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(3), 100)
		set itemID_48 = RandomDistChoose()
		if trigUnit_48 != null then
			call UnitDropItem(trigUnit_48, itemID_48)
		else
			call WidgetDropItem(trigWidget_48, itemID_48)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000044_DropItems takes nothing returns nothing
	local widget trigWidget_49 = null
	local unit trigUnit_49 = null
	local integer itemID_49 = 0
	local boolean canDrop_49 = true
	set trigWidget_49 = bj_lastDyingWidget
	if trigWidget_49 == null then
		set trigUnit_49 = GetTriggerUnit()
	endif
	if trigUnit_49 != null then
		set canDrop_49 =  not IsUnitHidden(trigUnit_49)
		if canDrop_49 and GetChangingUnit() != null then
			set canDrop_49 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_49 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(2), 100)
		set itemID_49 = RandomDistChoose()
		if trigUnit_49 != null then
			call UnitDropItem(trigUnit_49, itemID_49)
		else
			call WidgetDropItem(trigWidget_49, itemID_49)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function Unit000030_DropItems takes nothing returns nothing
	local widget trigWidget_50 = null
	local unit trigUnit_50 = null
	local integer itemID_50 = 0
	local boolean canDrop_50 = true
	set trigWidget_50 = bj_lastDyingWidget
	if trigWidget_50 == null then
		set trigUnit_50 = GetTriggerUnit()
	endif
	if trigUnit_50 != null then
		set canDrop_50 =  not IsUnitHidden(trigUnit_50)
		if canDrop_50 and GetChangingUnit() != null then
			set canDrop_50 = GetChangingUnitPrevOwner() == Player(PLAYER_NEUTRAL_AGGRESSIVE)
		endif
	endif
	if canDrop_50 then
		call RandomDistReset()
		call RandomDistAddItem(ChooseRandomItem(2), 100)
		set itemID_50 = RandomDistChoose()
		if trigUnit_50 != null then
			call UnitDropItem(trigUnit_50, itemID_50)
		else
			call WidgetDropItem(trigWidget_50, itemID_50)
		endif
	endif
	set bj_lastDyingWidget = null
	call DestroyTrigger(GetTriggeringTrigger())
endfunction

function CreateNeutralHostile takes nothing returns nothing
	local player p_4 = Player(PLAYER_NEUTRAL_AGGRESSIVE)
	local unit u_2 = CreateUnit(p_4, 1853320819, 335.9,  - 4691.3, 89.060)
	local real life_2
	local integer unitID_2
	local trigger t_3
	call SetUnitState(u_2, UNIT_STATE_MANA, 0.)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000031_DropItems)
	set u_2 = CreateUnit(p_4, 1852207728, 131.3,  - 4743.7, 87.791)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1852207728, 569.1,  - 4711.8, 90.513)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1852207728, 213.1,  - 4535.3, 88.259)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000030_DropItems)
	set u_2 = CreateUnit(p_4, 1852207728, 503.9,  - 4543.3, 90.109)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1852207728, 705.3, 4897.8, 252.195)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1852207728, 350.1, 4685.1, 321.375)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1852207728, 627.4, 4652.3, 229.815)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1852207728, 249.9, 4897.0, 292.018)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000035_DropItems)
	set u_2 = CreateUnit(p_4, 1853320819, 490.1, 4834.9, 279.310)
	call SetUnitState(u_2, UNIT_STATE_MANA, 0.)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000036_DropItems)
	set u_2 = CreateUnit(p_4, 1852404855,  - 2121.3,  - 535.6, 227.220)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000037_DropItems)
	set u_2 = CreateUnit(p_4, 1852404840,  - 2054.5,  - 381.1, 351.057)
	call SetUnitState(u_2, UNIT_STATE_MANA, 0.)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1852404840,  - 2057.0,  - 689.5, 148.250)
	call SetUnitState(u_2, UNIT_STATE_MANA, 0.)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000039_DropItems)
	set u_2 = CreateUnit(p_4, 1852404850,  - 2227.7,  - 431.6, 215.437)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1852404850,  - 2206.3,  - 643.2, 102.582)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1852404855, 2276.2,  - 478.6, 227.220)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000042_DropItems)
	set u_2 = CreateUnit(p_4, 1852404840, 2210.0,  - 259.2, 351.057)
	call SetUnitState(u_2, UNIT_STATE_MANA, 0.)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1852404840, 2216.2,  - 682.4, 148.250)
	call SetUnitState(u_2, UNIT_STATE_MANA, 0.)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000044_DropItems)
	set u_2 = CreateUnit(p_4, 1852404850, 2364.1,  - 297.5, 215.437)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1852404850, 2381.1,  - 613.9, 102.582)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1852404840,  - 1614.8,  - 2145.7, 26.265)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000047_DropItems)
	set u_2 = CreateUnit(p_4, 1852404840,  - 1308.9,  - 2399.7, 67.228)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000048_DropItems)
	set u_2 = CreateUnit(p_4, 1852404851,  - 1454.4,  - 2021.7, 20.239)
	set u_2 = CreateUnit(p_4, 1852404851,  - 1240.9,  - 2177.1, 63.065)
	set u_2 = CreateUnit(p_4, 1852404852,  - 1405.2,  - 2208.3, 45.786)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000051_DropItems)
	set u_2 = CreateUnit(p_4, 1852404852,  - 1293.6,  - 2007.3, 30.718)
	set u_2 = CreateUnit(p_4, 1852404840, 2121.1,  - 2345.9, 121.455)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000053_DropItems)
	set u_2 = CreateUnit(p_4, 1852404840, 2339.2,  - 2069.4, 157.645)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000139_DropItems)
	set u_2 = CreateUnit(p_4, 1852404851, 1985.4,  - 2195.3, 115.790)
	set u_2 = CreateUnit(p_4, 1852404851, 2124.4,  - 1970.8, 159.325)
	set u_2 = CreateUnit(p_4, 1852404852, 2167.8,  - 2132.3, 141.457)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000057_DropItems)
	set u_2 = CreateUnit(p_4, 1852404852, 1959.0,  - 2036.0, 127.416)
	set u_2 = CreateUnit(p_4, 1852404840, 2427.7, 1358.0, 220.358)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000054_DropItems)
	set u_2 = CreateUnit(p_4, 1852404840, 2129.2, 1623.6, 255.008)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000060_DropItems)
	set u_2 = CreateUnit(p_4, 1852404851, 2274.3, 1245.4, 222.130)
	set u_2 = CreateUnit(p_4, 1852404851, 2061.0, 1401.0, 256.043)
	set u_2 = CreateUnit(p_4, 1852404852, 2225.3, 1432.1, 240.130)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000063_DropItems)
	set u_2 = CreateUnit(p_4, 1852404852, 2113.4, 1231.2, 239.593)
	set u_2 = CreateUnit(p_4, 1852404840,  - 1425.3, 1397.6, 285.913)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000065_DropItems)
	set u_2 = CreateUnit(p_4, 1852404840,  - 1788.3, 1235.4, 318.504)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000066_DropItems)
	set u_2 = CreateUnit(p_4, 1852404851,  - 1384.9, 1199.0, 287.811)
	set u_2 = CreateUnit(p_4, 1852404851,  - 1618.6, 1076.1, 319.302)
	set u_2 = CreateUnit(p_4, 1852404852,  - 1574.0, 1237.3, 304.421)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000069_DropItems)
	set u_2 = CreateUnit(p_4, 1852404852,  - 1443.0, 1048.3, 304.218)
	set u_2 = CreateUnit(p_4, 1853318510,  - 6747.8,  - 2360.7, 78.752)
	set u_2 = CreateUnit(p_4, 1853318510,  - 6768.9,  - 2045.1, 56.351)
	set u_2 = CreateUnit(p_4, 1853320818,  - 6667.9,  - 2187.4, 40.980)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000073_DropItems)
	set u_2 = CreateUnit(p_4, 1853318510,  - 6666.0,  - 6831.0, 78.752)
	set u_2 = CreateUnit(p_4, 1853318510,  - 6687.1,  - 6515.4, 56.351)
	set u_2 = CreateUnit(p_4, 1853320818,  - 6586.1,  - 6657.7, 40.980)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000076_DropItems)
	set u_2 = CreateUnit(p_4, 1853318510,  - 6842.7, 1979.9, 304.582)
	set u_2 = CreateUnit(p_4, 1853318510,  - 6863.8, 2295.5, 295.554)
	set u_2 = CreateUnit(p_4, 1853320818,  - 6762.8, 2153.2, 0.000)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000079_DropItems)
	set u_2 = CreateUnit(p_4, 1853318510,  - 6412.1, 7096.7, 320.919)
	set u_2 = CreateUnit(p_4, 1853318510,  - 6433.3, 7412.3, 308.702)
	set u_2 = CreateUnit(p_4, 1853320818,  - 6332.2, 7270.0, 309.334)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000082_DropItems)
	set u_2 = CreateUnit(p_4, 1853318510, 5781.6, 7112.4, 179.503)
	set u_2 = CreateUnit(p_4, 1853318510, 5760.5, 7428.0, 228.200)
	set u_2 = CreateUnit(p_4, 1853320818, 5684.5, 7298.7, 222.027)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000085_DropItems)
	set u_2 = CreateUnit(p_4, 1853318510, 6944.3, 2290.8, 179.503)
	set u_2 = CreateUnit(p_4, 1853318510, 6923.2, 2606.4, 228.200)
	set u_2 = CreateUnit(p_4, 1853320818, 6847.2, 2477.1, 222.027)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000088_DropItems)
	set u_2 = CreateUnit(p_4, 1853318510, 7333.8,  - 4251.1, 179.503)
	set u_2 = CreateUnit(p_4, 1853318510, 7312.7,  - 3935.5, 228.200)
	set u_2 = CreateUnit(p_4, 1853320818, 7236.7,  - 4064.8, 222.027)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000091_DropItems)
	set u_2 = CreateUnit(p_4, 1853318510, 5724.5,  - 7482.6, 179.503)
	set u_2 = CreateUnit(p_4, 1853318510, 5703.4,  - 7167.0, 228.200)
	set u_2 = CreateUnit(p_4, 1853320818, 5627.4,  - 7296.3, 222.027)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000094_DropItems)
	set u_2 = CreateUnit(p_4, 1853323110, 3880.5,  - 5593.6, 305.704)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853323110, 3974.9,  - 5381.7, 282.910)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000096_DropItems)
	set u_2 = CreateUnit(p_4, 1853323110, 4145.5,  - 5554.8, 79.203)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853323110, 4434.5,  - 4416.3, 305.704)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853323110, 4563.3,  - 4351.9, 282.910)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000099_DropItems)
	set u_2 = CreateUnit(p_4, 1853323110, 4636.2,  - 4546.3, 79.203)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853323110, 6702.5, 570.9, 305.704)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853323110, 6556.9, 779.8, 282.910)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000102_DropItems)
	set u_2 = CreateUnit(p_4, 1853323110, 6859.7, 868.2, 79.203)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853323110, 2628.5, 7067.1, 305.704)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853323110, 2722.9, 7279.0, 282.910)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000105_DropItems)
	set u_2 = CreateUnit(p_4, 1853323110, 2893.5, 7105.9, 79.203)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853323110,  - 3148.4, 6990.8, 289.379)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853323110,  - 3049.0, 7210.1, 277.040)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000108_DropItems)
	set u_2 = CreateUnit(p_4, 1853323110,  - 2911.9, 6972.8, 265.574)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853323110,  - 5873.4, 96.4, 59.107)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853323110,  - 5713.2, 199.4, 71.945)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000111_DropItems)
	set u_2 = CreateUnit(p_4, 1853323110,  - 5638.2, 5.8, 85.570)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853323110,  - 5032.4,  - 4257.9, 305.704)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853323110,  - 4938.0,  - 4046.0, 282.910)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000114_DropItems)
	set u_2 = CreateUnit(p_4, 1853323110,  - 4767.4,  - 4219.1, 79.203)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853323110,  - 3939.5,  - 7829.5, 71.752)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853323110,  - 3845.1,  - 7617.6, 74.343)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000117_DropItems)
	set u_2 = CreateUnit(p_4, 1853323110,  - 3674.5,  - 7790.8, 92.695)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1851876459, 28.1,  - 270.7, 97.570)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000119_DropItems)
	set u_2 = CreateUnit(p_4, 1851876459, 356.8,  - 608.8, 139.150)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000120_DropItems)
	set u_2 = CreateUnit(p_4, 1851876471,  - 73.5,  - 467.6, 41.563)
	set u_2 = CreateUnit(p_4, 1851876471, 208.3,  - 182.3, 12.624)
	set u_2 = CreateUnit(p_4, 1851876471, 474.0,  - 381.2, 344.443)
	set u_2 = CreateUnit(p_4, 1851876471, 123.6,  - 743.1, 148.518)
	set u_2 = CreateUnit(p_4, 1851876459, 310.8, 7686.9, 200.660)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000125_DropItems)
	set u_2 = CreateUnit(p_4, 1851876471, 188.6, 7796.6, 103.077)
	set u_2 = CreateUnit(p_4, 1851876471, 446.2, 7533.0, 92.870)
	set u_2 = CreateUnit(p_4, 1851876459, 643.1, 7721.9, 0.000)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000129_DropItems)
	set u_2 = CreateUnit(p_4, 1851876459, 314.4,  - 7781.6, 200.660)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000130_DropItems)
	set u_2 = CreateUnit(p_4, 1851876471, 752.7, 7791.7, 62.756)
	set u_2 = CreateUnit(p_4, 1851876471, 192.3,  - 7671.9, 103.077)
	set u_2 = CreateUnit(p_4, 1851876471, 423.1,  - 7618.9, 92.870)
	set u_2 = CreateUnit(p_4, 1851876471, 761.1,  - 7709.4, 62.756)
	set u_2 = CreateUnit(p_4, 1851876459, 646.7,  - 7746.6, 0.000)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000134_DropItems)
	set u_2 = CreateUnit(p_4, 1851876471, 201.9,  - 455.0, 123.380)
	set u_2 = CreateUnit(p_4, 1853320818,  - 697.0,  - 6458.9, 154.583)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853318510,  - 980.2,  - 6486.0, 119.452)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853320818,  - 935.4,  - 6786.0, 139.302)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000156_DropItems)
	set u_2 = CreateUnit(p_4, 1853320818,  - 384.3, 6310.9, 181.712)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853320818, 1677.2,  - 6234.8, 38.736)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000159_DropItems)
	set u_2 = CreateUnit(p_4, 1853318510,  - 616.4, 6342.3, 232.760)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853320818, 1956.0,  - 6520.5, 61.466)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853318510, 1944.4,  - 6236.3, 26.335)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853320818,  - 685.5, 6556.4, 231.304)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000147_DropItems)
	set u_2 = CreateUnit(p_4, 1853320818, 1312.1, 6401.2, 331.635)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853318510, 1596.3, 6413.7, 296.503)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853320818, 1537.6, 6655.2, 299.357)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000150_DropItems)
	set u_2 = CreateUnit(p_4, 1853320818, 6918.5, 4898.4, 212.041)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853318510, 6624.4, 4835.9, 242.338)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853320818, 6661.3, 5127.5, 226.927)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000059_DropItems)
	set u_2 = CreateUnit(p_4, 1853320818,  - 6716.9, 4119.3, 342.211)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853318510,  - 6439.8, 4183.7, 307.080)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853320818,  - 6538.7, 4384.5, 312.380)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000137_DropItems)
	set u_2 = CreateUnit(p_4, 1853320818,  - 7291.8,  - 4293.2, 61.466)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853318510,  - 7112.5,  - 4127.2, 26.335)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853320818,  - 7334.0,  - 4033.3, 354.053)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000153_DropItems)
	set u_2 = CreateUnit(p_4, 1853320818, 6867.4,  - 1490.4, 190.564)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853318510, 6634.2,  - 1549.8, 186.667)
	call SetUnitAcquireRange(u_2, 200.0)
	set u_2 = CreateUnit(p_4, 1853320818, 6634.5,  - 1287.2, 228.374)
	call SetUnitAcquireRange(u_2, 200.0)
	set t_3 = CreateTrigger()
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_DEATH)
	call TriggerRegisterUnitEvent(t_3, u_2, EVENT_UNIT_CHANGE_OWNER)
	call TriggerAddAction(t_3, function Unit000136_DropItems)
endfunction

function CreatePlayerBuildings takes nothing returns nothing
endfunction

function CreatePlayerUnits takes nothing returns nothing
endfunction

function CreateAllUnits takes nothing returns nothing
	call CreateNeutralPassiveBuildings()
	call CreatePlayerBuildings()
	call CreateNeutralHostile()
	call CreatePlayerUnits()
endfunction

function init_Maths takes nothing returns nothing
	set PI = 3.14159265
	set DEGTORAD = 0.01745329
	set RADTODEG = 57.2957795
endfunction

function init_TimerUtils takes nothing returns nothing
	set freeTimersCount = 0
	set ht = InitHashtable()
	set timedLoopTimer = CreateTimer()
	set timedLoopTrig = CreateTrigger()
endfunction

function heal takes nothing returns nothing
	if IsUnitType(GetTriggerUnit(), UNIT_TYPE_UNDEAD) == false and IsPlayerAlly(GetOwningPlayer(GetSpellAbilityUnit()), GetOwningPlayer(GetEnumUnit())) == true and IsUnitType(GetTriggerUnit(), UNIT_TYPE_STRUCTURE) == false then
		call SetUnitLifeBJ(GetEnumUnit(), GetUnitStateSwap(UNIT_STATE_LIFE, GetEnumUnit()) + 50.00)
	else
		if IsUnitType(GetTriggerUnit(), UNIT_TYPE_UNDEAD) == true and IsPlayerAlly(GetOwningPlayer(GetSpellAbilityUnit()), GetOwningPlayer(GetEnumUnit())) == false then
			call SetUnitLifeBJ(GetEnumUnit(), GetUnitStateSwap(UNIT_STATE_LIFE, GetEnumUnit()) - 30.00)
		endif
	endif
	if i_4 > 0 then
		if LoadInteger(fromash, GetUnitTypeId(GetEnumUnit()), 0) != 0 then
			if GetPlayerState(GetOwningPlayer(GetSpellAbilityUnit()), PLAYER_STATE_RESOURCE_FOOD_CAP) > GetPlayerState(GetOwningPlayer(GetSpellAbilityUnit()), PLAYER_STATE_RESOURCE_FOOD_USED) + GetFoodUsed(LoadInteger(fromash, GetUnitTypeId(GetEnumUnit()), 0) - 1) then
				call SetUnitOwner(GetEnumUnit(), GetOwningPlayer(GetSpellAbilityUnit()), true)
				call ReplaceUnitBJ(GetEnumUnit(), LoadInteger(fromash, GetUnitTypeId(GetEnumUnit()), 0), bj_UNIT_STATE_METHOD_DEFAULTS)
				call AddSpecialEffectLocBJ(GetRectCenter(GetPlayableMapRect()), "Abilities\\Spells\\Human\\Resurrect\\ResurrectTarget.mdl")
				call DestroyEffectBJ(GetLastCreatedEffectBJ())
				set i_4 = i_4 - 1
			endif
		endif
	endif
endfunction

function pickgroup takes nothing returns nothing
	local integer ii
	if GetSpellAbilityId() == 1093677137 then
		set ii = 0
		loop
			exitwhen ii > 3
			call ForGroupBJ(GetUnitsInRangeOfLocAll(900.00, GetUnitLoc(GetSpellAbilityUnit())), function heal)
			set i_4 = 2
			call TriggerSleepAction(1.27)
			set ii = ii + 1
		endloop
	endif
endfunction

function init_SunRescur takes nothing returns nothing
	set gg_trg_SunRescur = CreateTrigger()
	call TriggerRegisterAnyUnitEventBJ(gg_trg_SunRescur, EVENT_PLAYER_UNIT_SPELL_EFFECT)
	call TriggerAddAction(gg_trg_SunRescur, function pickgroup)
endfunction

function Trig_ClearUp_Buildings_Func003Func001C takes nothing returns boolean
	if  not (GetOwningPlayer(GetEnumUnit()) == GetOwningPlayer(GetDyingUnit())) then
		return false
	endif
	if  not (GetUnitTypeId(GetEnumUnit()) == 1747988535) then
		return false
	endif
	return true
endfunction

function Trig_ClearUp_Buildings_Func003A takes nothing returns nothing
	if Trig_ClearUp_Buildings_Func003Func001C() then
		call RemoveUnit(GetEnumUnit())
	endif
endfunction

function Trig_ClearUp_Buildings_Func001Func001C takes nothing returns boolean
	if  not (GetOwningPlayer(GetEnumUnit()) == GetOwningPlayer(GetDyingUnit())) then
		return false
	endif
	return true
endfunction

function Trig_ClearUp_Buildings_Func001A takes nothing returns nothing
	if Trig_ClearUp_Buildings_Func001Func001C() then
		call KillUnit(GetEnumUnit())
	endif
endfunction

function Trig_ClearUp_Buildings_Actions takes nothing returns nothing
	call ForGroupBJ(GetUnitsInRangeOfLocAll(512., GetUnitLoc(GetDyingUnit())), function Trig_ClearUp_Buildings_Func001A)
	call TriggerSleepAction(3.10)
	call ForGroupBJ(GetUnitsInRangeOfLocAll(512., GetUnitLoc(GetDyingUnit())), function Trig_ClearUp_Buildings_Func003A)
endfunction

function Trig_ClearUp_Buildings_Func004C takes nothing returns boolean
	if GetUnitTypeId(GetDyingUnit()) == 1747988528 then
		return true
	endif
	if GetUnitTypeId(GetDyingUnit()) == 1747988546 then
		return true
	endif
	if GetUnitTypeId(GetDyingUnit()) == 1747988547 then
		return true
	endif
	return false
endfunction

function Trig_ClearUp_Buildings_Conditions takes nothing returns boolean
	if  not Trig_ClearUp_Buildings_Func004C() then
		return false
	endif
	return true
endfunction

function InitTrig_ClearUp_Buildings takes nothing returns nothing
	set gg_trg_ClearUp_Buildings = CreateTrigger()
	call TriggerRegisterAnyUnitEventBJ(gg_trg_ClearUp_Buildings, EVENT_PLAYER_UNIT_DEATH)
	call TriggerAddCondition(gg_trg_ClearUp_Buildings, Condition(function Trig_ClearUp_Buildings_Conditions))
	call TriggerAddAction(gg_trg_ClearUp_Buildings, function Trig_ClearUp_Buildings_Actions)
endfunction

function Trig_ReturnBuildingSpaces_Func002C takes nothing returns boolean
	if GetUnitTypeId(GetDyingUnit()) == 1747988536 then
		return true
	endif
	if GetUnitTypeId(GetDyingUnit()) == 1747988533 then
		return true
	endif
	if GetUnitTypeId(GetDyingUnit()) == 1747988545 then
		return true
	endif
	if GetUnitTypeId(GetDyingUnit()) == 1747988534 then
		return true
	endif
	if GetUnitTypeId(GetDyingUnit()) == 1747988532 then
		return true
	endif
	if GetUnitTypeId(GetDyingUnit()) == 1966092338 then
		return true
	endif
	if GetUnitTypeId(GetDyingUnit()) == 1966092339 then
		return true
	endif
	return false
endfunction

function Trig_ReturnBuildingSpaces_Conditions takes nothing returns boolean
	if  not Trig_ReturnBuildingSpaces_Func002C() then
		return false
	endif
	return true
endfunction

function Trig_ReturnBuildingSpaces_Func001Func003Func003C takes nothing returns boolean
	if GetUnitTypeId(GetDyingUnit()) == 1747988545 then
		return true
	endif
	if GetUnitTypeId(GetDyingUnit()) == 1747988533 then
		return true
	endif
	return false
endfunction

function Trig_ReturnBuildingSpaces_Func001Func003C takes nothing returns boolean
	if  not Trig_ReturnBuildingSpaces_Func001Func003Func003C() then
		return false
	endif
	return true
endfunction

function Trig_ReturnBuildingSpaces_Func001Func004C takes nothing returns boolean
	if GetUnitTypeId(GetDyingUnit()) == 1747988537 then
		return true
	endif
	if GetUnitTypeId(GetDyingUnit()) == 1865429045 then
		return true
	endif
	return false
endfunction

function Trig_ReturnBuildingSpaces_Func001C takes nothing returns boolean
	if  not Trig_ReturnBuildingSpaces_Func001Func004C() then
		return false
	endif
	return true
endfunction

function Trig_ReturnBuildingSpaces_Actions takes nothing returns nothing
	if Trig_ReturnBuildingSpaces_Func001C() then
		call AddSpecialEffectLocBJ(GetUnitLoc(GetDyingUnit()), "Objects\\Spawnmodels\\Human\\HCancelDeath\\HCancelDeath.mdl")
		call RemoveUnit(GetDyingUnit())
	else
		if Trig_ReturnBuildingSpaces_Func001Func003C() then
			call AddSpecialEffectLocBJ(GetUnitLoc(GetDyingUnit()), "Objects\\Spawnmodels\\Human\\HCancelDeath\\HCancelDeath.mdl")
			set udg_x = GetLocationX(GetUnitLoc(GetDyingUnit()))
			set udg_y = GetLocationY(GetUnitLoc(GetDyingUnit()))
			set udg_p = GetOwningPlayer(GetDyingUnit())
			call RemoveUnit(GetDyingUnit())
			call TriggerSleepAction(3.00)
			call CreateNUnitsAtLoc(1, 1747988535, udg_p, Location(udg_x, udg_y), bj_UNIT_FACING)
		else
			call TriggerSleepAction(3.00)
			call CreateNUnitsAtLoc(1, 1747988535, GetOwningPlayer(GetDyingUnit()), GetUnitLoc(GetDyingUnit()), bj_UNIT_FACING)
		endif
	endif
endfunction

function InitTrig_ReturnBuildingSpaces takes nothing returns nothing
	set gg_trg_ReturnBuildingSpaces = CreateTrigger()
	call TriggerRegisterAnyUnitEventBJ(gg_trg_ReturnBuildingSpaces, EVENT_PLAYER_UNIT_DEATH)
	call TriggerAddCondition(gg_trg_ReturnBuildingSpaces, Condition(function Trig_ReturnBuildingSpaces_Conditions))
	call TriggerAddAction(gg_trg_ReturnBuildingSpaces, function Trig_ReturnBuildingSpaces_Actions)
endfunction

function Trig_DEMONPORTAL_Func001Func001Func001Func001Func001C takes nothing returns boolean
	if  not (GetUnitTypeId(GetTriggerUnit()) == 1747988532) then
		return false
	endif
	return true
endfunction

function Trig_DEMONPORTAL_Func001C takes nothing returns boolean
	if  not (GetUnitTypeId(GetTriggerUnit()) == 1747988533) then
		return false
	endif
	return true
endfunction

function Trig_DEMONPORTAL_Func001Func001C takes nothing returns boolean
	if  not (GetUnitTypeId(GetTriggerUnit()) == 1966092339) then
		return false
	endif
	return true
endfunction

function Trig_DEMONPORTAL_Func001Func001Func001C takes nothing returns boolean
	if  not (GetUnitTypeId(GetTriggerUnit()) == 1747988536) then
		return false
	endif
	return true
endfunction

function Trig_DEMONPORTAL_Func001Func001Func001Func001Func001Func001C takes nothing returns boolean
	if  not (GetUnitTypeId(GetTriggerUnit()) == 1966092338) then
		return false
	endif
	return true
endfunction

function Trig_DEMONPORTAL_Func001Func001Func001Func001C takes nothing returns boolean
	if  not (GetUnitTypeId(GetTriggerUnit()) == 1747988545) then
		return false
	endif
	return true
endfunction

function Trig_DEMONPORTAL_Actions takes nothing returns nothing
	if Trig_DEMONPORTAL_Func001C() then
		call SetUnitScalePercent(GetTriggerUnit(), 30.00, 30.00, 30.00)
	else
		if Trig_DEMONPORTAL_Func001Func001C() then
			call SetUnitScalePercent(GetTriggerUnit(), 80.00, 80.00, 80.00)
		else
			if Trig_DEMONPORTAL_Func001Func001Func001C() then
				call SetUnitScalePercent(GetTriggerUnit(), 90.00, 90.00, 90.00)
			else
				if Trig_DEMONPORTAL_Func001Func001Func001Func001C() then
					call SetUnitScalePercent(GetTriggerUnit(), 100.00, 100.00, 100.00)
				else
					if Trig_DEMONPORTAL_Func001Func001Func001Func001Func001C() then
						call SetUnitScalePercent(GetTriggerUnit(), 90.00, 90.00, 90.00)
					else
						if Trig_DEMONPORTAL_Func001Func001Func001Func001Func001Func001C() then
							call SetUnitScalePercent(GetTriggerUnit(), 90.00, 90.00, 90.00)
						endif
					endif
				endif
			endif
		endif
	endif
endfunction

function InitTrig_DEMONPORTAL takes nothing returns nothing
	set gg_trg_DEMONPORTAL = CreateTrigger()
	call TriggerRegisterAnyUnitEventBJ(gg_trg_DEMONPORTAL, EVENT_PLAYER_UNIT_UPGRADE_START)
	call TriggerAddAction(gg_trg_DEMONPORTAL, function Trig_DEMONPORTAL_Actions)
endfunction

function Trig_Melee_Initialization_Actions takes nothing returns nothing
	call MeleeStartingVisibility()
	call MeleeStartingHeroLimit()
	call MeleeGrantHeroItems()
	call MeleeStartingResources()
	call MeleeClearExcessUnits()
	call MeleeStartingUnits()
	call MeleeStartingAI()
	set bj_forLoopAIndex = 1
	set bj_forLoopAIndexEnd = 12
	loop
		exitwhen bj_forLoopAIndex > bj_forLoopAIndexEnd
		call SetPlayerTechMaxAllowedSwap(1747988548, 1, ConvertedPlayer(GetForLoopIndexA()))
		call SetPlayerTechMaxAllowedSwap(1865429047, 5, ConvertedPlayer(GetForLoopIndexA()))
		call SetPlayerTechMaxAllowedSwap(1211117617, 1, ConvertedPlayer(GetForLoopIndexA()))
		call SetPlayerTechMaxAllowedSwap(1311780915, 1, ConvertedPlayer(GetForLoopIndexA()))
		call SetPlayerTechMaxAllowedSwap(1328558134, 1, ConvertedPlayer(GetForLoopIndexA()))
		set bj_forLoopAIndex = bj_forLoopAIndex + 1
	endloop
endfunction

function InitTrig_Melee_Initialization takes nothing returns nothing
	set gg_trg_Melee_Initialization = CreateTrigger()
	call TriggerAddAction(gg_trg_Melee_Initialization, function Trig_Melee_Initialization_Actions)
endfunction

function Trig_Exploding_Buildings_Conditions takes nothing returns boolean
	if  not (GetUnitAbilityLevelSwapped(1093677141, GetTriggerUnit()) > 0) then
		return false
	endif
	return true
endfunction

function Trig_Exploding_Buildings_Func002Func001C takes nothing returns boolean
	if  not (IsUnitEnemy(GetEnumUnit(), GetOwningPlayer(GetDyingUnit())) == true) then
		return false
	endif
	return true
endfunction

function Trig_Exploding_Buildings_Func002A takes nothing returns nothing
	if Trig_Exploding_Buildings_Func002Func001C() then
		call UnitDamageTargetBJ(GetDyingUnit(), GetEnumUnit(), 200.00, ATTACK_TYPE_CHAOS, DAMAGE_TYPE_FIRE)
	endif
endfunction

function Trig_Exploding_Buildings_Actions takes nothing returns nothing
	call AddSpecialEffectLocBJ(GetUnitLoc(GetDyingUnit()), "Abilities\\Spells\\Other\\Incinerate\\FireLordDeathExplode.mdl")
	call ForGroupBJ(GetUnitsInRangeOfLocAll(250.00, GetUnitLoc(GetDyingUnit())), function Trig_Exploding_Buildings_Func002A)
endfunction

function InitTrig_Exploding_Buildings takes nothing returns nothing
	set gg_trg_Exploding_Buildings = CreateTrigger()
	call TriggerRegisterAnyUnitEventBJ(gg_trg_Exploding_Buildings, EVENT_PLAYER_UNIT_DEATH)
	call TriggerAddCondition(gg_trg_Exploding_Buildings, Condition(function Trig_Exploding_Buildings_Conditions))
	call TriggerAddAction(gg_trg_Exploding_Buildings, function Trig_Exploding_Buildings_Actions)
endfunction

function Trig_Destroy_Conditions takes nothing returns boolean
	if  not (GetSpellAbilityId() == 1093677361) then
		return false
	endif
	return true
endfunction

function Trig_Destroy_Actions takes nothing returns nothing
	call KillUnit(GetSpellAbilityUnit())
	call AdjustPlayerStateBJ(113, GetOwningPlayer(GetSpellAbilityUnit()), PLAYER_STATE_RESOURCE_GOLD)
endfunction

function InitTrig_Destroy takes nothing returns nothing
	set gg_trg_Destroy = CreateTrigger()
	call TriggerRegisterAnyUnitEventBJ(gg_trg_Destroy, EVENT_PLAYER_UNIT_SPELL_EFFECT)
	call TriggerAddCondition(gg_trg_Destroy, Condition(function Trig_Destroy_Conditions))
	call TriggerAddAction(gg_trg_Destroy, function Trig_Destroy_Actions)
endfunction

function Trig_DEMONPORTAL_CANCEL_Actions takes nothing returns nothing
	call SetUnitScalePercent(GetTriggerUnit(), 120.00, 120.00, 120.00)
endfunction

function Trig_DEMONPORTAL_CANCEL_Conditions takes nothing returns boolean
	if  not (GetUnitTypeId(GetTriggerUnit()) == 1747988535) then
		return false
	endif
	return true
endfunction

function InitTrig_DEMONPORTAL_CANCEL takes nothing returns nothing
	set gg_trg_DEMONPORTAL_CANCEL = CreateTrigger()
	call TriggerRegisterAnyUnitEventBJ(gg_trg_DEMONPORTAL_CANCEL, EVENT_PLAYER_UNIT_UPGRADE_CANCEL)
	call TriggerAddCondition(gg_trg_DEMONPORTAL_CANCEL, Condition(function Trig_DEMONPORTAL_CANCEL_Conditions))
	call TriggerAddAction(gg_trg_DEMONPORTAL_CANCEL, function Trig_DEMONPORTAL_CANCEL_Actions)
endfunction

function Trig_Volcano_Temple_Actions takes nothing returns nothing
	call SetUnitPositionLoc(GetTrainedUnit(), GetUnitLoc(GetTriggerUnit()))
	call SetUnitAnimation(GetTrainedUnit(), "stand")
	call TriggerSleepAction(2.)
	call RemoveUnitFromStockBJ(1747988548, GetTriggerUnit())
endfunction

function Trig_Volcano_Temple_Conditions takes nothing returns boolean
	if  not (GetUnitTypeId(GetTrainedUnit()) == 1747988548) then
		return false
	endif
	return true
endfunction

function InitTrig_Volcano_Temple takes nothing returns nothing
	set gg_trg_Volcano_Temple = CreateTrigger()
	call TriggerRegisterAnyUnitEventBJ(gg_trg_Volcano_Temple, EVENT_PLAYER_UNIT_TRAIN_FINISH)
	call TriggerAddCondition(gg_trg_Volcano_Temple, Condition(function Trig_Volcano_Temple_Conditions))
	call TriggerAddAction(gg_trg_Volcano_Temple, function Trig_Volcano_Temple_Actions)
endfunction

function InitCustomTriggers takes nothing returns nothing
	call InitTrig_Melee_Initialization()
	call InitTrig_ReturnBuildingSpaces()
	call InitTrig_Volcano_Temple()
	call InitTrig_DEMONPORTAL()
	call InitTrig_DEMONPORTAL_CANCEL()
	call InitTrig_Exploding_Buildings()
	call InitTrig_ClearUp_Buildings()
	call InitTrig_Destroy()
endfunction

function RunInitializationTriggers takes nothing returns nothing
	call ConditionalTriggerExecute(gg_trg_Melee_Initialization)
endfunction

function turnToAshInit takes nothing returns nothing
	set ht_2 = InitHashtable()
	set toash = InitHashtable()
	call SaveInteger(toash, 1697656882, 0, 1697656883)
	call SaveReal(toash, 1697656882, 1, 6.)
	call SaveInteger(toash, 1865429040, 0, 1865429042)
	call SaveReal(toash, 1865429040, 1, 8.)
	call SaveInteger(toash, 1865429041, 0, 1865429043)
	call SaveReal(toash, 1865429041, 1, 20.)
	call SaveInteger(toash, 1848651825, 0, 1848651828)
	call SaveReal(toash, 1848651825, 1, 10.)
	call SaveInteger(toash, 1966092337, 0, 1966092341)
	call SaveReal(toash, 1966092337, 1, 9.)
	call SaveInteger(toash, 1966092340, 0, 1966092336)
	call SaveReal(toash, 1966092340, 1, 6.)
	call SaveInteger(toash, 1865429044, 0, 1865429049)
	call SaveReal(toash, 1865429041, 1, 6.)
	call SaveInteger(toash, 1747988530, 0, 1747988549)
	call SaveReal(toash, 1747988530, 1, 8.)
	call SaveInteger(toash, 1848651824, 0, 1848651829)
	call SaveReal(toash, 1848651829, 1, 9.)
endfunction

function turn takes unit u_3 returns nothing
	local unit v_3
	if LoadInteger(toash, GetUnitTypeId(u_3), 0) != 0 and GetUnitTypeId(u_3) != 1966092340 then
		set v_3 = CreateUnit(GetOwningPlayer(u_3), LoadInteger(toash, GetUnitTypeId(u_3), 0), GetUnitX(u_3), GetUnitY(u_3), GetUnitFacing(u_3))
		call SetUnitTimeScale(v_3, 0.)
		call RemoveUnit(u_3)
	endif
endfunction

function toturn takes nothing returns nothing
	call turn(GetDyingUnit())
endfunction

function check2 takes nothing returns boolean
	if LoadInteger(toash, GetUnitTypeId(GetFilterUnit()), 0) != 0 then
		if UnitHasBuffBJ(GetFilterUnit(), 1110454320) then
			return true
		endif
	endif
	return false
endfunction

function check1 takes nothing returns boolean
	if LoadInteger(toash, GetUnitTypeId(GetFilterUnit()), 0) != 0 then
		if  not UnitHasBuffBJ(GetFilterUnit(), 1110454320) then
			return true
		endif
	endif
	return false
endfunction

function burn1 takes nothing returns nothing
	local unit u_4 = GetEnumUnit()
	local real substr = LoadReal(toash, GetUnitTypeId(u_4), 1)
	if GetPlayerTechCount(GetOwningPlayer(u_4), 1378889783, true) == 1 then
		set substr = substr * 0.8
	endif
	if GetPlayerState(GetOwningPlayer(GetEnumUnit()), PLAYER_STATE_RESOURCE_LUMBER) > 0 then
		call SetPlayerState(GetOwningPlayer(GetEnumUnit()), PLAYER_STATE_RESOURCE_LUMBER, GetPlayerState(GetOwningPlayer(GetEnumUnit()), PLAYER_STATE_RESOURCE_LUMBER) - GetUnitFoodUsed(GetEnumUnit()))
	else
		if GetUnitState(u_4, UNIT_STATE_LIFE) > substr then
			call SetUnitLifeBJ(u_4, GetUnitState(u_4, UNIT_STATE_LIFE) - substr)
		else
			call KillUnit(u_4)
		endif
	endif
endfunction

function burn2 takes nothing returns nothing
	local integer i_6
	if GetPlayerState(GetOwningPlayer(GetEnumUnit()), PLAYER_STATE_RESOURCE_LUMBER) > 0 then
		set i_6 = R2I(I2R(GetUnitFoodUsed(GetEnumUnit())) / 4. + 0.5)
		call SetPlayerState(GetOwningPlayer(GetEnumUnit()), PLAYER_STATE_RESOURCE_LUMBER, GetPlayerState(GetOwningPlayer(GetEnumUnit()), PLAYER_STATE_RESOURCE_LUMBER) - i_6)
	endif
endfunction

function burnchoose takes nothing returns nothing
	call ForGroup(GetUnitsInRectMatching(GetPlayableMapRect(), Condition(function check1)), function burn1)
	call ForGroup(GetUnitsInRectMatching(GetPlayableMapRect(), Condition(function check2)), function burn2)
endfunction

function init_TurnToAsh takes nothing returns nothing
	local trigger t_4
	call turnToAshInit()
	set gg_trg_TurnToAsh = CreateTrigger()
	call TriggerRegisterTimerEvent(gg_trg_TurnToAsh, 0.50, true)
	call TriggerAddAction(gg_trg_TurnToAsh, function burnchoose)
	set t_4 = CreateTrigger()
	call TriggerRegisterAnyUnitEventBJ(t_4, EVENT_PLAYER_UNIT_DEATH)
	call TriggerAddAction(t_4, function toturn)
endfunction

function destroyBurningtrees takes integer this returns nothing
	call RemoveUnit(harvester[this])
	set count_2 = count_2 - 1
	if this != first then
		set next[prev[this]] = next[this]
	endif
	if this != last then
		set prev[next[this]] = prev[this]
	endif
	if this == last then
		set last = prev[this]
	endif
	if this == first then
		set first = next[this]
	endif
	if Burningtrees_nextFree[this] < 0 then
		set Burningtrees_nextFree[this] = Burningtrees_firstFree
		set Burningtrees_firstFree = Burningtrees_firstFree
	else
		call BJDebugMsg("Double Free of Burningtrees")
	endif
endfunction

function Burningtrees_getFirst takes nothing returns integer
	return first
endfunction

function removefires takes nothing returns nothing
	local unit keyunit = GetDyingUnit()
	local integer tree_2 = Burningtrees_getFirst()
	loop
		exitwhen  not (tree_2 != 0)
		if owner_3[tree_2] == keyunit then
			call destroyBurningtrees(tree_2)
			set tree_2 = Burningtrees_getFirst()
		else
			set tree_2 = next[tree_2]
		endif
	endloop
endfunction

function isBusy takes destructable dest returns boolean
	local integer tree_3 = Burningtrees_getFirst()
	loop
		exitwhen  not (tree_3 != 0)
		if d[tree_3] == dest then
			return false
		else
			set tree_3 = next[tree_3]
		endif
	endloop
	return true
endfunction

function checkifavailable takes nothing returns boolean
	if GetDestructableTypeId(GetFilterDestructable()) == 1465152372 then
		if GetDestructableLife(GetFilterDestructable()) > 0. then
			if isBusy(GetFilterDestructable()) then
				return true
			endif
		endif
	endif
	return false
endfunction

function distanceBetweenCoords takes real x1_2, real y1_2, real x2_2, real y2_2 returns real
	return SquareRoot((x2_2 - x1_2) * (x2_2 - x1_2) + (y2_2 - y1_2) * (y2_2 - y1_2))
endfunction

function construct_Burningtrees takes integer this_2, unit u_5, destructable des, unit casting returns nothing
	set count_2 = count_2 + 1
	if count_2 == 1 then
		set first = this_2
		set prev[this_2] = 0
	else
		set prev[this_2] = last
		set next[last] = this_2
		set prev[first] = this_2
	endif
	set next[this_2] = 0
	set last = this_2
	set d[this_2] = des
	set harvester[this_2] = u_5
	set p_6[this_2] = GetOwningPlayer(u_5)
	set rate[this_2] = 2.5
	set owner_3[this_2] = casting
endfunction

function new_Burningtrees takes unit u_6, destructable des_2, unit casting_2 returns integer
	local integer this_3
	if Burningtrees_firstFree > 0 then
		set this_3 = Burningtrees_firstFree
		set Burningtrees_firstFree = Burningtrees_nextFree[this_3]
	else
		set Burningtrees_maxIndex = Burningtrees_maxIndex + 1
		set this_3 = Burningtrees_maxIndex
	endif
	set Burningtrees_nextFree[this_3] = -1
	call construct_Burningtrees(this_3, u_6, des_2, casting_2)
	return this_3
endfunction

function extend takes real x_17, real y_16, player p_7, unit owner_4 returns nothing
	local destructable d_6 = RandomDestructableInRectBJ(Rect(x_17 - 180., y_16 - 180., x_17 + 180., y_16 + 180.), Condition(function checkifavailable))
	local integer tree_4
	local unit u2
	local integer tree_5
	if distanceBetweenCoords(GetDestructableX(d_6), GetDestructableY(d_6), GetUnitX(owner_4), GetUnitY(owner_4)) < 1200. and d_6 != null then
		set u2 = CreateUnitAtLoc(p_7, 1697656880, Location(GetDestructableX(d_6), GetDestructableY(d_6)), 0.)
		call SetBlightLoc(p_7, Location(GetDestructableX(d_6), GetDestructableY(d_6)), 64.00, true)
		call SetBlightLoc(p_7, Location(GetDestructableX(d_6), GetDestructableY(d_6)), 66.00, false)
		call TriggerSleepAction(0.01)
		call IssueTargetOrder(u2, "harvest", d_6)
		set tree_4 = new_Burningtrees(u2, d_6, owner_4)
	else
		set d_6 = RandomDestructableInRectBJ(Rect(GetUnitX(owner_4) - 700., GetUnitY(owner_4) - 700., GetUnitX(owner_4) + 700., GetUnitY(owner_4) + 700.), Condition(function checkifavailable))
		if d_6 != null then
			set u2 = CreateUnitAtLoc(p_7, 1697656880, Location(GetDestructableX(d_6), GetDestructableY(d_6)), 0.)
			call SetBlightLoc(p_7, Location(GetDestructableX(d_6), GetDestructableY(d_6)), 64.00, true)
			call SetBlightLoc(p_7, Location(GetDestructableX(d_6), GetDestructableY(d_6)), 66.00, false)
			call TriggerSleepAction(0.01)
			call IssueTargetOrder(u2, "harvest", d_6)
			set tree_5 = new_Burningtrees(u2, d_6, owner_4)
		endif
	endif
endfunction

function Burningtrees_decreaselife takes integer this_4 returns nothing
	if GetDestructableLife(d[this_4]) < rate[this_4] then
		call KillDestructable(d[this_4])
	else
		call SetDestructableLife(d[this_4], GetDestructableLife(d[this_4]) - rate[this_4])
	endif
endfunction

function foreachtree takes nothing returns nothing
	local integer tree_6 = Burningtrees_getFirst()
	loop
		exitwhen  not (tree_6 != 0)
		if GetDestructableLife(d[tree_6]) <= rate[tree_6] then
			call KillDestructable(d[tree_6])
			call extend(GetUnitX(harvester[tree_6]), GetUnitY(harvester[tree_6]), p_6[tree_6], owner_3[tree_6])
			call destroyBurningtrees(tree_6)
			set tree_6 = Burningtrees_getFirst()
		else
			call Burningtrees_decreaselife(tree_6)
			if GetRandomInt(1, 400) < 7 + 4 * GetPlayerTechCount(p_6[tree_6], 1378889785, true) then
				call extend(GetDestructableX(d[tree_6]), GetDestructableY(d[tree_6]), p_6[tree_6], owner_3[tree_6])
			endif
			set tree_6 = next[tree_6]
		endif
	endloop
endfunction

function startburning takes nothing returns nothing
	local unit caster = GetSpellAbilityUnit()
	local unit u_7
	local destructable d_9
	local integer tree_7
	if GetSpellAbilityId() == 1093677134 then
		set d_9 = GetSpellTargetDestructable()
		call SetDestructableLife(d_9, GetDestructableLife(d_9) - 0.01)
		call SetDestructableLife(d_9, GetDestructableLife(d_9) + 0.01)
		set u_7 = CreateUnitAtLoc(GetOwningPlayer(caster), 1697656880, Location(GetDestructableX(d_9), GetDestructableY(d_9)), 0.)
		set tree_7 = new_Burningtrees(u_7, d_9, caster)
		call SetBlightLoc(GetOwningPlayer(u_7), Location(GetDestructableX(d_9), GetDestructableY(d_9)), 64.00, true)
		call SetBlightLoc(GetOwningPlayer(u_7), Location(GetDestructableX(d_9), GetDestructableY(d_9)), 66.00, false)
		call TriggerSleepAction(0.01)
		call IssueTargetOrder(u_7, "harvest", d_9)
	endif
endfunction

function init_Harvest takes nothing returns nothing
	local trigger t3
	local trigger t_5
	local trigger t2
	set first = 0
	set last = 0
	set count_2 = 0
	set t_5 = CreateTrigger()
	set t2 = CreateTrigger()
	set t3 = CreateTrigger()
	call TriggerRegisterTimerEvent(t2, 1.8, true)
	call TriggerRegisterAnyUnitEventBJ(t3, EVENT_PLAYER_UNIT_DEATH)
	call TriggerAddAction(t3, function removefires)
	call TriggerRegisterAnyUnitEventBJ(t_5, EVENT_PLAYER_UNIT_SPELL_EFFECT)
	call TriggerAddAction(t_5, function startburning)
	call TriggerAddAction(t2, function foreachtree)
endfunction

function revive takes nothing returns nothing
	local unit u_8
	if GetSpellAbilityId() == 1093677122 or GetSpellAbilityId() == 1093677144 then
		if LoadInteger(fromash, GetUnitTypeId(GetSpellTargetUnit()), 0) != 0 then
			if GetPlayerState(GetOwningPlayer(GetSpellAbilityUnit()), PLAYER_STATE_RESOURCE_FOOD_CAP) > GetPlayerState(GetOwningPlayer(GetSpellAbilityUnit()), PLAYER_STATE_RESOURCE_FOOD_USED) + GetFoodUsed(LoadInteger(fromash, GetUnitTypeId(GetSpellTargetUnit()), 0) - 1) then
				call SetUnitOwner(GetEnumUnit(), GetOwningPlayer(GetSpellAbilityUnit()), true)
				set u_8 = ReplaceUnitBJ(GetSpellTargetUnit(), LoadInteger(fromash, GetUnitTypeId(GetSpellTargetUnit()), 0), bj_UNIT_STATE_METHOD_DEFAULTS)
				call SetUnitOwner(u_8, GetOwningPlayer(GetSpellAbilityUnit()), true)
				if GetSpellAbilityId() == 1093677122 then
					call RemoveUnit(GetSpellAbilityUnit())
				endif
			else
				call DisplayTimedTextToPlayer(GetOwningPlayer(GetSpellAbilityUnit()), 100., 100., 30., "|cffffcc00 Not Enough Food!|r")
			endif
		else
			call DisplayTimedTextToPlayer(GetOwningPlayer(GetSpellAbilityUnit()), 0., 0., 30., "|cffffcc00 Can't target this unit!|r")
		endif
	endif
endfunction

function initReviveAshes takes nothing returns nothing
	set fromash = InitHashtable()
	call SaveInteger(fromash, 1697656883, 0, 1697656882)
	call SaveInteger(fromash, 1865429043, 0, 1865429041)
	call SaveInteger(fromash, 1865429042, 0, 1865429040)
	call SaveInteger(fromash, 1848651828, 0, 1848651825)
	call SaveInteger(fromash, 1966092341, 0, 1966092337)
	call SaveInteger(fromash, 1966092336, 0, 1966092340)
	call SaveInteger(fromash, 1865429049, 0, 1865429044)
	call SaveInteger(fromash, 1747988549, 0, 1747988530)
	call SaveInteger(fromash, 1848651829, 0, 1848651824)
endfunction

function init_ReviveAshes takes nothing returns nothing
	call initReviveAshes()
	set gg_trg_ReviveAshes = CreateTrigger()
	call TriggerRegisterAnyUnitEventBJ(gg_trg_ReviveAshes, EVENT_PLAYER_UNIT_SPELL_EFFECT)
	call TriggerAddAction(gg_trg_ReviveAshes, function revive)
endfunction

function init_PrintingHelper takes nothing returns nothing
	set DEBUG_LEVEL = 1
endfunction

function InitGlobals takes nothing returns nothing
	set udg_x = 0.
	set udg_y = 0.
endfunction

function main takes nothing returns nothing
	call SetCameraBounds(( - 8192.0) + GetCameraMargin(CAMERA_MARGIN_LEFT), ( - 8192.0) + GetCameraMargin(CAMERA_MARGIN_BOTTOM), 8192.0 - GetCameraMargin(CAMERA_MARGIN_RIGHT), 8192.0 - GetCameraMargin(CAMERA_MARGIN_TOP), ( - 8192.0) + GetCameraMargin(CAMERA_MARGIN_LEFT), 8192.0 - GetCameraMargin(CAMERA_MARGIN_TOP), 8192.0 - GetCameraMargin(CAMERA_MARGIN_RIGHT), ( - 8192.0) + GetCameraMargin(CAMERA_MARGIN_BOTTOM))
	call SetDayNightModels("Environment\\DNC\\DNCLordaeron\\DNCLordaeronTerrain\\DNCLordaeronTerrain.mdl", "Environment\\DNC\\DNCLordaeron\\DNCLordaeronUnit\\DNCLordaeronUnit.mdl")
	call NewSoundEnvironment("Default")
	call SetAmbientDaySound("NorthrendDay")
	call SetAmbientNightSound("NorthrendNight")
	call SetMapMusic("Music", true, 0)
	call CreateAllUnits()
	call InitBlizzard()
	call InitGlobals()
	call InitCustomTriggers()
	call RunInitializationTriggers()
	call init_PrintingHelper()
	call init_ReviveAshes()
	call init_SunRescur()
	call init_Init()
	call init_Maths()
	call init_TimerUtils()
	call init_Harvest()
	call init_TurnToAsh()
endfunction

function InitAllyPriorities takes nothing returns nothing
	call SetStartLocPrioCount(0, 2)
	call SetStartLocPrio(0, 0, 1, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(0, 1, 2, MAP_LOC_PRIO_LOW)
	call SetStartLocPrioCount(1, 2)
	call SetStartLocPrio(1, 0, 0, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(1, 1, 2, MAP_LOC_PRIO_LOW)
	call SetStartLocPrioCount(2, 2)
	call SetStartLocPrio(2, 0, 1, MAP_LOC_PRIO_LOW)
	call SetStartLocPrio(2, 1, 3, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrioCount(3, 2)
	call SetStartLocPrio(3, 0, 1, MAP_LOC_PRIO_LOW)
	call SetStartLocPrio(3, 1, 2, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrioCount(4, 2)
	call SetStartLocPrio(4, 0, 5, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(4, 1, 6, MAP_LOC_PRIO_LOW)
	call SetStartLocPrioCount(5, 2)
	call SetStartLocPrio(5, 0, 4, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrio(5, 1, 6, MAP_LOC_PRIO_LOW)
	call SetStartLocPrioCount(6, 2)
	call SetStartLocPrio(6, 0, 5, MAP_LOC_PRIO_LOW)
	call SetStartLocPrio(6, 1, 7, MAP_LOC_PRIO_HIGH)
	call SetStartLocPrioCount(7, 2)
	call SetStartLocPrio(7, 0, 5, MAP_LOC_PRIO_LOW)
	call SetStartLocPrio(7, 1, 6, MAP_LOC_PRIO_HIGH)
endfunction

function InitCustomPlayerSlots takes nothing returns nothing
	call SetPlayerStartLocation(Player(0), 0)
	call SetPlayerColor(Player(0), ConvertPlayerColor(0))
	call SetPlayerRacePreference(Player(0), RACE_PREF_HUMAN)
	call SetPlayerRaceSelectable(Player(0), true)
	call SetPlayerController(Player(0), MAP_CONTROL_USER)
	call SetPlayerStartLocation(Player(1), 1)
	call SetPlayerColor(Player(1), ConvertPlayerColor(1))
	call SetPlayerRacePreference(Player(1), RACE_PREF_ORC)
	call SetPlayerRaceSelectable(Player(1), true)
	call SetPlayerController(Player(1), MAP_CONTROL_USER)
	call SetPlayerStartLocation(Player(2), 2)
	call SetPlayerColor(Player(2), ConvertPlayerColor(2))
	call SetPlayerRacePreference(Player(2), RACE_PREF_UNDEAD)
	call SetPlayerRaceSelectable(Player(2), true)
	call SetPlayerController(Player(2), MAP_CONTROL_USER)
	call SetPlayerStartLocation(Player(3), 3)
	call SetPlayerColor(Player(3), ConvertPlayerColor(3))
	call SetPlayerRacePreference(Player(3), RACE_PREF_NIGHTELF)
	call SetPlayerRaceSelectable(Player(3), true)
	call SetPlayerController(Player(3), MAP_CONTROL_USER)
	call SetPlayerStartLocation(Player(4), 4)
	call SetPlayerColor(Player(4), ConvertPlayerColor(4))
	call SetPlayerRacePreference(Player(4), RACE_PREF_HUMAN)
	call SetPlayerRaceSelectable(Player(4), true)
	call SetPlayerController(Player(4), MAP_CONTROL_USER)
	call SetPlayerStartLocation(Player(5), 5)
	call SetPlayerColor(Player(5), ConvertPlayerColor(5))
	call SetPlayerRacePreference(Player(5), RACE_PREF_ORC)
	call SetPlayerRaceSelectable(Player(5), true)
	call SetPlayerController(Player(5), MAP_CONTROL_USER)
	call SetPlayerStartLocation(Player(6), 6)
	call SetPlayerColor(Player(6), ConvertPlayerColor(6))
	call SetPlayerRacePreference(Player(6), RACE_PREF_UNDEAD)
	call SetPlayerRaceSelectable(Player(6), true)
	call SetPlayerController(Player(6), MAP_CONTROL_USER)
	call SetPlayerStartLocation(Player(7), 7)
	call SetPlayerColor(Player(7), ConvertPlayerColor(7))
	call SetPlayerRacePreference(Player(7), RACE_PREF_NIGHTELF)
	call SetPlayerRaceSelectable(Player(7), true)
	call SetPlayerController(Player(7), MAP_CONTROL_USER)
endfunction

function config takes nothing returns nothing
	call SetMapName("TRIGSTR_001")
	call SetMapDescription("TRIGSTR_013")
	call SetPlayers(8)
	call SetTeams(8)
	call SetGamePlacement(MAP_PLACEMENT_TEAMS_TOGETHER)
	call DefineStartLocation(0, 5440.0,  - 7296.0)
	call DefineStartLocation(1, 7040.0,  - 4160.0)
	call DefineStartLocation(2, 6656.0, 2368.0)
	call DefineStartLocation(3, 5440.0, 6976.0)
	call DefineStartLocation(4,  - 6080.0, 7168.0)
	call DefineStartLocation(5,  - 6592.0, 1920.0)
	call DefineStartLocation(6,  - 6400.0,  - 2112.0)
	call DefineStartLocation(7,  - 6336.0,  - 6656.0)
	call InitCustomPlayerSlots()
	call SetPlayerSlotAvailable(Player(0), MAP_CONTROL_USER)
	call SetPlayerSlotAvailable(Player(1), MAP_CONTROL_USER)
	call SetPlayerSlotAvailable(Player(2), MAP_CONTROL_USER)
	call SetPlayerSlotAvailable(Player(3), MAP_CONTROL_USER)
	call SetPlayerSlotAvailable(Player(4), MAP_CONTROL_USER)
	call SetPlayerSlotAvailable(Player(5), MAP_CONTROL_USER)
	call SetPlayerSlotAvailable(Player(6), MAP_CONTROL_USER)
	call SetPlayerSlotAvailable(Player(7), MAP_CONTROL_USER)
	call InitGenericPlayerSlots()
	call InitAllyPriorities()
endfunction

