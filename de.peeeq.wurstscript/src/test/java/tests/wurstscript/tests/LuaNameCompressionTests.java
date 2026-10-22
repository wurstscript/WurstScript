package tests.wurstscript.tests;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import org.testng.annotations.Test;
import org.wurstscript.projectconfig.WurstProjectConfigData;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        assertSiblingBindings(lua, slot);
        assertEquals(lua, compile(true));
        assertTrue("Preserved function", lua.contains("function u("));
        assertTrue("Preserved global", lua.contains("w = 7"));
    }

    private void assertSiblingBindings(String lua, String slot) {
        String identifier = "[A-Za-z_][A-Za-z_0-9]*";
        // Each sibling's unique field initializer identifies its allocated descriptor and field
        // table even after all names are compressed. A shared allocator(descriptor) names the
        // descriptor at the allocation, a class's own allocator() in its body.
        var allocations = Pattern.compile("(?m)^\\s*(" + identifier + ") = (" + identifier
            + ")\\((" + identifier + ")?\\)\\s*\\R\\s*(" + identifier
            + ")\\[\\1\\] = ([1-4])\\b").matcher(lua);
        Map<Integer, String> descriptors = new HashMap<>();
        Set<String> implementations = new HashSet<>();
        Set<String> noOps = new HashSet<>();
        var noOpFunctions = Pattern.compile("(?m)^function (" + identifier
            + ")\\([^\\r\\n]*\\)\\s+return 0\\s+end\\b").matcher(lua);
        while (noOpFunctions.find()) noOps.add(noOpFunctions.group(1));
        while (allocations.find()) {
            int sibling = Integer.parseInt(allocations.group(5));
            String descriptor = allocations.group(3);
            if (descriptor == null) {
                // the allocator records the object's descriptor before any field default
                var recorded = Pattern.compile("(?m)^function " + Pattern.quote(allocations.group(2))
                    + "\\(\\)(?:(?!\\nend)[\\s\\S])*?^\\s*" + identifier + "\\[" + identifier + "\\] = ("
                    + identifier + ")\\s*$").matcher(lua);
                assertTrue("Find the descriptor sibling " + sibling + "'s allocator records", recorded.find());
                descriptor = recorded.group(1);
            }
            assertNull("One allocation anchor per sibling " + sibling, descriptors.put(sibling, descriptor));
            String storage = allocations.group(4);
            var override = Pattern.compile("(?m)^function (" + identifier + ")\\((" + identifier
                + ")[^\\r\\n]*\\)\\s+return " + Pattern.quote(storage) + "\\[\\2\\]\\s+end\\b").matcher(lua);
            assertTrue("Find sibling " + sibling + " override reading its own field", override.find());
            String implementation = override.group(1);
            assertTrue("Each sibling has its own override", implementations.add(implementation));
            var binding = Pattern.compile("(?m)^\\s*" + Pattern.quote(descriptor + "." + slot)
                + " = (" + identifier + ")\\s*$").matcher(lua);
            assertTrue("Sibling " + sibling + " binds the actual dispatched slot", binding.find());
            assertFalse("Sibling " + sibling + " must not bind a no-op base", noOps.contains(binding.group(1)));
            assertEquals("Sibling " + sibling + " binds its own override", implementation, binding.group(1));
        }
        assertEquals("Identify all four concrete siblings", Set.of(1, 2, 3, 4), descriptors.keySet());
        assertEquals("Each sibling has its own descriptor", 4, new HashSet<>(descriptors.values()).size());
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
