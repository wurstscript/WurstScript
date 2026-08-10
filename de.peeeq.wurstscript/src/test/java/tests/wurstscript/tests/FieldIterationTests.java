package tests.wurstscript.tests;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class FieldIterationTests extends WurstScriptTest {

    @Test
    public void explicitTargetAndGenericConstructionWorkForJassAndLua() throws IOException {
        test()
            .testLua(true)
            .luaOnly(false)
            .executeProg()
            .lines(
                "package MagicFunctions",
                "endpackage",
                "",
                "package FieldIterationTest",
                "    import MagicFunctions",
                "    native testSuccess()",
                "",
                "    interface FieldLoader<T:>",
                "        function apply(Reader reader, T target)",
                "",
                "    class Reader",
                "        function read(string name, int oldValue) returns int",
                "            return oldValue + 1",
                "        function read(string name, real oldValue) returns real",
                "            return oldValue + 1.",
                "        function read(string name, string oldValue) returns string",
                "            return oldValue + \"!\"",
                "        function read(string name, boolean oldValue) returns boolean",
                "            return not oldValue",
                "",
                "    function load<T:>(Reader reader, FieldLoader<T> loader) returns T",
                "        let result = newInstance<T>()",
                "        loader.apply(reader, result)",
                "        return result",
                "",
                "    class FirstState",
                "        int score",
                "        real ratio",
                "        construct()",
                "            score = 10",
                "            ratio = 2.",
                "",
                "    class SecondState",
                "        string name",
                "        boolean enabled",
                "        construct()",
                "            name = \"loaded\"",
                "            enabled = false",
                "",
                "    init",
                "        let reader = new Reader",
                "        let first = load<FirstState>(reader, (Reader r, FirstState state) -> begin",
                "            mapFields(state, (name, oldValue) -> r.read(name, oldValue))",
                "        end)",
                "        let second = load<SecondState>(reader, (Reader r, SecondState state) -> begin",
                "            mapFields(state, (name, oldValue) -> r.read(name, oldValue))",
                "        end)",
                "        if first.score == 11 and first.ratio == 3. and second.name == \"loaded!\" and second.enabled",
                "            testSuccess()",
                "endpackage"
            );

        String lua = Files.readString(new File(TEST_OUTPUT_PATH
            + "lua/FieldIterationTests_explicitTargetAndGenericConstructionWorkForJassAndLua.lua").toPath());
        String jass = Files.readString(new File(TEST_OUTPUT_PATH
            + "FieldIterationTests_explicitTargetAndGenericConstructionWorkForJassAndLua_opt.j").toPath());
        for (String generated : new String[]{lua, jass}) {
            assertFalse(generated.contains("newInstance"));
            assertFalse(generated.contains("mapFields"));
            assertFalse(generated.contains("reflection"));
        }
    }

    @Test
    public void explicitTargetIsEvaluatedOnceAndIncludesInheritedAndModuleFields() {
        test()
            .testLua(true)
            .luaOnly(false)
            .executeProg()
            .lines(
                "package MagicFunctions",
                "endpackage",
                "",
                "package FieldIterationTest",
                "    import MagicFunctions",
                "    native testSuccess()",
                "    int evaluations = 0",
                "    int total = 0",
                "",
                "    module ExtraState",
                "        int moduleValue = 2",
                "",
                "    class BaseState",
                "        int inheritedValue = 3",
                "",
                "    class State extends BaseState",
                "        use ExtraState",
                "        int localValue = 4",
                "",
                "    function evaluated(State state) returns State",
                "        evaluations++",
                "        return state",
                "    function add(int value)",
                "        total += value",
                "",
                "    init",
                "        let state = new State",
                "        forFields(evaluated(state), (name, value) -> add(value))",
                "        mapFields(evaluated(state), (name, value) -> value + 1)",
                "        if evaluations == 2 and total == 9 and state.inheritedValue == 4 and state.moduleValue == 3 and state.localValue == 5",
                "            testSuccess()",
                "endpackage"
            );
    }

    @Test
    public void genericConstructionDiagnostics() {
        expectIntrinsicError("requires a concrete, non-abstract class type, but found int",
            "init", "    newInstance<int>()");
        expectIntrinsicError("requires a concrete, non-abstract class type, but found handle",
            "init", "    newInstance<handle>()");
        expectIntrinsicError("cannot construct interface State",
            "interface State", "    function unused()", "", "init", "    newInstance<State>()");
        expectIntrinsicError("cannot construct abstract class State",
            "abstract class State", "", "init", "    newInstance<State>()");
        expectIntrinsicError("requires class State to have a zero-argument constructor",
            "class State", "    construct(int value)", "", "init", "    newInstance<State>()");
        expectIntrinsicError("cannot access the zero-argument constructor of class State",
            "class State", "    private construct()", "", "init", "    newInstance<State>()");
        expectIntrinsicError("cannot construct unresolved type parameter T",
            "function make<T>() returns T", "    return newInstance<T>()");
    }

    @Test
    public void explicitTargetFieldIterationDiagnostics() {
        expectIntrinsicError("target must have a concrete class type, but found int",
            "function consume(string name, int value)", "", "init", "    forFields(1, (name, value) -> consume(name, value))");
        expectIntrinsicError("expects a closure with (fieldName, fieldValue) parameters",
            "class State", "    int value", "", "init", "    let state = new State", "    mapFields(state, value -> value)");
        expectIntrinsicError("no accessible mutable instance fields were found",
            "class State", "    readonly int value = 1", "", "init", "    let state = new State", "    mapFields(state, (name, value) -> value)");
        expectIntrinsicError("no accessible mutable instance fields were found",
            "function consume(string name, int value)", "", "class State", "    static int value = 1", "", "init", "    let state = new State", "    forFields(state, (name, value) -> consume(name, value))");
    }

    private void expectIntrinsicError(String message, String... body) {
        String[] lines = new String[body.length + 2];
        lines[0] = "package FieldIterationTest";
        System.arraycopy(body, 0, lines, 1, body.length);
        lines[lines.length - 1] = "endpackage";
        test().expectError(message).lines(lines);
    }

    @Test
    public void serializesAndDeserializesFieldsWithoutRuntimeReflection() throws IOException {
        test()
            .testLua(true)
            .luaOnly(false)
            .executeProg()
            .lines(
                "package FieldIterationTest",
                "    native testSuccess()",
                "",
                "    class Codec",
                "        int writtenInt",
                "        string writtenString",
                "        string writtenIntName",
                "        string writtenStringName",
                "",
                "        function write(string fieldName, int value)",
                "            writtenInt = value",
                "            writtenIntName = fieldName",
                "",
                "        function write(string fieldName, string value)",
                "            writtenString = value",
                "            writtenStringName = fieldName",
                "",
                "        function read(string fieldName, int oldValue) returns int",
                "            if fieldName == \"score\"",
                "                return 42",
                "            return -1",
                "",
                "        function read(string fieldName, string oldValue) returns string",
                "            if fieldName == \"name\"",
                "                return \"loaded\"",
                "            return \"wrong field\"",
                "",
                "    class Data",
                "        int score = 7",
                "        string name = \"initial\"",
                "        static int schemaVersion = 1",
                "",
                "        function save(Codec codec)",
                "            forFields((fieldName, value) -> codec.write(fieldName, value))",
                "",
                "        function load(Codec codec)",
                "            mapFields((fieldName, value) -> codec.read(fieldName, value))",
                "",
                "    init",
                "        let codec = new Codec",
                "        let data = new Data",
                "        data.save(codec)",
                "        if codec.writtenInt == 7 and codec.writtenString == \"initial\" and codec.writtenIntName == \"score\" and codec.writtenStringName == \"name\"",
                "            data.load(codec)",
                "            if data.score == 42 and data.name == \"loaded\"",
                "                testSuccess()",
                "endpackage"
            );

        String lua = Files.readString(new File(TEST_OUTPUT_PATH +
            "lua/FieldIterationTests_serializesAndDeserializesFieldsWithoutRuntimeReflection.lua").toPath());
        assertFalse(lua.contains("forFields"));
        assertFalse(lua.contains("mapFields"));
        assertTrue(lua.contains("Codec_Codec_write(codec, \"score\", this"));
        assertTrue(lua.contains("Codec_Codec_write1(codec, \"name\", this"));
        assertTrue(lua.contains("Data_score = Codec_Codec_read(codec1, \"score\""));
        assertTrue(lua.contains("Data_name = Codec_Codec_read1(codec1, \"name\""));
    }

    @Test
    public void rejectsFieldIterationOutsideInstanceContext() {
        test()
            .expectError("can only be used in an instance method or constructor")
            .lines(
                "package FieldIterationTest",
                "    function consume(string name, int value)",
                "",
                "    init",
                "        forFields((name, value) -> consume(name, value))",
                "endpackage"
            );
    }

    @Test
    public void rejectsInvalidFieldIterationClosure() {
        test()
            .expectError("expects a closure with (fieldName, fieldValue) parameters")
            .lines(
                "package FieldIterationTest",
                "    class Data",
                "        int value",
                "",
                "        function save()",
                "            forFields(value -> value)",
                "endpackage"
            );
    }

    @Test
    public void ordinaryFieldHelperNamesRemainUserFunctions() {
        test()
            .executeProg()
            .lines(
                "package FieldIterationTest",
                "    native testSuccess()",
                "",
                "    function forFields(int value) returns int",
                "        return value + 1",
                "",
                "    init",
                "        if forFields(1) == 2",
                "            testSuccess()",
                "endpackage"
            );
    }

    @Test
    public void nestedClosureParametersAreNotSubstituted() {
        test()
            .executeProg()
            .lines(
                "package FieldIterationTest",
                "    native testSuccess()",
                "    interface IntFunc",
                "        function apply(int value) returns int",
                "",
                "    function consume(IntFunc callback) returns int",
                "        return callback.apply(5)",
                "",
                "    class Codec",
                "        int total",
                "",
                "        function write(string fieldName, int value)",
                "            total = total + value",
                "",
                "    class Data",
                "        int first = 1",
                "        int second = 2",
                "",
                "        function save(Codec codec)",
                "            forFields((fieldName, value) -> codec.write(fieldName, consume((int value) -> value)))",
                "",
                "    init",
                "        let codec = new Codec",
                "        let data = new Data",
                "        data.save(codec)",
                "        if codec.total == 10",
                "            testSuccess()",
                "endpackage"
            );
    }

    @Test
    public void rejectsExplicitFieldIterationParameterTypes() {
        test()
            .expectError("must use inferred types")
            .lines(
                "package FieldIterationTest",
                "    class Data",
                "        int value",
                "",
                "        function save()",
                "            forFields((NoSuch name, NoSuch value) -> value)",
                "endpackage"
            );
    }

    @Test
    public void rejectsFieldIterationWithoutInstanceFields() {
        test()
            .expectError("requires at least one instance field")
            .lines(
                "package FieldIterationTest",
                "    class Data",
                "        static int schemaVersion = 1",
                "",
                "        function save()",
                "            forFields((name, value) -> noSuchFunction(name, value))",
                "endpackage"
            );
    }

    @Test
    public void rejectsDuplicateFieldIterationParameterNames() {
        test()
            .expectError("must have distinct names")
            .lines(
                "package FieldIterationTest",
                "    class Data",
                "        int value",
                "",
                "        function save()",
                "            mapFields((value, value) -> 42)",
                "endpackage"
            );
    }

    @Test
    public void preservesLocalBindingsInBlockCallbacks() {
        test()
            .executeProg()
            .lines(
                "package FieldIterationTest",
                "    native testSuccess()",
                "    class Data",
                "        int first = 1",
                "        int second = 2",
                "",
                "        function load()",
                "            mapFields((name, value) -> begin",
                "                let temporary = 42",
                "                return temporary",
                "            end)",
                "",
                "    init",
                "        let data = new Data",
                "        data.load()",
                "        if data.first == 42 and data.second == 42",
                "            testSuccess()",
                "endpackage"
            );
    }

    @Test
    public void keepsLoopBindingsInsideLoopBody() {
        test()
            .executeProg()
            .lines(
                "package FieldIterationTest",
                "    native testSuccess()",
                "    class Data",
                "        int first = 1",
                "        int second = 2",
                "",
                "        function load()",
                "            mapFields((name, value) -> begin",
                "                for int index = 0 to 1",
                "                    continue",
                "                return 42 + value - value",
                "            end)",
                "",
                "    init",
                "        let data = new Data",
                "        data.load()",
                "        if data.first == 42 and data.second == 42",
                "            testSuccess()",
                "endpackage"
            );
    }

    @Test
    public void rejectsBlockLocalShadowingBeforeExpansion() {
        test()
            .expectError("cannot declare locals or loop variables")
            .lines(
                "package FieldIterationTest",
                "    class Data",
                "        int value",
                "",
                "        function load()",
                "            mapFields((name, value) -> begin",
                "                let old = value",
                "                let value = 42",
                "                return old",
                "            end)",
                "endpackage"
            );
    }

    @Test
    public void includesInheritedInstanceFields() {
        test()
            .executeProg()
            .lines(
                "package FieldIterationTest",
                "    native testSuccess()",
                "    class Acc",
                "        int total = 0",
                "        function add(int value)",
                "            total = total + value",
                "    class Base",
                "        private int hidden = 100",
                "        int inherited = 1",
                "    class Data extends Base",
                "        int local = 2",
                "",
                "        function save(Acc acc)",
                "            forFields((name, value) -> acc.add(value))",
                "",
                "    init",
                "        let acc = new Acc",
                "        let data = new Data",
                "        data.save(acc)",
                "        if acc.total == 3",
                "            testSuccess()",
                "endpackage"
            );
    }

    @Test
    public void expandsFieldIterationInModuleMethodsAfterInstantiation() {
        test()
            .executeProg()
            .lines(
                "package FieldIterationTest",
                "    native testSuccess()",
                "    class Acc",
                "        int total = 0",
                "        function add(int value)",
                "            total = total + value",
                "    module Serializer",
                "        function save(Acc acc)",
                "            forFields((name, value) -> acc.add(value))",
                "            forFields((name, value) -> acc.add(value))",
                "",
                "    class Data",
                "        use Serializer",
                "        int inherited = 1",
                "        int local = 2",
                "        int count = 0",
                "",
                "    init",
                "        let acc = new Acc",
                "        let data = new Data",
                "        data.save(acc)",
                "        if acc.total == 6",
                "            testSuccess()",
                "endpackage"
            );
    }

    @Test
    public void includesInstanceFieldsFromModules() {
        test()
            .executeProg()
            .lines(
                "package FieldIterationTest",
                "    native testSuccess()",
                "    class Acc",
                "        int total = 0",
                "        function add(int value)",
                "            total = total + value",
                "    module BaseState",
                "        int baseValue = 2",
                "    module State",
                "        use BaseState",
                "        int moduleValue = 4",
                "    class Data",
                "        use State",
                "        int local = 5",
                "",
                "        function save(Acc acc)",
                "            forFields((name, value) -> acc.add(value))",
                "",
                "    init",
                "        let acc = new Acc",
                "        let data = new Data",
                "        data.save(acc)",
                "        if acc.total == 11",
                "            testSuccess()",
                "endpackage"
            );
    }

    @Test
    public void qualifiesFieldsFromSiblingModules() {
        test()
            .testLua(true)
            .luaOnly(false)
            .executeProg()
            .lines(
                "package FieldIterationTest",
                "    native testSuccess()",
                "    class Acc",
                "        int left",
                "        int right",
                "        function add(string name, int value)",
                "            if name == \"Left.x\"",
                "                left = value",
                "            if name == \"Right.x\"",
                "                right = value",
                "    module Left",
                "        int x = 1",
                "    module Right",
                "        int x = 2",
                "    class Data",
                "        use Left",
                "        use Right",
                "        function save(Acc acc)",
                "            forFields((name, value) -> acc.add(name, value))",
                "    init",
                "        let acc = new Acc",
                "        let data = new Data",
                "        data.save(acc)",
                "        if acc.left == 1 and acc.right == 2",
                "            testSuccess()",
                "endpackage"
            );
    }

    @Test
    public void excludesPrivateModuleFields() {
        test()
            .executeProg()
            .lines(
                "package FieldIterationTest",
                "    native testSuccess()",
                "    class Acc",
                "        int total",
                "        function add(int value)",
                "            total = total + value",
                "    module State",
                "        private int hidden = 100",
                "        int visible = 2",
                "    class Data",
                "        use State",
                "        function save(Acc acc)",
                "            forFields((name, value) -> acc.add(value))",
                "    init",
                "        let acc = new Acc",
                "        let data = new Data",
                "        data.save(acc)",
                "        if acc.total == 2",
                "            testSuccess()",
                "endpackage"
            );
    }

    @Test
    public void validatesUnusedModuleFieldIteration() {
        test()
            .expectError("expects a closure with (fieldName, fieldValue) parameters")
            .lines(
                "package FieldIterationTest",
                "    module Unused",
                "        function save()",
                "            forFields(value -> value)",
                "endpackage"
            );
    }
}
