package tests.wurstscript.tests;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import org.testng.annotations.Test;
import org.wurstscript.projectconfig.WurstProjectConfigData;

import java.util.Collections;
import java.util.regex.Pattern;

import static org.testng.AssertJUnit.*;

public class LuaNameCompressionTests extends WurstScriptTest {
    static String[] source() {
        StringBuilder source = new StringBuilder("""
            package Test
            native testSuccess()
            native testFail(string message)
            @preserveName int w = 7
            @preserveName @noinline function u() returns int
                return w
            class CompressionOwner
            abstract class CompressionState<T:>
                abstract function compressionUpdate(T owner) returns int
            class CompressionNoOp<T:> extends CompressionState<T>
                override function compressionUpdate(T owner) returns int
                    return 0
            """);
        for (int i = 1; i <= 4; i++) {
            source.append("class CompressionSibling" + i + " extends CompressionNoOp<CompressionOwner>\n")
                .append("    int compressionField = " + i + "\n")
                .append("    override function compressionUpdate(CompressionOwner owner) returns int\n")
                .append("        return compressionField\n");
        }
        source.append("""
            constant CompressionState<CompressionOwner> compressionEarly = new CompressionSibling1()
            @noinline function compressionCall(CompressionState<CompressionOwner> state, CompressionOwner owner) returns int
                return state.compressionUpdate(owner)
            init
                let owner = new CompressionOwner()
                if u() != 7
                    testFail("preserved names")
                if compressionCall(compressionEarly, owner) != 1
                    testFail("early dispatch")
            """);
        for (int i = 2; i <= 4; i++) {
            source.append("    if compressionCall(new CompressionSibling" + i + "(), owner) != " + i + "\n")
                .append("        testFail(\"sibling dispatch\")\n");
        }
        source.append("    testSuccess()\n");
        return source.toString().split("\n");
    }

    private String compile(boolean optimize) {
        RunArgs args = new RunArgs().with("-lua", "-inline", "-localOptimizations");
        if (optimize) args = args.with("-opt");
        var gui = new WurstGuiCliImpl();
        var compiler = new WurstCompilerJassImpl(null, gui, null, args);
        WurstModel model = parseFiles(Collections.emptyList(),
            Collections.singletonList(new CU("compression.wurst", String.join("\n", source()))), false, compiler);
        compiler.checkProg(model);
        assertTrue(gui.getErrorList().toString(), gui.getErrorList().isEmpty());
        compiler.translateProgToIm(model);
        compiler.runCompiletime(WurstProjectConfigData.empty(), false, false);
        var lua = compiler.transformProgToLua();
        StringBuilder output = new StringBuilder();
        lua.print(output, 0);
        return output.toString();
    }

    @Test
    public void optCompressesLuaDescriptorsFieldsAndDispatchSlots() {
        assertTrue(compile(false).contains("CompressionSibling"));
        String lua = compile(true);
        for (String name : new String[]{"CompressionSibling", "compressionField", "compressionUpdate", "compressionCall"}) {
            assertFalse("Uncompressed Lua name: " + name, lua.contains(name));
        }
        var call = Pattern.compile("return [A-Za-z_][A-Za-z_0-9]*\\[[^\\n]+?\\]\\.([A-Za-z_][A-Za-z_0-9]*)\\(").matcher(lua);
        assertTrue("Expected virtual dispatch", call.find());
        String slot = call.group(1);
        assertTrue("Expected a compressed slot: " + slot, slot.length() <= 3);
        var bindings = Pattern.compile("(?m)^\\s*[A-Za-z_][A-Za-z_0-9]*\\." + Pattern.quote(slot) + " = ").matcher(lua);
        int count = 0;
        while (bindings.find()) count++;
        assertTrue("All four siblings must bind the called slot", count >= 4);
        assertEquals(lua, compile(true));
        assertTrue("Preserved function", lua.contains("function u("));
        assertTrue("Preserved global", lua.contains("w = 7"));
    }

    @Test
    public void compressedDispatchExecutesOnBothTargets() {
        test().testLua(true).luaOnly(false).optimize().inline().localOptimizations().executeProg().lines(source());
    }

    @Test
    public void compressedDescriptorsKeepRuntimeMetadata() {
        test().testLua(true).luaOnly(false).optimize().inline().localOptimizations().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class CompressionMetadata",
            "class CompressionMetadataChild extends CompressionMetadata",
            "@noinline function checkMetadata(CompressionMetadata value) returns boolean",
            "    return value instanceof CompressionMetadataChild and value.typeId != 0",
            "init",
            "    if checkMetadata(new CompressionMetadataChild())",
            "        testSuccess()");
    }

    @Test
    public void compressionKeepsNativeAndStringCallbackNames() {
        test().testLua(true).luaOnly(false).optimize().inline().localOptimizations().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "@extern native ExecuteFunc(string name)",
            "@extern native I2S(int value) returns string",
            "function namedCompressionCallback()",
            "    if I2S(27) == \"27\"",
            "        testSuccess()",
            "init",
            "    ExecuteFunc(\"namedCompressionCallback\")");
    }
}
