package dpAndRecursion;

import java.util.ArrayList;
import java.util.List;

/**
 * Power set string. cleaner to read than a power set list. But the logic is the
 * same.
 * 
 * Get the powerset of "" which is [""] this is the base case. Then add char at
 * index strLength-1. ["", c] get char at index strLength-2 then add it to
 * previous subset and concatenate with what remains [b, bc, "", c]
 * 
 * 
 * @author 746310
 *
 */
public class PowerSetString {

    public static void main(String[] args) {
	String word = "abc";
	List<String> powerset = getpowerset(word);
	System.out.println(powerset);

    }

    private static List<String> getpowerset(String word) {

	List<String> allCombinations = new ArrayList<>();
	if (word.length() == 0) {
	    allCombinations.add("");
	    return allCombinations;
	} else {
	    char c = word.charAt(0);
	    List<String> remainCombinations = getpowerset(word.substring(1));
	    allCombinations.addAll(remainCombinations);

	    for (String str : remainCombinations)
		allCombinations.add(c + str);

	}
	return allCombinations;

    }

}
