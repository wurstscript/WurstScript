package tests.utils;

import de.peeeq.datastructures.UnionFind;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class UnionFindTests {

    @Test
    public void unionKeepsTheTallerTreeAsRoot() {
        UnionFind<String> uf = new UnionFind<>();
        // a and b form a tree of rank 1 with root a
        uf.union("a", "b");
        // the singleton c has rank 0, so it goes below a, whichever side of the union it is on
        uf.union("c", "a");
        assertEquals(uf.find("c"), "a");
        assertEquals(uf.find("b"), "a");
    }

    @Test
    public void unionOfEqualRanksMergesTheSecondIntoTheFirst() {
        UnionFind<String> uf = new UnionFind<>();
        uf.union("a", "b");
        uf.union("c", "d");
        uf.union("c", "a");
        assertEquals(uf.find("a"), "c");
        assertEquals(uf.find("b"), "c");
        assertEquals(uf.find("d"), "c");
    }
}
