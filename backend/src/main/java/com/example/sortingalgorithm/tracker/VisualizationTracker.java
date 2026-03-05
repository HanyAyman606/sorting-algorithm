package com.example.sortingalgorithm.tracker;

import com.example.sortingalgorithm.dto.SortStep;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VisualizationTracker implements EinSehrya {

    private List<SortStep> steps = new ArrayList<>();
    private long comparisons = 0;
    private long interchanges = 0;

    @Override
    public void compare(int i, int j) {
        steps.add(new SortStep("compare", Arrays.asList(i, j)));
        comparisons++;
    }

    @Override
    public void swap(int i, int j) {
        steps.add(new SortStep("swap", Arrays.asList(i, j)));
        interchanges++;
    }

    @Override
    public void overwrite(int index, int value) {
        steps.add(new SortStep("overwrite", Arrays.asList(index), Arrays.asList(value)));
        interchanges++;
    }

    @Override
    public void it_is_sorted(int index) {
        steps.add(new SortStep("markSorted", Arrays.asList(index)));
    }

    @Override
    public void pivot(int index) {
        steps.add(new SortStep("pivot", Arrays.asList(index)));
    }

    public List<SortStep> getSteps() { return steps; }
    public long getComparisons() { return comparisons; }
    public long getInterchanges() { return interchanges; }

    public void reset() {
        steps.clear();
        comparisons = 0;
        interchanges = 0;
    }
}