package bitwiseAndStr;

public class CheckIfStrIsPermOfPalindrome {

    private static boolean checkpermsPalin(String s) {
	int placeholder[] = new int[128];

	for (char c : s.toCharArray())
	    placeholder[c]++;

	int numofOdd = 0;
	for (int i : placeholder) {
	    if (i % 2 == 1)
		numofOdd++;
	    if (numofOdd > 1)
		return false;
	}
	return true;

    }

    public static void main(String args[]) {

	// madamimadam
	String check = "maamimaddam";
	System.out.println("Its " + checkpermsPalin(check) + " that " + check + " is a palindrome permutation");

    }

}
