package main;

import ds.LinkedList;

/**
 * Testing my linked list code.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class LinkedListDemo {

    public static void main(String args[]) {
	LinkedList<Integer> ls = new LinkedList<Integer>();
	ls.add(1);
	ls.add(2);
	ls.add(3);
	ls.add(4);
	ls.add(6);

	ls.addHead(0);
	ls.printListContents();
	ls.insertValueAtIndex(5, 5);
	ls.printListContents();
	ls.delNodeAtIndex(0);
	ls.printListContents();
	ls.delNodeAtIndex(1);
	ls.printListContents();

	System.out.println(ls.search(ls.getHead(), 6));
	System.out.println(ls.search(ls.getHead(), 99));
    }

}
