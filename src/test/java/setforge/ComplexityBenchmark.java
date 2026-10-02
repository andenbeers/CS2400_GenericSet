package setforge;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Not a JUnit test (name does not end in "Test", so Surefire skips it).
 * Counts equals() calls, which is the dominant cost in both representations, and reports
 * the doubling ratio count(2n)/count(n): ~1 means O(1), ~2 means O(n), ~4 means O(n^2).
 *
 * Run (after mvn test-compile):
 *   java -cp target/classes;target/test-classes setforge.ComplexityBenchmark      (Windows)
 *   java -cp target/classes:target/test-classes setforge.ComplexityBenchmark      (Linux/macOS)
 */
public class ComplexityBenchmark {

    /** Element whose equals() is counted. Distinct objects with the same id are equal. */
    static final class Elem {
        static long compares = 0;
        final int id;

        Elem(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            compares++;
            return o instanceof Elem e && e.id == id;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    static final int[] SIZES = {250, 500, 1000, 2000, 4000};

    static SetInterface<Elem> build(Supplier<SetInterface<Elem>> factory, int from, int count) {
        SetInterface<Elem> s = factory.get();
        for (int i = from; i < from + count; i++) {
            s.add(new Elem(i));
        }
        return s;
    }

    /** Runs op on fresh state built by setup and returns the equals() calls the op alone made. */
    static <S> long count(Supplier<S> setup, Consumer<S> op) {
        S state = setup.get();
        Elem.compares = 0;
        op.accept(state);
        return Elem.compares;
    }

    interface Case {
        long at(int n);
    }

    static void report(String name, Case c) {
        long[] v = new long[SIZES.length];
        StringBuilder row = new StringBuilder(String.format("| %-34s |", name));
        for (int i = 0; i < SIZES.length; i++) {
            v[i] = c.at(SIZES[i]);
            row.append(String.format(" %9d |", v[i]));
        }
        row.append(" ");
        for (int i = 1; i < SIZES.length; i++) {
            row.append(v[i - 1] == 0 ? "n/a" : String.format("%.2f", (double) v[i] / v[i - 1]));
            row.append(i < SIZES.length - 1 ? ", " : "");
        }
        row.append(" |");
        System.out.println(row);
    }

    static void header() {
        StringBuilder h = new StringBuilder("| operation (equals calls)           |");
        StringBuilder d = new StringBuilder("|------------------------------------|");
        for (int n : SIZES) {
            h.append(String.format(" n=%-6d |", n));
            d.append("-----------|");
        }
        h.append(" doubling ratios |");
        d.append("-----------------|");
        System.out.println(h);
        System.out.println(d);
    }

    static void runFor(String impl, Supplier<SetInterface<Elem>> f) {
        System.out.println("\n### " + impl + "\n");
        header();
        // absent probe object / present-at-far-end probe, each a fresh Elem so identity shortcut never fires
        report("add (absent entry)", n -> count(() -> build(f, 0, n), s -> s.add(new Elem(-1))));
        report("add (duplicate of oldest entry)", n -> count(() -> build(f, 0, n), s -> s.add(new Elem(0))));
        report("add (duplicate of newest entry)", n -> count(() -> build(f, 0, n), s -> s.add(new Elem(n - 1))));
        report("contains (absent)", n -> count(() -> build(f, 0, n), s -> s.contains(new Elem(-1))));
        report("contains (oldest entry)", n -> count(() -> build(f, 0, n), s -> s.contains(new Elem(0))));
        report("remove(entry) (absent)", n -> count(() -> build(f, 0, n), s -> s.remove(new Elem(-1))));
        report("remove(entry) (oldest entry)", n -> count(() -> build(f, 0, n), s -> s.remove(new Elem(0))));
        report("remove() (any element)", n -> count(() -> build(f, 0, n), s -> s.remove()));
        report("union, disjoint, |a|=|b|=n", n -> count(
                () -> new SetInterface[] {build(f, 0, n), build(f, n, n)},
                p -> p[0].union(p[1])));
        report("union, identical members", n -> count(
                () -> new SetInterface[] {build(f, 0, n), build(f, 0, n)},
                p -> p[0].union(p[1])));
        report("intersection, disjoint", n -> count(
                () -> new SetInterface[] {build(f, 0, n), build(f, n, n)},
                p -> p[0].intersection(p[1])));
        report("intersection, identical members", n -> count(
                () -> new SetInterface[] {build(f, 0, n), build(f, 0, n)},
                p -> p[0].intersection(p[1])));
        report("difference, disjoint", n -> count(
                () -> new SetInterface[] {build(f, 0, n), build(f, n, n)},
                p -> p[0].difference(p[1])));
        report("difference, identical members", n -> count(
                () -> new SetInterface[] {build(f, 0, n), build(f, 0, n)},
                p -> p[0].difference(p[1])));
        report("toArray (equals calls)", n -> count(() -> build(f, 0, n), SetInterface::toArray));
    }

    /** toArray makes no equals() calls, so its linear cost is shown by median wall-clock time instead. */
    static void timeToArray(String impl, Supplier<SetInterface<Elem>> f) {
        // Filling via add() is O(n^2), so keep sizes modest.
        int[] sizes = {5_000, 10_000, 20_000, 40_000, 80_000};
        long[] m = new long[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            SetInterface<Elem> s = build(f, 0, sizes[i]);
            long[] runs = new long[21];
            for (int r = 0; r < runs.length; r++) {
                long t0 = System.nanoTime();
                Object[] a = s.toArray();
                runs[r] = System.nanoTime() - t0;
                if (a.length != sizes[i]) {
                    throw new AssertionError();
                }
            }
            Arrays.sort(runs);
            m[i] = runs[runs.length / 2];
        }
        StringBuilder row = new StringBuilder("| " + String.format("%-22s", impl) + " |");
        for (long x : m) {
            row.append(String.format(" %9d |", x));
        }
        row.append(" ");
        for (int i = 1; i < m.length; i++) {
            row.append(String.format("%.2f", (double) m[i] / m[i - 1])).append(i < m.length - 1 ? ", " : "");
        }
        row.append(" |");
        System.out.println(row);
    }

    public static void main(String[] args) {
        runFor("ResizableArraySet", ResizableArraySet::new);
        runFor("LinkedSet", LinkedSet::new);

        System.out.println("\n### toArray median time in ns (21 runs; n = 5000, 10000, 20000, 40000, 80000)\n");
        System.out.println("| implementation         |      5000 |     10000 |     20000 |     40000 |     80000 | doubling ratios |");
        System.out.println("|------------------------|-----------|-----------|-----------|-----------|-----------|-----------------|");
        timeToArray("ResizableArraySet", ResizableArraySet::new);
        timeToArray("LinkedSet", LinkedSet::new);
    }
}
