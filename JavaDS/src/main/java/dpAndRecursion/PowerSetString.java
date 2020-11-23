package dpAndRecursion;

import java.util.ArrayList;
import java.util.List;

/**
 * Power set string. cleaner to read than a power set list. But the logic is the
 * same.
 * 
 * @author 746310
 *
 */
public class PowerSetString {

    public static void main(String[] args) {
	String name = "abcd";
	List<String> powerset = getpowerset(name, 0);
	System.out.println(powerset);

    }

    private static List<String> getpowerset(String name, int index) {

	List<String> allCombinations;
	if (index == name.length()) {
	    allCombinations = new ArrayList<>();
	    allCombinations.add("");
	    return allCombinations;
	} else {
	    allCombinations = getpowerset(name, index + 1);
	    char c = name.charAt(index);
	    List<String> moreCombinations = new ArrayList<>();
	    for (String str : allCombinations)
		moreCombinations.add(str + c);
	    allCombinations.addAll(moreCombinations);
	}
	return allCombinations;

    }

}
