package de.peeeq.immutablecollections;

import static org.junit.Assert.*;
import de.peeeq.immutablecollections.*;
import org.junit.Test;

public class ImmutableListTest {

	@Test
	public void test1() {
		ImmutableList<Integer> list = ImmutableList.emptyList();
		list = list.appFront(1);
		list = list.appFront(2);
		list = list.appFront(3);
		assertEquals(3, list.size());
		assertEquals(3, (int)list.head());
	}

	@Test
	public void testIterator() {
		ImmutableList<Integer> list = ImmutableList.emptyList();
		list = list.appFront(1);
		list = list.appFront(2);
		list = list.appFront(3);
		list = list.appFront(4);
		list = list.appFront(5);
		String con = "";
		for (int i : list) {
			con+=i;
		}
		assertEquals("54321", con);
		
	}
	

	

	@Test
	public void testCons() {
		ImmutableList<Integer> list1 = ImmutableList.of(1,2,3,4);
		ImmutableList<Integer> list2 = ImmutableList.of(5,6,7,8);
		ImmutableList<Integer> list3 = list1.cons(list2);
		String con = "";
		for (int i : list3) {
			con+=i;
		}
		assertEquals("12345678", con);
		
	}
	
	@Test
	public void testCons2() {
		ImmutableList<Integer> list1 = ImmutableList.of(1,2);
		ImmutableList<Integer> list2 = ImmutableList.of();
		ImmutableList<Integer> list3 = ImmutableList.of(3,4,5);
		ImmutableList<Integer> list4 = ImmutableList.of(6);
		ImmutableList<Integer> list5 = ImmutableList.of(7);
		ImmutableList<Integer> list6 = list1.cons(list2).cons(list3).cons(list4).cons(list5);
		String con = "";
		for (int i : list6) {
			con+=i;
		}
		assertEquals("1234567", con);
		
	}
	
	
}
