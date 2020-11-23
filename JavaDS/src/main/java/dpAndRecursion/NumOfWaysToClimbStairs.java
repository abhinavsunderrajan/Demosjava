package dpAndRecursion;

class NumOfWaysToClimbStairs {

    private static final int NUM_STEPS = 15;

    public static int climbStairs(int n, int cache[]) {
	if (n == 1 || n == 0)
	    return 1;
	else if (n == 2)
	    return 2;
	else if (cache[n] > 0) {
	    return cache[n];
	} else {
	    int result = climbStairs(n - 1, cache) + climbStairs(n - 2, cache);
	    cache[n] = result;
	    return result;
	}

    }

    public static void main(String args[]) {
	int[] cache = new int[NUM_STEPS + 1];
	System.out.println("Number of ways to climb " + NUM_STEPS + " steps " + climbStairs(NUM_STEPS, cache));

    }
}
