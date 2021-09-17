package bitwiseAndStr;

public class BitwiseTest {

    public static void main(String[] args) {
	System.out.println("right shift check");
	for (int j = 0; j < 4; j++)
	    System.out.println(1 << j);

	// xor check
	System.out.println("xor check");
	int result = 0;
	int nums[] = { 4, 1, 2, 1, 2 };
	for (int value : nums) {
	    result ^= value;
	    System.out.println(result);

	}

    }

}
