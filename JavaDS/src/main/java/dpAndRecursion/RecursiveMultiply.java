package dpAndRecursion;

/**
 * Multiply two numbers
 * 
 * @author 746310
 *
 */
public class RecursiveMultiply {

    public static void main(String[] args) {
	int num1 = 9;
	int num2 = 7;
	int product = recurMult(num1, num2);
	System.out.println(product);

    }

    private static int recurMult(int num1, int num2) {
	if (num2 == 0)
	    return 0;
	else if (num2 == 1)
	    return num1;
	else {

	    int value = recurMult(num1, num2 / 2);
	    if (num2 % 2 == 0)
		return value + value;
	    else
		return value + value + num1;
	}
    }

}
