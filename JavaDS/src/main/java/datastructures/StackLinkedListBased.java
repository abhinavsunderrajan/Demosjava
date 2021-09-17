package datastructures;

public class StackLinkedListBased<T extends Comparable<T>> {
    private StackNode top;
    private int size;

    private class StackNode {
	private StackNode next;
	private T value;

	public StackNode(T t) {
	    this.value = t;
	}
    }

    public void push(T t) {
	StackNode newNode = new StackNode(t);
	if (top == null)
	    top = newNode;
	else {
	    newNode.next = top;
	    top = newNode;

	}
	size++;
    }

    public T pop() {
	if (size == 0)
	    return null;

	T val = top.value;
	top = top.next;
	size--;
	return val;
    }

    public String printContents() {
	if (size == 0)
	    return "[]";
	StackNode current = top;
	StringBuffer buf = new StringBuffer("[");
	do {
	    buf.append(current.value + ",");
	    current = current.next;
	} while (current != null);
	buf.replace(buf.length() - 1, buf.length(), "").append("]");
	return buf.toString();

    }

    public T peek() {
	return top.value;
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
	top = null;
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
