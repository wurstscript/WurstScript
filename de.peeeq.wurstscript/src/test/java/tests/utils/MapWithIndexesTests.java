package tests.utils;

import de.peeeq.wurstscript.utils.MapWithIndexes;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MapWithIndexesTests {
    private record Item(String group, Set<String> tags, boolean active) {
    }

    @Test
    public void indexesStayConsistentWhenValuesAreReplaced() {
        MapWithIndexes<String, Item> items = new MapWithIndexes<>();
        MapWithIndexes.Index<String, String> groups = items.createIndex(Item::group);
        MapWithIndexes.PredIndex<String> active = items.createPredicateIndex(Item::active);
        MapWithIndexes.Index<String, String> tags = items.createMultiIndex(Item::tags);

        items.put("one", new Item("red", Set.of("warm", "bright"), true));
        assertEquals(groups.lookup("red"), List.of("one"));
        assertEquals(active.lookup(), List.of("one"));
        assertEquals(tags.lookup("warm"), List.of("one"));

        items.put("one", new Item("blue", Set.of("cold"), false));
        assertTrue(groups.lookup("red").isEmpty());
        assertEquals(groups.lookup("blue"), List.of("one"));
        assertTrue(active.lookup().isEmpty());
        assertTrue(tags.lookup("warm").isEmpty());
        assertEquals(tags.lookup("cold"), List.of("one"));
    }

    @Test
    public void removeAllAndClearUpdateEveryIndex() {
        MapWithIndexes<String, Item> items = new MapWithIndexes<>();
        MapWithIndexes.Index<String, String> groups = items.createIndex(Item::group);
        items.put("one", new Item("red", Set.of(), true));
        items.put("two", new Item("red", Set.of(), true));
        items.put("three", new Item("blue", Set.of(), true));

        items.removeAll(List.of("one", "three"));
        assertEquals(items.keySet(), Set.of("two"));
        assertEquals(groups.lookup("red"), List.of("two"));
        assertTrue(groups.lookup("blue").isEmpty());

        items.clear();
        assertTrue(items.isEmpty());
        assertTrue(groups.lookup("red").isEmpty());
    }
}
