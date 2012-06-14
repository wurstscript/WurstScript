function loadAgent( int parentKey ) returns agent
    return ht.loadAgentHandle( this castTo int, parentKey )

function saveAgent( int parentKey, agent value )
    ht.saveAgentHandle( this castTo int, parentKey, value )

function loadEvent( int parentKey ) returns event
    return ht.loadEventHandle( this castTo int, parentKey )

function saveEvent( int parentKey, event value )
    ht.saveEventHandle( this castTo int, parentKey, value )

function loadPlayer( int parentKey ) returns player
    return ht.loadPlayerHandle( this castTo int, parentKey )

function savePlayer( int parentKey, player value )
    ht.savePlayerHandle( this castTo int, parentKey, value )

function loadWidget( int parentKey ) returns widget
    return ht.loadWidgetHandle( this castTo int, parentKey )

function saveWidget( int parentKey, widget value )
    ht.saveWidgetHandle( this castTo int, parentKey, value )

function loadUnit( int parentKey ) returns unit
    return ht.loadUnitHandle( this castTo int, parentKey )

function saveUnit( int parentKey, unit value )
    ht.saveUnitHandle( this castTo int, parentKey, value )

function loadDestructable( int parentKey ) returns destructable
    return ht.loadDestructableHandle( this castTo int, parentKey )

function saveDestructable( int parentKey, destructable value )
    ht.saveDestructableHandle( this castTo int, parentKey, value )

function loadItem( int parentKey ) returns item
    return ht.loadItemHandle( this castTo int, parentKey )

function saveItem( int parentKey, item value )
    ht.saveItemHandle( this castTo int, parentKey, value )

function loadAbility( int parentKey ) returns ability
    return ht.loadAbilityHandle( this castTo int, parentKey )

function saveAbility( int parentKey, ability value )
    ht.saveAbilityHandle( this castTo int, parentKey, value )

function loadBuff( int parentKey ) returns buff
    return ht.loadBuffHandle( this castTo int, parentKey )

function saveBuff( int parentKey, buff value )
    ht.saveBuffHandle( this castTo int, parentKey, value )

function loadForce( int parentKey ) returns force
    return ht.loadForceHandle( this castTo int, parentKey )

function saveForce( int parentKey, force value )
    ht.saveForceHandle( this castTo int, parentKey, value )

function loadGroup( int parentKey ) returns group
    return ht.loadGroupHandle( this castTo int, parentKey )

function saveGroup( int parentKey, group value )
    ht.saveGroupHandle( this castTo int, parentKey, value )

function loadTrigger( int parentKey ) returns trigger
    return ht.loadTriggerHandle( this castTo int, parentKey )

function saveTrigger( int parentKey, trigger value )
    ht.saveTriggerHandle( this castTo int, parentKey, value )

function loadTriggercondition( int parentKey ) returns triggercondition
    return ht.loadTriggerconditionHandle( this castTo int, parentKey )

function saveTriggercondition( int parentKey, triggercondition value )
    ht.saveTriggerconditionHandle( this castTo int, parentKey, value )

function loadTriggeraction( int parentKey ) returns triggeraction
    return ht.loadTriggeractionHandle( this castTo int, parentKey )

function saveTriggeraction( int parentKey, triggeraction value )
    ht.saveTriggeractionHandle( this castTo int, parentKey, value )

function loadTimer( int parentKey ) returns timer
    return ht.loadTimerHandle( this castTo int, parentKey )

function saveTimer( int parentKey, timer value )
    ht.saveTimerHandle( this castTo int, parentKey, value )

function loadLocation( int parentKey ) returns location
    return ht.loadLocationHandle( this castTo int, parentKey )

function saveLocation( int parentKey, location value )
    ht.saveLocationHandle( this castTo int, parentKey, value )

function loadRegion( int parentKey ) returns region
    return ht.loadRegionHandle( this castTo int, parentKey )

function saveRegion( int parentKey, region value )
    ht.saveRegionHandle( this castTo int, parentKey, value )

function loadRect( int parentKey ) returns rect
    return ht.loadRectHandle( this castTo int, parentKey )

function saveRect( int parentKey, rect value )
    ht.saveRectHandle( this castTo int, parentKey, value )

function loadBoolexpr( int parentKey ) returns boolexpr
    return ht.loadBoolexprHandle( this castTo int, parentKey )

function saveBoolexpr( int parentKey, boolexpr value )
    ht.saveBoolexprHandle( this castTo int, parentKey, value )

function loadSound( int parentKey ) returns sound
    return ht.loadSoundHandle( this castTo int, parentKey )

