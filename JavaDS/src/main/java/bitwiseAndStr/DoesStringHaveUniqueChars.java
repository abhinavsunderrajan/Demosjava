package bitwiseAndStr;

public class DoesStringHaveUniqueChars {

    private static boolean testUniqueChars(String test) {
	int[] uniqueASCII = new int[128];
	boolean uniqueChars = true;

	for (char c : test.toCharArray()) {
	    if (uniqueASCII[c] > 0) {
		uniqueChars = false;
		break;
	    }
	    uniqueASCII[c]++;
	}

	return uniqueChars;

    }

    private static boolean checkIfPerms(String s1, String s2) {
	int[] charCounts = new int[128];
	boolean isPermutation = true;

	if (s1.length() != s2.length())
	    return false;

	for (char c : s1.toCharArray())
	    charCounts[c]++;

	for (char c : s2.toCharArray()) {
	    charCounts[c]--;
	    if (charCounts[c] < 0) {
		isPermutation = false;
		break;
	    }

	}

	return isPermutation;

    }

    public static void main(String[] args) {

	String test = "abhinav";
	boolean uniqueChars = testUniqueChars(test);
	if (uniqueChars)
	    System.out.println("string contains unique chars");
	else
	    System.out.println("string contains duplicate chars");

	System.out.println(checkIfPerms("abcd", "dcza"));
    }

}
