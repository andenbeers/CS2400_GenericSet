package setforge;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Behavioral tests shared by every SetInterface implementation.
 * Subclasses only supply newSet(). No assertion depends on iteration order,
 * and no java.util collection is used: membership is checked with contains() and size.
 */
public abstract class AbstractSetTest {

    protected abstract SetInterface<Object> newSet();

    protected SetInterface<Object> set;

    @BeforeEach
    void setUp() {
        set = newSet();
    }

    // ---------- helpers ----------

    protected SetInterface<Object> setOf(Object... items) {
        SetInterface<Object> s = newSet();
        for (Object o : items) {
            s.add(o);
        }
        return s;
    }

    private static boolean arrayHas(Object[] arr, Object x) {
        for (Object o : arr) {
            if (o.equals(x)) {
                return true;
            }
        }
        return false;
    }

    /** s contains exactly the (distinct) expected items: size, contains(), and toArray() all agree. */
    protected static void assertSameMembers(SetInterface<Object> s, Object... expected) {
        assertEquals(expected.length, s.getCurrentSize(), "size");
        assertEquals(expected.length == 0, s.isEmpty(), "isEmpty");
        Object[] arr = s.toArray();
        assertEquals(expected.length, arr.length, "toArray length");
        for (Object e : expected) {
            assertTrue(s.contains(e), "missing " + e);
            assertTrue(arrayHas(arr, e), "toArray missing " + e);
        }
        for (Object o : arr) { // with equal lengths, every array item in expected means no extras/duplicates
            boolean found = false;
            for (Object e : expected) {
                found |= e.equals(o);
            }
            assertTrue(found, "unexpected element " + o);
        }
    }

    protected static void assertEqualMembers(SetInterface<Object> a, SetInterface<Object> b) {
        assertEquals(a.getCurrentSize(), b.getCurrentSize());
        for (Object o : a.toArray()) {
            assertTrue(b.contains(o));
        }
    }

    // ---------- boundary: empty, singleton, capacity ----------

    @Test
    void emptySetState() {
        assertTrue(set.isEmpty());
        assertEquals(0, set.getCurrentSize());
        assertEquals(0, set.toArray().length);
        assertFalse(set.contains("x"));
    }

    @Test
    void removeOnEmptyReturnsNullAndStaysEmpty() {
        assertNull(set.remove());
        assertFalse(set.remove("x"));
        assertEquals(0, set.getCurrentSize());
        assertTrue(set.isEmpty());
    }

    @Test
    void singletonAddThenRemoveReturnsToEmpty() {
        assertTrue(set.add("a"));
        assertSameMembers(set, "a");
        assertEquals("a", set.remove());
        assertSameMembers(set);
        assertFalse(set.contains("a"));
    }

    @Test
    void capacityBoundaryAroundDefaultCapacity() {
        // ResizableArraySet starts at capacity 10: sizes 9/10/11 and 20/21 straddle the growth points.
        for (int i = 1; i <= 25; i++) {
            assertTrue(set.add(i));
            assertEquals(i, set.getCurrentSize());
            assertTrue(set.contains(i));
        }
        for (int i = 1; i <= 25; i++) {
            assertTrue(set.contains(i), "lost " + i + " after growth");
        }
        assertEquals(25, set.toArray().length);
    }

    @Test
    void removeSpecificFromEveryPosition() {
        for (int target = 0; target < 5; target++) {
            SetInterface<Object> s = setOf(0, 1, 2, 3, 4);
            assertTrue(s.remove(target));
            assertFalse(s.contains(target));
            assertEquals(4, s.getCurrentSize());
            for (int other = 0; other < 5; other++) {
                if (other != target) {
                    assertTrue(s.contains(other));
                }
            }
        }
    }

    @Test
    void removeAbsentEntryReturnsFalseAndChangesNothing() {
        set.add("a");
        assertFalse(set.remove("zzz"));
        assertSameMembers(set, "a");
    }

    @Test
    void removeUnspecifiedDrainsEveryElementExactlyOnce() {
        set.add("a");
        set.add("b");
        set.add("c");
        Object r1 = set.remove();
        Object r2 = set.remove();
        Object r3 = set.remove();
        assertNotNull(r1);
        assertNotNull(r2);
        assertNotNull(r3);
        assertNotEquals(r1, r2);
        assertNotEquals(r1, r3);
        assertNotEquals(r2, r3);
        assertNull(set.remove());
        assertTrue(set.isEmpty());
        assertFalse(set.contains(r1));
    }

