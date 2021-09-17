package dpAndRecursion;

public class MakeChange {

    private static final int[] DENOMS = { 1, 2, 5 };

    public static void main(String[] args) {
	int amount = 11;
	long[][] cache = new long[amount + 1][DENOMS.length];
	long numOfWays = makeChange(amount, cache, 0);
	System.out.println(numOfWays);

    }

    private static long makeChange(int amount, long[][] cache, int index) {
	if (index == DENOMS.length)
	    return 0;
	if (amount == 0)
	    return 1;
	if (amount < 0)
	    return 0;
	if (cache[amount][index] > 0)
	    return cache[amount][index];

	int denom = DENOMS[index];
	long result = 0;
	for (int numCoins = 0; denom * numCoins <= amount; numCoins++) {
	    int amountRemaining = amount - numCoins * denom;
	    System.out.println(amountRemaining + ", " + numCoins + ", " + denom);
	    result += makeChange(amountRemaining, cache, index + 1);
	}

	cache[amount][index] = result;
	return result;

    }

}
