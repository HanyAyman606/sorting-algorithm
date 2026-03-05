package com.example.sortingalgorithm.algorithms;

import com.example.sortingalgorithm.tracker.EinSehrya;

public class MergeSort implements SortingAlgorithm {

    @Override
    public void sort(int[] arr1, EinSehrya einSehrya) {
        mergeSort(arr1, 0, arr1.length - 1, einSehrya);
    }
 private void mergeSort(int[] arr1, int lower, int upper, EinSehrya einSehrya) {
        if (lower >= upper) {
            if (lower == upper) {
                einSehrya.it_is_sorted(lower);
            }
            return;
        }
        int mid = (lower + upper) / 2;
        mergeSort(arr1, lower, mid, einSehrya);
        mergeSort(arr1, mid + 1, upper, einSehrya);
        merge(arr1, lower, mid, upper, einSehrya);
    }

    private void merge(int[] arr1, int lower, int mid, int upper, EinSehrya einSehrya) {
        int n1 = mid - lower + 1;
        int n2 = upper - mid;

        int[] arr2 = new int[n1];
        int[] arr3 = new int[n2];

        for (int i = 0; i < n1; i++)
            arr2[i] = arr1[lower + i];
        for (int j = 0; j < n2; j++)
            arr3[j] = arr1[mid + 1 + j];

        int i = 0, j = 0, k = lower;

        while (i < n1 && j < n2) {
            einSehrya.compare(lower + i, mid + 1 + j);
            if (arr2[i] <= arr3[j]) {
                einSehrya.overwrite(k, arr2[i]);
                arr1[k] = arr2[i];
                i++;
            } else {
                einSehrya.overwrite(k, arr3[j]);
                arr1[k] = arr3[j];
                j++;
            }
            k++;
        }

        while (i < n1) {
            einSehrya.overwrite(k, arr2[i]);
            arr1[k] = arr2[i];
            i++;
            k++;
        }

        while (j < n2) {
            einSehrya.overwrite(k, arr3[j]);
            arr1[k] = arr3[j];
            j++;
            k++;
        }

        for (int m = lower; m <= upper; m++) {
            einSehrya.it_is_sorted(m);
        }
    }
}