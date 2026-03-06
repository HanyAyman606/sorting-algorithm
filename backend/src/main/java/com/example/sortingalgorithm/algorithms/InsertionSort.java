package com.example.sortingalgorithm.algorithms;

import com.example.sortingalgorithm.tracker.EinSehrya;

public class InsertionSort implements SortingAlgorithm {
    @Override
    public void sort(int[] arr1, EinSehrya einSehrya) {
        int n = arr1.length;
        einSehrya.it_is_sorted(0);

        for (int i = 1; i < n; i++) {
            int key = arr1[i];
            int j = i - 1;

            while (j >= 0 && arr1[j] > key) {
                einSehrya.compare(j, j + 1);
                einSehrya.overwrite(j + 1, arr1[j]);
                arr1[j + 1] = arr1[j];
                j--;
            }

            if (j >= 0) {
                einSehrya.compare(j, j + 1);
            }

            arr1[j + 1] = key;
            einSehrya.overwrite(j + 1, key);
            einSehrya.it_is_sorted(i);
        }
    }
}