function saveSound( int parentKey, sound value )
    ht.saveSoundHandle( this castTo int, parentKey, value )

function loadConditionfunc( int parentKey ) returns conditionfunc
    return ht.loadConditionfuncHandle( this castTo int, parentKey )

function saveConditionfunc( int parentKey, conditionfunc value )
    ht.saveConditionfuncHandle( this castTo int, parentKey, value )

function loadFilterfunc( int parentKey ) returns filterfunc
    return ht.loadFilterfuncHandle( this castTo int, parentKey )

function saveFilterfunc( int parentKey, filterfunc value )
    ht.saveFilterfuncHandle( this castTo int, parentKey, value )

function loadUnitpool( int parentKey ) returns unitpool
    return ht.loadUnitpoolHandle( this castTo int, parentKey )

function saveUnitpool( int parentKey, unitpool value )
    ht.saveUnitpoolHandle( this castTo int, parentKey, value )

function loadItempool( int parentKey ) returns itempool
    return ht.loadItempoolHandle( this castTo int, parentKey )

function saveItempool( int parentKey, itempool value )
    ht.saveItempoolHandle( this castTo int, parentKey, value )

function loadRace( int parentKey ) returns race
    return ht.loadRaceHandle( this castTo int, parentKey )

function saveRace( int parentKey, race value )
    ht.saveRaceHandle( this castTo int, parentKey, value )

function loadAlliancetype( int parentKey ) returns alliancetype
    return ht.loadAlliancetypeHandle( this castTo int, parentKey )

function saveAlliancetype( int parentKey, alliancetype value )
    ht.saveAlliancetypeHandle( this castTo int, parentKey, value )

function loadRacepreference( int parentKey ) returns racepreference
    return ht.loadRacepreferenceHandle( this castTo int, parentKey )

function saveRacepreference( int parentKey, racepreference value )
    ht.saveRacepreferenceHandle( this castTo int, parentKey, value )

function loadGamestate( int parentKey ) returns gamestate
    return ht.loadGamestateHandle( this castTo int, parentKey )

function saveGamestate( int parentKey, gamestate value )
    ht.saveGamestateHandle( this castTo int, parentKey, value )

function loadIgamestate( int parentKey ) returns igamestate
    return ht.loadIgamestateHandle( this castTo int, parentKey )

function saveIgamestate( int parentKey, igamestate value )
    ht.saveIgamestateHandle( this castTo int, parentKey, value )

function loadFgamestate( int parentKey ) returns fgamestate
    return ht.loadFgamestateHandle( this castTo int, parentKey )

function saveFgamestate( int parentKey, fgamestate value )
    ht.saveFgamestateHandle( this castTo int, parentKey, value )

function loadPlayerstate( int parentKey ) returns playerstate
    return ht.loadPlayerstateHandle( this castTo int, parentKey )

function savePlayerstate( int parentKey, playerstate value )
    ht.savePlayerstateHandle( this castTo int, parentKey, value )

function loadPlayerscore( int parentKey ) returns playerscore
    return ht.loadPlayerscoreHandle( this castTo int, parentKey )

function savePlayerscore( int parentKey, playerscore value )
    ht.savePlayerscoreHandle( this castTo int, parentKey, value )

function loadPlayergameresult( int parentKey ) returns playergameresult
    return ht.loadPlayergameresultHandle( this castTo int, parentKey )

function savePlayergameresult( int parentKey, playergameresult value )
    ht.savePlayergameresultHandle( this castTo int, parentKey, value )

function loadUnitstate( int parentKey ) returns unitstate
    return ht.loadUnitstateHandle( this castTo int, parentKey )

function saveUnitstate( int parentKey, unitstate value )
    ht.saveUnitstateHandle( this castTo int, parentKey, value )

function loadAidifficulty( int parentKey ) returns aidifficulty
    return ht.loadAidifficultyHandle( this castTo int, parentKey )

function saveAidifficulty( int parentKey, aidifficulty value )
    ht.saveAidifficultyHandle( this castTo int, parentKey, value )

function loadEventid( int parentKey ) returns eventid
    return ht.loadEventidHandle( this castTo int, parentKey )

function saveEventid( int parentKey, eventid value )
    ht.saveEventidHandle( this castTo int, parentKey, value )

function loadGameevent( int parentKey ) returns gameevent
    return ht.loadGameeventHandle( this castTo int, parentKey )

function saveGameevent( int parentKey, gameevent value )
    ht.saveGameeventHandle( this castTo int, parentKey, value )

function loadPlayerevent( int parentKey ) returns playerevent
    return ht.loadPlayereventHandle( this castTo int, parentKey )

