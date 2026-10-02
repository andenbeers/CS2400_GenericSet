# Complexity analysis

Both implementations store entries **unsorted** and find them by scanning with `Objects.equals`. That one fact
decides almost every row below.

Conventions: `n` is the number of entries in the set being operated on. For the binary operations the other
operand has `m` entries; the measurements use `m = n`. Costs are worst case unless a row says otherwise.

## Big-O summary

| Operation | `ResizableArraySet` | `LinkedSet` | Dominant cost |
|-----------|---------------------|-------------|---------------|
| `add(x)` | O(n) | O(n) | duplicate scan (`indexOf` / `containsEntry`); the insert itself is O(1) (amortized O(1) for the array's resize) |
| `contains(x)` | O(n) | O(n) | linear scan |
| `remove(x)` | O(n) | O(n) | linear scan to find `x`; the array fills the gap with the last entry (O(1)), the chain unlinks a node (O(1)) |
| `remove()` | O(1) | O(1) | takes the last slot / the head, no equality checks |
| `toArray()` | O(n) | O(n) | copies n references |
| `union(other)` | O((n+m)²) | O((n+m)²) | each `result.add` scans the growing result |
| `intersection(other)` | O(n·m) | O(n·m) | `other.contains` per entry (an array-backed or list-backed `other` costs O(m)) |
| `difference(other)` | O(n·(n+m)) | O(n·(n+m)) | `other.contains` per entry, plus `result.add` scans; O(n²) when m = n |

The two representations have identical asymptotics. They differ only in which positions are cheap:
the array finds the oldest entry first, the chain finds the newest entry first.

### Worst case vs amortized

Every bound in the table is a worst-case bound for a single call, except the resize cost noted for `add` on the
array. `ResizableArraySet.add` doubles the array when it is full, and that one call copies all n entries (O(n)).
Because the capacity doubles each time, a sequence of k adds performs at most about 2k element copies for resizing in
total, so resizing costs O(1) amortized per add; it is a worst-case O(n) spike on a few calls, not a cost on every call.
This does not change the O(n) bound for `add`: the duplicate scan costs up to n comparisons on every call, not just
occasionally, so it is O(n) both worst case and amortized. The linked chain has no resize, so it has nothing to amortize.

## Measured operation counts

`ComplexityBenchmark` counts calls to a counting `equals()` on the element type. This is deterministic, unlike
wall-clock time, and `equals` calls are the dominant cost. The doubling ratio is `count(2n) / count(n)`:
about 1 means O(1), about 2 means O(n), about 4 means O(n²).

Reproduce:

```bash
mvn test-compile
java -cp "target/classes;target/test-classes" setforge.ComplexityBenchmark    # Windows
java -cp target/classes:target/test-classes setforge.ComplexityBenchmark      # Linux / macOS
```

### ResizableArraySet

| operation (equals calls) | n=250 | n=500 | n=1000 | n=2000 | n=4000 | doubling ratios |
|---|---:|---:|---:|---:|---:|---|
| add (absent entry) | 250 | 500 | 1000 | 2000 | 4000 | 2.00 ×4 |
| add (duplicate of oldest entry) | 1 | 1 | 1 | 1 | 1 | 1.00 ×4 |
| add (duplicate of newest entry) | 250 | 500 | 1000 | 2000 | 4000 | 2.00 ×4 |
| contains (absent) | 250 | 500 | 1000 | 2000 | 4000 | 2.00 ×4 |
| contains (oldest entry) | 1 | 1 | 1 | 1 | 1 | 1.00 ×4 |
| remove(entry) (absent) | 250 | 500 | 1000 | 2000 | 4000 | 2.00 ×4 |
| remove(entry) (oldest entry) | 1 | 1 | 1 | 1 | 1 | 1.00 ×4 |
| remove() | 0 | 0 | 0 | 0 | 0 | no equality checks |
| union, disjoint | 124,750 | 499,500 | 1,999,000 | 7,998,000 | 31,996,000 | 4.00 ×4 |
| union, identical members | 62,500 | 250,000 | 1,000,000 | 4,000,000 | 16,000,000 | 4.00 ×4 |
| intersection, disjoint | 62,500 | 250,000 | 1,000,000 | 4,000,000 | 16,000,000 | 4.00 ×4 |
| intersection, identical members | 62,500 | 250,000 | 1,000,000 | 4,000,000 | 16,000,000 | 4.00 ×4 |
| difference, disjoint | 93,625 | 374,750 | 1,499,500 | 5,999,000 | 23,998,000 | 4.00 ×4 |
| difference, identical members | 31,375 | 125,250 | 500,500 | 2,001,000 | 8,002,000 | 3.99, 4.00, 4.00, 4.00 |
| toArray | 0 | 0 | 0 | 0 | 0 | no equality checks (see timing below) |

### LinkedSet

| operation (equals calls) | n=250 | n=500 | n=1000 | n=2000 | n=4000 | doubling ratios |
|---|---:|---:|---:|---:|---:|---|
| add (absent entry) | 250 | 500 | 1000 | 2000 | 4000 | 2.00 ×4 |
| add (duplicate of oldest entry) | 250 | 500 | 1000 | 2000 | 4000 | 2.00 ×4 |
| add (duplicate of newest entry) | 1 | 1 | 1 | 1 | 1 | 1.00 ×4 |
| contains (absent) | 250 | 500 | 1000 | 2000 | 4000 | 2.00 ×4 |
| contains (oldest entry) | 250 | 500 | 1000 | 2000 | 4000 | 2.00 ×4 |
| remove(entry) (absent) | 250 | 500 | 1000 | 2000 | 4000 | 2.00 ×4 |
| remove(entry) (oldest entry) | 250 | 500 | 1000 | 2000 | 4000 | 2.00 ×4 |
| remove() | 0 | 0 | 0 | 0 | 0 | no equality checks |
| union, disjoint | 124,750 | 499,500 | 1,999,000 | 7,998,000 | 31,996,000 | 4.00 ×4 |
| union, identical members | 62,500 | 250,000 | 1,000,000 | 4,000,000 | 16,000,000 | 4.00 ×4 |
| intersection, disjoint | 62,500 | 250,000 | 1,000,000 | 4,000,000 | 16,000,000 | 4.00 ×4 |
| intersection, identical members | 62,500 | 250,000 | 1,000,000 | 4,000,000 | 16,000,000 | 4.00 ×4 |
| difference, disjoint | 93,625 | 374,750 | 1,499,500 | 5,999,000 | 23,998,000 | 4.00 ×4 |
| difference, identical members | 31,375 | 125,250 | 500,500 | 2,001,000 | 8,002,000 | 3.99, 4.00, 4.00, 4.00 |
| toArray | 0 | 0 | 0 | 0 | 0 | no equality checks (see timing below) |

### Interpreting the ratios

- **Ratio 2.00 (add absent, contains absent, remove(x) absent):** doubling n doubles the work, so O(n). These are the
  worst cases, where the scan runs to the end.
- **Ratio 1.00 (the "cheap end" rows):** one comparison regardless of n. This is a best case that depends on *which
  entry* you ask about, not a guarantee. Array: oldest entry first. Chain: newest entry first. The same call on the
  other end of the structure is O(n), as the paired rows show.
- **Ratio 4.00 (all algebra rows):** doubling n quadruples the work, so O(n²). The exact counts match the formulas:
  disjoint union does n(n−1)/2 comparisons building the copy of A, then (3n²−n)/2 adding B, about 2n² in total
  (n = 250 gives 124,750). Intersection and difference add a full `other.contains` scan per entry.
- **`remove()` and `toArray()`:** zero equality checks. `remove()` is O(1) because it never searches.

### toArray timing

`toArray` does no comparisons, so its cost is shown by median wall-clock time (21 runs) in nanoseconds:

| implementation | n=5,000 | n=10,000 | n=20,000 | n=40,000 | n=80,000 | doubling ratios |
|---|---:|---:|---:|---:|---:|---|
| ResizableArraySet | 3,000 | 4,400 | 6,400 | 14,200 | 12,300 | 1.47, 1.45, 2.22, 0.87 |
| LinkedSet | 38,200 | 27,600 | 21,700 | 43,600 | 100,500 | 0.72, 0.79, 2.01, 2.31 |

These timings are noisy at this size (JIT warm-up, cache effects, timer resolution) and should not be read as clean
doubling evidence. The O(n) claim rests on the code: both implementations make one pass that copies n references
(`Arrays.copyOf` / a node walk). The chain's ratios settle near 2 at the larger sizes, as expected.

## Reasoning trap: why `add` is O(n) even though a linked list inserts at the head in O(1)

Inserting at the head really is O(1): `firstNode = new Node<>(newEntry, firstNode); size++;` touches a constant number
of references. But `Set.add` is not "insert". It must first check if theres an equal value there, and if there is it returns false. For this reason since we are not using a Hashset, it must loop through the whole array and check before anything can be done, so if it isn't there it WILL loop through all.
