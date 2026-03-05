package com.example.sortingalgorithm.algorithms;

import com.example.sortingalgorithm.tracker.EinSehrya;

public class InsertionSort {
    public void sort(int[] arr1, EinSehrya einSehrya) {
        int n = arr1.length;
        einSehrya.it_is_sorted(0);
        for (int i = 1; i < n; i++) {
            int temp = arr1[i];
            int j = i - 1;
            while (j >= 0 && arr1[j] > temp) {
                einSehrya.compare(j, j + 1);
                einSehrya.swap(j, j + 1);
                arr1[j + 1] = arr1[j];
                j--;
            }
            if (j >= 0) {
                einSehrya.compare(j, j + 1);
            }
            arr1[j + 1] = temp;
            einSehrya.it_is_sorted(i);
        }
    }
}