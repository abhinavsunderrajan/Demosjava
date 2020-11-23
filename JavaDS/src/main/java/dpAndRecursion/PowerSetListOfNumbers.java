package dpAndRecursion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Solution page 361 cracking the coding interview.
 * 
 * @author 746310
 *
 */
public class PowerSetListOfNumbers {

    private static ArrayList<ArrayList<Integer>> getSubsets(List<Integer> set, int index) {

	ArrayList<ArrayList<Integer>> allsubsets;
	if (index == set.size()) {
	    allsubsets = new ArrayList<ArrayList<Integer>>();
	    allsubsets.add(new ArrayList<Integer>());
	} else {
	    allsubsets = getSubsets(set, index + 1);
	    int item = set.get(index);
	    ArrayList<ArrayList<Integer>> moresubsets = new ArrayList<ArrayList<Integer>>();
	    for (ArrayList<Integer> subset : allsubsets) {
		ArrayList<Integer> newsubset = new ArrayList<Integer>();
		newsubset.addAll(subset);
		newsubset.add(item);
		moresubsets.add(newsubset);

	    }
	    allsubsets.addAll(moresubsets);

	}

	return allsubsets;

    }

    public static void main(String args[]) {

	Integer arr[] = { 1, 2, 3 };
	List<Integer> list = Arrays.asList(arr);
	ArrayList<ArrayList<Integer>> allsubsets = getSubsets(list, 0);
	for (ArrayList<Integer> subset : allsubsets)
	    System.out.println(subset);

    }

}