function savePlayerevent( int parentKey, playerevent value )
    ht.savePlayereventHandle( this castTo int, parentKey, value )

function loadPlayerunitevent( int parentKey ) returns playerunitevent
    return ht.loadPlayeruniteventHandle( this castTo int, parentKey )

function savePlayerunitevent( int parentKey, playerunitevent value )
    ht.savePlayeruniteventHandle( this castTo int, parentKey, value )

function loadUnitevent( int parentKey ) returns unitevent
    return ht.loadUniteventHandle( this castTo int, parentKey )

function saveUnitevent( int parentKey, unitevent value )
    ht.saveUniteventHandle( this castTo int, parentKey, value )

function loadLimitop( int parentKey ) returns limitop
    return ht.loadLimitopHandle( this castTo int, parentKey )

function saveLimitop( int parentKey, limitop value )
    ht.saveLimitopHandle( this castTo int, parentKey, value )

function loadWidgetevent( int parentKey ) returns widgetevent
    return ht.loadWidgeteventHandle( this castTo int, parentKey )

function saveWidgetevent( int parentKey, widgetevent value )
    ht.saveWidgeteventHandle( this castTo int, parentKey, value )

function loadDialogevent( int parentKey ) returns dialogevent
    return ht.loadDialogeventHandle( this castTo int, parentKey )

function saveDialogevent( int parentKey, dialogevent value )
    ht.saveDialogeventHandle( this castTo int, parentKey, value )

function loadUnittype( int parentKey ) returns unittype
    return ht.loadUnittypeHandle( this castTo int, parentKey )

function saveUnittype( int parentKey, unittype value )
    ht.saveUnittypeHandle( this castTo int, parentKey, value )

function loadGamespeed( int parentKey ) returns gamespeed
    return ht.loadGamespeedHandle( this castTo int, parentKey )

function saveGamespeed( int parentKey, gamespeed value )
    ht.saveGamespeedHandle( this castTo int, parentKey, value )

function loadGamedifficulty( int parentKey ) returns gamedifficulty
    return ht.loadGamedifficultyHandle( this castTo int, parentKey )

function saveGamedifficulty( int parentKey, gamedifficulty value )
    ht.saveGamedifficultyHandle( this castTo int, parentKey, value )

function loadGametype( int parentKey ) returns gametype
    return ht.loadGametypeHandle( this castTo int, parentKey )

function saveGametype( int parentKey, gametype value )
    ht.saveGametypeHandle( this castTo int, parentKey, value )

function loadMapflag( int parentKey ) returns mapflag
    return ht.loadMapflagHandle( this castTo int, parentKey )

function saveMapflag( int parentKey, mapflag value )
    ht.saveMapflagHandle( this castTo int, parentKey, value )

function loadMapvisibility( int parentKey ) returns mapvisibility
    return ht.loadMapvisibilityHandle( this castTo int, parentKey )

function saveMapvisibility( int parentKey, mapvisibility value )
    ht.saveMapvisibilityHandle( this castTo int, parentKey, value )

function loadMapsetting( int parentKey ) returns mapsetting
    return ht.loadMapsettingHandle( this castTo int, parentKey )

function saveMapsetting( int parentKey, mapsetting value )
    ht.saveMapsettingHandle( this castTo int, parentKey, value )

function loadMapdensity( int parentKey ) returns mapdensity
    return ht.loadMapdensityHandle( this castTo int, parentKey )

function saveMapdensity( int parentKey, mapdensity value )
    ht.saveMapdensityHandle( this castTo int, parentKey, value )

function loadMapcontrol( int parentKey ) returns mapcontrol
    return ht.loadMapcontrolHandle( this castTo int, parentKey )

function saveMapcontrol( int parentKey, mapcontrol value )
    ht.saveMapcontrolHandle( this castTo int, parentKey, value )

function loadPlayerslotstate( int parentKey ) returns playerslotstate
    return ht.loadPlayerslotstateHandle( this castTo int, parentKey )

function savePlayerslotstate( int parentKey, playerslotstate value )
    ht.savePlayerslotstateHandle( this castTo int, parentKey, value )

function loadVolumegroup( int parentKey ) returns volumegroup
    return ht.loadVolumegroupHandle( this castTo int, parentKey )

function saveVolumegroup( int parentKey, volumegroup value )
    ht.saveVolumegroupHandle( this castTo int, parentKey, value )

function loadCamerafield( int parentKey ) returns camerafield
    return ht.loadCamerafieldHandle( this castTo int, parentKey )

function saveCamerafield( int parentKey, camerafield value )
    ht.saveCamerafieldHandle( this castTo int, parentKey, value )

