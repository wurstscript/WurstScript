package tests.wurstscript.utils;



import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Function;

import de.peeeq.wurstscript.utils.TopsortCycleException;
import de.peeeq.wurstscript.utils.Utils;

public class UtilsTest {

  @Test
  public void array() {
    int[] ar1 = {1,2,3};
    int[] ar2 = {1,2,3};
    Assert.assertArrayEquals(ar2, ar1);
  }

  @Test
  public void filter() {
	  
    List<Integer> list = Utils.list(1,2,3,4,5,6,7,8);
	List<Integer> list2 = Utils.filter(list , new Function<Integer, Boolean>() {

		@Override
		public Boolean apply(Integer input) {
			return input % 3 == 0;
		}
    	
    });
	Assert.assertEquals(8, list.size());
	Assert.assertEquals(2, list2.size());
	Object[] ar = {3, 6};
	Assert.assertArrayEquals(ar, list2.toArray());
  }
  
  @Test
  public void topSort_1() throws TopsortCycleException {
    List<Integer> toSort = Utils.list(2,1,3);
    List<Integer> sorted = Utils.topSort(toSort, new Function<Integer,Collection<Integer>>() {

		@Override
		public Collection<Integer> apply(Integer input) {
			Collection<Integer> result = new LinkedList<Integer>();
			for (int i = 1; i<=3; i++) {
				if (i > input) {
					result.add(i);
				}
			}
			return result ;
		}
    	
    });
    
    Object[] ar = {3, 2, 1};
	Assert.assertArrayEquals(ar , sorted.toArray());
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
  public void testAvgSubseqLen() {
	  Assert.assertEquals(2.5, Utils.averageSubsequenceLength("KilUn", "KillUnit"), 0.05);
	  Assert.assertEquals(2.0, Utils.averageSubsequenceLength("llUt", "KillUnit"), 0.05);
	  Assert.assertEquals(1.33, Utils.averageSubsequenceLength("llnt", "KillUnit"), 0.05);
	  
	  Assert.assertEquals(5.0, Utils.averageSubsequenceLength("light", "LoadLightning".toLowerCase()), 0.05);
	  Assert.assertEquals(2.5, Utils.averageSubsequenceLength("light", "GetUnitFlyHeight".toLowerCase()), 0.05);
	  
  }
  
  @Test
  public void testSubseqLen() {
	  
	  Assert.assertEquals(Utils.list(3, 2), 
			  Utils.subsequenceLengthes("KilUn", "KillUnit"));
	  
	  Assert.assertEquals(Utils.list(6), 
			  Utils.subsequenceLengthes("abc123", "abcfabc123"));
	  
	  Assert.assertEquals(Utils.list(3,1), 
			  Utils.subsequenceLengthes("illu", "getkillingunit"));
	  
	  Assert.assertEquals(Utils.list(4), 
			  Utils.subsequenceLengthes("illu", "isunitillusion"));
	  
  }
  
  @Test
  public void joinArrays() {
	  String[] ar1 = {"a","b"};
	  String[] ar2 = {"c","d","e"};
	  String[] ar3 = {"a","b","c","d","e"};
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
