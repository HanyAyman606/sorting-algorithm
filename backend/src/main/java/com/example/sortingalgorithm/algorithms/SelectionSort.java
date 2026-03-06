package com.example.sortingalgorithm.algorithms;

import com.example.sortingalgorithm.tracker.EinSehrya;

public class SelectionSort implements SortingAlgorithm {
    @Override
    public void sort(int[] arr1, EinSehrya einSehrya) {
        int n = arr1.length;

        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;

            for (int j = i + 1; j < n; j++) {
                einSehrya.compare(minIdx, j);
                if (arr1[j] < arr1[minIdx]) {
                    minIdx = j;
                }
            }

            if (minIdx != i) {
                einSehrya.swap(i, minIdx);
                int temp    = arr1[i];
                arr1[i]     = arr1[minIdx];
                arr1[minIdx] = temp;
            }

            einSehrya.it_is_sorted(i);
        }

        einSehrya.it_is_sorted(n - 1);
    }
}
