package de.peeeq.wurstscript.translation.imoptimizer;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * this optimization is supposed to remove useless calls like
 * <p>
 * I2S(5)
 * <p>
 * when the result is not used
 */
public class UselessFunctionCallsRemover implements OptimizerPass {
    public int totalCallsRemoved = 0;

    public int optimize(ImTranslator trans) {
        totalCallsRemoved = 0;
        ImProg prog = trans.getImProg();
        for (ImFunction func : prog.getFunctions()) {
            optimizeFunc(func, trans);
        }
        return totalCallsRemoved;
    }

    @Override
    public String getName() {
        return "Useless function calls removed";
    }

    private void optimizeFunc(ImFunction func, ImTranslator trans) {
        optimizeStmts(func.getBody());
        func.flatten(trans);
    }

    private void optimizeStmts(ImStmts stmts) {
        ListIterator<ImStmt> it = stmts.listIterator();
        while (it.hasNext()) {
            ImStmt s = it.next();

            if (s instanceof ImFunctionCall) {
                ImFunctionCall fc = (ImFunctionCall) s;
                if (isNativeWithoutSideEffect(fc.getFunc())) {
                    if (!isWantedError(fc)) {
                        // put arguments into a list of statements
                        ImStmts newStmts = JassIm.ImStmts();
                        for (ImExpr arg : fc.getArguments()) {
                            // remove from current call
                            arg.setParent(null);
                            // and move to new statements
                            newStmts.add(arg);
                        }
                        s = ImHelper.statementExprVoid(newStmts);
                        it.set(s);
                        totalCallsRemoved++;
                    }
                }
            }
            optimizeChildren(s);
        }
    }

    /**
     * a call like fooo(_ / 0) is considered a wanted error and kept
     */
    private boolean isWantedError(ImFunctionCall fc) {
        if (fc.getArguments().size() != 1) {
            return false;
        }
        return isDivisionByZero(fc.getArguments().get(0));
    }

