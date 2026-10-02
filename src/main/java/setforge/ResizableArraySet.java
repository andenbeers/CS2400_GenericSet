package setforge;

import java.util.Arrays;
import java.util.Objects;



public class ResizableArraySet<T> implements SetInterface<T> {

    private static final int DEFAULT_CAPACITY = 10;

    private T[] array;  // backing storage
    private int size;   // logical size

    @SuppressWarnings("unchecked")
    public ResizableArraySet() {
        array = (T[]) new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    @Override
    public int getCurrentSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean add(T newEntry) {
        requireNonNull(newEntry);
        if (indexOf(newEntry) >= 0) {
            return false;   // duplicate: set unchanged
        }
        if (size == array.length) {
            array = Arrays.copyOf(array, array.length * 2); // grow
        }
        array[size] = newEntry;
        size++;
        return true;
    }

    @Override
    public T remove() {
        if (size == 0) {
            return null;
        }
        T removed = array[size - 1];
        array[size - 1] = null; // prevent loitering
        size--;
        return removed;
    }

    @Override
    public boolean remove(T anEntry) {
        requireNonNull(anEntry);
        int index = indexOf(anEntry);
        if (index < 0) {
            return false;
        }
        array[index] = array[size - 1]; // fill the gap with the last entry
        array[size - 1] = null; // prevent loitering
        size--;
        return true;
    }

    @Override
    public void clear() {
        Arrays.fill(array, 0, size, null);  // drop references
        size = 0;
    }

    @Override
    public boolean contains(T anEntry) {
        requireNonNull(anEntry);
        return indexOf(anEntry) >= 0;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(array, size);  // new snapshot, length == size
    }

    @SuppressWarnings("unchecked")
    @Override
    public SetInterface<T> union(SetInterface<T> otherSet) {
        requireNonNull(otherSet);
        ResizableArraySet<T> result = new ResizableArraySet<>();
        for (int i = 0; i < size; i++) {
            result.add(array[i]);
        }
        for (Object entry : otherSet.toArray()) {
            result.add((T) entry);  // safe: otherSet only holds T; duplicates are rejected by add
        }
        return result;
    }

    @Override
    public SetInterface<T> intersection(SetInterface<T> otherSet) {
        requireNonNull(otherSet);
        ResizableArraySet<T> result = new ResizableArraySet<>();
        for (int i = 0; i < size; i++) {
            if (otherSet.contains(array[i])) {
                result.add(array[i]);
            }
        }
        return result;
    }

    @Override
    public SetInterface<T> difference(SetInterface<T> otherSet) {
        requireNonNull(otherSet);
        ResizableArraySet<T> result = new ResizableArraySet<>();
        for (int i = 0; i < size; i++) {
            if (!otherSet.contains(array[i])) {
                result.add(array[i]);
            }
        }
        return result;
    }

    // these are easy since we already implemented all the behaivor for it


    // helper functions

    private int indexOf(T anEntry) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(array[i], anEntry)) { // must use object.equals 
                return i;
            }
        }
        return -1;
    }

    private static void requireNonNull(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("null is not allowed");
        }
    }
}