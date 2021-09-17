package dpAndRecursion;

/**
 * a[i] == i a is sorted
 * 
 * @author 746310
 *
 */
public class MagicIndex {

    private static int arr[] = { -5, -3, -2, -1, 0, 5, 9, 10, 12, 14, 17, 28, 93, 45 };

    public static void main(String[] args) {
	System.out.println(getMagicIndex(0, arr.length - 1));
    }

    private static int getMagicIndex(int beginIndex, int endIndex) {
	if (beginIndex > endIndex)
	    return -1;

	int mid = (beginIndex + endIndex) / 2;
	if (arr[mid] == mid)
	    return mid;

	if (arr[mid] > mid)
	    return getMagicIndex(beginIndex, mid);
	else
	    return getMagicIndex(mid, endIndex);

    }

}
