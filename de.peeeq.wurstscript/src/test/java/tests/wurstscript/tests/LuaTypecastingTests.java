package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.AssertJUnit.*;


public class LuaTypecastingTests extends WurstScriptTest {

    @Test
    public void hashMap1() throws IOException {
        String[] code = {
            "package Test",
            "",
            "import HashMap",
            "import ClosureTimers",
            "",
            "class A",
            "",
            "constant intMap = new HashMap<int,int>()",
            "constant realMap = new HashMap<real,real>()",
            "constant boolMap = new HashMap<bool,bool>()",
            "constant strMap = new HashMap<string,string>()",
            "constant unitMap = new HashMap<unit,unit>()",
            "constant classMap = new HashMap<A,A>()",
            "",
            "init",
            "	doAfter(0.0) -> ",
            "		foo()",
            "",
            "@compiletime",
            "function foo()",
            "	strMap.put(\"a\", \"some string\")",
            "	print(strMap.get(\"a\"))",
            "	let aString = strMap.get(\"a\")",
            "	var s = \"hjgf\" + strMap.get(\"a\")		// str + any",
            "	print(s)",
            "	s = \"hjgf\" + aString				// str + str",
            "	print(s)",
            "	s = strMap.get(\"a\") + strMap.get(\"a\")	// any + any",
            "	print(s)",
            "",
            "	if not compiletime",
            "	    let u = unitMap.get(null)",
            "	    print(u.getName())",
            "	var i = intMap.get(0)",
            "	print(i)",
            "	var r = realMap.get(0.0)",
            "	print(r)",
            "	var b = boolMap.get(true)",
            "	print(b)",
            "",
            "	if not compiletime",
            "		unitMap.put(null, createUnit(players[0], 'hfoo', vec2(0,0), angle(0)))",
            "		let u = unitMap.get(null)",
            "		print(u.getName())",
            "	intMap.put(4,8)",
            "	i = intMap.get(4)",
            "	print(i)",
            "	realMap.put(3.7754, 591.53)",
            "	r = realMap.get(3.7754)",
            "	print(r)",
            "	boolMap.put(true, true)",
            "	b = boolMap.get(true)",
            "	print(b)"
        };
        try {
            test().testLua(true).withStdLib().executeProg(false).lines(code);
        } catch (Error e) {
            if (!e.getMessage().equals("Succeed function not called")) {
                throw e;
            }
        }
    }

}

