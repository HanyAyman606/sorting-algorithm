package com.example.sortingalgorithm.algorithms;

import com.example.sortingalgorithm.tracker.EinSehrya;

public class HeapSort implements SortingAlgorithm {

    @Override
    public void sort(int[] arr1, EinSehrya einSehrya) {
        int n = arr1.length;

        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr1, n, i, einSehrya);
        }

        for (int i = n - 1; i > 0; i--) {
            einSehrya.swap(0, i);
            int temp = arr1[0];
            arr1[0] = arr1[i];
            arr1[i] = temp;

            einSehrya.it_is_sorted(i);

            heapify(arr1, i, 0, einSehrya);
        }

        einSehrya.it_is_sorted(0);
    }

    private void heapify(int[] arr1, int n, int i, EinSehrya einSehrya) {
        int largest = i;
        int left    = 2 * i + 1;   
        int right   = 2 * i + 2;   

        if (left < n) {
            einSehrya.compare(left, largest);
            if (arr1[left] > arr1[largest]) {
                largest = left;
            }
        }

        if (right < n) {
            einSehrya.compare(right, largest);
            if (arr1[right] > arr1[largest]) {
                largest = right;
            }
        }

        if (largest != i) {
            einSehrya.swap(i, largest);
            int temp = arr1[i];
            arr1[i] = arr1[largest];
            arr1[largest] = temp;

            heapify(arr1, n, largest, einSehrya);
        }
    }
}
