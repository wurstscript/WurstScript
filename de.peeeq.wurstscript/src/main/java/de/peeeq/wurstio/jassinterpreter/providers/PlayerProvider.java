package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.PlayerMock;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

import java.util.HashMap;

public class PlayerProvider extends Provider {
    HashMap<Integer, IlConstHandle> playerMap = new HashMap<>();

    public PlayerProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle Player(ILconstInt p) {
        return playerMap.computeIfAbsent(p.getVal(), (k) -> new IlConstHandle("Player" + p.getVal(), new PlayerMock(p)));
    }

    public ILconstInt GetPlayerId(IlConstHandle p) {
        return p != null ? ((PlayerMock) p.getObj()).id : ILconstInt.create(-1);
    }

    public void SetPlayerState(IlConstHandle player, IlConstHandle playerstate, ILconstInt value) {
        if (player == null || playerstate == null) {
            return;
        }
        String key = playerstate.print();
        if (key.equals("playerstate1") || key.equals("playerstate2")) {
            // Measured on the 3.0.0 client: gold and lumber are clamped to 0.
            value = ILconstInt.create(Math.max(0, value.getVal()));
        }
        ((PlayerMock) player.getObj()).playerStates.put(key, value);
    }

    public ILconstInt GetPlayerState(IlConstHandle player, IlConstHandle playerstate) {
        if (player == null || playerstate == null) {
            return ILconstInt.create(0);
        }
        return ((PlayerMock) player.getObj()).playerStates.getOrDefault(playerstate.print(), ILconstInt.create(0));
    }

    public IlConstHandle GetPlayerSlotState(IlConstHandle player) {
        return ConversionProvider.enumHandle("playerslotstate", 1);
    }

    public IlConstHandle GetPlayerController(IlConstHandle player) {
        return ConversionProvider.enumHandle("mapcontrol", 0);
    }

    public ILconstInt GetPlayerNeutralPassive() {
        // fake value
        return new ILconstInt(31);
    }

    public ILconstInt GetPlayerNeutralAggressive() {
        // fake value
        return new ILconstInt(30);
    }


    public IlConstHandle GetLocalPlayer() {
        return Player(ILconstInt.create(0));
    }

    public ILconstInt GetBJMaxPlayerSlots() {
        return new ILconstInt(28);
    }

    public ILconstInt GetBJMaxPlayers() {
        return new ILconstInt(24);
    }

    public void SetPlayerColor(IlConstHandle player, IlConstHandle playercolor) {
        ((PlayerMock) player.getObj()).playerColor = playercolor;
    }

    public ILconst GetPlayerColor(IlConstHandle player) {
        return ((PlayerMock) player.getObj()).playerColor;
    }

    public void SetPlayerTechMaxAllowed(IlConstHandle player, ILconstInt techid, ILconstInt maximum) {
        ((PlayerMock) player.getObj()).techMaxAllowed.put(techid.getVal(), maximum);
    }

    public ILconstInt GetPlayerTechMaxAllowed(IlConstHandle player, ILconstInt techid) {
        return ((PlayerMock) player.getObj()).techMaxAllowed.getOrDefault(techid.getVal(), ILconstInt.create(0));
    }
}
