package main.java.setforge;

public class ResizableArraySet<T> implements SetInterface<T> {

    private static final int DEFAULT_CAPACITY = 10;

    private int size;
    private T[] array;

@SuppressWarnings("unchecked")

    public ResizableArraySet() {
        array = (T[]) new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    public int getCurrentSize() {
        return size;
    }

    public boolean isEmpty() {
        if (size == 0) {
            return true;
        }
        return false;
    }
}