    @Test
    void clearEmptiesAndSetIsReusable() {
        for (int i = 0; i < 15; i++) {
            set.add(i);
        }
        set.clear();
        assertSameMembers(set);
        assertFalse(set.contains(3));
        assertTrue(set.add(3));
        assertSameMembers(set, 3);
    }

    @Test
    void nullIsRejectedConsistently() {
        assertThrows(IllegalArgumentException.class, () -> set.add(null));
        assertThrows(IllegalArgumentException.class, () -> set.remove((Object) null));
        assertThrows(IllegalArgumentException.class, () -> set.contains(null));
        assertThrows(IllegalArgumentException.class, () -> set.union(null));
        assertThrows(IllegalArgumentException.class, () -> set.intersection(null));
        assertThrows(IllegalArgumentException.class, () -> set.difference(null));
        assertSameMembers(set); // a rejected call must not change the set
    }

    // ---------- duplicate / equality ----------

    @Test
    void duplicateAddIsRejectedAndSizeUnchanged() {
        assertTrue(set.add("a"));
        assertFalse(set.add("a"));
        assertSameMembers(set, "a");
    }

    @Test
    void equalButDistinctObjectsAreDuplicates() {
        String first = new String("A12");
        String second = new String("A12");
        assertNotSame(first, second);
        assertTrue(set.add(first));
        assertFalse(set.add(second), "== instead of equals would let this in");
        assertTrue(set.contains(new String("A12")));
        assertEquals(1, set.getCurrentSize());
    }

    @Test
    void removeAndContainsUseEqualsNotIdentity() {
        set.add(new String("badge"));
        assertTrue(set.contains(new String("badge")));
        assertTrue(set.remove(new String("badge")));
        assertTrue(set.isEmpty());
    }

    @Test
    void customEqualsTypeIsHonoured() {
        record Point(int x, int y) { }
        set.add(new Point(1, 2));
        assertFalse(set.add(new Point(1, 2)));
        assertTrue(set.add(new Point(2, 1)));
        assertEquals(2, set.getCurrentSize());
    }

    @Test
    void boxedIntegersBeyondCacheRangeStillEqual() {
        // Integers above 127 are distinct objects; == would treat them as different.
        set.add(Integer.valueOf(1000));
        assertFalse(set.add(Integer.valueOf(1000)));
        assertTrue(set.contains(Integer.valueOf(1000)));
        assertEquals(1, set.getCurrentSize());
    }

    // ---------- mutation / aliasing ----------

    @Test
    void toArrayIsASnapshot() {
        set.add("a");
        set.add("b");
        Object[] snap = set.toArray();
        assertEquals(2, snap.length);
        snap[0] = "HACKED";
        snap[1] = "HACKED";
        assertSameMembers(set, "a", "b");
        assertFalse(set.contains("HACKED"));
    }

    @Test
    void toArrayDoesNotTrackLaterChanges() {
        set.add("a");
        Object[] snap = set.toArray();
        set.add("b");
        set.remove("a");
        assertEquals(1, snap.length);
        assertEquals("a", snap[0]);
    }

    @Test
    void toArrayLengthEqualsSizeNotCapacity() {
        for (int i = 0; i < 12; i++) {
            set.add(i);
        }
        set.remove(5);
        assertEquals(11, set.toArray().length);
    }

    @Test
    void algebraResultsAreIndependentOfInputs() {
        SetInterface<Object> a = setOf(1, 2, 3);
        SetInterface<Object> b = setOf(3, 4);

        SetInterface<Object> u = a.union(b);
        SetInterface<Object> i = a.intersection(b);
        SetInterface<Object> d = a.difference(b);
        assertNotSame(a, u);
        assertNotSame(b, u);

        // mutating results must not change inputs
        u.add(99);
        u.remove(1);
        i.clear();
        d.add(100);
        assertSameMembers(a, 1, 2, 3);
        assertSameMembers(b, 3, 4);

        // mutating inputs must not change an earlier result
        SetInterface<Object> u2 = a.union(b);
        a.clear();
        b.add(555);
        assertSameMembers(u2, 1, 2, 3, 4);
    }

