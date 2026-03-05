package com.example.sortingalgorithm.algorithms;

import com.example.sortingalgorithm.tracker.EinSehrya;

public class HeapSort implements SortingAlgorithm {

    @Override
    public void sort(int[] arr1, EinSehrya einSehrya) {
        int n = arr1.length;

        int[] heap = new int[n + 1];
        for (int i = 0; i < n; i++)
            heap[i + 1] = arr1[i];

        for (int i = n / 2; i >= 1; i--) {
            heapify(heap, n, i, einSehrya);
        }

        for (int i = n; i > 1; i--) {
            einSehrya.swap(0, i - 1);
            int temp = heap[1];
            heap[1] = heap[i];
            heap[i] = temp;

            einSehrya.it_is_sorted(i - 1);

            heapify(heap, i - 1, 1, einSehrya);
        }
        einSehrya.it_is_sorted(0);

        for (int i = 0; i < n; i++)
            arr1[i] = heap[i + 1];
    }

    private void heapify(int[] heap, int n, int i, EinSehrya einSehrya) {
        int largest = i;
        int left  = 2 * i;
        int right = 2 * i + 1;

        if (left <= n && heap[left] > heap[largest]) {
            einSehrya.compare(left - 1, largest - 1);
            largest = left;
        }

        if (right <= n && heap[right] > heap[largest]) {
            einSehrya.compare(right - 1, largest - 1);
            largest = right;
        }

        if (largest != i) {
            einSehrya.swap(i - 1, largest - 1);
            int temp = heap[i];
            heap[i] = heap[largest];
            heap[largest] = temp;
            heapify(heap, n, largest, einSehrya);
        }
    }
}