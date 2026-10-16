package tests.wurstscript.tests;

import org.testng.annotations.Test;

public class CoreJassVersionTests extends WurstScriptTest {
    @Test
    public void bundled301ApiCompilesOnBothBackends() {
        test().withStdLib().testLua(true).lines(
            "package Test",
            "    @extern native consume(boolean value)",
            "    init",
            "        unit u = null",
            "        consume(BlzUnitIsAlive(u))",
            "        consume(UnitIsAliveBJ(u))",
            "        consume(UnitIsDeadBJ(u))",
            "        consume(BlzGetHUDScale() > 0.)",
            "        consume(BlzGetCameraAllowsHotkeyTargetLock())",
            "        consume(BlzUnitHasItemEquipped(u, null))");
    }
}
