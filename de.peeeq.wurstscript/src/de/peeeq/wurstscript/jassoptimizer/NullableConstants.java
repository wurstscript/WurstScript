package de.peeeq.wurstscript.jassoptimizer;

public class NullableConstants {
	static String names[] = { 	"PLAYER_COLOR_RED" , "PLAYER_GAME_RESULT_VICTORY", "ALLIANCE_PASSIVE", "VERSION_REIGN_OF_CHAOS",
		      					"ATTACK_TYPE_NORMAL", "DAMAGE_TYPE_UNKNOWN", "DAMAGE_TYPE_UNIVERSAL", "WEAPON_TYPE_WHOKNOWS",
		      					"PATHING_TYPE_ANY", "MAP_CONTROL_USER", "MAP_PLACEMENT_RANDOM", "MAP_LOC_PRIO_LOW", "MAP_DENSITY_NONE", 
		      					"MAP_DIFFICULTY_EASY", "MAP_SPEED_SLOWEST", "PLAYER_SLOT_STATE_EMPTY", "SOUND_VOLUMEGROUP_UNITMOVEMENT",
		      					"GAME_STATE_DIVINE_INTERVENTION", "PLAYER_STATE_GAME_RESULT", "UNIT_STATE_LIFE", "AI_DIFFICULTY_NEWBIE",
		      					"PLAYER_SCORE_UNITS_TRAINED", "EVENT_GAME_VICTORY", "LESS_THAN", "UNIT_TYPE_HERO", "ITEM_TYPE_PERMANENT",
		      					"CAMERA_FIELD_TARGET_DISTANCE", "BLEND_MODE_NONE", "BLEND_MODE_DONT_CARE", "RARITY_FREQUENT", 
		      					"TEXMAP_FLAG_NONE", "EFFECT_TYPE_EFFECT", "SOUND_TYPE_EFFECT" };
	
	public static boolean contains(String s) {
		for (int i = 0; i <= 32; i++) {
			if ( names[i] == s) {
				return true;
			}
		}
		return false;
	}
}
