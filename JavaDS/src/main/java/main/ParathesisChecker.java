package main;

import java.util.Stack;

public class ParathesisChecker {

    public static void main(String[] args) {

	System.out.println(checkValid("asadkjahd"));
	System.out.println(checkValid("[{(1)+2}/99]"));
	String str = "[{()}]+()";
	if (checkValid(str)) {

	}

    }

    private static boolean checkValid(String str) {
	Stack<Character> stack = new Stack<>();

	boolean result = true;
	if (str.matches("(([^)]+))")) {
	    System.out.println("No parathesis in string");
	    return true;
	} else {
	    char[] chars = str.toCharArray();
	    for (char charachter : chars) {
		switch (charachter) {
		case '{':
		case '(':
		case '[':
		    stack.push(charachter);
		    break;
		case '}':
		    result = result && (stack.pop().equals('{'));
		    break;
		case ')':
		    result = result && (stack.pop().equals('('));
		    break;
		case ']':
		    result = result && (stack.pop().equals('['));
		    break;

		default:
		    break;
		}
	    }
	}
	return result && stack.isEmpty();

    }

}
