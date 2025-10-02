package tests.wurstscript.utils;


import de.peeeq.wurstscript.utils.TopsortCycleException;
import de.peeeq.wurstscript.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class UtilsTest {

    @Test
    public void array() {
        int[] ar1 = {1, 2, 3};
        int[] ar2 = {1, 2, 3};
        Assert.assertEquals(ar2, ar1);
    }


    @Test
    public void topSort_1() throws TopsortCycleException {
        List<Integer> toSort = Utils.list(2, 1, 3);
        List<Integer> sorted = Utils.topSort(toSort, input -> {
            Collection<Integer> result = new LinkedList<Integer>();
            for (int i = 1; i <= 3; i++) {
                if (i > input) {
                    result.add(i);
                }
            }
            return result;
        });

        Object[] ar = {3, 2, 1};
        Assert.assertEquals(ar, sorted.toArray());
    }

    @Test
    public void testSubseq() {
        Assert.assertFalse(Utils.isSubsequenceIgnoreCase("aaa", "aa"));
        Assert.assertTrue(Utils.isSubsequenceIgnoreCase("abc", "1a2b3c45"));
        Assert.assertTrue(Utils.isSubsequenceIgnoreCase("aaa", "aaaa"));
        Assert.assertTrue(Utils.isSubsequenceIgnoreCase("aaa", "aaa"));
        Assert.assertTrue(Utils.isSubsequenceIgnoreCase("aaa", "abaa"));
        Assert.assertTrue(Utils.isSubsequenceIgnoreCase("d", "OrderId2StringBJ"));
    }


    @Test
    public void testSubseqLen() {

        Assert.assertEquals(Utils.list(3, 2),
                Utils.subsequenceLengthes("KilUn", "KillUnit"));

        Assert.assertEquals(Utils.list(6),
                Utils.subsequenceLengthes("abc123", "abcfabc123"));

        Assert.assertEquals(Utils.list(3, 1),
                Utils.subsequenceLengthes("illu", "getkillingunit"));

        Assert.assertEquals(Utils.list(4),
                Utils.subsequenceLengthes("illu", "isunitillusion"));

    }

    @Test
    public void joinArrays() {
        String[] ar1 = {"a", "b"};
        String[] ar2 = {"c", "d", "e"};
        String[] ar3 = {"a", "b", "c", "d", "e"};
        Assert.assertEquals(ar3, Utils.joinArrays(ar1, ar2));
    }

/* TODO utils unit tests
  @Test
  public void isJassCode() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void join() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void list() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void map() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void parseAsciiInt1() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void parseAsciiInt4() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void parseHexInt() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void parseInt() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void printIndent() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void printSepStringBuilderStringTFunctionTString() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void printSepStringBuilderStringT() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void printSepStringBuilderStringIterableTFunctionTString() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void printSepStringString() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void removeDuplicates() {
    throw new RuntimeException("Test not implemented");
  }

  @Test
  public void sleep() {
    throw new RuntimeException("Test not implemented");
  }

  */
}