function loadCamerasetup( int parentKey ) returns camerasetup
    return ht.loadCamerasetupHandle( this castTo int, parentKey )

function saveCamerasetup( int parentKey, camerasetup value )
    ht.saveCamerasetupHandle( this castTo int, parentKey, value )

function loadPlayercolor( int parentKey ) returns playercolor
    return ht.loadPlayercolorHandle( this castTo int, parentKey )

function savePlayercolor( int parentKey, playercolor value )
    ht.savePlayercolorHandle( this castTo int, parentKey, value )

function loadPlacement( int parentKey ) returns placement
    return ht.loadPlacementHandle( this castTo int, parentKey )

function savePlacement( int parentKey, placement value )
    ht.savePlacementHandle( this castTo int, parentKey, value )

function loadStartlocprio( int parentKey ) returns startlocprio
    return ht.loadStartlocprioHandle( this castTo int, parentKey )

function saveStartlocprio( int parentKey, startlocprio value )
    ht.saveStartlocprioHandle( this castTo int, parentKey, value )

function loadRaritycontrol( int parentKey ) returns raritycontrol
    return ht.loadRaritycontrolHandle( this castTo int, parentKey )

function saveRaritycontrol( int parentKey, raritycontrol value )
    ht.saveRaritycontrolHandle( this castTo int, parentKey, value )

function loadBlendmode( int parentKey ) returns blendmode
    return ht.loadBlendmodeHandle( this castTo int, parentKey )

function saveBlendmode( int parentKey, blendmode value )
    ht.saveBlendmodeHandle( this castTo int, parentKey, value )

function loadTexmapflags( int parentKey ) returns texmapflags
    return ht.loadTexmapflagsHandle( this castTo int, parentKey )

function saveTexmapflags( int parentKey, texmapflags value )
    ht.saveTexmapflagsHandle( this castTo int, parentKey, value )

function loadEffect( int parentKey ) returns effect
    return ht.loadEffectHandle( this castTo int, parentKey )

function saveEffect( int parentKey, effect value )
    ht.saveEffectHandle( this castTo int, parentKey, value )

function loadEffecttype( int parentKey ) returns effecttype
    return ht.loadEffecttypeHandle( this castTo int, parentKey )

function saveEffecttype( int parentKey, effecttype value )
    ht.saveEffecttypeHandle( this castTo int, parentKey, value )

function loadWeathereffect( int parentKey ) returns weathereffect
    return ht.loadWeathereffectHandle( this castTo int, parentKey )

function saveWeathereffect( int parentKey, weathereffect value )
    ht.saveWeathereffectHandle( this castTo int, parentKey, value )

function loadTerraindeformation( int parentKey ) returns terraindeformation
    return ht.loadTerraindeformationHandle( this castTo int, parentKey )

function saveTerraindeformation( int parentKey, terraindeformation value )
    ht.saveTerraindeformationHandle( this castTo int, parentKey, value )

function loadFogstate( int parentKey ) returns fogstate
    return ht.loadFogstateHandle( this castTo int, parentKey )

function saveFogstate( int parentKey, fogstate value )
    ht.saveFogstateHandle( this castTo int, parentKey, value )

function loadFogmodifier( int parentKey ) returns fogmodifier
    return ht.loadFogmodifierHandle( this castTo int, parentKey )

function saveFogmodifier( int parentKey, fogmodifier value )
    ht.saveFogmodifierHandle( this castTo int, parentKey, value )

function loadDialog( int parentKey ) returns dialog
    return ht.loadDialogHandle( this castTo int, parentKey )

function saveDialog( int parentKey, dialog value )
    ht.saveDialogHandle( this castTo int, parentKey, value )

function loadButton( int parentKey ) returns button
    return ht.loadButtonHandle( this castTo int, parentKey )

function saveButton( int parentKey, button value )
    ht.saveButtonHandle( this castTo int, parentKey, value )

function loadQuest( int parentKey ) returns quest
    return ht.loadQuestHandle( this castTo int, parentKey )

function saveQuest( int parentKey, quest value )
    ht.saveQuestHandle( this castTo int, parentKey, value )

function loadQuestitem( int parentKey ) returns questitem
    return ht.loadQuestitemHandle( this castTo int, parentKey )

function saveQuestitem( int parentKey, questitem value )
    ht.saveQuestitemHandle( this castTo int, parentKey, value )

function loadDefeatcondition( int parentKey ) returns defeatcondition
    return ht.loadDefeatconditionHandle( this castTo int, parentKey )

function saveDefeatcondition( int parentKey, defeatcondition value )
    ht.saveDefeatconditionHandle( this castTo int, parentKey, value )

