package tests.wurstscript.tests;

import org.testng.annotations.Test;

public class CoreJassVersionTests extends WurstScriptTest {
    @Test
    public void bundled301ApiCompilesOnBothBackends() {
        test().withStdLib().testLua(true).luaOnly(false).lines(
            "package Test",
            "    function consume(boolean value)",
            "        if value",
            "            BJDebugMsg(\"true\")",
            "    init",
            "        unit u = null",
            "        consume(BlzUnitIsAlive(u))",
            "        consume(UnitIsAliveBJ(u))",
            "        consume(UnitIsDeadBJ(u))",
            "        consume(BlzGetHUDScale() > 0.)",
            "        BlzSetHUDScale(100)",
            "        BlzLockHUDScale()",
            "        BlzUnlockHUDScale()",
            "        consume(BlzGetCameraAllowsHotkeyTargetLock())",
            "        consume(BlzUnitHasItemEquipped(u, null))");
    }
}
