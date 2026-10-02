# SetForge (CS2400 GenericSet)

Two from-scratch implementations of `SetInterface<T>`: `ResizableArraySet` (array-backed) and `LinkedSet` (linked chain).

## Build and test

Requires JDK 21 and Maven.

```bash
mvn test                                    # compile and run everything
mvn test -Dtest=LinkedSetTest               # one class
mvn test -Dtest=LinkedSetTest#unionIsCorrect  # one method
mvn test -Dseed=<n>                         # replay a randomized run
```

Tests live in `src/test/java/setforge`. `AbstractSetTest` holds the shared behavioral tests and is run
against both implementations through `ResizableArraySetTest` and `LinkedSetTest`.
`RandomizedAndCrossImplTest` runs 2000-operation random sequences against a `boolean[]` model and
against the other implementation. Its seed is random per run and printed in every failure message.

Null policy: null is rejected everywhere with `IllegalArgumentException`.

## Mutation challenge (branch `mutation`)

Three small defects were injected, then the full suite (64 tests at the time) was run with each defect alone and with all three together.

| # | Defect | Location | Tests failing | Caught by |
|---|--------|----------|---------------|-----------|
| 1 | `result[index++] = n.data` changed to `result[index] = n.data` | `LinkedSet.toArray` | 16 of 64 | Every `LinkedSetTest` that reads `toArray` (algebra, snapshot, `assertSameMembers`); the random algebra test; the two cross-implementation algebra tests, because the array set reads a linked set's `toArray` |
| 2 | Removed the `throw` in `requireNonNull` | `LinkedSet` helper | 1 of 64 | `LinkedSetTest.nullIsRejectedConsistently` only |
| 3 | Removed `size++` | `ResizableArraySet.add` | 31 of 64 | 27 `ResizableArraySetTest` tests (size, duplicates, boundary, algebra), `resizableArraySetMatchesModel`, `bothImplementationsAgreeOnTheSameSequence`, the random algebra test, plus the cross-implementation test on both classes |
| 1+2+3 | All three together | | 45 of 64 | The union of the failures above; 19 tests still pass |

Notes:

- No defect was missed. Each one made at least one test fail.
- Defect 2 is the narrowest: only a single test notices it. A null passed into a `LinkedSet` through `add`, `contains`, `remove`, `union`, `intersection` or `difference` is only checked inside that test, so the null check is the least-covered behavior.
- Defect 1 shows the value of cross-implementation tests: a bug in `LinkedSet.toArray` also broke `ResizableArraySet` operations that take a `LinkedSet` argument.
- Defect 3 is caught early by the randomized tests, usually on the first or second operation, and the failure message includes the seed.
- The correct code was restored before merging to `main`.
