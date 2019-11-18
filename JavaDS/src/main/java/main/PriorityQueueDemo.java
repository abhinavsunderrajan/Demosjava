package main;

import datastructures.PriorityQueueHeap;

public class PriorityQueueDemo {

    public static void main(String args[]) {
	PriorityQueueHeap<String> queue = new PriorityQueueHeap<>();
	queue.add("India");
	queue.add("Bangladesh");
	queue.add("America");
	queue.add("Nigeria");
	queue.add("Egypt");
	queue.add("Brazil");
	queue.add("Venezuela");
	queue.add("Jordan");
	queue.add("Iran");
	queue.add("Zambia");

	while (queue.size() > 0)
	    System.out.println(queue.poll());

    }

}
