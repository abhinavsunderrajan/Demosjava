package dpAndRecursion;

import java.util.ArrayList;
import java.util.List;

public class StringPermute {

    public static void main(String[] args) {
	String str = "ABC";
	System.out.println(permute(str));
	// permute(str);
	permutation(str);

    }

    private static List<String> permute(String str) {

	if (str == null)
	    return null;

	List<String> perms = new ArrayList<String>();
	if (str.length() == 0) {
	    perms.add("");
	    return perms;
	}

	for (int i = 0; i < str.length(); i++) {
	    char c = str.charAt(i);
	    String remaining = str.substring(0, i) + str.substring(i + 1);
	    List<String> permsRemain = permute(remaining);

	    for (String s : permsRemain)
		perms.add(c + s);

	}

	return perms;

    }

    private static void permuteBackTrack(String str, int left, int right) {
	if (left == right)
	    System.out.println("----------- " + str + " ------------");
	for (int i = left; i < right; i++) {
	    str = swap(str, left, i);
	    System.out.println("swap " + left + " " + i);
	    System.out.println("permute " + (left + 1) + " " + right);
	    permuteBackTrack(str, left + 1, right);
	    // str = swap(str, left, i);
	}
    }

    private static String swap(String str, int i, int j) {
	char temp;
	char[] charArray = str.toCharArray();
	temp = charArray[i];
	charArray[i] = charArray[j];
	charArray[j] = temp;
	return String.valueOf(charArray);
    }

    /*
     * A method exposed to client to calculate permutation of String in Java.
     */
    public static void permutation(String input) {
	permutation("", input);
    }

    /*
     * Recursive method which actually prints all permutations of given String,
     * but since we are passing an empty String as current permutation to start
     * with, I have made this method private and didn't exposed it to client.
     */
    private static void permutation(String perm, String word) {
	System.out.println(perm + "," + word);
	if (word.isEmpty()) {
	    System.err.println(perm + word);

	} else {
	    for (int i = 0; i < word.length(); i++) {
		permutation(perm + word.charAt(i), word.substring(0, i) + word.substring(i + 1, word.length()));
	    }
	}

    }

}
