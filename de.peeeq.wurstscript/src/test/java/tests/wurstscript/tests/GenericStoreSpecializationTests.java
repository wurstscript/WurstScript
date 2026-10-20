package tests.wurstscript.tests;

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import static org.testng.AssertJUnit.*;

public class GenericStoreSpecializationTests extends WurstScriptTest {
    private static final String[] SOURCE = """
        package Test
        native testSuccess()
        native testFail(string message)
        int notifications = 0
        class Store<T:>
            static T array values
            static int next = 0
            int slot
            construct()
                slot = next
                next++
            function add(T value)
                values[slot] = value
            function get(int index) returns T
                return values[slot + index]
        interface Source
            function subscribe(Observer value)
        class Observer
            function changed()
        class Subscriber extends Observer
            override function changed()
                notifications++
        class Box<T:> extends Observer implements Source
            T value
            Store<Observer> subscribers = new Store<Observer>()
            construct(T initial)
                value = initial
            override function subscribe(Observer value)
                subscribers.add(value)
            override function changed()
                subscribers.get(0).changed()
        init
            let box = new Box<int>(7)
            Source source = box
            source.subscribe(new Subscriber())
            box.changed()
            if notifications == 1
                testSuccess()
            else
                testFail("subscriber was not notified")
        """.split("\n");

    @Test public void plainLua() throws java.io.IOException {
        test().testLua(true).executeProg().lines(SOURCE);
        String lua = compiledLua("plainLua");
        assertTrue("The concrete observer store must be emitted", lua.contains("Store_values_Observer_"));
        assertFalse("No erased copy of the same store may survive", lua.contains("Store_values["));
    }

    @Test public void optimizedLua() {
        test().testLua(true).optimize().inline().localOptimizations().executeProg().lines(SOURCE);
    }

    @DataProvider
    public Object[][] dispatchCases() {
        java.util.List<Object[]> cases = new java.util.ArrayList<>();
        for (boolean optimized : new boolean[]{false, true}) {
            for (int writer = 0; writer < 3; writer++) {
                for (int reader = 0; reader < 3; reader++) cases.add(new Object[]{optimized, writer, reader});
            }
        }
        return cases.toArray(Object[][]::new);
    }

    @Test(dataProvider = "dispatchCases")
    public void dispatchAndDestructionShareConcreteStores(boolean optimized, int writer, int reader) {
        String source = """
            package Test
            native testSuccess()
            native testFail(string message)
            int total = 0
            class Store<T:>
                static T array values
                static int next = 0
                int slot
                construct()
                    slot = next
                    next++
                function put(T value)
                    values[slot] = value
                function take() returns T
                    let value = values[slot]
                    values[slot] = null
                    return value
                function read() returns T
                    return values[slot]
            interface Source
                function subscribe(Observer value)
                function changed()
            class Observer
                function subscribe(Observer value)
                function changed()
            class Subscriber extends Observer
                int weight
                construct(int weight)
                    this.weight = weight
                override function changed()
                    total += weight
            class Box<T:> extends Observer implements Source
                T value
                Store<Observer> subscribers = new Store<Observer>()
                construct(T initial)
                    value = initial
                override function subscribe(Observer value)
                    subscribers.put(value)
                override function changed()
                    subscribers.read().changed()
                ondestroy
                    subscribers.take().changed()
                    destroy subscribers
            function put(Source source, Observer observer)
                source.subscribe(observer)
            function notify(Observer observer)
                observer.changed()
            init
                let first = new Box<int>(7)
                let second = new Box<string>("different owner type")
                Source firstSource = first
                Source secondSource = second
                Observer firstObserver = first
                Observer secondObserver = second
                %s
                %s
                destroy first
                destroy second
                if total == 26
                    testSuccess()
                else
                    testFail("collection contents or destruction callback changed")
            """.formatted(switch (writer) {
                case 0 -> "first.subscribe(new Subscriber(3))\n    second.subscribe(new Subscriber(10))";
                case 1 -> "firstSource.subscribe(new Subscriber(3))\n    secondSource.subscribe(new Subscriber(10))";
                default -> "put(firstSource, new Subscriber(3))\n    put(secondSource, new Subscriber(10))";
            }, switch (reader) {
                case 0 -> "first.changed()\n    second.changed()";
                case 1 -> "firstSource.changed()\n    secondSource.changed()";
                default -> "notify(firstObserver)\n    notify(secondObserver)";
            });
        var config = testNamed("dispatchStores" + optimized + writer + reader)
            .testLua(true).luaOnly(false).executeProg();
        if (optimized) config.optimize().inline().localOptimizations();
        config.lines(source.split("\n"));
    }

}
