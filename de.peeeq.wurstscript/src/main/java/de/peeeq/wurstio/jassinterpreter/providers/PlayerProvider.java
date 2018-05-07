package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.PlayerMock;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;

import java.util.HashMap;

public class PlayerProvider extends Provider {
    HashMap<Integer, IlConstHandle> playerMap = new HashMap<>();

    public PlayerProvider(ILInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle Player(ILconstInt p) {
        return playerMap.computeIfAbsent(p.getVal(), (k) -> new IlConstHandle("Player" + p.getVal(), new PlayerMock(p)));
    }

    public ILconstInt GetPlayerId(IlConstHandle p) {
        return p != null ? ((PlayerMock) p.getObj()).id : ILconstInt.create(-1);
    }

    public IlConstHandle GetLocalPlayer() {
        return new IlConstHandle("Local Player", "local player");
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
}
