function agentFromIndex( int index ) returns agent
	data.saveFogState(0,ConvertFogState(index))
	return data.loadAgent(0)

function agentToIndex(agent object ) returns int
	return object.getHandleId()

function eventFromIndex( int index ) returns event
	data.saveFogState(0,ConvertFogState(index))
	return data.loadEvent(0)

function eventToIndex(event object ) returns int
	return object.getHandleId()

function playerFromIndex( int index ) returns player
	data.saveFogState(0,ConvertFogState(index))
	return data.loadPlayer(0)

function playerToIndex(player object ) returns int
	return object.getHandleId()

function widgetFromIndex( int index ) returns widget
	data.saveFogState(0,ConvertFogState(index))
	return data.loadWidget(0)

function widgetToIndex(widget object ) returns int
	return object.getHandleId()

function unitFromIndex( int index ) returns unit
	data.saveFogState(0,ConvertFogState(index))
	return data.loadUnit(0)

function unitToIndex(unit object ) returns int
	return object.getHandleId()

function destructableFromIndex( int index ) returns destructable
	data.saveFogState(0,ConvertFogState(index))
	return data.loadDestructable(0)

function destructableToIndex(destructable object ) returns int
	return object.getHandleId()

function itemFromIndex( int index ) returns item
	data.saveFogState(0,ConvertFogState(index))
	return data.loadItem(0)

function itemToIndex(item object ) returns int
	return object.getHandleId()

function abilityFromIndex( int index ) returns ability
	data.saveFogState(0,ConvertFogState(index))
	return data.loadAbility(0)

function abilityToIndex(ability object ) returns int
	return object.getHandleId()

function buffFromIndex( int index ) returns buff
	data.saveFogState(0,ConvertFogState(index))
	return data.loadBuff(0)

function buffToIndex(buff object ) returns int
	return object.getHandleId()

function forceFromIndex( int index ) returns force
	data.saveFogState(0,ConvertFogState(index))
	return data.loadForce(0)

function forceToIndex(force object ) returns int
	return object.getHandleId()

function groupFromIndex( int index ) returns group
	data.saveFogState(0,ConvertFogState(index))
	return data.loadGroup(0)

function groupToIndex(group object ) returns int
	return object.getHandleId()

function triggerFromIndex( int index ) returns trigger
	data.saveFogState(0,ConvertFogState(index))
	return data.loadTrigger(0)

function triggerToIndex(trigger object ) returns int
	return object.getHandleId()

function triggerconditionFromIndex( int index ) returns triggercondition
	data.saveFogState(0,ConvertFogState(index))
	return data.loadTriggercondition(0)

function triggerconditionToIndex(triggercondition object ) returns int
	return object.getHandleId()

function triggeractionFromIndex( int index ) returns triggeraction
	data.saveFogState(0,ConvertFogState(index))
	return data.loadTriggeraction(0)

function triggeractionToIndex(triggeraction object ) returns int
	return object.getHandleId()

function timerFromIndex( int index ) returns timer
	data.saveFogState(0,ConvertFogState(index))
	return data.loadTimer(0)

function timerToIndex(timer object ) returns int
	return object.getHandleId()

function locationFromIndex( int index ) returns location
	data.saveFogState(0,ConvertFogState(index))
	return data.loadLocation(0)

function locationToIndex(location object ) returns int
	return object.getHandleId()

function regionFromIndex( int index ) returns region
	data.saveFogState(0,ConvertFogState(index))
	return data.loadRegion(0)

function regionToIndex(region object ) returns int
	return object.getHandleId()

function rectFromIndex( int index ) returns rect
	data.saveFogState(0,ConvertFogState(index))
	return data.loadRect(0)

function rectToIndex(rect object ) returns int
	return object.getHandleId()

function boolexprFromIndex( int index ) returns boolexpr
	data.saveFogState(0,ConvertFogState(index))
	return data.loadBoolexpr(0)

function boolexprToIndex(boolexpr object ) returns int
	return object.getHandleId()

function soundFromIndex( int index ) returns sound
	data.saveFogState(0,ConvertFogState(index))
	return data.loadSound(0)

