package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class IntRange implements Iterable<Integer> {
  final int start;
  final int end;

  public IntRange(int start, int end) {
    this.start = start;
    this.end = end;
  }

  @Override
  public String toString() {
    return start + "..<" + end;
  }

  public int size() {
    return end - start;
  }

  @Override
  public Iterator<Integer> iterator() {
    return new Iterator<Integer>() {
      int i = start;

      @Override
      public boolean hasNext() {
        return i < end;
      }

      @Override
      public Integer next() {
        return i++;
      }

      @Override
      public void remove() {
        throw new Error("Ranges are immutable");
      }
    };
  }

  public static List<IntRange> createFromIntList(List<Integer> list) {
    ArrayList<Integer> l = Lists.newArrayList(list);
    Collections.sort(l);
    List<IntRange> result = Lists.newArrayList();

    boolean start = true;
    int min = 0;
    int max = 0;
    for (int i : l) {
      if (start) {
        min = i;
        max = i;
        start = false;
      } else {
        if (i == max + 1) {
          max = i;
        } else {
          result.add(new IntRange(min, max));
          min = i;
          max = i;
        }
      }
    }
    if (!start) {
      result.add(new IntRange(min, max));
    }

    return result;
  }
}
