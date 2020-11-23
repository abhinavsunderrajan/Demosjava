package bitwiseAndStr;

public class CheckIfStrIsPermOfPalindrome {

    private static boolean checkpermsPalin(String check) {
	if (check.length() % 2 == 0)
	    return false;

	int placeholder[] = new int[128];

	for (char c : check.toCharArray()) {
	    placeholder[c]++;
	}

	int numofOdd = 0;
	for (char c : check.toCharArray()) {
	    if (placeholder[c] % 2 == 1)
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