function soundToIndex(sound object ) returns int
	return object.getHandleId()

function conditionfuncFromIndex( int index ) returns conditionfunc
	data.saveFogState(0,ConvertFogState(index))
	return data.loadConditionfunc(0)

function conditionfuncToIndex(conditionfunc object ) returns int
	return object.getHandleId()

function filterfuncFromIndex( int index ) returns filterfunc
	data.saveFogState(0,ConvertFogState(index))
	return data.loadFilterfunc(0)

function filterfuncToIndex(filterfunc object ) returns int
	return object.getHandleId()

function unitpoolFromIndex( int index ) returns unitpool
	data.saveFogState(0,ConvertFogState(index))
	return data.loadUnitpool(0)

function unitpoolToIndex(unitpool object ) returns int
	return object.getHandleId()

function itempoolFromIndex( int index ) returns itempool
	data.saveFogState(0,ConvertFogState(index))
	return data.loadItempool(0)

function itempoolToIndex(itempool object ) returns int
	return object.getHandleId()

function raceFromIndex( int index ) returns race
	data.saveFogState(0,ConvertFogState(index))
	return data.loadRace(0)

function raceToIndex(race object ) returns int
	return object.getHandleId()

function alliancetypeFromIndex( int index ) returns alliancetype
	data.saveFogState(0,ConvertFogState(index))
	return data.loadAlliancetype(0)

function alliancetypeToIndex(alliancetype object ) returns int
	return object.getHandleId()

function racepreferenceFromIndex( int index ) returns racepreference
	data.saveFogState(0,ConvertFogState(index))
	return data.loadRacepreference(0)

function racepreferenceToIndex(racepreference object ) returns int
	return object.getHandleId()

function gamestateFromIndex( int index ) returns gamestate
	data.saveFogState(0,ConvertFogState(index))
	return data.loadGamestate(0)

function gamestateToIndex(gamestate object ) returns int
	return object.getHandleId()

function igamestateFromIndex( int index ) returns igamestate
	data.saveFogState(0,ConvertFogState(index))
	return data.loadIgamestate(0)

function igamestateToIndex(igamestate object ) returns int
	return object.getHandleId()

function fgamestateFromIndex( int index ) returns fgamestate
	data.saveFogState(0,ConvertFogState(index))
	return data.loadFgamestate(0)

function fgamestateToIndex(fgamestate object ) returns int
	return object.getHandleId()

function playerstateFromIndex( int index ) returns playerstate
	data.saveFogState(0,ConvertFogState(index))
	return data.loadPlayerstate(0)

function playerstateToIndex(playerstate object ) returns int
	return object.getHandleId()

function playerscoreFromIndex( int index ) returns playerscore
	data.saveFogState(0,ConvertFogState(index))
	return data.loadPlayerscore(0)

function playerscoreToIndex(playerscore object ) returns int
	return object.getHandleId()

function playergameresultFromIndex( int index ) returns playergameresult
	data.saveFogState(0,ConvertFogState(index))
	return data.loadPlayergameresult(0)

function playergameresultToIndex(playergameresult object ) returns int
	return object.getHandleId()

function unitstateFromIndex( int index ) returns unitstate
	data.saveFogState(0,ConvertFogState(index))
	return data.loadUnitstate(0)

function unitstateToIndex(unitstate object ) returns int
	return object.getHandleId()

function aidifficultyFromIndex( int index ) returns aidifficulty
	data.saveFogState(0,ConvertFogState(index))
	return data.loadAidifficulty(0)

function aidifficultyToIndex(aidifficulty object ) returns int
	return object.getHandleId()

function eventidFromIndex( int index ) returns eventid
	data.saveFogState(0,ConvertFogState(index))
	return data.loadEventid(0)

function eventidToIndex(eventid object ) returns int
	return object.getHandleId()

function gameeventFromIndex( int index ) returns gameevent
	data.saveFogState(0,ConvertFogState(index))
	return data.loadGameevent(0)

function gameeventToIndex(gameevent object ) returns int
	return object.getHandleId()

function playereventFromIndex( int index ) returns playerevent
	data.saveFogState(0,ConvertFogState(index))
	return data.loadPlayerevent(0)

