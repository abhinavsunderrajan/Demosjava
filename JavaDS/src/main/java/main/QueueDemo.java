package main;

import datastructures.QueueLL;

public class QueueDemo {

    public static void main(String args[]) {

	QueueLL<Integer> queue = new QueueLL<>();
	for (int i = 1; i < 10; i++)
	    queue.add(i);
	System.out.println("peep");
	System.out.println(queue.peep());
	System.out.println(queue);
	System.out.println("pop");
	System.out.println(queue.pop());
	System.out.println(queue);

    }

}
