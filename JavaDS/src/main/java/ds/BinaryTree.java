package ds;

import java.util.LinkedList;
import java.util.Queue;

/**
 * My implementation of the binary tree.
 * 
 * BFS traversal using queue. DFS traversal using stack i.e. recursion.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class BinaryTree<T> {
    private Node root;

    private class Node {
	Node left;
	Node right;
	T key;

	Node(T key) {
	    this.key = key;
	}

	@Override
	public String toString() {
	    return key.toString();
	}

    }

    /**
     * In order traversal of the binary tree.
     * 
     * @param temp
     */
    public void traverseAndPrintDFS(Node temp) {
	if (temp == null)
	    return;
	System.out.print(temp.key + " ");
	traverseAndPrintDFS(temp.left);
	traverseAndPrintDFS(temp.right);

    }

    /**
     * In order traversal of the binary tree.
     * 
     * @param temp
     */
    public void traverseAndPrintBFS() {
	Queue<Node> q = new LinkedList<>();
	q.add(root);
	while (!q.isEmpty()) {
	    Node node = q.poll();
	    if (node != null) {
		if (node.left != null)
		    q.add(node.left);
		if (node.right != null)
		    q.add(node.right);
	    }
	    System.out.print(node.key + " ");

	}

    }

    /**
     * Function to insert element in binary tree.
     * 
     * @param temp
     * @param key
     */
    public void insert(T key) {
	if (root == null) {
	    root = new Node(key);
	    return;
	}

	Queue<Node> q = new LinkedList<Node>();
	Node temp = root;
	q.add(temp);

	// Do level order traversal until we find
	// an empty place.
	while (!q.isEmpty()) {
	    temp = q.poll();

	    if (temp.left == null) {
		temp.left = new Node(key);
		break;
	    } else
		q.add(temp.left);

	    if (temp.right == null) {
		temp.right = new Node(key);
		break;
	    } else
		q.add(temp.right);
	}
    }

    /**
     * 
     * @param root
     * @param d_node
     */
    void deleteDeepest(Node d_node) {
	Queue<Node> q = new LinkedList<Node>();
	q.add(root);

	// Do level order traversal until last node
	Node temp;
	while (!q.isEmpty()) {
	    temp = q.poll();
	    if (temp.equals(d_node)) {
		temp = null;
		return;
	    }
	    if (temp.right != null) {
		if (temp.right.equals(d_node)) {
		    temp.right = null;
		    return;
		} else
		    q.add(temp.right);
	    }

	    if (temp.left != null) {
		if (temp.left.equals(d_node)) {
		    temp.left = null;
		    return;
		} else
		    q.add(temp.left);
	    }
	}
    }

    /**
     * Function to delete a key the first occurrence and replace that with the right
     * most node in the binary tree.
     * 
     * @param key the key value to be deleted.
     */
    public void deletion(T key) {
	Queue<Node> q = new LinkedList<Node>();
	q.add(root);

	Node rightMostNode = null;
	Node key_node = null;

	// Do level order traversal to find deepest
	// node(temp) and node to be deleted (key_node)
	while (!q.isEmpty()) {
	    rightMostNode = q.poll();

	    if (rightMostNode.key == key)
		key_node = rightMostNode;

	    if (rightMostNode.left != null)
		q.add(rightMostNode.left);

	    if (rightMostNode.right != null)
		q.add(rightMostNode.right);
	}
	if (rightMostNode != null) {
	    T x = rightMostNode.key;
	    deleteDeepest(rightMostNode);
	    key_node.key = x;
	}
    }

    /**
     * Get the root of the binary tree.
     * 
     * @return root of the binary tree.
     */
    public Node getRoot() {
	return root;
    }

}