function playereventToIndex(playerevent object ) returns int
	return object.getHandleId()

function playeruniteventFromIndex( int index ) returns playerunitevent
	data.saveFogState(0,ConvertFogState(index))
	return data.loadPlayerunitevent(0)

function playeruniteventToIndex(playerunitevent object ) returns int
	return object.getHandleId()

function uniteventFromIndex( int index ) returns unitevent
	data.saveFogState(0,ConvertFogState(index))
	return data.loadUnitevent(0)

function uniteventToIndex(unitevent object ) returns int
	return object.getHandleId()

function limitopFromIndex( int index ) returns limitop
	data.saveFogState(0,ConvertFogState(index))
	return data.loadLimitop(0)

function limitopToIndex(limitop object ) returns int
	return object.getHandleId()

function widgeteventFromIndex( int index ) returns widgetevent
	data.saveFogState(0,ConvertFogState(index))
	return data.loadWidgetevent(0)

function widgeteventToIndex(widgetevent object ) returns int
	return object.getHandleId()

function dialogeventFromIndex( int index ) returns dialogevent
	data.saveFogState(0,ConvertFogState(index))
	return data.loadDialogevent(0)

function dialogeventToIndex(dialogevent object ) returns int
	return object.getHandleId()

function unittypeFromIndex( int index ) returns unittype
	data.saveFogState(0,ConvertFogState(index))
	return data.loadUnittype(0)

function unittypeToIndex(unittype object ) returns int
	return object.getHandleId()

function gamespeedFromIndex( int index ) returns gamespeed
	data.saveFogState(0,ConvertFogState(index))
	return data.loadGamespeed(0)

function gamespeedToIndex(gamespeed object ) returns int
	return object.getHandleId()

function gamedifficultyFromIndex( int index ) returns gamedifficulty
	data.saveFogState(0,ConvertFogState(index))
	return data.loadGamedifficulty(0)

function gamedifficultyToIndex(gamedifficulty object ) returns int
	return object.getHandleId()

function gametypeFromIndex( int index ) returns gametype
	data.saveFogState(0,ConvertFogState(index))
	return data.loadGametype(0)

function gametypeToIndex(gametype object ) returns int
	return object.getHandleId()

function mapflagFromIndex( int index ) returns mapflag
	data.saveFogState(0,ConvertFogState(index))
	return data.loadMapflag(0)

function mapflagToIndex(mapflag object ) returns int
	return object.getHandleId()

function mapvisibilityFromIndex( int index ) returns mapvisibility
	data.saveFogState(0,ConvertFogState(index))
	return data.loadMapvisibility(0)

function mapvisibilityToIndex(mapvisibility object ) returns int
	return object.getHandleId()

function mapsettingFromIndex( int index ) returns mapsetting
	data.saveFogState(0,ConvertFogState(index))
	return data.loadMapsetting(0)

function mapsettingToIndex(mapsetting object ) returns int
	return object.getHandleId()

function mapdensityFromIndex( int index ) returns mapdensity
	data.saveFogState(0,ConvertFogState(index))
	return data.loadMapdensity(0)

function mapdensityToIndex(mapdensity object ) returns int
	return object.getHandleId()

function mapcontrolFromIndex( int index ) returns mapcontrol
	data.saveFogState(0,ConvertFogState(index))
	return data.loadMapcontrol(0)

function mapcontrolToIndex(mapcontrol object ) returns int
	return object.getHandleId()

function playerslotstateFromIndex( int index ) returns playerslotstate
	data.saveFogState(0,ConvertFogState(index))
	return data.loadPlayerslotstate(0)

function playerslotstateToIndex(playerslotstate object ) returns int
	return object.getHandleId()

function volumegroupFromIndex( int index ) returns volumegroup
	data.saveFogState(0,ConvertFogState(index))
	return data.loadVolumegroup(0)

function volumegroupToIndex(volumegroup object ) returns int
	return object.getHandleId()

function camerafieldFromIndex( int index ) returns camerafield
	data.saveFogState(0,ConvertFogState(index))
	return data.loadCamerafield(0)

function camerafieldToIndex(camerafield object ) returns int
	return object.getHandleId()

