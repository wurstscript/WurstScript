package tests.utils;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import de.peeeq.datastructures.TransitiveClosure;
import de.peeeq.wurstio.languageserver.WFile;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;

/**
 *
 */
public class TransitiveClosureTests {


    @Test
    public void simpleExample() {
        Multimap<Integer, Integer> m = LinkedHashMultimap.create();


        m.put(1, 3);
        m.put(1, 4);
        m.put(3, 2);
        m.put(3, 5);
        m.put(4, 5);
        m.put(4, 7);
        m.put(7, 8);
        m.put(6, 1);
        m.put(6, 9);

        TransitiveClosure<Integer> c = new TransitiveClosure<>(m);

        List<Integer> from1 = c.get(1).collect(Collectors.toList());
        assertEquals(from1, Arrays.asList(3, 2, 5, 4, 7, 8), "from1 = " + from1);

    }
}
