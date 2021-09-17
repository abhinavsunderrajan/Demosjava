package dpAndRecursion;

/**
 * Using divide and conquer.
 * 
 * @author 746310
 *
 */
public class RecursiveExponentiation {

    /**
     * 
     * @param x
     * @param n
     * @return x^n
     */
    private static int power(int x, int n) {

	if (n == 0)
	    return 1;
	int value = power(x, n / 2);
	if (n % 2 == 0) {
	    return value * value;
	} else {
	    return x * value * value;
	}

    }

    public static void main(String[] args) {

	int x = 3;
	int n = 10;
	System.out.println(power(x, n));
    }

}