function camerasetupFromIndex( int index ) returns camerasetup
	data.saveFogState(0,ConvertFogState(index))
	return data.loadCamerasetup(0)

function camerasetupToIndex(camerasetup object ) returns int
	return object.getHandleId()

function playercolorFromIndex( int index ) returns playercolor
	data.saveFogState(0,ConvertFogState(index))
	return data.loadPlayercolor(0)

function playercolorToIndex(playercolor object ) returns int
	return object.getHandleId()

function placementFromIndex( int index ) returns placement
	data.saveFogState(0,ConvertFogState(index))
	return data.loadPlacement(0)

function placementToIndex(placement object ) returns int
	return object.getHandleId()

function startlocprioFromIndex( int index ) returns startlocprio
	data.saveFogState(0,ConvertFogState(index))
	return data.loadStartlocprio(0)

function startlocprioToIndex(startlocprio object ) returns int
	return object.getHandleId()

function raritycontrolFromIndex( int index ) returns raritycontrol
	data.saveFogState(0,ConvertFogState(index))
	return data.loadRaritycontrol(0)

function raritycontrolToIndex(raritycontrol object ) returns int
	return object.getHandleId()

function blendmodeFromIndex( int index ) returns blendmode
	data.saveFogState(0,ConvertFogState(index))
	return data.loadBlendmode(0)

function blendmodeToIndex(blendmode object ) returns int
	return object.getHandleId()

function texmapflagsFromIndex( int index ) returns texmapflags
	data.saveFogState(0,ConvertFogState(index))
	return data.loadTexmapflags(0)

function texmapflagsToIndex(texmapflags object ) returns int
	return object.getHandleId()

function effectFromIndex( int index ) returns effect
	data.saveFogState(0,ConvertFogState(index))
	return data.loadEffect(0)

function effectToIndex(effect object ) returns int
	return object.getHandleId()

function effecttypeFromIndex( int index ) returns effecttype
	data.saveFogState(0,ConvertFogState(index))
	return data.loadEffecttype(0)

function effecttypeToIndex(effecttype object ) returns int
	return object.getHandleId()

function weathereffectFromIndex( int index ) returns weathereffect
	data.saveFogState(0,ConvertFogState(index))
	return data.loadWeathereffect(0)

function weathereffectToIndex(weathereffect object ) returns int
	return object.getHandleId()

function terraindeformationFromIndex( int index ) returns terraindeformation
	data.saveFogState(0,ConvertFogState(index))
	return data.loadTerraindeformation(0)

function terraindeformationToIndex(terraindeformation object ) returns int
	return object.getHandleId()

function fogstateFromIndex( int index ) returns fogstate
	data.saveFogState(0,ConvertFogState(index))
	return data.loadFogstate(0)

function fogstateToIndex(fogstate object ) returns int
	return object.getHandleId()

function fogmodifierFromIndex( int index ) returns fogmodifier
	data.saveFogState(0,ConvertFogState(index))
	return data.loadFogmodifier(0)

function fogmodifierToIndex(fogmodifier object ) returns int
	return object.getHandleId()

function dialogFromIndex( int index ) returns dialog
	data.saveFogState(0,ConvertFogState(index))
	return data.loadDialog(0)

function dialogToIndex(dialog object ) returns int
	return object.getHandleId()

function buttonFromIndex( int index ) returns button
	data.saveFogState(0,ConvertFogState(index))
	return data.loadButton(0)

function buttonToIndex(button object ) returns int
	return object.getHandleId()

function questFromIndex( int index ) returns quest
	data.saveFogState(0,ConvertFogState(index))
	return data.loadQuest(0)

function questToIndex(quest object ) returns int
	return object.getHandleId()

function questitemFromIndex( int index ) returns questitem
	data.saveFogState(0,ConvertFogState(index))
	return data.loadQuestitem(0)

function questitemToIndex(questitem object ) returns int
	return object.getHandleId()

function defeatconditionFromIndex( int index ) returns defeatcondition
	data.saveFogState(0,ConvertFogState(index))
	return data.loadDefeatcondition(0)

function defeatconditionToIndex(defeatcondition object ) returns int
	return object.getHandleId()

