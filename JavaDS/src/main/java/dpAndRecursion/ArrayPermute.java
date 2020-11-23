package dpAndRecursion;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Get all distinct subsets of an array i.e. permuations.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class ArrayPermute {

    public static void main(String args[]) {

	int array[] = { 1, 2, 3 };
	List<ArrayList<Integer>> perms = permute(array);
	for (ArrayList<Integer> perm : perms)
	    System.out.println(perm);

    }

    private static List<ArrayList<Integer>> permute(int[] arr) {

	List<ArrayList<Integer>> perms = new ArrayList<ArrayList<Integer>>();
	if (arr.length == 0) {
	    perms.add(new ArrayList<>());
	    return perms;
	}

	for (int i = 0; i < arr.length; i++) {
	    int remaining[] = new int[arr.length - 1];
	    for (int j = 0; j < i; j++)
		remaining[j] = arr[j];
	    for (int j = i + 1; j < arr.length; j++)
		remaining[j - 1] = arr[j];

	    List<ArrayList<Integer>> permsRemain = permute(remaining);
	    for (ArrayList<Integer> s : permsRemain) {
		s.add(0, arr[i]);
		perms.add(s);
	    }

	}

	return perms;

    }

}