    @Test
    void algebraDoesNotMutateInputs() {
        SetInterface<Object> a = setOf(1, 2, 3);
        SetInterface<Object> b = setOf(2, 3, 4);
        a.union(b);
        a.intersection(b);
        a.difference(b);
        b.union(a);
        b.intersection(a);
        b.difference(a);
        assertSameMembers(a, 1, 2, 3);
        assertSameMembers(b, 2, 3, 4);
    }

    // ---------- algebraic properties ----------

    @Test
    void unionIsCorrect() {
        assertSameMembers(setOf(1, 2, 3).union(setOf(3, 4)), 1, 2, 3, 4);
    }

    @Test
    void intersectionIsCorrect() {
        assertSameMembers(setOf(1, 2, 3).intersection(setOf(3, 4, 2)), 2, 3);
    }

    @Test
    void differenceIsCorrect() {
        assertSameMembers(setOf(1, 2, 3).difference(setOf(3, 4)), 1, 2);
    }

    @Test
    void unionAndIntersectionAreCommutative() {
        SetInterface<Object> a = setOf(1, 2, 3, 4);
        SetInterface<Object> b = setOf(3, 4, 5);
        assertEqualMembers(a.union(b), b.union(a));
        assertSameMembers(b.union(a), 1, 2, 3, 4, 5);
        assertEqualMembers(a.intersection(b), b.intersection(a));
        assertSameMembers(b.intersection(a), 3, 4);
    }

    @Test
    void differenceIsNotCommutative() {
        SetInterface<Object> a = setOf(1, 2, 3);
        SetInterface<Object> b = setOf(3, 4);
        assertSameMembers(a.difference(b), 1, 2);
        assertSameMembers(b.difference(a), 4);
    }

    @Test
    void emptySetIdentities() {
        SetInterface<Object> a = setOf(1, 2, 3);
        SetInterface<Object> empty = newSet();
        assertSameMembers(a.union(empty), 1, 2, 3);
        assertSameMembers(empty.union(a), 1, 2, 3);
        assertSameMembers(a.intersection(empty));
        assertSameMembers(empty.intersection(a));
        assertSameMembers(a.difference(empty), 1, 2, 3);
        assertSameMembers(empty.difference(a));
        assertSameMembers(empty.union(newSet()));
    }

    @Test
    void selfOperations() {
        SetInterface<Object> a = setOf(1, 2, 3);
        assertSameMembers(a.union(a), 1, 2, 3);
        assertSameMembers(a.intersection(a), 1, 2, 3);
        assertSameMembers(a.difference(a));
        assertSameMembers(a, 1, 2, 3);
    }

    @Test
    void disjointSets() {
        SetInterface<Object> a = setOf(1, 2);
        SetInterface<Object> b = setOf(3, 4);
        assertSameMembers(a.union(b), 1, 2, 3, 4);
        assertSameMembers(a.intersection(b));
        assertSameMembers(a.difference(b), 1, 2);
    }

    @Test
    void completeOverlap() {
        SetInterface<Object> a = setOf(1, 2, 3);
        SetInterface<Object> b = setOf(3, 2, 1); // same members, different insertion order
        assertSameMembers(a.union(b), 1, 2, 3);
        assertSameMembers(a.intersection(b), 1, 2, 3);
        assertSameMembers(a.difference(b));
    }

    @Test
    void subsetRelations() {
        SetInterface<Object> small = setOf(1, 2);
        SetInterface<Object> big = setOf(1, 2, 3, 4);
        assertSameMembers(small.union(big), 1, 2, 3, 4);
        assertSameMembers(small.intersection(big), 1, 2);
        assertSameMembers(small.difference(big));
        assertSameMembers(big.difference(small), 3, 4);
    }

    @Test
    void algebraAcceptsADifferentImplementationAsArgument() {
        SetInterface<Object> other = (set instanceof ResizableArraySet) ? new LinkedSet<>() : new ResizableArraySet<>();
        other.add(2);
        other.add(9);
        SetInterface<Object> mine = setOf(1, 2);
        assertSameMembers(mine.union(other), 1, 2, 9);
        assertSameMembers(mine.intersection(other), 2);
        assertSameMembers(mine.difference(other), 1);
    }
}
