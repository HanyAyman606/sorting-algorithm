package com.example.sortingalgorithm.algorithms;

import com.example.sortingalgorithm.tracker.EinSehrya;

public class SelectionSort implements SortingAlgorithm{
    @Override
    public void sort(int[] arr1, EinSehrya einSehrya) {
        int n = arr1.length;
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            for (int j = i + 1; j < n; j++) {
                einSehrya.compare(min, j);
                if (arr1[j] < arr1[min]) {
                    min = j;
                }
            }
            if (min != i) {
                einSehrya.swap(i, min);
                int temp = arr1[i];
                arr1[i] = arr1[min];
                arr1[min] = temp;
            }
            einSehrya.it_is_sorted(i);
        }
        einSehrya.it_is_sorted(n - 1);
    }
}