1. What makes two entries duplicates? Defend your equality choice for a generic type T.

Two entries are duplicates when Objects.equals(a, b) is true. I chose value equality over == because a generic T could be String, Integer, or a custom class, and two distinct objects can represent the same value (new String("A12") and new String("A12") are different objects but the same badge ID). Using == would let both into the set.

2. What are the representation invariants for each implementation? State conditions that must always remain true.

ResizableArraySet

0 <= size <= array.length, and array is never null.
Cells 0..size-1 are non-null and pairwise not equal (no duplicates).
Cells size..length-1 are all null (no loitering, no gaps).
array.length never shrinks below its initial capacity.

LinkedSet

size equals the number of nodes reachable from firstNode.
size == 0 exactly when firstNode == null.
The chain has no cycles, and the last node's next is null.
Every node's data is non-null, and no two nodes hold equal data.

3. What should remove() do when the set is empty? Justify a return-value or exception policy.

It returns null and leaves the set unchanged. Since null can never be stored, a null result can only mean "empty," so there is no ambiguity. Returning null also keeps the method usable in a loop (while ((x = s.remove()) != null)) without try/catch. An exception was the alternative, but an empty set is a normal state, not an error, and I just don't think throwing an exception is necessary.

4. Why must the three algebra operations return independent sets rather than aliases? Give one failure scenario.

If union returned this (or reused one of the input's storage), the caller could change an input without realizing it. So for example if a user needed the union for something, but then changed a value in that union set they would accidently change the original value.