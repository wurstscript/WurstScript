package tests.utils;

import de.peeeq.wurstscript.utils.Lazy;
import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.utils.Pair;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.expectThrows;

public class CoreUtilitiesTests {
    @Test
    public void lazySupplierRunsExactlyOnceIncludingNullValues() {
        AtomicInteger calls = new AtomicInteger();
        Lazy<String> lazy = Lazy.create(() -> {
            calls.incrementAndGet();
            return null;
        });

        assertEquals(lazy.get(), null);
        assertEquals(lazy.get(), null);
        assertEquals(calls.get(), 1);
    }

    @Test
    public void notNullListRejectsNullThroughEveryMutationPath() {
        NotNullList<String> values = new NotNullList<>();
        values.add("a");
        values.add(1, "b");
        values.addAll(List.of("c", "d"));
        values.addAll(1, List.of("x"));
        values.set(0, "z");
        assertEquals(values, List.of("z", "x", "b", "c", "d"));

        expectThrows(IllegalArgumentException.class, () -> values.add(null));
        expectThrows(IllegalArgumentException.class, () -> values.add(0, null));
        expectThrows(IllegalArgumentException.class, () -> values.addAll(Arrays.asList("ok", null)));
        expectThrows(IllegalArgumentException.class, () -> values.addAll(0, Arrays.asList((String) null)));
        expectThrows(IllegalArgumentException.class, () -> values.set(0, null));
    }

    @Test
    public void pairProvidesValueEqualityAndAccessors() {
        Pair<String, Integer> first = Pair.create("value", 7);
        Pair<String, Integer> equal = Pair.create("value", 7);
        Pair<String, Integer> different = Pair.create("other", 7);

        assertEquals(first.getA(), "value");
        assertEquals(first.getB(), 7);
        assertEquals(first.toString(), "(value, 7)");
        assertEquals(first, equal);
        assertEquals(first.hashCode(), equal.hashCode());
        assertFalse(first.equals(different));
        assertEquals(Pair.create(null, 7), Pair.create(null, 7));
        assertFalse(first.equals("value"));
    }
}
