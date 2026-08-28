package tests.wurstscript.tests;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;

public class ConfigPackageTests extends WurstScriptTest {


    @Test
    public void configVar() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "int x = 5",
                "init",
                "	if x == 6",
                "		testSuccess()",
                "endpackage",
                "package Test_config",
                "@config int x = 6",
                "endpackage"
        );
    }

    @Test
    public void configVarWrongType() {
        testAssertErrorsLines(false, "Configured variable must have type int",
                "package Test",
                "int x = 5",
                "endpackage",
                "package Test_config",
                "@config string x = \"6\"",
                "endpackage"
        );
    }

    @Test
    public void configFunc() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "function foo(int x, int y) returns int",
                "	return x+y",
                "init",
                "	if foo(3,4) == 12",
                "		testSuccess()",
                "endpackage",
                "package Test_config",
                "@config function foo(int x, int y) returns int",
                "	return x*y",
                "endpackage"
        );
    }

    @Test
    public void configFuncWrongType() {
        testAssertErrorsLines(true, "Could not find a function foo",
                "package Test",
                "native testSuccess()",
                "function foo(int x, int y) returns int",
                "	return x+y",
                "endpackage",
                "package Test_config",
                "@config function foo(int x, real y) returns int",
                "	return x*y",
                "endpackage"
        );
    }

    @Test
    public void configOrdinaryFuncOverloadsByExactSignature() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "@configurable function value(int x) returns int",
                "    return 1",
                "@configurable function value(string x) returns int",
                "    return 2",
                "init",
                "    if value(1) == 10 and value(\"x\") == 20",
                "        testSuccess()",
                "endpackage",
                "package Test_config",
                "@config function value(string x) returns int",
                "    return 20",
                "@config function value(int x) returns int",
                "    return 10",
                "endpackage"
        );
    }

    @Test
    public void configExtensionExecutesConfiguredBody() {
        test().testLua(true).executeProg().lines(
                "package Test",
                "native testSuccess()",
                "@configurable function int.adjust(int x) returns int",
                "    return this + x",
                "init",
                "    int value = 4",
                "    if value.adjust(3) == 12",
                "        testSuccess()",
                "endpackage",
                "package Test_config",
                "@config function int.adjust(int x) returns int",
                "    return this * x",
                "endpackage"
        );
    }

    @Test
    public void configurableExtensionProducesNoWarning() {
        var result = test().setStopOnFirstError(false).lines(
                "package Test",
                "@configurable function int.adjust(int x) returns int",
                "    return this + x",
                "endpackage",
                "package Test_config",
                "@config function int.adjust(int x) returns int",
                "    return this * x",
                "endpackage"
        );
        assertFalse(result.getGui().getWarningList().stream()
                .anyMatch(w -> w.getMessage().contains("not marked with @configurable")));
    }

    @Test
    public void unannotatedConfiguredExtensionProducesWarning() {
        testAssertWarningsLines(false, "not marked with @configurable",
                "package Test",
                "function int.adjust(int x) returns int",
                "    return this + x",
                "endpackage",
                "package Test_config",
                "@config function int.adjust(int x) returns int",
                "    return this * x",
                "endpackage"
        );
    }

    @Test
    public void configExtensionOverloadsByExactSignature() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "@configurable function int.value(int x) returns int",
                "    return 1",
                "@configurable function int.value(string x) returns int",
                "    return 2",
                "init",
                "    int receiver = 0",
                "    if receiver.value(1) == 10 and receiver.value(\"x\") == 20",
                "        testSuccess()",
                "endpackage",
                "package Test_config",
                "@config function int.value(string x) returns int",
                "    return 20",
                "@config function int.value(int x) returns int",
                "    return 10",
                "endpackage"
        );
    }

    @Test
    public void configOnlyOneExtensionOverload() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "@configurable function int.value(int x) returns int",
                "    return 1",
                "@configurable function int.value(string x) returns int",
                "    return 2",
                "init",
                "    int receiver = 0",
                "    if receiver.value(1) == 10 and receiver.value(\"x\") == 2",
                "        testSuccess()",
                "endpackage",
                "package Test_config",
                "@config function int.value(int x) returns int",
                "    return 10",
                "endpackage"
        );
    }

    @Test
    public void configExtensionReceiverMismatch() {
        testAssertErrorsLines(false, "same signature in the configured package",
                "package Test",
                "function int.adjust(int x) returns int",
                "    return x",
                "endpackage",
                "package Test_config",
                "@config function real.adjust(int x) returns int",
                "    return x",
                "endpackage"
        );
    }

    @Test
    public void configExtensionParameterMismatch() {
        testAssertErrorsLines(false, "same signature in the configured package",
                "package Test",
                "function int.adjust(int x) returns int",
                "    return x",
                "endpackage",
                "package Test_config",
                "@config function int.adjust(real x) returns int",
                "    return this",
                "endpackage"
        );
    }

    @Test
    public void configExtensionReturnMismatch() {
        testAssertErrorsLines(false, "same signature in the configured package",
                "package Test",
                "function int.adjust(int x) returns int",
                "    return x",
                "endpackage",
                "package Test_config",
                "@config function int.adjust(int x) returns real",
                "    return x",
                "endpackage"
        );
    }

    @Test
    public void configExtensionVarargMismatch() {
        testAssertErrorsLines(false, "same signature in the configured package",
                "package Test",
                "function int.adjust(vararg int xs)",
                "endpackage",
                "package Test_config",
                "@config function int.adjust(int xs)",
                "endpackage"
        );
    }

    @Test
    public void configGenericExtensionWithRenamedTypeParameter() {
        test().testLua(true).executeProg().lines(
                "package Test",
                "native testSuccess()",
                "@configurable function T.pick<T:>(T other) returns T",
                "    return this",
                "init",
                "    int value = 1",
                "    if value.pick(2) == 2",
                "        testSuccess()",
                "endpackage",
                "package Test_config",
                "@config function U.pick<U:>(U other) returns U",
                "    return other",
                "endpackage"
        );
    }

    @Test
    public void configExtensionAppliesInsideImportingPackage() {
        testAssertOkLines(true,
                "package Extension",
                "@configurable public function int.adjust() returns int",
                "    return 1",
                "endpackage",
                "package Caller",
                "import Extension",
                "public function callAdjust() returns int",
                "    int value = 0",
                "    return value.adjust()",
                "endpackage",
                "package Test",
                "import Caller",
                "native testSuccess()",
                "init",
                "    if callAdjust() == 2",
                "        testSuccess()",
                "endpackage",
                "package Extension_config",
                "@config public function int.adjust() returns int",
                "    return 2",
                "endpackage"
        );
    }

    @Test
    public void configExtensionAppliesToCascade() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "public class Box",
                "    int value",
                "@configurable function Box.setValue(int value)",
                "    this.value = value",
                "init",
                "    let box = new Box()..setValue(3)",
                "    if box.value == 6",
                "        testSuccess()",
                "endpackage",
                "package Test_config",
                "import Test",
                "@config function Box.setValue(int value)",
                "    this.value = value * 2",
                "endpackage"
        );
    }

    @Test
    public void configExtensionAppliesToOperator() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "public class Box",
                "@configurable function Box.op_plus(int value) returns int",
                "    return value",
                "init",
                "    if new Box() + 2 == 4",
                "        testSuccess()",
                "endpackage",
                "package Test_config",
                "import Test",
                "@config function Box.op_plus(int value) returns int",
                "    return value * 2",
                "endpackage"
        );
    }

    @Test
    public void packageConfigDoesNotOverrideClassMethod() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "public class Box",
                "    function value(int x) returns int",
                "        return 1",
                "@configurable function value(int x) returns int",
                "    return 2",
                "init",
                "    if new Box().value(0) == 1 and value(0) == 3",
                "        testSuccess()",
                "endpackage",
                "package Test_config",
                "@config function value(int x) returns int",
                "    return 3",
                "endpackage"
        );
    }

    @Test
    public void packageConfigCannotTargetClassMethod() {
        testAssertErrorsLines(false, "same signature in the configured package",
                "package Test",
                "class Box",
                "    function value(int x) returns int",
                "        return 1",
                "endpackage",
                "package Test_config",
                "@config function value(int x) returns int",
                "    return 2",
                "endpackage"
        );
    }

    @Test
    public void configExtensionAllowsInitlaterDependencyCycle() {
        testAssertOkLines(false,
                "package Original",
                "@configurable public function int.adjust() returns int",
                "    return 1",
                "endpackage",
                "package Dependency",
                "import Original",
                "public function dependencyValue() returns int",
                "    return 2",
                "endpackage",
                "package Original_config",
                "import initlater Dependency",
                "@config public function int.adjust() returns int",
                "    return dependencyValue() * 2",
                "endpackage"
        );
    }

    @Test
    public void configVarCyclic() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "@configurable public var DEBUG_LEVEL = Loglevel.WARNING",
                "public enum Loglevel",
                "	WARNING",
                "	ERROR",
                "public function foo() returns Loglevel",
                "	return DEBUG_LEVEL",
                "init",
                "	if foo() == Loglevel.ERROR",
                "		testSuccess()",
                "endpackage",
                "package Test_config",
                "import Test",
                "@config public var DEBUG_LEVEL = Loglevel.ERROR",
                "endpackage"
        );
    }

    @Test
    public void configCyclicImportWarning() {
        testAssertErrorsLines(false,
            "Cyclic init dependency between packages",
            "package Test",
            "endpackage",
            "package Test_config",
            "import Requirement",
            "endpackage",
            "package Requirement",
            "import Test",
            "endpackage"
        );
    }

}
