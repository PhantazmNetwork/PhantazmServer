package com.github.phantazmnetwork.commons.collection;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class GapListTest {
    private static <T extends List<String>> T populateWithAppend(int values, Supplier<T> listSupplier) {
        T list = listSupplier.get();
        for(int i = 0; i < values; i++) {
            list.add(Integer.valueOf(i).toString());
        }

        return list;
    }

    @Nested
    class Append {
        private static void assertAppendOrder(int capacity) {
            GapList<String> gapList = new GapList<>(capacity);
            List<String> arrayList = new ArrayList<>(capacity);

            for(int i = 0; i < 100; i++) {
                String val = Integer.valueOf(i).toString();
                gapList.add(val);
                arrayList.add(val);
            }

            for(int i = 0; i < 100; i++) {
                assertEquals(arrayList.get(i), gapList.get(i));
            }
        }

        @Test
        void appendSize() {
            int size = 3;

            List<String> list = populateWithAppend(size, GapList::new);
            assertSame(size, list.size());
        }

        @Test
        void appendSizeZeroCapacity() {
            int size = 3;

            List<String> list = populateWithAppend(size, () -> new GapList<>(3));
            assertSame(size, list.size());
        }

        @Test
        void appendSizeLarge() {
            int size = 1000;

            List<String> list = populateWithAppend(size, GapList::new);
            assertEquals(size, list.size());
        }

        @Test
        void appendOrder() {
            assertAppendOrder(10);
        }

        @Test
        void appendOrderZeroCapacity() {
            assertAppendOrder(0);
        }

        @Test
        void addAtLengthSize() {
            int size = 10;

            List<String> list = populateWithAppend(size, GapList::new);
            list.add(size, Integer.valueOf(size).toString());

            assertEquals(size + 1, list.size());
        }
    }

    @Nested
    class Insert {
        @Test
        void insertSize() {
            int size = 10;
            int insertIndex = 0;

            List<String> list = populateWithAppend(size, GapList::new);
            list.add(insertIndex, "element");
            assertSame(size + 1, list.size());
        }

        @Test
        void insertRandom() {
            List<Integer> indices = List.of(0, 0, 1, 0, 3, 0, 1, 2, 0, 0, 0, 3, 5, 2, 2, 2);

            List<Integer> arrayList = new ArrayList<>();
            List<Integer> gapList = new GapList<>();

            int store = 0;
            for(int index : indices) {
                arrayList.add(index, store);
                gapList.add(index, store);

                store++;
            }

            assertEquals(arrayList, gapList);
        }

        @Test
        void insertMany() {
            int size = 1000;

            List<String> gapList = new GapList<>();
            List<String> arrayList = new ArrayList<>();

            for(int i = size; i >= 0; i--) {
                String value = Integer.valueOf(i).toString();
                gapList.add(0, value);
                arrayList.add(0, value);
            }

            assertEquals(arrayList, gapList);
        }
    }

    @Nested
    class GetAndSet {
        @Test
        void forLoopIteration() {
            int size = 10;

            List<String> gapList = populateWithAppend(size, GapList::new);
            List<String> compare = populateWithAppend(size, ArrayList::new);

            for(int i = 0; i < size; i++) {
                assertEquals(compare.get(i), gapList.get(i));
            }
        }

        @Test
        void setChangesList() {
            int size = 10;
            int index = 1;
            String value = "element";
            String value2 = "element2";

            GapList<String> gapList = populateWithAppend(size, GapList::new);
            gapList.set(index, value);

            assertEquals(value, gapList.get(index));
            gapList.setOnly(index, value2);

            assertEquals(value2, gapList.get(index));
        }
    }

    @SuppressWarnings({"ConstantConditions", "ListRemoveInLoop"})
    @Nested
    class Remove {
        @Test
        void removeSize() {
            int size = 3;

            List<String> list = populateWithAppend(size, GapList::new);

            list.remove(0);
            assertSame(size - 1, list.size());
        }

        @Test
        void removeOnlySize() {
            int size = 3;

            GapList<String> list = populateWithAppend(size, GapList::new);
            list.removeOnly(0);
            assertSame(size - 1, list.size());
        }

        @Test
        void removeAll() {
            int size = 3;

            List<String> list = populateWithAppend(size, GapList::new);

            for(int i  = 0; i < size; i++) {
                list.remove(0);
            }

            assertSame(0, list.size());
        }

        @Test
        void removeRandom() {
            int size = 20;

            List<Integer> indices = List.of(0, 0, 1, 0, 3, 0, 1, 2, 0, 0, 0, 3, 5, 2, 2, 2, 1, 0, 1);
            List<String> gapList = populateWithAppend(size, GapList::new);
            List<String> arrayList = populateWithAppend(size, ArrayList::new);

            for(int index : indices) {
                gapList.remove(index);
                arrayList.remove(index);
            }

            assertEquals(arrayList, gapList);
        }

        @Test
        void clearSize() {
            int size = 10;

            List<String> gapList = populateWithAppend(size, GapList::new);
            gapList.clear();
            assertEquals(0, gapList.size());
        }

        @Test
        void clearThenBuild() {
            int size = 10;

            List<String> gapList = populateWithAppend(size, GapList::new);
            List<String> arrayList = populateWithAppend(size, ArrayList::new);
            gapList.clear();

            populateWithAppend(size, () -> gapList);
            assertEquals(arrayList, gapList);
        }
    }

    @SuppressWarnings({ "ResultOfMethodCallIgnored", "ConstantConditions" })
    @Nested
    class FailFast {
        private static void assertThrowsConcurrentModificationException(List<String> list, Runnable operation) {
            assertThrows(ConcurrentModificationException.class, () -> {
                for(String string : list) {
                    operation.run();
                }
            });
        }

        @Test
        void negativeCapacity() {
            assertThrows(IllegalArgumentException.class, () -> new GapList<>(-1));
        }

        @Test
        void nullCollection() {
            assertThrows(NullPointerException.class, () -> new GapList<>(null));
        }

        @Test
        void negativeAddIndex() {
            List<String> list = populateWithAppend(10, GapList::new);
            assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, "-1"));
        }

        @Test
        void negativeAddAllIndex() {
            List<String> list = populateWithAppend(10, GapList::new);
            List<String> addList = populateWithAppend(10, ArrayList::new);
            assertThrows(IndexOutOfBoundsException.class, () -> list.addAll(-1, addList));
        }

        @Test
        void negativeGetIndex() {
            List<String> list = populateWithAppend(3, GapList::new);
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        }

        @Test
        void getIndexAtLength() {
            int size = 3;

            List<String> list = populateWithAppend(size, GapList::new);
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(size));
        }

        @Test
        void concurrentModificationRemove() {
            int size = 3;

            List<String> list = populateWithAppend(size, GapList::new);
            assertThrowsConcurrentModificationException(list, () -> list.remove(0));
        }

        @Test
        void concurrentModificationAdd() {
            int size = 3;

            List<String> list = populateWithAppend(size, GapList::new);
            assertThrowsConcurrentModificationException(list, () -> list.add("element"));
        }

        @Test
        void concurrentModificationAddAll() {
            int size = 3;

            List<String> list = populateWithAppend(size, GapList::new);
            List<String> insertList = populateWithAppend(size, ArrayList::new);
            assertThrowsConcurrentModificationException(list, () -> list.addAll(insertList));
        }

        @Test
        void concurrentModificationClear() {
            int size = 3;

            List<String> list = populateWithAppend(size, GapList::new);
            assertThrowsConcurrentModificationException(list, list::clear);
        }
    }

    @Nested
    class Iterators {
        private static <T> void assertIteratorSameOrder(Iterator<T> expected, Iterator<T> actual) {
            while(true) {
                boolean expectedHasNext = expected.hasNext();
                boolean actualHasNext = actual.hasNext();

                assertFalse((expectedHasNext && !actualHasNext) || (!expectedHasNext && actualHasNext));

                if(expectedHasNext) {
                    assertEquals(expected.next(), actual.next());
                }
                else {
                    break;
                }
            }
        }

        @Test
        void iteratorOrder() {
            int size = 10;

            List<String> gapList = populateWithAppend(size, GapList::new);
            List<String> arrayList = populateWithAppend(size, ArrayList::new);

            assertIteratorSameOrder(arrayList.iterator(), gapList.iterator());
        }
    }

    @Nested
    class Bulk {
        private static void assertInsert(int size, int insertLocation) {
            List<String> gapList = populateWithAppend(size, GapList::new);
            List<String> arrayList = populateWithAppend(size, ArrayList::new);
            List<String> insertList = populateWithAppend(size, ArrayList::new);

            gapList.addAll(insertLocation, insertList);
            arrayList.addAll(insertLocation, insertList);

            assertEquals(arrayList, gapList);
        }

        @Test
        void collectionConstructor() {
            List<String> arrayList = populateWithAppend(10, ArrayList::new);
            List<String> gapList = new GapList<>(arrayList);

            assertEquals(arrayList, gapList);
        }

        @Test
        void collectionConstructorAppendSize() {
            int size = 10;

            List<String> arrayList = populateWithAppend(size, ArrayList::new);
            List<String> gapList = new GapList<>(arrayList);

            gapList.add("element");

            assertEquals(size + 1, gapList.size());
        }

        @Test
        void insertBeginning() {
            assertInsert(10, 0);
        }

        @Test
        void insertEnd() {
            int size = 10;
            assertInsert(size, size);
        }

        @Test
        void insertThenPopulate() {
            int size = 10;
            int bulkInsertIndex = 1;
            int insertIndex = 2;
            String value = "element";

            List<String> gapList = populateWithAppend(size, GapList::new);
            List<String> arrayList = populateWithAppend(size, ArrayList::new);
            List<String> insertList = populateWithAppend(size, ArrayList::new);

            gapList.addAll(bulkInsertIndex, insertList);
            arrayList.addAll(bulkInsertIndex, insertList);

            gapList.add(value);
            gapList.add(insertIndex, value);

            arrayList.add(value);
            arrayList.add(insertIndex, value);

            assertEquals(arrayList, gapList);
        }
    }

    @Nested
    @Disabled("To improve test run time")
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    class Benchmark {
        private static void comparativeBenchmark(Supplier<Consumer<String>> first, Supplier<Consumer<String>> second,
                                                 String firstName, String secondName, String operationName, int reps,
                                                 int iters) {
            long[] secondTimes = new long[reps];
            long[] firstTimes = new long[reps];

            String val = "test";

            for(int r = 0; r < reps; r++) {
                Consumer<String> firstConsumer = first.get();
                Consumer<String> secondConsumer = second.get();

                long firstStart = System.nanoTime();
                for(int i = 0; i < iters; i++) {
                    firstConsumer.accept(val);
                }
                firstTimes[r] = System.nanoTime() - firstStart;

                long secondStart = System.nanoTime();
                for(int i = 0; i < iters; i++) {
                    secondConsumer.accept(val);
                }
                secondTimes[r] = System.nanoTime() - secondStart;
            }

            long secondSum = 0;
            long firstSum = 0;
            for(int i = 0; i < reps; i++) {
                firstSum += firstTimes[i];
                secondSum += secondTimes[i];
            }

            double secondAvg = ((double)secondSum / (double)reps);
            double firstAvg = ((double)firstSum / (double)reps);

            System.out.println(firstName + " average: " + firstAvg + "ns for " + iters + " " + operationName);
            System.out.println(secondName + " average: " + secondAvg + "ns for " + iters + " " + operationName);
            System.out.println(firstName + " is " + secondAvg / firstAvg + "x faster than " + secondName + " over " + reps
                    + " repetitions");
        }

        @Test
        void appendVsArrayList() {
            comparativeBenchmark(() -> new GapList<>()::add, () -> new ArrayList<>()::add, "GapList",
                    "ArrayList", "appends", 10000, 32768);
        }

        @Test
        void insertVsArrayDeque() {
            comparativeBenchmark(() -> {
                List<String> gapList = new GapList<>();
                return (string) -> gapList.add(0, string);
            }, () -> {
                Deque<String> arrayDeque = new ArrayDeque<>();
                return arrayDeque::addFirst;
            }, "GapList", "ArrayDeque", "inserts at index 0", 10000, 32768);
        }

        @Test
        void insertVsArrayList() {
            comparativeBenchmark(() -> {
                List<String> gapList = new GapList<>();
                return (string) -> gapList.add(0, string);
            }, () -> {
                List<String> arrayList = new ArrayList<>();
                return (string) -> arrayList.add(0, string);
            }, "GapList", "ArrayList", "inserts at index 0", 1000, 16384);
        }
    }
}