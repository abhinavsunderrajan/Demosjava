package main;

public class LongestCommonPrefix {

    private static String getCommonPrefix(String[] strArr) {
	int minLength = strArr[0].length();
	int minIndex = 0;
	for (int i = 1; i < strArr.length; i++) {
	    if (strArr[i].length() < minLength) {
		minLength = strArr[i].length();
		minIndex = i;
	    }
	}

	if (minIndex != 0) {
	    String temp = strArr[minIndex];
	    strArr[minIndex] = strArr[0];
	    strArr[0] = temp;
	}

	String comPrefix = "";
	for (int i = 1; i <= strArr[0].length(); i++) {

	    String substr = strArr[0].substring(0, i);
	    for (int j = 1; j < strArr.length; j++) {
		if (!strArr[j].startsWith(substr))
		    return comPrefix;
	    }
	    comPrefix = substr;

	}

	return comPrefix;

    }

    public static void main(String[] args) {
	String[] strArr = { "Flo", "Flotsam", "Florence", "Flo" };
	String comPrefix = getCommonPrefix(strArr);
	System.out.println(comPrefix);
    }

}
