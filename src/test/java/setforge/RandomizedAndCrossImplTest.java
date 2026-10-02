package setforge;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Randomized sequences. The seed is chosen fresh each run and printed in every assertion
 * message; replay a failure with: mvn test -Dseed=<seed>
 * The oracle is a plain boolean[] membership model (index = value), so no library set is used.
 */
class RandomizedAndCrossImplTest {

    private static final long SEED = Long.getLong("seed", System.nanoTime());
    private static final int OPERATIONS = 2000;
    private static final int RANGE = 40; // small range forces duplicates, hits and misses

    private static String tag() {
        return " [seed=" + SEED + "]";
    }

    private static int count(boolean[] model) {
        int n = 0;
        for (boolean b : model) {
            if (b) {
                n++;
            }
        }
        return n;
    }

    private static void check(SetInterface<Object> s, boolean[] model, String label, int step) {
        String msg = label + " diverged at step " + step + tag();
        assertEquals(count(model), s.getCurrentSize(), msg + " (size)");
        assertEquals(count(model) == 0, s.isEmpty(), msg + " (isEmpty)");
        for (int v = 0; v < RANGE; v++) {
            assertEquals(model[v], s.contains(v), msg + " (contains " + v + ")");
        }
        assertEquals(count(model), s.toArray().length, msg + " (toArray length)");
    }

    private void runAgainstModel(SetInterface<Object> s, String label) {
        Random rnd = new Random(SEED);
        boolean[] model = new boolean[RANGE];
        for (int step = 0; step < OPERATIONS; step++) {
            int v = rnd.nextInt(RANGE);
            String at = " @" + step + tag();
            switch (rnd.nextInt(5)) {
                case 0, 1 -> {
                    assertEquals(!model[v], s.add(v), label + " add " + v + at);
                    model[v] = true;
                }
                case 2 -> {
                    assertEquals(model[v], s.remove(v), label + " remove " + v + at);
                    model[v] = false;
                }
                case 3 -> assertEquals(model[v], s.contains(v), label + " contains " + v + at);
                default -> {
                    Object removed = s.remove();
                    if (count(model) == 0) {
                        assertNull(removed, label + " remove() on empty" + at);
                    } else {
                        assertNotNull(removed, label + " remove() on non-empty" + at);
                        int r = (Integer) removed;
                        assertTrue(model[r], label + " remove() returned non-member " + r + at);
                        model[r] = false;
                    }
                }
            }
            if (rnd.nextInt(150) == 0) {
                s.clear();
                model = new boolean[RANGE];
            }
            check(s, model, label, step);
        }
    }

    @Test
    void resizableArraySetMatchesModel() {
        runAgainstModel(new ResizableArraySet<>(), "ResizableArraySet");
    }

    @Test
    void linkedSetMatchesModel() {
        runAgainstModel(new LinkedSet<>(), "LinkedSet");
    }

    @Test
    void bothImplementationsAgreeOnTheSameSequence() {
        Random rnd = new Random(SEED + 1);
        SetInterface<Object> arr = new ResizableArraySet<>();
        SetInterface<Object> lnk = new LinkedSet<>();
        for (int step = 0; step < OPERATIONS; step++) {
            int v = rnd.nextInt(RANGE);
            String at = " @" + step + tag();
            switch (rnd.nextInt(4)) {
                case 0, 1 -> assertEquals(arr.add(v), lnk.add(v), "add " + v + at);
                case 2 -> assertEquals(arr.remove(v), lnk.remove(v), "remove " + v + at);
                default -> assertEquals(arr.contains(v), lnk.contains(v), "contains " + v + at);
            }
            assertEquals(arr.getCurrentSize(), lnk.getCurrentSize(), "size" + at);
            for (int x = 0; x < RANGE; x++) {
                assertEquals(arr.contains(x), lnk.contains(x), "membership of " + x + at);
            }
        }
    }

    @Test
    void algebraMatchesModelAndAgreesAcrossImplementations() {
        Random rnd = new Random(SEED + 2);
        for (int round = 0; round < 100; round++) {
            SetInterface<Object> a1 = new ResizableArraySet<>(), a2 = new LinkedSet<>();
            SetInterface<Object> b1 = new ResizableArraySet<>(), b2 = new LinkedSet<>();
            boolean[] ma = new boolean[RANGE], mb = new boolean[RANGE];
            int na = rnd.nextInt(30), nb = rnd.nextInt(30);
            for (int k = 0; k < na; k++) {
                int v = rnd.nextInt(RANGE);
                a1.add(v);
                a2.add(v);
                ma[v] = true;
            }
            for (int k = 0; k < nb; k++) {
                int v = rnd.nextInt(RANGE);
                b1.add(v);
                b2.add(v);
                mb[v] = true;
            }
            boolean[] union = new boolean[RANGE], inter = new boolean[RANGE], diff = new boolean[RANGE];
            for (int v = 0; v < RANGE; v++) {
                union[v] = ma[v] || mb[v];
                inter[v] = ma[v] && mb[v];
                diff[v] = ma[v] && !mb[v];
            }
            String r = " round " + round;
            check(a1.union(b1), union, "array union" + r, 0);
            check(a2.union(b2), union, "linked union" + r, 0);
            check(a1.intersection(b1), inter, "array intersection" + r, 0);
            check(a2.intersection(b2), inter, "linked intersection" + r, 0);
            check(a1.difference(b1), diff, "array difference" + r, 0);
            check(a2.difference(b2), diff, "linked difference" + r, 0);
            check(a1.union(b2), union, "mixed union" + r, 0);      // array receiver, linked argument
            check(a2.difference(b1), diff, "mixed difference" + r, 0);
        }
    }
}
