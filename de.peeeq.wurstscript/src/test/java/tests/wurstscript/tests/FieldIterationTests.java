package tests.wurstscript.tests;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class FieldIterationTests extends WurstScriptTest {

    @Test
    public void serializesAndDeserializesFieldsWithoutRuntimeReflection() throws IOException {
        test()
            .testLua(true)
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
}
