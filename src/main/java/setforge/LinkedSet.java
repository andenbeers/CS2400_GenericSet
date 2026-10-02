package setforge;

import java.util.Objects;


public class LinkedSet<T> implements SetInterface<T> {

    private Node<T> firstNode;  // head of the chain
    private int size;   // maintained by add/remove/clear

    public LinkedSet() {
        firstNode = null;
        size = 0;
    }

    private static class Node<T> {
        private final T data;
        private Node<T> next;

        private Node(T data, Node<T> next) {
            this.data = data;
            this.next = next;
        }
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
        if (containsEntry(newEntry)) {
            return false;   // duplicate: set unchanged
        }
        firstNode = new Node<>(newEntry, firstNode);    // insert at head
        size++;
        return true;
    }

    @Override
    public T remove() {
        if (firstNode == null) {
            return null;
        }
        T removed = firstNode.data;
        firstNode = firstNode.next; // head and singleton cases
        size--;
        return removed;
    }

    @Override
    public boolean remove(T anEntry) {
        requireNonNull(anEntry);
        if (firstNode == null) {
            return false;   // empty
        }
        if (Objects.equals(firstNode.data, anEntry)) {
            firstNode = firstNode.next; // head (or singleton)
            size--;
            return true;
        }
        Node<T> previous = firstNode;
        Node<T> current = firstNode.next;
        while (current != null) {
            if (Objects.equals(current.data, anEntry)) {
                previous.next = current.next;   // middle or tail: bypass current
                size--;
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;   // not found
    }

    @Override
    public void clear() {
        firstNode = null;   // old chain becomes unreachable
        size = 0;
    }

    @Override
    public boolean contains(T anEntry) {
        requireNonNull(anEntry);
        return containsEntry(anEntry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T[] toArray() {
        T[] result = (T[]) new Object[size];    // new snapshot
        int index = 0;
        for (Node<T> n = firstNode; n != null; n = n.next) {
            result[index++] = n.data;
        }
        return result;
    }

    @Override
    public SetInterface<T> union(SetInterface<T> otherSet) {
        requireNonNull(otherSet);
        LinkedSet<T> result = new LinkedSet<>();
        for (Node<T> n = firstNode; n != null; n = n.next) {
            result.add(n.data);
        }
        for (T entry : otherSet.toArray()) {
            result.add(entry);  // duplicates are rejected by add
        }
        return result;
    }

    @Override
    public SetInterface<T> intersection(SetInterface<T> otherSet) {
        requireNonNull(otherSet);
        LinkedSet<T> result = new LinkedSet<>();
        for (Node<T> n = firstNode; n != null; n = n.next) {
            if (otherSet.contains(n.data)) {
                result.add(n.data);
            }
        }
        return result;
    }

    @Override
    public SetInterface<T> difference(SetInterface<T> otherSet) {
        requireNonNull(otherSet);
        LinkedSet<T> result = new LinkedSet<>();
        for (Node<T> n = firstNode; n != null; n = n.next) {
            if (!otherSet.contains(n.data)) {
                result.add(n.data);
            }
        }
        return result;
    }

    // helper functions

    private boolean containsEntry(T anEntry) {
        for (Node<T> n = firstNode; n != null; n = n.next) {
            if (Objects.equals(n.data, anEntry)) {
                return true;
            }
        }
        return false;
    }

    private static void requireNonNull(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("null is not allowed");
        }
    }
}