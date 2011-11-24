package de.peeeq.immutablecollections;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ImmutableListTest {

    @Parameterized.Parameters
    public static Collection<Object[]> listsToTest() {
    	Collection<Object[]> result = new LinkedList<Object[]>();
    	for (int i = 0; i<100; i++) {
    		Object[] a = {randomList(150)};
			result.add(a); 
    	}
		return result;
    }
	
	
	private static ImmutableList<Integer> randomList(int maxSize) {
		Random r = new Random();
		if (maxSize > 4 && r.nextBoolean()) {
			return randomList(maxSize / 2).cons(randomList(maxSize / 2));
		} else {
			ImmutableList<Integer> list = ImmutableList.emptyList();
	    	
	    	int size = r.nextInt(10);
	    	for (int i=0; i<size; i++) {
	    		int toAdd = r.nextInt(100);
	    		if (r.nextBoolean()) {
	    			list = list.appFront(toAdd);
	    		} else {
	    			list = list.appBack(toAdd);
	    		}
	    	}
	    	return list;
		}
	}


	private ImmutableList<Integer> list;
	private static int count = 0;


	public ImmutableListTest(ImmutableList<Integer> list) {
		this.list = list;
		System.out.println("List " + ++count + " = " + list);
	}
	
	@Test
	public void sizeOfTail() {
		if (list.size() > 0) {
			assertEquals(list.size() - 1, list.tail().size());
		}
	}
	
	@Test
	public void sizeOfCons() {
		ImmutableList<Integer> otherList = randomList(10);
		assertEquals(list.size() + otherList.size(), list.cons(otherList).size());
	}
	
	@Test
	public void iterator1() {
		int count = 0;
		for (Integer i : list) {
			assertNotNull(i);
			count++;
		}
		assertEquals(count, list.size());
	}
	

}
