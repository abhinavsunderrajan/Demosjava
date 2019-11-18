package datastructures;

public class PriorityQueueHeap<T extends Comparable<T>> {
    private Heap<T> heap;

    public PriorityQueueHeap() {
	heap = new Heap<>(false);
    }

    public T peek() {
	return heap.peek();
    }

    public T poll() {
	return heap.pop();
    }

    public void add(T t) {
	heap.add(t);
    }

    public int size() {
	return heap.size();
    }

}
