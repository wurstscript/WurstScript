package de.peeeq.immutablecollections;

import static org.junit.Assert.*;

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
	

}
