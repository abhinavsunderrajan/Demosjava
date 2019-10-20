package main;

import java.util.Random;

import datastructures.StackArrayBased;
import datastructures.StackLinkedListBased;

/**
 * Demo of your stack methods.
 * 
 * @author PocketmathUser
 *
 */
public class StackDemo {

    private static final Random random = new Random(42);

    public static void main(String[] args) {
	StackArrayBased<Integer> stack = new StackArrayBased<>(false, 10);
	for (int i = 0; i < 12; i++) {
	    stack.push(i);
	    if (stack.isStackOverflow())
		break;
	    System.out.println(stack);
	}
	while (!stack.isEmpty())
	    System.out.println(stack.pop());

	StackLinkedListBased<Integer> llStack = new StackLinkedListBased<>();
	for (int i = 0; i < 12; i++)
	    llStack.push(i);
	System.out.println(llStack);

	for (int i = 0; i < 6; i++)
	    System.out.println(llStack.pop());
	System.out.println(llStack);

	llStack.clear();
	System.out.println(llStack);

	System.out.println("Implementing a sorted insert..");
	int i = 0;
	while (i < 20) {
	    llStack.sortedInsert(random.nextInt(100));
	    i++;
	}
	System.out.println(llStack);
	llStack.clear();

	i = 0;
	while (i < 20) {
	    llStack.push(random.nextInt(100));
	    i++;
	}
	System.out.println("Sorting an existing stack...");
	System.out.println(llStack);
	llStack.sortStack();
	System.out.println(llStack);
	System.out.println("insert at end");
	llStack.insertAtEnd(-999);
	System.out.println(llStack);
	System.out.println("reverse a stack O(n^2)");
	llStack.reverse();
	System.out.println(llStack);

    }

}