    private boolean isDivisionByZero(ImExpr e) {
        if (e instanceof ImOperatorCall) {
            ImOperatorCall oc = (ImOperatorCall) e;
            if (oc.getOp() == WurstOperator.DIV_INT) {
                ImExpr dividend = oc.getArguments().get(1);
                if (dividend instanceof ImIntVal) {
                    ImIntVal v = (ImIntVal) dividend;
                    if (v.getValI() == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void optimizeChildren(Element e) {
        for (int i = 0; i < e.size(); i++) {
            optimizeElement(e.get(i));
        }
    }

    private void optimizeElement(Element e) {
        if (e instanceof ImStmts) {
            ImStmts stmts = (ImStmts) e;
            optimizeStmts(stmts);
        } else {
            optimizeChildren(e);
        }
    }

    private boolean isNativeWithoutSideEffect(ImFunction func) {
        return func.isNative() && isFunctionWithoutSideEffect(func.getName());
    }

    private static List<String> functionsWithoutSideEffects;

    /**
     * checks if the jass-function with the given name can have no side effects.
     * Note that these are not all pure functions, some of these functions depend
     * on external state, but they never change it.
     */
    public static boolean isFunctionWithoutSideEffect(String funcName) {
        initFunctionsWithoutSideEffects();
        int res = Collections.binarySearch(functionsWithoutSideEffects, funcName);
        return res >= 0;
    }

    private static void initFunctionsWithoutSideEffects() {
        if (functionsWithoutSideEffects == null) {
            functionsWithoutSideEffects = Arrays.asList("AbilityId", "AbilityId2String", "Acos", "And", "Asin", "Atan",
                    "Atan2", "ConvertAIDifficulty", "ConvertAllianceType", "ConvertAttackType", "ConvertBlendMode",
                    "ConvertCameraField", "ConvertDamageType", "ConvertDialogEvent", "ConvertEffectType",
                    "ConvertFGameState", "ConvertFogState", "ConvertGameDifficulty", "ConvertGameEvent",
                    "ConvertGameSpeed", "ConvertGameType", "ConvertIGameState", "ConvertItemType", "ConvertLimitOp",
                    "ConvertMapControl", "ConvertMapDensity", "ConvertMapFlag", "ConvertMapSetting",
                    "ConvertMapVisibility", "ConvertPathingType", "ConvertPlacement", "ConvertPlayerColor",
                    "ConvertPlayerEvent", "ConvertPlayerGameResult", "ConvertPlayerScore", "ConvertPlayerSlotState",
                    "ConvertPlayerState", "ConvertPlayerUnitEvent", "ConvertRace", "ConvertRacePref",
                    "ConvertRarityControl", "ConvertSoundType", "ConvertStartLocPrio", "ConvertTexMapFlags",
                    "ConvertUnitEvent", "ConvertUnitState", "ConvertUnitType", "ConvertVersion", "ConvertVolumeGroup",
                    "ConvertWeaponType", "ConvertWidgetEvent", "Cos", "Deg2Rad", "FirstOfGroup", "GetAbilityEffect",
                    "GetAbilityEffectById", "GetAbilitySound", "GetAbilitySoundById", "GetAllyColorFilterState",
                    "GetAttacker", "GetBuyingUnit", "GetCameraBoundMaxX", "GetCameraBoundMaxY", "GetCameraBoundMinX",
                    "GetCameraBoundMinY", "GetCameraEyePositionLoc", "GetCameraEyePositionX", "GetCameraEyePositionY",
                    "GetCameraEyePositionZ", "GetCameraField", "GetCameraMargin", "GetCameraTargetPositionLoc",
                    "GetCameraTargetPositionX", "GetCameraTargetPositionY", "GetCameraTargetPositionZ",
                    "GetCancelledStructure", "GetChangingUnit", "GetChangingUnitPrevOwner", "GetClickedButton",
                    "GetClickedDialog", "GetConstructedStructure", "GetConstructingStructure", "GetCreatureDensity",
                    "GetCreepCampFilterState", "GetCustomCampaignButtonVisible", "GetDecayingUnit",
                    "GetDefaultDifficulty", "GetDestructableLife", "GetDestructableMaxLife", "GetDestructableName",
                    "GetDestructableOccluderHeight", "GetDestructableTypeId", "GetDestructableX", "GetDestructableY",
                    "GetDetectedUnit", "GetDyingUnit", "GetEnteringUnit", "GetEnumDestructable", "GetEnumItem",
                    "GetEnumPlayer", "GetEnumUnit", "GetEventDamage", "GetEventDamageSource", "GetEventDetectingPlayer",
                    "GetEventGameState", "GetEventPlayerChatString", "GetEventPlayerChatStringMatched",
                    "GetEventPlayerState", "GetEventTargetUnit", "GetEventUnitState", "GetExpiredTimer",
                    "GetFilterDestructable", "GetFilterItem", "GetFilterPlayer", "GetFilterUnit", "GetFloatGameState",
                    "GetFoodMade", "GetFoodUsed", "GetGameDifficulty", "GetGamePlacement", "GetGameSpeed",
                    "GetGameTypeSelected", "GetHandleId", "GetHeroAgi", "GetHeroInt", "GetHeroLevel",
                    "GetHeroProperName", "GetHeroSkillPoints", "GetHeroStr", "GetHeroXP", "GetIntegerGameState",
                    "GetIssuedOrderId", "GetItemCharges", "GetItemLevel", "GetItemName", "GetItemPlayer", "GetItemType",
                    "GetItemTypeId", "GetItemUserData", "GetItemX", "GetItemY", "GetKillingUnit", "GetLearnedSkill",
                    "GetLearnedSkillLevel", "GetLearningUnit", "GetLeavingUnit", "GetLevelingUnit",
                    "GetLightningColorA", "GetLightningColorB", "GetLightningColorG", "GetLightningColorR",
                    "GetLoadedUnit", "GetLocalizedHotkey", "GetLocalizedString", "GetLocationX", "GetLocationY",
                    "GetLocationZ", "GetManipulatedItem", "GetManipulatingUnit", "GetObjectName", "GetOrderedUnit",
                    "GetOrderPointLoc", "GetOrderPointX", "GetOrderPointY", "GetOrderTarget",
                    "GetOrderTargetDestructable", "GetOrderTargetItem", "GetOrderTargetUnit", "GetOwningPlayer",
                    "GetPlayerAlliance", "GetPlayerColor", "GetPlayerController", "GetPlayerHandicap",
                    "GetPlayerHandicapXP", "GetPlayerId", "GetPlayerName", "GetPlayerRace", "GetPlayers",
                    "GetPlayerScore", "GetPlayerSelectable", "GetPlayerSlotState", "GetPlayerStartLocation",
                    "GetPlayerState", "GetPlayerStructureCount", "GetPlayerTaxRate", "GetPlayerTeam",
                    "GetPlayerTechCount", "GetPlayerTechMaxAllowed", "GetPlayerTechResearched",
                    "GetPlayerTypedUnitCount", "GetPlayerUnitCount", "GetRectCenterX",
                    "GetRectCenterY", "GetRectMaxX", "GetRectMaxY", "GetRectMinX", "GetRectMinY", "GetRescuer",
                    "GetResearched", "GetResearchingUnit", "GetResourceAmount", "GetResourceDensity",
                    "GetRevivableUnit", "GetRevivingUnit", "GetSaveBasicFilename", "GetSelectedUnit", "GetSellingUnit",
                    "GetSoldItem", "GetSoldUnit", "GetSoundDuration", "GetSoundFileDuration", "GetSoundIsLoading",
                    "GetSoundIsPlaying", "GetSpellAbility", "GetSpellAbilityId", "GetSpellAbilityUnit",
                    "GetSpellTargetDestructable", "GetSpellTargetItem", "GetSpellTargetLoc", "GetSpellTargetUnit",
                    "GetSpellTargetX", "GetSpellTargetY", "GetStartLocationLoc", "GetStartLocationX",
                    "GetStartLocationY", "GetStartLocPrio", "GetStartLocPrioSlot", "GetStoredBoolean",
                    "GetStoredInteger", "GetStoredReal", "GetStoredString", "GetSummonedUnit", "GetSummoningUnit",
                    "GetTeams", "GetTerrainCliffLevel", "GetTerrainType", "GetTerrainVariance",
                    "GetTournamentFinishNowPlayer", "GetTournamentFinishNowRule",
                    "GetTournamentFinishSoonTimeRemaining", "GetTournamentScore", "GetTrainedUnit",
                    "GetTrainedUnitType", "GetTransportUnit", "GetTriggerDestructable", "GetTriggerEvalCount",
                    "GetTriggerEventId", "GetTriggerExecCount", "GetTriggeringRegion", "GetTriggeringTrackable",
                    "GetTriggeringTrigger", "GetTriggerPlayer", "GetTriggerUnit", "GetTriggerWidget",
                    "GetUnitAbilityLevel", "GetUnitAcquireRange", "GetUnitCurrentOrder", "GetUnitDefaultAcquireRange",
                    "GetUnitDefaultFlyHeight", "GetUnitDefaultMoveSpeed", "GetUnitDefaultPropWindow",
                    "GetUnitDefaultTurnSpeed", "GetUnitFacing", "GetUnitFlyHeight", "GetUnitFoodMade",
                    "GetUnitFoodUsed", "GetUnitLevel", "GetUnitLoc", "GetUnitMoveSpeed", "GetUnitName",
                    "GetUnitPointValue", "GetUnitPointValueByType", "GetUnitPropWindow", "GetUnitRace",
                    "GetUnitRallyDestructable", "GetUnitRallyPoint", "GetUnitRallyUnit", "GetUnitState",
                    "GetUnitTurnSpeed", "GetUnitTypeId", "GetUnitUserData", "GetUnitX", "GetUnitY", "GetWidgetLife",
                    "GetWidgetX", "GetWidgetY", "GetWinningPlayer", "GetWorldBounds", "HaveSavedBoolean",
                    "HaveSavedHandle", "HaveSavedInteger", "HaveSavedReal", "HaveSavedString", "HaveStoredBoolean",
                    "HaveStoredInteger", "HaveStoredReal", "HaveStoredString", "HaveStoredUnit", "I2R", "I2S",
                    "IsCineFilterDisplayed", "IsDestructableInvulnerable", "IsFogEnabled", "IsFoggedToPlayer",
                    "IsFogMaskEnabled", "IsGameTypeSupported", "IsHeroUnitId", "IsItemIdPawnable", "IsItemIdPowerup",
                    "IsItemIdSellable", "IsItemInvulnerable", "IsItemOwned", "IsItemPawnable", "IsItemPowerup",
                    "IsItemSellable", "IsItemVisible", "IsLeaderboardDisplayed", "IsLocationFoggedToPlayer",
                    "IsLocationInRegion", "IsLocationMaskedToPlayer", "IsLocationVisibleToPlayer", "IsMapFlagSet",
                    "IsMaskedToPlayer", "IsMultiboardDisplayed", "IsMultiboardMinimized", "IsNoDefeatCheat",
                    "IsNoVictoryCheat", "IsPlayerAlly", "IsPlayerEnemy", "IsPlayerInForce", "IsPlayerObserver",
                    "IsPlayerRacePrefSet", "IsPointBlighted", "IsPointInRegion", "IsQuestCompleted",
                    "IsQuestDiscovered", "IsQuestEnabled", "IsQuestFailed", "IsQuestItemCompleted", "IsQuestRequired",
                    "IsSuspendedXP", "IsTerrainPathable", "IsTimerDialogDisplayed", "IsTriggerEnabled",
                    "IsTriggerWaitOnSleeps", "IsUnit", "IsUnitAlly", "IsUnitDetected", "IsUnitEnemy", "IsUnitFogged",
                    "IsUnitHidden", "IsUnitIdType", "IsUnitIllusion", "IsUnitInForce", "IsUnitInGroup", "IsUnitInRange",
                    "IsUnitInRangeLoc", "IsUnitInRangeXY", "IsUnitInRegion", "IsUnitInTransport", "IsUnitInvisible",
                    "IsUnitLoaded", "IsUnitMasked", "IsUnitOwnedByPlayer", "IsUnitPaused", "IsUnitRace",
                    "IsUnitSelected", "IsUnitType", "IsUnitVisible", "IsVisibleToPlayer", "LoadAbilityHandle",
                    "LoadBoolean", "LoadBooleanExprHandle", "LoadButtonHandle", "LoadDefeatConditionHandle",
                    "LoadDestructableHandle", "LoadDialogHandle", "LoadEffectHandle", "LoadFogModifierHandle",
                    "LoadFogStateHandle", "LoadForceHandle", "LoadGroupHandle", "LoadHashtableHandle",
                    "LoadImageHandle", "LoadInteger", "LoadItemHandle", "LoadItemPoolHandle", "LoadLeaderboardHandle",
                    "LoadLightningHandle", "LoadLocationHandle", "LoadMultiboardHandle", "LoadMultiboardItemHandle",
                    "LoadPlayerHandle", "LoadQuestHandle", "LoadQuestItemHandle", "LoadReal", "LoadRectHandle",
                    "LoadRegionHandle", "LoadSoundHandle", "LoadStr", "LoadTextTagHandle", "LoadTimerDialogHandle",
                    "LoadTimerHandle", "LoadTrackableHandle", "LoadTriggerActionHandle", "LoadTriggerConditionHandle",
                    "LoadTriggerEventHandle", "LoadTriggerHandle", "LoadUbersplatHandle", "LoadUnitHandle",
                    "LoadUnitPoolHandle", "LoadWidgetHandle", "Not", "Or", "OrderId", "OrderId2String", "Player", "Pow", "R2I",
                    "R2S", "R2SW", "Rad2Deg", "S2I", "S2R", "Sin", "SquareRoot", "StringCase", "StringHash",
                    "StringLength", "SubString", "Tan", "TimerGetElapsed", "TimerGetRemaining", "TimerGetTimeout",
                    "VersionGet", "WaygateGetDestinationX", "WaygateGetDestinationY", "WaygateIsActive");
            // just to be sure, sort it again
            Collections.sort(functionsWithoutSideEffects);
        }
    }

}
