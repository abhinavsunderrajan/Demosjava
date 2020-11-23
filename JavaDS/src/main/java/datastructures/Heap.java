package datastructures;

import java.util.Arrays;

/**
 * My heap implementation. Note that the heap has a binary tree structure backed
 * by an array.
 * 
 * @author abhinav
 *
 * @param <T>
 */
public class Heap<T extends Comparable<T>> {
    private static final int INIT_CAPCITY = 16;
    private T[] arr;
    private int size;
    private boolean minHeap;

    @SuppressWarnings("unchecked")
    public Heap(boolean minHeap) {
	this.minHeap = minHeap;
	arr = (T[]) new Comparable[INIT_CAPCITY];
    }

    public int size() {
	return size;
    }

    public T peek() {
	return arr[0];
    }

    /**
     * When a parent node is not following the order we have to follow the basic
     * heap rules.
     */
    private void percolateDown(int parent) {
	int lChild = 2 * parent + 1;
	int rChild = 2 * parent + 2;
	int child = -1;
	if (lChild < size)
	    child = lChild;
	if (rChild < size && compare(lChild, rChild))
	    child = rChild;
	if (child != -1 && compare(parent, child)) {
	    T temp = arr[parent];
	    arr[parent] = arr[child];
	    arr[child] = temp;
	    percolateDown(child);
	}

    }

    public void add(T t) {
	// expand the arrays capacity if init capacity reached.
	if (size == INIT_CAPCITY)
	    arr = Arrays.copyOf(arr, INIT_CAPCITY * 2);
	arr[size] = t;
	// if (size > 0)
	bubbleUp(size);
	size++;
    }

    public T pop() {
	if (size == 0)
	    throw new IllegalStateException("Heap is empty");

	T top = arr[0];
	if (size > 0)
	    arr[0] = arr[size - 1];
	percolateDown(0);
	size--;
	return top;
    }

    private void bubbleUp(int child) {
	int parent = (child - 1) / 2;
	if (parent < 0)
	    return;
	if (compare(parent, child)) {
	    T temp = arr[child];
	    arr[child] = arr[parent];
	    arr[parent] = temp;
	    bubbleUp(parent);
	}
    }

    /**
     * Ordering based on min or max heaps.
     * 
     * @param first
     * @param second
     * @return
     */
    private boolean compare(int first, int second) {
	if (minHeap)
	    return arr[first].compareTo(arr[second]) > 0;
	else
	    return arr[first].compareTo(arr[second]) < 0;
    }

    @Override
    public String toString() {
	StringBuffer buff = new StringBuffer("");
	buff.append("index element\n");
	for (int i = 0; i < size; i++)
	    buff.append(i + " " + arr[i] + "\n");
	return buff.toString();
    }

}
