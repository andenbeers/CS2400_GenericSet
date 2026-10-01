package main.java.setforge;

public interface SetInterface<T> {



/**
 * 
    * @return the number of entries in the set as an int
 */
int getCurrentSize();

/**
 *  checks whether or not the set is empty
 * @return true if the set is empty; false otherwise
 */

boolean isEmpty();

/**
 * 
 * @param newEntry the entry we would like to add to the set
 * @return true if successful; false is unsuccessful (an equal entry is already present)
 */
boolean add(T newEntry);


/**
 * removes an unspecified element from the set
 * @return whichever element is chosen to be removed
 */
T remove();

/**
 * removes a specific entry and returns true or false
 * @param anEntry the entry to remove
 * @return true if successful, false is unsuccessful (the element wasn't present)
 */
boolean remove(T anEntry);

/**
 * Removes all entries from the set
 */
void clear();
/**  
 * @param anEntry the entry to check if present
 * @return true if the entry is found, false if not
 */
boolean contains(T anEntry);
/**
 * Creates a new array from our set
 * @return the new array
 */
T[] toArray();
/**
 * creates a union between two sets
 * @param otherSet the set to create the union with
 * @return a new set, which is the union of both sets and completely independent of both
 */
SetInterface<T> union(SetInterface<T> otherSet);
/**
 * Creates an intersection between two sets
 * @param otherSet the set to create the intersection with
 * @return a new set, which is the intersection of both sets and completely independent of both
 */
SetInterface<T> intersection(SetInterface<T> otherSet);
/**
 * Creates an difference between two sets
 * @param otherSet the set to create the difference with
 * @return a new set, which is the difference of both sets and completely independent of both
 */
SetInterface<T> difference(SetInterface<T> otherSet);

}