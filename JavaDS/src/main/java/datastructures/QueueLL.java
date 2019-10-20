package datastructures;

/**
 * A queue is implemented using a circular linked list.
 * 
 * @author PocketmathUser
 *
 * @param <T>
 */
public class QueueLL<T extends Comparable<T>> {

    // Note that tail.next is the head.
    private Node tail;
    private int size;

    private class Node {
	private Node next;
	private T value;

	public Node(Node next, T t) {
	    this.next = next;
	    this.value = t;
	}

	public String toString() {
	    return value.toString();
	}
    }

    public boolean isEmpty() {
	return size == 0;
    }

    public void add(T t) {
	Node temp = new Node(null, t);
	if (size == 0) {
	    tail = temp;
	    tail.next = temp;
	} else {
	    temp.next = tail.next;
	    tail.next = temp;
	    tail = temp;

	}

	size++;
    }

    public T peep() {
	return tail.next.value;
    }

    public T pop() {
	size--;
	T val = tail.next.value;
	tail.next = tail.next.next;
	return val;
    }

    private String printContents() {

	StringBuffer buffer = new StringBuffer("[");
	Node current = tail.next;
	while (true) {
	    buffer.append(current.value + ",");
	    current = current.next;
	    if (current.equals(tail.next))
		break;

	}
	return buffer.replace(buffer.length() - 1, buffer.length(), "").append("]").toString();

    }

    @Override
    public String toString() {
	return printContents();
    }

}
