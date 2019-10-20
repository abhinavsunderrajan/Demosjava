package datastructures;

public class StackLinkedListBased<T extends Comparable<T>> {
    private Node head;
    private int size;

    private class Node {
	private Node next;
	private T value;

	public Node(Node next, T t) {
	    this.next = next;
	    this.value = t;
	}
    }

    public void push(T t) {
	head = new Node(head, t);
	size++;
    }

    public T pop() {
	if (size == 0)
	    return null;

	T val = head.value;
	head = head.next;
	size--;
	return val;
    }

    public String printContents() {
	if (size == 0)
	    return "[]";
	Node current = head;
	StringBuffer buf = new StringBuffer("[");
	do {
	    buf.append(current.value + ",");
	    current = current.next;
	} while (current != null);
	buf.replace(buf.length() - 1, buf.length(), "").append("]");
	return buf.toString();

    }

    public T peek() {
	return head.value;
    }

    public int getSize() {
	return size;
    }

    public void sortedInsert(T t) {
	if (this.size == 0 || t.compareTo(this.peek()) > 0) {
	    this.push(t);
	} else {
	    T temp = this.pop();
	    sortedInsert(t);
	    this.push(temp);

	}

    }

    public void clear() {
	head = null;
	size = 0;
    }

    public void insertAtEnd(T t) {
	if (size == 0) {
	    push(t);
	} else {
	    T temp = this.pop();
	    insertAtEnd(t);
	    push(temp);
	}
    }

    /**
     * Sort the stack by making use of the sorted insert. This is equivalent to
     * removing all elements from the stack and reinserting them in order. Hence
     * O(n^2)
     */
    public void sortStack() {
	if (size > 0) {
	    T temp = this.pop();
	    sortStack();
	    sortedInsert(temp);

	}
    }

    /**
     * Again O(n^2) equivalent to popping everything out and inserting at the
     * end.
     */
    public void reverse() {
	if (size > 0) {
	    T temp = this.pop();
	    reverse();
	    insertAtEnd(temp);

	}
    }

    @Override
    public String toString() {
	return printContents();
    }

}
