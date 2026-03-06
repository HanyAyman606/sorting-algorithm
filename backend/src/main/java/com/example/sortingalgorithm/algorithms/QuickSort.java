package com.example.sortingalgorithm.algorithms;

import com.example.sortingalgorithm.tracker.EinSehrya;

public class QuickSort implements SortingAlgorithm {

    @Override
    public void sort(int[] arr1, EinSehrya einSehrya) {
        quickSort(arr1, 0, arr1.length - 1, einSehrya);
    }

    private void quickSort(int[] arr1, int lower, int upper, EinSehrya einSehrya) {
        if (lower >= upper) {
            if (lower == upper) {
                einSehrya.it_is_sorted(lower);
            }
            return;
        }

        int loc = partition(arr1, lower, upper, einSehrya);
        einSehrya.it_is_sorted(loc);
        quickSort(arr1, lower, loc - 1, einSehrya);
        quickSort(arr1, loc + 1, upper, einSehrya);
    }

    private void medianOfThree(int[] arr1, int lower, int upper, EinSehrya einSehrya) {
        int mid = (lower + upper) / 2;

        if (arr1[lower] > arr1[mid]) {
            einSehrya.compare(lower, mid);
            einSehrya.swap(lower, mid);
            int temp = arr1[lower]; arr1[lower] = arr1[mid]; arr1[mid] = temp;
        }

        if (arr1[lower] > arr1[upper]) {
            einSehrya.compare(lower, upper);
            einSehrya.swap(lower, upper);
            int temp = arr1[lower]; arr1[lower] = arr1[upper]; arr1[upper] = temp;
        }

        if (arr1[mid] > arr1[upper]) {
            einSehrya.compare(mid, upper);
            einSehrya.swap(mid, upper);
            int temp = arr1[mid]; arr1[mid] = arr1[upper]; arr1[upper] = temp;
        }

        einSehrya.swap(mid, upper - 1);
        int tmp = arr1[mid];
        arr1[mid] = arr1[upper - 1];
        arr1[upper - 1] = tmp;
    }

    private int partition(int[] arr1, int lower, int upper, EinSehrya einSehrya) {
        if (upper - lower >= 3) {
            medianOfThree(arr1, lower, upper, einSehrya);
        }

        int pivotIndex = (upper - lower >= 3) ? upper - 1 : upper;
        int pivot = arr1[pivotIndex];
        einSehrya.pivot(pivotIndex);

        int i = lower - 1;

        for (int j = lower; j < pivotIndex; j++) {
            einSehrya.compare(j, pivotIndex);
            if (arr1[j] <= pivot) {
                i++;
                if (i != j) {
                    einSehrya.swap(i, j);
                    int temp = arr1[i]; arr1[i] = arr1[j]; arr1[j] = temp;
                }
            }
        }

        int finalPos = i + 1;
        if (finalPos != pivotIndex) {
            einSehrya.swap(finalPos, pivotIndex);
            int temp = arr1[finalPos];
            arr1[finalPos] = arr1[pivotIndex];
            arr1[pivotIndex] = temp;
        }

        return finalPos;
    }
}
