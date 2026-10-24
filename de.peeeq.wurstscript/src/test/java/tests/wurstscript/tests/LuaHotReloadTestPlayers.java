package tests.wurstscript.tests;

/**
 * The player slots a hot-reload test's Lua runtime has: the poll counts the users playing, as blizzard.j's
 * InitBlizzardGlobals does for bj_isSinglePlayer, and the shared runtime has no player slots.
 */
final class LuaHotReloadTestPlayers {
    /** Player 0 is the one user playing; the others are empty. */
    static final String SINGLE_PLAYER = users(1);

    private LuaHotReloadTestPlayers() {
    }

    /** The first {@code n} players are users playing. */
    static String users(int n) {
        return "GetPlayerController = function(p) return MAP_CONTROL_USER end;"
            + "GetPlayerSlotState = function(p) if GetPlayerId(p) < " + n
            + " then return PLAYER_SLOT_STATE_PLAYING end return PLAYER_SLOT_STATE_EMPTY end;";
    }
}