function timerdialogFromIndex( int index ) returns timerdialog
	data.saveFogState(0,ConvertFogState(index))
	return data.loadTimerdialog(0)

function timerdialogToIndex(timerdialog object ) returns int
	return object.getHandleId()

function leaderboardFromIndex( int index ) returns leaderboard
	data.saveFogState(0,ConvertFogState(index))
	return data.loadLeaderboard(0)

function leaderboardToIndex(leaderboard object ) returns int
	return object.getHandleId()

function multiboardFromIndex( int index ) returns multiboard
	data.saveFogState(0,ConvertFogState(index))
	return data.loadMultiboard(0)

function multiboardToIndex(multiboard object ) returns int
	return object.getHandleId()

function multiboarditemFromIndex( int index ) returns multiboarditem
	data.saveFogState(0,ConvertFogState(index))
	return data.loadMultiboarditem(0)

function multiboarditemToIndex(multiboarditem object ) returns int
	return object.getHandleId()

function trackableFromIndex( int index ) returns trackable
	data.saveFogState(0,ConvertFogState(index))
	return data.loadTrackable(0)

function trackableToIndex(trackable object ) returns int
	return object.getHandleId()

function gamecacheFromIndex( int index ) returns gamecache
	data.saveFogState(0,ConvertFogState(index))
	return data.loadGamecache(0)

function gamecacheToIndex(gamecache object ) returns int
	return object.getHandleId()

function versionFromIndex( int index ) returns version
	data.saveFogState(0,ConvertFogState(index))
	return data.loadVersion(0)

function versionToIndex(version object ) returns int
	return object.getHandleId()

function itemtypeFromIndex( int index ) returns itemtype
	data.saveFogState(0,ConvertFogState(index))
	return data.loadItemtype(0)

function itemtypeToIndex(itemtype object ) returns int
	return object.getHandleId()

function texttagFromIndex( int index ) returns texttag
	data.saveFogState(0,ConvertFogState(index))
	return data.loadTexttag(0)

function texttagToIndex(texttag object ) returns int
	return object.getHandleId()

function attacktypeFromIndex( int index ) returns attacktype
	data.saveFogState(0,ConvertFogState(index))
	return data.loadAttacktype(0)

function attacktypeToIndex(attacktype object ) returns int
	return object.getHandleId()

function damagetypeFromIndex( int index ) returns damagetype
	data.saveFogState(0,ConvertFogState(index))
	return data.loadDamagetype(0)

function damagetypeToIndex(damagetype object ) returns int
	return object.getHandleId()

function weapontypeFromIndex( int index ) returns weapontype
	data.saveFogState(0,ConvertFogState(index))
	return data.loadWeapontype(0)

function weapontypeToIndex(weapontype object ) returns int
	return object.getHandleId()

function soundtypeFromIndex( int index ) returns soundtype
	data.saveFogState(0,ConvertFogState(index))
	return data.loadSoundtype(0)

function soundtypeToIndex(soundtype object ) returns int
	return object.getHandleId()

function lightningFromIndex( int index ) returns lightning
	data.saveFogState(0,ConvertFogState(index))
	return data.loadLightning(0)

function lightningToIndex(lightning object ) returns int
	return object.getHandleId()

function pathingtypeFromIndex( int index ) returns pathingtype
	data.saveFogState(0,ConvertFogState(index))
	return data.loadPathingtype(0)

function pathingtypeToIndex(pathingtype object ) returns int
	return object.getHandleId()

function imageFromIndex( int index ) returns image
	data.saveFogState(0,ConvertFogState(index))
	return data.loadImage(0)

function imageToIndex(image object ) returns int
	return object.getHandleId()

function ubersplatFromIndex( int index ) returns ubersplat
	data.saveFogState(0,ConvertFogState(index))
	return data.loadUbersplat(0)

function ubersplatToIndex(ubersplat object ) returns int
	return object.getHandleId()

function hashtableFromIndex( int index ) returns hashtable
	data.saveFogState(0,ConvertFogState(index))
	return data.loadHashtable(0)

function hashtableToIndex(hashtable object ) returns int
	return object.getHandleId()

