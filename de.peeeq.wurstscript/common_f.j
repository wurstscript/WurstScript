type agent extends handletype event extends agenttype player extends agenttype widget extends agenttype unit extends widgettype destructable extends widgettype item extends widgettype ability extends agenttype buff extends abilitytype force extends agenttype group extends agenttype trigger extends agenttype triggercondition extends agenttype triggeraction extends handletype timer extends agenttype location extends agenttype region extends agenttype rect extends agenttype boolexpr extends agenttype sound extends agenttype conditionfunc extends boolexprtype filterfunc extends boolexprtype unitpool extends handletype itempool extends handletype race extends handletype alliancetype extends handletype racepreference extends handletype gamestate extends handletype igamestate extends gamestatetype fgamestate extends gamestatetype playerstate extends handletype playerscore extends handletype playergameresult extends handletype unitstate extends handletype aidifficulty extends handletype eventid extends handletype gameevent extends eventidtype playerevent extends eventidtype playerunitevent extends eventidtype unitevent extends eventidtype limitop extends eventidtype widgetevent extends eventidtype dialogevent extends eventidtype unittype extends handletype gamespeed extends handletype gamedifficulty extends handletype gametype extends handletype mapflag extends handletype mapvisibility extends handletype mapsetting extends handletype mapdensity extends handletype mapcontrol extends handletype playerslotstate extends handletype volumegroup extends handletype camerafield extends handletype camerasetup extends handletype playercolor extends handletype placement extends handletype startlocprio extends handletype raritycontrol extends handletype blendmode extends handletype texmapflags extends handletype effect extends agenttype effecttype extends handletype weathereffect extends handletype terraindeformation extends handletype fogstate extends handletype fogmodifier extends agenttype dialog extends agenttype button extends agenttype quest extends agenttype questitem extends agenttype defeatcondition extends agenttype timerdialog extends agenttype leaderboard extends agenttype multiboard extends agenttype multiboarditem extends agenttype trackable extends agenttype gamecache extends agenttype version extends handletype itemtype extends handletype texttag extends handletype attacktype extends handletype damagetype extends handletype weapontype extends handletype soundtype extends handletype lightning extends handletype pathingtype extends handletype image extends handletype ubersplat extends handletype hashtable extends agentglobals
boolean FALSE=JassExprBoolVal(false)
boolean TRUE=JassExprBoolVal(true)
integer JASS_MAX_ARRAY_SIZE=JassExprIntVal(8192)
integer PLAYER_NEUTRAL_PASSIVE=JassExprIntVal(15)
integer PLAYER_NEUTRAL_AGGRESSIVE=JassExprIntVal(12)
playercolor PLAYER_COLOR_RED=JassExprFunctionCall(ConvertPlayerColor, JassExprlist(JassExprIntVal(0)))
playercolor PLAYER_COLOR_BLUE=JassExprFunctionCall(ConvertPlayerColor, JassExprlist(JassExprIntVal(1)))
playercolor PLAYER_COLOR_CYAN=JassExprFunctionCall(ConvertPlayerColor, JassExprlist(JassExprIntVal(2)))
playercolor PLAYER_COLOR_PURPLE=JassExprFunctionCall(ConvertPlayerColor, JassExprlist(JassExprIntVal(3)))
playercolor PLAYER_COLOR_YELLOW=JassExprFunctionCall(ConvertPlayerColor, JassExprlist(JassExprIntVal(4)))
playercolor PLAYER_COLOR_ORANGE=JassExprFunctionCall(ConvertPlayerColor, JassExprlist(JassExprIntVal(5)))
playercolor PLAYER_COLOR_GREEN=JassExprFunctionCall(ConvertPlayerColor, JassExprlist(JassExprIntVal(6)))
playercolor PLAYER_COLOR_PINK=JassExprFunctionCall(ConvertPlayerColor, JassExprlist(JassExprIntVal(7)))
playercolor PLAYER_COLOR_LIGHT_GRAY=JassExprFunctionCall(ConvertPlayerColor, JassExprlist(JassExprIntVal(8)))
playercolor PLAYER_COLOR_LIGHT_BLUE=JassExprFunctionCall(ConvertPlayerColor, JassExprlist(JassExprIntVal(9)))
playercolor PLAYER_COLOR_AQUA=JassExprFunctionCall(ConvertPlayerColor, JassExprlist(JassExprIntVal(10)))
playercolor PLAYER_COLOR_BROWN=JassExprFunctionCall(ConvertPlayerColor, JassExprlist(JassExprIntVal(11)))
race RACE_HUMAN=JassExprFunctionCall(ConvertRace, JassExprlist(JassExprIntVal(1)))
race RACE_ORC=JassExprFunctionCall(ConvertRace, JassExprlist(JassExprIntVal(2)))
race RACE_UNDEAD=JassExprFunctionCall(ConvertRace, JassExprlist(JassExprIntVal(3)))
race RACE_NIGHTELF=JassExprFunctionCall(ConvertRace, JassExprlist(JassExprIntVal(4)))
race RACE_DEMON=JassExprFunctionCall(ConvertRace, JassExprlist(JassExprIntVal(5)))
race RACE_OTHER=JassExprFunctionCall(ConvertRace, JassExprlist(JassExprIntVal(7)))
playergameresult PLAYER_GAME_RESULT_VICTORY=JassExprFunctionCall(ConvertPlayerGameResult, JassExprlist(JassExprIntVal(0)))
playergameresult PLAYER_GAME_RESULT_DEFEAT=JassExprFunctionCall(ConvertPlayerGameResult, JassExprlist(JassExprIntVal(1)))
playergameresult PLAYER_GAME_RESULT_TIE=JassExprFunctionCall(ConvertPlayerGameResult, JassExprlist(JassExprIntVal(2)))
playergameresult PLAYER_GAME_RESULT_NEUTRAL=JassExprFunctionCall(ConvertPlayerGameResult, JassExprlist(JassExprIntVal(3)))
alliancetype ALLIANCE_PASSIVE=JassExprFunctionCall(ConvertAllianceType, JassExprlist(JassExprIntVal(0)))
alliancetype ALLIANCE_HELP_REQUEST=JassExprFunctionCall(ConvertAllianceType, JassExprlist(JassExprIntVal(1)))
alliancetype ALLIANCE_HELP_RESPONSE=JassExprFunctionCall(ConvertAllianceType, JassExprlist(JassExprIntVal(2)))
alliancetype ALLIANCE_SHARED_XP=JassExprFunctionCall(ConvertAllianceType, JassExprlist(JassExprIntVal(3)))
alliancetype ALLIANCE_SHARED_SPELLS=JassExprFunctionCall(ConvertAllianceType, JassExprlist(JassExprIntVal(4)))
alliancetype ALLIANCE_SHARED_VISION=JassExprFunctionCall(ConvertAllianceType, JassExprlist(JassExprIntVal(5)))
alliancetype ALLIANCE_SHARED_CONTROL=JassExprFunctionCall(ConvertAllianceType, JassExprlist(JassExprIntVal(6)))
alliancetype ALLIANCE_SHARED_ADVANCED_CONTROL=JassExprFunctionCall(ConvertAllianceType, JassExprlist(JassExprIntVal(7)))
alliancetype ALLIANCE_RESCUABLE=JassExprFunctionCall(ConvertAllianceType, JassExprlist(JassExprIntVal(8)))
alliancetype ALLIANCE_SHARED_VISION_FORCED=JassExprFunctionCall(ConvertAllianceType, JassExprlist(JassExprIntVal(9)))
version VERSION_REIGN_OF_CHAOS=JassExprFunctionCall(ConvertVersion, JassExprlist(JassExprIntVal(0)))
version VERSION_FROZEN_THRONE=JassExprFunctionCall(ConvertVersion, JassExprlist(JassExprIntVal(1)))
attacktype ATTACK_TYPE_NORMAL=JassExprFunctionCall(ConvertAttackType, JassExprlist(JassExprIntVal(0)))
attacktype ATTACK_TYPE_MELEE=JassExprFunctionCall(ConvertAttackType, JassExprlist(JassExprIntVal(1)))
attacktype ATTACK_TYPE_PIERCE=JassExprFunctionCall(ConvertAttackType, JassExprlist(JassExprIntVal(2)))
attacktype ATTACK_TYPE_SIEGE=JassExprFunctionCall(ConvertAttackType, JassExprlist(JassExprIntVal(3)))
attacktype ATTACK_TYPE_MAGIC=JassExprFunctionCall(ConvertAttackType, JassExprlist(JassExprIntVal(4)))
attacktype ATTACK_TYPE_CHAOS=JassExprFunctionCall(ConvertAttackType, JassExprlist(JassExprIntVal(5)))
attacktype ATTACK_TYPE_HERO=JassExprFunctionCall(ConvertAttackType, JassExprlist(JassExprIntVal(6)))
damagetype DAMAGE_TYPE_UNKNOWN=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(0)))
damagetype DAMAGE_TYPE_NORMAL=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(4)))
damagetype DAMAGE_TYPE_ENHANCED=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(5)))
damagetype DAMAGE_TYPE_FIRE=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(8)))
damagetype DAMAGE_TYPE_COLD=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(9)))
damagetype DAMAGE_TYPE_LIGHTNING=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(10)))
damagetype DAMAGE_TYPE_POISON=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(11)))
damagetype DAMAGE_TYPE_DISEASE=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(12)))
damagetype DAMAGE_TYPE_DIVINE=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(13)))
damagetype DAMAGE_TYPE_MAGIC=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(14)))
damagetype DAMAGE_TYPE_SONIC=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(15)))
damagetype DAMAGE_TYPE_ACID=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(16)))
damagetype DAMAGE_TYPE_FORCE=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(17)))
damagetype DAMAGE_TYPE_DEATH=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(18)))
damagetype DAMAGE_TYPE_MIND=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(19)))
damagetype DAMAGE_TYPE_PLANT=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(20)))
damagetype DAMAGE_TYPE_DEFENSIVE=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(21)))
damagetype DAMAGE_TYPE_DEMOLITION=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(22)))
damagetype DAMAGE_TYPE_SLOW_POISON=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(23)))
damagetype DAMAGE_TYPE_SPIRIT_LINK=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(24)))
damagetype DAMAGE_TYPE_SHADOW_STRIKE=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(25)))
damagetype DAMAGE_TYPE_UNIVERSAL=JassExprFunctionCall(ConvertDamageType, JassExprlist(JassExprIntVal(26)))
weapontype WEAPON_TYPE_WHOKNOWS=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(0)))
weapontype WEAPON_TYPE_METAL_LIGHT_CHOP=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(1)))
weapontype WEAPON_TYPE_METAL_MEDIUM_CHOP=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(2)))
weapontype WEAPON_TYPE_METAL_HEAVY_CHOP=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(3)))
weapontype WEAPON_TYPE_METAL_LIGHT_SLICE=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(4)))
weapontype WEAPON_TYPE_METAL_MEDIUM_SLICE=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(5)))
weapontype WEAPON_TYPE_METAL_HEAVY_SLICE=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(6)))
weapontype WEAPON_TYPE_METAL_MEDIUM_BASH=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(7)))
weapontype WEAPON_TYPE_METAL_HEAVY_BASH=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(8)))
weapontype WEAPON_TYPE_METAL_MEDIUM_STAB=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(9)))
weapontype WEAPON_TYPE_METAL_HEAVY_STAB=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(10)))
weapontype WEAPON_TYPE_WOOD_LIGHT_SLICE=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(11)))
weapontype WEAPON_TYPE_WOOD_MEDIUM_SLICE=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(12)))
weapontype WEAPON_TYPE_WOOD_HEAVY_SLICE=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(13)))
weapontype WEAPON_TYPE_WOOD_LIGHT_BASH=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(14)))
weapontype WEAPON_TYPE_WOOD_MEDIUM_BASH=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(15)))
weapontype WEAPON_TYPE_WOOD_HEAVY_BASH=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(16)))
weapontype WEAPON_TYPE_WOOD_LIGHT_STAB=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(17)))
weapontype WEAPON_TYPE_WOOD_MEDIUM_STAB=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(18)))
weapontype WEAPON_TYPE_CLAW_LIGHT_SLICE=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(19)))
weapontype WEAPON_TYPE_CLAW_MEDIUM_SLICE=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(20)))
weapontype WEAPON_TYPE_CLAW_HEAVY_SLICE=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(21)))
weapontype WEAPON_TYPE_AXE_MEDIUM_CHOP=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(22)))
weapontype WEAPON_TYPE_ROCK_HEAVY_BASH=JassExprFunctionCall(ConvertWeaponType, JassExprlist(JassExprIntVal(23)))
pathingtype PATHING_TYPE_ANY=JassExprFunctionCall(ConvertPathingType, JassExprlist(JassExprIntVal(0)))
pathingtype PATHING_TYPE_WALKABILITY=JassExprFunctionCall(ConvertPathingType, JassExprlist(JassExprIntVal(1)))
pathingtype PATHING_TYPE_FLYABILITY=JassExprFunctionCall(ConvertPathingType, JassExprlist(JassExprIntVal(2)))
pathingtype PATHING_TYPE_BUILDABILITY=JassExprFunctionCall(ConvertPathingType, JassExprlist(JassExprIntVal(3)))
pathingtype PATHING_TYPE_PEONHARVESTPATHING=JassExprFunctionCall(ConvertPathingType, JassExprlist(JassExprIntVal(4)))
pathingtype PATHING_TYPE_BLIGHTPATHING=JassExprFunctionCall(ConvertPathingType, JassExprlist(JassExprIntVal(5)))
pathingtype PATHING_TYPE_FLOATABILITY=JassExprFunctionCall(ConvertPathingType, JassExprlist(JassExprIntVal(6)))
pathingtype PATHING_TYPE_AMPHIBIOUSPATHING=JassExprFunctionCall(ConvertPathingType, JassExprlist(JassExprIntVal(7)))
racepreference RACE_PREF_HUMAN=JassExprFunctionCall(ConvertRacePref, JassExprlist(JassExprIntVal(1)))
racepreference RACE_PREF_ORC=JassExprFunctionCall(ConvertRacePref, JassExprlist(JassExprIntVal(2)))
racepreference RACE_PREF_NIGHTELF=JassExprFunctionCall(ConvertRacePref, JassExprlist(JassExprIntVal(4)))
racepreference RACE_PREF_UNDEAD=JassExprFunctionCall(ConvertRacePref, JassExprlist(JassExprIntVal(8)))
racepreference RACE_PREF_DEMON=JassExprFunctionCall(ConvertRacePref, JassExprlist(JassExprIntVal(16)))
racepreference RACE_PREF_RANDOM=JassExprFunctionCall(ConvertRacePref, JassExprlist(JassExprIntVal(32)))
racepreference RACE_PREF_USER_SELECTABLE=JassExprFunctionCall(ConvertRacePref, JassExprlist(JassExprIntVal(64)))
mapcontrol MAP_CONTROL_USER=JassExprFunctionCall(ConvertMapControl, JassExprlist(JassExprIntVal(0)))
mapcontrol MAP_CONTROL_COMPUTER=JassExprFunctionCall(ConvertMapControl, JassExprlist(JassExprIntVal(1)))
mapcontrol MAP_CONTROL_RESCUABLE=JassExprFunctionCall(ConvertMapControl, JassExprlist(JassExprIntVal(2)))
mapcontrol MAP_CONTROL_NEUTRAL=JassExprFunctionCall(ConvertMapControl, JassExprlist(JassExprIntVal(3)))
mapcontrol MAP_CONTROL_CREEP=JassExprFunctionCall(ConvertMapControl, JassExprlist(JassExprIntVal(4)))
mapcontrol MAP_CONTROL_NONE=JassExprFunctionCall(ConvertMapControl, JassExprlist(JassExprIntVal(5)))
gametype GAME_TYPE_MELEE=JassExprFunctionCall(ConvertGameType, JassExprlist(JassExprIntVal(1)))
gametype GAME_TYPE_FFA=JassExprFunctionCall(ConvertGameType, JassExprlist(JassExprIntVal(2)))
gametype GAME_TYPE_USE_MAP_SETTINGS=JassExprFunctionCall(ConvertGameType, JassExprlist(JassExprIntVal(4)))
gametype GAME_TYPE_BLIZ=JassExprFunctionCall(ConvertGameType, JassExprlist(JassExprIntVal(8)))
gametype GAME_TYPE_ONE_ON_ONE=JassExprFunctionCall(ConvertGameType, JassExprlist(JassExprIntVal(16)))
gametype GAME_TYPE_TWO_TEAM_PLAY=JassExprFunctionCall(ConvertGameType, JassExprlist(JassExprIntVal(32)))
gametype GAME_TYPE_THREE_TEAM_PLAY=JassExprFunctionCall(ConvertGameType, JassExprlist(JassExprIntVal(64)))
gametype GAME_TYPE_FOUR_TEAM_PLAY=JassExprFunctionCall(ConvertGameType, JassExprlist(JassExprIntVal(128)))
mapflag MAP_FOG_HIDE_TERRAIN=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprIntVal(1)))
mapflag MAP_FOG_MAP_EXPLORED=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprIntVal(2)))
mapflag MAP_FOG_ALWAYS_VISIBLE=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprIntVal(4)))
mapflag MAP_USE_HANDICAPS=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprIntVal(8)))
mapflag MAP_OBSERVERS=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprIntVal(16)))
mapflag MAP_OBSERVERS_ON_DEATH=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprIntVal(32)))
mapflag MAP_FIXED_COLORS=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprIntVal(128)))
mapflag MAP_LOCK_RESOURCE_TRADING=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprIntVal(256)))
mapflag MAP_RESOURCE_TRADING_ALLIES_ONLY=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprIntVal(512)))
mapflag MAP_LOCK_ALLIANCE_CHANGES=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprIntVal(1024)))
mapflag MAP_ALLIANCE_CHANGES_HIDDEN=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprIntVal(2048)))
mapflag MAP_CHEATS=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprIntVal(4096)))
mapflag MAP_CHEATS_HIDDEN=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprIntVal(8192)))
mapflag MAP_LOCK_SPEED=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprBinary(JassExprIntVal(8192), JassOpMult, JassExprIntVal(2))))
mapflag MAP_LOCK_RANDOM_SEED=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprBinary(JassExprIntVal(8192), JassOpMult, JassExprIntVal(4))))
mapflag MAP_SHARED_ADVANCED_CONTROL=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprBinary(JassExprIntVal(8192), JassOpMult, JassExprIntVal(8))))
mapflag MAP_RANDOM_HERO=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprBinary(JassExprIntVal(8192), JassOpMult, JassExprIntVal(16))))
mapflag MAP_RANDOM_RACES=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprBinary(JassExprIntVal(8192), JassOpMult, JassExprIntVal(32))))
mapflag MAP_RELOADED=JassExprFunctionCall(ConvertMapFlag, JassExprlist(JassExprBinary(JassExprIntVal(8192), JassOpMult, JassExprIntVal(64))))
placement MAP_PLACEMENT_RANDOM=JassExprFunctionCall(ConvertPlacement, JassExprlist(JassExprIntVal(0)))
placement MAP_PLACEMENT_FIXED=JassExprFunctionCall(ConvertPlacement, JassExprlist(JassExprIntVal(1)))
placement MAP_PLACEMENT_USE_MAP_SETTINGS=JassExprFunctionCall(ConvertPlacement, JassExprlist(JassExprIntVal(2)))
placement MAP_PLACEMENT_TEAMS_TOGETHER=JassExprFunctionCall(ConvertPlacement, JassExprlist(JassExprIntVal(3)))
startlocprio MAP_LOC_PRIO_LOW=JassExprFunctionCall(ConvertStartLocPrio, JassExprlist(JassExprIntVal(0)))
startlocprio MAP_LOC_PRIO_HIGH=JassExprFunctionCall(ConvertStartLocPrio, JassExprlist(JassExprIntVal(1)))
startlocprio MAP_LOC_PRIO_NOT=JassExprFunctionCall(ConvertStartLocPrio, JassExprlist(JassExprIntVal(2)))
mapdensity MAP_DENSITY_NONE=JassExprFunctionCall(ConvertMapDensity, JassExprlist(JassExprIntVal(0)))
mapdensity MAP_DENSITY_LIGHT=JassExprFunctionCall(ConvertMapDensity, JassExprlist(JassExprIntVal(1)))
mapdensity MAP_DENSITY_MEDIUM=JassExprFunctionCall(ConvertMapDensity, JassExprlist(JassExprIntVal(2)))
mapdensity MAP_DENSITY_HEAVY=JassExprFunctionCall(ConvertMapDensity, JassExprlist(JassExprIntVal(3)))
gamedifficulty MAP_DIFFICULTY_EASY=JassExprFunctionCall(ConvertGameDifficulty, JassExprlist(JassExprIntVal(0)))
gamedifficulty MAP_DIFFICULTY_NORMAL=JassExprFunctionCall(ConvertGameDifficulty, JassExprlist(JassExprIntVal(1)))
gamedifficulty MAP_DIFFICULTY_HARD=JassExprFunctionCall(ConvertGameDifficulty, JassExprlist(JassExprIntVal(2)))
gamedifficulty MAP_DIFFICULTY_INSANE=JassExprFunctionCall(ConvertGameDifficulty, JassExprlist(JassExprIntVal(3)))
gamespeed MAP_SPEED_SLOWEST=JassExprFunctionCall(ConvertGameSpeed, JassExprlist(JassExprIntVal(0)))
gamespeed MAP_SPEED_SLOW=JassExprFunctionCall(ConvertGameSpeed, JassExprlist(JassExprIntVal(1)))
gamespeed MAP_SPEED_NORMAL=JassExprFunctionCall(ConvertGameSpeed, JassExprlist(JassExprIntVal(2)))
gamespeed MAP_SPEED_FAST=JassExprFunctionCall(ConvertGameSpeed, JassExprlist(JassExprIntVal(3)))
gamespeed MAP_SPEED_FASTEST=JassExprFunctionCall(ConvertGameSpeed, JassExprlist(JassExprIntVal(4)))
playerslotstate PLAYER_SLOT_STATE_EMPTY=JassExprFunctionCall(ConvertPlayerSlotState, JassExprlist(JassExprIntVal(0)))
playerslotstate PLAYER_SLOT_STATE_PLAYING=JassExprFunctionCall(ConvertPlayerSlotState, JassExprlist(JassExprIntVal(1)))
playerslotstate PLAYER_SLOT_STATE_LEFT=JassExprFunctionCall(ConvertPlayerSlotState, JassExprlist(JassExprIntVal(2)))
volumegroup SOUND_VOLUMEGROUP_UNITMOVEMENT=JassExprFunctionCall(ConvertVolumeGroup, JassExprlist(JassExprIntVal(0)))
volumegroup SOUND_VOLUMEGROUP_UNITSOUNDS=JassExprFunctionCall(ConvertVolumeGroup, JassExprlist(JassExprIntVal(1)))
volumegroup SOUND_VOLUMEGROUP_COMBAT=JassExprFunctionCall(ConvertVolumeGroup, JassExprlist(JassExprIntVal(2)))
volumegroup SOUND_VOLUMEGROUP_SPELLS=JassExprFunctionCall(ConvertVolumeGroup, JassExprlist(JassExprIntVal(3)))
volumegroup SOUND_VOLUMEGROUP_UI=JassExprFunctionCall(ConvertVolumeGroup, JassExprlist(JassExprIntVal(4)))
volumegroup SOUND_VOLUMEGROUP_MUSIC=JassExprFunctionCall(ConvertVolumeGroup, JassExprlist(JassExprIntVal(5)))
volumegroup SOUND_VOLUMEGROUP_AMBIENTSOUNDS=JassExprFunctionCall(ConvertVolumeGroup, JassExprlist(JassExprIntVal(6)))
volumegroup SOUND_VOLUMEGROUP_FIRE=JassExprFunctionCall(ConvertVolumeGroup, JassExprlist(JassExprIntVal(7)))
igamestate GAME_STATE_DIVINE_INTERVENTION=JassExprFunctionCall(ConvertIGameState, JassExprlist(JassExprIntVal(0)))
igamestate GAME_STATE_DISCONNECTED=JassExprFunctionCall(ConvertIGameState, JassExprlist(JassExprIntVal(1)))
fgamestate GAME_STATE_TIME_OF_DAY=JassExprFunctionCall(ConvertFGameState, JassExprlist(JassExprIntVal(2)))
playerstate PLAYER_STATE_GAME_RESULT=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(0)))
playerstate PLAYER_STATE_RESOURCE_GOLD=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(1)))
playerstate PLAYER_STATE_RESOURCE_LUMBER=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(2)))
playerstate PLAYER_STATE_RESOURCE_HERO_TOKENS=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(3)))
playerstate PLAYER_STATE_RESOURCE_FOOD_CAP=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(4)))
playerstate PLAYER_STATE_RESOURCE_FOOD_USED=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(5)))
playerstate PLAYER_STATE_FOOD_CAP_CEILING=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(6)))
playerstate PLAYER_STATE_GIVES_BOUNTY=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(7)))
playerstate PLAYER_STATE_ALLIED_VICTORY=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(8)))
playerstate PLAYER_STATE_PLACED=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(9)))
playerstate PLAYER_STATE_OBSERVER_ON_DEATH=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(10)))
playerstate PLAYER_STATE_OBSERVER=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(11)))
playerstate PLAYER_STATE_UNFOLLOWABLE=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(12)))
playerstate PLAYER_STATE_GOLD_UPKEEP_RATE=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(13)))
playerstate PLAYER_STATE_LUMBER_UPKEEP_RATE=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(14)))
playerstate PLAYER_STATE_GOLD_GATHERED=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(15)))
playerstate PLAYER_STATE_LUMBER_GATHERED=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(16)))
playerstate PLAYER_STATE_NO_CREEP_SLEEP=JassExprFunctionCall(ConvertPlayerState, JassExprlist(JassExprIntVal(25)))
unitstate UNIT_STATE_LIFE=JassExprFunctionCall(ConvertUnitState, JassExprlist(JassExprIntVal(0)))
unitstate UNIT_STATE_MAX_LIFE=JassExprFunctionCall(ConvertUnitState, JassExprlist(JassExprIntVal(1)))
unitstate UNIT_STATE_MANA=JassExprFunctionCall(ConvertUnitState, JassExprlist(JassExprIntVal(2)))
unitstate UNIT_STATE_MAX_MANA=JassExprFunctionCall(ConvertUnitState, JassExprlist(JassExprIntVal(3)))
aidifficulty AI_DIFFICULTY_NEWBIE=JassExprFunctionCall(ConvertAIDifficulty, JassExprlist(JassExprIntVal(0)))
aidifficulty AI_DIFFICULTY_NORMAL=JassExprFunctionCall(ConvertAIDifficulty, JassExprlist(JassExprIntVal(1)))
aidifficulty AI_DIFFICULTY_INSANE=JassExprFunctionCall(ConvertAIDifficulty, JassExprlist(JassExprIntVal(2)))
playerscore PLAYER_SCORE_UNITS_TRAINED=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(0)))
playerscore PLAYER_SCORE_UNITS_KILLED=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(1)))
playerscore PLAYER_SCORE_STRUCT_BUILT=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(2)))
playerscore PLAYER_SCORE_STRUCT_RAZED=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(3)))
playerscore PLAYER_SCORE_TECH_PERCENT=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(4)))
playerscore PLAYER_SCORE_FOOD_MAXPROD=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(5)))
playerscore PLAYER_SCORE_FOOD_MAXUSED=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(6)))
playerscore PLAYER_SCORE_HEROES_KILLED=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(7)))
playerscore PLAYER_SCORE_ITEMS_GAINED=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(8)))
playerscore PLAYER_SCORE_MERCS_HIRED=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(9)))
playerscore PLAYER_SCORE_GOLD_MINED_TOTAL=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(10)))
playerscore PLAYER_SCORE_GOLD_MINED_UPKEEP=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(11)))
playerscore PLAYER_SCORE_GOLD_LOST_UPKEEP=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(12)))
playerscore PLAYER_SCORE_GOLD_LOST_TAX=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(13)))
playerscore PLAYER_SCORE_GOLD_GIVEN=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(14)))
playerscore PLAYER_SCORE_GOLD_RECEIVED=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(15)))
playerscore PLAYER_SCORE_LUMBER_TOTAL=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(16)))
playerscore PLAYER_SCORE_LUMBER_LOST_UPKEEP=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(17)))
playerscore PLAYER_SCORE_LUMBER_LOST_TAX=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(18)))
playerscore PLAYER_SCORE_LUMBER_GIVEN=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(19)))
playerscore PLAYER_SCORE_LUMBER_RECEIVED=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(20)))
playerscore PLAYER_SCORE_UNIT_TOTAL=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(21)))
playerscore PLAYER_SCORE_HERO_TOTAL=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(22)))
playerscore PLAYER_SCORE_RESOURCE_TOTAL=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(23)))
playerscore PLAYER_SCORE_TOTAL=JassExprFunctionCall(ConvertPlayerScore, JassExprlist(JassExprIntVal(24)))
gameevent EVENT_GAME_VICTORY=JassExprFunctionCall(ConvertGameEvent, JassExprlist(JassExprIntVal(0)))
gameevent EVENT_GAME_END_LEVEL=JassExprFunctionCall(ConvertGameEvent, JassExprlist(JassExprIntVal(1)))
gameevent EVENT_GAME_VARIABLE_LIMIT=JassExprFunctionCall(ConvertGameEvent, JassExprlist(JassExprIntVal(2)))
gameevent EVENT_GAME_STATE_LIMIT=JassExprFunctionCall(ConvertGameEvent, JassExprlist(JassExprIntVal(3)))
gameevent EVENT_GAME_TIMER_EXPIRED=JassExprFunctionCall(ConvertGameEvent, JassExprlist(JassExprIntVal(4)))
gameevent EVENT_GAME_ENTER_REGION=JassExprFunctionCall(ConvertGameEvent, JassExprlist(JassExprIntVal(5)))
gameevent EVENT_GAME_LEAVE_REGION=JassExprFunctionCall(ConvertGameEvent, JassExprlist(JassExprIntVal(6)))
gameevent EVENT_GAME_TRACKABLE_HIT=JassExprFunctionCall(ConvertGameEvent, JassExprlist(JassExprIntVal(7)))
gameevent EVENT_GAME_TRACKABLE_TRACK=JassExprFunctionCall(ConvertGameEvent, JassExprlist(JassExprIntVal(8)))
gameevent EVENT_GAME_SHOW_SKILL=JassExprFunctionCall(ConvertGameEvent, JassExprlist(JassExprIntVal(9)))
gameevent EVENT_GAME_BUILD_SUBMENU=JassExprFunctionCall(ConvertGameEvent, JassExprlist(JassExprIntVal(10)))
playerevent EVENT_PLAYER_STATE_LIMIT=JassExprFunctionCall(ConvertPlayerEvent, JassExprlist(JassExprIntVal(11)))
playerevent EVENT_PLAYER_ALLIANCE_CHANGED=JassExprFunctionCall(ConvertPlayerEvent, JassExprlist(JassExprIntVal(12)))
playerevent EVENT_PLAYER_DEFEAT=JassExprFunctionCall(ConvertPlayerEvent, JassExprlist(JassExprIntVal(13)))
playerevent EVENT_PLAYER_VICTORY=JassExprFunctionCall(ConvertPlayerEvent, JassExprlist(JassExprIntVal(14)))
playerevent EVENT_PLAYER_LEAVE=JassExprFunctionCall(ConvertPlayerEvent, JassExprlist(JassExprIntVal(15)))
playerevent EVENT_PLAYER_CHAT=JassExprFunctionCall(ConvertPlayerEvent, JassExprlist(JassExprIntVal(16)))
playerevent EVENT_PLAYER_END_CINEMATIC=JassExprFunctionCall(ConvertPlayerEvent, JassExprlist(JassExprIntVal(17)))
playerunitevent EVENT_PLAYER_UNIT_ATTACKED=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(18)))
playerunitevent EVENT_PLAYER_UNIT_RESCUED=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(19)))
playerunitevent EVENT_PLAYER_UNIT_DEATH=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(20)))
playerunitevent EVENT_PLAYER_UNIT_DECAY=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(21)))
playerunitevent EVENT_PLAYER_UNIT_DETECTED=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(22)))
playerunitevent EVENT_PLAYER_UNIT_HIDDEN=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(23)))
playerunitevent EVENT_PLAYER_UNIT_SELECTED=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(24)))
playerunitevent EVENT_PLAYER_UNIT_DESELECTED=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(25)))
playerunitevent EVENT_PLAYER_UNIT_CONSTRUCT_START=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(26)))
playerunitevent EVENT_PLAYER_UNIT_CONSTRUCT_CANCEL=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(27)))
playerunitevent EVENT_PLAYER_UNIT_CONSTRUCT_FINISH=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(28)))
playerunitevent EVENT_PLAYER_UNIT_UPGRADE_START=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(29)))
playerunitevent EVENT_PLAYER_UNIT_UPGRADE_CANCEL=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(30)))
playerunitevent EVENT_PLAYER_UNIT_UPGRADE_FINISH=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(31)))
playerunitevent EVENT_PLAYER_UNIT_TRAIN_START=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(32)))
playerunitevent EVENT_PLAYER_UNIT_TRAIN_CANCEL=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(33)))
playerunitevent EVENT_PLAYER_UNIT_TRAIN_FINISH=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(34)))
playerunitevent EVENT_PLAYER_UNIT_RESEARCH_START=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(35)))
playerunitevent EVENT_PLAYER_UNIT_RESEARCH_CANCEL=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(36)))
playerunitevent EVENT_PLAYER_UNIT_RESEARCH_FINISH=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(37)))
playerunitevent EVENT_PLAYER_UNIT_ISSUED_ORDER=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(38)))
playerunitevent EVENT_PLAYER_UNIT_ISSUED_POINT_ORDER=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(39)))
playerunitevent EVENT_PLAYER_UNIT_ISSUED_TARGET_ORDER=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(40)))
playerunitevent EVENT_PLAYER_UNIT_ISSUED_UNIT_ORDER=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(40)))
playerunitevent EVENT_PLAYER_HERO_LEVEL=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(41)))
playerunitevent EVENT_PLAYER_HERO_SKILL=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(42)))
playerunitevent EVENT_PLAYER_HERO_REVIVABLE=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(43)))
playerunitevent EVENT_PLAYER_HERO_REVIVE_START=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(44)))
playerunitevent EVENT_PLAYER_HERO_REVIVE_CANCEL=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(45)))
playerunitevent EVENT_PLAYER_HERO_REVIVE_FINISH=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(46)))
playerunitevent EVENT_PLAYER_UNIT_SUMMON=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(47)))
playerunitevent EVENT_PLAYER_UNIT_DROP_ITEM=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(48)))
playerunitevent EVENT_PLAYER_UNIT_PICKUP_ITEM=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(49)))
playerunitevent EVENT_PLAYER_UNIT_USE_ITEM=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(50)))
playerunitevent EVENT_PLAYER_UNIT_LOADED=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(51)))
unitevent EVENT_UNIT_DAMAGED=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(52)))
unitevent EVENT_UNIT_DEATH=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(53)))
unitevent EVENT_UNIT_DECAY=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(54)))
unitevent EVENT_UNIT_DETECTED=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(55)))
unitevent EVENT_UNIT_HIDDEN=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(56)))
unitevent EVENT_UNIT_SELECTED=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(57)))
unitevent EVENT_UNIT_DESELECTED=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(58)))
unitevent EVENT_UNIT_STATE_LIMIT=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(59)))
unitevent EVENT_UNIT_ACQUIRED_TARGET=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(60)))
unitevent EVENT_UNIT_TARGET_IN_RANGE=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(61)))
unitevent EVENT_UNIT_ATTACKED=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(62)))
unitevent EVENT_UNIT_RESCUED=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(63)))
unitevent EVENT_UNIT_CONSTRUCT_CANCEL=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(64)))
unitevent EVENT_UNIT_CONSTRUCT_FINISH=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(65)))
unitevent EVENT_UNIT_UPGRADE_START=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(66)))
unitevent EVENT_UNIT_UPGRADE_CANCEL=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(67)))
unitevent EVENT_UNIT_UPGRADE_FINISH=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(68)))
unitevent EVENT_UNIT_TRAIN_START=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(69)))
unitevent EVENT_UNIT_TRAIN_CANCEL=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(70)))
unitevent EVENT_UNIT_TRAIN_FINISH=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(71)))
unitevent EVENT_UNIT_RESEARCH_START=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(72)))
unitevent EVENT_UNIT_RESEARCH_CANCEL=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(73)))
unitevent EVENT_UNIT_RESEARCH_FINISH=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(74)))
unitevent EVENT_UNIT_ISSUED_ORDER=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(75)))
unitevent EVENT_UNIT_ISSUED_POINT_ORDER=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(76)))
unitevent EVENT_UNIT_ISSUED_TARGET_ORDER=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(77)))
unitevent EVENT_UNIT_HERO_LEVEL=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(78)))
unitevent EVENT_UNIT_HERO_SKILL=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(79)))
unitevent EVENT_UNIT_HERO_REVIVABLE=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(80)))
unitevent EVENT_UNIT_HERO_REVIVE_START=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(81)))
unitevent EVENT_UNIT_HERO_REVIVE_CANCEL=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(82)))
unitevent EVENT_UNIT_HERO_REVIVE_FINISH=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(83)))
unitevent EVENT_UNIT_SUMMON=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(84)))
unitevent EVENT_UNIT_DROP_ITEM=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(85)))
unitevent EVENT_UNIT_PICKUP_ITEM=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(86)))
unitevent EVENT_UNIT_USE_ITEM=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(87)))
unitevent EVENT_UNIT_LOADED=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(88)))
widgetevent EVENT_WIDGET_DEATH=JassExprFunctionCall(ConvertWidgetEvent, JassExprlist(JassExprIntVal(89)))
dialogevent EVENT_DIALOG_BUTTON_CLICK=JassExprFunctionCall(ConvertDialogEvent, JassExprlist(JassExprIntVal(90)))
dialogevent EVENT_DIALOG_CLICK=JassExprFunctionCall(ConvertDialogEvent, JassExprlist(JassExprIntVal(91)))
gameevent EVENT_GAME_LOADED=JassExprFunctionCall(ConvertGameEvent, JassExprlist(JassExprIntVal(256)))
gameevent EVENT_GAME_TOURNAMENT_FINISH_SOON=JassExprFunctionCall(ConvertGameEvent, JassExprlist(JassExprIntVal(257)))
gameevent EVENT_GAME_TOURNAMENT_FINISH_NOW=JassExprFunctionCall(ConvertGameEvent, JassExprlist(JassExprIntVal(258)))
gameevent EVENT_GAME_SAVE=JassExprFunctionCall(ConvertGameEvent, JassExprlist(JassExprIntVal(259)))
playerevent EVENT_PLAYER_ARROW_LEFT_DOWN=JassExprFunctionCall(ConvertPlayerEvent, JassExprlist(JassExprIntVal(261)))
playerevent EVENT_PLAYER_ARROW_LEFT_UP=JassExprFunctionCall(ConvertPlayerEvent, JassExprlist(JassExprIntVal(262)))
playerevent EVENT_PLAYER_ARROW_RIGHT_DOWN=JassExprFunctionCall(ConvertPlayerEvent, JassExprlist(JassExprIntVal(263)))
playerevent EVENT_PLAYER_ARROW_RIGHT_UP=JassExprFunctionCall(ConvertPlayerEvent, JassExprlist(JassExprIntVal(264)))
playerevent EVENT_PLAYER_ARROW_DOWN_DOWN=JassExprFunctionCall(ConvertPlayerEvent, JassExprlist(JassExprIntVal(265)))
playerevent EVENT_PLAYER_ARROW_DOWN_UP=JassExprFunctionCall(ConvertPlayerEvent, JassExprlist(JassExprIntVal(266)))
playerevent EVENT_PLAYER_ARROW_UP_DOWN=JassExprFunctionCall(ConvertPlayerEvent, JassExprlist(JassExprIntVal(267)))
playerevent EVENT_PLAYER_ARROW_UP_UP=JassExprFunctionCall(ConvertPlayerEvent, JassExprlist(JassExprIntVal(268)))
playerunitevent EVENT_PLAYER_UNIT_SELL=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(269)))
playerunitevent EVENT_PLAYER_UNIT_CHANGE_OWNER=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(270)))
playerunitevent EVENT_PLAYER_UNIT_SELL_ITEM=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(271)))
playerunitevent EVENT_PLAYER_UNIT_SPELL_CHANNEL=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(272)))
playerunitevent EVENT_PLAYER_UNIT_SPELL_CAST=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(273)))
playerunitevent EVENT_PLAYER_UNIT_SPELL_EFFECT=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(274)))
playerunitevent EVENT_PLAYER_UNIT_SPELL_FINISH=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(275)))
playerunitevent EVENT_PLAYER_UNIT_SPELL_ENDCAST=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(276)))
playerunitevent EVENT_PLAYER_UNIT_PAWN_ITEM=JassExprFunctionCall(ConvertPlayerUnitEvent, JassExprlist(JassExprIntVal(277)))
unitevent EVENT_UNIT_SELL=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(286)))
unitevent EVENT_UNIT_CHANGE_OWNER=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(287)))
unitevent EVENT_UNIT_SELL_ITEM=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(288)))
unitevent EVENT_UNIT_SPELL_CHANNEL=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(289)))
unitevent EVENT_UNIT_SPELL_CAST=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(290)))
unitevent EVENT_UNIT_SPELL_EFFECT=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(291)))
unitevent EVENT_UNIT_SPELL_FINISH=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(292)))
unitevent EVENT_UNIT_SPELL_ENDCAST=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(293)))
unitevent EVENT_UNIT_PAWN_ITEM=JassExprFunctionCall(ConvertUnitEvent, JassExprlist(JassExprIntVal(294)))
limitop LESS_THAN=JassExprFunctionCall(ConvertLimitOp, JassExprlist(JassExprIntVal(0)))
limitop LESS_THAN_OR_EQUAL=JassExprFunctionCall(ConvertLimitOp, JassExprlist(JassExprIntVal(1)))
limitop EQUAL=JassExprFunctionCall(ConvertLimitOp, JassExprlist(JassExprIntVal(2)))
limitop GREATER_THAN_OR_EQUAL=JassExprFunctionCall(ConvertLimitOp, JassExprlist(JassExprIntVal(3)))
limitop GREATER_THAN=JassExprFunctionCall(ConvertLimitOp, JassExprlist(JassExprIntVal(4)))
limitop NOT_EQUAL=JassExprFunctionCall(ConvertLimitOp, JassExprlist(JassExprIntVal(5)))
unittype UNIT_TYPE_HERO=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(0)))
unittype UNIT_TYPE_DEAD=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(1)))
unittype UNIT_TYPE_STRUCTURE=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(2)))
unittype UNIT_TYPE_FLYING=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(3)))
unittype UNIT_TYPE_GROUND=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(4)))
unittype UNIT_TYPE_ATTACKS_FLYING=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(5)))
unittype UNIT_TYPE_ATTACKS_GROUND=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(6)))
unittype UNIT_TYPE_MELEE_ATTACKER=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(7)))
unittype UNIT_TYPE_RANGED_ATTACKER=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(8)))
unittype UNIT_TYPE_GIANT=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(9)))
unittype UNIT_TYPE_SUMMONED=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(10)))
unittype UNIT_TYPE_STUNNED=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(11)))
unittype UNIT_TYPE_PLAGUED=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(12)))
unittype UNIT_TYPE_SNARED=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(13)))
unittype UNIT_TYPE_UNDEAD=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(14)))
unittype UNIT_TYPE_MECHANICAL=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(15)))
unittype UNIT_TYPE_PEON=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(16)))
unittype UNIT_TYPE_SAPPER=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(17)))
unittype UNIT_TYPE_TOWNHALL=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(18)))
unittype UNIT_TYPE_ANCIENT=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(19)))
unittype UNIT_TYPE_TAUREN=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(20)))
unittype UNIT_TYPE_POISONED=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(21)))
unittype UNIT_TYPE_POLYMORPHED=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(22)))
unittype UNIT_TYPE_SLEEPING=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(23)))
unittype UNIT_TYPE_RESISTANT=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(24)))
unittype UNIT_TYPE_ETHEREAL=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(25)))
unittype UNIT_TYPE_MAGIC_IMMUNE=JassExprFunctionCall(ConvertUnitType, JassExprlist(JassExprIntVal(26)))
itemtype ITEM_TYPE_PERMANENT=JassExprFunctionCall(ConvertItemType, JassExprlist(JassExprIntVal(0)))
itemtype ITEM_TYPE_CHARGED=JassExprFunctionCall(ConvertItemType, JassExprlist(JassExprIntVal(1)))
itemtype ITEM_TYPE_POWERUP=JassExprFunctionCall(ConvertItemType, JassExprlist(JassExprIntVal(2)))
itemtype ITEM_TYPE_ARTIFACT=JassExprFunctionCall(ConvertItemType, JassExprlist(JassExprIntVal(3)))
itemtype ITEM_TYPE_PURCHASABLE=JassExprFunctionCall(ConvertItemType, JassExprlist(JassExprIntVal(4)))
itemtype ITEM_TYPE_CAMPAIGN=JassExprFunctionCall(ConvertItemType, JassExprlist(JassExprIntVal(5)))
itemtype ITEM_TYPE_MISCELLANEOUS=JassExprFunctionCall(ConvertItemType, JassExprlist(JassExprIntVal(6)))
itemtype ITEM_TYPE_UNKNOWN=JassExprFunctionCall(ConvertItemType, JassExprlist(JassExprIntVal(7)))
itemtype ITEM_TYPE_ANY=JassExprFunctionCall(ConvertItemType, JassExprlist(JassExprIntVal(8)))
itemtype ITEM_TYPE_TOME=JassExprFunctionCall(ConvertItemType, JassExprlist(JassExprIntVal(2)))
camerafield CAMERA_FIELD_TARGET_DISTANCE=JassExprFunctionCall(ConvertCameraField, JassExprlist(JassExprIntVal(0)))
camerafield CAMERA_FIELD_FARZ=JassExprFunctionCall(ConvertCameraField, JassExprlist(JassExprIntVal(1)))
camerafield CAMERA_FIELD_ANGLE_OF_ATTACK=JassExprFunctionCall(ConvertCameraField, JassExprlist(JassExprIntVal(2)))
camerafield CAMERA_FIELD_FIELD_OF_VIEW=JassExprFunctionCall(ConvertCameraField, JassExprlist(JassExprIntVal(3)))
camerafield CAMERA_FIELD_ROLL=JassExprFunctionCall(ConvertCameraField, JassExprlist(JassExprIntVal(4)))
camerafield CAMERA_FIELD_ROTATION=JassExprFunctionCall(ConvertCameraField, JassExprlist(JassExprIntVal(5)))
camerafield CAMERA_FIELD_ZOFFSET=JassExprFunctionCall(ConvertCameraField, JassExprlist(JassExprIntVal(6)))
blendmode BLEND_MODE_NONE=JassExprFunctionCall(ConvertBlendMode, JassExprlist(JassExprIntVal(0)))
blendmode BLEND_MODE_DONT_CARE=JassExprFunctionCall(ConvertBlendMode, JassExprlist(JassExprIntVal(0)))
blendmode BLEND_MODE_KEYALPHA=JassExprFunctionCall(ConvertBlendMode, JassExprlist(JassExprIntVal(1)))
blendmode BLEND_MODE_BLEND=JassExprFunctionCall(ConvertBlendMode, JassExprlist(JassExprIntVal(2)))
blendmode BLEND_MODE_ADDITIVE=JassExprFunctionCall(ConvertBlendMode, JassExprlist(JassExprIntVal(3)))
blendmode BLEND_MODE_MODULATE=JassExprFunctionCall(ConvertBlendMode, JassExprlist(JassExprIntVal(4)))
blendmode BLEND_MODE_MODULATE_2X=JassExprFunctionCall(ConvertBlendMode, JassExprlist(JassExprIntVal(5)))
raritycontrol RARITY_FREQUENT=JassExprFunctionCall(ConvertRarityControl, JassExprlist(JassExprIntVal(0)))
raritycontrol RARITY_RARE=JassExprFunctionCall(ConvertRarityControl, JassExprlist(JassExprIntVal(1)))
texmapflags TEXMAP_FLAG_NONE=JassExprFunctionCall(ConvertTexMapFlags, JassExprlist(JassExprIntVal(0)))
texmapflags TEXMAP_FLAG_WRAP_U=JassExprFunctionCall(ConvertTexMapFlags, JassExprlist(JassExprIntVal(1)))
texmapflags TEXMAP_FLAG_WRAP_V=JassExprFunctionCall(ConvertTexMapFlags, JassExprlist(JassExprIntVal(2)))
texmapflags TEXMAP_FLAG_WRAP_UV=JassExprFunctionCall(ConvertTexMapFlags, JassExprlist(JassExprIntVal(3)))
fogstate FOG_OF_WAR_MASKED=JassExprFunctionCall(ConvertFogState, JassExprlist(JassExprIntVal(1)))
fogstate FOG_OF_WAR_FOGGED=JassExprFunctionCall(ConvertFogState, JassExprlist(JassExprIntVal(2)))
fogstate FOG_OF_WAR_VISIBLE=JassExprFunctionCall(ConvertFogState, JassExprlist(JassExprIntVal(4)))
integer CAMERA_MARGIN_LEFT=JassExprIntVal(0)
integer CAMERA_MARGIN_RIGHT=JassExprIntVal(1)
integer CAMERA_MARGIN_TOP=JassExprIntVal(2)
integer CAMERA_MARGIN_BOTTOM=JassExprIntVal(3)
effecttype EFFECT_TYPE_EFFECT=JassExprFunctionCall(ConvertEffectType, JassExprlist(JassExprIntVal(0)))
effecttype EFFECT_TYPE_TARGET=JassExprFunctionCall(ConvertEffectType, JassExprlist(JassExprIntVal(1)))
effecttype EFFECT_TYPE_CASTER=JassExprFunctionCall(ConvertEffectType, JassExprlist(JassExprIntVal(2)))
effecttype EFFECT_TYPE_SPECIAL=JassExprFunctionCall(ConvertEffectType, JassExprlist(JassExprIntVal(3)))
effecttype EFFECT_TYPE_AREA_EFFECT=JassExprFunctionCall(ConvertEffectType, JassExprlist(JassExprIntVal(4)))
effecttype EFFECT_TYPE_MISSILE=JassExprFunctionCall(ConvertEffectType, JassExprlist(JassExprIntVal(5)))
effecttype EFFECT_TYPE_LIGHTNING=JassExprFunctionCall(ConvertEffectType, JassExprlist(JassExprIntVal(6)))
soundtype SOUND_TYPE_EFFECT=JassExprFunctionCall(ConvertSoundType, JassExprlist(JassExprIntVal(0)))
soundtype SOUND_TYPE_EFFECT_LOOPED=JassExprFunctionCall(ConvertSoundType, JassExprlist(JassExprIntVal(1)))
endglobals
native ConvertRace takes integer i returns race
native ConvertAllianceType takes integer i returns alliancetype
native ConvertRacePref takes integer i returns racepreference
native ConvertIGameState takes integer i returns igamestate
native ConvertFGameState takes integer i returns fgamestate
native ConvertPlayerState takes integer i returns playerstate
native ConvertPlayerScore takes integer i returns playerscore
native ConvertPlayerGameResult takes integer i returns playergameresult
native ConvertUnitState takes integer i returns unitstate
native ConvertAIDifficulty takes integer i returns aidifficulty
native ConvertGameEvent takes integer i returns gameevent
native ConvertPlayerEvent takes integer i returns playerevent
native ConvertPlayerUnitEvent takes integer i returns playerunitevent
native ConvertWidgetEvent takes integer i returns widgetevent
native ConvertDialogEvent takes integer i returns dialogevent
native ConvertUnitEvent takes integer i returns unitevent
native ConvertLimitOp takes integer i returns limitop
native ConvertUnitType takes integer i returns unittype
native ConvertGameSpeed takes integer i returns gamespeed
native ConvertPlacement takes integer i returns placement
native ConvertStartLocPrio takes integer i returns startlocprio
native ConvertGameDifficulty takes integer i returns gamedifficulty
native ConvertGameType takes integer i returns gametype
native ConvertMapFlag takes integer i returns mapflag
native ConvertMapVisibility takes integer i returns mapvisibility
native ConvertMapSetting takes integer i returns mapsetting
native ConvertMapDensity takes integer i returns mapdensity
native ConvertMapControl takes integer i returns mapcontrol
native ConvertPlayerColor takes integer i returns playercolor
native ConvertPlayerSlotState takes integer i returns playerslotstate
native ConvertVolumeGroup takes integer i returns volumegroup
native ConvertCameraField takes integer i returns camerafield
native ConvertBlendMode takes integer i returns blendmode
native ConvertRarityControl takes integer i returns raritycontrol
native ConvertTexMapFlags takes integer i returns texmapflags
native ConvertFogState takes integer i returns fogstate
native ConvertEffectType takes integer i returns effecttype
native ConvertVersion takes integer i returns version
native ConvertItemType takes integer i returns itemtype
native ConvertAttackType takes integer i returns attacktype
native ConvertDamageType takes integer i returns damagetype
native ConvertWeaponType takes integer i returns weapontype
native ConvertSoundType takes integer i returns soundtype
native ConvertPathingType takes integer i returns pathingtype
native OrderId takes string orderIdString returns integer
native OrderId2String takes integer orderId returns string
native UnitId takes string unitIdString returns integer
native UnitId2String takes integer unitId returns string
native AbilityId takes string abilityIdString returns integer
native AbilityId2String takes integer abilityId returns string
native GetObjectName takes integer objectId returns string
native Deg2Rad takes real degrees returns real
native Rad2Deg takes real radians returns real
native Sin takes real radians returns real
native Cos takes real radians returns real
native Tan takes real radians returns real
native Asin takes real y returns real
native Acos takes real x returns real
native Atan takes real x returns real
native Atan2 takes real y,real x returns real
native SquareRoot takes real x returns real
native Pow takes real x,real power returns real
native I2R takes integer i returns real
native R2I takes real r returns integer
native I2S takes integer i returns string
native R2S takes real r returns string
native R2SW takes real r,integer width,integer precision returns string
native S2I takes string s returns integer
native S2R takes string s returns real
native GetHandleId takes handle h returns integer
native SubString takes string source,integer start,integer end returns string
native StringLength takes string s returns integer
native StringCase takes string source,boolean upper returns string
native StringHash takes string s returns integer
native GetLocalizedString takes string source returns string
native GetLocalizedHotkey takes string source returns integer
native SetMapName takes string name returns nothing
native SetMapDescription takes string description returns nothing
native SetTeams takes integer teamcount returns nothing
native SetPlayers takes integer playercount returns nothing
native DefineStartLocation takes integer whichStartLoc,real x,real y returns nothing
native DefineStartLocationLoc takes integer whichStartLoc,location whichLocation returns nothing
native SetStartLocPrioCount takes integer whichStartLoc,integer prioSlotCount returns nothing
native SetStartLocPrio takes integer whichStartLoc,integer prioSlotIndex,integer otherStartLocIndex,startlocprio priority returns nothing
native GetStartLocPrioSlot takes integer whichStartLoc,integer prioSlotIndex returns integer
native GetStartLocPrio takes integer whichStartLoc,integer prioSlotIndex returns startlocprio
native SetGameTypeSupported takes gametype whichGameType,boolean value returns nothing
native SetMapFlag takes mapflag whichMapFlag,boolean value returns nothing
native SetGamePlacement takes placement whichPlacementType returns nothing
native SetGameSpeed takes gamespeed whichspeed returns nothing
native SetGameDifficulty takes gamedifficulty whichdifficulty returns nothing
native SetResourceDensity takes mapdensity whichdensity returns nothing
native SetCreatureDensity takes mapdensity whichdensity returns nothing
native GetTeams takes nothing returns integer
native GetPlayers takes nothing returns integer
native IsGameTypeSupported takes gametype whichGameType returns boolean
native GetGameTypeSelected takes nothing returns gametype
native IsMapFlagSet takes mapflag whichMapFlag returns boolean
native GetGamePlacement takes nothing returns placement
native GetGameSpeed takes nothing returns gamespeed
native GetGameDifficulty takes nothing returns gamedifficulty
native GetResourceDensity takes nothing returns mapdensity
native GetCreatureDensity takes nothing returns mapdensity
native GetStartLocationX takes integer whichStartLocation returns real
native GetStartLocationY takes integer whichStartLocation returns real
native GetStartLocationLoc takes integer whichStartLocation returns location
native SetPlayerTeam takes player whichPlayer,integer whichTeam returns nothing
native SetPlayerStartLocation takes player whichPlayer,integer startLocIndex returns nothing
native ForcePlayerStartLocation takes player whichPlayer,integer startLocIndex returns nothing
native SetPlayerColor takes player whichPlayer,playercolor color returns nothing
native SetPlayerAlliance takes player sourcePlayer,player otherPlayer,alliancetype whichAllianceSetting,boolean value returns nothing
native SetPlayerTaxRate takes player sourcePlayer,player otherPlayer,playerstate whichResource,integer rate returns nothing
native SetPlayerRacePreference takes player whichPlayer,racepreference whichRacePreference returns nothing
native SetPlayerRaceSelectable takes player whichPlayer,boolean value returns nothing
native SetPlayerController takes player whichPlayer,mapcontrol controlType returns nothing
native SetPlayerName takes player whichPlayer,string name returns nothing
native SetPlayerOnScoreScreen takes player whichPlayer,boolean flag returns nothing
native GetPlayerTeam takes player whichPlayer returns integer
native GetPlayerStartLocation takes player whichPlayer returns integer
native GetPlayerColor takes player whichPlayer returns playercolor
native GetPlayerSelectable takes player whichPlayer returns boolean
native GetPlayerController takes player whichPlayer returns mapcontrol
native GetPlayerSlotState takes player whichPlayer returns playerslotstate
native GetPlayerTaxRate takes player sourcePlayer,player otherPlayer,playerstate whichResource returns integer
native IsPlayerRacePrefSet takes player whichPlayer,racepreference pref returns boolean
native GetPlayerName takes player whichPlayer returns string
native CreateTimer takes nothing returns timer
native DestroyTimer takes timer whichTimer returns nothing
native TimerStart takes timer whichTimer,real timeout,boolean periodic,code handlerFunc returns nothing
native TimerGetElapsed takes timer whichTimer returns real
native TimerGetRemaining takes timer whichTimer returns real
native TimerGetTimeout takes timer whichTimer returns real
native PauseTimer takes timer whichTimer returns nothing
native ResumeTimer takes timer whichTimer returns nothing
native GetExpiredTimer takes nothing returns timer
native CreateGroup takes nothing returns group
native DestroyGroup takes group whichGroup returns nothing
native GroupAddUnit takes group whichGroup,unit whichUnit returns nothing
native GroupRemoveUnit takes group whichGroup,unit whichUnit returns nothing
native GroupClear takes group whichGroup returns nothing
native GroupEnumUnitsOfType takes group whichGroup,string unitname,boolexpr filter returns nothing
native GroupEnumUnitsOfPlayer takes group whichGroup,player whichPlayer,boolexpr filter returns nothing
native GroupEnumUnitsOfTypeCounted takes group whichGroup,string unitname,boolexpr filter,integer countLimit returns nothing
native GroupEnumUnitsInRect takes group whichGroup,rect r,boolexpr filter returns nothing
native GroupEnumUnitsInRectCounted takes group whichGroup,rect r,boolexpr filter,integer countLimit returns nothing
native GroupEnumUnitsInRange takes group whichGroup,real x,real y,real radius,boolexpr filter returns nothing
native GroupEnumUnitsInRangeOfLoc takes group whichGroup,location whichLocation,real radius,boolexpr filter returns nothing
native GroupEnumUnitsInRangeCounted takes group whichGroup,real x,real y,real radius,boolexpr filter,integer countLimit returns nothing
native GroupEnumUnitsInRangeOfLocCounted takes group whichGroup,location whichLocation,real radius,boolexpr filter,integer countLimit returns nothing
native GroupEnumUnitsSelected takes group whichGroup,player whichPlayer,boolexpr filter returns nothing
native GroupImmediateOrder takes group whichGroup,string order returns boolean
native GroupImmediateOrderById takes group whichGroup,integer order returns boolean
native GroupPointOrder takes group whichGroup,string order,real x,real y returns boolean
native GroupPointOrderLoc takes group whichGroup,string order,location whichLocation returns boolean
native GroupPointOrderById takes group whichGroup,integer order,real x,real y returns boolean
native GroupPointOrderByIdLoc takes group whichGroup,integer order,location whichLocation returns boolean
native GroupTargetOrder takes group whichGroup,string order,widget targetWidget returns boolean
native GroupTargetOrderById takes group whichGroup,integer order,widget targetWidget returns boolean
native ForGroup takes group whichGroup,code callback returns nothing
native FirstOfGroup takes group whichGroup returns unit
native CreateForce takes nothing returns force
native DestroyForce takes force whichForce returns nothing
native ForceAddPlayer takes force whichForce,player whichPlayer returns nothing
native ForceRemovePlayer takes force whichForce,player whichPlayer returns nothing
native ForceClear takes force whichForce returns nothing
native ForceEnumPlayers takes force whichForce,boolexpr filter returns nothing
native ForceEnumPlayersCounted takes force whichForce,boolexpr filter,integer countLimit returns nothing
native ForceEnumAllies takes force whichForce,player whichPlayer,boolexpr filter returns nothing
native ForceEnumEnemies takes force whichForce,player whichPlayer,boolexpr filter returns nothing
native ForForce takes force whichForce,code callback returns nothing
native Rect takes real minx,real miny,real maxx,real maxy returns rect
native RectFromLoc takes location min,location max returns rect
native RemoveRect takes rect whichRect returns nothing
native SetRect takes rect whichRect,real minx,real miny,real maxx,real maxy returns nothing
native SetRectFromLoc takes rect whichRect,location min,location max returns nothing
native MoveRectTo takes rect whichRect,real newCenterX,real newCenterY returns nothing
native MoveRectToLoc takes rect whichRect,location newCenterLoc returns nothing
native GetRectCenterX takes rect whichRect returns real
native GetRectCenterY takes rect whichRect returns real
native GetRectMinX takes rect whichRect returns real
native GetRectMinY takes rect whichRect returns real
native GetRectMaxX takes rect whichRect returns real
native GetRectMaxY takes rect whichRect returns real
native CreateRegion takes nothing returns region
native RemoveRegion takes region whichRegion returns nothing
native RegionAddRect takes region whichRegion,rect r returns nothing
native RegionClearRect takes region whichRegion,rect r returns nothing
native RegionAddCell takes region whichRegion,real x,real y returns nothing
native RegionAddCellAtLoc takes region whichRegion,location whichLocation returns nothing
native RegionClearCell takes region whichRegion,real x,real y returns nothing
native RegionClearCellAtLoc takes region whichRegion,location whichLocation returns nothing
native Location takes real x,real y returns location
native RemoveLocation takes location whichLocation returns nothing
native MoveLocation takes location whichLocation,real newX,real newY returns nothing
native GetLocationX takes location whichLocation returns real
native GetLocationY takes location whichLocation returns real
native GetLocationZ takes location whichLocation returns real
native IsUnitInRegion takes region whichRegion,unit whichUnit returns boolean
native IsPointInRegion takes region whichRegion,real x,real y returns boolean
native IsLocationInRegion takes region whichRegion,location whichLocation returns boolean
native GetWorldBounds takes nothing returns rect
native CreateTrigger takes nothing returns trigger
native DestroyTrigger takes trigger whichTrigger returns nothing
native ResetTrigger takes trigger whichTrigger returns nothing
native EnableTrigger takes trigger whichTrigger returns nothing
native DisableTrigger takes trigger whichTrigger returns nothing
native IsTriggerEnabled takes trigger whichTrigger returns boolean
native TriggerWaitOnSleeps takes trigger whichTrigger,boolean flag returns nothing
native IsTriggerWaitOnSleeps takes trigger whichTrigger returns boolean
native GetFilterUnit takes nothing returns unit
native GetEnumUnit takes nothing returns unit
native GetFilterDestructable takes nothing returns destructable
native GetEnumDestructable takes nothing returns destructable
native GetFilterItem takes nothing returns item
native GetEnumItem takes nothing returns item
native GetFilterPlayer takes nothing returns player
native GetEnumPlayer takes nothing returns player
native GetTriggeringTrigger takes nothing returns trigger
native GetTriggerEventId takes nothing returns eventid
native GetTriggerEvalCount takes trigger whichTrigger returns integer
native GetTriggerExecCount takes trigger whichTrigger returns integer
native ExecuteFunc takes string funcName returns nothing
native And takes boolexpr operandA,boolexpr operandB returns boolexpr
native Or takes boolexpr operandA,boolexpr operandB returns boolexpr
native Not takes boolexpr operand returns boolexpr
native Condition takes code func returns conditionfunc
native DestroyCondition takes conditionfunc c returns nothing
native Filter takes code func returns filterfunc
native DestroyFilter takes filterfunc f returns nothing
native DestroyBoolExpr takes boolexpr e returns nothing
native TriggerRegisterVariableEvent takes trigger whichTrigger,string varName,limitop opcode,real limitval returns event
native TriggerRegisterTimerEvent takes trigger whichTrigger,real timeout,boolean periodic returns event
native TriggerRegisterTimerExpireEvent takes trigger whichTrigger,timer t returns event
native TriggerRegisterGameStateEvent takes trigger whichTrigger,gamestate whichState,limitop opcode,real limitval returns event
native TriggerRegisterDialogEvent takes trigger whichTrigger,dialog whichDialog returns event
native TriggerRegisterDialogButtonEvent takes trigger whichTrigger,button whichButton returns event
native GetEventGameState takes nothing returns gamestate
native TriggerRegisterGameEvent takes trigger whichTrigger,gameevent whichGameEvent returns event
native GetWinningPlayer takes nothing returns player
native TriggerRegisterEnterRegion takes trigger whichTrigger,region whichRegion,boolexpr filter returns event
native GetTriggeringRegion takes nothing returns region
native GetEnteringUnit takes nothing returns unit
native TriggerRegisterLeaveRegion takes trigger whichTrigger,region whichRegion,boolexpr filter returns event
native GetLeavingUnit takes nothing returns unit
native TriggerRegisterTrackableHitEvent takes trigger whichTrigger,trackable t returns event
native TriggerRegisterTrackableTrackEvent takes trigger whichTrigger,trackable t returns event
native GetTriggeringTrackable takes nothing returns trackable
native GetClickedButton takes nothing returns button
native GetClickedDialog takes nothing returns dialog
native GetTournamentFinishSoonTimeRemaining takes nothing returns real
native GetTournamentFinishNowRule takes nothing returns integer
native GetTournamentFinishNowPlayer takes nothing returns player
native GetTournamentScore takes player whichPlayer returns integer
native GetSaveBasicFilename takes nothing returns string
native TriggerRegisterPlayerEvent takes trigger whichTrigger,player whichPlayer,playerevent whichPlayerEvent returns event
native GetTriggerPlayer takes nothing returns player
native TriggerRegisterPlayerUnitEvent takes trigger whichTrigger,player whichPlayer,playerunitevent whichPlayerUnitEvent,boolexpr filter returns event
native GetLevelingUnit takes nothing returns unit
native GetLearningUnit takes nothing returns unit
native GetLearnedSkill takes nothing returns integer
native GetLearnedSkillLevel takes nothing returns integer
native GetRevivableUnit takes nothing returns unit
native GetRevivingUnit takes nothing returns unit
native GetAttacker takes nothing returns unit
native GetRescuer takes nothing returns unit
native GetDyingUnit takes nothing returns unit
native GetKillingUnit takes nothing returns unit
native GetDecayingUnit takes nothing returns unit
native GetConstructingStructure takes nothing returns unit
native GetCancelledStructure takes nothing returns unit
native GetConstructedStructure takes nothing returns unit
native GetResearchingUnit takes nothing returns unit
native GetResearched takes nothing returns integer
native GetTrainedUnitType takes nothing returns integer
native GetTrainedUnit takes nothing returns unit
native GetDetectedUnit takes nothing returns unit
native GetSummoningUnit takes nothing returns unit
native GetSummonedUnit takes nothing returns unit
native GetTransportUnit takes nothing returns unit
native GetLoadedUnit takes nothing returns unit
native GetSellingUnit takes nothing returns unit
native GetSoldUnit takes nothing returns unit
native GetBuyingUnit takes nothing returns unit
native GetSoldItem takes nothing returns item
native GetChangingUnit takes nothing returns unit
native GetChangingUnitPrevOwner takes nothing returns player
native GetManipulatingUnit takes nothing returns unit
native GetManipulatedItem takes nothing returns item
native GetOrderedUnit takes nothing returns unit
native GetIssuedOrderId takes nothing returns integer
native GetOrderPointX takes nothing returns real
native GetOrderPointY takes nothing returns real
native GetOrderPointLoc takes nothing returns location
native GetOrderTarget takes nothing returns widget
native GetOrderTargetDestructable takes nothing returns destructable
native GetOrderTargetItem takes nothing returns item
native GetOrderTargetUnit takes nothing returns unit
native GetSpellAbilityUnit takes nothing returns unit
native GetSpellAbilityId takes nothing returns integer
native GetSpellAbility takes nothing returns ability
native GetSpellTargetLoc takes nothing returns location
native GetSpellTargetX takes nothing returns real
native GetSpellTargetY takes nothing returns real
native GetSpellTargetDestructable takes nothing returns destructable
native GetSpellTargetItem takes nothing returns item
native GetSpellTargetUnit takes nothing returns unit
native TriggerRegisterPlayerAllianceChange takes trigger whichTrigger,player whichPlayer,alliancetype whichAlliance returns event
native TriggerRegisterPlayerStateEvent takes trigger whichTrigger,player whichPlayer,playerstate whichState,limitop opcode,real limitval returns event
native GetEventPlayerState takes nothing returns playerstate
native TriggerRegisterPlayerChatEvent takes trigger whichTrigger,player whichPlayer,string chatMessageToDetect,boolean exactMatchOnly returns event
native GetEventPlayerChatString takes nothing returns string
native GetEventPlayerChatStringMatched takes nothing returns string
native TriggerRegisterDeathEvent takes trigger whichTrigger,widget whichWidget returns event
native GetTriggerUnit takes nothing returns unit
native TriggerRegisterUnitStateEvent takes trigger whichTrigger,unit whichUnit,unitstate whichState,limitop opcode,real limitval returns event
native GetEventUnitState takes nothing returns unitstate
native TriggerRegisterUnitEvent takes trigger whichTrigger,unit whichUnit,unitevent whichEvent returns event
native GetEventDamage takes nothing returns real
native GetEventDamageSource takes nothing returns unit
native GetEventDetectingPlayer takes nothing returns player
native TriggerRegisterFilterUnitEvent takes trigger whichTrigger,unit whichUnit,unitevent whichEvent,boolexpr filter returns event
native GetEventTargetUnit takes nothing returns unit
native TriggerRegisterUnitInRange takes trigger whichTrigger,unit whichUnit,real range,boolexpr filter returns event
native TriggerAddCondition takes trigger whichTrigger,boolexpr condition returns triggercondition
native TriggerRemoveCondition takes trigger whichTrigger,triggercondition whichCondition returns nothing
native TriggerClearConditions takes trigger whichTrigger returns nothing
native TriggerAddAction takes trigger whichTrigger,code actionFunc returns triggeraction
native TriggerRemoveAction takes trigger whichTrigger,triggeraction whichAction returns nothing
native TriggerClearActions takes trigger whichTrigger returns nothing
native TriggerSleepAction takes real timeout returns nothing
native TriggerWaitForSound takes sound s,real offset returns nothing
native TriggerEvaluate takes trigger whichTrigger returns boolean
native TriggerExecute takes trigger whichTrigger returns nothing
native TriggerExecuteWait takes trigger whichTrigger returns nothing
native TriggerSyncStart takes nothing returns nothing
native TriggerSyncReady takes nothing returns nothing
native GetWidgetLife takes widget whichWidget returns real
native SetWidgetLife takes widget whichWidget,real newLife returns nothing
native GetWidgetX takes widget whichWidget returns real
native GetWidgetY takes widget whichWidget returns real
native GetTriggerWidget takes nothing returns widget
native CreateDestructable takes integer objectid,real x,real y,real face,real scale,integer variation returns destructable
native CreateDestructableZ takes integer objectid,real x,real y,real z,real face,real scale,integer variation returns destructable
native CreateDeadDestructable takes integer objectid,real x,real y,real face,real scale,integer variation returns destructable
native CreateDeadDestructableZ takes integer objectid,real x,real y,real z,real face,real scale,integer variation returns destructable
native RemoveDestructable takes destructable d returns nothing
native KillDestructable takes destructable d returns nothing
native SetDestructableInvulnerable takes destructable d,boolean flag returns nothing
native IsDestructableInvulnerable takes destructable d returns boolean
native EnumDestructablesInRect takes rect r,boolexpr filter,code actionFunc returns nothing
native GetDestructableTypeId takes destructable d returns integer
native GetDestructableX takes destructable d returns real
native GetDestructableY takes destructable d returns real
native SetDestructableLife takes destructable d,real life returns nothing
native GetDestructableLife takes destructable d returns real
native SetDestructableMaxLife takes destructable d,real max returns nothing
native GetDestructableMaxLife takes destructable d returns real
native DestructableRestoreLife takes destructable d,real life,boolean birth returns nothing
native QueueDestructableAnimation takes destructable d,string whichAnimation returns nothing
native SetDestructableAnimation takes destructable d,string whichAnimation returns nothing
native SetDestructableAnimationSpeed takes destructable d,real speedFactor returns nothing
native ShowDestructable takes destructable d,boolean flag returns nothing
native GetDestructableOccluderHeight takes destructable d returns real
native SetDestructableOccluderHeight takes destructable d,real height returns nothing
native GetDestructableName takes destructable d returns string
native GetTriggerDestructable takes nothing returns destructable
native CreateItem takes integer itemid,real x,real y returns item
native RemoveItem takes item whichItem returns nothing
native GetItemPlayer takes item whichItem returns player
native GetItemTypeId takes item i returns integer
native GetItemX takes item i returns real
native GetItemY takes item i returns real
native SetItemPosition takes item i,real x,real y returns nothing
native SetItemDropOnDeath takes item whichItem,boolean flag returns nothing
native SetItemDroppable takes item i,boolean flag returns nothing
native SetItemPawnable takes item i,boolean flag returns nothing
native SetItemPlayer takes item whichItem,player whichPlayer,boolean changeColor returns nothing
native SetItemInvulnerable takes item whichItem,boolean flag returns nothing
native IsItemInvulnerable takes item whichItem returns boolean
native SetItemVisible takes item whichItem,boolean show returns nothing
native IsItemVisible takes item whichItem returns boolean
native IsItemOwned takes item whichItem returns boolean
native IsItemPowerup takes item whichItem returns boolean
native IsItemSellable takes item whichItem returns boolean
native IsItemPawnable takes item whichItem returns boolean
native IsItemIdPowerup takes integer itemId returns boolean
native IsItemIdSellable takes integer itemId returns boolean
native IsItemIdPawnable takes integer itemId returns boolean
native EnumItemsInRect takes rect r,boolexpr filter,code actionFunc returns nothing
native GetItemLevel takes item whichItem returns integer
native GetItemType takes item whichItem returns itemtype
native SetItemDropID takes item whichItem,integer unitId returns nothing
native GetItemName takes item whichItem returns string
native GetItemCharges takes item whichItem returns integer
native SetItemCharges takes item whichItem,integer charges returns nothing
native GetItemUserData takes item whichItem returns integer
native SetItemUserData takes item whichItem,integer data returns nothing
native CreateUnit takes player id,integer unitid,real x,real y,real face returns unit
native CreateUnitByName takes player whichPlayer,string unitname,real x,real y,real face returns unit
native CreateUnitAtLoc takes player id,integer unitid,location whichLocation,real face returns unit
native CreateUnitAtLocByName takes player id,string unitname,location whichLocation,real face returns unit
native CreateCorpse takes player whichPlayer,integer unitid,real x,real y,real face returns unit
native KillUnit takes unit whichUnit returns nothing
native RemoveUnit takes unit whichUnit returns nothing
native ShowUnit takes unit whichUnit,boolean show returns nothing
native SetUnitState takes unit whichUnit,unitstate whichUnitState,real newVal returns nothing
native SetUnitX takes unit whichUnit,real newX returns nothing
native SetUnitY takes unit whichUnit,real newY returns nothing
native SetUnitPosition takes unit whichUnit,real newX,real newY returns nothing
native SetUnitPositionLoc takes unit whichUnit,location whichLocation returns nothing
native SetUnitFacing takes unit whichUnit,real facingAngle returns nothing
native SetUnitFacingTimed takes unit whichUnit,real facingAngle,real duration returns nothing
native SetUnitMoveSpeed takes unit whichUnit,real newSpeed returns nothing
native SetUnitFlyHeight takes unit whichUnit,real newHeight,real rate returns nothing
native SetUnitTurnSpeed takes unit whichUnit,real newTurnSpeed returns nothing
native SetUnitPropWindow takes unit whichUnit,real newPropWindowAngle returns nothing
native SetUnitAcquireRange takes unit whichUnit,real newAcquireRange returns nothing
native SetUnitCreepGuard takes unit whichUnit,boolean creepGuard returns nothing
native GetUnitAcquireRange takes unit whichUnit returns real
native GetUnitTurnSpeed takes unit whichUnit returns real
native GetUnitPropWindow takes unit whichUnit returns real
native GetUnitFlyHeight takes unit whichUnit returns real
native GetUnitDefaultAcquireRange takes unit whichUnit returns real
native GetUnitDefaultTurnSpeed takes unit whichUnit returns real
native GetUnitDefaultPropWindow takes unit whichUnit returns real
native GetUnitDefaultFlyHeight takes unit whichUnit returns real
native SetUnitOwner takes unit whichUnit,player whichPlayer,boolean changeColor returns nothing
native SetUnitColor takes unit whichUnit,playercolor whichColor returns nothing
native SetUnitScale takes unit whichUnit,real scaleX,real scaleY,real scaleZ returns nothing
native SetUnitTimeScale takes unit whichUnit,real timeScale returns nothing
native SetUnitBlendTime takes unit whichUnit,real blendTime returns nothing
native SetUnitVertexColor takes unit whichUnit,integer red,integer green,integer blue,integer alpha returns nothing
native QueueUnitAnimation takes unit whichUnit,string whichAnimation returns nothing
native SetUnitAnimation takes unit whichUnit,string whichAnimation returns nothing
native SetUnitAnimationByIndex takes unit whichUnit,integer whichAnimation returns nothing
native SetUnitAnimationWithRarity takes unit whichUnit,string whichAnimation,raritycontrol rarity returns nothing
native AddUnitAnimationProperties takes unit whichUnit,string animProperties,boolean add returns nothing
native SetUnitLookAt takes unit whichUnit,string whichBone,unit lookAtTarget,real offsetX,real offsetY,real offsetZ returns nothing
native ResetUnitLookAt takes unit whichUnit returns nothing
native SetUnitRescuable takes unit whichUnit,player byWhichPlayer,boolean flag returns nothing
native SetUnitRescueRange takes unit whichUnit,real range returns nothing
native SetHeroStr takes unit whichHero,integer newStr,boolean permanent returns nothing
native SetHeroAgi takes unit whichHero,integer newAgi,boolean permanent returns nothing
native SetHeroInt takes unit whichHero,integer newInt,boolean permanent returns nothing
native GetHeroStr takes unit whichHero,boolean includeBonuses returns integer
native GetHeroAgi takes unit whichHero,boolean includeBonuses returns integer
native GetHeroInt takes unit whichHero,boolean includeBonuses returns integer
native UnitStripHeroLevel takes unit whichHero,integer howManyLevels returns boolean
native GetHeroXP takes unit whichHero returns integer
native SetHeroXP takes unit whichHero,integer newXpVal,boolean showEyeCandy returns nothing
native GetHeroSkillPoints takes unit whichHero returns integer
native UnitModifySkillPoints takes unit whichHero,integer skillPointDelta returns boolean
native AddHeroXP takes unit whichHero,integer xpToAdd,boolean showEyeCandy returns nothing
native SetHeroLevel takes unit whichHero,integer level,boolean showEyeCandy returns nothing
native GetHeroLevel takes unit whichHero returns integer
native GetUnitLevel takes unit whichUnit returns integer
native GetHeroProperName takes unit whichHero returns string
native SuspendHeroXP takes unit whichHero,boolean flag returns nothing
native IsSuspendedXP takes unit whichHero returns boolean
native SelectHeroSkill takes unit whichHero,integer abilcode returns nothing
native GetUnitAbilityLevel takes unit whichUnit,integer abilcode returns integer
native DecUnitAbilityLevel takes unit whichUnit,integer abilcode returns integer
native IncUnitAbilityLevel takes unit whichUnit,integer abilcode returns integer
native SetUnitAbilityLevel takes unit whichUnit,integer abilcode,integer level returns integer
native ReviveHero takes unit whichHero,real x,real y,boolean doEyecandy returns boolean
native ReviveHeroLoc takes unit whichHero,location loc,boolean doEyecandy returns boolean
native SetUnitExploded takes unit whichUnit,boolean exploded returns nothing
native SetUnitInvulnerable takes unit whichUnit,boolean flag returns nothing
native PauseUnit takes unit whichUnit,boolean flag returns nothing
native IsUnitPaused takes unit whichHero returns boolean
native SetUnitPathing takes unit whichUnit,boolean flag returns nothing
native ClearSelection takes nothing returns nothing
native SelectUnit takes unit whichUnit,boolean flag returns nothing
native GetUnitPointValue takes unit whichUnit returns integer
native GetUnitPointValueByType takes integer unitType returns integer
native UnitAddItem takes unit whichUnit,item whichItem returns boolean
native UnitAddItemById takes unit whichUnit,integer itemId returns item
native UnitAddItemToSlotById takes unit whichUnit,integer itemId,integer itemSlot returns boolean
native UnitRemoveItem takes unit whichUnit,item whichItem returns nothing
native UnitRemoveItemFromSlot takes unit whichUnit,integer itemSlot returns item
native UnitHasItem takes unit whichUnit,item whichItem returns boolean
native UnitItemInSlot takes unit whichUnit,integer itemSlot returns item
native UnitInventorySize takes unit whichUnit returns integer
native UnitDropItemPoint takes unit whichUnit,item whichItem,real x,real y returns boolean
native UnitDropItemSlot takes unit whichUnit,item whichItem,integer slot returns boolean
native UnitDropItemTarget takes unit whichUnit,item whichItem,widget target returns boolean
native UnitUseItem takes unit whichUnit,item whichItem returns boolean
native UnitUseItemPoint takes unit whichUnit,item whichItem,real x,real y returns boolean
native UnitUseItemTarget takes unit whichUnit,item whichItem,widget target returns boolean
native GetUnitX takes unit whichUnit returns real
native GetUnitY takes unit whichUnit returns real
native GetUnitLoc takes unit whichUnit returns location
native GetUnitFacing takes unit whichUnit returns real
native GetUnitMoveSpeed takes unit whichUnit returns real
native GetUnitDefaultMoveSpeed takes unit whichUnit returns real
native GetUnitState takes unit whichUnit,unitstate whichUnitState returns real
native GetOwningPlayer takes unit whichUnit returns player
native GetUnitTypeId takes unit whichUnit returns integer
native GetUnitRace takes unit whichUnit returns race
native GetUnitName takes unit whichUnit returns string
native GetUnitFoodUsed takes unit whichUnit returns integer
native GetUnitFoodMade takes unit whichUnit returns integer
native GetFoodMade takes integer unitId returns integer
native GetFoodUsed takes integer unitId returns integer
native SetUnitUseFood takes unit whichUnit,boolean useFood returns nothing
native GetUnitRallyPoint takes unit whichUnit returns location
native GetUnitRallyUnit takes unit whichUnit returns unit
native GetUnitRallyDestructable takes unit whichUnit returns destructable
native IsUnitInGroup takes unit whichUnit,group whichGroup returns boolean
native IsUnitInForce takes unit whichUnit,force whichForce returns boolean
native IsUnitOwnedByPlayer takes unit whichUnit,player whichPlayer returns boolean
native IsUnitAlly takes unit whichUnit,player whichPlayer returns boolean
native IsUnitEnemy takes unit whichUnit,player whichPlayer returns boolean
native IsUnitVisible takes unit whichUnit,player whichPlayer returns boolean
native IsUnitDetected takes unit whichUnit,player whichPlayer returns boolean
native IsUnitInvisible takes unit whichUnit,player whichPlayer returns boolean
native IsUnitFogged takes unit whichUnit,player whichPlayer returns boolean
native IsUnitMasked takes unit whichUnit,player whichPlayer returns boolean
native IsUnitSelected takes unit whichUnit,player whichPlayer returns boolean
native IsUnitRace takes unit whichUnit,race whichRace returns boolean
native IsUnitType takes unit whichUnit,unittype whichUnitType returns boolean
native IsUnit takes unit whichUnit,unit whichSpecifiedUnit returns boolean
native IsUnitInRange takes unit whichUnit,unit otherUnit,real distance returns boolean
native IsUnitInRangeXY takes unit whichUnit,real x,real y,real distance returns boolean
native IsUnitInRangeLoc takes unit whichUnit,location whichLocation,real distance returns boolean
native IsUnitHidden takes unit whichUnit returns boolean
native IsUnitIllusion takes unit whichUnit returns boolean
native IsUnitInTransport takes unit whichUnit,unit whichTransport returns boolean
native IsUnitLoaded takes unit whichUnit returns boolean
native IsHeroUnitId takes integer unitId returns boolean
native IsUnitIdType takes integer unitId,unittype whichUnitType returns boolean
native UnitShareVision takes unit whichUnit,player whichPlayer,boolean share returns nothing
native UnitSuspendDecay takes unit whichUnit,boolean suspend returns nothing
native UnitAddType takes unit whichUnit,unittype whichUnitType returns boolean
native UnitRemoveType takes unit whichUnit,unittype whichUnitType returns boolean
native UnitAddAbility takes unit whichUnit,integer abilityId returns boolean
native UnitRemoveAbility takes unit whichUnit,integer abilityId returns boolean
native UnitMakeAbilityPermanent takes unit whichUnit,boolean permanent,integer abilityId returns boolean
native UnitRemoveBuffs takes unit whichUnit,boolean removePositive,boolean removeNegative returns nothing
native UnitRemoveBuffsEx takes unit whichUnit,boolean removePositive,boolean removeNegative,boolean magic,boolean physical,boolean timedLife,boolean aura,boolean autoDispel returns nothing
native UnitHasBuffsEx takes unit whichUnit,boolean removePositive,boolean removeNegative,boolean magic,boolean physical,boolean timedLife,boolean aura,boolean autoDispel returns boolean
native UnitCountBuffsEx takes unit whichUnit,boolean removePositive,boolean removeNegative,boolean magic,boolean physical,boolean timedLife,boolean aura,boolean autoDispel returns integer
native UnitAddSleep takes unit whichUnit,boolean add returns nothing
native UnitCanSleep takes unit whichUnit returns boolean
native UnitAddSleepPerm takes unit whichUnit,boolean add returns nothing
native UnitCanSleepPerm takes unit whichUnit returns boolean
native UnitIsSleeping takes unit whichUnit returns boolean
native UnitWakeUp takes unit whichUnit returns nothing
native UnitApplyTimedLife takes unit whichUnit,integer buffId,real duration returns nothing
native UnitIgnoreAlarm takes unit whichUnit,boolean flag returns boolean
native UnitIgnoreAlarmToggled takes unit whichUnit returns boolean
native UnitResetCooldown takes unit whichUnit returns nothing
native UnitSetConstructionProgress takes unit whichUnit,integer constructionPercentage returns nothing
native UnitSetUpgradeProgress takes unit whichUnit,integer upgradePercentage returns nothing
native UnitPauseTimedLife takes unit whichUnit,boolean flag returns nothing
native UnitSetUsesAltIcon takes unit whichUnit,boolean flag returns nothing
native UnitDamagePoint takes unit whichUnit,real delay,real radius,real x,real y,real amount,boolean attack,boolean ranged,attacktype attackType,damagetype damageType,weapontype weaponType returns boolean
native UnitDamageTarget takes unit whichUnit,widget target,real amount,boolean attack,boolean ranged,attacktype attackType,damagetype damageType,weapontype weaponType returns boolean
native IssueImmediateOrder takes unit whichUnit,string order returns boolean
native IssueImmediateOrderById takes unit whichUnit,integer order returns boolean
native IssuePointOrder takes unit whichUnit,string order,real x,real y returns boolean
native IssuePointOrderLoc takes unit whichUnit,string order,location whichLocation returns boolean
native IssuePointOrderById takes unit whichUnit,integer order,real x,real y returns boolean
native IssuePointOrderByIdLoc takes unit whichUnit,integer order,location whichLocation returns boolean
native IssueTargetOrder takes unit whichUnit,string order,widget targetWidget returns boolean
native IssueTargetOrderById takes unit whichUnit,integer order,widget targetWidget returns boolean
native IssueInstantPointOrder takes unit whichUnit,string order,real x,real y,widget instantTargetWidget returns boolean
native IssueInstantPointOrderById takes unit whichUnit,integer order,real x,real y,widget instantTargetWidget returns boolean
native IssueInstantTargetOrder takes unit whichUnit,string order,widget targetWidget,widget instantTargetWidget returns boolean
native IssueInstantTargetOrderById takes unit whichUnit,integer order,widget targetWidget,widget instantTargetWidget returns boolean
native IssueBuildOrder takes unit whichPeon,string unitToBuild,real x,real y returns boolean
native IssueBuildOrderById takes unit whichPeon,integer unitId,real x,real y returns boolean
native IssueNeutralImmediateOrder takes player forWhichPlayer,unit neutralStructure,string unitToBuild returns boolean
native IssueNeutralImmediateOrderById takes player forWhichPlayer,unit neutralStructure,integer unitId returns boolean
native IssueNeutralPointOrder takes player forWhichPlayer,unit neutralStructure,string unitToBuild,real x,real y returns boolean
native IssueNeutralPointOrderById takes player forWhichPlayer,unit neutralStructure,integer unitId,real x,real y returns boolean
native IssueNeutralTargetOrder takes player forWhichPlayer,unit neutralStructure,string unitToBuild,widget target returns boolean
native IssueNeutralTargetOrderById takes player forWhichPlayer,unit neutralStructure,integer unitId,widget target returns boolean
native GetUnitCurrentOrder takes unit whichUnit returns integer
native SetResourceAmount takes unit whichUnit,integer amount returns nothing
native AddResourceAmount takes unit whichUnit,integer amount returns nothing
native GetResourceAmount takes unit whichUnit returns integer
native WaygateGetDestinationX takes unit waygate returns real
native WaygateGetDestinationY takes unit waygate returns real
native WaygateSetDestination takes unit waygate,real x,real y returns nothing
native WaygateActivate takes unit waygate,boolean activate returns nothing
native WaygateIsActive takes unit waygate returns boolean
native AddItemToAllStock takes integer itemId,integer currentStock,integer stockMax returns nothing
native AddItemToStock takes unit whichUnit,integer itemId,integer currentStock,integer stockMax returns nothing
native AddUnitToAllStock takes integer unitId,integer currentStock,integer stockMax returns nothing
native AddUnitToStock takes unit whichUnit,integer unitId,integer currentStock,integer stockMax returns nothing
native RemoveItemFromAllStock takes integer itemId returns nothing
native RemoveItemFromStock takes unit whichUnit,integer itemId returns nothing
native RemoveUnitFromAllStock takes integer unitId returns nothing
native RemoveUnitFromStock takes unit whichUnit,integer unitId returns nothing
native SetAllItemTypeSlots takes integer slots returns nothing
native SetAllUnitTypeSlots takes integer slots returns nothing
native SetItemTypeSlots takes unit whichUnit,integer slots returns nothing
native SetUnitTypeSlots takes unit whichUnit,integer slots returns nothing
native GetUnitUserData takes unit whichUnit returns integer
native SetUnitUserData takes unit whichUnit,integer data returns nothing
native Player takes integer number returns player
native GetLocalPlayer takes nothing returns player
native IsPlayerAlly takes player whichPlayer,player otherPlayer returns boolean
native IsPlayerEnemy takes player whichPlayer,player otherPlayer returns boolean
native IsPlayerInForce takes player whichPlayer,force whichForce returns boolean
native IsPlayerObserver takes player whichPlayer returns boolean
native IsVisibleToPlayer takes real x,real y,player whichPlayer returns boolean
native IsLocationVisibleToPlayer takes location whichLocation,player whichPlayer returns boolean
native IsFoggedToPlayer takes real x,real y,player whichPlayer returns boolean
native IsLocationFoggedToPlayer takes location whichLocation,player whichPlayer returns boolean
native IsMaskedToPlayer takes real x,real y,player whichPlayer returns boolean
native IsLocationMaskedToPlayer takes location whichLocation,player whichPlayer returns boolean
native GetPlayerRace takes player whichPlayer returns race
native GetPlayerId takes player whichPlayer returns integer
native GetPlayerUnitCount takes player whichPlayer,boolean includeIncomplete returns integer
native GetPlayerTypedUnitCount takes player whichPlayer,string unitName,boolean includeIncomplete,boolean includeUpgrades returns integer
native GetPlayerStructureCount takes player whichPlayer,boolean includeIncomplete returns integer
native GetPlayerState takes player whichPlayer,playerstate whichPlayerState returns integer
native GetPlayerScore takes player whichPlayer,playerscore whichPlayerScore returns integer
native GetPlayerAlliance takes player sourcePlayer,player otherPlayer,alliancetype whichAllianceSetting returns boolean
native GetPlayerHandicap takes player whichPlayer returns real
native GetPlayerHandicapXP takes player whichPlayer returns real
native SetPlayerHandicap takes player whichPlayer,real handicap returns nothing
native SetPlayerHandicapXP takes player whichPlayer,real handicap returns nothing
native SetPlayerTechMaxAllowed takes player whichPlayer,integer techid,integer maximum returns nothing
native GetPlayerTechMaxAllowed takes player whichPlayer,integer techid returns integer
native AddPlayerTechResearched takes player whichPlayer,integer techid,integer levels returns nothing
native SetPlayerTechResearched takes player whichPlayer,integer techid,integer setToLevel returns nothing
native GetPlayerTechResearched takes player whichPlayer,integer techid,boolean specificonly returns boolean
native GetPlayerTechCount takes player whichPlayer,integer techid,boolean specificonly returns integer
native SetPlayerUnitsOwner takes player whichPlayer,integer newOwner returns nothing
native CripplePlayer takes player whichPlayer,force toWhichPlayers,boolean flag returns nothing
native SetPlayerAbilityAvailable takes player whichPlayer,integer abilid,boolean avail returns nothing
native SetPlayerState takes player whichPlayer,playerstate whichPlayerState,integer value returns nothing
native RemovePlayer takes player whichPlayer,playergameresult gameResult returns nothing
native CachePlayerHeroData takes player whichPlayer returns nothing
native SetFogStateRect takes player forWhichPlayer,fogstate whichState,rect where,boolean useSharedVision returns nothing
native SetFogStateRadius takes player forWhichPlayer,fogstate whichState,real centerx,real centerY,real radius,boolean useSharedVision returns nothing
native SetFogStateRadiusLoc takes player forWhichPlayer,fogstate whichState,location center,real radius,boolean useSharedVision returns nothing
native FogMaskEnable takes boolean enable returns nothing
native IsFogMaskEnabled takes nothing returns boolean
native FogEnable takes boolean enable returns nothing
native IsFogEnabled takes nothing returns boolean
native CreateFogModifierRect takes player forWhichPlayer,fogstate whichState,rect where,boolean useSharedVision,boolean afterUnits returns fogmodifier
native CreateFogModifierRadius takes player forWhichPlayer,fogstate whichState,real centerx,real centerY,real radius,boolean useSharedVision,boolean afterUnits returns fogmodifier
native CreateFogModifierRadiusLoc takes player forWhichPlayer,fogstate whichState,location center,real radius,boolean useSharedVision,boolean afterUnits returns fogmodifier
native DestroyFogModifier takes fogmodifier whichFogModifier returns nothing
native FogModifierStart takes fogmodifier whichFogModifier returns nothing
native FogModifierStop takes fogmodifier whichFogModifier returns nothing
native VersionGet takes nothing returns version
native VersionCompatible takes version whichVersion returns boolean
native VersionSupported takes version whichVersion returns boolean
native EndGame takes boolean doScoreScreen returns nothing
native ChangeLevel takes string newLevel,boolean doScoreScreen returns nothing
native RestartGame takes boolean doScoreScreen returns nothing
native ReloadGame takes nothing returns nothing
native SetCampaignMenuRace takes race r returns nothing
native SetCampaignMenuRaceEx takes integer campaignIndex returns nothing
native ForceCampaignSelectScreen takes nothing returns nothing
native LoadGame takes string saveFileName,boolean doScoreScreen returns nothing
native SaveGame takes string saveFileName returns nothing
native RenameSaveDirectory takes string sourceDirName,string destDirName returns boolean
native RemoveSaveDirectory takes string sourceDirName returns boolean
native CopySaveGame takes string sourceSaveName,string destSaveName returns boolean
native SaveGameExists takes string saveName returns boolean
native SyncSelections takes nothing returns nothing
native SetFloatGameState takes fgamestate whichFloatGameState,real value returns nothing
native GetFloatGameState takes fgamestate whichFloatGameState returns real
native SetIntegerGameState takes igamestate whichIntegerGameState,integer value returns nothing
native GetIntegerGameState takes igamestate whichIntegerGameState returns integer
native SetTutorialCleared takes boolean cleared returns nothing
native SetMissionAvailable takes integer campaignNumber,integer missionNumber,boolean available returns nothing
native SetCampaignAvailable takes integer campaignNumber,boolean available returns nothing
native SetOpCinematicAvailable takes integer campaignNumber,boolean available returns nothing
native SetEdCinematicAvailable takes integer campaignNumber,boolean available returns nothing
native GetDefaultDifficulty takes nothing returns gamedifficulty
native SetDefaultDifficulty takes gamedifficulty g returns nothing
native SetCustomCampaignButtonVisible takes integer whichButton,boolean visible returns nothing
native GetCustomCampaignButtonVisible takes integer whichButton returns boolean
native DoNotSaveReplay takes nothing returns nothing
native DialogCreate takes nothing returns dialog
native DialogDestroy takes dialog whichDialog returns nothing
native DialogClear takes dialog whichDialog returns nothing
native DialogSetMessage takes dialog whichDialog,string messageText returns nothing
native DialogAddButton takes dialog whichDialog,string buttonText,integer hotkey returns button
native DialogAddQuitButton takes dialog whichDialog,boolean doScoreScreen,string buttonText,integer hotkey returns button
native DialogDisplay takes player whichPlayer,dialog whichDialog,boolean flag returns nothing
native ReloadGameCachesFromDisk takes nothing returns boolean
native InitGameCache takes string campaignFile returns gamecache
native SaveGameCache takes gamecache whichCache returns boolean
native StoreInteger takes gamecache cache,string missionKey,string key,integer value returns nothing
native StoreReal takes gamecache cache,string missionKey,string key,real value returns nothing
native StoreBoolean takes gamecache cache,string missionKey,string key,boolean value returns nothing
native StoreUnit takes gamecache cache,string missionKey,string key,unit whichUnit returns boolean
native StoreString takes gamecache cache,string missionKey,string key,string value returns boolean
native SyncStoredInteger takes gamecache cache,string missionKey,string key returns nothing
native SyncStoredReal takes gamecache cache,string missionKey,string key returns nothing
native SyncStoredBoolean takes gamecache cache,string missionKey,string key returns nothing
native SyncStoredUnit takes gamecache cache,string missionKey,string key returns nothing
native SyncStoredString takes gamecache cache,string missionKey,string key returns nothing
native HaveStoredInteger takes gamecache cache,string missionKey,string key returns boolean
native HaveStoredReal takes gamecache cache,string missionKey,string key returns boolean
native HaveStoredBoolean takes gamecache cache,string missionKey,string key returns boolean
native HaveStoredUnit takes gamecache cache,string missionKey,string key returns boolean
native HaveStoredString takes gamecache cache,string missionKey,string key returns boolean
native FlushGameCache takes gamecache cache returns nothing
native FlushStoredMission takes gamecache cache,string missionKey returns nothing
native FlushStoredInteger takes gamecache cache,string missionKey,string key returns nothing
native FlushStoredReal takes gamecache cache,string missionKey,string key returns nothing
native FlushStoredBoolean takes gamecache cache,string missionKey,string key returns nothing
native FlushStoredUnit takes gamecache cache,string missionKey,string key returns nothing
native FlushStoredString takes gamecache cache,string missionKey,string key returns nothing
native GetStoredInteger takes gamecache cache,string missionKey,string key returns integer
native GetStoredReal takes gamecache cache,string missionKey,string key returns real
native GetStoredBoolean takes gamecache cache,string missionKey,string key returns boolean
native GetStoredString takes gamecache cache,string missionKey,string key returns string
native RestoreUnit takes gamecache cache,string missionKey,string key,player forWhichPlayer,real x,real y,real facing returns unit
native InitHashtable takes nothing returns hashtable
native SaveInteger takes hashtable table,integer parentKey,integer childKey,integer value returns nothing
native SaveReal takes hashtable table,integer parentKey,integer childKey,real value returns nothing
native SaveBoolean takes hashtable table,integer parentKey,integer childKey,boolean value returns nothing
native SaveStr takes hashtable table,integer parentKey,integer childKey,string value returns boolean
native SavePlayerHandle takes hashtable table,integer parentKey,integer childKey,player whichPlayer returns boolean
native SaveWidgetHandle takes hashtable table,integer parentKey,integer childKey,widget whichWidget returns boolean
native SaveDestructableHandle takes hashtable table,integer parentKey,integer childKey,destructable whichDestructable returns boolean
native SaveItemHandle takes hashtable table,integer parentKey,integer childKey,item whichItem returns boolean
native SaveUnitHandle takes hashtable table,integer parentKey,integer childKey,unit whichUnit returns boolean
native SaveAbilityHandle takes hashtable table,integer parentKey,integer childKey,ability whichAbility returns boolean
native SaveTimerHandle takes hashtable table,integer parentKey,integer childKey,timer whichTimer returns boolean
native SaveTriggerHandle takes hashtable table,integer parentKey,integer childKey,trigger whichTrigger returns boolean
native SaveTriggerConditionHandle takes hashtable table,integer parentKey,integer childKey,triggercondition whichTriggercondition returns boolean
native SaveTriggerActionHandle takes hashtable table,integer parentKey,integer childKey,triggeraction whichTriggeraction returns boolean
native SaveTriggerEventHandle takes hashtable table,integer parentKey,integer childKey,event whichEvent returns boolean
native SaveForceHandle takes hashtable table,integer parentKey,integer childKey,force whichForce returns boolean
native SaveGroupHandle takes hashtable table,integer parentKey,integer childKey,group whichGroup returns boolean
native SaveLocationHandle takes hashtable table,integer parentKey,integer childKey,location whichLocation returns boolean
native SaveRectHandle takes hashtable table,integer parentKey,integer childKey,rect whichRect returns boolean
native SaveBooleanExprHandle takes hashtable table,integer parentKey,integer childKey,boolexpr whichBoolexpr returns boolean
native SaveSoundHandle takes hashtable table,integer parentKey,integer childKey,sound whichSound returns boolean
native SaveEffectHandle takes hashtable table,integer parentKey,integer childKey,effect whichEffect returns boolean
native SaveUnitPoolHandle takes hashtable table,integer parentKey,integer childKey,unitpool whichUnitpool returns boolean
native SaveItemPoolHandle takes hashtable table,integer parentKey,integer childKey,itempool whichItempool returns boolean
native SaveQuestHandle takes hashtable table,integer parentKey,integer childKey,quest whichQuest returns boolean
native SaveQuestItemHandle takes hashtable table,integer parentKey,integer childKey,questitem whichQuestitem returns boolean
native SaveDefeatConditionHandle takes hashtable table,integer parentKey,integer childKey,defeatcondition whichDefeatcondition returns boolean
native SaveTimerDialogHandle takes hashtable table,integer parentKey,integer childKey,timerdialog whichTimerdialog returns boolean
native SaveLeaderboardHandle takes hashtable table,integer parentKey,integer childKey,leaderboard whichLeaderboard returns boolean
native SaveMultiboardHandle takes hashtable table,integer parentKey,integer childKey,multiboard whichMultiboard returns boolean
native SaveMultiboardItemHandle takes hashtable table,integer parentKey,integer childKey,multiboarditem whichMultiboarditem returns boolean
native SaveTrackableHandle takes hashtable table,integer parentKey,integer childKey,trackable whichTrackable returns boolean
native SaveDialogHandle takes hashtable table,integer parentKey,integer childKey,dialog whichDialog returns boolean
native SaveButtonHandle takes hashtable table,integer parentKey,integer childKey,button whichButton returns boolean
native SaveTextTagHandle takes hashtable table,integer parentKey,integer childKey,texttag whichTexttag returns boolean
native SaveLightningHandle takes hashtable table,integer parentKey,integer childKey,lightning whichLightning returns boolean
native SaveImageHandle takes hashtable table,integer parentKey,integer childKey,image whichImage returns boolean
native SaveUbersplatHandle takes hashtable table,integer parentKey,integer childKey,ubersplat whichUbersplat returns boolean
native SaveRegionHandle takes hashtable table,integer parentKey,integer childKey,region whichRegion returns boolean
native SaveFogStateHandle takes hashtable table,integer parentKey,integer childKey,fogstate whichFogState returns boolean
native SaveFogModifierHandle takes hashtable table,integer parentKey,integer childKey,fogmodifier whichFogModifier returns boolean
native SaveAgentHandle takes hashtable table,integer parentKey,integer childKey,agent whichAgent returns boolean
native SaveHashtableHandle takes hashtable table,integer parentKey,integer childKey,hashtable whichHashtable returns boolean
native LoadInteger takes hashtable table,integer parentKey,integer childKey returns integer
native LoadReal takes hashtable table,integer parentKey,integer childKey returns real
native LoadBoolean takes hashtable table,integer parentKey,integer childKey returns boolean
native LoadStr takes hashtable table,integer parentKey,integer childKey returns string
native LoadPlayerHandle takes hashtable table,integer parentKey,integer childKey returns player
native LoadWidgetHandle takes hashtable table,integer parentKey,integer childKey returns widget
native LoadDestructableHandle takes hashtable table,integer parentKey,integer childKey returns destructable
native LoadItemHandle takes hashtable table,integer parentKey,integer childKey returns item
native LoadUnitHandle takes hashtable table,integer parentKey,integer childKey returns unit
native LoadAbilityHandle takes hashtable table,integer parentKey,integer childKey returns ability
native LoadTimerHandle takes hashtable table,integer parentKey,integer childKey returns timer
native LoadTriggerHandle takes hashtable table,integer parentKey,integer childKey returns trigger
native LoadTriggerConditionHandle takes hashtable table,integer parentKey,integer childKey returns triggercondition
native LoadTriggerActionHandle takes hashtable table,integer parentKey,integer childKey returns triggeraction
native LoadTriggerEventHandle takes hashtable table,integer parentKey,integer childKey returns event
native LoadForceHandle takes hashtable table,integer parentKey,integer childKey returns force
native LoadGroupHandle takes hashtable table,integer parentKey,integer childKey returns group
native LoadLocationHandle takes hashtable table,integer parentKey,integer childKey returns location
native LoadRectHandle takes hashtable table,integer parentKey,integer childKey returns rect
native LoadBooleanExprHandle takes hashtable table,integer parentKey,integer childKey returns boolexpr
native LoadSoundHandle takes hashtable table,integer parentKey,integer childKey returns sound
native LoadEffectHandle takes hashtable table,integer parentKey,integer childKey returns effect
native LoadUnitPoolHandle takes hashtable table,integer parentKey,integer childKey returns unitpool
native LoadItemPoolHandle takes hashtable table,integer parentKey,integer childKey returns itempool
native LoadQuestHandle takes hashtable table,integer parentKey,integer childKey returns quest
native LoadQuestItemHandle takes hashtable table,integer parentKey,integer childKey returns questitem
native LoadDefeatConditionHandle takes hashtable table,integer parentKey,integer childKey returns defeatcondition
native LoadTimerDialogHandle takes hashtable table,integer parentKey,integer childKey returns timerdialog
native LoadLeaderboardHandle takes hashtable table,integer parentKey,integer childKey returns leaderboard
native LoadMultiboardHandle takes hashtable table,integer parentKey,integer childKey returns multiboard
native LoadMultiboardItemHandle takes hashtable table,integer parentKey,integer childKey returns multiboarditem
native LoadTrackableHandle takes hashtable table,integer parentKey,integer childKey returns trackable
native LoadDialogHandle takes hashtable table,integer parentKey,integer childKey returns dialog
native LoadButtonHandle takes hashtable table,integer parentKey,integer childKey returns button
native LoadTextTagHandle takes hashtable table,integer parentKey,integer childKey returns texttag
native LoadLightningHandle takes hashtable table,integer parentKey,integer childKey returns lightning
native LoadImageHandle takes hashtable table,integer parentKey,integer childKey returns image
native LoadUbersplatHandle takes hashtable table,integer parentKey,integer childKey returns ubersplat
native LoadRegionHandle takes hashtable table,integer parentKey,integer childKey returns region
native LoadFogStateHandle takes hashtable table,integer parentKey,integer childKey returns fogstate
native LoadFogModifierHandle takes hashtable table,integer parentKey,integer childKey returns fogmodifier
native LoadHashtableHandle takes hashtable table,integer parentKey,integer childKey returns hashtable
native HaveSavedInteger takes hashtable table,integer parentKey,integer childKey returns boolean
native HaveSavedReal takes hashtable table,integer parentKey,integer childKey returns boolean
native HaveSavedBoolean takes hashtable table,integer parentKey,integer childKey returns boolean
native HaveSavedString takes hashtable table,integer parentKey,integer childKey returns boolean
native HaveSavedHandle takes hashtable table,integer parentKey,integer childKey returns boolean
native RemoveSavedInteger takes hashtable table,integer parentKey,integer childKey returns nothing
native RemoveSavedReal takes hashtable table,integer parentKey,integer childKey returns nothing
native RemoveSavedBoolean takes hashtable table,integer parentKey,integer childKey returns nothing
native RemoveSavedString takes hashtable table,integer parentKey,integer childKey returns nothing
native RemoveSavedHandle takes hashtable table,integer parentKey,integer childKey returns nothing
native FlushParentHashtable takes hashtable table returns nothing
native FlushChildHashtable takes hashtable table,integer parentKey returns nothing
native GetRandomInt takes integer lowBound,integer highBound returns integer
native GetRandomReal takes real lowBound,real highBound returns real
native CreateUnitPool takes nothing returns unitpool
native DestroyUnitPool takes unitpool whichPool returns nothing
native UnitPoolAddUnitType takes unitpool whichPool,integer unitId,real weight returns nothing
native UnitPoolRemoveUnitType takes unitpool whichPool,integer unitId returns nothing
native PlaceRandomUnit takes unitpool whichPool,player forWhichPlayer,real x,real y,real facing returns unit
native CreateItemPool takes nothing returns itempool
native DestroyItemPool takes itempool whichItemPool returns nothing
native ItemPoolAddItemType takes itempool whichItemPool,integer itemId,real weight returns nothing
native ItemPoolRemoveItemType takes itempool whichItemPool,integer itemId returns nothing
native PlaceRandomItem takes itempool whichItemPool,real x,real y returns item
native ChooseRandomCreep takes integer level returns integer
native ChooseRandomNPBuilding takes nothing returns integer
native ChooseRandomItem takes integer level returns integer
native ChooseRandomItemEx takes itemtype whichType,integer level returns integer
native SetRandomSeed takes integer seed returns nothing
native SetTerrainFog takes real a,real b,real c,real d,real e returns nothing
native ResetTerrainFog takes nothing returns nothing
native SetUnitFog takes real a,real b,real c,real d,real e returns nothing
native SetTerrainFogEx takes integer style,real zstart,real zend,real density,real red,real green,real blue returns nothing
native DisplayTextToPlayer takes player toPlayer,real x,real y,string message returns nothing
native DisplayTimedTextToPlayer takes player toPlayer,real x,real y,real duration,string message returns nothing
native DisplayTimedTextFromPlayer takes player toPlayer,real x,real y,real duration,string message returns nothing
native ClearTextMessages takes nothing returns nothing
native SetDayNightModels takes string terrainDNCFile,string unitDNCFile returns nothing
native SetSkyModel takes string skyModelFile returns nothing
native EnableUserControl takes boolean b returns nothing
native EnableUserUI takes boolean b returns nothing
native SuspendTimeOfDay takes boolean b returns nothing
native SetTimeOfDayScale takes real r returns nothing
native GetTimeOfDayScale takes nothing returns real
native ShowInterface takes boolean flag,real fadeDuration returns nothing
native PauseGame takes boolean flag returns nothing
native UnitAddIndicator takes unit whichUnit,integer red,integer green,integer blue,integer alpha returns nothing
native AddIndicator takes widget whichWidget,integer red,integer green,integer blue,integer alpha returns nothing
native PingMinimap takes real x,real y,real duration returns nothing
native PingMinimapEx takes real x,real y,real duration,integer red,integer green,integer blue,boolean extraEffects returns nothing
native EnableOcclusion takes boolean flag returns nothing
native SetIntroShotText takes string introText returns nothing
native SetIntroShotModel takes string introModelPath returns nothing
native EnableWorldFogBoundary takes boolean b returns nothing
native PlayModelCinematic takes string modelName returns nothing
native PlayCinematic takes string movieName returns nothing
native ForceUIKey takes string key returns nothing
native ForceUICancel takes nothing returns nothing
native DisplayLoadDialog takes nothing returns nothing
native SetAltMinimapIcon takes string iconPath returns nothing
native DisableRestartMission takes boolean flag returns nothing
native CreateTextTag takes nothing returns texttag
native DestroyTextTag takes texttag t returns nothing
native SetTextTagText takes texttag t,string s,real height returns nothing
native SetTextTagPos takes texttag t,real x,real y,real heightOffset returns nothing
native SetTextTagPosUnit takes texttag t,unit whichUnit,real heightOffset returns nothing
native SetTextTagColor takes texttag t,integer red,integer green,integer blue,integer alpha returns nothing
native SetTextTagVelocity takes texttag t,real xvel,real yvel returns nothing
native SetTextTagVisibility takes texttag t,boolean flag returns nothing
native SetTextTagSuspended takes texttag t,boolean flag returns nothing
native SetTextTagPermanent takes texttag t,boolean flag returns nothing
native SetTextTagAge takes texttag t,real age returns nothing
native SetTextTagLifespan takes texttag t,real lifespan returns nothing
native SetTextTagFadepoint takes texttag t,real fadepoint returns nothing
native SetReservedLocalHeroButtons takes integer reserved returns nothing
native GetAllyColorFilterState takes nothing returns integer
native SetAllyColorFilterState takes integer state returns nothing
native GetCreepCampFilterState takes nothing returns boolean
native SetCreepCampFilterState takes boolean state returns nothing
native EnableMinimapFilterButtons takes boolean enableAlly,boolean enableCreep returns nothing
native EnableDragSelect takes boolean state,boolean ui returns nothing
native EnablePreSelect takes boolean state,boolean ui returns nothing
native EnableSelect takes boolean state,boolean ui returns nothing
native CreateTrackable takes string trackableModelPath,real x,real y,real facing returns trackable
native CreateQuest takes nothing returns quest
native DestroyQuest takes quest whichQuest returns nothing
native QuestSetTitle takes quest whichQuest,string title returns nothing
native QuestSetDescription takes quest whichQuest,string description returns nothing
native QuestSetIconPath takes quest whichQuest,string iconPath returns nothing
native QuestSetRequired takes quest whichQuest,boolean required returns nothing
native QuestSetCompleted takes quest whichQuest,boolean completed returns nothing
native QuestSetDiscovered takes quest whichQuest,boolean discovered returns nothing
native QuestSetFailed takes quest whichQuest,boolean failed returns nothing
native QuestSetEnabled takes quest whichQuest,boolean enabled returns nothing
native IsQuestRequired takes quest whichQuest returns boolean
native IsQuestCompleted takes quest whichQuest returns boolean
native IsQuestDiscovered takes quest whichQuest returns boolean
native IsQuestFailed takes quest whichQuest returns boolean
native IsQuestEnabled takes quest whichQuest returns boolean
native QuestCreateItem takes quest whichQuest returns questitem
native QuestItemSetDescription takes questitem whichQuestItem,string description returns nothing
native QuestItemSetCompleted takes questitem whichQuestItem,boolean completed returns nothing
native IsQuestItemCompleted takes questitem whichQuestItem returns boolean
native CreateDefeatCondition takes nothing returns defeatcondition
native DestroyDefeatCondition takes defeatcondition whichCondition returns nothing
native DefeatConditionSetDescription takes defeatcondition whichCondition,string description returns nothing
native FlashQuestDialogButton takes nothing returns nothing
native ForceQuestDialogUpdate takes nothing returns nothing
native CreateTimerDialog takes timer t returns timerdialog
native DestroyTimerDialog takes timerdialog whichDialog returns nothing
native TimerDialogSetTitle takes timerdialog whichDialog,string title returns nothing
native TimerDialogSetTitleColor takes timerdialog whichDialog,integer red,integer green,integer blue,integer alpha returns nothing
native TimerDialogSetTimeColor takes timerdialog whichDialog,integer red,integer green,integer blue,integer alpha returns nothing
native TimerDialogSetSpeed takes timerdialog whichDialog,real speedMultFactor returns nothing
native TimerDialogDisplay takes timerdialog whichDialog,boolean display returns nothing
native IsTimerDialogDisplayed takes timerdialog whichDialog returns boolean
native TimerDialogSetRealTimeRemaining takes timerdialog whichDialog,real timeRemaining returns nothing
native CreateLeaderboard takes nothing returns leaderboard
native DestroyLeaderboard takes leaderboard lb returns nothing
native LeaderboardDisplay takes leaderboard lb,boolean show returns nothing
native IsLeaderboardDisplayed takes leaderboard lb returns boolean
native LeaderboardGetItemCount takes leaderboard lb returns integer
native LeaderboardSetSizeByItemCount takes leaderboard lb,integer count returns nothing
native LeaderboardAddItem takes leaderboard lb,string label,integer value,player p returns nothing
native LeaderboardRemoveItem takes leaderboard lb,integer index returns nothing
native LeaderboardRemovePlayerItem takes leaderboard lb,player p returns nothing
native LeaderboardClear takes leaderboard lb returns nothing
native LeaderboardSortItemsByValue takes leaderboard lb,boolean ascending returns nothing
native LeaderboardSortItemsByPlayer takes leaderboard lb,boolean ascending returns nothing
native LeaderboardSortItemsByLabel takes leaderboard lb,boolean ascending returns nothing
native LeaderboardHasPlayerItem takes leaderboard lb,player p returns boolean
native LeaderboardGetPlayerIndex takes leaderboard lb,player p returns integer
native LeaderboardSetLabel takes leaderboard lb,string label returns nothing
native LeaderboardGetLabelText takes leaderboard lb returns string
native PlayerSetLeaderboard takes player toPlayer,leaderboard lb returns nothing
native PlayerGetLeaderboard takes player toPlayer returns leaderboard
native LeaderboardSetLabelColor takes leaderboard lb,integer red,integer green,integer blue,integer alpha returns nothing
native LeaderboardSetValueColor takes leaderboard lb,integer red,integer green,integer blue,integer alpha returns nothing
native LeaderboardSetStyle takes leaderboard lb,boolean showLabel,boolean showNames,boolean showValues,boolean showIcons returns nothing
native LeaderboardSetItemValue takes leaderboard lb,integer whichItem,integer val returns nothing
native LeaderboardSetItemLabel takes leaderboard lb,integer whichItem,string val returns nothing
native LeaderboardSetItemStyle takes leaderboard lb,integer whichItem,boolean showLabel,boolean showValue,boolean showIcon returns nothing
native LeaderboardSetItemLabelColor takes leaderboard lb,integer whichItem,integer red,integer green,integer blue,integer alpha returns nothing
native LeaderboardSetItemValueColor takes leaderboard lb,integer whichItem,integer red,integer green,integer blue,integer alpha returns nothing
native CreateMultiboard takes nothing returns multiboard
native DestroyMultiboard takes multiboard lb returns nothing
native MultiboardDisplay takes multiboard lb,boolean show returns nothing
native IsMultiboardDisplayed takes multiboard lb returns boolean
native MultiboardMinimize takes multiboard lb,boolean minimize returns nothing
native IsMultiboardMinimized takes multiboard lb returns boolean
native MultiboardClear takes multiboard lb returns nothing
native MultiboardSetTitleText takes multiboard lb,string label returns nothing
native MultiboardGetTitleText takes multiboard lb returns string
native MultiboardSetTitleTextColor takes multiboard lb,integer red,integer green,integer blue,integer alpha returns nothing
native MultiboardGetRowCount takes multiboard lb returns integer
native MultiboardGetColumnCount takes multiboard lb returns integer
native MultiboardSetColumnCount takes multiboard lb,integer count returns nothing
native MultiboardSetRowCount takes multiboard lb,integer count returns nothing
native MultiboardSetItemsStyle takes multiboard lb,boolean showValues,boolean showIcons returns nothing
native MultiboardSetItemsValue takes multiboard lb,string value returns nothing
native MultiboardSetItemsValueColor takes multiboard lb,integer red,integer green,integer blue,integer alpha returns nothing
native MultiboardSetItemsWidth takes multiboard lb,real width returns nothing
native MultiboardSetItemsIcon takes multiboard lb,string iconPath returns nothing
native MultiboardGetItem takes multiboard lb,integer row,integer column returns multiboarditem
native MultiboardReleaseItem takes multiboarditem mbi returns nothing
native MultiboardSetItemStyle takes multiboarditem mbi,boolean showValue,boolean showIcon returns nothing
native MultiboardSetItemValue takes multiboarditem mbi,string val returns nothing
native MultiboardSetItemValueColor takes multiboarditem mbi,integer red,integer green,integer blue,integer alpha returns nothing
native MultiboardSetItemWidth takes multiboarditem mbi,real width returns nothing
native MultiboardSetItemIcon takes multiboarditem mbi,string iconFileName returns nothing
native MultiboardSuppressDisplay takes boolean flag returns nothing
native SetCameraPosition takes real x,real y returns nothing
native SetCameraQuickPosition takes real x,real y returns nothing
native SetCameraBounds takes real x1,real y1,real x2,real y2,real x3,real y3,real x4,real y4 returns nothing
native StopCamera takes nothing returns nothing
native ResetToGameCamera takes real duration returns nothing
native PanCameraTo takes real x,real y returns nothing
native PanCameraToTimed takes real x,real y,real duration returns nothing
native PanCameraToWithZ takes real x,real y,real zOffsetDest returns nothing
native PanCameraToTimedWithZ takes real x,real y,real zOffsetDest,real duration returns nothing
native SetCinematicCamera takes string cameraModelFile returns nothing
native SetCameraRotateMode takes real x,real y,real radiansToSweep,real duration returns nothing
native SetCameraField takes camerafield whichField,real value,real duration returns nothing
native AdjustCameraField takes camerafield whichField,real offset,real duration returns nothing
native SetCameraTargetController takes unit whichUnit,real xoffset,real yoffset,boolean inheritOrientation returns nothing
native SetCameraOrientController takes unit whichUnit,real xoffset,real yoffset returns nothing
native CreateCameraSetup takes nothing returns camerasetup
native CameraSetupSetField takes camerasetup whichSetup,camerafield whichField,real value,real duration returns nothing
native CameraSetupGetField takes camerasetup whichSetup,camerafield whichField returns real
native CameraSetupSetDestPosition takes camerasetup whichSetup,real x,real y,real duration returns nothing
native CameraSetupGetDestPositionLoc takes camerasetup whichSetup returns location
native CameraSetupGetDestPositionX takes camerasetup whichSetup returns real
native CameraSetupGetDestPositionY takes camerasetup whichSetup returns real
native CameraSetupApply takes camerasetup whichSetup,boolean doPan,boolean panTimed returns nothing
native CameraSetupApplyWithZ takes camerasetup whichSetup,real zDestOffset returns nothing
native CameraSetupApplyForceDuration takes camerasetup whichSetup,boolean doPan,real forceDuration returns nothing
native CameraSetupApplyForceDurationWithZ takes camerasetup whichSetup,real zDestOffset,real forceDuration returns nothing
native CameraSetTargetNoise takes real mag,real velocity returns nothing
native CameraSetSourceNoise takes real mag,real velocity returns nothing
native CameraSetTargetNoiseEx takes real mag,real velocity,boolean vertOnly returns nothing
native CameraSetSourceNoiseEx takes real mag,real velocity,boolean vertOnly returns nothing
native CameraSetSmoothingFactor takes real factor returns nothing
native SetCineFilterTexture takes string filename returns nothing
native SetCineFilterBlendMode takes blendmode whichMode returns nothing
native SetCineFilterTexMapFlags takes texmapflags whichFlags returns nothing
native SetCineFilterStartUV takes real minu,real minv,real maxu,real maxv returns nothing
native SetCineFilterEndUV takes real minu,real minv,real maxu,real maxv returns nothing
native SetCineFilterStartColor takes integer red,integer green,integer blue,integer alpha returns nothing
native SetCineFilterEndColor takes integer red,integer green,integer blue,integer alpha returns nothing
native SetCineFilterDuration takes real duration returns nothing
native DisplayCineFilter takes boolean flag returns nothing
native IsCineFilterDisplayed takes nothing returns boolean
native SetCinematicScene takes integer portraitUnitId,playercolor color,string speakerTitle,string text,real sceneDuration,real voiceoverDuration returns nothing
native EndCinematicScene takes nothing returns nothing
native ForceCinematicSubtitles takes boolean flag returns nothing
native GetCameraMargin takes integer whichMargin returns real
native GetCameraBoundMinX takes nothing returns real
native GetCameraBoundMinY takes nothing returns real
native GetCameraBoundMaxX takes nothing returns real
native GetCameraBoundMaxY takes nothing returns real
native GetCameraField takes camerafield whichField returns real
native GetCameraTargetPositionX takes nothing returns real
native GetCameraTargetPositionY takes nothing returns real
native GetCameraTargetPositionZ takes nothing returns real
native GetCameraTargetPositionLoc takes nothing returns location
native GetCameraEyePositionX takes nothing returns real
native GetCameraEyePositionY takes nothing returns real
native GetCameraEyePositionZ takes nothing returns real
native GetCameraEyePositionLoc takes nothing returns location
native NewSoundEnvironment takes string environmentName returns nothing
native CreateSound takes string fileName,boolean looping,boolean is3D,boolean stopwhenoutofrange,integer fadeInRate,integer fadeOutRate,string eaxSetting returns sound
native CreateSoundFilenameWithLabel takes string fileName,boolean looping,boolean is3D,boolean stopwhenoutofrange,integer fadeInRate,integer fadeOutRate,string SLKEntryName returns sound
native CreateSoundFromLabel takes string soundLabel,boolean looping,boolean is3D,boolean stopwhenoutofrange,integer fadeInRate,integer fadeOutRate returns sound
native CreateMIDISound takes string soundLabel,integer fadeInRate,integer fadeOutRate returns sound
native SetSoundParamsFromLabel takes sound soundHandle,string soundLabel returns nothing
native SetSoundDistanceCutoff takes sound soundHandle,real cutoff returns nothing
native SetSoundChannel takes sound soundHandle,integer channel returns nothing
native SetSoundVolume takes sound soundHandle,integer volume returns nothing
native SetSoundPitch takes sound soundHandle,real pitch returns nothing
native SetSoundPlayPosition takes sound soundHandle,integer millisecs returns nothing
native SetSoundDistances takes sound soundHandle,real minDist,real maxDist returns nothing
native SetSoundConeAngles takes sound soundHandle,real inside,real outside,integer outsideVolume returns nothing
native SetSoundConeOrientation takes sound soundHandle,real x,real y,real z returns nothing
native SetSoundPosition takes sound soundHandle,real x,real y,real z returns nothing
native SetSoundVelocity takes sound soundHandle,real x,real y,real z returns nothing
native AttachSoundToUnit takes sound soundHandle,unit whichUnit returns nothing
native StartSound takes sound soundHandle returns nothing
native StopSound takes sound soundHandle,boolean killWhenDone,boolean fadeOut returns nothing
native KillSoundWhenDone takes sound soundHandle returns nothing
native SetMapMusic takes string musicName,boolean random,integer index returns nothing
native ClearMapMusic takes nothing returns nothing
native PlayMusic takes string musicName returns nothing
native PlayMusicEx takes string musicName,integer frommsecs,integer fadeinmsecs returns nothing
native StopMusic takes boolean fadeOut returns nothing
native ResumeMusic takes nothing returns nothing
native PlayThematicMusic takes string musicFileName returns nothing
native PlayThematicMusicEx takes string musicFileName,integer frommsecs returns nothing
native EndThematicMusic takes nothing returns nothing
native SetMusicVolume takes integer volume returns nothing
native SetMusicPlayPosition takes integer millisecs returns nothing
native SetThematicMusicPlayPosition takes integer millisecs returns nothing
native SetSoundDuration takes sound soundHandle,integer duration returns nothing
native GetSoundDuration takes sound soundHandle returns integer
native GetSoundFileDuration takes string musicFileName returns integer
native VolumeGroupSetVolume takes volumegroup vgroup,real scale returns nothing
native VolumeGroupReset takes nothing returns nothing
native GetSoundIsPlaying takes sound soundHandle returns boolean
native GetSoundIsLoading takes sound soundHandle returns boolean
native RegisterStackedSound takes sound soundHandle,boolean byPosition,real rectwidth,real rectheight returns nothing
native UnregisterStackedSound takes sound soundHandle,boolean byPosition,real rectwidth,real rectheight returns nothing
native AddWeatherEffect takes rect where,integer effectID returns weathereffect
native RemoveWeatherEffect takes weathereffect whichEffect returns nothing
native EnableWeatherEffect takes weathereffect whichEffect,boolean enable returns nothing
native TerrainDeformCrater takes real x,real y,real radius,real depth,integer duration,boolean permanent returns terraindeformation
native TerrainDeformRipple takes real x,real y,real radius,real depth,integer duration,integer count,real spaceWaves,real timeWaves,real radiusStartPct,boolean limitNeg returns terraindeformation
native TerrainDeformWave takes real x,real y,real dirX,real dirY,real distance,real speed,real radius,real depth,integer trailTime,integer count returns terraindeformation
native TerrainDeformRandom takes real x,real y,real radius,real minDelta,real maxDelta,integer duration,integer updateInterval returns terraindeformation
native TerrainDeformStop takes terraindeformation deformation,integer duration returns nothing
native TerrainDeformStopAll takes nothing returns nothing
native AddSpecialEffect takes string modelName,real x,real y returns effect
native AddSpecialEffectLoc takes string modelName,location where returns effect
native AddSpecialEffectTarget takes string modelName,widget targetWidget,string attachPointName returns effect
native DestroyEffect takes effect whichEffect returns nothing
native AddSpellEffect takes string abilityString,effecttype t,real x,real y returns effect
native AddSpellEffectLoc takes string abilityString,effecttype t,location where returns effect
native AddSpellEffectById takes integer abilityId,effecttype t,real x,real y returns effect
native AddSpellEffectByIdLoc takes integer abilityId,effecttype t,location where returns effect
native AddSpellEffectTarget takes string modelName,effecttype t,widget targetWidget,string attachPoint returns effect
native AddSpellEffectTargetById takes integer abilityId,effecttype t,widget targetWidget,string attachPoint returns effect
native AddLightning takes string codeName,boolean checkVisibility,real x1,real y1,real x2,real y2 returns lightning
native AddLightningEx takes string codeName,boolean checkVisibility,real x1,real y1,real z1,real x2,real y2,real z2 returns lightning
native DestroyLightning takes lightning whichBolt returns boolean
native MoveLightning takes lightning whichBolt,boolean checkVisibility,real x1,real y1,real x2,real y2 returns boolean
native MoveLightningEx takes lightning whichBolt,boolean checkVisibility,real x1,real y1,real z1,real x2,real y2,real z2 returns boolean
native GetLightningColorA takes lightning whichBolt returns real
native GetLightningColorR takes lightning whichBolt returns real
native GetLightningColorG takes lightning whichBolt returns real
native GetLightningColorB takes lightning whichBolt returns real
native SetLightningColor takes lightning whichBolt,real r,real g,real b,real a returns boolean
native GetAbilityEffect takes string abilityString,effecttype t,integer index returns string
native GetAbilityEffectById takes integer abilityId,effecttype t,integer index returns string
native GetAbilitySound takes string abilityString,soundtype t returns string
native GetAbilitySoundById takes integer abilityId,soundtype t returns string
native GetTerrainCliffLevel takes real x,real y returns integer
native SetWaterBaseColor takes integer red,integer green,integer blue,integer alpha returns nothing
native SetWaterDeforms takes boolean val returns nothing
native GetTerrainType takes real x,real y returns integer
native GetTerrainVariance takes real x,real y returns integer
native SetTerrainType takes real x,real y,integer terrainType,integer variation,integer area,integer shape returns nothing
native IsTerrainPathable takes real x,real y,pathingtype t returns boolean
native SetTerrainPathable takes real x,real y,pathingtype t,boolean flag returns nothing
native CreateImage takes string file,real sizeX,real sizeY,real sizeZ,real posX,real posY,real posZ,real originX,real originY,real originZ,integer imageType returns image
native DestroyImage takes image whichImage returns nothing
native ShowImage takes image whichImage,boolean flag returns nothing
native SetImageConstantHeight takes image whichImage,boolean flag,real height returns nothing
native SetImagePosition takes image whichImage,real x,real y,real z returns nothing
native SetImageColor takes image whichImage,integer red,integer green,integer blue,integer alpha returns nothing
native SetImageRender takes image whichImage,boolean flag returns nothing
native SetImageRenderAlways takes image whichImage,boolean flag returns nothing
native SetImageAboveWater takes image whichImage,boolean flag,boolean useWaterAlpha returns nothing
native SetImageType takes image whichImage,integer imageType returns nothing
native CreateUbersplat takes real x,real y,string name,integer red,integer green,integer blue,integer alpha,boolean forcePaused,boolean noBirthTime returns ubersplat
native DestroyUbersplat takes ubersplat whichSplat returns nothing
native ResetUbersplat takes ubersplat whichSplat returns nothing
native FinishUbersplat takes ubersplat whichSplat returns nothing
native ShowUbersplat takes ubersplat whichSplat,boolean flag returns nothing
native SetUbersplatRender takes ubersplat whichSplat,boolean flag returns nothing
native SetUbersplatRenderAlways takes ubersplat whichSplat,boolean flag returns nothing
native SetBlight takes player whichPlayer,real x,real y,real radius,boolean addBlight returns nothing
native SetBlightRect takes player whichPlayer,rect r,boolean addBlight returns nothing
native SetBlightPoint takes player whichPlayer,real x,real y,boolean addBlight returns nothing
native SetBlightLoc takes player whichPlayer,location whichLocation,real radius,boolean addBlight returns nothing
native CreateBlightedGoldmine takes player id,real x,real y,real face returns unit
native IsPointBlighted takes real x,real y returns boolean
native SetDoodadAnimation takes real x,real y,real radius,integer doodadID,boolean nearestOnly,string animName,boolean animRandom returns nothing
native SetDoodadAnimationRect takes rect r,integer doodadID,string animName,boolean animRandom returns nothing
native StartMeleeAI takes player num,string script returns nothing
native StartCampaignAI takes player num,string script returns nothing
native CommandAI takes player num,integer command,integer data returns nothing
native PauseCompAI takes player p,boolean pause returns nothing
native GetAIDifficulty takes player num returns aidifficulty
native RemoveGuardPosition takes unit hUnit returns nothing
native RecycleGuardPosition takes unit hUnit returns nothing
native RemoveAllGuardPositions takes player num returns nothing
native Cheat takes string cheatStr returns nothing
native IsNoVictoryCheat takes nothing returns boolean
native IsNoDefeatCheat takes nothing returns boolean
native Preload takes string filename returns nothing
native PreloadEnd takes real timeout returns nothing
native PreloadStart takes nothing returns nothing
native PreloadRefresh takes nothing returns nothing
native PreloadEndEx takes nothing returns nothing
native PreloadGenClear takes nothing returns nothing
native PreloadGenStart takes nothing returns nothing
native PreloadGenEnd takes string filename returns nothing
native Preloader takes string filename returns nothing
