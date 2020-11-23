package dpAndRecursion;

/**
 * The simplest DP and memoization example.
 * 
 * @author abhinav
 *
 */
public class Fibinacci {

    private static final int FIB = 50;

    public static void main(String[] args) {
	long t1 = System.currentTimeMillis();
	System.out.println(fibinacci(FIB));
	System.out.println((System.currentTimeMillis() - t1) / 1000 + " seconds");

	t1 = System.currentTimeMillis();
	long memo[] = new long[FIB + 1];
	System.out.println(fibinacciMemoization(FIB, memo));
	System.out.println((System.currentTimeMillis() - t1) / 1000 + " seconds");
	StringBuffer buf = new StringBuffer("");
	for (long l : memo)
	    buf.append(l + ",");
	System.out.println(buf.toString());

	System.out.println(fibinacciMA(FIB));

    }

    private static long fibinacci(int n) {
	if (n <= 1)
	    return n;
	else
	    return fibinacci(n - 1) + fibinacci(n - 2);
    }

    private static long fibinacciMA(int n) {
	if (n == 0 || n == 1)
	    return n;
	long c = 0;
	long a = 0;
	long b = 1;
	int count = 1;
	while (count < n) {
	    c = a + b;
	    a = b;
	    b = c;
	    count++;
	}
	return c;
    }

    private static long fibinacciMemoization(int n, long memo[]) {
	if (memo[n] != 0l) {
	    return memo[n];
	}
	if (n == 1 || n == 2)
	    return 1;
	else {
	    long result = fibinacciMemoization(n - 1, memo) + fibinacciMemoization(n - 2, memo);
	    memo[n] = result;
	    return result;
	}

    }

}
