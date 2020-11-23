package random;

import java.util.ArrayList;
import java.util.List;

public class GetNumPlus1 {

    public static void main(String[] args) {

	int array1[] = { 9, 9, 9 };
	int answer[] = getPlus1Array(array1);
	for (int a : answer)
	    System.out.print(a + " ");
    }

    private static int[] getPlus1Array(int[] array) {
	List<Integer> answer = new ArrayList<Integer>();
	int carry = 1;
	int array_len = array.length;
	if (array[array_len - 1] < 9) {
	    array[array_len - 1] = array[array_len - 1] + 1;
	    return array;
	} else {
	    for (int i = array_len - 1; i >= 0; i--) {
		if (carry == 1) {
		    if (array[i] == 9) {
			answer.add(0);
		    } else {
			answer.add(array[i] + 1);
			carry = 0;
		    }

		} else {
		    answer.add(array[i]);
		}

	    }
	}

	if (carry == 1)
	    answer.add(1);

	int ansArr[] = new int[answer.size()];
	for (int i = answer.size() - 1; i >= 0; i--)
	    ansArr[answer.size() - 1 - i] = answer.get(i);

	return ansArr;

    }

}
