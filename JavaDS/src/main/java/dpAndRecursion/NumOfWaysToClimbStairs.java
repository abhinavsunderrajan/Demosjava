package dpAndRecursion;

class NumOfWaysToClimbStairs {

    private static final int NUM_STEPS = 15;

    public static int climbStairsTopDown(int n, int cache[]) {
	if (n == 1 || n == 0)
	    return 1;
	else if (n == 2)
	    return 2;
	else if (cache[n] > 0) {
	    return cache[n];
	} else {
	    int result = climbStairsTopDown(n - 1, cache) + climbStairsTopDown(n - 2, cache);
	    cache[n] = result;
	    return result;
	}

    }

    public static int climbStairsBottomUp() {
	// Number of ways to climb from 0 through NUM_STEPS;
	int[] cache = new int[NUM_STEPS + 1];
	cache[0] = 1;
	cache[1] = 1;
	for (int i = 2; i <= NUM_STEPS; i++) {
	    // Num of ways from one step behind and two steps behind
	    cache[i] = cache[i - 1] + cache[i - 2];
	}

	return cache[NUM_STEPS];

    }

    public static void main(String args[]) {
	int[] cache = new int[NUM_STEPS + 1];
	System.out.println("Number of ways to climb " + NUM_STEPS + " steps " + climbStairsTopDown(NUM_STEPS, cache));
	System.out.println(climbStairsBottomUp());
    }
}
