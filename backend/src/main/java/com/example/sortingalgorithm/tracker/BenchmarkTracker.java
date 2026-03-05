package com.example.sortingalgorithm.tracker;

public class BenchmarkTracker implements EinSehrya {

    private long comparisons = 0;
    private long interchanges = 0;

    @Override
    public void compare(int i, int j) {
        comparisons++;
    }

    @Override
    public void swap(int i, int j) {
        interchanges++;
    }

    @Override
    public void overwrite(int index, int value) {
        interchanges++;
    }

    @Override
    public void it_is_sorted(int index) {
    }

    @Override
    public void pivot(int index) {
    }

    public long getComparisons() { return comparisons; }
    public long getInterchanges() { return interchanges; }

    public void reset() {
        comparisons = 0;
        interchanges = 0;
    }
}