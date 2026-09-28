package tests.wurstscript.tests;

import de.peeeq.datastructures.Worklist;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class WorklistTests {

    @Test
    public void deduplicatesPendingItemsAndAllowsRequeueAfterPolling() {
        Worklist<Integer> worklist = new Worklist<>(List.of(1, 2, 1));
        Assert.assertEquals(worklist.size(), 2);
        Assert.assertFalse(worklist.isEmpty());
        Assert.assertEquals(worklist.poll().intValue(), 1);
        worklist.add(2); // still pending, must not be duplicated
        worklist.add(3);
        Assert.assertEquals(worklist.poll().intValue(), 2);
        worklist.add(1); // already consumed, so requeueing is valid
        Assert.assertEquals(worklist.poll().intValue(), 3);
        Assert.assertEquals(worklist.poll().intValue(), 1);
        Assert.assertTrue(worklist.isEmpty());
    }

    @Test
    public void addAllMaintainsFifoOrder() {
        Worklist<String> worklist = new Worklist<>();
        worklist.addAll(List.of("a", "b", "a"));
        Assert.assertEquals(worklist.poll(), "a");
        Assert.assertEquals(worklist.poll(), "b");
        Assert.assertTrue(worklist.isEmpty());
    }
}
