package main;

public class MergeTwoSortedArrays {

    public static void main(String args[]) {
	int a1[] = { 1, 3, 5, 7, 9, 23 };
	int a2[] = { 2, 4, 6, 8, 10, 12, 14 };
	int a3[] = new int[a1.length + a2.length];

	int a1Index = 0;
	boolean a1OverFlow = false;
	int a2Index = 0;
	boolean a2OverFlow = false;

	while (true) {
	    if (a1[a1Index] < a2[a2Index]) {
		a3[a1Index + a2Index] = a1[a1Index];
		a1Index++;
	    } else {
		a3[a1Index + a2Index] = a2[a2Index];
		a2Index++;

	    }

	    if (a1Index == a1.length) {
		a1OverFlow = true;
		break;

	    }
	    if (a2Index == a2.length) {
		a2OverFlow = true;
		break;
	    }

	}

	if (a1OverFlow) {
	    for (int index = a2Index; index < a2.length; index++)
		a3[a1Index + index] = a2[index];
	}

	if (a2OverFlow) {
	    for (int index = a1Index; index < a1.length; index++)
		a3[a2Index + index] = a1[index];
	}

	for (int num : a3)
	    System.out.print(num + ",");

    }
}
