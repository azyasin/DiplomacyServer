package Testing;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;

public class TestingRandom {
	public static void main (String args[]) {
		//int[][] totals = new int[6][6];
		Integer[] demo = {0, 1, 2, 3, 4, 5};

		for (int i = 0; i <= 1000; i++){
			Collections.shuffle(Arrays.asList(demo));
			for (int j = 0; j < 6; j++) {
				System.out.print(demo[j] + " ");
			}
			System.out.println();
		}
	}
}
