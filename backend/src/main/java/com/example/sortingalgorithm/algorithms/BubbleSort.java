package com.example.sortingalgorithm.algorithms;

import com.example.sortingalgorithm.tracker.EinSehrya;

public class BubbleSort implements SortingAlgorithm {
    @Override
    public void sort(int[] arr1, EinSehrya einSehrya) {
        int n = arr1.length;

        for (int i = 0; i < n; i++) {
            boolean swapped = false;

            for (int j = 0; j < n - i - 1; j++) {
                einSehrya.compare(j, j + 1);
                if (arr1[j] > arr1[j + 1]) {
                    einSehrya.swap(j, j + 1);
                    int temp = arr1[j];
                    arr1[j]     = arr1[j + 1];
                    arr1[j + 1] = temp;
                    swapped = true;
                }
            }

            einSehrya.it_is_sorted(n - i - 1);

            if (!swapped) {
                for (int k = 0; k <= n - i - 2; k++) {
                    einSehrya.it_is_sorted(k);
                }
                break;
            }
        }
    }
}
