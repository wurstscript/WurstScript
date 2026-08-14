package tests.wurstscript.tests;

import org.testng.annotations.Test;

/**
 * Tests for type class bounds on new-style generics:
 * <p>
 * - an ordinary generic {@code interface} declares the requirements,
 * - a top level {@code instance} block binds those requirements to one concrete type,
 * - a bound {@code <T: Iface>} makes the requirements available inside the generic body,
 * - {@code T.method(args)} dispatches through the instance chosen for {@code T}.
 */
public class TypeClassTests extends WurstScriptTest {

    @Test
    public void parseInstanceDecl() {
        testAssertOkLines(false,
            "package test",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "class A",
            "instance ToIndex<A>",
            "    function toIndex(A x) returns int",
            "        return 42"
        );
    }
}
