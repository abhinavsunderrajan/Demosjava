package datastructures;

/**
 * Abhinav's implementation of linked list
 * 
 * @author abhinav.sunderrajan
 *
 * @param <T>
 */
public class LinkedList<T> {

    private Node head;
    private int size;

    private class Node {
	Node next;
	T value;

	public Node(T value, Node next) {
	    this.value = value;
	    this.next = next;
	}

	public String toString() {
	    return value.toString();
	}

    }

    /**
     * Add head to the linked list i.e the first element of the linked list.
     * 
     * @param value
     */
    public void addHead(T value) {
	head = new Node(value, head);
	size++;
    }

    /**
     * Append value to the linked list.
     * 
     * @param value
     */
    public void add(T value) {
	if (size == 0)
	    addHead(value);
	else {
	    Node last = head;
	    while (last.next != null)
		last = last.next;
	    last.next = new Node(value, null);
	    size++;
	}

    }

    /**
     * Prints the contents of the linked in the order along with its size.
     */
    public void printListContents() {
	if (size > 0) {
	    StringBuffer buffer = new StringBuffer("[");
	    buffer.append(head.value + ",");
	    Node nextNode = head.next;
	    while (nextNode != null) {
		buffer.append(nextNode.value + ",");
		nextNode = nextNode.next;
	    }
	    buffer.replace(buffer.length() - 1, buffer.length(), "]");
	    System.out.println(buffer.toString());

	} else
	    System.out.println("empty list");

    }

    /**
     * Add an element at an index
     * 
     * @param value
     * @param index
     */
    public void insertValueAtIndex(T value, int index) {
	if (index > size)
	    throw new IllegalArgumentException("Insert index cannot be greater than its current size: " + size);

	if (index == 0)
	    addHead(value);
	else if (index == size)
	    add(value);
	else {
	    int i = 1;
	    Node next = head.next;
	    Node prev = head;
	    while (i < index) {
		prev = next;
		next = next.next;
		i++;
	    }
	    Node node = new Node(value, next);
	    prev.next = node;

	}

    }

    /**
     * Delete a node at the specified index.
     * 
     * @param index
     */
    public void delNodeAtIndex(int index) {

	if (index >= size)
	    throw new IllegalArgumentException("Illegal index exception");
	if (index == 0) {
	    // delete head
	    Node node = head.next;
	    head = node;
	} else {
	    int i = 1;
	    Node next = head.next;
	    Node prev = head;
	    while (i < index) {
		prev = next;
		next = next.next;
		i++;
	    }
	    prev.next = next.next;
	}
	size--;

    }

    /**
     * Search if a a value val is present in the linked list
     * 
     * @param head
     *            start the recursion at the head
     * @param val
     *            the value that you are searching for.
     * @return
     */
    public boolean search(Node head, T val) {
	if (head == null)
	    return false;
	if (head.value.equals(val))
	    return true;
	else
	    return search(head.next, val);

    }

    /**
     * Get the size of the linked list
     * 
     * @return size of list
     */
    public int getSize() {
	return size;
    }

    /**
     * Get the head of the linked list
     * 
     * @return the head of the linked list.
     */
    public Node getHead() {
	return head;
    }

    public void reverseList() {
	Node next = null;
	Node current = this.head;
	Node prev = null;
	while (current != null) {
	    next = current.next;
	    current.next = prev;
	    prev = current;
	    current = next;

	}
	this.head = prev;
    }

}
