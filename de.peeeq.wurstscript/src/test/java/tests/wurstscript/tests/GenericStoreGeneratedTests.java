package tests.wurstscript.tests;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Random;

/** Seeded source programs checked against a Java model, through the emitted backends. */
public class GenericStoreGeneratedTests extends WurstScriptTest {
    @DataProvider
    public Object[][] seeds() {
        var cases = new ArrayList<Object[]>();
        for (int seed = 0; seed < 12; seed++) {
            for (boolean optimized : new boolean[]{false, true}) cases.add(new Object[]{seed, optimized});
        }
        return cases.toArray(Object[][]::new);
    }

    @Test(dataProvider = "seeds")
    public void modeledStoreOperations(int seed, boolean optimized) {
        var config = testNamed("storeSeed" + seed + "Optimized" + optimized)
            .testLua(true).luaOnly(false).executeProg();
        if (optimized) config.optimize().inline().localOptimizations();
        config.lines(program(seed).split("\n"));
    }

    @Test(dataProvider = "seeds")
    public void deadDispatchDoesNotAddStaticInitializerEffects(int seed, boolean optimized) {
        int liveCount = 1 + new Random(seed).nextInt(3);
        StringBuilder allocations = new StringBuilder();
        String[] types = {"int", "string", "bool"};
        for (int i = 0; i < liveCount; i++) {
            allocations.append("    let store").append(i).append(" = new Store<").append(types[i]).append(">()\n");
        }
        // The generic erased initializer is specific to Lua; the operation model above checks Jass too.
        String source = """
            package Test
            native testSuccess()
            native testFail(string message)
            int initialized = 0
            function initialize<T:>() returns T
                initialized++
                return null
            class Store<T:>
                static T value = initialize<T>()
            interface Runner
                function run()
            class Live implements Runner
                override function run()
            class Dead<T:> implements Runner
                override function run()
                    let store = new Store<real>()
            class DeadChild<U:> extends Dead<U>
            init
            %s
                Runner runner = new Live()
                runner.run()
                if initialized == %d
                    testSuccess()
                else
                    testFail("dead dispatch changed static initializer effects")
            """.formatted(allocations, liveCount);
        var config = testNamed("staticSeed" + seed + "Optimized" + optimized).testLua(true).executeProg();
        if (optimized) config.optimize().inline().localOptimizations();
        config.lines(source.split("\n"));
    }

    static String program(int seed) {
        StringBuilder source = new StringBuilder("""
            package Test
            native testSuccess()
            native testFail(string message)
            int total = 0
            tuple Pair(int x, int y)
            interface Action
                function run()
            function invoke(Action action)
                action.run()
                destroy action
            class Store<T:>
                static T array values
                static int next = 0
                int slot
                construct()
                    slot = next
                    next++
                function put(T value)
                    values[slot] = value
                function read() returns T
                    return values[slot]
                function pop() returns T
                    let value = values[slot]
                    values[slot] = null
                    return value
            interface Source
                function put(Observer value)
                function changed()
                function pop() returns Observer
            class Observer
                function put(Observer value)
                function changed()
                function pop() returns Observer
                    return null
            class Subscriber extends Observer
                int weight
                construct(int weight)
                    this.weight = weight
                override function changed()
                    total += weight
            class Box<T:> extends Observer implements Source
                T value
                Store<Observer> entries = new Store<Observer>()
                construct(T initial)
                    value = initial
                override function put(Observer value)
                    entries.put(value)
                override function changed()
                    let value = entries.read()
                    if value != null
                        value.changed()
                override function pop() returns Observer
                    return entries.pop()
                ondestroy
                    let value = entries.pop()
                    if value != null
                        value.changed()
                    destroy entries
            class Child<T:> extends Box<T>
                construct(T value)
                    super(value)
            init
                let box0 = new Box<int>(7)
                let box1 = new Box<string>("one")
                let box2 = new Child<int>(9)
                let box3 = new Box<Pair>(Pair(1, 2))
                Source source0 = box0
                Source source1 = box1
                Source source2 = box2
                Source source3 = box3
                Observer observer0 = box0
                Observer observer1 = box1
                Observer observer2 = box2
                Observer observer3 = box3
            """);
        Random random = new Random(0x53544f5245L + seed);
        int[] contents = new int[4];
        int expected = 0;
        for (int operation = 0; operation < 32; operation++) {
            int box = random.nextInt(contents.length);
            String receiver = new String[]{"box", "source", "observer"}[random.nextInt(3)] + box;
            switch (random.nextInt(4)) {
                case 0 -> {
                    int weight = random.nextInt(99) + 1;
                    source.append("    ").append(receiver).append(".put(new Subscriber(").append(weight).append("))\n");
                    contents[box] = weight;
                }
                case 1 -> {
                    source.append("    ").append(receiver).append(".changed()\n");
                    expected += contents[box];
                }
                case 2 -> {
                    source.append("    let removed").append(operation).append(" = ").append(receiver).append(".pop()\n")
                        .append("    if removed").append(operation).append(" != null\n")
                        .append("        removed").append(operation).append(".changed()\n");
                    expected += contents[box];
                    contents[box] = 0;
                }
                default -> {
                    source.append("    invoke(() -> ").append(receiver).append(".changed())\n");
                    expected += contents[box];
                }
            }
            source.append("    if total != ").append(expected).append("\n")
                .append("        testFail(\"seed ").append(seed).append(" operation ").append(operation).append("\")\n");
        }
        for (int box = 0; box < contents.length; box++) {
            source.append("    destroy box").append(box).append('\n');
            expected += contents[box];
        }
        source.append("    if total == ").append(expected).append("\n        testSuccess()\n")
            .append("    else\n        testFail(\"destruction total\")\n");
        return source.toString();
    }
}
