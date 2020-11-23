package main;

import datastructures.LinkedList;
import datastructures.LinkedList.ListNode;

public class LinkedListDemo2 {

    private static LinkedList<Integer> ll = new LinkedList<>();

    public static void main(String[] args) {
	for (int i = 1; i < 10; i++)
	    ll.add(i);
	ll.printListContents();
	reverseList();
	ll.printListContents();

	System.out.println(printKthFromLastElement(4));

	LinkedList<Integer> ll1 = new LinkedList<>();
	LinkedList<Integer> ll2 = new LinkedList<>();
	for (int i = 9; i > 6; i--)
	    ll1.add(i);

	for (int i = 4; i < 7; i++)
	    ll2.add(i);

	llAdd(ll1, ll2);

    }

    /**
     * Use the runner technique to solve this problem.
     * 
     * @param k
     * @return
     */
    private static int printKthFromLastElement(int k) {
	ListNode node1 = null;
	ListNode node2 = null;

	node2 = ll.getHead();
	int i = 0;
	while (i < k) {
	    node2 = node2.getNext();
	    i++;
	}
	node1 = ll.getHead();
	while (node2 != null) {
	    node1 = node1.getNext();
	    node2 = node2.getNext();
	}

	return (int) node1.getValue();
    }

    private static void reverseList() {
	// rever the list
	ListNode current = ll.getHead();
	ListNode prev = null;
	while (current != null) {
	    ListNode next = current.getNext();
	    current.setNext(prev);
	    prev = current;
	    current = next;

	}
	ll.setHead(prev);

    }

    private static void llAdd(LinkedList<Integer> ll1, LinkedList<Integer> ll2) {

	ListNode startL1 = ll1.getHead();
	ListNode startL2 = ll2.getHead();
	ll1.printListContents();
	ll2.printListContents();

	LinkedList<Integer> result = new LinkedList<>();

	int prefix = 0;
	while (startL1 != null) {
	    int val = (int) startL1.getValue() + (int) startL2.getValue() + prefix;
	    if (val > 10) {
		prefix = 1;
		result.add(val - 10);
	    } else {
		result.add(val);
		prefix = 0;
	    }

	    startL1 = startL1.getNext();
	    startL2 = startL2.getNext();
	}

	if (prefix == 1)
	    result.add(1);

	result.printListContents();

    }

}
