package datastructures;

/**
 * Require two pointers
 * 
 * @author abhinav.sunderrajan
 *
 * @param <T>
 */
public class QueueLL<T extends Comparable<T>> {

    private QueueNode first;
    private QueueNode last;
    private int size;

    private class QueueNode {
	private QueueNode next;
	private T value;

	public QueueNode(T value) {
	    this.value = value;
	}

	public String toString() {
	    return value.toString();
	}
    }

    public boolean isEmpty() {
	return size == 0;
    }

    public void add(T t) {
	QueueNode node = new QueueNode(t);
	if (last == null)
	    last = node;
	else
	    last.next = node;

	if (first == null)
	    first = node;
	size++;
    }

    public T peep() {
	return first.value;
    }

    public T pop() {
	size--;
	T val = first.value;
	first = first.next;
	return val;
    }

    private String printContents() {

	if (size > 0) {
	    StringBuffer buffer = new StringBuffer("[");
	    QueueNode current = first;
	    while (true) {
		buffer.append(current.value + ",");
		current = current.next;
		if (current.next == null)
		    break;
	    }
	    return buffer.replace(buffer.length() - 1, buffer.length(), "").append("]").toString();
	} else {
	    return "[]";
	}

    }

    @Override
    public String toString() {
	return printContents();
    }

}
