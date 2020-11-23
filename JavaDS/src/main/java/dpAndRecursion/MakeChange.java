package dpAndRecursion;

public class MakeChange {

    private static final int[] DENOMS = { 25, 10, 5, 1 };

    public static void main(String[] args) {
	int amount = 100;
	long[][] cache = new long[amount + 1][DENOMS.length];
	long numOfWays = makeChange(amount, cache, 0);
	System.out.println(numOfWays);

    }

    private static long makeChange(int amount, long[][] cache, int index) {
	if (cache[amount][index] > 0)
	    return cache[amount][index];
	if (index >= DENOMS.length - 1)
	    return 1;

	int denom = DENOMS[index];
	long result = 0;
	for (int i = 0; denom * i <= amount; i++) {
	    int amountRemaining = amount - i * denom;
	    result += makeChange(amountRemaining, cache, index + 1);
	}

	cache[amount][index] = result;
	return result;

    }

}
