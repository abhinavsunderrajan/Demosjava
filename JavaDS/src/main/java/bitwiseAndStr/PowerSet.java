package bitwiseAndStr;

/**
 * Powerset implementation using bit manipulation.
 * 
 * @author 746310
 *
 */
public class PowerSet {

    static void printSubsets(char set[]) {
	int n = set.length;

	int numCombinations = (int) Math.pow(2, n);

	for (int i = 0; i < numCombinations; i++) {
	    System.out.print("{ ");

	    // Print current subset
	    for (int j = 0; j < n; j++)

		// Check if jth bit in the index is set
		// If set then add jth element to the subset
		if ((i & (1 << j)) > 0) {
		    System.out.print(set[j] + " ");
		}

	    System.out.println("}");
	}
    }

    // Driver code
    public static void main(String[] args) {
	char set[] = { 'a', 'b', 'c' };
	printSubsets(set);
    }
}