function loadTimerdialog( int parentKey ) returns timerdialog
    return ht.loadTimerdialogHandle( this castTo int, parentKey )

function saveTimerdialog( int parentKey, timerdialog value )
    ht.saveTimerdialogHandle( this castTo int, parentKey, value )

function loadLeaderboard( int parentKey ) returns leaderboard
    return ht.loadLeaderboardHandle( this castTo int, parentKey )

function saveLeaderboard( int parentKey, leaderboard value )
    ht.saveLeaderboardHandle( this castTo int, parentKey, value )

function loadMultiboard( int parentKey ) returns multiboard
    return ht.loadMultiboardHandle( this castTo int, parentKey )

function saveMultiboard( int parentKey, multiboard value )
    ht.saveMultiboardHandle( this castTo int, parentKey, value )

function loadMultiboarditem( int parentKey ) returns multiboarditem
    return ht.loadMultiboarditemHandle( this castTo int, parentKey )

function saveMultiboarditem( int parentKey, multiboarditem value )
    ht.saveMultiboarditemHandle( this castTo int, parentKey, value )

function loadTrackable( int parentKey ) returns trackable
    return ht.loadTrackableHandle( this castTo int, parentKey )

function saveTrackable( int parentKey, trackable value )
    ht.saveTrackableHandle( this castTo int, parentKey, value )

function loadGamecache( int parentKey ) returns gamecache
    return ht.loadGamecacheHandle( this castTo int, parentKey )

function saveGamecache( int parentKey, gamecache value )
    ht.saveGamecacheHandle( this castTo int, parentKey, value )

function loadVersion( int parentKey ) returns version
    return ht.loadVersionHandle( this castTo int, parentKey )

function saveVersion( int parentKey, version value )
    ht.saveVersionHandle( this castTo int, parentKey, value )

function loadItemtype( int parentKey ) returns itemtype
    return ht.loadItemtypeHandle( this castTo int, parentKey )

function saveItemtype( int parentKey, itemtype value )
    ht.saveItemtypeHandle( this castTo int, parentKey, value )

function loadTexttag( int parentKey ) returns texttag
    return ht.loadTexttagHandle( this castTo int, parentKey )

function saveTexttag( int parentKey, texttag value )
    ht.saveTexttagHandle( this castTo int, parentKey, value )

function loadAttacktype( int parentKey ) returns attacktype
    return ht.loadAttacktypeHandle( this castTo int, parentKey )

function saveAttacktype( int parentKey, attacktype value )
    ht.saveAttacktypeHandle( this castTo int, parentKey, value )

function loadDamagetype( int parentKey ) returns damagetype
    return ht.loadDamagetypeHandle( this castTo int, parentKey )

function saveDamagetype( int parentKey, damagetype value )
    ht.saveDamagetypeHandle( this castTo int, parentKey, value )

function loadWeapontype( int parentKey ) returns weapontype
    return ht.loadWeapontypeHandle( this castTo int, parentKey )

function saveWeapontype( int parentKey, weapontype value )
    ht.saveWeapontypeHandle( this castTo int, parentKey, value )

function loadSoundtype( int parentKey ) returns soundtype
    return ht.loadSoundtypeHandle( this castTo int, parentKey )

function saveSoundtype( int parentKey, soundtype value )
    ht.saveSoundtypeHandle( this castTo int, parentKey, value )

function loadLightning( int parentKey ) returns lightning
    return ht.loadLightningHandle( this castTo int, parentKey )

function saveLightning( int parentKey, lightning value )
    ht.saveLightningHandle( this castTo int, parentKey, value )

function loadPathingtype( int parentKey ) returns pathingtype
    return ht.loadPathingtypeHandle( this castTo int, parentKey )

function savePathingtype( int parentKey, pathingtype value )
    ht.savePathingtypeHandle( this castTo int, parentKey, value )

function loadImage( int parentKey ) returns image
    return ht.loadImageHandle( this castTo int, parentKey )

function saveImage( int parentKey, image value )
    ht.saveImageHandle( this castTo int, parentKey, value )

function loadUbersplat( int parentKey ) returns ubersplat
    return ht.loadUbersplatHandle( this castTo int, parentKey )

function saveUbersplat( int parentKey, ubersplat value )
    ht.saveUbersplatHandle( this castTo int, parentKey, value )

function loadHashtable( int parentKey ) returns hashtable
    return ht.loadHashtableHandle( this castTo int, parentKey )

function saveHashtable( int parentKey, hashtable value )
    ht.saveHashtableHandle( this castTo int, parentKey, value )

