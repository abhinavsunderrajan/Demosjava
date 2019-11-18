package main;

import java.util.Random;

import datastructures.Heap;

public class HeapSort {

    private static final Random random = new Random();
    private static final int SIZE = 10;

    public static void main(String[] args) {
	Heap<String> heap = new Heap<>(false);
	String[] sorted = new String[SIZE];
	heap.add("India");
	heap.add("Bangladesh");
	heap.add("America");
	heap.add("Nigeria");
	heap.add("Egypt");
	heap.add("Brazil");
	heap.add("Venezuela");
	heap.add("Jordan");
	heap.add("Iran");
	heap.add("Zambia");

	int i = 1;
	while (heap.size() > 0) {
	    sorted[SIZE - i] = heap.pop();
	    i++;
	}
	StringBuffer buff = new StringBuffer("");
	for (String s : sorted)
	    buff.append(s + ", ");
	System.out.println(buff);

    }

}
