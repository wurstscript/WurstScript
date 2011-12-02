package de.peeeq.immutablecollections;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.common.collect.Lists;

public class ImmutableListTest {

	private static final int TEST_COUNT = 100;


	public static Collection<ImmutableList<Integer>> listsToTest() {
		Collection<ImmutableList<Integer>> result = Lists.newArrayListWithCapacity(TEST_COUNT);
		for (int i = 0; i<TEST_COUNT; i++) {
			result.add(randomList(150)); 
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



	@Test
	public void sizeOfTail() {
		for (ImmutableList<Integer> list : listsToTest()) {
			if (list.size() > 0) {
				assertEquals(list.size() - 1, list.tail().size());
			}
		}
	}

	@Test
	public void sizeOfCons() {
		for (ImmutableList<Integer> list : listsToTest()) {
			ImmutableList<Integer> otherList = randomList(10);
			assertEquals(list.size() + otherList.size(), list.cons(otherList).size());
		}
	}

	@Test
	public void iterator1() {
		for (ImmutableList<Integer> list : listsToTest()) {
			int count = 0;
			for (Integer i : list) {
				assertNotNull(i);
				count++;
			}
			assertEquals(count, list.size());
		}
	}



}
