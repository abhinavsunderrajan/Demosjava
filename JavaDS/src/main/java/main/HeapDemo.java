package main;

import java.util.Random;

import datastructures.Heap;

public class HeapDemo {
    private static final Random random = new Random(42);

    public static void main(String[] args) {
	Heap<Integer> heap = new Heap<>(false);
	for (int i = 10; i >= 0; i--)
	    heap.add(random.nextInt(100));
	System.out.println(heap);
	System.out.println("popped: " + heap.pop());
	System.out.println(heap);

    }

}
