package de.peeeq.datastructures;

import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.utils.Utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class TransitiveClosure<T> {
    private final Multimap<T, T> base;

    public TransitiveClosure(Multimap<T, T> base) {
        this.base = base;
    }

    /**
     * returns all elements reachable from start (start is only included if it is reachable with > 0 steps)
     */
    public List<T> getAsList(T start) {
        return get(start).collect(Collectors.toList());
    }

    /**
     * returns all elements reachable from start (start is only included if it is reachable with > 0 steps)
     */
    public Stream<T> get(T start) {
        Set<T> visited = new HashSet<>();
        return base.get(start).stream()
                        .flatMap(c -> visit(c, visited));
    }

    private Stream<T> visit(T start, Set<T> visited) {
        if (visited.contains(start)) {
            return Stream.empty();
        }
        visited.add(start);
        return Stream.concat(Stream.of(start),
                base.get(start).stream()
                        .flatMap(c -> visit(c, visited)));
    }

}
