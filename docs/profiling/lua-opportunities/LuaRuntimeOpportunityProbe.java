package tests.wurstscript.tests;

import org.testng.annotations.Test;

/** Opt-in investigation fixtures; two methods reproduce the defects #1471 fixed. */
public class LuaRuntimeOpportunityProbe extends WurstScriptTest {
    @Test(dataProvider = "optimizationOptions")
    public void ignoredVirtualResultStillRunsOverrideEffects(boolean optimized) {
        TestConfig config = test().testLua(true).luaOnly(false).executeProg();
        if (optimized) config.inline().localOptimizations();
        config.lines(
            "package Probe", "native testSuccess()", "native testFail(string message)",
            "int hits = 0",
            "class Base",
            "    function value() returns int", "        return 1",
            "class Child extends Base",
            "    override function value() returns int", "        hits++", "        return 2",
            "@noinline function choose(boolean child) returns Base",
            "    return child ? new Child() : new Base()",
            "init", "    Base m = choose(true)", "    let unused = m.value()",
            "    if hits != 1", "        testFail(\"ignored return must not drop virtual override effects\")",
            "    testSuccess()");
    }

    @Test
    public void receiverRegisterPressure() {
        java.util.List<String> source = new java.util.ArrayList<>(java.util.List.of(
            "package Probe", "native sink(int value)", "native flag() returns boolean",
            "interface Metric", "    function value() returns int",
            "class A implements Metric", "    override function value() returns int", "        return 1",
            "class B implements Metric", "    override function value() returns int", "        return 2",
            "Metric left = new A()", "Metric right = new B()",
            "@noinline function choose(boolean pick) returns Metric", "    return pick ? left : right",
            "@noinline function crowded()"));
        for (int i = 0; i < 199; i++) source.add("    int v" + i + " = flag() ? 1 : 2");
        source.add("    sink(choose(flag()).value())");
        for (int i = 0; i < 199; i++) source.add("    sink(v" + i + ")");
        source.add("init");
        source.add("    crowded()");
        test().testLua(true).inline().localOptimizations().lines(source.toArray(String[]::new));
    }

    @org.testng.annotations.DataProvider
    public Object[][] optimizationOptions() { return new Object[][] {{false}, {true}}; }

    @Test(dataProvider = "optimizationOptions")
    public void unusedAllocationStillConsumesAnId(boolean optimized) {
        TestConfig config = test().testLua(true).luaOnly(false)
            .executeProgOnlyAfterTransforms().executeProg();
        if (optimized) config.inline().localOptimizations();
        config.lines(
            "package Probe",
            "native testSuccess()",
            "native testFail(string msg)",
            "class Empty",
            "init",
            "    let first = new Empty()",
            "    new Empty()",
            "    let last = new Empty()",
            "    if (last castTo int) != (first castTo int) + 2",
            "        testFail(\"unused allocation must still consume an id\")",
            "    testSuccess()");
    }
    @Test
    public void emission() {
        test().testLua(true).luaOnly(false).inline().localOptimizations().lines(
            "package Probe",
            "native sink(int value)",
            "native flag() returns boolean",
            "interface Metric",
            "    function value() returns int",
            "    function other() returns int",
            "class A implements Metric",
            "    int data = 1",
            "    override function value() returns int",
            "        return data",
            "    override function other() returns int",
            "        return data + 1",
            "class B implements Metric",
            "    int data = 2",
            "    override function value() returns int",
            "        return data",
            "    override function other() returns int",
            "        return data + 2",
            "class Box",
            "    Metric child",
            "    construct(Metric child)",
            "        this.child = child",
            "Metric left = new A()",
            "Metric right = new B()",
            "@noinline function choose(boolean pick) returns Metric",
            "    return pick ? left : right",
            "@noinline function repeated(Metric m) returns int",
            "    return m.value() + m.other() + m.value()",
            "@noinline function effectfulReceiver(boolean pick) returns int",
            "    return choose(pick).value()",
            "@noinline function fieldReceiver(Box box) returns int",
            "    return box.child.value() + box.child.other()",
            "@noinline function exactFresh() returns int",
            "    Metric m = new A()",
            "    return m.value() + m.other()",
            "init",
            "    sink(repeated(choose(flag())))",
            "    sink(effectfulReceiver(flag()))",
            "    sink(fieldReceiver(new Box(choose(flag()))))",
            "    sink(exactFresh())");
    }

    @Test
    public void lifetimeBarrierAndFieldMutation() {
        test().testLua(true).luaOnly(false).inline().localOptimizations().executeProgOnlyAfterTransforms().executeProg().lines(
            "package Probe",
            "native testSuccess()",
            "native testFail(string msg)",
            "interface Metric",
            "    function value() returns int",
            "    function advance() returns int",
            "class A implements Metric",
            "    int data = 1",
            "    override function value() returns int",
            "        return data",
            "    override function advance() returns int",
            "        destroy this",
            "        makeReplacement()",
            "        return 1",
            "class B implements Metric",
            "    override function value() returns int",
            "        return 20",
            "    override function advance() returns int",
            "        return 20",
            "class Box",
            "    Metric child",
            "    construct(Metric child)",
            "        this.child = child",
            "Box box = null",
            "Metric replacement = null",
            "@noinline function makeReplacement()",
            "    replacement = new B()",
            "@noinline function changeField()",
            "    box.child = replacement",
            "init",
            "    Metric m = new A()",
            "    let first = m.advance()",
            "    if first != 1 or replacement != m or m.value() != 20",
            "        testFail(\"dispatch must observe destroy and id reuse\")",
            "    box = new Box(new A())",
            "    replacement = new B()",
            "    let before = box.child.value()",
            "    changeField()",
            "    if before != 1 or box.child.value() != 20",
            "        testFail(\"receiver field must be read again after mutation\")",
            "    testSuccess()");
    }
}
