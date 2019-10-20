package main;

import datastructures.BinaryTree;

/**
 * Testing my Binary tree implementation.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class BinaryTreeDemo {

    public static void main(String[] args) {
	BinaryTree<Integer> bt = new BinaryTree<Integer>();
	for (int i = 0; i <= 100; i = i + 10)
	    bt.insert(i);
	System.out.println("DFS Print");
	bt.traverseAndPrintDFS(bt.getRoot());
	System.out.println("\nBFS Print");
	bt.traverseAndPrintBFS();
	bt.deletion(100);
	System.out.println("\nBFS Print");
	bt.traverseAndPrintBFS();
    }

}
